package com.example.pdao_app;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class MainActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();

        if (currentUser != null) {
            // User is signed in
            //startActivity(new Intent(this, PwdUserDashboard.class));
            startActivity(new Intent(this, LoginActivity.class)); //Remove this once the dashboard is done
        } else {
            // User is not signed in
            startActivity(new Intent(this, LoginActivity.class));
        }
        finish(); // Prevent user from returning here
    }
}
