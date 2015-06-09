package com.jrdcom.android.gallery3d.util;
/**
 * @author yaogang.hao@tct-nj.com
 * This class is Image floders wrapper.
 * 
 */
import java.io.File;
import java.util.ArrayList;
import java.util.List;

import android.graphics.Bitmap;
import com.jrdcom.android.gallery3d.R;
public class ImageAlbum {

    String count = "";//image total number
    String ablumName = "";//directory name
    int imageid = 0;//thumb image
    String buket_id = "";//directory id 
    String fimgpath ="";
    String ablumPath = "";
    Bitmap thumbNail;
    List<Image> childImageslist = null;
    
    public ImageAlbum(String count, String ablumName, int imageid, String buket_id,
            String fimgpath) {
        super();
        this.count = count;
        this.ablumName = ablumName;
        this.imageid = imageid;
        this.buket_id = buket_id;
        this.fimgpath = fimgpath;
        File temp = new File(fimgpath);
        this.ablumPath = temp.getParent();
    }
    
    public String getCount() {
        return count;
    }
    public void setCount(String count) {
        this.count = count;
    }
    public String getAblumName() {
        return ablumName;
    }
    public void setAblumName(String ablumName) {
        this.ablumName = ablumName;
    }
    public int getImageid() {
        return imageid;
    }
    public void setImageid(int imageid) {
        this.imageid = imageid;
    }
    public String getBuket_id() {
        return buket_id;
    }
    public void setBuket_id(String buket_id) {
        this.buket_id = buket_id;
    }
    public String getFimgpath() {
        return fimgpath;
    }
    public void setFimgpath(String fimgpath) {
        this.fimgpath = fimgpath;
    }
    public String getAblumPath() {
        return ablumPath;
    }
    public void setAblumPath(String ablumPath) {
        this.ablumPath = ablumPath;
    }

    public Bitmap getThumbNail() {
        return thumbNail;
    }

    public void setThumbNail(Bitmap thumbNail) {
        this.thumbNail = thumbNail;
    }

    public List<Image> getChildImageslist() {
        return childImageslist;
    }

    public void setChildImageslist(List<Image> childImageslist) {
        this.childImageslist = childImageslist;
    }
    
    
}
