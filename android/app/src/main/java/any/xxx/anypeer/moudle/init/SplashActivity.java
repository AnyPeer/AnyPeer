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

        final boolean isSpecialVersion = Utils.isSpecialVersion(this);
        new Handler().postDelayed(() -> {
            if (isSpecialVersion) {
                try {
                    DeprecatedUser oldUser = new Gson().fromJson(PrefereneceUtil.getString(SplashActivity.this, DeprecatedUser.USER), DeprecatedUser.class);
                    if (oldUser != null && !TextUtils.isEmpty(oldUser.getUserName()) && !TextUtils.isEmpty(oldUser.getGender())) {
                        User user = new User();
                        user.setGender(oldUser.getGender());
                        user.setUserName(oldUser.getUserName());

                        PrefereneceUtil.saveString(SplashActivity.this, DeprecatedUser.USER, "");

                        String userJson = new Gson().toJson(user);
                        PrefereneceUtil.saveString(SplashActivity.this, User.USER, userJson);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

                try {
                    //Delete the history before the SpecialVersion.
                    ChatManager.getInstance(SplashActivity.this).initChat(SplashActivity.this);
                    List<EMConversation> emConversations = ChatManager.getInstance(SplashActivity.this).getChatList();
                    if (emConversations != null && !emConversations.isEmpty()) {
                        for (EMConversation emConversation : emConversations) {
                            List<EMMessage> emMessages = emConversation.getMessages();
                            if (emMessages != null && !emMessages.isEmpty()) {
                                for (EMMessage emMessage : emMessages) {
                                    switch (emMessage.type) {
                                        case VIDEO:
                                            if (!TextUtils.isEmpty(emMessage.getVideoMessageBody().getVideoPath())) {
                                                File deleteFile = new File(emMessage.getVideoMessageBody().getVideoPath());
                                                if (deleteFile.exists()) {
                                                    deleteFile.delete();
                                                }
                                            }
                                            if (!TextUtils.isEmpty(emMessage.getVideoMessageBody().getVideoPath())) {
                                                File deleteFile = new File(emMessage.getVideoMessageBody().getLocalThumb());
                                                if (deleteFile.exists()) {
                                                    deleteFile.delete();
                                                }
                                            }
                                            break;

                                        case VOICE:
                                            if (!TextUtils.isEmpty(emMessage.getVoiceMessageBody().getVoicePath())) {
                                                File deleteFile = new File(emMessage.getVoiceMessageBody().getVoicePath());
                                                if (deleteFile.exists()) {
                                                    deleteFile.delete();
                                                }
                                            }
                                            break;
                                    }
                                }
                            }
                        }

                        PrefereneceUtil.saveString(SplashActivity.this, Utils.CHAT_LIST, "");
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

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
