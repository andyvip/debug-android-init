package com.jrdcom.timetool.countdown.activity;

import com.android.deskclock.R;
import com.jrdcom.timetool.countdown.service.BackgroundCountDownService;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.TextView;

public class RefreshHandler extends Handler {

	private TextView mTimerView;

	private CountDownActivity mActivity;
	
	private Animation mAnimation;

	private static final long DELAY_MILLIS = 500; ///@ modify by Yanjingming for pr444946

	private boolean isStart = false;
	
	private int mTime;

	public RefreshHandler(CountDownActivity activity) {
		mActivity = activity;
		mTimerView = (TextView) mActivity.findViewById(R.id.timer_timer);
		Typeface tf = Typeface.createFromAsset(activity.getAssets(), "fonts/Roboto-Thin.ttf");
		mTimerView.setTypeface(tf);
		mAnimation = AnimationUtils.loadAnimation(activity, R.anim.countdown_animation);
		mAnimation.setRepeatCount(0);
	}

	@Override
	public void handleMessage(Message msg) {
		int currentTime = BackgroundCountDownService.getSecondTime();

		mTimerView.setText(CountDownActivity.translateTimeToString(currentTime));
		if(mTime == 0){
		    mTimerView.startAnimation(mAnimation);
		}
		mTime++;
        // modify by Yanjingming-001 for pr444971 begin
        if (currentTime <= 0 && !isStart) {
            // modify by Yan Jingming for FR548923 begin
            Button startBtn = (Button) mActivity.findViewById(R.id.timer_Start);
            Button cancelBtn = (Button) mActivity.findViewById(R.id.timer_CancelofStart);
            startBtn.setVisibility(View.VISIBLE);
            cancelBtn.setVisibility(View.GONE);

            stop();
            // modify by Yan Jingming for FR548923 end
        ///modify by Yanjingming-001 for pr444971 end
			return;
		}
		loop();
	}

	private void loop() {
		if (!isStart) {
			return;
		}
		removeMessages(0);
		sendMessageDelayed(obtainMessage(0), DELAY_MILLIS);
	}

	public void start() {
		isStart = true;
		mTime = 0;
		sendMessage(obtainMessage(0));
	}

	public void stop() {
		isStart = false;
	}

	public boolean isStart() {
		return isStart;
	}

	public void clearnTimerString() {
		mTimerView.setText(CountDownActivity.translateTimeToString(0));
	}

}
