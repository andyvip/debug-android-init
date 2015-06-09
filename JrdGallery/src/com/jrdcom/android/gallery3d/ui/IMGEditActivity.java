
package com.jrdcom.android.gallery3d.ui;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

import com.jrdcom.android.gallery3d.R;
//PR936992 Undo and Redo icon are useless,while SD card is full add by limin.zhuo at 2015.03.06 begin
import com.jrdcom.example.joinpic.Utils;
//PR936992 Undo and Redo icon are useless,while SD card is full add by limin.zhuo at 2015.03.06 end
import com.jrdcom.mt.MTActivity;
import com.jrdcom.mt.core.ToolSharp;
import com.jrdcom.mt.mtxx.controls.MtprogressDialog;
import com.jrdcom.mt.util.MyData;
import com.jrdcom.mt.widget.ViewEditCut;
import com.jrdcom.mt.widget.ViewEditRotate;

//PR857320 modify Can not use Scale Free operation by fengke at 2014.12.02 start 
import android.widget.Toast;
//PR857320 modify Can not use Scale Free operation by fengke at 2014.12.02 end 

/**
 * 磨皮美白
 */
public class IMGEditActivity extends MTActivity implements RadioGroup.OnCheckedChangeListener,
        OnClickListener, OnSeekBarChangeListener, CompoundButton.OnCheckedChangeListener, 
        OnTouchListener {

    private RadioGroup radioGroup;// 3个状态栏的Group
    private RadioButton rbtn_edit_cut;
    private RadioButton rbtn_edit_rotate;
    private RadioButton rbtn_edit_sharp;
    private FrameLayout bottom_sub_men;
    private TextView title = null;
    private ImageButton cancle_btn = null;
    private ImageButton ok_btn = null;
    private Bitmap mBitmap = null;
    private ToolSharp m_tool;
    private int editnum = 0;

    // 锐化
    private ImageView imageview_editsharp = null;
    private SeekBar seekbar_sharp = null;
    private int nOrignalSlider = -1;
    private boolean mIsSharp = false;

    // 旋转
    private ViewEditRotate rotate_view = null;
    private float leftFromDegree = 0;
    private float leftToDegree = -90;
    private float rightFromDegree = 0;
    private float rightToDegree = 90;
    private boolean isSliderRotate = false;
    private Button rotateLeft;
    private Button rotateRight;
    private Button rotateHorizontal;
    private Button rotateVertical;
    private PopupWindow mPopupWindow;
    private Button mCutButton_1_1;
    private Button mCutButton_3_2;
    private Button mCutButton_4_3;
    private Button mCutButton_2_3;
    private Button mCutButton_3_4;
    private Button mCutButton_16_9;
    private Button mCutFreeButton;
    private Button mFreeResetButton;
    private CheckBox mFreeRotateeBox;
    private LinearLayout mFreeLayout;
    private SeekBar mFreeRotateSeekBar;
    private VelocityTracker mVelocityTracker;
    private float mkeyMove = 0f;
    private float newvalus = 0f;
    private float oldValus = 0f;
    private boolean mFreeMove = false;
    private int mFreeStart = 0;
    private boolean mFreeStop = false;
    private boolean isFreeRotate = false;
    private float Diatavalus;
    private CheckBox edit_cut_scale;
    private Button cut_reset_bt;
    
    //cut
    private ViewEditCut cut_view;
    private ImageView mTriangleView;
    private float[] smallLocations = {0f,0f,0f};
    private boolean isFocus = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_cut);
        m_tool = new ToolSharp();
        m_tool.init(MyData.getJNI());
        mBitmap = m_tool.getShowProcImage();
        nOrignalSlider = 0;
        findView();
        initPopWindow();

    }

    private void findView() {
        mTriangleView = (ImageView) findViewById(R.id.imageview_triangle);
        radioGroup = (RadioGroup) findViewById(R.id.bottom_menu);
        rbtn_edit_cut = (RadioButton) findViewById(R.id.rbtn_edit_cut);
        rbtn_edit_rotate = (RadioButton) findViewById(R.id.rbtn_edit_rotate);
        rbtn_edit_sharp = (RadioButton) findViewById(R.id.rbtn_edit_sharp);
        title = (TextView) findViewById(R.id.label_top_bar_title);
        cancle_btn = (ImageButton) findViewById(R.id.btn_cancel);
        ok_btn = (ImageButton) findViewById(R.id.btn_ok);
        bottom_sub_men = (FrameLayout) findViewById(R.id.bottom_sub_men);
        cancle_btn.setOnClickListener(this);
        ok_btn.setOnClickListener(this);
        radioGroup.setOnCheckedChangeListener(this);
        radioGroup.check(R.id.rbtn_edit_cut);
        title.setText(R.string.edit_tailor);
    }
    
    private void initSmallImgaeLocation(){
        smallLocations[0] = rbtn_edit_cut.getX()+rbtn_edit_cut.getWidth()/2-mTriangleView.getWidth()/2;
        smallLocations[1] = rbtn_edit_rotate.getX()+rbtn_edit_rotate.getWidth()/2-mTriangleView.getWidth()/2;
        smallLocations[2] = rbtn_edit_sharp.getX()+rbtn_edit_sharp.getWidth()/2-mTriangleView.getWidth()/2;
        mTriangleView.setX(smallLocations[0]);
    }
    
    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if(!isFocus)
        {
            isFocus = true;
            initSmallImgaeLocation();
        }
    }
    
    private void initPopWindow() {
        View PopView =
                getLayoutInflater().inflate(R.layout.activity_edit_cut_scale, null);
        mPopupWindow = new PopupWindow(PopView);
        mPopupWindow.setWindowLayoutMode(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        mPopupWindow.setAnimationStyle(R.style.cutscale_popwindow_anim_style);
        mPopupWindow.setOutsideTouchable(false);
        mPopupWindow.getContentView().measure(0,0);
        mCutButton_1_1 = (Button) PopView.findViewById(R.id.cut_1_1);
        mCutButton_3_2 = (Button) PopView.findViewById(R.id.cut_3_2);
        mCutButton_4_3 = (Button) PopView.findViewById(R.id.cut_4_3);
        mCutButton_2_3 = (Button) PopView.findViewById(R.id.cut_2_3);
        mCutButton_3_4 = (Button) PopView.findViewById(R.id.cut_3_4);
        mCutButton_16_9 = (Button) PopView.findViewById(R.id.cut_16_9);
      //PR484389-lilei-begin
        //mCutFreeButton = (Button) PopView.findViewById(R.id.cut_free);  
        mCutButton_1_1.setOnClickListener(this);
        mCutButton_3_2.setOnClickListener(this);
        mCutButton_4_3.setOnClickListener(this);
        mCutButton_2_3.setOnClickListener(this);
        mCutButton_3_4.setOnClickListener(this);
        mCutButton_16_9.setOnClickListener(this);
        //mCutFreeButton.setOnClickListener(this);
        //PR484389-lilei-end
    }

    private void getPopWindow(View v) {
        if (mPopupWindow != null) {
            if (mPopupWindow.isShowing()) {
                mPopupWindow.dismiss();
            } else {
                WindowManager wm = (WindowManager) this.getSystemService(Context.WINDOW_SERVICE);
                float width = edit_cut_scale.getLeft()+edit_cut_scale.getWidth()/2-mPopupWindow.getContentView().getMeasuredWidth()/2;
                int height = wm.getDefaultDisplay().getHeight();
                mPopupWindow.showAtLocation((View) (v.getParent()), Gravity.BOTTOM|Gravity.LEFT, (int)(width), (int)(height * 0.24f));//PR624970-taoli-001
            }
        }
    }

    @Override
    public void onCheckedChanged(RadioGroup group, int checkedId) {
        // TODO Auto-generated method stub
        switch (checkedId) {
            case R.id.rbtn_edit_cut:
                editnum = 0;
                title.setText(R.string.edit_tailor);
                mTriangleView.setX(smallLocations[0]);
                bottom_sub_men.removeAllViews();
                View view = View.inflate(IMGEditActivity.this, R.layout.layout_fragment_img_cut,
                        bottom_sub_men);
                cut_view=(ViewEditCut)view.findViewById(R.id.view_editcut);
                cut_reset_bt = (Button) view.findViewById(R.id.cut_reset);
                edit_cut_scale = (CheckBox) view.findViewById(R.id.edit_cut_scale);
                Button edit_cut_finish_bt=(Button)view.findViewById(R.id.edit_cut_finish);
                cut_reset_bt.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        // TODO Auto-generated method stub
                        cut_view.isOkCut(false);
                        cut_view.reset();
                    }

                });

                edit_cut_scale.setOnClickListener(new OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        // TODO Auto-generated method stub
                        cut_view.isOkCut(false);
                        getPopWindow(v);
                    }

                });
                
                edit_cut_finish_bt.setOnClickListener(new OnClickListener(){

                    @Override
                    public void onClick(View v) {
                        // TODO Auto-generated method stub
                        cut_view.isOkCut(true);
                    }
                    
                });
                break;
            // 旋转
            case R.id.rbtn_edit_rotate:
                if (mPopupWindow.isShowing()) {
                    mPopupWindow.dismiss();
                }
                editnum = 1;
                mTriangleView.setX(smallLocations[1]);
                title.setText(R.string.edit_spin);
                bottom_sub_men.removeAllViews();
                View view1 = View.inflate(IMGEditActivity.this,
                        R.layout.layout_fragment_img_rotate, bottom_sub_men);
                rotate_view = (ViewEditRotate) view1.findViewById(R.id.view_editrotate);
                rotateLeft = (Button) view1.findViewById(R.id.rotateLeft);
                rotateRight = (Button) view1.findViewById(R.id.rotateRight);
                rotateHorizontal = (Button) view1.findViewById(R.id.rotateHorizontal);
                rotateVertical = (Button) view1.findViewById(R.id.rotateVertical);
                mFreeRotateeBox = (CheckBox) view1.findViewById(R.id.freerotate);
                mFreeRotateeBox.setChecked(false);
                mFreeLayout = (LinearLayout) view1.findViewById(R.id.rotateFreeview);
                mFreeRotateSeekBar = (SeekBar) view1.findViewById(R.id.seekbar_rotate);
                mFreeResetButton = (Button) view1.findViewById(R.id.btn_rotatefree_reset);
                rotateLeft.setOnClickListener(this);
                rotateRight.setOnClickListener(this);
                rotateHorizontal.setOnClickListener(this);
                rotateVertical.setOnClickListener(this);
                mFreeRotateeBox.setOnCheckedChangeListener(this);
                mFreeRotateSeekBar.setOnSeekBarChangeListener(this);
                mFreeRotateSeekBar.setOnTouchListener(this);
                mFreeResetButton.setOnClickListener(this);
                break;
            // 锐化
            case R.id.rbtn_edit_sharp:
                if (mPopupWindow.isShowing()) {
                    mPopupWindow.dismiss();
                }
                m_tool.init(MyData.getJNI());//xiaodaijun PR667447 add
                editnum = 2;
                mTriangleView.setX(smallLocations[2]);
                title.setText(R.string.edit_sharpen);
                bottom_sub_men.removeAllViews();
                View view2 = View.inflate(IMGEditActivity.this, R.layout.layout_fragment_img_sharp,
                        bottom_sub_men);
                imageview_editsharp = (ImageView) view2.findViewById(R.id.imageview_editsharp);
                seekbar_sharp = (SeekBar) view2.findViewById(R.id.seekbar_sharp);
                seekbar_sharp.setOnSeekBarChangeListener(this);
                imageview_editsharp.setImageBitmap(mBitmap);
                break;
            default:
                break;
        }

    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        switch (buttonView.getId()) {
            case R.id.freerotate:
                if(isChecked){
                    mFreeLayout.setVisibility(View.VISIBLE);
                } else {
                    mFreeLayout.setVisibility(View.GONE);
                }
                break;

            default:
                break;
        }
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_MOVE:
                mFreeMove = true;
                if (mVelocityTracker == null) {
                    mVelocityTracker = VelocityTracker.obtain();
                }
                mVelocityTracker.addMovement(event);
                mVelocityTracker.computeCurrentVelocity(1, 0.01f);
                mkeyMove = mVelocityTracker.getXVelocity();
                break;
            case MotionEvent.ACTION_DOWN:
                mFreeMove = false;
                break;
            case MotionEvent.ACTION_UP:
                mFreeMove = false;
                break;
            default:
                break;
        }
        return false;
    }

    //PR857320 modify Can not use Scale Free operation by fengke at 2014.12.02 start
    Toast mScaleToast = null;
    //PR857320 modify Can not use Scale Free operation by fengke at 2014.12.02 ends

    @Override
    public void onClick(View v) {
        // TODO Auto-generated method stub
        isClicked = true; // added by jipu.xiong@tcl.com
        switch (v.getId()) {
            case R.id.btn_ok:
                //PR936992 Undo and Redo icon are useless,while SD card is full add by limin.zhuo at 2015.03.06 begin
                if (!Utils.updateCacheDirEditPicture()) {
                     Utils.showToast(this, R.string.storage_full_tag);
                }
                //PR936992 Undo and Redo icon are useless,while SD card is full add by limin.zhuo at 2015.03.06 end
                if (editnum == 2) {
                    if (isNeedSaveSharp()) {
                        toMain();
                    } else {
                        finish();
                    }
                }else if(editnum==1){
                    rotate_ok();
                } else if (editnum == 0) {
                    cut_view.saveCut();
                    finish();
                }
                break;
            case R.id.btn_cancel:
                onCancel();
                break;
            // 旋转
            case R.id.rotateLeft:
                rotateLeft();
                break;
            case R.id.rotateRight:
                rotateRight();
                break;
            case R.id.rotateHorizontal:
                mirrorHorizontal();
                break;
            case R.id.rotateVertical:
                mirrorVertical();
                break;
            case R.id.cut_1_1:
                if (cut_view.setRectSelcetBySize(1) == false) {
                    if (mScaleToast == null) {
                        mScaleToast = Toast.makeText(IMGEditActivity.this, IMGEditActivity.this.getResources().getString(R.string.jrdgallery_scale_tips), Toast.LENGTH_SHORT);
                    }
                    mScaleToast.show();
                }
                break;
            case R.id.cut_3_2:
                if (cut_view.setRectSelcetBySize(2) == false) {
                    if (mScaleToast == null) {
                        mScaleToast = Toast.makeText(IMGEditActivity.this, IMGEditActivity.this.getResources().getString(R.string.jrdgallery_scale_tips), Toast.LENGTH_SHORT);
                    }
                    mScaleToast.show();
                }
                break;
            case R.id.cut_4_3:
                if (cut_view.setRectSelcetBySize(3) == false) {
                    if (mScaleToast == null) {
                        mScaleToast = Toast.makeText(IMGEditActivity.this, IMGEditActivity.this.getResources().getString(R.string.jrdgallery_scale_tips), Toast.LENGTH_SHORT);
                    }
                    mScaleToast.show();
                }
                break;
            case R.id.cut_2_3:
                if (cut_view.setRectSelcetBySize(4) == false) {
                    if (mScaleToast == null) {
                        mScaleToast = Toast.makeText(IMGEditActivity.this, IMGEditActivity.this.getResources().getString(R.string.jrdgallery_scale_tips), Toast.LENGTH_SHORT);
                    }
                    mScaleToast.show();
                }
                break;
            case R.id.cut_3_4:
                if (cut_view.setRectSelcetBySize(5) == false) {
                    if (mScaleToast == null) {
                        mScaleToast = Toast.makeText(IMGEditActivity.this, IMGEditActivity.this.getResources().getString(R.string.jrdgallery_scale_tips), Toast.LENGTH_SHORT);
                    }
                    mScaleToast.show();
                }
                break;
            case R.id.cut_16_9:
                if (cut_view.setRectSelcetBySize(6) == false) {
                    if (mScaleToast == null) {
                        mScaleToast = Toast.makeText(IMGEditActivity.this, IMGEditActivity.this.getResources().getString(R.string.jrdgallery_scale_tips), Toast.LENGTH_SHORT);
                    }
                    mScaleToast.show();
                }
                break;
                //PR484389-lilei-begin
           //case R.id.cut_free:
                
           //     break;
              //PR484389-lilei-end
            case R.id.btn_rotatefree_reset:
                mFreeRotateSeekBar.setProgress(50);
                rotate_view.reset();
                mFreeMove = false;
                oldValus = 0;
                newvalus= 0;
                break;
            default:
                break;
        }
    }

    // 取消
    private void onCancel() {
        // 美图必备 裁剪的取消按钮。
        finish();
    }

    public void onDestroy() {
        //PR651052-taoli-begin 001
        if (m_tool != null) {
            m_tool.clear();
        }
        //PR651052-taoli-end 001
        try {
            cut_view.Release();
            System.gc();
            super.onDestroy();
            return;
        } catch (Exception localException) {
        }
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        // TODO Auto-generated method stub
        switch (seekBar.getId()) {
            case R.id.seekbar_sharp:
                nOrignalSlider = progress;
                float val = 1.0f * (progress) / 100;
                showProcessSharp(val);
                mIsSharp = true;
                break;
            case R.id.seekbar_rotate:
                isFreeRotate = true;
                float valus = progress * 0.9f;
                if(valus >= 45 && mFreeMove){
                    if (mkeyMove >= 0f) {
                        rotate_view.rotate(0.9f);
                    }else {
                        rotate_view.rotate(-0.9f);
                    }
                }else {
                    if (mkeyMove >= 0f) {
                        rotate_view.rotate(0.9f);
                    }else {
                        rotate_view.rotate(-0.9f);
                    }
                }
                mFreeStart++;
                if(mFreeStart > 1){
                    mFreeStop = false;
                }else {
                    mFreeStop = true;
                }
                break;
            default:
                break;
        }
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
        // TODO Auto-generated method stub
        switch (seekBar.getId()) {
            case R.id.seekbar_rotate:
                oldValus = seekBar.getProgress()*0.9f;
                mFreeStart = 0;
                mFreeStop = true;
                break;
            default:
                break;
        }
       
    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        // TODO Auto-generated method stub
        switch (seekBar.getId()) {
            case R.id.seekbar_rotate:
                newvalus = seekBar.getProgress()*0.9f;
                Diatavalus = newvalus-oldValus;
                if(mFreeStop){
                    rotate_view.rotate(Diatavalus);
                    oldValus = seekBar.getProgress()*0.9f;
                }
                break;
            default:
                break;
        }
    }

    // 锐化

    public void showProcessSharp(float val) {

        // 美图必备 锐化的处理函数并获取UI效果图
        m_tool.procImage(val, true);
        if (mBitmap != null && !mBitmap.isRecycled()) {
            mBitmap.recycle();
        }
        mBitmap = m_tool.getShowProcImage();
        imageview_editsharp.setImageBitmap(mBitmap);
    }

    public boolean saveSharp() {
        try {
            if (nOrignalSlider == 0 || !mIsSharp) {
                return false;
            }
            // 美图必备 保存按钮的操作
            m_tool.ok();
            // 这个目前没用
            MyData.getBeautyControl().pushImage();

            mIsSharp = false;
        } catch (Exception e) {
        }
        return true;
    }

    public void toMain() {
        new MtprogressDialog(this) {
            @Override
            public void process() {
                try {
                    saveSharp();
                    finish();
                } catch (Exception e) {
                }
            }
        }.show();
    }

    public boolean isNeedSaveSharp() {
        if (nOrignalSlider == 0 || !mIsSharp) {
            return false;
        }
        return true;
    }

    // 旋转

    // 左旋转
    private void rotateLeft() {
        if (isSliderRotate) {
            return;
        }
        isSliderRotate = true;
        if (leftFromDegree % 360 == -90 || leftFromDegree % 360 == -270
                || leftFromDegree % 360 == 90 || leftFromDegree % 360 == 270) {
            rotate_view.rotateWithAni(leftFromDegree, leftToDegree, rotate_view.getMidX(),
                    rotate_view.getMidY(), rotate_view.getMultiple(), 1, true);
        } else {
            rotate_view.rotateWithAni(leftFromDegree, leftToDegree, rotate_view.getMidX(),
                    rotate_view.getMidY(), 1, rotate_view.getMultiple(), true);
        }
        rotate_view.setRealAngle(-90);
        rightFromDegree = leftFromDegree - 90;
        rightToDegree = leftToDegree + 90;
        leftFromDegree = leftFromDegree - 90;
        leftToDegree = leftToDegree - 90;
        isSliderRotate = false;
    }

    // 右旋转
    private void rotateRight() {
        if (isSliderRotate) {
            return;
        }
        isSliderRotate = true;
        if (leftFromDegree % 360 == -90 || leftFromDegree % 360 == -270
                || leftFromDegree % 360 == 90 || leftFromDegree % 360 == 270) {
            rotate_view.rotateWithAni(rightFromDegree, rightToDegree, rotate_view.getMidX(),
                    rotate_view.getMidY(), rotate_view.getMultiple(), 1, true);
        } else {
            rotate_view.rotateWithAni(rightFromDegree, rightToDegree, rotate_view.getMidX(),
                    rotate_view.getMidY(), 1, rotate_view.getMultiple(), true);
        }
        rotate_view.setRealAngle(90);
        leftFromDegree = rightFromDegree + 90;
        leftToDegree = rightToDegree - 90;
        rightFromDegree = rightFromDegree + 90;
        rightToDegree = rightToDegree + 90;
        isSliderRotate = false;
    }

    // 垂直旋转
    private void mirrorVertical() {
        if (isSliderRotate) {
            return;
        }
        isSliderRotate = true;
        if (leftFromDegree % 360 == -90 || leftFromDegree % 360 == -270
                || leftFromDegree % 360 == 90 || leftFromDegree % 360 == 270) {
            //add by biao.luo for pr484973 begin
//            rotate_view.scale(-1, 1);
            rotate_view.scale(-1, 1,leftFromDegree % 360);
        } else {
//            rotate_view.scale(1, -1);
            rotate_view.scale(1, -1, leftFromDegree % 360);
            //add by biao.luo end
        }
        rotate_view.setMirror(2);
        isSliderRotate = false;
    }

    // 水平旋转
    private void mirrorHorizontal() {
        if (isSliderRotate) {
            return;
        }
        isSliderRotate = true;
        if (leftFromDegree % 360 == -90 || leftFromDegree % 360 == -270
                || leftFromDegree % 360 == 90 || leftFromDegree % 360 == 270) {
            //add by biao.luo for pr484973 begin
//            rotate_view.scale(1, -1);
            rotate_view.scale(1, -1, leftFromDegree % 360);
        } else {
//            rotate_view.scale(-1, 1);
            rotate_view.scale(-1, 1, leftFromDegree % 360);
            //add by biao.luo end
        }
        rotate_view.setMirror(1);
        isSliderRotate = false;
    }

    // 重置
    private void reset() {
        int degree;
        if (Math.abs((leftFromDegree / 90) % 2) == 1) {
            degree = (int) ((leftFromDegree / 90) % 2) * 90;
            rotate_view.rotateWithAni(degree, 0, rotate_view.getMidX(), rotate_view.getMidY(),
                    rotate_view.getMultiple(), 1, true);
        } else if (Math.abs(leftFromDegree % 360) == 180) {
            degree = (int) (leftFromDegree % 360);
            rotate_view.rotateWithAni(degree, 0, rotate_view.getMidX(), rotate_view.getMidY(), 1,
                    1, true);
        }
        rotate_view.reset();
        leftFromDegree = 0;
        leftToDegree = -90;
        rightFromDegree = 0;
        rightToDegree = 90;
    }

    // 是否需要保存
    public boolean isNeedSaveRotate() {
        if (rotate_view != null) {
            return rotate_view.isNeedSave();
        } else {
            return false;
        }
    }

    // 保存
    public void saveRotate() {
        if (isNeedSaveRotate()) {
            rotate_view.savePic();
        }
    }
    
    public void saveRotate(float index) {
        if (isNeedSaveRotate()) {
            rotate_view.savePic(index);
        }
    }

    // ok
    public void rotate_ok() {
        new MtprogressDialog(IMGEditActivity.this) {

            @Override
            public void process() {
                // TODO Auto-generated method stub
                if (isFreeRotate) {
                    if(Diatavalus < 0)
                        Diatavalus = Diatavalus + 360;
                    saveRotate(Diatavalus);
                }else {
                    saveRotate();
                }
                finish();
            }
        }.show();
    }

    // 剪裁
}
