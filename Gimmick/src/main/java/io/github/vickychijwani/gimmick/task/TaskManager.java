package io.github.vickychijwani.gimmick.task;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Looper;

import javax.inject.Inject;
import javax.inject.Singleton;

import io.github.vickychijwani.giantbomb.api.GamesAPI;
import io.github.vickychijwani.giantbomb.api.ReviewsAPI;
import io.github.vickychijwani.giantbomb.api.VideosAPI;
import io.github.vickychijwani.giantbomb.item.Game;
import io.github.vickychijwani.giantbomb.item.GameList;
import io.github.vickychijwani.gimmick.dagger.ApplicationContext;
import io.github.vickychijwani.metacritic.api.MetacriticAPI;

/**
 * Inspired by florianmski's traktoid TraktManager. This class is used to hold running tasks, so it
 * can execute independently from a running activity (so the application can still be used while the
 * update continues). A plain AsyncTask could do this, too, but here we can also restrict it to one
 * task running at a time.
 */
@Singleton
public class TaskManager {

    private final Handler mHandler = new Handler(Looper.getMainLooper());
    private AddGameTask mAddTask;
    private final Context mContext;
    private final GamesAPI mGamesAPI;
    private final VideosAPI mVideosAPI;
    private final ReviewsAPI mReviewsAPI;
    private final MetacriticAPI mMetacriticAPI;

    @Inject
    TaskManager(@ApplicationContext Context context, GamesAPI gamesAPI, VideosAPI videosAPI,
                ReviewsAPI reviewsAPI, MetacriticAPI metacriticAPI) {
        mContext = context;
        mGamesAPI = gamesAPI;
        mVideosAPI = videosAPI;
        mReviewsAPI = reviewsAPI;
        mMetacriticAPI = metacriticAPI;
    }

    public synchronized void performAddTask(Game game) {
        GameList wrapper = new GameList();
        wrapper.add(game);
        performAddTask(wrapper, false);
    }

    public synchronized void performAddTask(final GameList games,
                                            final boolean isSilent) {
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
                    mAddTask = new AddGameTask(mContext, games, mGamesAPI, mVideosAPI, mReviewsAPI,
                            mMetacriticAPI);
                    mAddTask.execute();
                }
            });
        }
    }

    public boolean isAddTaskRunning() {
        return ! (mAddTask == null || mAddTask.getStatus() == AsyncTask.Status.FINISHED);
    }

}
