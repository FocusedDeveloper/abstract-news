package org.abstractnews.podcastplayer;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.res.Resources;
import android.database.Cursor;
import android.media.MediaPlayer;
import android.media.audiofx.Visualizer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.app.ShareCompat;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.widget.AppCompatSeekBar;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.analytics.FirebaseAnalytics;
import com.squareup.picasso.Picasso;

import java.util.concurrent.TimeUnit;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link PlayerFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link PlayerFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class PlayerFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor>{


    private static final java.lang.String SESSION_ID = "MyMediaPlayer";
    private static final int LOADER_ID_2 = 2;
    private Context context;
    Handler handler;

    MediaPlayer mediaPlayer;
    static AppCompatSeekBar seekBar;
    static ImageButton playButton;
    static TextView currentTimeTextView;
    static TextView endTimeTextView;

    TextView titleView;
    ImageView imageView;
    TextView descriptionView;

    static VisualizerView mVisualizerView;
    private Visualizer mVisualizer;

    View rootView;

    boolean dualPane = false;

    Intent playServiceIntent;

    MyService myService;
    boolean isServiceBound= false;
    ServiceConnection serviceConnection;

    static boolean isPlaying;

    BroadcastReceiver mBroadcastReceiver;

    boolean onlineAccess;

    private static double startTime = 0;
    private static double endTime = 0;

    public static final String BROADCAST_TO_PLAYER_SERVICE= "org.abstractnews.podcastplayer.MyService.communicate";

    private Podcast podcast;

    Uri podcastUri;

    CursorLoader podcastCursor;

    Intent sendSeekUpdateIntent;
    Intent sendButtonIntent;

    //Tracker mTracker;

    private FirebaseAnalytics mFirebaseAnalytics;

    String name = "Player Fragment";

    String TAG = "FRAG";

    private int oneTimeOnly = 0;
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_TRACK_URL = "trackURL";
    /*private static final String ARG_PARAM2 = "param2";*/

    // TODO: Rename and change types of parameters
    private String trackURL;
    private String mParam2;

    private static final String APIKEY = "d92f41852eb99ec25074c7d2cb1bf601";

    private OnFragmentInteractionListener mListener;
    private boolean broadcastRegistered;
    IntentFilter filter;


    public PlayerFragment() {
        // Required empty public constructor
    }


    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     *               //* @param param2 Parameter 2.
     * @return A new instance of fragment PlayerFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static PlayerFragment newInstance(String param1) {
        PlayerFragment fragment = new PlayerFragment();
        Bundle args = new Bundle();
        args.putString(ARG_TRACK_URL, param1);
        //args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    private boolean isNetworkAvailable() {
        //ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        //NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        //Boolean network = activeNetworkInfo != null && activeNetworkInfo.isConnected();
        return true;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getActivity().postponeEnterTransition();
        if (getArguments() != null) {
            trackURL = getArguments().getString(ARG_TRACK_URL);
            // mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        Log.i("PLAYER FRAG","onCreateView");

        rootView = inflater.inflate(R.layout.fragment_player, container, false);


        if (getActivity() instanceof MainActivity) {
            dualPane = ((MainActivity) getActivity()).getIsTwoPane();
        } else {
            dualPane = false;
            View view =  rootView.findViewById(R.id.player_frag_view);
            setMargins(view,56);
        }
       /* ViewGroup.LayoutParams lp = ((ViewGroup) rootView).getLayoutParams();
        if( lp instanceof ViewGroup.MarginLayoutParams)
        {
            if(getActivity() instanceof PlayerActivity)
            ((ViewGroup.MarginLayoutParams) lp).topMargin = R.attr.actionBarSize;
        }*/



        context = getContext();
        podcastUri = null;

       /* // Obtain the shared Tracker instance.
        AnalyticsApplication application = (AnalyticsApplication) getActivity().getApplication();
        mTracker = application.getDefaultTracker();*/

        // Obtain the FirebaseAnalytics instance.
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(context);


        filter = new IntentFilter();
        filter.addAction("prepped_key");
        filter.addAction("update_key");

        titleView = (TextView) rootView.findViewById(R.id.player_title_text_view);
        imageView = (ImageView) rootView.findViewById(R.id.large_image_view);
        descriptionView = (TextView) rootView.findViewById(R.id.description_text_view);
        descriptionView.setMovementMethod(new ScrollingMovementMethod());

        sendSeekUpdateIntent = new Intent(BROADCAST_TO_PLAYER_SERVICE);
        sendSeekUpdateIntent.setAction("seek_bar_sent");

        sendButtonIntent = new Intent(BROADCAST_TO_PLAYER_SERVICE);
        sendButtonIntent.setAction("play_pause");

        mVisualizerView = (VisualizerView) rootView.findViewById(R.id.myvisualizerview);

        handler = new Handler();

        Intent intent = getActivity().getIntent();
        podcast = new Podcast();

        Bundle bundle = this.getArguments();

        mediaPlayer = null;

        if (bundle != null) {
            Log.i("PLAYER FRAG","onCreateView.  Bundle != null");
            podcastUri = Uri.parse(bundle.getString(context.getString(R.string.uri_key)));
            //podcast = bundle.getParcelable(String.valueOf(R.string.uri_key));

        }else{
            // On saved state, we don't need to set isPlaying because it get's set according to prior condition
            isPlaying = false;
        }
        if (intent != null &&  podcastUri == null) {
            //podcast = intent.getParcelableExtra(Intent.EXTRA_TEXT);
            Log.i("PLAYER FRAGMENT", "getURI from intent");
            //if(intent.has)
            podcastUri = intent.getData();
           // Log.i("PLAYER FRAGMENT", podcastUri.toString());
        }




            playServiceIntent = new Intent(getActivity(), MyService.class);

        //mediaPlayer = MediaPlayer.create(this,R.raw.joe_budden_making_a_murderer);

        playButton = (ImageButton) rootView.findViewById(R.id.play_button);
        playButton.setEnabled(false);

        seekBar = (AppCompatSeekBar) rootView.findViewById(R.id.seek_bar);
        currentTimeTextView = (TextView) rootView.findViewById(R.id.current_time_textView);
        endTimeTextView = (TextView) rootView.findViewById(R.id.end_time_textView);



        playButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switchPlayPause();
            }
        });


        seekBar.setOnSeekBarChangeListener(new AppCompatSeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (  fromUser) {
                    //seekBarMoved(progress);
                    //mediaPlayer.seekTo(progress);
                    sendSeekUpdateIntent.putExtra("counter_key",progress);
                    getActivity().sendBroadcast(sendSeekUpdateIntent);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }


        });

        serviceConnection = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {

                //mBoundService = ((LocalService.LocalBinder)service).getService();
                isServiceBound = true;
                //myBinder = ((MyService.MyBinder) service).getService();
                myService = ((MyService.MyBinder) service).getService();
                connectVisualizer();
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {
                isServiceBound = false;
               myService = null;
            }
        };

        //seekBar.setEnabled(false);

        // Inflate the layout for this fragment
        return rootView;
    }

    private void setMargins (View view,  int top ) {
        if (view.getLayoutParams() instanceof ViewGroup.MarginLayoutParams) {
            Log.i("List Frag","instance of ViewGroup.MarginLayoutParams");

            ViewGroup.MarginLayoutParams p = (ViewGroup.MarginLayoutParams) view.getLayoutParams();
            Resources r = getActivity().getResources();
            int px = (int) TypedValue.applyDimension(
                    TypedValue.COMPLEX_UNIT_DIP,
                    top,
                    r.getDisplayMetrics()
            );
            p.setMargins(0, px, 0, 0);
            view.requestLayout();
        }
    }

    private void setupVisualizerFxAndUI() {

        // Create the Visualizer object and attach it to our media player.
        mVisualizer = new Visualizer(mediaPlayer.getAudioSessionId());
        mVisualizer.setCaptureSize(Visualizer.getCaptureSizeRange()[1]);
        mVisualizer.setDataCaptureListener(
                new Visualizer.OnDataCaptureListener() {
                    public void onWaveFormDataCapture(Visualizer visualizer,
                                                      byte[] bytes, int samplingRate) {
                        mVisualizerView.updateVisualizer(bytes);
                    }

                    public void onFftDataCapture(Visualizer visualizer,
                                                 byte[] bytes, int samplingRate) {
                    }
                }, Visualizer.getMaxCaptureRate() / 2, true, false);
    }

    private void scheduleStartPostponedTransition(final View sharedElement) {
        sharedElement.getViewTreeObserver().addOnPreDrawListener(
                new ViewTreeObserver.OnPreDrawListener() {
                    @Override
                    public boolean onPreDraw() {
                        sharedElement.getViewTreeObserver().removeOnPreDrawListener(this);
                        getActivity().startPostponedEnterTransition();
                        return true;
                    }
                });
    }

    private void bindViews(Cursor podcastCursor) {

        final String audioStream = podcastCursor.getString(ListFragment.STREAM) + "?client_id=" + APIKEY;
        String title = podcastCursor.getString(ListFragment.TITLE);

        // If screen rotation during song play, don't restart my service
        if(!isPlaying) {
            playServiceIntent = new Intent(getActivity(), MyService.class);
            playServiceIntent.putExtra("streamURL_Key", audioStream);
            playServiceIntent.putExtra("title_Key", title);
            Log.i("STREAM URL", podcastCursor.getString(ListFragment.STREAM) );
            getActivity().startService(playServiceIntent);
            getActivity().bindService(playServiceIntent,serviceConnection,Context.BIND_IMPORTANT);
        }

        /*if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Service service = (MyService) getActivity().getSystemService(MyService.class).;
        }*/
       // if(service.)

        titleView.setText(title);
        descriptionView.setText(podcastCursor.getString(ListFragment.DESCRIPTION));

        Picasso.with(context).load(podcastCursor.getString(ListFragment.THUMBNAIL)).into(imageView);
        imageView.setTransitionName(context.getString(R.string.imageTransitison)+podcastCursor.getInt(ListFragment.ID));
        scheduleStartPostponedTransition(imageView);

        endTime = podcastCursor.getInt(ListFragment.DURATION);
        setTime();
        seekBar.setMax((int) endTime);
        // playButton.setEnabled(true);
        // playButton.setImageResource(R.drawable.ic_pause_circle_outline_black_24dp);
        //isPlaying = true;

        rootView.findViewById(R.id.share_fab).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(Intent.createChooser(ShareCompat.IntentBuilder.from(getActivity())
                        .setType("text/plain")
                        .setText(context.getString(R.string.abstractURL))
                        .setSubject(context.getString(R.string.AbstractSendTitle))
                        .getIntent(), getString(R.string.action_share)));

                /*Intent intent = new Intent(Intent.ACTION_SEND).setType("audio*//*");
                intent.putExtra(Intent.EXTRA_STREAM, audioStream );
                startActivity(Intent.createChooser(intent, "Share to"));

                Intent i = new Intent(Intent.ACTION_VIEW);
                i.setDataAndType(Uri.parse(audioStream), "audio/*");
                context.startActivity(i);

                */
            }
        });

        doBindService();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putDouble("count",startTime);
        outState.putDouble("endTime",endTime);
        outState.putBoolean("isPlaying",isPlaying);

        if(podcastUri != null) {
            outState.putString("podcastURI", podcastUri.toString());
        }else {
            outState.putString("podcastURI", PodcastContract.PodcastEntry.buildPodcastUri(1).toString() );
        }


       // int sessionID = mediaPlayer.getAudioSessionId();
        //outState.putInt(SESSION_ID, sessionID);
    }


    @Override
    public void onViewStateRestored(@Nullable Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);
        if (savedInstanceState != null) {
            startTime = savedInstanceState.getDouble("count");
            endTime = savedInstanceState.getDouble("endTime");
            isPlaying = savedInstanceState.getBoolean("isPlaying");
            playButton.setImageResource(R.drawable.ic_pause_circle_outline_black_24dp);
            playButton.setEnabled(true);
            podcastUri = Uri.parse(savedInstanceState.getString("podcastURI"));
        }

    }

    public static void setTime() {
        currentTimeTextView.setText(String.format("%d min, %d sec",
                TimeUnit.MILLISECONDS.toMinutes((long) startTime),
                TimeUnit.MILLISECONDS.toSeconds((long) startTime) -
                        TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes((long) startTime))));

        endTimeTextView.setText(String.format("%d min, %d sec",
                TimeUnit.MILLISECONDS.toMinutes((long) endTime),
                TimeUnit.MILLISECONDS.toSeconds((long) endTime) -
                        TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes((long) endTime))));
    }

    void switchPlayPause() {
        Log.i("PLAYER FRAG", "switchPlayPause");

        if (isPlaying) {
            Log.i("PLAYER FRAG", "switchPlayPause: pause");
            pause();
            isPlaying = false;
        } else {
            Log.i("PLAYER FRAG", "switchPlayPause: onlineCheck");
            onlineAccess = isNetworkAvailable();

            if (onlineAccess) {
                Log.i("PLAYER FRAG", "switchPlayPause: play");
                play();
                isPlaying = true;
            } else {
                Toast.makeText(getActivity(), R.string.no_data, Toast.LENGTH_SHORT).show();
            }
        }
        Log.i("PLAYER FRAG", "switchPlayPause: exit");
    }

    public void play() {
        //MediaController mediaController = getActivity().getMediaController();
/*
        mediaPlayer.start();
        // endTime = mediaPlayer.getDuration();
        startTime = mediaPlayer.getCurrentPosition();

        if(oneTimeOnly == 0){
            seekBar.setMax((int) endTime);
            oneTimeOnly = 1;
        }
        setTime();
        seekBar.setProgress((int) startTime);

        handler.postDelayed(UpdateSongTime,100);*/
        //playButton.setEnabled(false);

        Log.i("PLAYER_FRAG","PLay Pressed");

        sendButtonIntent.putExtra("play","play_btn_string");
        getActivity().sendBroadcast(sendButtonIntent);

       // getActivity().startService(playServiceIntent);

        playButton.setImageResource(R.drawable.ic_pause_circle_outline_black_24dp);
        playButton.setContentDescription(context.getString(R.string.play_description));
        Log.i("PLAYER_FRAG","Set Image");

    }

    private Runnable UpdateSongTime = new Runnable() {
        @Override
        public void run() {
           // startTime = mediaPlayer.getCurrentPosition();
            setTime();
            seekBar.setProgress((int) startTime);
            handler.postDelayed(this, 100);
        }
    };

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        if (mBroadcastReceiver == null) {
            mBroadcastReceiver = new playerFragReciever();
        }

    }



    public void pause() {
       /* mediaPlayer.pause();
        if(oneTimeOnly ==0){
            seekBar.setMax((int) endTime);
            oneTimeOnly = 1;
        }*/
        // pauseButton.setEnabled(false);
        // playButton.setEnabled(true);
        sendButtonIntent.putExtra("play","pause");
        getActivity().sendBroadcast(sendButtonIntent);
        playButton.setImageResource(R.drawable.ic_play_circle_outline_black_24dp);
        playButton.setContentDescription(context.getString(R.string.pause_description));
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (broadcastRegistered) {
            getActivity().unregisterReceiver(mBroadcastReceiver);
            broadcastRegistered = false;
        }


    }


    @Override
    public void onResume() {
        super.onResume();

        getLoaderManager().initLoader(LOADER_ID_2, null, this);

        if (!broadcastRegistered) {
            getActivity().registerReceiver(mBroadcastReceiver, filter);
            broadcastRegistered = true;
        }
        /*Log.i(TAG, "Setting screen name: " + name);
        mTracker.setScreenName("Image~" + name);
        mTracker.send(new HitBuilders.ScreenViewBuilder().build());*/

        Bundle bundle = new Bundle();
        bundle.putString(FirebaseAnalytics.Param.ITEM_ID, TAG);
        bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, name);
        bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "fragment");
        mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);


    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;



    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        Log.i("PLAYER FRAG","onCReateLoader");
        if(podcastUri != null) {
            podcastCursor = new CursorLoader(getActivity(), podcastUri,
                    null, null, null, null);
        }else{
            Log.i("PLAYER FRAG","onCReateLoader.  Uri == null");
            podcastCursor = new CursorLoader(getActivity(), PodcastContract.PodcastEntry.buildPodcastUri(1),
                    null, null, null, null);

        }
        return podcastCursor;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if (!data.moveToFirst()) {
            return;
        }
        bindViews(data);

    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }

    void connectVisualizer(){
        if(myService != null){

            mediaPlayer = myService.getMediaPlayer();
            if(mediaPlayer!= null) {
                if(mVisualizer!=null) {
                    if (!mVisualizer.getEnabled()) {
                        mVisualizer.setEnabled(true);
                        Log.i("PlayerFrag","Visualizer Connected");
                    }
                }else{
                    if(!dualPane)  {
                        setupVisualizerFxAndUI();
                        mVisualizer.setEnabled(true);
                        Log.i("PlayerFrag", "Visualizer Initialized");
                    }
                }
            }

        }else{
            Log.i("PlayerFrag","Connect myService == null!");
        }
    }

    void disconnectVisualier(){
        if(myService != null){

            Log.i("PlayerFrag","Visualizer Connected");
            mVisualizer.setEnabled(false);
            mediaPlayer = null;

        }else{
            Log.i("PlayerFrag","Disconnect mediaPlayer = null!");
        }
    }

    void doBindService() {
        getActivity().bindService(new Intent(getActivity(),
                MyService.class), serviceConnection, Context.BIND_AUTO_CREATE);
        isServiceBound = true;
        Log.i("PlayerFragment","service bound");
    }

    void doUnbindService() {
        if (isServiceBound) {
            Log.i("PlayerFragment","service unbound");
            // Detach our existing connection.
            getActivity().unbindService(serviceConnection);
            isServiceBound = false;
            Log.i("PlayerFragment","service unbound");
            if(mVisualizer!= null) {
                if (mVisualizer.getEnabled()) {
                    disconnectVisualier();
                }
            }

        }

    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }

    private void prepped(){
        playButton.setEnabled(true);
        playButton.setImageResource(R.drawable.ic_play_circle_outline_black_24dp);
    }

    static public class playerFragReciever extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            Log.i("PLAYER_FRAG", "Broadcast Received");
            String isprep;
            String action;
            if(intent != null) {
                action = intent.getAction();
                Log.i("PLAYER FRAG","action: "+ action);
                if ( action.equals("prepped_key")) {
                /*isprep = intent.getStringExtra(context.getString(R.string.broadcast_extra_key));
                Log.i("PLAYER_FRAG", "value: " + isprep + " = " + context.getString(R.string.extra_prepared));*/
                    playButton.setEnabled(true);
                    playButton.setImageResource(R.drawable.ic_play_circle_outline_black_24dp);

                }
                if (action.equals("update_key")) {


                    Boolean ended = intent.getBooleanExtra("ended_key", false);
                    String counter = intent.getStringExtra("counter_key");
                    byte [] myBytes = intent.getByteArrayExtra("visual_key");
                    Integer countInt = Integer.parseInt(counter);
                    seekBar.setProgress(countInt);
                    startTime = countInt;
                    setTime();
                    if (ended) {
                        playButton.setImageResource(R.drawable.ic_play_circle_outline_black_24dp);
                        isPlaying = false;
                    }
                    mVisualizerView.updateVisualizer(myBytes);
                }
            }

            Log.i("PLAYER_FRAG", "Broadcast Received FIN");

        }
    }

    @Override
    public void onStop() {
        super.onStop();
        doUnbindService();
    }
}
