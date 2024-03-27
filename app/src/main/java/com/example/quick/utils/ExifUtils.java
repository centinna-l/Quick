package com.example.quick.utils;

import android.content.Context;
import android.net.Uri;

import androidx.exifinterface.media.ExifInterface;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class ExifUtils {

    public static Uri removeExif(Context context, Uri imageUri) throws IOException {
        InputStream inputStream = context.getContentResolver().openInputStream(imageUri);
        if (inputStream != null) {
            try {
                File outputDir = context.getCacheDir();
                File outputFile = File.createTempFile("image_without_exif", ".jpg", outputDir);
                OutputStream outputStream = new FileOutputStream(outputFile);

                // Copy the image data while removing EXIF metadata
                byte[] buffer = new byte[8 * 1024];
                int bytesRead;
                while ((bytesRead = inputStream.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, bytesRead);
                }

                // Close streams
                inputStream.close();
                outputStream.close();

                return Uri.fromFile(outputFile);
            } finally {
                inputStream.close();
            }
        }
        return null;
    }
}
