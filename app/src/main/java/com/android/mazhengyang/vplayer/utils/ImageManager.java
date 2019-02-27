/*
 * Copyright (C) 2007 The Android Open Source Project
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

package com.android.mazhengyang.vplayer.utils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;

import android.content.ContentResolver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;

import com.android.mazhengyang.vplayer.model.BaseImageList;
import com.android.mazhengyang.vplayer.model.IImage;
import com.android.mazhengyang.vplayer.model.IImageList;
import com.android.mazhengyang.vplayer.model.ImageListUber;
import com.android.mazhengyang.vplayer.model.VideoList;

/**
 * ImageManager is used to retrieve and store images in the media content
 * provider.
 */
public class ImageManager {
	private static final String TAG = "ImageManager";

	public static final Uri VIDEO_STORAGE_URI = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;

	// Sort
	public static final int SORT_BY_NAME = 0;
	public static final int SORT_BY_SIZE = 1;
	public static final int SORT_BY_TYPE = 2;
	public static final int SORT_BY_ASCENDING = 3;
	public static final int SORT_BY_DESCENDING = 4;

	// This is the factory function to create an image list.
	public static IImageList makeImageList(ContentResolver cr, int sort,
										   boolean isEmptyImageList) {

		if (isEmptyImageList || cr == null) {
			return new EmptyImageList();
		}

		// false ==> don't require write access
		// boolean haveSdCard = hasStorage(false);

		// use this code to merge videos and stills into the same list
		ArrayList<BaseImageList> l = new ArrayList<>();
		l.add(new VideoList(cr, VIDEO_STORAGE_URI, sort));

		// Optimization: If some of the lists are empty, remove them.
		// If there is only one remaining list, return it directly.
		Iterator<BaseImageList> iter = l.iterator();
		while (iter.hasNext()) {
			BaseImageList sublist = iter.next();
			if (sublist.isEmpty()) {
				sublist.close();
				iter.remove();
			}
		}

		if (l.size() == 1) {
			BaseImageList list = l.get(0);
			return list;
		}

		ImageListUber uber = new ImageListUber(l.toArray(new IImageList[l
				.size()]), sort);
		return uber;
	}

	private static class EmptyImageList implements IImageList {
		public void close() {
		}

		public int getCount() {
			return 0;
		}

		public boolean isEmpty() {
			return true;
		}

		public IImage getImageAt(int i) {
			return null;
		}

		public boolean removeImage(IImage image) {
			return false;
		}

		public boolean removeImageAt(int i) {
			return false;
		}

		public int getImageIndex(IImage image) {
			throw new UnsupportedOperationException();
		}
	}

	private static boolean checkFsWritable() {
		// Create a temporary file to see whether a volume is really writeable.
		// It's important not to put it in the root directory which may have a
		// limit on the number of files.
		String directoryName = Environment.getExternalStorageDirectory()
				.toString() + "/DCIM";
		File directory = new File(directoryName);
		if (!directory.isDirectory()) {
			if (!directory.mkdirs()) {
				return false;
			}
		}
		File f = new File(directoryName, ".probe");
		try {
			// Remove stale file if any
			if (f.exists()) {
				f.delete();
			}
			if (!f.createNewFile()) {
				return false;
			}
			f.delete();
			return true;
		} catch (IOException ex) {
			Log.e(TAG, ex.getMessage());
			return false;
		}
	}

	public static boolean hasStorage(boolean requireWriteAccess) {
		String state = Environment.getExternalStorageState();

		if (Environment.MEDIA_MOUNTED.equals(state)) {
			if (requireWriteAccess) {
				boolean writable = checkFsWritable();
				return writable;
			} else {
				return true;
			}
		} else if (!requireWriteAccess
				&& Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
			return true;
		}
		return false;
	}

	private static Cursor query(ContentResolver resolver, Uri uri,
			String[] projection, String selection, String[] selectionArgs,
			String sortOrder) {
		try {
			if (resolver == null) {
				return null;
			}
			return resolver.query(uri, projection, selection, selectionArgs,
					sortOrder);
		} catch (UnsupportedOperationException ex) {
			Log.e(TAG, ex.getMessage());
			return null;
		}

	}

	public static boolean isMediaScannerScanning(ContentResolver cr) {
		boolean result = false;
		Cursor cursor = query(cr, MediaStore.getMediaScannerUri(),
				new String[] { MediaStore.MEDIA_SCANNER_VOLUME }, null, null,
				null);
		if (cursor != null) {
			if (cursor.getCount() == 1) {
				cursor.moveToFirst();
				result = "external".equals(cursor.getString(0));
			}
			cursor.close();
		}

		return result;
	}

	public static int getSort(int sort) {
		switch (sort) {
		case 1:
			return ImageManager.SORT_BY_TYPE;
		case 2:
			return ImageManager.SORT_BY_SIZE;
		case 3:
			return ImageManager.SORT_BY_ASCENDING;
		case 4:
			return ImageManager.SORT_BY_DESCENDING;
		default:
			return ImageManager.SORT_BY_NAME;
		}
	}
}
