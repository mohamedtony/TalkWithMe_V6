package dev.mohamed.tony.talkwithme.models;

/**
 * Created by Tony on 1/25/2018.
 */

public class Talk {
    private String is_seen;
    private Long time;

    public Talk() {
    }

    public String getIs_seen() {
        return is_seen;
    }

    public void setIs_seen(String is_seen) {
        this.is_seen = is_seen;
    }

    public Long getTime() {
        return time;
    }

    public void setTime(Long time) {
        this.time = time;
    }
}
