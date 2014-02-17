package io.github.vickychijwani.gimmick.view;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;
import io.github.vickychijwani.gimmick.R;
import io.github.vickychijwani.gimmick.adapter.VideoListAdapter;
import io.github.vickychijwani.gimmick.item.Video;
import io.github.vickychijwani.gimmick.utility.AppUtils;

public class VideosFragment extends DataFragment<List<Video>,VideoListAdapter> {

    private static final String TAG = "VideosFragment";
    private static final int LOW_RES = 0;
    private static final int HIGH_RES = 1;

    private VideoListAdapter mAdapter;
    private List<Video> mVideoList;

    @InjectView(android.R.id.list) ListView mVideoListView;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        View view = inflater.inflate(R.layout.fragment_videos, container, false);
        ButterKnife.inject(this, view);

        mAdapter = new VideoListAdapter(getActivity(), new ArrayList<Video>(), mItemClickListener);
        mVideoListView.setAdapter(mAdapter);

        bindDataToView(mVideoList, mAdapter);

        return view;
    }

    @Override
    void onDataLoaded(List<Video> videoList) {
        mVideoList = videoList;
        bindDataToView(mVideoList, mAdapter);
    }

    @Override
    protected void onBindDataToView(@NotNull List<Video> data, @NotNull VideoListAdapter adapter) {
        AppUtils.changeAdapterDataSet(adapter, data);
    }

    private void playVideo(Video video, int choice) {
        if (AppUtils.showErrorIfOffline(getActivity())) {
            return;
        }

        String videoUrl;
        switch (choice) {
            case LOW_RES:
                videoUrl = video.getLowUrl();
                break;
            case HIGH_RES:
                videoUrl = video.getHighUrl();
                break;
            default:
                throw new IllegalArgumentException("invalid video resolution choice!");
        }

        Log.i(TAG, "Playing " + ((choice == LOW_RES) ? "low-res" : "high-res") + " video from " + videoUrl);
        Intent playIntent = new Intent(Intent.ACTION_VIEW);
        playIntent.setDataAndType(Uri.parse(videoUrl), "video/*");
        if (AppUtils.isIntentResolvable(getActivity(), playIntent)) {
            startActivity(playIntent);
        } else {
            // TODO no video player! show error and link to Play Store!
        }
    }

    private final View.OnClickListener mItemClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (AppUtils.showErrorIfOffline(getActivity())) {
                return;
            }

            int position = mVideoListView.getPositionForView(v);
            Video video = mAdapter.getItem(position);
            new PlayVideoDialogFragment(video).show(getActivity().getFragmentManager(), null);
        }
    };

    private class PlayVideoDialogFragment extends DialogFragment {

        private final Video mVideo;
        private int mChoice = LOW_RES;          // default to low-res

        public PlayVideoDialogFragment(@NotNull Video video) {
            mVideo = video;
        }

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            if (getActivity() == null) {
                Log.e(TAG, "getActivity() returned null!");
                return null;
            }

            return new AlertDialog.Builder(getActivity())
                    .setTitle(R.string.play_which_version)
                    .setSingleChoiceItems(R.array.video_resolutions, mChoice, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            mChoice = which;
                        }
                    })
                    .setPositiveButton(R.string.just_once, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            playVideo(mVideo, mChoice);
                        }
                    })
                    .setNegativeButton(R.string.always, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            // TODO remember this preference!
                        }
                    })
                    .create();
        }
    }

}