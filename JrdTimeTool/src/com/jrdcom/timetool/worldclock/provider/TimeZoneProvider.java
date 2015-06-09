/* File Name:TimeZoneProvider.java
 * Version:V1.0
 * Author:jingjiang.yu
 * Date:2011-7-21 04:39:57PM
 * CopyRight (c) 2011, TCL Communication All Rights Reserved.
 */

package com.jrdcom.timetool.worldclock.provider;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.text.TextUtils;

import java.util.List;

/**
 * @author jingjiang.yu
 * @since V 1.0
 * @Date 2011-7-21 04:39:57PM
 */
public class TimeZoneProvider extends ContentProvider {

    private SQLiteOpenHelper mOpenHelper;

    private static final int TIMEZONES = 1;

    private static final int TIMEZONE_ID = 2;

    private static final UriMatcher sURLMatcher = new UriMatcher(
            UriMatcher.NO_MATCH);

    static {
        sURLMatcher.addURI("com.jrdcom.timetool.worldclock", "timezone",
                TIMEZONES);
        sURLMatcher.addURI("com.jrdcom.timetool.worldclock", "timezone/#",
                TIMEZONE_ID);
    }

    private static class DatabaseHelper extends SQLiteOpenHelper {
        private static final String DATABASE_NAME = "timezones.db";

        private static final int DATABASE_VERSION = 5;

        private Context mContext;

        public DatabaseHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
            mContext = context;
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL("CREATE TABLE timezones ("
                    + "_id INTEGER PRIMARY KEY,"
                    + "timezone_id TEXT, "
                    + "display_name_id TEXT, "
                    + "is_show INTEGER, "
                    + "off_set INTEGER, "
                    + "update_time INTEGER, is_default_show INTEGER, summer_time INTEGER);");

            insertDefaultData(db);
        }

        private void insertDefaultData(SQLiteDatabase db) {
            List<TimeZoneInfo> TimeZoneList = TimeZones
                    .getTimeZoneConfig(mContext);

            if (TimeZoneList == null || TimeZoneList.size() <= 0) {
                return;
            }

            String insertMe = "INSERT INTO timezones "
                    + "(timezone_id, display_name_id, is_show, off_set, update_time, is_default_show, summer_time) "
                    + "VALUES ";
            StringBuilder insertSQL = new StringBuilder();
            for (TimeZoneInfo timeZoneInfo : TimeZoneList) {
                insertSQL.delete(0, insertSQL.length());

                insertSQL.append(insertMe);
                insertSQL.append("('");
                insertSQL.append(timeZoneInfo.timeZoneId);
                insertSQL.append("', '");
                insertSQL.append(timeZoneInfo.displayNameId);
                insertSQL.append("', ");
                if (timeZoneInfo.isShow) {
                    insertSQL.append(1);
                } else {
                    insertSQL.append(0);
                }
                insertSQL.append(", ");
                insertSQL.append(timeZoneInfo.offset);
                insertSQL.append(", ");
                insertSQL.append(timeZoneInfo.updateTime);
                insertSQL.append(", ");
                // PR:488578 add by XIBIN start --integrate world clock widget in Timetool
//                if (timeZoneInfo.isDefaultShow) {
//                    insertSQL.append(1);
//                } else {
                    insertSQL.append(0);
//                }
                // PR:488578 add by XIBIN end
                insertSQL.append(", ");
                insertSQL.append(timeZoneInfo.summerTime);
                insertSQL.append(");");

                db.execSQL(insertSQL.toString());
            }

        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion,
                int currentVersion) {
            db.execSQL("DROP TABLE IF EXISTS timezones");
            onCreate(db);
        }
    }

    @Override
    public boolean onCreate() {
        mOpenHelper = new DatabaseHelper(getContext());
        return true;
    }

    @Override
    public Cursor query(Uri url, String[] projectionIn, String selection,
            String[] selectionArgs, String sort) {
        SQLiteQueryBuilder qb = new SQLiteQueryBuilder();

        // Generate the body of the query
        int match = sURLMatcher.match(url);
        switch (match) {
            case TIMEZONES:
                qb.setTables("timezones");
                break;
            case TIMEZONE_ID:
                qb.setTables("timezones");
                qb.appendWhere("_id=");
                qb.appendWhere(url.getPathSegments().get(1));
                break;
            default:
                throw new IllegalArgumentException("Unknown URL " + url);
        }

        SQLiteDatabase db = mOpenHelper.getReadableDatabase();
        Cursor ret = qb.query(db, projectionIn, selection, selectionArgs, null,
                null, sort);

        if (ret == null) {

        } else {
            ret.setNotificationUri(getContext().getContentResolver(), url);
        }

        return ret;
    }

    @Override
    public String getType(Uri url) {
        int match = sURLMatcher.match(url);
        switch (match) {
            case TIMEZONES:
                return "vnd.android.cursor.dir/timezones";
            case TIMEZONE_ID:
                return "vnd.android.cursor.item/timezones";
            default:
                throw new IllegalArgumentException("Unknown URL");
        }
    }

    @Override
    public int update(Uri url, ContentValues values, String where,
            String[] whereArgs) {
        int count;
        long rowId = 0;
        int match = sURLMatcher.match(url);
        SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        switch (match) {
            case TIMEZONE_ID: {
                String segment = url.getPathSegments().get(1);
                rowId = Long.parseLong(segment);
                count = db.update("timezones", values, "_id=" + rowId, null);
                break;
            }
            default: {
                throw new UnsupportedOperationException("Cannot update URL: " + url);
            }
        }
        getContext().getContentResolver().notifyChange(url, null);
        return count;
    }

    @Override
    public Uri insert(Uri url, ContentValues initialValues) {
        if (sURLMatcher.match(url) != TIMEZONES) {
            throw new IllegalArgumentException("Cannot insert into URL: " + url);
        }

        ContentValues values = new ContentValues(initialValues);

        SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        long rowId = db.insert("timezones", null, values);
        if (rowId < 0) {
            throw new SQLException("Failed to insert row into " + url);
        }

        Uri newUrl = ContentUris.withAppendedId(
                TimeZoneInfo.Columns.CONTENT_URI, rowId);
        getContext().getContentResolver().notifyChange(newUrl, null);

        return newUrl;
    }

    @Override
    public int delete(Uri url, String where, String[] whereArgs) {
        SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        int count;
        long rowId = 0;
        switch (sURLMatcher.match(url)) {
            case TIMEZONES:
                count = db.delete("timezones", where, whereArgs);
                break;
            case TIMEZONE_ID:
                String segment = url.getPathSegments().get(1);
                rowId = Long.parseLong(segment);
                if (TextUtils.isEmpty(where)) {
                    where = "_id=" + rowId;
                } else {
                    where = "_id=" + rowId + " AND (" + where + ")";
                }
                count = db.delete("timezones", where, whereArgs);
                break;
            default:
                throw new IllegalArgumentException("Cannot delete from URL: " + url);
        }
        getContext().getContentResolver().notifyChange(url, null);
        return count;
    }

}
