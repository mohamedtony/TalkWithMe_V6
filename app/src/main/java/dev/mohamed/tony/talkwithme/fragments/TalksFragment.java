package dev.mohamed.tony.talkwithme.fragments;


import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import dev.mohamed.tony.talkwithme.R;
import dev.mohamed.tony.talkwithme.RecyclerViewEmptySupport;
import dev.mohamed.tony.talkwithme.activities.StartActivity;
import dev.mohamed.tony.talkwithme.adapters.FirebaseChatsFragAdapter;
import dev.mohamed.tony.talkwithme.adapters.FirebaseContactsAdapter;
import dev.mohamed.tony.talkwithme.adapters.TalksChatAdapter;
import dev.mohamed.tony.talkwithme.models.Contact;
import dev.mohamed.tony.talkwithme.models.Talk;

import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;

import java.util.ArrayList;

import static dev.mohamed.tony.talkwithme.MyConstants.CHAT;
import static dev.mohamed.tony.talkwithme.MyConstants.MESSAGES_TALKS;
import static dev.mohamed.tony.talkwithme.MyConstants.RECYCLER_STATE2;
import static dev.mohamed.tony.talkwithme.MyConstants.TIME;
import static dev.mohamed.tony.talkwithme.MyConstants.USERS;

/**
 * A simple {@link Fragment} subclass.
 */

public class TalksFragment extends Fragment {


    private RecyclerView mChatList;

    private DatabaseReference mChatDatabase;
    private DatabaseReference mMessageDatabase;
    private DatabaseReference mUsersDatabase;
    private FirebaseAuth mAuth;
    private String mCurrent_user_id;
    private LinearLayoutManager layoutManager;
    private RecyclerViewEmptySupport talksList_recycler;
    private ArrayList<String> usersKeyList;
    private TalksChatAdapter talksChatAdapter;
    private static Bundle mBundleRecyclerViewState2 = null;
    private final String KEY_RECYCLER_STATE2 = RECYCLER_STATE2;
    private FirebaseChatsFragAdapter firebaseChatsFragAdapter;

    public TalksFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_talks, container, false);

        // ButterKnife.bind(getActivity());

        talksList_recycler = (RecyclerViewEmptySupport) view.findViewById(R.id.talksList_recycler);
        layoutManager = new LinearLayoutManager(getContext());
        talksList_recycler.setEmptyView(view.findViewById(R.id.empty_view_talks));
        talksList_recycler.setHasFixedSize(true);
        talksList_recycler.setLayoutManager(layoutManager);




        return view;
    }

    @Override
    public void onStart() {
        super.onStart();

        mAuth = FirebaseAuth.getInstance();
        FirebaseUser mUser=FirebaseAuth.getInstance().getCurrentUser();
        if (mUser != null) {
            mCurrent_user_id = mAuth.getCurrentUser().getUid();
            mChatDatabase = FirebaseDatabase.getInstance().getReference().child(CHAT).child(mCurrent_user_id);
            mChatDatabase.keepSynced(true);
            mUsersDatabase = FirebaseDatabase.getInstance().getReference().child(USERS);
            mMessageDatabase = FirebaseDatabase.getInstance().getReference().child(MESSAGES_TALKS).child(mCurrent_user_id);
            mUsersDatabase.keepSynced(true);
            usersKeyList = new ArrayList<>();
            loadChats2();
            firebaseChatsFragAdapter.startListening();
        }
       else {
            Toast.makeText(getContext(),getString(R.string.no_user), Toast.LENGTH_SHORT).show();
            gotoStartActivity();

        }

    }

    @Override
    public void onStop() {
        super.onStop();
        if(mAuth.getCurrentUser()!=null){
            firebaseChatsFragAdapter.stopListening();
        }
    }

    private void loadChats2() {
        Query conversationQuery = mChatDatabase.orderByChild(TIME);
        FirebaseRecyclerOptions personsOptions = new FirebaseRecyclerOptions.Builder<Talk>().setQuery(conversationQuery, Talk.class).build();
        firebaseChatsFragAdapter=
                new FirebaseChatsFragAdapter(personsOptions,getContext());
        talksList_recycler.setAdapter(firebaseChatsFragAdapter);

    }

    private void gotoStartActivity() {
        Intent intent = new Intent(getContext(), StartActivity.class);
        startActivity(intent);
    }

    @Override
    public void onResume() {
        super.onResume();

        // restore RecyclerView state
        if (mBundleRecyclerViewState2 != null) {
            if(talksList_recycler!=null) {
                Parcelable listState1 = mBundleRecyclerViewState2.getParcelable(KEY_RECYCLER_STATE2);
                talksList_recycler.getLayoutManager().onRestoreInstanceState(listState1);

                mBundleRecyclerViewState2 = null;
            }
        }
    }

    @Override
    public void onSaveInstanceState(Bundle state) {
        super.onSaveInstanceState(state);

        mBundleRecyclerViewState2 = new Bundle();
        if(talksList_recycler!=null) {
            Parcelable listState1 = talksList_recycler.getLayoutManager().onSaveInstanceState();
            mBundleRecyclerViewState2.putParcelable(KEY_RECYCLER_STATE2, listState1);
        }

    }


    private void loadChats() {


        Query conversationQuery = mChatDatabase.orderByChild(TIME);
        conversationQuery.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {

                final Talk talk = dataSnapshot.getValue(Talk.class);

                final String userChat_id = dataSnapshot.getKey();
                usersKeyList.add(userChat_id);
                talksChatAdapter = new TalksChatAdapter(getContext(), usersKeyList);
                talksList_recycler.setAdapter(talksChatAdapter);

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


    }
}


