package any.xxx.anypeer.moudle.wallet;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import java.lang.ref.WeakReference;
import java.util.Locale;

import any.xxx.anypeer.R;
import any.xxx.anypeer.app.BaseActivity;
import any.xxx.anypeer.moudle.chat.PayFragment;
import any.xxx.anypeer.util.AnyWallet;
import any.xxx.anypeer.util.Utils;
import any.xxx.anypeer.widget.PayPwdView;

public class WalletActivity extends BaseActivity implements PayPwdView.InputCallBack {
    private static final String TAG = "WalletActivity";
    private ListView lv;

    private AnyWallet mAnyWallet = AnyWallet.getInstance();
    private PayFragment fragment = new PayFragment();
    private TextView tvWalletSync;
    private TextView tvBalance;
    private String mSyncText;
    private Handler mHandler;
    private ItemWalletLayoutAdapter mItemWalletLayoutAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wallet);

        initView();
    }

    private void initView() {
        TextView mTitle = findViewById(R.id.txt_title);
        ImageView mBackground = findViewById(R.id.img_back);
        tvBalance = findViewById(R.id.tv_account);
        tvWalletSync = findViewById(R.id.tv_wallet_sync);
        mSyncText = getString(R.string.wallet_sync);
        String text = mSyncText + " ...";
        tvWalletSync.setText(text);
        LinearLayout llBackup = findViewById(R.id.ll_backup);
        lv = findViewById(R.id.lv);

        mHandler = new InnerHandler(this);
        fragment.setPaySuccessCallBack(this);

        try {
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
			        Bundle bundle = new Bundle();
			        bundle.putString(PayFragment.EXTRA_CONTENT, getString(R.string.wallet_backupmnemonic));
			        fragment.setArguments(bundle);

			        fragment.show(getSupportFragmentManager(), "Pay");
		        }
	        });

	        if (mAnyWallet != null) {
		        long balance = mAnyWallet.getBalance();
		        tvBalance.setText(String.format(Locale.US, "%.4f", AnyWallet.TOELA(balance)));

		        //set message handler
		        mAnyWallet.setMessageHandler(mHandler);
	        }

	        mItemWalletLayoutAdapter = new ItemWalletLayoutAdapter(this);
	        lv.setAdapter(mItemWalletLayoutAdapter);
        }
        catch (Exception e) {
	        tvBalance.setText("0.0");
	        String warning = getString(R.string.renew_wallet_warning) + ", " + getString(R.string.renew_wallet);
			Utils.showLongToast(this, warning);
			e.printStackTrace();
        }
    }

	@Override
	protected void onDestroy() {
		super.onDestroy();

		try {
			if (mAnyWallet != null) {
				//set message handler
				mAnyWallet.setMessageHandler(null);
			}
		} catch (Exception e) {
			//
		}
	}

	private static class InnerHandler extends Handler {
    	private final WeakReference<WalletActivity> mActivity;
    	InnerHandler(WalletActivity activity) {
    		mActivity = new WeakReference<>(activity);
    	}

    	@Override
	    public void handleMessage(Message msg) {
		    WalletActivity activity = mActivity.get();
    		if (activity != null) {
			    switch (msg.what) {
				    case AnyWallet.WalletCallback.BlockSyncProgress: {
				    	Bundle data = msg.getData();
				    	if (data != null) {
						    int currentBlockHeight = data.getInt(AnyWallet.WalletCallback.SYNCCURRENTHEIGHT);
						    int estimatedHeight = data.getInt(AnyWallet.WalletCallback.SYNCESTIMATEHEIGHT);
						    activity.updateBlock(currentBlockHeight, estimatedHeight);
					    }

					    break;
				    }
			    }

			    activity.updateTheList();
		    }
    	}
    }

    private void updateTheList() {
    	try {
		    if (mAnyWallet != null) {
			    long balance = mAnyWallet.getBalance();
			    tvBalance.setText(String.format(Locale.US, "%.4f", AnyWallet.TOELA(balance)));
		    }

		    mItemWalletLayoutAdapter.updateList();
	    }
	    catch (Exception e) {
    		e.printStackTrace();
	    }
    }

    private void updateBlock(int currentBlockHeight, int estimatedHeight) {
        String text = mSyncText + " ( " + Integer.toString(currentBlockHeight) + "/" + Integer.toString(estimatedHeight) + " )";
        tvWalletSync.setText(text);
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
