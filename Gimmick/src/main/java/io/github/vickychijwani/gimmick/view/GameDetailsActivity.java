package io.github.vickychijwani.gimmick.view;

import android.app.ActionBar;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.util.Log;

import org.jetbrains.annotations.NotNull;

import io.github.vickychijwani.gimmick.R;

public class GameDetailsActivity extends BaseActivity {

    private static final String TAG = "GameDetailsActivity";

    public interface IntentFields {
        String GAME_GIANT_BOMB_ID = "game_giant_bomb_id";
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game_details);

        int gameId = getIntent().getIntExtra(IntentFields.GAME_GIANT_BOMB_ID, -1);
        if (gameId == -1) {
            finish();
            return;
        }

        Log.d(TAG, "Displaying details for game ID = " + gameId);
    }

    @Override
    protected void setupActionBar(@NotNull ActionBar actionBar) {
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

        ActionBar.TabListener tabListener = new ActionBar.TabListener() {
            @Override
            public void onTabSelected(ActionBar.Tab tab, FragmentTransaction ft) {

            }

            @Override
            public void onTabUnselected(ActionBar.Tab tab, FragmentTransaction ft) {

            }

            @Override
            public void onTabReselected(ActionBar.Tab tab, FragmentTransaction ft) {

            }
        };

        actionBar.addTab(actionBar.newTab().setText("Overview").setTabListener(tabListener));
        actionBar.addTab(actionBar.newTab().setText("Images").setTabListener(tabListener));
        actionBar.addTab(actionBar.newTab().setText("Videos").setTabListener(tabListener));
    }

}
