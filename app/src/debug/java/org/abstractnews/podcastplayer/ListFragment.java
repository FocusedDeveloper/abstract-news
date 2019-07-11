package org.abstractnews.podcastplayer;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.analytics.Tracker;
import com.google.firebase.analytics.FirebaseAnalytics;

import org.abstractnews.podcastplayer.sync.SyncAdapter;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link ListFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ListFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {
    // TODO: Rename parameter arguments,  names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private static final String[] COLUMN_NAMES = {PodcastContract.PodcastEntry._ID,
            PodcastContract.PodcastEntry.COLUMN_TITLE,
            PodcastContract.PodcastEntry.COLUMN_DATE,
            PodcastContract.PodcastEntry.COLUMN_DURATION,
            PodcastContract.PodcastEntry.COLUMN_DESCRIPTION,
            PodcastContract.PodcastEntry.COLUMN_URL_THUMBNAIL,
            PodcastContract.PodcastEntry.COLUMN_URL_STREAM};

    static final int ID = 0;
    static final int TITLE = 1;
    static final int DATE = 2;
    static final int DURATION = 3;
    static final int DESCRIPTION = 4;
    static final int THUMBNAIL = 5;
    static final int STREAM = 6;

    static final String[] itemProjection = {COLUMN_NAMES[ID], COLUMN_NAMES[TITLE], COLUMN_NAMES[DATE], COLUMN_NAMES[DURATION], COLUMN_NAMES[THUMBNAIL]};

    Context mContext;

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;
    ListView listView;
    View rootView;
    private PodcastAdapter podcastAdapter;
    private final int LOADER_ID_1 = 1;


    private OnFragmentInteractionListener mListener;

    AdView adView;
    AdRequest adRequest;
    AdRequest adTestRequest;

    Tracker mTracker;
    private FirebaseAnalytics mFirebaseAnalytics;
    private int id = 1;

    String name = "List Fragment";

    String TAG = "FRAGMENT";

    public ListFragment() {
        // Required empty public constructor
        Log.i("LIST FRAGMENT", "constructor");
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment ListFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static ListFragment newInstance(String param1, String param2) {
        ListFragment fragment = new ListFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        // update();

        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        Log.i("LIST FRAG", "on createView");
        rootView = inflater.inflate(R.layout.fragment_list, container, false);

        if (((MainActivity) getActivity()).getIsTwoPane()) {

        } else // if not in twoPane...
        {
            //
            View view =  rootView.findViewById(R.id.list_layout);
            setMargins(view, 112);

        }


        mContext = getContext();

        // Obtain the FirebaseAnalytics instance.
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(mContext);

        podcastAdapter = new PodcastAdapter(getActivity(), null, 0);

        // Inflate the layout for this fragment


        adView = (AdView) rootView.findViewById(R.id.adView);
        adRequest = new AdRequest.Builder().build();


        adTestRequest = new AdRequest.Builder().addTestDevice("A2261812009296D6F61BBA07B53D214A").build();

        adView.loadAd(adTestRequest);

        listView = (ListView) rootView.findViewById(R.id.recylerview);

        listView.setAdapter(podcastAdapter);

        Log.i("LIST FRAGMENT", "pre for loop");

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {


            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                Cursor cursor = (Cursor) parent.getItemAtPosition(position);

                boolean twoPanes;

                if (getActivity() instanceof MainActivity) {
                    twoPanes = ((MainActivity) getActivity()).getIsTwoPane();
                } else {
                    twoPanes = false;
                }

                if (cursor != null) {

                    // Single Pane Mode
                    // Use Intent to Launch Player Activity
                    if (!twoPanes) {
                        ImageView imageView = (ImageView) view.findViewById(R.id.thumbnail_image_view);
                        Intent intent = new Intent(getActivity(), PlayerActivity.class)
                                .setData(PodcastContract.PodcastEntry.buildPodcastUri(id));
                        ActivityOptionsCompat options = ActivityOptionsCompat.
                                makeSceneTransitionAnimation(getActivity(), (View)imageView , imageView.getTransitionName());
                        startActivity(intent, options.toBundle());
                        // Dual Pane Mode
                        // Open Fragment in this Activity
                    } else {
                        PlayerFragment fragment = new PlayerFragment();

                        Bundle bundle = new Bundle();
                        bundle.putString(mContext.getString(R.string.uri_key), PodcastContract.PodcastEntry.buildPodcastUri(id).toString());
                        fragment.setArguments(bundle);
                        ((MainActivity) getActivity()).getSupportFragmentManager().beginTransaction()
                                .replace(R.id.player_container, fragment).commit();
                    }
                }
            }
        });

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

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {

        getLoaderManager().initLoader(LOADER_ID_1, null, this);
        super.onActivityCreated(savedInstanceState);

    }

    @Override
    public void onStart() {
        super.onStart();

        // update();
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
        Log.i("LIST FRAGMENT", "create loader");

        return new CursorLoader(getActivity(), PodcastContract.PodcastEntry.CONTENT_URI,
                itemProjection, null, null, null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        podcastAdapter.swapCursor(data);

    }


    @Override
    public void onLoaderReset(Loader loader) {
        podcastAdapter.swapCursor(null);
    }


    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p/>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }

    public void update() {
        Log.i("JSon Testing", "Pre aysnc");
        /*Intent serviceIntent = new Intent(getActivity(),FetchPodcastService.AlarmReciever.class);

        PendingIntent pendingIntent = PendingIntent.getBroadcast(getActivity(),0,serviceIntent,PendingIntent.FLAG_ONE_SHOT);

        AlarmManager alarmManager = (AlarmManager) getActivity().getSystemService(Context.ALARM_SERVICE);

        //alarmManager.setRepeating(AlarmManager.ELAPSED_REALTIME,5000,5000,pendingIntent);
        alarmManager.setRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP,
                5000,
                5000, pendingIntent);*/
        SyncAdapter.syncImmediately(getActivity());

        Log.i("JSon Testing", "Post aysnc");
    }

    @Override
    public void onResume() {
        super.onResume();
        /*Log.i(TAG, "Setting screen name: " + name);
        mTracker.setScreenName("Image~" + name);
        mTracker.send(new HitBuilders.ScreenViewBuilder().build());*/

        Bundle bundle = new Bundle();
        bundle.putString(FirebaseAnalytics.Param.ITEM_ID, TAG);
        bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, name);
        bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "fragment");
        mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);
    }

    // for communicating in two pane mode
    public interface Callback {
        public void onItemSelected(Uri contentUri);
    }

}
