package com.example.pdao_app;

public class Transaction {
    private String storeName;
    private String date;
    private String details;

    public Transaction(String storeName, String date, String details) {
        this.storeName = storeName;
        this.date = date;
        this.details = details;
    }

    public String getStoreName() {
        return storeName;
    }

    public String getDate() {
        return date;
    }

    public String getDetails() {
        return details;
    }
}

