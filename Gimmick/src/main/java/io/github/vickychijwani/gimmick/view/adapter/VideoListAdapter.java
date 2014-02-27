package io.github.vickychijwani.gimmick.view.adapter;

import android.content.Context;
import android.text.format.DateUtils;
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
import io.github.vickychijwani.giantbomb.item.Video;
import io.github.vickychijwani.gimmick.R;
import io.github.vickychijwani.gimmick.utility.Utils;

public class VideoListAdapter extends ArrayAdapter<Video> {

    private static final int LAYOUT = R.layout.component_video_card;

    private final LayoutInflater mLayoutInflater;
    private final View.OnClickListener mClickListener;

    public VideoListAdapter(Context context, List<Video> videoList,
                            @Nullable View.OnClickListener clickListener) {
        super(context, LAYOUT, videoList);
        mLayoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mClickListener = clickListener;
    }

    @NotNull
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        VideoViewHolder viewHolder;

        final Video item = getItem(position);

        if (convertView == null) {
            convertView = mLayoutInflater.inflate(LAYOUT, null);
            assert convertView != null;

            viewHolder = new VideoViewHolder(convertView);

            if (mClickListener != null) {
                viewHolder.card.setOnClickListener(mClickListener);
            }

            convertView.setTag(viewHolder);
        } else {
            viewHolder = (VideoViewHolder) convertView.getTag();
        }

        Utils.loadImage(item.getThumbUrl(), viewHolder.thumbnail);
        viewHolder.duration.setText(DateUtils.formatElapsedTime(item.getDuration()));
        viewHolder.name.setText(item.getName());
        viewHolder.user.setText(getContext().getString(R.string.uploaded_by_user, item.getUser()));
        viewHolder.type.setText(item.getType());

        return convertView;
    }

    class VideoViewHolder {
        @InjectView(R.id.card)      ViewGroup card;
        @InjectView(R.id.thumbnail) ImageView thumbnail;
        @InjectView(R.id.duration)  TextView duration;
        @InjectView(R.id.name)      TextView name;
        @InjectView(R.id.user)      TextView user;
        @InjectView(R.id.type)      TextView type;

        public VideoViewHolder(View view) {
            ButterKnife.inject(this, view);
        }
    }

}
