
package com.jrdcom.timetool.worldclock.activity;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.AlertDialog.Builder;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CursorAdapter;
import android.widget.TextView;
import com.android.deskclock.R;
import com.jrdcom.timetool.alarm.activity.DeleteAlarm;
import com.jrdcom.timetool.alarm.provider.Alarm;
import com.jrdcom.timetool.alarm.provider.Alarms;
import com.jrdcom.timetool.worldclock.provider.TimeZoneInfo;
import com.jrdcom.timetool.worldclock.provider.TimeZones;

public class DeleteWorldClockActivity extends Activity implements AdapterView.OnItemClickListener {

    private Cursor mCursor;

    private AdapterView<CursorAdapter> mWorldTimeList;

    private WorldTimeAdapter mWorldTimeAdapter;

    private Map<Integer, Boolean> mSelectedMap = new HashMap<Integer, Boolean>();
    // PR:510457 add by xibin start
    private AlertDialog alertDialog = null;
    private BroadcastReceiver mIntentReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            updateTimeAndDateDisplay();
        }
    };

    private Handler mH = new Handler();

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        // get all showed worldClock.
        // PR:488578 add by XIBIN start --integrate world clock widget in
        // Timetool
        // mCursor = TimeZones.getShowedTimeZone(getContentResolver());
        mCursor = TimeZones.getShowedTimeZone(getApplicationContext(), false);
        // PR:488578 add by XIBIN end
        mWorldTimeAdapter = new WorldTimeAdapter(this, mCursor);
        setTitle(R.string.worldtime_delete_title);

        setContentView(R.layout.worldtime_delete_list);
        initLayout();

        // add by Yanjingming for PR489165 begin
        // initialize select map.
        mSelectedMap.clear();
        int count = mWorldTimeList.getCount();
        for (int i = 0; i < count; i++) {
            mSelectedMap.put(i, false);
        }
        // add by Yanjingming for PR489165 end
    }

    private void initLayout() {

        mWorldTimeList = (AdapterView<CursorAdapter>) findViewById(R.id.worldtime_list);
        mWorldTimeList.setAdapter(mWorldTimeAdapter);
        mWorldTimeList.setOnItemClickListener(this);
        View bk_btn = findViewById(R.id.bk_btn);
        bk_btn.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                finish();
            }

        });

        Button deleteButton = (Button) findViewById(R.id.worldtime_delete);
        deleteButton.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                deleteWorldTime();
            }
        });
        // PR:497844 add by XIBIN start
        if (mWorldTimeAdapter.isEmpty()) {
            deleteButton.setEnabled(false);
        }
        // PR:497844 add by XIBIN end
        Button cancelButton = (Button) findViewById(R.id.worldtime_cancel);
        cancelButton.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                finish();
            }
        });

    }

    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        setContentView(R.layout.worldtime_delete_list);
        initLayout();
    }

    public void onResume() {
        super.onResume();

        // initialize select map.
        // remove by Yanjingming for PR489165 begin
        // mSelectedMap.clear();
        // int count = mWorldTimeList.getCount();
        // for (int i = 0; i < count; i++) {
        // mSelectedMap.put(i, false);
        // }
        // remove by Yanjingming for PR489165 end

        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_TIME_TICK);
        filter.addAction(Intent.ACTION_TIME_CHANGED);
        filter.addAction(Intent.ACTION_TIMEZONE_CHANGED);
        registerReceiver(mIntentReceiver, filter, null, null);

        updateTimeAndDateDisplay();

        mH.postDelayed(new Runnable() {
            public void run() {
                updateTimeAndDateDisplay();
            }
        }, 10);
    }

    public void onPause() {
        super.onPause();
        unregisterReceiver(mIntentReceiver);
    }

    @Override
    protected void onDestroy() {
        // TODO Auto-generated method stub
        super.onDestroy();
        mCursor.close();
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
        updateTimeAndDateDisplay();
    }

    private void deleteWorldTime() {

        final Dialog check_dialog = new Dialog(this);
        check_dialog.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        View contentView = getLayoutInflater().inflate(
                R.layout.world_activity_delete_check_dialog_layout, null);
        TextView cancel_button = (TextView) contentView.findViewById(R.id.cancle_button);
        TextView message = (TextView) contentView.findViewById(R.id.message);
        TextView title = (TextView) contentView.findViewById(R.id.tile);
        message.setText(R.string.delete_worldtime_message);
        title.setText(R.string.delete_city);

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

                int position = mWorldTimeList.getCount() - 1;
                while (position >= 0) {
                    Cursor c = (Cursor) mWorldTimeList.getAdapter().getItem(position);
                    TimeZoneInfo currTimeZone = new TimeZoneInfo(c, DeleteWorldClockActivity.this);

                    if (/*
                         * !currTimeZone.isDefaultShow &&
                         */mSelectedMap.get(position)) {
                        currTimeZone.isShow = false;
                        currTimeZone.summerTime = TimeZoneInfo.SUMMERTIME_NONE;
                        currTimeZone.updateTime = Calendar.getInstance().getTimeInMillis();
                        TimeZones.updateTimeZone(
                                DeleteWorldClockActivity.this.getContentResolver(), currTimeZone);
                    }
                    position--;
                }

                check_dialog.dismiss();
                finish();

            }
        });
        check_dialog.setContentView(contentView);
        check_dialog.show();

        //
        // Builder alertBuilder = new AlertDialog.Builder(this);
        // alertBuilder.setIcon(android.R.drawable.ic_dialog_alert);
        // alertBuilder.setTitle(R.string.delete_city);
        // alertBuilder.setMessage(R.string.delete_worldtime_message);
        // alertBuilder.setNegativeButton(R.string.worldtime_cancel, null);
        // alertBuilder.setPositiveButton(R.string.worldtime_ok,
        // new DialogInterface.OnClickListener() {
        //
        // public void onClick(DialogInterface dialog, int which) {
        //
        // int position = mWorldTimeList.getCount() - 1;
        // while (position >= 0) {
        // Cursor c = (Cursor) mWorldTimeList.getAdapter()
        // .getItem(position);
        // TimeZoneInfo currTimeZone = new TimeZoneInfo(c,
        // DeleteWorldClockActivity.this);
        //
        // if (/*!currTimeZone.isDefaultShow
        // && */mSelectedMap.get(position)) {
        // currTimeZone.isShow = false;
        // currTimeZone.summerTime = TimeZoneInfo.SUMMERTIME_NONE;
        // currTimeZone.updateTime = Calendar
        // .getInstance().getTimeInMillis();
        // TimeZones.updateTimeZone(
        // DeleteWorldClockActivity.this
        // .getContentResolver(),
        // currTimeZone);
        // }
        // position--;
        // }
        //
        // dialog.dismiss();
        // finish();
        // }
        // });
        // //PR:510457 add by xibin
        // alertDialog = alertBuilder.show();
    }

    private void updateTimeAndDateDisplay() {
        if (mCursor.getCount() > 0) {

            ((WorldTimeAdapter) mWorldTimeList.getAdapter()).notifyDataSetChanged();
        }
    }

    private class WorldTimeAdapter extends CursorAdapter {

        public WorldTimeAdapter(Context context, Cursor c) {
            super(context, c);
        }

        @Override
        public View newView(Context context, Cursor cursor, ViewGroup parent) {
            return LayoutInflater.from(DeleteWorldClockActivity.this).inflate(
                    R.layout.worldtime_delete_listitem, parent, false);
        }

        @Override
        public void bindView(View view, Context context, Cursor cursor) {

            final TimeZoneInfo timeZone = new TimeZoneInfo(cursor, DeleteWorldClockActivity.this);
            TextView displayView = (TextView) view.findViewById(R.id.worldtime_display_name);
            displayView.setText(timeZone.displayName);

            // set select check box
            CheckBox selectCheckBox = (CheckBox) view.findViewById(R.id.worldtime_select_check_box);
            // Also delete BeiJing
            // if (timeZone.isDefaultShow) {
            // selectCheckBox.setVisibility(View.INVISIBLE);
            // } else {
            selectCheckBox.setVisibility(View.VISIBLE);
            /* FR 567596- Neo Skunkworks - Paul Xu modified - 001 Begin */
            /*
             * selectCheckBox
             * .setChecked(mSelectedMap.get(cursor.getPosition()));
             */
            if (mSelectedMap != null && cursor != null) {
                selectCheckBox.setChecked(mSelectedMap.get(cursor.getPosition()));
            }
            /* FR 567596- Neo Skunkworks - Paul Xu modified - 001 End */
            // }

            // Is there a selected item existing ?
            boolean isSelected = false;
            for (int i = 0; i < getCount(); i++) {
                Cursor c = (Cursor) getItem(i);
                TimeZoneInfo currTimeZone = new TimeZoneInfo(c, DeleteWorldClockActivity.this);
                // if (currTimeZone.isDefaultShow) {
                // continue;
                // }

                // PR697972-modify-by-peng.xie begin
                boolean choosed = false;
                if (mSelectedMap != null && mSelectedMap.size() > i) {
                    choosed = mSelectedMap.get(i);
                }
                // PR697972-modify-by-peng.xie end
                // Selected item is not find,go on
                if (!isSelected) {
                    isSelected = choosed;
                }
                // if unAllselected and selected are found at the same time,stop
                // looping
                if (isSelected) {
                    break;
                }
            }
            Button deleteButton = (Button) DeleteWorldClockActivity.this
                    .findViewById(R.id.worldtime_delete);
            deleteButton.setEnabled(isSelected);

        }
    }
}
