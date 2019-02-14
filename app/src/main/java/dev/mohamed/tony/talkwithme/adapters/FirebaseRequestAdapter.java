package dev.mohamed.tony.talkwithme.adapters;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
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

import java.util.ArrayList;

import dev.mohamed.tony.talkwithme.MyConstants;
import dev.mohamed.tony.talkwithme.R;
import dev.mohamed.tony.talkwithme.activities.UserProfileActivity;
import dev.mohamed.tony.talkwithme.models.TalkRequests;
import dev.mohamed.tony.talkwithme.models.User;

public class FirebaseRequestAdapter extends FirebaseRecyclerAdapter<TalkRequests, FirebaseRequestAdapter.RequestViewHolder> {


    /**
     * Initialize a {@link RecyclerView.Adapter} that listens to a Firebase query. See
     * {@link FirebaseRecyclerOptions} for configuration options.
     *
     * @param options
     */
    private DatabaseReference mUsersDatabase;
    private Context mContext;
    private ArrayList<TalkRequests> talkRequests;
    private ArrayList<String>keys;

    public FirebaseRequestAdapter(@NonNull FirebaseRecyclerOptions<TalkRequests> options, DatabaseReference mUsersDatabase, Context mContext) {
        super(options);
        this.mUsersDatabase = mUsersDatabase;
        this.mContext = mContext;
        talkRequests=new ArrayList<>();

      //  Log.d("allllll",getSnapshots().get(5).getRequest_type().toString());
    }

    /*    @Override
    public int getItemCount() {
            int count=0;
            int i=0;
            keys=new ArrayList<>();

        for (TalkRequests talkRequests : getSnapshots()) {
            if (talkRequests.getRequest_type().equals("recieved")) {

                keys.add(getRef(i).getKey());
                Log.d("key-->",getRef(i).getKey());
                count++;
            }
            i++;
        }
        Log.d("cout-->",String.valueOf(i));
       // Log.d("key-->",count);
        return count;
    }*/

    @NonNull
    @Override
    public FirebaseRequestAdapter.RequestViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.request_item, parent, false);
            return new FirebaseRequestAdapter.RequestViewHolder(view);

        //sent
/*      if (viewType == 0) {
            View view = LayoutInflater.from(mContext).inflate(R.layout.re, parent, false);
            return new FirebaseRequestAdapter.RequestViewHolder(view);

        } else if (viewType == 1) {
            View view = LayoutInflater.from(mContext).inflate(R.layout.talks_recycler_item_user, parent, false);
            return new FirebaseRequestAdapter.RequestViewHolder(view);
        }
        return null;
    }*/
    }

    @Override
    public int getItemViewType(int position) {

        Log.d("req_type",getSnapshots().get(0).getRequest_type());
        if(getSnapshots().get(position).getRequest_type().equals("sent")) {
            return 0;
        }else{
            return 1;
        }
    }

    @Override
    protected void onBindViewHolder(@NonNull final FirebaseRequestAdapter.RequestViewHolder holder, int position, @NonNull TalkRequests model) {

       // if(model.getRequest_type().equals("recieved")) {
            Log.d("type_rec", model.getRequest_type());
            Log.d("uid_key", getRef(position).getKey());

            // if(model.getRequest_type().matches(MyConstants.RECIEVED)) {
            mUsersDatabase.child(getRef(position).getKey()).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if (dataSnapshot.hasChildren()) {
                        //========== each value considers as user============
                        final User user = dataSnapshot.getValue(User.class);

                        holder.username.setText(user.getName());
                        Log.d("namse", user.getName());
                        if (!user.getImage().matches("null")) {
                            Picasso.with(mContext).load(user.getImage()).networkPolicy(NetworkPolicy.OFFLINE).into(holder.userImage, new Callback() {
                                @Override
                                public void onSuccess() {
                                    /// success in ofline mode
                                }

                                @Override
                                public void onError() {
                                    Picasso.with(mContext).load(user.getImage()).into(holder.userImage);
                                }
                            });
                        }
                  /*
                   Log.d("name_in", "in");
                   if (dataSnapshot.hasChild(MyConstants.NAME)) {
                       String name = dataSnapshot.child(MyConstants.NAME).getValue().toString();
                       Log.d("name_", "in");
                       holder.username.setText(name);
                   }
                   if (dataSnapshot.hasChild(MyConstants.TAG_IMAGE)) {
                       final String image = dataSnapshot.child(MyConstants.TAG_IMAGE).getValue().toString();


                       if (!image.matches("null")) {
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
                   }*/
                    }

                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
       // }

        //}
    }

    class RequestViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
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
            intent.putExtra(MyConstants.UID, getRef(pos).getKey());
            mContext.startActivity(intent);
        }

/*        @Override
        public void onClick(View view) {

            int pos = getAdapterPosition();

            Intent intent = new Intent(mContext, UserProfileActivity.class);
            intent.putExtra(MyConstants.UID, usersKeyList.get(pos));
            mContext.startActivity(intent);

        }*/
    }

}

