package com.example.pdao_app;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;



import androidx.appcompat.app.AppCompatActivity;

public class LoginActivity extends AppCompatActivity {

    private EditText usernameEditText, passwordEditText;
    private Button loginButton, pwdButton, storeButton;
    private LinearLayout loginLayout, initialButtonsLayout;
    private FirebaseAuth mAuth;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            // User already signed in
            /*Intent intent = new Intent(LoginActivity.this, PwdUserDashboard.class);
            startActivity(intent);
            finish();*/
        }

        setContentView(R.layout.activity_login);

        mAuth = FirebaseAuth.getInstance();


        // Login layout
        usernameEditText = findViewById(R.id.usernameEditText);
        passwordEditText = findViewById(R.id.passwordEditText);
        loginButton = findViewById(R.id.loginButton);
        loginLayout = findViewById(R.id.add_Shadow);

        // Initial buttons
        pwdButton = findViewById(R.id.pwdButton);
        storeButton = findViewById(R.id.storeButton);
        initialButtonsLayout = findViewById(R.id.initial_buttons_layout);

        // Handle "PWD" button click
        pwdButton.setOnClickListener(v -> {
            initialButtonsLayout.setVisibility(View.GONE);
            loginLayout.setVisibility(View.VISIBLE);
        });

        // Optional: Handle "Store" button click
        storeButton.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, QRScanner.class);
            startActivity(intent);
            finish();
        });

        // Handle login
        loginButton.setOnClickListener(v -> {
            String email = usernameEditText.getText().toString().trim();
            String password = passwordEditText.getText().toString().trim();

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(LoginActivity.this, "Please enter both email and password", Toast.LENGTH_SHORT).show();
                return;
            }

            mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    FirebaseUser user = mAuth.getCurrentUser();
                    if (user != null) {
                        String uemail = user.getEmail();
                        String uid = user.getUid();
                        String displayName = user.getDisplayName(); // May be null if not set

                        String userDetails = "Email: " + uemail + "\nUID: " + uid;
                        if (displayName != null) {
                            userDetails += "\nName: " + displayName;
                        }

                        Toast.makeText(LoginActivity.this, userDetails, Toast.LENGTH_LONG).show();

                        Toast.makeText(LoginActivity.this, displayName, Toast.LENGTH_LONG).show();

                        // Optional: Navigate or finish
                        // Intent intent = new Intent(LoginActivity.this, PwdUserDashboard.class);
                        // startActivity(intent);
                        // finish();
                    }
                }
                else {
                    Toast.makeText(LoginActivity.this, "Authentication failed: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        });
    }
}
