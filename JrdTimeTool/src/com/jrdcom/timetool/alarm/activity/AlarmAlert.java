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

import com.android.deskclock.R;
import com.jrdcom.timetool.alarm.provider.Alarms;

import android.app.KeyguardManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.telephony.TelephonyManager;
import android.util.Log;

/**
 * Full screen alarm alert: pops visible indicator and plays alarm tone. This
 * activity shows the alert as a dialog.
 */
public class AlarmAlert extends AlarmAlertFullScreen {

    // If we try to check the keyguard more than 5 times, just launch the full
    // screen activity.
	private static final boolean DEBUG = true;
	private static final String DEBUG_STRING = "jrdtimetool";
    private int mKeyguardRetryCount;
    private final int MAX_KEYGUARD_CHECKS = 5;

    // PR746297-mingwei.han-add begin
    private int CallState = -1;
    private TelephonyManager mTelephonyManager;
    // PR746297-mingwei.han-add end

    private final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            handleScreenOff((KeyguardManager) msg.obj);
        }
    };

    private final BroadcastReceiver mScreenOffReceiver =
            new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    KeyguardManager km =
                            (KeyguardManager) context.getSystemService(
                            Context.KEYGUARD_SERVICE);
                    handleScreenOff(km);
                }
            };

    @Override
    protected void onCreate(Bundle icicle) {
        super.onCreate(icicle);

        // Listen for the screen turning off so that when the screen comes back
        // on, the user does not need to unlock the phone to dismiss the alarm.
        registerReceiver(mScreenOffReceiver,
                new IntentFilter(Intent.ACTION_SCREEN_OFF));
        // PR746297-mingwei.han-add begin
        mTelephonyManager =
                (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        // PR746297-mingwei.han-add end
        
        //modify by min.qiu for pr871605 begin
        sendBroadcast(new Intent("com.jrdcom.timetool.destroy_alarm_alert"));
        //modify by min.qiu for pr871605 end
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mScreenOffReceiver);
        // Remove any of the keyguard messages just in case
        mHandler.removeMessages(0);
    }

    @Override
    public void onBackPressed() {
        finish();
    }

    @Override
    protected int getLayoutResId() {
        return R.layout.alarm_alert;
    }
    
    private boolean checkRetryCount() {
        if (mKeyguardRetryCount++ >= MAX_KEYGUARD_CHECKS) {
            return false;
        }
        return true;
    }

    private void handleScreenOff(final KeyguardManager km) {
        // PR746297-mingwei.han-add begin
        CallState = mTelephonyManager.getCallState();
        if (CallState == TelephonyManager.CALL_STATE_IDLE) {
            // PR746297-mingwei.han-add end
            if (!km.inKeyguardRestrictedInputMode() && checkRetryCount()) {
                if (checkRetryCount()) {
                    mHandler.sendMessageDelayed(mHandler.obtainMessage(0, km), 500);
                }
            } else {
                // Launch the full screen activity but do not turn the screen
                // on.
                if (DEBUG) {
                    Log.d(DEBUG_STRING, "handleScreenOff start AlarmAlertFullScreen");
                }
                Intent i = new Intent(this, AlarmAlertFullScreen.class);
                i.putExtra(Alarms.ALARM_INTENT_EXTRA, mAlarm);
                i.putExtra(SCREEN_OFF, true);
                startActivity(i);
                finish();
            }
        }
    }
}
