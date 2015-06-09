package com.jrdcom.example.joinpic;

import java.util.ArrayList;
import java.util.Vector;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Shader;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.View.MeasureSpec;
import android.widget.RelativeLayout;
import android.graphics.Rect;

public class JointPuzzleLayoutView extends RelativeLayout {
    
    private Paint paint ;
    private Context context;
    private int mWidth;
    private int mHeight;
    private  ArrayList<Bitmap> m_vecTexture = null; // 边框的列表
    public static  final int MarginTOP_BOTTOM = 30;
    private final int MarginLEFT_RIGHT = 40;
    private final int Middle_bar = 10;
    
    public JointPuzzleLayoutView(Context context) {
       this(context, null);
    }

    public JointPuzzleLayoutView(Context context, AttributeSet attrs) {
        super(context, attrs);
        setWillNotDraw(false);
        this.paint = new Paint();
        this.context = context;
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {

        final int count = getChildCount();
        LayoutParams lp;
        int h = MarginTOP_BOTTOM;
        for(int i = 0; i < count; i++) {
            final View child = getChildAt(i);
            lp = (LayoutParams) child.getLayoutParams();
            child.layout(0,h, this.getMeasuredWidth(),child.getMeasuredHeight()+h);
            h += child.getMeasuredHeight();
            if(i<count)
            {
                h += Middle_bar;
            }
        }
       
    }

    
    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        // TODO Auto-generated method stub
        super.onSizeChanged(w, h, oldw, oldh);
    }
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        // TODO Auto-generated method stub
        
        final int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        final int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        final int heightMode = MeasureSpec.getMode(heightMeasureSpec);
         int heightSize = MeasureSpec.getSize(heightMeasureSpec);


        final int count = getChildCount();
        for(int i = 0; i < count; i++) {
            final View child = getChildAt(i);
            final LayoutParams lp = (LayoutParams)child.getLayoutParams();

            final int childWidthMeasureSpec = MeasureSpec.makeMeasureSpec(lp.width,
                    MeasureSpec.EXACTLY);
            final int childHeightMeasureSpec = MeasureSpec.makeMeasureSpec(lp.height,
                    MeasureSpec.EXACTLY);

            child.measure(childWidthMeasureSpec, childHeightMeasureSpec);
            heightSize += child.getMeasuredHeight();
        }
        
        mWidth = widthSize;
        mHeight = heightSize+2*MarginTOP_BOTTOM+((count-1)*Middle_bar);
        
        setMeasuredDimension(mWidth, mHeight);
        
    }
    @Override
    protected void onDraw(Canvas canvas) {
        canvas.drawColor(Color.WHITE);
        canvas.saveLayer(0.0F, 0.0F, getWidth(), getHeight(), null, 12);
        int lCurrentHeight=0;
        
        if(m_vecTexture!=null && m_vecTexture.size()>0)
        {
            // 左边
            Bitmap pLeftBitmap = m_vecTexture.get(3);
            if (pLeftBitmap != null) {
                canvas.drawBitmap(pLeftBitmap, 0, 0, null);
            }
            // 上边
            Bitmap pTopBitmap = m_vecTexture.get(1);
            if (pTopBitmap != null) {
                canvas.drawBitmap(pTopBitmap, 0, 0, null);
            }
            // 右边
            Bitmap pRightBitmap = m_vecTexture.get(4);
            if (pRightBitmap != null) {
                canvas.drawBitmap(pRightBitmap, pTopBitmap.getWidth()
                        - pRightBitmap.getWidth(), 0, null);
            }
            lCurrentHeight=pLeftBitmap.getHeight();
            
            //TODO  如果有超过5个边框，一直画下去 
            for(int i=0;i<(m_vecTexture.size()-5)/2;i++)
             {
                // 左边
                Bitmap lTempBitmap = m_vecTexture.get(i*2+5);
                if (pLeftBitmap != null) {
                    canvas.drawBitmap(lTempBitmap, 0, lCurrentHeight, null);
                }
                //右边
                lTempBitmap=m_vecTexture.get(i*2+5+1);
                if (pLeftBitmap != null) {
                    canvas.drawBitmap(lTempBitmap,  pTopBitmap.getWidth()
                            - pRightBitmap.getWidth(), lCurrentHeight, null);
                }
                lCurrentHeight += lTempBitmap.getHeight();
             }
        
            // 下边
            Bitmap pBottomBitmap = m_vecTexture.get(2);
            if (pBottomBitmap != null) {
                canvas.drawBitmap(pBottomBitmap, 0, mHeight-pBottomBitmap.getHeight(), null);
            }
            // 中间，或许要平铺，这个由开发者自己的需求而定，这里只提供图片
            Bitmap pMiddleBitmap = m_vecTexture.get(0);
            if (pMiddleBitmap != null) {
                Paint fillPaint = new Paint();
                fillPaint.setColor(0xFFFFFFFF);
                fillPaint.setStyle(Paint.Style.FILL);
                fillPaint.setShader(new BitmapShader(pMiddleBitmap, Shader.TileMode.REPEAT, Shader.TileMode.REPEAT));
                canvas.drawRect(new Rect(pLeftBitmap.getWidth(),pTopBitmap.getHeight(),pTopBitmap.getWidth()-pRightBitmap.getWidth(),mHeight-pBottomBitmap.getHeight()), fillPaint);
            }
        }
        canvas.restore();
        super.onDraw(canvas);
    }
    @Override
    protected void dispatchDraw(Canvas canvas) {
        // TODO Auto-generated method stub
        super.dispatchDraw(canvas);
    }
    public void setPuzzleTexture( ArrayList<Bitmap> list)
    {
        this.m_vecTexture=list;
    }
}
