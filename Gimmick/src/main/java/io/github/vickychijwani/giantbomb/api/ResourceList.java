package io.github.vickychijwani.giantbomb.api;

import android.net.Uri;
import android.util.Log;

import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.RequestFuture;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Collections;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.concurrent.ExecutionException;

import io.github.vickychijwani.network.volley.RequestTag;

public class ResourceList<T> {

    private static final String TAG = "ResourceList";

    private static final String RESULTS = "results";
    private static final String STATUS_CODE = "status_code";
    private static final String ERROR = "error";
    private static final String NUMBER_OF_TOTAL_RESULTS = "number_of_total_results";
    private static final String NUMBER_OF_PAGE_RESULTS = "number_of_page_results";
    private static final String LIMIT = "limit";
    private static final String OFFSET = "offset";

    private StatusCode mStatusCode;
    private String mStatusString;
    private int mTotalResults;
    private int mPageResults;
    private int mLimit;
    private int mOffset;
    private final BaseAPI<T> mApi;
    private final Uri mBaseUri;
    private final RequestTag mRequestTag;

    ResourceList(@NotNull BaseAPI<T> api, @NotNull Uri baseUri,
                        @Nullable RequestTag requestTag) {
        mStatusCode = StatusCode.OK;
        mStatusString = "START";
        mOffset = 0;
        mLimit = 0;
        mPageResults = 0;
        mTotalResults = 1;
        mApi = api;
        mBaseUri = baseUri;
        mRequestTag = requestTag;
    }

    ResourceList(BaseAPI<T> api, Uri baseUri) {
        this(api, baseUri, null);
    }

    private void updatePageMetadata(@NotNull JSONObject json) throws JSONException {
        mStatusCode = StatusCode.fromCodeNumber(json.getInt(STATUS_CODE));
        mStatusString = json.getString(ERROR);
        mTotalResults = json.getInt(NUMBER_OF_TOTAL_RESULTS);
        mPageResults = json.getInt(NUMBER_OF_PAGE_RESULTS);
        mLimit = json.getInt(LIMIT);
        mOffset = json.getInt(OFFSET);
    }

    public boolean hasNextPage() {
        return mOffset + mPageResults < mTotalResults;
    }

    /**
     * Get the next page of results.
     *
     * NOTE: this is a blocking call!
     *
     * @return a list of results from the next page
     */
    public List<T> getNextPage() {
        if (! hasNextPage()) {
            throw new NoSuchElementException();
        }

        Uri uri = mBaseUri
                .buildUpon()
                .appendQueryParameter(OFFSET, String.valueOf(mOffset + mLimit))
                .build();
        assert uri != null;

        RequestFuture<JSONObject> future = RequestFuture.newFuture();
        JsonObjectRequest req = new JsonObjectRequest(uri.toString(), null, future, future);
        mApi.enqueueRequest(req, mRequestTag);

        try {
            JSONObject json = future.get();
            updatePageMetadata(json);
            return mApi.itemListFromJson(json.getJSONArray(RESULTS));
        } catch (InterruptedException e) {
            Log.e(TAG, Log.getStackTraceString(e));
        } catch (ExecutionException e) {
            Log.e(TAG, Log.getStackTraceString(e));
        } catch (JSONException e) {
            Log.e(TAG, Log.getStackTraceString(e));
        }
        return Collections.emptyList();
    }

    enum StatusCode {
        OK(1),
        INVALID_API_KEY(100),
        OBJECT_NOT_FOUND(101),
        ERROR_IN_URL_FORMAT(102),
        MISSING_JSON_CALLBACK(103),
        FILTER_ERROR(104),
        SUBSCRIBER_ONLY(105)
        ;

        private final int mCodeNumber;

        private StatusCode(int codeNumber) {
            mCodeNumber = codeNumber;
        }

        private static StatusCode fromCodeNumber(int codeNumber) {
            for (StatusCode statusCode : StatusCode.values()) {
                if (statusCode.mCodeNumber == codeNumber) {
                    return statusCode;
                }
            }
            throw new IllegalArgumentException("invalid status code number");
        }
    }

}
