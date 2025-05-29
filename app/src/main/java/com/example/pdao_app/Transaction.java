package com.example.pdao_app;

public class Transaction {
    private String storeName;
    private String address;
    private String date;
    private String details;

    public Transaction(String storeName, String address, String date, String details) {
        this.storeName = storeName;
        this.address = address;
        this.date = date;
        this.details = details;
    }

    public String getStoreName() {
        return storeName;
    }

    public String getAddress() {
        return address;
    }

    public String getDate() {
        return date;
    }

    public String getDetails() {
        return details;
    }
}

