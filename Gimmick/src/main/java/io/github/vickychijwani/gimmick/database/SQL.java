package io.github.vickychijwani.gimmick.database;

import android.text.TextUtils;

import org.jetbrains.annotations.NotNull;

/**
 * Utilities for constructing SQL statements.
 */
public class SQL {

    // utils for creating new tables
    public enum Type {
        INTEGER,
        TEXT
    }

    public static class Constraint {
        public static final String PRIMARY_KEY = "PRIMARY KEY";
        public static final String PRIMARY_KEY_AUTOINCREMENT = PRIMARY_KEY + " AUTOINCREMENT";
        public static final String NOT_NULL = "NOT NULL";
        public static final String UNIQUE = "UNIQUE";
        public static       String FOREIGN_KEY(String tableName, String colName) { return "REFERENCES " + tableName + "(" + colName + ")"; }
        public static       String DEFAULT(String value) { return "DEFAULT " + value; }
        public static       String DEFAULT(int value) { return "DEFAULT " + value; }
    }

    public static String CREATE_TABLE(String tableName, String... columnDefs) {
        return "CREATE TABLE " + tableName + "(" + TextUtils.join(",", columnDefs) + ");";
    }

    public static String DEF_COL(String colName, Type colType, String... colConstraints) {
        return colName + " " + colType + " " + TextUtils.join(" ", colConstraints);
    }

    public static String DEF_PRIMARY_KEY(String colName, Type colType) {
        return DEF_COL(colName, colType, Constraint.PRIMARY_KEY);
    }

    public static String DEF_PRIMARY_KEY_AUTOINCREMENT(String colName, Type colType) {
        return DEF_COL(colName, colType, Constraint.PRIMARY_KEY_AUTOINCREMENT);
    }

    public static String DEF_FOREIGN_KEY_NOT_NULL(String sourceColName, String targetTableName, String targetColName) {
        return DEF_COL(sourceColName, Type.INTEGER, Constraint.NOT_NULL, Constraint.FOREIGN_KEY(targetTableName, targetColName));
    }

    public static String DEF_COMPOSITE_KEY(String... colNames) {
        return "PRIMARY KEY (" + TextUtils.join(",", colNames) + ")";
    }

    // utils for querying
    abstract static class Condition {
        public final String operator;
        public final String leftOperand;
        public final String rightOperand;

        protected Condition(String operator, String leftOperand, String rightOperand) {
            this.operator = operator;
            this.leftOperand = leftOperand;
            this.rightOperand = rightOperand;
        }

        @Override
        public String toString() {
            return leftOperand + operator + rightOperand;
        }
    }

    static class Eq extends Condition {
        public Eq(String leftOperand, String rightOperand) {
            super("=", leftOperand, rightOperand);
        }

        public Eq(String leftOperand, long rightOperand) {
            super("=", leftOperand, String.valueOf(rightOperand));
        }

        public Eq(String leftOperand, Double rightOperand) {
            super("=", leftOperand, String.valueOf(rightOperand));
        }
    }

    /**
     * Construct a SQLite GROUP_CONCAT() clause.
     *
     * @param colName       name of column on which to apply GROUP_CONCAT()
     * @param asColName     new name of column, after application of clause
     * @return              raw string usable as a projection argument in a SQLite query
     */
    @NotNull
    public static String groupConcat(@NotNull String colName, @NotNull String asColName) {
        return "GROUP_CONCAT(" + colName + ") AS " + asColName;
    }

}
