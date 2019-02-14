package dev.mohamed.tony.talkwithme.adapters;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import dev.mohamed.tony.talkwithme.MyConstants;
import dev.mohamed.tony.talkwithme.R;
import dev.mohamed.tony.talkwithme.activities.UserProfileActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import java.util.List;

/**
 * Created by Tony on 1/25/2018.
 */

public class TalkRequestAdapter extends RecyclerView.Adapter<TalkRequestAdapter.RequestViewHolder> {
    private Context mContext;
    private List<String> usersKeyList;
    private DatabaseReference mDataBaseRef;
    private FirebaseAuth mAuth;
    private String currentUserId;

    public TalkRequestAdapter(Context mContext, List<String> usersKeyList) {
        this.mContext = mContext;
        this.usersKeyList = usersKeyList;
        mDataBaseRef = FirebaseDatabase.getInstance().getReference().child(MyConstants.USERS);
        mAuth = FirebaseAuth.getInstance();
        currentUserId = mAuth.getCurrentUser().getUid();
    }


    @Override
    public TalkRequestAdapter.RequestViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.request_item, parent, false);
        return new TalkRequestAdapter.RequestViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final TalkRequestAdapter.RequestViewHolder holder, final int position) {

        mDataBaseRef.child(usersKeyList.get(position)).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.hasChild(MyConstants.NAME)) {
                    String name = dataSnapshot.child(MyConstants.NAME).getValue().toString();
                    holder.username.setText(name);
                }
                if (dataSnapshot.hasChild(MyConstants.TAG_IMAGE)) {
                    final String image = dataSnapshot.child(MyConstants.TAG_IMAGE).getValue().toString();


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


    public class RequestViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        ImageView userImage;
        TextView username;

        public RequestViewHolder(View itemView) {
            super(itemView);
            userImage = (ImageView) itemView.findViewById(R.id.request_image);
            username = (TextView) itemView.findViewById(R.id.request_user_name);
            itemView.setOnClickListener(this);

        }

        @Override
        public void onClick(View view) {

            int pos = getAdapterPosition();

            Intent intent = new Intent(mContext, UserProfileActivity.class);
            intent.putExtra(MyConstants.UID, usersKeyList.get(pos));
            mContext.startActivity(intent);

        }
    }
}
