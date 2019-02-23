package com.android.mazhengyang.vplayer;

import android.app.Service;
import android.content.Intent;
import android.net.Uri;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

/**
 * Created by mazhengyang on 19-2-22.
 */

public class FloatingService extends Service {

    private static final String TAG = "Vplayer" + FloatingService.class.getSimpleName();

    private FloatingPlayer floatingPlayer;

    @Override
    public void onCreate() {
        super.onCreate();

        floatingPlayer = new FloatingPlayer(this);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        floatingPlayer = null;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        return super.onUnbind(intent);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        Uri uri = intent.getData();
        Log.d(TAG, "onStartCommand: uri=" + uri);

        if (uri != null) {
            floatingPlayer.startVideo(uri);
        }

        return START_STICKY;
    }
}
