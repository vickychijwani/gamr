package io.github.vickychijwani.gimmick.sync;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

/**
 * Define a Service that returns an IBinder for the sync adapter class, allowing the sync adapter
 * framework to call onPerformSync().
 */
public class SyncService extends Service {

    // singletons
    private static SyncAdapter sSyncAdapter = null;
    private static final Object sSyncAdapterLock = new Object();

    @Override
    public void onCreate() {
        synchronized (sSyncAdapterLock) {
            if (sSyncAdapter == null) {
                sSyncAdapter = new SyncAdapter(getApplicationContext(), true);
            }
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        // Get the object that allows external processes to call onPerformSync(). The object is
        // created in the base class code when the SyncAdapter constructors call super()
        return sSyncAdapter.getSyncAdapterBinder();
    }

}
