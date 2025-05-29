package com.example.pdao_app;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;


import androidx.activity.EdgeToEdge;
import androidx.activity.OnBackPressedDispatcherOwner;
import androidx.appcompat.app.AppCompatActivity;

public class LoginActivity extends AppCompatActivity {

    private EditText usernameEditText, passwordEditText;
    private Button loginButton, pwdButton, storeButton;
    private ImageButton backButton;
    private LinearLayout loginLayout, initialLayout;
    private FirebaseAuth mAuth;



    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);

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
        backButton = findViewById(R.id.backBtn);

        // Initial buttons
        pwdButton = findViewById(R.id.pwdButton);
        storeButton = findViewById(R.id.storeButton);
        initialLayout = findViewById(R.id.initial_layout);

        // Handle "PWD" button click
        pwdButton.setOnClickListener(v -> {
            initialLayout.setVisibility(View.GONE);
            loginLayout.setVisibility(View.VISIBLE);
        });



        backButton.setOnClickListener(v -> {
            initialLayout.setVisibility(View.VISIBLE);
            loginLayout.setVisibility(View.GONE);
        });

        // Optional: Handle "Store" button click
        storeButton.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, QRScanner.class);
            startActivity(intent);
            finish();
        });

        passwordEditText.setOnTouchListener((v, event) -> {
            final int DRAWABLE_END = 2; // right drawable
            if (event.getAction() == MotionEvent.ACTION_UP) {
                if (event.getRawX() >= (passwordEditText.getRight() - passwordEditText.getCompoundDrawables()[DRAWABLE_END].getBounds().width())) {
                    // Toggle password visibility
                    if (passwordEditText.getInputType() == (InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD)) {
                        // Show password
                        passwordEditText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                        passwordEditText.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_hide, 0);
                    } else {
                        // Hide password
                        passwordEditText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                        passwordEditText.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_show, 0);
                    }
                    passwordEditText.setSelection(passwordEditText.length());
                    return true;
                }
            }
            return false;
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
                        Intent intent = new Intent(LoginActivity.this, Dashboard.class);
                        startActivity(intent);
                        finish();
                    }
                }
                else {
                    Toast.makeText(LoginActivity.this, "Authentication failed: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        if (initialLayout.getVisibility() == View.VISIBLE) {
            // If the initial layout is visible, show the login layout and hide the initial layout
            initialLayout.setVisibility(View.GONE);
            loginLayout.setVisibility(View.VISIBLE);
        } else {
            // Handle the case when the login layout is visible (or any other action)
            //super.onBackPressed(); // This will perform the default back button behavior (exit the activity)
        }
    }

}
