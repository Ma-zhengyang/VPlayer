package com.android.mazhengyang.vplayer;

import android.content.Context;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Handler;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;

import com.android.mazhengyang.vplayer.view.FloatingPlayerButton;

import java.io.IOException;

/**
 * Created by mazhengyang on 19-2-22.
 */

public class FloatingPlayer implements View.OnClickListener,
        FloatingPlayerButton.Listener {

    private static final String TAG = "Vplayer." + FloatingPlayer.class.getSimpleName();

    private int baseX, baseY;
    private int xDst, yDst;
    private float mStartX, mStartY;
    private int videoWidth;
    private int videoHeight;
    private View layoutView;
    private Context context;
    private SurfaceView surfaceView;
    private SurfaceHolder surfaceHolder;
    private MediaPlayer mediaPlayer;

    private Handler handler = new Handler();

    private WindowManager windowManager = null;
    private WindowManager.LayoutParams wmParams = null;

    private static int screenWidth = 0;
    private static int screenHeight = 0;
    private static int floatingViewWidth = 0;
    private static int floatingViewHeight = 0;

    // all possible internal states
    private static final int STATE_ERROR = -1;
    private static final int STATE_IDLE = 0;
    private static final int STATE_PREPARING = 1;
    private static final int STATE_PREPARED = 2;
    private static final int STATE_PLAYING = 3;
    private static final int STATE_PAUSED = 4;
    private static final int STATE_PLAYBACK_COMPLETED = 5;
    private int mCurrentState = STATE_IDLE;

    private FloatingPlayerButton backBtn;
    private FloatingPlayerButton closeBtn;
    private FloatingPlayerButton pauseBtn;

    private Uri uri;
    private String scheme;
    private boolean isStream = false;
    private boolean mShowing = false;
    private int startTime = 0;
    private static float titleBarHeight = 0;

    public FloatingPlayer(Context context, int startTime) {
        Log.d(TAG, "FloatingPlayer: ");
        this.context = context;
        this.startTime = startTime;

        View v = View.inflate(context, R.layout.floatingview, null);
        surfaceView = v.findViewById(R.id.floating_surface_view);
        layoutView = v;

        initVideoView();
        initWindow();
    }

    private void initVideoView() {
        Log.d(TAG, "initVideoView: ");
        if (screenWidth == 0) {
            DisplayMetrics dm = context.getResources().getDisplayMetrics();
            screenWidth = dm.widthPixels;
            screenHeight = dm.heightPixels;
            floatingViewWidth = (int) (240 * dm.density);
            floatingViewHeight = (int) (180 * dm.density);
        }
        surfaceView.setOnTouchListener(mTouchListener);
        surfaceHolder = surfaceView.getHolder();
        surfaceHolder.addCallback(mSHCallback);
        surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        mCurrentState = STATE_IDLE;

        backBtn = layoutView.findViewById(R.id.btn_backActivity);
        closeBtn = layoutView.findViewById(R.id.btn_closeFloatView);
        pauseBtn = layoutView.findViewById(R.id.btn_pause);
        backBtn.setOnClickListener(this);
        closeBtn.setOnClickListener(this);
        pauseBtn.setOnClickListener(this);
        backBtn.setListener(this);
        closeBtn.setListener(this);
        pauseBtn.setListener(this);
    }

    private void initWindow() {
        Log.d(TAG, "initWindow: ");
        windowManager = (WindowManager) context
                .getSystemService(Context.WINDOW_SERVICE);
        wmParams = new WindowManager.LayoutParams();
        wmParams.type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
        wmParams.format = PixelFormat.TRANSPARENT;
        wmParams.flags = WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
                | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                | WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS;
        wmParams.gravity = Gravity.LEFT | Gravity.TOP;

        wmParams.x = (screenWidth - floatingViewWidth) / 2;
        wmParams.y = 0;

        baseX = wmParams.x;
        baseY = 0;

        wmParams.width = floatingViewWidth;
        wmParams.height = floatingViewHeight;

        windowManager.addView(layoutView, wmParams);
    }

    public void startVideo(Uri uri) {
        Log.d(TAG, "startVideo: ");
        stopPlayback();
        this.uri = uri;
        scheme = uri.getScheme();
        isStream = isStream();
        openVideo();
        show();
        maybeStartHiding();
    }

    private void startHiding() {
        Log.d(TAG, "startHiding: mShowing=" + mShowing);
        if (!mShowing) {
            return;
        }
        backBtn.setVisibility(View.INVISIBLE);
        closeBtn.setVisibility(View.INVISIBLE);
        pauseBtn.setVisibility(View.INVISIBLE);
        mShowing = false;
    }

    private Runnable startHidingRunnable = new Runnable() {
        public void run() {
            startHiding();
        }
    };

    private void cancelHiding() {
        handler.removeCallbacks(startHidingRunnable);
    }

    private void maybeStartHiding() {
        Log.d(TAG, "maybeStartHiding: ");
        cancelHiding();
        if (mediaPlayer != null) {
            handler.postDelayed(startHidingRunnable, 3000);
        }
    }

    private void show() {
        Log.d(TAG, "show: mShowing=" + mShowing);
        cancelHiding();
        if (mShowing) {
            return;
        }
        backBtn.setVisibility(View.VISIBLE);
        closeBtn.setVisibility(View.VISIBLE);
        pauseBtn.setVisibility(View.VISIBLE);
        mShowing = true;
    }

    private boolean isStream() {
        if ("http".equalsIgnoreCase(scheme) || "rtsp".equalsIgnoreCase(scheme)) {
            Log.d(TAG, "isStream");
            return true;
        } else {
            return false;
        }
    }

    private void openVideo() {
        Log.d(TAG, "openVideo: ");
        if (uri == null || surfaceHolder == null) {
            // not ready for playback just yet, will try again later
            Log.w(TAG, "openVideo uri or surfaceHolder is null");
            return;
        }

        // we shouldn't clear the target state, because somebody might have
        // called start() previously
        if (isStream()) {
            final MediaPlayer player = mediaPlayer;
            mediaPlayer = null;
            if (player != null) {
                mCurrentState = STATE_IDLE;
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        releaseMediaPlayer(player);
                    }
                }).start();
            }
        } else {
            release();
        }
        if (surfaceHolder == null) {
            // not ready for playback just yet, will try again later
            Log.w(TAG, "openVideo  surfaceHolder is null");
            return;
        }
        try {
            mediaPlayer = new MediaPlayer();
            mediaPlayer.setOnPreparedListener(mPreparedListener);
            mediaPlayer.setOnSeekCompleteListener(mSeekCompleteListener);
            mediaPlayer.setOnVideoSizeChangedListener(mSizeChangedListener);
            mediaPlayer.setOnCompletionListener(mCompletionListener);
            mediaPlayer.setOnErrorListener(mErrorListener);
            mediaPlayer.setOnInfoListener(mInfoListener);
//            mediaPlayer.setDisplay(surfaceHolder);
            mediaPlayer.setDataSource(context, uri);
            mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            mediaPlayer.setScreenOnWhilePlaying(true);
            mediaPlayer.prepareAsync();

            mCurrentState = STATE_PREPARING;
        } catch (IOException ex) {
            Log.w(TAG, "Unable to open content: " + uri, ex);
            mCurrentState = STATE_ERROR;
            mErrorListener.onError(mediaPlayer,
                    MediaPlayer.MEDIA_ERROR_UNKNOWN, 0);
            return;
        } catch (IllegalArgumentException ex) {
            Log.w(TAG, "Unable to open content: " + uri, ex);
            mCurrentState = STATE_ERROR;
            mErrorListener.onError(mediaPlayer,
                    MediaPlayer.MEDIA_ERROR_UNKNOWN, 0);
            return;
        }
    }

    /*
     * release the media player in any state
     */
    private void release() {
        Log.d(TAG, "release: ");
        try {
            if (mediaPlayer != null) {
                mediaPlayer.reset();
                mediaPlayer.release();
                mediaPlayer = null;
                mCurrentState = STATE_IDLE;
            }
        } catch (java.lang.IllegalStateException e1) {
            mediaPlayer = null;
            mCurrentState = STATE_IDLE;
            Log.e(TAG, "release: ", e1);
        } catch (NullPointerException e2) {
            mediaPlayer = null;
            mCurrentState = STATE_IDLE;
            Log.e(TAG, "release: ", e2);
        }
    }

    private void releaseMediaPlayer(MediaPlayer player) {
        Log.d(TAG, "releaseMediaPlayer: ");
        try {
            player.reset();
            player.release();
        } catch (java.lang.IllegalStateException e1) {
            Log.e(TAG, "releaseMediaPlayer: ", e1);
        } catch (NullPointerException e2) {
            Log.e(TAG, "releaseMediaPlayer: ", e2);
        }
    }

    SurfaceHolder.Callback mSHCallback = new SurfaceHolder.Callback() {
        public void surfaceChanged(SurfaceHolder holder, int format, int w,
                                   int h) {
            Log.v(TAG, "surfaceChanged Called");
        }

        public void surfaceCreated(SurfaceHolder holder) {
            Log.v(TAG, "surfaceCreated Called");
            openVideo();
        }

        public void surfaceDestroyed(SurfaceHolder holder) {
            Log.v(TAG, "surfaceDestroyed Called");
        }
    };

    MediaPlayer.OnVideoSizeChangedListener mSizeChangedListener = new MediaPlayer.OnVideoSizeChangedListener() {
        public void onVideoSizeChanged(MediaPlayer mp, int width, int height) {
            Log.v(TAG, "onVideoSizeChanged Called");
            try {
                videoWidth = mp.getVideoWidth();
                videoHeight = mp.getVideoHeight();
            } catch (IllegalStateException e) {
                e.printStackTrace();
            }
            if (videoWidth != 0 && videoHeight != 0) {
                resize();
            }
        }
    };

    MediaPlayer.OnSeekCompleteListener mSeekCompleteListener = new MediaPlayer.OnSeekCompleteListener() {
        @Override
        public void onSeekComplete(MediaPlayer mp) {
            // TODO Auto-generated method stub
            Log.v(TAG, "onSeekComplete Called");
        }
    };

    private boolean isInPlaybackState() {
        Log.d(TAG, "isInPlaybackState: mCurrentState=" + mCurrentState);
        return (mediaPlayer != null && mCurrentState != STATE_ERROR
                && mCurrentState != STATE_IDLE && mCurrentState != STATE_PREPARING);
    }

    MediaPlayer.OnPreparedListener mPreparedListener = new MediaPlayer.OnPreparedListener() {
        public void onPrepared(MediaPlayer mp) {
            Log.v(TAG, "onPrepared Called");
            mCurrentState = STATE_PREPARED;

            try {
                videoWidth = mp.getVideoWidth();
                videoHeight = mp.getVideoHeight();
            } catch (IllegalStateException e) {
                e.printStackTrace();
            }

            if (mediaPlayer != null) {
                if (startTime != 0) {
                    seekTo(startTime);
                }
            }

            if (videoWidth != 0 && videoHeight != 0) {
                resize();

                if (!isStream) {
                    start();
                }
            } else {
                // We don't know the video size yet, but should start anyway.
                // The video size might be reported to us later.
                if (!isStream) {
                    start();
                }
            }
        }
    };

    public boolean isPlaying() {
        if (mediaPlayer == null) {
            return false;
        }
        return mediaPlayer.isPlaying();
    }

    public void start() {
        Log.d(TAG, "start: isInPlaybackState " + isInPlaybackState());
        if (isInPlaybackState()) {
            mediaPlayer.setDisplay(surfaceHolder);
            mediaPlayer.start();
            mCurrentState = STATE_PLAYING;
            pauseBtn.setImageResource(R.drawable.ic_btn_floating_pause);
        }
        maybeStartHiding();
    }

    public void pause() {
        Log.d(TAG, "pause: isInPlaybackState " + isInPlaybackState());
        if (isInPlaybackState()) {
            if (mediaPlayer.isPlaying()) {
                mediaPlayer.pause();
                mCurrentState = STATE_PAUSED;
                pauseBtn.setImageResource(R.drawable.ic_btn_floating_play);
            }
        }
        show();
        maybeStartHiding();
    }

    public void stopPlayback() {
        Log.d(TAG, "stopPlayback: ");
        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
            mCurrentState = STATE_IDLE;
        }
    }

    private MediaPlayer.OnInfoListener mInfoListener = new MediaPlayer.OnInfoListener() {
        @Override
        public boolean onInfo(MediaPlayer arg0, int whatInfo, int extra) {
            Log.d(TAG, "onInfo " + whatInfo);
            return false;
        }
    };

    private MediaPlayer.OnErrorListener mErrorListener = new MediaPlayer.OnErrorListener() {
        public boolean onError(MediaPlayer arg0, int arg1, int arg2) {
            Log.e(TAG, "onError Called");
            if (arg1 == MediaPlayer.MEDIA_ERROR_SERVER_DIED) {
                Log.e(TAG, "Media Error, Server Died " + arg2);
            } else if (arg1 == MediaPlayer.MEDIA_ERROR_UNKNOWN) {
                Log.e(TAG, "Media Error, Error Unknown " + arg2);
            }
            mCurrentState = STATE_ERROR;
            return false;
        }
    };

    private MediaPlayer.OnCompletionListener mCompletionListener = new MediaPlayer.OnCompletionListener() {
        public void onCompletion(MediaPlayer mp) {
            Log.v(TAG, "onCompletion Called");
            mCurrentState = STATE_PLAYBACK_COMPLETED;
            stopService();
        }
    };

    private void seekTo(int msec) {
        Log.d(TAG, "seekTo() msec=" + msec);
        Log.d(TAG, "seekTo() isInPlaybackState=" + isInPlaybackState());

        if (isInPlaybackState()) {
            mediaPlayer.seekTo(msec);
        }
    }

    private void stopService() {
        Log.d(TAG, "stopService: ");
        try {
            if (layoutView != null) {
                windowManager.removeView(layoutView);
                layoutView = null;
            }
            Intent intent = new Intent("stopService");
            if (mediaPlayer != null) {
                int time = mediaPlayer.getCurrentPosition();
                mediaPlayer.pause();
                mediaPlayer.stop();
                mediaPlayer.release();
                mediaPlayer = null;
                intent.putExtra("bookmark_time", time);
            }
            intent.setClass(context, FloatingService.class);
            context.startService(intent);
        } catch (Exception e) {
            e.printStackTrace();
            Log.e(TAG, "stopService: ", e);
        }
    }

    public void stop() {
        Log.d(TAG, "stop: ");
        try {
            if (layoutView != null) {
                windowManager.removeView(layoutView);
                layoutView = null;
            }
            if (mediaPlayer != null) {
                mediaPlayer.pause();
                mediaPlayer.stop();
                mediaPlayer.release();
                mediaPlayer = null;
            }
        } catch (Exception e) {
            e.printStackTrace();
            Log.e(TAG, "stop: ", e);
        }
    }

    private void backToActivity() {
        Log.d(TAG, "backToActivity: ");
        try {
            if (layoutView != null) {
                windowManager.removeView(layoutView);
                layoutView = null;
            }
            Intent intent = new Intent("backToActivity");
            intent.setClass(context, FloatingService.class);
            if (mediaPlayer != null) {
                int time = mediaPlayer.getCurrentPosition();
                intent.putExtra("startTime", mediaPlayer.getCurrentPosition());
                intent.putExtra("bookmark_time", time);
                mediaPlayer.pause();
                mediaPlayer.stop();
                mediaPlayer.release();
                mediaPlayer = null;
            }
            context.startService(intent);
        } catch (Exception e) {
            e.printStackTrace();
            Log.e(TAG, "backActivity: ", e);
        }
    }

    private void resize() {
        if (mediaPlayer == null) {
            return;
        }
        videoWidth = floatingViewWidth;
        videoHeight = floatingViewHeight;

/*		int videoWidth = floatingViewWidth;
        int videoHeight = floatingViewHeight;
		float widRate = (float) screenWidth / videoWidth;
		float heiRate = (float) screenHeight / videoHeight;
		if (widRate > heiRate) {
			videoWidth = (int) (videoWidth * heiRate);
			videoHeight = screenHeight;
		} else if (widRate < heiRate) {
			videoWidth = screenWidth;
			videoHeight = (int) (videoHeight * widRate);
		} else {
			videoWidth = screenWidth;
			videoHeight = screenHeight;
		}*/
    }

    @Override
    public void show(boolean pressed) {
        if (pressed) {
            show();
        } else {
            maybeStartHiding();
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_backActivity: {
                backToActivity();
                break;
            }
            case R.id.btn_closeFloatView: {
                stopService();
                break;
            }
            case R.id.btn_pause: {
                if (mediaPlayer.isPlaying()) {
                    pause();
                } else {
                    start();
                }
                break;
            }
            default:
                break;
        }
    }

    private View.OnTouchListener mTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            // TODO Auto-generated method stub
            Log.d(TAG, "onTouch: " + event.getAction());
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    mStartX = event.getRawX();
                    mStartY = event.getRawY();
                    show();
                    break;
                case MotionEvent.ACTION_MOVE:
                    if (layoutView != null) {
                        float rx = event.getRawX();
                        float ry = event.getRawY();
                        int xoffset = (int) (rx - mStartX);
                        int yoffset = (int) (ry - mStartY);
                        updateViewPosition(xoffset, yoffset, false);
                        mStartX = rx;
                        mStartY = ry;
                    }
                    break;
                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_CANCEL:
                    if (layoutView != null) {
                        updateViewPosition(-1, -1, true);
                    }
                    mStartX = mStartY = 0;
                    xDst = wmParams.x - baseX;
                    yDst = wmParams.y - baseY;
                    maybeStartHiding();
                    break;
            }
            return true;
        }
    };

    private void updateViewPosition(float xoffset, float yoffset, boolean isUp) {
        Log.d(TAG, "updateViewPosition: ");
        try {
            if (isUp) {
                resetLocation();
            } else {
                wmParams.x += xoffset;
                wmParams.y += yoffset;
            }
            windowManager.updateViewLayout(layoutView, wmParams);
        } catch (NullPointerException e) {
            e.printStackTrace();
            Log.e(TAG, "updateViewPosition: ", e);
        } catch (Exception e) {
            e.printStackTrace();
            Log.e(TAG, "updateViewPosition: ", e);
        }
    }

    private void resetLocation() {
        Log.d(TAG, "resetLocation: ");
        int halfW = floatingViewWidth / 2;
        int halfH = floatingViewHeight / 2;
        int x = wmParams.x;
        int y = wmParams.y;

        if (x < 0 && Math.abs(x) > halfW) {// 左x
            wmParams.x = -floatingViewWidth / 2;
        } else if (x > 0 && (x + floatingViewWidth > screenWidth + halfW)) {// 右x
            wmParams.x = screenWidth - floatingViewWidth / 2;
        }

        if (y < 0 && Math.abs(y) > halfH) {// 上y
            wmParams.y = -floatingViewHeight / 2;
        } else if (y > 0
                && (y + floatingViewHeight + titleBarHeight > screenHeight + halfH)) {// 下y
            wmParams.y = screenHeight - floatingViewHeight / 2 - (int) titleBarHeight;
        }
    }

    public void onConfigurationChanged(Context context) {
        DisplayMetrics dm = context.getResources().getDisplayMetrics();
        screenWidth = dm.widthPixels;
        screenHeight = dm.heightPixels;

        try {
            wmParams.x = (screenWidth - floatingViewWidth) / 2;
            wmParams.y = 0;

            baseX = wmParams.x;
            baseY = 0;

            wmParams.x += xDst;
            wmParams.y += yDst;
            resetLocation();
            windowManager.updateViewLayout(layoutView, wmParams);
        } catch (NullPointerException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


}
