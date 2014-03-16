package io.github.vickychijwani.network.volley;

import android.util.Log;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.VolleyLog;

import org.jetbrains.annotations.NotNull;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class VolleyRequestQueue {

    private static final String TAG = "VolleyRequestQueue";

    private final RequestQueue mRequestQueue;

    @Inject
    public VolleyRequestQueue(@NotNull RequestQueue requestQueue) {
        mRequestQueue = requestQueue;
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
    public <T> RequestTag add(Request<T> req, RequestTag tag) {
        VolleyLog.d("Adding request to queue: %s", req.getUrl());

        // set the default tag if tag is empty
        tag = (tag == null) ? RequestTag.DEFAULT : tag;
        req.setTag(tag);

        mRequestQueue.add(req);
        return tag;
    }

    /**
     * Add a request to the global queue, tagged with {@link RequestTag#DEFAULT}.
     *
     * @param req   the request to add
     */
    public <T> RequestTag add(Request<T> req) {
        return add(req, RequestTag.DEFAULT);
    }

    /**
     * Cancel all pending requests having the specified {@link RequestTag}.
     *
     * @param tag   the tag whose requests are to be cancelled
     */
    public void cancelAll(@NotNull RequestTag tag) {
        Log.i(TAG, "Cancelling all pending requests tagged '" + tag.toString() + "'");
        mRequestQueue.cancelAll(tag);
    }

}
