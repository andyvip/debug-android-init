package com.jrdcom.mt.core;

import android.graphics.Bitmap;
import com.jrdcom.mt.util.MyData;
import com.mt.mtxx.image.JNI;

/**
 * 美化——边框功能
 * 
 * @author aidy
 * 
 */
public class ToolFrame extends ToolBase {
	private int m_nKind;// 操作类型，0为简单边框，1为炫彩边框
	private String m_strPath;       //边框的绝对路径
	private boolean m_bIsSDCard;   //true表示是SD卡上的素材，false表示是assets里的素材

	private int m_nChannelType;     //炫彩边框的混合模式

    /**
     * 初始化
     * @param jni   NDK与JAVA的链接工具
     */
	public void init(JNI jni) {
		super.init(jni);
		m_nChannelType = 0;
		m_nKind = -1;
	}

	/**
	 * 处理简单边框素材应用到当前图片上
	 * @param strPath       素材文件的路径
	 * @param bIsSDCard     true 来自SDcard, false 来自 Assets
	 * @param bIsPreview    true 对预览图进行操作 false 对真实图操作。
	 */
	public void procImageWithSimleFrame(String strPath, boolean bIsSDCard, boolean bIsPreview) {
		m_isProcessed = true;
		m_nKind = 0;
		m_strPath = strPath;
		m_bIsSDCard = bIsSDCard;

		m_jni.ToolSimpleFrame(strPath, bIsSDCard, bIsPreview);
		
	}

	/**
	 * 处理炫彩边框素材的函数
	 * 
	 * @param strPath           素材的存放路劲
	 * @param nChannelType     此参数由素材内部
	 * @param bIsSDCard        true 来自SDcard, false 来自 Assets
	 * @param bIsPreview       true 对预览图进行操作 false 对真实图操作。
	 */
	public void procImageWithColorFrame(String strPath, int nChannelType, boolean bIsSDCard, boolean bIsPreview) {
		m_isProcessed = true;
		m_nKind = 1;
		m_strPath = strPath;
		m_bIsSDCard = bIsSDCard;
		m_nChannelType = nChannelType;

		m_jni.ToolColorFrame(strPath, nChannelType, bIsSDCard, bIsPreview);

	}
	
	/**
	 * 将UI显示图片置为原图
	 */
	public void procImageWithResetToOral()
	{
		m_isProcessed = false;
		m_jni.ToolResetImageToOral(true);
	}

    /**
     * 点击确定按钮
     */
	public void ok() 
	{
		if (m_isProcessed == true)
		{
			switch (m_nKind) {
			case 0:// 简单边框
			{
				procImageWithSimleFrame(m_strPath, m_bIsSDCard, false);
			}
				break;
			case 1:// 炫彩边框
			{
				procImageWithColorFrame(m_strPath, m_nChannelType, m_bIsSDCard, false);
			}
				break;
			}

			// 真实图的操作
			super.ok();	
		}
	}

    /**
     * 缩略图做简单边框处理（不作用于原图）
     * @param pImage        缩略图
     * @param strPath       素材文件的路径
     * @param bIsSDCard     true 来自SDcard, false 来自 Assets
     * @return              缩略图经过简单边框处理的结果
     */
    public static Bitmap ThumbnailWithSimleFrame(Bitmap pImage,String strPath, boolean bIsSDCard)
    {
        return MyData.getJNI().ToolSimpleFrameWithThumbnailFromBitmap(pImage,strPath,bIsSDCard);
    }

    /**
     * 缩略图做炫彩边框处理(作用于原图)
     * @param pImage        缩略图
     * @param strPath           素材的存放路劲
     * @param nChannelType     此参数由素材内部
     * @param bIsSDCard        true 来自SDcard, false 来自 Assets
     * @return  返回1表示操作成功，返回0表示操作失败
     */
    public static int ThumbnailWithColorFrame(Bitmap pImage,String strPath, int nChannelType, boolean bIsSDCard)
    {
        return MyData.getJNI().ToolColorFrameWithThumbnailFromBitmap(pImage,strPath,nChannelType,bIsSDCard);
    }


}
