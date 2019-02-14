package dev.mohamed.tony.talkwithme.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import dev.mohamed.tony.talkwithme.R;
import dev.mohamed.tony.talkwithme.adapters.ViewPagerAdapter;
import dev.mohamed.tony.talkwithme.fragments.ContactsFragment;
import dev.mohamed.tony.talkwithme.fragments.TalksFragment;
import dev.mohamed.tony.talkwithme.fragments.TalksRequestFragment;

import com.facebook.login.LoginManager;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Calendar;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MainActivity extends AppCompatActivity {
    @BindView(R.id.toolbar_mainactivity)
    Toolbar toolbar;

    @BindView(R.id.myViewPager)
    ViewPager viewPager;

    @BindView(R.id.myTablayout)
    TabLayout tabLayout;


    private FirebaseAuth mAuth;
    private DatabaseReference mUsersRefs;
    private DatabaseReference mUnreadChats;
    public int cout=0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(R.string.app_name);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        /// for the view pager
        setupViewPager(viewPager);
        tabLayout.setupWithViewPager(viewPager);
        mAuth = FirebaseAuth.getInstance();
        if(mAuth.getCurrentUser()!=null) {
            mUsersRefs = FirebaseDatabase.getInstance().getReference().child("Users").child(mAuth.getCurrentUser().getUid());
            mUnreadChats=FirebaseDatabase.getInstance().getReference().child("messages_talks").child(mAuth.getCurrentUser().getUid());
            mUnreadChats.addChildEventListener(new ChildEventListener() {
                @Override
                public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

                    cout=0;
                    if(dataSnapshot.hasChildren()){

                        Query mUnreadChatsNum=mUnreadChats.child(dataSnapshot.getKey()).orderByChild("is_seen").equalTo(false);

                       mUnreadChatsNum.addChildEventListener(new ChildEventListener() {
                                                                 @Override
                                                                 public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                                                                     cout++;
                                                                     Log.d("users_key", String.valueOf(cout));
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

                       //cout=0;
                    }
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
        }


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        super.onOptionsItemSelected(item);

        if (item.getItemId() == android.R.id.home) {
            finish();
        } else if (item.getItemId() ==R.id.log_out) {
            mAuth.signOut();
            // FirebaseAuth.getInstance().signOut();
            mUsersRefs.child("online").setValue("false");
            // Google sign out
            GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                    .requestIdToken(getString(R.string.default_web_client_id))
                    .requestEmail()
                    .build();
            // Build a GoogleSignInClient with the options specified by gso.
           GoogleSignInClient mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
            mGoogleSignInClient.signOut().addOnCompleteListener(this,
                    new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful())
                               gotoStartActivity();
                        }
                    });
            if(LoginManager.getInstance()!=null) {
                LoginManager.getInstance().logOut();
                gotoStartActivity();
            }


            finish();
        } else if (item.getItemId() == R.id.account_setting) {
            Intent intent = new Intent(MainActivity.this, ProfileSettingsActivity.class);
            startActivity(intent);
        } else if (item.getItemId() == R.id.all_user) {
            Intent intent = new Intent(MainActivity.this, UsersActivity.class);
            startActivity(intent);
        }else if((item.getItemId() == R.id.about_me)){
            Intent intent = new Intent(MainActivity.this, AboutMe.class);
            startActivity(intent);
        }
        return true;
    }

    private void setupViewPager(ViewPager viewPager) {
        ViewPagerAdapter adapter = new ViewPagerAdapter(getSupportFragmentManager());



        adapter.addFragment(new TalksFragment(), getString(R.string.talks));
        adapter.addFragment(new ContactsFragment(), getString(R.string.contacts));
        adapter.addFragment(new TalksRequestFragment(), getString(R.string.talks_request));
        viewPager.setAdapter(adapter);
    }

    private void gotoStartActivity() {
        Intent intent = new Intent(MainActivity.this, StartActivity.class);
        startActivity(intent);
    }

    @Override
    public void onResume() {
        super.onResume();


        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = null;
        if(mAuth!=null) {
             currentUser = mAuth.getCurrentUser();
             if(currentUser!=null) {
               //  mUsersRefs.child("online").setValue("true");
             }
        }
        if (currentUser == null) {
            Toast.makeText(this,getString(R.string.no_user), Toast.LENGTH_SHORT).show();
            gotoStartActivity();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mAuth.getCurrentUser() != null) {
           // mUsersRefs.child("online").setValue("false");

            Calendar time=Calendar.getInstance();
            SimpleDateFormat currentTimeFormat=new SimpleDateFormat("hh:mm a");
            String currentTime=currentTimeFormat.format(time.getTime());

          //  mUsersRefs.child("lastSeen").setValue(ServerValue.TIMESTAMP);
        }
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (!hasFocus) {
            Log.i("key_pressed", "not_changed");
        }
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        Log.i("key_pressed", String.valueOf(event.getKeyCode()));
        return super.dispatchKeyEvent(event);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_MENU) {
            //do your work
            Log.d("menu_button","kelicked");
            return true;
        }
        return super.onKeyDown(keyCode, event);

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mAuth.getCurrentUser() != null) {
            //mUsersRefs.child("online").setValue("false");
            /*Calendar time=Calendar.getInstance();
            SimpleDateFormat currentTimeFormat=new SimpleDateFormat("hh:mm a");
            String currentTime=currentTimeFormat.format(time.getTime());*/

            if(mUsersRefs!=null)
            mUsersRefs.child("lastSeen").setValue(ServerValue.TIMESTAMP);
        }
    }
}
