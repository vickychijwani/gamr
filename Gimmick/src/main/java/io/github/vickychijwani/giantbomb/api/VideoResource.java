package io.github.vickychijwani.giantbomb.api;

import android.text.TextUtils;
import android.util.Log;

import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.RequestFuture;

import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.util.Iterator;
import java.util.concurrent.ExecutionException;

import io.github.vickychijwani.giantbomb.item.Game;
import io.github.vickychijwani.giantbomb.item.Video;
import io.github.vickychijwani.network.json.JSONArrayIterator;
import io.github.vickychijwani.network.volley.VolleyRequestQueue;
import io.github.vickychijwani.utility.DateTimeUtils;

class VideoResource implements Resource<Video> {

    private static VideoResource sInstance = null;

    /**
     * Use {@link #getInstance()} instead.
     */
    private VideoResource() { }

    @NotNull
    public Game fetchAllForGame(@NotNull Game game)
            throws ExecutionException, InterruptedException, JSONException {
        Iterator<Video> videoIterator = game.getVideos();

        String videoIds = "";
        while (videoIterator.hasNext()) {
            Video video = videoIterator.next();
            videoIds += video.getGiantBombId();
            if (videoIterator.hasNext()) {
                videoIds += "|";
            }
        }

        // no videos to fetch
        if (TextUtils.isEmpty(videoIds)) {
            return game;
        }

        String url = URLBuilder.newInstance()
                .setResource(getResourceName())
                .addParam("filter", ID + ":" + videoIds)
                .build();

        Log.i(TAG, "Fetching videos from " + url);

        RequestFuture<JSONObject> future = RequestFuture.newFuture();
        JsonObjectRequest req = new JsonObjectRequest(url, null, future, future);
        VolleyRequestQueue.add(req);

        try {
            JSONArray videoJsonArray = future.get().getJSONArray(RESULTS);  // block until request completes
            Log.i(TAG, "Received " + videoJsonArray.length() + " videos");
            JSONArrayIterator videoJsonIterator = new JSONArrayIterator(videoJsonArray);
            videoIterator = game.getVideos();
            while (videoJsonIterator.hasNext() && videoIterator.hasNext()) {
                JSONObject videoJson = videoJsonIterator.next();
                if (videoJson != null) {
                    itemFromJson(videoJson, videoIterator.next());
                }
            }

            // sometimes GB returns fewer videos than were requested (the error is "Object Not Found"
            // for the missing ones), also some videos may be skipped if their JSON cannot be parsed,
            // so we remove those videos
            while (videoIterator.hasNext()) {
                videoIterator.next();
                videoIterator.remove();
            }
        } catch (InterruptedException e) {
            Log.e(TAG, Log.getStackTraceString(e));
            throw e;
        } catch (ExecutionException e) {
            Log.e(TAG, Log.getStackTraceString(e));
            throw e;
        } catch (JSONException e) {
            Log.e(TAG, Log.getStackTraceString(e));
            throw e;
        }

        return game;
    }

    public static VideoResource getInstance() {
        if (sInstance == null) {
            sInstance = new VideoResource();
        }
        return sInstance;
    }

    @Override
    public String getResourceName() {
        return "videos";
    }

    @Override
    @NotNull
    public Video itemFromJson(@NotNull JSONObject videoJson, @NotNull Video video) {
        // game id is set automatically when calling Game#addVideo()

        try {
            video.setName(videoJson.optString(NAME));
            video.setBlurb(videoJson.optString(DECK));
            video.setGiantBombId(videoJson.getInt(ID));
            video.setLowUrl(videoJson.optString(LOW_URL));
            video.setHighUrl(videoJson.optString(HIGH_URL));
            video.setDuration(videoJson.optInt(LENGTH_SECONDS, -1));
            video.setUser(videoJson.optString(USER));
            video.setType(videoJson.optString(VIDEO_TYPE));
            video.setYoutubeId(videoJson.optString(YOUTUBE_ID));
        } catch (JSONException e) {
            Log.e(TAG, Log.getStackTraceString(e));
        }

        // publish date
        try {
            video.setPublishDate(DateTimeUtils.isoDateStringToDate(videoJson.optString(PUBLISH_DATE, DateTimeUtils.getEarliestDateString())));
        } catch (ParseException e) {
            Log.e(TAG, Log.getStackTraceString(e));
        }

        // thumbnail
        JSONObject imageUrls = videoJson.optJSONObject(IMAGE_URLS);
        if (imageUrls != null) {
            video.setThumbUrl(imageUrls.optString(SCREEN_URL));
        } else {
            Log.w(TAG, "No thumbnail found for video id = " + video.getGiantBombId());
        }

        return video;
    }

}
