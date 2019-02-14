package dev.mohamed.tony.talkwithme;

import android.app.Application;
import android.util.Log;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.OkHttpDownloader;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.Map;

/**
 * Created by Tony on 1/22/2018.
 */

public class TalkWithMe extends Application {
    /****
     * For online Feature
     *
     */

    private DatabaseReference mDatabaseRef;
    private FirebaseAuth mAuth;


    @Override
    public void onCreate() {
        super.onCreate();

        FirebaseDatabase.getInstance().setPersistenceEnabled(true);

        Picasso.Builder builder=new Picasso.Builder(this);
        builder.downloader(new OkHttpDownloader(this,Integer.MAX_VALUE));
        Picasso built=builder.build();
        built.setIndicatorsEnabled(true);
        built.setLoggingEnabled(true);
        Picasso.setSingletonInstance(built);

        mAuth=FirebaseAuth.getInstance();


        if(mAuth!=null) {
            if (mAuth.getCurrentUser() != null) {
                mDatabaseRef = FirebaseDatabase.getInstance().getReference().child("Users").child(mAuth.getCurrentUser().getUid());

                mDatabaseRef.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        mDatabaseRef.child("online").onDisconnect().setValue("false");
                        mDatabaseRef.child("lastSeen").onDisconnect().setValue(ServerValue.TIMESTAMP);
                        mDatabaseRef.child("online")/*.onDisconnect()*/.setValue("true");

                       /* Calendar time=Calendar.getInstance();
                        SimpleDateFormat currentTimeFormat=new SimpleDateFormat("EEE h:mm a");
                        String currentTime=currentTimeFormat.format(time.getTime());*/

                        //mDatabaseRef.child("lastSeen").setValue(currentTime);
                     /*  String ago = TimeAgo.getTimeAgo(time.getTimeInMillis(),getApplicationContext());
                      Map m =ServerValue.TIMESTAMP;
                        Log.d("dateeee",ago);

                        mDatabaseRef.child("lastSeen").setValue(ago);*/
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
            }
        }

    }
}
