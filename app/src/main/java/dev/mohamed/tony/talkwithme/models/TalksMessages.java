package dev.mohamed.tony.talkwithme.models;

/**
 * Created by Tony on 1/24/2018.
 */

public class TalksMessages {
    private String message;
    private boolean is_seen;
    private Long time;
    private String type;
    private String from;
    public TalksMessages(){

    }



    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public boolean getIs_seen() {
        return is_seen;
    }

    public void setIs_seen(boolean is_seen) {
        this.is_seen = is_seen;
    }

    public Long getTime() {
        return time;
    }

    public void setTime(Long time) {
        this.time = time;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

}
