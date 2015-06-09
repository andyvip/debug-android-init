
package com.jrdcom.mt.widget;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import com.jrdcom.mt.mtxx.tools.PicUnit;
import com.jrdcom.mt.mtxx.tools.Point;
import com.jrdcom.mt.mtxx.tools.ToolBubble;
import com.jrdcom.mt.mtxx.tools.WordsPicUnit;
import com.jrdcom.mt.mtxx.tools.BitmapOperate;
import com.jrdcom.mt.core.ImageProcess;
import com.jrdcom.mt.core.ToolRotation;
import com.jrdcom.mt.core.ToolText;
import com.jrdcom.mt.util.MyData;
import com.mt.util.tools.VerifyManager;
import com.mt.util.tools.VerifyMothod;
import com.jrdcom.mt.core.*;
import com.jrdcom.mt.mtxx.tools.*;
import com.jrdcom.android.gallery3d.R;
//import com.mt.util.tools.VerifyManager;
//import com.mt.util.tools.VerifyMothod;

public class ViewEditWords extends View
{
    /** selected state */
    public static final int Selected = 1;
    public static final int UnSelected = 0;
    /** operate type */
    public static final int OperateMoving = 0;
    public static final int OperateZooming = 1;
    public static final int OperateRoting = 2;
    public static final int OperateMultiPnt = 3;
    public static final int Max_Bubble_XY = 600;

    private float mStartX;
    private float mStartY;
    private boolean mCaptured;

    private int nViewWidth;
    private int nViewHeight;
    private int nSrcPosX;
    private int nSrcPosY;
    public float fSrcScale;
    public PicCanvas mPicCanvas;
    public Bitmap mFrameTop; // button to adjust directions
    public Bitmap mFrameTopSel;
    public Bitmap bmpBack;

    private OnViewEditWordsTouchListener mListener = null;

    /**
     * to set the touch listeners
     * 
     * @param theListener
     */
    public void setOnViewEditWordsTouchListener(OnViewEditWordsTouchListener theListener) {
        mListener = theListener;
    }

    /**
     * get the current listener
     * 
     * @return
     */
    public OnViewEditWordsTouchListener getOnViewEditWordsTouchListener() {
        return mListener;
    }

    public interface OnViewEditWordsTouchListener {
        /**
         * be touching 
         */
        public void onTouchBegan();
    }

    public ViewEditWords(Context context, AttributeSet attrs) {
        super(context, attrs);
        initial();
    }

    public void initial() {
        nViewWidth = MyData.nScreenW;
        nViewHeight = MyData.nScreenH - (int) (90 * MyData.nDensity);

        bmpBack = BitmapOperate.FittingWindow(MyData.bmpDst, (int) (nViewWidth - 20),
                (int) (nViewHeight - 20), false);
        nSrcPosX = (nViewWidth - bmpBack.getWidth()) / 2;
        nSrcPosY = (nViewHeight - bmpBack.getHeight()) / 2;
        fSrcScale = 1.0f * bmpBack.getWidth() / MyData.bmpDst.getWidth();
        mPicCanvas = new PicCanvas(bmpBack);

        mPicCanvas.initPreView(nViewWidth, nViewHeight, nSrcPosX, nSrcPosY);
        mFrameTop = BitmapFactory.decodeResource(getResources(), R.drawable.text_top_pnt_a);
    }

    public void release() {
        if (mPicCanvas != null)
            mPicCanvas.release();
    }

    public void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawBitmap(mPicCanvas.composePic(fSrcScale), 0, 0, null);

    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {

        int pointerCount = VerifyManager.getPointerCount(event);
        float x, y = 0;

        x = event.getX() - nSrcPosX;
        y = event.getY() - nSrcPosY;

        switch (event.getAction() & 0xff) { // MotionEvent.ACTION_MASK)
                                            // //MotionEvent.ACTION_MASK
            case MotionEvent.ACTION_DOWN:
                if (-1 == mPicCanvas.updateSelUnit(x, y)) {
                    mCaptured = false;
                    invalidate();

                    break;
                }
                else {
                    mCaptured = true;
                    mStartX = x;
                    mStartY = y;
                }
                invalidate();

                if (null != mListener) {
                    // notification
                    mListener.onTouchBegan();
                }

                break;
            case 5:// MotionEvent.ACTION_POINTER_DOWN:
                if (pointerCount == 2) {
                    float x1, y1 = 0;
                    float x0, y0 = 0;

                    x0 = VerifyMothod.getX(event, 0) - nSrcPosX;
                    y0 = VerifyMothod.getY(event, 0) - nSrcPosY;
                    x1 = VerifyMothod.getX(event, 1) - nSrcPosX;
                    y1 = VerifyMothod.getY(event, 1) - nSrcPosY;

                    float d1 = (x0 - mStartX) * (x0 - mStartX) + (y0 - mStartY) * (y0 - mStartY);
                    float d2 = (x1 - mStartX) * (x1 - mStartX) + (y1 - mStartY) * (y1 - mStartY);

                    if (d1 < d2) {
                        mStartX = x0;
                        mStartY = y0;
                        mPicCanvas.initMultiPnt(x0, y0, x1, y1);
                    }
                    else {
                        mStartX = x1;
                        mStartY = y1;
                        mPicCanvas.initMultiPnt(x1, y1, x0, y0);
                    }
                }
                break;
            case MotionEvent.ACTION_MOVE:
                if (mCaptured) {
                    if (pointerCount == 2) {
                        float x1, y1 = 0;
                        float x0, y0 = 0;
                        x0 = VerifyMothod.getX(event, 0) - nSrcPosX;
                        y0 = VerifyMothod.getY(event, 0) - nSrcPosY;

                        x1 = VerifyMothod.getX(event, 1) - nSrcPosX;
                        y1 = VerifyMothod.getY(event, 1) - nSrcPosY;

                        float d1 = (x0 - mStartX) * (x0 - mStartX) + (y0 - mStartY)
                                * (y0 - mStartY);
                        float d2 = (x1 - mStartX) * (x1 - mStartX) + (y1 - mStartY)
                                * (y1 - mStartY);

                        if (d1 < d2) {
                            mStartX = x0;
                            mStartY = y0;
                            mPicCanvas.multiPnt(mPicCanvas.mPicSelIndex, x0, y0, x1, y1);
                        }
                        else {
                            mStartX = x1;
                            mStartY = y1;
                            mPicCanvas.multiPnt(mPicCanvas.mPicSelIndex, x1, y1, x0, y0);
                        }
                    }
                    else {
                        mPicCanvas.operatePicUnit(mPicCanvas.mPicSelIndex, x - mStartX,
                                y - mStartY, x, y);
                        mStartX = x;
                        mStartY = y;
                    }

                    invalidate();
                }
                break;
            case MotionEvent.ACTION_UP:
                mPicCanvas.resetStatus();
                invalidate();
                mCaptured = false;
                break;
            case 6:// MotionEvent.ACTION_POINTER_UP://MotionEvent.ACTION_POINTER_UP

                break;
            default:
                break;
        }

        return true;
    }

    public boolean savePic() {
        // MTDebug.Print("MTXX", "ViewEditWords__savePic");
        // float val[] = new float[3];
        // Bitmap bmp =
        // mPicCanvas.getRealTextImage(MyData.mOptMiddle.mRealScale, val);
        // // MTDebug.Print("MTXX", "val[2]="+val[2]);
        // int data[] = ImageProcess.bitmap2IntARGB(bmp);
        // // MTDebug.Print("MTXX",
        // "savePic w="+bmp.getWidth()+" h="+bmp.getHeight());
        //
        // MyData.mOptMiddle.addImage(data,bmp.getWidth(),bmp.getHeight(),(int)val[0],(int)val[1],val[2]);
        // bmp.recycle();
        // MTDebug.Print("MTXX", "ViewEditWords__savePic3");
        return true;
    }

    /**
     * PicUnit with the word region
     * 
     * @param bmp
     * @param rc
     * @param x
     * @param y
     */
    public void addPic(Bitmap bmp, Rect rc, int x, int y) {
        mPicCanvas.addPicUnit(bmp, rc, x, y, fSrcScale);
    }

    public void setFontItalic(boolean isItalic) {
        mPicCanvas.setFontItalic(isItalic);
    }

    public void setFontShadow(boolean isShadow) {
        mPicCanvas.setFontShadow(isShadow);
    }

    public void setImageString(String str)
    {
        mPicCanvas.setSelPicUnitWords(0, str);
    }

    public void setImageBubble(Bitmap bg, Rect rect)
    {
        mPicCanvas.setSelPicUnitSrcBmp(0, bg, rect);
    }

    public void setFontColor(int color)
    {
        mPicCanvas.setFontCorlor(color);
    }

    public Bitmap getResultWorldBitmap()
    {
        return mPicCanvas.getResultBitmap();
    }
    public float getBubbleScaleValue()
    {
        return mPicCanvas.getBubbleScale();
    }
    public float[] getPercentXY()
    {
    	return mPicCanvas.getPercentXY();
    }
    
    /*
     * compose images
     */
    public class PicCanvas
    {
        public int mWidth;
        public int mHeight;
        private Bitmap mBmp;//preview image
        private Paint mPaint;
        private PicUnit[] mPicGroup;
        private int mPicNum = 0;
        public int mPicSelIndex = -1;
        private Bitmap mPreViewBmp;//preview image
        private Bitmap mPreViewBg;//backgound image
        private Canvas mPreViewCanvas;
        private int nPosX;
        private int nPosY;
        private float mPreScale;

        public void initPreView(int width, int height, int x, int y) {
            mPreViewBmp = Bitmap.createBitmap(width, height, Config.ARGB_8888);
            mPreViewCanvas = new Canvas(mPreViewBmp);

            mPreViewBg = BitmapFactory.decodeResource(getResources(), R.drawable.bg);
            // obtain the backgound image
            int im = (mWidth + 2 * x) / mPreViewBg.getWidth() + 1;
            int jm = (mHeight + 2 * y) / mPreViewBg.getHeight() + 1;
            for (int j = 0; j < jm; j++)
                for (int i = 0; i < im; i++)
                {
                    mPreViewCanvas.drawBitmap(mPreViewBg, mPreViewBg.getWidth() * i,
                            mPreViewBg.getHeight() * j, mPaint);
                }

            mPreViewCanvas.drawBitmap(mBmp, x, y, mPaint);
            mPreViewBg = mPreViewBmp.copy(Config.ARGB_8888, true);
            nPosX = x;
            nPosY = y;
        }

        public void release() {
            try {
                if (mBmp != null && !mBmp.isRecycled()) {
                    mBmp.recycle();
                    mBmp = null;
                }
                if (mPreViewBmp != null && !mPreViewBmp.isRecycled()) {
                    mPreViewBmp.recycle();
                    mPreViewBmp = null;
                }
                if (mPreViewBg != null && !mPreViewBg.isRecycled()) {
                    mPreViewBg.recycle();
                    mPreViewBg = null;
                }

            } catch (Exception e) {
                // MTDebug.PrintError(e);
            }
        }

        public PicCanvas(Bitmap bmp) {
            mWidth = bmp.getWidth();
            mHeight = bmp.getHeight();

            mBmp = bmp;
            mPaint = new Paint();
            mPaint.setAntiAlias(true);
            mPaint.setDither(false);

            mPicGroup = new PicUnit[10];
            for (int i = 0; i < 10; i++) {
                mPicGroup[i] = null;
            }
        }

        public void setFontCorlor(int c) {
            if (mPicGroup[0] != null)
                ((WordsPicUnit) mPicGroup[0]).setFontCorlor(c);
        }

        public void setFontItalic(boolean isItalic) {
            if (mPicGroup[0] != null)
                ((WordsPicUnit) mPicGroup[0]).setWordsItalic(isItalic);
        }

        public void setFontShadow(boolean isShadow) {
            if (mPicGroup[0] != null)
                ((WordsPicUnit) mPicGroup[0]).setWordsShadow(isShadow);
        }

        /**
         * add the pic unit with word and scaled image
         * 
         * @param bmp
         * @param rc
         * @param x
         * @param y
         * @param sc
         */
        public void addPicUnit(Bitmap bmp, Rect rc, int x, int y, float sc) {
            if (mPicGroup[mPicNum] != null) {
                mPicGroup[mPicNum] = null;
                System.gc();
            }
            mPreScale = sc;
            mPicSelIndex = mPicNum;

            mPicGroup[mPicNum] = new WordsPicUnit(bmp, rc, "请输入文字", nPosX, nPosY);
            mPicGroup[mPicNum].setMaxRightBottom(mWidth, mHeight);

            mPicGroup[mPicNum].mStatus = Selected;
            mPicGroup[mPicNum].setScale(sc);

            if (mPicGroup[mPicNum].mIsStaticWords == 1) {
                float scs = MyData.bmpDst.getWidth() / 280.0f;
                mPicGroup[mPicNum].setScale(scs, scs);
            }
            else
            {
                float maxscale =  mPicGroup[mPicNum].getMaxScaleValue();
                
                mPicGroup[mPicNum].setScale(maxscale, maxscale);
            }
            mPicGroup[mPicNum].forceScale();

            // float scxy =
            // mPicGroup[mPicNum].mScale*mPicGroup[mPicNum].mScaleX;
            x = mWidth / 2 - (int) (mPicGroup[mPicNum].mWidth / 2);// *scxy);
            y = mHeight / 2 - (int) (mPicGroup[mPicNum].mHeight / 2);// *scxy);

            mPicGroup[mPicNum].movePic(x, y);

            if (mPicGroup[mPicNum].mIsStaticWords == 1) {
                mPicGroup[mPicNum].mNeedInavlidate = 1;
                mPicGroup[mPicNum].mOperateStyle = 7;
                mPicGroup[mPicNum].makeResultBmp(sc);
                mPicGroup[mPicNum].mOperateStyle = 0;
            }
            if (mPicNum < 9)
                mPicNum++;
            else
                mPicNum = 9;
        }

        public void resetStatus() {
            if (mPicGroup[0] != null) {
                mPicGroup[0].mStatus = Selected;
                mPicGroup[0].mOperateStyle = 0;
            }
        }

        /**
         * update the state of picture unit acoording to x,y coordiates
         * 
         * @param x
         * @param y
         * @return
         */
        public int updateSelUnit(float x, float y) {
            if (mPicGroup[0] != null)
                mPicGroup[0].mStatus = Selected;
            for (int i = mPicNum - 1; i >= 0; i--) {
                if (mPicGroup[i] != null) {
                    if (mPicGroup[i].isInRect(x, y)) {
                        mPicGroup[i].mStatus = Selected;
                        mPicSelIndex = i;
                        return i;
                    }
                }

            }
            mPicSelIndex = -1;
            return -1;
        }

        public void initMultiPnt(float x0, float y0, float x1, float y1) {
            if (mPicSelIndex == -1)
                return;
            if (mPicGroup[mPicSelIndex] != null) {
                mPicGroup[mPicSelIndex].initMultiPnt(x0, y0, x1, y1);
            }
        }

        public void movePicUnit(int index, float disx, float disy) {
            if (mPicGroup[index] != null) {
                mPicGroup[index].movePicTo(disx, disy);
            }
        }

        public void multiPnt(int index, float x0, float y0, float x1, float y1) {
            if (index == -1)
                return;
            if (mPicGroup[index] != null) {
                mPicGroup[index].OperateMultiPnt(x0, y0, x1, y1);
            }
        }

        public void operatePicUnit(int index, float dx, float dy, float tx, float ty) {
            if (index == -1)
                return;
            if (mPicGroup[index] != null) {
                mPicGroup[index].OperatePic((int) dx, (int) dy, (int) tx, (int) ty);
            }
        }

        public Bitmap composePic(float scale) {
            mPreViewCanvas.drawBitmap(mPreViewBg, 0, 0, mPaint);
            for (int i = 0; i < 10; i++) {
                if (mPicGroup[i] != null) {
                    mPicGroup[i].makeResultBmp(scale);
                    mPicGroup[i].drawImage(mPreViewCanvas, mPaint, nPosX, nPosY);
                    if (i == mPicSelIndex)
                        drawFramePicUnit(mPicGroup[i]);
                }
            }
            if (mPicGroup[0] != null && mPicSelIndex == -1)
                drawFramePicUnit(mPicGroup[0]);

            return mPreViewBmp;
        }

        /**
         * obtain get final image with word and handle it in the so file library
         * 
         * @param val the x,y coordinate of words to scale ,2 bits
         * @return
         */
        public Bitmap getRealTextImage(float scale, float val[]) {
            Bitmap bmp = ((WordsPicUnit) mPicGroup[0]).getRealTextImage(scale, mPreScale, val);
            return bmp;
        }

        public void drawFramePnt(PicUnit pic, Bitmap bmp, float x, float y, float off) {
            if (pic.getAngle() != 0) {
                Point pnt = pic.getFramePntXY(x, y);
                x = pnt.x;
                y = pnt.y;
            }
            // fristly frametop not added
            if (bmp == null)
                return;
            mPreViewCanvas.drawBitmap(bmp, x - off + nPosX, y - off + nPosY, mPaint);
        }

        public void drawFrameRect(PicUnit pic) {
            float xoff = 50;
            if (pic.mOperateStyle == OperateRoting) {

            }
            else {
                switch (pic.mOptPntPos)
                {
                    case 0:
                        drawFramePnt(pic, mFrameTop, pic.mResultX + pic.mResultW, pic.mResultY
                                + pic.mResultH, xoff);
                        break;
                    case 1:
                        drawFramePnt(pic, mFrameTop, pic.mResultX + pic.mResultW, pic.mResultY,
                                xoff);
                        break;
                    case 2:
                        drawFramePnt(pic, mFrameTop, pic.mResultX, pic.mResultY, xoff);
                        break;
                    case 3:
                        drawFramePnt(pic, mFrameTop, pic.mResultX, pic.mResultY + pic.mResultH,
                                xoff);
                        break;
                    case 4:
                        drawFramePnt(pic, mFrameTop, pic.mResultX + pic.mResultW / 2, pic.mResultY
                                + pic.mResultH / 2, xoff);
                        break;
                }
            }
        }

        public void drawFramePicUnit(PicUnit pic) {
            if (pic.mStatus == UnSelected)
                return;
            drawFrameRect(pic);
        }

        public void setSelPicUnitWords(int index, String str) {
            if (mPicGroup[index] != null)
                ((WordsPicUnit) mPicGroup[index]).setWords(str);
        }

        public void setSelPicUnitSrcBmp(int index, Bitmap bmp, Rect rc) {
            if (mPicGroup[index] != null)
                ((WordsPicUnit) mPicGroup[index]).setSrcBmp(bmp, rc);
        }
        
        public Bitmap getResultBitmap()
        {
            if (mPicGroup[0] != null)
               return  ((WordsPicUnit) mPicGroup[0]).getResultBmp();
            return null;
        }
        public float getBubbleScale()
        {
            if (mPicGroup[0] != null)
                return  ((WordsPicUnit) mPicGroup[0]).mScale/fSrcScale;
             return 1.0f;
        }
        public float[] getPercentXY()
        {
        	if (mPicGroup[0] != null)
        	{
        		
        		 float x = ((WordsPicUnit) mPicGroup[0]).getOrginx()/(mWidth*1.0f)/fSrcScale;
        		 float y = ((WordsPicUnit) mPicGroup[0]).getOrginy()/(mHeight*1.0f)/fSrcScale;
        		return new float[]{x,y};
        	}
        	return null;
        }
    }

  /*********** yaogang.hao begin*********************/
    
    public Bitmap getBubblePreview()
    {
        if (mPicCanvas.mPicGroup[0] != null)
        {
            return ((WordsPicUnit)mPicCanvas.mPicGroup[0]).getBubblePreview();
        }
        return null;
            
    }
    public Bitmap getBubbleSrc()
    {
        if (mPicCanvas.mPicGroup[0] != null)
        {
            return ((WordsPicUnit)mPicCanvas.mPicGroup[0]).getBubbleSrc();
        }
        return null;
    }
    public int getResultx()
    {
        if (mPicCanvas.mPicGroup[0] != null)
        {
            return ((WordsPicUnit)mPicCanvas.mPicGroup[0]).getOrginx();
        }
        return 0;
    }
    public int getResulty()
    {
        if (mPicCanvas.mPicGroup[0] != null)
        {
            return ((WordsPicUnit)mPicCanvas.mPicGroup[0]).getOrginy();
        }
        return 0;
    }
    public void procImage(ToolText mtool)
    {
        float[] vals = new float[3];//x,y,scale
        BitmapOperate.FittingWindowSize(MyData.bmpDst.getWidth(),MyData.bmpDst.getHeight() , 
                (int) (nViewWidth - 20), (int) (nViewHeight - 20), vals);
        Bitmap  mBubblePreview = getBubblePreview();
        Bitmap  mBubbleBg = getBubbleSrc();
        float scale = (float)(mBubblePreview.getWidth())/vals[0];
        float bubbleScale = (scale*MyData.bmpDst.getWidth())/mBubbleBg.getWidth();
        
         float val[] = new float[3];
         Bitmap bmp =
         mPicCanvas.getRealTextImage(1.0f/bubbleScale, val);
        
        mtool.procImage(bmp, 1.0f-(vals[0]-getResultx())/vals[0], 
                1.0f-(vals[1]-getResulty())/vals[1],bubbleScale*mtool.getScaleBetweenRealAndShow());
        
    }
    /*********** yaogang.hao end*********************/
}
