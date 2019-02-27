package com.android.mazhengyang.vplayer.model;

import android.content.Context;

import com.android.mazhengyang.vplayer.presenter.IVideoListPresent;

/**
 * Created by mazhengyang on 19-2-20.
 */

public interface IVideoListModel {
    IImageList makeAllImages(Context context, int sortMode, boolean storageAvailable);
    void setVisibleRows(int startRow, int endRow, IVideoListPresent iVideoListPresent);
    void stop();
    void recycle();
}
