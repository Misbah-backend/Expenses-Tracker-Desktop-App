package com.expensetracker; // package declaration

import java.util.List; // for lists
import java.util.Map; // for maps

/**
 * SmokeTest.java
 *
 * Description (start):
 * Very easy: small program to exercise DBHelper methods quickly.
 * - It initializes DB, creates a sample user, inserts two expenses, updates and deletes one,
 *   and prints totals. Use for quick manual checks while developing.
 * - Run: mvn exec:java -Dexec.mainClass=com.expensetracker.SmokeTest
 *
 * End of start description.
 */
public class SmokeTest {
    public static void main(String[] args) {
        String phone = "9998887770"; // sample phone used for test data
        try {
            System.out.println("Initializing database..."); // message
            DBHelper.initializeDatabase(); // create tables if missing

            if (!DBHelper.userExists(phone)) { // check user present
                System.out.println("Creating user Alice, phone=" + phone); // log
                DBHelper.addUser("Alice", phone); // add user
            } else {
                System.out.println("User already exists: " + phone); // already present
            }

            String name = DBHelper.getUserName(phone); // fetch name
            System.out.println("User name resolved: " + name); // print

            System.out.println("Adding expense 1..."); // insert example expenses
            DBHelper.addExpense(phone, "Food", "Lunch", 12.5, "12:00", "1", "5", "2026");
            System.out.println("Adding expense 2...");
            DBHelper.addExpense(phone, "Travel", "Bus", 5.75, "09:30", "2", "5", "2026");

            List<String[]> rows = DBHelper.getExpensesForUser(phone); // read back expenses
            System.out.println("Expenses for " + phone + ":");
            for (String[] r : rows) {
                System.out.printf("  id=%s category=%s title=%s amount=%s date=%s/%s/%s time=%s\n", r[0], r[1], r[2], r[3], r[5], r[6], r[7], r[4]);
            }

            if (!rows.isEmpty()) { // if we have at least one row
                int id = Integer.parseInt(rows.get(0)[0]); // take first id
                System.out.println("Updating first expense id=" + id + " -> amount=99.99"); // log
                DBHelper.updateExpense(id, rows.get(0)[1], rows.get(0)[2], 99.99, rows.get(0)[4], rows.get(0)[5], rows.get(0)[6], rows.get(0)[7]); // update

                System.out.println("Totals by category:"); // print totals
                Map<String, Double> totals = DBHelper.getTotalsByCategory(phone);
                for (Map.Entry<String, Double> e : totals.entrySet()) System.out.println("  " + e.getKey() + " -> " + e.getValue());

                System.out.println("Deleting first expense id=" + id); // delete row
                DBHelper.deleteExpense(id);
            }

            System.out.println("Final expenses list:"); // final list
            rows = DBHelper.getExpensesForUser(phone);
            for (String[] r : rows) System.out.printf("  id=%s category=%s title=%s amount=%s\n", r[0], r[1], r[2], r[3]);

            System.out.println("DB smoke test completed."); // done

            // Quick password reset verification block
            String testPhone = "9998887770";
            String originalPassword = "password123";
            String newPassword = "newPassword123";
            if (!DBHelper.userExists(testPhone)) {
                System.out.println("Creating test user with phone=" + testPhone);
                DBHelper.addUser("Alice", testPhone, "alice@example.com", originalPassword);
            }
            System.out.println("Authenticating with original password: " + DBHelper.authenticateUser(testPhone, "Alice", originalPassword));
            System.out.println("Updating password to newPassword123: " + DBHelper.updateUserPassword(testPhone, newPassword));
            System.out.println("Authenticating with old password after update: " + DBHelper.authenticateUser(testPhone, "Alice", originalPassword));
            System.out.println("Authenticating with new password after update: " + DBHelper.authenticateUser(testPhone, "Alice", newPassword));
        } catch (Exception ex) {
            System.err.println("SmokeTest error: " + ex.getMessage()); // error
            ex.printStackTrace(); // stack
        }
    }
}

/*
 * SmokeTest.java
 *
 * End description: quick helper to exercise DBHelper while developing.
 */
