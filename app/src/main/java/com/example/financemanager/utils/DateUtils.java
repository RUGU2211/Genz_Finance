package com.example.financemanager.utils;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class DateUtils {

    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy", Locale.US);
    private static final SimpleDateFormat dateTimeFormat = new SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.US);

    public static String formatDate(Date date) {
        if (date == null) {
            return "";
        }
        return dateFormat.format(date);
    }

    public static String formatDateTime(Date date) {
        if (date == null) {
            return "";
        }
        return dateTimeFormat.format(date);
    }
}