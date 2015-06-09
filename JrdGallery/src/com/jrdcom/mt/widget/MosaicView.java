
package com.jrdcom.mt.widget;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.RectF;
import android.graphics.Shader;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import  java.util.ArrayList;

import com.jrdcom.mt.core.ToolMosaic;
import com.jrdcom.mt.util.MyData;

public class MosaicView extends View {

    private static final float FRICTION = 10.0F;
    private static final float MAXSCALE = 3;
    private static final float MINSCALE = 1.0f;

    private Bitmap mBitmap;
    private Bitmap mMaskBitmap;
    private Bitmap mSaveBitmap;
    private Canvas mMaskCanvas;
    private Matrix mMatrix;
    private RectF mBitmapScaleRect;
    private Matrix mBorderMatrix;
    private Matrix mMaskMatrix;
    private Path mPath;
    private Paint mPaint;
    private ToolMosaic m_tool;
    private Bitmap currBitmap;

    private Bitmap orgnalBitmap;
    private TouchMosaicViewListener mTouchListener;
    // private MosaicEventListener mListener;
    /**
     * 画笔的边缘paint
     */
    private Paint paintBorder;
    /**
     * 画笔内部Paint
     */
    private Paint paintContent;
    /**
     * 默认抗锯齿画笔
     */
    private Paint mBitmapPaint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG);
    /**
     * 画笔动作列表，undo、redo用
     */
     private ArrayList<MosaicPaintAction> mPaintActionList = new
      ArrayList<MosaicPaintAction>();

    private boolean isShowDrawPoint = false;
    private boolean mDragging = false;
    private boolean mInitOnMeasure;
    /**
     * 是否展示预览画笔大小
     */
    public boolean isShowCenterPen = false;
    private boolean isPaint = true;
    /**
     * 画笔默认大小
     */
    public int penSize = 15;
    private int mViewWidth;
    private int mViewHeight;
    private int mBitmapWidth;
    private int mBitmapHeight;
    private int mBitmapScaleWidth;
    private int mBitmapScaleHeight;
    private float mScale;
    /**
     * 图片载入缩放比例
     */
    private float bmpScale;
    /**
     * 手势坐标对应Bitmap的位置
     */
    private float mCurrentX;
    private float mCurrentY;
    /**
     * 当前手势坐标
     */
    private float mX;
    private float mY;
    private float mMidX;
    private float mMidY;

    public MosaicView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mInitOnMeasure = false;
        mPath = new Path();
        mMatrix = new Matrix();
        mBorderMatrix = new Matrix();
        mMaskMatrix = new Matrix();
        // mListener = new MosaicEventListener(context, this);
        // setOnTouchListener(mListener);
        setFocusable(true);

        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setDither(false);
        mPaint.setColor(0x7FFFFFFF);

        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeJoin(Paint.Join.ROUND);
        mPaint.setStrokeCap(Paint.Cap.ROUND);
        mPaint.setStrokeWidth(penSize);
        m_tool = new ToolMosaic();
        m_tool.init(MyData.getJNI());
//        m_tool.procMosaic(10);
        orgnalBitmap = m_tool.getShowOralImage();
        setBitmap(orgnalBitmap);

    }

    private float getOptimalScale(int ViewWidth, int ViewHeight, int PicWidth, int PicHeight) {

        float scale;
        scale = ((float) ViewWidth / ViewHeight < (float) PicWidth / PicHeight) ? ((float) PicWidth / ViewWidth)
                : ((float) PicHeight / ViewHeight);
        return scale;
    }

    public void translateMatrix(float offx, float offy) {

        mMatrix.postTranslate(offx, offy);
        mBorderMatrix.postTranslate(offx, offy);
        mMaskMatrix.postTranslate(offx, offy);
        invalidate();
    }

    public void isDrawing(boolean b) {

    }

    // x,y 缩放中心
    public void scaleMatrix(float x, float y, float scalex, float scaley) {

        float scale = 1.0f;
        scale *= scalex;
        mScale *= scalex;
        // 放大完后要平移，�?���?��刷新
        mMatrix.postScale(scale, scale, x, y);
        mBorderMatrix.postScale(scale, scale, x, y);
        mMaskMatrix.postScale(scale, scale, x, y);
    }

    public void midPointCallback(float midX, float midY) {
        mMidX = midX;
        mMidY = midY;
    }

    public void actionCallback(boolean isDragging) {
        mDragging = isDragging;
        invalidate();
    }

    // 是否�?��显示画笔
    public void isShowPoint(boolean b) {
        isShowDrawPoint = b;
    }

    // 画笔的坐标
    public void getCurrentPoint(float i, float j) {
        float[] points = new float[] {
                i, j
        };
        Matrix invertMatrix = new Matrix();
        mMatrix.invert(invertMatrix);
        invertMatrix.mapPoints(points);
        float x = points[0];
        float y = points[1];
        float dx = Math.abs(x - mCurrentX);
        float dy = Math.abs(y - mCurrentY);
        if (dx >= 3 || dy >= 3) {// 根据move时坐标的距离判断是否绘制
            mPath.quadTo(mCurrentX, mCurrentY, (x + mCurrentX) / 2, (y + mCurrentY) / 2);

            mPaint.setStrokeWidth(penSize * bmpScale / mScale);
            mMaskCanvas.drawPath(mPath, mPaint);
            mCurrentX = x;
            mCurrentY = y;
            mX = i;
            mY = j;
        }
        invalidate();
    }

    // 画笔的坐标
    public void getDownPoint(float i, float j) {

        float[] points = new float[] {
                i, j
        };

        if (this.mTouchListener != null)
            this.mTouchListener.onTouch();

        Matrix invertMatrix = new Matrix();
        mMatrix.invert(invertMatrix);
        invertMatrix.mapPoints(points);
        float x = points[0];
        float y = points[1];
        mPath.reset();
        mPath.moveTo(x, y);
        // MTDebug.Print("test", "penSize = " + penSize + "bmpScale = " +
        // bmpScale + "Stroke width = " + penSize
        // * bmpScale);
        mPaint.setStrokeWidth(penSize * bmpScale / mScale);

        mMaskCanvas.drawPath(mPath, mPaint);
        mCurrentX = x;
        mCurrentY = y;
        mX = i;
        mY = j;

        invalidate();
    }

    // 画笔的坐�?
    public void getUpPoint(float i, float j) {
        mPath.lineTo(mCurrentX, mCurrentY);
        // MTDebug.Print("test", "penSize = " + penSize + "bmpScale = " +
        // bmpScale + "penSize * bmpScale = " + penSize
        // * mScale + "penSize / bmpScale = " + penSize / bmpScale);
        mPaint.setStrokeWidth(penSize * bmpScale / mScale);
        mMaskCanvas.drawPath(mPath, mPaint);
        //add biao.luo begin for 470276
        Paint paint = new Paint(mPaint);
        mPaintActionList.add(new MosaicPaintAction(mPath, paint, mScale,isPaint));
        //add biao.luo end
        mPath.reset();
        invalidate();
    }

    // 画笔的坐�?
    public void getDoubleDownPoint() {
        mPath.reset();
        invalidate();
    }

    public void generateNewMaskBitmap(boolean isNeedRedraw) {
        //add biao.luo begin for 470276
//        SafeRelease(mMaskBitmap);
//        mMaskBitmap = Bitmap.createBitmap(mBitmapWidth, mBitmapHeight, Config.ARGB_8888);
//        mMaskCanvas = new Canvas(mMaskBitmap);
        mMaskBitmap.eraseColor(Color.TRANSPARENT);
        //add biao.luo end

        if (isNeedRedraw) {
            
            Shader shader = new BitmapShader(currBitmap, Shader.TileMode.CLAMP,Shader.TileMode.CLAMP);
            Shader shader2 = new BitmapShader(mBitmap,Shader.TileMode.CLAMP,Shader.TileMode.CLAMP);
            for (int i = 0; i < mPaintActionList.size(); i++) {
                 MosaicPaintAction action = mPaintActionList.get(i);
                 if(action.isPaint())
                 {
                     action.setShader(shader);
                 }
                 else
                 {
                     action.setShader(shader2);
                 }
                 action.doPaintAction(mMaskCanvas, null);
            }
        }
        
    }

    public Bitmap generateSaveMaskBitmap() {
        // SafeRelease(mMaskBitmap);
        mSaveBitmap = Bitmap.createBitmap(mBitmapWidth, mBitmapHeight, Config.ARGB_8888);
        Canvas canvas = new Canvas(mSaveBitmap);
        // int count = mPaintActionList.size();
        // // MTDebug.Print("reDraw count  = " + count);
        // for (int i = 0; i < count; i++) {
        // MosaicPaintAction action = mPaintActionList.get(i);
        // action.doSavePaintAction(canvas);
        // }
        return mSaveBitmap;
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        if (!mInitOnMeasure) {
            mViewWidth = w;
            mViewHeight = h;
            bmpScale = getOptimalScale(mViewWidth, mViewHeight, mBitmap.getWidth(),
                    mBitmap.getHeight());
            // MTDebug.Print("beauty", "bmpScale = " + bmpScale);
            mMatrix.setScale(1 / bmpScale, 1 / bmpScale);
            mBitmapScaleWidth = (int) (mBitmapWidth / bmpScale);
            mBitmapScaleHeight = (int) (mBitmapHeight / bmpScale);
            // MTDebug.Print("beauty", "mBitmapWidth =  " + mBitmapWidth +
            // "mBitmapScaleWidth = " + mBitmapScaleWidth);
            int offx = (mViewWidth - mBitmapScaleWidth) / 2;
            int offy = (mViewHeight - mBitmapScaleHeight) / 2;
            mBitmapScaleRect = new RectF(offx, offy, offx + mBitmapScaleWidth, offy
                    + mBitmapScaleHeight);// 定义图片区域的rect
            mMatrix.postTranslate(offx, offy);
            mMaskMatrix.postTranslate(offx, offy);
            mMaskBitmap = Bitmap.createBitmap(mBitmapWidth, mBitmapHeight, Config.ARGB_8888);
            mMaskCanvas = new Canvas(mMaskBitmap);
            mScale = 1.0f;
            mInitOnMeasure = true;
        }

    }

    public void setBitmap(Bitmap bitmap) {
        if (bitmap != null) {
            mBitmap = bitmap.copy(Config.ARGB_8888, true);
            this.SafeRelease(bitmap);
            mBitmapWidth = bitmap.getWidth();
            mBitmapHeight = bitmap.getHeight();
        }

    }

    public float getScale() {
        return mScale;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        // m_tool.procMosaic(10);
        // mMaskBitmap = m_tool.getShowProcImage();
        canvas.drawBitmap(mBitmap, mMatrix, mBitmapPaint);
        canvas.drawBitmap(mMaskBitmap, mMatrix, mBitmapPaint);

        // mListener.setMatrix(mMatrix);
        RectF dst = new RectF();
        mBorderMatrix.mapRect(dst, mBitmapScaleRect);
        if (mDragging)
            return;

        if (paintBorder == null) {
            paintBorder = new Paint(Paint.ANTI_ALIAS_FLAG);
            paintBorder.setStyle(Paint.Style.STROKE);
            paintBorder.setColor(0xffffffff);
            paintBorder.setAntiAlias(true);
            paintBorder.setStrokeWidth(2);
        }

        if (paintContent == null) {
            paintContent = new Paint(Paint.ANTI_ALIAS_FLAG);
            paintContent.setStyle(Paint.Style.FILL);
            paintContent.setColor(0x7fffffff);
            paintContent.setAntiAlias(true);
        }
        if (isShowDrawPoint) {
            canvas.drawCircle(mX, mY, penSize / 2, paintContent);
            canvas.drawCircle(mX, mY, penSize / 2, paintBorder);

        }
        if (isShowCenterPen) {
            mX = this.getWidth() / 2;
            mY = this.getHeight() / 2;
            canvas.drawCircle(mX, mY, penSize / 2, paintContent);
            canvas.drawCircle(mX, mY, penSize / 2, paintBorder);
        }

        // scale <1.0f
        if ((Math.abs((dst.left + dst.right) / 2 - mViewWidth / 2) > 1.0f || Math
                .abs((dst.top + dst.bottom) / 2
                        - mViewHeight / 2) > 1.0f)
                && mScale <= 1.0f) {

            mMatrix.postTranslate(-((dst.left + dst.right) / 2 - mViewWidth / 2) / FRICTION,
                    -((dst.top + dst.bottom) / 2 - mViewHeight / 2) / FRICTION);
            mBorderMatrix.postTranslate(-((dst.left + dst.right) / 2 - mViewWidth / 2) / FRICTION,
                    -((dst.top + dst.bottom) / 2 - mViewHeight / 2) / FRICTION);
            mMaskMatrix.postTranslate(-((dst.left + dst.right) / 2 - mViewWidth / 2) / FRICTION,
                    -((dst.top + dst.bottom) / 2 - mViewHeight / 2) / FRICTION);
            invalidate();
        }

        // 缩放比大�?.0，宽超出显示区域，但长未超出
        else if (mScale > 1.0f && (dst.right - dst.left) > mViewWidth
                && (dst.bottom - dst.top) < mViewHeight) {

            if (dst.left > 0) {
                mMatrix.postTranslate(-dst.left / FRICTION,
                        -((dst.top + dst.bottom) / 2 - mViewHeight / 2) / FRICTION);

                mBorderMatrix.postTranslate(-dst.left / FRICTION,
                        -((dst.top + dst.bottom) / 2 - mViewHeight / 2)
                                / FRICTION);
                invalidate();
            }
            else if (dst.right < mViewWidth) {
                mMatrix.postTranslate((mViewWidth - dst.right) / FRICTION,
                        -((dst.top + dst.bottom) / 2 - mViewHeight / 2) / FRICTION);

                mBorderMatrix.postTranslate((mViewWidth - dst.right) / FRICTION,
                        -((dst.top + dst.bottom) / 2 - mViewHeight / 2) / FRICTION);
                mMaskMatrix.postTranslate((mViewWidth - dst.right) / FRICTION,
                        -((dst.top + dst.bottom) / 2 - mViewHeight / 2) / FRICTION);
                invalidate();
            }
            // 两边都没越界

            else {
                mMatrix.postTranslate(0, -((dst.top + dst.bottom) / 2 - mViewHeight / 2) / FRICTION);
                mBorderMatrix.postTranslate(0, -((dst.top + dst.bottom) / 2 - mViewHeight / 2)
                        / FRICTION);
                mMaskMatrix.postTranslate(0, -((dst.top + dst.bottom) / 2 - mViewHeight / 2)
                        / FRICTION);
                invalidate();
            }

        }

        // 缩放比大�?.0，长超出显示区域，但宽未超出
        else if (mScale > 1.0f && (dst.right - dst.left) < mViewWidth
                && (dst.bottom - dst.top) > mViewHeight) {

            // 上越�?
            if (dst.top > 0) {
                mMatrix.postTranslate(-((dst.right + dst.left) / 2 - mViewWidth / 2) / FRICTION,
                        -dst.top / FRICTION);

                mBorderMatrix.postTranslate(-((dst.right + dst.left) / 2 - mViewWidth / 2)
                        / FRICTION, -dst.top
                        / FRICTION);
                mMaskMatrix.postTranslate(
                        -((dst.right + dst.left) / 2 - mViewWidth / 2) / FRICTION, -dst.top
                                / FRICTION);
                invalidate();

            }
            // 下越�?
            else if (dst.bottom < mViewHeight) {
                mMatrix.postTranslate(-((dst.right + dst.left) / 2 - mViewWidth / 2) / FRICTION,
                        -(dst.bottom - mViewHeight) / FRICTION);

                mBorderMatrix.postTranslate(-((dst.right + dst.left) / 2 - mViewWidth / 2)
                        / FRICTION,
                        -(dst.bottom - mViewHeight) / FRICTION);
                mMaskMatrix.postTranslate(
                        -((dst.right + dst.left) / 2 - mViewWidth / 2) / FRICTION,
                        -(dst.bottom - mViewHeight) / FRICTION);
                invalidate();

            }
            // 两边都没越界
            else {
                mMatrix.postTranslate(-((dst.right + dst.left) / 2 - mViewWidth / 2) / FRICTION, 0);
                mBorderMatrix.postTranslate(-((dst.right + dst.left) / 2 - mViewWidth / 2)
                        / FRICTION, 0);
                mMaskMatrix.postTranslate(
                        -((dst.right + dst.left) / 2 - mViewWidth / 2) / FRICTION, 0);
                invalidate();
            }

        }
        // 缩放比大�?.0，长和宽都超过显示区�?
        else if (mScale > 1.0f && (dst.right - dst.left) > mViewWidth
                && (dst.bottom - dst.top) > mViewHeight) {

            if (dst.top > 0) {
                mMatrix.postTranslate(0, -dst.top / FRICTION);

                mBorderMatrix.postTranslate(0, -dst.top / FRICTION);
                mMaskMatrix.postTranslate(0, -dst.top / FRICTION);
                invalidate();
            }
            if (dst.bottom < mViewHeight) {
                mMatrix.postTranslate(0, (mViewHeight - dst.bottom) / FRICTION);

                mBorderMatrix.postTranslate(0, (mViewHeight - dst.bottom) / FRICTION);
                mMaskMatrix.postTranslate(0, (mViewHeight - dst.bottom) / FRICTION);
                invalidate();
            }
            if (dst.left > 0) {
                mMatrix.postTranslate(-dst.left / FRICTION, 0);

                mBorderMatrix.postTranslate(-dst.left / FRICTION, 0);
                mMaskMatrix.postTranslate(-dst.left / FRICTION, 0);
                invalidate();
            }
            if (dst.right < mViewWidth) {
                mMatrix.postTranslate((mViewWidth - dst.right) / FRICTION, 0);
                mBorderMatrix.postTranslate((mViewWidth - dst.right) / FRICTION, 0);
                mMaskMatrix.postTranslate((mViewWidth - dst.right) / FRICTION, 0);
                invalidate();
            }
        }

        // 小于�?��缩放倍数，由于缩放比变化巨大，所以需要缓和的函数计算每次的微小的缩放�?
        if (mScale - MINSCALE < -0.001f) {
            float scale = 1.0f;
            scale = (float) Math.sqrt(Math.sqrt(Math.sqrt(MINSCALE / mScale)));
            // scale = MathQuadratic1((float) Math.sqrt(MINSCALE / mScale));
            mScale = mScale * scale;
            mMatrix.postScale(scale, scale, mMidX, mMidY);
            mBorderMatrix.postScale(scale, scale, mMidX, mMidY);
            mMaskMatrix.postScale(scale, scale, mMidX, mMidY);
            invalidate();
        }
        // 大于�?��缩放倍数，缩放比变化会缓和很多，�?��用开平方运算比较适合
        else if (mScale - MAXSCALE > 0.001f) {
            float scale = 1.0f;
            scale = (float) Math.sqrt(Math.sqrt(MAXSCALE / mScale));
            mScale = mScale * scale;
            mMatrix.postScale(scale, scale, mMidX, mMidY);
            mBorderMatrix.postScale(scale, scale, mMidX, mMidY);
            mMaskMatrix.postScale(scale, scale, mMidX, mMidY);
            invalidate();
        }
    }

    private boolean SafeRelease(Bitmap bmp) {
        if (bmp != null && !bmp.isRecycled()) {
            bmp.recycle();
            bmp = null;
            return true;
        }
        return false;
    }

    public boolean onTouchEvent(MotionEvent event) {
        super.onTouchEvent(event);
        
        //add biao.luo begin for 470276
        if (isPaint) {
            mPaint.setXfermode(null);
        }else {
            mPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
        }
        //add biao.luo end
        switch (event.getAction()) {
            case MotionEvent.ACTION_MOVE:
                isShowPoint(true);
                getCurrentPoint(event.getX(), event.getY());
                break;
            case MotionEvent.ACTION_DOWN:
                if (isPaint) {
                    mPaint.setShader(new BitmapShader(currBitmap, Shader.TileMode.CLAMP,
                            Shader.TileMode.CLAMP));
                } else {
                    mPaint.setShader(new BitmapShader(mBitmap, Shader.TileMode.CLAMP,
                            Shader.TileMode.CLAMP));
                }
                getDownPoint(event.getX(), event.getY());
                break;
            case MotionEvent.ACTION_UP:
                isShowPoint(false);
                getUpPoint(event.getX(), event.getY());
                break;
            default:
                break;
        }
        return true;
    }

    public void release() {
        SafeRelease(mBitmap);
        SafeRelease(mMaskBitmap);
        SafeRelease(mSaveBitmap);
    }

    // luobiao
    public void setShaderBitmap(boolean shader) {
        isPaint = shader;
    }

    public void setSaveBitmap() {
        Bitmap bmp = null;
        int nWidth = m_tool.getShowImageSize()[0];
        int nHeight = m_tool.getShowImageSize()[1];
        bmp = Bitmap.createBitmap(nWidth, nHeight, Bitmap.Config.ARGB_8888);

        Canvas canvas = new Canvas(bmp);
        Paint paint = new Paint();
        paint.setColor(Color.WHITE);
        paint.setAntiAlias(true);
        int cx = (int) (nWidth * 0.5);
        int cy = (int) (nHeight * 0.5);
        int radius = 30;
        canvas.drawCircle(cx, cy, radius, paint);
        invalidate();
    }

    public void setPaint(Paint paint) {
        // TODO Auto-generated method stub
        mPaint = paint;
    }

    public void setTouchMosaicViewListener(TouchMosaicViewListener listener) {
        this.mTouchListener = listener;
    }

    public interface TouchMosaicViewListener {
        public void onTouch();
        
    }
    public void changeMosaicPaint()
    {
        generateNewMaskBitmap(true);
        invalidate();
    }
    
    private int[] pensizes = {
            15, 30, 50, 70, 90
    };

    public void setPenSize(int type)
    {
        penSize = pensizes[type];
    }
    public void setCurrBitmap(Bitmap temp)
    {
//        m_tool.procMosaicWithTexture(5,temp);
        this.currBitmap = temp;
    }
    public float getbmpScale()
    {
        return bmpScale;
    }
    public Bitmap getMasaicBitmap() {
        return this.mMaskBitmap;
    }
    public void onOK() {
        m_tool.ok(mMaskBitmap);
        MyData.getBeautyControl().pushImage();
    }
    
    public void onCancel() {
        m_tool.cancel();
    }
    public ToolMosaic getMoasicTools()
    {
        return m_tool;
    }
}
