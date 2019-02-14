package dev.mohamed.tony.talkwithme.widget;

import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.widget.RemoteViews;

import dev.mohamed.tony.talkwithme.MyConstants;
import dev.mohamed.tony.talkwithme.models.User;

import java.io.IOException;
import java.util.ArrayList;

import android.widget.RemoteViewsService;

import dev.mohamed.tony.talkwithme.R;
import com.squareup.picasso.Picasso;

public class MyTalkViewWidgetService extends RemoteViewsService {
    ArrayList<User> usersList;
    ArrayList<String> usersKeyList;
    ArrayList<String> usersDateList;

    int appWidgetId;

    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
        appWidgetId = intent.getIntExtra(
                AppWidgetManager.EXTRA_APPWIDGET_ID,
                AppWidgetManager.INVALID_APPWIDGET_ID);

        return new GridRemoteViewsFactory(this.getApplicationContext(), intent);
    }


    class GridRemoteViewsFactory implements RemoteViewsService.RemoteViewsFactory {

        Context mContext = null;

        public GridRemoteViewsFactory(Context context, Intent intent) {
            mContext = context;

            appWidgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID,
                    AppWidgetManager.INVALID_APPWIDGET_ID);

        }

        @Override
        public void onCreate() {
        }

        @Override
        public void onDataSetChanged() {
            usersList = TalkAppWidget.usersList;
            usersKeyList = TalkAppWidget.usersKeyList;
            usersDateList = TalkAppWidget.usersDateList;
        }

        @Override
        public void onDestroy() {

        }

        @Override
        public int getCount() {

            return usersList.size();
        }

        @Override
        public RemoteViews getViewAt(int position) {

            RemoteViews remoteViews = new RemoteViews(mContext.getPackageName(), R.layout.widget_grid_view_item);
            if(usersList!=null&&!usersList.isEmpty()) {
                remoteViews.setTextViewText(R.id.widget_grid_view_item, usersList.get(position).getName());
             //   remoteViews.setTextViewText(R.id.widget_date, usersDateList.get(position));


            try {
                if (!usersList.get(position).getImage().matches("null")) {
                    Bitmap b = Picasso.with(mContext).load(usersList.get(position).getImage()).get();
                    remoteViews.setImageViewBitmap(R.id.userImageView, b);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            }

            Intent fillInIntent = new Intent();
            Bundle bundle = new Bundle();
            if(usersList!=null&&!usersList.isEmpty()) {
                bundle.putString(MyConstants.UID, usersKeyList.get(position));
            }
            fillInIntent.putExtras(bundle);
            remoteViews.setOnClickFillInIntent(R.id.relative_layout_item, fillInIntent);

            return remoteViews;
        }

        @Override
        public RemoteViews getLoadingView() {
            return null;
        }

        @Override
        public int getViewTypeCount() {
            return 1;
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public boolean hasStableIds() {
            return true;
        }


    }


}
