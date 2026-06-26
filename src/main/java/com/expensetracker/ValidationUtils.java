package com.expensetracker; // package: validation helpers

import javax.swing.text.AttributeSet; // for DocumentFilter methods
import javax.swing.text.BadLocationException; // for document edits
import javax.swing.text.DocumentFilter; // to restrict text input
import java.time.DateTimeException; // date/time parsing errors
import java.time.LocalDate; // validate date
import java.time.LocalTime; // validate time
import java.time.format.DateTimeFormatter; // parse time
import java.time.format.DateTimeParseException; // parse failures for multiple formats
import java.util.regex.Pattern; // phone regex

/**
 * ValidationUtils.java
 *
 * Description (start):
 * Small collection of validation helpers used across UI frames.
 * - `isValidAmountString` checks positive numeric amounts.
 * - `isValidDate` checks day/month/year form a real date.
 * - `isValidTime` verifies HH:mm times.
 * - `isValidPhone` checks a simple digit length range.
 * - `NumericDocumentFilter` restricts text input to digits and one dot.
 *
 * End of start description.
 */
public class ValidationUtils {

    // Time formats used by the app (24-hour and user-friendly 12-hour)
    private static final DateTimeFormatter TIME_FMT_24 = DateTimeFormatter.ofPattern("HH:mm");
    private static final DateTimeFormatter TIME_FMT_12 = DateTimeFormatter.ofPattern("h:mm a");

    // Very simple phone validation: 7 to 15 digits (no symbols)
    private static final Pattern PHONE_DIGITS = Pattern.compile("^\\d{7,15}$");

    // Check amount text is a positive number > 0
    public static boolean isValidAmountString(String s) {
        if (s == null || s.trim().isEmpty()) return false; // null/empty -> invalid
        try {
            double v = Double.parseDouble(s.trim()); // parse double
            return v > 0; // positive only
        } catch (NumberFormatException ex) {
            return false; // not a number
        }
    }

    // Validate a date given as separate day, month, year strings
    public static boolean isValidDate(String dayStr, String monthStr, String yearStr) {
        try {
            int d = Integer.parseInt(dayStr.trim()); // day int
            int m = Integer.parseInt(monthStr.trim()); // month int
            int y = Integer.parseInt(yearStr.trim()); // year int
            LocalDate.of(y, m, d); // throws if invalid
            return true; // valid date
        } catch (NumberFormatException | DateTimeException | NullPointerException ex) {
            return false; // parsing or date creation failed
        }
    }

    // Validate time in either HH:mm or h:mm a
    public static boolean isValidTime(String timeStr) {
        if (timeStr == null) return false;
        String s = timeStr.replace('\u00A0', ' ').trim(); // replace NBSP and trim
        if (s.isEmpty()) return false;
        // remove common punctuation used in AM/PM (a.m., p.m.) and normalize whitespace
        s = s.replaceAll("\\.", "");
        s = s.replaceAll("\\s+", " ").trim();

        // Normalize AM/PM spacing for formatter parsing (ensure a space before AM/PM)
        String upper = s.toUpperCase();
        upper = upper.replaceAll("\\s*AM$", " AM");
        upper = upper.replaceAll("\\s*PM$", " PM");

        // Try a set of common patterns (24-hour and 12-hour variants)
        DateTimeFormatter[] formatters = new DateTimeFormatter[] {
            TIME_FMT_24,
            DateTimeFormatter.ofPattern("H:mm"),
            TIME_FMT_12,
            DateTimeFormatter.ofPattern("hh:mm a"),
            DateTimeFormatter.ofPattern("h:mma"),
            DateTimeFormatter.ofPattern("hh:mma")
        };
        for (DateTimeFormatter fmt : formatters) {
            try {
                LocalTime.parse(upper, fmt);
                return true;
            } catch (DateTimeParseException | IllegalArgumentException ignored) {
                // try next
            }
        }

        // Fallback: permissive manual regex (accept optional AM/PM, with flexible spacing)
        java.util.regex.Matcher m = java.util.regex.Pattern.compile("^(\\d{1,2}):(\\d{2})\\s*([APap][Mm])?$").matcher(s);
        if (m.matches()) {
            try {
                int hour = Integer.parseInt(m.group(1));
                int minute = Integer.parseInt(m.group(2));
                String ampm = m.group(3);
                if (minute < 0 || minute > 59) return false;
                if (ampm != null && !ampm.isEmpty()) {
                    if (hour < 1 || hour > 12) return false;
                    if (ampm.equalsIgnoreCase("AM")) {
                        if (hour == 12) hour = 0;
                    } else {
                        if (hour < 12) hour += 12;
                    }
                } else {
                    if (hour < 0 || hour > 23) return false;
                }
                LocalTime.of(hour, minute);
                return true;
            } catch (NumberFormatException | DateTimeException ex) {
                return false;
            }
        }

        return false;
    }

    public static String formatTime12Hour(LocalTime time) {
        if (time == null) return ""; // null -> empty string
        return time.format(TIME_FMT_12); // user-friendly display format
    }

    // Simple phone validator – trims and matches regex
    public static boolean isValidPhone(String phone) {
        if (phone == null) return false; // null -> invalid
        String p = phone.trim(); // remove whitespace
        return PHONE_DIGITS.matcher(p).matches(); // return match result
    }

    /**
     * DocumentFilter that allows only digits and an optional single dot.
     * Used to limit JTextField input for numeric values.
     */
    public static class NumericDocumentFilter extends DocumentFilter {
        @Override
        public void insertString(FilterBypass fb, int offset, String string, AttributeSet attr) throws BadLocationException {
            if (string == null) return; // nothing to insert
            StringBuilder sb = new StringBuilder(fb.getDocument().getText(0, fb.getDocument().getLength())); // current text
            sb.insert(offset, string); // simulate insertion
            if (isAcceptable(sb.toString())) super.insertString(fb, offset, string, attr); // allow if acceptable
        }

        @Override
        public void replace(FilterBypass fb, int offset, int length, String text, AttributeSet attrs) throws BadLocationException {
            StringBuilder sb = new StringBuilder(fb.getDocument().getText(0, fb.getDocument().getLength())); // current
            sb.replace(offset, offset + length, text == null ? "" : text); // simulate replace
            if (isAcceptable(sb.toString())) super.replace(fb, offset, length, text, attrs); // allow if acceptable
        }

        @Override
        public void remove(FilterBypass fb, int offset, int length) throws BadLocationException {
            super.remove(fb, offset, length); // removals always allowed
        }

        // Check string only contains digits and at most one dot
        private boolean isAcceptable(String s) {
            if (s.isEmpty()) return true; // empty allowed
            int dots = 0; // dot counter
            for (char c : s.toCharArray()) {
                if (c == '.') dots++; // count dots
                else if (!Character.isDigit(c)) return false; // non-digit -> invalid
                if (dots > 1) return false; // more than one dot -> invalid
            }
            return true; // acceptable
        }
    }
}

/*
 * ValidationUtils.java
 *
 * End description: compact validators used across UI code.
 */
