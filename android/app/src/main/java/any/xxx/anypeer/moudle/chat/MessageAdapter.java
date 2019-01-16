package any.xxx.anypeer.moudle.chat;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import org.elastos.carrier.UserInfo;

import java.util.Date;
import java.util.Hashtable;
import java.util.Map;
import java.util.Timer;

import any.xxx.anypeer.R;
import any.xxx.anypeer.bean.EMConversation;
import any.xxx.anypeer.bean.EMMessage;
import any.xxx.anypeer.bean.User;
import any.xxx.anypeer.chatbean.ChatConversation;
import any.xxx.anypeer.chatbean.ChatMessage;
import any.xxx.anypeer.db.FriendManager;
import any.xxx.anypeer.moudle.common.PhotoActivity;
import any.xxx.anypeer.util.DateUtils;
import any.xxx.anypeer.util.NetUtils;

public class MessageAdapter extends BaseAdapter {
    private final static String TAG = "MessageAdapter";

    private LayoutInflater mInflater;
    private Activity mActivity;

    // reference to conversation object in chatsdk
    private ChatConversation mConversation;
    private User user;

    private Context mContext;
    private HeaderOnClick headerOnClick;

    MessageAdapter(Context context, ChatConversation conversation) {
        this.mContext = context;
        mInflater = LayoutInflater.from(context);
        mActivity = (Activity) context;
        this.mConversation = conversation;
        this.user = FriendManager.getInstance().getUserById(mConversation.getmUserId());
    }

    public int getCount() {
        if (mConversation != null && mConversation.getmMessages() != null) {
            return mConversation.getmMessages().size();
        }
        return 0;
    }

    /**
     * refresh
     */
    void refresh() {
        notifyDataSetChanged();
    }

    public ChatMessage getItem(int position) {
        return mConversation.getmMessages().get(position);
    }

    public long getItemId(int position) {
        return position;
    }

    public int getViewTypeCount() {
        return 16;
    }

    private View createViewByMessage(ChatMessage message, int position) {
        Log.d(TAG, position + " " + ChatMessage.Direct.getMsgDirect(message.getDirect()));

        if (ChatMessage.Direct.getMsgDirect(message.getDirect()) == ChatMessage.Direct.RECEIVE) {
            switch (ChatMessage.Type.getMsgType(message.getType())) {
                case IMAGE:
                    return mInflater.inflate(R.layout.row_received_pic, null);
                case VOICE:
                    return mInflater.inflate(R.layout.row_received_voice, null);
                case VIDEO:
                    return mInflater.inflate(R.layout.row_received_video, null);
                case MONEY:
                    return mInflater.inflate(R.layout.row_received_money, null);
                default:
                    return mInflater.inflate(R.layout.row_received_message, null);
            }
        } else {
            switch (ChatMessage.Type.getMsgType(message.getType())) {
                case IMAGE:
                    return mInflater.inflate(R.layout.row_sent_pic, null);
                case VOICE:
                    return mInflater.inflate(R.layout.row_sent_voice, null);
                case VIDEO:
                    return mInflater.inflate(R.layout.row_sent_video, null);
                case MONEY:
                    return mInflater.inflate(R.layout.row_sent_money, null);
                default:
                    return mInflater.inflate(R.layout.row_sent_message, null);
            }
        }
    }

    @SuppressLint("NewApi")
    public View getView(final int position, View convertView, ViewGroup parent) {
        final ChatMessage message = getItem(position);
        final ViewHolder holder = new ViewHolder();
        convertView = createViewByMessage(message, position);

        try {
            holder.iv = convertView.findViewById(R.id.iv_userhead);
            holder.tv = convertView.findViewById(R.id.tv_chatcontent);
            holder.tv_userId = convertView.findViewById(R.id.tv_userid);
            holder.iv_pic = convertView.findViewById(R.id.iv_pic);
            holder.iv_voice = convertView.findViewById(R.id.iv_voice);
            holder.tv_delivered = convertView.findViewById(R.id.tv_delivered);
            holder.tv_money = convertView.findViewById(R.id.tv_money);
        } catch (Exception e) {
            e.printStackTrace();
        }

        convertView.setTag(holder);

        switch (ChatMessage.Type.getMsgType(message.getType())) {
            case TXT:
                try {
                    holder.tv.setText(message.getMessage());
                    holder.tv.setOnLongClickListener(v -> {
                        ((Activity) mContext).startActivityForResult(
                                (new Intent(mContext, ContextMenuActivity.class)).putExtra("position", position)
                                        .putExtra("type", ChatMessage.Type.TXT.ordinal())
                                        .putExtra("isRetry", ChatMessage.Status.getMsgStatus(message.getStatus()) == ChatMessage.Status.FAIL),
                                ChatActivity.REQUEST_CODE_CONTEXT_MENU);
                        return true;
                    });
                } catch (Exception e) {

                }

                break;

            case MONEY:
                try {
                    switch (ChatMessage.Direct.getMsgDirect(message.getDirect())) {
                        case RECEIVE: {
                            String format = mContext.getString(R.string.chat_transfer_received);
                            String text = String.format(format, message.getMessage());
                            holder.tv_money.setText(text);
                            break;
                        }

                        case SEND: {
                            String format = mContext.getString(R.string.chat_transfer_send);
                            String text = String.format(format, message.getMessage());
                            holder.tv_money.setText(text);
                            break;
                        }
                    }

                    holder.tv.setOnLongClickListener(v -> {
                        ((Activity) mContext).startActivityForResult(
                                (new Intent(mContext, ContextMenuActivity.class)).putExtra("position", position)
                                        .putExtra("type", ChatMessage.Type.MONEY.ordinal())
                                        .putExtra("isRetry", ChatMessage.Status.getMsgStatus(message.getStatus()) == ChatMessage.Status.FAIL),
                                ChatActivity.REQUEST_CODE_CONTEXT_MENU);
                        return true;
                    });
                } catch (Exception e) {

                }

                break;

            case IMAGE:
                try {
                    Glide.with(mContext).load(message.getFilePath()).into(holder.iv_pic);

                    holder.iv_pic.setOnClickListener(v -> {
                        Intent intent = new Intent(mContext, PhotoActivity.class);
                        intent.putExtra(PhotoActivity.PIC, message.getFilePath());
                        mContext.startActivity(intent);
                    });

                    holder.iv_pic.setOnLongClickListener(v -> {
                        ((Activity) mContext).startActivityForResult(
                                (new Intent(mContext, ContextMenuActivity.class)).putExtra("position", position)
                                        .putExtra("type", ChatMessage.Type.IMAGE.ordinal())
                                        .putExtra("isRetry", ChatMessage.Status.getMsgStatus(message.getStatus()) == ChatMessage.Status.FAIL),
                                ChatActivity.REQUEST_CODE_CONTEXT_MENU);
                        return true;
                    });
                } catch (Exception e) {

                }

                break;

            case VOICE:
                try {
                    holder.iv_voice.setOnClickListener(new VoicePlayClickListener(message, holder.iv_voice, this, mActivity));
                    holder.iv_voice.setOnLongClickListener(v -> {
                        ((Activity) mContext).startActivityForResult(
                                (new Intent(mContext, ContextMenuActivity.class))
                                        .putExtra("position", position)
                                        .putExtra("type", ChatMessage.Type.VOICE.ordinal())
                                        .putExtra("isRetry", ChatMessage.Status.getMsgStatus(message.getStatus()) == ChatMessage.Status.FAIL),
                                ChatActivity.REQUEST_CODE_CONTEXT_MENU);
                        return true;
                    });
                } catch (Exception e) {

                }

                break;

            case VIDEO:
                try {
                    if (!TextUtils.isEmpty(message.getLocalThumb())) {
                        Glide.with(mContext).load(message.getLocalThumb()).into(holder.iv_pic);
                    }

                    holder.iv_pic.setOnClickListener(v -> {
                        Intent intent = new Intent(mContext, VideoActivity.class);
                        intent.putExtra(VideoActivity.PATH, message.getFilePath());
                        mContext.startActivity(intent);
                    });

                    holder.iv_pic.setOnLongClickListener(v -> {
                        ((Activity) mContext).startActivityForResult(
                                (new Intent(mContext, ContextMenuActivity.class))
                                        .putExtra("position", position)
                                        .putExtra("type", ChatMessage.Type.VIDEO.ordinal())
                                        .putExtra("isRetry", ChatMessage.Status.getMsgStatus(message.getStatus()) == ChatMessage.Status.FAIL),
                                ChatActivity.REQUEST_CODE_CONTEXT_MENU);
                        return true;
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
        }

        holder.iv.setImageResource(R.drawable.icon_default);
        holder.iv.setOnClickListener(view -> {
            if (headerOnClick != null) {
                headerOnClick.onClick(position);
            }
        });

        TextView timestamp = convertView.findViewById(R.id.timestamp);

        if (position == 0) {
            timestamp.setText(DateUtils.getTimestampString(mContext, new Date(message.getmMsgTime())));
            timestamp.setVisibility(View.VISIBLE);
        } else {
            // 两条消息时间离得如果稍长，显示时间
            if (DateUtils.isCloseEnough(message.getmMsgTime(), mConversation.getmMessages().get(position - 1).getmMsgTime())) {
                timestamp.setVisibility(View.GONE);
            } else {
                timestamp.setText(DateUtils.getTimestampString(mContext, new Date(message.getmMsgTime())));
                timestamp.setVisibility(View.VISIBLE);
            }
        }

        if (ChatMessage.Direct.getMsgDirect(message.getDirect()) == ChatMessage.Direct.SEND
                && ChatMessage.Status.getMsgStatus(message.getStatus()) != null) {
            Log.d(TAG, ChatMessage.Status.getMsgStatus(message.getStatus()) + "");
            switch (ChatMessage.Status.getMsgStatus(message.getStatus())) {
                case SUCCESS:
                    holder.tv_delivered.setText(mContext.getString(R.string.delivered));
                    break;

                case INPROGRESS:
                    holder.tv_delivered.setText(mContext.getString(R.string.delivering));
                    break;

                case FAIL:
                    holder.tv_delivered.setText(mContext.getString(R.string.undelivered));
                    break;
            }
        }

        if (ChatMessage.Direct.getMsgDirect(message.getDirect()) == ChatMessage.Direct.SEND) {
            UserInfo self = NetUtils.getInstance().getSelfInfo();
            if (!TextUtils.isEmpty(self.getGender())) {
                switch (self.getGender()) {
                    case "0":
                        Glide.with(mContext).load(R.drawable.icon_man_header).into(holder.iv);
                        break;
                    case "1":
                        Glide.with(mContext).load(R.drawable.icon_women_header).into(holder.iv);
                        break;
                    default:
                        Glide.with(mContext).load(R.drawable.icon_default).into(holder.iv);
                        break;
                }
            }
            else {
                Glide.with(mContext).load(R.drawable.icon_default).into(holder.iv);
            }
        }
        else {
            if (!TextUtils.isEmpty(user.getGender())) {
                switch (user.getGender()) {
                    case "0":
                        Glide.with(mContext).load(R.drawable.icon_man_header).into(holder.iv);
                        break;
                    case "1":
                        Glide.with(mContext).load(R.drawable.icon_women_header).into(holder.iv);
                        break;
                    default:
                        Glide.with(mContext).load(R.drawable.icon_default).into(holder.iv);
                        break;
                }
            } else {
                Glide.with(mContext).load(R.drawable.icon_default).into(holder.iv);
            }
        }

        return convertView;
    }


    public static class ViewHolder {
        ImageView iv;
        TextView tv;
        ProgressBar pb;
        ImageView staus_iv;
        ImageView head_iv;
        TextView tv_userId;
        ImageView playBtn;
        TextView timeLength;
        TextView size;
        LinearLayout container_status_btn;
        LinearLayout ll_container;
        ImageView iv_read_status;
        // 显示已读回执状态
        TextView tv_ack;
        // 显示送达回执状态
        TextView tv_delivered;

        TextView tv_file_name;
        TextView tv_file_size;
        TextView tv_file_download_state;
        ImageView iv_pic;
        ImageView iv_voice;
        TextView tv_money;
    }

    public interface HeaderOnClick {
        void onClick(int position);
    }

    void setHeaderOnClick(HeaderOnClick headerOnClick) {
        this.headerOnClick = headerOnClick;
    }
}