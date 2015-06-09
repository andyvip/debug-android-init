package com.jrdcom.mt.widget;


/**
 * @author yaogang.hao
 * vertical seek bar
 * you can register listener's progress to notify your activity
 */


import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.Shader;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.MeasureSpec;
import com.jrdcom.android.gallery3d.R;

public class VSeekBar extends View {
    
    /**************************************
     * some necessary variables
     **************************************/
    
    private Bitmap backBitmap;
    private Bitmap maskBitmap;
    private Bitmap thumbBitmap;
    
    private int mWidth;
    private int mHeight;
    private int thumbWidth;
    private int thumbHeight;
    
    private Paint paint= new Paint(Paint.ANTI_ALIAS_FLAG|Paint.DITHER_FLAG);
    private Matrix matrix = new Matrix();
    private Paint maskPaint = new Paint();
    private Shader backShader ;
    private Shader progressShader;
    private Rect maskRect;
    private int progress = 100;
    private int maxProgress = 100;
    private PointF thumbPoint;
    private PointF lastDragPoint;
    
    private OnProgressChangedListener progressChangedListener;
    
    public VSeekBar(Context context, AttributeSet attrs) {
        super(context, attrs);
        initMeterials();
    }

    public VSeekBar(Context context) {
        this(context, null);
        initMeterials();
    }
    
    /**************************************
     * initialize some necessary materials
     **************************************/
    
    public void initMeterials()
    {
        //note: here you must use drawable to get dimension size ,because of different dimensions;
        
        Drawable backDrawable = getResources().getDrawable(R.drawable.ic_seekbar_bg);
        mHeight = backDrawable.getIntrinsicWidth();
        mWidth = backDrawable.getIntrinsicHeight();
        
        Drawable thumbdDrawable = getResources().getDrawable(R.drawable.ic_seekbar_thumb);
        thumbWidth = thumbdDrawable.getIntrinsicWidth();
        thumbHeight = thumbdDrawable.getIntrinsicHeight();
        
        Bitmap backBitmapTemp = BitmapFactory.decodeResource(getResources(), R.drawable.ic_seekbar_bg);
        Bitmap  maskBitmapTemp = BitmapFactory.decodeResource(getResources(), R.drawable.ic_seekbar_rate_progress);
        thumbBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.ic_seekbar_thumb);
        matrix.postRotate(-90.f, 0, 0);
        
        //note: here i rotate the original bitmap which is horizontal.we need vertical bar.
        
        backBitmap = Bitmap.createBitmap(mWidth, mHeight, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(backBitmap);
        canvas.rotate(-90);
        canvas.translate(-mHeight,0);
        canvas.drawBitmap(backBitmapTemp, 0,0, paint);
        maskBitmap = Bitmap.createBitmap(mWidth, mHeight, Bitmap.Config.ARGB_8888);
         canvas = new Canvas(maskBitmap);
        canvas.rotate(-90);
        canvas.translate(-mHeight,0);
        canvas.drawBitmap(maskBitmapTemp, 0,0, paint);
        
        maskPaint.setAntiAlias(true);
        maskPaint.setDither(false);
        maskPaint.setColor(0x7FFFFFFF);
        
        backShader = new BitmapShader(backBitmap, Shader.TileMode.CLAMP,
                Shader.TileMode.CLAMP);
        progressShader = new BitmapShader(maskBitmap, Shader.TileMode.CLAMP,
                Shader.TileMode.CLAMP);
        maskPaint.setShader(progressShader);
        
        maskRect = new Rect(0,0,mWidth,mHeight);
        setProgress(100);
        
        thumbPoint = new PointF();
        lastDragPoint = new PointF();
    }

    /**************************************
     * Here you must measure your whole canvas size to fit your desire size.
     **************************************/
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int widthmode = MeasureSpec.getMode(widthMeasureSpec);
        int heightmode = MeasureSpec.getMode(heightMeasureSpec);
        int width = MeasureSpec.getSize(widthMeasureSpec);
        int height = MeasureSpec.getSize(heightMeasureSpec);
        
        mWidth = widthmode == MeasureSpec.EXACTLY ? width : mWidth;
        mHeight = heightmode == MeasureSpec.EXACTLY ? height : mHeight;
        setMeasuredDimension(mWidth, mHeight);
    }
    
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.translate(0,0);
        canvas.drawBitmap(backBitmap, 0, 0, paint);
        canvas.drawRect(maskRect, maskPaint);
        canvas.save();
        canvas.drawBitmap(thumbBitmap, 0, thumbPoint.y, maskPaint);
        canvas.restore();
        
    }
    
    /**************************************
     * Here I change the y coordinate of small thumb image by MotionEvent,then 
     * refresh your canvas through onDraw method.
     **************************************/
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                {
                    if(null != this.progressChangedListener)
                    {
                        this.progressChangedListener.startTracking();
                    }
                    lastDragPoint.x = event.getX();
                    lastDragPoint.y = event.getY();
                    thumbPoint.y = event.getY();
                    changePosition();
                    break;
                }
            case MotionEvent.ACTION_MOVE:
                {
                    thumbPoint.y += event.getY() - lastDragPoint.y;
                    changePosition();
                    lastDragPoint.y = event.getY();
                    break;
                }
            case MotionEvent.ACTION_UP:
                {
                    changePosition();
                    
                    if(null != this.progressChangedListener)
                    {
                        this.progressChangedListener.stopTracking();
                    }
                    break;
                }
        }
        invalidate();
        return true;
    }
    
    public void setProgress(int pro)
    {
        maskRect.set(0,(int)(mHeight*((maxProgress-pro)/(maxProgress*1.0f))),mWidth,mHeight);
        publishProgress(pro);
        progress = pro;
    }
    public void changePosition()
    {
        if (thumbPoint.y < 0) {
            thumbPoint.y = 0;
        }
        if(thumbPoint.y > (mHeight-thumbWidth))
        {
            thumbPoint.y = (mHeight-thumbWidth);
        }
        int progresstemp = (int)(((mHeight-thumbPoint.y-thumbHeight)/(mHeight-thumbHeight))*100);
        setProgress(progresstemp);
    }
    
    public int getProgress()
    {
        return this.progress;
    }
    public int getMax()
    {
        return this.maxProgress;
    }
    public void setProgressChangedListener(OnProgressChangedListener progressChangedListener)
    {
        this.progressChangedListener = progressChangedListener;
    }
    
    public void publishProgress(int cprogress)
    {
        if(null != this.progressChangedListener)
        {
            this.progressChangedListener.progressChanged(cprogress);
        }
    }
    /**************************************
     * Here I design an interface for your application to callback 
     * and monitor current progress
     **************************************/
    public interface OnProgressChangedListener
    {
        public void progressChanged(int progresstemp);
        public void startTracking();
        public void stopTracking();
    }
    
}
