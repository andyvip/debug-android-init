package com.jrdcom.mt.core;

import com.mt.mtxx.image.JNI;

/**
 * 瘦脸瘦身算法
 * @author aidy
 *
 */
public class ToolSlimFaceAndBody extends ToolBase 
{
	/*
	 * 相关的功能处理，总共六个参数。第一个参数为起始点的坐标x/图片的宽，第二个参数为起始点的坐标y/图片的高
	 * 第三个参数为终点的坐标x/图片的宽，第四个参数为终点的坐标y/图片的高
	 * 第五个参数为笔的半径（暂时为15-100，初始为50）/图片的宽，第六个参数为力度（范围为0-100，默认为50）
	 */
	public void procImage(float[] pValues,int length)
	{
		//操作
		float rat = 1.0f;
		float xoffset = (pValues[2] - pValues[0]);
		float yoffset = (pValues[3] - pValues[1]);

        
		float xoffsetabs = (xoffset>0?xoffset:-xoffset);
		float yoffsetabs = (yoffset>0?yoffset:-yoffset);
        float offset = xoffsetabs > yoffsetabs ? xoffsetabs : yoffsetabs;
        
        float nMaxOffset = 0.1f;

        if (offset>nMaxOffset) 
        {
            rat = nMaxOffset /offset;
        }
        
		pValues[2] = pValues[0] + (pValues[2] - pValues[0])*rat;
		pValues[3] = pValues[1] + (pValues[3] - pValues[1])*rat;

        
		m_isProcessed = true;
		m_jni.ToolSlimFaceAndBody(pValues, length);
	}
}
