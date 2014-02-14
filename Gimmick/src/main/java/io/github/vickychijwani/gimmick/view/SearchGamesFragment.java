package io.github.vickychijwani.gimmick.view;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Toast;

import com.meetme.android.multistateview.MultiStateView;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import io.github.vickychijwani.gimmick.R;
import io.github.vickychijwani.gimmick.api.GiantBomb;
import io.github.vickychijwani.gimmick.api.NetworkRequestQueue;
import io.github.vickychijwani.gimmick.api.RequestTag;
import io.github.vickychijwani.gimmick.utility.NetworkUtils;

public class SearchGamesFragment extends AddGamesFragment {

    private RequestTag mRequestTag;

    @InjectView(R.id.searchbox) EditText mSearchBox;
    @InjectView(R.id.clear_button) ImageButton mClearButton;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        View view = inflater.inflate(R.layout.fragment_search_games, container, false);
        ButterKnife.inject(this, view);

        mGameListContainer.setState(MultiStateView.ContentState.EMPTY);
        setupAdapter();

        mSearchBox.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                // we only want to react to down events
                if (event.getAction() != KeyEvent.ACTION_DOWN)
                    return false;

                if (keyCode == KeyEvent.KEYCODE_ENTER) {
                    initiateRequest();
                    return true;
                } else {
                    return false;
                }
            }
        });

        return view;
    }

    @Override
    protected void initiateRequest() {
        if (! NetworkUtils.isNetworkConnected(getActivity())) {
            Toast.makeText(getActivity(), R.string.offline, Toast.LENGTH_LONG).show();
            return;
        }

        assert mSearchBox.getText() != null;
        String query = mSearchBox.getText().toString().trim();
        if (TextUtils.isEmpty(query)) {
            return;
        }

        mGameListContainer.setState(MultiStateView.ContentState.LOADING);
        mRequestTag = GiantBomb.searchGames(query, getResultsHandler(), getErrorHandler());
    }

    @Override
    protected void cancelPendingRequests() {
        if (mRequestTag != null) {
            NetworkRequestQueue.cancelPending(mRequestTag);
            mGameListContainer.setState(MultiStateView.ContentState.EMPTY);
        }
    }

    @OnClick(R.id.clear_button) void clearInput() {
        mSearchBox.setText("");
    }

}
