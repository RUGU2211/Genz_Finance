package com.example.financemanager.utils;

import java.text.NumberFormat;
import java.util.Locale;

public class CurrencyFormatter {

    private static final NumberFormat currencyFormatter = NumberFormat.getCurrencyInstance(Locale.US);

    public static String format(double amount) {
        return currencyFormatter.format(amount);
    }
}