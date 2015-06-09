package com.jrdcom.android.gallery3d.ui;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
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
import com.jrdcom.mt.core.ToolFrame;
import com.jrdcom.mt.mtxx.controls.MtprogressDialog;
import com.jrdcom.mt.util.MyData;
import com.mt.mtxx.image.NDKUtil;

public class BeautyFrameActivity extends MTActivity implements OnClickListener,
        RadioGroup.OnCheckedChangeListener, OnItemClickListener {

    // 美图必备 边框的工具定义
    private ToolFrame m_tool;

    private ImageView mPreview;
    private HorizontalListView mListView = null;
    private RadioGroup rg = null;
    private RadioButton rbtn_simple_frame = null;
    private RadioButton rbtn_color_frame = null;
    private ImageButton cancle_btn = null;
    private ImageButton ok_btn = null;
    private boolean isframe=true;;
    private int effectId=0;
    private MenuListAdapter mMenuListAdapter; 
    private int destiny = 1;
    private ImageView mTriangleView;
    private float[] smallLocations = {0f,0f};
    private boolean isFocus = false;
    private int densityDpi = 240;//PR666193-tao li 001

    // public List<String> image_frame1=new ArrayList<String>();

    private String[] image_frame = {
            "img_frame/10019000.mtxbk", "img_frame/10019001.mtxbk", "img_frame/10019002.mtxbk",
            "img_frame/10019003.mtxbk", "img_frame/10019004.mtxbk", "img_frame/10019005.mtxbk",
            "img_frame/10019006.mtxbk", "img_frame/10019007.mtxbk", "img_frame/10019008.mtxbk",
            "img_frame/10019011.mtxbk", "img_frame/10019012.mtxbk", "img_frame/10019013.mtxbk"
    };

    private String[] image_pic_frame = {
            "img_frame/10019000.thu", "img_frame/10019001.thu", "img_frame/10019002.thu",
            "img_frame/10019003.thu", "img_frame/10019004.thu", "img_frame/10019005.thu",
            "img_frame/10019006.thu", "img_frame/10019007.thu", "img_frame/10019008.thu",
            "img_frame/10019011.thu", "img_frame/10019012.thu", "img_frame/10019013.thu"
    };

    private String[] image_name = new String[12];
    
    
    private String[] color_frame_zoom = {
            "colorful/10029000.thu", "colorful/10029001.thu", "colorful/10029002.thu",
            "colorful/10029003.thu", "colorful/10029004.thu", "colorful/10029005.thu"
    };
    
    private String[] color_frame = {
            "colorful/10029000.jpg", "colorful/10029001.jpg", "colorful/10029002.jpg",
            "colorful/10029003.jpg", "colorful/10029004.jpg", "colorful/10029005.jpg"
    };
    
    
    private String[] xuancai_name = new String[6];

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        setContentView(R.layout.beauty_frame);

        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);
        densityDpi = metrics.densityDpi;//PR666193-tao li 001
        destiny = metrics.densityDpi/160;
        // 美图必备 边框工具的初始化
        m_tool = new ToolFrame();
        m_tool.init(MyData.getJNI());

        NDKUtil.setConext(this);

        mPreview = (ImageView) findViewById(R.id.imgv_effect);
        mTriangleView = (ImageView) findViewById(R.id.imageview_triangle);
        mPreview.setImageBitmap(m_tool.getShowProcImage());
        mListView = (HorizontalListView) findViewById(R.id.thumb_listview);
        rg = (RadioGroup) findViewById(R.id.bottom_menu);
        rg.setOnCheckedChangeListener(this);
        rg.check(R.id.rbtn_simple_frame);
        rbtn_simple_frame = (RadioButton) findViewById(R.id.rbtn_simple_frame);
        rbtn_color_frame = (RadioButton) findViewById(R.id.rbtn_color_frame);
        ((TextView) findViewById(R.id.label_top_bar_title)).setText(R.string.mainmenu_frame);
        cancle_btn = (ImageButton) findViewById(R.id.btn_cancel);
        ok_btn = (ImageButton) findViewById(R.id.btn_ok);
        cancle_btn.setOnClickListener(this);
        ok_btn.setOnClickListener(this);
        image_name = getResources().getStringArray(R.array.img_simple_frame_titles);
        xuancai_name = getResources().getStringArray(R.array.img_colorful_frame_titles);

        mMenuListAdapter = new MenuListAdapter(this, image_pic_frame,image_name);
        mListView.setAdapter(mMenuListAdapter);
        mListView.setOnItemClickListener(this);
    }

    // 自定义Handler 继随自Handler，需要实现handleMessage方法
    Handler frameHandler = new Handler() {
        public void handleMessage(Message msg) {
            mPreview.setImageBitmap(m_tool.getShowProcImage());
            super.handleMessage(msg);
        }
    };

    private void initSmallImgaeLocation() {
        smallLocations[0] = rbtn_simple_frame.getX() + rbtn_simple_frame.getWidth() / 2 - mTriangleView.getWidth() / 2;
        smallLocations[1] = rbtn_color_frame.getX() + rbtn_color_frame.getWidth() / 2 - mTriangleView.getWidth()
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
            case R.id.rbtn_simple_frame:
                isframe=true;
                effectId=-1;
                mTriangleView.setX(smallLocations[0]);
                mMenuListAdapter = new MenuListAdapter(this, image_pic_frame,image_name);
                mListView.setAdapter(mMenuListAdapter);
                break;
            case R.id.rbtn_color_frame:
                isframe=false;
                effectId=-1;
                mTriangleView.setX(smallLocations[1]);
                mMenuListAdapter = new MenuListAdapter(this, color_frame_zoom,xuancai_name);
                mListView.setAdapter(mMenuListAdapter);
                break;
            default:
                break;
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        // TODO Auto-generated method stub
        effectId = position;
        mMenuListAdapter.notifyDataSetChanged();
        if(isframe){
            addFrame(image_frame[position]);
        }else{
            addFrame3(color_frame[position]);
        }

    }

    // 保存
    private void onOK() {
        new MtprogressDialog(this) {
            @Override
            public void process() {
                try {
                    if (m_tool.isProcessed()) {
                        // 美图必备
                        m_tool.ok();
                        // 这个目前没用
                        MyData.getBeautyControl().pushImage();
                    } else {
                        m_tool.cancel();
                    }

                    finish();
                } catch (Exception e) {
                }
            }
        }.show();
    }

    // 取消
    private void onCancel() {
        // 美图必备 取消功能
        m_tool.cancel();

        finish();
    }

    // 原图
    private void oriPic() {
        new MtprogressDialog(this) {
            @Override
            public void process() {
                try {
                    // 美图必备 恢复为原图
                    m_tool.procImageWithResetToOral();

                    Message message = new Message();// 发送消息来刷新
                    frameHandler.sendMessage(message);
                } catch (Exception e) {
                }
            }
        }.show();

    }

    /**
     * 简单边框
     */
    // 添加边框示例1
    private void addFrame(final String path) {
        // 假设素材在asset目录下的
        new MtprogressDialog(this) {
            @Override
            public void process() {
                try {
                    // 简单边框
                    // 美图必备 假设在asset目录下的简单边框的素材，即apk自带的,直接将
                    // String strPath = "img_frame/10019000.mtxbk";
                    m_tool.procImageWithSimleFrame(path, false, true);

                    Message message = new Message();// 发送消息来刷新
                    frameHandler.sendMessage(message);
                } catch (Exception e) {
                }
            }
        }.show();
    }

     // 添加边框示例3
     private void addFrame3(final String path) {
     // 假设在asset目录下的素材的调用
     new MtprogressDialog(this) {
     @Override
     public void process() {
     try {
     // 美图必备 假设在asset目录下的炫彩边框的素材,即APK自带的
     m_tool.procImageWithColorFrame(path, 1, false, true);
    
     Message message = new Message();// 发送消息来刷新
     frameHandler.sendMessage(message);
     } catch (Exception e) {
     }
     }
     }.show();
     }

    class MenuListAdapter extends BaseAdapter {

        private Context mContext;
        private LayoutInflater layoutInflater;
        List<String> image_frame_temp = new ArrayList<String>();
        List<String> name_temp=new ArrayList<String>();

        public MenuListAdapter(Context context, String[] image,String[] name) {
            mContext = context;
            layoutInflater = LayoutInflater.from(mContext);
            image_frame_temp = Arrays.asList(image); 
            name_temp = Arrays.asList(name);
        }

        @Override
        public int getCount() {
            return image_frame_temp.size();
        }

        @Override
        public String getItem(int position) {
            return image_frame_temp.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            convertView = layoutInflater.inflate(R.layout.effect_item, null);
            LinearLayout relative = (LinearLayout) convertView.findViewById(R.id.bg);
            ImageView imageview = (ImageView) convertView.findViewById(R.id.menu_image);
            TextView textView = (TextView) convertView.findViewById(R.id.menu_text);
            textView.setText(name_temp.get(position));
            //PR625036-taoli-begin 001
            InputStream iStream = null;
            InputStream is = null;
            Bitmap bitmap;
            try {
                iStream = getApplicationContext().getAssets().open(image_frame_temp.get(position));
                BitmapFactory.Options options = new BitmapFactory.Options();
                options.inJustDecodeBounds = true;
                bitmap = BitmapFactory.decodeStream(iStream, null, options);
                int w = options.outWidth;
                int h = options.outHeight;
                w = w*(destiny>2?(destiny-1):destiny);
                h = h*(destiny>2?(destiny-1):destiny);
                is = getApplicationContext().getAssets().open(image_frame_temp.get(position));
                bitmap = BitmapFactory.decodeStream(is);
                //PR666193-tao li-begin 001
                if (densityDpi < 240) {//240 small screen dpi
                    imageview.setLayoutParams(new LinearLayout.LayoutParams(50, 50));
                }else {
                    imageview.setLayoutParams(new LinearLayout.LayoutParams(w, h));
                }
                //PR666193-tao li-end 001
                imageview.setImageBitmap(bitmap);
            } catch (IOException e) {
                e.printStackTrace();
            }finally{
                if (iStream != null) {
                    try {
                        iStream.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                if (is != null) {
                    try {
                        is.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
           //PR625036-taoli-end 001
            if (position == effectId) {
                relative.setBackgroundResource(R.drawable.image_select);
                textView.setTextColor(getResources().getColor(R.color.effect_text_color));
            }
            return convertView;
        }
    }
}
