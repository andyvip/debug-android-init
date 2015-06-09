package com.jrdcom.timetool.alarm.service;

import com.jrdcom.timetool.alarm.activity.SettingsActivity;
import com.jrdcom.timetool.alarm.provider.Alarms;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.util.Log;
import com.android.deskclock.R;

// add by liang.zhang for PR 849401 at 2014-12-17 begin
import com.jrdcom.timetool.alarm.provider.Alarm;
//add by liang.zhang for PR 849401 at 2014-12-17 end

/**
 * This broadcast receiver intents to receive power off alarm alert broadcast
 * sent by AlarmManagerService.
 */
public class PowerOffAlarmService extends Service {
    private static final String TAG = "PowerOffAlarmService";
    @Override
    public IBinder onBind(Intent intent) {
        // TODO Auto-generated method stub
        return null;
    }
    public int onStartCommand(Intent intent, int flags, int startId) {
        //add PR455503 xibin --The pointer's location is wrong
        if(intent != null)
        Log.v("jrdtimetool","start service , intent = " + intent.getAction());
        if (intent == null) {
            stopSelf();
            return START_NOT_STICKY;
        }
        // PR: 480471 update by XIBIN
        snooze(PowerOffAlarmService.this , intent);
        return START_STICKY;
    }

    /**
     * save next alarm time and show snooze time by toast
     */
    private void snooze(Context context,Intent intent) {
        final String snooze = PreferenceManager.getDefaultSharedPreferences(
                context).getString(SettingsActivity.KEY_ALARM_SNOOZE, DEFAULT_SNOOZE);
        Log.v("jrdtimetool","duration of alarm snooze: " + snooze);
        int snoozeMinutes = Integer.parseInt(snooze);
        final long snoozeTime = System.currentTimeMillis()
                + ((long) 1000 * 60 * snoozeMinutes);
        // PR: 480471 update by XIBIN start
        int snoozeId = intent.getIntExtra("SNOOZE_ALARM_ID", -1);
        int id = (snoozeId == -1) ? AlarmKlaxon.mCurrentPlayingAlarmId :snoozeId;
        if(id != -1){
        	// add by liang.zhang for PR 849401 at 2014-12-17 begin
        	Alarm mAlarm = Alarms.getAlarm(getContentResolver(),id);
        	if (mAlarm!=null && !mAlarm.daysOfWeek.isRepeatSet()) {
        		mAlarm.enabled = true;
        		Alarms.enableAlarm(this, mAlarm.id, true);
        	}
        	// add by liang.zhang for PR 849401 at 2014-12-17 end
            Alarms.saveSnoozeAlert(context, id, snoozeTime);
        }
        // PR: 480471 update by XIBIN end
        String displayTime = context.getResources().getString(R.string.alarm_alert_snooze_set,
                snoozeMinutes);
        Log.v("jrdtimetool","display time: " + displayTime + " snoozeTime: " + snoozeTime);
        // modify by Yan Jingming for pr560209 begin
        boolean isSecondAlarm = intent.getBooleanExtra("IS_SECOND_ALARM",false);
        if(!isSecondAlarm){
            stopService(new Intent(Alarms.ALARM_ALERT_ACTION));// add by Yan Jingming for pr547942
        }
        // modify by Yan Jingming for pr560209 end
        /// M: device will shutdown when autosilent_time finished
        // PR: 480471 update by XIBIN start
        boolean isShutDown= intent.getBooleanExtra("IS_SHUT_DOWN", true);
        Log.v("jrdtimetool","isShutDown: " + isShutDown);
        if (isShutDown) {
            context.sendBroadcast(new Intent("stop_ringtone"));
            shutDown(context);// @ add by Yanjingming for pr453514
        }
        // PR: 480471 update by XIBIN end
    }

    // shut down the device
    protected static void shutDown(Context mContext) {
        // send normal shutdown broadcast
        Intent shutdownIntent = new Intent(NORMAL_SHUTDOWN_ACTION);
        mContext.sendBroadcast(shutdownIntent);

        // shutdown the device
        Intent intent = new Intent(ALARM_REQUEST_SHUTDOWN_ACTION);
        //intent.putExtra(Intent.EXTRA_KEY_CONFIRM, false);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        mContext.startActivity(intent);
    }

    private static final String DEFAULT_SNOOZE = "5";
    /*PR 655226 - Neo Skunkworks - Paul Xu modified - 001 Begin*/
    /*
    static final String SNOOZE = "android.intent.action.SNOOZE";
    */
    static final String SNOOZE = "com.android.deskclock.SNOOZE_ALARM";
    /*PR 655226 - Neo Skunkworks - Paul Xu modified - 001 End*/
    private static final String NORMAL_SHUTDOWN_ACTION = "android.intent.action.normal.shutdown";
    private static final String NORMAL_BOOT_ACTION = "android.intent.action.normal.boot";
    private static final String ALARM_REQUEST_SHUTDOWN_ACTION = "android.intent.action.ACTION_ALARM_REQUEST_SHUTDOWN";
}
