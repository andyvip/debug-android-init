package com.jrdcom.mt.core;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

//import com.meitu.sdkdemo.puzzle.BaseApplication;
import com.jrdcom.mt.util.StreamUtils;
import com.jrdcom.mt.mtxx.tools.BitmapOperate;

import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.Shader.TileMode;
import android.graphics.drawable.Drawable;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Debug;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.Toast;
import com.jrdcom.android.gallery3d.R;


/**
 * User: Javan.Eu Date: 12-11-6 Time: 下午3:25
 */
public class BitmapUtil {
	private static final String TAG = "BitmapUtil";

	/**
	 * the Max bitmap width
	 */
	//由于图片保存的尺寸最多是640*896拼接的宽也就是640所以可以改为900
	private static final int MAX_IMAGE_WIDTH = 700;		

	/**
	 * the Max bitmap height*
	 */
	private static final int MAX_IMAGE_HEIGHT = 700;	

	/**
	 * 水平翻转传入的图片
	 * 
	 * @param pSrc
	 *            需要翻转的图片
	 * @return 返回结果图片
	 */
	static public Bitmap flipHorizontal(Bitmap pSrc) {

		Bitmap mirrorPic = null;

		try {

			Matrix mirrorMatrix = new Matrix();
			mirrorMatrix.preScale(-1, 1);
			mirrorPic = Bitmap.createBitmap(pSrc, 0, 0, pSrc.getWidth(), pSrc.getHeight(), mirrorMatrix, true); // 镜像

		} catch (OutOfMemoryError e) {
//			Toast.makeText(BaseApplication.sharedApplication(), "内存耗尽", Toast.LENGTH_LONG).show();
		}

		return mirrorPic;
	}

	/**
	 * 上下翻转图片
	 * 
	 * @param pSrc
	 *            需要翻转的图片
	 * @return 翻转后的结果图
	 */
	static public Bitmap flipVertical(Bitmap pSrc) {

		Bitmap mirrorPic = null;

		try {

			Matrix mirrorMatrix = new Matrix();
			mirrorMatrix.preScale(1, -1);
			mirrorPic = Bitmap.createBitmap(pSrc, 0, 0, pSrc.getWidth(), pSrc.getHeight(), mirrorMatrix, true); // 镜像

		} catch (OutOfMemoryError e) {
//			Toast.makeText(BaseApplication.sharedApplication(), "内存耗尽", Toast.LENGTH_LONG).show();
		}

		return mirrorPic;
	}

	/**
	 * @param pSrc
	 * @param pDegree
	 * @return
	 */
	static public Bitmap rotateBitmapByDegree(Bitmap pSrc, float pDegree) {
		Bitmap lRes = null;

		try {

			Matrix matrix = new Matrix();
			matrix.postRotate(pDegree);
			lRes = Bitmap.createBitmap(pSrc, 0, 0, pSrc.getWidth(), pSrc.getHeight(), matrix, true); // 镜像

		} catch (OutOfMemoryError e) {
//			Toast.makeText(BaseApplication.sharedApplication(), "内存耗尽", Toast.LENGTH_LONG).show();
		}

		return lRes;
	}

	/**
	 * 翻转加旋转一张图片 如果没有任何变化的，函数直接返回原图 pSrc 传入的Bitmap对象。
	 * 
	 * @param pSrc
	 * @param pFlipHorizontal
	 * @param pFlipVertical
	 * @param pDegree
	 * @return
	 */
	static public Bitmap flipAndRotateBitmap(Bitmap pSrc, boolean pFlipHorizontal, boolean pFlipVertical, float pDegree) {
		if (pSrc == null) {
			return null;
		}

		Bitmap lRes = pSrc;

		if (pFlipHorizontal || pFlipVertical || pDegree != 0.0f) {
			try {

				Matrix matrix = new Matrix();

				if (pFlipVertical) {
					matrix.preScale(1, -1);
				}

				if (pFlipHorizontal) {
					matrix.preScale(-1, 1);
				}

				matrix.postRotate(pDegree);
				lRes = Bitmap.createBitmap(pSrc, 0, 0, pSrc.getWidth(), pSrc.getHeight(), matrix, true); // 镜像

			} catch (OutOfMemoryError e) {
			}
		}

		return lRes;
	}

	/**
	 * load a bitmap from SDcard by path
	 * 
	 * @param strPath
	 *            file path
	 * @param isNeedToCompress
	 *            when the value is true,the max size of the result bitmap is
	 *            1280*960 when the real size big than this
	 * @return
	 */
	public static Bitmap loadBitmapFromSDcard(String strPath, boolean isNeedToCompress) {
		FileInputStream input = null;
		// bitmap option
		BitmapFactory.Options options = new BitmapFactory.Options();

		/** 不真正的进行读 **/
		options.inJustDecodeBounds = true;

		// pre read the bitmap,the result is null
		Bitmap bmp = BitmapFactory.decodeFile(strPath, options);

		// the real size of the bitmap
		int realWidth = options.outWidth;
		int realHeight = options.outHeight;

		/** 预期大小 **/
		int resultWidth = realWidth;
		int resultHeight = realHeight;
		// read the bitmap
		if (realWidth > MAX_IMAGE_WIDTH || realHeight > MAX_IMAGE_HEIGHT) {
			if (isNeedToCompress == true) {
				int simpleSize = 1;
				while (true) {
					if (resultWidth < MAX_IMAGE_WIDTH && resultHeight < MAX_IMAGE_HEIGHT) {
						break;
					}
					resultWidth /= 2;
					resultHeight /= 2;
					simpleSize *= 2;
				}
				options.inSampleSize = simpleSize;
			}
		}
		// read real data
		options.inJustDecodeBounds = false;
		options.inPurgeable = true;
		options.inInputShareable = true;
		try {
			bmp = BitmapFactory.decodeFile(strPath, options);
		} catch (Exception e) {
			return null;
		}
        //PR-526083 added by xiaowei.xu begin
        if(bmp == null){
            return null;
        }
        //PR-526083 added by xiaowei.xu end 

		// 如果还没有到预期大小则进行放缩
		Bitmap resultBitmap = null;
		if (bmp.getWidth() > resultWidth || bmp.getHeight() > resultHeight) {
			resultBitmap = Bitmap.createScaledBitmap(bmp, resultWidth, resultHeight, false);
			bmp.recycle();
			bmp = null;
		} else
			resultBitmap = bmp;
		return resultBitmap;
	}

	public static int calculateInSampleSize(Context pContext, Uri pBitmapUri, int pTargetWidth, int pTargetHeight,
			float pInitScale) {
		pTargetWidth = (int) (pTargetWidth * pInitScale);
		pTargetHeight = (int) (pTargetHeight * pInitScale);
		if (pTargetWidth > MAX_IMAGE_WIDTH)
			pTargetWidth = MAX_IMAGE_WIDTH;
		if (pTargetHeight > MAX_IMAGE_HEIGHT)
			pTargetHeight = MAX_IMAGE_HEIGHT;

		int lInSampleSize = 1;
		boolean lError = false;
		// bitmap option
		BitmapFactory.Options options = new BitmapFactory.Options();
		options.inJustDecodeBounds = true;

		// pre read the bitmap,the result is null
		InputStream input = null;
		try {
			// 第一次伪读取
			input = pContext.getContentResolver().openInputStream(pBitmapUri);
			BitmapFactory.decodeStream(input, null, options);
		} catch (Exception e1) {
			lError = true;
		} finally {
			StreamUtils.close(input);
		}

		if (!lError) {
			// the real size of the bitmap
			int realWidth = options.outWidth;
			int realHeight = options.outHeight;

			int resultWidth = realWidth;
			int resultHeight = realHeight;
			// 计算simpleSize
			while (true) {
				if (resultWidth - pTargetWidth < pTargetWidth / 2 && resultHeight - pTargetHeight < pTargetHeight / 2) {// 在最大尺寸一半的范围内
					break;
				}
				resultWidth /= 2;
				resultHeight /= 2;
				lInSampleSize *= 2;
			}
		}

		return lInSampleSize;
	}

	/**
	 * 将BitmapConfig改为ARGB8888
	 * @param src
	 * @param isSrcRelease
	 * @return
	 */
	public static Bitmap Change2ARGB8888(Bitmap src, boolean isSrcRelease) {
		Bitmap lRes = null;
		Bitmap.Config lConfig = src.getConfig();

		if (null != lConfig && lConfig.compareTo(Bitmap.Config.ARGB_8888) == 0) {
			lRes = src;
		} else {
			lRes = src.copy(Bitmap.Config.ARGB_8888, false);
		}
		if (isSrcRelease && lRes != src)
			BitmapOperate.SafeRelease(src);
		return lRes;

	}
	/**
	 * 给一张图片添加白边
	 * 
	 * @param srcBmp
	 *            源图片
	 * @param frameWidth
	 *            白边宽度
	 * @param isNeedToRelease
	 *            是否需要释放
	 * @return
	 */
	public static Bitmap addWhiteFrame(Context pContext, Bitmap srcBmp, int frameWidth, boolean isNeedToRelease) {
		/** 目标图片大小 **/
		int lDstWidth = srcBmp.getWidth() + 3 * frameWidth;
		int lDstHeight = srcBmp.getHeight() + 3 * frameWidth;
		/** 生成底图 **/
		Bitmap dstBitmap = Bitmap.createBitmap(lDstWidth, lDstHeight, Config.ARGB_8888);
		Canvas canvas = new Canvas(dstBitmap);
		// /**.9图片资源**/
		// Bitmap whiteRes =
		// BitmapFactory.decodeResource(pContext.getResources(),
		// R.drawable.ic_white_shadow);
		// /**.9对象**/
		// NinePatch np = new NinePatch(whiteRes, whiteRes.getNinePatchChunk(),
		// null);
		Drawable lShadow = pContext.getResources().getDrawable(R.drawable.ic_white_shadow);
		lShadow.setBounds(0, 0, lDstWidth, lDstHeight);
		// Rect rectBottom = new Rect(0, 0, lDstWidth, lDstHeight);
		/** 平铺白底阴影 **/
		lShadow.draw(canvas);
		Rect rectTop = new Rect(3 * frameWidth / 2, 3 * frameWidth / 2, srcBmp.getWidth() + 3 * frameWidth / 2,
				srcBmp.getHeight() + 3 * frameWidth / 2);
		/** 蒙上图片 **/
		canvas.drawBitmap(srcBmp, null, rectTop, null);
		if (isNeedToRelease) {
			srcBmp.recycle();
			srcBmp = null;
		}
		return dstBitmap;
	}

	/**
	 * 裁剪落在某一路径里面的一块图形
	 * 
	 * @param bmp
	 *            原图片
	 * @param maxRect
	 *            包含路径的最大矩形
	 * @param path
	 *            基于最大矩形的路径
	 * @param matrix
	 *            包含图片所做的各种动作的matrix
	 * @param isNeedToReleaseOrg
	 *            是否需要在内部释放原Bitmap以减少绘制过程的内存占用
	 * @return 最大矩形大小的bitmap
	 */
	public static Bitmap clipBitmapByPath(Bitmap bmp, Rect maxRect, Path path, Matrix matrix, boolean isNeedToReleaseOrg) {

		int width = maxRect.width();
		int height = maxRect.height();
		Bitmap lResBitmap = null;
		Bitmap lTmpBitmap = null;
		boolean lRes = false;
		try {
			/** 先建立一个最大矩形的bitmap */
			lTmpBitmap = Bitmap.createBitmap(width, height, Config.ARGB_8888);
			Canvas canvas = new Canvas(lTmpBitmap);

			// bitmap pen
			Paint mBitmapPaint = new Paint();
			mBitmapPaint.setAntiAlias(true);
			mBitmapPaint.setFilterBitmap(true);
			/** 绘制矩形里面的图片内容 */
			canvas.drawBitmap(bmp, matrix, mBitmapPaint);

			// Paint mBitmapPaint = new Paint();
			if (isNeedToReleaseOrg == true) {
				bmp.recycle();
			}

			/** 最终生成的在path里面的bitmap */
			lResBitmap = Bitmap.createBitmap(width, height, Config.ARGB_8888);
			Canvas lResBitmapCanvas = new Canvas(lResBitmap);
			mBitmapPaint.setShader(new BitmapShader(lTmpBitmap, TileMode.CLAMP, TileMode.CLAMP));

			/** 按Path裁剪矩形里面的内容 **/
			lResBitmapCanvas.drawPath(path, mBitmapPaint);
			lRes = true;

		} catch (OutOfMemoryError error) {

		}
		if (!lRes) {
			try {
				BitmapOperate.SafeRelease(lTmpBitmap);
				lTmpBitmap = null;
				BitmapOperate.SafeRelease(lResBitmap);
				lResBitmap = null;
				System.gc();
				lResBitmap = Bitmap.createBitmap(width, height, Config.ARGB_8888);
				Canvas canvas = new Canvas(lResBitmap);
				// bitmap pen
				Paint mBitmapPaint = new Paint();
				mBitmapPaint.setAntiAlias(true);
				mBitmapPaint.setFilterBitmap(true);
				canvas.save();
				canvas.clipPath(path);
				/** 绘制矩形里面的图片内容 */
				canvas.drawBitmap(bmp, matrix, mBitmapPaint);
				canvas.restore();
			} catch (OutOfMemoryError e) {
				e.printStackTrace();
				return null;
			}
		}
		return lResBitmap;

	}

	/**
	 * save a Bitmap to the object SD filePath
	 * 
	 * @param savePath
	 * @param bmp
	 * @return
	 */
	public static boolean saveBitmapToSDcard(String savePath, Bitmap bmp, CompressFormat format) {

		File f = new File(savePath);
		try {
			f.createNewFile();
		} catch (IOException e) {
			Log.i(TAG, "file create failed");
			return false;
		}
		FileOutputStream fOut = null;
		try {
			fOut = new FileOutputStream(f);
		} catch (FileNotFoundException e) {
			Log.i(TAG, "file create failed");
			return false;
		}
		bmp.compress(format, 100, fOut);
		try {
			fOut.flush();
			fOut.close();

		} catch (IOException e) {
			Log.i(TAG, "save failed by ioexception");
			return false;
		}
		return true;

	}

	public static int[] createIntARGBBitmap(Bitmap pSrc) {

		int pix[] = null;

		if (pSrc != null) {

			try {
				int w = pSrc.getWidth();
				int h = pSrc.getHeight();
				pix = new int[w * h];
				pSrc.getPixels(pix, 0, w, 0, 0, w, h);
			} catch (OutOfMemoryError e) {
			}

		}

		return pix;

	}

	/**
	 * Convert the bytes format by ARGB_8888 to a bitmap
	 * 
	 * @param data
	 *            byte datas
	 * @param w
	 *            bitmap width
	 * @param h
	 *            bitmap height
	 * @return a result bitmap,return null when convert failed
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

	public static int getBitmapOrientation(String pPath) {
		ExifInterface exif = null;
		int lRes = 1;
		try {
			exif = new ExifInterface(pPath);
			lRes = Integer.parseInt(exif.getAttribute(ExifInterface.TAG_ORIENTATION));
			lRes = lRes == 0 ? 1 : lRes;
		} catch (IOException e) {
			e.printStackTrace();
		}
		return lRes;
	}
	
	public static Bitmap getThumbBmp(String filePath, int maxLength) {
		BitmapFactory.Options options = new BitmapFactory.Options();
		options.inJustDecodeBounds = true;
		try {
			BitmapFactory.decodeStream(new FileInputStream(filePath), null, options);
			float width_tmp = options.outWidth;
			float height_tmp = options.outHeight;
			float bigger = Math.max(height_tmp, width_tmp);
			float scale = bigger / maxLength;
			options.inSampleSize = (int) (scale);
			options.inJustDecodeBounds = false;
			options.inDither = false;
			options.inPreferredConfig = Config.ARGB_8888;
			return BitmapFactory.decodeStream(new FileInputStream(filePath), null, options);
		} catch (Exception e) {
			return null;
		}
	}
	
	/**
	 * compute Sample Size
	 *
	 * @param options
	 * @param minSideLength
	 * @param maxNumOfPixels
	 * @return
	 */
	public static int computeSampleSize(BitmapFactory.Options options,
	                                    int minSideLength, int maxNumOfPixels) {
		int initialSize = computeInitialSampleSize(options, minSideLength,
				maxNumOfPixels);

		int roundedSize;
		if (initialSize <= 8) {
			roundedSize = 1;
			while (roundedSize < initialSize) {
				roundedSize <<= 1;
			}
		} else {
			roundedSize = (initialSize + 7) / 8 * 8;
		}

		return roundedSize;
	}

	/**
	 * compute Initial Sample Size
	 *
	 * @param options
	 * @param minSideLength
	 * @param maxNumOfPixels
	 * @return
	 */
	private static int computeInitialSampleSize(BitmapFactory.Options options,
	                                            int minSideLength, int maxNumOfPixels) {
		double w = options.outWidth;
		double h = options.outHeight;

		// 上下限范围
		int lowerBound = (maxNumOfPixels == -1) ? 1 : (int) Math.ceil(Math
				.sqrt(w * h / maxNumOfPixels));
		int upperBound = (minSideLength == -1) ? 128 : (int) Math.min(
				Math.floor(w / minSideLength), Math.floor(h / minSideLength));

		if (upperBound < lowerBound) {
			// return the larger one when there is no overlapping zone.
			return lowerBound;
		}

		if ((maxNumOfPixels == -1) && (minSideLength == -1)) {
			return 1;
		} else if (minSideLength == -1) {
			return lowerBound;
		} else {
			return upperBound;
		}
	}

    /**
     * 根据图片文件的路径获取图片的EXIF信息里的方向
     * @param path  图片文件的绝对路径
     * @return      图片文件里的EXIF信息里的方向
     */
	public static String getOrientation(String path) {
		ExifInterface exif = null;
		try {
			exif = new ExifInterface(path);
		} catch (IOException e) {
			// TODO Auto-generated catch block
		}
		return exif.getAttribute(ExifInterface.TAG_ORIENTATION);
	}

	public static String getRealPathFromUri(Activity activity, Uri contentUri) {
		if (null == contentUri) {
			return null;
		}

		String[] projs = { MediaStore.Images.Media.DATA };
		Cursor cursor = activity.getContentResolver().query(contentUri, // 内容的uri
				projs, // Which columns to return
				null, // WHERE clause; which rows to return (all rows)
				null, // WHERE clause selection arguments (none)
				null); // Order-by clause (ascending by name)
		String finalFilePath = null;
		if (cursor != null) {
			int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            //PR669201-taoli-begin 001
            try {
                cursor.moveToFirst();
                finalFilePath = cursor.getString(column_index);
            } catch (Exception e) {
                Log.e(TAG, "CursorIndexOutOfBoundsException");
                finalFilePath = null;
            }finally{
                if (cursor != null) {
                    cursor.close();
                    cursor = null;
                }
            }
            //PR669201-taoli-end 001
		} else {
			finalFilePath = contentUri.getPath();
		}
		return finalFilePath;
	}

	public static boolean SafeRelease(Bitmap bmp) {
		if (bmp != null && !bmp.isRecycled()) {
			bmp.recycle();
			bmp = null;
			return true;
		}
		return false;
	}
	}
