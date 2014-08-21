package io.github.vickychijwani.gimmick.view;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
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

    private final View.OnClickListener mItemClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (AppUtils.showErrorIfOffline(getActivity())) {
                return;
            }

            int position = mVideoListView.getPositionForView(v);
            Video video = mAdapter.getItem(position);
            BaseActivity activity = (BaseActivity) getActivity();
            int preferredResolution = activity.getUserPrefs().getInteger(UserPrefs.Key.VIDEO_RES);
            if (preferredResolution != UserPrefs.VIDEO_RES_ASK) {
                // can't call PlayVideoDialogFragment.playVideo() because that only works after it is attached to an activity
                String videoUrl = (preferredResolution == UserPrefs.VIDEO_RES_LOW) ? video.getLowUrl() : video.getHighUrl();
                Log.i(TAG, "Playing " + ((preferredResolution == UserPrefs.VIDEO_RES_LOW) ? "low-res" : "high-res") + " video from " + videoUrl);
                AppUtils.playVideoFromUrl(activity, videoUrl);
            } else {
                PlayVideoDialogFragment playFragment = PlayVideoDialogFragment.newInstance(video);
                playFragment.show(activity.getFragmentManager(), null);
            }
        }
    };



    // public because the OS can re-create the fragment by itself, e.g., on orientation change
    // static to avoid leaking the outer class instance by holding a reference to it
    // see http://stackoverflow.com/a/14011878
    public static class PlayVideoDialogFragment extends DialogFragment {

        private static final String ARG_LOW_URL = "low_url";
        private static final String ARG_HIGH_URL = "high_url";

        private int mChoice = UserPrefs.VIDEO_RES_ASK;

        // static function newInstance() instead of parameterized constructor to allow Android to
        // destroy and re-create this fragment at will (Android saves the Bundle arguments on
        // destruction and passes them along to the new instance on re-creation)
        // see http://developer.android.com/reference/android/app/DialogFragment.html#AlertDialog
        public static PlayVideoDialogFragment newInstance(@NotNull Video video) {
            PlayVideoDialogFragment fragment = new PlayVideoDialogFragment();
            Bundle args = new Bundle();
            args.putString(ARG_LOW_URL, video.getLowUrl());
            args.putString(ARG_HIGH_URL, video.getHighUrl());
            fragment.setArguments(args);
            return fragment;
        }

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            final BaseActivity activity = (BaseActivity) getActivity();
            mChoice = activity.getAppState().getInteger(AppState.Key.VIDEO_RES_LAST_SELECTED);

            return new AlertDialog.Builder(activity)
                    .setTitle(R.string.play_which_version)
                    .setSingleChoiceItems(R.array.video_resolutions, mChoice, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            mChoice = which;
                            activity.getAppState().setInteger(AppState.Key.VIDEO_RES_LAST_SELECTED, mChoice);
                        }
                    })
                    .setPositiveButton(R.string.just_once, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            playVideo();
                        }
                    })
                    .setNegativeButton(R.string.always, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            activity.getUserPrefs().setInteger(UserPrefs.Key.VIDEO_RES, mChoice);
                            playVideo();
                        }
                    })
                    .create();
        }

        private void playVideo() {
            String videoUrl = getArguments().getString((mChoice == UserPrefs.VIDEO_RES_LOW) ? ARG_LOW_URL : ARG_HIGH_URL);
            Log.i(TAG, "Playing " + ((mChoice == UserPrefs.VIDEO_RES_LOW) ? "low-res" : "high-res") + " video from " + videoUrl);
            AppUtils.playVideoFromUrl(getActivity(), videoUrl);
        }

    }

}
