/*
 * Copyright (C) 2009 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.mazhengyang.vplayer.model;

import java.io.IOException;
import java.io.InputStream;

import android.content.ContentResolver;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore.Video;
import android.util.Log;

import com.android.mazhengyang.vplayer.utils.BitmapManager;

/**
 * Represents a particular video and provides access to the underlying data and
 * two thumbnail bitmaps as well as other information such as the id, and the
 * path to the actual video data.
 */
public class VideoObject extends BaseImage implements IImage {
    private static final String TAG = "VPlayer." + VideoObject.class.getSimpleName();

    /**
     * Constructor.
     *
     * @param id the image id of the image
     * @param cr the content resolver
     */
    public VideoObject(BaseImageList container, ContentResolver cr, long id,
                       int index, Uri uri, String dataPath, String mimeType,
                       long dateTaken, String title, long duration, String resolution) {
        super(container, cr, id, index, uri, dataPath, mimeType, dateTaken,
                title, duration, resolution);
    }

    @Override
    public boolean equals(Object other) {
        if (other == null || !(other instanceof VideoObject))
            return false;
        return fullSizeImageUri().equals(
                ((VideoObject) other).fullSizeImageUri());
    }

    @Override
    public int hashCode() {
        return fullSizeImageUri().toString().hashCode();
    }

    @Override
    public InputStream fullSizeImageData() {
        try {
            InputStream input = mContentResolver
                    .openInputStream(fullSizeImageUri());
            return input;
        } catch (IOException ex) {
            return null;
        }
    }

    public boolean isReadonly() {
        return false;
    }

    public boolean isDrm() {
        return false;
    }

    @Override
    public Bitmap miniThumbBitmap() {
        try {
            if (mResolution == null) {
                return null;
            }
            if (mBitmap != null) {
                if(!mBitmap.isRecycled()){
                    return mBitmap;
                }else{
                    Log.w(TAG, "============miniThumbBitmap: isRecycled, reloading...");
                }
            }
            mBitmap = BitmapManager.instance().getThumbnail(mContentResolver,
                    mId, Video.Thumbnails.MICRO_KIND, null, true);
            return mBitmap;
        } catch (Throwable ex) {
            Log.e(TAG, "miniThumbBitmap got exception", ex);
            return null;
        }
    }

    private Bitmap mBitmap = null;
    private boolean mSelected = false;

    public Bitmap getBitmap() {
        return mBitmap;
    }

    public void setSelected(boolean selected) {
        mSelected = selected;
    }

    public boolean isSelected() {
        return mSelected;
    }

    public String getResolution() {
        return mResolution;
    }

    private static final int INDEX_DATA_PATH = 0;
    private static final int INDEX_TITLE = 1;

    public void updateTitle() {
        Cursor cursor = mContainer.createUpdateCursor(mId);
        if (cursor.moveToFirst()) {
            String dataPath = cursor.getString(INDEX_DATA_PATH);
            String title = cursor.getString(INDEX_TITLE);
            if (title == null || title.length() == 0) {
                title = dataPath;
            }
            setTitle(dataPath, title);
        }
        cursor.close();
    }

    // end

    @Override
    public String toString() {
        return new StringBuilder("VideoObject").append(mId).toString();
    }
}
