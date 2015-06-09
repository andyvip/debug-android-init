package com.jrdcom.example.layout;

import com.jrdcom.example.layout.LayoutItemEntity;

/**
 * 模版拼图节点数据保存类 User: Javan.Eu Date: 12-9-27 Time: 下午4:53
 */
public class FreePuzzleLayoutItem extends LayoutItemEntity {
	protected float mWidth;
	protected float mHeight;

	public float getWidth() {
		return mWidth;
	}

	public void setWidth(float pWidth) {
		this.mWidth = pWidth;
	}

	public float getHeight() {
		return mHeight;
	}

	public void setHeight(float pHeight) {
		this.mHeight = pHeight;
	}

	/**
	 * 获取屏幕上节点宽度
	 * 
	 * @return 返回模版拼图节点数据在屏幕上的宽度
	 */
	public float getScreenWidth() {
		return this.mWidth * this.mScreenScale;
	}

	/**
	 * 获取节点屏幕上的高度
	 * 
	 * @return 返回相对屏幕的高度值
	 */
	public float getScreenHeight() {
		return this.mHeight * this.mScreenScale;
	}
	
}
