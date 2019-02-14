package dev.mohamed.tony.talkwithme.activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ProgressBar;

import dev.mohamed.tony.talkwithme.MyConstants;
import dev.mohamed.tony.talkwithme.R;
import dev.mohamed.tony.talkwithme.adapters.RecyclerAdapter;
import dev.mohamed.tony.talkwithme.models.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

import static dev.mohamed.tony.talkwithme.MyConstants.RECYCLER_STATE;
import static dev.mohamed.tony.talkwithme.MyConstants.USERS;

public class UsersActivity extends AppCompatActivity implements RecyclerAdapter.OnItemClicked {

    private static Bundle mBundleRecyclerViewState = null;
    private final String KEY_RECYCLER_STATE = RECYCLER_STATE;
    public Parcelable listState;
    @BindView(R.id.toolbar_usersActivity)
    Toolbar mToolbar;
    @BindView(R.id.recyclerUsers)
    RecyclerView mRecyclerViewUsers;
    @BindView((R.id.progressBar))
    ProgressBar progressBar;
    private RecyclerView.LayoutManager layoutManager;
    private RecyclerAdapter recyclerAdapter;
    private List<User> usersList;
    private List<String> usersKeyList;
    private DatabaseReference mFireBaseDatabase;
    private FirebaseUser currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_users);
        ButterKnife.bind(this);

        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle(getString(R.string.all_users));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        progressBar.setVisibility(View.VISIBLE);

        layoutManager = new LinearLayoutManager(this);
        mRecyclerViewUsers.setHasFixedSize(true);
        mRecyclerViewUsers.setLayoutManager(layoutManager);
    }

    @Override
    protected void onStart() {
        super.onStart();
        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            mFireBaseDatabase = FirebaseDatabase.getInstance().getReference().child(USERS);
            mFireBaseDatabase.keepSynced(true);
            readListData();
            mFireBaseDatabase.child(currentUser.getUid()).child("online").setValue("true");
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        // restore RecyclerView state
        if (mBundleRecyclerViewState != null) {
            if (mRecyclerViewUsers!=null) {
                Parcelable listState = mBundleRecyclerViewState.getParcelable(KEY_RECYCLER_STATE);
                mRecyclerViewUsers.getLayoutManager().onRestoreInstanceState(listState);

                mBundleRecyclerViewState = null;
            }
        }
    }

    @Override
    public void onSaveInstanceState(Bundle state) {
        super.onSaveInstanceState(state);

        mBundleRecyclerViewState = new Bundle();
        if (mRecyclerViewUsers!=null) {
            Parcelable listState = mRecyclerViewUsers.getLayoutManager().onSaveInstanceState();
            mBundleRecyclerViewState.putParcelable(KEY_RECYCLER_STATE, listState);
        }

    }

    private void readListData() {
        usersList = new ArrayList<>();
        usersKeyList = new ArrayList<>();
        ChildEventListener childEventListener = new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String previousChildName) {
                User user = dataSnapshot.getValue(User.class);

                usersKeyList.add(dataSnapshot.getKey());
                usersList.add(user);
                recyclerAdapter = new RecyclerAdapter(UsersActivity.this, UsersActivity.this, usersList, usersKeyList);
                mRecyclerViewUsers.setAdapter(recyclerAdapter);

                progressBar.setVisibility(View.INVISIBLE);
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String previousChildName) {
                User user = dataSnapshot.getValue(User.class);
                String commentKey = dataSnapshot.getKey();
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
                User user = dataSnapshot.getValue(User.class);
                String commentKey = dataSnapshot.getKey();
            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String previousChildName) {
                User user = dataSnapshot.getValue(User.class);
                String commentKey = dataSnapshot.getKey();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                progressBar.setVisibility(View.INVISIBLE);
                //  Toast.makeText(UsersActivity.this, "Failed to load comments.",
                //       Toast.LENGTH_SHORT).show();
            }
        };
        mFireBaseDatabase.addChildEventListener(childEventListener);


    }

    @Override
    public void onItemClickedLisener(final String uid) {
        //  Toast.makeText(this, " item " + uid + " is clicked ", Toast.LENGTH_SHORT).show();

        if (currentUser.getUid().equals(uid)) {
            Intent intent = new Intent(UsersActivity.this, ProfileSettingsActivity.class);
            startActivity(intent);
        } else {
            Intent intent = new Intent(UsersActivity.this, UserProfileActivity.class);
            intent.putExtra(MyConstants.UID, uid);
            startActivity(intent);
        }

    }

  @Override
    protected void onPause() {
      super.onPause();
      if (currentUser != null) {
          mFireBaseDatabase.child(currentUser.getUid()).child("online").setValue("false");
      }
  }
}
