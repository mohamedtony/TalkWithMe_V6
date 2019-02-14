package dev.mohamed.tony.talkwithme.adapters;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import dev.mohamed.tony.talkwithme.MyConstants;
import dev.mohamed.tony.talkwithme.R;
import dev.mohamed.tony.talkwithme.activities.UserProfileActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import dev.mohamed.tony.talkwithme.models.User;

/**
 * Created by Tony on 1/23/2018.
 */

public class ContactsAdapter extends RecyclerView.Adapter<ContactsAdapter.ContactViewHolder> {


    private Context mContext;
    private List<User> userList;
    private List<String> usersKeyList;
    private List<String> usersDatesList;
    private DatabaseReference mUsersRef;
    private FirebaseAuth mAuth;

    public ContactsAdapter(Context mContext, List<String> usersKeyList, List<User> userList, List<String> usersDatesList) {

        this.userList = userList;
        this.mContext = mContext;
        this.usersKeyList = usersKeyList;
        this.usersDatesList = usersDatesList;

    }


    @Override
    public ContactsAdapter.ContactViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.contact_recycler_item, parent, false);
        return new ContactsAdapter.ContactViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ContactsAdapter.ContactViewHolder holder, final int position) {

        if (!TextUtils.isEmpty(userList.get(position).getName())) {
            holder.username.setText(userList.get(position).getName());
        }
        if (!TextUtils.isEmpty(userList.get(position).getImage())) {
            if (!userList.get(position).getImage().matches("null")) {
                // Picasso.with(mContext).load(userList.get(position).getImage()).into(holder.userImage);
                Picasso.with(mContext).load(userList.get(position).getImage()).networkPolicy(NetworkPolicy.OFFLINE).into(holder.userImage, new Callback() {
                    @Override
                    public void onSuccess() {
                        /// success in ofline mode
                    }

                    @Override
                    public void onError() {
                        Picasso.with(mContext).load(userList.get(position).getImage()).into(holder.userImage);
                    }
                });
            }
        }
        if(userList.get(position).getOnline()=="true"){
            holder.icOnline.setVisibility(View.VISIBLE);
        }else{
            holder.icOnline.setVisibility(View.INVISIBLE);
        }

        //holder.date_freinds.setText(userList.get(position).getDate());


    }
    private String getDate(long time) {
        Calendar cal = Calendar.getInstance(Locale.ENGLISH);
        cal.setTimeInMillis(time);
        String date = DateFormat.format(MyConstants.DD_MM_YYYY, cal).toString();
        return date;
    }

    @Override
    public int getItemCount() {
        return userList.size();
    }


    public class ContactViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        ImageView userImage,icOnline;
        TextView date_freinds, username;

        public ContactViewHolder(View itemView) {
            super(itemView);
            userImage = (ImageView) itemView.findViewById(R.id.contact_image);
            date_freinds = (TextView) itemView.findViewById(R.id.date_freinds);
            username = (TextView) itemView.findViewById(R.id.contat_name);
            icOnline=(ImageView)itemView.findViewById(R.id.ic_online_icon);
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
