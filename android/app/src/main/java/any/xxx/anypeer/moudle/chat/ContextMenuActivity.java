package any.xxx.anypeer.moudle.chat;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.MotionEvent;
import android.view.View;

import any.xxx.anypeer.R;
import any.xxx.anypeer.bean.EMMessage;

public class ContextMenuActivity extends FragmentActivity {
    private int position;
    private boolean isRetry;

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
        isRetry = getIntent().getBooleanExtra("isRetry", false);

        if (!isRetry) {
            findViewById(R.id.tv_retry).setVisibility(View.GONE);
        }
    }

    public void retry(View view) {
        setResult(ChatActivity.RESULT_CODE_RETRY,
                new Intent().putExtra("position", position));
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
