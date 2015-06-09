package com.mt.mtxx.image;

public class GIFJNI 
{
	/**
	 * GIF保存的第一步，初始化函数
	 * @param path			GIF保存的绝对路径
	 * @param count			GIF的帧数
	 * @param w				GIF每一帧的宽
	 * @param h				GIF每一帧的高
	 * @param timeDelay		GIF每一帧的间隔时间。例如：100（1s）
	 */
	public native void SaveGifInit(String path,int count,int w,int h,int timeDelay);

	/**
	 * GIF保存的第二步，添加帧的数据，
	 * @param image			帧的数据
	 * @param id			暂时没用
	 */
	public native void SaveGifAddFrame(int []image,int id);

	/**
	 * GIF保存的第三步，也是最后一步，关闭写IO，保存完成
	 */
	public native void SaveGifFinish();

    static {
        System.loadLibrary("_mt_gif");
    }
}
