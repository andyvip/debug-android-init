/* Copyright Statement:
 *
 * This software/firmware and related documentation ("MediaTek Software") are
 * protected under relevant copyright laws. The information contained herein is
 * confidential and proprietary to MediaTek Inc. and/or its licensors. Without
 * the prior written permission of MediaTek inc. and/or its licensors, any
 * reproduction, modification, use or disclosure of MediaTek Software, and
 * information contained herein, in whole or in part, shall be strictly
 * prohibited.
 *
 * MediaTek Inc. (C) 2010. All rights reserved.
 *
 * BY OPENING THIS FILE, RECEIVER HEREBY UNEQUIVOCALLY ACKNOWLEDGES AND AGREES
 * THAT THE SOFTWARE/FIRMWARE AND ITS DOCUMENTATIONS ("MEDIATEK SOFTWARE")
 * RECEIVED FROM MEDIATEK AND/OR ITS REPRESENTATIVES ARE PROVIDED TO RECEIVER
 * ON AN "AS-IS" BASIS ONLY. MEDIATEK EXPRESSLY DISCLAIMS ANY AND ALL
 * WARRANTIES, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE OR
 * NONINFRINGEMENT. NEITHER DOES MEDIATEK PROVIDE ANY WARRANTY WHATSOEVER WITH
 * RESPECT TO THE SOFTWARE OF ANY THIRD PARTY WHICH MAY BE USED BY,
 * INCORPORATED IN, OR SUPPLIED WITH THE MEDIATEK SOFTWARE, AND RECEIVER AGREES
 * TO LOOK ONLY TO SUCH THIRD PARTY FOR ANY WARRANTY CLAIM RELATING THERETO.
 * RECEIVER EXPRESSLY ACKNOWLEDGES THAT IT IS RECEIVER'S SOLE RESPONSIBILITY TO
 * OBTAIN FROM ANY THIRD PARTY ALL PROPER LICENSES CONTAINED IN MEDIATEK
 * SOFTWARE. MEDIATEK SHALL ALSO NOT BE RESPONSIBLE FOR ANY MEDIATEK SOFTWARE
 * RELEASES MADE TO RECEIVER'S SPECIFICATION OR TO CONFORM TO A PARTICULAR
 * STANDARD OR OPEN FORUM. RECEIVER'S SOLE AND EXCLUSIVE REMEDY AND MEDIATEK'S
 * ENTIRE AND CUMULATIVE LIABILITY WITH RESPECT TO THE MEDIATEK SOFTWARE
 * RELEASED HEREUNDER WILL BE, AT MEDIATEK'S OPTION, TO REVISE OR REPLACE THE
 * MEDIATEK SOFTWARE AT ISSUE, OR REFUND ANY SOFTWARE LICENSE FEES OR SERVICE
 * CHARGE PAID BY RECEIVER TO MEDIATEK FOR SUCH MEDIATEK SOFTWARE AT ISSUE.
 *
 * The following software/firmware and/or related documentation ("MediaTek
 * Software") have been modified by MediaTek Inc. All revisions are subject to
 * any receiver's applicable license agreements with MediaTek Inc.
 */

package com.jrdcom.mediatek.gallery3d.pq;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapRegionDecoder;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore.Images;
import android.provider.MediaStore.Images.ImageColumns;
import android.text.BoringLayout.Metrics;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.View.OnTouchListener;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.AbsoluteLayout.LayoutParams;

import com.jrdcom.android.gallery3d.R;
import com.jrdcom.android.gallery3d.common.BitmapUtils;
import com.jrdcom.android.gallery3d.data.MediaItem;
import com.jrdcom.android.gallery3d.ui.GLRoot;

import com.jrdcom.mediatek.gallery3d.util.MediatekFeature;
import com.jrdcom.mediatek.gallery3d.util.MtkLog;
import com.jrdcom.mediatek.gallery3d.util.MediatekFeature.Params;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import android.view.GestureDetector;
public class PictureQualityTool extends Activity {
    /** Called when the activity is first created. */

    private static final String TAG = "PictureQualityTool";
    public static final String ACTION_PQ = "com.android.camera.action.PQ";
    private static final int BACKUP_PIXEL_COUNT = 480000; // around 800x600
    private static final int SUCCESS_LOAD_BITMAP = 1;
    private static boolean isEnterADVmode;
    private PictureQualityJni89 PQJni89;
    private ImageView mImageView;
    private Bitmap mBitmap;
    private DecodeImage mDecodeImage;

    private SeekBar mSeekBarSkyTone ;
    private SeekBar mSeekBarGlobal;

    private SeekBar mSeekBarSkinTone;
    private SeekBar mSeekBarGrassTone;
    private SeekBar mSeekBarSharpness;
    //add 3 new bar 
    private SeekBar mSeekBarSkinSat;
    private SeekBar mSeekBarGrassSat;
    private SeekBar mSeekBarSkySat;
//---------------------------------- need add to

    private TextView mTextViewSkinTone;
    private TextView mTextViewGrassTone;
    private TextView mTextViewSkyTone;
    private TextView mTextViewGlobal;
    private TextView mTextViewSharpness;
    //  add process TextView view 
    private TextView mTextViewSkinSat;
    private TextView mTextViewGrassSat;
    private TextView mTextViewSkySat;
    
    
    private TextView mTextViewSkinToneMin;
    private TextView mTextViewGrassToneMin;
    private TextView mTextViewSkyToneMin;
//----------------------------------

    private TextView mTextViewSkyToneRange;
    private TextView mTextViewSkinToneRange;
    private TextView mTextViewGrassToneRange;
    private TextView mTextViewSharpnessRange;
    private TextView mTextViewGlobalSatRange;
    //add 3 new bar
    private TextView mTextViewSkinSatRange;
    private TextView mTextViewGrassSatRange;
    private TextView mTextViewSkySatRange;

    private int mSkyToneRange;
    private int mSkinToneRange;
    private int mGrassToneRange;
    private int mGlobleSatRange;
    private int mSharpnessRange;
    //add 3 new bar range
    private int mSkinSatRange;
    private int mGrassSatRange;
    private int mSkySatRange;
    /////////////////////////////

    private int mSharpnessOption;
    private int mGlobalSatOption;
    private int mSkinToneOption;
    private int mSkyToneOption;
    private int mGrassToneOption;


    ////add 3 new bar option
    private int mSkinSatOption;
    private int mGrassSatOption;
    private int mSkySatOption;
    public String pqUri;
    public BitmapFactory.Options options = new BitmapFactory.Options();
    public View mView;

    private int origionSharpnessIndex;
    private int origionSkyToneIndex;
    private int origionGrassToneIndex;
    private int origionSkinToneIndex;
    private int origionSatAdjIndex;
    private int origionSkinToneSIndex;
    private int origionGrassToneSIndex;
    private int origionSkyToneSIndex;

    public ActionBar mActionBar;
    ////////////////////////ADV mode////////////////////
    //the matric is for ADV mode
    Matrix mMetric = new Matrix();
    private   ImageView mImageViewADV;
    private int WindowsWidth;
    private int WindowsHeight;
    public  BitmapRegionDecoder mbitmapRegionDecoder =null; 
    private SeekBar seekBar_hue;
    private TextView textView_hue_left;
    private TextView textView_hue;
    private TextView textView_hue_progress;
    // add for temple view when SeekBar visible
    private TextView textView_hue_left_temple;
    private TextView textView_hue_temple;
    private TextView textView_hue_progress_temple;
    private int mHudOptionADV;
    private int mHudRangeADV;
    private  GestureDetector mGestureDetector;
    
    public SeekBar seekBar_saturation;
    private TextView textView_saturation_left;
    private TextView textView_saturation;
    private TextView textView_saturation_progress;
    // add for temple view when SeekBar visible
    private TextView textView_saturation_left_temple;
    private TextView textView_saturation_temple;
    private TextView textView_saturation_progress_temple;
    private int mSatOptionADV;
    private int mSatRangeADV;
    private ImageViewTouchBase mImageViewTouchBase=null;
    int lastX, lastY;
    public VisibleLisenter mVisibleLisenter;
    public SeekBarChangeLisenter mSeekBarChangeLisenter = null;
    public OnSeekBarChangelisenter mOnSeekBarChangelisenter = null;
    public SeekBarTouchBase hueSeekBarTouchBase;
    public SeekBarTouchBase saturationSeekBarTouchBase;
    public SettingXYAxisLisenter mSettingXYAxisLisenter;
    public MenuItem PQSwitchmemu;
    public MenuItem PQADVMode;
    public boolean sign = false;
    ////////////////////////////////////////////////////
    private Handler mHandler = new Handler(){
            public void handleMessage(Message msg) {
                switch (msg.what) {
                case SUCCESS_LOAD_BITMAP:
                    if (!isEnterADVmode) {
                        if(mImageView != null) {
                            mImageView.setImageBitmap(mBitmap);
                            mImageView.invalidate(); 
                        }
                    } else {
                        if(mImageViewADV != null) {
                            Log.d(TAG,"w====="+((WindowsWidth-mBitmap.getWidth())/2)+ " H===="+((WindowsHeight - mBitmap.getHeight())/2));
                            mImageViewADV.setImageBitmap(mBitmap);
                            mImageViewADV.setImageMatrix(mMetric);
                            mImageViewADV.invalidate();  
                        }
                    }
                }
        }
    };
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_ACTION_BAR);
        requestWindowFeature(Window.FEATURE_ACTION_BAR_OVERLAY);
        DisplayMetrics outMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(outMetrics);
        WindowsWidth = outMetrics.widthPixels;
        WindowsHeight = outMetrics.heightPixels;
        Log.d(TAG,"WindowsWidth=="+WindowsWidth+" WindowsHeight=="+WindowsHeight);
        mView = (View)findViewById(R.layout.picture_quality_tool);
        setContentView(R.layout.picture_quality_tool);
        Bundle bundle = this.getIntent().getExtras();
        pqUri = bundle.getString("PQUri");
        mDecodeImage= new DecodeImage();
        mOnSeekBarChangelisenter = new OnSeekBarChangelisenter(mSeekBarChangeLisenter);
        mOnSeekBarChangelisenter.setDecodeImage(mDecodeImage);
        mDecodeImage.setNeedDecodeBound(true);
        isEnterADVmode = false;
        //for picture quality enhancement
        if (MediatekFeature.isPictureQualityEnhanceSupported()) {
            options.inPostProc = true;
        }
        options.inSampleSize = 1;
        initPQToolView();
        addSeekBarListener();
    }
    @Override
    public void onResume() {
        super.onResume();
        isEnterADVmode = false;
    }
    @Override
    public void onDestroy() {
        super.onDestroy();
        isEnterADVmode = false;
        if (mBitmap != null)
            mBitmap.recycle();
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.picturequality, menu);
        PQSwitchmemu =  menu.findItem(R.id.PQSwitch);
        PQSwitchmemu.setVisible(false);
        if (MtkLog.SUPPORT_PQ_ADV == false) {
            PQADVMode =  menu.findItem(R.id.ADVmode);
            PQADVMode.setVisible(false);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        case android.R.id.home: 
            finish();
            break;
        case R.id.cancel: 
            recoverIndex();
            onSaveClicked();
            finish();
            break;
        case R.id.save: 
            onSaveClicked();
            finish();
            break;
        case R.id.ADVmode: 
            if (isEnterADVmode) {
                isEnterADVmode = false;
                item.setTitle("Base");
                enterBasemode();
                PQSwitchmemu.setVisible(false);
            } else {
                PQSwitchmemu.setVisible(true);
                mMetric.setValues(new float[]{1f, 0f, 0f,  0f, 1f, 0f,  0f, 0f, 1f} );
              //  mMetric.postTranslate((WindowsWidth-mBitmap.getWidth())/2 , (WindowsHeight - mBitmap.getHeight())/2);
                float scaleW = ((float) WindowsWidth)/mBitmap.getWidth();
                float scaleH = ((float) WindowsHeight)/mBitmap.getHeight();
                float scale = scaleW - scaleH > 0 ? scaleH :scaleW;
                mMetric.postScale(scale, scale);
                mMetric.postTranslate((WindowsWidth-mBitmap.getWidth()*scale)/2 , (WindowsHeight - mBitmap.getHeight()*scale)/2);
                isEnterADVmode = true;
                item.setTitle("ADV");
                enterADVmode();
            }

            break;
        case R.id.PQSwitch:
            if (sign == false) {
                item.setTitle("PQ off");
                sign = true;
            } else {
                item.setTitle("PQ on");
                sign = false;
            }
            break;
        default:
            break;
        }
        return true;
    }

    private void recoverIndex() {
        PQJni89.nativeSetSharpAdjIndex(origionSharpnessIndex);
        PQJni89.nativeSetSatAdjIndex(origionSatAdjIndex);
        PQJni89.nativeSetSkinToneHIndex(origionSkinToneIndex);
        PQJni89.nativeSetGrassToneHIndex(origionGrassToneIndex);
        PQJni89.nativeSetSkyToneHIndex(origionSkyToneIndex);
        PQJni89.nativeSetSkinToneSIndex(origionSkinToneSIndex);
        PQJni89.nativeSetGrassToneSIndex(origionGrassToneSIndex);
        PQJni89.nativeSetSkyToneSIndex(origionSkyToneSIndex);
    }

    private void enterBasemode() {
        setContentView(R.layout.picture_quality_tool);
        initPQToolView();
    }
    
    private void enterADVmode() {
         setContentView(R.layout.picture_quality_tool_advmode);
         initPQToolViewADVMode();
    }
    
    private void initPQToolView() {
        getViewById();
        (new Thread(mDecodeImage)).start();
        setRangeAndIndex();

        addSeekBarListener();

    }

    private void getViewById() {
        //PQTool.nativeGetSharpnessRange() = 16, Sharpness adjust range [0---15]
        //PQTool.nativeGetColorRange() = 7, Color adjust range [0---6]
        //PQTool.nativeGetSkinToneRange() = 7, SkinTone adjust range [-3---3]
        //PQTool.nativeGetGrassToneRange() = 7, GrassTone adjust range [-3---3]
        //PQTool.nativeGetSkyToneRange() = 7, SkyTone adjust range [-3---3]

        mTextViewSkyToneMin = (TextView)findViewById(R.id.textView1);
        mTextViewSkinToneMin = (TextView)findViewById(R.id.textView4);
        mTextViewGrassToneMin = (TextView)findViewById(R.id.textView5);
        
        mImageView = (ImageView)findViewById(R.id.imageView);
        mTextViewSharpnessRange = (TextView)findViewById(R.id.textView_sharpness);
        mTextViewGlobalSatRange = (TextView)findViewById(R.id.textView_color);
        mTextViewSkyToneRange = (TextView)findViewById(R.id.textView_skyTone);
        mTextViewSkinToneRange = (TextView)findViewById(R.id.textView_skinTone);
        mTextViewGrassToneRange = (TextView)findViewById(R.id.textView_grassTone);
        //add 3 new bar
        mTextViewSkinSatRange = (TextView)findViewById(R.id.textView_skinSat);
        mTextViewGrassSatRange= (TextView)findViewById(R.id.textView_GrassSat);
        mTextViewSkySatRange = (TextView)findViewById(R.id.textView_skySat);

        //
        mTextViewSharpness = (TextView)findViewById(R.id.textView_sharpness_progress);
        mSeekBarSharpness  = (SeekBar)findViewById(R.id.seekBar_sharpness);


        mTextViewSkyTone = (TextView)findViewById(R.id.textView_skyTone_progress);
        mSeekBarSkyTone = (SeekBar)findViewById(R.id.seekBar_skyTone);


        mTextViewGrassTone = (TextView)findViewById(R.id.textView_grassTone_progress);
        mSeekBarGrassTone = (SeekBar)findViewById(R.id.seekBar_grassTone);


        mTextViewSkinTone = (TextView)findViewById(R.id.textView_skinTone_progress);
        mSeekBarSkinTone = (SeekBar)findViewById(R.id.seekBar_skinTone);


        mTextViewGlobal = (TextView)findViewById(R.id.textView_color_progress);
        mSeekBarGlobal  = (SeekBar)findViewById(R.id.seekBar_color);
        //add 3 new bars
        mTextViewSkinSat = (TextView)findViewById(R.id.textView_skinSat_progress);
        mSeekBarSkinSat = (SeekBar)findViewById(R.id.seekBar_skinSat);
            
        mTextViewGrassSat = (TextView)findViewById(R.id.textView_GrassSat_progress);
        mSeekBarGrassSat = (SeekBar)findViewById(R.id.seekBar_GrassSat);
        
        mTextViewSkySat = (TextView)findViewById(R.id.textView_skySat_progress);
        mSeekBarSkySat = (SeekBar)findViewById(R.id.seekBar_skySat);
    }

    private void getViewByIdADVMode() {
        mImageViewADV = (ImageView)findViewById(R.id.imageView_adv);
        LayoutInflater lay = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View v = lay.inflate(R.layout.qulity_tool_seekbar_advmode, null);
        RelativeLayout mRelativeLayout = (RelativeLayout)findViewById(R.layout.qulity_tool_seekbar_advmode);
        getWindow().addContentView(getLayoutInflater().inflate(R.layout.qulity_tool_seekbar_advmode, null), new ViewGroup.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT, ViewGroup.LayoutParams.FILL_PARENT));
        getWindow().addContentView(getLayoutInflater().inflate(R.layout.qulity_tool_seekbar_advmode2, null), new ViewGroup.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT, ViewGroup.LayoutParams.FILL_PARENT));
        getWindow().addContentView(getLayoutInflater().inflate(R.layout.qulity_tool_textview_advmode, null), new ViewGroup.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT, ViewGroup.LayoutParams.FILL_PARENT));
        mImageViewTouchBase = new ImageViewTouchBase(this ,mMetric, mImageViewADV, mBitmap, mbitmapRegionDecoder, options.inSampleSize);
        mImageViewADV.setOnTouchListener(mImageViewTouchBase);
        //mImageViewADV.setImageBitmap(mBitmap);
        seekBar_hue = (SeekBar)findViewById(R.id.seekBar_hue);
        textView_hue_left = (TextView)findViewById(R.id.textView2_hue);
        textView_hue = (TextView)findViewById(R.id.textView_hue);
        textView_hue_progress = (TextView)findViewById(R.id.textView_hue_progress);

        textView_hue_left_temple = (TextView)findViewById(R.id.textView2_hue_old);
        textView_hue_temple = (TextView)findViewById(R.id.textView_hue_old);
        textView_hue_progress_temple = (TextView)findViewById(R.id.textView_hue_progress_old);

        setVisibilityADM(seekBar_hue, View.GONE);
        setVisibilityADM(textView_hue_left, View.GONE);
        setVisibilityADM(textView_hue, View.GONE);
        setVisibilityADM(textView_hue_progress, View.GONE);

        setVisibilityADM(textView_hue_left_temple, View.GONE);
        setVisibilityADM(textView_hue_temple, View.GONE);
        setVisibilityADM(textView_hue_progress_temple, View.GONE);

        seekBar_saturation = (SeekBar)findViewById(R.id.seekBar_saturation);
        textView_saturation_left = (TextView)findViewById(R.id.textView3_saturation);
        textView_saturation = (TextView)findViewById(R.id.textView_saturation);
        textView_saturation_progress = (TextView)findViewById(R.id.textView_saturation_progress);

        textView_saturation_left_temple = (TextView)findViewById(R.id.textView3_saturation_old);
        textView_saturation_temple = (TextView)findViewById(R.id.textView_saturation_old);
        textView_saturation_progress_temple = (TextView)findViewById(R.id.textView_saturation_progress_old);

        setVisibilityADM(seekBar_saturation, View.GONE);
        setVisibilityADM(textView_saturation_left, View.GONE);
        setVisibilityADM(textView_saturation, View.GONE);
        setVisibilityADM(textView_saturation_progress, View.GONE);

        setVisibilityADM(textView_saturation_left_temple, View.GONE);
        setVisibilityADM(textView_saturation_temple, View.GONE);
        setVisibilityADM(textView_saturation_progress_temple, View.GONE);
        mVisibleLisenter = new VisibleLisenter();
        mImageViewTouchBase.setVisibleLisenter(mVisibleLisenter);
        mSettingXYAxisLisenter = new SettingXYAxisLisenter();
        mImageViewTouchBase.setXYAxisLisenter(mSettingXYAxisLisenter);
    }
    
    public void setVisibilityADM(View mView, int isVisible) {
        if (mView != null) {
            mView.setVisibility(isVisible);
        }
    }

    private void initPQToolViewADVMode() {
         getViewByIdADVMode();
         (new Thread(mDecodeImage)).start();
         setRangeAndIndexADVMode();
         addSeekBarListenerADVMode();

    }
    private void setRangeAndIndexADVMode () {
        mHudRangeADV = PQJni89.nativeGetHueAdjRange();
        mSatRangeADV = PQJni89.nativeGetSatAdjRange();

        textView_hue.setText(Integer.toString(mHudRangeADV - 1));
        textView_hue_progress.setText("Hue:  " + Integer.toString(PQJni89.nativeGetHueAdjIndex()));
        seekBar_hue.setProgress(PQJni89.nativeGetHueAdjIndex() * 100 / (mHudRangeADV - 1));

        textView_saturation.setText(Integer.toString(mSatRangeADV - 1));
        textView_saturation_progress.setText("Sat:  " + Integer.toString(PQJni89.nativeGetSatAdjIndex()));
        seekBar_saturation.setProgress(PQJni89.nativeGetSatAdjIndex() * 100 / (mHudRangeADV - 1));
        
        textView_hue_temple.setText(Integer.toString(mHudRangeADV - 1));
        textView_hue_progress_temple.setText("Hue:  " + Integer.toString(PQJni89.nativeGetHueAdjIndex()));
   
        textView_saturation_temple.setText(Integer.toString(mSatRangeADV - 1));
        textView_saturation_progress_temple.setText("Sat:  " + Integer.toString(PQJni89.nativeGetSatAdjIndex()));
    }
    private void setRangeAndIndex() {
        getOriginIndex();
        mSharpnessRange = PQJni89.nativeGetSharpAdjRange();
        mGlobleSatRange = PQJni89.nativeGetSatAdjRange();

        mSkinToneRange = PQJni89.nativeGetSkinToneHRange();
        mGrassToneRange = PQJni89.nativeGetGrassToneHRange();
        mSkyToneRange = PQJni89.nativeGetSkyToneHRange();
        Log.d(TAG,"mSkinToneRange=="+mSkinToneRange);
        Log.d(TAG,"mGrassToneRange=="+mGrassToneRange);
        Log.d(TAG,"mSkyToneRange=="+mSkyToneRange);
        //add 3 bar 
        mSkinSatRange = PQJni89.nativeGetSkinToneSRange();
        mGrassSatRange = PQJni89.nativeGetGrassToneSRange();
        mSkySatRange = PQJni89.nativeGetSkyToneSRange();

        mTextViewSkyToneMin.setText(Integer.toString(mSkyToneRange / 2 + 1 - mSkyToneRange));
        mTextViewSkinToneMin.setText(Integer.toString(mSkinToneRange / 2 + 1 - mSkinToneRange));
        mTextViewGrassToneMin.setText(Integer.toString(mGrassToneRange / 2 + 1 - mGrassToneRange));
        /// only 10 
        mTextViewSharpnessRange.setText(Integer.toString(mSharpnessRange - 1));
        mTextViewGlobalSatRange.setText(Integer.toString(mGlobleSatRange - 1));

        mTextViewSkyToneRange.setText(Integer.toString((mSkyToneRange - 1) / 2));
        mTextViewSkinToneRange.setText(Integer.toString((mSkinToneRange - 1) / 2));
        mTextViewGrassToneRange.setText(Integer.toString((mGrassToneRange - 1) / 2));
        //add 3 new  bars
        mTextViewSkinSatRange.setText(Integer.toString((mSkinSatRange - 1) ));
        mTextViewGrassSatRange.setText(Integer.toString((mGrassSatRange - 1)));
        mTextViewSkySatRange.setText(Integer.toString((mSkySatRange - 1)));

        mTextViewSharpness.setText("Sharpness:  " + Integer.toString(PQJni89.nativeGetSharpAdjIndex()));
        mSeekBarSharpness.setProgress(PQJni89.nativeGetSharpAdjIndex() * 100 / (mSharpnessRange - 1));
        Log.d(TAG, "PQJni89.nativeGetSkyToneHIndex()=="+PQJni89.nativeGetSkyToneHIndex());
        Log.d(TAG, "PQJni89.nativeGetSkyToneHIndex()=="+(PQJni89.nativeGetSkyToneHIndex() - PQJni89.nativeGetSkyToneHIndex() / 2));
        mTextViewSkyTone.setText("Sky tone(Hue):  " + 
                Integer.toString(mSkyToneRange / 2 + 1 - mSkyToneRange +PQJni89.nativeGetSkyToneHIndex())); 
        mSeekBarSkyTone.setProgress(PQJni89.nativeGetSkyToneHIndex() * 100 / (mSkyToneRange - 1));

        mTextViewGrassTone.setText("Grass tone(Hue):  " + 
                Integer.toString(mGrassToneRange / 2 + 1 - mGrassToneRange + PQJni89.nativeGetGrassToneHIndex()));
        mSeekBarGrassTone.setProgress(PQJni89.nativeGetGrassToneHIndex() * 100 / (mGrassToneRange - 1));

        mTextViewSkinTone.setText("Skin tone(Hue):  " + 
                Integer.toString(mSkinToneRange / 2 + 1 - mSkinToneRange + PQJni89.nativeGetSkinToneHIndex()));
        mSeekBarSkinTone.setProgress(PQJni89.nativeGetSkinToneHIndex() * 100 / (mSkinToneRange - 1));
        mTextViewGlobal.setText("Global Sat.:  "  + Integer.toString(PQJni89.nativeGetSatAdjIndex()));
        mSeekBarGlobal.setProgress(PQJni89.nativeGetSatAdjIndex() * 100 / (mGlobleSatRange - 1));
        MtkLog.i(TAG, "SkyToneRange " + mSkyToneRange + " SkinToneRange " + mSkyToneRange + " GrassToneRange "
                + mGrassToneRange + " mColorRange " + mGlobleSatRange + " mSharpnessRange " + mSharpnessRange); 
  
        
        // add 3 new bars
        mTextViewSkinSat.setText("Skin tone(Sat):  " + Integer.toString(PQJni89.nativeGetSkinToneSIndex()));
        mSeekBarSkinSat.setProgress(PQJni89.nativeGetSkinToneSIndex() * 100 / (mSkinSatRange - 1));
        
        mTextViewGrassSat.setText("Grass tone(Sat):  " + Integer.toString(PQJni89.nativeGetGrassToneSIndex()));
        mSeekBarGrassSat.setProgress(PQJni89.nativeGetGrassToneSIndex() * 100 / (mGrassSatRange - 1));
        
        mTextViewSkySat.setText("Sky tone(Sat):  " + Integer.toString(PQJni89.nativeGetSkyToneSIndex()));
        mSeekBarSkySat.setProgress(PQJni89.nativeGetSkyToneSIndex() * 100 / (mSkySatRange - 1));
        
        
    }

    private void getOriginIndex() {
        origionSharpnessIndex = PQJni89.nativeGetSharpAdjIndex();
        origionSkyToneIndex = PQJni89.nativeGetSkyToneHIndex();
        origionGrassToneIndex = PQJni89.nativeGetGrassToneHIndex();
        origionSkinToneIndex = PQJni89.nativeGetSkinToneHIndex();
        origionSatAdjIndex = PQJni89.nativeGetSatAdjIndex();
        origionSkinToneSIndex = PQJni89.nativeGetSkinToneSIndex();
        origionGrassToneSIndex = PQJni89.nativeGetGrassToneSIndex();
        origionSkyToneSIndex = PQJni89.nativeGetSkyToneSIndex();
        Log.d(TAG,"origionSharpnessIndex=="+origionSharpnessIndex);
        Log.d(TAG,"origionSkyToneIndex=="+origionSkyToneIndex);
        Log.d(TAG,"origionGrassToneIndex=="+origionGrassToneIndex);
        Log.d(TAG,"origionSkinToneIndex=="+origionSkinToneIndex);
        Log.d(TAG,"origionSatAdjIndex=="+origionSatAdjIndex);
        Log.d(TAG,"origionSkinToneSIndex=="+origionSkinToneSIndex);
        Log.d(TAG,"origionGrassToneSIndex=="+origionGrassToneSIndex);
        Log.d(TAG,"origionSkyToneSIndex=="+ origionSkyToneSIndex);
    }
    
    private void onReDisplayPQImage() {
        try {
            mBitmap = BitmapFactory.decodeStream(getContentResolver().openInputStream(Uri.parse(pqUri)), null, options);
            if (mBitmap != null) {
                Log.d(TAG,"mBitmap.width=="+mBitmap.getWidth()+" mBitmap.height=="+mBitmap.getHeight()+" option.insamplesize=="+options.inSampleSize);
            } else {
                finish();
            }
       } catch (NullPointerException e) {
            MtkLog.e(TAG, "bitmapfactory decodestream fail");
            finish();
        } catch (FileNotFoundException e) {
            MtkLog.e(TAG, "bitmapfactory decodestream fail");
            finish();
        } catch (OutOfMemoryError w) {
              MtkLog.e(TAG, "bitmapfactory decodestream fail  out Of memory!!!!!!!!option.insampleSize=="+options.inSampleSize);
              finish();
        }
        
        Message mMessage = Message.obtain();
        mMessage.what = SUCCESS_LOAD_BITMAP;
        mHandler.sendMessage(mMessage);
    }

    private void onSaveClicked() {
        Intent intent = new Intent();
        Bundle bundle =  new Bundle();
        bundle.putInt("global", PQJni89.nativeGetSatAdjIndex());
        bundle.putInt("sharpness", PQJni89.nativeGetSharpAdjIndex());
        bundle.putInt("skyTone", PQJni89.nativeGetSkyToneHIndex());
        bundle.putInt("skinTone", PQJni89.nativeGetSkinToneHRange());
        bundle.putInt("grassTone", PQJni89.nativeGetGrassToneHRange());
        bundle.putInt("skinSat", PQJni89.nativeGetSkinToneSIndex());
        bundle.putInt("grassSat", PQJni89.nativeGetGrassToneSIndex());
        bundle.putInt("skySat", PQJni89.nativeGetSkyToneSIndex());
        intent.putExtras(bundle);
        setResult(RESULT_OK, intent);
    }
    private void addSeekBarListenerADVMode(){
        mGestureDetector = new GestureDetector(this,new MyGestureListener());
        hueSeekBarTouchBase = new SeekBarTouchBase(WindowsWidth, WindowsHeight, textView_hue_left, textView_hue, textView_hue_progress);
        // listener seek bar up and down drag event
        seekBar_hue.setOnTouchListener(hueSeekBarTouchBase);
        saturationSeekBarTouchBase = new SeekBarTouchBase(WindowsWidth, WindowsHeight, textView_saturation_left, textView_saturation, textView_saturation_progress);
        // listener seek bar up and down drag event
        seekBar_saturation.setOnTouchListener(saturationSeekBarTouchBase);
        SeekBarTouchVisibleLisenter mSeekBarTouchVisibleLisenter = new SeekBarTouchVisibleLisenter();
        // control the visible of TextView
        hueSeekBarTouchBase.setLisenter(mSeekBarTouchVisibleLisenter, seekBar_hue);
        saturationSeekBarTouchBase.setLisenter(mSeekBarTouchVisibleLisenter, seekBar_saturation);
        // listener seek bar left and right drag event
        addSeekBarListenerADVmode();
    return ;
    }
    
    private void  addSeekBarListenerADVmode() {
        seekBar_hue.setOnSeekBarChangeListener(mOnSeekBarChangelisenter);
        seekBar_saturation.setOnSeekBarChangeListener(mOnSeekBarChangelisenter);
    }
    
    private void  addSeekBarListener() {
        mSeekBarChangeLisenter= new SeekBarChangeLisenter();
        mOnSeekBarChangelisenter.setLisenter(mSeekBarChangeLisenter);
        mSeekBarSharpness.setOnSeekBarChangeListener(mOnSeekBarChangelisenter);
        mSeekBarGlobal.setOnSeekBarChangeListener(mOnSeekBarChangelisenter);
        mSeekBarSkinTone.setOnSeekBarChangeListener(mOnSeekBarChangelisenter);
        mSeekBarGrassTone.setOnSeekBarChangeListener(mOnSeekBarChangelisenter);
        mSeekBarSkyTone.setOnSeekBarChangeListener(mOnSeekBarChangelisenter);
        mSeekBarSkinSat.setOnSeekBarChangeListener(mOnSeekBarChangelisenter);
        mSeekBarGrassSat.setOnSeekBarChangeListener(mOnSeekBarChangelisenter);
        mSeekBarSkySat.setOnSeekBarChangeListener(mOnSeekBarChangelisenter);

    }

    public class DecodeImage implements Runnable{
        private  boolean needDecodeBound ; 
        public void setNeedDecodeBound(boolean needDecodeBound){
            this.needDecodeBound = needDecodeBound;
        }
        @Override
        public void run() {
            // TODO Auto-generated method stub
            if (needDecodeBound) {
                options.inJustDecodeBounds = true;
                try {
                    ///Modify temple 
                    mbitmapRegionDecoder = null;// BitmapRegionDecoder.newInstance(getContentResolver().openInputStream(Uri.parse(pqUri)), false);
                    // Log.d("wjxjni", "[mbitmapRegionDecoder]height=="+mbitmapRegionDecoder.getHeight()+" width="+mbitmapRegionDecoder.getWidth() );
                    mBitmap = BitmapFactory.decodeStream(getContentResolver().openInputStream(Uri.parse(pqUri)), null, options);
                }catch (FileNotFoundException e) {
                    MtkLog.e("wjx4", "bitmapfactory decodestream fail");
                }catch (IOException e) {
                    MtkLog.e("wjx4", "bitmapfactory decodestream fail");
                }
                int width = options.outWidth;
                int height = options.outHeight;
               if (width != 0 && height != 0) {
                   options.inSampleSize = caculateInSampleSize(pqUri, width , height);
               } else {
                   options.inSampleSize = 1;
               }
               if(mImageViewTouchBase != null) {
                   mImageViewTouchBase.setInsampleSize(options.inSampleSize);
               }

                options.inJustDecodeBounds = false;
            }
            needDecodeBound = false;
            onReDisplayPQImage();
        }
        
        public int caculateInSampleSize(String pqUri, int width , int height) {
            int targetSize = MediaItem.getTargetSize(MediaItem.TYPE_THUMBNAIL);
            String mMineType = getMineType(Uri.parse(pqUri));
            if (mMineType != null) {
                if (!BitmapUtils.isSupportedByRegionDecoder(mMineType)) {
                    targetSize = Params.THUMBNAIL_TARGET_SIZE_LARGER;
                }
            }
            float scale = (float) targetSize / Math.max(width, height);
            options.inSampleSize = BitmapUtils.computeSampleSizeLarger(scale);
            Log.d(TAG, " pq  options.inSampleSize=="+options.inSampleSize +" width=="+width+ " height=="+height + "targetSize=="+targetSize);
            return options.inSampleSize;
        }
        public  String getMineType(Uri mUri) {
            Log.d(TAG, "Path==="+mUri.toString());
            Cursor cursor = null;
            String mMineType = null;
            try {
                if (mUri != null) {
                    cursor = getContentResolver().query(
                            mUri,
                            new String[]{ImageColumns.MIME_TYPE},
                            null,
                            null,
                            null);
                    if (cursor != null && cursor.moveToFirst()) {
                        mMineType = cursor.getString(0);
                        Log.d(TAG,"  mMineType===== "+mMineType);
                    }
                }
            } catch (Exception e) {
                Log.d(TAG, "[ ]:"+e);
            } finally {
                cursor.close();
            }
             return mMineType;
        }
    }

    private class MyGestureListener
    extends GestureDetector.SimpleOnGestureListener {
    // M: modified for MTK UX issues:
    // use onSingleTapConfirmed to avoid action bar from poping up
    // during double tap gesture.
    //@Override
    //public boolean onSingleTapUp(MotionEvent e) {
    //    return mListener.onSingleTapUp(e.getX(), e.getY());
    //}

    @Override
    public boolean onDoubleTap(MotionEvent e) {
        Log.d(TAG, "onDoubleTap");
        return false;
    }

    @Override
    public boolean onScroll(
            MotionEvent e1, MotionEvent e2, float dx, float dy) {
        Log.d(TAG, "onScroll");
        return false;
    }

    @Override
    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
            float velocityY) {
        Log.d(TAG, "onFling");
        
        return false;
    }

    @Override
    public boolean onSingleTapConfirmed(MotionEvent e) {
        Log.d(TAG, "onSingleTapConfirmed");
        return false;
    }
}
    public void setTextViewPositionAsSeekBar (View parent ,View leftOfParent, View rightOfParent, View bottomAndcenterOfParent ) {
        int left = parent.getLeft();
        int top = parent.getTop();
        int right = parent.getRight();
        int bottom = parent.getBottom();
        int width = parent.getWidth();
        Log.d(TAG, "left=="+left+" top=="+top+" right=="+right+" bottom=="+bottom+" width="+width );
        LayoutParams paramsLeft = new LayoutParams(LayoutParams.WRAP_CONTENT,
                LayoutParams.WRAP_CONTENT, left, top);
        leftOfParent.setLayoutParams(paramsLeft);
        LayoutParams paramsRight = new LayoutParams(LayoutParams.WRAP_CONTENT,
               LayoutParams.WRAP_CONTENT, right, top);
        rightOfParent.setLayoutParams(paramsRight);
        LayoutParams paramsCenter = new LayoutParams(LayoutParams.WRAP_CONTENT,
               LayoutParams.WRAP_CONTENT, left + width/2, bottom);
        bottomAndcenterOfParent.setLayoutParams(paramsCenter);
    }
    public class SettingXYAxisLisenter implements SetXYAxisIndex {

        @Override
        public void setAxisIndex(int x, int y) {
            // TODO Auto-generated method stub
            Log.d(TAG,"[setAxisIndex]  x== "+x+ "   y=="+y);
            PQJni89.nativeSetXAxisIndex(x);
            PQJni89.nativeSetYAxisIndex(y);
            //setRangeAndIndexADVMode();
        }
        
    }
    public class VisibleLisenter implements SetViewVisible {
        @Override
        public void setVisible(Object obj) {
            // TODO Auto-generated method stub
            if (seekBar_hue.getVisibility() == View.GONE || seekBar_saturation.getVisibility() == View.GONE) {
                setVisibilityADM(seekBar_hue, View.VISIBLE);
                setVisibilityADM(textView_hue_left_temple, View.VISIBLE);
                setVisibilityADM(textView_hue_temple, View.VISIBLE);
                setVisibilityADM(textView_hue_progress_temple, View.VISIBLE);
                
                setVisibilityADM(seekBar_saturation, View.VISIBLE);
                setVisibilityADM(textView_saturation_left_temple, View.VISIBLE);
                setVisibilityADM(textView_saturation_temple, View.VISIBLE);
                setVisibilityADM(textView_saturation_progress_temple, View.VISIBLE);
            }
        }
    }

    public class SeekBarTouchVisibleLisenter implements SetViewVisible {
        @Override
        public void setVisible(Object obj) {
            // TODO Auto-generated method stub
            if ((SeekBar)obj == seekBar_hue && (textView_hue_left_temple.getVisibility() == View.VISIBLE)) {
                setVisibilityADM(textView_hue_left_temple, View.GONE);
                setVisibilityADM(textView_hue_temple, View.GONE);
                setVisibilityADM(textView_hue_progress_temple, View.GONE);
            } else if ((SeekBar)obj == seekBar_saturation && (textView_saturation_left_temple.getVisibility() == View.VISIBLE)) {
                setVisibilityADM(textView_saturation_left_temple, View.GONE);
                setVisibilityADM(textView_saturation_temple, View.GONE);
                setVisibilityADM(textView_saturation_progress_temple, View.GONE);
            }
        }
    }
    @Override
    public void onBackPressed() {
        // send the back event to the top sub-state
        recoverIndex();
        super.onBackPressed();
    }
    public class SeekBarChangeLisenter implements SeekBarChangeInterface {

        @Override
        public void progressChanged(SeekBar seekBar, int progress,
                boolean fromUser) {
            // TODO Auto-generated method stub
            if (mSeekBarSharpness == seekBar) {
                mSharpnessOption = (progress * (mSharpnessRange - 1)) / 100;
                mTextViewSharpness.setText("Sharpness:  " + mSharpnessOption);
            } else if (mSeekBarGlobal == seekBar) {
                mGlobalSatOption = (progress * (mGlobleSatRange - 1)) / 100;
                mTextViewGlobal.setText("GlobalSat:   " + mGlobalSatOption);
            } else if (mSeekBarSkinTone == seekBar) {
                mSkinToneOption = (progress * (mSkinToneRange - 1)) / 100;
                mTextViewSkinTone.setText("Skin tone(Hue):   " + (mSkinToneOption - 3));
            } else if (mSeekBarGrassTone == seekBar) {
                mGrassToneOption = (progress * (mGrassToneRange - 1)) / 100;
                mTextViewGrassTone.setText("Grass tone(Hue):  " + (mGrassToneOption - 3));
            } else if (mSeekBarSkyTone == seekBar) {
                mSkyToneOption = (progress * (mSkyToneRange - 1)) / 100;
                mTextViewSkyTone.setText("Sky tone(Hue):  " + (mSkyToneOption - 3));
            } else if (mSeekBarSkinSat == seekBar) {
                mSkinSatOption = (progress * (mSkinSatRange - 1)) / 100;
                mTextViewSkinSat.setText("Skin tone(Sat):  " + mSkinSatOption);
            } else if (mSeekBarGrassSat == seekBar) {
                mGrassSatOption = (progress * (mGrassSatRange - 1)) / 100;
                mTextViewGrassSat.setText("Grass tone(Sat):  " + mGrassSatOption);
            } else if (mSeekBarSkySat == seekBar) {
                mSkySatOption = (progress * (mSkySatRange - 1)) / 100;
                mTextViewSkySat.setText("Sky tone(Sat):  " + mSkySatOption);
            } else if (seekBar_hue == seekBar) {
                mHudOptionADV = (progress * (mHudRangeADV - 1)) / 100;
                textView_hue_progress.setText("Hue:  " + mHudOptionADV);
            } else if (seekBar_saturation == seekBar) {
                mSatOptionADV = (progress * (mSatRangeADV - 1)) / 100;
                textView_saturation_progress.setText("Sat:  " + mSatOptionADV);
            }
        }

        @Override
        public void startTrackingTouch(SeekBar seekBar) {
            // TODO Auto-generated method stub
        }

        @Override
        public void stopTrackingTouch(SeekBar seekBar) {
            // TODO Auto-generated method stub
            if (mSeekBarSharpness == seekBar) {
                PQJni89.nativeSetSharpAdjIndex(mSharpnessOption);
                 MtkLog.i(TAG, "Sharpness Index is " + PQJni89.nativeGetSharpAdjIndex());
            } else if (mSeekBarGlobal == seekBar) {
                PQJni89.nativeSetSatAdjIndex(mGlobalSatOption);
                MtkLog.i(TAG, "Color Index is " + PQJni89.nativeGetSatAdjIndex());
            } else if (mSeekBarSkinTone == seekBar) {
                PQJni89.nativeSetSkinToneHIndex(mSkinToneOption);
                MtkLog.i(TAG, "SkinTone Index is " + PQJni89.nativeGetSkinToneHIndex());
            } else if (mSeekBarGrassTone == seekBar) {
                PQJni89.nativeSetGrassToneHIndex(mGrassToneOption);
                MtkLog.i(TAG, "GrassTone Index is " + PQJni89.nativeGetGrassToneHIndex());
            } else if(mSeekBarSkyTone == seekBar) {
                PQJni89.nativeSetSkyToneHIndex(mSkyToneOption);
                MtkLog.i(TAG, "SkyTone Index is " + PQJni89.nativeGetSkyToneHIndex());
            } else if (mSeekBarSkinSat == seekBar) {
                PQJni89.nativeSetSkinToneSIndex(mSkinSatOption);
            } else if (mSeekBarGrassSat == seekBar) {
                PQJni89.nativeSetGrassToneSIndex(mGrassSatOption);
            } else if (mSeekBarSkySat == seekBar) {
                PQJni89.nativeSetSkyToneSIndex(mSkySatOption);
            } else if (seekBar_hue == seekBar) {
                PQJni89.nativeSetHueAdjIndex(mHudOptionADV);
            } else if (seekBar_saturation == seekBar) {
                PQJni89.nativeSetSatAdjIndex(mSatOptionADV);
            }
         }
    }
}

