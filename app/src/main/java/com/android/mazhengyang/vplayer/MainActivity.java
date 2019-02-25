package com.android.mazhengyang.vplayer;

import android.annotation.TargetApi;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.widget.Toast;

import com.android.mazhengyang.vplayer.adapter.VideoListAdapter;
import com.android.mazhengyang.vplayer.bean.VideoBean;
import com.android.mazhengyang.vplayer.permissions.PermissionsManager;
import com.android.mazhengyang.vplayer.permissions.PermissionsResultAction;
import com.android.mazhengyang.vplayer.present.IVideoListPresent;
import com.android.mazhengyang.vplayer.present.VideoListPresentImpl;
import com.android.mazhengyang.vplayer.view.IVideoListView;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MainActivity extends AppCompatActivity implements IVideoListView {

    private static final String TAG = "Vplayer" + MainActivity.class.getSimpleName();

    private static final int REQ_CAN_DRAW_OVERLAYS = 1024;

    private IVideoListPresent videoListPresent;
    private VideoListAdapter videoListAdapter;

    @BindView(R.id.video_list_recyclerView)
    RecyclerView recyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        requestPermissions();

        videoListAdapter = new VideoListAdapter(this);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        videoListAdapter.setOnVideoItemClickListener(onVideoItemClickListener);
        recyclerView.setAdapter(videoListAdapter);

        videoListPresent = new VideoListPresentImpl(this, this);
    }

    @TargetApi(Build.VERSION_CODES.M)
    private void requestPermissions() {
        PermissionsManager.getInstance().
                requestAllManifestPermissionsIfNecessary(this, new PermissionsResultAction() {
                    @Override
                    public void onGranted() {
                        Log.d(TAG, "onGranted: all permissions have been granted");
                    }

                    @Override
                    public void onDenied(String permission) {
                        Log.d(TAG, "onDenied: Permission " + permission);

                        if (!permission.contains("READ_EXTERNAL_STORAGE")) {
                            videoListPresent.loadData();
                        }

                    }
                });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        Log.d(TAG, "onRequestPermissionsResult: requestCode=" + requestCode);

        PermissionsManager.getInstance().notifyPermissionsChange(permissions, grantResults);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQ_CAN_DRAW_OVERLAYS) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (!Settings.canDrawOverlays(this)) {
                    Toast.makeText(MainActivity.this,
                            getString(R.string.drawoverlays_permission_failed),
                            Toast.LENGTH_SHORT).show();
                }
            }
        }

    }

    @Override
    public void addVideo(VideoBean videoBean) {
        videoListAdapter.add(videoBean);
    }

    private VideoListAdapter.OnVideoItemClickListener onVideoItemClickListener =
            new VideoListAdapter.OnVideoItemClickListener() {
                @Override
                public void onVideoItemClick(VideoBean videoBean) {

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        if (!Settings.canDrawOverlays(MainActivity.this)) {

                            Toast.makeText(MainActivity.this,
                                    getString(R.string.drawoverlays_permission),
                                    Toast.LENGTH_SHORT).show();

                            Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION);
                            intent.setData(Uri.parse("package:" + getPackageName()));
                            startActivityForResult(intent, REQ_CAN_DRAW_OVERLAYS);
                            return;
                        }
                    }

                    Uri uri = Uri.parse(videoBean.getPath());
                    Log.d(TAG, "onVideoItemClick: uri=" + uri);

                    Intent intent = new Intent("startFloatingPlayer", uri);
                    intent.setClass(MainActivity.this, FloatingService.class);
                    startService(intent);
                }
            };
}
