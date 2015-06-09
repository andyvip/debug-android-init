/* File Name:TimeZoneInfo.java
 * Version:V1.0
 * Author:jingjiang.yu
 * Date:2011-7-21 03:08:39PM
 * CopyRight (c) 2011, TCL Communication All Rights Reserved.
 */

package com.jrdcom.timetool.worldclock.provider;

import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.BaseColumns;

/**
 * @author jingjiang.yu
 * @since V 1.0
 * @Date 2011-7-21 03:08:39PM
 */
public class TimeZoneInfo {
	/**
	 * Column definitions
	 */
	public static class Columns implements BaseColumns {
		/**
		 * The content:// style URL for this table
		 */
		public static final Uri CONTENT_URI = Uri
				.parse("content://com.jrdcom.timetool.worldclock/timezone");

		public static final String TIME_ZONE_ID = "timezone_id";

		public static final String DISPLAY_NAME_ID = "display_name_id";

		// 1:show 0: do not show
		public static final String IS_SHOW = "is_show";

		public static final String OFF_SET = "off_set";

		public static final String UPDATE_TIME = "update_time";

		public static final String IS_DEFAULT_SHOW = "is_default_show";

		public static final String SUMMER_TIME = "summer_time";

		/**
		 * The default sort order for this table
		 */
		public static final String DEFAULT_SORT_ORDER = OFF_SET + " ASC";

		public static final String UPDATE_TIME_SORT_ORDER = UPDATE_TIME
				+ " ASC";

		// Used when filtering show timeZoneInfos.
		public static final String WHERE_SHOWED = IS_SHOW + "=1";

		public static final String[] TIME_ZONE_INFO_QUERY_COLUMNS = { _ID,
				TIME_ZONE_ID, DISPLAY_NAME_ID, IS_SHOW, OFF_SET, UPDATE_TIME,
				IS_DEFAULT_SHOW, SUMMER_TIME };

		/**
		 * These save calls to cursor.getColumnIndexOrThrow() THEY MUST BE KEPT
		 * IN SYNC WITH ABOVE QUERY COLUMNS
		 */
		public static final int ID_INDEX = 0;

		public static final int TIME_ZONE_ID_INDEX = 1;

		public static final int DISPLAY_NAME_ID_INEDX = 2;

		public static final int IS_SHOW_INEDX = 3;

		public static final int OFF_SET_INEDX = 4;

		public static final int UPDATE_TIME_INEDX = 5;

		public static final int IS_DEFAULT_SHOW_INDEX = 6;

		public static final int SUMMER_TIME_INDEX = 7;
	}

	/** ID in DB **/
	public int id;

	/** ID of TimeZone **/
	public String timeZoneId;

	/** ID of display name **/
	public String displayNameId;

	/** display Name **/
	public String displayName;

	/** is Showed or not **/
	public boolean isShow;

	/** TimeZone offset,for sorting **/
	public int offset;

	/** update(set showing or not)time,for sorting **/
	public long updateTime;

	/** is default workClock. **/
	public boolean isDefaultShow;

	public static final int DEFAULT_SHOW_COUNT = 1;

	public int summerTime;

	public static final int SUMMERTIME_NONE = 0;

	public static final int SUMMERTIME_ONE_HOUR = 1;

	public static final int SUMMERTIME_TWO_HOUR = 2;

	public TimeZoneInfo() {
		id = -1;
		isShow = false;
		isDefaultShow = false;
		summerTime = SUMMERTIME_NONE;
	}

	public TimeZoneInfo(Cursor c, Context context) {
		id = c.getInt(Columns.ID_INDEX);
		timeZoneId = c.getString(Columns.TIME_ZONE_ID_INDEX);
		displayNameId = c.getString(Columns.DISPLAY_NAME_ID_INEDX);
		isShow = c.getInt(Columns.IS_SHOW_INEDX) == 1;
		offset = c.getInt(Columns.OFF_SET_INEDX);
		updateTime = c.getLong(Columns.UPDATE_TIME_INEDX);
		isDefaultShow = c.getInt(Columns.IS_DEFAULT_SHOW_INDEX) == 1;

		summerTime = c.getInt(Columns.SUMMER_TIME_INDEX);

		displayName = TimeZones.getDisplayNameById(context, displayNameId);
        // PR:488578 add by XIBIN start --integrate world clock widget in Timetool
        if (displayName == null || displayName.trim().length() == 0) {
            // PR:510625 add by XIBIN start
            Calendar calendar = Calendar.getInstance();
            offset = calendar.getTimeZone().getOffset(
                    calendar.getTimeInMillis());
            // PR:510625 add by XIBIN end
            TimeZone tz = TimeZone.getDefault();
            Date now = new Date();
            displayName = (tz.getDisplayName(tz.inDaylightTime(now),
                    TimeZone.LONG)).toString();
        }
     // PR:488578 add by XIBIN end
	}
}
