package com.jrdcom.example.joinpic;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.Log;

import com.jrdcom.example.layout.TemplateLayout;
import com.jrdcom.example.layout.TemplateLayoutItem;
import com.jrdcom.example.layout.TemplateTool;
import com.jrdcom.mt.core.BitmapUtil;
import com.jrdcom.mt.mtxx.tools.PuzzleFrameLoader;
import com.jrdcom.mt.util.AssetInputStreamOpener;
import com.jrdcom.mt.util.IInputStreamOpener;



public class TemplatePuzzleModel extends PuzzleModel {
	/**
	 * 边框与底纹加载类
	 */
	private PuzzleFrameLoader mFrameloader=null;
	
	/**
	 *图片保存大小,这里定为640*896，具体大小还要看需求
	 */
	private int mWidth=450;
	private int mHeight=630;
	private float mDeltaX[] ;
	private float mDeltaY[];
	private float mScale[] ;
	private float mScaleX[];
	private float mScaleY[];
	private boolean isScale[];
	
	/**
	 * 当前正在使用中的模版拼图版式布局对象
	 */
	private TemplateLayout mTemplateLayout = null;
	
	
	public TemplatePuzzleModel()
	{			
	}
	
	/**设置边框与底纹
	 * @param mCurrentBiankuang  边框文件路径
	 * @param mCurrentDiwen   底纹文件路径
	 */
	public void setStytle(String strBiankuang, String strDiwen) {
		mFrameloader.resetFrame(strBiankuang, strDiwen);
	}
	
	/**
	 * 设置模版拼图当前使用板式文件
	 *
	 * @param pContext        当前的操作的Context对象
	 * @param pLayoutFilePath 要加载的版式文件路
	 * @param pIsFromSDCard   标记当前加载的版式文件是否来自程序外部存储或者是来自Assets
	 */
	public void setTemplatePuzzleLayout(Context pContext, final String pLayoutFilePath, boolean pIsFromSDCard) {
		if (!pIsFromSDCard) {
			//从Asset文件夹中加载
			this.setTemplatePuzzleLayout(new AssetInputStreamOpener(pContext.getAssets(), pLayoutFilePath));
		} else {
			//从SD卡中加载
			this.setTemplatePuzzleLayout(new IInputStreamOpener() {
				@Override
				public InputStream open() throws IOException {
					return new BufferedInputStream(new FileInputStream(new File(pLayoutFilePath)));
				}
			});
		}
	}
	
	/**返回模板拼图
	 * @return
	 */
	public TemplateLayout getTemplateLayout()
	{
		return this.mTemplateLayout;
	}
	

	private void setTemplatePuzzleLayout(final IInputStreamOpener pInputStreamOpener) {
		this.mTemplateLayout = null;
		this.mTemplateLayout = new TemplateLayout(pInputStreamOpener);

		//解析设计稿文件中的数据
		this.mTemplateLayout.load();
	}
	
	/**
	 *初始化边框，生成底纹与边框合成的图片文件，与确定保存尺寸
	 *每次改变图片大小，或者是切换边框与底纹时都要调用这个方法
	 */
	public void initFrame(String strBiankuang, String strDiwen) {
		if(mFrameloader==null)
			mFrameloader=new PuzzleFrameLoader(m_jni);	
		mFrameloader.initFrame(strBiankuang, strDiwen, mWidth, mHeight);
	}
	public ArrayList<Bitmap > getBitmap() {
	    if (null != mFrameloader) {
	        return mFrameloader.getFrameBmp(mWidth, mHeight);
        }
	    else {
            return null;
        }
    }
	
	public void setDeltaX(float deltax,int index) {
        this.mDeltaX[index] = deltax;
    }
    public void setDeltaY(float deltay,int index) {
        this.mDeltaY[index] = deltay;
    }
    public void setScale(float scale, int index) {
        this.mScale[index] = scale;
    }
    public void setScaleX(float scalex, int index) {
        this.mScaleX[index] = scalex;
    }
    public void setScaleY(float scaley, int index) {
        this.mScaleY[index] = scaley;
    }
    public void isScale(boolean isScale, int index) {
        this.isScale[index] = isScale;
    }
	@Override
	protected boolean saveDataToPath(String pPath) {

		int ImageCount=this.mListImagePath.size();
		int[] lIds = new int[this.mListImagePath.size()];

		/**设计稿里面的最大Rect对象**/
		float[] lLayoutItems = new float[ImageCount << 2];
		int[] lRotates = new int[ImageCount];

		Arrays.fill(lRotates, 0);

		int lPosition = 0;  // 用于数组索引
		Rect lMaxRect = new Rect();
		float lResizeScale = .0f;  // 保存着最终用于保存的图片上的坐标换算Scale。
		boolean lRes = true;
		
		/**NDK底层设置缓存路径**/
		m_jni.PuzzleStartWithTempFileSavePath(this.mPuzzleTmpFilePath,1);
	
		for (int i = 0; i < ImageCount; i++) {
			/**从Sd卡中读取图片数据**/
			Bitmap lResBitmap =BitmapUtil.loadBitmapFromSDcard(this.mListImagePath.get(i), true); // 保存最后用于保存的图片
			
			/**节点数据对象*/
			TemplateLayoutItem lLayoutItem = this.mTemplateLayout.getItem(i);
			
			float xScale=lLayoutItem.getWidth()/lResBitmap.getWidth();
			float yScale=lLayoutItem.getHeight()/lResBitmap.getHeight();
			lResizeScale=xScale>yScale?xScale:yScale;
			lResizeScale = mScale[i];

			// 模版拼图框选中实际图片的区域位置
			lMaxRect.left = Math.round(lLayoutItem.getX()*TemplateTool.mPuzzleScale);
			lMaxRect.top = Math.round(Math.abs(lLayoutItem.getY())*TemplateTool.mPuzzleScaleY);
			lMaxRect.right = Math.round((lLayoutItem.getX() + lLayoutItem.getWidth())*TemplateTool.mPuzzleScale);
			lMaxRect.bottom = Math.round((lLayoutItem.getY() + lLayoutItem.getHeight())*TemplateTool.mPuzzleScaleY);
			/**获取屏幕展示区域的Path*/
            Path path = new Path(lLayoutItem.getPath());
            Matrix pathMatrix = new Matrix();
            pathMatrix.setScale(TemplateTool.mPuzzleScale, TemplateTool.mPuzzleScaleY);
            path.transform(pathMatrix);

            /**对图片的一些操作，海报拼图还要进行旋转操作**/
            Matrix matrix = new Matrix();
            matrix.preTranslate(mDeltaX[i],mDeltaY[i]);
            if (isScale[i]) {
                matrix.postScale(lResizeScale, lResizeScale,mScaleX[i],mScaleY[i]);
            }else {
                matrix.postScale(lResizeScale, lResizeScale,0,0);
            }
//			matrix.postTranslate(mDeltaX[i], mDeltaY[i]);

			/**获取每个节点里面path区域显示的图片*/
			Bitmap dstBitmap = BitmapUtil.clipBitmapByPath(lResBitmap, lMaxRect, path, matrix, false);

			dstBitmap = BitmapUtil.Change2ARGB8888(dstBitmap, true);
			m_jni.PuzzleInsertNodeImage(i, dstBitmap);

			dstBitmap.recycle();

			// 组装模版拼图版式ID数组
			lIds[i] = i;
			lLayoutItems[lPosition++] = lLayoutItem.getOrgRect().left;
			lLayoutItems[lPosition++] = lLayoutItem.getOrgRect().top;
			lLayoutItems[lPosition++] = lLayoutItem.getOrgRect().right;
			lLayoutItems[lPosition++] = lLayoutItem.getOrgRect().bottom;

			matrix.reset();
			path.reset();
			lMaxRect.setEmpty();
		}

		if (lRes) {
			// 如果前面初始化底层数据正常，执行保存。
			lRes = m_jni.puzzleIrregularSaveToSD(pPath, lIds, lLayoutItems);
		}

		// 清除底层数据缓存
		m_jni.PuzzleClearMemory();

		return lRes;
	}

	@Override
	public void setImagePathList(ArrayList<String > childPathList)
	{
	    super.setImagePathList(childPathList);
	    mDeltaX = new float[this.mListImagePath.size()];
	    mDeltaY = new float[this.mListImagePath.size()];
	    mScale = new float[this.mListImagePath.size()];
	    mScaleX = new float[this.mListImagePath.size()];
	    mScaleY = new float[this.mListImagePath.size()];
	    isScale = new boolean[this.mListImagePath.size()];
	}
	public void setFrameWidthAndHeight(int width,int height)
	{
		mWidth = width;
		mHeight = height;
	}
}
