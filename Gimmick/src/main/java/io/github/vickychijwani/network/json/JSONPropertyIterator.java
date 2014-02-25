package io.github.vickychijwani.network.json;

import android.util.Log;

import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONException;

import java.util.Iterator;

/**
 * Utility class to iterate over a single property of type {@code T} in a {@link JSONArray} of
 * {@link org.json.JSONObject}s.
 */
public class JSONPropertyIterator<T> implements Iterator<T> {

    private static final String TAG = "JSONPropertyIterator";

    private JSONArray mJsonArray;
    private String mPropertyName;
    private int mPosition = 0;

    public JSONPropertyIterator(@NotNull JSONArray jsonArray, @NotNull String propertyName)
            throws JSONException, IllegalArgumentException {
        if (jsonArray.length() > 0 && ! jsonArray.getJSONObject(0).has(propertyName)) {
            throw new IllegalArgumentException("objects in JSONArray do not have \"" + propertyName + "\" field for iteration");
        }
        mJsonArray = jsonArray;
        mPropertyName = propertyName;
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
    @SuppressWarnings("unchecked")
    public T next() {
        try {
            return (T) mJsonArray.getJSONObject(mPosition++).get(mPropertyName);
        } catch (JSONException e) {
            Log.e(TAG, Log.getStackTraceString(e));
        }
        return null;
    }

}
