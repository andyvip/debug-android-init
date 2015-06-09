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

import java.io.File;

import android.app.ActionBar;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.database.ContentObserver;
import android.database.Cursor;
import android.database.sqlite.SQLiteException;
import android.media.AudioManager;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Parcelable;
import android.os.SystemProperties;
import android.os.Vibrator;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceScreen;
import android.provider.MediaStore;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.deskclock.R;
import com.jrdcom.timetool.MyLog;
import com.jrdcom.timetool.alarm.ToastMaster;
import com.jrdcom.timetool.alarm.preference.AlarmVolumePreference;
import com.jrdcom.timetool.alarm.preference.RepeatPreference;
import com.jrdcom.timetool.alarm.provider.Alarm;
import com.jrdcom.timetool.alarm.provider.Alarms;
import com.jrdcom.timetool.alarm.provider.RingtoneList;
import com.jrdcom.timetool.alarm.view.MyTimePicker;

/**
 * Manages each alarm
 * 
 * @param <MyTimePicker>
 */
public class SetAlarm extends PreferenceActivity implements Preference.OnPreferenceChangeListener {
    private static final String KEY_CURRENT_ALARM = "currentAlarm";
    private static final String KEY_ORIGINAL_ALARM = "originalAlarm";
    private static final String KEY_TIME_PICKER_BUNDLE = "timePickerBundle";

    private EditText mLabel;
    private TextView mRingtoneTv;
    private CheckBoxPreference mVibratePref;
    public static RepeatPreference mRepeatPref;// PR -596220 - Neo Skunworks -
                                               // Soar Gao , modify -001 modify
                                               // the static
    private AlarmVolumePreference mVolumePref;

    private int mId;
    private int mHour;
    private int mMinute;
    private Alarm mOriginalAlarm;
    private boolean mIsDestroyedByItself = false;

    private Uri mExtraAlarmRingtone;

    protected MyTimePicker mTimePicker;
    private boolean newFlag;

    static final String ACTION_NAME = "UPDATE_RINGTIONE_NAME";
    String mRingtonePath;
    String mRingtoneName;
    // modify by Yanjingming for pr502505 begin
    private static int SCREEN_WIDTH;
    private static int SCREEN_HEIGHT;
    // modify by Yanjingming for pr502505 end
    public static final String RINGTONE_OF_PREALARM = "ringtone_of_prealarm";
    public static final String RINGTONE_PATH = "ringtone_patch";
    public static final String RINGTONE_NAME = "ringtone_value";
    public static final String CURRENT_RIONGTONE = "current_ringtone";
    public static final String CURRENT_VOLUME = "current_volume";
    public static String mDefaultRingtoneName;
    public static String mDefaultRingtonePath;
    private String preRingtoneName;
    private String preRingtonePath;
    private int mVolume;
    private static final int mDefaultVolume = 3;

    private ContentResolver mContentResolver; // Add by xiaolong.que@tcl.com for
                                              // BUG-420462
    private boolean enableWhenDataChange = false; // Add by xiaolong.que@tcl.com
                                                  // for BUG-420462
    private boolean mAlarmSuperMode = false; // huanglin 20130826 for PR513663

    /* PR 626156- Neo Skunkworks - Paul Xu added - 001 Begin */
    /* alarm enable status variable */
    private boolean mAlarmEnable = false;
    /* PR 626156- Neo Skunkworks - Paul Xu added - 001 End */
    /* PR 685347- Neo Skunkworks - Paul Xu added - 001 Begin */
    private boolean mSetAlarm = false;
    /* PR 685347- Neo Skunkworks - Paul Xu added - 001 End */
    /* PR 678654 - Neo Skunkworks - Paul Xu added - 001 Begin */
    private boolean mCancelAlarm = false;

    /* PR 678654 - Neo Skunkworks - Paul Xu added - 001 End */

    @Override
    protected void onCreate(Bundle state) {
        super.onCreate(state);
        // Override the default content view.
        setContentView(R.layout.alarm_set);
        /* PR 685347- Neo Skunkworks - Paul Xu added - 001 Begin */
        // Get intent BooleanExtraerror values.
        getSetAlarmValues();
        /* PR 685347- Neo Skunkworks - Paul Xu added - 001 End */
        init();
    }

    private void init() {
        getScreenResolution();
        mContentResolver = getContentResolver(); // Add by xiaolong.que@tcl.com
                                                 // for BUG-420462
        // PR539763 default ringtone to ring. begin
        //Uri defaultRingtoneUri = RingtoneManager.getDefaultRingtoneUri(this,
        //        RingtoneManager.TYPE_ALARM);
        // PR539763 default ringtone to ring. end
        // PR828026 by xing.zhao  Click the add alarm Button in alarm occur FC on SkinUpdateVersion  begin
        Uri defaultRingtoneUri = RingtoneManager.getActualDefaultRingtoneUri(this,
                RingtoneManager.TYPE_ALARM);
        // PR828026 by xing.zhao Click the add alarm Button in alarm occur FC on SkinUpdateVersion  end
        boolean hasDeafault = false;
        Cursor cursor = null;
        if (defaultRingtoneUri != null) {
            try {
                cursor = getContentResolver().query(defaultRingtoneUri, new String[] {
                    MediaStore.Audio.Media.DISPLAY_NAME
                }, null, null, null);
                if (cursor != null && cursor.moveToFirst()) {
                    String defauleName = cursor.getString(0);
                    File[] mediaFiles = RingtoneList.mediaFiles;
                    for (int i = 0; i < mediaFiles.length; i++) {
                        if (defauleName != null && defauleName.equals(mediaFiles[i].getName())) {
                            mDefaultRingtoneName = defauleName;
                            mDefaultRingtonePath = mediaFiles[i].getAbsolutePath();
                            hasDeafault = true;
                            break;
                        }
                    }
                }
            } catch (SQLiteException sqle) {
                sqle.printStackTrace();
            } finally {
                if (cursor != null)
                    cursor.close();
            }
        }

        if (!hasDeafault) {
            // set the first ringtone as default ringtone
            /* FR 562625- Neo Skunkworks - Paul Xu modified - 001 Begin */
            /*
             * mDefaultRingtoneName = RingtoneList.getRingtoneList(this)[1];
             */
            if (RingtoneList.getRingtoneList(this).length > 1) {
                mDefaultRingtoneName = RingtoneList.getRingtoneList(this)[1];
            } else {
                mDefaultRingtoneName = RingtoneList.getRingtoneList(this)[0];
            }
            /* FR 562625- Neo Skunkworks - Paul Xu modified - 001 End */
            mDefaultRingtonePath = RingtoneList.mediaFiles[0].getAbsolutePath();
        }
        // add by xibin for PR:484655 end
        SharedPreferences sharedPre = getSharedPreferences(RINGTONE_OF_PREALARM, MODE_PRIVATE);
        mTimePicker = (MyTimePicker) findViewById(R.id.timePicker);
        mTimePicker.setOnTimeChangedListener(new MyTimePicker.OnTimeChangedListener() {

            
            @Override
            public void onTimeChanged(MyTimePicker view, int hour, int minute) {
                onTimeSet(view, hour, minute);
            }
        });

        EditText label = (EditText) getLayoutInflater().inflate(R.layout.alarm_label, null);
        final Intent intent = new Intent(this, GotoRingtoneActivity.class);
        LinearLayout footView = (LinearLayout) LayoutInflater.from(this).inflate(
                R.layout.alarm_ringtone_pref, null);

        TextView ringtone = (TextView) footView.findViewById(R.id.ringtone_name);
        ListView list = (ListView) findViewById(android.R.id.list);
        list.setOnScrollListener(new MyScrollListener());// PR738839-haiying.he
        list.addHeaderView(label);
        list.addFooterView(footView);
        // TODO Stop using preferences for this view. Save on done, not after
        // each change.
        addPreferencesFromResource(R.xml.alarm_prefs);

        // Get each preference so we can retrieve the value later.
        mLabel = label;
        mLabel.setTag(new Object());// PR738839-haiying.he
        mRingtoneTv = ringtone;
        if (mVibratePref != null) {
            getPreferenceScreen().removePreference(mVibratePref);
        }
        mVibratePref = (CheckBoxPreference) findPreference("vibrate");
        mVibratePref.setOnPreferenceChangeListener(this);
        Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        if (!v.hasVibrator()) {
            getPreferenceScreen().removePreference(mVibratePref);
        }
        /* PR 672701 - Neo Skunkworks - Paul Xu added - 001 Begin */
        if (Alarms.needVibratePreference(this) == false) {
            getPreferenceScreen().removePreference(mVibratePref);
        }
        /* PR 672701 - Neo Skunkworks - Paul Xu added - 001 End */
        if (mRepeatPref != null) {
            getPreferenceScreen().removePreference(mRepeatPref);
        }
        mRepeatPref = (RepeatPreference) findPreference("setRepeat");
        mRepeatPref.setOnPreferenceChangeListener(this);
        if (mVolumePref != null) {
            getPreferenceScreen().removePreference(mVolumePref);
        }
        mVolumePref = (AlarmVolumePreference) findPreference("setVolume");
        mVolumePref.setOnPreferenceChangeListener(this);

        Intent i = getIntent();
        mExtraAlarmRingtone = i.getData();
        // huanglin 20130826 for PR513663
        boolean superMode = Boolean
                .parseBoolean(SystemProperties.get("sys.supermode.key", "false"));
        if (superMode) {
            mAlarmSuperMode = i.getBooleanExtra(Alarms.INTENT_EXTRA_SUPERMODE, false);
        }
        Alarm alarm = i.getParcelableExtra(Alarms.ALARM_INTENT_EXTRA);
        preRingtoneName = sharedPre.getString(RINGTONE_NAME, mDefaultRingtoneName);
        preRingtonePath = sharedPre.getString(RINGTONE_PATH, mDefaultRingtonePath);
        if (alarm == null) {
            // No alarm means create a new alarm.
            alarm = new Alarm();
            // add PR433714 XIBIN start-The default ringtone has been set
            // abnormal after delete or rename the alarm ringtone
            preRingtoneName = mDefaultRingtoneName;
            preRingtonePath = mDefaultRingtonePath;
            // add PR433714 XIBIN end
            newFlag = true;
            mRingtonePath = preRingtonePath;
            mRingtoneName = preRingtoneName;
            alarm.alert = preRingtoneName;
            alarm.alertPath = preRingtonePath;
            mVolume = mDefaultVolume;
            enableWhenDataChange = true; // Add by xiaolong.que@tcl.com for
                                         // BUG-420462
            /*--PR 667590 	- Neo Skunkworks - Paul Xu added - 001 Begin--*/
            HandleSetAlarm.mSilentRingTone = false;
            /*--PR 667590 	- Neo Skunkworks - Paul Xu added - 001 End--*/
        } else {
            if (new MusicActivity().isCurrentMusicExist(this, alarm.alert)
                    || new SystemActivity().isCurrentRingtoneExist(this, alarm.alert)) {
                // modify by Yanjingming for pr484826 begin
                /*--PR 667590 	- Neo Skunkworks - Paul Xu added - 001 Begin--*/
                if (HandleSetAlarm.mSilentRingTone) {
                    mDefaultRingtoneName = HandleSetAlarm.mRingtoneName;
                    alarm.alert = "Silent";
                    mRingtonePath = "silent";
                    mDefaultRingtonePath = "silent";
                }
                if ("Silent".equals(alarm.alert) || "silent".equals(alarm.alert)) {
                    mRingtoneTv.setText(getResources().getString(R.string.timer_ringtone_silent));
                } else {
                    mRingtoneTv.setText(formatRingtone(alarm.alert));// modify
                                                                     // by
                                                                     // Yanjingming
                                                                     // for
                                                                     // pr543246
                }
                /*--PR 667590 	- Neo Skunkworks - Paul Xu added - 001 End--*/
                // modify by Yanjingming for pr484826 end
                mRingtoneName = alarm.alert;
            } else {
                mRingtoneName = mDefaultRingtoneName;
                mRingtonePath = mDefaultRingtonePath;
                alarm.alert = mDefaultRingtoneName;
                alarm.alertPath = mDefaultRingtonePath;
            }
            mVolume = alarm.volume;
        }
        sharedPre.edit().putInt(CURRENT_VOLUME, mVolume).commit();
        mOriginalAlarm = alarm;
        // Populate the prefs with the original alarm data. updatePrefs also
        // sets mId so it must be called before checking mId below.
        updatePrefs(mOriginalAlarm);

        // We have to do this to get the save/cancel buttons to highlight on
        // their own.
        getListView().setItemsCanFocus(true);
        // Modified by xiaxia.yao for PR:417702 begin
        initActionBar();
        // Modified by xiaxia.yao for PR:417702 end
        // Attach actions to each button.
        Button b = (Button) findViewById(R.id.alarm_save);
        b.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                mVolumePref.onSaveVolume();
                long time = saveAlarm(null, true);
                // PR:503578 add by xibin start
                // @ modify by Yanjingming for pr455568
                Intent intentToast = new Intent();
                if (enableWhenDataChange || mOriginalAlarm.enabled) {
                    // popAlarmSetToast(SetAlarm.this, time);
                    String toastText = formatToast(SetAlarm.this, time);
                    intentToast.putExtra("TIME_ACTION", toastText);
                    /* PR 685347- Neo Skunkworks - Paul Xu added - 001 Begin */
                    setAlarmToast(toastText);
                    /* PR 685347- Neo Skunkworks - Paul Xu added - 001 End */
                }
                if (mExtraAlarmRingtone != null) {
                    setResult(RESULT_OK, intentToast);
                } else {
                    setResult(RESULT_CANCELED, intentToast);
                }
                // PR:503578 add by xibin end
                SharedPreferences sharedPre = getSharedPreferences(RINGTONE_OF_PREALARM,
                        MODE_PRIVATE);
                preRingtoneName = mRingtoneName;
                preRingtonePath = mRingtonePath;
                sharedPre.edit().putString(RINGTONE_NAME, preRingtoneName)
                        .putString(RINGTONE_PATH, preRingtonePath).commit();
                mIsDestroyedByItself = true;
                // huanglin 20130826 for PR513663
                boolean superMode = Boolean.parseBoolean(SystemProperties.get("sys.supermode.key",
                        "false"));
                if (superMode && mAlarmSuperMode) {
                    Intent intent = new Intent();
                    intent.setClassName("com.jrdcom.supermode",
                            "com.jrdcom.supermode.SuperModeActivity");
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    getApplicationContext().startActivity(intent);
                } else {
                    finish();
                }
            }
        });
        Button revert = (Button) findViewById(R.id.alarm_revert);
        revert.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                revert();

                if (mExtraAlarmRingtone != null) {
                    setResult(RESULT_CANCELED);
                }
                mIsDestroyedByItself = true;
                // huanglin 20130826 for PR513663
                boolean superMode = Boolean.parseBoolean(SystemProperties.get("sys.supermode.key",
                        "false"));
                if (superMode && mAlarmSuperMode) {
                    Intent intent = new Intent();
                    intent.setClassName("com.jrdcom.supermode",
                            "com.jrdcom.supermode.SuperModeActivity");
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    getApplicationContext().startActivity(intent);
                } else {
                    finish();
                }
            }
        });
        registerBoradcastReceiver();
        footView.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                // PR:502047 add by XIBIN
                mLabel.clearFocus();
                SharedPreferences sharedPre = getSharedPreferences(RINGTONE_OF_PREALARM,
                        MODE_PRIVATE);
                sharedPre.edit().putString(CURRENT_RIONGTONE, mRingtoneName).commit();
                sharedPre = getSharedPreferences(RingtoneList.TIMER_RPREFERENCES, 0);
                // PR:502487 add By XIBIN start
                sharedPre.edit().putString(RingtoneList.ALERT_RINGTONE_PATH_KEY, mRingtonePath)
                        .commit();
                // PR:502487 add By XIBIN end
                startActivity(intent);
            }

        });
        /* Add by xiaolong.que@tcl.com for BUG-420462 Begin */
        if (mId != -1) {
            mContentResolver.registerContentObserver(Alarm.Columns.CONTENT_URI, true,
                    mSetAlarmObserver);
        }
        /* Add by xiaolong.que@tcl.com for BUG-420462 End */
    }

    private void initActionBar() {
        ActionBar actionBar = getActionBar();
        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View customActionBarView = inflater.inflate(R.layout.set_alarm_action_bar, null);
        actionBar.setDisplayShowHomeEnabled(false);
        actionBar.setDisplayShowTitleEnabled(false);
        actionBar.setDisplayShowCustomEnabled(true);
        actionBar.setBackgroundDrawable(getResources().getDrawable(R.drawable.title_bar));
        actionBar.setCustomView(customActionBarView);
        View bk_btn = customActionBarView.findViewById(R.id.bk_btn);
        bk_btn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // huanglin 20130829 for PR513663
                boolean superMode = Boolean.parseBoolean(SystemProperties.get("sys.supermode.key",
                        "false"));
                if (superMode && mAlarmSuperMode) {
                    Intent intent = new Intent();
                    intent.setClassName("com.jrdcom.supermode",
                            "com.jrdcom.supermode.SuperModeActivity");
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    getApplicationContext().startActivity(intent);
                } else {
                    finish();
                }
            }

        });
    }

    /* PR 685347- Neo Skunkworks - Paul Xu added - 001 Begin */
    /**
     * Get set alarm values.
     * 
     * @param null
     * @return boolean .
     */
    private boolean getSetAlarmValues() {
        Intent intent = getIntent();
        if (intent != null) {
            mSetAlarm = intent.getBooleanExtra(Alarms.EXTRA_SET_ALARM, false);
        }

        return mSetAlarm;
    }

    /**
     * Set alarm toast.
     * 
     * @param String toastText
     * @return null .
     */
    private void setAlarmToast(String toastText) {
        if (getSetAlarmValues()) {
            Toast.makeText(SetAlarm.this, toastText, Toast.LENGTH_LONG).show();
        }
    }

    /* PR 685347- Neo Skunkworks - Paul Xu added - 001 End */

    /**
     * + * Add by xiaolong.que@tcl.com for BUG-420462 Begin +
     */

    private final ContentObserver mSetAlarmObserver = new ContentObserver(new Handler()) {
        @Override
        public void onChange(boolean selfChange) {
            if (mId == -1) {
                return;
            }
            Cursor cursor = mContentResolver.query(
                    ContentUris.withAppendedId(Alarm.Columns.CONTENT_URI, mId), new String[] {
                        "enabled"
                    }, null, null, null);
            if (cursor != null) {
                enableWhenDataChange = true;
                cursor.close();
            }
        }
    };

    /**
     * + * Add by xiaolong.que@tcl.com for BUG-420462 End +
     */
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable(KEY_ORIGINAL_ALARM, mOriginalAlarm);
        outState.putParcelable(KEY_CURRENT_ALARM, buildAlarmFromUi());
        if (mTimePicker != null) {
            outState.putParcelable(KEY_TIME_PICKER_BUNDLE, mTimePicker.onSaveInstanceState());
            //mTimePicker = null;   // modify by xinlei.sheng for PR839694
        }
    }

    @Override
    protected void onRestoreInstanceState(Bundle state) {
        super.onRestoreInstanceState(state);

        Alarm alarmFromBundle = state.getParcelable(KEY_ORIGINAL_ALARM);
        if (alarmFromBundle != null) {
            mOriginalAlarm = alarmFromBundle;
        }

        alarmFromBundle = state.getParcelable(KEY_CURRENT_ALARM);
        if (alarmFromBundle != null) {
            updatePrefs(alarmFromBundle);
        }

        Parcelable b = state.getParcelable(KEY_TIME_PICKER_BUNDLE);
        if (b != null) {
            mTimePicker.onRestoreInstanceState(b);
        }
    }

    // Used to post runnables asynchronously.
    private static final Handler sHandler = new Handler();

    public boolean onPreferenceChange(final Preference p, Object newValue) {
        // Asynchronously save the alarm since this method is called _before_
        // the value of the preference has changed.
        enableWhenDataChange = true; // Add by xiaolong.que@tcl.com for
                                     // BUG-420462
        //PR851952-jin.chen-begin-001
		// sHandler.post(new Runnable() {
		// public void run() {
		// // add xibin PR444396 start -- The alarm automatically start
		// // after disable "Vibrate"
		// saveAlarm(null, false);
		// }
		// });
		// // add xibin PR444396 end
        //PR851952-jin.chen-end-001
        return true;
    }

    private void updatePrefs(Alarm alarm) {
        mId = alarm.id;
        mLabel.setText(alarm.label);
        mHour = alarm.hour;
        mMinute = alarm.minutes;
        /*--PR 667590 	- Neo Skunkworks - Paul Xu added - 001 Begin--*/
        if (HandleSetAlarm.mSilentRingTone) {
            alarm.daysOfWeek = new Alarm.DaysOfWeek(1 | 4);
            alarm.alertPath = "silent";
        }
        /*--PR 667590 	- Neo Skunkworks - Paul Xu added - 001 End--*/
        mRepeatPref.setDaysOfWeek(alarm.daysOfWeek);
        mVibratePref.setChecked(alarm.vibrate);
        if (newFlag) {
            mRingtoneTv.setText(getString(R.string.default_ringtone) + " ( "
                    + formatRingtone(preRingtoneName) + " )");// modify by
                                                              // Yanjingming for
                                                              // pr543246
        } else {
            // modify by Yanjingming for pr484826 begin
            if ("Silent".equals(alarm.alert) || "silent".equals(alarm.alert)) {
                mRingtoneTv.setText(getResources().getString(R.string.timer_ringtone_silent));
            } else {
                mRingtoneTv.setText(formatRingtone(alarm.alert));// modify by
                                                                 // Yanjingming
                                                                 // for pr543246
            }
            // modify by Yanjingming for pr484826 end
        }
        mTimePicker.setCurrentHour(mHour);
        mTimePicker.setCurrentMinute(mMinute);
        mRingtoneName = alarm.alert;// PR -586334 - Neo Skunworks - Soar Gao ,
                                    // add -001
        mRingtonePath = alarm.alertPath;
        if (newFlag) {
            mVolumePref.setDefaultVoume();
            mVolumePref.setCurRingtone(mRingtonePath);
        } else {
            mVolumePref.setVoume(alarm.volume);
            mVolumePref.setCurRingtone(mRingtonePath);
        }
        updateRingtoneName(alarm.alert);// PR -586334 - Neo Skunworks - Soar Gao
                                        // , add -001
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        // TODO Auto-generated method stub
        super.onConfigurationChanged(newConfig);

        setContentView(R.layout.alarm_set);
        // PR 586334 - Neo Skunkworks - Soar Gao - 001 begin
        // save modified value
        Alarm alarm = buildAlarmFromUi();
        init();
        updatePrefs(alarm);
        // add by haifeng.tang PR:790675 Time:2014.9.18 begin
        mLabel.requestFocus();
        if (mLabel.isFocused()) {

            MyLog.debug("lable get focus", getClass());

        } else {
            if (getCurrentFocus() != null) {
                MyLog.debug(getCurrentFocus().toString(), getClass());
            } else {
                MyLog.debug("current focus is null", getClass());
            }
        }
        // add by haifeng.tang PR:790675 Time:2014.9.18 end
        // PR 586334 - Neo Skunkworks - Soar Gao - 001 end
    }

    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
        return super.onPreferenceTreeClick(preferenceScreen, preference);
    }

    @Override
    public void onBackPressed() {
        /*--PR 667590 	- Neo Skunkworks - Paul Xu added - 001 Begin--*/
        HandleSetAlarm.mSilentRingTone = false;
        HandleSetAlarm.mVerifyDissmiss = false;
        /*--PR 667590 	- Neo Skunkworks - Paul Xu added - 001 End--*/
        revert();
        if (mExtraAlarmRingtone != null) {
            setResult(RESULT_CANCELED);
        }
        // huanglin 20130826 for PR513663
        boolean superMode = Boolean
                .parseBoolean(SystemProperties.get("sys.supermode.key", "false"));
        if (superMode && mAlarmSuperMode) {
            Intent intent = new Intent();
            intent.setClassName("com.jrdcom.supermode", "com.jrdcom.supermode.SuperModeActivity");
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
            getApplicationContext().startActivity(intent);
        } else {
            finish();
        }
    }

    /* PR 674835 - Neo Skunkworks - Paul Xu added - 001 Begin */
    /**
     * Get alarm volume.
     * 
     * @param null
     * @return int.
     */
    private int getStreamAlarmVolume() {
        int volume = 0;
        AudioManager audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        if (audioManager != null) {
            volume = audioManager.getStreamVolume(AudioManager.STREAM_ALARM);
        }

        return volume;
    }

    /**
     * Get preference volume.
     * 
     * @param null
     * @return int.
     */
    private int getPreferenceVolume() {
        int volume = 0;
        if (mVolumePref != null) {
            volume = mVolumePref.getVolume();
        }

        return volume;
    }

    @Override
    protected void onResume() {
        // TODO Auto-generated method stub
        super.onResume();

        int streamVolume = getStreamAlarmVolume();
        int preVolume = getPreferenceVolume();
        if (streamVolume != preVolume) {
            mVolumePref.setVoume(streamVolume);
        }
    }

    /* PR 674835 - Neo Skunkworks - Paul Xu added - 001 End */

    @Override
    protected void onDestroy() {
        // TODO Auto-generated method stub
        /*--PR 667590 	- Neo Skunkworks - Paul Xu added - 001 Begin--*/
        HandleSetAlarm.mSilentRingTone = false;
        HandleSetAlarm.mVerifyDissmiss = false;
        /*--PR 667590 	- Neo Skunkworks - Paul Xu added - 001 End--*/
        unregisterReceiver(mBroadcastReceiver);
        mContentResolver.unregisterContentObserver(mSetAlarmObserver); // Add by
                                                                       // xiaolong.que@tcl.com
                                                                       // for
                                                                       // BUG-420462
        if (!mIsDestroyedByItself) {
            revert();
        }
        super.onDestroy();
    }

    public void onTimeSet(MyTimePicker view, int hourOfDay, int minute) {
        // add by xinlei.sheng for PR839694 start
        if(mMinute == 0 && minute == 59){
            hourOfDay = hourOfDay - 1;
            if(hourOfDay == -1){
                hourOfDay = 23;
            }
        }
        if(mMinute == 59 && minute == 0){
            hourOfDay = hourOfDay + 1;
            if(hourOfDay == 24){
                hourOfDay = 0;
            }
        }
        mHour = hourOfDay;
        mMinute = minute;
        mTimePicker.setCurrentHour(mHour);
        mTimePicker.setCurrentMinute(mMinute);
        // add by xinlei.sheng for PR839694 end
        enableWhenDataChange = true; // Add by xiaolong.que@tcl.com for
                // BUG-420462
    }

    // add xibin PR444396 start -- The alarm automatically start after disable
    // "Vibrate"
    private long saveAlarm(Alarm alarm, boolean ifNeedClearSnooze) {
        return saveAlarm(alarm, true, ifNeedClearSnooze);
    }

    private long saveAlarm(Alarm alarm, boolean isStatusBarIcon, boolean ifNeedClearSnooze) {
        /* PR 655666- Neo Skunkworks - Paul Xu added - 001 Begin */
        if (!isStatusBarIcon) {
            return 0;
        }
        /* PR 655666- Neo Skunkworks - Paul Xu added - 001 End */
        /* PR 626156- Neo Skunkworks - Paul Xu added - 001 Begin */
        /* get alarm enable status */
        mAlarmEnable = isStatusBarIcon;
        /* PR 626156- Neo Skunkworks - Paul Xu added - 001 End */
        if (alarm == null) {
            alarm = buildAlarmFromUi();
        }

        long time;
        if (alarm.id == -1) {
            time = Alarms.addAlarm(this, alarm, isStatusBarIcon);
            // addAlarm populates the alarm with the new id. Update mId so that
            // changes to other preferences update the new alarm.
            mId = alarm.id;
        } else {
            // modified by haifeng.tang PR 785586 begin
            time = Alarms.setAlarm(this, alarm, isStatusBarIcon, ifNeedClearSnooze);
            // modified by haifeng.tang PR 785586 end
        }
        /* PR 678654 - Neo Skunkworks - Paul Xu added - 001 Begin */
        if (!mCancelAlarm) {
            Alarms.storeNearestAlarm(SetAlarm.this, alarm);
        }
        mCancelAlarm = false;
        /* PR 678654 - Neo Skunkworks - Paul Xu added - 001 End */
        return time;
    }

    // add xibin PR444396 end
    private Alarm buildAlarmFromUi() {
        Alarm alarm = new Alarm();
        alarm.id = mId;
        if (newFlag) {
            alarm.enabled = true;
        } else {
            alarm.enabled = mOriginalAlarm.enabled;
            /*
             * @{ modify by Yanjingming for PR455568 begin
             */
            // alarm.enabled = enableWhenDataChange; //modify by
            // xiaolong.que@tcl.com for BUG-420462
            if (enableWhenDataChange) {
                alarm.enabled = true;
            }
            /*
             * @{ modify by Yanjingming for PR455568 end
             */
        }
        alarm.hour = mHour;
        alarm.minutes = mMinute;
        alarm.daysOfWeek = mRepeatPref.getDaysOfWeek();
        alarm.vibrate = mVibratePref.isChecked();
        alarm.label = mLabel.getText().toString();
        alarm.alert = mRingtoneName;
        alarm.volume = mVolumePref.getVolume();
        alarm.alertPath = mRingtonePath;
        return alarm;
    }

    /**
     * add by Yanjingming for pr539153 begin To update enable state when click
     * cancel button.
     */
    private boolean updateEnable(int id, boolean defEnable) {
        ContentResolver contentResolver = this.getContentResolver();
        Uri uri = ContentUris.withAppendedId(Alarm.Columns.CONTENT_URI, id);
        Cursor cursor = contentResolver.query(uri, new String[] {
            "enabled"
        }, "_id = ?", new String[] {
            String.valueOf(id)
        }, null);
        try {
            if (cursor != null && cursor.moveToFirst()) {
                int enable = cursor.getInt(0);
                if (0 == enable) {
                    return false;
                } else if (1 == enable) {
                    return true;
                }
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return defEnable;
    }

    // add by Yanjingming for pr539153 end

    private void revert() {
        // PR :470784 update by xibin
        mVolumePref.onRevertVolume();
        mContentResolver.unregisterContentObserver(mSetAlarmObserver); // Add by
                                                                       // xiaolong.que@tcl.com
                                                                       // for
                                                                       // BUG-420462
        int newId = mId;
        // "Revert" on a newly created alarm should delete it.
        // add pr466531 xibin start--Vibration and Repeat items are still
        // changed although the edit behavior is cancelled
        if (mOriginalAlarm.id == -1) {
            Alarms.deleteAlarm(SetAlarm.this, newId);
        } else {
            // add by Yanjingming for pr539153 begin
            if (!mOriginalAlarm.daysOfWeek.isRepeatSet()) {
                /* PR 626156- Neo Skunkworks - Paul Xu modified - 001 Begin */
                /* Update only when mAlarmEnable is true */
                /*
                 * mOriginalAlarm.enabled =
                 * updateEnable(mOriginalAlarm.id,mOriginalAlarm.enabled);
                 */
                if (mOriginalAlarm.enabled) {
                    mOriginalAlarm.enabled = updateEnable(mOriginalAlarm.id, mOriginalAlarm.enabled);
                } else {
                    mOriginalAlarm.enabled = false;
                }
                /* PR 626156- Neo Skunkworks - Paul Xu modified - 001 End */
            }
            // add by Yanjingming for pr539153 end
            /* PR 678654 - Neo Skunkworks - Paul Xu added - 001 Begin */
            mCancelAlarm = true;
            /* PR 678654 - Neo Skunkworks - Paul Xu added - 001 End */
            saveAlarm(mOriginalAlarm, false);
        }
        // add pr466531 xibin end
    }

    /* PR 626156- Neo Skunkworks - Paul Xu added - 001 Begin */
    /**
     * get alarm enable status.
     * 
     * @param boolean enableStatus
     * @return boolean
     */
    private boolean getAlarmEnableStatus(boolean enableStatus) {
        return enableStatus;
    }

    /* PR 626156- Neo Skunkworks - Paul Xu added - 001 End */

    /**
     * Display a toast that tells the user how long until the alarm goes off.
     * This helps prevent "am/pm" mistakes.
     */
    static void popAlarmSetToast(Context context, int hour, int minute, Alarm.DaysOfWeek daysOfWeek) {
        popAlarmSetToast(context, Alarms.calculateAlarm(hour, minute, daysOfWeek).getTimeInMillis());
    }

    static void popAlarmSetToast(Context context, long timeInMillis) {
        String toastText = formatToast(context, timeInMillis);
        Toast toast = Toast.makeText(context, toastText, Toast.LENGTH_LONG);
        ToastMaster.setToast(toast);
        toast.show();
    }

    /**
     * format "Alarm set for 2 days 7 hours and 53 minutes from now"
     */
    static String formatToast(Context context, long timeInMillis) {
        long delta = timeInMillis - System.currentTimeMillis() + 60 * 1000;
        long hours = delta / (1000 * 60 * 60);
        long minutes = delta / (1000 * 60) % 60;
        long days = hours / 24;
        hours = hours % 24;
        Log.d("haiying.he", "minutes : " + minutes);

        String daySeq = (days == 0) ? "" : (days == 1) ? context.getString(R.string.day) : context
                .getString(R.string.days, Long.toString(days));

        String minSeq = (minutes == 0) ? "" : (minutes == 1) ? context.getString(R.string.minute)
                : context.getString(R.string.minutes, Long.toString(minutes));

        String hourSeq = (hours == 0) ? "" : (hours == 1) ? context.getString(R.string.hour)
                : context.getString(R.string.hours, Long.toString(hours));

        boolean dispDays = days > 0;
        boolean dispHour = hours > 0;
        boolean dispMinute = minutes > 0;

        int index = (dispDays ? 1 : 0) | (dispHour ? 2 : 0) | (dispMinute ? 4 : 0);

        String[] formats = context.getResources().getStringArray(R.array.alarm_set);
        return String.format(formats[index], daySeq, hourSeq, minSeq);
    }

    private void updateRingtoneName(String ringtone) {
        // modify by Yanjingming for pr484826 begin
        if ("Silent".equals(ringtone)) {
            mRingtoneTv.setText(getResources().getString(R.string.timer_ringtone_silent));
        } else {
            // PR 590654 - Neo Skunkworks - Soar Gao - 001 begin
            if (!formatRingtone(ringtone).equals(formatRingtone(mDefaultRingtoneName))) {
                mRingtoneTv.setText(formatRingtone(ringtone));// modify by
                                                              // Yanjingming for
                                                              // pr543246
            } else {
                mRingtoneTv.setText(getString(R.string.default_ringtone) + " ( "
                        + formatRingtone(ringtone) + " )");
            }
            // PR 590654 - Neo Skunkworks - Soar Gao - 001 end
        }
        // modify by Yanjingming for pr484826 end
    }

    private BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals(ACTION_NAME)) {
                mRingtoneName = intent.getStringExtra("ringtone");
                mRingtonePath = intent.getStringExtra("ringtonePath");
                preRingtoneName = mRingtoneName;
                preRingtonePath = mRingtonePath;
                updateRingtoneName(mRingtoneName);
                mVolumePref.setCurRingtone(mRingtonePath);
                // PR:491892 add by XIBIN start
                AudioManager audioManager = (AudioManager) context
                        .getSystemService(Context.AUDIO_SERVICE);
                 int volume = audioManager.getStreamVolume(AudioManager.STREAM_ALARM);
                 mVolumePref.setVoume(volume);
                // PR:491892 add by XIBIN end
                enableWhenDataChange = true; // Add by xiaolong.que@tcl.com for
                                             // BUG-420462
            }
        }
    };

    // modify by Yanjingming for pr502505 begin
    private void getScreenResolution() {
        DisplayMetrics displaysMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displaysMetrics);
        SCREEN_WIDTH = displaysMetrics.widthPixels;
        SCREEN_HEIGHT = displaysMetrics.heightPixels;
    }

    public static int getScreenResolutionWidth() {
        return SCREEN_WIDTH;
    }

    public static int getScreenResolutionHeight() {
        return SCREEN_HEIGHT;
    }

    // modify by Yanjingming for pr502505 end

    public void registerBoradcastReceiver() {
        IntentFilter myIntentFilter = new IntentFilter();
        myIntentFilter.addAction(ACTION_NAME);
        registerReceiver(mBroadcastReceiver, myIntentFilter);
    }

    // add by Yanjingming for pr543246 begin
    private String formatRingtone(String name) {
        // PR 605347 - Neo Skunkworks - Soar Gao - 002 begin
        // String
        // nameExtension=name.substring(name.lastIndexOf('.'),name.length()).toLowerCase();
        // if(nameExtension.equals(".mp3")||nameExtension.equals(".ogg")||nameExtension.equals(".wav")||nameExtension.equals(".wma")||nameExtension.equals(".ape")||nameExtension.equals(".acc")){
        // PR 605347 - Neo Skunkworks - Soar Gao - 002 end
        // PR -613490 - Neo Skunworks - Soar Gao , -001 begin
        // Delete All extention
        if (HandleSetAlarm.mSilentRingTone) {
            return name;
        }
        if ((name != null) && (!("".equals(name)))) {
            name = name.substring(0, name.lastIndexOf('.'));
            name = name.replace("_", " ");
        } else {
            return "";
        }
        // PR -613490 - Neo Skunworks - Soar Gao , -001 end
        // }
        return name;
    }

    // add by Yanjingming for pr543246 end
    // PR738839-haiying.he start
    protected class MyScrollListener implements OnScrollListener {

        @Override
        public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount,
                int totalItemCount) {
            // do nothing
        }

        @Override
        public void onScrollStateChanged(AbsListView view, int scrollState) {
            if (SCROLL_STATE_TOUCH_SCROLL == scrollState) {
                View currentFocus = getCurrentFocus();
                if (currentFocus != null && mLabel != null
                        && currentFocus.getTag() == mLabel.getTag()) {
                    final InputMethodManager inputMethodManager = ((InputMethodManager) getApplicationContext()
                            .getSystemService(Context.INPUT_METHOD_SERVICE));
                    inputMethodManager.hideSoftInputFromWindow(mLabel.getWindowToken(), 0);
                    mLabel.clearFocus();
                }
            }
        }

    }
    // PR738839-haiying.he end
}
