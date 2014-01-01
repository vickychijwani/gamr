package io.github.vickychijwani.gimmick.database;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;

import io.github.vickychijwani.gimmick.database.DatabaseContract.GameListTable;
import io.github.vickychijwani.gimmick.database.DatabaseContract.GameTable;
import io.github.vickychijwani.gimmick.database.DatabaseContract.PlatformTable;

public class GamrProvider extends ContentProvider {

    private static final String TAG = "GamrProvider";

    private static UriMatcher sUriMatcher;

    private static final int GAMES = 100;
    private static final int GAMES_ID = 101;

    private static final int LISTS = 200;
    private static final int LISTS_METADATA = 201;
    private static final int LISTS_GAMES = 202;

    private static final int PLATFORMS = 300;
    private static final int PLATFORMS_ID = 301;

    @Override
    public boolean onCreate() {
        final Context context = getContext();
        sUriMatcher = buildUriMatcher(context);
        DBHelper.createInstance(context);
        return true;
    }

    /** {@inheritDoc} */
    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        Log.i(TAG, "ContentProvider query uri: " + uri);
        final int match = sUriMatcher.match(uri);
        Cursor cursor;
        switch (match) {
            case LISTS_GAMES:
                cursor = DBHelper.getGamesInList(ContentUris.parseId(uri));
                break;
            default:
                throw new IllegalArgumentException("Unknown uri: " + uri);
        }

        // enable notifications on the cursor when underlying data changes
        assert getContext() != null;
        cursor.setNotificationUri(getContext().getContentResolver(), uri);

        return cursor;
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        return null;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        return 0;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        return 0;
    }

    @Override
    public String getType(Uri uri) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case GAMES:
                return GameTable.CONTENT_TYPE;
            case GAMES_ID:
                return GameTable.CONTENT_ITEM_TYPE;
            case LISTS:
                return GameListTable.CONTENT_TYPE;
            case LISTS_METADATA:
                return GameListTable.CONTENT_ITEM_TYPE;
            case LISTS_GAMES:
                return GameTable.CONTENT_TYPE;
            case PLATFORMS:
                return PlatformTable.CONTENT_TYPE;
            case PLATFORMS_ID:
                return PlatformTable.CONTENT_ITEM_TYPE;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
    }

    /**
     * Build and return a {@link UriMatcher} that catches all {@link Uri}
     * variations supported by this {@link ContentProvider}.
     */
    private static UriMatcher buildUriMatcher(Context context) {
        final UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);
        final String authority = context.getPackageName() + ".provider";

        // Games
        matcher.addURI(authority, GameTable.TABLE_NAME, GAMES);
        matcher.addURI(authority, GameTable.TABLE_NAME + "/#", GAMES_ID);

        // Lists
        matcher.addURI(authority, GameListTable.TABLE_NAME, LISTS);
        matcher.addURI(authority, GameListTable.TABLE_NAME + "/#", LISTS_METADATA);
        matcher.addURI(authority, GameListTable.TABLE_NAME + "/" + GameTable.TABLE_NAME + "/#", LISTS_GAMES);

        // Platforms
        matcher.addURI(authority, PlatformTable.TABLE_NAME, PLATFORMS);
        matcher.addURI(authority, PlatformTable.TABLE_NAME + "/#", PLATFORMS_ID );

        return matcher;
    }

}
