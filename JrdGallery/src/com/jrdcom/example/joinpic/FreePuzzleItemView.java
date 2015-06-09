
package com.jrdcom.example.joinpic;

import android.content.Context;
import android.graphics.*;
import android.graphics.drawable.NinePatchDrawable;
import android.os.Handler;
import android.util.FloatMath;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.AnimationSet;
import android.view.animation.RotateAnimation;
import android.view.animation.ScaleAnimation;
import android.view.animation.TranslateAnimation;
import com.jrdcom.android.gallery3d.R;

//import com.mt.mtxx.mtxx.R;
//import com.mt.mtxx.operate.MTDebug;
//import com.mt.mtxx.operate.MyData;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

public class FreePuzzleItemView extends View {
    // ===========================================================
    // Constants
    // ===========================================================
    private static final int NONE = 0;
    private static final int DRAG = 1;
    private static final int ZOOM = 2;
    private static final int BIGGER = 3;
    private static final int SMALLER = 4;
    private static final int DELETE = 5;
    private int mode = NONE;
    private static final int CTR_INDEX_NONE = -1;
    private static final int CTR_INDEX_LEFT_TOP = 0;
    private static final int CTR_INDEX_RIGHT_TOP = 1;
    private static final int CTR_INDEX_RIGHT_BOTTOM = 2;
    private static final int CTR_INDEX_LEFT_BOTTOM = 3;
    private static final int CTR_INDEX_MID_MID = 4;
    private static final int OPT_TRANSLATE = 0;
    private static final int OPT_SCALE = 1;
    private static final int OPT_ROTATE = 2;
    // ===========================================================
    // Fields
    // ===========================================================
    /**
     * 目标操作图片
     */
    private Bitmap mTargetBmp = null;
    // /**
    // * 操作杆bmp
    // */
    // private Bitmap mControlZoomBmp = null;
    // /**
    // * 操作杆bmp
    // */
    // private Bitmap mControlDelBmp = null;
    /**
     * 选中边框画笔
     */
    private Paint mPaintFrame = null;
    private Paint mPaintRect = null;
    /**
     * 操作杆bmp的宽高
     */
    private int mControlBmpWidth = 0;
    private int mControlBmpHeight = 0;
    /**
     * 操作目标bmp的宽高
     */
    private int mTargetBmpWidth = 0;
    private int mTargetBmpHeight = 0;
    /**
     * 图片矩阵
     */
    private Matrix mMatrix = null;
    /**
     * 当前操作的图片中心点
     */
    private Point mPrePivot = null;
    /**
     * 上一次操作的图片中心点
     */
    private Point mLastPivot = null;
    /**
     * 移动操作记录XY坐标
     */
    private Point mLastPoint = null;
    private RectF mSrcRect = null;

    private RectF mDstRect = null;
    /**
     * 初始载入图片的point[]
     */
    private float[] mSrcPoint = null;
    /**
     * 操作后的Point[]
     */
    private float[] mDstPoint = null;

    private float mPreDegree = 0f;

    private float mLastDegree = 0f;

    private float[] mPrePoint = null;

    /**
     * 所需要移动的距离
     */
    private float mDeltaX = 0;

    private float mDeltaY = 0;

    private float mScale = 1f;

    private float mDegree = 0f;
    /**
     * 缩放大小
     */
    private float mScaleValue = 1f;
    /**
     * 初始缩放大小
     */
    private float mScaleDefault = 1f;
    /**
     * 旋转角度
     */
    private float mRotateDegreeValue = 0f;
    private float mLastDist = 1f;
    private float mPreDist = 1f;
    /**
     * 图片类型
     */
    private int mPhotoType;
    /**
     * 图片所在的层级
     */
    private int mIndex;
    /**
     * 是否选中
     */
    private boolean isSelected = false;
    private boolean isMoved = false;
    /**
     * 是否锁定编辑
     */
    private boolean isLock = false;
    private PaintFlagsDrawFilter drawFilter;
    /**
     * 图片路径path
     */
    private String mPhotoPath;
    private onClickRemoveViewListener mOnClickRemoveViewListener;
    private onMtDoubleTapListenerListener mDoubleTapListenerListener;
    /**
     * 默认画笔
     */
    private Paint mPaint = null;
    /**
     * 手势
     */
    // private GestureDetector gestureDetector;
    private boolean isDoubleTapProcing = false;

    /**
     * 白色边框
     */
    private Rect mBoundRect = null;// 白色边框的Rect
    private NinePatchDrawable mBoundDrawable = null;// 白色边框的素材
    /**
     * 唯一标记id
     * 
     * @param context
     */
    private int mUniqueKey;

    private boolean isInDelAnimation = false;

    private boolean isWhiteFrame = false;
    private boolean isShadeFrame = false;
    private int mRealPhotoWidth = 0;
    private int mRealPhotoHeight = 0;
    private boolean is2Edited = false;

    private OnTouchesListener mPointDownListener = null;

    /**
     * 节点在版式里面的位置
     */
    private int mLayoutItemIndex = -1;

    /**
     * 自由拼图节点视图唯一构造函数
     * 
     * @param context 上下文对象
     * @param pItemIndex 节点在自由拼图版式里面的位置。
     */
    public FreePuzzleItemView(Context context, int pItemIndex) {
        super(context);

        mPaintFrame = new Paint();
        mPaintFrame.setAntiAlias(true);
        mPaintFrame.setFilterBitmap(true);
        mPaintFrame.setDither(true);
        mPaintFrame.setColor(Color.RED);
        mPaintFrame.setStyle(Paint.Style.STROKE);

        this.mLayoutItemIndex = pItemIndex;
        // gestureDetector = new GestureDetector(new GestureListener());

        setWillNotDraw(false);

        setUniqueKey();
    }

    /**
     * Get layout item index.
     * 
     * @return the index value.
     */
    public int getLayoutItemIndex() {
        return mLayoutItemIndex;
    }

    /**
     * Set item layout index .
     * 
     * @param pItemIndex the index.
     */
    public void setLayoutItemIndex(int pItemIndex) {
        this.mLayoutItemIndex = pItemIndex;
    }

    // ===========================================================
    // Methods
    // ===========================================================
    public void setBitmap(Bitmap pBitmap) {
        /*
         * 图片控制点 0-------1 | | | 4 | | | 3-------2
         */

        // mControlZoomBmp = BitmapFactory.decodeResource(getResources(),
        // R.drawable.rotate);
        // mControlDelBmp = BitmapFactory.decodeResource(getResources(),
        // R.drawable.delete);
        mTargetBmp = pBitmap;
        // mControlBmpWidth = mControlZoomBmp.getWidth();
        // mControlBmpHeight = mControlZoomBmp.getHeight();
        mTargetBmpWidth = mTargetBmp.getWidth();
        mTargetBmpHeight = mTargetBmp.getHeight();

        mSrcPoint = new float[] {
                0, 0, mTargetBmpWidth, 0, mTargetBmpWidth, mTargetBmpHeight, 0, mTargetBmpHeight,
                mTargetBmpWidth / 2, mTargetBmpHeight / 2
        };

        mPrePoint = new float[2];
        mDstPoint = mSrcPoint.clone();
        mSrcRect = new RectF(0, 0, mTargetBmpWidth, mTargetBmpHeight);

        int frameWidth = 0;// (int)
                           // this.getContext().getResources().getDimension(R.dimen.puzzlefree_whiteframe_width);
        frameWidth = 13;

        mBoundRect = new Rect(0, 0, mTargetBmpWidth, mTargetBmpHeight);
        mBoundRect.inset(-frameWidth, -frameWidth);

        mBoundDrawable = (NinePatchDrawable)
        this.getContext().getResources().getDrawable(R.drawable.ic_white_shadow);
        mBoundDrawable.setBounds(mBoundRect);
        mDstRect = new RectF();
        mMatrix = new Matrix();
        mPrePivot = new Point(mTargetBmpWidth / 2, mTargetBmpHeight / 2);
        mLastPivot = new Point(mTargetBmpWidth / 2, mTargetBmpHeight / 2);
        mLastPoint = new Point(0, 0);

        mPaintRect = new Paint();
        mPaintRect.setColor(Color.RED);
        mPaintRect.setAlpha(100);
        mPaintRect.setAntiAlias(true);

        mPaint = new Paint();
        mPaint.setDither(true);
        mPaint.setAntiAlias(true);
        mPaint.setFilterBitmap(true);
    }

    @Override
    protected void onDraw(Canvas canvas) {

        if (drawFilter == null) {
            drawFilter = new PaintFlagsDrawFilter(0, Paint.ANTI_ALIAS_FLAG
                    | Paint.FILTER_BITMAP_FLAG);
        }

        canvas.setDrawFilter(drawFilter);

        if (mBoundDrawable != null) {
            canvas.save();
            canvas.concat(mMatrix);
            mBoundDrawable.draw(canvas);
            canvas.restore();
        }

        if (null != mTargetBmp) {
            // MTDebug.Print("test", "mTargetBmp   is !!!!null  " );
            canvas.drawBitmap(mTargetBmp, mMatrix, null);
        }

        // if (isSelected && this.mPhotoType !=
        // PuzzleConstant.STYLE_DECOPIC_PHOTO) {
//         drawFrame(canvas);
        // drawControlPoints(canvas);
        // }
    }

    /**
     * 绘制控制按钮
     * 
     * @param canvas
     */
    private void drawControlPoints(Canvas canvas) {
        // if (BitmapUtil.isValid(mControlDelBmp))
        // canvas.drawBitmap(mControlDelBmp, mDstPoint[0] - mControlBmpWidth /
        // 2, mDstPoint[1] - mControlBmpHeight / 2,
        // mPaint);
        // if (BitmapUtil.isValid(mControlZoomBmp))
        // canvas.drawBitmap(mControlZoomBmp, mDstPoint[4] - mControlBmpWidth /
        // 2, mDstPoint[5] - mControlBmpHeight / 2,
        // mPaint);
    }

    /**
     * 绘制边框
     * 
     * @param canvas
     */
    private void drawFrame(Canvas canvas) {
        canvas.drawLine(mDstPoint[0], mDstPoint[1], mDstPoint[2], mDstPoint[3], mPaintFrame);
        canvas.drawLine(mDstPoint[2], mDstPoint[3], mDstPoint[4], mDstPoint[5], mPaintFrame);
        canvas.drawLine(mDstPoint[4], mDstPoint[5], mDstPoint[6], mDstPoint[7], mPaintFrame);
        canvas.drawLine(mDstPoint[6], mDstPoint[7], mDstPoint[0], mDstPoint[1], mPaintFrame);
    }

    private void setMatrix(int operationType) {
        switch (operationType) {
            case OPT_SCALE:
                mMatrix.postScale(mScaleValue, mScaleValue, mDstPoint[8], mDstPoint[9]);
                break;
            case OPT_TRANSLATE:
                mMatrix.postTranslate(mDeltaX, mDeltaY);
                break;
            case OPT_ROTATE:
                mMatrix.postRotate(mPreDegree - mLastDegree, mDstPoint[8], mDstPoint[9]);
                break;
            default:
                break;
        }

        mMatrix.mapPoints(mDstPoint, mSrcPoint);
        mMatrix.mapRect(mDstRect, mSrcRect);
    }

    /**
     * 是否在可以相应区域内
     * 
     * @param x
     * @param y
     * @return
     */
    private boolean isOnResponseRect(int x, int y) {

        if (isInFrameRect(x, y)) {
            return true;
        }

        if (isOnZoomCtrolBmp(x, y) != CTR_INDEX_NONE) {
            return true;
        }

        if (isOnDelCtrolBmp(x, y) != CTR_INDEX_NONE) {
            return true;
        }

        return false;
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        int x = (int) event.getX();
        int y = (int) event.getY();

        if (isLock()) {
            return false;
        }

        switch (event.getAction() & MotionEvent.ACTION_MASK) {

            case MotionEvent.ACTION_DOWN:

                if (!isOnResponseRect(x, y)) {
                    isSelected = false;
                    invalidate();
                    return false;
                }

                if (isOnDelCtrolBmp(x, y) != CTR_INDEX_NONE) {
                    // mode = DELETE;
                    // doRemoveAction();
                    // return true;
                } else if (isOnZoomCtrolBmp(x, y) != CTR_INDEX_NONE) {
                    mLastDegree = computeDegree(new Point((int) event.getX(), (int) event.getY()),
                            new Point(
                                    (int) mDstPoint[8], (int) mDstPoint[9]));
                    mode = ZOOM;
                } else {
                    mode = DRAG;
                }

                isMoved = false;

                if (mPointDownListener != null) {
                    mPointDownListener.onTouchBegan(this);
                }

                break;
            case MotionEvent.ACTION_POINTER_DOWN:
                isMoved = true;
                // 计算两手指间的距离
                mLastDist = spacing(event);
                mLastDegree = computeDegree(new Point((int) event.getX(0), (int) event.getY(0)),
                        new Point((int) event.getX(1), (int) event.getY(1)));
                mode = ZOOM;
                break;
            case MotionEvent.ACTION_POINTER_UP:
                mode = NONE;
                break;
            case MotionEvent.ACTION_UP:
                mode = NONE;
                break;
            case MotionEvent.ACTION_MOVE:

                isSelected = true;
                if (mode == DRAG) {
                    // 单指移动
                    translate(x, y);

                } else if (mode == ZOOM) {
                    isMoved = true;
                    rotate(event);
                    scale(event);
                }
                break;

            default:
                break;
        }
        mLastPoint.x = x;
        mLastPoint.y = y;
        invalidate();
        return true;
    }

    private void doRemoveAction() {
        if (mOnClickRemoveViewListener != null) {
            mOnClickRemoveViewListener.doRemoveViewAction(this);
        }
    }

    private void setTranslateValue(float x, float y) {

        mDeltaX = x - mPrePivot.x;
        mDeltaY = y - mPrePivot.y;

        mPrePivot.x = (int) x;
        mPrePivot.y = (int) y;
        mLastPivot.x = mPrePivot.x;
        mLastPivot.y = mPrePivot.y;

        mMatrix.postTranslate(mDeltaX, mDeltaY);

        mMatrix.mapPoints(mDstPoint, mSrcPoint);
        mMatrix.mapRect(mDstRect, mSrcRect);
    }

    private void setScaleValue(float scale, float centerX, float centerY, boolean isScaleByCenterXY) {
        mScale = scale;
        mScaleValue = scale;
        if (isScaleByCenterXY) {
            mMatrix.postScale(mScaleValue, mScaleValue, centerX, centerY);
        } else {
            mMatrix.postScale(mScaleValue, mScaleValue, mDstPoint[8], mDstPoint[9]);
        }
        mMatrix.mapPoints(mDstPoint, mSrcPoint);
        mMatrix.mapRect(mDstRect, mSrcRect);
    }

    /**
     * 旋转
     * 
     * @param degree
     * @param centerX
     * @param centerY
     * @param isRotateByCenterXY
     */
    private void setRotateDegreeValue(float degree, float centerX, float centerY,
            boolean isRotateByCenterXY) {
        mRotateDegreeValue = degree;
        mDegree = degree;
        if (isRotateByCenterXY) {
            mMatrix.postRotate(mRotateDegreeValue, centerX, centerY);
        } else {
            mMatrix.postRotate(mRotateDegreeValue, mDstPoint[8], mDstPoint[9]);
        }
        mMatrix.mapPoints(mDstPoint, mSrcPoint);
        mMatrix.mapRect(mDstRect, mSrcRect);
    }

    /**
     * 设置样式
     * 
     * @param x 偏移
     * @param y 偏移
     * @param scale
     * @param angle
     */
    public void setStyle(float x, float y, float scale, float angle) {
        mScaleDefault = scale;
        setTranslateValue(x, y);
        setScaleValue(scale, 0f, 0f, false);
        setRotateDegreeValue(angle, 0f, 0f, false);
        // startScaleAnimation();
    }

    private void startScaleAnimation() {
        ScaleAnimation localScaleAnimation = new ScaleAnimation(1.0f / mScale, 1.0f, 1.0f / mScale,
                1.0f, mDstPoint[8],
                mDstPoint[9]);
        localScaleAnimation.setDuration(800L);
        this.startAnimation(localScaleAnimation);
    }

    /**
     * 切换板式并执行动画
     * 
     * @param x 偏移
     * @param y 偏移
     * @param scale
     * @param angle
     */
    public void postChangeStyle(float x, float y, float scale, float angle) {
        mDeltaX = x - mPrePivot.x;
        mDeltaY = y - mPrePivot.y;

        mPrePivot.x = (int) x;
        mPrePivot.y = (int) y;
        float x1 = mDstRect.left - (mDstRect.right - mDstRect.left) / 2;
        float y1 = mDstRect.top - (mDstRect.bottom - mDstRect.top) / 2;
        mLastPivot.x = mPrePivot.x;
        mLastPivot.y = mPrePivot.y;
        // 先还原到原图比例再乘当前比例
        mScaleValue = 1.0f / mScale * scale;
        mScale = scale;
        // 先还原到原图角度再加上当前角度
        mRotateDegreeValue = -mDegree + angle;
        mDegree = angle;
        if (Math.abs(mRotateDegreeValue) > 180) {
            if (mRotateDegreeValue > 0) {
                mRotateDegreeValue = mRotateDegreeValue - 360;
            } else {
                mRotateDegreeValue = 360 + mRotateDegreeValue;
            }
        } else {
        }
        setScale(mScaleValue, mRotateDegreeValue);
        handler.postDelayed(new Runnable() {

            public void run() {
                setTranslate(mDeltaX, mDeltaY);
            }
        }, 300);
    }

    private void setTranslate(float mDeltaX, float mDeltaY) {
        mMatrix.postTranslate(mDeltaX, mDeltaY);
        mMatrix.mapPoints(mDstPoint, mSrcPoint);
        mMatrix.mapRect(mDstRect, mSrcRect);
        this.invalidate();
        this.clearAnimation();
        TranslateAnimation localTranslateAnimation = new TranslateAnimation(-mDeltaX, 0, -mDeltaY,
                0);
        localTranslateAnimation.setDuration(300L);
        this.startAnimation(localTranslateAnimation);

    }

    private void setScale(float scale, float degree) {
        mMatrix.postScale(scale, scale, mDstPoint[8], mDstPoint[9]);
        mMatrix.mapPoints(mDstPoint, mSrcPoint);
        mMatrix.mapRect(mDstRect, mSrcRect);
        mMatrix.postRotate(degree, mDstPoint[8], mDstPoint[9]);
        mMatrix.mapPoints(mDstPoint, mSrcPoint);
        mMatrix.mapRect(mDstRect, mSrcRect);
        // ItemView.this.setImageMatrix(mMatrix);
        invalidate();
        clearAnimation();
        ScaleAnimation localScaleAnimation = new ScaleAnimation(1.0f / scale, 1.0f, 1.0f / scale,
                1.0f, mDstPoint[8],
                mDstPoint[9]);
        localScaleAnimation.setDuration(300L);
        RotateAnimation localRotateAnimation = new RotateAnimation(-degree, 0, mDstPoint[8],
                mDstPoint[9]);
        // localRotateAnimation.setStartOffset(3000L);
        localRotateAnimation.setDuration(300L);
        AnimationSet localAnimationSet = new AnimationSet(false);
        // localAnimationSet.addAnimation(localTranslateAnimation);
        localAnimationSet.addAnimation(localScaleAnimation);
        localAnimationSet.addAnimation(localRotateAnimation);
        this.startAnimation(localAnimationSet);
    }

    Handler handler = new Handler();

    public void translate(int x, int y) {

        mPrePivot.x += x - mLastPoint.x;
        mPrePivot.y += y - mLastPoint.y;
        mDeltaX = mPrePivot.x - mLastPivot.x;
        mDeltaY = mPrePivot.y - mLastPivot.y;

        if ((Math.abs(mDeltaX) > 5f || Math.abs(mDeltaY) > 5f)) {
            isMoved = true;
        }

        mLastPivot.x = mPrePivot.x;
        mLastPivot.y = mPrePivot.y;
        setMatrix(OPT_TRANSLATE);
        is2Edited = true;
    }

    public void rotate(MotionEvent event) {
        if (event.getPointerCount() == 2) {
            mPreDegree = computeDegree(new Point((int) event.getX(0), (int) event.getY(0)),
                    new Point((int) event.getX(1), (int) event.getY(1)));
        } else {
            mPreDegree = computeDegree(new Point((int) event.getX(), (int) event.getY()),
                    new Point(
                            (int) mDstPoint[8], (int) mDstPoint[9]));
        }
        setMatrix(OPT_ROTATE);
        mDegree = (mDegree + (mPreDegree - mLastDegree)) % 360;
        mLastDegree = mPreDegree;
        is2Edited = true;
    }

    public void scale(MotionEvent event) {

        if (event.getPointerCount() == 2) {
            mPreDist = spacing(event);
            mScaleValue = 1 + (mPreDist - mLastDist) / 200;
            // if (mScaleValue > 2 / mScale) {// 限定放大的比例 2
            // mScaleValue = 2 / mScale;
            // }
            // if ((mTargetBmp.getWidth() * mScale * mScaleValue >=
            // MyData.nScreenW ||
            // mTargetBmp.getHeight() * mScale * mScaleValue >=
            // MyData.nScreenH)) {
            // return;
            // }
        } else {
            int pointIndex = 4;
            float px = mDstPoint[pointIndex];
            float py = mDstPoint[pointIndex + 1];

            float evx = event.getX();
            float evy = event.getY();

            float oppositeX = 0;
            float oppositeY = 0;
            oppositeX = mDstPoint[pointIndex + 4];
            oppositeY = mDstPoint[pointIndex + 5];
            float temp1 = getDistanceOfTwoPoints(px, py, oppositeX, oppositeY);
            float temp2 = getDistanceOfTwoPoints(evx, evy, oppositeX, oppositeY);

            this.mScaleValue = temp2 / temp1;
        }

        // if ((mTargetBmp.getWidth() * mScale * mScaleValue >= MyData.nScreenW
        // ||
        // mTargetBmp.getHeight() * mScale * mScaleValue >= MyData.nScreenH)) {
        // return;
        // }

        // if (mScale * mScaleValue <= mScaleDefault) {
        // if ((mTargetBmp.getWidth() * mScale * mScaleValue < 45 *
        // MyData.nDensity ||
        // mTargetBmp.getHeight() * mScale * mScaleValue < 45 *
        // MyData.nDensity)) {
        // return;
        // }
        // }

        setMatrix(OPT_SCALE);
        mScale = mScale * mScaleValue;
        mLastDist = mPreDist;
        is2Edited = true;
    }

    private float getDistanceOfTwoPoints(float x1, float y1, float x2, float y2) {
        return (float) (Math.sqrt((x1 - x2) * (x1 - x2) + (y1 - y2) * (y1 - y2)));
    }

    private int isOnZoomCtrolBmp(int x, int y) {

        Rect rect = new Rect((int) mDstPoint[4] - mControlBmpWidth, (int) mDstPoint[5]
                - mControlBmpHeight,
                (int) mDstPoint[4] + mControlBmpWidth, (int) mDstPoint[5] + mControlBmpHeight);

        if (rect.contains(x, y)) {
            return CTR_INDEX_RIGHT_BOTTOM;
        }

        return CTR_INDEX_NONE;
    }

    private int isOnDelCtrolBmp(int x, int y) {

        Rect rect = new Rect((int) mDstPoint[0] - mControlBmpWidth / 2, (int) mDstPoint[1]
                - mControlBmpHeight / 2,
                (int) mDstPoint[0] + mControlBmpWidth / 2, (int) mDstPoint[1] + mControlBmpHeight
                        / 2);

        if (rect.contains(x, y)) {
            return CTR_INDEX_LEFT_TOP;
        }

        return CTR_INDEX_NONE;
    }

    public float computeDegree(Point p1, Point p2) {
        float tran_x = p1.x - p2.x;
        float tran_y = p1.y - p2.y;
        float degree = 0.0f;
        float angle = (float) (Math.asin(tran_x / Math.sqrt(tran_x * tran_x + tran_y * tran_y)) * 180 / Math.PI);
        if (!Float.isNaN(angle)) {
            if (tran_x >= 0 && tran_y <= 0) {// 第一象限
                degree = angle;
            } else if (tran_x <= 0 && tran_y <= 0) {// 第二象限
                degree = angle;
            } else if (tran_x <= 0 && tran_y >= 0) {// 第三象限
                degree = -180 - angle;
            } else if (tran_x >= 0 && tran_y >= 0) {// 第四象限
                degree = 180 - angle;
            }
        }
        return degree;
    }

    private boolean isInFrameRect(int x, int y) {
        boolean isInRect = false;

        if ((isPointUpLine(mDstPoint[0], mDstPoint[1], mDstPoint[2], mDstPoint[3], x, y) ^ (isPointUpLine(
                mDstPoint[4], mDstPoint[5], mDstPoint[6], mDstPoint[7], x, y)))
                && (isPointUpLine(mDstPoint[2], mDstPoint[3], mDstPoint[4], mDstPoint[5], x, y) ^ (isPointUpLine(
                        mDstPoint[6], mDstPoint[7], mDstPoint[0], mDstPoint[1], x, y)))) {
            isInRect = true;
        }

        return isInRect;

    }

    private boolean isPointUpLine(float x1, float y1, float x2, float y2, float x, float y) {
        float n = 0;
        if (x1 == x2) {
            if (x > x1) {
                return false;
            } else {
                return true;
            }
        } else {
            n = y1 - (x1 - x) * (y1 - y2) / (x1 - x2);
            if (n > y) {
                return true;
            } else {
                return false;
            }
        }
    }

    /**
     * 计算两手指间的距离
     * 
     * @param event
     * @return
     */
    private float spacing(MotionEvent event) {
        try {
            float x = event.getX(0) - event.getX(1);
            float y = event.getY(0) - event.getY(1);
            return FloatMath.sqrt(x * x + y * y);
        } catch (Exception e) {
            return 0;
        }
    }

    public void destroy() {
        // BitmapUtil.SafeRelease(mTargetBmp);
        // BitmapUtil.SafeRelease(mControlZoomBmp);
        // BitmapUtil.SafeRelease(mControlDelBmp);
        // setImageBitmap(null);
    }

    public Matrix getMatrixs() {
        return mMatrix;
    }

    /**
     * 0-xOffset, 1-yOffset, 2-scale, 3-degree
     * 
     * @return
     */
    public float[] getMatrixsInfo() {
        float[] info = new float[4];

        info[0] = this.mLastPivot.x;
        info[1] = this.mLastPivot.y;
        info[2] = mScale;
        info[3] = mDegree;
        //
        // Log.e("getMatrixsInfo", " mScale = " + this.mScale);
        // Log.e("getMatrixsInfo", " mDegree = " + this.mDegree);
        // Log.e("getMatrixsInfo", " mLastPivotx = " + this.mLastPivot.x);
        // Log.e("getMatrixsInfo", " mLastPivoty = " + this.mLastPivot.y);

        return info;
    }

    public int getBitmapWidth() {
        return this.mTargetBmpWidth;
    }

    public int getBitmapHeight() {
        return this.mTargetBmpHeight;
    }

    public boolean isSelected() {
        return isSelected;
    }

    public void setSelected(boolean flag) {
        this.isSelected = flag;
    }

    public boolean isLock() {
        return isLock;
    }

    public void setLock(boolean flag) {
        this.isLock = flag;
    }

    public String getPhotoPath() {
        return mPhotoPath;
    }

    public void setPhotoPath(String path) {
        this.mPhotoPath = path;
    }

    public int getPhotoType() {
        return mPhotoType;
    }

    public void setPhotoType(int type) {
        this.mPhotoType = type;
    }

    public int getIndex() {
        return mIndex;
    }

    public void setIndex(int index) {
        this.mIndex = index;
    }

    public boolean isMove() {
        return isMoved;
    }

    public void setOnClickRemoveViewListener(onClickRemoveViewListener listener) {
        mOnClickRemoveViewListener = listener;
    }

    public void setOnDoubleTapListener(onMtDoubleTapListenerListener listener) {
        mDoubleTapListenerListener = listener;
    }

    public int getUniqueKey() {
        return mUniqueKey;
    }

    public void setUniqueKey() {
        SimpleDateFormat sDateFormat = new SimpleDateFormat(
                "ddhhmmss");
        String date = sDateFormat.format(new Date());
        this.mUniqueKey = Integer.parseInt(date);
    }

    public boolean isWhiteFrame() {
        return isWhiteFrame;
    }

    public void setWhiteFrame(boolean isWihteFrame) {
        this.isWhiteFrame = isWihteFrame;
    }

    public boolean isShadeFrame() {
        return isShadeFrame;
    }

    public void setShadeFrame(boolean isShadeFrame) {
        this.isShadeFrame = isShadeFrame;
    }

    public int getRealPhotoWidth() {
        return mRealPhotoWidth;
    }

    public void setRealPhotoWidth(int mRealPhotoWidth) {
        this.mRealPhotoWidth = mRealPhotoWidth;
    }

    public int getRealPhotoHeight() {
        return mRealPhotoHeight;
    }

    public void setRealPhotoHeight(int mRealPhotoHeight) {
        this.mRealPhotoHeight = mRealPhotoHeight;
    }

    public boolean getSelectFlag() {
        return this.isSelected;
    }

    public void setSelectFlag(boolean flag) {
        this.isSelected = flag;
    }

    public void setOnTouchesListener(OnTouchesListener listener) {
        this.mPointDownListener = listener;
    }

    // ===========================================================
    // Inner and Anonymous Classes
    // ===========================================================

    public boolean isInDelAnimation() {
        return isInDelAnimation;
    }

    public void setInDelAnimation(boolean isInDelAnimation) {
        this.isInDelAnimation = isInDelAnimation;
    }

    public boolean isEdited() {
        return is2Edited;
    }

    public void setEditedFlag(boolean is2Edited) {
        this.is2Edited = is2Edited;
    }

    public interface OnTouchesListener {
        public void onTouchBegan(FreePuzzleItemView view);
    }

    public interface onClickRemoveViewListener {
        public void doRemoveViewAction(FreePuzzleItemView view);
    }

    public interface onMtDoubleTapListenerListener {
        public void doDoubleTapAction(FreePuzzleItemView view);
    }

    class GestureListener extends SimpleOnGestureListener {
        @Override
        public boolean onDoubleTap(MotionEvent e) {
            boolean isClickInTextSprite = isInFrameRect((int) e.getX(), (int) e.getY());
            if (isClickInTextSprite) {
                if (isDoubleTapProcing) {
                    return true;
                }
                isDoubleTapProcing = true;

                if (mDoubleTapListenerListener != null) {
                    mDoubleTapListenerListener.doDoubleTapAction(FreePuzzleItemView.this);
                }

                TimerTask tasks = new TimerTask() {
                    @Override
                    public void run() {
                        isDoubleTapProcing = false;
                    }
                };
                Timer timer = new Timer();
                timer.schedule(tasks, 800);
            }

            return true;
        }

    }

    public Point getLastPoint()
    {
        return mLastPivot;
    }
    public float getRotation()
    {
        return mDegree;
    }
    public float getScale()
    {
        return mScale;
    }
}
