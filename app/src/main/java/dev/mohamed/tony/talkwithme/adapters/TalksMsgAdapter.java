package dev.mohamed.tony.talkwithme.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import dev.mohamed.tony.talkwithme.R;
import dev.mohamed.tony.talkwithme.activities.TalksActivity;
import dev.mohamed.tony.talkwithme.models.TalksMessages;

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

import static dev.mohamed.tony.talkwithme.MyConstants.USERS;

/**
 * Created by Tony on 1/24/2018.
 */

public class TalksMsgAdapter extends RecyclerView.Adapter<TalksMsgAdapter.TalksMsgViewHolder> {

    private List<TalksMessages> talksMessages;
    private Context mContext;
    private FirebaseAuth mAuth;
    private String currentUid;
    private DatabaseReference mDatabase;

    public TalksMsgAdapter(Context mContext, List<TalksMessages> talksMessages) {
        this.talksMessages = talksMessages;
        this.mContext = mContext;
        mAuth = FirebaseAuth.getInstance();
        if (mAuth != null) {
            currentUid = mAuth.getCurrentUser().getUid();
        }
        mDatabase = FirebaseDatabase.getInstance().getReference().child(USERS).child(currentUid);
    }


    @Override
    public TalksMsgAdapter.TalksMsgViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        if (viewType == TalksActivity.VIEW_TYPE_FRIEND_MESSAGE) {
            View view = LayoutInflater.from(mContext).inflate(R.layout.talks_recycler_item_friend, parent, false);
            return new TalksMsgAdapter.TalksMsgViewHolder(view);
        } else if (viewType == TalksActivity.VIEW_TYPE_USER_MESSAGE) {
            View view = LayoutInflater.from(mContext).inflate(R.layout.talks_recycler_item_user, parent, false);
            return new TalksMsgAdapter.TalksMsgViewHolder(view);
        }
        return null;

    }

    @Override
    public int getItemViewType(int position) {
        if (talksMessages != null) {
            return currentUid.equals(talksMessages.get(position).getFrom()) ? TalksActivity.VIEW_TYPE_USER_MESSAGE : TalksActivity.VIEW_TYPE_FRIEND_MESSAGE;
        } else {
            return 0;
        }
    }

    @Override
    public void onBindViewHolder(final TalksMsgAdapter.TalksMsgViewHolder holder, final int position) {

        holder.msgTalk.setText(talksMessages.get(position).getMessage());


        mDatabase.keepSynced(true);
        // Read from the database
        mDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                final String image = dataSnapshot.child("image").getValue().toString();
                if (!image.matches("null")) {
                    if (holder.currentuserImage != null) {
                        Picasso.with(mContext).load(image).networkPolicy(NetworkPolicy.OFFLINE).into(holder.currentuserImage, new Callback() {
                            @Override
                            public void onSuccess() {
                                /// success in ofline mode
                            }

                            @Override
                            public void onError() {
                                Picasso.with(mContext).load(image).into(holder.currentuserImage);
                            }
                        });
                    }
                }

            }

            @Override
            public void onCancelled(DatabaseError error) {

            }
        });
    }

    @Override
    public int getItemCount() {
        return talksMessages.size();
    }


    public class TalksMsgViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        ImageView currentuserImage;
        TextView msgTalk;
        RelativeLayout relative_layout_item;

        public TalksMsgViewHolder(View itemView) {
            super(itemView);
            currentuserImage = (ImageView) itemView.findViewById(R.id.user_talk_image);
            msgTalk = (TextView) itemView.findViewById(R.id.chat_msg_item);
            relative_layout_item = (RelativeLayout) itemView.findViewById(R.id.relative_layout_item);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            int pos = getAdapterPosition();

        }
    }
}
