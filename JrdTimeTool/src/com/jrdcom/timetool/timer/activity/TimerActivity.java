
package com.jrdcom.timetool.timer.activity;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import com.android.deskclock.R;
import com.jrdcom.timetool.TimeToolActivity;
import com.jrdcom.timetool.countdown.activity.CountDownActivity;
import com.jrdcom.timetool.timer.service.EmptyService;
import com.jrdcom.timetool.timer.service.MediaPlayerServiceForButton;
import com.jrdcom.timetool.timer.service.TimerRunable;
import android.util.Log;

public class TimerActivity extends Activity {

    public static final int STOP_STATE = 1;

    public static final int START_STATE = 2;

    public static final int PAUSE_STATE = 3;

    public static final int FINISH_STATE = 4; //add by Yanjingming for PR526355

    public boolean IsonStop = false;

    public static final String MEDIA_PATH_DEFAULT = Environment
            .getRootDirectory().toString() + "/media/audio/alarms";

    public static final String ALERT_RINGTONE_PATH_KEY = "alert.ringtone.path";
    public static final String TIMER_LOG_LIST = "time.log.list";
    public static final String TIMER_RPREFERENCES = "timetool.timer";
    public static final String TIMER_ABSOLUTETIME = "time.absolutetime";
    public static final String TIMER_RELATIVETIME = "time.relativetime";
    public static final String TIMER_RESUMESTARTTIME = "time.resumeStarttime";
    public static final String TIMER_WATCHTIME = "time.watchTime";
    public static final String TAG = "TimerActivity";

    // public static final String BTN_RINGTONE_PATH =
    // "/system/media/audio/alarms/Alarm_Rooster_02.ogg";

    private int mCurrentState;

    private TimerRunable mTimerRunable;

    private RefreshHandler mRefreshHandler;

    private TextView mAbsoluteView;

    private TextView mRelativeView;

    private RecordAdapter mTimerAdapter;

    //private View mMainPort = null;

    //private View mMainLand = null;

    //private Locale mLocale_MainPort;

    //private Locale mLocale_MainLand;

    private String STOP_STOPWHATCH_ACTION = "stop_Timer_action";

    ListView logListView;
    LinearLayout listFooter;
    //PR:510457 add by xibin
    private  AlertDialog alertDialog = null;
    private BroadcastReceiver mIntentReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(Intent.ACTION_CONFIGURATION_CHANGED)) {
            	// add by junye.li for PR921126 begin
            	if (mCurrentState == START_STATE) {
            		IsonStop = false;
            		stopTimer();
            	}
            	// add by junye.li for PR921126 end
            } else if (intent.getAction().equals(STOP_STOPWHATCH_ACTION)) {
                if (IsonStop) {
                    mCurrentState = STOP_STATE;
                    setButtonVisibility(mCurrentState);

                    mTimerRunable.clean();
                    mAbsoluteView.setText(R.string.Timer_time_format);
                    mRelativeView.setText(R.string.Timer_time_format);

                    // Clear the AnalogClock
                    mRefreshHandler.ClearAnalogClock();
                    mTimerAdapter.clearRecords();
                }
            }
            else {
                if (mTimerRunable.isRun()) {
                    mTimerRunable.resetStartTime();
                }
            }
        }
    };

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.timer_main);

        mCurrentState = STOP_STATE;
        setButtonVisibility(mCurrentState);

        mAbsoluteView = (TextView) findViewById(R.id.Timer_absolute_time);
        mRelativeView = (TextView) findViewById(R.id.Timer_relative_time);
        mTimerRunable = new TimerRunable();
        mRefreshHandler = new RefreshHandler(mTimerRunable, this);

        mTimerAdapter = new RecordAdapter(this);
        mTimerAdapter.setAm(getAssets());

        logListView = (ListView) findViewById(R.id.Timer_loglist);
        logListView.setAdapter(mTimerAdapter);

        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_TIME_CHANGED);
        filter.addAction(Intent.ACTION_CONFIGURATION_CHANGED); // add by junye.li for PR921126
        filter.addAction(STOP_STOPWHATCH_ACTION);
        registerReceiver(mIntentReceiver, filter, null, null);
        List<String> mTimerLoglist = readTimerLog();
         if (mTimerLoglist.size() > 0) {
			mTimerAdapter.addRecordlist(mTimerLoglist);
			setButtonVisibility(PAUSE_STATE);
			mCurrentState = PAUSE_STATE;
			mRefreshHandler.setTimerTextView();
		}
    }

    public void onConfigurationChanged(Configuration newConfig) {

        super.onConfigurationChanged(newConfig);
        setContentView(R.layout.timer_main);
        setButtonVisibility(mCurrentState);

        mAbsoluteView = (TextView) findViewById(R.id.Timer_absolute_time);
        mRelativeView = (TextView) findViewById(R.id.Timer_relative_time);

        if (mRefreshHandler.isStart()) {
            mRefreshHandler.stop();
            mRefreshHandler = new RefreshHandler(mTimerRunable, this);
            mRefreshHandler.start();
        } else {
            mRefreshHandler = new RefreshHandler(mTimerRunable, this);
            long[] Timesbuffer = mTimerRunable.gettimes();
            mRefreshHandler.updateAnalogClock(Timesbuffer[0]);
            if (mCurrentState == STOP_STATE) {
                mRefreshHandler.ClearTextView();
            } else {
                mRefreshHandler.RefreshTextView();
            }

        }

        logListView = (ListView) findViewById(R.id.Timer_loglist);
        logListView.setDivider(null);
        logListView.setAdapter(mTimerAdapter);

    }

    public void onStart() {
        super.onStart();
    }

    public void onResume() {
        super.onResume();

        if (mTimerRunable.isRun()) {
            if (!mRefreshHandler.isStart()) {
                mRefreshHandler.start();
            }
        }

        IsonStop = false;
    }

    @Override
    protected void onPause() {
        super.onPause();

        if (mRefreshHandler.isStart()) {
            mRefreshHandler.stop();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        IsonStop = true;
    }

    public void onBackPressed() {
        TimeToolActivity parentActivity = (TimeToolActivity) getParent();

        if (!mTimerRunable.isRun()) {

            if (!parentActivity.checkIsCountDownRun()) {
                super.onBackPressed();
            }
            return;
        } else

            showAlert();
    }

    /**
     * Alert users exit or not
     */
    private void showAlert() {
        TimeToolActivity parentActivity = (TimeToolActivity) getParent();
        final CountDownActivity CountDownActivity = (CountDownActivity) parentActivity
                .getLocalActivityManager().getActivity(
                        parentActivity.TIMER_TAB_TAG);
        Builder alertBuilder = new AlertDialog.Builder(this);
        alertBuilder.setIcon(android.R.drawable.ic_dialog_alert);
        alertBuilder.setTitle(R.string.Timer_warning);
        alertBuilder.setMessage(R.string.Timer_exit_warning);
        alertBuilder.setNegativeButton(R.string.Timer_cancel, null);
        alertBuilder.setPositiveButton(R.string.Timer_ok,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        stopTimer();

                        // if the BackgroundCountDownService is running,Stop it.
                        if (CountDownActivity != null) {
                            if (CountDownActivity.isRunning()) {
                                CountDownActivity.cancelTimer();
                            }
                        }

                        finish();
                        dialog.dismiss();
                    }
                });
        // PR:510457 add by xibin
        alertDialog = alertBuilder.show();
    }

    public boolean isRunning() {
        return mTimerRunable.isRun();
    }

    protected void onDestroy() {
        super.onDestroy();
        stopService(new Intent(this, EmptyService.class));
        unregisterReceiver(mIntentReceiver);
        // PR:510457 add by xibin start
        if (alertDialog != null && alertDialog.isShowing()) {
            alertDialog.dismiss();
        }
        // PR:510457 add by xibin end
        writeTimerLog();
    }

    public void onButtonClick(View view) {
        switch (view.getId()) {
            case R.id.Timer_start:

                mCurrentState = START_STATE;
                setButtonVisibility(mCurrentState);

                mTimerRunable.start();
                mRefreshHandler.start();

                // start a empty service to prevent to be killed by am
                startService(new Intent(this, EmptyService.class));
                break;
            case R.id.Timer_stop:
                stopTimer();
                break;
            case R.id.Timer_resume:
                //added by wei.li for PR503576 begin
                if (mCurrentState != PAUSE_STATE) {
                    return;
                }
                //added by wei.li for PR503576 end
                mCurrentState = START_STATE;
                setButtonVisibility(mCurrentState);

                mTimerRunable.resume();
                mRefreshHandler.start();
                // start a empty service to prevent to be killed by am
                startService(new Intent(this, EmptyService.class));
                break;
            case R.id.Timer_record:
                //added by wei.li for PR503572 begin
                if (mCurrentState != START_STATE) {
                    return;
                }
                //added by wei.li for PR503572 end
                long[] times = mTimerRunable.record();
                String absoluteTime = RefreshHandler.convertTime(times[0]);
                String relativeTime = RefreshHandler.convertTime(times[1]);

                mTimerAdapter.addRecords(absoluteTime + "/" + relativeTime);
                break;
            case R.id.Timer_clean:
                mCurrentState = STOP_STATE;
                setButtonVisibility(mCurrentState);

                mTimerRunable.clean();
                // PR620305-haiying.he-start
                String Timer_time_format = "00 : 00 . 00";
                String mLocalLanguage = Locale.getDefault().getLanguage();
                if ("ar".equals(mLocalLanguage) || "fa".equals(mLocalLanguage)
                        || "iw".equals(mLocalLanguage)) {
                    mAbsoluteView.setText("\u202D" + Timer_time_format + "\u202C");
                    mRelativeView.setText("\u202D" + Timer_time_format + "\u202C");
                } else {
                    mAbsoluteView.setText(Timer_time_format);
                    mRelativeView.setText(Timer_time_format);
                }
                // PR620305-haiying.he-end

                // Clear the AnalogClock
                mRefreshHandler.ClearAnalogClock();
                mTimerAdapter.clearRecords();

                break;
            default:
                break;
        }
    }

    //modify by Yanjingming for PR526355 begin
    public void stopTimer(boolean isFinish) {
        if(isFinish){
            mCurrentState = FINISH_STATE;
        }else{
            mCurrentState = PAUSE_STATE;
        }

        setButtonVisibility(mCurrentState);

        mTimerRunable.stop();
        mRefreshHandler.stop();

        stopService(new Intent(this, EmptyService.class));
    }

    public void stopTimer() {
        stopTimer(false);
    }
    //modify by Yanjingming for PR526355 end

    public void startButtonRing() {

        Intent intent = new Intent(TimerActivity.this,
                MediaPlayerServiceForButton.class);

        startService(intent);
    }

    private void setButtonVisibility(int state) {
        Button startBtn = (Button) findViewById(R.id.Timer_start);
        Button stopBtn = (Button) findViewById(R.id.Timer_stop);
        Button resumeBtn = (Button) findViewById(R.id.Timer_resume);
        Button recordBtn = (Button) findViewById(R.id.Timer_record);
        Button cleanBtn = (Button) findViewById(R.id.Timer_clean);
        switch (state) {
            case STOP_STATE:
                startBtn.setVisibility(View.VISIBLE);
                stopBtn.setVisibility(View.GONE);
                resumeBtn.setVisibility(View.GONE);

                recordBtn.setVisibility(View.VISIBLE);
                recordBtn.setEnabled(false);
                cleanBtn.setVisibility(View.GONE);
                break;
            case START_STATE:
                startBtn.setVisibility(View.GONE);
                stopBtn.setVisibility(View.VISIBLE);
                resumeBtn.setVisibility(View.GONE);

                recordBtn.setVisibility(View.VISIBLE);
                recordBtn.setEnabled(true);
                cleanBtn.setVisibility(View.GONE);
                break;
            case PAUSE_STATE:
                startBtn.setVisibility(View.GONE);
                stopBtn.setVisibility(View.GONE);
                resumeBtn.setVisibility(View.VISIBLE);
                resumeBtn.setEnabled(true);// PR -587339 - Neo Skunworks - Soar Gao , add -001
                recordBtn.setVisibility(View.GONE);
                cleanBtn.setVisibility(View.VISIBLE);
                break;
            //add by Yanjingming for PR526355 begin
            // M : State when absoluteTime is the biggest value
            case FINISH_STATE:
                startBtn.setVisibility(View.GONE);
                stopBtn.setVisibility(View.GONE);
                resumeBtn.setVisibility(View.VISIBLE);
                resumeBtn.setEnabled(false);
                recordBtn.setVisibility(View.GONE);
                cleanBtn.setVisibility(View.VISIBLE);
                break;
            //add by Yanjingming for PR526355 end
            default:
                startBtn.setVisibility(View.VISIBLE);
                stopBtn.setVisibility(View.GONE);
                resumeBtn.setVisibility(View.GONE);

                recordBtn.setVisibility(View.VISIBLE);
                recordBtn.setTextColor(getResources().getColor(R.color.countdown_grey));
                recordBtn.setEnabled(false);
                cleanBtn.setVisibility(View.GONE);
                break;
        }
    }
    public void writeTimerLog() {
    	SharedPreferences sharedPre = getSharedPreferences(TIMER_RPREFERENCES,MODE_PRIVATE);
    	String mListStr = mTimerAdapter.getRecordlist().toString().replaceAll("\\[", "").replaceAll("\\]", "");
    	SharedPreferences.Editor editor = sharedPre.edit();
    	editor.putString(TIMER_LOG_LIST,mListStr);
    	editor.putLong(TIMER_ABSOLUTETIME,mTimerRunable.getAbsoluteTime());
    	editor.putLong(TIMER_RELATIVETIME,mTimerRunable.getRelativeTime());
    	editor.putLong(TIMER_RESUMESTARTTIME,mTimerRunable.getResumeStartTime());
    	editor.putLong(TIMER_WATCHTIME,mTimerRunable.getwatchTime());
    	editor.commit();
    	Log.e(TAG, "getAbsoluteTime()"+mTimerRunable.getAbsoluteTime()
    			+" getRelativeTime()()"+mTimerRunable.getRelativeTime()
    			+" getResumeStartTime()"+mTimerRunable.getResumeStartTime()
    			+" getwatchTime()"+mTimerRunable.getwatchTime());
    }
    public List<String> readTimerLog() {
    	SharedPreferences sharedPre = getSharedPreferences(TIMER_RPREFERENCES,MODE_PRIVATE);
    	String timerLogString = sharedPre.getString(TIMER_LOG_LIST, "");
    	long mAbsoluteTime = sharedPre.getLong(TIMER_ABSOLUTETIME, 0);
    	long mRelativeTime = sharedPre.getLong(TIMER_RELATIVETIME, 0);
    	long mResumeStartTime = sharedPre.getLong(TIMER_RESUMESTARTTIME, 0);
    	long mwatchTime = sharedPre.getLong(TIMER_WATCHTIME, 0);
    	mTimerRunable.setAbsoluteTime(mAbsoluteTime);
    	mTimerRunable.setRelativeTime(mRelativeTime);
    	mTimerRunable.setResumeStartTime(mResumeStartTime);
    	mTimerRunable.setwatchTime(mwatchTime);
    	List<String> arrayList = new ArrayList<String>();
    	if (timerLogString.length() == 0) {
    		return arrayList;
		}
    	String[] mStrList = timerLogString.split(",");
    	arrayList = new ArrayList(Arrays.asList(mStrList));
    	return arrayList;

    }
}
