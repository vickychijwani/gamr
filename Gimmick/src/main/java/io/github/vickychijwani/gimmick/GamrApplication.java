package io.github.vickychijwani.gimmick;

import android.annotation.SuppressLint;
import android.app.Application;
import android.os.StrictMode;

import org.jetbrains.annotations.NotNull;

import io.github.vickychijwani.gimmick.api.GiantBomb;
import io.github.vickychijwani.gimmick.api.Metacritic;
import io.github.vickychijwani.gimmick.utility.DeviceUtils;

public class GamrApplication extends Application {

    private static GamrApplication sInstance;

    /**
     * The content authority used to identify the Gamr
     * {@link android.content.ContentProvider}
     */
    public static String CONTENT_AUTHORITY;

    /**
     * @return singleton instance of the application
     */
    @NotNull
    public static synchronized GamrApplication getInstance() {
        return sInstance;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        sInstance = this;

        // Set content provider authority
        CONTENT_AUTHORITY = "io.github.vickychijwani.gimmick.provider";

        // Set API keys
        GiantBomb.setApiKey(getString(R.string.giantbomb_api_key));
        Metacritic.setApiKey(getString(R.string.mashape_api_key));

        enableStrictMode();
    }

    /**
     * Used to enable {@link android.os.StrictMode} during production
     */
    @SuppressLint("NewApi")
    public static void enableStrictMode() {
        if (!BuildConfig.DEBUG) {
            return;
        }
        // Enable StrictMode
        final StrictMode.ThreadPolicy.Builder threadPolicyBuilder = new StrictMode.ThreadPolicy.Builder();
        threadPolicyBuilder.detectAll();
        threadPolicyBuilder.penaltyLog();
        StrictMode.setThreadPolicy(threadPolicyBuilder.build());

        // Policy applied to all threads in the virtual machine's process
        final StrictMode.VmPolicy.Builder vmPolicyBuilder = new StrictMode.VmPolicy.Builder();
        vmPolicyBuilder.detectAll();
        vmPolicyBuilder.penaltyLog();
        if (DeviceUtils.isJellyBeanOrHigher()) {
            vmPolicyBuilder.detectLeakedRegistrationObjects();
        }
        StrictMode.setVmPolicy(vmPolicyBuilder.build());
    }

}
