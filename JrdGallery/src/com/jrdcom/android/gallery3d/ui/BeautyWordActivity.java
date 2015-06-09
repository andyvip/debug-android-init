
package com.jrdcom.android.gallery3d.ui;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import android.R.integer;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.*;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.*;
import android.widget.TextView;
import android.widget.Toast;

import com.jrdcom.mt.MTActivity;
import com.jrdcom.mt.util.MyData;
import com.jrdcom.mt.core.ToolText;
import com.jrdcom.mt.mtxx.controls.MtprogressDialog;
import com.jrdcom.mt.mtxx.tools.BitmapOperate;
import com.jrdcom.mt.mtxx.tools.ToolBubble;
import com.jrdcom.android.gallery3d.R;
import com.jrdcom.mt.widget.ViewEditWords;
import com.jrdcom.example.joinpic.HorizontalListView;
//PR936992 Undo and Redo icon are useless,while SD card is full add by limin.zhuo at 2015.03.06 begin
import com.jrdcom.example.joinpic.Utils;
//PR936992 Undo and Redo icon are useless,while SD card is full add by limin.zhuo at 2015.03.06 end
import com.jrdcom.mt.widget.ColorSelectorView;
import com.jrdcom.mt.widget.ColorSelectorView.OnColorChangedListener;
import android.widget.AdapterView.OnItemClickListener;
import java.util.*;
import android.graphics.Matrix;
import android.widget.ToggleButton;
import  android.graphics.*;

public class BeautyWordActivity extends MTActivity implements OnClickListener,
        ViewEditWords.OnViewEditWordsTouchListener, OnItemClickListener, OnColorChangedListener
{

    private HorizontalListView mBubblelistview;
    private RelativeLayout edit_font_layout;
    private BubbleAdapter bubbleAdapter;
    private Bitmap orgBubbleBitmap;
    private Button mWordsButton;
    private RadioButton mBubbleWordsButton;
    private RadioButton mEditWordsButton;
    private RadioGroup radioGroup;
    private String mEditString;
    //PR501920-lilei-begin
    private RelativeLayout tipinfoImageView;
    //PR501920-lilei-end
    private Bitmap nullBitmap;
    private int mSelectedBgPos = 0;

    private boolean isEditWord = false;
    
    
    ColorSelectorView colorSelectorView;
    private final String image_root = "Bubble/";

    private final String[] imagePaths = {
    		"10039000.thu",
            "10039001.thu", "10039002.thu", "10039003.thu", "10039004.thu","10039005.thu",
            "10039006.thu","10039007.thu", "10039008.thu", "10039009.thu", "10039010.thu"
    };
    // meterials
    private final String[] meterials = {
    		"10039000.mtqp",
            "10039001.mtqp", "10039002.mtqp", "10039003.mtqp", "10039004.mtqp","10039005.mtqp",
            "10039006.mtqp","10039007.mtqp", "10039008.mtqp", "10039009.mtqp", "10039010.mtqp"
    };
    private Map<Integer, Bitmap> meterialDynamicCache = new HashMap<Integer, Bitmap>();

    private Bitmap[] bubbleImages;

    public static final int EDIT_WORDS_ACTIVITY = 100;
    public static final int MODIFY_WORDS_ACTIVITY = 101;
    // meitu necessary
    private ToolText m_tool;
    //the coordiates showing on UI images
    float m_x = 0.0f;
    float m_y = 0.0f;
    float m_scale = 0.8f;// 1.0f;
    int destiny = 1;
    /** bubble material for praser class **/
    private ToolBubble mBubbleTool;

    /** the background of bubbles **/
    private Bitmap mBubbleBg;

//    private Bitmap tempBitmap;
    private ViewEditWords viewEditControl;
    private final int VIEW_WORD_REFRESH_WHOLE = 1;
    private final int VIEW_WORD_REFRESH_PARTNAL = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);
        destiny = metrics.densityDpi/160;
        //PR698969-taoli-begin 001
        MyData.nScreenW = metrics.widthPixels;
        MyData.nScreenH = metrics.heightPixels;
        //PR698969-taoli-end 001
        initData();
        setContentView(R.layout.beauty_word);
        findView();
        //yaogang.hao delete tip info because of only image
        tipinfoImageView.setVisibility(View.GONE);
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
            case R.id.btn_addwords:
                Intent intent = new Intent();
                intent.setClass(this, EditWordsActivity.class);
                int action = -1;
                if(isEditWord)//modify words
                {
                    intent.putExtra("words",mEditString);
                    action = MODIFY_WORDS_ACTIVITY;
                }
                else
                {
                    action = EDIT_WORDS_ACTIVITY;
                }
                startActivityForResult(intent, action);
                
                break;
            case R.id.decorations_bubblewords: {
                showBubbles();
                hideEditWord();
                break;
            }
            case R.id.decorations_editwords: {
                showEditWord();
                hideBubbles();

                break;
            }
        }
    }

    public void hideBubbles()
    {
        if (mBubblelistview.getVisibility() == View.VISIBLE)
            mBubblelistview.setVisibility(View.GONE);
    }

    public void showBubbles()
    {
        if (mBubblelistview.getVisibility() == View.GONE)
            mBubblelistview.setVisibility(View.VISIBLE);
    }

    public void hideEditWord()
    {
        if (edit_font_layout.getVisibility() == View.VISIBLE)
            edit_font_layout.setVisibility(View.GONE);

    }

    public void showEditWord()
    {
        if (edit_font_layout.getVisibility() == View.GONE)
            edit_font_layout.setVisibility(View.VISIBLE);
    }

    // luobiao
    private void initData() {
        // init tool text
        m_tool = new ToolText();
        mBubbleTool = new ToolBubble(BeautyWordActivity.this);
        m_tool.init(MyData.getJNI());
        // init common data
        MyData.bmpDst = m_tool.getShowOralImage();
        creatNullBitmap();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode != RESULT_OK)
            return;

        if (requestCode == EDIT_WORDS_ACTIVITY) {
            mEditString = data.getExtras().getString("words").toString();
            // add words to picunit
            addFontString(mEditString);
            // refresh
            refresh();
            tipinfoImageView.setVisibility(View.GONE);
            mBubbleWordsButton.setEnabled(true);
            mEditWordsButton.setEnabled(true);
            isEditWord = true;
            mWordsButton.setText(R.string.edit_word);
        }
        else if(requestCode == MODIFY_WORDS_ACTIVITY)
        {
            mEditString = data.getExtras().getString("words").toString();
            modifyFontString(mEditString);
            refresh();
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    public void refresh()
    {
        Message msg = new Message();
        msg.what = VIEW_WORD_REFRESH_WHOLE;
        wordHandler.sendMessage(msg);
    }
    public void addFontString(String font)
    {
        // test coordinate values
//        Rect rc = new Rect(0, 0, 300, 300);
//        Bitmap word = Bitmap.createBitmap(rc.width(), rc.height(), Bitmap.Config.ARGB_8888);
//        viewEditControl.addPic(nullBitmap, new Rect(0, 0, nullBitmap.getWidth(),nullBitmap.getHeight()), 0, 0);
//        Rect rc = mBubbleTool.getBubbleRc(image_root + meterials[1],false);
       
//        viewEditControl.addPic(nullBitmap,new Rect(0, 0, 524, 286), 0, 0);
        //yaogang.hao
        viewEditControl.addPic(null,new Rect(), 0, 0);
        viewEditControl.setImageString(font);
        setSelWordBG(0);

    }
    public void modifyFontString(String font)
    {
        viewEditControl.setImageString(font);
    }
    
    public void creatNullBitmap()
    {
    	 Rect rc = new Rect(0, 0, 524, 286);
//        Rect rc = mBubbleTool.getBubbleRc(image_root + meterials[1],false);
    	 nullBitmap = Bitmap.createBitmap(rc.width(), rc.height(), Bitmap.Config.ARGB_8888);
    }

    private void findView() {

        mBubblelistview = (HorizontalListView) findViewById(R.id.thumb_listview);
        edit_font_layout = (RelativeLayout) findViewById(R.id.edit_font);

        mWordsButton = (Button) findViewById(R.id.btn_addwords);
        mBubbleWordsButton = (RadioButton) findViewById(R.id.decorations_bubblewords);
        mEditWordsButton = (RadioButton) findViewById(R.id.decorations_editwords);
        mBubbleWordsButton.setEnabled(false);
        mEditWordsButton.setEnabled(false);
        radioGroup = (RadioGroup) findViewById(R.id.decorations_btns);
        //PR501920-lilei-begin
        tipinfoImageView = (RelativeLayout) findViewById(R.id.iv_tip_info);
        //PR501920-lilei-end
        colorSelectorView = (ColorSelectorView) findViewById(R.id.color_sel);
        colorSelectorView.setOnColorChangedListenner(this);
        mWordsButton.setOnClickListener(this);

        findViewById(R.id.btn_ok).setOnClickListener(this);
        findViewById(R.id.btn_cancel).setOnClickListener(this);
        ((TextView) findViewById(R.id.label_top_bar_title)).setText(R.string.mainmenu_word);
        mBubbleWordsButton.setOnClickListener(this);
        mEditWordsButton.setOnClickListener(this);

        viewEditControl = (ViewEditWords) findViewById(R.id.img_text_control_view);
        viewEditControl.setOnViewEditWordsTouchListener(this);
        mBubblelistview.setVisibility(View.GONE);
        edit_font_layout.setVisibility(View.GONE);
        bubbleAdapter = new BubbleAdapter(this, imagePaths);
        mBubblelistview.setAdapter(bubbleAdapter);
        mBubblelistview.setOnItemClickListener(this);
    }

    // self-defined Handler that extends handler parent,and you need to override handlerMessage method
    Handler wordHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {

                case VIEW_WORD_REFRESH_WHOLE:
                {
                    viewEditControl.invalidate();
                    break;
                }
                case VIEW_WORD_REFRESH_PARTNAL:
                {

                    break;
                }

            }
        }
    };

    // to save
    private void onOK() {
        new MtprogressDialog(this) {
            @SuppressLint("NewApi")
            @Override
            public void process() {
                try {
//                    float[] vals = new float[3];
//                    BitmapOperate.FittingWindowSize(tempBitmap.getWidth(), tempBitmap.getHeight(),
//                            mPreview.getWidth(), mPreview.getHeight(), vals);
//
//                    float scale = (float) (mBubblePreview.getWidth()) / vals[0];
//
//                    float bubbleScale = (scale * tempBitmap.getWidth()) / mBubbleBg.getWidth();
////                     int[] data = ImageProcess.bitmap2IntARGB(mBubbleBg);
//                    /** compose the bubble-worded image with the background image **/
//                    m_tool.procImage(mBubbleBg, ((vals[0] - mBubblePreview.getWidth()) / 2)
//                            / vals[0], (vals[1] - mBubblePreview.getHeight()) / 2 / vals[1],
//                            bubbleScale / m_scale);
//                    
                    
//                    float xy[] = viewEditControl.getPercentXY();
//                    
//                    m_tool.procImage(viewEditControl.getResultWorldBitmap(),
//                            xy[0],
//                            xy[1],
//                            viewEditControl.getBubbleScaleValue());
//                    
//                   
                    //yaogang.hao
                    
                    viewEditControl.procImage(m_tool);
                    m_tool.ok();
                    // no used 
                    MyData.getBeautyControl().pushImage();
                    finish();
                } catch (Exception e) {
                }
            }
        }.show();
    }

    // 取消
    private void onCancel() {
        //necessary cancel operate
        m_tool.cancel();
        finish();
    }

    // orignal image
    private void oriPic() {
        new MtprogressDialog(this) {
            @Override
            public void process() {
                try {
                    // recovery to the orignal image
                    m_tool.procImageWithResetToOral();
                    Message message = new Message();// send message to refresh UI
                    wordHandler.sendMessage(message);
                } catch (Exception e) {
                }
            }
        }.show();

    }

    // key listener
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            // back key
        }
        return super.onKeyDown(keyCode, event);
    }

    // when wordText is being dragged to hide controls below
    @Override
    public void onTouchBegan()
    {
        hideBubbles();
        hideEditWord();
        radioGroup.clearCheck();
    }

    // toggle button click
    public void onToggleClicked(View view)
    {
        // Is the toggle on?
        boolean on = ((ToggleButton) view).isChecked();

        switch (view.getId()) {

            case R.id.toggle_blod:
                viewEditControl.setFontItalic(on);
                break;
            case R.id.toggle_italic:
                viewEditControl.setFontShadow(on);
                break;
        }
        viewEditControl.invalidate();
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        mSelectedBgPos = position;
        setSelWordBG(position);
        bubbleAdapter.notifyDataSetChanged();
    }

    public void setSelWordBG(int position)
    {
        Bitmap tempBitmap = null;
        Rect rect = null;
        if(position != 0)
        {
        
	        if (!meterialDynamicCache.containsKey(position))
	        {
	            tempBitmap = mBubbleTool.getBubbleBg(image_root + meterials[position], false);
	        }
	        else
	        {
	            tempBitmap = meterialDynamicCache.get(position);
	        }
	         rect = mBubbleTool.getBubbleRc(image_root + meterials[position],false);
        }
        else
        {
        	tempBitmap = nullBitmap;
        	rect = new Rect(0, 0, 524, 286);
        }
       
        viewEditControl.setImageBubble(tempBitmap, rect);
        viewEditControl.invalidate();
    }

    @Override
    public void onColorChanged(int color) {
        viewEditControl.setFontColor(color);
        viewEditControl.invalidate();
    }
    @Override
    public void onColorChanging(int color)
    {
        
    }

    private class BubbleAdapter extends BaseAdapter
    {
        private Context context;
        private String[] drawables;

        public BubbleAdapter(Context context, String[] drawables)
        {
            this.context = context;
            this.drawables = drawables;

        }

        @Override
        public int getCount() {
            return drawables.length;
        }

        @Override
        public Object getItem(int position) {
            return drawables[position];
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            // to improve the speed of loading image
            ViewHolder holder;
            if (convertView == null)
            {
                convertView = LayoutInflater.from(context).inflate(R.layout.bubble_item, null);

                holder = new ViewHolder();
                holder.bubble = (ImageView) convertView.findViewById(R.id.bubble);
                holder.img_new = (ImageView) convertView.findViewById(R.id.img_new);
                holder.img_select = (ImageView) convertView.findViewById(R.id.select);
                convertView.setTag(holder);
            }
            else {
                holder = (ViewHolder) convertView.getTag();
            }
            InputStream iStream = null;
            Bitmap bitmap;
            try {
                iStream = getApplicationContext().getAssets().open(image_root + drawables[position]);
                BitmapFactory.Options options = new BitmapFactory.Options();
                options.inJustDecodeBounds = true;
                bitmap = BitmapFactory.decodeStream(iStream, null, options);
                int w = options.outWidth;
                int h = options.outHeight;
                w = w*(destiny>2?(destiny-1):destiny);
                h = h*(destiny>2?(destiny-1):destiny);
                // xiaodaijun PR629129 start
                if (null != iStream) {
                    iStream.reset();
                }
                // xiaodaijun PR629129 end
                bitmap = BitmapFactory.decodeStream(iStream);
                holder.bubble.setLayoutParams(new LinearLayout.LayoutParams(w, h));
                holder.bubble.setImageBitmap(bitmap);
            } catch (IOException e) {
                e.printStackTrace();
            }
            // xiaodaijun PR629129 start
            finally{
                try {
                    if (null != iStream) {
                        iStream.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            // xiaodaijun PR629129 end
            
            if(mSelectedBgPos == position)
            {
                holder.img_select.setVisibility(View.VISIBLE);
            }
            else
            {
                holder.img_select.setVisibility(View.INVISIBLE);
            }

            return convertView;
        }

        public class ViewHolder
        {
            ImageView bubble;
            ImageView img_new;
            ImageView img_select;
        }
    }
}
