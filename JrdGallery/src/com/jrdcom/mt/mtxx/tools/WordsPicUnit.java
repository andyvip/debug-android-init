
package com.jrdcom.mt.mtxx.tools;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.graphics.Bitmap.Config;

import com.mt.mtxx.image.WordsPlace;
import com.jrdcom.mt.util.MyData;

public class WordsPicUnit extends PicUnit {

    // public int mIsStaticWords = 0; //文字是否为静态文字

    public boolean mIsItalic = false; // 文字是否问斜体
    public boolean mIsShadow = false; // 文字是否有阴影

    protected Rect mWordsRect = null; // 图片文字的矩形区域
    protected Rect mWordsResultRC = null; // 效果图中文字的矩形区域
    protected String mWords; // 图片文字
    protected Paint mPaint;

    /*
     * 初始化带有文字的单元
     */

    public int getRowsNum(String str, Paint paint) {
        float[] widths = new float[str.length()];
        paint.getTextWidths(str, 0, str.length(), widths);
        float sum = 0;
        int rowsnum = 1;
        for (int i = 0; i < str.length(); i++) {
            sum += widths[i];
            if (sum * MyData.nBmpDstW / 280.0f > MyData.nBmpDstW) {
                rowsnum++;
                sum = 0;
            }
        }
        return rowsnum;
    }

    // public Bitmap getStringBmp(String str) {
    // Rect rc = new Rect();
    // Paint paint = new Paint();
    // paint.setTypeface(Typeface.create(Typeface.SERIF, Typeface.ITALIC));
    // paint.setTextSize((float) (20.0));
    // // ArrayList<String> strlist = new ArrayList<String>();
    // int maxWidth = 0;
    // int rowsBegin = 0;
    // int rowsnum = 0;
    // char[] strchar = str.toCharArray();
    // for (int i = 0; i < strchar.length; i++) {
    // if (strchar[i] == '\n' || i == strchar.length - 1) {
    // String tmp;
    // if (i == strchar.length - 1)
    // tmp = str.substring(rowsBegin, i + 1);
    // else
    // tmp = str.substring(rowsBegin, i);
    //
    // if (getRowsNum(tmp, paint) > 1) {
    // rowsnum += getRowsNum(tmp, paint);
    // maxWidth = (int) (280.0f);
    // }
    //
    // paint.getTextBounds(tmp, 0, tmp.length(), rc);
    // if (rc.width() > maxWidth) {
    // maxWidth = rc.width();
    // }
    // rowsBegin = i + 1;
    // // i++;
    // rowsnum++;
    // }
    //
    // }
    //
    // int maxHeight = rowsnum*(20+WordsPlace.SHADOW_DISTANCE
    // +WordsPlace.SHADOW_DISTANCE)+20;
    // // int maxHeight = 200;
    // maxWidth += 20;
    // if (maxWidth > Max_Bubble_XY) {
    // maxWidth = Max_Bubble_XY;
    // }
    // if (maxHeight > Max_Bubble_XY) {
    // maxHeight = Max_Bubble_XY;
    // }
    //
    // Bitmap bmp = Bitmap.createBitmap(maxWidth, maxHeight, Config.ARGB_8888);
    //
    // return bmp;
    // }

    public WordsPicUnit(Bitmap bmp, Rect rc, String words, int x, int y) {

        if (bmp == null) {
            // mIsStaticWords = 1;
            // bmp = getStringBmp(words);
            bmp = getDynimicStringBmp(words);
            rc.left = 0;
            rc.top = 0;
            rc.right = bmp.getWidth();
            rc.bottom = bmp.getHeight();
        }

        mWidth = bmp.getWidth();
        mHeight = bmp.getHeight();
        mResultW = mWidth;
        mResultH = mHeight;
        mResultWR = mWidth;
        mResultHR = mHeight;
        mScaleX = 1.0f;
        mScaleY = 1.0f;
        // mSrcPic = new int[mWidth*mHeight];
        // bmp.getPixels(mSrcPic, 0, mWidth, 0, 0, mWidth, mHeight);
        mMatrix = new Matrix();
        mSrcBmp = bmp;
        mNeedInavlidate = 1;
        mWordsRect = rc;
        mWordsResultRC = new Rect();
        mWords = words;

        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setDither(false);
        mPaint.setTypeface(Typeface.create(Typeface.SERIF, Typeface.NORMAL));
        mPaint.setTextAlign(Paint.Align.LEFT);
        mResultBmp = Bitmap.createBitmap(mSrcBmp, 0, 0, mWidth, mHeight,
                mMatrix, true);
        // drawWords(2);
    }

    public void setWordsShadow(boolean shadow) {
        mIsShadow = shadow;
        if (shadow) {
            mPaint.setShadowLayer(3, 2, 2, 0xFF000000);
            // mPaint.setColor(Color.GREEN);
        } else {
            // mPaint.setColor(Color.BLUE);
            mPaint.setShadowLayer(3, 2, 2, 0x00FF00FF);
        }
        mNeedInavlidate = 1;
    }

    public void setFontCorlor(int c) {
        mPaint.setColor(c);
        mNeedInavlidate = 1;
    }

    public void setWordsItalic(boolean italic) {
        mIsItalic = italic;
        if (italic) {
            // mPaint.setTypeface(Typeface.create(Typeface.SERIF,
            // Typeface.BOLD));
            mPaint.setFakeBoldText(true);
        } else {
            // mPaint.setTypeface(Typeface.create(Typeface.SERIF,
            // Typeface.NORMAL));
            mPaint.setFakeBoldText(false);
        }
        mNeedInavlidate = 1;
    }

    public boolean getWordsShadow() {
        return mIsShadow;
    }

    public boolean getWordsItalic() {
        return mIsItalic;
    }

    public void setWords(String str) {
        mWords = str;
        // if (mIsStaticWords == 1) {
        // mSrcBmp = getStringBmp(str);
        // forceScale();
        //
        // mWidth = mSrcBmp.getWidth();
        // mHeight = mSrcBmp.getHeight();
        // mWordsRect.left = 0;
        // mWordsRect.top = 0;
        // mWordsRect.right = mSrcBmp.getWidth();
        // mWordsRect.bottom = mSrcBmp.getHeight();
        // }
        mNeedInavlidate = 1;
        mOperateStyle = 7;
    }

    public void setSrcBmp(Bitmap bmp, Rect rc) {

        if (bmp == null) {
            // mSrcBmp = getStringBmp(mWords);

            mSrcBmp = getDynimicStringBmp(mWords);
            mWidth = mSrcBmp.getWidth();
            mHeight = mSrcBmp.getHeight();
            mWordsRect.left = 0;
            mWordsRect.top = 0;
            mWordsRect.right = mSrcBmp.getWidth();
            mWordsRect.bottom = mSrcBmp.getHeight();

            mScaleX = mScaleY = MyData.nBmpDstW / 280.0f;

            // mIsStaticWords = 1;
            mOperateStyle = 0;

        } else {
            mSrcBmp = bmp;
            mWidth = mSrcBmp.getWidth();
            mHeight = mSrcBmp.getHeight();
            mWordsRect = rc;

            // if (mIsStaticWords == 1) {
            // float sc;
            // sc = mScaleX > mScaleY ? mScaleX : mScaleY;
            // sc = sc * mScale;
            // mScaleX = 1.0f;
            // mScaleY = 1.0f;
            // mIsStaticWords = 0;
            // mOperateStyle = 0;
            // }
        }

        mOperateStyle = 7;

        mNeedInavlidate = 1;
    }

    /**
     * 根据缩放比例改变文字区域
     * 
     * @param scale
     */

    public void changeWordsRect(float scale) {
        mWordsResultRC.left = (int) (mWordsRect.left * scale);
        mWordsResultRC.top = (int) (mWordsRect.top * scale);
        mWordsResultRC.right = (int) (mWordsRect.right * scale);
        mWordsResultRC.bottom = (int) (mWordsRect.bottom * scale);
    }

    /**
     * 绘制静态文字的方法 （10 月 11 日 的版本未采用）
     * 
     * @param str
     * @param canvas
     * @param scale
     * @param paint
     */

    public void drawStaticWords(String str, Canvas canvas, float scale,
            Paint paint) {
        // int rowsBegin = 0;
        // int rowsnum = 1;
        // char[] strchar = str.toCharArray();
        // for (int i = 0; i < strchar.length; i++) {
        // if (strchar[i] == '\n' || i == strchar.length - 1) {
        // String tmp;
        // if (i == strchar.length - 1 && strchar[i] != '\n')
        // tmp = str.substring(rowsBegin, i + 1);
        // else
        // tmp = str.substring(rowsBegin, i);
        //
        // rowsBegin = i + 1;
        // // i++;
        // float x = 8 * scale;
        // float y = 30 * scale + (rowsnum - 1) * (20) * scale;
        // canvas.drawText(tmp, x, y, paint);
        // rowsnum++;
        // }
        //
        // }
        // getScaleFont(str,paint);

        // Rect rc = new Rect();
        // paint.getTextBounds(str, 0, str.length(),rc );

        char[] strchar = str.toCharArray();
        float[] widths = new float[str.length()];
        paint.getTextWidths(str, 0, str.length(), widths);
        float sum = 0;
        int rowsnum = 1;
        int rowsBegin = 0;
        boolean isCanDrawing = false;

        for (int i = 0; i < str.length(); i++) {
            sum += widths[i];
            if (sum * MyData.nBmpDstW / 280.f > MyData.nBmpDstW) {
                isCanDrawing = true;
                sum = 0;
            }

            if (isCanDrawing || i == str.length() - 1)
            {
                // float x = 8 * scale;
                float x = 8;
                float y = 30 * scale + (rowsnum - 1) * (20) * scale + 30;
                String tmp;
                if (i == str.length() - 1)
                    tmp = str.substring(rowsBegin, i + 1);
                else
                    tmp = str.substring(rowsBegin, i);

                canvas.drawText(tmp, x, y, paint);
                rowsBegin = i + 1;
                rowsnum++;
                isCanDrawing = false;
            }
        }
    }

    public void getScaleFont(String str, Paint paint)
    {
        Rect rc = new Rect();
        float scale = 1.0f;
        boolean isCanDrawing = false;
        float[] widths = new float[str.length()];
        paint.getTextWidths(str, 0, str.length(), widths);
        float sum = 0;
        int rowsnum = 1;
        int rowsBegin = 0;
        int maxWidth = 0;
        for (int i = 0; i < str.length(); i++) {
            sum += widths[i];
            if (sum * MyData.nBmpDstW / 280.0f > MyData.nBmpDstW) {
                rowsnum++;
                isCanDrawing = true;
                sum = 0;
            }
            if (isCanDrawing || i == str.length() - 1)
            {
                String tmp;
                if (i == str.length() - 1)
                    tmp = str.substring(rowsBegin, i + 1);
                else
                    tmp = str.substring(rowsBegin, i);

                paint.getTextBounds(tmp, 0, tmp.length(), rc);
                if (rc.width() > maxWidth) {
                    maxWidth = rc.width();
                }
            }

        }

        int maxHeight = rowsnum * (20 + WordsPlace.SHADOW_DISTANCE + WordsPlace.SHADOW_DISTANCE)
                + 20 + 30;

    }

    /**
     * 在图片src上打印文字
     * 
     * @param src
     * @param scale
     */
    public Bitmap getResultBmp() {
        return mResultBmp;
    }

    public void drawWords(Bitmap src, float scale) {
        Canvas canvas = new Canvas(src);
        mPaint.setTextSize((float) (35.0 * scale));

        // if (mIsStaticWords == 1) // 如果是静态文字
        // {
        // drawStaticWords(mWords, canvas, scale, mPaint);
        //
        // } else// 如果是气泡文字
        {
            WordsPlace wp = new WordsPlace(mWords, mWordsResultRC.left,
                    mWordsResultRC.top, mWordsResultRC.right,
                    mWordsResultRC.bottom, canvas);
            wp.initDefaultValues(mPaint, scale);
            if (mIsStaticWords == 1)
                wp.drawWords(1);
            else
                wp.drawWords(0);

        }

    }

    /**
     * 根据缩放比例绘制文字
     * 
     * @param sc
     */
    public void drawWords(float sc) {

        if (mWordsRect != null) {
            changeWordsRect(sc);
            drawWords(mResultBmp, sc);
        }
    }

    /**
     * 生成效果图
     * 
     * @param scale 预览比例
     */
    public void makeResultBmp(float scale) {
        mNeedInavlidate = 1;
        if (mNeedInavlidate == 0)
            return;

        if (mScaleX <= 0.0 || mScaleY <= 0.0) {
            mScaleX = 1.0f;
            mScaleY = 1.0f;
        }

        float sc;
        int nResultW = mResultW;
        int nResultH = mResultH;
        forceScale();// 限制缩放比例
        sc = mScaleX > mScaleY ? mScaleX : mScaleY;
        sc = sc * scale;
        mScalePre = sc;
        zoomToResultPic(sc);
        drawWords(sc);

        if (mAngle != 0) {
            rotateToResultPic();
        }

        if (mOperateStyle != OperateMoving)// mOperateStyle == OperateZooming ||
                                           // mOperateStyle == OperateRoting ||
                                           // mOperateStyle == OperateMultiPnt)
        {
            getXYAfterZooming(nResultW, nResultH);
        }

        getOpearatePntXY();

        mNeedInavlidate = 0;
    }

    /**
     * 将图片按照预览比例绘制到画布上
     * 
     * @param canvas
     * @param paint
     * @param scale预览比例
     */

    public void drawImage(Canvas canvas, Paint paint, float scale) {
        mScale = scale;
        float sc = mScaleX > mScaleY ? mScaleX : mScaleY;

        zoomToResultPic(sc);
        drawWords(sc);

        if (mAngle != 0) {
            rotateToResultPic();
            canvas.drawBitmap(mResultBmp, mResultX / scale
                    - (mResultWR - mResultW) / 2, mResultY / scale
                    - (mResultHR - mResultH) / 2, paint);
        } else {
            canvas.drawBitmap(mResultBmp, mResultX / scale, mResultY / scale,
                    paint);
        }

    }

    /**
     * 获取so库所用的文字图片
     * 
     * @param scale 实际图片/sd预览图片 scalePre =
     * @param val
     * @return
     */
    public Bitmap getRealTextImage(float scale, float scalePre, float val[]) {
        float sc = scale * mScalePre / scalePre;
        val[2] = 1;

        if (mAngle != 0) {
            val[0] = mResultX - (mResultWR - mResultW) / 2;
            val[1] = mResultY - (mResultHR - mResultH) / 2;
        } else {
            val[0] = mResultX;
            val[1] = mResultY;
        }

        if (mSrcBmp.getWidth() * sc > 600) {// 限定最大大小，防止内存不足
            float tempSc = sc;
            sc = 1.0f * 600 / mSrcBmp.getWidth();
            val[2] = 1.0f * tempSc / sc;
            // MTDebug.Print("MTXX", "getRealTextImage new scale="+val[2]);
        }
        // MTDebug.Print("MTXX",
        // "getRealTextImage scale="+scale+" scalePre="+scalePre+" mScalePre="+mScalePre);
        zoomToResultPic(sc);
        drawWords(sc);

        if (mAngle != 0) {
            rotateToResultPic();
        }

        //
        // 计算偏移量
        sc = scale / scalePre;
        val[0] = val[0] * sc;
        val[1] = val[1] * sc;
        // MTDebug.Print("MTXX", "getRealTextImage2 new scale="+val[2]);
        return mResultBmp;
    }

    // private class WordPlace {
    // private String mWords;
    // private Rect rect = new Rect(0, 0, 0, 0);
    // private Canvas mCanvas;
    // private Paint mpaint;
    //
    // public WordPlace(String words, int left, int top, int right,
    // int bottom, Canvas canvas) {
    // this.mWords = words;
    // rect.left = left;
    // rect.top = top;
    // rect.right = right;
    // rect.bottom = bottom;
    //
    // mCanvas = canvas;
    // }
    //
    // public void initDefaultValues(Paint paint) {
    // mpaint = paint;
    // }
    //
    // public void drawWords(int flag) {
    // // int w = (int)mpaint.measureText(mWords);
    // // int left = rect.left+((rect.width()<w)?0:(rect.width()-w)/2);
    // mCanvas.drawText(mWords, rect.left + 20, rect.top + rect.height()
    // / 2, mpaint);
    // }
    //
    // }

    public int getOrginx() {
        return mResultX;
    }

    public int getOrginy() {
        return mResultY;
    }

    // yaogang.hao
    public Bitmap getBubbleSrc()
    {
        return mSrcBmp;
    }

    // yaogang.hao
    public Bitmap getBubblePreview()
    {
        return mResultBmp;
    }

}
