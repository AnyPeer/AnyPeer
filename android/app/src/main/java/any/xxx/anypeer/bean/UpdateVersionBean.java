package any.xxx.anypeer.bean;

import java.io.Serializable;

public class UpdateVersionBean implements Serializable {

    private int version;
    private boolean is_force;
    private String description;
    private String url;

    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
    }

    public boolean isIs_force() {
        return is_force;
    }

    public void setIs_force(boolean is_force) {
        this.is_force = is_force;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}
