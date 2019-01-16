package any.xxx.anypeer.bean;

import java.io.Serializable;

public class TextMessageBody extends MessageBody implements Serializable {
    private String message;

    public TextMessageBody() {
    }

    public TextMessageBody(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
