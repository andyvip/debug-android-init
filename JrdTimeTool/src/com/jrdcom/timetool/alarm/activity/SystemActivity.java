
package com.jrdcom.timetool.alarm.activity;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.media.AudioManager;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ListView;

import com.android.deskclock.R;
import com.jrdcom.timetool.alarm.provider.HelpAdapter;
import com.jrdcom.timetool.alarm.provider.RingtoneList;
import com.jrdcom.timetool.countdown.service.MediaPlayerService;

public class SystemActivity extends Activity implements OnClickListener, OnItemClickListener {

    private ListView mList;
    private List<Map<String, Object>> listItems;
    private HelpAdapter mAdapter;

    private String[] mRingtones;
    private String mPreRingtone;
    private int firstPos;
    private static int mSystemPosition = 0;
    private String mFilePath;

    private Button ok_btn;
    private Button cancle_btn;
    /*PR 605487- Neo Skunkworks - Paul Xu added - 001 Begin*/
    private static final int ALARM_STREAM_TYPE_BIT =
            1 << AudioManager.STREAM_ALARM;
    /*PR 605487- Neo Skunkworks - Paul Xu added - 001 End*/

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // PR:486634 add xibin --Can't adjust alarm volume when press volume
        // up/down in select alarm ringtone list box.
        setVolumeControlStream(AudioManager.STREAM_ALARM);
        setContentView(R.layout.alarm_system_ringtone);
// PR 594450 - Neo Skunkworks - Soar Gao - 001 begin 
        /*getRingtoneInfo(this);
        initData();
        initView();
*/
// PR 594450 - Neo Skunkworks - Soar Gao - 001 end 
    }

    private void initData() {
        SharedPreferences sharedPre1 = getSharedPreferences(SetAlarm.RINGTONE_OF_PREALARM,
                MODE_PRIVATE);
        mPreRingtone = sharedPre1.getString(SetAlarm.CURRENT_RIONGTONE, "");
        getRingtoneFilePath();
// PR 601710 - Neo Skunkworks - Soar Gao - 001 begin  
if(GotoRingtoneActivity.musicID==-1){
       GotoRingtoneActivity.musicPath=mFilePath;
        GotoRingtoneActivity.musicName=mPreRingtone;
        }
// PR 601710 - Neo Skunkworks - Soar Gao - 001 end  
    }

    private void initView() {
        SharedPreferences sharedPre = getSharedPreferences(RingtoneList.TIMER_RPREFERENCES, 0);
        mSystemPosition = sharedPre.getInt("mSystemPosition", 0);
      //add PR433837 xibin start -- Can not set ringtone successful after concurrency
//        if(mFilePath == null) {
//        mFilePath = getRingtoneFilePath();
//        }
      //add PR433837 xibin end
        mList = (ListView) findViewById(R.id.system_ringtone_list_view);
        listItems = getListItems();
        mAdapter = new HelpAdapter(this, listItems); // create adapter
        mList.setOnItemClickListener(this);
        mList.setAdapter(mAdapter);
// PR 587415 - Neo Skunkworks - Soar Gao - 001 begin
       if(GotoRingtoneActivity.musicActivity==0&&GotoRingtoneActivity.musicID!=-1){
    	   mList.setSelection(GotoRingtoneActivity.musicID);
       }else{
    	   mList.setSelection(mSystemPosition);
       }

// PR 587415 - Neo Skunkworks - Soar Gao - 001 end
        ok_btn = (Button) findViewById(R.id.ok_button);
        ok_btn.setOnClickListener(this);
        cancle_btn = (Button) findViewById(R.id.cancle_button);
        cancle_btn.setOnClickListener(this);
    }

    private void getRingtoneInfo(Context context) {
        mRingtones = RingtoneList.getRingtoneList(context);
    }

    public boolean isCurrentRingtoneExist(Context context, String curRingtoneName) {
        getRingtoneInfo(context);
        for (int i = 0; i < mRingtones.length; i++) {
            if (curRingtoneName.equals(mRingtones[i])) {
                return true;
            }
        }
        return false;
    }

    private String getRingtoneFilePath() {
        SharedPreferences sharedPre = this.getSharedPreferences(RingtoneList.TIMER_RPREFERENCES, 0);
        mFilePath = sharedPre.getString(RingtoneList.ALERT_RINGTONE_PATH_KEY,
                SetAlarm.mDefaultRingtonePath);     
        /*--PR 667590 	- Neo Skunkworks - Paul Xu added - 001 Begin--*/
    	if(HandleSetAlarm.mSilentRingTone){
    		saveDefaultRing();
    		return mFilePath;
    	}
        /*--PR 667590 	- Neo Skunkworks - Paul Xu added - 001 End--*/
     // PR 587415 - Neo Skunkworks - Soar Gao - 001 begin
        if(GotoRingtoneActivity.musicID==-1){
        if(SetAlarm.mDefaultRingtonePath != null && SetAlarm.mDefaultRingtonePath.equals(mFilePath)){
        	//save default
        	//GotoRingtoneActivity.musicID=1;// PR -601710 - Neo Skunworks - Soar Gao , delete -001
        	GotoRingtoneActivity.musicPath=mFilePath;
        	if(!("".equals(mFilePath))){
        		GotoRingtoneActivity.musicName=mFilePath.substring(mFilePath.lastIndexOf('/')+1, mFilePath.lastIndexOf('.'));
        	}
        	saveDefaultRing();
        }
        }
        // PR 587415 - Neo Skunkworks - Soar Gao - 001 end
        return mFilePath;
    }

    private List<Map<String, Object>> getListItems() {
        List<Map<String, Object>> listItems = new ArrayList<Map<String, Object>>();
        for (int i = 0; i < mRingtones.length; i++) {
            Map<String, Object> map = new HashMap<String, Object>();
            map.put("activity", mRingtones[i].replace("_", " "));
// PR 587415 - Neo Skunkworks - Soar Gao - 001 begin
/*
            if (mRingtones[i].equals(mPreRingtone)) {
                mSystemPosition = i;
                map.put("checked", true);
            } else {
                map.put("checked", false);
            }
*/
 if (mRingtones[i].equals(mPreRingtone)&&GotoRingtoneActivity.musicID==-1) { // modify by Yanjingming for pr478002
                    	 
            			 mSystemPosition = i;
                         map.put("checked", true);
                     } else if(GotoRingtoneActivity.musicID!=-1&&i==GotoRingtoneActivity.musicID){
                    	 mSystemPosition=GotoRingtoneActivity.musicID;
                    	 if(GotoRingtoneActivity.musicActivity==0){
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

    @Override
	public void onConfigurationChanged(Configuration newConfig) {
		// TODO Auto-generated method stub
		super.onConfigurationChanged(newConfig);
		
		setVolumeControlStream(AudioManager.STREAM_ALARM);
        setContentView(R.layout.alarm_system_ringtone);

        getRingtoneInfo(this);
        initData();
        initView();
	}

	@Override
    protected void onResume() {
        super.onResume();
     // PR 587415 - Neo Skunkworks - Soar Gao - 001 begin
        //Somehow the screen to switch 
        setVolumeControlStream(AudioManager.STREAM_ALARM);
        getRingtoneInfo(this);
     // PR 587415 - Neo Skunkworks - Soar Gao - 001 end
        initData();
        initView();
    }

    @Override
    protected void onPause() {
        super.onPause();
        SharedPreferences sharedPre1 = getSharedPreferences(SetAlarm.RINGTONE_OF_PREALARM,
                MODE_PRIVATE);
        sharedPre1.edit().putString(SetAlarm.CURRENT_RIONGTONE, mPreRingtone).commit();
        //PR:502487 add By XIBIN start
        sharedPre1 = getSharedPreferences(RingtoneList.TIMER_RPREFERENCES, 0);
        sharedPre1.edit().putString(RingtoneList.ALERT_RINGTONE_PATH_KEY, mFilePath).commit();
        //PR:502487 add By XIBIN end
    }

    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        mSystemPosition = position;
        // PR 587415 - Neo Skunkworks - Soar Gao - 001 begin
        GotoRingtoneActivity.musicActivity=0;
        GotoRingtoneActivity.musicID=mSystemPosition;
        
        // PR 587415 - Neo Skunkworks - Soar Gao - 001 begin
        mPreRingtone = mRingtones[position];
        firstPos = mList.getFirstVisiblePosition();
        mAdapter = new HelpAdapter(this, getListItems());
        mList.setAdapter(mAdapter);
        mList.setSelection(firstPos);

        // start play
        stopService(new Intent(this, MediaPlayerService.class));
        // when choose silent
        if (position == 0) {
// PR 594821 - Neo Skunkworks - Soar Gao - 001 begin 
        	GotoRingtoneActivity.musicPath="silent";
        	GotoRingtoneActivity.musicName=mPreRingtone;
// PR 594821 - Neo Skunkworks - Soar Gao - 001 end 
            mFilePath = "";
            mFilePath = "silent";
            return;
        }
        if (position < RingtoneList.mediaFiles.length + 1 && position > 0) {// =ringtones
                                                                            // length
            File file = RingtoneList.mediaFiles[position - 1];
            mFilePath = file.getAbsolutePath();
            // PR 587415 - Neo Skunkworks - Soar Gao - 001 begin
            GotoRingtoneActivity.musicPath=mFilePath;
            GotoRingtoneActivity.musicName=mPreRingtone;
            // PR 587415 - Neo Skunkworks - Soar Gao - 001 end
            Intent intent = new Intent(this, MediaPlayerService.class);

            intent.putExtra(MediaPlayerService.MEDIA_FILE_PATH_EXTRA, mFilePath);

            startService(intent);
        }
    }

    private void saveRingtone() {
    	// PR 587415 - Neo Skunkworks - Soar Gao - 001 begin
    	if(((GotoRingtoneActivity.musicID!=-1)&&(GotoRingtoneActivity.musicActivity==0))){
    		sendToService();
    	}
    	// PR 587415 - Neo Skunkworks - Soar Gao - 001 end
        SharedPreferences sharedPre = getSharedPreferences(
                RingtoneList.TIMER_RPREFERENCES,
                0);
// PR 587415 - Neo Skunkworks - Soar Gao - 001 begin
/*
        if (!mFilePath.equals("")) {
            sharedPre
                    .edit()
                    .putString(RingtoneList.ALERT_RINGTONE_NAME_KEY, mPreRingtone)
                    .putString(RingtoneList.ALERT_RINGTONE_PATH_KEY, mFilePath)
                    .putInt("mSystemPosition", mSystemPosition)
                    .commit();
        }
        Intent mIntent = new Intent(SetAlarm.ACTION_NAME);
        String ringtoneName = sharedPre.getString(RingtoneList.ALERT_RINGTONE_NAME_KEY,
                mPreRingtone);
        String ringtonePath = sharedPre.getString(RingtoneList.ALERT_RINGTONE_PATH_KEY,
                mFilePath);
*/
 if (!GotoRingtoneActivity.musicPath.equals("")) {
            sharedPre
                    .edit()
                    .putString(RingtoneList.ALERT_RINGTONE_NAME_KEY, GotoRingtoneActivity.musicName)
                    .putString(RingtoneList.ALERT_RINGTONE_PATH_KEY, GotoRingtoneActivity.musicPath)
                    .putInt("mSystemPosition", GotoRingtoneActivity.musicID)
                    .commit();
        }
        Intent mIntent = new Intent(SetAlarm.ACTION_NAME);
        String ringtoneName = sharedPre.getString(RingtoneList.ALERT_RINGTONE_NAME_KEY,
        		GotoRingtoneActivity.musicName);
        String ringtonePath = sharedPre.getString(RingtoneList.ALERT_RINGTONE_PATH_KEY,
        		GotoRingtoneActivity.musicPath);
// PR 587415 - Neo Skunkworks - Soar Gao - 001 end
        mIntent.putExtra("ringtone", ringtoneName);
        mIntent.putExtra("ringtonePath", ringtonePath);
        mIntent.putExtra("ringtonePosition", mSystemPosition);
        sendBroadcast(mIntent);
        stopService(new Intent(this, MediaPlayerService.class));
    }

    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.ok_button:
                saveRingtone();
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
// PR 587415 - Neo Skunkworks - Soar Gao - 001 begin
    public void sendToService(){
    stopService(new Intent(this, MediaPlayerService.class));
   Intent intent = new Intent(this, MediaPlayerService.class);

   intent.putExtra(MediaPlayerService.MEDIA_FILE_PATH_EXTRA, GotoRingtoneActivity.musicPath);

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
         switch(keyCode){
    		case KeyEvent.KEYCODE_BACK:
    			stopService(new Intent(this, MediaPlayerService.class));
    			break;
    		case KeyEvent.KEYCODE_VOLUME_DOWN:
    		case KeyEvent.KEYCODE_VOLUME_UP:
    			if(adjustAlarmStreamVolume()){
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
    //save default if music deleted
    private void saveDefaultRing(){
    	 SharedPreferences sharedPre = getSharedPreferences(
                 RingtoneList.TIMER_RPREFERENCES,
                 0);
    	 if (!GotoRingtoneActivity.musicPath.equals("")) {
             sharedPre
                     .edit()
                     .putString(RingtoneList.ALERT_RINGTONE_NAME_KEY, GotoRingtoneActivity.musicName)
                     .putString(RingtoneList.ALERT_RINGTONE_PATH_KEY, GotoRingtoneActivity.musicPath)
                     .putInt("mSystemPosition", GotoRingtoneActivity.musicID)
                     .commit();
         }
    	// PR 590654 - Neo Skunkworks - Soar Gao - 001 begin
//         Intent mIntent = new Intent(SetAlarm.ACTION_NAME);
//         String ringtoneName = sharedPre.getString(RingtoneList.ALERT_RINGTONE_NAME_KEY,
//         		GotoRingtoneActivity.musicName);
//         String ringtonePath = sharedPre.getString(RingtoneList.ALERT_RINGTONE_PATH_KEY,
//         		GotoRingtoneActivity.musicPath);
//         mIntent.putExtra("ringtone", ringtoneName);
//         mIntent.putExtra("ringtonePath", ringtonePath);
//         mIntent.putExtra("ringtonePosition", mSystemPosition);
//         sendBroadcast(mIntent);
    	// PR 590654 - Neo Skunkworks - Soar Gao - 001 end
    }
// PR 587415 - Neo Skunkworks - Soar Gao - 001 begin
}
