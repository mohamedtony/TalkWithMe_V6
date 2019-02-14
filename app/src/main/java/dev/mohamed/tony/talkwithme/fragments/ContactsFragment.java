package dev.mohamed.tony.talkwithme.fragments;


import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;

import dev.mohamed.tony.talkwithme.MyConstants;
import dev.mohamed.tony.talkwithme.R;
import dev.mohamed.tony.talkwithme.RecyclerViewEmptySupport;
import dev.mohamed.tony.talkwithme.activities.StartActivity;
import dev.mohamed.tony.talkwithme.adapters.ContactsAdapter;
import dev.mohamed.tony.talkwithme.adapters.FirebaseContactsAdapter;
import dev.mohamed.tony.talkwithme.models.Contact;
import dev.mohamed.tony.talkwithme.models.User;

/**
 * A simple {@link Fragment} subclass.
 */
public class ContactsFragment extends Fragment {

    private static Bundle mBundleRecyclerViewState1 = null;
    private final String KEY_RECYCLER_STATE1 = MyConstants.RECYCLER_STATE1;
    private DatabaseReference mContactListDataBase, mFireBaseUsersDatabase;
    private FirebaseAuth mAuth;
    private String currentUser;
    private RecyclerView.LayoutManager layoutManager;
    private ContactsAdapter recyclerAdapter;
    private ArrayList<User> usersList;
    private ArrayList<String> usersKeyList;
    private ArrayList<String> usersDateList;
    private RecyclerViewEmptySupport contactList;
    private User user;
    private  FirebaseContactsAdapter firebaseContactsAdapter;
    private FirebaseUser mUser;


    public ContactsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_contacts, container, false);

        contactList = (RecyclerViewEmptySupport) rootView.findViewById(R.id.contacts_list);
        layoutManager = new LinearLayoutManager(getContext());
        contactList.setEmptyView(rootView.findViewById(R.id.empty_view_contact));
        contactList.setHasFixedSize(true);
        contactList.setLayoutManager(layoutManager);

        return rootView;
    }

    @Override
    public void onStart() {
        super.onStart();

        mAuth = FirebaseAuth.getInstance();
         mUser = FirebaseAuth.getInstance().getCurrentUser();
        if (mUser != null) {
            currentUser = mAuth.getCurrentUser().getUid();
            Log.d("current_uid",currentUser);
            mContactListDataBase = FirebaseDatabase.getInstance().getReference().child(MyConstants.CONTACTS).child(currentUser);
            mContactListDataBase.keepSynced(true);
            mFireBaseUsersDatabase = FirebaseDatabase.getInstance().getReference().child(MyConstants.USERS);
            mFireBaseUsersDatabase.keepSynced(true);
            readContactList();

            firebaseContactsAdapter.startListening();

        } else {
            Toast.makeText(getContext(), getString(R.string.no_user), Toast.LENGTH_SHORT).show();
            gotoStartActivity();
        }


    }

    @Override
    public void onStop() {
        super.onStop();
        if(mUser!=null) {
            firebaseContactsAdapter.stopListening();
        }
    }

    private void readContactList() {
        //Query query=mContactListDataBase.();
        FirebaseRecyclerOptions personsOptions = new FirebaseRecyclerOptions.Builder<Contact>().setQuery(mContactListDataBase, Contact.class).build();
         firebaseContactsAdapter=
                new FirebaseContactsAdapter(personsOptions,mFireBaseUsersDatabase,getContext());
        contactList.setAdapter(firebaseContactsAdapter);
    }

    private void gotoStartActivity() {
        Intent intent = new Intent(getContext(), StartActivity.class);
        startActivity(intent);
    }

  /*  @Override
    public void onResume() {
        super.onResume();

        // restore RecyclerView state
        if (mBundleRecyclerViewState1 != null) {
            if(contactList!=null) {
                Parcelable listState1 = mBundleRecyclerViewState1.getParcelable(KEY_RECYCLER_STATE1);
                contactList.getLayoutManager().onRestoreInstanceState(listState1);

                mBundleRecyclerViewState1 = null;
            }
        }
    }

    @Override
    public void onSaveInstanceState(Bundle state) {
        super.onSaveInstanceState(state);

        mBundleRecyclerViewState1 = new Bundle();
        if(contactList!=null) {
            Parcelable listState1 = contactList.getLayoutManager().onSaveInstanceState();
            mBundleRecyclerViewState1.putParcelable(KEY_RECYCLER_STATE1, listState1);
        }

    }
*/
}
