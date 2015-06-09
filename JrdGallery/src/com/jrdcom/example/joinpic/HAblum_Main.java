package com.jrdcom.example.joinpic;
/**
 * @author yaogang.hao@tct-nj.com
 * This is the main class to show all pictures be to checked.
 * 
 */
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.jrdcom.android.gallery3d.R;
import android.app.Activity;
import android.app.ProgressDialog;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.ConditionVariable;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.GridView;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.content.Intent;
import android.graphics.BitmapFactory;//PR-511200 added by Xiaowei.Xu

import android.os.SystemProperties;

public class HAblum_Main extends Activity {
    
    private ConditionVariable conditionVariable = new ConditionVariable();
    //scrolling flag, judget if the gridview is scrolling.
    public static boolean isScrolling = false;
    /**************Hander Message IDs***************/
    private final int MSG_LOAD_ALBUM_SUCCESS = 0X01;
    private final int MSG_LOAD_ALBUMLIST_SUCCESS = 0X02;
    
    private final int MSG_ADD_SELECTED_IMAGE = 0X11;
    private final int MSG_DEL_SELECTED_IMAGE = 0X12;
    
    /**************Widget views********************/
    private FrameLayout album_content;
    private ListView ablumListView;
    private GridView album_grid;
//    private LinearLayout album_selector_container;
//    private HorizontalScrollView album_scroll;
    
    private SelectorHorizontalListView horizontalListView;
    private ProgressDialog progressDialog;
    
    private Button btn_back;
    private Button btn_cancel;
    private Button btn_goPintu;
    private TextView album_title;
    private TextView tv_selected;
    
    /********************Data list*****************/
    List<ImageAlbum> albumList = new ArrayList<ImageAlbum>();
    List<Image>  albumchirldlist = null; 
    Map<Integer, String> selectedImageMap = new HashMap<Integer, String>();
    List<Image> selectedImagelist = new ArrayList<Image>();
    static ArrayList<String> pathlist = new ArrayList<String>();
    /********************Data Adapter******************/
    AlbumListAdater albumListAdater = null;
    ImageGridAdapter imageGridAdapter = null;
    SelectorAdapter selectorAdapter = null;
    /*******************Flags status ***********************/
  
    private final int LIST_VIEW = 1;
    private final int GRID_VIEW = 2;
    private int CURR_VIEW = LIST_VIEW;
    private int CURR_ALBUM_POSITION = -1;
    
    /******** Message handler****************/
    private Handler mainHandler = new Handler(){
        public void handleMessage(android.os.Message msg) {
            switch (msg.what) {
            case MSG_LOAD_ALBUM_SUCCESS:
                {
                    // yaogang.hao for PR 524297
                    // conditionVariable.block();
                    albumListAdater = new AlbumListAdater(HAblum_Main.this, albumList);
                    ablumListView.setAdapter(albumListAdater);
                    //PR907135 modify for monkey test error by fengke at 2015.01.19 start
                    try {
                        progressDialog.dismiss();
                    } catch (Exception e) {
                        Log.w("HAblum_Main", "fengke Exception e = " + e);
                    }
                    //PR907135 modify for monkey test error by fengke at 2015.01.19 end
                    break;
                }
            case MSG_LOAD_ALBUMLIST_SUCCESS:
                {
                    imageGridAdapter = new ImageGridAdapter(HAblum_Main.this, albumchirldlist);
                    album_grid.setAdapter(imageGridAdapter);
                    album_grid.setOnItemClickListener(albumChildListener);
                    //add scroll listener
                    album_grid.setOnScrollListener(scrollListener);
                    //PR907135 modify for monkey test error by fengke at 2015.01.19 start
                    try {
                        progressDialog.dismiss();
                    } catch (Exception e) {
                        Log.w("HAblum_Main", "fengke Exception e = " + e);
                    }
                    //PR907135 modify for monkey test error by fengke at 2015.01.19 end
                    break;
                }
            case MSG_ADD_SELECTED_IMAGE:
                {
                    break;
                }
            case MSG_DEL_SELECTED_IMAGE:
                {
                    break;
                }
            }
        };
    };

     @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, 
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.album_main);
        createDialog();
        findViews();
//        getAlbumList();
        
        //modify for 579067 by yaogang.hao begin
        int guestMode = Integer.parseInt(SystemProperties.get("persist.security.guestmode", "0"));
        if(guestMode==0){
            getAlbumList();
        }
        //modify for 579067 by yaogang.hao end
        setListeners();
        
     }
    public void findViews()
    {
        album_content = (FrameLayout) findViewById(R.id.album_content);
        ablumListView = (ListView)findViewById(R.id.album_list);
        album_grid = (GridView)findViewById(R.id.album_grid);
        // yaogang.hao for PR 524297
        // conditionVariable.open();

        horizontalListView  = (SelectorHorizontalListView) findViewById(R.id.album_scroll);
        selectorAdapter = new SelectorAdapter(this, selectedImagelist);
        horizontalListView.setAdapter(selectorAdapter);
        horizontalListView.setOnItemClickListener(albumSelectorListener);
        
        btn_back = (Button) findViewById(R.id.btn_back);
        btn_cancel = (Button) findViewById(R.id.btn_cancel);
        btn_goPintu = (Button) findViewById(R.id.btn_goPintu);
        
        btn_back.setOnClickListener(buttonListeners);
        btn_cancel.setOnClickListener(buttonListeners);
        btn_goPintu.setOnClickListener(buttonListeners);
        
        album_title = (TextView) findViewById(R.id.album_title);
        tv_selected = (TextView) findViewById(R.id.tv_selected);
        
        setSelectedText(selectedImageMap.size());
        btn_back.setVisibility(View.GONE);
    }
    public void setSelectedText(int count)
    {
//        tv_selected.setText(getString(R.string.album_select_num, count));
        tv_selected.setText(getString(R.string.choosed_pic)+" "+count+" "+getString(R.string.pic_num));
    }
    public void createDialog()
    {
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage(getString(R.string.tip_please_wait_content));
        progressDialog.setIndeterminate(true);
        progressDialog.setCancelable(false);
    }
    public void setListeners()
    {
        ablumListView.setOnItemClickListener(albumListener);
    }
    ////////////////////////////////////Threads /////////////////////////////////
    public void getAlbumList()
    {
        //show dialog for a tip
        progressDialog.show();
        //start a Asynchronized thread
        new Thread(new Runnable() {
            
            @Override
            public void run() {
                ImageProvider provider = new ImageProvider(HAblum_Main.this);
                albumList = (List<ImageAlbum>) provider.getAlbumList();
                for(ImageAlbum tempalbum : albumList)
                {
                    int id =  tempalbum.getImageid();
                    Bitmap thumbNail = MediaStore.Images.Thumbnails.getThumbnail(
                            getContentResolver(), 
                            id, MediaStore.Images.Thumbnails.MICRO_KIND, null);
                    // yaogang.hao for PR 524297
                    if (thumbNail != null)
                        tempalbum.setThumbNail(thumbNail);
                }
                mainHandler.sendEmptyMessage(MSG_LOAD_ALBUM_SUCCESS);
            }
        }).start();
    }
    /**
     * 
     * @author yaogang.hao
     * get the album list
     */
    private class AlbumListThread extends Thread
    {
        private ImageAlbum mImageAlbum;
        
        public AlbumListThread(ImageAlbum album)
        {
            this.mImageAlbum = album;
            start();
        }
        
        @Override
        public void run() {
            //judge if already get the children image list.
            if(null == this.mImageAlbum.getChildImageslist())
            {
                List<Image> albumchildlist = getAlbumChildList(this.mImageAlbum);
                this.mImageAlbum.setChildImageslist(albumchildlist);
            }
            albumchirldlist = this.mImageAlbum.getChildImageslist();
            mainHandler.sendEmptyMessage(MSG_LOAD_ALBUMLIST_SUCCESS);
        }
    }
    /**
     * get the children of album
     * @param imageAlbum
     * @return
     */
    public List<Image> getAlbumChildList(ImageAlbum imageAlbum)
    {
        List<Image>  list = new ArrayList<Image>();
        ImageProvider provider = new ImageProvider(HAblum_Main.this);
        list = provider.getAlbumChildList(imageAlbum);
        
        return list;
    }
    ////////////////////////////////////////////////////
    ///  Listeners 
    ////////////////////////////////////////////////////
    
    public void switchView(int flag)
    {
        if(flag == GRID_VIEW)
        {
            album_content.bringChildToFront(ablumListView);
            album_grid.setVisibility(View.GONE);
            ablumListView.setVisibility(View.VISIBLE);
            CURR_VIEW = LIST_VIEW;
            btn_back.setText(R.string.home);
            btn_back.setVisibility(View.GONE);
            album_title.setText(R.string.select_album);
            
            if(albumchirldlist != null)
            {
                albumchirldlist = null;
            }
            imageGridAdapter = null;
            album_grid.setOnItemClickListener(null);
            album_grid.setAdapter(null);
            CURR_ALBUM_POSITION = -1;
            
        }else if(flag == LIST_VIEW)
        {
            album_content.bringChildToFront(album_grid);
            ablumListView.setVisibility(View.GONE);
            album_grid.setVisibility(View.VISIBLE);
            CURR_VIEW = GRID_VIEW;
            btn_back.setText(R.string.btn_return);
            btn_back.setVisibility(View.VISIBLE);
        }
    }
    AdapterView.OnItemClickListener albumListener = new AdapterView.OnItemClickListener() {

        @Override
        public void onItemClick(AdapterView<?> arg0, View view, int pos, long arg3) {
            
            //switch view
            switchView(LIST_VIEW);
            CURR_ALBUM_POSITION = pos;
            ImageAlbum tempalbum = albumList.get(pos);
            album_title.setText(tempalbum.getAblumName());
            progressDialog.show();
            new AlbumListThread(tempalbum);
        }
    };
    View.OnClickListener buttonListeners = new View.OnClickListener() {
        
        @Override
        public void onClick(View v) {
            int id = v.getId();
            
            switch (id) {
            
                case R.id.btn_back://return back button
                {
                    if(CURR_VIEW == GRID_VIEW)
                    {
                        switchView(GRID_VIEW);
                    }
                    else
                    {
                        goback();
                    }
                    break;
                }
                case R.id.btn_cancel:
                {
                    goback();
                    break;
                }
                case R.id.btn_goPintu:
                {
                    if (selectedImagelist.size() > 1) {
                        pathlist.clear();
                        for(Image image:selectedImagelist)
                        {
                            pathlist.add(image.getPath());
                        }
                        //PR651227-tao li-begin 001
                        if (!Utils.hasAvailableSpace(Utils.getDefaultPath())) {
                            Utils.showToast(HAblum_Main.this, R.string.not_enough_storage);
                                break;
                        }
                        //PR651227-tao li-end 001
                        Intent intent = new Intent();
                        intent.setClass(HAblum_Main.this, PinTuTabActivity.class);
                        intent.putStringArrayListExtra("tupian", pathlist);
                        startActivity(intent);
                    } else {
                        Toast.makeText(HAblum_Main.this,getString(R.string.puzzle_title), Toast.LENGTH_SHORT).show();
                    }
                    break;
                }

            }
        }
    };
    
    AdapterView.OnItemClickListener albumChildListener = new AdapterView.OnItemClickListener() {

        @Override
        public void onItemClick(AdapterView<?> arg0, View view, int pos, long arg3) {
            
            if(CURR_ALBUM_POSITION == -1 && albumchirldlist!= null) return;
            //PR-511200 added by Xiaowei.Xu begin
            //PR792158 gallery photoedit bitmap is not recycle modify by fengke at 2014.09.18 start
            //BitmapFactory.Options options = new BitmapFactory.Options();
            //options.inJustDecodeBounds = false;
            //PR792158 gallery photoedit bitmap is not recycle modify by fengke at 2014.09.18 end
            //PR-511200 added by Xiaowei.Xu end
            if(selectedImagelist.size() <= 9)
            {
                Image image = albumchirldlist.get(pos);
                //PR-511200 added by Xiaowei.Xu begin

                //PR792158 gallery photoedit bitmap is not recycle modify by zhanying.yang at 2014.10.22 start
                BitmapFactory.Options options = new BitmapFactory.Options();
                options.inJustDecodeBounds = true;
                BitmapFactory.decodeFile(image.getPath(), options);

                int realWidth = options.outWidth;
                int realHeight = options.outHeight;
                options.inSampleSize = computeSampleSize(options,-1,240*320);
                options.inJustDecodeBounds = false;
                options.inInputShareable = true;
                options.inPurgeable = true;
                Log.v("HAblum_Main","fengke  realWidth =  " + realWidth + ", realHeight = " + realHeight + ", inSampleSize = "+ options.inSampleSize);

                Bitmap bmp = BitmapFactory.decodeFile(image.getPath(), options);
                if(bmp == null){
                	Toast.makeText(HAblum_Main.this,getString(R.string.prohibit_use), Toast.LENGTH_SHORT).show();
                	return;
                } else {
                    bmp.recycle();
                    bmp = null;
                }
                //PR792158 gallery photoedit bitmap is not recycle modify by zhanying.yang at 2014.10.22 end

                //PR-511200 added by Xiaowei.Xu end                
                image.setTempPos(CURR_ALBUM_POSITION);
                if(!image.isSelected() && selectedImagelist.size() < 9)
                {
                    selectedImagelist.add(image);
                    image.setSelected(!image.isSelected());
                    selectorAdapter.notifyDataSetChanged();
                }else if(image.isSelected())
                {
                    selectedImagelist.remove(image);
                    image.setSelected(!image.isSelected());
                    selectorAdapter.notifyDataSetChanged();
                }
                setSelectedText(selectedImagelist.size());
                if(imageGridAdapter != null)
                {
                    imageGridAdapter.notifyDataSetChanged();
                }
                
            }
            
        }
    };
    AdapterView.OnItemClickListener albumSelectorListener = new AdapterView.OnItemClickListener() {

        @Override
        public void onItemClick(AdapterView<?> arg0, View view, int pos, long arg3) {
            if(selectedImagelist.size() > 0)
            {
               Image image = selectedImagelist.remove(pos);
               image.setSelected(false);
                selectorAdapter.notifyDataSetChanged();
                if(imageGridAdapter != null)
                {
                    imageGridAdapter.notifyDataSetChanged();
                }
                setSelectedText(selectedImagelist.size());
            }
        }
    };
    /**
     * Scroll listeners
     * 
     */
    AbsListView.OnScrollListener scrollListener =  new AbsListView.OnScrollListener() {
        
        @Override
        public void onScrollStateChanged(AbsListView view, int scrollState) {
            //no scrolling, idle state
            if(scrollState ==  AbsListView.OnScrollListener.SCROLL_STATE_IDLE)
            {
                isScrolling = false;
                if(imageGridAdapter != null)
                {
                    imageGridAdapter.notifyDataSetChanged();
                }
            }
            //custom is fling the view. scrolling state
            else if(scrollState ==  AbsListView.OnScrollListener.SCROLL_STATE_FLING|| scrollState == AbsListView.OnScrollListener.SCROLL_STATE_TOUCH_SCROLL)
            {
                isScrolling = true;
            }
                
            
        }
        
        @Override
        public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount,
                int totalItemCount) {
            
        }
    };
    /////////////////////////

    //PR792158 gallery photoedit bitmap is not recycle modify by zhanying.yang at 2014.10.22 start
    public int computeSampleSize(BitmapFactory.Options options,
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
    private int computeInitialSampleSize(BitmapFactory.Options options,
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
    //PR792158 gallery photoedit bitmap is not recycle modify by zhanying.yang at 2014.10.22 end





    
    protected void onDestroy() {
        
        if(albumList != null)
            albumList.clear();
        if(albumchirldlist != null)
            albumchirldlist.clear();
        if(selectedImagelist != null)
            selectedImagelist.clear();
        super.onDestroy();
    };
    @Override
    public void onBackPressed() {
        goback();
    }
    public void goback()
    {
        Intent go_back = new Intent();
        go_back.setClass(HAblum_Main.this, com.jrdcom.android.gallery3d.app.Gallery.class);
        //yaogang.hao for PR 499412 Need tap back key twice to exit gallery after exit joint screen.
        go_back.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        startActivity(go_back);
        finish();
    }
    
}
