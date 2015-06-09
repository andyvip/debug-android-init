package com.jrdcom.timetool.worldclock.widget;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class WorldClockKeyguardProvider extends AppWidgetProvider {
    public static final String TAG = WorldClockKeyguardProvider.class
            .getSimpleName();

    public void onUpdate(Context context, AppWidgetManager appWidgetManager,
            int[] appWidgetIds) {
        super.onUpdate(context, appWidgetManager, appWidgetIds);
        context.sendBroadcast(new Intent(
                "android.appwidget.action.APPWIDGET_UPDATE_VIEW"));

    }

    public void onDeleted(Context context, int[] appWidgetIds) {
        super.onDeleted(context, appWidgetIds);
    }

    @Override
    public void onEnabled(Context context) {
        Log.e(TAG, "WorldClockKeyguardProvider : onEnabled");
        Intent intent = new Intent();
        intent.setClass(context, UpdateKeyguardService.class);
        context.startService(intent);
        super.onEnabled(context);
    }

    @Override
    public void onDisabled(Context context) {
        super.onDisabled(context);
        Intent intent = new Intent();
        intent.setClass(context, UpdateKeyguardService.class);
        context.stopService(intent);
    }
}