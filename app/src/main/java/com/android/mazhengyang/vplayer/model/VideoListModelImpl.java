package com.android.mazhengyang.vplayer.model;

import android.content.Context;
import android.os.Handler;

import com.android.mazhengyang.vplayer.presenter.IVideoListPresent;
import com.android.mazhengyang.vplayer.utils.ImageBlockManager;
import com.android.mazhengyang.vplayer.utils.ImageLoader;
import com.android.mazhengyang.vplayer.utils.ImageManager;

/**
 * Created by mazhengyang on 19-2-20.
 */

public class VideoListModelImpl implements IVideoListModel {

    private static final String TAG = "Vplayer." + VideoListModelImpl.class.getSimpleName();

    private ImageLoader mLoader;
    private ImageBlockManager mImageBlockManager;
    private final Handler mHandler = new Handler();

    private IImageList mImageList;

    public VideoListModelImpl(Context context) {
        mLoader = new ImageLoader(context.getContentResolver(), mHandler);
        mImageBlockManager = new ImageBlockManager(mHandler, mLoader);
    }

    @Override
    public IImageList makeAllImages(Context context, int sortMode, boolean storageAvailable) {
        int sort = ImageManager.getSort(sortMode);
        boolean isEmptyImageList = false;
        if (!storageAvailable) {
            isEmptyImageList = true;
        }
        mImageList = ImageManager.makeImageList(context.getContentResolver(), sort,
                isEmptyImageList);
        return mImageList;
    }

    @Override
    public void setVisibleRows(int start, int end, IVideoListPresent iVideoListPresent) {
        if (mImageBlockManager != null) {
            mImageBlockManager.setVisibleRows(start, end, mImageList, iVideoListPresent);
        }
    }

    @Override
    public void stop() {
        if (mLoader != null) {
            mLoader.stop();
        }
    }

    @Override
    public void recycle() {
        if (mImageBlockManager != null) {
            mImageBlockManager.recycle();
            mImageBlockManager = null;
        }
    }
}
