/*
 * Copyright (C) 2011 The Android Open Source Project
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

package com.jrdcom.android.gallery3d.app;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.DisplayMetrics;
import android.view.MotionEvent;
import android.view.View;

import com.jrdcom.android.gallery3d.R;
import com.jrdcom.android.gallery3d.common.Utils;
import com.jrdcom.mediatek.gallery3d.util.MtkLog;

/**
 * The time bar view, which includes the current and total time, the progress bar,
 * and the scrubber.
 */
public class TimeBar extends View {

  public interface Listener {
    void onScrubbingStart();
    void onScrubbingMove(int time);
        void onScrubbingEnd(int time, int start, int end);
        void onShowSeekToView(int seek);
        void onSetSeekViewShow(boolean isShow);
  }

  /*
   * PR 620961 gangchen@tcl.com the SCRUBBER_PADDING_IN_DP has changed to 5,the former is 10.
   */
  // Padding around the scrubber to increase its touch target
  private static final int SCRUBBER_PADDING_IN_DP = 5;
  /*
  * PR 620961 gangchen@tcl.com increase the follow scale.
  */
  final float scale = getResources().getDisplayMetrics().density;
  /*
   * PR 620961 gangchen@tcl.com the SCRUBBER_PADDING_IN_DP has changed to 15,the former is 30.
   */
  // The total padding, top plus bottom
  private static final int V_PADDING_IN_DP = 15;

  private static final int TEXT_SIZE_IN_DP = 14;

    protected final Listener mListener;

  // the bars we use for displaying the progress
    protected final Rect mProgressBar;
    protected final Rect mPlayedBar;

    protected final Paint mProgressPaint;
    protected final Paint mPlayedPaint;
    protected final Paint mTimeTextPaint;

    protected final Bitmap mScrubber;
    protected int mScrubberPadding; // adds some touch tolerance around the
                                    // scrubber

    protected int mScrubberLeft;
    protected int mScrubberTop;
    protected int mScrubberCorrection;
    protected boolean mScrubbing;
    protected boolean mShowTimes;
    protected boolean mShowScrubber;

    protected int mTotalTime;
    protected int mCurrentTime;

    protected final Rect mTimeBounds;

    protected int mVPaddingInPx;
    private int mPreSeekTime = -1000;
  //PR485574-lilei-begin
    private Context mContext;
  //PR485574-lilei-end

  public TimeBar(Context context, Listener listener) {
    super(context);
        //PR485574-lilei-begin
        mContext = context;
      //PR485574-lilei-end
        mListener = Utils.checkNotNull(listener);

        mShowTimes = true;
        mShowScrubber = true;

        mProgressBar = new Rect();
        mPlayedBar = new Rect();

        //modified by qjz for VideoPlayer NewUI 20130411 begin
        /*mProgressPaint = new Paint();
        mProgressPaint.setColor(0xFF808080);
        mPlayedPaint = new Paint();
        mPlayedPaint.setColor(0xFFFFFFFF);*/
        mProgressPaint = new Paint();
        mProgressPaint.setColor(0xFF9f9f9f);
        mPlayedPaint = new Paint();
        mPlayedPaint.setColor(0xFFf7b400);
        //modified by qjz for VideoPlayer NewUI 20130411 end

    DisplayMetrics metrics = context.getResources().getDisplayMetrics();
    float textSizeInPx = metrics.density * TEXT_SIZE_IN_DP;
        mTimeTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mTimeTextPaint.setColor(0xFFCECECE);
        mTimeTextPaint.setTextSize(textSizeInPx);
        mTimeTextPaint.setTextAlign(Paint.Align.CENTER);

        mTimeBounds = new Rect();
    //timeTextPaint.getTextBounds("0:00:00", 0, 7, timeBounds);

        //modified by qjz for VideoPlayer NewUI 20130411 begin
        //mScrubber = BitmapFactory.decodeResource(getResources(), R.drawable.scrubber_knob);
        mScrubber = BitmapFactory.decodeResource(getResources(), R.drawable.scrubber_knob_new);
        //modified by qjz for VideoPlayer NewUI 20130411 end
        /*
         * PR 620961 gangchen@tcl.com the SCRUBBER_PADDING_IN_DP has changed to SCRUBBER_PADDING_IN_DP*scale+0.5f.
         */
        mScrubberPadding = (int) (metrics.density * (SCRUBBER_PADDING_IN_DP*scale+0.5f));

        /*
         * PR 620961 gangchen@tcl.com the V_PADDING_IN_DP has changed to V_PADDING_IN_DP*scale+0.5f.
         */
        mVPaddingInPx = (int) (metrics.density * (V_PADDING_IN_DP*scale+0.5f));

    mLayoutExt.init(mScrubberPadding, mVPaddingInPx);
    mInfoExt.init(textSizeInPx);
    mSecondaryProgressExt.init();
  }

  private void update() {
        mPlayedBar.set(mProgressBar);

        if (mTotalTime > 0) {
            mPlayedBar.right =
                    mPlayedBar.left + (int) ((mProgressBar.width() * (long) mCurrentTime) / mTotalTime);
      /*
       *  M: if duration is not accurate, here just adjust playedBar
       *  we also show the accurate position text to final user.
       */
      if (mPlayedBar.right > mProgressBar.right) {
          mPlayedBar.right = mProgressBar.right;
      }
    } else {
            mPlayedBar.right = mProgressBar.left;
    }

        if (!mScrubbing) {
            mScrubberLeft = mPlayedBar.right - mScrubber.getWidth() / 2;
    }
    //update text bounds when layout changed or time changed
    updateBounds();
    mInfoExt.updateVisibleText(this, mProgressBar, mTimeBounds);
    invalidate();
  }

  /**
   * @return the preferred height of this view, including invisible padding
   */
  public int getPreferredHeight() {
    int preferredHeight = mTimeBounds.height() + mVPaddingInPx + mScrubberPadding;
    return mLayoutExt.getPreferredHeight(preferredHeight, mTimeBounds);
  }

  /**
   * @return the height of the time bar, excluding invisible padding
   */
  public int getBarHeight() {
    int barHeight = mTimeBounds.height() + mVPaddingInPx;
    return mLayoutExt.getBarHeight(barHeight, mTimeBounds);
  }

  public void setTime(int currentTime, int totalTime,
          int trimStartTime, int trimEndTime) {
    if (LOG) {
        MtkLog.v(TAG, "setTime(" + currentTime + ", " + totalTime + ")");
    }
    if (this.mCurrentTime == currentTime && this.mTotalTime == totalTime) {
        return;
    }
    this.mCurrentTime = currentTime;
    this.mTotalTime = Math.abs(totalTime);
    if (totalTime <= 0) { /// M: disable scrubbing before mediaplayer ready.
        setScrubbing(false);
    }
    update();
  }
  //modified by qjz for NewVedioPlayerUI 20130405 begin
  public boolean isSeekTo(){
      if (mTotalTime < 1000) {
          if (Math.abs(mPreSeekTime-mCurrentTime)>=50) {
              mPreSeekTime = mCurrentTime;
              return true;
          }
      } else if (mTotalTime <10000) {
          if (Math.abs(mPreSeekTime-mCurrentTime)>=200) {
              mPreSeekTime = mCurrentTime;
              return true;
          }
      }else if (mTotalTime <60000) {
          if (Math.abs(mPreSeekTime-mCurrentTime)>=500) {
              mPreSeekTime = mCurrentTime;
              return true;
          }
      } else {
          if (Math.abs(mPreSeekTime-mCurrentTime)>=1000) {
              mPreSeekTime = mCurrentTime;
              return true;
          }
      }
      return false;
  }
  //modified by qjz for NewVedioPlayerUI 20130405 end
  private boolean inScrubber(float x, float y) {
    int scrubberRight = mScrubberLeft + mScrubber.getWidth();
    int scrubberBottom = mScrubberTop + mScrubber.getHeight();
    return mScrubberLeft - mScrubberPadding < x && x < scrubberRight + mScrubberPadding
        && mScrubberTop - mScrubberPadding < y && y < scrubberBottom + mScrubberPadding;
  }

  private void clampScrubber() {
    int half = mScrubber.getWidth() / 2;
    int max = mProgressBar.right - half;
    int min = mProgressBar.left - half;
    mScrubberLeft = Math.min(max, Math.max(min, mScrubberLeft));
  }

  private int getScrubberTime() {
    return (int) ((long) (mScrubberLeft + mScrubber.getWidth() / 2 - mProgressBar.left)
        * mTotalTime / mProgressBar.width());
  }

  @Override
  protected void onLayout(boolean changed, int l, int t, int r, int b) {
    int w = r - l;
    int h = b - t;
    if (!mShowTimes && !mShowScrubber) {
        mProgressBar.set(0, 0, w, h);
    } else {
      int margin = mScrubber.getWidth() / 3;
      if (mShowTimes) {
        margin += mTimeBounds.width();
      }
      margin = mLayoutExt.getProgressMargin(margin);
      /*
       * PR 620961 gangchen@tcl.com the progressY has changed.
       * the former is (h + mScrubberPadding) / 2
       */
      int progressY = (h + mScrubberPadding) / 2 -(int)(23*scale+0.5f)/*+ mLayoutExt.getProgressOffset(mTimeBounds)*/;
      mScrubberTop = progressY - mScrubber.getHeight() / 2 + 1;
      //PR597079-Li-Zhao begin
      int progressbar_paddingleft = (int)getResources().getDimension(R.dimen.progressbar_paddingleft);
    //PR485574-lilei-begin
      DisplayMetrics metrics = mContext.getResources().getDisplayMetrics();
      if(metrics.density == 2.0){
          mProgressBar.set(
                  getPaddingLeft() + margin+136, progressY,
                  w - getPaddingRight() - margin-136, progressY + 4);
      }else{
      mProgressBar.set(
          getPaddingLeft() + margin+progressbar_paddingleft, progressY,
          w - getPaddingRight() - margin-progressbar_paddingleft, progressY + 4);
      }
    //PR485574-lilei-end
      //PR597079-Li-Zhao end
    }
    update();
  }

  @Override
    protected void onDraw(Canvas canvas) {
    super.onDraw(canvas);

    // draw progress bars
    canvas.drawRect(mProgressBar, mProgressPaint);
    mSecondaryProgressExt.draw(canvas, mProgressBar);
    canvas.drawRect(mPlayedBar, mPlayedPaint);

    // draw scrubber and timers
    if (mShowScrubber) {
      canvas.drawBitmap(mScrubber, mScrubberLeft, mScrubberTop, null);
    }
    if (mShowTimes) {
      canvas.drawText(
          stringForTime(mCurrentTime),
          mTimeBounds.width() / 2 + getPaddingLeft(),
          /*
           * PR 620961 gangchen@tcl.com the third number has changed.the former is 1.
           */
          mTimeBounds.height() + mVPaddingInPx / 2 + mScrubberPadding + (5*scale+0.5f) + mLayoutExt.getTimeOffset(),
          mTimeTextPaint);
      canvas.drawText(
          stringForTime(mTotalTime),
          getWidth() - getPaddingRight() - mTimeBounds.width() / 2,
          /*
           * PR 620961 gangchen@tcl.com the third number has changed.
           *  the former is 1.
           */
          mTimeBounds.height() + mVPaddingInPx / 2 + mScrubberPadding + (5*scale+0.5f) + mLayoutExt.getTimeOffset(),
          mTimeTextPaint);
    }
    mInfoExt.draw(canvas, mLayoutExt.getInfoBounds(this, mTimeBounds));
  }

  @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (LOG) {
            MtkLog.v(TAG, "onTouchEvent() showScrubber=" + mShowScrubber
                    + ", enableScrubbing=" + mEnableScrubbing + ", totalTime="
                    + mTotalTime + ", scrubbing=" + mScrubbing + ", event="
                    + event);
        }
        if (mShowScrubber && mEnableScrubbing) {
            int x = (int) event.getX();
            int y = (int) event.getY();

            switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN: {
                if (y>100) return true;//modified by qjz for PR461018 20130603
                mScrubberCorrection = inScrubber(x, y) ? x - mScrubberLeft
                        : mScrubber.getWidth() / 2;
                mScrubbing = true;
                mListener.onScrubbingStart();
                mPreSeekTime = -1000;
                mListener.onSetSeekViewShow(true);//add by qjz for new UI
                return true;
            }
            // fall-through
            case MotionEvent.ACTION_MOVE:
                if (y>100) return true;//modified by qjz for PR461018 20130523
                mScrubberLeft = x - mScrubberCorrection;
                clampScrubber();
                mCurrentTime = getScrubberTime();
                mListener.onScrubbingMove(mCurrentTime);
                update();
                invalidate();
                if (isSeekTo()) {
                    mListener.onShowSeekToView(mCurrentTime);//add by qjz for new UI
                }
                return true;
            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP:
                if (mScrubbing) {
                    mListener.onScrubbingEnd(getScrubberTime(), 0, 0);
                    mScrubbing = false;
                    update();
                    mListener.onSetSeekViewShow(false);//add by qjz for new UI
                    return true;
                }
                break;
            }
        }
        return false;
    }

    protected String stringForTime(long millis) {
    int totalSeconds = (int) millis / 1000;
    int seconds = totalSeconds % 60;
    int minutes = (totalSeconds / 60) % 60;
    int hours = totalSeconds / 3600;
    if (hours > 0) {
      return String.format("%d:%02d:%02d", hours, minutes, seconds).toString();
    } else {
      return String.format("%02d:%02d", minutes, seconds).toString();
    }
  }

  private static final String TAG = "Gallery3D/TimeBar";
  private static final boolean LOG = true;
  public static final int UNKNOWN = -1;
  
  /// M: if time changed, we should update time bounds. @{
  private int mLastShowTime = UNKNOWN;
  private void updateBounds() {
      int showTime = mTotalTime > mCurrentTime ? mTotalTime : mCurrentTime;
      if (mLastShowTime == showTime) {
          //do not need to recompute the bounds.
          return;
      }
      String durationText = stringForTime(showTime);
      int length = durationText.length();
      mTimeTextPaint.getTextBounds(durationText, 0, length, mTimeBounds);
      mLastShowTime = showTime;
      if (LOG) {
          MtkLog.v(TAG, "updateBounds() durationText=" + durationText + ", timeBounds=" + mTimeBounds);
      }
  }
  /// @}

  /// M: we should disable scrubbing in some state. @{
  private boolean mEnableScrubbing;
  public void setScrubbing(boolean enable) {
      if (LOG) {
          MtkLog.v(TAG, "setScrubbing(" + enable + ") scrubbing=" + mScrubbing);
      }
      mEnableScrubbing = enable;
      if (mScrubbing) { //if it is scrubbing, change it to false
          mListener.onScrubbingEnd(getScrubberTime(),0,0);
          mScrubbing = false;
      }
  }
  public boolean getScrubbing() {
      if (LOG) {
          MtkLog.v(TAG, "mEnableScrubbing=" + mEnableScrubbing);
      }
      return mEnableScrubbing;
  }
  /// @}
  
  private ITimeBarSecondaryProgressExt mSecondaryProgressExt = new TimeBarSecondaryProgressExtImpl();
  private ITimeBarInfoExt mInfoExt = new TimeBarInfoExtImpl();
  private ITimeBarLayoutExt mLayoutExt = new TimeBarLayoutExtImpl();
  
  /// M: for info feature. @{
  public void setInfo(String info) {
      if (LOG) {
          MtkLog.v(TAG, "setInfo(" + info + ")");
      }
      mInfoExt.setInfo(info);
      mInfoExt.updateVisibleText(this, mProgressBar, mTimeBounds);
      invalidate();
  }
  /// @}
  
  /// M: for secondary progress feature @{
  public void setSecondaryProgress(int percent) {
      if (LOG) {
          MtkLog.v(TAG, "setSecondaryProgress(" + percent + ")");
      }
      mSecondaryProgressExt.setSecondaryProgress(mProgressBar, percent);
      invalidate();
  }
  /// @}
}

interface ITimeBarInfoExt {
    void init(float textSizeInPx);
    void setInfo(String info);
    void draw(Canvas canvas, Rect infoBounds);
    void updateVisibleText(View parent, Rect progressBar, Rect timeBounds);
}

interface ITimeBarSecondaryProgressExt {
    void init();
    void setSecondaryProgress(Rect progressBar, int percent);
    void draw(Canvas canvas, Rect progressBounds);
}

interface ITimeBarLayoutExt {
    void init(int scrubberPadding, int vPaddingInPx);
    int getPreferredHeight(int originalPreferredHeight, Rect timeBounds);
    int getBarHeight(int originalBarHeight, Rect timeBounds);
    int getProgressMargin(int originalMargin);
    int getProgressOffset(Rect timeBounds);
    int getTimeOffset();
    Rect getInfoBounds(View parent, Rect timeBounds);
}

class TimeBarInfoExtImpl implements ITimeBarInfoExt {
    private static final String TAG = "TimeBarInfoExtensionImpl";
    private static final boolean LOG = true;
    private static final String ELLIPSE = "...";
    
    private Paint mInfoPaint;
    private Rect mInfoBounds;
    private String mInfoText;
    private String mVisibleText;
    private int mEllipseLength;

    @Override
    public void init(float textSizeInPx) {
        mInfoPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mInfoPaint.setColor(0xFFCECECE);
        mInfoPaint.setTextSize(textSizeInPx);
        mInfoPaint.setTextAlign(Paint.Align.CENTER);

        mEllipseLength = (int)Math.ceil(mInfoPaint.measureText(ELLIPSE));
    }

    @Override
    public void draw(Canvas canvas, Rect infoBounds) {
        // delete by yaping.liu for pr497232
        /*if (mInfoText != null && mVisibleText != null) {
            canvas.drawText(mVisibleText, infoBounds.centerX(), infoBounds.centerY(), mInfoPaint);
        }*/
    }

    @Override
    public void setInfo(String info) {
        mInfoText = info;
    }
    
    public void updateVisibleText(View parent, Rect progressBar, Rect timeBounds) {
        if (mInfoText == null) {
            mVisibleText = null;
            return;
        }
        float tw = mInfoPaint.measureText(mInfoText);
        float space = progressBar.width() - timeBounds.width() * 2 - parent.getPaddingLeft() - parent.getPaddingRight(); 
        if (tw > 0 && space > 0 && tw > space) {
            //we need to cut the info text for visible
            float originalNum = mInfoText.length();
            int realNum = (int)((space - mEllipseLength) * originalNum / tw);
            if (LOG) {
                MtkLog.v(TAG, "updateVisibleText() infoText=" + mInfoText + " text width=" + tw
                    + ", space=" + space + ", originalNum=" + originalNum + ", realNum=" + realNum
                    + ", getPaddingLeft()=" + parent.getPaddingLeft() + ", getPaddingRight()=" + parent.getPaddingRight()
                    + ", progressBar=" + progressBar + ", timeBounds=" + timeBounds);
            }
            mVisibleText = mInfoText.substring(0, realNum) + ELLIPSE;
        } else {
            mVisibleText = mInfoText;
        }
        if (LOG) {
            MtkLog.v(TAG, "updateVisibleText() infoText=" + mInfoText + ", visibleText=" + mVisibleText
                + ", text width=" + tw + ", space=" + space);
        }
    }
}

class TimeBarSecondaryProgressExtImpl implements ITimeBarSecondaryProgressExt {
    private static final String TAG = "TimeBarSecondaryProgressExtensionImpl";
    private static final boolean LOG = true;
    
    private int mBufferPercent;
    private Rect mSecondaryBar;
    private Paint mSecondaryPaint;

    @Override
    public void init() {
        mSecondaryBar = new Rect();
        mSecondaryPaint = new Paint();
        mSecondaryPaint.setColor(0xFF5CA0C5);
    }

    @Override
    public void draw(Canvas canvas, Rect progressBounds) {
        if (mBufferPercent >= 0) {
            mSecondaryBar.set(progressBounds);
            mSecondaryBar.right = mSecondaryBar.left + (int)(mBufferPercent * progressBounds.width() / 100);
            canvas.drawRect(mSecondaryBar, mSecondaryPaint);
        }
        if (LOG) {
            MtkLog.v(TAG, "draw() bufferPercent=" + mBufferPercent + ", secondaryBar=" + mSecondaryBar);
        }
    }
    @Override
    public void setSecondaryProgress(Rect progressBar, int percent) {
        mBufferPercent = percent;
    }
}

class TimeBarLayoutExtImpl implements ITimeBarLayoutExt {
    private static final String TAG = "TimeBarLayoutExtensionImpl";
    private static final boolean LOG = true;
    
    private int mTextPadding;
    private int mVPaddingInPx;
    
    @Override
    public void init(int scrubberPadding, int vPaddingInPx) {
        mTextPadding = scrubberPadding / 2;
        mVPaddingInPx = vPaddingInPx;
    }
    
    @Override
    public int getPreferredHeight(int originalPreferredHeight, Rect timeBounds) {
        return originalPreferredHeight + timeBounds.height() + mTextPadding;
    }

    @Override
    public int getBarHeight(int originalBarHeight, Rect timeBounds) {
        return originalBarHeight + timeBounds.height() + mTextPadding;
    }
    
    @Override
    public int getProgressMargin(int originalMargin) {
        return 0;
    }
    
    @Override
    public int getProgressOffset(Rect timeBounds) {
        return (timeBounds.height() + mTextPadding) / 2;
    }
    
    @Override
    public int getTimeOffset() {
        return mTextPadding - mVPaddingInPx / 2;
    }
    
    @Override
    public Rect getInfoBounds(View parent, Rect timeBounds) {
        Rect bounds = new Rect(parent.getPaddingLeft(), 0,
                parent.getWidth() - parent.getPaddingRight(),
                (timeBounds.height() + mTextPadding * 3 + 1) * 2);
        return bounds;
    }
}
