package io.github.vickychijwani.gimmick.view;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import io.github.vickychijwani.gimmick.R;
import io.github.vickychijwani.gimmick.item.SearchResult;
import io.github.vickychijwani.gimmick.utility.NetworkUtils;

public class GameOverviewFragment extends DataFragment<SearchResult> {

    private SearchResult mGame;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        View view = inflater.inflate(R.layout.fragment_game_overview, container, false);

        if (mGame != null) {
            bindDataToView(mGame, view);
        }

        return view;
    }

    @Override
    void onDataLoaded(SearchResult game) {
        mGame = game;
        if (getView() != null) {
            bindDataToView(game, getView());
        }
    }

    private void bindDataToView(SearchResult game, View view) {
        assert game != null;
        assert view != null;

        ImageView poster = (ImageView) view.findViewById(R.id.poster);
        TextView releaseDate = (TextView) view.findViewById(R.id.release_date);
        TextView platforms = (TextView) view.findViewById(R.id.platforms);
        TextView blurb = (TextView) view.findViewById(R.id.blurb);
        TextView metacriticRating = (TextView) view.findViewById(R.id.rating_metacritic_value);
        TextView genres = (TextView) view.findViewById(R.id.genres);
        TextView franchises = (TextView) view.findViewById(R.id.franchises);

        NetworkUtils.loadImage(game.smallPosterUrl, poster);
        releaseDate.setText(game.releaseDate.toString());
        platforms.setText(game.getPlatformsDisplayString());
        blurb.setText(game.blurb);

        if (game.metacriticRating > 0) {
            metacriticRating.setText(String.valueOf(game.metacriticRating));
        }
        bindTextOrHide(game.getGenresDisplayString(), genres, view.findViewById(R.id.genres_header));
        bindTextOrHide(game.getFranchisesDisplayString(), franchises, view.findViewById(R.id.franchises_header));
    }

    private void bindTextOrHide(String text, TextView textView, View headerView) {
        if ("".equals(text)) {
            textView.setVisibility(View.GONE);
            headerView.setVisibility(View.GONE);
        } else {
            textView.setText(text);
        }
    }

}
