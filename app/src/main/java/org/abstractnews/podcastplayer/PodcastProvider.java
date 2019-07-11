package org.abstractnews.podcastplayer;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.support.annotation.Nullable;

/**
 * Created by Darnell on 7/15/2016.
 */
public class PodcastProvider extends ContentProvider {

    private static final UriMatcher sUriMatcher = buildUriMatcher();

    private static UriMatcher buildUriMatcher() {
        final UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);
        final String authority = PodcastContract.CONTENT_AUTHORITY;

        matcher.addURI(authority, PodcastContract.PodcastEntry.TABLE_NAME, PODCAST);
        matcher.addURI(authority, PodcastContract.PodcastEntry.TABLE_NAME + "/#", PODCAST_WITH_ID);

        return matcher;
    }

    private PodcastDbHelper mOpenHelper;

    static final int PODCAST = 100;
    static final int PODCAST_WITH_ID = 101;


    @Override
    public boolean onCreate() {
        mOpenHelper = new PodcastDbHelper(getContext());
        return true;
    }

    @Nullable
    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        Cursor cursor;
        switch (sUriMatcher.match(uri)) {
            case PODCAST: {
               // Log.i("PROVIDER","Pre query");
                cursor = getPodcast(null, projection, selection, selectionArgs, sortOrder);
                //Log.i("PROVIDER","Post query");
                break;
            }
            case PODCAST_WITH_ID: {
                String id = uri.getPathSegments().get(1);
                //Log.i("PROVIDER","Pre query_ID");
                cursor = getPodcast(id, projection, selection, selectionArgs, sortOrder);
                //Log.i("PROVIDER","Post query_ID");
                break;
            }
            default: {
                throw new UnsupportedOperationException("Unknown uri: " + uri);
            }

        }
        if (cursor != null) {
            cursor.setNotificationUri(getContext().getContentResolver(), uri);
        }
        return cursor;
    }

    private Cursor getPodcast(String id, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        SQLiteQueryBuilder sqliteQueryBuilder = new SQLiteQueryBuilder();
        sqliteQueryBuilder.setTables(PodcastContract.PodcastEntry.TABLE_NAME);

        SQLiteDatabase db = mOpenHelper.getReadableDatabase();

        if (id != null) {
            sqliteQueryBuilder.appendWhere("_id" + " = " + id);
        }

        if (sortOrder == null || sortOrder == "") {
            sortOrder = PodcastContract.PodcastEntry._ID + " ASC";
        }
        Cursor cursor = sqliteQueryBuilder.query(db,
                projection,
                selection,
                selectionArgs,
                null,
                null,
                sortOrder);

        return cursor;
    }

    @Nullable
    @Override
    public String getType(Uri uri) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case PODCAST:
                return PodcastContract.PodcastEntry.CONTENT_DIR_TYPE;
            case PODCAST_WITH_ID:
                return PodcastContract.PodcastEntry.CONTENT_ITEM_TYPE;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
    }

    @Nullable
    @Override
    public Uri insert(Uri uri, ContentValues values) {
        try {
            long id = addNewPodcast(values);
            Uri returnUri = ContentUris.withAppendedId(uri, id);
            getContext().getContentResolver().notifyChange(uri, null);
            return returnUri;
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    public int bulkInsert(Uri uri, ContentValues[] values) {
        final int match = sUriMatcher.match(uri);
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        switch (match) {
            case PODCAST:
                db.beginTransaction();
                int returnCount = 0;
                try{
                    for(ContentValues value : values){
                        long id = db.insert(PodcastContract.PodcastEntry.TABLE_NAME,null,value);
                        if(id != -1){
                            //Log.i("DATABASE", "bulk insert: "+ id);
                            returnCount++;
                        }
                    }
                  //  Log.i("DATABASE", "bulk insert");
                    db.setTransactionSuccessful();
                }finally {
                    db.endTransaction();
                }
                getContext().getContentResolver().notifyChange(uri, null);
                return returnCount;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        String id = null;
        if (sUriMatcher.match(uri) == PODCAST_WITH_ID) {
            id = uri.getPathSegments().get(1);
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return deletePodcast(id);
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        String id = null;
        if (sUriMatcher.match(uri) == PODCAST_WITH_ID) {
            id = uri.getPathSegments().get(1);
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return updatePodcast(id, values);
    }


    private long addNewPodcast(ContentValues values) throws SQLException {
        long id = mOpenHelper.getWritableDatabase().insert(PodcastContract.PodcastEntry.TABLE_NAME, "", values);
        if (id <= 0) {
            throw new SQLException("Failed to add a podcast");
        }

        return id;
    }

    private int deletePodcast(String id) {
        if (id == null) {
            return mOpenHelper.getWritableDatabase().delete(PodcastContract.PodcastEntry.TABLE_NAME, null, null);
        } else {
            return mOpenHelper.getWritableDatabase().delete(PodcastContract.PodcastEntry.TABLE_NAME, "_id=?", new String[]{id});
        }
    }

    private int updatePodcast(String id, ContentValues values) {
        if (id == null) {
            return mOpenHelper.getWritableDatabase().update(PodcastContract.PodcastEntry.TABLE_NAME, values, null, null);
        } else {
            return mOpenHelper.getWritableDatabase().update(PodcastContract.PodcastEntry.TABLE_NAME, values, "_id=?", new String[]{id});
        }
    }
}
