package any.xxx.anypeer.moudle.chat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.AnimationDrawable;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.PowerManager;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.ClipboardManager;
import android.text.Editable;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.google.gson.Gson;
import com.mabeijianxi.smallvideorecord2.DeviceUtils;
import com.mabeijianxi.smallvideorecord2.JianXiCamera;
import com.mabeijianxi.smallvideorecord2.MediaRecorderActivity;
import com.mabeijianxi.smallvideorecord2.model.MediaRecorderConfig;

import org.elastos.carrier.FriendInfo;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import any.xxx.anypeer.R;
import any.xxx.anypeer.bean.User;
import any.xxx.anypeer.chatbean.ChatConversation;
import any.xxx.anypeer.chatbean.ChatMessage;
import any.xxx.anypeer.db.ChatDBManager;
import any.xxx.anypeer.db.FriendManager;
import any.xxx.anypeer.manager.ChatManager;
import any.xxx.anypeer.manager.SendVideoManager;
import any.xxx.anypeer.moudle.chat.MessageService.IMessageCallback;
import any.xxx.anypeer.moudle.friend.FriendDetailActivity;
import any.xxx.anypeer.moudle.main.ContactAdapter;
import any.xxx.anypeer.util.EventBus;
import any.xxx.anypeer.util.FileUtils;
import any.xxx.anypeer.util.NetUtils;
import any.xxx.anypeer.util.PrefereneceUtil;
import any.xxx.anypeer.util.Utils;
import any.xxx.anypeer.util.VoiceRecorder;
import any.xxx.anypeer.widget.emoji.DefEmoticons;
import any.xxx.anypeer.widget.emoji.EmojiBean;
import sj.keyboard.XhsEmoticonsKeyBoard;
import sj.keyboard.adpater.EmoticonsAdapter;
import sj.keyboard.adpater.PageSetAdapter;
import sj.keyboard.data.EmoticonPageEntity;
import sj.keyboard.data.EmoticonPageSetEntity;
import sj.keyboard.interfaces.EmoticonClickListener;
import sj.keyboard.interfaces.EmoticonDisplayListener;
import sj.keyboard.interfaces.PageViewInstantiateListener;
import sj.keyboard.utils.imageloader.ImageBase;
import sj.keyboard.widget.EmoticonPageView;
import sj.keyboard.widget.FuncLayout;
import top.zibin.luban.Luban;
import top.zibin.luban.OnCompressListener;

import static any.xxx.anypeer.chatbean.ChatMessage.Type.TXT;

public class ChatActivity extends AppCompatActivity implements IMessageCallback, MessageAdapter.HeaderOnClick, SendVideoManager.Callback {
    public static final int RESULT_CODE_COPY = 1;
    public static final int RESULT_CODE_DELETE = 2;
    public static final int REQUEST_CODE_CONTEXT_MENU = 3;
    public static final int REQUEST_CODE_VIDEO = 4;
    public static final int REQUEST_CODE_CAMERA = 5;
    public static final int REQUEST_CODE_LOCAL = 6;
    public static final int REQUEST_CAMERA = 7;
    public static final int REQUEST_RECORD_AUDIO = 8;
    public static final int REQUEST_CODE_TRANSFER = 9;
    public static final int RESULT_CODE_RETRY = 10;
	public static final int RESULT_CODE_ADDFRIEND = 11;
	public static final int REQUEST_CODE_GROUP_DETAIL = 12;
	public static final int RESULT_CODE_DELETE_GROUP_MESSAGE = 13;

    private XhsEmoticonsKeyBoard mEKeyBoard;
    private TextView mTitle;
    private ListView mListView;
    private View recordingContainer;
    private TextView recordingHint;
    private ImageView micImage;
    private ImageView imgRight;

    private MessageAdapter mAdapter;
    private InputMethodManager mInputMethodManager;
    private ClipboardManager mClipboard;

    private String mName;
    private String mUserId;
    private ChatConversation mEMConversation;
    private ChatManager mChatManager;
    private NetUtils mNetUtils;
    private MessageService mMessageService;
    private ServiceConnection mServiceConnection;
    private File cameraFile, videoFile;
    private AnimationDrawable animationDrawable;
    private PowerManager.WakeLock wakeLock;
    public String playMsgId;
    private VoiceRecorder voiceRecorder;

    private static final String TAG = "ChatActivity";

    private static boolean IS_CHAT_START = false;
    private static String ACTIVE_USER_ID = "";

    private Handler micImageHandler = new Handler() {
        @Override
        public void handleMessage(android.os.Message msg) {
        }
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(any.xxx.anypeer.R.layout.activity_chat);


        mInputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        mClipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);

        SendVideoManager.getInstance().addCallback(this);

        IS_CHAT_START = true;

        initView();
        setUpView();
        initSmallVideo();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        Log.d(TAG, "onNewIntent");
        initView();
        setUpView();
    }

    private static boolean sShowAutoMessage = true;
    @Override
    public void processMessage(Message msg) {
        switch (msg.what) {
            case NetUtils.ANYSTATE.FRIENDMESSAGE: {
                //A message is coming.
                Bundle data = msg.getData();
                if (data != null) {
                    String userId = data.getString(NetUtils.ANYSTATE.FROM);
                    if (userId != null && userId.equals(mUserId)) {
                        String message = data.getString(NetUtils.ANYSTATE.MSG);
                        int msgType = msg.arg1;
                        onReceive(message, ChatMessage.Type.getMsgType(msgType));
                    }
                }

                break;
            }
            case NetUtils.ANYSTATE.FILE_TRANSFER: {
                //TODO : A File data is coming.
                Bundle data = msg.getData();
                if (data != null) {
                    String userId = data.getString(NetUtils.ANYSTATE.FROM);
                    byte[] fileData = data.getByteArray(NetUtils.ANYSTATE.FILEDATA);
                    int msgType = msg.arg1;
                    if (userId != null && userId.equals(mUserId)) {
                        onReceiveFile(fileData, ChatMessage.Type.getMsgType(msgType));
                    }
                }
            }
            case NetUtils.ANYSTATE.FILE_TRANSFER_STATE: {
                //TODO
//                String state = (String) msg.obj;
//                Utils.showLongToast(ChatActivity.this, state);

                break;
            }
            case NetUtils.ANYSTATE.FILE_TRANSFER_SEND_ERROR: {
                //If it has error when using session, Update the message item.
                Bundle data = msg.getData();
                if (data != null) {
                    String userId = data.getString(NetUtils.ANYSTATE.USERID);
                    String msgId = data.getString(NetUtils.ANYSTATE.MSGID);

                    if (userId != null && userId.equals(mUserId)) {
                        ChatDBManager.getInstance().setMessageStatus(msgId, ChatMessage.Status.FAIL);
                    }

                    mEMConversation = ChatDBManager.getInstance().getChatConversationByUserId(userId);

                    mAdapter.refresh();
                }
                break;
            }
            case NetUtils.ANYSTATE.FILE_TRANSFER_FEEDBACK: {
                Bundle data = msg.getData();
                if (data != null) {
                    String userId = data.getString(NetUtils.ANYSTATE.FROM);
                    String msgId = data.getString(NetUtils.ANYSTATE.MSG);

                    if (userId != null && userId.equals(mUserId)) {
                        ChatDBManager.getInstance().setMessageStatus(msgId, ChatMessage.Status.SUCCESS);
                    }

                    mEMConversation = ChatDBManager.getInstance().getChatConversationByUserId(userId);

                    mAdapter.refresh();
                }
                break;
            }
            case NetUtils.ANYSTATE.FRIENDCONNECTION: {
                //TODO the friend is online.
                break;
            }
            case NetUtils.ANYSTATE.GROUP_PEER_CHANGED: {
                setTitle(mName);
                break;
            }
            case NetUtils.ANYSTATE.GROUP_MESSAGE: {
                try {
                    int type = msg.arg1;
                    boolean isFromBot = msg.arg2 == 1;
                    Bundle data = msg.getData();
                    String title = data.getString(NetUtils.ANYSTATE.GROUP_TITLE);
                    String message = data.getString(NetUtils.ANYSTATE.DATA);
                    String from = data.getString(NetUtils.ANYSTATE.FROM);

                    if (isFromBot) {
                        //The message from bot.
                        if (sShowAutoMessage) {
                            //Show the auto-message from AnyBot
                            sShowAutoMessage = false;
                        }

	                    if (title != null && title.equals(mName)) {
		                    if (type == -1) {
			                    onReceiveGroupMessage(title, from, message, ChatMessage.Type.TXT);
		                    }
		                    else {
			                    onReceiveGroupMessage(title, from, message, ChatMessage.Type.getMsgType(type));
		                    }
	                    }
                    }
                    else {
                        //Support type
                        if (type == -1) {
                            Utils.showLongToast(this, getString(R.string.group_message_upsupport));
                        }

                        if (title != null && title.equals(mName)) {
                            if (type == -1) {
                                onReceiveGroupMessage(title, from, message, ChatMessage.Type.TXT);
                            }
                            else {
                                onReceiveGroupMessage(title, from, message, ChatMessage.Type.getMsgType(type));
                            }
                        }
                    }

                    Log.d(TAG, String.format("title==[%s], message=====[%s]", title, message));
                } catch (Exception e) {
                    e.printStackTrace();
                }

                break;
            }
            default: {
                break;
            }
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        bindService(new Intent(this, MessageService.class), mServiceConnection, BIND_AUTO_CREATE);
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        IS_CHAT_START = false;

	    try {
		    SendVideoManager.getInstance().removeCallback(this);

		    unbindService(mServiceConnection);

		    mMessageService.removeMessageCallback(ChatActivity.this);
		    mMessageService = null;
	    }
	    catch (Exception e) {
		    e.printStackTrace();
	    }
    }

    @Override
    public void onBackPressed() {
        EventBus.getInstance().notifyAllCallback(true);
        super.onBackPressed();
    }

    private void addFrinendDlg(String name, String address) {
    	try {
		    MaterialDialog.Builder builder = new MaterialDialog.Builder(ChatActivity.this);
		    builder.onNegative((dialog, which) -> dialog.dismiss());

		    builder.title(R.string.friend_add_text1);
		    builder.content(getString(R.string.friend_add_ask_1) + name + getString(R.string.friend_add_ask_2));

		    builder.negativeText(R.string.friend_add_no);
		    builder.positiveText(R.string.friend_add_yes);

		    builder.onPositive((dialog, which) -> {
			    User user = new Gson().fromJson(PrefereneceUtil.getString(ChatActivity.this
					    , User.USER), User.class);

			    //TODO: maybe make the user input some thing.
			    mNetUtils.addFriend(address, user.getUserName());

			    dialog.dismiss();
		    }).show();
	    }
	    catch (Exception e) {
    		e.printStackTrace();
	    }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (data == null) return;

        if (requestCode == REQUEST_CODE_CONTEXT_MENU) {
            switch (resultCode) {
                case RESULT_CODE_COPY: {
	                ChatMessage copyMsg = mAdapter.getItem(data.getIntExtra("position", -1));
	                mClipboard.setText(copyMsg.getMessage());
	                Utils.showLongToast(this, getString(R.string.content_copyed));
	                break;
                }
	            case RESULT_CODE_ADDFRIEND: {
	                try {
                        ChatMessage addFriendMsg = mAdapter.getItem(data.getIntExtra("position", -1));
                        String id = addFriendMsg.getFromId();
                        if (mNetUtils.isFriend(id)) {
                            Utils.showLongToast(this, getString(R.string.menu_friend_already));
                        }
                        else {
                            addFrinendDlg(addFriendMsg.getFromName(), addFriendMsg.getMessage());
                        }
                    }
                    catch (Exception e) {
	                    e.printStackTrace();
                    }
		            break;
	            }
                case RESULT_CODE_DELETE: {// 删除消息
	                ChatMessage deleteMsg = mAdapter.getItem(data.getIntExtra("position", -1));
	                ChatDBManager.getInstance().removeMessage(deleteMsg.getMsgId());
	                mEMConversation = ChatDBManager.getInstance().getChatConversationByUserId(mUserId);
	                mAdapter.refresh();
	                mListView.setSelection(data.getIntExtra("position", mAdapter.getCount()) - 1);
	                break;
                }
                case RESULT_CODE_RETRY: {
	                ChatMessage retryMsg = mAdapter.getItem(data.getIntExtra("position", -1));
	                ChatDBManager.getInstance().setMessageStatus(retryMsg.getMsgId(), ChatMessage.Status.INPROGRESS);
	                mEMConversation = ChatDBManager.getInstance().getChatConversationByUserId(mUserId);
	                mAdapter.notifyDataSetChanged();

	                switch (ChatMessage.Type.getMsgType(retryMsg.getType())) {
		                case TXT:
			                mNetUtils.sendMessage(mUserId, retryMsg.getMessage(), getAssembledIdForTextSending(retryMsg.getMsgId(), TXT));
			                break;

		                case MONEY:
			                mNetUtils.sendMessage(mUserId, retryMsg.getMessage(), retryMsg.getMsgId());
			                break;

		                case IMAGE:
		                case VOICE:
		                case VIDEO:
			                mNetUtils.sendFile(mUserId, retryMsg.getFilePath(), ChatMessage.Type.getMsgType(retryMsg.getType()), retryMsg.getMsgId());
			                break;
	                }
	                break;
                }
                default:
                    break;
            }

            return;
        }
        if (resultCode == RESULT_CODE_DELETE_GROUP_MESSAGE) {
            mEMConversation = ChatDBManager.getInstance().getChatConversationByUserId(mUserId);
            mAdapter.refresh();
        }

        if (requestCode == REQUEST_CODE_CAMERA) {
            if (cameraFile != null && cameraFile.exists()) {
                Log.d(TAG, cameraFile.getAbsolutePath());
                sendPic(cameraFile.getAbsolutePath());
            }
        } else if (requestCode == REQUEST_CODE_LOCAL) {
            if (data != null) {
                Uri selectedImage = data.getData();
                if (selectedImage != null) {
                    String type = FileUtils.getFileType(Utils.getPhotoPathFromContentUri(this, selectedImage));
                    Log.d(TAG, Utils.getPhotoPathFromContentUri(this, selectedImage) + " " + type);
                    if (type != null) {
                        if (type.equals("jpg") || type.equals("png") || type.equals("gif") || type.equals("tif")) {
                            sendPic(Utils.getPhotoPathFromContentUri(this, selectedImage));
                        } else if (type.equals("mp4")) {
                            // TODO: send video
                            sendVideo(Utils.getPhotoPathFromContentUri(this, selectedImage));
                        }
                    }
                }
            }
        } else if (requestCode == REQUEST_CODE_TRANSFER && resultCode == RESULT_OK) {
            sendText(data.getStringExtra(TransferAccountsActivity.MONEY), ChatMessage.Type.MONEY);
        } else if (requestCode == REQUEST_CODE_VIDEO) {
            if (videoFile.exists()) {
                sendVideo(videoFile.getAbsolutePath());
            }
        }

    }

    private void setTitle(String title) {
        if (mEMConversation.isGroup()) {
            String extra = title + "(" + NetUtils.getInstance().peersCount(title) + ")";
            mTitle.setText(extra);
            return;
        }
        mTitle.setText(title);
    }

    @SuppressLint("InvalidWakeLockTag")
    protected void initView() {
        mServiceConnection = new ChatActivity.MessageServiceConn();
        wakeLock = ((PowerManager) getSystemService(Context.POWER_SERVICE)).newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK, "demo");

        mNetUtils = NetUtils.getInstance();
        mChatManager = ChatManager.getInstance(this);
        mName = getIntent().getStringExtra(Utils.NAME);
        mUserId = getIntent().getStringExtra(Utils.USERID);
        ACTIVE_USER_ID = mUserId;

        mEMConversation = ChatDBManager.getInstance().getChatConversationByUserId(mUserId);
        if (mEMConversation == null) {
            mEMConversation = new ChatConversation();
            mEMConversation.setmUserId(mUserId);
            ChatDBManager.getInstance().addConversation(mEMConversation);
            mEMConversation = ChatDBManager.getInstance().getChatConversationByUserId(mUserId);
        }

        mTitle = findViewById(any.xxx.anypeer.R.id.txt_title);
        ImageView mBackground = findViewById(any.xxx.anypeer.R.id.img_back);
        recordingContainer = findViewById(R.id.view_talk);
        recordingHint = findViewById(R.id.recording_hint);
        micImage = findViewById(R.id.mic_image);
        animationDrawable = (AnimationDrawable) micImage.getBackground();
        animationDrawable.setOneShot(false);
        imgRight = findViewById(R.id.img_right);

        if (mEMConversation.isGroup()) {
            IS_CHAT_START = true;
            ACTIVE_USER_ID = mEMConversation.getGroupName();
            imgRight.setImageResource(R.drawable.icon_more);
            imgRight.setVisibility(View.VISIBLE);
            imgRight.setOnClickListener(v -> {
                Intent intent = new Intent(this, GroupDetailActivity.class);
                intent.putExtra(GroupDetailActivity.GROUP_ID, mUserId);
                startActivityForResult(intent, REQUEST_CODE_GROUP_DETAIL);
            });
        }

        setTitle(mName);

        mBackground.setVisibility(View.VISIBLE);
        mBackground.setOnClickListener(view -> {
            EventBus.getInstance().notifyAllCallback(true);
            finish();
        });

        mListView = findViewById(any.xxx.anypeer.R.id.list);

        mEKeyBoard = findViewById(any.xxx.anypeer.R.id.ek_bar);
        mEKeyBoard.getBtnSend().setOnClickListener(view -> {
            if (mEMConversation.isGroup()) {
                if (!NetUtils.getInstance().isForbidden(mName)) {
                    sendText(mEKeyBoard.getEtChat().getText().toString().trim(), TXT);
                }
                else {
                    Toast.makeText(ChatActivity.this, R.string.group_forbidden_message, Toast.LENGTH_SHORT).show();
                }
            }
            else {
                sendText(mEKeyBoard.getEtChat().getText().toString().trim(), TXT);
            }
        });

        if (mEMConversation.isGroup()) {
            mEKeyBoard.findViewById(R.id.btn_voice_or_text).setVisibility(View.GONE);
        }


        mEKeyBoard.addOnFuncKeyBoardListener(new FuncLayout.OnFuncKeyBoardListener() {
            @Override
            public void OnFuncPop(int i) {
                mListView.setSelection(mListView.getCount() - 1);
            }

            @Override
            public void OnFuncClose() {

            }
        });

        mEKeyBoard.getBtnVoice().setOnTouchListener(new PressToSpeakListen());
        voiceRecorder = new VoiceRecorder(micImageHandler);

        if (!mEMConversation.isGroup()) {
            View funcView = LayoutInflater.from(this).inflate(R.layout.func_layout, null);
            mEKeyBoard.addFuncView(funcView);

            ImageView ivTr = funcView.findViewById(R.id.iv_tm);
            ivTr.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(ChatActivity.this, TransferAccountsActivity.class);
                    User user = FriendManager.getInstance().getUserById(mUserId);
                    intent.putExtra(TransferAccountsActivity.NAME, user.getUserName());
                    intent.putExtra(TransferAccountsActivity.GENDER, user.getGender());
                    intent.putExtra(TransferAccountsActivity.ADDRESS, user.getWalletAddress());
                    startActivityForResult(intent, REQUEST_CODE_TRANSFER);
                }
            });

            ImageView ivCamera = funcView.findViewById(R.id.iv_camera);
            ImageView ivPhoto = funcView.findViewById(R.id.iv_photo);
            ImageView ivVideo = funcView.findViewById(R.id.iv_video);

            ivCamera.setOnClickListener(v -> {
                if (ActivityCompat.checkSelfPermission(ChatActivity.this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED
                        || ActivityCompat.checkSelfPermission(ChatActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                    requestCameraPermission();
                } else {
                    cameraFile = new File(getFilesDir(), "anypeer_" + System.currentTimeMillis() + ".jpg");
                    cameraFile.getParentFile().mkdirs();
                    startActivityForResult(new Intent(MediaStore.ACTION_IMAGE_CAPTURE).putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(cameraFile)), REQUEST_CODE_CAMERA);
                }
            });

            ivPhoto.setOnClickListener(v -> {
                if (ActivityCompat.checkSelfPermission(ChatActivity.this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED
                        || ActivityCompat.checkSelfPermission(ChatActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                    requestCameraPermission();
                } else {
                    Intent intent;
                    if (Build.VERSION.SDK_INT < 19) {
                        intent = new Intent(Intent.ACTION_GET_CONTENT);
                        intent.setType("video/;image/");
                    } else {
                        intent = new Intent(Intent.ACTION_PICK);
                        intent.setData(MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
//                        intent.setData(MediaStore.Video.Media.EXTERNAL_CONTENT_URI);
                    }
                    startActivityForResult(intent, REQUEST_CODE_LOCAL);
                }
            });

            ivVideo.setOnClickListener(v -> {
                if (ActivityCompat.checkSelfPermission(ChatActivity.this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED
                        || ActivityCompat.checkSelfPermission(ChatActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                    requestCameraPermission();
                } else {
                    MediaRecorderConfig config = new MediaRecorderConfig.Buidler()
                            .fullScreen(true)
                            .smallVideoWidth(0)
                            .smallVideoHeight(480)
                            .recordTimeMax(15000)
                            .recordTimeMin(1000)
                            .maxFrameRate(20)
                            .videoBitrate(580000)
                            .captureThumbnailsTime(1)
                            .build();
                    MediaRecorderActivity.goSmallVideoRecorder(ChatActivity.this, SendSmallVideoActivity.class.getName(), config);
                }
            });
        }

        initEmoticon();
    }

    private void setUpView() {
        if (mEMConversation == null) {
            mEMConversation = new ChatConversation();
            mEMConversation.setmUserId(mUserId);
        }

        ChatDBManager.getInstance().setMessagesReaded(mUserId);
        ChatDBManager.getInstance().addConversation(mEMConversation);

        mAdapter = new MessageAdapter(this, mEMConversation);
        mAdapter.setHeaderOnClick(this);
        // 显示消息
        mListView.setAdapter(mAdapter);
        int count = mListView.getCount();
        if (count > 0) {
            mListView.setSelection(count);
        }

        mListView.setOnTouchListener((view, motionEvent) -> {
            mEKeyBoard.reset();
            return false;
        });

        requestRecordAudioPermission();
    }

    public void more(View view) {
        hideKeyboard();
    }

    private void hideKeyboard() {
        if (getWindow().getAttributes().softInputMode != WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN) {
            if (getCurrentFocus() != null)
                mInputMethodManager.hideSoftInputFromWindow(getCurrentFocus()
                        .getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
        }
    }

    private void onReceive(String friendMessage, ChatMessage.Type msgType) {
        ChatMessage message = new ChatMessage();
        message.setMsgId(UUID.randomUUID().toString());
        message.setDirect(ChatMessage.Direct.RECEIVE.ordinal());
        message.setUnread(false);
        message.setType(msgType.ordinal());
        message.setMessage(friendMessage);

        ChatDBManager.getInstance().addMessage(mUserId, message);
        mEMConversation = ChatDBManager.getInstance().getChatConversationByUserId(mUserId);

        mAdapter.refresh();

        mListView.setSelection(mListView.getCount() - 1);
    }

    private void onReceiveGroupMessage(String groupID, String userId, String friendMessage, ChatMessage.Type msgType) {
        ChatMessage message = new ChatMessage();
        message.setMsgId(UUID.randomUUID().toString());
        message.setDirect(ChatMessage.Direct.RECEIVE.ordinal());
        message.setUnread(false);
        message.setType(msgType.ordinal());
        message.setMessage(friendMessage);

        message.setFromId(userId);

        ChatDBManager.getInstance().addMessage(groupID, message);
        mEMConversation = ChatDBManager.getInstance().getChatConversationByUserId(groupID);
        mAdapter.refresh();

        mListView.setSelection(mListView.getCount() - 1);
    }

    private void onReceiveFile(byte[] fileData, ChatMessage.Type msgType) {
        if (fileData == null) {
            return;
        }

        String filePath = FileUtils.bytesToFile(fileData);
        if (filePath == null || filePath.isEmpty()) {
            return;
        }

        ChatMessage message = new ChatMessage();
        message.setMsgId(UUID.randomUUID().toString());
        message.setDirect(ChatMessage.Direct.RECEIVE.ordinal());
        message.setType(msgType.ordinal());
        message.setUnread(false);
        // Set the message body
        if (msgType == ChatMessage.Type.IMAGE) {
            message.setFilePath(filePath);
        } else if (msgType == ChatMessage.Type.VOICE) {
            message.setFilePath(filePath);
        } else if (msgType == ChatMessage.Type.VIDEO) {
            MediaMetadataRetriever mmr = new MediaMetadataRetriever();
            mmr.setDataSource(filePath);//设置数据源为该文件对象指定的绝对路径
            Bitmap bitmap = mmr.getFrameAtTime();

            String imagePath = getFilesDir().getAbsolutePath();

            String imageFilePath = imagePath + "_" + System.currentTimeMillis() + ".png";
            FileUtils.saveBitmap(imageFilePath, bitmap);

            message.setLocalThumb(imageFilePath);
            message.setFilePath(filePath);
        }

        ChatDBManager.getInstance().addMessage(mUserId, message);
        mEMConversation = ChatDBManager.getInstance().getChatConversationByUserId(mUserId);

        mAdapter.refresh();

        mListView.setSelection(mListView.getCount() - 1);
    }

    private void sendText(final String content, ChatMessage.Type type) {
        if (content.length() > 0) {
            ChatMessage message = new ChatMessage();
            final String msgId = UUID.randomUUID().toString();
            message.setMsgId(msgId);
            message.setDirect(ChatMessage.Direct.SEND.ordinal());
            message.setType(type.ordinal());
            message.setStatus(ChatMessage.Status.INPROGRESS.ordinal());
            message.setMessage(content);

            ChatDBManager.getInstance().addMessage(mUserId, message);
            mEMConversation = ChatDBManager.getInstance().getChatConversationByUserId(mUserId);
            mAdapter.refresh();

            mEKeyBoard.getEtChat().setText("");

            mListView.setSelection(mListView.getCount() - 1);

            if (mEMConversation.isGroup()) {
                try {
                    mNetUtils.groupSendMessage(mName, content, type);
                }
                catch (Exception e) {
                    Toast.makeText(ChatActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();

	                //TODO Restart group invite?
	                mNetUtils.sendGroupLeaveMessage(mName);
                }
            } else {
                //TODO Add the message type to msgId
                mNetUtils.sendMessage(mUserId, content, getAssembledIdForTextSending(msgId, type));
            }
        }
    }

    private String getAssembledIdForTextSending(String originalMsgId, ChatMessage.Type type) {
        return originalMsgId + type.ordinal();
    }

    private void sendPic(final String picPath) {
        Log.d(TAG, "sendPic picPath=" + picPath);
        if (!TextUtils.isEmpty(picPath)) {
            ChatMessage message = new ChatMessage();
            final String msgId = UUID.randomUUID().toString();
            message.setMsgId(msgId);
            message.setDirect(ChatMessage.Direct.SEND.ordinal());
            message.setType(ChatMessage.Type.IMAGE.ordinal());
            message.setStatus(ChatMessage.Status.INPROGRESS.ordinal());
            message.setFilePath(picPath);

            ChatDBManager.getInstance().addMessage(mUserId, message);
            mEMConversation = ChatDBManager.getInstance().getChatConversationByUserId(mUserId);
            mAdapter.refresh();

            mEKeyBoard.reset();
            new Handler().postDelayed(() -> mListView.setSelection(mListView.getCount() - 1), 100);

            Luban.with(this)
                    .load(picPath)
                    .ignoreBy(100)
                    .setTargetDir(getFilesDir().getAbsolutePath())
                    .filter(path -> !(TextUtils.isEmpty(path) || path.toLowerCase().endsWith(".gif")))
                    .setCompressListener(new OnCompressListener() {
                        @Override
                        public void onStart() {
                            // TODO 压缩开始前调用，可以在方法内启动 loading UI
                        }

                        @Override
                        public void onSuccess(File file) {
                            // TODO 压缩成功后调用，返回压缩后的图片文件
                            mNetUtils.sendFile(mUserId, file.getAbsoluteFile(), ChatMessage.Type.IMAGE, msgId);
                        }

                        @Override
                        public void onError(Throwable e) {
                            // TODO 当压缩过程出现问题时调用
                        }
                    }).launch();
        }
    }

    private void sendVoice(final String voicePath) {
        Log.d(TAG, "sendVoice voicePath=" + voicePath);
        if (!TextUtils.isEmpty(voicePath)) {
            ChatMessage message = new ChatMessage();
            final String msgId = UUID.randomUUID().toString();
            message.setMsgId(msgId);
            message.setDirect(ChatMessage.Direct.SEND.ordinal());
            message.setType(ChatMessage.Type.VOICE.ordinal());
            message.setStatus(ChatMessage.Status.INPROGRESS.ordinal());
            message.setFilePath(voicePath);

            ChatDBManager.getInstance().addMessage(mUserId, message);
            mEMConversation = ChatDBManager.getInstance().getChatConversationByUserId(mUserId);
            mAdapter.refresh();

            mEKeyBoard.reset();

            mListView.setSelection(mListView.getCount() - 1);
            mNetUtils.sendFile(mUserId, voicePath, ChatMessage.Type.VOICE, msgId);
        }
    }

    public void sendVideo(final String videoPath) {
        Log.d(TAG, "sendVideo sendVideo=" + videoPath);
        if (!TextUtils.isEmpty(videoPath)) {
            ChatMessage message = new ChatMessage();
            final String msgId = UUID.randomUUID().toString();
            message.setMsgId(msgId);
            message.setDirect(ChatMessage.Direct.SEND.ordinal());
            message.setType(ChatMessage.Type.VIDEO.ordinal());
            message.setStatus(ChatMessage.Status.INPROGRESS.ordinal());
            message.setFilePath(videoPath);

            MediaMetadataRetriever mmr = new MediaMetadataRetriever();
            mmr.setDataSource(videoPath);//设置数据源为该文件对象指定的绝对路径
            Bitmap bitmap = mmr.getFrameAtTime();

            String imagePath = getFilesDir().getAbsolutePath() + "/";

            String imageFilePath = imagePath + "_" + System.currentTimeMillis() + ".png";
            FileUtils.saveBitmap(imageFilePath, bitmap);

            message.setLocalThumb(imageFilePath);

            ChatDBManager.getInstance().addMessage(mUserId, message);
            mEMConversation = ChatDBManager.getInstance().getChatConversationByUserId(mUserId);
            mAdapter.refresh();

            mEKeyBoard.reset();

            new Handler().postDelayed(() -> mListView.setSelection(mListView.getCount() - 1), 100);
            mNetUtils.sendFile(mUserId, videoPath, ChatMessage.Type.VIDEO, msgId);
        }
    }

    @Override
    public void onClick(int position) {
        ChatMessage emMessage = mEMConversation.getmMessages().get(position);
        if (ChatMessage.Direct.getMsgDirect(emMessage.getDirect()) == ChatMessage.Direct.RECEIVE) {
            if (!mEMConversation.isGroup()) {
                User user = null;
                List<FriendInfo> friendInfos = NetUtils.getInstance().getFriends();
                if (friendInfos != null) {
                    for (int i = 0; i < friendInfos.size(); i++) {
                        if (mUserId.equals(friendInfos.get(i).getUserId())) {
                            FriendInfo info = friendInfos.get(i);
                            user = ContactAdapter.friendInfo2User(info);
                        }
                    }
                }

                if (TextUtils.isEmpty(user.getUserName())) {
                    user.setUserName(mName);
                }

                Intent intent = new Intent(this, FriendDetailActivity.class);
                intent.putExtra(Utils.USERID, user.getUserId());
                startActivity(intent);
            }
            else {
	            // TODO add group friend
//	            try {
//	            	String id = emMessage.getFromId();
//		            FriendInfo fi = NetUtils.getInstance().getFriend(id);
//		            if (fi != null) {
//			            Intent intent = new Intent(this, FriendDetailActivity.class);
//			            intent.putExtra(Utils.USERID, id);
//			            startActivity(intent);
//			            finish();
//		            }
//		            else {
//			            MaterialDialog.Builder builder = new MaterialDialog.Builder(this);
//			            builder.onNegative((dialog, which) -> dialog.dismiss());
//
//		                builder.title(R.string.friend_add_text1);
//		                String name = NetUtils.getInstance().peerName(id);
//		                builder.content(getString(R.string.friend_add_ask_1) + name + getString(R.string.friend_add_ask_2));
//
//			            builder.negativeText(R.string.friend_add_no);
//			            builder.positiveText(R.string.friend_add_yes);
//
//			            builder.onPositive((dialog, which) -> {
//                            dialog.dismiss();
//                        }).show();
//                    }
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }
            }
        }
    }

    @Override
    public void call(String path) {
        sendVideo(path);
    }

    private class MessageServiceConn implements ServiceConnection {
        @Override
        public void onServiceConnected(ComponentName name, IBinder binder) {
            if (mMessageService == null) {
                mMessageService = ((MessageService.LocalBinder) binder).getService();
                mMessageService.addMessageCallback(ChatActivity.this);
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mMessageService.removeMessageCallback(ChatActivity.this);
            mMessageService = null;
        }
    }

    private void initEmoticon() {
        ArrayList<EmojiBean> emojiArray = new ArrayList<>();
        Collections.addAll(emojiArray, DefEmoticons.sEmojiArray);

        final EmoticonClickListener emoticonClickListener = new EmoticonClickListener() {
            @Override
            public void onEmoticonClick(Object o, int actionType, boolean isDelBtn) {
                if (isDelBtn) {
                    int action = KeyEvent.ACTION_DOWN;
                    int code = KeyEvent.KEYCODE_DEL;
                    KeyEvent event = new KeyEvent(action, code);
                    mEKeyBoard.getEtChat().onKeyDown(KeyEvent.KEYCODE_DEL, event);
                } else {
                    if (o == null) {
                        return;
                    }
                    String content = null;
                    if (o instanceof EmojiBean) {
                        content = ((EmojiBean) o).emoji;
                    }
                    int index = mEKeyBoard.getEtChat().getSelectionStart();
                    Editable editable = mEKeyBoard.getEtChat().getText();
                    editable.insert(index, content);
                }
            }
        };

        final EmoticonDisplayListener emoticonDisplayListener = (i, viewGroup, viewHolder, object, isDelBtn) -> {
            final EmojiBean emojiBean = (EmojiBean) object;
            if (emojiBean == null && !isDelBtn) {
                return;
            }

            viewHolder.ly_root.setBackgroundResource(com.keyboard.view.R.drawable.bg_emoticon);

            if (isDelBtn) {
                viewHolder.iv_emoticon.setImageResource(R.mipmap.icon_del);
            } else {
                viewHolder.iv_emoticon.setImageResource(emojiBean.icon);
            }

            viewHolder.rootView.setOnClickListener(v -> emoticonClickListener.onEmoticonClick(emojiBean, 0, isDelBtn));
        };

        PageViewInstantiateListener pageViewInstantiateListener = (PageViewInstantiateListener<EmoticonPageEntity>) (viewGroup, i, pageEntity) -> {
            if (pageEntity.getRootView() == null) {
                EmoticonPageView pageView = new EmoticonPageView(viewGroup.getContext());
                pageView.setNumColumns(pageEntity.getRow());
                pageEntity.setRootView(pageView);
                try {
                    EmoticonsAdapter adapter = new EmoticonsAdapter(viewGroup.getContext(), pageEntity, null);
                    // emoticon instantiate
                    adapter.setOnDisPlayListener(emoticonDisplayListener);
                    pageView.getEmoticonsGridView().setAdapter(adapter);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            return pageEntity.getRootView();
        };

        EmoticonPageSetEntity xhsPageSetEntity
                = new EmoticonPageSetEntity.Builder()
                .setLine(3)
                .setRow(7)
                .setEmoticonList(emojiArray)
                .setIPageViewInstantiateItem(pageViewInstantiateListener)
                .setShowDelBtn(EmoticonPageEntity.DelBtnStatus.LAST)
                .setIconUri(ImageBase.Scheme.DRAWABLE.toUri("ic_launcher"))
                .build();

        PageSetAdapter pageSetAdapter = new PageSetAdapter();
        pageSetAdapter.add(xhsPageSetEntity);
        mEKeyBoard.setAdapter(pageSetAdapter);

        // add a filter
        mEKeyBoard.getEtChat().addEmoticonFilter(new EmojiFilter());
    }

    /**
     * requestCameraPermission
     */
    private void requestCameraPermission() {
        String[] permissions = {Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.CAMERA};

        for (String str : permissions) {
            if (this.checkSelfPermission(str) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, permissions, REQUEST_CAMERA);
            }
        }
    }

    /**
     * requestRecordAudioPermission
     */
    private void requestRecordAudioPermission() {
        String[] permissions = {Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.RECORD_AUDIO};

        for (String str : permissions) {
            if (this.checkSelfPermission(str) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, permissions, REQUEST_RECORD_AUDIO);
            }
        }
    }

    class PressToSpeakListen implements View.OnTouchListener {
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    animationDrawable.start();
                    try {
                        v.setPressed(true);
                        wakeLock.acquire();
                        if (VoicePlayClickListener.isPlaying)
                            VoicePlayClickListener.currentPlayListener
                                    .stopPlayVoice();
                        recordingContainer.setVisibility(View.VISIBLE);
                        recordingHint.setText(getString(R.string.move_up_to_cancel));
                        recordingHint.setBackgroundColor(Color.TRANSPARENT);
                        voiceRecorder.startRecording(null, mUserId, getApplicationContext());
                    } catch (Exception e) {
                        e.printStackTrace();
                        v.setPressed(false);
                        if (wakeLock.isHeld())
                            wakeLock.release();
                        if (voiceRecorder != null)
                            voiceRecorder.discardRecording();
                        recordingContainer.setVisibility(View.INVISIBLE);
                        Toast.makeText(ChatActivity.this, R.string.recoding_fail, Toast.LENGTH_SHORT).show();
                        return false;
                    }

                    return true;
                case MotionEvent.ACTION_MOVE: {
                    if (event.getY() < 0) {
                        recordingHint
                                .setText(getString(R.string.release_to_cancel));
                        recordingHint
                                .setBackgroundResource(R.drawable.recording_text_hint_bg);
                    } else {
                        recordingHint
                                .setText(getString(R.string.move_up_to_cancel));
                        recordingHint.setBackgroundColor(Color.TRANSPARENT);
                        animationDrawable.start();
                    }
                    return true;
                }
                case MotionEvent.ACTION_UP:
                    if (animationDrawable.isRunning()) {
                        animationDrawable.stop();
                    }
                    v.setPressed(false);
                    recordingContainer.setVisibility(View.INVISIBLE);
                    if (wakeLock.isHeld()) {
                        wakeLock.release();
                    }

                    if (event.getY() < 0) {
                        // discard the recorded audio.
                        voiceRecorder.discardRecording();
                    }

                    // TODO: Network send voice
                    if (voiceRecorder != null) {
                        int length = voiceRecorder.stopRecoding();
                        if (length > 0) {
                            String voiceFile = voiceRecorder.getVoiceFilePath();
                            if (voiceFile != null && !voiceFile.isEmpty()) {
                                try {
                                    sendVoice(voiceFile);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                    }

                    return true;
                default:
                    recordingContainer.setVisibility(View.INVISIBLE);
                    if (voiceRecorder != null)
                        voiceRecorder.discardRecording();
                    return false;
            }
        }
    }

    public static boolean isActiveChat(String userId) {
        if (userId != null) {
            return IS_CHAT_START && userId.equals(ACTIVE_USER_ID);
        }

        return false;
    }

    private static void initSmallVideo() {
        // Set the store path
        File dcim = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM);
        if (DeviceUtils.isZte()) {
            if (dcim.exists()) {
                JianXiCamera.setVideoCachePath(dcim + "/anypeer/");
            } else {
                JianXiCamera.setVideoCachePath(dcim.getPath().replace("/sdcard/",
                        "/sdcard-ext/")
                        + "/anypeer/");
            }
        } else {
            JianXiCamera.setVideoCachePath(dcim + "/anypeer/");
        }

        // initialize
        JianXiCamera.initialize(false, null);
    }
}

