package com.jrdcom.mt.core;

import android.graphics.Bitmap;
import com.mt.mtxx.image.JNI;
/**
 * 文字功能
 * @author aidy
 *
 */
public class ToolText extends ToolBase
{
//	/**
//	 * 文字操作，传递的是ARGB数据
//	 * @param data          文字的图片数据ARGB
//	 * @param width         文字的图片的宽
//	 * @param height        文字的图片的高
//	 * @param x              文字在图片真实的左上角区域，百分比
//	 * @param y              文字在图片真实的左上角区域，百分比
//	 * @param scale          文字图片的放缩尺寸
//	 */
//	public void procImage(int data[],int width,int height,float x,float y,float scale)
//	{
//		m_jni.ToolText(data, width, height, x, y, scale);
//		m_isProcessed = true;
//	}

    /**
     * 文字操作，传递的是Bitmap对象
     * @param pImage    文字的图片Bitmap对象
     * @param x              文字在图片真实的左上角区域，百分比
     * @param y              文字在图片真实的左上角区域，百分比
     * @param scale          文字图片的放缩尺寸
     */
    public void procImage(Bitmap pImage,float x,float y,float scale)
    {
        m_jni.ToolTextWithImage(pImage, x, y, scale);
        m_isProcessed = true;
    }
	/**
	 * 将当前的UI显示图置为原图
	 */
	public void procImageWithResetToOral()
	{
		m_isProcessed = false;
		m_jni.ToolResetImageToOral(false);
	}

//    /**
//     * 清理图片数据
//     */
//	public void clearWordData()
//	{
//		m_isProcessed=false;
//		m_jni.clearProcImageData();
//	}
}
