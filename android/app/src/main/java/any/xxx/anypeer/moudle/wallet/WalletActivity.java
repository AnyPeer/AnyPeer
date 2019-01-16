package any.xxx.anypeer.moudle.wallet;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import java.util.Locale;

import any.xxx.anypeer.R;
import any.xxx.anypeer.app.BaseActivity;
import any.xxx.anypeer.moudle.chat.PayFragment;
import any.xxx.anypeer.util.AnyWallet;
import any.xxx.anypeer.util.Utils;
import any.xxx.anypeer.widget.PayPwdView;

public class WalletActivity extends BaseActivity implements PayPwdView.InputCallBack {
    private static final String TAG = "WalletActivity";
    private LinearLayout llBackup;
    private ListView lv;

    private AnyWallet mAnyWallet = AnyWallet.getInstance();
    private PayFragment fragment = new PayFragment();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wallet);

        initView();
    }

    private void initView() {
        TextView mTitle = findViewById(R.id.txt_title);
        ImageView mBackground = findViewById(R.id.img_back);
        TextView tvBalance = findViewById(R.id.tv_account);
        llBackup = findViewById(R.id.ll_backup);
        lv = findViewById(R.id.lv);

        fragment.setPaySuccessCallBack(this);

        if (mAnyWallet != null) {
            long balance = mAnyWallet.getBalance();
            tvBalance.setText(String.format(Locale.US, "%.4f", AnyWallet.TOELA(balance)));
        }

        mTitle.setText(R.string.wallet);

        mBackground.setVisibility(View.VISIBLE);
        mBackground.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        llBackup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                fragment.show(getSupportFragmentManager(), "Pay");
            }
        });

        ItemWalletLayoutAdapter itemWalletLayoutAdapter = new ItemWalletLayoutAdapter(this);
        lv.setAdapter(itemWalletLayoutAdapter);
    }

    @Override
    public void onInputFinish(String password) {
        AnyWallet wallet = AnyWallet.getInstance();
        if (wallet == null) {
            return;
        }

        String mnemonic = wallet.exportWalletWithMnemonic(password);
        if (mnemonic != null && !mnemonic.isEmpty()) {
            try {
                //Generate the did
                wallet.generateDID(password);
            }
            catch (Exception e) {
                e.printStackTrace();
            }

            fragment.dismiss();
            Intent intent = new Intent(WalletActivity.this, BackupActivity.class);
            Bundle data = new Bundle();
            data.putString(AnyWallet.MNEMONIC, mnemonic);
            intent.putExtras(data);
            startActivity(intent);
        }
        else {
            Utils.showShortToast(this, "Invalid password.");
        }
    }
}
