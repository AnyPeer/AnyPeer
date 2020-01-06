package any.xxx.anypeer.moudle.main;

import android.Manifest;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.media.AudioManager;
import android.media.MediaMetadataRetriever;
import android.media.SoundPool;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Vibrator;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
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
import com.allenliu.versionchecklib.v2.AllenVersionChecker;
import com.allenliu.versionchecklib.v2.builder.DownloadBuilder;
import com.allenliu.versionchecklib.v2.builder.UIData;
import com.google.gson.Gson;
import com.kaopiz.kprogresshud.KProgressHUD;

import org.elastos.carrier.ConnectionStatus;
import org.elastos.carrier.FriendInfo;
import org.elastos.carrier.UserInfo;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import any.xxx.anypeer.BuildConfig;
import any.xxx.anypeer.Config;
import any.xxx.anypeer.R;
import any.xxx.anypeer.app.BaseActivity;
import any.xxx.anypeer.app.ForegroundCallbacks;
import any.xxx.anypeer.bean.UpdateVersionBean;
import any.xxx.anypeer.bean.User;
import any.xxx.anypeer.chatbean.ChatConversation;
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
import any.xxx.anypeer.moudle.init.InitActivity;
import any.xxx.anypeer.util.AnyWallet;
import any.xxx.anypeer.util.EventBus;
import any.xxx.anypeer.util.FileUtils;
import any.xxx.anypeer.util.NetUtils;
import any.xxx.anypeer.util.NotificationUtils;
import any.xxx.anypeer.util.PrefereneceUtil;
import any.xxx.anypeer.util.Utils;
import any.xxx.anypeer.zxing.CaptureActivity;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

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
    private RankFragment rankFragment;
    private View mLayout;
    private TextView mTxtTitle;
    private MessageService.NetHandler mHandler = null;
    private NetUtils sNetUtils = null;
    private MessageService mMessageService;
    private ServiceConnection mServiceConnection;
    private ChatManager mChatManager;
    private KProgressHUD mKProgressHUD;
    private static boolean mIsReady = false;
    private static boolean mIsJoined = false;
    private AnyWallet mAnyWallet;
    private String mName;
    private boolean isTipUpdate, isFocre;
    private View ll_loading;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        init();
        findViewById();
    }

    @Override
    protected void onResume() {
        super.onResume();

        try {
            //For Re-generate the wallet.
            if (mIsReady && InitActivity.LOGIN_CHANAGED) {
                updateSelf();

                mKProgressHUD.dismiss();
                if (mIsJoined) {
                    ll_loading.setVisibility(View.GONE);
                }
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }

        checkUpdate();
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
                .setLabel(getString(R.string.wait_carrier_ready))
                .show();

	sNetUtils = NetUtils.getInstance(MainActivity.this, mHandler);

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

        //init sound
        initSoundPool();

        ForegroundCallbacks.get(getApplication()).addListener(new ForegroundCallbacks.Listener() {
            @Override
            public void onBecameForeground() {
                checkUpdate();
            }

            @Override
            public void onBecameBackground() {

            }
        });
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
        int what = msg.what;
        switch (what) {
            case NetUtils.ANYSTATE.READY: {
                mKProgressHUD.dismiss();
                mIsReady = true;
                //the local network is ready.
                //For update the name and ela address.
                updateSelf();

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

                break;
            }
            case NetUtils.ANYSTATE.MSG_FORBIDDEN: {
                sNetUtils.forbidDefault();
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
                                int unread = ChatDBManager.getInstance().getChatConversationByUserId(userId).getUnreadMsgCount() + 1;
                                String unreadString = getString(R.string.unread_number, String.valueOf(unread));
                                showMessageNotification(this, id, user.getUserName() + " [" + unreadString + "]", message, user);
                            }
                        }
                    }

                    if (ChatActivity.isActiveChat(userId)) {
                        return;
                    }

                    //a new message is coming.
                    vibrateSound();

                    if (mFriendFragment.isRobot(userId)) {
                        //TODO
//                        showTestInfo("Robot say: " + message);
                        mFriendFragment.applyResult(message);
                    } else {
                        ChatMessage chatMessage = new ChatMessage();
                        chatMessage.setMsgId(UUID.randomUUID().toString());
                        chatMessage.setDirect(ChatMessage.Direct.RECEIVE.ordinal());
                        chatMessage.setUnread(true);
                        chatMessage.setType(msg.arg1);
                        chatMessage.setMessage(message);

                        ChatDBManager.getInstance().addMessage(userId, chatMessage);
                        mMessageFragment.onReflash();
                    }
                }

                break;
            }
            case NetUtils.ANYSTATE.GROUP_MESSAGE: {
                try {
                    Bundle data = msg.getData();
                    String userId = data.getString(NetUtils.ANYSTATE.GROUP_TITLE);
                    String message = data.getString(NetUtils.ANYSTATE.DATA);
                    String from = data.getString(NetUtils.ANYSTATE.FROM);

                    if (ForegroundCallbacks.get().isBackground()) {
                        if (ChatManager.getInstance(this).getIsMessageShowNotification(this)) {
                            if (NotificationManagerCompat.from(this).areNotificationsEnabled()) {
                                User user = new User(userId, userId);
                                // TODO: get 3 char
                                int id = Integer.valueOf(Utils.stringToAscii(userId.substring(0, 3)));
                                int unread = ChatDBManager.getInstance().getChatConversationByUserId(userId).getUnreadMsgCount() + 1;
                                String unreadString = getString(R.string.unread_number, String.valueOf(unread));
                                showMessageNotification(this, id, user.getUserName() + " [" + unreadString + "]", message, user);
                            }
                        }
                    }

                    if (ChatActivity.isActiveChat(userId)) {
                        return;
                    }

                    vibrateSound();

                    ChatMessage chatMessage = new ChatMessage();
                    String msgId = UUID.randomUUID().toString();
                    chatMessage.setMsgId(msgId);
                    chatMessage.setDirect(ChatMessage.Direct.RECEIVE.ordinal());
                    chatMessage.setUnread(true);
                    chatMessage.setType(ChatMessage.Type.TXT.ordinal());
                    chatMessage.setMessage(message);
                    chatMessage.setFromId(from);

                    ChatDBManager.getInstance().addMessage(userId, chatMessage);

                    //Store the user's name.
                    String fromName = NetUtils.getInstance().peerName(userId, from);
                    ChatDBManager.getInstance().setGroupMessageName(msgId, fromName);

                    mMessageFragment.onReflash();
                } catch (Exception e) {
                    e.printStackTrace();
                }

                break;
            }
            case NetUtils.ANYSTATE.FILE_TRANSFER: {
                //TODO : A File data is coming.
                Bundle data = msg.getData();
                if (data != null) {
                    String userId = data.getString(NetUtils.ANYSTATE.FROM);
                    byte[] fileData = data.getByteArray(NetUtils.ANYSTATE.FILEDATA);
                    ChatMessage.Type msgType = ChatMessage.Type.getMsgType(msg.arg1);

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
                                int unread = ChatDBManager.getInstance().getChatConversationByUserId(userId).getUnreadMsgCount() + 1;
                                String unreadString = getString(R.string.unread_number, String.valueOf(unread));
                                showMessageNotification(this, id, user.getUserName() + " [" + unreadString + "]", messageString, user);
                            }
                        }
                    }

                    if (ChatActivity.isActiveChat(userId)) {
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
                        mmr.setDataSource(filePath);
                        Bitmap bitmap = mmr.getFrameAtTime();

                        String imagePath = getFilesDir() + "/";

                        String imageFilePath = imagePath + "_" + System.currentTimeMillis() + ".png";
                        FileUtils.saveBitmap(imageFilePath, bitmap);

                        message.setLocalThumb(imageFilePath);
                        message.setFilePath(filePath);
                    }
                    ChatDBManager.getInstance().addMessage(userId, message);

                    mMessageFragment.onReflash();
                }
            }
            case NetUtils.ANYSTATE.CONNECTION: {
                //TODO the local network is connected.
                break;
            }
            case NetUtils.ANYSTATE.GROUP_PEER_NAME: {
                String groupTitle = (String) msg.obj;
                updateGroupTitle(groupTitle);

                break;
            }
            case NetUtils.ANYSTATE.GROUP_PEER_CHANGED: {
                String groupTitle = (String) msg.obj;
                updateGroupTitle(groupTitle);
                break;
            }
            case NetUtils.ANYSTATE.GROUP_LEAVE: {
                String groupTitle = (String) msg.obj;
                try {
                    sNetUtils.inviteDefault(groupTitle);
                }
                catch (Exception e) {
                    e.printStackTrace();
                }
                break;
            }
            case NetUtils.ANYSTATE.GROUP_DEFAULT_ONLINE: {
                try {
                    String groupTitle = (String) msg.obj;
                    updateGroupTitle(groupTitle);
                } catch (Exception e) {
                    e.printStackTrace();
                }

                break;
            }
            case NetUtils.ANYSTATE.FRIENDCONNECTION: {
                //The friend is online.
                String userId = (String) msg.obj;
                if (userId != null && mFriendFragment.isRobot(userId)) {
                    boolean connected = msg.arg1 == ConnectionStatus.Connected.value();
                    mFriendFragment.updateRobot(connected);

                    if (connected) {
                        //Notice the bot to invite me.
//	                    if (Utils.isCN(this)) {
		                    sNetUtils.inviteDefault(NetUtils.DEFAULT_GROUP_TYPE.CN);
//	                    }
//	                    else {
		                    sNetUtils.inviteDefault(NetUtils.DEFAULT_GROUP_TYPE.EN);
//	                    }
                    }
                } else {
                    mFriendFragment.initContactList();
                    mMessageFragment.onReflash();
                }

                break;
            }
            case NetUtils.ANYSTATE.SELFINFOCHANGED: {
                break;
            }
            case NetUtils.ANYSTATE.FRIENDINFOCHANGED: {
                FriendInfo info = (FriendInfo) msg.obj;
                Bundle data = msg.getData();
                if (info != null) {
                    String userId = data.getString(NetUtils.ANYSTATE.USERID);
                    mChatManager.updateFriend(info.getName(), userId, info.getDescription()
                            , info.getGender(), info.getEmail());
                    FriendManager.getInstance().updateUserInfo(userId, info.getName(), info.getGender(),
                            info.getRegion(), info.getEmail(), info.getDescription());
                    mMessageFragment.onReflash();

                    //Update the contact list.
                    mFriendFragment.initContactList();
                }

                break;
            }
            case NetUtils.ANYSTATE.FRIENDREMOVED: {
                //TODO a friend is removed.
                String id = (String) msg.obj;
                if (id != null) {
                    //Update the contact list.
                    mFriendFragment.removeUser(id);
                    int size = mReAddFriendAddress.size();

                    for (int i = 0; i < size; i++) {
                        String address = mReAddFriendAddress.get(i);
                        String userId = NetUtils.getUserIdByAddress(address);
                        if (id.equals(userId)) {
                            sNetUtils.addFriend(address, mName);
                            mReAddFriendAddress.remove(i);
                            break;
                        }
                    }
                }
                break;
            }
            case NetUtils.ANYSTATE.FRIENDINVITEREQUEST: {
                break;
            }
            case NetUtils.ANYSTATE.FILE_TRANSFER_FEEDBACK: {
                Bundle data = msg.getData();
                if (data != null) {
                    String msgId = data.getString(NetUtils.ANYSTATE.MSG);
                    ChatDBManager.getInstance().setMessageStatus(msgId, ChatMessage.Status.SUCCESS);
                }
            }
        }
    }

	private void vibrateSound() {
		Vibrator vib = (Vibrator) getSystemService(Service.VIBRATOR_SERVICE);
		if (vib != null) {
			vib.vibrate(100);
		}

		//sound
		playSounds(VOICE_ID_MESSAGE);
	}

    private void updateGroupTitle(String groupTitle) {
        if (groupTitle != null && !groupTitle.isEmpty()) {
            ll_loading.setVisibility(View.GONE);
            mIsJoined = true;

            ChatConversation chatConversation = ChatDBManager.getInstance().getChatConversationByUserId(groupTitle);
            if (chatConversation == null) {
                chatConversation = new ChatConversation();
                chatConversation.setGroup(true);
                chatConversation.setGroupName(groupTitle);
                chatConversation.setmUserId(groupTitle);
                ChatDBManager.getInstance().addConversation(chatConversation);
            }
            mMessageFragment.onReflash();
        }
    }

    private void showTestInfo(String info) {
        if (info != null && !info.isEmpty()) {
//            Toast.makeText(this, info, Toast.LENGTH_LONG).show();
        }
    }

    private void updateSelf() {
        try {
            User user = new Gson().fromJson(PrefereneceUtil.getString(this, User.USER), User.class);
            UserInfo self = sNetUtils.getSelfInfo();
            self.setName(user.getUserName());
            self.setEmail(user.getEmail());
            self.setGender(user.getGender());
            self.setRegion(Long.toString(System.currentTimeMillis()));

            if (mAnyWallet != null) {
                if (!mAnyWallet.isValidWallet()) {
                    Utils.showShortToast(this, getString(R.string.wallet_has_exception));
                }
                else {
                    self.setDescription(mAnyWallet.getWalletAddress());
                }
            } else {
                self.setDescription("NONE");
            }

            //Check robot
            mFriendFragment.initRobotUserId(user.getUserName());

            sNetUtils.setSelfInfo(self);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.scan: {
                showCamera();
                break;
            }
            case R.id.paymentandgathering: {
                try {
                    String receipAddress = mAnyWallet.getWalletAddress();
                    Intent intent = new Intent(this, BarCodeActivity.class);
                    intent.putExtra(Utils.QRCODETYPE, Utils.QrCodeType.Wallet_Address.ordinal());
                    intent.putExtra(Utils.ADDRESS, receipAddress);
                    startActivity(intent);
                } catch (Exception e) {
                    Utils.showShortToast(MainActivity.this, "Start the receipt-address Activity failed.");
                }

                break;
            }
            case R.id.friend: {
                Intent intent = new Intent(this, AddFriendActivity.class);
                startActivityForResult(intent, REQ_QR_CODE);
                break;
            }
            case R.id.myChatAddress: {
                Intent intent = new Intent(this, BarCodeActivity.class);
                intent.putExtra(Utils.QRCODETYPE, Utils.QrCodeType.Carrier_Address.ordinal());
                intent.putExtra(Utils.ADDRESS, Utils.getQrcodeString(this));
                startActivity(intent);
                break;
            }
	        case R.id.transferto: {
		        Intent intent = new Intent(MainActivity.this, TransferAccountsActivity.class);
		        intent.putExtra(TransferAccountsActivity.NAME, "");
		        intent.putExtra(TransferAccountsActivity.GENDER, "");
		        intent.putExtra(TransferAccountsActivity.ADDRESS, "");
		        startActivity(intent);
		        break;
	        }

            default:
                break;
        }
        return false;
    }

    @Override
    public void callback(boolean isGoMain) {
        if (isGoMain) {
            new Handler().postDelayed(() -> onTabClicked(R.id.re_anypeer), 300);
        }

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
            if (m_soundPool != null) {
	        m_soundPool.release();
            }
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
        ll_loading = findViewById(R.id.ll_loading);
        ImageView iv_loading = findViewById(R.id.iv_loading);

        ObjectAnimator objectAnimator = ObjectAnimator.ofFloat(iv_loading, "rotation", 0f, 360f);
        objectAnimator.setRepeatCount(ValueAnimator.INFINITE);
        objectAnimator.setRepeatMode(ValueAnimator.RESTART);
        objectAnimator.start();

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
        mImagebuttons.add(findViewById(R.id.ib_rank));
        mImagebuttons.add(findViewById(R.id.ib_profile));

        mImagebuttons.get(0).setSelected(true);
        mTextviews = new ArrayList<>();
        mTextviews.add(findViewById(R.id.tv_anypeer));
        mTextviews.add(findViewById(R.id.tv_contact_list));
        mTextviews.add(findViewById(R.id.tv_rank));
        mTextviews.add(findViewById(R.id.tv_profile));
        mTextviews.get(0).setTextColor(ContextCompat.getColor(this, R.color.colorPrimary));

        mMessageFragment = new MessageFragment();
        mFriendFragment = new FriendFragment();
        rankFragment = new RankFragment();
        MeFragment meFragment = new MeFragment();

        mFragments = new ArrayList<>();
        mFragments.add(mMessageFragment);
        mFragments.add(mFriendFragment);
        mFragments.add(rankFragment);
        mFragments.add(meFragment);

        getSupportFragmentManager().beginTransaction()
                .add(R.id.fragment_container, mMessageFragment)
                .add(R.id.fragment_container, mFriendFragment)
                .add(R.id.fragment_container, rankFragment)
                .add(R.id.fragment_container, meFragment)
                .hide(mFriendFragment).hide(meFragment).hide(rankFragment)
                .show(mMessageFragment).commit();

        findViewById(R.id.re_anypeer).setOnClickListener(view -> onTabClicked(view.getId()));

        findViewById(R.id.re_contact_list).setOnClickListener(view -> onTabClicked(view.getId()));

        findViewById(R.id.re_rank_list).setOnClickListener(view -> onTabClicked(view.getId()));

        findViewById(R.id.re_profile).setOnClickListener(view -> onTabClicked(view.getId()));
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
                case R.id.re_rank_list:
                    mIndex = 2;
                    mTxtTitle.setText(getString(R.string.node_title));
                    break;
                case R.id.re_profile:
                    mIndex = 3;
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
                    String complex = args[0];
                    if (args[0].contains("elastos:")) {
                        int pos = args[0].indexOf(":");
                        complex = args[0].substring(pos + 1);
                    }

                    final String address = complex;
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

                    final String userId = NetUtils.getUserIdByAddress(address);
                    final boolean isFriend = sNetUtils.isFriend(userId);
                    if (NetUtils.isValidAddress(address)) {
                        MaterialDialog.Builder builder = new MaterialDialog.Builder(MainActivity.this);
                        builder.onNegative((dialog, which) -> dialog.dismiss());

                        if (isFriend) {
                            builder.title(R.string.friend_readd_text1);
                            builder.content(getString(R.string.friend_readd_ask_1) + name + getString(R.string.friend_add_ask_2));
                        } else {
                            builder.title(R.string.friend_add_text1);
                            builder.content(getString(R.string.friend_add_ask_1) + name + getString(R.string.friend_add_ask_2));
                        }

                        builder.negativeText(R.string.friend_add_no);
                        builder.positiveText(R.string.friend_add_yes);

                        builder.onPositive((dialog, which) -> {
                            User user = new Gson().fromJson(PrefereneceUtil.getString(MainActivity.this
                                    , User.USER), User.class);

                            //if we are friend, bug i don't have the chat history, add him(her) again.
                            if (isFriend) {
                                mReAddFriendAddress.add(address);
                                sNetUtils.removeFriend(userId);
                            } else {
                                //TODO: maybe make the user input some thing.
                                sNetUtils.addFriend(address, user.getUserName());
                            }

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
        // User define
        Intent resultIntent = new Intent(context, FriendDetailActivity.class);
        resultIntent.putExtra(FriendDetailActivity.TYPE, FriendDetailActivity.TYPE_ADD_FRIEND);
        resultIntent.putExtra(Utils.USERID, user.getUserId());
        resultIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        PendingIntent contentIntent = PendingIntent.getActivity(context, 0, resultIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        NotificationUtils notificationUtils = new NotificationUtils(this);
        notificationUtils
                .setOngoing(true)
                .setContentIntent(contentIntent)
                .sendNotification(id, title, text, R.mipmap.app_icon);
    }

    private void showMessageNotification(Context context, int id, String title, String text, User user) {
        // User define
        Intent resultIntent = new Intent(context, ChatActivity.class);
        resultIntent.putExtra(Utils.NAME, user.getUserName());
        resultIntent.putExtra(Utils.USERID, user.getUserId());
        resultIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        PendingIntent contentIntent = PendingIntent.getActivity(context, 0, resultIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationUtils notificationUtils = new NotificationUtils(this);
        notificationUtils
                .setOngoing(true)
                .setContentIntent(contentIntent)
                .sendNotification(id, title, text, R.mipmap.app_icon);
    }

    private void checkNotification() {
        if (!NotificationManagerCompat.from(this).areNotificationsEnabled()) {
            MaterialDialog.Builder builder = new MaterialDialog.Builder(this)
                    .title(R.string.open_notification_dialog)
                    .positiveText(R.string.btn_sure)
                    .negativeText(R.string.btn_cancel);
            builder.onAny((dialog, which) -> {
                if (which == DialogAction.POSITIVE) {
                    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
                        Intent intent = new Intent();
                        intent.setAction("android.settings.APP_NOTIFICATION_SETTINGS");
                        intent.putExtra("app_package", MainActivity.this.getPackageName());
                        intent.putExtra("app_uid", MainActivity.this.getApplicationInfo().uid);
                        startActivity(intent);
                    }
                    else {
                        Intent localIntent = new Intent();
                        localIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        localIntent.setAction("android.settings.APPLICATION_DETAILS_SETTINGS");
                        localIntent.setData(Uri.fromParts("package", MainActivity.this.getPackageName(), null));
                        startActivity(localIntent);
                    }
                }
            }).show();
        }
    }

    private void checkUpdate() {
        OkHttpClient okHttpClient = new OkHttpClient();
        Request request = new Request.Builder()
                // TODO : test config
                .url("https://anyxxx.github.io/AnyPeer/")
                .get()
                .build();

        okHttpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                try {
                    if (response.isSuccessful()) {
                        String html = response.body().string();
                        String[] strings = html.split("Update");
                        String versionJson = null;
                        for (int i = 0; i < strings.length; i++) {
                            if (i % 2 != 0) {
                                try {
                                    versionJson = strings[i].replaceAll("\\<.*?>", "").trim();
                                    break;
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        }

                        if (!TextUtils.isEmpty(versionJson)) {
                            UpdateVersionBean updateVersionBean = new Gson().fromJson(versionJson, UpdateVersionBean.class);
                            if (updateVersionBean == null) {
                                return;
                            }

                            if (updateVersionBean.getVersion() > BuildConfig.VERSION_CODE) {
                                if (isTipUpdate && !updateVersionBean.isIs_force() || isFocre) {
                                    return;
                                }

                                isTipUpdate = true;
                                Log.d(TAG, getString(R.string.channal));
                                if (getString(R.string.channal).equals(Config.CHANNAL_CN)) {
                                    UIData uiData = UIData.create();
                                    uiData.setTitle(getString(R.string.update_title));
                                    uiData.setDownloadUrl(updateVersionBean.getUrl());
                                    uiData.setContent(updateVersionBean.getDescription());

                                    DownloadBuilder builder = AllenVersionChecker
                                            .getInstance()
                                            .downloadOnly(uiData);

                                    builder.setShowNotification(false);

                                    if (updateVersionBean.isIs_force()) {
                                        isFocre = true;
                                        builder.setForceRedownload(true);
                                        builder.setForceUpdateListener(() -> {
                                            finish();
                                        });
                                    }

                                    builder.executeMission(MainActivity.this);
                                } else if (getString(R.string.channal).equals(Config.CHANNAL_GP)) {
                                    MaterialDialog.Builder builder = new MaterialDialog.Builder(MainActivity.this)
                                            .title(R.string.update_title)
                                            .content(updateVersionBean.getDescription())
                                            .positiveText(R.string.btn_sure)
                                            .onPositive((dialog, which) -> {
                                                if (!Utils.checkAppInstalled(MainActivity.this, "com.android.vending")) {
                                                    Toast.makeText(MainActivity.this, getString(R.string.not_install_gp), Toast.LENGTH_LONG).show();
                                                    if (updateVersionBean.isIs_force()) {
                                                        finish();
                                                    } else {
                                                        return;
                                                    }
                                                }

                                                try {
                                                    Uri uri = Uri.parse("market://details?id=" + BuildConfig.APPLICATION_ID);
                                                    Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                                                    intent.setPackage("com.android.vending");
                                                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                                    startActivity(intent);
                                                } catch (Exception e) {
                                                    e.printStackTrace();
                                                }

                                                if (updateVersionBean.isIs_force()) {
                                                    isFocre = true;
                                                    new Handler().postDelayed(() -> {
                                                        finish();
                                                    }, 1000);
                                                }
                                            });

                                    if (updateVersionBean.isIs_force()) {
                                        builder.canceledOnTouchOutside(false);
                                    } else {
                                        builder.negativeText(R.string.btn_cancel);
                                    }

                                    runOnUiThread(() -> builder.show());

                                }
                            }
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

	private int VOICE_ID_MESSAGE = 0;
	private SoundPool m_soundPool = null;
	private void initSoundPool() {
		try {
			m_soundPool = new SoundPool.Builder().setMaxStreams(2).build();
			VOICE_ID_MESSAGE = m_soundPool.load(this, R.raw.message, 1);
                        setVolumeControlStream(AudioManager.STREAM_MUSIC);
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}

	@SuppressWarnings("static-access")
	private void playSounds(int soundID){
    	try {
    	    if (ChatManager.getInstance(this).getIsMessageVoice(this)) {
                AudioManager am = (AudioManager)this.getSystemService(this.AUDIO_SERVICE);
                if (am != null) {
                    float audioMaxVolume = am.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
                    float audioCurrentVolume = am.getStreamVolume(AudioManager.STREAM_MUSIC);
                    float volumeRatio = audioCurrentVolume / audioMaxVolume;
                    m_soundPool.play(soundID, volumeRatio, volumeRatio, 0, 0, 1);
                }
            }
	    }
	    catch (Exception e) {
    		e.printStackTrace();
	    }
	}
}
