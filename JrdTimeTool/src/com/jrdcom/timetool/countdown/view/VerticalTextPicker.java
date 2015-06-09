
package com.jrdcom.timetool.countdown.view;



import java.util.Date;

import junit.framework.Test;

import android.R.integer;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Paint.FontMetrics;
import android.graphics.drawable.Drawable;
import android.text.TextPaint;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.MeasureSpec;
import android.view.ViewGroup.LayoutParams;
import android.widget.TextView;

import com.android.deskclock.R;
import com.jrdcom.timetool.MyLog;

public class VerticalTextPicker extends View {

    private static final int TEXT_HORIZONTAL_MARGIN = 10;

    private float itemHeight;

    private float center_x = -1;
    private float center_y = -1;

    private float arrowCenter;

    public float real_normal_text_height;
    public float real_light_text_height;

    private boolean isDrawLeftArrow = false;

    private int height_count = -1;

    private Rect arrowRect;

    public void setHeight_count(int height_count) {
        this.height_count = height_count;
    }

    public void setDrawLeftArrow(boolean isDrawLeftArrow) {
        this.isDrawLeftArrow = isDrawLeftArrow;
    }

    public void setDrawRighttArrow(boolean isDrawRighttArrow) {
        this.isDrawRighttArrow = isDrawRighttArrow;
    }

    private float normal_text_height = -1;
    private boolean isDrawRighttArrow = false;

    private Drawable leftArrow;
    private Drawable rightArrow;

    private int arrowWidth;
    private int arrowHeight;

    public float getNormal_text_height() {
        return normal_text_height;
    }

    private float light_text_height = -1;

    public float getLight_text_height() {
        return light_text_height;
    }

    public void setLight_text_height(float light_text_height) {
        this.light_text_height = light_text_height;
    }

    private float text_width;
    private float picker_text_canvas_height;

    private int TEXT_SIZE;
    private float TEXT_MAGIN = 14;

    public float getTEXT_MAGIN() {
        return TEXT_MAGIN;
    }

    private int drawItemCount = 6;

    private int middleDrawCount = drawItemCount / 2;

    public void setDrawItemCount(int count) {
        this.drawItemCount = count;
        middleDrawCount = drawItemCount / 2;
    }

    private float VERTICAL_BACKGROUND_MARGIN = 5;

    private float HORIZONTAL_BACKGROUND_MARGIN = 20;

    private static final int SCROLL_MODE_NONE = 0;

    private static final int SCROLL_MODE_UP = -1;

    private static final int SCROLL_MODE_DOWN = 1;

    private static final int MOTION_STOP = 0;

    private static final int MOTION_SCROLL_ONE_ITEM = 1;

    private static final int MOTION_SCROLLING = 2;

    private static final int MOTION_ADJUST = 3;

    private static final int MOTION_PROGRESS = 4;

    public static final int ALIGN_RIGHT = 0;

    public static final int ALIGN_CENTER = 1;

    public static final int ALIGN_LEFT = 2;

    public static final int AMOUNT_THR = 3;

    public static final int AMOUNT_FIV = 5;

    public static final int AMOUNT_SEV = 7;

    public static final int SPINNER_POS_SINGLE = 0;

    public static final int SPINNER_POS_LEFT = 1;

    public static final int SPINNER_POS_CENTER = 2;

    public static final int SPINNER_POS_RIGHT = 3;

    private boolean isText = false;

    public void setText(boolean isText) {
        this.isText = isText;
    }

    private int SCROLL_DISTANCE;
    private int SMALL_SCROLL_DISTANCE;

    private TextPaint mNormalTextPaint;

    private TextPaint mLightTextPaint;

    private int mPreMovDistance;

    private int mTotalMovDistance;

    private int mTotalMovCount;

    private int mMotion;

    private int mCount;

    private long mPreTime;

    private boolean mToBottom = false;

    private int mAmountOfItems = AMOUNT_SEV;

    private int mTotalDistance;

    private int mScrollMode;

    private boolean mWrapAround = true;

    private int mTotalAnimatedDistance;

    private int mDistanceOfEachAnimation;

    private String[] mTextList;

    private int mCurrentSelectedPos;

    private int mIsScroll;

    private OnChangedListener mListener;
    private String[] mText;

    private String mTitle;

    // add by haifeng.tang start,unit=dp or dip
    private float PAINT_OFFSET = 10;

    private float real_text_item_height;

    private final int COUNT = 7;

    private float normal_text_size = 21;
    private float light_text_size = 32;

    private float center_baseline;

    private int realWidth;

    public int getRealWidth() {
        return realWidth;
    }

    public void setRealWidth(int realWidth) {
        this.realWidth = realWidth;
    }

    public int getRealHeight() {
        return realHeight;
    }

    public void setRealHeight(int realHeight) {
        this.realHeight = realHeight;
    }

    private int realHeight;

    private TextPaint currentPaint;

    private RectF mCenterSeletedRect;

    private float centerItemTop;

    // add by haifeng.tang end

    public interface OnChangedListener {
        void onChanged(VerticalTextPicker spinner, int oldPos, int newPos, String[] items);
    }

    public VerticalTextPicker(Context context) {
        this(context, null);
    }

    public VerticalTextPicker(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public VerticalTextPicker(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        mMotion = MOTION_STOP;

        Drawable d = getResources().getDrawable(R.drawable.countdown_arrow_left);

        arrowWidth = d.getIntrinsicWidth();
        arrowHeight = d.getIntrinsicHeight();

        initPaint();
        convertValue();
        initData();
        loadArrowDrawable();
    }

    private void loadArrowDrawable() {
        leftArrow = getResources().getDrawable(R.drawable.countdown_arrow_left);
        arrowWidth = leftArrow.getIntrinsicWidth();
        arrowHeight = leftArrow.getIntrinsicHeight();
        rightArrow = getResources().getDrawable(R.drawable.countdown_arrow_right);
        arrowWidth = leftArrow.getIntrinsicWidth();
        arrowHeight = leftArrow.getIntrinsicHeight();
    }

    public int getSeletedItemMargin() {
        return (int) ((int) (light_text_height / 2) + dpToPx(14) / 2);
    }

    /**
     * add by haifeng.tang 2014.8.25
     */

    private void initData() {
        mText = new String[9];
        mScrollMode = SCROLL_MODE_NONE;
        setAmount(AMOUNT_SEV);
    }

    // add by haifeng.tang
    private void initPaint() {

        mNormalTextPaint = new TextPaint(Paint.ANTI_ALIAS_FLAG);
        mNormalTextPaint.setColor(getResources().getColor(R.color.verticaltextpicker_small_color));
        mNormalTextPaint.setTextAlign(Paint.Align.CENTER);
        normal_text_size = dpToPx(normal_text_size);
        mNormalTextPaint.setTextSize(normal_text_size);

        mLightTextPaint = new TextPaint(Paint.ANTI_ALIAS_FLAG);
        mLightTextPaint.setColor(getResources().getColor(R.color.verticaltextpicker_big_color));
        mLightTextPaint.setTextAlign(Paint.Align.CENTER);
        light_text_size = dpToPx(light_text_size);
        mLightTextPaint.setTextSize(light_text_size);

    }

    private void convertValue() {
        PAINT_OFFSET = dpToPx(PAINT_OFFSET);
        TEXT_MAGIN = dpToPx(TEXT_MAGIN);
        VERTICAL_BACKGROUND_MARGIN = dpToPx(VERTICAL_BACKGROUND_MARGIN);
        HORIZONTAL_BACKGROUND_MARGIN = dpToPx(HORIZONTAL_BACKGROUND_MARGIN);
    }

    // add by haifeng.tang
    private float dpToPx(float dp) {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, getResources()
                .getDisplayMetrics());
    }

    /*
     * Interface
     */
    public void setOnChangeListener(OnChangedListener listener) {
        mListener = listener;
    }

    /*
     * Set string list which spinner will use to display
     */
    public void setItems(String[] textList) {
        mTextList = textList;
        calculateTextPositions();
    }

    private void calculateItem() {
        MyLog.debug("calculate item", getClass());
        mLightTextPaint.setTextSize(TEXT_SIZE);
        Rect item = new Rect();
        MyLog.debug("mTextList[0]->" + mTextList[0], getClass());
        mLightTextPaint.getTextBounds(mTextList[0], 0, mTextList[0].length(), item);
        normal_text_height = item.height();
        MyLog.debug("textHeight->" + normal_text_height, getClass());
        text_width = item.width();
        MyLog.debug("text_width->" + text_width, getClass());
        // real_text_item_height = (picker_text_canvas_height - text_margin *
        // 2f) / COUNT;
        MyLog.debug("real_text_item_height->" + real_text_item_height, getClass());
        float real_text_item_width = getMeasuredWidth() - HORIZONTAL_BACKGROUND_MARGIN * 2;
        MyLog.debug("real_text_item_width->" + real_text_item_width, getClass());
        MyLog.debug("getMeasuredWidth->" + getMeasuredWidth(), getClass());
        boolean b = normal_text_height > real_text_item_height || text_width > real_text_item_width;
        MyLog.debug("b->" + b, getClass());
        if (b) {
            TEXT_SIZE -= 1;
            mLightTextPaint.setTextSize(TEXT_SIZE);
            mNormalTextPaint.setTextSize(TEXT_SIZE - PAINT_OFFSET);
            calculateItem();
        }

    }

    public void setRange(int start, int end) {
        mTextList = new String[end - start + 1];
        for (int i = 0, j = start; i <= end - start; i++, j++) {
            mTextList[i] = j < 10 ? ("0" + j) : String.valueOf(j);
        }
    }

    public void setRange(String[] str) {
        int len = str.length;
        mTextList = new String[str.length];
        for (int i = 0; i < len; i++) {
            mTextList[i] = str[i];
        }
    }

    public void setCurrent(String current) {
        if (mTextList == null || current == null) {
            return;
        }
        int selectedPos = 0;
        for (int i = 0; i < mTextList.length; i++) {
            if (mTextList[i] != null && mTextList[i].equals(current)) {
                selectedPos = i;
                break;
            }
        }
        setSelectedPos(selectedPos);
    }

    /*
     * Set default item which had been selected last time
     */
    public void setSelectedPos(int selectedPos) {
        mCurrentSelectedPos = selectedPos;
        calculateTextPositions();
        postInvalidate();
    }

    /*
     * Set if display all items in circulation mode default: false
     */

    public void setWrapAround(boolean wrap) {
        mWrapAround = wrap;
    }

    /*
     * Set Title on the selector,if do not set,no title default: null
     */
    public void setTitle(String title) {
        mTitle = title;
    }

    /*
     * Set amount of all the items to display default : 3
     */
    public void setAmount(int n) {
        switch (n) {
            case AMOUNT_FIV:
            case AMOUNT_SEV:
                mAmountOfItems = n;
                break;
            default:
                mAmountOfItems = AMOUNT_SEV;
        }
    }

    /*
     * return Item list
     */

    public String[] getTextList() {
        return mTextList;
    }

    /*
     * return the item with the index
     */
    public String getTextItem(int idx) {
        return mTextList[idx];
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {

        if (hasFocus()) {
            if ((keyCode == KeyEvent.KEYCODE_DPAD_UP)) {
                mScrollMode = SCROLL_MODE_UP;
                mMotion = MOTION_SCROLL_ONE_ITEM;
                invalidate();

                return true;
            } else if ((keyCode == KeyEvent.KEYCODE_DPAD_DOWN)) {
                mScrollMode = SCROLL_MODE_DOWN;
                mMotion = MOTION_SCROLL_ONE_ITEM;
                invalidate();
                return true;
            }

            return super.onKeyDown(keyCode, event);
        }
        return true;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        final int action = event.getAction();
        final int y = (int) event.getY();

        switch (action) {
            case MotionEvent.ACTION_DOWN:
                requestFocus();
                mPreMovDistance = y;
                mTotalMovDistance = 0;
                mTotalMovCount = 0;
                mMotion = MOTION_STOP;
                mDistanceOfEachAnimation = 0;
                break;

            case MotionEvent.ACTION_MOVE:

                int pos = y - mPreMovDistance;

                mTotalMovDistance += Math.abs(pos);

                if (pos < 0) {
                    if (mScrollMode != SCROLL_MODE_UP) {
                        mScrollMode = SCROLL_MODE_UP;
                    }
                    if (Math.abs(pos) >= SCROLL_DISTANCE)
                        mDistanceOfEachAnimation = SCROLL_DISTANCE;
                    else
                        mDistanceOfEachAnimation = Math.abs(pos);
                    invalidate();
                } else if (pos > 0) {
                    if (mScrollMode != SCROLL_MODE_DOWN) {
                        mScrollMode = SCROLL_MODE_DOWN;

                    }
                    if (pos >= SCROLL_DISTANCE)
                        mDistanceOfEachAnimation = SCROLL_DISTANCE;
                    else
                        mDistanceOfEachAnimation = pos;
                    invalidate();
                }
                mPreTime = new Date().getTime();
                mPreMovDistance = y;
                mTotalMovCount++;
                break;

            case MotionEvent.ACTION_UP:
                if (mTotalMovCount != 0) {
                    int speed = mTotalMovDistance / mTotalMovCount;

                    long curTime = new Date().getTime();
                    if (speed > 12 && curTime - mPreTime < 100) {
                        mMotion = MOTION_SCROLLING;
                        if (speed > 30)
                            mDistanceOfEachAnimation = 30;
                        else
                            mDistanceOfEachAnimation = speed;
                    } else {
                        mDistanceOfEachAnimation = 0;
                        mMotion = MOTION_ADJUST;
                    }

                } else
                    mMotion = MOTION_ADJUST;
                invalidate();
                break;
            case MotionEvent.ACTION_CANCEL:

                invalidate();
                break;
            default:
                invalidate();
                break;
        }
        return true;
    }

    protected int getMeasuredHeight_tcl() {
        return getMeasuredHeight();

    }

    private void drawArrow(Canvas canvas) {

        if (isDrawLeftArrow) {
            canvas.save();
            canvas.translate(0, arrowCenter);

            leftArrow.setBounds(arrowRect);
            leftArrow.draw(canvas);
            canvas.restore();
        }

        if (isDrawRighttArrow) {
            canvas.save();
            canvas.translate(getMeasuredWidth() - arrowWidth, arrowCenter);
            rightArrow.setBounds(arrowRect);
            rightArrow.draw(canvas);
            canvas.restore();
        }

    }

    /**
     * add by haifeng.tang 2014.8.20
     */

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {

        MyLog.i("->>>>OnMeasure");

//        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
//            setDrawItemCount(7);
//        } else {
//            setDrawItemCount(5);
//        }

        Rect item = new Rect();
        float light_item_width = 0;
        mNormalTextPaint.getTextBounds(mTextList[0], 0, mTextList[0].length(), item);
        normal_text_height = item.width();
        normal_text_height = item.height();
        mLightTextPaint.getTextBounds(mTextList[0], 0, mTextList[0].length(), item);
        light_item_width = item.width();
        light_text_height = item.height();
        if (isText) {
            normal_text_height = real_normal_text_height;
            light_text_height = real_light_text_height;
        }

        float height = (drawItemCount - 1) * TEXT_MAGIN + normal_text_height * (drawItemCount - 1)
                + light_text_height;

        SCROLL_DISTANCE = (int) (light_text_height + TEXT_MAGIN);
        SMALL_SCROLL_DISTANCE = (int) (normal_text_height + TEXT_MAGIN);

        realWidth = (int) (light_item_width + HORIZONTAL_BACKGROUND_MARGIN * 2);
        realHeight = (int) (height + VERTICAL_BACKGROUND_MARGIN * 2);

        center_baseline = center_y + light_text_height / 2;

        centerItemTop = normal_text_height * middleDrawCount + TEXT_MAGIN * middleDrawCount - 10;
        float centerItemBottom = centerItemTop + light_text_height + 20;
        if (isDrawLeftArrow || isDrawRighttArrow) {

            realWidth += (arrowWidth * 2);

            if (isDrawLeftArrow) {
                mCenterSeletedRect = new RectF(arrowWidth, centerItemTop, realWidth,
                        centerItemBottom);
            }
            if (isDrawRighttArrow) {
                mCenterSeletedRect = new RectF(0, centerItemTop, realWidth - arrowWidth,
                        centerItemBottom);
            }

        } else {
            mCenterSeletedRect = new RectF(0, centerItemTop, realWidth, centerItemBottom);
        }

        arrowRect = new Rect(0, 0, arrowWidth, arrowHeight);

        arrowCenter = normal_text_height * middleDrawCount + TEXT_MAGIN * middleDrawCount
                + light_text_height / 2 - arrowHeight / 2;

        center_y = realHeight / 2;
        center_x = realWidth / 2;

        MyLog.debug("realWidth->" + realWidth, getClass());
        MyLog.debug("realHeight->" + realHeight, getClass());
        MyLog.i("center_baseline->" + center_baseline);
        MyLog.i("normal_text_height->" + normal_text_height);
        MyLog.i("light_text_height->" + light_text_height);
        MyLog.i("center_y->" + center_y);
        setMeasuredDimension(realWidth, realHeight);
        MyLog.i("getMeasuredHeight->" + getMeasuredHeight());

        // calculateItem();
    }

    /**
     * add by haifeng.tang for adjust screen
     */
    private void calculateTextSize() {
        picker_text_canvas_height = getMeasuredHeight() - VERTICAL_BACKGROUND_MARGIN * 2;
        itemHeight = picker_text_canvas_height / COUNT;
        center_x = getMeasuredWidth() / 2f;
        center_y = picker_text_canvas_height / 2 + VERTICAL_BACKGROUND_MARGIN;
        TEXT_SIZE = getMeasuredHeight() / COUNT;
        mLightTextPaint.setTextSize(TEXT_SIZE);
        mNormalTextPaint.setTextSize(TEXT_SIZE - PAINT_OFFSET);
        SCROLL_DISTANCE = (int) itemHeight;
    }

    /* PR 564397- Neo Skunkworks - Paul Xu added - 001 End */

    @Override
    protected void onDraw(Canvas canvas) {

        MyLog.i("onDraw");

        MyLog.i("centerItemTop->" + centerItemTop);
        MyLog.i("middleDrawCount->" + middleDrawCount);
        MyLog.i("normal_text_height->" + normal_text_height);
        MyLog.i("mText->" + mText.toString());
        MyLog.i("mText length->" + mText.length);

        for (int i = 0; i < mText.length; i++) {
            MyLog.e("mText" + i + "->" + mText[i]);
        }

        if (mTextList == null || mTextList.length == 0) {
            return;
        }
        int nextScrollDistance = mTotalAnimatedDistance + mDistanceOfEachAnimation * mScrollMode;

        if (nextScrollDistance >= SCROLL_DISTANCE || nextScrollDistance <= -SCROLL_DISTANCE) {
            if (mScrollMode == SCROLL_MODE_UP) {
                int oldPos = mCurrentSelectedPos;
                int newPos = getNewIndex(1);

                if (newPos >= 0) {
                    mIsScroll = 1;
                    mCurrentSelectedPos = newPos;
                    mTotalAnimatedDistance += SCROLL_DISTANCE - mDistanceOfEachAnimation;
                    if (mListener != null) {
                        mListener.onChanged(this, oldPos, newPos, mTextList);
                    }
                }

                calculateTextPositions();
            } else if (mScrollMode == SCROLL_MODE_DOWN) {
                int oldPos = mCurrentSelectedPos;
                int newPos = getNewIndex(-1);
                if (newPos >= 0) {
                    mIsScroll = 1;
                    mCurrentSelectedPos = newPos;
                    mTotalAnimatedDistance += mDistanceOfEachAnimation - SCROLL_DISTANCE;
                    if (mListener != null) {
                        mListener.onChanged(this, oldPos, newPos, mTextList);
                    }
                }
                calculateTextPositions();
            }
        } else {
            if (mScrollMode == SCROLL_MODE_DOWN)
                mTotalAnimatedDistance += mDistanceOfEachAnimation;
            else
                mTotalAnimatedDistance -= mDistanceOfEachAnimation;
            mIsScroll = 0;
        }

        if ((mCurrentSelectedPos == 0 && mScrollMode == SCROLL_MODE_DOWN
                && mTotalAnimatedDistance > SCROLL_DISTANCE / 2 || mCurrentSelectedPos == mTextList.length - 1
                && mScrollMode == SCROLL_MODE_UP && mTotalAnimatedDistance < -SCROLL_DISTANCE / 2)
                && mWrapAround == false) {
            if (mScrollMode == SCROLL_MODE_UP)
                mTotalAnimatedDistance = -SCROLL_DISTANCE / 2;
            else
                mTotalAnimatedDistance = SCROLL_DISTANCE / 2;
            mToBottom = true;
        }
        // canvas.drawLine(center_x, 0, center_x, realHeight, mLightTextPaint);
        //
        // for (int i = 1; i <= mAmountOfItems / 2; i++) {
        // drawText(canvas, mNormalTextPaint, center_baseline - i *
        // SCROLL_DISTANCE
        // + mTotalAnimatedDistance, mText[3 - i]);
        // drawText(canvas, mNormalTextPaint, center_baseline + i *
        // SCROLL_DISTANCE
        // + mTotalAnimatedDistance, mText[3 + i]);
        // }

        // canvas.saveLayerAlpha(rect, 255, Canvas.ALL_SAVE_FLAG);
        // drawCenterRect(canvas);
        // canvas.restore();

        canvas.saveLayerAlpha(mCenterSeletedRect, 255, Canvas.ALL_SAVE_FLAG);
        drawCenterRect(canvas);
        canvas.restore();
        int baseLine = (int) normal_text_height;
        currentPaint = mNormalTextPaint;
        double start_alpha = 255 * 0.3;
        double alpha_count = (255 - start_alpha) / (drawItemCount / 2);
        double alpha = start_alpha;
        if (height_count != -1) {
            baseLine = (int) (middleDrawCount * normal_text_height + (middleDrawCount - 1)
                    * TEXT_MAGIN);
            drawText(canvas, mNormalTextPaint, baseLine + mTotalAnimatedDistance, mText[0]);
            baseLine += (TEXT_MAGIN + light_text_height);
            drawText(canvas, mLightTextPaint, baseLine + mTotalAnimatedDistance, mText[1]);

        } else {
            for (int i = 0; i < drawItemCount; i++) {
                MyLog.i("current index->" + i);
                MyLog.i("drawItemCount->" + drawItemCount);
                mNormalTextPaint.setAlpha((int) alpha);
                if (currentPaint == mNormalTextPaint) {
                    MyLog.i("current paint-> mNormalTextPaint");
                } else {
                    MyLog.i("current paint-> mLightTextPaint");
                }

                drawText(canvas, currentPaint, baseLine + mTotalAnimatedDistance, mText[i]);

                if (i == drawItemCount / 2 - 1) {
                    baseLine += SCROLL_DISTANCE;
                    currentPaint = mLightTextPaint;
                    alpha = 255;
                } else {
                    if (i < drawItemCount / 2 - 1) {
                        alpha += alpha_count;
                    } else {
                        alpha -= alpha_count;
                    }
                    baseLine += SMALL_SCROLL_DISTANCE;
                    currentPaint = mNormalTextPaint;
                }
            }
        }

        // if (count == 7) {
        // // drawText(canvas, mLightTextPaint, center_baseline +
        // mTotalAnimatedDistance, mText[3]);
        // // mNormalTextPaint.setColor(0xff565656);
        // // drawText(canvas, mNormalTextPaint, center_baseline - 1 *
        // // SCROLL_DISTANCE
        // // + mTotalAnimatedDistance, mText[2]);
        // // drawText(canvas, mNormalTextPaint, center_baseline + 1 *
        // // SCROLL_DISTANCE
        // // + mTotalAnimatedDistance, mText[4]);
        // // mNormalTextPaint.setColor(0xff656565);
        // // drawText(canvas, mNormalTextPaint, center_baseline - 2 *
        // // SCROLL_DISTANCE
        // // + mTotalAnimatedDistance, mText[1]);
        //
        // // drawText(canvas, mNormalTextPaint, center_baseline + 2 *
        // // SMALL_SCROLL_DISTANCE
        // // + mTotalAnimatedDistance, mText[5]);
        //
        // // mNormalTextPaint.setColor(0xffc4c4c4);
        // // drawText(canvas, mNormalTextPaint, center_baseline - 3 *
        // // SCROLL_DISTANCE
        // // + mTotalAnimatedDistance, mText[0]);
        // // drawText(canvas, mNormalTextPaint, center_baseline + 3 *
        // // SMALL_SCROLL_DISTANCE
        // // + mTotalAnimatedDistance, mText[6]);
        // } else {
        // int total = count / 2;
        // for (int i = 1; i <= total; i++) {
        // drawText(canvas, mNormalTextPaint, center_baseline - i *
        // SCROLL_DISTANCE
        // + mTotalAnimatedDistance, mText[total - i]);
        // drawText(canvas, mNormalTextPaint, center_baseline + i *
        // SCROLL_DISTANCE
        // + mTotalAnimatedDistance, mText[total + i]);
        // }
        //
        // drawText(canvas, mLightTextPaint, center_baseline +
        // mTotalAnimatedDistance,
        // mText[total]);
        // }

        MyLog.i("normal_text_size->" + normal_text_size);
        MyLog.i("light_text_size->" + light_text_size);

        if (mMotion == MOTION_SCROLL_ONE_ITEM) {
            mDistanceOfEachAnimation = 4;
            if (mToBottom == true) {
                mMotion = MOTION_ADJUST;
                mToBottom = false;
                Adjust();
            } else {
                invalidate();
                mTotalDistance += 4;
            }
            if (mIsScroll == 1) {
                mMotion = MOTION_STOP;
                mTotalDistance = 0;
                mDistanceOfEachAnimation = 0;
            }
        } else if (mMotion == MOTION_SCROLLING) {
            if (mToBottom == true) {
                mMotion = MOTION_ADJUST;
                mCount = 0;
                mToBottom = false;
            } else {
                mCount++;
                if (mCount % 6 == 0) {
                    mDistanceOfEachAnimation -= 4;
                    mCount = 0;
                }

                if (mDistanceOfEachAnimation < 0) {
                    mDistanceOfEachAnimation = 0;
                    mMotion = MOTION_ADJUST;
                }
            }
            invalidate();
        } else if (mMotion > MOTION_SCROLLING) {
            Adjust();
        }

        drawArrow(canvas);

    }

    private void drawCenterRect(Canvas c) {
        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setColor(0xfff5f5f5);
        c.drawRect(mCenterSeletedRect, paint);
    }

    private void calculateTextPositions() {
        
        if (height_count != -1) {
            mText[0] = getTextToDraw(-1);
            mText[1] = getTextToDraw(-0);
            mText[2] = getTextToDraw(1);
        } else {
            
            mText[0] = getTextToDraw(-3);
            mText[1] = getTextToDraw(-2);
            mText[2] = getTextToDraw(-1);
            mText[3] = getTextToDraw(0);
            mText[4] = getTextToDraw(1);
            mText[5] = getTextToDraw(2);
            mText[6] = getTextToDraw(3);
            
            
        }

    }

    private void drawText(Canvas canvas, Paint paint, float y, String text) {
        if (text != null && !TextUtils.isEmpty(text)) {

            canvas.drawText(text, center_x, y, paint);
        }
    }

    private String getTextToDraw(int offset) {
        // PR829126 by xing.zhao  [Alarm]One miute and one hour more than DateTimePicker  begin
        int index = getNewIndex(offset);
        // PR829126 by xing.zhao [Alarm]One miute and one hour more than DateTimePicker  end
        if (index < 0) {
            return "";
        }
        return mTextList[index];
    }

    private int getNewIndex(int offset) {
        int index = mCurrentSelectedPos + offset;
        if (index < 0) {
            index += mTextList.length;

        } else if (index >= mTextList.length) {
            index -= mTextList.length;

        }
        return index;
    }

    private void Adjust() {
        if (Math.abs(mTotalAnimatedDistance) == 0
                || Math.abs(mTotalAnimatedDistance) == SCROLL_DISTANCE) {
            mDistanceOfEachAnimation = 0;
            mMotion = MOTION_STOP;
        } else if (mMotion == MOTION_ADJUST) {
            mDistanceOfEachAnimation = 1;
            mMotion = MOTION_PROGRESS;
            if (Math.abs(mTotalAnimatedDistance) <= SCROLL_DISTANCE / 2) {
                if (mTotalAnimatedDistance < 0)
                    mScrollMode = SCROLL_MODE_DOWN;
                else
                    mScrollMode = SCROLL_MODE_UP;
            } else {
                if (mTotalAnimatedDistance < 0)
                    mScrollMode = SCROLL_MODE_UP;
                else
                    mScrollMode = SCROLL_MODE_DOWN;
            }
        }

        invalidate();
    }

    public int getCurrentSelectedPos() {
        return mCurrentSelectedPos;
    }

    public String getCurrent() {
        if (mTextList == null) {
            return null;
        } else {
            return mTextList[mCurrentSelectedPos];
        }
    }
}
