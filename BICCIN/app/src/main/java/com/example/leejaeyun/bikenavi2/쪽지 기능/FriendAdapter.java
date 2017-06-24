package com.example.leejaeyun.bikenavi2;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.squareup.picasso.Picasso;

import java.util.List;

import static com.example.leejaeyun.bikenavi2.R.color.indigo;

/**
 * Created by 161116 on 2017-05-06.
 */

public class FriendAdapter extends RecyclerView.Adapter<FriendAdapter.ViewHolder> {
    List<Friend> mFriend;
    String stEmail;
    Context context;
    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView tvEmail;
        public ImageView ivUser;
        public Button btnChat;
        public ImageView ivIam;

        public ViewHolder(View itemView) {
            super(itemView);
            tvEmail = (TextView) itemView.findViewById(R.id.tvEmail);
            ivUser = (ImageView) itemView.findViewById(R.id.ivUser);
            btnChat = (Button) itemView.findViewById(R.id.btnChat);
            ivIam = (ImageView) itemView.findViewById(R.id.ivIam);
        }
    }

    public FriendAdapter(List<Friend> mFriend, Context context) {
        this.mFriend = mFriend;
        this.context = context;
    }

    @Override
    public FriendAdapter.ViewHolder onCreateViewHolder(ViewGroup parent,
                                                       int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.list_friend, parent, false);

        ViewHolder vh = new ViewHolder(v);
        return vh;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, final int position) {
        holder.tvEmail.setText(mFriend.get(position).getEmail());
        String stPhoto = mFriend.get(position).getPhoto();
        if (TextUtils.isEmpty(stPhoto)) {
            Picasso.with(context)
                    .load(R.mipmap.ic_launcher)
                    .fit()
                    .centerInside()
                    .into(holder.ivUser);

        } else {
            Picasso.with(context)
                    .load(stPhoto)
                    .fit()
                    .centerInside()
                    .into(holder.ivUser);
        }

        if (user.getEmail().equals(mFriend.get(position).getEmail())) {
            holder.btnChat.setText("수신함");
            holder.btnChat.setBackgroundColor(Color.parseColor("#3F51B5"));
            holder.ivIam.setVisibility(View.VISIBLE);
            holder.btnChat.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    String stFriendId = mFriend.get(position).getKey();
                    Intent in = new Intent(context, ChatActivity.class);
                    in.putExtra("friendUid", stFriendId);
                    context.startActivity(in);

                }
            });
        } else {
            holder.btnChat.setText("보내기");
            holder.btnChat.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    String stFriendId = mFriend.get(position).getKey();
                    Intent in = new Intent(context, ChatActivity.class);
                    in.putExtra("friendUid", stFriendId);
                    context.startActivity(in);

                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return mFriend.size();
    }

}