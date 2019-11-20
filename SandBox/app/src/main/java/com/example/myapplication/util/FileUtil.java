package com.example.myapplication.util;

import android.text.TextUtils;

import java.io.File;

public class FileUtil {
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
}
