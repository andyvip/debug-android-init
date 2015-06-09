package com.jrdcom.example.joinpic;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import com.jrdcom.example.layout.PosterLayout;
import com.jrdcom.example.layout.PosterLayoutItem;
import com.jrdcom.mt.util.AssetInputStreamOpener;
import com.jrdcom.mt.util.IInputStreamOpener;
import com.jrdcom.mt.util.MyData;
import com.jrdcom.mt.core.BitmapUtil;
import com.jrdcom.mt.mtxx.tools.PosterPicLoader;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.Path;
import android.graphics.Rect;
import android.util.Log;

public class PosterPuzzleModel extends PuzzleModel {
	
	
	/**海报版式对象**/
	private PosterLayout mPosterLayout;
	
	/**海报背景与顶图加载对象*/
	private PosterPicLoader mPosterPicLoader;
	/**
	 *图片保存大小
	 */
	private int mWidth=640;
	private int mHeight=896;	
	
	public PosterPuzzleModel()
	{
		mPosterPicLoader=new PosterPicLoader();
	}
	/**
	 * 设置模版类型拼图当前使用板式文件
	 *
	 * @param pContext        当前的操作的Context对象。
	 * @param pLayoutFilePath 需要加载的版式文件路径。
	 * @param pIsFromSDCard   标记当前加载的版式文件是否来自程序外部
	 */
	public void setPosterLayout(Context pContext, final String pLayoutFilePath, boolean pIsFromSDCard) {
		if (!pIsFromSDCard) {
			this.setPosterPuzzleLayout(new AssetInputStreamOpener(pContext.getAssets(), pLayoutFilePath));
		} else {
			this.setPosterPuzzleLayout(new IInputStreamOpener() {
				@Override
				public InputStream open() throws IOException {
					return new BufferedInputStream(new FileInputStream(new File(pLayoutFilePath)));
				}
			});
		}
	}
	
	/**
	 * 设置当前正在使用的海报拼图版式
	 * 
	 * @param pInputStreamOpener
	 *            打开版式流对象
	 */
	public void setPosterPuzzleLayout(final IInputStreamOpener pInputStreamOpener) {
		// end of delete.

		this.mPosterLayout = null;
		this.mPosterLayout = new PosterLayout(pInputStreamOpener);

		// 加载新的模版
		this.mPosterLayout.load();
		}
	
	/**海报当前正在使用的海报拼图版式
	 * @param pContext  程序上下文
	 * @param strPosterPath  海报素材文件路径
	 * @param isFromSDCard  是否来自SD卡
	 */
	public void setPosterStytle(Context pContext,final String strPosterPath,boolean isFromSDCard)
	{
		mPosterPicLoader.setPosterWidthAndHeight(mWidth, mHeight);
		if (!isFromSDCard) {
			this.mPosterPicLoader.reload(new AssetInputStreamOpener(pContext.getAssets(), strPosterPath));
		} else {
			this.mPosterPicLoader.reload(new IInputStreamOpener() {
				@Override
				public InputStream open() throws IOException {
					return new BufferedInputStream(new FileInputStream(new File(strPosterPath)));
				}
			});
		}

	}
	
	
	@Override
	protected boolean saveDataToPath(String pPath) {
		int lImageCount=this.mListImagePath.size();
		int[] lIds = new int[lImageCount];
		/** 设计稿里面的最大Rect对象 **/
		float[] lLayoutItems = new float[lImageCount << 2];
		int lPosition = 0; // 用于数组索引
		float lResizeScale = 1.0f;  // 保存着最终用于保存的图片上的坐标换算Scale。
		boolean lRes = true;

		m_jni.PuzzleStartWithTempFileSavePath(this.mPuzzleTmpFilePath, 4);
		for (int i = 0; i < lImageCount; i++) {
			Rect lMaxRect = new Rect();
			Bitmap lResBitmap =BitmapUtil.loadBitmapFromSDcard(this.mListImagePath.get(i), true);
			
			PosterLayoutItem lLayoutItem = this.mPosterLayout.getItem(i);
			// 图片的大小比版式设计稿还小，需要进行调整
			// 获取图片的原始大小。进行评估是否需要重新生成一个适合保存的图片，以免造成保存的图片质量太差的问题
			float lLayoutItemWidth = lLayoutItem.getWidth();
			float lLayoutItemHeight = lLayoutItem.getHeight();
			float xScale=lLayoutItemWidth/lResBitmap.getWidth();
			float yScale=lLayoutItemHeight/lResBitmap.getHeight();
			lResizeScale=xScale>yScale?xScale:yScale;

			lMaxRect.left = Math.round(lLayoutItem.getX());
			lMaxRect.top = Math.round(Math.abs(lLayoutItem.getY()));
			lMaxRect.right = Math.round(lLayoutItem.getX() + lLayoutItemWidth);
			lMaxRect.bottom = Math.round(lLayoutItem.getY() + lLayoutItemHeight);

			Path path = new Path(lLayoutItem.getPath());
			Matrix matrix = new Matrix();
			matrix.postScale(lResizeScale, lResizeScale);
			/** 获取每个节点里面path区域显示的图片 */
			Bitmap dstBitmap = BitmapUtil.clipBitmapByPath(lResBitmap, lMaxRect, path, matrix, false);
			
			dstBitmap = BitmapUtil.Change2ARGB8888(dstBitmap, true);
			m_jni.PuzzleInsertNodeImage(i, dstBitmap);
			
			dstBitmap.recycle();


			// 组装模版拼图版式ID数组
			lIds[i] = i;
			lLayoutItems[lPosition++] = lLayoutItem.getOrgRect().left;
			lLayoutItems[lPosition++] = lLayoutItem.getOrgRect().top;
			lLayoutItems[lPosition++] = lLayoutItem.getOrgRect().right;
			lLayoutItems[lPosition++] = lLayoutItem.getOrgRect().bottom;
		}

		if (lRes) {
			// 如果前面初始化底层数据正常，执行保存。
			lRes = m_jni.puzzleHBPTSaveToSD(pPath, mPosterLayout.getWidth(), mPosterLayout.getHeight(), lIds,
					lLayoutItems);
		}

		// 清除底层数据缓存
		m_jni.PuzzleClearMemory();

		return lRes;
	}

}
