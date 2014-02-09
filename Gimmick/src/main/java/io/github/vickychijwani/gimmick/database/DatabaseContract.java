package io.github.vickychijwani.gimmick.database;

import android.content.ContentValues;
import android.net.Uri;
import android.provider.BaseColumns;

import org.jetbrains.annotations.NotNull;

import io.github.vickychijwani.gimmick.GamrApplication;
import io.github.vickychijwani.gimmick.item.Platform;
import io.github.vickychijwani.gimmick.item.SearchResult;

public final class DatabaseContract {

    // prevent creation of instances
    private DatabaseContract() {}

    private static final String CONTENT_TYPE_BASE = "vnd.android.cursor.dir/vnd.vickychijwani.gimmick.";
    private static final String CONTENT_ITEM_TYPE_BASE = "vnd.android.cursor.item/vnd.vickychijwani.gimmick.";

    private static final Uri CONTENT_URI_BASE = Uri.parse("content://" + GamrApplication.CONTENT_AUTHORITY);

    /**
     * Database schema
     */
    // Game lists
    public static abstract class GameListTable implements BaseColumns {
        public static final String TABLE_NAME = "game_list";
        public static final String TO_PLAY = "To play";
        public static final int TO_PLAY_ID = 1;

        /** Use if multiple items get returned */
        public static final String CONTENT_TYPE = CONTENT_TYPE_BASE + TABLE_NAME;

        /** Use if a single item is returned */
        public static final String CONTENT_ITEM_TYPE = CONTENT_ITEM_TYPE_BASE + TABLE_NAME;

        /** Content URI for listing all games in a given list */
        public static final Uri CONTENT_URI_LIST_GAMES = CONTENT_URI_BASE.buildUpon()
                .appendPath(GameListTable.TABLE_NAME)
                .appendPath(GameTable.TABLE_NAME)
                .build();

        /** Game list name */
        public static final String COL_NAME = "name";

        public static String createTable() {
            return SQL.CREATE_TABLE(TABLE_NAME,
                    SQL.DEF_PRIMARY_KEY_AUTOINCREMENT(_ID, SQL.Type.INTEGER),
                    SQL.DEF_COL(COL_NAME, SQL.Type.TEXT, SQL.Constraint.NOT_NULL, SQL.Constraint.UNIQUE));
        }

        @NotNull
        public static ContentValues contentValuesForToPlayList() {
            ContentValues values = new ContentValues();
            values.put(_ID, TO_PLAY_ID);
            values.put(COL_NAME, TO_PLAY);
            return values;
        }

        @NotNull
        public static String qualify(@NotNull String colName) { return TABLE_NAME + "." + colName; }
    }

    // Games
    public static abstract class GameTable implements BaseColumns {
        public static final String TABLE_NAME = "game";

        /** Use if multiple items get returned */
        public static final String CONTENT_TYPE = CONTENT_TYPE_BASE + TABLE_NAME;

        /** Use if a single item is returned */
        public static final String CONTENT_ITEM_TYPE = CONTENT_ITEM_TYPE_BASE + TABLE_NAME;

        /** Content URI for listing all games in the database */
        public static final Uri CONTENT_URI_LIST = CONTENT_URI_BASE.buildUpon()
                .appendPath(GameTable.TABLE_NAME)
                .build();

        /** Content URI for inserting a new game */
        public static final Uri CONTENT_URI_INSERT = CONTENT_URI_LIST;

        /** Game name */
        public static final String COL_NAME = "name";

        /** URL for thumb-sized game poster */
        public static final String COL_POSTER_URL = "poster_url";

        /** URL for small-sized game poster */
        public static final String COL_SMALL_POSTER_URL = "small_url";

        /** Game release date (exact, if in the past; best guess, if in the future) */
        public static final String COL_RELEASE_DAY = "release_day";
        public static final String COL_RELEASE_MONTH = "release_month";
        public static final String COL_RELEASE_QUARTER = "release_quarter";
        public static final String COL_RELEASE_YEAR = "release_year";

        /** Short blurb describing the game */
        public static final String COL_BLURB = "blurb";

        /** [Foreign key] ID of the list to which this game belongs */
        public static final String COL_GAME_LIST_ID = "game_list_id";

        /** [Pseudo-column] CSV of all platforms this game is available on */
        public static final String COL_PSEUDO_PLATFORMS = "platforms";


        /** Metacritic rating for the game */
        public static final String COL_METACRITIC_RATING = "metacritic_rating";

        /** CSV of all genres this game belongs to */
        public static final String COL_GENRES = "genres";

        /** CSV of all franchises this game belongs to */
        public static final String COL_FRANCHISES = "franchises";

        public static String createTable() {
            return SQL.CREATE_TABLE(TABLE_NAME,
                    SQL.DEF_PRIMARY_KEY(_ID, SQL.Type.INTEGER),
                    SQL.DEF_COL(COL_POSTER_URL, SQL.Type.TEXT, SQL.Constraint.DEFAULT("\"\"")),
                    SQL.DEF_COL(COL_SMALL_POSTER_URL, SQL.Type.TEXT, SQL.Constraint.DEFAULT("\"\"")),
                    SQL.DEF_COL(COL_NAME, SQL.Type.TEXT, SQL.Constraint.NOT_NULL),
                    SQL.DEF_COL(COL_RELEASE_DAY, SQL.Type.INTEGER, SQL.Constraint.NOT_NULL),
                    SQL.DEF_COL(COL_RELEASE_MONTH, SQL.Type.INTEGER, SQL.Constraint.NOT_NULL),
                    SQL.DEF_COL(COL_RELEASE_QUARTER, SQL.Type.INTEGER, SQL.Constraint.NOT_NULL),
                    SQL.DEF_COL(COL_RELEASE_YEAR, SQL.Type.INTEGER, SQL.Constraint.NOT_NULL),
                    SQL.DEF_COL(COL_BLURB, SQL.Type.TEXT, SQL.Constraint.DEFAULT("\"\"")),
                    SQL.DEF_FOREIGN_KEY_NOT_NULL(COL_GAME_LIST_ID, GameListTable.TABLE_NAME, GameListTable._ID),
                    SQL.DEF_COL(COL_METACRITIC_RATING, SQL.Type.INTEGER, SQL.Constraint.NOT_NULL, SQL.Constraint.DEFAULT(-1)),
                    SQL.DEF_COL(COL_GENRES, SQL.Type.TEXT, SQL.Constraint.DEFAULT("\"\"")),
                    SQL.DEF_COL(COL_FRANCHISES, SQL.Type.TEXT, SQL.Constraint.DEFAULT("\"\""))
            );
        }

        @NotNull
        public static ContentValues contentValuesFor(SearchResult game) {
            ContentValues values = new ContentValues();
            values.put(_ID, game.giantBombId);
            values.put(COL_NAME, game.name);
            values.put(COL_POSTER_URL, game.posterUrl);
            values.put(COL_SMALL_POSTER_URL, game.smallPosterUrl);
            values.put(COL_RELEASE_DAY, game.releaseDate.getDay());
            values.put(COL_RELEASE_MONTH, game.releaseDate.getMonth());
            values.put(COL_RELEASE_QUARTER, game.releaseDate.getQuarter());
            values.put(COL_RELEASE_YEAR, game.releaseDate.getYear());
            values.put(COL_BLURB, game.blurb);
            values.put(COL_GAME_LIST_ID, GameListTable.TO_PLAY_ID);
            values.put(COL_METACRITIC_RATING, game.metacriticRating);
            values.put(COL_GENRES, game.getGenresDisplayString());
            values.put(COL_FRANCHISES, game.getFranchisesDisplayString());
            return values;
        }

        @NotNull
        public static String[] essentialColumns() {
            return new String[] {
                    qualify(_ID), qualify(COL_NAME), COL_POSTER_URL,
                    COL_RELEASE_DAY, COL_RELEASE_MONTH, COL_RELEASE_QUARTER, COL_RELEASE_YEAR,
                    SQL.groupConcat(PlatformTable.qualify(PlatformTable.COL_NAME), COL_PSEUDO_PLATFORMS)
            };
        }

        @NotNull
        public static String[] allColumns() {
            return new String[] {
                    qualify(_ID), qualify(COL_NAME), COL_POSTER_URL, COL_SMALL_POSTER_URL, COL_BLURB,
                    COL_RELEASE_DAY, COL_RELEASE_MONTH, COL_RELEASE_QUARTER, COL_RELEASE_YEAR,
                    SQL.groupConcat(PlatformTable.qualify(PlatformTable.COL_NAME), COL_PSEUDO_PLATFORMS),
                    COL_METACRITIC_RATING, COL_GENRES, COL_FRANCHISES
            };
        }

        @NotNull
        public static String qualify(@NotNull String colName) { return TABLE_NAME + "." + colName; }
    }

    // Platforms
    public static abstract class PlatformTable implements BaseColumns {
        public static final String TABLE_NAME = "platform";

        /** Use if multiple items get returned */
        public static final String CONTENT_TYPE = CONTENT_TYPE_BASE + TABLE_NAME;

        /** Use if a single item is returned */
        public static final String CONTENT_ITEM_TYPE = CONTENT_ITEM_TYPE_BASE + TABLE_NAME;

        /** Content URI for listing all platforms */
        public static final Uri CONTENT_URI_LIST = CONTENT_URI_BASE.buildUpon()
                .appendPath(PlatformTable.TABLE_NAME)
                .build();

        /** Content URI for inserting a new platform */
        public static final Uri CONTENT_URI_INSERT = CONTENT_URI_LIST;

        /** Platform name */
        public static final String COL_NAME = "name";

        public static String createTable() {
            return SQL.CREATE_TABLE(TABLE_NAME,
                    SQL.DEF_PRIMARY_KEY_AUTOINCREMENT(_ID, SQL.Type.INTEGER),
                    SQL.DEF_COL(COL_NAME, SQL.Type.TEXT, SQL.Constraint.NOT_NULL, SQL.Constraint.UNIQUE));
        }

        @NotNull
        public static ContentValues contentValuesFor(Platform platform) {
            ContentValues values = new ContentValues();
            values.put(COL_NAME, platform.getShortName());
            return values;
        }

        @NotNull
        public static String qualify(@NotNull String colName) { return TABLE_NAME + "." + colName; }
    }

    // Join table mapping games <> platforms
    public static abstract class GamePlatformMappingTable {
        public static final String TABLE_NAME = "game_platform_mapping";

        /** Use if multiple items get returned */
        public static final String CONTENT_TYPE = CONTENT_TYPE_BASE + TABLE_NAME;

        /** Use if a single item is returned */
        public static final String CONTENT_ITEM_TYPE = CONTENT_ITEM_TYPE_BASE + TABLE_NAME;

        /** Content URI for inserting a new mapping */
        public static final Uri CONTENT_URI_INSERT = CONTENT_URI_BASE.buildUpon()
                .appendPath(GamePlatformMappingTable.TABLE_NAME)
                .build();

        /** Content URI for listing all mappings */
        public static final Uri CONTENT_URI_LIST = CONTENT_URI_INSERT;

        /** [Foreign key] Game ID */
        public static final String COL_GAME_ID = "game_id";

        /** [Foreign key] Platform ID */
        public static final String COL_PLATFORM_ID = "platform_id";

        public static String createTable() {
            return SQL.CREATE_TABLE(TABLE_NAME,
                    SQL.DEF_FOREIGN_KEY_NOT_NULL(COL_GAME_ID, GameTable.TABLE_NAME, GameTable._ID),
                    SQL.DEF_FOREIGN_KEY_NOT_NULL(COL_PLATFORM_ID, PlatformTable.TABLE_NAME, PlatformTable._ID),
                    SQL.DEF_COMPOSITE_KEY(COL_GAME_ID, COL_PLATFORM_ID));
        }

        @NotNull
        public static ContentValues contentValuesFor(long gameId, long platformId) {
            ContentValues values = new ContentValues();
            values.put(COL_GAME_ID, gameId);
            values.put(COL_PLATFORM_ID, platformId);
            return values;
        }
    }

}
