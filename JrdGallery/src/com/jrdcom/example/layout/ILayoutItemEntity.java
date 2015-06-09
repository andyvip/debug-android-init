package com.jrdcom.example.layout;

/**
 * User: Javan.Eu
 * Date: 12-9-27
 * Time: 下午4:38
 */
public interface ILayoutItemEntity {

	/**
	 * 设置版式节点的设计x轴位置
	 * @param pX
	 */
	public void setX(final float pX);
	public void setY(final float pY);
	public float getX();
	public float getY();

	public void setPosition(final float pX, final float pY);

	public boolean isScaled();
	public float getScaleX();
	public float getScaleY();
	public void setScaleX(final float pScaleX);
	public void setScaleY(final float pScaleY);
	public void setScale(final float pScale);
	public void setScale(final float pScaleX, final float pScaleY);

	public float getScaleCenterX();
	public float getScaleCenterY();
	public void setScaleCenterX(final float pScaleCenterX);
	public void setScaleCenterY(final float pScaleCenterY);
	public void setScaleCenter(final float pScaleCenterX, final float pScaleCenterY);

	public boolean isRotated();
	public float getRotation();
	public void setRotation(final float pRotation);

	/**
	 * 设置版式节点各个坐标参数设计稿与实际屏幕之间的放缩值
	 * 默认值为1.0表示节点坐标与设计稿相同，如果在大于设计稿的机器屏幕上 pScreenScale 大于1.0 ；小于设计稿的机器上应该小于 1.0
	 * @param pScreenScale
	 */
	public void setScreenScale(final float pScreenScale);

	/**
	 * 获取实际与设计稿之间的放缩值
	 * @return
	 */
	public float getScreenScale();

	/**
	 * 获取屏幕上的x位置
	 * @return  返回屏幕上位置的X轴坐标
	 */
	public float getScreenX();

	/**
	 * 获取屏幕上的y位置
	 * @return  返回屏幕上位置的Y轴坐标
	 */
	public float getScreenY();

}
