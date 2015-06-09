package com.jrdcom.timetool.worldclock.widget;

import com.jrdcom.timetool.worldclock.provider.TimeZoneInfo;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.ContentObserver;
import android.os.Handler;
import android.os.HandlerThread;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.RemoteViews;
import com.android.deskclock.R;

/**
 * WorldClockProvider is an AppWidgetProvider used for receive app update
 * message
 * 
 */
public class WorldClockProvider extends AppWidgetProvider {
    public static final String WORLD_CLOCK_ACTION = "WORLD_CLOCK_ACTION";
    private ComponentName thisWidget;
    private RemoteViews remoteViews;

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager,
            int[] appWidgetIds) {
        thisWidget = new ComponentName(context, WorldClockProvider.class);
        remoteViews = new RemoteViews(context.getPackageName(),
                R.layout.widget_layout);
        Intent intent = new Intent(context, UpdateService.class);
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetIds[0]);
        remoteViews.setRemoteAdapter(R.id.widget_list, intent);
        Intent worldClockIntent = new Intent();
        worldClockIntent.setClassName("com.android.deskclock",
                "com.android.deskclock.WorldClock");
        worldClockIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        final PendingIntent onClickPendingIntent = PendingIntent
                .getActivity(context, 0, worldClockIntent,
                        PendingIntent.FLAG_UPDATE_CURRENT);
        remoteViews.setPendingIntentTemplate(R.id.widget_list,
                onClickPendingIntent);
        appWidgetManager.updateAppWidget(thisWidget, remoteViews);
        appWidgetManager.notifyAppWidgetViewDataChanged(appWidgetIds[0],
                R.id.widget_list);
        context.startService(intent);
        super.onUpdate(context, appWidgetManager, appWidgetIds);
    }

    @Override
    public void onDisabled(Context context) {
        super.onDisabled(context);
        Intent service = new Intent(context, UpdateService.class);
        context.stopService(service);
    }
}
