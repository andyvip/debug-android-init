package com.jrdcom.android.gallery3d.ui;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

import com.jrdcom.mt.MTActivity;
import com.jrdcom.mt.util.MyData;
import com.jrdcom.mt.core.BitmapUtil;
import com.jrdcom.mt.core.ToolCosmesis;
import com.jrdcom.mt.mtxx.controls.MtprogressDialog;
import com.jrdcom.android.gallery3d.R;
//PR936992 Undo and Redo icon are useless,while SD card is full add by limin.zhuo at 2015.03.06 begin
import com.jrdcom.example.joinpic.Utils;
//PR936992 Undo and Redo icon are useless,while SD card is full add by limin.zhuo at 2015.03.06 end
/**
 * 磨皮美白
 */
public class BeautyMopiActivity extends MTActivity {

	private RadioGroup radioGroup;// 3个状态栏的Group
	private ImageView showView;// 展示的View
	private SeekBar seekBar_beauty;// 拖拉杄1�7
	private RelativeLayout layout_seekBar;
	private int lable_Type = 0;// 0:磨皮 1：美癄1�7 2：肤艄1�7
	private Bitmap mBitmap;// 临时用于展示的bitmap
	private int[] size = { 50, 50, 50 };// 0:0.0~1.0 5:0~10 0:-1.0 ~ 1.0

	private ToolCosmesis m_tool; // 美容的操作功胄1�7
	private boolean isSaveing = false;
	private boolean isCancel = false;
	private PopupWindow barSizePopUpView = null;// 拖拉条的倄1�7
	private final int COLOR_PREVIEW_WIDTH = (int) (58 * MyData.nDensity);
	private final int COLOR_PREVIEW_HEIGHT = (int) (47 * MyData.nDensity);
	private TextView textThumbSize = null;
    private TextView txt_left = null;
    private TextView txt_right = null;
    private ImageView mTriangleView;
    private float[] smallLocations = {0f,0f,0f};
    private boolean isFocus = false;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.beauty_mopi);
		findView();
		initData();
		setListener();
		setFirstView();
	}

	private void findView() {
	    mTriangleView = (ImageView) findViewById(R.id.imageview_triangle);
		radioGroup = (RadioGroup) findViewById(R.id.bottom_menu);
		showView = (ImageView) findViewById(R.id.preview);
		layout_seekBar = (RelativeLayout) findViewById(R.id.layout_seekbar);
		((TextView) findViewById(R.id.tv_title)).setText(R.string.beauty_main_whitemopi);
	}

    private void initSmallImgaeLocation() {
        RadioButton rbmopi = (RadioButton) findViewById(R.id.mopilable);
        RadioButton rbmeibai = (RadioButton) findViewById(R.id.meibailable);
        RadioButton rbfuse = (RadioButton) findViewById(R.id.fuselable);
        smallLocations[0] = rbmopi.getX() + rbmopi.getWidth() / 2 - mTriangleView.getWidth()/2;
        smallLocations[1] = rbmeibai.getX() + rbmeibai.getWidth() / 2 - mTriangleView.getWidth()/2;
        smallLocations[2] = rbfuse.getX() + rbfuse.getWidth() / 2 - mTriangleView.getWidth()/2;
        mTriangleView.setX(smallLocations[0]);
        mTriangleView.setVisibility(View.VISIBLE);
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (!isFocus) {
            isFocus = true;
            initSmallImgaeLocation();
        }
    }

	// 初始化数捄1�7
	private void initData() 
	{
		//美图必备		磨皮美白工具的初始化
		m_tool = new ToolCosmesis();
		m_tool.init(MyData.getJNI());
		mBitmap = m_tool.getShowProcImage();
		
		showView.setImageBitmap(mBitmap);
		View view = View.inflate(BeautyMopiActivity.this, R.layout.mopi_seekbar, layout_seekBar);
		seekBar_beauty = (SeekBar) view.findViewById(R.id.seekbar_mopi);
		if (barSizePopUpView == null) {
			View popupview = View.inflate(BeautyMopiActivity.this, R.layout.seekbar_popupview, null);
			textThumbSize = (TextView) popupview.findViewById(R.id.txt_size);
			barSizePopUpView = new PopupWindow(popupview, COLOR_PREVIEW_WIDTH, COLOR_PREVIEW_HEIGHT);
		}
	}

	private void setListener() {
		radioGroup.setOnCheckedChangeListener(new CheckedChange());
		 findViewById(R.id.btn_ok).setOnClickListener(new OnClickListenerSave());
		 findViewById(R.id.btn_cancel).setOnClickListener(new OnClickListenerDel());
		seekBar_beauty.setOnSeekBarChangeListener(new OnSeekBarChangeListenerBeauty());
	}

	Handler mHandler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case 1:
				//美图必备		获取处理后的UI效果图并显示
				showView.setImageBitmap(m_tool.getShowProcImage());
				break;
			}
			super.handleMessage(msg);
		}

	};

	private void setFirstView() {
		new MtprogressDialog(this, true, this.getString(R.string.mopi_progess_title)) {
			@Override
			public void process() {
				try {
					float arr[] = new float[4];
					arr[0] = 13;
					arr[1] = size[1] / 10;
					arr[2] = (size[2] - 50) / 50.0f;
					arr[3] = size[0] * 0.01f;
					m_tool.procImage(arr, 4, true);
					
					
					Message message = new Message();
					message.what = 1;
					mHandler.sendMessage(message);
				} catch (Exception e) {
				}
			}
		}.show();
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			if (!isCancel) {
				isCancel = true;
				new MtprogressDialog(BeautyMopiActivity.this) {
					@Override
					public void process() {
						//美图必备		手机上返回键的调用
						m_tool.cancel();
						finish();
					}
				}.show();
			}
			return true;
		} else if (keyCode == KeyEvent.KEYCODE_MENU) {
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		showView.setImageBitmap(null);
		showView = null;
		BitmapUtil.SafeRelease(mBitmap);
	}

	private class CheckedChange implements OnCheckedChangeListener {

		@Override
		public void onCheckedChanged(RadioGroup group, int checkedId) {
			switch (checkedId) {
			case R.id.mopilable:
				lable_Type = 0;
				mTriangleView.setX(smallLocations[0]);
				layout_seekBar.removeAllViews();
				View view = View.inflate(BeautyMopiActivity.this, R.layout.mopi_seekbar, layout_seekBar);
				seekBar_beauty = (SeekBar) view.findViewById(R.id.seekbar_mopi);
				seekBar_beauty.setProgress(size[0]);
				seekBar_beauty.setOnSeekBarChangeListener(new OnSeekBarChangeListenerBeauty());
				if (barSizePopUpView != null) {
					barSizePopUpView.dismiss();
				}
				break;
			case R.id.meibailable:
				lable_Type = 1;
				mTriangleView.setX(smallLocations[1]);
				layout_seekBar.removeAllViews();
				View view1 = View.inflate(BeautyMopiActivity.this, R.layout.mopi_seekbar, layout_seekBar);
				seekBar_beauty = (SeekBar) view1.findViewById(R.id.seekbar_mopi);
				seekBar_beauty.setProgress(size[1]);
				seekBar_beauty.setOnSeekBarChangeListener(new OnSeekBarChangeListenerBeauty());
				if (barSizePopUpView != null) {
					barSizePopUpView.dismiss();
				}

				break;
			case R.id.fuselable:
				lable_Type = 2;
				mTriangleView.setX(smallLocations[2]);
				layout_seekBar.removeAllViews();
				View view2 = View.inflate(BeautyMopiActivity.this, R.layout.fuse_seekbar, layout_seekBar);
				seekBar_beauty = (SeekBar) view2.findViewById(R.id.seekbar_fuse);
				seekBar_beauty.setProgress(size[2]);
				seekBar_beauty.setOnSeekBarChangeListener(new OnSeekBarChangeListenerBeauty());
				if (barSizePopUpView != null) {
					barSizePopUpView.dismiss();
				}
				break;
			}
		}
	}

	private class OnTouchListenerContrast implements OnTouchListener {

		@Override
		public boolean onTouch(View v, MotionEvent event) {
			
			//美图必备		这边是处理对比按钮的，一个是UI显示原始图片，一个是UI显示处理图片
			if (event.getAction() == MotionEvent.ACTION_DOWN) {
				mBitmap = m_tool.getShowOralImage();
				showView.setImageBitmap(mBitmap);
			}
			if (event.getAction() == MotionEvent.ACTION_UP) {
				mBitmap = m_tool.getShowProcImage();
				showView.setImageBitmap(mBitmap);
			}
			return false;
		}

	}

	// 保存按钮
	private class OnClickListenerSave implements OnClickListener {

		@Override
		public void onClick(View v) {
                   //PR936992 Undo and Redo icon are useless,while SD card is full add by limin.zhuo at 2015.03.06 begin
                   if (!Utils.updateCacheDirEditPicture()) {
                       Utils.showToast(BeautyMopiActivity.this, R.string.storage_full_tag);
                   }
                   //PR936992 Undo and Redo icon are useless,while SD card is full add by limin.zhuo at 2015.03.06 end
            isClicked = true; // added by jipu.xiong@tcl.com
			toMain();
		}
	}

	// 逄1�7出按钄1�7
	private class OnClickListenerDel implements OnClickListener {

		@Override
		public void onClick(View v) {
		    isClicked = true; // added by jipu.xiong@tcl.com
			// 逄1�7出调甄1�7
			if (!isCancel) {
				isCancel = true;
				new MtprogressDialog(BeautyMopiActivity.this) {
					@Override
					public void process() {
						//美图必备		返回功能的调用
						m_tool.cancel();
						
						finish();
					}
				}.show();

			}
		}
	}

	// 保存图片回到主界靄1�7
	public void toMain() {
		// 没有任何操作，直接�1�7�1�7凄1�7
		new MtprogressDialog(this, true) {
			@Override
			public void process() {
				if (!isSaveing) {
					try {
						isSaveing = true;
						
						//美图必备		点击保存的函数调用
						if (m_tool.isProcessed()) {
							m_tool.ok();
							MyData.getBeautyControl().pushImage();
						}
						else
						{
							m_tool.cancel();
						}
						
					} catch (Exception e) {
						e.printStackTrace();
					} finally {
						BeautyMopiActivity.this.finish();
						isSaveing = false;
					}
				}
			}
		}.show();
	}

	private class OnSeekBarChangeListenerBeauty implements OnSeekBarChangeListener {
		@Override
		public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

			if (barSizePopUpView.isShowing()) {
				// barSizePopUpView.update(layout_seekBar, (int) ((30 *
				// MyData.nDensity) + (MyData.nScreenW - 60
				// * MyData.nDensity - barSizePopUpView.getWidth())
				// / (seekBar.getMax()) * progress),
				// -(layout_seekBar.getHeight() * 2 / 3 + COLOR_PREVIEW_HEIGHT),
				// -1, -1);
				// 间隔30+padding的1�7+thumb的半径1�7-popview的宽/2
				barSizePopUpView
						.update(layout_seekBar,
								(int) ((((30 + 18) * MyData.nDensity) - COLOR_PREVIEW_WIDTH / 2) + (MyData.nScreenW - 96 * MyData.nDensity)
										/ (seekBar.getMax()) * progress),
								-(layout_seekBar.getHeight() * 2 / 3 + COLOR_PREVIEW_HEIGHT), -1, -1);
			} else {
				barSizePopUpView
						.showAsDropDown(
								layout_seekBar,
								(int) ((((30 + 18) * MyData.nDensity) - COLOR_PREVIEW_WIDTH / 2) + (MyData.nScreenW - 96 * MyData.nDensity)
										/ (seekBar.getMax()) * progress),
								-(layout_seekBar.getHeight() * 2 / 3 + COLOR_PREVIEW_HEIGHT));
			}
			if (lable_Type == 2) {// 肤色 -50 -50
				if (textThumbSize != null) {
					textThumbSize.setText((progress - 50) + "");
				}
			} else {
				if (textThumbSize != null) {// 0 -100
					textThumbSize.setText(progress + "");
				}
			}

		}

		@Override
		public void onStartTrackingTouch(SeekBar seekBar) {

		}

		@Override
		public void onStopTrackingTouch(SeekBar seekBar) {
			size[lable_Type] = seekBar_beauty.getProgress();
			setView();
			barSizePopUpView.dismiss();
		}
	}

	private void setView() {
		new MtprogressDialog(this) {
			@Override
			public void process() {
				try {
					
					//美图必备		磨皮美白的处理函数
					float arr[] = new float[4];
					arr[0] = 13;
					arr[1] = size[1] / 10.0f;
					arr[2] = (size[2] - 50) / 50.0f;
					arr[3] = size[0] * 0.01f;
					m_tool.procImage(arr, 4, true);
					
					
					Message message = new Message();
					message.what = 1;
					mHandler.sendMessage(message);
				} catch (Exception e) {
				}
			}
		}.show();
	}

}
