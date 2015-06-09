/*
 * Copyright (C) 2012 The Android Open Source Project
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

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.RelativeLayout;

import com.jrdcom.android.gallery3d.R;

import java.util.HashMap;
import java.util.Map;

public class PhotoPageBottomControls implements OnClickListener {
    public interface Delegate {
        public boolean canDisplayBottomControls();
        public boolean canDisplayBottomControl(int control);
        public void onBottomControlClicked(int control);
        public void refreshBottomControlsWhenReady();
        //FR418632 add by yashuang.mu@tcl.com for Bottom Menu begin
        public void onMenuBottomControlClicked(int control);
        //FR418632 add by yashuang.mu@tcl.com for Bottom Menu end
    }

    private Delegate mDelegate;
    private ViewGroup mParentLayout;
    private ViewGroup mContainer;

    private boolean mContainerVisible = false;
    private Map<View, Boolean> mControlsVisible = new HashMap<View, Boolean>();

    private Animation mContainerAnimIn = new AlphaAnimation(0f, 1f);
    private Animation mContainerAnimOut = new AlphaAnimation(1f, 0f);
    private static final int CONTAINER_ANIM_DURATION_MS = 200;

    private static final int CONTROL_ANIM_DURATION_MS = 150;
    private static Animation getControlAnimForVisibility(boolean visible) {
        Animation anim = visible ? new AlphaAnimation(0f, 1f)
                : new AlphaAnimation(1f, 0f);
        anim.setDuration(CONTROL_ANIM_DURATION_MS);
        return anim;
    }
    //FR418632 add by yashuang.mu@tcl.com for Bottom Menu begin
    OnClickListener mMenuOnClickListener = new View.OnClickListener() {

        @Override
      public void onClick(View view) {
         // TODO Auto-generated method stub
            mDelegate.onMenuBottomControlClicked(view.getId());
        }
     };
    //FR418632 add by yashuang.mu@tcl.com for Bottom Menu end


    public PhotoPageBottomControls(Delegate delegate, Context context, RelativeLayout layout) {
        mDelegate = delegate;
        mParentLayout = layout;

        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mContainer = (ViewGroup) inflater
                .inflate(R.layout.photopage_bottom_controls, mParentLayout, false);
        mParentLayout.addView(mContainer);

        for (int i = mContainer.getChildCount() - 1; i >= 0; i--) {
            View child = mContainer.getChildAt(i);
            child.setOnClickListener(this);
            mControlsVisible.put(child, false);
        }
        //FR418632 add by yashuang.mu@tcl.com for Bottom Menu begin
        ImageButton shareButton = (ImageButton) mParentLayout.findViewById(R.id.photopage_bottom_control_share);
        shareButton.setOnClickListener(mMenuOnClickListener);
        ImageButton editButton = (ImageButton )mParentLayout.findViewById(R.id.photopage_bottom_control_edit);
        editButton.setOnClickListener(mMenuOnClickListener);
        ImageButton setAsButton =  (ImageButton) mParentLayout.findViewById(R.id.photopage_bottom_control_setas);
        setAsButton.setOnClickListener(mMenuOnClickListener);
        ImageButton slideButton = (ImageButton) mParentLayout.findViewById(R.id.photopage_bottom_control_slide);
        slideButton.setOnClickListener(mMenuOnClickListener);
        //FR418632 add by yashuang.mu@tcl.com for Bottom Menu end

        mContainerAnimIn.setDuration(CONTAINER_ANIM_DURATION_MS);
        mContainerAnimOut.setDuration(CONTAINER_ANIM_DURATION_MS);

        mDelegate.refreshBottomControlsWhenReady();
    }
   //add a Menu for Gallery, when luanch from Camera.
    public PhotoPageBottomControls(Delegate delegate, Context context, FrameLayout layout) {
        mDelegate = delegate;
        mParentLayout = layout;

        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mContainer = (ViewGroup) inflater
                .inflate(R.layout.photopage_bottom_controls, mParentLayout, false);
        mParentLayout.addView(mContainer);

        for (int i = mContainer.getChildCount() - 1; i >= 0; i--) {
            View child = mContainer.getChildAt(i);
            child.setOnClickListener(this);
            mControlsVisible.put(child, false);
        }
        //FR418632 add by yashuang.mu@tcl.com for Bottom Menu begin
        ImageButton shareButton = (ImageButton) mParentLayout.findViewById(R.id.photopage_bottom_control_share);
        shareButton.setOnClickListener(mMenuOnClickListener);
        ImageButton editButton = (ImageButton )mParentLayout.findViewById(R.id.photopage_bottom_control_edit);
        editButton.setOnClickListener(mMenuOnClickListener);
        ImageButton setAsButton =  (ImageButton) mParentLayout.findViewById(R.id.photopage_bottom_control_setas);
        setAsButton.setOnClickListener(mMenuOnClickListener);
        ImageButton slideButton = (ImageButton) mParentLayout.findViewById(R.id.photopage_bottom_control_slide);
        slideButton.setOnClickListener(mMenuOnClickListener);
        //FR418632 add by yashuang.mu@tcl.com for Bottom Menu end
        
        mContainerAnimIn.setDuration(CONTAINER_ANIM_DURATION_MS);
        mContainerAnimOut.setDuration(CONTAINER_ANIM_DURATION_MS);

        mDelegate.refreshBottomControlsWhenReady();
    }
    
    private void hide() {
        mContainer.clearAnimation();
        mContainerAnimOut.reset();
        mContainer.startAnimation(mContainerAnimOut);
        mContainer.setVisibility(View.INVISIBLE);
    }

    private void show() {
        mContainer.clearAnimation();
        mContainerAnimIn.reset();
        mContainer.startAnimation(mContainerAnimIn);
        mContainer.setVisibility(View.VISIBLE);
    }

    public void refresh() {
        boolean visible = mDelegate.canDisplayBottomControls();
        boolean containerVisibilityChanged = (visible != mContainerVisible);
        if (containerVisibilityChanged) {
            if (visible) {
                show();
            } else {
                hide();
            }
            mContainerVisible = visible;
        }
        if (!mContainerVisible) {
            return;
        }
        for (View control : mControlsVisible.keySet()) {
            Boolean prevVisibility = mControlsVisible.get(control);
            boolean curVisibility = mDelegate.canDisplayBottomControl(control.getId());
            if (prevVisibility.booleanValue() != curVisibility) {
                if (!containerVisibilityChanged) {
                    control.clearAnimation();
                    control.startAnimation(getControlAnimForVisibility(curVisibility));
                }
                control.setVisibility(curVisibility ? View.VISIBLE : View.INVISIBLE);
                mControlsVisible.put(control, curVisibility);
            }
        }
        // Force a layout change
        mContainer.requestLayout(); // Kick framework to draw the control.
    }

    public void cleanup() {
        mParentLayout.removeView(mContainer);
        mControlsVisible.clear();
    }

    @Override
    public void onClick(View view) {
        if (mContainerVisible && mControlsVisible.get(view).booleanValue()) {
            mDelegate.onBottomControlClicked(view.getId());
        }
    }
}
