package com.example.planifia;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class myEvents_adapter extends RecyclerView.Adapter<myEvents_adapter.MyViewHolder> {

    private ArrayList<Event_Class> eventsList;

    public myEvents_adapter(ArrayList<Event_Class> eventsList) {
        this.eventsList = eventsList;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.event_item_layout, parent, false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        Event_Class event = eventsList.get(position);

        holder.textViewTitle.setText(event.getTitle());
        holder.textViewDescription.setText(event.getDescription());
        holder.textViewDate.setText(event.getDueDate());
        holder.textViewTime.setText(event.getStartTime() + " - " + event.getEndTime());
        holder.textViewLocation.setText(event.getLocation());
    }

    @Override
    public int getItemCount() {
        return eventsList.size();
    }

    public void setData(ArrayList<Event_Class> newData) {
        this.eventsList = newData;
        notifyDataSetChanged();
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder {
        TextView textViewTitle, textViewDescription, textViewDate, textViewTime, textViewLocation;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewTitle = itemView.findViewById(R.id.textViewEventItemTitle);
            textViewDescription = itemView.findViewById(R.id.textViewEventItemDescription);
            textViewDate = itemView.findViewById(R.id.textViewEventItemDate);
            textViewTime = itemView.findViewById(R.id.textViewEventItemTime);
            textViewLocation = itemView.findViewById(R.id.textViewEventItemLocation);
        }
    }
}