package io.github.vickychijwani.gimmick.sync;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import javax.inject.Inject;

import dagger.Lazy;
import io.github.vickychijwani.gimmick.GamrApplication;

/**
 * Define a Service that returns an IBinder for the sync adapter class, allowing the sync adapter
 * framework to call onPerformSync().
 */
public class SyncService extends Service {

    @Inject Lazy<SyncAdapter> mSyncAdapter;

    @Override
    public void onCreate() {
        super.onCreate();

        // inject this service into the Dagger object graph
        GamrApplication.getApp(this).inject(this);
    }

    @Override
    public IBinder onBind(Intent intent) {
        // Get the object that allows external processes to call onPerformSync(). The object is
        // created in the base class code when the SyncAdapter constructors call super()
        return mSyncAdapter.get().getSyncAdapterBinder();
    }

}
