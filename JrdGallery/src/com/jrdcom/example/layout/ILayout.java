package com.jrdcom.example.layout;

import android.view.View;
import com.jrdcom.example.joinpic.IPuzzleFrame;
import com.jrdcom.example.layout.ILayoutItemEntity;

/**
 * User: Javan.Eu
 * Date: 12-9-27
 * Time: 下午4:35
 */
public interface ILayout<T extends ILayoutItemEntity> {
	public void load();

	public T getItem(int pIndex);
	public int size();

	public int getWidth();
	public int getHeight();

	/**
	 * 获取版式映射到屏幕上的坐标X
	 * @return  返回坐标数值
	 */
	public float getScreenX();

	/**
	 * 获取拼图版式映射到屏幕上的坐标Y
	 *
	 * @return 返回坐标数值
	 */
	public float getScreenY();

	/**
	 * 获取版式在屏幕放缩后的宽度。
	 * @return 宽度值
	 */
	public float getScreenWidth();

	/**
	 * 返回够的高度
	 * @return  高度值
	 */
	public float getScreenHeight();

	/**
	 * 设置与模版一同工作的拼图边框对象
	 * @param pFrame 边框对象
	 */
	public void setPuzzleFrame(IPuzzleFrame pFrame);

	/**
	 * 获取当前模版一同工作的边框对象
	 * @return 返回一同工作的边框对象
	 */
	public IPuzzleFrame getPuzzleFrame();

	/**
	 * 更新拼图版式中的节点根据屏幕的真实可展示区域
	 * @param pView 用来展示版式数据的View
	 * @param pWidth  View的新宽度
	 * @param pHeight  View的新高度
	 */
	public void updateLayoutOnScreenSizeChanged(View pView, int pWidth, int pHeight);
}
