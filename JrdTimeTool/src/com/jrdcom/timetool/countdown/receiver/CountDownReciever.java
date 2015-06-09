package com.jrdcom.timetool.countdown.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import com.jrdcom.timetool.countdown.activity.CountDownAlarmAlert;

public class CountDownReciever extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		// Wake the device and stay awake until the AlarmAlert intent is
		// handled. Also acquire the screen lock so that if the AlarmAlert
		// activity is paused, it will be resumed.
		Intent fireAlarm = new Intent(context, CountDownAlarmAlert.class);

		fireAlarm.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
				| Intent.FLAG_ACTIVITY_NO_USER_ACTION);
		context.startActivity(fireAlarm);
	}
}
