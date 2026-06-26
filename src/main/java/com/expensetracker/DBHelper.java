package com.expensetracker; // Declare that this class is part of the com.expensetracker package.

// Import all classes from java.sql package needed for JDBC operations.
import java.sql.*;
import java.nio.charset.StandardCharsets; // For encoding strings when hashing passwords.
import java.security.MessageDigest; // For hashing passwords securely using SHA-256 algorithm.
import java.security.NoSuchAlgorithmException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap; // For using List and Map collections to store query results in memory.
import java.util.List; // List is used to store multiple rows of expenses, Map is used to store category totals.
import java.util.Map; // Map is used to store category totals, List is used to store expense rows.

/*
 * DBHelper.java
 *
 * Description (start):
 * Very easy: Small helper for database operations used by the expense tracker.
 * - Manages the SQLite file `expenses.db` and ensures required tables/columns exist.
 * - Provides simple CRUD and summary helper methods used by the UI screens.
 * - Comments use short Roman-English notes so the code is easy to read.
 *
 * End of start description.
 */
public class DBHelper {

    // Location of the database file. The file will be created in the project folder.
    private static final String URL = "jdbc:sqlite:expenses.db";

    // Open and return a connection to the database file.
    // A connection is like opening the book so we can read or write data.
    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL);
    }

    // Create tables if they do not exist. Call this once at the start of the program.
    public static void initializeDatabase() throws SQLException {
        try (Connection conn = getConnection(); Statement s = conn.createStatement()) {
            // Turn on foreign key checks (helps keep data linked correctly).
            s.executeUpdate("PRAGMA foreign_keys = ON;");

            // Create a table to store users (id, name, phone).
            s.executeUpdate(
                "CREATE TABLE IF NOT EXISTS users (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "name TEXT NOT NULL," +
                "phone TEXT UNIQUE NOT NULL" +
                ", monthly_budget REAL DEFAULT 0" +
                ");"
            );

            // Create a table to store expenses for each user.
            s.executeUpdate(
                "CREATE TABLE IF NOT EXISTS expenses (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "user_phone TEXT NOT NULL," +
                "category TEXT NOT NULL," +
                "amount REAL NOT NULL," +
                "time TEXT," +
                "day TEXT," +
                "month TEXT," +
                "year TEXT," +
                "FOREIGN KEY(user_phone) REFERENCES users(phone)" +
                ");"
            );
// Ensure expenses table has title column (added in a later version, so may not exist in older DBs)
            ensureExpenseColumn(conn, "title", "TEXT NOT NULL DEFAULT ''");

            // Ensure users table has monthly_budget column (older DBs may not)
            ensureUserColumn(conn, "monthly_budget", "REAL DEFAULT 0");
            // Ensure users table has email column for signup + OTP flows
            ensureUserColumn(conn, "email", "TEXT DEFAULT ''");
            // Ensure users table has password_hash column for login + reset flows
            ensureUserColumn(conn, "password_hash", "TEXT DEFAULT ''");
            // Create categories table to store default categories and optional icon paths
            s.executeUpdate(
                "CREATE TABLE IF NOT EXISTS categories (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "name TEXT UNIQUE NOT NULL," +
                "icon TEXT DEFAULT ''" +
                ");"
            );

            // Seed default categories if table empty
            try (ResultSet rs = s.executeQuery("SELECT COUNT(1) as cnt FROM categories;")) {
                if (rs.next()) {
                    int cnt = rs.getInt("cnt");
                    if (cnt == 0) {
                        // insert a few defaults with resource icon paths (packaged under /icons/...)
                        s.executeUpdate("INSERT INTO categories(name, icon) VALUES ('Entertainment', '/icons/entertainment.png');");
                        s.executeUpdate("INSERT INTO categories(name, icon) VALUES ('Shopping', '/icons/shopping.png');");
                        s.executeUpdate("INSERT INTO categories(name, icon) VALUES ('Food', '/icons/food.png');");
                        s.executeUpdate("INSERT INTO categories(name, icon) VALUES ('Grocery', '/icons/grocery.png');");
                    }
                }
            }
        }
    }

    private static void ensureExpenseColumn(Connection conn, String columnName, String columnDefinition) throws SQLException {
        boolean exists = false;
        try (Statement s = conn.createStatement(); ResultSet rs = s.executeQuery("PRAGMA table_info(expenses);") ) {
            while (rs.next()) {
                if (columnName.equalsIgnoreCase(rs.getString("name"))) {
                    exists = true;
                    break;
                }
            }
        }

        if (!exists) {
            try (Statement s = conn.createStatement()) {
                s.executeUpdate("ALTER TABLE expenses ADD COLUMN " + columnName + " " + columnDefinition + ";");
            }
        }
    }

    // Add a new user. name = user's name, phone = user's phone number.
    // Returns true if the user was added successfully.
    public static boolean addUser(String name, String phone) throws SQLException {
        String sql = "INSERT INTO users(name, phone, password_hash) VALUES (?, ?, ?);";
        try (Connection conn = getConnection(); PreparedStatement p = conn.prepareStatement(sql)) {
            p.setString(1, name);
            p.setString(2, phone);
            p.setString(3, "");
            return p.executeUpdate() == 1;
        }
    }

    // Add a new user with email. Returns true if added.
    public static boolean addUser(String name, String phone, String email) throws SQLException {
        String sql = "INSERT INTO users(name, phone, email, password_hash) VALUES (?, ?, ?, ?);";
        try (Connection conn = getConnection(); PreparedStatement p = conn.prepareStatement(sql)) {
            p.setString(1, name);
            p.setString(2, phone);
            p.setString(3, email == null ? "" : email);
            p.setString(4, "");
            return p.executeUpdate() == 1;
        }
    }

    // Add a new user with email and password. Password is stored as a SHA-256 hash.
    public static boolean addUser(String name, String phone, String email, String password) throws SQLException {
        String sql = "INSERT INTO users(name, phone, email, password_hash) VALUES (?, ?, ?, ?);";
        try (Connection conn = getConnection(); PreparedStatement p = conn.prepareStatement(sql)) {
            p.setString(1, name);
            p.setString(2, phone);
            p.setString(3, email == null ? "" : email);
            p.setString(4, hashPassword(password));
            return p.executeUpdate() == 1;
        }
    }

    // Check if a user already exists by phone number.
    public static boolean userExists(String phone) throws SQLException {
        String sql = "SELECT 1 FROM users WHERE phone = ? LIMIT 1;";
        try (Connection conn = getConnection(); PreparedStatement p = conn.prepareStatement(sql)) {
            p.setString(1, phone);
            try (ResultSet rs = p.executeQuery()) {
                return rs.next();
            }
        }
    }

    // Get the user's name for the welcome screen.
    public static String getUserName(String phone) throws SQLException {
        String sql = "SELECT name FROM users WHERE phone = ? LIMIT 1;"; // SQL query to get the name of the user with the given phone number.
        try (Connection conn = getConnection(); PreparedStatement p = conn.prepareStatement(sql)) { // Open a connection to the database and prepare the SQL statement. prepared statements help prevent SQL injection and allow us to set parameters safely.
            p.setString(1, phone);
            try (ResultSet rs = p.executeQuery()) { // Execute the query and get the result set. The result set is like a table of data returned by the query.
                if (rs.next()) {
                    return rs.getString("name"); // If there is a result, return the value in the "name" column. This is the user's name.
                }
            }
        }
        return null;
    }

    // Get the stored email address for a user phone number.
    public static String getUserEmail(String phone) throws SQLException {
        String sql = "SELECT email FROM users WHERE phone = ? LIMIT 1;";
        try (Connection conn = getConnection(); PreparedStatement p = conn.prepareStatement(sql)) {
            p.setString(1, phone);
            try (ResultSet rs = p.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("email");
                }
            }
        }
        return null;
    }

    // Check if a user exists and their password matches the stored value.
    public static boolean authenticateUser(String phone, String name, String password) throws SQLException {
        String sql = "SELECT password_hash FROM users WHERE phone = ? LIMIT 1;";
        try (Connection conn = getConnection(); PreparedStatement p = conn.prepareStatement(sql)) {
            p.setString(1, phone);
            try (ResultSet rs = p.executeQuery()) {
                if (!rs.next()) {
                    return false; // No user with this phone number found, authentication fails.
                }
             // Get the stored password hash from the database and compare it to the hash of the entered password.
                String storedHash = rs.getString("password_hash");
                String enteredPassword = password == null ? "" : password;
                if (storedHash == null || storedHash.trim().isEmpty()) {
                    // Older accounts created before password support can still open with an empty password.
                    return enteredPassword.isEmpty(); // If no password was set, only allow authentication if the entered password is also empty.
                }
// Hash the entered password and compare it to the stored hash. If they match, authentication is successful.
                String enteredHash = hashPassword(enteredPassword);
                boolean matches = storedHash.equals(enteredHash);
                // DEBUG: log for troubleshooting if needed
                if (!matches) {
                    System.out.println("Password mismatch for phone: " + phone);
                    System.out.println("Stored hash: " + storedHash);
                    System.out.println("Entered hash: " + enteredHash);
                }
                return matches;
            }
        }
    }

    // Update the stored password hash for a user phone number.
    public static boolean updateUserPassword(String phone, String newPassword) throws SQLException {
        String sql = "UPDATE users SET password_hash = ? WHERE phone = ?;";
        try (Connection conn = getConnection(); PreparedStatement p = conn.prepareStatement(sql)) {
            p.setString(1, hashPassword(newPassword));
            p.setString(2, phone);
            return p.executeUpdate() == 1;
        }
    }

    // Add a new expense for a given user phone.
    // category = Food/Shopping etc, title = short note, amount = number, date/time as text.
    public static boolean addExpense(String userPhone, String category, double amount, String time, String day, String month, String year) throws SQLException {
        return addExpense(userPhone, category, "", amount, time, day, month, year);
    }

    public static boolean addExpense(String userPhone, String category, String title, double amount, String time, String day, String month, String year) throws SQLException {
        String sql = "INSERT INTO expenses(user_phone, category, title, amount, time, day, month, year) VALUES (?, ?, ?, ?, ?, ?, ?, ?);";
        try (Connection conn = getConnection(); PreparedStatement p = conn.prepareStatement(sql)) {
            p.setString(1, userPhone);
            p.setString(2, category);
            p.setString(3, title);
            p.setDouble(4, amount);
            p.setString(5, time);
            p.setString(6, day);
            p.setString(7, month);
            p.setString(8, year);
            return p.executeUpdate() == 1;
        }
    }

    // English: Return a list of expenses for a user.
    // Roman: DB se us user ke saare kharche nikaal kar list return karta hai.
    // Each list item is a String[]: {id, category, title, amount, time, day, month, year}
    public static List<String[]> getExpensesForUser(String userPhone) throws SQLException {
        String sql = "SELECT id, category, title, amount, time, day, month, year FROM expenses WHERE user_phone = ? ORDER BY id DESC;";
        List<String[]> rows = new ArrayList<>();
        try (Connection conn = getConnection(); PreparedStatement p = conn.prepareStatement(sql)) {
            p.setString(1, userPhone);
            try (ResultSet rs = p.executeQuery()) {
                while (rs.next()) {
                    String[] r = new String[8];
                    r[0] = String.valueOf(rs.getInt("id"));
                    r[1] = rs.getString("category");
                    r[2] = rs.getString("title");
                    r[3] = String.valueOf(rs.getDouble("amount"));
                    r[4] = rs.getString("time");
                    r[5] = rs.getString("day");
                    r[6] = rs.getString("month");
                    r[7] = rs.getString("year");
                    rows.add(r);
                }
            }
        }
        return rows;
    }

    // English: Compute totals grouped by category for a user.
    // Roman: Har category ka total nikalo (category ke hisaab se jama rakho).
    public static Map<String, Double> getTotalsByCategory(String userPhone) throws SQLException {
        String sql = "SELECT category, SUM(amount) as total FROM expenses WHERE user_phone = ? GROUP BY category;";
        Map<String, Double> totals = new HashMap<>();
        try (Connection conn = getConnection(); PreparedStatement p = conn.prepareStatement(sql)) {
            p.setString(1, userPhone);
            try (ResultSet rs = p.executeQuery()) {
                while (rs.next()) {
                    totals.put(rs.getString("category"), rs.getDouble("total"));
                }
            }
        }
        return totals;
    }

    // English: Delete an expense by id.
    // Roman: Kisi expense ko uske id se delete karne ke liye.
    public static boolean deleteExpense(int id) throws SQLException {
        String sql = "DELETE FROM expenses WHERE id = ?;";
        try (Connection conn = getConnection(); PreparedStatement p = conn.prepareStatement(sql)) {
            p.setInt(1, id);
            return p.executeUpdate() == 1;
        }
    }

    // English: Update an existing expense row by id.
    // Roman: Kisi kharche (expense) ko uske id se update karo — category, amount, time, date fields.
    public static boolean updateExpense(int id, String category, double amount, String time, String day, String month, String year) throws SQLException {
        return updateExpense(id, category, "", amount, time, day, month, year);
    }

    public static boolean updateExpense(int id, String category, String title, double amount, String time, String day, String month, String year) throws SQLException {
        String sql = "UPDATE expenses SET category = ?, title = ?, amount = ?, time = ?, day = ?, month = ?, year = ? WHERE id = ?;"; // Update the expense row with the given id, setting new values for category, title, amount, time, and date fields.
        try (Connection conn = getConnection(); PreparedStatement p = conn.prepareStatement(sql)) { // Open a connection and prepare the SQL statement.
            p.setString(1, category);
            p.setString(2, title);
            p.setDouble(3, amount);
            p.setString(4, time);
            p.setString(5, day);
            p.setString(6, month);
            p.setString(7, year);
            p.setInt(8, id);
            return p.executeUpdate() == 1; // returns true if one row was updated
        }
    }

    // English: Get expenses for a given month and year (both strings, e.g. "5","2026").
    // Roman: Diya gaya month aur year ke hisaab se expenses laao.
    public static List<String[]> getExpensesForUserByMonth(String userPhone, String month, String year) throws SQLException {
        String sql = "SELECT id, category, amount, time, day, month, year FROM expenses WHERE user_phone = ? AND month = ? AND year = ? ORDER BY id DESC;";
        List<String[]> rows = new ArrayList<>();
        try (Connection conn = getConnection(); PreparedStatement p = conn.prepareStatement(sql)) {
            p.setString(1, userPhone);
            p.setString(2, month);
            p.setString(3, year);
            try (ResultSet rs = p.executeQuery()) {
                while (rs.next()) {
                    String[] r = new String[7];
                    r[0] = String.valueOf(rs.getInt("id"));
                    r[1] = rs.getString("category");
                    r[2] = String.valueOf(rs.getDouble("amount"));
                    r[3] = rs.getString("time");
                    r[4] = rs.getString("day");
                    r[5] = rs.getString("month");
                    r[6] = rs.getString("year");
                    rows.add(r);
                }
            }
        }
        return rows;
    }

    // English: Get totals by category for a specific month and year.
    // Roman: Kisi month-year ke liye har category ka total nikaalo.
    public static Map<String, Double> getTotalsByCategoryByMonth(String userPhone, String month, String year) throws SQLException {
        String sql = "SELECT category, SUM(amount) as total FROM expenses WHERE user_phone = ? AND month = ? AND year = ? GROUP BY category;";
        Map<String, Double> totals = new HashMap<>();
        try (Connection conn = getConnection(); PreparedStatement p = conn.prepareStatement(sql)) {
            p.setString(1, userPhone);
            p.setString(2, month);
            p.setString(3, year);
            try (ResultSet rs = p.executeQuery()) {
                while (rs.next()) {
                    totals.put(rs.getString("category"), rs.getDouble("total"));
                }
            }
        }
        return totals;
    }

    // English: Get expenses for a specific day, month and year.
    // Roman: Kisi khaas din (day), mahina (month) aur saal (year) ke kharche lao.
    public static List<String[]> getExpensesForUserByDayMonthYear(String userPhone, String day, String month, String year) throws SQLException {
        String sql = "SELECT id, category, amount, time, day, month, year FROM expenses WHERE user_phone = ? AND day = ? AND month = ? AND year = ? ORDER BY id DESC;";
        List<String[]> rows = new ArrayList<>();
        try (Connection conn = getConnection(); PreparedStatement p = conn.prepareStatement(sql)) {
            p.setString(1, userPhone);
            p.setString(2, day);
            p.setString(3, month);
            p.setString(4, year);
            try (ResultSet rs = p.executeQuery()) {
                while (rs.next()) {
                    String[] r = new String[7];
                    r[0] = String.valueOf(rs.getInt("id"));
                    r[1] = rs.getString("category");
                    r[2] = String.valueOf(rs.getDouble("amount"));
                    r[3] = rs.getString("time");
                    r[4] = rs.getString("day");
                    r[5] = rs.getString("month");
                    r[6] = rs.getString("year");
                    rows.add(r);
                }
            }
        }
        return rows;
    }

    // English: Get totals by category for a specific day, month and year.
    // Roman: Us din/mahina/saal ke liye category-wise totals lao.
    public static Map<String, Double> getTotalsByCategoryByDayMonthYear(String userPhone, String day, String month, String year) throws SQLException {
        String sql = "SELECT category, SUM(amount) as total FROM expenses WHERE user_phone = ? AND day = ? AND month = ? AND year = ? GROUP BY category;";
        Map<String, Double> totals = new HashMap<>();
        try (Connection conn = getConnection(); PreparedStatement p = conn.prepareStatement(sql)) {
            p.setString(1, userPhone);
            p.setString(2, day);
            p.setString(3, month);
            p.setString(4, year);
            try (ResultSet rs = p.executeQuery()) {
                while (rs.next()) {
                    totals.put(rs.getString("category"), rs.getDouble("total"));
                }
            }
        }
        return totals;
    }

    // English: Get expenses between two dates (inclusive). Dates in format YYYY-MM-DD.
    // Roman: Do tareekh ke beech ke sab kharche nikaalo (shamil dono din).
    public static List<String[]> getExpensesForUserByRange(String userPhone, String startDate, String endDate) throws SQLException {
        String sql = "SELECT id, category, amount, time, day, month, year FROM expenses WHERE user_phone = ? AND (CAST(year AS INTEGER) || '-' || printf('%02d', CAST(month AS INTEGER)) || '-' || printf('%02d', CAST(day AS INTEGER))) BETWEEN ? AND ? ORDER BY id DESC;";
        List<String[]> rows = new ArrayList<>();
        try (Connection conn = getConnection(); PreparedStatement p = conn.prepareStatement(sql)) {
            p.setString(1, userPhone);
            p.setString(2, startDate);
            p.setString(3, endDate);
            try (ResultSet rs = p.executeQuery()) {
                while (rs.next()) {
                    String[] r = new String[7];
                    r[0] = String.valueOf(rs.getInt("id"));
                    r[1] = rs.getString("category");
                    r[2] = String.valueOf(rs.getDouble("amount"));
                    r[3] = rs.getString("time");
                    r[4] = rs.getString("day");
                    r[5] = rs.getString("month");
                    r[6] = rs.getString("year");
                    rows.add(r);
                }
            }
        }
        return rows;
    }

    // English: Get totals by category between two dates (inclusive).
    // Roman: Do tareekh ke beech har category ka total nikaalo.
    public static Map<String, Double> getTotalsByCategoryByRange(String userPhone, String startDate, String endDate) throws SQLException {
        String sql = "SELECT category, SUM(amount) as total FROM expenses WHERE user_phone = ? AND (CAST(year AS INTEGER) || '-' || printf('%02d', CAST(month AS INTEGER)) || '-' || printf('%02d', CAST(day AS INTEGER))) BETWEEN ? AND ? GROUP BY category;";
        Map<String, Double> totals = new HashMap<>();
        try (Connection conn = getConnection(); PreparedStatement p = conn.prepareStatement(sql)) {
            p.setString(1, userPhone);
            p.setString(2, startDate);
            p.setString(3, endDate);
            try (ResultSet rs = p.executeQuery()) {
                while (rs.next()) {
                    totals.put(rs.getString("category"), rs.getDouble("total"));
                }
            }
        }
        return totals;
    }

    // Get icon path for a given category name (may be a resource path like '/icons/food.png' or a filesystem path).
    public static String getCategoryIcon(String categoryName) throws SQLException {
        String sql = "SELECT icon FROM categories WHERE name = ? LIMIT 1;";
        try (Connection conn = getConnection(); PreparedStatement p = conn.prepareStatement(sql)) {
            p.setString(1, categoryName);
            try (ResultSet rs = p.executeQuery()) {
                if (rs.next()) return rs.getString("icon");
            }
        }
        return ""; // no icon known
    }

    // Update or insert icon path for a category name.
    public static boolean updateCategoryIcon(String categoryName, String iconPath) throws SQLException {
        String sql = "UPDATE categories SET icon = ? WHERE name = ?;";
        try (Connection conn = getConnection(); PreparedStatement p = conn.prepareStatement(sql)) {
            p.setString(1, iconPath);
            p.setString(2, categoryName);
            int updated = p.executeUpdate();
            if (updated == 1) return true;
        }
        // if not updated, try insert
        String ins = "INSERT OR IGNORE INTO categories(name, icon) VALUES (?, ?);";
        try (Connection conn = getConnection(); PreparedStatement p = conn.prepareStatement(ins)) {
            p.setString(1, categoryName);
            p.setString(2, iconPath);
            return p.executeUpdate() == 1;
        }
    }

    // Ensure the users table has a specific column (used for adding monthly_budget to older DBs)
    private static void ensureUserColumn(Connection conn, String columnName, String columnDefinition) throws SQLException {
        boolean exists = false;
        try (Statement s = conn.createStatement(); ResultSet rs = s.executeQuery("PRAGMA table_info(users);") ) {
            while (rs.next()) {
                if (columnName.equalsIgnoreCase(rs.getString("name"))) {
                    exists = true;
                    break;
                }
            }
        }

        if (!exists) {
            try (Statement s = conn.createStatement()) {
                s.executeUpdate("ALTER TABLE users ADD COLUMN " + columnName + " " + columnDefinition + ";");
            }
        }
    }

    private static String hashPassword(String password) {
        String value = password == null ? "" : password;
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashed = digest.digest(value.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : hashed) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException("SHA-256 not available", ex);
        }
    }

    // Get the user's monthly budget amount. Returns 0.0 if not set or user not found.
    public static double getUserMonthlyBudget(String phone) throws SQLException {
        String sql = "SELECT monthly_budget FROM users WHERE phone = ? LIMIT 1;";
        try (Connection conn = getConnection(); PreparedStatement p = conn.prepareStatement(sql)) {
            p.setString(1, phone);
            try (ResultSet rs = p.executeQuery()) {
                if (rs.next()) {
                    return rs.getDouble("monthly_budget");
                }
            }
        }
        return 0.0;
    }

    // Update (or set) the user's monthly budget. Returns true when update applied.
    public static boolean updateUserMonthlyBudget(String phone, double amount) throws SQLException {
        String sql = "UPDATE users SET monthly_budget = ? WHERE phone = ?;";
        try (Connection conn = getConnection(); PreparedStatement p = conn.prepareStatement(sql)) {
            p.setDouble(1, amount);
            p.setString(2, phone);
            return p.executeUpdate() == 1;
        }
    }

    // Get the user's current month's total spending
    public static double getCurrentMonthSpending(String phone) throws SQLException {
        LocalDate today = LocalDate.now();
        Map<String, Double> totals = getTotalsByCategoryByMonth(phone, String.valueOf(today.getMonthValue()), String.valueOf(today.getYear()));
        double sum = 0.0;
        for (Double v : totals.values()) {
            sum += v;
        }
        return sum;
    }

    // Check if adding an expense would exceed the budget
    public static boolean wouldExceedBudget(String phone, double expenseAmount) throws SQLException {
        double budget = getUserMonthlyBudget(phone);
        if (budget <= 0) return false; // no budget set
        
        double currentSpending = getCurrentMonthSpending(phone);
        return (currentSpending + expenseAmount) > budget;
    }

    // Get budget status as percentage (0-100)
    public static double getBudgetPercentage(String phone) throws SQLException {
        double budget = getUserMonthlyBudget(phone);
        if (budget <= 0) return 0;
        
        double currentSpending = getCurrentMonthSpending(phone);
        return (currentSpending / budget) * 100;
    }

    /*
     * DBHelper.java
     *
     * End description:
     * Small, easy DB helper used by the UI.
     * - Provides simple functions to create, read, update, delete expenses and users.
     * - Uses plain JDBC and a local SQLite file `expenses.db` so there is no external server.
     */
}
