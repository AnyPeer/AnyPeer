package any.xxx.anypeer.manager;

import android.content.Context;
import android.text.TextUtils;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import any.xxx.anypeer.R;
import any.xxx.anypeer.bean.EMConversation;
import any.xxx.anypeer.bean.EMMessage;
import any.xxx.anypeer.bean.TextMessageBody;
import any.xxx.anypeer.util.PrefereneceUtil;
import any.xxx.anypeer.util.Utils;

import java.util.ArrayList;
import java.util.List;

public class ChatManager {
    private static final String TAG = "ChatManager";
    private static final String IS_MESSAGE_SHOW_NOTIFICATION = "is_message_show_notification";
    private static final String IS_MESSAGE_VOICE = "is_message_voice";
    private static ChatManager instance = null;
    private List<EMConversation> mChatList;
    private Context mContext;

    public static ChatManager getInstance() {
        return instance;
    }

    public static ChatManager getInstance(Context context) {
        if (instance == null) {
            instance = new ChatManager(context);
        }
        return instance;
    }

    private ChatManager(Context context) {
        mContext = context;
    }

    public void initChat(Context context) {
        mContext = context;
        mChatList = initChatList();
        if (mChatList == null) {
            mChatList = new ArrayList<>();
        }
    }

    public void addFriend(String userName, String userId, String gender) {
        if (userName == null || userName.isEmpty()) {
            userName = mContext.getString(R.string.main_getting_friend_info);
        }

        if (containUser(userId)) {
        	return;
        }

	    EMConversation emConversation = new EMConversation(userName, userId);
        emConversation.setGender(gender);
	    TextMessageBody emMessage = new TextMessageBody();

	    List<EMMessage> emMessageList = new ArrayList<>();
	    EMMessage e = new EMMessage();
	    e.setBody(emMessage);
	    emMessageList.add(e);
	    emConversation.setMessages(emMessageList);
	    mChatList.add(emConversation);
        update();
    }

    public void addNewEMConversation(EMConversation emConversation) {
        if (emConversation != null) {
            if (!containUser(emConversation.getUserId())) {
                mChatList.add(emConversation);
                update();
            }
        }
    }

    private boolean containUser(String userId) {
	    for (EMConversation e : mChatList) {
		    if (e.getUserId().equals(userId)) {
			    return true;
		    }
	    }
	    return false;
    }

    public void updateFriend(String userName, String userId, String walletAddress, String gender, String email) {
        if (userName == null || userName.isEmpty()) {
            userName = "Getting the friend's name ..";
        }

        List<EMConversation> list = mChatList;
        for (EMConversation e : list) {
            if (e.getUserId().equals(userId)) {
                e.setUsername(userName);
                e.setWalletAddress(walletAddress);
                e.setGender(gender);
                e.setEmail(email);
                break;
            }
        }

        update();
    }

    public void removeFriend(String userId) {
        List<EMConversation> list = getChatList();
        for (EMConversation e : list) {
            if (e.getUserId().equals(userId)) {
                list.remove(e);
                update();
                break;
            }
        }
    }

    private void update() {
        String json = new Gson().toJson(mChatList);
        PrefereneceUtil.saveString(mContext, Utils.CHAT_LIST, json);
    }

    public List<EMConversation> getChatList() {
        return mChatList;
    }

    private List<EMConversation> initChatList() {
        String json = PrefereneceUtil.getString(mContext, Utils.CHAT_LIST);
        if (TextUtils.isEmpty(json) || json.equals("[null]")) {
            return null;
        }
        return new Gson().fromJson(json, new TypeToken<List<EMConversation>>() {}.getType());
    }

    public EMConversation getChat(String userId) {
        List<EMConversation> list = getChatList();
        for (EMConversation e : list) {
            if (e.getUserId().equals(userId)) {
                return e;
            }
        }

        return null;
    }

    public void addMessage(Context context, String userId, EMMessage message) {
        EMConversation emConversation = null;

        List<EMConversation> list = getChatList();
        for (EMConversation e : list) {
            if (e.getUserId().equals(userId)) {
                emConversation = e;
                break;
            }
        }

        if (emConversation != null) {
            emConversation.addMessage(message);
        }

        updateChatList(context, emConversation);
    }

    public EMMessage getMessage(Context context, String userId, String msgId) {
        EMConversation emConversation = null;

        List<EMConversation> list = getChatList();
        for (EMConversation e : list) {
            if (e.getUserId().equals(userId)) {
                emConversation = e;
                break;
            }
        }

        if (emConversation != null) {
            for (EMMessage emMessage : emConversation.getMessages()) {
                if (emMessage.getMsgId().equals(msgId)) {
                    return emMessage;
                }
            }
        }

        return null;
    }

    public void removeMessage(Context context, String userId, String msgId) {
        EMConversation emConversation = null;

        List<EMConversation> list = getChatList();
        for (EMConversation e : list) {
            if (e.getUserId().equals(userId)) {
                emConversation = e;
                break;
            }
        }

        if (emConversation != null) {
            int index = 0;
            for (EMMessage emMessage : emConversation.getMessages()) {
                if (emMessage.getMsgId() != null &&emMessage.getMsgId().equals(msgId)) {
                    index = emConversation.getMessages().indexOf(emMessage);
                    break;
                }
            }

            emConversation.getMessages().remove(index);
        }

        updateChatList(context, emConversation);
    }

    public void setMessageStatus(Context context, String userId, String msgId, EMMessage.Status status) {
        EMConversation emConversation = null;

        List<EMConversation> list = getChatList();
        for (EMConversation e : list) {
            if (e.getUserId().equals(userId)) {
                emConversation = e;
                break;
            }
        }

        if (emConversation != null) {
            int index = 0;
            for (EMMessage emMessage : emConversation.getMessages()) {
                if (emMessage.getMsgId() != null &&emMessage.getMsgId().equals(msgId)) {
                    index = emConversation.getMessages().indexOf(emMessage);
                    break;
                }
            }

            emConversation.getMessage(index).status = status;
        }

        updateChatList(context, emConversation);
    }

    public void initChatsSendState() {
        List<EMConversation> list = getChatList();
        if (list != null) {
            for (EMConversation e : list) {
                for (EMMessage emMessage : e.getMessages()) {
                    if (emMessage != null && emMessage.status == EMMessage.Status.INPROGRESS) {
                        emMessage.status = EMMessage.Status.FAIL;
                    }
                }
            }
            String json = new Gson().toJson(list);
            PrefereneceUtil.saveString(mContext, Utils.CHAT_LIST, json);
        }
    }

    private void updateChatList(Context context, EMConversation emConversation ) {
        EMConversation deleteEmConversation = null;

        List<EMConversation> list = getChatList();
        for (EMConversation e : list) {
            if (e.getUserId().equals(emConversation.getUserId())) {
                deleteEmConversation = e;
                break;
            }
        }

        list.remove(deleteEmConversation);
        list.add(emConversation);

        String json = new Gson().toJson(list);
        PrefereneceUtil.saveString(context, Utils.CHAT_LIST, json);
    }

    public void saveIsMessageShowNotification(Context context, boolean isShow) {
        PrefereneceUtil.saveBoolean(context, IS_MESSAGE_SHOW_NOTIFICATION, isShow);
    }

    public boolean getIsMessageShowNotification(Context context) {
        return PrefereneceUtil.getBoolean(context, IS_MESSAGE_SHOW_NOTIFICATION, true);
    }

    public void saveIsMessageShowVoice(Context context, boolean isShow) {
        PrefereneceUtil.saveBoolean(context, IS_MESSAGE_VOICE, isShow);
    }

    public boolean getIsMessageVoice(Context context) {
        return PrefereneceUtil.getBoolean(context, IS_MESSAGE_VOICE, true);
    }
}
