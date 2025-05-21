package com.example.luna;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;

public class SplashScreen extends AppCompatActivity {
    Handler myHandler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);

        myHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                // Vérifier si l'utilisateur est déjà connecté
                if (isLoggedIn()) {
                    // Rediriger vers Home_Page si authentifié
                    Intent myIntent = new Intent(SplashScreen.this, Home_Page.class);
                    startActivity(myIntent);
                } else {
                    // Rediriger vers MainActivity (page de login) si non authentifié
                    Intent myIntent = new Intent(SplashScreen.this, MainActivity.class);
                    startActivity(myIntent);
                }
                finish(); // Fermer l'activité SplashScreen
            }
        }, 2000); // Délai de 2 secondes
    }

    private boolean isLoggedIn() {
        SharedPreferences sharedPreferences = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
        return sharedPreferences.getBoolean("isLoggedIn", false);
    }
}