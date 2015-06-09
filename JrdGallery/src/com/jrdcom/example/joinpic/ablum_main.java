
package com.jrdcom.example.joinpic;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import com.jrdcom.android.gallery3d.R;

public class ablum_main extends Activity implements OnClickListener,
        OnItemClickListener {
    static int counter=0;
    private ListView list_album = null;
    private GridView grid_album = null;
    private TextView picnum_text = null;
    private TextView picnum_text1 = null;
    private Button btn_goPintu = null;
    private TextView picnum_text2 = null;
    private Button folders_back = null;
    private TextView album_title = null;
    private HorizontalListView horizontallist;
    private HorizListAdapter horizlistadapter;
    
    private Object mutextObject = new Object();
    // data
    private ArrayList<String> list;
    private ArrayList<Map<String, Object>> data;
    // 添加文件名
    private HashMap<String, ArrayList<File>> map;

    private ArrayList<String> dirnameList = null;

    private ImageAdapter imageAdapter;

    public static ArrayList<String> pathlist = null;
    public static ArrayList<String> imagelist = null;

    private LoadImagesFromSDCard loadImagesFromSDCard;
    private Button mCancelButton;
    private Intent intent;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.album);
        init();
      //add by biao.luo pr465364 begin  
//        loadImages();
//        setupViews();
      //add by biao.luo end
    }

    
    @Override
    protected void onStart() {
        super.onStart();
        //add by biao.luo pr465364 begin  
        loadImages();
        setupViews();
      //add by biao.luo pr465364 end  
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        intent = getIntent();
        if(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT == intent.getFlags()){
            folders_back.setVisibility(View.GONE);
        }
    }
    
    public void init() {
        list_album = (ListView) findViewById(R.id.list_album);
        grid_album = (GridView) findViewById(R.id.grid_album);
        picnum_text = (TextView) findViewById(R.id.picnum_text);
        picnum_text1 = (TextView) findViewById(R.id.picnum_text1);
        btn_goPintu = (Button) findViewById(R.id.btn_goPintu);
        picnum_text2 = (TextView) findViewById(R.id.picnum_text2);
        folders_back = (Button) findViewById(R.id.folders_back);
        album_title = (TextView) findViewById(R.id.album_title);
        horizontallist = (HorizontalListView) findViewById(R.id.horizontalList);
        mCancelButton = (Button) findViewById(R.id.btn_cancel);
        listeners();
        list = new ArrayList<String>();
        dirnameList = new ArrayList<String>();
        pathlist = new ArrayList<String>();
        imagelist = new ArrayList<String>();
    }

    private void listeners() {
        // 返回监听
        this.folders_back.setOnClickListener(this);
        // 文件夹list监听
        this.list_album.setOnItemClickListener(this);
        // 开始拼图监听
        this.btn_goPintu.setOnClickListener(this);
        // gridview监听
        this.grid_album.setOnItemClickListener(this);

        horizontallist.setOnItemClickListener(listener);
        mCancelButton.setOnClickListener(this);
    }

    OnItemClickListener listener = new OnItemClickListener() {

        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position,
                long id) {
            // TODO Auto-generated method stub
            pathlist.remove(position);
            horizlistadapter.notifyDataSetChanged();
            picnum_text.setText(pathlist.size() + "");
        }

    };
    
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if(keyCode == KeyEvent.KEYCODE_BACK){
            ablum_main.this.finish();
        }
        return false;
    };
    
    @Override
    public void onClick(View v) {
        // TODO Auto-generated method stub
        switch (v.getId()) {
            case R.id.folders_back:
                ablum_main.this.finish();
                break;
            case R.id.btn_goPintu:
                if (pathlist.size() > 1) {
                    Intent intent = new Intent();
                    intent.setClass(ablum_main.this, PinTuTabActivity.class);
                    intent.putStringArrayListExtra("tupian", pathlist);
                    startActivity(intent);
                    ablum_main.this.finish();
                } else {
                    Toast.makeText(this, this.getString(R.string.puzzle_title), 1000).show();
                }
                break;
            case R.id.btn_cancel:
                ablum_main.this.finish();
                break;
            default:
                break;
        }

    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position,
            long id) {

//        Log.e("", "=========position is==========" + position);
//        int columnIndex = 0;
//        String[] projection = {
//                MediaStore.Images.Media.DATA
//        };
//        Cursor cursor = managedQuery(
//                MediaStore.Images.Media.EXTERNAL_CONTENT_URI, projection, null,
//                null, null);
//        if (cursor != null) {
//            columnIndex = cursor
//                    .getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
//            cursor.moveToPosition(position);
//            String imagePath = cursor.getString(columnIndex);
//            Log.e("", "=========imagePath is==========" + imagePath);
//            if (pathlist.size() < 9) {
//                pathlist.add(imagePath);
//                horizlistadapter.notifyDataSetChanged();
//            }
//            picnum_text.setText(this.pathlist.size() + "");
//        }
//    	imagelist
    	
    	if (pathlist.size() < 9) {
          pathlist.add(imagelist.get(position));
          horizlistadapter.notifyDataSetChanged();
      }
      picnum_text.setText(this.pathlist.size() + "");
    }

    /**
     * Free up bitmap related resources.
     */
    protected void onDestroy() {
        loadImagesFromSDCard.cancel(true);
        final GridView grid = grid_album;
        final int count = grid.getChildCount();
        ImageView v = null;
        for (int i = 0; i < count; i++) {
            v = (ImageView) grid.getChildAt(i);
            ((BitmapDrawable) v.getDrawable()).setCallback(null);
        }
        super.onDestroy();
    }


    private void setupViews() {
        // sdcardImages.setNumColumns(display.getWidth()/45);
        grid_album.setClipToPadding(false);
        grid_album.setOnItemClickListener(ablum_main.this);
        imageAdapter = new ImageAdapter(getApplicationContext());
        grid_album.setAdapter(imageAdapter);
        Intent intent = this.getIntent();
        if (intent.getStringArrayListExtra("go_back_list") == null) {

        } else {
            pathlist = intent.getStringArrayListExtra("go_back_list");
            picnum_text.setText(pathlist.size() + "");
        }
        horizlistadapter = new HorizListAdapter(getApplicationContext(),
                pathlist);
        horizontallist.setAdapter(horizlistadapter);
    }

    /**
     * Load images.
     */
    private void loadImages() {
        final Object data = getLastNonConfigurationInstance();
        if (data == null) {
            loadImagesFromSDCard   = new LoadImagesFromSDCard();
            loadImagesFromSDCard.execute();
        } else {
            final LoadedImage[] photos = (LoadedImage[]) data;
            if (photos.length == 0) {
                loadImagesFromSDCard   = new LoadImagesFromSDCard();
                loadImagesFromSDCard.execute();
            }
            for (LoadedImage photo : photos) {
                addImage(photo);
            }
        }
    }

    //add by biao.luo pr465364 begin
    @Override
    protected void onStop() {
        super.onStop();
        if (!loadImagesFromSDCard.isCancelled()) {
            loadImagesFromSDCard.cancel(true);
        }
    }
    //add by biao.luo end
    /*	*//**
     * Add image(s) to the grid view adapter.
     * 
     * @param value Array of LoadedImages references
     */

    private void addImage(LoadedImage... value) {
        for (LoadedImage image : value) {
            imageAdapter.addPhoto(image);
            imageAdapter.notifyDataSetChanged();
        }
    }

    /**
     * Save bitmap images into a list and return that list.
     * 
     * @see android.app.Activity#onRetainNonConfigurationInstance()
     */
    @Override
    public Object onRetainNonConfigurationInstance() {
        final GridView grid = grid_album;
        final int count = grid.getChildCount();
        final LoadedImage[] list = new LoadedImage[count];

        for (int i = 0; i < count; i++) {
            final ImageView v = (ImageView) grid.getChildAt(i);
            list[i] = new LoadedImage(
                    ((BitmapDrawable) v.getDrawable()).getBitmap());
        }

        return list;
    }

     class LoadImagesFromSDCard extends AsyncTask<Object, LoadedImage, Object> {

        /**
         * Load images from SD Card in the background, and display each image on
         * the screen.
         * 
         * @see android.os.AsyncTask#doInBackground(Params[])
         */
        private  Cursor cursor;//add by biao.luo pr465364   
        
        @Override
        protected Object doInBackground(Object... params) {

                setProgressBarIndeterminateVisibility(true);
                Bitmap bitmap = null;
                Bitmap newBitmap = null;
                Uri uri = null;

                // Set up an array of the Thumbnail Image ID column we want
                String[] projection = {
                        MediaStore.Images.Media._ID,
                        MediaStore.Images.Media.DATA,
                };
                // Create the cursor pointing to the SDCard
                 cursor = managedQuery(
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI, projection, // Which
                                                                                  // columns
                                                                                  // to
                                                                                  // return
                        null, // Return all rows
                        null, null);
                int columnIndex = cursor
                        .getColumnIndexOrThrow(MediaStore.Images.Media._ID);
                int dataindex = cursor
                        .getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
                int size = cursor.getCount();
                // If size is 0, there are no images on the SD Card.
                if (size == 0) {
                    // No Images available, post some message to the user
                }
                int imageID = 0;
                String imagePath="";
//                for (int i = 0; i < size; i++) {
//                    cursor.moveToPosition(i);
                    while (cursor.moveToNext()) {
                    imageID = cursor.getInt(columnIndex);
                    uri = Uri.withAppendedPath(
                            MediaStore.Images.Media.EXTERNAL_CONTENT_URI, ""
                                    + imageID);
                    try {
                        // bitmap =
                        // BitmapFactory.decodeStream(getContentResolver()
                        // .openInputStream(uri));
                        BitmapFactory.Options options = new BitmapFactory.Options();
                        options.inSampleSize = 4;
                        bitmap = BitmapFactory.decodeStream(getContentResolver()
                                .openInputStream(uri), null, options);
                        if (bitmap != null) {
                            // newBitmap = Bitmap.createScaledBitmap(bitmap, 70,
                            // 70,
                            // true);
                            newBitmap = Bitmap.createScaledBitmap(bitmap, 160, 160,
                                    true);
                            // BitmapFactory.Options options = new
                            // BitmapFactory.Options();
                            // options.inSampleSize = 4;
                            // Bitmap bm =
                            // BitmapFactory.decodeFile(mData.get(position),
                            // options);

                            // bitmap.recycle();
                            if (newBitmap != null) {
                                imagePath = cursor.getString(dataindex);
                                imagelist.add(imagePath);
                                publishProgress(new LoadedImage(newBitmap));
                            }
                        }
                    } catch (IOException e) {
                        // Error fetching image, try to recover
                    }
            }
            // cursor.close();
            return null;
        }

        /**
         * Add a new LoadedImage in the images grid.
         * 
         * @param value The image.
         */
        @Override
        public void onProgressUpdate(LoadedImage... value) {
            addImage(value);
        }

        /**
         * Set the visibility of the progress bar to false.
         * 
         * @see android.os.AsyncTask#onPostExecute(java.lang.Object)
         */
        @Override
        protected void onPostExecute(Object result) {
            setProgressBarIndeterminateVisibility(false);
        }
        //add by biao.luo pr465364 begin  
        @Override
        protected void onCancelled(Object result) {
            if(!cursor.isClosed())
            {
                cursor.close();
            }
        }
        //add by biao.luo end  
        
    }

    private static class LoadedImage {
        Bitmap mBitmap;

        LoadedImage(Bitmap bitmap) {
            mBitmap = bitmap;
        }

        public Bitmap getBitmap() {
            return mBitmap;
        }
    }

    class ImageAdapter extends BaseAdapter {

        private Context mContext;
        private ArrayList<LoadedImage> photos = new ArrayList<LoadedImage>();

        public ImageAdapter(Context context) {
            mContext = context;
        }

        public void addPhoto(LoadedImage photo) {
            photos.add(photo);
        }

        public int getCount() {
            return photos.size();
        }

        public Object getItem(int position) {
            return photos.get(position);
        }

        public long getItemId(int position) {
            return position;
        }

        public View getView(int position, View convertView, ViewGroup parent) {
            final ImageView imageView;
            if (convertView == null) {
                imageView = new ImageView(mContext);
            } else {
                imageView = (ImageView) convertView;
            }
            imageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
            imageView.setPadding(8, 8, 8, 8);
            imageView.setImageBitmap(photos.get(position).getBitmap());
            return imageView;
        }
    }

}
