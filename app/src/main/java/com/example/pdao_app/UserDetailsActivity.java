package com.example.pdao_app;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class UserDetailsActivity extends AppCompatActivity {

    private TextView textTitle, textDescription;
    private Button btnAction; // added the Button

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

        // Initialize the views
        textTitle = findViewById(R.id.textTitle);
        textDescription = findViewById(R.id.textDescription);
        btnAction = findViewById(R.id.btnAction);

        // Get the QR result from the intent
        Intent intent = getIntent();
        if (intent != null) {
            String qrResult = intent.getStringExtra("qr_result");
            if (qrResult != null) {
                textTitle.setText("(INSERT USER NAME HERE)");
                textDescription.setText(qrResult);
            }
        }

        // Set button click listener
        btnAction.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Get the value from the TextView
                String qrValue = textDescription.getText().toString();

                // Create an Intent to move to TransactionForms
                Intent intent = new Intent(UserDetailsActivity.this, TransactionForms.class);
                intent.putExtra("qr_result", qrValue);
                startActivity(intent);
            }
        });

    }
}
