package com.jrdcom.example.joinpic;

import java.util.ArrayList;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Bitmap.Config;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ScrollView;
import com.jrdcom.android.gallery3d.R;

class JoinView extends View {
	public ArrayList<String> pathlist = new ArrayList<String>();
	int cur_x;
	int cur_y;

	public JoinView(Context context) {
		super(context);
	}

	public JoinView(Context context, String[] path) {
		super(context);
//		this.path = path;
	}

	public JoinView(Context context, String[] path, int width, int height) {
		super(context);
		pathlist=ablum_main.pathlist;
	}

	public JoinView(Context context, AttributeSet attrs) {
		super(context, attrs);
		pathlist=ablum_main.pathlist;
		// TODO Auto-generated constructor stub
	}

	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
//		BitmapFactory.Options opt = new BitmapFactory.Options();
//		opt.inPreferredConfig = Bitmap.Config.RGB_565;
//		opt.inPurgeable = true;
//		opt.inInputShareable = true;
//		opt.inJustDecodeBounds = false;
//		opt.inSampleSize = 10;
//		int height_sum = border;
//		Bitmap pic[] = new Bitmap[pathlist.size()];
//		for (int i = 0; i < pic.length; i++) {
//			// if(pic[i].getWidth())
//			width = BitmapFactory.decodeFile(pathlist.get(i)).getWidth();
//			heigh = BitmapFactory.decodeFile(pathlist.get(i)).getHeight();
//			float k = (float) width / 460;
//			float j = (float) heigh / 768;
//
//			pic[i] = Bitmap.createScaledBitmap(
//					BitmapFactory.decodeFile(pathlist.get(i)),
//					(int) (width / k), (int) (heigh / j), true);
//			height_sum += pic[i].getHeight() + border;
//		}
//		Bitmap new_pic = Bitmap.createBitmap(width + border * 2, height_sum,
//				Config.ARGB_8888);
//		Canvas _reCanvas = new Canvas(new_pic);
//		_reCanvas.drawColor(-1);
//		for (int i = 0; i < pic.length; i++) {
//			_reCanvas.drawBitmap(pic[i], border, heigh * i + border * i
//					+ border, null);
//			pic[i].recycle();
//		}
//		Matrix new_pic_Matrix = canvas.getMatrix();
//		canvas.setMatrix(new_pic_Matrix);
//		canvas.drawBitmap(new_pic, 0, 65, null);
		
        int height_sum = border;
        Bitmap pic[] = new Bitmap[pathlist.size()];
        for (int i = 0; i < pic.length; i++)
        {
            pic[i] = Bitmap.createScaledBitmap(BitmapFactory.decodeFile(pathlist.get(i)), width, heigh, true); 
            height_sum += pic[i].getHeight() + border;
        }
        Bitmap new_pic = Bitmap.createBitmap(width + border * 2, height_sum, Config.ARGB_8888);  
        Canvas _reCanvas = new Canvas(new_pic);
        _reCanvas.drawColor(-1);
        for (int i = 0; i < pic.length; i++)
        {
            _reCanvas.drawBitmap(pic[i], border, heigh * i + border * i + border, null);
            pic[i].recycle();
        }
        Matrix new_pic_Matrix = canvas.getMatrix();
        new_pic_Matrix.postScale(200.0f / (float)width, 200.0f / (float)width);
        canvas.setMatrix(new_pic_Matrix);
        canvas.drawBitmap(new_pic, 0, 150, null);

	}

	public boolean onTouchEvent(MotionEvent event) {
		// TODO Auto-generated method stub
		switch (event.getAction()) {
		case MotionEvent.ACTION_DOWN:
			cur_x = (int) event.getX();
			cur_y = (int) event.getY();
			break;
		case MotionEvent.ACTION_MOVE:
			int x = (int) event.getX();
			int y = (int) event.getY();
			scrollTo(0, cur_y - y);
//			cur_x = x;
//			cur_y = y;
//			moveView(0,cur_y);
			invalidate(); 
			break;
		}
		return true;
	}

	public void scrollTo(int x, int y) {
		// TODO Auto-generated method stub
		super.scrollTo(x, y);
//		requestLayout();
	}


//	String path1 = "/mnt/sdcard/1.jpg";
//	String path2 = "/mnt/sdcard/2.jpg";
//	String path3 = "/mnt/sdcard/3.jpg";
	// private String[] path = {path1,path2,path3};
	// private String[] path = { path1, path2 };
//	private String[] path = { path1 };
	private int width=480;
	private int heigh=480;
	private int border = 10;

}
