package com.example.pdao_app.ui.transaction;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.pdao_app.Transaction;
import com.example.pdao_app.TransactionAdapter;
import com.example.pdao_app.databinding.FragmentTransactionBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import android.text.Editable;
import android.text.TextWatcher;


public class TransactionFragment extends Fragment {

    private FragmentTransactionBinding binding;
    private TransactionAdapter adapter;
    private List<Transaction> transactionList = new ArrayList<>();
    private List<Transaction> fullTransactionList = new ArrayList<>(); // For original data


    private DatabaseReference transactionsRef;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        TransactionViewModel transactionViewModel =
                new ViewModelProvider(this).get(TransactionViewModel.class);

        binding = FragmentTransactionBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        // Set header
        transactionViewModel.getText().observe(getViewLifecycleOwner(), text -> {
            binding.textHeader.setText(text);
        });

        // Setup RecyclerView
        adapter = new TransactionAdapter(transactionList);
        binding.recyclerTransactions.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.recyclerTransactions.setAdapter(adapter);

        binding.swipeRefresh.setOnRefreshListener(() -> {
            loadTransactionsFromFirebase();
        });


        // Load transactions from Firebase
        loadTransactionsFromFirebase();

        binding.editSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterTransactions(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });


        return root;
    }

    private void loadTransactionsFromFirebase() {
        String userId = FirebaseAuth.getInstance().getCurrentUser() != null
                ? FirebaseAuth.getInstance().getCurrentUser().getUid()
                : null;

        if (userId == null) return;

        // Show loading spinner
        binding.progressBar.setVisibility(View.VISIBLE);

        transactionsRef = FirebaseDatabase.getInstance().getReference("transactions").child(userId);

        transactionsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                transactionList.clear();

                List<TempTransaction> tempList = new ArrayList<>();

                for (DataSnapshot timestampSnapshot : snapshot.getChildren()) {
                    String timestampKey = timestampSnapshot.getKey();
                    String storeName = timestampSnapshot.child("storeName").getValue(String.class);
                    String description = timestampSnapshot.child("description").getValue(String.class);
                    String address = timestampSnapshot.child("address").getValue(String.class);

                    try {
                        long timestamp = Long.parseLong(timestampKey);
                        tempList.add(new TempTransaction(timestamp, storeName, address, description));
                    } catch (NumberFormatException e) {
                        // Skip invalid timestamps
                    }
                }

                // Sort and populate
                tempList.sort((a, b) -> Long.compare(b.timestamp, a.timestamp));

                for (TempTransaction temp : tempList) {
                    String formattedDate = formatTimestamp(String.valueOf(temp.timestamp));
                    transactionList.add(new Transaction(temp.storeName, temp.address, formattedDate, temp.description));
                }

                fullTransactionList.clear();
                fullTransactionList.addAll(transactionList);

                adapter.notifyDataSetChanged();

                // Hide loading spinner
                binding.progressBar.setVisibility(View.GONE);
                binding.swipeRefresh.setRefreshing(false);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getContext(), "Failed to load transactions: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                binding.progressBar.setVisibility(View.GONE); // Hide even on error
                binding.swipeRefresh.setRefreshing(false);
            }
        });
    }


    private void filterTransactions(String query) {
        transactionList.clear();

        if (query.isEmpty()) {
            transactionList.addAll(fullTransactionList);
        } else {
            String lowerCaseQuery = query.toLowerCase();
            for (Transaction t : fullTransactionList) {
                if (t.getStoreName().toLowerCase().contains(lowerCaseQuery)) {
                    transactionList.add(t);
                }
            }
        }

        adapter.notifyDataSetChanged();
    }



    // Helper class to temporarily store transaction info with timestamp
    private static class TempTransaction {
        long timestamp;
        String storeName;
        String address;
        String description;

        TempTransaction(long timestamp, String storeName, String address, String description) {
            this.timestamp = timestamp;
            this.storeName = storeName;
            this.address = address;
            this.description = description;
        }
    }


    private String formatTimestamp(String timestampStr) {
        try {
            SimpleDateFormat inputFormat = new SimpleDateFormat("yyyyMMddHHmmss", Locale.getDefault());
            Date date = inputFormat.parse(timestampStr);

            SimpleDateFormat outputFormat = new SimpleDateFormat("MMMM dd, yyyy - hh:mm a", Locale.getDefault());
            return outputFormat.format(date);
        } catch (Exception e) {
            return "Invalid date";
        }
    }



    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
