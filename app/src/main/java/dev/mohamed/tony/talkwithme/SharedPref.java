package dev.mohamed.tony.talkwithme;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;

public class SharedPref {
    private static SharedPref sharePref = new SharedPref();
    private static SharedPreferences sharedPreferences;
    private static SharedPreferences.Editor editor;

    private static final String USER_ID = "user_id";

    private SharedPref() {
    } //prevent creating multiple instances by making the constructor private

    //The context passed into the getInstance should be application level context.
    public static SharedPref getInstance(Context context) {
        if (sharedPreferences == null) {
            sharedPreferences = context.getSharedPreferences(context.getPackageName(), Activity.MODE_PRIVATE);
            editor = sharedPreferences.edit();
        }
        return sharePref;
    }

    public void saveUserID(String user_id) {
        editor.putString(USER_ID, user_id);
        editor.commit();
    }

    public String getUserId() {
        return sharedPreferences.getString(USER_ID, "");
    }

    public void removeUserId() {
        editor.remove(USER_ID);
        editor.commit();
    }

    public void clearAll() {
        editor.clear();
        editor.commit();
    }
}
