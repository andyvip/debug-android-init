package com.jrdcom.example.joinpic;
/**
 * @author yaogang.hao@tct-nj.com
 * This class is folders's children .
 * contains many images from specific floders.
 * 
 */
import java.util.List;


import com.jrdcom.android.gallery3d.R;
import android.content.ContentResolver;
import android.content.Context;
import android.graphics.Bitmap;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

public class ImageGridAdapter extends BaseAdapter {

    private Context mContext;
    private List<Image> mList;
    private LayoutInflater inflater;
    private ContentResolver contentResolver;
    public ImageGridAdapter(Context context,List<Image> list)
    {
        this.mContext = context;
        this.mList = list;
        this.inflater = LayoutInflater.from(mContext);
        contentResolver = mContext.getContentResolver();
    }
    
    @Override
    public int getCount() {
        return this.mList.size();
    }

    @Override
    public Object getItem(int position) {
        return this.mList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = convertView;
        ImageHolder imageHolder = null;
        Image image = this.mList.get(position);
        if(null == view)
        {
            view = this.inflater.inflate(R.layout.album_grid_item, null);
            imageHolder = new ImageHolder();
            imageHolder.image = (ImageView) view.findViewById(R.id.album_thumb);
            imageHolder.checkedImage = (ImageView) view.findViewById(R.id.album_thumb_checked);
            view.setTag(imageHolder);
        }
        else
        {
            imageHolder = (ImageHolder) view.getTag();
        }
        if(image.getBitmap() == null)
        {
            imageHolder.image.setImageResource(R.drawable.empty_photo);
//            if(AsyncLoadImageTask.getThreadPoolSize() < 200)
            //if not scrolling ,to decode image.
            if(!HAblum_Main.isScrolling)
            {
                AsyncLoadImageTask asyncLoadImageTask = new AsyncLoadImageTask(mContext, image, imageHolder.image);
                asyncLoadImageTask.execute();
            }
        }else
        {
            imageHolder.image.setImageBitmap(image.getBitmap());
        }
        
        int state = image.isSelected() ? View.VISIBLE:View.GONE;
        imageHolder.checkedImage.setVisibility(state);
        
        return view;
    }
    private class ImageHolder
    {
        ImageView image;
        ImageView checkedImage;
    }


}
