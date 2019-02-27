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

    private static final String TAG = "Vplayer." + FloatingService.class.getSimpleName();

    private FloatingPlayer floatingPlayer;

    @Override
    public void onCreate() {
        Log.d(TAG, "onCreate: ");
        super.onCreate();

        floatingPlayer = new FloatingPlayer(this, 0);
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "onDestroy: ");
        super.onDestroy();
        floatingPlayer = null;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        Log.d(TAG, "onUnbind: ");
        return super.onUnbind(intent);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        if (intent != null) {
            String action = intent.getAction();
            Log.d(TAG, "onStartCommand: action=" + action);
            if ("startFloatingPlayer".equals(action)) {
                Uri uri = intent.getData();
                Log.d(TAG, "onStartCommand: uri=" + uri);
                if (uri != null) {
                    floatingPlayer.startVideo(uri);
                }
            } else if ("backToActivity".equals(action)) {
                Intent i = new Intent();
                i.setClass(this, MainActivity.class);
                startActivity(i);
                stopSelf();
            } else if ("stopService".equals(action)) {
                floatingPlayer.stop();
                stopSelf();
            }
        } else {
            Log.w(TAG, "onStartCommand: intent is null.");
        }

        return START_STICKY;
    }
}
