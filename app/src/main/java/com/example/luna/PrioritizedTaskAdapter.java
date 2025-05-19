package com.example.luna;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class PrioritizedTaskAdapter extends RecyclerView.Adapter<PrioritizedTaskAdapter.TaskViewHolder> {

    private List<Task_Class> tasks = new ArrayList<>();

    public void setTasks(List<Task_Class> tasks) {
        this.tasks = tasks;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public TaskViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_prioritized_task, parent, false);
        return new TaskViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TaskViewHolder holder, int position) {
        Task_Class task = tasks.get(position);

        holder.textViewTitle.setText(task.getTitle());
        holder.textViewDescription.setText(task.getDescription());
        holder.textViewDueDate.setText("Due: " + task.getDueDate());
        holder.textViewCategory.setText("Category: " + task.getCategory());

        // Afficher le score de priorité (arrondi à 2 décimales)
        double score = task.getPriorityScore();
        String formattedScore = String.format("%.2f", score);
        holder.textViewPriorityScore.setText("Priority Score: " + formattedScore);

        // Définir la couleur de priorité en fonction du score
        if (score > 0.7) {
            holder.viewPriorityIndicator.setBackgroundResource(R.color.high_priority);
        } else if (score > 0.4) {
            holder.viewPriorityIndicator.setBackgroundResource(R.color.medium_priority);
        } else {
            holder.viewPriorityIndicator.setBackgroundResource(R.color.low_priority);
        }
    }

    @Override
    public int getItemCount() {
        return tasks.size();
    }

    static class TaskViewHolder extends RecyclerView.ViewHolder {
        TextView textViewTitle, textViewDescription, textViewDueDate, textViewCategory, textViewPriorityScore;
        View viewPriorityIndicator;

        public TaskViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewTitle = itemView.findViewById(R.id.textViewTaskTitle);
            textViewDescription = itemView.findViewById(R.id.textViewTaskDescription);
            textViewDueDate = itemView.findViewById(R.id.textViewTaskDueDate);
            textViewCategory = itemView.findViewById(R.id.textViewTaskCategory);
            textViewPriorityScore = itemView.findViewById(R.id.textViewPriorityScore);
            viewPriorityIndicator = itemView.findViewById(R.id.viewPriorityIndicator);
        }
    }
}