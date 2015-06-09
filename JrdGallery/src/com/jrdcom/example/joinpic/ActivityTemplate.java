package com.jrdcom.example.joinpic;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.jrdcom.android.gallery3d.R;
import com.jrdcom.example.layout.TemplateTool;
import com.jrdcom.mt.core.BitmapUtil;
import com.mt.mtxx.image.JNI;
import com.jrdcom.example.joinpic.TemplateViewGroup.OnMeasureCompleteListener;
import com.jrdcom.mt.util.*;


public class ActivityTemplate extends Activity implements OnClickListener,
		OnMeasureCompleteListener {
    private ProgressDialog progressDialog;
    ArrayList<Bitmap> tempArrayList;
    private Button btn_return;
    private ImageView picView;
    private ImageView maskView;
    private Button changeFrame;
    private Button addorDelete;
    private ImageButton closePicEdit;
    private Button replacepic;
    private ImageButton rightRotate;
    private ImageButton horizontal_ibtn;
    private ImageButton vertical_ibtn;
    private Button laststyle;
    private Button nextStyle;
    private Button btn_change_layout;
    private Button btn_close;
    private ArrayList<String> imagePathList;
    private Context mContext;
    private Button btn_pintu_nextstyle;
    private Button btn_pintu_laststyle;
    private Button mSavaPintuButton;
    private TextView tvw_pintu_top_title;
    private RelativeLayout mRelativeLayout;
    private LinearLayout mLinearLayout;
    private HorizontalListView mHorizontalListView;
    private MenuListAdapter mMenuListAdapter;
    private TypedArray mTypedArray = null;
    private ArrayList<Drawable> mDrawablelist = null;
    public static int startX[][];
    public static int startY[][];
    public static int endX[][];
    public static int endY[][];
    private int key = 0; 
    public static final int TEMPLATE_ACTIVITY = 100;
    
	public static int PADDING_FRAME;
	public static int PADDING_TOP;
	private String METERIALS_PATH ="";
	
    private String mCurrentBiankuang[] = {"/Biankuang/001.ptbj",
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
            "/Biankuang/011.ptbj",
            "/Biankuang/003.ptbj",
            "/Biankuang/002.ptbj",
            "/Biankuang/001.ptbj",
            "/Biankuang/001.ptbj",
            "/Biankuang/010.ptbj",
            "/Biankuang/004.ptbj",
            "/Biankuang/009.ptbj",
            "/Biankuang/009.ptbj",
            "/Biankuang/011.ptbj",
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
    private String mCurrentStytle[][] = {{"puzzle/Banshi/002-001.ptljb","puzzle/Banshi/002-002.ptljb","puzzle/Banshi/002-003.ptljb",
        "puzzle/Banshi/002-004.ptljb","puzzle/Banshi/002-005.ptljb","puzzle/Banshi/002-006.ptljb"},
        {"puzzle/Banshi/003-001.ptljb","puzzle/Banshi/003-002.ptljb","puzzle/Banshi/003-003.ptljb","puzzle/Banshi/003-004.ptljb",
            "puzzle/Banshi/003-005.ptljb","puzzle/Banshi/003-006.ptljb","puzzle/Banshi/003-007.ptljb","puzzle/Banshi/003-008.ptljb",
            "puzzle/Banshi/003-009.ptljb","puzzle/Banshi/003-010.ptljb","puzzle/Banshi/003-011.ptljb","puzzle/Banshi/003-012.ptljb",
            "puzzle/Banshi/003-013.ptljb","puzzle/Banshi/003-014.ptljb"},
        {"puzzle/Banshi/004-001.ptljb","puzzle/Banshi/004-002.ptljb","puzzle/Banshi/004-003.ptljb","puzzle/Banshi/004-004.ptljb",
            "puzzle/Banshi/004-005.ptljb","puzzle/Banshi/004-006.ptljb","puzzle/Banshi/004-007.ptljb","puzzle/Banshi/004-008.ptljb",
            "puzzle/Banshi/004-009.ptljb","puzzle/Banshi/004-010.ptljb","puzzle/Banshi/004-011.ptljb","puzzle/Banshi/004-012.ptljb"},
        {"puzzle/Banshi/005-001.ptljb","puzzle/Banshi/005-002.ptljb","puzzle/Banshi/005-003.ptljb","puzzle/Banshi/005-004.ptljb",
            "puzzle/Banshi/005-005.ptljb","puzzle/Banshi/005-006.ptljb","puzzle/Banshi/005-007.ptljb","puzzle/Banshi/005-008.ptljb",
            "puzzle/Banshi/005-009.ptljb","puzzle/Banshi/005-010.ptljb","puzzle/Banshi/005-011.ptljb","puzzle/Banshi/005-012.ptljb"},
        {"puzzle/Banshi/006-001.ptljb","puzzle/Banshi/006-002.ptljb","puzzle/Banshi/006-003.ptljb","puzzle/Banshi/006-004.ptljb",
            "puzzle/Banshi/006-005.ptljb","puzzle/Banshi/006-006.ptljb","puzzle/Banshi/006-007.ptljb","puzzle/Banshi/006-008.ptljb",
            "puzzle/Banshi/006-009.ptljb","puzzle/Banshi/006-010.ptljb","puzzle/Banshi/006-011.ptljb"},
        {"puzzle/Banshi/007-001.ptljb","puzzle/Banshi/007-002.ptljb","puzzle/Banshi/007-003.ptljb","puzzle/Banshi/007-004.ptljb",
            "puzzle/Banshi/007-005.ptljb","puzzle/Banshi/007-006.ptljb","puzzle/Banshi/007-007.ptljb","puzzle/Banshi/007-008.ptljb",
            "puzzle/Banshi/007-009.ptljb","puzzle/Banshi/007-010.ptljb","puzzle/Banshi/007-011.ptljb"},
        {"puzzle/Banshi/008-001.ptljb","puzzle/Banshi/008-002.ptljb","puzzle/Banshi/008-003.ptljb","puzzle/Banshi/008-004.ptljb",
            "puzzle/Banshi/008-005.ptljb","puzzle/Banshi/008-006.ptljb","puzzle/Banshi/008-007.ptljb","puzzle/Banshi/008-008.ptljb",
            "puzzle/Banshi/008-009.ptljb","puzzle/Banshi/008-010.ptljb"},
        {"puzzle/Banshi/009-001.ptljb","puzzle/Banshi/009-002.ptljb","puzzle/Banshi/009-003.ptljb","puzzle/Banshi/009-004.ptljb",
                "puzzle/Banshi/009-005.ptljb","puzzle/Banshi/009-006.ptljb","puzzle/Banshi/009-007.ptljb","puzzle/Banshi/009-008.ptljb"}};
    private String mSavePath=Utils.getDefaultPath() + "/Pictures/template";
    private String mPathString = null;
    private final String TMP_PATH= "/mnt/sdcard/Pictures/template";
    private static TemplatePuzzleModel model;
    private TemplateItemView[] mTemplateItemViews;
    private TemplateViewGroup mTemplateViewGroup;
    private JNI lJni;
    private int []mShowSize=new int[2];
    private int mType=1;
    private int mFrameCount;
    private int mResultWidth;
    private int mResultHeight;
    private int mMaxShowWidth=450;
    private int mMaxShowHeight=630;
    private int mPuzzleWidth=450;
    private int mPuzzleHeight=630;
    private Vector<Bitmap> m_vecTexture = new Vector<Bitmap>();
    private int mId=0;
    private float mScaleDefault = 1f;
    public final int SAVE_PROGRESS_DIALOG = 1;
    public final int MSG_LOAD_SUCCESS = 0X20;
    public final int MSG_DIMSS_DIALOG= 4;
    private ProgressDialog mDialog;
    private Button mReturnButton;
    private final String SAVE_PATH= "sdcard/Pictures";
	//PR 554244 wangpan begin
    private Bundle savedInstanceState;
    //PR 554244 wangpan end
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		createDialog();
		 METERIALS_PATH = FileUtils.getAbsolutePathOnExternalStorage(this,"resource/puzzle");
		lJni=new JNI();
		TemplateTool.key = mId;
		model = new TemplatePuzzleModel();
		model.init(lJni);
		model.setNDKPuzzleTempPath(TMP_PATH);
		// model.initFrame(mCurrentBiankuang[0], mCurrentDiwen[0]);
		Global.setModelSaver(model);

		mContext = this;
//		imagePathList=HAblum_Main.pathlist;
		imagePathList=HAblum_Main.pathlist;
		mTemplateItemViews = new TemplateItemView[imagePathList.size()];
		model.setImagePathList(imagePathList);
		// model.setTemplatePuzzleLayout(this,
		// mCurrentStytle[imagePathList.size()-2][0], false);
        
		setContentView(R.layout.activity_template);
		init();
		// createFrame(mType,key);
		// isTemplateItemViewsInit();
		setDrawablelist();
		// showView();
		//PR 554244 wangpan begin
		this.savedInstanceState = savedInstanceState;
		//PR 554244 wangpan end
		
	}
	//PR 554244 wangpan begin
	private void initViewPrevious() {
		if(savedInstanceState != null) {
			com.jrdcom.android.gallery3d.ui.Log.i("wp", "mId = " + savedInstanceState.getInt("mId"));
			com.jrdcom.android.gallery3d.ui.Log.i("wp", "imagePath = " + savedInstanceState.getInt("imagePathSize"));
			
			  mId = savedInstanceState.getInt("mId");
            TemplateTool.key = mId;
            switchFrame(mId);
            ArrayList<Bitmap> tempArrayList = model.getBitmap();
            PADDING_FRAME = tempArrayList.get(3).getWidth();
            PADDING_TOP = tempArrayList.get(1).getHeight();
            PADDING_TOP = PADDING_TOP > (TemplateTool.wholeWidth * 0.06f) ? (int) (TemplateTool.wholeWidth * 0.06f)
                    : PADDING_TOP;
            PADDING_FRAME = PADDING_FRAME > (TemplateTool.wholeWidth * 0.06f) ? (int) (TemplateTool.wholeWidth * 0.06f)
                    : PADDING_FRAME;

            mTemplateViewGroup.setDisplayHeight(TemplateTool.wholeHeight);
            TemplateTool.resetWidth = tempArrayList.get(1).getWidth() - PADDING_FRAME * 2;
            TemplateTool.resetHeight = tempArrayList.get(3).getHeight() - PADDING_TOP * 2;
            TemplateTool.mPuzzleScale = TemplateTool.resetWidth / (640 * 1.0f);
            TemplateTool.mPuzzleScaleY = TemplateTool.resetHeight / (896 * 1.0f);

			 switchStytle(savedInstanceState.getInt("imagePathSize"),savedInstanceState.getInt("key"));

            mTemplateViewGroup.requestLayout();
            
		}
	}
	
	@Override
	protected void onSaveInstanceState(Bundle outState) {
		// TODO Auto-generated method stub
		outState.putInt("mId", mId);
		outState.putInt("imagePathSize",imagePathList.size() - 2);
		outState.putInt("key", key);
		super.onSaveInstanceState(outState);
	}
	//PR 554244 wangpan end
    protected void isTemplateItemViewsInit() {
        //add by biao.luo begin for pr533428
        if (imagePathList.size() != mTemplateItemViews.length) {
            mTemplateItemViews = new TemplateItemView[imagePathList.size()];
        }
        //add by biao.luo end
        for (int i = 0; i < imagePathList.size(); i++) {
            mTemplateItemViews[i] = new TemplateItemView(mContext, i);
            Bitmap lResBitmap =BitmapUtil.loadBitmapFromSDcard(imagePathList.get(i), true);
            mTemplateItemViews[i].setBitmap(lResBitmap);
            float ScaleH = (model.getTemplateLayout().getItem(i).getHeight()*TemplateTool.mPuzzleScaleY)/lResBitmap.getHeight();
            float ScaleW = (model.getTemplateLayout().getItem(i).getWidth()*TemplateTool.mPuzzleScale)/lResBitmap.getWidth();
            mScaleDefault = ScaleH > ScaleW ? ScaleH : ScaleW;
            mScaleDefault = mScaleDefault > 1f ? mScaleDefault : 1f;
            mTemplateItemViews[i].setStyle(0f,0f, mScaleDefault, 0f);
            mTemplateItemViews[i].setKey(i);
            mTemplateItemViews[i].setPuzzleTexture(m_vecTexture);
        }
        for (int i = 0; i < imagePathList.size(); i++) {
            mTemplateViewGroup.addView(mTemplateItemViews[imagePathList.size()-1-i]);
        }
        tvw_pintu_top_title.setText((key+1)+"/"+mCurrentStytle[imagePathList.size()-2].length);
        tvw_pintu_top_title.setVisibility(View.VISIBLE);
        mHandler.sendEmptyMessage(MSG_DIMSS_DIALOG);
    }

    
    
    private void switchFrame(int m)
    {
        model.setStytle(METERIALS_PATH+mCurrentBiankuang[m], METERIALS_PATH+mCurrentDiwen[m]);
    }
    
    private void switchStytle(int m,int n)
    {
        model.setTemplatePuzzleLayout(this,mCurrentStytle[m][n], false);
    }
    
    private void showView()
    {
        ArrayList<Bitmap> tempArrayList = model.getBitmap();
        if(null != tempArrayList)
        {
            mTemplateViewGroup.setPuzzleTexture(tempArrayList);
            mTemplateViewGroup.requestLayout();
            mTemplateViewGroup.invalidate();
        }
    }
    
    private void createFrame(int type,int m)
    {
        int []lResultSize=lJni.PuzzleFrameInit(METERIALS_PATH+mCurrentBiankuang[m], METERIALS_PATH+mCurrentDiwen[m], mPuzzleWidth, mPuzzleHeight);
        
        mShowSize=lJni.PuzzleResetShowSize(mMaxShowWidth, mMaxShowHeight, type);
        
        mFrameCount=lJni.PuzzleGetShowCount();
        
        ReleaseFrameVector();
        
        for(int i=0;i<mFrameCount;i++)
        {
            int val[] = new int[2];
            int[] data = lJni.PuzzleGetFrameShowDataWithIndex(i, val);
            m_vecTexture.add(BitmapUtil.intARGB2Bitmap(data, val[0], val[1]));
        }
        
//        mTemplateViewGroup.setPuzzleTexture(m_vecTexture);
        mTemplateViewGroup.invalidate();
        
        lJni.PuzzleClearShowFrames();
    }
    
    private void ReleaseFrameVector() {
        for (int i = m_vecTexture.size() - 1; i > -1; i--) {
            Bitmap tmpTextureStruct = m_vecTexture.get(i);

            if (tmpTextureStruct != null && !tmpTextureStruct.isRecycled()) {
                tmpTextureStruct.recycle();
                tmpTextureStruct = null;
            }
            m_vecTexture.remove(i);
        }
        m_vecTexture.clear();

        System.gc();
    }
    
    public static TemplatePuzzleModel getModel() {
        return model;
    }

    public void init() {
        picView = (ImageView) findViewById(R.id.mSrcPicView);
        maskView = (ImageView) findViewById(R.id.mMaskView);
        changeFrame = (Button) findViewById(R.id.btn_pintu_changeFrame);
        addorDelete = (Button) findViewById(R.id.btn_pintu_addOrDelete);
        closePicEdit = (ImageButton) findViewById(R.id.template_ClosePicEdit);
        replacepic = (Button) findViewById(R.id.template_replacepic);
        rightRotate = (ImageButton) findViewById(R.id.template_rightrotate);
        horizontal_ibtn = (ImageButton) findViewById(R.id.template_horizontal);
        vertical_ibtn = (ImageButton) findViewById(R.id.template_vertical);
        laststyle = (Button) findViewById(R.id.btn_pintu_laststyle);
        mSavaPintuButton = (Button) findViewById(R.id.btn_pintu_save);

		btn_pintu_nextstyle = (Button) findViewById(R.id.btn_pintu_nextstyle);
		btn_pintu_laststyle = (Button) findViewById(R.id.btn_pintu_laststyle);
		btn_change_layout = (Button) findViewById(R.id.btn_change_layout);
		btn_close = (Button) findViewById(R.id.btn_close);
		mReturnButton = (Button) findViewById(R.id.btn_pintu_return);
		tvw_pintu_top_title = (TextView) findViewById(R.id.tvw_pintu_top_title);
		mRelativeLayout = (RelativeLayout) findViewById(R.id.template_picedit);
		mLinearLayout = (LinearLayout) findViewById(R.id.layout_pintu_picFunction);
		mHorizontalListView = (HorizontalListView) findViewById(R.id.list_template_layouts);
		mDialog = new ProgressDialog(this);
		btn_pintu_nextstyle.setOnClickListener(this);
		btn_pintu_laststyle.setOnClickListener(this);
		btn_change_layout.setOnClickListener(this);
		btn_close.setOnClickListener(this);
		changeFrame.setOnClickListener(this);
		mSavaPintuButton.setOnClickListener(this);
		addorDelete.setOnClickListener(this);
		mReturnButton.setOnClickListener(this);

		mTemplateViewGroup = (TemplateViewGroup) findViewById(R.id.template_view_group);
		mTemplateViewGroup.setOnMeasureCompleteListener(this);
		mDrawablelist = new ArrayList<Drawable>();

		mHorizontalListView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
					long arg3) {
				key = arg2;
				mMenuListAdapter.notifyDataSetChanged();
				mTemplateViewGroup.removeAllViews();
				switchStytle(imagePathList.size() - 2, key);
				isTemplateItemViewsInit();
			}
		});
	}

	private void setDrawablelist() {
		if (imagePathList == null)
			return;

		switch (imagePathList.size()) {
		case 2:
			mTypedArray = this.getResources().obtainTypedArray(
					R.array.template_layout_2_thumb_list);
			for (int i = 0; i < mTypedArray.length(); i++) {
				mDrawablelist.add(mTypedArray.getDrawable(i));
			}
			mMenuListAdapter = new MenuListAdapter(getApplicationContext(),
					mDrawablelist);
			break;
		case 3:
			mTypedArray = this.getResources().obtainTypedArray(
					R.array.template_layout_3_thumb_list);
			for (int i = 0; i < mTypedArray.length(); i++) {
				mDrawablelist.add(mTypedArray.getDrawable(i));
			}
			mMenuListAdapter = new MenuListAdapter(getApplicationContext(),
					mDrawablelist);
			break;
		case 4:
			mTypedArray = this.getResources().obtainTypedArray(
					R.array.template_layout_4_thumb_list);
			for (int i = 0; i < mTypedArray.length(); i++) {
				mDrawablelist.add(mTypedArray.getDrawable(i));
			}
			mMenuListAdapter = new MenuListAdapter(getApplicationContext(),
					mDrawablelist);
			break;
		case 5:
			mTypedArray = this.getResources().obtainTypedArray(
					R.array.template_layout_5_thumb_list);
			for (int i = 0; i < mTypedArray.length(); i++) {
				mDrawablelist.add(mTypedArray.getDrawable(i));
			}
			mMenuListAdapter = new MenuListAdapter(getApplicationContext(),
					mDrawablelist);
			break;
		case 6:
			mTypedArray = this.getResources().obtainTypedArray(
					R.array.template_layout_6_thumb_list);
			for (int i = 0; i < mTypedArray.length(); i++) {
				mDrawablelist.add(mTypedArray.getDrawable(i));
			}
			mMenuListAdapter = new MenuListAdapter(getApplicationContext(),
					mDrawablelist);
			break;
		case 7:
			mTypedArray = this.getResources().obtainTypedArray(
					R.array.template_layout_7_thumb_list);
			for (int i = 0; i < mTypedArray.length(); i++) {
				mDrawablelist.add(mTypedArray.getDrawable(i));
			}
			mMenuListAdapter = new MenuListAdapter(getApplicationContext(),
					mDrawablelist);
			break;
		case 8:
			mTypedArray = this.getResources().obtainTypedArray(
					R.array.template_layout_8_thumb_list);
			for (int i = 0; i < mTypedArray.length(); i++) {
				mDrawablelist.add(mTypedArray.getDrawable(i));
			}
			mMenuListAdapter = new MenuListAdapter(getApplicationContext(),
					mDrawablelist);
			break;
		case 9:
			mTypedArray = this.getResources().obtainTypedArray(
					R.array.template_layout_9_thumb_list);
			for (int i = 0; i < mTypedArray.length(); i++) {
				mDrawablelist.add(mTypedArray.getDrawable(i));
			}
			mMenuListAdapter = new MenuListAdapter(getApplicationContext(),
					mDrawablelist);
			break;
		default:
			break;
		}
		
        // yaogang.hao for PR530449
        if (mMenuListAdapter == null)
        {
            finish();
            return;
        }
        else//yaogang.hao for PR533196
        {
            if(mMenuListAdapter == null)
            {
                mMenuListAdapter = new  MenuListAdapter(getApplicationContext(),
                        mDrawablelist);
            }
            mHorizontalListView.setAdapter(mMenuListAdapter);
        }
       
	}

    private void setTemplateViewInit() {
        
        if(imagePathList == null)return;
        TemplateView[] templateView = new TemplateView[imagePathList.size()];
        
        for (int i = 0; i < imagePathList.size(); i++) {
            switch (imagePathList.size()) {
                case 2:
                    startX = new int[][]{{0,0},{0,0},{0,0},{0,200},{0,200}};
                    startY = new int[][]{{0,200},{0,300},{0,400},{0,0},{0,400}};
                    endX = new int[][]{{400,400},{400,400},{400,400},{200,400},{400,400}};
                    endY = new int[][]{{200,600},{300,600},{400,600},{600,600},{600,600}};
                    templateView[i] = new TemplateView(mContext, null, imagePathList.get(i),endX[key][i]-startX[key][i],endY[key][i]-startY[key][i]);
                    mTemplateViewGroup.addView(templateView[i]);
                    break;
                case 3:
                    startX = new int[][]{{0,0,0},{0,0,200},{0,0,200},{0,200,200},{0,0,250},
                            {0,0,0},{0,150,0},{0,200,0},{0,250,250},{0,200,0}};
                    startY = new int[][]{{0,200,400},{0,400,400},{0,300,300},{0,0,300},{0,400,400},
                            {0,150,350},{0,0,200},{0,0,400},{0,0,300},{0,0,400}};
                    endX = new int[][]{{400,400,400},{400,200,400},{400,200,400},{200,400,400},{400,250,400},
                            {400,400,400},{150,400,400},{200,400,200},{250,400,400},{200,400,400}};
                    endY = new int[][]{{200,400,600},{400,600,600},{300,600,600},{600,300,600},{400,600,600},
                            {150,350,600},{200,200,600},{400,600,600},{600,300,600},{400,400,600}};
                    templateView[i] = new TemplateView(mContext, null, imagePathList.get(i),endX[key][i]-startX[key][i],endY[key][i]-startY[key][i]);
                    mTemplateViewGroup.addView(templateView[i]);
                    break;
                case 4:
                    startX = new int[][]{{0,200,0,200},{0,0,133,266},{0,200,200,0},{0,250,0,150},{0,200,200,200},
                            {0,200,0,0},{0,200,0,200},{0,200,0,200},{0,150,0,150},{0,250,0,250}};
                    startY = new int[][]{{0,0,300,300},{0,400,400,400},{0,0,150,300},{0,0,300,300},{0,0,200,400},
                            {0,0,250,450},{0,0,400,200},{0,0,200,400},{0,0,200,200},{0,0,400,400}};
                    endX = new int[][]{{200,400,200,400},{400,133,266,400},{200,400,400,400},{250,400,150,400},{200,400,400,400},
                            {200,400,200,200},{200,400,200,400},{200,400,200,400},{150,400,150,400},{250,400,250,400}};
                    endY = new int[][]{{300,300,600,600},{400,600,600,600},{300,150,300,600},{300,300,600,600},{600,200,400,600},
                            {250,600,450,600},{400,200,600,600},{200,400,600,600},{200,200,600,600},{400,400,600,600}};
                    templateView[i] = new TemplateView(mContext, null, imagePathList.get(i),endX[key][i]-startX[key][i],endY[key][i]-startY[key][i]);
                    mTemplateViewGroup.addView(templateView[i]);
                    break;
                case 5:
                    startX = new int[][]{{0,250,250,250,250},{0,250,250,0,250},{0,250,250,0,250},
                            {0,0,200,0,200},{0,200,0,0,200},{0,200,0,200,0},{0,250,0,250,0},
                            {0,200,200,0,200},{0,150,0,0,250},{0,0,100,200,300},{0,250,250,250,0}};
                    startY = new int[][]{{0,0,150,300,450},{0,0,200,300,400},{0,0,200,400,400},
                            {0,300,300,450,450},{0,0,150,450,450},{0,0,200,200,400},{0,0,200,300,400},
                            {0,0,200,400,400},{0,0,150,450,450},{0,450,450,450,450},{0,0,120,240,360}};
                    endX = new int[][]{{250,400,400,400,400},{250,400,400,250,400},{250,400,400,250,400},
                            {400,200,400,200,400},{200,400,400,200,400},{200,400,200,400,400},{250,400,250,400,250},
                            {200,400,400,200,400},{150,400,400,250,400},{400,100,200,300,400},{250,400,400,400,400}};
                    endY = new int[][]{{600,150,300,450,600},{300,200,400,600,600},{400,200,400,600,600},
                            {300,450,450,600,600},{150,150,450,600,600},{200,200,400,400,600},{200,300,400,600,600},
                            {400,200,400,600,600},{150,150,450,600,600},{450,600,600,600,600},{360,120,240,360,600}};
                    templateView[i] = new TemplateView(mContext, null, imagePathList.get(i),endX[key][i]-startX[key][i],endY[key][i]-startY[key][i]);
                    mTemplateViewGroup.addView(templateView[i]);
                    break;
                case 6:
                    startX = new int[][]{{0,200,200,0,200,200},{0,200,0,0,200,200},{0,0,200,0,133,266},
                            {0,200,0,200,0,200},{0,150,0,200,0,250},{0,200,0,200,0,200},{0,200,0,200,0,0},
                            {0,250,0,250,0,250},{0,250,0,150,0,250},{0,250,250,250,0,200}};
                    startY = new int[][]{{0,0,150,300,300,450},{0,0,150,300,300,450},{0,200,200,400,400,400},
                            {0,0,200,200,400,400},{0,0,200,200,400,400},{0,0,150,150,300,300},{0,0,150,150,300,450},
                            {0,0,200,200,400,400},{0,0,200,200,400,400},{0,0,120,240,360,360}};
                    endX = new int[][]{{200,400,400,200,400,400},{200,400,200,200,400,400},{400,200,400,133,266,400},
                            {200,400,200,400,200,400},{150,400,200,400,250,400},{200,400,200,400,200,400},{200,400,200,400,400,400},
                            {250,400,250,400,250,400},{250,400,150,400,250,400},{250,400,400,400,200,400}};
                    endY = new int[][]{{300,150,300,600,450,600},{150,300,300,600,450,600},{200,400,400,600,600,600},
                            {200,200,400,400,600,600},{200,200,400,400,600,600},{150,150,300,300,600,600},{150,150,300,300,450,600},
                            {200,200,400,400,600,600},{200,200,400,400,600,600},{360,120,240,360,600,600}};
                    templateView[i] = new TemplateView(mContext, null, imagePathList.get(i),endX[key][i]-startX[key][i],endY[key][i]-startY[key][i]);
                    mTemplateViewGroup.addView(templateView[i]);
                    break;
                case 7:
                    startX = new int[][]{{0,133,266,0,0,133,266},{0,200,0,200,0,133,266},{0,250,0,250,0,133,266},
                            {0,133,266,133,0,133,266},{0,133,0,133,266,0,266},{0,266,0,133,266,0,266},
                            {0,133,266,266,0,133,266},{0,200,200,0,200,0,200},{0,0,133,266,0,133,266},
                            {0,100,200,300,0,200,0}};
                    startY = new int[][]{{0,0,0,175,425,425,425},{0,0,200,200,400,400,400},{0,0,200,200,400,400,400},
                            {0,0,0,200,300,400,300},{0,0,200,200,200,400,400},{0,0,200,200,200,400,400},
                            {0,0,0,200,300,300,400},{0,0,150,300,300,450,450},{0,300,300,300,450,450,450},
                            {0,0,0,0,150,150,300}};
                    endX = new int[][]{{133,266,400,400,133,266,400},{200,400,200,400,133,266,400},{250,400,250,400,133,266,400},
                            {133,266,400,266,133,266,400},{133,400,133,266,400,266,400},{266,400,133,266,400,266,400},
                            {133,266,400,400,133,266,400},{200,400,400,200,400,200,400},{400,133,266,400,133,266,400},
                            {100,200,300,400,200,400,400}};
                    endY = new int[][]{{175,175,175,425,600,600,600},{200,200,400,400,600,600,600},{200,200,400,400,600,600,600},
                            {300,200,300,400,600,600,600},{200,200,400,400,400,600,600},{200,200,400,400,400,600,600},
                            {300,300,200,400,600,600,600},{300,150,300,450,450,600,600},{300,450,450,450,600,600,600},
                            {150,150,150,150,300,300,600}};
                    templateView[i] = new TemplateView(mContext, null, imagePathList.get(i),endX[key][i]-startX[key][i],endY[key][i]-startY[key][i]);
                    mTemplateViewGroup.addView(templateView[i]);
                    break;
                case 8:
                    startX = new int[][]{{0,200,0,200,0,200,0,200},{0,133,266,0,133,266,0,266},{0,266,0,133,266,0,133,266},
                            {0,133,266,0,266,0,133,266},{0,133,266,0,200,0,133,266},{0,200,0,133,266,0,133,266},
                            {0,200,300,0,200,0,100,200},{0,133,266,0,0,0,133,266},{0,133,0,266,0,133,0,266},
                            {0,100,200,200,0,200,300,0}};
                    startY = new int[][]{{0,0,150,150,300,300,450,450},{0,0,0,200,300,200,400,400},{0,0,300,300,300,450,450,450},
                            {0,0,0,200,200,400,400,400},{0,0,0,200,200,400,400,400},{0,0,300,300,300,450,450,450},
                            {0,0,0,175,175,425,425,425},{0,0,0,150,300,450,450,450},{0,0,150,150,300,300,450,450},
                            {0,0,0,150,300,300,300,450}};
                    endX = new int[][]{{200,400,200,400,200,400,200,400},{133,266,400,133,266,400,133,400},{266,400,133,266,400,133,266,400},
                            {133,266,400,266,400,133,266,400},{133,266,400,200,400,133,266,400},{200,400,133,266,400,133,266,400},
                            {200,300,400,200,400,100,200,400},{133,266,400,400,400,133,266,400},{133,400,266,400,133,400,266,400},
                            {100,200,400,400,200,300,400,200}};
                    endY = new int[][]{{150,150,300,300,450,450,600,600},{200,300,200,400,600,400,600,600},{300,300,450,450,450,600,600,600},
                            {200,200,200,400,400,600,600,600},{200,200,200,400,400,600,600,600},{300,300,450,450,450,600,600,600},
                            {175,175,175,425,425,600,600,600},{150,150,150,300,450,600,600,600},{150,150,300,300,450,450,600,600},
                            {300,300,150,300,450,600,600,600}};
                    templateView[i] = new TemplateView(mContext, null, imagePathList.get(i),endX[key][i]-startX[key][i],endY[key][i]-startY[key][i]);
                    mTemplateViewGroup.addView(templateView[i]); 
                    break;
                case 9:
                	startX = new int[][]{{0,133,266,0,133,266,0,133,266},{0,100,300,0,100,300,0,100,300},
                            {0,200,0,133,266,0,100,200,300},{0,0,100,200,300,0,100,200,300},{0,200,0,200,0,0,100,200,300},
                            {0,100,200,300,0,0,100,200,300},{0,133,266,0,200,0,100,200,300},{0,133,266,0,133,266,0,133,266}};
                    startY = new int[][]{{0,0,0,200,200,200,400,400,400},{0,0,0,175,175,175,425,425,425},
                            {0,0,250,250,250,425,425,425,425},{0,300,300,300,300,450,450,450,450},{0,0,100,100,200,450,450,450,450},
                            {0,0,0,0,150,450,450,450,450},{0,0,0,200,200,450,450,450,450},{0,0,0,150,150,150,450,450,450}};
                    endX = new int[][]{{133,266,400,133,266,400,133,266,400},{100,300,400,100,300,400,100,300,400},
                            {200,400,133,266,400,100,200,300,400},{400,100,200,300,400,100,200,300,400},{200,400,200,400,400,100,200,300,400},
                            {100,200,300,400,400,100,200,300,400},{133,266,400,200,400,100,200,300,400},{133,266,400,133,266,400,133,266,400}};
                    endY = new int[][]{{200,200,200,400,400,400,600,600,600},{175,175,175,425,425,425,600,600,600},
                            {250,250,425,425,425,600,600,600,600},{300,450,450,450,450,600,600,600,600},{100,100,200,200,450,600,600,600,600},
                            {150,150,150,150,450,600,600,600,600},{200,200,200,450,450,600,600,600,600},{150,150,150,450,450,450,600,600,600}};
                    templateView[i] = new TemplateView(mContext, null, imagePathList.get(i),endX[key][i]-startX[key][i],endY[key][i]-startY[key][i]);
                    mTemplateViewGroup.addView(templateView[i]); 
                    break;

                default:
                    break;
            }
        }
        mTemplateViewGroup.setKey(imagePathList.size());
//        tvw_pintu_top_title.setText((key+1)+"/"+);
//        tvw_pintu_top_title.setVisibility(View.VISIBLE);
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
            case R.id.btn_pintu_changeFrame:
                Intent intent = new Intent();
                intent.setClass(ActivityTemplate.this,
                        ActivityTemplateSetStyle.class);
                startActivityForResult(intent, TEMPLATE_ACTIVITY);
                break;
            case R.id.btn_pintu_addOrDelete:
                Intent intent_addordelete = new Intent();
                intent_addordelete.setClass(ActivityTemplate.this,
                        HAblum_Main.class);
//                intent_addordelete.putStringArrayListExtra("go_back_list",imagePathList);
                intent_addordelete.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                startActivity(intent_addordelete);

                //PR865251 modify gallery show error we support one instance by fengke at 2014.12.8 start
                finish();
                //PR865251 modify gallery show error we support one instance by fengke at 2014.12.8 end

                break;
            case R.id.btn_pintu_nextstyle:
                key++;
                showLoadDialog();//pr 550162 by xiangchen begin
                mHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        // TODO Auto-generated method stub
                        if (key < mCurrentStytle[imagePathList.size()-2].length) {
                            mTemplateViewGroup.removeAllViews();
                            switchStytle(imagePathList.size()-2,key);
                            isTemplateItemViewsInit();
                        } else {
                            key = 0;
                            mTemplateViewGroup.removeAllViews();
                            switchStytle(imagePathList.size()-2,key);
                            isTemplateItemViewsInit();
                        }
                        //PR853418 moidfy The style of template doesn't refresh when changing the template by fengke at 2014.11.27 start
                        if (mMenuListAdapter != null) {
                            mMenuListAdapter.notifyDataSetChanged();
                        }
                        //PR853418 moidfy The style of template doesn't refresh when changing the template by fengke at 2014.11.27 end
                    }
                }, 200);//pr 550162 by xiangchen end
                break;
            case R.id.btn_pintu_laststyle:
                key--;
                showLoadDialog();//pr 550162 by xiangchen begin
                mHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        // TODO Auto-generated method stub
                        if (key >= 0) {
                            mTemplateViewGroup.removeAllViews();
                            switchStytle(imagePathList.size()-2,key);
                            isTemplateItemViewsInit();
                        } else {
                            key = mCurrentStytle[imagePathList.size()-2].length - 1;
                            mTemplateViewGroup.removeAllViews();
                            switchStytle(imagePathList.size()-2,key);
                            isTemplateItemViewsInit();
                        }
                        //PR853418 moidfy The style of template doesn't refresh when changing the template by fengke at 2014.11.27 start
                        if (mMenuListAdapter != null) {
                            mMenuListAdapter.notifyDataSetChanged();
                        }
                        //PR853418 moidfy The style of template doesn't refresh when changing the template by fengke at 2014.11.27 end
                    }
                }, 200);//pr 550162 by xiangchen end
                break;
            case R.id.btn_change_layout:
                PinTuTabActivity.setGenderGroup(true);
                mRelativeLayout.setVisibility(View.VISIBLE);
                mLinearLayout.setVisibility(View.GONE);
                break;
            case R.id.btn_close:
                mRelativeLayout.setVisibility(View.GONE);
                mLinearLayout.setVisibility(View.VISIBLE);
                PinTuTabActivity.setGenderGroup(false);
                break;
            case R.id.btn_pintu_save:
                {
                    //PR651227-tao li-begin 001
                    if (!Utils.hasAvailableSpace(Utils.getDefaultPath())) {
                      Utils.showToast(ActivityTemplate.this, R.string.not_enough_storage);
                          break;
                    }
                    //PR651227-tao li-end 001
                    //PR939610 the save dir should be associate with default storage path modify by fengke at 2015.03.07 start
                    mSaveDir = Utils.getDefaultPath() + "/Pictures/";
                    mDialog.setMessage(mContext.getString(R.string.save_saving)+"\n"+
                            mContext.getString(R.string.save_path)+Utils.getDescriptionPath(mContext,mSaveDir));
                    //PR939610 the save dir should be associate with default storage path modify by fengke at 2015.03.07 end
                    mDialog.show();
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                for (int i = 0; i <imagePathList.size() ; i++) {
                                    model.setScale(mTemplateItemViews[i].getScale(), i);
                                    model.setScaleX(mTemplateItemViews[i].getScaleX(), i);
                                    model.setScaleY(mTemplateItemViews[i].getScaleY(), i);
                                    model.setDeltaX(mTemplateItemViews[i].getDeltaX(), i);
                                    model.setDeltaY(mTemplateItemViews[i].getDeltaY(), i);
                                    model.isScale(mTemplateItemViews[i].isScale(), i);
                                }
                                //PR939610 the save dir should be associate with default storage path modify by fengke at 2015.03.07 start
                                mSavePath = mSaveDir + "template";
                                //mSavePath = Utils.getDefaultPath() + "/Pictures/template";// xiaodaijun PR675235 add
                                //PR939610 the save dir should be associate with default storage path modify by fengke at 2015.03.07 end
                                mPathString = getSavedPicName(mSavePath);
                                createSDDir(mSaveDir);//PR630752-tao li-001
                                model.saveDataToPath(mPathString);
                                scanDirAsync();
                            } catch (Exception e) {
                                e.printStackTrace();
                            }finally {
                                mHandler.sendEmptyMessage(SAVE_PROGRESS_DIALOG);
                            }
                        }
                    }).start();
                    break;
                }
            case R.id.btn_pintu_return:
                Intent go_back = new Intent();
                go_back.setClass(ActivityTemplate.this, HAblum_Main.class);
                go_back.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                startActivity(go_back);
                finish();
                break;
            default:
                break;
        }
    }
    
    /**
     * pr 550162 by xiangchen
     */
    public void showLoadDialog(){
    	mDialog.setMessage(mContext.getString(R.string.loading));
    	mDialog.setCancelable(false);
    	mDialog.show();
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

    private Handler mHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
	            case MSG_DIMSS_DIALOG:
	            	  if(mDialog!=null&&mDialog.isShowing())mDialog.dismiss();  //pr 550162 by xiangchen
	            	   break; 
                case SAVE_PROGRESS_DIALOG:
                    //PR857266 modify for force close during saving by fengke at 2014.12.1 start
                    if ((mDialog != null) && mDialog.isShowing()) {
                        mDialog.dismiss();
                    }
                    //PR857266 modify for force close during saving by fengke at 2014.12.1 end
                    Toast.makeText(ActivityTemplate.this, ActivityTemplate.this.getString(R.string.save_success), Toast.LENGTH_SHORT).show();
                    break;
                    //added by yaogang.hao optimize loading speed
                case MSG_LOAD_SUCCESS:
                {
                  mTemplateViewGroup.setDisplayHeight(TemplateTool.wholeHeight);
                  //PR 554244 wangpan begin
				  initViewPrevious();
				  //PR 554244 wangpan end
                  isTemplateItemViewsInit();
                  //yaogang.hao for PR 533428
                  if(progressDialog!=null && progressDialog.isShowing())
                  progressDialog.dismiss();
                  showView();
                  
                    break;
                }
                default:
                        break;
                }
        };
    };
    
    private void scanDirAsync(){
        Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        File f = new File(mPathString);
        Uri contentUri = Uri.fromFile(f);
        mediaScanIntent.setData(contentUri);
        ActivityTemplate.this.sendBroadcast(mediaScanIntent);
    }
    
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode != RESULT_OK)
            return;

        if (requestCode == TEMPLATE_ACTIVITY) {
            mId = data.getIntExtra("id", 0);
//            mTemplateViewGroup.removeAllViews();
//            switchFrame(mId);
////            createFrame(mType,mId);
//            showView(mId);
//            mTemplateViewGroup.requestLayout();
//            mTemplateViewGroup.invalidate();
            TemplateTool.key = mId;
            switchFrame(mId);
            ArrayList<Bitmap> tempArrayList = model.getBitmap();
            PADDING_FRAME = tempArrayList.get(3).getWidth();
            PADDING_TOP = tempArrayList.get(1).getHeight();
            PADDING_TOP = PADDING_TOP > (TemplateTool.wholeWidth * 0.06f) ? (int) (TemplateTool.wholeWidth * 0.06f)
                    : PADDING_TOP;
            PADDING_FRAME = PADDING_FRAME > (TemplateTool.wholeWidth * 0.06f) ? (int) (TemplateTool.wholeWidth * 0.06f)
                    : PADDING_FRAME;

            mTemplateViewGroup.setDisplayHeight(TemplateTool.wholeHeight);
            TemplateTool.resetWidth = tempArrayList.get(1).getWidth() - PADDING_FRAME * 2;
            TemplateTool.resetHeight = tempArrayList.get(3).getHeight() - PADDING_TOP * 2;
            TemplateTool.mPuzzleScale = TemplateTool.resetWidth / (640 * 1.0f);
            TemplateTool.mPuzzleScaleY = TemplateTool.resetHeight / (896 * 1.0f);

            switchStytle(imagePathList.size()-2,key);
            mTemplateViewGroup.requestLayout();
            showView();
        }
        super.onActivityResult(requestCode, resultCode, data);
    }
    
    public String getSavedPicName(String path)
    {
        return path+"_"+System.currentTimeMillis()+".jpg";
    }
    
    class MenuListAdapter extends BaseAdapter {

        private Context mContext;
        private ArrayList<Drawable> mData;
        private LayoutInflater layoutInflater;

        public MenuListAdapter(Context context, ArrayList<Drawable> data) {
            mContext = context;
            layoutInflater = LayoutInflater.from(mContext);
            mData = data;
        }

        @Override
        public int getCount() {
            return mData.size();
        }

        @Override
        public Drawable getItem(int position) {
            return mData.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            convertView = layoutInflater.inflate(R.layout.menuitem,null);
            RelativeLayout relative = (RelativeLayout) convertView.findViewById(R.id.bg);
            ImageView imageview = (ImageView) convertView.findViewById(R.id.menu_image);
            imageview.setBackground(mData.get(position));
            if(position == key){
                relative.setBackgroundResource(R.drawable.bg_template_layout_selected);
            }
            return convertView;
        }
    }
    @Override
    public void onMeasureComplete(int width, int height) {
        
        //added by yaogang.hao optimize loading speed
        TemplateTool.wholeWidth = width;
        TemplateTool.wholeHeight = (int) (896 * (TemplateTool.wholeWidth / (640 * 1.0f)));
        //yaogang.hao for PR 533428
        if(progressDialog!=null && !progressDialog.isShowing())
            progressDialog.show();
        
        new Thread(new Runnable() {
            
            @Override
            public void run() {
                model.initFrame(METERIALS_PATH+mCurrentBiankuang[0], METERIALS_PATH+mCurrentDiwen[0]);
                model.setFrameWidthAndHeight(TemplateTool.wholeWidth, TemplateTool.wholeHeight);
                ArrayList<Bitmap> tempArrayList = model.getBitmap();
                PADDING_FRAME = tempArrayList.get(3).getWidth();
                PADDING_TOP = tempArrayList.get(1).getHeight();
                PADDING_TOP = PADDING_TOP > (TemplateTool.wholeWidth * 0.06f) ? (int) (TemplateTool.wholeWidth * 0.06f)
                        : PADDING_TOP;
                PADDING_FRAME = PADDING_FRAME > (TemplateTool.wholeWidth * 0.06f) ? (int) (TemplateTool.wholeWidth * 0.06f)
                        : PADDING_FRAME;
               
                TemplateTool.resetWidth = tempArrayList.get(1).getWidth() - PADDING_FRAME * 2;
                TemplateTool.resetHeight = tempArrayList.get(3).getHeight() - PADDING_TOP * 2;
                TemplateTool.mPuzzleScale = TemplateTool.resetWidth / (640 * 1.0f);
                TemplateTool.mPuzzleScaleY = TemplateTool.resetHeight / (896 * 1.0f);
        
                model.setTemplatePuzzleLayout(ActivityTemplate.this,
                        mCurrentStytle[imagePathList.size() - 2][0], false);
                mHandler.sendEmptyMessage(MSG_LOAD_SUCCESS);
            }
        }).start();
//        model.initFrame(METERIALS_PATH+mCurrentBiankuang[0], METERIALS_PATH+mCurrentDiwen[0]);
//        model.setFrameWidthAndHeight(TemplateTool.wholeWidth, TemplateTool.wholeHeight);
//         tempArrayList = model.getBitmap();
//        PADDING_FRAME = tempArrayList.get(3).getWidth();
//        PADDING_TOP = tempArrayList.get(1).getHeight();
//        PADDING_TOP = PADDING_TOP > (TemplateTool.wholeWidth * 0.06f) ? (int) (TemplateTool.wholeWidth * 0.06f)
//                : PADDING_TOP;
//        PADDING_FRAME = PADDING_FRAME > (TemplateTool.wholeWidth * 0.06f) ? (int) (TemplateTool.wholeWidth * 0.06f)
//                : PADDING_FRAME;
//        mTemplateViewGroup.setDisplayHeight(TemplateTool.wholeHeight);
//        mHandler.sendEmptyMessage(MSG_LOAD_SUCCESS);
//        TemplateTool.resetWidth = tempArrayList.get(1).getWidth() - PADDING_FRAME * 2;
//        TemplateTool.resetHeight = tempArrayList.get(3).getHeight() - PADDING_TOP * 2;
//        TemplateTool.mPuzzleScale = TemplateTool.resetWidth / (640 * 1.0f);
//        TemplateTool.mPuzzleScaleY = TemplateTool.resetHeight / (896 * 1.0f);
//
//        model.setTemplatePuzzleLayout(this,
//                mCurrentStytle[imagePathList.size() - 2][0], false);
//        isTemplateItemViewsInit();
//        showView();
        
    }
    @Override
    protected void onResume ()
    {
        super.onResume();
        lJni.PuzzleFrameInit(METERIALS_PATH+mCurrentBiankuang[TemplateTool.key], METERIALS_PATH+mCurrentDiwen[TemplateTool.key], mPuzzleWidth, mPuzzleHeight);
    }
    public void createDialog()
    {
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage(getString(R.string.tip_please_wait_content));
        progressDialog.setIndeterminate(true);
        progressDialog.setCancelable(false);
    }
    
    @Override
    public void onBackPressed() {
        Intent go_back = new Intent();
        go_back.setClass(ActivityTemplate.this, HAblum_Main.class);
        go_back.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        startActivity(go_back);
        finish();
    }
}
