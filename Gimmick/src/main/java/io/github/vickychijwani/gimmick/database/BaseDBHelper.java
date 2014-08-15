package io.github.vickychijwani.gimmick.database;

import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;
import android.text.TextUtils;
import android.util.Log;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

abstract class BaseDBHelper extends SQLiteOpenHelper {

    protected static final String TAG = "DBHelper";

    protected static BaseDBHelper sInstance = null;

    protected BaseDBHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
    }

    public static BaseDBHelper getInstance() {
        assert sInstance != null;
        return sInstance;
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
    protected static BaseSelectStatement select(@Nullable String... colNames) {
        assert sInstance != null;
        if (colNames != null) {
            return new CursorSelectStatement(TextUtils.join(",", colNames));
        } else {
            return selectAll();
        }
    }

    protected abstract static class BaseSelectStatement {
        private final String mProjection;
        private String mTableName;
        private String mSelection;
        private String mGrouping;
        private String mOrderBy;
        private final List<JoinClause> mJoinClauses = new ArrayList<JoinClause>();

        private class JoinClause {
            public String type;
            public String tableName;
            public SQL.Condition onCondition;
        }

        public BaseSelectStatement(@NotNull String projection) {
            mProjection = projection;
        }

        @NotNull
        public BaseSelectStatement from(@NotNull String tableName) {
            mTableName = tableName;
            return this;
        }

        @NotNull
        public BaseSelectStatement innerJoin(@NotNull String tableName, @NotNull SQL.Condition onCondition) {
            return join("INNER JOIN", tableName, onCondition);
        }

        @NotNull
        public BaseSelectStatement leftOuterJoin(@NotNull String tableName, @NotNull SQL.Condition onCondition) {
            return join("LEFT OUTER JOIN", tableName, onCondition);
        }

        @NotNull
        public BaseSelectStatement rightOuterJoin(@NotNull String tableName, @NotNull SQL.Condition onCondition) {
            return join("RIGHT OUTER JOIN", tableName, onCondition);
        }

        @NotNull
        private BaseSelectStatement join(@NotNull String joinType, @NotNull String tableName, @NotNull SQL.Condition onCondition) {
            JoinClause joinClause = new JoinClause();
            joinClause.type = joinType;
            joinClause.tableName = tableName;
            joinClause.onCondition = onCondition;
            mJoinClauses.add(joinClause);
            return this;
        }

        @NotNull
        public BaseSelectStatement where(@Nullable SQL.Condition whereCondition) {
            if (whereCondition != null) {
                return where(whereCondition.toString());
            }
            return this;
        }

        @NotNull
        public BaseSelectStatement where(@Nullable String whereCondition) {
            if (whereCondition != null) {
                mSelection = (TextUtils.isEmpty(mSelection)) ? "" : (mSelection + " AND ");
                mSelection += whereCondition;
            }
            return this;
        }

        @NotNull
        public BaseSelectStatement groupBy(@NotNull String colName) {
            mGrouping = colName;
            return this;
        }

        @NotNull
        public BaseSelectStatement orderBy(@Nullable String colName) {
            mOrderBy = colName;
            return this;
        }

        @NotNull
        public abstract Object execute();

        @NotNull
        public String buildQuery() {
            //noinspection ConstantConditions
            if (mProjection == null || mTableName == null) {
                throw new IllegalStateException("Query not completely built");
            }

            String query = " SELECT " + mProjection
                    + " FROM " + mTableName;

            for (JoinClause c : mJoinClauses) {
                query += " " + c.type + " " + c.tableName + " ON " + c.onCondition.toString();
            }

            if (! TextUtils.isEmpty(mSelection))
                query += " WHERE " + mSelection;

            if (! TextUtils.isEmpty(mGrouping))
                query += " GROUP BY " + mGrouping;

            if (! TextUtils.isEmpty(mOrderBy))
                query += " ORDER BY " + mOrderBy;

            Log.i(TAG, "SQL query: " + query);

            return query;
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
