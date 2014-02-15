package io.github.vickychijwani.gimmick.utility;

import android.widget.ArrayAdapter;

import org.jetbrains.annotations.Nullable;

import java.util.List;

public class AppUtils {

    /**
     * Change the data set in an {@link ArrayAdapter}. This function calls {@link
     * android.widget.BaseAdapter#notifyDataSetChanged()} internally.
     *
     * @param adapter the adapter on which to perform the operation
     * @param list    the new data set to be swapped in (can be null, in which case the adapter will
     *                simply be cleared)
     * @param <T>     the type of data contained in the adapter
     */
    public static <T> void changeAdapterDataSet(ArrayAdapter<T> adapter, @Nullable List<T> list) {
        adapter.setNotifyOnChange(false);
        adapter.clear();
        if (list != null) {
            adapter.addAll(list);
        }
        adapter.notifyDataSetChanged();
    }

}
