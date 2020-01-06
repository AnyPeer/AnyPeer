package any.xxx.anypeer.moudle.mine;

import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v4.app.NotificationManagerCompat;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;

import any.xxx.anypeer.R;
import any.xxx.anypeer.app.BaseActivity;
import any.xxx.anypeer.manager.ChatManager;
import any.xxx.anypeer.moudle.chat.PayFragment;
import any.xxx.anypeer.moudle.init.InitActivity;
import any.xxx.anypeer.util.AnyWallet;
import any.xxx.anypeer.util.PrefereneceUtil;
import any.xxx.anypeer.util.Utils;
import any.xxx.anypeer.widget.PayPwdView;
import any.xxx.anypeer.widget.supertext.SuperTextView;

public class SettingActivity extends BaseActivity implements PayPwdView.InputCallBack {
    private PayFragment fragment = new PayFragment();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);

        findViewById(R.id.img_back).setVisibility(View.VISIBLE);
        findViewById(R.id.img_back).setOnClickListener(view -> finish());
        ((TextView) findViewById(R.id.txt_title)).setText(R.string.setting);
	    SuperTextView stvVersion = findViewById(R.id.tv_version);

        try {
            String version = "AnyPeer V" + getPackageManager().getPackageInfo(getPackageName(), 0).versionName;
            stvVersion.setLeftString(version);
        }
        catch (Exception e) {
            e.printStackTrace();
        }

	    SuperTextView stvIsPass = findViewById(R.id.stv_is_pass);
            SuperTextView stvRenewWallet = findViewById(R.id.stv_renew_wallet);
	    SuperTextView stvClearFriendData = findViewById(R.id.stv_clear_friend_data);
	    SuperTextView stvIsMessageShowNotification = findViewById(R.id.stv_is_message_show_notification);
        SuperTextView stvIsMessageVoice = findViewById(R.id.stv_is_message_voice);
	    TextView tvLogout = findViewById(R.id.tv_logout);

        stvIsPass.setSwitchIsChecked(PrefereneceUtil.getBoolean(SettingActivity.this, Utils.ADD_FRIEND_CHECK));
        stvIsPass.setSwitchCheckedChangeListener((buttonView, isChecked) -> PrefereneceUtil.saveBoolean(SettingActivity.this, Utils.ADD_FRIEND_CHECK, isChecked));

        stvIsMessageShowNotification.setSwitchIsChecked(ChatManager.getInstance(this).getIsMessageShowNotification(this));
        stvIsMessageShowNotification.setSwitchCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                checkNotification();
            }

            ChatManager.getInstance(this).saveIsMessageShowNotification(SettingActivity.this, isChecked);
        });

        stvIsMessageVoice.setSwitchIsChecked(ChatManager.getInstance(this).getIsMessageVoice(this));
        stvIsMessageVoice.setSwitchCheckedChangeListener((buttonView, isChecked) -> {
            ChatManager.getInstance(this).saveIsMessageShowVoice(SettingActivity.this, isChecked);
        });

        fragment.setPaySuccessCallBack(this);
	    stvRenewWallet.setOnClickListener(v -> {
            Bundle bundle = new Bundle();
            bundle.putString(PayFragment.EXTRA_CONTENT, getString(R.string.renew_wallet_warning));

            fragment.setArguments(bundle);
            fragment.show(getSupportFragmentManager(), getString(R.string.renew_wallet));
        });

        stvClearFriendData.setOnClickListener(v -> {
            //TODO
        });

        tvLogout.setOnClickListener(v -> {
            //TODO
        });
    }

    @Override
    public void onInputFinish(String password) {
        AnyWallet wallet = AnyWallet.getInstance();
        if (wallet == null) {
            return;
        }

        try {
        	if (!wallet.isValidWallet()) {
		        Utils.showShortToast(this, getString(R.string.wallet_has_exception));
		        fragment.dismiss();

		        //Must initialize the wallet at the InitActivity.
		        PrefereneceUtil.saveBoolean(SettingActivity.this, InitActivity.INIT_USER, false);
		        startActivity(new Intent(SettingActivity.this, InitActivity.class));
		        finish();
	        }
	        else {
		        String mnemonic = wallet.exportWalletWithMnemonic(password);
		        if (mnemonic != null && !mnemonic.isEmpty()) {
			        if (wallet.destroyWallet()) {
				        fragment.dismiss();

				        //Must initialize the wallet at the InitActivity.
				        PrefereneceUtil.saveBoolean(SettingActivity.this, InitActivity.INIT_USER, false);
				        startActivity(new Intent(SettingActivity.this, InitActivity.class));
				        finish();
			        }
			        else {
				        Utils.showShortToast(this, "Invalid password (WD).");
			        }
		        }
		        else {
			        Utils.showShortToast(this, "Invalid password.");
		        }
	        }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void checkNotification() {
        if (!NotificationManagerCompat.from(this).areNotificationsEnabled()) {
            MaterialDialog.Builder builder = new MaterialDialog.Builder(this)
                    .title(R.string.open_notification_dialog)
                    .positiveText(R.string.btn_sure)
                    .negativeText(R.string.btn_cancel);
            builder.onAny((dialog, which) -> {
                if (which == DialogAction.NEUTRAL) {
                } else if (which == DialogAction.POSITIVE) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP && Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
                        Intent intent = new Intent();
                        intent.setAction("android.settings.APP_NOTIFICATION_SETTINGS");
                        intent.putExtra("app_package", SettingActivity.this.getPackageName());
                        intent.putExtra("app_uid", SettingActivity.this.getApplicationInfo().uid);
                        startActivity(intent);
                    } else if (Build.VERSION.SDK_INT == Build.VERSION_CODES.KITKAT) {
                        Intent intent = new Intent();
                        intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                        intent.addCategory(Intent.CATEGORY_DEFAULT);
                        intent.setData(Uri.parse("package:" + SettingActivity.this.getPackageName()));
                        startActivity(intent);
                    } else {
                        Intent localIntent = new Intent();
                        localIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        localIntent.setAction("android.settings.APPLICATION_DETAILS_SETTINGS");
                        localIntent.setData(Uri.fromParts("package", SettingActivity.this.getPackageName(), null));
                        startActivity(localIntent);
                    }
                } else if (which == DialogAction.NEGATIVE) {
                }
            }).show();
        }
    }
}
