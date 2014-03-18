package io.github.vickychijwani.gimmick.database;

import android.annotation.TargetApi;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Build;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import io.github.vickychijwani.gimmick.database.DatabaseContract.GameListTable;
import io.github.vickychijwani.gimmick.database.DatabaseContract.GamePlatformMappingTable;
import io.github.vickychijwani.gimmick.database.DatabaseContract.GameTable;
import io.github.vickychijwani.gimmick.database.DatabaseContract.PlatformTable;
import io.github.vickychijwani.gimmick.database.DatabaseContract.ReviewTable;
import io.github.vickychijwani.gimmick.database.DatabaseContract.VideoTable;
import io.github.vickychijwani.utility.DeviceUtils;

public class DBHelper extends BaseDBHelper {

    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "gamr.db";

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

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    @Override
    public void onConfigure(SQLiteDatabase db) {
        if (DeviceUtils.isJellyBeanOrHigher()) {
            db.setForeignKeyConstraintsEnabled(true);
        }
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(GameListTable.createTable());
        db.execSQL(GameTable.createTable());
        db.execSQL(PlatformTable.createTable());
        db.execSQL(GamePlatformMappingTable.createTable());
        db.execSQL(VideoTable.createTable());
        db.execSQL(ReviewTable.createTable());
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // TODO decide how to handle this!
    }

    @Override
    public void onOpen(SQLiteDatabase db) {
        validateInitialState(db);
    }

    /**
     * Add a game to the database.
     *
     * @return      id of the newly-inserted game if successful, else -1
     */
    public static long addGame(ContentValues values) {
        return insertOrIgnore(GameTable.TABLE_NAME, values);
    }

    /**
     * Add a platform to the database.
     *
     * @return      id of the newly-inserted platform if successful, else -1
     */
    public static long addPlatform(ContentValues values) {
        return insertOrIgnore(PlatformTable.TABLE_NAME, values);
    }

    /**
     * Add a game-platform mapping to the database.
     *
     * @return      id of the newly-inserted mapping if successful, else -1
     */
    public static long addGamePlatformMapping(ContentValues values) {
        return insertOrIgnore(GamePlatformMappingTable.TABLE_NAME, values);
    }

    /**
     * Add a video to the database.
     *
     * @return      id of the newly-inserted video if successful, else -1
     */
    public static long addVideo(@NotNull ContentValues values) {
        return insertOrIgnore(VideoTable.TABLE_NAME, values);
    }

    /**
     * Add a review to the database.
     *
     * @return      id of the newly-inserted review if successful, else -1
     */
    public static long addReview(@NotNull ContentValues values) {
        return insertOrIgnore(ReviewTable.TABLE_NAME, values);
    }

    @NotNull
    public static Cursor getGame(long gameId) {
        return getGames(GameTable.allColumns(),
                new SQL.Eq(GameTable.qualify(GameTable._ID), gameId));
    }

    @NotNull
    public static Cursor getGamesInList(long listId) {
        return getGames(GameTable.essentialColumns(),
                new SQL.Eq(GameTable.COL_GAME_LIST_ID, listId));
    }

    @NotNull
    public static Cursor getPlatforms(@Nullable String[] projection, @Nullable String selection,
                                      @Nullable String sortOrder) {
        return (Cursor) select(projection)
                .from(PlatformTable.TABLE_NAME)
                .where(selection)
                .orderBy(sortOrder)
                .execute();
    }

    @NotNull
    public static Cursor getVideosForGame(long gameId) {
        return (Cursor) selectAll()
                .from(VideoTable.TABLE_NAME)
                .where(new SQL.Eq(VideoTable.COL_GAME_ID, gameId))
                .execute();
    }

    @NotNull
    public static Cursor getReviewsForGame(long gameId) {
        return (Cursor) selectAll()
                .from(ReviewTable.TABLE_NAME)
                .where(new SQL.Eq(ReviewTable.COL_GAME_ID, gameId))
                .execute();
    }

    private static long insertOrIgnore(@NotNull String tableName, @NotNull ContentValues values) {
        SQLiteDatabase db = getInstance().getWritableDatabase();
        assert db != null;

        return db.insertWithOnConflict(tableName, null,
                values, SQLiteDatabase.CONFLICT_IGNORE);
    }

    @NotNull
    private static Cursor getGames(@NotNull String[] cols, @NotNull SQL.Condition whereCondition) {
        return (Cursor) select(cols)
                .from(GameTable.TABLE_NAME)
                .innerJoin(GamePlatformMappingTable.TABLE_NAME,
                        new SQL.Eq(GamePlatformMappingTable.COL_GAME_ID, GameTable.qualify(GameTable._ID)))
                .leftOuterJoin(PlatformTable.TABLE_NAME,
                        new SQL.Eq(GamePlatformMappingTable.COL_PLATFORM_ID, PlatformTable.qualify(PlatformTable._ID)))
                .where(whereCondition)
                .groupBy(GameTable.qualify(GameTable._ID))
                .execute();
    }

    public static long getPlatformId(ContentValues values) {
        return (Long) selectId()
                .from(PlatformTable.TABLE_NAME)
                .where(new SQL.Eq(PlatformTable.COL_NAME, "'" + values.getAsString(PlatformTable.COL_NAME) + "'"))
                .execute();
    }

    /**
     * Execute an operation on the database.
     *
     * @param operation   the operation to run. Note that if the operation fails, no rollback will
     *                    be performed, as when using {@link #executeTransaction(Operation)}.
     * @return            the result of the database operation
     */
    public static <T> T executeOperation(@NotNull Operation<T> operation)
            throws Exception {
        SQLiteDatabase db = getInstance().getWritableDatabase();
        assert db != null;

        return operation.run(db);
    }

    /**
     * Execute an operation as a transaction on the database. The transaction is atomic, meaning
     * that either all changes are applied successfully, or none of them are.
     *
     * @param transaction   the transaction to run
     * @return              the result of the transaction
     */
    public static <T> T executeTransaction(@NotNull Operation<T> transaction)
            throws Exception {
        SQLiteDatabase db = getInstance().getWritableDatabase();
        assert db != null;
        db.beginTransaction();

        try {
            T ret = transaction.run(db);
            db.setTransactionSuccessful();
            return ret;
        } finally {
            db.endTransaction();
        }
    }

    private void validateInitialState(SQLiteDatabase db) {
        assert ! db.isReadOnly();
        ensureDefaultGameListExists(db);
    }

    private void ensureDefaultGameListExists(SQLiteDatabase db) {
        db.insertWithOnConflict(GameListTable.TABLE_NAME, null,
                GameListTable.contentValuesForToPlayList(), SQLiteDatabase.CONFLICT_IGNORE);
    }

    public interface Operation<T> {
        /**
         * Run the database operation.
         *
         * @param db    a writable instance of the SQLite database
         */
        public T run(@NotNull SQLiteDatabase db) throws Exception;
    }

}
