package com.jrdcom.timetool.alarm.service;

import java.util.ArrayList;

import com.jrdcom.timetool.alarm.provider.Alarms;

import android.app.Service;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.IBinder;

public class ClearSnoozeService extends Service {
    @Override
    public IBinder onBind(Intent intent) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void onCreate() {
        // TODO Auto-generated method stub
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // TODO Auto-generated method stub
        ArrayList<Integer> ids = calibrationLabel(this);
        for (int i = 0; i < ids.size(); i++) {
            Alarms.enableAlarm(this, ids.get(i), false);
        }
        //add PR457629 xibin -- Data cannot be cleared after click "clear data"
        Intent clearDataIntent = new Intent(
                "com.android.deskclock.alarm.receiver.CLEAR_USER_DATA");
        sendBroadcast(clearDataIntent);
        return super.onStartCommand(intent, flags, startId);
    }

    private ArrayList<Integer> calibrationLabel(Context context) {
        ArrayList<Integer> ids = new ArrayList<Integer>();
        ContentResolver contentResolver = context.getContentResolver();
        Cursor cursor = contentResolver.query(Uri.parse("content://"
                + "com.jrdcom.timetool.alarm" + "/alarm/"),
                new String[] { "_id" }, null, null, null);

        while (cursor != null && cursor.getCount() != 0 && cursor.moveToNext()) {
            ids.add(cursor.getInt(cursor.getColumnIndex("_id")));
        }
        if (cursor != null) {
            cursor.close();
        }
        return ids;
    }

}
