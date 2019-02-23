package com.android.mazhengyang.vplayer;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;

import com.android.mazhengyang.vplayer.adapter.VideoListAdapter;
import com.android.mazhengyang.vplayer.bean.VideoBean;
import com.android.mazhengyang.vplayer.present.IVideoListPresent;
import com.android.mazhengyang.vplayer.present.VideoListPresentImpl;
import com.android.mazhengyang.vplayer.view.IVideoListView;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MainActivity extends AppCompatActivity implements IVideoListView {

    private static final String TAG = "Vplayer" + MainActivity.class.getSimpleName();

    private IVideoListPresent videoListPresent;
    private VideoListAdapter videoListAdapter;

    @BindView(R.id.video_list_recyclerView)
    RecyclerView recyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        videoListAdapter = new VideoListAdapter(this);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        videoListAdapter.setOnVideoItemClickListener(onVideoItemClickListener);
        recyclerView.setAdapter(videoListAdapter);

        videoListPresent = new VideoListPresentImpl(this, this);
        videoListPresent.loadData();
    }

    @Override
    public void addVideo(VideoBean videoBean) {
        videoListAdapter.add(videoBean);
    }

    private VideoListAdapter.OnVideoItemClickListener onVideoItemClickListener =
            new VideoListAdapter.OnVideoItemClickListener() {
                @Override
                public void onVideoItemClick(VideoBean videoBean) {
//                    Intent intent = new Intent();
//                    intent.setClass(MainActivity.this, PlayerActivity.class);
//                    startActivity(intent);

                    Uri uri = Uri.parse(videoBean.getPath());
                    Log.d(TAG, "onVideoItemClick: uri=" + uri);

                    Intent intent = new Intent("startFloatingPlayer", uri);
                    intent.setClass(MainActivity.this, FloatingService.class);
                    startService(intent);
                }
            };
}
