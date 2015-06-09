
package com.jrdcom.timetool.alarm.activity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.database.Cursor;
import android.drm.DrmManagerClient;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.provider.Settings;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.android.deskclock.R;
import com.jrdcom.timetool.alarm.provider.HelpAdapter;
import com.jrdcom.timetool.alarm.provider.RingtoneList;
import com.jrdcom.timetool.alarm.service.ReflectionTool;
import com.jrdcom.timetool.countdown.service.MediaPlayerService;

public class MusicActivity extends Activity implements OnClickListener, OnItemClickListener {

    private ListView mList;

    private List<Map<String, Object>> listItems;

    private HelpAdapter mAdapter;

    private String mCurMusicPath;
    private String mCurMusicName;

    private int firstPos;

    private static int mMusicPosition = 0;

    private Button ok_btn;

    private Button cancle_btn;

    private int counter;

    private Cursor cursor;

    private String[] musicName;

    private String[] musicPath;

    public static final String ALERT_MUSIC_PATH_KEY = "alert.music.path";

    public static final String music_position = "mMusicPosition";

    private TextView mNoMusicTv;
    ReflectionTool reflectionTool=new ReflectionTool();
    /*PR 605487- Neo Skunkworks - Paul Xu added - 001 Begin*/
    private static final int ALARM_STREAM_TYPE_BIT =
            1 << AudioManager.STREAM_ALARM;
    /*PR 605487- Neo Skunkworks - Paul Xu added - 001 End*/

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // PR:486634 add xibin --Can't adjust alarm volume when press volume
        // up/down in select alarm ringtone list box.
        setVolumeControlStream(AudioManager.STREAM_ALARM);
        setContentView(R.layout.alarm_music_ringtone);
		// PR 594450 - Neo Skunkworks - Soar Gao - 001 begin   
        //initData();
        //initView();
		// PR 594450 - Neo Skunkworks - Soar Gao - 001 end   
    }

    private void initData() {
        SharedPreferences sharedPre1 = getSharedPreferences(SetAlarm.RINGTONE_OF_PREALARM,
                MODE_PRIVATE);
        mCurMusicName = sharedPre1.getString(SetAlarm.CURRENT_RIONGTONE, "");
        getCurrentMusicPath();//PR:502487 add By XIBIN
// PR 601710 - Neo Skunkworks - Soar Gao - 001 begin 
		if(GotoRingtoneActivity.musicID==-1){
        	GotoRingtoneActivity.musicPath=mCurMusicPath;
        	GotoRingtoneActivity.musicName=mCurMusicName;
        }
// PR 601710 - Neo Skunkworks - Soar Gao - 001 end
    }

    private void initView() {
        getMusicInfo(this);
        //add PR433837 xibin start -- Can not set ringtone successful after concurrency
//        if (mCurMusicPath == null) {
//            mCurMusicPath = getCurrentMusicPath();
//        }
        // add PR433837 xibin end
        SharedPreferences sharedPre = this.getSharedPreferences(RingtoneList.TIMER_RPREFERENCES, 0);
        mMusicPosition = sharedPre.getInt(music_position, 0);

        mNoMusicTv = (TextView) findViewById(R.id.no_music);
        mNoMusicTv.setText(R.string.no_music);
        //add PR444369 XIBIN start--In the ringtone screen,it will popup force close after monut and unmount.
        mList = (ListView) findViewById(R.id.music_ringtone_list_view);
        if (musicPath==null||counter == 0) {//add XIBIN PR438925 //PR578695-Zonghua-Jin-001-begin
            mNoMusicTv.setVisibility(View.VISIBLE);
            mList.setVisibility(View.GONE);
        } else {
            // adapter for list
            mNoMusicTv.setVisibility(View.GONE);
            listItems = getListItems();
            mAdapter = new HelpAdapter(this, listItems); // create adapter
            mList.setVisibility(View.VISIBLE);
            mList.setOnItemClickListener(this);
            mList.setAdapter(mAdapter);
// PR 587415 - Neo Skunkworks - Soar Gao - 001 begin
           if(GotoRingtoneActivity.musicActivity==1&&GotoRingtoneActivity.musicID!=-1){
            mList.setSelection(GotoRingtoneActivity.musicID);
           }else{
        	   mList.setSelection(mMusicPosition);
           }
// PR 587415 - Neo Skunkworks - Soar Gao - 001 end
        }
        //add PR444369 XIBIN end
        ok_btn = (Button) findViewById(R.id.ok_button);
        ok_btn.setOnClickListener(this);
        cancle_btn = (Button) findViewById(R.id.cancle_button);
        cancle_btn.setOnClickListener(this);
    }

    private void getMusicInfo(Context context) {
        cursor = context.getContentResolver().query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                null,
                null, null, MediaStore.Audio.Media.DEFAULT_SORT_ORDER);
        //add XIBIN PR438925 begin--In the ringtone screen,it will popup force close after monut and unmount.
        if(cursor==null)
            return;
        //add XIBIN PR438925 end
        cursor.moveToFirst();
        counter = cursor.getCount();
        int count = counter;//PR578695-Zonghua-Jin-001-begin
        musicName = new String[counter];
        musicPath = new String[counter];
        //PR578695-Zonghua-Jin-001-begin
        for (int j = 0, i = 0; j < count; j++) {
            String strMusicPath = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DATA));
            /*PR 661666 - Neo Skunkworks - Paul Xu modified - 001 Begin*/
            try {
                if (reflectionTool.isMTKDrmEnable()
                       && reflectionTool.isDrm(strMusicPath)) {
                    if (isValidDrmRingtone(strMusicPath)) {
                        musicPath[i] = cursor.getString(cursor
                               .getColumnIndex(MediaStore.Audio.Media.DATA));
                        musicName[i] = cursor
                               .getString(cursor.getColumnIndex(MediaStore.Audio.Media.DISPLAY_NAME));
                        i++;
                    } else {
                        counter--;
                    }
                } else {
                    musicPath[i] = cursor.getString(cursor
                           .getColumnIndex(MediaStore.Audio.Media.DATA));
                    // add by caorongxing for pr 425324 begin
                    // musicName[j] =
                    // cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.TITLE));
                    musicName[i] = cursor
                         .getString(cursor.getColumnIndex(MediaStore.Audio.Media.DISPLAY_NAME));
                    i++;
                }
             } catch (NoSuchMethodError ex) {
                musicPath[i] = cursor.getString(cursor
                        .getColumnIndex(MediaStore.Audio.Media.DATA));
                musicName[i] = cursor.getString(cursor
                     .getColumnIndex(MediaStore.Audio.Media.DISPLAY_NAME));
                i++;
            }
            /*PR 661666 - Neo Skunkworks - Paul Xu modified - 001 End*/
            //PR578695-Zonghua-Jin-001-end
            // add by caorongxing for pr 425324 end
            cursor.moveToNext();
        }
        cursor.close();
    }

    private List<Map<String, Object>> getListItems() {
        List<Map<String, Object>> listItems = new ArrayList<Map<String, Object>>();
        /*FR 567690- Neo Skunkworks - Paul Xu added - 001 Begin*/
        if(musicName == null){
        	return listItems;
        }
        /*FR 567690- Neo Skunkworks - Paul Xu added - 001 End*/
        String filename = null;//
        for (int i = 0; i < counter; i++) {//PR578695-Zonghua-Jin-001-begin
            Map<String, Object> map = new HashMap<String, Object>();
            //PR730529-haiying.he start
            filename = musicName[i].replace("_", " ");
            map.put("activity", filename);
            //PR730529-haiying.he end
// PR 587415 - Neo Skunkworks - Soar Gao - 001 begin
/*
            if (musicPath[i].equals(mCurMusicPath)) { // modify by Yanjingming for pr478002
                mMusicPosition = i;
                map.put("checked", true);
            } else {
                map.put("checked", false);
            }
*/
 if (musicPath[i].equals(mCurMusicPath)&&GotoRingtoneActivity.musicID==-1) { // modify by Yanjingming for pr478002
                    	 
            			 mMusicPosition = i;
                         map.put("checked", true);
                     } else if(GotoRingtoneActivity.musicID!=-1&&i==GotoRingtoneActivity.musicID){
                    	 if(GotoRingtoneActivity.musicActivity==1){
                    		 map.put("checked", true); 
                    	 }else{
                    		 map.put("checked", false); 
                    	 }
                     }else{
                         map.put("checked", false);
                     } 
// PR 587415 - Neo Skunkworks - Soar Gao - 001 end
            listItems.add(map);
        }
        return listItems;
    }

    public boolean isCurrentMusicExist(Context context, String curMusicName) {
        getMusicInfo(context);
        //add PR437630  XIBIN begin--Snooze the alarm, label displays an error after delete the label.
        if(musicName==null)
            return false;
        //add PR437630 XIBIN end
        for (int i = 0; i < counter; i++) {//PR578695-Zonghua-Jin-001-begin
            if (curMusicName.equals(musicName[i])) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        // TODO Auto-generated method stub
        super.onConfigurationChanged(newConfig);

        setVolumeControlStream(AudioManager.STREAM_ALARM);
        setContentView(R.layout.alarm_music_ringtone);

        initData();
        initView();
    }

	@Override
    protected void onResume() {
        super.onResume();
     // PR 587415 - Neo Skunkworks - Soar Gao - 001 begin
        //Somehow the screen to switch 
        setVolumeControlStream(AudioManager.STREAM_ALARM);
     // PR 587415 - Neo Skunkworks - Soar Gao - 001 end
        initData();
        initView();
    }

    @Override
    protected void onPause() {
        super.onPause();
        SharedPreferences sharedPre = getSharedPreferences(SetAlarm.RINGTONE_OF_PREALARM,
                MODE_PRIVATE);
        sharedPre.edit().putString(SetAlarm.CURRENT_RIONGTONE, mCurMusicName).commit();
      //PR:502487 add By XIBIN start
        sharedPre = getSharedPreferences(RingtoneList.TIMER_RPREFERENCES, 0);
        sharedPre.edit().putString(RingtoneList.ALERT_RINGTONE_PATH_KEY, mCurMusicPath).commit();
        //PR:502487 add By XIBIN end
    }

    private String getCurrentMusicPath() {
        SharedPreferences sharedPre = this.getSharedPreferences(RingtoneList.TIMER_RPREFERENCES, 0);
        mCurMusicPath = sharedPre.getString(RingtoneList.ALERT_RINGTONE_PATH_KEY, "");
        return mCurMusicPath;
    }

    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        mMusicPosition = position;
        // PR 587415 - Neo Skunkworks - Soar Gao - 001 begin
        GotoRingtoneActivity.musicActivity=1;
        GotoRingtoneActivity.musicID=mMusicPosition;
        GotoRingtoneActivity.musicPath=musicPath[position];
        GotoRingtoneActivity.musicName=musicName[position];
        // PR 587415 - Neo Skunkworks - Soar Gao - 001 end
        mCurMusicPath = musicPath[position];
        mCurMusicName = musicName[position];
        firstPos = mList.getFirstVisiblePosition();
        mAdapter = new HelpAdapter(this, getListItems());
        mList.setAdapter(mAdapter);
        mList.setSelection(firstPos);

        stopService(new Intent(this, MediaPlayerService.class));
        Intent intent = new Intent(this, MediaPlayerService.class);

        intent.putExtra(MediaPlayerService.MEDIA_FILE_PATH_EXTRA, mCurMusicPath);

        startService(intent);
    }

    private void saveMusic() {
    	// PR 587415 - Neo Skunkworks - Soar Gao - 001 begin
    	if((GotoRingtoneActivity.musicID!=-1)&&(GotoRingtoneActivity.musicActivity==1)){
    		sendToService();
    	}
		// PR 587415 - Neo Skunkworks - Soar Gao - 001 end
        SharedPreferences sharedPre = getSharedPreferences(RingtoneList.TIMER_RPREFERENCES,
                0);
// PR 587415 - Neo Skunkworks - Soar Gao - 001 begin
/*
        if (!mCurMusicPath.equals("")) {
            sharedPre.edit().putString(RingtoneList.ALERT_RINGTONE_PATH_KEY, mCurMusicPath)
                    .putString(RingtoneList.ALERT_RINGTONE_NAME_KEY, mCurMusicName)
                    .putInt(music_position, mMusicPosition).commit();
        }
        Intent mIntent = new Intent(SetAlarm.ACTION_NAME);
        String ringtoneName = sharedPre.getString(RingtoneList.ALERT_RINGTONE_NAME_KEY,
                mCurMusicName);
        String ringtonePath = sharedPre.getString(RingtoneList.ALERT_RINGTONE_PATH_KEY,
                mCurMusicPath);
*/
if (!GotoRingtoneActivity.musicPath.equals("")) {
            sharedPre.edit().putString(RingtoneList.ALERT_RINGTONE_PATH_KEY, GotoRingtoneActivity.musicPath)
                    .putString(RingtoneList.ALERT_RINGTONE_NAME_KEY, GotoRingtoneActivity.musicName)
                    .putInt(music_position, GotoRingtoneActivity.musicID).commit();
        }
        Intent mIntent = new Intent(SetAlarm.ACTION_NAME);
        String ringtoneName = sharedPre.getString(RingtoneList.ALERT_RINGTONE_NAME_KEY,
        		GotoRingtoneActivity.musicName);
        String ringtonePath = sharedPre.getString(RingtoneList.ALERT_RINGTONE_PATH_KEY,
        		GotoRingtoneActivity.musicPath);
// PR 587415 - Neo Skunkworks - Soar Gao - 001 end
        mIntent.putExtra("ringtone", ringtoneName);
        mIntent.putExtra("ringtonePath", ringtonePath);
        sendBroadcast(mIntent);
        stopService(new Intent(this, MediaPlayerService.class));
    }

    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.ok_button:
                saveMusic();
                //add PR455342 XIBIN -- Change timetool package name and class name as deskclock
//                onBackPressed();
                finish();
            case R.id.cancle_button:
                stopService(new Intent(this, MediaPlayerService.class));
                //add PR455342 XIBIN -- Change timetool package name and class name as deskclock
//                onBackPressed();
                finish();
        }
    }

    //PR578695-Zonghua-Jin-001-begin
    private boolean isValidDrmRingtone(String filePath) {
        DrmManagerClient drmManager = ReflectionTool.getInstance(this);
        boolean hasCount = ReflectionTool.hasCountConstraint(drmManager,filePath);
        boolean isValid = drmManager.checkRightsStatus(filePath,
                android.drm.DrmStore.Action.PLAY) == android.drm.DrmStore.RightsStatus.RIGHTS_VALID;
        if (hasCount || !isValid) {
            return false;
        }
        return true;
    }
    //PR578695-Zonghua-Jin-001-end
    
  // PR 587415 - Neo Skunkworks - Soar Gao - 001 begin
    public void sendToService(){
    	 stopService(new Intent(this, MediaPlayerService.class));
        Intent intent = new Intent(this, MediaPlayerService.class);
        intent.putExtra(MediaPlayerService.MEDIA_FILE_PATH_EXTRA,  GotoRingtoneActivity.musicPath);
        startService(intent);
   }
    @Override  
	 public boolean onKeyDown(int keyCode, KeyEvent event) {
         /*PR 605487- Neo Skunkworks - Paul Xu modified - 001 Begin*/
         /*  
	     if (keyCode == KeyEvent.KEYCODE_BACK) {  
	    	 stopService(new Intent(this, MediaPlayerService.class));
	     }
	     return super.onKeyUp(keyCode, event); 
         */
         switch (keyCode) {
           case KeyEvent.KEYCODE_BACK:
                stopService(new Intent(this, MediaPlayerService.class));
                break;
           case KeyEvent.KEYCODE_VOLUME_DOWN:
           case KeyEvent.KEYCODE_VOLUME_UP:
                if (adjustAlarmStreamVolume()) {
                    return true;
                }
                break;
         }
    	
         return super.onKeyDown(keyCode, event);
         /*PR 605487- Neo Skunkworks - Paul Xu modified - 001 End*/ 
	 } 
     
     /*PR 605487- Neo Skunkworks - Paul Xu added - 001 Begin*/ 
     private boolean adjustAlarmStreamVolume(){
    	AudioManager audioManager = (AudioManager)getSystemService(Context.AUDIO_SERVICE);	
    	int volume = audioManager.getStreamVolume(AudioManager.STREAM_ALARM);
    	final int silentModeStreams = Settings.System.getInt(getContentResolver(),
	                        Settings.System.MODE_RINGER_STREAMS_AFFECTED, 0);
    	boolean result = (silentModeStreams & ALARM_STREAM_TYPE_BIT) == 0;
    	if (!result && volume == 0) {
    		audioManager.adjustStreamVolume(AudioManager.STREAM_ALARM,
    				AudioManager.ADJUST_SAME, AudioManager.FLAG_SHOW_UI);
    		return true;
    	}
			
    	return false;
    } 
    /*PR 605487- Neo Skunkworks - Paul Xu added - 001 End*/ 
// PR 587415 - Neo Skunkworks - Soar Gao - 001 end
}
