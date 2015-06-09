/******************************************************************************************************************/
/*                                                                         Date : 08/2013  */
/*                            PRESENTATION                                                 */
/*              Copyright (c) 2010 JRD Communications, Inc.                                */
/******************************************************************************************************************/
/*                                                                                                                */
/*    This material is company confidential, cannot be reproduced in any                   */
/*    form without the written permission of JRD Communications, Inc.                      */
/*                                                                                                                */
/*================================================================================================================*/
/*   Author :                                                                              */
/*   Role :                                                                                */
/*   Reference documents :                                                                 */
/*================================================================================================================*/
/* Comments :                                                                              */
/*     file    :packages/apps/JrdGallery/src/com/jrdcom/android/gallery3d/filtershow/BeautyMainActivity.java     */
/*     Labels  :                                                                           */
/*================================================================================================================*/
/* Modifications   (month/day/year)                                                        */
/*================================================================================================================*/
/* date    | author       |FeatureID                |modification                          */
/*============|==============|=========================|==========================================================*/
/*08/06/13 | zhangcheng |PR498772-zhangcheng-001 |Pop up gallery force close when tap home key during loanding images. */
/*============|==============|=========================|==========================================================*/
package com.jrdcom.android.gallery3d.filtershow;


import java.io.IOException;

import com.jrdcom.android.gallery3d.R;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;
import android.view.Window;
import android.widget.AdapterView.OnItemClickListener;
import com.jrdcom.android.gallery3d.ui.BeautyBlurActivity;
import com.jrdcom.android.gallery3d.ui.BeautyColorActivity;
import com.jrdcom.android.gallery3d.ui.BeautyEffectActivity;
import com.jrdcom.android.gallery3d.ui.BeautyFrameActivity;
import com.jrdcom.android.gallery3d.ui.BeautyMopiActivity;
import com.jrdcom.android.gallery3d.ui.BeautyMosaicActivity;
import com.jrdcom.android.gallery3d.ui.BeautyWordActivity;
import com.jrdcom.android.gallery3d.ui.IMGEditActivity;
import com.jrdcom.android.gallery3d.util.GalleryUtils;
import com.jrdcom.mt.core.BeautyControl;
import com.jrdcom.mt.core.BitmapUtil;
import com.jrdcom.mt.mtxx.controls.DialogWait;
import com.jrdcom.mt.util.MyData;
import com.jrdcom.mt.mtxx.controls.MtprogressDialog;
import android.view.*;
import android.widget.ImageView;
import com.jrdcom.mt.util.FileUtils;
import com.jrdcom.mt.mtxx.controls.OnSetPauseListener;

public class BeautyMainActivity extends Activity implements OnClickListener, OnSetPauseListener {

    
    //add by biao.luo begin
    private final int TIMER_INVALIDATE = 0x101;
    private final int LOAD_PIC = 0x102;
    private String finalFilePath = null;
    private Button mImageEditButton;
    private Button mWhitemopiButton;
    private Button mEnhanceButton;
    private Button mEffectButton;
    private Button mFrameButton;
    private Button mMosaicButton;
    private Button mWordsButton;
    private Button mWeakButton;
    //add by biao.luo end
    private ImageView mPreview;
    private Button saveButton;
    
    private BeautyControl mBeautyControl = MyData.getBeautyControl();

	//add by zhangcheng for PR498772 begin	
	private PauseListener mPauseListener;

	public void setPauseListener(PauseListener pauseListener){
		mPauseListener = pauseListener;
	}

       @Override
	protected void onPause() {
		// TODO Auto-generated method stub
		mPauseListener.onPause();
		super.onPause();
	}
	//add by zhangcheng for PR498772 end
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_beauty_main);
        
      //add by biao.luo begin
        initDevData();
        mImageEditButton=(Button)findViewById(R.id.btn_edit);
        mWhitemopiButton=(Button)findViewById(R.id.btn_menu_white);
        mEnhanceButton=(Button)findViewById(R.id.btn_color);
        mEffectButton=(Button)findViewById(R.id.btn_effect);
        mFrameButton=(Button)findViewById(R.id.btn_frame);
        mMosaicButton=(Button)findViewById(R.id.btn_mosaic);
        mWordsButton=(Button)findViewById(R.id.btn_words);
        mWeakButton=(Button)findViewById(R.id.btn_weak);
        saveButton = (Button)findViewById(R.id.btn_save);
        mPreview = (ImageView)findViewById(R.id.ImageViewMain);
        
        mImageEditButton.setOnClickListener(this);
        mWhitemopiButton.setOnClickListener(this);
        mEnhanceButton.setOnClickListener(this);
        mEffectButton.setOnClickListener(this);
        mFrameButton.setOnClickListener(this);
        mMosaicButton.setOnClickListener(this);
        mWordsButton.setOnClickListener(this);
        mWeakButton.setOnClickListener(this);
        saveButton.setOnClickListener(this);
        
        
        loadImageWithFilePath(finalFilePath);
        MyData.setAPKPathToNDK(getApplicationContext());
        try {
            MyData.NDKCheckColorARGB8888Index( this.getAssets().open("ndk_check_color.bmp") );
        } catch (IOException e) {
        }
       
        //add by biao.luo end 
    }
    
    
    @Override
    public void onClick(View v) {
        Intent intent = new Intent();
        switch (v.getId()) {
            case R.id.btn_edit:
                intent.setClass(BeautyMainActivity.this, IMGEditActivity.class);
                startActivity(intent);
                break;
            case R.id.btn_menu_white:
                intent.setClass(BeautyMainActivity.this, BeautyMopiActivity.class);
                startActivity(intent);
                break;
            case R.id.btn_color:
                intent.setClass(BeautyMainActivity.this, BeautyColorActivity.class);
                startActivity(intent);
                break;
            case R.id.btn_effect:
                intent.setClass(BeautyMainActivity.this, BeautyEffectActivity.class);
                startActivity(intent);
                break;

            case R.id.btn_frame:
                intent.setClass(BeautyMainActivity.this, BeautyFrameActivity.class);
                startActivity(intent);
                break;
            case R.id.btn_mosaic:
                intent.setClass(BeautyMainActivity.this, BeautyMosaicActivity.class);
                startActivity(intent);
                break;

            case R.id.btn_words:
                intent.setClass(BeautyMainActivity.this, BeautyWordActivity.class);
                startActivity(intent);
                break;

            case R.id.btn_weak:
                intent.setClass(BeautyMainActivity.this, BeautyBlurActivity.class);
                startActivity(intent);
                break;
            case R.id.btn_save:
                doSave();
                break;
            default:
                break;
        }
    }
    private void loadImageWithFilePath(String filepath) {

        if (filepath == null) {
            throw new NullPointerException("The path starts pictures can not be empty");
        }

        new DialogWait(this, "Please wait a moment...", "Loading Images") {
            @Override
            public void process() {
                int nRetn = 1;
                try {
                    nRetn = mBeautyControl.initWithImagePath(MyData.strPicPath, MyData.nScreenW, MyData.nScreenH,
                            MyData.nOutPutWidth, MyData.nOutPutHeight);

                } catch (Exception e) {
                } finally {
                    Message message = new Message();
                    message.what = LOAD_PIC;
                    switch (nRetn) {
                    case 0:
                        message.what = TIMER_INVALIDATE;
                        break;
                    }
                    mHandler.sendMessage(message);
                }
            }
        }.run();
    }
    
    Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
            case TIMER_INVALIDATE:
                if (!MyData.getBeautyControl().m_bIsLoadPic) {
                    MyData.strPicPath = null;
                    finish();
                    return;
                }
                refreshPreviewImageViewIfNeed();
                break;
            case LOAD_PIC:
                refreshPreviewImageViewIfNeed();
                break;
            default:
                // do nothing
                break;
            }
            super.handleMessage(msg);
        }
    };
    
    private boolean refreshPreviewImageViewIfNeed() {
        // return false;

        Bitmap m_pViewBitmap = mBeautyControl.getCurrentShowImage();

        if (m_pViewBitmap != null) {
            mPreview.setImageBitmap(m_pViewBitmap);
            m_pViewBitmap = null;
            return true;
        } else {
            return false;
        }
    }
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode) {
        case KeyEvent.KEYCODE_BACK: {
            mBeautyControl.clearMemory();
            this.finish();
            return true;
        }
        default:
            break;
        }
        return super.onKeyDown(keyCode, event);
    }
    protected void initDevData() {
        final Intent intent = getIntent();
        finalFilePath = BitmapUtil.getRealPathFromUri(BeautyMainActivity.this, intent.getData());
        MyData.strPicPath = finalFilePath;
        if (MyData.nScreenW == 0 || MyData.nScreenH == 0 || MyData.nDensity == 0) {
            Display display = getWindowManager().getDefaultDisplay();
            DisplayMetrics dm = new DisplayMetrics();
            display.getMetrics(dm);
            MyData.nScreenW = dm.widthPixels;
            MyData.nScreenH = dm.heightPixels;
            MyData.nDensity = dm.density;
            MyData.nBmpDstW = MyData.nScreenW;
            MyData.nBmpDstH = MyData.nScreenH - 100;
            if (MyData.nBmpDstW < MyData.nOutPutWidth && MyData.nBmpDstH < MyData.nOutPutHeight) {
                MyData.nBmpDstW = MyData.nOutPutWidth;
                MyData.nBmpDstH = MyData.nOutPutHeight;
            }
        }
    }
    
 // 保存
    private void doSave() {

        new MtprogressDialog(this) {
            @Override
            public void process() {
                try {
                    // 美图必备 保存真实图片到路径下
                    mBeautyControl.saveImage(FileUtils.generateToSaveFileName(null));
                } catch (Exception e) {
                }
            }
        }.show();

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // MyData.bmpDst = MyData.getCosmesisControl().getCurrentShowImage();
        refreshPreviewImageViewIfNeed();
        super.onActivityResult(requestCode, resultCode, data);
    }
    @Override
    protected void onResume() {
        super.onResume();
        refreshPreviewImageViewIfNeed();
    }
}
