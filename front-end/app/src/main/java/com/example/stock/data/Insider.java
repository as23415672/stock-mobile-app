package com.example.stock.data;

public class Insider {
    final String symbol;
    final int year, month, change;
    final double mspr;

    public Insider(String symbol, int year, int month, int change, double mspr) {
        this.symbol = symbol;
        this.year = year;
        this.month = month;
        this.change = change;
        this.mspr = mspr;
    }

    public String getSymbol() {
        return symbol;
    }

    public int getYear() {
        return year;
    }

    public int getMonth() {
        return month;
    }

    public int getChange() {
        return change;
    }

    public double getMSPR() {
        return mspr;
    }
}
