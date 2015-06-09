package com.jrdcom.timetool.worldclock.widget;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import android.appwidget.AppWidgetManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.ContentObserver;
import android.database.Cursor;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.text.format.DateFormat;
import android.view.View;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;
import android.util.Log;

import com.jrdcom.timetool.worldclock.provider.TimeZoneInfo;
import com.jrdcom.timetool.worldclock.provider.TimeZones;
import com.android.deskclock.R;

/**
 * UpdateService is called when the first widget is created. This service is
 * used to update the widget.
 */
public class UpdateService extends RemoteViewsService {
    private boolean isGetData = true;
    private static final String TAG = UpdateService.class.getSimpleName();
    // private AppWidgetManager manager;
    private ContentObserver mObserver = new ContentObserver(new Handler()) {
        public void onChange(boolean selfChange) {
            Log.v(TAG, "UpdateService ->ContentObserver : onChange()");
            isGetData = true;
            notifyAppWidgetViewDataChanged(getApplicationContext());
        };
    };

    private void notifyAppWidgetViewDataChanged(Context context) {
        final ComponentName cn = new ComponentName(context,
                WorldClockProvider.class);
        AppWidgetManager widgetManager = AppWidgetManager.getInstance(context);
        widgetManager.notifyAppWidgetViewDataChanged(
                widgetManager.getAppWidgetIds(cn), R.id.widget_list);
    }

    @Override
    public void onCreate() {
        // TODO Auto-generated method stub
        super.onCreate();
        //PR470797 add by xibin
        notifyAppWidgetViewDataChanged(getApplicationContext());
        getContentResolver().registerContentObserver(
                TimeZoneInfo.Columns.CONTENT_URI, true, mObserver);

    }

    // ListRemoteViewsFactory factory;
    /**
     * a BroadcastReceiver is used to receive the update news, and update the
     * widget.
     */
    private final BroadcastReceiver mIntentReceiver = new BroadcastReceiver() {

        public void onReceive(Context context, Intent intent) {
            Log.e(TAG, "UpdateService : mIntentReceiver : onReceive");
            String action = intent.getAction();
            if (Intent.ACTION_TIMEZONE_CHANGED.equals(action) || Intent.ACTION_LOCALE_CHANGED.equals(action)) {
                isGetData = true;
            }
            notifyAppWidgetViewDataChanged(getApplicationContext());
        }
    };

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // restart the service when the service was killed
        if (intent == null) {
            return START_STICKY;
        }
        return super.onStartCommand(intent, flags, startId);
    }


    @Override
    public void onDestroy() {
        Log.e(TAG, "UpdateService : mIntentReceiver : onDestroy");
        super.onDestroy();
        getContentResolver().unregisterContentObserver(mObserver);
    }

    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
        Log.e(TAG, "RemoteViewsFactory : onGetViewFactory");
        return new ListRemoteViewsFactory(this.getApplicationContext(), intent);
    }

    class ListRemoteViewsFactory implements
            RemoteViewsService.RemoteViewsFactory {
        private final Context mContext;
        private List<TimeZoneInfo> mList;
        private int size;
        private boolean isOddNumber = false;
        private final static String M24 = "kk:mm";
        private final static String M12 = "h:mm";
        private final int start_hour = 06;
        private final int end_hour = 18;
        private final int start13_hour = 06;
        private final int end13_hour = 11;
        private final static String dateFormat = "MM.dd";

        public ListRemoteViewsFactory(Context context, Intent intent) {
            mContext = context;
            if (Looper.myLooper() == null) {
                Looper.prepare();
            }
        }

        @Override
        public void onCreate() {
            Log.e(TAG, "ListRemoteViewsFactory : mIntentReceiver : onCreate");

            IntentFilter filter = new IntentFilter();
            // modify by fan.yang for PR 952336 at 2015-3-25 start
//            filter.addAction(Intent.ACTION_TIME_CHANGED);
            filter.addAction(Intent.ACTION_TIMEZONE_CHANGED);
            filter.addAction("com.android.broadcasttest.CHANGE_THEME");
            filter.addAction(Intent.ACTION_LOCALE_CHANGED);
//            filter.addAction(Intent.ACTION_TIME_TICK);
            // modify by fan.yang for PR 952336 at 2015-3-25 end
            filter.addCategory(Intent.CATEGORY_DEFAULT);
            // PR:499760 add by xibin
            filter.addAction("android.intent.action.dateformatchange");
            getApplicationContext().registerReceiver(mIntentReceiver, filter,
                    null, null);
        }

        @Override
        public void onDataSetChanged() {
            Log.e(TAG, "ListRemoteViewsFactory : onDataSetChanged: isGetData: "+ isGetData);
            if (isGetData) {
                isGetData = false;
                mList = TimeZones.getShowedTimeZone(mContext);
            }
            size = mList.size();
            isOddNumber = size % 2 == 0 ? false : true;
            Log.e(TAG, "ListRemoteViewsFactory : onDataSetChanged: mList.size(): "+ size + " isOddNumber: "+ isOddNumber);
        }

        @Override
        public void onDestroy() {
            Log.e(TAG, "ListRemoteViewsFactory : onDestroy");
            if (mList != null)
                mList.clear();
            mContext.unregisterReceiver(mIntentReceiver);
        }

        @Override
        public int getCount() {
            if (mList == null) {
                return 0;
            }
            int size = (mList.size() - 1) / 2 + 1;
            Log.e(TAG, "getCount() :" + size);
            return size;
        }

        @Override
        public RemoteViews getViewAt(int position) {
            if (position < 0 || position >= getCount() || mList == null
                    || mList.size() == 0)
                return null;
            final RemoteViews rv = new RemoteViews(mContext.getPackageName(),
                    R.layout.widget_list_item);
            Intent newIntent = new Intent();
            rv.setOnClickFillInIntent(R.id.linearLayout, newIntent);
            if (position + 1 == getCount() && isOddNumber) {
                Log.e(TAG, "getViewAt: only one city");
                TimeZoneInfo timeZoneInfo = mList.get(mList.size() - 1);
                rv.setViewVisibility(R.id.widget_item_1, View.GONE);
                rv.setViewVisibility(R.id.widget_item_2, View.GONE);
                rv.setViewVisibility(R.id.widget_item_3, View.VISIBLE);
                initTime(rv, timeZoneInfo, R.id.city_3, R.id.time_3,
                        R.id.date_3, R.id.am_pm_3, R.id.day_night_icon_3);
                return rv;
            }
            Log.e(TAG, "getViewAt: two city");
            rv.setViewVisibility(R.id.widget_item_1, View.VISIBLE);
            rv.setViewVisibility(R.id.widget_item_2, View.VISIBLE);
            rv.setViewVisibility(R.id.widget_item_3, View.GONE);

            position = 2 * position;
            TimeZoneInfo timeZoneInfo = mList.get(position);

            initTime(rv, timeZoneInfo, R.id.city_1, R.id.time_1, R.id.date_1,
                    R.id.am_pm_1, R.id.day_night_icon_1);
            if (position + 1 < size) {
                timeZoneInfo = mList.get(position + 1);
                initTime(rv, timeZoneInfo, R.id.city_2, R.id.time_2,
                        R.id.date_2, R.id.am_pm_2, R.id.day_night_icon_2);
            }
            return rv;
        }

        private void initTime(RemoteViews rv, TimeZoneInfo timeZone,
                int idCity, int idTime, int idDate, int idAmOrPm,
                int idDayNightIcon) {
        	
            rv.setTextViewText(idCity, timeZone.displayName);
            
            // acquire time of current time zone
            Calendar calendar = Calendar.getInstance();
            /*PR 567420- Neo Skunkworks - Paul Xu added - 001 Begin*/
            TimeZones.setTimeZoneOffset(timeZone);
            /*PR 567420- Neo Skunkworks - Paul Xu added - 001 End*/
            long nowMilliseconds = calendar.getTimeInMillis()
                    - calendar.getTimeZone().getOffset(
                            calendar.getTimeInMillis()) + timeZone.offset;
            calendar.setTimeInMillis(nowMilliseconds);

            boolean is24Hour = android.text.format.DateFormat
                    .is24HourFormat(mContext);
            String timeFormat = is24Hour ? M24 : M12;
            // modify by fan.yang for PR 952336 at 2015-3-25 start
            //rv.setTextViewText(idTime, DateFormat.format(timeFormat, calendar));
            rv.setString(idTime, "setTimeZone", timeZone.timeZoneId);
            // modify by fan.yang for PR 952336 at 2015-3-25 end
            // PR:499760 add xibin start
            java.text.DateFormat shortDateFormat = DateFormat
                    .getDateFormat(mContext);
            // SimpleDateFormat format = new SimpleDateFormat(dateFormat);
            // modify by fan.yang for PR 952336 at 2015-3-25 start
            //rv.setTextViewText(idDate,shortDateFormat.format(calendar.getTime()));
            rv.setString(idDate, "setTimeZone", timeZone.timeZoneId);
            // modify by fan.yang for PR 952336 at 2015-3-25 end
            // PR:499760 add xibin end
            // set AM PM
            String strs = (String) DateFormat.format(timeFormat, calendar);
            String[] dds = new String[] {};
            dds = strs.split(":");
            int dhs = Integer.parseInt(dds[0]);
            // modify by fan.yang for PR 952336 at 2015-3-25 start
            if (!is24Hour) {
                rv.setViewVisibility(idAmOrPm, View.VISIBLE);
                rv.setString(idDayNightIcon, "setTimeZone", timeZone.timeZoneId);
                rv.setString(idAmOrPm, "setTimeZone", timeZone.timeZoneId);
            } else {
                rv.setViewVisibility(idAmOrPm, View.GONE);
                rv.setString(idDayNightIcon, "setTimeZone", timeZone.timeZoneId);
            }
            // modify by fan.yang for PR 952336 at 2015-3-25 end
        }

        @Override
        public RemoteViews getLoadingView() {
            return null;
        }

        @Override
        public int getViewTypeCount() {
            return 2;
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public boolean hasStableIds() {
            return true;
        }

    }
}
