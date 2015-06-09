package com.jrdcom.example.joinpic;
/**
 * @author yaogang.hao@tct-nj.com
 * This class is a wrapper for image which is in sdcard.
 * 
 */
import android.graphics.Bitmap;
import com.jrdcom.android.gallery3d.R;
public class Image {
    private int id;
    private String title;
    private String displayName;
    private String mimeType;
    private String path;
    private String dir;
    private long size;
    private boolean isSelected = false;

    private int tempPos = -1;
    /***Bitmaps**/
    private Bitmap bitmap = null;
    public Image() {
        super();
    }

    /**
     * @param id
     * @param title
     * @param displayName
     * @param mimeType
     * @param path
     * @param size
     */
    public Image(int id, String title, String displayName, String mimeType,String path
           ) {
        super();
        this.id = id;
        this.title = title;
        this.displayName = displayName;
        this.mimeType = mimeType;
        this.path = path;
       
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getMimeType() {
        return mimeType;
    }

    public void setMimeType(String mimeType) {
        this.mimeType = mimeType;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public long getSize() {
        return size;
    }

    public void setSize(long size) {
        this.size = size;
    }

    public String getDir() {
        return dir;
    }

    public void setDir(String dir) {
        this.dir = dir;
    }

    public Bitmap getBitmap() {
        return bitmap;
    }

    public void setBitmap(Bitmap bitmap) {
        this.bitmap = bitmap;
    }

    public boolean isSelected() {
        return isSelected;
    }

    public void setSelected(boolean isSelected) {
        this.isSelected = isSelected;
    }

    public int getTempPos() {
        return tempPos;
    }

    public void setTempPos(int tempPos) {
        this.tempPos = tempPos;
    }
    
    
    
}
