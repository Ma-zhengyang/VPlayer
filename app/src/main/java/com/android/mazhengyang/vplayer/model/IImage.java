package com.android.mazhengyang.vplayer.model;

import android.graphics.Bitmap;
import android.net.Uri;

import java.io.InputStream;

/**
 * Created by mazhengyang on 19-2-26.
 */

public interface IImage {
    /** Get the image list which contains this image. */
    public abstract IImageList getContainer();

    /** Get the bitmap for the full size image. */

    /** Get the input stream associated with a given full size image. */
    public abstract InputStream fullSizeImageData();

    public abstract Uri fullSizeImageUri();

    /** Get the path of the (full size) image data. */
    public abstract String getDataPath();

    // Get the title of the image
    public abstract String getTitle();

    // Get metadata of the image
    public abstract long getDateTaken();

    public abstract String getMimeType();

    // Get property of the image
    public abstract boolean isReadonly();

    public abstract boolean isDrm();

    // Get the bitmap of the mini thumbnail.
    public abstract Bitmap miniThumbBitmap();

    public abstract Bitmap getBitmap();

    public abstract long getDuration();
}
