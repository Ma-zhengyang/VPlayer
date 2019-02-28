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

import static com.android.mazhengyang.vplayer.utils.Util.rotate;

import java.io.IOException;
import java.io.InputStream;

import android.content.ContentResolver;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore.Images;
import android.util.Log;

import com.android.mazhengyang.vplayer.utils.BitmapManager;

/**
 * Represents a particular image and provides access to the underlying bitmap
 * and two thumbnail bitmaps as well as other information such as the id, and
 * the path to the actual image data.
 */
public abstract class BaseImage implements IImage {
	private static final String TAG = "VPlayer." + BaseImage.class.getSimpleName();
	protected ContentResolver mContentResolver;

	// Database field
	protected Uri mUri;
	protected long mId;
	protected String mDataPath;
	protected int mIndex;
	protected String mMimeType;
	protected long mDateTaken;
	protected String mTitle;
	protected long mDuration;
	protected String mResolution;

	protected BaseImageList mContainer;;

	protected BaseImage(BaseImageList container, ContentResolver cr, long id,
			int index, Uri uri, String dataPath, String mimeType,
			long dateTaken, String title, long duration, String resolution) {
		mContainer = container;
		mContentResolver = cr;
		mId = id;
		mIndex = index;
		mUri = uri;
		mDataPath = dataPath;
		mMimeType = mimeType;
		mDateTaken = dateTaken;
		mTitle = title;
		mDuration = duration;
		mResolution = resolution;
	}

	protected void setTitle(String dataPath, String title) {
		mDataPath = dataPath;
		mTitle = title;
	}
	
	public long getId(){
		return mId;
	}

	public String getDataPath() {
		return mDataPath;
	}

	public long getDuration() {
		return mDuration;
	}

	@Override
	public int hashCode() {
		return mUri.hashCode();
	}

	public InputStream fullSizeImageData() {
		try {
			InputStream input = mContentResolver.openInputStream(mUri);
			return input;
		} catch (IOException ex) {
			return null;
		}
	}

	public Uri fullSizeImageUri() {
		return mUri;
	}

	public IImageList getContainer() {
		return mContainer;
	}

	public long getDateTaken() {
		return mDateTaken;
	}

	public int getDegreesRotated() {
		return 0;
	}

	public String getMimeType() {
		return mMimeType;
	}

	public String getTitle() {
		return mTitle;
	}

	public Bitmap miniThumbBitmap() {
		Bitmap b;
		try {
			long id = mId;
			b = BitmapManager.instance().getThumbnail(mContentResolver, id,
					Images.Thumbnails.MICRO_KIND, null, false);
		} catch (Throwable ex) {
			Log.e(TAG, "miniThumbBitmap got exception", ex);
			return null;
		}
		if (b != null) {
			b = rotate(b, getDegreesRotated());
		}
		return b;
	}

	protected void onRemove() {
	}

	@Override
	public String toString() {
		return mUri.toString();
	}
}
