package com.jrdcom.example.layout;

import android.graphics.Matrix;
import android.graphics.RectF;
import android.view.View;
import com.jrdcom.example.joinpic.IPuzzleFrame;
import com.jrdcom.mt.util.IInputStreamOpener;

import java.util.ArrayList;

/**
 * 拼图版式基础类
 * User: Javan.Eu
 * Date: 12-9-27
 * Time: 下午4:47
 */
public abstract class Layout<T extends ILayoutItemEntity> implements ILayout<T> {
	// ===========================================================
	// Fields
	// ===========================================================

	/**
	 * 屏幕上拼图展示的X，Y偏移坐标
	 */
	protected float mScreenX = 0.0f;
	protected float mScreenY = 0.0f;

	/**
	 * 版式设计时候的宽度
	 */
	protected int mWidth = 0;

	/**
	 * 版式设计时候的高度
	 */
	protected int mHeight = 0;

	/**
	 * 相对屏幕的放缩值
	 */
	protected float mScreenScale = 1.0f;

	/**
	 * 拼图边框对象
	 */
	private IPuzzleFrame mPuzzleFrame = null;

	/**
	 * 布局节点存放列表
	 */
	protected final ArrayList<T> mLayoutItemEntities = new ArrayList<T>(9);

	protected final IInputStreamOpener mInputStreamOpener;

	/**
	 * 唯一构造函数。
	 *
	 * @param pInputStreamOpener
	 */
	public Layout(IInputStreamOpener pInputStreamOpener) {
		this.mInputStreamOpener = pInputStreamOpener;
	}

	@Override
	public void load() {
	}

	@Override
	public T getItem(int pIndex) {
		return this.mLayoutItemEntities.get(pIndex);
	}

	@Override
	public int size() {
		return this.mLayoutItemEntities.size();
	}

	@Override
	public int getWidth() {
		return this.mWidth;
	}

	@Override
	public int getHeight() {
		return this.mHeight;
	}

	@Override
	public float getScreenX() {
		return this.mScreenX;
	}

	@Override
	public float getScreenY() {
		return this.mScreenY;
	}

	@Override
	public float getScreenHeight() {
		return this.mHeight * this.mScreenScale;
	}

	@Override
	public float getScreenWidth() {
		return this.mWidth * this.mScreenScale;
	}

	@Override
	public IPuzzleFrame getPuzzleFrame() {
		return this.mPuzzleFrame;
	}

	@Override
	public void setPuzzleFrame(IPuzzleFrame pFrame) {
		if (pFrame == null)
			return;

		this.mPuzzleFrame = pFrame;
		this.mPuzzleFrame.initWithLayout(this);
	}

	@Override
	public void updateLayoutOnScreenSizeChanged(View pView, int pWidth, int pHeight) {

		RectF dst = new RectF(pView.getPaddingLeft(),
							pView.getPaddingTop(),
							pWidth - pView.getPaddingRight(),
							pHeight - pView.getPaddingBottom() );

		RectF src = null;
		if (this.mPuzzleFrame != null) {
			//  需要对区域进行缩小，把边框的部分不考虑计算
			src = new RectF(0,0,
					this.mWidth + (this.mPuzzleFrame.getOffsetX()<<1),
					this.mHeight + (this.mPuzzleFrame.getOffsetY()<<1));
		} else {
			src = new RectF(0,0,this.mWidth, this.mHeight);
		}

		Matrix matrix = new Matrix();
		boolean lRes = matrix.setRectToRect(src, dst, Matrix.ScaleToFit.CENTER);
		if (lRes) {
			// 计算变换矩阵成功，更新各个节点的位置坐标
			float[] lValues = new float[9];
			matrix.getValues(lValues);

			if (this.mPuzzleFrame != null) {
				RectF lFrameRect = new RectF(src);
				matrix.mapRect(lFrameRect);
				this.mPuzzleFrame.setScreenRect(lFrameRect);
				this.mPuzzleFrame.setScreenScale(lValues[Matrix.MSCALE_X]);
				src.inset(this.mPuzzleFrame.getOffsetX(), this.mPuzzleFrame.getOffsetY());
			} else {

			}

			matrix.mapRect(src);

			this.mScreenX = Math.round(src.left) ;
			this.mScreenY = Math.round(src.top);
			this.mScreenScale = lValues[Matrix.MSCALE_X];
			for (ILayoutItemEntity item : this.mLayoutItemEntities) {
				// 为只节点添加缩放比例
				item.setScreenScale(mScreenScale);
			}
		} else {
			// do nothing.
		}

	}

	public float getScreenScale() {
		return this.mScreenScale;
	}
}
