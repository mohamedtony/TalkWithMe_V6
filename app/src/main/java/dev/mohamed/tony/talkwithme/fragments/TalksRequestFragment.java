package dev.mohamed.tony.talkwithme.fragments;


import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import dev.mohamed.tony.talkwithme.R;
import dev.mohamed.tony.talkwithme.RecyclerViewEmptySupport;
import dev.mohamed.tony.talkwithme.activities.StartActivity;
import dev.mohamed.tony.talkwithme.adapters.FirebaseContactsAdapter;
import dev.mohamed.tony.talkwithme.adapters.FirebaseRequestAdapter;
import dev.mohamed.tony.talkwithme.adapters.TalkRequestAdapter;

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
import java.util.List;

import dev.mohamed.tony.talkwithme.MyConstants;
import dev.mohamed.tony.talkwithme.models.Contact;
import dev.mohamed.tony.talkwithme.models.TalkRequests;

/**
 * A simple {@link Fragment} subclass.
 */
public class TalksRequestFragment extends Fragment {


    private static Bundle mBundleRecyclerViewState3 = null;
    private final String KEY_RECYCLER_STATE3 = MyConstants.RECYCLER_STATE3;
    private DatabaseReference mRequestListDataBase;
    private FirebaseAuth firebaseAuth;
    private String mCurrentUserId;
    private List<String> usersKeyList;
    private RecyclerViewEmptySupport recyclerView;
    private LinearLayoutManager linearLayoutManager;
    private TalkRequestAdapter talkRequestAdapter;
    private FirebaseRequestAdapter firebaseRequestAdapter;
    private DatabaseReference mFireBaseUsersDatabase;
    private  FirebaseUser mUser;

    public TalksRequestFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_talks_request, container, false);

        usersKeyList = new ArrayList<>();

        recyclerView = (RecyclerViewEmptySupport) view.findViewById(R.id.requests_list);
        recyclerView.setHasFixedSize(true);
        linearLayoutManager = new LinearLayoutManager(getContext());
        recyclerView.setEmptyView(view.findViewById(R.id.empty_view_request));
        recyclerView.setLayoutManager(linearLayoutManager);

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();


        firebaseAuth = FirebaseAuth.getInstance();
         mUser = firebaseAuth.getCurrentUser();
        if (mUser != null) {
            mCurrentUserId = firebaseAuth.getCurrentUser().getUid();
            mRequestListDataBase = FirebaseDatabase.getInstance().getReference().child("Talks_Requesta").child(mCurrentUserId);
            mFireBaseUsersDatabase = FirebaseDatabase.getInstance().getReference().child(MyConstants.USERS);
            mRequestListDataBase.keepSynced(true);
            mFireBaseUsersDatabase.keepSynced(true);
            loadTalkRequest2();

            firebaseRequestAdapter.startListening();
        } else {
            Toast.makeText(getContext(), getString(R.string.no_user), Toast.LENGTH_SHORT).show();
            gotoStartActivity();
        }

    }

    @Override
    public void onStop() {
        super.onStop();
        if(mUser!=null){
            firebaseRequestAdapter.stopListening();
        }
    }

    private void loadTalkRequest2() {

       Query query=mRequestListDataBase.orderByChild("request_type").equalTo("recieved");
        Log.d("bbb",mRequestListDataBase.getRef().getKey().toString());

        FirebaseRecyclerOptions personsOptions = new FirebaseRecyclerOptions.Builder<TalkRequests>().setQuery(query, TalkRequests.class).build();
        firebaseRequestAdapter=
                new FirebaseRequestAdapter(personsOptions,mFireBaseUsersDatabase,getContext());
        recyclerView.setAdapter(firebaseRequestAdapter);
    }


    private void gotoStartActivity() {
        Intent intent = new Intent(getContext(), StartActivity.class);
        startActivity(intent);
    }

    @Override
    public void onResume() {
        super.onResume();

        // restore RecyclerView state
        if (mBundleRecyclerViewState3 != null) {
            if(recyclerView!=null) {
                Parcelable listState1 = mBundleRecyclerViewState3.getParcelable(KEY_RECYCLER_STATE3);
                recyclerView.getLayoutManager().onRestoreInstanceState(listState1);

                mBundleRecyclerViewState3 = null;
            }
        }
    }

    @Override
    public void onSaveInstanceState(Bundle state) {
        super.onSaveInstanceState(state);

        mBundleRecyclerViewState3 = new Bundle();
        if(recyclerView!=null) {
            Parcelable listState1 = recyclerView.getLayoutManager().onSaveInstanceState();
            mBundleRecyclerViewState3.putParcelable(KEY_RECYCLER_STATE3, listState1);
        }

    }


    private void loadTalkRequest() {
        usersKeyList = new ArrayList<>();
        mRequestListDataBase.child(mCurrentUserId).addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {

                if (dataSnapshot.hasChildren()) {

                    if (dataSnapshot.hasChild(MyConstants.REQUEST_TYPE)) {
                        String requestType = dataSnapshot.child(MyConstants.REQUEST_TYPE).getValue().toString();
                        if (requestType.matches(MyConstants.RECIEVED)) {
                            String userId = dataSnapshot.getKey();
                            Log.d(MyConstants.MESSAGE, userId);
                            usersKeyList.add(userId);
                            talkRequestAdapter = new TalkRequestAdapter(getContext(), usersKeyList);
                            recyclerView.setAdapter(talkRequestAdapter);

                        }

                    }

                }
                // }
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
