package com.jrdcom.mt.core;


import android.graphics.Bitmap;

/**
 * 马赛克功能
 * @author aidy
 *
 */
public class ToolMosaic extends ToolBase
{
    /**
     * 马赛克处理函数(对保存图的操作)
     * @param nMosaicBlock		马赛克的半径，如果为0则不作马赛克处理
     */
    public void procMosaic(int nMosaicBlock)
    {
        m_jni.ToolMosaic(nMosaicBlock);
    }

    /**
     * 有添加纹理的马赛克处理函数(对保存图的操作)
     * @param nMosaicBlock		马赛克的半径，如果为0则不作马赛克处理
     * @param pTexture			纹理图片
     */
    public void procMosaicWithTexture(int nMosaicBlock,Bitmap pTexture)
    {
        m_jni.ToolMosaicWithTexture(nMosaicBlock, pTexture);
    }

    /**
     * 马赛克功能的确定功能（最终的合成函数）
     * @param pMask	需要合成的mask图片
     */
    public void ok(Bitmap pMask)
    {
        m_isProcessed = true;
        m_jni.ToolMosaicMixing(pMask);
        super.ok();
    }
}
