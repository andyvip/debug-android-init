/*
 * Copyright (C) 2007 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.jrdcom.android.gallery3d.app;

import java.io.File;
import java.util.HashMap;

import com.jrdcom.android.gallery3d.R;
import com.jrdcom.android.gallery3d.data.DataManager;
import com.jrdcom.android.gallery3d.util.ExAdapter;

import android.app.ActionBar;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

public class HideAlbumActivity extends Activity {

    /*private ImageView mBackImage;
    private TextView mSelectText;
    private ListView mHideList;
    private HideAlbumAdapter mHideAdapter;
    private int mHideCount = 0;
    private int mAlbumCount = 0;
    private HashMap <String,Object> mHideKey;
    private ExAdapter.ListGroup [] mAlbumData;
    private boolean isLoading = false;
    private int mLoadingCount = 0;
    public static final int REFRESH_LISTVIEW = 13;
    DataManager manager = AlbumSetPage.mExAdapter.getDataManager();
    ContentResolver contentResolver = AlbumSetPage.mExAdapter.getContentResolver();
    String[] projectionfile = new String[] {
            android.provider.MediaStore.Images.Media.DATA,};
    private Handler mListViewHandler = new Handler(){
        @Override
        public void handleMessage(Message message) {
            switch (message.what) {
            case REFRESH_LISTVIEW:
                mListViewHandler.removeMessages(REFRESH_LISTVIEW);
                if (isLoading&&mLoadingCount<mAlbumCount) {
                    if (mAlbumData[mLoadingCount].path ==null&&mAlbumData[mLoadingCount].item!=null) {
                        Cursor cursor = contentResolver.query(manager.getContentUri(mAlbumData[mLoadingCount].item.get(0).getPath()), projectionfile, null, null, null);
                        cursor.moveToFirst();
                        int dataindex = cursor.getColumnIndex(android.provider.MediaStore.Images.Media.DATA);
                        File file = new File(cursor.getString(dataindex));
                        mAlbumData[mLoadingCount].path = file.getParent();
                        cursor.close();
                    }
                    mLoadingCount++;
                    mHideAdapter.notifyDataSetChanged();
                    //mListViewHandler.sendEmptyMessageDelayed(REFRESH_LISTVIEW,8);
                    mListViewHandler.sendEmptyMessage(REFRESH_LISTVIEW);
                } else {
                    isLoading = false;
                }
                break;
            default:
                break;
            }
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.hide_album);
        ActionBar actionBar = getActionBar();
        View customView = LayoutInflater.from(this).inflate(
                R.layout.action_bar_hide_album, null);
        actionBar.setCustomView(customView);
        //actionBar.setCustomView(R.layout.action_bar_hide_album);
        actionBar.setDisplayShowHomeEnabled(false);
        actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        //actionBar.setNavigationMode(ActionBar.DISPLAY_SHOW_CUSTOM);
        mSelectText = (TextView)customView.findViewById(R.id.hide_album_bar_text);
        mAlbumCount = AlbumSetPage.mExAdapter.getAlbumCount();
        mHideKey = AlbumSetPage.mExAdapter.getHideHashMap();
        mAlbumData = AlbumSetPage.mExAdapter.getListData();
        for (int i=0;i<mAlbumCount;i++) {
            if((mHideKey.get(mAlbumData[i].path)!=null?(Boolean)mHideKey.get(mAlbumData[i].path):false)) {//modified by qjz for PR463426
                mHideCount++;
            }
        }
        mSelectText.setText(""+mHideCount+"/"+mAlbumCount);
        mBackImage = (ImageView)customView.findViewById(R.id.hide_album_back);
        mBackImage.setOnClickListener(new OnClickListener(){
            @Override
            public void onClick(View v) {
                isLoading = false;
                HideAlbumActivity.this.finish();
            }
        });        

        mHideAdapter = new HideAlbumAdapter(this);
        mHideList = (ListView) findViewById(R.id.hide);
        mHideList.setAdapter(mHideAdapter);
        mHideList.setOnItemClickListener(new OnItemClickListener(){
            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                    int position, long id) {
                boolean check = !(Boolean)mHideKey.get(mAlbumData[position].path);
                if (check) {
                    AlbumSetPage.mHideAlbumInfo.add(mAlbumData[position].path);
                    mHideCount++;
                } else {
                    AlbumSetPage.mHideAlbumInfo.remove(mAlbumData[position].path);
                    mHideCount--;
                }
                mSelectText.setText(""+mHideCount+"/"+mAlbumCount);
                mHideKey.put(mAlbumData[position].path, check);
                AlbumSetPage.mExAdapter.setHideValue(mAlbumData[position].path, check);
                mHideAdapter.notifyDataSetChanged();
            }
        });
        isLoading = true;
        mListViewHandler.sendEmptyMessage(REFRESH_LISTVIEW);
    }
    
    @Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
        HideAlbumActivity.this.setResult(AlbumSetPage.STATIC_ACTIVITY_RESULT_CODE);
		super.onDestroy();
	}

	@Override
    public void onBackPressed() {
        // TODO Auto-generated method stub
        isLoading = false;
        super.onBackPressed();
    }

    public class HideAlbumAdapter extends BaseAdapter
    {
        private Context mContext;

        public HideAlbumAdapter(Context c) {
            mContext = c;
            //update();
        }

        public int getCount() {
            return mLoadingCount;
        }

        public Object getItem(int position) {
            return position;
        }

        public long getItemId(int position) {
            return position;
        }

        public View getView(int position, View convertView, ViewGroup parent) {
            View view = convertView;
            if (view == null) {
                LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                view = inflater.inflate(R.layout.hide_album_item, null);
            }
            TextView folder = (TextView)view.findViewById(R.id.folder);
            TextView path = (TextView)view.findViewById(R.id.folder_path);
            
            folder.setText(mAlbumData[position].title);
            //path.setText(mAlbumData[position].setPath.toString());
            path.setText(mAlbumData[position].path!=null?mAlbumData[position].path:mAlbumData[position].setPath.toString());
            
            CheckBox hide = (CheckBox)view.findViewById(R.id.ishide);
            hide.setTag(position);
            hide.setChecked((Boolean)mHideKey.get(mAlbumData[position].path));            
            return view;
        }
    }*/
}
