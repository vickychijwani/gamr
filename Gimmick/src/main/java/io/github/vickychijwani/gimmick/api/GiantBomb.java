package io.github.vickychijwani.gimmick.api;

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
import java.net.URLEncoder;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ExecutionException;

import io.github.vickychijwani.gimmick.item.Platform;
import io.github.vickychijwani.gimmick.item.ReleaseDate;
import io.github.vickychijwani.gimmick.item.SearchResult;

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
    private static final String DECK = "deck";
    private static final String API_DETAIL_URL = "api_detail_url";
    private static final String REVIEW_COUNT = "number_of_user_reviews";
    private static final String ORIGINAL_RELEASE_DATE = "original_release_date";
    private static final String EXPECTED_RELEASE_YEAR = "expected_release_year";
    private static final String EXPECTED_RELEASE_QUARTER = "expected_release_quarter";
    private static final String EXPECTED_RELEASE_MONTH = "expected_release_month";
    private static final String EXPECTED_RELEASE_DAY = "expected_release_day";

    private static final String GENRES = "genres";
    private static final String FRANCHISES = "franchises";

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
    public static RequestTag searchGames(@NotNull String query, Response.Listener<List<SearchResult>> successHandler,
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

        GameListJsonRequest req = new GameListJsonRequest(url, new SearchResult.LatestFirstComparator(),
                successHandler, errorHandler);
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
    public static RequestTag fetchUpcomingGames(final Response.Listener<List<SearchResult>> successHandler,
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
        Response.Listener<List<SearchResult>> successHandlerWrapper = new Response.Listener<List<SearchResult>>() {
            @Override
            public void onResponse(List<SearchResult> games) {
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

        GameListJsonRequest req = new GameListJsonRequest(url, new SearchResult.EarliestFirstComparator(),
                successHandlerWrapper, errorHandler);
        return NetworkRequestQueue.add(req, RequestTag.GIANTBOMB_UPCOMING);
    }

    /**
     * Fetch a game's details in a <i>synchronous</i> manner from the given URL.
     *
     * NOTE: never call this from the UI thread!
     *
     * @return  the requested {@link SearchResult}
     */
    @Nullable
    public static SearchResult fetchGame(@NotNull String giantBombUrl) {
        Log.i(TAG, "Fetching game info from " + giantBombUrl);

        String url = new URLBuilder(giantBombUrl)
                .setFieldList(ID, NAME, PLATFORMS, IMAGE_URLS, DECK, API_DETAIL_URL,
                        ORIGINAL_RELEASE_DATE, EXPECTED_RELEASE_YEAR, EXPECTED_RELEASE_QUARTER,
                        EXPECTED_RELEASE_MONTH, EXPECTED_RELEASE_DAY,
                        GENRES, FRANCHISES)
                .toString();

        RequestFuture<SearchResult> future = RequestFuture.newFuture();
        GameJsonRequest req = new GameJsonRequest(url, future, future);
        NetworkRequestQueue.add(req);

        try {
            return future.get();
        } catch (InterruptedException e) {
            Log.e(TAG, Log.getStackTraceString(e));
        } catch (ExecutionException e) {
            Log.e(TAG, Log.getStackTraceString(e));
        }

        return null;
    }

    @Nullable
    private static SearchResult buildGameFromJson(@NotNull JSONObject resultJsonWrapper) {
        try {
            JSONObject resultJson = resultJsonWrapper.getJSONObject(RESULTS);
            SearchResult result = new SearchResult();
            JSONArrayNameIterator nameIterator;

            if (! parseEssentialGameInfoFromJson(resultJson, result)) {
                return null;
            }

            // genres
            if (! resultJson.isNull(GENRES)) {
                nameIterator = new JSONArrayNameIterator(resultJson.getJSONArray(GENRES));
                while (nameIterator.hasNext()) {
                    result.addGenre(nameIterator.next());
                }
            }

            // franchises
            if (! resultJson.isNull(FRANCHISES)) {
                nameIterator = new JSONArrayNameIterator(resultJson.getJSONArray(FRANCHISES));
                while (nameIterator.hasNext()) {
                    result.addFranchise(nameIterator.next());
                }
            }

            return result;
        } catch (JSONException e) {
            Log.e(TAG, Log.getStackTraceString(e));
        }
        return null;
    }

    @NotNull
    private static List<SearchResult> buildGameListFromJson(@NotNull JSONObject resultsJsonWrapper) {
        JSONArray resultsArray;
        try {
            resultsArray = resultsJsonWrapper.getJSONArray(RESULTS);
        } catch (JSONException e) {
            Log.e(TAG, Log.getStackTraceString(e));
            return new ArrayList<SearchResult>();
        }
        Log.d(TAG, "Got " + resultsArray.length() + " search results");

        List<SearchResult> games = new ArrayList<SearchResult>();
        for (int i = 0; i < resultsArray.length(); ++i) {
            try {
                SearchResult result = new SearchResult();
                JSONObject resultJson = resultsArray.getJSONObject(i);

                if (! parseEssentialGameInfoFromJson(resultJson, result)) {
                    continue;
                }

                games.add(result);
            } catch (JSONException e) {
                Log.e(TAG, Log.getStackTraceString(e));
            }
        }

        Log.d(TAG, games.size() + " results parsed: " + games);
        return games;
    }

    private static boolean parseEssentialGameInfoFromJson(JSONObject resultJson, SearchResult game)
            throws JSONException {
        JSONArrayNameIterator nameIterator;

        // platforms
        nameIterator = new JSONArrayNameIterator(resultJson.getJSONArray(PLATFORMS));
        while (nameIterator.hasNext()) {
            String platformName = nameIterator.next();
            try {
                game.addPlatform(Platform.fromName(platformName));
            } catch (IllegalArgumentException e) {
                Log.d(TAG, "Ignoring '" + platformName + "' platform");
            }
        }

        if (game.platforms.size() == 0)
            return false;

        // essentials
        game.giantBombId = resultJson.getInt(ID);
        game.name = resultJson.getString(NAME);
        game.giantBombUrl = resultJson.getString(API_DETAIL_URL);
        game.posterUrl = resultJson.getJSONObject(IMAGE_URLS).getString(POSTER_URL);
        game.smallPosterUrl = resultJson.getJSONObject(IMAGE_URLS).getString(SMALL_POSTER_URL);
        game.blurb = resultJson.getString(DECK);
        game.releaseDate = parseReleaseDateFromJson(resultJson);
        return true;
    }

    private static ReleaseDate parseReleaseDateFromJson(JSONObject resultJson) {
        ReleaseDate releaseDate = ReleaseDate.INVALID;
        String originalReleaseDate = resultJson.optString(ORIGINAL_RELEASE_DATE);
        short expectedReleaseYear = (short) resultJson.optInt(EXPECTED_RELEASE_YEAR, ReleaseDate.YEAR_INVALID);
        if (originalReleaseDate != null && ! TextUtils.isEmpty(originalReleaseDate) && ! TextUtils.equals(originalReleaseDate, "null")) {
            try {
                releaseDate = new ReleaseDate(originalReleaseDate);
            } catch (ParseException e) {
                Log.e(TAG, Log.getStackTraceString(e));
            }
        } else if (expectedReleaseYear != ReleaseDate.YEAR_INVALID) {
            byte expectedReleaseQuarter = (byte) resultJson.optInt(EXPECTED_RELEASE_QUARTER, ReleaseDate.QUARTER_INVALID);
            byte expectedReleaseMonth = (byte) resultJson.optInt(EXPECTED_RELEASE_MONTH, ReleaseDate.MONTH_INVALID);
            byte expectedReleaseDay = (byte) resultJson.optInt(EXPECTED_RELEASE_DAY, ReleaseDate.DAY_INVALID);

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

    /**
     * Utility class to iterate over a JSONArray of objects with a "name" field
     */
    private static class JSONArrayNameIterator implements Iterator<String> {
        private JSONArray mJsonArray;
        private int mPosition = 0;

        public JSONArrayNameIterator(JSONArray jsonArray) throws JSONException, IllegalArgumentException {
            if (jsonArray.length() > 0 && ! jsonArray.getJSONObject(0).has(NAME)) {
                throw new IllegalArgumentException("objects in JSONArray must have \"name\" field for iteration");
            }
            mJsonArray = jsonArray;
        }

        @Override
        public boolean hasNext() {
            return mPosition < mJsonArray.length();
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException("iterator is read-only");
        }

        @Override
        public String next() {
            try {
                return mJsonArray.getJSONObject(mPosition++).getString(NAME);
            } catch (JSONException e) {
                Log.e(TAG, Log.getStackTraceString(e));
            }
            return null;
        }
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
     * A JSON request for retrieving a <code>{@link java.util.List}<{@link SearchResult}></code>
     * from a given URL.
     */
    private static class GameListJsonRequest extends JsonRequest<List<SearchResult>> {

        private final Comparator<SearchResult> mSortComparator;

        /**
         * @param url               the URL to query
         * @param sortComparator    games will be sorted using this {@link Comparator}. If this is
         *                          {@code null}, no sorting will be performed.
         * @param listener          handler to invoke if request succeeds
         * @param errorListener     handler to invoke if request fails
         */
        public GameListJsonRequest(String url, @Nullable Comparator<SearchResult> sortComparator,
                                   Response.Listener<List<SearchResult>> listener,
                                   Response.ErrorListener errorListener) {
            super(Method.GET, url, null, listener, errorListener);
            mSortComparator = sortComparator;
        }

        @Override
        protected Response<List<SearchResult>> parseNetworkResponse(NetworkResponse response) {
            try {
                String jsonString = new String(response.data, HttpHeaderParser.parseCharset(response.headers));
                List<SearchResult> games = buildGameListFromJson(new JSONObject(jsonString));

                if (mSortComparator != null) {
                    Collections.sort(games, mSortComparator);
                }

                return Response.success(games, HttpHeaderParser.parseCacheHeaders(response));
            } catch (UnsupportedEncodingException e) {
                return Response.error(new ParseError(e));
            } catch (JSONException je) {
                return Response.error(new ParseError(je));
            }
        }

    }

    /**
     * A JSON request for retrieving a {@link SearchResult} from a given URL.
     */
    private static class GameJsonRequest extends JsonRequest<SearchResult> {

        public GameJsonRequest(String url, Response.Listener<SearchResult> listener,
                                   Response.ErrorListener errorListener) {
            super(Method.GET, url, null, listener, errorListener);
        }

        @Override
        protected Response<SearchResult> parseNetworkResponse(NetworkResponse response) {
            try {
                String jsonString = new String(response.data, HttpHeaderParser.parseCharset(response.headers));
                SearchResult game = buildGameFromJson(new JSONObject(jsonString));
                return Response.success(game, HttpHeaderParser.parseCacheHeaders(response));
            } catch (UnsupportedEncodingException e) {
                return Response.error(new ParseError(e));
            } catch (JSONException je) {
                return Response.error(new ParseError(je));
            }
        }

    }

}
