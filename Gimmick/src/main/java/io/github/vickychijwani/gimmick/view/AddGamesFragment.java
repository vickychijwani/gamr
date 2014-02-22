package io.github.vickychijwani.gimmick.view;

import android.util.Log;
import android.view.View;
import android.widget.ListView;
import android.widget.Toast;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.meetme.android.multistateview.MultiStateView;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import butterknife.InjectView;
import io.github.vickychijwani.gimmick.R;
import io.github.vickychijwani.gimmick.adapter.AddGamesAdapter;
import io.github.vickychijwani.gimmick.api.NetworkRequestQueue;
import io.github.vickychijwani.gimmick.api.RequestTag;
import io.github.vickychijwani.gimmick.item.GameList;
import io.github.vickychijwani.gimmick.utility.AppUtils;

public abstract class AddGamesFragment extends BaseFragment {

    private static final String TAG = "AddGamesFragment";

    private RequestTag mRequestTag = null;
    protected AddGamesAdapter mAdapter;

    @InjectView(android.R.id.list) ListView mGameList;
    @InjectView(R.id.list_container) MultiStateView mGameListContainer;

    /**
     * Initiate a network request to fetch some games.
     */
    protected abstract void initiateRequest();

    /**
     * Override this to be notified when a request completes successfully.
     *
     * @param gameList the results received in the response
     */
    protected void onReceivedResults(GameList gameList) {
        // nothing to do
    }

    /**
     * Override this to be notified when a request results in an error.
     *
     * @param error the error that occurred
     */
    protected void onReceivedError(VolleyError error) {
        // nothing to do
    }

    /**
     * Call this at the end of {@link #initiateRequest()}, to update the UI to a loading state, etc.
     */
    protected final void onRequestInitiated() {
        mGameListContainer.setState(MultiStateView.ContentState.LOADING);
    }

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
    public void onDestroy() {
        super.onDestroy();
        cancelPendingRequests();
    }

    /**
     * Call this from within {@link #initiateRequest()}, to allow pre-emptive cancellation of stale
     * requests.
     *
     * @param requestTag a tag identifying the requests initiated
     */
    protected final void setRequestTag(@NotNull RequestTag requestTag) {
        mRequestTag = requestTag;
    }

    private void cancelPendingRequests() {
        if (mRequestTag != null) {
            NetworkRequestQueue.cancelPending(mRequestTag);
            mRequestTag = null;
        }
    }

    private final Response.Listener<GameList> mResultsHandler = new Response.Listener<GameList>() {
        @Override
        public void onResponse(GameList results) {
            onReceivedResults(results);
            mRequestTag = null;

            if (! results.isEmpty()) {
                mGameListContainer.setState(MultiStateView.ContentState.CONTENT);
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

            onReceivedError(error);
            mRequestTag = null;

            mGameListContainer.setState(MultiStateView.ContentState.EMPTY);
            AppUtils.changeAdapterDataSet(mAdapter, new GameList());
            Toast.makeText(getActivity(), R.string.fetching_games_failed, Toast.LENGTH_LONG).show();
        }
    };

    protected final Response.ErrorListener getErrorHandler() {
        return mErrorHandler;
    }

}
