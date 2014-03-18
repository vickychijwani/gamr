package io.github.vickychijwani.giantbomb.api;

import com.android.volley.Request;

import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

import io.github.vickychijwani.giantbomb.api.URLFactory.SortParam;
import io.github.vickychijwani.giantbomb.item.ResourceType;
import io.github.vickychijwani.network.volley.RequestTag;
import io.github.vickychijwani.network.volley.VolleyRequestQueue;

abstract class BaseAPI<T> {

    static final String TAG = "GiantBombAPI";

    // API fields
    static final String RESULTS = "results";
    static final String ID = "id";
    static final String NAME = "name";
    static final String IMAGE_URLS = "image";
    static final String POSTER_URL = "thumb_url";
    static final String SMALL_POSTER_URL = "small_url";
    static final String SCREEN_URL = "screen_url";
    static final String DECK = "deck";
    static final String ALIASES = "aliases";
    static final String SITE_DETAIL_URL = "site_detail_url";

    // fields for games / releases
    static final String PLATFORMS = "platforms";
    static final String REVIEW_COUNT = "number_of_user_reviews";
    static final String ORIGINAL_RELEASE_DATE = "original_release_date";
    static final String EXPECTED_RELEASE_YEAR = "expected_release_year";
    static final String EXPECTED_RELEASE_QUARTER = "expected_release_quarter";
    static final String EXPECTED_RELEASE_MONTH = "expected_release_month";
    static final String EXPECTED_RELEASE_DAY = "expected_release_day";
    static final String GENRES = "genres";
    static final String FRANCHISES = "franchises";
    static final String VIDEOS = "videos";
    static final String REVIEWS = "reviews";

    // fields for platform
    static final String ABBREVIATION = "abbreviation";

    // fields for videos
    static final String LOW_URL = "low_url";
    static final String HIGH_URL = "high_url";
    static final String LENGTH_SECONDS = "length_seconds";
    static final String YOUTUBE_ID = "youtube_id";
    static final String VIDEO_TYPE = "video_type";
    static final String PUBLISH_DATE = "publish_date";
    static final String USER = "user";

    // fields for resource types
    static final String DETAIL_RESOURCE_NAME = "detail_resource_name";
    static final String LIST_RESOURCE_NAME = "list_resource_name";

    // fields for reviews
    static final String REVIEWER = "reviewer";
    static final String SCORE = "score";

    // sorting parameters
    static final SortParam SORT_BY_MOST_REVIEWS = new SortParam(REVIEW_COUNT, SortParam.DESC);
    static final SortParam SORT_BY_LATEST_RELEASES = new SortParam(ORIGINAL_RELEASE_DATE, SortParam.DESC);

    // request tags (used for cancelling pending requests when they are not needed)
    static final RequestTag REQUEST_TAG_SEARCH = RequestTag.generate();
    static final RequestTag REQUEST_TAG_UPCOMING = RequestTag.generate();
    static final RequestTag REQUEST_TAG_RECENT = RequestTag.generate();


    // instance fields
    private final ResourceType mResourceType;
    private final VolleyRequestQueue mRequestQueue;
    private final URLFactory mUrlfactory;

    // methods
    protected BaseAPI(ResourceType resourceType, VolleyRequestQueue requestQueue,
                      URLFactory urlFactory) {
        mResourceType = resourceType;
        mRequestQueue = requestQueue;
        mUrlfactory = urlFactory;
    }

    protected final URLFactory.Builder newDetailResourceURL(int resourceId) {
        return mUrlfactory.newURL()
                .setResource(mResourceType.getSingularName(), mResourceType.getId(), resourceId);
    }

    protected final URLFactory.Builder newListResourceURL() {
        return mUrlfactory.newURL()
                .setResource(mResourceType.getPluralName());
    }

    final RequestTag enqueueRequest(Request request) {
        return mRequestQueue.add(request);
    }

    final RequestTag enqueueRequest(Request request, RequestTag tag) {
        return mRequestQueue.add(request, tag);
    }

    @NotNull
    abstract T itemFromJson(@NotNull JSONObject json, @NotNull T item)
            throws JSONException;

    @NotNull
    List<T> itemListFromJson(@NotNull JSONArray json) {
        throw new UnsupportedOperationException(mResourceType.getPluralName() + " API does not support this operation");
    }

}
