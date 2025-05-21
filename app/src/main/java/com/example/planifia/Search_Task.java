package com.example.planifia;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.DatePicker;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class Search_Task extends AppCompatActivity {

    private static final String TAG = "Search_Task";

    RecyclerView searchTaskRecyclerView;
    myTasks_adapter myTasks_adapter;
    ArrayList<Task_Class> myTaskArrayList = new ArrayList<>();
    DatabaseReference rootRef;

    String selectedDate;
    ConstraintLayout constraintLayoutSearchTasks;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.search_task_activity);

        // Initialiser les vues
        constraintLayoutSearchTasks = findViewById(R.id.constraintLayoutSearchTasks);
        searchTaskRecyclerView = findViewById(R.id.recyclerViewSearchTasks);

        // Obtenir l'ID de l'utilisateur
        FirebaseAuth myAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = myAuth.getCurrentUser();
        String userId = currentUser.getUid();

        // Configurer le RecyclerView
        myTasks_adapter = new myTasks_adapter(myTaskArrayList);
        searchTaskRecyclerView.setAdapter(myTasks_adapter);
        searchTaskRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Ajouter une décoration au RecyclerView
        int spacingInPixels = getResources().getDimensionPixelSize(R.dimen.item_spacing);
        searchTaskRecyclerView.addItemDecoration(new RecyclerViewItemDecorationClass(this, spacingInPixels));

        // Obtenir la date actuelle
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH);

        // Initialiser la date sélectionnée avec la date actuelle
        String myMonth = (month + 1) < 10 ? "0" + (month + 1) : String.valueOf(month + 1);
        String myDay = dayOfMonth < 10 ? "0" + dayOfMonth : String.valueOf(dayOfMonth);
        selectedDate = year + "-" + myMonth + "-" + myDay;

        // Initialiser la référence Firebase - pointer vers la racine
        rootRef = FirebaseDatabase.getInstance().getReference();

        // Configurer le DatePicker
        DatePicker datePicker = findViewById(R.id.datePickerNew);
        datePicker.init(year, month, dayOfMonth, new DatePicker.OnDateChangedListener() {
            @Override
            public void onDateChanged(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                // Mettre à jour la date sélectionnée
                String myMonth = (monthOfYear + 1) < 10 ? "0" + (monthOfYear + 1) : String.valueOf(monthOfYear + 1);
                String myDay = dayOfMonth < 10 ? "0" + dayOfMonth : String.valueOf(dayOfMonth);
                selectedDate = year + "-" + myMonth + "-" + myDay;

                Log.d(TAG, "Date sélectionnée: " + selectedDate);

                // Rechercher les tâches pour la nouvelle date
                searchTasksByDate(userId, selectedDate);
            }
        });

        // Charger les tâches pour la date actuelle au démarrage
        searchTasksByDate(userId, selectedDate);
    }

    // Nouvelle méthode pour rechercher les tâches par date
    private void searchTasksByDate(String userId, String date) {
        Log.d(TAG, "Recherche des tâches pour la date: " + date);

        // Vider la liste des tâches
        myTaskArrayList.clear();

        // Rechercher dans "Tasks" et "Categorised Tasks" pour être sûr
        searchInPath("Tasks/" + userId, date);
        searchInPath("Categorised Tasks/" + userId, date);
    }

    // Méthode pour rechercher dans un chemin spécifique
    private void searchInPath(String path, String date) {
        rootRef.child(path).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!snapshot.exists()) {
                    Log.d(TAG, "Aucune donnée trouvée dans " + path);
                    updateUI();
                    return;
                }

                Log.d(TAG, "Données trouvées dans " + path + ": " + snapshot.getChildrenCount() + " enfants");

                // Parcourir tous les enfants (catégories ou dates selon la structure)
                for (DataSnapshot categoryOrDateSnapshot : snapshot.getChildren()) {
                    Log.d(TAG, "Parcours de " + categoryOrDateSnapshot.getKey() + " dans " + path);

                    // Parcourir tous les enfants de ce niveau
                    for (DataSnapshot taskOrCategorySnapshot : categoryOrDateSnapshot.getChildren()) {
                        // Si c'est une tâche directement
                        if (taskOrCategorySnapshot.hasChild("dueDate")) {
                            processTaskSnapshot(taskOrCategorySnapshot, date);
                        }
                        // Si c'est encore un niveau de hiérarchie
                        else {
                            for (DataSnapshot taskSnapshot : taskOrCategorySnapshot.getChildren()) {
                                if (taskSnapshot.hasChild("dueDate")) {
                                    processTaskSnapshot(taskSnapshot, date);
                                }
                            }
                        }
                    }
                }

                updateUI();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "Erreur Firebase: " + error.getMessage());
                Toast.makeText(Search_Task.this, "Erreur lors de la récupération des tâches", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Méthode pour traiter un snapshot de tâche
    private void processTaskSnapshot(DataSnapshot taskSnapshot, String date) {
        try {
            String dueDate = taskSnapshot.child("dueDate").getValue(String.class);

            if (dueDate != null && dueDate.equals(date)) {
                Log.d(TAG, "Tâche trouvée avec date correspondante: " + taskSnapshot.getKey());

                // Créer un Map pour stocker toutes les valeurs
                Map<String, Object> taskData = new HashMap<>();
                for (DataSnapshot field : taskSnapshot.getChildren()) {
                    taskData.put(field.getKey(), field.getValue());
                }

                // Créer l'objet Task_Class avec les valeurs du Map
                Task_Class task = new Task_Class();

                // Définir les propriétés de la tâche
                if (taskData.containsKey("title")) task.setTitle((String) taskData.get("title"));
                if (taskData.containsKey("description")) task.setDescription((String) taskData.get("description"));
                if (taskData.containsKey("startTime")) task.setStartTime((String) taskData.get("startTime"));
                if (taskData.containsKey("dueDate")) task.setDueDate((String) taskData.get("dueDate"));
                if (taskData.containsKey("category")) task.setCategory((String) taskData.get("category"));
                if (taskData.containsKey("status")) task.setStatus((String) taskData.get("status"));
                if (taskData.containsKey("dateTime")) task.setDateTime((String) taskData.get("dateTime"));
                if (taskData.containsKey("endTime")) task.setEndTime((String) taskData.get("endTime"));

                // Ajouter la tâche à la liste
                myTaskArrayList.add(task);
                Log.d(TAG, "Tâche ajoutée: " + task.getTitle());
            }
        } catch (Exception e) {
            Log.e(TAG, "Erreur lors du traitement de la tâche: " + e.getMessage());
        }
    }

    // Méthode pour mettre à jour l'interface utilisateur
    private void updateUI() {
        if (myTaskArrayList.isEmpty()) {
            Log.d(TAG, "Aucune tâche trouvée pour la date: " + selectedDate);
            searchTaskRecyclerView.setVisibility(View.GONE);
            constraintLayoutSearchTasks.setVisibility(View.VISIBLE);
        } else {
            Log.d(TAG, "Nombre de tâches trouvées: " + myTaskArrayList.size());
            searchTaskRecyclerView.setVisibility(View.VISIBLE);
            constraintLayoutSearchTasks.setVisibility(View.GONE);
            myTasks_adapter.setData(myTaskArrayList);
        }
    }
}