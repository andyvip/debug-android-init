package com.mt.mtxx.image;

import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;

import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;


public class NDKUtil {
	public static WeakReference<AssetManager> mAmReference;
	private static int mBmpW;
	private static int mBmpH;
	private static int mDataPic[];

	private static byte mDataBytes[];

	public static void setConext(Context context) {
		mAmReference = new WeakReference<AssetManager>(context.getAssets());
	}

	public static int LoadAssertsPic(String path) {
		try {
			InputStream is = mAmReference.get().open(path);
			BitmapFactory.Options options = new BitmapFactory.Options();
			options.inDither = false;
			options.inPreferredConfig = Config.ARGB_8888;
			Bitmap bmp = BitmapFactory.decodeStream(is, null, options);
			is.close();
			int w = bmp.getWidth();
			int h = bmp.getHeight();
			mDataPic = new int[w * h];
			bmp.getPixels(mDataPic, 0, w, 0, 0, w, h);
			bmp.recycle();
			mBmpW = w;
			mBmpH = h;
			System.gc();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			
		}
		return 1;
	}

	public static int LoadAssertsPicWidth() {
		return mBmpW;
	}

	public static int LoadAssertsPicHeight() {
		return mBmpH;
	}

	public static int LoadAssertsPicData(int data[]) {
		int n = 0;
		try {
			n = mDataPic.length;
			System.arraycopy(mDataPic, 0, data, 0, mDataPic.length);
			mDataPic = null;
			System.gc();
		} catch (Exception e) {
			
		}
		return n;
	}

	// 从assets 文件夹中获取字节流
	public static int LoadAssetsBytesLength(String fileName) {
		return ReadAssetsLength(fileName);// 放在子函数中调用，不然会挂掉
	}

	private static int ReadAssetsLength(String fileName) {
		int n = 0;
		try {
			InputStream in = mAmReference.get().open(fileName);
			n = in.available();
			// 获取文件的字节数
			// mFileLength = in.available(); //这个函数一被NDK调用就会挂掉，太奇怪了
			// //创建byte数组
			mDataBytes = new byte[n];
			// //将文件中的数据读到byte数组中
			in.read(mDataBytes);
			in.close();
		} catch (Exception e) {
			
		}
		return n;
	}

	// 从assets 文件夹中获取字节流
	public static int LoadAssetsBytes(byte data[]) {
		int n = 0;
		try {
			n = mDataBytes.length;
			System.arraycopy(mDataBytes, 0, data, 0, mDataBytes.length);
			mDataBytes = null;
			System.gc();
		} catch (Exception e) {
			
		}
		return n;
	}

	// /////////////////////////////////////////////////////////////
	public static int DecodeByteToPicSize(byte data[]) {
		Bitmap bmp = BitmapFactory.decodeByteArray(data, 0, data.length);
		int w = bmp.getWidth();
		int h = bmp.getHeight();
		mDataPic = new int[w * h];
		bmp.getPixels(mDataPic, 0, w, 0, 0, w, h);
		bmp.recycle();
		return w * 1000 + h;
	}

	public static int DecodeByteToPicData(int data[]) {
		System.arraycopy(mDataPic, 0, data, 0, mDataPic.length);
		mDataPic = null;
		return data.length;
	}
}
