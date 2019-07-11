package org.abstractnews.podcastplayer;

import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;

import org.abstractnews.podcastplayer.sync.SyncAdapter;

public class MainActivity extends AppCompatActivity implements ListFragment.Callback{



    private android.support.v4.app.FragmentManager fragmentManager;
    private boolean mTwoPane;

    private Toolbar mToolbar;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private CollapsingToolbarLayout collapsingToolbarLayout;

    public boolean getIsTwoPane() {return mTwoPane; }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mToolbar = (Toolbar) findViewById(R.id.toolbar);

        setSupportActionBar(mToolbar);
        /*collapsingToolbarLayout = (CollapsingToolbarLayout) findViewById(R.id.collapsing_toolbar_layout);
        collapsingToolbarLayout.setTitle(getString(R.string.app_name));

        mSwipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipe_refresh_layout);*/


        // if the player activity is present, two pane mode!
        if(findViewById(R.id.player_container) !=null){
            Log.i("MAIN ACTIVITY","Two Pane Mode");
            mTwoPane = true;
            if( savedInstanceState == null){
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.player_container, new PlayerFragment()).commit();
            }
        }else {
            Log.i("MAIN ACTIVITY","Single Pane Mode");
            mTwoPane = false;
        }



        Log.i("MAIN ACTIVITY", "pre frag");

        Fragment fragment = new ListFragment();


        fragmentManager = getSupportFragmentManager();
        android.support.v4.app.FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

        if (findViewById(R.id.main_layout) != null) {
            fragmentTransaction.replace(R.id.main_layout, fragment, "ListFrag");
        }


        fragmentTransaction.commit();
        Log.i("MAIN ACTIVITY", "post frag");


        SyncAdapter.initializeSyncAdapter(this);
    }


    @Override
    public void onItemSelected(Uri contentUri) {
      /*  if(mTwoPane){
            Bundle args = new Bundle();

            PlayerFragment fragment = new PlayerFragment();
            fragment.setArguments(args);

            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.player_container,fragment, "Player_Fragment")
                    .commit();
        } else {
            Intent intent = new Intent(this, PlayerActivity.class)
                    .setData(contentUri);
            startActivity(intent);
        }*/
    }
}
