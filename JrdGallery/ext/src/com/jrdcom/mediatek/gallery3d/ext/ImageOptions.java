package com.jrdcom.mediatek.gallery3d.ext;

public class ImageOptions implements IImageOptions {

    @Override
    public boolean shouldUseOriginalSize() {
        return false;
    }
    
    @Override
    public boolean shouldReturnTopWhenBack() {
        return false;
    }

}
