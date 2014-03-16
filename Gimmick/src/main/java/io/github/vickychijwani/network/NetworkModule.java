package io.github.vickychijwani.network;

import android.content.Context;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;

import dagger.Module;
import dagger.Provides;
import io.github.vickychijwani.gimmick.dagger.ApplicationContext;

@Module(
        library = true,
        complete = false
)
public class NetworkModule {

    @Provides
    RequestQueue provideRequestQueue(@ApplicationContext Context context) {
        return Volley.newRequestQueue(context);
    }

}
