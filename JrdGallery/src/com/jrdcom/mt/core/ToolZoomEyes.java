package com.jrdcom.mt.core;

import com.mt.mtxx.image.JNI;

/**
 * 眼睛放大
 * @author aidy
 *
 */
public class ToolZoomEyes extends ToolBase 
{
	/*
	 * 相关的功能处理，总共四个参数，第一个参数为点x/图片的宽。第二个坐标为点y/图片的高
	 * 第三个参数为半径（范围暂时未15-60，初始为25）/图片的宽，
	 * 第四个参数为力度（眼睛放大为0到100，暂时默认为50，眼睛缩小为-100到0）
	 */
	public void procImage(float[] pValues,int length)
	{
		m_isProcessed = true;
		m_jni.ToolZoomEyes(pValues, length);
	}
}
