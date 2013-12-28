package io.github.vickychijwani.gimmick.view;

import android.app.ActionBar;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import io.github.vickychijwani.gimmick.R;
import io.github.vickychijwani.gimmick.database.DBHelper;
import io.github.vickychijwani.gimmick.database.DatabaseContract;
import io.github.vickychijwani.gimmick.item.SearchResult;
import io.github.vickychijwani.gimmick.viewholder.SearchResultViewHolder;

public class MainActivity extends BaseActivity {

    private ListView mGameList;
    private GameListAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        DBHelper.createInstance(this);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // TODO use a Loader to load results in the background! Also make sure they update automatically!
        mAdapter = new GameListAdapter(this, R.layout.game_basic,
                new ArrayList<SearchResult>(), mDetailsButtonListener);
        mGameList = (ListView) findViewById(android.R.id.list);
        mGameList.setAdapter(mAdapter);
    }

    @Override
    protected void setupActionBar(@NotNull ActionBar actionBar) {
        actionBar.setDisplayHomeAsUpEnabled(false);
    }

    @Override
    protected void onStart() {
        super.onStart();
        List<SearchResult> gameList = DBHelper.getGames(DatabaseContract.GameListTable.TO_PLAY);
        Collections.sort(gameList, new SearchResult.LatestFirstComparator());
        setGameList(gameList);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);

        MenuItem addItem = menu.findItem(R.id.action_add);
        assert addItem != null;

        SearchView searchView = (SearchView) addItem.getActionView();
        assert searchView != null;

        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));

        return super.onCreateOptionsMenu(menu);
    }

    private void setGameList(List<SearchResult> gameList) {
        mAdapter.setNotifyOnChange(false);
        mAdapter.clear();
        mAdapter.addAll(gameList);
        mAdapter.notifyDataSetChanged();
    }

    private View.OnClickListener mDetailsButtonListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            int position = mGameList.getPositionForView(v);
            SearchResult game = mAdapter.getItem(position);

            Intent intent = new Intent(MainActivity.this, GameDetailsActivity.class);
            intent.putExtra(GameDetailsActivity.IntentFields.GAME_GIANT_BOMB_ID, game.giantBombId);
            startActivity(intent);
        }
    };

    private class GameListAdapter extends ArrayAdapter<SearchResult> {

        private LayoutInflater mLayoutInflater;
        private int mLayout;
        private View.OnClickListener mDetailsButtonListener;

        public GameListAdapter(Context context, int layout, List<SearchResult> objects,
                          View.OnClickListener detailsButtonListener) {
            super(context, layout, objects);
            mLayoutInflater = (LayoutInflater) context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            mLayout = layout;
            mDetailsButtonListener = detailsButtonListener;
        }

        @NotNull
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            SearchResultViewHolder viewHolder;

            if (convertView == null) {
                convertView = mLayoutInflater.inflate(mLayout, null);
                assert convertView != null;

                viewHolder = new SearchResultViewHolder();
                viewHolder.poster = (ImageView) convertView.findViewById(R.id.poster);
                viewHolder.title = (TextView) convertView.findViewById(R.id.title);
                viewHolder.releaseDate = (TextView) convertView.findViewById(R.id.release_date);
                viewHolder.details = convertView.findViewById(R.id.details);
                viewHolder.platforms = (TextView) convertView.findViewById(R.id.platforms);

                // add button listeners
                viewHolder.details.setOnClickListener(mDetailsButtonListener);

                convertView.setTag(viewHolder);
            } else {
                viewHolder = (SearchResultViewHolder) convertView.getTag();
            }

            final SearchResult item = getItem(position);

            if (item.posterUrl != null) {
                viewHolder.poster.setVisibility(View.VISIBLE);
                Glide.load(item.posterUrl)
                        .fitCenter()
                        .animate(android.R.anim.fade_in)
                        .into(viewHolder.poster);
            } else {
                viewHolder.poster.setVisibility(View.GONE);
            }
            viewHolder.title.setText(item.name);
            viewHolder.releaseDate.setText(item.releaseDate.toString());
            viewHolder.platforms.setText(item.getPlatformsDisplayString());

            return convertView;
        }

    }

}
