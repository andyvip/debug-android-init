package com.jrdcom.timetool.alarm.provider;

import java.io.File;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Environment;
import com.android.deskclock.R;

public class RingtoneList {
    public static final String MEDIA_PATH_DEFAULT = Environment.getRootDirectory().toString()
            + "/media/audio/alarms";

    public static final String MEDIA_PATH_ALCATEL = Environment.getRootDirectory().toString()
            + "/media_alcatel/audio/alarms";

    public static final String TIMER_RPREFERENCES = "timetool.timer";

    public static final String ALERT_RINGTONE_PATH_KEY = "alert.ringtone.path";
    
    public static final String ALERT_RINGTONE_NAME_KEY = "alert.ringtone.name";

    public static final String ALERT_SILENT_PATH = "silent";

    static String[] ringItems = null;

    static String preRingtonePath;

    static int checkedItem;

    static String mediaPath = MEDIA_PATH_DEFAULT;

    static String company = "";

    public final static File[] mediaFiles = new File(mediaPath).listFiles();

    public static String[] getRingtoneList(Context context) {

        if ("ALCATEL".equals(company) || "FPT".equals(company)) {
            mediaPath = MEDIA_PATH_ALCATEL;
        }
        if (mediaFiles == null || mediaFiles.length <= 0) {
            ringItems = new String[1];
            // add by caorongxing for PR:431861 begin
            //ringItems[0] = context.getString(R.string.timer_ringtone_silent);
            ringItems[0] = "Silent";
            // add by caorongxing for PR:431861 end
        } else {
            ringItems = new String[mediaFiles.length + 1];
            // add by caorongxing for PR:431861 begin
            //ringItems[0] = context.getString(R.string.timer_ringtone_silent);
            ringItems[0] = "Silent";
            // add by caorongxing for PR:431861 end
            for (int i = 0; i < mediaFiles.length; i++) {
                File mediaFile = mediaFiles[i];
                ringItems[i + 1] = mediaFile.getName();// add silent position
            }
        }
        
        /*PR 651115- Neo Skunkworks - Paul Xu added - 001 Begin*/
        if(ringItems == null){
            ringItems = new String[1];
            ringItems[0] = "Silent";
        }
        /*PR 651115- Neo Skunkworks - Paul Xu added - 001 End*/
        
        return ringItems;
    }

    public static String getPreRingtonePath(Context context) {

        // retrieve the former ring
        final SharedPreferences sharedPre = context.getSharedPreferences(TIMER_RPREFERENCES, 0);
        preRingtonePath = sharedPre.getString(ALERT_RINGTONE_PATH_KEY, "");

        // when the former ring is not silent
        if (!ALERT_SILENT_PATH.equals(preRingtonePath)) {
            // when the ring does not exit ,set to be silent
            if (mediaFiles == null || mediaFiles.length <= 0) {
                sharedPre.edit().putString(ALERT_RINGTONE_PATH_KEY, ALERT_SILENT_PATH).commit();
            } else {
                boolean isFindPreMediaFile = false;
                for (int i = 0; i < mediaFiles.length; i++) {
                    if (preRingtonePath.equals(mediaFiles[i].getAbsolutePath())) {
                        isFindPreMediaFile = true;
                        break;
                    }
                }
                // when do not find the former ring
                if (!isFindPreMediaFile) {
                    if (preRingtonePath == null) {
                        sharedPre.edit().putString(ALERT_RINGTONE_PATH_KEY, ALERT_SILENT_PATH)
                                .commit();
                    } else if (preRingtonePath != null) {
                        checkedItem = mediaFiles.length + 1;
                    }
                }
            }
        }
        return preRingtonePath;
    }
}
