package com.jrdcom.timetool.countdown.service;

import com.jrdcom.timetool.timer.activity.TimerActivity;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.AudioManager.OnAudioFocusChangeListener;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnErrorListener;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;

import java.io.IOException;

public class MediaPlayerServiceForScroll extends Service {

	public static MediaPlayer mMediaPlayer;

	public static final String SPEED_OF_SCROLL = "speedtoscroll";

	public static final String IS_RING_CHANGE = "ringtonechange";
	
	public static final String BTN_RINGTONE_PATH = "/system/media/audio/alarms/Alarm_Beep_01.ogg";

	public IBinder onBind(Intent i) {
		return null;
	}

	public void onCreate() {
		super.onCreate();

		mMediaPlayer = new MediaPlayer();
	}

	public void onStart(Intent intent, int startId) {
		if (mMediaPlayer.isPlaying()) {

			mMediaPlayer.stop();
			removeMessage();

		}

		mMediaPlayer = new MediaPlayer();
		mMediaPlayer.setOnCompletionListener(new OnCompletionListener() {
			public void onCompletion(MediaPlayer mp) {
				stopSelf();
			}
		});

		try {
			mMediaPlayer.reset();

			if (BTN_RINGTONE_PATH.equals("")) {
				mMediaPlayer.setDataSource(BTN_RINGTONE_PATH + "/"
						+ TimerActivity.ALERT_RINGTONE_PATH_KEY);
			} else
				mMediaPlayer.setDataSource(BTN_RINGTONE_PATH);
			mMediaPlayer.setAudioStreamType(AudioManager.STREAM_NOTIFICATION);
			mMediaPlayer.setLooping(true);
			mMediaPlayer.prepare();
		} catch (IllegalArgumentException e) {
			return;
		} catch (IllegalStateException e) {
			return;
		} catch (IOException e) {
			return;
		}

		mMediaPlayer.setOnErrorListener(new OnErrorListener() {
			public boolean onError(MediaPlayer mp, int what, int extra) {
				mp.stop();
				mp.release();
				mMediaPlayer = null;
				stopSelf();
				return true;
			}
		});

        //PR486560 :Popup video and countdown remind work at the same time.
        //add by qjz 20130712 begin
        ((AudioManager) getSystemService(Context.AUDIO_SERVICE)).requestAudioFocus(
                mAudioFocusChangeListener, AudioManager.STREAM_MUSIC,
                AudioManager.AUDIOFOCUS_GAIN_TRANSIENT);
        //PR486560 :Popup video and countdown remind work at the same time.
        //add by qjz 20130712 end
        mMediaPlayer.start();
        mHandler.sendMessageDelayed(mHandler.obtainMessage(1), 3000);
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

    Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            // TODO Auto-generated method stub
            super.handleMessage(msg);
    
            if (mMediaPlayer != null) {
                mMediaPlayer.stop();    
            }
        }
    };
    
    public void removeMessage(){
        
        mHandler.removeMessages(1);
        
    }

    public void onDestroy() {
        super.onDestroy();

        if (mMediaPlayer != null) {
            mMediaPlayer.stop();
            mMediaPlayer.release();
            removeMessage();
            mMediaPlayer = null;
            //add by qjz for PR497097 20130802
            ((AudioManager) getSystemService(Context.AUDIO_SERVICE)).abandonAudioFocus(mAudioFocusChangeListener);
        }
    }

}
