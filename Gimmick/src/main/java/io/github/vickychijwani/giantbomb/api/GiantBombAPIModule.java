package io.github.vickychijwani.giantbomb.api;

import org.jetbrains.annotations.NotNull;

import java.util.Map;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import io.github.vickychijwani.giantbomb.item.ResourceType;
import io.github.vickychijwani.gimmick.dagger.GameResourceType;
import io.github.vickychijwani.gimmick.dagger.PlatformResourceType;
import io.github.vickychijwani.gimmick.dagger.VideoResourceType;

@Module(
        library = true
)
public final class GiantBombAPIModule {

    private static final String BASE_URL = "www.giantbomb.com/api";
    private static String sApiKey;
    private static Map<String, ResourceType> sResourceTypes;

    public static void setAPIKey(@NotNull String apiKey) {
        sApiKey = apiKey;
    }

    public static void setResourceTypes(@NotNull Map<String, ResourceType> resourceTypes) {
        sResourceTypes = resourceTypes;
    }

    @Provides
    public @GameResourceType ResourceType provideGameResourceType() {
        return sResourceTypes.get("game");
    }

    @Provides
    public @PlatformResourceType ResourceType providePlatformResourceType() {
        return sResourceTypes.get("platform");
    }

    @Provides
    public @VideoResourceType ResourceType provideVideoResourceType() {
        return sResourceTypes.get("video");
    }

    @Provides @Singleton
    URLFactory provideURLFactory() {
        return new URLFactory(BASE_URL, sApiKey);
    }

}
