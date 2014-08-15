package io.github.vickychijwani.giantbomb.item;

import android.database.Cursor;
import android.text.TextUtils;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import io.github.vickychijwani.gimmick.database.DatabaseContract.PlatformTable;

public class Platform implements Comparable<Platform> {

    private int mGiantBombId;
    private String mName;
    private String mShortName;
    private final Set<String> mAliases = new HashSet<String>();
    private int mMetacriticId;  // move this out (or don't? or simply merge metacritic and giantbomb code?)

    public Platform() { }

    public Platform(Cursor cursor) {
        mGiantBombId = cursor.getInt(cursor.getColumnIndexOrThrow(PlatformTable._ID));
        mName = cursor.getString(cursor.getColumnIndexOrThrow(PlatformTable.COL_NAME));
        mShortName = cursor.getString(cursor.getColumnIndexOrThrow(PlatformTable.COL_SHORT_NAME));
        String aliasesJoined = cursor.getString(cursor.getColumnIndexOrThrow(PlatformTable.COL_ALIASES));
        if (aliasesJoined != null) {
            mAliases.addAll(Arrays.asList(TextUtils.split(aliasesJoined, "\n")));
        }
    }

    @Override
    public int compareTo(@NotNull Platform another) {
        // newer platforms tend to have larger IDs
        return -((Integer) this.mGiantBombId).compareTo(another.mGiantBombId);
    }

    public int getGiantBombId() {
        return mGiantBombId;
    }

    public void setGiantBombId(int giantBombId) {
        mGiantBombId = giantBombId;
    }

    public int getMetacriticId() {
        return mMetacriticId;
    }

    public void setMetacriticId(int metacriticId) {
        mMetacriticId = metacriticId;
    }

    @Nullable
    public String getName() {
        return mName;
    }

    public void setName(@Nullable String name) {
        mName = name;
    }

    @Nullable
    public String getShortName() {
        return mShortName;
    }

    public void setShortName(@Nullable String shortName) {
        mShortName = shortName;
    }

    @NotNull
    public Set<String> getAliases() {
        return mAliases;
    }

    public void setAliases(@Nullable Collection<String> aliases) {
        mAliases.clear();
        if (aliases != null) {
            mAliases.addAll(aliases);
        }
    }

    @Override
    public String toString() {
        return mShortName;
    }

}
