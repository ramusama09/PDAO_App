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
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

public class LoginActivity extends AppCompatActivity {

    private EditText usernameEditText, passwordEditText;
    private Button loginButton, pwdButton, storeButton;
    private ImageButton backButton;
    private LinearLayout loginLayout, initialLayout;
    private FirebaseAuth mAuth;
    private TextView forgotPasswordText, signUpText;  // <- New

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            // Optional: User already signed in
        }

        setContentView(R.layout.activity_login);

        mAuth = FirebaseAuth.getInstance();

        // Layout references
        usernameEditText = findViewById(R.id.usernameEditText);
        passwordEditText = findViewById(R.id.passwordEditText);
        loginButton = findViewById(R.id.loginButton);
        loginLayout = findViewById(R.id.add_Shadow);
        backButton = findViewById(R.id.backBtn);
        forgotPasswordText = findViewById(R.id.forgot_password); // <- New
        signUpText = findViewById(R.id.sign_up_text);

        pwdButton = findViewById(R.id.pwdButton);
        storeButton = findViewById(R.id.storeButton);
        initialLayout = findViewById(R.id.initial_layout);

        pwdButton.setOnClickListener(v -> {
            initialLayout.setVisibility(View.GONE);
            loginLayout.setVisibility(View.VISIBLE);
        });

        backButton.setOnClickListener(v -> {
            initialLayout.setVisibility(View.VISIBLE);
            loginLayout.setVisibility(View.GONE);
        });

        storeButton.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, QRScanner.class);
            startActivity(intent);
            finish();
        });


        signUpText.setOnClickListener(v -> {
            startActivity(new Intent(LoginActivity.this, SignUpActivity.class));
        });


        passwordEditText.setOnTouchListener((v, event) -> {
            final int DRAWABLE_END = 2;
            if (event.getAction() == MotionEvent.ACTION_UP) {
                if (event.getRawX() >= (passwordEditText.getRight() - passwordEditText.getCompoundDrawables()[DRAWABLE_END].getBounds().width())) {
                    if (passwordEditText.getInputType() == (InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD)) {
                        passwordEditText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                        passwordEditText.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_hide, 0);
                    } else {
                        passwordEditText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                        passwordEditText.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_show, 0);
                    }
                    passwordEditText.setSelection(passwordEditText.length());
                    return true;
                }
            }
            return false;
        });

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
                        Intent intent = new Intent(LoginActivity.this, Dashboard.class);
                        startActivity(intent);
                        finish();
                    }
                } else {
                    Toast.makeText(LoginActivity.this, "Authentication failed: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        });

        // 🔐 Forgot password handler
        forgotPasswordText.setOnClickListener(v -> {
            String email = usernameEditText.getText().toString().trim();
            if (email.isEmpty()) {
                Toast.makeText(LoginActivity.this, "Enter your email to reset password", Toast.LENGTH_SHORT).show();
                return;
            }

            mAuth.sendPasswordResetEmail(email).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    Toast.makeText(LoginActivity.this, "Password reset email sent!", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(LoginActivity.this, "Failed to send reset email: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                }
            });
        });
    }

    @Override
    public void onBackPressed() {
        if (initialLayout.getVisibility() == View.VISIBLE) {
            initialLayout.setVisibility(View.GONE);
            loginLayout.setVisibility(View.VISIBLE);
        } else {
            super.onBackPressed();
        }
    }
}
