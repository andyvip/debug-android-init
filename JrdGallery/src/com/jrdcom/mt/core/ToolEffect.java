package com.jrdcom.mt.core;

import java.io.File;

import android.graphics.Bitmap;
import com.jrdcom.mt.util.MyData;
import com.mt.mtxx.image.JNI;

/**
 * 特效功能
 * @author aidy
 *
 */
public class ToolEffect extends ToolBase
{
	//临时文件缓存地方，主要是为了提高处理速度，处理过一次后保存为临时文件，则下次调用同一个的时候直接读取临时文件
	private ImageDiskCache m_imageDiskCache = null;
	//处理的特效编号
	int m_nEffectIndex;

    private float m_fEffectAlpha;//特效的透明度
    /**
     * 初始化
     * @param jni   NDK与JAVA的链接工具
     */
	public void init(JNI jni)
	{
		super.init(jni);
		//临时文件控制类初始化并创建临时文件夹
		m_imageDiskCache = new ImageDiskCache();
		m_imageDiskCache.createCacheFolder("ToolEffect");	
		m_nEffectIndex = 0;
        m_fEffectAlpha = 1.0f;
	}

	/**
	 * 处理特效图片
	 * @param nIndex 指定特效对象的索引值，具体见本文件尾说明
	 * @param bIsPreview true 针对预览图 false 针对原图
	 */
	public void procImage(int nIndex,boolean bIsPreview)
	{
		if (nIndex == 0)
		{
			m_isProcessed = false;
		}
		else
		{
			m_isProcessed = true;
		}
		//显示图片操作
		if (bIsPreview == true)
		{
			m_nEffectIndex = nIndex;
			String strTempFile = m_imageDiskCache.m_strSavePath + "/effect"+nIndex;
			File file = new File(strTempFile);
			if (!file.exists()) 
			{
				m_jni.ToolEffect(nIndex,bIsPreview);
				//保存缓存文件
				m_jni.saveImageDataToDisk(strTempFile, 2);
			}
			else
			{
				//加载缓存文件
				m_jni.loadImageDataFromDisk(strTempFile,2);
			}		
		}
		else
		{
			m_jni.ToolEffect(nIndex,bIsPreview);
		}
		
	}
    /**
     * 点击确定的时候，传递透明度，做真实图片操作
     *
     * @param fAlpha
     *            该特效的透明度
     */
    public void setEffectAlpha(float fAlpha) {
        m_fEffectAlpha = fAlpha;
    }
    /**
     * 点击确定按钮
     */
	public void ok()
	{
        if (m_fEffectAlpha == 1.0f) {
            m_jni.ToolEffect(m_nEffectIndex, false);
            
        } else {
            m_jni.ToolEffectWithAlpha(m_nEffectIndex, m_fEffectAlpha, false);
        }
		//真实图的操作
		super.ok();
	}

    /**
     * 删除临时数据与内存
     */
	public void clear()
	{
		if ( m_imageDiskCache != null)
		{
			m_imageDiskCache.clear();
			m_imageDiskCache = null;
		}
		m_nEffectIndex = 0;
		super.clear();
	}

    /**
     * 特效缩略图处理
     * @param pImage    特效缩略图，效果作用于本身
     * @param nIndex    特效的编号
     * @return  返回1表示操作成功，返回0表示操作失败
     */
	public static int ThumbnailWithEffectProc(Bitmap pImage,int nIndex)
    {
        return MyData.getJNI().ToolEffectWithThumbnailFromBitmap(pImage, nIndex);
    }
	/**
	 * 特效的编号，0原图，1经典lomo，2淡雅，3胶片，4复古，5亮红，6日系，7阿宝色，8印象，9旧时光，10古典，11HDR
	 * 12老照片，13古铜色，14经典HDR,15牛皮纸，16炫彩lomo，17哥特风，18流金岁月，19平安夜，20星芒，21飞雪，22夜景
	 * 23七彩光晕，24暖洋洋，25反转色，26粉红佳人，28黑白，29柔光，30日光，31时光隧道，32移轴，33写生素描，34古典素描
	 * 35彩铅，36油画
	 * */
}
