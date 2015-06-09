package com.jrdcom.timetool.timer.service;

import java.util.ArrayList;

import android.os.SystemClock;
import android.util.Log;

public class TimerRunable {

	private Thread mTimeThread;

	private boolean isRunning = false;

	private long mAbsoluteStartTime = 0;

	private long mRelativeStartTime = 0;

	private long mResumeStartTime = 0;

	private long watchTime = 0;

	private ArrayList<Long> timeList = new ArrayList<Long>();

	private class TimeRunnable implements Runnable {
		public void run() {
			while (isRunning) {
				//add by caorongxing for pr 424432 begin
				//watchTime = System.currentTimeMillis();
				//watchTime = (watchTime/10)*10;
				watchTime = getTimeNow();
				//add by caorongxing for pr 424432 end

				if (timeList.size() > 20) {
					timeList.remove(0);
				}
				timeList.add(watchTime);

				try {
					Thread.sleep(10);
				} catch (InterruptedException e) {
					return;
				}
			}
		}
	}
	// add by caorongxing for pr 424432 begin
    public static long getTimeNow() {
        return SystemClock.elapsedRealtime();
    }
    // add by caorongxing for pr 424432 end 
	public void resetStartTime() {
		// add by caorongxing for pr 424432 begin
		//long delta = (System.currentTimeMillis()/10)*10 
		long delta = getTimeNow()- timeList.get(0);
		// add by caorongxing for pr 424432 end
		mAbsoluteStartTime += delta;
		mRelativeStartTime += delta;
	}

	public void start() {
		if (!isRunning) {
			isRunning = true;

			timeList.clear();
			// add by caorongxing for pr 424432 begin
			//mAbsoluteStartTime = (System.currentTimeMillis()/10)*10;
			mAbsoluteStartTime = getTimeNow();
			// add by caorongxing for pr 424432 end

			mRelativeStartTime = mAbsoluteStartTime;

			mTimeThread = new Thread(new TimeRunnable(), "TimerTimeThread");
			mTimeThread.start();
		}
	}

	public void resume() {
		if (!isRunning) {
			isRunning = true;
			// add by caorongxing for pr 424432 begin
			//long currTime = System.currentTimeMillis();			
			//currTime = (currTime/10) * 10;
			long currTime = getTimeNow();
			// add by caorongxing for pr 424432 end
			
			mAbsoluteStartTime = mAbsoluteStartTime
					+ (currTime - mResumeStartTime);
			mRelativeStartTime = mRelativeStartTime
					+ (currTime - mResumeStartTime);
			mTimeThread = new Thread(new TimeRunnable(), "TimerTimeThread");
			mTimeThread.start();
		}
	}

	public void stop() {
		isRunning = false;

		// changed the mRelativeStartTime,
		// add by caorongxing for pr 424432 begin
		//mResumeStartTime = (System.currentTimeMillis()/10)*10;
		mResumeStartTime = getTimeNow();
		// add by caorongxing for pr 424432 end
	}

	public void clean() {
		isRunning = false;

		timeList.clear();
		mAbsoluteStartTime = 0;
		mRelativeStartTime = 0;
		watchTime = 0;
	}

	public long[] record() {
		long[] times = new long[2]; 
		long currenttime = watchTime;
		
		// add by caorongxing for pr 424432 begin
		final long current_time = currenttime;
		// add by caorongxing for pr 424432 end
				
		times[0] = current_time - mAbsoluteStartTime;
		times[1] = current_time - mRelativeStartTime;
		
		mRelativeStartTime = current_time;
		return times;
	
	}

//	public long getAbsoluteTime() {
//		return (watchTime - mAbsoluteStartTime);
//	}
//
//	public long getRelativeTime() {
//		return (watchTime - mRelativeStartTime);
//	}

	public long getAbsoluteTime() {
		return mAbsoluteStartTime;
	}

	public long getRelativeTime() {
		return mRelativeStartTime;
	}

	public long getResumeStartTime() {
		return mResumeStartTime;
	}

	public long getwatchTime() {
		return watchTime;
	}

	public void setAbsoluteTime(long a) {
		mAbsoluteStartTime = a;
	}

	public void setRelativeTime(long b) {
		mRelativeStartTime = b;
	}
	
	public void setResumeStartTime(long c) {
		mResumeStartTime = c;
	}

	public void setwatchTime(long d) {
		watchTime = d;
	}

	public long[] gettimes(){
		long[] times = new long[2]; 
		final long currenttime = watchTime;
		times[0] = currenttime - mAbsoluteStartTime;
		times[1] = currenttime - mRelativeStartTime;
		return times;
	}

    //modify by Yanjingming for PR526355 begin
    // M : get different from RelativeStartTime and AbsoluteStartTime
    public long getdiff(){
        return mRelativeStartTime - mAbsoluteStartTime;
    }
    //modify by Yanjingming for PR526355 end

	public boolean isRun() {
		return isRunning;
	}

}
