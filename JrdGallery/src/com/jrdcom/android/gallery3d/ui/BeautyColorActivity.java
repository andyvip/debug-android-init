
package com.jrdcom.android.gallery3d.ui;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

import com.jrdcom.mt.MTActivity;
import com.jrdcom.mt.util.MyData;
import com.jrdcom.mt.core.ToolColorMixing;
import com.jrdcom.mt.mtxx.controls.MtprogressDialog;
import com.jrdcom.android.gallery3d.R;
//PR936992 Undo and Redo icon are useless,while SD card is full add by limin.zhuo at 2015.03.06 begin
import com.jrdcom.example.joinpic.Utils;
//PR936992 Undo and Redo icon are useless,while SD card is full add by limin.zhuo at 2015.03.06 end

/**
 * 调色
 */
public class BeautyColorActivity extends MTActivity implements OnSeekBarChangeListener,
        OnClickListener, RadioGroup.OnCheckedChangeListener {

    private SeekBar mColorSaturationSeekBar = null;
    private SeekBar mBrightnessSeekBar = null;
    private SeekBar mContrastSeekBar = null;

    private SeekBar mColorExposureSeekBar = null;

    private ImageView mImageView = null;
    private Bitmap m_pViewBitmap = null;
    private ToolColorMixing m_tool;

    private RadioGroup pattleRadioGroup = null;
    private RadioButton rbtn_palette = null;
    private RadioButton rbtn_exposure = null;
    private TextView title = null;
    private FrameLayout layout_color = null;
    private final int COLOR_PREVIEW_WIDTH = (int) (58 * MyData.nDensity);
    private final int COLOR_PREVIEW_HEIGHT = (int) (47 * MyData.nDensity);

    /**
     * 当前色彩饱和度数值, 默认值为100 取值 0～200
     */
    private float mColorSaturation = 100.0f;
    /**
     * 当前亮度数值, 默认值为128 取值 0～256
     */
    private float mBrightness = 128.0f;
    /**
     * 当前对比度数值, 默认值为100 取值 0～200
     */
    private float mContrast = 100.0f;

    /**
     * 当前智能补光数值, 默认值为100 取值 0～200
     */
    private float mFillLight = 100.0f;

    private boolean isProcessing = false;// 是否正在执行
    private PopupWindow barSizePopUpView = null;
    private ImageView mTriangleView;
    private float[] smallLocations = {0f,0f};
    private boolean isFocus = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        setContentView(R.layout.beauty_color);

        layout_color = (FrameLayout) findViewById(R.id.bottom_sub_men);
        mImageView = (ImageView) findViewById(R.id.iv_result_image);
        mTriangleView = (ImageView) findViewById(R.id.imageview_triangle);
        pattleRadioGroup = (RadioGroup) findViewById(R.id.bottom_menu);
        rbtn_palette = (RadioButton) findViewById(R.id.rbtn_palette);
        rbtn_exposure = (RadioButton) findViewById(R.id.rbtn_exposure);
        title = (TextView) findViewById(R.id.label_top_bar_title);
        title.setText(R.string.img_enhance_title);
        pattleRadioGroup.setOnCheckedChangeListener(this);
//        rbtn_palette.setOnClickListener(this);
//        rbtn_exposure.setOnClickListener(this);
        pattleRadioGroup.check(R.id.rbtn_palette);
        setProgerssSize();
        //
        // // 美图必备 调色工具初始化功能
        m_tool = new ToolColorMixing();
        m_tool.init(MyData.getJNI());
        // // 美图必备 获取UI展示的功能图片
        m_pViewBitmap = m_tool.getShowProcImage();
        //
        mImageView.setImageBitmap(m_pViewBitmap);

//        View view = View.inflate(BeautyColorActivity.this, R.layout.img_enhance_palette_menu,
//                layout_color);
//        mColorSaturationSeekBar = (SeekBar) view.findViewById(R.id.sb_color_saturation);
//        mColorSaturationSeekBar.setOnSeekBarChangeListener(this);
//        mBrightnessSeekBar = (SeekBar) view.findViewById(R.id.sb_brightness);
//        mBrightnessSeekBar.setOnSeekBarChangeListener(this);
//        mContrastSeekBar = (SeekBar) view.findViewById(R.id.sb_contrast);
//        mContrastSeekBar.setOnSeekBarChangeListener(this);
//        if (barSizePopUpView == null) {
//            View popupview = View.inflate(BeautyColorActivity.this, R.layout.seekbar_popupview,
//                    null);
//            barSizePopUpView = new PopupWindow(popupview, COLOR_PREVIEW_WIDTH, COLOR_PREVIEW_HEIGHT);
//        }
        // // 设置 OK，CANCEL 按钮事件
        findViewById(R.id.btn_ok).setOnClickListener(this);
        findViewById(R.id.btn_cancel).setOnClickListener(this);
    }
    
    private void initSmallImgaeLocation() {
        smallLocations[0] = rbtn_palette.getX() + rbtn_palette.getWidth() / 2 - mTriangleView.getWidth() / 2;
        smallLocations[1] = rbtn_exposure.getX() + rbtn_exposure.getWidth() / 2 - mTriangleView.getWidth()
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
                onOK();
                break;
            case R.id.btn_cancel:
                onCancel();
                break;
            default:
                break;

        }
    }

    @Override
    public void onCheckedChanged(RadioGroup group, int checkedId) {
        // TODO Auto-generated method stub
        switch (checkedId) {
            case R.id.rbtn_palette:
                layout_color.removeAllViews();
                mTriangleView.setX(smallLocations[0]);
                View view = View.inflate(BeautyColorActivity.this,
                        R.layout.img_enhance_palette_menu, layout_color);
                mColorSaturationSeekBar = (SeekBar) view.findViewById(R.id.sb_color_saturation);
                mColorSaturationSeekBar.setOnSeekBarChangeListener(this);
                mBrightnessSeekBar = (SeekBar) view.findViewById(R.id.sb_brightness);
                mBrightnessSeekBar.setOnSeekBarChangeListener(this);
                mContrastSeekBar = (SeekBar) view.findViewById(R.id.sb_contrast);
                mContrastSeekBar.setOnSeekBarChangeListener(this);
                if (barSizePopUpView == null) {
                    View popupview = View.inflate(BeautyColorActivity.this,
                            R.layout.seekbar_popupview, null);
                    // barSizePopUpView = new PopupWindow(popupview,
                    // COLOR_PREVIEW_WIDTH, COLOR_PREVIEW_HEIGHT);
                }
                if (barSizePopUpView != null) {
                    barSizePopUpView.dismiss();
                }
                break;
            case R.id.rbtn_exposure:
                layout_color.removeAllViews();
                mTriangleView.setX(smallLocations[1]);
                View view1 = View.inflate(BeautyColorActivity.this,
                        R.layout.img_enhance_seekbar_menu, layout_color);
                mColorExposureSeekBar = (SeekBar) view1.findViewById(R.id.sb_enhance_image);
                mColorExposureSeekBar.setOnSeekBarChangeListener(this);
                // seekBar_beauty.setProgress(size[1]);
                // mColorExposureSeekBar.setOnSeekBarChangeListener(new
                // OnSeekBarChangeListenerBeauty());
                if (barSizePopUpView != null) {
                    barSizePopUpView.dismiss();
                }

                break;
            default:
                break;
        }
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
        // TODO Auto-generated method stub

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        // TODO Auto-generated method stub

    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        if (fromUser) {
            // 数据变化来自用户拉动滑动杄1�7
            if (seekBar == mColorSaturationSeekBar) {
                if (mColorSaturation == progress) {
                    return;
                }
                mColorSaturation = progress;
                updatePaletteImage();
            } else if (seekBar == mBrightnessSeekBar) {
                if (mBrightness == progress) {
                    return;
                }
                mBrightness = progress;
                updatePaletteImage();
            } else if (seekBar == mContrastSeekBar) {
                if (mContrast == progress) {
                    return;
                }
                mContrast = progress;
                updatePaletteImage();
            } else if (seekBar == mColorExposureSeekBar) {
                if (mFillLight == progress) {
                    return;
                }
                mFillLight = progress;
                updatePaletteImage();
            } else {// Other SeekBar. Do nothing.

            }
        } else {
            // do nothing.
        }
    }

    private void setProgerssSize() {
        mColorSaturationSeekBar.setProgress((int) mColorSaturation);
        mBrightnessSeekBar.setProgress((int) mBrightness);
        mContrastSeekBar.setProgress((int) mContrast);
    }

    /**
     * 更新调色后的图片到屏幕上
     */
    private void updatePaletteImage() {

        float arr[] = new float[4];
        // 亮度 -128~128,对比庄1�7 -100~100,饱和庄1�7 -100~100 补光 -100~100
        arr[0] = mBrightness - 128;
        arr[1] = mContrast - 100;
        arr[2] = mColorSaturation - 100;
        arr[3] = mFillLight - 100;
        isProcessing = true;

        // 美图必备 调色功能的具体处理函数
        m_tool.procImage(arr, 4, true);
        // 美图必备 获取应用调色功能后的UI效果图
        m_pViewBitmap = m_tool.getShowProcImage();

        isProcessing = false;
        Message msg = new Message();
        msg.what = 0;
        handler.sendMessage(msg);

    }

    Handler handler = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 0:
                    mImageView.setImageBitmap(m_pViewBitmap);
                    break;

                default:
                    break;
            }
            super.handleMessage(msg);
        }

    };

    // /**
    // * 响应用户点击保存按钮
    // */
    public void onOK() {
        // 美图必备 判断是否处理过，主要是为了提高效率用。当点击保存后，如果没进行改变过，就不进行任何操作
        if (!m_tool.isProcessed()) {
            onCancel();
            return;
        }

        new MtprogressDialog(this) {
            @Override
            public void process() {
                try {

                    // 美图必备 保存按钮的操作
                    m_tool.ok();
                    // 这个目前没用
                    MyData.getBeautyControl().pushImage();

                    finish();

                } catch (Exception e) {
                }
            }
        }.show();
    }

    /**
     * 响应用户点击返回按钮
     */
    private void onCancel() {
        if (isProcessing) {
            return;
        }
        // 美图必备 返回按钮的时候必须调用
        m_tool.cancel();

        finish();
    }

}
