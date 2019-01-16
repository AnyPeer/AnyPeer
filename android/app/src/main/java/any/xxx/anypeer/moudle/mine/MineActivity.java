package any.xxx.anypeer.moudle.mine;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.afollestad.materialdialogs.MaterialDialog;
import com.google.gson.Gson;

import org.elastos.carrier.UserInfo;

import any.xxx.anypeer.R;
import any.xxx.anypeer.app.BaseActivity;
import any.xxx.anypeer.bean.User;
import any.xxx.anypeer.moudle.barcode.BarCodeActivity;
import any.xxx.anypeer.util.NetUtils;
import any.xxx.anypeer.util.PrefereneceUtil;
import any.xxx.anypeer.util.Utils;
import any.xxx.anypeer.widget.supertext.SuperTextView;

public class MineActivity extends BaseActivity {

    private SuperTextView stvName;
    private SuperTextView stvEmail;
    private SuperTextView stvSex;

    private User user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mine);

        findViewById(R.id.img_back).setVisibility(View.VISIBLE);
        findViewById(R.id.img_back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
        ((TextView) findViewById(R.id.txt_title)).setText(R.string.mine);

        stvName = findViewById(R.id.stv_name);
        stvSex = findViewById(R.id.stv_sex);
        stvEmail = findViewById(R.id.stv_email);
        SuperTextView stvQrcode = findViewById(R.id.stv_qrcode);

        final UserInfo self = NetUtils.getInstance().getSelfInfo();
        user = new Gson().fromJson(PrefereneceUtil.getString(this, User.USER), User.class);
        stvName.setRightString(user.getUserName());
        stvEmail.setRightString(user.getEmail());

        switch (user.getGender()) {
            case "0":
                stvSex.setRightString(getString(R.string.tr_radio_op1));
                break;

            case "1":
                stvSex.setRightString(getString(R.string.tr_radio_op2));
                break;

            case "2":
                stvSex.setRightString(getString(R.string.tr_radio_op3));
                break;
        }

        stvSex.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new MaterialDialog.Builder(MineActivity.this).itemsCallbackSingleChoice(Integer.valueOf(user.getGender()), new MaterialDialog.ListCallbackSingleChoice() {
                    @Override
                    public boolean onSelection(MaterialDialog dialog, View itemView, int which, CharSequence text) {

                        user.setGender(which + "");

                        String userJson = new Gson().toJson(user);
                        PrefereneceUtil.saveString(MineActivity.this, User.USER, userJson);
                        switch (which + "") {
                            case "0":
                                stvSex.setRightString(getString(R.string.tr_radio_op1));
                                break;

                            case "1":
                                stvSex.setRightString(getString(R.string.tr_radio_op2));
                                break;

                            case "2":
                                stvSex.setRightString(getString(R.string.tr_radio_op3));
                                break;
                        }

                        self.setGender(which + "");
                        NetUtils.getInstance().setSelfInfo(self);

                        setResult(RESULT_OK);
                        return false;
                    }
                }).items(R.array.sex).show();
            }
        });

        stvName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new MaterialDialog.Builder(MineActivity.this).input(user.getUserName(), "", new MaterialDialog.InputCallback() {
                    @Override
                    public void onInput(@NonNull MaterialDialog dialog, CharSequence input) {
                        if (input == null || input.length() == 0) {
                            return;
                        }

                        user.setUserName(String.valueOf(input));

                        String userJson = new Gson().toJson(user);
                        PrefereneceUtil.saveString(MineActivity.this, User.USER, userJson);
                        stvName.setRightString(input);

                        self.setName(String.valueOf(input));
                        NetUtils.getInstance().setSelfInfo(self);

                        setResult(RESULT_OK);
                    }
                }).show();
            }
        });

        stvEmail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new MaterialDialog.Builder(MineActivity.this).input(user.getEmail(), "", new MaterialDialog.InputCallback() {
                    @Override
                    public void onInput(@NonNull MaterialDialog dialog, CharSequence input) {
                        if (input == null || input.length() == 0) {
                            return;
                        }

                        user.setEmail(String.valueOf(input));

                        String userJson = new Gson().toJson(user);
                        PrefereneceUtil.saveString(MineActivity.this, User.USER, userJson);
                        stvEmail.setRightString(input);

                        self.setEmail(String.valueOf(input));
                        NetUtils.getInstance().setSelfInfo(self);

                        setResult(RESULT_OK);
                    }
                }).show();
            }
        });

        stvQrcode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MineActivity.this, BarCodeActivity.class);
                intent.putExtra(Utils.QRCODETYPE, Utils.QrCodeType.Carrier_Address.ordinal());
                intent.putExtra(Utils.ADDRESS, Utils.getQrcodeString(MineActivity.this));
                startActivity(intent);
            }
        });
    }
}
