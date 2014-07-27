package io.github.vickychijwani.gimmick.task;

import android.content.Context;
import android.content.res.Resources;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import java.util.LinkedList;

import io.github.vickychijwani.giantbomb.api.GamesAPI;
import io.github.vickychijwani.giantbomb.api.ReviewsAPI;
import io.github.vickychijwani.giantbomb.api.VideosAPI;
import io.github.vickychijwani.giantbomb.item.Game;
import io.github.vickychijwani.giantbomb.item.GameList;
import io.github.vickychijwani.gimmick.R;
import io.github.vickychijwani.gimmick.database.GamrProvider;
import io.github.vickychijwani.metacritic.api.MetacriticAPI;
import io.github.vickychijwani.utility.NetworkUtils;

public class AddGameTask extends AsyncTask<Void, AddGameTask.Result, Void> {

    private final Context mContext;
    private final LinkedList<Game> mAddQueue = new LinkedList<Game>();
    private final GamesAPI mGamesAPI;
    private final VideosAPI mVideosAPI;
    private final ReviewsAPI mReviewsAPI;
    private final MetacriticAPI mMetacriticAPI;

    private boolean mIsFinishedAddingGames = false;

    private static enum StatusCode {
        OFFLINE,
        SUCCESS,
        ALREADY_EXISTS,
        UNKNOWN_ERROR
    }

    protected static class Result {
        public final StatusCode statusCode;
        public final String gameName;

        public Result(final StatusCode statusCode) {
            this(statusCode, "");
        }

        public Result(final StatusCode statusCode, final String gameName) {
            this.statusCode = statusCode;
            this.gameName = gameName;
        }
    }

    private static final String TAG = "AddGameTask";

    public AddGameTask(Context context, GameList games, GamesAPI gamesAPI, VideosAPI videosAPI,
                       ReviewsAPI reviewsAPI, MetacriticAPI metacriticAPI) {
        // use an activity-independent context
        mContext = context.getApplicationContext();
        mAddQueue.addAll(games);
        mGamesAPI = gamesAPI;
        mVideosAPI = videosAPI;
        mReviewsAPI = reviewsAPI;
        mMetacriticAPI = metacriticAPI;
    }

    /**
     * Adds shows to the add queue. If this returns false, the shows were not
     * added because the task is finishing up. Create a new one instead.
     */
    public boolean addGames(GameList games) {
        Log.d(TAG, "Trying to add games to queue...");
        if (mIsFinishedAddingGames) {
            Log.d(TAG, "FAILED. Already finishing up.");
            return false;
        } else {
            mAddQueue.addAll(games);
            Log.d(TAG, "SUCCESS.");
            return true;
        }
    }

    @Override
    protected Void doInBackground(Void... params) {
        Log.d(TAG, "Starting to add games...");

        try {
            if (mAddQueue.isEmpty()) {
                Log.d(TAG, "Finished. Queue was empty.");
                return null;
            }

            if (! NetworkUtils.isNetworkConnected(mContext)) {
                Log.d(TAG, "Finished. No internet connection.");
                publishProgress(new Result(StatusCode.OFFLINE));
                return null;
            }

            if (isCancelled()) {
                Log.d(TAG, "Finished. Cancelled.");
                return null;
            }

            Result result;

            while (! mAddQueue.isEmpty()) {
                Log.d(TAG, "Starting to add next game...");
                if (isCancelled()) {
                    Log.d(TAG, "Finished. Cancelled.");
                    return null;
                }

                if (! NetworkUtils.isNetworkConnected(mContext)) {
                    Log.d(TAG, "Finished. No internet connection.");
                    publishProgress(new Result(StatusCode.OFFLINE));
                    break;
                }

                Game game = mAddQueue.removeFirst();
                Game fullGame = mGamesAPI.fetch(game.giantBombId);

                if (fullGame == null) {
                    result = new Result(StatusCode.UNKNOWN_ERROR, game.name);
                } else {
                    mMetacriticAPI.fetchMetascore(fullGame);    // metascore is not essential

                    try {
                        mVideosAPI.fetchAllForGame(fullGame); // videos are required when adding games
                        mReviewsAPI.fetchAllForGame(fullGame);  // reviews are required when adding games
                        if (GamrProvider.addGame(fullGame)) {
                            result = new Result(StatusCode.SUCCESS, game.name);
                        } else {
                            result = new Result(StatusCode.ALREADY_EXISTS, game.name);
                        }
                    } catch (Exception e) {
                        Log.e(TAG, Log.getStackTraceString(e));
                        result = new Result(StatusCode.UNKNOWN_ERROR, game.name);
                    }
                }

                publishProgress(result);
                Log.d(TAG, "Finished adding game (result code: " + result.statusCode + ")");
            }

            Log.d(TAG, "Finished adding all games.");
            return null;
        } finally {
            mIsFinishedAddingGames = true;
        }
    }

    @Override
    protected void onProgressUpdate(Result... values) {
        Resources resources = mContext.getResources();

        switch (values[0].statusCode) {
            case SUCCESS:
                Toast.makeText(mContext,
                        resources.getString(R.string.add_success, values[0].gameName),
                        Toast.LENGTH_SHORT).show();
                break;
            case ALREADY_EXISTS:
                Toast.makeText(mContext,
                        resources.getString(R.string.add_already_exists, values[0].gameName),
                        Toast.LENGTH_LONG).show();
                break;
            case UNKNOWN_ERROR:
                Toast.makeText(mContext,
                        resources.getString(R.string.add_unknown_error, values[0].gameName),
                        Toast.LENGTH_LONG).show();
                break;
            case OFFLINE:
                Toast.makeText(mContext,
                        R.string.offline,
                        Toast.LENGTH_LONG).show();
                break;
        }
    }

}
