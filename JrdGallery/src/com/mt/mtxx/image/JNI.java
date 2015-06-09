package com.mt.mtxx.image;

import android.graphics.Bitmap;

/**
 * 
 * <p>
 * The jni class
 * </p>
 * @author  aidy
 *
 */
public class JNI {
    /**
     * 1.1版     2013/02/17      内核引入Bitmap来操作
     * 1.0版     2012/10/29      基础框架与算法操作
     */
    /**
     * 校验NDK里Bitmap传递进来的ARGB的顺序
     * 1.1版添加
     * @param pImage  校验图片为assets里的ndk_check_color.bmp文件
     * @return        返回0校验失败，返回1校验成功
     */
    public native int NDKCheckColorARGB8888Index(Bitmap pImage);

    /**
     * 设置APK安装在手机里的路径给NDK，在程序初始化的时候必须调用
     * @param path    APK安装在手机里的路径
     * @return         0为设置失败，1为设置成功
     */
	public native int SetAPKPath(String path);

    /**
     * NDK内的图片初始化加载
     * @param path              图片的绝对路径
     * @param ori               为图片的exif方向
     * @param nMaxShowWidth    手机要显示出来的图片的最大宽
     * @param nMaxShowHeight   手机要显示出来的图片的最大高
     * @param nMaxWidth         保存的最大宽
     * @param nMaxHeight        保存的最大高
     * @return                   加载成功返回1，加载失败返回0
     */
	public native int initImageWithPath(String path,int ori,int nMaxShowWidth,int nMaxShowHeight,int nMaxWidth,int nMaxHeight);

    /**
     * 释放控制层的数据内存
     * @return  释放成功返回1，释放失败返回0
     */
	public native int ReleaseControlMemory();
    /**
     *   获取当前步骤显示的图片数据
     * @param imageSize    当前步骤的显示图片的尺寸
     * @return              返回当前步骤显示的图片数据ARGB；如果为NULL，则表示当前不存在
     */
	public native int[] getCurrentShowImageData(int []imageSize);

    /**
     * 获取当前步骤显示的图片
     * @param pImage	当前步骤显示的图片
     * @return			0为获取失败，1为获取成功
     */
    public native int getCurrentShowImage(Bitmap pImage);

    /**
     * 获取当前步骤的UI显示的图片尺寸大小
     * @return      返回当前步骤的UI显示的图片尺寸的大小，如果不存在，则返回0，0
     */
    public native int[] getCurrentShowImageSize();

    /**
     * 获取当前步骤真实的图片数据
     * @param imageSize         当前步骤真实的图片的尺寸
     * @return             返回当前步骤真实图片的图片数据ARGB；如果不存在，则返回NULL
     */
	public native int[] getCurrentImageData(int []imageSize);

    /**
     * 获取当前步骤真实的图片
     * @param pImage        当前步骤真实的图片
     * @return               0为获取失败，1为获取成功
     */
    public native int getCurrentImage(Bitmap pImage);

    /**
     * 获取当前步骤的真实图片的尺寸
     * @return     返回当前步骤的真实图片的尺寸；如果不存在，则返回(0,0)
     */
    public native int[] getCurrentImageSize();
	/**
	 * 获取真实图片的局部数据
	 * @param x			区域的左上角的x坐标
	 * @param y			区域的左上角的y坐标
	 * @param width		区域的宽
	 * @param height	区域的高
	 * @return			返回区域的数据，如果区域超出图片的话，则返回null
	 */
	public native int[] getCurrentImageDataRegion(int x, int y, int width, int height);

    /**
     *
     * @param pImage        区域的图片Bitmap
     * @param x             区域的左上角的x坐标
     * @param y             区域的左上角的y坐标
     * @param nGetWidth    区域的宽
     * @param nGetHeight   区域的高
     * @return      0为获取失败，1为获取成功
     */
    public native int getCurrentImageRegion(Bitmap pImage, int x,int y,int nGetWidth,int nGetHeight);


	/**
	 * 保存当前步骤的真实图片到硬盘里
	 * @param strPath	为图片保存的绝对路径
	 * @param nMaxlen	当值为0时则为原始图片数据，当值大于0时，则图片的宽或高最大值就为nMaxlen
	 * @param nQuality	图片保存质量，参数为60-100，最高质量为100,默认为85
	 * @return	返回0表示保存失败，返回1表示保存成功
	 */
	public native int saveImageWithPath(String strPath,int nMaxlen,int nQuality);

    /**
     * 缓存当前步骤的图片数据到硬盘
     * @param strPath  缓存到本地硬盘的绝对路径
     * @param nType     参数0为显示的图片数据，1为真实的图片数据。2为功能内操作的显示数据，3为功能内操作的真实数据
     * @return          返回1表示缓存成功，返回0表示缓存失败
     */
    public native int saveImageDataToDisk(String strPath,int nType);

    /**
     * 从本地硬盘将缓存文件恢复为内存
     * @param strPath   缓存到本地硬盘的绝对路径
     * @param nType      参数0为显示的图片数据，1为真实的图片数据。2为功能内操作的显示数据，3为功能内操作的真实数据
     * @return          返回1表示恢复成功，返回0表示恢复失败
     */
	public native int loadImageDataFromDisk(String strPath,int nType);
	

	//子功能内的操作函数
    /**
     * 初始化子功能的操作数据
     * @return      返回1表示初始化成功，返回0表示初始化失败
     */
	public native int initProcImageData();

    /**
     * 释放子功能的操作数据
     * @return      返回1表示释放成功，返回0表示释放失败
     */
	public native int clearProcImageData();

    /**
     * 子功能的确定操作
     * @param nType     参数为0表示功能的UI显示图，1表示功能的真实图，2表示功能的显示图与真实图数据
     * @return          返回1表示操作成功，返回0表示操作失败
     */
	public native int ok(int nType);

    /**
     * 获取功能内的UI显示图片数据
     * @param imageSize     当前功能UI显示图片的尺寸
     * @return      当前功能内UI显示的图片数据ARGB，如果为NULL,则表示不存在
     */
	public native int[] getShowProcImageData(int []imageSize);
    /**
     * 获取功能内的显示图片Bitmap
     * @param pImage	显示的图片
     * @return	0为获取失败，1为获取成功
     */
    public native int getShowProcImage(Bitmap pImage);

    /**
     * 获取功能内的显示图片的尺寸
     * @return      返回功能内的显示图片的尺寸
     */
    public native int[] getShowProcImageSize();
    /**
     * 清除子功能的缓存内存
     */
    public native int ToolTempDataClear();
	/**
	 * 	美容的tool
	 */
    /**
     * 磨皮美白
     * @param strPath  临时文件夹
     * @param values   总共四个参数，
     *                  第一个参数为图片的处理半径,固定为13，第二个参数为美白的index（范围为0-10）
     *                  第三个参数为肤色的index（范围为0-10）,第四个参数为磨皮的百分比（范围为0-10）
     * @param length   values的个数
     * @param bIsPreview    是否是预览图
     * @return      返回1表示操作成功，返回0表示操作失败
     */
	public native int ToolCosmesisProcess(String strPath,float values[],int length,boolean bIsPreview);
    /**
     * 瘦脸瘦身功能
     * @param values 六个参数，
     *               第一个参数为起始点的坐标x/图片的宽，第二个参数为始点的坐标y/图片的高
     *               第三个参数为终点的坐标x/图片的宽，第四个参数为终点的坐标y/图片的高
     *               第五个参数为半径/图片的宽，第六个参数为力度(1-100之间)
     * @param length   values的个数
     * @return     返回1表示操作成功，返回0表示操作失败
     */
	public native int ToolSlimFaceAndBody(float values[],int length);
    /**
     * 眼睛放大功能
     * @param values  四个参数，
     *                第一个参数为点的坐标x/图片的宽，第二个参数为点的坐标y/图片的高
     *                第三个参数为半径/图片的宽，第四个参数为力度(眼睛放大为正数，眼睛缩小为负数，范围是-100到100)
     * @param length     values的个数
     * @return     返回1表示操作成功，返回0表示操作失败
     */
	public native int ToolZoomEyes(float values[],int length);

    /**
     * 整张图片的亮眼初始化操作
     * @param nRadius     nRadius为亮眼半径
     * @param nStrength   nStrength程度，范围0-10
     * @return      返回1表示操作成功，返回0表示操作失败
     */
	public native int ToolBrightEyes(int nRadius,int nStrength);

    /**
     * 亮眼的确定操作
     * @param maskData      用户需要亮眼的区域
     * @param nMaskWidth    用户需要亮眼的区域的宽
     * @param nMaskHeight   用户需要亮眼的区域的高
     * @param fAlpha        亮眼的透明度
     * @return              返回1表示操作成功，返回0表示操作失败
     */
//	public native int ToolBrightEyesMixing(int[]maskData,int nMaskWidth,int nMaskHeight,float fAlpha);
    /**
     * 亮眼的确定操作
     * @param pImage        用户需要亮眼的区域的对象
     * @param fAlpha        亮眼的透明度
     * @return              返回1表示操作成功，返回0表示操作失败
     */
    public native int ToolBrightEyesMixingWithImage(Bitmap pImage,float fAlpha);
	//

    /**
     * 祛痘祛斑，传递的是ARGB数据
     * @param maskData          需要祛痘祛斑的区域的图片数据
     * @param nMaskWidth        需要祛痘祛斑的区域的图片的宽
     * @param nMaskHeight       需要祛痘祛斑的区域的图片
     * @param nMaxFristSearch   第一次搜索的次数，默认为150
     * @param nMaxOtherSearch   接下来搜索的次数，默认为20
     * @return          返回1表示操作成功，返回0表示操作失败
     */
//	public native int ToolRemoveBeverageAndAcne(int[] maskData,int nMaskWidth,int nMaskHeight,int nMaxFristSearch,int nMaxOtherSearch );

    /**
     * 祛痘祛斑，传递的是Bitmap对象
     * @param pImage            需要祛痘祛斑的区域的图片对象
     * @param nMaxFristSearch   第一次搜索的次数，默认为150
     * @param nMaxOtherSearch   接下来搜索的次数，默认为20
     * @return          返回1表示操作成功，返回0表示操作失败
     */
    public native int ToolRemoveBeverageAndAcneWithImage(Bitmap pImage,int nMaxFristSearch,int nMaxOtherSearch);

    /**
     * 消除黑圆圈
     * @param maskData       消除黑圆圈的区域的图片数据
     * @param nMaskWidth     消除黑圆圈的区域的图片的宽
     * @param nMaskHeight    消除黑圆圈的区域的图片的高
     * @return         返回1表示操作成功，返回0表示操作失败
     */
//	public native int ToolSkinMove(int[]maskData,int nMaskWidth,int nMaskHeight);

    /**
     * 消除黑圆圈
     * @param pImage        消除黑圆圈的区域的图片对象
     * @return              返回1表示操作成功，返回0表示操作失败
     */
    public native int ToolSkinMoveWithImage(Bitmap pImage);
	
	/**
	 * 	美化的tool
	 */

    /**
     *   将子功能的图片数据恢复为刚进入的图片
     * @param bIsPreview   false为子功能的真实图，true为子功能的UI显示图
     * @return       返回0操作失败，返回1操作成功
     */
	public native int ToolResetImageToOral(boolean bIsPreview);
	/**
	 * 简单边框
	 * @param strPath		文件路径
	 * @param bIsSDCard		文件是否在SD卡里，true为是，false为在ASSETS文件夹里，即软件自带
	 * @param bIsPreview	是否为显示图
     * @return       返回0操作失败，返回1操作成功
	 */
	public native int ToolSimpleFrame( String strPath, boolean bIsSDCard, boolean bIsPreview);
    /**
     * 简单边框的缩略图操作（数据会不会同步到pData，在返回值那边获取）
     * @param pData			缩略图的数据ARGB的
     * @param width			缩略图的宽
     * @param height		缩略图的高
     * @param strPath		文件路径
     * @param isSDCard		文件是否在SD卡里，true为是，false为在ASSETS文件夹里，即软件自带
     * @param imageSize		返回的图片数据的宽和高，第一位数值表示宽，第二位数值表示高
     * @return				简单边框操作后的图片数据
     */
    public native int[] ToolSimpleFrameWithThumbnail(int []pData,int width,int height,String strPath,boolean isSDCard,int[] imageSize);

    /**
     * 简单边框的缩略图操作
     * @param pImage       传递缩略图的图片Bitmap，不修改在此图上
     * @param strPath		文件路径
     * @param isSDCard		文件是否在SD卡里，true为是，false为在ASSETS文件夹里，即软件自带
     * @return              返回生成的简单边框的缩略图，如果为nil则是生成失败
     */
    public native Bitmap ToolSimpleFrameWithThumbnailFromBitmap(Bitmap pImage,String strPath,boolean isSDCard);

    /**
	 * 炫彩边框
	 * @param strPath		文件路径
	 * @param nChannelType	图片与原图的叠加模式
	 * @param bIsSDCard		文件是否在SD卡里，true为是，false为在ASSETS文件夹里，即软件自带
	 * @param bIsPreview	是否为显示图
     * @return       返回1则生成成功，返回0则生成失败
	 */
	public native int ToolColorFrame(String strPath,int nChannelType, boolean bIsSDCard, boolean bIsPreview);

    /**
     * 炫彩边框的缩略图操作（数据会自动同步到pData，即pData数据会修改）
     * @param pData			缩略图的数据ARGB的
     * @param width			缩略图的宽
     * @param height		缩略图的高
     * @param strPath		文件路径
     * @param nChannelType	图片与原图的叠加模式
     * @param isSDCard		文件是否在SD卡里，true为是，false为在ASSETS文件夹里，即软件自带
     * @return            返回1则生成成功，返回0则生成失败
     */
    public native int ToolColorFrameWithThumbnail(int[] pData,int width,int height,String strPath,int nChannelType,boolean isSDCard);

    /**
     * 炫彩边框的缩略图操作
     * @param pImage        传递缩略图的图片Bitmap，直接修改在此图片上
     * @param strPath		 文件路径
     * @param nChannelType 图片与原图的叠加模式
     * @param isSDCard		文件是否在SD卡里，true为是，false为在ASSETS文件夹里，即软件自带
     * @return         返回1则生成成功，返回0则生成失败
     */
    public native int ToolColorFrameWithThumbnailFromBitmap(Bitmap pImage,String strPath,int nChannelType,boolean isSDCard);

    /**
	 * 裁剪
	 * @param values		裁剪的四个点的值（具体数值的百分比）
	 * @param length		参数为4
	 * @param bIsPreview	是否为显示图
     * @return         返回1则生成成功，返回0则生成失败
	 */
	public native int ToolCut(float values[],int length,boolean bIsPreview );
	/**
	 * 锐化功能
	 * @param value			锐化的参数
	 * @param bIsPreview	是否为显示图
	 */
	public native int ToolSharp(float value,boolean bIsPreview);
	/**
	 * 文字操作，传递的是ARGB数据
	 * @param data		文字的数据
	 * @param width		数据的宽
	 * @param height	数据的高
	 * @param x			显示的坐标X
	 * @param y			显示的坐标Y
	 * @param scale		数据放缩的比例
	 */
//	public native int ToolText(int data[],int width,int height,float x,float y,float scale);

    /**
     * 文字操作，传递的是Bitmap对象
     * @param pImage    文字的Bitmap对象
     * @param x			显示的坐标X
     * @param y			显示的坐标Y
     * @param scale		数据放缩的比例
     * @return
     */
    public native int ToolTextWithImage(Bitmap pImage,float x,float y,float scale);
    /**
     * 新的旋转接口
     * @param rsArray           向左右旋转，上下翻转，左右翻转的矩阵变换数组，参数个数为9个，由Matrix转换而来
     * @param length             rsArray的参数个数，固定为9个
     * @param fAngleFree        自由旋转的角度，范围为0-360度
     * @return     返回0则生成成功，返回1则生成失败
     */
    public native int ToolRotationNew(float []rsArray,int length,float fAngleFree);
    /**
     * 调色
     * @param values 参数为四个，第一个为亮度，第二个为对比度，第三个为饱和度，第四个为曝光度
     * @param length 值固定为4
     * @param bIsPreview 是否是显示图片操作，true表示显示图片操作，false表示真实图片操作
     * @return 返回1则生成成功，返回0则生成失败
     */
    public native int ToolColorMixing(float values[],int length,boolean bIsPreview);

    /**
     * 特效
     * nIndex为特效的编号，bIsPreview为是否为显示图
     */
    public native int ToolEffect(int nIndex,boolean bIsPreview);

    /**
     * 特效
     * @param nIndex            特效的编号
     * @param fEffectAlpha      应用该特效的百分比
     * @param bIsPreview        是否为显示图
     * @return             返回1则生成成功，返回0则生成失败
     */
    public native int ToolEffectWithAlpha(int nIndex, float fEffectAlpha, boolean bIsPreview);
    /**
     * 特效缩略图操作(数据会自动同步到pData，即pData数据会修改)
     * @param pData		缩略图的数据ARGB的
     * @param width			缩略图的宽
     * @param height		缩略图的高
     * @param nIndex		特效的编号
     * @return          返回1则生成成功，返回0则生成失败
     */
    public native int ToolEffectWithThumbnail(int []pData,int width,int height,int nIndex);
    /**
     * 特效缩略图操作
     * @param pImage    传递缩略图的图片Bitmap，直接修改在此图片上
     * @param nIndex    特效的编号
     * @return           返回1则生成成功，返回0则生成失败
     */
    public native int ToolEffectWithThumbnailFromBitmap(Bitmap pImage,int nIndex);
    /**
     * 虚化初始化
     */
	public native boolean MiddleWeakInit();

    /**
     * 设置虚化类型
     * @param kind  0为圆形虚化，1为直线虚化
     */
	public native boolean MiddleWeakSetType(int kind);

    /**
     *  设置虚化的内外圈半径
     * @param InRadius         内圈半径
     * @param OutRadius         外圈半径，必须大于内圈半径
     */
	public native boolean MiddleWeakSetRadius(int InRadius,int OutRadius);

    /**
     * 圆形虚化处理
     * @param x         中心点的x坐标
     * @param y         中心点的y坐标
     */
	public native boolean MiddleWeakDealRadiusPic(int x,int y);

    /**
     * 直线虚化处理
     * @param x         中心点的x坐标
     * @param y         中心点的y坐标
     * @param angle     直线虚化的旋转角度
     */
	public native boolean MiddleWeakDealLinePic(int x[],int y[],float angle);

    /**
     * 虚化的释放函数
     */
	public native boolean MiddleWeakRelease();

    /**
     * 虚化确定按钮
     */
	public native int MiddleWeakOK();



    /**
     * 马赛克功能
     *
     * @param nBlock 马赛克半径
     * @return  返回1则生成成功，返回0则生成失败
     */
    public native int ToolMosaic(int nBlock);

    /**
     * 马赛克功能
     *
     * @param nBlock         马赛克半径
     * @param pTexture      纹理的Bitmap对象
     * @return      返回1则生成成功，返回0则生成失败
     */
    public native int ToolMosaicWithTexture(int nBlock, Bitmap pTexture);

    /**
     * 马赛克功能的混合功能
     * @param pMaskImage    马赛克需要涂抹的区域的Bitmap（用白色填充）
     * @return  返回1则生成成功，返回0则生成失败
     */
    public native int ToolMosaicMixing(Bitmap pMaskImage);
    
    
    
    
    

	/*********************************拼图*****************************************************************/
	
	/**
	 * 校验NDK底下的ARGB的顺序，正常的顺序是RGBA，但是出现中兴U800的是BGRA，所以这个用一张已知的图来校正顺序
	 * @param pImage	Asserts里的ndk_check_color.bmp图片
	 * @return
	 */
	public native int PuzzleNDKCheckColorARGB8888Index(Bitmap pImage);
	/**
	 * 设置缓存文件路径给NDK层，主要用于拼图的节点拼图的缓存
	 * @param path
	 * @param nPuzzleType 拼图类型，模板拼图1，自由拼图2，图片拼接3，海报拼图4
	 * @return
	 */
	public native int PuzzleStartWithTempFileSavePath(String path,int nPuzzleType);
	/**
	 * 设置APk路径给拼图的NDK底层
	 *
	 * @param path 路径
	 * @return
	 */
	public native int SetAPKPathForPuzzle(String path);
	/**
	 * 拼图时边框初始化JNI
	 *
	 * @param strFramePath   存放边框文件的路径
	 * @param strTexturePath 存放底纹数据的路径
	 * @param nPuzzleWidth   拼图图片的宽度
	 * @param nPuzzleHeight  拼图图片的高度
	 * @param isFromSd       素材文件是否来自Sd卡
	 * @return 最终的拼图图片的宽、高
	 */
	public native int[] PuzzleFrameInit(String strFramePath, String strTexturePath, int nPuzzleWidth, int nPuzzleHeight);

	/**
	 * 自由拼图初始化底层背景
	 *
	 * @param pBgData       背景图片数据
	 * @param nPuzzleWidth  图片Size (也是指拼图保存图片大小)
	 * @param nPuzzleHeight
	 */
	public native void PuzzleBackGroundInitByte(int[] pBgData, int nPuzzleWidth, int nPuzzleHeight);

	/**
	 * 自由拼图初始化底层背景
	 * @param bitmap	自由背景的图片
	 * @return	返回0为插入失败，返回1为插入成功
	 */
	public native int PuzzleBackGroundInitBitmap(Bitmap bitmap);

	/**
	 * 设定展示图片的宽高
	 *
	 * @param nShowWidth
	 * @param nShowHeight
	 * @param nIsAlpha 图片拼接为0，模板拼图为1
	 * @return 展示图片的宽高
	 */
	public native int[] PuzzleResetShowSize(int nShowWidth, int nShowHeight,int nIsAlpha);

	/**
	 * 获取拼接图片的边框个数
	 *
	 * @return
	 */
	public native int PuzzleGetShowCount();


	//获取五个区域的图片数据
	/*
	1111111111111111111111
	3000000000000000000004
	3000000000000000000004
	3000000000000000000004
	3000000000000000000004
	5000000000000000000006
	5000000000000000000006
	5000000000000000000006
	5000000000000000000006
	.00000000000000000000.
	.00000000000000000000.
	.00000000000000000000.
	.00000000000000000000.
	2222222222222222222222
	*/

	/**
	 *  获取给定位置的展示边框的图片尺寸
	 * @param nIndex	边框的编号
	 * @return	图片的尺寸
	 */
	public native int[] PuzzleGetFrameShowSizeWithIndex(int nIndex);
	/**
	 * 获取给定位置的展示边框的图片
	 * @param nIndex	边框的编号
	 * @param pImage	边框的图片
	 * @return	0获取失败，1获取成功
	 */
	public native int PuzzleGetFrameShowImageWithIndex(int nIndex,Bitmap pImage);
	/**
	 * 获取给定位置带阴影的展示边框的图片
	 * @param nIndex	边框的编号
	 * @param pImage	边框的图片
	 * @return	0获取失败，1获取成功
	 */
	public native int PuzzleGetFrameShowImageWithShadowByIndex(int nIndex,Bitmap pImage);
	/**
	 * 获取给定位置的展示边框图片数据
	 *
	 * @param nIndex    边框编号
	 * @param imageSize 边框的宽、高数据
	 * @return 边框图片的数据
	 */

	public native int[] PuzzleGetFrameShowDataWithIndex(int nIndex, int[] imageSize);

	/**
	 * 释放图片拼接和模板拼图的边框的内存
	 * @return
	 */
	public native int PuzzleClearShowFrames();
	
	/**
	 * 获取给定位置，带阴影的边框图片数据
	 *
	 * @param nIndex    边框编号
	 * @param imageSize 边框的宽、高数据
	 * @return 带阴影的边框图片数据
	 */
	public native int[] PuzzleGetFrameShowDataWithShadowByIndex(int nIndex, int[] imageSize);


	/**
	 * 通过Bitmap的形式向底层传递图片数据
	 * @param nID		节点的ID,唯一ID,这样如果有相同的图片，可以重用
	 * @param pImage	图片
	 * @return 返回0为插入失败，返回1为插入成功
	 */
	public native int PuzzleInsertNodeImage(int nID,Bitmap pImage);
	/**
	 * 通过BYTE流的形式向底层传递图片数据
	 *
	 * @param nID        节点的ID,唯一ID,这样如果有相同的图片，可以重用
	 * @param imageData  图片数据
	 * @param MaskWidth  图片宽
	 * @param MaskHeight 图片高
	 * @return 底层图片个数
	 */
	public native int PuzzleInsertNodeImageData(int nID, int[] imageData, int MaskWidth, int MaskHeight);


	/**
	 * 通过路径的形式向底层传递图片数据
	 * @param nID			节点的ID,唯一ID,这样如果有相同的图片，可以重用
	 * @param strImagePath	图片储存在本地的路径
	 * @param nExifOrientation	图片的EXIF里的旋转方向
	 * @return	底层图片个数
	 */
	public native int PuzzleInsertNodeWithPath(int nID,String strImagePath,int nExifOrientation);
	/**
	 * 移除一个图片数据
	 *
	 * @param nID 图片的标识
	 * @return
	 */
	public native int PuzzleRemoveNodeWithID(int nID);

	// 获取旋转角度,返回值1~6,1 代表正常情况
	//     1        2       3      4         5            6           7          8
//
//    888888  888888      88  88      8888888888  88                  88  8888888888
//    88          88      88  88      88  88      88  88          88  88      88  88
//    8888      8888    8888  8888    88          8888888888  8888888888          88
//    88          88      88  88
//    88          88  888888  888888

	/**
	 * 图片拼接保存接口
	 *
	 * @param strSavePath 要保存到的目标路径
	 * @param pID         节点的ID
	 * @return 是否成功
	 */

	public native boolean PuzzleJointSaveToSD(String strSavePath, int pID[]);


	/**
	 * 自由拼图保存到指定的路径上，白边宽度固定为10
	 *
	 * @param strSavePath  保存的路径
	 * @param pID          使用的图片ID数组(从下到上层)
	 * @param pRotate      对应节点的旋转角度(0~360+)
	 * @param pScale       对应节点的图片缩放系数
	 * @param pCenterPoint 每个节点图片在旋转和放缩后中心点在拼图区域上的XY位置(基于拼图区域尺寸的比例信息)
	 * @return 是否成功
	 */
	public native boolean PuzzleFreeSaveToSD(String strSavePath, int pID[], int pRotate[], float pScale[],
	                                         float pCenterPoint[]);

	/**
	 * 自由拼图保存到指定路径，白边宽度由java定
	 *
	 * @param strSavePath  保存路径
	 * @param pID          图片ID序列
	 * @param pRotate      ID对应的旋转角度序列
	 * @param pScale       对应的节点图片放缩值序列
	 * @param pCenterPoint 每个节点图片在旋转和放缩后中心点在拼图区域上的XY位置(基于拼图区域尺寸的比例信息)
	 * @param pFrameWidth  白边宽度序列（在放缩操作之前加的宽度）
	 * @return
	 */
	public native boolean PuzzleFreeSaveToSDwithFrame(String strSavePath, int pID[], int pRotate[], float pScale[],
	                                                  float pCenterPoint[], int[] pFrameWidth);


	/**
	 * 读取配置文件
	 *
	 * @param formatPath 配置文件地址
	 * @return 是否读取成功
	 */
	public native boolean puzzleHBPTloadByPath(String formatPath);


	/**
	 * 通过传byte流的形式读取素材
	 * @param pFormatData 	素材文件byte数据
	 * @param nResultWidth	海报保存的图片大小
	 * @param nResultHight	
	 * @return
	 */
	public native boolean puzzleHBPTloadByBytes(byte pFormatData[],int nResultWidth,int nResultHight);

	/**
	 * 获取底图数据
	 *
	 * @param sizeAndType 3个长度，第1，2为宽高，第3个为背景类型
	 * @return 底图数据
	 */
	public native int[] puzzleHBPTgetBackgroundData(int sizeAndType[]);

	/**
	 * 获取海报拼图的底图数据
	 * @param sizeAndType	3个长度，第1，2为宽高，第3个为背景类型
	 * @param pImage		获取海报拼图的图片
	 * @return			0表示获取失败，1表示获取成功 
	 */
	public native int puzzleHBPTgetBackgroundImage(int sizeAndType[],Bitmap pImage);
	
	/**
	 * 获取顶图数据
	 *
	 * @param size 宽高
	 * @return 顶图数据
	 */
	public native int[] puzzleHBPTgetForegroundData(int size[]);

	/**
	 * 获取海报拼图顶图的图片(处理PNG图片的时候有点问题)
	 * @param size		图片的尺寸
	 * @param pImage	图片的bitmap
	 * @return
	 */
	public native int puzzleHBPTgetForegroundImage(int size[],Bitmap pImage);

	/**
	 * 是否存在顶图
	 *
	 * @return
	 */
	public native boolean puzzleHBPTisExistForeGround();

	/**
	 * 是否需要更新
	 *
	 * @return 是否要更新
	 */
	public native boolean puzzleHBPTisNeedToUpdate();

	/**
	 * 海报拼图保存接口
	 *
	 * @param savePath     保存路径
	 * @param nSaveWidth   最终保存图片的宽
	 * @param nSaveHeight  最终保存图片的高
	 * @param pID          节点图片的ID
	 * @param pDstPosition 节点图片的最大矩形区域
	 * @return 是否保存成功
	 */
	public native boolean puzzleHBPTSaveToSD(String savePath, int nSaveWidth, int nSaveHeight, int[] pID, float[] pDstPosition);


	/**
	 * 不规则拼图保存，模板拼图都使用这个
	 *
	 * @param strSavePath  保存路径
	 * @param pID          节点图片ID序列
	 * @param pDstPosition 节点最大Rect序列(left top right bottom)
	 * @return 是否保存成功
	 */
	public native boolean puzzleIrregularSaveToSD(String strSavePath, int[] pID, float[] pDstPosition);

	/**
	 * 释放内存，释放以后，NDK底层所有节点数据释放，没有释放边框与海报拼图背景数据资源。
	 */
	public native void PuzzleClearMemory();

	/**
	 * 释放所有资源，包括边框资源，海报拼图背景与顶图资源，在退出拼图模块时调用
	 */
	public native void PuzzleClearMemoryAll();

}
