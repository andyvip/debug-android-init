package com.jrdcom.example.layout;

import android.graphics.RectF;
import android.util.Log;
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
public class TemplateLayout extends Layout<TemplateLayoutItem> {

    PuzzleFormatPath mTemplateLayoutReader = new PuzzleFormatPath();
    RectF lRect ;

	/**
	 * 唯一构造函数。
	 *
	 * @param pInputStreamOpener
	 */
	public TemplateLayout(IInputStreamOpener pInputStreamOpener) {
		super(pInputStreamOpener);
	}

	@Override
	public void load() {
		// 已经加载后，拼图版式节点数目不等于0，可以不用二次加载
		if (null == this.mInputStreamOpener || this.mLayoutItemEntities.size() != 0) return;

        try {

            this.mTemplateLayoutReader.ReadPuzzleFromatInputStream(this.mInputStreamOpener.open());

            // 获取设计稿的宽度跟高度

            this.mWidth = this.mTemplateLayoutReader.getOrgWidth();
            this.mHeight = this.mTemplateLayoutReader.getOrgHeight();

            this.mTemplateLayoutReader.resizePuzzle(this.mWidth, this.mHeight);
//            this.mTemplateLayoutReader.resizePuzzle(TemplateTool.resetWidth, TemplateTool.resetHeight);
            int lCount = this.mTemplateLayoutReader.GetFormatPathCount();

            for (int i = 0; i < lCount; i++) {

                TemplateLayoutItem lItem = new TemplateLayoutItem();
                lRect = this.mTemplateLayoutReader.GetFormatMaxRectAtIndex(i);

                lItem.setWidth(lRect.width());
                lItem.setHeight(lRect.height());
                lItem.setPosition(lRect.left, lRect.top);
                lItem.setRotation(this.mTemplateLayoutReader.GetFormatRotateAtIndex(i));
                lItem.setPath(this.mTemplateLayoutReader.GetFormatBezierPathAtIndex(i));
                lItem.setAbsPath(this.mTemplateLayoutReader.getAbsoluteFormatBezierPathByIndex(i));
                lItem.setOrgRect(this.mTemplateLayoutReader.getOrgMaxRectAtIndex(i));

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
			TemplateLayoutItem layoutItem = this.getItem(i);
			layoutItem.setScreenPath(null);
			layoutItem.setScreenAbsPath(null);
		}
	}
}
