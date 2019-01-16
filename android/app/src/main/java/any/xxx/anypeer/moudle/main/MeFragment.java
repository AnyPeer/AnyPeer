package any.xxx.anypeer.moudle.main;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.gson.Gson;

import any.xxx.anypeer.R;
import any.xxx.anypeer.bean.User;
import any.xxx.anypeer.moudle.barcode.BarCodeActivity;
import any.xxx.anypeer.moudle.mine.MineActivity;
import any.xxx.anypeer.moudle.mine.SettingActivity;
import any.xxx.anypeer.moudle.wallet.WalletActivity;
import any.xxx.anypeer.util.AnyWallet;
import any.xxx.anypeer.util.PrefereneceUtil;
import any.xxx.anypeer.util.Utils;

public class MeFragment extends Fragment {
    private View view;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        if (view == null) {
            Activity context = this.getActivity();
            if (context == null) {
                return null;
            }

            view = context.getLayoutInflater().inflate(R.layout.fragment_me, null);
            initView();
        } else {
            ViewGroup parent = (ViewGroup) view.getParent();
            if (parent != null) {
                parent.removeView(view);
            }
        }
        return view;
    }

    private void initView() {
        RelativeLayout viewUser = view.findViewById(R.id.view_user);
        ImageView head = view.findViewById(R.id.head);
        TextView tvname = view.findViewById(R.id.tvname);
        TextView tvDid = view.findViewById(R.id.tvdid);
        if (AnyWallet.getInstance() != null) {
            String did = AnyWallet.getInstance().getDID();
            tvDid.setText(did);
        }

        ImageView ivSex = view.findViewById(R.id.iv_sex);
        TextView txtMoney = view.findViewById(R.id.txt_money);
        TextView txtSetting = view.findViewById(R.id.txt_setting);
        ImageView ivQrcode = view.findViewById(R.id.iv_qrcode);

        User user = new Gson().fromJson(PrefereneceUtil.getString(getActivity(), User.USER), User.class);

        tvname.setText(user.getUserName());

        switch (user.getGender()) {
            case "0":
                head.setImageResource(R.drawable.icon_man_header);
                ivSex.setImageResource(R.drawable.ic_sex_male);
                ivSex.setVisibility(View.VISIBLE);
                break;

            case "1":
                head.setImageResource(R.drawable.icon_women_header);
                ivSex.setImageResource(R.drawable.ic_sex_female);
                ivSex.setVisibility(View.VISIBLE);
                break;

            case "2":
                head.setImageResource(R.drawable.icon_default);
                ivSex.setVisibility(View.GONE);
                break;
        }

        viewUser.setOnClickListener(view -> startActivityForResult(new Intent(getActivity(), MineActivity.class), 1000));
        txtMoney.setOnClickListener(view -> startActivity(new Intent(getActivity(), WalletActivity.class)));
        txtSetting.setOnClickListener(view -> startActivity(new Intent(getActivity(), SettingActivity.class)));
        ivQrcode.setOnClickListener(view -> {
            Intent intent = new Intent(getActivity(), BarCodeActivity.class);
            intent.putExtra(Utils.QRCODETYPE, Utils.QrCodeType.Carrier_Address.ordinal());
            intent.putExtra(Utils.ADDRESS, Utils.getQrcodeString(getActivity()));
            startActivity(intent);
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 1000 && resultCode == getActivity().RESULT_OK) {
            initView();
        }
    }
}
