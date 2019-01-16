package any.xxx.anypeer.moudle.main;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.google.gson.Gson;

import any.xxx.anypeer.R;
import any.xxx.anypeer.bean.User;
import any.xxx.anypeer.util.NetUtils;
import any.xxx.anypeer.util.PrefereneceUtil;
import any.xxx.anypeer.util.Utils;

public class AddFriendActivity extends AppCompatActivity implements TextWatcher, View.OnClickListener {

    private EditText etAddress;
    private Button btAddFriend;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_friend);

        findViewById(R.id.img_back).setVisibility(View.VISIBLE);
        findViewById(R.id.img_back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
        ((TextView) findViewById(R.id.txt_title)).setText(R.string.friend_add_text1);

        etAddress = findViewById(R.id.et_address);
        btAddFriend = findViewById(R.id.bt_add_friend);

        etAddress.addTextChangedListener(this);
        btAddFriend.setOnClickListener(this);
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {

    }

    @Override
    public void afterTextChanged(Editable s) {
        if (etAddress.getText().toString().trim().isEmpty()) {
            btAddFriend.setEnabled(false);
        } else {
            btAddFriend.setEnabled(true);
        }
    }

    @Override
    public void onClick(View v) {
        final String address = etAddress.getText().toString().trim();
        NetUtils netUtils = NetUtils.getInstance();
        if (netUtils != null) {
            if (NetUtils.isValidAddress(address)) {
                User user = new Gson().fromJson(PrefereneceUtil.getString(AddFriendActivity.this
                        , User.USER), User.class);
                String name = "Hello";
                if (user != null) {
                    name = user.getUserName();
                }
                netUtils.addFriend(address, name);
            }
            else {
                Utils.showShortToast(AddFriendActivity.this, getString(R.string.invalid_friend_adress));
            }
        }
        finish();
    }
}
