package io.github.vickychijwani.gimmick.view;

import android.support.v4.app.Fragment;

public abstract class DataFragment<T> extends Fragment {

    abstract void onDataLoaded(T data);

}
