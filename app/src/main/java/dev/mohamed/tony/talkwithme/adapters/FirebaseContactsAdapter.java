package dev.mohamed.tony.talkwithme.adapters;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import dev.mohamed.tony.talkwithme.MyConstants;
import dev.mohamed.tony.talkwithme.R;
import dev.mohamed.tony.talkwithme.activities.UserProfileActivity;
import dev.mohamed.tony.talkwithme.models.Contact;
import dev.mohamed.tony.talkwithme.models.User;

public class FirebaseContactsAdapter extends FirebaseRecyclerAdapter<Contact, FirebaseContactsAdapter.ContactsViewHolder> {


    private Context mContext;
    private DatabaseReference users;

    /**
     * Initialize a {@link RecyclerView.Adapter} that listens to a Firebase query. See
     * {@link FirebaseRecyclerOptions} for configuration options.
     *
     * @param options
     */
    public FirebaseContactsAdapter(FirebaseRecyclerOptions<Contact> options, DatabaseReference users, Context mContext) {
        super(options);
        this.mContext = mContext;
        // ========== users(contacts list)=============
        this.users = users;
    }


    @Override
    protected void onBindViewHolder(final ContactsViewHolder viewHolder, int position, Contact model) {
        //============== contact user id ==============
        String user_id = getRef(position).getKey();
        // ============= date for every contact ============
        viewHolder.date_freinds.setText(model.getDate());

        //============= for each user(contact id)_id there are values (name,image,online)
        if(user_id!=null) {

            users.child(user_id).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if(dataSnapshot.hasChildren()) {
                        //========== each value considers as user============
                        final User user = dataSnapshot.getValue(User.class);

                        //========= set contact name to the TextView================
                        viewHolder.username.setText(user.getName());
                        //=============== Contact Status========================
                        if (dataSnapshot.hasChild("online")) {
                            if (user.getOnline().equals("true")) {
                                viewHolder.icOnline.setVisibility(View.VISIBLE);
                            } else {
                                viewHolder.icOnline.setVisibility(View.INVISIBLE);
                            }
                        }
                        //============== contact profile image ==================
                        if (!TextUtils.isEmpty(user.getImage()) && !user.getImage().equals("null")) {
                            Picasso.with(mContext).load(user.getImage()).networkPolicy(NetworkPolicy.OFFLINE).into(viewHolder.userImage, new Callback() {
                                @Override
                                public void onSuccess() {
                                    /// success in ofline mode
                                }

                                @Override
                                public void onError() {
                                    Picasso.with(mContext).load(user.getImage()).networkPolicy(NetworkPolicy.OFFLINE).into(viewHolder.userImage);
                                }
                            });
                        }
                    }

                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }
        //}
    }

    @NonNull
    @Override
    public ContactsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.contact_recycler_item, parent, false);
        return new FirebaseContactsAdapter.ContactsViewHolder(view);
    }

    public class ContactsViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        ImageView userImage, icOnline;
        TextView date_freinds, username;

        public ContactsViewHolder(View itemView) {
            super(itemView);
            userImage = (ImageView) itemView.findViewById(R.id.contact_image);
            date_freinds = (TextView) itemView.findViewById(R.id.date_freinds);
            username = (TextView) itemView.findViewById(R.id.contat_name);
            icOnline = (ImageView) itemView.findViewById(R.id.ic_online_icon);
             itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            int pos = getAdapterPosition();

            Intent intent = new Intent(mContext, UserProfileActivity.class);
            intent.putExtra(MyConstants.UID, getRef(pos).getKey());
            mContext.startActivity(intent);
        }

    }
}
