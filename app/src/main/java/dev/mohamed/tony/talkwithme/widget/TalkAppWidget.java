package dev.mohamed.tony.talkwithme.widget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;

import dev.mohamed.tony.talkwithme.MyConstants;
import dev.mohamed.tony.talkwithme.R;
import dev.mohamed.tony.talkwithme.activities.MainActivity;
import dev.mohamed.tony.talkwithme.models.User;

import java.util.ArrayList;


/**
 * Implementation of App Widget functionality.
 */
public class TalkAppWidget extends AppWidgetProvider {


    static ArrayList<String> usersKeyList = new ArrayList<>();
    static ArrayList<User> usersList = new ArrayList<>();
    static ArrayList<String> usersDateList = new ArrayList<>();

    static void updateAppWidget(Context context, AppWidgetManager appWidgetManager, int appWidgetId) {


        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_grid_view);

        Intent appIntent = new Intent(context, MainActivity.class);
        PendingIntent appPendingIntent = PendingIntent.getActivity(context, 0, appIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        views.setPendingIntentTemplate(R.id.widget_grid_view, appPendingIntent);


        Intent intent = new Intent(context, MyTalkViewWidgetService.class);
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
        views.setRemoteAdapter(R.id.widget_grid_view, intent);

        appWidgetManager.updateAppWidget(appWidgetId, views);

    }

    public static void updateBakingWidgets(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        for (int appWidgetId : appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId);
        }
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {

    }

    @Override
    public void onEnabled(Context context) {

    }

    @Override
    public void onDisabled(Context context) {

    }

    @Override
    public void onReceive(Context context, Intent intent) {
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
        int[] appWidgetIds = appWidgetManager.getAppWidgetIds(new ComponentName(context, TalkAppWidget.class));


        final String action = intent.getAction();

        if (action != null && action.equals(MyConstants.ANDROID_APPWIDGET_ACTION_APPWIDGET_UPDATE2)) {

            usersList =  intent.getExtras().getParcelableArrayList(MyConstants.MY_USERS_LIST);
            usersKeyList = intent.getExtras().getStringArrayList(MyConstants.MY_USERS_KEY_LIST);
            usersDateList = intent.getExtras().getStringArrayList(MyConstants.MY_USERS_DATE_LIST);

            appWidgetManager.notifyAppWidgetViewDataChanged(appWidgetIds, R.id.widget_grid_view);
            TalkAppWidget.updateBakingWidgets(context, appWidgetManager, appWidgetIds);
            super.onReceive(context, intent);
        }
    }
}
