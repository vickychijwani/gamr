package io.github.vickychijwani.giantbomb.api;

import android.text.TextUtils;
import android.util.Log;

import com.android.volley.NetworkResponse;
import com.android.volley.ParseError;
import com.android.volley.Response;
import com.android.volley.toolbox.HttpHeaderParser;
import com.android.volley.toolbox.JsonRequest;
import com.android.volley.toolbox.RequestFuture;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.text.ParseException;
import java.util.concurrent.ExecutionException;

import io.github.vickychijwani.giantbomb.item.Game;
import io.github.vickychijwani.giantbomb.item.Platform;
import io.github.vickychijwani.giantbomb.item.ReleaseDate;
import io.github.vickychijwani.giantbomb.item.Video;
import io.github.vickychijwani.network.json.JSONPropertyIterator;
import io.github.vickychijwani.network.volley.VolleyRequestQueue;

class GameResource implements Resource<Game> {

    @Nullable
    public Game fetch(@NotNull String giantBombUrl) {
        Log.i(TAG, "Fetching game info from " + giantBombUrl);

        String url = new URLBuilder(giantBombUrl)
                .setFieldList(ID, NAME, PLATFORMS, IMAGE_URLS, DECK, API_DETAIL_URL,
                        ORIGINAL_RELEASE_DATE, EXPECTED_RELEASE_YEAR, EXPECTED_RELEASE_QUARTER,
                        EXPECTED_RELEASE_MONTH, EXPECTED_RELEASE_DAY,
                        GENRES, FRANCHISES, VIDEOS)
                .build();

        RequestFuture<Game> future = RequestFuture.newFuture();
        GameJsonRequest req = new GameJsonRequest(url, future, future);
        VolleyRequestQueue.add(req);

        try {
            return future.get();    // block until request completes
        } catch (InterruptedException e) {
            Log.e(TAG, Log.getStackTraceString(e));
        } catch (ExecutionException e) {
            Log.e(TAG, Log.getStackTraceString(e));
        }

        return null;
    }

    @Override
    public String getResourceName() {
        return "games";
    }

    @Override
    @NotNull
    public Game itemFromJson(@NotNull JSONObject gameJsonWrapper, @NotNull Game game) {
        try {
            JSONObject gameJson = gameJsonWrapper.getJSONObject(RESULTS);

            if (! parseEssentialGameInfoFromJson(gameJson, game)) {
                return Game.INVALID;
            }

            // genres
            if (! gameJson.isNull(GENRES)) {
                JSONPropertyIterator<String> nameIterator
                        = new JSONPropertyIterator<String>(gameJson.getJSONArray(GENRES), NAME);
                while (nameIterator.hasNext()) {
                    game.addGenre(nameIterator.next());
                }
            }

            // franchises
            if (! gameJson.isNull(FRANCHISES)) {
                JSONPropertyIterator<String> nameIterator
                        = new JSONPropertyIterator<String>(gameJson.getJSONArray(FRANCHISES), NAME);
                while (nameIterator.hasNext()) {
                    game.addFranchise(nameIterator.next());
                }
            }

            // videos
            if (! gameJson.isNull(VIDEOS)) {
                JSONPropertyIterator<Integer> idIterator
                        = new JSONPropertyIterator<Integer>(gameJson.getJSONArray(VIDEOS), ID);
                while (idIterator.hasNext()) {
                    Video video = new Video();
                    video.setGiantBombId(idIterator.next());
                    game.addVideo(video);
                }
            }

            return game;
        } catch (JSONException e) {
            Log.e(TAG, Log.getStackTraceString(e));
        }
        return Game.INVALID;
    }

    static boolean parseEssentialGameInfoFromJson(JSONObject gameJson, Game game)
            throws JSONException {
        if (gameJson.isNull(PLATFORMS))
            return false;

        // platforms
        JSONPropertyIterator<String> platformsIterator
                = new JSONPropertyIterator<String>(gameJson.getJSONArray(PLATFORMS), NAME);
        while (platformsIterator.hasNext()) {
            String platformName = platformsIterator.next();
            try {
                game.addPlatform(Platform.fromName(platformName));
            } catch (IllegalArgumentException e) {
                Log.d(TAG, "Ignoring '" + platformName + "' platform");
            }
        }

        if (game.platforms.size() == 0)
            return false;

        // essentials
        game.giantBombId = gameJson.getInt(ID);
        game.name = gameJson.getString(NAME);
        game.giantBombUrl = gameJson.getString(API_DETAIL_URL);
        JSONObject imagesJson = gameJson.optJSONObject(IMAGE_URLS);
        if (imagesJson != null) {
            game.posterUrl = imagesJson.getString(POSTER_URL);
            game.smallPosterUrl = imagesJson.getString(SMALL_POSTER_URL);
        }
        game.blurb = gameJson.getString(DECK);
        game.releaseDate = parseReleaseDateFromJson(gameJson);
        return true;
    }

    private static ReleaseDate parseReleaseDateFromJson(JSONObject gameJson) {
        ReleaseDate releaseDate = ReleaseDate.INVALID;
        String originalReleaseDate = gameJson.optString(ORIGINAL_RELEASE_DATE);
        short expectedReleaseYear = (short) gameJson.optInt(EXPECTED_RELEASE_YEAR, ReleaseDate.YEAR_INVALID);
        if (originalReleaseDate != null && ! TextUtils.isEmpty(originalReleaseDate) && ! TextUtils.equals(originalReleaseDate, "null")) {
            try {
                releaseDate = new ReleaseDate(originalReleaseDate);
            } catch (ParseException e) {
                Log.e(TAG, Log.getStackTraceString(e));
            }
        } else if (expectedReleaseYear != ReleaseDate.YEAR_INVALID) {
            byte expectedReleaseQuarter = (byte) gameJson.optInt(EXPECTED_RELEASE_QUARTER, ReleaseDate.QUARTER_INVALID);
            byte expectedReleaseMonth = (byte) gameJson.optInt(EXPECTED_RELEASE_MONTH, ReleaseDate.MONTH_INVALID);
            byte expectedReleaseDay = (byte) gameJson.optInt(EXPECTED_RELEASE_DAY, ReleaseDate.DAY_INVALID);

            // if the expected release date is 1st Jan, it is most likely wrong!
            if (expectedReleaseDay == ReleaseDate.DAY_MIN && expectedReleaseMonth == ReleaseDate.MONTH_MIN) {
                expectedReleaseDay = ReleaseDate.DAY_INVALID;
                expectedReleaseMonth = ReleaseDate.MONTH_INVALID;
            }

            try {
                releaseDate = new ReleaseDate(expectedReleaseDay, expectedReleaseMonth, expectedReleaseQuarter, expectedReleaseYear);
            } catch (IllegalArgumentException e) {
                Log.e(TAG, Log.getStackTraceString(e));
            }
        }
        return releaseDate;
    }

    /**
     * A JSON request for retrieving a {@link Game} from a given URL.
     */
    private class GameJsonRequest extends JsonRequest<Game> {

        public GameJsonRequest(String url, Response.Listener<Game> listener,
                               Response.ErrorListener errorListener) {
            super(Method.GET, url, null, listener, errorListener);
        }

        @Override
        protected Response<Game> parseNetworkResponse(NetworkResponse response) {
            try {
                String jsonString = new String(response.data, HttpHeaderParser.parseCharset(response.headers));
                Game game = itemFromJson(new JSONObject(jsonString), new Game());
                return Response.success(game, HttpHeaderParser.parseCacheHeaders(response));
            } catch (UnsupportedEncodingException e) {
                return Response.error(new ParseError(e));
            } catch (JSONException je) {
                return Response.error(new ParseError(je));
            }
        }

    }

}