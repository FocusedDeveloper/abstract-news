package org.abstractnews.podcastplayer;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Created by Darnell on 7/15/2016.
 */
public class PodcastContract {

    public static final String CONTENT_AUTHORITY = "org.abstractnews.podcastplayer.app";

    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);

    public static final class PodcastEntry implements BaseColumns{

        public static final String _ID = "_id";
        public static final String COLUMN_VERSION_NAME = "version_name";

        public static final  String TABLE_NAME = "podcast";
        public static final  String COLUMN_TITLE = "title";
        public static final  String COLUMN_DATE = "date";
        public static final  String COLUMN_DURATION = "duration";
        public static final  String COLUMN_DESCRIPTION = "description";
        public static final  String COLUMN_URL_THUMBNAIL = "urlThumbnail";
        public static final  String COLUMN_URL_STREAM = "urlStream";

        public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon()
                .appendPath(TABLE_NAME).build();

        public static final String CONTENT_DIR_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" +TABLE_NAME;

        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" +TABLE_NAME;

        public static Uri buildPodcastUri( long id ){
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }


    }
}
