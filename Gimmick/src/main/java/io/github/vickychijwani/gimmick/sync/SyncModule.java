package io.github.vickychijwani.gimmick.sync;

import android.content.Context;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import io.github.vickychijwani.gimmick.dagger.ApplicationContext;

@Module(
        library = true,
        complete = false
)
public class SyncModule {

    @Provides @Singleton
    SyncAdapter provideSyncAdapter(@ApplicationContext Context context) {
        return new SyncAdapter(context, true);
    }

}
