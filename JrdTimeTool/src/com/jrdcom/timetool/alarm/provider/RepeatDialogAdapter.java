
package com.jrdcom.timetool.alarm.provider;

import java.util.ArrayList;
import java.util.Arrays;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.RadioButton;
import android.widget.TextView;

import com.android.deskclock.R;
import com.jrdcom.timetool.MyLog;

public class RepeatDialogAdapter extends BaseAdapter {

    private Context mContext;
    private CharSequence[] data;
    private boolean[] checks;


    public void changeItemCheckState(int position) {

        checks[position] = !checks[position];
        notifyDataSetChanged();
    }

    public boolean getItemCheckState(int position) {
        return checks[position];
    }


    public RepeatDialogAdapter(Context mContext, CharSequence[] data, boolean[] checks) {
        super();
        this.mContext = mContext;
        this.data = data;
        this.checks = checks;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        ViewHolder holder;

        if (convertView == null) {
            convertView = LayoutInflater.from(mContext).inflate(R.layout.checkbox_listitem, null);
            holder = new ViewHolder();
            holder.text_name = (TextView) convertView.findViewById(R.id.list_name);
            holder.checkBox = (CheckBox) convertView.findViewById(R.id.checkbox);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        holder.text_name.setText(data[position]);
        holder.checkBox.setChecked(checks[position]);

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
        if (data != null && data.length > 0) {
            return data.length;
        } else {
            return 0;
        }

    }

    public static class ViewHolder {
        TextView text_name;
        CheckBox checkBox;
    }
}
