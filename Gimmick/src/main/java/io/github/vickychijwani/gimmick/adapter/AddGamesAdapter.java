package io.github.vickychijwani.gimmick.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;
import io.github.vickychijwani.gimmick.R;
import io.github.vickychijwani.gimmick.background.TaskManager;
import io.github.vickychijwani.gimmick.item.SearchResult;
import io.github.vickychijwani.gimmick.utility.NetworkUtils;

public class AddGamesAdapter extends ArrayAdapter<SearchResult> {

    private final LayoutInflater mLayoutInflater;
    private final int mLayout;
    private final View.OnClickListener mDetailsButtonListener;

    public AddGamesAdapter(Context context, int layout, List<SearchResult> objects,
                           @Nullable View.OnClickListener detailsButtonListener) {
        super(context, layout, objects);
        mLayoutInflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mLayout = layout;
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

        // hide add button if already added that show
        viewHolder.addbutton.setVisibility(item.isAdded ? View.INVISIBLE : View.VISIBLE);
        viewHolder.addbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TaskManager.getInstance(getContext()).performAddTask(item);
                item.isAdded = true;
                v.setVisibility(View.INVISIBLE);
            }
        });

        return convertView;
    }

    static class SearchResultViewHolder {
        @InjectView(R.id.poster)        public ImageView poster;
        @InjectView(R.id.title)         public TextView title;
        @InjectView(R.id.release_date)  public TextView releaseDate;
        @InjectView(R.id.platforms)     public TextView platforms;
        @InjectView(R.id.details)       public View details;
        @InjectView(R.id.addbutton)     public View addbutton;

        public SearchResultViewHolder(View view) {
            ButterKnife.inject(this, view);
        }
    }

}
