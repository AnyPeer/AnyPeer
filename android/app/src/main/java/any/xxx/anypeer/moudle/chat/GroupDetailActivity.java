package any.xxx.anypeer.moudle.chat;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

import com.afollestad.materialdialogs.MaterialDialog;
import org.elastos.carrier.Group;
import java.util.ArrayList;
import java.util.List;
import any.xxx.anypeer.R;
import any.xxx.anypeer.R.id;
import any.xxx.anypeer.db.ChatDBManager;
import any.xxx.anypeer.util.NetUtils;
import any.xxx.anypeer.widget.supertext.SuperTextView;

import static any.xxx.anypeer.moudle.chat.ChatActivity.RESULT_CODE_DELETE_GROUP_MESSAGE;

public class GroupDetailActivity extends AppCompatActivity {

    public static final String GROUP_ID = "group_id";

    private TextView mTitle;
    private ImageView mBackground;
    private GridView gv;
    private SuperTextView stvGroupName;
    private SuperTextView stvGroupAnnouncement;
    private TextView tvClear;
    private TextView tvDelete;
    private ItemGroupPeopleLayoutAdapter itemGroupPeopleLayoutAdapter;

    private String groupId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_detail);

        groupId = getIntent().getStringExtra(GROUP_ID);
        initViews();
    }

    private void initViews() {
        mTitle = findViewById(id.txt_title);
        mBackground = findViewById(id.img_back);
        gv = findViewById(R.id.gv);
        stvGroupName = findViewById(id.stv_group_name);
        stvGroupAnnouncement = findViewById(id.stv_group_announcement);
        tvClear = findViewById(id.tv_clear);
        tvDelete = findViewById(id.tv_delete);

        mTitle.setText(R.string.group_info);

        mBackground.setVisibility(View.VISIBLE);
        mBackground.setOnClickListener(view -> {
            finish();
        });

        tvClear.setOnClickListener(v -> {
            MaterialDialog.Builder builder = new MaterialDialog.Builder(this);
            builder.onNegative((dialog, which) -> dialog.dismiss());

            builder.content(R.string.group_clear_message);
            builder.negativeText(R.string.friend_add_no);
            builder.positiveText(R.string.friend_add_yes);

            builder.onPositive((dialog, which) -> {
                //TODO Delete the history
                dialog.dismiss();

                ChatDBManager.getInstance().removeAllMessage(groupId);
                setResult(RESULT_CODE_DELETE_GROUP_MESSAGE);
            }).show();
        });

        tvDelete.setOnClickListener(v -> {

        });

        gv.setNumColumns(5);

        List<String> names = new ArrayList<>();
        try {
            List<Group.PeerInfo> peers = NetUtils.getInstance().peersList(groupId);
            if (peers != null) {
                for (int i = 0; i < peers.size(); i++) {
                    names.add(peers.get(i).getName());
                }
            }
            stvGroupName.setLeftString(NetUtils.getInstance().getGroupTitle(groupId));
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        itemGroupPeopleLayoutAdapter = new ItemGroupPeopleLayoutAdapter(this, names);
        gv.setAdapter(itemGroupPeopleLayoutAdapter);
    }
}
