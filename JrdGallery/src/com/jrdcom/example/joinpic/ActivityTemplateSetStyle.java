package com.jrdcom.example.joinpic;

import java.io.IOException;
import java.io.InputStream;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
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

//PR814292 modify puzzle choose frame and background overlapped by fengke at 2014.10.21 start
import android.util.DisplayMetrics;
//PR814292 modify puzzle choose frame and background overlapped by fengke at 2014.10.21 end

import com.jrdcom.android.gallery3d.R;

public class ActivityTemplateSetStyle extends Activity {
    private GridViewOpen gridViewStyle;
    private Button btn_cancle;
    private int mId = 0;
    private double destiny = 1.5;//PR814292 modify puzzle choose frame and background overlapped by fengke at 2014.10.21
    private final String image_root = "puzzle/";
    private String[] mThumbIds = {
            "10049001.thu", "10049002.thu", "10049003.thu", "10049004.thu", "10049005.thu", 
            "10049006.thu", "10049007.thu", "10049008.thu", "10049009.thu", "10049010.thu", 
            "10049011.thu", "10049012.thu", "10049013.thu", "10049014.thu", "10049015.thu", 
            "10049016.thu", "10049017.thu", "10049018.thu", "10049019.thu", "10049020.thu", 
            "10049021.thu", "10049022.thu", "10049023.thu", "10049024.thu", "10049025.thu", 
            "10049026.thu", "10049027.thu"
        };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.activity_pintusetstyle);
        init();
        gridViewStyle.setAdapter(new StyleAdapter(this));
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
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // TODO Auto-generated method stub
                Intent intent = new Intent();
                intent.putExtra("id", (int) id);
                setResult(RESULT_OK, intent);
                finish();
            }

        });

        btn_cancle.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                ActivityTemplateSetStyle.this.finish();
            }

        });
    }

    public void init() {
        gridViewStyle = (GridViewOpen) findViewById(R.id.setStyle);
        // headerBtn = (Button) findViewById(R.id.headerBtn);
        btn_cancle = (Button) findViewById(R.id.btn_setstyle_cancel);
    }

    public class StyleAdapter extends BaseAdapter {
        private Context mContext;

        public StyleAdapter(Context c) {
            mContext = c;
        }

        @Override
        public int getCount() {
            // TODO Auto-generated method stub
            return mThumbIds.length;
        }

        @Override
        public Object getItem(int position) {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public long getItemId(int position) {
            // TODO Auto-generated method stub
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

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
            InputStream iStream;
            Bitmap bitmap;
            try {
                iStream = getApplicationContext().getAssets()
                        .open(image_root + mThumbIds[position]);
                bitmap = BitmapFactory.decodeStream(iStream);
                imageview.setImageBitmap(bitmap);
            } catch (IOException e) {
                e.printStackTrace();
            }
            // imageview.setImageResource(mThumbIds[position]);
            mId = position;
            return imageview;
        }
    }
}
