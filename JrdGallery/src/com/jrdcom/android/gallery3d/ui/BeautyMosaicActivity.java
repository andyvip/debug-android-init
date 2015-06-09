package com.jrdcom.android.gallery3d.ui;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.R.integer;
import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Shader;
import android.graphics.Bitmap.Config;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;


import com.jrdcom.android.gallery3d.R;
import com.jrdcom.example.joinpic.HorizontalListView;
//PR936992 Undo and Redo icon are useless,while SD card is full add by limin.zhuo at 2015.03.06 begin
import com.jrdcom.example.joinpic.Utils;
//PR936992 Undo and Redo icon are useless,while SD card is full add by limin.zhuo at 2015.03.06 end
import com.jrdcom.mt.MTActivity;
import com.jrdcom.mt.core.ToolMosaic;
import com.jrdcom.mt.mtxx.controls.MtprogressDialog;
import com.jrdcom.mt.util.MyData;
import com.jrdcom.mt.widget.MosaicView;
import com.jrdcom.mt.widget.DragCircleSeekBar;
import com.jrdcom.mt.widget.DragCircleSeekBar.onDragCircleListener;


public class BeautyMosaicActivity extends MTActivity implements OnClickListener,
        OnItemClickListener,onDragCircleListener
        {

    private ImageView mPreview = null;
    private HorizontalListView mMosaiclistview;
    private MenuListAdapter mListAdapter;
    private int mosaicId;
    private RadioButton mPaintRadioButton;
    private RadioButton mEraserRadioButton;
    private ImageView mMosaicImage;
    private boolean mMosaicshow = true;
    private boolean mEraseshow = true;
    private RelativeLayout mMosaicStyle;
    private MosaicView mMosaicView;
    private ToolMosaic m_tool; // 马赛克工具类
    private String ASSETS_MOSAIC[] = {
            "mosaic/10079001.moc","mosaic/10079002.moc","mosaic/10079003.moc","mosaic/10079004.moc",
            "mosaic/10079005.moc","mosaic/10079006.moc","mosaic/10079007.moc","mosaic/10079008.moc",
            "mosaic/10079009.moc","mosaic/10079010.moc","mosaic/10079011.moc"
    };
    Bitmap pMosaicTexture;
    Bitmap firstBitmap;
    int mWidth;
    int mHeight;
    private DragCircleSeekBar dragCircleSeekBar;
    //xiaodaijun PR675113 start
    private int[] beautymosaic = {
        R.drawable.mosaic_style_icon_1, R.drawable.mosaic_style_icon_2, R.drawable.mosaic_style_icon_3,
        R.drawable.mosaic_style_icon_4, R.drawable.mosaic_style_icon_5, R.drawable.mosaic_style_icon_6,
        R.drawable.mosaic_style_icon_7, R.drawable.mosaic_style_icon_8, R.drawable.mosaic_style_icon_9,
        R.drawable.mosaic_style_icon_10, R.drawable.mosaic_style_icon_11, R.drawable.mosaic_style_icon_12
    };
    //xiaodaijun PR675113 end
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        setContentView(R.layout.beauty_mosaic);
        findView();
        initData();

        // mPreview = (ImageView) findViewById(R.id.preview);
        // findViewById(R.id.btn_example1).setOnClickListener(this);
        // findViewById(R.id.btn_example2).setOnClickListener(this);
        // findViewById(R.id.btn_example3).setOnClickListener(this);
        // ((TextView)
        // findViewById(R.id.tv_title)).setText(R.string.mainmenu_mosaic);
        //
        //
        // // 初始化
        // m_tool = new ToolMosaic();
        // m_tool.init(MyData.getJNI());
        //
        // m_tool.procMosaic(10);
        // // 获取界面原图用
        // Bitmap mBitmap = m_tool.getShowOralImage();
        // mPreview.setImageBitmap(mBitmap);
    }

    Handler mHandler = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1:
                    // 美图必备 获取处理后的UI效果图并显示
                    mPreview.setImageBitmap(m_tool.getShowProcImage());
                    break;
                case 2:
                    mMosaicView.isShowCenterPen = false;
                    mMosaicView.invalidate();
                    break;
            }
            super.handleMessage(msg);
        }

    };

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
            case R.id.rbtn_paint:
                mMosaicView.setShaderBitmap(true);
                mEraseshow = true;
                break;
            case R.id.rbtn_eraser:
                if(mMosaicshow && mEraseshow)
                {
                    hidi();
                }
                mEraseshow = false;
                mMosaicView.setShaderBitmap(false);
                mPaintRadioButton.setPressed(false);
                break;
            case R.id.layout_mosaic_style:
                if(mMosaicshow)
                {
                    hidi();
                }else {
                    show();
                }
                break;
            // case R.id.btn_example1:
            // doExample1();
            // break;
            // case R.id.btn_example2:
            // doExample2();
            // break;
            // case R.id.btn_example3:
            // doExample3();
            // break;
            default:
                break;
        }
    }

    // 保存
    private void onOK() {
        new MtprogressDialog(this, true, this.getString(R.string.mosaic_progess_title)) {
            @Override
            public void process() {
                try {
                    // 美图必备 确定功能

                    // 马赛克涂抹的区域
//                    Bitmap bmp = null;
//                    int nWidth = m_tool.getShowImageSize()[0];
//                    int nHeight = m_tool.getShowImageSize()[1];
//                    bmp = Bitmap.createBitmap(nWidth, nHeight, Bitmap.Config.ARGB_8888);
//
//                    Canvas canvas = new Canvas(bmp);
//                    Paint paint = new Paint();
//                    paint.setColor(Color.WHITE);
//                    paint.setAntiAlias(true);
//                    int cx = (int) (nWidth * 0.5);
//                    int cy = (int) (nHeight * 0.5);
//                    int radius = 30;
//                    canvas.drawCircle(cx, cy, radius, paint);
//
//                    // 确定功能
//                    m_tool.ok(mMosaicView.getMasaicBitmap());
//                    MyData.getBeautyControl().pushImage();
                    mMosaicView.onOK();

                    finish();
                } catch (Exception e) {
                }
            }
        }.show();

    }

    // 取消
    private void onCancel() {
        // 美图必备 取消功能
//        m_tool.cancel();
        mMosaicView.onCancel();

        finish();
    }

    // 普通马赛克
    private void doExample1() {
        new MtprogressDialog(this, true, "普通马赛克") {
            @Override
            public void process() {
                try {
                    m_tool.procMosaic(10);
                    Message message = new Message();
                    message.what = 1;
                    mHandler.sendMessage(message);
                } catch (Exception e) {
                }
            }
        }.show();
    }

    // Asset
    private void doExample2() {
        new MtprogressDialog(this, true, "assets里的素材马赛克") {
            @Override
            public void process() {
                try {
                    Bitmap pMosaicTexture = BitmapFactory.decodeStream(getAssets().open(
                            "mosaic/10079001.moc"));
                    m_tool.procMosaicWithTexture(10, pMosaicTexture);
                    Message message = new Message();
                    message.what = 1;
                    mHandler.sendMessage(message);
                } catch (Exception e) {
                }
            }
        }.show();
    }

    // SD卡
    private void doExample3() {
        new MtprogressDialog(this, true, "SD卡里的素材马赛克") {
            @Override
            public void process() {
                try {
                    Bitmap pMosaicTexture = BitmapFactory
                            .decodeFile("/mnt/sdcard/sdk/mosaic/10079002.moc");
                    m_tool.procMosaicWithTexture(10, pMosaicTexture);
                    Message message = new Message();
                    message.what = 1;
                    mHandler.sendMessage(message);
                } catch (Exception e) {
                }
            }
        }.show();
    }

    // luobiao
    private void initData() {
        // 初始化
        m_tool = mMosaicView.getMoasicTools();
        mMosaiclistview.setOnItemClickListener(this);
        getCurrentAdapter();
        mMosaiclistview.setAdapter(mListAdapter);
        m_tool.procMosaic(10);
        firstBitmap =  m_tool.getShowProcImage();
        mWidth = firstBitmap.getWidth();
        mHeight = firstBitmap.getHeight();
        mMosaicView.setCurrBitmap(firstBitmap);
    }
    
    private void hidi()
    {
        mMosaiclistview.setVisibility(View.GONE);
        mMosaiclistview.startAnimation(AnimationUtils.loadAnimation(this, R.anim.mosaic_adapter_dismiss));
        mMosaicshow = false;
    }
    
    private void show()
    {
        mMosaiclistview.setVisibility(View.VISIBLE);
        mMosaiclistview.startAnimation(AnimationUtils.loadAnimation(this, R.anim.mosaic_adapter_show));
        mMosaicshow = true;
    }

    private void findView() {
        mMosaiclistview = (HorizontalListView) findViewById(R.id.thumb_listview);
        mPaintRadioButton = (RadioButton) findViewById(R.id.rbtn_paint);
        mEraserRadioButton = (RadioButton) findViewById(R.id.rbtn_eraser);
        mMosaicImage = (ImageView) findViewById(R.id.imgv_mosaic_icon);
        mMosaicStyle = (RelativeLayout) findViewById(R.id.layout_mosaic_style);
        mMosaicView = (MosaicView) findViewById(R.id.mosaic_view);
        mPaintRadioButton.setPressed(true);
        mPaintRadioButton.setOnClickListener(this);
        mEraserRadioButton.setOnClickListener(this);
        mMosaicStyle.setOnClickListener(this);
        findViewById(R.id.btn_ok).setOnClickListener(this);
        findViewById(R.id.btn_cancel).setOnClickListener(this);
        ((TextView) findViewById(R.id.label_top_bar_title)).setText(R.string.mainmenu_mosaic);
        
        dragCircleSeekBar = (DragCircleSeekBar)findViewById(R.id.seekbar);
        dragCircleSeekBar.setDragListener(this);
    }

    private void getCurrentAdapter() {
        //xiaodaijun PR675113 start
        /*
        Resources res = getResources();
        TypedArray mTypedArray = null;
        List<Map<String, Drawable>> data = new ArrayList<Map<String, Drawable>>();
        mTypedArray = res.obtainTypedArray(R.array.img_mosaic);
        for (int i = 0; i < mTypedArray.length(); i++) {
            Map<String, Drawable> map = new HashMap<String, Drawable>();
            map.put("image", mTypedArray.getDrawable(i));
            data.add(map);
        }
        mListAdapter = new MenuListAdapter(this, data);
        */
        mListAdapter = new MenuListAdapter(this, beautymosaic);
        //xiaodaijun PR675113 end
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        mosaicId = position;
        mListAdapter.notifyDataSetChanged();
        //xiaodaijun PR675113 start
        //mMosaicImage.setBackground(mListAdapter.getItem(position));
        mMosaicImage.setBackgroundResource(mListAdapter.getItem(position));
        //xiaodaijun PR675113 end
        if(position==0)
        {
            mMosaicView.setCurrBitmap(firstBitmap);
            m_tool.procMosaic(10);// xiaodaijun PR641127 add
        }
        else
        {
            mMosaicView.setCurrBitmap(getTexture(mosaicId-1));
        }
        
        mMosaicView.changeMosaicPaint();
    }
    public Bitmap getTexture(int pos){
        Bitmap tempBitmap = null;
        try{
        pMosaicTexture = BitmapFactory.decodeStream(getAssets().open(ASSETS_MOSAIC[pos]));
        
        m_tool.procMosaicWithTexture(10,pMosaicTexture);
        }catch(Exception e){e.printStackTrace();}
        return m_tool.getShowProcImage();
    }
    class MenuListAdapter extends BaseAdapter {

        private Context mContext;
        //xiaodaijun PR675113 start
        //private List<Map<String, Drawable>> mData = new ArrayList<Map<String, Drawable>>();
        private List<Integer> mData = new ArrayList<Integer>();
        //xiaodaijun PR675113 end
        private LayoutInflater layoutInflater;

        //xiaodaijun PR675113 start
        /*
        public MenuListAdapter(Context context, List<Map<String, Drawable>> data) {
            mContext = context;
            layoutInflater = LayoutInflater.from(mContext);
            mData = data;
        }
        */
        public MenuListAdapter(Context context, int[] image) {
            mContext = context;
            layoutInflater = LayoutInflater.from(mContext);
            for (int i :image){
                mData.add(i);
            }
        }
        //xiaodaijun PR675113 end

        @Override
        public int getCount() {
            return mData.size();
        }

        @Override
        //xiaodaijun PR675113 start
        /*
        public Drawable getItem(int position) {
            return mData.get(position).get("image");
        }
        */
        public Integer getItem(int position) {
            return mData.get(position);
        }
        //xiaodaijun PR675113 end

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            convertView = layoutInflater.inflate(R.layout.mosaic_adapter, null);
            RelativeLayout relative = (RelativeLayout) convertView.findViewById(R.id.bg);
            ImageView imageview = (ImageView) convertView.findViewById(R.id.galleryImage);
            //xiaodaijun PR675113 start
            //imageview.setBackground(mData.get(position).get("image"));
            imageview.setBackgroundResource(mData.get(position));
            //xiaodaijun PR675113 end
            if (position == mosaicId) {
                relative.setBackgroundResource(R.drawable.image_select);
            }
            return convertView;
        }
    }
    @Override
    public void onStopDrag(int pos) {
        mMosaicView.setPenSize(pos);
        mMosaicView.isShowCenterPen = true;
        mMosaicView.invalidate();
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                mHandler.sendEmptyMessage(2);
            }
        }, 500);
    }
}
