package com.android.mazhengyang.vplayer;

import android.content.Context;
import android.net.Uri;
import android.os.Handler;
import android.util.DisplayMetrics;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;

/**
 * Created by mazhengyang on 19-2-22.
 */

public class FloatingPlayer {

    private static final String TAG = "Vplayer" + FloatingPlayer.class.getSimpleName();

    private Context context;
    private View layoutView;
    private SurfaceView surfaceView;
    private SurfaceHolder surfaceHolder;

    private Handler handler = new Handler();

    private static int screenWidth = 0;
    private static int screenHeight = 0;
    private static int videoWdith = 0;
    private static int videoHeight = 0;

    private Uri uri;

    public FloatingPlayer(Context context) {
        this.context = context;

        View v = View.inflate(context, R.layout.floatingview, null);
        surfaceView = v.findViewById(R.id.floating_surface_view);
        layoutView = v;

        if (screenWidth == 0) {
            DisplayMetrics dm = context.getResources().getDisplayMetrics();
            float scale = dm.density;
            screenWidth = dm.widthPixels;
            screenHeight = dm.heightPixels;
            videoWdith = (int) (240 * scale);
            videoHeight = (int) (180 * scale);
        }

    }

    public void initVideo() {

    }

    public void startVideo(Uri uri) {
        this.uri = uri;
    }

    private void startHiding() {

    }

    private Runnable startHidingRunnable = new Runnable() {
        public void run() {
            startHiding();
        }
    };

}
