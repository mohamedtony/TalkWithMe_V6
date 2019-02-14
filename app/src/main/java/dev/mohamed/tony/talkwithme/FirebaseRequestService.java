package dev.mohamed.tony.talkwithme;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.sql.Time;
import java.util.Calendar;
import java.util.Map;

public class FirebaseRequestService extends FirebaseMessagingService {
    private String notification_title = null, notification_message = null, click_action = null;
    private String from_user_id = null, notification_time = null;
    private Long timeSent = 0L;
    public static String REQUEST_FRIEND="dev.mohamed.tony.talkwithme_REQUEST_NOTIFICATION";
    public static String NOTIFY_MESSAGE="dev.mohamed.tony.talkwithme_MESSAGE_NOTIFICATION";
    public static int NOTIFICATION_REQUEST_ID=0;
    public static int NOTIFICATION_MESSAGE_ID=2;
    PendingIntent pendingIntent;
    private DatabaseReference mDataBaseRef;

    @Override
    public void onNewToken(String token) {
        super.onNewToken(token);
        Log.d("new_token",token);

        // TODO: Implement this method to send token to your app server.
        mDataBaseRef=FirebaseDatabase.getInstance().getReference();
        FirebaseUser currentUserId=FirebaseAuth.getInstance().getCurrentUser();
        if(currentUserId!=null){
            mDataBaseRef.child(currentUserId.getUid()).child("device_token").setValue(token);
        }
    }

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);

        //=========== to call onMessageReceived on foreground and background and when killed
        // we must use ==> data instead of notification in node.js code
        // ref : https://stackoverflow.com/a/38795553

        if (remoteMessage.getData().size() > 0) {

            String action=remoteMessage.getData().get("click_action");
            if(action.equals(REQUEST_FRIEND)) {
                sendRequestNotification(remoteMessage.getData());
            }
            else {
                sendMessageNotification(remoteMessage.getData());
            }

        }

    }

    private void sendRequestNotification(Map<String, String> messageBody) {

        notification_title = messageBody.get("title");
        notification_message = messageBody.get("body");
        notification_time = messageBody.get("sent_time");
        timeSent = Long.parseLong(notification_time);
        click_action = messageBody.get("click_action");
        from_user_id = messageBody.get("sender_user_id");


        Intent intent = new Intent(click_action);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.putExtra(MyConstants.UID, from_user_id);
         pendingIntent = PendingIntent.getActivity(this, NOTIFICATION_REQUEST_ID /* Request code */, intent,
                PendingIntent.FLAG_ONE_SHOT);

        notificationBuilder(NOTIFICATION_REQUEST_ID);
       }


    private void sendMessageNotification(Map<String, String> messageBody) {

        notification_title = messageBody.get("title");
        notification_message = messageBody.get("body");
        notification_time = messageBody.get("sent_time");
        timeSent = Long.parseLong(notification_time);
        click_action = messageBody.get("click_action");
        from_user_id = messageBody.get("sender_user_id");
        String user_image=messageBody.get("user_image");
        String user_name=messageBody.get("userName");


        Intent intent = new Intent(click_action);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.putExtra(MyConstants.UID, from_user_id);
        intent.putExtra(MyConstants.UNAME, user_name);
        intent.putExtra(MyConstants.UIMAGE, user_image);
        pendingIntent = PendingIntent.getActivity(this, NOTIFICATION_REQUEST_ID /* Request code */, intent,
                PendingIntent.FLAG_ONE_SHOT);

        notificationBuilder(NOTIFICATION_MESSAGE_ID);

    }
       private void notificationBuilder(int noti_id){
           Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
           String channelId = getString(R.string.default_notification_channel_id);
           NotificationCompat.Builder notificationBuilder =
                   new NotificationCompat.Builder(this, channelId)
                           .setPriority(NotificationCompat.PRIORITY_HIGH)
                           .setWhen(timeSent)
                           .setSmallIcon(R.mipmap.chat)
                           .setContentTitle(notification_title)
                           .setContentText(notification_message)
                           .setAutoCancel(true)
                           .setSound(defaultSoundUri)
                           .setContentIntent(pendingIntent);

           NotificationManager notificationManager =
                   (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

           // Since android Oreo notification channel is needed.
           if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
               NotificationChannel channel = new NotificationChannel(channelId,
                       "Channel human readable title",
                       NotificationManager.IMPORTANCE_HIGH);
               if (notificationManager != null) {
                   notificationManager.createNotificationChannel(channel);
               }
           }

           notificationManager.notify(noti_id /* ID of notification */, notificationBuilder.build());

       }
}

