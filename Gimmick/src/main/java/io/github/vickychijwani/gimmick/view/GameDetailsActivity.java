package io.github.vickychijwani.gimmick.view;

import android.app.ActionBar;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ContentUris;
import android.content.DialogInterface;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

import io.github.vickychijwani.giantbomb.item.Game;
import io.github.vickychijwani.giantbomb.item.Review;
import io.github.vickychijwani.giantbomb.item.Video;
import io.github.vickychijwani.gimmick.R;
import io.github.vickychijwani.gimmick.constants.LoaderId;
import io.github.vickychijwani.gimmick.database.DatabaseContract;
import io.github.vickychijwani.gimmick.database.GamrProvider;

public class GameDetailsActivity extends BaseActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final String TAG = "GameDetailsActivity";

    private int mGiantBombId;
    private Game mGame = null;

    private GameOverviewFragment    mGameOverviewFragment;
    private VideosFragment          mGameVideosFragment;
    private ReviewsFragment         mGameReviewsFragment;

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
        // if savedInstanceState is not null, Android would have re-created the fragments for us, in
        // which case we would have already saved references to them in onAttachFragment()
        if (savedInstanceState == null) {
            mGameOverviewFragment = new GameOverviewFragment();
            mGameVideosFragment = new VideosFragment();
            mGameReviewsFragment = new ReviewsFragment();
        }

        Fragment[] fragments = new Fragment[] {
                mGameOverviewFragment,
                mGameVideosFragment,
                mGameReviewsFragment,
        };
        String[] tabTitles = new String[] {
                getString(R.string.overview),
                getString(R.string.videos),
                getString(R.string.reviews),
        };
        setupTabsAndViewPager(fragments, tabTitles, 2);     // cache 2 tabs each on either side of the current one

        getSupportLoaderManager().initLoader(LoaderId.GAME_OVERVIEW, null, this);
        getSupportLoaderManager().initLoader(LoaderId.GAME_VIDEOS, null, this);
        getSupportLoaderManager().initLoader(LoaderId.GAME_REVIEWS, null, this);
    }

    @Override
    protected void setupActionBar(@NotNull ActionBar actionBar) {
        actionBar.setDisplayHomeAsUpEnabled(true);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.game_details, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_remove:
                String name = (mGame != null) ? mGame.name : "this game";
                DialogFragment removeDialog = ConfirmRemoveDialogFragment.newInstance(mGiantBombId, name);
                removeDialog.show(getSupportFragmentManager(), null);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onAttachFragment(Fragment fragment) {
        if (fragment instanceof GameOverviewFragment) {
            mGameOverviewFragment = (GameOverviewFragment) fragment;
        } else if (fragment instanceof VideosFragment) {
            mGameVideosFragment = (VideosFragment) fragment;
        } else if (fragment instanceof ReviewsFragment) {
            mGameReviewsFragment = (ReviewsFragment) fragment;
        }
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
            case LoaderId.GAME_REVIEWS:
                return new CursorLoader(this,
                        ContentUris.withAppendedId(DatabaseContract.GameTable.CONTENT_URI_GAME_REVIEWS, mGiantBombId),
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
                if (cursor.getCount() == 0) {
                    finish();   // game deleted
                    return;
                }
                cursor.moveToFirst();
                mGame = new Game(cursor);
                getActionBar().setTitle(mGame.name);
                mGameOverviewFragment.onDataLoaded(mGame);
                return;
            case LoaderId.GAME_VIDEOS:
                List<Video> videoList = new ArrayList<Video>(cursor.getCount());
                cursor.moveToPosition(-1);  // move to beginning
                while (cursor.moveToNext()) {
                    videoList.add(new Video(cursor));
                }
                mGameVideosFragment.onDataLoaded(videoList);
                return;
            case LoaderId.GAME_REVIEWS:
                List<Review> reviewList = new ArrayList<Review>(cursor.getCount());
                cursor.moveToPosition(-1);  // move to beginning
                while (cursor.moveToNext()) {
                    reviewList.add(new Review(cursor));
                }
                mGameReviewsFragment.onDataLoaded(reviewList);
                return;
            default:
                throw new IllegalArgumentException("Invalid loader id " + loader.getId());
        }
    }

    @Override
    public void onLoaderReset(Loader loader) {
        Log.d(TAG, "Resetting loader for game " + mGiantBombId + "!");
    }


    public static class ConfirmRemoveDialogFragment extends DialogFragment {

        private static final String ARG_GIANT_BOMB_ID = "giant_bomb_id";
        private static final String ARG_NAME = "name";

        public static ConfirmRemoveDialogFragment newInstance(int giantBombId, @NotNull String name) {
            ConfirmRemoveDialogFragment fragment = new ConfirmRemoveDialogFragment();
            Bundle args = new Bundle();
            args.putInt(ARG_GIANT_BOMB_ID, giantBombId);
            args.putString(ARG_NAME, name);
            fragment.setArguments(args);
            return fragment;
        }

        @NotNull
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            Bundle args = getArguments();
            final int giantBombId = args.getInt(ARG_GIANT_BOMB_ID);
            String name = args.getString(ARG_NAME);
            return new AlertDialog.Builder(getActivity())
                    .setMessage(getString(R.string.remove_from_library_confirmation, name))
                    .setPositiveButton(R.string.remove_yes, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            GamrProvider.removeGame(giantBombId);
                        }
                    })
                    .setNegativeButton(R.string.remove_no, null)
                    .create();
        }

    }

}
