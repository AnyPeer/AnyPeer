package any.xxx.anypeer.moudle.chat;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;

import any.xxx.anypeer.R;
import any.xxx.anypeer.bean.EMMessage;
import any.xxx.anypeer.util.NetUtils;

public class ContextMenuActivity extends FragmentActivity {
    private int position;
    private boolean isContactAddress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        int type = getIntent().getIntExtra("type", -1);

        if (type == EMMessage.Type.TXT.ordinal()) {
            setContentView(R.layout.context_menu_for_text);
        } else {
            setContentView(R.layout.context_menu_for_delete);
        }

        position = getIntent().getIntExtra("position", -1);
        boolean isRetry = getIntent().getBooleanExtra("isRetry", false);
        boolean isGroup = getIntent().getBooleanExtra("isGroup", false);

        if (!isRetry) {
            findViewById(R.id.tv_retry).setVisibility(View.GONE);
        }

        if (isGroup) {
            try {
                String value = getIntent().getStringExtra("message");
                if (value != null && !value.isEmpty()) {
                    if(NetUtils.isValidAddress(value)) {
                        isContactAddress = true;
                        TextView addFriendView = findViewById(R.id.tv_retry);
                        addFriendView.setVisibility(View.VISIBLE);
                        addFriendView.setText(R.string.menu_addfriend);
                    }
                }
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void retry(View view) {
        if (isContactAddress) {
            setResult(ChatActivity.RESULT_CODE_ADDFRIEND,
                    new Intent().putExtra("position", position));
        }
        else {
            setResult(ChatActivity.RESULT_CODE_RETRY,
                    new Intent().putExtra("position", position));
        }

        finish();
    }


    public void copy(View view) {
        setResult(ChatActivity.RESULT_CODE_COPY,
                new Intent().putExtra("position", position));
        finish();
    }

    public void delete(View view) {
        setResult(ChatActivity.RESULT_CODE_DELETE,
                new Intent().putExtra("position", position));
        finish();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        finish();
        return true;
    }
}
