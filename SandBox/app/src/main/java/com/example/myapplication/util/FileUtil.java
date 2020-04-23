package com.example.myapplication.util;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.OpenableColumns;
import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.NonNull;

import com.example.myapplication.MainActivity;

import java.io.File;
import java.text.DecimalFormat;

public class FileUtil {
    public static final String TAG = FileUtil.class.getSimpleName();
    /**
     * check the file is exist or not
     * or the file start with content://, return true...
     * @param localFile
     * @return
     */
    public static boolean isExist(String localFile) {
        if (TextUtils.isEmpty(localFile)) return false;
        if (localFile.startsWith("content://")) return true;
        File file = new File(localFile.startsWith("file://")?localFile.substring("file://".length()) : localFile );
        return file.exists();
    }

    /**
     * support the absolute file or start with content://
     * returns file size in bytes
     * @param uri
     * @return
     */
    public static long getFileSize(Uri uri, @NonNull Context context) {
        long fileSize = 0L;
        if (uri != null) {
            if (isContentUri(uri)) {
                Cursor cursor = null;
                try {
                    cursor = context.getContentResolver().query(uri, new String[]{OpenableColumns.SIZE}, null, null, null);
                    if (cursor.moveToFirst()) {
                        fileSize = cursor.getLong(cursor.getColumnIndex(OpenableColumns.SIZE));
                    }
                } catch (Exception e) {
                    Log.e(TAG, e.getMessage());
                } finally {
                    if (cursor != null) {
                        cursor.close();
                    }
                }
            }
            else {
                File file = new File(uri.getPath());
                fileSize = file.length();
            }
        }

        return fileSize;
    }

    public static boolean isContentUri(@NonNull Uri uri) {
        return ContentResolver.SCHEME_CONTENT.equals(uri.getScheme());
    }

    public static String formatFileSize(long size) {
        String hrSize;

        double b = size;
        double k = size / 1024.0;
        double m = ((size / 1024.0) / 1024.0);
        double g = (((size / 1024.0) / 1024.0) / 1024.0);
        double t = ((((size / 1024.0) / 1024.0) / 1024.0) / 1024.0);

        DecimalFormat dec = new DecimalFormat("0.00");

        if (t > 1) {
            hrSize = dec.format(t).concat(" TB");
        } else if (g > 1) {
            hrSize = dec.format(g).concat(" GB");
        } else if (m > 1) {
            hrSize = dec.format(m).concat(" MB");
        } else if (k > 1) {
            hrSize = dec.format(k).concat(" KB");
        } else {
            hrSize = dec.format(b).concat(" Bytes");
        }

        return hrSize;
    }
}
