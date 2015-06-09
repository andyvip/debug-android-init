package com.jrdcom.mt.mtxx.tools;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;

import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;

public class BitmapOperate {

	// 保存文件
	public static boolean savePic(String path, Bitmap bmp, int quality) {
		if (bmp == null || bmp.isRecycled()) {
			return false;
		}
		File myCaptureFile = new File(path);
		try {
			BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(myCaptureFile));

			if (quality == 1) {// jpg
				bmp.compress(Bitmap.CompressFormat.JPEG, 100, bos);
			} else if (quality == 2) {// png
				bmp.compress(Bitmap.CompressFormat.PNG, 100, bos);
			}
			try {
				bos.flush();
				bos.close();
				// writeEixf(path);
			} catch (IOException e) {
			}
		} catch (FileNotFoundException e) {
		}
		return true;
	}

	public static Bitmap LoadAssertsPic(String path, AssetManager am) {
		Bitmap bmp = null;
		try {
			InputStream is = am.open(path);
			BitmapFactory.Options options = new BitmapFactory.Options();
			options.inDither = false;
			options.inPreferredConfig = Config.ARGB_8888;
			bmp = BitmapFactory.decodeStream(is, null, options);
			is.close();

		} catch (IOException e) {
		}
		return bmp;
	}

	// 释放图片内存
	public static boolean SafeRelease(Bitmap bmp) {
		if (bmp != null && !bmp.isRecycled()) {
			bmp.recycle();
			bmp = null;
			return true;
		}
		return false;
	}

	// 预览大图片,拼图用,节省内存
	public static Bitmap PreviewBigPicTemplate(String path, Config config, int radius) {
		File f = new File(path);
		try {
			BitmapFactory.Options o = new BitmapFactory.Options();
			o.inJustDecodeBounds = true;
			FileInputStream input = new FileInputStream(f);
			BitmapFactory.decodeStream(input, null, o);
			try {
				input.close();
			} catch (IOException e) {
			}

			// Find the correct scale value. It should be the power of 2.
			// final int REQUIRED_SIZE = 640;
			int width_tmp = o.outWidth, height_tmp = o.outHeight;
			int scale = 1;
			while (true) {
				if (width_tmp < radius && height_tmp < radius) {
					break;
				}
				width_tmp /= 2;
				height_tmp /= 2;
				scale *= 2;
			}
			// decode with inSampleSize
			if (scale > 1) {
				BitmapFactory.Options o2 = new BitmapFactory.Options();
				o2.inSampleSize = scale;
				o2.inJustDecodeBounds = false;
				o2.inDither = false;
				o2.inPreferredConfig = config;

				input = new FileInputStream(f);
				Bitmap bmpReturn = BitmapFactory.decodeStream(input, null, o2);

				try {
					input.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
				}
				return bmpReturn;
			} else {// 不缩放
				BitmapFactory.Options options = new BitmapFactory.Options();
				options.inDither = false;
				options.inPreferredConfig = config;
				Bitmap bmpReturn = BitmapFactory.decodeFile(path, options);

				return bmpReturn;
			}
		} catch (FileNotFoundException e) {
		} finally {

		}
		return null;
	}
	
	public static Bitmap createScaledBitmap(Bitmap putBitmap, int dstW, int dstH, Config dstConfig) {
		Bitmap bitmap = null;
		try {
			Paint p = new Paint();
			p.setDither(false);
			p.setAntiAlias(true);
			p.setFilterBitmap(true);

			if (dstW <= 0) {
				dstW = 1;
			}
			if (dstH <= 0) {
				dstH = 1;
			}
			if (dstConfig == null) {// zk 20110726
				dstConfig = Config.ARGB_8888;
			}
			bitmap = Bitmap.createBitmap(dstW, dstH, dstConfig);
			Canvas c2 = new Canvas(bitmap);

			c2.drawBitmap(putBitmap, new Rect(0, 0, putBitmap.getWidth(), putBitmap.getHeight()), new Rect(0, 0, dstW,
					dstH), p);
		} catch (Exception e) {
		}
		return bitmap;
	}
	
	// 将一张图片适应窗口大小 缩放或放大
		// w h 目标窗口宽高
		public static Bitmap FittingWindow(Bitmap src, int w, int h, boolean isNeedRelease) {
			Bitmap bmpDst = null;
			try {
				float scale;
				int srcW, srcH, dstW, dstH;
				srcW = src.getWidth();
				srcH = src.getHeight();
				if (srcW * h > srcH * w) {// 适应宽度
					dstW = w;
					dstH = srcH * w / srcW;
					scale = (float) dstW / srcW;
				} else { // 适应高度
					dstH = h;
					dstW = srcW * h / srcH;
					scale = (float) dstH / srcH;
				}
				scale = ((int) (scale * 1000)) / 1000.0f;
				Matrix matrix = new Matrix();
				matrix.reset(); // 重置矩阵
				// matrix.postScale(scale, scale);
				matrix.preScale(scale, scale);

				if (srcW == dstW && srcH == dstH) {// 相同尺寸，返回的是原图的引用
					bmpDst = src.copy(src.getConfig(), true);
				} else {
					bmpDst = createScaledBitmap(src, dstW, dstH, src.getConfig());
				}
				if (isNeedRelease) {
					src.recycle();
					src = null;
				}
			} catch (Exception e) {
			}
			return bmpDst;
		}
		
		
		
		/** result =0 x,=1 y,=2 scale */
		// 将一张图片适应窗口大小
		public static boolean FittingWindowSize(int srcW, int srcH, int dstW, int dstH, float result[]) {
			try {
				if (srcW * dstH > srcH * dstW) {// 适应宽度
					result[0] = dstW;
					result[1] = srcH * dstW / srcW;
					result[2] = (float) dstW / srcW;

				} else { // 适应高度
					result[1] = dstH;
					result[0] = srcW * dstH / srcH;
					result[2] = (float) dstH / srcH;
				}
			} catch (Exception e) {
			}
			return true;
		}
		
		/**
		 * 按比列裁剪图片
		 * 
		 * @param bmp
		 * @param dstW
		 * @param dstH
		 * @return
		 */
		public static Bitmap cutBmpTemplate(Bitmap bmp, int dstW, int dstH, boolean isNeedRelease) {
			float scale = (float) dstW / dstH;
			Bitmap bmpDst = null;
			if (bmp.getWidth() < bmp.getHeight() * scale) {// 按高度裁剪
				int dh = (int) (bmp.getHeight() - bmp.getWidth() / scale) / 2;
				int w = bmp.getWidth();
				int h = bmp.getHeight() - dh * 2;
				bmpDst = cut(bmp, 0, dh, w, dh + h, isNeedRelease);
			} else {// 按宽度裁剪
				int dw = (int) (bmp.getWidth() - bmp.getHeight() * scale) / 2;
				int w = bmp.getWidth() - dw * 2;
				int h = bmp.getHeight();
				bmpDst = cut(bmp, dw, 0, dw + w, h, isNeedRelease);
			}
			return bmpDst;
		}
		
		// 裁剪
		public static Bitmap cut(Bitmap bmp, int left, int top, int right, int bottom, boolean isNeedRelease) {
			Bitmap bmpDst = null;
			try {
				// 最小值为1，防止过分裁剪长宽为0
				if (right <= left) {
					right = left + 1;
				}
				if (bottom < top) {
					bottom = top + 1;
				}
				if (android.os.Build.VERSION.SDK_INT > 8) {
					bmpDst = Bitmap.createBitmap(right - left, bottom - top, Config.ARGB_8888);
				} else {
					bmpDst = Bitmap.createBitmap(right - left, bottom - top, Config.RGB_565);
				}

				Canvas canvas = new Canvas(bmpDst);
				// canvas.drawBitmap(bmpBack, rectSelect.left, rectSelect.top,
				// null);
				Paint mPaint = new Paint();
				mPaint.setAntiAlias(true);
				canvas.drawBitmap(bmp, new Rect(left, top, right, bottom), new Rect(0, 0, right - left, bottom - top),
						mPaint);
				if (isNeedRelease) {
					bmp.recycle();
					bmp = null;

				}
			} catch (Exception e) {
			}
			return bmpDst;
		}
		
		// 放大
		public static Bitmap scale(Bitmap src, float scale, boolean isNeedRelease) {
			Bitmap bmpDst = null;
			try {
				/* 产生Resize后的Bitmap对象 */
				if (scale == 1) {// 相同尺寸，返回的是原图的引用
					bmpDst = src.copy(src.getConfig(), true);
				} else {
					int dstW = (int) (src.getWidth() * scale);
					int dstH = (int) (src.getHeight() * scale);
					if (dstW < 1) {
						dstW = 1;
					}
					if (dstH < 1) {
						dstH = 1;
					}
					bmpDst = createScaledBitmap(src, dstW, dstH, src.getConfig());
				}

				if (isNeedRelease) {
					src.recycle();
					src = null;
				}
			} catch (Exception e) {
			}
			return bmpDst;
		}
		
		// 旋转 angle=0~360
		public static Bitmap rotate(Bitmap src, float degress, boolean isNeedRelease) {
			Bitmap bmpDst = null;
			try {
				/* 产生Resize后的Bitmap对象 */
				if (degress == 0 || degress == 360) {
					bmpDst = src.copy(src.getConfig(), true);
				} else {// 相同尺寸，返回的是原图的引用
					Matrix matrix = new Matrix();
					matrix.preRotate(degress);

					bmpDst = Bitmap.createBitmap(src, 0, 0, src.getWidth(), src.getHeight(), matrix, true);
				}
				if (isNeedRelease) {
					src.recycle();
					src = null;
				}
			} catch (Exception e) {
			}
			return bmpDst;
		}
		
		// 获取实际放大倍数（边界不越界）,获取放大后的偏移
		// result =0 posx,=1 posy,=2 w,=3 h,=4 scale
		public static boolean GetFittingSize(int srcW, int srcH, int curW, int curH, int backW, int backH, int posX,
				int posY, float scale, float angle, float val[]) {
			int midX = posX + curW / 2;
			int midY = posY + curH / 2;

			int rotateSize[] = new int[2];
			GetRotateSize(srcW, srcH, angle, rotateSize);
			int optHalfW = rotateSize[0] / 2;
			int optHalfH = rotateSize[1] / 2;

			val[0] = midX - optHalfW * scale;
			val[1] = midY - optHalfH * scale;
			val[2] = midX + optHalfW * scale;
			val[3] = midY + optHalfH * scale;
			val[4] = scale;

			return true;
		}
		
		// 获取旋转后图片的宽高 angle=0~360, val[0]=w,val[1]=h
		public static boolean GetRotateSize(int srcW, int srcH, float angle, int dstSize[]) {
			if (angle == 0 || angle == 180 || angle == 360) {
				dstSize[0] = srcW;
				dstSize[1] = srcH;
			} else if (angle == 90 || angle == 270) {
				dstSize[0] = srcH;
				dstSize[1] = srcW;
			} else if (angle < 90 || (angle > 180 && angle < 270)) {
				angle = (float) (angle * Math.PI / 180);

				// y轴
				int hs = (int) Math.abs(srcH * Math.sin(angle));
				int wc = (int) Math.abs(srcW * Math.cos(angle));
				// x轴
				int ws = (int) Math.abs(srcW * Math.sin(angle));
				int hc = (int) Math.abs(srcH * Math.cos(angle));

				dstSize[0] = hs + wc;
				dstSize[1] = hc + ws;
			} else if ((angle > 90 && angle < 180) || (angle > 270 && angle < 360)) {
				angle = (float) (angle * Math.PI / 180);

				// y轴
				int hs = (int) Math.abs(srcH * Math.sin(angle));
				int wc = (int) Math.abs(srcW * Math.cos(angle));
				// x轴
				int ws = (int) Math.abs(srcW * Math.sin(angle));
				int hc = (int) Math.abs(srcH * Math.cos(angle));

				dstSize[0] = hs + wc;
				dstSize[1] = hc + ws;
			}

			return true;
		}
		
		
		//拼图，小于1:3的要裁剪
		public static Point getPicSizeTemplate(String path){
			Point pt = new Point(0,0);	
			try {
				File f = new File(path);
				BitmapFactory.Options o = new BitmapFactory.Options();
				o.inJustDecodeBounds = true;
				FileInputStream input = new FileInputStream(f);
				BitmapFactory.decodeStream(input, null, o);
				input.close();
				// Find the correct scale value. It should be the power of 2.
				pt.x = o.outWidth;
				pt.y = o.outHeight;
				if(pt.y > pt.x * 3){
					pt.y = pt.x * 3;
				}
			} catch (IOException e) {
			}
			return pt;
		}
		
		/**
		 * 从数据流中读取数据
		 * 
		 * @param path
		 * @return
		 */
		public static Bitmap readPicBuffer(String path) {
			Bitmap bmp = null;
			try {
				BufferedInputStream bufferedInputStream = new BufferedInputStream(new FileInputStream(path));
				byte byteVal[] = new byte[40];
				bufferedInputStream.read(byteVal, 0, 4);// 宽
				int w = bytes2int(byteVal);

				bufferedInputStream.read(byteVal, 0, 4);// 宽
				int h = bytes2int(byteVal);

				int len = bufferedInputStream.available();
				byte[] bytes = new byte[len];
				bufferedInputStream.read(bytes, 0, len);

				bmp = Bitmap.createBitmap(w, h, Config.ARGB_8888);
				ByteBuffer bbf = ByteBuffer.wrap(bytes);

				bmp.copyPixelsFromBuffer(bbf);
			} catch (Exception e) {
			}
			return bmp;
		}
		
		public static byte[] int2bytes(int i) {
			byte[] result = new byte[4];
			result[0] = (byte) ((i >> 24) & 0xFF);
			result[1] = (byte) ((i >> 16) & 0xFF);
			result[2] = (byte) ((i >> 8) & 0xFF);
			result[3] = (byte) (i & 0xFF);
			return result;
		}
		
		public static int bytes2int(byte a[]) {
			int s = 0;
			for (int i = 0; i < 3; i++) {
				if (a[i] >= 0)
					s = s + a[i];
				else
					s = s + 256 + a[i];
				s = s * 256;
			}
			if (a[3] >= 0) // 最后一个之所以不乘，是因为可能会溢出
				s = s + a[3];
			else
				s = s + 256 + a[3];
			return s;
		}
		
		//绘制图片的中间区域至画布
		public static boolean drawBmpMidToCanvas(Canvas canvas,Bitmap bmp,Rect rect){
			try{
				boolean isNew = false;
				if(bmp.getWidth() < rect.width()){
					isNew = true;
					int h = bmp.getHeight() * rect.width() / bmp.getWidth();
					bmp = resize(bmp, rect.width(), h, false);
				}
				int dw = (bmp.getWidth() - rect.width()) / 2;
				int dh = (bmp.getHeight() - rect.height()) / 2;
			
				//防止取出的边界超出图片边界
				int tw = dw+rect.width();
				if(tw > bmp.getWidth()){
					tw = bmp.getWidth();
				}
				int th = dh+rect.height();
				if(th > bmp.getHeight()){
					th = bmp.getHeight();
				}
				canvas.drawBitmap(bmp, new Rect(dw,dh,tw,th), rect, null);
				if(isNew){
					bmp.recycle();
					bmp = null;
				}
			}catch(Exception e){
			}
			return true;
		}
		
		// 缩放，不等比例，指定宽高
		public static Bitmap resize(Bitmap bmp, int width, int height, boolean isNeedRelease) {
			Bitmap bmpDst = null;
			try {
				if (width == bmp.getWidth() && height == bmp.getHeight()) {
					bmpDst = bmp.copy(Config.ARGB_8888, true);
				} else {// 相同尺寸，返回的是原图的引用
					bmpDst = createScaledBitmap(bmp, width, height, bmp.getConfig());
				}
				if (isNeedRelease) {
					bmp.recycle();
					bmp = null;
				}
			} catch (Exception e) {
			}
			return bmpDst;
		}
		
		/**
		 * 把Bitmap按byte数据流写入文件
		 * 
		 * @param path
		 * @param bmp
		 * @return
		 */
		public static boolean savePicBuffer(String path, Bitmap bmp) {
			ByteBuffer dst = ByteBuffer.allocate(bmp.getWidth() * bmp.getHeight() * 4);
			bmp.copyPixelsToBuffer(dst);
			try {
				FileOutputStream fos = new FileOutputStream(path);
				// 把长宽写入头部
				fos.write(int2bytes(bmp.getWidth()));
				fos.write(int2bytes(bmp.getHeight()));
				fos.write(dst.array());
				fos.flush();
				fos.close();
			} catch (IOException e) {
			}
			return true;
		}
		
		// bitmap(指定部分)转化成int数组
		public static int[] bitmap2IntARGBPart(Bitmap bmp,int x,int y,int w,int h) {
			int pix[] = null;
			try{
				pix = new int[w * h];
				bmp.getPixels(pix, 0, w, x, y, w, h);
			}
			catch(Exception e){
			}
			return pix;
		}
		
		// byte数组转化成bitmap
		public static Bitmap ChangeBitmapDataPart(Bitmap bmp,int[] data,int x,int y,int w,int h) {
			try{
				if(data.length != w * h){
					return null;
				}
				if (data.length > 0) {
					if (bmp == null) {
						return null;
					}
					bmp.setPixels(data, 0, w, x, y, w, h);
					return bmp;
				}
			}
			catch(Exception e){
			}
			return null;
		}
		
		// byte数组转化成bitmap
		public static Bitmap bytesARGB2Bimap(byte[] data, int w, int h) {
			try{
				if (data.length > 0) {
					if(data.length != w * h * 4){
						return null;
					}
					Bitmap bmp = Bitmap.createBitmap(w, h, Config.ARGB_8888);
					if (bmp == null) {
						return null;
					}
					int pix[] = new int[w * h];
					int i,j,posFour,posOne;
					// byte是有符号的，这里要通过&转化成无符号的
					for (i = posOne = posFour =0; i < h; i++) {
						for (j = 0; j < w; j++) {
							pix[posOne] = (data[posFour] & 0xff)
									+ ((data[posFour + 1] & 0xff) << 8)
									+ ((data[posFour + 2] & 0xff) << 16)
									+ ((data[posFour + 3] & 0xff) << 24);
							posFour += 4;
							posOne++;
						}
					}
					bmp.setPixels(pix, 0, w, 0, 0, w, h);
					pix = null;
					return bmp;
				} else {
					return null;
				}
			}
			catch(Exception e){
			}
			return null;
		}
		
		/**
		 * 挖掉指定区域，变成透明
		 * 
		 * @param rect
		 * @return
		 */
		public static boolean cutRectAlpha(Bitmap bmp, Rect rect) {
			int n = rect.width() * rect.height();
			int data[] = new int[n];
			for (int i = 0; i < n; i++) {
				data[i] = 0x110000ff;
			}
			bmp.setPixels(data, 0, rect.width(), rect.left, rect.top, rect.width(), rect.height());
			return true;
		}
		// bitmap转化成int数组
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

		// byte数组转化成bitmap
		public static Bitmap ChangeBitmapData(Bitmap bmp,int[] data) {
			try{
				int w = bmp.getWidth();
				int h = bmp.getHeight();
				if(data.length != w * h){
					return null;
				}
				if (data.length > 0) {
					if (bmp == null) {
						return null;
					}
					bmp.setPixels(data, 0, w, 0, 0, w, h);
					return bmp;
				}
			}
			catch(Exception e){
			}
			return null;
		}
		
		// byte数组转化成bitmap
		public static Bitmap intARGB2Bimap(int[] data, int w, int h) {
			try{
				if(data == null || data.length != w * h){
					return null;
				}
				Bitmap bmp = Bitmap.createBitmap(w, h, Config.ARGB_8888);
				if (bmp == null) {
					return null;
				}
				bmp.setPixels(data, 0, w, 0, 0, w, h);
				return bmp;
			}
			catch(Exception e){
			}
			return null;
		}
		

}
