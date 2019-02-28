package com.android.mazhengyang.vplayer.presenter;

import android.content.Context;

import com.android.mazhengyang.vplayer.model.IImageList;
import com.android.mazhengyang.vplayer.model.IVideoListModel;
import com.android.mazhengyang.vplayer.model.VideoListModelImpl;
import com.android.mazhengyang.vplayer.model.VideoObject;
import com.android.mazhengyang.vplayer.view.IVideoListView;

/**
 * Created by mazhengyang on 19-2-20.
 */

public class VideoListPresentImpl implements IVideoListPresent {

    private static final String TAG = "VPlayer." + VideoListPresentImpl.class.getSimpleName();

    private IVideoListModel videoListModel;
    private IVideoListView videoListView;
    private Context context;

    public VideoListPresentImpl(Context context, IVideoListView videoListView) {
        this.context = context;
        this.videoListView = videoListView;
        videoListModel = new VideoListModelImpl(context);
    }

    @Override
    public IImageList makeAllImages(int sortMode, boolean storageAvailable) {
        return videoListModel.makeAllImages(context, sortMode, storageAvailable);
    }

    @Override
    public void setVisibleRows(int startRow, int endRow) {
        videoListModel.setVisibleRows(startRow,  endRow, this);
    }

    @Override
    public void reDraw(int position) {
        videoListView.reDraw(position);
    }

    @Override
    public void onSuccess(VideoObject videoObject) {

    }

    @Override
    public void onFailure(String error) {

    }

    @Override
    public void stop() {
        videoListModel.stop();
    }

    @Override
    public void recycle() {
        videoListModel.recycle();
    }
}
