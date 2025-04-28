package com.example.pdao_app;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;

public class TransactionForms extends AppCompatActivity {

    private EditText referenceNoText, storeNameText, storeAddressText, descriptionText;
    private Button submitButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_transaction_forms);

        // Initialize inputs
        referenceNoText = findViewById(R.id.referenceEditText);
        storeNameText = findViewById(R.id.storeNameEditText);
        storeAddressText = findViewById(R.id.storeAddressEditText);
        descriptionText = findViewById(R.id.descriptionEditText);
        submitButton = findViewById(R.id.submitButton);


        Intent intent = getIntent();
        if (intent != null) {
            String qrResult = intent.getStringExtra("qr_result");
            if (qrResult != null) {
                referenceNoText.setText("SUCCESS");
                storeNameText.setText(qrResult);
                storeAddressText.setText("AGEY");
                descriptionText.setText("NICE");
            }
        }


        // Listener for Submit button
        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                submitTransaction();
            }
        });
    }

    // Update the Original Total and Discounted Total

    // Handle Submit
    private void submitTransaction() {
        String referenceNo = referenceNoText.getText().toString();
        String storeName = storeNameText.getText().toString();
        String storeAddress = storeAddressText.getText().toString();
        String desc = descriptionText.getText().toString();

        if (referenceNo.isEmpty() || storeName.isEmpty() || storeAddress.isEmpty() || desc.isEmpty()) {
            Toast.makeText(this, "Please complete all fields!", Toast.LENGTH_SHORT).show();
            return;
        }

        // You can process the data (save to database, send to server, etc.)
        Toast.makeText(this, "Transaction Submitted!", Toast.LENGTH_SHORT).show();

        // Optionally clear everything
        referenceNoText.setText("");
        storeNameText.setText("");
        storeAddressText.setText("");
        descriptionText.setText("");
    }
}
