package any.xxx.anypeer.moudle.wallet;

import android.content.Context;
import android.os.Bundle;
import android.text.ClipboardManager;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import any.xxx.anypeer.R;
import any.xxx.anypeer.app.BaseActivity;
import any.xxx.anypeer.util.AnyWallet;

public class BackupActivity extends BaseActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_backup);

        initView();
    }

    private void initView() {
        final TextView tvBackup = findViewById(R.id.tv_backup);
        Bundle data = getIntent().getExtras();
        if (data != null) {
            String mnemonic = data.getString(AnyWallet.MNEMONIC);
            tvBackup.setText(mnemonic);
        }

        TextView title = findViewById(R.id.txt_title);
        title.setText(R.string.wallet_backupmnemonic);

        ImageView background = findViewById(R.id.img_back);
        background.setVisibility(View.VISIBLE);
        background.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        Button btCopy = findViewById(R.id.bt_copy);
        btCopy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                clipboard.setText(tvBackup.getText().toString().trim());
                Toast.makeText(BackupActivity.this, R.string.content_copyed, Toast.LENGTH_LONG).show();
            }
        });
    }
}
