
package com.jrdcom.android.gallery3d.util;

import java.lang.ref.SoftReference;
import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Iterator;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executor;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import android.content.Context;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.provider.MediaStore;
import android.provider.MediaStore.Images;
import android.widget.ImageView;

import com.jrdcom.android.gallery3d.R;
import com.jrdcom.android.gallery3d.data.MediaItem;
import com.jrdcom.android.gallery3d.data.MediaObject;

/**
 * @author yaping.liu
 **/
public class AsyncBitmapLoader
{
	private HashMap<Uri, SoftReference<Bitmap>> photoCache = new HashMap<Uri, SoftReference<Bitmap>>();

    private Context mContext;

    private BitmapFactory.Options options;
    
    private HashMap<Uri, BitmapWorkerTask> taskList = new HashMap<Uri, BitmapWorkerTask>();

    private static final int CORE_POOL_SIZE = 4;
    private static final int MAXIMUM_POOL_SIZE = 128;
    private static final int KEEP_ALIVE = 1;

    private static final ThreadFactory sThreadFactory = new ThreadFactory() {
        private final AtomicInteger mCount = new AtomicInteger(1);

        public Thread newThread(Runnable r) {
            return new Thread(r, "AsyncTask #" + mCount.getAndIncrement());
        }
    };

    private static final BlockingQueue<Runnable> sPoolWorkQueue =
            new LinkedBlockingQueue<Runnable>();
    
    private static final Executor THREAD_POOL_EXECUTOR
    = new ThreadPoolExecutor(CORE_POOL_SIZE, MAXIMUM_POOL_SIZE, KEEP_ALIVE,
            TimeUnit.SECONDS, sPoolWorkQueue, sThreadFactory);
    private HashMap<String,Integer> mImageId;
    public void setIdHashMap(HashMap<String,Integer> imageId) {
    	mImageId = imageId;
    }
    public AsyncBitmapLoader(Context mContext)
    {
        this.mContext = mContext;
        options = new BitmapFactory.Options();
        options.inScreenDensity = 3;
        options.inDither = false;
        options.inPreferredConfig = Bitmap.Config.ARGB_8888;
    }

    // Used to display bitmap in the UI thread
    class BitmapDisplayer implements Runnable
    {
        Bitmap bitmap;
        ImageView imageView;

        public BitmapDisplayer(Bitmap bitmap, ImageView imageView) {
            this.bitmap = bitmap;
            this.imageView = imageView;
        }

        public void run()
        {
            imageView.setImageBitmap(bitmap);
        }
    }

    public void loadBitmap(ImageView imageView,MediaItem item) {
    	/*if (item.getThumbnail() !=null) {
    		if (imageView !=null) {
                imageView.setImageBitmap(item.getThumbnail());                
                return;
        	}
    	}*/
    	Uri uri = item.getContentUri();
    	if (photoCache.containsKey(uri)) {
    		SoftReference<Bitmap> softReference = photoCache.get(uri);
			if (softReference != null) {
				Bitmap bitmap = softReference.get();
				if (bitmap != null) {
					if (imageView != null) {
						imageView.setImageBitmap(bitmap);
						return;
					}
				}
			}
        }
    	
    	if (cancelPotentialWork(uri, imageView)) {
    		final BitmapWorkerTask task = new BitmapWorkerTask(imageView, item);
//            taskList.put(uri, task);
            final AsyncDrawable asyncDrawable =  
                    new AsyncDrawable(mContext.getResources(), task);
            if (imageView !=null) {
                imageView.setImageDrawable(asyncDrawable);
            }  
            //task.execute(uri);
            task.executeOnExecutor(THREAD_POOL_EXECUTOR, uri);
    	}
    	
    	/*if (!taskList.containsKey(uri)) {
            final BitmapWorkerTask task = new BitmapWorkerTask(imageView, item);
            taskList.put(uri, task);
            final AsyncDrawable asyncDrawable =  
                    new AsyncDrawable(mContext.getResources(), task);
            if (imageView !=null) {
                imageView.setImageDrawable(asyncDrawable);
            }  
            //task.execute(uri);
            task.executeOnExecutor(THREAD_POOL_EXECUTOR, uri);
        }*/
    }

    static class AsyncDrawable extends BitmapDrawable {
        private final WeakReference bitmapWorkerTaskReference;
//        private BitmapWorkerTask bitmapWorkerTask;

        public AsyncDrawable(Resources res, BitmapWorkerTask bitmapWorkerTask) {
            super(res);
            bitmapWorkerTaskReference =
                    new WeakReference(bitmapWorkerTask);
//            this.bitmapWorkerTask = bitmapWorkerTask;
        }

        public BitmapWorkerTask getBitmapWorkerTask() {
            if (bitmapWorkerTaskReference.get() != null)
                return (BitmapWorkerTask) bitmapWorkerTaskReference.get();
            return null;
//            return bitmapWorkerTask;
        }
    }

    class BitmapWorkerTask extends AsyncTask<Uri, Integer, Bitmap> {
        private final WeakReference imageViewReference;
        private MediaItem imageItem;

        public BitmapWorkerTask(ImageView imageView, MediaItem item) {
            // use weakReference to ensure ImageView can be recycled
            imageViewReference = new WeakReference(imageView);
            this.imageItem = item;
        }
        public boolean isVideo() {
        	return (imageItem.getMediaType()==MediaObject.MEDIA_TYPE_VIDEO);
        }
        
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }
        // load photo in Background
        @Override
		protected Bitmap doInBackground(Uri... params) {
			Bitmap bitmap = null;
			int id = -1;
			if (null != mImageId.get(imageItem.getFilePath())) {
				id = mImageId.get(imageItem.getFilePath());
			} else {
				Cursor cursor = mContext.getContentResolver().query(params[0],
						new String[] { MediaStore.MediaColumns._ID }, null,
						null, null);
				if (cursor != null) {
					if (cursor.moveToFirst()) {
						id = cursor.getInt(cursor
							.getColumnIndexOrThrow(MediaStore.MediaColumns._ID));
					}
					cursor.close();
				}
			}
			if (id != -1) {
				if (imageItem.getMediaType() == MediaObject.MEDIA_TYPE_VIDEO) {
					bitmap = MediaStore.Video.Thumbnails.getThumbnail(
							mContext.getContentResolver(), id,
							Images.Thumbnails.MICRO_KIND, options);
					if (bitmap == null) {
						bitmap = BitmapFactory.decodeResource(
								mContext.getResources(),
								R.drawable.video_thumbnail);
					}
				} else {
					bitmap = MediaStore.Images.Thumbnails.getThumbnail(
							mContext.getContentResolver(), id,
							Images.Thumbnails.MICRO_KIND, options);
					if (bitmap == null) {
						bitmap = BitmapFactory.decodeResource(
								mContext.getResources(),
								R.drawable.photo_thumbnail);
					}
				}
			}
//			taskList.remove(params[0]);
            //imageItem.setThumbnail(bitmap);
            photoCache.put(params[0], new SoftReference(bitmap));
			return bitmap;
		}
        
        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);
        }
        
        // once complete,if ImageView still exist， set bitmap to ImageView
        @Override
        protected void onPostExecute(Bitmap bitmap) {
            if (isCancelled()) {  
                bitmap = null;  
            }
      
            if (imageViewReference != null && bitmap != null) {
                final ImageView imageView = (ImageView) imageViewReference.get();
                final BitmapWorkerTask bitmapWorkerTask =  getBitmapWorkerTask(imageView);
                if (this == bitmapWorkerTask && imageView != null) {
                    imageView.setImageBitmap(bitmap);
                    imageView.setBackground(null);
                }  
            }
        }
        
    }
    public static boolean cancelPotentialWork(Uri uri, ImageView imageView) {  
        final BitmapWorkerTask bitmapWorkerTask = getBitmapWorkerTask(imageView);  
      
        if (bitmapWorkerTask != null) {  
            final Uri thumnailUri = bitmapWorkerTask.imageItem.getContentUri();  
            if (thumnailUri != uri) {
                // Cancel previous task  
                bitmapWorkerTask.cancel(true);
            } else {  
                // The same work is already in progress  
                return false;  
            }  
        }  
        // No task associated with the ImageView, or an existing task was cancelled  
        return true;  
    }

    private static BitmapWorkerTask getBitmapWorkerTask(ImageView imageView) {  
       if (imageView != null) {  
           final Drawable drawable = imageView.getDrawable();  
           if (drawable instanceof AsyncDrawable) {  
               final AsyncDrawable asyncDrawable = (AsyncDrawable) drawable;  
               return asyncDrawable.getBitmapWorkerTask();  
           }  
        }  
        return null;  
    }
    
    public void cancelAllTask() {
        Iterator iterator = taskList.keySet().iterator();
        while(iterator.hasNext())  
        {  
            Uri uri = (Uri) iterator.next();
            BitmapWorkerTask task = taskList.get(uri);
            if (task != null &&!task.isVideo() && !task.isCancelled()) {
                task.cancel(true);
            }
        }  
    }

    public void onDestory() {
        cancelAllTask();
        photoCache.clear();
    }
}
