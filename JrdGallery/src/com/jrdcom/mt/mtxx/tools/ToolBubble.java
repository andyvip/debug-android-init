package com.jrdcom.mt.mtxx.tools;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Rect;

public class ToolBubble {
	private Context mContext;
	public ToolBubble(Context context)
	{
		mContext=context;
	}
	/**
	 * 解析气泡的大小信息
	 * 
	 * @param filepath
	 * @return Rect 包含气泡最小矩形
	 */
	public Rect getBubbleRc(String filepath, boolean isSD) {
		byte[] data = getBytesFromFile(filepath, isSD);

		byte[] left = new byte[4];
		byte[] right = new byte[4];
		byte[] top = new byte[4];
		byte[] bottom = new byte[4];
		arraycopy(data, 187, left, 4);
		arraycopy(data, 191, top, 4);
		arraycopy(data, 211, right, 4);
		arraycopy(data, 215, bottom, 4);

		Rect rc = new Rect();
		rc.left = bytesToInt(left);
		rc.right = bytesToInt(right);
		rc.top = bytesToInt(top);
		rc.bottom = bytesToInt(bottom);

		return rc;
	}

	/**
	 * 解析气泡的图片信息
	 * @param filepath
	 * @param isSD
	 * @return 气泡的一张bitmap
	 */
	public Bitmap getBubbleBg(String filepath,boolean isSD) {
		byte[] data = null;
		data = getBytesFromFile(filepath,isSD);

		byte[] pngbytes = new byte[data.length - 383];
		arraycopy(data, 383, pngbytes, data.length - 383);

		try {
			InputStream is = new ByteArrayInputStream(pngbytes);
			Bitmap bmp = BitmapFactory.decodeStream(is);
			return bmp;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * 解析气泡的颜色信息
	 * 
	 * @param filepath
	 * @return
	 */
	public int getBubbleColor(String filepath,boolean isSD) {
		byte[] color = new byte[4];
		byte[] data;
		byte[] tmp = new byte[4];
		data = getBytesFromFile(filepath, isSD);

		arraycopy(data, 220, color, 4);
		tmp[3] = (byte) 255;
		tmp[0] = color[2];
		tmp[1] = color[1];
		tmp[2] = color[0];
		return bytesToInt(tmp);

	}

	/**
	 * @param bytes
	 * @return
	 */
	public static int bytesToInt(byte[] bytes) {
		int num = 0;
		byte temp;
		temp = bytes[0];
		bytes[0] = bytes[3];
		bytes[3] = temp;
		temp = bytes[1];
		bytes[1] = bytes[2];
		bytes[2] = temp;

		ByteArrayInputStream bintput = new ByteArrayInputStream(bytes);
		DataInputStream dintput = new DataInputStream(bintput);
		try {
			num = dintput.readInt();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return num;
	}


	/**把Asset中或者SD文件转化为字节流
	 * @param filepath Assets为相对路径，SD为绝对路径
	 * @param isSD
	 * @param context
	 * @return
	 */
	private byte[] getBytesFromFile(String filepath, boolean isSD) {
		byte[] data = null;
		InputStream in = null;
		try {
			if (isSD == false)
				in = mContext.getResources().getAssets().open(filepath);
			else
				in = new FileInputStream(filepath);
			/** 获取文件的字节数 **/
			int lenght = in.available();
			data = new byte[lenght];
			/** 将文件中的数据读到byte数组中 **/
			in.read(data);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return data;
	}
	/**
	 * 数组拷贝
	 * 
	 * @param src
	 *            源数组
	 * @param pos
	 *            源数组起始定位
	 * @param dst
	 *            目标数组
	 * @param len
	 *            拷贝长度
	 * 
	 * @return
	 */
	public static int arraycopy(byte[] src, int pos, byte dst[], int len) {
		try {
			if (src.length >= pos + len) {
				System.arraycopy(src, pos, dst, 0, len);
				return len;
			}
		} catch (Exception e) {
		}
		return -1;
	}

}
