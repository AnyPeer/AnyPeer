package any.xxx.anypeer.moudle.chat;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import any.xxx.anypeer.R;
import any.xxx.anypeer.app.BaseActivity;
import any.xxx.anypeer.util.AnyWallet;
import any.xxx.anypeer.util.Utils;
import any.xxx.anypeer.widget.PayPwdView;

public class TransferAccountsActivity extends BaseActivity implements TextWatcher, PayPwdView.InputCallBack {
    private static final String TAG = "TAActivity";
    public static final String NAME = "name";
    public static final String GENDER = "gender";
    public static final String ADDRESS = "address";
    public static final String MONEY = "money";

    private ImageView mHeader;
    private TextView mTitle;
    private ImageView mBackground;
    private TextView tvName;
    private ImageView ivClear;
    private Button btSure;
    private EditText etAsset;
    private EditText etElaAddress;

    private PayFragment fragment = new PayFragment();

    private String name, gender, friendWalletAddress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_transfer_accounts);

        init();
    }

    private void init() {
        friendWalletAddress = getIntent().getStringExtra(ADDRESS);
        name = getIntent().getStringExtra(NAME);
        gender = getIntent().getStringExtra(GENDER);

        //Transfer to using a friend's ELA address.
        if (name.isEmpty()) {
            name = friendWalletAddress;
        }

        //Transfer to using ELA address.
        if (name.isEmpty()) {
            findViewById(R.id.rl_transferto).setVisibility(View.VISIBLE);
        }

        mHeader = findViewById(R.id.iv_header);
        mTitle = findViewById(R.id.txt_title);
        mBackground = findViewById(R.id.img_back);
        tvName = findViewById(R.id.tv_name);
        ivClear = findViewById(R.id.iv_clear);
        btSure = findViewById(R.id.bt_sure);
        etAsset = findViewById(R.id.et_money);
        etElaAddress = findViewById(R.id.et_elaaddress);
        TextView transferBalance = findViewById(R.id.transfer_balance);
        String infoFormat = getResources().getString(R.string.transfer_amount_balance);
        if (AnyWallet.getInstance() != null) {
            transferBalance.setText(String.format(infoFormat, AnyWallet.getInstance().getBalanceString()));
        }
        else {
            transferBalance.setText(String.format(infoFormat, "0"));
        }

        if (!TextUtils.isEmpty(gender)) {
            switch (gender) {
                case "0":
                    Glide.with(this).load(R.drawable.icon_man_header).into(mHeader);
                    break;
                case "1":
                    Glide.with(this).load(R.drawable.icon_women_header).into(mHeader);
                    break;
                default:
                    Glide.with(this).load(R.drawable.icon_default).into(mHeader);
                    break;
            }
        } else {
            Glide.with(this).load(R.drawable.icon_default).into(mHeader);
        }

        mTitle.setText(R.string.transfer_text);
        tvName.setText(name);
        mBackground.setVisibility(View.VISIBLE);
        mBackground.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        etAsset.addTextChangedListener(this);
        ivClear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                etAsset.setText("");
            }
        });

        fragment.setPaySuccessCallBack(this);

        btSure.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
            	if (!isValidInput(etAsset.getText().toString().trim())) {
		            Utils.showShortToast(TransferAccountsActivity.this, getString(R.string.transfer_amount_least));
		            return;
	            }

                AnyWallet wallet = AnyWallet.getInstance();
                String elaAddress = etElaAddress.getText().toString().trim();
                if (name.isEmpty()) {
                    if (wallet != null && wallet.isAddressValid(elaAddress)) {
                        name = elaAddress;
                    }
                    else {
                        Utils.showShortToast(TransferAccountsActivity.this, getString(R.string.transfer_friend_address_invalid));
                        return;
                    }
                }

                if (friendWalletAddress == null || friendWalletAddress.isEmpty()) {
                    friendWalletAddress = elaAddress;
                }
                Bundle bundle = new Bundle();
                bundle.putString(PayFragment.EXTRA_TITLE, getString(R.string.transfer_to) + name);
                bundle.putString(PayFragment.EXTRA_CONTENT, etAsset.getText().toString().trim() + "  " + AnyWallet.ELA);

                fragment.setArguments(bundle);
                fragment.show(getSupportFragmentManager(), "Pay");
            }
        });
    }

    private static boolean isValidInput(String input) {
    	try {
		    final String zero = "0";
		    return Double.doubleToLongBits(Double.parseDouble(input)) > Double.doubleToLongBits(Double.parseDouble(zero));
	    }
    	catch (Exception e){
    		e.printStackTrace();
	    }

    	return false;
    }

    @Override
    public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
    }

    @Override
    public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
    }

    @Override
    public void afterTextChanged(Editable editable) {
        if (TextUtils.isEmpty(etAsset.getText().toString().trim())) {
            ivClear.setVisibility(View.GONE);
            btSure.setEnabled(false);
        } else {
            ivClear.setVisibility(View.VISIBLE);
            btSure.setEnabled(true);
        }
        limitInput(editable, etAsset);
    }

    public static void limitInput(Editable edt,EditText editText) {
        final int BEFORE = 4;
        final int AFTER = 4;
        String temp = edt.toString();
        int posDot = temp.indexOf(".");
        int index = editText.getSelectionStart();
        if (posDot < 0) {
            if (temp.length() <= BEFORE) {
                return;
            }
            else {
                edt.delete(index-1, index);
                return;
            }
        }
        if (posDot > BEFORE) {
            edt.delete(index-1, index);
            return;
        }

        if (temp.length() - posDot - 1 > AFTER) {
            edt.delete(index-1, index);
        }
    }

    @Override
    public void onInputFinish(String password) {
        // TODO : check password
        AnyWallet wallet = AnyWallet.getInstance();
        if (wallet == null) {
            Utils.showShortToast(this, "wallet is NULL");
            return;
        }

        long balance = wallet.getBalance();
        if (friendWalletAddress == null || friendWalletAddress.isEmpty()) {
            Utils.showShortToast(this, getString(R.string.transfer_friend_address_invalid));
        } else {
            double damount = Double.parseDouble(etAsset.getText().toString().trim());
            long amount = Math.round(damount * AnyWallet.BASE_TRANSFER);

            if (balance > amount) {
                String resultJson = wallet.transfer(friendWalletAddress, amount, "666", password);
                if (resultJson != null) {
                    Intent intent = new Intent();
                    intent.putExtra(MONEY, etAsset.getText().toString().trim());
                    setResult(RESULT_OK, intent);

                    try {
                        //Generate the did
                        wallet.generateDID(password);
                    }
                    catch (Exception e) {
                        e.printStackTrace();
                    }

                    finish();
                } else {
                    Utils.showLongToast(this, "Transfer has error");
                }
            }
        }
    }
}
