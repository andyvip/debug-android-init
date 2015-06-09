/*
 * Copyright (C) 2009 The Android Open Source Project
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

package com.jrdcom.timetool.alarm.activity;

import java.util.Calendar;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.database.Cursor;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.PowerManager;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import android.os.UEventObserver;

import com.android.deskclock.R;
import com.jrdcom.timetool.MyLog;
import com.jrdcom.timetool.alarm.provider.Alarm;
import com.jrdcom.timetool.alarm.provider.Alarms;
import com.jrdcom.timetool.alarm.provider.Alarm.Columns;
import com.jrdcom.timetool.alarm.receiver.AlarmReceiver;
import com.jrdcom.timetool.alarm.view.CustomAlarmDialog;
import com.jrdcom.timetool.countdown.view.CustomDialog;

/**
 * Alarm Clock alarm alert: pops visible indicator and plays alarm tone. This
 * activity is the full screen version which shows over the lock screen with the
 * wallpaper as the background.
 */
public class AlarmAlertFullScreen extends Activity {

    // These defaults must match the values in res/xml/settings.xml
    private static final boolean DEBUG = false;
    private boolean isRegister = false;
    private static final String DEBUG_STRING = "jrdtimetool";
    private static final String DEFAULT_SNOOZE = "5";
    private static final String DEFAULT_VOLUME_BEHAVIOR = "2";
    protected static final String SCREEN_OFF = "screen_off";
    /* FR 548923- Neo Skunkworks - Paul Xu added - 001 Begin */
    private static final int MAX_AUTO_SNOOZECOUNT = 3;
    /* FR 548923- Neo Skunkworks - Paul Xu added - 001 End */
    /* PR 610163- Neo Skunkworks - Paul Xu added - 001 Begin */
    /* Alarm snooze or dissmiss message ID */
    private static final int ALARM_SNOOZE_OR_DISSMISS = 100;
    /* Current degree */
    private double mCurrentDegree = 0;
    /* Last degree string */
    private static final String LAST_SENSOR_DEGREE = "last_degree";
    /* Delay snooze or dissmiss alarm time */
    private static final int DELAY_SNOOZE_DISSMISS_TIME = 450;
    /*
     * When phone is virbating on the desktop, no touch,the delta degree maybe
     * 2.5
     */
    private static final double DELTA_DEGREE = 2.5;
    /* PR 610163- Neo Skunkworks - Paul Xu added - 001 End */

    protected Alarm mAlarm;
    private int mVolumeBehavior;
    boolean mFullscreenStyle;

    private SensorEventListener mSensorEventListener;
    private SensorManager mSensorManager;
    private Sensor mSensor;
    private boolean mIsFaceDown = false;
    private float mCoordinateZ;
    private boolean mIsgetZ = false;
    private boolean mGestureEnable;
    private boolean mSnoozeEnable;
    // Added by xiaxia.yao for turn over to active --007 begin
    private boolean mStopEnable;
    // Added by xiaxia.yao for turn over to active --007 begin
    // Receives the ALARM_KILLED action from the AlarmKlaxon,
    // and also ALARM_SNOOZE_ACTION / ALARM_DISMISS_ACTION from other
    // applications
    /* PR 656709- Neo Skunkworks - Paul Xu added - 001 Begin */
    private PowerManager mPowerManager = null;
    private static final int UEVENT_HANDLER_OPEN_ID = 1001;
    private static final int UEVENT_HANDLER_CLOSE_ID = 1002;
    private static final int UEVENT_HANDLER_DISSMISS_ID = 1003;
    private static final int UEVENT_HANDLER_SNOOZE_ID = 1004;
    private static final int UEVENT_HANDLER_DELAY_TIME = 500;
    private static final int FLIP_COVER_STATE_CLOSE = 0;
    private static final int FLIP_COVER_STATE_OPEN = 1;
    private static final int FLIP_COVER_TYPE = 1;
	private static final String ALARM_TURNOVER_ENABLE = "alarm_turnover_enable";
	private static final String SNOOZE_ENABLE = "snooze_enable";
	private static final String STOP_ENABLE = "stop_enable";
    /* PR 656709- Neo Skunkworks - Paul Xu added - 001 End */
    /* PR 683052 - Neo Skunkworks - Paul Xu added - 001 Begin */
    /* The states of dissmiss or snooze in call state */
    public static boolean mStates = false;
    /* PR 683052 - Neo Skunkworks - Paul Xu added - 001 End */

    //modify by min.qiu for pr871605 begin
    private BroadcastReceiver mDestroyReceiver =  null;
    //modify by min.qiu for pr871605 begin
    
    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            // PR746297-mingwei.han-add begin
            TelephonyManager mTelephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
            int CallState = mTelephonyManager.getCallState();
            // PR746297-mingwei.han-add end
            String action = intent.getAction();
            if (DEBUG) {
                Log.d(DEBUG_STRING, "onReceive action:" + action);
            }
            if (action.equals(Alarms.ALARM_SNOOZE_ACTION)) {
                snooze();
                // PR:494200 by xibin
                /* PR 613533- Neo Skunkworks - Paul Xu modified - 001 Begin */
            } else if (action.equals(Alarms.ALARM_DISMISS_ACTION)
                    || (action.equals(Intent.ACTION_SCREEN_OFF) && CallState != TelephonyManager.CALL_STATE_IDLE)// PR746297-mingwei.han-add  // PR823716-modified by xinlei.sheng-2014/10/30
                    || action.equals(Alarms.ALARM_DELETED)) {
                /* PR 613533- Neo Skunkworks - Paul Xu modified - 001 End */
                /* PR 656709- Neo Skunkworks - Paul Xu added - 001 Begin */
                if (action.equals(Intent.ACTION_SCREEN_OFF) && Alarms.getFlipCoverMode(context)) {
                    return;
                }
                /* PR 656709- Neo Skunkworks - Paul Xu added - 001 End */
                dismiss(false);
                /* FR 548923- Neo Skunkworks - Paul Xu added - 001 Begin */
            } else if (action.equals(Alarms.ALARM_AUTO_SNOOZE_ACTION)) {
                // PR587968 added by tiejun.zhang begin.
                Alarm alarm = intent.getExtras().getParcelable(Alarms.ALARM_INTENT_EXTRA);
                /* PR 626156- Neo Skunkworks - Paul Xu modified - 001 Begin */
                /*
                 * Alarm nearestAlarm = Alarms.getNearestAlarm(context);
                 * if(DEBUG && nearestAlarm != null && alarm != null){
                 * Log.d(DEBUG_STRING, "autoSnooze alarm id:" + alarm.id +
                 * "--nearestAlarm id:" + nearestAlarm.id); } if (alarm != null
                 * && nearestAlarm != null && nearestAlarm.id == alarm.id){
                 * autoSnooze(alarm); }
                 */
                autoSnooze(alarm);
                /* PR 626156- Neo Skunkworks - Paul Xu modified - 001 End */
                // PR587968 added by tiejun.zhang end.
                /* FR 548923- Neo Skunkworks - Paul Xu added - 001 End */
            }// PR746297-mingwei.han-add begin
            else if (action.equals(Intent.ACTION_SHUTDOWN)) {
                dismiss(false);
            }
            // PR746297-mingwei.han-add end
            else {
                Alarm alarm = intent.getParcelableExtra(Alarms.ALARM_INTENT_EXTRA);
                // Alarm nearestAlarm = Alarms.getNearestAlarm(context);
                // add by haifeng.tang PR 773517 2014.9.5 begin
                int playingId = Alarms.getPlayingAlarmId(context);
                if (DEBUG && alarm != null) {
                    Log.d(DEBUG_STRING, "AlarmAlertFullScreen alarm killed alarm.id:" + alarm.id
                            + "--playing id:" + playingId);
                }

                if (alarm != null && playingId == alarm.id) {
                    // add by haifeng.tang PR 773517 2014.9.5 end
                    dismissCurAlarm(alarm);
                }

                /* PR 563538- Neo Skunkworks - Paul Xu modified - 001 End */
            }
        }
    };
    private CustomAlarmDialog customDialog;
    private TextView mLabel;// //PR590800 by tiejun.zhang added for smart cover.

    @Override
    protected void onCreate(Bundle icicle) {
        super.onCreate(icicle);

        // add by haifeng.tang PR 771829 begin

        TelephonyManager tm = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        if (tm.getCallState() != TelephonyManager.CALL_STATE_IDLE) {
            finish();
            return;
        }

        isRegister = true;
        // add by haifeng.tang PR 771829 end

        /* [PR762981]zhouxinzhu 20140818 begin */
        Intent alarmStart = new Intent("com.android.deskclock.START_ALARM_LED");
        this.sendBroadcast(alarmStart);
        /* [PR762981]zhouxinzhu 20140818 end */
        requestWindowFeature(Window.FEATURE_NO_TITLE); // @ add by Yanjingming
                                                       // for pr463926
        mAlarm = getIntent().getParcelableExtra(Alarms.ALARM_INTENT_EXTRA);
        // Added by xiaxia.yao for PR:418295 begin --001
        setFinishOnTouchOutside(false);
        // Added by xiaxia.yao for PR:418295 end --002
        // Get the volume/camera button behavior setting
        final String vol = PreferenceManager.getDefaultSharedPreferences(this).getString(
                SettingsActivity.KEY_VOLUME_BEHAVIOR, DEFAULT_VOLUME_BEHAVIOR);
        mVolumeBehavior = Integer.parseInt(vol);
        // Added by xiaxia.yao for turn over to active --008 begin
        mGestureEnable = (Settings.System.getInt(getContentResolver(),
                 ALARM_TURNOVER_ENABLE, 1) == 1);
        // mGestureEnable = PreferenceManager.getDefaultSharedPreferences(this).
        // getBoolean(SettingsActivity.KEY_ALARM_PREFERENCE, true);
        // SharedPreferences sharedPre =
        // getSharedPreferences(SetAlarm.RINGTONE_OF_PREALARM,
        // MODE_PRIVATE);
        // Added by xiaxia.yao for turn over to active --008 begin
        // mSnoozeEnable = sharedPre.getBoolean(SettingsActivity.SNOOZE_ENABLE,
        // true);
        mSnoozeEnable = Settings.System.getInt(getContentResolver(), SNOOZE_ENABLE,
                1) == 1;
        mStopEnable = Settings.System.getInt(getContentResolver(), STOP_ENABLE, 0) == 1;
        // Added by xiaxia.yao for turn over to active --008 end
        final Window win = getWindow();
        win.addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED |
        /* PR724581- Neo Skunkworks - Tony - 001 begin */
        // set statusbar background to translucent
                WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS
        /* PR724581- Neo Skunkworks - Tony - 001 end */
        // | WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD //add xibin
        // PR442606 --MS screen will be unlocked after validating the alarm
        // notification.
        );
        // Turn on the screen unless we are being launched from the AlarmAlert
        // subclass as a result of the screen turning off.
        if (!getIntent().getBooleanExtra(SCREEN_OFF, false)) {
            win.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
                    | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
                    | WindowManager.LayoutParams.FLAG_ALLOW_LOCK_WHILE_SCREEN_ON);
        //modify by min.qiu for pr871605 begin
        } else {
            mDestroyReceiver = new BroadcastReceiver() {
        		@Override
        		public void onReceive(Context context, Intent intent) {
        			finish();
        		}
            };
            registerReceiver(mDestroyReceiver, new IntentFilter("com.jrdcom.timetool.destroy_alarm_alert"));
        //modify by min.qiu for pr871605 end
        }
		// PR774169-Neo Skunkworks-Soar Gao-001 delete begin
/*
        // PR743107-Neo Skunkworks-kehao.wei-001 add begin
        if (Alarms.getFlipCoverOpenState() && Alarms.getFlipCoverMode(getApplicationContext())) {
            getWindow().setBackgroundDrawable(getWallpaper());
        }
        // PR743107-Neo Skunkworks-kehao.wei-001 add end
*/
		// PR774169-Neo Skunkworks-Soar gao-001 delete end
        updateLayout();
        /* PR 656709- Neo Skunkworks - Paul Xu added - 001 Begin */
        if (Alarms.getFlipCoverOpenState()) {
            mUEventObserver.startObserving("DEVPATH=/devices/platform/scover"); // PR698319-Neo
                                                                                // Skunkworks-kehao.wei
                                                                                // modify
            mPowerManager = (PowerManager) getApplicationContext().getSystemService(
                    Context.POWER_SERVICE);
        }
        /* PR 656709- Neo Skunkworks - Paul Xu added - 001 End */
        // Register to get the alarm killed/snooze/dismiss intent.
        IntentFilter filter = new IntentFilter(Alarms.ALARM_KILLED);
        filter.addAction(Alarms.ALARM_SNOOZE_ACTION);
        filter.addAction(Alarms.ALARM_DISMISS_ACTION);
        // PR:494200 by xibin
        filter.addAction(Intent.ACTION_SCREEN_OFF);
        filter.addAction(Alarms.ALARM_AUTO_SNOOZE_ACTION);
        /* PR 613533- Neo Skunkworks - Paul Xu added - 001 Begin */
        filter.addAction(Alarms.ALARM_DELETED);
        /* PR 613533- Neo Skunkworks - Paul Xu added - 001 End */
        filter.addAction(Intent.ACTION_SHUTDOWN);// PR746297-mingwei.han-add
        registerReceiver(mReceiver, filter);
        /* PR 683052 - Neo Skunkworks - Paul Xu added - 001 Begin */
        setDissmissOrSnoozeState(false);
        /* PR 683052 - Neo Skunkworks - Paul Xu added - 001 End */
    }
    
    
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        // TODO Auto-generated method stub
        super.onConfigurationChanged(newConfig);
    }

    // Added by xiaxia.yao for PR:418295 begin --002
    @Override
    public void setFinishOnTouchOutside(boolean finish) {
        super.setFinishOnTouchOutside(finish);
    }

    // Added by xiaxia.yao for PR:418295 begin --002

    private void setTitle() {
        // add PR437630 XIBIN start --Snooze the alarm, label displays an error
        // after delete the label.
        // final String titleText = mAlarm.getLabelOrDefault(this);
        final String titleText = calibrationLabel(mAlarm.id);
        // add PR461071 xibin start --Alarm to remind label does not appear on
        // the lock screen when time up
        if (customDialog != null) {
            /* PR 656709- Neo Skunkworks - Paul Xu added - 001 Begin */
            if (Alarms.getFlipCoverOpenState()
                    && Alarms.getFlipCoverMode(AlarmAlertFullScreen.this)) {
                customDialog.setLabel(titleText);
            } else {
                customDialog.setTitle(titleText);
            }
            /* PR 656709- Neo Skunkworks - Paul Xu added - 001 End */
        }
        // add PR437630 XIBIN end
    }

    protected int getLayoutResId() {
        return R.layout.alarm_alert_fullscreen;
    }

    private void updateLayout() {
        // add PR461071 xibin start --Alarm to remind label does not appear on
        // the lock screen when time up
        // LayoutInflater inflater = LayoutInflater.from(this);

        // setContentView(inflater.inflate(getLayoutResId(), null));

        /*
         * snooze behavior: pop a snooze confirmation view, kick alarm manager.
         */
        // Button snooze = (Button) findViewById(R.id.snooze);
        // snooze.requestFocus();
        // snooze.setOnClickListener(new Button.OnClickListener() {
        // public void onClick(View v) {
        // snooze();
        // }
        // });
        //
        // /* dismiss button: close notification */
        // findViewById(R.id.dismiss).setOnClickListener(
        // new Button.OnClickListener() {
        // public void onClick(View v) {
        // dismiss(false);
        // }
        // });
        //
        // /* Set the title from the passed in alarm */
        // setTitle();

        CustomAlarmDialog.IAction snoozeAction = new CustomAlarmDialog.IAction() {
            @Override
            public void execution() {
                // TODO Auto-generated method stub
                snooze();
                if (Alarms.getFlipCoverOpenState()
                        && Alarms.getFlipCoverMode(AlarmAlertFullScreen.this)) {
                    powerOff();
                }
                /* PR 673758 - Neo Skunkworks - Paul Xu added - 001 Begin */
                dissmissCustomDialog();
                /* PR 673758 - Neo Skunkworks - Paul Xu added - 001 End */
                /* PR 683052 - Neo Skunkworks - Paul Xu added - 001 Begin */
                setDissmissOrSnoozeState(true);
                /* PR 683052 - Neo Skunkworks - Paul Xu added - 001 End */
            }
        };
        CustomAlarmDialog.IAction dismissAction = new CustomAlarmDialog.IAction() {
            @Override
            public void execution() {
                // TODO Auto-generated method stub
                dismiss(false);
                if (Alarms.getFlipCoverOpenState()
                        && Alarms.getFlipCoverMode(AlarmAlertFullScreen.this)) {
                    powerOff();
                }
                /* PR 673758 - Neo Skunkworks - Paul Xu added - 001 Begin */
                dissmissCustomDialog();
                /* PR 673758 - Neo Skunkworks - Paul Xu added - 001 End */
                /* PR 683052 - Neo Skunkworks - Paul Xu added - 001 Begin */
                setDissmissOrSnoozeState(true);
                /* PR 683052 - Neo Skunkworks - Paul Xu added - 001 End */
            }
        };
        // PR590800 by tiejun.zhang added for smart cover begin.
        /* PR 673758 - Neo Skunkworks - Paul Xu deleted - 001 Begin */
        // PR698319-Neo Skunkworks-kehao.wei modify begin
        if (customDialog != null) {
            customDialog.dismiss();
        }
        // PR698319-Neo Skunkworks-kehao.wei modify end
        /* PR 673758 - Neo Skunkworks - Paul Xu deleted - 001 End */
        // PR590800 by tiejun.zhang added for smart cover end.
        customDialog = new CustomAlarmDialog.Builder(this).create(snoozeAction, dismissAction);
        customDialog.setCanceledOnTouchOutside(false);
        setTitle();
        customDialog.show();
        // add PR461071 xibin end
    }

    /* FR 548923- Neo Skunkworks - Paul Xu added - 001 Begin */
    private void updateAlarmAlertCount(Alarm alarm) {
        Cursor cursor = null;
        int count = 0;
        try {
            cursor = Alarms.getAlarmsCursor(getContentResolver());

            while (cursor != null && cursor.getCount() != 0 && cursor.moveToNext()) {
                if (alarm.id == cursor.getInt(Columns.ALARM_ID_INDEX)) {
                    count = cursor.getInt(Columns.ALARM_ALERT_COUNT_INDEX);
                    break;
                }
            }

        } finally {
            if (cursor != null) {
                cursor.close();
                cursor = null;
            }
        }

        ContentValues values = new ContentValues(12);
        if (count == 0) {
            alarm.alertCount = 1;
        } else if (count == 1) {
            alarm.alertCount = 2;
        } else if (count == 2) {
            alarm.alertCount = 3;
        }
        values.put(Alarm.Columns.ALERT_COUNT, alarm.alertCount);

        ContentResolver resolver = getContentResolver();
        resolver.update(ContentUris.withAppendedId(Alarm.Columns.CONTENT_URI, alarm.id), values,
                null, null);
    }

    private void resetAlarmAlertCount(Alarm alarm, int count) {
        ContentValues values = new ContentValues(12);
        values.put(Alarm.Columns.ALERT_COUNT, count);

        ContentResolver resolver = getContentResolver();
        resolver.update(ContentUris.withAppendedId(Alarm.Columns.CONTENT_URI, alarm.id), values,
                null, null);
    }

    private int getCurAlarmAlertCount(Alarm alarm) {
        Cursor cursor = null;
        int count = 0;
        try {
            cursor = Alarms.getAlarmsCursor(getContentResolver());

            while (cursor != null && cursor.getCount() != 0 && cursor.moveToNext()) {
                if (alarm.id == cursor.getInt(Columns.ALARM_ID_INDEX)) {
                    count = cursor.getInt(Columns.ALARM_ALERT_COUNT_INDEX);
                    break;
                }
            }

        } finally {
            if (cursor != null) {
                cursor.close();
                cursor = null;
            }
        }

        return count;
    }

    /* FR 548923- Neo Skunkworks - Paul Xu added - 001 End */

    // Attempt to auto snooze this alert.
    private void autoSnooze(Alarm alarm) {
        // PR587968 added by tiejun.zhang begin
        // if the to be snoozed alarm is not the same as the current one,ignore
        // it.
        /* PR 589686- Neo Skunkworks - Paul Xu deleted - 001 Begin */
        /*
         * if(DEBUG){ Log.d(DEBUG_STRING, "autoSnooze alarm:" + alarm.id +
         * "--mAlarm:" + mAlarm.id); } if (alarm.id != mAlarm.id) { return; }
         */
        /* PR 589686- Neo Skunkworks - Paul Xu deleted - 001 End */
        // PR587968 added by tiejun.zhang end.
        /* FR 548923- Neo Skunkworks - Paul Xu added - 001 Begin */
        updateAlarmAlertCount(mAlarm);
        if (getCurAlarmAlertCount(mAlarm) == MAX_AUTO_SNOOZECOUNT) {
            resetAlarmAlertCount(mAlarm, 0);
            dismiss(false);
            return;
        }
        /* FR 548923- Neo Skunkworks - Paul Xu added - 001 End */
        // Do not snooze if the snooze button is disabled.
        if (customDialog != null && !customDialog.getSnooze().isEnabled()) {// add
                                                                            // PR461071
                                                                            // xibin
            dismiss(false);
            return;
        }
        // PR-717855-mingwei.han-add begin
        if (!mAlarm.daysOfWeek.isRepeatSet()) {
            mAlarm.enabled = true;
            Alarms.enableAlarm(AlarmAlertFullScreen.this, mAlarm.id, true);
        }
        // PR-717855-mingwei.han-add end
        final String snooze = PreferenceManager.getDefaultSharedPreferences(this).getString(
                SettingsActivity.KEY_ALARM_SNOOZE, DEFAULT_SNOOZE);
        int snoozeMinutes = Integer.parseInt(snooze);

        final long snoozeTime = System.currentTimeMillis() + (1000 * 60 * snoozeMinutes);
        Alarms.saveSnoozeAlert(AlarmAlertFullScreen.this, mAlarm.id, snoozeTime);

        // Get the display time for the snooze and update the notification.
        final Calendar c = Calendar.getInstance();
        c.setTimeInMillis(snoozeTime);

        // Append (snoozed) to the label.
        String label = mAlarm.getLabelOrDefault(this);
        label = getString(R.string.alarm_notify_snooze_label, label);

        // Notify the user that the alarm has been snoozed.
        Intent cancelSnooze = new Intent(this, AlarmReceiver.class);
        cancelSnooze.setAction(Alarms.CANCEL_SNOOZE);
        cancelSnooze.putExtra(Alarms.ALARM_INTENT_EXTRA, mAlarm);
        PendingIntent broadcast = PendingIntent.getBroadcast(this, mAlarm.id, cancelSnooze, 0);
        NotificationManager nm = getNotificationManager();
        Notification n = new Notification(R.drawable.stat_notify_alarm, label, 0);
        // PR 767261 - mingwei.han added - Begin
        if (Alarms.isArFaIwLanguage(AlarmAlertFullScreen.this)) {
            n.setLatestEventInfo(this, "\u202D" + label + "\u202C",
                    getString(R.string.alarm_notify_snooze_text, Alarms.formatTime(this, c)),
                    broadcast);
        } else {
            n.setLatestEventInfo(this, label,
                    getString(R.string.alarm_notify_snooze_text, Alarms.formatTime(this, c)),
                    broadcast);
        }
        // PR 767261 - mingwei.han added - End
        n.flags |= Notification.FLAG_AUTO_CANCEL | Notification.FLAG_ONGOING_EVENT;
        nm.notify(mAlarm.id, n);

        String displayTime = getString(R.string.alarm_alert_snooze_set, snoozeMinutes);
        // Intentionally log the snooze time for debugging.

        // Display the snooze minutes in a toast.
        Toast.makeText(AlarmAlertFullScreen.this, displayTime, Toast.LENGTH_LONG).show();
        stopService(new Intent(Alarms.ALARM_ALERT_ACTION));
        finish();
    }

    private void snooze() {
        /* FR 548923- Neo Skunkworks - Paul Xu added - 001 Begin */
        resetAlarmAlertCount(mAlarm, 1);
        /* FR 548923- Neo Skunkworks - Paul Xu added - 001 End */
        // Do not snooze if the snooze button is disabled.
        if (customDialog != null && !customDialog.getSnooze().isEnabled()) {// add
                                                                            // PR461071
                                                                            // xibin
            dismiss(false);
            return;
        }
        // PR-717855-mingwei.han-add begin
        if (!mAlarm.daysOfWeek.isRepeatSet()) {
            mAlarm.enabled = true;
            Alarms.enableAlarm(AlarmAlertFullScreen.this, mAlarm.id, true);
        }
        // PR-717855-mingwei.han-add end
        // PR746297-mingwei.han-add begin
        int palyalarmId = Alarms.getPlayingAlarmId(AlarmAlertFullScreen.this);
        if (palyalarmId == mAlarm.id) {
            Alarms.savePlayingAlarmID(this, -1);
        }
        // PR746297-mingwei.han-add end
        final String snooze = PreferenceManager.getDefaultSharedPreferences(this).getString(
                SettingsActivity.KEY_ALARM_SNOOZE, DEFAULT_SNOOZE);
        int snoozeMinutes = Integer.parseInt(snooze);

        final long snoozeTime = System.currentTimeMillis() + (1000 * 60 * snoozeMinutes);
        Alarms.saveSnoozeAlert(AlarmAlertFullScreen.this, mAlarm.id, snoozeTime);

        // Get the display time for the snooze and update the notification.
        final Calendar c = Calendar.getInstance();
        c.setTimeInMillis(snoozeTime);

        // Append (snoozed) to the label.
        String label = mAlarm.getLabelOrDefault(this);
        label = getString(R.string.alarm_notify_snooze_label, label);
        
        
        MyLog.debug("AlarmAlert Alarm  ID->"+ mAlarm.id, getClass());
        MyLog.debug("AlarmAlert lable->"+ label, getClass());

        // Notify the user that the alarm has been snoozed.
        Intent cancelSnooze = new Intent(this, AlarmReceiver.class);
        cancelSnooze.setAction(Alarms.CANCEL_SNOOZE);
        cancelSnooze.putExtra(Alarms.ALARM_INTENT_EXTRA, mAlarm);
        PendingIntent broadcast = PendingIntent.getBroadcast(this, mAlarm.id, cancelSnooze, 0);
        NotificationManager nm = getNotificationManager();
        Notification n = new Notification(R.drawable.stat_notify_alarm, label, 0);
        n.setLatestEventInfo(this, label,
                getString(R.string.alarm_notify_snooze_text, Alarms.formatTime(this, c)), broadcast);
        n.flags |= Notification.FLAG_AUTO_CANCEL | Notification.FLAG_ONGOING_EVENT;
        nm.notify(mAlarm.id, n);

        String displayTime = getString(R.string.alarm_alert_snooze_set, snoozeMinutes);
        // Intentionally log the snooze time for debugging.

        // Display the snooze minutes in a toast.
        Toast.makeText(AlarmAlertFullScreen.this, displayTime, Toast.LENGTH_LONG).show();
        stopService(new Intent(Alarms.ALARM_ALERT_ACTION));
        finish();
    }

    private NotificationManager getNotificationManager() {
        return (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
    }

    // Dismiss the alarm.
    private void dismiss(boolean killed) {
        /* FR 548923- Neo Skunkworks - Paul Xu added - 001 Begin */
        resetAlarmAlertCount(mAlarm, 0);
        /* FR 548923- Neo Skunkworks - Paul Xu added - 001 End */
        // PR746297-mingwei.han-add begin
        int palyalarmId = Alarms.getPlayingAlarmId(AlarmAlertFullScreen.this);
        if (palyalarmId == mAlarm.id) {
            Alarms.savePlayingAlarmID(this, -1);
        }
        // PR746297-mingwei.han-add end
        // The service told us that the alarm has been killed, do not modify
        // the notification or stop the service.
        if (!killed) {
            // Cancel the notification and stop playing the alarm
            NotificationManager nm = getNotificationManager();
            nm.cancel(mAlarm.id);
            // moddify by Yan Jingming for547942 begin
            // stopService(new Intent(Alarms.ALARM_ALERT_ACTION));
        }
        stopService(new Intent(Alarms.ALARM_ALERT_ACTION));
        // moddify by Yan Jingming for547942 end
        if (customDialog != null) {
            android.util.Log.v("CustomDialog", "dismissed");
            customDialog.dismiss();
        }

        /*--PR 667590 	- Neo Skunkworks - Paul Xu added - 001 Begin--*/
        if (HandleSetAlarm.mVerifyDissmiss) {
            HandleSetAlarm.mVerifyDissmiss = false;
            Alarms.deleteAlarm(AlarmAlertFullScreen.this, mAlarm.id);
        }
        /*--PR 667590 	- Neo Skunkworks - Paul Xu added - 001 End--*/
        finish();
    }

    /* PR 673758 - Neo Skunkworks - Paul Xu added - 001 Begin */
    private void dissmissCustomDialog() {
        if (customDialog != null) {
            customDialog.dismiss();
        }
    }

    /* PR 673758 - Neo Skunkworks - Paul Xu added - 001 Begin */

    /* PR 563538- Neo Skunkworks - Paul Xu added - 001 Begin */
    private void dismissCurAlarm(Alarm pAlarm) {
        // NotificationManager nm = getNotificationManager();
        // nm.cancel(pAlarm.id);
        stopService(new Intent(Alarms.ALARM_ALERT_ACTION));
        if (customDialog != null) {// PR583575 window leak.
            android.util.Log.v("CustomDialog", "dismissed");
            customDialog.dismiss();
        }
        finish();
    }

    /* PR 563538- Neo Skunkworks - Paul Xu added - 001 End */
    
    // add by liang.zhang for PR 894212 at 2015-01-16 begin
    private PowerManager.WakeLock mWakeLock = null;
    // add by liang.zhang for PR 894212 at 2015-01-16 end
    
    /**
     * this is called when a second alarm is triggered while a previous alert
     * window is still active.
     */
    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        mAlarm = intent.getParcelableExtra(Alarms.ALARM_INTENT_EXTRA);
        setTitle();
        // add by liang.zhang for PR 894212 at 2015-01-16 begin
        if (mPowerManager == null) {
        	mPowerManager = (PowerManager) getApplicationContext().getSystemService(
                    Context.POWER_SERVICE);
        }
        if (!mPowerManager.isScreenOn()) {
        	mWakeLock = mPowerManager.newWakeLock(PowerManager.ACQUIRE_CAUSES_WAKEUP |
        			PowerManager.FULL_WAKE_LOCK | PowerManager.ON_AFTER_RELEASE, MyLog.TAG);
        	mWakeLock.acquire();
        }
        // add by liang.zhang for PR 894212 at 2015-01-16 end
    }

    @Override
    protected void onResume() {
        super.onResume();
        // PR590800 by tiejun.zhang added for smart cover begin.
        if (mLabel != null) {
            // mLabel.setText(mAlarm.label);
        }
        // PR590800 by tiejun.zhang added for smart cover end.

        // If the alarm was deleted at some point, disable snooze.
        if (Alarms.getAlarm(getContentResolver(), mAlarm.id) == null) {
            View snooze = customDialog.getSnooze();
            snooze.setEnabled(false);
        }
        // Turn over the phone to mute the alarm
        mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        mSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        this.mIsgetZ = false;
        if (mGestureEnable) {
            mSensorEventListener = new SensorEventListener() {
                @Override
                public void onSensorChanged(SensorEvent event) {
                    /* PR 610163- Neo Skunkworks - Paul Xu modified - 001 Begin */
                    /*
                     * mCoordinateZ = event.values[2]; // Added by xiaxia.yao
                     * for turn over to active --009 begin if (!mIsgetZ) { if
                     * (mCoordinateZ < 0) { mIsFaceDown = true; } else if
                     * (mCoordinateZ >= 0) { mIsFaceDown = false; //mIsgetZ =
                     * true; } mIsgetZ = true; } if ((!mIsFaceDown &&
                     * mCoordinateZ < -5) || (mIsFaceDown && mCoordinateZ > 5))
                     * { //mIsFaceDown = true;
                     * mSensorManager.unregisterListener(mSensorEventListener);
                     * if (mSnoozeEnable) { snooze(); } //else { if
                     * (mStopEnable) { dismiss(false); } // Added by xiaxia.yao
                     * for turn over to active --009 end }
                     */
                    /* Calculate the acceleration in X,Y,Z directions */
                    double gravitySpeed = Math.sqrt(Math.pow(event.values[SensorManager.DATA_X], 2)
                            + Math.pow(event.values[SensorManager.DATA_Y], 2)
                            + Math.pow(event.values[SensorManager.DATA_Z], 2));
                    /* Converted into an degree in Z direction */
                    double degrees = Math.toDegrees(Math.acos(event.values[SensorManager.DATA_Z]
                            / gravitySpeed));
                    /* If degrees is between 20 to 160, is a invalid degrees */
                    if (degrees > 20 && degrees < 160) {

                        return;
                    }

                    mCurrentDegree = degrees;
                    /* Calculate face down or face up */
                    if (mIsgetZ == false) {
                        if (degrees >= 160 && degrees <= 180) {
                            mIsFaceDown = true;
                        } else if (degrees >= 0 && degrees <= 20) {
                            mIsFaceDown = false;
                        }
                        mIsgetZ = true;
                    }

                    /* If degrees is this, send alarm snooze or dimiss mesage */
                    if ((mIsFaceDown == false && degrees >= 160 && degrees <= 180)
                            || (mIsFaceDown == true && degrees >= 0 && degrees <= 20)) {
                        if (mSnoozeEnable || mStopEnable) {
                            sendSnoozeDismissMessage(degrees);
                        }
                    }
                    /* PR 610163- Neo Skunkworks - Paul Xu modified - 001 End */
                }

                @Override
                public void onAccuracyChanged(Sensor sensor, int accuracy) {
                }
            };
            mSensorManager.registerListener(mSensorEventListener, mSensor,
                    SensorManager.SENSOR_DELAY_UI);
        }
    }

    /* PR 610163- Neo Skunkworks - Paul Xu modified - 001 Begin */
    /**
     * Send alarm snooze or dimiss mesage.
     * 
     * @param double degree
     * @return null
     */
    private void sendSnoozeDismissMessage(double degree) {

        if (mHandler != null) {
            mHandler.sendMessageDelayed(mHandler.obtainMessage(ALARM_SNOOZE_OR_DISSMISS, degree),
                    DELAY_SNOOZE_DISSMISS_TIME);
        }
    }

    /**
     * A handler for message.
     * 
     * @param null
     * @return null
     */
    private Handler mHandler = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            // TODO Auto-generated method stub
            super.handleMessage(msg);

            switch (msg.what) {
                case ALARM_SNOOZE_OR_DISSMISS:
                    handleAlarmSnoozeDissmissMessage(msg);
                    break;

                default:
                    break;
            }
        }
    };

    /**
     * Handle alarm snooze or dissmiss message.
     * 
     * @param Message msg
     * @return null
     */
    private void handleAlarmSnoozeDissmissMessage(Message msg) {

        double lastDegree = (Double) msg.obj;
        if (Math.abs(lastDegree - mCurrentDegree) <= DELTA_DEGREE) {
            mHandler.removeMessages(ALARM_SNOOZE_OR_DISSMISS);
            if (mSnoozeEnable) {
                snooze();
            }

            if (mStopEnable) {
                dismiss(false);
            }
        }
    }

    /* PR 610163- Neo Skunkworks - Paul Xu modified - 001 End */

    @Override
    protected void onPause() {
        mSensorManager.unregisterListener(mSensorEventListener);
        // add by liang.zhang for PR 894212 at 2015-01-16 begin
        if (mWakeLock != null) {
        	mWakeLock.release();
        	mWakeLock = null;
        }
        // add by liang.zhang for PR 894212 at 2015-01-16 end
        super.onPause();
    }

    @Override
    public void onDestroy() {
        /* PR 656709- Neo Skunkworks - Paul Xu added - 001 Begin */
        if (Alarms.getFlipCoverOpenState()) {
            mUEventObserver.stopObserving();
        }
        /* PR 656709- Neo Skunkworks - Paul Xu added - 001 End */
        // No longer care about the alarm being killed.

        // modified by haifeng.tang PR 771829 begin
        if (isRegister) {

            unregisterReceiver(mReceiver);
        }
        
        //added by min.qiu for pr871605 begin
        if (mDestroyReceiver != null) {
        	unregisterReceiver(mDestroyReceiver);
        }
        //added by min.qiu for pr871605 end

        // add by haifeng.tang PR 771829 end
        /* PR 610163- Neo Skunkworks - Paul Xu added - 001 Begin */
        if (mHandler != null) {
            mHandler.removeMessages(ALARM_SNOOZE_OR_DISSMISS);
        }
        /* PR 610163- Neo Skunkworks - Paul Xu added - 001 End */
        super.onDestroy();
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        // Do this on key down to handle a few of the system keys.
        boolean up = event.getAction() == KeyEvent.ACTION_UP;
        switch (event.getKeyCode()) {
            // Volume keys and camera keys dismiss the alarm
            case KeyEvent.KEYCODE_VOLUME_UP:
            case KeyEvent.KEYCODE_VOLUME_DOWN:
            case KeyEvent.KEYCODE_VOLUME_MUTE:
            case KeyEvent.KEYCODE_CAMERA:
            case KeyEvent.KEYCODE_FOCUS:
                if (up) {
                    switch (mVolumeBehavior) {
                        case 1:
                            snooze();
                            break;

                        case 2:
                            dismiss(false);
                            break;

                        default:
                            break;
                    }
                }
                return true;
            default:
                break;
        }
        return super.dispatchKeyEvent(event);
    }

    @Override
    public void onBackPressed() {
        // Don't allow back to dismiss. This method is overriden by AlarmAlert
        // so that the dialog is dismissed.
        return;
    }

    // add PR437630 XIBIN start --Snooze the alarm, label displays an error
    // after delete the label.
    public String calibrationLabel(int id) {
        String label = "";
        ContentResolver contentResolver = getContentResolver();
        Cursor cursor = contentResolver.query(
                Uri.parse("content://" + "com.jrdcom.timetool.alarm" + "/alarm/" + id),
                new String[] {
                    "message"
                }, null, null, null);
        if (cursor != null && cursor.getCount() != 0 && cursor.moveToNext()) {
            label = cursor.getString(cursor.getColumnIndex("message"));
        }
        if (label == null || label.length() == 0) {
            label = getString(R.string.default_label);
        }
        if (cursor != null) {
            cursor.close();
        }
        return label;
    }

    // add PR437630 XIBIN end

    /* PR 656709- Neo Skunkworks - Paul Xu added - 001 Begin */
    /**
     * powerOff.
     * 
     * @param null
     * @return null.
     */
    private void powerOff() {

        if (null != mPowerManager) {
            mPowerManager.goToSleep(SystemClock.uptimeMillis());
        }
    }

    /**
     * UEventObserver.
     * 
     * @param null
     * @return null.
     */
    private UEventObserver mUEventObserver = new UEventObserver() {
        public void onUEvent(UEventObserver.UEvent envent) {
            // PR725321-Neo Skunkworks-kehao.wei-001 modify begin
            int type, state;
            try {
                type = Integer.parseInt(envent.get("TYPE"));
                state = Integer.parseInt(envent.get("STATE"));
            } catch (Exception e) {
                // TODO: handle exception
                Log.e(DEBUG_STRING, "" + e.toString());
                return;
            }
            // PR725321-Neo Skunkworks-kehao.wei-001 modify end

            switch (state) {
                case FLIP_COVER_STATE_CLOSE:
                    // PR742342-Neo Skunkworks-kehao.wei-001 modify begin
                    if (type == FLIP_COVER_TYPE) {
                        // mUEventHandler.sendEmptyMessage(UEVENT_HANDLER_OPEN_ID);
                        // mUEventHandler.sendEmptyMessageDelayed(UEVENT_HANDLER_OPEN_ID,
                        // UEVENT_HANDLER_DELAY_TIME);
                        mUEventHandler.sendEmptyMessage(UEVENT_HANDLER_SNOOZE_ID);
                    } else {
                        mUEventHandler.sendEmptyMessage(UEVENT_HANDLER_DISSMISS_ID);
                    }
                    // PR742342-Neo Skunkworks-kehao.wei-001 modify end
                    break;
                case FLIP_COVER_STATE_OPEN:
                    mUEventHandler.sendEmptyMessageDelayed(UEVENT_HANDLER_CLOSE_ID,
                            UEVENT_HANDLER_DELAY_TIME);
                    break;
                default:
                    break;
            }
        }
    };

    /**
     * Handle for flip cover.
     * 
     * @param null
     * @return null.
     */
    Handler mUEventHandler = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            // TODO Auto-generated method stub
            super.handleMessage(msg);

            switch (msg.what) {
                case UEVENT_HANDLER_OPEN_ID:
                    openMagicWindow();
                    break;
                case UEVENT_HANDLER_CLOSE_ID:
                    closeMagicWindow();
                    break;
                case UEVENT_HANDLER_DISSMISS_ID:
                    dismiss(false);
                    break;
                // PR725321-Neo Skunkworks-kehao.wei-001 add begin
                case UEVENT_HANDLER_SNOOZE_ID:
                    snooze();
                    break;
                // PR725321-Neo Skunkworks-kehao.wei-001 add end

                default:
                    break;
            }
        }
    };

    /**
     * Open magic window.
     * 
     * @param null
     * @return null.
     */
    private void openMagicWindow(){
//        getWindow().setBackgroundDrawable(getWallpaper()); // PR 747642 - Neo Skunkworks - Soar Gao - 001 delete       
        updateLayout();
    }

    /**
     * Close magic window.
     * 
     * @param null
     * @return null.
     */
    private void closeMagicWindow(){
//        getWindow().setBackgroundDrawable(getWallpaper()); //// PR 747642 - Neo Skunkworks - Soar Gao - 001 delete
        updateLayout();
    }

    /* PR 656709- Neo Skunkworks - Paul Xu added - 001 End */

    /* PR 683052 - Neo Skunkworks - Paul Xu added - 001 Begin */
    /**
     * Set Dissmiss Or Snooze State in call state.
     * 
     * @param null
     * @return boolean .
     */
    public static void setDissmissOrSnoozeState(boolean states) {

        mStates = states;
    }

    /**
     * Get Dissmiss Or Snooze State in call state.
     * 
     * @param null
     * @return boolean .
     */
    public static boolean getDissmissOrSnoozeState() {

        return mStates;
    }
    /* PR 683052 - Neo Skunkworks - Paul Xu added - 001 End */
}
