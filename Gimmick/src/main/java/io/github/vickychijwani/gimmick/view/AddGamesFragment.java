package io.github.vickychijwani.gimmick.view;

import android.util.Log;
import android.view.View;
import android.widget.ListView;
import android.widget.Toast;

import com.android.volley.Response;
import com.android.volley.VolleyError;

import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

import butterknife.InjectView;
import io.github.vickychijwani.gimmick.R;
import io.github.vickychijwani.gimmick.adapter.AddGamesAdapter;
import io.github.vickychijwani.gimmick.item.SearchResult;

public abstract class AddGamesFragment extends BaseFragment {

    private static final String TAG = "AddGamesFragment";

    protected AddGamesAdapter mAdapter;
    @InjectView(android.R.id.list) ListView mResultsList;

    protected abstract void initiateRequest();

    protected abstract void cancelPendingRequests();

    protected final void setupAdapter() {
        mAdapter = new AddGamesAdapter(getActivity(), R.layout.component_game_add,
                new ArrayList<SearchResult>(), getDetailsButtonListener());
        mResultsList.setAdapter(mAdapter);
    }

    @Nullable
    protected View.OnClickListener getDetailsButtonListener() {
        return null;
    }

    @Override
    public void onStop() {
        super.onStop();
        cancelPendingRequests();
    }

    protected void setGameList(List<SearchResult> games) {
        mAdapter.setNotifyOnChange(false);
        mAdapter.clear();
        mAdapter.addAll(games);
        mAdapter.notifyDataSetChanged();
    }

    private Response.Listener<List<SearchResult>> mResultsHandler = new Response.Listener<List<SearchResult>>() {
        @Override
        public void onResponse(List<SearchResult> results) {
            getActivity().setProgressBarIndeterminateVisibility(false);

            if (! results.isEmpty()) {
                setGameList(results);
            } else {
                Toast.makeText(getActivity(), R.string.fetching_games_failed, Toast.LENGTH_LONG).show();
            }
        }
    };

    protected final Response.Listener<List<SearchResult>> getResultsHandler() {
        return mResultsHandler;
    }

    private Response.ErrorListener mErrorHandler = new Response.ErrorListener() {
        @Override
        public void onErrorResponse(VolleyError error) {
            Log.e(TAG, "Error: " + error.getMessage());
            Log.e(TAG, Log.getStackTraceString(error));

            getActivity().setProgressBarIndeterminateVisibility(false);
            setGameList(new ArrayList<SearchResult>());
            Toast.makeText(getActivity(), R.string.fetching_games_failed, Toast.LENGTH_LONG).show();
        }
    };

    protected final Response.ErrorListener getErrorHandler() {
        return mErrorHandler;
    }

}
