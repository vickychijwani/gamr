package io.github.vickychijwani.gimmick.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import io.github.vickychijwani.gimmick.R;
import io.github.vickychijwani.gimmick.item.GameList;
import io.github.vickychijwani.gimmick.item.SearchResult;
import io.github.vickychijwani.gimmick.utility.NetworkUtils;

public class GameListAdapter extends ArrayAdapter<SearchResult> {

    private static final int LAYOUT = R.layout.component_game_item;

    private final int                   mLayout;
    private final LayoutInflater        mLayoutInflater;
    private final View.OnClickListener  mDetailsButtonListener;

    public GameListAdapter(Context context, GameList gameList,
                           @Nullable View.OnClickListener detailsButtonListener) {
        this(context, LAYOUT, gameList, detailsButtonListener);
    }

    protected GameListAdapter(Context context, int layout, GameList gameList,
                              @Nullable View.OnClickListener detailsButtonListener) {
        super(context, layout, gameList);
        mLayout = layout;
        mLayoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mDetailsButtonListener = detailsButtonListener;
    }

    @NotNull
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        SearchResultViewHolder viewHolder;

        final SearchResult item = getItem(position);

        if (convertView == null) {
            convertView = mLayoutInflater.inflate(mLayout, null);
            assert convertView != null;

            viewHolder = new SearchResultViewHolder(convertView);

            if (mDetailsButtonListener != null) {
                viewHolder.details.setOnClickListener(mDetailsButtonListener);
            }

            convertView.setTag(viewHolder);
        } else {
            viewHolder = (SearchResultViewHolder) convertView.getTag();
        }

        NetworkUtils.loadImage(item.posterUrl, viewHolder.poster);
        viewHolder.title.setText(item.name);
        viewHolder.releaseDate.setText(item.releaseDate.toString());
        viewHolder.platforms.setText(item.getPlatformsDisplayString());

        onGetViewFinished(viewHolder, item);

        return convertView;
    }

    protected void onGetViewFinished(final SearchResultViewHolder viewHolder, final SearchResult item) {
        // nothing to do
    }

}
