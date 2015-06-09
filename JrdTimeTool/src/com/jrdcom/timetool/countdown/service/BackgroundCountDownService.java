package com.jrdcom.timetool.countdown.service;

import android.app.KeyguardManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.SystemProperties;
import android.util.Log;

import com.jrdcom.timetool.alarm.activity.HandleSetAlarm;
import com.jrdcom.timetool.countdown.AlarmAlertWakeLock;
import com.jrdcom.timetool.countdown.activity.CountDownAlarmAlert;
import com.jrdcom.timetool.countdown.activity.CountDownAlarmAlertFullScreen;

public class BackgroundCountDownService extends Service {

	public static int mSecondTime = 0;
	
	public static int mTotalTime = 0;

	private Thread mTimeThread;

	private static boolean isRun = false;

	public final static String TIMER_COUNT_EXTRA = "TIMER_COUNT_EXTRA";
	
	private static final int SHOW_BACKGROUND = 1001;

	public IBinder onBind(Intent i) {
		return null;
	}

	public void onCreate() {
		super.onCreate();
		//Modified by xiaxia.yao for PR:411601 begin
		if (mTimeThread == null) {
			mTimeThread = new Thread(new TimeRunnable(), "TimerThread");
		}
		//Modified by xiaxia.yao for PR:411601 begin
	}

	public int onStartCommand(Intent intent, int flags, int startId) {

		if (!isRun) {
			
			if (intent == null) {
				return super.onStartCommand(intent, flags, startId);
			}
			
			int timeCount = intent.getIntExtra(TIMER_COUNT_EXTRA, 0);
			if (timeCount <= 0) {
				return super.onStartCommand(intent, flags, startId);
			}

			mSecondTime = timeCount;
			mTotalTime = mSecondTime;
			isRun = true;

			mTimeThread.start();

			AlarmAlertWakeLock.acquireCpuWakeLock(this);
			AlarmAlertWakeLock.acquireScreenWakeLock(this);

		} else {

		}

		return super.onStartCommand(intent, flags, startId);
	}

	public void onDestroy() {
		super.onDestroy();

		isRun = false;

		if (mSecondTime != 0) {
			mSecondTime = 0;
		}
		//Added by xiaxia.yao for PR:411601 begin
		mTimeThread.interrupt();
		mTimeThread = null;
		//Added by xiaxia.yao for PR:411601 end
	}

	private class TimeRunnable implements Runnable {

		public void run() {
			/*--PR 667590 	- Neo Skunkworks - Paul Xu added - 001 Begin--*/
			if(HandleSetAlarm.mExtraLength && HandleSetAlarm.mTimerSkipUI){
				HandleSetAlarm.mExtraLength = false;
				HandleSetAlarm.mTimerSkipUI = false;
				mHandler.sendEmptyMessageDelayed(SHOW_BACKGROUND, 1000 * 30);
				isRun = false;
				return ;
			}
			/*--PR 667590 	- Neo Skunkworks - Paul Xu added - 001 End--*/
			while (isRun) {
				if (mSecondTime <= 0) {

					// Close dialogs and window shade
					AlarmAlertWakeLock.acquireCpuWakeLock(BackgroundCountDownService.this);

					BackgroundCountDownService.this.sendBroadcast(new Intent(
							Intent.ACTION_CLOSE_SYSTEM_DIALOGS));

                    //add Pr449151 xibin start -- Cannot pop up dialogue on lock screen when time up
//					Intent fireAlarm = new Intent(BackgroundCountDownService.this,
//							CountDownAlarmAlert.class);
//					KeyguardManager km = (KeyguardManager) getSystemService(Context.KEYGUARD_SERVICE);
					// if the device is in
					// KeyguardRestrictedInputMode,SHOW_WHEN_LOCKED
//					if (km.inKeyguardRestrictedInputMode()) {
					/*PR 627087- Neo Skunkworks - Paul Xu added - 001 Begin*/
					if(getSuperPowerSavingMode() == false){
						Intent fireAlarm = new Intent(BackgroundCountDownService.this,
									CountDownAlarmAlertFullScreen.class);
						fireAlarm.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
								| Intent.FLAG_ACTIVITY_NO_USER_ACTION);
	
						AlarmAlertWakeLock.release();
						AlarmAlertWakeLock.acquireScreenWakeLock(BackgroundCountDownService.this);// add Yanjingming for PR464456
						BackgroundCountDownService.this.startActivity(fireAlarm);
					}
					/*PR 627087- Neo Skunkworks - Paul Xu added - 001 End*/
					isRun = false;
					stopSelf();
					return;
				} else {
                    // PR:498158 update by xibin start
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        return;
                    }
                    mSecondTime--;
                    // PR:498158 update by xibin end
				}
			}
		}

	}
	
	/*--PR 667590 	- Neo Skunkworks - Paul Xu added - 001 Begin--*/	
	private Handler mHandler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			super.handleMessage(msg);
			switch (msg.what) {
			case SHOW_BACKGROUND:
				Intent fireAlarm = new Intent(BackgroundCountDownService.this,
							CountDownAlarmAlertFullScreen.class);
				fireAlarm.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
						| Intent.FLAG_ACTIVITY_NO_USER_ACTION);

				AlarmAlertWakeLock.release();
				AlarmAlertWakeLock.acquireScreenWakeLock(BackgroundCountDownService.this);// add Yanjingming for PR464456
				BackgroundCountDownService.this.startActivity(fireAlarm);
				
				stopSelf();
				
				break;
			}
		}
	};
	/*--PR 667590 	- Neo Skunkworks - Paul Xu added - 001 End--*/
	
	/*PR 627087- Neo Skunkworks - Paul Xu added - 001 Begin*/
    /**
     * Determine whether it is a super power saving mode.
     *
     * @param  null
     * @return boolean
     */
    private boolean  getSuperPowerSavingMode(){
    	boolean superMode = Boolean.parseBoolean(SystemProperties.get("sys.supermode.key", "false"));
    	
    	return superMode;
    }
    /*PR 627087- Neo Skunkworks - Paul Xu added - 001 End*/

	/**
	 * Return the current SecondTime .in second units.
	 * 
	 * @return
	 */
	public static int getSecondTime() {
		return mSecondTime;
	}

	public static boolean isTimerRun() {
		return isRun;
	}
	public static int getTotalTime() {
		return mTotalTime;
	}
    // PR:517007 add by XIBIN start
    public static void setTimerRun(boolean isTimerRun) {
        isRun = isTimerRun;
    }
    // PR:517007 add by XIBIN end
}
