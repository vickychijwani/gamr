package io.github.vickychijwani.gimmick.item;

import android.database.Cursor;
import android.text.Html;
import android.text.TextUtils;

import org.jetbrains.annotations.NotNull;

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
    public String smallPosterUrl = "";
    public String blurb = "";
    public ReleaseDate releaseDate = null;
    public Set<Platform> platforms = new TreeSet<Platform>();
    public Set<String> genres = new TreeSet<String>();

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
        releaseDate = new ReleaseDate(cursor);

        String platformsCsv = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseContract.GameTable.COL_PSEUDO_PLATFORMS));
        assert platformsCsv != null;
        platforms = Platform.fromCsv(platformsCsv);

        try {
            blurb = Html.fromHtml(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseContract.GameTable.COL_BLURB))).toString();
        } catch (IllegalArgumentException ignored) { }

        try {
            smallPosterUrl = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseContract.GameTable.COL_SMALL_POSTER_URL));
        } catch (IllegalArgumentException ignored) { }

        try {
            String genresCsv = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseContract.GameTable.COL_GENRES));
            assert genresCsv != null;
        } catch (IllegalArgumentException ignored) { }
    }

    public void addPlatform(Platform platform) {
        platforms.add(platform);
    }

    public void addGenre(@NotNull String genre) {
        genres.add(genre);
    }

    public Iterator<Platform> getPlatforms() {
        return platforms.iterator();
    }

    public String getPlatformsDisplayString() {
        return TextUtils.join("  ", platforms);
    }

    public String getGenresDisplayString() {
        return TextUtils.join(", ", genres);
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
