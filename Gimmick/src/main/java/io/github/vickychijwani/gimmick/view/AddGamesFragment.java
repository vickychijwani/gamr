package io.github.vickychijwani.gimmick.view;

import android.util.Log;
import android.view.View;
import android.widget.ListView;
import android.widget.Toast;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.meetme.android.multistateview.MultiStateView;

import org.jetbrains.annotations.Nullable;

import butterknife.InjectView;
import io.github.vickychijwani.gimmick.R;
import io.github.vickychijwani.gimmick.adapter.AddGamesAdapter;
import io.github.vickychijwani.gimmick.item.GameList;
import io.github.vickychijwani.gimmick.utility.AppUtils;

public abstract class AddGamesFragment extends BaseFragment {

    private static final String TAG = "AddGamesFragment";

    protected AddGamesAdapter mAdapter;

    @InjectView(android.R.id.list) ListView mGameList;
    @InjectView(R.id.list_container) MultiStateView mGameListContainer;

    protected abstract void initiateRequest();

    protected abstract void cancelPendingRequests();

    protected abstract void sortResults(GameList gameList);

    protected final void setupAdapter() {
        mAdapter = new AddGamesAdapter(getActivity(), new GameList(), getDetailsButtonListener());
        mGameList.setAdapter(mAdapter);
    }

    @SuppressWarnings("SameReturnValue")
    @Nullable
    protected View.OnClickListener getDetailsButtonListener() {
        return null;
    }

    @Override
    public void onStop() {
        super.onStop();
        cancelPendingRequests();
    }

    private final Response.Listener<GameList> mResultsHandler = new Response.Listener<GameList>() {
        @Override
        public void onResponse(GameList results) {
            if (! results.isEmpty()) {
                mGameListContainer.setState(MultiStateView.ContentState.CONTENT);
                sortResults(results);
                AppUtils.changeAdapterDataSet(mAdapter, results);
            } else {
                mGameListContainer.setState(MultiStateView.ContentState.EMPTY);
            }
        }
    };

    protected final Response.Listener<GameList> getResultsHandler() {
        return mResultsHandler;
    }

    private final Response.ErrorListener mErrorHandler = new Response.ErrorListener() {
        @Override
        public void onErrorResponse(VolleyError error) {
            Log.e(TAG, "Error: " + error.getMessage());
            Log.e(TAG, Log.getStackTraceString(error));

            mGameListContainer.setState(MultiStateView.ContentState.EMPTY);
            AppUtils.changeAdapterDataSet(mAdapter, new GameList());
            Toast.makeText(getActivity(), R.string.fetching_games_failed, Toast.LENGTH_LONG).show();
        }
    };

    protected final Response.ErrorListener getErrorHandler() {
        return mErrorHandler;
    }

}
