package org.abstractnews.podcastplayer.sync;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

public class PodcastSyncService extends Service {
    public PodcastSyncService() {
    }

    private static final Object sSyncAdapterLock = new Object();
    private static SyncAdapter sSyncAdapter = null;

    @Override
    public void onCreate() {
        synchronized (sSyncAdapterLock) {
            if (sSyncAdapter == null)
                sSyncAdapter = new SyncAdapter(getApplicationContext(), true);
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        return sSyncAdapter.getSyncAdapterBinder();
    }
}
