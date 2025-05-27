package com.example.pdao_app.ui.transaction;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.pdao_app.Transaction;
import com.example.pdao_app.TransactionAdapter;
import com.example.pdao_app.databinding.FragmentTransactionBinding;

import java.util.ArrayList;
import java.util.List;

public class TransactionFragment extends Fragment {

    private FragmentTransactionBinding binding;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        TransactionViewModel transactionViewModel =
                new ViewModelProvider(this).get(TransactionViewModel.class);

        binding = FragmentTransactionBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        // Set header text via ViewModel
        transactionViewModel.getText().observe(getViewLifecycleOwner(), text -> {
            binding.textHeader.setText(text);
        });

        // Create mock transactions

        //To populate real data, put them all in the ArrayList using "Transaction.java" as Data Model
        List<Transaction> transactionList = new ArrayList<>();
        transactionList.add(new Transaction("Payment to John Doe", "May 25, 2025", "$120.00"));
        transactionList.add(new Transaction("Salary Credited", "May 20, 2025", "+$2,000.00"));
        transactionList.add(new Transaction("Netflix Subscription", "May 18, 2025", "$15.99"));

        // Set up RecyclerView
        TransactionAdapter adapter = new TransactionAdapter(transactionList);
        binding.recyclerTransactions.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.recyclerTransactions.setAdapter(adapter);

        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
