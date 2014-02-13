package io.github.vickychijwani.gimmick.view;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import butterknife.ButterKnife;
import io.github.vickychijwani.gimmick.R;
import io.github.vickychijwani.gimmick.api.GiantBomb;
import io.github.vickychijwani.gimmick.api.NetworkRequestQueue;
import io.github.vickychijwani.gimmick.api.RequestTag;
import io.github.vickychijwani.gimmick.utility.NetworkUtils;

public class UpcomingGamesFragment extends AddGamesFragment {

    private RequestTag mRequestTag = null;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        View view = inflater.inflate(R.layout.fragment_upcoming_games, container, false);
        ButterKnife.inject(this, view);

        setupAdapter();

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mRequestTag == null) {
            initiateRequest();
        }
    }

    @Override
    protected void initiateRequest() {
        if (! NetworkUtils.isNetworkConnected(getActivity())) {
            Toast.makeText(getActivity(), R.string.offline, Toast.LENGTH_LONG).show();
            return;
        }

        getActivity().setProgressBarIndeterminateVisibility(true);
        mRequestTag = GiantBomb.fetchUpcomingGames(getResultsHandler(), getErrorHandler());
    }

    @Override
    protected void cancelPendingRequests() {
        if (mRequestTag != null) {
            NetworkRequestQueue.cancelPending(mRequestTag);
        }
    }

}
