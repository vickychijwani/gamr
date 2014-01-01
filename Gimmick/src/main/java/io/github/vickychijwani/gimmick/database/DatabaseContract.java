package io.github.vickychijwani.gimmick.database;

import android.content.ContentValues;
import android.provider.BaseColumns;

import org.jetbrains.annotations.NotNull;

import io.github.vickychijwani.gimmick.item.Platform;
import io.github.vickychijwani.gimmick.item.SearchResult;

public final class DatabaseContract {

    // prevent creation of instances
    private DatabaseContract() {}

    /**
     * Database schema
     */
    // Game lists
    public static abstract class GameListTable implements BaseColumns {
        public static final String TABLE_NAME = "game_list";
        public static final String TO_PLAY = "To play";
        public static final int TO_PLAY_ID = 1;

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
    }

    // Games
    public static abstract class GameTable implements BaseColumns {
        public static final String TABLE_NAME = "game";

        /** Game name */
        public static final String COL_NAME = "name";

        /** URL for game poster */
        public static final String COL_POSTER_URL = "poster_url";

        /** Game release date (exact, if in the past; best guess, if in the future) */
        public static final String COL_RELEASE_DAY = "release_day";
        public static final String COL_RELEASE_MONTH = "release_month";
        public static final String COL_RELEASE_QUARTER = "release_quarter";
        public static final String COL_RELEASE_YEAR = "release_year";

        /** Short blurb describing the game */
        public static final String COL_BLURB = "blurb";

        /** [Foreign key] ID of the list to which this game belongs */
        public static final String COL_GAME_LIST_ID = "game_list_id";

        public static String createTable() {
            return SQL.CREATE_TABLE(TABLE_NAME,
                    SQL.DEF_PRIMARY_KEY(_ID, SQL.Type.INTEGER),
                    SQL.DEF_COL(COL_POSTER_URL, SQL.Type.TEXT, SQL.Constraint.DEFAULT("\"\"")),
                    SQL.DEF_COL(COL_NAME, SQL.Type.TEXT, SQL.Constraint.NOT_NULL),
                    SQL.DEF_COL(COL_RELEASE_DAY, SQL.Type.INTEGER, SQL.Constraint.NOT_NULL),
                    SQL.DEF_COL(COL_RELEASE_MONTH, SQL.Type.INTEGER, SQL.Constraint.NOT_NULL),
                    SQL.DEF_COL(COL_RELEASE_QUARTER, SQL.Type.INTEGER, SQL.Constraint.NOT_NULL),
                    SQL.DEF_COL(COL_RELEASE_YEAR, SQL.Type.INTEGER, SQL.Constraint.NOT_NULL),
                    SQL.DEF_COL(COL_BLURB, SQL.Type.TEXT, SQL.Constraint.DEFAULT("\"\"")),
                    SQL.DEF_FOREIGN_KEY_NOT_NULL(COL_GAME_LIST_ID, GameListTable.TABLE_NAME, GameListTable._ID));
        }

        @NotNull
        public static ContentValues contentValuesFor(SearchResult game) {
            ContentValues values = new ContentValues();
            values.put(_ID, game.giantBombId);
            values.put(COL_NAME, game.name);
            values.put(COL_POSTER_URL, game.posterUrl);
            values.put(COL_RELEASE_DAY, game.releaseDate.getDay());
            values.put(COL_RELEASE_MONTH, game.releaseDate.getMonth());
            values.put(COL_RELEASE_QUARTER, game.releaseDate.getQuarter());
            values.put(COL_RELEASE_YEAR, game.releaseDate.getYear());
            values.put(COL_BLURB, game.blurb);
            values.put(COL_GAME_LIST_ID, GameListTable.TO_PLAY_ID);
            return values;
        }
    }

    // Platforms
    public static abstract class PlatformTable implements BaseColumns {
        public static final String TABLE_NAME = "platform";

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
    }

    // Join table mapping games <> platforms
    public static abstract class GamePlatformMappingTable {
        public static final String TABLE_NAME = "game_platform_mapping";

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
