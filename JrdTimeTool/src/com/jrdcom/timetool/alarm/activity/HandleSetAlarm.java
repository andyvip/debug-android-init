/*
 * Copyright (C) 2010 The Android Open Source Project
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

import static android.provider.AlarmClock.ACTION_SET_ALARM;
import static android.provider.AlarmClock.EXTRA_HOUR;
import static android.provider.AlarmClock.EXTRA_MESSAGE;
import static android.provider.AlarmClock.EXTRA_MINUTES;
import static android.provider.AlarmClock.EXTRA_SKIP_UI;
import static android.provider.AlarmClock.EXTRA_LENGTH;
import static android.provider.AlarmClock.EXTRA_RINGTONE;
import java.io.File;
import java.util.Calendar;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Intent;
import android.content.res.Configuration;
import android.database.Cursor;
import android.database.sqlite.SQLiteException;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.os.SystemProperties;

import com.jrdcom.timetool.TimeToolActivity;
import com.jrdcom.timetool.alarm.provider.Alarm;
import com.jrdcom.timetool.alarm.provider.Alarms;
import com.jrdcom.timetool.alarm.provider.RingtoneList;

public class HandleSetAlarm extends Activity {
    // PR:515430 add by xibin start
    private String mDefaultRingtoneName;
    private String mDefaultRingtonePath;
    // PR:515430 add by xibin end
    /* PR 638550- Neo Skunkworks - Paul Xu added - 001 Begin */
    public static boolean mShowAlarm = false;
    public static boolean mSetAlarm = false;
    public static boolean mSetTimer = false;
    public static boolean mExtraLength = false;
    public static boolean mTimerSkipUI = false;
    public static boolean mExtraMinutes = false;
    public static boolean mVerifyDissmiss = false;
    public static boolean mSilentRingTone = false;
    public static int mFromIntentTimecount = 0;//PR743707-haiying.he
    public static String mRingtoneName;
    private static String SHOW_ALARMS_ACTION = "android.intent.action.SHOW_ALARMS";
    private static String SET_TIMER_ACTION = "android.intent.action.SET_TIMER";

    /* PR 638550- Neo Skunkworks - Paul Xu added - 001 End */
    @Override
    protected void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        Intent intent = getIntent();
        /*--PR 667590 	- Neo Skunkworks - Paul Xu modified - 001 Begin--*/
        /*
         * if (intent == null || !ACTION_SET_ALARM.equals(intent.getAction())) {
         * finish(); return; } else if (!intent.hasExtra(EXTRA_HOUR)) {
         * startActivity(new Intent(this, AlarmActivity.class)); finish();
         * return; }
         */
        init();
        /* PR 690902 - Neo Skunkworks - Paul Xu added - 001 Begin */
        if (intent != null) {
            if (Boolean.parseBoolean(SystemProperties.get("sys.supermode.key", "false"))) {
                startActivity(new Intent(this, AlarmActivity.class));
                finish();
                return;
            }
        }
        /* PR 690902 - Neo Skunkworks - Paul Xu added - 001 End */

        if (intent == null) {
            finish();
            return;
        } else if (ACTION_SET_ALARM.equals(intent.getAction())) {
            handleSetAlarm(intent);
            if (mSetAlarm && mExtraMinutes) {
                mSetAlarm = false;
                mExtraMinutes = false;
                mVerifyDissmiss = true;
                final Calendar calendar = Calendar.getInstance();
                calendar.setTimeInMillis(System.currentTimeMillis());
                final int hour = intent.getIntExtra(EXTRA_HOUR, calendar.get(Calendar.HOUR_OF_DAY));
                final int minutes = intent
                        .getIntExtra(EXTRA_MINUTES, calendar.get(Calendar.MINUTE));
                final boolean skipUi = intent.getBooleanExtra(EXTRA_SKIP_UI, false);
                String message = intent.getStringExtra(EXTRA_MESSAGE);
                mRingtoneName = intent.getStringExtra(EXTRA_RINGTONE);
                /* PR 686126 - Neo Skunkworks - Paul Xu modified - 001 Begin */
                if (mRingtoneName != null && mRingtoneName.equals("silent")) {
                    mDefaultRingtoneName = "Silent";
                }
                /* PR 686126 - Neo Skunkworks - Paul Xu modified - 001 End */
                if (message == null) {
                    message = "";
                }
                /* PR 686126 - Neo Skunkworks - Paul Xu modified - 001 Begin */
                if (mRingtoneName != null && mRingtoneName.equals("silent")) {
                    mSilentRingTone = true;
                }
                /* PR 686126 - Neo Skunkworks - Paul Xu modified - 001 End */
                Cursor c = null;
                long timeInMillis = Alarms.calculateAlarm(hour, minutes, new Alarm.DaysOfWeek(0))
                        .getTimeInMillis();
                try {
                    c = getContentResolver().query(
                            Alarm.Columns.CONTENT_URI,
                            Alarm.Columns.ALARM_QUERY_COLUMNS,
                            Alarm.Columns.HOUR + "=" + hour + " AND " + Alarm.Columns.MINUTES + "="
                                    + minutes + " AND " + Alarm.Columns.DAYS_OF_WEEK + "=0 AND "
                                    + Alarm.Columns.MESSAGE + "=?", new String[] {
                                message
                            }, null);
                    if (handleCursorResult(c, timeInMillis, true, skipUi)) {
                        finish();
                        return;
                    }
                } finally {
                    if (c != null)
                        c.close();
                    // Reset for use below.
                    c = null;
                }
                // PR:515430 add by xibin start
                initAlertAndRingtonePath();
                /* PR 686126 - Neo Skunkworks - Paul Xu modified - 001 Begin */
                if (mRingtoneName != null && mRingtoneName.equals("silent")) {
                    mDefaultRingtoneName = "Silent";
                }
                /* PR 686126 - Neo Skunkworks - Paul Xu modified - 001 End */
                ContentValues values = new ContentValues();
                values.put(Alarm.Columns.HOUR, hour);
                values.put(Alarm.Columns.MINUTES, minutes);
                values.put(Alarm.Columns.MESSAGE, message);
                values.put(Alarm.Columns.ENABLED, 1);
                values.put(Alarm.Columns.VIBRATE, 1);
                values.put(Alarm.Columns.DAYS_OF_WEEK, 0);
                values.put(Alarm.Columns.ALARM_TIME, timeInMillis);
                values.put(Alarm.Columns.ALERT, mDefaultRingtoneName);
                values.put(Alarm.Columns.VOLUME, 5);
                values.put(Alarm.Columns.RINGTONE_PATH, mDefaultRingtonePath);
                /* FR 548923- Neo Skunkworks - Paul Xu added - 001 Begin */
                values.put(Alarm.Columns.ALERT_COUNT, 0);
                /* FR 548923- Neo Skunkworks - Paul Xu added - 001 End */
                // PR:515430 add by xibin end
                ContentResolver cr = getContentResolver();
                Uri result = cr.insert(Alarm.Columns.CONTENT_URI, values);
                if (result != null) {
                    try {
                        c = cr.query(result, Alarm.Columns.ALARM_QUERY_COLUMNS, null, null, null);
                        handleCursorResult(c, timeInMillis, false, skipUi);
                    } finally {
                        if (c != null)
                            c.close();
                    }
                }
            } else {
                startActivity(new Intent(this, TimeToolActivity.class));
            }
            finish();
            return;

        } else if (SHOW_ALARMS_ACTION.equals(intent.getAction())) {
            handleShowAlarms();
            finish();
            return;
        } else if (SET_TIMER_ACTION.equals(intent.getAction())) {
            handleSetTimer(intent);
            finish();
            return;
        }
        /*--PR 667590 	- Neo Skunkworks - Paul Xu modified - 001 End--*/

        final Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        final int hour = intent.getIntExtra(EXTRA_HOUR, calendar.get(Calendar.HOUR_OF_DAY));
        final int minutes = intent.getIntExtra(EXTRA_MINUTES, calendar.get(Calendar.MINUTE));
        final boolean skipUi = intent.getBooleanExtra(EXTRA_SKIP_UI, false);
        String message = intent.getStringExtra(EXTRA_MESSAGE);
        if (message == null) {
            message = "";
        }

        Cursor c = null;
        long timeInMillis = Alarms.calculateAlarm(hour, minutes, new Alarm.DaysOfWeek(0))
                .getTimeInMillis();
        try {
            c = getContentResolver().query(
                    Alarm.Columns.CONTENT_URI,
                    Alarm.Columns.ALARM_QUERY_COLUMNS,
                    Alarm.Columns.HOUR + "=" + hour + " AND " + Alarm.Columns.MINUTES + "="
                            + minutes + " AND " + Alarm.Columns.DAYS_OF_WEEK + "=0 AND "
                            + Alarm.Columns.MESSAGE + "=?", new String[] {
                        message
                    }, null);
            if (handleCursorResult(c, timeInMillis, true, skipUi)) {
                finish();
                return;
            }
        } finally {
            if (c != null)
                c.close();
            // Reset for use below.
            c = null;
        }
        // PR:515430 add by xibin start
        initAlertAndRingtonePath();
        ContentValues values = new ContentValues();
        values.put(Alarm.Columns.HOUR, hour);
        values.put(Alarm.Columns.MINUTES, minutes);
        values.put(Alarm.Columns.MESSAGE, message);
        values.put(Alarm.Columns.ENABLED, 1);
        values.put(Alarm.Columns.VIBRATE, 1);
        values.put(Alarm.Columns.DAYS_OF_WEEK, 0);
        values.put(Alarm.Columns.ALARM_TIME, timeInMillis);
        values.put(Alarm.Columns.ALERT, mDefaultRingtoneName);
        values.put(Alarm.Columns.VOLUME, 5);
        values.put(Alarm.Columns.RINGTONE_PATH, mDefaultRingtonePath);
        /* FR 548923- Neo Skunkworks - Paul Xu added - 001 Begin */
        values.put(Alarm.Columns.ALERT_COUNT, 0);
        /* FR 548923- Neo Skunkworks - Paul Xu added - 001 End */
        // PR:515430 add by xibin end
        ContentResolver cr = getContentResolver();
        Uri result = cr.insert(Alarm.Columns.CONTENT_URI, values);
        if (result != null) {
            try {
                c = cr.query(result, Alarm.Columns.ALARM_QUERY_COLUMNS, null, null, null);
                handleCursorResult(c, timeInMillis, false, skipUi);
            } finally {
                if (c != null)
                    c.close();
            }
        }

        finish();
    }

    /*--PR 667590 	- Neo Skunkworks - Paul Xu added - 001 Begin--*/
    /***
     * Processes the SET_ALARM intent
     * 
     * @param intent
     */
    private void handleSetAlarm(Intent intent) {
        mSetAlarm = true;
        mExtraMinutes = intent.hasExtra(EXTRA_MINUTES);
    }

    private void handleShowAlarms() {
        mShowAlarm = true;
        startActivity(new Intent(this, TimeToolActivity.class).putExtra("TAB_FLAG", "alarm"));
    }

    private void handleSetTimer(Intent intent) {
        mSetTimer = true;
        mExtraLength = intent.hasExtra(EXTRA_LENGTH);
        mTimerSkipUI = intent.getBooleanExtra(EXTRA_SKIP_UI, false);
      //PR743707-haiying.he start
        if (mExtraLength) {
            mFromIntentTimecount = intent.getIntExtra(EXTRA_LENGTH, 0);
        }
        Log.d("haiying.he", "handleSetTimer(): " + intent.toString());
        Log.d("haiying.he", "handleSetTimer(): " + intent.getIntExtra(EXTRA_LENGTH, 0));
        Log.d("haiying.he", "handleSetTimer(): " + intent.hasExtra(EXTRA_LENGTH));
      //PR743707-haiying.he end

        startActivity(new Intent(this, TimeToolActivity.class).putExtra("TAB_FLAG", "timer"));
    }

    private void init() {
        mShowAlarm = false;
        mSetAlarm = false;
        mSetTimer = false;
        mExtraLength = false;
        mTimerSkipUI = false;
        mExtraMinutes = false;
        // mVerifyDissmiss = false;
        mSilentRingTone = false;
        mFromIntentTimecount = 0;//PR743707-haiying.he
    }

    /*--PR 667590 	- Neo Skunkworks - Paul Xu added - 001 End--*/

    private boolean handleCursorResult(Cursor c, long timeInMillis, boolean enable, boolean skipUi) {
        if (c != null && c.moveToFirst()) {
            Alarm alarm = new Alarm(c);
            // PR:515430 add by xibin start
            if (enable) {
                alarm.enabled = true;
                Alarms.enableAlarm(this, alarm.id, true);
            }
            SetAlarm.popAlarmSetToast(this, timeInMillis);
            if (skipUi) {
                Alarms.setAlarm(this, alarm);
            } else {
                Intent i = new Intent(this, SetAlarm.class);
                i.putExtra(Alarms.ALARM_INTENT_EXTRA, alarm);
                startActivity(i);
            }
            // PR:515430 add by xibin end
            return true;
        }
        return false;
    }

    // PR:515430 add by xibin start
    private void initAlertAndRingtonePath() {
        Uri defaultRingtoneUri = RingtoneManager.getActualDefaultRingtoneUri(this,
                RingtoneManager.TYPE_ALARM);
        boolean hasDeafault = false;
        Cursor cursor = null;
        if (defaultRingtoneUri != null) {
            try {
                cursor = getContentResolver().query(defaultRingtoneUri, new String[] {
                    MediaStore.Audio.Media.DISPLAY_NAME
                }, null, null, null);
                if (cursor != null && cursor.moveToFirst()) {
                    String defauleName = cursor.getString(0);
                    File[] mediaFiles = RingtoneList.mediaFiles;
                    for (int i = 0; i < mediaFiles.length; i++) {
                        if (defauleName != null && defauleName.equals(mediaFiles[i].getName())) {
                            mDefaultRingtoneName = defauleName;
                            mDefaultRingtonePath = mediaFiles[i].getAbsolutePath();
                            hasDeafault = true;
                        }
                    }
                }
            } catch (SQLiteException sqle) {
                sqle.printStackTrace();
            } finally {
                if (cursor != null)
                    cursor.close();
            }
        }
        if (!hasDeafault) {
            mDefaultRingtoneName = RingtoneList.getRingtoneList(this)[1];
            mDefaultRingtonePath = RingtoneList.mediaFiles[0].getAbsolutePath();
        }
    }

    // PR:515430 add by xibin end

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        // TODO Auto-generated method stub
        super.onConfigurationChanged(newConfig);
    }

}
