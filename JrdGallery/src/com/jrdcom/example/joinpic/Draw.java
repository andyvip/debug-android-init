package com.jrdcom.example.joinpic;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Bitmap.Config;
import android.os.Message;
import android.util.AttributeSet;
import android.util.FloatMath;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import com.jrdcom.android.gallery3d.R;


public class Draw extends View {

	Bmp bmp[];
	private ArrayList<String> mImagePathList = new ArrayList<String>();
	public int number = 0;
	public int aa = 1;
	List<String> it = new ArrayList<String>();

	private Bitmap canvasBitmap = Bitmap.createBitmap(480, 700,
			Config.ARGB_4444);
	/*
	 * private Bitmap mCanvasBitmap =
	 * BitmapFactory.decodeResource(getResources(),
	 * R.drawable.bg5).copy(Bitmap.Config.ARGB_8888, true);
	 */
	private Bitmap newBitmap = Bitmap.createBitmap(canvasBitmap);
	private Bmp tempBitmap = null;
	private Canvas mCanvas = new Canvas(newBitmap);

	private float X = 0f;
	private float Y = 0f;
	private Bmp[] pic;
	private float CX = 0f;
	private float CY = 0f;
	private boolean Begin = true;
	float rotalC[] = new float[2];
	float rotalP[] = new float[2];
	float rotalP_2[] = new float[2];
	int twoPoint = 0;

	private float preLength = 480.0f;
	private float length = 480.0f;
	private float preCos = 0f;
	private float cos = 0f;
	private boolean bool = true;
	Bitmap bitmap;

	public void setImagePathList(ArrayList<String> imagePathList) {
		mImagePathList = imagePathList;

		pic = new Bmp[mImagePathList.size()];
		bmp = new Bmp[mImagePathList.size()];
		for (int i = 0; i < mImagePathList.size(); i++) {
			bmp[i] = new Bmp(Bitmap.createScaledBitmap(
					add_rec(mImagePathList.get(i)), 150, 200, true), i,
					i * 10f, i * 20f);
			bmp[i].width = bmp[i].getPic().getWidth();
			bmp[i].height = bmp[i].getPic().getHeight();

		}
		this.pic = bmp;

		mCanvas.drawBitmap(bitmap, 0, 0, null);

		for (int i = 0; i < mImagePathList.size(); i++) {
			tempBitmap = pic[0].findByPiority(pic, i);
			if (i == 0) {
				tempBitmap.matrix.preTranslate(60, 150);
				tempBitmap.matrix.preRotate(20f);
			} else if (i == 1) {
				tempBitmap.matrix.preTranslate(60, 20);
				tempBitmap.matrix.preRotate(-20f);
			} else if (i == 2) {
				tempBitmap.matrix.preTranslate(100,60);
				tempBitmap.matrix.preRotate(0f);
			} else {
				tempBitmap.matrix.preTranslate(
						tempBitmap.getXY(1) - tempBitmap.getWidth() / 2 + 100,
						tempBitmap.getXY(2) - tempBitmap.getHeight() / 2);
				tempBitmap.matrix.preRotate(20f);
			}
			mCanvas.drawBitmap(tempBitmap.pic, tempBitmap.matrix, null);
		}
	}

	public Draw(Context context, AttributeSet attrs) {
		super(context, attrs);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		canvas.save();
		canvas.drawBitmap(newBitmap, 0, 0, null);
		canvas.restore();

	}


	@Override
	public boolean onTouchEvent(MotionEvent event) {
		// 向上弹起
		if (event.getAction() == MotionEvent.ACTION_UP) {
			bool = true;
			if (twoPoint > 0)
				twoPoint--;
		}
		if (event.getAction() == MotionEvent.ACTION_DOWN
				&& event.getPointerCount() == 1) {
			order(event);
			this.X = event.getX();
			this.Y = event.getY();
			CX = pic[mImagePathList.size() - 1].findByPiority(pic,
					mImagePathList.size() - 1).getXY(1)
					- event.getX();
			CY = pic[mImagePathList.size() - 1].findByPiority(pic,
					mImagePathList.size() - 1).getXY(2)
					- event.getY();
			Begin = true;
		}

		if (event.getAction() == MotionEvent.ACTION_MOVE && Begin
				&& event.getPointerCount() == 1
		// && twoPoint == 0
		) {
			this.X = event.getX();
			this.Y = event.getY();
			// this.mCanvas.drawColor(-232432);
			mCanvas.drawBitmap(bitmap, 0, 0, null);
			// canvasbg();
			// Bitmap bitmap=BitmapFactory.decodeResource(getResources(),
			// R.drawable.btn_bg_long_sqr_black_b);
			// canvas.drawBitmap(bitmap, 0, 0, null);
			// this.mCanvas.drawBitmap(bitmap, null, null);
			// this.mCanvas.drawBitmap(bitmap, 0, 0, null);
			for (int i = 0; i < mImagePathList.size() - 1; i++) {
				tempBitmap = pic[0].findByPiority(pic, i);
				// tempBitmap.matrix.preTranslate(0f, 0f);
				mCanvas.drawBitmap(tempBitmap.getPic(), tempBitmap.matrix, null);
			}
			tempBitmap = pic[0].findByPiority(pic, mImagePathList.size() - 1);
			rotalP = rotalPoint(new float[] { this.X, this.Y },
					tempBitmap.preX, tempBitmap.preY, tempBitmap.width / 2,
					tempBitmap.height / 2, tempBitmap.matrix);
			if ((Math.abs(X
					- pic[0].findByPiority(pic, mImagePathList.size() - 1)
							.getXY(1)) < pic[0].findByPiority(pic,
					mImagePathList.size() - 1).getWidth() / 2)
					&& (Math.abs(Y
							- pic[0].findByPiority(pic,
									mImagePathList.size() - 1).getXY(2)) < pic[0]
							.findByPiority(pic, mImagePathList.size() - 1)
							.getHeight() / 2)) {
				rotalC = this.getT(tempBitmap.width / 2, tempBitmap.height / 2,
						X + CX, Y + CY, tempBitmap.matrix);
				mCanvas.drawBitmap(tempBitmap.getPic(), tempBitmap.matrix, null);
				tempBitmap.preX = X + CX;
				tempBitmap.preY = Y + CY;
			} else {
				// tempBitmap.matrix.preTranslate(0f, 0f);
				mCanvas.drawBitmap(tempBitmap.getPic(), tempBitmap.matrix, null);
			}
		}
		// 两指移动
		if (event.getPointerCount() >= 2
				&& event.getAction() == MotionEvent.ACTION_MOVE) {
			twoPoint = 2;
			// this.mCanvas.drawColor(-232432);
			mCanvas.drawBitmap(bitmap, 0, 0, null);
			// canvasbg();
			invalidate();
			for (int i = 0; i < mImagePathList.size() - 1; i++) {
				tempBitmap = pic[0].findByPiority(pic, i);
				this.mCanvas.drawBitmap(tempBitmap.getPic(), tempBitmap.matrix,
						null);
			}
			tempBitmap = pic[0].findByPiority(pic, mImagePathList.size() - 1);
			rotalP = rotalPoint(new float[] { event.getX(0), event.getY(0) },
					tempBitmap.preX, tempBitmap.preY, tempBitmap.width / 2,
					tempBitmap.height / 2, tempBitmap.matrix);
			rotalP_2 = rotalPoint(new float[] { event.getX(1), event.getY(1) },
					tempBitmap.preX, tempBitmap.preY, tempBitmap.width / 2,
					tempBitmap.height / 2, tempBitmap.matrix);
			if ((Math.abs(rotalP[0]
					- pic[0].findByPiority(pic, mImagePathList.size() - 1)
							.getXY(1)) < pic[0].findByPiority(pic,
					mImagePathList.size() - 1).width / 2)
					&& (Math.abs(rotalP[1]
							- pic[0].findByPiority(pic,
									mImagePathList.size() - 1).getXY(2)) < pic[0]
							.findByPiority(pic, mImagePathList.size() - 1).height / 2)
					&& (Math.abs(rotalP_2[0]
							- pic[0].findByPiority(pic,
									mImagePathList.size() - 1).getXY(1)) < pic[0]
							.findByPiority(pic, mImagePathList.size() - 1).width / 2)
					&& (Math.abs(rotalP_2[1]
							- pic[0].findByPiority(pic,
									mImagePathList.size() - 1).getXY(2)) < pic[0]
							.findByPiority(pic, mImagePathList.size() - 1).height / 2)
					|| true) {
				if (bool) {
					preLength = spacing(event);
					preCos = cos(event);
					bool = false;
				}
				// 获取角度和长度
				length = spacing(event);
				cos = cos(event);
				// 放大和缩小
				if (length - preLength != 0) {
					// tempBitmap.width *= (1.0f + (length - preLength) /
					// length);
					// tempBitmap.height *= (1.0f + (length - preLength) /
					// length);
					float scale = length / preLength;
					tempBitmap.width *= scale;
					tempBitmap.height *= scale;
					tempBitmap.pic = Bitmap.createScaledBitmap(tempBitmap.pic,
							(int) tempBitmap.width, (int) tempBitmap.height,
							false);
					scale(tempBitmap.width / 2, tempBitmap.height / 2,
							tempBitmap.preX, tempBitmap.preY, tempBitmap.matrix);
				}

				// 旋转
				if (Math.abs(cos) > 3 && Math.abs(cos) < 177
						&& Math.abs(cos - preCos) < 15) {
					tempBitmap.matrix.postRotate(cos - preCos);
					this.getT(tempBitmap.width / 2, tempBitmap.height / 2,
							tempBitmap.preX, tempBitmap.preY, tempBitmap.matrix);
				}
				preCos = cos;
				preLength = length;
				// this.getT(tempBitmap.width / 2, tempBitmap.height / 2 ,
				// tempBitmap.preX, tempBitmap.preY, tempBitmap.matrix);

			}
			// 初始位置
			mCanvas.drawBitmap(tempBitmap.getPic(), tempBitmap.matrix, null);
		}

		if (event.getAction() == MotionEvent.ACTION_UP) {
			CX = 0f;
			CY = 0f;
			Begin = false;
		}
		invalidate();
		return true;
	}

	public void order(MotionEvent event) {

		Bmp temp = null;
		for (int i = mImagePathList.size() - 1; i >= 0; i--) {
			rotalP = rotalPoint(new float[] { event.getX(), event.getY() },
					pic[0].findByPiority(pic, i).preX,
					pic[0].findByPiority(pic, i).preY,
					pic[0].findByPiority(pic, i).width / 2,
					pic[0].findByPiority(pic, i).height / 2,
					pic[0].findByPiority(pic, i).matrix);
			if ((Math.abs(pic[0].findByPiority(pic, i).getXY(1) - rotalP[0]) < pic[0]
					.findByPiority(pic, i).width / 2)
					&& (Math.abs(pic[0].findByPiority(pic, i).getXY(2)
							- rotalP[1]) < pic[0].findByPiority(pic, i).height / 2)) {
				temp = pic[0].findByPiority(pic, i);
				for (Bmp bmp : pic) {
					if (bmp.getPriority() > pic[0].findByPiority(pic, i)
							.getPriority()) {
						bmp.priority--;
					}
				}
				temp.setPiority(mImagePathList.size() - 1);
				Begin = true;
				return;
			}
		}
	}

	// 添加白色边框
	public Bitmap add_rec(String pathName) {

		BitmapFactory.Options opt = new BitmapFactory.Options();
		opt.inPreferredConfig = Bitmap.Config.RGB_565;
		opt.inPurgeable = true;
		opt.inInputShareable = true;
		opt.inJustDecodeBounds = false;
		opt.inSampleSize = 10;
		// // 获取资源图片
		// Bitmap bitmap =BitmapFactory.decodeStream(getResources().
		// .openRawResource(R.drawable.abc),null,opt);
		Bitmap bitmap = BitmapFactory.decodeFile(pathName, opt);
		Bitmap output = Bitmap.createBitmap(bitmap.getWidth(),
				bitmap.getHeight(), Config.ARGB_8888);
		Canvas canvas = new Canvas(output);
		final Rect rect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());
		Paint paint = new Paint();
		// // 设置边框颜色
		paint.setColor(Color.WHITE);
		paint.setStyle(Paint.Style.STROKE);
		paint.setAntiAlias(true);
		// // 设置边框宽度
		paint.setStrokeWidth(12F);
		canvas.drawBitmap(bitmap, rect, rect, null);
		canvas.drawRect(rect, paint);

		return output;
	}

	public static void saveJPGE_After(Bitmap bitmap, String path) {
		File file = new File(path);
		try {
			FileOutputStream out = new FileOutputStream(file);
			if (bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out)) {
				out.flush();
				out.close();
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public float[] getT(float preX, float preY, float x, float y, Matrix matrix) {
		float[] re = new float[2];
		float[] matrixArray = new float[9];
		matrix.getValues(matrixArray);
		float a = x - preX * matrixArray[0] - preY * matrixArray[1];
		float b = y - preX * matrixArray[3] - preY * matrixArray[4];
		matrixArray[2] = a;
		matrixArray[5] = b;
		matrix.setValues(matrixArray);
		re[0] = a;
		re[1] = b;
		return re;
	}

	public void scale(float preX, float preY, float x, float y, Matrix matrix) {
		float[] matrixArray = new float[9];
		matrix.getValues(matrixArray);
		float a = x - preX;
		float b = y - preY;
		matrixArray[2] = a;
		matrixArray[5] = b;
		matrix.setValues(matrixArray);
	}

	public void setToO(Matrix matrix) {
		float[] matrixArray = new float[9];
		matrix.getValues(matrixArray);
		float a = 0f;
		float b = 0f;
		matrixArray[2] = a;
		matrixArray[5] = b;
		matrix.setValues(matrixArray);
	}

	public float[] rotalPoint(float[] p, float X, float Y, float width,
			float height, Matrix matrix) {
		float re[] = new float[2];
		float matrixArray[] = new float[9];
		matrix.getValues(matrixArray);
		float a = p[0] - X;
		float b = p[1] - Y;
		re[0] = a * matrixArray[0] - b * matrixArray[1] + X;
		re[1] = -a * matrixArray[3] + b * matrixArray[4] + Y;
		return re;
	}

	// 计算长度
	private float spacing(MotionEvent event) {
		float x = event.getX(0) - event.getX(1);
		float y = event.getY(0) - event.getY(1);
		return FloatMath.sqrt(x * x + y * y);
	}

	// 计算余弦
	private float cos(MotionEvent event) {
		if ((event.getX(0) - event.getX(1)) * (event.getY(0) - event.getY(1)) > 0) {
			return (float) ((float) Math.acos(Math.abs(event.getX(0)
					- event.getX(1))
					/ spacing(event))
					/ Math.PI * 180f);
		}
		if ((event.getX(0) - event.getX(1)) * (event.getY(0) - event.getY(1)) < 0) {
			return (float) ((float) Math.acos(-Math.abs(event.getX(0)
					- event.getX(1))
					/ spacing(event))
					/ Math.PI * 180f);
		}
		if (event.getX(0) - event.getX(1) == 0) {
			return (float) 90f;
		}
		if (event.getY(0) - event.getY(1) == 0) {
			return 0f;
		}
		return 45f;
	}
}

// @param pic:the Bitmap to draw
// @param piority: the order to draw picture
// @param preX,preY: the X and Y
class Bmp {
	// 构造器
	public Bmp(Bitmap pic, int piority) {
		this.pic = pic;
		this.priority = piority;
	}

	// 构造器
	public Bmp(Bitmap pic, int priority, float preX, float preY) {
		this(pic, priority);
		this.preX = preX + pic.getWidth() / 2 * 1.5f;
		this.preY = preY + pic.getHeight() / 2 * 1.5f;
	}

	// findPiority
	public Bmp findByPiority(Bmp[] pic, int priority) {
		for (Bmp p : pic) {
			if (p.priority == priority) {
				return p;
			}
		}
		return null;
	}

	// set Priority
	public void setPiority(int priority) {
		this.priority = priority;
	}

	// return Priority
	public int getPriority() {
		return this.priority;
	}

	// return X and Y
	public float getXY(int i) {
		if (i == 1) {
			return this.preX;
		} else if (i == 2) {
			return this.preY;
		}
		return (Float) null;
	}

	// getPicture
	public Bitmap getPic() {
		return this.pic;
	}

	// getWidth
	public float getWidth() {
		return width;
	}

	// getHeight
	public float getHeight() {
		return height;
	}

	float preX = 0;
	float preY = 0;
	float width = 0;
	float height = 0;
	Bitmap pic = null;
	int priority = 0;
	Matrix matrix = new Matrix();
}
