package io.github.vickychijwani.gimmick.view;

import android.app.ActionBar;
import android.app.LoaderManager;
import android.content.ContentUris;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.meetme.android.multistateview.MultiStateView;

import org.jetbrains.annotations.NotNull;

import butterknife.ButterKnife;
import butterknife.InjectView;
import io.github.vickychijwani.gimmick.R;
import io.github.vickychijwani.gimmick.database.DatabaseContract;
import io.github.vickychijwani.gimmick.database.DatabaseContract.GameListTable;
import io.github.vickychijwani.gimmick.item.SearchResult;
import io.github.vickychijwani.gimmick.utility.NetworkUtils;

public class MainActivity extends BaseActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final int LAYOUT = R.layout.activity_main;
    private static final int LOADER_ID = LAYOUT;

    private ListView mGameList;
    private GameListAdapter mAdapter;

    @InjectView(R.id.list_container) MultiStateView mGameListContainer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(LAYOUT);
        ButterKnife.inject(this);

        mGameListContainer.setState(MultiStateView.ContentState.LOADING);
        mGameList = (ListView) mGameListContainer.getContentView();

        mAdapter = new GameListAdapter(this, null, 0, mItemClickListener);
        mGameList.setAdapter(mAdapter);
        mGameList.setEmptyView(findViewById(android.R.id.empty));

        getLoaderManager().initLoader(LOADER_ID, null, this);
    }

    @Override
    protected void setupActionBar(@NotNull ActionBar actionBar) {
        actionBar.setDisplayHomeAsUpEnabled(false);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
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
            Cursor cursor = (Cursor) mAdapter.getItem(position);
            assert cursor != null;
            int giantBomdId = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseContract.GameTable._ID));

            Intent intent = new Intent(MainActivity.this, GameDetailsActivity.class);
            intent.putExtra(GameDetailsActivity.IntentFields.GAME_GIANT_BOMB_ID, giantBomdId);
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
        mGameListContainer.setState(MultiStateView.ContentState.CONTENT);

        // Swap the new cursor in (the loader will take care of closing the old one)
        mAdapter.swapCursor(cursor);
    }

    @Override
    public void onLoaderReset(Loader loader) {
        mGameListContainer.setState(MultiStateView.ContentState.CONTENT);
        mAdapter.swapCursor(null);
    }

    private class GameListAdapter extends CursorAdapter {

        private static final int LAYOUT = R.layout.component_game_item;
        private final LayoutInflater mLayoutInflater;
        private final View.OnClickListener mClickListener;

        public GameListAdapter(Context context, Cursor cursor, int flags,
                          View.OnClickListener clickListener) {
            super(context, cursor, flags);
            mLayoutInflater = (LayoutInflater) context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            mClickListener = clickListener;
        }

        @NotNull
        @Override
        public View newView(Context context, Cursor cursor, ViewGroup parent) {
            View view = mLayoutInflater.inflate(LAYOUT, null);
            assert view != null;

            View detailsView = view.findViewById(R.id.details);

            view.setTag(R.id.poster, view.findViewById(R.id.poster));
            view.setTag(R.id.title, view.findViewById(R.id.title));
            view.setTag(R.id.release_date, view.findViewById(R.id.release_date));
            view.setTag(R.id.details, detailsView);
            view.setTag(R.id.platforms, view.findViewById(R.id.platforms));

            detailsView.setOnClickListener(mClickListener);

            return view;
        }

        @Override
        public void bindView(View view, Context context, Cursor cursor) {
            ImageView poster = (ImageView) view.getTag(R.id.poster);
            TextView title = (TextView) view.getTag(R.id.title);
            TextView releaseDate = (TextView) view.getTag(R.id.release_date);
            TextView platforms = (TextView) view.getTag(R.id.platforms);

            assert poster != null && title != null && releaseDate != null && platforms != null;
            SearchResult game = new SearchResult(cursor);

            NetworkUtils.loadImage(game.posterUrl, poster);
            title.setText(game.name);
            releaseDate.setText(game.releaseDate.toString());
            platforms.setText(game.getPlatformsDisplayString());
        }

    }


}
