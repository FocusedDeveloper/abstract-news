package org.abstractnews.podcastplayer;

import android.app.Activity;
import android.content.ContentValues;
import android.os.AsyncTask;
import android.util.Log;

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
 * Created by Darnell on 7/19/2016.
 */
public class PodcastAsyncTask extends AsyncTask<String, Void, String[]> {

    private final String LOG_TAG = PodcastAsyncTask.class.getSimpleName();
    private static final String APIKEY = "d92f41852eb99ec25074c7d2cb1bf601";

    private PodcastAdapter podcastAdapter;

    private Activity activity;
    private ArrayList<Podcast> myPodcastList = new ArrayList<>();

    public PodcastAsyncTask(Activity activity, PodcastAdapter podcastAdapter){
        this.activity = activity;
        this.podcastAdapter = podcastAdapter;
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

        activity.getContentResolver().bulkInsert(PodcastContract.PodcastEntry.CONTENT_URI, valuesArray);
    }

    @Override
    protected String[] doInBackground(String... params) {

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
                return null;
            }
            reader = new BufferedReader(new InputStreamReader(inputStream));

            String line;
            while ((line = reader.readLine()) != null) {

                buffer.append(line + "\n");
            }

            if (buffer.length() == 0) {
                return null;
            }
            podcastJsonStr = buffer.toString();

            Log.v(LOG_TAG, "Podcast string: " + podcastJsonStr);
        } catch (IOException e) {
            Log.e(LOG_TAG, "Error ", e);
            return null;
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
        return null;
    }

    @Override
    protected void onPostExecute(String[] strings) {
        super.onPostExecute(strings);
        /*Log.i("JSON TESTING", "on post execute");


        if(activity != null && myPodcastList != null && podcastAdapter != null) {

            *//*podcastAdapter.podcastArray.clear();
            podcastAdapter.podcastArray.addAll(myPodcastList);*//*
            podcastAdapter.notifyDataSetChanged();
            Log.i("JSON TESTING", "after adapter");
        }*/
    }
}