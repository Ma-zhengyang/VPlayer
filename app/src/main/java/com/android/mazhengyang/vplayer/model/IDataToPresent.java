package com.android.mazhengyang.vplayer.model;

import com.android.mazhengyang.vplayer.bean.VideoBean;

/**
 * Created by mazhengyang on 19-2-20.
 */

public interface IDataToPresent {
    void onSuccess(VideoBean videoBean);
    void onFailure(String error);
}
