package io.github.vickychijwani.giantbomb.item;

public class ResourceType {

    private int mId;
    private String mSingularName;
    private String mPluralName;

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
