package io.github.vickychijwani.gimmick.database;

import android.text.TextUtils;

/**
 * Utilities for constructing SQL statements.
 */
public class SQL {

    // data types
    public enum Type {
        INTEGER,
        TEXT
    }

    // constraints
    public static class Constraint {
        public static final String PRIMARY_KEY = "PRIMARY KEY";
        public static final String PRIMARY_KEY_AUTOINCREMENT = PRIMARY_KEY + " AUTOINCREMENT";
        public static final String NOT_NULL = "NOT NULL";
        public static final String UNIQUE = "UNIQUE";
        public static       String FOREIGN_KEY(String tableName, String colName) { return "REFERENCES " + tableName + "(" + colName + ")"; }
        public static       String DEFAULT(String value) { return "DEFAULT " + value; }
        public static       String DEFAULT(int value) { return "DEFAULT " + value; }
    }

    // statements
    public static String CREATE_TABLE(String tableName, String... columnDefs) {
        return "CREATE TABLE " + tableName + "(" + TextUtils.join(",", columnDefs) + ");";
    }

    // other things
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

}
