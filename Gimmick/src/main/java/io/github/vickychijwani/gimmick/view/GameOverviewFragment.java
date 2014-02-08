package io.github.vickychijwani.gimmick.view;

import android.database.Cursor;
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

public class GameOverviewFragment extends DataFragment {

    private Cursor mCursor;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        View view = inflater.inflate(R.layout.fragment_game_overview, container, false);

        if (mCursor != null) {
            bindDataToView(mCursor, view);
        }

        return view;
    }

    @Override
    void onDataLoaded(Cursor cursor) {
        mCursor = cursor;
        if (getView() != null) {
            bindDataToView(cursor, getView());
        }
    }

    private void bindDataToView(Cursor cursor, View view) {
        assert cursor != null;
        assert view != null;
        ImageView poster = (ImageView) view.findViewById(R.id.poster);
        TextView releaseDate = (TextView) view.findViewById(R.id.release_date);
        TextView platforms = (TextView) view.findViewById(R.id.platforms);
        TextView blurb = (TextView) view.findViewById(R.id.blurb);

        SearchResult game = new SearchResult(cursor);

        NetworkUtils.loadImage(game.smallPosterUrl, poster);
        releaseDate.setText(game.releaseDate.toString());
        platforms.setText(game.getPlatformsDisplayString());
        blurb.setText(game.blurb);
    }

}
