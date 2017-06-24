package com.example.leejaeyun.bikenavi2;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.example.leejaeyun.bikenavi2.Tmap.DistanceValue;

import java.util.List;

/**
 * Created by Lee Jae Yun on 2017-06-07.
 */

public class DistanceAdapter extends RecyclerView.Adapter<DistanceAdapter.ViewHolder> {
    List<DistanceValue> mDistanceValue;
    Context context;
    String TAG = getClass().getSimpleName();

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView starttime;
        public TextView endtime;
        public TextView distance;

        public ViewHolder(View itemView) {
            super(itemView);
            starttime = (TextView) itemView.findViewById(R.id.main_starttime);
            endtime = (TextView) itemView.findViewById(R.id.main_endtime);
            distance = (TextView) itemView.findViewById(R.id.distance);
        }
    }

    public DistanceAdapter(List<DistanceValue> mDistanceValue, Context context) {
        this.mDistanceValue = mDistanceValue;
        this.context = context;

    }

    @Override
    public DistanceAdapter.ViewHolder onCreateViewHolder(ViewGroup parent,
                                                         int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.distancelist_item, parent, false);

        ViewHolder vh = new ViewHolder(v);
        return vh;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, final int position) {
        holder.starttime.setText(mDistanceValue.get(position).getstartTime());
        holder.endtime.setText(mDistanceValue.get(position).getendTime());
        holder.distance.setText(mDistanceValue.get(position).getDistance() + "km");

        Log.d(TAG, "Value1 is: " + mDistanceValue.get(position).getstartTime());
        Log.d(TAG, "Value2 is: " + mDistanceValue.get(position).getendTime());

    }

    @Override
    public int getItemCount() {
        return mDistanceValue.size();
    }

}
