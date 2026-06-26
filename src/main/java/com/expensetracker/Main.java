package com.expensetracker; // package: top-level namespace for the app

import com.expensetracker.ui.UiKit; // UiKit: shared UI helpers, colors, LAF installer
import javax.swing.SwingUtilities; // SwingUtilities: run GUI code on EDT

/*
 * Main.java
 *
 * Description (start):
 * - Program entry point. Small and explicit.
 * - Responsibilities:
 *   1) Install the application's look-and-feel via `UiKit`.
 *   2) Ensure the SQLite database and tables exist via `DBHelper`.
 *   3) Launch the login UI on the Swing Event Dispatch Thread (EDT).
 *
 * Roman-English line-by-line comments follow below each statement.
 *
 * End of start description.
 */
public class Main {
    // main: program entry point
    public static void main(String[] args) {
        try {
            // Install look-and-feel (colors, fonts, UI defaults) from UiKit
            UiKit.installLookAndFeel(); // UiKit se look-and-feel lagao

            // Initialize the database and apply any schema migrations
            DBHelper.initializeDatabase(); // DB bana ke tables check karo

            // Always create and show Swing windows on the EDT for thread-safety edt=event dispatch thread
            SwingUtilities.invokeLater(() -> // Create and show the login window on the EDT
                    new com.expensetracker.ui.LoginFrame().setVisible(true)
            ); // Login window ko EDT par visible karo

        } catch (Exception e) {
            // If anything fails during startup, print a helpful message and stack
            System.err.println("Initialization failed: " + e.getMessage()); // error print karo
            e.printStackTrace(); // stack trace bhi print karo
        }
    }
}

/*
 * Main.java
 *
 * End description: tiny launcher that keeps startup responsibilities explicit.
 */
