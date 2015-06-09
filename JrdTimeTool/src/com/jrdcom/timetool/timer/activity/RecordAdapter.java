package com.jrdcom.timetool.timer.activity;

import com.android.deskclock.R;
import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import java.util.ArrayList;
import java.util.List;

public class RecordAdapter extends BaseAdapter {
    
    private AssetManager am;
    
    private Typeface tf;
    
	public void setAm(AssetManager am) {
        this.am = am;
        tf = Typeface.createFromAsset(am, "fonts/Roboto-Thin.ttf");
    }
    private List<String> mRecords = new ArrayList<String>();

	private Context mContext;

	public RecordAdapter(Context context) {
		mContext = context;
	}

	public int getCount() {
		return mRecords.size();
	}

	public Object getItem(int position) {
		return position;
	}

	public long getItemId(int position) {
		return position;
	}

	public View getView(int position, View convertView, ViewGroup parent) {
		View view = null;
		if (convertView == null) {
			view = ((LayoutInflater) mContext
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE))
					.inflate(R.layout.timer_listitem, parent, false);
		} else {
			view = convertView;
		}

		TextView mTag = (TextView) view.findViewById(R.id.Timer_item_tag);
		TextView absoluteTimeView = (TextView) view
				.findViewById(R.id.Timer_item_absolute_time);
		TextView relativeTimeView = (TextView) view
				.findViewById(R.id.Timer_item_relative_time);
		
		mTag.setTypeface(tf);
		absoluteTimeView.setTypeface(tf);
		relativeTimeView.setTypeface(tf);

		String[] str = mRecords.get(mRecords.size() - 1 - position).split("/");
		mTag.setText("" + (mRecords.size() - position));
		absoluteTimeView.setText(str[0]);
		relativeTimeView.setText(str[1]);
		return view;
	}

	public void clearRecords() {
		mRecords.clear();
		notifyDataSetChanged();
	}

	public void addRecords(String log) {
		mRecords.add(log);
		notifyDataSetChanged();
	}
	public List getRecordlist() {
		return mRecords;
	}
	public void addRecordlist(List list) {
		mRecords = list;
		notifyDataSetChanged();
	}

}
