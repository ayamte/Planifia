package com.example.luna;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class NotificationReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        String taskTitle = intent.getStringExtra("taskTitle");
        String taskDescription = intent.getStringExtra("taskDescription");
        int notificationId = intent.getIntExtra("notificationId", 0);

        SmartNotificationManager notificationManager = new SmartNotificationManager(context);
        notificationManager.showTaskReminder(taskTitle, taskDescription, notificationId);
    }
}