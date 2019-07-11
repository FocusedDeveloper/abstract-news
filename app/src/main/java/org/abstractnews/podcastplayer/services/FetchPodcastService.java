package org.abstractnews.podcastplayer.services;

import android.app.IntentService;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import org.abstractnews.podcastplayer.Podcast;
import org.abstractnews.podcastplayer.PodcastAsyncTask;
import org.abstractnews.podcastplayer.PodcastContract;
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
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p>
 * TODO: Customize class - update intent actions, extra parameters and static
 * helper methods.
 */
public class FetchPodcastService extends IntentService {
    // TODO: Rename actions, choose action names that describe tasks that this
    // IntentService can perform, e.g. ACTION_FETCH_NEW_ITEMS
    private static final String ACTION_FOO = "org.abstractnews.podcastplayer.services.action.FOO";
    private static final String ACTION_BAZ = "org.abstractnews.podcastplayer.services.action.BAZ";

    // TODO: Rename parameters
    private static final String EXTRA_PARAM1 = "org.abstractnews.podcastplayer.services.extra.PARAM1";
    private static final String EXTRA_PARAM2 = "org.abstractnews.podcastplayer.services.extra.PARAM2";
    private final String LOG_TAG = PodcastAsyncTask.class.getSimpleName();
    private static final String APIKEY = "d92f41852eb99ec25074c7d2cb1bf601";

    private ArrayList<Podcast> myPodcastList = new ArrayList<>();


    public FetchPodcastService() {
        super("FetchPodcastService");
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




            Log.i("JSon Testing", "My podcast title is: " + podcast.getTitle());
            Log.i("JSon Testing", "My podcast date is: " + podcast.getDate());
            Log.i("JSon Testing", "My podcast duration is: " + podcast.getDuration());
            Log.i("JSon Testing", "My podcast URL is: " + podcast.getUrlThumbnail());
            Log.i("JSon Testing", "My podcast desc is: " + podcast.getDescription());


        }

        ContentValues[] valuesArray = new ContentValues[contentValues.size()];
        contentValues.toArray(valuesArray);

        getApplicationContext().getContentResolver().bulkInsert(PodcastContract.PodcastEntry.CONTENT_URI, valuesArray);
    }

    /**
     * Starts this service to perform action Foo with the given parameters. If
     * the service is already performing a task this action will be queued.
     *
     * @see IntentService
     */
    // TOO: Customize helper method
    public static void startActionFoo(Context context, String param1, String param2) {
        Intent intent = new Intent(context, FetchPodcastService.class);
        intent.setAction(ACTION_FOO);
        intent.putExtra(EXTRA_PARAM1, param1);
        intent.putExtra(EXTRA_PARAM2, param2);
        context.startService(intent);
    }

    /**
     * Starts this service to perform action Baz with the given parameters. If
     * the service is already performing a task this action will be queued.
     *
     * @see IntentService
     */
    // TODO: Customize helper method
    public static void startActionBaz(Context context, String param1, String param2) {
        Intent intent = new Intent(context, FetchPodcastService.class);
        intent.setAction(ACTION_BAZ);
        intent.putExtra(EXTRA_PARAM1, param1);
        intent.putExtra(EXTRA_PARAM2, param2);
        context.startService(intent);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            final String action = intent.getAction();
            if (ACTION_FOO.equals(action)) {
                final String param1 = intent.getStringExtra(EXTRA_PARAM1);
                final String param2 = intent.getStringExtra(EXTRA_PARAM2);
                handleActionFoo(param1, param2);
            } else if (ACTION_BAZ.equals(action)) {
                final String param1 = intent.getStringExtra(EXTRA_PARAM1);
                final String param2 = intent.getStringExtra(EXTRA_PARAM2);
                handleActionBaz(param1, param2);
            }
        }

        Log.i("FETCHSERVICE", "Service started");
       // Toast.makeText(FetchPodcastService.this, "Service Started!", Toast.LENGTH_SHORT).show();
        HttpURLConnection urlConnection = null;
        BufferedReader reader = null;

        String podcastJsonStr = null;


        try {

            final String PODCAST_BASE_URL =
                    "https://api.soundcloud.com/users/";
            final String USER = "mbrella-entertainment/";
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
     * Handle action Foo in the provided background thread with the provided
     * parameters.
     */
    private void handleActionFoo(String param1, String param2) {
        // TODO: Handle action Foo
        throw new UnsupportedOperationException("Not yet implemented");
    }

    /**
     * Handle action Baz in the provided background thread with the provided
     * parameters.
     */
    private void handleActionBaz(String param1, String param2) {
        // TODO: Handle action Baz
        throw new UnsupportedOperationException("Not yet implemented");
    }
    static public class AlarmReciever extends BroadcastReceiver{

        @Override
        public void onReceive(Context context, Intent intent) {
            Log.i("ALARM", "alarm recieved");
            Intent startServiceIntent = new Intent(context,FetchPodcastService.class);
            context.startService(startServiceIntent);
        }
    }
}
