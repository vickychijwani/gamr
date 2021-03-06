package io.github.vickychijwani.giantbomb.api;

import android.net.Uri;
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
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.text.ParseException;
import java.util.Calendar;
import java.util.List;
import java.util.concurrent.ExecutionException;

import javax.inject.Inject;
import javax.inject.Singleton;

import io.github.vickychijwani.giantbomb.item.Game;
import io.github.vickychijwani.giantbomb.item.GameList;
import io.github.vickychijwani.giantbomb.item.Platform;
import io.github.vickychijwani.giantbomb.item.ReleaseDate;
import io.github.vickychijwani.giantbomb.item.ResourceType;
import io.github.vickychijwani.giantbomb.item.Review;
import io.github.vickychijwani.giantbomb.item.Video;
import io.github.vickychijwani.network.json.JSONArrayIterator;
import io.github.vickychijwani.network.json.JSONPropertyIterator;
import io.github.vickychijwani.network.volley.RequestTag;
import io.github.vickychijwani.network.volley.VolleyRequestQueue;
import io.github.vickychijwani.utility.DateTimeUtils;

@Singleton
public class GamesAPI extends BaseAPI<Game> {

    private final PlatformsAPI mPlatformsApi;

    @Inject
    public GamesAPI(VolleyRequestQueue requestQueue, URLFactory urlFactory,
                    PlatformsAPI platformsApi) {
        super(ResourceType.GAME, requestQueue, urlFactory);
        mPlatformsApi = platformsApi;
    }

    /**
     * Fetch a game's details in a <i>synchronous</i> manner.
     *
     * NOTE: never call this from the UI thread!
     *
     * @param giantBombId  ID of the game on GiantBomb
     * @return  the requested {@link Game}
     */
    @Nullable
    public Game fetch(int giantBombId) {
        Uri uri = newDetailResourceURL(giantBombId)
                .setFieldList(ID, NAME, PLATFORMS, IMAGE_URLS, DECK, ORIGINAL_RELEASE_DATE,
                        EXPECTED_RELEASE_YEAR, EXPECTED_RELEASE_QUARTER,
                        EXPECTED_RELEASE_MONTH, EXPECTED_RELEASE_DAY,
                        GENRES, FRANCHISES, VIDEOS, REVIEWS)
                .build();

        Log.i(TAG, "Fetching game info from " + uri);

        RequestFuture<Game> future = RequestFuture.newFuture();
        GameJsonRequest req = new GameJsonRequest(uri, future, future);
        enqueueRequest(req);

        try {
            return future.get();    // block until request completes
        } catch (InterruptedException e) {
            Log.e(TAG, Log.getStackTraceString(e));
        } catch (ExecutionException e) {
            Log.e(TAG, Log.getStackTraceString(e));
        }

        return null;
    }

    /**
     * Fire an asynchronous game search.
     *
     * @param query             Search term
     * @param successHandler    handler to invoke if request succeeds
     * @param errorHandler      handler to invoke if request fails
     * @return                  a tag that can be used to cancel any ongoing search requests by
     *                          calling {@link io.github.vickychijwani.network.volley.VolleyRequestQueue#cancelAll(io.github.vickychijwani.network.volley.RequestTag)}.
     */
    public RequestTag search(@NotNull String query,
                             Response.Listener<GameList> successHandler,
                             Response.ErrorListener errorHandler) {
        // forgiving search: replace punctuations and spaces with % signs
        // for instance, "batm asylum" should match "Batman: Arkham Asylum"
        query = query.replaceAll("\\W", "%");

        Log.i(TAG, "Searching for \"" + query + "\"...");

        Uri uri = newListResourceURL()
                .addParam("filter", NAME + ":" + query)
                .setSortOrder(SORT_BY_LATEST_RELEASES, SORT_BY_MOST_REVIEWS)
                .setFieldList(ID, NAME, PLATFORMS, IMAGE_URLS, DECK, ORIGINAL_RELEASE_DATE,
                        EXPECTED_RELEASE_YEAR, EXPECTED_RELEASE_QUARTER,
                        EXPECTED_RELEASE_MONTH, EXPECTED_RELEASE_DAY)
                .build();

        GameListJsonRequest req = new GameListJsonRequest(uri, successHandler, errorHandler);
        return enqueueRequest(req, REQUEST_TAG_SEARCH);
    }

    /**
     * Fire an asynchronous request to fetch "upcoming" games (i.e., games that will be released in
     * the current year).
     *
     * @param successHandler    handler to invoke if request succeeds
     * @param errorHandler      handler to invoke if request fails
     * @return                  a tag that can be used to cancel any ongoing "upcoming games"
     *                          requests by calling {@link io.github.vickychijwani.network.volley.VolleyRequestQueue#cancelAll(RequestTag)}.
     */
    public RequestTag fetchUpcoming(final Response.Listener<GameList> successHandler,
                                    final Response.ErrorListener errorHandler) {
        int currentYear = Calendar.getInstance().get(Calendar.YEAR);

        Log.i(TAG, "Fetching upcoming games for year " + currentYear + "...");

        // TODO what if the current date is 25th Dec? Shouldn't "upcoming" include the next year as well in that case?

        final Uri uri = newListResourceURL()
                .addParam("filter", EXPECTED_RELEASE_YEAR + ":" + currentYear)
                .setFieldList(ID, NAME, PLATFORMS, IMAGE_URLS, DECK,
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

        GameListJsonRequest req = new GameListJsonRequest(uri, successHandlerWrapper, errorHandler);
        return enqueueRequest(req, REQUEST_TAG_UPCOMING);
    }

    /**
     * Fire an asynchronous request to fetch recently-released games (i.e., games that have been
     * released in the past 1 year).
     *
     * @param successHandler    handler to invoke if request succeeds
     * @param errorHandler      handler to invoke if request fails
     * @return                  a tag that can be used to cancel any ongoing "recent games"
     *                          requests by calling {@link io.github.vickychijwani.network.volley.VolleyRequestQueue#cancelAll(RequestTag)}.
     */
    public RequestTag fetchRecent(final Response.Listener<GameList> successHandler,
                                  final Response.ErrorListener errorHandler) {
        Calendar currentDate = Calendar.getInstance();
        String now = DateTimeUtils.dateToIsoDateString(currentDate.getTime());
        currentDate.roll(Calendar.YEAR, false);
        String oneYearAgo = DateTimeUtils.dateToIsoDateString(currentDate.getTime());

        Log.i(TAG, "Fetching recent games released from " + oneYearAgo + " to " + now + " ...");

        final Uri uri = newListResourceURL()
                .addParam("filter", ORIGINAL_RELEASE_DATE + ":" + oneYearAgo + "|" + now)
                .setFieldList(ID, NAME, PLATFORMS, IMAGE_URLS, DECK, ORIGINAL_RELEASE_DATE)
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

        GameListJsonRequest req = new GameListJsonRequest(uri, successHandlerWrapper, errorHandler);
        return enqueueRequest(req, REQUEST_TAG_RECENT);
    }

    @Override
    @NotNull
    Game itemFromJson(@NotNull JSONObject gameJson, @NotNull Game game)
            throws JSONException {
        parseEssentialGameInfoFromJson(gameJson, game);

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

        // reviews
        if (! gameJson.isNull(REVIEWS)) {
            JSONPropertyIterator<Integer> idIterator
                    = new JSONPropertyIterator<Integer>(gameJson.getJSONArray(REVIEWS), ID);
            while (idIterator.hasNext()) {
                Review review = new Review();
                review.setGiantBombId(idIterator.next());
                game.addReview(review);
            }
        }

        return game;
    }

    @Override
    @NotNull
    List<Game> itemListFromJson(@NotNull JSONArray jsonArray) {
        Log.i(TAG, "Got " + jsonArray.length() + " games");

        JSONArrayIterator jsonIterator = new JSONArrayIterator(jsonArray);
        List<Game> gameList = new GameList(jsonArray.length());
        while (jsonIterator.hasNext()) {
            JSONObject jsonObject = jsonIterator.next();
            if (jsonObject != null) {
                try {
                    gameList.add(itemFromJson(jsonObject, new Game()));
                } catch (JSONException e) {
                    Log.e(TAG, Log.getStackTraceString(e));
                }
            }
        }

        Log.i(TAG, gameList.size() + " games parsed: " + gameList);
        return gameList;
    }

    void parseEssentialGameInfoFromJson(JSONObject gameJson, Game game)
            throws JSONException {
        // platforms
        JSONArrayIterator platformsIterator = new JSONArrayIterator(gameJson.getJSONArray(PLATFORMS));
        while (platformsIterator.hasNext()) {
            JSONObject platformJson = platformsIterator.next();
            if (platformJson != null) {
                game.addPlatform(mPlatformsApi.itemFromJson(platformJson, new Platform()));
            }
        }

        // essentials
        game.giantBombId = gameJson.getInt(ID);
        game.name = gameJson.getString(NAME);
        JSONObject imagesJson = gameJson.optJSONObject(IMAGE_URLS);
        if (imagesJson != null) {
            game.posterUrl = imagesJson.getString(POSTER_URL);
            game.smallPosterUrl = imagesJson.getString(SMALL_POSTER_URL);
        }
        game.blurb = gameJson.getString(DECK);
        game.releaseDate = parseReleaseDateFromJson(gameJson);
    }

    private ReleaseDate parseReleaseDateFromJson(JSONObject gameJson) {
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

        /**
         * @param uri               the URL to query
         * @param listener          handler to invoke if request succeeds
         * @param errorListener     handler to invoke if request fails
         */
        public GameJsonRequest(Uri uri, Response.Listener<Game> listener,
                               Response.ErrorListener errorListener) {
            super(Method.GET, uri.toString(), null, listener, errorListener);
        }

        @Override
        protected Response<Game> parseNetworkResponse(NetworkResponse response) {
            try {
                String jsonString = new String(response.data, HttpHeaderParser.parseCharset(response.headers));
                Game game = itemFromJson(new JSONObject(jsonString).getJSONObject(RESULTS), new Game());
                return Response.success(game, HttpHeaderParser.parseCacheHeaders(response));
            } catch (UnsupportedEncodingException e) {
                return Response.error(new ParseError(e));
            } catch (JSONException je) {
                return Response.error(new ParseError(je));
            }
        }

    }

    /**
     * A JSON request for retrieving a {@link GameList} from a given URL.
     */
    private class GameListJsonRequest extends JsonRequest<GameList> {

        /**
         * @param uri               the URL to query
         * @param listener          handler to invoke if request succeeds
         * @param errorListener     handler to invoke if request fails
         */
        public GameListJsonRequest(Uri uri, Response.Listener<GameList> listener,
                                   Response.ErrorListener errorListener) {
            super(Method.GET, uri.toString(), null, listener, errorListener);
        }

        @Override
        protected Response<GameList> parseNetworkResponse(NetworkResponse response) {
            try {
                String jsonString = new String(response.data, HttpHeaderParser.parseCharset(response.headers));
                GameList games = (GameList) itemListFromJson(new JSONObject(jsonString).getJSONArray(RESULTS));

                return Response.success(games, HttpHeaderParser.parseCacheHeaders(response));
            } catch (UnsupportedEncodingException e) {
                return Response.error(new ParseError(e));
            } catch (JSONException je) {
                return Response.error(new ParseError(je));
            }
        }

    }

}
