
package com.jrdcom.example.joinpic;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import com.jrdcom.android.gallery3d.R;
import com.jrdcom.mt.mtxx.tools.BitmapOperate;

//PR814292 modify puzzle choose frame and background overlapped by fengke at 2014.10.21 start
import android.util.DisplayMetrics;
//PR814292 modify puzzle choose frame and background overlapped by fengke at 2014.10.21 end

public class ActivityTemplateSetBg extends Activity {
    com.jrdcom.example.joinpic.GridViewOpen gridViewStyle;
    // Button headerBtn;
    Button btn_cancle;
    //PR814292 modify puzzle choose frame and background overlapped by fengke at 2014.10.21 start
    private double destiny = 1.5;
    //PR814292 modify puzzle choose frame and background overlapped by fengke at 2014.10.21 end
    int i = 0;
    // public static SharedPreferences sharedPrefrences;
    // public static Editor editor;

    //1-18.jpg
    private static final String  path_root = "freedom/";
    private static final String img_suffixString = ".mate";
    private static final int img_sum = 18;
    
    public static String[] mThumbName={
        "10059006","10059007","10059008","10059009","10059010","10059011",
        "10059012","10059013","10059014","10059015","10059016","10059017",
        "10059018","10059019","10059020","10059021","10059022","10059023",
    };
    public  static Bitmap[]  mThumbimages = null;
    
    private Handler handler =  new Handler(){
        public void handleMessage(android.os.Message msg) {
            gridViewStyle.setAdapter(new StyleAdapter(ActivityTemplateSetBg.this));
        };
    };
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.freedomback);
        init();
       

        // sharedPrefrences = this.getSharedPreferences("bg_id",
        // MODE_WORLD_READABLE);// 得到SharedPreferences，会生成user.xml
        // editor = sharedPrefrences.edit();

        gridViewStyle.setOnTouchListener(new OnTouchListener() {

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                // TODO Auto-generated method stub
                return false;
            }

        });

        //PR814292 modify puzzle choose frame and background overlapped by fengke at 2014.10.21 start
        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);
        destiny = metrics.densityDpi/160.0;
        //PR814292 modify puzzle choose frame and background overlapped by fengke at 2014.10.21 end
        gridViewStyle.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> arg0, View view,
                    int position, long id) {
                Intent intent = new Intent();
                String imgpath = path_root+mThumbName[i]+img_suffixString;
                intent.putExtra("imagepath", imgpath);
                intent.setClass(ActivityTemplateSetBg.this,
                        PinTuTabActivity.class);
                setResult(RESULT_OK, intent);
                finish();
            }

        });

        btn_cancle.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                ActivityTemplateSetBg.this.finish();
            }

        });
    }

    public void init() {
        gridViewStyle = (com.jrdcom.example.joinpic.GridViewOpen) findViewById(R.id.setBg);
        // headerBtn = (Button) findViewById(R.id.headerBtn);
        btn_cancle = (Button) findViewById(R.id.btn_setstyle_cancel);
        new Thread(
                new Runnable() {
                    
                    @Override
                    public void run() {
                        // TODO Auto-generated method stub
                        mThumbimages = new Bitmap[mThumbName.length];
                        String mCurrentBgImage ="";
                        for(int i=0;i< mThumbName.length;i++)
                        {
                            mCurrentBgImage = path_root+mThumbName[i]+".thu";
                            mThumbimages[i] =  BitmapOperate.LoadAssertsPic(mCurrentBgImage,
                                    getResources().getAssets());
                        }
                        handler.sendEmptyMessage(1);
                    }
                }
                ).start();
    }

    public class StyleAdapter extends BaseAdapter {
        private Context mContext;

        public StyleAdapter(Context c) {
            mContext = c;
        }

        @Override
        public int getCount() {
            // TODO Auto-generated method stub
//            return mThumbIds.length;
            return mThumbimages.length;
        }

        @Override
        public Object getItem(int position) {
            // TODO Auto-generated method stub
            return mThumbimages[position];
        }

        @Override
        public long getItemId(int position) {
            // TODO Auto-generated method stub
            i = position;
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            // TODO Auto-generated method stub

            ImageView imageview;
            if (convertView == null) {
                imageview = new ImageView(mContext);
                //PR814292 modify puzzle choose frame and background overlapped by fengke at 2014.10.21 start
                if (destiny == 1.0) {
                    imageview.setLayoutParams(new GridView.LayoutParams(100, 100));
                    imageview.setScaleType(ImageView.ScaleType.CENTER_CROP);
                    imageview.setPadding(5, 5, 5, 5);
                } else {
                    imageview.setLayoutParams(new GridView.LayoutParams(150, 150));
                    imageview.setScaleType(ImageView.ScaleType.CENTER_CROP);
                    imageview.setPadding(8, 8, 8, 8);
                }
                //PR814292 modify puzzle choose frame and background overlapped by fengke at 2014.10.21 end
            } else {
                imageview = (ImageView) convertView;
            }
            imageview.setImageBitmap(mThumbimages[position]);
            return imageview;
        }
    }

    @Override
    protected void onDestroy() {
        // TODO Auto-generated method stub
        super.onDestroy();
        // editor.putInt("bg_id", i);
        // editor.commit();
    }

}
