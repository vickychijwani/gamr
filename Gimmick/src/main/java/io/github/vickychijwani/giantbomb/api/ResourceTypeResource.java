package io.github.vickychijwani.giantbomb.api;

import android.util.Log;

import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.RequestFuture;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

import io.github.vickychijwani.giantbomb.item.ResourceType;
import io.github.vickychijwani.network.json.JSONArrayIterator;
import io.github.vickychijwani.network.volley.VolleyRequestQueue;

class ResourceTypeResource implements Resource<ResourceType> {

    private static ResourceTypeResource sInstance = null;

    /**
     * Use {@link #getInstance()} instead.
     */
    private ResourceTypeResource() { }

    @Nullable
    public List<ResourceType> fetchAll() {
        Log.i(TAG, "Fetching resource types...");

        String url = URLBuilder.newInstance()
                .setResource(getResourceName())
                .build();

        RequestFuture<JSONObject> future = RequestFuture.newFuture();
        JsonObjectRequest req = new JsonObjectRequest(url, null, future, future);
        VolleyRequestQueue.add(req);

        try {
            JSONArray typesJson = future.get().getJSONArray(RESULTS);    // block until request completes
            JSONArrayIterator typesJsonIterator = new JSONArrayIterator(typesJson);
            List<ResourceType> resourceTypes = new ArrayList<ResourceType>(typesJson.length());
            while (typesJsonIterator.hasNext()) {
                JSONObject typeJson = typesJsonIterator.next();
                if (typeJson != null) {
                    resourceTypes.add(itemFromJson(typeJson, new ResourceType()));
                }
            }
            return resourceTypes;
        } catch (InterruptedException e) {
            Log.e(TAG, Log.getStackTraceString(e));
        } catch (ExecutionException e) {
            Log.e(TAG, Log.getStackTraceString(e));
        } catch (JSONException e) {
            Log.e(TAG, Log.getStackTraceString(e));
        }

        return null;
    }

    public static ResourceTypeResource getInstance() {
        if (sInstance == null) {
            sInstance = new ResourceTypeResource();
        }
        return sInstance;
    }

    @Override
    public String getResourceName() {
        return "types";
    }

    @NotNull
    @Override
    public ResourceType itemFromJson(@NotNull JSONObject json, @NotNull ResourceType resourceType) {
        try {
            resourceType.setId(json.getInt(ID));
            resourceType.setSingularName(json.getString(DETAIL_RESOURCE_NAME));
            resourceType.setPluralName(json.getString(LIST_RESOURCE_NAME));
        } catch (JSONException e) {
            Log.e(TAG, Log.getStackTraceString(e));
        }
        return resourceType;
    }

}
