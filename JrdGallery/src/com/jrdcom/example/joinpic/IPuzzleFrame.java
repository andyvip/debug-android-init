package com.jrdcom.example.joinpic;

import android.graphics.Bitmap;
import android.graphics.RectF;

import java.util.ArrayList;

import com.jrdcom.example.layout.ILayout;

/**
 *
 * 定义拼图边框素材行为定义类
 *
 * User: Javan.Eu
 * Date: 12-10-19
 * Time: 下午4:48
 */
public interface IPuzzleFrame {
	/**
	 * 获取边框与设计版式的X轴偏移
	 * @return 具体数值
	 */
	public int getOffsetX();

	/**
	 * 获取边框与设计版式的Y轴偏移
	 * @return  具体数值
	 */
	public int getOffsetY();


	/**
	 * 设置边框在屏幕上展示区域
	 * @param rect
	 */
	public void setScreenRect(RectF rect);

	/**
	 * 获取屏幕展示区域
	 * @return  拼图边框展示的屏幕坐标
	 */
	public RectF getScreenRect();

	public void setScreenScale(float pScale);
	public float getScreenScale();
	
	/**
	 * 获取生成底图所需的边框图片与底纹
	 * @return 全部的bitmapList
	 */
	public ArrayList<Bitmap> getFrameBmp();
	
	
	/**
	 * 获取生成底图所需的边缘阴影图片
	 * @return 全部的bitmapList
	 */
	public ArrayList<Bitmap> getCoverBmp();
	
	/**
	 * 
	 * @param pLayout
	 */
	public void initWithLayout(ILayout pLayout);
	
}
