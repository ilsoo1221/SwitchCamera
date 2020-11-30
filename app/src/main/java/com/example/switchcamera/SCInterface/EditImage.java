package com.example.switchcamera.SCInterface;

import android.graphics.Bitmap;

public interface EditImage {
    Bitmap scaleDownBitmapImage(Bitmap bitmap, int newWidth, int newHeight);
    Bitmap rotateImage(Bitmap src);
    Bitmap resizeBitmap(Bitmap src);
}
