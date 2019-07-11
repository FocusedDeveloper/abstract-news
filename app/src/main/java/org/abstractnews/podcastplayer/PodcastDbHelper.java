package org.abstractnews.podcastplayer;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by Darnell on 7/15/2016.
 */
public class PodcastDbHelper extends SQLiteOpenHelper {

    private static final int DATABASE_VERSION = 1;

    static final String DATABASE_NAME = "podcast.db";

    public PodcastDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }


    @Override
    public void onCreate(SQLiteDatabase db) {
        final String SQL_CREATE_PODCAST_TABLE = "CREATE TABLE " + PodcastContract.PodcastEntry.TABLE_NAME + "(" +
                PodcastContract.PodcastEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                PodcastContract.PodcastEntry.COLUMN_TITLE + " TEXT NOT NULL, " +
                PodcastContract.PodcastEntry.COLUMN_DATE + " TEXT NOT NULL, " +
                PodcastContract.PodcastEntry.COLUMN_DURATION + " INTEGER NOT NULL, " +
                PodcastContract.PodcastEntry.COLUMN_DESCRIPTION + " TEXT, " +
                PodcastContract.PodcastEntry.COLUMN_URL_THUMBNAIL + " TEXT, " +
                PodcastContract.PodcastEntry.COLUMN_URL_STREAM + " TEXT NOT NULL " +
                " );";
        db.execSQL(SQL_CREATE_PODCAST_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXIST " + PodcastContract.PodcastEntry.TABLE_NAME);
        db.execSQL("DELETE FROM SQLITE SEQUENCE WHERE NAME = '" + PodcastContract.PodcastEntry.TABLE_NAME + "'");

        onCreate(db);
    }


}
