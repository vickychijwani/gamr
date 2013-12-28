package io.github.vickychijwani.gimmick.view;

import android.app.ActionBar;
import android.app.Activity;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.bumptech.glide.Glide;

import org.jetbrains.annotations.NotNull;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import io.github.vickychijwani.gimmick.R;
import io.github.vickychijwani.gimmick.api.GiantBomb;
import io.github.vickychijwani.gimmick.background.TaskManager;
import io.github.vickychijwani.gimmick.item.SearchResult;
import io.github.vickychijwani.gimmick.utility.NetworkUtils;
import io.github.vickychijwani.gimmick.viewholder.SearchResultViewHolder;

public class SearchResultsActivity extends BaseActivity {

    private static final String TAG = "SearchResultsActivity";

    private AddAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_results);

        mAdapter = new AddAdapter(this, R.layout.game_search_result,
                new ArrayList<SearchResult>(), mDetailsButtonListener);
        ListView resultsList = (ListView) findViewById(android.R.id.list);
        resultsList.setAdapter(mAdapter);

        handleIntent(getIntent());
    }

    @Override
    protected void setupActionBar(@NotNull ActionBar actionBar) {
        actionBar.setDisplayHomeAsUpEnabled(true);
    }

    private Response.Listener<JSONObject> mResultsHandler = new Response.Listener<JSONObject>() {
        @Override
        public void onResponse(JSONObject resultsJson) {
            List<SearchResult> results = GiantBomb.buildSearchResultsFromJson(resultsJson);
            final Activity activity = SearchResultsActivity.this;
            activity.setProgressBarIndeterminateVisibility(false);

            if (! results.isEmpty()) {
                setSearchResults(results);
            } else {
                Toast.makeText(activity, R.string.search_failed, Toast.LENGTH_LONG).show();
            }
        }
    };

    private Response.ErrorListener mErrorHandler = new Response.ErrorListener() {
        @Override
        public void onErrorResponse(VolleyError error) {
            Log.d(TAG, "Error: " + error.getMessage());
            error.printStackTrace();

            Toast.makeText(SearchResultsActivity.this, R.string.search_failed, Toast.LENGTH_LONG).show();
            setSearchResults(new ArrayList<SearchResult>());
        }
    };

    @Override
    protected void onNewIntent(Intent intent) {
        setIntent(intent);
        handleIntent(intent);
    }

    private void handleIntent(Intent intent) {
        if (! NetworkUtils.isNetworkConnected(this)) {
            Toast.makeText(this, getString(R.string.offline), Toast.LENGTH_LONG).show();
            return;
        }

        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            SearchResultsActivity.this.setProgressBarIndeterminateVisibility(true);
            String query = intent.getStringExtra(SearchManager.QUERY);
            assert query != null;
            query = query.trim();

            GiantBomb.searchGames(query, mResultsHandler, mErrorHandler);
        }
    }

    protected void setSearchResults(List<SearchResult> results) {
        mAdapter.setNotifyOnChange(false);
        mAdapter.clear();
        mAdapter.addAll(results);
        mAdapter.notifyDataSetChanged();
    }

    private View.OnClickListener mDetailsButtonListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            // display more details in a dialog
//            int position = mGrid.getPositionForView(v);
//            SearchResult show = mAdapter.getItem(position);
//            AddDialogFragment.showAddDialog(show, getFragmentManager());
        }
    };

    private static class AddAdapter extends ArrayAdapter<SearchResult> {

        private LayoutInflater mLayoutInflater;
        private int mLayout;
        private View.OnClickListener mDetailsButtonListener;

        public AddAdapter(Context context, int layout, List<SearchResult> objects,
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
                viewHolder.addbutton = convertView.findViewById(R.id.addbutton);

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

            // hide add button if already added that show
            viewHolder.addbutton.setVisibility(item.isAdded ? View.INVISIBLE : View.VISIBLE);
            viewHolder.addbutton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    TaskManager.getInstance(getContext()).performAddTask(item);
                    item.isAdded = true;
                    v.setVisibility(View.INVISIBLE);
                }
            });

            return convertView;
        }

    }

}
