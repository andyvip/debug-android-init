package com.jrdcom.timetool.timer.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

/**
 * empty service is just attempt to make this process not be killed by activity
 * manager service
 */
public class EmptyService extends Service {
	
	private String STOP_STOPWHATCH_ACTION = "stop_Timer_action";

	public IBinder onBind(Intent i) {
		return null;
	}

	public void onCreate() {
		super.onCreate();
	}

	public int onStartCommand(Intent intent, int flags, int startId) {
		return super.onStartCommand(intent, flags, startId);
	}

	public void onDestroy() {
		super.onDestroy();
        Intent TimerStop = new Intent();
        TimerStop.setAction(STOP_STOPWHATCH_ACTION);
       sendBroadcast(TimerStop);
	}
}
