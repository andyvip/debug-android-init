
package com.jrdcom.example.joinpic;

import java.io.File;
import  java.util.*;
import java.util.concurrent.locks.Lock;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.jrdcom.android.gallery3d.R;
import com.jrdcom.example.joinpic.FreePuzzleLayoutView;
import com.jrdcom.example.joinpic.FreePuzzleLayoutView.OnMeasureCompleteListener;
import com.jrdcom.mt.mtxx.tools.BitmapOperate;
import com.jrdcom.example.joinpic.FreePuzzleModel;
import com.jrdcom.example.joinpic.Global;
import com.mt.mtxx.image.JNI;
import com.jrdcom.example.layout.FreePuzzleLayout;
import android.graphics.BitmapFactory.Options;
import com.jrdcom.mt.core.BitmapUtil;
import com.jrdcom.example.layout.FreePuzzleLayoutItem;


public class ActivityFreedom extends Activity implements OnClickListener 
,OnMeasureCompleteListener
{
    private ProgressDialog progressDialog;
    List<ImageObject> freePuzzleItemList = new ArrayList<ImageObject>();
    /*******Model*************/
    // service logic processing layer model object
    private FreePuzzleModel model = null;
    /** the example meterial of the format and backgound **/
    private String mCurrentStytle = "freedom/002-001.zypt";
    private String mCurrentBgImage = "freedom/1.jpg";
    /** save path */
    private String mSavePath = Utils.getDefaultPath() + "/Pictures/freedom";
    private String mPathString = null;
    
    
    /**NDK cache path**/
    private final String TMP_PATH= "/mnt/sdcard/MTXX/puzzle";
    private ArrayList<String> imagePathList;
    public final int PROGRESS_DIALOG = 1;
    public final int SAVE_PROGRESS_DIALOG = 2;
    public final int MSG_LOADING_SUCCESS = 3;
    public final int MSG_DIMSS_DIALOG = 5;
    
    private int styleIndex = 0;
    private int currentStyleIndex = 0;
    private float scale = 0.675f;
    private boolean isLocked = false;//550162 added by xiaowei.xu
    
    /****different format styles*****/
    private static final String[][] formatStyles={
        {"002-001.zypt","002-002.zypt","002-003.zypt","002-004.zypt","002-005.zypt","002-006.zypt"},//2
        {"003-001.zypt","003-002.zypt","003-003.zypt","003-004.zypt","003-005.zypt","003-006.zypt"},//3
        {"004-001.zypt","004-002.zypt","004-003.zypt","004-004.zypt","004-005.zypt","004-006.zypt"},//4
        {"005-001.zypt","005-002.zypt","005-003.zypt","005-004.zypt","005-005.zypt","005-006.zypt"},//5
        {"006-001.zypt","006-002.zypt","006-003.zypt","006-004.zypt","006-005.zypt","006-006.zypt"},//6
        {"007-001.zypt","007-002.zypt","007-003.zypt","007-004.zypt","007-005.zypt","007-006.zypt"},//7
        {"008-001.zypt","008-002.zypt","008-003.zypt","008-004.zypt","008-005.zypt","008-006.zypt"},//8
        {"009-001.zypt","009-002.zypt","009-003.zypt","009-004.zypt","009-005.zypt","009-006.zypt"},//9
    };
    
    private Button changeBg;
    private Button addorDelete;
    private Button btn_return;
    private Button btn_save;
    private Button btn_pintu_laststyle;
    private Button btn_pintu_nextstyle;
    private AlertDialog dialog;

    private Bitmap bitmap_free;
    private FreePuzzleLayoutView freePuzzlelayout;
    private ProgressDialog mDialog;
    private FreePuzzleLayout layout;
    private int parentWidth;
    private Context mContext;
    private final String SAVE_PATH= "sdcard/Pictures";
    
    public static final int FLAG_CHANGE_BG = 100;
    
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        createDialog();
        createProgressDialog();
        this.setContentView(R.layout.activity_freedom);
        mContext = getApplicationContext();
//        imagePathList = HAblum_Main.pathlist;
        imagePathList=HAblum_Main.pathlist;
        styleIndex = imagePathList.size() - 2;
        mCurrentStytle = "freedom/"+formatStyles[styleIndex][currentStyleIndex];
        init();
        initFreePuzzleMode();
    }

    @Override
    protected void onResume() {
        // TODO Auto-generated method stub
        super.onResume();
    }

    public void init() {
        changeBg = (Button) findViewById(R.id.btn_pintu_changeFrame);
        addorDelete = (Button) findViewById(R.id.btn_pintu_addOrDelete);
        btn_return = (Button) findViewById(R.id.btn_pintu_return);
        btn_save = (Button) findViewById(R.id.btn_pintu_save);
        btn_pintu_laststyle = (Button)findViewById(R.id.btn_pintu_laststyle);
        btn_pintu_nextstyle = (Button)findViewById(R.id.btn_pintu_nextstyle);
        
        btn_return.setOnClickListener(this);
        btn_save.setOnClickListener(this);
        changeBg.setOnClickListener(this);
        addorDelete.setOnClickListener(this);
        btn_pintu_laststyle.setOnClickListener(this);
        btn_pintu_nextstyle.setOnClickListener(this);
        
        freePuzzlelayout = (FreePuzzleLayoutView)findViewById(R.id.freepuzzle_view);
        freePuzzlelayout.setOnMeasureCompleteListener(this);
        mDialog = new ProgressDialog(this);
    }
    /**
     * @author hyg
     * To init service logic processing object
     */
    public void initFreePuzzleMode()
    {
        JNI lJni=new JNI();
        model = new FreePuzzleModel();
        model.init(lJni);
        model.setNDKPuzzleTempPath(TMP_PATH);     
        Global.setModelSaver(model);
        model.setImagePathList(imagePathList);
        model.setLayoutView(freePuzzlelayout);
        
        switchBg();
    }
    
    private void switchStytle() {
        //added by yaogang.hao optimize loading speed
//        model.setFreePuzzleLayout(this, mCurrentStytle, false);
        //show view after switching style
//        freePuzzlelayout.relayout();
        scale = freePuzzlelayout.getScaleValue();
//        layout = model.getFreePuzzleLayoutObject();
//        for(int i=0;i<model.mListImagePath.size();i++)
//        {
//            FreePuzzleLayoutItem lLayoutItem = layout.getItem(i);
//            
//            lLayoutItem.setScaleX(scale);
//            lLayoutItem.setScreenScale(scale);
//            float x = lLayoutItem.getScreenX();
//            float y = lLayoutItem.getScreenY();
//            float rotation = lLayoutItem.getRotation();
//            
//            FreePuzzleItemView tempitem = new FreePuzzleItemView(this,i);
//            Bitmap lBitmap = BitmapUtil.loadBitmapFromSDcard(model.mListImagePath.get(i), true);
//            tempitem.setBitmap(lBitmap);
//            tempitem.setStyle((lBitmap.getWidth()/2+x),(lBitmap.getHeight()/2+y),scale,rotation);
//            tempitem.setOnTouchesListener(freePuzzlelayout);
//            freePuzzlelayout.addInScreen(tempitem,0,0);
//        }
        
        
        new Thread(new Runnable() {
            @Override
            public void run() {
                model.setFreePuzzleLayout(ActivityFreedom.this, mCurrentStytle, false);
                layout = model.getFreePuzzleLayoutObject();
                synchronized(freePuzzleItemList)
                {
                    freePuzzleItemList.clear();
                    ImageObject imageobject = null;
                    for(int i=0;i<model.mListImagePath.size();i++)
                    {
                        FreePuzzleLayoutItem lLayoutItem = layout.getItem(i);
                        lLayoutItem.setScaleX(scale);
                        lLayoutItem.setScreenScale(scale);
                        float x = lLayoutItem.getScreenX();
                        float y = lLayoutItem.getScreenY();
                        float rotation = lLayoutItem.getRotation();
                        Bitmap lBitmap = BitmapUtil.loadBitmapFromSDcard(model.mListImagePath.get(i), true);
                        imageobject = new ImageObject(x,y,rotation,lBitmap);
                        freePuzzleItemList.add(imageobject);
                    }
                    
                    myhandler.sendEmptyMessage(MSG_LOADING_SUCCESS);
                    //pr 550162 by xiangchen
                    myhandler.sendEmptyMessage(MSG_DIMSS_DIALOG);
                }
            }
        }).start();
    }
    private void switchBg() {
        Bitmap bgBitmap = BitmapOperate.LoadAssertsPic(mCurrentBgImage,
                getResources().getAssets());
        model.setCustomBackgroundBitmap(bgBitmap);
    }
    
    public Bitmap getBitmap(String img_path)
    {
        BitmapFactory.Options opt = new BitmapFactory.Options();
        opt.inPreferredConfig = Bitmap.Config.RGB_565;
        opt.inPurgeable = true;
        opt.inInputShareable = true;
        opt.inJustDecodeBounds = false;
        opt.inSampleSize = 10;
        
        Bitmap bitmap = BitmapFactory.decodeFile(img_path, opt);
        
        return bitmap;
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
        // TODO Auto-generated method stub

        switch (v.getId()) {
            case R.id.btn_pintu_changeFrame:
                Intent intent = new Intent();
                intent.putStringArrayListExtra("change_bg", imagePathList);
                intent.setClass(ActivityFreedom.this, ActivityTemplateSetBg.class);
                startActivityForResult(intent,FLAG_CHANGE_BG);
                break;
            case R.id.btn_pintu_addOrDelete:
                Intent intent_addordelete = new Intent();
                intent_addordelete.putStringArrayListExtra("go_back_list",imagePathList);
                intent_addordelete.setClass(ActivityFreedom.this, HAblum_Main.class);
                intent_addordelete.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                startActivity(intent_addordelete);

                //PR865251 modify gallery show error we support one instance by fengke at 2014.12.8 start
                finish();
                //PR865251 modify gallery show error we support one instance by fengke at 2014.12.8 end

                break;
            // add by hui.xu@2013/2/20 begin
            case R.id.btn_pintu_return:
                Intent go_back = new Intent();
                go_back.setClass(ActivityFreedom.this, HAblum_Main.class);
                go_back.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                startActivity(go_back);
                finish();
                break;
            case R.id.btn_pintu_save:
            {
                //PR651227-tao li-begin 001
                if (!Utils.hasAvailableSpace(Utils.getDefaultPath())) {
                  Utils.showToast(ActivityFreedom.this, R.string.not_enough_storage);
                      break;
                }
                //PR651227-tao li-end 001
                //PR939610 the save dir should be associate with default storage path modify by fengke at 2015.03.07 start
                mSaveDir = Utils.getDefaultPath() + "/Pictures/";//fengke change
                mDialog.setMessage(mContext.getString(R.string.save_saving)+"\n"+
                        mContext.getString(R.string.save_path) + Utils.getDescriptionPath(mContext,mSaveDir));
                //PR939610 the save dir should be associate with default storage path modify by fengke at 2015.03.07 end
                mDialog.show();
                new Thread() {
                    @Override
                    public void run() {
                        try {
                            FreePuzzleLayout layout = model.getFreePuzzleLayoutObject();
                            for(int i=0;i<freePuzzlelayout.getChildCount();i++)
                            {
                                FreePuzzleItemView tempitem = (FreePuzzleItemView)freePuzzlelayout.getChildAt(i);
                                int k = tempitem.getLayoutItemIndex();
                                model.setLids(i, k);
                                layout.getItem(i).setX(tempitem.getLastPoint().x/scale);
                                layout.getItem(i).setY(tempitem.getLastPoint().y/scale);
                                layout.getItem(i).setScaleX(tempitem.getScale()/scale);
                                layout.getItem(i).setRotation(tempitem.getRotation());
                            }
                            //PR939610 the save dir should be associate with default storage path modify by fengke at 2015.03.07 start
                            mSavePath = mSaveDir + "freedom";
                            //mSavePath = Utils.getDefaultPath() + "/Pictures/freedom";// xiaodaijun PR675235 add
                            //PR939610 the save dir should be associate with default storage path modify by fengke at 2015.03.07 end
                            mPathString = getSavedPicName(mSavePath);
                            createSDDir(mSaveDir);//PR630752-tao li-001
                            model.saveDataToPath(mPathString);
                            scanDirAsync();
                        } catch (Exception e) {
                        } finally {
                            myhandler.sendEmptyMessage(SAVE_PROGRESS_DIALOG);
                        }
                    }
                }.start();
                break;
            }
            case R.id.btn_pintu_laststyle:
            {        
            	//pr 550162 by xiaowei.xu begin
            	if(isLocked){
            		return ;
            	}
            	//pr 550162 by xiaowei.xu end
                currentStyleIndex--;
                if(currentStyleIndex < 0 )
                {
                    currentStyleIndex = 4;
                }
                mCurrentStytle = "freedom/"+formatStyles[styleIndex][currentStyleIndex];
                showLoadDialog(); //pr 550162 by xiangchen
                switchStytle();
                break;
            }
            case R.id.btn_pintu_nextstyle:
            {
            	//pr 550162 by xiaowei.xu begin
            	if (isLocked) {
            		return;					
				}
            	//pr 550162 by xiaowei.xu end
                currentStyleIndex++;
                if(currentStyleIndex > 4 )
                {
                    currentStyleIndex = 0;
                }
                mCurrentStytle =  "freedom/"+formatStyles[styleIndex][currentStyleIndex];
                showLoadDialog(); //pr 550162 by xiangchen
                switchStytle();
                break;
            }    
            // ad by hui.xu@2013/2/20 end
            default:
                break;
        }

    }
    
    /**
     * pr 550162 by xiangchen
     */
    public void showLoadDialog(){
    	isLocked = true;//pr 550162 added by xiaowei.xu 
    	mDialog.setMessage(mContext.getString(R.string.loading));
    	mDialog.setCancelable(false);
    	mDialog.show();
    }
    
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode != RESULT_OK)
            return;
        if(FLAG_CHANGE_BG == requestCode)
        {
            
            mCurrentBgImage = data.getStringExtra("imagepath");
            switchBg();
            
        }
    }

    //PR857266 modify for force close during saving by fengke at 2014.12.1 start
    @Override
    protected void onPause () {
        super.onPause();
        if ((mDialog != null) && mDialog.isShowing()) {
            mDialog.dismiss();
        }
    }
    //PR857266 modify for force close during saving by fengke at 2014.12.1 end

    private Handler myhandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            // TODO Auto-generated method stub
            super.handleMessage(msg);
            switch (msg.what) {
                case PROGRESS_DIALOG:
                    dialog.dismiss();
                    break;
                case MSG_DIMSS_DIALOG:
                    //PR857266 modify for force close during saving by fengke at 2014.12.1 start
                    if ((mDialog != null) && mDialog.isShowing()) {
                        mDialog.dismiss();
                    }
                    //PR857266 modify for force close during saving by fengke at 2014.12.1 end
                	isLocked = false;
                    break;
                case SAVE_PROGRESS_DIALOG:
                    //PR857266 modify for force close during saving by fengke at 2014.12.1 start
                    if ((mDialog != null) && mDialog.isShowing()) {
                        mDialog.dismiss();
                    }
                    //PR857266 modify for force close during saving by fengke at 2014.12.1 end
                    Toast.makeText(ActivityFreedom.this, ActivityFreedom.this.getString(R.string.save_success), Toast.LENGTH_SHORT).show();
                    break;
                case MSG_LOADING_SUCCESS:
                    {
                        //added by yaogang.hao optimize loading speed
                        synchronized(freePuzzleItemList)
                        {
                            freePuzzlelayout.relayout();
                            ImageObject imageobject = null;
                            for(int i=0;i<freePuzzleItemList.size();i++)
                            {
                                imageobject = freePuzzleItemList.get(i);
                              FreePuzzleItemView tempitem = new FreePuzzleItemView(ActivityFreedom.this,i);
                              tempitem.setBitmap(imageobject.getBitmap());
                              //PR663162-taoli-begin 001
                              tempitem.setStyle((imageobject.getBitmap().getWidth()/4+imageobject.getX()),(imageobject.getBitmap().getHeight()/4+imageobject.getY()),scale,imageobject.getRotation());
                              //PR663162-taoli-end 001
                              tempitem.setOnTouchesListener(freePuzzlelayout);
                              freePuzzlelayout.addInScreen(tempitem,0,0);
                            }
                            freePuzzleItemList.clear();
                        }
                        if(progressDialog.isShowing())
                            progressDialog.dismiss();
                        break;
                    }
                default:
                    break;
            }
        }
    };
    
    private void scanDirAsync(){
        Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        File f = new File(mPathString);
        Uri contentUri = Uri.fromFile(f);
        mediaScanIntent.setData(contentUri);
        ActivityFreedom.this.sendBroadcast(mediaScanIntent);
    }

    public void createDialog() {
        View view = LayoutInflater.from(this).inflate(
                R.layout.mtprogress_dialog_view, null);
        TextView textview = (TextView) view.findViewById(R.id.txt_progress);
        textview.setText("loading .......");
        dialog = new AlertDialog.Builder(ActivityFreedom.this)
                .setTitle("wait a moment").setIcon(android.R.drawable.ic_dialog_alert)
                .setView(view).create();
        dialog.show();
    }
    
    public String getSavedPicName(String path)
    {
        return path+"_"+System.currentTimeMillis()+".jpg";
    }
    @Override
    public void onMeasureComplete()
    {
        progressDialog.show();
        switchStytle();
    }
    public void createProgressDialog()
    {
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage(getString(R.string.tip_please_wait_content));
        progressDialog.setIndeterminate(true);
        progressDialog.setCancelable(false);
    }
    @Override
    public void onBackPressed() {
        Intent go_back = new Intent();
        go_back.setClass(ActivityFreedom.this, HAblum_Main.class);
        go_back.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        startActivity(go_back);
        finish();
    }
    
    private class ImageObject
    {
        private float mx;
        private float my;
        private Bitmap mbitmap;
        private float mrotation;
        
        public ImageObject(float x,float y,float rotation,Bitmap bitmap)
        {
            this.mx = x;
            this.my = y;
            this.mbitmap = bitmap;
            this.mrotation = rotation;
        }
        
        public float getX()
        {
            return this.mx;
        }
        public float getY()
        {
            return this.my;
        }
        public float getRotation()
        {
            return this.mrotation;
        }
        public Bitmap getBitmap()
        {
            return this.mbitmap;
        }
    }
}
