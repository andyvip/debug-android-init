package com.jrdcom.mt.core;

import com.mt.mtxx.image.JNI;


/**
 * 虚化功能
 * @author aidy
 *
 */
public class ToolBlur extends ToolBase
{
    /**
     * 初始化
     * @param jni   NDK与JAVA的链接工具
     */
	public void init(JNI jni)
	{
		super.init(jni);
		m_jni.MiddleWeakInit();
	}

    /**
     * 圆形虚化的处理函数
     * @param x              圆形的中心点的x坐标
     * @param y              圆形的中心点的y坐标
     * @param nInRadius     圆形的内圆半径
     * @param nOutRadius    圆形的外圆半径
     */
	public void procRadiusDealPic(int x, int y,int nInRadius,int nOutRadius)
	{
		m_isProcessed = true;
		setWeakType(0);
		setWeakRadius(nInRadius, nOutRadius);
		m_jni.MiddleWeakDealRadiusPic(x, y);
	}

    /**
     * 直线虚化的处理函数
     * @param x              直线的中心点的x坐标
     * @param y              直线的中心点的y坐标
     * @param angle          直线的旋转角度，范围0-360
     * @param nInRadius     直线的内圆半径
     * @param nOutRadius    直线的外圆半径
     */
	public void procLineDealPic(int x,int y,float angle,int nInRadius,int nOutRadius)
	{
		//角度转换,最终转换为[0,180)
		while (angle<0)
		{
			angle += 360;
		}
		while (angle>=360)
		{
			angle -= 360;
		}
		
		if (angle>=180)
		{
			angle = angle - 180;
		}
		
		//校验90度的情况
		angle = checkAngle(angle,90);
		
		angle = (float)(Math.PI*angle/180.0);
		
		
		m_isProcessed = true;
		int arrX[] = new int[8];
		int arrY[] = new int[8];
		setWeakType(1);
		setWeakRadius(nInRadius, nOutRadius);
		getLinePoint(arrX, arrY, x, y, angle, nInRadius, nOutRadius);

		m_jni.MiddleWeakDealLinePic(arrX, arrY, angle);
	}

    /**
     * 点击确定按钮
     */
	public void ok()
	{
		m_jni.MiddleWeakOK();
		//真实图的操作
		super.ok();
	}

    /**
     * 删除临时数据与内存
     */
	public void clear()
	{
		m_jni.MiddleWeakRelease();
		super.clear();
	}

	/**
	 * 以下为私有函数，外部接口不用调用
	 */

    /**
     * 设置虚化的类型
     * @param nType     0为圆形虚化，1为直线虚化
     */
	public void setWeakType(int nType)
	{
		m_jni.MiddleWeakSetType(nType);
	}

    /**
     * 设置外圈和内圈的半径
     * @param nInRadius     内圈的半径
     * @param nOutRadius    外圈的半径
     */
	public void setWeakRadius(int nInRadius,int nOutRadius)
	{
		m_jni.MiddleWeakSetRadius(nInRadius, nOutRadius);
	}
	// 获取8个边界点
	// 0 1
	// 2 3
	// 4 5
	// 6 7
	// angle 0~pi
	public boolean getLinePoint(int arrX[], int arrY[], int x, int y, float angle, int InRadius,
					int OutRadius) 
	{
		int val[] = new int[2];
		val = getShowImageSize();
		int width = val[0];
		int height= val[1];
		try {
			int i;
			int mpX[] = new int[4];// 点到线的垂直交点
			int mpY[] = new int[4];// 点到线的垂直交点

			mpX[0] = (int) (x - OutRadius * Math.sin(angle));
			mpY[0] = (int) (y - OutRadius * Math.cos(angle));
			mpX[1] = (int) (x - InRadius * Math.sin(angle));
			mpY[1] = (int) (y - InRadius * Math.cos(angle));
			mpX[2] = (int) (x + InRadius * Math.sin(angle));
			mpY[2] = (int) (y + InRadius * Math.cos(angle));
			mpX[3] = (int) (x + OutRadius * Math.sin(angle));
			mpY[3] = (int) (y + OutRadius * Math.cos(angle));

			while (angle < 0) {
				angle += Math.PI;
			}
			while (angle > Math.PI) {
				angle -= Math.PI;
			}
			if (angle <= Math.PI / 2 ) 
			{
				double tanA = Math.tan(angle);
				
				for (i = 0; i < 4; i++) {
					// 左边的点
					if (mpX[i] * tanA <= height - mpY[i]) {
						arrX[i * 2] = 0;
						arrY[i * 2] = (int) (mpX[i] * tanA + mpY[i]);
					} else {
						arrX[i * 2] = (int) (mpX[i] - (height - mpY[i]) / tanA);
						arrY[i * 2] = height;
					}
					// 右边的点
					if ((width - mpX[i]) * tanA <= mpY[i]) {
						arrX[i * 2 + 1] = width;
						arrY[i * 2 + 1] = (int) (mpY[i] - (width - mpX[i]) * tanA);
					} else {
						arrX[i * 2 + 1] = (int) (mpX[i] + mpY[i] / tanA);
						arrY[i * 2 + 1] = 0;
					}
				}
			}
			else if (angle < Math.PI) 
			{
				double tanA = -1 * Math.tan(angle);
				
				for (i = 0; i < 4; i++) 
				{
					// 左边的点
					if (mpX[i] * tanA <= mpY[i]) 
					{
						arrX[6 - i * 2] = 0;
						arrY[6 - i * 2] = (int) (mpY[i] - mpX[i] * tanA);
					} 
					else 
					{
						arrX[6 - i * 2] = (int) (mpX[i] - mpY[i] / tanA);
						arrY[6 - i * 2] = 0;
					}
					// 右边的点
					if ((width - mpX[i]) * tanA <= height - mpY[i]) 
					{
						arrX[8 - i * 2 - 1] = width;
						arrY[8 - i * 2 - 1] = (int) (mpY[i] + (width - mpX[i]) * tanA);
					} else {
						arrX[8 - i * 2 - 1] = (int) (mpX[i] + (height - mpY[i]) / tanA);
						arrY[8 - i * 2 - 1] = height;
					}
				}
			}
		} catch (Exception e) {

		}
		return true;
	}

    /**
     * 检验角度
     * @param angle         校验前的角度
     * @param nCheckAngle   校验的对象
     * @return      返回校验后的角度
     */
	public float checkAngle(float angle,int nCheckAngle)
	{
		if (angle>nCheckAngle-1 && angle<=nCheckAngle)
		{
			angle = nCheckAngle-1;
		}
		else if(angle > nCheckAngle && angle<nCheckAngle+1)
		{
			angle = nCheckAngle+1;
		}

		return angle;
	}
}
