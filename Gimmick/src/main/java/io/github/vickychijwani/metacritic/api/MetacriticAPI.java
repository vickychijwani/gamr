package io.github.vickychijwani.metacritic.api;

import android.util.Log;

import com.android.volley.AuthFailureError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.RequestFuture;

import org.jetbrains.annotations.NotNull;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import io.github.vickychijwani.giantbomb.item.Game;
import io.github.vickychijwani.giantbomb.item.Platform;
import io.github.vickychijwani.network.volley.VolleyRequestQueue;

public class MetacriticAPI {

    private static final String TAG = "MetacriticAPI";

    private final String mBaseUrl;
    private final String mApiKey;
    private final VolleyRequestQueue mRequestQueue;

    private static final String RESULT = "result";
    private static final String SCORE = "score";

    public MetacriticAPI(@NotNull String baseUrl, @NotNull String apiKey,
                         @NotNull VolleyRequestQueue requestQueue) {
        mBaseUrl = baseUrl;
        mApiKey = apiKey;
        mRequestQueue = requestQueue;
    }

    public void fetchMetascore(@NotNull Game game) {
        if (! game.isReleased()) {
            Log.i(TAG, "Game not released, not fetching Metascore");
            return;
        }

        String url = mBaseUrl;

        Iterator<Platform> platforms = game.getPlatforms();
        while (platforms.hasNext()) {
            Platform platform = platforms.next();
            Map<String, String> paramsMap = new HashMap<String, String>();
            paramsMap.put("title", game.name);
            paramsMap.put("platform", String.valueOf(platform.getMetacriticId()));
            JSONObject params = new JSONObject(paramsMap);

            Log.i(TAG, "Fetching Metascore for \"" + game.name + "\" (" + platform.getShortName() + ")");

            RequestFuture<JSONObject> future = RequestFuture.newFuture();
            JsonObjectRequest req = new JsonObjectRequest(url, params, future, future) {
                @Override
                public Map<String, String> getHeaders() throws AuthFailureError {
                    Map<String, String> headers = new HashMap<String, String>();
                    headers.put("X-Mashape-Authorization", mApiKey);
                    return headers;
                }
            };

            mRequestQueue.add(req);

            try {
                JSONObject resultJson = future.get().getJSONObject(RESULT);
                game.metascore = Short.parseShort(resultJson.getString(SCORE));
                return;     // metascore fetched successfully, break loop and return
            } catch (InterruptedException e) {
                Log.e(TAG, Log.getStackTraceString(e));
            } catch (ExecutionException e) {
                Log.e(TAG, Log.getStackTraceString(e));
            } catch (JSONException e) {
                Log.e(TAG, Log.getStackTraceString(e));
            } catch (NumberFormatException ignored) {

            }
        }

        Log.i(TAG, "Metascore not found for \"" + game.name + "\" on any platform (" + game.getPlatformsDisplayString() + ")");
    }

}
