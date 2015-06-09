
package com.jrdcom.timetool.countdown.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.LinearLayout;

import com.android.deskclock.R;
import com.jrdcom.timetool.MyLog;
import com.jrdcom.timetool.countdown.view.VerticalTextPicker.OnChangedListener;

public class CountDownPickerTcl extends LinearLayout {
    private int mCurrentHour = 0;

    private int mCurrentMinute = 0;

    private int mCurrentSecond = 0;

    private PickerLable mHourPicker;

    private PickerLable mMinutePicker;

    private PickerLable mSecondPicker;

    private Drawable leftArrow;
    private Drawable rightArrow;

    private OnTimerChangedListener mOnTimerChangedListener;

    private int arrowHeight;

    private RectF rect;

    public interface OnTimerChangedListener {
        void onTimerChanged(CountDownPickerTcl view, int hour, int minute, int second);
    }

    public CountDownPickerTcl(Context context) {
        this(context, null);
    }

    public CountDownPickerTcl(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CountDownPickerTcl(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        MyLog.debug("start inflate layout", getClass());
        inflater.inflate(R.layout.countdown_picker_tclf, this, true);
        leftArrow = getResources().getDrawable(R.drawable.countdown_arrow_left);
        rightArrow = getResources().getDrawable(R.drawable.countdown_arrow_right);
        arrowWidth = leftArrow.getIntrinsicWidth();
        arrowHeight = rightArrow.getIntrinsicHeight();

    }

    // modified by haifeng.tang Pr 785760
    private void initLayout() {
        MyLog.debug("inflate layout success", getClass());

        mHourPicker = (PickerLable) findViewById(R.id.timerpicker_hour);
        mHourPicker.getPicker().setRange(0, 24);
        mHourPicker.getPicker().setOnChangeListener(mChangedListener);
        mHourPicker.getPicker().setDrawLeftArrow(true);

        mMinutePicker = (PickerLable) findViewById(R.id.timerpicker_minute);
        mMinutePicker.getPicker().setRange(0, 59);
        mMinutePicker.getPicker().setOnChangeListener(mChangedListener);

        mSecondPicker = (PickerLable) findViewById(R.id.timerpicker_second);
        mSecondPicker.getPicker().setRange(0, 59);
        mSecondPicker.getPicker().setOnChangeListener(mChangedListener);
        mSecondPicker.getPicker().setDrawRighttArrow(true);
        
        
        MyLog.debug("inflate layout end", getClass());

        init(0, 1, 0, null);


        if (!isEnabled()) {
            setEnabled(false);
        }
    }

//     @Override
//     protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
//     mSecondPicker.getPicker().measure(0, 0);
//     setMeasuredDimension(mSecondPicker.getPicker().getMeasuredWidth()*3+arrowWidth*2,
//     getMeasuredHeight());
//     }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        // add by haifeng.tang Pr 785760 begin
        initLayout();
        // add by haifeng.tang Pr 785760 end
    }

    private OnChangedListener mChangedListener = new OnChangedListener() {
        public void onChanged(VerticalTextPicker spinner, int oldPos, int newPos, String[] items) {

            mCurrentHour = Integer.parseInt(mHourPicker.getPicker().getCurrent());
            mCurrentMinute = Integer.parseInt(mMinutePicker.getPicker().getCurrent());
            mCurrentSecond = Integer.parseInt(mSecondPicker.getPicker().getCurrent());
            onTimerChanged();
        }
    };

    private int arrowWidth;

    protected void dispatchDraw(android.graphics.Canvas canvas) {
        MyLog.i("dispatchDraw");
    
      
//        rightArrow.setBounds(rect);
//        canvas.translate(getMeasuredWidth() - arrowWidth, getMeasuredHeight() / 2);
//        rightArrow.draw(canvas);
//        canvas.restore();
        // float margin = mHourPicker.getPicker().getSeletedItemMargin();
        // MyLog.i("margin->" + margin);
        // MyLog.i("getMeasuredHeight->" + getMeasuredHeight());
        // float center = getMeasuredHeight() / 2;
        // RectF rect1 = new RectF(0, center - margin, getMeasuredWidth(),
        // center + margin);
        // Paint paint = new Paint();
        // // paint.setColor(0xfff5f5f5);
        // paint.setColor(0xfff0f0f0);
        // canvas.drawRect(rect1, paint);
        // canvas.restore();

        super.dispatchDraw(canvas);

    };
    
  

    public void init(int hour, int minute, int second, OnTimerChangedListener onTimerChangedListener) {
        setCurrentHour(hour);
        setCurrentMinute(minute);
        setCurrentSecond(second);
        setOnTimerChangedListener(onTimerChangedListener);
    }

    public Integer getCurrentHour() {
        return mCurrentHour;
    }

    public Integer getCurrentMinute() {
        return mCurrentMinute;
    }

    public Integer getCurrentSecond() {
        return mCurrentSecond;
    }

    public void setCurrentHour(Integer currentHour) {
        mCurrentHour = currentHour;
        mHourPicker.getPicker().setCurrent(pad(currentHour));
    }

    public void setCurrentMinute(Integer currentMinute) {
        mCurrentMinute = currentMinute;
        mMinutePicker.getPicker().setCurrent(pad(currentMinute));
    }

    public void setCurrentSecond(Integer currentSecond) {
        mCurrentSecond = currentSecond;
        mSecondPicker.getPicker().setCurrent(pad(currentSecond));
    }

    public void setOnTimerChangedListener(OnTimerChangedListener onTimerChangedListener) {
        mOnTimerChangedListener = onTimerChangedListener;
    }

    private void onTimerChanged() {
        if (mOnTimerChangedListener != null) {
            mOnTimerChangedListener.onTimerChanged(this, getCurrentHour(), getCurrentMinute(),
                    getCurrentSecond());
        }
    }

    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        mMinutePicker.setEnabled(enabled);
        mHourPicker.setEnabled(enabled);
        mSecondPicker.setEnabled(enabled);
    }

    private String pad(int i) {
        return i < 10 ? "0" + i : String.valueOf(i);
    }
}
