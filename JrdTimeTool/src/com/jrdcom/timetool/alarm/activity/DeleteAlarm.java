
package com.jrdcom.timetool.alarm.activity;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.database.Cursor;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CursorAdapter;
import android.widget.TextView;
import android.widget.AbsListView.LayoutParams;

import com.android.deskclock.R;
import com.jrdcom.timetool.MyLog;
import com.jrdcom.timetool.alarm.provider.Alarm;
import com.jrdcom.timetool.alarm.provider.Alarms;
import com.jrdcom.timetool.alarm.view.DigitalClock;
import com.jrdcom.timetool.worldclock.activity.DeleteWorldClockActivity;
import com.jrdcom.timetool.worldclock.activity.WorldClockActivity;
import com.jrdcom.timetool.worldclock.provider.TimeZoneInfo;
import com.jrdcom.timetool.worldclock.provider.TimeZones;

public class DeleteAlarm extends Activity implements AdapterView.OnItemClickListener {

    private static String TAG = "JrdTimeTool";// add by Yanjingming for pr533248

    private Cursor mCursor;

    private AdapterView<CursorAdapter> mAlarmList;

    private AlarmAdapter mAlarmAdapter;

    private int smallHeight = 68;
    private int largeHeight = 72;

    private Map<Integer, Boolean> mSelectedMap = new HashMap<Integer, Boolean>();

    /* PR 587249- Neo Skunkworks - Paul Xu added - 001 Begin */
    private boolean mIsScreenOff = false;
    /* PR 587249- Neo Skunkworks - Paul Xu added - 001 End */

    // PR:510457 add by xibin
    private AlertDialog alertDialog = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.alarm_delete_list);

        mCursor = Alarms.getAlarmsCursor(getContentResolver());
        mAlarmAdapter = new AlarmAdapter(this, mCursor);
        initLayout();
    }

    @SuppressWarnings("unchecked")
    private void initLayout() {

        mAlarmList = (AdapterView<CursorAdapter>) findViewById(R.id.alarm_list);
        mAlarmList.setAdapter(mAlarmAdapter);
        mAlarmList.setOnItemClickListener(this);
        View bk_btn = findViewById(R.id.bk_btn);
        bk_btn.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                finish();
            }

        });
        Button deleteButton = (Button) findViewById(R.id.alarm_delete);
        deleteButton.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                deleteAlarm();
            }
        });

        Button cancelButton = (Button) findViewById(R.id.alarm_cancel);
        cancelButton.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                finish();
            }
        });

    }

    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        setContentView(R.layout.alarm_delete_list);
        initLayout();
    }

    /* PR 587249- Neo Skunkworks - Paul Xu added - 001 Begin */
    void registerMyReceiver() {
        IntentFilter mFilter = new IntentFilter();
        mFilter.addAction(Intent.ACTION_SCREEN_OFF);
        /* PR 659207 - Neo Skunkworks - Paul Xu added - 001 Begin */
        mFilter.addAction(TelephonyManager.ACTION_PHONE_STATE_CHANGED);
        /* PR 659207 - Neo Skunkworks - Paul Xu added - 001 End */
        // PR695264-Neo Skunkworks-kehao.wei-001 add begin
        mFilter.addAction(Alarms.ALARM_ALERT_ACTION);
        // PR695264-Neo Skunkworks-kehao.wei-001 add end
        registerReceiver(screenOffReceiver, mFilter);
    }

    void unregisterMyReceiver() {
        if (screenOffReceiver != null)
            unregisterReceiver(screenOffReceiver);
    }

    private BroadcastReceiver screenOffReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            // TODO Auto-generated method stub
            String action = intent.getAction();
            /* PR 659207 - Neo Skunkworks - Paul Xu modified - 001 Begin */
            if (action.equals(Intent.ACTION_SCREEN_OFF)
                    || action.equals(TelephonyManager.ACTION_PHONE_STATE_CHANGED)
                    || action.equals(Alarms.ALARM_ALERT_ACTION)) {// PR695264-Neo
                                                                  // Skunkworks-kehao.wei-001
                                                                  // add
                mIsScreenOff = true;
            }
            /* PR 659207 - Neo Skunkworks - Paul Xu modified - 001 End */
        }

    };

    /* PR 587249- Neo Skunkworks - Paul Xu added - 001 End */

    public void onResume() {
        super.onResume();
        MyLog.debug("onResume of Delete is called.--", DeleteWorldClockActivity.class);
        /* PR 587249- Neo Skunkworks - Paul Xu added - 001 Begin */
        registerMyReceiver();
        if (mIsScreenOff) {
            mIsScreenOff = false;
            return;
        }
        /* PR 587249- Neo Skunkworks - Paul Xu added - 001 End */
        // initialize select map.
        // PR711198-modify-peng.xie-begin
        if (mSelectedMap != null && mSelectedMap.size() == 0) {
            mSelectedMap.clear();
            int count = mAlarmList.getCount();
            for (int i = 0; i < count; i++) {
                mSelectedMap.put(i, false);
            }
            updateDeleteAlarmDisplay();
        }
        // PR711198-modify-peng.xie-end
    }

    public void onPause() {
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        /* PR 587249- Neo Skunkworks - Paul Xu added - 001 Begin */
        unregisterMyReceiver();
        /* PR 587249- Neo Skunkworks - Paul Xu added - 001 End */
        /* PR 570245- Neo Skunkworks - Paul Xu modified - 001 Begin */
        /*
         * mCursor.close();
         */
        if (mCursor != null) {
            mCursor.close();
        }
        /* PR 570245- Neo Skunkworks - Paul Xu modified - 001 End */
        mSelectedMap.clear();
        // PR:510457 add by xibin start
        if (alertDialog != null && alertDialog.isShowing()) {
            alertDialog.dismiss();
        }
        // PR:510457 add by xibin end
    }

    public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
        Boolean isSelected = mSelectedMap.get(position);
        if (isSelected == null) {
            return;
        }
        mSelectedMap.put(position, !isSelected);
        updateDeleteAlarmDisplay();
    }

    private void deleteAlarm() {

        final Dialog check_dialog = new Dialog(this);
        check_dialog.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        View contentView = getLayoutInflater().inflate(
                R.layout.world_activity_delete_check_dialog_layout, null);
        TextView cancel_button = (TextView) contentView.findViewById(R.id.cancle_button);
        TextView message= (TextView) contentView.findViewById(R.id.message);
        TextView title = (TextView) contentView.findViewById(R.id.tile);
        message.setText(R.string.delete_alarm_message);
        title.setText(R.string.delete_alarm);
        
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

                int position = mAlarmList.getCount() - 1;
                while (position >= 0) {
                    Cursor c = (Cursor) mAlarmList.getAdapter().getItem(position);
                    Alarm curAlarm = new Alarm(c);
                    int curId = curAlarm.id;
                    if (mSelectedMap.get(position)) {
                        Alarms.deleteAlarm(DeleteAlarm.this, curId);
                        /* PR 616192- Neo Skunkworks - Paul Xu added - 001 Begin */
                        if (Alarms.getPlayingAlarmId(DeleteAlarm.this) == curId) {
                            sendAlarmDeletedBroadcast();
                        }
                        /* PR 616192- Neo Skunkworks - Paul Xu added - 001 End */
                    }
                    position--;
                }
                check_dialog.dismiss();
                finish();

            }
        });
        check_dialog.setContentView(contentView);
        check_dialog.show();

//        Builder alertBuilder = new AlertDialog.Builder(this);
//        alertBuilder.setIcon(android.R.drawable.ic_dialog_alert);
//        alertBuilder.setTitle(R.string.delete_city);
//        // Added by xiaxia.yao for PR:423355 begin
//        // alertBuilder.setMessage(R.string.delete_worldtime_message);
//        alertBuilder.setMessage(R.string.delete_alarm_message);
//        // Added by xiaxia.yao for PR:423355 end
//        alertBuilder.setNegativeButton(R.string.worldtime_cancel, null);
//        alertBuilder.setPositiveButton(R.string.worldtime_ok,
//                new DialogInterface.OnClickListener() {
//
//                    public void onClick(DialogInterface dialog, int which) {
//                        int position = mAlarmList.getCount() - 1;
//                        while (position >= 0) {
//                            Cursor c = (Cursor) mAlarmList.getAdapter().getItem(position);
//                            Alarm curAlarm = new Alarm(c);
//                            int curId = curAlarm.id;
//                            if (mSelectedMap.get(position)) {
//                                Alarms.deleteAlarm(DeleteAlarm.this, curId);
//                                /*
//                                 * PR 616192- Neo Skunkworks - Paul Xu added -
//                                 * 001 Begin
//                                 */
//                                if (Alarms.getPlayingAlarmId(DeleteAlarm.this) == curId) {
//                                    sendAlarmDeletedBroadcast();
//                                }
//                                /*
//                                 * PR 616192- Neo Skunkworks - Paul Xu added -
//                                 * 001 End
//                                 */
//                            }
//                            position--;
//                        }
//                        dialog.dismiss();
//                        finish();
//                    }
//                });
//        // PR:510457 add by xibin
//        alertDialog = alertBuilder.show();
    }

    /* PR 616192- Neo Skunkworks - Paul Xu added - 001 Begin */
    private void sendAlarmDeletedBroadcast() {
        Intent alarmDeleted = new Intent(Alarms.ALARM_DELETED);
        sendBroadcast(alarmDeleted);
    }

    /* PR 616192- Neo Skunkworks - Paul Xu added - 001 End */

    private void updateDeleteAlarmDisplay() {
        /* PR 570245- Neo Skunkworks - Paul Xu modified - 001 Begin */
        /*
         * if (mCursor.getCount() > 0) {
         */
        if (mCursor != null && mCursor.getCount() > 0) {
            /* PR 570245- Neo Skunkworks - Paul Xu modified - 001 End */
            ((AlarmAdapter) mAlarmList.getAdapter()).notifyDataSetChanged();
        }
    }

    private class AlarmAdapter extends CursorAdapter {
        public AlarmAdapter(Context context, Cursor c) {
            super(context, c);
        }

        @Override
        public void bindView(View view, Context context, Cursor cursor) {
            final Alarm alarm = new Alarm(cursor);
            // Set the initial state of the clock "checkbox"
            DigitalClock digitalClock = (DigitalClock) view.findViewById(R.id.alarm_digitalClock);

            // set the alarm text
            final Calendar c = Calendar.getInstance();
            c.set(Calendar.HOUR_OF_DAY, alarm.hour);
            c.set(Calendar.MINUTE, alarm.minutes);
            digitalClock.setLive(false);
            digitalClock.updateTime(c);

            // Set the repeat text or leave it blank if it does not repeat.
            TextView daysOfWeekView = (TextView) digitalClock.findViewById(R.id.daysOfWeek);
            final String daysOfWeekStr = alarm.daysOfWeek.toString(DeleteAlarm.this, false);
            if (daysOfWeekStr != null && daysOfWeekStr.length() != 0) {
                daysOfWeekView.setText(daysOfWeekStr);
                daysOfWeekView.setVisibility(View.VISIBLE);
            } else {
                daysOfWeekView.setVisibility(View.GONE);
            }

            // Display the label
            TextView labelView = (TextView) view.findViewById(R.id.label);
            if (alarm.label != null && alarm.label.length() != 0) {
                labelView.setText(alarm.label);
                labelView.setVisibility(View.VISIBLE);
            } else {
                labelView.setVisibility(View.GONE);
            }

            CheckBox selectCheckBox = (CheckBox) view.findViewById(R.id.alarm_select_check_box);
            /* PR 570245- Neo Skunkworks - Paul Xu modified - 001 Begin */
            /*
             * selectCheckBox.setChecked(mSelectedMap.get(cursor.getPosition()));
             */
            if (selectCheckBox != null && mSelectedMap != null && cursor != null) {
                selectCheckBox.setChecked(mSelectedMap.get(cursor.getPosition()));
            }
            /* PR 570245- Neo Skunkworks - Paul Xu modified - 001 End */
            boolean isSelected = false;
            for (int i = 0; i < getCount(); i++) {
                boolean choosed = mSelectedMap.get(i);
                // Selected item is not find,go on
                if (!isSelected) {
                    isSelected = choosed;
                }
                if (isSelected) {
                    break;
                }
            }
            Button deleteButton = (Button) DeleteAlarm.this.findViewById(R.id.alarm_delete);
            /* PR 570245- Neo Skunkworks - Paul Xu modified - 001 Begin */
            /*
             * deleteButton.setEnabled(isSelected);
             */
            if (deleteButton != null) {
                deleteButton.setEnabled(isSelected);
            }

            DisplayMetrics dm = getResources().getDisplayMetrics();
            float desnity = dm.density;
            int height = (int) (labelView.getVisibility() == View.VISIBLE ? largeHeight * desnity
                    : smallHeight * desnity);

            LayoutParams lp = new LayoutParams(ViewGroup.LayoutParams.FILL_PARENT, height);
            view.setLayoutParams(lp);

            /* PR 570245- Neo Skunkworks - Paul Xu modified - 001 End */
        }

        @Override
        public View newView(Context context, Cursor cursor, ViewGroup parent) {
            return LayoutInflater.from(DeleteAlarm.this).inflate(R.layout.alarm_delete_listitem,
                    parent, false);
        }

    }
}
