package org.abstractnews.podcastplayer;

import android.app.Service;
import android.appwidget.AppWidgetManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import java.io.IOException;

public class MyService extends Service implements MediaPlayer.OnCompletionListener, MediaPlayer.OnPreparedListener, MediaPlayer.OnErrorListener,
        MediaPlayer.OnSeekCompleteListener, MediaPlayer.OnInfoListener, MediaPlayer.OnBufferingUpdateListener {

    private MediaPlayer mediaPlayer = new MediaPlayer();
    private String streamURL;
    private IBinder iBinder = new MyBinder();

    private final Handler handler = new Handler();
    String seekPosition;
    int intSeekPosition;
    int mediaPosition;
    int mediaMax;
    private static boolean podcastEnded;
    public static final String BROADCAST_SEEK = "org.abstractnews.podcastplayer.PlayerFragment.seekBarUpdate";

    public static final String BROADCAST_FILTER = "org.abstractnews.podcastplayer.PlayerFragment.playerFragReciever";
    private Intent seekBarIntent;
    private Intent preparedIntent;
    private Intent widgetIntent;
    IntentFilter filter;

    private String title;

    BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.i("SYNC SERV", "create Broadcast Receiver");
            if (intent != null) {
                String action = null;

                Log.i("SYNC SERV", "Get Action");
                action = intent.getAction();
                if(action!= null) {
                    Log.i("SYNC SERV", "Action: "+action);
                    if (action.equals("seek_bar_sent")) {
                        int count = intent.getIntExtra("counter_key", 0);
                        mediaPlayer.seekTo(count);
                    }
                    if (action.equals("play_pause")) {
                        String buttonPressed = intent.getStringExtra("play");
                        Log.i("SYNC SERV", "extra: "+buttonPressed);
                        if (buttonPressed.equals("play_btn_string")) {
                            startPlaying();
                        } else {
                            pausePlaying();
                        }
                    }
                }
            }
        }
    };

    public MyService() {
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mediaPlayer.setOnCompletionListener(this);
        mediaPlayer.setOnPreparedListener(this);
        mediaPlayer.setOnErrorListener(this);
        mediaPlayer.setOnSeekCompleteListener(this);
        mediaPlayer.setOnInfoListener(this);
        mediaPlayer.setOnBufferingUpdateListener(this);
        mediaPlayer.reset();

        widgetIntent = new Intent(getApplicationContext(), PodcastWidget.class );

        widgetIntent.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
        //widgetIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, PodcastWidget.mAppWidgetIds);


        seekBarIntent = new Intent(BROADCAST_FILTER);
        seekBarIntent.setAction("update_key");
        seekBarIntent.putExtra(getString(R.string.seek_extra_key), "seek_bar_sent");

        preparedIntent = new Intent(BROADCAST_FILTER);
        preparedIntent.setAction("prepped_key");
        preparedIntent.putExtra("broadcast_extra_key", "extra_prepared");
        filter = new IntentFilter();
        filter.addAction("play_pause");
        filter.addAction("seek_bar_sent");

       // setupVisualizerFxAndUI();

    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        Log.i("PLAYER SERVICD", "onStart");
        if(intent != null) {
            if (intent.hasExtra("streamURL_Key")) {
                streamURL = intent.getExtras().getString("streamURL_Key");
                title = intent.getExtras().getString("title_Key");
            }
        }else {
            Log.i("PLAYER SERVICD", "onStart: intent = null.  streamURL = "+ streamURL);
        }
        mediaPlayer.reset();

        registerReceiver(broadcastReceiver, filter);

        if (!mediaPlayer.isPlaying()) {
            try {
                mediaPlayer.setDataSource(streamURL);
                mediaPlayer.prepareAsync();
            } catch (IOException e) {
                Log.i("PLAYER SERVICD", "onStart: Error init Media Player");
                e.printStackTrace();
            }
        }

        // run till i say stop please
        return Service.START_STICKY_COMPATIBILITY;
    }


    @Override
    public void onDestroy() {
        super.onDestroy();

        if (mediaPlayer != null) {
            if (mediaPlayer.isPlaying()) {
                mediaPlayer.stop();
            }

            mediaPlayer = null;
            handler.removeCallbacks(updateUI);
            widgetIntent.putExtra("isPrepped",false );
            sendBroadcast(widgetIntent);
        }

            unregisterReceiver(broadcastReceiver);

    }


    @Override
    public IBinder onBind(Intent intent) {

        return iBinder;
    }

    @Override
    public void onBufferingUpdate(MediaPlayer mp, int percent) {

    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        podcastEnded = true;
        stopPlaying();
        stopSelf();
        getPosition();
        widgetIntent.putExtra("isPlaying",mediaPlayer.isPlaying() );
        widgetIntent.putExtra("isPrepped",true );
        sendBroadcast(widgetIntent);
    }

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        switch(what) {
            case MediaPlayer.MEDIA_ERROR_NOT_VALID_FOR_PROGRESSIVE_PLAYBACK:
                Toast.makeText(this,
                        getString(R.string.media_error_not_valid) + extra,
                        Toast.LENGTH_SHORT).show();
                break;
            case MediaPlayer.MEDIA_ERROR_SERVER_DIED:
                Toast.makeText(this,
                        getString(R.string.media_server_died) + extra,
                        Toast.LENGTH_SHORT).show();
                break;
            case MediaPlayer.MEDIA_ERROR_UNKNOWN:
                Toast.makeText(this,
                        getString(R.string.unknown_media_error) + extra,
                        Toast.LENGTH_SHORT).show();
                break;
        }
        return false;
    }

    @Override
    public boolean onInfo(MediaPlayer mp, int what, int extra) {
        return false;
    }

    @Override
    public void onPrepared(MediaPlayer mp) {

        sendBroadcast(preparedIntent);
        Log.i("SYNC SERV", "prepared Broadcast sent");

        widgetIntent.putExtra("isPrepped",true );
        widgetIntent.putExtra("title",title);
        sendBroadcast(widgetIntent);

        //  startPlaying();
    }

    @Override
    public void onSeekComplete(MediaPlayer mp) {

    }

    public class MyBinder extends Binder {
        MyService getService() {
            return MyService.this;
        }
    }

    public android.media.MediaPlayer getMediaPlayer() {
        return mediaPlayer;
    }

    public void stopPlaying() {
        if (mediaPlayer.isPlaying()) {
            mediaPlayer.stop();
            widgetIntent.putExtra("isPlaying",mediaPlayer.isPlaying() );
            widgetIntent.putExtra("isPrepped",true );
            sendBroadcast(widgetIntent);
        }
    }

    public void pausePlaying() {
        Log.i("SYNC SERV", "pause pressed");
        if (mediaPlayer.isPlaying()) {
            mediaPlayer.pause();
            widgetIntent.putExtra("isPlaying",mediaPlayer.isPlaying() );
            widgetIntent.putExtra("isPrepped",true );
            sendBroadcast(widgetIntent);
        }
    }

    public void startPlaying() {
        Log.i("SYNC SERV", "play pressed");
        if (!mediaPlayer.isPlaying()) {
            mediaPlayer.start();
            podcastEnded = false;
            setupHandler();
            widgetIntent.putExtra("isPlaying",mediaPlayer.isPlaying() );
            widgetIntent.putExtra("isPrepped",true );
            widgetIntent.putExtra("title",title);
            sendBroadcast(widgetIntent);
        }
    }

    private void setupHandler() {
        handler.removeCallbacks(updateUI);
        handler.postDelayed(updateUI, 1000);
    }

    private Runnable updateUI = new Runnable() {
        @Override
        public void run() {
            getPosition();
            handler.postDelayed(this, 1000);
        }
    };

    private void getPosition() {
        if (mediaPlayer.isPlaying()) {
            mediaPosition = mediaPlayer.getCurrentPosition();

            mediaMax = mediaPlayer.getDuration();
            seekBarIntent.putExtra("counter_key", String.valueOf(mediaPosition));
            seekBarIntent.putExtra("ended_key", podcastEnded);
           // seekBarIntent.putExtra("visual_key",myBytes);
            sendBroadcast(seekBarIntent);
        }
    }

}
