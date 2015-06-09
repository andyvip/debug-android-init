/*
 * Copyright (C) 2009 The Android Open Source Project
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

import java.util.ArrayList;
import java.util.Arrays;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.res.Configuration;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.SystemProperties;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceActivity;
import android.preference.PreferenceScreen;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.android.deskclock.R;
import com.jrdcom.timetool.MyLog;
import com.jrdcom.timetool.alarm.preference.GesturesAlarmPreference;
import com.jrdcom.timetool.alarm.preference.ListPreference;
import com.jrdcom.timetool.alarm.provider.DialogAdapter;

/**
 * Settings for the Alarm Clock.
 */
public class SettingsActivity extends PreferenceActivity implements
        Preference.OnPreferenceChangeListener, OnPreferenceClickListener {
    private static final String TAG = "SettingsActivity";
    private static final boolean DEBUG = false;
    // Added by xiaxia.yao for PR:413675 begin
    private static final int ALARM_STREAM_TYPE_BIT = 1 << AudioManager.STREAM_ALARM;
    static final String KEY_ALARM_IN_SILENT_MODE = "alarm_in_silent_mode";
    // Added by xiaxia.yao for PR:413675 end
    public static final String KEY_ALARM_SNOOZE = "snooze_duration"; // Add by
                                                                     // caorongxing
                                                                     // for
                                                                     // PR:433114
    public static final String KEY_VOLUME_BEHAVIOR = "volume_button_setting";
    public static final String KEY_AUTO_SILENCE = "auto_silence";
    public static final String KEY_ALARM_PREFERENCE = "turn_over_preference";
    public static final String SNOOZE_ENABLE = "snooze_enable";     // modify by xinlei.sheng for PR847582
    public static final String PRE_SUMMARY = "pre_summary";
    // modify by Yanjingming for pr525162 begin
    public static final String DEFAULT_ALARM_TIMEOUT = "10";
    private static final String ERROR_VALUE = "-1";
	private static final String ALARM_TURNOVER_ENABLE = "alarm_turnover_enable";
	private static final String STOP_ENABLE = "stop_enable";
    // modify by Yanjingming for pr525162 end
	
	// remove by liang.zhang for PR 910288 at 2015-01-26 begin
//    private GesturesAlarmPreference mTurnOverPreference;
    
//    private String[] mAlarmItem;
//    private String mCurSummmary;
	// remove by liang.zhang for PR 910288 at 2015-01-26 end
    // Added by xiaxia.yao for turn over to active --001 begin
    private boolean mSnoozeEnable;
    private boolean mStopEnable;
    // private boolean mSnoozeEnable = true;
    // private SharedPreferences sharedPre;
    // Added by xiaxia.yao for turn over to active --001 end
    // PR:510457 add by xibin
    private AlertDialog mAlertDialog = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.alarm_settings);
        View bk_btn = findViewById(R.id.bk_btn);
        bk_btn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                finish();
            }
        });
        addPreferencesFromResource(R.xml.settings);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        // TODO Auto-generated method stub
        super.onConfigurationChanged(newConfig);
    }

    @Override
    protected void onDestroy() {
        // Added by xiaxia.yao for turn over to active --002 begin
        // sharedPre.edit().putString(PRE_SUMMARY, mCurSummmary)
        // .putBoolean(SNOOZE_ENABLE, mSnoozeEnable).commit();
        // Added by xiaxia.yao for turn over to active --002 end
        super.onDestroy();
        // PR:510457 add by xibin start
        if (mAlertDialog != null && mAlertDialog.isShowing()) {
            mAlertDialog.dismiss();
        }
        // PR:510457 add by xibin end
    }

    @Override
    protected void onResume() {
        super.onResume();
        // remove by liang.zhang for PR 910288 at 2015-01-26 begin
//        mAlarmItem = getResources().getStringArray(R.array.gestures_alarm_item);
//        refresh();
        // remove by liang.zhang for PR 910288 at 2015-01-26 end
        
        // add by liang.zhang for PR 927083 at 2015-02-14 begin
        final CheckBoxPreference alarmInSilentModePref = (CheckBoxPreference) findPreference(KEY_ALARM_IN_SILENT_MODE);
        final int silentModeStreams = Settings.System.getInt(getContentResolver(),
                Settings.System.MODE_RINGER_STREAMS_AFFECTED, 0);
        alarmInSilentModePref.setChecked((silentModeStreams & ALARM_STREAM_TYPE_BIT) == 0);
        // add by liang.zhang for PR 927083 at 2015-02-14 end
        
        // add by liang.zhang for PR 927050 at 2015-02-27 begin
        ListPreference listPref = (ListPreference) findPreference(KEY_ALARM_SNOOZE);
        listPref.setSummary(listPref.getEntry());
        listPref.setChangeSummary(true);
        listPref.setOnPreferenceChangeListener(this);
        // add by liang.zhang for PR 927050 at 2015-02-27 end
    }

    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen,
            final Preference preference) {
        // Added by xiaxia.yao for PR:413675 begin

        MyLog.i("onPreferenceTreeClick");
        if (KEY_ALARM_IN_SILENT_MODE.equals(preference.getKey())) {
            CheckBoxPreference pref = (CheckBoxPreference) preference;
            int ringerModeStreamTypes = Settings.System.getInt(getContentResolver(),
                    Settings.System.MODE_RINGER_STREAMS_AFFECTED, 0);

            if (pref.isChecked()) {
                ringerModeStreamTypes &= ~ALARM_STREAM_TYPE_BIT;
            } else {
                ringerModeStreamTypes |= ALARM_STREAM_TYPE_BIT;
            }

            Settings.System.putInt(getContentResolver(),
                    Settings.System.MODE_RINGER_STREAMS_AFFECTED, ringerModeStreamTypes);

            return true;
        }
        // Added by xiaxia.yao for PR:413675 end
        return super.onPreferenceTreeClick(preferenceScreen, preference);
    }

    public boolean onPreferenceChange(Preference pref, Object newValue) {

        if (KEY_AUTO_SILENCE.equals(pref.getKey())) {
            final ListPreference listPref = (ListPreference) pref;
            String delay = (String) newValue;
            updateAutoSnoozeSummary(listPref, delay);
            // Added by xiaxia.yao for turn over to active --003 begin
        } else if (KEY_ALARM_PREFERENCE.equals(pref.getKey())) {
            Settings.System.putInt(getContentResolver(), ALARM_TURNOVER_ENABLE,
                    (Boolean) newValue ? 1 : 0);
        } // Added by xiaxia.yao for turn over to active --003 end
        return true;
    }

    private void updateAutoSnoozeSummary(ListPreference listPref, String delay) {
        int i = Integer.parseInt(delay);
        // modify by Yanjingming for pr525162 begin
        if (i <= 0) {
            i = Integer.parseInt(DEFAULT_ALARM_TIMEOUT);
        }
        // modify by Yanjingming for pr525162 end
        listPref.setSummary(getString(R.string.auto_silence_summary, i));
    }

    // remove by liang.zhang for PR 910288 at 2015-01-26 begin
    /*private void refresh() {
        // Added by xiaxia.yao for PR:413675 begin
        final CheckBoxPreference alarmInSilentModePref = (CheckBoxPreference) findPreference(KEY_ALARM_IN_SILENT_MODE);
        final int silentModeStreams = Settings.System.getInt(getContentResolver(),
                Settings.System.MODE_RINGER_STREAMS_AFFECTED, 0);
        alarmInSilentModePref.setChecked((silentModeStreams & ALARM_STREAM_TYPE_BIT) == 0);

        listPref = (ListPreference) findPreference(KEY_ALARM_SNOOZE);
        listPref.setSummary(listPref.getEntry());
        listPref.setChangeSummary(true);
        listPref.setOnPreferenceChangeListener(this);
        /* FR 548923- Neo Skunkworks - Paul Xu deleted - 001 Begin */
        /*
         * listPref = (ListPreference) findPreference(KEY_AUTO_SILENCE); String
         * delay = listPref.getValue(); // add by Yanjingming for pr525162 begin
         * if(ERROR_VALUE.equals(delay)){
         * listPref.setValue(DEFAULT_ALARM_TIMEOUT); } // add by Yanjingming for
         * pr525162 end updateAutoSnoozeSummary(listPref, delay);
         * listPref.setOnPreferenceChangeListener(this);
         */
        /* FR 548923- Neo Skunkworks - Paul Xu deleted - 001 End */
        // Added by xiaxia.yao for turn over to active --004 begin
        
        // remove by liang.zhang for PR 910288 at 2015-01-26 begin
//        mTurnOverPreference = (GesturesAlarmPreference) findPreference(KEY_ALARM_PREFERENCE);
        // mTurnOverPreference.setPersistent(true);
//        mTurnOverPreference.setPersistent(false);
        /* PR 631260- Neo Skunkworks - Paul Xu added - 001 Begin */
        /* Set alarm turnover enable to default value */
//        if (DEBUG) {
//            Log.d(TAG,
//                    "ro_def_alarm_turnover_enable:"
//                            + SystemProperties.get("ro_def_alarm_turnover_enable"));
//        }
//        if (("false".equalsIgnoreCase(SystemProperties.get("ro_def_alarm_turnover_enable")))) {
//            mTurnOverPreference.setChecked(Settings.System.getInt(getContentResolver(),
//                    ALARM_TURNOVER_ENABLE, 0) == 1);
//        } else {
//            mTurnOverPreference.setChecked(Settings.System.getInt(getContentResolver(),
//                    ALARM_TURNOVER_ENABLE, 1) == 1);
//        }
        /* PR 631260- Neo Skunkworks - Paul Xu added - 001 End 
//        mTurnOverPreference.setOnPreferenceChangeListener(this);
//        mTurnOverPreference.setOnPreferenceClickListener(this);
        // sharedPre = getSharedPreferences(SetAlarm.RINGTONE_OF_PREALARM,
        // MODE_PRIVATE);
        // mSnoozeEnable = sharedPre.getBoolean(SNOOZE_ENABLE, true);
        mSnoozeEnable = Settings.System.getInt(getContentResolver(), SNOOZE_ENABLE,
                1) == 1;
        mStopEnable = Settings.System.getInt(getContentResolver(), STOP_ENABLE, 0) == 1;
        if (mSnoozeEnable) {
            mCurSummmary = mAlarmItem[0];
        }// else{
        if (mStopEnable) {
            mCurSummmary = mAlarmItem[1];
        }
//        mTurnOverPreference.setSummary(mCurSummmary);
    }*/

    
    /* PR 631260- Neo Skunkworks - Paul Xu added - 001 Begin */
    /**
     * Get the alarm turnover enable value.
     * 
     * @param null
     * @return boolean
     */
    /*private boolean getAlarmTurnOverEnableValue() {
        boolean enable = SystemProperties.getBoolean("ro_def_alarm_turnover_enable", true);

        return enable;
    }*/

    /* PR 631260- Neo Skunkworks - Paul Xu added - 001 End */

    /*private void showAlarmStatusDialog() {
        ArrayList<String> labelArray = new ArrayList<String>(Arrays.asList(mAlarmItem));
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_list_item_single_choice, labelArray);
        int index = 0;
        
        if (mTurnOverPreference.getSummary().equals(mAlarmItem[0])) {
            index = 0;
        } else if (mTurnOverPreference.getSummary().equals(mAlarmItem[1])) {
            index = 1;
        }

        final Dialog dialog = new Dialog(this);
        dialog.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        View view = getLayoutInflater().inflate(R.layout.set_alarm_dialog_layout, null);
        TextView title = (TextView) view.findViewById(R.id.tile);
        ListView listView = (ListView) view.findViewById(R.id.music_ringtone_list_view);
        Button close = (Button) view.findViewById(R.id.cancle_button);
        close.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                dialog.dismiss();

            }
        });
        listView.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                if (mAlarmItem != null && mAlarmItem.length > position) {
                    mTurnOverPreference.setSummary(mAlarmItem[position]);
                }
                
                if (position == 0) {
                    // Added by xiaxia.yao for turn over to active --005 begin
                     mCurSummmary = mAlarmItem[0];
                     mSnoozeEnable = true;
                    Settings.System.putInt(getContentResolver(),SNOOZE_ENABLE, 1);
                    Settings.System.putInt(getContentResolver(), STOP_ENABLE, 0);
                } else if (position == 1) {
                     mCurSummmary = mAlarmItem[1];
                     mSnoozeEnable = false;
                    Settings.System.putInt(getContentResolver(), STOP_ENABLE, 1);
                    Settings.System.putInt(getContentResolver(), SNOOZE_ENABLE, 0);
                    // Added by xiaxia.yao for turn over to active --005 end
                }
                dialog.dismiss();
            }
        });
        title.setText(R.string.turn_over_to_active_dialog_title);

        listView.setAdapter(new DialogAdapter(this, labelArray, index));
        dialog.setContentView(view);

        dialog.show();// PR:512833 add by XIBIN
    }*/
    // remove by liang.zhang for PR 910288 at 2015-01-26 end

    private ListPreference listPref;

    @Override
    public boolean onPreferenceClick(Preference preference) {
        MyLog.i("switch onPreferenceClick");
        // remove by liang.zhang for PR 910288 at 2015-01-26 begin
//        if (KEY_ALARM_PREFERENCE.equals(preference.getKey())) {
//            if (mTurnOverPreference.isChecked()) {
//                mTurnOverPreference.setChecked(false);
//            } else {
//                mTurnOverPreference.setChecked(true);
//            }
            // Added by xiaxia.yao for turn over to active --006 begin
//            boolean value = mTurnOverPreference.isChecked();
//            Settings.System.putInt(getContentResolver(), ALARM_TURNOVER_ENABLE,
//                    (Boolean) value ? 1 : 0);
            // Added by xiaxia.yao for turn over to active --006 end
//            showAlarmStatusDialog();
//        }
        // remove by liang.zhang for PR 910288 at 2015-01-26 end
        return true;
    }

}
