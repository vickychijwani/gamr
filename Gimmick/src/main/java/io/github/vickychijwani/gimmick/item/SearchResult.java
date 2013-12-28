package io.github.vickychijwani.gimmick.item;

import android.database.Cursor;

import java.util.Comparator;
import java.util.Iterator;
import java.util.Set;
import java.util.TreeSet;

import io.github.vickychijwani.gimmick.database.DatabaseContract;

public class SearchResult {

    public String name = "";
    public int giantBombId = -1;
    public String giantBombUrl = "";
    public String posterUrl = "";
    public String blurb = "";
    public Set<Platform> platforms = new TreeSet<Platform>();
    public ReleaseDate releaseDate = null;

    public boolean isAdded;

    public SearchResult() {

    }

    /**
     * @param cursor    a database cursor from which to construct this object
     */
    public SearchResult(Cursor cursor) {
        giantBombId = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseContract.GameTable._ID));
        name = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseContract.GameTable.COL_NAME));
        posterUrl = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseContract.GameTable.COL_POSTER_URL));
        blurb = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseContract.GameTable.COL_BLURB));
        short day = cursor.getShort(cursor.getColumnIndexOrThrow(DatabaseContract.GameTable.COL_RELEASE_DAY));
        short month = cursor.getShort(cursor.getColumnIndexOrThrow(DatabaseContract.GameTable.COL_RELEASE_MONTH));
        short quarter = cursor.getShort(cursor.getColumnIndexOrThrow(DatabaseContract.GameTable.COL_RELEASE_QUARTER));
        short year = cursor.getShort(cursor.getColumnIndexOrThrow(DatabaseContract.GameTable.COL_RELEASE_YEAR));
        releaseDate = new ReleaseDate((byte) day, (byte) month, (byte) quarter, year);
    }

    public void addPlatform(Platform platform) {
        platforms.add(platform);
    }

    public Iterator<Platform> getPlatforms() {
        return platforms.iterator();
    }

    public CharSequence getPlatformsDisplayString() {
        StringBuilder builder = new StringBuilder();
        Iterator<Platform> it = platforms.iterator();
        while (it.hasNext()) {
            Platform platform = it.next();
            builder.append(platform.getShortName());
            if (it.hasNext())
                builder.append("  ");
        }
        return builder;
    }

    @Override
    public String toString() {
        return name;
    }

    public static class LatestFirstComparator implements Comparator<SearchResult> {
        @Override
        public int compare(SearchResult lhs, SearchResult rhs) {
            return -lhs.releaseDate.compareTo(rhs.releaseDate);
        }
    }

}
