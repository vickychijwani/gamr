package io.github.vickychijwani.giantbomb.api;

import org.jetbrains.annotations.NotNull;
import org.json.JSONObject;

import io.github.vickychijwani.giantbomb.api.URLBuilder.SortParam;
import io.github.vickychijwani.network.volley.RequestTag;

interface Resource<T> {

    static final String TAG = "GiantBomb";

    // API fields
    static final String RESULTS = "results";
    static final String ID = "id";
    static final String NAME = "name";
    static final String IMAGE_URLS = "image";
    static final String POSTER_URL = "thumb_url";
    static final String SMALL_POSTER_URL = "small_url";
    static final String SCREEN_URL = "screen_url";
    static final String DECK = "deck";
    static final String API_DETAIL_URL = "api_detail_url";
    static final String ALIASES = "aliases";

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

    // sorting parameters
    static final SortParam SORT_BY_MOST_REVIEWS = new SortParam(REVIEW_COUNT, SortParam.DESC);
    static final SortParam SORT_BY_LATEST_RELEASES = new SortParam(ORIGINAL_RELEASE_DATE, SortParam.DESC);

    // request tags (used for cancelling pending requests when they are not needed)
    static final RequestTag REQUEST_TAG_SEARCH = RequestTag.generate();
    static final RequestTag REQUEST_TAG_UPCOMING = RequestTag.generate();
    static final RequestTag REQUEST_TAG_RECENT = RequestTag.generate();


    // methods
    public String getResourceName();

    @NotNull
    public T itemFromJson(@NotNull JSONObject json, @NotNull T item)
            throws GiantBombException;

}
