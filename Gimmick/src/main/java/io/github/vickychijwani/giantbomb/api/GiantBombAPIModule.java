package io.github.vickychijwani.giantbomb.api;

import org.jetbrains.annotations.NotNull;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

@Module(
        library = true
)
public final class GiantBombAPIModule {

    private static final String BASE_URL = "www.giantbomb.com/api";
    private static String sApiKey;

    public static void setAPIKey(@NotNull String apiKey) {
        sApiKey = apiKey;
    }

    @Provides @Singleton
    URLFactory provideURLFactory() {
        return new URLFactory(BASE_URL, sApiKey);
    }

}
