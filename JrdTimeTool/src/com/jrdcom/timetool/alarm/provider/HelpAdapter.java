package com.jrdcom.timetool.alarm.provider;
import java.util.List;
import java.util.Map;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.RadioButton;
import android.widget.TextView;

import com.android.deskclock.R;
import com.jrdcom.timetool.alarm.activity.SetAlarm;

public final class HelpAdapter extends BaseAdapter {

    private Context mContext; // add by Yanjingming for pr484826

    private LayoutInflater mInflater;

    private List<Map<String, Object>> listItems;

    private static class ViewHolder {
        TextView text_name;
        RadioButton radio_button;
    }

    public HelpAdapter(Context context, List<Map<String, Object>> listItems) {
        mInflater = LayoutInflater.from(context);
        this.listItems = listItems;
        mContext = context; // add by Yanjingming for pr484826
    }

    public int getCount() {
        // TODO Auto-generated method stub
        return listItems.size();
    }

    public Object getItem(int position) {
        // TODO Auto-generated method stub
        return position;
    }

    public long getItemId(int position) {
        // TODO Auto-generated method stub
        return position;
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        // TODO Auto-generated method stub
        ViewHolder holder;

        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.alarm_list_view_ringtone,
                    null);
            holder = new ViewHolder();
            holder.text_name = (TextView) convertView.findViewById(R.id.list_name);
            holder.radio_button = (RadioButton) convertView.findViewById(R.id.radio_button);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        // modify by wei.li for PR500653 begin
        if (listItems != null && listItems.size() > position
                && listItems.get(position) != null && holder != null) {
            // modify by Yanjingming for pr484826 begin
            if (listItems.get(position).get("activity") != null) {
                String ringtone = listItems.get(position).get("activity").toString();
                if ("Silent".equals(ringtone)) {
                    holder.text_name.setText(mContext.getString(R.string.timer_ringtone_silent));
                } else {
                 // add by Yanjingming for pr543246 begin
                	// PR 605347 - Neo Skunkworks - Soar Gao - 002 begin 
//                	String ringtoneExtension=ringtone.substring(ringtone.lastIndexOf('.'),ringtone.length() ).toLowerCase();
//                    if(ringtoneExtension.equals(".mp3")||ringtoneExtension.equals(".ogg")||ringtoneExtension.equals(".wav")||ringtoneExtension.equals(".wma")||ringtoneExtension.equals(".ape")||ringtoneExtension.equals(".acc")){
                    // PR 605347 - Neo Skunkworks - Soar Gao - 002 end 
                	// PR -613490 - Neo Skunworks - Soar Gao ,  -001 begin
                	//Delete All extention
                	if((ringtone!=null)&&(!("".equals(ringtone)))){
                       // modify by xing.zhao for PR807602 begin
                       int mPostion=ringtone.lastIndexOf('.');
                       ringtone = ringtone.substring(0, mPostion==-1?ringtone.length():mPostion);
                       // modify by xing.zhao for PR807602 end
                	}
                	// PR -613490 - Neo Skunworks - Soar Gao ,  -001 end
                 // add by Yanjingming for pr543246 end
                    holder.text_name.setText(ringtone);
                }
            }
            // modify by Yanjingming for pr484826 end
            if(listItems.get(position).get("checked") != null){
                holder.radio_button.setChecked((Boolean) listItems.get(position).get("checked"));
            }
        }
        // modify by wei.li for PR500653 end

        return convertView;
    }

}
