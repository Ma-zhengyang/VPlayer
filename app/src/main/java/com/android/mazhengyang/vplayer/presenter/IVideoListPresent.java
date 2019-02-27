package com.android.mazhengyang.vplayer.presenter;

import com.android.mazhengyang.vplayer.model.VideoObject;
import com.android.mazhengyang.vplayer.model.IImageList;

/**
 * Created by mazhengyang on 19-2-20.
 */

public interface IVideoListPresent {

    IImageList makeAllImages(int sortMode, boolean storageAvailable);
    void setVisibleRows(int startRow, int endRow);
    void reDraw(int position);
    void onSuccess(VideoObject videoObject);
    void onFailure(String error);
    void stop();
    void recycle();

}
