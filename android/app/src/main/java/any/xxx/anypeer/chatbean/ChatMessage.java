package any.xxx.anypeer.chatbean;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

public class ChatMessage extends RealmObject {
    @PrimaryKey
    private String msgId;

    private String message;
    private String filePath;
    private String localThumb;
    private long mMsgTime;
    private boolean unread;
    private int type;
    private int direct;
    private int status;

    private ChatConversation owners;

    public ChatMessage() {
        this.mMsgTime = System.currentTimeMillis();
        this.unread = true;
    }

    public static enum Direct {
        SEND,
        RECEIVE;

        private Direct() {
        }

        public static Direct getMsgDirect(int direct) {
            if (direct == SEND.ordinal()) {
                return SEND;
            } else {
                return RECEIVE;
            }
        }
    }

    public static enum Type {
        TXT,
        MONEY,
        IMAGE,
        VOICE,
        VIDEO,
        UNKNOWN;

        private Type() {
        }

        public static Type getMsgType(int type) {
            if (type == TXT.ordinal()) {
                return TXT;
            } else if (type == MONEY.ordinal()) {
                return MONEY;
            } else if (type == IMAGE.ordinal()) {
                return IMAGE;
            } else if (type == VOICE.ordinal()) {
                return VOICE;
            } else if (type == VIDEO.ordinal()) {
                return VIDEO;
            }
            return UNKNOWN;
        }
    }

    public static enum Status {
        SUCCESS,
        FAIL,
        INPROGRESS;

        private Status() {
        }

        public static Status getMsgStatus(int status) {
            if (status == SUCCESS.ordinal()) {
                return SUCCESS;
            } else if (status == FAIL.ordinal()) {
                return FAIL;
            } else {
                return INPROGRESS;
            }
        }
    }

    public String getMsgId() {
        return msgId;
    }

    public void setMsgId(String msgId) {
        this.msgId = msgId;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public String getLocalThumb() {
        return localThumb;
    }

    public void setLocalThumb(String localThumb) {
        this.localThumb = localThumb;
    }

    public long getmMsgTime() {
        return mMsgTime;
    }

    public void setmMsgTime(long mMsgTime) {
        this.mMsgTime = mMsgTime;
    }

    public boolean isUnread() {
        return unread;
    }

    public void setUnread(boolean unread) {
        this.unread = unread;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public int getDirect() {
        return direct;
    }

    public void setDirect(int direct) {
        this.direct = direct;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public ChatConversation getOwners() {
        return owners;
    }

    public void setOwners(ChatConversation owners) {
        this.owners = owners;
    }

    @Override
    public String toString() {
        return "ChatMessage{" +
                "msgId='" + msgId + '\'' +
                ", message='" + message + '\'' +
                ", filePath='" + filePath + '\'' +
                ", localThumb='" + localThumb + '\'' +
                ", mMsgTime=" + mMsgTime +
                ", unread=" + unread +
                ", type=" + type +
                ", direct=" + direct +
                ", status=" + status +
                '}';
    }
}
