package io.github.vickychijwani.giantbomb.item;

import android.database.Cursor;
import android.util.Log;

import java.text.ParseException;
import java.util.Date;

import io.github.vickychijwani.gimmick.database.DatabaseContract.ReviewTable;
import io.github.vickychijwani.utility.DateTimeUtils;

public class Review implements Comparable<Review> {
    private static final String TAG = "Video";

    private int mGiantBombId = -1;
    private String mReviewer = "";
    private int mGameId = -1;
    private String mTitle = "";
    private double mScore = -1.0;  // out of 5.0
    private Date mPublishDate;
    private String mSiteUrl = "";

    public Review() { }

    public Review(Cursor cursor) {
        mGiantBombId = cursor.getInt(cursor.getColumnIndexOrThrow(ReviewTable._ID));
        mReviewer = cursor.getString(cursor.getColumnIndexOrThrow(ReviewTable.COL_REVIEWER));
        mGameId = cursor.getInt(cursor.getColumnIndexOrThrow(ReviewTable.COL_GAME_ID));
        mTitle = cursor.getString(cursor.getColumnIndexOrThrow(ReviewTable.COL_TITLE));
        mScore = cursor.getFloat(cursor.getColumnIndexOrThrow(ReviewTable.COL_SCORE));
        mSiteUrl = cursor.getString(cursor.getColumnIndexOrThrow(ReviewTable.COL_SITE_URL));
        try {
            mPublishDate = DateTimeUtils.isoDateStringToDate(cursor.getString(cursor.getColumnIndexOrThrow(ReviewTable.COL_PUBLISH_DATE)));
        } catch (ParseException e) {
            Log.e(TAG, Log.getStackTraceString(e));
        }
    }

    @Override
    public int compareTo(Review another) {
        if (this.mPublishDate != null && another.mPublishDate != null) {
            return this.mPublishDate.compareTo(another.mPublishDate);
        }
        return 0;
    }

    // getters / setters
    public int getGiantBombId() {
        return mGiantBombId;
    }

    public void setGiantBombId(int giantBombId) {
        mGiantBombId = giantBombId;
    }

    public String getReviewer() {
        return mReviewer;
    }

    public void setReviewer(String reviewer) {
        mReviewer = reviewer;
    }

    public int getGameId() {
        return mGameId;
    }

    public void setGameId(int gameId) {
        mGameId = gameId;
    }

    public String getTitle() {
        return mTitle;
    }

    public void setTitle(String title) {
        mTitle = title;
    }

    public double getScore() {
        return mScore;
    }

    public void setScore(double score) {
        mScore = score;
    }

    public Date getPublishDate() {
        return mPublishDate;
    }

    public void setPublishDate(Date publishDate) {
        mPublishDate = publishDate;
    }

    public String getSiteUrl() {
        return mSiteUrl;
    }

    public void setSiteUrl(String siteUrl) {
        mSiteUrl = siteUrl;
    }

}
