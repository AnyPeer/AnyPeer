package any.xxx.anypeer.bean;

import java.io.Serializable;
import java.util.List;

public class RankListBean implements Serializable {
    private List<RankBean> result;

    public List<RankBean> getResult() {
        return result;
    }

    public void setResult(List<RankBean> result) {
        this.result = result;
    }
}
