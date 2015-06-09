package com.jrdcom.mt.core;

import java.io.File;

import android.os.Environment;

import com.mt.mtxx.image.JNI;

/**
 * 图片缓冲区域
 * 
 * @author aidy
 * 
 */
public class ImageDiskCache {
	public String m_strSavePath;
	private final static String mTempPath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/Test";

	/*
	 * 创建临时文件夹，folerName为文件夹名称
	 */
	public void createCacheFolder(String folerName) {

		File file = new File(mTempPath);
		if (!file.exists()) {
			file.mkdirs();
		}
	    //创建的临时文件夹为隐藏文件，所以加.标志
		m_strSavePath = mTempPath + "/." + folerName;// 临时文件保存路径为mtxx/.temp
		MTFile.createPath(m_strSavePath);
	}

    /**
     * 缓存图片数据到本地硬盘
     * @param jni           NDK与JAVA的链接工具
     * @param strName       缓存到本地硬盘的绝对路径
     * @param nType         0为显示的图片数据，1为真实的图片数据。2为功能内操作的显示数据，3为功能内操作的真实数据
     */
	public void saveCurrentImageToDisk(JNI jni, String strName, int nType) {
		String strPath = m_strSavePath + "/" + strName;
		jni.saveImageDataToDisk(strPath, nType);
	}

    /**
     * 从本地硬盘加载已经缓存的图片数据文件
     * @param jni               NDK与JAVA的链接工具
     * @param strName          已经缓存到本地硬盘的文件的绝对路径
     * @param nType            0为显示的图片数据，1为真实的图片数据。2为功能内操作的显示数据，3为功能内操作的真实数据
     */
	public void loadCurrentImageFromDisk(JNI jni, String strName, int nType) {
		String strPath = m_strSavePath + "/" + strName;
		jni.loadImageDataFromDisk(strPath, nType);
	}

	/*
	 * 清除文件夹
	 */
	public void clear() {
		MTFile.delFolder(m_strSavePath);
	}
}
