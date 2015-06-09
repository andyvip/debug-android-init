/************************************************************************************************************/
/*                                                                                           Date : 04/2013 */
/*                                      PRESENTATION                                                        */
/*                        Copyright (c) 2012 JRD Communications, Inc.                                       */
/************************************************************************************************************/
/*                                                                                                          */
/*              This material is company confidential, cannot be reproduced in any                          */
/*              form without the written permission of JRD Communications, Inc.                             */
/*                                                                                                          */
/*==========================================================================================================*/
/*   Author :  KUi Wang                                                                                     */
/*   Role :    JrdTimeTool                                                                                  */
/*   Reference documents : None                                                                             */
/*==========================================================================================================*/
/* Comments :                                                                                               */
/*     file    :                                                                                            */
/*     Labels  :                                                                                            */
/*==========================================================================================================*/
/* Modifications   (month/day/year)                                                                         */
/*==========================================================================================================*/
/* date    | author       |FeatureID                                 |modification                          */
/*=========|==============|==========================================|======================================*/

/*==========================================================================================================*/
/* Problems Report(PR/CR)                                                                                   */
/*==========================================================================================================*/
/* date    | author       | PR #                                     |                                      */
/*=========|==============|==========================================|======================================*/
/* 04/17/13 |Kui Wang     |PR439915-kuiwang-001                      | the gesture title display abnormal   */
/*=========|==============|==========================================|======================================*/

package com.jrdcom.timetool.alarm.preference;

import java.lang.reflect.Field;

import com.android.deskclock.R;
import com.jrdcom.timetool.MyLog;
import com.jrdcom.timetool.alarm.view.SwitchButton;

import android.R.integer;
import android.content.Context;
import android.graphics.Color;

import android.preference.TwoStatePreference;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.Checkable;
import android.widget.CompoundButton;
import android.widget.Switch; //add by junye.li for PR864244

public class GesturesAlarmPreference extends TwoStatePreference {

    private final Listener mListener = new Listener();

    public GesturesAlarmPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    private class Listener implements CompoundButton.OnCheckedChangeListener {
        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            if (!callChangeListener(isChecked)) {
                buttonView.setChecked(!isChecked);
                return;
            }

            GesturesAlarmPreference.this.setChecked(isChecked);
        }
    }

    @Override
    public void setChecked(boolean checked) {
        // TODO Auto-generated method stub
        super.setChecked(checked);
        MyLog.i("GesturesAlarmPreference checked->" + checked);
    }

    @Override
    protected void onBindView(View view) {
        super.onBindView(view);
        MyLog.i("onBindView");
        SwitchButton checkableView = (SwitchButton) view.findViewById(R.id.imageswitch);
        if (checkableView != null && checkableView instanceof Checkable) {
            Field field;
            try {
                field = TwoStatePreference.class.getDeclaredField("mChecked");
                MyLog.i(field.toString());
                field.setAccessible(true);
                boolean mChecked;
                try {
                    mChecked = (Boolean) field.get(this);
                    checkableView.setChecked(mChecked);

                    if (checkableView instanceof Switch) { //modify by junye.li for PR864244
                        MyLog.i("onBindView value->" + mChecked);
                        checkableView.setFocusable(false);
                        checkableView.setOnCheckedChangeListener(mListener);
                    }
                } catch (IllegalAccessException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                    MyLog.i("error" + e.getMessage());
                } catch (IllegalArgumentException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                    MyLog.i("error" + e.getMessage());
                }

            } catch (NoSuchFieldException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
                MyLog.i("error" + e.getMessage());
            }

        }
    }

  

}
