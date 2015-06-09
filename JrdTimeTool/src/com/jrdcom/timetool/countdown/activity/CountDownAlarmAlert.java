package com.jrdcom.timetool.countdown.activity;

import android.app.Activity;
import android.app.KeyguardManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import com.android.deskclock.R;
import com.jrdcom.timetool.countdown.AlarmAlertWakeLock;
import com.jrdcom.timetool.countdown.service.BackgroundCountDownService;
import com.jrdcom.timetool.countdown.service.MediaPlayerService;
import android.net.Uri;

public class CountDownAlarmAlert extends Activity {

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		AlarmAlertWakeLock.acquireCpuWakeLock(this);
		AlarmAlertWakeLock.acquireScreenWakeLock(this);

		final Window win = getWindow();

		KeyguardManager km = (KeyguardManager) getSystemService(Context.KEYGUARD_SERVICE);

		win.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
				| WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
				| WindowManager.LayoutParams.FLAG_ALLOW_LOCK_WHILE_SCREEN_ON
				| WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED);

		requestWindowFeature(Window.FEATURE_NO_TITLE);

		setContentView(R.layout.countdown_alarmalert);

		SharedPreferences sharedPre = getSharedPreferences(
				CountDownChooseRingtoneDialogActivity.TIMER_RPREFERENCES, MODE_PRIVATE);
		String ringtonePath = sharedPre.getString(
		        CountDownChooseRingtoneDialogActivity.ALERT_RINGTONE_PATH_KEY,
		        CountDownChooseRingtoneDialogActivity.ALERT_SILENT_PATH);


		if (!CountDownChooseRingtoneDialogActivity.ALERT_SILENT_PATH.equals(ringtonePath)) {
			Intent intent = new Intent(this, MediaPlayerService.class);
			intent.putExtra(MediaPlayerService.MEDIA_FILE_PATH_EXTRA,
					ringtonePath);
			intent.putExtra(MediaPlayerService.RUN_TIME_EXTRA, 60);

			startService(intent);
		}

		TextView tvTime = (TextView) findViewById(R.id.timer_alert_icon);
		int totalTime = BackgroundCountDownService.getTotalTime();
		tvTime.setText(CountDownActivity.translateTimeToString(totalTime));
		
		Button okButton = (Button) findViewById(R.id.timer_alert_ok);
		okButton.requestFocus();
		okButton.setOnClickListener(new OnClickListener() {

			public void onClick(View v) {
				CountDownActivity.setCurrentState(1);
				closeAlert();
			}
		});
		//Added by xiaxia.yao for PR:419257 begin
		setFinishOnTouchOutside(false);
	}
	
	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		// TODO Auto-generated method stub
		super.onConfigurationChanged(newConfig);
	}

	@Override
	public void setFinishOnTouchOutside(boolean finish) {
		super.setFinishOnTouchOutside(finish);
	}
	//Added by xiaxia.yao for PR:419257 end
	@Override
	public void onAttachedToWindow() {
		super.onAttachedToWindow();
	}

	public void onResume() {
		super.onResume();
	}

	protected void onPause() {
		super.onPause();
		//Added by xiaxia.yao for PR:419257 begin
		CountDownActivity.setCurrentState(1);
		//Added by xiaxia.yao for PR:419257 end
	}

	 @Override
	 protected void onStop() {
	 super.onStop();
	
	 closeAlert();
	 }

	protected void onDestroy() {
		super.onDestroy();
		AlarmAlertWakeLock.release();
	}

    private void closeAlert() {
//PR574723 for AT command AT+CTMRV begin
        getContentResolver().delete(Uri.parse("content://com.jrdcom.timetool.provider"), null, null);
//PR574723 for AT command AT+CTMRV end
        stopService(new Intent(this, MediaPlayerService.class));
        AlarmAlertWakeLock.release();
        finish();
    }
}
