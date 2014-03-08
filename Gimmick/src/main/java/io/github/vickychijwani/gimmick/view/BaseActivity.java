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

import java.util.Arrays;
import java.util.List;

import io.github.vickychijwani.gimmick.R;
import io.github.vickychijwani.gimmick.utility.EventBus;
import io.github.vickychijwani.gimmick.view.adapter.BaseFragmentPagerAdapter;
import io.github.vickychijwani.utility.DeviceUtils;

public abstract class BaseActivity extends FragmentActivity {

    private Object mEventHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ActionBar actionBar = getActionBar();
        actionBar.setIcon(R.drawable.ic_logo);
        setupActionBar(actionBar);
    }

    @Override
    protected void onPause() {
        if (usesEventBus() && mEventHandler != null) {
            EventBus.getInstance().unregister(mEventHandler);
        }
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (usesEventBus()) {
            mEventHandler = getEventHandler();
            EventBus.getInstance().register(mEventHandler);
        }
    }

    /**
     * Setup the action bar for this activity. Called at the beginning of {@link #onCreate}.
     */
    protected abstract void setupActionBar(@NotNull ActionBar actionBar);

    /**
     * Setup a {@link ViewPager} and tabs for this activity.
     *
     * @param fragments      the fragments to page through
     * @param tabTitles      the title for each tab
     * @param offscreenLimit the number of tabs whose view hierarchy is to be retained in memory. If
     *                       less than 1, the default value will be used.
     */
    protected void setupTabsAndViewPager(@NotNull List<Fragment> fragments, @NotNull String[] tabTitles, int offscreenLimit) {
        // setup viewpager
        ViewPager pager = (ViewPager) findViewById(R.id.pager);

        if (pager == null)
            return;

        if (offscreenLimit >= 1) {
            pager.setOffscreenPageLimit(offscreenLimit);
        }

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

    /**
     * Setup a {@link ViewPager} and tabs for this activity.
     *
     * @param fragments      the fragments to page through
     * @param tabTitles      the title for each tab
     * @param offscreenLimit the number of tabs whose view hierarchy is to be retained in memory. If
     *                       less than 1, the default value will be used.
     */
    protected void setupTabsAndViewPager(@NotNull Fragment[] fragments, @NotNull String[] tabTitles, int offscreenLimit) {
        setupTabsAndViewPager(Arrays.asList(fragments), tabTitles, offscreenLimit);
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

    /**
     * Override this and return true if your activity subscribes to (or produces initial events for)
     * the global {@link EventBus}. Also override {@link #getEventHandler()} and return an
     * {@link Object} having methods annotated with {@link com.squareup.otto.Subscribe}.
     *
     * @see #getEventHandler()
     */
    protected boolean usesEventBus() {
        return false;
    }

    /**
     * Override this and return an {@link Object} having methods annotated with {@link
     * com.squareup.otto.Subscribe}. Also override {@link #usesEventBus()} and return true if your
     * activity subscribes to (or produces initial events for) the global {@link EventBus}.
     *
     * @return an {@link Object} having methods annotated with {@link com.squareup.otto.Subscribe}.
     * @see #usesEventBus()
     */
    @NotNull
    protected Object getEventHandler() {
        return new Object();
    }

}
