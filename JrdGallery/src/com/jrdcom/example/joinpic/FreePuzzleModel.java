
package com.jrdcom.example.joinpic;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;
import com.jrdcom.example.joinpic.FreePuzzleLayoutView;
import com.jrdcom.example.layout.FreePuzzleLayout;
import com.jrdcom.example.layout.FreePuzzleLayoutItem;
import com.jrdcom.example.layout.ILayout;
import com.jrdcom.mt.util.AssetInputStreamOpener;
import com.jrdcom.mt.util.IInputStreamOpener;
import com.jrdcom.mt.util.MyData;
import com.jrdcom.mt.core.BitmapUtil;
import com.mt.mtxx.image.JNI;

import java.io.*;
import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.List;

public class FreePuzzleModel extends PuzzleModel {
    /**
     * 背景图片Bitmap对象
     */
    private Bitmap mBackgroundBitmap = null;
    private FreePuzzleLayoutView freePuzzlelayoutview;
    public int[] mLIds;
    /**
     * 当前使用的自由拼图版式对豄1�7
     */
    private FreePuzzleLayout mFreePuzzleLayout = null;

    public FreePuzzleModel() {
    }

    public FreePuzzleLayout getFreePuzzleLayoutObject()
    {
        return mFreePuzzleLayout;
    }

    /**
     * 设置模版拼图当前使用板式文件
     * 
     * @param pContext 当前的操作的Context对象
     * @param pLayoutFilePath 要加载的版式文件路
     * @param pIsFromSDCard 标记当前加载的版式文件是否来自程序外部存储或者是来自Assets
     */
    public void setFreePuzzleLayout(Context pContext, final String pLayoutFilePath,
            boolean pIsFromSDCard) {
        if (!pIsFromSDCard) {
            // 从Asset文件夹中加载
            this.setFreePuzzleLayout(new AssetInputStreamOpener(pContext.getAssets(),
                    pLayoutFilePath));
        } else {
            // 从SD卡中加载
            this.setFreePuzzleLayout(new IInputStreamOpener() {
                @Override
                public InputStream open() throws IOException {
                    return new BufferedInputStream(new FileInputStream(new File(pLayoutFilePath)));
                }
            });
        }
    }

    /**
     * @param pInputStreamOpener
     */
    private void setFreePuzzleLayout(final IInputStreamOpener pInputStreamOpener) {
        this.mFreePuzzleLayout = null;
        this.mFreePuzzleLayout = new FreePuzzleLayout(pInputStreamOpener);

        // 解析设计稿文件中的数据
        this.mFreePuzzleLayout.load();
    }

    /**
     * 设置当前使用的背景数据
     * 
     * @param bgBitmap
     */
    public void setCustomBackgroundBitmap(Bitmap bgBitmap) {
        this.mBackgroundBitmap = bgBitmap;
        if (freePuzzlelayoutview != null)
        {
            freePuzzlelayoutview.setBackgroundBitmap(mBackgroundBitmap);
        }
    }

    public void setLayoutView(FreePuzzleLayoutView freePuzzlelayoutview)
    {
        this.freePuzzlelayoutview = freePuzzlelayoutview;
    }

    /**
     * Save data to path〄1�7
     * 
     * @param pPath 文件路劲
     * @return 成功返回true。失败返回false
     */
    protected boolean saveDataToPath(String pPath) {

        int lImageCount = this.mListImagePath.size();
        /** NDK 底层接口需要为每个节点加一个特定的ID */
//        int[] lIds = new int[lImageCount];

        /** 接点的旋转角度数据 */
        int[] lRotates = new int[lImageCount];

        /** NDK 底层为每个节点添加白边所需要根据界面换算成一个合适的宽度 **/
        int[] lFrameWidth = new int[lImageCount];

        /** 用户操作过程中图片的放缩系数 */
        float[] lScales = new float[lImageCount];

        /** 每个节点中心点在底图中的相对位置 **/
        float[] lCenterPointers = new float[lImageCount << 1];
        boolean lRes = false;

        int lPointerIndex = 0; // 中心点数组当前栈顶位置

        /** 拼图保存大小,设计稿为640*896,可以根据需要改变保存大小，但要进行合适的换算 */
        int lSaveWidth = this.mBackgroundBitmap.getWidth();
        int lSaveHeight = this.mBackgroundBitmap.getHeight();
        /** 为底层设置一个节点图片缓存路径 **/
        m_jni.PuzzleStartWithTempFileSavePath(this.mPuzzleTmpFilePath, 2);

        do {

            /** 将背景图片数据 **/
            m_jni.PuzzleBackGroundInitBitmap(this.mBackgroundBitmap);

            for (int i = 0; i < lImageCount; i++) {

                FreePuzzleLayoutItem lLayoutItem = this.mFreePuzzleLayout.getItem(i);

                Bitmap lBitmap = BitmapUtil.loadBitmapFromSDcard(this.mListImagePath.get(i), true);
                /** 向底层增加图片数据，可以有两种方式，一种是直接传Bitmap对象，另一种是转在Byte流进行传递 **/
                if (m_jni.PuzzleInsertNodeImage(i, lBitmap) == 0)// 尝试传递Bitmap形式，返回0则表示没有成功
                {
                    // 如果获取不到，则用传递byte流数据
                    int[] lData = BitmapUtil.createIntARGBBitmap(lBitmap);
                    if (lData == null) {
                        throw new NullPointerException(
                                "Create byte Bitmap fail. in FreePuzzleModel.saveImageToPath");
                    }
                    /** 传byte流的形式 **/
                    m_jni.PuzzleInsertNodeImageData(i, lData, lBitmap.getWidth(),
                            lBitmap.getHeight());
                }
//                lIds[i] = mLIds[i];
                lRotates[i] = (int) lLayoutItem.getRotation();
                lScales[i] = lLayoutItem.getScaleX();
                lPointerIndex = i << 1;
                lCenterPointers[lPointerIndex++] = (lLayoutItem.getX()) / lSaveWidth;
                lCenterPointers[lPointerIndex] = (lLayoutItem.getY()) / lSaveHeight;
                // 底层的默认边框为在mdpi的机器为 5
                lFrameWidth[i] = 5;
            }

            lRes = true;
        } while (false);

        if (lRes) {
            /** 调用保存接口，传递所需要的参数，底层进行保存 **/
            lRes = m_jni.PuzzleFreeSaveToSDwithFrame(pPath, mLIds, lRotates, lScales,
                    lCenterPointers, lFrameWidth);
        }

        // 清除底层数据缓存
        m_jni.PuzzleClearMemory();

        return lRes;
    }
    
    @Override
    public void setImagePathList(ArrayList<String > childPathList)
    {
        super.setImagePathList(childPathList);
        mLIds = new int[this.mListImagePath.size()];
    }
    
    public void setLids(int m,int id){
        mLIds[m] = id;
    }
}
