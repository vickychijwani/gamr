package io.github.vickychijwani.gimmick.api;

import android.text.TextUtils;
import android.util.Log;

import com.android.volley.NetworkResponse;
import com.android.volley.ParseError;
import com.android.volley.Response;
import com.android.volley.toolbox.HttpHeaderParser;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.JsonRequest;
import com.android.volley.toolbox.RequestFuture;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.ParseException;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ExecutionException;

import io.github.vickychijwani.gimmick.item.Game;
import io.github.vickychijwani.gimmick.item.GameList;
import io.github.vickychijwani.gimmick.item.Platform;
import io.github.vickychijwani.gimmick.item.ReleaseDate;
import io.github.vickychijwani.gimmick.item.Video;
import io.github.vickychijwani.gimmick.utility.AppUtils;

public class GiantBomb {

    private static final String TAG = "GiantBomb";
    private static final String BASE_URL = "http://www.giantbomb.com/api/";
    private static String API_KEY = null;

    private static final String RESULTS = "results";
    private static final String ID = "id";
    private static final String NAME = "name";
    private static final String PLATFORMS = "platforms";
    private static final String IMAGE_URLS = "image";
    private static final String POSTER_URL = "thumb_url";
    private static final String SMALL_POSTER_URL = "small_url";
    private static final String SCREEN_URL = "screen_url";
    private static final String DECK = "deck";
    private static final String API_DETAIL_URL = "api_detail_url";
    private static final String REVIEW_COUNT = "number_of_user_reviews";
    private static final String ORIGINAL_RELEASE_DATE = "original_release_date";
    private static final String EXPECTED_RELEASE_YEAR = "expected_release_year";
    private static final String EXPECTED_RELEASE_QUARTER = "expected_release_quarter";
    private static final String EXPECTED_RELEASE_MONTH = "expected_release_month";
    private static final String EXPECTED_RELEASE_DAY = "expected_release_day";
    private static final String USER = "user";
    private static final String GENRES = "genres";

    private static final String FRANCHISES = "franchises";
    private static final String VIDEOS = "videos";
    // for videos
    private static final String LOW_URL = "low_url";

    private static final String HIGH_URL = "high_url";
    private static final String LENGTH_SECONDS = "length_seconds";
    private static final String YOUTUBE_ID = "youtube_id";
    private static final String VIDEO_TYPE = "video_type";
    private static final String PUBLISH_DATE = "publish_date";

    private static final SortParam SORT_BY_MOST_REVIEWS = new SortParam(REVIEW_COUNT, SortParam.DESC);
    private static final SortParam SORT_BY_LATEST_RELEASES = new SortParam(ORIGINAL_RELEASE_DATE, SortParam.DESC);

    /**
     * Fire an asynchronous game search.
     *
     * @param query             Search term
     * @param successHandler    handler to invoke if request succeeds
     * @param errorHandler      handler to invoke if request fails
     * @return                  a tag that can be used to cancel any ongoing search requests by
     *                          calling {@link NetworkRequestQueue#cancelPending(RequestTag)}.
     */
    public static RequestTag searchGames(@NotNull String query,
                                         Response.Listener<GameList> successHandler,
                                         Response.ErrorListener errorHandler) {
        Log.i(TAG, "Searching for \"" + query + "\"...");

        String url = new URLBuilder()
                .setResource("games")
                .addParam("filter", NAME + ":" + query)
                .setSortOrder(SORT_BY_LATEST_RELEASES, SORT_BY_MOST_REVIEWS)
                .setFieldList(ID, NAME, PLATFORMS, IMAGE_URLS, DECK, API_DETAIL_URL,
                        ORIGINAL_RELEASE_DATE, EXPECTED_RELEASE_YEAR, EXPECTED_RELEASE_QUARTER,
                        EXPECTED_RELEASE_MONTH, EXPECTED_RELEASE_DAY)
                .toString();

        GameListJsonRequest req = new GameListJsonRequest(url, successHandler, errorHandler);
        return NetworkRequestQueue.add(req, RequestTag.GIANTBOMB_SEARCH);
    }

    /**
     * Fire an asynchronous request to fetch "upcoming" games (i.e., games that will be released in
     * the current year).
     *
     * @param successHandler    handler to invoke if request succeeds
     * @param errorHandler      handler to invoke if request fails
     * @return                  a tag that can be used to cancel any ongoing "upcoming games"
     *                          requests by calling {@link NetworkRequestQueue#cancelPending(RequestTag)}.
     */
    public static RequestTag fetchUpcomingGames(final Response.Listener<GameList> successHandler,
                                          final Response.ErrorListener errorHandler) {
        int currentYear = Calendar.getInstance().get(Calendar.YEAR);

        Log.i(TAG, "Fetching upcoming games for year " + currentYear + "...");

        // TODO what if the current date is 25th Dec? Shouldn't "upcoming" include the next year as well in that case?

        final String url = new URLBuilder()
                .setResource("games")
                .addParam("filter", EXPECTED_RELEASE_YEAR + ":" + currentYear)
                .setFieldList(ID, NAME, PLATFORMS, IMAGE_URLS, DECK, API_DETAIL_URL,
                        EXPECTED_RELEASE_YEAR, EXPECTED_RELEASE_QUARTER,
                        EXPECTED_RELEASE_MONTH, EXPECTED_RELEASE_DAY)
                .toString();

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
        return NetworkRequestQueue.add(req, RequestTag.GIANTBOMB_UPCOMING);
    }

    /**
     * Fetch a game's details in a <i>synchronous</i> manner from the given URL.
     *
     * NOTE: never call this from the UI thread!
     *
     * @param giantBombUrl  API URL
     * @return  the requested {@link io.github.vickychijwani.gimmick.item.Game}
     */
    @Nullable
    public static Game fetchGame(@NotNull String giantBombUrl) {
        Log.i(TAG, "Fetching game info from " + giantBombUrl);

        String url = new URLBuilder(giantBombUrl)
                .setFieldList(ID, NAME, PLATFORMS, IMAGE_URLS, DECK, API_DETAIL_URL,
                        ORIGINAL_RELEASE_DATE, EXPECTED_RELEASE_YEAR, EXPECTED_RELEASE_QUARTER,
                        EXPECTED_RELEASE_MONTH, EXPECTED_RELEASE_DAY,
                        GENRES, FRANCHISES, VIDEOS)
                .toString();

        RequestFuture<Game> future = RequestFuture.newFuture();
        GameJsonRequest req = new GameJsonRequest(url, future, future);
        NetworkRequestQueue.add(req);

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
     * Fetch a game's videos' details in a <i>synchronous</i> manner.
     * <p/>
     * NOTE: never call this from the UI thread!
     *
     * @param game the {@link Game} for which to fetch video data. It must have {@link Video}s with
     *             a valid GiantBomb video ID on each of them.
     * @return the {@link Game} that was passed in, augmented with the requested video data
     */
    @Nullable
    public static Game fetchVideosForGame(@NotNull Game game) {
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

        String url = new URLBuilder()
                .setResource("videos")
                .addParam("filter", ID + ":" + videoIds)
                .toString();

        Log.i(TAG, "Fetching videos from " + url);

        RequestFuture<JSONObject> future = RequestFuture.newFuture();
        JsonObjectRequest req = new JsonObjectRequest(url, null, future, future);
        NetworkRequestQueue.add(req);

        try {
            JSONArray videoJsonArray = future.get().getJSONArray(RESULTS);  // block until request completes
            Log.i(TAG, "Received " + videoJsonArray.length() + " videos");
            JSONArrayIterator videoJsonIterator = new JSONArrayIterator(videoJsonArray);
            videoIterator = game.getVideos();
            while (videoJsonIterator.hasNext() && videoIterator.hasNext()) {
                parseVideoInfoFromJson(videoJsonIterator.next(), videoIterator.next());
            }
            // sometimes GB returns fewer videos than were requested (the error is "Object Not Found"
            // for the missing ones), so we remove those videos
            while (videoIterator.hasNext()) {
                videoIterator.next();
                videoIterator.remove();
            }
        } catch (InterruptedException e) {
            Log.e(TAG, Log.getStackTraceString(e));
        } catch (ExecutionException e) {
            Log.e(TAG, Log.getStackTraceString(e));
        } catch (JSONException e) {
            Log.e(TAG, Log.getStackTraceString(e));
        }

        return game;
    }

    @Nullable
    private static Game buildGameFromJson(@NotNull JSONObject gameJsonWrapper) {
        try {
            JSONObject gameJson = gameJsonWrapper.getJSONObject(RESULTS);
            Game game = new Game();

            if (! parseEssentialGameInfoFromJson(gameJson, game)) {
                return null;
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
        return null;
    }

    @NotNull
    private static GameList buildGameListFromJson(@NotNull JSONObject gamesJsonWrapper) {
        JSONArray gamesArray;
        try {
            gamesArray = gamesJsonWrapper.getJSONArray(RESULTS);
        } catch (JSONException e) {
            Log.e(TAG, Log.getStackTraceString(e));
            return new GameList();
        }
        Log.i(TAG, "Got " + gamesArray.length() + " games");

        GameList games = new GameList();
        for (int i = 0; i < gamesArray.length(); ++i) {
            try {
                Game game = new Game();
                JSONObject gameJson = gamesArray.getJSONObject(i);

                if (! parseEssentialGameInfoFromJson(gameJson, game)) {
                    continue;
                }

                games.add(game);
            } catch (JSONException e) {
                Log.e(TAG, Log.getStackTraceString(e));
            }
        }

        Log.i(TAG, games.size() + " games parsed: " + games);
        return games;
    }

    private static boolean parseEssentialGameInfoFromJson(JSONObject gameJson, Game game)
            throws JSONException {
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
        game.posterUrl = gameJson.getJSONObject(IMAGE_URLS).getString(POSTER_URL);
        game.smallPosterUrl = gameJson.getJSONObject(IMAGE_URLS).getString(SMALL_POSTER_URL);
        game.blurb = gameJson.getString(DECK);
        game.releaseDate = parseReleaseDateFromJson(gameJson);
        return true;
    }

    @NotNull
    private static Video parseVideoInfoFromJson(JSONObject videoJson, Video video) throws JSONException {
        // game id is set automatically when calling Game#addVideo()

        video.setName(videoJson.optString(NAME));
        video.setBlurb(videoJson.optString(DECK));
        video.setGiantBombId(videoJson.getInt(ID));
        video.setGiantBombUrl(videoJson.getString(API_DETAIL_URL));
        video.setLowUrl(videoJson.optString(LOW_URL));
        video.setHighUrl(videoJson.optString(HIGH_URL));
        video.setDuration(videoJson.optInt(LENGTH_SECONDS, -1));
        video.setUser(videoJson.optString(USER));
        video.setType(videoJson.optString(VIDEO_TYPE));
        video.setYoutubeId(videoJson.optString(YOUTUBE_ID));

        // publish date
        try {
            video.setPublishDate(AppUtils.isoDateStringToDate(videoJson.optString(PUBLISH_DATE, AppUtils.getEarliestDateString())));
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

    public static void setApiKey(@NotNull String apiKey) {
        API_KEY = apiKey;
    }

    private static class URLBuilder {

        private StringBuilder mBuilder;

        public URLBuilder() {
            assert API_KEY != null;
            mBuilder = new StringBuilder(BASE_URL);
        }

        public URLBuilder(String url) {
            assert API_KEY != null;
            mBuilder = new StringBuilder(url);
            appendEssentialParams();
        }

        public URLBuilder setResource(String resource) {
            mBuilder.append(resource);
            appendEssentialParams();
            return this;
        }

        private void appendEssentialParams() {
            mBuilder.append("?api_key=")
                    .append(API_KEY)
                    .append("&format=json");
        }

        public URLBuilder setFieldList(String... fieldList) {
            String fieldString = TextUtils.join(",", fieldList);
            return addParam("field_list", fieldString);
        }

        public URLBuilder setSortOrder(SortParam... sortParamList) {
            List<SortParam> sortParams = Arrays.asList(sortParamList);
            Collections.reverse(sortParams);   // the Giant Bomb API takes sort options in reverse order, no idea why
            String sortOrderString = TextUtils.join(",", sortParams);
            return addParam("sort", sortOrderString);
        }

        public URLBuilder addParam(String field, String value) {
            try {
                mBuilder.append("&");
                mBuilder.append(field);
                mBuilder.append("=");
                mBuilder.append(URLEncoder.encode(value, "UTF-8"));
            } catch (UnsupportedEncodingException e) {
                Log.e(TAG, Log.getStackTraceString(e));
            }
            return this;
        }

        @Override
        public String toString() {
            return mBuilder.toString();
        }

    }

    private static class SortParam {
        public static final String ASC = "asc";
        public static final String DESC = "desc";

        public final String field;
        public final String order;

        public SortParam(String field, String order) {
            this.field = field;
            this.order = order;
        }

        @Override
        public String toString() {
            return field + ":" + order;
        }
    }

    /**
     * A JSON request for retrieving a <code>{@link java.util.List}<{@link io.github.vickychijwani.gimmick.item.Game}></code>
     * from a given URL.
     */
    private static class GameListJsonRequest extends JsonRequest<GameList> {

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
                GameList games = buildGameListFromJson(new JSONObject(jsonString));

                return Response.success(games, HttpHeaderParser.parseCacheHeaders(response));
            } catch (UnsupportedEncodingException e) {
                return Response.error(new ParseError(e));
            } catch (JSONException je) {
                return Response.error(new ParseError(je));
            }
        }

    }

    /**
     * A JSON request for retrieving a {@link io.github.vickychijwani.gimmick.item.Game} from a given URL.
     */
    private static class GameJsonRequest extends JsonRequest<Game> {

        public GameJsonRequest(String url, Response.Listener<Game> listener,
                                   Response.ErrorListener errorListener) {
            super(Method.GET, url, null, listener, errorListener);
        }

        @Override
        protected Response<Game> parseNetworkResponse(NetworkResponse response) {
            try {
                String jsonString = new String(response.data, HttpHeaderParser.parseCharset(response.headers));
                Game game = buildGameFromJson(new JSONObject(jsonString));
                return Response.success(game, HttpHeaderParser.parseCacheHeaders(response));
            } catch (UnsupportedEncodingException e) {
                return Response.error(new ParseError(e));
            } catch (JSONException je) {
                return Response.error(new ParseError(je));
            }
        }

    }

}
