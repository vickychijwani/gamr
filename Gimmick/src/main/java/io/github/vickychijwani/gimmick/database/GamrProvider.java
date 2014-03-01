package io.github.vickychijwani.gimmick.database;

import android.content.ContentProvider;
import android.content.ContentProviderOperation;
import android.content.ContentProviderResult;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.OperationApplicationException;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.util.Log;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Iterator;

import io.github.vickychijwani.gimmick.database.DatabaseContract.GameListTable;
import io.github.vickychijwani.gimmick.database.DatabaseContract.GamePlatformMappingTable;
import io.github.vickychijwani.gimmick.database.DatabaseContract.GameTable;
import io.github.vickychijwani.gimmick.database.DatabaseContract.PlatformTable;
import io.github.vickychijwani.gimmick.database.DatabaseContract.VideoTable;
import io.github.vickychijwani.giantbomb.item.Game;
import io.github.vickychijwani.giantbomb.item.Platform;
import io.github.vickychijwani.giantbomb.item.Video;

public class GamrProvider extends ContentProvider {

    private static final String TAG = "GamrProvider";

    private static GamrProvider sInstance;
    private static UriMatcher sUriMatcher;

    private static final int GAMES = 100;
    private static final int GAMES_ID = 101;
    private static final int GAMES_VIDEOS = 102;

    private static final int LISTS = 200;
    private static final int LISTS_METADATA = 201;
    private static final int LISTS_GAMES = 202;

    private static final int PLATFORMS = 300;
    private static final int PLATFORMS_ID = 301;

    private static final int GAME_PLATFORM_MAPPINGS = 400;

    private static final int VIDEOS = 500;

    @Override
    public boolean onCreate() {
        final Context context = getContext();
        sUriMatcher = buildUriMatcher(context);
        DBHelper.createInstance(context);
        sInstance = this;
        return true;
    }

    /**
     * Performs the work provided in a single transaction
     */
    @Nullable
    @Override
    public ContentProviderResult[] applyBatch(@NotNull final ArrayList<ContentProviderOperation> operations) {
        try {
            return DBHelper.executeTransaction(new DBHelper.Transaction<ContentProviderResult[]>() {
                @Override
                public ContentProviderResult[] run(@NotNull SQLiteDatabase db) throws OperationApplicationException {
                    int i = 0;
                    ContentProviderResult[] result = new ContentProviderResult[operations.size()];
                    for (ContentProviderOperation operation : operations) {
                        // Chain the result for back references
                        result[i++] = operation.apply(GamrProvider.this, result, i);
                    }
                    return result;
                }
            });
        } catch (Exception e) {
            Log.d(TAG, "batch operation failed: " + e.getMessage());
            return null;
        }
    }

    /** {@inheritDoc} */
    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        Log.i(TAG, "[query ] uri: " + uri);
        final int match = sUriMatcher.match(uri);
        Cursor cursor;

        switch (match) {
            case GAMES_ID:
                cursor = DBHelper.getGame(ContentUris.parseId(uri));
                break;
            case GAMES_VIDEOS:
                cursor = DBHelper.getVideosForGame(ContentUris.parseId(uri));
                break;
            case LISTS_GAMES:
                cursor = DBHelper.getGamesInList(ContentUris.parseId(uri));
                break;
            default:
                throw new IllegalArgumentException("[query ] uri unknown or operation not supported for this uri: " + uri);
        }

        // enable notifications on the cursor when underlying data changes
        assert getContext() != null;
        cursor.setNotificationUri(getContext().getContentResolver(), uri);

        Log.i(TAG, "[query ] returning " + cursor.getCount() + " result(s)");

        return cursor;
    }

    /** {@inheritDoc} */
    @Override
    public Uri insert(@NotNull Uri uri, @NotNull ContentValues values) {
        Log.i(TAG, "[insert] uri: " + uri);
        final int match = sUriMatcher.match(uri);
        Uri insertedUri = null;
        long insertedId;
        assert getContext() != null;

        switch (match) {
            case LISTS_GAMES:
                insertedId = DBHelper.addGame(values);
                if (insertedId >= 0) {
                    insertedUri = ContentUris.withAppendedId(GameTable.CONTENT_URI_LIST, insertedId);
                }
                break;
            case PLATFORMS:
                insertedId = DBHelper.addPlatform(values);
                if (insertedId < 0) {
                    insertedId = DBHelper.getPlatformId(values);
                }
                insertedUri = ContentUris.withAppendedId(PlatformTable.CONTENT_URI_LIST, insertedId);
                break;
            case GAME_PLATFORM_MAPPINGS:
                insertedId = DBHelper.addGamePlatformMapping(values);
                if (insertedId >= 0) {
                    insertedUri = ContentUris.withAppendedId(GamePlatformMappingTable.CONTENT_URI_LIST, insertedId);
                }
                break;
            case VIDEOS:
                insertedId = DBHelper.addVideo(values);
                if (insertedId >= 0) {
                    insertedUri = ContentUris.withAppendedId(VideoTable.CONTENT_URI_LIST, insertedId);
                }
                break;
            default:
                throw new IllegalArgumentException("[insert] uri unknown or operation not supported for this uri: " + uri);
        }

        if (insertedId >= 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }

        return insertedUri;
    }

    /** {@inheritDoc} */
    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        return 0;
    }

    /** {@inheritDoc} */
    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        return 0;
    }

    public static boolean addGame(Game game) {
        ArrayList<ContentProviderOperation> ops = new ArrayList<ContentProviderOperation>();
        Uri toPlayListUri = ContentUris.withAppendedId(GameListTable.CONTENT_URI_LIST_GAMES, GameListTable.TO_PLAY_ID);
        ops.add(ContentProviderOperation.newInsert(toPlayListUri)
                .withValues(GameTable.contentValuesFor(game))
                .build());

        Iterator<Platform> platforms = game.getPlatforms();
        while (platforms.hasNext()) {
            Platform platform = platforms.next();
            ops.add(ContentProviderOperation.newInsert(PlatformTable.CONTENT_URI_INSERT)
                    .withValues(PlatformTable.contentValuesFor(platform))
                    .build());

            ops.add(ContentProviderOperation.newInsert(GamePlatformMappingTable.CONTENT_URI_INSERT)
                    .withValueBackReferences(GamePlatformMappingTable.contentValuesFor(0, ops.size() - 1))
                    .build());
        }

        Iterator<Video> videos = game.getVideos();
        while (videos.hasNext()) {
            ops.add(ContentProviderOperation.newInsert(VideoTable.CONTENT_URI_INSERT)
                    .withValues(VideoTable.contentValuesFor(videos.next()))
                    .build());
        }

        ContentProviderResult[] result = sInstance.applyBatch(ops);

        return result != null;
    }

    @Override
    public String getType(Uri uri) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case GAMES:
                return GameTable.CONTENT_TYPE;
            case GAMES_ID:
                return GameTable.CONTENT_ITEM_TYPE;
            case GAMES_VIDEOS:
                return VideoTable.CONTENT_TYPE;
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
            case GAME_PLATFORM_MAPPINGS:
                return GamePlatformMappingTable.CONTENT_TYPE;
            case VIDEOS:
                return VideoTable.CONTENT_TYPE;
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
        matcher.addURI(authority, GameTable.TABLE_NAME + "/" + VideoTable.TABLE_NAME + "/#", GAMES_VIDEOS);

        // Lists
        matcher.addURI(authority, GameListTable.TABLE_NAME, LISTS);
        matcher.addURI(authority, GameListTable.TABLE_NAME + "/#", LISTS_METADATA);
        matcher.addURI(authority, GameListTable.TABLE_NAME + "/" + GameTable.TABLE_NAME + "/#", LISTS_GAMES);

        // Platforms
        matcher.addURI(authority, PlatformTable.TABLE_NAME, PLATFORMS);
        matcher.addURI(authority, PlatformTable.TABLE_NAME + "/#", PLATFORMS_ID );

        // Game <=> platform mappings
        matcher.addURI(authority, GamePlatformMappingTable.TABLE_NAME, GAME_PLATFORM_MAPPINGS);

        // Videos
        matcher.addURI(authority, VideoTable.TABLE_NAME, VIDEOS);

        return matcher;
    }

}
