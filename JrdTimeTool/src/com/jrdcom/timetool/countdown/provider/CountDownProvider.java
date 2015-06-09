/************************************************************************************************************/
/*                                                                                           Date : 12/2013 */
/*                                      PRESENTATION                                                        */
/*                        Copyright (c) 2012 JRD Communications, Inc.                                       */
/************************************************************************************************************/
/*                                                                                                          */
/*              This material is company confidential, cannot be reproduced in any                          */
/*              form without the written permission of JRD Communications, Inc.                             */
/*                                                                                                          */
/*==========================================================================================================*/
/*   Author :                                                                                               */
/*   Role :    TimeTool                                                                                      */
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
/* 12/24/13 |Haidong Wang |PR574723-Haidong-Wang-001        |AT command for count down        */
/*=========|==============|==========================================|======================================*/

package com.jrdcom.timetool.countdown.provider;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;

public class CountDownProvider extends ContentProvider {
    private static final String DATABASE_NAME = "count_down.db";
    private static final int DATABASE_VERSION = 1;
    private static final String TABLE_NAME = "count_down";
    public static final String CONTENT_URI = "com.jrdcom.timetool.provider";
    private SQLiteOpenHelper mCountDownHelper;
    private SQLiteDatabase countDownDB;

    public static class DatabaseHelper extends SQLiteOpenHelper {
        public DatabaseHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL("Create table if not exists " + TABLE_NAME + "( _id INTEGER PRIMARY KEY AUTOINCREMENT, count_time INTEGER);");
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
            onCreate(db);
        }

    }

    @Override
    public boolean onCreate() {
        mCountDownHelper = new DatabaseHelper(getContext());
        return (mCountDownHelper == null) ? false : true;
    }

    @Override
    public String getType(Uri uri) {
        return null;
    }

    @Override
    public Uri insert(Uri uri, ContentValues contentvalues) {
        countDownDB = mCountDownHelper.getWritableDatabase();
        long rowId = countDownDB.insert(TABLE_NAME, "", contentvalues);
        if (rowId > 0) {
            Uri rowUri = ContentUris.appendId(Uri.parse("content://" + CONTENT_URI).buildUpon(), rowId).build();
            getContext().getContentResolver().notifyChange(rowUri, null);
            return rowUri;
        }
        throw new SQLException("Failed to insert row into" + uri);
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        countDownDB = mCountDownHelper.getWritableDatabase();
        int count = countDownDB.delete(TABLE_NAME, selection, selectionArgs);
        getContext().getContentResolver().notifyChange(uri, null);
        return count;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection,
            String[] selectionArgs) {
        return 0;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
        SQLiteDatabase db = mCountDownHelper.getReadableDatabase();
        qb.setTables(TABLE_NAME);
        Cursor c = qb.query(db, projection, selection, null, null, null, sortOrder);
        c.setNotificationUri(getContext().getContentResolver(), uri);
        return c;
    }
}
