package com.jrdcom.example.layout;

import android.graphics.RectF;
import android.view.View;

import java.io.IOException;

import com.jrdcom.mt.util.IInputStreamOpener;
import com.jrdcom.mt.mtxx.tools.PuzzleFormatPath;

/**
 * 模版拼图版式数据存储对象
 * User: Javan.Eu
 * Date: 12-9-27
 * Time: 下午4:29
 */
public class PosterLayout extends Layout<PosterLayoutItem> {

    private PuzzleFormatPath mPosterLayoutReader = new PuzzleFormatPath();

	/**
	 * 唯一构造函数。
	 *
	 * @param pInputStreamOpener
	 */
	public PosterLayout(IInputStreamOpener pInputStreamOpener) {
		super(pInputStreamOpener);
	}

	@Override
	public void load() {
		// 已经加载后，拼图版式节点数目不等于0，可以不用二次加载
		if (null == this.mInputStreamOpener || this.mLayoutItemEntities.size() != 0) return;

        try {
        	
        	//海报拼图的保存大小.
        	int nDestWidth = 640;
        	int nDestHeight = 896;
            this.mPosterLayoutReader.ReadPuzzleFromatInputStream(this.mInputStreamOpener.open(),nDestWidth,nDestHeight);

            // 获取设计稿的宽度跟高度
//            this.mWidth = this.mPosterLayoutReader.getOrgWidth();
//            this.mHeight = this.mPosterLayoutReader.getOrgHeight();
            this.mWidth = this.mPosterLayoutReader.getDstWidth();
            this.mHeight = this.mPosterLayoutReader.getDstHeight();

//            this.mPosterLayoutReader.resizePuzzle(this.mWidth, this.mHeight);
            
            int lCount = this.mPosterLayoutReader.GetFormatPathCount();
            for (int i = 0; i < lCount; i++) {

                PosterLayoutItem lItem = new PosterLayoutItem();
                
                RectF lRect = this.mPosterLayoutReader.GetFormatMaxRectAtIndex(i);
                lItem.setWidth(lRect.width());
                lItem.setHeight(lRect.height());
                lItem.setPosition(lRect.left, lRect.top);
                lItem.setRotation(this.mPosterLayoutReader.GetFormatRotateAtIndex(i));
                lItem.setPath(this.mPosterLayoutReader.GetFormatBezierPathAtIndex(i));
                lItem.setAbsPath(this.mPosterLayoutReader.getAbsoluteFormatBezierPathByIndex(i));
                lItem.setOrgRect(this.mPosterLayoutReader.getOrgMaxRectAtIndex(i));
                
                this.mLayoutItemEntities.add(lItem);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

	@Override
	public void updateLayoutOnScreenSizeChanged(View pView, int pWidth, int pHeight) {
		super.updateLayoutOnScreenSizeChanged(pView, pWidth, pHeight);

		// 清除所有旧的数据
		int lCount = this.size();
		for (int i = 0; i < lCount; i++) {
			PosterLayoutItem layoutItem = this.getItem(i);
			layoutItem.setScreenPath(null);
			layoutItem.setScreenAbsPath(null);
		}
//      delete by Javan.Eu 此处加载过于卡。效率低下影响用户体验。
//		// 更新版式的大小后同时自动更新里面海报样式加载者的数据
//		this.mStyleLoader.setPosterWidthAndHeight(Math.round(this.getScreenWidth()),
//				Math.round(this.getScreenHeight()));
//		this.mStyleLoader.reload();
	}
	
}
