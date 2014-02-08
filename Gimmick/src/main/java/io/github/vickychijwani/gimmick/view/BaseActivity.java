package io.github.vickychijwani.gimmick.view;

import android.app.ActionBar;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.NavUtils;
import android.view.MenuItem;

import org.jetbrains.annotations.NotNull;

import io.github.vickychijwani.gimmick.R;

public abstract class BaseActivity extends FragmentActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ActionBar actionBar = getActionBar();
        actionBar.setIcon(R.drawable.ic_logo);
        setupActionBar(actionBar);
    }

    /**
     * Setup the action bar for this activity. Called at the beginning of {@link #onCreate}.
     */
    protected abstract void setupActionBar(@NotNull ActionBar actionBar);

    @NotNull
    @Override
    public ActionBar getActionBar() {
        ActionBar actionBar = super.getActionBar();
        assert actionBar != null;
        return actionBar;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                NavUtils.navigateUpFromSameTask(this);
                overridePendingTransition(R.anim.shrink_enter, R.anim.shrink_exit);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.shrink_enter, R.anim.shrink_exit);
    }

}
