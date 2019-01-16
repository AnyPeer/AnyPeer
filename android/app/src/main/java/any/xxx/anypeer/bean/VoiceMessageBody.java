package any.xxx.anypeer.bean;

import java.io.Serializable;

public class VoiceMessageBody extends MessageBody implements Serializable {
    private String voicePath;

    public VoiceMessageBody() {
    }

    public VoiceMessageBody(String voicePath) {
        this.voicePath = voicePath;
    }

    public String getVoicePath() {
        return voicePath;
    }

    public void setVoicePath(String voicePath) {
        this.voicePath = voicePath;
    }
}
