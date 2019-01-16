package any.xxx.anypeer.moudle.friend;

import android.content.Intent;
import android.os.Bundle;
import android.os.Message;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;

import any.xxx.anypeer.R;
import any.xxx.anypeer.app.BaseActivity;
import any.xxx.anypeer.bean.User;
import any.xxx.anypeer.db.FriendManager;
import any.xxx.anypeer.moudle.chat.ChatActivity;
import any.xxx.anypeer.moudle.chat.MessageService;
import any.xxx.anypeer.util.EventBus;
import any.xxx.anypeer.util.NetUtils;
import any.xxx.anypeer.util.Utils;
import any.xxx.anypeer.widget.supertext.SuperTextView;

public class FriendDetailActivity extends BaseActivity implements MessageService.IMessageCallback, EventBus.Callback {
    public static final String TYPE = "type";

    public static final int TYPE_ADD_FRIEND = 0;
    public static final int TYPE_SEE_FRIEND = 1;

    private TextView mTitle;
    private ImageView mBackground;
    private TextView tvname;

    private SuperTextView stvNickname;
    private SuperTextView stvEmail;
    private SuperTextView stvAddress;

    private ImageView mHead;
    private Button mRemoveButton;
    private Button mSendMessageButton;
    private String userId;
    private User user;
    private int type;
    private NetUtils mNetUtils = NetUtils.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friend_detail);
        initView();
    }

    protected void initView() {
        type = getIntent().getIntExtra(TYPE, TYPE_SEE_FRIEND);
        userId = (String) getIntent().getSerializableExtra(Utils.USERID);

        user = FriendManager.getInstance().getUserById(userId);

        mTitle = findViewById(R.id.txt_title);
        mBackground = findViewById(R.id.img_back);
        tvname = findViewById(R.id.tvname);

        mHead = findViewById(R.id.head);

        stvNickname = findViewById(R.id.stv_nickname);
        stvEmail = findViewById(R.id.stv_email);
        stvAddress = findViewById(R.id.stv_address);

        mRemoveButton = findViewById(R.id.bt_remove_firend);
        mSendMessageButton = findViewById(R.id.bt_send);

        tvname.setText(user.getUserName());
        mTitle.setText(R.string.friend_request_detail);

        mBackground.setVisibility(View.VISIBLE);
        mBackground.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        if (TextUtils.isEmpty(user.getUserName())) {
            stvNickname.setLeftString(getString(R.string.nickname));
        } else {
            stvNickname.setLeftString(getString(R.string.nickname) + ": " + user.getUserName());
        }

        if (TextUtils.isEmpty(user.getUserName())) {
            stvEmail.setLeftString(getString(R.string.email_title));
        } else {
            stvEmail.setLeftString(getString(R.string.email_title) + user.getEmail());
        }

        if (TextUtils.isEmpty(user.getUserName())) {
            stvAddress.setLeftString(getString(R.string.address_title));
        } else {
            stvAddress.setLeftString(getString(R.string.address_title) + user.getRegion());
        }


        if (!TextUtils.isEmpty(user.getGender())) {
            switch (user.getGender()) {
                case "0":
                    mHead.setImageResource(R.drawable.icon_man_header);
                    break;
                case "1":
                    mHead.setImageResource(R.drawable.icon_women_header);
                    break;
                default:
                    mHead.setImageResource(R.drawable.icon_default);
                    break;
            }
        } else {
            mHead.setImageResource(R.drawable.icon_default);
        }

        if (type == TYPE_ADD_FRIEND) {
            mRemoveButton.setVisibility(View.VISIBLE);
            mRemoveButton.setText(R.string.friend_request_reject);
            mSendMessageButton.setText(R.string.friend_request_agree);
            mTitle.setText(R.string.friend_request_apply);
        }
        else {
            mRemoveButton.setVisibility(View.GONE);
        }

        if (mNetUtils == null) {
            mNetUtils = NetUtils.getInstance();
        }

        mRemoveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (type == TYPE_ADD_FRIEND) {
                    //return directly.
                    finish();
                } else {
                    new MaterialDialog.Builder(FriendDetailActivity.this)
                            .title(R.string.friend_detail_deletefriend)
                            .content(getString(R.string.friend_detail_delete_ask) + user.getUserName())
                            .negativeText(R.string.friend_add_no)
                            .onNegative(new MaterialDialog.SingleButtonCallback() {
                                @Override
                                public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                    dialog.dismiss();
                                }
                            })
                            .positiveText(R.string.friend_add_yes)
                            .onPositive((dialog, which) -> {
                                NetUtils.getInstance().removeFriend(user.getUserId());
                                dialog.dismiss();
                            }).show();
                }
            }
        });

        mSendMessageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (type == TYPE_ADD_FRIEND) {
                    mNetUtils.accept(user.getUserId());
                    finish();
                } else {
                    Intent intent = new Intent(FriendDetailActivity.this, ChatActivity.class);
                    intent.putExtra(Utils.NAME, user.getUserName());
                    intent.putExtra(Utils.USERID, user.getUserId());
                    startActivity(intent);
                }
            }
        });

        EventBus.getInstance().addCallback(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getInstance().removeCallback(this);
    }

    @Override
    public void processMessage(Message message) {
        int what = message.what;
        switch (what) {
            case NetUtils.ANYSTATE.FRIENDREMOVED: {
                String id = (String) message.obj;
                if (id != null && id.equals(user.getUserId())) {
                    finish();
                }
                break;
            }
        }
    }

    @Override
    public void callback() {
        finish();
    }
}
