package com.jrdcom.mt.core;

import com.mt.mtxx.image.JNI;



/**
 * 旋转
 * @author aidy
 *
 */
public class ToolRotation extends ToolBase
{
    /**
     * 处理函数
     * @param pValuesRS         正常旋转(向左右旋转，上下翻转，左右翻转)的矩阵转换后的数组
     * @param nValuesLength     pValuesRS的个数，固定为9
     * @param fFreeValue        自由旋转的角度，范围0-360
     */
	public void procImage(float pValuesRS[],int nValuesLength,float fFreeValue)
	{
		m_jni.ToolRotationNew(pValuesRS, nValuesLength, fFreeValue);
//		m_jni.ToolRotation(pValues, pValues.length);//将真实图与显示图都操作了
		m_isProcessed = true;
	}
}
