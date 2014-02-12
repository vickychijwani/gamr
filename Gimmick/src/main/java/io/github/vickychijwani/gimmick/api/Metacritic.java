package io.github.vickychijwani.gimmick.api;

import android.util.Log;

import com.android.volley.AuthFailureError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.RequestFuture;

import org.jetbrains.annotations.NotNull;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import io.github.vickychijwani.gimmick.GamrApplication;
import io.github.vickychijwani.gimmick.item.Platform;
import io.github.vickychijwani.gimmick.item.SearchResult;

public class Metacritic {

    private static final String TAG = "Metacritic";

    private static final String BASE_URL = "https://byroredux-metacritic.p.mashape.com/find/game";
    private static String API_KEY = null;

    private static final String RESULT = "result";
    private static final String SCORE = "score";

    public static void fetchMetascore(@NotNull SearchResult game) {
        Log.i(TAG, "Fetching Metascore for \"" + game.name + "\"");

        String url = BASE_URL;
        Platform platform = game.getPlatforms().next();
        Map<String, String> paramsMap = new HashMap<String, String>();
        paramsMap.put("title", game.name);
        paramsMap.put("platform", String.valueOf(platform.getMetacriticId()));
        JSONObject params = new JSONObject(paramsMap);

        RequestFuture<JSONObject> future = RequestFuture.newFuture();
        JsonObjectRequest req = new JsonObjectRequest(url, params, future, future) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<String, String>();
                headers.put("X-Mashape-Authorization", API_KEY);
                return headers;
            }
        };

        GamrApplication.getInstance().addToRequestQueue(req);

        try {
            JSONObject resultJson = future.get().getJSONObject(RESULT);
            game.metascore = Short.parseShort(resultJson.getString(SCORE));
        } catch (InterruptedException e) {
            Log.e(TAG, Log.getStackTraceString(e));
        } catch (ExecutionException e) {
            Log.e(TAG, Log.getStackTraceString(e));
        } catch (JSONException e) {
            Log.e(TAG, Log.getStackTraceString(e));
        } catch (NumberFormatException ignored) {

        }
    }

    public static void setApiKey(@NotNull String apiKey) {
        API_KEY = apiKey;
    }

}
