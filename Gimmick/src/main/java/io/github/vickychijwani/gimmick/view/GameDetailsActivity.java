package io.github.vickychijwani.gimmick.view;

import android.app.ActionBar;
import android.content.ContentUris;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.util.Log;

import org.jetbrains.annotations.NotNull;

import io.github.vickychijwani.gimmick.R;
import io.github.vickychijwani.gimmick.database.DatabaseContract;
import io.github.vickychijwani.gimmick.item.Game;

public class GameDetailsActivity extends BaseActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final String TAG = "GameDetailsActivity";
    private static final int LAYOUT = R.layout.activity_game_details;
    private static final int LOADER_ID = LAYOUT;

    private int mGiantBombId;

    private DataFragment[] mFragments;

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
        GameOverviewFragment gameOverviewFragment = new GameOverviewFragment();
        mFragments = new DataFragment[] {
                gameOverviewFragment
        };
        String[] tabTitles = new String[] {
                getString(R.string.overview)
        };

        setupTabsAndViewPager(mFragments, tabTitles);

        getSupportLoaderManager().initLoader(LOADER_ID, null, this);
    }

    @Override
    protected void setupActionBar(@NotNull ActionBar actionBar) {
        actionBar.setDisplayHomeAsUpEnabled(true);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new CursorLoader(this,
                ContentUris.withAppendedId(DatabaseContract.GameTable.CONTENT_URI_LIST, mGiantBombId),
                null, null, null, null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        // Swap the new data set in (the loader will take care of closing the old cursor)
        cursor.moveToFirst();
        String gameName = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseContract.GameTable.COL_NAME));
        getActionBar().setTitle(gameName);

        Game game = new Game(cursor);

        for (DataFragment<Game> dataFragment : mFragments) {
            dataFragment.onDataLoaded(game);
        }
    }

    @Override
    public void onLoaderReset(Loader loader) {

    }

}
