package com.jrdcom.android.gallery3d.ui;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.Animator.AnimatorListener;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.PixelFormat;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import com.jrdcom.android.gallery3d.R;
import android.widget.TextView;//PR912344 Add One finger zoom function by fengke at 2015.01.26

public class OneFingerDetectorTips {
    private SharedPreferences sp;
    private Context mContext;
    private WindowManager.LayoutParams mWindowParams;
    private WindowManager mWindowManager;
    private View mContentView;
    private ImageView hand0Img, hand1Img, hand2Img, basepicImg, arrowImg;
    private Button done;
    private float tips_offset_x, tips_offset_y, tips_basepic_x, tips_basepic_y,
            tips_hand0_x, tips_hand0_y, tips_hand1_x, tips_hand1_y,
            tips_hand2_x, tips_hand2_y, tips_arrow_x, tips_arrow_y,
            tips_movedown_start, tips_movedown_mid, tips_movedown_end;
    private AnimatorSet animation;
    private boolean isAdd;

    public OneFingerDetectorTips(Context mContext) {
        super();
        this.mContext = mContext;
        mWindowManager = (WindowManager) mContext
                .getSystemService(Context.WINDOW_SERVICE);
        mContentView = View.inflate(mContext, R.layout.tips, null);
        animation = new AnimatorSet();
        sp = mContext.getSharedPreferences("tips", Context.MODE_PRIVATE);
        initView();
        setDimen();
        setViewProperty();
        setAnimation();
        animation.start();
    }

    //PR912344 Add One finger zoom function by fengke at 2015.01.26 start
    public void setTipsContent(int resId){
        if(mContentView!=null){
            ((TextView)mContentView.findViewById(R.id.tips_content)).setText(R.string.onef_inger_zoomer_tips_camera);
        }
    }
    //PR912344 Add One finger zoom function by fengke at 2015.01.26 end

    public void onPause() {
        if (isFirstTime(mContext)) {
            isAdd = false;
            mWindowManager.removeView(mContentView);
        }
    }

    public void onResume() {
        if (isFirstTime(mContext)) {
            isAdd = true;
            mWindowManager.addView(mContentView, getWindowParams());
        }
    }
    
    private WindowManager.LayoutParams getWindowParams() {
        if (mWindowParams == null) {
            mWindowParams = new WindowManager.LayoutParams();
            mWindowParams.type = WindowManager.LayoutParams.TYPE_PHONE;
            mWindowParams.format = PixelFormat.RGBA_8888;
            mWindowParams.flags = WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
                    | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                    | WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS;
            mWindowParams.gravity = Gravity.LEFT | Gravity.TOP;
        }
        return mWindowParams;
    }

    private void initView() {
        hand0Img = (ImageView) mContentView.findViewById(R.id.hand0);
        hand1Img = (ImageView) mContentView.findViewById(R.id.hand1);
        hand2Img = (ImageView) mContentView.findViewById(R.id.hand2);
        basepicImg = (ImageView) mContentView.findViewById(R.id.basepic);
        arrowImg = (ImageView) mContentView.findViewById(R.id.arrow);
        done = (Button) mContentView.findViewById(R.id.done);
        done.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View arg0) {
                mWindowManager.removeView(mContentView);
                isAdd = false;
                setSP();
            }
        });
    }

    private void setDimen() {
        Resources res = mContext.getResources();
        tips_offset_x = res.getDimension(R.dimen.tips_offset_x);
        tips_offset_y = res.getDimension(R.dimen.tips_offset_y);
        tips_basepic_x = res.getDimension(R.dimen.tips_basepic_x);
        tips_basepic_y = res.getDimension(R.dimen.tips_basepic_y);
        tips_hand0_x = res.getDimension(R.dimen.tips_hand0_x);
        tips_hand0_y = res.getDimension(R.dimen.tips_hand0_y);
        tips_hand1_x = res.getDimension(R.dimen.tips_hand1_x);
        tips_hand1_y = res.getDimension(R.dimen.tips_hand1_y);
        tips_hand2_x = res.getDimension(R.dimen.tips_hand2_x);
        tips_hand2_y = res.getDimension(R.dimen.tips_hand2_y);
        tips_arrow_x = res.getDimension(R.dimen.tips_arrow_x);
        tips_arrow_y = res.getDimension(R.dimen.tips_arrow_y);
        tips_movedown_start = res.getDimension(R.dimen.tips_movedown_start);
        tips_movedown_mid = res.getDimension(R.dimen.tips_movedown_mid);
        tips_movedown_end = res.getDimension(R.dimen.tips_movedown_end);
    }

    private void setViewProperty() {
        basepicImg.setX(tips_basepic_x + tips_offset_x);
        basepicImg.setY(tips_basepic_y + tips_offset_y);
        basepicImg.setScaleX(1.0f);
        basepicImg.setScaleY(1.0f);
        hand0Img.setX(tips_hand0_x + tips_offset_x);
        hand0Img.setY(tips_hand0_y + tips_offset_y);
        hand1Img.setX(tips_hand1_x + tips_offset_x);
        hand1Img.setY(tips_hand1_y + tips_offset_y);
        hand2Img.setX(tips_hand2_x + tips_offset_x);
        hand2Img.setY(tips_hand2_y + tips_offset_y);
        arrowImg.setX(tips_arrow_x + tips_offset_x);
        arrowImg.setY(tips_arrow_y + tips_offset_y);
    }

    private void setAnimation() {
        ObjectAnimator basepicXIn, basepicYIn, basepicXOut, basepicYOut, hand0, hand1, hand2, arrow, handMoveDown, handMoveUp;
        basepicXIn = ObjectAnimator.ofFloat(basepicImg, "scaleX", 1f, 1.5f);
        basepicXIn.setDuration(500);
        basepicYIn = (ObjectAnimator) ObjectAnimator.ofFloat(basepicImg,
                "scaleY", 1f, 1.5f);
        basepicYIn.setDuration(500);
        basepicXOut = (ObjectAnimator) ObjectAnimator.ofFloat(basepicImg,
                "scaleX", 1.5f, 0.5f);
        basepicXOut.setDuration(1000);
        basepicYOut = (ObjectAnimator) ObjectAnimator.ofFloat(basepicImg,
                "scaleY", 1.5f, 0.5f);
        basepicYOut.setDuration(1000);
        hand0 = (ObjectAnimator) ObjectAnimator.ofFloat(hand0Img, "click", 1f);
        hand0.setDuration(200);
        hand0.addListener(new ShortAnimatorListener(hand0Img));
        hand1 = (ObjectAnimator) ObjectAnimator.ofFloat(hand1Img, "click", 1f);
        hand1.setDuration(200);
        hand1.addListener(new ShortAnimatorListener(hand1Img));
        hand2 = (ObjectAnimator) ObjectAnimator.ofFloat(hand2Img, "click", 1f);
        hand2.addListener(new ShortAnimatorListener(hand2Img));
        hand2.setDuration(200);
        handMoveDown = ObjectAnimator.ofFloat(hand2Img, "translationY",
                tips_movedown_start + tips_offset_y, tips_movedown_mid
                        + tips_offset_y);
        handMoveDown.setDuration(500);
        handMoveDown.addListener(new ShortAnimatorListener(hand2Img));
        handMoveUp = ObjectAnimator.ofFloat(hand2Img, "translationY",
                tips_movedown_mid + tips_offset_y, tips_movedown_end
                        + tips_offset_y);
        handMoveUp.setDuration(1000);
        handMoveUp.addListener(new ShortAnimatorListener(hand2Img));
        AnimatorSet click1 = new AnimatorSet();
        click1.playSequentially(hand0, hand1, hand2);
        AnimatorSet click2 = new AnimatorSet();
        click2.playSequentially(hand0, hand1, hand2);
        AnimatorSet scaleIn = new AnimatorSet();
        scaleIn.playTogether(basepicXIn, basepicYIn);
        AnimatorSet scaleOut = new AnimatorSet();
        scaleOut.playTogether(basepicXOut, basepicYOut);
        AnimatorSet scale = new AnimatorSet();
        scale.playSequentially(scaleIn, scaleOut);
        AnimatorSet handMove = new AnimatorSet();
        handMove.playSequentially(handMoveDown, handMoveUp);
        handMove.playTogether(scale);
        handMove.addListener(new ShortAnimatorListener(arrowImg));
        animation.playSequentially(click1, click2, handMove);
        animation.addListener(new EmptyAnimator() {
            @Override
            public void onAnimationEnd(Animator arg0) {
                setViewProperty();
                animation.start();
            }
        });
    }

    public boolean isAdd() {
        return isAdd;
    }

    public static boolean isFirstTime(Context context) {
        SharedPreferences sp = context.getSharedPreferences("tips",
                Context.MODE_PRIVATE);
        return sp.getBoolean("firstTime", true);
    }

    private void setSP() {
        SharedPreferences sp = mContext.getSharedPreferences("tips",
                Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putBoolean("firstTime", false);
        editor.commit();
    }

    class ShortAnimatorListener extends EmptyAnimator {
        ImageView img;

        public ShortAnimatorListener(ImageView img) {
            super();
            this.img = img;
        }

        @Override
        public void onAnimationEnd(Animator animation) {
            // TODO Auto-generated method stub
            super.onAnimationEnd(animation);
            img.setVisibility(View.GONE);
        }

        @Override
        public void onAnimationStart(Animator animation) {
            // TODO Auto-generated method stub
            super.onAnimationStart(animation);
            img.setVisibility(View.VISIBLE);
        }
        
        
    }

    class EmptyAnimator implements AnimatorListener {

        @Override
        public void onAnimationCancel(Animator animation) {
            // TODO Auto-generated method stub

        }

        @Override
        public void onAnimationEnd(Animator animation) {
            // TODO Auto-generated method stub

        }

        @Override
        public void onAnimationRepeat(Animator animation) {
            // TODO Auto-generated method stub

        }

        @Override
        public void onAnimationStart(Animator animation) {
            // TODO Auto-generated method stub

        }

    }

}
