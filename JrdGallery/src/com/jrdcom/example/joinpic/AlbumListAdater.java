package com.jrdcom.example.joinpic;
/**
 * @author yaogang.hao@tct-nj.com
 * This class is Image folders album adapter
 * 
 */
import java.util.List;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import com.jrdcom.android.gallery3d.R;
public class AlbumListAdater extends BaseAdapter {
    
    private Context mContext;
    private List<ImageAlbum> mlist;
    
    public AlbumListAdater(Context context, List<ImageAlbum>  list) {
        
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
        ImageAlbumHolder imageAlbumHolder = null;
        View view = convertView;
        if(null == view)
        {
            LayoutInflater inflater = LayoutInflater.from(mContext);
            view = inflater.inflate(R.layout.album_list_item, null);
            imageAlbumHolder = new ImageAlbumHolder();
            imageAlbumHolder.thumbNail = (ImageView) view.findViewById(R.id.album_dir_thumb);
            imageAlbumHolder.dirname = (TextView) view.findViewById(R.id.album_dir_name);
            imageAlbumHolder.album_dir_path = (TextView) view.findViewById(R.id.album_dir_path);
            imageAlbumHolder.album_dir_item_num = (TextView) view.findViewById(R.id.album_dir_item_num);
            view.setTag(imageAlbumHolder);
        }
        else
        {
            imageAlbumHolder = (ImageAlbumHolder) view.getTag();
        }
        ImageAlbum album = mlist.get(position);
        
        imageAlbumHolder.thumbNail.setImageBitmap(album.getThumbNail());
        imageAlbumHolder.dirname.setText(album.getAblumName());
        imageAlbumHolder.album_dir_item_num.setText("("+album.getCount()+")");
        imageAlbumHolder.album_dir_path.setText(album.getFimgpath());
        
        return view;
    }

    private class ImageAlbumHolder
    {
        ImageView thumbNail;
        TextView dirname;
        TextView album_dir_path;
        TextView album_dir_item_num;
    }
}
