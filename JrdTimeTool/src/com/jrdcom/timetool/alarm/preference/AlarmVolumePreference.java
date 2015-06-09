
package com.jrdcom.timetool.alarm.preference;

import android.content.Context;
import android.content.Intent;
import android.database.ContentObserver;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Handler;
import android.os.Parcel;
import android.os.Parcelable;
import android.preference.Preference;
import android.provider.Settings;
import android.provider.Settings.System;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnKeyListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;

import com.android.deskclock.R;
import com.jrdcom.timetool.alarm.activity.SetAlarm;
import com.jrdcom.timetool.alarm.provider.Alarms;
import com.jrdcom.timetool.countdown.service.MediaPlayerService;

public class AlarmVolumePreference extends Preference {

    LinearLayout layout;

    private int mStreamType;

    private SeekBarVolumizer mSeekBarVolumizer;

    private int defaultVolume = 2;

    private static int mVolume;

    static final String ACTION_NAME = "UPDATE_RINGTIONE_NAME";

    private String mCurRingtonePath;

    private LinearLayout.LayoutParams params1;
    /* PR 687407 - Neo Skunkworks - Paul Xu modified - 001 Begin */
    private Context mPContext;
    /* PR 687407 - Neo Skunkworks - Paul Xu modified - 001 End */

    private static final int ALARM_STREAM_TYPE_BIT = 1 << AudioManager.STREAM_ALARM;// PR-710176-mingwei.han-add

    public AlarmVolumePreference(Context context) {
        super(context);
        /* PR 687407 - Neo Skunkworks - Paul Xu modified - 001 Begin */
        mPContext = context;
        /* PR 687407 - Neo Skunkworks - Paul Xu modified - 001 End */
    }

    public AlarmVolumePreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        /* PR 687407 - Neo Skunkworks - Paul Xu modified - 001 Begin */
        mPContext = context;
        /* PR 687407 - Neo Skunkworks - Paul Xu modified - 001 End */
    }

    public AlarmVolumePreference(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        /* PR 687407 - Neo Skunkworks - Paul Xu modified - 001 Begin */
        mPContext = context;
        /* PR 687407 - Neo Skunkworks - Paul Xu modified - 001 End */
    }

    @Override
    protected View onCreateView(ViewGroup parent) {
        // add PR470784 xibin start -- memory leak
        if (layout != null) {
            return layout;
        }
        // modified by haifeng.tang 2014.9.2 begin
        layout = (LinearLayout) LayoutInflater.from(getContext()).inflate(
                R.layout.alarm_volume_preference, null); 
        final SeekBar seekBar = (SeekBar) layout.findViewById(R.id.alarm_seekbar);
        mSeekBarVolumizer = new SeekBarVolumizer(getContext(), seekBar, AudioManager.STREAM_ALARM);
        seekBar.setOnKeyListener(new SeekBarOnKeyListener());
        // modified by haifeng.tang 2014.9.2 end
        return layout;
    }

    public int getVolume() {
        return mVolume;
    }

    public int getDefaultVolume() {
        return defaultVolume;
    }

    public void setDefaultVoume() {
        setVoume(defaultVolume);
    }

    public void setCurRingtone(String curRingtonePath) {
        mCurRingtonePath = curRingtonePath;
    }

    public void setVoume(int volume) {
        if (mSeekBarVolumizer != null) {
            mSeekBarVolumizer.postSetVolume(volume);
        }
        mVolume = volume;
    }

    public void onRevertVolume() {
        if (mSeekBarVolumizer != null) {
            mSeekBarVolumizer.revertVolume();
        }
        cleanup();
    }

    public void onSaveVolume() {
        // PR :470784 update by xibin start
        if (mSeekBarVolumizer != null) {
            mSeekBarVolumizer.stop();
        }
        mSeekBarVolumizer = null;
        // PR :470784 update by xibin end
    }

    /***
     * Do clean up. This can be called multiple times! 124
     */
    private void cleanup() {
        if (mSeekBarVolumizer != null) {
            mSeekBarVolumizer.revertVolume();
            // add PR455342 XIBIN -- Change timetool package name and class name
            // as deskclock
            mSeekBarVolumizer.stop();
        }

        mSeekBarVolumizer = null;
    }

    public void setStreamType(int streamType) {
        mStreamType = streamType;
    }

    protected void onSampleStarting(SeekBarVolumizer volumizer) {
        if (mSeekBarVolumizer != null && volumizer != mSeekBarVolumizer) {
            mSeekBarVolumizer.stopSample();
        }
    }

    @Override
    protected Parcelable onSaveInstanceState() {
        final Parcelable superState = super.onSaveInstanceState();
        if (isPersistent()) {
            // No need to save instance state since it's persistent
            return superState;
        }

        final SavedState myState = new SavedState(superState);
        if (mSeekBarVolumizer != null) {
            mSeekBarVolumizer.onSaveInstanceState();
        }
        myState.volume = mVolume;
        return myState;
    }

    @Override
    protected void onRestoreInstanceState(Parcelable state) {
        if (state == null || !state.getClass().equals(SavedState.class)) {
            // Didn't save state for us in onSaveInstanceState
            super.onRestoreInstanceState(state);
            return;
        }

        SavedState myState = (SavedState) state;
        super.onRestoreInstanceState(myState.getSuperState());
        if (mSeekBarVolumizer != null) {
            mSeekBarVolumizer.onRestoreInstanceState(myState);
        }
    }

    private final class SeekBarOnKeyListener implements OnKeyListener {
        @Override
        public boolean onKey(View v, int keyCode, KeyEvent event) {
            // If key arrives immediately after the activity has been
            // cleaned up.
            if (mSeekBarVolumizer == null)
                return true;
            boolean isdown = (event.getAction() == KeyEvent.ACTION_DOWN);
            switch (keyCode) {
                case KeyEvent.KEYCODE_VOLUME_DOWN:
                    if (isdown) {
                        mSeekBarVolumizer.changeVolumeBy(-1);
                    }
                    return true;
                case KeyEvent.KEYCODE_VOLUME_UP:
                    if (isdown) {
                        mSeekBarVolumizer.changeVolumeBy(1);
                    }
                    return true;
                case KeyEvent.KEYCODE_VOLUME_MUTE:
                    if (isdown) {
                        mSeekBarVolumizer.muteVolume();
                    }
                    return true;
                default:
                    return false;
            }
        }
    }

    private static class SavedState extends BaseSavedState {
        int volume;

        public SavedState(Parcel source) {
            super(source);
            volume = source.readInt();
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            super.writeToParcel(dest, flags);
            dest.writeInt(volume);
        }

        public SavedState(Parcelable superState) {
            super(superState);
        }

        public static final Parcelable.Creator<SavedState> CREATOR = new Parcelable.Creator<SavedState>() {
            public SavedState createFromParcel(Parcel in) {
                return new SavedState(in);
            }

            public SavedState[] newArray(int size) {
                return new SavedState[size];
            }
        };
    }

    public class SeekBarVolumizer implements OnSeekBarChangeListener, Runnable {

        private Context mContext;
        private Handler mHandler = new Handler();

        private AudioManager mAudioManager;
        private int mStreamType;
        private int mOriginalStreamVolume;
        private int mRingerMode;// PR 710176-mingwei.han-add

        private int mLastProgress = -1;
        private SeekBar mSeekBar;
        private int mVolumeBeforeMute = -1;
        private ContentObserver mVolumeObserver = new ContentObserver(mHandler) {
            @Override
            public void onChange(boolean selfChange) {
                super.onChange(selfChange);
                if (mSeekBar != null && mAudioManager != null) {
                    // int volume = mAudioManager.isStreamMute(mStreamType) ?
                    // mAudioManager
                    // .getLastAudibleStreamVolume(mStreamType)
                    // : mAudioManager.getStreamVolume(mStreamType);
                    int volume = mAudioManager.getStreamVolume(mStreamType);
                    mSeekBar.setProgress(volume);
                }
            }
        };

        public SeekBarVolumizer(Context context, SeekBar seekBar, int streamType) {
            this(context, seekBar, streamType, null);
        }

        public SeekBarVolumizer(Context context, SeekBar seekBar, int streamType, Uri defaultUri) {
            mContext = context;
            mAudioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
            mStreamType = streamType;
            mSeekBar = seekBar;
            initSeekBar(seekBar, defaultUri);
        }

        private void initSeekBar(SeekBar seekBar, Uri defaultUri) {
            seekBar.setMax(mAudioManager.getStreamMaxVolume(mStreamType));
            mOriginalStreamVolume = mAudioManager.getStreamVolume(mStreamType);
            // Noted by xiaxia.yao for PR:416598 begin
            //mOriginalStreamVolume = mVolume;
            // Noted by xiaxia.yao for PR:416598 begin
            mRingerMode = mAudioManager.getRingerMode();// PR
                                                        // 710176-mingwei.han-add
            seekBar.setProgress(mOriginalStreamVolume);
            seekBar.setOnSeekBarChangeListener(this);
            mContext.getContentResolver().registerContentObserver(
                    System.getUriFor(System.VOLUME_SETTINGS[mStreamType]), false, mVolumeObserver);
        }

        public void stop() {
            stopSample();
            mContext.getContentResolver().unregisterContentObserver(mVolumeObserver);
            mSeekBar.setOnSeekBarChangeListener(null);
        }

        public void revertVolume() {
            // PR 710176-mingwei.han-add begin
            int silentModeStreams = Settings.System.getInt(mContext.getContentResolver(),
                    Settings.System.MODE_RINGER_STREAMS_AFFECTED, 0);
            boolean result = (silentModeStreams & ALARM_STREAM_TYPE_BIT) == 0;
            if (result || mRingerMode != 0 && mRingerMode != 1) {
                mAudioManager.setStreamVolume(mStreamType, mOriginalStreamVolume, 0);
            }
            // PR 710176-mingwei.han-add end
        }

        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromTouch) {
            if (!fromTouch) {
                return;
            }

            postSetVolume(progress);
        }

        public void postSetVolume(int progress) {
            // Do the volume changing separately to give responsive UI
            mLastProgress = progress;
            mVolume = progress;
            mHandler.removeCallbacks(this);
            mHandler.post(this);
        }

        @Override
        public void onStartTrackingTouch(SeekBar arg0) {

        }

        @Override
        public void run() {
            mAudioManager.setStreamVolume(mStreamType, mLastProgress, 0);
            // PR:491892 add by XIBIN start
            if (mSeekBar != null) {
                mSeekBar.setProgress(mLastProgress);
            }
            // PR:491892 add by XIBIN end
        }

        @Override
        public void onStopTrackingTouch(SeekBar arg0) {
            startSample();
        }

        public void startSample() {
            // onSampleStarting(this);//add PR470784 xibin
            if (mCurRingtonePath != null) {
                stopSample();// add PR470784 xibin
                Intent intent = new Intent(mContext, MediaPlayerService.class);
                intent.putExtra(MediaPlayerService.MEDIA_FILE_PATH_EXTRA, mCurRingtonePath);
                intent.putExtra(MediaPlayerService.RUN_TIME_EXTRA, 5);
                intent.putExtra(MediaPlayerService.VOLUME, mVolume);
                mContext.startService(intent);
            }
        }

        public void stopSample() {
            mContext.stopService(new Intent(mContext, MediaPlayerService.class));
        }

        public SeekBar getSeekBar() {
            return mSeekBar;
        }

        public void changeVolumeBy(int amount) {
            mSeekBar.incrementProgressBy(amount);
            startSample();
            postSetVolume(mSeekBar.getProgress());
            mVolumeBeforeMute = -1;
        }

        public void muteVolume() {
            if (mVolumeBeforeMute != -1) {
                mSeekBar.setProgress(mVolumeBeforeMute);
                startSample();
                postSetVolume(mVolumeBeforeMute);
                mVolumeBeforeMute = -1;
            } else {
                mVolumeBeforeMute = mSeekBar.getProgress();
                mSeekBar.setProgress(0);
                stopSample();
                postSetVolume(0);
            }
        }

        public void onSaveInstanceState() {
            if (mLastProgress >= 0) {
                mVolume = mLastProgress;
            }
        }

        public void onRestoreInstanceState(SavedState myState) {
            if (myState.volume != -1) {
                mVolume = myState.volume;
                mLastProgress = mVolume;
                postSetVolume(mLastProgress);
            }
        }
    }
}
