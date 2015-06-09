package com.jrdcom.mt.core;

import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;

public class ImageProcess 
{
	/**
	 * 主要是将ARGB数据转换为Bitmap
	 * @param data
	 * @param w
	 * @param h
	 * @return
	 */
	public static Bitmap intARGB2Bitmap(int[] data, int w, int h) {
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
	
	
	/**
	 * bitmap转化成int数组
	 * @param bmp	将要被转换的图片
	 * @return		返回int数组的ARGB图片数据
	 */
	// 
	public static int[] bitmap2IntARGB(Bitmap bmp) {
		int pix[] = null;
		try{
			int w = bmp.getWidth();
			int h = bmp.getHeight();
			pix = new int[w * h];
			bmp.getPixels(pix, 0, w, 0, 0, w, h);
		}
		catch(Exception e){
			
		}
		return pix;
	}
}
