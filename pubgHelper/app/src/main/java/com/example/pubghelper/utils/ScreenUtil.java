package com.example.pubghelper.utils;

import static android.content.Context.WINDOW_SERVICE;

import android.app.Activity;
import android.content.Context;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.WindowManager;

public class ScreenUtil {
    public static float getDPI(Activity context) {
        DisplayMetrics metrics = new DisplayMetrics();
        context.getWindowManager().getDefaultDisplay().getMetrics(metrics);
        return metrics.densityDpi;
    }
    public static int[] getSize(Context context) {
        WindowManager windowManager = (WindowManager) context.getSystemService(WINDOW_SERVICE);
        //获取屏幕的宽、高，单位是像素
        int width = windowManager.getMaximumWindowMetrics().getBounds().width();
        int height = windowManager.getMaximumWindowMetrics().getBounds().height();
        return new int[]{width, height};
    }
}
