package org.abstractnews.podcastplayer;

import android.content.Context;
import android.database.Cursor;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.concurrent.TimeUnit;

/**
 * Created by Darnell on 7/14/2016.
 */
public class PodcastAdapter extends CursorAdapter {

    private LayoutInflater inflater;
    //List<Podcast> podcastArray = new ArrayList<>();
    Context context;
    Cursor cursor;

    private static final String [] COLUMN_NAMES = {PodcastContract.PodcastEntry._ID, PodcastContract.PodcastEntry.COLUMN_TITLE, PodcastContract.PodcastEntry.COLUMN_DATE, PodcastContract.PodcastEntry.COLUMN_DURATION,
             PodcastContract.PodcastEntry.COLUMN_URL_THUMBNAIL};


    private static final int ID = 0;
    private static final int TITLE = 1;
    private static final int DATE = 2;
    private static final int DURATION = 3;
    private static final int THUMBNAIL = 4;

    String [] listItemProjection = {COLUMN_NAMES[TITLE], COLUMN_NAMES[DATE], COLUMN_NAMES[DURATION], COLUMN_NAMES[THUMBNAIL]};

    public PodcastAdapter(Context context, Cursor cursor, int flags){
        super(context, cursor, flags);
        this.cursor = cursor;

        Log.i("PODCAST ADAPTER","constructor");
        // create an inflater from the context passed to the Adapter
        inflater = LayoutInflater.from(context);
        this.context = context;
      //  this.podcastArray = podcastArray;

       /* for(int i = 0; i < podcastArray.size(); i++){
            Log.i("PODCAST ADAPTER","podcast title["+i+"]: "+podcastArray.get(i).title);
        }*/

    }

    /*@Override
    public PodcastViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Log.i("PODCAST ADAPTER","onCreateViewHolder PRE");
        // inflate a view for the viewholder
        View view  = inflater.inflate(R.layout.list_item_layout,parent,false);

        Log.i("PODCAST ADAPTER","onCreateViewHolder POST");
        //
        PodcastViewHolder vh = new PodcastViewHolder(view);
        return vh;
    }*/

  /*  public void onBindViewHolder(PodcastViewHolder holder, int position) {

        // get the current podcast
        *//*final Podcast current = podcastArray.get(position);*//*

        cursor.moveToPosition(position);
        int titleIndex = cursor.getColumnIndex(PodcastContract.PodcastEntry.COLUMN_TITLE);
        int dateIndex = cursor.getColumnIndex(PodcastContract.PodcastEntry.COLUMN_DATE);
        int durationIndex = cursor.getColumnIndex(PodcastContract.PodcastEntry.COLUMN_DURATION);
        int imageIndex = cursor.getColumnIndex(PodcastContract.PodcastEntry.COLUMN_URL_THUMBNAIL);

        holder.titleView.setText(cursor.getString(titleIndex));


        holder.dateView.setText(cursor.getString(dateIndex));

        String duration = setDurationString(cursor.getInt(durationIndex));


        holder.durationView.setText(duration );

        String image = cursor.getString(imageIndex);


        Picasso.with(context).load(image).into(holder.imageView);
        
       *//* holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Podcast podcast = current;
                
                boolean twoPanes;
                
                if(context instanceof MainActivity){
                    twoPanes = ((MainActivity)context).getIsTwoPane();
                }else {
                    twoPanes = false;
                }
                
                if(!twoPanes) {
                    Intent intent = new Intent(context, PlayerActivity.class)
                            .putExtra(Intent.EXTRA_TEXT, podcast);
                    context.startActivity(intent);
                }else {
                    PlayerFragment fragment = new PlayerFragment();

                    Bundle bundle = new Bundle();
                    bundle.putParcelable(context.getString(R.string.podcast_key), podcast);
                    fragment.setArguments(bundle);
                    
                    ((MainActivity)context).getSupportFragmentManager().beginTransaction()
                            .replace(R.id.player_container,fragment).commit();
                }
                
                
            }
        });

        Log.i("PODCAST ADAPTER","title: "+ current.getTitle());*//*
    }*/

    private String setDurationString(long duration){

        String durationString;

        durationString = String.format("%d min, %d sec",
                TimeUnit.MILLISECONDS.toMinutes((long) duration),
                TimeUnit.MILLISECONDS.toSeconds((long) duration) -
                        TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes((long) duration)));
        return durationString;
    }

    /*@Override
    public int getItemCount() {
        return podcastArray.size();
    }*/

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        View view  = inflater.inflate(R.layout.list_item_layout,parent,false);
        return view;
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {

        TextView titleView = (TextView) view.findViewById(R.id.title_text_view);
        TextView durationView = (TextView) view.findViewById(R.id.duration_text_view);
        TextView dateView = (TextView) view.findViewById(R.id.date_text_view);
        ImageView imageView = (ImageView) view.findViewById(R.id.thumbnail_image_view);

        int titleIndex = cursor.getColumnIndex(PodcastContract.PodcastEntry.COLUMN_TITLE);
 /*       int dateIndex = cursor.getColumnIndex(PodcastContract.PodcastEntry.COLUMN_DATE);
        int durationIndex = cursor.getColumnIndex(PodcastContract.PodcastEntry.COLUMN_DURATION);
        int imageIndex = cursor.getColumnIndex(PodcastContract.PodcastEntry.COLUMN_URL_THUMBNAIL);
*/
        String title = cursor.getColumnName(TITLE);
        //Toast.makeText(context, titleIndex + ": " + ListFragment.TITLE+"= "+title, Toast.LENGTH_SHORT).show();
        titleView.setText(cursor.getString(TITLE));
        dateView.setText(cursor.getString(DATE));

        String duration = setDurationString(cursor.getInt(DURATION));
        durationView.setText(duration );

        String image = cursor.getString(THUMBNAIL);
        Picasso.with(context).load(image).into(imageView);
        
        imageView.setTransitionName(context.getString(R.string.imageTransitison)+cursor.getInt(ID));
    }


}
