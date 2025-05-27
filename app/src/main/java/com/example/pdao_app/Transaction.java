package com.example.pdao_app;

public class Transaction {
    private String title;
    private String date;
    private String amount;

    public Transaction(String title, String date, String amount) {
        this.title = title;
        this.date = date;
        this.amount = amount;
    }

    public String getTitle() {
        return title;
    }

    public String getDate() {
        return date;
    }

    public String getAmount() {
        return amount;
    }
}

