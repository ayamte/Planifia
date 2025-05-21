package com.example.luna;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.DatePickerDialog;
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

public class Create_Event extends AppCompatActivity {

    String eventLocationName = "";

    com.google.api.services.calendar.Calendar mService;
    EditText editTextTaskTitle, editTextTaskDescription, editTextLocation;
    Spinner spinnerTaskCategory;
    TextView textViewDate, textViewStartTime, textViewEndTime;
    Button buttonSaveTask;
    // Footer elements
    ImageView buttonHome, buttonSearchTask;
    FrameLayout buttonAddTask;

    FirebaseAuth userAuth;
    String userId;
    DatabaseReference eventReference;
    private String[] categoryArray = {
            "Select Category",
            "Personal",
            "Finance",
            "Leisure",
            "Health",
            "Self Care",
            "Work"
    };

    ProgressBar progressBar;

    private String eventTitle = "", eventDescription = "", eventDueDate = "", eventStartTime = "", eventEndTime = "", eventCategory = "";

    private DatePickerDialog datePickerDialog;
    private TimePickerDialog timePickerDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_event);

        // Initialize form elements
        editTextTaskTitle = (EditText) this.findViewById(R.id.editTextCreateEventTitle);
        editTextTaskDescription = (EditText) this.findViewById(R.id.editTextCreateEventDescription);
        editTextLocation = (EditText) this.findViewById(R.id.editTextEventLocation);
        progressBar = (ProgressBar) this.findViewById(R.id.progressBarSaveTask);

        // Initialize Firebase
        userAuth = FirebaseAuth.getInstance();
        FirebaseUser user = userAuth.getCurrentUser();
        assert user != null;
        userId = user.getUid();
        eventReference = FirebaseDatabase.getInstance().getReference("Events");

        // Initialize date and time views
        textViewDate = (TextView) this.findViewById(R.id.textViewCreateTaskDate);
        textViewStartTime = (TextView) this.findViewById(R.id.textViewCreateTaskStartTime);
        textViewEndTime = (TextView) this.findViewById(R.id.textViewCreateEventEndTime);

        // Initialize save button
        buttonSaveTask = (Button) this.findViewById(R.id.buttonCreateEventSave);
        buttonSaveTask.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //call method to save new task
                saveTask();
            }
        });

        // Initialize category spinner
        spinnerTaskCategory = (Spinner) this.findViewById(R.id.spinnerCreateEventCategory);
        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, categoryArray);
        arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerTaskCategory.setAdapter(arrayAdapter);
        spinnerTaskCategory.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                String myText = spinnerTaskCategory.getSelectedItem().toString().trim();
                if (myText.equals("Select Category".trim())) {
                    //Nothing
                } else {
                    Toast.makeText(Create_Event.this, "You have Selected " + myText, Toast.LENGTH_SHORT).show();
                    eventCategory = myText;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                // Do nothing
            }
        });

        // Initialize footer elements
        buttonHome = findViewById(R.id.buttonHome);
        buttonAddTask = findViewById(R.id.buttonAddTask);
        buttonSearchTask = findViewById(R.id.buttonSearchTask);

        // Setup footer navigation
        setupFooter();

        // Setting date
        textViewDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Create a Calendar instance to get the current date
                Calendar calendar = Calendar.getInstance();
                int currentYear = calendar.get(Calendar.YEAR);
                int currentMonth = calendar.get(Calendar.MONTH);
                int currentDay = calendar.get(Calendar.DAY_OF_MONTH);

                // Create a DatePickerDialog and set the initial date
                DatePickerDialog datePickerDialog = new DatePickerDialog(
                        Create_Event.this,
                        new DatePickerDialog.OnDateSetListener() {
                            @Override
                            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                                // Format the date as needed (e.g., "YYYY-MM-DD")
                                String myMonth, myDay;
                                if ((month + 1) < 10) {
                                    myMonth = "0" + (month + 1);
                                } else {
                                    myMonth = String.valueOf(month + 1);
                                }

                                if (dayOfMonth < 10) {
                                    myDay = "0" + dayOfMonth;
                                } else {
                                    myDay = String.valueOf(dayOfMonth);
                                }

                                String formattedDate = year + "-" + myMonth + "-" + myDay;
                                textViewDate.setText(formattedDate);
                                eventDueDate = formattedDate;
                            }
                        },
                        currentYear,
                        currentMonth,
                        currentDay
                );

                // Show the DatePickerDialog
                datePickerDialog.show();
            }
        });

        // Setting start time
        textViewStartTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Create a Calendar instance to get the current time
                Calendar calendar = Calendar.getInstance();
                int currentHour = calendar.get(Calendar.HOUR_OF_DAY);
                int currentMinute = calendar.get(Calendar.MINUTE);

                // Create a TimePickerDialog and set the initial time
                TimePickerDialog timePickerDialog = new TimePickerDialog(
                        Create_Event.this,
                        new TimePickerDialog.OnTimeSetListener() {
                            @Override
                            public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                                String myHour, myMinute;
                                //if minutes are less than 10
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
                                eventStartTime = myHour + ":" + myMinute;
                            }
                        },
                        currentHour,
                        currentMinute,
                        true // true if you want to use the 24-hour format, false for 12-hour format
                );

                // Show the TimePickerDialog
                timePickerDialog.show();
            }
        });

        // Setting end time
        textViewEndTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Create a Calendar instance to get the current time
                Calendar calendar = Calendar.getInstance();
                int currentHour = calendar.get(Calendar.HOUR_OF_DAY);
                int currentMinute = calendar.get(Calendar.MINUTE);

                // Create a TimePickerDialog and set the initial time
                TimePickerDialog timePickerDialog = new TimePickerDialog(
                        Create_Event.this,
                        new TimePickerDialog.OnTimeSetListener() {
                            @Override
                            public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                                String myHour, myMinute;
                                //if minutes are less than 10
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
                                eventEndTime = myHour + ":" + myMinute;
                            }
                        },
                        currentHour,
                        currentMinute,
                        true // true if you want to use the 24-hour format, false for 12-hour format
                );

                // Show the TimePickerDialog
                timePickerDialog.show();
            }
        });
    }

    // Setup footer navigation
    private void setupFooter() {
        buttonHome.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Create_Event.this, Home_Page.class);
                startActivity(intent);
                overridePendingTransition(R.anim.slide_in, R.anim.slide_out);
                finish();
            }
        });

        buttonAddTask.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Déjà dans l'écran de création
            }
        });

        buttonSearchTask.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Create_Event.this, Search_Task.class);
                startActivity(intent);
                overridePendingTransition(R.anim.slide_in, R.anim.slide_out);
            }
        });
    }

    // Save event to Firebase
    private void saveTask() {
        eventTitle = editTextTaskTitle.getText().toString().trim();
        eventDescription = editTextTaskDescription.getText().toString().trim();
        eventLocationName = editTextLocation.getText().toString().trim();

        if (eventTitle.isEmpty()) {
            editTextTaskTitle.setError("Cannot be blank!");
            editTextTaskTitle.requestFocus();
        } else if (eventDescription.isEmpty()) {
            editTextTaskDescription.setError("Cannot be blank!");
            editTextTaskDescription.requestFocus();
        } else if (eventStartTime.isEmpty()) {
            textViewStartTime.setError("Cannot be blank!");
            textViewStartTime.requestFocus();
        } else if (eventEndTime.isEmpty()) {
            textViewEndTime.setError("Cannot be blank");
        } else if (eventDueDate.isEmpty()) {
            textViewDate.setError("Cannot be blank");
        } else if (eventCategory.isEmpty()) {
            Toast.makeText(this, "Choose Task Category", Toast.LENGTH_SHORT).show();
        } else if (eventLocationName.isEmpty()) {
            editTextLocation.setError("Cannot be blank");
        } else {
            progressBar.setVisibility(View.VISIBLE);
            Event_Class newEvent = new Event_Class(eventTitle, eventDescription, eventDueDate, eventStartTime, eventEndTime, eventCategory, eventLocationName);
            eventReference.child(userId).child(eventDueDate).child(eventTitle).setValue(newEvent).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (task.isSuccessful()) {
                        //event data will be saved successfully
                        progressBar.setVisibility(View.GONE);
                        Toast.makeText(Create_Event.this, "Event Saved Successfully", Toast.LENGTH_SHORT).show();

                        // Modification: Rediriger vers Home_Page au lieu de APIMainActivity
                        Intent myIntent = new Intent(Create_Event.this, Home_Page.class);
                        startActivity(myIntent);
                        overridePendingTransition(R.anim.slide_in, R.anim.slide_out);
                        finish(); // Fermer l'activité actuelle
                    } else {
                        progressBar.setVisibility(View.GONE);
                        Toast.makeText(Create_Event.this, "Failed to save event", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
    }
}