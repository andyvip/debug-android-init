package com.jrdcom.example.joinpic;
/**
 * @author yaogang.hao@tct-nj.com
 * This class is a adapter which is showing the selected images styles
 * 
 */
import java.util.List;
import com.jrdcom.android.gallery3d.R;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

public class SelectorAdapter extends BaseAdapter {

    private Context mContext;
    private List<Image> mlist;
    
    public SelectorAdapter(Context context, List<Image> list)
    {
        this.mContext = context;
        this.mlist = list;
    }
    
    @Override
    public int getCount() {
        return this.mlist.size();
    }
    @Override
    public Object getItem(int position) {
        return this.mlist.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ImageHolder imageHolder = null;
        if(null == convertView)
        {
            imageHolder = new ImageHolder();
            LayoutInflater inflater = LayoutInflater.from(mContext);
            convertView = inflater.inflate(R.layout.album_select_item, null);
            imageHolder.image =(ImageView)convertView.findViewById(R.id.album_select_image);
            convertView.setTag(imageHolder);
        }
        else
        {
            imageHolder = (ImageHolder) convertView.getTag();
        }
        imageHolder.image.setImageBitmap(this.mlist.get(position).getBitmap());
        
        return convertView;
    }

    
    private class ImageHolder
    {
        ImageView image;
    }

}
