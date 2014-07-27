package io.github.vickychijwani.giantbomb.api;

import android.net.Uri;
import android.util.Log;

import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import io.github.vickychijwani.giantbomb.item.Platform;
import io.github.vickychijwani.giantbomb.item.ResourceType;
import io.github.vickychijwani.network.json.JSONArrayIterator;
import io.github.vickychijwani.network.volley.VolleyRequestQueue;

@Singleton
public class PlatformsAPI extends BaseAPI<Platform> {

    @Inject
    public PlatformsAPI(VolleyRequestQueue requestQueue, URLFactory urlFactory) {
        super(ResourceType.PLATFORM, requestQueue, urlFactory);
    }

    /**
     * Fetch all platforms listed by the GiantBomb API in a <i>synchronous</i> manner.
     *
     * @return  a {@link ResourceList} of all platforms supported by the GiantBomb API
     */
    @NotNull
    public ResourceList<Platform> fetchAll() {
        Log.i(TAG, "Fetching all platforms...");

        Uri uri = newListResourceURL()
                .setFieldList(new String[] { ID, NAME, ALIASES, ABBREVIATION })
                .build();
        assert uri != null;

        return new ResourceList<Platform>(this, uri);
    }

    @NotNull
    @Override
    Platform itemFromJson(@NotNull JSONObject json, @NotNull Platform platform) {
        try {
            platform.setGiantBombId(json.getInt(ID));
            platform.setName(json.getString(NAME));
            platform.setShortName(json.getString(ABBREVIATION));

            List<String> aliases = new ArrayList<String>();
            if (! json.isNull(ALIASES)) {
                aliases.addAll(Arrays.asList(json.getString(ALIASES).split("\n")));
            }

            platform.setAliases(aliases);
        } catch (JSONException e) {
            Log.e(TAG, Log.getStackTraceString(e));
        }
        return platform;
    }

    @NotNull
    @Override
    List<Platform> itemListFromJson(@NotNull JSONArray jsonArray) {
        JSONArrayIterator jsonIterator = new JSONArrayIterator(jsonArray);
        List<Platform> platforms = new ArrayList<Platform>(jsonArray.length());
        while (jsonIterator.hasNext()) {
            JSONObject jsonObject = jsonIterator.next();
            if (jsonObject != null) {
                platforms.add(itemFromJson(jsonObject, new Platform()));
            }
        }
        return platforms;
    }

}
