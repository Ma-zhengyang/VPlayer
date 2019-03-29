package com.android.mazhengyang.vplayer;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.vov.vitamio.MediaPlayer;
import io.vov.vitamio.Vitamio;
import io.vov.vitamio.widget.MediaController;
import io.vov.vitamio.widget.VideoView;

/**
 * Created by mazhengyang on 19-2-22.
 */

public class PlayerActivity extends AppCompatActivity {

    @BindView(R.id.video_view)
    VideoView mVideoView;

    @SuppressWarnings("unused")
    private static final String TAG = "VPlayer." + PlayerActivity.class.getSimpleName();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        Log.d(TAG, "onCreate: ");
        super.onCreate(savedInstanceState);

        Vitamio.isInitialized(getApplicationContext());
//        Log.d(TAG, "onCreate: getVitamioPackage="+Vitamio.getVitamioPackage());
//        Log.d(TAG, "onCreate: getDataPath="+Vitamio.getDataPath());
//        Log.d(TAG, "onCreate: getLibraryPath="+Vitamio.getLibraryPath());
//        Log.d(TAG, "onCreate: getBrowserLibraryPath="+Vitamio.getBrowserLibraryPath());

        setContentView(R.layout.activity_player);
        ButterKnife.bind(this);

        Intent intent = getIntent();
        if (intent != null) {
            Uri uri = intent.getData();
            Log.d(TAG, "onCreate: uri=" + uri);

            mVideoView.setVideoURI(uri);
            mVideoView.setMediaController(new MediaController(this));
            mVideoView.requestFocus();
            mVideoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mediaPlayer) {
                    // optional need Vitamio 4.0
                    mediaPlayer.setPlaybackSpeed(1.0f);
                }
            });
        }
    }

    @Override
    protected void onResume() {
        Log.d(TAG, "onResume: ");
        super.onResume();
        mVideoView.resume();
    }

    @Override
    protected void onPause() {
        Log.d(TAG, "onPause: ");
        super.onPause();
        mVideoView.pause();
    }

    @Override
    protected void onDestroy() {
        Log.d(TAG, "onDestroy: ");
        super.onDestroy();
        mVideoView.stopPlayback();
    }
}
