package io.github.vickychijwani.gimmick.view;

public abstract class DataFragment<T> extends BaseFragment {

    abstract void onDataLoaded(T data);

}
