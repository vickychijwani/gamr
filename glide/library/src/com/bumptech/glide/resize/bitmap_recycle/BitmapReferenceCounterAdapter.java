package com.bumptech.glide.resize.bitmap_recycle;

import android.graphics.Bitmap;

public class BitmapReferenceCounterAdapter implements BitmapReferenceCounter {
    @Override
    public void acquireBitmap(Bitmap bitmap) { }

    @Override
    public void releaseBitmap(Bitmap bitmap) { }
}
