
package com.jrdcom.timetool.worldclock.activity;

import java.util.Calendar;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.database.ContentObserver;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.Button;
import android.widget.CursorAdapter;
import android.widget.TextView;

import com.android.deskclock.R;
import com.jrdcom.timetool.TimeToolActivity;
import com.jrdcom.timetool.worldclock.provider.TimeZoneInfo;
import com.jrdcom.timetool.worldclock.provider.TimeZones;
import com.jrdcom.timetool.worldclock.view.MyAnalogClock;

public class WorldClockActivity extends Activity {

    private Cursor mCursor;

    private AdapterView<CursorAdapter> mWorldTimeList;

    private WorldTimeAdapter mWorldTimeAdapter;

    //private Locale mLocale_MainPort;
    //private Locale mLocale_MainLand;

    public static final int MENU_ITEM_DELETE = Menu.FIRST;

    public static final int MENU_ITEM_SUMMNER_TIME = Menu.FIRST + 1;

    private View delWorldTimeView;

    private int weekStandard;
    //PR:510457 add by xibin
    private  AlertDialog alertDialog = null;
    private BroadcastReceiver mIntentReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            // PR:488578 add by XIBIN start --integrate world clock widget in Timetool
//            updateTimeAndDateDisplay();
            String action = intent.getAction();
            if(!action.equals(Intent.ACTION_TIMEZONE_CHANGED)){
                action = null;
            }
            updateTimeAndDateDisplay(action);
            
            
            Log.i("Clock", "time change");
            
            // PR:488578 add by XIBIN end
        }
    };

    private Handler mH = new Handler();

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        // get all showed worldClock.
        // PR:488578 add by XIBIN start --integrate world clock widget in Timetool
//        mCursor = TimeZones.getShowedTimeZone(getContentResolver());
//        mWorldTimeAdapter = new WorldTimeAdapter(this, mCursor);
        //mWorldTimeAdapter = new WorldTimeAdapter(this, null);
        // PR:488578 add by XIBIN end
        //mLocale_MainLand = getResources().getConfiguration().locale;
        //mLocale_MainPort = getResources().getConfiguration().locale;
        setContentView(R.layout.worldtime_main);
        initLayout();
    }

    private void initLayout() {
    	mWorldTimeAdapter = new WorldTimeAdapter(this, null);
        mWorldTimeList = (AdapterView<CursorAdapter>) findViewById(R.id.worldtime_list);
        mWorldTimeList.setAdapter(mWorldTimeAdapter);
//        registerForContextMenu(mWorldTimeList);

        View addWorldTimeView = findViewById(R.id.add_worldtime);
        addWorldTimeView.setOnClickListener(new OnClickListener() {

            public void onClick(View v) {
                startChooseWorldTime();
            }
        });
        
        mWorldTimeList.setOnItemLongClickListener(new OnItemLongClickListener() {

            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                // PR860688 by xing.zhao  [Time]Abnoral when delete beijing city  begin
                //not allow to delete the default timezone city like beijing
                if (position==0) {
                    return false;
                }
                // PR860688 by xing.zhao  [Time]Abnoral when delete beijing city  end
                Cursor c = (Cursor) mWorldTimeList.getAdapter().getItem(
                    position);

                final TimeZoneInfo timeZoneInfo = new TimeZoneInfo(c, WorldClockActivity.this);
            
                final Dialog dialog = new Dialog(WorldClockActivity.this);
                dialog.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
                View v = getLayoutInflater().inflate(R.layout.world_activity_long_selected_layout, null);
                TextView title = (TextView) v.findViewById(R.id.tile);
                title.setText(timeZoneInfo.displayName);
                Button delete = (Button)v.findViewById(R.id.delete_button);
                delete.setOnClickListener(new OnClickListener() {
                    
                    @Override
                    public void onClick(View v) {
                        dialog.dismiss();
                        final Dialog check_dialog = new Dialog(WorldClockActivity.this);
                        check_dialog.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
                        View contentView = getLayoutInflater().inflate(R.layout.world_activity_delete_check_dialog_layout, null);
                        TextView cancel_button = (TextView) contentView.findViewById(R.id.cancle_button);
                        cancel_button.setOnClickListener(new OnClickListener() {
                            
                            @Override
                            public void onClick(View v) {
                                check_dialog.dismiss();
                            }
                        });
                        TextView ok_button = (TextView) contentView.findViewById(R.id.ok_button);
                        ok_button.setOnClickListener(new OnClickListener() {
                            
                            @Override
                            public void onClick(View v) {
                                timeZoneInfo.isShow = false;
                                timeZoneInfo.summerTime = TimeZoneInfo.SUMMERTIME_NONE;
                                timeZoneInfo.updateTime = Calendar.getInstance()
                                        .getTimeInMillis();
                                TimeZones.updateTimeZone(
                                        WorldClockActivity.this.getContentResolver(),
                                        timeZoneInfo);
                                // modify by liang.zhang for PR 893959  at 2015-01-12 begin
                                /*PR 601131- Neo Skunkworks - Paul Xu modified - 001 Begin*/
                                /*Prevent null pointer*/
                                /*if(mCursor != null && mCursor.getCount() - 1 > 0){
                                PR 601131- Neo Skunkworks - Paul Xu modified - 001 End
                                    delWorldTimeView.setVisibility(View.VISIBLE);
                                }else{
                                    delWorldTimeView.setVisibility(View.GONE);
                                }*/
                                setButtonVisibleOrNot();
                                Runnable run = new Runnable(){
                                	@Override
                                	public void run() {
                                		delWorldTimeView.postInvalidate();
                                	}
                                };
                                new Thread(run).start();
                                // modify by liang.zhang for PR 893959  at 2015-01-12 end
                                check_dialog.dismiss();
                            }
                        });
                        check_dialog.setContentView(contentView);
                        check_dialog.show();
                        
                    }
                });
                dialog.setContentView(v);
                dialog.show();
                
                return true;
            }
        });
        delWorldTimeView = findViewById(R.id.del_worldtime);
        delWorldTimeView.setOnClickListener(new OnClickListener() {

            public void onClick(View v) {
                startActivity(new Intent(WorldClockActivity.this,
                        DeleteWorldClockActivity.class));
            }
        });
//        setButtonVisibleOrNot(); // PR:488578 add by XIBIN
    }
    private void setButtonVisibleOrNot() {
    	// modify by liang.zhang for PR 889417 at 2015-01-04 begin
        /*PR 601131- Neo Skunkworks - Paul Xu modified - 001 Begin*/
        /*Prevent null pointer*/
//    	if(mCursor != null && mCursor.getCount() > 0){
//        /*PR 601131- Neo Skunkworks - Paul Xu modified - 001 End*/
//        	delWorldTimeView.setVisibility(View.VISIBLE);
//        }else{
//        	delWorldTimeView.setVisibility(View.GONE);
//        }
    	
    	Cursor cursor = TimeZones.getShowedTimeZone(this, false);
        int count = cursor == null ? 0 : cursor.getCount();
        if (count == 0) {
        	delWorldTimeView.setVisibility(View.GONE);
        } else {
        	delWorldTimeView.setVisibility(View.VISIBLE);
        }
    	// modify by liang.zhang for PR 889417 at 2015-01-04 end
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        setContentView(R.layout.worldtime_main);
        initLayout();
        initTimeZoneList();
    }

    private void initTimeZoneList(){
    	// PR:488578 add by XIBIN start --integrate world clock widget in Timetool
        ContentResolver contentResolver = getContentResolver();
        // get all showed worldClock.
// PR 605593 - Neo Skunkworks - Soar Gao - 001 begin 
        if(mCursor==null||mCursor.isClosed()){
        	mCursor = TimeZones.getShowedTimeZone(getApplicationContext(), true);
        }else{
        	mCursor.close();
        	mCursor = TimeZones.getShowedTimeZone(getApplicationContext(), true);
        }
// PR 605593 - Neo Skunkworks - Soar Gao - 001 end 
        mWorldTimeAdapter.changeCursor(mCursor);
        contentResolver.registerContentObserver(
                TimeZoneInfo.Columns.CONTENT_URI, true, mObserver);
        setTitle(getResources().getString(R.string.activity_worldclock));
        setButtonVisibleOrNot();
        
        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_TIME_TICK);
        filter.addAction(Intent.ACTION_TIME_CHANGED);
        filter.addAction(Intent.ACTION_TIMEZONE_CHANGED);
        registerReceiver(mIntentReceiver, filter, null, null);

        updateTimeAndDateDisplay(null);
    }
    
    public void onResume() {
        super.onResume();
        initTimeZoneList();
    }


	public void onPause() {
        super.onPause();
        unregisterReceiver(mIntentReceiver);
        //PR:488578 add by XIBIN
        getContentResolver().unregisterContentObserver(mObserver);
    }

    protected void onDestroy() {
        super.onDestroy();
        // modify by Yanjingming for pr495280 begin
        if(mCursor != null) {
            mCursor.close();
        }
        // modify by Yanjingming for pr495280 end
        // PR:510457 add by xibin start
        if (alertDialog != null && alertDialog.isShowing()) {
            alertDialog.dismiss();
        }
        // PR:510457 add by xibin end
    }

    public void onBackPressed() {
        TimeToolActivity parentActivity = (TimeToolActivity) getParent();

        // whether Timer is running?
        if (!parentActivity.checkIsCountDownRun()) {
            // whether timer is running?
            if (!parentActivity.checkIsTimerRun()) {
                super.onBackPressed();
            }
        }
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v,
            ContextMenuInfo menuInfo) {
        AdapterContextMenuInfo adpterMenuinfo = (AdapterContextMenuInfo) menuInfo;
     // add by Yanjingming for pr459670 begin
        if(adpterMenuinfo == null){
            Log.v("jrdtimetool","adpterMenuinfo == null");
            return;
        }
     // add by Yanjingming for pr459670 end
        Cursor c = (Cursor) mWorldTimeList.getAdapter().getItem(
                adpterMenuinfo.position);

        TimeZoneInfo timeZoneInfo = new TimeZoneInfo(c, this);
        menu.setHeaderTitle(timeZoneInfo.displayName);
        // PR:488578 add by XIBIN start --integrate world clock widget in Timetool
        //if (!timeZoneInfo.isDefaultShow) {
        if (adpterMenuinfo.position != 0) {
            menu.add(0, MENU_ITEM_DELETE, 0, R.string.worldtime_menu_delete);
        }
        //}
        // PR:488578 add by XIBIN end
    }

    private void startChooseWorldTime() {
        startActivity(new Intent(WorldClockActivity.this,
                TimeZoneChooseActivity.class));
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterContextMenuInfo adpterMenuinfo = (AdapterContextMenuInfo) item
                .getMenuInfo();
        Cursor c = (Cursor) mWorldTimeList.getAdapter().getItem(
                adpterMenuinfo.position);
        TimeZoneInfo timeZoneInfo = new TimeZoneInfo(c, this);

        switch (item.getItemId()) {
            case MENU_ITEM_DELETE:
                deleteWorldTime(timeZoneInfo);
                break;
            default:
                return super.onContextItemSelected(item);
        }
        return true;
    }

    private void deleteWorldTime(final TimeZoneInfo timeZoneInfo) {
        Builder alertBuilder = new AlertDialog.Builder(WorldClockActivity.this);

        alertBuilder.setIcon(android.R.drawable.ic_dialog_alert);
        alertBuilder.setTitle(R.string.delete_city);
        alertBuilder.setMessage(R.string.delete_worldtime_message);
        alertBuilder.setNegativeButton(R.string.worldtime_cancel, null);
        alertBuilder.setPositiveButton(R.string.worldtime_ok,
                new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog, int which) {
                        timeZoneInfo.isShow = false;
                        timeZoneInfo.summerTime = TimeZoneInfo.SUMMERTIME_NONE;
                        timeZoneInfo.updateTime = Calendar.getInstance()
                                .getTimeInMillis();
                        TimeZones.updateTimeZone(
                                WorldClockActivity.this.getContentResolver(),
                                timeZoneInfo);
                        /*PR 601131- Neo Skunkworks - Paul Xu modified - 001 Begin*/
                        /*Prevent null pointer*/
                        if(mCursor != null && mCursor.getCount() - 1 > 0){
                        /*PR 601131- Neo Skunkworks - Paul Xu modified - 001 End*/
                        	delWorldTimeView.setVisibility(View.VISIBLE);
                        }else{
                        	delWorldTimeView.setVisibility(View.GONE);
                        }
                        dialog.dismiss();
                    }
                });
        // PR:510457 add by xibin
        alertDialog = alertBuilder.show();
    }
    // PR:488578 add by XIBIN start --integrate world clock widget in Timetool
    private void updateTimeAndDateDisplay(String action) {

        if (action != null && action.equals(Intent.ACTION_TIMEZONE_CHANGED)) {
// PR 605593 - Neo Skunkworks - Soar Gao - 001 begin 
        	if(mCursor==null||mCursor.isClosed()){
        		mCursor = TimeZones.getShowedTimeZone(getApplicationContext(), true);
        	}else{
        		mCursor.close();
        		mCursor = TimeZones.getShowedTimeZone(getApplicationContext(), true);
        	}
// PR 605593 - Neo Skunkworks - Soar Gao - 001 end 
            mWorldTimeAdapter.changeCursor(mCursor);
        }
        /*PR 601131- Neo Skunkworks - Paul Xu modified - 001 Begin*/
        /*Prevent null pointer*/
        if (mCursor != null && mCursor.getCount() > 0) {
        /*PR 601131- Neo Skunkworks - Paul Xu modified - 001 End*/
            ((WorldTimeAdapter) mWorldTimeList.getAdapter())
                    .notifyDataSetChanged();
        }
        
        
        
       
    }
    private class WorldTimeAdapter extends CursorAdapter {

        private final static String M24 = "kk:mm";

        private final static String M12 = "h:mm";

        private final int start_hour = 06;
        private final int end_hour = 18;
        private final int start13_hour = 06;
        private final int end13_hour = 11;

        public WorldTimeAdapter(Context context, Cursor c) {
//            super(context, c);
              super(context, c, false);
        }

        @Override
        public View newView(Context context, Cursor cursor, ViewGroup parent) {
        	return LayoutInflater.from(WorldClockActivity.this).inflate(
                    R.layout.worldtime_listitem, parent, false);
        }

        @Override
        public void bindView(View view, Context context, Cursor cursor) {
            final TimeZoneInfo timeZone = new TimeZoneInfo(cursor,
                    WorldClockActivity.this);
            TextView displayView = (TextView) view
                    .findViewById(R.id.worldtime_display_name);
            displayView.setText(timeZone.displayName);

            // set AnalogClock
            MyAnalogClock analogClock_daytime = (MyAnalogClock) view
                    .findViewById(R.id.worldtime_analogClock_daytime);
            MyAnalogClock analogClock_night = (MyAnalogClock) view
                    .findViewById(R.id.worldtime_analogClock_nighttime);

            // acquire time of current time zone
            Calendar calendar = Calendar.getInstance();
            /*PR 567420- Neo Skunkworks - Paul Xu added - 001 Begin*/
            TimeZones.setTimeZoneOffset(timeZone);
            /*PR 567420- Neo Skunkworks - Paul Xu added - 001 End*/
            long nowMilliseconds = calendar.getTimeInMillis()
                    - calendar.getTimeZone().getOffset(
                            calendar.getTimeInMillis()) + timeZone.offset;
            calendar.setTimeInMillis(nowMilliseconds);

            boolean is24Hour = android.text.format.DateFormat
                    .is24HourFormat(WorldClockActivity.this);
            String timeFormat = is24Hour ? M24 : M12;
            // set time to show
            TextView timeView = (TextView) view
                    .findViewById(R.id.worldtime_time);
            // Date date_time = DateFormat.format(timeFormat, calendar);
            timeView.setText(DateFormat.format(timeFormat, calendar));
            // set AM PM
            TextView ampmView = (TextView) view
                    .findViewById(R.id.worldtime_am_pm);
            String strs = (String) DateFormat.format(timeFormat, calendar);
            String[] dds = new String[] {};
            dds = strs.split(":");
            int dhs = Integer.parseInt(dds[0]);
            if (!is24Hour) {
                ampmView.setVisibility(View.VISIBLE);
//                String[] ampm = new DateFormatSymbols().getAmPmStrings();
//                String amOrPmStr = ampm[calendar.get(Calendar.AM_PM)];
                boolean isMorning = calendar.get(Calendar.AM_PM) == 0;
                if (!isMorning) {
                    if ((start13_hour <= dhs && dhs <= end13_hour)) {
                        analogClock_daytime.setVisibility(View.GONE);
                        analogClock_night.setVisibility(View.VISIBLE);
                        analogClock_night.setTime(calendar);
                    } else {
                        analogClock_night.setVisibility(View.GONE);
                        analogClock_daytime.setVisibility(View.VISIBLE);
                        analogClock_daytime.setTime(calendar);
                    }
                    ampmView.setText(R.string.time_pm);
                } else {
                    if ((start13_hour <= dhs && dhs <= end13_hour)) {
                        analogClock_night.setVisibility(View.GONE);
                        analogClock_daytime.setVisibility(View.VISIBLE);
                        analogClock_daytime.setTime(calendar);
                    } else {
                        analogClock_daytime.setVisibility(View.GONE);
                        analogClock_night.setVisibility(View.VISIBLE);
                        analogClock_night.setTime(calendar);
                    }
                    ampmView.setText(R.string.time_am);
                }
//                ampmView.setText(ampm[calendar.get(Calendar.AM_PM)]);
            } else {
                ampmView.setVisibility(View.GONE);
                if (start_hour <= dhs && dhs < end_hour) {
                    analogClock_night.setVisibility(View.GONE);
                    analogClock_daytime.setVisibility(View.VISIBLE);
                    analogClock_daytime.setTime(calendar);
                } else {
                    analogClock_daytime.setVisibility(View.GONE);
                    analogClock_night.setVisibility(View.VISIBLE);
                    analogClock_night.setTime(calendar);
                }
            }
            // set data
            TextView dateView = (TextView) view
                    .findViewById(R.id.worldtime_date);

            //if (timeZone.isDefaultShow) {
               // weekStandard = calendar.get(Calendar.DAY_OF_WEEK);
            //}
            //acquire current day of week of system timezone
            weekStandard = Calendar.getInstance().get(Calendar.DAY_OF_WEEK);
            if (weekStandard > calendar.get(Calendar.DAY_OF_WEEK)) {
                dateView.setText(R.string.wolrdtime_yestoday);
            } else if (weekStandard < calendar.get(Calendar.DAY_OF_WEEK)) {
            	if(weekStandard == 1 && calendar.get(Calendar.DAY_OF_WEEK) == 7){
            		dateView.setText(R.string.wolrdtime_yestoday);
            	}else{
            		dateView.setText(R.string.wolrdtime_tomorrow);
            	}
            } else {
                dateView.setText(R.string.wolrdtime_today);
            }

        }
    }

    ContentObserver mObserver = new ContentObserver(mH) {
        public void onChange(boolean selfChange) {
// PR 605593 - Neo Skunkworks - Soar Gao - 001 begin 
        	if(mCursor==null||mCursor.isClosed()){
        		mCursor = TimeZones.getShowedTimeZone(getApplicationContext(), true);
        	}else{
        		mCursor.close();
            mCursor = TimeZones.getShowedTimeZone(getApplicationContext(), true);
        	}
// PR 605593 - Neo Skunkworks - Soar Gao - 001 end 
            ((CursorAdapter) mWorldTimeList.getAdapter()).changeCursor(mCursor);
        };
    };
    // PR:488578 add by XIBIN end
}
