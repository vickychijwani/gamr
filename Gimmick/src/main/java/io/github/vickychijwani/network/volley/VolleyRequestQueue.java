package io.github.vickychijwani.network.volley;

import android.content.Context;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.Volley;

import org.jetbrains.annotations.NotNull;

public class VolleyRequestQueue {

    private static final String TAG = "VolleyRequestQueue";

    private static RequestQueue sInstance;

    private VolleyRequestQueue() { }

    public static void initialize(@NotNull Context context) throws IllegalArgumentException {
        if (context.getApplicationContext() == null) {
            throw new IllegalArgumentException("context.getApplicationContext() returned null");
        }
        if (sInstance == null) {
            sInstance = Volley.newRequestQueue(context.getApplicationContext());
        }
    }

    /**
     * Adds the specified request to the global queue. If {@code tag} is non-null
     * then it is used to tag {@code req}, else {@link RequestTag#DEFAULT} is used.
     *
     * @param req   the request to add
     * @param tag   the tag to be attached to the request, can be used later for request manipulation
     * @return      the tag that actually got attached to {@code req} (i.e., {@code tag}, or
     *              {@link RequestTag#DEFAULT} if {@code tag} was null)
     */
    public static <T> RequestTag add(Request<T> req, RequestTag tag) {
        VolleyLog.d("Adding request to queue: %s", req.getUrl());

        // set the default tag if tag is empty
        tag = (tag == null) ? RequestTag.DEFAULT : tag;
        req.setTag(tag);

        sInstance.add(req);
        return tag;
    }

    /**
     * Add a request to the global queue, tagged with {@link RequestTag#DEFAULT}.
     *
     * @param req   the request to add
     */
    public static <T> RequestTag add(Request<T> req) {
        return add(req, RequestTag.DEFAULT);
    }

    /**
     * Cancel all pending requests having the specified {@link RequestTag}.
     *
     * @param tag   the tag whose requests are to be cancelled
     */
    public static void cancelAll(@NotNull RequestTag tag) {
        Log.i(TAG, "Cancelling all pending requests tagged '" + tag.toString() + "'");
        sInstance.cancelAll(tag);
    }

}
