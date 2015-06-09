/**
 * @author yaogang.hao
 * 
 * Drag cirlce seek bar to set the size of circle
 */
package com.jrdcom.mt.widget;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.MeasureSpec;
import android.widget.ImageView;
import android.widget.SeekBar;

import com.jrdcom.android.gallery3d.R;
public class DragCircleSeekBar extends View {

    private int mMaxWidth = Integer.MAX_VALUE;
    private int mMaxHeight = Integer.MAX_VALUE;
    private  int mWidth;
    private  int mHeight;
    private int thumbWidth;
    private Bitmap backBitmap;
    private Bitmap thumBitmap;
    private Paint paint;
    private PointF thumbPoint;
    private PointF lastDragPoint;
    private float sector;
    private int circlepos = 0;
    private onDragCircleListener onCircleListener;
//    private int tempw;
//    private int temph;
    
    
    public DragCircleSeekBar(Context context, AttributeSet attrs) {
        super(context, attrs);
        initMeterials();
    }

    public DragCircleSeekBar(Context context) {
        this(context, null);
        initMeterials();
    }

    public void initMeterials()
    {
//        BitmapFactory.Options options = new BitmapFactory.Options();
//        options.inJustDecodeBounds = true;
//        backBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.thumbviewback,options);
        
        Drawable bgDrawable = getResources().getDrawable(R.drawable.thumbviewback);
        mWidth = bgDrawable.getIntrinsicWidth();
        mHeight = bgDrawable.getIntrinsicHeight();
//        mWidth = options.outWidth;
//        mHeight = options.outHeight;
//        options.inJustDecodeBounds = false;
//        backBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.thumbviewback,options);
        backBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.thumbviewback);
//        BitmapFactory.Options optionsThumb = new BitmapFactory.Options();
//        optionsThumb.inJustDecodeBounds = true;
//        thumBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.thumbviewthumb,optionsThumb);
//        thumbWidth = optionsThumb.outWidth;
//        optionsThumb.inJustDecodeBounds = false;
        
        Drawable thumbdDrawable = getResources().getDrawable(R.drawable.thumbviewthumb);
        thumbWidth = thumbdDrawable.getIntrinsicWidth();
//        thumBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.thumbviewthumb,optionsThumb);
        thumBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.thumbviewthumb);
        paint = new Paint();
        thumbPoint = new PointF();
        lastDragPoint = new PointF();
        sector = (mWidth*1.0f)/4;
        
//        Drawable drawable = getResources().getDrawable(R.drawable.thumbviewback);
//         tempw = drawable.getIntrinsicWidth();
//         temph = drawable.getIntrinsicHeight();
//         Drawable drawablethumb = getResources().getDrawable(R.drawable.thumbviewthumb);
//         thumbWidth = drawablethumb.getIntrinsicWidth();
    }
    
    public void setDragListener(onDragCircleListener onCircleListener)
    {
        this.onCircleListener = onCircleListener;
    }
    public void draglistenercallback(int pos)
    {
        if(null != this.onCircleListener)
        {
            this.onCircleListener.onStopDrag(pos);
        }
    }
    
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        
        int widthmode = MeasureSpec.getMode(widthMeasureSpec);
        int heightmode = MeasureSpec.getMode(heightMeasureSpec);
        int width = MeasureSpec.getSize(widthMeasureSpec);
        int height = MeasureSpec.getSize(heightMeasureSpec);
        
//        mWidth = widthmode == MeasureSpec.EXACTLY ? width : tempw;
//        mHeight = heightmode == MeasureSpec.EXACTLY ? height : temph;
        
//        mWidth =  resolveAdjustedSize(tempw,mMaxWidth,widthMeasureSpec);
//        mHeight =  resolveAdjustedSize(temph,mMaxHeight,widthMeasureSpec);
//        mWidth = resolveSizeAndState(tempw,widthMeasureSpec,0);
//        mHeight = resolveSizeAndState(temph,widthMeasureSpec,0);
        setMeasuredDimension(mWidth, mHeight);
    }
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.translate(0, 0);
        canvas.drawBitmap(backBitmap, 0, 0, paint);
        canvas.save();
        canvas.drawBitmap(thumBitmap, thumbPoint.x, thumbPoint.y, paint);
        canvas.restore();
    }
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        
        switch (event.getAction()) {
            
            case MotionEvent.ACTION_DOWN:
                {
                    lastDragPoint.x = event.getX();
                    lastDragPoint.y = event.getY();
                    thumbPoint.x = event.getX();
                    drawCircleInRect();
                    break;
                }
            case MotionEvent.ACTION_MOVE:
                {
                    thumbPoint.x += event.getX() - lastDragPoint.x;
                    drawCircleInRect();
                    lastDragPoint.x = event.getX();
                    break;
                }
            case MotionEvent.ACTION_UP:
                {
                    drawCircleInRect();
                    float temp =  (thumbPoint.x+(thumbWidth*1.0f)/2)/sector;
                    
                    if(temp < 1.0f)
                    {
                        if(temp > 0.5f)
                        {
                            thumbPoint.x = (mWidth*1.0f)/4-(thumbWidth*1.0f)/2;
                            circlepos = 1;
                        }
                        else {
                            thumbPoint.x = 0;
                            circlepos = 0;
                        }
                        
                    }else if(temp <2.0f)
                    {
                        if(temp > 1.5f)
                        {
                            thumbPoint.x = (mWidth*1.0f)/4*2-(thumbWidth*1.0f)/2;
                            circlepos = 2;
                        }
                        else {
                            thumbPoint.x = (mWidth*1.0f)/4-(thumbWidth*1.0f)/2;
                            circlepos = 1;
                        }
                        
                    }else if(temp < 3.0f)
                    {
                        if(temp > 2.5f)
                        {
                            thumbPoint.x = (mWidth*1.0f)/4*3-(thumbWidth*1.0f)/2-8.0f;
                            circlepos = 3;
                        }
                        else {
                            thumbPoint.x = (mWidth*1.0f)/4*2-(thumbWidth*1.0f)/2;
                            circlepos = 2;
                        }
                    }else {
                        if(temp > 3.5f)
                        {
                            thumbPoint.x = mWidth-thumbWidth-this.getPaddingRight();
                            circlepos = 4;
                        }
                        else {
                            thumbPoint.x = (mWidth*1.0f)/4*3-(thumbWidth*1.0f)/2-8.0f;
                            circlepos = 3;
                        }
                    }
                    draglistenercallback(circlepos);
                    break;
                }
        }
        invalidate();
        return true;
    }
    
    public void drawCircleInRect()
    {
        if (thumbPoint.x < 0) {
            thumbPoint.x = 0;
        }
        if(thumbPoint.x > (mWidth-thumbWidth-this.getPaddingRight()))
        {
            thumbPoint.x = mWidth-thumbWidth-this.getPaddingRight();
        }
    }
    
    
    public interface onDragCircleListener
    {
        public void onStopDrag(int pos);
    }
    
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        Log.e("", "");
        return super.onKeyDown(keyCode, event);
        
    }
    private int resolveAdjustedSize(int desiredSize, int maxSize,
            int measureSpec) {
        int result = desiredSize;
        int specMode = MeasureSpec.getMode(measureSpec);
        int specSize =  MeasureSpec.getSize(measureSpec);
        switch (specMode) {
        case MeasureSpec.UNSPECIFIED:
        /* Parent says we can be as big as we want. Just don't be larger
        than max size imposed on ourselves.
        */
        result = Math.min(desiredSize, maxSize);
        break;
        case MeasureSpec.AT_MOST:
        // Parent says we can be as big as we want, up to specSize. 
        // Don't be larger than specSize, and don't be larger than 
        // the max size imposed on ourselves.
        result = Math.min(Math.min(desiredSize, specSize), maxSize);
        break;
        case MeasureSpec.EXACTLY:
        // No choice. Do what we are told.
        result = specSize;
        break;
        }
        return result;
     }
}
