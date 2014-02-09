package io.github.vickychijwani.gimmick.background;

import android.content.Context;
import android.content.res.Resources;
import android.util.Log;
import android.widget.Toast;

import java.util.LinkedList;
import java.util.List;

import io.github.vickychijwani.gimmick.R;
import io.github.vickychijwani.gimmick.api.GiantBomb;
import io.github.vickychijwani.gimmick.api.Metacritic;
import io.github.vickychijwani.gimmick.database.GamrProvider;
import io.github.vickychijwani.gimmick.item.SearchResult;
import io.github.vickychijwani.gimmick.utility.NetworkUtils;

public class AddGameTask extends android.os.AsyncTask<Void, AddGameTask.Result, Void> {

    private final Context mContext;
    private final LinkedList<SearchResult> mAddQueue = new LinkedList<SearchResult>();

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

    public AddGameTask(Context context, List<SearchResult> games) {
        // use an activity-independent context
        mContext = context.getApplicationContext();
        mAddQueue.addAll(games);
    }

    /**
     * Adds shows to the add queue. If this returns false, the shows were not
     * added because the task is finishing up. Create a new one instead.
     */
    public boolean addGames(List<SearchResult> games) {
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

                SearchResult game = mAddQueue.removeFirst();
                SearchResult fullGame = GiantBomb.fetchGame(game.giantBombUrl);

                if (fullGame == null) {
                    result = new Result(StatusCode.UNKNOWN_ERROR, game.name);
                }
                else {
                    Metacritic.fetchRating(fullGame);
                    try {
                        if (GamrProvider.addGame(fullGame)) {
                            result = new Result(StatusCode.SUCCESS, game.name);
                        } else {
                            result = new Result(StatusCode.ALREADY_EXISTS, game.name);
                        }
                    } catch (Exception e) {
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
