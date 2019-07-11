package org.abstractnews.podcastplayer;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by Darnell on 7/14/2016.
 */
public class Podcast implements Parcelable{

    private String id;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date.substring(0,10);
    }

    public long getDuration() {
        return duration;
    }

    public void setDuration(long duration) {

        this.duration = duration;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getUrlThumbnail() {
        return urlThumbnail;
    }

    public void setUrlThumbnail(String urlThumbnail) {
        this.urlThumbnail = urlThumbnail;
    }

    private String title;
    private String date;
    private long duration;
    private String description;
    private String urlThumbnail;
    private String urlStream;

    // test data...
    public static String[] titleDummyData = {"Title 1", "Title 2", "Title 3", "Title 4", "Title 5", "Title 6", "Title 7", "Title 8"};
    public static String[] dateDummyData = {"2016/01/18", "2016/04/20", "2016/06/10", "2016/10/01", "2016/09/12", "2016/12/25",
            "2016/01/01", "2016/06/25"};
    public static long [] durationDummyData = {11313,234452,53452345,73423,235345,23545467,575463,3243423};

    public Podcast() {

    }

    public Podcast(String id, String title, String date, long duration, String description, String urlThumbnail, String urlStream){
        this.id = id;
        this.title = title;
        this.date = date;
        this.duration = duration;
        this.description = description;
        this.urlThumbnail = urlThumbnail;
        this.urlStream = urlStream;
    }

    public Podcast(Parcel input){
        id = input.readString();
        title = input.readString();
        date = input.readString();
        duration = input.readLong();
        description = input.readString();
        urlThumbnail = input.readString();
        urlStream = input.readString();

    }

    @Override
    public int describeContents() {
        return 0;
    }

    public String getUrlStream() {
        return urlStream;
    }

    public void setUrlStream(String urlStream) {
        this.urlStream = urlStream;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.id);
        dest.writeString(this.title);
        dest.writeString(this.date);
        dest.writeLong(this.duration);
        dest.writeString(this.description);
        dest.writeString(this.urlThumbnail);
        dest.writeString(this.urlStream);
    }

    public static final Parcelable.Creator<Podcast> CREATOR
            = new Parcelable.Creator<Podcast>() {
        public Podcast createFromParcel(Parcel in) { return new Podcast(in); }

        public Podcast[] newArray(int size) { return new Podcast[size]; }
    };
}
