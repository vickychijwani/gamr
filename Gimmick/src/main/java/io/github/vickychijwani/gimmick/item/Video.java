package io.github.vickychijwani.gimmick.item;

import android.database.Cursor;
import android.util.Log;

import java.text.ParseException;
import java.util.Date;

import io.github.vickychijwani.gimmick.database.DatabaseContract.VideoTable;
import io.github.vickychijwani.gimmick.utility.DateTimeUtils;

public class Video implements Comparable<Video> {

    private static final String TAG = "Video";

    private String mName = "";
    private int mGameId = -1;
    private String mBlurb = "";
    private int mGiantBombId = -1;
    private String mGiantBombUrl = "";
    private String mLowUrl = "";
    private String mHighUrl = "";
    private int mDuration = -1;  // in seconds
    private String mThumbUrl = "";
    private String mUser = "";
    private String mType = "";
    private String mYoutubeId = "";
    private Date mPublishDate;

    public Video() { }

    public Video(Cursor cursor) {
        mName = cursor.getString(cursor.getColumnIndexOrThrow(VideoTable.COL_NAME));
        mGameId = cursor.getInt(cursor.getColumnIndexOrThrow(VideoTable.COL_GAME_ID));
        mBlurb = cursor.getString(cursor.getColumnIndexOrThrow(VideoTable.COL_BLURB));
        mGiantBombId = cursor.getInt(cursor.getColumnIndexOrThrow(VideoTable._ID));
        mLowUrl = cursor.getString(cursor.getColumnIndexOrThrow(VideoTable.COL_LOW_URL));
        mHighUrl = cursor.getString(cursor.getColumnIndexOrThrow(VideoTable.COL_HIGH_URL));
        mDuration = cursor.getInt(cursor.getColumnIndexOrThrow(VideoTable.COL_DURATION));
        mThumbUrl = cursor.getString(cursor.getColumnIndexOrThrow(VideoTable.COL_THUMB_URL));
        mUser = cursor.getString(cursor.getColumnIndexOrThrow(VideoTable.COL_USER));
        mType = cursor.getString(cursor.getColumnIndexOrThrow(VideoTable.COL_TYPE));
        mYoutubeId = cursor.getString(cursor.getColumnIndexOrThrow(VideoTable.COL_YOUTUBE_ID));
        try {
            mPublishDate = DateTimeUtils.isoDateStringToDate(cursor.getString(cursor.getColumnIndexOrThrow(VideoTable.COL_PUBLISH_DATE)));
        } catch (ParseException e) {
            Log.e(TAG, Log.getStackTraceString(e));
        }
    }

    @Override
    public int compareTo(Video another) {
        if (this.mPublishDate != null && another.mPublishDate != null) {
            return this.mPublishDate.compareTo(another.mPublishDate);
        }
        return 0;
    }

    // getters / setters
    public String getName() {
        return mName;
    }

    public void setName(String name) {
        mName = name;
    }

    public int getGameId() {
        return mGameId;
    }

    public void setGameId(int gameId) {
        mGameId = gameId;
    }

    public String getBlurb() {
        return mBlurb;
    }

    public void setBlurb(String blurb) {
        mBlurb = blurb;
    }

    public int getGiantBombId() {
        return mGiantBombId;
    }

    public void setGiantBombId(int giantBombId) {
        mGiantBombId = giantBombId;
    }

    public String getGiantBombUrl() {
        return mGiantBombUrl;
    }

    public void setGiantBombUrl(String giantBombUrl) {
        mGiantBombUrl = giantBombUrl;
    }

    public String getLowUrl() {
        return mLowUrl;
    }

    public void setLowUrl(String lowUrl) {
        mLowUrl = lowUrl;
    }

    public String getHighUrl() {
        return mHighUrl;
    }

    public void setHighUrl(String highUrl) {
        mHighUrl = highUrl;
    }

    public int getDuration() {
        return mDuration;
    }

    public void setDuration(int duration) {
        mDuration = duration;
    }

    public String getThumbUrl() {
        return mThumbUrl;
    }

    public void setThumbUrl(String thumbUrl) {
        mThumbUrl = thumbUrl;
    }

    public String getUser() {
        return mUser;
    }

    public void setUser(String user) {
        mUser = user;
    }

    public String getType() {
        return mType;
    }

    public void setType(String type) {
        mType = type;
    }

    public String getYoutubeId() {
        return mYoutubeId;
    }

    public void setYoutubeId(String youtubeId) {
        mYoutubeId = youtubeId;
    }

    public Date getPublishDate() {
        return mPublishDate;
    }

    public void setPublishDate(Date publishDate) {
        mPublishDate = publishDate;
    }

}
