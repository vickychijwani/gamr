package io.github.vickychijwani.gimmick.view;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import butterknife.ButterKnife;
import io.github.vickychijwani.gimmick.R;
import io.github.vickychijwani.gimmick.api.GiantBomb;
import io.github.vickychijwani.gimmick.item.GameList;
import io.github.vickychijwani.gimmick.utility.AppUtils;

public class RecentGamesFragment extends AddGamesFragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        View view = inflater.inflate(R.layout.component_add_games_list, container, false);
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

        setRequestTag(GiantBomb.fetchRecentGames(getResultsHandler(), getErrorHandler()));
        onRequestInitiated();
    }

    @Override
    protected void onReceivedResults(GameList gameList) {
        gameList.sortByLatestFirst();
    }

}