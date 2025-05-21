package com.example.luna;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Color;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class Track_Tasks_Activity extends AppCompatActivity {

    FirebaseAuth mAuth;
    FirebaseUser currentUser;
    String userId;

    DatabaseReference taskReference, myTaskRef;
    // Correction: Renommer "overdue" au lieu de "OverDue" pour uniformiser les statuts
    int taskCounter=0, completed=0, deferred=0, overdue=0, cancelled=0, pendingTasks=0;
    TextView textViewTasks;
    PieChart pieChart;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_track_tasks);

        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();
        userId = currentUser.getUid();

        taskReference = FirebaseDatabase.getInstance().getReference("Created Tasks");
        myTaskRef = FirebaseDatabase.getInstance().getReference("Task Progress");
        textViewTasks = (TextView) this.findViewById(R.id.textViewTasksCreated);

        pieChart = findViewById(R.id.pieChart);

        checkUserTasks();
    }

    private void setupChart(PieChart chart) {
        chart.setDrawHoleEnabled(true);
        chart.setHoleColor(Color.TRANSPARENT);
        chart.setTransparentCircleRadius(61f);
        chart.getDescription().setEnabled(false);
        chart.setEntryLabelColor(Color.BLACK);
        chart.setEntryLabelTextSize(14f);

        Legend legend = chart.getLegend();
        legend.setOrientation(Legend.LegendOrientation.VERTICAL);
        legend.setVerticalAlignment(Legend.LegendVerticalAlignment.TOP);
        legend.setHorizontalAlignment(Legend.LegendHorizontalAlignment.RIGHT);
        legend.setDrawInside(false);
        legend.setWordWrapEnabled(true);
        legend.setTextSize(15f);
        legend.setTextColor(Color.BLACK);
    }

    private void updateChartData(PieChart chart) {
        List<PieEntry> entries = new ArrayList<>();

        if(completed > 0) {
            entries.add(new PieEntry(completed, "Tasks Completed"));
        }
        if(cancelled > 0) {
            entries.add(new PieEntry(cancelled, "Tasks Cancelled"));
        }
        if(deferred > 0) {
            entries.add(new PieEntry(deferred, "Tasks Deferred"));
        }
        if(overdue > 0) {
            // Correction: Utiliser "Overdue" au lieu de "OverDue"
            entries.add(new PieEntry(overdue, "Tasks Overdue"));
        }
        if(pendingTasks > 0) {
            entries.add(new PieEntry(pendingTasks, "Tasks Pending"));
        }

        PieDataSet dataSet = new PieDataSet(entries, "");
        dataSet.setColors(Color.GREEN, Color.BLUE, Color.YELLOW, Color.RED, Color.parseColor("#800080"));

        PieData pieData = new PieData(dataSet);
        pieData.setDrawValues(true);
        pieData.setValueTextSize(18f);

        chart.setData(pieData);
        chart.invalidate();
    }

    void checkUserTasks() {
        taskReference.child(userId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                taskCounter = 0; // Reset counter before counting
                for(DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    taskCounter++;
                }
                String tasks = Integer.toString(taskCounter);
                textViewTasks.setText("Total Tasks Created = " + taskCounter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(Track_Tasks_Activity.this, "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

        // Initialiser le graphique
        setupChart(pieChart);

        // Récupérer les tâches complétées
        myTaskRef.child(userId).child("Completed").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                completed = 0;
                for(DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    completed++;
                }
                updateChartData(pieChart);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(Track_Tasks_Activity.this, "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

        // Récupérer les tâches différées
        myTaskRef.child(userId).child("Deferred").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                deferred = 0;
                for(DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    deferred++;
                }
                updateChartData(pieChart);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(Track_Tasks_Activity.this, "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

        // Récupérer les tâches annulées
        myTaskRef.child(userId).child("Cancelled").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                cancelled = 0;
                for(DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    cancelled++;
                }
                updateChartData(pieChart);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(Track_Tasks_Activity.this, "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

        // Récupérer les tâches en retard
        // Correction: Utiliser "Overdue" au lieu de "OverDue"
        myTaskRef.child(userId).child("Overdue").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                overdue = 0;
                for(DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    overdue++;
                }
                updateChartData(pieChart);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(Track_Tasks_Activity.this, "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

        // Récupérer les tâches en attente
        FirebaseDatabase.getInstance().getReference("Categorised Tasks").child(userId)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        pendingTasks = 0;
                        for(DataSnapshot categorySnapshot : dataSnapshot.getChildren()) {
                            for(DataSnapshot taskSnapshot : categorySnapshot.getChildren()) {
                                Task_Class task = taskSnapshot.getValue(Task_Class.class);
                                if(task != null && task.getStatus().equals("Pending")) {
                                    pendingTasks++;
                                }
                            }
                        }
                        updateChartData(pieChart);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(Track_Tasks_Activity.this, "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }
}