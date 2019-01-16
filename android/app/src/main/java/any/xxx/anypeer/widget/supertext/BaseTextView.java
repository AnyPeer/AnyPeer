package any.xxx.anypeer.widget.supertext;

import android.content.Context;
import android.text.InputFilter;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.Gravity;
import android.widget.LinearLayout;
import android.widget.TextView;

public class BaseTextView extends LinearLayout {
    private Context mContext;

    private TextView topTextView, centerTextView, bottomTextView;

    private LayoutParams topTVParams, centerTVParams, bottomTVParams;

    public BaseTextView(Context context) {
        this(context, null);
    }

    public BaseTextView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public BaseTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        this.setOrientation(VERTICAL);
        this.setGravity(Gravity.LEFT);

        mContext = context;

        initView();
    }

    private void initView() {
        initTopView();
        initCenterView();
        initBottomView();
    }

    private void initTopView() {
        if (topTVParams == null) {
            topTVParams = getParams(topTVParams);
        }
        if (topTextView == null) {
            topTextView = initTextView(topTVParams, topTextView);
        }
    }

    private void initCenterView() {
        if (centerTVParams == null) {
            centerTVParams = getParams(centerTVParams);
        }
        if (centerTextView == null) {
            centerTextView = initTextView(centerTVParams, centerTextView);
        }
    }

    private void initBottomView() {
        if (bottomTVParams == null) {
            bottomTVParams = getParams(bottomTVParams);
        }
        if (bottomTextView == null) {
            bottomTextView = initTextView(bottomTVParams, bottomTextView);
        }
    }

    private TextView initTextView(LayoutParams params, TextView textView) {
        textView = getTextView(textView, params);
        addView(textView);
        return textView;
    }

    public TextView getTextView(TextView textView, LayoutParams layoutParams) {
        if (textView == null) {
            textView = new TextView(mContext);
            textView.setLayoutParams(layoutParams);
            textView.setVisibility(GONE);
        }
        return textView;
    }

    public LayoutParams getParams(LayoutParams params) {
        if (params == null) {
            params = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
        }
        return params;
    }

    private void setTextString(TextView textView, CharSequence textString) {
        textView.setText(textString);
        if (!TextUtils.isEmpty(textString)) {
            textView.setVisibility(VISIBLE);
        }
    }

    public void setTopTextString(CharSequence s) {
        setTextString(topTextView, s);
    }


    public void setCenterTextString(CharSequence s) {
        setTextString(centerTextView, s);
    }

    public void setBottomTextString(CharSequence s) {
        setTextString(bottomTextView, s);
    }

    public TextView getTopTextView() {
        return topTextView;
    }

    public TextView getCenterTextView() {
        return centerTextView;
    }

    public TextView getBottomTextView() {
        return bottomTextView;
    }

    public void setMaxEms(int topMaxEms, int centerMaxEms, int bottomMaxEms) {

        if (topMaxEms != 0) {
            topTextView.setEllipsize(TextUtils.TruncateAt.END);
            topTextView.setFilters(new InputFilter[]{new InputFilter.LengthFilter(topMaxEms)});
        }
        if (centerMaxEms != 0) {
            centerTextView.setEllipsize(TextUtils.TruncateAt.END);
            centerTextView.setFilters(new InputFilter[]{new InputFilter.LengthFilter(centerMaxEms)});
        }
        if (bottomMaxEms != 0) {
            bottomTextView.setEllipsize(TextUtils.TruncateAt.END);
            bottomTextView.setFilters(new InputFilter[]{new InputFilter.LengthFilter(bottomMaxEms)});
        }

    }

    public void setCenterSpaceHeight(int centerSpaceHeight) {
        topTVParams.setMargins(0, 0, 0, centerSpaceHeight);
        centerTVParams.setMargins(0, 0, 0, 0);
        bottomTVParams.setMargins(0, centerSpaceHeight, 0, 0);
    }
}
