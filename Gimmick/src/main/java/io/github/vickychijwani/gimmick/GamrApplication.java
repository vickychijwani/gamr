package io.github.vickychijwani.gimmick;

import android.annotation.SuppressLint;
import android.app.Application;
import android.os.StrictMode;

import io.github.vickychijwani.giantbomb.api.GiantBomb;
import io.github.vickychijwani.metacritic.api.Metacritic;
import io.github.vickychijwani.network.volley.VolleyRequestQueue;
import io.github.vickychijwani.utility.DeviceUtils;

public class GamrApplication extends Application {

    /**
     * The content authority used to identify the application's
     * {@link android.content.ContentProvider}
     */
    public static String CONTENT_AUTHORITY;

    @Override
    public void onCreate() {
        super.onCreate();

        // set content provider authority
        CONTENT_AUTHORITY = "io.github.vickychijwani.gimmick.provider";

        // initialize network request queue
        VolleyRequestQueue.initialize(this.getApplicationContext());

        // set API keys
        GiantBomb.initialize(getString(R.string.giantbomb_api_key));
        Metacritic.setApiKey(getString(R.string.mashape_api_key));

        enableStrictMode();
    }

    /**
     * Used to enable {@link android.os.StrictMode} during development
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
