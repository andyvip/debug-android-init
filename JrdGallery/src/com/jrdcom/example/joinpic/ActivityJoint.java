
package com.jrdcom.example.joinpic;

import java.io.File;
import java.util.ArrayList;
import java.util.Vector;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.Toast;

import com.jrdcom.android.gallery3d.R;
import com.jrdcom.mt.util.MyData;
import com.jrdcom.mt.core.BitmapUtil;
import com.jrdcom.mt.core.BitmapUtil;
import com.jrdcom.example.joinpic.Global;
import com.jrdcom.example.joinpic.JointPuzzleModel;
import com.mt.mtxx.image.JNI;
import  android.util.DisplayMetrics;
import static com.jrdcom.example.joinpic.JointPuzzleModel.JOINT_TEMPLATE_WIDTH;
import com.jrdcom.mt.util.*;
import java.util.List;

public class ActivityJoint extends Activity implements OnClickListener

{
    private ProgressDialog progressDialog;
    private List<Bitmap>  bitmaplist = new ArrayList<Bitmap>();
    private Vector<Bitmap> m_vecTexture = new Vector<Bitmap>(); // 边框的列表
    private int mFrameCount;
    private String METERIALS_PATH ="";
    private String mCurrentBiankuang[] = {
            "/Biankuang/001.ptbj",
            "/Biankuang/002.ptbj",
            "/Biankuang/003.ptbj",
            "/Biankuang/004.ptbj",
            "/Biankuang/005.ptbj",
            "/Biankuang/006.ptbj",
            "/Biankuang/007.ptbj",
            "/Biankuang/008.ptbj",
            "/Biankuang/009.ptbj",
            "/Biankuang/010.ptbj",
            "/Biankuang/010.ptbj",
            "/Biankuang/002.ptbj",
            "/Biankuang/003.ptbj",
            "/Biankuang/002.ptbj",
            "/Biankuang/001.ptbj",
            "/Biankuang/001.ptbj",
            "/Biankuang/010.ptbj",
            "/Biankuang/004.ptbj",
            "/Biankuang/009.ptbj",
            "/Biankuang/009.ptbj",
            "/Biankuang/002.ptbj",
            "/Biankuang/007.ptbj",
            "/Biankuang/008.ptbj",
            "/Biankuang/009.ptbj",
            "/Biankuang/004.ptbj",
            "/Biankuang/005.ptbj",
            "/Biankuang/006.ptbj"
            };
    private String mCurrentDiwen[]={
            "/Diwen/pat000.diwen",
            "/Diwen/pat000.diwen",
            "/Diwen/pat000.diwen",
            "/Diwen/pat000.diwen",
            "/Diwen/pat000.diwen",
            "/Diwen/pat000.diwen",
            "/Diwen/pat000.diwen",
            "/Diwen/pat000.diwen",
            "/Diwen/pat000.diwen",
            "/Diwen/pat000.diwen",
            "/Diwen/pat001.diwen",
            "/Diwen/pat002.diwen",
            "/Diwen/pat003.diwen",
            "/Diwen/pat004.diwen",
            "/Diwen/pat005.diwen",
            "/Diwen/pat006.diwen",
            "/Diwen/pat007.diwen",
            "/Diwen/pat008.diwen",
            "/Diwen/pat009.diwen",
            "/Diwen/pat010.diwen",
            "/Diwen/pat011.diwen",
            "/Diwen/pat012.diwen",
            "/Diwen/pat013.diwen",
            "/Diwen/pat014.diwen",
            "/Diwen/pat015.diwen",
            "/Diwen/pat016.diwen",
            "/Diwen/pat017.diwen"
    };
    
    private String mSavePath=Utils.getDefaultPath() + "/Pictures/joint";
    private String mPathString = null;
    private final String TMP_PATH= "/mnt/sdcard/puzzle";
    private JointPuzzleModel model;
    public static  int JOINT_TEMPLATE_WIDTH = 480;
    public static final int MARGIN = 40;
    int widthpixel ;
    public static final int JOINT_ACTIVITY = 100; 
    private int mId = 0;
    private int mType = 1;
    private boolean isinitjni = false;
    
    private JNI lJni;
    
    private Button btn_changeFrame;
    private Button btn_addordelete;
    private Button btn_replacepic;
    private Button btn_saveButton;
    private Button btn_backButon;
    
    private ImageButton btn_rightRotate;
    private ImageButton btn_horizontal;
    private ImageButton btn_vertical;
    private Context mContext;
    private final String SAVE_PATH= "sdcard/Pictures";
    
    //yaogang.hao for PR 539491
    private int screenWidth;
    private int screenHeight;
    
    // ScrollView scrollview;
    private ArrayList<String> imageList = new ArrayList<String>();
    private JointPuzzleLayoutView jointPuzzleLayoutView;
    private final int MSG_SAVE_SUCCESS = 1;
    private final int MSG_LOAD_SUCCESS = 2;
    
    private ProgressDialog mDialog;
    Handler wordHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch(msg.what)
            {
                case MSG_SAVE_SUCCESS:
                {
                    //PR857266 modify for force close during saving by fengke at 2014.12.1 start
                    if ((mDialog != null) && mDialog.isShowing()) {
                        mDialog.dismiss();
                    }
                    //PR857266 modify for force close during saving by fengke at 2014.12.1 end
                    Toast.makeText(ActivityJoint.this, ActivityJoint.this.getString(R.string.save_success), Toast.LENGTH_SHORT).show();
                    break;
                }
                case MSG_LOAD_SUCCESS:
                {
                    ImageView view2 = null;
                    RelativeLayout.LayoutParams localLayoutParams = new RelativeLayout.LayoutParams(
                            LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
                    for(int i=0;i<bitmaplist.size();i++)
                    {
                          view2 = new ImageView(ActivityJoint.this);
                          view2.setImageBitmap(bitmaplist.get(i));
                          view2.setLayoutParams(localLayoutParams);
                          jointPuzzleLayoutView.addView(view2, i, localLayoutParams);
                    }
                    if(progressDialog.isShowing())
                    {
                        progressDialog.dismiss();
                    }
                    showView();
                    bitmaplist.clear();
                    break;
                }
            }
        }
    };

    //PR857266 modify for force close during saving by fengke at 2014.12.1 start
    @Override
    protected void onPause () {
        super.onPause();
        if ((mDialog != null) && mDialog.isShowing()) {
            mDialog.dismiss();
        }
    }
    //PR857266 modify for force close during saving by fengke at 2014.12.1 end

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.activity_joint);
        mContext = getApplicationContext();
        METERIALS_PATH = FileUtils.getAbsolutePathOnExternalStorage(this,"resource/puzzle");
        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);
        widthpixel = metrics.widthPixels;
        JOINT_TEMPLATE_WIDTH = widthpixel-MARGIN*2;
        createDialog();
        init();
        initMode();
        //yaogang.hao for PR 539491
         DisplayMetrics  dm = new DisplayMetrics();  
         getWindowManager().getDefaultDisplay().getMetrics(dm);
         screenWidth = dm.widthPixels;
         screenHeight = dm.heightPixels;
    }

    public void init() {
        
        btn_changeFrame = (Button) findViewById(R.id.btn_pintu_changeFrame);
        btn_addordelete = (Button) findViewById(R.id.btn_pintu_addOrDelete);
        btn_replacepic = (Button) findViewById(R.id.template_replacepic);
        btn_rightRotate = (ImageButton) findViewById(R.id.template_rightrotate);
        btn_horizontal = (ImageButton) findViewById(R.id.template_vertical);
        btn_saveButton = (Button) findViewById(R.id.btn_pintu_save);
        btn_backButon = (Button) findViewById(R.id.btn_pintu_return);
        
        btn_saveButton.setOnClickListener(this);
        btn_backButon.setOnClickListener(this);
        btn_changeFrame.setOnClickListener(this);
        btn_addordelete.setOnClickListener(this);
        
        mDialog= new ProgressDialog(this);
        jointPuzzleLayoutView = (JointPuzzleLayoutView)findViewById(R.id.joint_layoutview);
//        for (int i = 0; i < HAblum_Main.pathlist.size(); i++)
//        {
//            BitmapFactory.Options options = new BitmapFactory.Options();
//            options.inJustDecodeBounds = true;
//            Bitmap roundBitmap = BitmapFactory.decodeFile(HAblum_Main.pathlist.get(i), options);
//            int realWidth = options.outWidth;
//            int realHeight = options.outHeight;
//            int preHeight=(int)(realHeight*(JOINT_TEMPLATE_WIDTH/(realWidth*1.0f)));
//            Bitmap temp = BitmapFactory.decodeFile(HAblum_Main.pathlist.get(i));
//            float scale = preHeight/(realHeight*1.0f);//JOINT_TEMPLATE_WIDTH/(options.outWidth*1.0f);
//            Matrix matrix = new Matrix();
//            matrix.postScale(scale, scale);
//            temp = Bitmap.createBitmap(temp, 0, 0, options.outWidth, options.outHeight, matrix,true);
//            ImageView view2 = new ImageView(this);
//            view2.setImageBitmap(temp);
//            RelativeLayout.LayoutParams localLayoutParams = new RelativeLayout.LayoutParams(
//                    LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
//            view2.setLayoutParams(localLayoutParams);
//            jointPuzzleLayoutView.addView(view2, i, localLayoutParams);
//        }
    }
    public void initMode()
    {
        lJni=new JNI();
        model=new JointPuzzleModel();
        model.init(lJni);
        model.setNDKPuzzleTempPath(TMP_PATH);
        Global.setModelSaver(model);
        model.setImagePathList(HAblum_Main.pathlist);
        /**加载图片数据，预先计算出拼图区域的大小 **/
        model.setWidth(widthpixel);
        model.initFrame(mCurrentBiankuang[0], mCurrentDiwen[0]);
        if(!progressDialog.isShowing())
            progressDialog.show();
        new Thread(
              new Runnable() {
                  @Override
                  public void run() {
                      bitmaplist.clear();
                      for (int i = 0; i < HAblum_Main.pathlist.size(); i++)
                      {
                          BitmapFactory.Options options = new BitmapFactory.Options();
                          options.inJustDecodeBounds = true;
                          //yaogang.hao for PR511292
                          //yaogang.hao for PR539491 After fota upgrade stitching is very vague begin
                          
                          //1:for calculate sample size
                          BitmapFactory.decodeFile(HAblum_Main.pathlist.get(i), options);
                          int realWidth = options.outWidth;
                          int realHeight = options.outHeight;
                          options.inSampleSize=computeSampleSize(options,-1,600*800);
                          
                          //2:measure the width after sample size
                          BitmapFactory.decodeFile(HAblum_Main.pathlist.get(i), options);
                          realWidth = options.outWidth;
                          realHeight = options.outHeight;
                          int preHeight=(int)(realHeight*(JOINT_TEMPLATE_WIDTH/(realWidth*1.0f)));
                          //yaogang.hao for PR511292
                          
                          //3:to scale it fit window with above width
                          options.inJustDecodeBounds = false;
                          //yaogang.hao for PR  533428
                          options.inInputShareable = true;
                          options.inPurgeable = true;
                          
                          Bitmap temp = BitmapFactory.decodeFile(HAblum_Main.pathlist.get(i),options);
                          float scale = preHeight/(realHeight*1.0f);//JOINT_TEMPLATE_WIDTH/(options.outWidth*1.0f);
                          Matrix matrix = new Matrix();
                          matrix.postScale(scale, scale);
                          temp = Bitmap.createBitmap(temp, 0, 0, options.outWidth, options.outHeight, matrix,true);
                          bitmaplist.add(temp);
                        //yaogang.hao for PR539491 After fota upgrade stitching is very vague begin
                      } 
                      Message msg = new Message();
                      msg.what = MSG_LOAD_SUCCESS;
                      wordHandler.sendMessage(msg);
                  }
                  }
          ).start();
          
    }
    private void switchStytle(int m) {
        model.setStytle(METERIALS_PATH+mCurrentBiankuang[m],METERIALS_PATH+mCurrentDiwen[m]);
    }
    private void showView()
    {
        ArrayList<Bitmap> tempArrayList = model.getFrameTexture();
        if(null != tempArrayList)
        {
            jointPuzzleLayoutView.setPuzzleTexture(tempArrayList);
            jointPuzzleLayoutView.invalidate();
        }
    }
    //PR630752-tao li-begin 001
    //PR939610 the save dir should be associate with default storage path modify by fengke at 2015.03.07 start
    private String mSaveDir = Utils.getDefaultPath() + "/Pictures/";
    //PR939610 the save dir should be associate with default storage path modify by fengke at 2015.03.07 end
    public File createSDDir(String dirName) {
        File dir = new File(dirName);
        if (!dir.exists()) {
            dir.mkdir();
        }
        return dir;
    }
    //PR630752-tao li-end 001
    @Override
    public void onClick(View v) {
        
        switch (v.getId()) {
            case R.id.btn_pintu_addOrDelete:
                Intent intent_addordelete = new Intent();
                intent_addordelete.setClass(ActivityJoint.this, HAblum_Main.class);
                intent_addordelete.putStringArrayListExtra("go_back_list",HAblum_Main.pathlist);
                intent_addordelete.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                startActivity(intent_addordelete);
                finish();
                break;
            case R.id.btn_pintu_changeFrame:
                Intent intent = new Intent();
                intent.setClass(ActivityJoint.this, ActivityTemplateSetStyle.class);
                startActivityForResult(intent, JOINT_ACTIVITY);
                break;
            case R.id.btn_pintu_save:
                {
                    //PR651227-tao li-begin 001
                    if (!Utils.hasAvailableSpace(Utils.getDefaultPath())) {
                      Utils.showToast(ActivityJoint.this, R.string.not_enough_storage);
                          break;
                    }
                    //PR651227-tao li-end 001
                    //PR939610 the save dir should be associate with default storage path modify by fengke at 2015.03.07 start
                    mSaveDir = Utils.getDefaultPath() + "/Pictures/";
                    mDialog.setMessage(mContext.getString(R.string.save_saving)+"\n"+
                            mContext.getString(R.string.save_path)+Utils.getDescriptionPath(mContext,mSaveDir));
                    //PR939610 the save dir should be associate with default storage path modify by fengke at 2015.03.07 end
                    mDialog.show();
                    new Thread() {
                        @Override
                        public void run() {
                            try {
                                //PR939610 the save dir should be associate with default storage path modify by fengke at 2015.03.07 start
                                mSavePath = mSaveDir + "joint";
                                //mSavePath = Utils.getDefaultPath() + "/Pictures/joint";// xiaodaijun PR675235 add
                                //PR939610 the save dir should be associate with default storage path modify by fengke at 2015.03.07 end
                                mPathString = getSavedPicName(mSavePath);
                                createSDDir(mSaveDir);//PR630752-tao li-001
                                model.saveDataToPath(mPathString);
                                scanDirAsync();
                            } catch (Exception e) {
                            } finally {
                                Message msg = new Message();
                                msg.what = MSG_SAVE_SUCCESS;
                                wordHandler.sendMessage(msg);
                            }
                        }
                    }.start();
                    
                    break;
                }
            case R.id.btn_pintu_return:
                {
                    Intent go_back = new Intent();
                    go_back.setClass(ActivityJoint.this, HAblum_Main.class);
                    go_back.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);//add by biao.luo for PR520298
                    startActivity(go_back);
                    finish();
                    break;
                }
        }

    }
    @Override
    public void onBackPressed() {
        Intent go_back = new Intent();
        go_back.setClass(ActivityJoint.this, HAblum_Main.class);
        //PR-513291 added by Xioawei.Xu begin
        go_back.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        //PR-513291 added by Xioawei.Xu end
        startActivity(go_back);
        finish();
    }
    private void scanDirAsync(){
        Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        File f = new File(mPathString);
        Uri contentUri = Uri.fromFile(f);
        mediaScanIntent.setData(contentUri);
        ActivityJoint.this.sendBroadcast(mediaScanIntent);
    }
    
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode != RESULT_OK)
            return;

        if (requestCode == JOINT_ACTIVITY) {
            mId = data.getIntExtra("id", 0);
            switchStytle(mId);
            showView();
        }
        super.onActivityResult(requestCode, resultCode, data);
    }
    
    public String getSavedPicName(String path)
    {
        return path+"_"+System.currentTimeMillis()+".jpg";
    }
    @Override
    protected void onResume ()
    {
        super.onResume();
        model.initFrame(METERIALS_PATH+mCurrentBiankuang[mId], METERIALS_PATH+mCurrentDiwen[mId]);
    }
    public void createDialog()
    {
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage(getString(R.string.tip_please_wait_content));
        progressDialog.setIndeterminate(true);
        progressDialog.setCancelable(false);
    }
    
    //yaogang.hao for PR539491 After fota upgrade stitching is very vague begin
    public  int computeSampleSize(BitmapFactory.Options options,
            int minSideLength, int maxNumOfPixels) {
        int initialSize = computeInitialSampleSize(options, minSideLength,
                maxNumOfPixels);
        int roundedSize;
        if (initialSize <= 8) {
            roundedSize = 1;
            while (roundedSize < initialSize) {
                roundedSize <<= 1;
            }
        } else {
            roundedSize = (initialSize + 7) / 8 * 8;
        }
        return roundedSize;
    }
    private  int computeInitialSampleSize(BitmapFactory.Options options,
            int minSideLength, int maxNumOfPixels) {
        double w = options.outWidth;
        double h = options.outHeight;
        int lowerBound = (maxNumOfPixels == -1) ? 1 :
                (int) Math.ceil(Math.sqrt(w * h / maxNumOfPixels));
        int upperBound = (minSideLength == -1) ? 128 :
                (int) Math.min(Math.floor(w / minSideLength),
                Math.floor(h / minSideLength));
        if (upperBound < lowerBound) {
            // return the larger one when there is no overlapping zone.
            return lowerBound;
        }
        if ((maxNumOfPixels == -1) &&
                (minSideLength == -1)) {
            return 1;
        } else if (minSideLength == -1) {
            return lowerBound;
        } else {
            return upperBound;
        }
    } 
    //yaogang.hao for PR539491 After fota upgrade stitching is very vague end
}
