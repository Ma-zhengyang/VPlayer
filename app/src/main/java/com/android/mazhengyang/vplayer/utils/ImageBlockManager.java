package com.android.mazhengyang.vplayer.utils;

import android.graphics.Bitmap;
import android.os.Handler;
import android.util.Log;

import com.android.mazhengyang.vplayer.model.IImage;
import com.android.mazhengyang.vplayer.model.IImageList;
import com.android.mazhengyang.vplayer.presenter.IVideoListPresent;

import java.util.HashMap;

import static com.android.mazhengyang.vplayer.utils.Util.Assert;

/**
 * Created by mazhengyang on 19-2-27.
 */

public class ImageBlockManager {
    @SuppressWarnings("unused")
    private static final String TAG = "VPlayer." + ImageBlockManager.class.getSimpleName();

    // Number of rows we want to cache.
    private static final int CACHE_ROWS = 60;

    // mCache maps from row number to the ImageBlock.
    private final HashMap<Integer, ImageBlock> mCache;

    // These are parameters set in the constructor.
    private Handler mHandler;
    private IVideoListPresent iVideoListPresent;
    private ImageLoader mLoader;
    private int mRows;

    // Visible row range: [mStartRow, mEndRow). Set by setVisibleRows().
    private int mStartRow = 0;
    private int mEndRow = 0;

    private IImageList mImageList;

    public ImageBlockManager(Handler handler, ImageLoader loader) {
        mHandler = handler;
        mLoader = loader;
        mCache = new HashMap<>();
        // mPendingRequest = 0;
    }

    // Set the window of visible rows. Once set we will start to load them as
    // soon as possible (if they are not already in cache).
    public void setVisibleRows(int startRow, int endRow, IImageList imageList, IVideoListPresent iVideoListPresent) {
        Log.d(TAG, "setVisibleRows: mStartRow=" + mStartRow + ", mEndRow=" + mEndRow);
        Log.d(TAG, "setVisibleRows: startRow=" + startRow + ", endRow=" + endRow);

        mImageList = imageList;
        mRows = imageList.getCount();
        this.iVideoListPresent = iVideoListPresent;

        if (startRow != mStartRow || endRow != mEndRow) {
            mStartRow = startRow;
            mEndRow = endRow;
            startLoading();
        }
    }

    // After clear requests currently in queue, start loading the thumbnails.
    // We need to clear the queue first because the proper order of loading
    // may have changed (because the visible region changed, or some images
    // have been invalidated).
    private void startLoading() {
        clearLoaderQueue();
        continueLoading();
    }

    private void clearLoaderQueue() {
        int[] tags = mLoader.clearQueue();
        for (int pos : tags) {
            int row = pos;
            ImageBlock blk = mCache.get(row);
            Assert(blk != null); // We won't reuse the block if it has pending
            // requests. See getEmptyBlock().
            blk.cancelRequest();
        }
    }

    // Scan the cache and send requests to ImageLoader if needed.
    private void continueLoading() {

        // Scan the visible rows.
        Log.d(TAG, "continueLoading: " + mStartRow + "~" + mEndRow);
        for (int i = mStartRow; i < mEndRow; i++) {
            tryToLoad(i);
        }

        int range = (CACHE_ROWS - (mEndRow - mStartRow)) / 2;
        // Scan other rows.
        // d is the distance between the row and visible region.
        for (int d = 1; d <= range; d++) {
            int after = mEndRow - 1 + d;
            int before = mStartRow - d;
            if (after >= mRows && before < 0) {
                break; // Nothing more the scan.
            }

            if (after < mRows) {
                Log.d(TAG, "continueLoading: after=" + after);
                tryToLoad(after);
            }
            if (before >= 0) {
                Log.d(TAG, "continueLoading: before=" + before);
                tryToLoad(before);
            }
        }
    }

    // Returns number of requests we issued for this row.
    private void tryToLoad(int row) {
        Assert(row >= 0 && row < mRows);
        ImageBlock blk = mCache.get(row);
        if (blk == null) {
            // Find an empty block
            blk = getEmptyBlock();
            blk.setRow(row);
            mCache.put(row, blk);
        }

        blk.loadImages();
    }

    // Get an empty block for the cache.
    private ImageBlock getEmptyBlock() {
        // See if we can allocate a new block.
        if (mCache.size() < CACHE_ROWS) {
            return new ImageBlock();
        }
        // Reclaim the old block with largest distance from the visible region.
        int bestDistance = -1;
        int bestIndex = -1;
        for (int index : mCache.keySet()) {
            // Make sure we don't reclaim a block which still has pending
            // request.
            if (mCache.get(index).hasPendingRequests()) {
                continue;
            }
            int dist = 0;
            if (index >= mEndRow) {
                dist = index - mEndRow + 1;
            } else if (index < mStartRow) {
                dist = mStartRow - index;
            } else {
                // Inside the visible region.
                continue;
            }
            if (dist > bestDistance) {
                bestDistance = dist;
                bestIndex = index;
            }
        }

        ImageBlock blk = mCache.remove(bestIndex);
        if (blk != null) {
            recycleBitmap(bestIndex);
            return blk;
        } else {
            return new ImageBlock();
        }
    }

    // After calling recycle(), the instance should not be used anymore.
    public void recycle() {
        for (ImageBlock blk : mCache.values()) {
            blk.recycle();
        }
        mCache.clear();
    }

    private void recycleBitmap(int row) {
        IImage image = mImageList.getImageAt(row);
        if (image != null) {
            Bitmap b = image.miniThumbBitmap();
            if (b != null) {
                Log.w(TAG, "recycleBitmap:    row=" + row);
                b.recycle();
            }
        }
    }

    // ImageBlock stores bitmap for one row. The loaded thumbnail images are
    // drawn to mBitmap. mBitmap is later used in onDraw() of GridViewSpecial.
    private class ImageBlock {

        private int mRow = -1;
        private int mRequestedMask = 0;

        public ImageBlock() {
        }

        public void setRow(int row) {
            mRow = row;
            mRequestedMask = 0;
        }

        // After recycle, the ImageBlock instance should not be accessed.
        public void recycle() {
            cancelAllRequests();
        }

        private boolean isVisible() {
            return mRow >= mStartRow && mRow <= mEndRow;
        }

        public void loadImages() {
        //    Log.d(TAG, "loadImages: mRow=" + mRow);
            final IImage image = mImageList.getImageAt(mRow);
            if (image != null) {

        //        Log.d(TAG, "loadImages: start=" + System.currentTimeMillis());
                Bitmap b = image.miniThumbBitmap();
          //      Log.d(TAG, "loadImages: end=" + System.currentTimeMillis());

                if (b != null) {
                    drawBitmap(image, b);
                    return;
                }

                ImageLoader.LoadedCallback cb = new ImageLoader.LoadedCallback() {
                    public void run(final Bitmap b) {
                        mHandler.post(new Runnable() {
                            public void run() {
                                drawBitmap(image, b);
                            }
                        });
                    }
                };
                mLoader.getBitmap(image, cb, mRow);
                mRequestedMask = 1;
            }
        }

        // // Whether this block has pending requests.
        public boolean hasPendingRequests() {
            return mRequestedMask != 0;
        }

        // Draw the loaded bitmap to the block bitmap.
        private void drawBitmap(IImage image, Bitmap b) {
            mRequestedMask = 0;
            if (isVisible()) {
               // Log.d(TAG, "drawBitmap: mRow=" + mRow);
                iVideoListPresent.reDraw(mRow);
            }
        }

        // Mark a request as cancelled. The request has already been removed
        // from the queue of ImageLoader, so we only need to mark the fact.
        public void cancelRequest() {
            mRequestedMask = 0;
        }

        // Try to cancel all pending requests for this block. After this
        // completes there could still be requests not cancelled (because it is
        // already in progress). We deal with that situation by setting mBitmap
        // to null in recycle() and check this in loadImageDone().
        private void cancelAllRequests() {
            if (mRequestedMask == 1) {
                if (mLoader.cancel(mImageList.getImageAt(mRow))) {
                    mRequestedMask = 0;
                }
            }
            recycleBitmap(mRow);
        }
    }
}
