package com.jrdcom.timetool.worldclock.widget;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import com.jrdcom.timetool.worldclock.provider.TimeZoneInfo;
import com.jrdcom.timetool.worldclock.provider.TimeZones;
import com.jrdcom.timetool.worldclock.widget.UpdateService.ListRemoteViewsFactory;

import android.app.PendingIntent;
import android.app.Service;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProviderInfo;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.ContentObserver;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.View;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;
import android.widget.RemoteViewsService.RemoteViewsFactory;

import com.android.deskclock.R;

/**
 * UpdateService is called when the first widget is created. This service is
 * used to update the widget.
 */
public class UpdateKeyguardService extends RemoteViewsService {

    private final static String TAG = UpdateKeyguardService.class
            .getSimpleName();

    private RemoteViews mViews = null;
    private boolean isGetData = true;
    private ComponentName mWidgetComponent = null;

    private Context mContext;

    private List<TimeZoneInfo> mList;
    private final static String M24 = "kk:mm";
    private final static String M12 = "h:mm";
    /*PR 563998- Neo Skunkworks - Paul Xu added - 001 Begin*/
    private AppWidgetManager mAppWidgetManager;
    /*PR 563998- Neo Skunkworks - Paul Xu added - 001 End*/

    private ContentObserver mObserver = new ContentObserver(new Handler()) {
        public void onChange(boolean selfChange) {
            Log.v(TAG, "UpdateService ->ContentObserver : onChange()");
            isGetData = true;
            notifyAppWidgetViewDataChanged(getApplicationContext());
        };
    };
    /**
     * a BroadcastReceiver is used to receive the update news, and update the
     * widget.
     */
    private final BroadcastReceiver mIntentReceiver = new BroadcastReceiver() {

        public void onReceive(Context context, Intent intent) {
            Log.e(TAG, "UpdateService : mIntentReceiver : onReceive");
            String action = intent.getAction();
            if (Intent.ACTION_TIMEZONE_CHANGED.equals(action)
                    || Intent.ACTION_LOCALE_CHANGED.equals(action)) {
                isGetData = true;
            }
            updateClock();
        }

    };

    public void onCreate() {
        super.onCreate();
        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_TIME_TICK);
        filter.addAction(Intent.ACTION_TIME_CHANGED);
        filter.addAction(Intent.ACTION_TIMEZONE_CHANGED);
        filter.addAction("android.appwidget.action.APPWIDGET_UPDATE_VIEW");
        filter.addAction("com.android.broadcasttest.CHANGE_THEME");
        filter.addAction(Intent.ACTION_LOCALE_CHANGED);
        filter.addCategory("android.intent.category.DEFAULT");
        filter.addAction("com.jrdcom.worldclock.view.update");
        //PR:499760 add by xibin
        filter.addAction("android.intent.action.dateformatchange");
        getApplicationContext().registerReceiver(mIntentReceiver, filter, null,
                null);
        mWidgetComponent = new ComponentName(this,
                WorldClockKeyguardProvider.class);
        mContext = this;
        /*PR 563998- Neo Skunkworks - Paul Xu added - 001 Begin*/
        mAppWidgetManager = AppWidgetManager.getInstance(mContext);
        /*PR 563998- Neo Skunkworks - Paul Xu added - 001 End*/
        mList = TimeZones.getShowedTimeZone(mContext);
        getContentResolver().registerContentObserver(
                TimeZoneInfo.Columns.CONTENT_URI, true, mObserver);

    }

    public void onDestroy() {
        super.onDestroy();
        getApplicationContext().unregisterReceiver(mIntentReceiver);
        getContentResolver().unregisterContentObserver(mObserver);
        /*PR 563998- Neo Skunkworks - Paul Xu added - 001 Begin*/
		Intent intent = new Intent(this, UpdateKeyguardService.class);
		startService(intent);
		/*PR 563998- Neo Skunkworks - Paul Xu added - 001 End*/
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // restart the service when the service was killed
    	/*PR 563998- Neo Skunkworks - Paul Xu added - 001 Begin*/
        super.onStartCommand(intent, flags, startId);
        
        return START_STICKY;
        /*PR 563998- Neo Skunkworks - Paul Xu added - 001 End*/
    }

    private void updateClock() {
    	/*PR 563998- Neo Skunkworks - Paul Xu added - 001 Begin*/
        //AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(this);
        int[] appWidgetIds = mAppWidgetManager.getAppWidgetIds(mWidgetComponent);
        /*PR 563998- Neo Skunkworks - Paul Xu added - 001 End*/
        for (int appWidgetId : appWidgetIds) {
            if (mViews == null) {
                mViews = new RemoteViews(getPackageName(),
                        R.layout.widget_layout_keyguard);
                Intent intent = new Intent(this, UpdateKeyguardService.class);
                mViews.setRemoteAdapter(R.id.widget_list, intent);
                Intent worldClockIntent = new Intent();
                worldClockIntent.setClassName("com.android.deskclock",
                        "com.android.deskclock.WorldClock");
                worldClockIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                final PendingIntent onClickPendingIntent = PendingIntent
                        .getActivity(this, 0, worldClockIntent,
                                PendingIntent.FLAG_UPDATE_CURRENT);
                mViews.setPendingIntentTemplate(R.id.widget_list,
                        onClickPendingIntent);
                // startService(intent);
            } else {
                notifyAppWidgetViewDataChanged(getApplicationContext());
            }
            /*PR 563998- Neo Skunkworks - Paul Xu added - 001 Begin*/
            mAppWidgetManager.updateAppWidget(appWidgetId, mViews);
            /*PR 563998- Neo Skunkworks - Paul Xu added - 001 End*/
        }
    }

    private int getDataSize() {
        return mList == null ? 0 : mList.size();
    }

    private String getTime(TimeZoneInfo info) {

        boolean is24Hour = android.text.format.DateFormat
                .is24HourFormat(mContext);
        String timeFormat = is24Hour ? M24 : M12;
        return DateFormat.format(timeFormat, getCurrCalendar(info)).toString();
    }

    private String getDate(TimeZoneInfo info) {
        // PR:499760 add by xibin start
        Calendar calendar = getCurrCalendar(info);
        // String format = mContext
        // .getString(com.android.internal.R.string.abbrev_wday_month_day_no_year);
        // String mDate = DateFormat.format(format, calendar).toString();
        java.text.DateFormat shortDateFormat = DateFormat
                .getDateFormat(mContext);
        String mDate = shortDateFormat.format(calendar.getTime());
        // PR:499760 add by xibin end
        return mDate;
    }

    private Calendar getCurrCalendar(TimeZoneInfo info) {
        Calendar calendar = Calendar.getInstance();
        /*PR 567420- Neo Skunkworks - Paul Xu added - 001 Begin*/
        TimeZones.setTimeZoneOffset(info);
        /*PR 567420- Neo Skunkworks - Paul Xu added - 001 End*/
        long nowMilliseconds = calendar.getTimeInMillis()
                - calendar.getTimeZone().getOffset(calendar.getTimeInMillis())
                + info.offset;
        calendar.setTimeInMillis(nowMilliseconds);
        return calendar;

    }

    private void setAmOrPm(RemoteViews remoteViews, TimeZoneInfo info,
            int idAmOrPm) {

        boolean is24Hour = android.text.format.DateFormat
                .is24HourFormat(mContext);
        Log.e(TAG, "is24Hour = " + is24Hour);
        if (!is24Hour) {
            Log.e(TAG, "idAmOrPm = " + idAmOrPm);
            remoteViews.setViewVisibility(idAmOrPm, View.VISIBLE);
            boolean isMorning = getCurrCalendar(info).get(Calendar.AM_PM) == 0;
            if (!isMorning) {
                remoteViews
                        .setTextViewText(idAmOrPm, getText(R.string.time_pm));
            } else {
                remoteViews
                        .setTextViewText(idAmOrPm, getText(R.string.time_am));
            }
        }
    }

    private void fillTimeData(RemoteViews remoteViews, TimeZoneInfo info,
            int idAmPm, int cityId, int timeId, int dateId) {
        setAmOrPm(remoteViews, info, idAmPm);
        remoteViews.setTextViewText(cityId, info.displayName);
        remoteViews.setTextViewText(timeId, getTime(info));
        remoteViews.setTextViewText(dateId, getDate(info));
    }

    private void notifyAppWidgetViewDataChanged(Context context) {
    	/*PR 563998- Neo Skunkworks - Paul Xu added - 001 Begin*/
        //AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(this);
        mAppWidgetManager.notifyAppWidgetViewDataChanged(
        		mAppWidgetManager.getAppWidgetIds(mWidgetComponent),
                R.id.widget_list);
        /*PR 563998- Neo Skunkworks - Paul Xu added - 001 End*/
    }

    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
        Log.e(TAG, "RemoteViewsFactory : onGetViewFactory");
        return new ListRemoteViewsFactory(this.getApplicationContext(), intent);
    }

    private class ListRemoteViewsFactory implements
            RemoteViewsService.RemoteViewsFactory {
        private final Context mContext;
        private int size;

        public ListRemoteViewsFactory(Context context, Intent intent) {
            mContext = context;
            if (Looper.myLooper() == null) {
                Looper.prepare();
            }
        }

        @Override
        public void onCreate() {
        }

        @Override
        public void onDataSetChanged() {
            Log.e(TAG, "ListRemoteViewsFactory : onDataSetChanged");
            /*PR 563998- Neo Skunkworks - Paul Xu modified - 001 Begin*/
            /*
            if (isGetData) {
            */
			isGetData = false;
			mList = TimeZones.getShowedTimeZone(mContext);
            /*PR 563998- Neo Skunkworks - Paul Xu modified - 001 End*/
            size = getDataSize();

        }

        @Override
        public void onDestroy() {
            Log.e(TAG, "ListRemoteViewsFactory : onDestroy");
            if (mList != null)
                mList.clear();
        }

        @Override
        public int getCount() {
            if (mList == null) {
                return 0;
            }
            return size;
        }

        @Override
        public RemoteViews getViewAt(int position) {
            if (position < 0 || position >= getCount() || mList == null
                    || getDataSize() == 0)
                return null;
            final RemoteViews rv = new RemoteViews(mContext.getPackageName(),
                    R.layout.worldclock_widget_keyguard_list_item);
            Intent newIntent = new Intent();
            rv.setOnClickFillInIntent(R.id.linearLayout, newIntent);
            TimeZoneInfo timeZoneInfo = mList.get(position);

            fillTimeData(rv, timeZoneInfo, R.id.am_pm, R.id.city, R.id.time,
                    R.id.date);

            return rv;
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
