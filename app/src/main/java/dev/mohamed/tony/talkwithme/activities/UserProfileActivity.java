package dev.mohamed.tony.talkwithme.activities;

import android.content.Intent;
import android.graphics.Color;
import android.icu.text.TimeZoneFormat;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import dev.mohamed.tony.talkwithme.MyConstants;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.messaging.FirebaseMessaging;
import com.squareup.picasso.Picasso;

import java.text.DateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import dev.mohamed.tony.talkwithme.R;
import dev.mohamed.tony.talkwithme.SharedPref;

public class UserProfileActivity extends AppCompatActivity {


    public static String isReqeustPrefs = MyConstants.IS_FRIEND_REQUEST_SENT;


    @BindView(R.id.user_profile_imageView)
    ImageView userImage;
    @BindView(R.id.user_profile_name)
    TextView userNameText;
    @BindView(R.id.add_conatct)
    Button add_contact;
    @BindView(R.id.delete_contact)
    Button delete_contact;
    @BindView(R.id.fab)
    FloatingActionButton fab;
    private String isFriendRequestSent = "Not_Friends";
    private DatabaseReference mFireBaseDatabase, mTalksRequestData, mContactDataBase, mNotificationDataBase, mRootDatabase;
    private FirebaseUser currentUser;
    private FirebaseAuth mAuth;
    private String user_id;
    private String userName;
    private String user_image;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //   setContentView(R.layout.activity_user_profile);
        setContentView(R.layout.activity_user_profile);
        ButterKnife.bind(this);
        if (getIntent().getExtras() != null) {
            user_id = getIntent().getExtras().getString(MyConstants.UID);
            Toast.makeText(this, " user_id " + user_id, Toast.LENGTH_SHORT).show();
            //user_id = getIntent().getStringExtra(MyConstants.UID);
        } else {
            user_id = getIntent().getStringExtra(MyConstants.UID);

        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        mAuth = FirebaseAuth.getInstance();
        if (mAuth.getCurrentUser() != null) {
            currentUser = mAuth.getCurrentUser();
            loadProfile();

            mFireBaseDatabase.child(currentUser.getUid()).child("online").setValue("true");
            //Add to Activity
           //// FirebaseMessaging.getInstance().unsubscribeFromTopic("sendNotification");
        } else {
            Toast.makeText(UserProfileActivity.this, getString(R.string.no_user), Toast.LENGTH_SHORT).show();
            gotoStartActivity();

        }

    }

    private void gotoStartActivity() {
        Intent intent = new Intent(UserProfileActivity.this, StartActivity.class);
        startActivity(intent);
    }

    private void loadProfile() {
        mTalksRequestData = FirebaseDatabase.getInstance().getReference().child(MyConstants.TALKS_REQUESTA);
        mTalksRequestData.keepSynced(true);
        mContactDataBase = FirebaseDatabase.getInstance().getReference().child(MyConstants.CONTACTS);
        mContactDataBase.keepSynced(true);
        mFireBaseDatabase = FirebaseDatabase.getInstance().getReference().child(MyConstants.USERS);
        mFireBaseDatabase.keepSynced(true);
        mNotificationDataBase = FirebaseDatabase.getInstance().getReference().child(MyConstants.NOYIFICATION);
        mNotificationDataBase.keepSynced(true);
        mRootDatabase = FirebaseDatabase.getInstance().getReference();
        mRootDatabase.keepSynced(true);
        // Read from the database
        if (user_id != null) {
            mFireBaseDatabase.child(user_id).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    // This method is called once with the initial value and again
                    // whenever data at this location is updated.
                    if (dataSnapshot.hasChild(MyConstants.NAME)) {
                        userName = dataSnapshot.child(MyConstants.NAME).getValue().toString();

                        if (!TextUtils.isEmpty(userName)) {
                            userNameText.setText(userName);
                        }
                    }
                    if (dataSnapshot.hasChild(MyConstants.TAG_IMAGE)) {
                        user_image = dataSnapshot.child(MyConstants.TAG_IMAGE).getValue().toString();
                        if (!user_image.matches("null")) {
                            Picasso.with(UserProfileActivity.this).load(user_image).into(userImage);
                        }

                    }


                    // Log.d(TAG, "Value is: " + value);
                    // Toast.makeText(ProfileSettingsActivity.this, "value : "+value, Toast.LENGTH_SHORT).show();
                    mTalksRequestData.child(currentUser.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            if (dataSnapshot.hasChild(user_id)) {
                                String requestType = dataSnapshot.child(user_id).child(MyConstants.REQUEST_TYPE).getValue().toString();
                                Log.d("in_mTalks"," inTalks");
                                if (requestType.matches(MyConstants.SENT)) {

                                    add_contact.setText(getString(R.string.request_sent_btn));
                                    add_contact.setTextColor(Color.parseColor("#005694"));
                                    add_contact.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_friend_accepted, 0, 0, 0);
                                    isFriendRequestSent = "Request_sent"; //sent
                                    delete_contact.setEnabled(false);

                                } else if (requestType.matches(MyConstants.RECIEVED)) {
                                    add_contact.setText(getString(R.string.accept_req));
                                    add_contact.setTextColor(Color.parseColor("#005694"));
                                    add_contact.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_friend_accepted, 0, 0, 0);
                                    isFriendRequestSent = "Request_received"; //recieved
                                    delete_contact.setEnabled(false);
                                }else{
                                    add_contact.setText(getString(R.string.add_contact));
                                }
                            } else {

                                //============================== if they are friends================================
                                mContactDataBase.child(currentUser.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(DataSnapshot dataSnapshot) {
                                        if (dataSnapshot.hasChild(user_id)) {
                                            if (dataSnapshot.child(user_id).child(MyConstants.DATE).getValue() != null) {
                                                isFriendRequestSent = "Friends" /*freinds*/;
                                                add_contact.setText(getString(R.string.frind));
                                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                                    add_contact.setTextColor(getColor(R.color.colorPrimary));
                                                }
                                                add_contact.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_friends, 0, 0, 0);
                                                add_contact.setEnabled(false);
                                                delete_contact.setEnabled(true);
                                                //  delete_contact.setText(getString(R.string.cancel_friend));

                                            }
                                        }
                                    }

                                    @Override
                                    public void onCancelled(DatabaseError databaseError) {

                                    }
                                });
                            }

                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });
                }

                @Override
                public void onCancelled(DatabaseError error) {
                    // Failed to read value
                    // Log.w(TAG, "Failed to read value.", error.toException());
                }
            });
        }


        //============================================ add contact button =========================================================
        //==========================================================================================================================
        add_contact.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                //================== code after minimized not_friend only =================================
                //=========================================================================================
                if (isFriendRequestSent.equals("Not_Friends")) {

                    DatabaseReference notificationRef = mRootDatabase.child(MyConstants.NOYIFICATION).child(user_id).push();
                    String notificationID = notificationRef.getKey();
                    HashMap notificationData = new HashMap<>();
                    notificationData.put("from", currentUser.getUid());
                    notificationData.put("type", "request");
                    notificationData.put("time", String.valueOf(Calendar.getInstance().getTimeInMillis()));

                    Map requestMapNotifi = new HashMap<>();
                    requestMapNotifi.put(MyConstants.TALKS_REQUESTA + "/" + currentUser.getUid() + "/" + user_id + "/" + MyConstants.REQUEST_TYPE, MyConstants.SENT);
                    requestMapNotifi.put(MyConstants.TALKS_REQUESTA + "/" + user_id + "/" + currentUser.getUid() + "/" + MyConstants.REQUEST_TYPE, MyConstants.RECIEVED);
                    requestMapNotifi.put(MyConstants.NOYIFICATION + "/" + user_id + "/" + notificationID, notificationData);

                    mRootDatabase.updateChildren(requestMapNotifi, new DatabaseReference.CompletionListener() {
                        @Override
                        public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {

                            if (databaseError != null) {
                                Toast.makeText(UserProfileActivity.this, getString(R.string.cannot_send), Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(UserProfileActivity.this, getString(R.string.request_sent), Toast.LENGTH_SHORT).show();
                                SharedPref.getInstance(UserProfileActivity.this).saveUserID(user_id);
                                isFriendRequestSent = "Request_sent"; //sent
                                add_contact.setText(getString(R.string.request_sent_btn));
                                add_contact.setTextColor(Color.parseColor("#005694"));
                                add_contact.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_friend_accepted, 0, 0, 0);
                            }

                        }
                    });
                }


                //================== code after minimized Request_sent only =================================
                //================== code after minimized Request_sent only =================================
                if (isFriendRequestSent.equals("Request_sent")/*sent*/) {
                    Map requestSent=new HashMap();
                    requestSent.put(MyConstants.TALKS_REQUESTA+"/"+currentUser.getUid()+"/"+user_id,null);
                    requestSent.put(MyConstants.TALKS_REQUESTA+"/"+user_id+"/"+currentUser.getUid(),null);

                    mRootDatabase.updateChildren(requestSent, new DatabaseReference.CompletionListener() {
                        @Override
                        public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                            add_contact.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_add_friend, 0);
                            if(databaseError==null){
                                isFriendRequestSent = "Not_Friends";//Not_Friends
                                add_contact.setText(getString(R.string.add_contact));
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                    add_contact.setTextColor(getColor(R.color.colorPrimary));
                                }
                                add_contact.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_add_friend, 0);


                            }else {
                                Toast.makeText(UserProfileActivity.this, databaseError.getMessage(), Toast.LENGTH_SHORT).show();

                            }
                        }
                    });
                }

                //================== code after minimized Request_received only =================================
                //================== code after minimized Request_received only =================================
                if (isFriendRequestSent.equals("Request_received")) { //recieved
                    final String currentDate = DateFormat.getDateInstance().format(new Date());
                    //String currentTime= TimeZoneFormat.getInstance(Locale.getDefault()).
                    //final Long currentDate = ServerValue.TIMESTAMP;
                    Map contactReceived = new HashMap();
                    contactReceived.put(MyConstants.CONTACTS + "/" + currentUser.getUid() + "/" + user_id + "/" + MyConstants.DATE, currentDate);
                    contactReceived.put(MyConstants.CONTACTS + "/" + user_id + "/" + currentUser.getUid() + "/" + MyConstants.DATE, currentDate);

                    contactReceived.put(MyConstants.TALKS_REQUESTA + "/" + currentUser.getUid() + "/" + user_id, null);
                    contactReceived.put(MyConstants.TALKS_REQUESTA + "/" + user_id + "/" + currentUser.getUid(), null);

                    mRootDatabase.updateChildren(contactReceived, new DatabaseReference.CompletionListener() {
                        @Override
                        public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {

                            if (databaseError != null) {
                                Toast.makeText(UserProfileActivity.this, databaseError.getMessage(), Toast.LENGTH_SHORT).show();

                            } else {
                                isFriendRequestSent = "Friends" /*freinds*/;
                                add_contact.setText(getString(R.string.frind));
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                    add_contact.setTextColor(getColor(R.color.colorPrimary));
                                }
                                add_contact.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_friends, 0, 0, 0);
                                add_contact.setEnabled(false);
                                delete_contact.setEnabled(true);
                            }
                        }
                    });

                }

            }


        });



        //================================= cancel friend chip button ============================================
        //========================================================================================================
        delete_contact.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Map deleteContact=new HashMap();
                deleteContact.put(MyConstants.CONTACTS+"/"+currentUser.getUid()+"/"+user_id,null);
                deleteContact.put(MyConstants.CONTACTS+"/"+user_id+"/"+currentUser.getUid(),null);

                mRootDatabase.updateChildren(deleteContact, new DatabaseReference.CompletionListener() {
                    @Override
                    public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {

                        if(databaseError==null){
                            delete_contact.setEnabled(false);
                            add_contact.setEnabled(true);
                            isFriendRequestSent = "Not_Friends";//Not_Friends
                            add_contact.setText(getString(R.string.add_contact));
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                add_contact.setTextColor(getColor(R.color.colorPrimary));
                            }
                            add_contact.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_add_friend, 0);
                        }else{
                            Toast.makeText(UserProfileActivity.this, databaseError.getMessage(), Toast.LENGTH_SHORT).show();

                        }
                    }
                });
            }
        });

        //============================================ fab button =========================================
        //=================================================================================================

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //   Toast.makeText(UserProfileActivity.this, " you vlicked", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(UserProfileActivity.this, TalksActivity.class);
                intent.putExtra(MyConstants.UUID, user_id);
                intent.putExtra(MyConstants.UNAME, userName);
                intent.putExtra(MyConstants.UIMAGE, user_image);
                startActivity(intent);
            }
        });

    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mAuth.getCurrentUser() != null) {
            mFireBaseDatabase.child(mAuth.getCurrentUser().getUid()).child("online").setValue("false");
        }
    }
}


