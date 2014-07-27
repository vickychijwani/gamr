package io.github.vickychijwani.gimmick.view;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;
import io.github.vickychijwani.giantbomb.item.Review;
import io.github.vickychijwani.gimmick.R;
import io.github.vickychijwani.gimmick.view.adapter.ReviewListAdapter;
import io.github.vickychijwani.utility.AppUtils;

public class ReviewsFragment extends DataFragment<List<Review>,ReviewListAdapter> {

    private static final String TAG = "ReviewsFragment";

    private ReviewListAdapter mAdapter;
    private List<Review> mReviewList;

    @InjectView(android.R.id.list) AbsListView mReviewListView;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        View view = inflater.inflate(R.layout.fragment_reviews, container, false);
        ButterKnife.inject(this, view);

        mAdapter = new ReviewListAdapter(getActivity(), new ArrayList<Review>());
        mReviewListView.setAdapter(mAdapter);

        bindDataToView(mReviewList, mAdapter);

        return view;
    }

    @Override
    void onDataLoaded(List<Review> reviewList) {
        mReviewList = reviewList;
        bindDataToView(mReviewList, mAdapter);
    }

    @Override
    protected void onBindDataToView(@NotNull List<Review> data, @NotNull ReviewListAdapter adapter) {
        AppUtils.changeAdapterDataSet(adapter, data);
    }

}
