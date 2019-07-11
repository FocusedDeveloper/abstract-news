package org.abstractnews.podcastplayer;

import android.test.AndroidTestCase;

/**
 * Created by Darnell on 7/15/2016.
 */
public class TestMyDB extends AndroidTestCase {

   /* private Context getTestContext() {
        try {
            Method getTestContext = ServiceTestCase.class.getMethod("getTestContext");
            return (Context) getTestContext.invoke(this);
        } catch (final Exception exception) {
            exception.printStackTrace();
            return null;
        }
    }*/

    public void testPodcastTable() {


      /* *//* Context context = getTestContext();*//*

        PodcastDbHelper dbHelper = new PodcastDbHelper(mContext);
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        ContentValues contentValues = new ContentValues();
        contentValues.put(PodcastContract.PodcastEntry.COLUMN_TITLE, "In Love With Her");
        contentValues.put(PodcastContract.PodcastEntry.COLUMN_DATE, "1983/01/18");
        contentValues.put(PodcastContract.PodcastEntry.COLUMN_DESCRIPTION, "She's Amazing");
        contentValues.put(PodcastContract.PodcastEntry.COLUMN_DURATION, 555555);
        contentValues.put(PodcastContract.PodcastEntry.COLUMN_URL_THUMBNAIL, "www.shesFine.com");
        contentValues.put(PodcastContract.PodcastEntry.COLUMN_URL_STREAM, "www.SingItGirl.com");

        long podcastRowId;

        podcastRowId = db.insert(PodcastContract.PodcastEntry.TABLE_NAME, null, contentValues);

        assertTrue(podcastRowId != -1);

        Cursor cursor = db.query(PodcastContract.PodcastEntry.TABLE_NAME, // Table Name
                null, // which columns = all
                null, // columns where...
                null, // values where...
                null, // columns to group by
                null, // columns to filter by row groups
                null // sort order
        );

        // assertTrue(false);

        assertTrue("Error: query returned no results", cursor.moveToFirst());

        int titleIndex = cursor.getColumnIndex(PodcastContract.PodcastEntry.COLUMN_TITLE);



        assertEquals("Error: not what expected from cursor", "In Love With Her",cursor.getString(titleIndex));

        cursor.close();
        db.close();*/
    }
}
