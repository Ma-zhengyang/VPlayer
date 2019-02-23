package com.android.mazhengyang.vplayer.model;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Log;

import com.android.mazhengyang.vplayer.bean.VideoBean;
import com.android.mazhengyang.vplayer.utils.Util;

/**
 * Created by mazhengyang on 19-2-20.
 */

public class VideoListModelImpl implements IVideoListModel {

    private static final String TAG = "Vplayer" + VideoListModelImpl.class.getSimpleName();

    private final Uri videoListUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;

    @Override
    public void loadData(Context context, IDataToPresent listen) {

        Cursor cursor = context.getContentResolver().query(videoListUri, new String[]
                {MediaStore.Video.Media.DISPLAY_NAME,
                        MediaStore.Video.Media.DATA,
                        MediaStore.Video.Media.DURATION},
                null,
                null,
                null);
        int count = cursor.getCount();
        cursor.moveToFirst();
        Log.d(TAG, "loadData: count=" + count);
        for (int i = 0; i != count; ++i) {
            String displayName = cursor.getString(cursor.getColumnIndex(MediaStore.Video.Media.DISPLAY_NAME));
            Long duration = cursor.getLong(cursor.getColumnIndex(MediaStore.Video.Media.DURATION));
            String path = cursor.getString(cursor.getColumnIndex(MediaStore.Video.Media.DATA));
            cursor.moveToNext();
            Log.d(TAG, "onCreate: displayName=" + displayName + ", path=" + path);

            VideoBean videoBean = new VideoBean();
            videoBean.setDisplayName(displayName);
            videoBean.setDuration(Util.formatTime(duration));
            videoBean.setPath(path);
            listen.onSuccess(videoBean);

        }

    }

}
