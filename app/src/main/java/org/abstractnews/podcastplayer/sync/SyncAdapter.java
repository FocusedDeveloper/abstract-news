package org.abstractnews.podcastplayer.sync;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.SyncRequest;
import android.content.SyncResult;
import android.database.Cursor;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;

import org.abstractnews.podcastplayer.Podcast;
import org.abstractnews.podcastplayer.PodcastContract;
import org.abstractnews.podcastplayer.R;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

/**
 * Created by Darnell on 7/22/2016.
 */
public class SyncAdapter extends AbstractThreadedSyncAdapter {

    private static final String LOG_TAG = SyncAdapter.class.getSimpleName();
    private static final String APIKEY = "d92f41852eb99ec25074c7d2cb1bf601";
    private static boolean newAccountCreated;

    private ArrayList<Podcast> myPodcastList = new ArrayList<>();

    public static final int SYNC_INTERVAL = 360;
    public static final int SYNC_FLEXTIME = SYNC_INTERVAL/3;

    public SyncAdapter(Context context, boolean autoInitialize) {
                super(context, autoInitialize);
            }

    private void getPodcastDataFromJson(String podcastJsonStr)
            throws JSONException {


        // These are the names of the JSON objects that need to be extracted.


        // JSONObject podcastJson = new JSONObject(podcastJsonStr);
        JSONArray podcastArray = new JSONArray(podcastJsonStr);

        ArrayList<ContentValues> contentValues = new ArrayList<>();

        for (int i = 0; i < podcastArray.length(); i++) {
            Podcast podcast = new Podcast();

            JSONObject jRealObject = podcastArray.getJSONObject(i);

            podcast.setDate(jRealObject.getString("created_at"));
            podcast.setDuration(jRealObject.getLong("duration"));
            podcast.setTitle(jRealObject.getString("title"));
            podcast.setDescription(jRealObject.getString("description"));
            podcast.setUrlThumbnail(jRealObject.getString("artwork_url"));
            podcast.setUrlStream(jRealObject.getString("stream_url"));

            //  count_em++;
            //  Log.i("My COUNTER", "iterate i " + i);
            myPodcastList.add(podcast);

            ContentValues values = new ContentValues();

            values.put(PodcastContract.PodcastEntry.COLUMN_DATE,podcast.getDate());
            values.put(PodcastContract.PodcastEntry.COLUMN_DURATION,podcast.getDuration());
            values.put(PodcastContract.PodcastEntry.COLUMN_TITLE, podcast.getTitle());
            values.put(PodcastContract.PodcastEntry.COLUMN_DESCRIPTION,podcast.getDescription());
            values.put(PodcastContract.PodcastEntry.COLUMN_URL_THUMBNAIL,podcast.getUrlThumbnail());
            values.put(PodcastContract.PodcastEntry.COLUMN_URL_STREAM,podcast.getUrlStream());

            contentValues.add(values);




          /*  Log.i("JSon Testing", "My podcast title is: " + podcast.getTitle());
            Log.i("JSon Testing", "My podcast date is: " + podcast.getDate());
            Log.i("JSon Testing", "My podcast duration is: " + podcast.getDuration());
            Log.i("JSon Testing", "My podcast URL is: " + podcast.getUrlThumbnail());
            Log.i("JSon Testing", "My podcast desc is: " + podcast.getDescription());*/


        }

        ContentValues[] valuesArray = new ContentValues[contentValues.size()];
        contentValues.toArray(valuesArray);

        // If there was a new account created
        // Add all values to database
        if(newAccountCreated) {
            getContext().getContentResolver().bulkInsert(PodcastContract.PodcastEntry.CONTENT_URI, valuesArray);
        }else{
            Cursor cursor;
            // Else, For each podcast
            for (ContentValues value: valuesArray
                 ) {
                // check if it is already in the DB
                cursor = getContext().getContentResolver().query(PodcastContract.PodcastEntry.CONTENT_URI,
                        new String[] {PodcastContract.PodcastEntry._ID},
                        PodcastContract.PodcastEntry.COLUMN_TITLE + " =? and " + PodcastContract.PodcastEntry.COLUMN_DATE + " =?",
                        new String[] {(String) value.get(PodcastContract.PodcastEntry.COLUMN_TITLE), (String)value.get(PodcastContract.PodcastEntry.COLUMN_DATE)} ,
                        null);
                // IF not, Add to DB
                if(!cursor.moveToFirst() ){
                    getContext().getContentResolver().insert(PodcastContract.PodcastEntry.CONTENT_URI,value);
                }
            }
        }
    }


                @Override
       public void onPerformSync(Account account, Bundle extras, String authority, ContentProviderClient provider, SyncResult syncResult) {
               Log.i(LOG_TAG, "onPerformSync Called.");
                    HttpURLConnection urlConnection = null;
                    BufferedReader reader = null;

                    String podcastJsonStr = null;


                    try {

                        final String PODCAST_BASE_URL =
                                "https://api.soundcloud.com/users/";
                        final String USER = "bbcacademy/";  //"mbrella-entertainment/";

                        final String API_KEY = "tracks?client_id=" + APIKEY;


                        String tmp = PODCAST_BASE_URL + USER + API_KEY;

                        URL url = new URL(tmp);
                        Log.i(LOG_TAG, "Built URI " + url);

                        // Create the request to OpenWeatherMap, and open the connection
                        urlConnection = (HttpURLConnection) url.openConnection();
                        urlConnection.setRequestMethod("GET");
                        urlConnection.connect();

                        // Read the input stream into a String
                        InputStream inputStream = urlConnection.getInputStream();
                        StringBuffer buffer = new StringBuffer();
                        if (inputStream == null) {
                            // Nothing to do.
                            return ;
                        }
                        reader = new BufferedReader(new InputStreamReader(inputStream));

                        String line;
                        while ((line = reader.readLine()) != null) {

                            buffer.append(line + "\n");
                        }

                        if (buffer.length() == 0) {
                            return ;
                        }
                        podcastJsonStr = buffer.toString();

                        Log.v(LOG_TAG, "Podcast string: " + podcastJsonStr);
                    } catch (IOException e) {
                        Log.e(LOG_TAG, "Error ", e);
                        return ;
                    } finally {
                        if (urlConnection != null) {
                            urlConnection.disconnect();
                        }
                        if (reader != null) {
                            try {
                                reader.close();
                            } catch (final IOException e) {
                                Log.e(LOG_TAG, "Error closing stream", e);
                            }
                        }
                    }

                    try {
                        getPodcastDataFromJson(podcastJsonStr);
                    } catch (JSONException e) {
                        Log.e(LOG_TAG, e.getMessage(), e);
                        e.printStackTrace();
                    }
                    return;
                }



                /**
     +     * Helper method to have the sync adapter sync immediately
     +     * @param context The context used to access the account service
     +     */
                public static void syncImmediately(Context context) {
                Bundle bundle = new Bundle();
                bundle.putBoolean(ContentResolver.SYNC_EXTRAS_EXPEDITED, true);
               bundle.putBoolean(ContentResolver.SYNC_EXTRAS_MANUAL, true);
                ContentResolver.requestSync(getSyncAccount(context),
                                context.getString(R.string.content_authority), bundle);
            }

                /**
     +     * Helper method to get the fake account to be used with SyncAdapter, or make a new one
     +     * if the fake account doesn't exist yet.  If we make a new account, we call the
     +     * onAccountCreated method so we can initialize things.
     +     *
     +     * @param context The context used to access the account service
     +     * @return a fake account.
     +     */
                public static Account getSyncAccount(Context context) {

                    newAccountCreated = false;
                    // Get an instance of the Android account manager
                    AccountManager accountManager =
                            (AccountManager) context.getSystemService(Context.ACCOUNT_SERVICE);



                    // Create the account type and default account
                    Account newAccount = new Account(
                            context.getString(R.string.app_name), context.getString(R.string.sync_account_type));

                    // If the password doesn't exist, the account doesn't exist
                    if ( null == accountManager.getPassword(newAccount) ) {

        /*
         * Add the account and account type, no password or user data
         * If successful, return the Account object, otherwise report an error.
         */
                        if (!accountManager.addAccountExplicitly(newAccount, "", null)) {
                            return null;
                        }
            /*
             * If you don't set android:syncable="true" in
             * in your <provider> element in the manifest,
             * then call ContentResolver.setIsSyncable(account, AUTHORITY, 1)
             * here.
             */

                        onAccountCreated(newAccount, context);
                    }
                    return newAccount;
                }

    /**
     * Helper method to schedule the sync adapter periodic execution
     */
    public static void configurePeriodicSync(Context context, int syncInterval, int flexTime) {
        Account account = getSyncAccount(context);
        String authority = context.getString(R.string.content_authority);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            // we can enable inexact timers in our periodic sync
            SyncRequest request = new SyncRequest.Builder().
                    syncPeriodic(syncInterval, flexTime).
                    setSyncAdapter(account, authority).
                    setExtras(new Bundle()).build();
            ContentResolver.requestSync(request);
        } else {
            ContentResolver.addPeriodicSync(account,
                    authority, new Bundle(), syncInterval);
        }
    }


    private static void onAccountCreated(Account newAccount, Context context) {

        newAccountCreated = true;

        /*
         * Since we've created an account
         */
        SyncAdapter.configurePeriodicSync(context, SYNC_INTERVAL, SYNC_FLEXTIME);

        /*
         * Without calling setSyncAutomatically, our periodic sync will not be enabled.
         */
        ContentResolver.setSyncAutomatically(newAccount, context.getString(R.string.content_authority), true);

        /*
         * Finally, let's do a sync to get things started
         */
        syncImmediately(context);
    }

    public static void initializeSyncAdapter(Context context) {
        getSyncAccount(context);
    }

}
