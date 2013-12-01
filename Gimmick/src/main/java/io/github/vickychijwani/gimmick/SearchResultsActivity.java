package io.github.vickychijwani.gimmick;

import android.app.Activity;
import android.app.ListActivity;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import io.github.vickychijwani.gimmick.data.SearchResult;
import io.github.vickychijwani.gimmick.giantbomb.GiantBombAPI;
import io.github.vickychijwani.gimmick.utility.ImageDownloader;
import io.github.vickychijwani.gimmick.utility.NetworkUtils;

public class SearchResultsActivity extends ListActivity {

    private SearchTask mSearchTask;
    private AddAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_results);
        getActionBar().setDisplayHomeAsUpEnabled(true);

        mAdapter = new AddAdapter(this, R.layout.game_search_result,
                new ArrayList<SearchResult>(), mDetailsButtonListener);

        setListAdapter(mAdapter);

        handleIntent(getIntent());
    }

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
            String query = intent.getStringExtra(SearchManager.QUERY).trim();
            if (mSearchTask == null || mSearchTask.getStatus() == AsyncTask.Status.FINISHED) {
                mSearchTask = new SearchTask();
                mSearchTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, query);
            }
        }
    }

    protected void setSearchResults(List<SearchResult> results) {
        mAdapter.setNotifyOnChange(false);
        mAdapter.clear();
        mAdapter.addAll(results);
        mAdapter.notifyDataSetChanged();
    }

    protected View.OnClickListener mDetailsButtonListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            // display more details in a dialog
//            int position = mGrid.getPositionForView(v);
//            SearchResult show = mAdapter.getItem(position);
//            AddDialogFragment.showAddDialog(show, getFragmentManager());
        }
    };

    protected static class AddAdapter extends ArrayAdapter<SearchResult> {

        private LayoutInflater mLayoutInflater;
        private int mLayout;
        private ImageDownloader mImageDownloader;
        private View.OnClickListener mDetailsButtonListener;

        public AddAdapter(Context context, int layout, List<SearchResult> objects,
                          View.OnClickListener detailsButtonListener) {
            super(context, layout, objects);
            mLayoutInflater = (LayoutInflater) context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            mLayout = layout;
            mImageDownloader = ImageDownloader.getInstance(context);
            mDetailsButtonListener = detailsButtonListener;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder viewHolder;

            if (convertView == null) {
                convertView = mLayoutInflater.inflate(mLayout, null);

                viewHolder = new ViewHolder();
                viewHolder.addbutton = convertView.findViewById(R.id.addbutton);
                viewHolder.details = convertView.findViewById(R.id.details);
                viewHolder.title = (TextView) convertView.findViewById(R.id.title);
                viewHolder.description = (TextView) convertView.findViewById(R.id.description);
                viewHolder.poster = (ImageView) convertView.findViewById(R.id.poster);

                // add button listeners
                viewHolder.details.setOnClickListener(mDetailsButtonListener);

                convertView.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) convertView.getTag();
            }

            final SearchResult item = getItem(position);

            // hide add button if already added that show
            viewHolder.addbutton.setVisibility(item.isAdded ? View.INVISIBLE : View.VISIBLE);
            viewHolder.addbutton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //TaskManager.getInstance(getContext()).performAddTask(item);

                    item.isAdded = true;
                    v.setVisibility(View.INVISIBLE);
                }
            });

            // set text properties immediately
            viewHolder.title.setText(item.name);
            viewHolder.description.setText(item.description);
            if (item.posterUrl != null) {
                viewHolder.poster.setVisibility(View.VISIBLE);
                mImageDownloader.download(item.posterUrl, viewHolder.poster, false);
            } else {
                viewHolder.poster.setVisibility(View.GONE);
            }

            return convertView;
        }

        static class ViewHolder {
            public TextView title;
            public TextView description;
            public ImageView poster;
            public View addbutton;
            public View details;
        }
    }

    public class SearchTask extends AsyncTask<String, Void, List<SearchResult>> {

        @Override
        protected void onPreExecute() {
            SearchResultsActivity.this.setProgressBarIndeterminateVisibility(true);
        }

        @Override
        protected List<SearchResult> doInBackground(String... params) {
            return GiantBombAPI.searchGames(params[0]);
        }

        @Override
        protected void onPostExecute(List<SearchResult> results) {
            final Activity activity = SearchResultsActivity.this;
            activity.setProgressBarIndeterminateVisibility(false);

            if (results != null) {
                setSearchResults(results);
            } else {
                Toast.makeText(activity, "Searching failed, try again later.", Toast.LENGTH_LONG).show();
            }
        }

    }
}
