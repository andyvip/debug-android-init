package com.jrdcom.mt.mtxx.tools;

import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;

import com.mt.mtxx.image.WordsPlace;
import com.jrdcom.mt.util.MyData;


public class PicUnit {
	
	/** 选中状态 */
	public static final int Selected = 1;
	public static final int UnSelected = 0;
	/** 操作类型*/
	public static final int OperateMoving = 0;
	public static final int OperateZooming = 1;
	public static final int OperateRoting = 2;
	public static final int OperateMultiPnt = 3;
    public static final int Max_Bubble_XY = 800;// 600;
    // public static final int Max_Bubble_XY = 480;
	
	public int mWidth; //原图片素材的宽。
	public int mHeight;// 原图片素材的高。
	protected int minWidth = 30;
	

	protected int mOrginX = 0;
	protected int mOrginY = 0; //原图片位置左上角坐标。
	
	public int mResultX = 0;
	public int mResultY = 0;//效果图位置左上角坐标。
	           
	public int mResultW = 0;//效果图的宽度。
	public int mResultH = 0;//效果图的高度。
	
	protected float mAngle = 0.0f;//旋转图片的角度。
	
	public float mScale = 1.0f;//预览比例。
	public float mScaleX = 1.0f;//缩放时X方向缩放的比例。
	public float mScaleY = 1.0f;//缩放时Y方向缩放的比例。
	protected float mScalePre = 1.0f;
	protected Matrix mMatrix;   //生成图片使用的矩阵。
	
	protected Bitmap mSrcBmp;   //原图。
	protected Bitmap mResultBmp;//效果图。
	
	public int mStatus = UnSelected; //是否选中的标志位。
	
	public int mOperateStyle = 0; //操作的类型。	
	
	public int mMovingFrom = 0;  //缩放时四个方向。	
	public int mNeedInavlidate = 1; //是否需要重新生成效果图。
	
	public int mResultWR;  //图片旋转后的宽。
	public int mResultHR;  //图片旋转后的高。
	
	public int mIsStaticWords = 0;  //文字是否为静态文字
	public float mRotateR = 0.0f; //图片旋转前 两点的初始距离（多点是为两个点的初始距离）
	public float mScaleR = 1.0f; //图片缩放前比例
	public float mAngleR= 0.0f;  //多点触摸时 两个点的初始角度
	public float mAngleS = 0.0f;//图片旋转前角度
	protected Rect mWordsRect = null; //图片文字的矩形区域
	protected Rect mWordsResultRC = null;  //效果图中文字的矩形区域
	protected Paint mPaint;
	protected String mWords;  //图片文字
	private int maxRight;
	private int maxBottom;

	public int mOptPntPos= 0;
	
	public PicUnit()
	{
		mWidth = 0;
		mHeight = 0;
		//mSrcPic = null;
		mMatrix = null;
		mSrcBmp = null;
		mNeedInavlidate = 0;
	}
	/*
	 * 初始化只有pic的单元
	 */
	
	public PicUnit(Bitmap bmp)
	{
		mWidth = bmp.getWidth();
		mHeight = bmp.getHeight();
		mResultW = mWidth;
		mResultH = mHeight;
		mResultWR = mWidth; 
		mResultHR = mHeight;
	    mScaleX = 1.0f;
	    mScaleY = 1.0f;	
		mMatrix = new Matrix();
		mSrcBmp = bmp;
		mNeedInavlidate = 1;
	}
		   
	public PicUnit(Bitmap bmp, Rect rc, String words , int x, int y){
		if(bmp == null){
            // mIsStaticWords = 1;
            // bmp = getStringBmp(words);
            bmp = getDynimicStringBmp(words);
			rc.left = 0;
			rc.top = 0;
			rc.right = bmp.getWidth() ;
			rc.bottom = bmp.getHeight() ;
		}
		
		mWidth = bmp.getWidth();
		mHeight = bmp.getHeight();
//		MTDebug.Print("TEMP", "_____PicUnit mWidth="+mWidth+" mHeight="+mHeight);
		mResultW = mWidth;
		mResultH = mHeight;
		mResultWR = mWidth;
		mResultHR = mHeight;
	    mScaleX = 1.0f;
	    mScaleY = 1.0f;
		
		mMatrix = new Matrix();
		mSrcBmp = bmp;
		mNeedInavlidate = 1;
		mWordsRect = rc;
		mWordsResultRC = new Rect();
		mWords = words;
		
		mPaint = new Paint();
		mPaint.setAntiAlias(true);
		mPaint.setDither(false);
		mPaint.setTypeface(Typeface.create(Typeface.SERIF,
                Typeface.NORMAL));
		mPaint.setTextAlign(Paint.Align.LEFT);
		
	}
	
	/*
	 * 初始化带有文字的单元
	 */
	public int getRowsNum(String str ,Paint paint){
		float [] widths = new float[str.length()];
		paint.getTextWidths(str, 0, str.length(), widths);
		float sum = 0;
		int rowsnum = 1;
		for(int i = 0 ;i< str.length();i++){
			sum += widths[i];
			if(sum*MyData.nBmpDstW/280.0f > MyData.nBmpDstW){
				rowsnum++;
				sum = 0;
			}
		}
		return rowsnum;
	}
    public Bitmap getDynimicStringBmp(String str) {
        int maxWidth = 0;
        int maxHeight = 0;
        Rect rc = new Rect();
        Paint paint = new Paint();
        paint.setTypeface(Typeface.create(Typeface.SERIF, Typeface.ITALIC));
        paint.setTextSize((float) (20.0) * mScale);

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
        if (rowsnum == 1) {
            paint.getTextBounds(str, 0, str.length(), rc);
            maxWidth = rc.width() + 20;
        } else {
            maxWidth = MyData.nBmpDstW;
        }
        maxHeight = rowsnum * (20 + WordsPlace.SHADOW_DISTANCE + WordsPlace.SHADOW_DISTANCE) + 20
                + 30;
        Bitmap bmp = Bitmap.createBitmap(maxWidth, maxHeight, Config.ARGB_8888);
        return bmp;
    }
	public Bitmap getStringBmp(String str){
		Rect rc = new Rect();
		Paint paint = new Paint();
		paint.setTypeface(Typeface.create(Typeface.SERIF,
                Typeface.ITALIC));
		paint.setTextSize((float)(20.0));

		int maxWidth = 0 ;
		int rowsBegin = 0;
		int rowsnum = 0;
		char[] strchar = str.toCharArray();
		for(int i = 0;i<strchar.length;i++){
		   if(strchar[i]=='\n'||i== strchar.length-1){
			   String tmp;
			   if(i== strchar.length-1)
			       tmp = str.substring(rowsBegin, i+1);
			   else
				   tmp = str.substring(rowsBegin, i);
			  
			   if(getRowsNum(tmp ,paint)>1){
				   rowsnum += getRowsNum(tmp ,paint);
				   maxWidth = (int)(280.0f);
			   }
	
			   paint.getTextBounds(tmp, 0, tmp.length(),rc );
			   if(rc.width() > maxWidth){
				   maxWidth = rc.width();
			   }
			   rowsBegin = i+1;
			   //i++;
			   rowsnum++;
		   }
		   
		}

        int maxHeight = rowsnum * (20 + WordsPlace.SHADOW_DISTANCE + WordsPlace.SHADOW_DISTANCE)
                + 20;
        maxWidth += 20;
		if(maxWidth > Max_Bubble_XY){
			maxWidth = Max_Bubble_XY;
		}
		if(maxHeight > Max_Bubble_XY){
			maxHeight = Max_Bubble_XY;
		}
		
		Bitmap bmp = Bitmap.createBitmap( maxWidth,
				 maxHeight, Config.ARGB_8888);
		return bmp;
	}
	
	public void setMaxRightBottom(int right, int bottom)
	{
		maxRight = right;
		maxBottom = bottom;
	}
	
	public boolean isOutOfView(float x,float y)
	{
		if(x>=0 && x < maxRight && y>=0 && y< maxBottom)
			return false;
		else 
			return true;
	}
	
	public void getOpearatePntXY()
	{
		
		int dis = 15;
		
	   Point rightBottom	 = getFramePntXY(mResultX+mResultW,mResultY+mResultH);
	   
	   if(!isOutOfView(rightBottom.x + dis ,rightBottom.y + dis))
	   {
		   mOptPntPos = 0;
		   return;
	   }
	   
	   Point rightTop	 = getFramePntXY(mResultX+mResultW,mResultY +mResultY);
	   
	   if(!isOutOfView(rightTop.x + dis ,rightTop.y - dis))
	   {
		   mOptPntPos = 1;
		   return;
	   }
	   
	   Point leftTop	= getFramePntXY(mResultX, mResultY);
	   
	   if(!isOutOfView(leftTop.x - dis ,leftTop.y -dis))
	   {
		   mOptPntPos = 2;
		   return;
	   }
	   
	   Point leftBottom	 = getFramePntXY(mResultX,mResultY+mResultH);
	  
	   if(!isOutOfView(leftBottom.x - dis,leftBottom.y + dis))
	   {
		   mOptPntPos = 3;
		   return;
	   }
	   
	   mOptPntPos = 4;
		
		
	}
	
	

	
	public void changeXYAfterChangeBg()
	{
		float sc;

		sc = mScaleX > mScaleY?mScaleX:mScaleY; 
		sc = sc*mScale;		
		
		mResultX = mResultX - (int)((mSrcBmp.getWidth() - mWidth)/2*sc);
		mResultY = mResultY - (int)((mSrcBmp.getHeight() - mHeight)/2*sc);
		
	}
	
	
	public void setAngle(float angle)
	{
		mAngle = angle;
	}
	
	public float getAngle()
	{
		return mAngle;
	}
	
	public void setScale(float scale)
	{
		mScale = scale;
	}
	
	public float getScale()
	{
		return mScale;
	}
	public void setScale(float sx, float sy)
	{
		mScaleX = sx;
		mScaleY = sy;
	}



	/**
	 * 移动图片按照差值
	 * @param x
	 * @param y
	 */
	public void movePicTo(float dx, float dy)
	{
		
		
		mOrginX += (int)dx;
		mOrginY += (int)dy;
		mResultX += (int)dx;
		mResultY += (int)dy;
		
		if(mResultX+mResultW/2 > maxRight)
		{
			mResultX = maxRight - mResultW/2;
		}
		
		if(mResultX+mResultW/2 < 0)
		{
			mResultX = 0 - mResultW/2;
		}
		
		if(mResultY+mResultH/2 > maxBottom)
		{
			mResultY = maxBottom - mResultH/2;
		}
		
		if(mResultY+mResultH/2 < 0)
		{
			mResultY = 0 - mResultH/2;
		}					
		
		
		getOpearatePntXY();
		
	}
	
	/**
	 * 多点触摸初始化
	 * @param x0
	 * @param y0
	 * @param x1
	 * @param y1
	 */
	
	public void initMultiPnt(float x0, float y0, float x1, float y1)
	{
		if(mOperateStyle == OperateMoving ||mOperateStyle == OperateMultiPnt)
		{
			float  dx = x1 - x0;
			float  dy = y1 - y0;
		
			
			mAngleR = getAngleFromDxDy(dx, dy);
			mAngleS = mAngle;
			
			mScaleR = mScaleX;
			mRotateR =(float) Math.sqrt(dx*dx + dy*dy);
			mOperateStyle = OperateMultiPnt;
			
		}
	}
		
	
	
	/**
	 * 移动图片按位置。
	 * @param x
	 * @param y
	 */
	public void movePic(int x, int y)
	{
		mOrginX = x + mResultWR/2;
		mOrginY = y + mResultHR/2;
		mResultX = x;
		mResultY = y;
		
		
		
	}
	/**
	 * 判断点是否在另一个点的周围。
	 * @param orgx
	 * @param orgy
	 * @param x
	 * @param y
	 * @param dis 半径
	 * @return
	 */
	public boolean aroundPoint(int orgx, int orgy, int x, int y, int dis)
	{
		
		if(x-orgx < dis && x-orgx > -dis && y - orgy <dis && y-orgy > -dis)
			return true;
		else
			return false;
	}
	
	/**
	 * 判断手指在不在响应位置。
	 * @param x
	 * @param y
	 * @return
	 */
	
	public boolean disOptPntActive(float x, float y)
	{
		int dis = 30;
		switch(mOptPntPos)
		{
		case 0:
			return aroundPoint(mResultX + mResultW,mResultY + mResultH,(int)x,(int)y, dis);
			
		case 1:
			return aroundPoint(mResultX + mResultW,mResultY,(int)x,(int)y ,dis);
			
		case 2:
			return aroundPoint(mResultX,mResultY,(int)x,(int)y ,dis);
			
		case 3:
			return aroundPoint(mResultX,mResultY+mResultH,(int)x,(int)y ,dis);
		case 4:
			return aroundPoint(mResultX + mResultW/2,mResultY + mResultH/2,(int)x,(int)y ,dis);
		default:
			return false;
			
		
		}
	}
	
	
	/**
	 * 根据点在区域区分操作的类型。
	 * @param x
	 * @param y
	 */
	public void distinguishOperate(float x1, float y1 ,float x, float y)
	{
		
		if(disOptPntActive(x1, y1))
		{
			mOperateStyle = OperateRoting;
			float dx = x - (mResultX + mResultW/2.0f);
			float dy = y - (mResultY + mResultH/2.0f);
			
			mAngleR = getAngleFromDxDy(dx, dy);
			mAngleS = mAngle;
							
			mScaleR = mScaleX;
			if(mOptPntPos == 4)
				mRotateR =100.0f;
			else					
				mRotateR =(float) Math.sqrt(dx*dx + dy*dy);
		}
			
		else
			mOperateStyle = OperateMoving;
	}

    /**
     * 得到旋转后图片的对应点的坐标。
     * 
     * @param x
     * @param y
     * @return
     */
    public Point getRotateXY(float x, float y) {
        float rx, ry = 0.0f;
        float rw = x - (mResultX + mResultW / 2);
        float rh = y - (mResultY + mResultH / 2);
        float angle = -(float) ((mAngle / 180.0) * Math.PI);
        rx = (float) (mResultX + mResultW / 2 + rw * Math.cos(angle) - rh * Math.sin(angle));
        ry = (float) (mResultY + mResultH / 2 + rh * Math.cos(angle) + rw * Math.sin(angle));
        return new Point(rx, ry);
    }

    /**
     * 
     * @param x
     * @param y
     * @return
     */

    public Point getFramePntXY(float x, float y) {
        float rx, ry = 0.0f;
        float rw = x - (mResultX + mResultW / 2);
        float rh = y - (mResultY + mResultH / 2);

        float angle = (float) ((mAngle / 180.0) * Math.PI);
        rx = (float) (mResultX + mResultW / 2 + rw * Math.cos(angle) - rh * Math.sin(angle));
        ry = (float) (mResultY + mResultH / 2 + rh * Math.cos(angle) + rw * Math.sin(angle));

        return new Point(rx, ry);
    }

    /**
     * 判断 当前点是否落在 图片作用范围内。
     * 
     * @param x
     * @param y
     * @return
     */
    public boolean isInRect(float x, float y) {
        Point pnt = getRotateXY(x, y);
        float x1 = pnt.x;
        float y1 = pnt.y;
        int dis = 50;

        if (mStatus == Selected) {
            if (x1 >= mResultX - dis && x1 <= mResultX + dis + mResultW && y1 >= mResultY - dis
                    && y1 <= mResultY + mResultH + dis  /*|| aroundPoint(mResultX + mResultW/2,mResultY - mResultH/2,(int)x,(int)y,dis)*/
                    )
            {
                distinguishOperate(x1, y1, x, y);
                return true;
            }
            return false;

        } else {
            if (x1 >= mResultX && x1 <= mResultX + mResultW && y1 >= mResultY
                    && y1 <= mResultY + mResultH) {
                distinguishOperate(x1, y1, x, y);
		    	return true;
			}   
			else 
				return false;
		}
	}
	/**
	 * 缩放图片。
	 * @param dx
	 * @param dy
	 */
	private void zoomPicUnit(int tx, int ty)
	{
		
		int dx =  (mResultX + mResultW/2) - tx;
		int dy =  (mResultY + mResultH/2) - ty;	
		mScaleX = mScaleY =  (float)(Math.sqrt(dx*dx + dy*dy))/mRotateR*mScaleR;

    }

    /**
     * 计算 x轴相距 dx y轴相距dy的两点间的角度。
     * 
     * @param dx
     * @param dy
     * @return
     */
    public float getAngleFromDxDy(float dx, float dy) {
        if (dx == 0) {
            if (dy > 0)
                return 90.0f;
            else
                return -90.0f;

        }

        float angle;
        angle = (float) (Math.atan((float) (1000.0f * dy) / ((float) dx * 1000.0f)) * 180.0f / Math.PI);

        if (dx < 0)
            angle += 180.0d;

        return (float) angle;
    }

    /**
     * 旋转图片。
     * 
     * @param tx
     * @param ty
     */
    public void rotatePicUnit(int tx, int ty) {
        int dx = tx - (mResultX + mResultW / 2);
        int dy = ty - (mResultY + mResultH / 2);

        float dangle = getAngleFromDxDy(dx, dy) - mAngleR;// 计算当前偏离的角度差
        mAngle = mAngleS + dangle;

        if (mAngle > 360.0f)
            mAngle = 0.0f;
        else if (mAngle < -360.0f)
            mAngle = 0.0f;

        mScaleX = mScaleY = (float) (Math.sqrt(dx * dx + dy * dy)) / mRotateR * mScaleR; // 缩放最初版本只是旋转。

        // double angle = Math.atan((double)dx/(double)dy)/Math.PI * 180.0d;
        // if(dy < 0)
        // angle += 180.0d;
        // mAngle = -(float)angle;
    }

    /**
     * 单点操作 旋转，缩放 ，移动图片。
     * 
     * @param dx
     * @param dy
     * @param tx
     * @param ty
     */
    public void OperatePic(int dx, int dy, int tx, int ty) {
        switch (mOperateStyle) {
        case OperateMoving: {
            movePicTo(dx, dy);
            mStatus = UnSelected;
            mNeedInavlidate = 0;
        }
            break;
        case OperateRoting: {
            rotatePicUnit(tx, ty);
            mNeedInavlidate = 1;
        }
            break;
        case OperateZooming: {
            zoomPicUnit(tx, ty);
            mNeedInavlidate = 1;
        }
            break;

        }
    }

    /**
     * 执行多点操作
     * 
     * @param x0
     *            第一个点的x坐标
     * @param y0
     *            第一个点的y坐标
     * @param x1
     *            第二个点的x坐标
     * @param y1
     *            第二个点的y坐标
     */
    public void OperateMultiPnt(float x0, float y0, float x1, float y1) {

        if (x0 == x1 && y0 == y1)
            return;
        if (mOperateStyle != OperateMultiPnt)
            return;
        mStatus = UnSelected;// 多点旋转时应处于未选中状态。

        float dx = x1 - x0;
        float dy = y1 - y0;

        float dangle = getAngleFromDxDy(dx, dy) - mAngleR;// 计算当前偏离的角度差
        mAngle = mAngleS + dangle;

        if (mAngle > 360.0f)
            mAngle = 0.0f;
        else if (mAngle < -360.0f)
            mAngle = 0.0f;

        mScaleX = mScaleY = (float) (Math.sqrt(dx * dx + dy * dy)) / mRotateR * mScaleR;// 计算缩放比例

        mNeedInavlidate = 1;
    }

    /**
     * 得到缩放后图片的位置坐标。
     * 
     * @param orgw原来的宽
     *            。
     * @param orgh原来的高
     *            。
     */

    public void getXYAfterZooming(int orgw, int orgh) {

        mResultX = mResultX - (mResultW - orgw) / 2;
        mResultY = mResultY - (mResultH - orgh) / 2;

    }

    /**
     * 对文字单元的大小进行限制 大小为 800pix
     */

    public void forceScale() {
        float minLength = mWidth < mHeight ? mWidth : mHeight;

        if (mScale * mScaleX * minLength < minWidth) {
            mScaleY = mScaleX = minWidth / (minLength * mScale);
            return;
        }
        float maxlength = mWidth > mHeight ? mWidth : mHeight;

        if (mScale < 1.0) {
            if (mScaleX * maxlength > Max_Bubble_XY) {
                mScaleY = mScaleX = Max_Bubble_XY / (maxlength);
            }
        } else {
           
            if (mScale * mScaleX * maxlength > Max_Bubble_XY) {
                mScaleY = mScaleX = Max_Bubble_XY / (mScale * maxlength);
                
            }
        }

    }

    public float getMaxScaleValue() {
        float maxlength = mWidth > mHeight ? mWidth : mHeight;
        return Max_Bubble_XY / (mScale * maxlength);
    }
	
	/**
	 * 根据缩放比例得到缩放后的效果图
	 * @param sc
	 */
	public void zoomToResultPic(float sc )
	{
		mMatrix.reset();
		mMatrix.postScale(sc, sc);
	
		mResultBmp = Bitmap.createBitmap(mSrcBmp,0,0,mWidth,mHeight,mMatrix,true);
		
		mResultW = mResultBmp.getWidth();
		mResultH = mResultBmp.getHeight();
		mResultWR = mResultW;
		mResultHR = mResultH;
	}
	
	/**
	 * 根据缩放比例绘制文字
	 * @param sc
	 */
	public void drawWords(float sc){
		if(mWordsRect != null){
			changeWordsRect(sc);
			drawWords(mResultBmp,sc);
		}
	}
	
	/**
	 * 在图片src上打印文字
	 * @param src
	 * @param scale
	 */
	public void drawWords(Bitmap src, float scale){
		Canvas canvas = new Canvas(src);

        // WordsPlace wp = new WordsPlace(mWords,mWordsResultRC.left, mWordsResultRC.top,
        // mWordsResultRC.width(), mWordsResultRC.height(), canvas );
        // wp.initDefaultValues(mPaint);
        // if(mIsStaticWords == 1)
        // wp.drawWords(1);
        // else
        // wp.drawWords(0);
        // BitmapOperate.savePic("/sdcard/2/words.png", src, 2);//test
	}
	
	/**
	 * 根据缩放比例改变文字区域
	 * @param scale
	 */
	public void changeWordsRect(float scale){
		mWordsResultRC.left = (int)(mWordsRect.left*scale);
		mWordsResultRC.top = (int)(mWordsRect.top*scale);
		mWordsResultRC.right = (int)(mWordsRect.right*scale);
		mWordsResultRC.bottom = (int)(mWordsRect.bottom*scale);
	}
	/**
	 * 根据旋转角度得到旋转后的效果图
	 */
	
	public void rotateToResultPic()
	{
		mMatrix.reset();
		mMatrix.postRotate(mAngle);	
	
		mResultBmp = Bitmap.createBitmap(mResultBmp,0,0,mResultW,mResultH,mMatrix,true);
		
		mResultWR = mResultBmp.getWidth();
		mResultHR = mResultBmp.getHeight();
	}		
	
	/**
	 * 生成效果图 
	 * @param scale 预览比例
	 */
	public void makeResultBmp(float scale)
	{
		if(mNeedInavlidate == 0)
			return;
	    
		
		if(mScaleX <= 0.0 || mScaleY <= 0.0)
		{
			mScaleX =1.0f;
			mScaleY =1.0f;
		}
		
		float sc ;
		int nResultW = mResultW;
		int nResultH = mResultH;
		forceScale();//限制缩放比例
	    sc = mScaleX > mScaleY?mScaleX:mScaleY; 
		sc = sc*scale;
		mScalePre = sc;
		zoomToResultPic(sc);		

	    
		if(mAngle != 0)
		{
			rotateToResultPic();
		}
		
        if (mOperateStyle != OperateMoving)// mOperateStyle == OperateZooming || mOperateStyle ==
                                           // OperateRoting || mOperateStyle == OperateMultiPnt)
		{
			getXYAfterZooming(nResultW, nResultH);
		}
		
		getOpearatePntXY();
		
		
		mNeedInavlidate = 0;
	}			
	
	/**
	 * 将图片绘制在画布上。
	 * @param canvas 将要绘制到得画布。
	 * @param paint  绘制使用的的piant。
	 */
	public void drawImage(Canvas canvas, Paint  paint , int x , int y )
	{
		if(mAngle != 0)
		{
		
	        canvas.drawBitmap(mResultBmp, mResultX -(mResultWR - mResultW)/2 + x,  mResultY -(mResultHR - mResultH)/2 + y, paint);

		}
		else
		{
			//px 3/6
			if(mResultBmp != null && !mResultBmp.isRecycled()){
				canvas.drawBitmap(mResultBmp, mResultX + x, mResultY + y, paint);
			}else{
				canvas.drawBitmap(mSrcBmp, mResultX + x, mResultY + y, paint);
			}
			
		}
		//drawDebugInfo(canvas,paint);
		
		
	}
	/**
	 * 将图片按照预览比例绘制到画布上
	 * @param canvas 
	 * @param paint
	 * @param scale预览比例
	 */

	public void drawImage(Canvas canvas, Paint  paint, float scale)
	{
		float sc = mScaleX > mScaleY?mScaleX:mScaleY;
		
		zoomToResultPic(sc);
	//	drawWords(sc);
		
		if(mAngle != 0)
		{	
		    rotateToResultPic();
            canvas.drawBitmap(mResultBmp, mResultX/scale -(mResultWR - mResultW)/2,  mResultY/scale -(mResultHR - mResultH)/2, paint);
		}
		else
		{		//px 3/6
			if(mResultBmp != null && !mResultBmp.isRecycled()){
				canvas.drawBitmap(mResultBmp, mResultX / scale, mResultY / scale, paint);
			}else{
				canvas.drawBitmap(mSrcBmp, mResultX / scale, mResultY / scale, paint);
			}
				
			
		}
		
		
	}	
	

	
	public void release()
	{
		if(mSrcBmp != null)
		{
			mSrcBmp.recycle();
			mSrcBmp = null;
		}
		
		if(mResultBmp != null)
		{
			mResultBmp.recycle();
			mResultBmp = null;
		}
	}
	
	
	public void drawDebugInfo(Canvas canvas, Paint  paint)
	{
		paint.setTextSize(14);
		paint.setColor(Color.RED);
		String str = String.format("mResultX : %d   mResultY: %d ", (int)mResultX, mResultY);
		canvas.drawText(str, 10, 20, paint);
		str = String.format("mRotateR : %f   mScaleR: %f ", mRotateR, mScaleR);
		canvas.drawText(str, 10, 40, paint);
		
		str = String.format("mAngleS : %f   mAngleR: %f ", mAngleS, mAngleR);
		canvas.drawText(str, 10, 60, paint);
		
		str = String.format("mResultW : %d   mResultH: %d ", mResultW, mResultH);
		canvas.drawText(str, 10, 80, paint);
		
		str = String.format("mResultWR : %d   mResultHR: %d ", mResultWR, mResultHR);
		canvas.drawText(str, 10, 100, paint);
		
		str = String.format("mScaleX : %f   mScale: %f ", mScaleX, mScale);
		canvas.drawText(str, 10, 120, paint);
		
		str = String.format("mOrginX : %d   mOrginY: %d ", mOrginX, mOrginY);
		canvas.drawText(str, 10, 140, paint);
		
		str = String.format("mOrginXfake : %d   mOrginYfake: %d ", mResultX -(mResultWR - mResultW)/2, mResultY -(mResultHR - mResultH)/2);
		canvas.drawText(str, 10, 160, paint);
		
		str = String.format("mOrginXfake1 : %d   mOrginYfake1: %d ", mOrginX - mResultWR /2, mOrginY - mResultHR /2);
		canvas.drawText(str, 10, 180, paint);
		
	}
	


}
