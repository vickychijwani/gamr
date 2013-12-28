package io.github.vickychijwani.gimmick.database;

import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;
import android.text.TextUtils;

import org.jetbrains.annotations.NotNull;

abstract class BaseDBHelper extends SQLiteOpenHelper {

    protected static BaseDBHelper sInstance = null;

    protected BaseDBHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
    }

    @NotNull
    protected static BaseSelectStatement selectAll() {
        assert sInstance != null;
        return new CursorSelectStatement("*");
    }

    @NotNull
    protected static BaseSelectStatement selectId() {
        assert sInstance != null;
        return new LongSelectStatement(BaseColumns._ID);
    }

    @NotNull
    protected static BaseSelectStatement selectString(String colName) {
        assert sInstance != null;
        return new StringSelectStatement(colName);
    }

    @NotNull
    protected static BaseSelectStatement select(@NotNull String... colNames) {
        assert sInstance != null;
        return new CursorSelectStatement(TextUtils.join(",", colNames));
    }

    protected abstract static class BaseSelectStatement {
        private String mProjection;
        private String mTableName;
        private String mSelection;

        public BaseSelectStatement(@NotNull String projection) {
            mProjection = projection;
        }

        @NotNull
        public BaseSelectStatement from(@NotNull String tableName) {
            mTableName = tableName;
            return this;
        }

        @NotNull
        public BaseSelectStatement whereEquals(@NotNull String colName, @NotNull String value) {
            mSelection = (mSelection == null) ? "" : (mSelection + " AND ");
            mSelection += colName + " = '" + value + "'";
            return this;
        }

        @NotNull
        public BaseSelectStatement whereEquals(@NotNull String colName, @NotNull Integer value) {
            return whereEquals(colName, Double.valueOf(value));
        }

        @NotNull
        public BaseSelectStatement whereEquals(@NotNull String colName, @NotNull Long value) {
            return whereEquals(colName, Double.valueOf(value));
        }

        @NotNull
        public BaseSelectStatement whereEquals(@NotNull String colName, @NotNull Double value) {
            mSelection = (mSelection == null) ? "" : (mSelection + " AND ");
            mSelection += colName + " = " + value.toString();
            return this;
        }

        @NotNull
        public abstract Object execute();

        @NotNull
        protected String buildQuery() {
            if (mProjection == null || mTableName == null || mSelection == null) {
                throw new IllegalStateException("Query not completely built");
            }

            return " SELECT " + mProjection
                    + " FROM " + mTableName
                    + " WHERE " + mSelection;
        }
    }

    protected static class CursorSelectStatement extends BaseSelectStatement {
        public CursorSelectStatement(@NotNull String projection) {
            super(projection);
        }

        @NotNull @Override
        public Cursor execute() {
            String query = buildQuery();

            SQLiteDatabase db = sInstance.getReadableDatabase();
            assert db != null;

            return db.rawQuery(query, null);
        }
    }

    protected static class LongSelectStatement extends BaseSelectStatement {
        public LongSelectStatement(@NotNull String projection) {
            super(projection);
        }

        @NotNull @Override
        public Long execute() {
            String query = buildQuery();

            SQLiteDatabase db = sInstance.getReadableDatabase();
            assert db != null;

            return DatabaseUtils.longForQuery(db, query, null);
        }
    }

    protected static class StringSelectStatement extends BaseSelectStatement {
        public StringSelectStatement(@NotNull String projection) {
            super(projection);
        }

        @NotNull @Override
        public String execute() {
            String query = buildQuery();

            SQLiteDatabase db = sInstance.getReadableDatabase();
            assert db != null;

            return DatabaseUtils.stringForQuery(db, query, null);
        }
    }

}
