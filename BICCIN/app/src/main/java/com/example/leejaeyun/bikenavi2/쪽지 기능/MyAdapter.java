package com.example.leejaeyun.bikenavi2;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

/**
 * Created by 161116 on 2017-05-06.
 */

public class MyAdapter extends RecyclerView.Adapter<MyAdapter.ViewHolder> {
    List<Chat> mChat;
    String stEmail;
    Context context;

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView mTextView;
        TextView sender;

        public ViewHolder(View ItemView) {
            super(ItemView);
            mTextView = (TextView) ItemView.findViewById(R.id.mTextView);
            sender = (TextView) ItemView.findViewById(R.id.whosend);
        }
    }

    public MyAdapter(List<Chat> mChat, String email, Context context) {
        this.mChat = mChat;
        this.stEmail = email;
        this.context = context;
    }

    @Override
    public int getItemViewType(int position) {
        if (mChat.get(position).getEmail().equals(stEmail)) {
            return 1; //송신자일 경우
        } else {
            return 2; //상대방일 경우
        }
    }

    @Override
    public MyAdapter.ViewHolder onCreateViewHolder(ViewGroup parent,
                                                   int viewType) {
        View v;
        if (viewType == 1) {
            v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.right_text_view, parent, false); //송신자일 경우 화면 오른쪽에 위치
        } else {
            v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.my_text_view, parent, false); //수신자일 경우 화면 왼쪽에 위치

        }

        ViewHolder vh = new ViewHolder(v);
        return vh;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, final int position) {
        holder.sender.setText(mChat.get(position).getEmail());
        holder.mTextView.setText(mChat.get(position).getText());
        holder.mTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(context, String.valueOf(position), Toast.LENGTH_SHORT);
            }
        });
    }

    @Override
    public int getItemCount() {
        return mChat.size();
    }
}
