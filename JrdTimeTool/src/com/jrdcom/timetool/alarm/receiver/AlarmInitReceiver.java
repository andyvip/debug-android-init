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
import java.util.HashSet;
import java.util.Set;

import com.android.deskclock.R;
import com.jrdcom.timetool.alarm.AlarmAlertWakeLock;
import com.jrdcom.timetool.alarm.activity.AlarmActivity;
import com.jrdcom.timetool.alarm.activity.SettingsActivity;
import com.jrdcom.timetool.alarm.provider.Alarm;
import com.jrdcom.timetool.alarm.provider.Alarms;
import com.jrdcom.timetool.alarm.AsyncHandler;
import com.jrdcom.timetool.dreamservice.Log;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.BroadcastReceiver;
import android.content.SharedPreferences;
import android.os.PowerManager.WakeLock;
import android.preference.PreferenceManager;

public class AlarmInitReceiver extends BroadcastReceiver {

    /**
     * Sets alarm on ACTION_BOOT_COMPLETED.  Resets alarm on
     * TIME_SET, TIMEZONE_CHANGED
     */
    @Override
    public void onReceive(final Context context, Intent intent) {
        final String action = intent.getAction();

        final PendingResult result = goAsync();
        final WakeLock wl = AlarmAlertWakeLock.createPartialWakeLock(context);
        wl.acquire();
        AsyncHandler.post(new Runnable() {
            @Override public void run() {
                //PR: 480471 update by XIBIN start
                // Remove the snooze alarm after a boot.
//                if (action.equals(Intent.ACTION_BOOT_COMPLETED)) {
//                    Alarms.saveSnoozeAlert(context, Alarms.INVALID_ALARM_ID, -1);
//                }
                //PR: 480471 update by XIBIN end
            	// add by liang.zhang for PR 857546 at 2014-12-19 begin
            	if (action.equals(Intent.ACTION_BOOT_COMPLETED)) {
            		NotificationManager nm = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            		final Calendar c = Calendar.getInstance();
            		
            		// modify by liang.zhang for PR 889441 at 2015-01-04 begin
            		final String snooze = PreferenceManager.getDefaultSharedPreferences(context).getString(
                            SettingsActivity.KEY_ALARM_SNOOZE, "5");
                    int snoozeMinutes = Integer.parseInt(snooze);
                    final long snoozeTime = 1000 * 60 * snoozeMinutes;
                    // modify by liang.zhang for PR 889441 at 2015-01-04 end
                    
            		Intent cancelSnooze = new Intent(context, AlarmReceiver.class);
                    cancelSnooze.setAction(Alarms.CANCEL_SNOOZE);
            		
            		SharedPreferences prefs = context.getSharedPreferences(AlarmActivity.PREFERENCES, 0);
            		// get all alarm id whose is snoozed
            		final Set<String> snoozedIds = prefs.getStringSet("snooze_ids", null);
            		if (snoozedIds != null ) {
	            		for (String snoozeId : snoozedIds) {
	            			int alarmId = Integer.parseInt(snoozeId);
	            			Alarm alarm = Alarms.getAlarm(context.getContentResolver(), alarmId);
	            			if (alarm.enabled) {
	            				// get information of alarm and init notification
	            				String label = alarm.getLabelOrDefault(context);
		            			label = context.getString(R.string.alarm_notify_snooze_label, label);
		            			// modify by liang.zhang for PR 898798 at 2015-01-10 begin
		            			long time = prefs.getLong("snooze_time" + alarm.id, -1);
		            			c.setTimeInMillis(time); // modify by liang.zhang for PR 889441 at 2015-01-04
		            			// modify by liang.zhang for PR 898798 at 2015-01-10 end
		                        cancelSnooze.putExtra(Alarms.ALARM_INTENT_EXTRA, alarm);
		            			PendingIntent broadcast = PendingIntent.getBroadcast(context, alarm.id, cancelSnooze, 0);
		            			
		            	        Notification n = new Notification(R.drawable.stat_notify_alarm, label, 0);
		            	        n.setLatestEventInfo(context, label,
		            	                context.getString(R.string.alarm_notify_snooze_text, Alarms.formatTime(context, c)), broadcast);
		            	        n.flags |= Notification.FLAG_AUTO_CANCEL | Notification.FLAG_ONGOING_EVENT;
		            	        nm.notify(alarmId, n);
	            			}
	            		}
            		}
            	}
            	// add by liang.zhang for PR 857546 at 2014-12-19 end
            	
                Alarms.disableExpiredAlarms(context);
                Alarms.setNextAlert(context);
                result.finish();
                wl.release();
            }
        });
    }
}
