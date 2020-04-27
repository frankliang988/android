package com.example.myapplication.util;

import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.content.CursorLoader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.provider.OpenableColumns;
import android.text.TextUtils;
import android.util.Log;
import android.webkit.MimeTypeMap;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

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

    /**
     * return the file name without path
     * test with: image from gallery/download
     *            audio file from audio library / download
     * @param uri
     * @return
     */
    @Nullable
    public static String getFileName(Uri uri, Context context) {
        //SXLog.d(TAG, "getFileName() uri: " + uri.toString());
        String result = null;
        if (uri != null && "content".equalsIgnoreCase(uri.getScheme())) {
            Cursor cursor = null;
            try {

                String mimeType = context.getContentResolver().getType(uri);

                cursor = context.getContentResolver().query(uri, new String[]{OpenableColumns.DISPLAY_NAME}, null, null, null);
                if (cursor.moveToFirst()) {
                    result = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));
                }
                if (!TextUtils.isEmpty(mimeType)){
                    String strExt = MimeTypeMap.getSingleton().getExtensionFromMimeType(mimeType);
                    if (strExt != null && !"null".equals(strExt)) {
                        if (!result.endsWith("." + strExt)) {
                            result = result + "." + strExt;
                        }
                    }
                }

            } catch (Exception e) {
                Log.e(TAG, e.getMessage());
            } finally {
                if (cursor != null) {
                    cursor.close();
                }
            }
        }

        if (result == null && uri != null) {
            result = uri.getPath();
            int cut = result.lastIndexOf('/');
            if (cut != -1) {
                result = result.substring(cut + 1);
            }
        }
        return result;
    }

    /**
     * @refer https://gist.github.com/tatocaster/32aad15f6e0c50311626
     * @param context
     * @param fileUri
     * @return
     */
    public static String getRealPath(Context context, Uri fileUri) {
        // if fileUri's scheme is file:\\\, or contains no scheme

        String realPath;
//        // SDK < API11
//        if (Build.VERSION.SDK_INT < 11) {
//            realPath = getRealPathFromURI_BelowAPI11(context, fileUri);
//        }
        // SDK >= 11 && SDK < 19
        if (Build.VERSION.SDK_INT < 19) {
            realPath = getRealPathFromURI_API11to18(context, fileUri);
        }
        // SDK > 19 (Android 4.4) and up
        else {
            realPath = getRealPathFromURI_API19(context, fileUri);
        }
        return realPath;
    }


    @SuppressLint("NewApi")
    public static String getRealPathFromURI_API11to18(Context context, Uri contentUri) {
        String[] proj = {MediaStore.Images.Media.DATA};
        String result = null;

        CursorLoader cursorLoader = new CursorLoader(context, contentUri, proj, null, null, null);
        Cursor cursor = cursorLoader.loadInBackground();

        if (cursor != null) {
            int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            cursor.moveToFirst();
            result = cursor.getString(column_index);
            cursor.close();
        }
        return result;
    }

    public static String getRealPathFromURI_BelowAPI11(Context context, Uri contentUri) {
        String[] proj = {MediaStore.Images.Media.DATA};
        Cursor cursor = context.getContentResolver().query(contentUri, proj, null, null, null);
        int column_index = 0;
        String result = "";
        if (cursor != null) {
            column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            cursor.moveToFirst();
            result = cursor.getString(column_index);
            cursor.close();
            return result;
        }
        return result;
    }

    /**
     * Get a file path from a Uri. This will get the the path for Storage Access
     * Framework Documents, as well as the _data field for the MediaStore and
     * other file-based ContentProviders.
     *
     * @param context The context.
     * @param uri     The Uri to query.
     * @author paulburke
     */
    @SuppressLint("NewApi")
    public static String getRealPathFromURI_API19(final Context context, final Uri uri) {

        final boolean isKitKat = Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT;
        if (!isKitKat) return null;

        // DocumentProvider
        if (DocumentsContract.isDocumentUri(context, uri)) {
            // ExternalStorageProvider
            if (MediaUtil.isExternalStorageDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                if ("primary".equalsIgnoreCase(type)) {
                    return Environment.getExternalStorageDirectory() + "/" + split[1];
                }

                // TODO handle non-primary volumes
            }
            // DownloadsProvider
            else if (MediaUtil.isDownloadsDocument(uri)) {
                try {
                    String fileName = getFileName(uri, context);

                    if (fileName != null) {
                        return Environment.getExternalStorageDirectory().toString() + "/Download/"+ fileName;
                    }

                    String id = DocumentsContract.getDocumentId(uri);
                    if (id.startsWith("raw:")) {
                        id = id.replaceFirst("raw:", "");
                        File file = new File(id);
                        if (file.exists())
                            return id;
                    }
                    if (id.startsWith("raw%3A%2F")){
                        id = id.replaceFirst("raw%3A%2F", "");
                        File file = new File(id);
                        if (file.exists())
                            return id;
                    }
                    final Uri contentUri = ContentUris.withAppendedId(Uri.parse("content://downloads/public_downloads"), Long.valueOf(id));
                    return getDataColumn(context, contentUri, null, null);
                } catch (Exception e) {
                }
            }
            // MediaProvider
            else if (MediaUtil.isMediaDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                Uri contentUri = null;
                if ("image".equals(type)) {
                    contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                } else if ("video".equals(type)) {
                    contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                } else if ("audio".equals(type)) {
                    contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                }

                final String selection = "_id=?";
                final String[] selectionArgs = new String[]{
                        split[1]
                };

                return getDataColumn(context, contentUri, selection, selectionArgs);
            }
            // Add this section because files from google drive goes here...but still return null
            // MediaStore (and general)
            else if ("content".equalsIgnoreCase(uri.getScheme())) {

                // Return the remote address
                if (MediaUtil.isGooglePhotosUri(uri))
                    return uri.getLastPathSegment();

                //FIXME: other google driver docs..
                //@refer https://developers.google.com/drive/api/v3/integrate-open#open_and_convert_google_docs_in_your_app

                String path = getDataColumn(context, uri, null, null);
                return path;
            }
        }
        // MediaStore (and general)
        else if ("content".equalsIgnoreCase(uri.getScheme())) {

            // Return the remote address
            if (MediaUtil.isGooglePhotosUri(uri))
                return uri.getLastPathSegment();

            //FIXME: other google driver docs..
            //@refer https://developers.google.com/drive/api/v3/integrate-open#open_and_convert_google_docs_in_your_app

            return getDataColumn(context, uri, null, null);
        }
        // File
        else if ("file".equalsIgnoreCase(uri.getScheme())) {
            return uri.getPath();
        }

        return null;
    }

    /**
     * Get the value of the data column for this Uri. This is useful for
     * MediaStore Uris, and other file-based ContentProviders.
     *
     * @param context       The context.
     * @param uri           The Uri to query.
     * @param selection     (Optional) Filter used in the query.
     * @param selectionArgs (Optional) Selection arguments used in the query.
     * @return The value of the _data column, which is typically a file path.
     */
    public static String getDataColumn(Context context, Uri uri, String selection,
                                       String[] selectionArgs) {

        Cursor cursor = null;
        final String column = "_data";
        final String[] projection = {
                column
        };

        try {
            cursor = context.getContentResolver().query(uri, projection, selection, selectionArgs,
                    null);
            if (cursor != null && cursor.moveToFirst()) {
                final int index = cursor.getColumnIndex(column);
                return index != -1 ? cursor.getString(index) : null;
            }
        }
        catch (SecurityException e) {
        }
        finally {
            if (cursor != null)
                cursor.close();
        }
        return null;
    }


}
