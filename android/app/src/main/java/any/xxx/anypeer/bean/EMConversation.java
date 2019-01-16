package any.xxx.anypeer.bean;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import any.xxx.anypeer.util.AnyWallet;

public class EMConversation implements Serializable {
    List<EMMessage> mMessages;
    private int mUnreadMsgCount = 0;
    private String mUsername;
    private String mUserId;
    private boolean mIsOnline = false;
    private String mWalletAddress;
    private String mGender;
    private String mEmail;

    public EMConversation() {
    }

    public EMConversation(String username, String userId) {
        this.mUsername = username;
        this.mUserId = userId;
        this.mMessages = new ArrayList<>();
    }

    public boolean isOnline() {
        return mIsOnline;
    }

    public void setOnline(boolean online) {
        mIsOnline = online;
    }

    public List<EMMessage> getMessages() {
        return mMessages;
    }

    public void setMessages(List<EMMessage> messages) {
        this.mMessages = messages;
    }

    public int getUnreadMsgCount() {
        mUnreadMsgCount = 0;
        if (mMessages != null && !mMessages.isEmpty()) {
            for (EMMessage emMessage : mMessages) {
                if (emMessage.direct == EMMessage.Direct.RECEIVE && emMessage.isUnread()) {
                    mUnreadMsgCount++;
                }
            }
        }

        return mUnreadMsgCount;
    }

    public void setUnreadMsgCount(int unreadMsgCount) {
        this.mUnreadMsgCount = unreadMsgCount;
    }

    public String getUsername() {
        return mUsername;
    }

    public void setUsername(String username) {
        this.mUsername = username;
    }

    public void setEmail(String email) {
        mEmail = email;
    }

    public String getEmail() {
        return mEmail;
    }

    public String getUserId() {
        return mUserId;
    }

    public void setUserId(String userId) {
        this.mUserId = userId;
    }

    public String getGender() {
        return mGender;
    }

    public void setGender(String mGender) {
        this.mGender = mGender;
    }

    public int getMsgCount() {
        return this.mMessages.size();
    }

    public EMMessage getMessage(int position) {
        return mMessages.get(position);
    }

    public void setWalletAddress(String address) {
        mWalletAddress = null;
        if (AnyWallet.getInstance().isAddressValid(address)) {
            mWalletAddress = address;
        }
    }

    public String getWalletAdrress() {
        return mWalletAddress;
    }

    public void addMessage(EMMessage emMessage) {
        mMessages.add(emMessage);
    }

    public EMMessage getLastMessage() {
        return this.mMessages.size() == 0 ? null : this.mMessages.get(this.mMessages.size() - 1);
    }

    public void setMessagesReaded() {
        if (mMessages != null && !mMessages.isEmpty()) {
            for (EMMessage emMessage : mMessages) {
                if (emMessage.direct == EMMessage.Direct.RECEIVE && emMessage.isUnread()) {
                    emMessage.setUnread(false);
                }
            }
        }
    }
}
