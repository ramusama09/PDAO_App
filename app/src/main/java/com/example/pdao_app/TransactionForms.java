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

    private TextView referenceNoText, storeNameText;
    private LinearLayout itemsContainer;
    private Button addItemButton, removeItemButton, submitButton;
    private TextView originalTotalTextView, discountedTotalTextView;

    // Store all item views
    private ArrayList<View> itemViews = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_transaction_forms);

        // Initialize inputs
        referenceNoText = findViewById(R.id.referenceTextView);
        storeNameText = findViewById(R.id.storeNameTextView);
        itemsContainer = findViewById(R.id.itemListContainer);
        addItemButton = findViewById(R.id.addItemButton);
        removeItemButton = findViewById(R.id.removeItemButton);
        submitButton = findViewById(R.id.submitButton);
        originalTotalTextView = findViewById(R.id.originalTotalText);
        discountedTotalTextView = findViewById(R.id.discountedTotalText);

        Intent intent = getIntent();
        if (intent != null) {
            String qrResult = intent.getStringExtra("qr_result");
            if (qrResult != null) {
                referenceNoText.setText("Reference No: INSERT");
                storeNameText.setText("Store Name: " + qrResult);
            }
        }

        // Add first item row by default
        addItem();

        // Listener for Add Item button
        addItemButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addItem();
            }
        });

        // Listener for Remove Item button
        removeItemButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                removeItem();
            }
        });

        // Listener for Submit button
        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                submitTransaction();
            }
        });
    }

    // Function to dynamically add an item input row
    private void addItem() {
        View itemView = LayoutInflater.from(this).inflate(R.layout.item_row, itemsContainer, false);
        itemsContainer.addView(itemView);
        itemViews.add(itemView);

        // Add listeners to auto-update total when price/discount changes
        EditText priceEditText = itemView.findViewById(R.id.priceEditText);
        EditText discountedPriceEditText = itemView.findViewById(R.id.discountedPriceEditText);

        TextWatcherAdapter watcher = new TextWatcherAdapter(this::updateTotals);
        priceEditText.addTextChangedListener(watcher);
        discountedPriceEditText.addTextChangedListener(watcher);

        updateTotals();
    }

    // Function to dynamically remove the last item row
    private void removeItem() {
        if (!itemViews.isEmpty()) {
            View lastItemView = itemViews.remove(itemViews.size() - 1);
            itemsContainer.removeView(lastItemView);
            updateTotals();
        }
    }

    // Update the Original Total and Discounted Total
    private void updateTotals() {
        double originalTotal = 0.0;
        double discountedTotal = 0.0;

        for (View itemView : itemViews) {
            EditText quantityEditText = itemView.findViewById(R.id.qtyEditText);
            EditText priceEditText = itemView.findViewById(R.id.priceEditText);
            EditText discountedPriceEditText = itemView.findViewById(R.id.discountedPriceEditText);

            double qty = parseDouble(quantityEditText.getText().toString());
            double price = parseDouble(priceEditText.getText().toString());
            double discountedPrice = parseDouble(discountedPriceEditText.getText().toString());

            originalTotal += (price * qty);
            discountedTotal += (discountedPrice * qty);
        }

        originalTotalTextView.setText(String.format("Original Total Price: %.2f", originalTotal));
        discountedTotalTextView.setText(String.format("Discounted Total Price: %.2f", discountedTotal));
    }

    // Handle Submit
    private void submitTransaction() {
        String referenceNo = referenceNoText.getText().toString();
        String storeName = storeNameText.getText().toString();

        if (referenceNo.isEmpty() || storeName.isEmpty() || itemViews.isEmpty()) {
            Toast.makeText(this, "Please complete all fields!", Toast.LENGTH_SHORT).show();
            return;
        }

        // You can process the data (save to database, send to server, etc.)
        Toast.makeText(this, "Transaction Submitted!", Toast.LENGTH_SHORT).show();

        // Optionally clear everything
        referenceNoText.setText("");
        storeNameText.setText("");
        itemsContainer.removeAllViews();
        itemViews.clear();
        updateTotals();
    }

    // Helper function
    private double parseDouble(String value) {
        try {
            return Double.parseDouble(value);
        } catch (NumberFormatException e) {
            return 0.0;
        }
    }
}
