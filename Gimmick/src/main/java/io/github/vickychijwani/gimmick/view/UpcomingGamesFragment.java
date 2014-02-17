package io.github.vickychijwani.gimmick.view;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.android.volley.VolleyError;

import butterknife.ButterKnife;
import io.github.vickychijwani.gimmick.R;
import io.github.vickychijwani.gimmick.api.GiantBomb;
import io.github.vickychijwani.gimmick.api.NetworkRequestQueue;
import io.github.vickychijwani.gimmick.api.RequestTag;
import io.github.vickychijwani.gimmick.item.GameList;
import io.github.vickychijwani.gimmick.utility.AppUtils;

public class UpcomingGamesFragment extends AddGamesFragment {

    private RequestTag mRequestTag = null;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        View view = inflater.inflate(R.layout.fragment_upcoming_games, container, false);
        ButterKnife.inject(this, view);

        setupAdapter();
        initiateRequest();

        return view;
    }

    @Override
    protected void initiateRequest() {
        if (AppUtils.showErrorIfOffline(getActivity())) {
            return;
        }

        mRequestTag = GiantBomb.fetchUpcomingGames(getResultsHandler(), getErrorHandler());
        onRequestInitiated();
    }

    @Override
    protected void cancelPendingRequests() {
        if (mRequestTag != null) {
            NetworkRequestQueue.cancelPending(mRequestTag);
            mRequestTag = null; // discard invalid request tag
        }
    }

    @Override
    protected void onReceivedResults(GameList gameList) {
        gameList.sortByEarliestFirst();
        mRequestTag = null; // discard invalid request tag
    }

    @Override
    protected void onReceivedError(VolleyError error) {
        mRequestTag = null; // discard invalid request tag
    }

}
