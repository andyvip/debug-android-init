
package com.jrdcom.timetool.alarm.activity;

import android.app.Activity;
import android.app.TabActivity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TabHost;
import android.widget.TabWidget;
import android.widget.TextView;

import com.android.deskclock.R;
import com.jrdcom.timetool.MyLog;
import com.jrdcom.timetool.TimeToolActivity;
import com.jrdcom.timetool.countdown.service.MediaPlayerService;

public class GotoRingtoneActivity extends TabActivity implements OnClickListener{
    // PR 587415 - Neo Skunkworks - Soar Gao - 001 begin
    public static int musicID;
    public static String musicPath;
    public static int musicActivity;
    public static String musicName;

    private LinearLayout mTab1;
    private LinearLayout mTab2;

    private TextView tab_line1;
    private TextView tab_line2;

    public static final String RINGTONE_TAG = "ringtone_tag";

    public static final String MUSIC_TAG = "music_tag";

    private HomeKeyEventBroadCastReceiver receiver;
    private TabHost tabHost;

    // PR 587415 - Neo Skunkworks - Soar Gao - 001 end
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.alarm_set_goto_ringtone);

        // PR 587415 - Neo Skunkworks - Soar Gao - 001 begin
        musicID = -1;
        musicPath = "";
        musicName = "";
        receiver = new HomeKeyEventBroadCastReceiver();
        registerReceiver(receiver, new IntentFilter(Intent.ACTION_CLOSE_SYSTEM_DIALOGS));
        // PR 587415 - Neo Skunkworks - Soar Gao - 001 end

        tabHost = getTabHost();
        TabHost.TabSpec spec; // Resusable TabSpec for each tab
        Intent intent1, intent2; // Reusable Intent for each tab

        intent1 = new Intent().setClass(this, SystemActivity.class);

        mTab1 = (LinearLayout) getLayoutInflater().inflate(R.layout.ringtone_tab_widget, null);
        mTab2 = (LinearLayout) getLayoutInflater().inflate(R.layout.ringtone_tab_widget, null);
        TextView title1 = (TextView) mTab1.findViewById(R.id.tab_title);
        tab_line1 = (TextView) mTab1.findViewById(R.id.tab_line);
        title1.setText(R.string.system_acticity_name);

        spec = tabHost.newTabSpec(RINGTONE_TAG).setIndicator(mTab1)

        .setContent(intent1);
        tabHost.addTab(spec);

        intent2 = new Intent().setClass(this, MusicActivity.class);
        TextView title2 = (TextView) mTab2.findViewById(R.id.tab_title);
        tab_line2 = (TextView) mTab2.findViewById(R.id.tab_line);
        title2.setText(R.string.music_acticity_name);
        spec = tabHost.newTabSpec(MUSIC_TAG).setIndicator(mTab2).setContent(intent2);
        tabHost.addTab(spec);
        mTab1.setOnClickListener(this);
        mTab2.setOnClickListener(this);
        if (tabHost.getCurrentTab() == 0) {
            tab_line1.setVisibility(View.VISIBLE);
            tab_line2.setVisibility(View.GONE);
        }else {
            tab_line1.setVisibility(View.GONE);
            tab_line2.setVisibility(View.VISIBLE);
        }
       
      
    }
    




    // PR 587415 - Neo Skunkworks - Soar Gao - 001 begin
    @Override
    protected void onDestroy() {
        // TODO Auto-generated method stub
        super.onDestroy();
        unregisterReceiver(receiver);
    }

    @Override
    protected void onRestoreInstanceState(Bundle state) {
        // TODO Auto-generated method stub
        super.onRestoreInstanceState(state);
        musicActivity = state.getInt("musicActivity");
        musicID = state.getInt("musicID");
        musicPath = state.getString("musicPath");
        musicName = state.getString("musicName");
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        // TODO Auto-generated method stub
        super.onSaveInstanceState(outState);
        outState.putInt("musicID", musicID);
        outState.putInt("musicActivity", musicActivity);
        outState.putString("musicPath", musicPath);
        outState.putString("musicName", musicName);
    }

    /**
     * touch others out of dialog to stop ring
     */
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        // TODO Auto-generated method stub
        stopService(new Intent(this, MediaPlayerService.class));
        return super.onTouchEvent(event);
    }
    // PR 587415 - Neo Skunkworks - Soar Gao - 001 end

    @Override
    public void onClick(View v) {
        if (v == mTab1) {
            // PR827785 by xing.zhao [Alarm]Can not select alarm ringtone form My music.   begin
            tabHost.setCurrentTab(0);
            // PR827785 by xing.zhao [Alarm]Can not select alarm ringtone form My music.   end
            tab_line1.setVisibility(View.VISIBLE);
            tab_line2.setVisibility(View.GONE);
        }else {
            // PR827785 by xing.zhao [Alarm]Can not select alarm ringtone form My music.   begin
            tabHost.setCurrentTab(1);
            // PR827785 by xing.zhao [Alarm]Can not select alarm ringtone form My music.   end
            tab_line1.setVisibility(View.GONE);
            tab_line2.setVisibility(View.VISIBLE);
        }
    }
}

// PR 587415 - Neo Skunkworks - Soar Gao - 001 begin
class HomeKeyEventBroadCastReceiver extends BroadcastReceiver {

    static final String SYSTEM_REASON = "reason";
    static final String SYSTEM_HOME_KEY = "homekey";// home key
    static final String SYSTEM_POWER_KEY = "lock";// power key
    static final String SYSTEM_RECENT_APPS = "recentapps";// long home key
    static final String SYSTEM_LONGPOWER_KEY = "globalactions";// long power key

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if (action.equals(Intent.ACTION_CLOSE_SYSTEM_DIALOGS)) {
            String reason = intent.getStringExtra(SYSTEM_REASON);
            if (reason != null) {
                if (reason.equals(SYSTEM_HOME_KEY)) {
                    context.stopService(new Intent(context, MediaPlayerService.class));
                    // PR 594450 - Neo Skunkworks - Soar Gao - 001 begin
                    ((Activity) context).finish();
                    // PR 594450 - Neo Skunkworks - Soar Gao - 001 end
                } else if (reason.equals(SYSTEM_RECENT_APPS)) {
                    context.stopService(new Intent(context, MediaPlayerService.class));
                } else if (reason.equals(SYSTEM_POWER_KEY)) {
                    context.stopService(new Intent(context, MediaPlayerService.class));
                } else if (reason.equals(SYSTEM_LONGPOWER_KEY)) {
                    context.stopService(new Intent(context, MediaPlayerService.class));
                }
            }
        }
    }
}
// PR 587415 - Neo Skunkworks - Soar Gao - 001 end
