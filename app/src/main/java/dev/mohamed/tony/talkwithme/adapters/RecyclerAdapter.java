package dev.mohamed.tony.talkwithme.adapters;

import android.app.Activity;
import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import dev.mohamed.tony.talkwithme.R;
import dev.mohamed.tony.talkwithme.models.User;

import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import java.util.List;

/**
 * Created by Tony on 1/21/2018.
 */

public class RecyclerAdapter extends RecyclerView.Adapter<RecyclerAdapter.UserViewHolder> {

    final private OnItemClicked onItemClicked;
    private Activity mActivity;
    private Context mContext;
    private List<User> userList;
    private List<String> usersKeyList;

    public RecyclerAdapter(OnItemClicked onItemClicked, Context mContext, List<User> userList, List<String> usersKeyList) {
        this.mActivity = mActivity;
        this.userList = userList;
        this.mContext = mContext;
        this.usersKeyList = usersKeyList;
        this.onItemClicked = onItemClicked;
    }


    @Override
    public UserViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.recycler_item, parent, false);
        return new UserViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final UserViewHolder holder, final int position) {

        if (!TextUtils.isEmpty(userList.get(position).getName())) {
            holder.userName.setText(userList.get(position).getName());
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


    }

    @Override
    public int getItemCount() {
        return userList.size();
    }

    public interface OnItemClicked {
        void onItemClickedLisener(String id);
    }

    public class UserViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
        ImageView userImage, friendRequest;
        TextView userName;

        public UserViewHolder(View itemView) {
            super(itemView);
            userImage = (ImageView) itemView.findViewById(R.id.user_circle_image);
            userName = (TextView) itemView.findViewById(R.id.user_name);
            friendRequest = (ImageView) itemView.findViewById(R.id.freind_request);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            int pos = getAdapterPosition();
            onItemClicked.onItemClickedLisener(usersKeyList.get(pos));
        }
    }
}