package any.xxx.anypeer.moudle.main;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.UserManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import any.xxx.anypeer.bean.EMConversation;
import any.xxx.anypeer.bean.EMMessage;
import any.xxx.anypeer.bean.User;
import any.xxx.anypeer.chatbean.ChatConversation;
import any.xxx.anypeer.chatbean.ChatMessage;
import any.xxx.anypeer.db.ChatDBManager;
import any.xxx.anypeer.db.FriendManager;
import any.xxx.anypeer.manager.ChatManager;
import any.xxx.anypeer.moudle.chat.ChatActivity;
import any.xxx.anypeer.util.Utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class MessageFragment extends Fragment implements AdapterView.OnItemClickListener {
    private static final String TAG = "MessageFragment";
    private View mLayout;
    private ListView mContactListView;

    private NewMsgAdpter mAdpter;
    private List<ChatConversation> mConversationList = new ArrayList<>();
    private ChatManager mChatManager;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        if (mLayout == null) {
            Activity context = this.getActivity();
            if (context == null) return null;

	        mLayout = context.getLayoutInflater().inflate(any.xxx.anypeer.R.layout.fragment_message, null);
	        mContactListView = mLayout.findViewById(any.xxx.anypeer.R.id.listview);

            mChatManager = ChatManager.getInstance(context);
	        mChatManager.initChat(context);

            initChatsSendState();
            updateList();
        } else {
            ViewGroup parent = (ViewGroup) mLayout.getParent();
            if (parent != null) {
                parent.removeView(mLayout);
            }
        }
        return mLayout;
    }

    private void initChatsSendState() {
//        if (mChatManager != null) {
//            mChatManager.initChatsSendState();
//        }
        ChatDBManager.getInstance().initChatsSendState();
    }

    private void updateList() {
        updateList(null);
    }

    private void updateList(List<ChatConversation> list) {
        if (list == null) {
            list = loadConversationsWithRecentChat();
        }

        if (list != null) {
	        mConversationList.clear();
	        mConversationList.addAll(list);
	        mAdpter = new NewMsgAdpter(getActivity(), mConversationList);
	        mContactListView.setAdapter(mAdpter);
	        mContactListView.setOnItemClickListener(this);
        }
    }

    /**
     * get all EMConversations
     *
     */
    private List<ChatConversation> loadConversationsWithRecentChat() {
//        List<EMConversation> list = mChatManager.getChatList();
        List<ChatConversation> list = ChatDBManager.getInstance().getAllChatConversaion();
        //Sort
        sortConversationByLastChatTime(list);
        sortConversationByIsOnline(list);

        for (ChatConversation chatConversation : list) {
            Log.d(TAG, chatConversation.toString());
        }

        return list;
    }

    /**
     * sort by the time of last message.
     *
     */
    private void sortConversationByLastChatTime(List<ChatConversation> conversationList) {
        if (conversationList != null) {
            Collections.sort(conversationList, (con1, con2) -> {

                ChatMessage con2LastMessage = con2.getLastMessage();
                ChatMessage con1LastMessage = con1.getLastMessage();

                if (con1LastMessage == null || con2LastMessage == null) {
                    return 0;
                }

                if (con2LastMessage.getmMsgTime() == con1LastMessage.getmMsgTime()) {
                    return 0;
                } else if (con2LastMessage.getmMsgTime() > con1LastMessage.getmMsgTime()) {
                    return 1;
                } else {
                    return -1;
                }
            });
        }
    }

    private void sortConversationByIsOnline(List<ChatConversation> conversationList) {
        if (conversationList != null) {
            Collections.sort(conversationList, (con1, con2) -> {

                User user1 = FriendManager.getInstance().getUserById(con1.getmUserId());
                User user2 = FriendManager.getInstance().getUserById(con2.getmUserId());

                Log.d(TAG, user1.toString());
                Log.d(TAG, user2.toString());

                if (user1.isOnline()) {
                    return -1;
                } else if (user2.isOnline()) {
                    return 1;
                } else {
                    return 0;
                }
            });
        }
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
        ChatConversation conversation = mConversationList.get(position);
        Intent intent = new Intent(getActivity(), ChatActivity.class);
        //Set title
        User user = FriendManager.getInstance().getUserById(conversation.getmUserId());
        intent.putExtra(Utils.NAME, user.getUserName());
        intent.putExtra(Utils.USERID, user.getUserId());
        startActivity(intent);
    }

    public void onReflash() {
        updateList(loadConversationsWithRecentChat());
	    mAdpter.notifyDataSetChanged();
    }

    public boolean hasChatHistory(String userId) {
        return mChatManager.getChat(userId) != null;
    }

    public int getChatMessageCount(String userId) {
        EMConversation history = mChatManager.getChat(userId);
        if (history != null) {
            return history.getMsgCount();
        }
        return 0;
    }
}
