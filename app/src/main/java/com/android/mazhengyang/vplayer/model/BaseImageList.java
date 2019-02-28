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

import android.content.ContentResolver;
import android.content.ContentUris;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore.Video.Media;
import android.util.Log;

import com.android.mazhengyang.vplayer.utils.ImageManager;

/**
 * A collection of <code>BaseImage</code>s.
 */
public abstract class BaseImageList implements IImageList {
    private static final String TAG = "VPlayer." + BaseImageList.class.getSimpleName();
    private static final int CACHE_CAPACITY = 512;
    private final LruCache<Integer, BaseImage> mCache = new LruCache<>(
            CACHE_CAPACITY);

    protected ContentResolver mContentResolver;
    protected int mSort;

    protected Uri mBaseUri;
    protected Cursor mCursor;
    protected boolean mCursorDeactivated = false;

    protected static final String[] VIDEO_PROJECTION = new String[]{
            Media._ID, Media.DATA, Media.DATE_TAKEN, Media.TITLE,
            Media.MIME_TYPE, Media.DATE_MODIFIED, Media.DURATION,
            Media.RESOLUTION};

    protected static final int INDEX_ID = 0;
    protected static final int INDEX_DATA_PATH = 1;
    protected static final int INDEX_DATE_TAKEN = 2;
    protected static final int INDEX_TITLE = 3;
    protected static final int INDEX_MIME_TYPE = 4;
    protected static final int INDEX_DATE_MODIFIED = 5;
    protected static final int INDEX_DURATION = 6;
    protected static final int INDEX_RESOLUTION = 7;

    public BaseImageList(ContentResolver resolver, Uri uri, int sort) {
        mSort = sort;
        mBaseUri = uri;
        mContentResolver = resolver;
        mCursor = createCursor();

        if (mCursor == null) {
            Log.w(TAG, "createCursor returns null.");
        }

        // TODO: We need to clear the cache because we may "reopen" the image
        // list. After we implement the image list state, we can remove this
        // kind of usage.
        mCache.clear();
    }

    public void close() {
        try {
            invalidateCursor();
        } catch (IllegalStateException e) {
            // IllegalStateException may be thrown if the cursor is stale.
            Log.e(TAG, "Caught exception while deactivating cursor.", e);
        }
        mContentResolver = null;
        if (mCursor != null) {
            mCursor.close();
            mCursor = null;
        }
    }

    // TODO: Change public to protected
    public Uri contentUri(long id) {
        // TODO: avoid using exception for most cases
        try {
            // does our uri already have an id (single image query)?
            // if so just return it
            long existingId = ContentUris.parseId(mBaseUri);
            if (existingId != id)
                Log.e(TAG, "id mismatch");
            return mBaseUri;
        } catch (NumberFormatException ex) {
            // otherwise tack on the id
            return ContentUris.withAppendedId(mBaseUri, id);
        }
    }

    public int getCount() {
        Cursor cursor = getCursor();
        if (cursor == null)
            return 0;
        synchronized (this) {
            return cursor.getCount();
        }
    }

    public boolean isEmpty() {
        return getCount() == 0;
    }

    public Cursor getCursor() {
        synchronized (this) {
            if (mCursor == null)
                return null;
            if (mCursorDeactivated) {
                mCursor.requery();
                mCursorDeactivated = false;
            }
            return mCursor;
        }
    }

    public IImage getImageAt(int i) {
        BaseImage result = mCache.get(i);
        if (result == null) {
            Cursor cursor = getCursor();
            if (cursor == null)
                return null;
            synchronized (this) {
                result = cursor.moveToPosition(i) ? loadImageFromCursor(cursor)
                        : null;
                mCache.put(i, result);
            }
        }
        return result;
    }

    public boolean removeImage(IImage image) {
        // TODO: need to delete the thumbnails as well
        if (mContentResolver.delete(image.fullSizeImageUri(), null, null) > 0) {
            ((BaseImage) image).onRemove();
            invalidateCursor();
            invalidateCache();
            return true;
        } else {
            return false;
        }
    }

    public boolean removeImageAt(int i) {
        // TODO: need to delete the thumbnails as well
        return removeImage(getImageAt(i));
    }

    protected abstract Cursor createCursor();

    protected abstract BaseImage loadImageFromCursor(Cursor cursor);

    protected abstract long getImageId(Cursor cursor);

    protected void invalidateCursor() {
        if (mCursor == null)
            return;
        mCursor.deactivate();
        mCursorDeactivated = true;
    }

    protected void invalidateCache() {
        mCache.clear();
    }

    public int getImageIndex(IImage image) {
        return ((BaseImage) image).mIndex;
    }

    // This provides a default sorting order string for subclasses.
    // The list is first sorted by date, then by id. The order can be ascending
    // or descending, depending on the mSort variable.
    // The date is obtained from the "datetaken" column. But if it is null,
    // the "date_modified" column is used instead.
    protected String sortOrder() {
        return sortOrder(mSort);
    }

    protected static final String[] MY_VIDEO_PROJECTION = new String[]{
            Media.DATA, Media.TITLE};

    public Cursor createUpdateCursor(long id) {
        StringBuilder where = new StringBuilder();
        where.append(Media._ID + " = '" + id + "'");
        Cursor c = mContentResolver.query(mBaseUri, MY_VIDEO_PROJECTION,
                where.toString(), null, null);
        return c;
    }

    public static String sortOrder(int index) {
        switch (index) {
            case ImageManager.SORT_BY_NAME:
                return Media.DEFAULT_SORT_ORDER;
            case ImageManager.SORT_BY_SIZE:
                return "_size ASC";
            case ImageManager.SORT_BY_TYPE:
                return "mime_type";
            case ImageManager.SORT_BY_ASCENDING:
                return "case ifnull(datetaken,0)"
                        + " when 0 then date_modified*1000" + " else datetaken"
                        + " end" + " ASC" + ", _id" + " ASC";
            case ImageManager.SORT_BY_DESCENDING:
                return "case ifnull(datetaken,0)"
                        + " when 0 then date_modified*1000" + " else datetaken"
                        + " end" + " DESC" + ", _id" + " DESC";
        }
        return null;
    }
}
