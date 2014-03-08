package io.github.vickychijwani.giantbomb.api;

import android.util.Log;

import com.android.volley.NetworkResponse;
import com.android.volley.ParseError;
import com.android.volley.Response;
import com.android.volley.toolbox.HttpHeaderParser;
import com.android.volley.toolbox.JsonRequest;

import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.Calendar;

import io.github.vickychijwani.giantbomb.item.Game;
import io.github.vickychijwani.giantbomb.item.GameList;
import io.github.vickychijwani.giantbomb.item.ReleaseDate;
import io.github.vickychijwani.network.volley.RequestTag;
import io.github.vickychijwani.network.volley.VolleyRequestQueue;
import io.github.vickychijwani.utility.DateTimeUtils;

class GameListResource implements Resource<GameList> {

    private static GameListResource sInstance = null;

    /**
     * Use {@link #getInstance()} instead.
     */
    private GameListResource() { }

    public RequestTag search(@NotNull String query,
                             Response.Listener<GameList> successHandler,
                             Response.ErrorListener errorHandler) {
        // forgiving search: replace punctuations and spaces with % signs
        // for instance, "batm asylum" should match "Batman: Arkham Asylum"
        query = query.replaceAll("\\W", "%");

        Log.i(TAG, "Searching for \"" + query + "\"...");

        String url = new URLBuilder()
                .setResource(getResourceName())
                .addParam("filter", NAME + ":" + query)
                .setSortOrder(SORT_BY_LATEST_RELEASES, SORT_BY_MOST_REVIEWS)
                .setFieldList(ID, NAME, PLATFORMS, IMAGE_URLS, DECK, API_DETAIL_URL,
                        ORIGINAL_RELEASE_DATE, EXPECTED_RELEASE_YEAR, EXPECTED_RELEASE_QUARTER,
                        EXPECTED_RELEASE_MONTH, EXPECTED_RELEASE_DAY)
                .build();

        GameListJsonRequest req = new GameListJsonRequest(url, successHandler, errorHandler);
        return VolleyRequestQueue.add(req, REQUEST_TAG_SEARCH);
    }

    public RequestTag fetchUpcoming(final Response.Listener<GameList> successHandler,
                                    final Response.ErrorListener errorHandler) {
        int currentYear = Calendar.getInstance().get(Calendar.YEAR);

        Log.i(TAG, "Fetching upcoming games for year " + currentYear + "...");

        // TODO what if the current date is 25th Dec? Shouldn't "upcoming" include the next year as well in that case?

        final String url = new URLBuilder()
                .setResource(getResourceName())
                .addParam("filter", EXPECTED_RELEASE_YEAR + ":" + currentYear)
                .setFieldList(ID, NAME, PLATFORMS, IMAGE_URLS, DECK, API_DETAIL_URL,
                        EXPECTED_RELEASE_YEAR, EXPECTED_RELEASE_QUARTER,
                        EXPECTED_RELEASE_MONTH, EXPECTED_RELEASE_DAY)
                .build();

        // The GiantBomb API, when queried for upcoming games in 2014, also returns all those whose
        // release date is unknown! This wrapper gets rid of those spurious results.
        Response.Listener<GameList> successHandlerWrapper = new Response.Listener<GameList>() {
            @Override
            public void onResponse(GameList games) {
                int unfilteredCount = games.size();
                for (int i = games.size() - 1; i >= 0; --i) {
                    if (ReleaseDate.INVALID.equals(games.get(i).releaseDate)) {
                        games.remove(i);
                    }
                }
                Log.i(TAG, "Filtered out " + (unfilteredCount - games.size()) + " spurious results");
                successHandler.onResponse(games);
            }
        };

        GameListJsonRequest req = new GameListJsonRequest(url, successHandlerWrapper, errorHandler);
        return VolleyRequestQueue.add(req, REQUEST_TAG_UPCOMING);
    }

    public RequestTag fetchRecent(final Response.Listener<GameList> successHandler,
                                  final Response.ErrorListener errorHandler) {
        Calendar currentDate = Calendar.getInstance();
        String now = DateTimeUtils.dateToIsoDateString(currentDate.getTime());
        currentDate.roll(Calendar.YEAR, false);
        String oneYearAgo = DateTimeUtils.dateToIsoDateString(currentDate.getTime());

        Log.i(TAG, "Fetching recent games released from " + oneYearAgo + " to " + now + " ...");

        final String url = new URLBuilder()
                .setResource(getResourceName())
                .addParam("filter", ORIGINAL_RELEASE_DATE + ":" + oneYearAgo + "|" + now)
                .setFieldList(ID, NAME, PLATFORMS, IMAGE_URLS, DECK, API_DETAIL_URL,
                        ORIGINAL_RELEASE_DATE)
                .setSortOrder(SORT_BY_LATEST_RELEASES)
                .build();

        // The GiantBomb API, when queried for recent games, also returns all those whose
        // release date is unknown! This wrapper gets rid of those spurious results.
        Response.Listener<GameList> successHandlerWrapper = new Response.Listener<GameList>() {
            @Override
            public void onResponse(GameList games) {
                int unfilteredCount = games.size();
                for (int i = games.size() - 1; i >= 0; --i) {
                    if (ReleaseDate.INVALID.equals(games.get(i).releaseDate)) {
                        games.remove(i);
                    }
                }
                Log.i(TAG, "Filtered out " + (unfilteredCount - games.size()) + " spurious results");
                successHandler.onResponse(games);
            }
        };

        GameListJsonRequest req = new GameListJsonRequest(url, successHandlerWrapper, errorHandler);
        return VolleyRequestQueue.add(req, REQUEST_TAG_RECENT);
    }

    public static GameListResource getInstance() {
        if (sInstance == null) {
            sInstance = new GameListResource();
        }
        return sInstance;
    }

    @Override
    public String getResourceName() {
        return "games";
    }

    @Override
    @NotNull
    public GameList itemFromJson(@NotNull JSONObject gamesJsonWrapper, @NotNull GameList gameList) {
        JSONArray gamesArray;
        try {
            gamesArray = gamesJsonWrapper.getJSONArray(RESULTS);
        } catch (JSONException e) {
            Log.e(TAG, Log.getStackTraceString(e));
            return gameList;
        }
        Log.i(TAG, "Got " + gamesArray.length() + " games");

        for (int i = 0; i < gamesArray.length(); ++i) {
            try {
                Game game = new Game();
                JSONObject gameJson = gamesArray.getJSONObject(i);
                GameResource.parseEssentialGameInfoFromJson(gameJson, game);
                gameList.add(game);
            } catch (JSONException e) {
                Log.e(TAG, Log.getStackTraceString(e));
            }
        }

        Log.i(TAG, gameList.size() + " games parsed: " + gameList);
        return gameList;
    }

    /**
     * A JSON request for retrieving a <code>{@link java.util.List}<{@link io.github.vickychijwani.giantbomb.item.Game}></code>
     * from a given URL.
     */
    private class GameListJsonRequest extends JsonRequest<GameList> {

        /**
         * @param url               the URL to query
         * @param listener          handler to invoke if request succeeds
         * @param errorListener     handler to invoke if request fails
         */
        public GameListJsonRequest(String url, Response.Listener<GameList> listener,
                                   Response.ErrorListener errorListener) {
            super(Method.GET, url, null, listener, errorListener);
        }

        @Override
        protected Response<GameList> parseNetworkResponse(NetworkResponse response) {
            try {
                String jsonString = new String(response.data, HttpHeaderParser.parseCharset(response.headers));
                GameList games = itemFromJson(new JSONObject(jsonString), new GameList());

                return Response.success(games, HttpHeaderParser.parseCacheHeaders(response));
            } catch (UnsupportedEncodingException e) {
                return Response.error(new ParseError(e));
            } catch (JSONException je) {
                return Response.error(new ParseError(je));
            }
        }

    }

}
