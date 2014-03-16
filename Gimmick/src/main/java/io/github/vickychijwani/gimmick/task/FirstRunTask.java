package io.github.vickychijwani.gimmick.task;

import android.os.AsyncTask;

import com.squareup.otto.Bus;

import org.jetbrains.annotations.NotNull;

import java.util.List;

import javax.inject.Inject;

import io.github.vickychijwani.giantbomb.api.PlatformsAPI;
import io.github.vickychijwani.giantbomb.item.Platform;
import io.github.vickychijwani.gimmick.database.GamrProvider;
import io.github.vickychijwani.gimmick.pref.AppState;
import io.github.vickychijwani.gimmick.utility.event.FirstRunTaskDoneEvent;

public class FirstRunTask extends AsyncTask<Void, Void, Boolean> {

    private final AppState mAppState;
    private final Bus mEventBus;
    private final PlatformsAPI mPlatformsAPI;

    @Inject
    public FirstRunTask(AppState appState, Bus eventBus, PlatformsAPI platformsAPI) {
        mAppState = appState;
        mEventBus = eventBus;
        mPlatformsAPI = platformsAPI;
    }

    @Override
    @NotNull
    protected Boolean doInBackground(Void... params) {
        List<Platform> platforms = mPlatformsAPI.fetchAll();
        boolean platformsAdded = GamrProvider.addPlatforms(platforms);

        mAppState.setBoolean(AppState.Key.FIRST_RUN, (! platformsAdded));
        return platformsAdded;
    }

    @Override
    protected void onPostExecute(@NotNull Boolean taskSucceeded) {
        mEventBus.post(new FirstRunTaskDoneEvent(taskSucceeded));
    }

}
