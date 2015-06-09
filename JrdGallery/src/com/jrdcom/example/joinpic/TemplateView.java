package com.jrdcom.example.joinpic;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;


public class TemplateView extends View
{
    private static Paint mPaint = null;
    private Bitmap mBitmap2 = null;
    private static Rect dst;
    private final Rect rect;
    private Rect mrctRect;
    
    private float x=0,y=0;
    private int m = 0;
    private int miDTX = 0 , miDTX1 = 0, miDTX2 = 0, miDTX3 = 0;
    private int miDTY = 0 , miDTY1 = 0, miDTY2 = 0, miDTY3 = 0;
    private int mXsave = 0, mYsave = 0;
    private int mWidth=0, mHeight=0;
    
    public TemplateView(Context context, AttributeSet attrs ,String pathName, int width, int height) {
        super(context,attrs);
        mPaint = new Paint();
        dst = new Rect();
        mrctRect = new Rect();
        
        mPaint.setFilterBitmap(true);
        mPaint.setAntiAlias(true);
        
        mWidth = width;
        mHeight = height;
        
        BitmapFactory.Options opt = new BitmapFactory.Options();
        opt.inJustDecodeBounds = true; 
        mBitmap2 = BitmapFactory.decodeFile(pathName,opt);
        final int minSideLength = Math.max(width, height);
        opt.inSampleSize = computeSampleSize(opt, 2*minSideLength, opt.outWidth*opt.outHeight);
        opt.inPreferredConfig = Bitmap.Config.RGB_565;
        opt.inPurgeable = true;
        opt.inInputShareable = true;
        opt.inJustDecodeBounds = false; 
        
        mBitmap2 = BitmapFactory.decodeFile(pathName,opt);
        rect = new Rect(0, 0, mWidth, mHeight);
//        mBitmap2 = ((BitmapDrawable) getResources().getDrawable(R.drawable.road_rage)).getBitmap();
    }
    
    public void onDraw(Canvas canvas)
    {
        super.onDraw(canvas);
        canvas.drawColor(Color.GRAY);
        drawImage(canvas, mBitmap2, miDTX, miDTY, miDTX1, miDTY1, mBitmap2.getWidth(),
                mBitmap2.getHeight(), 0, 0);
    }
    
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        // TODO Auto-generated method stub
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        setMeasuredDimension(mWidth, mHeight);
    }
    
    public boolean onTouchEvent(MotionEvent event) {
        super.onTouchEvent(event);
        int action = event.getAction();
        if (action == MotionEvent.ACTION_UP) {
            if(mBitmap2.getWidth() < mWidth && mBitmap2.getHeight() < mHeight){
                miDTX1 = (int) event.getX();
                miDTY1 = (int) event.getY();
            }else if(mBitmap2.getWidth() < mWidth ){
                if(miDTY - miDTY1 < 0){
                    if (miDTY1 - miDTY > mBitmap2.getHeight() - mHeight) {
                        miDTX1 = (int) event.getX();
                        miDTY1 = (int) (miDTY + mBitmap2.getHeight() - mHeight);
                    } else {
                        miDTX1 = (int) event.getX();
                        miDTY1 = (int) miDTY1;
                    }
                }else{
                    miDTX1 = (int) event.getX();
                    miDTY1 = (int) event.getY();
                }
            }else if (mBitmap2.getHeight() < mHeight) {
                if(miDTX - miDTX1 < 0){
                    if (miDTX1 - miDTX > mBitmap2.getWidth() - mWidth) {
                        miDTY1 = (int) event.getY();
                        miDTX1 = (int) (miDTX + mBitmap2.getWidth() - mWidth);
                    } else {
                        miDTX1 = (int) miDTX1;
                        miDTY1 = (int) event.getY();
                    }
                }else{
                    miDTX1 = (int) event.getX();
                    miDTY1 = (int) event.getY();
                }
            } else {
                if (miDTX - miDTX1 > 0 && miDTY - miDTY1 > 0) {
                    miDTX1 = (int) event.getX();
                    miDTY1 = (int) event.getY();
                } else if (miDTX - miDTX1 < 0 && miDTY - miDTY1 > 0) {
                    if (miDTX1 - miDTX > mBitmap2.getWidth() - mWidth) {
                        miDTX1 = (int) (miDTX + mBitmap2.getWidth() - mWidth);
                        miDTY1 = (int) event.getY();
                    } else {
                        miDTX1 = (int) miDTX1;
                        miDTY1 = (int) event.getY();
                    }
                } else if (miDTX - miDTX1 > 0 && miDTY - miDTY1 < 0) {
                    if (miDTY1 - miDTY > mBitmap2.getHeight() - mHeight) {
                        miDTX1 = (int) event.getX();
                        miDTY1 = (int) (miDTY + mBitmap2.getHeight() - mHeight);
                    } else {
                        miDTX1 = (int) event.getX();
                        miDTY1 = (int) miDTY1;
                    }

                } else if (miDTX - miDTX1 < 0 && miDTY - miDTY1 < 0) {
                    if (miDTX1 - miDTX > mBitmap2.getWidth() - mWidth
                            && miDTY1 - miDTY > mBitmap2.getHeight() - mHeight) {
                        miDTX1 = (int) (miDTX + mBitmap2.getWidth() - mWidth);
                        miDTY1 = (int) (miDTY + mBitmap2.getHeight() - mHeight);
                    } else if (miDTX1 - miDTX < mBitmap2.getWidth() - mWidth) {
                        miDTX1 = (int) miDTX1;
                        miDTY1 = (int) (miDTY + mBitmap2.getHeight() - mHeight);
                    } else if (miDTY1 - miDTY < mBitmap2.getHeight() - mHeight) {
                        miDTX1 = (int) (miDTX + mBitmap2.getWidth() - mWidth);
                        miDTY1 = (int) miDTY1;
                    } else {
                        miDTX1 = (int) miDTX1;
                        miDTY1 = (int) miDTY1;
                    }
                } else {
                    miDTX1 = miDTX2;
                    miDTY1 = miDTY2;
                    miDTX = miDTX3;
                    miDTY = miDTY3;
                }
            }
            miDTX2 = miDTX1;
            miDTY2 = miDTY1;
            miDTX3 = miDTX;
            miDTY3 = miDTY;
            mXsave = Math.abs(miDTX - miDTX1);
            mYsave = Math.abs(miDTY - miDTY1);
        } else if (action == MotionEvent.ACTION_DOWN) {
            miDTX1 = (int) event.getX()+mXsave;
            miDTY1 = (int) event.getY()+mYsave;
        }else if(action == MotionEvent.ACTION_MOVE){
            miDTX = (int) event.getX();
            miDTY = (int) event.getY();
            postInvalidate();
        }
        return true;
    }
    private void drawImage(Canvas canvas, Bitmap blt, int x, int y, int x1, int y1, int w, int h,int bx,int by) {
        dst.left = x - x1;
        dst.top = y - y1;
        dst.right = x + w - x1;
        dst.bottom = y + h - y1;
        
        mrctRect.left = bx;
        mrctRect.top = by;
        mrctRect.right = bx + w;
        mrctRect.bottom = by + h;
        
        Paint paint = new Paint();
        paint.setColor(Color.WHITE);
        paint.setStyle(Paint.Style.STROKE);
        paint.setAntiAlias(true);
        
        paint.setStrokeWidth(10f);
        canvas.drawBitmap(blt, mrctRect, dst, null);
        canvas.drawRect(rect, paint);
    }

    public static int computeSampleSize(BitmapFactory.Options options,
            int minSideLength, int maxNumOfPixels) {
        int initialSize = computeInitialSampleSize(options, minSideLength,
                maxNumOfPixels);
        int roundedSize;
        if (initialSize <= 8) {
            roundedSize = 1;
            while (roundedSize < initialSize) {
                roundedSize <<= 1;
            }
        } else {
            roundedSize = (initialSize + 7) / 8 * 8;
        }
        return roundedSize;
    }

    private static int computeInitialSampleSize(BitmapFactory.Options options,
            int minSideLength, int maxNumOfPixels) {
        double w = options.outWidth;
        double h = options.outHeight;
        int lowerBound = (maxNumOfPixels == -1) ? 1 :
                (int) Math.ceil(Math.sqrt(w * h / maxNumOfPixels));
        int upperBound = (minSideLength == -1) ? 128 :
                (int) Math.min(Math.floor(w / minSideLength),
                        Math.floor(h / minSideLength));
        if (upperBound < lowerBound) {
            return lowerBound;
        }
        if ((maxNumOfPixels == -1) &&
                (minSideLength == -1)) {
            return 1;
        } else if (minSideLength == -1) {
            return lowerBound;
        } else {
            return upperBound;
        }
    }
}























