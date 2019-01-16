package any.xxx.anypeer.bean;

import java.io.Serializable;

public class PicMessageBody extends MessageBody implements Serializable {
    private String picPath;

    public PicMessageBody() {
    }

    public PicMessageBody(String picPath) {
        this.picPath = picPath;
    }

    public String getPicPath() {
        return picPath;
    }

    public void setPicPath(String picPath) {
        this.picPath = picPath;
    }
}
