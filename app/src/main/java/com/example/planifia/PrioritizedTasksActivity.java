package com.example.planifia;

import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;



public class PrioritizedTasksActivity extends AppCompatActivity {

    private RecyclerView recyclerViewPrioritizedTasks;
    private PrioritizedTaskAdapter taskAdapter;
    private ProgressBar progressBar;
    private TextView textViewNoTasks;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_prioritized_tasks);

        recyclerViewPrioritizedTasks = findViewById(R.id.recyclerViewPrioritizedTasks);
        progressBar = findViewById(R.id.progressBarPrioritized);
        textViewNoTasks = findViewById(R.id.textViewNoTasksPrioritized);

        // Configurer le RecyclerView
        recyclerViewPrioritizedTasks.setLayoutManager(new LinearLayoutManager(this));

        // Initialiser l'adaptateur
        taskAdapter = new PrioritizedTaskAdapter();
        recyclerViewPrioritizedTasks.setAdapter(taskAdapter);

        // Afficher le chargement
        progressBar.setVisibility(View.VISIBLE);

        // Analyser et prioriser les tâches
        TaskAnalyzer taskAnalyzer = new TaskAnalyzer();
        taskAnalyzer.analyzeTasks(new TaskAnalyzer.TaskAnalysisCallback() {
            @Override
            public void onAnalysisComplete(List<Task_Class> prioritizedTasks) {
                progressBar.setVisibility(View.GONE);

                if (prioritizedTasks.isEmpty()) {
                    textViewNoTasks.setVisibility(View.VISIBLE);
                    recyclerViewPrioritizedTasks.setVisibility(View.GONE);
                } else {
                    textViewNoTasks.setVisibility(View.GONE);
                    recyclerViewPrioritizedTasks.setVisibility(View.VISIBLE);
                    taskAdapter.setTasks(prioritizedTasks);
                }
            }

            @Override
            public void onError(String errorMessage) {
                // Handle the error case
                progressBar.setVisibility(View.GONE);
                textViewNoTasks.setVisibility(View.VISIBLE);
                textViewNoTasks.setText("Error: " + errorMessage);
                recyclerViewPrioritizedTasks.setVisibility(View.GONE);
            }
        });
    }
}