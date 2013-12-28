package io.github.vickychijwani.gimmick.background;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import io.github.vickychijwani.gimmick.R;
import io.github.vickychijwani.gimmick.item.SearchResult;

/**
 * Inspired by florianmski's traktoid TraktManager. This class is used to hold running tasks, so it
 * can execute independently from a running activity (so the application can still be used while the
 * update continues). A plain AsyncTask could do this, too, but here we can also restrict it to one
 * task running at a time.
 */
public class TaskManager {

    private static TaskManager sInstance;
    private final Handler mHandler = new Handler(Looper.getMainLooper());
    private AddGameTask mAddTask;
    private final Context mContext;

    private TaskManager(Context context) {
        mContext = context;
    }

    public static synchronized TaskManager getInstance(Context context) {
        // Use the application context, which will ensure that you
        // don't accidentally leak an Activity's context.
        // See http://android-developers.blogspot.in/2009/01/avoiding-memory-leaks.html
        if (sInstance == null) {
            sInstance = new TaskManager(context.getApplicationContext());
        }
        return sInstance;
    }

    public synchronized void performAddTask(SearchResult show) {
        List<SearchResult> wrapper = new ArrayList<SearchResult>();
        wrapper.add(show);
        performAddTask(wrapper, false);
    }

    public synchronized void performAddTask(final List<SearchResult> games,
                                            final boolean isSilent) {
        if (! isSilent) {
            if (games.size() == 1) {
                SearchResult game = games.get(0);
                Toast.makeText(mContext,
                        mContext.getString(R.string.add_started, game.name),
                        Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(mContext,
                        R.string.add_multiple,
                        Toast.LENGTH_SHORT).show();
            }
        }

        // add the show(s) to a running add task or create a new one
        boolean isRequiringNewTask;
        if (! isAddTaskRunning()) {
            isRequiringNewTask = true;
        } else {
            // addTask is still running, try to add to its queue
            isRequiringNewTask = ! mAddTask.addGames(games);
        }

        if (isRequiringNewTask) {
            // ensure this is called on our main thread (AsyncTask needs access to it)
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    mAddTask = (AddGameTask) new AddGameTask(mContext, games).execute();
                }
            });
        }
    }

    public boolean isAddTaskRunning() {
        return ! (mAddTask == null || mAddTask.getStatus() == android.os.AsyncTask.Status.FINISHED);
    }

}
