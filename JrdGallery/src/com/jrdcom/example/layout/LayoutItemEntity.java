package com.jrdcom.example.layout;

/**
 * 拼图版式节点对象
 * User: Javan.Eu
 * Date: 12-9-27
 * Time: 下午4:41
 */
public abstract class LayoutItemEntity implements ILayoutItemEntity {


    protected float mX;
	protected float mY;

	protected float mRotation = 0;

	protected float mScaleX = 1;
	protected float mScaleY = 1;

	protected float mScreenScale = 1;

	protected float mScaleCenterX = 0;
	protected float mScaleCenterY = 0;

	public LayoutItemEntity() {
		this(0, 0);
	}
	
	public LayoutItemEntity(float pX, float pY) {
        this.mX = pX;
        this.mY = pY;
    }
	
	@Override
	public float getX() {
		return this.mX;
	}

	@Override
	public float getY() {
		return this.mY;
	}

	@Override
	public void setX(final float pX) {
		this.mX = pX;
	}

	@Override
	public void setY(final float pY) {
		this.mY = pY;
	}

	@Override
	public void setPosition(final float pX, final float pY) {
		this.mX = pX;
		this.mY = pY;
	}

	@Override
	public boolean isScaled() {
		return (this.mScaleX != 1) || (this.mScaleY != 1);
	}

	@Override
	public float getScaleX() {
		return this.mScaleX;
	}

	@Override
	public float getScaleY() {
		return this.mScaleY;
	}

	@Override
	public void setScaleX(final float pScaleX) {
		this.mScaleX = pScaleX;
	}

	@Override
	public void setScaleY(final float pScaleY) {
		this.mScaleY = pScaleY;
	}

	@Override
	public void setScale(final float pScale) {
		this.mScaleX = pScale;
		this.mScaleY = pScale;
	}

	@Override
	public void setScale(final float pScaleX, final float pScaleY) {
		this.mScaleX = pScaleX;
		this.mScaleY = pScaleY;
	}


	@Override
	public boolean isRotated() {
		return this.mRotation != 0;
	}

	@Override
	public float getRotation() {
		return this.mRotation;
	}

	@Override
	public void setRotation(final float pRotation) {
		this.mRotation = pRotation;
	}

	@Override
	public void setScreenScale(float pScreenScale) {
		this.mScreenScale = pScreenScale;
	}

	@Override
	public float getScreenScale() {
		return this.mScreenScale;
	}

	@Override
	public float getScreenX() {
		return this.mX * this.mScreenScale;
	}

	@Override
	public float getScreenY() {
		return this.mY * this.mScreenScale;
	}

	@Override
	public float getScaleCenterX() {
		return this.mScaleCenterX;
	}

	@Override
	public float getScaleCenterY() {
		return this.mScaleCenterY;
	}

	@Override
	public void setScaleCenterX(final float pScaleCenterX) {
		this.mScaleCenterX = pScaleCenterX;
	}

	@Override
	public void setScaleCenterY(final float pScaleCenterY) {
		this.mScaleCenterY = pScaleCenterY;
	}

	@Override
	public void setScaleCenter(final float pScaleCenterX, final float pScaleCenterY) {
		this.mScaleCenterX = pScaleCenterX;
		this.mScaleCenterY = pScaleCenterY;
	}

	/**
	 * 获取拼图缩放中心点位置 x 方向上的
	 * @return   中心点x缩放后的数值，
	 */
	public float getScreenScaleCenterX() {
		return this.mScaleCenterX * this.mScreenScale;
	}

	/**
	 * 获取拼图缩放中心点位置 y 方向上的
	 * @return   中心点y缩放后的数值，
	 */
	public float getScreenScaleCenterY() {
		return this.mScaleCenterY * this.mScreenScale;
	}
}
