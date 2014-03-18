package io.github.vickychijwani.giantbomb.item;

import org.jetbrains.annotations.NotNull;

public enum ResourceType {

    GAME(3030, "game", "games"),
    PLATFORM(3045, "platform", "platforms"),
    VIDEO(2300, "video", "videos"),
    REVIEW(1900, "review", "reviews"),
    ;

    private int mId;
    private String mSingularName;
    private String mPluralName;

    private ResourceType(int id, @NotNull String singularName, @NotNull String pluralName) {
        mId = id;
        mSingularName = singularName;
        mPluralName = pluralName;
    }

    public int getId() {
        return mId;
    }

    public String getSingularName() {
        return mSingularName;
    }

    public String getPluralName() {
        return mPluralName;
    }

}
