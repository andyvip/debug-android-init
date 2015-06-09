package com.jrdcom.example.joinpic;

import java.util.ArrayList;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.jrdcom.example.layout.TemplateLayout;
import com.jrdcom.mt.util.MyData;
import com.jrdcom.mt.core.BitmapUtil;
import com.jrdcom.mt.mtxx.tools.PuzzleFrameLoader;
import static com.jrdcom.example.joinpic.JointPuzzleLayoutView.MarginTOP_BOTTOM;

public class JointPuzzleModel extends PuzzleModel{

    
    public static final int JOINT_TEMPLATE_WIDTH = 480;
	/**
	 * 边框与底纹加载类
	 */
	private PuzzleFrameLoader mFrameloader=null;
	
	/**拼图区域的尺寸*/
	private int mWidth=640;
	private int mHeight=0;
	
	/**图片拼接中接点图片之间的间距**/
	int mGapSize=10;

	public JointPuzzleModel()
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
	 *初始化边框，生成底纹与边框合成的图片文件，与确定保存尺寸
	 *每次改变图片大小，或者是切换边框与底纹时都要调用这个方法
	 */
	public void initFrame(String strBiankuang, String strDiwen) {
		int lImageCount=this.mListImagePath.size();
		/**预读图片的尺寸*/
		BitmapFactory.Options options = new BitmapFactory.Options();
		/** 不真正的进行读 **/
		options.inJustDecodeBounds = true;
		mHeight=0;
		for(int i=0;i<lImageCount;i++)
		{
			Bitmap bmp = BitmapFactory.decodeFile(this.mListImagePath.get(i), options);
			int realWidth = options.outWidth;
			int realHeight = options.outHeight;
			int preHeight=(int)(realHeight*(mWidth/(realWidth*1.0f)));
			mHeight+=preHeight;//累加图片的大小
		}
		mHeight+=mGapSize*(lImageCount-1);//还要加上比节点少一个的间隔高度
		mHeight+=MarginTOP_BOTTOM*2;
		if(mFrameloader==null)
			mFrameloader=new PuzzleFrameLoader(m_jni);
		mFrameloader.initFrame(strBiankuang, strDiwen, mWidth, mHeight);
	}
	
	public void setWidth(int w)
	{
	    this.mWidth = w;
	}
	
	public void resetPuzzleSize(int w,int h)
	{
	    mWidth = w;
	    mHeight = h;
	    if(mFrameloader!=null)
	        mFrameloader.resetPuzzleSize(w,h);
	}
	public  ArrayList<Bitmap >  getFrameTexture()
	{
	    if(mFrameloader!=null)
	    {
	       return  mFrameloader.getFrameBmp(mWidth,mHeight);
	    }
	    return null;
	}
	@Override
	protected boolean saveDataToPath(String pPath) {
		int lImageCount=this.mListImagePath.size();
		int[] lIds = new int[lImageCount];
		boolean lRes = true;
		m_jni.PuzzleStartWithTempFileSavePath(this.mPuzzleTmpFilePath,3);
		for (int i = 0; i < lImageCount; i++) {
			Bitmap lResBitmap=BitmapUtil.loadBitmapFromSDcard(this.mListImagePath.get(i), true);
			if (lResBitmap == null) {
				lRes = false;
				break;
			}

			if (m_jni.PuzzleInsertNodeImage(i, lResBitmap)==0) {
                //PR630752-tao li-001
                int[] lData = BitmapUtil.createIntARGBBitmap(lResBitmap);
                if (lData == null) {
                    throw new NullPointerException("Create byte Bitmap fail. in FreePuzzleModel.saveImageToPath");
                }
                m_jni.PuzzleInsertNodeImageData(i, lData, lResBitmap.getWidth(), lResBitmap.getHeight());
                lRes = true;
                //PR630752-tao li-001
               }
			// 组装模版拼图版式ID数组
			lIds[i] = i;
		}

		if (lRes) {
			lRes = m_jni.PuzzleJointSaveToSD(pPath, lIds);
		}

		// 清除底层数据缓存
		m_jni.PuzzleClearMemory();

		return lRes;
	}

}
