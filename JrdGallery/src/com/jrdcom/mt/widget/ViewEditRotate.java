package com.jrdcom.mt.widget;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.animation.AnimationSet;
import android.view.animation.RotateAnimation;
import android.view.animation.ScaleAnimation;

import com.jrdcom.mt.util.MyData;
import com.jrdcom.mt.core.ToolRotation;
import com.jrdcom.mt.mtxx.tools.BitmapOperate;

/**
 * 旋转View
 * 
 * Last Modified 2012/12/27
 */
public class ViewEditRotate extends View {
	public float m_fAngle = 0;// 滑动条拖动的角度-45~45
	public float mRealAnglePos = 0; // 点击按钮时产生的附加旋转角度 左旋转，右旋转按钮产生的角度
	public int m_nMirror = 0;// =0没有=1左右镜像=2上下镜像
	// 图片宽高
	private int m_nPicWidth;
	private int m_nPicHeight;
	// 控件宽高
	private int nViewWidth = 0;
	private int nViewHeight = 0;

	// 记录按钮点击，左/右旋转，/上下/左右镜像的matrix
	private Matrix matrixRS;
	// 用于实际操作的matrix
	private Matrix matrix;
	// 展示图片
	public Bitmap bmpBack = null;
	// 画笔
	private Paint paint;
	// 用于标记图片所占空间的位置
	private RectF srcRect, dstRect;
	// 用于裁出图片显示区域
	private Rect rect;
	// srcRect所占的空间宽高
	private float srcHeight;
	private float srcWidth;
	// 放大倍数
	private float multiple = 1;
	// 旋转，放大动画
	private AnimationSet animationSet;
	private ScaleAnimation scaleAnimation;
	private RotateAnimation rotateAnimation;

	ToolRotation m_tool;

	private boolean isMirror = false;

	public ViewEditRotate(Context context, AttributeSet attrs) {
		super(context, attrs);

		m_tool = new ToolRotation();
		m_tool.init(MyData.getJNI());
	}

	// 初始化参数
	public boolean setPic() {
		try {
			if (bmpBack != null && !bmpBack.isRecycled()) {
				bmpBack.recycle();
				bmpBack = null;
			}
			// 获得展示的图片
			bmpBack = m_tool.getShowOralImage();
			bmpBack = BitmapOperate.FittingWindow(bmpBack, nViewWidth, nViewHeight, true);// 在View里进行了偏移

			srcRect = new RectF(0, 0, bmpBack.getWidth(), bmpBack.getHeight());
			dstRect = new RectF();
			matrix = new Matrix();
			matrixRS = new Matrix();

			paint = new Paint();
			paint.setAntiAlias(true);
			paint.setFilterBitmap(true);

			matrix.postTranslate(getMidX() - bmpBack.getWidth() / 2, getMidY() - bmpBack.getHeight() / 2);
			matrix.mapRect(dstRect, srcRect);

			srcHeight = srcRect.height();
			srcWidth = srcRect.width();

			m_nPicWidth = bmpBack.getWidth();
			m_nPicHeight = bmpBack.getHeight();

//			rect = new Rect(getWidth() / 2 - bmpBack.getWidth() / 2, getHeight() / 2 - bmpBack.getHeight() / 2,
//					getWidth() / 2 + bmpBack.getWidth() / 2, getHeight() / 2 + bmpBack.getHeight() / 2);
            //add by biao.luo for pr484973 begin
            rect = new Rect(0, 0,this.getWidth(), this.getHeight());
            //add by biao.luo end
		} catch (Exception e) {
			// TODO: handle exception
		}
		return true;
	}

	// 重设按钮
	public void resetVal() {
		m_fAngle = 0;
		mRealAnglePos = 0;
		m_nMirror = 0;
	}

	// 标记水平，垂直翻转
	public void setMirror(int val) {
		mRealAnglePos = 360 - mRealAnglePos;
		m_fAngle = -1 * m_fAngle;
		if (m_nMirror == val) {
			m_nMirror = m_nMirror - val;
		} else if (m_nMirror == 3) {
			m_nMirror = m_nMirror - val;
		} else
			m_nMirror = m_nMirror + val;
	}

	// 旋转产生的附加角度
	public void setRealAngle(float degree) {
		mRealAnglePos += degree;
		if (mRealAnglePos >= 360) {
			mRealAnglePos -= 360;
		}
		if (mRealAnglePos < 0) {
			mRealAnglePos += 360;
		}
	}

	// 获取自由旋转放大倍数
	public float getMultiple() {
		float m1, m2;
		m1 = (float) nViewHeight / (float) m_nPicWidth;
		m2 = (float) nViewWidth / (float) m_nPicHeight;
		if (m1 > m2)
			return m2;
		else
			return m1;
	}

	// 获取X轴中点
	public float getMidX() {
		return this.getWidth() / 2;
	}

	// 获取Y轴中点
	public float getMidY() {
		return this.getHeight() / 2;
	}

	// 旋转
	public void rotate(float oldD, float newD) {
		matrix.postRotate(oldD - newD, getMidX(), getMidY());

		// fsl add begin 解决魅族底层的矩阵的bug
		float pValue[] = new float[9];
		matrix.getValues(pValue);
		float tempValue = pValue[0] * pValue[1] * pValue[3] * pValue[4];
		if (tempValue == 0.0f) {
			matrix.postRotate(0.05f);
		}
		// fsl add end

		matrix.mapRect(dstRect, srcRect);
		if (m_nPicWidth > m_nPicHeight) {
			multiple = dstRect.height() / srcHeight;
			scale(multiple, multiple);
			srcHeight = dstRect.height();
		} else {
			multiple = dstRect.width() / srcWidth;
			scale(multiple, multiple);
			srcWidth = dstRect.width();
		}
		m_fAngle += (oldD - newD);
	}
	
	public void rotate(float dis) {
        matrix.postRotate(dis, getMidX(), getMidY());

        float pValue[] = new float[9];
        matrix.getValues(pValue);
        float tempValue = pValue[0] * pValue[1] * pValue[3] * pValue[4];
        if (tempValue == 0.0f) {
            matrix.postRotate(0.05f);
        }

        matrix.mapRect(dstRect, srcRect);
        if (m_nPicWidth > m_nPicHeight) {
            multiple = dstRect.height() / srcHeight;
            scale(multiple, multiple);
            srcHeight = dstRect.height();
        } else {
            multiple = dstRect.width() / srcWidth;
            scale(multiple, multiple);
            srcWidth = dstRect.width();
        }
        m_fAngle += dis;
    }

	// 水平旋转，垂直旋转，放大
	public void scale(float x, float y) {
		matrix.postScale(x, y, getMidX(), getMidY());
		matrixRS.postScale(x, y);
		
		// 2012/12/27 Add.
		isMirror = !isMirror;
		
		// fsl add begin 解决魅族底层的矩阵的bug
		float pValue[] = new float[9];
		matrix.getValues(pValue);
		float tempValue = pValue[0] * pValue[1] * pValue[3] * pValue[4];
		if (tempValue == 0.0f) {
			matrix.postRotate(0.05f);
		}
		// fsl add end

		matrix.mapRect(dstRect, srcRect);
		invalidate();
	}
	
    //add by biao.luo for pr484973 begin
    public void scale(float x, float y ,float degree ) {
//        matrix.postScale(x, y, getMidX(), getMidY());
        if(degree==-90 || degree==-270 || degree==90 || degree==270){
            matrix.postScale(-x, -y, getMidX(), getMidY());
        }else{
            matrix.postScale(x, y, getMidX(), getMidY());
        }
        matrixRS.postScale(x, y);
        isMirror = !isMirror;
        
        float pValue[] = new float[9];
        matrix.getValues(pValue);
        float tempValue = pValue[0] * pValue[1] * pValue[3] * pValue[4];
        if (tempValue == 0.0f) {
            matrix.postRotate(0.05f);
        }

        matrix.mapRect(dstRect, srcRect);
        invalidate();
    }
    //add by biao.luo end

	// 左右旋转，放大动画
	public void rotateWithAni(float from, float to, float pivotX, float pivotY, float fromM, float toM,
			boolean fillAfter) {
		animationSet = new AnimationSet(true);
		scaleAnimation = new ScaleAnimation(fromM, toM, fromM, toM, ScaleAnimation.RELATIVE_TO_SELF, 0.5f,
				ScaleAnimation.RELATIVE_TO_SELF, 0.5f);

		if (from % 90 == 0.0f) {
			from += 0.05;
		}
		if (to % 90 == 0.0f) {
			to += 0.05;
		}

        //add by biao.luo for pr484973 begin
        matrixRS.postRotate(to - from);
        matrix.postScale(toM/fromM, toM/fromM, pivotX, pivotY);
        matrix.postRotate(to - from, pivotX, pivotY);
        // add by biao.luo end
		// If mirror times is singular,we should make contrast rotation.
		if (isMirror) {
			float[] values = new float[9];
			matrixRS.getValues(values);
			values[0] *= -1;
			values[1] *= -1;
			values[3] *= -1;
			values[4] *= -1;
			matrixRS.setValues(values);
		}

		rotateAnimation = new RotateAnimation(from, to, pivotX, pivotY);
		animationSet.addAnimation(scaleAnimation);
		animationSet.addAnimation(rotateAnimation);
		animationSet.setDuration(300);
		animationSet.setFillAfter(fillAfter);
		animationSet.setStartOffset(0);
        //add by biao.luo for pr484973 begin
//      startAnimation(animationSet);
        invalidate();
        //add by biao.luo end
	}

	// 绘制图形
	@Override
	protected void onDraw(Canvas canvas) {
		// TODO Auto-generated method stub
		super.onDraw(canvas);
		if (nViewWidth == 0) {
			nViewWidth = this.getWidth();
			nViewHeight = this.getHeight();
			setPic();
		}
		canvas.clipRect(rect);
		if (bmpBack != null && !bmpBack.isRecycled()) {
			canvas.drawBitmap(bmpBack, matrix, paint);
		}
	}

	// 获取处理后的图像
	public boolean savePic() {
		float[] value = new float[9];
		matrixRS.getValues(value);

		for (int i = 0; i < 9; i++) {
			Log.d("fsl", "rs = " + value[i]);
		}
		// 确定
		m_tool.procImage(value, 9, 0);
		m_tool.ok();
		MyData.getBeautyControl().pushImage();

		return true;
	}
	
	public boolean savePic(float index) {
        float[] value = new float[9];
        matrixRS.getValues(value);

        for (int i = 0; i < 9; i++) {
            Log.d("fsl", "rs = " + value[i]);
        }
        m_tool.procImage(value, 9, index);
        m_tool.ok();
        MyData.getBeautyControl().pushImage();

        return true;
    }

	public int[] bitmap2IntARGB(Bitmap bmp) {
		int pix[] = null;
		try {
			int w = bmp.getWidth();
			int h = bmp.getHeight();
			pix = new int[w * h];
			bmp.getPixels(pix, 0, w, 0, 0, w, h);
		} catch (Exception e) {

		}
		return pix;
	}

	/**
	 * 判断是否需要保存
	 * 
	 * @return
	 */
	public boolean isNeedSave() {
		if (m_fAngle == 0 && m_nMirror == 0 && mRealAnglePos == 0) {
			return false;// 没有改变
		}
		return true;
	}

	public boolean Release() {
		try {
			if (bmpBack != null && !bmpBack.isRecycled()) {
				bmpBack.recycle();
				bmpBack = null;
			}
			m_tool.cancel();
		} catch (Exception e) {
		}
		return true;
	}

	public void reset() {
		m_fAngle = 0;
		mRealAnglePos = 0;
		m_nMirror = 0;
		setPic();
		invalidate();
	}

}
