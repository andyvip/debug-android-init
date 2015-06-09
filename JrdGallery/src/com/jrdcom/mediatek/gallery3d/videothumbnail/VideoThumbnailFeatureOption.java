package com.jrdcom.mediatek.gallery3d.videothumbnail;

public class VideoThumbnailFeatureOption {
    // constant section start
    // --------------------------------------------------------------------
    static final int OPTION_RENDER_TYPE_REGARDLESS_OF_ASPECT_RATIO = 0;
    static final int OPTION_RENDER_TYPE_KEEP_ASPECT_RATIO = 1;
    static final int OPTION_RENDER_TYPE_CROP_CENTER = 2;
    // --------------------------------------------------------------------
    // constant section end

    // configuration section start
    // --------------------------------------------------------------------
    // DO NOT enable this feature right now
    static final boolean OPTION_MONITOR_LOADING = false;
    // for test purpose (only test playing), set the below to be false
    static final boolean OPTION_TRANSCODE_BEFORE_PLAY = true;
    static final boolean OPTION_PREPARE_ASYNC = false;
    static final int OPTION_RENDER_TYPE = OPTION_RENDER_TYPE_CROP_CENTER;
    // --------------------------------------------------------------------
    // configuration section end
}
