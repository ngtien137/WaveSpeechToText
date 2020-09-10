package com.lhd.wavespeech;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Typeface;
import android.media.MediaMetadataRetriever;
import android.os.Build;
import android.util.Log;

import androidx.core.content.res.ResourcesCompat;

import com.lhd.MyException;

import java.io.File;

public class CustomViewSupport {

    public static boolean ENABLE_LOG = true;
    public static String TAG = "APP_LOG";

    public static Typeface getTypefaceFromAttribute(Context context, TypedArray typedArray, int styleableId) {
        Typeface typeface = null;
        int fontId = typedArray.getResourceId(styleableId, -1);
        if (fontId != -1) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                typeface = context.getResources().getFont(fontId);
            } else
                typeface = ResourcesCompat.getFont(context, fontId);
        }
        return typeface;
    }

    public static void loge(Object... message) {
        if (ENABLE_LOG) {
            StringBuilder mes = new StringBuilder();
            for (Object sMes : message
            ) {
                String m = "null";
                if (sMes != null)
                    m = sMes.toString();
                mes.append(m);
            }
            Log.e(TAG, mes.toString());
        }
    }

    public static long getDuration(File file) {
        long duration = 0;
        MediaMetadataRetriever mediaMetadataRetriever = new MediaMetadataRetriever();
        try {
            if (!file.exists())
                throw new MyException("Audio Path is not exist or file is invalid. Path: " + file.getAbsolutePath());
            mediaMetadataRetriever.setDataSource(file.getAbsolutePath());
            String durationStr = mediaMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
            if (durationStr != null && !durationStr.isEmpty())
                duration = Long.parseLong(durationStr);
        } catch (Exception e) {
            loge("MediaMetadataRetriever error when get duration: ", e.getMessage());
        }
        mediaMetadataRetriever.release();
        return duration;
    }

    public static float dpToPixel(Context context,float dp) {
        return dp * context.getResources().getDisplayMetrics().density;
    }
}
