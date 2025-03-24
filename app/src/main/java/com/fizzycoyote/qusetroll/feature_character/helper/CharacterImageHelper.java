package com.fizzycoyote.qusetroll.feature_character.helper;

import android.content.Context;
import android.net.Uri;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;

public class CharacterImageHelper {
    public static String copyImageToInternalStorage(Context context, Uri uri) {
        InputStream inputStream = null;
        OutputStream outputStream = null;
        String filePath = null;

        try {
            File internalStorageDir = new File(context.getFilesDir(), "characters_images");
            if (!internalStorageDir.exists()) {
                internalStorageDir.mkdir();
            }

            inputStream = context.getContentResolver().openInputStream(uri);

            String mimeType = context.getContentResolver().getType(uri);
            String extension = ".jpg";
            if (mimeType != null) {
                switch (mimeType) {
                    case "image/png":
                        extension = ".png";
                        break;
                    case "image/jpeg":
                        extension = ".jpg";
                        break;
                }
            }

            File imageFile = new File(internalStorageDir, "image_" + System.currentTimeMillis() + extension);
            filePath = imageFile.getAbsolutePath();

            outputStream = Files.newOutputStream(imageFile.toPath());
            byte[] buffer = new byte[1024];
            int length;
            while ((length = inputStream.read(buffer)) > 0) {
                outputStream.write(buffer, 0, length);
            }
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        } finally {
            try {
                if (inputStream != null) inputStream.close();
                if (outputStream != null) outputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return filePath;
    }
}