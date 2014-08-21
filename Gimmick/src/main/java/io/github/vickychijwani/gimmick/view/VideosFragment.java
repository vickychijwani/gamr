package io.github.vickychijwani.gimmick.view;

import android.annotation.SuppressLint;
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
import android.widget.AbsListView;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;
import io.github.vickychijwani.giantbomb.item.Video;
import io.github.vickychijwani.gimmick.R;
import io.github.vickychijwani.gimmick.pref.AppState;
import io.github.vickychijwani.gimmick.pref.UserPrefs;
import io.github.vickychijwani.gimmick.view.adapter.VideoListAdapter;
import io.github.vickychijwani.utility.AppUtils;

public class VideosFragment extends DataFragment<List<Video>,VideoListAdapter> {

    private static final String TAG = "VideosFragment";

    private VideoListAdapter mAdapter;
    private List<Video> mVideoList;

    @InjectView(android.R.id.list) AbsListView mVideoListView;

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
        Collections.sort(videoList, new Comparator<Video>() {
            @Override
            public int compare(Video lhs, Video rhs) {
                return -lhs.compareTo(rhs);
            }
        });
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
            case UserPrefs.VIDEO_RES_LOW:
                videoUrl = video.getLowUrl();
                break;
            case UserPrefs.VIDEO_RES_HIGH:
                videoUrl = video.getHighUrl();
                break;
            default:
                throw new IllegalArgumentException("invalid video resolution choice " + choice + "!");
        }

        Log.i(TAG, "Playing " + ((choice == UserPrefs.VIDEO_RES_LOW) ? "low-res" : "high-res") + " video from " + videoUrl);
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
            BaseActivity activity = (BaseActivity) getActivity();
            int preferredVideoRes = activity.getUserPrefs().getInteger(UserPrefs.Key.VIDEO_RES);
            if (preferredVideoRes != UserPrefs.VIDEO_RES_ASK) {
                playVideo(video, preferredVideoRes);
            } else {
                new PlayVideoDialogFragment(video).show(activity.getFragmentManager(), null);
            }
        }
    };



    @SuppressLint("ValidFragment")
    private class PlayVideoDialogFragment extends DialogFragment {

        private final Video mVideo;
        private int mChoice;

        public PlayVideoDialogFragment(@NotNull Video video) {
            mVideo = video;
        }

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            final BaseActivity activity = (BaseActivity) getActivity();
            if (activity == null) {
                Log.e(TAG, "getActivity() returned null!");
                return null;
            }

            mChoice = activity.getAppState().getInteger(AppState.Key.VIDEO_RES_LAST_SELECTED);
            return new AlertDialog.Builder(activity)
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
                            activity.getAppState().setInteger(AppState.Key.VIDEO_RES_LAST_SELECTED, mChoice);
                            playVideo(mVideo, mChoice);
                        }
                    })
                    .setNegativeButton(R.string.always, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            activity.getAppState().setInteger(AppState.Key.VIDEO_RES_LAST_SELECTED, mChoice);
                            activity.getUserPrefs().setInteger(UserPrefs.Key.VIDEO_RES, mChoice);
                            playVideo(mVideo, mChoice);
                        }
                    })
                    .create();
        }
    }

}
