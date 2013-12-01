package io.github.vickychijwani.gimmick.giantbomb;

import android.text.TextUtils;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import io.github.vickychijwani.gimmick.data.Platform;
import io.github.vickychijwani.gimmick.data.SearchResult;
import io.github.vickychijwani.gimmick.utility.NetworkUtils;

public class GiantBombAPI {

    private static final String FIELD_RESULTS = "results";
    private static final String FIELD_ID = "id";
    private static final String FIELD_NAME = "name";
    private static final String FIELD_PLATFORMS = "platforms";
    private static final String FIELD_API_DETAIL_URL = "api_detail_url";
    private static final String FIELD_IMAGE_URLS = "image";
    private static final String FIELD_POSTER_URL = "thumb_url";
    private static final String FIELD_DESC_URL = "deck";
    private static final String FIELD_REVIEW_COUNT = "number_of_user_reviews";
    private static final String FIELD_ORIGINAL_RELEASE_DATE = "original_release_date";

    private static final String SORT_ORDER_ASC = "asc";
    private static final String SORT_ORDER_DESC = "desc";
    private static final SortOption SORT_BY_MOST_REVIEWS = new SortOption(FIELD_REVIEW_COUNT, SORT_ORDER_DESC);
    private static final SortOption SORT_BY_LATEST_RELEASES = new SortOption(FIELD_ORIGINAL_RELEASE_DATE, SORT_ORDER_DESC);

    private static final String TAG = "GiantBombAPI";

    public static List<SearchResult> searchGames(String query) {
        String url = new URLBuilder()
                .setResource("games")
                .addParam("filter", "name:" + query)
                .setSortOrder(SORT_BY_LATEST_RELEASES, SORT_BY_MOST_REVIEWS)
                .setFieldList(FIELD_ID, FIELD_NAME, FIELD_PLATFORMS, FIELD_IMAGE_URLS,
                        FIELD_DESC_URL, FIELD_API_DETAIL_URL)
                .toString();

        JSONObject resultsJson = NetworkUtils.getJsonFromUrl(url);
        return buildSearchResultsFromJson(resultsJson);
    }

    private static List<SearchResult> buildSearchResultsFromJson(JSONObject resultsJson) {
        JSONArray resultsArray;
        try {
            resultsArray = resultsJson.getJSONArray(FIELD_RESULTS);
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
        Log.d(TAG, "Got " + resultsArray.length() + " search results");

        List<SearchResult> searchResults = new ArrayList<SearchResult>(resultsArray.length());
        for (int i = 0; i < resultsArray.length(); ++i) {
            try {
                SearchResult result = new SearchResult();
                JSONObject resultJson = resultsArray.getJSONObject(i);
                JSONArray platformsJson = resultJson.getJSONArray(FIELD_PLATFORMS);
                for (int j = 0; j < platformsJson.length(); ++j) {
                    String platformName = platformsJson.getJSONObject(j).getString(FIELD_NAME);
                    try {
                        result.addPlatform(Platform.fromString(platformName));
                    } catch (IllegalArgumentException e) {
                        Log.d(TAG, "Ignoring '" + platformName + "' platform");
                    }
                }

                if (result.platforms.size() == 0)
                    continue;

                result.name = resultJson.getString(FIELD_NAME);
                result.giantBombUrl = resultJson.getString(FIELD_API_DETAIL_URL);
                result.posterUrl = resultJson.getJSONObject(FIELD_IMAGE_URLS).getString(FIELD_POSTER_URL);
                result.description = resultJson.getString(FIELD_DESC_URL);

                searchResults.add(result);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        Log.d(TAG, searchResults.size() + " results parsed: " + searchResults);
        return searchResults;
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
