package com.example.luna;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class TaskAnalyzer {

    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;
    private String userId;
    private DatabaseReference tasksReference;
    private DatabaseReference completedTasksReference;

    // Facteurs de pondération pour le calcul de priorité
    private static final double WEIGHT_DUE_DATE = 0.4;
    private static final double WEIGHT_CATEGORY_PREFERENCE = 0.3;
    private static final double WEIGHT_COMPLETION_RATE = 0.2;
    private static final double WEIGHT_TIME_OF_DAY = 0.1;

    // Interface pour retourner les résultats de l'analyse
    public interface TaskAnalysisCallback {
        void onAnalysisComplete(List<Task_Class> prioritizedTasks);
        void onError(String errorMessage);
    }

    public TaskAnalyzer() {
        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();

        // Vérifier si l'utilisateur est connecté
        if (currentUser != null) {
            userId = currentUser.getUid();
            tasksReference = FirebaseDatabase.getInstance().getReference("Categorised Tasks").child(userId);
            completedTasksReference = FirebaseDatabase.getInstance().getReference("Task Progress").child(userId).child("Completed");
        }
        // Pas besoin d'initialiser les références si l'utilisateur n'est pas connecté
    }

    // Méthode principale pour analyser et prioriser les tâches
    public void analyzeTasks(final TaskAnalysisCallback callback) {
        // Vérifier si l'utilisateur est connecté
        if (currentUser == null) {
            callback.onError("Utilisateur non connecté");
            return;
        }

        // Récupérer toutes les tâches actives
        final List<Task_Class> allTasks = new ArrayList<>();
        final Map<String, Integer> categoryCompletionCount = new HashMap<>();
        final Map<String, Integer> timeOfDayPreference = new HashMap<>();

        // Récupérer d'abord les statistiques des tâches complétées
        completedTasksReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // Analyser les tâches complétées pour déterminer les préférences
                analyzeCompletedTasks(dataSnapshot, categoryCompletionCount, timeOfDayPreference);

                // Maintenant récupérer les tâches actives
                tasksReference.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        // Pour chaque catégorie
                        for (DataSnapshot categorySnapshot : dataSnapshot.getChildren()) {
                            String category = categorySnapshot.getKey();

                            // Pour chaque tâche dans cette catégorie
                            for (DataSnapshot taskSnapshot : categorySnapshot.getChildren()) {
                                Task_Class task = taskSnapshot.getValue(Task_Class.class);
                                if (task != null) {
                                    allTasks.add(task);
                                }
                            }
                        }

                        // Calculer les scores de priorité pour chaque tâche
                        calculatePriorityScores(allTasks, categoryCompletionCount, timeOfDayPreference);

                        // Trier les tâches par score de priorité
                        Collections.sort(allTasks, new Comparator<Task_Class>() {
                            @Override
                            public int compare(Task_Class t1, Task_Class t2) {
                                // Ordre décroissant (priorité la plus élevée en premier)
                                return Double.compare(t2.getPriorityScore(), t1.getPriorityScore());
                            }
                        });

                        // Retourner les tâches priorisées
                        callback.onAnalysisComplete(allTasks);
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        callback.onError("Erreur lors de la récupération des tâches: " + databaseError.getMessage());
                    }
                });
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                callback.onError("Erreur lors de la récupération des tâches complétées: " + databaseError.getMessage());
            }
        });
    }

    // Analyser les tâches complétées pour déterminer les préférences de l'utilisateur
    private void analyzeCompletedTasks(DataSnapshot dataSnapshot, Map<String, Integer> categoryCompletionCount,
                                       Map<String, Integer> timeOfDayPreference) {
        for (DataSnapshot taskSnapshot : dataSnapshot.getChildren()) {
            Task_Class task = taskSnapshot.getValue(Task_Class.class);
            if (task != null) {
                // Compter les complétions par catégorie
                String category = task.getCategory();
                if (categoryCompletionCount.containsKey(category)) {
                    categoryCompletionCount.put(category, categoryCompletionCount.get(category) + 1);
                } else {
                    categoryCompletionCount.put(category, 1);
                }

                // Analyser l'heure de la journée préférée
                try {
                    SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss", Locale.getDefault());
                    Date taskTime = sdf.parse(task.getStartTime());
                    Calendar cal = Calendar.getInstance();
                    cal.setTime(taskTime);
                    int hour = cal.get(Calendar.HOUR_OF_DAY);

                    // Diviser la journée en 4 périodes (matin, après-midi, soir, nuit)
                    String timeSlot;
                    if (hour >= 5 && hour < 12) {
                        timeSlot = "morning";
                    } else if (hour >= 12 && hour < 17) {
                        timeSlot = "afternoon";
                    } else if (hour >= 17 && hour < 22) {
                        timeSlot = "evening";
                    } else {
                        timeSlot = "night";
                    }

                    if (timeOfDayPreference.containsKey(timeSlot)) {
                        timeOfDayPreference.put(timeSlot, timeOfDayPreference.get(timeSlot) + 1);
                    } else {
                        timeOfDayPreference.put(timeSlot, 1);
                    }
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    // Calculer les scores de priorité pour chaque tâche
    private void calculatePriorityScores(List<Task_Class> tasks, Map<String, Integer> categoryCompletionCount,
                                         Map<String, Integer> timeOfDayPreference) {
        // Trouver le total des tâches complétées
        int totalCompletions = 0;
        for (Integer count : categoryCompletionCount.values()) {
            totalCompletions += count;
        }

        // Trouver la catégorie préférée
        String preferredCategory = "";
        int maxCount = 0;
        for (Map.Entry<String, Integer> entry : categoryCompletionCount.entrySet()) {
            if (entry.getValue() > maxCount) {
                maxCount = entry.getValue();
                preferredCategory = entry.getKey();
            }
        }

        // Trouver le moment préféré de la journée
        String preferredTimeSlot = "";
        maxCount = 0;
        for (Map.Entry<String, Integer> entry : timeOfDayPreference.entrySet()) {
            if (entry.getValue() > maxCount) {
                maxCount = entry.getValue();
                preferredTimeSlot = entry.getKey();
            }
        }

        // Calculer le score pour chaque tâche
        for (Task_Class task : tasks) {
            double score = 0.0;

            // Facteur 1: Date d'échéance (plus proche = plus prioritaire)
            try {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                Date dueDate = sdf.parse(task.getDueDate());
                Date today = new Date();

                // Calculer la différence en jours
                long diffInMillies = dueDate.getTime() - today.getTime();
                long diffInDays = diffInMillies / (24 * 60 * 60 * 1000);

                // Normaliser: 0 jours = 1.0, 7 jours ou plus = 0.0
                double dueDateScore = Math.max(0.0, 1.0 - (diffInDays / 7.0));
                score += WEIGHT_DUE_DATE * dueDateScore;
            } catch (ParseException e) {
                e.printStackTrace();
            }

            // Facteur 2: Préférence de catégorie
            if (task.getCategory().equals(preferredCategory)) {
                score += WEIGHT_CATEGORY_PREFERENCE;
            }

            // Facteur 3: Taux de complétion pour cette catégorie
            if (categoryCompletionCount.containsKey(task.getCategory()) && totalCompletions > 0) {
                double completionRate = (double) categoryCompletionCount.get(task.getCategory()) / totalCompletions;
                score += WEIGHT_COMPLETION_RATE * completionRate;
            }

            // Facteur 4: Moment de la journée
            try {
                SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss", Locale.getDefault());
                Date taskTime = sdf.parse(task.getStartTime());
                Calendar cal = Calendar.getInstance();
                cal.setTime(taskTime);
                int hour = cal.get(Calendar.HOUR_OF_DAY);

                String taskTimeSlot;
                if (hour >= 5 && hour < 12) {
                    taskTimeSlot = "morning";
                } else if (hour >= 12 && hour < 17) {
                    taskTimeSlot = "afternoon";
                } else if (hour >= 17 && hour < 22) {
                    taskTimeSlot = "evening";
                } else {
                    taskTimeSlot = "night";
                }

                if (taskTimeSlot.equals(preferredTimeSlot)) {
                    score += WEIGHT_TIME_OF_DAY;
                }
            } catch (ParseException e) {
                e.printStackTrace();
            }

            // Enregistrer le score dans l'objet tâche
            task.setPriorityScore(score);
        }
    }

    // Vérifier si l'utilisateur est connecté
    public boolean isUserLoggedIn() {
        return currentUser != null;
    }
}