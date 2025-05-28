package com.example.pdao_app;

import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;

public class TransactionForms extends AppCompatActivity {

    private EditText referenceNoText, storeNameText, storeAddressText, descriptionText;
    private Button submitButton;
    private String userId;
    private DatabaseReference usersRef, transactionsRef;
    private String generatedRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_transaction_forms);

        referenceNoText = findViewById(R.id.referenceEditText);
        storeNameText = findViewById(R.id.storeNameEditText);
        storeAddressText = findViewById(R.id.storeAddressEditText);
        descriptionText = findViewById(R.id.descriptionEditText);
        submitButton = findViewById(R.id.submitButton);

        // Disable editing of reference number
        referenceNoText.setInputType(InputType.TYPE_NULL);
        referenceNoText.setFocusable(false);

        usersRef = FirebaseDatabase.getInstance().getReference("users");
        transactionsRef = FirebaseDatabase.getInstance().getReference("transactions");

        Intent intent = getIntent();
        if (intent != null) {
            userId = intent.getStringExtra("qr_result");
            if (userId != null) {
                generateReferenceNumber(userId);
            }
        }

        submitButton.setOnClickListener(v -> submitTransaction());
    }

    private void generateReferenceNumber(String userId) {
        usersRef.child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                if (!snapshot.exists()) {
                    Toast.makeText(TransactionForms.this, "User not found.", Toast.LENGTH_SHORT).show();
                    return;
                }

                String pwdIdNo = snapshot.child("idCards").child("pwdIdNo").getValue(String.class);
                if (pwdIdNo == null || pwdIdNo.isEmpty()) {
                    Toast.makeText(TransactionForms.this, "PWD ID No. not found.", Toast.LENGTH_SHORT).show();
                    return;
                }

                String timestamp = new SimpleDateFormat("yyyyMMddHHmmss", Locale.getDefault()).format(new Date());
                generatedRef = timestamp + pwdIdNo;
                referenceNoText.setText(generatedRef);
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Toast.makeText(TransactionForms.this, "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void submitTransaction() {
        String storeName = storeNameText.getText().toString().trim();
        String storeAddress = storeAddressText.getText().toString().trim();
        String desc = descriptionText.getText().toString().trim();

        if (generatedRef == null || generatedRef.isEmpty() ||
                storeName.isEmpty() || storeAddress.isEmpty() || desc.isEmpty()) {
            Toast.makeText(this, "Please complete all fields!", Toast.LENGTH_SHORT).show();
            return;
        }

        String timestamp = new SimpleDateFormat("yyyyMMddHHmmss", Locale.getDefault()).format(new Date());

        HashMap<String, Object> transactionData = new HashMap<>();
        transactionData.put("referenceNum", generatedRef);
        transactionData.put("storeName", storeName);
        transactionData.put("address", storeAddress);
        transactionData.put("description", desc);

        transactionsRef.child(userId).child(timestamp).setValue(transactionData)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(TransactionForms.this, "Transaction Submitted!", Toast.LENGTH_SHORT).show();
                        clearFields();
                        Intent intent = new Intent(TransactionForms.this, MainActivity.class);
                        startActivity(intent);
                        finish();
                    } else {
                        Toast.makeText(TransactionForms.this, "Failed to submit transaction.", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void clearFields() {
        storeNameText.setText("");
        storeAddressText.setText("");
        descriptionText.setText("");
    }
}
