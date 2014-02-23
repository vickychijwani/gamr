package io.github.vickychijwani.gimmick.api;

import android.util.Log;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Iterator;

/**
 * Utility class to iterate over a {@link JSONArray} of {@link org.json.JSONObject}s.
 */
class JSONArrayIterator implements Iterator {

    private static final String TAG = "JSONArrayIterator";

    private JSONArray mJsonArray;
    private int mPosition = 0;

    public JSONArrayIterator(@NotNull JSONArray jsonArray) {
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
    @Nullable
    public JSONObject next() {
        try {
            return mJsonArray.getJSONObject(mPosition++);
        } catch (JSONException e) {
            Log.e(TAG, Log.getStackTraceString(e));
        }
        return null;
    }

}
