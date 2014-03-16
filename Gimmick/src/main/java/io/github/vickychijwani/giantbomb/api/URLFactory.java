package io.github.vickychijwani.giantbomb.api;

import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;

import org.intellij.lang.annotations.MagicConstant;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

class URLFactory {

    private static final String TAG = "GiantBombAPI";
    private final String mBaseUrl;
    private final String mApiKey;

    public URLFactory(@NotNull String baseUrl, @NotNull String apiKey) {
        mBaseUrl = baseUrl;
        mApiKey = apiKey;
    }

    public Builder newURL() {
        return new Builder();
    }

    class Builder {
        private Uri.Builder mBuilder;

        public Builder() {
            mBuilder = new Uri.Builder();
            mBuilder.scheme("http");
            mBuilder.encodedAuthority(mBaseUrl);

            // add essential parameters
            addParam("api_key", mApiKey);
            addParam("format", "json");
        }

        public Builder setResource(@NotNull String resource) {
            mBuilder.path(resource);
            return this;
        }

        public Builder setResource(@NotNull String resource, int resourceTypeId, int resourceId) {
            mBuilder.path(joinPathSegments(resource, resourceTypeId + "-" + resourceId));
            return this;
        }

        public Builder setFieldList(String... fieldList) {
            String fieldString = TextUtils.join(",", fieldList);
            return addParam("field_list", fieldString);
        }

        public Builder setSortOrder(SortParam... sortParamList) {
            List<SortParam> sortParams = Arrays.asList(sortParamList);
            Collections.reverse(sortParams);   // the Giant Bomb API takes sort options in reverse order, no idea why
            String sortOrderString = TextUtils.join(",", sortParams);
            return addParam("sort", sortOrderString);
        }

        public Builder addParam(@NotNull String key, @NotNull String value) {
            mBuilder.appendQueryParameter(key, value);
            return this;
        }

        @Nullable
        public String build() {
            Uri uri = mBuilder.build();
            if (uri == null) {
                Log.e(TAG, "Could not build Uri");
                return null;
            }
            return uri.toString();
        }

        private String joinPathSegments(String... segments) {
            return TextUtils.join("/", segments);
        }

    }

    public static class SortParam {
        public static final String ASC = "asc";
        public static final String DESC = "desc";

        public final String field;
        public final String order;

        public SortParam(String field, @MagicConstant(stringValues = {"asc", "desc"}) String order) {
            this.field = field;
            this.order = order;
        }

        @Override
        public String toString() {
            return field + ":" + order;
        }
    }

}
