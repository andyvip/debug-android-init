package com.jrdcom.mt.mtxx.tools;

import android.graphics.Path;
import android.graphics.PointF;
import android.graphics.RectF;
import android.util.Log;
import com.jrdcom.mt.util.StreamUtils;

import java.io.*;
import java.util.ArrayList;

/**
 * 读取版式配置文件里面的信息
 * 
 * @author Intern Hrx 2012-11-23 created
 */
public class PuzzleFormatPath {

	/** 板式路径列表 **/
	private ArrayList<Path> mPathList = null;

	/** 绝对坐标路径列表(根据最大矩形所做的路径偏移) **/
	private ArrayList<Path> mAbsolutePathList = null;

	/** 每个path对应的最大矩形列表 **/
	private ArrayList<RectF> mPathRectList = null;

	/** 每个Path经过计算后得到的Rect **/
	private ArrayList<RectF> mDstPathRectList = null;

	/** 存放路径的点的位置信息 **/
	private ArrayList<ArrayList<PointF>> mPointList = null;

	/** 边类型 */
	private ArrayList<int[]> mSideTypeList = null;

	/** 旋转角度 **/
	private float mRotateList[];

	/** 原板式的宽高 **/
	private int mPlateSize[] = new int[2];

	/** 目标板式宽高 **/
	private int mDstPlateSize[] = new int[2];

	/** 板式的路径个数 **/
	private int mPathCount = 0;

	/** 文件类型 **/
	private String mFileType = null;

	/** 外层View的宽度 */
	private int mViewWidth = 0;

	/** 外层View的高度 */
	private int mViewHeight = 0;

	/** 根据所给区域大小换算的Scale **/
	private float mScale = 0f;

	/** 原点X偏移 **/
	private float mOffsetX = 0;

	/** 原点Y偏移 */
	private float mOffsetY = 0;

	/** 版本信息 **/
	private int mVersion;

	private final String TAG = "ReadFile";
	public static final int SIDE_TYPE_LINE = 0;// 直线边
	public static final int SIDE_TYPE_CURVE = 1;// 基数样条
	public static final int SIDE_TYPE_BEZIER = 2;// 贝赛尔样条
	public static final int SIDE_TYPE_ARC = 3;// 弧线
	public static final String DEFAULT_FILETYPE = "PTLJB";

	/**
	 * Constracter
	 */
	public PuzzleFormatPath() {
	}

	/**
	 * 根据所给配置文件，读取版式配置文件，解析相关版式信息
	 * 
	 * @param nPath
	 *            配置文件路径
	 * @param nDstWidth
	 *            视口宽度
	 * @param nDstHeight
	 *            视口高度
	 * @return 读取成功与否标志
	 */
	public boolean ReadPuzzleFromatPathFile(String nPath, int nDstWidth, int nDstHeight) {
		if (ReadPuzzleFromatPathFile(nPath) == false)
			return false;

		/** 进行换算 **/
		mScale = getScaleAjustWidthAndHeight(mPlateSize[0], mPlateSize[1], nDstWidth, nDstHeight);
		Log.i(TAG, "DstWidth:" + mDstPlateSize[0] + "  DstHeight:" + mDstPlateSize[1]);
		Log.i(TAG, "Xoffset:" + mOffsetX + "  Yoffset:" + mOffsetY);
		return true;

	}

	/**
	 * 根据所给配置文件，读取版式配置文件，解析相关版式信息
	 * 
	 * @param nPath
	 *            配置文件路径
	 * @return 读取成功与否标志
	 */
	public boolean ReadPuzzleFromatPathFile(String nPath) {
		File plateIniFile = new File(nPath);
		if (plateIniFile == null || plateIniFile.exists() == false)// 文件路径有错或其他问题
		{
			Log.i(TAG, "File----->make failure");
			return false;
		}
		Log.i(TAG, "File------>make Success");
		FileInputStream inputS = null;
		try {
			inputS = new FileInputStream(plateIniFile);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
		if (inputS == null) {
			Log.i(TAG, "InputStream--->null");
			return false;
		}
		return ReadPuzzleFromatInputStream(inputS);
	}

	/**
	 * 通过传递InputStream流读取数据流，解析配置文件
	 * 
	 * @param inputS
	 *            数据输入流
	 * @param nDstWidth
	 *            视口宽度
	 * @param nDstHeight
	 *            视口高度
	 * @return 是否解析成功
	 */
	public boolean ReadPuzzleFromatInputStream(InputStream inputS, int nDstWidth, int nDstHeight) {
		if (inputS == null)
			return false;
		if (ReadPuzzleFromatInputStream(inputS) == false)
			return false;
		/** 进行换算 **/
		mScale = getScaleAjustWidthAndHeight(mPlateSize[0], mPlateSize[1], nDstWidth, nDstHeight);
//		Log.i(TAG, "DstWidth:" + mDstPlateSize[0] + "  DstHeight:" + mDstPlateSize[1]);
//		Log.i(TAG, "Xoffset:" + mOffsetX + " Yoffset:" + mOffsetY);
		return true;
	}

	/**
	 * 通过传递InputStream流读取数据流
	 * 
	 * @param inputS
	 *            数据流
	 * @return 是否解析成功
	 */
	// 12-16 文件类型5个字节
	// 32-35 版本信息4个字节
	// 36-39 版式宽度4个字节
	// 40-43 版式高度4个字节
	// 44-47 版式路径个数4个字节
	// 48-55 预留8个字节
	// 56-59 第一个路径的旋转角度
	//
	public boolean ReadPuzzleFromatInputStream(InputStream inputS) {
		if (null == inputS) {
			Log.i(TAG, "InputStream--->null");
			return false;
		}
		byte data[] = null;// 存放配置文件的byte数据,由于配置文件不大，这样偏移会比较快
		int dataCursor;// 数据的游标
		try {
			data = StreamUtils.streamToBytes(inputS);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		} finally {
			StreamUtils.close(inputS);
		}
		/** 读取文件类型，字符串类型，位置为12-16的5个字节 **/
		dataCursor = 12;
		mFileType = getStringFromStream(data, dataCursor, 5);
		Log.i(TAG, "fileType：" + mFileType);
		if (mFileType.equals(DEFAULT_FILETYPE) == false)// 是否为正确的文件类型
		{
			Log.i(TAG, "FileType illegal");
			return false;
		}

		/** 读取版本信息，Int 类型，位置为32-35的4个字节 **/
		dataCursor = 32;
		mVersion = getIntArrayFromStream(data, dataCursor, 1)[0];
		dataCursor += 4;
		Log.i(TAG, "Verion:" + mVersion);

		/** 读取版式的Size信息，即Width&Height,Int类型，位置为36-39，40-43，共8个字节 **/
		mPlateSize = getIntArrayFromStream(data, dataCursor, 2);
		dataCursor += 8;
		Log.i(TAG, "SrcWidth:" + mPlateSize[0] + " SrcHeight:" + mPlateSize[1]);
		/** 读取路径的个数，int类型，位置为44-47的4个字节 **/
		mPathCount = getIntArrayFromStream(data, dataCursor, 1)[0];
		dataCursor += 4;
		Log.i(TAG, "Path count:" + mPathCount);

		/*** 中间将预留8个字节 **/
		dataCursor += 8;

		mPathRectList = new ArrayList<RectF>(mPathCount);
		mRotateList = new float[mPathCount];
		mPointList = new ArrayList<ArrayList<PointF>>(mPathCount);
		mSideTypeList = new ArrayList<int[]>(mPathCount);

		/** 依次读取每个路径信息 **/
		for (int index = 0; index < mPathCount; index++) {
			/** 读取旋转角度4个字节 */
			mRotateList[index] = getFloatArrayFromStream(data, dataCursor, 1)[0];
			dataCursor += 4;
			Log.i(TAG, "rotate:" + mRotateList[index] + "----->" + index);

			/** 读取Path的最大矩形信息 **/
			float rectData[] = getFloatArrayFromStream(data, dataCursor, 4);// point,width,height
			dataCursor += 16;// 偏移4*4个字节
			RectF rectPath = new RectF();
			rectPath.left = rectData[0];
			rectPath.top = rectData[1];
			rectPath.right = rectData[2] + rectData[0];
			rectPath.bottom = rectData[3] + rectData[1];
			mPathRectList.add(index, rectPath);// 加入List

			/** 读取路径起始点 **/
			float startpoint[] = getFloatArrayFromStream(data, dataCursor, 2);
			dataCursor += 8;
			PointF startPoint = new PointF(startpoint[0], startpoint[1]);
			Log.i(TAG, "StartPoint X:" + startpoint[0] + " Y:" + startpoint[1]);
			/** 读取边的个数 **/
			int sideCount = getIntArrayFromStream(data, dataCursor, 1)[0];
			Log.i(TAG, "SideCount---->" + sideCount);
			dataCursor += 4;

			ArrayList<PointF> pointList = new ArrayList<PointF>();
			int sideTypes[] = new int[sideCount];
			pointList.add(0, startPoint);// 第一个为起始点

			/** 给Path添加边 **/
			int pointCount = 0;
			for (int i = 0; i < sideCount; i++) {
				// 确定边的类型
				int sideType = getIntArrayFromStream(data, dataCursor, 1)[0];
				Log.i(TAG, "SideType---->" + sideType);
				sideTypes[i] = sideType;
				dataCursor += 4;
				switch (sideType) {
				case SIDE_TYPE_LINE:// 直线
					float nextPoint[] = getFloatArrayFromStream(data, dataCursor, 2);
					dataCursor += 8;
					PointF point = new PointF(nextPoint[0], nextPoint[1]);
					pointCount++;
					pointList.add(pointCount, point);
					Log.i(TAG, "point" + i + "  X:" + nextPoint[0] + "  Y:" + nextPoint[1]);
					break;
				case SIDE_TYPE_CURVE:// 基数样条
					float point_curve[] = getFloatArrayFromStream(data, dataCursor, 4);
					dataCursor += 16;
					PointF pointC_0 = new PointF(point_curve[0], point_curve[1]);
					Log.i(TAG, "Point0X:" + point_curve[0] + "  Point0Y:" + point_curve[1]);
					pointCount++;
					pointList.add(pointCount, pointC_0);

					PointF pointC_1 = new PointF(point_curve[2], point_curve[3]);
					Log.i(TAG, "Point1X:" + point_curve[2] + "  Point1Y:" + point_curve[3]);
					pointCount++;
					pointList.add(pointCount, pointC_1);
					break;
				case SIDE_TYPE_BEZIER:// 贝赛尔样条
					float points[] = getFloatArrayFromStream(data, dataCursor, 6);
					dataCursor += 24;
					PointF point_0 = new PointF(points[0], points[1]);
					Log.i(TAG, "Point0X:" + points[0] + "  Point0Y:" + points[1]);
					pointCount++;
					pointList.add(pointCount, point_0);

					PointF point_1 = new PointF(points[2], points[3]);
					Log.i(TAG, "Point1X:" + points[2] + "  Point1Y:" + points[3]);
					pointCount++;
					pointList.add(pointCount, point_1);

					PointF point_2 = new PointF(points[4], points[5]);
					Log.i(TAG, "Point_endX:" + points[4] + "  Point_endY:" + points[5]);
					pointCount++;
					pointList.add(pointCount, point_2);
					break;
				case SIDE_TYPE_ARC:// 圆弧
					break;
				default:
					break;
				}
			}
			mPointList.add(pointList);
			mSideTypeList.add(sideTypes);
		}
		return true;
	}

	/**
	 * 获取原板式设计稿的宽
	 * 
	 * @return int
	 */
	final public int getOrgWidth() {
		return mPlateSize[0];
	}

	/**
	 * 获取原板式设计稿的高
	 * 
	 * @return int
	 */
	final public int getOrgHeight() {
		return mPlateSize[1];
	}

	/**
	 * 获取根据视口大小计算出来的板式宽
	 * 
	 * @return int
	 */
	final public int getDstWidth() {
		return mDstPlateSize[0];
	}

	/**
	 * 获取根据视口大小计算出来的板式高
	 * 
	 * @return int
	 */
	final public int getDstHeight() {
		return mDstPlateSize[1];
	}

	/**
	 * 更新Size，重新计算板式的相差数据
	 * 
	 * @param nDstWidth
	 *            视口宽度
	 * @param nDstHeight
	 *            视口高度
	 */
	public boolean resizePuzzle(int nDstWidth, int nDstHeight) {
		if (mPathRectList == null)
			return false;
		mScale = getScaleAjustWidthAndHeight(mPlateSize[0], mPlateSize[1], nDstWidth, nDstHeight);
		return true;
	}

	/**
	 * 计算后X方向上的偏移
	 * 
	 * @return float
	 */
	public float getXoffset() {
		return mOffsetX;
	}

	/**
	 * 计算后Y方向上的偏移
	 * 
	 * @return float
	 */
	public float getYoffset() {
		return mOffsetY;
	}

	/**
	 * 获取坐标换算后的Scale
	 * 
	 * @return float
	 */
	public float getScale() {
		return mScale;
	}

	/**
	 * 获取路径个数
	 * 
	 * @return int
	 */
	public int GetFormatPathCount() {
		return mPathCount;
	}

	/**
	 * 获取某一个位置的最大矩形
	 * 
	 * @param index
	 *            标记位置
	 * @return float RectF
	 */
	public RectF GetFormatMaxRectAtIndex(int index) {
		if (mDstPathRectList != null)
			return mDstPathRectList.get(index);
		return null;
	}

	/**
	 * 获取某一个位置原始版式的Rect数据(基于版式的0~1.0的float数据)
	 * 
	 * @param index
	 *            位置标记
	 * @return rectF
	 */
	public RectF getOrgMaxRectAtIndex(int index) {
		if (mPathRectList != null)
			return mPathRectList.get(index);
		return null;
	}

	/**
	 * 获取版式中最大Rect的原始数据集合
	 * 
	 * @return RectFlist
	 */
	public ArrayList<RectF> getOrgMaxRectList() {
		if (mPathRectList != null)
			return mPathRectList;
		return null;
	}

	/**
	 * 获取某一个位置的路径
	 * 
	 * @param index
	 *            标记位置
	 * @return Path
	 */
	public Path GetFormatBezierPathAtIndex(int index) {
		if (mPathList != null)
			return mPathList.get(index);
		return null;
	}

	/**
	 * 获取最终路径的列表
	 * 
	 * @return
	 */
	public ArrayList<Path> getDstFormatBezierPathList() {
		return mPathList;
	}

	/**
	 * 获取标记位的绝对坐标路径
	 * 
	 * @param index
	 * @return
	 */
	public Path getAbsoluteFormatBezierPathByIndex(int index) {
		return mAbsolutePathList.get(index);
	}

	/**
	 * 获取绝对坐标路径列表
	 * 
	 * @return
	 */
	public ArrayList<Path> getAbsoluteFormatBezierPathList() {
		return mAbsolutePathList;
	}

	/**
	 * 获取某一个标记位置的旋转角度
	 * 
	 * @param index
	 *            节点位置
	 * @return float
	 */
	public float GetFormatRotateAtIndex(int index) {
		return mRotateList[index];
	}

	/**
	 * 获取最终版式的大小
	 * 
	 * @return int width&&height
	 */
	public int[] GetFormatSize() {
		return mDstPlateSize;
	}

	/**
	 * 获取版式版本信息
	 * 
	 * @return int
	 */
	public int getPlateVersion() {
		return mVersion;
	}

	/**
	 * 获取版式配置文件的类型
	 * 
	 * @return string
	 */
	public String getPlateFileType() {
		return mFileType;
	}

	/**
	 * 通过所给目标区域大小，进行自适应调整
	 * 
	 * @param nSrcW
	 *            原大小
	 * @param nSrcH
	 * @param nDstW
	 *            目标区域大小
	 * @param nDstH
	 * @return 调整后的Scale值
	 */
	private float getScaleAjustWidthAndHeight(int nSrcW, int nSrcH, int nDstW, int nDstH) {
		float scale;
		if (nSrcW * nDstH < nSrcH * nDstW || nSrcW * nDstH == nSrcH * nDstW)// 以高为标准的情况
		{
			scale = nDstH / (nSrcH * 1.0f);
			Log.i(TAG, "Scale=" + scale);
			mDstPlateSize[1] = nDstH;
			mDstPlateSize[0] = (int) (nSrcW * scale + 0.5f);
			mOffsetX = (nDstW - mDstPlateSize[0]) / 2.0f;
			mOffsetY = 0.0f;
		} else// 适应宽
		{
			scale = nDstW / (nSrcW * 1.0f);
			Log.i(TAG, "Scale=" + scale);
			mDstPlateSize[0] = nDstW;
			mDstPlateSize[1] = (int) (nSrcH * scale + 0.5f);
			mOffsetX = 0.0f;
			mOffsetY = (nDstH - mDstPlateSize[1]) / 2.0f;
		}

		Log.i(TAG, "dstWidth=" + mDstPlateSize[0] + "   dstHeight=" + mDstPlateSize[1]);
		/** 根据当前大小计算各个Path **/
		mDstPathRectList = null;
		mDstPathRectList = new ArrayList<RectF>(mPathCount);

		mPathList = null;
		mAbsolutePathList = null;
		mPathList = new ArrayList<Path>(mPathCount);
		mAbsolutePathList = new ArrayList<Path>(mPathCount);
		Log.i("ReadFile", "Xoffset" + getXoffset() + "   Yoffsset+" + getYoffset());

		for (int index = 0; index < mPathCount; index++) {
			/** 最大矩形 **/
			float left;
			float right;
			float top;
			float bottom;
			RectF dstrect = new RectF();
			RectF rect = mPathRectList.get(index);
			left = rect.left * mDstPlateSize[0];
			top = rect.top * mDstPlateSize[1];
			right = rect.right * mDstPlateSize[0];
			bottom = rect.bottom * mDstPlateSize[1];
			Log.i(TAG, "RectF:left:" + left + " top:" + top + " right:" + right + " bottom:" + bottom);
			dstrect.set(left, top, right, bottom);

			mDstPathRectList.add(index, dstrect);

			/** 生成Path **/
			Path currPath = new Path();
			Path aCurrPath = new Path();
			ArrayList<PointF> points = mPointList.get(index);
			int sideTypes[] = mSideTypeList.get(index);
			PointF startPoint = points.get(0);

			currPath.moveTo(startPoint.x * mDstPlateSize[0] , startPoint.y * mDstPlateSize[1]);
			aCurrPath.moveTo(startPoint.x * mDstPlateSize[0]  + left, startPoint.y * mDstPlateSize[1] + top);
			int pointIndex = 1;
			for (int i = 0; i < sideTypes.length; i++) {
				Log.i(TAG, "Draw-->side" + i);
				int sideType = sideTypes[i];
				switch (sideType) {
				case SIDE_TYPE_LINE:// 直线
					PointF nextPoint = points.get(pointIndex);
					pointIndex++;
					// currPath.lineTo(nextPoint.x*mDstPlateSize[0],nextPoint.y*mDstPlateSize[1]);
					// aCurrPath.lineTo(nextPoint.x*mDstPlateSize[0]+left,
					// nextPoint.y*mDstPlateSize[1]+top);

					currPath.lineTo((float) (Math.round(nextPoint.x * mDstPlateSize[0])),
							(float) (Math.round(nextPoint.y * mDstPlateSize[1])));
					aCurrPath.lineTo((float) (Math.round(nextPoint.x * mDstPlateSize[0] + left)),
							(float) (Math.round(nextPoint.y * mDstPlateSize[1] + top)));

					break;
				case SIDE_TYPE_CURVE:// 基数样条
					PointF pointC_0 = points.get(pointIndex);
					pointIndex++;
					PointF pointC_1 = points.get(pointIndex);
					pointIndex++;
					currPath.quadTo(pointC_0.x * mDstPlateSize[0], pointC_0.y * mDstPlateSize[1], pointC_1.x
							* mDstPlateSize[0], pointC_1.y * mDstPlateSize[1]);
					aCurrPath.quadTo(pointC_0.x * mDstPlateSize[0] + left, pointC_0.y * mDstPlateSize[1] + top,
							pointC_1.x + mDstPlateSize[0] + left, pointC_1.y * mDstPlateSize[1] + top);
					break;
				case SIDE_TYPE_BEZIER:// 贝赛尔样条
					PointF point_0 = points.get(pointIndex);
					pointIndex++;
					PointF point_1 = points.get(pointIndex);
					pointIndex++;
					PointF point_2 = points.get(pointIndex);
					pointIndex++;
					currPath.cubicTo(point_0.x * mDstPlateSize[0], point_0.y * mDstPlateSize[1], point_1.x
							* mDstPlateSize[0], point_1.y * mDstPlateSize[1], point_2.x * mDstPlateSize[0], point_2.y
							* mDstPlateSize[1]);
					aCurrPath.cubicTo(point_0.x * mDstPlateSize[0] + left, point_0.y * mDstPlateSize[1] + top,
							point_1.x * mDstPlateSize[0] + left, point_1.y * mDstPlateSize[1] + top, point_2.x
									+ mDstPlateSize[0] + left, point_2.y * mDstPlateSize[1] + top);
					break;
				case SIDE_TYPE_ARC:// 圆弧
					break;
				default:
					break;
				}
			}
			currPath.close();
			aCurrPath.close();
			mPathList.add(index, currPath);
			mAbsolutePathList.add(index, aCurrPath);
		}
		return scale;
	}

	/**
	 * 读取一段int数组
	 * 
	 * @param data
	 *            源数组
	 * @param offset
	 *            偏移
	 * @param length
	 *            数组个数
	 * @return int 数组
	 */
	private int[] getIntArrayFromStream(byte data[], int offset, int length) {
		int dstArray[] = new int[length];
		int cusor = offset;
		byte byteTemp[] = new byte[4];
		for (int i = 0; i < length; i++) {
			arraycopy(data, cusor, byteTemp, 4);
			cusor += 4;
			dstArray[i] = bytesToInt(byteTemp);
		}

		return dstArray;
	}

	/**
	 * 读取一段Float数组
	 * 
	 * @param data
	 * @param offset
	 * @param length
	 * @return float[]
	 */
	private float[] getFloatArrayFromStream(byte data[], int offset, int length) {
		float dstArray[] = new float[length];
		int cusor = offset;
		byte byteTemp[] = new byte[4];
		for (int i = 0; i < length; i++) {
			arraycopy(data, cusor, byteTemp, 4);
			cusor += 4;
			dstArray[i] = bytesToFloat(byteTemp);
		}

		return dstArray;
	}

	/**
	 * 读取一段字符串
	 * 
	 * @param data
	 * @param offset
	 * @param length
	 * @return string
	 */
	private String getStringFromStream(byte data[], int offset, int length) {
		char dstArray[] = new char[length];
		int cusor = offset;

		for (int i = 0; i < length; i++) {
			byte byteTemp[] = new byte[2];
			arraycopy(data, cusor, byteTemp, 1);
			cusor += 1;
			dstArray[i] = bytesToChar(byteTemp);
		}
		return new String(dstArray);
	}

	/**
	 * 数组拷贝
	 * 
	 * @param src
	 *            源数组
	 * @param pos
	 *            源数组起始定位
	 * @param dst
	 *            目标数组
	 * @param len
	 *            拷贝长度
	 * @return int
	 */
	private int arraycopy(byte[] src, int pos, byte dst[], int len) {
		try {
			if (src.length >= pos + len) {
				System.arraycopy(src, pos, dst, 0, len);
				return len;
			}
		} catch (Exception e) {
		}
		return -1;
	}

	/**
	 * 把byte数组转成16进制的int(4个字节)
	 * 
	 * @param bytes
	 * @return
	 */
	private int bytesToInt(byte[] bytes) {

		int num = 0;
		byte temp;
		temp = bytes[0];
		bytes[0] = bytes[3];
		bytes[3] = temp;
		temp = bytes[1];
		bytes[1] = bytes[2];
		bytes[2] = temp;

		ByteArrayInputStream bintput = new ByteArrayInputStream(bytes);
		DataInputStream dintput = new DataInputStream(bintput);
		try {
			num = dintput.readInt();

		} catch (IOException e) {
			e.printStackTrace();
		}
		return num;
	}

	/**
	 * 把byte转成16进制的float类型(4个字节)
	 * 
	 * @param bytes
	 * @return float
	 */
	private float bytesToFloat(byte[] bytes) {
		float num = 0;
		byte temp;
		temp = bytes[0];
		bytes[0] = bytes[3];
		bytes[3] = temp;
		temp = bytes[1];
		bytes[1] = bytes[2];
		bytes[2] = temp;

		ByteArrayInputStream bintput = new ByteArrayInputStream(bytes);
		DataInputStream dintput = new DataInputStream(bintput);
		try {
			num = dintput.readFloat();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return num;

	}

	/**
	 * 把byte转成16进制Char(一个字节)
	 * 
	 * @param bytes
	 * @return char
	 */
	private char bytesToChar(byte[] bytes) {
		byte temp;
		temp = bytes[0];
		bytes[0] = bytes[1];
		bytes[1] = temp;
		char code = 0;
		ByteArrayInputStream bcharput = new ByteArrayInputStream(bytes);
		DataInputStream dcharput = new DataInputStream(bcharput);
		try {
			code = dcharput.readChar();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return code;
	}
}
