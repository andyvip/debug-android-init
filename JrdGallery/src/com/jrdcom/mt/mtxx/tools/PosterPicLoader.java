package com.jrdcom.mt.mtxx.tools;

import android.graphics.Bitmap;

import com.jrdcom.mt.util.FileInputStreamOpener;
import com.jrdcom.mt.util.IInputStreamOpener;
import com.jrdcom.mt.util.StreamUtils;

import java.io.InputStream;

public class PosterPicLoader {

	private PuzzleHBPTMaterialTool picTool = null;// 读取图片类
	private int[] size = new int[2];// 读取版式后的宽高
	private Bitmap mOverBmp = null;// 顶部的图
	private Bitmap mFloorBmp = null;// 底部的图
	IInputStreamOpener mStreamOpener = null;
    private boolean mHasLoaded = false;

	public PosterPicLoader() {
		picTool = new PuzzleHBPTMaterialTool();
	}

	public boolean isLoaded() {
		return mHasLoaded;
	}
	
/*	*//**
	 * 改变海报拼图素材加载者数据的来源
	 *
	 * @param pInputStreamOpener 新数据来源的流对象。
	 *//*
	public void setStreamOpener(IInputStreamOpener pInputStreamOpener) {
		this.mStreamOpener = pInputStreamOpener;
		mHasLoaded = false;
	}*/

	/**
	 * 设置海报图片素材界面图片大小
	 *
	 * @param pWidth  宽度
	 * @param pHeight 高度
	 */
	public void setPosterWidthAndHeight(int pWidth, int pHeight) {
		this.size[0] = pWidth;
		this.size[1] = pHeight;
	}

	/**
	 * 获取顶部图片
	 *
	 * @return
	 */
	public Bitmap getOverPic() {
		return mOverBmp;
	}

	/**
	 * 获取底部图片
	 *
	 * @return
	 */
	public Bitmap getFloorPic() {
		return mFloorBmp;
	}

	// 读取图片
	private void loadMaterial() {
		 BitmapOperate.SafeRelease(mOverBmp);
		 BitmapOperate.SafeRelease(mFloorBmp);
		
		if (picTool.isExistFg()) {
			mOverBmp = picTool.getFgWithSize(size[0], size[1]);
		}
		mFloorBmp = picTool.getBgWithSize(size[0], size[1]);
	}

	public void reload(IInputStreamOpener pInputStreamOpener) {
		mStreamOpener=null;
		mStreamOpener=pInputStreamOpener;

		// 重新加载内部数据。
		byte data[] = null;// 存放配置文件的byte数据,由于配置文件不大，这样偏移会比较快
		// int dataCursor;//数据的游标
		InputStream lInputStream = null;
		try {
			lInputStream = this.mStreamOpener.open();
			data = StreamUtils.streamToBytes(lInputStream);
		} catch (Exception e) {
			e.printStackTrace();
			return;
		} finally {
			StreamUtils.close(lInputStream);
		}
		picTool.loadMaterialByBytes(data,size[0],size[1]);
		loadMaterial();

		mHasLoaded = true;
	}

	/**
	 *  清除所有数据
	 */
	public void clear() {
		this.mHasLoaded =false;
		this.mStreamOpener = null;
		this.mFloorBmp = null;
		this.mOverBmp = null;
	}

	public void clearBitmap() {
		BitmapOperate.SafeRelease(mOverBmp);
		BitmapOperate.SafeRelease(mFloorBmp);
		this.mFloorBmp = null;
		this.mOverBmp = null;
	}
}
