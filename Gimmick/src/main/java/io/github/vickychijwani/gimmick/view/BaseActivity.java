package io.github.vickychijwani.gimmick.view;

import android.app.ActionBar;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.NavUtils;
import android.support.v4.view.ViewPager;
import android.view.MenuItem;

import com.astuetz.PagerSlidingTabStrip;

import org.jetbrains.annotations.NotNull;

import io.github.vickychijwani.gimmick.R;
import io.github.vickychijwani.gimmick.adapter.BaseFragmentPagerAdapter;
import io.github.vickychijwani.gimmick.utility.DeviceUtils;

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

    protected void setupTabsAndViewPager(@NotNull Fragment[] fragments, @NotNull String[] tabTitles) {
        // setup viewpager
        ViewPager pager = (ViewPager) findViewById(R.id.pager);

        if (pager == null)
            return;

        pager.setAdapter(new BaseFragmentPagerAdapter(getSupportFragmentManager(), fragments, tabTitles));

        // bind tabs to viewpager
        final PagerSlidingTabStrip tabs = (PagerSlidingTabStrip) findViewById(R.id.tabs);
        tabs.setViewPager(pager);

        // hide keyboard on tab change, if it is open
        tabs.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                DeviceUtils.hideSoftKeyboard(BaseActivity.this, tabs.getWindowToken());
            }
        });
    }

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

    @Override
    public void startActivity(Intent intent) {
        super.startActivity(intent);
        overridePendingTransition(R.anim.blow_up_enter, R.anim.blow_up_exit);
    }

}
