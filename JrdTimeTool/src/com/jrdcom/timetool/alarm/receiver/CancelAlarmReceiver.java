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
/* **********************************************************************************************/
/*                            PRESENTATION                                                     */
/*              Copyright (c) 2010 JRD Communications, Inc.                                    */
/* **********************************************************************************************/
/*=========|==============|=========================|==========================================*/
/* date    | author       |FeatureID                |modification                              */
/*=========|==============|=========================|==========================================*/
/*10/16/13 | Xiaorong Yu |PR535346-Xiaorong-Yu-449  |The alarm icon won't disappear in car mode.  */
/*=============================================================================================*/

package com.jrdcom.timetool.alarm.receiver;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Parcel;
import com.android.deskclock.R;
import com.jrdcom.timetool.alarm.AlarmAlertWakeLock;
import com.jrdcom.timetool.alarm.AsyncHandler;
import com.jrdcom.timetool.alarm.provider.Alarm;
import com.jrdcom.timetool.alarm.provider.Alarms;
import android.os.PowerManager.WakeLock;
import android.util.Log;

/* * 
 * The receiver to deal with the broadcast sent from CarMode
 */
public class CancelAlarmReceiver extends BroadcastReceiver {

    /** If the alarm is older than STALE_WINDOW, ignore.  It
        is probably the result of a time or timezone change */
    private final static int STALE_WINDOW = 30 * 60 * 1000;
    
    private final static String TAG = "CancelAlarmReceiver";

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
        Log.d(TAG, "handleIntent intent:" + intent);
        if (!Alarms.ACTION_ALARM_CANCEL.equals(intent.getAction())) {
            // Unknown intent, bail.
            return;
        }

        Alarm alarm = null;
        // Grab the alarm from the intent. Since the remote AlarmManagerService
        // fills in the Intent to add some extra data, it must unparcel the
        // Alarm object. It throws a ClassNotFoundException when unparcelling.
        // To avoid this, do the marshalling ourselves.
        final byte[] data = intent.getByteArrayExtra(Alarms.ACTION_ALARM_CANCEL);
        if (data != null) {
            Parcel in = Parcel.obtain();
            in.unmarshall(data, 0, data.length);
            in.setDataPosition(0);
            alarm = Alarm.CREATOR.createFromParcel(in);
        }

        if (alarm == null) {
            // Make sure we set the next alert if needed.
            Alarms.setNextAlert(context);
            return;
        }
        // Disable the snooze alert if this alarm is the snooze.
        Alarms.disableSnoozeAlert(context, alarm.id);

        // Disable this alarm if it does not repeat.
        if (!alarm.daysOfWeek.isRepeatSet()) {
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
        if (now > alarm.time + STALE_WINDOW) {
            return;
        }
    }
}

