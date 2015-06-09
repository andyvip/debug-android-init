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

package com.jrdcom.timetool.alarm.receiver;

import java.util.Calendar;

import android.app.KeyguardManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Parcel;
import android.os.PowerManager.WakeLock;
import android.os.Vibrator;

import com.android.deskclock.R;
import com.jrdcom.timetool.MyLog;
import com.jrdcom.timetool.alarm.AlarmAlertWakeLock;
import com.jrdcom.timetool.alarm.AsyncHandler;
import com.jrdcom.timetool.alarm.activity.AlarmAlert;
import com.jrdcom.timetool.alarm.activity.AlarmAlertFullScreen;
import com.jrdcom.timetool.alarm.activity.SetAlarm;
import com.jrdcom.timetool.alarm.provider.Alarm;
import com.jrdcom.timetool.alarm.provider.Alarms;

import android.os.RemoteException;
import android.telephony.TelephonyManager;
import android.text.format.DateFormat;
import android.os.ServiceManager;
/**
 * Glue class: connects AlarmAlert IntentReceiver to AlarmAlert
 * activity.  Passes through Alarm ID.
 */
public class AlarmReceiver extends BroadcastReceiver {

    /** If the alarm is older than STALE_WINDOW, ignore.  It
        is probably the result of a time or timezone change */
    private final static int STALE_WINDOW = 30 * 60 * 1000;
    private static final int VIBRATE_LENGTH = 1000;
    
    // add by liang.zhang for PR 927097 at 2015-03-11 begin
    private static boolean mIsRingoutInCall = false;
    // add by liang.zhang for PR 927097 at 2015-03-11 end
    
    @Override
    public void onReceive(final Context context, final Intent intent) {
    	
        final PendingResult result = goAsync();
        final WakeLock wl = AlarmAlertWakeLock.createPartialWakeLock(context);
        wl.acquire();
        AsyncHandler.post(new Runnable() {
            @Override public void run() {
                handleIntent(context, intent);
                result.finish();
                wl.release();
            }
        });
    }

    private void handleIntent(Context context, Intent intent) {
        if (Alarms.ALARM_KILLED.equals(intent.getAction())) {
            // The alarm has been killed, update the notification
            updateNotification(context, (Alarm)
                    intent.getParcelableExtra(Alarms.ALARM_INTENT_EXTRA),
                    intent.getIntExtra(Alarms.ALARM_KILLED_TIMEOUT, -1));
            return;
        } else if (Alarms.CANCEL_SNOOZE.equals(intent.getAction())) {
            Alarm alarm = null;
            if (intent.hasExtra(Alarms.ALARM_INTENT_EXTRA)) {
                // Get the alarm out of the Intent
                alarm = intent.getParcelableExtra(Alarms.ALARM_INTENT_EXTRA);
            }

            if (alarm != null) {
                // PR-717855-mingwei.han-add begin
                if (!alarm.daysOfWeek.isRepeatSet()) {
                    alarm.enabled = false;
                    Alarms.enableAlarm(context, alarm.id, false);
                }
                // PR-717855-mingwei.han-add end
                Alarms.disableSnoozeAlert(context, alarm.id);
                Alarms.setNextAlert(context);
            } else {
                // Don't know what snoozed alarm to cancel, so cancel them all.  This
                // shouldn't happen
                Alarms.saveSnoozeAlert(context, Alarms.INVALID_ALARM_ID, -1);
            }
            return;
        } else if (!Alarms.ALARM_ALERT_ACTION.equals(intent.getAction())) {
            // Unknown intent, bail.
            /*--PR 665930 - Neo Skunkworks - Paul Xu added - 001 Begin--*/
            /*Listen phone state when phone coming*/        	
            if(Alarms.ALARM_PHONE_STATE_ACTION.equals(intent.getAction()) && !Alarms.bootFromPoweroffAlarm()){       		
//                try {
//                   ITelephony mTelephonyService = ITelephony.Stub.asInterface(ServiceManager
//                                .getService(Context.TELEPHONY_SERVICE));
                   TelephonyManager mTelephonyService=(TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
                   if (mTelephonyService != null) {
                       int mCurrentCallState = mTelephonyService.getCallState();
                       if (mCurrentCallState != TelephonyManager.CALL_STATE_IDLE){
                          return;
                        }// PR746297-mingwei.han-add begin
                        else {
                            try {
                                Thread.sleep(1000);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                        // PR746297-mingwei.han-add end
                   }
//                }catch (RemoteException ex) {
//                    android.util.Log.e("AlarmReceiver : ",
//                          "---Catch exception when getPreciseCallState: ex = " + ex.getMessage());
//                }
                    	
            }else{
                return;
            }
            /*--PR 665930 - Neo Skunkworks - Paul Xu added - 001 End--*/
        }

        Alarm alarm = null;
        // Grab the alarm from the intent. Since the remote AlarmManagerService
        // fills in the Intent to add some extra data, it must unparcel the
        // Alarm object. It throws a ClassNotFoundException when unparcelling.
        // To avoid this, do the marshalling ourselves.
        final byte[] data = intent.getByteArrayExtra(Alarms.ALARM_RAW_DATA);
        if (data != null) {
            Parcel in = Parcel.obtain();
            in.unmarshall(data, 0, data.length);
            in.setDataPosition(0);
            alarm = Alarm.CREATOR.createFromParcel(in);
        }
        boolean PlayingAlarmState = true;// PR746297-mingwei.han-add
        if (alarm == null) {
            /*--PR 665930 - Neo Skunkworks - Paul Xu added - 001 Begin--*/
            /*If phone is CALL_STATE_IDLE state, get the playing alarm id*/        
            int alarmId = Alarms.getPlayingAlarmId(context);
            if(alarmId > 0 && AlarmAlertFullScreen.getDissmissOrSnoozeState() == false){
                alarm = Alarms.getAlarm(context.getContentResolver(), alarmId);
                PlayingAlarmState = false;// PR746297-mingwei.han-add
            }else{
            	/* PR 683052 - Neo Skunkworks - Paul Xu added - 001 Begin*/
            	AlarmAlertFullScreen.setDissmissOrSnoozeState(false);
            	/* PR 683052 - Neo Skunkworks - Paul Xu added - 001 End*/
            	// Make sure we set the next alert if needed.
            	Alarms.setNextAlert(context);
            	return;
            }
            /*--PR 665930 - Neo Skunkworks - Paul Xu added - 001 End--*/
        }
        // PR: 480471 update by XIBIN start
        if (Alarms.bootFromPoweroffAlarm()
                && Alarms.ALARM_ALERT_ACTION.equals(intent.getAction())) {
            /*PR 655226 - Neo Skunkworks - Paul Xu modified - 001 Begin*/
            /*
            Intent intentSnooze = new Intent("android.intent.action.SNOOZE");
            */
            Intent intentSnooze = new Intent(Alarms.POWER_OFF_ALARM_SERVICE_ACTION);
            /*PR 655226 - Neo Skunkworks - Paul Xu modified - 001 End*/
            intentSnooze.putExtra("IS_SHUT_DOWN",false);
            intentSnooze.putExtra("IS_SECOND_ALARM",true);// add by Yan Jingming for pr560209
            intentSnooze.putExtra("SNOOZE_ALARM_ID", alarm.id);
            context.startService(intentSnooze);
            return;
        }
        // PR: 480471 update by XIBIN end
        // Disable the snooze alert if this alarm is the snooze.
        Alarms.disableSnoozeAlert(context, alarm.id);
        // add by haifeng.tang 773517 2014.9.5 begin
        if(alarm != null){
            Alarms.savePlayingAlarmID(context, alarm.id);
        }
     // add by haifeng.tang PR 773517 2014.9.5 end

        // Disable this alarm if it does not repeat.
        if (!alarm.daysOfWeek.isRepeatSet()) {
            // add PR466531 xibin --Vibration and Repeat items are still changed
            // although the edit behavior is cancelled
            alarm.enabled =false;
            Alarms.enableAlarm(context, alarm.id, false);
            
        } else {
            // Enable the next alert if there is one. The above call to
            // enableAlarm will call setNextAlert so avoid calling it twice.
            Alarms.setNextAlert(context);
        }

        // Intentionally verbose: always log the alarm time to provide useful
        // information in bug reports.
        long now = System.currentTimeMillis();

        // Always verbose to track down time change problems.
        if (now > alarm.time + STALE_WINDOW
                && PlayingAlarmState) {// PR 732852-mingwei.han-add
            return;
        }
        
        // add by liang.zhang for PR 927097 at 2015-03-11 begin
        String format = "yy:mm:ss";
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(alarm.time);
        String alarmTime = (String) DateFormat.format(format, calendar);
        calendar.setTimeInMillis(now);
        String nowTime = (String) DateFormat.format(format, calendar);
        
        boolean flag = nowTime.endsWith(alarmTime);
        // add by liang.zhang for PR 927097 at 2015-03-11 end
        
        // Maintain a cpu wake lock until the AlarmAlert and AlarmKlaxon can
        // pick it up.
        AlarmAlertWakeLock.acquireCpuWakeLock(context);

        /* Close dialogs and window shade */
        Intent closeDialogs = new Intent(Intent.ACTION_CLOSE_SYSTEM_DIALOGS);
        context.sendBroadcast(closeDialogs);

        // Decide which activity to start based on the state of the keyguard.
        Class c = AlarmAlert.class;
        KeyguardManager km = (KeyguardManager) context.getSystemService(
                Context.KEYGUARD_SERVICE);
        if (km.inKeyguardRestrictedInputMode()) {
            // Use the full screen activity for security.
            c = AlarmAlertFullScreen.class;
        }
        // Trigger a notification that, when clicked, will show the alarm alert
        // dialog. No need to check for fullscreen since this will always be
        // launched from a user action.
        Intent notify = new Intent(context, c);//add XIBIN PR434460 -- There are two alarm reminder when alarm comes
        notify.putExtra(Alarms.ALARM_INTENT_EXTRA, alarm);
        PendingIntent pendingNotify = PendingIntent.getActivity(context,
                alarm.id, notify, 0);

        // Use the alarm's label or the default label as the ticker text and
        // main text of the notification.
        String label = alarm.getLabelOrDefault(context);
        Notification n = new Notification(R.drawable.stat_notify_alarm,
                label, alarm.time);
        n.setLatestEventInfo(context, label,
                context.getString(R.string.alarm_notify_text),
                pendingNotify);
        n.flags |= Notification.FLAG_SHOW_LIGHTS
                | Notification.FLAG_ONGOING_EVENT;
        n.defaults |= Notification.DEFAULT_LIGHTS;

        // NEW: Embed the full-screen UI here. The notification manager will
        // take care of displaying it if it's OK to do so.
        //only show dialog when telephone state is idle
        //add PR456230 xibin satrt --Alarm will ring in incoming video call screen.
//        try {
//            ITelephony mTelephonyService = ITelephony.Stub
//                    .asInterface(ServiceManager
//                            .getService(Context.TELEPHONY_SERVICE));
            TelephonyManager mTelephonyService=(TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
            if (mTelephonyService != null) {
                int mCurrentCallState = mTelephonyService.getCallState();
                if (mCurrentCallState == TelephonyManager.CALL_STATE_IDLE) {
                    // Play the alarm alert and vibrate the device.
                    Intent playAlarm = new Intent(Alarms.ALARM_ALERT_ACTION);
                    playAlarm.putExtra(Alarms.ALARM_INTENT_EXTRA, alarm);
                    // add by liang.zhang for PR 927097 at 2015-03-11 begin
                    if (flag) {
                    	context.startService(playAlarm);
                    } else if (mIsRingoutInCall) {
                    	context.startService(playAlarm);
                    }
                    // add by liang.zhang for PR 927097 at 2015-03-11 end

                    Intent alarmAlert = new Intent(context, c);
                    alarmAlert.putExtra(Alarms.ALARM_INTENT_EXTRA, alarm);
                 
                    alarmAlert.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                            | Intent.FLAG_ACTIVITY_NO_USER_ACTION);
                    //785586 In the status bar the alarm prompt tag is not same with the ringing alarm clock's - liujianmin modify - Begin
                    //n.fullScreenIntent = PendingIntent.getActivity(context,
                    //        alarm.id, alarmAlert, 0);
                    // add by liang.zhang for PR 927097 at 2015-03-11 begin
                    if (flag) {
                    	context.startActivity(alarmAlert);
                    } else if (mIsRingoutInCall) {
                    	context.startActivity(alarmAlert);
                    }
                    // add by liang.zhang for PR 927097 at 2015-03-11 end
                    //785586 In the status bar the alarm prompt tag is not same with the ringing alarm clock's - liujianmin modify - End
                } else {
                	// add by liang.zhang for PR 927097 at 2015-03-11 begin
                	mIsRingoutInCall =  true;
                	// add by liang.zhang for PR 927097 at 2015-03-11 end
                	
                    //PR533869 give a short vibration when alert when phone call. begin
                    Vibrator vibrator = (Vibrator)context.getSystemService(Context.VIBRATOR_SERVICE);
                    if (vibrator != null) {
                        vibrator.vibrate(VIBRATE_LENGTH);
                    }
                    /*--PR 665930 - Neo Skunkworks - Paul Xu added - 001 Begin--*/
                    /*If phone is not CALL_STATE_IDLE state, save the playing alarm id*/                        
                    if(alarm != null){
                        Alarms.savePlayingAlarmID(context, alarm.id);
                    }
                    /*--PR 665930 - Neo Skunkworks - Paul Xu added - 001 End--*/
                    //PR533869 give a short vibration when alert when phone call. end
                }
            }
//        } catch (RemoteException ex) {
//            android.util.Log.e(
//                    "AlarmReceiver : ",
//                    "Catch exception when getPreciseCallState: ex = "
//                            + ex.getMessage());
//        }
        MyLog.debug("Alarm ID->"+ alarm.id, getClass());
        MyLog.debug("Alarm lable->"+ alarm.getLabelOrDefault(context), getClass());
        // add PR456230 xibin end
        // Send the notification using the alarm id to easily identify the
        // correct notification.
        NotificationManager nm = getNotificationManager(context);
        /*PR 594894- Neo Skunkworks - Paul Xu added - 001 Begin*/
        nm.cancel(alarm.id);
        /*PR 594894- Neo Skunkworks - Paul Xu added - 001 End*/
        // add by liang.zhang for PR 927097 at 2015-03-11 begin
        if (flag) {
        	nm.notify(alarm.id, n);
        } else if (mIsRingoutInCall) {
        	mIsRingoutInCall = false;
        	nm.notify(alarm.id, n);
        }
        // add by liang.zhang for PR 927097 at 2015-03-11 end
    }

    private NotificationManager getNotificationManager(Context context) {
        return (NotificationManager)
                context.getSystemService(Context.NOTIFICATION_SERVICE);
    }

    private void updateNotification(Context context, Alarm alarm, int timeout) {
        NotificationManager nm = getNotificationManager(context);

        // If the alarm is null, just cancel the notification.
        if (alarm == null) {

            return;
        }

        // Launch SetAlarm when clicked.
        /*PR 560053- Neo Skunkworks - Paul Xu modified - 001 Begin*/
        /*
        Intent viewAlarm = new Intent(context, SetAlarm.class);
        */
        Intent viewAlarm = new Intent();
        viewAlarm.setAction("android.intent.action.SET_ALARM");   
        /*PR 560053- Neo Skunkworks - Paul Xu modified - 001 End*/
        viewAlarm.putExtra(Alarms.ALARM_INTENT_EXTRA, alarm);
        //huanglin 20130826 for PR513663
        viewAlarm.putExtra(Alarms.INTENT_EXTRA_SUPERMODE, true);
        PendingIntent intent =
                PendingIntent.getActivity(context, alarm.id, viewAlarm, 0);

        // Update the notification to indicate that the alert has been
        // silenced.
        String label = alarm.getLabelOrDefault(context);
        Notification n = new Notification(R.drawable.stat_notify_alarm,
                label, alarm.time);
        n.setLatestEventInfo(context, label,
                context.getString(R.string.alarm_alert_alert_silenced, timeout),
                intent);
        n.flags |= Notification.FLAG_AUTO_CANCEL;
        // We have to cancel the original notification since it is in the
        // ongoing section and we want the "killed" notification to be a plain
        // notification.
        nm.cancel(alarm.id);
        nm.notify(alarm.id, n);
    }
}
