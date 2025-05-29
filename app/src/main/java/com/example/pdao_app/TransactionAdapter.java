package com.example.pdao_app;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class TransactionAdapter extends RecyclerView.Adapter<TransactionAdapter.TransactionViewHolder> {

    private List<Transaction> transactionList;

    public TransactionAdapter(List<Transaction> transactionList) {
        this.transactionList = transactionList;
    }

    public static class TransactionViewHolder extends RecyclerView.ViewHolder {
        TextView title, date, address, details;

        public TransactionViewHolder(View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.text_transaction_store);
            date = itemView.findViewById(R.id.text_transaction_date);
            address = itemView.findViewById(R.id.text_transaction_address);
            details = itemView.findViewById(R.id.text_transaction_details);
        }
    }

    @NonNull
    @Override
    public TransactionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_transaction, parent, false);
        return new TransactionViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TransactionViewHolder holder, int position) {
        Transaction transaction = transactionList.get(position);
        holder.title.setText(transaction.getStoreName());
        holder.address.setText(transaction.getAddress());
        holder.date.setText(transaction.getDate());
        holder.details.setText(transaction.getDetails());
    }

    @Override
    public int getItemCount() {
        return transactionList.size();
    }
}

