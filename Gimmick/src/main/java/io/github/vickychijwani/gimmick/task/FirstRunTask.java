package io.github.vickychijwani.gimmick.task;

import android.os.AsyncTask;

import org.jetbrains.annotations.NotNull;

import java.util.List;

import io.github.vickychijwani.giantbomb.api.GiantBomb;
import io.github.vickychijwani.giantbomb.item.Platform;
import io.github.vickychijwani.giantbomb.item.ResourceType;
import io.github.vickychijwani.gimmick.database.GamrProvider;
import io.github.vickychijwani.gimmick.pref.AppState;
import io.github.vickychijwani.gimmick.utility.EventBus;
import io.github.vickychijwani.gimmick.utility.event.FirstRunTaskDoneEvent;

public class FirstRunTask extends AsyncTask<Void, Void, Boolean> {

    @Override
    @NotNull
    protected Boolean doInBackground(Void... params) {
        List<ResourceType> resourceTypes = GiantBomb.ResourceTypes.fetchAll();
        boolean resourceTypesAdded = GamrProvider.addResourceTypes(resourceTypes);

        List<Platform> platforms = GiantBomb.Platforms.fetchAll();
        boolean platformsAdded = GamrProvider.addPlatforms(platforms);

        boolean taskSucceeded = resourceTypesAdded && platformsAdded;

        AppState.getInstance().setBoolean(AppState.Key.FIRST_RUN, (! taskSucceeded));
        return taskSucceeded;
    }

    @Override
    protected void onPostExecute(@NotNull Boolean taskSucceeded) {
        EventBus.getInstance().post(new FirstRunTaskDoneEvent(taskSucceeded));
    }

}
