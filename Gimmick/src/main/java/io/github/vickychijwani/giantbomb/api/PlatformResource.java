package io.github.vickychijwani.giantbomb.api;

import android.util.Log;

import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.RequestFuture;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutionException;

import io.github.vickychijwani.giantbomb.item.Platform;
import io.github.vickychijwani.network.json.JSONArrayIterator;
import io.github.vickychijwani.network.volley.VolleyRequestQueue;

class PlatformResource implements Resource<Platform> {

    private static PlatformResource sInstance = null;

    /**
     * Use {@link #getInstance()} instead.
     */
    private PlatformResource() { }

    @Nullable
    public List<Platform> fetchAll() {
        Log.i(TAG, "Fetching all platforms...");

        String url = new URLBuilder()
                .setResource(getResourceName())
                .setFieldList(new String[] { ID, NAME, ALIASES, ABBREVIATION })
                .build();

        RequestFuture<JSONObject> future = RequestFuture.newFuture();
        JsonObjectRequest req = new JsonObjectRequest(url, null, future, future);
        VolleyRequestQueue.add(req);

        try {
            JSONArray platformsJson = future.get().getJSONArray(RESULTS);
            JSONArrayIterator platformsJsonIterator = new JSONArrayIterator(platformsJson);
            List<Platform> platforms = new ArrayList<Platform>(platformsJson.length());
            while (platformsJsonIterator.hasNext()) {
                JSONObject platformJson = platformsJsonIterator.next();
                if (platformJson != null) {
                    platforms.add(itemFromJson(platformJson, new Platform()));
                }
            }
            return platforms;
        } catch (InterruptedException e) {
            Log.e(TAG, Log.getStackTraceString(e));
        } catch (ExecutionException e) {
            Log.e(TAG, Log.getStackTraceString(e));
        } catch (JSONException e) {
            Log.e(TAG, Log.getStackTraceString(e));
        }

        return null;
    }

    public static PlatformResource getInstance() {
        if (sInstance == null) {
            sInstance = new PlatformResource();
        }
        return sInstance;
    }

    @Override
    public String getResourceName() {
        return "platforms";
    }

    @NotNull
    @Override
    public Platform itemFromJson(@NotNull JSONObject json, @NotNull Platform platform) {
        try {
            platform.setGiantBombId(json.getInt(ID));
            platform.setName(json.getString(NAME));
            platform.setShortName(json.getString(ABBREVIATION));

            List<String> aliases = new ArrayList<String>();
            if (! json.isNull(ALIASES)) {
                aliases.addAll(Arrays.asList(json.getString(ALIASES).split("\n")));
            }

            platform.setAliases(aliases);
        } catch (JSONException e) {
            Log.e(TAG, Log.getStackTraceString(e));
        }
        return platform;
    }

}
