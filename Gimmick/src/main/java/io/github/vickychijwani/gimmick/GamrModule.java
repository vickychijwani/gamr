package io.github.vickychijwani.gimmick;

import android.content.Context;

import dagger.Module;
import dagger.Provides;
import io.github.vickychijwani.giantbomb.api.GiantBombAPIModule;
import io.github.vickychijwani.gimmick.dagger.ApplicationContext;
import io.github.vickychijwani.gimmick.utility.UtilityModule;
import io.github.vickychijwani.gimmick.view.AddGamesActivity;
import io.github.vickychijwani.gimmick.view.AddGamesFragment;
import io.github.vickychijwani.gimmick.view.BaseActivity;
import io.github.vickychijwani.gimmick.view.GameDetailsActivity;
import io.github.vickychijwani.gimmick.view.LauncherActivity;
import io.github.vickychijwani.gimmick.view.LibraryActivity;
import io.github.vickychijwani.gimmick.view.RecentGamesFragment;
import io.github.vickychijwani.gimmick.view.SearchGamesFragment;
import io.github.vickychijwani.gimmick.view.UpcomingGamesFragment;
import io.github.vickychijwani.gimmick.view.adapter.AddGamesAdapter;
import io.github.vickychijwani.metacritic.api.MetacriticAPIModule;
import io.github.vickychijwani.network.NetworkModule;

@Module(
        includes = {
                UtilityModule.class,
                NetworkModule.class,
                GiantBombAPIModule.class,
                MetacriticAPIModule.class
        },
        injects = {
                AddGamesFragment.class,
                UpcomingGamesFragment.class,
                RecentGamesFragment.class,
                SearchGamesFragment.class,

                BaseActivity.class,
                LauncherActivity.class,
                LibraryActivity.class,
                GameDetailsActivity.class,
                AddGamesActivity.class,

                AddGamesAdapter.class
        }
)
public class GamrModule {

    private final GamrApplication mGamrApplication;

    public GamrModule(GamrApplication app) {
        mGamrApplication = app;
    }

    @Provides @ApplicationContext
    Context provideApplicationContext() {
        return mGamrApplication.getApplicationContext();
    }

}
