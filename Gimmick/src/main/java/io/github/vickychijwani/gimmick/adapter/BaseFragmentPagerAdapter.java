package io.github.vickychijwani.gimmick.adapter;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import org.jetbrains.annotations.NotNull;

public class BaseFragmentPagerAdapter extends FragmentPagerAdapter {
    private final Fragment[] mFragments;
    private final String[] mTitles;

    public BaseFragmentPagerAdapter(FragmentManager fm, @NotNull Fragment[] fragments, @NotNull String[] titles) {
        super(fm);
        mFragments = fragments;
        mTitles = titles;
    }

    @Override
    public Fragment getItem(int position) {
        return mFragments[position];
    }

    @Override
    public int getCount() {
        return mFragments.length;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return mTitles[position];
    }
}

