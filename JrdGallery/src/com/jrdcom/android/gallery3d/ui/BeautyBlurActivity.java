package com.jrdcom.android.gallery3d.ui;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

import com.jrdcom.android.gallery3d.R;
//PR936992 Undo and Redo icon are useless,while SD card is full add by limin.zhuo at 2015.03.06 begin
import com.jrdcom.example.joinpic.Utils;
//PR936992 Undo and Redo icon are useless,while SD card is full add by limin.zhuo at 2015.03.06 end
import com.jrdcom.mt.MTActivity;
import com.jrdcom.mt.core.ToolBlur;
import com.jrdcom.mt.mtxx.controls.MtprogressDialog;
import com.jrdcom.mt.widget.ViewEditWeak;
/**
 * 虚化
 */
public class BeautyBlurActivity extends MTActivity implements OnClickListener,
        OnSeekBarChangeListener,com.jrdcom.mt.widget.ViewEditWeak.Listener{

    private ImageView mPreview;
    private ViewEditWeak mViewEditWeak;
    private SeekBar mRangeSeekBar;
    private SeekBar mSizeSeekBar;
    private RadioButton mCircleRadioButton;
    private RadioButton mLineRadioButton;
    private int mLineProgress;
    private int mCircleProgress;
    private int mLineSizeProgress;
    private int mCircleSizeProgress;
    private boolean mFromUser = false;
    private ImageView mTriangleView;
    private float[] smallLocations = {0f,0f};
    private boolean isFocus = false;

    //yaogang.hao for PR  delete old operation logic
    // 美图必备
//    private ToolBlur m_tool;

    // 自定义Handler 继随自Handler，需要实现handleMessage方法
    Handler blurHandler = new Handler() {
        public void handleMessage(Message msg)
        {
            //yaogang.hao for PR  delete old operation logic
            // 美图必备 刷新
//            mPreview.setImageBitmap(m_tool.getShowProcImage());

            super.handleMessage(msg);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        setContentView(R.layout.beauty_blur);
        findView();
        mLineProgress = 100;
        mCircleProgress = 100;
        mLineSizeProgress = 128;
        mCircleSizeProgress = 128;
    }

    @Override
    public void onClick(View v) {
        isClicked = true; // added by jipu.xiong@tcl.com
        switch (v.getId()) {
            case R.id.btn_ok:
                //PR936992 Undo and Redo icon are useless,while SD card is full add by limin.zhuo at 2015.03.06 begin
                if (!Utils.updateCacheDirEditPicture()) {
                    Utils.showToast(this, R.string.storage_full_tag);
                }
                //PR936992 Undo and Redo icon are useless,while SD card is full add by limin.zhuo at 2015.03.06 end
                onOK();
                break;
            case R.id.btn_cancel:
                onCancel();
                break;
            case R.id.rbtn_circle:
                mTriangleView.setX(smallLocations[0]);
                mViewEditWeak.setType(0);
                mViewEditWeak.isDown(true);
                mViewEditWeak.startAnimation();
                mSizeSeekBar.setProgress(mCircleSizeProgress);
                mRangeSeekBar.setProgress(mCircleProgress);
                break;
            case R.id.rbtn_line:
                mTriangleView.setX(smallLocations[1]);
                mViewEditWeak.setType(1);
                mCircleRadioButton.setChecked(false);
                mViewEditWeak.isDown(true);
                mViewEditWeak.startAnimation();
                mSizeSeekBar.setProgress(mLineSizeProgress);
                mRangeSeekBar.setProgress(mLineProgress);
                break;
            default:
                break;
        }

    }

    private void findView() {
        mTriangleView = (ImageView) findViewById(R.id.imageview_triangle);
        mViewEditWeak = (ViewEditWeak) findViewById(R.id.view_styleemptiness);
        mRangeSeekBar = (SeekBar) findViewById(R.id.emptiness_range);
        mSizeSeekBar = (SeekBar) findViewById(R.id.emptiness_size);
        mCircleRadioButton = (RadioButton) findViewById(R.id.rbtn_circle);
        mLineRadioButton = (RadioButton) findViewById(R.id.rbtn_line);
        mCircleRadioButton.setOnClickListener(this);
        mLineRadioButton.setOnClickListener(this);
        mViewEditWeak.setOnSizeChangeLisenter(this);
        mRangeSeekBar.setOnSeekBarChangeListener(this);
        mSizeSeekBar.setOnSeekBarChangeListener(this);
        findViewById(R.id.btn_ok).setOnClickListener(this);
        findViewById(R.id.btn_cancel).setOnClickListener(this);
        ((TextView) findViewById(R.id.label_top_bar_title)).setText(R.string.mainmenu_weak);
        mViewEditWeak.setType(0);
        mViewEditWeak.isDown(true);
        mViewEditWeak.startAnimation();
        mViewEditWeak.refresh();
        mCircleRadioButton.requestFocus();
        mCircleRadioButton.setChecked(true);
    }
    
    private void initSmallImgaeLocation() {
        smallLocations[0] = mCircleRadioButton.getX() + mCircleRadioButton.getWidth() / 2 - mTriangleView.getWidth() / 2;
        smallLocations[1] = mLineRadioButton.getX() + mLineRadioButton.getWidth() / 2 - mTriangleView.getWidth()
                / 2;
        mTriangleView.setX(smallLocations[0]);
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (!isFocus) {
            isFocus = true;
            initSmallImgaeLocation();
        }
    }

    @Override
    public void onSizeChanged(int size){
        if(mSizeSeekBar != null)
        mSizeSeekBar.setProgress((int)((size-1)/1.4));
   }
    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        switch (seekBar.getId()) {
            case R.id.emptiness_range:
                mViewEditWeak.setOutRadius((int)(progress*0.6+3),true,false);
                if(fromUser){
                    mViewEditWeak.isDown(true);
                    mViewEditWeak.setAlpha();
                }
                mViewEditWeak.invalidate();
                break;
            case R.id.emptiness_size:
                mViewEditWeak.setInRadius((float) (progress * 1.4 + 1), true, false);
                if(fromUser){
                    mViewEditWeak.isDown(true);
                    mViewEditWeak.setAlpha();
                }
                mViewEditWeak.invalidate();
                break;

            default:
                break;
        }
        mFromUser = fromUser;
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        switch (seekBar.getId()) {
            case R.id.emptiness_range:
                if(1==mViewEditWeak.getType()){
                    mLineProgress = seekBar.getProgress();
                }else {
                    mCircleProgress = seekBar.getProgress();
                }
                break;
            case R.id.emptiness_size:
                if(1==mViewEditWeak.getType()){
                    mLineSizeProgress = seekBar.getProgress();
                }else {
                    mCircleSizeProgress = seekBar.getProgress();
                }
                break;
            default:
                break;
        }
        mViewEditWeak.reWeak(true);
        mViewEditWeak.invalidate();
        if (mFromUser) {
            mViewEditWeak.isDown(false);
            mViewEditWeak.setAlpha();
            mViewEditWeak.invalidate();
        }
    }

    // 保存
    private void onOK() {
        //yaogang.hao for PR 547506
        if(!mViewEditWeak.isAnimation()
                || mViewEditWeak.isSaving())
            return;
        
        new MtprogressDialog(this) {
            @Override
            public void process() {
                try {

                    // 美图必备
                    // 确定操作
//                    m_tool.ok();
//                    // 这个目前没用
//                    MyData.getBeautyControl().pushImage();
                    mViewEditWeak.onSave();

                    finish();
                } catch (Exception e) {
                }
            }
        }.show();
    }

    // 取消
    private void onCancel() {
        // 美图必备
        // 取消操作
//        m_tool.cancel();
        mViewEditWeak.onCancel();
        finish();
    }

    // 圆形1
    private void doCircle1() {
        new MtprogressDialog(this) {
            @Override
            public void process() {
                try {
                    //yaogang.hao for PR  delete old operation logic
                    // 美图必备
                    // 在图片(1/4,1/3)处，半径为100,200的圆形虚化
//                    int val[] = new int[2];
//                    val = m_tool.getShowImageSize();
//                    m_tool.procRadiusDealPic(val[0] / 4, val[1] / 3, 100, 200);
//
//                    Message message = new Message();// 发送消息来刷新
//                    blurHandler.sendMessage(message);

                } catch (Exception e) {
                }
            }
        }.show();
    }

    // 圆形2
    private void doCircle2() {
        new MtprogressDialog(this) {
            @Override
            public void process() {
                try {
                    //yaogang.hao for PR  delete old operation logic
                    // 美图必备
                    // 在图片(3/4,2/3)处，半径为150,180的圆形虚化
//                    int val[] = new int[2];
//                    val = m_tool.getShowImageSize();
//                    m_tool.procRadiusDealPic(val[0] * 3 / 4, val[1] * 2 / 3, 150, 180);
//
//                    Message message = new Message();// 发送消息来刷新
//                    blurHandler.sendMessage(message);

                } catch (Exception e) {
                }
            }
        }.show();
    }

    float m_angle1 = 0.0f;
    float m_angle2 = 10.0f;

    // 直线1
    private void doLine1() {
        new MtprogressDialog(this) {
            @Override
            public void process() {
                try {
                    //yaogang.hao for PR  delete old operation logic
//                    // 美图必备
//                    // 在图片(1/2,1/2)处半径为10,50,角度为m_angle1的直线虚化
//                    int nInRadius = 10;
//                    int nOutRadius = 50;
//                    // 获取UI显示的图片的大小
//                    int val[] = new int[2];
//                    val = m_tool.getShowImageSize();
//
//                    int x = val[0] / 2;
//                    int y = val[1] / 2;
//                    float angle = m_angle1;
//                    m_tool.procLineDealPic(x, y, angle, nInRadius, nOutRadius);
//
//                    // 测试用
//                    m_angle1 += 20;
//
//                    Message message = new Message();// 发送消息来刷新
//                    blurHandler.sendMessage(message);

                } catch (Exception e) {
                }
            }
        }.show();

    }

    // 直线2
    private void doLine2() {
        new MtprogressDialog(this) {
            @Override
            public void process() {
                try {
                    //yaogang.hao for PR  delete old operation logic
//                    // 美图必备
//                    // 在图片(1/2,1/2)处半径为10,50,角度为m_angle2的直线虚化
//                    int nInRadius = 10;
//                    int nOutRadius = 50;
//
//                    int val[] = new int[2];
//                    val = m_tool.getShowImageSize();
//
//                    int x = val[0] / 2;
//                    int y = val[1] / 2;
//
//                    float angle = m_angle2;
//                    m_tool.procLineDealPic(x, y, angle, nInRadius, nOutRadius);
//
//                    // 测试用
//                    m_angle2 += 20;
//
//                    Message message = new Message();// 发送消息来刷新
//                    blurHandler.sendMessage(message);

                } catch (Exception e) {
                }
            }
        }.show();
    }

    // 物理按键监听
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            // 按下返回键

        }
        return super.onKeyDown(keyCode, event);
    }


}
