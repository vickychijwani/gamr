package io.github.vickychijwani.gimmick.view;

import android.app.ActionBar;
import android.content.ContentUris;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.util.Log;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

import io.github.vickychijwani.gimmick.R;
import io.github.vickychijwani.gimmick.constants.LoaderId;
import io.github.vickychijwani.gimmick.database.DatabaseContract;
import io.github.vickychijwani.gimmick.item.Game;
import io.github.vickychijwani.gimmick.item.Video;

public class GameDetailsActivity extends BaseActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final String TAG = "GameDetailsActivity";

    private int mGiantBombId;

    private GameOverviewFragment    mGameOverviewFragment;
    private VideosFragment          mGameVideosFragment;

    public interface IntentFields {
        String GAME_GIANT_BOMB_ID = "game_giant_bomb_id";
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game_details);

        if (getIntent() == null) {
            finish();
            return;
        }

        mGiantBombId = getIntent().getIntExtra(IntentFields.GAME_GIANT_BOMB_ID, -1);
        if (mGiantBombId == -1) {
            finish();
            return;
        }

        Log.i(TAG, "Displaying details for game ID = " + mGiantBombId);

        // setup fragments
        mGameOverviewFragment = new GameOverviewFragment();
        mGameVideosFragment = new VideosFragment();
        setupTabsAndViewPager(new Fragment[] {
                mGameOverviewFragment,
                mGameVideosFragment
        }, new String[] {
                getString(R.string.overview),
                getString(R.string.videos)
        }, 2);

        getSupportLoaderManager().initLoader(LoaderId.GAME_OVERVIEW, null, this);
        getSupportLoaderManager().initLoader(LoaderId.GAME_VIDEOS, null, this);
    }

    @Override
    protected void setupActionBar(@NotNull ActionBar actionBar) {
        actionBar.setDisplayHomeAsUpEnabled(true);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        switch (id) {
            case LoaderId.GAME_OVERVIEW:
                return new CursorLoader(this,
                        ContentUris.withAppendedId(DatabaseContract.GameTable.CONTENT_URI_LIST, mGiantBombId),
                        null, null, null, null);
            case LoaderId.GAME_VIDEOS:
                return new CursorLoader(this,
                        ContentUris.withAppendedId(DatabaseContract.GameTable.CONTENT_URI_GAME_VIDEOS, mGiantBombId),
                        null, null, null, null);
            default:
                throw new IllegalArgumentException("Invalid loader id " + id);
        }
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        // Swap the new data set in (the loader will take care of closing the old cursor)
        switch (loader.getId()) {
            case LoaderId.GAME_OVERVIEW:
                cursor.moveToFirst();
                Game game = new Game(cursor);
                getActionBar().setTitle(game.name);
                mGameOverviewFragment.onDataLoaded(new Game(cursor));
                return;
            case LoaderId.GAME_VIDEOS:
                List<Video> videoList = new ArrayList<Video>(cursor.getCount());
                while (cursor.moveToNext()) {
                    videoList.add(new Video(cursor));
                }
                mGameVideosFragment.onDataLoaded(videoList);
                return;
            default:
                throw new IllegalArgumentException("Invalid loader id " + loader.getId());
        }
    }

    @Override
    public void onLoaderReset(Loader loader) {
        // this should never happen
    }

}
