package com.jrdcom.timetool.timer.activity;

import java.util.Locale;

import com.android.deskclock.R;
import com.jrdcom.timetool.timer.service.TimerRunable;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.TextView;

public class RefreshHandler extends Handler {

	private TextView mAbsoluteView;

	private TextView mRelativeView;

	private TimerRunable mTimerRunable;

	private TimerActivity mActivity;

	private static long absoluteTime;

	private static long relativeTime;

	private static final long DELAY_MILLIS = 10;

	private boolean isStart = false;

	private static final long MAX_MILLISECOND = 5999990;
	
	private static final long ONE_HOUR_MILLISECOND = 3600000;

	public RefreshHandler(TimerRunable TimerRunable,
			TimerActivity activity) {
		mActivity = activity;
		mAbsoluteView = (TextView) mActivity
				.findViewById(R.id.Timer_absolute_time);
		mRelativeView = (TextView) mActivity
				.findViewById(R.id.Timer_relative_time);
		mTimerRunable = TimerRunable;
	}

	@Override
	public void handleMessage(Message msg) {
		
		 long time[] = mTimerRunable.gettimes();
		 absoluteTime = time[0];
		 relativeTime = time[1];

		// exceed the max time ,stop
		if (absoluteTime > MAX_MILLISECOND) {
            //modify by Yanjingming for PR526355 begin
            absoluteTime = MAX_MILLISECOND;
            relativeTime = MAX_MILLISECOND - mTimerRunable.getdiff();
            mActivity.stopTimer(true);
            mAbsoluteView.setText(convertTime(MAX_MILLISECOND));
            mRelativeView.setText(convertTime(relativeTime));
            //modify by Yanjingming for PR526355 end
            return;
		}else if(absoluteTime > ONE_HOUR_MILLISECOND){
		    mAbsoluteView.setTextSize(42);
		}
//PR620305-haiying.he-start
		String mLocalLanguage = Locale.getDefault().getLanguage();
        if ("ar".equals(mLocalLanguage) || "fa".equals(mLocalLanguage) || "iw".equals(mLocalLanguage)) {
            mAbsoluteView.setText("\u202D" + convertTime(absoluteTime) + "\u202C");
            mRelativeView.setText("\u202D" + convertTime(relativeTime) + "\u202C");
        }else{
            mAbsoluteView.setText(convertTime(absoluteTime));
            mRelativeView.setText(convertTime(relativeTime));
        }
//PR620305-haiying.he-end

		updateAnalogClock(absoluteTime);

		sleep(DELAY_MILLIS);
	}

	public static String convertTime(long millisecond) {
		long minute = millisecond / 1000 / 60;
		long second = (millisecond - minute * 60 * 1000) / 1000;
		StringBuilder time = new StringBuilder();
		time.append(pad(minute)).append(" : ").append(pad(second))
				.append(" . ");
		if (millisecond % 1000 / 10 < 10)
			time.append(0);
		time.append(Math.abs(millisecond % 1000 / 10));
		return time.toString();
	}

	public void updateAnalogClock(long millisecond) {
		float minute = millisecond / 1000 / 60;
		float second = (millisecond - minute * 60 * 1000) / 1000;
		float minutes = minute + second / 60;

	}

	public void ClearAnalogClock() {
	}

	public void RefreshTextView() {
		mAbsoluteView.setText(convertTime(absoluteTime));
		mRelativeView.setText(convertTime(relativeTime));
	}
	
	public void ClearTextView(){
		mAbsoluteView.setText(R.string.Timer_time_format);
		mRelativeView.setText(R.string.Timer_time_format);
	}

	private static String pad(long c) {
		if (c >= 10)
			return String.valueOf(c);
		else
			return "0" + String.valueOf(c);
	}

	public void sleep(long delayMillis) {
		if (!isStart) {
			return;
		}
		removeMessages(0);
		sendMessageDelayed(obtainMessage(0), delayMillis);
	}

	public void start() {
		isStart = true;
		sendMessage(obtainMessage(0));
	}

	public void stop() {
		isStart = false;
	}

	public boolean isStart() {
		return isStart;
	}

	public void setTimerTextView() {
		long time[] = mTimerRunable.gettimes();
		absoluteTime = time[0];
		relativeTime = time[1];
		mAbsoluteView.setText(convertTime(absoluteTime));
		mRelativeView.setText(convertTime(relativeTime));
	}
}
