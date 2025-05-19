package com.example.luna;

import android.app.AlarmManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import androidx.core.app.NotificationCompat;

import java.util.Calendar;
import java.util.List;
import java.util.Random;

public class SmartNotificationManager {

    private static final String CHANNEL_ID = "smart_reminder_channel";
    private static final String CHANNEL_NAME = "Smart Reminders";
    private static final String CHANNEL_DESCRIPTION = "AI-powered task reminders";

    private Context context;
    private TaskAnalyzer taskAnalyzer;

    public SmartNotificationManager(Context context) {
        this.context = context;
        this.taskAnalyzer = new TaskAnalyzer();
        createNotificationChannel();
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    CHANNEL_NAME,
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            channel.setDescription(CHANNEL_DESCRIPTION);

            NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    public void scheduleSmartReminders() {
        taskAnalyzer.analyzeTasks(new TaskAnalyzer.TaskAnalysisCallback() {
            @Override
            public void onAnalysisComplete(List<Task_Class> prioritizedTasks) {
                if (!prioritizedTasks.isEmpty()) {
                    // Prendre les 3 tâches les plus prioritaires
                    int tasksToRemind = Math.min(3, prioritizedTasks.size());
                    for (int i = 0; i < tasksToRemind; i++) {
                        Task_Class task = prioritizedTasks.get(i);
                        scheduleReminderForTask(task, i);
                    }
                }
            }
        });
    }

    private void scheduleReminderForTask(Task_Class task, int taskIndex) {
        // Calculer un moment optimal pour rappeler cette tâche
        Calendar calendar = Calendar.getInstance();

        // Ajouter une heure aléatoire dans les prochaines 24 heures
        // Dans une vraie implémentation, cela serait basé sur les habitudes de l'utilisateur
        Random random = new Random();
        int hoursToAdd = random.nextInt(24) + 1;
        calendar.add(Calendar.HOUR_OF_DAY, hoursToAdd);

        // Créer l'intent pour la notification
        Intent intent = new Intent(context, NotificationReceiver.class);
        intent.putExtra("taskTitle", task.getTitle());
        intent.putExtra("taskDescription", task.getDescription());
        intent.putExtra("notificationId", taskIndex);

        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context,
                taskIndex,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        // Programmer l'alarme
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        alarmManager.set(
                AlarmManager.RTC_WAKEUP,
                calendar.getTimeInMillis(),
                pendingIntent
        );
    }

    public void showTaskReminder(String taskTitle, String taskDescription, int notificationId) {
        Intent intent = new Intent(context, PrioritizedTasksActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                context,
                0,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_ai)
                .setContentTitle("Prioritized Task: " + taskTitle)
                .setContentText(taskDescription)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);

        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(notificationId, builder.build());
    }
}