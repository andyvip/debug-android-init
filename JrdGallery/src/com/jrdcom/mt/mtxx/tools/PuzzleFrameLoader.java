package com.jrdcom.mt.mtxx.tools;

import java.util.ArrayList;

import com.jrdcom.example.layout.TemplateTool;
import com.jrdcom.mt.core.BitmapUtil;
import com.mt.mtxx.image.JNI;

import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.util.Log;

/**边框图片加载类，主要负责利用NDK加载边框与底纹文件，生成边框图片
 * @author Helios
 */
public class PuzzleFrameLoader {
	
	/**
	 *边框与底纹文件路径，目前NDK底层只支持从SD卡中加载 
	 */
	private String mBianKuangPath="";
	private String mDiwenPath="";
	
	/**
	 * 存放UI层用于展示的边框图片的列表(0,1,2……),这些图片由NDK层生成
	1111111111111111111111
	3000000000000000000004
	3000000000000000000004
	5000000000000000000006
	5000000000000000000006
	.00000000000000000000.
	.00000000000000000000.
	2222222222222222222222
	*/
	private ArrayList<Bitmap> mVertureList=new ArrayList<Bitmap>();
	
	/**
	 * 最终保存的图片大小 ，由加载完边框以后NDK确定,并返回(实际上，界面基本用不上,NDK有保存)
	 */
	private int mResultW;
	private int mResultH;
	
	/**
	 * 拼图区域的大小
	 */
	private int mWidth;
	private int mHeight;
	
	/**界面展示时，拼图区域的尺寸**/
	private int mShowWidth=480;
	private int mShowHeight=0;
	
	private JNI m_jni=null;
	
	public PuzzleFrameLoader(JNI jni)
	{
		this.m_jni=jni;
	}
	
	/**初始化方法
	 * @param strBiankuang 边框文件路径
	 * @param strDiwen   底纹文件路径
	 * @param nWidth   拼图区域大小
	 * @param nHeight
	 */
	public void initFrame(String strBiankuang,String strDiwen,int nWidth,int nHeight)
	{
		this.mWidth=nWidth;
		this.mHeight=nHeight;
		
		this.mBianKuangPath=strBiankuang;
		this.mDiwenPath=strDiwen;
		loadFrame();
	}
	
	
	private void loadFrame()
	{
		/**初始化边框与底纹**/
		int resultSize[]=m_jni.PuzzleFrameInit(mBianKuangPath, mDiwenPath, mWidth, mHeight);
		mResultW=resultSize[0];
		mResultH=resultSize[1];
	}
	
	/**重设拼图区域尺寸
	 * @param nWidth
	 * @param nHeight
	 */
	public void resetPuzzleSize(int nWidth,int nHeight)
	{
		this.mWidth=nWidth;
		this.mHeight=nHeight;
		loadFrame();
	}
	
	/**重新加载边框与底纹
	 * @param strBiankuang
	 * @param strDiwen
	 */
	public void resetFrame(String strBiankuang,String strDiwen)
	{
		this.mBianKuangPath=strBiankuang;
		this.mDiwenPath=strDiwen;
		loadFrame();
	}
	
	/**根据界面展示区域的大小获取适当大小的边框文件
	 * @param nShowWidth
	 * @param nShowHeight
	 * @return
	 */
	public ArrayList<Bitmap > getFrameBmp(int nShowWidth,int nShowHeight)
	{
		/**设置UI层拼图区域的展示尺寸，以便NDK层生成用来展示的边框图片**/
		int vals[]=m_jni.PuzzleResetShowSize(nShowWidth, nShowHeight, 0);
		
		/**获取五个区域的图片数据*/
		int m_nPuzzleFrameCount = m_jni.PuzzleGetShowCount();
		ReleaseFrameVector();
		if (m_nPuzzleFrameCount >= 5) {// 至少需要5个图片数据
			for (int index = 0; index < m_nPuzzleFrameCount; index++) {
			    int val[] = new int[2];
//				int[] valsize = m_jni.PuzzleGetFrameShowSizeWithIndex(index,val);
				int[] data = m_jni.PuzzleGetFrameShowDataWithIndex(index, val);
				Bitmap pImage = BitmapUtil.intARGB2Bitmap(data, val[0], val[1]);
                if (m_nPuzzleFrameCount == 5) {
                    TemplateTool.Item_Width[index] = (int) val[0];
                    TemplateTool.Item_Height[index] = (int) val[1];
                }
//				Bitmap pImage = Bitmap.createBitmap(valsize[0], valsize[1], Config.ARGB_8888);
//				m_jni.PuzzleGetFrameShowImageWithIndex(index, pImage);
				mVertureList.add(pImage);		
			}
		}
		/**清理边框内存**/
		m_jni.PuzzleClearShowFrames();	
		return mVertureList;
	}
	
	 private void ReleaseFrameVector() {
	        for (int i = mVertureList.size() - 1; i > -1; i--) {
	            Bitmap tmpTextureStruct = mVertureList.get(i);

	            if (tmpTextureStruct != null && !tmpTextureStruct.isRecycled()) {
	                tmpTextureStruct.recycle();
	                tmpTextureStruct = null;
	            }
	            mVertureList.remove(i);
	        }
	        mVertureList.clear();

	        System.gc();
	    }
}
