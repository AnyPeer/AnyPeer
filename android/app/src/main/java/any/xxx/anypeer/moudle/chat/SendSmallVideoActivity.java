package any.xxx.anypeer.moudle.chat;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import com.mabeijianxi.smallvideorecord2.MediaRecorderActivity;
import any.xxx.anypeer.manager.SendVideoManager;

public class SendSmallVideoActivity extends AppCompatActivity  {
    private String videoUri;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initData();
    }


    private void initData() {
        videoUri = getIntent().getStringExtra(MediaRecorderActivity.VIDEO_URI);
        SendVideoManager.getInstance().notifyAllCallback(videoUri);
        finish();
    }
}
