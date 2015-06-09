package com.jrdcom.timetool.countdown.service;

import java.io.IOException;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.AudioManager.OnAudioFocusChangeListener;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnErrorListener;
import android.os.IBinder;
import com.jrdcom.timetool.countdown.activity.CountDownActivity;
import com.jrdcom.timetool.countdown.activity.CountDownChooseRingtoneDialogActivity;

public class MediaPlayerService extends Service {

	private Thread mTimerThread;

	/** Ringing time ,in second units. **/
	private int mRunTimeSecond;
	
	private int mVolume;

	private MediaPlayer mMediaPlayer;

	public static final String IS_LOOP_EXTRA = "isloop";// add by Yanjingming for pr541647

	public static final String MEDIA_FILE_PATH_EXTRA = "mediaFilePath";

	public static final String RUN_TIME_EXTRA = "runTime";

	public static final String SLEEP_TIME_EXTRA = "sleepTime";
	
	public static final String VOLUME = "volume";
	
	private int maxVolume; 
	
	private AudioManager mAudioManager;


	public IBinder onBind(Intent i) {
		return null;
	}

	public void onCreate() {
		super.onCreate();

	}
    // PR:503524 add by XIBIN start
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        onStartService(intent, startId);
        return START_NOT_STICKY;
    }
    private void onStartService(Intent intent, int startId) {
        // PR:503524 add by XIBIN end
		mTimerThread = new Thread(new secondCountDownRunnable(),
				"TimerSecondCountDownThread");
		mMediaPlayer = new MediaPlayer();
		mAudioManager = (AudioManager) this
                .getSystemService(Context.AUDIO_SERVICE);
		mMediaPlayer.setOnCompletionListener(new OnCompletionListener() {
			public void onCompletion(MediaPlayer mp) {
				stopSelf();
			}
		});
		String mediaFilePath = intent.getStringExtra(MEDIA_FILE_PATH_EXTRA);
		boolean isSetLoop = intent.getBooleanExtra(IS_LOOP_EXTRA,
		        false);// add by Yanjingming for pr541647
		try {

			mMediaPlayer.reset();

			if (mediaFilePath.equals("")) {
				mMediaPlayer.setDataSource(mediaFilePath + "/"
						+ CountDownChooseRingtoneDialogActivity.ALERT_RINGTONE_PATH_KEY);
			} else
				mMediaPlayer.setDataSource(mediaFilePath);
			//Noted by xiaxia.yao for PR:416598 begin
			//mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
			mMediaPlayer.setAudioStreamType(AudioManager.STREAM_ALARM);
			//Noted by xiaxia.yao for PR:416598 end
			// add by Yanjingming for pr541647 begin
			if(isSetLoop){
			    mMediaPlayer.setLooping(true);
			}
			// add by Yanjingming for pr541647 end
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
				// stopSelf();
				return true;
			}
		});

		// modify by Yanjingming for pr541647 begin
		mRunTimeSecond = intent.getIntExtra(RUN_TIME_EXTRA, -1);
		mVolume = intent.getIntExtra(VOLUME, 3);
        //PR486560 :Popup video and countdown remind work at the same time.
        //add by qjz 20130712 begin
        if (isSetLoop) {
            ((AudioManager) getSystemService(Context.AUDIO_SERVICE)).requestAudioFocus(
                    mAudioFocusChangeListener, AudioManager.STREAM_MUSIC,
                    AudioManager.AUDIOFOCUS_GAIN_TRANSIENT);
        }else {
            ((AudioManager) getSystemService(Context.AUDIO_SERVICE)).requestAudioFocus(
                    mAudioFocusChangeListener, AudioManager.STREAM_MUSIC,
                    AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK);
        }
        // modify by Yanjingming for pr541647 end

        //PR486560 :Popup video and countdown remind work at the same time.
        //add by qjz 20130712 end
        mMediaPlayer.start();
        //Noted by xiaxia.yao for PR:416598 begin
        //maxVolume = mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        //mMediaPlayer.setVolume(maxVolume * mVolume / 10, maxVolume * mVolume / 10);
        maxVolume = mAudioManager.getStreamMaxVolume(AudioManager.STREAM_ALARM);
        mMediaPlayer.setVolume(mVolume, mVolume);
        //Noted by xiaxia.yao for PR:416598 end
        if( mRunTimeSecond >= 0){
            mTimerThread = new Thread(new secondCountDownRunnable());
            mTimerThread.start();
        }
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

    public void onDestroy() {
        super.onDestroy();

        if (mMediaPlayer != null) {
            mMediaPlayer.stop();
            mMediaPlayer.release();
            mMediaPlayer = null;
            //add by qjz for PR497097 20130802
            ((AudioManager) getSystemService(Context.AUDIO_SERVICE)).abandonAudioFocus(mAudioFocusChangeListener);
        }
        mRunTimeSecond = 0;
        mTimerThread.interrupt();
    }

    private class secondCountDownRunnable implements Runnable {

        public void run() {
            while (!Thread.currentThread().isInterrupted()) {
                if (mRunTimeSecond == 0) {

                    stopSelf();

                } else {
                    mRunTimeSecond--;

                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                }
            }

        }

    }
}
