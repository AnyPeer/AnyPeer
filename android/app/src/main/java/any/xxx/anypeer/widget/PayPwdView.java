package any.xxx.anypeer.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.text.InputType;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.BaseInputConnection;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputConnection;
import java.util.ArrayList;
import any.xxx.anypeer.R;

public class PayPwdView extends View {
    private ArrayList<String> result;
    private int count;
    private int size;
    private Paint mBorderPaint;
    private Paint mDotPaint;
    private int mBorderColor;
    private int mDotColor;
    private RectF mRoundRect;
    private int mRoundRadius;

    public PayPwdView(Context context) {
        super(context);
        init(null);
    }

    private InputCallBack inputCallBack;
    private PwdInputMethodView inputMethodView;


    public interface InputCallBack {
        void onInputFinish(String result);
    }

    public PayPwdView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs);
    }

    public PayPwdView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(attrs);
    }

    /**
     * Init
     */
    void init(AttributeSet attrs) {
        final float dp = getResources().getDisplayMetrics().density;
        this.setFocusable(true);
        this.setFocusableInTouchMode(true);
        result = new ArrayList<>();
        if (attrs != null) {
            TypedArray ta = getContext().obtainStyledAttributes(attrs, R.styleable.PayPwdView);
            mBorderColor = ta.getColor(R.styleable.PayPwdView_border_color, Color.LTGRAY);
            mDotColor = ta.getColor(R.styleable.PayPwdView_dot_color, Color.BLACK);
            count = ta.getInt(R.styleable.PayPwdView_count, 6);
            ta.recycle();
        } else {
            mBorderColor = Color.LTGRAY;
            mDotColor = Color.GRAY;
            count = 6;//default password count
        }
        size = (int) (dp * 30);
        //color
        mBorderPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mBorderPaint.setStrokeWidth(3);
        mBorderPaint.setStyle(Paint.Style.STROKE);
        mBorderPaint.setColor(mBorderColor);

        mDotPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mDotPaint.setStrokeWidth(3);
        mDotPaint.setStyle(Paint.Style.FILL);
        mDotPaint.setColor(mDotColor);
        mRoundRect = new RectF();
        mRoundRadius = (int) (5 * dp);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int w = measureWidth(widthMeasureSpec);
        int h = measureHeight(heightMeasureSpec);
        int wsize = MeasureSpec.getSize(widthMeasureSpec);
        int hsize = MeasureSpec.getSize(heightMeasureSpec);
        if (w == -1) {
            if (h != -1) {
                w = h * count;
                size = h;
            } else {
                w = size * count;
                h = size;
            }
        } else {
            if (h == -1) {
                h = w / count;
                size = h;
            }
        }
        setMeasuredDimension(Math.min(w, wsize), Math.min(h, hsize));
    }

    private int measureWidth(int widthMeasureSpec) {
        int wmode = MeasureSpec.getMode(widthMeasureSpec);
        int wsize = MeasureSpec.getSize(widthMeasureSpec);
        if (wmode == MeasureSpec.AT_MOST) {//wrap_content
            return -1;
        }
        return wsize;
    }

    private int measureHeight(int heightMeasureSpec) {
        int hmode = MeasureSpec.getMode(heightMeasureSpec);
        int hsize = MeasureSpec.getSize(heightMeasureSpec);
        if (hmode == MeasureSpec.AT_MOST) {//wrap_content
            return -1;
        }
        return hsize;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            requestFocus();
            inputMethodView.setVisibility(VISIBLE);
            return true;
        }
        return true;
    }

    @Override
    protected void onFocusChanged(boolean gainFocus, int direction, Rect previouslyFocusedRect) {
        super.onFocusChanged(gainFocus, direction, previouslyFocusedRect);
        if (gainFocus) {
            inputMethodView.setVisibility(VISIBLE);
        } else {
            inputMethodView.setVisibility(GONE);
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        final int width = getWidth() - 2;
        final int height = getHeight() - 2;
        mRoundRect.set(0, 0, width, height);
        canvas.drawRoundRect(mRoundRect, 0, 0, mBorderPaint);
        for (int i = 1; i < count; i++) {
            final int x = i * size;
            canvas.drawLine(x, 0, x, height, mBorderPaint);
        }
        int dotRadius = size / 8;
        for (int i = 0; i < result.size(); i++) {
            final float x = (float) (size * (i + 0.5));
            final float y = size / 2;
            canvas.drawCircle(x, y, dotRadius, mDotPaint);
        }
    }

    @Override
    public boolean onCheckIsTextEditor() {
        return true;
    }

    @Override
    public InputConnection onCreateInputConnection(EditorInfo outAttrs) {
        outAttrs.inputType = InputType.TYPE_CLASS_NUMBER;
        outAttrs.imeOptions = EditorInfo.IME_ACTION_DONE;
        return new MyInputConnection(this, false);
    }

    public void setInputCallBack(InputCallBack inputCallBack) {
        this.inputCallBack = inputCallBack;
    }

    public void clearResult() {
        result.clear();
        invalidate();
    }


    private class MyInputConnection extends BaseInputConnection {
        public MyInputConnection(View targetView, boolean fullEditor) {
            super(targetView, fullEditor);
        }

        @Override
        public boolean commitText(CharSequence text, int newCursorPosition) {
            return super.commitText(text, newCursorPosition);
        }

        @Override
        public boolean deleteSurroundingText(int beforeLength, int afterLength) {
            if (beforeLength == 1 && afterLength == 0) {
                return super.sendKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_DEL))
                        && super.sendKeyEvent(new KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_DEL));
            }
            return super.deleteSurroundingText(beforeLength, afterLength);
        }
    }


    /**
     * @param inputMethodView
     */
    public void setInputMethodView(PwdInputMethodView inputMethodView) {
        this.inputMethodView = inputMethodView;
        this.inputMethodView.setInputReceiver(new PwdInputMethodView.InputReceiver() {
            @Override
            public void receive(String num) {
                if (num.equals("-1")) {
                    if (!result.isEmpty()) {
                        result.remove(result.size() - 1);
                        invalidate();
                    }
                } else {
                    if (result.size() < count) {
                        result.add(num);
                        invalidate();
                        ensureFinishInput();
                    }
                }


            }
        });
    }

    /**
     * check finish?
     */
    void ensureFinishInput() {
        if (result.size() == count && inputCallBack != null) {
            StringBuffer sb = new StringBuffer();
            for (String i : result) {
                sb.append(i);
            }
            inputCallBack.onInputFinish(sb.toString());
        }
    }

    public String getInputText() {
        if (result.size() == count) {
            StringBuffer sb = new StringBuffer();
            for (String i : result) {
                sb.append(i);
            }
            return sb.toString();
        }
        return null;
    }
}
