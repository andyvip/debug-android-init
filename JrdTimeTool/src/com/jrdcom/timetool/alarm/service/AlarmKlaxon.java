/*
 * Copyright (C) 2008 The Android Open Source Project
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

package com.jrdcom.timetool.alarm.service;

import java.io.File;
import java.lang.reflect.Method;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.AssetFileDescriptor;
import android.content.res.Resources;
import android.database.Cursor;
import android.drm.DrmManagerClient;
import android.media.AudioManager;
import android.media.AudioManager.OnAudioFocusChangeListener;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnErrorListener;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;

import com.android.deskclock.R;
import com.jrdcom.timetool.alarm.AlarmAlertWakeLock;
import com.jrdcom.timetool.alarm.activity.AlarmAlertFullScreen;
import com.jrdcom.timetool.alarm.activity.SettingsActivity;
import com.jrdcom.timetool.alarm.provider.Alarm;
import com.jrdcom.timetool.alarm.provider.Alarms;
import com.jrdcom.timetool.alarm.provider.RingtoneList;
//add by fan.yang PR961592 start
import android.provider.Settings;
//add by fan.yang PR961592 end
/**
 * Manages alarms and vibe. Runs as a service so that it can continue to play if
 * another activity overrides the AlarmAlert dialog.
 */
public class AlarmKlaxon extends Service {
	ReflectionTool reflectionTool=new ReflectionTool();
    // Default of 10 minutes until alarm is silenced.
    private static final boolean DEBUG = false;
    private static final String DEBUG_STRING = "jrdtimetool";
    private static final String DEFAULT_ALARM_TIMEOUT = "10";

    private static final long[] sVibratePattern = new long[] {
            500, 500
    };

    private boolean mPlaying = false;
    private Vibrator mVibrator;
    private MediaPlayer mMediaPlayer;
    private Alarm mCurrentAlarm;
    private long mStartTime;
    private TelephonyManager mTelephonyManager;
    private int mInitialCallState;

    // add by caorongxing for PR:433114 start
    static final String ALARM_REQUEST_SHUTDOWN_ACTION = "android.intent.action.ACTION_ALARM_REQUEST_SHUTDOWN";
    static final String NORMAL_SHUTDOWN_ACTION = "android.intent.action.normal.shutdown";
    protected static int mCurrentPlayingAlarmId = -1;
    private Context mContext;
    private static final int STOP_SERVICE = 0;
    // add by caorongxing for PR:433114 end
    // Internal messages
    private static final int KILLER = 1000;
    /* FR 548923- Neo Skunkworks - Paul Xu added - 001 Begin */
    private static final int SNOOZE_ALARM = 1001;
    private static final int CARING_MODE = 1002;
    private static final int AUTO_SNOOZE_ALARM_TIME = 2 * 60 * 1000;
    private static final int DEFAULT_VOLUME = 4;
    private int mMaxVolume = 5;
    private int mCurrentVolume = 4;
    private AudioManager mAudioManager;
    private static final int STREAM_TYPE = AudioManager.STREAM_ALARM;
    private static final int CARING_DELAY_TIME = 5 * 1000;// PR -604419 - Neo
                                                          // Skunworks - Soar
                                                          // Gao , add -001
    /* FR 548923- Neo Skunkworks - Paul Xu added - 001 End */
    // add by caorongxing for PR:433114 start
    private boolean isAlarmBoot = false;
    // add by caorongxing for PR:433114 end
    private Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case KILLER:
                    sendKillBroadcast((Alarm) msg.obj);
                    // stopSelf(); //remove by Yan Jingming for pr547942
                    break;
                // add by caorongxing for PR:433114 start
                case STOP_SERVICE:
                    Log.v("jrdtimetool", "stop alarmklaxon service ... ");
                    PowerOffAlarmService.shutDown(mContext);
                    stopSelf();
                    break;
                // add by caorongxing for PR:433114 end
                /* FR 548923- Neo Skunkworks - Paul Xu added - 001 Begin */
                case CARING_MODE:
                    if (shouldStop()) {
                        return;
                    }
                    increaseVolume();
                    if (mHandler != null) {
                        mHandler.removeMessages(CARING_MODE);
                    }
                    sendEmptyMessageDelayed(CARING_MODE, CARING_DELAY_TIME);
                    break;
                case SNOOZE_ALARM:
                    sendAutoSnoozeBroadcast((Alarm) msg.obj);
                    break;
                /* FR 548923- Neo Skunkworks - Paul Xu added - 001 End */
            }
        }
    };

    private boolean mPreSpeakerOn = false;

    private PhoneStateListener mPhoneStateListener = new PhoneStateListener() {
        @Override
        public void onCallStateChanged(int state, String ignored) {
            // The user might already be in a call when the alarm fires. When
            // we register onCallStateChanged, we get the initial in-call state
            // which kills the alarm. Check against the initial call state so
            // we don't kill the alarm during a call.
            if (state != TelephonyManager.CALL_STATE_IDLE && state != mInitialCallState) {
                sendKillBroadcast(mCurrentAlarm);
                stopSelf();
            }
        }
    };

    // PR : 486153 add by XIBIN start --Music isn't interrupted when alarm ring
    /*
     * private AudioManager audioManager; private OnAudioFocusChangeListener
     * mAudioFocusListener = new OnAudioFocusChangeListener() { public void
     * onAudioFocusChange(int focusChange) { } };
     */
    // PR : 486153 add by XIBIN end
    @Override
    public void onCreate() {
        /* FR 548923- Neo Skunkworks - Paul Xu added - 001 Begin */
        mAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        /* FR 548923- Neo Skunkworks - Paul Xu added - 001 End */
        mVibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        // Listen for incoming calls to kill the alarm.
        mTelephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        /* PR 560386- Neo Skunkworks - Paul Xu modified - 001 Begin */
        /*
         * mTelephonyManager.listen( mPhoneStateListener,
         * PhoneStateListener.LISTEN_CALL_STATE);
         */
        if (mTelephonyManager != null) {
            mTelephonyManager.listen(mPhoneStateListener, PhoneStateListener.LISTEN_CALL_STATE);
        }
        /* PR 560386- Neo Skunkworks - Paul Xu modified - 001 End */
        mContext = AlarmKlaxon.this; // add by caorongxing for PR:433114
        AlarmAlertWakeLock.acquireCpuWakeLock(this);
        // add by caorongxing for PR:433114 start
        IntentFilter filter = new IntentFilter("stop_ringtone");
        registerReceiver(stopPlayReceiver, filter);
        // add by caorongxing for PR:433114 end
        // PR : 486153 add by XIBIN start --Music isn't interrupted when alarm
        // ring
        /*
         * audioManager = (AudioManager)
         * getSystemService(Context.AUDIO_SERVICE);
         * audioManager.requestAudioFocus(mAudioFocusListener,
         * AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);
         */
        // PR : 486153 add by XIBIN end
    }

    @Override
    public void onDestroy() {
        stop();
        // Stop listening for incoming calls.
        /* PR 560386- Neo Skunkworks - Paul Xu modified - 001 Begin */
        /*
         * mTelephonyManager.listen(mPhoneStateListener, 0);
         */
        // PR746297-mingwei.han-add begin
        int CallState = 0;
        if (mTelephonyManager != null) {
            CallState = mTelephonyManager.getCallState();
            // PR746297-mingwei.han-add end
            mTelephonyManager.listen(mPhoneStateListener, 0);
        }
        /* PR 560386- Neo Skunkworks - Paul Xu modified - 001 End */
        AlarmAlertWakeLock.releaseCpuLock();
        // add by caorongxing for PR:433114
        unregisterReceiver(stopPlayReceiver);
        mCurrentPlayingAlarmId = -1;
        // add by caorongxing for PR:433114
        /* FR 548923- Neo Skunkworks - Paul Xu added - 001 Begin */
        // add by haifeng.tang Pr789729 2014.9.16 begin
        resetAlarmVolume();
        // add by haifeng.tang Pr789729 2014.9.16 end

        if (mHandler != null) {
            /* PR 590481- Neo Skunkworks - Paul Xu added - 001 Begin */
            mHandler.removeMessages(SNOOZE_ALARM);
            /* PR 590481- Neo Skunkworks - Paul Xu added - 001 End */
            mHandler.removeMessages(CARING_MODE);
        }
        /* FR 548923- Neo Skunkworks - Paul Xu added - 001 End */
        /* PR 613533- Neo Skunkworks - Paul Xu added - 001 Begin */
        // PR746297-mingwei.han-add begin
        if (CallState == TelephonyManager.CALL_STATE_IDLE) {
            Alarms.savePlayingAlarmID(AlarmKlaxon.this, mCurrentPlayingAlarmId);
        }
        // PR746297-mingwei.han-add end
        AlarmAlertFullScreen.setDissmissOrSnoozeState(false);
        /* PR 613533- Neo Skunkworks - Paul Xu added - 001 End */
    }

    private void resetAlarmVolume() {
        // PR860545 by xing.zhao  [Force close][Time]Time force close after clear it from recent app.  begin
        if (needCaringMode() && mMaxVolume > 0) {
        // PR860545 by xing.zhao  [Force close][Time]Time force close after clear it from recent app.  end
            mAudioManager.setStreamVolume(STREAM_TYPE, mMaxVolume, 0);
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // add by caorongxing for PR:433114 begin
        if (Alarms.bootFromPoweroffAlarm()) {
            mCurrentAlarm = null;
        }
        // add by caorongxing for PR:433114 end
        if (intent == null) {
            stopSelf();
            return START_NOT_STICKY;
        }
        /* PR:415395 20130315 hengfeng.liu added start */
        final Alarm alarm;
        boolean isAlarmBoot = intent.getBooleanExtra("isAlarmBoot", false);
        if (isAlarmBoot) {
            // PR700067-Neo Skunkworks-kehao.wei-001 modify begin
            // alarm = Alarms.getNearestAlarm(this);
            alarm = Alarms.getLatestAlarm(this);
            // PR700067-Neo Skunkworks-kehao.wei-001 modify end
            if (alarm == null) {
                // Make sure we set the next alert if needed.
                Alarms.setNextAlert(this);
                return START_NOT_STICKY;
            }
            // Disable the snooze alert if this alarm is the snooze.
            Alarms.disableSnoozeAlert(this, alarm.id);
            // Disable this alarm if it does not repeat.
            if (alarm.daysOfWeek.isRepeatSet()) {
                // Enable the next alert if there is one. The above call to
                // enableAlarm will call setNextAlert so avoid calling it twice.
                Alarms.setNextAlert(this);
            } else {
                Alarms.enableAlarm(this, alarm.id, false);
            }
        } else {
            alarm = intent.getParcelableExtra(Alarms.ALARM_INTENT_EXTRA);
        }
        /* PR:415395 20130315 hengfeng.liu added end */

        if (alarm == null) {
            stopSelf();
            return START_NOT_STICKY;
        }
        /* FR 548923- Neo Skunkworks - Paul Xu added - 001 Begin */
        mMaxVolume = mAudioManager.getStreamVolume(AudioManager.STREAM_ALARM);// alarm.volume;
        Log.e("huihui","mMaxVolume  "+mMaxVolume);
        if (DEBUG) {
            Log.d(DEBUG_STRING, "onStartCommand mMaxVolume:" + mMaxVolume + "--alarm.volume:"
                    + alarm.volume);
        }
        /* FR 548923- Neo Skunkworks - Paul Xu added - 001 End */
        // add by caorongxing for PR:433114 begin
        if (alarm != null && Alarms.bootFromPoweroffAlarm()) {
            updatePoweroffAlarmLabel(alarm.label);
        }
        // add by caorongxing for PR:433114 end
        if (mCurrentAlarm != null) {
            sendKillBroadcast(mCurrentAlarm);
        }

        // PR:528456 add by Yanjingming start
        if (Alarms.bootFromPoweroffAlarm() && (!alarm.silent)
                && (!(new File(alarm.alertPath).exists()))) {
            tempAlarm = alarm;
            handler.sendEmptyMessage(0);
        } else {
            playAlarm(alarm);
        }
        // PR:528456 add by Yanjingming end
        return START_STICKY;
    }

    private void sendKillBroadcast(Alarm alarm) {
        long millis = System.currentTimeMillis() - mStartTime;
        int minutes = (int) Math.round(millis / 60000.0);
        Intent alarmKilled = new Intent(Alarms.ALARM_KILLED);
        alarmKilled.putExtra(Alarms.ALARM_INTENT_EXTRA, alarm);
        alarmKilled.putExtra(Alarms.ALARM_KILLED_TIMEOUT, minutes);
        sendBroadcast(alarmKilled);
    }

    // Volume suggested by media team for in-call alarms.
    private static final float IN_CALL_VOLUME = 0.125f;

    // PR 425324 add by carongxing begin
    public void updateAlarmRingtone(Alarm alarm) {
        /* FR 548923- Neo Skunkworks - Paul Xu modified - 001 Begin */
        ContentValues values = new ContentValues(12);
        /* FR 548923- Neo Skunkworks - Paul Xu modified - 001 End */
        values.put(Alarm.Columns.ALERT, alarm.alert);
        values.put(Alarm.Columns.RINGTONE_PATH, alarm.alertPath);
        updateRingtonePath(this, values, alarm);
    }

    private void updateRingtonePath(Context context, ContentValues values, Alarm alarm) {
        // Uri uri =
        // context.getContentResolver().insert(Alarm.Columns.CONTENT_URI,
        // values); // PR -559339 - Neo Skunworks - Soar Gao , delete -001
        ContentResolver resolver = context.getContentResolver();
        resolver.update(ContentUris.withAppendedId(Alarm.Columns.CONTENT_URI, alarm.id), values,
                null, null);
    }


    private static final int ALARM_STREAM_TYPE_BIT = 1 << AudioManager.STREAM_ALARM;// add by fan.yang PR961592
    // PR 425324 add by carongxing end
    private void play(Alarm alarm) {
        // stop() checks to see if we are already playing.
        stop();
        // modified by fan.yang PR945269 2015.3.11 begin
        // modified by haifeng.tang 789729 2014.9.16 begin
        int alarmVolume = alarm.volume;
        // remove by fan.yang PR972489 2015.4.13 start
        // add by fan.yang PR961592 2015.4.7 start
//        boolean isSilentFlag = (mAudioManager.getRingerMode() == AudioManager.RINGER_MODE_SILENT) ? true: false;
//        int silentModeStreams = Settings.System.getInt(mContext.getContentResolver(),
//                Settings.System.MODE_RINGER_STREAMS_AFFECTED, 0);
//        boolean result = (silentModeStreams & ALARM_STREAM_TYPE_BIT) == 0;//alarm in silent mode checkbox is checked
//        if(isSilentFlag && result){
//        	alarmVolume = mMaxVolume;
//        }
        // add by fan.yang PR972489 2015.4.13 end
        // remove by fan.yang PR961592 2015.4.7 start
        if (!alarm.silent && alarmVolume > 0) {
            // modified by haifeng.tang Pr789729 2014.9.16 end
        	// modified by fan.yang PR945269 2015.3.11 end
            String alert = alarm.alert;
            String alertPath = alarm.alertPath;
            // PR 425324 add by carongxing begin
            File file = new File(alertPath);
            if (!file.exists()) {
                // PR539763 default ringtone to ring. begin
                Uri defaultUri = RingtoneManager.getActualDefaultRingtoneUri(this,
                        RingtoneManager.TYPE_ALARM);
                if (defaultUri != null) {
                    Cursor cursor = getContentResolver().query(defaultUri, new String[] {
                        "_data"
                    }, null, null, null);
                    if (cursor != null && cursor.moveToFirst()) {
                        alertPath = cursor.getString(0);
                        // PR578695-Zonghua-Jin-001-begin
                        /*
                         * PR 661666 - Neo Skunkworks - Paul Xu modified - 001
                         * Begin
                         */
                        try {
                            if (reflectionTool.isMTKDrmEnable()
                                    && reflectionTool.isDrm(alertPath)) {
                                if (!isValidDrmRingtone(alertPath)) {
                                    if (RingtoneList.getRingtoneList(this) != null
                                            && RingtoneList.getRingtoneList(this).length > 1) {
                                        alert = RingtoneList.getRingtoneList(this)[1];
                                    } else {
                                        alert = RingtoneList.getRingtoneList(this)[0];
                                    }
                                    alertPath = RingtoneList.mediaFiles[0].getAbsolutePath();
                                }
                            }
                        } catch (NoSuchMethodError ex) {
                            if (RingtoneList.getRingtoneList(this) != null
                                    && RingtoneList.getRingtoneList(this).length > 1) {
                                alert = RingtoneList.getRingtoneList(this)[1];
                            } else {
                                alert = RingtoneList.getRingtoneList(this)[0];
                            }
                            alertPath = RingtoneList.mediaFiles[0].getAbsolutePath();
                        }
                        /*
                         * PR 661666 - Neo Skunkworks - Paul Xu modified - 001
                         * End
                         */
                        // PR578695-Zonghua-Jin-001-end
                    }
                } else {
                    /* PR 567783- Neo Skunkworks - Paul Xu modified - 001 Begin */
                    /*
                     * alert = RingtoneList.getRingtoneList(this)[1];
                     */
                    if (RingtoneList.getRingtoneList(this).length > 1) {
                        alert = RingtoneList.getRingtoneList(this)[1];
                    } else {
                        alert = RingtoneList.getRingtoneList(this)[0];
                    }
                    /* PR 567783- Neo Skunkworks - Paul Xu modified - 001 End */
                    alertPath = RingtoneList.mediaFiles[0].getAbsolutePath();
                }
                // PR539763 default ringtone to ring. end
                alarm.alert = alert;
                alarm.alertPath = alertPath;
                if (alertPath != null && alert != null) {
                    updateAlarmRingtone(alarm);
                }
            } else {// PR578695-Zonghua-Jin-001-begin
                /* PR 661666 - Neo Skunkworks - Paul Xu modified - 001 Begin */
                try {
                    if (reflectionTool.isMTKDrmEnable() && reflectionTool.isDrm(alertPath)) {
                        if (!isValidDrmRingtone(alertPath)) {
                            if (RingtoneList.getRingtoneList(this) != null
                                    && RingtoneList.getRingtoneList(this).length > 1) {
                                alert = RingtoneList.getRingtoneList(this)[1];
                            } else {
                                alert = RingtoneList.getRingtoneList(this)[0];
                            }
                            alertPath = RingtoneList.mediaFiles[0].getAbsolutePath();
                        }
                    }
                } catch (NoSuchMethodError ex) {
                    if (RingtoneList.getRingtoneList(this) != null
                            && RingtoneList.getRingtoneList(this).length > 1) {
                        alert = RingtoneList.getRingtoneList(this)[1];
                    } else {
                        alert = RingtoneList.getRingtoneList(this)[0];
                    }
                }
                /* PR 661666 - Neo Skunkworks - Paul Xu modified - 001 End */
            }
            // PR578695-Zonghua-Jin-001-end
            // PR 425324 add by carongxing end

            // Fall back on the default alarm if the database does not have an
            // alarm stored.
            if (alert == null) {
                alert = RingtoneList.ALERT_SILENT_PATH;
            }

            // TODO: Reuse mMediaPlayer instead of creating a new one and/or use
            // RingtoneManager.
            mMediaPlayer = new MediaPlayer();
            mMediaPlayer.setOnErrorListener(new OnErrorListener() {
                public boolean onError(MediaPlayer mp, int what, int extra) {
                    mp.stop();
                    mp.release();
                    mMediaPlayer = null;
                    return true;
                }
            });

            try {
                // Check if we are in a call. If we are, use the in-call alarm
                // resource at a low volume to not disrupt the call.
                /* PR 560386- Neo Skunkworks - Paul Xu modified - 001 Begin */
                /*
                 * if (mTelephonyManager.getCallState() !=
                 * TelephonyManager.CALL_STATE_IDLE) {
                 */
                if (mTelephonyManager != null
                        && mTelephonyManager.getCallState() != TelephonyManager.CALL_STATE_IDLE) {
                    /* PR 560386- Neo Skunkworks - Paul Xu modified - 001 End */
                    mMediaPlayer.setVolume(IN_CALL_VOLUME, IN_CALL_VOLUME);
                    setDataSourceFromResource(getResources(), mMediaPlayer, R.raw.in_call_alarm);
                } else {
                    mMediaPlayer.setDataSource(alertPath);
                }
                startAlarm(mMediaPlayer,alarmVolume); // modified by fan.yang PR945269 2015.3.11 
            } catch (Exception ex) {
                // The alert may be on the sd card which could be busy right
                // now. Use the fallback ringtone.
                try {
                    // Must reset the media player to clear the error state.
                    mMediaPlayer.reset();
                    setDataSourceFromResource(getResources(), mMediaPlayer, R.raw.fallbackring);
                    startAlarm(mMediaPlayer,alarmVolume);// modified by fan.yang PR945269 2015.3.11 
                } catch (Exception ex2) {
                    // At this point we just don't play anything.
                }
            }
            /* FR 548923- Neo Skunkworks - Paul Xu added - 001 Begin */
            if (alarm.volume > 0 && needCaringMode()) {

                sendCaringModeMessage(alarm);
            }
            /* FR 548923- Neo Skunkworks - Paul Xu added - 001 End */
        }

        /* Start the vibrator after everything is ok with the media player */
        if (alarm.vibrate) {
            mVibrator.vibrate(sVibratePattern, 0);
        } else {
            mVibrator.cancel();
        }
        /* FR 548923- Neo Skunkworks - Paul Xu modified - 001 Begin */
        /*
         * enableKiller(alarm);
         */
        autoSnoozeCurrAlarm(alarm);
        /* FR 548923- Neo Skunkworks - Paul Xu modified - 001 End */
        mPlaying = true;
        mStartTime = System.currentTimeMillis();
    }

    /* PR 614651- Neo Skunkworks - Paul Xu added - 001 Begin */
    private boolean needCaringMode() {
        // boolean result =
        // getResources().getBoolean(R.bool.def_alarm_need_caring_mode);
        boolean result = Alarms.getBoolean(mContext, "def_alarm_need_caring_mode");

        return result;
    }

    /* PR 614651- Neo Skunkworks - Paul Xu added - 001 End */
    
    // Do the common stuff when starting the alarm.
    // modified by fan.yang PR945269 2015.3.11 start 
    private void startAlarm(MediaPlayer player,int alarmVolume) throws java.io.IOException,
            IllegalArgumentException, IllegalStateException {
        final AudioManager audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        // PR534349 Ringtone does not come from loudspeaker when incoming a
        // alarm in earphone mode begin.
        mPreSpeakerOn = audioManager.isSpeakerphoneOn();
        audioManager.setSpeakerphoneOn(true);
        // Ringtone does not come from loudspeaker when incoming a alarm in
        // earphone mode end

        // do not play alarms if stream volume is 0
        // (typically because ringer mode is silent).
        if (alarmVolume != 0) {
            // PR486560 :Popup video and countdown remind work at the same time.
            // add by qjz 20130712 begin
            ((AudioManager) getSystemService(Context.AUDIO_SERVICE)).requestAudioFocus(
                    mAudioFocusChangeListener, AudioManager.STREAM_MUSIC,
                    AudioManager.AUDIOFOCUS_GAIN_TRANSIENT);
            // PR486560 :Popup video and countdown remind work at the same time.
            // add by qjz 20130712 begin
            player.setAudioStreamType(AudioManager.STREAM_ALARM);
            player.setLooping(true);
            player.prepare();
            player.start();
        }
    }
    // modified by fan.yang PR945269 2015.3.11 end

    // PR486560 :Popup video and countdown remind work at the same time.
    // add by qjz 20130712 begin
    OnAudioFocusChangeListener mAudioFocusChangeListener = new OnAudioFocusChangeListener() {
        public void onAudioFocusChange(int focusChange) {
            if (focusChange == AudioManager.AUDIOFOCUS_LOSS_TRANSIENT) {
                // Pause playback
                if (mMediaPlayer != null && mMediaPlayer.isPlaying()) {
                    mMediaPlayer.pause();
                }
            } else if (focusChange == AudioManager.AUDIOFOCUS_LOSS) {
                // Stop playback
                if (mMediaPlayer != null && mMediaPlayer.isPlaying()) {
                    mMediaPlayer.stop();
                }
                // add by qjz for PR497097 20130802
                ((AudioManager) getSystemService(Context.AUDIO_SERVICE))
                        .abandonAudioFocus(mAudioFocusChangeListener);
            }// PR717763-mingwei.han-add begin
            else if (focusChange == AudioManager.AUDIOFOCUS_GAIN) {
                if (mMediaPlayer != null && !mMediaPlayer.isPlaying()) {
                    mMediaPlayer.start();
                }
            }
            // PR717763-mingwei.han-add end
        }
    };

    // PR486560 :Popup video and countdown remind work at the same time.
    // add by qjz 20130712 end

    private void setDataSourceFromResource(Resources resources, MediaPlayer player, int res)
            throws java.io.IOException {
        AssetFileDescriptor afd = resources.openRawResourceFd(res);
        if (afd != null) {
            player.setDataSource(afd.getFileDescriptor(), afd.getStartOffset(), afd.getLength());
            afd.close();
        }
    }

    /**
     * Stops alarm audio and disables alarm if it not snoozed and not repeating
     */
    public void stop() {
        if (mPlaying) {
            mPlaying = false;

            Intent alarmDone = new Intent(Alarms.ALARM_DONE_ACTION);
            sendBroadcast(alarmDone);
            /* PR 690468 - Neo Skunkworks - Paul Xu modified - 001 Begin */
            ((AudioManager) getSystemService(Context.AUDIO_SERVICE))
                    .abandonAudioFocus(mAudioFocusChangeListener);
            // Stop audio playing
            if (mMediaPlayer != null) {
                mMediaPlayer.stop();
                mMediaPlayer.release();
                mMediaPlayer = null;
            }

            // add by qjz for PR497097 20130802
            /*
             * ((AudioManager)
             * getSystemService(Context.AUDIO_SERVICE)).abandonAudioFocus
             * (mAudioFocusChangeListener);
             */
            // Stop vibrator
            /* PR 690468 - Neo Skunkworks - Paul Xu modified - 001 End */
            mVibrator.cancel();

            // PR534349 Ringtone does not come from loudspeaker when incoming a
            // alarm in earphone mode begins.
            final AudioManager audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
            if (!mPreSpeakerOn) {
                audioManager.setSpeakerphoneOn(false);
            }
            // PR534349 Ringtone does not come from loudspeaker when incoming a
            // alarm in earphone mode ends.
        }
        /* PR 575988- Neo Skunkworks - Paul Xu deleted - 001 Begin */
        /*
         * disableKiller();
         */
        /* PR 575988- Neo Skunkworks - Paul Xu deleted - 001 End */
    }

    /**
     * Kills alarm audio after ALARM_TIMEOUT_SECONDS, so the alarm won't run all
     * day. This just cancels the audio, but leaves the notification popped, so
     * the user will know that the alarm tripped.
     */
    private void enableKiller(Alarm alarm) {
        final String autoSnooze = PreferenceManager.getDefaultSharedPreferences(this).getString(
                SettingsActivity.KEY_AUTO_SILENCE, DEFAULT_ALARM_TIMEOUT);
        int autoSnoozeMinutes = Integer.parseInt(autoSnooze);
        // modify by Yanjingming for pr512019 begin
        if (autoSnoozeMinutes <= 0) {
            autoSnoozeMinutes = Integer.parseInt(DEFAULT_ALARM_TIMEOUT);
        }
        if (DEBUG) {
            Log.d(DEBUG_STRING, "enableKiller autoSnoozeMinutes:" + autoSnoozeMinutes);
        }
        /* PR 575988- Neo Skunkworks - Paul Xu added - 001 Begin */
        disableKiller();
        /* PR 575988- Neo Skunkworks - Paul Xu added - 001 End */
        if (mHandler != null) {
            mHandler.sendMessageDelayed(mHandler.obtainMessage(KILLER, alarm),
                    1000 * autoSnoozeMinutes * 60);
        }
        // modify by Yanjingming for pr512019 end
    }

    /* FR 548923- Neo Skunkworks - Paul Xu added - 001 Begin */
    private void autoSnoozeCurrAlarm(Alarm alarm) {
        if (mHandler != null) {
            mHandler.removeMessages(SNOOZE_ALARM);
            mHandler.sendMessageDelayed(mHandler.obtainMessage(SNOOZE_ALARM, alarm),
                    AUTO_SNOOZE_ALARM_TIME);
        }
    }

    private void sendAutoSnoozeBroadcast(Alarm alarm) {
        Intent autoSnoozeAlarm = new Intent(Alarms.ALARM_AUTO_SNOOZE_ACTION);
        autoSnoozeAlarm.putExtra(Alarms.ALARM_INTENT_EXTRA, alarm);
        sendBroadcast(autoSnoozeAlarm);
    }

    private void sendCaringModeMessage(Alarm alarm) {
        setVolume(DEFAULT_VOLUME);
        if (mHandler != null) {
            mHandler.removeMessages(CARING_MODE);
            mHandler.sendEmptyMessage(CARING_MODE);
        }
    }

    private boolean shouldStop() {
        if (mCurrentVolume > mMaxVolume) {
            mCurrentVolume = mMaxVolume;

            return true;
        } else {
            return false;
        }
    }

    private void setVolume(int pVolume) {
        mAudioManager.setStreamVolume(STREAM_TYPE, pVolume, 0);
    }

    private void increaseVolume() {
        setVolume(mCurrentVolume++);
    }

    /* FR 548923- Neo Skunkworks - Paul Xu added - 001 End */

    private void disableKiller() {
        if (mHandler != null) {
            mHandler.removeMessages(KILLER);
        }
    }

    // add by caorongxing for PR:433114 begin
    private BroadcastReceiver stopPlayReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals("stop_ringtone")) {
                if (mMediaPlayer != null) {
                    mMediaPlayer.stop();
                }
                // Alarms.backupRingtoneForPoweroffAlarm(getApplicationContext(),
                // mHandler);
                mVibrator.cancel();
                // PR746297-mingwei.han-add begin
                int palyalarmId = Alarms.getPlayingAlarmId(context);
                if (mCurrentAlarm != null && palyalarmId == mCurrentAlarm.id) {
                    Alarms.savePlayingAlarmID(context, -1);
                }
                // PR746297-mingwei.han-add end
                shutDown();
                // PowerOffAlarmService.shutDown(mContext);
                /* PR 675131 - Neo Skunkworks - Paul Xu added - 001 Begin */
                stopSelf();
                /* PR 675131 - Neo Skunkworks - Paul Xu added - 001 End */
            }
        }
    };

    // add by caorongxing for PR:426200 end
    // add by caorongxing for PR:426200 begin
    private void shutDown() {
        Log.v("jrdtimetool", "send shutdown broadcast: android.intent.action.normal.shutdown");
        Intent shutdownIntent = new Intent(NORMAL_SHUTDOWN_ACTION);
        sendBroadcast(shutdownIntent);
        Intent intent = new Intent(ALARM_REQUEST_SHUTDOWN_ACTION);
        intent.putExtra("android.intent.extra.KEY_CONFIRM", false); // Intent.EXTRA_KEY_CONFIRM
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

    // private void initTelephonyService() {
    // // Record the initial call state here so that the new alarm has the
    // // newest state.
    // if (mTelephonyService != null) {
    // try {
    // mCurrentCallState = mTelephonyService.getPreciseCallState();
    // } catch (RemoteException ex) {
    // Log.v("Catch exception when getPreciseCallState: ex = "
    // + ex.getMessage());
    // }
    // }
    // }

    private void updatePoweroffAlarmLabel(String label) {
        Intent intent = new Intent("update.power.off.alarm.label");
        intent.putExtra("label", (label == null ? "" : label));
        sendStickyBroadcast(intent);
    }

    // add by caorongxing for PR:433114 end

    // PR:528456 add by Yanjingming start
    private Alarm tempAlarm = null;
    private Handler handler = new Handler() {
        private static final int TIMEOUT = 30000;
        private int timeCount = 0;

        public void handleMessage(Message msg) {
            if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
                playAlarm(tempAlarm);
                return;
            }
            loop();
        }

        public void loop() {
            if (timeCount <= TIMEOUT) {
                timeCount += 1000;
                handler.sendEmptyMessageDelayed(0, 1000);
            } else {
                playAlarm(tempAlarm);
            }
        }
    };

    private void playAlarm(Alarm alarm) {
        play(alarm);
        mCurrentAlarm = alarm;
        // Record the initial call state here so that the new alarm has the
        // newest state.
        /* PR 560386- Neo Skunkworks - Paul Xu modified - 001 Begin */
        /*
         * mInitialCallState = mTelephonyManager.getCallState();
         */
        if (mTelephonyManager != null) {
            try {
                mInitialCallState = mTelephonyManager.getCallState();
            } catch (Exception ex) {
                mInitialCallState = TelephonyManager.CALL_STATE_IDLE;
            }
        }
        /* PR 560386- Neo Skunkworks - Paul Xu modified - 001 End */
        // add by caorongxing for PR:433114 begin
        mCurrentPlayingAlarmId = alarm.id;
        // add by caorongxing for PR:433114 end
        /* PR 613533- Neo Skunkworks - Paul Xu added - 001 Begin */
        Alarms.savePlayingAlarmID(AlarmKlaxon.this, mCurrentPlayingAlarmId);
        /* PR 613533- Neo Skunkworks - Paul Xu added - 001 End */
    }

    // PR:528456 add by Yanjingming end

    // PR578695-Zonghua-Jin-001-begin
    private boolean isValidDrmRingtone(String filePath) {
        DrmManagerClient drmManager = ReflectionTool.getInstance(this);
        boolean hasCount = ReflectionTool.hasCountConstraint(drmManager,filePath);
        boolean isValid = drmManager.checkRightsStatus(filePath, android.drm.DrmStore.Action.PLAY) == android.drm.DrmStore.RightsStatus.RIGHTS_VALID;
        if (hasCount || !isValid) {
            return false;
        }
        return true;
    }
    // PR578695-Zonghua-Jin-001-end
}
