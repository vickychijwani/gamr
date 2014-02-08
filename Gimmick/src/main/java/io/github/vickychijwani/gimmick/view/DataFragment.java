package io.github.vickychijwani.gimmick.view;

import android.database.Cursor;
import android.support.v4.app.Fragment;

public abstract class DataFragment extends Fragment {

    abstract void onDataLoaded(Cursor cursor);

}
