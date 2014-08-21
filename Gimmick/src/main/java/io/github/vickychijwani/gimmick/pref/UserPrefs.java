package io.github.vickychijwani.gimmick.pref;

import android.content.Context;

import org.jetbrains.annotations.NotNull;

import javax.inject.Inject;
import javax.inject.Singleton;

import io.github.vickychijwani.gimmick.dagger.ApplicationContext;

/**
 * Utility class to persist user preferences. Do NOT use this for persisting application state, that
 * is managed separately by {@link AppState}.
 */
@Singleton
public class UserPrefs extends Prefs<UserPrefs.Key> {

    private static final String PREFS_FILE_NAME = ".user_prefs";

    // public constants
    public static final int VIDEO_RES_ASK = -1;
    public static final int VIDEO_RES_LOW = 0;
    public static final int VIDEO_RES_HIGH = 1;

    // keys
    public static class Key extends BaseKey {

        public static final Key VIDEO_RES = new Key("video_res", Integer.class, VIDEO_RES_ASK);

        protected Key(String str, Class type, Object defaultValue) {
            super(str, type, defaultValue);
        }

    }

    @Inject
    UserPrefs(@NotNull @ApplicationContext Context context) {
        super(context, PREFS_FILE_NAME);
    }

}
