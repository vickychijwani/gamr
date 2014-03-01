package io.github.vickychijwani.gimmick.pref;

import android.content.Context;

import org.jetbrains.annotations.NotNull;

/**
 * Utility class to persist global app state (like first-run status, etc). Do NOT use this for
 * persisting user preferences, those are managed separately by {@link UserPrefs}.
 */
public class AppState extends Prefs<AppState.Key> {

    private static final String PREFS_FILE_NAME = ".app_state";

    private static AppState sAppState;

    // keys
    public static class Key extends BaseKey {

        public static final Key FIRST_RUN = new Key("first_run", Boolean.class, true);

        protected Key(String str, Class type, Object defaultValue) {
            super(str, type, defaultValue);
        }

    }

    private AppState(@NotNull Context context) {
        super(context, PREFS_FILE_NAME);
    }

    public static void initialize(@NotNull Context context) {
        if (sAppState == null) {
            sAppState = new AppState(context);
        }
    }

    public static AppState getInstance() {
        return sAppState;
    }

}
