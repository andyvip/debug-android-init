package com.jrdcom.timetool;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.LocalActivityManager;
import android.app.TabActivity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TabHost;
import android.widget.TabHost.OnTabChangeListener;
import android.widget.TabHost.TabSpec;
import android.widget.TabWidget;
import android.widget.TextView;
import android.util.Log;
import com.android.deskclock.R;
import com.jrdcom.timetool.alarm.activity.AlarmActivity;
import com.jrdcom.timetool.alarm.activity.HandleSetAlarm;
import com.jrdcom.timetool.alarm.activity.SetAlarm;
import com.jrdcom.timetool.alarm.provider.Alarms;
import com.jrdcom.timetool.countdown.activity.CountDownActivity;
import com.jrdcom.timetool.timer.activity.TimerActivity;
import com.jrdcom.timetool.worldclock.activity.WorldClockActivity;
import static android.provider.AlarmClock.EXTRA_SKIP_UI;


public class TimeToolActivity extends TabActivity {

	public static final String WORLD_CLOCK_TAB_TAG = "worldclock";

	public static final String ALARM_TAB_TAG = "alarm";

	public static final String Timer_TAB_TAG = "Timer";

	public static final String TIMER_TAB_TAG = "timer";

	private TabHost mTabHost;
	private TabWidget tabWidget;
	LinearLayout myTab0;
	TextView myText0;
	ImageView img0;
	LinearLayout myTab1;
	TextView myText1;
	ImageView img1;
	LinearLayout myTab2;
	TextView myText2;
	ImageView img2;
	LinearLayout myTab3;
	TextView myText3;
	ImageView img3;
	
	
	Activity TimerActivity, worldclockActivity, alarmActivity,
    CountDownActivity;

    //PR:510457 add by xibin start
    private AlertDialog alertDialogIc_1 = null;
    private AlertDialog alertDialogIc_2 = null;
    //PR:510457 add by xibin end
	@Override
	public void onCreate(Bundle savedInstanceState) {
	    requestWindowFeature(Window.FEATURE_NO_TITLE);
		super.onCreate(savedInstanceState);
		setContentView(R.layout.tab_content);
		mTabHost = (TabHost) findViewById(android.R.id.tabhost);
		tabWidget = mTabHost.getTabWidget();
		initChildActivity();
		mTabHost.setOnTabChangedListener(new OnTabChangeListener() {

			@Override
			public void onTabChanged(String arg0) {
				setTabImage();
				setTabBackground();
			}
		});
		/* modify by xinlei.sheng for PR856818 begin*/
		//mTabHost.setCurrentTab(0);
		/* modify by xinlei.sheng for PR856818 end*/
		initTabResources();
	}



	private void initTabResources() {
		setTabImage();
		setTabBackground();
	}

	private void setTabImage() {

		switch (mTabHost.getCurrentTab()) {
		case 0:
			img0.setImageDrawable(getResources().getDrawable(
					R.drawable.world_clock_press));
			img1.setImageDrawable(getResources().getDrawable(R.drawable.alarm));
			img2.setImageDrawable(getResources().getDrawable(R.drawable.timer));
			img3.setImageDrawable(getResources().getDrawable(
					R.drawable.countdown));
			break;
		case 1:
			img0.setImageDrawable(getResources().getDrawable(
					R.drawable.world_clock));
			img1.setImageDrawable(getResources().getDrawable(
					R.drawable.alarm_press));
			img2.setImageDrawable(getResources().getDrawable(R.drawable.timer));
			img3.setImageDrawable(getResources().getDrawable(
					R.drawable.countdown));
			break;
		case 2:
			img0.setImageDrawable(getResources().getDrawable(
					R.drawable.world_clock));
			img1.setImageDrawable(getResources().getDrawable(R.drawable.alarm));
			img2.setImageDrawable(getResources().getDrawable(
					R.drawable.timer_press));
			img3.setImageDrawable(getResources().getDrawable(
					R.drawable.countdown));
			break;
		case 3:
			img0.setImageDrawable(getResources().getDrawable(
					R.drawable.world_clock));
			img1.setImageDrawable(getResources().getDrawable(R.drawable.alarm));
			img2.setImageDrawable(getResources().getDrawable(R.drawable.timer));
			img3.setImageDrawable(getResources().getDrawable(
					R.drawable.countdown_press));
			break;
		}
	}
    
	private void setTabBackground() {
		for (int i = 0; i < tabWidget.getChildCount(); i++) {
			View v = tabWidget.getChildAt(i);
		/*	TextView tv = (TextView) tabWidget.getChildAt(i).findViewById(
					R.id.tab_label);*/
			View line = v.findViewById(R.id.tab_line);
			if (mTabHost.getCurrentTab() == i) {
				line.setVisibility(View.VISIBLE);
			} else {
			    line.setVisibility(View.INVISIBLE);
			}
		}
	}
    // PR:488578 add by XIBIN start --integrate world clock widget in Timetool
    @Override
    protected void onNewIntent(Intent intent) {
        setCurrentTab(intent);
        super.onNewIntent(intent);
    }

    private void setCurrentTab(Intent intent) {
        if (intent == null)
            return;
        String worldClockClassName = "com.android.deskclock.WorldClock";
        String alarmClassName = "com.android.deskclock.AlarmClock";
        String currentClassName = intent.getComponent().getClassName();
        String tagName = null;
        if (worldClockClassName.equalsIgnoreCase(currentClassName)) {
            tagName = WORLD_CLOCK_TAB_TAG;
        }
        if (alarmClassName.equalsIgnoreCase(currentClassName)) {
            tagName = ALARM_TAB_TAG;
        }

        if (tagName != null) {
            mTabHost.setCurrentTabByTag(tagName);
        }
    }
    // PR:488578 add by XIBIN end
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);

		// dispense ConfigurationChanged to the subActivities
		LocalActivityManager activityManager = getLocalActivityManager();
		TimerActivity = activityManager.getActivity(Timer_TAB_TAG);
		if (TimerActivity != null) {
			TimerActivity.onConfigurationChanged(newConfig);
		}

		worldclockActivity = activityManager.getActivity(WORLD_CLOCK_TAB_TAG);
		if (worldclockActivity != null) {
			worldclockActivity.onConfigurationChanged(newConfig);
		}

		alarmActivity = activityManager.getActivity(ALARM_TAB_TAG);
		if (alarmActivity != null) {
			alarmActivity.onConfigurationChanged(newConfig);
		}

		CountDownActivity = activityManager.getActivity(TIMER_TAB_TAG);
		if (CountDownActivity != null) {
			CountDownActivity.onConfigurationChanged(newConfig);
		}
	}

	private void initChildActivity() {
		myTab0 = (LinearLayout) LayoutInflater.from(this).inflate(R.layout.tab_widget, null);
		
		View line = myTab0.findViewById(R.id.tab_line);
		line.setVisibility(View.VISIBLE);
		MyLog.i("height->" + myTab0.getHeight());
		img0 = (ImageView)myTab0.findViewById(R.id.tab_image);
		img0.setImageDrawable(getResources().getDrawable(
					R.drawable.world_clock_press));
		
		TabSpec tabSpec = mTabHost.newTabSpec(WORLD_CLOCK_TAB_TAG);
		tabSpec.setIndicator(myTab0);
		tabSpec.setContent(new Intent(this, WorldClockActivity.class));
		mTabHost.addTab(tabSpec);

		myTab1 = (LinearLayout) LayoutInflater.from(this).inflate(R.layout.tab_widget, null);
	/*	myText1 =(TextView)myTab1.findViewById(R.id.tab_label);
		myText1.setText(R.string.activity_alarm);
		myText1.setTextColor(R.color.tab_unselected);*/
		img1 = (ImageView)myTab1.findViewById(R.id.tab_image);
		img1.setImageDrawable(getResources().getDrawable(
					R.drawable.alarm));
		
		tabSpec = mTabHost.newTabSpec(ALARM_TAB_TAG);
		tabSpec.setIndicator(myTab1);

		tabSpec.setContent(new Intent(this, AlarmActivity.class));

		mTabHost.addTab(tabSpec);

		myTab2 = (LinearLayout) LayoutInflater.from(this).inflate(R.layout.tab_widget, null);
	/*	myText2 =(TextView)myTab2.findViewById(R.id.tab_label);
		myText2.setText(R.string.activity_Timer);
		myText2.setTextColor(R.color.tab_unselected);*/
		img2 = (ImageView)myTab2.findViewById(R.id.tab_image);
		img2.setImageDrawable(getResources().getDrawable(
					R.drawable.timer));
		
		tabSpec = mTabHost.newTabSpec(Timer_TAB_TAG);
		tabSpec.setIndicator(myTab2);

		tabSpec.setContent(new Intent(this, TimerActivity.class));
		mTabHost.addTab(tabSpec);

		myTab3 = (LinearLayout) LayoutInflater.from(this).inflate(R.layout.tab_widget, null);
		/*myText3 =(TextView)myTab3.findViewById(R.id.tab_label);
		myText3.setText(R.string.activity_timer);
		myText3.setTextColor(R.color.tab_unselected);*/
		img3 = (ImageView)myTab3.findViewById(R.id.tab_image);
		img3.setImageDrawable(getResources().getDrawable(
					R.drawable.countdown));
		
		tabSpec = mTabHost.newTabSpec(TIMER_TAB_TAG);
		tabSpec.setIndicator(myTab3);
		// tabSpec.set
		tabSpec.setContent(new Intent(this, CountDownActivity.class));
		mTabHost.addTab(tabSpec);
		/* modify by xinlei.sheng for PR856818 begin*/
		mTabHost.setCurrentTabByTag(WORLD_CLOCK_TAB_TAG);
		/* modify by xinlei.sheng for PR856818 end*/

		Intent intent = getIntent();		
		/*PR 653807 - Neo Skunkworks - Paul Xu added - 001 Begin*/
//		if(intent != null && intent.getAction() != null){
//		}
//		if(intent != null && intent.getAction() != null 
//				&&intent.getAction().equals("android.intent.action.SHOW_ALARMS")){
//			mTabHost.setCurrentTabByTag(ALARM_TAB_TAG);
//		}else if(intent != null && intent.getAction() != null
//				&& intent.getAction().equals("android.intent.action.SET_TIMER")){
//			mTabHost.setCurrentTabByTag(TIMER_TAB_TAG);
//			boolean skipUi = intent.getBooleanExtra(EXTRA_SKIP_UI, false);
//		}
		/*PR 653807 - Neo Skunkworks - Paul Xu added - 001 Begin*/
		boolean deskclock = intent.hasExtra("DeskClock");
		if (deskclock) {
			mTabHost.setCurrentTabByTag(ALARM_TAB_TAG);
		}

		/*PR 638550- Neo Skunkworks - Paul Xu added - 001 Begin*/
        /*Go into alarm tab tag*/
        if(HandleSetAlarm.mSetAlarm){
        	HandleSetAlarm.mSetAlarm = false;
            mTabHost.setCurrentTabByTag(ALARM_TAB_TAG);
            /*PR 685347- Neo Skunkworks - Paul Xu modified - 001 Begin*/
            /*
            startActivity(new Intent(this, SetAlarm.class));
            */
            startSetAlarmActivity();
            /*PR 685347- Neo Skunkworks - Paul Xu modified - 001 End*/
            
        }

        if(HandleSetAlarm.mShowAlarm){
        	HandleSetAlarm.mShowAlarm = false;
            mTabHost.setCurrentTabByTag(ALARM_TAB_TAG);
        }

        if(HandleSetAlarm.mSetTimer){
        	HandleSetAlarm.mSetTimer = false;
            mTabHost.setCurrentTabByTag(TIMER_TAB_TAG);
        }
        /*PR 638550- Neo Skunkworks - Paul Xu added - 001 End*/
	}
	
    /*PR 685347- Neo Skunkworks - Paul Xu added - 001 Begin*/
    /**
     * start SetAlarm Activity.
     *
     * @param  null
     * @return null .
    */
    private void startSetAlarmActivity(){
        Intent intent = new Intent();
        intent.setClass(this, SetAlarm.class);
        intent.putExtra(Alarms.EXTRA_SET_ALARM, true);
        startActivity(intent);
    }
    /*PR 685347- Neo Skunkworks - Paul Xu added - 001 End*/

	/**
	 * Estimate whether the timer is running or not
	 * 
	 * @return
	 */
	public boolean checkIsTimerRun() {
		final TimerActivity TimerActivity = (TimerActivity) getLocalActivityManager()
				.getActivity(Timer_TAB_TAG);
		final CountDownActivity CountDownActivity = (CountDownActivity) getLocalActivityManager()
				.getActivity(TIMER_TAB_TAG);
		if (TimerActivity == null) {
			return false;
		}

		if (TimerActivity.isRunning()) {

			Builder alertBuilder = new AlertDialog.Builder(this);
			alertBuilder.setIcon(android.R.drawable.ic_dialog_alert);
			alertBuilder.setTitle(R.string.Timer_warning);
			alertBuilder.setMessage(R.string.Timer_exit_warning);
			alertBuilder.setNegativeButton(R.string.Timer_cancel, null);
			alertBuilder.setPositiveButton(R.string.Timer_ok,
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int which) {
							TimerActivity.stopTimer();

							// if timer is running ,stop it.
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
            alertDialogIc_1 = alertBuilder.show();

			return true;

		} else {
			return false;
		}
	}

	/**
	 * Estimate whether the timer is running or not
	 * 
	 * @return
	 */

	public boolean checkIsCountDownRun() {
		final CountDownActivity CountDownActivity = (CountDownActivity) getLocalActivityManager()
				.getActivity(TIMER_TAB_TAG);
		final TimerActivity TimerActivity = (TimerActivity) getLocalActivityManager()
				.getActivity(Timer_TAB_TAG);
		if (CountDownActivity == null) {
			return false;
		}

		if (CountDownActivity.isRunning()) {

			Builder alertBuilder = new AlertDialog.Builder(this);
			alertBuilder.setIcon(android.R.drawable.ic_dialog_alert);
			alertBuilder.setTitle(R.string.Timer_warning);
			alertBuilder.setMessage(R.string.timer_exit_warning);
			alertBuilder.setNegativeButton(R.string.timer_dialog_cancel, null);
			alertBuilder.setPositiveButton(R.string.timer_dialog_ok,
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int which) {
							CountDownActivity.cancelTimer();

							// if Timer is not null and is running,stop it.
							if (TimerActivity != null) {
								if (TimerActivity.isRunning()) {
									TimerActivity.stopTimer();
								}
							}
							finish();
							dialog.dismiss();
						}
					});
            // PR:510457 add by xibin
            alertDialogIc_2 = alertBuilder.show();
			return true;
		} else {
			return false;
		}
	}

@Override
protected void onDestroy() {
    // TODO Auto-generated method stub
    super.onDestroy();
    //PR:510457 add by xibin start
        if (alertDialogIc_1 != null && alertDialogIc_1.isShowing()) {
            alertDialogIc_1.dismiss();
        }
        if (alertDialogIc_2 != null && alertDialogIc_2.isShowing()) {
            alertDialogIc_2.dismiss();
        }
        //PR:510457 add by xibin end
}
}
