package io.github.vickychijwani.gimmick.view;

import android.app.ActionBar;
import android.app.LoaderManager;
import android.content.ContentUris;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;

import com.meetme.android.multistateview.MultiStateView;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

import butterknife.ButterKnife;
import butterknife.InjectView;
import io.github.vickychijwani.gimmick.R;
import io.github.vickychijwani.gimmick.adapter.GameListAdapter;
import io.github.vickychijwani.gimmick.database.DatabaseContract.GameListTable;
import io.github.vickychijwani.gimmick.item.SearchResult;
import io.github.vickychijwani.gimmick.utility.AppUtils;

public class LibraryActivity extends BaseActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final int LAYOUT = R.layout.activity_library;
    private static final int LOADER_ID = LAYOUT;
    private static final int LOADING_STATE_DELAY = 500;  // time after which to display loading spinner

    private GameListAdapter mAdapter;
    private Handler mHandler;

    @InjectView(android.R.id.list) ListView mGameList;
    @InjectView(R.id.list_container) MultiStateView mGameListContainer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(LAYOUT);
        ButterKnife.inject(this);

        // show loading spinner after a delay, to avoid flicker if loading time is < {@link #LOADING_STATE_DELAY}
        mHandler = new Handler(getMainLooper());
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                mGameListContainer.setState(MultiStateView.ContentState.LOADING);
            }
        }, LOADING_STATE_DELAY);

        mAdapter = new GameListAdapter(this, new ArrayList<SearchResult>(), mItemClickListener);
        mGameList.setAdapter(mAdapter);

        getLoaderManager().initLoader(LOADER_ID, null, this);
    }

    @Override
    protected void setupActionBar(@NotNull ActionBar actionBar) {
        actionBar.setDisplayHomeAsUpEnabled(false);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.library, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_add:
                Intent addIntent = new Intent(this, AddGamesActivity.class);
                startActivity(addIntent);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private final View.OnClickListener mItemClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            int position = mGameList.getPositionForView(v);
            SearchResult game = mAdapter.getItem(position);

            Intent intent = new Intent(LibraryActivity.this, GameDetailsActivity.class);
            intent.putExtra(GameDetailsActivity.IntentFields.GAME_GIANT_BOMB_ID, game.giantBombId);
            startActivity(intent);
        }
    };

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new CursorLoader(this,
                ContentUris.withAppendedId(GameListTable.CONTENT_URI_LIST_GAMES, GameListTable.TO_PLAY_ID),
                null, null, null, null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        mHandler.removeCallbacksAndMessages(null);

        if (cursor.getCount() > 0) {
            mGameListContainer.setState(MultiStateView.ContentState.CONTENT);
        } else {
            mGameListContainer.setState(MultiStateView.ContentState.EMPTY);
        }

        // Swap the new data set in (the loader will take care of closing the old cursor)
        AppUtils.changeAdapterDataSet(mAdapter, SearchResult.listFromCursor(cursor));
    }

    @Override
    public void onLoaderReset(Loader loader) {
        mGameListContainer.setState(MultiStateView.ContentState.EMPTY);
        AppUtils.changeAdapterDataSet(mAdapter, null);
    }

}
