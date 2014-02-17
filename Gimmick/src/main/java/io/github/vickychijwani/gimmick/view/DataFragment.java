package io.github.vickychijwani.gimmick.view;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Base class for fragments that depend on an external (possibly slow-loading) data-source.
 *
 * @param <D>   the type of data the fragment needs to construct its views.
 * @param <V>   the type of view to which data of type {@code <D>}
 */
public abstract class DataFragment<D,V> extends BaseFragment {

    /**
     * Callback to invoke when the required data is loaded (from disk, network, etc.)
     *
     * @param data  the data, loaded from some data-source
     */
    abstract void onDataLoaded(D data);

    /**
     * Bind the {@code data} received via {@link #onDataLoaded(D)} to the {@code view}. Both {@code
     * data} and {@code view} are guaranteed to be non-null.
     *
     * @param data the data received via {@link #onDataLoaded(D)}. Guaranteed to be non-null.
     * @param view the view with which to bind {@code data}. Guaranteed to be non-null.
     */
    protected abstract void onBindDataToView(@NotNull D data, @NotNull V view);

    /**
     * Calls {@link #onBindDataToView(D, V)} if appropriate (i.e., when both {@code data} and {@code
     * view} are non-null).
     */
    protected void bindDataToView(@Nullable D data, @Nullable V view) {
        if (data != null && view != null) {
            onBindDataToView(data, view);
        }
    }

}
