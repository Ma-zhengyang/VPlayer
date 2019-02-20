package com.android.mazhengyang.vplayer.present;

import android.content.Context;

import com.android.mazhengyang.vplayer.bean.VideoBean;
import com.android.mazhengyang.vplayer.model.IDataToPresent;
import com.android.mazhengyang.vplayer.model.IVideoListModel;
import com.android.mazhengyang.vplayer.model.VideoListModelImpl;
import com.android.mazhengyang.vplayer.view.IVideoListView;

/**
 * Created by mazhengyang on 19-2-20.
 */

public class VideoListPresentImpl implements IVideoListPresent, IDataToPresent {

    private IVideoListModel videoListModel;
    private IVideoListView videoListView;
    private Context context;

    public VideoListPresentImpl(Context context, IVideoListView videoListView) {
        videoListModel = new VideoListModelImpl();
        this.videoListView = videoListView;
        this.context = context;
    }

    @Override
    public void loadData() {
        videoListModel.loadData(context, this);
    }

    @Override
    public void onSuccess(VideoBean videoBean) {
        videoListView.addVideo(videoBean);
    }

    @Override
    public void onFailure(String error) {

    }
}
