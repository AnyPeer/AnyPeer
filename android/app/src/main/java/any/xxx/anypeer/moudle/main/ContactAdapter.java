package any.xxx.anypeer.moudle.main;

import android.content.Context;
import android.graphics.Color;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.SectionIndexer;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import org.elastos.carrier.ConnectionStatus;
import org.elastos.carrier.FriendInfo;

import any.xxx.anypeer.R;
import any.xxx.anypeer.bean.User;
import any.xxx.anypeer.util.OnlineStatusComparator;
import any.xxx.anypeer.util.PingYinUtil;
import any.xxx.anypeer.util.PinyinComparator;
import any.xxx.anypeer.util.ViewHolder;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import jp.wasabeef.glide.transformations.GrayscaleTransformation;
import static com.bumptech.glide.request.RequestOptions.bitmapTransform;

public class ContactAdapter extends BaseAdapter implements SectionIndexer {
    private Context mContext;
    private List<User> mUserInfos = new ArrayList<>();
    private PinyinComparator mComparator = new PinyinComparator();
    private OnlineStatusComparator mOnlineStatusComparator = new OnlineStatusComparator();

    ContactAdapter(Context context, List<User> UserInfos) {
        mContext = context;
        if (UserInfos != null) {
	        mUserInfos.addAll(UserInfos);
	        Collections.sort(mUserInfos, mComparator);
            Collections.sort(mUserInfos, mOnlineStatusComparator);
        }
    }

	public void addUserList(List<User> userInfos) {
		mUserInfos.addAll(userInfos);
		Collections.sort(mUserInfos, mComparator);
        Collections.sort(mUserInfos, mOnlineStatusComparator);
	}

	public static User friendInfo2User(FriendInfo info) {
		User user = new User(info.getName(), info.getUserId());
		user.setEmail(info.getEmail());
		user.setGender(info.getGender());
		user.setRegion(info.getRegion());
        user.setOnline(info.getConnectionStatus().value() == ConnectionStatus.Connected.value());
		return user;
	}

	void addNewUser(FriendInfo friend) {
        mUserInfos.add(friendInfo2User(friend));
        Collections.sort(mUserInfos, mComparator);
    }

    void removeUser(String userId) {
    	for (int i = 0; i < mUserInfos.size(); i++) {
    		User user = mUserInfos.get(i);
    		if (user.getUserId().equals(userId)) {
    			mUserInfos.remove(user);
    			break;
		    }
	    }

        Collections.sort(mUserInfos, mComparator);
    }

    @Override
    public int getCount() {
        return mUserInfos.size();
    }

    @Override
    public User getItem(int position) {
        return mUserInfos.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        User user = mUserInfos.get(position);
        if (convertView == null) {
            convertView = LayoutInflater.from(mContext).inflate(R.layout.contact_item, null);
        }
        ImageView ivAvatar = ViewHolder.get(convertView,
                R.id.contactitem_avatar_iv);
        TextView tvNick = ViewHolder.get(convertView, R.id.contactitem_nick);
        TextView txt_state = ViewHolder.get(convertView, R.id.txt_state);

        Glide.with(mContext).load(R.drawable.icon_default).apply(bitmapTransform(new GrayscaleTransformation())).into(ivAvatar);
        tvNick.setText(user.getUserName());

        if (user.isOnline()) {
            if (!TextUtils.isEmpty(user.getGender())) {
                switch (user.getGender()) {
                    case "0":
                        Glide.with(mContext).load(R.drawable.icon_man_header).into(ivAvatar);
                        break;
                    case "1":
                        Glide.with(mContext).load(R.drawable.icon_women_header).into(ivAvatar);
                        break;
                    default:
                        Glide.with(mContext).load(R.drawable.icon_default).into(ivAvatar);
                        break;
                }
            } else {
                Glide.with(mContext).load(R.drawable.icon_default).into(ivAvatar);
            }

            txt_state.setText(R.string.friend_online);
            txt_state.setTextColor(ContextCompat.getColor(mContext, R.color.colorPrimary));
        } else {
            if (!TextUtils.isEmpty(user.getGender())) {
                switch (user.getGender()) {
                    case "0":
                        Glide.with(mContext).load(R.drawable.icon_man_header).apply(bitmapTransform(new GrayscaleTransformation())).into(ivAvatar);
                        break;
                    case "1":
                        Glide.with(mContext).load(R.drawable.icon_women_header).apply(bitmapTransform(new GrayscaleTransformation())).into(ivAvatar);
                        break;
                    default:
                        Glide.with(mContext).load(R.drawable.icon_default).apply(bitmapTransform(new GrayscaleTransformation())).into(ivAvatar);
                        break;
                }
            }
            txt_state.setText(R.string.friend_offline);
            txt_state.setTextColor(Color.parseColor("#C69978"));
        }

        return convertView;
    }

    @Override
    public int getPositionForSection(int section) {
        for (int i = 0; i < mUserInfos.size(); i++) {
            User user = mUserInfos.get(i);
            String l = PingYinUtil.converterToFirstSpell(user.getUserName())
                    .substring(0, 1);
            char firstChar = l.toUpperCase().charAt(0);
            if (firstChar == section) {
                return i;
            }
        }
        return 0;
    }

    @Override
    public int getSectionForPosition(int position) {
        return 0;
    }

    @Override
    public Object[] getSections() {
        return null;
    }
}
