package org.abstractnews.podcastplayer;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;
import android.widget.RemoteViews;

/**
 * Implementation of App Widget functionality.
 */
public class PodcastWidget extends AppWidgetProvider {

    BroadcastReceiver mBroadcastReceiver;
    private boolean broadcastRegistered;
    IntentFilter filter;


    static boolean contentLoaded = false;

    static boolean isPlaying = false;

    static String title = "Podcast Title";


    Intent inboundIntent;


    static void updateAppWidget(Context context, AppWidgetManager appWidgetManager,
                                int appWidgetId) {

        Log.i("WIDGET","updateAppWidget");

        //isPlaying = false;

        //sendButtonIntent = new Intent(BROADCAST_TO_PLAYER_SERVICE);
        //sendButtonIntent.setAction("play_pause");

       // CharSequence widgetText = context.getString(R.string.appwidget_text);
        // Construct the RemoteViews object
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.podcast_widget);

        Intent launcherIntent = new Intent(context, MainActivity.class);

        //launcherIntent.setAction(AppWidgetManager.);
        //launcherIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, appWidgetId);

        PendingIntent pendingLaunchIntent = PendingIntent.getActivity(context,
                0, launcherIntent, PendingIntent.FLAG_ONE_SHOT);

        Intent updateIntent = new Intent();
        updateIntent.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
        updateIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, appWidgetId);

        PendingIntent pendingIntent = PendingIntent.getBroadcast(context,
                0, updateIntent, PendingIntent.FLAG_UPDATE_CURRENT);


        views.setTextViewText(R.id.appwidget_appTitle, "Abstract News");

        views.setTextViewText(R.id.appwidget_podcastTitle, title);

        setPlay(views, context);



        /*if(isPlaying){

            //context.sendBroadcast(sendButtonIntent);
        }else {
            Log.i("SYNC SERV", "play it");
            sendButtonIntent.putExtra("play","pause");
            //context.sendBroadcast(sendButtonIntent);
        }


        if(contentLoaded){
            views.setOnClickPendingIntent(R.id.widget_playbutton,playeryControlIntent);
        }else {
            views.setOnClickPendingIntent(R.id.widget_playbutton,pendingLaunchIntent);
        }*/

        /*if(isPlaying){
            views.setImageViewResource(R.id.widget_playbutton, R.drawable.ic_pause_circle_outline_black_24dp);
        }else{
            views.setImageViewResource(R.id.widget_playbutton, R.drawable.ic_play_circle_outline_black_24dp);
        }*/

        //PendingIntent pending = PendingIntent.getActivity(context, 0, intent, 0);
        views.setOnClickPendingIntent(R.id.widget_titles, pendingLaunchIntent);


        // Instruct the widget manager to update the widget
        appWidgetManager.updateAppWidget(appWidgetId, views);
    }

    private static void setPlay(RemoteViews views, Context context) {
        Intent sendButtonIntent = new Intent(PlayerFragment.BROADCAST_TO_PLAYER_SERVICE);
        sendButtonIntent.setAction("play_pause");

        if(isPlaying){
            views.setImageViewResource(R.id.widget_playbutton,R.drawable.ic_pause_circle_outline_black_24dp);

                Log.i("SYNC SERV x", "pause it");
                sendButtonIntent.putExtra("play","pause");
            Log.i("SYNC SERV y", "extra: "+sendButtonIntent.getStringExtra("play"));
        }else{
            views.setImageViewResource(R.id.widget_playbutton,R.drawable.ic_play_circle_outline_black_24dp);
                Log.i("SYNC SERV x", "play it");
                sendButtonIntent.putExtra("play","play_btn_string");
                Log.i("SYNC SERV y", "extra: "+sendButtonIntent.getStringExtra("play"));
        }
        if(contentLoaded){
            Log.i("SYNC SERV x", "content loaded");
            PendingIntent playeryControlIntent = PendingIntent.getBroadcast(context,0,sendButtonIntent,PendingIntent.FLAG_UPDATE_CURRENT);
            views.setOnClickPendingIntent(R.id.widget_playbutton,playeryControlIntent);
        }
        else{
            Log.i("SYNC SERV x", "content NOT loaded");
        }

        //views.setOnClickPendingIntent(R.id.widget_playbutton,);
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {

        Log.i("WIDGET","onUpdate");
        int i = 0;



        // There may be multiple widgets active, so update all of them
        for (int appWidgetId : appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId);
        }
    }

    @Override
    public void onReceive(Context context, Intent intent) {

        if(intent!= null) {


            String tempTitle = null;

            Log.i("WIDGET","onReceive");
            //String action = intent.getAction();

            isPlaying = intent.getBooleanExtra("isPlaying",false);

            contentLoaded = intent.getBooleanExtra("isPrepped", false);

            tempTitle = intent.getStringExtra("title");
            if(tempTitle != null){
                title = tempTitle;
            }



            onUpdate(context,AppWidgetManager.getInstance(context),AppWidgetManager.getInstance(context).getAppWidgetIds(new ComponentName(context, this.getClass())) );

            /*if (action == "prepped_key") {
                isprep = intent.getStringExtra(context.getString(R.string.broadcast_extra_key));
                Log.i("PLAYER_FRAG", "value: " + isprep + " = " + context.getString(R.string.extra_prepared));

//            playButton.setEnabled(true);
//            playButton.setImageResource(R.drawable.ic_play_circle_outline_black_24dp);
            }
            if (action == "update_key") {
                Boolean ended = intent.getBooleanExtra("ended_key", false);
                //String counter = intent.getStringExtra("counter_key");
                //Integer countInt = Integer.parseInt(counter);
            seekBar.setProgress(countInt);
            startTime = countInt;                isPlaying = true;

//            setTime();
                if (ended) {
//                playButton.setImageResource(R.drawable.ic_play_circle_outline_black_24dp);
                    isPlaying = false;
                }
            }*/

        }
        super.onReceive(context, intent);
    }

   /* @Override
    public void onEnabled(Context context) {

        sendButtonIntent = new Intent();
        // Enter relevant functionality for when the first widget is created
        if (!broadcastRegistered) {
            filter = new IntentFilter();
            filter.addAction("prepped_key");
            filter.addAction("update_key");
            context.registerReceiver(mBroadcastReceiver, filter);
            broadcastRegistered = true;
        }

    }*/

    /*@Override
    public void onDisabled(Context context) {
        // Enter relevant functionality for when the last widget is disabled

        if (broadcastRegistered) {
            context.unregisterReceiver(mBroadcastReceiver);
            broadcastRegistered = false;
        }
    }*/
}

