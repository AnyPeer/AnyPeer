package any.xxx.anypeer.moudle.main;

import android.Manifest;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.provider.Settings;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.PopupMenu.OnMenuItemClickListener;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.google.gson.Gson;
import com.kaopiz.kprogresshud.KProgressHUD;

import org.elastos.carrier.ConnectionStatus;
import org.elastos.carrier.FriendInfo;
import org.elastos.carrier.UserInfo;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import any.xxx.anypeer.R;
import any.xxx.anypeer.app.BaseActivity;
import any.xxx.anypeer.app.ForegroundCallbacks;
import any.xxx.anypeer.bean.User;
import any.xxx.anypeer.chatbean.ChatMessage;
import any.xxx.anypeer.db.ChatDBManager;
import any.xxx.anypeer.db.FriendManager;
import any.xxx.anypeer.manager.ChatManager;
import any.xxx.anypeer.moudle.barcode.BarCodeActivity;
import any.xxx.anypeer.moudle.chat.ChatActivity;
import any.xxx.anypeer.moudle.chat.MessageService;
import any.xxx.anypeer.moudle.chat.MessageService.IMessageCallback;
import any.xxx.anypeer.moudle.chat.TransferAccountsActivity;
import any.xxx.anypeer.moudle.friend.FriendDetailActivity;
import any.xxx.anypeer.util.AnyWallet;
import any.xxx.anypeer.util.EventBus;
import any.xxx.anypeer.util.FileUtils;
import any.xxx.anypeer.util.NetUtils;
import any.xxx.anypeer.util.PrefereneceUtil;
import any.xxx.anypeer.util.Utils;
import any.xxx.anypeer.zxing.CaptureActivity;

import static any.xxx.anypeer.moudle.chat.ChatActivity.IS_CHAT_START;

public class MainActivity extends BaseActivity implements IMessageCallback, OnMenuItemClickListener, EventBus.Callback {
    private static final String TAG = "MainActivity";
    private static final int REQUEST_CAMERA = 0;

    private int mIndex;
    private int mCurrentTabIndex;
    private List<ImageView> mImagebuttons;
    private List<TextView> mTextviews;

    private List<Fragment> mFragments;
    private MessageFragment mMessageFragment;
    private FriendFragment mFriendFragment;
    private View mLayout;
    private TextView mTxtTitle;
    private MessageService.NetHandler mHandler = null;
    private NetUtils sNetUtils = null;
    private MessageService mMessageService;
    private ServiceConnection mServiceConnection;
    private ChatManager mChatManager;
    private KProgressHUD mKProgressHUD;
    private boolean mIsReady = false;
    private AnyWallet mAnyWallet;
    private String mName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        init();
        findViewById();
    }

    private void init() {
        mServiceConnection = new MessageServiceConn();
        bindService(new Intent(this, MessageService.class), mServiceConnection, BIND_AUTO_CREATE);

        mHandler = new MessageService.NetHandler();

        mKProgressHUD = KProgressHUD.create(MainActivity.this)
                .setStyle(KProgressHUD.Style.SPIN_INDETERMINATE)
                .setCancellable(true)
                .setAnimationSpeed(2)
                .setDimAmount(0.5f)
                .show();

        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                sNetUtils = NetUtils.getInstance(MainActivity.this, mHandler);
            }
        });
        thread.start();

        mChatManager = ChatManager.getInstance(this);

        EventBus.getInstance().addCallback(this);

        mAnyWallet = AnyWallet.getInstance(this);
        if (mAnyWallet != null) {
            mAnyWallet.login(null);
        }

        initTabView();

        User user = new Gson().fromJson(PrefereneceUtil.getString(MainActivity.this
                , User.USER), User.class);
        if (user != null) {
            mName = user.getUserName();
        }
    }

    private boolean checkAddFriend() {
        try {
            return PrefereneceUtil.getBoolean(MainActivity.this, Utils.ADD_FRIEND_CHECK);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    private List<String> mReAddFriendAddress = new ArrayList<>();

    @Override
    public void processMessage(Message msg) {
        String testString = "ERROR";
        int what = msg.what;
        switch (what) {
            case NetUtils.ANYSTATE.READY: {
                mIsReady = true;
                //the local network is ready.
                updateSelf();
                testString = "the Network is READY";
                mKProgressHUD.dismiss();
                checkNotification();

                break;
            }
            case NetUtils.ANYSTATE.FRIENDADDED: {
                //TODO a friend is added.
                FriendInfo info = (FriendInfo) msg.obj;
                if (info != null) {
                    String id = info.getUserId();
                    if (!mFriendFragment.isRobot(id)) {
                        String name = info.getName();

                        mChatManager.addFriend(name, id, info.getGender());
                        mMessageFragment.onReflash();

                        //Update the contact list.
//                        mFriendFragment.addNewUser(info);
                        testString = "A friend is added.";

                        User user = new User(name, id);
                        FriendManager.getInstance().addFriend(user);

                        mFriendFragment.initContactList();
                    }
                }

                break;
            }
            case NetUtils.ANYSTATE.FRIENDREQUEST: {
                //TODO friend request. default to accept the request.
                Bundle data = msg.getData();
                String userId = data.getString(NetUtils.ANYSTATE.USERID);
                String hello = data.getString(NetUtils.ANYSTATE.HELLO);

                final User user = new User();
                user.setUserName(hello);
                user.setUserId(userId);
                user.setEmail("");
                user.setRegion("");

                if (checkAddFriend()) {
                    try {
                        if (NotificationManagerCompat.from(this).areNotificationsEnabled()) {
                            showNotification(this, 111, getString(R.string.friend_request_apply), hello, user);
                        } else {
                            new MaterialDialog.Builder(MainActivity.this)
                                    .title(R.string.friend_request_apply)
                                    .content(getString(R.string.friend_request_apply_1) + userId + getString(R.string.friend_request_apply_2))
                                    .negativeText(R.string.friend_add_no)
                                    .onNegative((dialog, which) -> dialog.dismiss())
                                    .positiveText(R.string.friend_add_yes)
                                    .onPositive((dialog, which) -> {
                                        Intent intent = new Intent(MainActivity.this, FriendDetailActivity.class);
                                        intent.putExtra(Utils.USERID, user.getUserId());
                                        startActivity(intent);
                                    }).show();
                        }
                    } catch (Exception e) {
                        Log.e(TAG, e.getMessage());
                    }
                } else {
                    sNetUtils.accept(userId);
                }

                testString = "A friend request.";
                break;
            }
            case NetUtils.ANYSTATE.FRIENDMESSAGE: {
                //TODO a message is coming.

                Bundle data = msg.getData();
                if (data != null) {
                    String userId = data.getString(NetUtils.ANYSTATE.FROM);
                    String message = data.getString(NetUtils.ANYSTATE.MSG);

                    if (ForegroundCallbacks.get().isBackground()) {
                        if (ChatManager.getInstance(this).getIsMessageShowNotification(this)) {
                            if (NotificationManagerCompat.from(this).areNotificationsEnabled()) {
                                User user = FriendManager.getInstance().getUserById(userId);
                                // TODO: get 3 char
                                int id = Integer.valueOf(Utils.stringToAscii(userId.substring(0, 3)));
                                int unread = ChatDBManager.getInstance().getChatConversaionByUserId(userId).getUnreadMsgCount() + 1;
                                String unreadString = getString(R.string.unread_number, String.valueOf(unread));
                                showMessageNotification(this, id, user.getUserName() + " [" + unreadString + "]", message, user);
                            }
                        }
                    }

                    if (IS_CHAT_START) {
                        return;
                    }

                    Log.d(TAG, "FRIENDMESSAGE:=" + message);
                    if (mFriendFragment.isRobot(userId)) {
                        //TODO
                        showTestInfo("Robot say: " + message);
                        mFriendFragment.applyResult(message);
                    } else {
                        ChatMessage chatMessage = new ChatMessage();
                        chatMessage.setMsgId(UUID.randomUUID().toString());
                        chatMessage.setDirect(ChatMessage.Direct.RECEIVE.ordinal());
                        chatMessage.setUnread(true);
                        chatMessage.setType(ChatMessage.Type.TXT.ordinal());
                        chatMessage.setMessage(message);

//                        mChatManager.addMessage(MainActivity.this, userId, emMessage);
                        ChatDBManager.getInstance().addMessage(userId, chatMessage);
                        mMessageFragment.onReflash();
                    }
                }

                testString = "A new message is coming, need to process";

                break;
            }
            case NetUtils.ANYSTATE.FILE_TRANSFER: {
                //TODO : A File data is coming.
                Bundle data = msg.getData();
                if (data != null) {
                    String userId = data.getString(NetUtils.ANYSTATE.FROM);
                    byte[] fileData = data.getByteArray(NetUtils.ANYSTATE.FILEDATA);
                    ChatMessage.Type msgType = ChatMessage.Type.getMsgType(msg.arg1);
                    Log.d(TAG, "processMessage========msg=" + msg.what + ", userId=" + userId + ", msgType=" + msgType);

                    String filePath = FileUtils.bytesToFile(fileData);
                    if (filePath == null || filePath.isEmpty()) {
                        return;
                    }

                    String messageString = "";

                    switch (msgType) {
                        case MONEY:
                            messageString = getString(R.string.newmsg_recvtransfer);
                            break;

                        case IMAGE:
                            messageString = getString(R.string.newmsg_image);
                            break;

                        case VOICE:
                            messageString = getString(R.string.newmsg_voice);
                            break;

                        case VIDEO:
                            messageString = getString(R.string.newmsg_video);
                            break;

                    }


                    if (ForegroundCallbacks.get().isBackground()) {
                        if (ChatManager.getInstance(this).getIsMessageShowNotification(this)) {
                            if (NotificationManagerCompat.from(this).areNotificationsEnabled()) {
                                User user = FriendManager.getInstance().getUserById(userId);
                                // TODO: get 3 char
                                int id = Integer.valueOf(Utils.stringToAscii(userId.substring(0, 3)));
                                int unread = ChatDBManager.getInstance().getChatConversaionByUserId(userId).getUnreadMsgCount() + 1;
                                String unreadString = getString(R.string.unread_number, String.valueOf(unread));
                                showMessageNotification(this, id, user.getUserName() + " [" + unreadString + "]", messageString, user);
                            }
                        }
                    }

                    if (IS_CHAT_START) {
                        return;
                    }

                    ChatMessage message = new ChatMessage();
                    message.setMsgId(UUID.randomUUID().toString());
                    message.setDirect(ChatMessage.Direct.RECEIVE.ordinal());
                    message.setType(msgType.ordinal());
                    message.setUnread(true);
                    // Set the message body
                    if (msgType == ChatMessage.Type.IMAGE) {
                        message.setFilePath(filePath);
                    } else if (msgType == ChatMessage.Type.VOICE) {
                        message.setFilePath(filePath);
                    } else if (msgType == ChatMessage.Type.VIDEO) {
                        MediaMetadataRetriever mmr = new MediaMetadataRetriever();
                        mmr.setDataSource(filePath);//设置数据源为该文件对象指定的绝对路径
                        Bitmap bitmap = mmr.getFrameAtTime();

                        String imagePath = getFilesDir() + "/";

                        String imageFilePath = imagePath + "_" + System.currentTimeMillis() + ".png";
                        FileUtils.saveBitmap(imageFilePath, bitmap);

                        message.setLocalThumb(imageFilePath);
                        message.setFilePath(filePath);
                    }
//                    mChatManager.addMessage(MainActivity.this, userId, message);
                    ChatDBManager.getInstance().addMessage(userId, message);

                    mMessageFragment.onReflash();
                }
            }
            case NetUtils.ANYSTATE.CONNECTION: {
                //TODO the local network is connected.
                testString = "CONNECTION";
                break;
            }
            case NetUtils.ANYSTATE.FRIENDCONNECTION: {
                //TODO the friend is online.
                if (msg.arg1 == ConnectionStatus.Connected.value()) {
                    testString = "A friend is online";
                } else {
                    testString = "A friend is offline";
                }

                String userId = (String) msg.obj;
                if (userId != null && mFriendFragment.isRobot(userId)) {
                    mFriendFragment.updateRobot(msg.arg1 == ConnectionStatus.Connected.value());
                } else {
                    mFriendFragment.initContactList();
                    mMessageFragment.onReflash();
                }

                break;
            }
            case NetUtils.ANYSTATE.SELFINFOCHANGED: {
                testString = "My information is changed.";
                break;
            }
            case NetUtils.ANYSTATE.FRIENDINFOCHANGED: {
                FriendInfo info = (FriendInfo) msg.obj;
                Bundle data = msg.getData();
                if (info != null) {
                    String userId = data.getString(NetUtils.ANYSTATE.USERID);
                    Log.d(TAG, String.format("FRIENDINFOCHANGED  label=[%s], name=[%s], userId=[%s], des=[%s]",
                            info.getLabel(), info.getName(), userId, info.getDescription()));
                    mChatManager.updateFriend(info.getName(), userId, info.getDescription()
                            , info.getGender(), info.getEmail());
                    FriendManager.getInstance().updateUserInfo(userId, info.getName(), info.getGender(),
                            info.getRegion(), info.getEmail(), info.getDescription());
                    mMessageFragment.onReflash();

                    //Update the contact list.
                    mFriendFragment.initContactList();
                    testString = "A friend's information is changed.";
                }

                break;
            }
            case NetUtils.ANYSTATE.FRIENDREMOVED: {
                //TODO a friend is removed.
                String id = (String) msg.obj;
                if (id != null) {
                    //Update the contact list.
                    mFriendFragment.removeUser(id);
                    testString = "A friend is removed.";
                }
                break;
            }
            case NetUtils.ANYSTATE.FRIENDINVITEREQUEST: {
                break;
            }
            case NetUtils.ANYSTATE.FILE_TRANSFER_FEEDBACK: {
                Bundle data = msg.getData();
                if (data != null) {
                    String userId = data.getString(NetUtils.ANYSTATE.FROM);
                    String msgId = data.getString(NetUtils.ANYSTATE.MSG);
//                    mChatManager.setMessageStatus(MainActivity.this, userId, msgId, EMMessage.Status.SUCCESS);

                    ChatDBManager.getInstance().setMessageStatus(msgId, ChatMessage.Status.SUCCESS);
                }
            }
            default: {
                testString = "";
            }
        }

        showTestInfo(testString);
    }

    private void showTestInfo(String info) {
        if (info != null && !info.isEmpty()) {
//            Toast.makeText(this, info, Toast.LENGTH_LONG).show();
        }
    }

    private void updateSelf() {
        User user = new Gson().fromJson(PrefereneceUtil.getString(this, User.USER), User.class);
        UserInfo self = sNetUtils.getSelfInfo();
        self.setName(user.getUserName());
        self.setEmail(user.getEmail());
        self.setGender(user.getGender());

        if (mAnyWallet != null) {
            self.setDescription(mAnyWallet.getWalletAddress());
        } else {
            self.setDescription("NONE");
        }

        //Check robot
        mFriendFragment.initRobotUserId(user.getUserName());

        sNetUtils.setSelfInfo(self);
    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.scan: {
                showCamera();
                break;
            }
            case R.id.paymentandgathering: {
                if (mIsReady) {
                    try {
                        String receipAddress = mAnyWallet.getWalletAddress();
                        Intent intent = new Intent(this, BarCodeActivity.class);
                        intent.putExtra(Utils.QRCODETYPE, Utils.QrCodeType.Wallet_Address.ordinal());
                        intent.putExtra(Utils.ADDRESS, receipAddress);
                        startActivity(intent);
                    } catch (Exception e) {
                        Utils.showShortToast(MainActivity.this, "Start the receipt-address Activity failed.");
                    }
                } else {
                    Utils.showShortToast(MainActivity.this, "The Network is not ready.");
                }

                break;
            }

            case R.id.friend:
                Intent intent = new Intent(this, AddFriendActivity.class);
                startActivityForResult(intent, REQ_QR_CODE);
                break;

            default:
                break;
        }
        return false;
    }

    @Override
    public void callback() {
        new Handler().postDelayed(() -> onTabClicked(R.id.re_anypeer), 300);

        mMessageFragment.onReflash();
    }

    private class MessageServiceConn implements ServiceConnection {
        @Override
        public void onServiceConnected(ComponentName name, IBinder binder) {
            if (mMessageService == null) {
                mMessageService = ((MessageService.LocalBinder) binder).getService();
                mMessageService.addMessageCallback(MainActivity.this);
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mMessageService.removeMessageCallback(MainActivity.this);
            mMessageService = null;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbindService(mServiceConnection);

        try {
            mMessageService.removeMessageCallback(MainActivity.this);
            mMessageService = null;

            sNetUtils.kill();
            mAnyWallet.onDestroy();
        } catch (Exception e) {
            //
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        try {
            mAnyWallet.onPause();
        } catch (Exception e) {
            //
        }
    }

    private void findViewById() {
        mLayout = findViewById(R.id.fragment_container);
        mTxtTitle = findViewById(R.id.txt_title);
        ImageView moreTools = findViewById(R.id.img_right);
        moreTools.setVisibility(View.VISIBLE);
        moreTools.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                PopupMenu popup = new PopupMenu(MainActivity.this, view);
                MenuInflater inflater = popup.getMenuInflater();
                inflater.inflate(R.menu.more, popup.getMenu());
                popup.setOnMenuItemClickListener(MainActivity.this);
                popup.show();
            }
        });

        mTxtTitle.setText(getString(R.string.app_name));
    }

    private void initTabView() {
        mImagebuttons = new ArrayList<>();
        mImagebuttons.add(findViewById(R.id.ib_anypeer));
        mImagebuttons.add(findViewById(R.id.ib_contact_list));
        mImagebuttons.add(findViewById(R.id.ib_profile));

        mImagebuttons.get(0).setSelected(true);
        mTextviews = new ArrayList<>();
        mTextviews.add(findViewById(R.id.tv_anypeer));
        mTextviews.add(findViewById(R.id.tv_contact_list));
        mTextviews.add(findViewById(R.id.tv_profile));
        mTextviews.get(0).setTextColor(ContextCompat.getColor(this, R.color.colorPrimary));

        mMessageFragment = new MessageFragment();
        mFriendFragment = new FriendFragment();
        MeFragment meFragment = new MeFragment();

        mFragments = new ArrayList<>();
        mFragments.add(mMessageFragment);
        mFragments.add(mFriendFragment);
        mFragments.add(meFragment);

        getSupportFragmentManager().beginTransaction()
                .add(R.id.fragment_container, mMessageFragment)
                .add(R.id.fragment_container, mFriendFragment)
                .add(R.id.fragment_container, meFragment)
                .hide(mFriendFragment).hide(meFragment)
                .show(mMessageFragment).commit();

        findViewById(R.id.re_anypeer).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onTabClicked(view.getId());
            }
        });

        findViewById(R.id.re_contact_list).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onTabClicked(view.getId());
            }
        });

        findViewById(R.id.re_profile).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onTabClicked(view.getId());
            }
        });
    }

    public void onTabClicked(int id) {
        try {
            switch (id) {
                case R.id.re_anypeer:
                    mIndex = 0;
                    mTxtTitle.setText(getString(R.string.app_name));
                    break;
                case R.id.re_contact_list:
                    mIndex = 1;
                    mTxtTitle.setText(getString(R.string.contacts));
                    break;
                case R.id.re_profile:
                    mIndex = 2;
                    mTxtTitle.setText(getString(R.string.mine));
                    break;
            }
            if (mCurrentTabIndex != mIndex) {
                FragmentTransaction trx = getSupportFragmentManager().beginTransaction();
                trx.hide(mFragments.get(mCurrentTabIndex));
                if (!mFragments.get(mIndex).isAdded()) {
                    trx.add(R.id.fragment_container, mFragments.get(mIndex));
                }
                trx.show(mFragments.get(mIndex)).commitAllowingStateLoss();
            }

            mImagebuttons.get(mCurrentTabIndex).setSelected(false);
            // Set the selected image.
            mImagebuttons.get(mIndex).setSelected(true);
            mTextviews.get(mCurrentTabIndex).setTextColor(0xFF999999);
            mTextviews.get(mIndex).setTextColor(ContextCompat.getColor(this, R.color.colorPrimary));
            mCurrentTabIndex = mIndex;
        } catch (Exception e) {
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
            Log.e(TAG, e.getMessage());
        }
    }

    private static final int REQ_QR_CODE = 1010;

    public void showCamera() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            requestCameraPermission();
        } else {
            Intent intent = new Intent(MainActivity.this, CaptureActivity.class);
            startActivityForResult(intent, REQ_QR_CODE);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        try {
            if (requestCode == REQ_QR_CODE && resultCode == RESULT_OK) {
                Bundle bundle = data.getExtras();
                if (bundle == null) return;

                String scanResult = bundle.getString("result");
                if (scanResult != null) {
                    String[] args = scanResult.split("\\s+");
                    final String address = args[0];
                    if (mAnyWallet.isAddressValid(address)) {
                        Intent intent = new Intent(MainActivity.this, TransferAccountsActivity.class);
                        intent.putExtra(TransferAccountsActivity.NAME, "");
                        intent.putExtra(TransferAccountsActivity.GENDER, "");
                        intent.putExtra(TransferAccountsActivity.ADDRESS, address);
                        startActivity(intent);
                        return;
                    }

                    String name = "NULL";
                    if (args.length >= 2) {
                        name = args[1];
                    }

                    if (NetUtils.isValidAddress(address)) {
                        MaterialDialog.Builder builder = new MaterialDialog.Builder(MainActivity.this);
                        builder.onNegative((dialog, which) -> dialog.dismiss());

                        builder.title(R.string.friend_add_text1);
                        builder.content(getString(R.string.friend_add_ask_1) + name + getString(R.string.friend_add_ask_2));

                        builder.negativeText(R.string.friend_add_no);
                        builder.positiveText(R.string.friend_add_yes);

                        builder.onPositive((dialog, which) -> {
                            User user = new Gson().fromJson(PrefereneceUtil.getString(MainActivity.this
                                    , User.USER), User.class);

                            //TODO: maybe make the user input some thing.
                            sNetUtils.addFriend(address, user.getUserName());

                            dialog.dismiss();
                        }).show();
                    }

                    //TODO
                    if (args.length >= 2) {
                        String userName = args[1];
                        Log.d(TAG, "The new friend's name ======================" + userName);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Request Camera Permission
     */
    private void requestCameraPermission() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                Manifest.permission.CAMERA)) {
            Snackbar.make(mLayout, getString(R.string.permission_camera),
                    Snackbar.LENGTH_INDEFINITE)
                    .setAction(getString(R.string.text_ok), new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            ActivityCompat.requestPermissions(MainActivity.this,
                                    new String[]{Manifest.permission.CAMERA},
                                    REQUEST_CAMERA);
                        }
                    })
                    .show();
        } else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA},
                    REQUEST_CAMERA);
        }
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_HOME);
        startActivity(intent);
    }

    private void showNotification(Context context, int id, String title, String text, User user) {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context);
        builder.setSmallIcon(R.mipmap.app_icon);
        builder.setLargeIcon(BitmapFactory.decodeResource(context.getResources(), R.mipmap.app_icon));
        builder.setContentTitle(title);
        builder.setContentText(text);
        builder.setAutoCancel(true);
        builder.setOnlyAlertOnce(true);


        // User define
        Intent resultIntent = new Intent(context, FriendDetailActivity.class);
        resultIntent.putExtra(FriendDetailActivity.TYPE, FriendDetailActivity.TYPE_ADD_FRIEND);
        resultIntent.putExtra(Utils.USERID, user.getUserId());
        resultIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        PendingIntent contentIntent = PendingIntent.getActivity(context, 0, resultIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        builder.setContentIntent(contentIntent);

        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(id, builder.build());
    }

    private void showMessageNotification(Context context, int id, String title, String text, User user) {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context);
        builder.setSmallIcon(R.mipmap.app_icon);
        builder.setLargeIcon(BitmapFactory.decodeResource(context.getResources(), R.mipmap.app_icon));
        builder.setContentTitle(title);
        builder.setContentText(text);
        builder.setAutoCancel(true);
        builder.setOnlyAlertOnce(true);

        // User define
        Intent resultIntent = new Intent(context, ChatActivity.class);
        resultIntent.putExtra(Utils.NAME, user.getUserName());
        resultIntent.putExtra(Utils.USERID, user.getUserId());
        resultIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        PendingIntent contentIntent = PendingIntent.getActivity(context, 0, resultIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        builder.setContentIntent(contentIntent);

        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(id, builder.build());
    }

    private void checkNotification() {
        if (!NotificationManagerCompat.from(this).areNotificationsEnabled()) {
            MaterialDialog.Builder builder = new MaterialDialog.Builder(this)
                    .title(R.string.open_notification_dialog)
                    .positiveText(R.string.btn_sure)
                    .negativeText(R.string.btn_cancel);
            builder.onAny((dialog, which) -> {
                if (which == DialogAction.NEUTRAL) {
                } else if (which == DialogAction.POSITIVE) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP && Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
                        Intent intent = new Intent();
                        intent.setAction("android.settings.APP_NOTIFICATION_SETTINGS");
                        intent.putExtra("app_package", MainActivity.this.getPackageName());
                        intent.putExtra("app_uid", MainActivity.this.getApplicationInfo().uid);
                        startActivity(intent);
                    } else if (Build.VERSION.SDK_INT == Build.VERSION_CODES.KITKAT) {
                        Intent intent = new Intent();
                        intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                        intent.addCategory(Intent.CATEGORY_DEFAULT);
                        intent.setData(Uri.parse("package:" + MainActivity.this.getPackageName()));
                        startActivity(intent);
                    } else {
                        Intent localIntent = new Intent();
                        localIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        localIntent.setAction("android.settings.APPLICATION_DETAILS_SETTINGS");
                        localIntent.setData(Uri.fromParts("package", MainActivity.this.getPackageName(), null));
                        startActivity(localIntent);
                    }
                } else if (which == DialogAction.NEGATIVE) {
                }
            }).show();
        }
    }
}
