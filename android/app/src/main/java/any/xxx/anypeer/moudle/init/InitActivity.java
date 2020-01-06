package any.xxx.anypeer.moudle.init;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.TextInputEditText;
import android.support.v4.widget.NestedScrollView;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;

import any.xxx.anypeer.R;
import any.xxx.anypeer.app.BaseActivity;
import any.xxx.anypeer.bean.User;
import any.xxx.anypeer.moudle.main.MainActivity;
import any.xxx.anypeer.util.AnyWallet;
import any.xxx.anypeer.util.KeyboardPatch;
import any.xxx.anypeer.util.PrefereneceUtil;

public class InitActivity extends BaseActivity implements TextWatcher, View.OnTouchListener, View.OnFocusChangeListener {
    public static final String INIT_USER = "init_user";
    private TextInputEditText etName;
    private RadioGroup rg;
    private TextInputEditText email;
    private TextInputEditText password, passwordAgain;
    private Button btComplite;
    private ImageView ivHeader;
    private NestedScrollView sv;

    private KeyboardPatch keyboardPatch;
    private static final int MIN_PASSWORDLEN = 6;
    public static boolean LOGIN_CHANAGED = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_init);

        etName = findViewById(R.id.et_name);
        User user = null;
        try {
            user = new Gson().fromJson(PrefereneceUtil.getString(this, User.USER), User.class);
            if (user != null) {
                String name = user.getUserName();
                if (name != null && !name.isEmpty()) {
                    etName.setText(name);
                }
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }

        findViewById(R.id.img_back).setVisibility(View.VISIBLE);
        findViewById(R.id.img_back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
        ((TextView) findViewById(R.id.txt_title)).setText(R.string.complite_user_info);

        keyboardPatch = new KeyboardPatch(this, findViewById(R.id.content));
        keyboardPatch.enable();

        rg = findViewById(R.id.rg);
        email = findViewById(R.id.email);
        password = findViewById(R.id.password);
        btComplite = findViewById(R.id.bt_complite);
        ivHeader = findViewById(R.id.iv_header);
        passwordAgain = findViewById(R.id.password_again);
        sv = findViewById(R.id.sv);

        etName.addTextChangedListener(this);
        password.addTextChangedListener(this);
        passwordAgain.addTextChangedListener(this);
        btComplite = findViewById(R.id.bt_complite);

        etName.addTextChangedListener(this);

        rg.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                switch (checkedId) {
                    case R.id.rb1:
                        ivHeader.setImageResource(R.drawable.icon_man_header);
                        break;
                    case R.id.rb2:
                        ivHeader.setImageResource(R.drawable.icon_women_header);
                        break;
                    case R.id.rb3:
                        ivHeader.setImageResource(R.drawable.icon_default);
                        break;
                }
            }
        });

        btComplite.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    if (!password.getText().toString().trim().equals(passwordAgain.getText().toString().trim())) {
                        Toast.makeText(InitActivity.this, R.string.Two_input_password, Toast.LENGTH_LONG).show();
                        return;
                    }

                    PrefereneceUtil.saveBoolean(InitActivity.this, INIT_USER, true);

                    User user = new User();
                    user.setUserName(etName.getText().toString().trim());

                    String sex = "2";
                    switch (rg.getCheckedRadioButtonId()) {
                        case R.id.rb1:
                            sex = "0";
                            break;

                        case R.id.rb2:
                            sex = "1";
                            break;

                        case R.id.rb3:
                            sex = "2";
                            break;
                    }

                    user.setGender(sex);
                    user.setEmail(email.getText().toString().trim());

                    String userJson = new Gson().toJson(user);
                    PrefereneceUtil.saveString(InitActivity.this, User.USER, userJson);

                    String passwordString = password.getText().toString().trim();

                    AnyWallet anyWallet = AnyWallet.getInstance(InitActivity.this);
                    if (!anyWallet.login(passwordString)) {
                        //try again
                        anyWallet.login(passwordString);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

                LOGIN_CHANAGED = true;
                startActivity(new Intent(InitActivity.this, MainActivity.class));
                finish();
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (keyboardPatch != null) {
            keyboardPatch.disable();
        }
    }

    @Override
    public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

    }

    @Override
    public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

    }

    @Override
    public void afterTextChanged(Editable editable) {
        if (etName.getText().toString().trim().isEmpty()
                || password.getText().toString().trim().length() < MIN_PASSWORDLEN
                || passwordAgain.getText().toString().trim().length() < MIN_PASSWORDLEN) {
            btComplite.setEnabled(false);
        } else {
            btComplite.setEnabled(true);
        }
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                sv.fullScroll(NestedScrollView.FOCUS_DOWN);
            }
        }, 300);

        return false;
    }

    @Override
    public void onFocusChange(final View v, boolean hasFocus) {
        if (hasFocus) {
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    v.requestFocus();
                }
            }, 300);
        }
    }
}
