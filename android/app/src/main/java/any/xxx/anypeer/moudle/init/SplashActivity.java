package any.xxx.anypeer.moudle.init;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;

import com.google.gson.Gson;

import java.io.File;
import java.util.List;

import any.xxx.anypeer.R;
import any.xxx.anypeer.bean.DeprecatedUser;
import any.xxx.anypeer.bean.EMConversation;
import any.xxx.anypeer.bean.EMMessage;
import any.xxx.anypeer.bean.User;
import any.xxx.anypeer.manager.ChatManager;
import any.xxx.anypeer.moudle.main.MainActivity;
import any.xxx.anypeer.util.PrefereneceUtil;
import any.xxx.anypeer.util.Utils;

public class SplashActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        new Handler().postDelayed(() -> {
            boolean isInit = PrefereneceUtil.getBoolean(SplashActivity.this, InitActivity.INIT_USER);
            if (isInit) {
                startActivity(new Intent(SplashActivity.this, MainActivity.class));
            } else {
                startActivity(new Intent(SplashActivity.this, InitActivity.class));
            }

            finish();
        }, 1000);
    }
}
