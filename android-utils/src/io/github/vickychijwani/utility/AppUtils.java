package io.github.vickychijwani.utility;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import org.jetbrains.annotations.NotNull;
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
    public static <T> void changeAdapterDataSet(@NotNull ArrayAdapter<T> adapter, @Nullable List<T> list) {
        adapter.setNotifyOnChange(false);
        adapter.clear();
        if (list != null) {
            adapter.addAll(list);
        }
        adapter.notifyDataSetChanged();
    }

    /**
     * Check if the specified {@link Intent} can be handled by any {@link android.app.Activity} on
     * the system.
     *
     * @param context   used for querying available Activities on the system
     * @param intent    the {@link Intent} to resolve
     * @return          true if the {@link Intent} can be resolved
     */
    public static boolean isIntentResolvable(@NotNull Context context, @NotNull Intent intent) {
        PackageManager pm = context.getPackageManager();
        if (pm == null) {
            return false;
        }
        List<ResolveInfo> resolvedList = pm.queryIntentActivities(intent,
                PackageManager.MATCH_DEFAULT_ONLY);
        return resolvedList.size() > 0;
    }

    /**
     * Play a video in an external video player if a network connection is available, else display
     * an error.
     *
     * @param context   used to invoke the play intent
     * @param url       the web address of the video to be played
     */
    public static void playVideoFromUrl(@NotNull Context context, @NotNull String url) {
        if (showErrorIfOffline(context)) {
            return;
        }

        Intent playIntent = new Intent(Intent.ACTION_VIEW);
        playIntent.setDataAndType(Uri.parse(url), "video/*");
        if (isIntentResolvable(context, playIntent)) {
            context.startActivity(playIntent);
        } else {
            // TODO no video player! show error and link to Play Store!
        }
    }

    /**
     * Displays an error message if no working network connection is found. Also returns true in
     * such a case, else false.
     *
     * @return  true if an error was shown, else false
     * @see     NetworkUtils#isNetworkConnected(Context)
     */
    public static boolean showErrorIfOffline(@NotNull Context context) {
        if (! NetworkUtils.isNetworkConnected(context)) {
            Toast.makeText(context, context.getString(R.string.offline), Toast.LENGTH_LONG).show();
            return true;
        }
        return false;
    }

}
