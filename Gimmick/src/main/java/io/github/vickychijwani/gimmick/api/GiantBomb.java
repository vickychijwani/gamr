package io.github.vickychijwani.gimmick.api;

import android.text.TextUtils;
import android.util.Log;

import com.android.volley.Response;
import com.android.volley.toolbox.JsonObjectRequest;

import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import io.github.vickychijwani.gimmick.GamrApplication;
import io.github.vickychijwani.gimmick.item.Platform;
import io.github.vickychijwani.gimmick.item.ReleaseDate;
import io.github.vickychijwani.gimmick.item.SearchResult;

//import org.jetbrains.annotations.NotNull;

public class GiantBomb {

    private static final String RESULTS = "results";
    private static final String ID = "id";
    private static final String NAME = "name";
    private static final String PLATFORMS = "platforms";
    private static final String IMAGE_URLS = "image";
    private static final String POSTER_URL = "thumb_url";
    private static final String DECK = "deck";
    private static final String API_DETAIL_URL = "api_detail_url";
    private static final String REVIEW_COUNT = "number_of_user_reviews";
    private static final String ORIGINAL_RELEASE_DATE = "original_release_date";
    private static final String EXPECTED_RELEASE_YEAR = "expected_release_year";
    private static final String EXPECTED_RELEASE_QUARTER = "expected_release_quarter";
    private static final String EXPECTED_RELEASE_MONTH = "expected_release_month";
    private static final String EXPECTED_RELEASE_DAY = "expected_release_day";

    private static final String SORT_ORDER_ASC = "asc";
    private static final String SORT_ORDER_DESC = "desc";
    private static final SortOption SORT_BY_MOST_REVIEWS = new SortOption(REVIEW_COUNT, SORT_ORDER_DESC);
    private static final SortOption SORT_BY_LATEST_RELEASES = new SortOption(ORIGINAL_RELEASE_DATE, SORT_ORDER_DESC);

    private static final String TAG = "GiantBomb";

    public static void searchGames(String query, Response.Listener<JSONObject> successHandler,
                                   Response.ErrorListener errorHandler) {
        Log.d(TAG, "Searching for \"" + query+ "\"...");

        String url = new URLBuilder()
                .setResource("games")
                .addParam("filter", "name:" + query)
                .setSortOrder(SORT_BY_LATEST_RELEASES, SORT_BY_MOST_REVIEWS)
                .setFieldList(ID, NAME, PLATFORMS, IMAGE_URLS, DECK, API_DETAIL_URL,
                        ORIGINAL_RELEASE_DATE, EXPECTED_RELEASE_YEAR, EXPECTED_RELEASE_QUARTER,
                        EXPECTED_RELEASE_MONTH, EXPECTED_RELEASE_DAY)
                .toString();

        JsonObjectRequest req = new JsonObjectRequest(url, null, successHandler, errorHandler);
        GamrApplication.getInstance().addToRequestQueue(req);
    }

    @NotNull
    public static List<SearchResult> buildSearchResultsFromJson(@NotNull JSONObject resultsJson) {
        JSONArray resultsArray;
        try {
            resultsArray = resultsJson.getJSONArray(RESULTS);
        } catch (JSONException e) {
            e.printStackTrace();
            return new ArrayList<SearchResult>();
        }
        Log.d(TAG, "Got " + resultsArray.length() + " search results");

        List<SearchResult> searchResults = new ArrayList<SearchResult>(resultsArray.length());
        for (int i = 0; i < resultsArray.length(); ++i) {
            try {
                SearchResult result = new SearchResult();
                JSONObject resultJson = resultsArray.getJSONObject(i);
                JSONArray platformsJson = resultJson.getJSONArray(PLATFORMS);
                for (int j = 0; j < platformsJson.length(); ++j) {
                    String platformName = platformsJson.getJSONObject(j).getString(NAME);
                    try {
                        result.addPlatform(Platform.fromString(platformName));
                    } catch (IllegalArgumentException e) {
                        Log.d(TAG, "Ignoring '" + platformName + "' platform");
                    }
                }

                if (result.platforms.size() == 0)
                    continue;

                result.giantBombId = resultJson.getInt(ID);
                result.name = resultJson.getString(NAME);
                result.giantBombUrl = resultJson.getString(API_DETAIL_URL);
                result.posterUrl = resultJson.getJSONObject(IMAGE_URLS).getString(POSTER_URL);
                result.blurb = resultJson.getString(DECK);
                result.releaseDate = parseReleaseDateFromJson(resultJson);

                searchResults.add(result);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        Log.d(TAG, searchResults.size() + " results parsed: " + searchResults);
        return searchResults;
    }

    private static ReleaseDate parseReleaseDateFromJson(JSONObject resultJson) {
        ReleaseDate releaseDate = ReleaseDate.INVALID;
        String originalReleaseDate = resultJson.optString(ORIGINAL_RELEASE_DATE);
        short expectedReleaseYear = (short) resultJson.optInt(EXPECTED_RELEASE_YEAR, ReleaseDate.YEAR_INVALID);
        if (originalReleaseDate != null && ! TextUtils.isEmpty(originalReleaseDate) && ! TextUtils.equals(originalReleaseDate, "null")) {
            try {
                releaseDate = new ReleaseDate(originalReleaseDate);
            } catch (ParseException e) {
                e.printStackTrace();
            }
        } else if (expectedReleaseYear != ReleaseDate.YEAR_INVALID) {
            byte expectedReleaseQuarter = (byte) resultJson.optInt(EXPECTED_RELEASE_QUARTER, ReleaseDate.QUARTER_INVALID);
            byte expectedReleaseMonth = (byte) resultJson.optInt(EXPECTED_RELEASE_MONTH, ReleaseDate.MONTH_INVALID);
            byte expectedReleaseDay = (byte) resultJson.optInt(EXPECTED_RELEASE_DAY, ReleaseDate.DAY_INVALID);
            try {
                releaseDate = new ReleaseDate(expectedReleaseDay, expectedReleaseMonth, expectedReleaseQuarter, expectedReleaseYear);
            } catch (IllegalArgumentException e) {
                e.printStackTrace();
            }
        }
        return releaseDate;
    }

    private static class URLBuilder {

        private static final String BASE_URL = "http://www.giantbomb.com/api/";
        private static final String API_KEY = "f389a37a2ab8f1820572098a113ca89ba95de6ef";

        private StringBuilder mBuilder = new StringBuilder(BASE_URL);

        public URLBuilder setResource(String resource) {
            mBuilder.append(resource);
            mBuilder.append("?api_key=");
            mBuilder.append(API_KEY);
            mBuilder.append("&format=json");
            return this;
        }

        public URLBuilder setFieldList(String... fieldList) {
            String fieldString = TextUtils.join(",", fieldList);
            return addParam("field_list", fieldString);
        }

        public URLBuilder setSortOrder(SortOption... sortOptionList) {
            List<SortOption> sortOptions = Arrays.asList(sortOptionList);
            Collections.reverse(sortOptions);   // the Giant Bomb API takes sort options in reverse order, no idea why
            String sortOrderString = TextUtils.join(",", sortOptions);
            return addParam("sort", sortOrderString);
        }

        public URLBuilder addParam(String field, String value) {
            try {
                mBuilder.append("&");
                mBuilder.append(field);
                mBuilder.append("=");
                mBuilder.append(URLEncoder.encode(value, "UTF-8"));
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
            return this;
        }

        @Override
        public String toString() {
            return mBuilder.toString();
        }

    }

    private static class SortOption {
        public final String field;
        public final String order;

        public SortOption(String field, String order) {
            this.field = field;
            this.order = order;
        }

        @Override
        public String toString() {
            return field + ":" + order;
        }
    }

}
