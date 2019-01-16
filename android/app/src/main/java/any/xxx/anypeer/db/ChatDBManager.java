package any.xxx.anypeer.db;

import android.util.Log;
import java.util.ArrayList;
import java.util.List;
import any.xxx.anypeer.chatbean.ChatConversation;
import any.xxx.anypeer.chatbean.ChatMessage;
import io.realm.Realm;

public class ChatDBManager {
    private static final String TAG = "ChatManager";
    private static ChatDBManager instance = new ChatDBManager();

    public static ChatDBManager getInstance() {
        return instance;
    }

    public void addConversation(ChatConversation chatConversation) {
        try {
            Log.d(TAG, "addConversation " + chatConversation.toString());
            Realm realm = Realm.getDefaultInstance();
            realm.beginTransaction();
            realm.copyToRealmOrUpdate(chatConversation);
            realm.commitTransaction();

            getAllChatConversaion();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void addMessage(String userId, ChatMessage emMessage) {
        try {
            Log.d(TAG, "addMessage " + emMessage.toString());
            Realm realm = Realm.getDefaultInstance();
            realm.beginTransaction();
            ChatConversation chatConversation = realm.where(ChatConversation.class).equalTo("mUserId", userId).findFirst();
            if (chatConversation != null) {
                chatConversation.getmMessages().add(emMessage);
                emMessage.setOwners(chatConversation);
            }
            realm.commitTransaction();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void removeMessage(String msgId) {
        try {
            Log.d(TAG, "removeMessage " + msgId);
            Realm realm = Realm.getDefaultInstance();
            realm.beginTransaction();
            ChatMessage msg = realm.where(ChatMessage.class).equalTo("msgId", msgId).findFirst();
            if (msg != null) {
                msg.deleteFromRealm();
            }
            realm.commitTransaction();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    public ChatConversation getChatConversaionByUserId(String userId) {
        try {
            Realm realm = Realm.getDefaultInstance();
            realm.beginTransaction();
            ChatConversation chatConversation = realm.where(ChatConversation.class).equalTo("mUserId", userId).findFirst();
            realm.commitTransaction();

            if (chatConversation != null) {
                Log.d(TAG, "getAllChatConversaion " + chatConversation.toString());
            }

            return chatConversation;
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public List<ChatConversation> getAllChatConversaion() {
        try {
            Realm realm = Realm.getDefaultInstance();
            List<ChatConversation> chatConversations = realm.where(ChatConversation.class).findAll();

            for (ChatConversation chatConversation : chatConversations) {
                Log.d(TAG, chatConversation.toString());
            }

            Log.d(TAG, "getAllChatConversaion size=" + chatConversations.size());
            return realm.copyFromRealm(chatConversations);
        }
        catch (Exception e) {
            e.printStackTrace();
        }

        return new ArrayList<>();
    }

    boolean isAdd(String userId) {
        try {
            Realm realm = Realm.getDefaultInstance();
            realm.beginTransaction();
            List<ChatConversation> chatConversations = realm.where(ChatConversation.class).equalTo("mUserId", userId).findAll();
            realm.commitTransaction();
            Log.d(TAG, "isAdd " + userId + " " + chatConversations.size());
            return chatConversations.size() != 0;
        }
        catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }

    public void setMessageStatus(String msgId, ChatMessage.Status status) {
        try {
            Log.d(TAG, "setMessageStatus " + msgId + " " + status);
            Realm realm = Realm.getDefaultInstance();
            realm.beginTransaction();
            ChatMessage msg = realm.where(ChatMessage.class).equalTo("msgId", msgId).findFirst();
            if (msg != null) {
                msg.setStatus(status.ordinal());
            }
            realm.commitTransaction();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void initChatsSendState() {
        try {
            Log.d(TAG, "setMessageStatus ");
            Realm realm = Realm.getDefaultInstance();
            realm.beginTransaction();

            List<ChatMessage> chatMessages = realm.where(ChatMessage.class).equalTo("status", ChatMessage.Status.INPROGRESS.ordinal()).findAll();
            for (ChatMessage chatMessage : chatMessages) {
                chatMessage.setStatus(ChatMessage.Status.FAIL.ordinal());
            }

            realm.commitTransaction();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void setMessagesReaded(String userId) {
        try {
            Log.d(TAG, "setMessagesReaded ");
            Realm realm = Realm.getDefaultInstance();
            realm.beginTransaction();

            ChatConversation chatConversation = realm.where(ChatConversation.class).equalTo("mUserId", userId).findFirst();
            if (chatConversation != null) {
                for (ChatMessage chatMessage : chatConversation.getmMessages()) {
                    chatMessage.setUnread(false);
                }
            }

            realm.commitTransaction();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }
}
