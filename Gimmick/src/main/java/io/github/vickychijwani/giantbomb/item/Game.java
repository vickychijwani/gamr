package io.github.vickychijwani.giantbomb.item;

import android.database.Cursor;
import android.text.Html;
import android.text.TextUtils;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import io.github.vickychijwani.gimmick.database.DatabaseContract.GameTable;
import io.github.vickychijwani.gimmick.database.GamrProvider;
import io.github.vickychijwani.utility.DateTimeUtils;

public class Game {

    public String name = "";
    public int giantBombId = -1;
    public String posterUrl = "";
    public String smallPosterUrl = "";
    public String blurb = "";
    public ReleaseDate releaseDate = null;
    public Set<Platform> platforms = new TreeSet<Platform>();

    public short metascore = -1;
    public Set<String> genres = new TreeSet<String>();
    public Set<String> franchises = new TreeSet<String>();
    public List<Video> videos = new ArrayList<Video>();
    public List<Review> reviews = new ArrayList<Review>();

    public boolean isAdded;

    public Game() {

    }

    /**
     * @param cursor    a database cursor from which to construct this object
     */
    public Game(Cursor cursor) {
        giantBombId = cursor.getInt(cursor.getColumnIndexOrThrow(GameTable._ID));
        name = cursor.getString(cursor.getColumnIndexOrThrow(GameTable.COL_NAME));
        posterUrl = cursor.getString(cursor.getColumnIndexOrThrow(GameTable.COL_POSTER_URL));
        releaseDate = new ReleaseDate(cursor);

        String platformIdsCsv = cursor.getString(cursor.getColumnIndexOrThrow(GameTable.COL_PSEUDO_PLATFORMS));
        if (platformIdsCsv != null) {
            String[] platformIds = platformIdsCsv.split(",");
            for (String platformId : platformIds) {
                platforms.add(GamrProvider.getPlatform(Integer.parseInt(platformId)));
            }
        }

        int colIndex;

        colIndex = cursor.getColumnIndex(GameTable.COL_BLURB);
        if (colIndex >= 0) {
            blurb = Html.fromHtml(cursor.getString(colIndex)).toString();
        }

        colIndex = cursor.getColumnIndex(GameTable.COL_SMALL_POSTER_URL);
        if (colIndex >= 0) {
            smallPosterUrl = cursor.getString(colIndex);
        }

        colIndex = cursor.getColumnIndex(GameTable.COL_METASCORE);
        if (colIndex >= 0) {
            metascore = cursor.getShort(colIndex);
        }

        colIndex = cursor.getColumnIndex(GameTable.COL_GENRES);
        if (colIndex >= 0) {
            String genresCsv = cursor.getString(colIndex);
            assert genresCsv != null;
            genres = new TreeSet<String>(Arrays.asList(genresCsv.split(", ")));
        }

        colIndex = cursor.getColumnIndex(GameTable.COL_FRANCHISES);
        if (colIndex >= 0) {
            String franchisesCsv = cursor.getString(colIndex);
            assert franchisesCsv != null;
            franchises = new TreeSet<String>(Arrays.asList(franchisesCsv.split(", ")));
        }
    }

    public boolean isReleased() {
        // game is released if its release date occurs before today's date
        return releaseDate.compareTo(new ReleaseDate(DateTimeUtils.getCurrentDate())) < 0;
    }

    public void addPlatform(Platform platform) {
        platforms.add(platform);
    }

    public void addGenre(@NotNull String genre) {
        genres.add(genre);
    }

    public void addFranchise(@NotNull String franchise) {
        franchises.add(franchise);
    }

    public void addVideo(@NotNull Video video) {
        video.setGameId(giantBombId);
        videos.add(video);
    }

    public void addReview(@NotNull Review review) {
        review.setGameId(giantBombId);
        reviews.add(review);
    }

    public Iterator<Platform> getPlatforms() {
        return platforms.iterator();
    }

    public Iterator<Video> getVideos() {
        return videos.iterator();
    }

    public Iterator<Review> getReviews() {
        return reviews.iterator();
    }

    public String getPlatformsDisplayString() {
        return TextUtils.join("  ", platforms);
    }

    public String getGenresDisplayString() {
        return TextUtils.join(", ", genres);
    }

    public String getFranchisesDisplayString() {
        return TextUtils.join(", ", franchises);
    }

    @Override
    public String toString() {
        return name;
    }

}
