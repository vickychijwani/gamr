package io.github.vickychijwani.gimmick.pref;

import android.content.Context;

import org.jetbrains.annotations.NotNull;

/**
 * Utility class to persist user preferences. Do NOT use this for persisting application state, that
 * is managed separately by {@link AppState}.
 */
public class UserPrefs extends Prefs<UserPrefs.Key> {

    private static final String PREFS_FILE_NAME = ".user_prefs";

    private static UserPrefs sUserPrefs;

    // keys
    public static class Key extends BaseKey {

        protected Key(String str, Class type, Object defaultValue) {
            super(str, type, defaultValue);
        }

    }

    private UserPrefs(@NotNull Context context) {
        super(context, PREFS_FILE_NAME);
    }

    public static void initialize(@NotNull Context context) {
        if (sUserPrefs == null) {
            sUserPrefs = new UserPrefs(context);
        }
    }

    public static UserPrefs getInstance() {
        return sUserPrefs;
    }

}
