package com.example.planifia;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class view_events_activity extends AppCompatActivity {

    private static final String TAG = "view_events_activity";

    RecyclerView recyclerViewEvents;
    myEvents_adapter myEvents_adapter;
    ArrayList<Event_Class> myEventArrayList = new ArrayList<>();
    DatabaseReference myEventsRef;
    ProgressBar progressBar;
    TextView textViewNoEvents;
    String category;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_events);

        // Récupérer la catégorie depuis l'intent
        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            category = bundle.getString("category");
        }

        // Initialiser les vues
        recyclerViewEvents = findViewById(R.id.recyclerViewEvents);
        progressBar = findViewById(R.id.progressBarEvents);
        textViewNoEvents = findViewById(R.id.textViewNoEvents);

        // Configurer le RecyclerView
        myEvents_adapter = new myEvents_adapter(myEventArrayList);
        recyclerViewEvents.setAdapter(myEvents_adapter);
        recyclerViewEvents.setLayoutManager(new LinearLayoutManager(this));

        // Ajouter une décoration au RecyclerView
        int spacingInPixels = getResources().getDimensionPixelSize(R.dimen.item_spacing);
        recyclerViewEvents.addItemDecoration(new RecyclerViewItemDecorationClass(this, spacingInPixels));

        // Obtenir l'ID de l'utilisateur
        FirebaseAuth myAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = myAuth.getCurrentUser();
        String userId = currentUser.getUid();

        // Initialiser la référence Firebase
        myEventsRef = FirebaseDatabase.getInstance().getReference("Events").child(userId);

        // Charger les événements
        loadEvents();
    }

    private void loadEvents() {
        progressBar.setVisibility(View.VISIBLE);
        Log.d(TAG, "Loading events for category: " + category);

        myEventsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                myEventArrayList.clear();
                Log.d(TAG, "Number of date entries: " + snapshot.getChildrenCount());

                // Parcourir tous les jours
                for (DataSnapshot dateSnapshot : snapshot.getChildren()) {
                    String date = dateSnapshot.getKey();
                    Log.d(TAG, "Processing date: " + date);

                    // Parcourir tous les événements de chaque jour
                    for (DataSnapshot eventSnapshot : dateSnapshot.getChildren()) {
                        String eventKey = eventSnapshot.getKey();
                        Log.d(TAG, "Processing event with key: " + eventKey);

                        try {
                            // Log the raw data
                            Log.d(TAG, "Raw event data: " + eventSnapshot.getValue());

                            Event_Class event = eventSnapshot.getValue(Event_Class.class);
                            if (event == null) {
                                Log.e(TAG, "Event deserialized as null");
                                continue;
                            }

                            Log.d(TAG, "Event category: " + event.getCategory() + ", Looking for: " + category);

                            if (event.getCategory().equals(category)) {
                                myEventArrayList.add(event);
                                Log.d(TAG, "Événement ajouté: " + event.getTitle());
                            } else {
                                Log.d(TAG, "Event category doesn't match: " + event.getCategory() + " vs " + category);
                            }
                        } catch (Exception e) {
                            Log.e(TAG, "Erreur lors de la récupération de l'événement: " + e.getMessage(), e);
                        }
                    }
                }

                // Mettre à jour l'interface utilisateur
                progressBar.setVisibility(View.GONE);
                Log.d(TAG, "Total events found: " + myEventArrayList.size());

                if (myEventArrayList.isEmpty()) {
                    textViewNoEvents.setVisibility(View.VISIBLE);
                    recyclerViewEvents.setVisibility(View.GONE);
                    Log.d(TAG, "No events to display");
                } else {
                    textViewNoEvents.setVisibility(View.GONE);
                    recyclerViewEvents.setVisibility(View.VISIBLE);
                    myEvents_adapter.setData(myEventArrayList);
                    Log.d(TAG, "Events displayed in RecyclerView");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                progressBar.setVisibility(View.GONE);
                textViewNoEvents.setVisibility(View.VISIBLE);
                Log.e(TAG, "Erreur Firebase: " + error.getMessage());
            }
        });
    }}