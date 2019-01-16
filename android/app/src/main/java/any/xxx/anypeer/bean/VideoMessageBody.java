package any.xxx.anypeer.bean;

import java.io.Serializable;

public class VideoMessageBody extends MessageBody implements Serializable {
    private String videoPath;
    private String localThumb;

    public VideoMessageBody() {
    }

    public VideoMessageBody(String videoPath) {
        this.videoPath = videoPath;
    }

    public String getVideoPath() {
        return videoPath;
    }

    public void setVideoPath(String videoPath) {
        this.videoPath = videoPath;
    }

    public String getLocalThumb() {
        return localThumb;
    }

    public void setLocalThumb(String localThumb) {
        this.localThumb = localThumb;
    }
}
