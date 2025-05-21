package com.example.luna;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.squareup.picasso.Picasso;

import android.content.SharedPreferences;
import android.content.Context;
import android.widget.Button;
import android.widget.FrameLayout;

public class Home_Page extends AppCompatActivity {
    View viewMyEvents, viewMyTasks, viewMyTrack, viewAIPriorities, viewHomeUserProfile;
    FrameLayout buttonAddTask;
    ImageView buttonHome, buttonSearchTask, imageViewUserIcon;

    FirebaseAuth firebaseAuth;
    FirebaseUser firebaseUser;
    DatabaseReference userReference;
    private static final int REQUEST_GOOGLE_CALENDAR = 1;

    TextView textViewUserWelcomeText;
    ImageView buttonLogout;

    private static final String TAG = "Home_Page"; // Tag pour les logs

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_page);

        Log.d(TAG, "onCreate: Initialisation de Home_Page");

        // Initialiser Firebase
        firebaseAuth = FirebaseAuth.getInstance();
        firebaseUser = firebaseAuth.getCurrentUser();
        userReference = FirebaseDatabase.getInstance().getReference("users");

        // Initialiser les vues
        textViewUserWelcomeText = findViewById(R.id.textViewUserWelcomeText);
        viewHomeUserProfile = findViewById(R.id.viewHomeUserProfile);
        imageViewUserIcon = findViewById(R.id.imageViewUserIcon);
        buttonLogout = findViewById(R.id.buttonLogout);

        // Initialiser les vues des carreaux
        viewMyEvents = findViewById(R.id.viewMyEvents);
        viewMyTasks = findViewById(R.id.viewMyTasks);
        Log.d(TAG, "onCreate: viewMyTasks initialisé: " + (viewMyTasks != null));
        viewMyTrack = findViewById(R.id.viewMyTrack);
        viewAIPriorities = findViewById(R.id.viewAIPriorities);

        // Initialiser les éléments du footer
        buttonHome = findViewById(R.id.buttonHome);
        buttonAddTask = findViewById(R.id.buttonAddTask);
        buttonSearchTask = findViewById(R.id.buttonSearchTask);

        // Configurer le gestionnaire de notifications
        SmartNotificationManager smartNotificationManager = new SmartNotificationManager(this);
        smartNotificationManager.scheduleSmartReminders();

        // Lire et afficher le nom d'utilisateur seulement si l'utilisateur est connecté
        if (firebaseUser != null) {
            readUserName();
        } else {
            textViewUserWelcomeText.setText("Veuillez vous connecter");
            // Rediriger vers l'écran de connexion si nécessaire
            // Intent intent = new Intent(Home_Page.this, LoginActivity.class);
            // startActivity(intent);
        }

        // Configurer le profil utilisateur
        setupUserProfile();

        // Configurer les clics sur les carreaux
        setupCardClicks();
        Log.d(TAG, "onCreate: setupCardClicks appelé");

        // Configurer le footer
        setupFooter();

        // Configurer le bouton de déconnexion
        buttonLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                logoutUser();
            }
        });
    }

    private void setupUserProfile() {
        // Configurer le clic sur le profil
        viewHomeUserProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Home_Page.this, User_Profile_Activity.class);
                startActivity(intent);
                overridePendingTransition(R.anim.new_slide_in, R.anim.new_slide_out);
            }
        });
    }

    private void setupCardClicks() {
        // My Events - Ouvre Google Calendar
        viewMyEvents.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Afficher le dialogue de sélection de catégorie pour les événements
                showEventCategorySelectionDialog();
            }
        });

        // My Tasks - Affiche le dialogue de sélection de catégorie
        viewMyTasks.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "onClick: Clic sur My Tasks détecté");
                // Afficher le dialogue de sélection de catégorie
                showCategorySelectionDialog();
            }
        });

        // My Track
        viewMyTrack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Home_Page.this, Track_Tasks_Activity.class);
                startActivity(intent);
                overridePendingTransition(R.anim.slide_in, R.anim.slide_out);
            }
        });

        // AI Priorities
        viewAIPriorities.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Home_Page.this, PrioritizedTasksActivity.class);
                startActivity(intent);
                overridePendingTransition(R.anim.slide_in, R.anim.slide_out);
            }
        });
    }

    private void setupFooter() {
        // Home button
        buttonHome.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Déjà sur la page d'accueil, ne rien faire
                Toast.makeText(Home_Page.this, "Vous êtes déjà sur la page d'accueil", Toast.LENGTH_SHORT).show();
            }
        });

        // Add Task button
        buttonAddTask.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showEventTaskDialog();
            }
        });

        // Search Task button
        buttonSearchTask.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Home_Page.this, Search_Task.class);
                startActivity(intent);
                overridePendingTransition(R.anim.slide_in, R.anim.slide_out);
            }
        });
    }

    void readUserName() {
        // Vérifier si l'utilisateur est connecté
        if (firebaseUser == null) {
            // Aucun utilisateur connecté, afficher un message approprié
            textViewUserWelcomeText.setText("Veuillez vous connecter");
            return;
        }

        // Continuer seulement si l'utilisateur est connecté
        String userId = firebaseUser.getUid();
        userReference.child(userId).get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DataSnapshot> task) {
                if (task.isSuccessful()) {
                    DataSnapshot thisDataSnapshot = task.getResult();
                    String userName = String.valueOf(thisDataSnapshot.child("username").getValue());
                    if (userName.equals("null")) {
                        textViewUserWelcomeText.setText("Kindly Register With Us.");
                    } else {
                        textViewUserWelcomeText.setText("Hi, " + userName + ".");
                    }
                } else {
                    textViewUserWelcomeText.setText("Hello User Check Network!");
                }
            }
        });
    }

    private void showEventTaskDialog() {
        // Initialiser la boîte de dialogue
        Dialog categoryDialog = new Dialog(this);
        categoryDialog.setContentView(R.layout.event_task_dialog_layout);

        View viewTaskDialog = categoryDialog.findViewById(R.id.viewTaskDialog);
        View viewEventDialog = categoryDialog.findViewById(R.id.viewEventDialog);

        viewEventDialog.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Home_Page.this, Create_Event.class);
                startActivity(intent);
                overridePendingTransition(R.anim.slide_in, R.anim.slide_out);
                categoryDialog.dismiss();
            }
        });

        viewTaskDialog.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Home_Page.this, Create_Task.class);
                startActivity(intent);
                overridePendingTransition(R.anim.slide_in, R.anim.slide_out);
                categoryDialog.dismiss();
            }
        });

        // Afficher la boîte de dialogue
        categoryDialog.show();
    }


    private void showEventCategorySelectionDialog() {
        try {
            Log.d(TAG, "showEventCategorySelectionDialog: Début de la méthode");

            // Créer une boîte de dialogue pour la sélection de catégorie
            Dialog categoryDialog = new Dialog(Home_Page.this);
            Log.d(TAG, "showEventCategorySelectionDialog: Dialog créé");

            categoryDialog.setContentView(R.layout.category_selection_dialog);
            Log.d(TAG, "showEventCategorySelectionDialog: Content view défini");

            // Trouver les vues de catégorie dans la boîte de dialogue
            View viewPersonalCategory = categoryDialog.findViewById(R.id.viewPersonalCategory);
            View viewFinanceCategory = categoryDialog.findViewById(R.id.viewFinanceCategory);
            View viewLeisureCategory = categoryDialog.findViewById(R.id.viewLeisureCategory);
            View viewHealthCategory = categoryDialog.findViewById(R.id.viewHealthCategory);
            View viewSelfCategory = categoryDialog.findViewById(R.id.viewSelfCategory);
            View viewWorkCategory = categoryDialog.findViewById(R.id.viewWorkCategory);

            Log.d(TAG, "showEventCategorySelectionDialog: Vues des catégories trouvées");

            // Configurer les écouteurs de clic pour chaque catégorie
            viewPersonalCategory.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(Home_Page.this, view_events_activity.class);
                    Bundle bundle = new Bundle();
                    bundle.putString("category", "Personal");
                    intent.putExtras(bundle);
                    startActivity(intent);
                    overridePendingTransition(R.anim.slide_in, R.anim.slide_out);
                    categoryDialog.dismiss();
                }
            });

            viewFinanceCategory.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(Home_Page.this, view_events_activity.class);
                    Bundle bundle = new Bundle();
                    bundle.putString("category", "Finance");
                    intent.putExtras(bundle);
                    startActivity(intent);
                    overridePendingTransition(R.anim.slide_in, R.anim.slide_out);
                    categoryDialog.dismiss();
                }
            });

            viewLeisureCategory.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(Home_Page.this, view_events_activity.class);
                    Bundle bundle = new Bundle();
                    bundle.putString("category", "Leisure");
                    intent.putExtras(bundle);
                    startActivity(intent);
                    overridePendingTransition(R.anim.slide_in, R.anim.slide_out);
                    categoryDialog.dismiss();
                }
            });

            viewHealthCategory.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(Home_Page.this, view_events_activity.class);
                    Bundle bundle = new Bundle();
                    bundle.putString("category", "Health");
                    intent.putExtras(bundle);
                    startActivity(intent);
                    overridePendingTransition(R.anim.slide_in, R.anim.slide_out);
                    categoryDialog.dismiss();
                }
            });

            viewSelfCategory.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(Home_Page.this, view_events_activity.class);
                    Bundle bundle = new Bundle();
                    bundle.putString("category", "Self Care");
                    intent.putExtras(bundle);
                    startActivity(intent);
                    overridePendingTransition(R.anim.slide_in, R.anim.slide_out);
                    categoryDialog.dismiss();
                }
            });

            viewWorkCategory.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(Home_Page.this, view_events_activity.class);
                    Bundle bundle = new Bundle();
                    bundle.putString("category", "Work");
                    intent.putExtras(bundle);
                    startActivity(intent);
                    overridePendingTransition(R.anim.slide_in, R.anim.slide_out);
                    categoryDialog.dismiss();
                }
            });

            Log.d(TAG, "showEventCategorySelectionDialog: Écouteurs de clic configurés");

            // Afficher la boîte de dialogue
            categoryDialog.show();
        } catch (Exception e) {
            Log.e(TAG, "showEventCategorySelectionDialog: Erreur", e);
        }
    }
    // Méthode pour afficher la boîte de dialogue de sélection de catégorie
    private void showCategorySelectionDialog() {
        try {
            Log.d(TAG, "showCategorySelectionDialog: Début de la méthode");

            // Créer une boîte de dialogue pour la sélection de catégorie
            Dialog categoryDialog = new Dialog(Home_Page.this);
            Log.d(TAG, "showCategorySelectionDialog: Dialog créé");

            categoryDialog.setContentView(R.layout.category_selection_dialog);
            Log.d(TAG, "showCategorySelectionDialog: Content view défini");

            // Trouver les vues de catégorie dans la boîte de dialogue
            View viewPersonalCategory = categoryDialog.findViewById(R.id.viewPersonalCategory);
            View viewFinanceCategory = categoryDialog.findViewById(R.id.viewFinanceCategory);
            View viewLeisureCategory = categoryDialog.findViewById(R.id.viewLeisureCategory);
            View viewHealthCategory = categoryDialog.findViewById(R.id.viewHealthCategory);
            View viewSelfCategory = categoryDialog.findViewById(R.id.viewSelfCategory);
            View viewWorkCategory = categoryDialog.findViewById(R.id.viewWorkCategory);

            Log.d(TAG, "showCategorySelectionDialog: Vues des catégories trouvées");

            // Configurer les écouteurs de clic pour chaque catégorie
            viewPersonalCategory.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(Home_Page.this, view_tasks_activity.class);
                    Bundle bundle = new Bundle();
                    bundle.putString("category", "Personal");
                    intent.putExtras(bundle);
                    startActivity(intent);
                    overridePendingTransition(R.anim.slide_in, R.anim.slide_out);
                    categoryDialog.dismiss();
                }
            });

            viewFinanceCategory.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(Home_Page.this, view_tasks_activity.class);
                    Bundle bundle = new Bundle();
                    bundle.putString("category", "Finance");
                    intent.putExtras(bundle);
                    startActivity(intent);
                    overridePendingTransition(R.anim.slide_in, R.anim.slide_out);
                    categoryDialog.dismiss();
                }
            });

            viewLeisureCategory.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(Home_Page.this, view_tasks_activity.class);
                    Bundle bundle = new Bundle();
                    bundle.putString("category", "Leisure");
                    intent.putExtras(bundle);
                    startActivity(intent);
                    overridePendingTransition(R.anim.slide_in, R.anim.slide_out);
                    categoryDialog.dismiss();
                }
            });

            viewHealthCategory.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(Home_Page.this, view_tasks_activity.class);
                    Bundle bundle = new Bundle();
                    bundle.putString("category", "Health");
                    intent.putExtras(bundle);
                    startActivity(intent);
                    overridePendingTransition(R.anim.slide_in, R.anim.slide_out);
                    categoryDialog.dismiss();
                }
            });

            viewSelfCategory.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(Home_Page.this, view_tasks_activity.class);
                    Bundle bundle = new Bundle();
                    bundle.putString("category", "Self Care");
                    intent.putExtras(bundle);
                    startActivity(intent);
                    overridePendingTransition(R.anim.slide_in, R.anim.slide_out);
                    categoryDialog.dismiss();
                }
            });

            viewWorkCategory.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(Home_Page.this, view_tasks_activity.class);
                    Bundle bundle = new Bundle();
                    bundle.putString("category", "Work");
                    intent.putExtras(bundle);
                    startActivity(intent);
                    overridePendingTransition(R.anim.slide_in, R.anim.slide_out);
                    categoryDialog.dismiss();
                }
            });

            Log.d(TAG, "showCategorySelectionDialog: Écouteurs de clic configurés");

            // Afficher la boîte de dialogue
            categoryDialog.show();
            Log.d(TAG, "showCategorySelectionDialog: Dialog affiché");
        } catch (Exception e) {
            Log.e(TAG, "Erreur lors de l'affichage du dialogue: " + e.getMessage());
            e.printStackTrace();
            Toast.makeText(this, "Erreur: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void logoutUser() {
        AlertDialog.Builder builder = new AlertDialog.Builder(Home_Page.this);
        builder.setTitle("Déconnexion");
        builder.setMessage("Êtes-vous sûr de vouloir vous déconnecter ?");

        builder.setPositiveButton("Oui", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                FirebaseAuth.getInstance().signOut();
                saveLoginStatus(false);
                Intent intent = new Intent(Home_Page.this, MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
            }
        });

        builder.setNegativeButton("Non", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    private void saveLoginStatus(boolean isLoggedIn) {
        SharedPreferences sharedPreferences = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean("isLoggedIn", isLoggedIn);
        editor.apply();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_GOOGLE_CALENDAR) {
            // Traiter le résultat si nécessaire
        }
    }
}