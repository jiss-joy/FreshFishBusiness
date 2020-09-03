package com.smartechbraintechnologies.freshfishbusiness;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class SelectFishAdapter extends RecyclerView.Adapter<SelectFishAdapter.MyViewHolder> {

    private Context context;
    private ArrayList<SelectFishModel> FishList;
    private ArrayList<Integer> FishCounter;
    private OnFishSelectedListener onFishSelectedListener;

    private int mPosition;
    private int indicator = 0;

    public SelectFishAdapter(Context context, ArrayList<SelectFishModel> fishList, ArrayList<Integer> fishCounter, OnFishSelectedListener onFishSelectedListener) {
        this.context = context;
        FishList = fishList;
        FishCounter = fishCounter;
        this.onFishSelectedListener = onFishSelectedListener;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (FishCounter.get(mPosition) == 0) {
            indicator = 0;
            return new MyViewHolder(LayoutInflater.from(context).inflate(R.layout.select_fish_item, parent, false), onFishSelectedListener);
        } else {
            indicator = 1;
            return new MyViewHolder(LayoutInflater.from(context).inflate(R.layout.select_fish_item_selected, parent, false), onFishSelectedListener);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        if (indicator == 0) {
            holder.fishName_tv.setText(FishList.get(position).getFishName());
        } else {
            holder.fishName_tv_selected.setText(FishList.get(position).getFishName());
        }
    }

    @Override
    public int getItemCount() {
        return FishList.size();
    }

    @Override
    public int getItemViewType(int position) {
        mPosition = position;
        return super.getItemViewType(position);
    }

    public class MyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        TextView fishName_tv;
        TextView fishName_tv_selected;
        OnFishSelectedListener onFishSelectedListener;

        public MyViewHolder(@NonNull View itemView, OnFishSelectedListener onFishSelectedListener) {
            super(itemView);
            fishName_tv = itemView.findViewById(R.id.select_fish_name);
            fishName_tv_selected = itemView.findViewById(R.id.select_fish_name_selected);
            this.onFishSelectedListener = onFishSelectedListener;

            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            onFishSelectedListener.onFishClick(getAdapterPosition());
        }
    }

    public interface OnFishSelectedListener {
        void onFishClick(int position);
    }
}
