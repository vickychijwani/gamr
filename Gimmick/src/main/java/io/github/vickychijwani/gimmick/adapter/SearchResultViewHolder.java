package io.github.vickychijwani.gimmick.adapter;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.Optional;
import io.github.vickychijwani.gimmick.R;

class SearchResultViewHolder {
    @InjectView(R.id.poster)                ImageView poster;
    @InjectView(R.id.title)                 TextView title;
    @InjectView(R.id.release_date)          TextView releaseDate;
    @InjectView(R.id.platforms)             TextView platforms;
    @InjectView(R.id.details)               View details;
    @InjectView(R.id.addbutton) @Optional   View addbutton;

    public SearchResultViewHolder(View view) {
        ButterKnife.inject(this, view);
    }
}
