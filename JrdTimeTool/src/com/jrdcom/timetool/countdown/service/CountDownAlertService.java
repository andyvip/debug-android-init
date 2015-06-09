/*
 *CountDownAlertService.
 *Author: Lychee Wan
 *Date:2009.07.03
 *Copyright (C) 2009 ArcherMind Technology, Inc.
 */

package com.jrdcom.timetool.countdown.service;

import java.io.IOException;
import com.jrdcom.timetool.countdown.activity.CountDownActivity;
import com.jrdcom.timetool.countdown.activity.CountDownChooseRingtoneDialogActivity;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.AudioManager.OnAudioFocusChangeListener;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnErrorListener;
import android.os.IBinder;

public class CountDownAlertService extends Service {
	private String mAlertPath;

	private MediaPlayer mMediaPlayer;

	private Thread timeThread;

	private int mTime = 60;

	class secondCountDownRunner implements Runnable {
		// @Override
		public void run() {
			while (!Thread.currentThread().isInterrupted()) {
				if (mTime == 0) {
					stopSelf();

				} else {
					mTime--;
					try {
						Thread.sleep(1000);
					} catch (InterruptedException e) {
						Thread.currentThread().interrupt();
					}
				}
			}
		}
	}

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	public void onCreate() {
		super.onCreate();

		SharedPreferences settings = getSharedPreferences(
				CountDownChooseRingtoneDialogActivity.TIMER_RPREFERENCES, 0);

		mAlertPath = settings.getString(CountDownChooseRingtoneDialogActivity.ALERT_RINGTONE_PATH_KEY,
				"");
		mMediaPlayer = new MediaPlayer();
		try {
			mMediaPlayer.reset();

			if (mAlertPath.equals("")) {

				mMediaPlayer.setDataSource(mAlertPath + "/"
						+ CountDownChooseRingtoneDialogActivity.ALERT_SILENT_PATH);

			} else
				mMediaPlayer.setDataSource(mAlertPath);
			mMediaPlayer.setAudioStreamType(AudioManager.STREAM_NOTIFICATION);
			mMediaPlayer.setLooping(true);
			mMediaPlayer.prepare();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (SecurityException e) {
			e.printStackTrace();
		} catch (IllegalStateException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		mMediaPlayer.setOnErrorListener(new OnErrorListener() {
			public boolean onError(MediaPlayer mp, int what, int extra) {
				android.util.Log.e("", "Error occurred while playing audio.");
				mp.stop();
				mp.release();
				mMediaPlayer = null;
				return true;
			}
		});

		mMediaPlayer.setOnCompletionListener(new OnCompletionListener() {

			public void onCompletion(MediaPlayer mp) {
				stopSelf();
			}

		});
	}

	public void onStart(Intent intent, int startId) {
        //PR486560 :Popup video and countdown remind work at the same time.
        //add by qjz 20130712 begin
        ((AudioManager) getSystemService(Context.AUDIO_SERVICE)).requestAudioFocus(
                mAudioFocusChangeListener, AudioManager.STREAM_MUSIC,
                AudioManager.AUDIOFOCUS_GAIN_TRANSIENT);
        //PR486560 :Popup video and countdown remind work at the same time.
        //add by qjz 20130712 end
        mMediaPlayer.start();
        timeThread = new Thread(new secondCountDownRunner());
        timeThread.start();
    }

    //PR486560 :Popup video and countdown remind work at the same time.
    //add by qjz 20130712 begin
    OnAudioFocusChangeListener mAudioFocusChangeListener = new OnAudioFocusChangeListener() {
        public void onAudioFocusChange(int focusChange) {
            if (focusChange == AudioManager.AUDIOFOCUS_LOSS_TRANSIENT) {
                // Pause playback
                if(mMediaPlayer!=null && mMediaPlayer.isPlaying()){
                    mMediaPlayer.pause();
                }
            } else if (focusChange == AudioManager.AUDIOFOCUS_LOSS) {
                // Stop playback
                if(mMediaPlayer!=null && mMediaPlayer.isPlaying()){
                    mMediaPlayer.stop();
                }
                //add by qjz for PR497097 20130802
                ((AudioManager) getSystemService(Context.AUDIO_SERVICE)).abandonAudioFocus(mAudioFocusChangeListener);
            }
        }
    };
    //PR486560 :Popup video and countdown remind work at the same time.
    //add by qjz 20130712 end

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mMediaPlayer != null) {
            mMediaPlayer.stop();
            mMediaPlayer.release();
            mMediaPlayer = null;
            //add by qjz for PR497097 20130802
            ((AudioManager) getSystemService(Context.AUDIO_SERVICE)).abandonAudioFocus(mAudioFocusChangeListener);
        }
        timeThread.interrupt();
    }
}
