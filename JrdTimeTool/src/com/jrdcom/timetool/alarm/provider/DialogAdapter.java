package com.jrdcom.timetool.alarm.provider;


import java.util.ArrayList;
import java.util.Arrays;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.RadioButton;
import android.widget.TextView;

import com.android.deskclock.R;
import com.jrdcom.timetool.MyLog;

public class DialogAdapter extends BaseAdapter {

    private Context mContext;
    private ArrayList<String> data;
    private int checkedItemPosition;

    public DialogAdapter(Context mContext, ArrayList<String> data, int checkedItemPosition) {
        super();
        this.mContext = mContext;
        this.data = data;
        this.checkedItemPosition = checkedItemPosition;
        MyLog.i("==============\n" + data.toString());
    }
    public DialogAdapter(Context mContext, CharSequence[] data, int checkedItemPosition) {
        super();
        this.mContext = mContext;
        this.data = new ArrayList<String>();
        for (int i = 0; i < data.length; i++) {
            String s = data[i].toString();
            this.data.add(s);
        }
        
        MyLog.i("==============\n" + data.toString());
   
        this.checkedItemPosition = checkedItemPosition;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        ViewHolder holder;

        if (convertView == null) {
            convertView = LayoutInflater.from(mContext).inflate(R.layout.alarm_list_view_ringtone,
                    null);
            holder = new ViewHolder();
            holder.text_name = (TextView) convertView.findViewById(R.id.list_name);
            holder.radio_button = (RadioButton) convertView.findViewById(R.id.radio_button);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        holder.text_name.setText(data.get(position));
        holder.radio_button.setChecked(checkedItemPosition == position);
        return convertView;

    }

    @Override
    public long getItemId(int position) {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public Object getItem(int position) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public int getCount() {
        if (data != null && data.size() > 0) {
            return data.size();
        } else {
            return 0;
        }

    }

    public static class ViewHolder {
        TextView text_name;
        RadioButton radio_button;
    }
}
