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
/*     file    :packages/apps/JrdGallery/src/com/jrdcom/android/gallery3d/filtershow/FilterShowActivity.java      */
/*     Labels  :                                                                           */
/*================================================================================================================*/
/* Modifications   (month/day/year)                                                        */
/*================================================================================================================*/
/* date    | author       |FeatureID                |modification                          */
/*============|==============|=========================|==========================================================*/
/*08/06/13 | zhangcheng |PR498772-zhangcheng-001 |Pop up gallery force close when tap home key during loanding images. */
/*============|==============|=========================|==========================================================*/
/*
 * Copyright (C) 2012 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.jrdcom.android.gallery3d.filtershow;
import java.io.File;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.Vector;

import android.annotation.TargetApi;
import android.app.ActionBar;
import android.app.ActionBar.LayoutParams;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.database.ContentObserver;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.Display;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.ShareActionProvider;
import android.widget.ShareActionProvider.OnShareTargetSelectedListener;
import android.widget.SlidingDrawer;
import android.widget.Toast;

import com.jrdcom.android.gallery3d.R;
import com.jrdcom.android.gallery3d.data.LocalAlbum;
import com.jrdcom.android.gallery3d.filtershow.cache.ImageLoader;
import com.jrdcom.android.gallery3d.filtershow.filters.ImageFilter;
import com.jrdcom.android.gallery3d.filtershow.filters.ImageFilterBorder;
import com.jrdcom.android.gallery3d.filtershow.filters.ImageFilterBwFilter;
import com.jrdcom.android.gallery3d.filtershow.filters.ImageFilterContrast;
import com.jrdcom.android.gallery3d.filtershow.filters.ImageFilterExposure;
import com.jrdcom.android.gallery3d.filtershow.filters.ImageFilterFx;
import com.jrdcom.android.gallery3d.filtershow.filters.ImageFilterHue;
import com.jrdcom.android.gallery3d.filtershow.filters.ImageFilterParametricBorder;
import com.jrdcom.android.gallery3d.filtershow.filters.ImageFilterRS;
import com.jrdcom.android.gallery3d.filtershow.filters.ImageFilterSaturated;
import com.jrdcom.android.gallery3d.filtershow.filters.ImageFilterShadows;
import com.jrdcom.android.gallery3d.filtershow.filters.ImageFilterTinyPlanet;
import com.jrdcom.android.gallery3d.filtershow.filters.ImageFilterVibrance;
import com.jrdcom.android.gallery3d.filtershow.filters.ImageFilterVignette;
import com.jrdcom.android.gallery3d.filtershow.filters.ImageFilterWBalance;
import com.jrdcom.android.gallery3d.filtershow.imageshow.ImageBorder;
import com.jrdcom.android.gallery3d.filtershow.imageshow.ImageCrop;
import com.jrdcom.android.gallery3d.filtershow.imageshow.ImageFlip;
import com.jrdcom.android.gallery3d.filtershow.imageshow.ImageRotate;
import com.jrdcom.android.gallery3d.filtershow.imageshow.ImageShow;
import com.jrdcom.android.gallery3d.filtershow.imageshow.ImageSmallBorder;
import com.jrdcom.android.gallery3d.filtershow.imageshow.ImageSmallFilter;
import com.jrdcom.android.gallery3d.filtershow.imageshow.ImageStraighten;
import com.jrdcom.android.gallery3d.filtershow.imageshow.ImageTinyPlanet;
import com.jrdcom.android.gallery3d.filtershow.imageshow.ImageWithIcon;
import com.jrdcom.android.gallery3d.filtershow.imageshow.ImageZoom;
import com.jrdcom.android.gallery3d.filtershow.presets.ImagePreset;
import com.jrdcom.android.gallery3d.filtershow.provider.SharedImageProvider;
import com.jrdcom.android.gallery3d.filtershow.tools.SaveCopyTask;
import com.jrdcom.android.gallery3d.filtershow.ui.FramedTextButton;
import com.jrdcom.android.gallery3d.filtershow.ui.ImageButtonTitle;
import com.jrdcom.android.gallery3d.filtershow.ui.ImageCurves;
import com.jrdcom.android.gallery3d.filtershow.ui.Spline;
import com.jrdcom.android.gallery3d.ui.BeautyBlurActivity;
import com.jrdcom.android.gallery3d.ui.BeautyColorActivity;
import com.jrdcom.android.gallery3d.ui.BeautyEffectActivity;
import com.jrdcom.android.gallery3d.ui.BeautyFrameActivity;
import com.jrdcom.android.gallery3d.ui.BeautyMopiActivity;
import com.jrdcom.android.gallery3d.ui.BeautyMosaicActivity;
import com.jrdcom.android.gallery3d.ui.BeautyWordActivity;
import com.jrdcom.android.gallery3d.ui.IMGEditActivity;
import com.jrdcom.android.gallery3d.util.GalleryUtils;
import com.jrdcom.example.joinpic.Utils;
import com.jrdcom.mt.MTActivity;
import com.jrdcom.mt.core.BeautyControl;
import com.jrdcom.mt.core.BitmapUtil;
import com.jrdcom.mt.core.ToolBase;
import com.jrdcom.mt.mtxx.controls.DialogWait;
import com.jrdcom.mt.mtxx.controls.MtprogressDialog;
import com.jrdcom.mt.util.FileUtils;
import com.jrdcom.mt.util.MyData;
import com.jrdcom.mt.mtxx.controls.OnSetPauseListener;

@TargetApi(16)
public class FilterShowActivity extends Activity implements OnItemClickListener,OnClickListener,
        OnShareTargetSelectedListener, MTActivity.MTPauseLisener, OnSetPauseListener /*added by jipu.xiong@tcl.com*/ {
    public static final String CROP_ACTION = "com.android.camera.action.EDITOR_CROP";
    public static final String TINY_PLANET_ACTION = "com.android.camera.action.TINY_PLANET";
    public static final String LAUNCH_FULLSCREEN = "launch-fullscreen";
    private final PanelController mPanelController = new PanelController();
    private ImageLoader mImageLoader = null;
    private ImageShow mImageShow = null;
    private ImageCurves mImageCurves = null;
    private ImageBorder mImageBorders = null;
    private ImageStraighten mImageStraighten = null;
    private ImageZoom mImageZoom = null;
    private ImageCrop mImageCrop = null;
    private ImageRotate mImageRotate = null;
    private ImageFlip mImageFlip = null;
    private ImageTinyPlanet mImageTinyPlanet = null;

    private View mListFx = null;
    private View mListBorders = null;
    private View mListGeometry = null;
    private View mListColors = null;
    private View mListFilterButtons = null;

    private ImageButton mFxButton = null;
    private ImageButton mBorderButton = null;
    private ImageButton mGeometryButton = null;
    private ImageButton mColorsButton = null;
    
    
    private SlidingDrawer mdrawer;
    private ImageButton mslidingButton;

    private ImageSmallFilter mCurrentImageSmallFilter = null;
    private static final int SELECT_PICTURE = 1;
    private static final String LOGTAG = "FilterShowActivity";
    protected static final boolean ANIMATE_PANELS = true;
    private static int mImageBorderSize = 4; // in percent

    private boolean mShowingHistoryPanel = false;
    private boolean mShowingImageStatePanel = false;
    private boolean mshowingMenuItemDeleteAndShare = true;

    private final Vector<ImageShow> mImageViews = new Vector<ImageShow>();
    private final Vector<View> mListViews = new Vector<View>();
    private final Vector<ImageButton> mBottomPanelButtons = new Vector<ImageButton>();
    //delete this lines added by yaogang.hao for PR470988
//    private ShareActionProvider mShareActionProvider;
    private File mSharedOutputFile = null;

    private boolean mSharingImage = false;

    private WeakReference<ProgressDialog> mSavingProgressDialog;
    private static final int SEEK_BAR_MAX = 600;

    private LoadBitmapTask mLoadBitmapTask;
    private ImageSmallFilter mNullFxFilter;
    private ImageSmallFilter mNullBorderFilter;
    private Button btn_color;
    //add by biao.luo begin
    private final int TIMER_INVALIDATE = 0x101;
    private final int MSG_MEDIA_SCANNER = 1000;
    private final int LOAD_PIC = 0x102;
    private BeautyControl mBeautyControl = MyData.getBeautyControl();
    private String finalFilePath = null;
    private Button mImageEditButton;
    private Button mWhitemopiButton;
    private Button mEnhanceButton;
    private Button mEffectButton;
    private Button mFrameButton;
    private Button mMosaicButton;
    private Button mWordsButton;
    private Button mWeakButton;
    private MenuItem undoItem;
    private MenuItem redoItem;
    private MediaDatatObserver mediaDatatObserver;
    private boolean isClicked = false;//add by biao.luo for pr472873
    //add by biao.luo end

	private boolean isSharing = false;
	//add by zhangcheng for PR498772 begin	
	private PauseListener mPauseListener;

	public void setPauseListener(PauseListener pauseListener){
		mPauseListener = pauseListener;
	}
	
    // yaogang.hao for PR 548850
    public final String UPDATE_ACTION = "com.photoedit.update_action";
    BroadcastReceiver updatereReceiver = new BroadcastReceiver() {
        
        @Override
        public void onReceive(Context arg0, Intent arg1) {
            refreshPreviewImageViewIfNeed();
            isClicked = false;
            mImageShow.setProcBitmap(true);
        }
    };
	
	//add by zhangcheng for PR498772 end

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // yaogang.hao for PR 548850
        IntentFilter filter = new IntentFilter(UPDATE_ACTION);
        registerReceiver(updatereReceiver, filter);
        
        ImageFilterRS.setRenderScriptContext(this);

        ImageShow.setDefaultBackgroundColor(getResources().getColor(R.color.background_screen));
        ImageSmallFilter.setDefaultBackgroundColor(getResources().getColor(R.color.background_main_toolbar));
        // TODO: get those values from XML.
        ImageZoom.setZoomedSize(getPixelsFromDip(256));
        FramedTextButton.setTextSize((int) getPixelsFromDip(14));
        ImageShow.setTextSize((int) getPixelsFromDip(12));
        ImageShow.setTextPadding((int) getPixelsFromDip(10));
        ImageShow.setOriginalTextMargin((int) getPixelsFromDip(4));
        ImageShow.setOriginalTextSize((int) getPixelsFromDip(18));
        ImageShow.setOriginalText(getResources().getString(R.string.original_picture_text));
        ImageButtonTitle.setTextSize((int) getPixelsFromDip(12));
        ImageButtonTitle.setTextPadding((int) getPixelsFromDip(10));
        ImageSmallFilter.setMargin((int) getPixelsFromDip(3));
        ImageSmallFilter.setTextMargin((int) getPixelsFromDip(4));
        Drawable curveHandle = getResources().getDrawable(R.drawable.camera_crop);
        int curveHandleSize = (int) getResources().getDimension(R.dimen.crop_indicator_size);
        Spline.setCurveHandle(curveHandle, curveHandleSize);
        Spline.setCurveWidth((int) getPixelsFromDip(3));

        setContentView(R.layout.filtershow_activity);
        //add by biao.luo begin
        initDevData();
        
        //yaogang.hao for PR 544189
      //1:clear some variables
        MyData.clearCacheFiles();
        MyData.mCurr_index = -1;
        MyData.cacheBitmaps.clear();
        BeautyControl.cacheRefCount = -1;
        mBeautyControl.clearMemory();
        
        mImageEditButton=(Button)findViewById(R.id.btn_edit);
        mWhitemopiButton=(Button)findViewById(R.id.btn_menu_white);
        mEnhanceButton=(Button)findViewById(R.id.btn_color);
        mEffectButton=(Button)findViewById(R.id.btn_effect);
        mFrameButton=(Button)findViewById(R.id.btn_frame);
        mMosaicButton=(Button)findViewById(R.id.btn_mosaic);
        mWordsButton=(Button)findViewById(R.id.btn_words);
        mWeakButton=(Button)findViewById(R.id.btn_weak);
        mImageEditButton.setOnClickListener(this);
        mWhitemopiButton.setOnClickListener(this);
        mEnhanceButton.setOnClickListener(this);
        mEffectButton.setOnClickListener(this);
        mFrameButton.setOnClickListener(this);
        mMosaicButton.setOnClickListener(this);
        mWordsButton.setOnClickListener(this);
        mWeakButton.setOnClickListener(this);
        loadImageWithFilePath(finalFilePath);
        MyData.setAPKPathToNDK(getApplicationContext());
        try {
            MyData.NDKCheckColorARGB8888Index( this.getAssets().open("ndk_check_color.bmp") );
        } catch (IOException e) {
        }
//        btn_color.setOnClickListener(new OnClickListener(){
//
//           @Override
//           public void onClick(View v) {
//               Intent intent = new Intent();
//               intent.setClass(FilterShowActivity.this, BeautyColorActivity.class);
//               startActivity(intent);
//           }
//        });
        //Develop for Photo Edit, the following codes for the Action Bar on the Photo Edit UI
        final ActionBar actionBar = getActionBar();
        actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        actionBar.setCustomView(R.layout.filtershow_actionbar);
        actionBar.getCustomView().setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                mBeautyControl.clearMemory();
                FilterShowActivity.this.finish();
            }
        });
        actionBar.getCustomView().setVisibility(View.VISIBLE);
        mshowingMenuItemDeleteAndShare = false;


//        invalidateOptionsMenu();
//        actionBar.setCustomView(R.layout.gogallery_actionbar);
//        actionBar.setCustomView(R.layout.filtershow_actionbar);
//        actionBar.getCustomView().setOnClickListener(new OnClickListener() {
//            @Override
//            public void onClick(View view) {
//
//
//
//                setResult(RESULT_OK, new Intent().setData(mImageLoader.getUri()));
//                finish();
//            }
//        });

        //add by biao.luo end 
        //Develop for Photo Edit, the following codes for the sliding Menu on the Phont Edit UI 
        mdrawer = (SlidingDrawer) findViewById(R.id.slidingdrawer);
        mslidingButton = (ImageButton) findViewById(R.id.edithandle);

        int dip = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                60, getResources().getDisplayMetrics());

        mdrawer.setLayoutParams(new LinearLayout.LayoutParams(
                LayoutParams.FILL_PARENT, dip));
        mslidingButton.setLayoutParams(new LinearLayout.LayoutParams(
                LayoutParams.FILL_PARENT, dip));

        mdrawer.setOnDrawerOpenListener(new SlidingDrawer.OnDrawerOpenListener() {
            @Override
            public void onDrawerOpened() {
                actionBar.getCustomView().setVisibility(View.GONE);
                actionBar.setCustomView(R.layout.filtershow_actionbar);
                actionBar.getCustomView().setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        saveImage();
                    }
                });
                
                
                //update the Menu on the ActionBar
                mslidingButton.setImageResource(R.drawable.filtershow_button_sliding_down);
                actionBar.getCustomView().setVisibility(View.VISIBLE);
                mshowingMenuItemDeleteAndShare = false;

               //update the size of the Handle menu.
                int dip = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                        178, getResources().getDisplayMetrics());

                mdrawer.setLayoutParams(new LinearLayout.LayoutParams(
                        LayoutParams.FILL_PARENT, dip));
                mslidingButton.setLayoutParams(new LinearLayout.LayoutParams(
                        LayoutParams.FILL_PARENT, (int) TypedValue.applyDimension(
                                TypedValue.COMPLEX_UNIT_DIP,
                                30, getResources().getDisplayMetrics())));

                invalidateOptionsMenu();
                

            }

        });

        mdrawer.setOnDrawerCloseListener(new SlidingDrawer.OnDrawerCloseListener() {

            @Override
            public void onDrawerClosed() {
                mslidingButton.setImageResource(R.drawable.filtershow_button_sliding_up);
                actionBar.getCustomView().setVisibility(View.GONE);
                actionBar.setCustomView(R.layout.gogallery_actionbar);
                actionBar.getCustomView().setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View view) {
					//back the last Activity.
                        setResult(RESULT_OK, new Intent().setData(mImageLoader.getUri()));
                        finish();
                    }
                });
                mshowingMenuItemDeleteAndShare = true;

                int dip = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                        60, getResources().getDisplayMetrics());

                mdrawer.setLayoutParams(new LinearLayout.LayoutParams(
                        LayoutParams.FILL_PARENT, dip));
                mslidingButton.setLayoutParams(new LinearLayout.LayoutParams(
                        LayoutParams.FILL_PARENT, dip));
                
                invalidateOptionsMenu();

            }

        });

        mdrawer.setOnDrawerScrollListener(new SlidingDrawer.OnDrawerScrollListener() {

            @Override
            public void onScrollEnded() {

            }

            @Override
            public void onScrollStarted() {

            }
        });

        mImageLoader = new ImageLoader(this, getApplicationContext());

        LinearLayout listFilters = (LinearLayout) findViewById(R.id.listFilters);
        LinearLayout listBorders = (LinearLayout) findViewById(R.id.listBorders);
        LinearLayout listColors = (LinearLayout) findViewById(R.id.listColorsFx);

        mImageShow = (ImageShow) findViewById(R.id.imageShow);
        mImageCurves = (ImageCurves) findViewById(R.id.imageCurves);
        mImageBorders = (ImageBorder) findViewById(R.id.imageBorder);
        mImageStraighten = (ImageStraighten) findViewById(R.id.imageStraighten);
        mImageZoom = (ImageZoom) findViewById(R.id.imageZoom);
        mImageCrop = (ImageCrop) findViewById(R.id.imageCrop);
        mImageRotate = (ImageRotate) findViewById(R.id.imageRotate);
        mImageFlip = (ImageFlip) findViewById(R.id.imageFlip);
        mImageTinyPlanet = (ImageTinyPlanet) findViewById(R.id.imageTinyPlanet);

        ImageCrop.setTouchTolerance((int) getPixelsFromDip(25));
        mImageViews.add(mImageShow);
        mImageViews.add(mImageCurves);
        mImageViews.add(mImageBorders);
        mImageViews.add(mImageStraighten);
        mImageViews.add(mImageZoom);
        mImageViews.add(mImageCrop);
        mImageViews.add(mImageRotate);
        mImageViews.add(mImageFlip);
        mImageViews.add(mImageTinyPlanet);

        mListFx = findViewById(R.id.fxList);
        mListBorders = findViewById(R.id.bordersList);
        mListGeometry = findViewById(R.id.geometryList);
        mListFilterButtons = findViewById(R.id.filterButtonsList);
        mListColors = findViewById(R.id.colorsFxList);
        mListViews.add(mListFx);
        mListViews.add(mListBorders);
        mListViews.add(mListGeometry);
        mListViews.add(mListFilterButtons);
        mListViews.add(mListColors);

        mFxButton = (ImageButton) findViewById(R.id.fxButton);
        mBorderButton = (ImageButton) findViewById(R.id.borderButton);
        mGeometryButton = (ImageButton) findViewById(R.id.geometryButton);
        mColorsButton = (ImageButton) findViewById(R.id.colorsButton);

        mImageShow.setImageLoader(mImageLoader);
        mImageCurves.setImageLoader(mImageLoader);
        mImageCurves.setMaster(mImageShow);
        mImageBorders.setImageLoader(mImageLoader);
        mImageBorders.setMaster(mImageShow);
        mImageStraighten.setImageLoader(mImageLoader);
        mImageStraighten.setMaster(mImageShow);
        mImageZoom.setImageLoader(mImageLoader);
        mImageZoom.setMaster(mImageShow);
        mImageCrop.setImageLoader(mImageLoader);
        mImageCrop.setMaster(mImageShow);
        mImageRotate.setImageLoader(mImageLoader);
        mImageRotate.setMaster(mImageShow);
        mImageFlip.setImageLoader(mImageLoader);
        mImageFlip.setMaster(mImageShow);
        mImageTinyPlanet.setImageLoader(mImageLoader);
        mImageTinyPlanet.setMaster(mImageShow);

        mPanelController.setActivity(this);

        mPanelController.addImageView(findViewById(R.id.imageShow));
        mPanelController.addImageView(findViewById(R.id.imageCurves));
        mPanelController.addImageView(findViewById(R.id.imageBorder));
        mPanelController.addImageView(findViewById(R.id.imageStraighten));
        mPanelController.addImageView(findViewById(R.id.imageCrop));
        mPanelController.addImageView(findViewById(R.id.imageRotate));
        mPanelController.addImageView(findViewById(R.id.imageFlip));
        mPanelController.addImageView(findViewById(R.id.imageZoom));
        mPanelController.addImageView(findViewById(R.id.imageTinyPlanet));

        mPanelController.addPanel(mFxButton, mListFx, 0);
        mPanelController.addPanel(mBorderButton, mListBorders, 1);

        mPanelController.addPanel(mGeometryButton, mListGeometry, 2);
        mPanelController.addComponent(mGeometryButton, findViewById(R.id.straightenButton));
        mPanelController.addComponent(mGeometryButton, findViewById(R.id.cropButton));
        mPanelController.addComponent(mGeometryButton, findViewById(R.id.rotateButton));
        mPanelController.addComponent(mGeometryButton, findViewById(R.id.flipButton));

        mPanelController.addPanel(mColorsButton, mListColors, 3);

        int[] recastIDs = {
                R.id.tinyplanetButton,
                R.id.vignetteButton,
                R.id.vibranceButton,
                R.id.contrastButton,
                R.id.saturationButton,
                R.id.bwfilterButton,
                R.id.wbalanceButton,
                R.id.hueButton,
                R.id.exposureButton,
                R.id.shadowRecoveryButton
        };
        ImageFilter[] filters = {
                new ImageFilterTinyPlanet(),
                new ImageFilterVignette(),
                new ImageFilterVibrance(),
                new ImageFilterContrast(),
                new ImageFilterSaturated(),
                new ImageFilterBwFilter(),
                new ImageFilterWBalance(),
                new ImageFilterHue(),
                new ImageFilterExposure(),
                new ImageFilterShadows()
        };

        for (int i = 0; i < filters.length; i++) {
            ImageSmallFilter fView = new ImageSmallFilter(this);
            View v = listColors.findViewById(recastIDs[i]);
            int pos = listColors.indexOfChild(v);
            listColors.removeView(v);

            filters[i].setParameter(filters[i].getPreviewParameter());
            if (v instanceof ImageButtonTitle)
                filters[i].setName(((ImageButtonTitle) v).getText());
            fView.setImageFilter(filters[i]);
            fView.setController(this);
            fView.setImageLoader(mImageLoader);
            fView.setId(recastIDs[i]);
            mPanelController.addComponent(mColorsButton, fView);
            listColors.addView(fView, pos);
        }

        int[] overlayIDs = {
                R.id.sharpenButton,
                R.id.curvesButtonRGB
        };
        int[] overlayBitmaps = {
                R.drawable.filtershow_button_colors_sharpen,
                R.drawable.filtershow_button_colors_curve
        };
        int[] overlayNames = {
                R.string.sharpness,
                R.string.curvesRGB
        };

        for (int i = 0; i < overlayIDs.length; i++) {
            ImageWithIcon fView = new ImageWithIcon(this);
            View v = listColors.findViewById(overlayIDs[i]);
            int pos = listColors.indexOfChild(v);
            listColors.removeView(v);
            final int sid = overlayNames[i];
            ImageFilterExposure efilter = new ImageFilterExposure() {
                {
                    mName = getString(sid);
                }
            };
            efilter.setParameter(-300);
            Bitmap bitmap = BitmapFactory.decodeResource(getResources(),
                    overlayBitmaps[i]);

            fView.setIcon(bitmap);
            fView.setImageFilter(efilter);
            fView.setController(this);
            fView.setImageLoader(mImageLoader);
            fView.setId(overlayIDs[i]);
            mPanelController.addComponent(mColorsButton, fView);
            listColors.addView(fView, pos);
        }

        mPanelController.addView(findViewById(R.id.applyEffect));
        mPanelController.addView(findViewById(R.id.pickCurvesChannel));
        mPanelController.addView(findViewById(R.id.aspect));
        findViewById(R.id.resetOperationsButton).setOnClickListener(
                createOnClickResetOperationsButton());

        ListView operationsList = (ListView) findViewById(R.id.operationsList);
        operationsList.setAdapter(mImageShow.getHistory());
        operationsList.setOnItemClickListener(this);
        ListView imageStateList = (ListView) findViewById(R.id.imageStateList);
        imageStateList.setAdapter(mImageShow.getImageStateAdapter());
        mImageLoader.setAdapter(mImageShow.getHistory());

        fillListImages(listFilters);
        fillListBorders(listBorders);
        listFilters.setVisibility(View.GONE);//add by biao.luo

        SeekBar seekBar = (SeekBar) findViewById(R.id.filterSeekBar);
        seekBar.setMax(SEEK_BAR_MAX);

        mImageShow.setSeekBar(seekBar);
        mImageZoom.setSeekBar(seekBar);
        mImageTinyPlanet.setSeekBar(seekBar);
        mPanelController.setRowPanel(findViewById(R.id.secondRowPanel));
        mPanelController.setUtilityPanel(this, findViewById(R.id.filterButtonsList),
                findViewById(R.id.applyEffect), findViewById(R.id.aspect),
                findViewById(R.id.pickCurvesChannel));
        mPanelController.setMasterImage(mImageShow);
        mPanelController.setCurrentPanel(mFxButton);
        Intent intent = getIntent();
        if (intent.getBooleanExtra(LAUNCH_FULLSCREEN, false)) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        }

        if (intent.getData() != null) {
            //yaogang.hao for pr 548601
            MyData.oldurl = intent.getData();
            startLoadBitmap(MyData.oldurl);
        } else {
            pickImage();
        }

        String action = intent.getAction();
        if (action.equalsIgnoreCase(CROP_ACTION)) {
            mPanelController.showComponent(findViewById(R.id.cropButton));
        } else if (action.equalsIgnoreCase(TINY_PLANET_ACTION)) {
            mPanelController.showComponent(findViewById(R.id.tinyplanetButton));
        }
        mImageShow.setProcBitmap(false);
        
		//add biao.luo begin
        mediaDatatObserver = new MediaDatatObserver(this, mHandler);
        registerObserver();
        //add biao.luo end
        
        //yaogang.hao for PR 544189
        MyData.isImagevalidated = true;
    }

    private void startLoadBitmap(Uri uri) {
        final View filters = findViewById(R.id.filtersPanel);
        final View loading = findViewById(R.id.loading);
        loading.setVisibility(View.VISIBLE);
        filters.setVisibility(View.INVISIBLE);
        View tinyPlanetView = findViewById(R.id.tinyplanetButton);
        if (tinyPlanetView != null) {
            tinyPlanetView.setVisibility(View.GONE);
        }
        mLoadBitmapTask = new LoadBitmapTask(tinyPlanetView);
        mLoadBitmapTask.execute(uri);
    }

    private class LoadBitmapTask extends AsyncTask<Uri, Void, Boolean> {
        View mTinyPlanetButton;
        int mBitmapSize;
        boolean mLoadResult;

        public LoadBitmapTask(View button) {
            mTinyPlanetButton = button;
            mBitmapSize = getScreenImageSize();
        }

        @Override
        protected Boolean doInBackground(Uri... params) {
            mLoadResult = mImageLoader.loadBitmap(params[0], mBitmapSize);
            if(false == mLoadResult) {
                return false;
            }
            publishProgress();
            // PR 500042 jipu.xiong@tcl.com begin
            if (!FilterShowActivity.this.isFinishing()) {
                return mImageLoader.queryLightCycle360();
            }
            return false;
            // PR 500042 jipu.xiong@tcl.com end

        }

        @Override
        protected void onProgressUpdate(Void... values) {
            super.onProgressUpdate(values);
            if (isCancelled()) return;
            final View filters = findViewById(R.id.filtersPanel);
            final View loading = findViewById(R.id.loading);
            loading.setVisibility(View.GONE);
            filters.setVisibility(View.VISIBLE);
        }

        @Override
        protected void onPostExecute(Boolean result) {
            if(false == mLoadResult) {
                cannotLoadImage();
            }
            if (isCancelled()) {
                return;
            }
            if (result) {
                mTinyPlanetButton.setVisibility(View.VISIBLE);
            }
            mLoadBitmapTask = null;
            super.onPostExecute(result);
        }

    }

    @Override
    protected void onDestroy() {
        // yaogang.hao for PR 548850
        unregisterReceiver(updatereReceiver);
        
        if (mLoadBitmapTask != null) {
            mLoadBitmapTask.cancel(false);
        }
        if (mHandler != null) {
            mHandler.removeCallbacksAndMessages(null);
        }
        unregisterObserver();// add by biao.luo
        MyData.clearCacheFiles();
        mBeautyControl.clearMemory();
        //yaogang.hao for PR 544189
        MyData.isImagevalidated = false;
        super.onDestroy();
    }

    private int translateMainPanel(View viewPanel) {
        int accessoryPanelWidth = viewPanel.getWidth();
        int mainViewWidth = findViewById(R.id.mainView).getWidth();
        int mainPanelWidth = mImageShow.getDisplayedImageBounds().width();
        if (mainPanelWidth == 0) {
            mainPanelWidth = mainViewWidth;
        }
        int leftOver = mainViewWidth - mainPanelWidth - accessoryPanelWidth;
        if (leftOver < 0) {
            return -accessoryPanelWidth;
        }
        return 0;
    }

    private int getScreenImageSize() {
        DisplayMetrics metrics = new DisplayMetrics();
        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        display.getMetrics(metrics);
        int msize = Math.min(size.x, size.y);
        return (133 * msize) / metrics.densityDpi;
    }

    private void showSavingProgress(String albumName) {
        ProgressDialog progress;
        if (mSavingProgressDialog != null) {
            progress = mSavingProgressDialog.get();
            if (progress != null) {
                progress.show();
                return;
            }
        }
        // TODO: Allow cancellation of the saving process
        String progressText;
        if (albumName == null) {
            progressText = getString(R.string.saving_image);
        } else {
            progressText = getString(R.string.filtershow_saving_image, albumName);
        }
        progress = ProgressDialog.show(this, "", progressText, true, false);
        mSavingProgressDialog = new WeakReference<ProgressDialog>(progress);
    }

    private void hideSavingProgress() {
        if (mSavingProgressDialog != null) {
            ProgressDialog progress = mSavingProgressDialog.get();
            if (progress != null)
                progress.dismiss();
        }
    }

    public void completeSaveImage(Uri saveUri) {
        if (mSharingImage && mSharedOutputFile != null) {
            // Image saved, we unblock the content provider
            Uri uri = Uri.withAppendedPath(SharedImageProvider.CONTENT_URI,
                    Uri.encode(mSharedOutputFile.getAbsolutePath()));
            ContentValues values = new ContentValues();
            values.put(SharedImageProvider.PREPARE, false);
            getContentResolver().insert(uri, values);
        }
        setResult(RESULT_OK, new Intent().setData(saveUri));
        hideSavingProgress();
        finish();
    }

    @Override
    public boolean onShareTargetSelected(ShareActionProvider arg0, Intent arg1) {
        // First, let's tell the SharedImageProvider that it will need to wait
        // for the image
        Uri uri = Uri.withAppendedPath(SharedImageProvider.CONTENT_URI,
                Uri.encode(mSharedOutputFile.getAbsolutePath()));
        ContentValues values = new ContentValues();
        values.put(SharedImageProvider.PREPARE, true);
        getContentResolver().insert(uri, values);
        mSharingImage = true;

        // Process and save the image in the background.
        showSavingProgress(null);
        mImageShow.saveImage(this, mSharedOutputFile);
        return true;
    }

    private Intent getDefaultShareIntent() {
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        intent.setType(SharedImageProvider.MIME_TYPE);
        mSharedOutputFile = SaveCopyTask.getNewFile(this, mImageLoader.getUri());
        Uri uri = Uri.withAppendedPath(SharedImageProvider.CONTENT_URI,
                Uri.encode(mSharedOutputFile.getAbsolutePath()));
        intent.putExtra(Intent.EXTRA_STREAM, FileUtils.getUri());//add biao.luo
        return intent;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.filtershow_activity_menu, menu);
        //add by biao.luo begin
        //cancel Show History menu
//        MenuItem showHistory = menu.findItem(R.id.operationsButton);
//        if (mShowingHistoryPanel) {
//            showHistory.setTitle(R.string.hide_history_panel);
//        } else {
//            showHistory.setTitle(R.string.show_history_panel);
//        }
      //add by biao.luo end
        MenuItem showState = menu.findItem(R.id.showImageStateButton);
        if (mShowingImageStatePanel) {
            showState.setTitle(R.string.hide_imagestate_panel);
        } else {
            showState.setTitle(R.string.show_imagestate_panel);
        }
		//update the Menu on the ActionBar.
//        MenuItem showDelete = menu.findItem(R.id.edit_action_delete);//add by biao.luo
        MenuItem showShare = menu.findItem(R.id.menu_share);
        showShare.setEnabled(true);//add by biao.luo
        MenuItem undo = menu.findItem(R.id.undoButton);
        MenuItem redo = menu.findItem(R.id.redoButton);
        MenuItem save = menu.findItem(R.id.saveButton);
        if(mshowingMenuItemDeleteAndShare){
//            showDelete.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);//add by biao.luo
            showShare.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
            undo.setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER);
            redo.setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER);
            save.setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER);
        }else{
            undo.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
            redo.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
            save.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
//            showDelete.setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER);//add by biao.luo
            showShare.setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER);
        }
        //delete this lines added by yaogang.hao for PR470988
//        mShareActionProvider = (ShareActionProvider) menu.findItem(R.id.menu_share)
//                .getActionProvider();
//       mShareActionProvider.setShareIntent(getDefaultShareIntent());
//        mShareActionProvider.setOnShareTargetSelectedListener(this);

        undoItem = menu.findItem(R.id.undoButton);
        redoItem = menu.findItem(R.id.redoButton);
        MenuItem saveItem = menu.findItem(R.id.saveButton);
//        MenuItem resetItem = menu.findItem(R.id.resetHistoryButton);
//        mImageShow.getHistory().setMenuItems(undoItem, redoItem, resetItem);
        return true;
    }

    @Override
    public void onPause() {
        super.onPause();

	//add by zhangcheng for PR498772 begin	
	mPauseListener.onPause();
	//add by zhangcheng for PR498772 end

        // PR 489413 jipu.xiong@tcl.com begin
//        if (!isClicked) {
//            onBackPressed();
//        }
        // PR 489413 jipu.xiong@tcl.com end

        //delete this lines added by yaogang.hao for PR470988
//        if (mShareActionProvider != null) {
//            mShareActionProvider.setOnShareTargetSelectedListener(null);
//        }
    }

    @Override
    public void onResume() {
        super.onResume();
        refreshPreviewImageViewIfNeed();
        isClicked = false;//add by biao.luo for pr472873
        mImageShow.setProcBitmap(true);
        //delete this lines added by yaogang.hao for PR470988
//        if (mShareActionProvider != null) {
//            mShareActionProvider.setOnShareTargetSelectedListener(this);
//        }
      //yaogang.hao for PR 544189
        if(!MyData.isImagevalidated)
        {
        	finish();
        }
        //yaogang.hao for pr 548601
        if(MyData.oldurl != getIntent().getData())
        {
            startLoadBitmap(MyData.oldurl); 
        }
        
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.undoButton: {
//                HistoryAdapter adapter = mImageShow.getHistory();
//                int position = adapter.undo();
//                mImageShow.onItemClick(position);
//                mImageShow.showToast("Undo");
//                invalidateViews();
//                mImageShow.setProcBitmap(false);
                //up
                mBeautyControl.getPreCacheImage();
                mImageShow.setProcBitmap(true);
                return true;
            }
            case R.id.redoButton: {
//                HistoryAdapter adapter = mImageShow.getHistory();
//                int position = adapter.redo();
//                mImageShow.onItemClick(position);
//                mImageShow.showToast("Redo");
//                invalidateViews();
//                mImageShow.setProcBitmap(true);
                //down
                mBeautyControl.getNextCacheImage();
                mImageShow.setProcBitmap(true);
                return true;
            }
            case R.id.saveButton:
                /*//PR651227-tao li-begin 001
                if (!Utils.hasAvailableSpace(Utils.getDefaultPath())) {
                    Utils.showToast(FilterShowActivity.this, R.string.not_enough_storage);
                    break;
                }
                //PR651227-tao li-end 001*/

               // PR932423 Gallery Force close while edit photo add by limin.zhuo at 20150303 begin
              if (!Utils.ishasAvailableSpaceFromPath(finalFilePath)) {
                 Utils.showToast(FilterShowActivity.this, R.string.not_enough_storage);
                     break;
               }
              // PR932423 Gallery Force close while edit photo add by limin.zhuo at 20150303 end
                doSave();
                return true;
//            case R.id.resetHistoryButton: {
//                resetHistory();
//                return true;
//            }
            case R.id.showImageStateButton: {
                toggleImageStatePanel();
                return true;
            }
            //add by biao.luo begin
            //cancel Show History menu
//            case R.id.operationsButton: {
//                toggleHistoryPanel();
//                return true;
//            }
            //add by biao.luo begin
            //cancel Delete menu
//            case R.id.edit_action_delete:{
//                setResult(RESULT_OK, new Intent().setData(mImageLoader.getUri()));
//                finish();
//                
//            }
          //add by biao.luo begin
            case android.R.id.home: {
                saveImage();
                return true;
            }
            case R.id.menu_share:
            {
                isSharing = true;
                doSave();
                return true;
            }
        }
        return false;
    }

    private void fillListImages(LinearLayout listFilters) {
        // TODO: use listview
        // TODO: load the filters straight from the filesystem

        ImageFilterFx[] fxArray = new ImageFilterFx[18];
        int p = 0;

        int[] drawid = {
                R.drawable.filtershow_fx_0005_punch,
                R.drawable.filtershow_fx_0000_vintage,
                R.drawable.filtershow_fx_0004_bw_contrast,
                R.drawable.filtershow_fx_0002_bleach,
                R.drawable.filtershow_fx_0001_instant,
                R.drawable.filtershow_fx_0007_washout,
                R.drawable.filtershow_fx_0003_blue_crush,
                R.drawable.filtershow_fx_0008_washout_color,
                R.drawable.filtershow_fx_0006_x_process
        };

        int[] fxNameid = {
                R.string.ffx_punch,
                R.string.ffx_vintage,
                R.string.ffx_bw_contrast,
                R.string.ffx_bleach,
                R.string.ffx_instant,
                R.string.ffx_washout,
                R.string.ffx_blue_crush,
                R.string.ffx_washout_color,
                R.string.ffx_x_process
        };

        ImagePreset preset = new ImagePreset(getString(R.string.history_original)); // empty
        preset.setImageLoader(mImageLoader);
        mNullFxFilter = new ImageSmallFilter(this);

        mNullFxFilter.setSelected(true);
        mCurrentImageSmallFilter = mNullFxFilter;

        mNullFxFilter.setImageFilter(new ImageFilterFx(null, getString(R.string.none)));

        mNullFxFilter.setController(this);
        mNullFxFilter.setImageLoader(mImageLoader);
        listFilters.addView(mNullFxFilter);
        ImageSmallFilter previousFilter = mNullFxFilter;

        BitmapFactory.Options o = new BitmapFactory.Options();
        o.inScaled = false;

        for (int i = 0; i < drawid.length; i++) {
            Bitmap b = BitmapFactory.decodeResource(getResources(), drawid[i], o);
            fxArray[p++] = new ImageFilterFx(b, getString(fxNameid[i]));
        }
        ImageSmallFilter filter;
        for (int i = 0; i < p; i++) {
            filter = new ImageSmallFilter(this);
            filter.setImageFilter(fxArray[i]);
            filter.setController(this);
            filter.setNulfilter(mNullFxFilter);
            filter.setImageLoader(mImageLoader);
            listFilters.addView(filter);
            previousFilter = filter;
        }

        // Default preset (original)
        mImageShow.setImagePreset(preset);
    }

    public Drawable getBitmapDrawable (int id) {
        Log.d(LOGTAG, "[getBitmapDrawable]id========"+id);
        Bitmap mBitmap= getBitmap(id);
        BitmapDrawable mDrawable = null;
        if (mBitmap != null) {
            mDrawable = new BitmapDrawable(getResources(),mBitmap);
        }
        return mDrawable;
    }

    /// M: decode bitmap and avoid out of memory error.
    public Bitmap getBitmap (int id) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inSampleSize = 1;
        Log.w(LOGTAG,"getBitmap:try for sample size::" + options.inSampleSize);
        Bitmap bitmap = null;
        try {
            bitmap = BitmapFactory.decodeResource(getResources(), id, options);
        } catch (OutOfMemoryError e) {
            /// M: As there is a chance no enough dvm memory for decoded Bitmap,
            //Skia will return a null Bitmap. In this case, we have to
            //downscale the decoded Bitmap by increase the options.inSampleSize
            final int maxTryNum = 8;
            for (int i=0; i < maxTryNum; i++) {
                //we increase inSampleSize to expect a smaller Bitamp
                options.inSampleSize *= 2;
                Log.w(LOGTAG,"getBitmap:try for sample size::" + options.inSampleSize);
                try {
                    bitmap = BitmapFactory.decodeResource(getResources(), id, options);
                } catch (OutOfMemoryError e1) {
                    Log.w(LOGTAG," getBitmap :out of memory when decoding:"+e1);
                    bitmap = null;
                }
                if (bitmap != null) break;
            }
        } finally {
            return bitmap;
        }
    }

    private void fillListBorders(LinearLayout listBorders) {
        // TODO: use listview
        // TODO: load the borders straight from the filesystem
        int p = 0;
        ImageFilter[] borders = new ImageFilter[7];
        borders[p++] = new ImageFilterBorder(null);

        //Drawable npd1 = getResources().getDrawable(R.drawable.filtershow_border_4x5);
        /// M: avoid out of memory error.
        Drawable npd1 = getBitmapDrawable(R.drawable.filtershow_border_4x5);
        borders[p++] = new ImageFilterBorder(npd1);
        //  Drawable npd2 = getResources().getDrawable(R.drawable.filtershow_border_brush);
        /// M: avoid out of memory error.
        Drawable npd2 = getBitmapDrawable(R.drawable.filtershow_border_brush);
        borders[p++] = new ImageFilterBorder(npd2);
        borders[p++] = new ImageFilterParametricBorder(Color.BLACK, mImageBorderSize, 0);
        borders[p++] = new ImageFilterParametricBorder(Color.BLACK, mImageBorderSize,
                mImageBorderSize);
        borders[p++] = new ImageFilterParametricBorder(Color.WHITE, mImageBorderSize, 0);
        borders[p++] = new ImageFilterParametricBorder(Color.WHITE, mImageBorderSize,
                mImageBorderSize);

        ImageSmallFilter previousFilter = null;
        for (int i = 0; i < p; i++) {
            ImageSmallBorder filter = new ImageSmallBorder(this);
            if (i == 0) { // save the first to reset it
                mNullBorderFilter = filter;
            } else {
                filter.setNulfilter(mNullBorderFilter);
            }
            borders[i].setName(getString(R.string.borders));
            filter.setImageFilter(borders[i]);
            filter.setController(this);
            filter.setBorder(true);
            filter.setImageLoader(mImageLoader);
            filter.setShowTitle(false);
            listBorders.addView(filter);
            previousFilter = filter;
        }
    }

    // //////////////////////////////////////////////////////////////////////////////
    // Some utility functions
    // TODO: finish the cleanup.

    public void showOriginalViews(boolean value) {
        for (ImageShow views : mImageViews) {
            views.showOriginal(value);
        }
    }

    public void invalidateViews() {
        for (ImageShow views : mImageViews) {
            views.invalidate();
            views.updateImage();
        }
    }

    public void hideListViews() {
        for (View view : mListViews) {
            view.setVisibility(View.GONE);
        }
    }

    public void hideImageViews() {
        mImageShow.setShowControls(false); // reset
        for (View view : mImageViews) {
            view.setVisibility(View.GONE);
        }
    }

    public void unselectBottomPanelButtons() {
        for (ImageButton button : mBottomPanelButtons) {
            button.setSelected(false);
        }
    }

    public void unselectPanelButtons(Vector<ImageButton> buttons) {
        for (ImageButton button : buttons) {
            button.setSelected(false);
        }
    }

    // //////////////////////////////////////////////////////////////////////////////
    // imageState panel...

    public boolean isShowingHistoryPanel() {
        return mShowingHistoryPanel;
    }

    private void toggleImageStatePanel() {
        final View view = findViewById(R.id.mainPanel);
        final View viewList = findViewById(R.id.imageStatePanel);

        if (mShowingHistoryPanel) {
            findViewById(R.id.historyPanel).setVisibility(View.INVISIBLE);
            mShowingHistoryPanel = false;
        }

        int translate = translateMainPanel(viewList);
        if (!mShowingImageStatePanel) {
            mShowingImageStatePanel = true;
            view.animate().setDuration(200).x(translate)
                    .withLayer().withEndAction(new Runnable() {
                        @Override
                        public void run() {
                            viewList.setAlpha(0);
                            viewList.setVisibility(View.VISIBLE);
                            viewList.animate().setDuration(100)
                                    .alpha(1.0f).start();
                        }
                    }).start();
        } else {
            mShowingImageStatePanel = false;
            viewList.setVisibility(View.INVISIBLE);
            view.animate().setDuration(200).x(0).withLayer()
                    .start();
        }
        invalidateOptionsMenu();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig)
    {
        super.onConfigurationChanged(newConfig);
        if (mShowingHistoryPanel) {
            toggleHistoryPanel();
        }
    }

    // //////////////////////////////////////////////////////////////////////////////
    // history panel...

    public void toggleHistoryPanel() {
        final View view = findViewById(R.id.mainPanel);
        final View viewList = findViewById(R.id.historyPanel);

        if (mShowingImageStatePanel) {
            findViewById(R.id.imageStatePanel).setVisibility(View.INVISIBLE);
            mShowingImageStatePanel = false;
        }

        int translate = translateMainPanel(viewList);
        if (!mShowingHistoryPanel) {
            mShowingHistoryPanel = true;
            view.animate().setDuration(200).x(translate)
                    .withLayer().withEndAction(new Runnable() {
                        @Override
                        public void run() {
                            viewList.setAlpha(0);
                            viewList.setVisibility(View.VISIBLE);
                            viewList.animate().setDuration(100)
                                    .alpha(1.0f).start();
                        }
                    }).start();
        } else {
            mShowingHistoryPanel = false;
            viewList.setVisibility(View.INVISIBLE);
            view.animate().setDuration(200).x(0).withLayer()
                    .start();
        }
        invalidateOptionsMenu();
    }

    private void resetHistory() {
//        mNullFxFilter.onClick(mNullFxFilter);
//        mNullBorderFilter.onClick(mNullBorderFilter);
//
//        HistoryAdapter adapter = mImageShow.getHistory();
//        adapter.reset();
//        ImagePreset original = new ImagePreset(adapter.getItem(0));
//        mImageShow.setImagePreset(original);
//        mPanelController.resetParameters();
//        invalidateViews();
        mImageShow.setProcBitmap(false);
        MyData.clearCacheFiles();
        MyData.mCurr_index = -1;
        MyData.cacheBitmaps.clear();
        BeautyControl.cacheRefCount = -1;
        mBeautyControl.clearMemory();
        initDevData();
        new MtprogressDialog(this,true,this.getString(R.string.reseting)) {
            @Override
            public void process() {
                try {
                    mBeautyControl.initWithImagePath(MyData.strPicPath, MyData.nScreenW, MyData.nScreenH,
                            MyData.nOutPutWidth, MyData.nOutPutHeight);
                } catch (Exception e) {
                }
            }
        }.show();
        MyData.setAPKPathToNDK(getApplicationContext());
        try {
            MyData.NDKCheckColorARGB8888Index( this.getAssets().open("ndk_check_color.bmp") );
        } catch (IOException e) {
        }
        MyData.getBeautyControl().pushImage();
    }

    // reset button in the history panel.
    private OnClickListener createOnClickResetOperationsButton() {
        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                resetHistory();
            }
        };
    }

    @Override
    public void onBackPressed() {
        if (mPanelController.onBackPressed()) {
            saveImage();
        }
    }

    public void cannotLoadImage() {
        CharSequence text = getString(R.string.cannot_load_image);
        Toast toast = Toast.makeText(this, text, Toast.LENGTH_SHORT);
        toast.show();
        finish();
    }

    // //////////////////////////////////////////////////////////////////////////////

    public float getPixelsFromDip(float value) {
        Resources r = getResources();
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, value,
                r.getDisplayMetrics());
    }

    public void useImagePreset(ImageSmallFilter imageSmallFilter, ImagePreset preset) {
        if (preset == null) {
            return;
        }

        if (mCurrentImageSmallFilter != null) {
            mCurrentImageSmallFilter.setSelected(false);
        }
        mCurrentImageSmallFilter = imageSmallFilter;
        mCurrentImageSmallFilter.setSelected(true);

        ImagePreset copy = new ImagePreset(preset);
        mImageShow.setImagePreset(copy);
        if (preset.isFx()) {
            // if it's an FX we rest the curve adjustment too
            mImageCurves.resetCurve();
        }
        invalidateViews();
    }

    public void useImageFilter(ImageSmallFilter imageSmallFilter, ImageFilter imageFilter,
            boolean setBorder) {
        if (imageFilter == null) {
            return;
        }

        if (mCurrentImageSmallFilter != null) {
            mCurrentImageSmallFilter.setSelected(false);
        }
        mCurrentImageSmallFilter = imageSmallFilter;
        mCurrentImageSmallFilter.setSelected(true);

        ImagePreset oldPreset = mImageShow.getImagePreset();
        ImagePreset copy = new ImagePreset(oldPreset);
        // TODO: use a numerical constant instead.

        copy.add(imageFilter);

        mImageShow.setImagePreset(copy);
        invalidateViews();
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position,
            long id) {
        mImageShow.onItemClick(position);
        invalidateViews();
    }

    public void pickImage() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, getString(R.string.select_image)),
                SELECT_PICTURE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.v(LOGTAG, "onActivityResult");
        refreshPreviewImageViewIfNeed();
        if (resultCode == RESULT_OK) {
            if (requestCode == SELECT_PICTURE) {
                Uri selectedImageUri = data.getData();
                startLoadBitmap(selectedImageUri);
            }
        }
    }

    public void saveImage() {
        if (mImageShow.hasModifications()) {
            // Get the name of the album, to which the image will be saved
            File saveDir = SaveCopyTask.getFinalSaveDirectory(this, mImageLoader.getUri());
            int bucketId = GalleryUtils.getBucketId(saveDir.getPath());
            String albumName = LocalAlbum.getLocalizedName(getResources(), bucketId, null);
            showSavingProgress(albumName);
            mImageShow.saveImage(this, null);
        } else {
            finish();
        }
    }
    //add by biao.luo begin
    private void loadImageWithFilePath(String filepath) {

        //PR669201-taoli-begin 001
        if (filepath == null) {
            //throw new NullPointerException("The path starts pictures can not be empty");
            Log.e(LOGTAG, "The path starts pictures can not be empty");
            MyData.clearCacheFiles();
            MyData.mCurr_index = -1;
            MyData.cacheBitmaps.clear();
            BeautyControl.cacheRefCount = -1;
            mBeautyControl.clearMemory();
            finish();
            return;
        }
        //PR669201-taoli-end 001

        new DialogWait(this, getString(R.string.tip_please_wait), getString(R.string.tip_please_wait_content)) {
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
                //PR936992 Undo and Redo icon are useless,while SD card is full add by limin.zhuo at 2015.03.06 begin
                if (!Utils.updateCacheDirEditPicture()) {
                 Utils.showToast(FilterShowActivity.this, R.string.storage_full_tag);
                }
                //PR936992 Undo and Redo icon are useless,while SD card is full add by limin.zhuo at 2015.03.06 end
                MyData.getBeautyControl().pushImage();//add by biao.luo for pr469042
                refreshPreviewImageViewIfNeed();
                // yaogang.hao for PR 548850
                sendBroadcast(new Intent(UPDATE_ACTION));
                break;
            case MSG_MEDIA_SCANNER:
            {
                //add by biao.luo 
                startActivity(getDefaultShareIntent());
                isSharing = false;
                break;
            }
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
            mImageShow.updateFilteredImage(m_pViewBitmap);
            mImageShow.invalidate();
            m_pViewBitmap.recycle();//add by biao.luo
            m_pViewBitmap = null;
            return true;
        } else {
            return false;
        }
    }
    
    protected void initDevData() {
        final Intent intent = getIntent();
        finalFilePath = BitmapUtil.getRealPathFromUri(FilterShowActivity.this, intent.getData());
        MyData.strPicPath = finalFilePath;
        if (0 == MyData.nScreenW || 0 == MyData.nScreenH || 0 == MyData.nDensity) {
            Display display = getWindowManager().getDefaultDisplay();
            DisplayMetrics dm = new DisplayMetrics();
            display.getMetrics(dm);
            MyData.nScreenW = dm.widthPixels;
            MyData.nScreenH = dm.heightPixels;

            //PR944102 modify The image display incomplete when select Effect by fengke at 2015.03.11 start
            // if init in landscape it will be lead to some display error
            if (MyData.nScreenW > MyData.nScreenH) {
                int temp = 0;
                temp = MyData.nScreenW - 0;//0 is navigationbar height
                MyData.nScreenW =  MyData.nScreenH + 0;
                MyData.nScreenH =  temp;
            }
            //PR944102 modify The image display incomplete when select Effect by fengke at 2015.03.11 end

            MyData.nDensity = dm.density;
            MyData.nBmpDstW = MyData.nScreenW;
            MyData.nBmpDstH = MyData.nScreenH - 100;
            if (MyData.nBmpDstW < MyData.nOutPutWidth && MyData.nBmpDstH < MyData.nOutPutHeight) {
                MyData.nBmpDstW = MyData.nOutPutWidth;
                MyData.nBmpDstH = MyData.nOutPutHeight;
            }
        }
    }
    @Override
    public void onClick(View v) {
        //add by biao.luo begin for pr472873
        if(isClicked) return;
        isClicked = true;
        //add by biao.luo end
        MyData.removeCahcheFiles();
        Intent intent = new Intent();
        MTActivity.setMTPauseLisener(FilterShowActivity.this); //added by jipu.xiong@tcl.com
        switch (v.getId()) {
            case R.id.btn_edit:
                intent.setClass(FilterShowActivity.this, IMGEditActivity.class);
                startActivity(intent);
                break;
            case R.id.btn_menu_white:
                intent.setClass(FilterShowActivity.this, BeautyMopiActivity.class);
                startActivity(intent);
                break;
            case R.id.btn_color:
                intent.setClass(FilterShowActivity.this, BeautyColorActivity.class);
                startActivity(intent);
                break;
            case R.id.btn_effect:
                intent.setClass(FilterShowActivity.this, BeautyEffectActivity.class);
                startActivity(intent);
                break;

            case R.id.btn_frame:
                intent.setClass(FilterShowActivity.this, BeautyFrameActivity.class);
                startActivity(intent);
                break;
            case R.id.btn_mosaic:
                intent.setClass(FilterShowActivity.this, BeautyMosaicActivity.class);
                startActivity(intent);
                break;

            case R.id.btn_words:
                intent.setClass(FilterShowActivity.this, BeautyWordActivity.class);
                startActivity(intent);
                break;

            case R.id.btn_weak:
                intent.setClass(FilterShowActivity.this, BeautyBlurActivity.class);
                startActivity(intent);
                break;

            default:
                break;
        }
    }
    
    private void doSave() {
        new MtprogressDialog(this) {
            @Override
            public void process() {
                try {
                    if (mBeautyControl.saveImage(FileUtils.generateToSaveFileName(finalFilePath)) == 1)
                        FileUtils.scanDirAsync(FilterShowActivity.this);
                        // biao.luo
//                        MyData.clearCacheFiles();
                } catch (Exception e) {
                }
            }
        }.show();
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

    public void registerObserver()
    {
        Uri uri = Uri.parse("content://media/external/images/media");
        getContentResolver().registerContentObserver(uri, true, mediaDatatObserver);
    }
    public void unregisterObserver()
    {
        getContentResolver().unregisterContentObserver(mediaDatatObserver);
    }
    private class MediaDatatObserver extends   ContentObserver
    {
        private Context mContext;
        private Handler mediahandler ; 

        public MediaDatatObserver(Context context,Handler handler) {
            super(handler);  
            mContext = context ;  
            mediahandler = handler ;  
        }
        @Override
        public void onChange(boolean selfChange,Uri uri) {
            if(uri.toString().contains("content://media/external/images/media") && isSharing)
            {
                mediahandler.sendEmptyMessage(MSG_MEDIA_SCANNER); 
            }
        }
    }
  //yaogang.hao for PR535931 begin
    /**
     * launch the new activity from another task
     * Eg:
     * one from gallery task
     * another from camera task
     */
    @Override
    protected void onNewIntent(Intent intent) {
        refreshScreenShow(intent);
    }
    public void refreshScreenShow(Intent intent)
    {
        //1:clear some variables
        MyData.clearCacheFiles();
        MyData.mCurr_index = -1;
        MyData.cacheBitmaps.clear();
        BeautyControl.cacheRefCount = -1;
        mBeautyControl.clearMemory();
       
        //2:initialize new data with intent attachment
        finalFilePath = BitmapUtil.getRealPathFromUri(FilterShowActivity.this, intent.getData());
        MyData.strPicPath = finalFilePath;
       
        if (0 == MyData.nScreenW || 0 == MyData.nScreenH || 0 == MyData.nDensity) {
            Display display = getWindowManager().getDefaultDisplay();
            DisplayMetrics dm = new DisplayMetrics();
            display.getMetrics(dm);
            MyData.nScreenW = dm.widthPixels;
            MyData.nScreenH = dm.heightPixels;

            //PR944102 modify The image display incomplete when select Effect by fengke at 2015.03.11 start
            // if init in landscape it will be lead to some display error
            if (MyData.nScreenW > MyData.nScreenH) {
                int temp = 0;
                temp = MyData.nScreenW - 0;//0 is navigationbar height
                MyData.nScreenW =  MyData.nScreenH + 0;
                MyData.nScreenH =  temp;
            }
            //PR944102 modify The image display incomplete when select Effect by fengke at 2015.03.11 end

            MyData.nDensity = dm.density;
            MyData.nBmpDstW = MyData.nScreenW;
            MyData.nBmpDstH = MyData.nScreenH - 100;
            if (MyData.nBmpDstW < MyData.nOutPutWidth && MyData.nBmpDstH < MyData.nOutPutHeight) {
                MyData.nBmpDstW = MyData.nOutPutWidth;
                MyData.nBmpDstH = MyData.nOutPutHeight;
            }
        }
        loadImageWithFilePath(finalFilePath);
       
    }
    //yaogang.hao for PR535931 end
    
    // PR 489413 jipu.xiong@tcl.com begin
    @Override
    public void doOnPause() {
//        if (isClicked) {
//            onBackPressed();
//        }
    }
    // PR 489413 jipu.xiong@tcl.com end
    
}
    //add by biao.luo end
