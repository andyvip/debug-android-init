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

package com.jrdcom.timetool.alarm.provider;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Parcel;
import android.os.SystemProperties;
import android.provider.Settings;
import android.text.format.DateFormat;
import android.util.Log;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import com.google.android.collect.Lists;
import com.jrdcom.timetool.alarm.activity.AlarmActivity;
import com.jrdcom.timetool.alarm.provider.Alarm.DaysOfWeek;
import com.jrdcom.timetool.alarm.service.ReflectionTool;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import android.content.res.Resources;
import android.util.Xml;
import org.xmlpull.v1.*;

/**
 * The Alarms provider supplies info about Alarm Clock settings
 */
public class Alarms {

    private static final boolean DEBUG = true;
    private static final String DEBUG_STRING = "jrdtimetool";
    private static final String PATH = "/custpack/plf/JrdTimeTool/";
    private static final String FILE = "isdm_JrdTimeTool_defaults.xml";

    // This action triggers the AlarmReceiver as well as the AlarmKlaxon. It
    // is a public action used in the manifest for receiving Alarm broadcasts
    // from the alarm manager.
    /* PR 655226 - Neo Skunkworks - Paul Xu modified - 001 Begin */
    /*
     * public static final String ALARM_ALERT_ACTION =
     * "com.android.deskclock.ALARM_ALERT"; // PR:415395 20130315 hengfeng.liu
     * modified
     */
    public static final String ALARM_ALERT_ACTION = "com.android.deskclock.START_ALARM";
    /* PR 655226 - Neo Skunkworks - Paul Xu modified - 001 Begin */
    // A public action sent by AlarmKlaxon when the alarm has stopped sounding
    // for any reason (e.g. because it has been dismissed from
    // AlarmAlertFullScreen,
    // or killed due to an incoming phone call, etc).
    public static final String ALARM_DONE_ACTION = "com.android.deskclock.ALARM_DONE"; // PR:415395
                                                                                       // 20130315
                                                                                       // hengfeng.liu
                                                                                       // modified

    // AlarmAlertFullScreen listens for this broadcast intent, so that other
    // applications
    // can snooze the alarm (after ALARM_ALERT_ACTION and before
    // ALARM_DONE_ACTION).
    public static final String ALARM_SNOOZE_ACTION = "com.android.deskclock.ALARM_SNOOZE"; // PR:415395
                                                                                           // 20130315
                                                                                           // hengfeng.liu
                                                                                           // modified

    // AlarmAlertFullScreen listens for this broadcast intent, so that other
    // applications
    // can dismiss the alarm (after ALARM_ALERT_ACTION and before
    // ALARM_DONE_ACTION).
    public static final String ALARM_DISMISS_ACTION = "com.android.deskclock.ALARM_DISMISS"; // PR:415395
                                                                                             // 20130315
                                                                                             // hengfeng.liu
                                                                                             // modified

    /* FR 548923- Neo Skunkworks - Paul Xu added - 001 Begin */
    public static final String ALARM_AUTO_SNOOZE_ACTION = "com.android.deskclock.ALARM_AUTO_SNOOZE";
    /* FR 548923- Neo Skunkworks - Paul Xu added - 001 End */

    /* PR 655226 - Neo Skunkworks - Paul Xu added - 001 Begin */
    public static final String POWER_OFF_ALARM_SERVICE_ACTION = "com.android.deskclock.SNOOZE_ALARM";
    /* PR 655226 - Neo Skunkworks - Paul Xu added - 001 End */

    // This is a private action used by the AlarmKlaxon to update the UI to
    // show the alarm has been killed.
    public static final String ALARM_KILLED = "alarm_killed";

    // Extra in the ALARM_KILLED intent to indicate to the user how long the
    // alarm played before being killed.
    public static final String ALARM_KILLED_TIMEOUT = "alarm_killed_timeout";

    // This string is used to indicate a silent alarm in the db.
    public static final String ALARM_ALERT_SILENT = "silent";

    // This intent is sent from the notification when the user cancels the
    // snooze alert.
    public static final String CANCEL_SNOOZE = "cancel_snooze";

    // This string is used when passing an Alarm object through an intent.
    public static final String ALARM_INTENT_EXTRA = "intent.extra.alarm";

    // huanglin 20130826 for PR513663
    // This string is used when passing an Alarm object through an intent.
    public static final String INTENT_EXTRA_SUPERMODE = "intent.extra.supermode";

    /*--PR 665930 - Neo Skunkworks - Paul Xu added - 001 Begin--*/
    public static final String ALARM_PHONE_STATE_ACTION = "android.intent.action.PHONE_STATE";
    /*--PR 665930 - Neo Skunkworks - Paul Xu added - 001 End--*/

    // This extra is the raw Alarm object data. It is used in the
    // AlarmManagerService to avoid a ClassNotFoundException when filling in
    // the Intent extras.
    public static final String ALARM_RAW_DATA = "intent.extra.alarm_raw";

    private static final String PREF_SNOOZE_IDS = "snooze_ids";
    private static final String PREF_SNOOZE_TIME = "snooze_time";

    private final static String DM12 = "E h:mm aa";
    private final static String DM24 = "E kk:mm";

    private final static String M12 = "h:mm aa";
    // Shared with DigitalClock
    public final static String M24 = "kk:mm";

    public final static int INVALID_ALARM_ID = -1;
    // PR:415395 add by caorongxing for power off alarm begin
    public static final int POWER_OFF_WAKE_UP = 8;
    // PR:415395 add by caorongxing for power off alarm end
    /* PR:415395 20130315 hengfeng.liu added start */
    public static final String PREF_NEAREST_ALARM_ID = "nearest_id";
    public static final String PREF_NEAREST_ALARM_TIME = "nearest_time";
    public static final String NEAREST_ALARM_PREFERENCES = "NearestAlarm";
    /* PR:415395 20130315 hengfeng.liu added end */
    /* PR 613533- Neo Skunkworks - Paul Xu added - 001 Begin */
    // This is a private action used by the AlarmKlaxon to update the UI to
    // show the alarm has been deleted.
    public static final String ALARM_DELETED = "alarm_deleted";
    public static final String PLAYING_ALARM_ID = "alarm_ring_id";
    public static final String PLAYING_ALARM_PREFERENCES = "PlayingAlarm";
    /* PR 613533- Neo Skunkworks - Paul Xu added - 001 End */

    // add xibin PR444396 start -- The alarm automatically start after disable
    // "Vibrate"

    public final static String ACTION_ALARM_CANCEL = "com.jrdcom.carmode_cancel_alarm";// PR535346-Xiaorong-Yu-449
    /* PR 685347- Neo Skunkworks - Paul Xu added - 001 Begin */
    // This string is used start SetAlarm activity.
    public final static String EXTRA_SET_ALARM = "extra_set_alarm";

    /* PR 685347- Neo Skunkworks - Paul Xu added - 001 End */
    /**
     * Creates a new Alarm and fills in the given alarm's id.
     */
    public static long addAlarm(Context context, Alarm alarm) {
        // ContentValues values = createContentValues(alarm);
        // Uri uri = context.getContentResolver().insert(
        // Alarm.Columns.CONTENT_URI, values);
        // alarm.id = (int) ContentUris.parseId(uri);
        //
        // long timeInMillis = calculateAlarm(alarm);
        // if (alarm.enabled) {
        // clearSnoozeIfNeeded(context, timeInMillis);
        // }
        // setNextAlert(context);
        return addAlarm(context, alarm, true);
    }

    public static long addAlarm(Context context, Alarm alarm, boolean isStatusBarIcon) {
        ContentValues values = createContentValues(alarm);
        Uri uri = context.getContentResolver().insert(Alarm.Columns.CONTENT_URI, values);
        alarm.id = (int) ContentUris.parseId(uri);

        long timeInMillis = calculateAlarm(alarm);
        // PR752822-haiying.he start
        // if (alarm.enabled) {
        // clearSnoozeIfNeeded(context, timeInMillis);
        // }
        // PR752822-haiying.he end
        setNextAlert(context, isStatusBarIcon);
        return timeInMillis;
    }

    // add xibin PR444396
    /**
     * Removes an existing Alarm. If this alarm is snoozing, disables snooze.
     * Sets next alert.
     */
    public static void deleteAlarm(Context context, int alarmId) {
        if (alarmId == INVALID_ALARM_ID)
            return;

        ContentResolver contentResolver = context.getContentResolver();
        /* If alarm is snoozing, lose it */
        disableSnoozeAlert(context, alarmId);

        Uri uri = ContentUris.withAppendedId(Alarm.Columns.CONTENT_URI, alarmId);
        contentResolver.delete(uri, "", null);
        setNextAlert(context);
    }

    /**
     * Queries all alarms
     * 
     * @return cursor over all alarms
     */
    public static Cursor getAlarmsCursor(ContentResolver contentResolver) {
        /* PR 570245- Neo Skunkworks - Paul Xu modified - 001 Begin */
        /*
         * return contentResolver.query( Alarm.Columns.CONTENT_URI,
         * Alarm.Columns.ALARM_QUERY_COLUMNS, null, null,
         * Alarm.Columns.DEFAULT_SORT_ORDER);
         */
        Cursor cursor = null;
        try {
            cursor = contentResolver
                    .query(Alarm.Columns.CONTENT_URI, Alarm.Columns.ALARM_QUERY_COLUMNS, null,
                            null, Alarm.Columns.DEFAULT_SORT_ORDER);

            return cursor;
        } catch (Exception ex) {
            if (cursor != null) {
                cursor.close();
                cursor = null;
            }
            return cursor;
        }
        /* PR 570245- Neo Skunkworks - Paul Xu modified - 001 End */
    }

    // Private method to get a more limited set of alarms from the database.
    private static Cursor getFilteredAlarmsCursor(ContentResolver contentResolver) {
        /* PR 570245- Neo Skunkworks - Paul Xu modified - 001 Begin */
        /*
         * return contentResolver.query(Alarm.Columns.CONTENT_URI,
         * Alarm.Columns.ALARM_QUERY_COLUMNS, Alarm.Columns.WHERE_ENABLED, null,
         * null);
         */
        Cursor cursor = null;
        try {
            cursor = contentResolver.query(Alarm.Columns.CONTENT_URI,
                    Alarm.Columns.ALARM_QUERY_COLUMNS, Alarm.Columns.WHERE_ENABLED, null, null);

            return cursor;
        } catch (Exception ex) {
            if (cursor != null) {
                cursor.close();
                cursor = null;
            }
            return cursor;
        }
        /* PR 570245- Neo Skunkworks - Paul Xu modified - 001 End */
    }

    private static ContentValues createContentValues(Alarm alarm) {
        ContentValues values = new ContentValues(12);
        // Set the alarm_time value if this alarm does not repeat. This will be
        // used later to disable expire alarms.
        long time = 0;
        if (!alarm.daysOfWeek.isRepeatSet()) {
            time = calculateAlarm(alarm);
        }

        values.put(Alarm.Columns.ENABLED, alarm.enabled ? 1 : 0);
        values.put(Alarm.Columns.HOUR, alarm.hour);
        values.put(Alarm.Columns.MINUTES, alarm.minutes);
        values.put(Alarm.Columns.ALARM_TIME, time);
        values.put(Alarm.Columns.DAYS_OF_WEEK, alarm.daysOfWeek.getCoded());
        values.put(Alarm.Columns.VIBRATE, alarm.vibrate);
        values.put(Alarm.Columns.MESSAGE, alarm.label);

        // A null alert Uri indicates a silent alarm.
        values.put(Alarm.Columns.ALERT, alarm.alert == null ? ALARM_ALERT_SILENT : alarm.alert);
        values.put(Alarm.Columns.VOLUME, alarm.volume);
        values.put(Alarm.Columns.RINGTONE_PATH, alarm.alertPath);
        /* FR 548923- Neo Skunkworks - Paul Xu added - 001 Begin */
        values.put(Alarm.Columns.ALERT_COUNT, alarm.alertCount);
        /* FR 548923- Neo Skunkworks - Paul Xu added - 001 End */
        
        return values;
    }

    private static void clearSnoozeIfNeeded(Context context, long alarmTime) {
        // If this alarm fires before the next snooze, clear the snooze to
        // enable this alarm.
        SharedPreferences prefs = context.getSharedPreferences(AlarmActivity.PREFERENCES, 0);

        // modify by Yanjingming for 478539 begin
        // Get the list of snoozed alarms
        final Set<String> snoozedIdSet = prefs.getStringSet(PREF_SNOOZE_IDS, new HashSet<String>());
        final List<String> snoozedIds = new ArrayList<String>();
        snoozedIds.addAll(snoozedIdSet);
        if (snoozedIds != null) {
            for (String snoozedAlarm : snoozedIds) {
                final long snoozeTime = prefs.getLong(getAlarmPrefSnoozeTimeKey(snoozedAlarm), 0);
                if (alarmTime < snoozeTime) {
                    final int alarmId = Integer.parseInt(snoozedAlarm);
                    clearSnoozePreference(context, prefs, alarmId);
                    //modify by fan.yang for 948124 begin
                    Alarms.enableAlarm(context, alarmId, false);
                    //modify by fan.yang for 948124 end
                }
            }
        }
        // modify by Yanjingming for 478539 end
    }

    /**
     * Return an Alarm object representing the alarm id in the database. Returns
     * null if no alarm exists.
     */
    public static Alarm getAlarm(ContentResolver contentResolver, int alarmId) {
        Cursor cursor = contentResolver.query(
                ContentUris.withAppendedId(Alarm.Columns.CONTENT_URI, alarmId),
                Alarm.Columns.ALARM_QUERY_COLUMNS, null, null, null);
        Alarm alarm = null;
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                alarm = new Alarm(cursor);
            }
            cursor.close();
        }
        return alarm;
    }

    /**
     * A convenience method to set an alarm in the Alarms content provider.
     * 
     * @return Time when the alarm will fire.
     */
    // add xibin PR444396 start -- The alarm automatically start after disable
    // "Vibrate"
    public static long setAlarm(Context context, Alarm alarm) {
        return setAlarm(context, alarm, true, false);
    }

    public static long setAlarm(Context context, Alarm alarm, boolean isStatusBarIcon,
            boolean ifNeedClearSnooze) {
        ContentValues values = createContentValues(alarm);
        ContentResolver resolver = context.getContentResolver();
        resolver.update(ContentUris.withAppendedId(Alarm.Columns.CONTENT_URI, alarm.id), values,
                null, null);
        // boolean ifNeedClearSnooze
        long timeInMillis = calculateAlarm(alarm);
        // modified by haifeng.tang PR 785586 begin
        if (alarm.enabled && ifNeedClearSnooze) {
            disableSnoozeAlert(context, alarm.id);
            clearSnoozeIfNeeded(context, timeInMillis);
        }
        //modified by haifeng.tang PR 785586 end
        setNextAlert(context, isStatusBarIcon);
        // modified by haifeng.tang PR 778825 end

        return timeInMillis;
    }

    // add xibin PR444396 end
    /**
     * A convenience method to enable or disable an alarm.
     * 
     * @param id corresponds to the _id column
     * @param enabled corresponds to the ENABLED column
     */

    public static void enableAlarm(final Context context, final int id, boolean enabled) {
        enableAlarmInternal(context, id, enabled);
        setNextAlert(context);
    }

    private static void enableAlarmInternal(final Context context, final int id, boolean enabled) {
        enableAlarmInternal(context, getAlarm(context.getContentResolver(), id), enabled);
    }

    private static void enableAlarmInternal(final Context context, final Alarm alarm,
            boolean enabled) {
        if (alarm == null) {
            return;
        }
        ContentResolver resolver = context.getContentResolver();

        ContentValues values = new ContentValues(2);
        values.put(Alarm.Columns.ENABLED, enabled ? 1 : 0);

        // If we are enabling the alarm, calculate alarm time since the time
        // value in Alarm may be old.
        if (enabled) {
            long time = 0;
            if (!alarm.daysOfWeek.isRepeatSet()) {
                time = calculateAlarm(alarm);
            }
            values.put(Alarm.Columns.ALARM_TIME, time);
        } else {
            // Clear the snooze if the id matches.
            disableSnoozeAlert(context, alarm.id);
        }

        resolver.update(ContentUris.withAppendedId(Alarm.Columns.CONTENT_URI, alarm.id), values,
                null, null);
    }

    private static Alarm calculateNextAlert(final Context context) {
        long minTime = Long.MAX_VALUE;
        long now = System.currentTimeMillis();
        final SharedPreferences prefs = context.getSharedPreferences(AlarmActivity.PREFERENCES, 0);

        Set<Alarm> alarms = new HashSet<Alarm>();

        // We need to to build the list of alarms from both the snoozed list and
        // the scheduled
        // list. For a non-repeating alarm, when it goes of, it becomes
        // disabled. A snoozed
        // non-repeating alarm is not in the active list in the database.

        // first go through the snoozed alarms

        final Set<String> snoozedIds = prefs.getStringSet(PREF_SNOOZE_IDS, new HashSet<String>());

        for (String snoozedAlarm : snoozedIds) {
            final int alarmId = Integer.parseInt(snoozedAlarm);
            final Alarm a = getAlarm(context.getContentResolver(), alarmId);
            alarms.add(a);
        }

        // Now add the scheduled alarms
        final Cursor cursor = getFilteredAlarmsCursor(context.getContentResolver());

        if (cursor != null) {
            try {
                if (cursor.moveToFirst()) {
                    do {
                        final Alarm a = new Alarm(cursor);
                        alarms.add(a);
                    } while (cursor.moveToNext());
                }
            } finally {
                cursor.close();
            }
        }
        // PR -605593 - Neo Skunworks - Soar Gao , -001 -begin
        if (cursor != null) {
            cursor.close();
        }
        // PR -605593 - Neo Skunworks - Soar Gao , -001 -end
        Alarm alarm = null;
        for (Alarm a : alarms) {

        	// modify by liang.zhang for PR 910105 at 2015-01-28 begin
        	boolean isRepeatAlarm = false;
        	
            // A time of 0 indicates this is a repeating alarm, so
            // calculate the time to get the next alert.
            if (a.time == 0) {
            	isRepeatAlarm = true;
                a.time = calculateAlarm(a);
            }

            // Update the alarm if it has been snoozed
            updateAlarmTimeForSnooze(prefs, a);

            if (!isRepeatAlarm && a.time < now) {
                // Expired alarm, disable it and move along.
                enableAlarmInternal(context, a, false);
                continue;
            }
        	// modify by liang.zhang for PR 910105 at 2015-01-28 end
            
            if (a.time < minTime) {
                minTime = a.time;
                alarm = a;
            }
        }
        return alarm;
    }

    /**
     * Disables non-repeating alarms that have passed. Called at boot.
     */
    public static void disableExpiredAlarms(final Context context) {
        Cursor cur = getFilteredAlarmsCursor(context.getContentResolver());
        long now = System.currentTimeMillis();

        try {
            /* PR 570245- Neo Skunkworks - Paul Xu modified - 001 Begin */
            /*
             * if (cur.moveToFirst()) {
             */
            if (cur != null && cur.moveToFirst()) {
                /* PR 570245- Neo Skunkworks - Paul Xu modified - 001 End */
                do {
                    Alarm alarm = new Alarm(cur);
                    // A time of 0 means this alarm repeats. If the time is
                    // non-zero, check if the time is before now.
                    if (alarm.time != 0 && alarm.time < now) {
                        enableAlarmInternal(context, alarm, false);
                    }
                } while (cur.moveToNext());
            }
        } finally {
            /* PR 570245- Neo Skunkworks - Paul Xu modified - 001 Begin */
            /*
             * cur.close();
             */
            if (cur != null) {
                cur.close();
            }
            /* PR 570245- Neo Skunkworks - Paul Xu modified - 001 End */
        }
        // PR -605593 - Neo Skunworks - Soar Gao , -001 begin
        if (cur != null) {
            cur.close();
        }
        // PR -605593 - Neo Skunworks - Soar Gao , -001 end
    }

    /**
     * Called at system startup, on time/timezone change, and whenever the user
     * changes alarm settings. Activates snooze if set, otherwise loads all
     * alarms, activates next alert.
     */
    // add xibin PR444396 start -- The alarm automatically start after disable
    // "Vibrate"
    public static void setNextAlert(final Context context) {
        // final Alarm alarm = calculateNextAlert(context);
        // if (alarm != null) {
        // enableAlert(context, alarm, alarm.time);
        // } else {
        // disableAlert(context);
        // }
        setNextAlert(context, true);
    }

    public static void setNextAlert(final Context context, boolean isStatusBarIcon) {
        final Alarm alarm = calculateNextAlert(context);
        if (alarm != null) {
            enableAlert(context, alarm, alarm.time, isStatusBarIcon);
        } else {
            disableAlert(context);
        }
    }

    // add xibin PR444396 end
    /**
     * Sets alert in AlarmManger and StatusBar. This is what will actually
     * launch the alert when the alarm triggers.
     * 
     * @param alarm Alarm.
     * @param atTimeInMillis milliseconds since epoch
     */
    // add xibin PR444396 start -- The alarm automatically start after disable
    // "Vibrate"
    private static void enableAlert(Context context, final Alarm alarm, final long atTimeInMillis) {
        // AlarmManager am = (AlarmManager)
        // context.getSystemService(Context.ALARM_SERVICE);
        //
        // Intent intent = new Intent(ALARM_ALERT_ACTION);
        //
        // // XXX: This is a slight hack to avoid an exception in the remote
        // // AlarmManagerService process. The AlarmManager adds extra data to
        // // this Intent which causes it to inflate. Since the remote process
        // // does not know about the Alarm class, it throws a
        // // ClassNotFoundException.
        // //
        // // To avoid this, we marshall the data ourselves and then parcel a
        // plain
        // // byte[] array. The AlarmReceiver class knows to build the Alarm
        // // object from the byte[] array.
        // Parcel out = Parcel.obtain();
        // alarm.writeToParcel(out, 0);
        // out.setDataPosition(0);
        // intent.putExtra(ALARM_RAW_DATA, out.marshall());
        //
        // PendingIntent sender = PendingIntent.getBroadcast(
        // context, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT);
        //
        // am.set(POWER_OFF_WAKE_UP, atTimeInMillis, sender); // PR:415395
        // rongxing.cao 20120313 modified
        //
        // /* PR:415395 20130315 hengfeng.liu added start */
        // storeNearestAlarm(context, alarm);
        // /* PR:415395 20130315 hengfeng.liu added end */
        // setStatusBarIcon(context, true);
        //
        // Calendar c = Calendar.getInstance();
        // c.setTimeInMillis(atTimeInMillis);
        // String timeString = formatDayAndTime(context, c);
        // saveNextAlarm(context, timeString);
        enableAlert(context, alarm, atTimeInMillis, true);
    }

    private static void enableAlert(Context context, final Alarm alarm, final long atTimeInMillis,
            boolean isStatusBarIcon) {
        AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        Intent intent = new Intent(ALARM_ALERT_ACTION);

        // XXX: This is a slight hack to avoid an exception in the remote
        // AlarmManagerService process. The AlarmManager adds extra data to
        // this Intent which causes it to inflate. Since the remote process
        // does not know about the Alarm class, it throws a
        // ClassNotFoundException.
        //
        // To avoid this, we marshall the data ourselves and then parcel a plain
        // byte[] array. The AlarmReceiver class knows to build the Alarm
        // object from the byte[] array.
        Parcel out = Parcel.obtain();
        alarm.writeToParcel(out, 0);
        out.setDataPosition(0);
        intent.putExtra(ALARM_RAW_DATA, out.marshall());
        // PR853147 by liujianmin Alarm leak begin
        /*
        PendingIntent sender = PendingIntent.getBroadcast(context, 0, intent,
                PendingIntent.FLAG_CANCEL_CURRENT);
        */
        PendingIntent sender = PendingIntent.getBroadcast(context, 0, intent,
                PendingIntent.FLAG_UPDATE_CURRENT);
        // PR853147 by liujianmin Alarm leak end
        // PR828032 by xing.zhao  Click the On/Off Button in alarm occur FC on SkinUpdateVersion   begin
        if (Build.HARDWARE.startsWith("qcom")) {
          am.set(4, atTimeInMillis, sender);
        }else{
        am.set(POWER_OFF_WAKE_UP, atTimeInMillis, sender); // PR:415395
        }                                              // rongxing.cao
                                                           // 20120313 modified
        // PR828032 by xing.zhao Click the On/Off Button in alarm occur FC on SkinUpdateVersion   end
        /* PR:415395 20130315 hengfeng.liu added start */
        /* PR 678654 - Neo Skunkworks - Paul Xu deleted - 001 Begin */
        /*
         * storeNearestAlarm(context, alarm);
         */
        /* PR 678654 - Neo Skunkworks - Paul Xu deleted - 001 End */
        /* PR:415395 20130315 hengfeng.liu added end */
        // add xibin PR445566 start--The alarm automatically start after Set
        // "Repeat"
        if (isStatusBarIcon) {
            setStatusBarIcon(context, isStatusBarIcon);
        }
        // add xibin PR445566 end
        Calendar c = Calendar.getInstance();
        c.setTimeInMillis(atTimeInMillis);
        String timeString = formatDayAndTime(context, c);
        saveNextAlarm(context, timeString);
    }

    // add xibin PR444396 end
    /**
     * Disables alert in AlarmManger and StatusBar.
     * 
     * @param id Alarm ID.
     */
    static void disableAlert(Context context) {
        AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        // PR853147 by liujianmin Alarm leak begin
        /*
        PendingIntent sender = PendingIntent.getBroadcast(context, 0,
                new Intent(ALARM_ALERT_ACTION), PendingIntent.FLAG_CANCEL_CURRENT);
        */
        PendingIntent sender = PendingIntent.getBroadcast(context, 0,
                new Intent(ALARM_ALERT_ACTION), PendingIntent.FLAG_UPDATE_CURRENT);
        // PR853147 by liujianmin Alarm leak end
        am.cancel(sender);
        // add by liang.zhang for PR 862146 at 2014-12-19 begin
        // PR828032 by xing.zhao  Click the On/Off Button in alarm occur FC on SkinUpdateVersion   begin
//        if(Build.HARDWARE.startsWith("mtk")){
        ReflectionTool.cancelPoweroffAlarm(am,context.getPackageName());// add by Yanjingming
//        }              // for pr478225
        // PR828032 by xing.zhao Click the On/Off Button in alarm occur FC on SkinUpdateVersion   end
        // add by liang.zhang for PR 862146 at 2014-12-19 end
        setStatusBarIcon(context, false);
        saveNextAlarm(context, "");
    }

    public static void saveSnoozeAlert(final Context context, final int id, final long time) {
        SharedPreferences prefs = context.getSharedPreferences(AlarmActivity.PREFERENCES, 0);
        if (id == INVALID_ALARM_ID) {
            clearAllSnoozePreferences(context, prefs);
        } else {
            final Set<String> snoozedIds = prefs.getStringSet(PREF_SNOOZE_IDS,
                    new HashSet<String>());
            snoozedIds.add(Integer.toString(id));
            final SharedPreferences.Editor ed = prefs.edit();
            ed.putStringSet(PREF_SNOOZE_IDS, snoozedIds);
            ed.putLong(getAlarmPrefSnoozeTimeKey(id), time);
            ed.apply();
        }
        // Set the next alert after updating the snooze.
        setNextAlert(context);
    }

    private static String getAlarmPrefSnoozeTimeKey(int id) {
        return getAlarmPrefSnoozeTimeKey(Integer.toString(id));
    }

    private static String getAlarmPrefSnoozeTimeKey(String id) {
        return PREF_SNOOZE_TIME + id;
    }

    /**
     * Disable the snooze alert if the given id matches the snooze id.
     */
    public static void disableSnoozeAlert(final Context context, final int id) {
        SharedPreferences prefs = context.getSharedPreferences(AlarmActivity.PREFERENCES, 0);
        if (hasAlarmBeenSnoozed(prefs, id)) {
            // This is the same id so clear the shared prefs.
            clearSnoozePreference(context, prefs, id);
        }
    }

    // Helper to remove the snooze preference. Do not use clear because that
    // will erase the clock preferences. Also clear the snooze notification in
    // the window shade.
    private static void clearSnoozePreference(final Context context, final SharedPreferences prefs,
            final int id) {
        final String alarmStr = Integer.toString(id);
        final Set<String> snoozedIds = prefs.getStringSet(PREF_SNOOZE_IDS, new HashSet<String>());
        if (snoozedIds.contains(alarmStr)) {
            NotificationManager nm = (NotificationManager) context
                    .getSystemService(Context.NOTIFICATION_SERVICE);
            nm.cancel(id);
        }

        final SharedPreferences.Editor ed = prefs.edit();
        snoozedIds.remove(alarmStr);
        ed.putStringSet(PREF_SNOOZE_IDS, snoozedIds);
        ed.remove(getAlarmPrefSnoozeTimeKey(alarmStr));
        ed.apply();
    }

    private static void clearAllSnoozePreferences(final Context context,
            final SharedPreferences prefs) {
        NotificationManager nm = (NotificationManager) context
                .getSystemService(Context.NOTIFICATION_SERVICE);
        final Set<String> snoozedIds = prefs.getStringSet(PREF_SNOOZE_IDS, new HashSet<String>());
        final SharedPreferences.Editor ed = prefs.edit();
        for (String snoozeId : snoozedIds) {
            nm.cancel(Integer.parseInt(snoozeId));
            ed.remove(getAlarmPrefSnoozeTimeKey(snoozeId));
        }
        ed.remove(PREF_SNOOZE_IDS);
        ed.apply();
    }

    private static boolean hasAlarmBeenSnoozed(final SharedPreferences prefs, final int alarmId) {
        final Set<String> snoozedIds = prefs.getStringSet(PREF_SNOOZE_IDS, null);

        // Return true if there a valid snoozed alarmId was saved
        return snoozedIds != null && snoozedIds.contains(Integer.toString(alarmId));
    }

    /**
     * Updates the specified Alarm with the additional snooze time. Returns a
     * boolean indicating whether the alarm was updated.
     */
    private static boolean updateAlarmTimeForSnooze(final SharedPreferences prefs, final Alarm alarm) {
        if (!hasAlarmBeenSnoozed(prefs, alarm.id)) {
            // No need to modify the alarm
            return false;
        }

        final long time = prefs.getLong(getAlarmPrefSnoozeTimeKey(alarm.id), -1);
        // The time in the database is either 0 (repeating) or a specific time
        // for a non-repeating alarm. Update this value so the AlarmReceiver
        // has the right time to compare.
        alarm.time = time;

        return true;
    }

    /**
     * Tells the StatusBar whether the alarm is enabled or disabled PR:500047
     * update by XIBIN
     */
    public static void setStatusBarIcon(Context context, boolean enabled) {
        Intent alarmChanged = new Intent("android.intent.action.ALARM_CHANGED");
        alarmChanged.putExtra("alarmSet", enabled);
        context.sendBroadcast(alarmChanged);
    }

    private static long calculateAlarm(Alarm alarm) {
        return calculateAlarm(alarm.hour, alarm.minutes, alarm.daysOfWeek).getTimeInMillis();
    }

    /**
     * Given an alarm in hours and minutes, return a time suitable for setting
     * in AlarmManager.
     */
    public static Calendar calculateAlarm(int hour, int minute, Alarm.DaysOfWeek daysOfWeek) {

        // start with now
        Calendar c = Calendar.getInstance();
        c.setTimeInMillis(System.currentTimeMillis());

        int nowHour = c.get(Calendar.HOUR_OF_DAY);
        // /add by Yanjingming-001 for pr445587 begin
        if (24 == hour) {
            hour = 0;
        }
        // /add by Yanjingming-001 for pr445587 end
        int nowMinute = c.get(Calendar.MINUTE);

        // if alarm is behind current time, advance one day
        if (hour < nowHour || hour == nowHour && minute <= nowMinute) {
            c.add(Calendar.DAY_OF_YEAR, 1);
        }
        c.set(Calendar.HOUR_OF_DAY, hour);
        c.set(Calendar.MINUTE, minute);
        c.set(Calendar.SECOND, 0);
        c.set(Calendar.MILLISECOND, 0);

        int addDays = daysOfWeek.getNextAlarm(c);
        if (addDays > 0)
            c.add(Calendar.DAY_OF_WEEK, addDays);
        return c;
    }

    public static String formatTime(final Context context, int hour, int minute,
            Alarm.DaysOfWeek daysOfWeek) {
        Calendar c = calculateAlarm(hour, minute, daysOfWeek);
        return formatTime(context, c);
    }

    /* used by AlarmAlert */
    public static String formatTime(final Context context, Calendar c) {
        String format = get24HourMode(context) ? M24 : M12;
        return (c == null) ? "" : (String) DateFormat.format(format, c);
    }

    /**
     * Shows day and time -- used for lock screen
     */
    private static String formatDayAndTime(final Context context, Calendar c) {
        String format = get24HourMode(context) ? DM24 : DM12;
        return (c == null) ? "" : (String) DateFormat.format(format, c);
    }

    /**
     * Save time of the next alarm, as a formatted string, into the system
     * settings so those who care can make use of it.
     */
    static void saveNextAlarm(final Context context, String timeString) {
        Settings.System.putString(context.getContentResolver(),
                Settings.System.NEXT_ALARM_FORMATTED, timeString);
    }

    /**
     * @return true if clock is set to 24-hour mode
     */
    public static boolean get24HourMode(final Context context) {
        return android.text.format.DateFormat.is24HourFormat(context);
    }

    /* PR:415395 20130315 hengfeng.liu added start */
    /**
     * Store the nearest alarm into preference.
     * 
     * @param alarm Alarm to stored.
     * @param context context.
     */
    public static void storeNearestAlarm(final Context context, final Alarm alarm) {
        if (DEBUG) {
            Log.d(DEBUG_STRING, "storeNearestAlarm alarm.id:" + alarm.id);
        }
        if (alarm.id == -1) {
            return;
        } else {
            SharedPreferences prefs = context.getSharedPreferences(NEAREST_ALARM_PREFERENCES, 0);
            int alarmId = prefs.getInt(PREF_NEAREST_ALARM_ID, 0);
            long time = prefs.getLong(PREF_NEAREST_ALARM_TIME, 0);
            Log.d(DEBUG_STRING, "storeNearestAlarm alarmId:" + alarmId);
            if (alarm.id == alarmId && time == alarm.time) {
                return;
            }
            SharedPreferences.Editor ed = prefs.edit();
            ed.clear();
            ed.putInt(PREF_NEAREST_ALARM_ID, alarm.id);
            ed.putLong(PREF_NEAREST_ALARM_TIME, alarm.time);
            ed.apply();
        }
    }

    /**
     * Get the nearest alarm from preference file.
     * 
     * @param context
     * @return the nearest alarm object, if not set, return null.
     */
    public static Alarm getNearestAlarm(final Context context) {
        SharedPreferences prefs = context.getSharedPreferences(NEAREST_ALARM_PREFERENCES, 0);
        int alarmId = prefs.getInt(PREF_NEAREST_ALARM_ID, -1);

        if (alarmId == -1) {
            return null;
        }

        ContentResolver cr = context.getContentResolver();
        return Alarms.getAlarm(cr, alarmId);
    }

    // PR700067-Neo Skunkworks-kehao.wei-001 add begin
    public static Alarm getLatestAlarm(final Context context) {
        List<Alarm> result = new LinkedList<Alarm>();
        final SharedPreferences prefs = context.getSharedPreferences(AlarmActivity.PREFERENCES, 0);
        final Set<String> snoozedIds = prefs.getStringSet(PREF_SNOOZE_IDS, new HashSet<String>());

        for (String snoozedAlarm : snoozedIds) {
            final int alarmId = Integer.parseInt(snoozedAlarm);
            final Alarm a = getAlarm(context.getContentResolver(), alarmId);
            result.add(a);
        }

        ContentResolver cr = context.getContentResolver();
        Cursor cursor = getFilteredAlarmsCursor(cr);
        if (null != cursor) {
            try {
                if (cursor.moveToFirst()) {
                    do {
                        result.add(new Alarm(cursor));
                    } while (cursor.moveToNext());
                }
            } finally {
                cursor.close();
            }
        }
        // PR -605593 - Neo Skunworks - Soar Gao , -001 -begin
        if (cursor != null) {
            cursor.close();
        }
        // PR -605593 - Neo Skunworks - Soar Gao , -001 -end
        long now = System.currentTimeMillis();
        Alarm nextAlarm = null;
        for (Alarm alarm : result) {
            if (alarm.time == 0) {
                alarm.time = calculateAlarmDetail(alarm);
            }
            // Update the alarm if it has been snoozed
            updateAlarmTimeForSnooze(prefs, alarm);

            if (null == nextAlarm || (alarm.time < nextAlarm.time)
                    || (Math.abs(alarm.time - now) < Math.abs(nextAlarm.time - now))) {
                nextAlarm = alarm;
            }
        }
        return nextAlarm;
    }

    private static long calculateAlarmDetail(Alarm alarm) {
        Calendar c = Calendar.getInstance();
        c.setTimeInMillis(System.currentTimeMillis());
        int hour = alarm.hour;
        int minute = alarm.minutes;
        DaysOfWeek daysOfWeek = alarm.daysOfWeek;
        int nowHour = c.get(Calendar.HOUR_OF_DAY);
        // /add by Yanjingming-001 for pr445587 begin
        if (24 == hour) {
            hour = 0;
        }
        // /add by Yanjingming-001 for pr445587 end
        int nowMinute = c.get(Calendar.MINUTE);

        // if alarm is behind current time, advance one day
        if (hour < nowHour || hour == nowHour && minute < nowMinute) {
            c.add(Calendar.DAY_OF_YEAR, 1);
        }
        c.set(Calendar.HOUR_OF_DAY, hour);
        c.set(Calendar.MINUTE, minute);
        c.set(Calendar.SECOND, 0);
        c.set(Calendar.MILLISECOND, 0);

        int addDays = daysOfWeek.getNextAlarm(c);
        if (addDays > 0)
            c.add(Calendar.DAY_OF_WEEK, addDays);
        return c.getTimeInMillis();
    }

    // PR700067-Neo Skunkworks-kehao.wei-001 add end

    /* PR 605870- Neo Skunkworks - Paul Xu added - 001 Begin */
    /**
     * Get the nearest alarm id from preference file.
     * 
     * @param context
     * @return the nearest alarm id.
     */
    public static int getNearestAlarmId(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(NEAREST_ALARM_PREFERENCES, 0);
        int alarmId = prefs.getInt(PREF_NEAREST_ALARM_ID, -1);

        return alarmId;
    }

    /**
     * Save the playing alarm id from preference file.
     * 
     * @param Context context
     * @param id
     * @return the playing alarm id.
     */
    public static void savePlayingAlarmID(final Context context, final int id) {
        SharedPreferences prefs = context.getSharedPreferences(PLAYING_ALARM_PREFERENCES, 0);
        final SharedPreferences.Editor ed = prefs.edit();
        ed.clear();
        ed.putInt(PLAYING_ALARM_ID, id);
        ed.apply();
    }

    /**
     * Get the playing alarm id from preference file.
     * 
     * @param Context context
     * @return the playing alarm id.
     */
    public static int getPlayingAlarmId(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PLAYING_ALARM_PREFERENCES, 0);
        int alarmId = prefs.getInt(PLAYING_ALARM_ID, -1);

        return alarmId;
    }

    /* PR 605870- Neo Skunkworks - Paul Xu added - 001 End */

    /**
     * Whether this boot is from power off alarm or schedule power on or normal
     * boot.
     * 
     * @param context
     * @return
     */
    public static boolean bootFromPoweroffAlarm() {
        String bootReason = SystemProperties.get("sys.boot.reason");
        boolean ret = (bootReason != null && bootReason.equals("1")) ? true : false;
        return ret;
    }

    /* PR:415395 20130315 hengfeng.liu added end */

    /**
     * get isdm value which is bool
     * 
     * @param mContext
     * @param def_name : the name of isdmID
     * @return
     */
    public static boolean getBoolean(Context mContext, String def_name) {
        Resources res = mContext.getResources();
        int id = res.getIdentifier(def_name, "bool", mContext.getPackageName());
        // get the native isdmID value
        boolean result = mContext.getResources().getBoolean(id);
        try {
            String bool_frameworks = getISDMString(new File(PATH + FILE), def_name, "bool");
            if (null != bool_frameworks) {
                result = Boolean.parseBoolean(bool_frameworks);
            }
        } catch (XmlPullParserException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return result;
    }

    /**
     * get isdm value which is bool
     * 
     * @param mContext
     * @param def_name : the name of isdmID
     * @return
     */
    public static String getString(Context mContext, String def_name) {
        Resources res = mContext.getResources();
        int id = res.getIdentifier(def_name, "string", mContext.getPackageName());
        // get the native isdmID value
        String result = mContext.getResources().getString(id);
        try {
            String string_frameworks = getISDMString(new File(PATH + FILE), def_name, "string");
            if (null != string_frameworks) {
                result = string_frameworks;
            }
        } catch (XmlPullParserException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return result;
    }

    /**
     * parser the XML file to get the isdmID value
     * 
     * @param file : xml file
     * @param name : isdmID
     * @param type : isdmID type like bool and string
     * @return
     * @throws XmlPullParserException
     * @throws IOException
     */
    private static String getISDMString(File file, String name, String type)
            throws XmlPullParserException, IOException {
        if (!file.exists() || null == file) {
            if (DEBUG) {
                Log.e(DEBUG_STRING, "file not exist : " + file);
            }
            return null;
        }
        String result = null;
        InputStream inputStream = new FileInputStream(file);
        XmlPullParser xmlParser = Xml.newPullParser();
        xmlParser.setInput(inputStream, "utf-8");

        int evtType = xmlParser.getEventType();
        boolean query_end = false;
        while (evtType != XmlPullParser.END_DOCUMENT && !query_end) {

            switch (evtType) {
                case XmlPullParser.START_TAG:

                    String start_tag = xmlParser.getAttributeValue(null, "name");
                    String start_type = xmlParser.getName();
                    if (null != start_tag && type.equals(start_type) && start_tag.equals(name)) {
                        result = xmlParser.nextText();
                        query_end = true;
                    }
                    break;

                case XmlPullParser.END_TAG:

                    break;

                default:
                    break;
            }
            // move to next node if not tail
            evtType = xmlParser.next();
        }
        inputStream.close();
        return result;
    }

    /* PR 656709- Neo Skunkworks - Paul Xu added - 001 Begin */
    /**
     * Get flip cover open state.
     * 
     * @param null
     * @return boolean state.
     */
    public static boolean getFlipCoverOpenState() {
        boolean state = SystemProperties.getBoolean("feature_tctfw_flipCover_on", false);

        return state;
    }

    /**
     * Get flip cover mode.
     * 
     * @param Context context
     * @return boolean .
     */

	public static boolean getFlipCoverMode(Context context) {

        if ("2".equals(Settings.Global.getString(context.getContentResolver(), "flip_cover_mode"))) {
            return true;
        }

        return false;
    }

    /* PR 656709- Neo Skunkworks - Paul Xu added - 001 End */

    /* PR 687407 - Neo Skunkworks - Paul Xu added - 001 Begin */
    /**
     * Need vibrate preference
     * 
     * @param Context context
     * @return boolean result.
     */
    public static boolean needVibratePreference(Context context) {
        boolean result = getBoolean(context, "def_alarm_need_vibrate");

        return result;
    }

    /* PR 687407 - Neo Skunkworks - Paul Xu added - 001 End */

    /* PR 767261 - mingwei.han added - Begin */
    /**
     * get locale language is "ar", "fa", "iw".
     * 
     * @param Context context
     * @return boolean.
     */
    public static boolean isArFaIwLanguage(Context context) {
        String language = context.getResources().getConfiguration().locale.getLanguage();
        if ("ar".equals(language) || "fa".equals(language) || "iw".equals(language)) {
            return true;
        }
        return false;
    }
    /* PR 767261 - mingwei.han added - End */
}
