package io.github.vickychijwani.gimmick.view;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import butterknife.ButterKnife;
import io.github.vickychijwani.gimmick.R;
import io.github.vickychijwani.giantbomb.api.GiantBomb;
import io.github.vickychijwani.giantbomb.item.GameList;
import io.github.vickychijwani.utility.AppUtils;

public class UpcomingGamesFragment extends AddGamesFragment {

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

        setRequestTag(GiantBomb.Games.fetchUpcoming(getResultsHandler(), getErrorHandler()));
        onRequestInitiated();
    }

    @Override
    protected void onReceivedResults(GameList gameList) {
        gameList.sortByEarliestFirst();
    }

}
