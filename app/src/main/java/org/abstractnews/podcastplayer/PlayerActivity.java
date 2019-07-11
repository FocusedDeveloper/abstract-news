package org.abstractnews.podcastplayer;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

public class PlayerActivity extends AppCompatActivity {

    private Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        setContentView(R.layout.activity_player);

        toolbar = (Toolbar) findViewById(R.id.toolbar_player);
        toolbar.setTitle(R.string.abstract_news);
        setSupportActionBar(toolbar);

        if(savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.player_container,new PlayerFragment())
                    .commit();
        }


    }
}
