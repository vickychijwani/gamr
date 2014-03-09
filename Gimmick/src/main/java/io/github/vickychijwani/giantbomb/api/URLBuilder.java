package io.github.vickychijwani.giantbomb.api;

import android.text.TextUtils;
import android.util.Log;

import com.squareup.otto.Subscribe;

import org.jetbrains.annotations.NotNull;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.github.vickychijwani.giantbomb.item.ResourceType;
import io.github.vickychijwani.gimmick.utility.EventBus;

class URLBuilder {

    private static final String TAG = "GiantBomb";
    private static final String BASE_URL = "http://www.giantbomb.com/api/";
    private static String API_KEY = null;

    private StringBuilder mBuilder;
    private String mResource = null;
    private int mResourceId = -1;
    private Collection<String> mParams = new ArrayList<String>();

    private static final Map<String, Integer> mResourceTypeToIdMap = new HashMap<String, Integer>();

    public static void initialize(@NotNull String apiKey) {
        API_KEY = apiKey;
        EventBus.getInstance().register(new Object() {
            @Subscribe
            public void onResourceTypesChanged(ResourceTypesChangedEvent event) {
                mResourceTypeToIdMap.clear();
                for (ResourceType type : event.resourceTypes) {
                    mResourceTypeToIdMap.put(type.getSingularName(), type.getId());
                }
            }
        });
    }

    private URLBuilder() { }

    public static URLBuilder newInstance() {
        if (API_KEY == null) {
            throw new IllegalStateException("API key not set");
        }
        URLBuilder urlBuilder = new URLBuilder();
        urlBuilder.mBuilder = new StringBuilder(BASE_URL);
        return urlBuilder;
    }

    public URLBuilder setResource(String resource) {
        mResource = resource;
        return this;
    }

    public URLBuilder setResource(String resource, int resourceId) {
        mResource = resource;
        mResourceId = resourceId;
        return this;
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

    public URLBuilder addParam(String key, String value) {
        try {
            mParams.add(key + "=" + URLEncoder.encode(value, "UTF-8"));
        } catch (UnsupportedEncodingException e) {
            Log.e(TAG, Log.getStackTraceString(e));
        }
        return this;
    }

    public String build()
            throws IllegalStateException {
        if (mResource == null) {
            throw new IllegalStateException("resource not set");
        }
        mBuilder.append(mResource)
                .append("/");

        if (mResourceId >= 0) {
            Integer resourceTypeId = mResourceTypeToIdMap.get(mResource);
            if (resourceTypeId == null) {
                throw new IllegalStateException("invalid resource type: " + mResource);
            }
            mBuilder.append(resourceTypeId)
                    .append("-")
                    .append(mResourceId)
                    .append("/");
        }

        mBuilder.append("?");

        // add essential parameters
        addParam("api_key", API_KEY);
        addParam("format", "json");

        mBuilder.append(TextUtils.join("&", mParams));

        return mBuilder.toString();
    }


    public static class SortParam {
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

}
