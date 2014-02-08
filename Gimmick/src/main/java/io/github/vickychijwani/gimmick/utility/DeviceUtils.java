package io.github.vickychijwani.gimmick.utility;

import android.os.Build;

public class DeviceUtils {

    public static boolean isJellyBeanOrHigher() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN;
    }

}
