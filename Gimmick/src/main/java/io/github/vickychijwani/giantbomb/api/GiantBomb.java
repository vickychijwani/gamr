package io.github.vickychijwani.giantbomb.api;

import com.android.volley.Response;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.json.JSONException;

import java.util.List;
import java.util.concurrent.ExecutionException;

import io.github.vickychijwani.giantbomb.item.Game;
import io.github.vickychijwani.giantbomb.item.GameList;
import io.github.vickychijwani.giantbomb.item.ResourceType;
import io.github.vickychijwani.network.volley.RequestTag;

/**
 * Public interface for accessing the GiantBomb API.
 */
public abstract class GiantBomb {

    // call this before using any other methods
    public static void initialize(@NotNull String apiKey) {
       URLBuilder.setApiKey(apiKey);
    }

    public static class Games {

        /**
         * Fetch a game's details in a <i>synchronous</i> manner from the given URL.
         *
         * NOTE: never call this from the UI thread!
         *
         * @param giantBombUrl  API URL
         * @return  the requested {@link Game}
         */
        @Nullable
        public static Game fetch(@NotNull String giantBombUrl) {
            return GameResource.getInstance().fetch(giantBombUrl);
        }

        /**
         * Fire an asynchronous game search.
         *
         * @param query             Search term
         * @param successHandler    handler to invoke if request succeeds
         * @param errorHandler      handler to invoke if request fails
         * @return                  a tag that can be used to cancel any ongoing search requests by
         *                          calling {@link io.github.vickychijwani.network.volley.VolleyRequestQueue#cancelAll(io.github.vickychijwani.network.volley.RequestTag)}.
         */
        public static RequestTag search(@NotNull String query,
                                        Response.Listener<GameList> successHandler,
                                        Response.ErrorListener errorHandler) {
            return GameListResource.getInstance().search(query, successHandler, errorHandler);
        }

        /**
         * Fire an asynchronous request to fetch "upcoming" games (i.e., games that will be released in
         * the current year).
         *
         * @param successHandler    handler to invoke if request succeeds
         * @param errorHandler      handler to invoke if request fails
         * @return                  a tag that can be used to cancel any ongoing "upcoming games"
         *                          requests by calling {@link io.github.vickychijwani.network.volley.VolleyRequestQueue#cancelAll(RequestTag)}.
         */
        public static RequestTag fetchUpcoming(final Response.Listener<GameList> successHandler,
                                               final Response.ErrorListener errorHandler) {
            return GameListResource.getInstance().fetchUpcoming(successHandler, errorHandler);
        }

        /**
         * Fire an asynchronous request to fetch recently-released games (i.e., games that have been
         * released in the past 1 year).
         *
         * @param successHandler    handler to invoke if request succeeds
         * @param errorHandler      handler to invoke if request fails
         * @return                  a tag that can be used to cancel any ongoing "recent games"
         *                          requests by calling {@link io.github.vickychijwani.network.volley.VolleyRequestQueue#cancelAll(RequestTag)}.
         */
        public static RequestTag fetchRecent(final Response.Listener<GameList> successHandler,
                                             final Response.ErrorListener errorHandler) {
            return GameListResource.getInstance().fetchRecent(successHandler, errorHandler);
        }

    }

    public static class Videos {

        /**
         * Fetch a game's videos' details in a <i>synchronous</i> manner.
         * <p/>
         * NOTE: never call this from the UI thread!
         *
         * @param game  the {@link Game} for which to fetch video data. It must have
         *              {@link io.github.vickychijwani.giantbomb.item.Video}s with a valid GiantBomb
         *              video ID on each of them.
         * @return      the {@link Game} that was passed in, augmented with the requested video data
         */
        @NotNull
        public static Game fetchAllForGame(@NotNull Game game)
                throws ExecutionException, InterruptedException, JSONException {
            return VideoResource.getInstance().fetchAllForGame(game);
        }

    }

    public static class ResourceTypes {

        /**
         * Fetch all resource types supported by the GiantBomb API in a <i>synchronous</i> manner.
         * <p/>
         * NOTE: never call this from the UI thread!
         *
         * @return  a list of all resource types supported by the GiantBomb API
         */
        @Nullable
        public static List<ResourceType> fetchAll() {
            return ResourceTypeResource.getInstance().fetchAll();
        }

    }

}
