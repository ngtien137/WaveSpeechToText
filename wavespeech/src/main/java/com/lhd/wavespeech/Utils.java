package com.lhd.wavespeech;

import android.content.ContentValues;
import android.content.Context;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;

import java.io.File;

public class Utils {

    public static String FOLDER_NAME = "AudioRecorder";

    public static ContentValues getContentMusic(String fileNewPath, long duration) {
        String mimeType;
        if (fileNewPath.endsWith(".m4a")) {
            mimeType = "audio/mp4a-latm";
        } else if (fileNewPath.endsWith(".wav")) {
            mimeType = "audio/wav";
        } else {
            mimeType = "audio/mpeg";
        }

        File file = new File(fileNewPath);

        ContentValues values = new ContentValues();
        values.put(MediaStore.MediaColumns.DATA, fileNewPath);
        values.put(MediaStore.MediaColumns.TITLE, file.getName());
        values.put(MediaStore.MediaColumns.SIZE, file.length());
        values.put(MediaStore.MediaColumns.MIME_TYPE, mimeType);
        values.put(MediaStore.Audio.Media.IS_MUSIC, true);

        values.put(MediaStore.Audio.Media.DURATION, duration);

        return values;
    }

    public static String getRecordFolder(Context context) {
        String path;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            path = getInternalFolder(context);
        } else {
            path = Environment.getExternalStorageDirectory() + File.separator + "AudioRecorder" + File.separator;
        }
        return path;
    }

    private static String getInternalFolder(Context context) {
        return context.getExternalFilesDir("").getAbsolutePath() + File.separator + FOLDER_NAME;
    }

    public static File getFile(String path, boolean createIfNotExist, boolean isFolder) {
        try {
            File file = new File(path);
            if (file.exists()) return file;
            if (createIfNotExist) {
                if (isFolder) {
                    if (file.mkdir()) return file;
                    else return null;
                } else {
                    if (file.createNewFile()) return file;
                    else return null;
                }
            }
        } catch (Exception e) {
        }
        return null;
    }
}
