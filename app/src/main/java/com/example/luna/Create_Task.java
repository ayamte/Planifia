package com.example.luna;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Calendar;

public class Create_Task extends AppCompatActivity {

   Spinner spinnerTaskCategory;
   EditText editTextTaskDescription, editTextTaskTitle;
   TextView textViewTaskDate, textViewStartTime, textViewEndTime;
   ProgressBar progressBar;
   DatabaseReference taskReference, createdTaskRef;
   Button btnSaveTask;
   // Éléments du footer
   ImageView buttonHome, buttonSearchTask;
   FrameLayout buttonAddTask;

   private DatePickerDialog datePickerDialog;
   private TimePickerDialog timePickerDialog;

   private String taskTitle = "", taskDescription = "", taskDate = "", taskStartTime = "", taskEndTime = "", taskCategory = "";

   FirebaseAuth userAuth;

   private String[] categoryArray = {
           "Select Category",
           "Personal",
           "Finance",
           "Leisure",
           "Health",
           "Self Care",
           "Work"
   };

   String userId;

   @Override
   protected void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);
      setContentView(R.layout.activity_create_task);

      userAuth = FirebaseAuth.getInstance();
      FirebaseUser user = userAuth.getCurrentUser();
      assert user != null;
      userId = user.getUid();

      taskReference = FirebaseDatabase.getInstance().getReference("Tasks");
      createdTaskRef = FirebaseDatabase.getInstance().getReference("Created Tasks");

      editTextTaskTitle = (EditText) this.findViewById(R.id.editTextCreateTaskTitle);
      editTextTaskDescription = (EditText) this.findViewById(R.id.editTextCreateTaskDescription);

      textViewTaskDate = (TextView) this.findViewById(R.id.textViewCreateTaskDate);
      textViewStartTime = (TextView) this.findViewById(R.id.textViewCreateTaskStartTime);
      textViewEndTime = (TextView) this.findViewById(R.id.textViewCreateTaskEndTime);

      progressBar = (ProgressBar) this.findViewById(R.id.progressBarSaveTaskNew);

      // Configuration des sélecteurs de date et heure
      setupDateTimePickers();

      // Configuration du spinner de catégorie
      setupCategorySpinner();

      // Configuration du bouton de sauvegarde
      btnSaveTask = (Button) this.findViewById(R.id.buttonCreateTaskSave);
      btnSaveTask.setOnClickListener(new View.OnClickListener() {
         @Override
         public void onClick(View view) {
            saveTask();
         }
      });

      // Initialisation du footer
      initializeFooter();
   }

   private void setupDateTimePickers() {
      // Configuration du sélecteur de date
      textViewTaskDate.setOnClickListener(new View.OnClickListener() {
         @Override
         public void onClick(View view) {
            final Calendar calendar = Calendar.getInstance();
            int day = calendar.get(Calendar.DAY_OF_MONTH);
            int month = calendar.get(Calendar.MONTH);
            int year = calendar.get(Calendar.YEAR);

            datePickerDialog = new DatePickerDialog(Create_Task.this, new DatePickerDialog.OnDateSetListener() {
               @Override
               public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                  String myMonth;
                  if (month < 9) { // Month is 0-based, so add 1 for display
                     myMonth = "0" + Integer.toString(month + 1);
                  } else {
                     myMonth = Integer.toString(month + 1);
                  }

                  String myDay;
                  if (dayOfMonth < 10) {
                     myDay = "0" + Integer.toString(dayOfMonth);
                  } else {
                     myDay = Integer.toString(dayOfMonth);
                  }

                  textViewTaskDate.setText(year + "-" + myMonth + "-" + myDay);
                  taskDate = year + "-" + myMonth + "-" + myDay;
               }
            }, year, month, day);
            datePickerDialog.show();
         }
      });

      // Configuration du sélecteur d'heure de début
      textViewStartTime.setOnClickListener(new View.OnClickListener() {
         @Override
         public void onClick(View view) {
            Calendar calendar = Calendar.getInstance();
            int currentHour = calendar.get(Calendar.HOUR_OF_DAY);
            int currentMinute = calendar.get(Calendar.MINUTE);

            TimePickerDialog timePickerDialog = new TimePickerDialog(
                    Create_Task.this,
                    new TimePickerDialog.OnTimeSetListener() {
                       @Override
                       public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                          String myHour, myMinute;
                          if (minute < 10) {
                             myMinute = "0" + Integer.toString(minute);
                          } else {
                             myMinute = Integer.toString(minute);
                          }

                          if (hourOfDay < 10) {
                             myHour = "0" + Integer.toString(hourOfDay);
                          } else {
                             myHour = Integer.toString(hourOfDay);
                          }

                          textViewStartTime.setText(myHour + ":" + myMinute + " hours");
                          taskStartTime = myHour + ":" + myMinute + ":00";
                       }
                    },
                    currentHour,
                    currentMinute,
                    true
            );
            timePickerDialog.show();
         }
      });

      // Configuration du sélecteur d'heure de fin
      textViewEndTime.setOnClickListener(new View.OnClickListener() {
         @Override
         public void onClick(View view) {
            Calendar calendar = Calendar.getInstance();
            int currentHour = calendar.get(Calendar.HOUR_OF_DAY);
            int currentMinute = calendar.get(Calendar.MINUTE);

            TimePickerDialog timePickerDialog = new TimePickerDialog(
                    Create_Task.this,
                    new TimePickerDialog.OnTimeSetListener() {
                       @Override
                       public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                          String myHour, myMinute;
                          if (minute < 10) {
                             myMinute = "0" + Integer.toString(minute);
                          } else {
                             myMinute = Integer.toString(minute);
                          }

                          if (hourOfDay < 10) {
                             myHour = "0" + Integer.toString(hourOfDay);
                          } else {
                             myHour = Integer.toString(hourOfDay);
                          }

                          textViewEndTime.setText(myHour + ":" + myMinute + " hours");
                          taskEndTime = myHour + ":" + myMinute + ":00";
                       }
                    },
                    currentHour,
                    currentMinute,
                    true
            );
            timePickerDialog.show();
         }
      });
   }

   private void setupCategorySpinner() {
      spinnerTaskCategory = (Spinner) this.findViewById(R.id.spinnerCreateTaskCategory);
      ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, categoryArray);
      arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
      spinnerTaskCategory.setAdapter(arrayAdapter);

      spinnerTaskCategory.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
         @Override
         public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
            String myText = spinnerTaskCategory.getSelectedItem().toString().trim();
            if (myText.equals("Select Category".trim())) {
               // Ne rien faire
            } else {
               Toast.makeText(Create_Task.this, "You have Selected " + myText, Toast.LENGTH_SHORT).show();
               taskCategory = myText;
            }
         }

         @Override
         public void onNothingSelected(AdapterView<?> adapterView) {
            // Ne rien faire
         }
      });
   }

   private void saveTask() {
      // Récupérer les valeurs des champs
      taskTitle = editTextTaskTitle.getText().toString().trim();
      taskDescription = editTextTaskDescription.getText().toString().trim();

      // Vérifier que tous les champs sont remplis
      if (taskTitle.isEmpty()) {
         editTextTaskTitle.setError("Cannot be blank!");
         editTextTaskTitle.requestFocus();
      } else if (taskDescription.isEmpty()) {
         editTextTaskDescription.setError("Cannot be blank!");
         editTextTaskDescription.requestFocus();
      } else if (taskDate.isEmpty()) {
         textViewTaskDate.setError("Cannot be blank!");
         textViewTaskDate.requestFocus();
      } else if (taskStartTime.isEmpty()) {
         textViewStartTime.setError("Cannot be blank!");
         textViewStartTime.requestFocus();
      } else if (taskEndTime.isEmpty()) {
         textViewEndTime.setError("Cannot be blank!");
         textViewEndTime.requestFocus();
      } else if (taskCategory.isEmpty()) {
         Toast.makeText(this, "Choose Task Category", Toast.LENGTH_SHORT).show();
      } else {
         // Tous les champs sont remplis, sauvegarder la tâche
         progressBar.setVisibility(View.VISIBLE);

         // Définir le statut et le dateTimeString comme dans votre code original
         String status = "Pending"; // Statut par défaut pour les nouvelles tâches
         String dateTimeString = taskDate + "T" + taskStartTime; // Format comme dans votre code original

         // Créer un nouvel objet Task_Class avec tous les paramètres requis
         Task_Class taskObj = new Task_Class(taskTitle, taskDescription, taskStartTime, taskDate, taskCategory, status, dateTimeString, taskEndTime);

         // Sauvegarder la tâche dans Firebase
         FirebaseDatabase.getInstance().getReference("Categorised Tasks").child(userId).child(taskCategory).child(taskTitle).setValue(taskObj)
                 .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                       if (task.isSuccessful()) {
                          // Tâche sauvegardée avec succès
                          progressBar.setVisibility(View.GONE);
                          Toast.makeText(Create_Task.this, "Task Saved Successfully", Toast.LENGTH_SHORT).show();

                          // Retourner à la page d'accueil
                          Intent intent = new Intent(Create_Task.this, Home_Page.class);
                          startActivity(intent);
                          overridePendingTransition(R.anim.slide_in, R.anim.slide_out);
                          finish();
                       } else {
                          // Échec de la sauvegarde
                          progressBar.setVisibility(View.GONE);
                          Toast.makeText(Create_Task.this, "Failed to save task!", Toast.LENGTH_SHORT).show();
                       }
                    }
                 });
      }
   }

   // Initialiser les éléments du footer
   private void initializeFooter() {
      buttonHome = findViewById(R.id.buttonHome);
      buttonAddTask = findViewById(R.id.buttonAddTask);
      buttonSearchTask = findViewById(R.id.buttonSearchTask);

      // Configurer le footer
      setupFooter();
   }

   // Configurer les actions du footer
   private void setupFooter() {
      // Bouton Home
      buttonHome.setOnClickListener(new View.OnClickListener() {
         @Override
         public void onClick(View view) {
            Intent intent = new Intent(Create_Task.this, Home_Page.class);
            startActivity(intent);
            finish();
         }
      });

      // Bouton Add Task
      buttonAddTask.setOnClickListener(new View.OnClickListener() {
         @Override
         public void onClick(View view) {
            showEventTaskDialog();
         }
      });

      // Bouton Search Task
      buttonSearchTask.setOnClickListener(new View.OnClickListener() {
         @Override
         public void onClick(View view) {
            Intent intent = new Intent(Create_Task.this, Search_Task.class);
            startActivity(intent);
            overridePendingTransition(R.anim.slide_in, R.anim.slide_out);
         }
      });
   }

   // Afficher le dialogue de sélection entre tâche et événement
   private void showEventTaskDialog() {
      Dialog categoryDialog = new Dialog(this);
      categoryDialog.setContentView(R.layout.event_task_dialog_layout);

      View viewTaskDialog = categoryDialog.findViewById(R.id.viewTaskDialog);
      View viewEventDialog = categoryDialog.findViewById(R.id.viewEventDialog);

      viewEventDialog.setOnClickListener(new View.OnClickListener() {
         @Override
         public void onClick(View view) {
            Intent intent = new Intent(Create_Task.this, Create_Event.class);
            startActivity(intent);
            overridePendingTransition(R.anim.slide_in, R.anim.slide_out);
            categoryDialog.dismiss();
         }
      });

      viewTaskDialog.setOnClickListener(new View.OnClickListener() {
         @Override
         public void onClick(View view) {
            Intent intent = new Intent(Create_Task.this, Create_Task.class);
            startActivity(intent);
            overridePendingTransition(R.anim.slide_in, R.anim.slide_out);
            categoryDialog.dismiss();
         }
      });

      categoryDialog.show();
   }
}