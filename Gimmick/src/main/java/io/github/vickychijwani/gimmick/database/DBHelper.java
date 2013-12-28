package io.github.vickychijwani.gimmick.database;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import org.jetbrains.annotations.NotNull;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import io.github.vickychijwani.gimmick.database.DatabaseContract.GameListTable;
import io.github.vickychijwani.gimmick.database.DatabaseContract.GamePlatformMappingTable;
import io.github.vickychijwani.gimmick.database.DatabaseContract.GameTable;
import io.github.vickychijwani.gimmick.database.DatabaseContract.PlatformTable;
import io.github.vickychijwani.gimmick.item.Platform;
import io.github.vickychijwani.gimmick.item.SearchResult;

public class DBHelper extends BaseDBHelper {

    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "gamr.db";
    private static final String TAG = "DBHelper";

    public static void createInstance(Context context) {
        // Use the application context, which will ensure that you
        // don't accidentally leak an Activity's context.
        // See http://android-developers.blogspot.in/2009/01/avoiding-memory-leaks.html
        if (sInstance == null) {
            Context appContext = context.getApplicationContext();
            sInstance = new DBHelper(appContext);
        }
    }

    /**
     * Use getInstance(Context) instead
     */
    private DBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(GameListTable.createTable());
        db.execSQL(GameTable.createTable());
        db.execSQL(PlatformTable.createTable());
        db.execSQL(GamePlatformMappingTable.createTable());
        validateInitialState(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // TODO decide how to handle this!
    }

    /**
     * Add a game to the database.
     *
     * @param game  the game to be added
     * @return      true if insertion succeeds, false if the game already exists in the database
     *
     * @throws SQLException
     */
    public static boolean add(SearchResult game) throws SQLException {
        SQLiteDatabase db = sInstance.getWritableDatabase();
        assert db != null;
        db.beginTransaction();

        try {
            long gameId = db.insertWithOnConflict(GameTable.TABLE_NAME, null,
                    GameTable.contentValuesFor(game), SQLiteDatabase.CONFLICT_IGNORE);

            if (gameId == -1)
                return false;

            Iterator<Platform> platforms = game.getPlatforms();
            while (platforms.hasNext()) {
                Platform platform = platforms.next();

                db.insertWithOnConflict(PlatformTable.TABLE_NAME, null,
                        PlatformTable.contentValuesFor(platform), SQLiteDatabase.CONFLICT_IGNORE);

                // TODO get rid of this ugly code once Platform data is dynamic!
                long platformId = (Long) selectId()
                        .from(PlatformTable.TABLE_NAME)
                        .whereEquals(PlatformTable.COL_NAME, platform.getShortName())
                        .execute();

                Log.d(TAG, "Game ID = " + gameId + ", platform ID = " + platformId);
                db.insertOrThrow(GamePlatformMappingTable.TABLE_NAME, null,
                        GamePlatformMappingTable.contentValuesFor(gameId, platformId));
            }

            db.setTransactionSuccessful();
            return true;
        } finally {
            db.endTransaction();
            db.close();
        }
    }

    /**
     * Fetch all games belonging to the given game list.
     *
     * @param listName  the game list which is to be fetched
     * @return          the list of all games belonging to {@code listName}
     */
    @NotNull
    public static List<SearchResult> getGames(@NotNull String listName) {
        SQLiteDatabase db = sInstance.getReadableDatabase();
        assert db != null;
        Cursor cursor, cursor2;

        try {
            long listId = (Long) selectId()
                    .from(GameListTable.TABLE_NAME)
                    .whereEquals(GameListTable.COL_NAME, listName)
                    .execute();

            cursor = (Cursor) selectAll()
                    .from(GameTable.TABLE_NAME)
                    .whereEquals(GameTable.COL_GAME_LIST_ID, listId)
                    .execute();
            List<SearchResult> gameList = new ArrayList<SearchResult>(cursor.getCount());

            while (cursor.moveToNext()) {
                SearchResult game = new SearchResult(cursor);

                cursor2 = (Cursor) select(GamePlatformMappingTable.COL_PLATFORM_ID)
                        .from(GamePlatformMappingTable.TABLE_NAME)
                        .whereEquals(GamePlatformMappingTable.COL_GAME_ID, game.giantBombId)
                        .execute();
                while (cursor2.moveToNext()) {
                    long platformId = cursor2.getLong(cursor2.getColumnIndexOrThrow(GamePlatformMappingTable.COL_PLATFORM_ID));
                    String platformName = (String) selectString(PlatformTable.COL_NAME)
                            .from(PlatformTable.TABLE_NAME)
                            .whereEquals(PlatformTable._ID, platformId)
                            .execute();
                    game.addPlatform(Platform.fromString(platformName));
                }
                cursor2.close();

                gameList.add(game);
            }
            cursor.close();

            return gameList;
        } finally {
            db.close();
        }
    }

    private void validateInitialState(SQLiteDatabase db) {
        ensureDefaultGameListExists(db);
    }

    private void ensureDefaultGameListExists(SQLiteDatabase db) {
        db.insertOrThrow(GameListTable.TABLE_NAME, null,
                GameListTable.contentValuesForToPlayList());
    }

}
