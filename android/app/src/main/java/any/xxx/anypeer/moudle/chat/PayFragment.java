package any.xxx.anypeer.moudle.chat;

import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import any.xxx.anypeer.R;
import any.xxx.anypeer.widget.PayPwdView;
import any.xxx.anypeer.widget.PwdInputMethodView;

public class PayFragment extends DialogFragment implements View.OnClickListener {

    public static final String EXTRA_TITLE = "extra_title";
    public static final String EXTRA_CONTENT = "extra_content";

    private PayPwdView psw_input;
    private View llPassword;
    private PayPwdView.InputCallBack inputCallBack;

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Dialog dialog = new Dialog(getActivity(), R.style.BottomDialog);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.fragment_pay);
        dialog.setCanceledOnTouchOutside(false);

        final Window window = dialog.getWindow();
        window.setWindowAnimations(R.style.AnimBottom);
        window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        final WindowManager.LayoutParams lp = window.getAttributes();
        lp.width = WindowManager.LayoutParams.MATCH_PARENT;
        lp.gravity = Gravity.TOP;
        window.setAttributes(lp);

        initView(dialog);
        return dialog;
    }

    private void initView(Dialog dialog) {
        Bundle bundle = getArguments();
        if (bundle != null) {
            TextView tv_content = dialog.findViewById(R.id.tv_content);

            if (!TextUtils.isEmpty(bundle.getString(EXTRA_CONTENT))) {
                tv_content.setText(bundle.getString(EXTRA_CONTENT));
                tv_content.setTextColor(Color.RED);
            } else {
                tv_content.setVisibility(View.GONE);
            }

            TextView tv_title = dialog.findViewById(R.id.tv_title);

            if (!TextUtils.isEmpty(bundle.getString(EXTRA_TITLE))) {
                tv_title.setText(bundle.getString(EXTRA_TITLE));
            } else {
                tv_title.setVisibility(View.GONE);
            }
        }

        psw_input = dialog.findViewById(R.id.payPwdView);
        PwdInputMethodView inputMethodView = dialog.findViewById(R.id.inputMethodView);
        psw_input.setInputMethodView(inputMethodView);
        psw_input.setInputCallBack(inputCallBack);

        dialog.findViewById(R.id.iv_close).setOnClickListener(this);

        llPassword = dialog.findViewById(R.id.ll_password);
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.iv_close:
                dismiss();
                break;
        }
    }

    /**
     * set input callback
     *
     * @param inputCallBack
     */
    public void setPaySuccessCallBack(PayPwdView.InputCallBack inputCallBack) {
        this.inputCallBack = inputCallBack;
    }
}
