package io.github.vickychijwani.gimmick.view.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.RatingBar;
import android.widget.TextView;

import org.jetbrains.annotations.NotNull;

import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;
import io.github.vickychijwani.giantbomb.item.Review;
import io.github.vickychijwani.gimmick.R;

public class ReviewListAdapter extends ArrayAdapter<Review> {

    private static final int LAYOUT = R.layout.component_review;

    private final LayoutInflater mLayoutInflater;

    public ReviewListAdapter(Context context, List<Review> reviewList) {
        super(context, LAYOUT, reviewList);
        mLayoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @NotNull
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ReviewViewHolder viewHolder;

        final Review item = getItem(position);

        if (convertView == null) {
            convertView = mLayoutInflater.inflate(LAYOUT, null);
            assert convertView != null;

            viewHolder = new ReviewViewHolder(convertView);

            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ReviewViewHolder) convertView.getTag();
        }

        viewHolder.user.setText(item.getReviewer());
        viewHolder.score.setRating((float) item.getScore());
        viewHolder.title.setText(item.getTitle());

        return convertView;
    }

    class ReviewViewHolder {
        @InjectView(R.id.user)      TextView user;
        @InjectView(R.id.score)     RatingBar score;
        @InjectView(R.id.title)     TextView title;

        public ReviewViewHolder(View view) {
            ButterKnife.inject(this, view);
        }
    }

}
