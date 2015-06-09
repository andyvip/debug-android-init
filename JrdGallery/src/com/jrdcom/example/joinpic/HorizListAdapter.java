package com.jrdcom.example.joinpic;

import java.util.ArrayList;
import java.util.Map;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import com.jrdcom.android.gallery3d.R;

public class HorizListAdapter extends BaseAdapter {

	private Context mContext;
	// private String[] mData;
	private ArrayList<String> mData;

	public HorizListAdapter(Context context, ArrayList<String> data) {
		mContext = context;
		mData = data;
	}

	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return mData.size();
	}

	@Override
	public String getItem(int position) {
		// TODO Auto-generated method stub
		return mData.get(position);
	}

	@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		// TODO Auto-generated method stub
		ViewHolder holder = null;
		if (convertView == null) {
			convertView = LayoutInflater.from(mContext).inflate(R.layout.item,
					null);
			holder = new ViewHolder();
			holder.imageview = (ImageView) convertView.findViewById(R.id.image);
			BitmapFactory.Options options = new BitmapFactory.Options();
			options.inSampleSize = 10;
			Bitmap bm = BitmapFactory.decodeFile(mData.get(position), options);
			holder.imageview.setImageBitmap(bm);
			convertView.setTag(holder);
		}
		else
		{
			holder = (ViewHolder)convertView.getTag();
		}
		
		return convertView;
	}

	private class ViewHolder {
		ImageView imageview;
	}
}
