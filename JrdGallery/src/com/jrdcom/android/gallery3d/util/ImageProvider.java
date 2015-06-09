package com.jrdcom.android.gallery3d.util;
/**
 * @author yaogang.hao@tct-nj.com
 * This class is a helper class for image get data from media database
 * 
 */
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import com.jrdcom.android.gallery3d.R;
import com.jrdcom.example.joinpic.AbstructProvider;
import com.jrdcom.android.gallery3d.util.Image;

import android.content.Context;
import android.database.Cursor;
import android.provider.MediaStore;

public class ImageProvider implements AbstructProvider {
    private Context context;
    private HashMap<String,Integer> mImageId = new HashMap<String,Integer>();
    private List<ImageAlbum> mAblbum= new ArrayList<ImageAlbum>();

    public ImageProvider(Context context) {
        this.context = context;
    }

    @Override
    public List<?> getList() {
        List<Image> list = null;
        if (context != null) {
            Cursor cursor = context.getContentResolver().query(
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI, null, null,
                    null, null);
            if (cursor != null) {
                list = new ArrayList<Image>();
                while (cursor.moveToNext()) {
                    int id = cursor
                            .getInt(cursor
                                    .getColumnIndexOrThrow(MediaStore.Images.Media._ID));
                    String title = cursor
                            .getString(cursor
                                    .getColumnIndexOrThrow(MediaStore.Images.Media.TITLE));
                    String path = cursor
                            .getString(cursor
                                    .getColumnIndexOrThrow(MediaStore.Images.Media.DATA));
                    String displayName = cursor
                            .getString(cursor
                                    .getColumnIndexOrThrow(MediaStore.Images.Media.DISPLAY_NAME));
                    String mimeType = cursor
                            .getString(cursor
                                    .getColumnIndexOrThrow(MediaStore.Images.Media.MIME_TYPE));
                    String dirname = cursor
                            .getString(cursor
                                    .getColumnIndexOrThrow(MediaStore.Images.Media.BUCKET_DISPLAY_NAME));
                    
                    long size = cursor
                            .getLong(cursor
                                    .getColumnIndexOrThrow(MediaStore.Images.Media.SIZE));
                    Image audio = new Image(id, title, displayName, mimeType,path
                            );
                    
                    list.add(audio);
                }
                cursor.close();
            }
        }
        return list;
    }
    
    
    public List<ImageAlbum> getAlbumList()
    {
        //List<ImageAlbum> list = null;
        if (context != null) {
            String[] projection = {
                    
                    MediaStore.Images.Media._ID, 
                    MediaStore.Images.Media.TITLE,
                    MediaStore.Images.Media.DATA,
                    MediaStore.Images.Media.DISPLAY_NAME,
                    MediaStore.Images.Media.MIME_TYPE,
                    MediaStore.Images.Media.BUCKET_DISPLAY_NAME,
                    MediaStore.Images.Media.BUCKET_ID,
                    "COUNT(*) AS totalNum"
                    
            };
            String where ="0==0) Group by ("+MediaStore.Images.Media.BUCKET_ID;
            Cursor cursor = context.getContentResolver().query(
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI, projection, where,
                    null, null);
            if (cursor != null) {
                //list = new ArrayList<ImageAlbum>();
                while (cursor.moveToNext()) {
                    int id = cursor
                            .getInt(cursor
                                    .getColumnIndexOrThrow(MediaStore.Images.Media._ID));
                    String title = cursor
                            .getString(cursor
                                    .getColumnIndexOrThrow(MediaStore.Images.Media.TITLE));
                    String path = cursor
                            .getString(cursor
                                    .getColumnIndexOrThrow(MediaStore.Images.Media.DATA));
                    String displayName = cursor
                            .getString(cursor
                                    .getColumnIndexOrThrow(MediaStore.Images.Media.DISPLAY_NAME));
                    String mimeType = cursor
                            .getString(cursor
                                    .getColumnIndexOrThrow(MediaStore.Images.Media.MIME_TYPE));
                    String dirname = cursor
                            .getString(cursor
                                    .getColumnIndexOrThrow(MediaStore.Images.Media.BUCKET_DISPLAY_NAME));
                    if(path.lastIndexOf("/") != -1)
                        path = path.substring(0, path.lastIndexOf("/"));
                    String buket_id = cursor
                            .getString(cursor
                                    .getColumnIndexOrThrow(MediaStore.Images.Media.BUCKET_ID));
                    String count = cursor.getString(7);
                    ImageAlbum album = new ImageAlbum(count, dirname, id, buket_id, path);
                    //list.add(album);
                    mAblbum.add(album);
                }
                cursor.close();
            }
        }
        return mAblbum;
    }
    
    public List<Image> getAlbumChildList(ImageAlbum imageAlbum)
    {
        List<Image> list = null;
        if (context != null) {
            String[] projection = {
                    
                    MediaStore.Images.Media._ID, 
                    MediaStore.Images.Media.TITLE,
                    MediaStore.Images.Media.DATA,
                    MediaStore.Images.Media.DISPLAY_NAME,
                    MediaStore.Images.Media.MIME_TYPE,
                    MediaStore.Images.Media.BUCKET_DISPLAY_NAME,
                    MediaStore.Images.Media.BUCKET_ID,
                    
            };
            String where =MediaStore.Images.Media.BUCKET_ID + "="+imageAlbum.getBuket_id();
            Cursor cursor = context.getContentResolver().query(
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI, projection, where,
                    null, null);
            if (cursor != null ) {
                list = new ArrayList<Image>();
                while (cursor.moveToNext()) {
                    int id = cursor
                            .getInt(cursor
                                    .getColumnIndexOrThrow(MediaStore.Images.Media._ID));
                    String title = cursor
                            .getString(cursor
                                    .getColumnIndexOrThrow(MediaStore.Images.Media.TITLE));
                    String path = cursor
                            .getString(cursor
                                    .getColumnIndexOrThrow(MediaStore.Images.Media.DATA));
                    String displayName = cursor
                            .getString(cursor
                                    .getColumnIndexOrThrow(MediaStore.Images.Media.DISPLAY_NAME));
                    String mimeType = cursor
                            .getString(cursor
                                    .getColumnIndexOrThrow(MediaStore.Images.Media.MIME_TYPE));
                    String dirname = cursor
                            .getString(cursor
                                    .getColumnIndexOrThrow(MediaStore.Images.Media.BUCKET_DISPLAY_NAME));
                    String buket_id = cursor
                            .getString(cursor
                                    .getColumnIndexOrThrow(MediaStore.Images.Media.BUCKET_ID));
                    
                    Image image = new Image(id, title, displayName, mimeType,path);
                    list.add(image);
                }
                cursor.close();
            }
            
        }
        return list;
    }

    public HashMap<String,Integer> getAlbumChildId(ImageAlbum imageAlbum)
    {
        if (context != null) {
            String[] projection = {
                    
                    MediaStore.Images.Media._ID, 
                    MediaStore.Images.Media.TITLE,
                    MediaStore.Images.Media.DATA,
                    MediaStore.Images.Media.DISPLAY_NAME,
                    MediaStore.Images.Media.MIME_TYPE,
                    MediaStore.Images.Media.BUCKET_DISPLAY_NAME,
                    MediaStore.Images.Media.BUCKET_ID,
                    
            };
            String where =MediaStore.Images.Media.BUCKET_ID + "="+imageAlbum.getBuket_id();
            Cursor cursor = context.getContentResolver().query(
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI, projection, where,
                    null, null);
            if (cursor != null ) {
                while (cursor.moveToNext()) {
                    int id = cursor
                            .getInt(cursor
                                    .getColumnIndexOrThrow(MediaStore.Images.Media._ID));
                    String title = cursor
                            .getString(cursor
                                    .getColumnIndexOrThrow(MediaStore.Images.Media.TITLE));
                    String path = cursor
                            .getString(cursor
                                    .getColumnIndexOrThrow(MediaStore.Images.Media.DATA));
                    String displayName = cursor
                            .getString(cursor
                                    .getColumnIndexOrThrow(MediaStore.Images.Media.DISPLAY_NAME));
                    String mimeType = cursor
                            .getString(cursor
                                    .getColumnIndexOrThrow(MediaStore.Images.Media.MIME_TYPE));
                    String dirname = cursor
                            .getString(cursor
                                    .getColumnIndexOrThrow(MediaStore.Images.Media.BUCKET_DISPLAY_NAME));
                    String buket_id = cursor
                            .getString(cursor
                                    .getColumnIndexOrThrow(MediaStore.Images.Media.BUCKET_ID));
                    
                    Image image = new Image(id, title, displayName, mimeType,path);
                    mImageId.put(path, id);
                }
                cursor.close();
            }            
        }
        return mImageId;
    }
    public void clearIdHashMap() {
    	mImageId.clear();
    }
    public HashMap<String,Integer> getIdHashMap() {
    	return mImageId;
    }
}
