/*
 * Copyright (C) 2010 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.jrdcom.android.gallery3d.gadget;

import android.annotation.TargetApi;
import android.appwidget.AppWidgetManager;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import com.jrdcom.android.gallery3d.R;
import com.jrdcom.android.gallery3d.app.GalleryApp;
import com.jrdcom.android.gallery3d.common.ApiHelper;
import com.jrdcom.android.gallery3d.data.ContentListener;
import com.jrdcom.android.gallery3d.data.DataManager;
import com.jrdcom.android.gallery3d.data.MediaSet;
import com.jrdcom.android.gallery3d.data.Path;

// M: Mediatek import
import com.jrdcom.mediatek.gallery3d.util.MediatekFeature;
import com.jrdcom.mediatek.gallery3d.util.MtkLog;

@TargetApi(ApiHelper.VERSION_CODES.HONEYCOMB)
public class WidgetService extends RemoteViewsService {

    @SuppressWarnings("unused")
    private static final String TAG = "GalleryAppWidgetService";

    public static final String EXTRA_WIDGET_TYPE = "widget-type";
    public static final String EXTRA_ALBUM_PATH = "album-path";

    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
        int id = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID,
                AppWidgetManager.INVALID_APPWIDGET_ID);
        int type = intent.getIntExtra(EXTRA_WIDGET_TYPE, 0);
        String albumPath = intent.getStringExtra(EXTRA_ALBUM_PATH);

        return new PhotoRVFactory((GalleryApp) getApplicationContext(),
                id, type, albumPath);
    }

    private static class EmptySource implements WidgetSource {

        @Override
        public int size() {
            return 0;
        }

        @Override
        public Bitmap getImage(int index) {
            throw new UnsupportedOperationException();
        }

        @Override
        public Uri getContentUri(int index) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void setContentListener(ContentListener listener) {}

        @Override
        public void reload() {}

        @Override
        public void close() {}
    }

    private static class PhotoRVFactory implements
            RemoteViewsService.RemoteViewsFactory, ContentListener {

        private final int mAppWidgetId;
        private final int mType;
        private final String mAlbumPath;
        private final GalleryApp mApp;

        private WidgetSource mSource;

        public PhotoRVFactory(GalleryApp app, int id, int type, String albumPath) {
            mApp = app;
            mAppWidgetId = id;
            mType = type;
            mAlbumPath = albumPath;
        }

        @Override
        public void onCreate() {
            MtkLog.d(TAG, "onCreate, widget type=" + mType);
            if (mType == WidgetDatabaseHelper.TYPE_ALBUM) {
                Path path = Path.fromString(mAlbumPath);
                //added for mediatek feature
                if (MediatekFeature.isStereoDisplaySupported() || 
                    MediatekFeature.isMpoSupported() ||
                    MediatekFeature.isDrmSupported()) {
                    path.setMtkInclusion(MediatekFeature.getPhotoWidgetInclusion());
                }
                DataManager manager = mApp.getDataManager();
                MediaSet mediaSet = (MediaSet) manager.getMediaObject(path);
                mSource = mediaSet == null
                        ? new EmptySource()
                        : new MediaSetSource(mediaSet);
            } else {
                mSource = new LocalPhotoSource(mApp.getAndroidContext());
            }
            mSource.setContentListener(this);
            AppWidgetManager.getInstance(mApp.getAndroidContext())
                    .notifyAppWidgetViewDataChanged(
                    mAppWidgetId, R.id.appwidget_stack_view);
        }

        @Override
        public void onDestroy() {
            MtkLog.d(TAG, "onDestroy");
            mSource.close();
            mSource = null;
        }

        @Override
        public int getCount() {
            MtkLog.d(TAG, "getCount=" + mSource.size());
            return mSource.size();
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public int getViewTypeCount() {
            return 1;
        }

        @Override
        public boolean hasStableIds() {
            return true;
        }

        @Override
        public RemoteViews getLoadingView() {
            RemoteViews rv = new RemoteViews(
                    mApp.getAndroidContext().getPackageName(),
                    R.layout.appwidget_loading_item);
            rv.setProgressBar(R.id.appwidget_loading_item, 0, 0, true);
            return rv;
        }

        @Override
        public RemoteViews getViewAt(int position) {
            MtkLog.d(TAG, ">> getViewAt: " + position);
            Bitmap bitmap = null;
            Uri uri = null;
            try {
                uri = mSource.getContentUri(position);
                bitmap = mSource.getImage(position);
            } catch (Exception e) {
                // M: if any exception happens here, it most probably means
                // that onDestroy has been called and mSource is not available anymore
                MtkLog.e(TAG, "getViewAt: exception when fetching uri/bitmap: ", e);
                return getLoadingView();
            }
            if (bitmap == null) return getLoadingView();
            
            MtkLog.i(TAG, " getViewAt(" + position + "): uri=" + uri + ", bitmap=" + bitmap);
            // M: added for displaying different overlay for different types of images
            MediatekFeature.drawWidgetImageTypeOverlay(mApp.getAndroidContext(), 
                    uri, bitmap);
            
            RemoteViews views = new RemoteViews(
                    mApp.getAndroidContext().getPackageName(),
                    R.layout.appwidget_photo_item);
            views.setImageViewBitmap(R.id.appwidget_photo_item, bitmap);
            views.setOnClickFillInIntent(R.id.appwidget_photo_item, new Intent()
                    .setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                    .setData(uri));
            return views;
        }

        @Override
        public void onDataSetChanged() {
            MtkLog.d(TAG, "onDataSetChanged");
            mSource.reload();
        }

        @Override
        public void onContentDirty() {
            MtkLog.d(TAG, "onContentDirty");
            AppWidgetManager.getInstance(mApp.getAndroidContext())
                    .notifyAppWidgetViewDataChanged(
                    mAppWidgetId, R.id.appwidget_stack_view);
        }
    }
}