
package com.jrdcom.example.joinpic;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.Region;
import android.util.AttributeSet;
import android.util.Log;
import android.view.ViewGroup;
import android.view.View.MeasureSpec;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import com.jrdcom.example.joinpic.FreePuzzleItemView;
import com.jrdcom.example.joinpic.FreePuzzleItemView.OnTouchesListener;
import android.util.*;


public class FreePuzzleLayoutView extends ViewGroup implements OnTouchesListener
{
    
    private Bitmap backgroundBitmap;
    private Rect refreshRect;
    private Paint paint = new Paint();
    private int mWidth;
    private int mHeight;
    private int display_width;
    private int display_height;
    private final String tag = "FreePuzzleLayoutView";
    private  int MARGIN_TOP = 100;
    private Matrix matrix = new Matrix();
    private float scale = 1.0f;
    private boolean isFristDraw = false;
    
    private OnMeasureCompleteListener onMeasureCompleteListener;
    public FreePuzzleLayoutView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void setOnMeasureCompleteListener(OnMeasureCompleteListener measureCompleteListener)
    {
        this.onMeasureCompleteListener = measureCompleteListener;
    }
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        
        final int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        final int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        final int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        final int heightSize = MeasureSpec.getSize(heightMeasureSpec);

        if(widthMode == MeasureSpec.UNSPECIFIED || heightMode == MeasureSpec.UNSPECIFIED) {
            throw new IllegalArgumentException("MeasureSpec mode");
        }

        final int count = getChildCount();
        for(int i = 0; i < count; i++) {
            final View child = getChildAt(i);
            final LayoutParams lp = (LayoutParams)child.getLayoutParams();

            final int childWidthMeasureSpec = MeasureSpec.makeMeasureSpec(lp.width,
                    MeasureSpec.EXACTLY);
            final int childHeightMeasureSpec = MeasureSpec.makeMeasureSpec(lp.height,
                    MeasureSpec.EXACTLY);

            child.measure(childWidthMeasureSpec, childHeightMeasureSpec);
        }

        mWidth = widthSize;
        mHeight = heightSize;
        setMeasuredDimension(widthSize, heightSize);

    }
    
    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
    }

    public void relayout()
    {
        this.removeAllViews();
    }
    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        
        final int count = getChildCount();
        LayoutParams lp;
        for(int i = 0; i < count; i++) {
            final View child = getChildAt(i);
            lp = (LayoutParams)child.getLayoutParams();
            child.layout(0,MARGIN_TOP, this.mWidth,this.display_height+MARGIN_TOP);
        }

    
    }
    @Override
    protected void dispatchDraw(Canvas canvas) {
        
        if(!isFristDraw)
        {
            isFristDraw = true;
            //compute scale value
            scale = this.mWidth /(this.backgroundBitmap.getWidth()*1.0f);
            display_height = (int)(scale * backgroundBitmap.getHeight());
            MARGIN_TOP = (this.mHeight - display_height)/2;
            if(this.onMeasureCompleteListener != null)
            {
                this.onMeasureCompleteListener.onMeasureComplete();
            }
        }
        if(this.backgroundBitmap != null)
        {
            canvas.drawBitmap(backgroundBitmap, new Rect(0,0,backgroundBitmap.getWidth(),backgroundBitmap.getHeight()), new Rect(0, 
                    MARGIN_TOP,this.mWidth,this.display_height+MARGIN_TOP), paint);
           
        }
        super.dispatchDraw(canvas);
        requestLayout();
    }
    
    
    public void addInScreen(View child, int left, int top) {
        addView(child, new LayoutParams(left, top, this.getMeasuredWidth(),this.getMeasuredHeight()));
    }

    public void setBackgroundBitmap(Bitmap bitmap)
    {
        if(this.backgroundBitmap != null && !(this.backgroundBitmap.isRecycled()))
        {
            this.backgroundBitmap.recycle();
           
        }
        this.backgroundBitmap = bitmap;
        invalidate();

    }
    
    public class LayoutParams extends ViewGroup.LayoutParams {
        public int left = 0;
        public int top = 0;

        public LayoutParams(int width, int height) {
            super(width, height);
        }

        public LayoutParams(int left, int top, int width, int height) {
            super(width, height);
            this.left = left;
            this.top = top;
        }
    }
    public void onTouchBegan(FreePuzzleItemView view)
    {
        bringChildToFront(view);
        invalidate();
    }
    
    public int getContainerWidth()
    {
        return this.getMeasuredWidth();
    }
    public float getScaleValue()
    {
        return this.scale;
    }
    
    public interface OnMeasureCompleteListener
    {
        public void onMeasureComplete();
    }
}
