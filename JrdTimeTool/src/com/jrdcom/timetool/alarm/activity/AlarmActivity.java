/*
 * Copyright (C) 2007 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.jrdcom.timetool.alarm.activity;

import java.util.Calendar;
import java.util.Locale;

import android.R.integer;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AbsListView.LayoutParams;
import android.widget.AdapterView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Checkable;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.CursorAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.deskclock.R;
import com.jrdcom.timetool.TimeToolActivity;
import com.jrdcom.timetool.alarm.ToastMaster;
import com.jrdcom.timetool.alarm.provider.Alarm;
import com.jrdcom.timetool.alarm.provider.Alarms;
import com.jrdcom.timetool.alarm.view.DigitalClock;
import com.jrdcom.timetool.alarm.view.SwitchButton;

// add by liang.zhang for PR 849375 at 2014-12-07 begin
import android.app.Dialog;
import android.view.View.OnClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.Button;
//add by liang.zhang for PR 849375 at 2014-12-07 end

/**
 * AlarmClock application.
 */
public class AlarmActivity extends Activity implements OnItemClickListener {

    public static final String PREFERENCES = "AlarmClock";

    private Locale mLocale;

    /**
     * This must be false for production. If true, turns on logging, test code,
     * etc.
     */
    static final boolean DEBUG = false;

    private LayoutInflater mFactory;
    private ListView mAlarmsList;
    private Cursor mCursor;
    private Uri mExtraAlarmRingtone;

    private static final int SET_EXTRA_ALARM_RINGTONE_REQUEST_CODE = 1;
    // PR:503578 add by xibin
    private static final int SET_EXTRA_ALARM_REQUEST_CODE = 2;
    private View delAlarm;  // PR 824225 - xinlei.sheng added
    private LinearLayout footView;
    //PR:510457 add by xibin
    private AlertDialog alertDialog;

    private int smallHeight = 68;
    private int largeHeight = 72;

    private void updateAlarm(boolean enabled, Alarm alarm) {
        Alarms.enableAlarm(this, alarm.id, enabled);
        if (enabled) {
            SetAlarm.popAlarmSetToast(this, alarm.hour, alarm.minutes, alarm.daysOfWeek);
        }
    }

    public class AlarmTimeAdapter extends CursorAdapter {
        public AlarmTimeAdapter(Context context, Cursor cursor) {
            super(context, cursor);
        }

        @Override
        public View newView(Context context, Cursor cursor, ViewGroup parent) {
            View ret = mFactory.inflate(R.layout.alarm_time, parent, false);

            DigitalClock digitalClock = (DigitalClock) ret.findViewById(R.id.digitalClock);
            digitalClock.setLive(false);
            return ret;
        }

        @Override
        public void bindView(View view, Context context, Cursor cursor) {
            final Alarm alarm = new Alarm(cursor);

            View checkboxView = view.findViewById(R.id.slipbtn);
            if (checkboxView != null && checkboxView instanceof Checkable) {
                SwitchButton.setBroadcasting(true);
                ((Checkable) checkboxView).setChecked(alarm.enabled);
                SwitchButton.setBroadcasting(false);
                final SwitchButton switchButton = (SwitchButton) checkboxView;
                switchButton.setOnCheckedChangeListener(new OnCheckedChangeListener() {

                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        switchButton.setChecked(isChecked);
                        updateAlarm(isChecked, alarm);
                    }
                });
            }

            DigitalClock digitalClock = (DigitalClock) view.findViewById(R.id.digitalClock);
            // set the alarm text
            final Calendar c = Calendar.getInstance();
            c.set(Calendar.HOUR_OF_DAY, alarm.hour);
            c.set(Calendar.MINUTE, alarm.minutes);
            digitalClock.updateTime(c);

            // Set the repeat text or leave it blank if it does not repeat.
            TextView daysOfWeekView = (TextView) digitalClock.findViewById(R.id.daysOfWeek);
            final String daysOfWeekStr = alarm.daysOfWeek.toString(AlarmActivity.this, false);
            if (daysOfWeekStr != null && daysOfWeekStr.length() != 0) {
                daysOfWeekView.setText(daysOfWeekStr);
                daysOfWeekView.setVisibility(View.VISIBLE);
            } else {
                daysOfWeekView.setVisibility(View.GONE);
            }

            // Display the label
            TextView labelView = (TextView) view.findViewById(R.id.label);
            if (alarm.label != null && alarm.label.length() != 0) {
                // PR 767261 - mingwei.han added - Begin
                if (Alarms.isArFaIwLanguage(context)) {
                    labelView.setText("\u202D" + alarm.label + "\u202C");
                } else {
                    labelView.setText(alarm.label);
                }
                // PR 767261 - mingwei.han added - End
                labelView.setVisibility(View.VISIBLE);
            } else {
                labelView.setVisibility(View.GONE);
            }

            DisplayMetrics dm = getResources().getDisplayMetrics();
            float desnity = dm.density;
            int height = (int) (labelView.getVisibility() == View.VISIBLE ? largeHeight * desnity
                    : smallHeight * desnity);

            LayoutParams lp = new LayoutParams(ViewGroup.LayoutParams.FILL_PARENT, height);
            view.setLayoutParams(lp);
        }
    };

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        // TODO Auto-generated method stub
        super.onConfigurationChanged(newConfig);

        if (!newConfig.locale.equals(mLocale)) {
            updateLayout();
            mLocale = newConfig.locale;
        }

    }

    @Override
    public boolean onContextItemSelected(final MenuItem item) {
        final AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
        final int id = (int) info.id;
        // Error check just in case.
        if (id == -1) {
            return super.onContextItemSelected(item);
        }
        switch (item.getItemId()) {
            case R.id.delete_alarm: {
                // Confirm that the alarm will be deleted.
                // PR:510457 add by xibin
                alertDialog = new AlertDialog.Builder(this)
                        .setTitle(getString(R.string.delete_alarm))
                        .setMessage(getString(R.string.delete_alarm_confirm))
                        .setPositiveButton(android.R.string.ok,
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface d, int w) {
                                        Alarms.deleteAlarm(AlarmActivity.this, id);
                                        /*
                                         * PR 616192- Neo Skunkworks - Paul Xu
                                         * added - 001 Begin
                                         */
                                        if (Alarms.getPlayingAlarmId(AlarmActivity.this) == id) {
                                            sendDeletedPlayingAlarmBroadcast();
                                        }
                                        /*
                                         * PR 616192- Neo Skunkworks - Paul Xu
                                         * added - 001 End
                                         */
                                        /*
                                         * PR 570245- Neo Skunkworks - Paul Xu
                                         * modified - 001 Begin
                                         */
                                        /*
                                         * if (mCursor.getCount() - 1 > 0) {
                                         */
                                        if (mCursor != null && mCursor.getCount() - 1 > 0) {
                                            /*
                                             * PR 570245- Neo Skunkworks - Paul
                                             * Xu modified - 001 End
                                             */
                                            delAlarm.setVisibility(View.VISIBLE);
                                        } else {
                                            delAlarm.setVisibility(View.GONE);
                                        }
                                    }
                                }).setNegativeButton(android.R.string.cancel, null).show();
                return true;
            }
            default:
                break;
        }
        return super.onContextItemSelected(item);
    }

    /* PR 616192- Neo Skunkworks - Paul Xu added - 001 Begin */
    /**
     * Send delete the playing alarm broadcast.
     * 
     * @param null
     * @return null
     */
    private void sendDeletedPlayingAlarmBroadcast() {
        Intent alarmDeleted = new Intent(Alarms.ALARM_DELETED);
        sendBroadcast(alarmDeleted);
    }

    /* PR 616192- Neo Skunkworks - Paul Xu added - 001 End */

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        Intent intent = getIntent();
        if (intent != null && "vnd.android.cursor.dir/set_alarm".equals(intent.getType())) {
            mExtraAlarmRingtone = intent.getData();
        }
        mFactory = LayoutInflater.from(this);
        mCursor = Alarms.getAlarmsCursor(getContentResolver());

        mLocale = getResources().getConfiguration().locale;

        updateLayout();

    }

    private void updateLayout() {
        setContentView(R.layout.alarm_clock);
        mAlarmsList = (ListView) findViewById(R.id.alarms_list);
        // PR 824225 - xinlei.sheng added - begin
        footView = (LinearLayout) LayoutInflater.from(this)
                .inflate(R.layout.alarm_footer_view, null);

        mAlarmsList.addFooterView(footView);
        // PR 824225 - xinlei.sheng added - end
        AlarmTimeAdapter adapter = new AlarmTimeAdapter(this, mCursor);
        mAlarmsList.setAdapter(adapter);
        mAlarmsList.setVerticalScrollBarEnabled(true);
        mAlarmsList.setOnItemClickListener(this);
        // add by liang.zhang for PR 849375 at 2014-12-07 begin
//        mAlarmsList.setOnCreateContextMenuListener(this);
        mAlarmsList.setOnItemLongClickListener(new OnItemLongClickListener() {

			@Override
			public boolean onItemLongClick(AdapterView<?> parent, View view,
					int position, long id) {
				Cursor c = (Cursor) mAlarmsList.getAdapter().getItem(position);
				
				final Alarm alarm = new Alarm(c);
				
				// Construct the Calendar to compute the time.
				final Calendar cal = Calendar.getInstance();
				cal.set(Calendar.HOUR_OF_DAY, alarm.hour);
				cal.set(Calendar.MINUTE, alarm.minutes);
				final String time = Alarms.formatTime(AlarmActivity.this, cal);
				
				final Dialog dialog = new Dialog(AlarmActivity.this);
				dialog.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
				View v = getLayoutInflater().inflate(R.layout.alarm_activity_long_selected_layout, null);
				TextView textView = (TextView) v.findViewById(R.id.header_time);
				textView.setText(time);
				textView = (TextView) v.findViewById(R.id.header_label);
				textView.setText(alarm.label);
				Button delete = (Button)v.findViewById(R.id.delete_button);
				delete.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View v) {
						dialog.dismiss();
						final Dialog check_dialog = new Dialog(AlarmActivity.this);
						check_dialog.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
						View contentView = getLayoutInflater().inflate(
								R.layout.alarm_activity_delete_check_dialog_layout, null);
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
								Alarms.deleteAlarm(AlarmActivity.this, alarm.id);
								if (Alarms.getPlayingAlarmId(AlarmActivity.this) == alarm.id) {
									sendDeletedPlayingAlarmBroadcast();
								}
								if (mCursor != null && mCursor.getCount() - 1 > 0) {
									delAlarm.setVisibility(View.VISIBLE);
								} else {
									delAlarm.setVisibility(View.GONE);
								}
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
        // add by liang.zhang for PR 849375 at 2014-12-07 end

        View addAlarm = findViewById(R.id.add_alarm);
        addAlarm.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                addNewAlarm();
            }
        });
        // Make the entire view selected when focused.
        addAlarm.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            public void onFocusChange(View v, boolean hasFocus) {
                v.setSelected(hasFocus);
            }
        });
        delAlarm = findViewById(R.id.del_alarm);
        delAlarm.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                deleteAlarm();
            }
        });
        // Make the entire view selected when focused.
        delAlarm.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            public void onFocusChange(View v, boolean hasFocus) {
                v.setSelected(hasFocus);
            }
        });

        View setAlarm = findViewById(R.id.set_alarm);
        setAlarm.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                setAlarm();
            }
        });
        // Make the entire view selected when focused.
        setAlarm.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            public void onFocusChange(View v, boolean hasFocus) {
                v.setSelected(hasFocus);
            }
        });
        setButtonVisibleOrNot();
    }

    private void setButtonVisibleOrNot() {
        /* PR 570245- Neo Skunkworks - Paul Xu modified - 001 Begin */
        /*
         * if (mCursor.getCount() > 0) {
         */
        if (mCursor != null && mCursor.getCount() > 0) {
            /* PR 570245- Neo Skunkworks - Paul Xu modified - 001 End */
            delAlarm.setVisibility(View.VISIBLE);
        } else {
            delAlarm.setVisibility(View.GONE);
        }
    }

    @Override
    protected void onResume() {
        setButtonVisibleOrNot();
        super.onResume();
    }

    private void deleteAlarm() {
        startActivity(new Intent(this, DeleteAlarm.class));
    }

    private void setAlarm() {
        startActivity(new Intent(this, SettingsActivity.class));
    }

    private void addNewAlarm() {
        if (mExtraAlarmRingtone == null) {
            // PR:503578 add by xibin
            startActivityForResult(new Intent(this, SetAlarm.class), SET_EXTRA_ALARM_REQUEST_CODE);
        } else {
            Intent intent = new Intent(this, SetAlarm.class);
            intent.setData(mExtraAlarmRingtone);
            startActivityForResult(intent, SET_EXTRA_ALARM_RINGTONE_REQUEST_CODE);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ToastMaster.cancelToast();
        if (mCursor != null) {
            mCursor.close();
        }
        // PR:510457 add by xibin start
        if (alertDialog != null && alertDialog.isShowing()) {
            alertDialog.dismiss();
        }
        // PR:510457 add by xibin end
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View view, ContextMenuInfo menuInfo) {
        // Use the current item to create a custom view for the header.
        AdapterContextMenuInfo info = (AdapterContextMenuInfo) menuInfo;
        // add by Yanjingming for pr459670 begin
        if(info == null){
            Log.v("jrdtimetool","info == null");
            return;
        }
        // add by Yanjingming for pr459670 end
        final Cursor c = (Cursor) mAlarmsList.getAdapter().getItem(
                info.position);
        if (!(info.targetView).equals(footView)) {
            // Inflate the menu from xml.
            getMenuInflater().inflate(R.menu.alarm_context_menu, menu);
            final Alarm alarm = new Alarm(c);

            // Construct the Calendar to compute the time.
            final Calendar cal = Calendar.getInstance();
            cal.set(Calendar.HOUR_OF_DAY, alarm.hour);
            cal.set(Calendar.MINUTE, alarm.minutes);
            final String time = Alarms.formatTime(this, cal);

            // Inflate the custom view and set each TextView's text.
            final View v = mFactory.inflate(R.layout.context_menu_header, null);
            TextView textView = (TextView) v.findViewById(R.id.header_time);
            textView.setText(time);
            textView = (TextView) v.findViewById(R.id.header_label);
            textView.setText(alarm.label);

            // Set the custom view on the menu.
            menu.setHeaderView(v);
            if (alarm.enabled) {
            }
        } else {
            return;
        }
    }

    @Override
    public void onItemClick(AdapterView parent, View v, int pos, long id) {
        if (v instanceof ViewGroup) {
            int count = ((ViewGroup) v).getChildCount();
            if (count > 1) {
                final Cursor c = (Cursor) mAlarmsList.getAdapter().getItem(pos);
                final Alarm alarm = new Alarm(c);
                Intent intent = new Intent(this, SetAlarm.class);
                intent.putExtra(Alarms.ALARM_INTENT_EXTRA, alarm);
                if (mExtraAlarmRingtone == null) {
                    // PR:503578 add by xibin
                    startActivityForResult(intent, SET_EXTRA_ALARM_REQUEST_CODE);
                } else {
                    intent.setData(mExtraAlarmRingtone);
                    startActivityForResult(intent, SET_EXTRA_ALARM_RINGTONE_REQUEST_CODE);
                }
            }
        }
    }

    @Override
    public void onBackPressed() {
        TimeToolActivity parentActivity = (TimeToolActivity) getParent();
        if (mExtraAlarmRingtone == null && parentActivity != null) {

            // whether Timer is running?
            if (!parentActivity.checkIsCountDownRun()) {
                // whether timer is running?
                if (!parentActivity.checkIsTimerRun()) {
                    super.onBackPressed();
                }
            }
        } else {
            setResult(RESULT_CANCELED);
            super.onBackPressed();
        }
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // PR:503578 add by xibin start
        if (data != null) {
            String showStr = data.getStringExtra("TIME_ACTION");
            if (showStr != null) {
                Toast.makeText(this, showStr, Toast.LENGTH_LONG).show();
            }
        }
        if (requestCode == SET_EXTRA_ALARM_RINGTONE_REQUEST_CODE) {
            setResult(resultCode);
            finish();
        }
        // PR:503578 add by xibin end
    }
}
