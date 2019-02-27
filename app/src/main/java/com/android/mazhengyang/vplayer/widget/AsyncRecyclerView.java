package com.android.mazhengyang.vplayer.widget;

import android.content.Context;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.util.Log;

/**
 * Created by mazhengyang on 19-2-26.
 */

public class AsyncRecyclerView extends RecyclerView {

    private static final String TAG = "Vplayer." + AsyncRecyclerView.class.getSimpleName();

    private AsyncRVListener asyncRVListener;

    public AsyncRecyclerView(Context context) {
        super(context);
        Log.d(TAG, "AsyncRecyclerView: context");
    }

    public AsyncRecyclerView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        Log.d(TAG, "AsyncRecyclerView: context, attrs");
        addOnScrollListener(onScrollListener);
    }

    public AsyncRecyclerView(Context context, @Nullable AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        Log.d(TAG, "AsyncRecyclerView: context, attrs, defStyle");
    }

    public interface AsyncRVListener {
        void moveDataWindow(int startRow, int endRow);
    }

    public void setAsyncRVListener(AsyncRVListener asyncRVListener) {
        this.asyncRVListener = asyncRVListener;
    }

    private RecyclerView.OnScrollListener onScrollListener = new RecyclerView.OnScrollListener() {
        @Override
        public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
            super.onScrollStateChanged(recyclerView, newState);

            switch (newState) {
                case RecyclerView.SCROLL_STATE_DRAGGING:
                    Log.d(TAG, "onScrollStateChanged: SCROLL_STATE_DRAGGING");
                    break;
                case RecyclerView.SCROLL_STATE_SETTLING:
                    Log.d(TAG, "onScrollStateChanged: SCROLL_STATE_SETTLING");
                    break;
                case RecyclerView.SCROLL_STATE_IDLE:
                    Log.d(TAG, "onScrollStateChanged: SCROLL_STATE_IDLE");

                    LinearLayoutManager linearLayoutManager = (LinearLayoutManager) getLayoutManager();
                    int firstVisibleItemPosition = linearLayoutManager.findFirstVisibleItemPosition();
                    int lastVisibleItemPosition = linearLayoutManager.findLastVisibleItemPosition();

                    Log.d(TAG, "onScrollStateChanged: firstVisibleItemPosition=" + firstVisibleItemPosition
                            + ", lastVisibleItemPosition=" + lastVisibleItemPosition);

                    if (asyncRVListener != null) {
                        asyncRVListener.moveDataWindow(firstVisibleItemPosition, lastVisibleItemPosition);
                    }
                    break;
            }
        }

        @Override
        public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
            super.onScrolled(recyclerView, dx, dy);
//            Log.d(TAG, "onScrolled: ");
        }
    };

}
