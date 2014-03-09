package io.github.vickychijwani.giantbomb.item;

import android.database.Cursor;

import io.github.vickychijwani.gimmick.database.DatabaseContract.ResourceTypeTable;

public class ResourceType {

    private int mId;
    private String mSingularName;
    private String mPluralName;

    public ResourceType() { }

    public ResourceType(Cursor cursor) {
        mId = cursor.getInt(cursor.getColumnIndexOrThrow(ResourceTypeTable._ID));
        mSingularName = cursor.getString(cursor.getColumnIndexOrThrow(ResourceTypeTable.COL_DETAIL_RESOURCE_NAME));
        mPluralName = cursor.getString(cursor.getColumnIndexOrThrow(ResourceTypeTable.COL_LIST_RESOURCE_NAME));
    }

    public int getId() {
        return mId;
    }

    public void setId(int id) {
        mId = id;
    }

    public String getSingularName() {
        return mSingularName;
    }

    public void setSingularName(String singularName) {
        mSingularName = singularName;
    }

    public String getPluralName() {
        return mPluralName;
    }

    public void setPluralName(String pluralName) {
        mPluralName = pluralName;
    }

}
