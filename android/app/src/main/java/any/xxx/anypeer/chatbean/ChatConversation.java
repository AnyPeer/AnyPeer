package any.xxx.anypeer.chatbean;

import any.xxx.anypeer.util.AnyWallet;
import io.realm.RealmList;
import io.realm.RealmObject;
import io.realm.annotations.Ignore;
import io.realm.annotations.PrimaryKey;

public class ChatConversation extends RealmObject {
    @PrimaryKey
    private String mUserId;

    private String mWalletAddress;
    private RealmList<ChatMessage> mMessages;
    private boolean isGroup;
    private String groupName;

    @Ignore
    private int mUnreadMsgCount = 0;

    public ChatMessage getLastMessage() {
        return this.mMessages.size() == 0 ? null : this.mMessages.get(this.mMessages.size() - 1);
    }

    public String getmUserId() {
        return mUserId;
    }

    public void setmUserId(String mUserId) {
        this.mUserId = mUserId;
    }

    public String getWalletAddress() {
        return mWalletAddress;
    }

    public void setWalletAddress(String mWalletAdress) {
        this.mWalletAddress = null;
        if (AnyWallet.getInstance().isAddressValid(mWalletAdress)) {
            this.mWalletAddress = mWalletAdress;
        }
    }

    public RealmList<ChatMessage> getmMessages() {
        return mMessages;
    }

    public void setmMessages(RealmList<ChatMessage> mMessages) {
        this.mMessages = mMessages;
    }

    public int getmUnreadMsgCount() {
        return mUnreadMsgCount;
    }

    public void setmUnreadMsgCount(int mUnreadMsgCount) {
        this.mUnreadMsgCount = mUnreadMsgCount;
    }

    public int getUnreadMsgCount() {
        mUnreadMsgCount = 0;
        if (mMessages != null && !mMessages.isEmpty()) {
            for (ChatMessage chatMessage : mMessages) {
                if (chatMessage.getDirect() == ChatMessage.Direct.RECEIVE.ordinal() && chatMessage.isUnread()) {
                    mUnreadMsgCount++;
                }
            }
        }

        return mUnreadMsgCount;
    }

    public boolean isGroup() {
        return isGroup;
    }

    public void setGroup(boolean group) {
        isGroup = group;
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    @Override
    public String toString() {
        return "ChatConversation{" +
                "mUserId='" + mUserId + '\'' +
                ", mWalletAdress='" + mWalletAddress + '\'' +
                ", mMessages=" + mMessages +
                ", mUnreadMsgCount=" + mUnreadMsgCount +
                '}';
    }
}
