package dev.mohamed.tony.talkwithme.activities;

import android.content.Intent;
import android.os.Build;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import dev.mohamed.tony.talkwithme.MyConstants;
import dev.mohamed.tony.talkwithme.R;
import dev.mohamed.tony.talkwithme.TimeAgo;
import dev.mohamed.tony.talkwithme.adapters.TalksMsgAdapter;
import dev.mohamed.tony.talkwithme.models.TalksMessages;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;

import static dev.mohamed.tony.talkwithme.MyConstants.CHAT;
import static dev.mohamed.tony.talkwithme.MyConstants.FROM;
import static dev.mohamed.tony.talkwithme.MyConstants.IS_SEEN;
import static dev.mohamed.tony.talkwithme.MyConstants.MESSAGE;
import static dev.mohamed.tony.talkwithme.MyConstants.MESSAGES_TALKS;
import static dev.mohamed.tony.talkwithme.MyConstants.MESSAGES_TALKS1;
import static dev.mohamed.tony.talkwithme.MyConstants.TEXT;
import static dev.mohamed.tony.talkwithme.MyConstants.TIME;
import static dev.mohamed.tony.talkwithme.MyConstants.TYPE;
import static dev.mohamed.tony.talkwithme.MyConstants.UIMAGE;
import static dev.mohamed.tony.talkwithme.MyConstants.UNAME;
import static java.security.AccessController.getContext;

public class TalksActivity extends AppCompatActivity {

    public static final int VIEW_TYPE_USER_MESSAGE = 0;
    public static final int VIEW_TYPE_FRIEND_MESSAGE = 1;
    @BindView(R.id.toolbar_talks)
    Toolbar toolbar;
    @BindView(R.id.inputMsg)
    EditText myMsg;
    @BindView(R.id.btnSend)
    ImageButton btnSend;
    @BindView(R.id.talks_recyclerView)
    RecyclerView talks_recyclerView;
    // @BindView(R.id.user_name_toolbar)
    private TextView userNameToolbar,lastSeenView;
    // @BindView(R.id.user_image_toolbar)
    private ImageView userImageToolbar;
    private String mUserid, userName, userImage;
    private DatabaseReference mDataBaseRef,mUnread;
    private FirebaseAuth mAuth;
    private String currentUser;
    private LinearLayoutManager layoutManager;
    private TalksMsgAdapter talksMsgAdapter;
    private List<TalksMessages> talksMessagesList;
    private Thread thread;
    private Long time;
    private static int TOTAL_MSGS_TO_SHOW=10;
    private int mCurrentPage=1;
    private SwipeRefreshLayout swipeRefreshLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_talks);
        ButterKnife.bind(this);

        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        swipeRefreshLayout=findViewById(R.id.swipToRefresh);
        if(actionBar!=null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setDisplayShowCustomEnabled(true);
            actionBar.setDisplayShowTitleEnabled(false);
        }

        if (getIntent().getExtras() != null) {
            mUserid = getIntent().getExtras().getString(MyConstants.UUID);
            userName = getIntent().getExtras().getString(UNAME);
            userImage = getIntent().getExtras().getString(UIMAGE);
        } else {
            mUserid = getIntent().getStringExtra(MyConstants.UUID);
            userName = getIntent().getStringExtra(UNAME);
            userImage = getIntent().getStringExtra(UIMAGE);
        }


        layoutManager = new LinearLayoutManager(this);
        //layoutManager.setReverseLayout(true);
        talks_recyclerView.setHasFixedSize(true);
        talks_recyclerView.setLayoutManager(layoutManager);
        talksMessagesList = new ArrayList<>();
        talksMsgAdapter = new TalksMsgAdapter(TalksActivity.this, talksMessagesList);
        talks_recyclerView.setAdapter(talksMsgAdapter);

        userNameToolbar = findViewById(R.id.user_name_toolbar);
        userImageToolbar = findViewById(R.id.user_image_toolbar);
        userNameToolbar.setText(userName);
        lastSeenView=findViewById(R.id.last_seen);

        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                mCurrentPage++;
                talksMessagesList.clear();
                loadChats();
            }
        });


    }

    @Override
    protected void onStart() {
        super.onStart();

        mAuth = FirebaseAuth.getInstance();
        if (mAuth.getCurrentUser() != null) {
            currentUser = mAuth.getCurrentUser().getUid();
            mDataBaseRef = FirebaseDatabase.getInstance().getReference();

            mDataBaseRef.keepSynced(true);
            mDataBaseRef.child(CHAT).child(currentUser).child(mUserid).child(IS_SEEN).setValue("true");
            mDataBaseRef.child(CHAT).child(currentUser).child(mUserid).child(TIME).setValue(ServerValue.TIMESTAMP);





            loadChats();
            mDataBaseRef.child(MyConstants.USERS).child(currentUser).child("online").setValue("true");

            mDataBaseRef.child(MyConstants.USERS).child(mUserid).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if(dataSnapshot.hasChildren()){
                      //  String lastSeen=dataSnapshot.child("lastSeen").getValue().toString();
                        String online=dataSnapshot.child("online").getValue().toString();
                        if(dataSnapshot.child("lastSeen").getValue()!=null)
                         time=Long.parseLong(dataSnapshot.child("lastSeen").getValue().toString());
                        if(online.matches("true")){
                            lastSeenView.setText("online");
                            stopCountdown();
                        }else{
                            if(time!=null) {
                                String timmme = TimeAgo.getTimeAgo(time, getApplicationContext());
                            }
                          //  Log.d("last_seen",timmme);
                            //lastSeenView.setText(timmme);
                          //  if(thread.)
                           // updateTime(time);
                            startCountdown();

                        }

                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
         /*   myMsg.addOnAttachStateChangeListener(new View.OnAttachStateChangeListener() {
                @Override
                public void onViewAttachedToWindow(View view) {
                    if (Build.VERSION.SDK_INT >= 11) {
                        talks_recyclerView.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
                            @Override
                            public void onLayoutChange(View v,
                                                       int left, int top, int right, int bottom,
                                                       int oldLeft, int oldTop, int oldRight, int oldBottom) {
                                if (bottom < oldBottom) {
                                    talks_recyclerView.post(new Runnable() {
                                        @Override
                                        public void run() {
                                            talks_recyclerView.smoothScrollToPosition(
                                                    talks_recyclerView.getAdapter().getItemCount() - 1);
                                        }
                                    });
                                }
                            }
                        });
                    }
                }

                @Override
                public void onViewDetachedFromWindow(View view) {

                }
            });*/
            myMsg.setOnFocusChangeListener(new View.OnFocusChangeListener() {
                @Override
                public void onFocusChange(View view, boolean b) {
                    if(b){
                        if (Build.VERSION.SDK_INT >= 11) {
                            talks_recyclerView.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
                                @Override
                                public void onLayoutChange(View v,
                                                           int left, int top, int right, int bottom,
                                                           int oldLeft, int oldTop, int oldRight, int oldBottom) {
                                    if (bottom < oldBottom) {
                                        talks_recyclerView.post(new Runnable() {
                                            @Override
                                            public void run() {
                                                talks_recyclerView.smoothScrollToPosition(
                                                        talks_recyclerView.getAdapter().getItemCount() - 1);
                                            }
                                        });
                                    }
                                }
                            });
                        }
                        //==========================================================================================================
                        //==========================================================================================================
                        //==========================================================================================================
                                    mUnread = FirebaseDatabase.getInstance().getReference();
                                    mUnread = mUnread.child(MyConstants.MESSAGES_TALKS).child(currentUser).child(mUserid);

                                    mUnread.addChildEventListener(new ChildEventListener() {
                                        @Override
                                        public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

                                            if(dataSnapshot.hasChildren()){
                                                if(dataSnapshot.hasChild("is_seen")) {
                                                    boolean isFalse = (boolean) dataSnapshot.child("is_seen").getValue();
                                                    if (!isFalse) {
                                                        mUnread.child(dataSnapshot.getKey()).child("is_seen").setValue(true);
                                                        Log.d("value--->", "called");


                                                        Map is_seen = new HashMap();
                                                        is_seen.put("is_seen", true);
                                                        Log.d("key_key", dataSnapshot.getKey());
                                                        Log.d("value1--->", dataSnapshot.child("is_seen").getValue().toString());

                                                    } else {
                                                        Log.d("value--->", dataSnapshot.getKey() + " " + dataSnapshot.child("is_seen").getValue().toString());
                                                    }
                                                }


                                            }
                  /*  Map is_seen=new HashMap();
                    is_seen.put("is_seen",true);
                    Log.d("key_key",dataSnapshot.getKey());
                    Log.d("value1--->",dataSnapshot.child("is_seen").getValue().toString());
                    //===============================================
                    mUnread.child(dataSnapshot.getKey()).addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                           // dataSnapshot.child("is_seen").getValue();

                            Log.d("value2---> ",dataSnapshot.child("is_seen").getValue().toString());
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });*/
                                            //===============================================




                  /*  mUnread.child(dataSnapshot.getKey()).updateChildren(is_seen, new DatabaseReference.CompletionListener() {
                        @Override
                        public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {
                          if(databaseError==null){
                              Toast.makeText(TalksActivity.this, " success ", Toast.LENGTH_SHORT).show();
                          }else{
                              Log.d("error",databaseError.getMessage());
                          }

                        }
                    });*/
                                        }

                                        @Override
                                        public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

                                        }

                                        @Override
                                        public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

                                        }

                                        @Override
                                        public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

                                        }

                                        @Override
                                        public void onCancelled(@NonNull DatabaseError databaseError) {

                                        }
                                    });


                        mUnread = FirebaseDatabase.getInstance().getReference();
                        mUnread = mUnread.child(MyConstants.MESSAGES_TALKS).child(mUserid).child(currentUser);
                        mUnread.addChildEventListener(new ChildEventListener() {
                            @Override
                            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

                                if(dataSnapshot.hasChildren()){
                                    boolean isFalse= (boolean) dataSnapshot.child("is_seen").getValue();
                                    if (!isFalse) {
                                      //  mUnread.child(dataSnapshot.getKey()).child("is_seen").setValue(false);
                                        Log.d("value--->", "called");


                                    /*    Map is_seen=new HashMap();
                                        is_seen.put("is_seen",true);
                                        Log.d("key_key",dataSnapshot.getKey());
                                        Log.d("value1--->",dataSnapshot.child("is_seen").getValue().toString());*/

                                    } else {
                                        Log.d("value--->", dataSnapshot.getKey() + " " + dataSnapshot.child("is_seen").getValue().toString());
                                    }


                                }
                  /*  Map is_seen=new HashMap();
                    is_seen.put("is_seen",true);
                    Log.d("key_key",dataSnapshot.getKey());
                    Log.d("value1--->",dataSnapshot.child("is_seen").getValue().toString());
                    //===============================================
                    mUnread.child(dataSnapshot.getKey()).addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                           // dataSnapshot.child("is_seen").getValue();

                            Log.d("value2---> ",dataSnapshot.child("is_seen").getValue().toString());
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });*/
                                //===============================================




                  /*  mUnread.child(dataSnapshot.getKey()).updateChildren(is_seen, new DatabaseReference.CompletionListener() {
                        @Override
                        public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {
                          if(databaseError==null){
                              Toast.makeText(TalksActivity.this, " success ", Toast.LENGTH_SHORT).show();
                          }else{
                              Log.d("error",databaseError.getMessage());
                          }

                        }
                    });*/
                            }

                            @Override
                            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

                            }

                            @Override
                            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

                            }

                            @Override
                            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        });




                        //==========================================================================================================
                        //==========================================================================================================
                    }else{

                    }
                }
            });



        }
        else {
            Toast.makeText(TalksActivity.this,getString(R.string.no_user), Toast.LENGTH_SHORT).show();
            gotoStartActivity();

        }

    }

    /**
     * The time (in ms) interval to update the countdown TextView.
     */
    private static final int COUNTDOWN_UPDATE_INTERVAL = 500;

    private Handler countdownHandler;

    /**
     * Stops the  countdown timer.
     */
    private void stopCountdown() {
        if (countdownHandler != null) {
            countdownHandler.removeCallbacks(updateCountdown);
            countdownHandler = null;
        }
    }

    /**
     * (Optionally stops) and starts the countdown timer.
     */
    private void startCountdown() {
        stopCountdown();

        countdownHandler = new Handler();
        updateCountdown.run();
    }

    /**
     * Updates the countdown.
     */
    private Runnable updateCountdown = new Runnable() {
        @Override
        public void run() {
            try {
                if(time!=null)
                lastSeenView.setText(TimeAgo.getTimeAgo(time,getApplicationContext()));
            } finally {
                countdownHandler.postDelayed(updateCountdown, COUNTDOWN_UPDATE_INTERVAL);
            }
        }
    };

    private void gotoStartActivity() {
        Intent intent = new Intent(TalksActivity.this, StartActivity.class);
        startActivity(intent);
    }

    private void loadChats() {

        if(userImage!=null) {
            if (!userImage.matches("null")) {
                Picasso.with(TalksActivity.this).load(userImage).networkPolicy(NetworkPolicy.OFFLINE).into(userImageToolbar, new Callback() {
                    @Override
                    public void onSuccess() {
                        /// success in ofline mode
                    }

                    @Override
                    public void onError() {
                        Picasso.with(TalksActivity.this).load(userImage).into(userImageToolbar);
                    }
                });
            }
        }

        Query queryForPaginatio=mDataBaseRef.child(MESSAGES_TALKS).child(currentUser).child(mUserid).limitToLast(mCurrentPage*TOTAL_MSGS_TO_SHOW);

        queryForPaginatio.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                TalksMessages talksMessages = dataSnapshot.getValue(TalksMessages.class);
                talksMessagesList.add(talksMessages);

                talksMsgAdapter.notifyDataSetChanged();
                layoutManager.scrollToPosition(talksMessagesList.size() - 1);
                talks_recyclerView.scrollToPosition(talksMessagesList.size() - 1);

                talks_recyclerView.smoothScrollToPosition(talksMessagesList.size() - 1);
                layoutManager.smoothScrollToPosition(talks_recyclerView,null,talksMessagesList.size() - 1);

                swipeRefreshLayout.setRefreshing(false);


            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        btnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String msg = myMsg.getText().toString();
                if (!TextUtils.isEmpty(msg)) {
                    String currentUser_ref = MESSAGES_TALKS1 + currentUser + "/" + mUserid;
                    String Otheruser_ref = MESSAGES_TALKS1 + mUserid + "/" + currentUser;
                    DatabaseReference mDatabaseRefPush = mDataBaseRef.child(MESSAGES_TALKS).child(currentUser).child(mUserid).push();

                    //==================================================================================================
                    DatabaseReference notificationRef = mDataBaseRef.child(MyConstants.NOYIFICATION).child(mUserid).push();
                    String notificationID = notificationRef.getKey();

                    HashMap messageMap = new HashMap();
                    messageMap.put(MESSAGE, msg);
                    messageMap.put(IS_SEEN, false);
                    messageMap.put(TYPE, TEXT);
                    messageMap.put(TIME, ServerValue.TIMESTAMP);
                    messageMap.put(FROM, currentUser);
                    //============== for message notification=====================
                    HashMap messageNotification=new HashMap();
                    messageNotification.put("from", currentUser);
                    messageNotification.put("type", "message");
                    messageNotification.put("time", String.valueOf(Calendar.getInstance().getTimeInMillis()));
                    messageNotification.put("msg",msg);

                    HashMap messageMap2 = new HashMap();
                    messageMap2.put(currentUser_ref + "/" + mDatabaseRefPush.getKey(), messageMap);
                    messageMap2.put(Otheruser_ref + "/" + mDatabaseRefPush.getKey(), messageMap);
                    messageMap2.put(MyConstants.NOYIFICATION + "/" + mUserid + "/" + notificationID, messageNotification);

                    mDataBaseRef.child(CHAT).child(mUserid).child(currentUser).child(IS_SEEN).setValue("false");
                    mDataBaseRef.child(CHAT).child(mUserid).child(currentUser).child(TIME).setValue(ServerValue.TIMESTAMP);

                    mDataBaseRef.updateChildren(messageMap2, new DatabaseReference.CompletionListener() {
                        @Override
                        public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                            if (databaseError != null) {
                                Log.e(TalksActivity.class.getName(), databaseError.getMessage());

                            }
                            myMsg.setText("");
                        }
                    });
                }


            }
        });


    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mAuth.getCurrentUser() != null) {
            mDataBaseRef.child(MyConstants.USERS).child(mUserid).child("online").setValue("false");
            if(thread!=null&&!thread.isInterrupted()) {
                thread.interrupt();
                Log.d("isIntrupted?","is intrupt");
            }
        }
    }
}
