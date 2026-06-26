/*
 * ResetPasswordFrame.java
 *
 * Description (start):
 * - Easy: This window lets a user reset their password using an OTP sent to email.
 * - Purpose: verify the account by phone and email, then set a new password.
 * - Why: forgot-password flow should be available from the login screen.
 *
 * End of start description.
 */
package com.expensetracker.ui;

import com.expensetracker.DBHelper;
import com.expensetracker.EmailUtil;

import javax.mail.MessagingException;
import javax.swing.*;
import java.awt.*;
import java.util.Random;

public class ResetPasswordFrame extends JFrame {

    private final JTextField phoneField; // input for phone number
    private final JTextField emailField; // input for registered email
    private final JTextField otpField; // input for the OTP code
    private final JPasswordField newPasswordField; // input for new password
    private final JPasswordField confirmPasswordField; // input to confirm new password
    private String sentOtp; // stores the OTP that was sent by email

    public ResetPasswordFrame() {
        setTitle("Expense Tracker - Reset Password"); // window title
        setPreferredSize(new Dimension(1000, 700)); // preferred window size
        setExtendedState(JFrame.MAXIMIZED_BOTH); // open maximized
        setResizable(true); // allow resizing the window
        setLocationRelativeTo(null); // center on screen
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE); // close only this window
        setContentPane(UiKit.gradientBackground(new Color(63, 88, 214), new Color(96, 84, 228))); // gradient background

        JPanel root = new JPanel(new GridBagLayout()); // root panel for centering the card
        root.setOpaque(false); // let background show through
        root.setBorder(BorderFactory.createEmptyBorder(16, 20, 16, 20)); // outer padding
        add(root);

        JPanel card = new JPanel(); // card panel to hold form content
        card.setOpaque(false);
        card.setBackground(new Color(255, 255, 255, 28)); // translucent card background
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(255, 255, 255, 55), 1, true),
            BorderFactory.createEmptyBorder(22, 30, 18, 30)
        )); // border and padding
        card.setPreferredSize(new Dimension(520, 520)); // fixed card size
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS)); // vertical layout inside card
        card.setAlignmentX(Component.CENTER_ALIGNMENT); // center card horizontally

        card.add(Box.createVerticalStrut(8)); // small top spacing
        JLabel title = UiKit.titleLabel("Reset Password", Color.WHITE); // title label
        title.setFont(new Font("Segoe UI", Font.BOLD, 30));
        title.setAlignmentX(Component.CENTER_ALIGNMENT);
        card.add(title);
        card.add(Box.createVerticalStrut(4)); // gap between title and subtitle
        JLabel subtitle = UiKit.subtitleLabel("Send an OTP to your email, then choose a new password", new Color(230, 235, 255));
        subtitle.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        subtitle.setAlignmentX(Component.CENTER_ALIGNMENT);
        card.add(subtitle);
        card.add(Box.createVerticalStrut(20)); // gap before form fields

        JPanel form = new JPanel(); // form container
        form.setOpaque(false);
        form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));
        form.setAlignmentX(Component.CENTER_ALIGNMENT);
        form.setMaximumSize(new Dimension(450, Integer.MAX_VALUE)); // limit width

        form.add(createLabel("Phone Number")); // phone label
        phoneField = createTextField(); // phone field
        form.add(createInputRow("📞", phoneField)); // row with icon and field
        form.add(Box.createVerticalStrut(10)); // spacing between rows

        form.add(createLabel("Email")); // email label
        emailField = createTextField(); // email field
        form.add(createInputRow("✉️", emailField));
        form.add(Box.createVerticalStrut(10));

        form.add(createLabel("OTP")); // otp label
        otpField = createTextField(); // otp field
        form.add(createInputRow("🔑", otpField));
        form.add(Box.createVerticalStrut(10));

        form.add(createLabel("New Password")); // new password label
        newPasswordField = createPasswordField(); // password input
        form.add(createPasswordRow("🔒", newPasswordField)); // password row with toggle
        form.add(Box.createVerticalStrut(10));

        form.add(createLabel("Confirm Password")); // confirm password label
        confirmPasswordField = createPasswordField(); // confirm field
        form.add(createPasswordRow("🔐", confirmPasswordField));
        form.add(Box.createVerticalStrut(18));

        JButton sendOtpButton = UiKit.primaryButton("Send OTP", new Color(30, 105, 223)); // OTP button
        sendOtpButton.setPreferredSize(new Dimension(450, 42));
        sendOtpButton.setMaximumSize(new Dimension(450, 42));
        sendOtpButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        sendOtpButton.addActionListener(e -> sendOtp()); // when clicked, send OTP

        JButton resetButton = UiKit.primaryButton("Reset Password", new Color(26, 176, 112)); // reset button
        resetButton.setPreferredSize(new Dimension(450, 42));
        resetButton.setMaximumSize(new Dimension(450, 42));
        resetButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        resetButton.addActionListener(e -> resetPassword()); // when clicked, reset password

        JButton backButton = UiKit.secondaryButton("Back", new Color(227, 236, 250), new Color(44, 51, 73)); // back button
        backButton.setPreferredSize(new Dimension(450, 42));
        backButton.setMaximumSize(new Dimension(450, 42));
        backButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        backButton.addActionListener(e -> {
            dispose(); // close this window
            new LoginFrame().setVisible(true); // show login screen again
        });

        form.add(sendOtpButton);
        form.add(Box.createVerticalStrut(10));
        form.add(resetButton);
        form.add(Box.createVerticalStrut(10));
        form.add(backButton);

        card.add(form); // add form to card
        root.add(card, new GridBagConstraints()); // add card to root panel
    }

    // Send a one-time password to the user's registered email.
    // This method checks that the phone and email match an existing account.
    // If the account is valid, the method generates the OTP and sends it.
    private void sendOtp() {
        String phone = phoneField.getText().trim(); // read phone text
        String email = emailField.getText().trim(); // read email text

        if (phone.isEmpty() || email.isEmpty()) { // require both fields
            JOptionPane.showMessageDialog(this, "Please enter phone and email.");
            return;
        }

        try {
            if (!DBHelper.userExists(phone)) { // verify account exists
                JOptionPane.showMessageDialog(this, "No user found with that phone number.");
                return;
            }

            String storedEmail = DBHelper.getUserEmail(phone); // get stored email from DB
            if (storedEmail == null || storedEmail.trim().isEmpty() || !storedEmail.trim().equalsIgnoreCase(email)) {
                JOptionPane.showMessageDialog(this, "Email does not match the registered account.");
                return;
            }

            sentOtp = String.format("%06d", new Random().nextInt(900000) + 100000); // random 6-digit code
            String subject = "Your Expense Tracker password reset code";
            String body = "Your password reset code is: " + sentOtp + "\n\nIf you did not request this, ignore this email.";
            EmailUtil.sendEmail(email, subject, body); // send OTP email
            JOptionPane.showMessageDialog(this, "OTP sent to your email.");
        } catch (MessagingException ex) { // email send error
            JOptionPane.showMessageDialog(this, "Failed to send OTP: " + ex.getMessage());
        } catch (Exception ex) { // any other error
            JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());
        }
    }

    // Reset the user's password after verifying the OTP.
    // It checks the OTP, validates the passwords, and then updates the database.
    private void resetPassword() {
        String phone = phoneField.getText().trim(); // read phone again
        String email = emailField.getText().trim(); // read email again
        String otp = otpField.getText().trim(); // read entered OTP
        String newPassword = new String(newPasswordField.getPassword()).trim(); // read new password
        String confirmPassword = new String(confirmPasswordField.getPassword()).trim(); // read password confirmation

        if (sentOtp == null || sentOtp.isEmpty()) { // OTP must be sent first
            JOptionPane.showMessageDialog(this, "Please send the OTP first.");
            return;
        }

        if (!sentOtp.equals(otp)) { // OTP must match
            JOptionPane.showMessageDialog(this, "Invalid OTP.");
            return;
        }

        if (newPassword.isEmpty() || confirmPassword.isEmpty()) { // both password fields are required
            JOptionPane.showMessageDialog(this, "Please enter and confirm your new password.");
            return;
        }

        if (!newPassword.equals(confirmPassword)) { // passwords must be same
            JOptionPane.showMessageDialog(this, "Passwords do not match.");
            return;
        }

        if (newPassword.length() < 6) { // simple password strength rule
            JOptionPane.showMessageDialog(this, "Password must be at least 6 characters long.");
            return;
        }

        try {
            String storedEmail = DBHelper.getUserEmail(phone); // verify email again
            if (storedEmail == null || storedEmail.trim().isEmpty() || !storedEmail.trim().equalsIgnoreCase(email)) {
                JOptionPane.showMessageDialog(this, "Email does not match the registered account.");
                return;
            }

            if (DBHelper.updateUserPassword(phone, newPassword)) { // update DB with hashed password
                JOptionPane.showMessageDialog(this, "Password reset successfully.");
                dispose(); // close this window
                new LoginFrame().setVisible(true); // return to login
            } else {
                JOptionPane.showMessageDialog(this, "Could not update password.");
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());
        }
    }

    private JLabel createLabel(String text) {
        JLabel label = new JLabel(text); // create a label for form rows
        label.setForeground(new Color(236, 240, 255)); // light text color
        label.setFont(new Font("Segoe UI", Font.BOLD, 14)); // bold label font
        label.setBorder(BorderFactory.createEmptyBorder(0, 2, 5, 0)); // spacing below the label
        label.setAlignmentX(Component.CENTER_ALIGNMENT); // center the label
        return label;
    }

    private JTextField createTextField() {
        JTextField field = new JTextField(); // create standard text field
        field.setFont(new Font("Segoe UI", Font.PLAIN, 13)); // set font for readability
        field.setForeground(new Color(56, 65, 93)); // dark input color
        field.setBorder(BorderFactory.createEmptyBorder(6, 6, 6, 6)); // padding inside field
        field.setOpaque(false); // transparent background for style
        return field;
    }

    private JPasswordField createPasswordField() {
        JPasswordField field = new JPasswordField(); // create password field
        field.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        field.setForeground(new Color(56, 65, 93));
        field.setBorder(BorderFactory.createEmptyBorder(6, 6, 6, 6));
        field.setOpaque(false);
        return field;
    }

    private JPanel createInputRow(String icon, JComponent field) {
        JPanel row = new JPanel(new BorderLayout(6, 0)); // row with icon on left and field in center
        row.setBackground(new Color(246, 249, 255)); // light row background
        row.setMaximumSize(new Dimension(450, 42)); // fixed row height
        row.setPreferredSize(new Dimension(450, 42));
        row.setAlignmentX(Component.CENTER_ALIGNMENT); // center row in form
        row.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(225, 232, 248), 1, true),
            BorderFactory.createEmptyBorder(2, 10, 2, 10)
        )); // row border + padding

        JLabel iconLabel = new JLabel(icon); // emoji icon label
        iconLabel.setForeground(new Color(148, 158, 186));
        iconLabel.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 16));
        row.add(iconLabel, BorderLayout.WEST); // icon at left
        row.add(field, BorderLayout.CENTER); // field fills rest
        return row;
    }

    private JPanel createPasswordRow(String icon, JPasswordField field) {
        JPanel row = new JPanel(new BorderLayout(6, 0)); // password row with toggle button
        row.setBackground(new Color(246, 249, 255));
        row.setMaximumSize(new Dimension(450, 42));
        row.setPreferredSize(new Dimension(450, 42));
        row.setAlignmentX(Component.CENTER_ALIGNMENT);
        row.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(225, 232, 248), 1, true),
            BorderFactory.createEmptyBorder(2, 10, 2, 6)
        ));

        JLabel iconLabel = new JLabel(icon); // icon for password row
        iconLabel.setForeground(new Color(148, 158, 186));
        iconLabel.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 16));
        row.add(iconLabel, BorderLayout.WEST);
        row.add(field, BorderLayout.CENTER);

        char defaultEcho = field.getEchoChar(); // default mask char
        JToggleButton eyeToggle = new JToggleButton(); // show/hide password button
        eyeToggle.setIcon(UiKit.eyeIcon(true, 18, new Color(108, 122, 160)));
        eyeToggle.setSelectedIcon(UiKit.eyeIcon(false, 18, new Color(108, 122, 160)));
        eyeToggle.setForeground(new Color(108, 122, 160));
        eyeToggle.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        eyeToggle.setBorder(BorderFactory.createEmptyBorder(2, 6, 2, 6));
        eyeToggle.setContentAreaFilled(false);
        eyeToggle.setFocusPainted(false);
        eyeToggle.setOpaque(false);
        eyeToggle.addActionListener(e -> {
            if (eyeToggle.isSelected()) {
                field.setEchoChar((char) 0); // show password
            } else {
                field.setEchoChar(defaultEcho); // hide password again
            }
        });
        row.add(eyeToggle, BorderLayout.EAST); // toggle button at right
        return row;
    }
}

/*
 * ResetPasswordFrame.java
 *
 * End description: Forgot-password screen that sends an OTP and allows the user to set a new password.
 */