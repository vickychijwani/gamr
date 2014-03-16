package io.github.vickychijwani.gimmick.view;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;

import com.meetme.android.multistateview.MultiStateView;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import io.github.vickychijwani.giantbomb.item.GameList;
import io.github.vickychijwani.gimmick.R;
import io.github.vickychijwani.utility.AppUtils;
import io.github.vickychijwani.utility.DeviceUtils;

public class SearchGamesFragment extends AddGamesFragment {

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
                    DeviceUtils.hideSoftKeyboard(getActivity(), getView().getWindowToken());
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
        if (AppUtils.showErrorIfOffline(getActivity())) {
            return;
        }

        assert mSearchBox.getText() != null;
        String query = mSearchBox.getText().toString().trim();
        if (TextUtils.isEmpty(query)) {
            return;
        }

        setRequestTag(mGamesAPI.search(query, getResultsHandler(), getErrorHandler()));
        onRequestInitiated();
    }

    @Override
    protected void onReceivedResults(GameList gameList) {
        gameList.sortByLatestFirst();
    }

    @OnClick(R.id.clear_button) void clearInput() {
        mSearchBox.setText("");
        mSearchBox.requestFocus();
        DeviceUtils.showSoftKeyboard(getActivity());
    }

}
