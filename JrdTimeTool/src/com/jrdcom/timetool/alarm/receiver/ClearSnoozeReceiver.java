package com.jrdcom.timetool.alarm.receiver;


import com.jrdcom.timetool.alarm.service.ClearSnoozeService;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class ClearSnoozeReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        // TODO Auto-generated method stub
        Intent startService=new Intent();
        startService.setClass(context, ClearSnoozeService.class);
        context.startService(startService);
    }


}
