package com.android.mazhengyang.vplayer;

import android.annotation.TargetApi;
import android.app.ActionBar;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.Toast;

import com.android.mazhengyang.vplayer.adapter.VideoListAdapter;
import com.android.mazhengyang.vplayer.model.IImage;
import com.android.mazhengyang.vplayer.model.IImageList;
import com.android.mazhengyang.vplayer.permissions.PermissionsManager;
import com.android.mazhengyang.vplayer.permissions.PermissionsResultAction;
import com.android.mazhengyang.vplayer.presenter.IVideoListPresent;
import com.android.mazhengyang.vplayer.presenter.VideoListPresentImpl;
import com.android.mazhengyang.vplayer.view.IVideoListView;
import com.android.mazhengyang.vplayer.widget.AsyncRecyclerView;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MainActivity extends AppCompatActivity implements
        IVideoListView, AsyncRecyclerView.AsyncRVListener {

    private static final String TAG = "VPlayer." + MainActivity.class.getSimpleName();

    private static final int REQ_CAN_DRAW_OVERLAYS = 1024;

    private IVideoListPresent videoListPresent;
    private VideoListAdapter videoListAdapter;
    private IImageList mAllImages;

    @BindView(R.id.video_list_recyclerView)
    AsyncRecyclerView asyncRecyclerView;
    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.search_view)
    View searchView;
    @BindView(R.id.search_edit_text)
    EditText searchEditText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate: ");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        requestPermissions();

        videoListPresent = new VideoListPresentImpl(this, this);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
        asyncRecyclerView.setLayoutManager(layoutManager);
        asyncRecyclerView.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));
        asyncRecyclerView.setAsyncRVListener(this);
        videoListAdapter = new VideoListAdapter(this);
        videoListAdapter.setOnVideoItemClickListener(onVideoItemClickListener);
        asyncRecyclerView.setAdapter(videoListAdapter);

        toolbar.inflateMenu(R.menu.main);
        toolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                int id = item.getItemId();
                switch (id) {
                    case R.id.action_search:
                        if (searchView.getVisibility() == View.VISIBLE) {
                            searchView.setVisibility(View.INVISIBLE);
                        } else {
                            searchView.setVisibility(View.VISIBLE);
                            searchEditText.requestFocus();
                            InputMethodManager inputMethodManager = (InputMethodManager)
                                    getSystemService(Context.INPUT_METHOD_SERVICE);
                            if (inputMethodManager != null) {
                                inputMethodManager.showSoftInput(searchEditText, 0);
                            } else {
                                Log.w(TAG, "onMenuItemClick: InputMethodManager is null");
                            }
                        }
                        break;
                    case R.id.action_sort_by:
                        break;
                }
                return true;
            }
        });
    }

    @Override
    protected void onResume() {
        Log.d(TAG, "onResume: ");
        super.onResume();
    }

    @Override
    public void onPause() {
        Log.d(TAG, "onPause: ");
        super.onPause();
        videoListPresent.stop();
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "onDestroy: ");
        super.onDestroy();
        videoListPresent.recycle();
    }

    @Override
    public void onBackPressed() {
        if (searchView.getVisibility() == View.VISIBLE) {
            searchView.setVisibility(View.INVISIBLE);
            return;
        }
        super.onBackPressed();
    }

    @TargetApi(Build.VERSION_CODES.M)
    private void requestPermissions() {
        PermissionsManager.getInstance().
                requestAllManifestPermissionsIfNecessary(this, new PermissionsResultAction() {
                    @Override
                    public void onGranted() {
                        Log.d(TAG, "onGranted: ");
                        startLoading();
                    }

                    @Override
                    public void onDenied(String permission) {
                        Log.d(TAG, "onDenied: permission " + permission);
                        if (!permission.equals(
                                android.Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                            startLoading();
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
        Log.d(TAG, "onActivityResult: requestCode=" + requestCode);
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

    private boolean hasDrawOverLaysPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!Settings.canDrawOverlays(MainActivity.this)) {
                Toast.makeText(MainActivity.this,
                        getString(R.string.drawoverlays_permission),
                        Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION);
                intent.setData(Uri.parse("package:" + getPackageName()));
                startActivityForResult(intent, REQ_CAN_DRAW_OVERLAYS);
                return false;
            }
        }

        return true;
    }

    private VideoListAdapter.OnVideoItemClickListener onVideoItemClickListener =
            new VideoListAdapter.OnVideoItemClickListener() {
                @Override
                public void onVideoItemClick(IImage image) {
                    Log.d(TAG, "onVideoItemClick: ");
                    if (!hasDrawOverLaysPermission()) {
                        return;
                    }
                    Uri uri = image.fullSizeImageUri();
                    Intent intent = new Intent("startFloatingPlayer", uri);
                    intent.setClass(MainActivity.this, FloatingService.class);
                    startService(intent);
                }

                @Override
                public void onVideoItemLongClick(IImage image) {
                    Log.d(TAG, "onVideoItemLongClick: " + image.fullSizeImageUri());
                    Log.d(TAG, "onVideoItemLongClick: " + image.getDataPath());
                    Log.d(TAG, "onVideoItemLongClick: " + image.getMimeType());
                    Log.d(TAG, "onVideoItemLongClick: " + image.getTitle());
                    Log.d(TAG, "onVideoItemLongClick: " + image.getDateTaken());
                    Log.d(TAG, "onVideoItemLongClick: " + image.getDuration());
                    Log.d(TAG, "onVideoItemLongClick: " + image.isDrm());
                }
            };


    private void startLoading() {
        Log.d(TAG, "startLoad: ");

        if (mAllImages != null) {
            mAllImages.close();
            mAllImages = null;
        }

        mAllImages = videoListPresent.makeAllImages(0, true);
        videoListAdapter.setImageList(mAllImages);

        moveDataWindow(-1, -1);
    }

    @Override
    public void moveDataWindow(int startRow, int endRow) {
        startRow = (startRow == -1) ? 0 : startRow;
        endRow = (endRow == -1) ? 10 : endRow;
        endRow = Math.min(endRow, mAllImages.getCount());

        Log.d(TAG, "moveDataWindow: startRow=" + startRow + ", endRow=" + endRow);

        videoListPresent.setVisibleRows(startRow, endRow);
    }


    @Override
    public void reDraw(int position) {
        Log.d(TAG, "reDraw: position=" + position);
        videoListAdapter.reDraw(position);
    }

}
