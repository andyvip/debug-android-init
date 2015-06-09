package com.jrdcom.timetool.countdown.activity;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.PowerManager;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.telephony.TelephonyManager;
import android.view.Window;
import android.view.WindowManager;

import com.jrdcom.timetool.countdown.AlarmAlertWakeLock;
import com.jrdcom.timetool.countdown.service.MediaPlayerService;
import com.jrdcom.timetool.countdown.view.CustomDialog;
import android.net.Uri;

public class CountDownAlarmAlertFullScreen extends Activity {

 // modify Yanjingming for PR464456 begin
    private final int CLOSEALERT = 1;
    private final int DELAY_MILLIS = 2000;
    private boolean isRinging = false;
    private PowerManager mPowerManager; // PR826845-and by xinlei.sheng

    Handler mHandler = new Handler(){
        public void handleMessage(Message msg){
            switch(msg.what){
            case CLOSEALERT:
                closeAlert();
                CountDownActivity.setCurrentState(1);
                break;
            }
        super.handleMessage(msg);
        }
    };
 // modify Yanjingming for PR464456 end

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        AlarmAlertWakeLock.acquireCpuWakeLock(this);
        AlarmAlertWakeLock.acquireScreenWakeLock(this);

        mPowerManager = (PowerManager) getSystemService(Context.POWER_SERVICE); // PR826845-and by xinlei.sheng

        final Window win = getWindow();

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        //add Pr449151 xibin start -- Cannot pop up dialogue on lock screen when time up
//        setContentView(R.layout.countdown_alarmalertfullscreen);

        win.addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
//                | WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD
                | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);
     // modify Yanjingming for PR464456 begin
        startAlert();
    }

    @Override
	public void onConfigurationChanged(Configuration newConfig) {
		// TODO Auto-generated method stub
		super.onConfigurationChanged(newConfig);
	}

	private void startAlert() {
//        try {
//            ITelephony mTelephonyService = ITelephony.Stub
//                    .asInterface(ServiceManager
//                            .getService(Context.TELEPHONY_SERVICE));
            TelephonyManager mTelephonyService=(TelephonyManager) this.getSystemService(Context.TELEPHONY_SERVICE);
            if (mTelephonyService != null) {
                int mCurrentCallState = mTelephonyService.getCallState();
                if (mCurrentCallState == TelephonyManager.CALL_STATE_RINGING) {
                    isRinging = true;
                }
             }
//         } catch (RemoteException ex) {
//             android.util.Log.e(
//                     "AlarmReceiver : ",
//                     "Catch exception when getPreciseCallState: ex = "
//                             + ex.getMessage());
//         }
     // modify Yanjingming for PR464456 end

        SharedPreferences sharedPre = getSharedPreferences(CountDownChooseRingtoneDialogActivity.TIMER_RPREFERENCES,
                MODE_PRIVATE);
        String ringtonePath = sharedPre.getString(CountDownChooseRingtoneDialogActivity.ALERT_RINGTONE_PATH_KEY,
                CountDownChooseRingtoneDialogActivity.ALERT_SILENT_PATH);

        if (!CountDownChooseRingtoneDialogActivity.ALERT_SILENT_PATH.equals(ringtonePath) && !isRinging) {// modify Yanjingming for PR464456
            Intent intent = new Intent(this, MediaPlayerService.class);
            intent.putExtra(MediaPlayerService.MEDIA_FILE_PATH_EXTRA, ringtonePath);
            intent.putExtra(MediaPlayerService.RUN_TIME_EXTRA, 60);
            intent.putExtra(MediaPlayerService.IS_LOOP_EXTRA,
                    true); //add by Yanjingming for pr541647

            startService(intent);
        }
//        Button okButton = (Button) findViewById(R.id.timer_alert_ok);
//        okButton.requestFocus();
//        okButton.setOnClickListener(new OnClickListener() {
//
//            public void onClick(View v) {
//            	CountDownActivity.setCurrentState(1);
//                closeAlert();
//            }
//        });
        CustomDialog.IAction action=new CustomDialog.IAction() {
            @Override
            public void execution() {
                // TODO Auto-generated method stub
              CountDownActivity.setCurrentState(1);
              closeAlert();
            }
        };
        CustomDialog customDialog=new CustomDialog.Builder(this).create(action);
        customDialog.setCanceledOnTouchOutside(false);
        customDialog.show();
      //add Pr449151 xibin end
    }

    @Override
    public void onAttachedToWindow() {
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
//              | WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD
                | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);
        super.onAttachedToWindow();
    }

    public void onResume() {
        mHandler.removeMessages(CLOSEALERT);// add Yanjingming for PR464456
        super.onResume();
    }

    protected void onPause() {
        if(mPowerManager.isScreenOn()) { // PR826845-modify by xinlei.sheng
            mHandler.sendEmptyMessageDelayed(CLOSEALERT, DELAY_MILLIS);// add Yanjingming for PR464456
        }
        super.onPause();
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
