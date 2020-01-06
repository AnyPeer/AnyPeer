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
import android.widget.TextView;

import com.bumptech.glide.Glide;

import java.util.Date;
import java.util.List;

import any.xxx.anypeer.R;
import any.xxx.anypeer.bean.User;
import any.xxx.anypeer.chatbean.ChatConversation;
import any.xxx.anypeer.chatbean.ChatMessage;
import any.xxx.anypeer.db.FriendManager;
import any.xxx.anypeer.util.DateUtils;
import any.xxx.anypeer.util.NetUtils;
import any.xxx.anypeer.util.ViewHolder;
import jp.wasabeef.glide.transformations.GrayscaleTransformation;

import static com.bumptech.glide.request.RequestOptions.bitmapTransform;

public class NewMsgAdpter extends BaseAdapter {
    public static final String TAG = "NewMsgAdpter";
    private Context mContext;
    private List<ChatConversation> mConversationList;

    NewMsgAdpter(Context ctx, List<ChatConversation> objects) {
        mContext = ctx;
        mConversationList = objects;
    }

    @Override
    public int getCount() {
        return mConversationList.size();
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        final ChatConversation conversation = mConversationList.get(position);
        if (conversation == null) {
            return null;
        }

        if (convertView == null) {
            convertView = LayoutInflater.from(mContext).inflate(
                    R.layout.layout_item_msg, parent, false);
        }
        ImageView img_avar = ViewHolder.get(convertView, R.id.contactitem_avatar_iv);
        TextView txt_name = ViewHolder.get(convertView, R.id.txt_name);
        TextView txt_state = ViewHolder.get(convertView, R.id.txt_state);
        TextView txt_content = ViewHolder.get(convertView, R.id.txt_content);
        TextView txt_time = ViewHolder.get(convertView, R.id.txt_time);
        TextView unreadLabel = ViewHolder.get(convertView, R.id.unread_msg_number);

        if (!conversation.isGroup()) {
            User user = FriendManager.getInstance().getUserById(conversation.getmUserId());

            txt_name.setText(user.getUserName());

            if (user.isOnline()) {
                txt_state.setText(R.string.friend_online);
                txt_state.setTextColor(ContextCompat.getColor(mContext, R.color.colorPrimary));

                if (!TextUtils.isEmpty(user.getGender())) {
                    String gender = user.getGender();

                    switch (gender) {
                        case "0":
                            Glide.with(mContext).load(R.drawable.icon_man_header).into(img_avar);
                            break;
                        case "1":
                            Glide.with(mContext).load(R.drawable.icon_women_header).into(img_avar);
                            break;
                        default:
                            Glide.with(mContext).load(R.drawable.icon_default).into(img_avar);
                            break;
                    }
                } else {
                    Glide.with(mContext).load(R.drawable.icon_default).into(img_avar);
                }
            } else {
                txt_state.setText(R.string.friend_offline);
                txt_state.setTextColor(Color.parseColor("#C69978"));

                if (!TextUtils.isEmpty(user.getGender())) {
                    String gender = user.getGender();

                    switch (gender) {
                        case "0":
                            Glide.with(mContext).load(R.drawable.icon_man_header).apply(bitmapTransform(new GrayscaleTransformation())).into(img_avar);
                            break;
                        case "1":
                            Glide.with(mContext).load(R.drawable.icon_women_header).apply(bitmapTransform(new GrayscaleTransformation())).into(img_avar);
                            break;
                        default:
                            Glide.with(mContext).load(R.drawable.icon_default).apply(bitmapTransform(new GrayscaleTransformation())).into(img_avar);
                            break;
                    }
                } else {
                    Glide.with(mContext).load(R.drawable.icon_default).apply(bitmapTransform(new GrayscaleTransformation())).into(img_avar);
                }
            }
        } else {
            txt_name.setText(conversation.getGroupName());
            Glide.with(mContext).load(R.drawable.icon_group).into(img_avar);

            if (NetUtils.getInstance().groupIsOnline(conversation.getGroupName())) {
                txt_state.setText(R.string.friend_online);
                txt_state.setTextColor(ContextCompat.getColor(mContext, R.color.colorPrimary));
            }
            else {
                txt_state.setText(R.string.friend_offline);
                txt_state.setTextColor(Color.parseColor("#C69978"));
            }
        }

        if (conversation.getUnreadMsgCount() > 0) {
            // Show the unread message count.
            unreadLabel.setText(String.valueOf(conversation.getUnreadMsgCount()));
            unreadLabel.setVisibility(View.VISIBLE);
        } else {
            unreadLabel.setVisibility(View.INVISIBLE);
        }

        if (conversation.getmMessages().size() != 0) {
            // Get the content according to the last chat message.
            ChatMessage lastMessage = conversation.getLastMessage();

            txt_content.setText(getMessageDigest(lastMessage));
            txt_time.setText(DateUtils.getTimestampString(mContext, new Date(lastMessage.getmMsgTime())));
        }
        return convertView;
    }

    /**
     * Get the content according to the last chat message
     *
     * @param message chat message
     * @return the show message
     */
    private String getMessageDigest(ChatMessage message) {
        String digest = "";

        switch (ChatMessage.Type.getMsgType(message.getType())) {
            case TXT:
                digest = message.getMessage();
                break;

            case MONEY:
                switch (ChatMessage.Direct.getMsgDirect(message.getDirect())) {
                    case RECEIVE:
                        digest = mContext.getString(R.string.newmsg_recvtransfer);
                        break;

                    case SEND:
                        digest = mContext.getString(R.string.newmsg_transfer);
                        break;
                }
                break;

            case IMAGE:
                digest = mContext.getString(R.string.newmsg_image);
                break;

            case VOICE:
                digest = mContext.getString(R.string.newmsg_voice);
                break;

            case VIDEO:
                digest = mContext.getString(R.string.newmsg_video);
                break;
        }

        return digest;
    }
}
