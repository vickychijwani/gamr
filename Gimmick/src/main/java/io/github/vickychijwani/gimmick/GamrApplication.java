package io.github.vickychijwani.gimmick;

import android.annotation.SuppressLint;
import android.app.Application;
import android.os.StrictMode;
import android.text.TextUtils;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.Volley;

import org.jetbrains.annotations.NotNull;

import io.github.vickychijwani.gimmick.api.GiantBomb;
import io.github.vickychijwani.gimmick.api.Metacritic;
import io.github.vickychijwani.gimmick.utility.DeviceUtils;

public class GamrApplication extends Application {

    private static GamrApplication sInstance;
    private RequestQueue mRequestQueue;

    private static final String TAG = "VolleyRequest";

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
     * @return the Volley request queue
     */
    public RequestQueue getRequestQueue() {
        // lazily initialize the request queue
        if (mRequestQueue == null) {
            mRequestQueue = Volley.newRequestQueue(getApplicationContext());
        }

        return mRequestQueue;
    }

    /**
     * Adds the specified request to the global queue, if tag is specified
     * then it is used else Default TAG is used.
     *
     * @param req   the request to add
     * @param tag   the string with which to tag the request
     */
    public <T> void addToRequestQueue(Request<T> req, String tag) {
        VolleyLog.d("Adding request to queue: %s", req.getUrl());

        // set the default tag if tag is empty
        req.setTag(TextUtils.isEmpty(tag) ? TAG : tag);

        getRequestQueue().add(req);
    }

    /**
     * Add a request to the global queue using the default tag.
     *
     * @param req   the request to add
     */
    public <T> void addToRequestQueue(Request<T> req) {
        addToRequestQueue(req, TAG);
    }

    /**
     * Cancel all pending requests by the specified tag.
     *
     * @param tag   the tag whose requests are to be cancelled
     */
    public void cancelPendingRequests(@NotNull String tag) {
        mRequestQueue.cancelAll(tag);
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
