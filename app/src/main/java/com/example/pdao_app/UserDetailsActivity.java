package com.example.pdao_app;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class UserDetailsActivity extends AppCompatActivity {

    private TextView textTitle, textDescription;
    private Button btnAction;
    private ProgressBar progressBar;
    private String userId;
    private DatabaseReference usersRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_user_details);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Initialize views
        progressBar = findViewById(R.id.progressBar);
        textTitle = findViewById(R.id.textTitle);
        textDescription = findViewById(R.id.textDescription);
        btnAction = findViewById(R.id.btnAction);

        // Firebase reference
        usersRef = FirebaseDatabase.getInstance().getReference("users");

        // Get QR result from intent
        Intent intent = getIntent();
        if (intent != null) {
            userId = intent.getStringExtra("qr_result");
            if (userId != null) {
                showLoading(true);
                fetchUserData(userId);
            } else {
                Toast.makeText(this, "QR result not found", Toast.LENGTH_SHORT).show();
                btnAction.setVisibility(View.GONE); // Hide button if no QR result
            }
        }

        // Button action
        btnAction.setOnClickListener(v -> {
            Intent i = new Intent(UserDetailsActivity.this, TransactionForms.class);
            i.putExtra("qr_result", userId);
            startActivity(i);
        });
    }

    private void fetchUserData(String key) {
        usersRef.child(key).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                showLoading(false); // Hide loading UI

                if (!snapshot.exists()) {
                    textTitle.setText("User not found");
                    textDescription.setText("No user matched the QR code");
                    Toast.makeText(UserDetailsActivity.this, "No user matched the QR code", Toast.LENGTH_SHORT).show();
                    btnAction.setVisibility(View.GONE); // Hide button on no user
                    return;
                }

                // Check for ID card
                DataSnapshot idCardsSnapshot = snapshot.child("idCards");
                if (!idCardsSnapshot.exists()) {
                    textTitle.setText("ID Card not found");
                    textDescription.setText("No ID card record found.");
                    btnAction.setVisibility(View.GONE); // Hide button on no ID card
                    return;
                }

                // Check if both expirationDate and pwdIdNo exist
                String expirationDateStr = idCardsSnapshot.child("expirationDate").getValue(String.class);
                String pwdIdNo = idCardsSnapshot.child("pwdIdNo").getValue(String.class);

                if (expirationDateStr == null || expirationDateStr.isEmpty()) {
                    textTitle.setText("ID Card incomplete");
                    textDescription.setText("No expiration date found.");
                    btnAction.setVisibility(View.GONE);
                    return;
                }

                if (pwdIdNo == null || pwdIdNo.isEmpty()) {
                    textTitle.setText("ID Card incomplete");
                    textDescription.setText("PWD ID Number not found.");
                    btnAction.setVisibility(View.GONE);
                    return;
                }

                // Valid data found, show the button
                btnAction.setVisibility(View.VISIBLE);

                // Process expiration date
                try {
                    SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                    SimpleDateFormat outputFormat = new SimpleDateFormat("MMMM dd, yyyy", Locale.getDefault());
                    Date expirationDate = inputFormat.parse(expirationDateStr);
                    String formattedDate = outputFormat.format(expirationDate);

                    Date currentDate = new Date();
                    StringBuilder idStatus = new StringBuilder();

                    if (expirationDate != null) {
                        if (currentDate.after(expirationDate)) {
                            idStatus.append("ID No. ").append(pwdIdNo).append("\n").append("Status: Expired\nExpired on: ").append(formattedDate);
                        } else {
                            idStatus.append("ID No. ").append(pwdIdNo).append("\n").append("Status: Valid\nValid until: ").append(formattedDate);
                        }
                        textDescription.setText(idStatus);
                    } else {
                        textDescription.setText("Invalid expiration date format.");
                    }
                } catch (ParseException e) {
                    e.printStackTrace();
                    textDescription.setText("Failed to parse expiration date.");
                }

                // Build and show full name
                String firstName = snapshot.child("firstName").getValue(String.class);
                String middleName = snapshot.child("middleName").getValue(String.class);
                String lastName = snapshot.child("lastName").getValue(String.class);
                String suffix = snapshot.child("suffix").getValue(String.class);

                StringBuilder fullName = new StringBuilder();
                if (firstName != null) fullName.append(firstName).append(" ");
                if (middleName != null && !middleName.isEmpty()) fullName.append(middleName).append(" ");
                if (lastName != null) fullName.append(lastName).append(" ");
                if (suffix != null && !suffix.isEmpty()) fullName.append(suffix);

                textTitle.setText(fullName.toString().trim());
            }

            @Override
            public void onCancelled(DatabaseError error) {
                showLoading(false);
                Toast.makeText(UserDetailsActivity.this, "Database error: " + error.getMessage(), Toast.LENGTH_LONG).show();
                btnAction.setVisibility(View.GONE); // Hide button on error
            }
        });
    }


    private void showLoading(boolean isLoading) {
        progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        textTitle.setVisibility(isLoading ? View.INVISIBLE : View.VISIBLE);
        textDescription.setVisibility(isLoading ? View.INVISIBLE : View.VISIBLE);
        btnAction.setVisibility(isLoading ? View.GONE : btnAction.getVisibility());
    }

    @Override
    protected void onResume() {
        super.onResume();
        showLoading(true);
        if (userId != null) {
            fetchUserData(userId);
        }
    }


    @Override
    public void onBackPressed() {
        userId = null; // Clear the QR result
        super.onBackPressed();
    }

}
