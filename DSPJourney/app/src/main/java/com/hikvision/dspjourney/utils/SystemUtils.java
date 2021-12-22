package com.hikvision.dspjourney.utils;

import android.content.Context;
import android.graphics.Point;
import android.os.Build;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.WindowManager;

import com.hikvision.dspjourney.MyApplication;

public class SystemUtils {
    /**
     * 获取屏幕高
     *
     * @return
     */
    public static int getDisplayHeight() {
        DisplayMetrics dm;
        dm = MyApplication.getContext().getResources().getDisplayMetrics();
        return dm.heightPixels; // 屏幕密度（像素比例：0.75/1.0/1.5/2.0）
    }

    /**
     * 获取手机屏幕宽度
     *
     * @return
     */
    public static int getDisplayWidth() {
        DisplayMetrics dm;
        dm = MyApplication.getContext().getResources().getDisplayMetrics();
        return dm.widthPixels; // 屏幕密度（像素比例：0.75/1.0/1.5/2.0）
    }
    /**
     * 获取设备屏幕大小
     *
     * @param context
     * @return 0 width, 1 height(includes status bar、navigation bar)
     */
    public static int[] getPhysicalSS(Context context) {
        WindowManager windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        Display display = windowManager.getDefaultDisplay();
        DisplayMetrics displayMetrics = new DisplayMetrics();
        display.getMetrics(displayMetrics);

        int widthSize = displayMetrics.widthPixels;
        int heightSize = displayMetrics.heightPixels;

        if (Build.VERSION.SDK_INT >= 14 && Build.VERSION.SDK_INT < 17) {
            try {
                widthSize = (Integer) Display.class.getMethod("getRawWidth").invoke(display);
                heightSize = (Integer) Display.class.getMethod("getRawHeight").invoke(display);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        if (Build.VERSION.SDK_INT >= 17) {
            try {
                Point realSize = new Point();
                Display.class.getMethod("getRealSize", Point.class).invoke(display, realSize);
                widthSize = realSize.x;
                heightSize = realSize.y;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return new int[]{
                widthSize, heightSize
        };
    }

}
