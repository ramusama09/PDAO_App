package com.example.pdao_app;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class TransactionForms extends AppCompatActivity {

    private EditText amountEditText, descriptionEditText, dateEditText;
    private Button submitButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_transaction_forms);

        // Initialize the EditText fields and Button
        amountEditText = findViewById(R.id.amountEditText);
        descriptionEditText = findViewById(R.id.descriptionEditText);
        dateEditText = findViewById(R.id.dateEditText);
        submitButton = findViewById(R.id.submitButton);

        // Get the QR result if available (optional based on previous flow)
        String qrResult = getIntent().getStringExtra("qr_result");

        //Check if THE QR is valid. Otherwise, do not proceed to the activity
        if (qrResult != null) {
            // You can choose to use the QR result for some pre-filled info or just ignore
            //Toast.makeText(this, "QR Code: " + qrResult, Toast.LENGTH_SHORT).show();
        }

        // Handle submit button click
        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Retrieve the input data
                String amount = amountEditText.getText().toString();
                String description = descriptionEditText.getText().toString();
                String date = dateEditText.getText().toString();

                // Optionally, you can validate or process the data
                if (amount.isEmpty() || description.isEmpty() || date.isEmpty()) {
                    Toast.makeText(TransactionForms.this, "Please fill all fields", Toast.LENGTH_SHORT).show();
                } else {
                    // Process the form data (e.g., save to database, send to server)
                    Toast.makeText(TransactionForms.this, "Transaction submitted", Toast.LENGTH_SHORT).show();

                    SharedPreferences prefs = getSharedPreferences("AuthPrefs", MODE_PRIVATE);
                    SharedPreferences.Editor editor = prefs.edit();
                    editor.clear();  // Clear all stored data
                    editor.apply();

                    // Optionally, clear fields after submission
                    amountEditText.setText("");
                    descriptionEditText.setText("");
                    dateEditText.setText("");
                }
            }
        });
    }
}
