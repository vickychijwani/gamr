package io.github.vickychijwani.gimmick.view;

import android.app.ActionBar;
import android.os.Bundle;
import android.support.v4.app.Fragment;

import org.jetbrains.annotations.NotNull;

import io.github.vickychijwani.gimmick.R;

public class AddGamesActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_games);

        // setup fragments
        Fragment[] fragments = new Fragment[] {
                new SearchGamesFragment(),
                new UpcomingGamesFragment()
        };
        String[] tabTitles = new String[] {
                getString(R.string.search),
                getString(R.string.upcoming)
        };

        setupTabsAndViewPager(fragments, tabTitles);
    }

    @Override
    protected void setupActionBar(@NotNull ActionBar actionBar) {
        actionBar.setDisplayHomeAsUpEnabled(true);
    }

}
