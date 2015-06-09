/*
 * Copyright (C) 2010 The Android Open Source Project
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

package com.jrdcom.android.gallery3d.app;

import android.annotation.TargetApi;
import android.app.ActionBar;
import android.app.Activity;
import android.app.ProgressDialog;
import android.app.WallpaperManager;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.BitmapRegionDecoder;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.os.ServiceManager;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.provider.MediaStore.Images;
import android.util.FloatMath;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import com.android.jrdcamera.Util;
import com.jrdcom.mediatek.gallery3d.util.MtkUtils;
import com.jrdcom.android.gallery3d.R;
import com.jrdcom.android.gallery3d.common.ApiHelper;
import com.jrdcom.android.gallery3d.common.BitmapUtils;
import com.jrdcom.android.gallery3d.common.Utils;
import com.jrdcom.android.gallery3d.data.DataManager;
import com.jrdcom.android.gallery3d.data.LocalImage;
import com.jrdcom.android.gallery3d.data.MediaItem;
import com.jrdcom.android.gallery3d.data.MediaObject;
import com.jrdcom.android.gallery3d.data.Path;
import com.jrdcom.android.gallery3d.exif.ExifData;
import com.jrdcom.android.gallery3d.exif.ExifOutputStream;
import com.jrdcom.android.gallery3d.exif.ExifReader;
import com.jrdcom.android.gallery3d.exif.ExifTag;
import com.jrdcom.android.gallery3d.picasasource.PicasaSource;
import com.jrdcom.android.gallery3d.ui.BitmapScreenNail;
import com.jrdcom.android.gallery3d.ui.BitmapTileProvider;
import com.jrdcom.android.gallery3d.ui.CropView;
import com.jrdcom.android.gallery3d.ui.GLRoot;
import com.jrdcom.android.gallery3d.ui.SynchronizedHandler;
import com.jrdcom.android.gallery3d.ui.TileImageViewAdapter;
import com.jrdcom.android.gallery3d.util.BucketNames;
import com.jrdcom.android.gallery3d.util.Future;
import com.jrdcom.android.gallery3d.util.FutureListener;
import com.jrdcom.android.gallery3d.util.GalleryUtils;
import com.jrdcom.android.gallery3d.util.InterruptableOutputStream;
import com.jrdcom.android.gallery3d.util.ThreadPool.CancelListener;
import com.jrdcom.android.gallery3d.util.ThreadPool.Job;
import com.jrdcom.android.gallery3d.util.ThreadPool.JobContext;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteOrder;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.Messenger;
import android.os.RemoteException;
import android.content.Context;
import android.content.ComponentName;

import com.jrdcom.mediatek.gallery3d.drm.DrmHelper;
import com.jrdcom.mediatek.gallery3d.stereo.StereoHelper;
import com.jrdcom.mediatek.gallery3d.util.MediatekFeature;
import com.jrdcom.mediatek.gallery3d.util.MediatekFeature.DataBundle;
import com.jrdcom.mediatek.gallery3d.util.MediatekFeature.Params;
import com.jrdcom.mediatek.gallery3d.util.MtkLog;
import com.mediatek.common.featureoption.FeatureOption;

import android.content.ContentResolver;
import android.content.IntentFilter;
import android.os.HandlerThread;
import android.content.BroadcastReceiver;
import android.database.Cursor;
import android.drm.DrmManagerClient;
import com.mediatek.drm.OmaDrmClient;
import android.os.storage.IMountService;
import android.os.storage.StorageManager;

import android.provider.Settings;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.BaseAdapter;
import android.widget.TextView;
import java.util.List;
import java.util.ArrayList;
import android.view.LayoutInflater;
import android.preference.PreferenceManager;
import android.content.SharedPreferences;
import android.widget.Button;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.view.MotionEvent;
import android.widget.ImageButton;
import android.app.WallpaperInfo;
import android.content.res.Configuration;
import android.widget.RelativeLayout;
import android.widget.LinearLayout;

/**
 * The activity can crop specific region of interest from an image.
 */
public class CropImage extends AbstractGalleryActivity {
    private static final String TAG = "CropImage";
    public static final String ACTION_CROP = "com.android.camera.action.CROP";

    private static final int MAX_PIXEL_COUNT = 5 * 1000000; // 5M pixels
    private static final int MAX_FILE_INDEX = 1000;
    private static final int TILE_SIZE = 512;
    private static final int BACKUP_PIXEL_COUNT = 480000; // around 800x600

    private static final int MSG_LARGE_BITMAP = 1;
    private static final int MSG_BITMAP = 2;
    private static final int MSG_SAVE_COMPLETE = 3;
    private static final int MSG_SHOW_SAVE_ERROR = 4;
    private static final int MSG_CANCEL_DIALOG = 5;
    private static final int MSG_SECOND_BITMAP = 6;

    //message that communicate with 3D wallpaper service
    private final static int MSG_SET_3D_WALLPAPER = 101;
    private final static int MSG_3D_WALLPAPER_RESULT_OK = 102;
    private final static int MSG_3D_WALLPAPER_RESULT_ERROR = 103;
    
    // check storage state message
    private static final int MSG_CHECK_STORAGE = 104;

    private static final int MAX_BACKUP_IMAGE_SIZE = 320;
    private static final int DEFAULT_COMPRESS_QUALITY = 90;
    private static final String TIME_STAMP_NAME = "'IMG'_yyyyMMdd_HHmmss";

    public static final String KEY_RETURN_DATA = "return-data";
    public static final String KEY_CROPPED_RECT = "cropped-rect";
    public static final String KEY_ASPECT_X = "aspectX";
    public static final String KEY_ASPECT_Y = "aspectY";
    public static final String KEY_SPOTLIGHT_X = "spotlightX";
    public static final String KEY_SPOTLIGHT_Y = "spotlightY";
    public static final String KEY_OUTPUT_X = "outputX";
    public static final String KEY_OUTPUT_Y = "outputY";
    public static final String KEY_SCALE = "scale";
    public static final String KEY_DATA = "data";
    public static final String KEY_SCALE_UP_IF_NEEDED = "scaleUpIfNeeded";
    public static final String KEY_OUTPUT_FORMAT = "outputFormat";
    public static final String KEY_SET_AS_WALLPAPER = "set-as-wallpaper";
    public static final String KEY_NO_FACE_DETECTION = "noFaceDetection";
    public static final String KEY_SHOW_WHEN_LOCKED = "showWhenLocked";

    private static final String KEY_STATE = "state";

    private static final int STATE_INIT = 0;
    private static final int STATE_LOADED = 1;
    private static final int STATE_SAVING = 2;
    private static final int STATE_SAVE_DONE = 3;

    public static final File DOWNLOAD_BUCKET = new File(
            Environment.getExternalStorageDirectory(), BucketNames.DOWNLOAD);

    public static final String CROP_ACTION = "com.android.camera.action.CROP";

    private static final boolean IS_DRM_SUPPORTED = 
                                          MediatekFeature.isDrmSupported();

    public static final String SET_3D_WALLPAPER =
                            "com.mediatek.stereo3dwallpaper.SET_WALLPAPER";
    private static final boolean IS_STEREO_DISPLAY_SUPPORTED = 
                                          MediatekFeature.isStereoDisplaySupported();
    private boolean mSetStereoWallpaper = false;
    private Messenger mService = null;
    private boolean mIsBounded;
    private Intent mStereoWallpaperIntent;
    private Uri mStereoWallpaperUri = null;
    private Future<Bitmap> mLoadSecondBitmapTask;
    private boolean mDecodedSecondImage = false;

    private int mState = STATE_INIT;

    private CropView mCropView;

    private boolean mDoFaceDetection = true;

    private Handler mMainHandler;

    // We keep the following members so that we can free them

    // mBitmap is the unrotated bitmap we pass in to mCropView for detect faces.
    // mCropView is responsible for rotating it to the way that it is viewed by users.
    private Bitmap mBitmap;
    private BitmapTileProvider mBitmapTileProvider;
    private BitmapRegionDecoder mRegionDecoder;
    private Bitmap mBitmapInIntent;
    private boolean mUseRegionDecoder = false;
    private BitmapScreenNail mBitmapScreenNail;

    private ProgressDialog mProgressDialog;
    private Future<BitmapRegionDecoder> mLoadTask;
    private Future<Bitmap> mLoadBitmapTask;
    private Future<Intent> mSaveTask;

    private MediaItem mMediaItem;

    //cached object to control it in this activity.
    private TileImageViewAdapter mTIVA;
    
 // CR535794-lilei-begin
   	int mWallpaperWidth;
   	int mWallpaperHeight;
   	private AlertDialog mAlertDialog;
   	private ImageButton mWideScreenModelButton;
   	private ImageButton mSingleScreenModelButton;
    //FR 576703 add by xiangchen begin
    private  String SDPATH = MtkUtils.getInternalStoragePath() + "/";//PR627495-tao li -001
    //FR 576703 add by xiangchen end

    private final String SDDIR = ".lockscreen_wallpaper/"; //PR525032-xiaowei.xu modify
    private final String SDFILE = "keyguard_wallpaper";//modify by jianxin.wei for PR803314
    public static final String LAUNCHER_WALLPAPER_MULTIPLE_SCREEN_FLAG = "multi_screen";
    public static final String LOCKSCREEN_WALLPAPER_INDEPENDENT = "lockscreen_independant";
    private static final String KEY_IS_WIDE_MODE = "wide_mode";
    private static final float WALLPAPER_SCREENS_SPAN = 2f;
    private WallpaperManager mWallpaperManager;
    private SharedPreferences msharedPreferences;
    private Future<Intent> mSaveLockscreenTask;
    private boolean isClickBoth = false;
    private WallpaperInfo mWallpaperInfo;
    private boolean isDynamicWallpaper = false;
    public static final String DYNAMIC_TAT = "com.jrdcom.weatherwallpaper";

   	OnClickListener mWideBtOnClickListener = new View.OnClickListener() {

   		@Override
   		public void onClick(View view) {
   			// TODO Auto-generated method stub
   			onWideModeButtonClick();
   		}
   	};

   	OnClickListener mSingleBtOnClickListener = new View.OnClickListener() {

   		@Override
   		public void onClick(View view) {
   			// TODO Auto-generated method stub
   			onSingleModeButtonClick();
   		}
   	};

   	public void onSingleModeButtonClick() {
//   		CharSequence text = getResources().getText(R.string.single_set_toast);
//   		Toast toast = Toast.makeText(this, text, Toast.LENGTH_SHORT);
//   		toast.show();
   		mWideScreenModelButton.setBackgroundResource(R.drawable.wide_screen);
   		mSingleScreenModelButton.setBackgroundResource(R.drawable.portrait_screen_sel);
   		msharedPreferences.edit().putBoolean(KEY_IS_WIDE_MODE, false).commit();
   		// set single screen value
   		//PR539964-lilei-begin
   		//Settings.System.putInt(CropImage.this.getContentResolver(),
   		//		LAUNCHER_WALLPAPER_MULTIPLE_SCREEN_FLAG, 0);
//   		int a = Settings.System.getInt(CropImage.this.getContentResolver(),
//   				LAUNCHER_WALLPAPER_MULTIPLE_SCREEN_FLAG, 0);
    	//PR539964-lilei-end
   		mCropView.setModeAction();
   	}

   	public void onWideModeButtonClick() {
   		//CharSequence text = getResources().getText(R.string.wide_set_toast);
   		//Toast toast = Toast.makeText(this, text, Toast.LENGTH_SHORT);
   		//toast.show();
   		mSingleScreenModelButton.setBackgroundResource(R.drawable.portrait_screen);
   		mWideScreenModelButton.setBackgroundResource(R.drawable.wide_screen_sel);
   		msharedPreferences.edit().putBoolean(KEY_IS_WIDE_MODE, true).commit();
   	//PR539964-lilei-begin
   		// set wide screen value
   		//Settings.System.putInt(CropImage.this.getContentResolver(),
   		//		LAUNCHER_WALLPAPER_MULTIPLE_SCREEN_FLAG, 1);
   	//PR539964-lilei-end
   		mCropView.setModeAction();
   	}

   	protected void setWallpaperDimension() {
   		DisplayMetrics displayMetrics = new DisplayMetrics();
   		getWindowManager().getDefaultDisplay().getRealMetrics(displayMetrics);
   		boolean isChooseWideMode = msharedPreferences.getBoolean(
   				KEY_IS_WIDE_MODE, true);
   		int orientation = this.getResources().getConfiguration().orientation;
   		//PR533845-lilei-begin
   		if (!isChooseWideMode) {
   			if(orientation == Configuration.ORIENTATION_PORTRAIT){
   			mWallpaperWidth = displayMetrics.widthPixels;
   			mWallpaperHeight = displayMetrics.heightPixels;
   			}else{
   	  	    mWallpaperWidth = displayMetrics.heightPixels;
   	  		mWallpaperHeight = displayMetrics.widthPixels;
   			}
   			
   		} else {
   			if(orientation == Configuration.ORIENTATION_PORTRAIT){
   			mWallpaperWidth = (int) (displayMetrics.widthPixels * WALLPAPER_SCREENS_SPAN);
   			mWallpaperHeight = displayMetrics.heightPixels;
   			}else{
   	  			mWallpaperWidth = (int) (displayMetrics.heightPixels * WALLPAPER_SCREENS_SPAN);
   	  			mWallpaperHeight = displayMetrics.widthPixels;
   			}
   		}
   	//PR533845-lilei-end
   		new Thread("setWallpaperDimension") {
   			public void run() {
   				MtkLog.w(TAG, "fengke setWallpaperDimension ");//PR928956 can not set wallpaper add some log by fengke at 2015.02.10
   				mWallpaperManager.suggestDesiredDimensions(mWallpaperWidth,
   						mWallpaperHeight);
   			}
   		}.start();
   	}

   	public class MyListAdapter extends BaseAdapter {
   		private Context context = null;
   		private TextView mTextView = null;
   		private List<String> list = null;

   		public MyListAdapter(Context c, List<String> list) {
   			this.context = c;
   			this.list = list;
   		}

   		@Override
   		public int getCount() {
   			// TODO Auto-generated method stub
   			return list.size();
   		}

   		@Override
   		public Object getItem(int position) {
   			// TODO Auto-generated method stub
   			return list.get(position);
   		}

   		@Override
   		public long getItemId(int position) {
   			// TODO Auto-generated method stub
   			return position;
   		}

   		@Override
   		public View getView(int position, View convertView, ViewGroup parent) {
   			// TODO Auto-generated method stub
   			convertView = LayoutInflater.from(context).inflate(
   					R.layout.text_list_item, null);
   			mTextView = (TextView) convertView.findViewById(R.id.quick_text);
   			mTextView.setText(list.get(position));
   			boolean isChooseWideMode = msharedPreferences.getBoolean(
   					KEY_IS_WIDE_MODE, true);
   			if (!isDynamicWallpaper && isChooseWideMode && (position == 0 || position == 2)) {
   				mTextView.setEnabled(false);
   			}
   			
   			if(isDynamicWallpaper
   					&& (position == 0 || position == 1))
   			{
   				mTextView.setEnabled(false);
   			}
   			return convertView;
   		}
   	}
   //PR525087-lilei-begin
 	private void onSaveBothscreenClicked() {
 		onSaveClicked();
 	}
 	//PR525087-lilei-end
 	
   	private void onSavelockscreenClicked() {
   		RectF cropRect = mCropView.getCropRectangle();
   		if (cropRect == null)
   			return;
   		mState = STATE_SAVING;
   		//PR541658-lilei-begin
   		//if(!isClickBoth){
   		int messageId = R.string.set_lockscreen;
   		mProgressDialog = ProgressDialog.show(this, null, getString(messageId),
   				true, false);
   		//}
   	  //PR541658-lilei-end
   		mSaveLockscreenTask = getThreadPool().submit(
   				new SavelockscreenOutput(cropRect),
   				new FutureListener<Intent>() {
   					@Override
   					public void onFutureDone(Future<Intent> future) {
   						mSaveLockscreenTask = null;
   						if (STATE_SAVE_DONE != mState) {
   							// MtkLog.w(TAG,
   							// "save task: save state != STATE_SAVE_DONE, cancel and return...");
   							mMainHandler.sendEmptyMessage(MSG_SHOW_SAVE_ERROR);
   							return;
   						}
   						Intent intent = future.get();
   						if (intent != null) {
   							Log.v(TAG,
   									"SaveTask:onFutureDone:send complete message");
   							mMainHandler.sendMessage(mMainHandler
   									.obtainMessage(MSG_SAVE_COMPLETE, intent));
   						} else {
   							Log.v(TAG,
   									"SaveTask:onFutureDone:send error message");
   							mMainHandler.sendEmptyMessage(MSG_SHOW_SAVE_ERROR);
   						}
   					}
   				});

   	}

   	public void setWallPaperHomescreen() {
   		MtkLog.w(TAG, "fengke setWallPaperHomescreen ");//PR928956 can not set wallpaper add some log by fengke at 2015.02.10
   		//PR539964-lilei-begin
   		boolean isChooseWideMode = msharedPreferences.getBoolean(
   				KEY_IS_WIDE_MODE, true);
   		if(isChooseWideMode){
   	   		Settings.System.putInt(CropImage.this.getContentResolver(),
   	   				LAUNCHER_WALLPAPER_MULTIPLE_SCREEN_FLAG, 1);
   		}else{
   	   		Settings.System.putInt(CropImage.this.getContentResolver(),
   	   				LAUNCHER_WALLPAPER_MULTIPLE_SCREEN_FLAG, 0);
   		}
   	//PR539964-lilei-end
   		//PR533825-lilei-begin
   		setWallpaperDimension();
   		onSaveClicked();
   		if(isDynamicWallpaper){
   	 		Settings.System.putInt(CropImage.this.getContentResolver(),
   	 				LOCKSCREEN_WALLPAPER_INDEPENDENT, 0);
   		}
   		//setWallpaperDimension();
   	   //PR533825-lilei-end
   	}

   	public void setWallPaperLockscreen() {
   		onSavelockscreenClicked();
   		Intent intent = new Intent();
   		intent.setAction("com.android.jrdcom.lockscreen_wallpaper_changed");
   		CropImage.this.sendBroadcast(intent);
   		Settings.System.putInt(CropImage.this.getContentResolver(),
   				LOCKSCREEN_WALLPAPER_INDEPENDENT, 1);
   	}
   //PR525087-lilei-begin
   	public void setWallPaperBothscreen(){
   		onSaveBothscreenClicked();
   		Intent intent = new Intent();
   		intent.setAction("com.android.jrdcom.lockscreen_wallpaper_changed");
   		CropImage.this.sendBroadcast(intent);
   		Settings.System.putInt(CropImage.this.getContentResolver(),
   				LOCKSCREEN_WALLPAPER_INDEPENDENT, 0);
   	}
   //PR525087-lilei-end
   	
   	public void setWallPaperBoth() {	
   	   	//PR539964-lilei-begin
   		boolean isChooseWideMode = msharedPreferences.getBoolean(
   				KEY_IS_WIDE_MODE, true);
   		if(isChooseWideMode){
   	   		Settings.System.putInt(CropImage.this.getContentResolver(),
   	   				LAUNCHER_WALLPAPER_MULTIPLE_SCREEN_FLAG, 1);
   		}else{
   	   		Settings.System.putInt(CropImage.this.getContentResolver(),
   	   				LAUNCHER_WALLPAPER_MULTIPLE_SCREEN_FLAG, 0);
   		}
   	//PR539964-lilei-end
   	//PR533825-lilei-begin
   		setWallpaperDimension();
   		//PR525087-lilei-begin
   		int messageId = R.string.set_both;
   		mProgressDialog = ProgressDialog.show(this, null, getString(messageId),
   				true, false);
   		setWallPaperBothscreen();
   		//setWallpaperDimension();
   		isClickBoth = false;
   	//PR525087-lilei-end
   	//PR533825-lilei-end
   	}

   	public void storePicToSDcard(Bitmap bitmap) {
   		File f = null;
   		try {
   			//FR 576703 add by xiangchen begin
   			//PR-568284 added by xiaowei.xu begin
   		   //createSDPath();
   		    //PR-568284 added by xiaowei.xu end
   		    //FR 576703 add by xiangchen end
   			createSDDir(SDDIR);
   			f = createSDFile(SDDIR + "/" + SDFILE);//modify by jianxin.wei for PR803314
   		} catch (IOException e) {
   		}
   		FileOutputStream fOut = null;
   		try {
   			fOut = new FileOutputStream(f);
   		} catch (FileNotFoundException e) {
   			e.printStackTrace();
   		}
  		boolean isChooseWideMode = msharedPreferences
  					.getBoolean(KEY_IS_WIDE_MODE, true);
   		if(isDynamicWallpaper && isChooseWideMode){
  			int[] pixels = new int[bitmap.getWidth() * bitmap.getHeight() / 2];
  			bitmap.getPixels(pixels, 0, bitmap.getWidth()/2, bitmap.getWidth()/4, 0, bitmap.getWidth() / 2, bitmap.getHeight());
  			Bitmap temp = Bitmap.createBitmap(bitmap.getWidth() / 2, bitmap.getHeight(), bitmap.getConfig());
  			temp.setPixels(pixels, 0, temp.getWidth(), 0, 0, temp.getWidth(), temp.getHeight());
  			temp.compress(Bitmap.CompressFormat.JPEG, 100, fOut);
   		}else{
   			bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fOut);
   		}
   		try {
   			fOut.flush();
   		} catch (IOException e) {
   			e.printStackTrace();
   		}
   		try {
   			fOut.close();
   		} catch (IOException e) {
   			e.printStackTrace();
   		}
   	}

   	public File createSDFile(String fileName) throws IOException {
   		File file = new File(SDPATH + fileName);
   		file.createNewFile();
   		return file;
   	}

   	public File createSDDir(String dirName) {
   		File dir = new File(SDPATH + dirName);
   		if (!dir.exists()) {
   			dir.mkdir();
   		}
   		return dir;
   	}
   	//FR 576703 add by xiangchen begin
   	//PR-568284 added by xiaowei.xu begin
//    public void createSDPath(){  
//        if(isExSDCardExist()){
//        	SDPATH = "/storage/sdcard1/";
//        }else{
//        	SDPATH = "/storage/sdcard0/";
//        }
//    }
//    public static boolean isExSDCardExist() {
//        if (Environment.isExternalStorageEmulated() ||
//                (FeatureOption.MTK_2SDCARD_SWAP && !isExSdcardInserted())) {
//            return false ;
//        } else {
//            return true ;
//        }
//    }
//    private static  boolean isExSdcardInserted() {
//    	
//        IBinder service = ServiceManager.getService("mount");
//        Log.d(TAG, "Util:service is " + service);
//        if (service != null) {
//            IMountService mountService = IMountService.Stub.asInterface(service);
//            Log.d(TAG, "Util:mountService is " + mountService);
//            if (mountService == null) {
//                return false;
//            }
//            try {
//                return mountService.isSDExist();
//            } catch (Exception e) {
//                Log.d(TAG, "Util:RemoteException when isSDExist: " + e);
//                return false;
//            }
//        } else {
//            return false;
//        }
//    }
    //PR-568284 added by xiaowei.xu end
    //FR 576703 add by xiangchen end

   	private class SavelockscreenOutput implements Job<Intent> {
   		private final RectF mCropRect;

   		public SavelockscreenOutput(RectF cropRect) {
   			mCropRect = cropRect;
   		}

   		@Override
   		public Intent run(JobContext jc) {
   			MtkLog.d(TAG, "SaveOutput.run");
   			RectF cropRect = mCropRect;

   			Rect rect = new Rect(Math.round(cropRect.left),
   					Math.round(cropRect.top), Math.round(cropRect.right),
   					Math.round(cropRect.bottom));

   			Intent result = new Intent();
   			result.putExtra(KEY_CROPPED_RECT, rect);
   			Bitmap cropped = null;
   			if (jc.isCancelled())
   				return null;
   			if (cropped == null)
   				cropped = getCroppedImage(jc, rect);
   			
   			storePicToSDcard(cropped);
   			mState = STATE_SAVE_DONE;
   			return result;
   		}
   	}

   	// CR535794-lilei-end

    @Override
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        requestWindowFeature(Window.FEATURE_ACTION_BAR);
        requestWindowFeature(Window.FEATURE_ACTION_BAR_OVERLAY);

        // Initialize UI
        setContentView(R.layout.cropimage);
        
        mCropView = new CropView(this);
        getGLRoot().setContentPane(mCropView);

        ActionBar actionBar = getActionBar();
        int displayOptions = ActionBar.DISPLAY_HOME_AS_UP
                | ActionBar.DISPLAY_SHOW_TITLE;
        actionBar.setDisplayOptions(displayOptions, displayOptions);

     // CR535794-lilei-begin
      		mWallpaperManager = WallpaperManager.getInstance(this);
      		if(mWallpaperInfo == null){
      		mWallpaperInfo = mWallpaperManager.getWallpaperInfo();
      		}
      		//PR928956 can not set wallpaper when set live wallpaper before by fengke at 2015.02.10 start
      		//if(mWallpaperInfo != null){
      			//isDynamicWallpaper = !mWallpaperInfo.getPackageName().equals(DYNAMIC_TAT);
      		//}
      		//PR928956 can not set wallpaper when set live wallpaper before by fengke at 2015.02.10 end
      		mWideScreenModelButton = (ImageButton) findViewById(R.id.photopage_wide_model);
      		mSingleScreenModelButton = (ImageButton) findViewById(R.id.photopage_single_model);
      		mWideScreenModelButton.setOnClickListener(mWideBtOnClickListener);
      		mSingleScreenModelButton.setOnClickListener(mSingleBtOnClickListener);
      		msharedPreferences = PreferenceManager
      				.getDefaultSharedPreferences(this);

      		final CharSequence[] items = {
      				getResources().getText(R.string.set_as_lockscreen),
      				getResources().getText(R.string.set_as_homescreen),
      				getResources().getText(R.string.set_as_both) };

      		List<String> TextsList = new ArrayList<String>();
      		for (int i = 0; i < items.length; i++) {
      			TextsList.add(items[i].toString());
      		}

      		MyListAdapter myListAdapter = new MyListAdapter(this, TextsList);
      		AlertDialog.Builder builder = new AlertDialog.Builder(this);
      		builder.setTitle(getResources().getText(R.string.set_wallpaper_title));
      		builder.setAdapter(myListAdapter,
      				new DialogInterface.OnClickListener() {
      					public void onClick(DialogInterface dialog, int item) {
      						boolean isChooseWideMode = msharedPreferences
      								.getBoolean(KEY_IS_WIDE_MODE, true);
      						switch (item) {
      						case 0:
      							if (isChooseWideMode || isDynamicWallpaper) {   								
      							}
      							else {
      								setWallPaperLockscreen();
      							}
      							break;
      						case 1:
      							if(isDynamicWallpaper){     								
      							}else{
      							setWallPaperHomescreen();
      							}
      							break;
      						case 2:
      							if(isDynamicWallpaper){	
      								//setWallPaperHomescreen();
     									isClickBoth = true;
     									setWallPaperBoth();
      							}else{
      								if(isChooseWideMode){
      								}else{
      									isClickBoth = true;
      									setWallPaperBoth();
      								}
      							}
      							break;
      						default:
      							break;
      						}
      					}
      				});

      		mAlertDialog = builder.create();
      		
      		
      		boolean isWideDefault = msharedPreferences.getBoolean(
      				KEY_IS_WIDE_MODE, true);    	
      		//CR493910-lilei-begin
      		if(isWideDefault){
      			mWideScreenModelButton.setBackgroundResource(R.drawable.wide_screen_sel);
//      	 		Settings.System.putInt(CropImage.this.getContentResolver(),
//      	 				LAUNCHER_WALLPAPER_MULTIPLE_SCREEN_FLAG, 1);	//pr 541882 from chen.xiang@tct-nj.com
      		}else{
//      	 		Settings.System.putInt(CropImage.this.getContentResolver(),
//      	 				LAUNCHER_WALLPAPER_MULTIPLE_SCREEN_FLAG, 0);	pr 541882 from chen.xiang@tct-nj.com
      			mSingleScreenModelButton.setBackgroundResource(R.drawable.portrait_screen_sel);
      		}
      		//CR493910-lilei-end
      	// CR535794-lilei-end
        convertUriIfDrm(this, getIntent().getData());//PR670976-chen.gui@tcl.com-for can't load the image issue add

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            mBitmapInIntent = extras.getParcelable(KEY_DATA);
            mMediaItem = getMediaItemFromIntentData();
            if (mMediaItem == null && mBitmapInIntent == null) {
                Toast.makeText(this, R.string.no_such_item, Toast.LENGTH_SHORT).show();
                finish();
                return;
            }
            Log.i(TAG,"create:mMediaItem="+mMediaItem);

            mSetStereoWallpaper = 
                IS_STEREO_DISPLAY_SUPPORTED &&
                    ((mMediaItem.getSupportedOperations() & 
                      MediaItem.SUPPORT_STEREO_DISPLAY) != 0) &&
                    ((mMediaItem.getSupportedOperations() & 
                      MediaItem.SUPPORT_CONVERT_TO_3D) == 0) &&
                    null != getIntent().getExtras() &&
                    getIntent().getExtras().getBoolean(KEY_SET_AS_WALLPAPER, false);
            Log.d(TAG,"onCreate:mSetStereoWallpaper="+mSetStereoWallpaper);
           //PR540221-lilei-begin
            if(getIntent().getExtras() !=null && !getIntent().getExtras().getBoolean(KEY_SET_AS_WALLPAPER, false)){
            	RelativeLayout mRelativeLayout = (RelativeLayout)findViewById(R.id.photopage_bottom_controls);
            	mRelativeLayout.setVisibility(View.INVISIBLE);
            }
          //PR540221-lilei-end
          //add by Rock.song for drm file not set lockscreen PR667097 @20140519{
          if(DrmManagerClient.isMTKDrmEnable()) {
              String filePath = mMediaItem.getFilePath();
              DrmManagerClient drmClient = DrmManagerClient.getInstance(this);
              if(null != filePath){
                  if (drmClient.isDrm(filePath)) {
                      Log.i(TAG, "drm file filePath =" + filePath);
                      LinearLayout linearLayout = (LinearLayout)findViewById(R.id.photopage_wide_single_ctronl);
                      linearLayout.setVisibility(View.GONE);
                  }
              }
          }
          //}add by Rock.song for drm file not set lockscreen PR667097 @20140519
            if (mSetStereoWallpaper) {
                //
                mCropView.setStereoWallpaperMode(true);
                //modify action bar title
                actionBar.setTitle(R.string.stereo3d_preview_title);
            }
            if (extras.getBoolean(KEY_SHOW_WHEN_LOCKED, false)) {
                getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED);
            }
        }

        mMainHandler = new SynchronizedHandler(getGLRoot()) {
            @Override
            public void handleMessage(Message message) {
                switch (message.what) {
                    case MSG_LARGE_BITMAP: {
                        dismissProgressDialogIfShown();
                        onBitmapRegionDecoderAvailable((BitmapRegionDecoder) message.obj);
                        break;
                    }
                    case MSG_BITMAP: {
                        dismissProgressDialogIfShown();
                        onBitmapAvailable((Bitmap) message.obj);
                        break;
                    }
                    case MSG_SHOW_SAVE_ERROR: {
                        dismissProgressDialogIfShown();
                        setResult(RESULT_CANCELED);
                        Toast.makeText(CropImage.this,
                                CropImage.this.getString(R.string.save_error),
                                Toast.LENGTH_LONG).show();
                        finish();
                    }
                    case MSG_SAVE_COMPLETE: {
                        dismissProgressDialogIfShown();
                        setResult(RESULT_OK, (Intent) message.obj);
                        finish();
                        break;
                    }
                    case MSG_CANCEL_DIALOG: {
                        setResult(RESULT_CANCELED);
                        finish();
                        break;
                    }
                    case MSG_SECOND_BITMAP: {
                        //added to treat the decoded second image of stereo photo
                        onSecondBitmapAvailable((Bitmap) message.obj);
                        break;
                    }
                }
            }
        };

        setCropParameters();
    }

    @Override
    protected void onSaveInstanceState(Bundle saveState) {
        saveState.putInt(KEY_STATE, mState);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.crop, menu);

        //when setting stereo wallpaper, display "SET" instead of "CROP"
        if (mSetStereoWallpaper) {
            MenuItem item = menu.findItem(R.id.save);
            if (item != null) item.setTitle(R.string.stereo3d_preview_button_set);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home: {
                finish();
                break;
            }
            case R.id.cancel: {
                setResult(RESULT_CANCELED);
                finish();
                break;
            }
            case R.id.save: {
                //PR540221-lilei-begin
                //CR535794 -lilei-begin
                MtkLog.w(TAG, "fengke onOptionsItemSelected save isDynamicWallpaper = " + isDynamicWallpaper);//PR928956 can not set wallpaper add some log by fengke at 2015.02.10
                Bundle extra = getIntent().getExtras();
                if(extra != null && extra.getBoolean(KEY_SET_AS_WALLPAPER)){
                    //PR908587 only set Homescreen by fengke at2015.01.23 start
                    //mAlertDialog.show();
                    if (isDynamicWallpaper) {
                    }else{
                        setWallPaperHomescreen();
                    }
                    //PR908587 only set Homescreen by fengke at2015.01.23 end
                }else{
                    onSaveClicked();
                }
                //mAlertDialog.show();
                //CR535794 -lilei-end
                //PR540221-lilei-end
                break;
            }
        }
        return true;
    }

    @Override
    public void onBackPressed() {
        finish();
    }

 private class SaveBothOutput implements Job<Intent> {
        private final RectF mCropRect;

        public SaveBothOutput(RectF cropRect) {
            mCropRect = cropRect;
        }

        @Override
        public Intent run(JobContext jc) {
            MtkLog.d(TAG, "SaveOutput.run");
            RectF cropRect = mCropRect;
            Bundle extra = getIntent().getExtras();

            Rect rect = new Rect(
                    Math.round(cropRect.left), Math.round(cropRect.top),
                    Math.round(cropRect.right), Math.round(cropRect.bottom));

            Intent result = new Intent();
            result.putExtra(KEY_CROPPED_RECT, rect);
            Bitmap cropped = null;
            boolean outputted = false;
            if (extra != null) {
                Uri uri = (Uri) extra.getParcelable(MediaStore.EXTRA_OUTPUT);
                if (uri != null) {
                    if (jc.isCancelled()) return null;
                    outputted = true;
                    cropped = getCroppedImage(jc, rect);
                    storePicToSDcard(cropped);
                    if (!saveBitmapToUri(jc, cropped, uri)) return null;
                }
                if (extra.getBoolean(KEY_RETURN_DATA, false)) {
                    if (jc.isCancelled()) return null;
                    MtkLog.d(TAG, "SaveOutput: KEY_RETURN_DATA");
                    outputted = true;
                    if (cropped == null) cropped = getCroppedImage(jc, rect);
                    result.putExtra(KEY_DATA, cropped);
                }
                if (extra.getBoolean(KEY_SET_AS_WALLPAPER, false)) {
                    if (jc.isCancelled()) return null;
                    MtkLog.d(TAG, "SaveOutput: KEY_SET_AS_WALLPAPER");
                    outputted = true;
                    //if 3d wallpaper is to be set, we bind to 3D wallpaper
                    //service and send uri to it.
                    if (mSetStereoWallpaper) {
                        MtkLog.d(TAG, "SaveOutput: KEY_SET_AS_WALLPAPER: stereo");
                        mStereoWallpaperUri = getIntent().getData();
                        Log.i(TAG,"saveoutput:run:mStereoWallpaperUri="+mStereoWallpaperUri);
                        //bind remote service
                        if (!doBindService()) {
                            //we have to change the state to save done because
                            //progress dialog will never disappear when this
                            //variable remains unchanged. This may be Google
                            //default issue, which other cases do not encounter.
                            mState = STATE_SAVE_DONE;
                            return null;
                        }
                    } else {
                        MtkLog.d(TAG, "SaveOutput: [wallpaper] => getCroppedImage");
                        if (cropped == null) cropped = getCroppedImage(jc, rect);
                        MtkLog.d(TAG, "SaveOutput: [wallpaper] <= getCroppedImage");
                        MtkLog.d(TAG, "SaveOutput: [wallpaper] => setAsWallpaper");
                        storePicToSDcard(cropped);
                        if (!setAsWallpaper(jc, cropped)) return null;
                        MtkLog.d(TAG, "SaveOutput: [wallpaper] <= setAsWallpaper");
                    }
                }
            }
            if (!outputted) {
                if (jc.isCancelled()) return null;
                MtkLog.d(TAG, "SaveOutput: ordinary crop");
                MtkLog.d(TAG, "SaveOutput: [ordinary] => getCroppedImage");
                if (cropped == null) cropped = getCroppedImage(jc, rect);
                MtkLog.d(TAG, "SaveOutput: [ordinary] <= getCroppedImage");
                MtkLog.d(TAG, "SaveOutput: [ordinary] => saveToMediaProvider");
                Uri data = saveToMediaProvider(jc, cropped);
                MtkLog.d(TAG, "SaveOutput: [ordinary] <= saveToMediaProvider");
                if (data != null) result.setData(data);
            }
            mState = STATE_SAVE_DONE;
            return result;
        }
    }

    private class SaveOutput implements Job<Intent> {
        private final RectF mCropRect;

        public SaveOutput(RectF cropRect) {
            mCropRect = cropRect;
        }

        @Override
        public Intent run(JobContext jc) {
            MtkLog.w(TAG, "fengke SaveOutput.run");//PR928956 can not set wallpaper add some log by fengke at 2015.02.10
            RectF cropRect = mCropRect;
            Bundle extra = getIntent().getExtras();

            Rect rect = new Rect(
                    Math.round(cropRect.left), Math.round(cropRect.top),
                    Math.round(cropRect.right), Math.round(cropRect.bottom));

            Intent result = new Intent();
            result.putExtra(KEY_CROPPED_RECT, rect);
            Bitmap cropped = null;
            boolean outputted = false;
            if (extra != null) {
                Uri uri = (Uri) extra.getParcelable(MediaStore.EXTRA_OUTPUT);
                if (uri != null) {
                    if (jc.isCancelled()) return null;
                    outputted = true;
                    cropped = getCroppedImage(jc, rect);
                    if (!saveBitmapToUri(jc, cropped, uri)) return null;
                }
                if (extra.getBoolean(KEY_RETURN_DATA, false)) {
                    if (jc.isCancelled()) return null;
                    MtkLog.d(TAG, "SaveOutput: KEY_RETURN_DATA");
                    outputted = true;
                    if (cropped == null) cropped = getCroppedImage(jc, rect);
                    result.putExtra(KEY_DATA, cropped);
                }
                if (extra.getBoolean(KEY_SET_AS_WALLPAPER, false)) {
                    if (jc.isCancelled()) return null;
                    MtkLog.d(TAG, "SaveOutput: KEY_SET_AS_WALLPAPER");
                    outputted = true;
                    //if 3d wallpaper is to be set, we bind to 3D wallpaper
                    //service and send uri to it.
                    if (mSetStereoWallpaper) {
                        MtkLog.d(TAG, "SaveOutput: KEY_SET_AS_WALLPAPER: stereo");
                        mStereoWallpaperUri = getIntent().getData();
                        Log.i(TAG,"saveoutput:run:mStereoWallpaperUri="+mStereoWallpaperUri);
                        //bind remote service
                        if (!doBindService()) {
                            //we have to change the state to save done because
                            //progress dialog will never disappear when this
                            //variable remains unchanged. This may be Google
                            //default issue, which other cases do not encounter.
                            mState = STATE_SAVE_DONE;
                            return null;
                        }
                    } else {
                        MtkLog.d(TAG, "SaveOutput: [wallpaper] => getCroppedImage");
                        if (cropped == null) cropped = getCroppedImage(jc, rect);
                        MtkLog.d(TAG, "SaveOutput: [wallpaper] <= getCroppedImage");
                        MtkLog.d(TAG, "SaveOutput: [wallpaper] => setAsWallpaper");
                        if (!setAsWallpaper(jc, cropped)) return null;
                        MtkLog.d(TAG, "SaveOutput: [wallpaper] <= setAsWallpaper");
                    }
                }
            }
            if (!outputted) {
                if (jc.isCancelled()) return null;
                MtkLog.d(TAG, "SaveOutput: ordinary crop");
                MtkLog.d(TAG, "SaveOutput: [ordinary] => getCroppedImage");
                if (cropped == null) cropped = getCroppedImage(jc, rect);
                MtkLog.d(TAG, "SaveOutput: [ordinary] <= getCroppedImage");
                MtkLog.d(TAG, "SaveOutput: [ordinary] => saveToMediaProvider");
                Uri data = saveToMediaProvider(jc, cropped);
                MtkLog.d(TAG, "SaveOutput: [ordinary] <= saveToMediaProvider");
                if (data != null) result.setData(data);
            }
            mState = STATE_SAVE_DONE;
            return result;
        }
    }

    public static String determineCompressFormat(MediaObject obj) {
        String compressFormat = "JPEG";
        if (obj instanceof MediaItem) {
            String mime = ((MediaItem) obj).getMimeType();
            if (mime.contains("png") || mime.contains("gif")) {
              // Set the compress format to PNG for png and gif images
              // because they may contain alpha values.
              compressFormat = "PNG";
            }
        }
        return compressFormat;
    }

    private boolean setAsWallpaper(JobContext jc, Bitmap wallpaper) {
        try {
            Log.w(TAG, "fengke setAsWallpaper............");//PR928956 can not set wallpaper add some log by fengke at 2015.02.10
            WallpaperManager.getInstance(this).setBitmap(wallpaper);
        } catch (IOException e) {
            Log.w(TAG, "fail to set wall paper", e);
        }
        return true;
    }

    private File saveMedia(
            JobContext jc, Bitmap cropped, File directory, String filename, ExifData exifData) {
        // Try file-1.jpg, file-2.jpg, ... until we find a filename
        // which does not exist yet.
        File candidate = null;
        String fileExtension = getFileExtension();
        for (int i = 1; i < MAX_FILE_INDEX; ++i) {
            candidate = new File(directory, filename + "-" + i + "."
                    + fileExtension);
            try {
                if (candidate.createNewFile()) break;
            } catch (IOException e) {
                Log.e(TAG, "fail to create new file: "
                        + candidate.getAbsolutePath(), e);
                return null;
            }
        }
        if (!candidate.exists() || !candidate.isFile()) {
            throw new RuntimeException("cannot create file: " + filename);
        }

        candidate.setReadable(true, false);
        candidate.setWritable(true, false);

        try {
            FileOutputStream fos = new FileOutputStream(candidate);
            try {
                if (exifData != null) {
                    ExifOutputStream eos = new ExifOutputStream(fos);
                    eos.setExifData(exifData);
                    saveBitmapToOutputStream(jc, cropped,
                            convertExtensionToCompressFormat(fileExtension), eos);
                } else {
                    saveBitmapToOutputStream(jc, cropped,
                            convertExtensionToCompressFormat(fileExtension), fos);
                }
            } finally {
                fos.close();
            }
        } catch (IOException e) {
            Log.e(TAG, "fail to save image: "
                    + candidate.getAbsolutePath(), e);
            candidate.delete();
            return null;
        }

        if (jc.isCancelled()) {
            candidate.delete();
            return null;
        }

        return candidate;
    }

    private ExifData getExifData(String path) {
        FileInputStream is = null;
        try {
            is = new FileInputStream(path);
            ExifReader reader = new ExifReader();
            ExifData data = reader.read(is);
            return data;
        } catch (Throwable t) {
            Log.w(TAG, "Cannot read EXIF data", t);
            return null;
        } finally {
            Util.closeSilently(is);
        }
    }

    private static final String EXIF_SOFTWARE_VALUE = "Android Gallery";

    private void changeExifData(ExifData data, int width, int height) {
        data.addTag(ExifTag.TAG_IMAGE_WIDTH).setValue(width);
        data.addTag(ExifTag.TAG_IMAGE_LENGTH).setValue(height);
        data.addTag(ExifTag.TAG_SOFTWARE).setValue(EXIF_SOFTWARE_VALUE);
        data.addTag(ExifTag.TAG_DATE_TIME).setTimeValue(System.currentTimeMillis());
        // Remove the original thumbnail
        // TODO: generate a new thumbnail for the cropped image.
        data.removeThumbnailData();
    }

    private Uri saveToMediaProvider(JobContext jc, Bitmap cropped) {
        if (PicasaSource.isPicasaImage(mMediaItem)) {
            return savePicasaImage(jc, cropped);
        } else if (mMediaItem instanceof LocalImage) {
            Uri uri = saveLocalImage(jc, cropped);
            if (IS_DRM_SUPPORTED) {
                Path path = mMediaItem.getPath();
                int mtkInclusion = null == path ? 0 : path.getMtkInclusion();
                if (mtkInclusion != 0) {
                uri = uri.buildUpon().appendQueryParameter("mtkInclusion", 
                     String.valueOf(mtkInclusion)).build();
                }
            }
            return uri;
        } else {
            return saveGenericImage(jc, cropped);
        }
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    private static void setImageSize(ContentValues values, int width, int height) {
        // The two fields are available since ICS but got published in JB
        if (ApiHelper.HAS_MEDIA_COLUMNS_WIDTH_AND_HEIGHT) {
            values.put(Images.Media.WIDTH, width);
            values.put(Images.Media.HEIGHT, height);
        }
    }

    private Uri savePicasaImage(JobContext jc, Bitmap cropped) {
        if (!DOWNLOAD_BUCKET.isDirectory() && !DOWNLOAD_BUCKET.mkdirs()) {
            throw new RuntimeException("cannot create download folder");
        }
        String filename = PicasaSource.getImageTitle(mMediaItem);
        int pos = filename.lastIndexOf('.');
        if (pos >= 0) filename = filename.substring(0, pos);
        ExifData exifData = new ExifData(ByteOrder.BIG_ENDIAN);
        PicasaSource.extractExifValues(mMediaItem, exifData);
        changeExifData(exifData, cropped.getWidth(), cropped.getHeight());
        File output = saveMedia(jc, cropped, DOWNLOAD_BUCKET, filename, exifData);
        if (output == null) return null;

        long now = System.currentTimeMillis() / 1000;
        ContentValues values = new ContentValues();
        values.put(Images.Media.TITLE, PicasaSource.getImageTitle(mMediaItem));
        values.put(Images.Media.DISPLAY_NAME, output.getName());
        values.put(Images.Media.DATE_TAKEN, PicasaSource.getDateTaken(mMediaItem));
        values.put(Images.Media.DATE_MODIFIED, now);
        values.put(Images.Media.DATE_ADDED, now);
        values.put(Images.Media.MIME_TYPE, getOutputMimeType());
        values.put(Images.Media.ORIENTATION, 0);
        values.put(Images.Media.DATA, output.getAbsolutePath());
        values.put(Images.Media.SIZE, output.length());
        setImageSize(values, cropped.getWidth(), cropped.getHeight());

        double latitude = PicasaSource.getLatitude(mMediaItem);
        double longitude = PicasaSource.getLongitude(mMediaItem);
        if (GalleryUtils.isValidLocation(latitude, longitude)) {
            values.put(Images.Media.LATITUDE, latitude);
            values.put(Images.Media.LONGITUDE, longitude);
        }
        return getContentResolver().insert(
                Images.Media.EXTERNAL_CONTENT_URI, values);
    }

    private Uri saveLocalImage(JobContext jc, Bitmap cropped) {
        LocalImage localImage = (LocalImage) mMediaItem;

        File oldPath = new File(localImage.filePath);
        File directory = new File(oldPath.getParent());

        String filename = oldPath.getName();
        int pos = filename.lastIndexOf('.');
        if (pos >= 0) filename = filename.substring(0, pos);
        File output = null;

        ExifData exifData = null;
        if (convertExtensionToCompressFormat(getFileExtension()) == CompressFormat.JPEG) {
            exifData = getExifData(oldPath.getAbsolutePath());
            if (exifData != null) {
                changeExifData(exifData, cropped.getWidth(), cropped.getHeight());
            }
        }
        output = saveMedia(jc, cropped, directory, filename, exifData);
        if (output == null) return null;

        long now = System.currentTimeMillis() / 1000;
        ContentValues values = new ContentValues();
        values.put(Images.Media.TITLE, localImage.caption);
        values.put(Images.Media.DISPLAY_NAME, output.getName());
        values.put(Images.Media.DATE_TAKEN, localImage.dateTakenInMs);
        values.put(Images.Media.DATE_MODIFIED, now);
        values.put(Images.Media.DATE_ADDED, now);
        values.put(Images.Media.MIME_TYPE, getOutputMimeType());
        values.put(Images.Media.ORIENTATION, 0);
        values.put(Images.Media.DATA, output.getAbsolutePath());
        values.put(Images.Media.SIZE, output.length());

        setImageSize(values, cropped.getWidth(), cropped.getHeight());

        if (GalleryUtils.isValidLocation(localImage.latitude, localImage.longitude)) {
            values.put(Images.Media.LATITUDE, localImage.latitude);
            values.put(Images.Media.LONGITUDE, localImage.longitude);
        }
        return getContentResolver().insert(
                Images.Media.EXTERNAL_CONTENT_URI, values);
    }

    private Uri saveGenericImage(JobContext jc, Bitmap cropped) {
        if (!DOWNLOAD_BUCKET.isDirectory() && !DOWNLOAD_BUCKET.mkdirs()) {
            throw new RuntimeException("cannot create download folder");
        }

        long now = System.currentTimeMillis();
        String filename = new SimpleDateFormat(TIME_STAMP_NAME).
                format(new Date(now));

        File output = saveMedia(jc, cropped, DOWNLOAD_BUCKET, filename, null);
        if (output == null) return null;

        ContentValues values = new ContentValues();
        values.put(Images.Media.TITLE, filename);
        values.put(Images.Media.DISPLAY_NAME, output.getName());
        values.put(Images.Media.DATE_TAKEN, now);
        values.put(Images.Media.DATE_MODIFIED, now / 1000);
        values.put(Images.Media.DATE_ADDED, now / 1000);
        values.put(Images.Media.MIME_TYPE, getOutputMimeType());
        values.put(Images.Media.ORIENTATION, 0);
        values.put(Images.Media.DATA, output.getAbsolutePath());
        values.put(Images.Media.SIZE, output.length());

        setImageSize(values, cropped.getWidth(), cropped.getHeight());

        return getContentResolver().insert(
                Images.Media.EXTERNAL_CONTENT_URI, values);
    }

    private boolean saveBitmapToOutputStream(
            JobContext jc, Bitmap bitmap, CompressFormat format, OutputStream os) {
        // We wrap the OutputStream so that it can be interrupted.
        final InterruptableOutputStream ios = new InterruptableOutputStream(os);
        jc.setCancelListener(new CancelListener() {
                @Override
                public void onCancel() {
                    ios.interrupt();
                }
            });
        try {
            bitmap.compress(format, DEFAULT_COMPRESS_QUALITY, ios);
            return !jc.isCancelled();
        } finally {
            jc.setCancelListener(null);
            Utils.closeSilently(ios);
        }
    }

    private boolean saveBitmapToUri(JobContext jc, Bitmap bitmap, Uri uri) {
        try {
            OutputStream out = getContentResolver().openOutputStream(uri);
            try {
                return saveBitmapToOutputStream(jc, bitmap,
                        convertExtensionToCompressFormat(getFileExtension()), out);
            } finally {
                Utils.closeSilently(out);
            }
        } catch (FileNotFoundException e) {
            Log.w(TAG, "cannot write output", e);
        }
        return true;
    }

    private CompressFormat convertExtensionToCompressFormat(String extension) {
        return extension.equals("png")
                ? CompressFormat.PNG
                : CompressFormat.JPEG;
    }

    private String getOutputMimeType() {
        return getFileExtension().equals("png") ? "image/png" : "image/jpeg";
    }

    private String getFileExtension() {
        String requestFormat = getIntent().getStringExtra(KEY_OUTPUT_FORMAT);
        String outputFormat = (requestFormat == null)
                ? determineCompressFormat(mMediaItem)
                : requestFormat;

        outputFormat = outputFormat.toLowerCase();
        return (outputFormat.equals("png") || outputFormat.equals("gif"))
                ? "png" // We don't support gif compression.
                : "jpg";
    }

     private void onSaveClicked() {
        MtkLog.w(TAG, "fengke onSaveClicked");//PR928956 can not set wallpaper add some log by fengke at 2015.02.10
        Bundle extra = getIntent().getExtras();
        RectF cropRect = mCropView.getCropRectangle();
        if (cropRect == null) return;
        mState = STATE_SAVING;
        if(!isClickBoth){
        int messageId = extra != null && extra.getBoolean(KEY_SET_AS_WALLPAPER)
                ? R.string.wallpaper
                : R.string.saving_image;
        mProgressDialog = ProgressDialog.show(
                this, null, getString(messageId), true, false);
        }
        //add by Rock.Song for PR631590 @20140327{
        if(DrmManagerClient.isMTKDrmEnable()) {
            String filePath = mMediaItem.file_path;
            DrmManagerClient drmClient = DrmManagerClient.getInstance(this);
            if (drmClient.isDrm(filePath)) {
                if (extra.getBoolean(KEY_SET_AS_WALLPAPER) && !drmClient.drmSetAsWallpaper(this, filePath)) {
                    String toastMsg = String.format(this.getResources().getString(com.jrdcom.internal.R.string.drm_no_crop), filePath);
                    Toast.makeText(this, toastMsg, Toast.LENGTH_LONG).show();
                    return;
                }
                //add for PR 631865 by guichen begin
                if (extra.getBoolean(KEY_SET_AS_WALLPAPER) && (filePath != null) && (!"".equals(filePath)) && (new File(filePath)).exists()) {
                    Settings.System.putString(this.getContentResolver(), DrmManagerClient.WALLPAPER_FILEPATH, filePath);
                    drmClient.watchingDrmWallpaperStatus(this.getAndroidContext(), filePath);
                }
                //add for PR 631865 by guichen begin
            }
        }
        //}add by Rock.Song for PR631590 @20140327
        if (null != mTIVA) {
            //when save clicked, we know that no further decoder task
            //in TileImageViewAdapter is meaningful, so we clear it to
            //to avoid blocking by its synchronizing RegionDecoder when
            //we call getCroppedImage() for very long time
            //Note: we should also add protection in TileImageViewAdapter
            //to avoid race condition.
            if (!mSetStereoWallpaper) {
                Log.i(TAG,"onSaveClick:clear TIVA's region decoder!");
                //mTIVA.clear();
                mTIVA.clearRegionDecoder();
            } else {
                Log.i(TAG,"onSaveClick:set null RegionDecoder");
                //mTIVA.setStereo(null, mBitmap,mBitmap.getWidth(),mBitmap.getHeight());
            }
        }
        //PR525087-lilei-begin
        if(isClickBoth){
            mSaveTask = getThreadPool().submit(new SaveBothOutput(cropRect),
                    new FutureListener<Intent>() {
                @Override
                public void onFutureDone(Future<Intent> future) {
                    mSaveTask = null;
                    if (STATE_SAVE_DONE != mState) {
//                        MtkLog.w(TAG, "save task: save state != STATE_SAVE_DONE, cancel and return...");
                        mMainHandler.sendEmptyMessage(MSG_SHOW_SAVE_ERROR);
                        return;
                    }
                    // The future has been canceled, but the saving process
                    // has actually been done, so we continue to send MSG_SAVE_COMPLETE
                    // and therefore common out the following line
                    // if (future.isCancelled()) return;
                    Intent intent = future.get();
                    if (intent != null) {
                        //Added for stereo wallpaper
                        if (mSetStereoWallpaper) {
                            mStereoWallpaperIntent = intent;
                            return;
                        }

                        Log.v(TAG,"SaveTask:onFutureDone:send complete message");

                        mMainHandler.sendMessage(mMainHandler.obtainMessage(
                                MSG_SAVE_COMPLETE, intent));
                    } else {
                        Log.v(TAG,"SaveTask:onFutureDone:send error message");
                        mMainHandler.sendEmptyMessage(MSG_SHOW_SAVE_ERROR);
                    }
                }
            });
        	
        }else{
        mSaveTask = getThreadPool().submit(new SaveOutput(cropRect),
                new FutureListener<Intent>() {
            @Override
            public void onFutureDone(Future<Intent> future) {
                mSaveTask = null;
                if (STATE_SAVE_DONE != mState) {
//                    MtkLog.w(TAG, "save task: save state != STATE_SAVE_DONE, cancel and return...");
                    mMainHandler.sendEmptyMessage(MSG_SHOW_SAVE_ERROR);
                    return;
                }
                // The future has been canceled, but the saving process
                // has actually been done, so we continue to send MSG_SAVE_COMPLETE
                // and therefore common out the following line
                // if (future.isCancelled()) return;
                Intent intent = future.get();
                if (intent != null) {
                    //Added for stereo wallpaper
                    if (mSetStereoWallpaper) {
                        mStereoWallpaperIntent = intent;
                        return;
                    }

                    Log.v(TAG,"SaveTask:onFutureDone:send complete message");
                    mMainHandler.sendMessage(mMainHandler.obtainMessage(
                            MSG_SAVE_COMPLETE, intent));
//                     }
                } else {
                    Log.v(TAG,"SaveTask:onFutureDone:send error message");
                    mMainHandler.sendEmptyMessage(MSG_SHOW_SAVE_ERROR);
                }
            }
        });}
      //PR525087-lilei-end
    }

    private Bitmap getCroppedImage(JobContext jc, Rect rect) {
        MtkLog.d(TAG, "getCroppedImage: rect=" + rect);
        Utils.assertTrue(rect.width() > 0 && rect.height() > 0);

        Bundle extras = getIntent().getExtras();
        // (outputX, outputY) = the width and height of the returning bitmap.
        int outputX = rect.width();
        int outputY = rect.height();
        //PR540221-lilei-begin
        //CR535794-lilei-begin
        if (extras != null && !extras.getBoolean(KEY_SET_AS_WALLPAPER)) {
            outputX = extras.getInt(KEY_OUTPUT_X, outputX);
            outputY = extras.getInt(KEY_OUTPUT_Y, outputY);
        }else{
        outputX = getWallpaperDesiredMinimumWidth();
        outputY = getWallpaperDesiredMinimumHeight();
         }
        //CR535794-lilei-end
      //PR540221-lilei-end
        if (outputX * outputY > MAX_PIXEL_COUNT) {
            float scale = FloatMath.sqrt((float) MAX_PIXEL_COUNT / outputX / outputY);
            Log.w(TAG, "scale down the cropped image: " + scale);
            outputX = Math.round(scale * outputX);
            outputY = Math.round(scale * outputY);
        }

        // (rect.width() * scaleX, rect.height() * scaleY) =
        // the size of drawing area in output bitmap
        float scaleX = 1;
        float scaleY = 1;
        Rect dest = new Rect(0, 0, outputX, outputY);
        if (extras == null || extras.getBoolean(KEY_SCALE, true)) {
            scaleX = (float) outputX / rect.width();
            scaleY = (float) outputY / rect.height();
            if (extras == null || !extras.getBoolean(
                    KEY_SCALE_UP_IF_NEEDED, false)) {
                if (scaleX > 1f) scaleX = 1;
                if (scaleY > 1f) scaleY = 1;
            }
        }

        // Keep the content in the center (or crop the content)
        int rectWidth = Math.round(rect.width() * scaleX);
        int rectHeight = Math.round(rect.height() * scaleY);
        dest.set(Math.round((outputX - rectWidth) / 2f),
                Math.round((outputY - rectHeight) / 2f),
                Math.round((outputX + rectWidth) / 2f),
                Math.round((outputY + rectHeight) / 2f));

        if (mBitmapInIntent != null) {
            Bitmap source = mBitmapInIntent;
            Bitmap result = Bitmap.createBitmap(
                    outputX, outputY, Config.ARGB_8888);
            Canvas canvas = new Canvas(result);
            canvas.drawBitmap(source, rect, dest, null);
            return result;
        }

        if (mUseRegionDecoder) {
            MtkLog.d(TAG, "getCroppedImage: use region decoder");
            int rotation = mMediaItem.getFullImageRotation();
            rotateRectangle(rect, mCropView.getImageWidth(),
                    mCropView.getImageHeight(), 360 - rotation);
            rotateRectangle(dest, outputX, outputY, 360 - rotation);

            BitmapFactory.Options options = new BitmapFactory.Options();
            int sample = BitmapUtils.computeSampleSizeLarger(
                    Math.max(scaleX, scaleY));
            options.inSampleSize = sample;

            // The decoding result is what we want if
            //   1. The size of the decoded bitmap match the destination's size
            //   2. The destination covers the whole output bitmap
            //   3. No rotation
            if ((rect.width() / sample) == dest.width()
                    && (rect.height() / sample) == dest.height()
                    && (outputX == dest.width()) && (outputY == dest.height())
                    && rotation == 0) {
                MtkLog.d(TAG, "getCroppedImage: use decoding result");
                // To prevent concurrent access in GLThread
                synchronized (mRegionDecoder) {
                    Bitmap bmp = null;
                    try {
                        Log.i(TAG,"getCroppedImage:decodeRegion(rect="+rect+"..)");
                        bmp = mRegionDecoder.decodeRegion(rect, options);
                        Log.v(TAG,"getCroppedImage:decodeRegion() returns"+bmp);
                    } catch (OutOfMemoryError e) {
                        Log.w(TAG,"getCroppedImage:out of memory when decoding:"+e);
                        bmp = null;
                    }
                    //As there is a chance no enough dvm memory for decoded Bitmap,
                    //Skia will return a null Bitmap. In this case, we have to
                    //downscale the decoded Bitmap by increase the options.inSampleSize
                    if (null == bmp) {
                        final int maxTryNum = 8;
                        for (int i=0; i < maxTryNum; i++) {
                            //we increase inSampleSize to expect a smaller Bitamp
                            options.inSampleSize *= 2;
                            Log.w(TAG,"getCroppedImage:try for sample size " +
                                    options.inSampleSize);
                            try {
                                Log.i(TAG,"getCroppedImage:decodeRegion(rect="+rect+"..)");
                                bmp = mRegionDecoder.decodeRegion(rect, options);
                                Log.v(TAG,"getCroppedImage:decodeRegion() returns"+bmp);
                            } catch (OutOfMemoryError e) {
                                Log.w(TAG,"getCroppedImage:out of memory when decoding:"+e);
                                bmp = null;
                            }
                            if (null != bmp) break;
                        }
                        if (null == bmp) {
                            Log.e(TAG,"getCroppedImage:failed to get a Bitmap");
                            return null;
                        }
                        //modify outputX,outputY if needed.
                        if (extras != null) {
                            if (outputX == extras.getInt(KEY_OUTPUT_X, 0) &&
                                outputY == extras.getInt(KEY_OUTPUT_Y, 0)) {
                                //if outputX & outputY is determined from extras,
                                //remains unchanged
                            } else {
                                //if outputX & outputY is determined by rect, change it
                                outputX = bmp.getWidth();
                                outputY = bmp.getHeight();
                            }
                        } else {
                            //if extra is null, outputX & outputY is directly determined
                            //from rect and MAX_PIXEL_COUNT
                            outputX = bmp.getWidth();
                            outputY = bmp.getHeight();
                        }
                    }
                    //modify rect
                    rect = new Rect(0, 0, bmp.getWidth(), bmp.getHeight());

                    Bitmap ret = null;
                    if (extras != null && extras.getBoolean(KEY_SCALE_UP_IF_NEEDED, false)
                            && ((scaleX > 1f) || (scaleY > 1f))) {
                        ret = Bitmap.createBitmap(outputX, outputY, Config.ARGB_8888);
                        Canvas c = new Canvas(ret);
                        c.drawBitmap(bmp, rect, new Rect(0, 0, outputX, outputY), null);
                        bmp.recycle();
                    } else {
                        ret = bmp;
                    }
                    
                    return ret;
                }
            }
            MtkLog.d(TAG, "getCroppedImage: [region] => createBitmap(" + outputX + ", " + outputY + ")");
            Bitmap result = Bitmap.createBitmap(
                    outputX, outputY, Config.ARGB_8888);
            MtkLog.d(TAG, "getCroppedImage: [region] <= createBitmap(" + outputX + ", " + outputY + ")");
            Canvas canvas = new Canvas(result);
            rotateCanvas(canvas, outputX, outputY, rotation);
            MtkLog.d(TAG, "getCroppedImage: [region] => drawInTiles: rect=" + rect + ", dest=" + dest + ", sample=" + sample);
            drawInTiles(jc, canvas, mRegionDecoder, rect, dest, sample);
            MtkLog.d(TAG, "getCroppedImage: [region] <= drawInTiles: rect=" + rect + ", dest=" + dest + ", sample=" + sample);
            return result;
        } else {
            MtkLog.d(TAG, "getCroppedImage: no region decoder");
            int rotation = mMediaItem.getRotation();
            rotateRectangle(rect, mCropView.getImageWidth(),
                    mCropView.getImageHeight(), 360 - rotation);
            rotateRectangle(dest, outputX, outputY, 360 - rotation);
            MtkLog.d(TAG, "getCroppedImage: [noregion] => createBitmap(" + outputX + ", " + outputY + ")");
            Bitmap result = Bitmap.createBitmap(outputX, outputY, Config.ARGB_8888);
            MtkLog.d(TAG, "getCroppedImage: [noregion] <= createBitmap(" + outputX + ", " + outputY + ")");
            Canvas canvas = new Canvas(result);
            rotateCanvas(canvas, outputX, outputY, rotation);
            MtkLog.d(TAG, "getCroppedImage: [noregion] => drawBitmap: rect=" + rect + ", dest=" + dest);
            canvas.drawBitmap(mBitmap,
                    rect, dest, new Paint(Paint.FILTER_BITMAP_FLAG));
            MtkLog.d(TAG, "getCroppedImage: [noregion] <= drawBitmap: rect=" + rect + ", dest=" + dest);
            return result;
        }
    }

    private static void rotateCanvas(
            Canvas canvas, int width, int height, int rotation) {
        canvas.translate(width / 2, height / 2);
        canvas.rotate(rotation);
        if (((rotation / 90) & 0x01) == 0) {
            canvas.translate(-width / 2, -height / 2);
        } else {
            canvas.translate(-height / 2, -width / 2);
        }
    }

    private static void rotateRectangle(
            Rect rect, int width, int height, int rotation) {
        if (rotation == 0 || rotation == 360) return;

        int w = rect.width();
        int h = rect.height();
        switch (rotation) {
            case 90: {
                rect.top = rect.left;
                rect.left = height - rect.bottom;
                rect.right = rect.left + h;
                rect.bottom = rect.top + w;
                return;
            }
            case 180: {
                rect.left = width - rect.right;
                rect.top = height - rect.bottom;
                rect.right = rect.left + w;
                rect.bottom = rect.top + h;
                return;
            }
            case 270: {
                rect.left = rect.top;
                rect.top = width - rect.right;
                rect.right = rect.left + h;
                rect.bottom = rect.top + w;
                return;
            }
            default: throw new AssertionError();
        }
    }

    private void drawInTiles(JobContext jc, Canvas canvas,
            BitmapRegionDecoder decoder, Rect rect, Rect dest, int sample) {
        int tileSize = TILE_SIZE * sample;
        Rect tileRect = new Rect();
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inPreferredConfig = Config.ARGB_8888;
        options.inSampleSize = sample;
        canvas.translate(dest.left, dest.top);
        canvas.scale((float) sample * dest.width() / rect.width(),
                (float) sample * dest.height() / rect.height());
        Paint paint = new Paint(Paint.FILTER_BITMAP_FLAG);
        for (int tx = rect.left, x = 0;
                tx < rect.right; tx += tileSize, x += TILE_SIZE) {
            for (int ty = rect.top, y = 0;
                    ty < rect.bottom; ty += tileSize, y += TILE_SIZE) {
                tileRect.set(tx, ty, tx + tileSize, ty + tileSize);
                if (tileRect.intersect(rect)) {
                    Bitmap bitmap;
                    //add protection in case rect is not valid
                    try {
                        // To prevent concurrent access in GLThread
                        MtkLog.v(TAG, "drawInTiles() before synchronize");
                        synchronized (decoder) {
                            MtkLog.v(TAG, "drawInTiles() begin decodeRegion()");
                            if (jc != null && jc.isCancelled()) {
                                MtkLog.w(TAG, "drawInTiles() job was canceled!");
                                return;
                            }
                            bitmap = decoder.decodeRegion(tileRect, options);
                            MtkLog.v(TAG, "drawInTiles() end decodeRegion()");
                        }
                        canvas.drawBitmap(bitmap, x, y, paint);
                        bitmap.recycle();
                    } catch (IllegalArgumentException e) {
                        Log.w(TAG,"drawInTiles:got exception:"+e);
                    }
                }
            }
        }
    }

    private void onBitmapRegionDecoderAvailable(
            BitmapRegionDecoder regionDecoder) {

        if (regionDecoder == null) {
            //Toast.makeText(this, R.string.load_image_fail, Toast.LENGTH_SHORT).show();
            //finish();
            //There is a chance that a image has no region decoder but can decode
            //thumbnail. some we change to decode thumbnail when fail.
            Log.w(TAG,"onBitmapRegionDecoderAvailable:failed and start to load thumb");
            startLoadBitmapTask();
            return;
        }
        mRegionDecoder = regionDecoder;
        mUseRegionDecoder = true;
        mState = STATE_LOADED;

        BitmapFactory.Options options = new BitmapFactory.Options();
        int width = regionDecoder.getWidth();
        int height = regionDecoder.getHeight();

        if (IS_STEREO_DISPLAY_SUPPORTED &&
            ((mMediaItem.getSupportedOperations() & 
              MediaItem.SUPPORT_STEREO_DISPLAY) != 0)) {
            width = StereoHelper.adjustDim(true, mMediaItem.getStereoLayout(),
                                               width);
            height = StereoHelper.adjustDim(false, mMediaItem.getStereoLayout(),
                                                height);
        }

        options.inSampleSize = BitmapUtils.computeSampleSize(width, height,
                BitmapUtils.UNCONSTRAINED, BACKUP_PIXEL_COUNT);
        mBitmap = regionDecoder.decodeRegion(
                new Rect(0, 0, width, height), options);
//        MtkLog.v(TAG, "onBitmapRegionDecoderAvailable() end decodeRegion()");
        if (mBitmap == null) {
            Log.e(TAG, "region decoder failed to decode the image!");
            Toast.makeText(this, R.string.load_image_fail, Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        //mCropView.setDataModel(new TileImageViewAdapter(mBitmap, 
        //        regionDecoder).setEnablePQ(false), mMediaItem.getFullImageRotation());
        TileImageViewAdapter tiva = new TileImageViewAdapter(mBitmap, regionDecoder);
        tiva.setEnablePQ(false);

        //adjust full image dimesion if needed
        if (IS_STEREO_DISPLAY_SUPPORTED &&
            ((mMediaItem.getSupportedOperations() & 
              MediaItem.SUPPORT_STEREO_DISPLAY) != 0)) {
            tiva.setRegionDecoder(regionDecoder, mBitmap, width, height);
        }

        //we hold this object
        mTIVA = tiva;
        mCropView.setDataModel(tiva, mMediaItem.getFullImageRotation());
        if (mDoFaceDetection) {
            mCropView.detectFaces(mBitmap);
        } else {
            mCropView.initializeHighlightRectangle();
        }
    }

    private void onBitmapAvailable(Bitmap bitmap) {
        if (bitmap == null) {
            MtkLog.e(TAG, "onBitmapAvailable: failed to load bitmap");
            Toast.makeText(this, R.string.fail_to_load_image, Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        mUseRegionDecoder = false;
        mState = STATE_LOADED;

        mBitmap = bitmap;
        //BitmapFactory.Options options = new BitmapFactory.Options();
        mCropView.setDataModel(new BitmapTileProvider(bitmap, 512),
                mMediaItem.getRotation());
        if (mDoFaceDetection) {
            mCropView.detectFaces(bitmap);
        } else {
            mCropView.initializeHighlightRectangle();
        }

        if (mSetStereoWallpaper) {
            mLoadSecondBitmapTask = getThreadPool().submit(
                    new LoadSecondBitmapDataTask(mMediaItem),
                    new FutureListener<Bitmap>() {
                public void onFutureDone(Future<Bitmap> future) {
                    mLoadSecondBitmapTask = null;
                    Bitmap bitmap = future.get();
                    if (future.isCancelled()) {
                        if (bitmap != null) bitmap.recycle();
                        return;
                    }
                    mMainHandler.sendMessage(mMainHandler.obtainMessage(
                            MSG_SECOND_BITMAP, bitmap));
                }
            });
        }
    }

    private void onSecondBitmapAvailable(Bitmap bitmap) {
        Log.i(TAG,"onSecondBitmapAvailable()");
        if (bitmap == null) {
            Log.w(TAG,"onSecondBitmapAvailable:got null second image");
            return;
        }

        //BitmapFactory.Options options = new BitmapFactory.Options();
        mCropView.setDataModel(new BitmapTileProvider(bitmap, 512),
                mMediaItem.getRotation());
        TileImageViewAdapter tiva = new TileImageViewAdapter();
        tiva.setScreenNail(mBitmap,mBitmap.getWidth(),mBitmap.getHeight());
        tiva.setStereoScreenNail(2, bitmap);
        //we hold this object
        mTIVA = tiva;
        //after loaded second image, enter stereo mode
        mCropView.setStereoMode(true);
        mCropView.setDataModel(tiva, mMediaItem.getFullImageRotation());
    }

    private void setCropParameters() {
        Bundle extras = getIntent().getExtras();
        if (extras == null)
            return;
        int aspectX = extras.getInt(KEY_ASPECT_X, 0);
        int aspectY = extras.getInt(KEY_ASPECT_Y, 0);
        if (aspectX != 0 && aspectY != 0) {
        	//PR540221-lilei-begin
        	//PR535147-lilei-begin
        	if(extras.getBoolean(KEY_SET_AS_WALLPAPER)){
      		boolean isChooseWideMode = msharedPreferences.getBoolean(
      				KEY_IS_WIDE_MODE, true);
          	 DisplayMetrics metric = new DisplayMetrics();
           this.getWindowManager().getDefaultDisplay().getMetrics(metric);
       	 int width = metric.widthPixels;
           int height = metric.heightPixels; 
           float tempAspectRatio = 0;
         //PR540937-lilei-begin
           mCropView.setAsWallpaper(true);
        	if(!isChooseWideMode){
        		tempAspectRatio = width > height ? (float)  height/ width : (float)  width/height;
            mCropView.setAspectRatio(tempAspectRatio);
        	}else{
        		tempAspectRatio = width > height ? (float) height * 2 / width : (float) width * 2 / height;
            mCropView.setAspectRatio(tempAspectRatio);
        	}
        	}else{
        		mCropView.setAsWallpaper(false);
        		mCropView.setAspectRatio((float) aspectX / aspectY);
        	}
        	//PR540937-lilei-end
           // mCropView.setAspectRatio((float) aspectX / aspectY);
        	//PR535147-lilei-end
        	//PR540221-lilei-end
        }

        float spotlightX = extras.getFloat(KEY_SPOTLIGHT_X, 0);
        float spotlightY = extras.getFloat(KEY_SPOTLIGHT_Y, 0);
        if (spotlightX != 0 && spotlightY != 0) {
            mCropView.setSpotlightRatio(spotlightX, spotlightY);
        }
    }

    private void initializeData() {
        Bundle extras = getIntent().getExtras();

        if (extras != null) {
            if (extras.containsKey(KEY_NO_FACE_DETECTION)) {
                mDoFaceDetection = !extras.getBoolean(KEY_NO_FACE_DETECTION);
            }

            if (mBitmapInIntent == null) {
                mBitmapInIntent = extras.getParcelable(KEY_DATA);
            }

            if (mBitmapInIntent != null) {
                MtkLog.d(TAG, "initializeData: bitmap in intent! (" + mBitmapInIntent.getWidth() 
                        + ", " + mBitmapInIntent.getHeight() + ")");
                mBitmapTileProvider =
                        new BitmapTileProvider(mBitmapInIntent, MAX_BACKUP_IMAGE_SIZE);
                mCropView.setDataModel(mBitmapTileProvider, 0);
                if (mDoFaceDetection) {
                    mCropView.detectFaces(mBitmapInIntent);
                } else {
                    mCropView.initializeHighlightRectangle();
                }
                mState = STATE_LOADED;
                return;
            }
        }

        //PR946884 modify for gallery focus close by fengke at 2015.03.16 start
        try {
            if (!this.isFinishing() && !this.isDestroyed()) {//PR946884 modify for gallery focus close by fengke at 2015.03.16
                mProgressDialog = ProgressDialog.show(
                        this, null, getString(R.string.loading_image), true, true);
                mProgressDialog.setCanceledOnTouchOutside(false);
                mProgressDialog.setCancelMessage(mMainHandler.obtainMessage(MSG_CANCEL_DIALOG));
            }
        } catch (Exception e) {
            MtkLog.e(TAG, "initializeData: mProgressDialog Exception e = " + e);
            finish();
            return;
        }
        //PR946884 modify for gallery focus close by fengke at 2015.03.16 end

        //as we have shift the create of MediaItem to onCreate,
        //we add a protection here
        if (null == mMediaItem) {
            mMediaItem = getMediaItemFromIntentData();
        }
        if (mMediaItem == null) {
            MtkLog.e(TAG, "initializeData: cannot find media item from intent data");
            //PR946884 modify for gallery focus close by fengke at 2015.03.16 start
            dismissProgressDialogIfShown();//mProgressDialog.dismiss();
            //PR946884 modify for gallery focus close by fengke at 2015.03.16 end
            Toast.makeText(this, R.string.no_such_item, Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        
        // M: prepare the file path for storage check
        initFilePathFromIntent();

        boolean supportedByBitmapRegionDecoder =
            (mMediaItem.getSupportedOperations() & MediaItem.SUPPORT_FULL_IMAGE) != 0;

        //when set 3D stereo wallpaper, we don't show crop box, we won't decode tile
        supportedByBitmapRegionDecoder = supportedByBitmapRegionDecoder &&
                                         !mSetStereoWallpaper;

        if (supportedByBitmapRegionDecoder) {
            mLoadTask = getThreadPool().submit(new LoadDataTask(mMediaItem),
                    new FutureListener<BitmapRegionDecoder>() {
                @Override
                public void onFutureDone(Future<BitmapRegionDecoder> future) {
                    mLoadTask = null;
                    BitmapRegionDecoder decoder = future.get();
                    if (future.isCancelled()) {
                        if (decoder != null) decoder.recycle();
                        return;
                    }
                    mMainHandler.sendMessage(mMainHandler.obtainMessage(
                            MSG_LARGE_BITMAP, decoder));
                }
            });
        } else {
          //mLoadBitmapTask = getThreadPool().submit(new LoadBitmapDataTask(mMediaItem),
            //        new FutureListener<Bitmap>() {
            //    public void onFutureDone(Future<Bitmap> future) {
            //        mLoadBitmapTask = null;
            //        Bitmap bitmap = future.get();
            //        if (future.isCancelled()) {
            //            if (bitmap != null) bitmap.recycle();
            //            return;
            //        }
            //        mMainHandler.sendMessage(mMainHandler.obtainMessage(
            //                MSG_BITMAP, bitmap));
            //    }
            //});
            //we shift the code to a function because we want to reuse this
            //code when get region decoder failed
            startLoadBitmapTask();
        }
    }

    private void startLoadBitmapTask() {
        mLoadBitmapTask = getThreadPool().submit(new LoadBitmapDataTask(mMediaItem),
                new FutureListener<Bitmap>() {
            public void onFutureDone(Future<Bitmap> future) {
                mLoadBitmapTask = null;
                Bitmap bitmap = future.get();
                if (future.isCancelled()) {
                    if (bitmap != null) bitmap.recycle();
                    return;
                }
                mMainHandler.sendMessage(mMainHandler.obtainMessage(
                        MSG_BITMAP, bitmap));
            }
        });
    }

    @Override
    protected void onResume() {
        MtkLog.d(TAG, "onResume");
        super.onResume();
        if (mState == STATE_INIT) initializeData();
        if (mState == STATE_SAVING) onSaveClicked();
        
        registerReceiverIfNeeded();
        if (mWorkerHandler != null) {
            mWorkerHandler.sendEmptyMessage(MSG_CHECK_STORAGE);
            Log.i(TAG, "onResume: start checking storage");
        }

        // TODO: consider to do it in GLView system
        GLRoot root = getGLRoot();
        root.lockRenderThread();
        try {
            mCropView.resume();
        } finally {
            root.unlockRenderThread();
        }
    }

    private void onSaveResumed() {
        Bundle extra = getIntent().getExtras();
        int messageId = extra != null && extra.getBoolean(KEY_SET_AS_WALLPAPER)
                ? R.string.wallpaper
                : R.string.saving_image;
        mProgressDialog = ProgressDialog.show(
                this, null, getString(messageId), true, false);
    }

    @Override
    protected void onPause() {
        MtkLog.d(TAG, "onPause");
        super.onPause();
        dismissProgressDialogIfShown();

        Future<BitmapRegionDecoder> loadTask = mLoadTask;
        if (loadTask != null && !loadTask.isDone()) {
            // load in progress, try to cancel it
            loadTask.cancel();
            loadTask.waitDone();
        }

        Future<Bitmap> loadBitmapTask = mLoadBitmapTask;
        if (loadBitmapTask != null && !loadBitmapTask.isDone()) {
            // load in progress, try to cancel it
            loadBitmapTask.cancel();
            loadBitmapTask.waitDone();
        }

        Future<Bitmap> loadSecondBitmapTask = mLoadSecondBitmapTask;
        if (loadSecondBitmapTask != null && !loadSecondBitmapTask.isDone()) {
            // load in progress, try to cancel it
            loadSecondBitmapTask.cancel();
            loadSecondBitmapTask.waitDone();
        }

        Future<Intent> saveTask = mSaveTask;
        if (saveTask != null && !saveTask.isDone()) {
            // save in progress, try to cancel it
            saveTask.cancel();
            saveTask.waitDone();
            //unbind 3d wallpaper service when the task cancels
            if (mSetStereoWallpaper) {
                doUnbindService();
            }
        }
        GLRoot root = getGLRoot();
        root.lockRenderThread();
        try {
            mCropView.pause();
        } finally {
            root.unlockRenderThread();
        }
        
        unregisterReceiverIfPossible();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mBitmapScreenNail != null) {
            mBitmapScreenNail.recycle();
            mBitmapScreenNail = null;
        }
    }

    private void dismissProgressDialogIfShown() {
        if (mProgressDialog != null) {
            mProgressDialog.dismiss();
            mProgressDialog = null;
        }
    }

    private MediaItem getMediaItemFromIntentData() {
        Uri uri = getIntent().getData();
        DataManager manager = getDataManager();
        Path path = manager.findPathByUri(uri, getIntent().getType());
        if (path == null) {
            Log.w(TAG, "cannot get path for: " + uri + ", or no data given");
            return null;
        }
        //MediaItem item = (MediaItem) manager.getMediaObject(path);
        MediaItem item = null;
        try {
            item = (MediaItem) manager.getMediaObject(path);
        } catch (RuntimeException e) {
            Log.e(TAG, "cannot get item for path: " + path.toString());
            return null;
        }
        
        // Since it's very possible that crop activity's process stays in background once used,
        // the DataManager in this process might still be the old one and might be out-synced with
        // the DataManager in the main process, we update rotation info from DB
        // each time we open the file to crop.
        if (item != null && (item instanceof LocalImage)) {
            android.database.Cursor c = null;
            try {
                c = getContentResolver().query(uri, new String[] {Images.ImageColumns.ORIENTATION}, null, null, null);
                if (c != null && c.moveToFirst()) {
                    ((LocalImage) item).rotation = c.getInt(0);
                }
            } catch (Exception e) {
                // in case any exception happens, we simply do not update the rotation info in item.
                Log.e(TAG, "Exception when trying to fetch orientation info", e);
            } finally {
                if (c != null) {
                    c.close();
                }
            }
        }
        
        return item;
    }

    private class LoadDataTask implements Job<BitmapRegionDecoder> {
        MediaItem mItem;

        public LoadDataTask(MediaItem item) {
            mItem = item;
        }

        @Override
        public BitmapRegionDecoder run(JobContext jc) {
            return mItem == null ? null : mItem.requestLargeImage().run(jc);
        }
    }

    private class LoadBitmapDataTask implements Job<Bitmap> {
        MediaItem mItem;

        public LoadBitmapDataTask(MediaItem item) {
            mItem = item;
        }
        @Override
        public Bitmap run(JobContext jc) {
            //for picture quality enhancement, we don't want enhanced Bitmap
            //to be cropped and saved, causing double enhancement
            //Logic modified to support stereo wallpaper: as the actually
            //operations to set wall paper is done be 3D wallpaper app, we
            //only have to display the stereo image, so we don't have to
            //decode the origin image without picture quality enhancement.
            if (MediatekFeature.isPictureQualityEnhanceSupported() &&
                !mSetStereoWallpaper) {
                if (null == mItem) return null;
                Params params = new Params();
                params.inOriginalFrame = true;
                DataBundle dataBundle = mItem.requestImage(
                                   MediaItem.TYPE_THUMBNAIL, params).run(jc);
                return dataBundle == null ? null : dataBundle.originalFrame;
            }

            return mItem == null
                    ? null
                    : mItem.requestImage(MediaItem.TYPE_THUMBNAIL).run(jc);
        }
    }

    private class LoadSecondBitmapDataTask implements Job<Bitmap> {
        MediaItem mItem;

        public LoadSecondBitmapDataTask(MediaItem item) {
            mItem = item;
        }
        public Bitmap run(JobContext jc) {
            if (mItem == null) return null;
            Params params = new Params();
            params.inSecondFrame = true;
            //params.inPQEnhance = true;

            DataBundle dataBundle = mItem.requestImage(MediaItem.TYPE_THUMBNAIL,
                                               params).run(jc);
            return dataBundle == null ? null : dataBundle.secondFrame;
        }
    }


    /**
     * Handler of incoming messages from service.
     */
    class IncomingHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_3D_WALLPAPER_RESULT_OK:
                    Log.v(TAG,"IncomingHandler:Received from service: OK");
                    doUnbindService();
                    if (null != mStereoWallpaperIntent) {
                        //send complete msg
                        Log.i(TAG,"IncomingHandler:send comlete mesage");
                        mMainHandler.sendMessage(mMainHandler.obtainMessage(
                                MSG_SAVE_COMPLETE, mStereoWallpaperIntent));
                        //reset stereo wallpaper intent
                        mStereoWallpaperIntent = null;
                    } else {
                        Log.e(TAG,"IncomingHandler:send ERROR mesage");
                        mMainHandler.sendEmptyMessage(MSG_SHOW_SAVE_ERROR);
                    }
                    break;
                case MSG_3D_WALLPAPER_RESULT_ERROR:
                    Log.v(TAG,"IncomingHandler:Received from service: ERROR");
                    doUnbindService();
                    Log.e(TAG,"IncomingHandler:send ERROR mesage");
                    mMainHandler.sendEmptyMessage(MSG_SHOW_SAVE_ERROR);
                    //reset stereo wallpaper intent
                    mStereoWallpaperIntent = null;
                    break;
                default:
                    super.handleMessage(msg);
            }
        }
    }
    
    /**
     * Target we publish for clients to send messages to IncomingHandler.
     */
    final Messenger mMessenger = new Messenger(new IncomingHandler());
    
    /**
     * Class for interacting with the main interface of the service.
     */
    private ServiceConnection mConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className,
                IBinder service) {
            // This is called when the connection with the service has been
            // established, giving us the service object we can use to
            // interact with the service.  We are communicating with our
            // service through an IDL interface, so get a client-side
            // representation of that from the raw service object.
            mService = new Messenger(service);

            // We want to monitor the service for as long as we are
            // connected to it.
            try {
                Message msg = Message.obtain(null, MSG_SET_3D_WALLPAPER);
                msg.replyTo = mMessenger;
                msg.obj = mStereoWallpaperUri;
                Log.i(TAG,"onServiceConnected:msg.obj="+mStereoWallpaperUri);
                mService.send(msg);
            } catch (RemoteException e) {
                // In this case the service has crashed before we could even
                // do anything with it; we can count on soon being
                // disconnected (and then reconnected if it can be restarted)
                // so there is no need to do anything here.
                Log.w(TAG,"onServiceConnected:got "+e);
                return;
            }
            
            Log.d(TAG,"onServiceConnected:service connected");
        }

        public void onServiceDisconnected(ComponentName className) {
            Log.i(TAG,"onServiceDisconnected(className="+className+")");
            // This is called when the connection with the service has been
            // unexpectedly disconnected -- that is, its process crashed.
            mService = null;

            Log.v(TAG,"onServiceConnected:service disconnected");
        }
    };
    
    boolean doBindService() {
        Log.i(TAG,"doBindService()");
        // Establish a connection with the service.  We use an explicit
        // class name because there is no reason to be able to let other
        // applications replace our component.
        mIsBounded = bindService(new Intent(SET_3D_WALLPAPER), mConnection,
                                    Context.BIND_AUTO_CREATE);
        Log.d(TAG,"doBindService:mIsBounded="+mIsBounded);
        return mIsBounded;
    }
    
    void doUnbindService() {
        Log.i(TAG,"doUnbindService()");
        if (mIsBounded) {
            // Detach our existing connection.
            Log.d(TAG,"doUnbindService:call unbindService()");
            // As CropImage activity may have already stopped, 
            // we temporarily add protect. Need further check!
            try {
                unbindService(mConnection);
            } catch (IllegalArgumentException e) {
                Log.w(TAG, "doUnbindService:got exception when unbind..");
                e.printStackTrace();
            }
            mIsBounded = false;
        }
    }
    
    // M: quit CropImage when the opened file is in the unmounting storage
    private BroadcastReceiver mStorageReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            String storagePath = intent.getData().getPath();
            boolean isMediaBroadcast = Intent.ACTION_MEDIA_EJECT.equals(action) 
                    || Intent.ACTION_MEDIA_UNMOUNTED.equals(action);
            if (!isMediaBroadcast) {
                Log.w(TAG, "Storage receiver: not media broadcast!!");
                return;
            }
            Log.d(TAG, "Storage receiver: action=" + action);
            
            Uri uri = getIntent().getData();
            if (uri == null) {
                return;
            }
            if (!isLocalOrMediaUri(uri)) {
                // M: not local image, ignore this message
                return;
            }
            String filePath = mFilePath;
            if (filePath == null) {
                // M: path not ready yet; ignore this message
                return;
            }
            
            boolean isInEjectingStorage = isFileInStorage(filePath, storagePath);
            Log.d(TAG, "Storage receiver: filePath=" + filePath);
            Log.d(TAG, "Storage receiver: storagePath=" + storagePath);
            Log.d(TAG, "Storage receiver: inEjectingStorage=" + isInEjectingStorage);
            if (isInEjectingStorage) {
                Log.e(TAG, "Storage receiver: opened file in ejecting storage, quit CropImage!!");
                Toast.makeText(CropImage.this, R.string.no_storage_title, Toast.LENGTH_SHORT).show();
                CropImage.this.setResult(RESULT_CANCELED);
                CropImage.this.finish();
            }
        };
    };
    
    
    private void registerReceiverIfNeeded() {
        if (mBitmapInIntent != null || !isLocalOrMediaUri(getIntent().getData())) {
            return;
        }
        
        // M: first start the worker thread
        initWorkerThread();
        
        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_MEDIA_EJECT);
        filter.addAction(Intent.ACTION_MEDIA_UNMOUNTED);
        filter.addDataScheme("file");
        registerReceiver(mStorageReceiver, filter);
    }
    
    private void unregisterReceiverIfPossible() {
        if (mBitmapInIntent != null || 
                !isLocalOrMediaUri(getIntent().getData())) {
            return;
        }
        
        try {
            unregisterReceiver(mStorageReceiver);
        } catch (IllegalArgumentException e) {
            Log.w(TAG, "trying to unregister receiver which has not been registered yet!!", e);
        }
        
        // M: finally stops the worker thread
        stopWorkerThread();
    }
    
    private boolean isFileInStorage(String filePath, String storagePath) {
        String[] sp1 = filePath.split("/");
        String[] sp2 = storagePath.split("/");
        if (sp1 == null || sp2 == null || sp1.length < sp2.length) {
            return false;
        }
        boolean match = true;
        for (int i = 0; i < sp2.length; ++i) {
            if (!sp2[i].equalsIgnoreCase(sp1[i])) {
                match = false;
                break;
            }
        }
        
        return match;
    }
    
    private boolean checkStorageState() {
        Uri intentUri = getIntent().getData();
        if (mBitmapInIntent != null || !isLocalOrMediaUri(intentUri)) {
            return true;
        }
        String filePath = mFilePath;
        Log.d(TAG, "checkStorageState: filePath=" + filePath);
        if (filePath == null) {
            return false;
        }
        StorageManager storageManager = (StorageManager) getSystemService(Context.STORAGE_SERVICE);
        String[] storageList = storageManager.getVolumePaths();
        Log.d(TAG, "checkStorageState: storageList=" + storageList);
        if (storageList == null || storageList.length <= 0) {
            return false;
        }
        //pr 567545 by Xiang Chen begin
        if(isFileInStorage(filePath, "/data/data"))return true;
        //pr 567545 by Xiang Chen end
        for (String storagePath : storageList) {
            Log.d(TAG, "checkStorageState: storagePath=" + storagePath);
            if (isFileInStorage(filePath, storagePath)) {
                String volumeState = storageManager.getVolumeState(storagePath);
                Log.d(TAG, "checkStorageState: file in storage {" + storagePath + "}, state=" + volumeState);
                return Environment.MEDIA_MOUNTED.equals(volumeState);
            }
        }
        return false;
    }
    
    private boolean isLocalOrMediaUri(Uri uri) {
        Log.d(TAG, "isLocalUri: uri=" + uri);
        if (uri == null) {
            return false;
        }
        if (ContentResolver.SCHEME_FILE.equals(uri.getScheme())) {
            return true;
        } else if (ContentResolver.SCHEME_CONTENT.equals(uri.getScheme())
                && MediaStore.AUTHORITY.equals(uri.getAuthority())) {
            return true;
        }
        
        return false;
    }
    
    private String mFilePath;
    private void initFilePathFromIntent() {
        Uri uri = getIntent().getData();
        if (uri == null) {
            mFilePath = null;
            return;
        }
        String filePath = null;
        if (ContentResolver.SCHEME_FILE.equals(uri.getScheme())) {
            filePath = uri.getEncodedPath();
        }
        if (ContentResolver.SCHEME_CONTENT.equals(uri.getScheme())
                && MediaStore.AUTHORITY.equals(uri.getAuthority())) {
            // M: media DB uri, query DB directly
            Cursor c = null;
            try {
                c = getContentResolver().query(uri, new String[] {MediaStore.Images.ImageColumns.DATA}, null, null, null);
                if (c != null && c.moveToFirst()) {
                    filePath = c.getString(0);
                }
            } finally {
                if (c != null) {
                    c.close();
                }
            }
        }
        mFilePath = filePath;
    }
    //PR670976-chen.gui@tcl.com-for can't load the image issue begin
    /**
     * convert the media content uri from documents uri
     */
    public void convertUriIfDrm(final Context context, final Uri uri) {
        if(context == null || uri == null) {
            return;
        }
        Uri newUri = null;
        try {
            DrmManagerClient drmClient = DrmManagerClient.getInstance(context);
            if (DrmManagerClient.isMTKDrmEnable()) {
                if (drmClient.isDrm(uri)) {
                    newUri = getContentUri(context, uri);
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            return;
        }
        if (newUri != null) {
            getIntent().setData(newUri);
            Log.e(TAG, ">>>>>>>>>>>convert the uri if drm, uri = "
                    + getIntent().getData());
        }
    }
    /**
     * Get a file uri from a Uri. This will get the the uri for Storage Access
     * Framework Documents, as well as the mediaprovider_uri field for the MediaStore and
     * other file-based ContentProviders.
     */
    public static Uri getContentUri(final Context context, final Uri uri) {

        final boolean isKitKat = Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT;

        // DocumentProvider
        if (isKitKat && DocumentsContract.isDocumentUri(context, uri)) {
            // ExternalStorageProvider
            if (isExternalStorageDocument(uri)) {
//                final String docId = DocumentsContract.getDocumentId(uri);
//                final String[] split = docId.split(":");
//                final String type = split[0];
//
//                if ("primary".equalsIgnoreCase(type)) {
//                    return Uri.fromFile(new File(Environment.getExternalStorageDirectory() + "/" + split[1]));
//                }
                return uri;
            }
            // DownloadsProvider
            else if (isDownloadsDocument(uri)) {

                final String id = DocumentsContract.getDocumentId(uri);
                final Uri contentUri = ContentUris.withAppendedId(
                        Uri.parse("content://downloads/public_downloads"), Long.valueOf(id));

                return Uri.parse(getUriColumn(context, contentUri, null, null));
            }
            // MediaProvider
            else if (isMediaDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                Uri contentUri = null;
                if ("image".equals(type)) {
                    contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                } else if ("video".equals(type)) {
                    contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                } else if ("audio".equals(type)) {
                    contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                }

                final String selection = "_id=?";
                final String[] selectionArgs = new String[] {
                        split[1]
                };
                contentUri = ContentUris.withAppendedId(contentUri, Long.valueOf(split[1]));

                return contentUri;
            }
        }
        // MediaStore (and general)
        else if ("content".equalsIgnoreCase(uri.getScheme())) {
            return uri;
        }
        // File
        else if ("file".equalsIgnoreCase(uri.getScheme())) {
            return uri;
        }

        return uri;
    }

    /**
     * Get the value of the uri column for this Uri. This is useful for
     * MediaStore Uris, and other file-based ContentProviders.
     *
     * @param context The context.
     * @param uri The Uri to query.
     * @param selection (Optional) Filter used in the query.
     * @param selectionArgs (Optional) Selection arguments used in the query.
     * @return The value of the _data column, which is typically a file path.
     */
    public static String getUriColumn(Context context, Uri uri, String selection,
            String[] selectionArgs) {

        Cursor cursor = null;
        final String column = "mediaprovider_uri";
        final String[] projection = {
                column
        };

        try {
            cursor = context.getContentResolver().query(uri, projection, selection, selectionArgs,
                    null);
            if (cursor != null && cursor.moveToFirst()) {
                final int column_index = cursor.getColumnIndexOrThrow(column);
                return cursor.getString(column_index);
            }
        } finally {
            if (cursor != null)
                cursor.close();
        }
        return null;
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is ExternalStorageProvider.
     */
    public static boolean isExternalStorageDocument(Uri uri) {
        return "com.android.externalstorage.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is DownloadsProvider.
     */
    public static boolean isDownloadsDocument(Uri uri) {
        return "com.android.providers.downloads.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is MediaProvider.
     */
    public static boolean isMediaDocument(Uri uri) {
        return "com.android.providers.media.documents".equals(uri.getAuthority());
    }
    //PR670976-chen.gui@tcl.com-for can't load the image issue end

    private HandlerThread mWorker;
    private Handler mWorkerHandler;
    
    private void initWorkerThread() {
        if (mWorker != null && mWorker.isAlive()) {
            return;
        }
        mWorker = new HandlerThread("cropimage-worker");
        mWorker.start();
        mWorkerHandler = new Handler(mWorker.getLooper()) {
            @Override
            public void handleMessage(Message msg) {
                switch (msg.what) {
                case MSG_CHECK_STORAGE:
                    if (!checkStorageState()) {
                        Log.e(TAG, "MSG_CHECK_STORAGE: failed");
                        mMainHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(CropImage.this, R.string.no_storage_title, Toast.LENGTH_SHORT).show();
                                CropImage.this.setResult(RESULT_CANCELED);
                                CropImage.this.finish();
                            }
                        });
                    }
                    break;
                }
            }
        };
    }
    
    private void stopWorkerThread() {
        if (mWorker == null || !mWorker.isAlive()) {
            return;
        }
        mWorker.quit();
        mWorkerHandler = null;
    }
    
    

}
