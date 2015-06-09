/* File Name:TimeZones.java
 * Version:V1.0
 * Author:jingjiang.yu
 * Date:2011-7-21 04:52:17PM
 * CopyRight (c) 2011, TCL Communication All Rights Reserved.
 */

package com.jrdcom.timetool.worldclock.provider;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import org.xmlpull.v1.XmlPullParserException;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.res.XmlResourceParser;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.database.MergeCursor;

import com.android.deskclock.R;
import com.jrdcom.timetool.worldclock.provider.TimeZoneInfo.Columns;

public class TimeZones {

	private static final String XMLTAG_TIMEZONE = "timezone";

	private static final String XML_ATTR_NAME_TIME_ZONE_ID = "time_zone_id";

	private static final String XML_ATTR_NAME_DISPLAY_NAME_ID = "display_name_id";
    // PR:488578 add by XIBIN
//	private static final String XML_ATTR_NAME_DEFAULT_SHOW = "dafault_show";

	private TimeZones() {

	}

	// obtaining time zone information from XML files.
	public static List<TimeZoneInfo> getTimeZoneConfig(Context context) {
		List<TimeZoneInfo> myData = new ArrayList<TimeZoneInfo>();
		long date = Calendar.getInstance().getTimeInMillis();
		try {
			XmlResourceParser xrp = context.getResources().getXml(
					R.xml.timezones);
			while (xrp.next() != XmlResourceParser.START_TAG) {
				continue;
			}

			xrp.next();
			while (xrp.getEventType() != XmlResourceParser.END_TAG) {
				while (xrp.getEventType() != XmlResourceParser.START_TAG) {
					if (xrp.getEventType() == XmlResourceParser.END_DOCUMENT) {
						xrp.close();
						return myData;
					}
					xrp.next();
				}
				if (xrp.getName().equals(XMLTAG_TIMEZONE)) {
					String timeZoneId = xrp.getAttributeValue(null,
							XML_ATTR_NAME_TIME_ZONE_ID);
					String displayNameId = xrp.getAttributeValue(null,
							XML_ATTR_NAME_DISPLAY_NAME_ID);

					boolean isDefaultShow = false;
                    // PR:488578 add by XIBIN start --integrate world clock widget in Timetool
                    // if (!isDefaultShow
                    // && timeZoneId.equals(TimeZone.getDefault().getID())) {
                    // isDefaultShow = true;
                    // }
                    // PR:488578 add by XIBIN end
					addItem(myData, timeZoneId, displayNameId, date,
							isDefaultShow);
				}
				while (xrp.getEventType() != XmlResourceParser.END_TAG) {
					xrp.next();
				}
				xrp.next();
			}
			xrp.close();
		} catch (XmlPullParserException e) {
		} catch (java.io.IOException e) {
		}

		return myData;
	}

	private static void addItem(List<TimeZoneInfo> myData, String timeZoneId,
			String displayNameId, long date, boolean isDefaultShow) {
		TimeZoneInfo timeZoneInfo = new TimeZoneInfo();
		timeZoneInfo.timeZoneId = timeZoneId;
		timeZoneInfo.displayNameId = displayNameId;
		TimeZone tz = TimeZone.getTimeZone(timeZoneId);
		int offset = tz.getOffset(date);
		timeZoneInfo.offset = offset;
		timeZoneInfo.updateTime = date;
        // PR:488578 add by XIBIN start --integrate world clock widget in Timetools
//		if (isDefaultShow) {
//			timeZoneInfo.isDefaultShow = isDefaultShow;
//			timeZoneInfo.isShow = true;
//		}
        // PR:488578 add by XIBIN end
		myData.add(timeZoneInfo);
	}

	/**
	 * Obtain content of Strings base on the "R.String.id" of the showing zone.
	 * 
	 * @param context
	 * @param displayNameId
	 * @return
	 */
	public static String getDisplayNameById(Context context,
			String displayNameId) {
		int displayNameStringId = 0;
		try {
			displayNameStringId = R.string.class.getField(displayNameId)
					.getInt(null);
		} catch (IllegalArgumentException e) {
			return null;
		} catch (SecurityException e) {
			return null;
		} catch (IllegalAccessException e) {
			return null;
		} catch (NoSuchFieldException e) {
			return null;
		} catch (Exception e) {
			return null;
		}

		return context.getString(displayNameStringId);
	}

	/**
	 * Return the GMT string base on the offset of the time zone.
	 * 
	 * @param offset
	 * @return
	 */
	public static String getGMTNameByOffset(int offset) {
		int p = Math.abs(offset);
		StringBuilder gmtStrBuilder = new StringBuilder();
		gmtStrBuilder.append("GMT");
		if (offset < 0) {
			gmtStrBuilder.append('-');
		} else {
			gmtStrBuilder.append('+');
		}
		gmtStrBuilder.append(p / 3600000);
		gmtStrBuilder.append(':');

		int min = p / 60000;
		min %= 60;

		if (min < 10) {
			gmtStrBuilder.append('0');
		}
		gmtStrBuilder.append(min);
		return gmtStrBuilder.toString();
	}
    // PR:488578 add by XIBIN start --integrate world clock widget in Timetool
    /**
     * Obtain the cities that were showed.
     *
     * @param contentResolver
     * @return
     */
    public static Cursor getShowedTimeZone(Context context,
            boolean displayLocalZone) {
        ContentResolver contentResolver = context.getContentResolver();
        Cursor setZone = contentResolver.query(
                TimeZoneInfo.Columns.CONTENT_URI,
                TimeZoneInfo.Columns.TIME_ZONE_INFO_QUERY_COLUMNS,
                TimeZoneInfo.Columns.WHERE_SHOWED, null,
                TimeZoneInfo.Columns.UPDATE_TIME_SORT_ORDER);
        // modify by liang.zhang for PR 889340 at 2015-01-04 begin
        // modify by liang.zhang for PR 880930 at 2014-12-23 begin
        String currentTimeZoneId = "";
        Cursor[] cursors = new Cursor[2];
        Cursor currentCursor = getCurrentTimeZone(context);
        if (currentCursor == null || currentCursor.getCount() == 0) {
            MatrixCursor localZone = new MatrixCursor(
                    TimeZoneInfo.Columns.TIME_ZONE_INFO_QUERY_COLUMNS);
            Object[] columnValues = new Object[TimeZoneInfo.Columns.TIME_ZONE_INFO_QUERY_COLUMNS.length];

            TimeZone timeZone = TimeZone.getDefault();
            columnValues[Columns.ID_INDEX] = -1;
            columnValues[Columns.TIME_ZONE_ID_INDEX] = timeZone.getID();
            currentTimeZoneId = timeZone.getID();
            localZone.addRow(columnValues);
            cursors[0] = localZone;
            // PR :470784 update by xibin start
            if(currentCursor != null){
                currentCursor.close();
            }
            // PR :470784 update by xibin end
        } else {
            cursors[0] = currentCursor;
            if (currentCursor.moveToFirst()) {
            	currentTimeZoneId = currentCursor.getString(
                		currentCursor.getColumnIndex(TimeZoneInfo.Columns.TIME_ZONE_ID));
            }
        }
        
        MatrixCursor cursor = new MatrixCursor(TimeZoneInfo.Columns.TIME_ZONE_INFO_QUERY_COLUMNS);
        Object[] values = new Object[TimeZoneInfo.Columns.TIME_ZONE_INFO_QUERY_COLUMNS.length];
        if (setZone != null && setZone.getCount() -1 >= 0) { // modify by liang.zhang for PR 889332 at 2015-01-04
        	while (setZone.moveToNext()) {
        		TimeZoneInfo info = new TimeZoneInfo(setZone, context);
        		if (!currentTimeZoneId.equals(info.timeZoneId)) {
        			values[Columns.ID_INDEX] = info.id;
        			values[Columns.TIME_ZONE_ID_INDEX] = info.timeZoneId;
        			values[Columns.DISPLAY_NAME_ID_INEDX] = info.displayNameId;
        			values[Columns.IS_SHOW_INEDX] = info.isShow ? 0 : 1;
        			values[Columns.OFF_SET_INEDX] = info.offset;
        			values[Columns.UPDATE_TIME_INEDX] = info.updateTime;
        			values[Columns.IS_DEFAULT_SHOW_INDEX] = info.isDefaultShow ? 0 : 1;
        			values[Columns.SUMMER_TIME_INDEX] = info.summerTime;
        			cursor.addRow(values);
        		}
        	}
        }
        
        // add by liang.zhang for PR 927579 at 2015-02-27 begin
        if (setZone != null) {
        	setZone.close();
        }
        // add by liang.zhang for PR 927579 at 2015-02-27 end
        cursors[1] = cursor;

        if (displayLocalZone) {
            return new MergeCursor(cursors);
            // modify by liang.zhang for PR 880930 at 2014-12-23 end
        } else {
            return cursor;
        }
        // modify by liang.zhang for PR 889340 at 2015-01-04 end
    }
    // PR:488578 add by XIBIN end
	/**
	 * Obtain all of cities' times.
	 * 
	 * @param context
	 * @return
	 */
	public static List<TimeZoneInfo> getAllTimeZone(Context context) {
		List<TimeZoneInfo> timezoneList = new ArrayList<TimeZoneInfo>();

		Cursor cursor = context.getContentResolver().query(
				TimeZoneInfo.Columns.CONTENT_URI,
				TimeZoneInfo.Columns.TIME_ZONE_INFO_QUERY_COLUMNS, null, null,
				TimeZoneInfo.Columns.DEFAULT_SORT_ORDER);
		if (cursor == null || cursor.getCount() <= 0) {
			if (cursor != null){
			cursor.close();// PR -605593 - Neo Skunworks - Soar Gao , add -001
			}
			return timezoneList;
		}

		for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {
			TimeZoneInfo timezone = new TimeZoneInfo(cursor, context);
			timezoneList.add(timezone);
		}

		cursor.close();

		return timezoneList;
	}

	/**
	 * Search the city base on the searchName,return all cities while the
	 * searchName is empty.
	 * 
	 * @param context
	 * @param searchName
	 * @return
	 */
	public static List<TimeZoneInfo> getTimeZoneByName(Context context,
			String searchName) {
		List<TimeZoneInfo> timezoneList = new ArrayList<TimeZoneInfo>();
		// when searchName is empty
		if (searchName == null || "".equals(searchName.trim())) {
			return getAllTimeZone(context);
		}

		Cursor cursor = context.getContentResolver().query(
				TimeZoneInfo.Columns.CONTENT_URI,
				TimeZoneInfo.Columns.TIME_ZONE_INFO_QUERY_COLUMNS, null, null,
				TimeZoneInfo.Columns.DEFAULT_SORT_ORDER);
		if (cursor == null || cursor.getCount() <= 0) {
			if (cursor != null){
			cursor.close();// PR -605593 - Neo Skunworks - Soar Gao , add -001
			}
			return timezoneList;
		}

		for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {
			TimeZoneInfo timezone = new TimeZoneInfo(cursor, context);
			if (timezone.displayName == null
					|| "".equals(timezone.displayName.trim())) {
				continue;
			}

            // add by XIBIN for PR:437757 begin--it needs match automaticlly when input low-case.
            String displayName = timezone.displayName;
            if ((searchName.length() == 1)
                    && searchName.equals(searchName.toUpperCase())) {
                if (displayName.indexOf(searchName) != -1) {
                    timezoneList.add(timezone);
                }
            } else {
                displayName = displayName.toLowerCase();
                searchName = searchName.toLowerCase();
                // if (timezone.displayName.indexOf(searchName) != -1) {
                if (displayName.indexOf(searchName) != -1) {
                    timezoneList.add(timezone);
                }
            }
            // add by XIBIN for PR:437757 end
		}

		cursor.close();
		return timezoneList;
	}

	/**
	 * Refresh informations in DB.
	 * 
	 * @param contentResolver
	 * @param timezone
	 */
	public static void updateTimeZone(ContentResolver contentResolver,
			TimeZoneInfo timezone) {
		ContentValues values = createContentValues(timezone);

		contentResolver.update(ContentUris.withAppendedId(
				TimeZoneInfo.Columns.CONTENT_URI, timezone.id), values, null,
				null);
	}
	
	/*PR 567420- Neo Skunkworks - Paul Xu added - 001 Begin*/
    public static void setTimeZoneOffset(TimeZoneInfo pTimeZoneInfo){
    	TimeZone tz = TimeZone.getTimeZone(pTimeZoneInfo.timeZoneId);
        long date = Calendar.getInstance().getTimeInMillis();
        int offset = tz.getOffset(date);
        pTimeZoneInfo.offset = offset;
    }
    /*PR 567420- Neo Skunkworks - Paul Xu added - 001 Begin*/

	private static ContentValues createContentValues(TimeZoneInfo timezone) {
		ContentValues values = new ContentValues(7);
		values.put(TimeZoneInfo.Columns.TIME_ZONE_ID, timezone.timeZoneId);
		values.put(TimeZoneInfo.Columns.DISPLAY_NAME_ID, timezone.displayNameId);
		values.put(TimeZoneInfo.Columns.IS_SHOW, timezone.isShow ? 1 : 0);
		/*PR 567420- Neo Skunkworks - Paul Xu added - 001 Begin*/		
		setTimeZoneOffset(timezone);
        /*PR 567420- Neo Skunkworks - Paul Xu added - 001 End*/
		values.put(TimeZoneInfo.Columns.OFF_SET, timezone.offset);
		values.put(TimeZoneInfo.Columns.UPDATE_TIME, timezone.updateTime);
		values.put(TimeZoneInfo.Columns.IS_DEFAULT_SHOW,
				timezone.isDefaultShow ? 1 : 0);
		values.put(TimeZoneInfo.Columns.SUMMER_TIME, timezone.summerTime);

		return values;
	}
    // PR:488578 add by XIBIN start --integrate world clock widget in Timetool
    public static List<TimeZoneInfo> getShowedTimeZone(Context context) {

        List<TimeZoneInfo> timezoneList = new ArrayList<TimeZoneInfo>();
        Cursor cursor = getShowedTimeZone(context, true);
        try {
            if (cursor == null || cursor.getCount() <= 0) {
            	if(cursor!=null){
				cursor.close();// PR -605593 - Neo Skunworks - Soar Gao , add -001
            	}
				return timezoneList;
            }

            for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {
                TimeZoneInfo timezone = new TimeZoneInfo(cursor, context);
                if (timezone.displayName == null
                        || "".equals(timezone.displayName.trim())) {
                    continue;
                }
                timezoneList.add(timezone);
            }
        } finally {
            if(cursor != null) {
                cursor.close();
            }
        }
        if(cursor!=null){
        cursor.close();// PR -605593 - Neo Skunworks - Soar Gao , add -001
        }
        return timezoneList;
    }

    public static Cursor getCurrentTimeZone(Context context) {
        String where = TimeZoneInfo.Columns.TIME_ZONE_ID + "=\""
                + TimeZone.getDefault().getID() + "\"";
        ContentResolver contentResolver = context.getContentResolver();
        Cursor cursor = contentResolver.query(TimeZoneInfo.Columns.CONTENT_URI,
                TimeZoneInfo.Columns.TIME_ZONE_INFO_QUERY_COLUMNS, where, null,
                TimeZoneInfo.Columns.UPDATE_TIME_SORT_ORDER);
        return cursor;
    }
    // PR:488578 add by XIBIN end
}
