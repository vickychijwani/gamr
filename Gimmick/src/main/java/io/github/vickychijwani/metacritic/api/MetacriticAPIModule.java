package io.github.vickychijwani.metacritic.api;

import org.jetbrains.annotations.NotNull;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import io.github.vickychijwani.network.volley.VolleyRequestQueue;

@Module(
        library = true,
        complete = false
)
public class MetacriticAPIModule {

    private static final String BASE_URL = "https://byroredux-metacritic.p.mashape.com/find/game";
    private static String sApiKey;

    public static void setAPIKey(@NotNull String apiKey) {
        sApiKey = apiKey;
    }

    @Provides @Singleton
    MetacriticAPI provideMetacriticAPI(@NotNull VolleyRequestQueue requestQueue) {
        return new MetacriticAPI(BASE_URL, sApiKey, requestQueue);
    }

}
