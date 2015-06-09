package com.jrdcom.timetool.countdown.view;

import android.app.Dialog;
import android.content.Context;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.android.deskclock.R;
import com.jrdcom.timetool.countdown.activity.CountDownActivity;
import com.jrdcom.timetool.countdown.service.BackgroundCountDownService;

public    class CustomDialog extends Dialog {
    public CustomDialog(Context context, int theme) {
        super(context, theme);
   }
 
    public CustomDialog(Context context) {
        super(context);
    }
    /**
     * Helper class for creating a custom dialog
     */
    public static class Builder {
  
        private Context context;
  
  
        public Builder(Context context) {
            this.context = context;
        }
  
  
        /**
         * Create the custom dialog
         */
        public CustomDialog create(final IAction action) {
            LayoutInflater inflater = (LayoutInflater) context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            // instantiate the dialog with the custom Theme
            final CustomDialog dialog = new CustomDialog(context);
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            View layout = inflater.inflate(R.layout.countdown_alarmalertfullscreen , null);
            dialog.addContentView(layout, new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));
            
            TextView tvTime = (TextView) layout.findViewById(R.id.timer_alert_icon);
            int totalTime = BackgroundCountDownService.getTotalTime();
            tvTime.setText(CountDownActivity.translateTimeToString(totalTime));
            
            Button okButton = (Button) layout.findViewById(R.id.timer_alert_ok);
            okButton.requestFocus();
            okButton.setOnClickListener(new View.OnClickListener() {

                public void onClick(View v) {
                    action.execution();
                }
            });
            dialog.setContentView(layout);
            return dialog;
        }
  
    }

    // @ add by Yanjingming for pr468300 begin
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        // TODO Auto-generated method stub
        if(keyCode == KeyEvent.KEYCODE_BACK){
            return true;
        }else{
            return super.onKeyDown(keyCode, event);
        }
    }
    // @ add by Yanjingming for pr468300 end

    public interface IAction{
        
        void execution();
        
        
    } 
}