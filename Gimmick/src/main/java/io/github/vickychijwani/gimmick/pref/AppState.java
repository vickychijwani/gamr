package io.github.vickychijwani.gimmick.pref;

import android.content.Context;

import org.jetbrains.annotations.NotNull;

import javax.inject.Inject;
import javax.inject.Singleton;

import io.github.vickychijwani.gimmick.dagger.ApplicationContext;

/**
 * Utility class to persist global app state (like first-run status, etc). Do NOT use this for
 * persisting user preferences, those are managed separately by {@link UserPrefs}.
 */
@Singleton
public class AppState extends Prefs<AppState.Key> {

    private static final String PREFS_FILE_NAME = ".app_state";

    // keys
    public static class Key extends BaseKey {

        public static final Key FIRST_RUN = new Key("first_run", Boolean.class, true);

        // remember the video resolution preference that was selected last time
        // useful for users who *want* to be asked for that preference every time
        public static final Key VIDEO_RES_LAST_SELECTED = new Key("video_res_last_selected", Integer.class, UserPrefs.VIDEO_RES_LOW);

        protected Key(String str, Class type, Object defaultValue) {
            super(str, type, defaultValue);
        }

    }

    @Inject
    AppState(@NotNull @ApplicationContext Context context) {
        super(context, PREFS_FILE_NAME);
    }

}
