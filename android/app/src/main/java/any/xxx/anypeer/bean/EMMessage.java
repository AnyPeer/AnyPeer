package any.xxx.anypeer.bean;

import java.io.Serializable;
import java.util.Random;

public class EMMessage implements Serializable {
    private String msgId;
    private TextMessageBody mBody;
    private PicMessageBody mPicMessageBody;
    private VoiceMessageBody mVoiceMessageBody;
    private VideoMessageBody mVideoMessageBody;
    private long mMsgTime;
    public EMMessage.Direct direct;
    public EMMessage.Type type = Type.TXT;
    public EMMessage.Status status;
    private boolean unread;

    private static Random sRandGen;
    private static char[] sNumbersAndLetters;
    private static long sId;

    static {
        sRandGen = new Random();
        sNumbersAndLetters = "0123456789abcdefghijklmnopqrstuvwxyz0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ".toCharArray();
        sId = 0L;
    }

    public EMMessage() {
        this.mMsgTime = System.currentTimeMillis();
        this.unread = true;
    }

    public TextMessageBody getBody() {
        return mBody;
    }

    public void setBody(TextMessageBody body) {
        this.mBody = body;
    }

    public PicMessageBody getPicMessageBody() {
        return mPicMessageBody;
    }

    public void setPicMessageBody(PicMessageBody mPicMessageBody) {
        this.mPicMessageBody = mPicMessageBody;
    }

    public VoiceMessageBody getVoiceMessageBody() {
        return mVoiceMessageBody;
    }

    public void setVoiceMessageBody(VoiceMessageBody mVoiceMessageBody) {
        this.mVoiceMessageBody = mVoiceMessageBody;
    }

    public VideoMessageBody getVideoMessageBody() {
        return mVideoMessageBody;
    }

    public void setVideoMessageBody(VideoMessageBody videoMessageBody) {
        this.mVideoMessageBody = videoMessageBody;
    }

    public long getMsgTime() {
        return mMsgTime;
    }

    boolean isUnread() {
        return unread;
    }

    public void setUnread(boolean unread) {
        this.unread = unread;
    }

    public String getMsgId() {
        return msgId;
    }

    public void setMsgId(String msgId) {
        this.msgId = msgId;
    }

    public static enum Direct {
        SEND,
        RECEIVE;

        private Direct() {
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
            }
            else if (type == MONEY.ordinal()) {
                return MONEY;
            }
            else if (type == IMAGE.ordinal()) {
                return IMAGE;
            }
            else if (type == VOICE.ordinal()) {
                return VOICE;
            }
            else if (type == VIDEO.ordinal()) {
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
    }

    static String getUniqueMessageId() {
        String var0 = Long.toHexString(System.currentTimeMillis());
        var0 = var0.substring(6);

        return randomString(5) + "-" + Long.toString((sId++)) + "-" + var0;
    }

    static String randomString(int var0) {
        if (var0 < 1) {
            return null;
        } else {
            char[] var1 = new char[var0];

            for(int var2 = 0; var2 < var1.length; ++var2) {
                var1[var2] = sNumbersAndLetters[sRandGen.nextInt(71)];
            }

            return new String(var1);
        }
    }
}
