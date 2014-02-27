package io.github.vickychijwani.giantbomb.api;

import android.text.TextUtils;
import android.util.Log;

import org.jetbrains.annotations.NotNull;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

class URLBuilder {

    private static final String TAG = "GiantBomb";
    private final String BASE_URL = "http://www.giantbomb.com/api/";
    private static String API_KEY = null;

    private StringBuilder mBuilder;
    private boolean mbAreEssentialParamsAppended;

    public static void setApiKey(@NotNull String apiKey) {
        API_KEY = apiKey;
    }

    public URLBuilder() {
        if (API_KEY == null) {
            throw new IllegalStateException("API key not set");
        }
        mBuilder = new StringBuilder(BASE_URL);
    }

    public URLBuilder(String url) {
        if (API_KEY == null) {
            throw new IllegalStateException("API key not set");
        }
        mBuilder = new StringBuilder(url);
        appendEssentialParams();
    }

    public URLBuilder setResource(String resource) {
        mBuilder.append(resource);
        appendEssentialParams();
        return this;
    }

    private void appendEssentialParams() throws IllegalStateException {
        if (! mbAreEssentialParamsAppended) {
            mbAreEssentialParamsAppended = true;
        } else {
            throw new IllegalStateException("appendEssentialParams() cannot be called more than once");
        }
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

    public String build() throws IllegalStateException {
        if (! mbAreEssentialParamsAppended) {
            throw new IllegalStateException("appendEssentialParams() must be called before the URL can be built");
        }

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
