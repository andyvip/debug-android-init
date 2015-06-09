
package com.jrdcom.android.gallery3d.util;

/*import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import  android.graphics.*;
import android.provider.MediaStore;
import android.database.Cursor;
import com.jrdcom.android.gallery3d.ui.SelectionManager;
import com.jrdcom.android.gallery3d.data.Path;
import com.jrdcom.android.gallery3d.app.FilterUtils;
import com.jrdcom.android.gallery3d.R;
import com.jrdcom.android.gallery3d.app.AbstractGalleryActivity;
import com.jrdcom.android.gallery3d.app.AlbumSetDataLoader;
import com.jrdcom.android.gallery3d.app.AlbumSetPage;
import com.jrdcom.android.gallery3d.data.MediaItem;
import com.jrdcom.android.gallery3d.data.MediaObject;
import com.jrdcom.android.gallery3d.ui.AlbumSetSlidingWindow.AlbumSetEntry;

import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.IContentProvider;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.PixelFormat;
import android.media.MediaMetadataRetriever;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.os.RemoteException;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.BaseAdapter;
import android.widget.BaseExpandableListAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.EditText;
import com.jrdcom.android.gallery3d.data.*;
import android.os.AsyncTask;
import android.view.View.OnTouchListener;
import com.jrdcom.android.gallery3d.app.DragSource;

import android.media.MediaScannerConnection.OnScanCompletedListener;
import java.util.HashMap;
import java.lang.Object;
import java.util.*;
import com.jrdcom.android.gallery3d.util.ImageProvider;
import com.jrdcom.android.gallery3d.util.ImageAlbum;*/

public class ExAdapter/* extends BaseExpandableListAdapter*/ {
    /*Context mContext;
    private Handler mHandler;
    private int mGroupCount=0;
    private int mTotalPicCount;
    private int mSelectPicCount=0;
    private ListGroup mListGroup[];
    private int mRealGroupCount = 0;
    private boolean isSelect = false;
    private ImageAdapter mGridViewAdapter[];
    private AbstractGalleryActivity mActivity;
    private SelectionManager mSelectionManager;
    private HashMap<Object,Object> mRealInder = new HashMap<Object,Object> ();
    private HashMap<String,Object> mAlbumHideKey = new HashMap<String, Object>();
    private HashMap<String,String> mPathToDirect = new HashMap<String, String>();
    private DataManager manager;
    private ContentResolver contentResolver;
    private static final int NONE = 0;
    private static final int ZOOM = 1;
    private float oldDistance;
    private int mode = NONE;
    // add start by yaping.liu for p452145
    private boolean mIsAlbumSet;
    
    private AlbumSetDataLoader mAlbumSetDataAdapter;
    
    private boolean multipleChoice = false;
    private DoubleClickeDetector doubleClickeDetector;
    
    private ArrayList<Path> pathList;
    private boolean mIsSetDataIng = false;
    
    private HashMap<Integer, NewGridView> mGridViews = new HashMap<Integer,NewGridView> ();
    
    private int mImageviewHeight = 0;  //
    private ImageProvider mImageProvider;
    public HashMap<String,Integer> mImageId;
    private int preIndex = -1;
    
    public void setMultipleChoice(boolean multipleChoice) {
        this.multipleChoice = multipleChoice;
        if (multipleChoice) 
        {
            isSelect = true;
            pathList = new ArrayList<Path>();
        }
    }
    
    public ArrayList<Path> getPathList() {
        return pathList;
    }

    public void setmIsAlbumSet(boolean mIsAlbumSet) {
        this.mIsAlbumSet = mIsAlbumSet;
    }

    public void setmAlbumSetDataAdapter(AlbumSetDataLoader mAlbumSetDataAdapter) {
        this.mAlbumSetDataAdapter = mAlbumSetDataAdapter;
    }
    // add end by yaping.liu for p452145

    *//*********************************
     * @author yaogang.hao declare some necessery variables for dragging
     *//*
    private WindowManager mWindowManager;
    private WindowManager.LayoutParams mWindowParams;
    private Bitmap mDragBitmap;
    private ImageView mDragView;
    private boolean isdragging = false;

    public class ListGroup {
        public String path=null;
        public String title;
        public Path setPath;
        public int totalCount;
        public int selectCount;
        public boolean isGet = false;
        public boolean isHide = false;
        public boolean isCheckAll = false;
        public ArrayList <MediaItem> item;
    }

    public void intiSelectData() {
        mTotalPicCount = 0;
        mSelectPicCount = 0;
        if (mListGroup != null) {
            for (int i=0; i<mGroupCount; i++) {
                mListGroup[i].selectCount = 0;
                mListGroup[i].isCheckAll = false;
                if (mListGroup[i].item!=null){
                    if (!mListGroup[i].isHide) {
                        mTotalPicCount += mListGroup[i].item.size();
                    }
                    for (MediaItem item : mListGroup[i].item) {
                        item.isSelect = false;
                    }
                }
            }
        }
    }

    public int addNewAlbum(String path,String name) {
        mListGroup[mRealGroupCount] = new ListGroup();
        mListGroup[mRealGroupCount].title = name;
        mListGroup[mRealGroupCount].setPath = null;
        mListGroup[mRealGroupCount].totalCount = 0;
        mListGroup[mRealGroupCount].selectCount = 0;
        mListGroup[mRealGroupCount].isGet = true;
        mListGroup[mRealGroupCount].item = null;
        mListGroup[mRealGroupCount].path = path;
        // PR467258 modify by ming.zhang begin
        if (mAlbumHideKey.get(path)==null) {
            mAlbumHideKey.put(path, false);
        } else if ((Boolean)mAlbumHideKey.get(path)){
            mListGroup[mRealGroupCount].isHide = true;
            mRealGroupCount++;
            return (mGroupCount-1);
        }
        mListGroup[mRealGroupCount].isHide = false;
        // PR467258 modify by ming.zhang end
        mRealInder.put(mGroupCount, mRealGroupCount);
        mRealGroupCount++;
        mGroupCount++;
        return (mGroupCount-1);
    }

    public boolean isEmptyAlbum(int index) {
        if (mListGroup[index]==null) {
            return false;
        }
        return mListGroup[index].item != null;
    }
    public void setSelectionManager(SelectionManager selectionManager) {
        mSelectionManager = selectionManager;
    }

    public int getSelectPicCount() {
        return mSelectPicCount;
    }

    public int getTotalPicCount() {
        return mTotalPicCount;
    }
    public String getSelectTitle() {
        return ""+mSelectPicCount+"/"+mTotalPicCount;
    }
    
    public String getGroupTitle(int groupIndex) {
        Integer realIndex = (Integer)mRealInder.get(groupIndex);
        if (realIndex!=null&&mListGroup!=null&&
            mListGroup[realIndex]!=null&&
            mListGroup[realIndex].isGet) {
            return mListGroup[realIndex].title;
        }
        return "";
    }

    public int getGroupTotalCount(int groupIndex) {
        Integer realIndex = (Integer)mRealInder.get(groupIndex);
        if (realIndex!=null&&mListGroup!=null&&
            mListGroup[realIndex]!=null&&
            mListGroup[realIndex].isGet) {
            return mListGroup[realIndex].totalCount;
        }
        return 0;
    }

    public ListGroup [] getListData() {
        return mListGroup;
    }
    
    public HashMap<String,Object> getHideHashMap() {
        return mAlbumHideKey;
    }

    public int getAlbumCount() {
        return mRealGroupCount;
    }
    
    public void setHideValue(String key,Object value) {
        mAlbumHideKey.put(key, value);
    }
    public DataManager getDataManager() {
        return manager;
    }
    public ContentResolver getContentResolver() {
        return contentResolver;
    }
    public void updateImageId() {
        List<ImageAlbum> temp = mImageProvider.getAlbumList();
        mImageProvider.clearIdHashMap();
        for (ImageAlbum ablum : temp) {
            mImageProvider.getAlbumChildId(ablum);
        }
        mImageId = mImageProvider.getIdHashMap();
    }

    public void setActivityHandler(AbstractGalleryActivity activity,Handler handler) {
        mActivity = activity;
        mHandler = handler;
        manager = activity.getDataManager();
        contentResolver = activity.getContentResolver();
        mImageProvider = new ImageProvider(mContext);
        updateImageId();
    }

    public void setListData(AlbumSetEntry []data) {
        //boolean isGetData = false;//del by qjz for PR455354 20130520
        mRealGroupCount = 0;
        mTotalPicCount = 0;//add by qjz for PR449173 20130516 
        mGroupCount = 0;
        mIsSetDataIng = true;
        mRealInder.clear();
        if (mListGroup == null) {
            mListGroup = new ListGroup[256];
        }
        if (data !=null) {
            String[] projectionfile = new String[] {
                    android.provider.MediaStore.Images.Media.DATA,};
            for (int i = 0; mIsSetDataIng && i < data.length; i++) {
                try {
                if (null == data[i]) {
                    Log.d("ExAdapter", "data[i] == null");
                    break;
                }
                //modified by qjz for PR449173 20130516 begin
                if (mListGroup[i]==null) {
                    mListGroup[i] = new ListGroup();
                }
                //modified by qjz for PR449173 20130516 end
                mListGroup[i].isGet = true;
                mListGroup[i].title = data[i].title;
                mListGroup[i].totalCount = data[i].totalCount;
                mListGroup[i].selectCount = 0;
                //if (data[i].setPath == null) return false;//del by qjz for PR455354 20130520
                mListGroup[i].setPath = data[i].setPath;
                mListGroup[i].item = data[i].album.getMediaItem(0, data[i].totalCount);
                if (mPathToDirect.get(mListGroup[i].setPath.toString()) == null) {
                    Cursor cursor = contentResolver.query(manager.getContentUri(mListGroup[i].item.get(0).getPath()), projectionfile, null, null, null);
                    cursor.moveToFirst();
                    int dataindex = cursor.getColumnIndex(android.provider.MediaStore.Images.Media.DATA);
                    File file = new File(cursor.getString(dataindex));
                    mListGroup[i].path = file.getParent();
                    mAlbumHideKey.put(mListGroup[i].setPath.toString(), mListGroup[i].path);
                    cursor.close();
                } else {
                    mListGroup[i].path = (String)mPathToDirect.get(mListGroup[i].setPath.toString());
                }
                //isGetData = true;//del by qjz for PR455354 20130520
                mRealGroupCount++;
                if (mAlbumHideKey.get(mListGroup[i].path)==null) {
                    mAlbumHideKey.put(mListGroup[i].path, false);
                } else if ((Boolean)mAlbumHideKey.get(mListGroup[i].path)){
                    mAlbumHideKey.put(mListGroup[i].path, true);
                    mListGroup[i].isHide = true;
                    continue;
                }
                mTotalPicCount += mListGroup[i].totalCount;//add by qjz for PR449173 20130516 
                mListGroup[i].isHide = false;
                mRealInder.put(mGroupCount, i);
                mGroupCount++;
                }catch (Exception ex) {
                    Log.d("ExAdapter","Exception:"+ex.toString());
                }
            }
        }
        //return isGetData;//del by qjz for PR455354 20130520
    }

    public void stopSetDataThread() {
        mIsSetDataIng = false;
    }
    public ExAdapter(Context context) {
        mContext = context;
        mGridViewAdapter = new ImageAdapter[256];
    }

    public void changeListMode(boolean select) {
        if (isSelect != select) {
            isSelect = select;
            if (isSelect) {
                mSelectionManager.setAutoLeaveSelectionMode(true);
                //mSelectionManager.enterSelectionMode();
            } else {
                mSelectionManager.leaveSelectionMode();
            }
            Message msg = new Message();
            msg.what = AlbumSetPage.MSG_GOTO_SELECTMODE;
            msg.arg1 = isSelect?1:0;
            msg.obj = ""+mSelectPicCount+"/"+mTotalPicCount;
            if (mHandler!=null) {
                mHandler.sendMessage(msg);
            }
        }
    }

    public boolean isSelectMode() {
        return isSelect;
    }

    // PR454627 ming.zhang modify begin
    public void setSelectMode(boolean select) {
        isSelect = select;
    }
    // PR454627 ming.zhang modify end

    @Override
    public Object getChild(int arg0, int arg1) {
        // TODO Auto-generated method stub
        return "me";
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public View getChildView(int groupPosition, int childPosition,
            boolean isLastChild, View convertView, ViewGroup parent) {
        // TODO Auto-generated method stub
        View view = convertView;
        if (view == null) {
            LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = inflater.inflate(R.layout.children, null);
        }
        Integer realPosition = (Integer)mRealInder.get(groupPosition);
        if (realPosition == null) {
            return view;
        }
        NewGridView gView = (NewGridView)view.findViewById(R.id.gridview);
        mGridViews.put(realPosition, gView);
        if (mGridViewAdapter[realPosition] == null)
            mGridViewAdapter[realPosition]=new ImageAdapter(mContext,realPosition);
        gView.setTag(realPosition);
        gView.setAdapter(mGridViewAdapter[realPosition]);
        gView.setOnItemClickListener(new OnItemClickListener(){
            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
                    long arg3) {
                if (mIsAlbumSet) return;// add by yaping.liu for pr452145
                Message msg = new Message();
                msg.arg1 = mSelectPicCount==0?1:0;
                // add start by yaping.liu for pr452145
                if (multipleChoice)
                {
                    CheckBox item = (CheckBox)arg1.findViewById(R.id.item_select);
                    item.setChecked(!item.isChecked());
                    Integer groupIndex = (Integer)arg0.getTag();
                    if (item.isChecked()) {
                        mSelectPicCount++;
                        mListGroup[groupIndex].selectCount++;
                        mListGroup[groupIndex].item.get(arg2).isSelect = true;
                        pathList.add(mListGroup[groupIndex].item.get(arg2).getPath());
                        if (mListGroup[groupIndex].selectCount == mListGroup[groupIndex].totalCount){
                            mListGroup[groupIndex].isCheckAll = true;
                        }
                    } else {
                        mSelectPicCount--;
                        mListGroup[groupIndex].selectCount--;
                        pathList.remove(mListGroup[groupIndex].item.get(arg2).getPath());
                        mListGroup[groupIndex].isCheckAll = false;
                        mListGroup[groupIndex].item.get(arg2).isSelect = false;
                    }
                    msg.what = AlbumSetPage.MSG_UPDATE_SELECTMODE_VIEW;
                    msg.obj = mSelectPicCount;
                    if (mHandler!=null) {
                        mHandler.sendMessage(msg);
                    }
                    ExAdapter.this.notifyDataSetChanged();

                    return;
                }
                // add end by yaping.liu for pr452145
                if (isSelect) {
                    CheckBox item = (CheckBox)arg1.findViewById(R.id.item_select);
                    item.setChecked(!item.isChecked());
                    Integer groupIndex = (Integer)arg0.getTag();
                    if (item.isChecked()) {
                        mSelectPicCount++;
                        mListGroup[groupIndex].selectCount++;
                        mListGroup[groupIndex].item.get(arg2).isSelect = true;
                        mSelectionManager.toggle(mListGroup[groupIndex].item.get(arg2).getPath());
                        if (mListGroup[groupIndex].selectCount == mListGroup[groupIndex].totalCount){
                            mListGroup[groupIndex].isCheckAll = true;
                        }
                    } else {
                        mSelectPicCount--;
                        mListGroup[groupIndex].selectCount--;
                        mListGroup[groupIndex].isCheckAll = false;
                        mSelectionManager.toggle(mListGroup[groupIndex].item.get(arg2).getPath());
                        mListGroup[groupIndex].item.get(arg2).isSelect = false;
                    }
                    msg.what = AlbumSetPage.MSG_UPDATE_SELECTMODE_VIEW;
                    msg.obj = ""+mSelectPicCount+"/"+mTotalPicCount;
                    if (mHandler!=null) {
                        mHandler.sendMessage(msg);
                    }
                    if (mListGroup[groupIndex].selectCount >= mListGroup[groupIndex].totalCount-1 ||
                        mListGroup[groupIndex].selectCount == 0)
                    {
                        ExAdapter.this.notifyDataSetChanged();
                    }
                } else {
                    //play photo or video
                    if(null == doubleClickeDetector) {
                        doubleClickeDetector = new DoubleClickeDetector();
                    }
                    doubleClickeDetector.detecting();
                    
                        if(!doubleClickeDetector.isDoubleClick()) {
                            msg.what = AlbumSetPage.MSG_PHOTO_SHOW;
                            msg.arg1 = (Integer)arg0.getTag();
                            msg.arg2 = arg2;
                            mHandler.sendMessage(msg);
                            doubleClickeDetector.resetDetectingState();
                        }
                }
            }

        });
        gView.setOnItemLongClickListener(new OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> arg0, View arg1,
                    int arg2, long arg3) {
                if (multipleChoice || mIsAlbumSet) return false; // add by yaping.liu for pr452145
                if (!isSelect) {
                    mSelectPicCount++;
                    Integer index = (Integer)arg0.getTag();
                    mListGroup[index].selectCount++;
                    mSelectionManager.toggle(mListGroup[index].item.get(arg2).getPath());
                    mListGroup[index].item.get(arg2).isSelect = true;
                    if (mListGroup[index].selectCount == mListGroup[index].totalCount){
                        mListGroup[index].isCheckAll = true;
                    }
                    changeListMode(true);
                    notifyDataSetChanged();
                } else {
                    if (mActivity.getGalleryActionBar().getCurrentIndex() == FilterUtils.CLUSTER_BY_ALBUM//modified by qjz for PR465800 20130607
                        &&mListGroup[(Integer) arg0.getTag()].item.get(arg2).isSelect) {
                        // move files
                         *//**********************
                         * @author yaogang.hao
                         * here to create new draggable bitmap
                         *//*
                        DragSource.group_position = (Integer) arg0.getTag();
                        DragSource.child_position = arg2;

                        if (!isdragging)
                        {
                            arg1.destroyDrawingCache();
                            arg1.setDrawingCacheEnabled(true);
                            Bitmap bitmap = Bitmap.createBitmap(arg1.getDrawingCache());
                            DragSource.drag_height = bitmap.getHeight();
                            //judge the number of selected images then change the display background of image
                            if(mSelectionManager.getSelectedCount() >1)
                            {
                                mDragBitmap = getMutipleDragBitmap(bitmap);
                            }else
                            {
                                mDragBitmap = bitmap;
                            }
                        }
                        startDragging(mDragBitmap, DragSource.drag_p_left, DragSource.drag_p_top);
                        //arg1.setVisibility(View.GONE);
                        DragSource.drag_view = arg1;
                    }
                }
                return true;//PR454627 ming.zhang modify
            }
        });
        gView.setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {

                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                    {
                        DragSource.drag_p_x = (int) event.getRawX();
                        DragSource.drag_p_y = (int) event.getRawY();
                        DragSource.drag_p_left = DragSource.drag_p_x;
                        DragSource.drag_p_top = DragSource.drag_p_y;

                        break;
                    }
                    case MotionEvent.ACTION_UP:
                        stopDragging();
                        break;
                }
                return false;
            }
        });
        return view;
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        // TODO Auto-generated method stub
        return 1;
    }

    @Override
    public Object getGroup(int groupPosition) {
        // TODO Auto-generated method stub

        return null;
    }

    @Override
    public int getGroupCount() {
        // TODO Auto-generated method stub
        // if (mListGroup != null) {
        return mGroupCount;
        // }
        // return 0;
    }

    // @Override
    public long getGroupId(int groupPosition) {
        // TODO Auto-generated method stub
        return groupPosition;
    }

    public class MyPosition {
        int groupPosition;
        int realPosition;
        public MyPosition(int group, int real) {
            groupPosition = group;
            realPosition = real;
        }
    }
    @Override
    public View getGroupView(int groupPosition, boolean isExpanded,
            View convertView, ViewGroup parent) {
        View view = convertView;
        if (view == null) {
            LayoutInflater inflater = (LayoutInflater) mContext
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = inflater.inflate(R.layout.group, null);
        }
        TextView title = (TextView) view.findViewById(R.id.folder);
        TextView count = (TextView) view.findViewById(R.id.count);

        Integer realPosition = (Integer)mRealInder.get(groupPosition);
        if (realPosition==null||mListGroup[realPosition]==null||!mListGroup[realPosition].isGet) {
            return view;
        }
        MyPosition temp = new MyPosition(groupPosition,realPosition);
        title.setTag(temp);
        view.setTag(realPosition);
        title.setText(mListGroup[realPosition].title);
        if (mListGroup[realPosition].item != null) {
            count.setText(""+mListGroup[realPosition].item.size());
        } else {
            count.setText(""+mListGroup[realPosition].totalCount);
        }
        CheckBox select = (CheckBox) view.findViewById(R.id.select);
        if (isSelect) {
            select.setChecked(mListGroup[realPosition].isCheckAll);
            select.setVisibility(View.VISIBLE);
            select.setTag(realPosition);
            select.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mIsAlbumSet) return;// add by yaping.liu for pr452145
                    CheckBox iselect = (CheckBox) v.findViewById(R.id.select);
                    Integer index = (Integer) iselect.getTag();
                    if (iselect.isChecked()) {
                        setGroupSelectState(index,true);
                    } else {
                        setGroupSelectState(index,false);
                    }
                    ImageAdapter temp = mGridViewAdapter[index];
                    if (temp != null) {
                        temp.notifyDataSetChanged();
                    }
                }
            });
        } else {
            select.setChecked(false);
            select.setVisibility(View.GONE);
        }

        ImageView image = (ImageView) view.findViewById(R.id.tubiao);
        if (isExpanded)
            image.setBackgroundResource(R.drawable.arrow_down);
        else
            image.setBackgroundResource(R.drawable.arrow_up);

        return view;
    }

    public void setGroupSelectState(int index, boolean isSelectAll) {
        //add by qjz for PR450144 20130511 begin
        if (null == mListGroup[index].item) {
            mSelectionManager.enterSelectionMode();
            return;
        }
        //add by qjz for PR450144 20130511 end
        Message msg = new Message();
        msg.arg1 = mSelectPicCount==0?1:0;
        mListGroup[index].isCheckAll = isSelectAll;
        mSelectPicCount = isSelectAll?(mSelectPicCount-mListGroup[index].selectCount+
                                        mListGroup[index].totalCount):
                                        mSelectPicCount-mListGroup[index].selectCount;
        mListGroup[index].selectCount = isSelectAll?mListGroup[index].totalCount:0;
        for (MediaItem item : mListGroup[index].item) {
            item.isSelect = isSelectAll;
            // add start by yaping.liu for pr452145
            if (multipleChoice)
            {
                if (isSelectAll)
                {
                    if (!pathList.contains(item.getPath()))
                    pathList.add(item.getPath());
                }
                else
                {
                    pathList.remove(item.getPath());
                }
                continue;
            }
            // add end by yaping.liu for pr452145
            if (isSelectAll) {
                if (!mSelectionManager.isItemSelectedNew(item.getPath())) {//modified by qjz for PR450296 20130521
                    mSelectionManager.toggle(item.getPath());
                }
            } else {
                mSelectionManager.toggle(item.getPath());
            }
        }
        msg.what = AlbumSetPage.MSG_UPDATE_SELECTMODE_VIEW;
        // modify start by yaping.liu for pr452145
        if (multipleChoice)
        {
            msg.obj = mSelectPicCount;
        }
        else
        {
            msg.obj = ""+mSelectPicCount+"/"+mTotalPicCount;
        }
        // modify end by yaping.liu for pr452145
        if (mHandler!=null) {
            mHandler.sendMessage(msg);
        }
    }
    
    @Override
    public boolean hasStableIds() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        // TODO Auto-generated method stub
        return true;
    }
    public void startLoadingThread(int position) {
        if (mGridViewAdapter[position]!=null) {
            mGridViewAdapter[position].startViewRefreshThread();
        }
    }
    public void stopLoadingThread(int position) {
        if (mGridViewAdapter[position]!=null) {
            mGridViewAdapter[position].stopViewRefreshThread();
        }
    }

    public void loadGridViewImage(int groupPosition, int height) {
        if (groupPosition < 0) {
            if (preIndex>=0) {
                mGridViewAdapter[preIndex].loadingByIndex(0);
            }
            preIndex = -1;
            return;
        }
        Integer realPosition = (Integer) mRealInder.get(groupPosition);
        preIndex = realPosition;
        NewGridView newGridView = mGridViews.get(realPosition);
        int count = newGridView.getCount();
        Log.i("ExAdapter", "count: " + count);
        if (count <= 32)
            return;
        if (mImageviewHeight == 0) {
            View childView = null;
            for (int i = 0; i < newGridView.getCount(); i++) {
                childView = newGridView.getChildAt(i);
                if (childView != null) {
                    mImageviewHeight = childView.getHeight()
                            + newGridView.getVerticalSpacing();
                    break;
                }
            }
        }

        if (mImageviewHeight == newGridView.getVerticalSpacing()) return;
        int[] winInt = new int[2];
        newGridView.getLocationInWindow(winInt);
        int orientation = mContext.getResources().getConfiguration().orientation;
        int index;
        if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
            index = ((Math.abs(height - winInt[1])) / mImageviewHeight) * 7;
        } else {
            index = ((Math.abs(height - winInt[1])) / mImageviewHeight) * 4;
        }
        Log.i("ExAdapter", "index: " + index);
        mGridViewAdapter[realPosition].loadingByIndex(index);
    }

    public class ImageAdapter extends BaseAdapter
    {
        public Object lock;
        private int mGroupId;
        private Context mContext;
        private int mThreadStep=1;
        private int mThreadFlag=-1;
        private final int STEP = 36;
        private Handler mGridViewHandler;
        private static final int REFRESH_GRIDVIEW = 565;
        private static final int UPDATE_POSITION = 656;
        private int mCount= 4;
        private int mLoadingCount=0;
        private boolean isLoadingFinish = false;
        private boolean isStartRefreshThread = false;
        private boolean isLoading = false;
        BitmapFactory.Options options = new BitmapFactory.Options();
        MediaMetadataRetriever mRetriever = new MediaMetadataRetriever();
        public AsyncBitmapLoader asyncBitmapLoader;
        private int mLoadingBeginIndex = 0;
        
        public void destroy() {
            asyncBitmapLoader.onDestory();
        }

        public void positionLoading() {
            if (mListGroup[mGroupId].item==null) {
                mCount= 0;
                return;
            }
            mLoadingBeginIndex = 0;
            if (STEP>mListGroup[mGroupId].item.size()) {
                mCount = mListGroup[mGroupId].item.size();
                return;
            }
            mCount = STEP;
            mLoadingCount = 5;
            mGridViewHandler.removeMessages(UPDATE_POSITION);
            mGridViewHandler.sendEmptyMessageDelayed(UPDATE_POSITION, 500);
        }

        public void loadingByIndex(int index) {
            mLoadingCount = 1;
            mLoadingBeginIndex = index;
            mGridViewHandler.removeMessages(UPDATE_POSITION);
            if (mListGroup[mGroupId].item!=null) {
                mGridViewHandler.sendEmptyMessageDelayed(UPDATE_POSITION, 500);
            }
        }
        public ImageAdapter(Context c, int id) {
            mContext = c;
            mGroupId = id;
            mGridViewHandler = new Handler(){
                @Override
                public void handleMessage(Message message) {
                    switch (message.what) {
                    case REFRESH_GRIDVIEW:
                        mGridViewHandler.removeMessages(REFRESH_GRIDVIEW);
                        ImageAdapter.this.notifyDataSetChanged();
                        break;
                    case UPDATE_POSITION:
                        if (mLoadingCount>0) {
                            if (mCount+STEP > mListGroup[mGroupId].item.size()){
                                mCount = mListGroup[mGroupId].item.size();
                                mLoadingCount = 0;
                            } else {
                                mCount += STEP;
                                mLoadingCount--;
                                if (mLoadingCount>0)
                                mGridViewHandler.sendEmptyMessageDelayed(UPDATE_POSITION, 300);
                            }
                            ImageAdapter.this.notifyDataSetChanged();
                        }
                        break;
                    default:
                        break;
                    }
                }
            };

            asyncBitmapLoader = new AsyncBitmapLoader(mContext);
            asyncBitmapLoader.setIdHashMap(mImageId);
            positionLoading();
            lock = new Object();
            options.inScaled=true;
            options.inSampleSize = 8;
            options.inJustDecodeBounds = false;
//            getThumbnailThread();
        }

        public int getCount()
        {
            if (mListGroup[mGroupId].item!=null) {
                mListGroup[mGroupId].totalCount=mListGroup[mGroupId].item.size();
                if (mCount < mListGroup[mGroupId].totalCount) {
                    return mCount;   
                }
                return mListGroup[mGroupId].totalCount;
            }
            return 0;
        }


        public void stopViewRefreshThread() {
            //isStartRefreshThread = false;
        }
        public void startViewRefreshThread() {
            positionLoading();
        }

        public Object getItem(int position)
        {
            return position;
        }

        public long getItemId(int position)
        {
            return position;
        }

        public View getView(int position, View convertView, ViewGroup parent)
        {
            ViewHolder viewHolder = null;
            if (convertView == null) {
                viewHolder = new ViewHolder();
                LayoutInflater inflater = (LayoutInflater) mContext
                        .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                convertView = inflater.inflate(R.layout.childitem, null);
                viewHolder.image = (ImageView) convertView.findViewById(R.id.image);
                viewHolder.is_Drm = (ImageView) convertView.findViewById(R.id.is_drm);
                viewHolder.is_video = (ImageView) convertView.findViewById(R.id.is_video);
                viewHolder.item_select = (CheckBox) convertView.findViewById(R.id.item_select);
                convertView.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) convertView.getTag();
            }
            ImageView imageView = viewHolder.image;
            MediaItem temp = mListGroup[mGroupId].item.get(position);
            if (temp == null) {
                return convertView;
            }
            if (mLoadingBeginIndex <= position && (mLoadingBeginIndex+STEP) > position ) {
                asyncBitmapLoader.loadBitmap(imageView,temp);
            } else if(position < STEP) {
                asyncBitmapLoader.loadBitmap(imageView,temp);
            } else {
                return convertView;
            }

            if (temp.getMediaType() != MediaObject.MEDIA_TYPE_VIDEO) {
                viewHolder.is_video.setVisibility(View.GONE);
            } else {
                viewHolder.is_video.setImageResource(R.drawable.ic_gallery_play);
                viewHolder.is_video.setVisibility(View.VISIBLE);
            }

             @
             * PR464501 ming.zhang modify begin
             * show DRM image‘s thumbnails lock icon
             * 
            //ImageView isDrm = (ImageView) view.findViewById(R.id.is_drm);
            if (temp.isDrm()) {
                if (0 != (temp.getSubType() & MediaObject.SUBTYPE_DRM_HAS_RIGHT)) {
                    viewHolder.is_Drm.setImageResource(com.mediatek.internal.R.drawable.drm_green_lock);
                    viewHolder.is_Drm.setVisibility(View.VISIBLE);
                } else if (0 != (temp.getSubType() & MediaObject.SUBTYPE_DRM_NO_RIGHT)) {
                    viewHolder.is_Drm.setImageResource(com.mediatek.internal.R.drawable.drm_red_lock);
                    viewHolder.is_Drm.setVisibility(View.VISIBLE);
                } else {
                    viewHolder.is_Drm.setVisibility(View.GONE);
                }
            } else {
                viewHolder.is_Drm.setVisibility(View.GONE);
            }
             PR464501 ming.zhang modify end 

            if (isSelect) {
                viewHolder.item_select.setVisibility(View.VISIBLE);
                viewHolder.item_select.setChecked(temp.isSelect);
            } else {
                viewHolder.item_select.setChecked(false);
                viewHolder.item_select.setVisibility(View.GONE);
            }
            return convertView;
        }
    }

    private class ViewHolder {
        private ImageView image;
        private ImageView is_Drm;
        private ImageView is_video;
        private CheckBox item_select;
    }
    
    public void onDestory() {
        //asyncBitmapLoader.onDestory();
    }

    *//******************************
     * modify folder name main function
     * 
     * @author yaogang.hao
     * @param filename
     * @param pos
     *//*
    public void changeFilefolderName(final String filename, final int pos)
    {

        LayoutInflater factory = LayoutInflater.from(mContext);
        final View textEntryView = factory.inflate(R.layout.alert_dialog_modify_filename, null);
        final EditText filenameTextView = (EditText) textEntryView.findViewById(R.id.username_edit);
        if (null != filename)
            filenameTextView.setText(filename);
            new AlertDialog.Builder(mContext)
                .setIconAttribute(android.R.attr.alertDialogIcon)
                .setTitle(R.string.rename_folder)
                .setView(textEntryView)
                .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                         User clicked OK so do some stuff 
                        String newfilenameString = filenameTextView.getText().toString().trim();
                        if (newfilenameString.equals(""))
                        {
                            Toast.makeText(mContext, R.string.filename_not_null, Toast.LENGTH_SHORT)
                                    .show();
                            return;
                        }
                        if (!newfilenameString.equals("filename"))
                        {
                            new RenameTask(newfilenameString, pos).execute("");
                        }

                    }
                })
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {

                         User clicked cancel so do some stuff 
                    }
                })
                .create().show();
    }

    *//********************************
     * syncronized backgroud thread which Do not block UI thread
     * 
     * @author yaogang.hao
     *//*
    private class RenameTask extends AsyncTask
    {
        private String newNameString;
        private int pos;
        File oldfile;
        File newFile;

        public RenameTask(String newname, int pos)
        {
            this.newNameString = newname;
            this.pos = pos;
        }

        @Override
        protected Object doInBackground(Object... params) {
            // PR465436 ming.zhang modify begin
            boolean result;
            String path;
            if (mListGroup[this.pos].setPath == null) {
                if (mListGroup[this.pos].path == null) {
                    return false;
                }
                path = mListGroup[this.pos].path;
                oldfile = new File(path);
                newFile = new File(oldfile.getParent() + "/" + this.newNameString.trim());
                result = oldfile.renameTo(newFile);
            } else {
                try {
                    Uri tempUri = manager.getContentUri(mListGroup[this.pos].setPath);
                    String urlstring = tempUri.toString();
                    String idtemp = urlstring.substring(urlstring.lastIndexOf("=") + 1);

                    String[] projection = new String[] {
                            android.provider.MediaStore.Files.FileColumns._ID,
                            android.provider.MediaStore.Files.FileColumns.DATA,
                    };
                    Cursor cursor = contentResolver.query(tempUri, projection, "bucket_id=?",
                            new String[] {
                            idtemp
                    }, null);
                    cursor.moveToFirst();
                    int dataindex = cursor
                            .getColumnIndex(android.provider.MediaStore.Files.FileColumns.DATA);
                    path = cursor.getString(dataindex);
                    cursor.close();
                    oldfile = new File(path);
                    oldfile = new File(oldfile.getParent());
                    newFile = new File(oldfile.getParent() + "/" + this.newNameString.trim());
                    result = oldfile.renameTo(newFile);
                } catch (Exception e) {
                    return false;
                }
            }
            return result;
            // PR465436 ming.zhang modify end
        }

        @Override
        protected void onPostExecute(Object result) {

            boolean bool = (Boolean) result;
            if (bool)
            {
                updateInMediaStore(newFile.getAbsolutePath(), oldfile.getAbsolutePath(), null);
                mListGroup[pos].title = newNameString;
                ExAdapter.this.notifyDataSetChanged();
            }
            showToast(bool);
        }

    }

    *//****************************************
     * update media files database
     * 
     * @author yaogang.hao
     * @param newPath
     * @param oldPath
     *//*
    public void updateInMediaStore(String newPath, String oldPath, OnScanCompletedListener listener) {

        if (mContext != null && !TextUtils.isEmpty(newPath) && !TextUtils.isEmpty(newPath)) {
            IContentProvider mediaProvider = mContext.getContentResolver().acquireProvider("media");
            Uri uri = MediaStore.Files.getMtpObjectsUri("external");
            uri = uri.buildUpon().appendQueryParameter("mtk_filemanager", "true").build();
            String where = MediaStore.Files.FileColumns.DATA + "=?";
            String[] whereArgs = new String[] {
                oldPath
            };
            ContentValues values = new ContentValues();
            values.put(MediaStore.Files.FileColumns.DATA, newPath);
            whereArgs = new String[] {
                oldPath
            };
            try {
                mediaProvider.update(uri, values, where, whereArgs);
                scanPathforMediaStore(newPath, listener);
            } catch (RemoteException e) {
            }
        }
    }

    public void scanPathforMediaStore(String path, OnScanCompletedListener listener) {
        if (mContext != null && !TextUtils.isEmpty(path)) {
            String[] paths = {
                    path
            };
            MediaScannerConnection.scanFile(mContext, paths, null, listener);
        }
    }

    public void showToast(boolean is)
    {
        String text = is ? mContext.getString(R.string.folder_rename_success) : mContext
                .getString(R.string.folder_rename_fail);
        Toast.makeText(mContext, text, Toast.LENGTH_SHORT).show();
    }

    *//***********************
     * begin to drag image
     * 
     * @param bm
     * @param x
     * @param y
     *//*
    private void startDragging(Bitmap bm, int x, int y) {

        mWindowParams = new WindowManager.LayoutParams();
        mWindowParams.gravity = Gravity.TOP | Gravity.LEFT;
        mWindowParams.x = x-150;
        mWindowParams.y = y-150;

        mWindowParams.height = WindowManager.LayoutParams.WRAP_CONTENT;
        mWindowParams.width = WindowManager.LayoutParams.WRAP_CONTENT;
        mWindowParams.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                | WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
                | WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
                | WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN;
        mWindowParams.format = PixelFormat.TRANSLUCENT;
        mWindowParams.windowAnimations = 0;

        ImageView v = new ImageView(mContext);
        v.setAlpha(150);
        v.setImageBitmap(mDragBitmap);
        mWindowManager = (WindowManager) mContext.getSystemService("window");
        mWindowManager.addView(v, mWindowParams);
        mDragView = v;
        isdragging = true;
    }

    *//********************
     * stop drag image
     *//*
    public void stopDragging() {
        if (isdragging)
        {
            isdragging = false;
            if (mDragView != null) {
                WindowManager wm = (WindowManager) mContext.getSystemService("window");
                wm.removeView(mDragView);
                mDragView.setImageBitmap(null);
                mDragView = null;
            }
            if (mDragBitmap != null) {
                mDragBitmap.recycle();
                mDragBitmap = null;
            }
        }
    }

    public boolean isDragging()
    {
        return this.isdragging;
    }
   *//******************
    * @author yaogang.hao
    * @param x
    * @param y
    *  To update the new location of the top view
    *//*
    public void drawDragView(int x, int y)
    {
        if (isdragging)
        {
            mWindowParams.x = x-150;
            mWindowParams.y = y-150;
            mWindowManager.updateViewLayout(mDragView, mWindowParams);
        }
    }

    *//*******************************
     * @author yaogang.hao
     * @param srcGroupPosition the source group folder
     * @param srcChildPosition the sub-image of source group folder
     * @param dstGroupPosition the destination group folder 
     * function: Drag the selected image from source folder into 
     * another folder,and then copy file ,delete file,and it is necessery to 
     * update your URi 
     *//*
    public void TransformView(int srcGroupPosition, int srcChildPosition, int dstGroupPosition)
    {
        if (srcGroupPosition != dstGroupPosition)
        {
            MediaItem temp = mListGroup[srcGroupPosition].item.remove(srcChildPosition);
            mListGroup[srcGroupPosition].totalCount = mListGroup[srcGroupPosition].totalCount - 1;
            //add by qjz for PR450144 begin
            if (mListGroup[dstGroupPosition].item != null) {
                mListGroup[dstGroupPosition].item.add(temp);
            }
            //add by qjz for PR450144 end
            mListGroup[dstGroupPosition].totalCount = mListGroup[dstGroupPosition].totalCount + 1;
            Uri tempUri = manager.getContentUri(temp.getPath());
            Uri dstdirUri;
            if (mListGroup[dstGroupPosition].setPath != null) {
                dstdirUri = manager.getContentUri(mListGroup[dstGroupPosition].setPath);
            } else {
                dstdirUri = Uri.parse(mListGroup[dstGroupPosition].path);
            }
            new TransformTask(dstdirUri, tempUri).execute("");
        }
        Uri dstdirUri;
        if (mListGroup[dstGroupPosition].setPath != null) {
            dstdirUri = manager.getContentUri(mListGroup[dstGroupPosition].setPath);
        } else {
            dstdirUri = Uri.parse(mListGroup[dstGroupPosition].path);
        }
         new TransformTask(dstdirUri).execute("");
    }

    *//******************************
     * 
     * @author yaogang.hao
     * because of mant task be being tasked,
     * here I use the AsyncTask which is likely to backgound Thread
     * and it will avoid  occuring ANR exception
     *//*
    private class TransformTask extends AsyncTask
    {
        private Uri newUri;
        private Uri oldUri;
        private String oldfilepath;
        private String newFilepath;

        public TransformTask(Uri newp) {
            this.newUri = newp;
        }

        @Override
        protected Object doInBackground(Object... params) {
            Uri updateUri = null;

            // uri FILE parse
            String[] projectionfile = new String[] {
                    android.provider.MediaStore.Images.Media.DATA,
            };
            // uri folder parse
            String urlstring = newUri.toString();
            String idtemp = urlstring.substring(urlstring.lastIndexOf("=") + 1);

            String[] projection = new String[] {
                    android.provider.MediaStore.Files.FileColumns._ID,
                    android.provider.MediaStore.Files.FileColumns.DATA,};
            Cursor cursor = contentResolver.query(newUri, projection, "bucket_id=?", new String[] {
                    idtemp}, null);
            //add by qjz for PR450144 begin
            if (cursor != null) {
                cursor.moveToFirst();
                int dataindex2 = cursor.getColumnIndex(android.provider.MediaStore.Files.FileColumns.DATA);
                newFilepath = cursor.getString(dataindex2);
                File file = new File(newFilepath);
                newFilepath = file.getParent();
            } else {
                newFilepath = newUri.toString();
            }
            //add by qjz for PR450144 end
            Set<Path> selectSetPath = mSelectionManager.getSelectItems();
            for (Path id : selectSetPath) {
                oldUri = manager.getContentUri(id);
                cursor = contentResolver.query(oldUri, projectionfile, null, null, null);
                cursor.moveToFirst();
                int dataindex = cursor.getColumnIndex(android.provider.MediaStore.Images.Media.DATA);
                oldfilepath = cursor.getString(dataindex);
                
                try {
                    // copy file
                    File srcfile = new File(oldfilepath);
                    String filename = srcfile.getName();
                    File dstFile = new File(newFilepath + "/" + filename);
                    srcfile.renameTo(dstFile);
                    // update database
                    updateInMediaStore(dstFile.getAbsolutePath(), srcfile.getAbsolutePath(), null);
                    //Thread.sleep(1);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
            cursor.close();
            Cursor cursor = contentResolver.query(oldUri, projectionfile, null, null, null);
            cursor.moveToFirst();
            int dataindex = cursor
                    .getColumnIndex(android.provider.MediaStore.Images.Media.DATA);
            oldfilepath = cursor.getString(dataindex);
            // uri folder parse
            String urlstring = newUri.toString();
            String idtemp = urlstring.substring(urlstring.lastIndexOf("=") + 1);

            String[] projection = new String[] {
                    android.provider.MediaStore.Files.FileColumns._ID,
                    android.provider.MediaStore.Files.FileColumns.DATA,
            };
            cursor = contentResolver.query(newUri, projection, "bucket_id=?", new String[] {
                    idtemp
            }, null);
            //add by qjz for PR450144 begin
            if (cursor != null) {
                cursor.moveToFirst();
                int dataindex2 = cursor
                        .getColumnIndex(android.provider.MediaStore.Files.FileColumns.DATA);
                newFilepath = cursor.getString(dataindex2);
                File file = new File(newFilepath);
                newFilepath = file.getParent();
            } else {
                newFilepath = newUri.toString();
            }
            //add by qjz for PR450144 end
            try {
                // copy file
                File srcfile = new File(oldfilepath);
                String filename = srcfile.getName();
                File dstFile = new File(newFilepath + "/" + filename);
                srcfile.renameTo(dstFile);
                // update database
                updateInMediaStore(dstFile.getAbsolutePath(), srcfile.getAbsolutePath(), null);
                
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            return updateUri;
        }

        @Override
        protected void onPostExecute(Object result) {
            Uri uri = (Uri) result;
            mHandler.sendEmptyMessage(AlbumSetPage.MSG_MOVING_COMPLETE);
        }
    }
    *//**
     * 
     * @author yaogang.hao
     * Doublc click event detector
     *
     *//*
   private class DoubleClickeDetector
   {
       private boolean isDoubleClicked = false;
       private int clickcount = 0;
       private long time1 = 0;
       private long time2 = 0;
       
       
       *//**
        * count the click times
        *//*
       public void detecting()
       {
           clickcount++;
           if(clickcount == 1)
           {
               time1 =  System.currentTimeMillis();
           }
           else if(clickcount == 2)
           {
               time2 =  System.currentTimeMillis();
               //Time difference  is 1000ms
               if(time2-time1<1000)
               {
                   isDoubleClicked = true;
                   
               }
           }
       }
       
       public boolean isDoubleClick()
       {
           return isDoubleClicked;
       }
       *//**
        * start timer to reset state.
        *//*
       public void resetDetectingState()
       {
           new Timer().schedule(new TimerTask(){
               public void run()
               {
                   clickcount = 0;
                   time1 = 0;
                   time2 = 0;
                   isDoubleClicked = false;
               }
           },1000);
       }
       
   }
   //yaogang.hao
   public Bitmap getMutipleDragBitmap(Bitmap bitmap)
   {
       Paint temp = new Paint();
       temp.setAlpha(120);
       temp.setAntiAlias(true);
       Bitmap mutipleBitmap =  Bitmap.createBitmap(bitmap.getWidth()+120, bitmap.getHeight()+120, Bitmap.Config.ARGB_8888);
       Canvas canvas = new Canvas(mutipleBitmap);
       canvas.save();
       canvas.rotate(10.0f,(bitmap.getWidth()+120)/2*1.0f,(bitmap.getHeight()+120)/2*1.0f);
       canvas.drawBitmap(bitmap,60.0f,60.0f,temp);
       canvas.restore();
       
       canvas.save();
       canvas.rotate(20.0f,(bitmap.getWidth()+120)/2*1.0f,(bitmap.getHeight()+120)/2*1.0f);
       canvas.drawBitmap(bitmap,60.0f,60.0f,temp);
       canvas.restore();
       
       canvas.drawBitmap(bitmap,60,60,new Paint());
      return mutipleBitmap;
   }*/
}
