package com.android.mazhengyang.vplayer.model;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Log;

import com.android.mazhengyang.vplayer.bean.VideoBean;

/**
 * Created by mazhengyang on 19-2-20.
 */

public class VideoListModelImpl implements IVideoListModel {

    private static final String TAG = VideoListModelImpl.class.getSimpleName();

    private final Uri videoListUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;

    @Override
    public void loadData(Context context, IDataToPresent listen) {

        Cursor cursor = context.getContentResolver().query(videoListUri, new String[]
                {"_display_name", "_data"}, null, null, null);
        int n = cursor.getCount();
        cursor.moveToFirst();
        Log.d(TAG, "onCreate: n=" + n);
        for (int i = 0; i != n; ++i) {
            String displayName = cursor.getString(cursor.getColumnIndex("_display_name"));
            String path = cursor.getString(cursor.getColumnIndex("_data"));
            cursor.moveToNext();
            Log.d(TAG, "onCreate: displayName=" + displayName + ", path=" + path);

            VideoBean videoBean = new VideoBean();
            videoBean.setDisplayName(displayName);
            videoBean.setPath(path);
            listen.onSuccess(videoBean);

        }

    }

}
