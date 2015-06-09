package com.jrdcom.example.layout;

import android.graphics.Matrix;
import android.graphics.Path;
import android.graphics.RectF;

import com.jrdcom.example.layout.LayoutItemEntity;

/**
 * 模版拼图节点数据保存类
 * User: Javan.Eu
 * Date: 12-9-27
 * Time: 下午4:53
 */
public class TemplateLayoutItem extends LayoutItemEntity {
	protected float mWidth;
	protected float mHeight;
	private RectF mOrgRect;//原版式里面的格子(与大小 无关相对于整体的0~1.0)
	private Path mPath;//相对每单个格子的path
	private Path mScreenPath;//基于整体的path
	private Path absPath;//基于整体的path
	private Path mScreenAbsPath = null;

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
	
	public void setOrgRect(RectF rect)
	{
		this.mOrgRect=rect;
	}
	
	public RectF getOrgRect()
	{
		return mOrgRect;
	}

	/**
	 * 获取屏幕上节点宽度
	 * @return    返回模版拼图节点数据在屏幕上的宽度
	 */
	public float getScreenWidth() {
		return this.mWidth*this.mScreenScale;
	}

	/**
	 * 获取节点屏幕上的高度
	 * @return   返回相对屏幕的高度值
	 */
	public float getScreenHeight() {
		return this.mHeight * this.mScreenScale;
	}

	/**
	 * 获取
	 * @return
	 */
	public Path getPath() {
		return mPath;
	}

	public void setPath(Path mPath) {
		this.mPath = mPath;
	}
	
	public Path getScreenPath() {
		if (mScreenPath == null) {
			mScreenPath = new Path(mPath);
			Matrix matrix = new Matrix();
			matrix.setScale(mScreenScale, mScreenScale);
			mScreenPath.transform(matrix);
		}

		return mScreenPath;
	}

	public void setScreenPath(Path path) {
		this.mScreenPath = path;
	}

	public void setAbsPath(Path path) {
		this.absPath = path;
	}
	
	public Path getAbsPath(){
		return absPath;
	}
	
	public Path getScreenAbsPath() {
		if (this.mScreenAbsPath == null) {
			mScreenAbsPath = new Path(absPath);
			Matrix matrix = new Matrix();
			matrix.setScale(mScreenScale, mScreenScale);
			mScreenAbsPath.transform(matrix);
		}
		return mScreenAbsPath;
	}

	public void setScreenAbsPath(Path pPath) {
		this.mScreenAbsPath = pPath;
	}
}
