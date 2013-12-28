package io.github.vickychijwani.gimmick;

import android.app.Application;
import android.text.TextUtils;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.Volley;

import org.jetbrains.annotations.NotNull;

public class GamrApplication extends Application {

    private static GamrApplication sInstance;
    private RequestQueue mRequestQueue;

    private static final String TAG = "VolleyRequest";

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

}
