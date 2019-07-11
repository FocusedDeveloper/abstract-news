package org.abstractnews.podcastplayer.sync;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

/**
 * Created by Darnell on 7/22/2016.
 */
public class AuthenticatorService extends Service {
    // Instance field that stores the authenticator object
    private PodcastAuthenticator mAuthenticator;
    @Override
    public void onCreate() {
        // Create a new authenticator object
        mAuthenticator = new PodcastAuthenticator(this) {
        };
    }
    /*
     * When the system binds to this Service to make the RPC call
     * return the authenticator's IBinder.
     */
    @Override
    public IBinder onBind(Intent intent) {
        return mAuthenticator.getIBinder();

    }
}
