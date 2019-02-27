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
import android.database.Cursor;
import android.net.Uri;


/**
 * A collection of all the <code>VideoObject</code> in gallery.
 */
public class VideoList extends BaseImageList {
	@SuppressWarnings("unused")
	private static final String TAG = "Vplayer." + VideoList.class.getSimpleName();

	public VideoList(ContentResolver resolver, Uri uri, int sort) {
		super(resolver, uri, sort);
	}

	@Override
	protected long getImageId(Cursor cursor) {
		return cursor.getLong(INDEX_ID);
	}

	@Override
	protected BaseImage loadImageFromCursor(Cursor cursor) {
		long id = cursor.getLong(INDEX_ID);
		String dataPath = cursor.getString(INDEX_DATA_PATH);
		long dateTaken = cursor.getLong(INDEX_DATE_TAKEN);
		if (dateTaken == 0) {
			dateTaken = cursor.getLong(INDEX_DATE_MODIFIED) * 1000;
		}
		String title = cursor.getString(INDEX_TITLE);
		String mimeType = cursor.getString(INDEX_MIME_TYPE);
		if (title == null || title.length() == 0) {
			title = dataPath;
		}
		long duration = cursor.getLong(INDEX_DURATION);
		String resolution = cursor.getString(INDEX_RESOLUTION);
		return new VideoObject(this, mContentResolver, id,
				cursor.getPosition(), contentUri(id), dataPath, mimeType,
				dateTaken, title, duration, resolution);
	}

	@Override
	protected Cursor createCursor() {
		Cursor c = mContentResolver.query(mBaseUri, VIDEO_PROJECTION, null,
				null, sortOrder());
		return c;
	}
}
