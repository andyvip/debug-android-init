package com.jrdcom.android.gallery3d.ui;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.TextView;

import com.jrdcom.android.gallery3d.R;
import com.jrdcom.example.joinpic.HorizontalListView;
import com.jrdcom.mt.MTActivity;
import com.jrdcom.mt.core.BitmapUtil;
import com.jrdcom.mt.core.ToolEffect;
import com.jrdcom.mt.mtxx.controls.MtprogressDialog;
import com.jrdcom.mt.util.MyData;
import com.jrdcom.mt.widget.VSeekBar;
import com.jrdcom.mt.widget.VSeekBar.OnProgressChangedListener;
//PR936992 Undo and Redo icon are useless,while SD card is full add by limin.zhuo at 2015.03.06 begin
import com.jrdcom.example.joinpic.Utils;
//PR936992 Undo and Redo icon are useless,while SD card is full add by limin.zhuo at 2015.03.06 end
/**
 * 特效
 */
public class BeautyEffectActivity extends MTActivity implements OnClickListener,
        OnCheckedChangeListener,
        OnItemClickListener,
        OnProgressChangedListener
        {

    private static final String TAG = "BeautyEffectActivity"; //PR669201-taoli 001
    private VSeekBar verticalSeekBar;
    
    private TextView seekbarTextView;
    private ImageView imageView2;
    private float[] smallLocations = {0f,0f,0f,0f};
    private boolean isFocus = false;
    
    
    /** handler消息标记 */
    private static final int MSG_RESRESH_VIEW = 0x101;
    private static final int TIMER_EXIT = 0x102;

    /** 特效类别 0：LOMO 1 人像 2：时尚 3: 艺术 */
    private int mEffectType = -1;

    /** 当前特效id */
    private int nCurrentimgEffectId = 0;
    /** 是否正在处理特效 */
    private boolean isProcessing = false;
    /** 添加特效时图片处理等待的ProgressBar */
    private ProgressBar effectBar;

    private HorizontalListView mListView = null;

    /** 展示图片的imamgeview */
    private ImageView imgvSrc = null;
    /*original image below effect image*/
    private ImageView imgvSrc_bottom = null;
    /** 展示图片的imamgeview的bitmap */
    private Bitmap m_pViewImage = null;
    /** 特效的工具类 */
    private ToolEffect m_tool;

    private MenuListAdapter mCurrentAdapter;
    private int effectId = 0;
    private float mAlpha = 1.0f;//PR636797-taoli 001

    /**
     * 特效的编号，0原图，1经典lomo，2淡雅，3胶片，4复古,5亮红，6日系,7阿宝色,8印象，
     * 11HDR，12老照片，13古铜色，15牛皮纸,17哥特风,19平安夜
     * 21飞雪,22夜景,23七彩光晕,24暖洋洋，25反转色,26粉红佳人,28黑白，29柔光
     * 30日光,31时光隧道,32移轴,33写生素描，34古典素描,35彩铅，36油画 44甜美可人 54 80S，55苦艾,58流年
     * 62粉嫩系,63自然，64清凉,71唯美，75动漫
     */
    private int[] m_nArrLomo = {
            0, 1, 3, 2, 4, 55, 54, 8, 11, 12, 13, 26, 17, 25, 32
    };
    private int[] m_nArrYingLou = {
            0, 29, 62, 63, 44, 64, 71, 6, 7, 58, 28, 24
    };
    private int[] m_nArrShiShang = {
            0, 5, 30, 19, 22, 21, 23, 31
    };
    private int[] m_nArrYiShu = {
            0, 33, 34, 35, 36, 75, 15
    };

    // 自定义Handler 继随自Handler，需要实现handleMessage方法
    Handler effectHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_RESRESH_VIEW:
                setEffectBarGone();
                refreshImageView();
                break;
            case TIMER_EXIT:
                break;
        }
        super.handleMessage(msg);
    }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        setContentView(R.layout.beauty_effect);
        findView();
        initData();

        // 美图必备 特效功能初始化函数
        m_tool = new ToolEffect();
        m_tool.init(MyData.getJNI());

        // 美图必备 获得UI要展示的图片,这个是功能的原图
//        m_pViewImage = m_tool.getShowOralImage();
        m_pViewImage = m_tool.getShowProcImage();
        imgvSrc_bottom.setImageBitmap(m_tool.getShowOralImage());
        refreshImageView();
    }

    private void initData() {
        mListView.setOnItemClickListener(this);
        getCurrentAdapter(mEffectType);
        mListView.setAdapter(mCurrentAdapter);
    }
    public void initSmallImgaeLocation()
    {
        RadioButton rblomo =  (RadioButton) findViewById(R.id.rbtn_effect_lomo);
        RadioButton rbying =  (RadioButton) findViewById(R.id.rbtn_effect_yinglou);
        RadioButton rbshi =  (RadioButton) findViewById(R.id.rbtn_effect_shishang);
        RadioButton rbyishu =  (RadioButton) findViewById(R.id.rbtn_effect_yishu);
        smallLocations[0] = rblomo.getX()+rblomo.getWidth()/2-imageView2.getWidth()/2;
        smallLocations[1] = rbying.getX()+rbying.getWidth()/2-imageView2.getWidth()/2;
        smallLocations[2] = rbshi.getX()+rbshi.getWidth()/2-imageView2.getWidth()/2;
        smallLocations[3] = rbyishu.getX()+rbyishu.getWidth()/2-imageView2.getWidth()/2;
        imageView2.setX(smallLocations[0]);
    }
    private void findView() {

        // Setting Button Actions.
        findViewById(R.id.btn_ok).setOnClickListener(this);
        findViewById(R.id.btn_cancel).setOnClickListener(this);
        imgvSrc = (ImageView) findViewById(R.id.imgv_effect);
        imgvSrc_bottom = (ImageView) findViewById(R.id.imgv_effect_bottom);
        ((TextView) findViewById(R.id.label_top_bar_title)).setText(R.string.mainmenu_effect);
        // find the thumb list view.
        mListView = (HorizontalListView) findViewById(R.id.thumb_listview);
        effectBar = (ProgressBar) findViewById(R.id.effectBar);
        imageView2 = (ImageView)findViewById(R.id.imageView2);
        // 设置RadioGroup 监听噄1�7
        RadioGroup rg = (RadioGroup) findViewById(R.id.bottom_menu);
        rg.setOnCheckedChangeListener(this);
        rg.check(R.id.rbtn_effect_lomo);
        verticalSeekBar = (VSeekBar) findViewById(R.id.seekbar);
        verticalSeekBar.setProgressChangedListener(this);
        seekbarTextView = (TextView)findViewById(R.id.tvw_alpha_seekbar_switch);
        seekbarTextView.setText("100%");
        switchVseekbarVisibility(0);
        effectId = 0;
    }
    
    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        // TODO Auto-generated method stub
        super.onWindowFocusChanged(hasFocus);
        if(!isFocus)
        {
            isFocus = true;
            initSmallImgaeLocation();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        //PR669201-taoli-begin 001
        if (m_pViewImage == null) {
            Log.e(TAG, "onResume" + "   m_pViewImage: " + m_pViewImage);
            finish();
        }
        //PR669201-taoli-end 001
    }
    // 刷新界面
    private void refreshImageView() {
        imgvSrc.setImageBitmap(m_pViewImage);
        imgvSrc.invalidate();
    }

    // 特效处理函数
    private void processImageBitmap() {

        new MtprogressDialog(this) {
            @Override
            public void process() {
                try {
                    isProcessing = true;

                    // 美图必备 应用某个特效的时候调用
                    // nCurrentimgEffectId为特效编号，具体可以看ToolEffect文件尾说明
                    // 参数二：true 为应用的为UI显示的，false为真实图操作
                    m_tool.procImage(nCurrentimgEffectId, true);
                    // 获取应用编号为nCurrentimgEffectId的特效后的UI效果图
                    m_pViewImage = m_tool.getShowProcImage();

                    isProcessing = false;
                    Message message = new Message();// 生成消息，并赋予ID倄1�7
                    message.what = MSG_RESRESH_VIEW;
                    effectHandler.sendMessage(message);// 投�1�7�消恄1�7

                } catch (Exception e) {
                }
            }
        }.show();
    }

    final public void setEffectBarGone() {
        effectBar.setVisibility(View.INVISIBLE);
    }

    final public void setEffectBarVisible() {
        effectBar.setVisibility(View.VISIBLE);
    }

    // 返回主activity
    public void toMain() {

        // 美图必备 判断是否处理过，主要是为了提高效率用。当点击保存后，如果没进行改变过，就不进行任何操作
        if (!m_tool.isProcessed()) {
            doActionCacnel();
            return;
        }

        new MtprogressDialog(this) {
            @Override
            public void process() {
                try {

                    // 美图必备 保存按钮的操作
                    m_tool.setEffectAlpha(mAlpha);//PR636797-taoli 001
                    m_tool.ok();
                    // 这个目前没用
                    MyData.getBeautyControl().pushImage();

                    BeautyEffectActivity.this.finish();
                } catch (Exception e) {
                }
            }
        }.show();
    }

    public void onDestroy() { // Activity结束回调用此方法
        try {
            imgvSrc.setImageBitmap(null);
            BitmapUtil.SafeRelease(m_pViewImage);
            System.gc();
        } catch (Exception e) {
        }
        super.onDestroy();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {

            // 美图必备 点击手机硬件返回按钮必须调用
            m_tool.cancel();

            this.finish();
            return true;
        } else if (keyCode == KeyEvent.KEYCODE_MENU) {
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    /**
     * 展示艺术特效缩略囄1�7
     */
    private void doActionYishu() {
        if (mEffectType == 3) {
            return;
        }

        mEffectType = 3;
        getCurrentAdapter(mEffectType);
        mListView.setAdapter(mCurrentAdapter);
        int position = getPositionByEffectId(mEffectType);

        mListView.setSelection(position);
    }

    /**
     * 展示时尚数据
     */
    private void doActionShishang() {
        if (mEffectType == 2) {
            return;
        }

        mEffectType = 2;
        getCurrentAdapter(mEffectType);
        mListView.setAdapter(mCurrentAdapter);
        int position = getPositionByEffectId(mEffectType);

        mListView.setSelection(position);
    }

    /**
     * Action for yinglou. 展示影楼对应的数捄1�7
     */
    private void doActionYinglou() {
        if (mEffectType == 1) {
            return;
        }

        mEffectType = 1;
        getCurrentAdapter(mEffectType);
        mListView.setAdapter(mCurrentAdapter);
        int position = getPositionByEffectId(mEffectType);

        mListView.setSelection(position);
    }

    /**
     * Action for LOMO, reload gallery data. 重新加载LOMO的Gallery展示数据
     */
    private void doActionLomo() {
        if (mEffectType == 0) {
            return;
        }
        mEffectType = 0;
        getCurrentAdapter(mEffectType);
        mListView.setAdapter(mCurrentAdapter);
        int position = getPositionByEffectId(mEffectType);

        mListView.setSelection(position);
    }

    @Override
    public void onClick(View v) {
        // Action for all buttons when they were clicked.
        int id = v.getId();
        isClicked = true; // added by jipu.xiong@tcl.com
        if (id == R.id.btn_ok) {
              //PR936992 Undo and Redo icon are useless,while SD card is full add by limin.zhuo at 2015.03.06 begin
              if (!Utils.updateCacheDirEditPicture()) {
                    Utils.showToast(this, R.string.storage_full_tag);
                }
              //PR936992 Undo and Redo icon are useless,while SD card is full add by limin.zhuo at 2015.03.06 end
            // User touch OK button.
            doActionOk();
        } else if (id == R.id.btn_cancel) {
            // User touch CANCEL button.
            doActionCacnel();
        } else {

        }
    }

    /**
     * Action for click on Cancel.
     */
    private void doActionCacnel() {
        if (isProcessing) {
            return;
        }

        // 美图必备 点击界面上的返回按钮必须调用
        m_tool.cancel();

        BeautyEffectActivity.this.finish();
    }

    /**
     * Action for click on OK.
     */
    private void doActionOk() {
        if (isProcessing) {
            return;
        }
        toMain();
    }

    private int getPositionFromArr(int[] nArrTemp) {
        int len = nArrTemp.length;
        for (int i = 1; i < len; i++) {
            if (nCurrentimgEffectId == nArrTemp[i]) {
                return i;
            }
        }
        return -1;
    }

    private int getPositionByEffectId(int effectType) {
        int position = -1;
        if (nCurrentimgEffectId == 0) {
            position = 0;
        } else if (effectType == mEffectType) {
            switch (effectType) {
                case 0: {
                    position = getPositionFromArr(m_nArrLomo);
                }
                    break;
                case 1: {
                    position = getPositionFromArr(m_nArrYingLou);
                }
                    break;
                case 2: {
                    position = getPositionFromArr(m_nArrShiShang);
                }
                    break;
                case 3: {
                    position = getPositionFromArr(m_nArrYiShu);
                }
                    break;
            }

        }
        return position;

    }

    private boolean getCurrentAdapter(int mEffectType) {

        Resources res = getResources();
        String[] titles = null;
        TypedArray mTypedArray = null;

        List<Map<String, String>> data = new ArrayList<Map<String, String>>();
        List<Map<String, Drawable>> data1 = new ArrayList<Map<String, Drawable>>();
        switch (mEffectType) {
            case 0:
                titles = res.getStringArray(R.array.img_effect_lomo_titles);
                mTypedArray = res.obtainTypedArray(R.array.effect_layout_lomo_list);
                break;
            case 1:
                titles = res.getStringArray(R.array.img_effect_yinglou_titles);
                mTypedArray = res.obtainTypedArray(R.array.img_effect_yinglou_thumbs);
                break;
            case 2:
                titles = res.getStringArray(R.array.img_effect_shishang_titles);
                mTypedArray = res.obtainTypedArray(R.array.img_effect_shishang_thumbs);
                break;
            case 3:
                titles = res.getStringArray(R.array.img_effect_yishu_titles);
                mTypedArray = res.obtainTypedArray(R.array.img_effect_yishu_thumbs);
                break;
        }
        for (int i = 0; i < titles.length; i++) {
            Map<String, String> map = new HashMap<String, String>();
            map.put("title", titles[i]);
            data.add(map);
            Map<String, Drawable> map1 = new HashMap<String, Drawable>();
            map1.put("image", mTypedArray.getDrawable(i));
            data1.add(map1);
        }
        mCurrentAdapter = new MenuListAdapter(this, data, data1);
//        mCurrentAdapter = new MenuListAdapter(this, data, android.R.layout.simple_gallery_item,
//                new String[] {
//                    "title"
//                },
//                new int[] {
//                    android.R.id.text1
//                });

        return true;
    }

    @Override
    public void onCheckedChanged(RadioGroup group, int checkedId) {
        // 响应点击变化
        if (checkedId == R.id.rbtn_effect_lomo) {
            // 点击LOMO
            effectId=-1;
            imageView2.setX(smallLocations[0]);
            doActionLomo();
        } else if (checkedId == R.id.rbtn_effect_yinglou) {
            // 人像
            effectId=-1;
            imageView2.setX(smallLocations[1]);
            doActionYinglou();
        } else if (checkedId == R.id.rbtn_effect_shishang) {
            // 时尚
            effectId=-1;
            imageView2.setX(smallLocations[2]);
            doActionShishang();
        } else if (checkedId == R.id.rbtn_effect_yishu) {
            // 艺术
            effectId=-1;
            imageView2.setX(smallLocations[3]);
            doActionYishu();
        } else {
            // 无效 id
            // do nothing.
        }
    }

    @Override
    public void onItemClick(AdapterView<?> arg0, View arg1, int position, long arg3) {
        try {
            if (isProcessing) {
                return;
            }
            effectId = position;
            switchVseekbarVisibility(position);
            
            mCurrentAdapter.notifyDataSetChanged();
            switch (mEffectType) {
                case 0:// lomo
                    nCurrentimgEffectId = m_nArrLomo[position];
                    break;
                case 1:// 人像
                    nCurrentimgEffectId = m_nArrYingLou[position];
                    break;
                case 2:// 时尚
                    nCurrentimgEffectId = m_nArrShiShang[position];
                    break;
                case 3:// 艺术
                    nCurrentimgEffectId = m_nArrYiShu[position];
                    break;
            }
            // 处理函数
            processImageBitmap();
        } catch (Exception e) {
            Log.e("error", e.toString());
        }
    }

    class MenuListAdapter extends BaseAdapter {

        private Context mContext;
        private List<Map<String, String>> mData = new ArrayList<Map<String, String>>();
        private List<Map<String, Drawable>> mData1 = new ArrayList<Map<String, Drawable>>();
        private LayoutInflater layoutInflater;

        public MenuListAdapter(Context context, List<Map<String, String>> data, List<Map<String, Drawable>> data1) {
            mContext = context;
            layoutInflater = LayoutInflater.from(mContext);
            mData = data;
            mData1 = data1;
        }

        @Override
        public int getCount() {
            return mData.size();
        }

        @Override
        public String getItem(int position) {
            return mData.get(position).get("title");
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            convertView = layoutInflater.inflate(R.layout.effect_item,null);
            LinearLayout relative = (LinearLayout) convertView.findViewById(R.id.bg);
            ImageView imageview = (ImageView) convertView.findViewById(R.id.menu_image);
            TextView textView = (TextView) convertView.findViewById(R.id.menu_text);
            textView.setText(mData.get(position).get("title"));
            imageview.setBackground(mData1.get(position).get("image"));
            if(position==effectId){
                relative.setBackgroundResource(R.drawable.image_select);
                textView.setTextColor(getResources().getColor(R.color.effect_text_color));
            }
            return convertView;
        }
    }
    @Override
    public void progressChanged(int progress)
    {
        seekbarTextView.setText(progress+"%");
    }
    @Override
    public void startTracking()
    {
        
    }
    @Override
    public void stopTracking()
    {
        //PR636797-taoli-begin 001
        mAlpha = verticalSeekBar.getProgress()/(verticalSeekBar.getMax()*1.0f);
        //mAlpha = 2.55f * mAlpha;
        imgvSrc.setAlpha(mAlpha);
        //PR636797-taoli-end 001
    }
    public void switchVseekbarVisibility(int isShow)
    {
        if(isShow == 0)
        {
            verticalSeekBar.setVisibility(View.GONE);
            seekbarTextView.setVisibility(View.GONE);
        }
        else
        {
            verticalSeekBar.setVisibility(View.VISIBLE);
            seekbarTextView.setVisibility(View.VISIBLE);
        }
    }
}
