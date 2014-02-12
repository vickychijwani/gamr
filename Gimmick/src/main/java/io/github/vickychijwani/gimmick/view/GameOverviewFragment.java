package io.github.vickychijwani.gimmick.view;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import butterknife.ButterKnife;
import butterknife.InjectView;
import io.github.vickychijwani.gimmick.R;
import io.github.vickychijwani.gimmick.item.SearchResult;
import io.github.vickychijwani.gimmick.utility.NetworkUtils;

public class GameOverviewFragment extends DataFragment<SearchResult> {

    private SearchResult mGame;

    @InjectView(R.id.poster)                    ImageView mPosterView;
    @InjectView(R.id.release_date)              TextView mReleaseDateView;
    @InjectView(R.id.platforms)                 TextView mPlatformsView;
    @InjectView(R.id.blurb)                     TextView mBlurbView;
    @InjectView(R.id.rating_metacritic_value)   TextView mMetacriticRatingView;
    @InjectView(R.id.genres)                    TextView mGenresView;
    @InjectView(R.id.franchises)                TextView mFranchisesView;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        View view = inflater.inflate(R.layout.fragment_game_overview, container, false);
        ButterKnife.inject(this, view);

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
        assert game != null && view != null;

        NetworkUtils.loadImage(game.smallPosterUrl, mPosterView);
        mReleaseDateView.setText(game.releaseDate.toString());
        mPlatformsView.setText(game.getPlatformsDisplayString());
        mBlurbView.setText(game.blurb);

        if (game.metacriticRating > 0) {
            mMetacriticRatingView.setText(String.valueOf(game.metacriticRating));
        }
        bindTextOrHide(game.getGenresDisplayString(), mGenresView, view.findViewById(R.id.genres_header));
        bindTextOrHide(game.getFranchisesDisplayString(), mFranchisesView, view.findViewById(R.id.franchises_header));
    }

    private void bindTextOrHide(@Nullable String text, @NotNull TextView textView, @NotNull View headerView) {
        if (TextUtils.isEmpty(text)) {
            textView.setVisibility(View.GONE);
            headerView.setVisibility(View.GONE);
        } else {
            textView.setText(text);
        }
    }

}
