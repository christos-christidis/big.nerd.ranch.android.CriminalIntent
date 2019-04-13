package com.bignerdranch.criminalintent;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;

class PictureUtils {

    private static Bitmap getScaledBitmap(String path, Point destSize) {
        Point srcSize = getSrcImageSize(path);

        // SOS: For every inSampleSize (eg 2) pixels, there'll be 1 pixel in the final bitmap
        int inSampleSize = 1;
        if (srcSize.x > destSize.x || srcSize.y > destSize.y) {
            float widthScale = (float) srcSize.x / destSize.x;
            float heightScale = (float) srcSize.y / destSize.y;

            inSampleSize = Math.round(Math.max(widthScale, heightScale));
        }

        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inSampleSize = inSampleSize;

        return BitmapFactory.decodeFile(path, options);
    }

    private static Point getSrcImageSize(String path) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(path, options);
        return new Point(options.outWidth, options.outHeight);
    }

    // SOS: unfortunately I don't know the exact dimensions of the ImageView before the 1st layout
    // pass. Thus, I have to make a conservative estimate, eg use the size of the screen.
    static Bitmap getConservativeEstimateBitmap(String path, Activity activity) {
        Point screenSize = new Point();
        activity.getWindowManager().getDefaultDisplay().getSize(screenSize);

        return getScaledBitmap(path, screenSize);
    }
}
