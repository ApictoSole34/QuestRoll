package com.fizzycoyote.qusetroll.feature_character.utils;

import android.content.ContentResolver;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.net.Uri;
import android.os.Environment;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;

public class ImageUtils {

    /**
     * Saves an image from a Uri to the app's private directory.
     * @return absolute file path, or null on error
     */
    public static String saveImageToAppDirectory(Context context, Uri sourceUri, String prefix) {
        try {
            File dir = new File(context.getFilesDir(), "character_images");
            if (!dir.exists()) dir.mkdirs();
            String fileName = prefix + UUID.randomUUID().toString() + ".jpg";
            File destFile = new File(dir, fileName);

            InputStream inputStream = context.getContentResolver().openInputStream(sourceUri);
            Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
            if (bitmap == null) return null;

            FileOutputStream out = new FileOutputStream(destFile);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 85, out);
            out.flush();
            out.close();
            inputStream.close();
            return destFile.getAbsolutePath();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Creates a circular thumbnail (200x200) from the given image.
     * @param imagePath path to the full image
     * @return thumbnail file path, or null on error
     */
    public static String createCircularThumbnail(Context context, String imagePath) {
        try {
            Bitmap src = BitmapFactory.decodeFile(imagePath);
            if (src == null) return null;

            int size = 200;
            Bitmap output = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(output);
            Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
            paint.setFilterBitmap(true);
            Rect rect = new Rect(0, 0, size, size);
            RectF rectF = new RectF(rect);

            canvas.drawARGB(0, 0, 0, 0);
            paint.setColor(0xFFFFFFFF);
            canvas.drawRoundRect(rectF, size / 2f, size / 2f, paint);

            paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
            Bitmap scaled = Bitmap.createScaledBitmap(src, size, size, true);
            canvas.drawBitmap(scaled, 0, 0, paint);
            scaled.recycle();
            src.recycle();

            File dir = new File(context.getFilesDir(), "character_thumbnails");
            if (!dir.exists()) dir.mkdirs();
            File thumbFile = new File(dir, "thumb_" + UUID.randomUUID().toString() + ".jpg");
            FileOutputStream out = new FileOutputStream(thumbFile);
            output.compress(Bitmap.CompressFormat.JPEG, 80, out);
            out.flush();
            out.close();
            output.recycle();
            return thumbFile.getAbsolutePath();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}