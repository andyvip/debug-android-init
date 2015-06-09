
package com.jrdcom.mt.widget;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Matrix;
import android.graphics.NinePatch;
import android.graphics.Paint;
import android.graphics.Paint.Cap;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.Bitmap.Config;
import android.graphics.Shader.TileMode;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.MeasureSpec;
import com.jrdcom.android.gallery3d.R;

import android.util.Log;
import android.widget.PopupWindow;
import android.widget.*;
import  android.view.*;
import  android.graphics.drawable.*;

/**
 * 
 * yaogang.hao
 * this widget is to select color.
 *
 */
public class ColorSelectorView extends View {
    
    private int mWIDTH = 400;
    private int mHEIGHT = 50;
    private int Margin_Left = 30;
    private int whole_w;
    private int whole_h;
    private Bitmap colorBg;
    private Bitmap colorselectorBitmap;
    private int colorwidth = 10;
    
    private int img_w;
    private int img_h;
    private int[] colors;
    private Paint mBitmapPaint;
    private Paint colorPaint = new Paint();
    private NinePatch ninePatch;
    
    private float scale_x;
    private float scale_y;
    private int selector_width;
    private int selector_height;
    private int padding = 3;
    private int leftpadding = 6;
    private Rect selectRect;
    private Bitmap leftbitmap;
    private Paint leftPaint;
    private Rect leftRect;
    
    private OnColorChangedListener mChangedListener;
    
    //////////////////////////
    //color tip widget
    private Bitmap popupBitmap ;
    private PopupWindow colorPreviewWindow;
    private PopupView popupImageview;
    
    public ColorSelectorView(Context context) {
        super(context);
    }

    public ColorSelectorView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initData();
        colorPreviewWindow =  new PopupWindow(context);
        colorPreviewWindow.setWindowLayoutMode(WindowManager.LayoutParams.WRAP_CONTENT,WindowManager.LayoutParams.WRAP_CONTENT);
        colorPreviewWindow.setBackgroundDrawable(new BitmapDrawable());
        popupImageview = new PopupView(context);
        popupImageview.setImageBitmap(popupBitmap);
        colorPreviewWindow.setContentView(popupImageview);
    }
    
    public void initData()
    {
        Resources res = getResources();
        
        colors = new int[]{
                res.getColor(R.color.img_text_color1),res.getColor(R.color.img_text_color2),res.getColor(R.color.img_text_color3),res.getColor(R.color.img_text_color4),res.getColor(R.color.img_text_color5),
                res.getColor(R.color.img_text_color6),res.getColor(R.color.img_text_color7),res.getColor(R.color.img_text_color8),res.getColor(R.color.img_text_color9),res.getColor(R.color.img_text_color10),
                res.getColor(R.color.img_text_color11),res.getColor(R.color.img_text_color12),res.getColor(R.color.img_text_color13),res.getColor(R.color.img_text_color14),res.getColor(R.color.img_text_color15),
                res.getColor(R.color.img_text_color16),res.getColor(R.color.img_text_color17),res.getColor(R.color.img_text_color18),res.getColor(R.color.img_text_color19),res.getColor(R.color.img_text_color20),
                res.getColor(R.color.img_text_color21),res.getColor(R.color.img_text_color22),res.getColor(R.color.img_text_color23),res.getColor(R.color.img_text_color24),res.getColor(R.color.img_text_color25),
                res.getColor(R.color.img_text_color26),
        };
        colorBg = BitmapFactory.decodeResource(getResources(), R.drawable.text_color_foreground);
        Drawable tempdraDrawable = getResources().getDrawable(R.drawable.text_color_foreground);
        img_w = tempdraDrawable.getIntrinsicWidth();//(int)(tempdraDrawable.getIntrinsicWidth()*0.6f);
        img_h = tempdraDrawable.getIntrinsicHeight();//(int)(tempdraDrawable.getIntrinsicHeight()*0.6f);
        leftbitmap = Bitmap.createBitmap(colorBg, 0,0, img_w, img_h);
        mHEIGHT = img_h;
        DisplayMetrics metrics = new DisplayMetrics();
        metrics = getResources().getDisplayMetrics();
        int scale = metrics.densityDpi/160;
        colorwidth *= scale;
        mWIDTH = img_w+Margin_Left+colorwidth*colors.length+padding*2;
        mBitmapPaint = new Paint();
        
        selector_width = colorwidth*colors.length;
        selector_height = mHEIGHT -padding*2;
        colorselectorBitmap = Bitmap.createBitmap(selector_width, selector_height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(colorselectorBitmap);
        ninePatch = new NinePatch(leftbitmap, leftbitmap.getNinePatchChunk(), null);
        
        int  tempx=0;
        Rect rect = new Rect(tempx, 0, colorwidth, mHEIGHT);
        colorPaint.setStyle(Paint.Style.FILL);
        for(int i=0;i<colors.length && (i+1)*colorwidth<selector_width;i++)
        {
            rect.set(i*colorwidth, 0, (i+1)*colorwidth, selector_height);
            colorPaint.setColor(colors[i]);
            canvas.drawRect(rect, colorPaint);
        }
        
        selectRect = new Rect(img_w + Margin_Left+padding,padding,img_w + Margin_Left+padding+selector_width,mHEIGHT-padding);
        leftPaint = new Paint();
        leftPaint.setAntiAlias(true);
        leftPaint.setStrokeCap(Cap.ROUND);
        leftRect = new Rect(leftpadding, leftpadding, img_w-leftpadding, img_h-leftpadding);
        
        // background of color tip view 
        popupBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.show_color_bg);
       
        
    }
    
    public void setOnColorChangedListenner(OnColorChangedListener listener) {
        mChangedListener = listener;
    }
    
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
    
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int width = MeasureSpec.getSize(widthMeasureSpec);
        int height = MeasureSpec.getSize(heightMeasureSpec);
        if (widthMode == MeasureSpec.EXACTLY) {
            whole_w = width;
        } else {
            whole_w = mWIDTH;
        }
        if (heightMode == MeasureSpec.EXACTLY) {
            whole_h = height;
        } else {
            whole_h = mHEIGHT;
        }
        setMeasuredDimension(whole_w, whole_h);
        
    }
    
    @Override
    protected void onDraw(Canvas canvas) {
        canvas.drawBitmap(leftbitmap, 0,0, mBitmapPaint);
        canvas.drawRect(leftRect, leftPaint);
        ninePatch.draw(canvas, new Rect(img_w + Margin_Left,0,img_w + Margin_Left+selector_width+2*padding,mHEIGHT));
        canvas.drawBitmap(colorselectorBitmap, null, selectRect,mBitmapPaint);
    }
    
    
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        
        switch (event.getAction()) {
        
            case MotionEvent.ACTION_DOWN:
            {
                float x = event.getX();
                float y = event.getY();
                if(!iscontained(x,y))
                {
                    return false;
                }
                colorPreviewWindow.showAtLocation(this,Gravity.LEFT | Gravity.TOP, (int) event.getRawX(),(int) event.getRawY()-(int)y-img_h-10);
                break;
            }
            case MotionEvent.ACTION_MOVE:
            {
               
                float x = event.getX();
                float y = event.getY();
                if(!iscontained(x,y))
                    {
                        return false;
                    }
                
                //update the tip's color
                int color = getSelectedColor(x-(img_w+Margin_Left+padding),y-padding);
                popupImageview.updateColor(color);
                colorPreviewWindow.update((int)x,(int) event.getRawY()-(int)y-img_h-10,-1,-1);
                if(null != mChangedListener)
                {
                    mChangedListener.onColorChanging(color);
                }
                break;
            }
            case MotionEvent.ACTION_UP:
            {
                float x = event.getX();
                float y = event.getY();
                colorPreviewWindow.dismiss();
                if(!iscontained(x,y))
                    {
                        return false;
                    }
                
                int color = getSelectedColor(x-(img_w+Margin_Left+padding),y-padding);
                setshowCurrColor(color);
                invalidate();
                if(null != mChangedListener)
                {
                    mChangedListener.onColorChanged(color);
                }
                
                break;
            }
        }
        
        return true;
    }
    
    private void setshowCurrColor(int color)
    {
        leftPaint.setColor(color);
        invalidate(leftRect);
    }
    private int getSelectedColor(float x, float y)
    {
        int intX = (int) x;
        int intY = (int) y;
        
        return colorselectorBitmap.getPixel(intX, intY);
    }
    public boolean iscontained(float x,float y)
    {
        return selectRect.contains((int)x, (int)y);
    }
    public interface OnColorChangedListener {
        void onColorChanged(int color);
        void onColorChanging(int color);
    }

    /**
     * 
     * color tip view
     *
     */
    private class PopupView extends ImageView
    {
        private int mWidth;
        private Paint paint;
        private final int offset= 6;
        public PopupView(Context context) {
            super(context);
            paint = new Paint();
            Drawable tempdraDrawable = getResources().getDrawable(R.drawable.show_color_bg);
            mWidth = tempdraDrawable.getIntrinsicWidth()-offset;
        }
        
        @Override
        protected void onDraw(Canvas canvas) {
            //1:draw my color
            canvas.drawRect(offset,offset,mWidth,mWidth,paint);
            //2:draw parent view
            super.onDraw(canvas);
        }
        public void updateColor(int color)
        {
            paint.setColor(color);
            invalidate();
        }
    }
}
