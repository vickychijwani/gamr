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
import io.github.vickychijwani.gimmick.item.Game;
import io.github.vickychijwani.gimmick.utility.NetworkUtils;

public class GameOverviewFragment extends DataFragment<Game,View> {

    private Game mGame;

    @InjectView(R.id.poster)            ImageView mPosterView;
    @InjectView(R.id.release_date)      TextView mReleaseDateView;
    @InjectView(R.id.platforms)         TextView mPlatformsView;
    @InjectView(R.id.blurb)             TextView mBlurbView;
    @InjectView(R.id.metascore_value)   TextView mMetascoreView;
    @InjectView(R.id.genres)            TextView mGenresView;
    @InjectView(R.id.franchises)        TextView mFranchisesView;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        View view = inflater.inflate(R.layout.fragment_game_overview, container, false);
        ButterKnife.inject(this, view);

        bindDataToView(mGame, view);

        return view;
    }

    @Override
    void onDataLoaded(Game game) {
        mGame = game;
        bindDataToView(mGame, getView());
    }

    @Override
    protected void onBindDataToView(@NotNull Game game, @NotNull View view) {
        NetworkUtils.loadImage(game.smallPosterUrl, mPosterView);
        mReleaseDateView.setText(game.releaseDate.toString());
        mPlatformsView.setText(game.getPlatformsDisplayString());
        mBlurbView.setText(game.blurb);

        if (game.metascore > 0) {
            mMetascoreView.setText(String.valueOf(game.metascore));
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
