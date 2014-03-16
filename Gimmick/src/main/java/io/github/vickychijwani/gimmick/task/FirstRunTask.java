package io.github.vickychijwani.gimmick.task;

import android.os.AsyncTask;

import com.squareup.otto.Bus;

import org.jetbrains.annotations.NotNull;

import java.util.List;

import javax.inject.Inject;

import io.github.vickychijwani.giantbomb.api.PlatformsAPI;
import io.github.vickychijwani.giantbomb.api.ResourceTypesAPI;
import io.github.vickychijwani.giantbomb.item.Platform;
import io.github.vickychijwani.giantbomb.item.ResourceType;
import io.github.vickychijwani.gimmick.database.GamrProvider;
import io.github.vickychijwani.gimmick.pref.AppState;
import io.github.vickychijwani.gimmick.utility.event.FirstRunTaskDoneEvent;

public class FirstRunTask extends AsyncTask<Void, Void, Boolean> {

    private final AppState mAppState;
    private final Bus mEventBus;
    private final ResourceTypesAPI mResourceTypesAPI;
    private final PlatformsAPI mPlatformsAPI;

    @Inject
    public FirstRunTask(AppState appState, Bus eventBus, ResourceTypesAPI resourceTypesAPI, PlatformsAPI platformsAPI) {
        mAppState = appState;
        mEventBus = eventBus;
        mResourceTypesAPI = resourceTypesAPI;
        mPlatformsAPI = platformsAPI;
    }

    @Override
    @NotNull
    protected Boolean doInBackground(Void... params) {
        List<ResourceType> resourceTypes = mResourceTypesAPI.fetchAll();
        boolean resourceTypesAdded = GamrProvider.addResourceTypes(resourceTypes);

        List<Platform> platforms = mPlatformsAPI.fetchAll();
        boolean platformsAdded = GamrProvider.addPlatforms(platforms);

        boolean taskSucceeded = resourceTypesAdded && platformsAdded;

        mAppState.setBoolean(AppState.Key.FIRST_RUN, (! taskSucceeded));
        return taskSucceeded;
    }

    @Override
    protected void onPostExecute(@NotNull Boolean taskSucceeded) {
        mEventBus.post(new FirstRunTaskDoneEvent(taskSucceeded));
    }

}
