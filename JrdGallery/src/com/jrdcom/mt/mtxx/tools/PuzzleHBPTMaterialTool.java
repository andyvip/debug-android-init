package com.jrdcom.mt.mtxx.tools;

import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.util.Log;
import com.mt.mtxx.image.JNI;

/**
 * @author Intern Helios 2013-3-13
 *
 */
public class PuzzleHBPTMaterialTool {
	private JNI m_jni;
	
	/**是否已加载文件数据**/
	private boolean mIsLoadSuccess=false;
	
	/**底图类型**/
	private final int TYPE_COLOR=0;
	private final int TYPE_TEXTURE=1;
	private final int TYPE_IMAGE=2;
	
	
	/**
	 *Constracter 
	 */
	public PuzzleHBPTMaterialTool()
	{
		m_jni=new JNI();
	}
	
	/**根据素材文件路径，解析素材文件信息
	 * @param materialPath 路径
	 * @param nResultWidth	结果图的宽
	 * @param nResultHeight	结果图的高
	 * @return 是否成功
	 */
	public boolean loadMaterialByPath(String materialPath,int nResultWidth,int nResultHeight)
	{
		mIsLoadSuccess=m_jni.puzzleHBPTloadByPath(materialPath);
		return mIsLoadSuccess;
	}
	/**
	 * 解析海报拼图的素材
	 * @param materialData	海报拼图素材的数据
	 * @param nResultWidth	结果图的宽
	 * @param nResultHeight	结果图的高
	 * @return
	 */
	public boolean loadMaterialByBytes(byte []materialData,int nResultWidth,int nResultHeight)
	{
//		Log.d("fsl", "nResultWidth"+nResultWidth+",nResultHeight="+nResultHeight);
		mIsLoadSuccess=m_jni.puzzleHBPTloadByBytes(materialData,nResultWidth,nResultHeight);
		return mIsLoadSuccess;
	}
	
	/**获取目标大小的背景Bitmap
	 * @param nDstWidth
	 * @param nDstHeight
	 * @return
	 */
	public Bitmap getBgWithSize(int nDstWidth, int nDstHeight) {
		Bitmap bitmap=null;
		int sizeAndType[] = new int[3];
		sizeAndType[0]=nDstWidth;
		sizeAndType[1]=nDstHeight;
		sizeAndType[2]=0;
		if (mIsLoadSuccess == true) {
			bitmap = Bitmap.createBitmap(nDstWidth, nDstHeight, Config.ARGB_8888);
			m_jni.puzzleHBPTgetBackgroundImage(sizeAndType, bitmap);
//			int data[];
//			data = m_jni.puzzleHBPTgetBackgroundData(sizeAndType);
//			Log.e("ReadFile", "DataType:"+sizeAndType[2]+"    Datalength:"+data.length);
//			if (data != null) {
//				// 生成目标Bitmap
//				bitmap = intARGB2Bitmap(data, nDstWidth, nDstHeight);
//				Log.e("ReadFile", "create Bitmap");
//				data = null;
//			}
		} else
			return null;
		return bitmap;
	}
	
	/**是否存在顶图
	 * @return
	 */
	public boolean isExistFg()
	{
		return m_jni.puzzleHBPTisExistForeGround();
	}
	
	/**获取目标大小的顶图Bitmap
	 * @param nDstWidth
	 * @param nDstHeight
	 * @return
	 */
	public Bitmap getFgWithSize(int nDstWidth,int nDstHeight)
	{		
		Log.d("fsl2", "nDstWidth="+nDstWidth+",nDstHeight="+nDstHeight);
		int size[]=new int[2];
		size[0]=nDstWidth;
		size[1]=nDstHeight;
		Bitmap bitmap=null;
		if (mIsLoadSuccess == true) {
			int data[];
			data=m_jni.puzzleHBPTgetForegroundData(size);
			//生成目标Bitmap
			bitmap=intARGB2Bitmap(data,nDstWidth,nDstHeight);
			data = null;
			
			//修改PNG的接口有问题，会出现颜色不对
//			bitmap = Bitmap.createBitmap(nDstWidth, nDstHeight, Config.ARGB_8888);
//			Canvas canvas = new Canvas(bitmap);
//			canvas.drawColor(Color.WHITE);
//			m_jni.puzzleHBPTgetForegroundImage(size, bitmap);
		}
		return bitmap;
		
	}	
	/**
	 * 将ARGB数据转换为Bitmap
	 * 
	 * @param data
	 * @param w
	 * @param h
	 * @return
	 */
	private Bitmap intARGB2Bitmap(int[] data, int w, int h) {
		try {
			if (data == null || data.length != w * h) {
				return null;
			}
			Bitmap bmp = Bitmap.createBitmap(w, h, Config.ARGB_8888);
			if (bmp == null) {
				return null;
			}
			bmp.setPixels(data, 0, w, 0, 0, w, h);
			return bmp;
		} catch (Exception e) {

		}
		return null;
	}


}
