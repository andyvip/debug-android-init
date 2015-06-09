package com.jrdcom.example.joinpic;

import java.util.ArrayList;

import com.mt.mtxx.image.JNI;

/**
 * 拼图数据模型基类
 * <p/>
 * User: Javan.Eu
 * Date: 12-10-18
 * Time: 上午9:25
 */
public abstract class PuzzleModel implements IModelSaver {

	/**节点图片数据的路径**/
	protected ArrayList<String> mListImagePath=new ArrayList<String>();
	
	/**
	 * JNI对象，所有的NDK底层处理接口都在这里
	 */
	protected JNI m_jni=null;
	
	/**
	 * 标记当前的model是否已经启动过了。
	 */
	protected boolean mHadStarted = false;
	
	/**
	 * NDK底层拼图临时数据存放位置。
	 */
	protected String mPuzzleTmpFilePath;
	

	/**拼图初始化方法
	 * @param jni JNI对象，Android应用层与NDK底层的桥梁
	 */
	public void init(JNI jni)
	{
		this.m_jni=jni;
	}
	
	/**设置NDK底层节点图片缓存路径
	 * @param tempPath
	 */
	public void setNDKPuzzleTempPath(String tempPath)
	{
		this.mPuzzleTmpFilePath=tempPath;
	}
	
	/**清除拼图底层数据**/
	public void clearAllPuzzleData() {
		if(m_jni!=null)
			this.m_jni.PuzzleClearMemoryAll();
	}
	public void setImagePathList(ArrayList<String > childPathList)
	{
		mListImagePath.clear();
		for(int index=0;index<childPathList.size();index++)
		{
			this.mListImagePath.add(index, childPathList.get(index));
		}
		
	}

	/**
	 * Save model data to path。
	 *
	 * @param pPath 文件路劲
	 * @return 成功返回true。失败返回false。
	 */
	protected abstract boolean saveDataToPath(String pPath);
	
	@Override
	public boolean saveImageToPath(String pPath) {
		boolean lRes = saveDataToPath(pPath);
		if(lRes==true)
			clearAllPuzzleData();
		return lRes;
	}
}