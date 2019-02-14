package dev.mohamed.tony.talkwithme.widget;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;

import dev.mohamed.tony.talkwithme.models.User;

import java.util.ArrayList;

import dev.mohamed.tony.talkwithme.MyConstants;


/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p>
 * TODO: Customize class - update intent actions and extra parameters.
 */

//(Context mContext, List<String> usersKeyList, List<User> userList, List<String> usersDatesList)


public class UpdateTalkIntentService extends IntentService {


    public UpdateTalkIntentService() {
        super(MyConstants.UPDATE_BAKING_INTENT_SERVICE);
    }

    public static void startBakingService(Context context, ArrayList<String> usersKeyList, ArrayList<User> userList, ArrayList<String> usersDatesList) {

        Intent intent = new Intent(context, UpdateTalkIntentService.class);
        intent.putExtra(MyConstants.MY_USERS_KEY_LIST, usersKeyList);
        intent.putParcelableArrayListExtra(MyConstants.MY_USERS_LIST, userList);
        intent.putExtra(MyConstants.MY_USERS_DATE_LIST, usersDatesList);
        context.startService(intent);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            ArrayList<User> userArrayList = intent.getExtras().getParcelableArrayList(MyConstants.MY_USERS_LIST);
            ArrayList<String> usersKeyList = intent.getExtras().getStringArrayList(MyConstants.MY_USERS_KEY_LIST);
            ArrayList<String> usersDatesList = intent.getExtras().getStringArrayList(MyConstants.MY_USERS_DATE_LIST);
            handleActionUpdateBakingWidgets(usersKeyList, userArrayList, usersDatesList);
        }
    }


    private void handleActionUpdateBakingWidgets(ArrayList<String> usersKeyList, ArrayList<User> userList, ArrayList<String> usersDatesList) {
        Intent intent = new Intent(MyConstants.ANDROID_APPWIDGET_ACTION_APPWIDGET_UPDATE2);
        intent.setAction(MyConstants.ANDROID_APPWIDGET_ACTION_APPWIDGET_UPDATE2);
        intent.putParcelableArrayListExtra(MyConstants.MY_USERS_LIST, userList);
        intent.putExtra(MyConstants.MY_USERS_KEY_LIST, usersKeyList);
        intent.putExtra(MyConstants.MY_USERS_DATE_LIST, usersDatesList);
        sendBroadcast(intent);
    }
}