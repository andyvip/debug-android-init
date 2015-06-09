package com.jrdcom.timetool.alarm.view;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.os.SystemProperties;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.android.deskclock.R;
import com.jrdcom.timetool.alarm.activity.SettingsActivity;
import com.jrdcom.timetool.alarm.provider.Alarm;
import com.jrdcom.timetool.alarm.provider.Alarms;

public class CustomAlarmDialog extends Dialog {

    //PR590800 by tiejun.zhang  changed for smart cover begin.
    private static View snooze;
    private static TextView label;
    //PR590800 by tiejun.zhang  changed for smart cover end.
    /// @ add by Yanjingming for pr464589 begin
    private static IAction msnoozeAction;
    private static IAction mdismissAction;
    private static final String DEFAULT_VOLUME_BEHAVIOR = "2";
    private static int mVolumeBehavior;
    /// @ add by Yanjingming for pr464589 end

    private static TextView mTitleView;// PR717899-mingwei.han-add
    private static Context mContext;// PR 767261 - mingwei.han added

    public CustomAlarmDialog(Context context, int theme) {
        super(context, theme);
    }

    public CustomAlarmDialog(Context context) {
        super(context);
    }

    // PR717899-mingwei.han-add begin
    @Override
    public void setTitle(CharSequence title) {
        super.setTitle(title);
        // PR 767261 - mingwei.han added - Begin
        if (Alarms.isArFaIwLanguage(mContext)) {
            mTitleView.setText("\u202D" + title + "\u202C");
        } else {
            mTitleView.setText(title);
        }
        // PR 767261 - mingwei.han added - End
    }
    // PR717899-mingwei.han-add end

    /**
     * Helper class for creating a custom dialog
     */
    public static class Builder {

        private Context context;

        public Builder(Context context) {
            this.context = context;
            mContext = context;// PR 767261 - mingwei.han added
        }

        /**
         * Create the custom dialog
         */
        public CustomAlarmDialog create(final IAction snoozeAction,
                final IAction dismissAction) {
         // @ add by Yanjingming for pr464589 begin
            final String vol =
                    PreferenceManager.getDefaultSharedPreferences(context)
                            .getString(SettingsActivity.KEY_VOLUME_BEHAVIOR,
                                    DEFAULT_VOLUME_BEHAVIOR);
            mVolumeBehavior = Integer.parseInt(vol);
            msnoozeAction = snoozeAction;
            mdismissAction = dismissAction;
         // @ add by Yanjingming for pr464589 end
            LayoutInflater inflater = (LayoutInflater) context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            // instantiate the dialog with the custom Theme
            /*PR 656709- Neo Skunkworks - Paul Xu modified - 001 Begin*/ 
            //PR590800 by tiejun.zhang  added for smart cover begin.
            CustomAlarmDialog dialog;
            View layout = null;
            
            if (Alarms.getFlipCoverOpenState()
            		&&  Alarms.getFlipCoverMode(context)) {

                layout = inflater.inflate(R.layout.alarm_alert_small_screen,null);
                dialog = new CustomAlarmDialog(context,
                        android.R.style.Theme_NoTitleBar);
                // PR 747642 - Neo Skunkworks - Soar Gao - 001 begin   
                //add background for cover_alarm
                DisplayMetrics metric = new DisplayMetrics();
            	dialog.getWindow().getWindowManager().getDefaultDisplay().getMetrics(metric);
                Bitmap bitmap = Bitmap.createBitmap(
                		metric.widthPixels,
                		metric.heightPixels,Bitmap.Config.ARGB_8888);
                       Canvas canvas = new Canvas(bitmap);
                       canvas.drawColor(Color.parseColor("#7a7f85"));
                // PR 747642 - Neo Skunkworks - Soar Gao - 001 end 
                       
                dialog.getWindow().setBackgroundDrawable(new BitmapDrawable(bitmap));
                dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                label = (TextView)layout.findViewById(R.id.label);
            } else {

                dialog = new CustomAlarmDialog(context);
                layout = inflater.inflate(R.layout.alarm_alert_fullscreen,null);
                // PR717899-mingwei.han-add
                dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                mTitleView = (TextView) layout.findViewById(R.id.alertTitle);
                // PR717899-mingwei.han-add
            }
            
            Window win = dialog.getWindow();
            win.addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED |
                    WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON |
//                    WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON | // remove by liang.zhang for PR 894168 at 2015-01-16
                    WindowManager.LayoutParams.FLAG_ALLOW_LOCK_WHILE_SCREEN_ON |
                    // PR724581- Neo Skunkworks - Tony - 001 begin
                    // set statusbar background to translucent 
                    WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
                    // PR724581- Neo Skunkworks - Tony - 001 end
            
          //PR590800 by tiejun.zhang  added for smart cover end.
            /*PR 656709- Neo Skunkworks - Paul Xu modified - 001 End*/
            dialog.addContentView(layout, new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.MATCH_PARENT));
            //PR590800 by tiejun.zhang  added for smart cover begin.
            snooze = layout.findViewById(R.id.snooze);
           //PR590800 by tiejun.zhang  added for smart cover end.
            snooze.requestFocus();
            snooze.setOnClickListener(new Button.OnClickListener() {
                public void onClick(View v) {
                    snoozeAction.execution();
                }
            });

            /* dismiss button: close notification */
            layout.findViewById(R.id.dismiss).setOnClickListener(
                    new Button.OnClickListener() {
                        public void onClick(View v) {
                            dismissAction.execution();
                        }
                    });

            dialog.setContentView(layout);
            return dialog;
        }

    }
    /*PR 656709- Neo Skunkworks - Paul Xu added - 001 Begin*/
    /**
     * set title for alarm.
     *
     * @param  String title
     * @return null.
     */ 
    public void setLabel(String title) {
        if (label != null) {
            // PR 767261 - mingwei.han added - Begin
            if (Alarms.isArFaIwLanguage(mContext)) {
                label.setText("\u202D" + title + "\u202C");
            } else {
                label.setText(title);
            }
            // PR 767261 - mingwei.han added - End
        }
    }
    /*PR 656709- Neo Skunkworks - Paul Xu added - 001 Begin*/ 
 // @ add by Yanjingming for pr463926 begin
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        // TODO Auto-generated method stub
        if(keyCode == KeyEvent.KEYCODE_BACK)
        {
            return true;
        }
        else{
            return super.onKeyDown(keyCode, event);
        }
    }
 // @ add by Yanjingming for pr463926 end

    public interface IAction {
        void execution();
    }

    public View getSnooze() {
        return snooze;
    }

    //  @ add by Yanjingming for pr464589 begin
    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        // Do this on key down to handle a few of the system keys.
        boolean up = event.getAction() == KeyEvent.ACTION_UP;
        switch (event.getKeyCode()) {
        // Volume keys and camera keys dismiss the alarm
            case KeyEvent.KEYCODE_VOLUME_UP:
            case KeyEvent.KEYCODE_VOLUME_DOWN:
            case KeyEvent.KEYCODE_VOLUME_MUTE:
            case KeyEvent.KEYCODE_CAMERA:
            case KeyEvent.KEYCODE_FOCUS:
                if (up) {
                    switch (mVolumeBehavior) {
                        case 1:
                            if(msnoozeAction != null)
                            {
                                msnoozeAction.execution();
                            }
                            break;
                        case 2:
                            if(mdismissAction != null)
                            {
                                mdismissAction.execution();
                            }
                            break;

                        default:
                            break;
                    }
                }
                return true;
            default:
                break;
        }
        return super.dispatchKeyEvent(event);
    }
    //  @ add by Yanjingming for pr464589 end
}
