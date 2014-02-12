package io.github.vickychijwani.gimmick.view;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Toast;

import com.android.volley.Response;
import com.android.volley.VolleyError;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import io.github.vickychijwani.gimmick.R;
import io.github.vickychijwani.gimmick.adapter.AddGamesAdapter;
import io.github.vickychijwani.gimmick.api.GiantBomb;
import io.github.vickychijwani.gimmick.item.SearchResult;
import io.github.vickychijwani.gimmick.utility.NetworkUtils;

public class SearchGamesFragment extends BaseFragment {

    private static final String TAG = "SearchGamesFragment";

    private AddGamesAdapter mAdapter;

    @InjectView(android.R.id.list) ListView mResultsList;
    @InjectView(R.id.searchbox) EditText mSearchBox;
    @InjectView(R.id.clear_button) ImageButton mClearButton;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        View view = inflater.inflate(R.layout.fragment_search_games, container, false);
        ButterKnife.inject(this, view);

        mAdapter = new AddGamesAdapter(getActivity(), R.layout.game_search_result,
                new ArrayList<SearchResult>(), mDetailsButtonListener);
        mResultsList.setAdapter(mAdapter);

        mSearchBox.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                // we only want to react to down events
                if (event.getAction() != KeyEvent.ACTION_DOWN)
                    return false;

                if (keyCode == KeyEvent.KEYCODE_ENTER) {
                    search();
                    return true;
                } else {
                    return false;
                }
            }
        });

        return view;
    }

    private Response.Listener<JSONObject> mResultsHandler = new Response.Listener<JSONObject>() {
        @Override
        public void onResponse(JSONObject resultsJson) {
            List<SearchResult> results = GiantBomb.buildSearchResultsFromJson(resultsJson);
            getActivity().setProgressBarIndeterminateVisibility(false);

            if (! results.isEmpty()) {
                setSearchResults(results);
            } else {
                Toast.makeText(getActivity(), R.string.search_failed, Toast.LENGTH_LONG).show();
            }
        }
    };

    private Response.ErrorListener mErrorHandler = new Response.ErrorListener() {
        @Override
        public void onErrorResponse(VolleyError error) {
            Log.e(TAG, "Error: " + error.getMessage());
            Log.e(TAG, Log.getStackTraceString(error));

            Toast.makeText(getActivity(), R.string.search_failed, Toast.LENGTH_LONG).show();
            setSearchResults(new ArrayList<SearchResult>());
        }
    };

    protected void setSearchResults(List<SearchResult> results) {
        mAdapter.setNotifyOnChange(false);
        mAdapter.clear();
        mAdapter.addAll(results);
        mAdapter.notifyDataSetChanged();
    }

    protected void search() {
        if (! NetworkUtils.isNetworkConnected(getActivity())) {
            Toast.makeText(getActivity(), R.string.offline, Toast.LENGTH_LONG).show();
            return;
        }

        assert mSearchBox.getText() != null;
        String query = mSearchBox.getText().toString().trim();
        if (TextUtils.isEmpty(query)) {
            return;
        }

        getActivity().setProgressBarIndeterminateVisibility(true);
        GiantBomb.searchGames(query, mResultsHandler, mErrorHandler);
    }

    @OnClick(R.id.clear_button) void clearInput() {
        mSearchBox.setText("");
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

}
