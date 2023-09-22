package com.android.services.util;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.ImageDecoder;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Build;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public class ImageCompression {

    private static final float maxHeight = 1280.0f;
    private static final float maxWidth = 1280.0f;

    public static synchronized String compressImage(Context context, String type, String imagePath, Uri uri, String id) {
        String fileName = imagePath.substring(imagePath.lastIndexOf("/") + 1);
        String filepath = null;
        Bitmap scaledBitmap = null;
        try {
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            Bitmap bmp = null;
            bmp = BitmapFactory.decodeFile(imagePath, options);
            if (bmp == null) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    try {
                        ImageDecoder.Source source = ImageDecoder.createSource(context.getContentResolver(), uri);
                        bmp = ImageDecoder.decodeBitmap(source);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                } else {
                    FileInputStream fileInputStream = null;
                    try {
                        fileInputStream = new FileInputStream(new File(imagePath));
                        bmp = BitmapFactory.decodeStream(fileInputStream);
                    } catch (FileNotFoundException e) {
                        e.getMessage();
                    } finally {
                        if (fileInputStream != null)
                            fileInputStream.close();
                    }
                }
            }
            int actualHeight = options.outHeight;
            int actualWidth = options.outWidth;
            float imgRatio = (float) actualWidth / (float) actualHeight;
            float maxRatio = maxWidth / maxHeight;
            if (actualHeight > maxHeight || actualWidth > maxWidth) {
                if (imgRatio < maxRatio) {
                    imgRatio = maxHeight / actualHeight;
                    actualWidth = (int) (imgRatio * actualWidth);
                    actualHeight = (int) maxHeight;
                } else if (imgRatio > maxRatio) {
                    imgRatio = maxWidth / actualWidth;
                    actualHeight = (int) (imgRatio * actualHeight);
                    actualWidth = (int) maxWidth;
                } else {
                    actualHeight = (int) maxHeight;
                    actualWidth = (int) maxWidth;
                }
            }
            options.inSampleSize = calculateInSampleSize(options, actualWidth, actualHeight);
            options.inJustDecodeBounds = false;
            options.inDither = false;
            options.inPurgeable = true;
            options.inInputShareable = true;
            options.inTempStorage = new byte[16 * 1024];
            try {
                bmp = BitmapFactory.decodeFile(imagePath, options);
            } catch (OutOfMemoryError exception) {
                exception.printStackTrace();
            }
            try {
                scaledBitmap = Bitmap.createBitmap(actualWidth, actualHeight, Bitmap.Config.RGB_565);
            } catch (OutOfMemoryError exception) {
                exception.printStackTrace();
            }

            float ratioX = actualWidth / (float) options.outWidth;
            float ratioY = actualHeight / (float) options.outHeight;
            float middleX = actualWidth / 2.0f;
            float middleY = actualHeight / 2.0f;
            Matrix scaleMatrix = new Matrix();
            scaleMatrix.setScale(ratioX, ratioY, middleX, middleY);
            if (scaledBitmap != null) {
                Canvas canvas = new Canvas(scaledBitmap);
                canvas.setMatrix(scaleMatrix);
                canvas.drawBitmap(bmp, middleX - bmp.getWidth() / 2, middleY - bmp.getHeight() / 2, new Paint(Paint.FILTER_BITMAP_FLAG));
                bmp.recycle();
                ExifInterface exifInterface;
                try {
                    exifInterface = new ExifInterface(imagePath);
                    int orientation = exifInterface.getAttributeInt(ExifInterface.TAG_ORIENTATION, 0);
                    Matrix matrix = new Matrix();
                    if (orientation == ExifInterface.ORIENTATION_ROTATE_90) {
                        matrix.postRotate(90);
                    } else if (orientation == ExifInterface.ORIENTATION_ROTATE_180) {
                        matrix.postRotate(180);
                    } else if (orientation == ExifInterface.ORIENTATION_ROTATE_270) {
                        matrix.postRotate(270);
                    }
                    scaledBitmap = Bitmap.createBitmap(scaledBitmap, 0, 0, scaledBitmap.getWidth(), scaledBitmap.getHeight(), matrix, true);
                } catch (IOException e) {
                    e.getMessage();
                }
                FileOutputStream out = null;
                if (type.equals(AppConstants.PHOTOS_TYPE)) {
                    filepath = AppUtils.retrieveFilePath(context, AppConstants.DIR_PHOTOS, id + "_" + fileName);
                } else if (type.equals(AppConstants.SNAP_CHAT_EVENTS_TYPE)) {
                    filepath = AppUtils.retrieveFilePath(context, AppConstants.DIR_SNAP_CHAT_EVENTS, fileName);
                } else if (type.equals(AppConstants.CAMERA_BUG_TYPE)) {
                    filepath = AppUtils.retrieveFilePath(context, AppConstants.DIR_CAMERA_BUG, fileName);
                }
                try {
                    out = new FileOutputStream(filepath);
                    scaledBitmap.compress(Bitmap.CompressFormat.JPEG, 20, out);
                } catch (FileNotFoundException e) {
                    e.getMessage();
                } finally {
                    if (out != null)
                        out.close();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return filepath;
    }

    private static int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;
        if (height > reqHeight || width > reqWidth) {
            final int heightRatio = Math.round((float) height / (float) reqHeight);
            final int widthRatio = Math.round((float) width / (float) reqWidth);
            inSampleSize = heightRatio < widthRatio ? heightRatio : widthRatio;
        }
        final float totalPixels = width * height;
        final float totalReqPixelsCap = reqWidth * reqHeight * 2;
        while (totalPixels / (inSampleSize * inSampleSize) > totalReqPixelsCap) {
            inSampleSize++;
        }
        return inSampleSize;
    }
}