package dev.mohamed.tony.talkwithme;

import android.content.Context;

import java.text.SimpleDateFormat;
import java.util.Calendar;

public class TimeAgo {
    private static final int SECOND_MILLIS = 1000;
    private static final int MINUTE_MILLIS = 60 * SECOND_MILLIS;
    private static final int HOUR_MILLIS = 60 * MINUTE_MILLIS;
    private static final int DAY_MILLIS = 24 * HOUR_MILLIS;
    private static final int WEEKS_MILLIS = 7 * DAY_MILLIS;


    public static String getTimeAgo(long time, Context ctx) {
        if (time < 1000000000000L) {
            // if timestamp given in seconds, convert to millis
            time *= 1000;
        }

        long now = Calendar.getInstance().getTimeInMillis();
        if (time > now || time <= 0) {
            return null;
        }

        // TODO: localize
        final long diff = now - time;
        if (diff < MINUTE_MILLIS) {
            return "just now";
        } else if (diff < 2 * MINUTE_MILLIS) {
            return "a minute ago";
        } else if (diff < 50 * MINUTE_MILLIS) {
            return diff / MINUTE_MILLIS +" minutes ago";
        } else if (diff < 90 * MINUTE_MILLIS) {
            return "an hour ago";
        } else if (diff < 24 * HOUR_MILLIS) {
            SimpleDateFormat currentTimeFormat=new SimpleDateFormat("h:mm a");
            String currentTime=currentTimeFormat.format(time);
            return currentTime;
        } else if (diff < 48 * HOUR_MILLIS) {
            return "yesterday";
        } else if (diff < 7 * DAY_MILLIS) {
            return diff / DAY_MILLIS + " days ago";
        } else if (diff < 2 * WEEKS_MILLIS) {
            return diff / WEEKS_MILLIS + " week ago";
        } else if (diff < 3.5 * WEEKS_MILLIS) {
            return diff / WEEKS_MILLIS + " weeks ago";
        } else {
            SimpleDateFormat currentTimeFormat=new SimpleDateFormat("dd/MM/yyyy");
            String currentTime=currentTimeFormat.format(time);
            return currentTime;
        }
    }
}
