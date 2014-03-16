package io.github.vickychijwani.gimmick;

import android.annotation.SuppressLint;
import android.app.Application;
import android.content.Context;
import android.os.StrictMode;

import java.util.Map;

import dagger.ObjectGraph;
import io.github.vickychijwani.giantbomb.api.GiantBombAPIModule;
import io.github.vickychijwani.giantbomb.item.ResourceType;
import io.github.vickychijwani.gimmick.database.GamrProvider;
import io.github.vickychijwani.metacritic.api.MetacriticAPIModule;
import io.github.vickychijwani.utility.DeviceUtils;

public class GamrApplication extends Application {

    /**
     * The content authority used to identify the application's
     * {@link android.content.ContentProvider}
     */
    public static final String CONTENT_AUTHORITY = "io.github.vickychijwani.gimmick.provider";

    private ObjectGraph mObjectGraph;

    @Override
    public void onCreate() {
        super.onCreate();
        buildObjectGraph();
        enableStrictMode();
    }

    private void buildObjectGraph() {
        Map<String, ResourceType> resourceTypes = GamrProvider.getResourceTypes(this);
        GiantBombAPIModule.setAPIKey(getString(R.string.giantbomb_api_key));
        GiantBombAPIModule.setResourceTypes(resourceTypes);
        MetacriticAPIModule.setAPIKey(getString(R.string.mashape_api_key));

        mObjectGraph = ObjectGraph.create(new GamrModule(this));
    }

    public void inject(Object o) {
        mObjectGraph.inject(o);
    }

    public <T> T get(Class<T> klazz) {
        return mObjectGraph.get(klazz);
    }

    public static GamrApplication getApp(Context context) {
        return (GamrApplication) context.getApplicationContext();
    }

    /**
     * Used to enable {@link android.os.StrictMode} during development
     */
    @SuppressLint("NewApi")
    public static void enableStrictMode() {
        if (!BuildConfig.DEBUG) {
            return;
        }
        // Enable StrictMode
        final StrictMode.ThreadPolicy.Builder threadPolicyBuilder = new StrictMode.ThreadPolicy.Builder();
        threadPolicyBuilder.detectAll();
        threadPolicyBuilder.penaltyLog();
        StrictMode.setThreadPolicy(threadPolicyBuilder.build());

        // Policy applied to all threads in the virtual machine's process
        final StrictMode.VmPolicy.Builder vmPolicyBuilder = new StrictMode.VmPolicy.Builder();
        vmPolicyBuilder.detectAll();
        vmPolicyBuilder.penaltyLog();
        if (DeviceUtils.isJellyBeanOrHigher()) {
            vmPolicyBuilder.detectLeakedRegistrationObjects();
        }
        StrictMode.setVmPolicy(vmPolicyBuilder.build());
    }

}
