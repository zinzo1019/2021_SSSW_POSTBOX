package com.example.sswu_postbox;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class MyRecyclerAdapter extends RecyclerView.Adapter<MyRecyclerAdapter.ViewHolder> {
    private ArrayList<String> itemList;
    private Context context;
    private View.OnClickListener onClickItem;
    private Object Dimension;


    public MyRecyclerAdapter(Context context, ArrayList<String> itemList, View.OnClickListener onClickItem) {

        this.context = context;
        this.itemList = itemList;
        this.onClickItem = onClickItem;
    }

    @NonNull
    @org.jetbrains.annotations.NotNull

    @Override
    public MyRecyclerAdapter.ViewHolder onCreateViewHolder(@NonNull @org.jetbrains.annotations.NotNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(context)
                .inflate(R.layout.recyclerview_layout, parent, false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull @org.jetbrains.annotations.NotNull MyRecyclerAdapter.ViewHolder holder, int position) {

        String item = itemList.get(position);

        holder.textView.setText(item);
        holder.textView.setTag(item);
        holder.textView.setOnClickListener(onClickItem);
    }

    @Override
    public int getItemCount() {
        return itemList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{

        public TextView textView;

        public ViewHolder(View itemView){
            super(itemView);
            textView = itemView.findViewById(R.id.home_keyword_recyclerview);
        }
    }
}
