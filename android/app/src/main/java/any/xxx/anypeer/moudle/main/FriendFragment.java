package any.xxx.anypeer.moudle.main;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.afollestad.materialdialogs.MaterialDialog;

import org.elastos.carrier.ConnectionStatus;
import org.elastos.carrier.FriendInfo;

import java.util.ArrayList;
import java.util.List;

import any.xxx.anypeer.R;
import any.xxx.anypeer.bean.User;
import any.xxx.anypeer.db.FriendManager;
import any.xxx.anypeer.moudle.friend.FriendDetailActivity;
import any.xxx.anypeer.util.AnyDBFriends;
import any.xxx.anypeer.util.NetUtils;
import any.xxx.anypeer.util.Utils;
import io.realm.Realm;

public class FriendFragment extends Fragment implements AdapterView.OnItemClickListener {
    private static final String TAG = "FriendFragment";
    private View mLayout;
//    private View mLlReboot;
    private ListView mContactListView;
    private ContactAdapter mContactAdapter;
    private MaterialDialog mMaterialDialog;
//    private ImageView ivClose;
    private ImageView mIvStatus;
    private TextView mTvStatus;
    private Button mBtnRetry;
    private TextView mTvRobotStatus;
    private boolean mRobotStatus = false;
    private AnyDBFriends mAnyDBFriends;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        if (mLayout == null) {
            final Activity context = this.getActivity();
            if (context == null) {
                return null;
            }

            mLayout = context.getLayoutInflater().inflate(R.layout.fragment_friends, null);
            mContactListView = mLayout.findViewById(R.id.listview);
            View mLlReboot = mLayout.findViewById(R.id.ll_reboot);
            mTvRobotStatus = mLlReboot.findViewById(R.id.tv_reboot_status);

            //Initialize the dialog.
            if (mMaterialDialog == null) {
                mMaterialDialog = new MaterialDialog.Builder(context).customView(R.layout.dialog_apply_test_money_layout, false).build();
            }

//            ivClose = (ImageView) mMaterialDialog.findViewById(R.id.iv_close);
            mIvStatus = (ImageView) mMaterialDialog.findViewById(R.id.iv_status);
            mTvStatus = (TextView) mMaterialDialog.findViewById(R.id.tv_status);
            mBtnRetry = (Button) mMaterialDialog.findViewById(R.id.btn_retry);
            mBtnRetry.setOnClickListener(view -> NetUtils.getInstance().sendMessage(NetUtils.ChatRobot.USERID, "ELA", null));

            Button btnSure = (Button) mMaterialDialog.findViewById(R.id.btn_sure);
            btnSure.setOnClickListener(view -> mMaterialDialog.dismiss());

            mLlReboot.setOnClickListener(v -> {
                if (mRobotStatus) {
                    NetUtils.getInstance().sendMessage(NetUtils.ChatRobot.USERID, "ELA", null);
                }
                else {
                    //TODO
                }
            });

            mAnyDBFriends = AnyDBFriends.getInstance(getActivity());
            initViews();
        } else {
            ViewGroup parent = (ViewGroup) mLayout.getParent();
            if (parent != null) {
                parent.removeView(mLayout);
            }
        }

        return mLayout;
    }

    void initRobotUserId(String name) {
        NetUtils.getInstance().initRobotUserId(name);
    }

    private static class ApplyResult {
        //"Sorry, invalid wallet address"
        private static final int Invalid_Address = 0;

        //"Sorry, you've already applied."
        private static final int Already_Applied = 1;

        //"Sorry, i spent my ELA"
        private static final int No_Ela = 2;

        //"Yes, you will get ELA"
        private static final int Apply_Success = 3;

        //"Sorry, please try again later"
        private static final int Try_Again = 4;
    }
    void applyResult(String message) {
        if (message == null || message.isEmpty()) {
            return;
        }

        try {
            int result = Integer.parseInt(message);
            String show = "";
            boolean success = false;
            switch (result) {
                case ApplyResult.Invalid_Address: {
                    show = getString(R.string.applyreslut_invalidaddress);
                    break;
                }
                case ApplyResult.Already_Applied: {
                    show = getString(R.string.applyreslut_applied);
                    break;
                }
                case ApplyResult.No_Ela: {
                    show = getString(R.string.applyreslut_noela);
                    break;
                }
                case ApplyResult.Apply_Success: {
                    show = getString(R.string.applyreslut_ela);
                    mBtnRetry.setVisibility(View.GONE);
                    success = true;
                    break;
                }
                case ApplyResult.Try_Again: {
                    show = getString(R.string.applyreslut_tryagain);
                    break;
                }
            }

            mTvStatus.setText(show);
            if (success) {
                mIvStatus.setBackgroundResource(R.drawable.icon_success);
            }
            else {
                mIvStatus.setBackgroundResource(R.drawable.icon_fail);
            }

            if (!mMaterialDialog.isShowing()) {
                mMaterialDialog.show();
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    void updateRobot(boolean status) {
        mRobotStatus = status;
        if (mRobotStatus) {
            mTvRobotStatus.setText(R.string.robot_online);
        }
        else {
            mTvRobotStatus.setText(R.string.robot_offline);
        }
    }

    boolean isRobot(String userId) {
        return NetUtils.getInstance().isRobot(userId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    public void addNewUser(FriendInfo friend) {
        if (mContactAdapter != null) {
            mContactAdapter.addNewUser(friend);
        }
    }

    public void removeUser(String userId) {
        if (mContactAdapter != null) {
            mContactAdapter.removeUser(userId);
        }

        FriendManager.getInstance().removeFriend(userId);
    }

    public void initContactList() {
        if (NetUtils.getInstance() != null) {
        	boolean isSpecialVersion = Utils.isSpecialVersion(getActivity());

            List<User> userInfos = new ArrayList<>();
            try {
                List<FriendInfo> friendInfos = NetUtils.getInstance().getFriends();
                String name = "";
                if (friendInfos != null) {
                    for (int i = 0; i < friendInfos.size(); i++) {
                        FriendInfo info = friendInfos.get(i);
                        name = info.getName();

                        // read only
                        User user = FriendManager.getInstance().getUserById(info.getUserId());

                        // 对老版本未存数据库的好友容错处理
                        if (user == null && isSpecialVersion) {
                            Log.d(TAG, "user null");

                            user = new User();
                            user.setUserId(info.getUserId());

                            if (name == null || name.isEmpty()) {
                            	name = mAnyDBFriends.getFriendName(info.getUserId());
                            }
                            user.setUserName(name);
                            FriendManager.getInstance().addFriend(user);

                            user = FriendManager.getInstance().getUserById(info.getUserId());
                        }

                        // 保存新名字
                        if (!TextUtils.isEmpty(name) && !name.equals(user.getUserName())){
                            FriendManager.getInstance().updateUserName(info.getUserId(), name);
                        }

                        // 保存性别
                        if (!TextUtils.isEmpty(info.getGender())) {
                            FriendManager.getInstance().updateUserGender(info.getUserId(), info.getGender());
                        }

                        // 保存在线状态
                        boolean isOnline = info.getConnectionStatus().value() == ConnectionStatus.Connected.value();
                        FriendManager.getInstance().updateUserIsOnline(info.getUserId(), isOnline);

                        User realUser = FriendManager.getInstance().getUserById(info.getUserId());
                        userInfos.add(realUser);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            finally {
                mContactAdapter = new ContactAdapter(getActivity(), userInfos);

                mContactListView.setAdapter(mContactAdapter);
                mContactListView.setOnItemClickListener(this);
            }
        }
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
        User user = mContactAdapter.getItem(position);
        Intent intent = new Intent(getActivity(), FriendDetailActivity.class);
        //Set title
        intent.putExtra(Utils.USERID, user.getUserId());
        startActivity(intent);
    }

    private void initViews() {
        mContactListView = mLayout.findViewById(R.id.lvContact);
        initContactList();
    }
}
