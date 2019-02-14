package dev.mohamed.tony.talkwithme.adapters;

import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import dev.mohamed.tony.talkwithme.MyConstants;
import dev.mohamed.tony.talkwithme.R;
import dev.mohamed.tony.talkwithme.activities.TalksActivity;
import dev.mohamed.tony.talkwithme.models.User;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;


/**
 * Created by Tony on 1/25/2018.
 */

public class TalksChatAdapter extends RecyclerView.Adapter<TalksChatAdapter.TalksChatViewHolder> {

    private Context mContext;
    private ArrayList<User> userListChat;
    private ArrayList<String> usersKeyList;
    private DatabaseReference mChatDatabase;
    private DatabaseReference mMessageDatabase;
    private DatabaseReference mUsersDatabase;
    private FirebaseAuth mAuth;
    private String mCurrent_user_id;
    private String name;
    private String image;

    public TalksChatAdapter(Context mContext, ArrayList<String> usersKeyList) {
        this.mContext = mContext;
        this.usersKeyList = usersKeyList;
        userListChat = new ArrayList<>();
        mAuth = FirebaseAuth.getInstance();
        if (mAuth != null) {
            mCurrent_user_id = mAuth.getCurrentUser().getUid();
        }

        mChatDatabase = FirebaseDatabase.getInstance().getReference().child(MyConstants.CHAT).child(mCurrent_user_id);
        mChatDatabase.keepSynced(true);
        mUsersDatabase = FirebaseDatabase.getInstance().getReference().child(MyConstants.USERS);
        mUsersDatabase.keepSynced(true);
        mMessageDatabase = FirebaseDatabase.getInstance().getReference().child(MyConstants.MESSAGES_TALKS).child(mCurrent_user_id);
        mUsersDatabase.keepSynced(true);
    }


    @Override
    public TalksChatAdapter.TalksChatViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.contact_recycler_item, parent, false);
        return new TalksChatAdapter.TalksChatViewHolder(view);
    }


    @Override
    public void onBindViewHolder(final TalksChatViewHolder holder, final int position) {


        final String uid = usersKeyList.get(position);
        //============================ get last message from the user =============================
        Query lastMessageQuery = mMessageDatabase.child(uid).limitToLast(1);
        lastMessageQuery.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                final String data = dataSnapshot.child(MyConstants.MESSAGE).getValue().toString();
                boolean seen = (boolean) dataSnapshot.child(MyConstants.IS_SEEN).getValue();

                if (!seen) {
                    holder.date_freinds.setTypeface(holder.date_freinds.getTypeface(), Typeface.BOLD);
                    holder.date_freinds.setTextSize(18.0f);

                } else {
                    holder.date_freinds.setTypeface(holder.date_freinds.getTypeface(), Typeface.NORMAL);

                }
                holder.date_freinds.setText(data);

                mUsersDatabase.child(uid).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {

                        name = dataSnapshot.child(MyConstants.NAME).getValue().toString();
                        image = dataSnapshot.child(MyConstants.TAG_IMAGE).getValue().toString();
                        User user = new User();
                        user.setName(name);
                        user.setImage(image);
                        userListChat.add(user);
                        holder.username.setText(name);

                        if(!image.matches("null")) {

                            Picasso.with(mContext).load(image).networkPolicy(NetworkPolicy.OFFLINE).into(holder.userImage, new Callback() {
                                @Override
                                public void onSuccess() {
                                    /// success in ofline mode
                                }

                                @Override
                                public void onError() {
                                    Picasso.with(mContext).load(image).into(holder.userImage);
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

    @Override
    public int getItemCount() {
        return usersKeyList.size();
    }


    public class TalksChatViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        ImageView userImage;
        TextView date_freinds, username;

        public TalksChatViewHolder(View itemView) {
            super(itemView);
            userImage = (ImageView) itemView.findViewById(R.id.contact_image);
            date_freinds = (TextView) itemView.findViewById(R.id.date_freinds);
            username = (TextView) itemView.findViewById(R.id.contat_name);
            itemView.setOnClickListener(this);

        }

        @Override
        public void onClick(View view) {

            int pos = getAdapterPosition();
            String user_id = usersKeyList.get(pos);
            Intent intent = new Intent(mContext, TalksActivity.class);
            intent.putExtra(MyConstants.UUID, user_id);
            intent.putExtra(MyConstants.UNAME, userListChat.get(pos).getName());
            intent.putExtra(MyConstants.UIMAGE, userListChat.get(pos).getImage());
            mContext.startActivity(intent);

        }
    }
}
