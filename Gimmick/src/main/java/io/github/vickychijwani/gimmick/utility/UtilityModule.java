package io.github.vickychijwani.gimmick.utility;

import com.squareup.otto.Bus;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

@Module(
        library = true
)
public class UtilityModule {

    @Provides @Singleton
    Bus provideBus() {
        return new Bus();
    }

}
