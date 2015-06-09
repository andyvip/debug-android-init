/*
 * Copyright (C) 2010 The Android Open Source Project
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

package com.jrdcom.timetool.alarm;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;
import android.util.Log;

import com.android.deskclock.R;
import com.jrdcom.timetool.alarm.provider.Alarm;
import com.jrdcom.timetool.alarm.provider.Alarms;

/**
 * Helper class for opening the database from multiple providers.  Also provides
 * some common functionality.
 */
public class AlarmDatabaseHelper extends SQLiteOpenHelper {

	private static final String TAG = "AlarmDatabaseHelper";
    private static final String DATABASE_NAME = "dalarms.db";
    /*PR 585405- Neo Skunkworks - Paul Xu modified - 001 Begin*/
    /*
    private static final int DATABASE_VERSION = 5;
    */
    private static final int DATABASE_VERSION = 6;
    /*PR 585405- Neo Skunkworks - Paul Xu modified - 001 End*/
    private boolean specialWeekend = false; // add by Yan Jingming for pr549425

    public AlarmDatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        //specialWeekend = context.getResources().getBoolean(
                //R.bool.def_alarm_use_special_weekend);// add by Yan Jingming for pr549425
        specialWeekend = Alarms.getBoolean(context, "def_alarm_use_special_weekend");
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE alarms (" +
                   "_id INTEGER PRIMARY KEY," +
                   "hour INTEGER, " +
                   "minutes INTEGER, " +
                   "daysofweek INTEGER, " +
                   "alarmtime INTEGER, " +
                   "enabled INTEGER, " +
                   "vibrate INTEGER, " +
                   "message TEXT, " +
                   "alert TEXT, " + 
                   "volume INTEGER, " +
                   "ringtonePath TEXT, " +
                   "alertCount INTEGER);");

        // insert default alarms
        String insertMe = "INSERT INTO alarms " +
                "(hour, minutes, daysofweek, alarmtime, enabled, vibrate, " +
                " message, alert, volume, ringtonePath, alertCount) VALUES ";
        // modify by Yan Jingming for pr549425 begin
        if(specialWeekend){
            db.execSQL(insertMe + "(8, 30, 79, 0, 0, 1, '', '', 2, '', 0 );");
            db.execSQL(insertMe + "(9, 00, 48, 0, 0, 1, '', '', 2, '', 0 );");
        }else{
            db.execSQL(insertMe + "(8, 30, 31, 0, 0, 1, '', '', 2, '', 0 );");
            db.execSQL(insertMe + "(9, 00, 96, 0, 0, 1, '', '', 2, '', 0 );");
        }
        // modify by Yan Jingming for pr549425 end
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion,
            int currentVersion) {
    	/*PR 585405- Neo Skunkworks - Paul Xu modified - 001 Begin*/
    	/*
    	db.execSQL("DROP TABLE IF EXISTS alarms");
		onCreate(db);
		*/
    	if(oldVersion < 5){
    		db.execSQL("DROP TABLE IF EXISTS alarms");
    		onCreate(db);
    	}
    	
    	if (oldVersion == 5) {
            // Alarm Tables: Add alertCount
            try {
                db.execSQL("alter table " + "alarms"
                        + " add column " + "alertCount" + " integer" + ";");
            } catch (SQLException e) {
                // Shouldn't be needed unless we're debugging and interrupt the process
                Log.e(TAG, "Exception upgrading dalarms.db from v5 to v6", e);
            }
            oldVersion = 6;
        }
    	/*PR 585405- Neo Skunkworks - Paul Xu modified - 001 End*/
    }

    public Uri commonInsert(ContentValues values) {
        SQLiteDatabase db = getWritableDatabase();
        long rowId = db.insert("alarms", Alarm.Columns.MESSAGE, values);
        if (rowId < 0) {
            throw new SQLException("Failed to insert row");
        }
        return ContentUris.withAppendedId(Alarm.Columns.CONTENT_URI, rowId);
    }
}
