package com.example.luna;

import android.app.Dialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;

public class Task_Adapter extends RecyclerView.Adapter<Task_Adapter.DataViewHolder> {

    public interface interface_adapter {
        // Implement the refresh logic here
        void onSaveInterface();
    }

    //define an interface
    public interface OnSaveButtonClickListener {
        void onSaveButtonClicked();
    }

    private interface_adapter interface_adapter;
    //member variable for the interface
    private OnSaveButtonClickListener onSaveButtonClickListener;

    ArrayList<Task_Class> myTasksArrayList=new ArrayList<>();
    Context myContext;
    TextView textViewTaskTitleDialog;
    Dialog myDialog;
    RadioGroup myRadioGroup;
    Button btnSaveTaskStatusDialog;
    String newStatus="";
    String userId;

    public Task_Adapter(view_tasks_activity view_tasks_activity, TextView textViewTaskTitleDialog, Dialog myTaskDialog, RadioGroup radioGroupOptions, Button btnSaveTaskStatusDialog, OnSaveButtonClickListener onSaveButtonClickListener, interface_adapter interface_adapter) {
        myContext = view_tasks_activity;
        this.textViewTaskTitleDialog = textViewTaskTitleDialog;
        this.myDialog = myTaskDialog;
        this.myRadioGroup=radioGroupOptions;
        this.btnSaveTaskStatusDialog = btnSaveTaskStatusDialog;

        //initializing the interface
        this.onSaveButtonClickListener = onSaveButtonClickListener;
        this.interface_adapter = interface_adapter;
    }

    public void setData(ArrayList<Task_Class> tasksAppointmentsList) {
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        assert currentUser != null;
        userId = currentUser.getUid();
        this.myTasksArrayList = tasksAppointmentsList;
        notifyDataSetChanged();
    }

    public static class DataViewHolder extends RecyclerView.ViewHolder {

        TextView textViewTaskProgress, textViewTaskTitle, textViewTaskDescription, textViewTaskStartTime,textViewTaskEndTime, textViewTaskDueDate;
        Button btnUpdateTask;

        public DataViewHolder(@NonNull View itemView) {
            super(itemView);

            textViewTaskProgress = (TextView)  itemView.findViewById(R.id.textViewTaskStatus_REC);
            textViewTaskTitle =(TextView)  itemView.findViewById(R.id.textViewTaskTitle_REC);
            textViewTaskDescription = (TextView)  itemView.findViewById(R.id.textViewTaskDescription_REC);
            textViewTaskDueDate = (TextView)  itemView.findViewById(R.id.textViewTaskDueDate_REC);
            textViewTaskStartTime = (TextView)  itemView.findViewById(R.id.textViewTaskStartTime_REC);
            textViewTaskEndTime = (TextView)   itemView.findViewById(R.id.textViewTaskEndTime_REC );

            btnUpdateTask = (Button)  itemView.findViewById(R.id.buttonUpdateStatus_REC);
        }
    }

    @NonNull
    @Override
    public DataViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.recyclerview_tasks_layout,parent, false);
        return new Task_Adapter.DataViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DataViewHolder holder, int position) {

        Task_Class taskObj = myTasksArrayList.get(position);

        holder.textViewTaskProgress.setText(taskObj.getStatus());
        holder.textViewTaskTitle.setText(taskObj.getTitle());
        holder.textViewTaskDescription.setText(taskObj.getDescription());
        holder.textViewTaskDueDate.setText(taskObj.getDueDate());
        holder.textViewTaskStartTime.setText(taskObj.getStartTime());
        holder.textViewTaskEndTime.setText(taskObj.getEndTime());

        holder.btnUpdateTask.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //set Task Title to the Dialog
                textViewTaskTitleDialog.setText("Task Title : "+taskObj.getTitle());
                //call method to display the dialog
                showEventTaskDialog();

                //button to update task status
                btnSaveTaskStatusDialog.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                        FirebaseAuth mAuth = FirebaseAuth.getInstance();
                        FirebaseUser currentUser = mAuth.getCurrentUser();
                        assert currentUser != null;
                        String userId = currentUser.getUid();

                        // Correction: Utiliser le titre de la tâche comme clé au lieu de newDateTime
                        DatabaseReference taskRef = FirebaseDatabase.getInstance().getReference("Categorised Tasks")
                                .child(userId)
                                .child(taskObj.getCategory())
                                .child(taskObj.getTitle());

                        if(!newStatus.isEmpty()) {
                            // 1. Pour les tâches annulées: supprimer complètement de la base de données
                            if(newStatus.equals("Cancelled")) {
                                taskRef.removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if(task.isSuccessful()) {
                                            Toast.makeText(myContext, "Task cancelled and removed", Toast.LENGTH_SHORT).show();

                                            // Notify the interface to refresh the view
                                            if (interface_adapter != null) {
                                                interface_adapter.onSaveInterface();
                                            }

                                            myDialog.dismiss();
                                        }
                                    }
                                });
                            }
                            // 2. Pour les tâches complétées: déplacer vers la section "Completed" et supprimer de la liste principale
                            else if(newStatus.equals("Completed")) {
                                taskRef.removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if(task.isSuccessful()) {
                                            // Create a new entry in the "Completed" section
                                            DatabaseReference completedTaskRef = FirebaseDatabase.getInstance().getReference("Task Progress")
                                                    .child(userId)
                                                    .child("Completed")
                                                    .child(taskObj.getTitle());

                                            Task_Class newTaskObj = new Task_Class(
                                                    taskObj.getTitle(),
                                                    taskObj.getDescription(),
                                                    taskObj.getStartTime(),
                                                    taskObj.getDueDate(),
                                                    taskObj.getCategory(),
                                                    "Completed",
                                                    taskObj.getDateTime(),
                                                    taskObj.getEndTime()
                                            );

                                            completedTaskRef.setValue(newTaskObj).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                @Override
                                                public void onComplete(@NonNull Task<Void> task) {
                                                    Toast.makeText(myContext, "Task marked as completed", Toast.LENGTH_SHORT).show();

                                                    // Notify the interface to refresh the view
                                                    if (interface_adapter != null) {
                                                        interface_adapter.onSaveInterface();
                                                    }

                                                    myDialog.dismiss();
                                                }
                                            });
                                        }
                                    }
                                });
                            }
                            // 3. Pour les tâches différées et en cours: simplement mettre à jour le statut sans les déplacer
                            else if(newStatus.equals("Deferred") || newStatus.equals("In-Progress")) {
                                // Just update the status field
                                taskRef.child("status").setValue(newStatus).addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if(task.isSuccessful()) {
                                            Toast.makeText(myContext, "Updated status successfully", Toast.LENGTH_SHORT).show();
                                            holder.textViewTaskProgress.setText(newStatus);

                                            // Notify the main activity about the save button click
                                            if (onSaveButtonClickListener != null) {
                                                onSaveButtonClickListener.onSaveButtonClicked();
                                            }

                                            myDialog.dismiss();
                                        }
                                    }
                                });
                            }
                        }
                        else {
                            myDialog.dismiss();
                        }
                    }
                });
            }
        });
    }

    private void showEventTaskDialog() {
        //set Task Title to the Dialog
        myDialog.show();

        myRadioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int checkedId) {
                // Check which radio button was selected
                switch (checkedId) {
                    case R.id.radioButtonInProgress:
                        newStatus = "In-Progress";
                        break;
                    case R.id.radioButtonCompleted:
                        newStatus="Completed";
                        break;
                    case R.id.radioButtonDeferred:
                        newStatus="Deferred";
                        break;
                    case R.id.radioButtonCancelled:
                        newStatus="Cancelled";
                        break;
                }
            }
        });

        //button to update task status
        btnSaveTaskStatusDialog.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Cette méthode est vide car le gestionnaire d'événements est défini ailleurs
            }
        });
    }

    @Override
    public int getItemCount() {
        return myTasksArrayList.size();
    }
}