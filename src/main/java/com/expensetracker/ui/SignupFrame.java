/*
 * SignupFrame.java
 *
 * Description (start):
 * Easy: This file builds the Sign Up window where new users create an account.
 * - Purpose: collect name and phone, validate, then save in DB.
 * - Why: new users need to register before using the app.
 *
 * End of start description.
 */
package com.expensetracker.ui; // ui package

import com.expensetracker.DBHelper; // DB helper to add user
import com.expensetracker.EmailUtil;
import com.expensetracker.ValidationUtils; // validation utilities for phone

import javax.swing.*; // Swing
import javax.mail.MessagingException;
import javax.swing.SwingWorker;
import java.util.Random;
import java.util.regex.Pattern;
import java.awt.*; // AWT
import java.awt.event.FocusAdapter; // focus listener
import java.awt.event.FocusEvent; // focus event
import java.awt.event.MouseAdapter; // mouse listener
import java.awt.event.MouseEvent; // mouse event

public class SignupFrame extends JFrame { // sign up window class

    private final JTextField nameField; // name input field
    private final JTextField phoneField; // phone input field
    private final JPasswordField passwordField; // password input field
    private final JPasswordField confirmPasswordField; // confirm password input field
    private final Timer titlePulseTimer; // timer for title animation
    private static final String NAME_PLACEHOLDER = "Enter your name"; // placeholder text
    private static final String PHONE_PLACEHOLDER = "Enter your phone number"; // placeholder text
    private static final String EMAIL_PLACEHOLDER = "Enter your email address"; // placeholder
    private final JTextField emailField; // email input

    public SignupFrame() { // constructor builds UI
        setTitle("Expense Tracker - Sign Up"); // window title
        setPreferredSize(new Dimension(1000, 700)); // prefer large size
        setExtendedState(JFrame.MAXIMIZED_BOTH); // start maximized like other screens
        setResizable(true); // allow resize
        setLocationRelativeTo(null); // center screen
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE); // dispose window on close
        setContentPane(UiKit.gradientBackground(new Color(61, 193, 135), new Color(46, 181, 121))); // green gradient background

        JPanel root = new JPanel(new GridBagLayout()); // root container
        root.setOpaque(false); // transparent
        root.setBorder(BorderFactory.createEmptyBorder(16, 20, 16, 20)); // padding
        add(root); // add root to frame

        JPanel card = new JPanel(); // center card
        card.setOpaque(false); // transparent
        card.setBackground(new Color(255, 255, 255, 28)); // overlay
        card.setBorder(BorderFactory.createCompoundBorder( // border + padding
            BorderFactory.createLineBorder(new Color(255, 255, 255, 55), 1, true),
            BorderFactory.createEmptyBorder(22, 30, 18, 30)
        ));
        card.setPreferredSize(new Dimension(520, 620)); // card size large enough for full form
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS)); // vertical layout
        card.setAlignmentX(Component.CENTER_ALIGNMENT); // center

        JPanel iconWrap = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0)); // icon wrapper
        iconWrap.setOpaque(false); // transparent
        iconWrap.add(UiKit.walletIcon(80)); // wallet icon
        iconWrap.setAlignmentX(Component.CENTER_ALIGNMENT); // center
        card.add(iconWrap); // add

        card.add(Box.createVerticalStrut(6)); // spacing
        JLabel title = UiKit.titleLabel("Create New Account", Color.WHITE); // title label
        title.setFont(new Font("Segoe UI", Font.BOLD, 30)); // font
        title.setAlignmentX(Component.CENTER_ALIGNMENT); // center
        card.add(title); // add title
        card.add(Box.createVerticalStrut(2)); // small gap
        JLabel subtitle = UiKit.subtitleLabel("Sign up to get started", new Color(230, 235, 255)); // subtitle
        subtitle.setFont(new Font("Segoe UI", Font.PLAIN, 12)); // font
        subtitle.setAlignmentX(Component.CENTER_ALIGNMENT); // center
        card.add(subtitle); // add subtitle
        titlePulseTimer = UiKit.startPulse(40, Color.WHITE, new Color(226, 245, 238), title, subtitle); // start pulse
        addWindowListener(new java.awt.event.WindowAdapter() { // stop timer when window closes
            @Override
            public void windowClosed(java.awt.event.WindowEvent e) {
                titlePulseTimer.stop(); // stop animation timer
            }
        });
        card.add(Box.createVerticalStrut(12)); // spacing before form

        JPanel form = new JPanel(); // form container
        form.setOpaque(false); // transparent
        form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS)); // vertical
        form.setAlignmentX(Component.CENTER_ALIGNMENT); // center
        form.setMaximumSize(new Dimension(450, Integer.MAX_VALUE)); // width cap

        form.add(createFieldLabelPanel("Name")); // name label
        nameField = createInputField(NAME_PLACEHOLDER); // name input
        form.add(createInputRow("👤", nameField)); // row with icon and field
        form.add(Box.createVerticalStrut(8)); // gap

        form.add(createFieldLabelPanel("Phone Number")); // phone label
        phoneField = createInputField(PHONE_PLACEHOLDER); // phone input
        form.add(createInputRow("📞", phoneField)); // row with icon and field
        form.add(Box.createVerticalStrut(8)); // gap

        form.add(createFieldLabelPanel("Email")); // email label
        emailField = createInputField(EMAIL_PLACEHOLDER);
        form.add(createInputRow("✉️", emailField));
        form.add(Box.createVerticalStrut(12)); // gap

        form.add(createFieldLabelPanel("Password")); // password label
        passwordField = createPasswordField();
        form.add(createPasswordRow("🔒", passwordField));
        form.add(Box.createVerticalStrut(12)); // gap

        form.add(createFieldLabelPanel("Confirm Password")); // confirm password label
        confirmPasswordField = createPasswordField();
        form.add(createPasswordRow("🔐", confirmPasswordField));
        form.add(Box.createVerticalStrut(10)); // gap

        JButton signupButton = UiKit.primaryButton("Sign Up", new Color(26, 176, 112)); // green signup button
        signupButton.setPreferredSize(new Dimension(450, 42)); // size
        signupButton.setMaximumSize(new Dimension(450, 42)); // max
        signupButton.setAlignmentX(Component.CENTER_ALIGNMENT); // center

        signupButton.addActionListener(e -> handleSignup()); // on click, call handleSignup

        JButton backButton = UiKit.secondaryButton("Back", new Color(227, 236, 250), new Color(44, 51, 73)); // back button
        backButton.setPreferredSize(new Dimension(450, 42)); // size
        backButton.setMaximumSize(new Dimension(450, 42)); // max
        backButton.setAlignmentX(Component.CENTER_ALIGNMENT); // center
        backButton.addActionListener(e -> { // go back to login on click
            dispose(); // close signup window
            new LoginFrame().setVisible(true); // show login
        });

        JPanel buttons = new JPanel(new GridLayout(1, 2, 14, 0)); // two buttons side-by-side
        buttons.setOpaque(false); // transparent
        buttons.setMaximumSize(new Dimension(450, 42)); // keep button row inside card
        buttons.add(signupButton); // add signup
        buttons.add(backButton); // add back
        form.add(buttons); // add buttons to form

        form.add(Box.createVerticalStrut(8)); // spacing
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.CENTER, 8, 0)); // footer with login link
        footer.setOpaque(false); // transparent
        footer.setAlignmentX(Component.CENTER_ALIGNMENT); // center
        JLabel footerText = new JLabel("Already have an account?"); // footer text
        footerText.setForeground(new Color(233, 237, 255)); // color
        footerText.setFont(new Font("Segoe UI", Font.PLAIN, 14)); // font
        JLabel loginLink = new JLabel("Login"); // login link label
        loginLink.setForeground(new Color(70, 226, 255)); // link color
        loginLink.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)); // hand cursor
        loginLink.setFont(new Font("Segoe UI", Font.BOLD, 14)); // bold
        loginLink.addMouseListener(new MouseAdapter() { // on click open login
            @Override
            public void mouseClicked(MouseEvent e) {
                dispose(); // close signup
                new LoginFrame().setVisible(true); // open login
            }
        });
        footer.add(footerText); // add footer text
        footer.add(loginLink); // add link
        form.add(footer); // add footer to form

        card.add(form); // add form to card

        root.add(card); // add card to root (GridBag centers card automatically)
    }

    private JPanel createFieldLabelPanel(String text) { // helper to make small label row above input
        JLabel label = new JLabel(text); // create label
        label.setForeground(new Color(236, 240, 255)); // light color
        label.setFont(new Font("Segoe UI", Font.BOLD, 14)); // bold font
        label.setBorder(BorderFactory.createEmptyBorder(0, 2, 5, 0)); // spacing below label

        JPanel panel = new JPanel(new BorderLayout()); // panel to hold label left aligned
        panel.setOpaque(false); // transparent
        panel.setMaximumSize(new Dimension(450, 22)); // size cap
        panel.setPreferredSize(new Dimension(450, 22)); // preferred
        panel.add(label, BorderLayout.WEST); // add to left
        panel.setAlignmentX(Component.CENTER_ALIGNMENT); // center panel
        return panel; // return
    }

    private JTextField createInputField(String placeholder) { // helper to create text field with placeholder
        JTextField field = new JTextField(placeholder); // set placeholder as initial text
        field.setFont(new Font("Segoe UI", Font.PLAIN, 13)); // font
        field.setForeground(new Color(148, 158, 186)); // placeholder color
        field.setBorder(BorderFactory.createEmptyBorder(6, 6, 6, 6)); // padding
        field.setOpaque(false); // transparent
        installPlaceholderBehavior(field, placeholder); // install placeholder focus behavior
        return field; // return field
    }

    private JPanel createInputRow(String icon, JTextField field) { // helper to create row with icon + field
        JPanel row = new JPanel(new BorderLayout(6, 0)); // border layout with gap
        row.setBackground(new Color(246, 249, 255)); // row bg
        row.setMaximumSize(new Dimension(450, 42)); // size cap
        row.setPreferredSize(new Dimension(450, 42)); // preferred
        row.setAlignmentX(Component.CENTER_ALIGNMENT); // center
        row.setBorder(BorderFactory.createCompoundBorder( // border + padding
            BorderFactory.createLineBorder(new Color(225, 232, 248), 1, true),
            BorderFactory.createEmptyBorder(2, 10, 2, 10)
        ));

        JLabel iconLabel = new JLabel(icon); // icon label
        iconLabel.setForeground(new Color(148, 158, 186)); // icon color
        iconLabel.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 16)); // emoji font
        row.add(iconLabel, BorderLayout.WEST); // add icon left
        row.add(field, BorderLayout.CENTER); // add input center
        return row; // return row
    }

    private JPanel createPasswordRow(String icon, JPasswordField field) {
        JPanel row = new JPanel(new BorderLayout(6, 0));
        row.setBackground(new Color(246, 249, 255));
        row.setMaximumSize(new Dimension(450, 42));
        row.setPreferredSize(new Dimension(450, 42));
        row.setAlignmentX(Component.CENTER_ALIGNMENT);
        row.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(225, 232, 248), 1, true),
            BorderFactory.createEmptyBorder(2, 10, 2, 6)
        ));

        JLabel iconLabel = new JLabel(icon);
        iconLabel.setForeground(new Color(148, 158, 186));
        iconLabel.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 16));
        row.add(iconLabel, BorderLayout.WEST);
        row.add(field, BorderLayout.CENTER);

        char defaultEcho = field.getEchoChar();
        JToggleButton eyeToggle = new JToggleButton();
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
                field.setEchoChar((char) 0);
            } else {
                field.setEchoChar(defaultEcho);
            }
        });
        row.add(eyeToggle, BorderLayout.EAST);
        return row;
    }

    private void installPlaceholderBehavior(JTextField field, String placeholder) { // focus listener for placeholder behavior
        field.addFocusListener(new FocusAdapter() { // add listener
            @Override
            public void focusGained(FocusEvent e) { // on focus in
                if (placeholder.equals(field.getText())) { // if showing placeholder
                    field.setText(""); // clear
                    field.setForeground(new Color(56, 65, 93)); // set input color
                }
            }

            @Override
            public void focusLost(FocusEvent e) { // on focus out
                if (field.getText().trim().isEmpty()) { // if empty
                    field.setText(placeholder); // restore placeholder
                    field.setForeground(new Color(148, 158, 186)); // placeholder color
                }
            }
        });
    }

    private void handleSignup() { // called when Sign Up button clicked
        String name = nameField.getText().trim(); // get name
        String phone = phoneField.getText().trim(); // get phone
        String email = emailField.getText().trim(); // get email
        String password = new String(passwordField.getPassword()).trim(); // password
        String confirmPassword = new String(confirmPasswordField.getPassword()).trim(); // confirm password
        if (NAME_PLACEHOLDER.equals(name)) { // if still placeholder
            name = ""; // treat as empty
        }
        if (PHONE_PLACEHOLDER.equals(phone)) { // if still placeholder
            phone = ""; // empty
        }

        if (name.isEmpty() || phone.isEmpty()) { // require both
            JOptionPane.showMessageDialog(this, "Please fill all fields."); // message
            return; // stop
        }

        if (EMAIL_PLACEHOLDER.equals(email) || email.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter your email address.");
            return;
        }

        // basic email check
        Pattern emailPattern = Pattern.compile("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$");
        if (!emailPattern.matcher(email).matches()) {
            JOptionPane.showMessageDialog(this, "Please enter a valid email address.");
            return;
        }

        if (!ValidationUtils.isValidPhone(phone)) { // validate phone digits
            JOptionPane.showMessageDialog(this, "Enter a valid phone number (7-15 digits)."); // message
            return; // stop
        }

        if (password.isEmpty() || confirmPassword.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter and confirm your password.");
            return;
        }

        if (!password.equals(confirmPassword)) {
            JOptionPane.showMessageDialog(this, "Passwords do not match.");
            return;
        }

        if (password.length() < 6) {
            JOptionPane.showMessageDialog(this, "Password must be at least 6 characters long.");
            return;
        }

        try {
            if (DBHelper.userExists(phone)) { // if phone already registered
                JOptionPane.showMessageDialog(this, "Phone number already exists."); // show message
                return; // stop
            }

            // Generate 6-digit OTP
            final String otp = String.format("%06d", new Random().nextInt(900000) + 100000);
            final String nameFinal = name;
            final String phoneFinal = phone;
            final String emailFinal = email;
            final String passwordFinal = password;

            // Show sending progress dialog while sending email in background
            final JDialog progress = new JDialog(this, "Sending OTP", true);
            JPanel p = new JPanel(new BorderLayout(8, 8));
            p.add(new JLabel("Sending verification code to " + email), BorderLayout.NORTH);
            JProgressBar bar = new JProgressBar();
            bar.setIndeterminate(true);
            p.add(bar, BorderLayout.CENTER);
            progress.setContentPane(p);
            progress.pack();
            progress.setLocationRelativeTo(this);

            SwingWorker<Void, Void> worker = new SwingWorker<>() {
                private Exception error;

                @Override
                protected Void doInBackground() {
                    try {
                        String subject = "Your Expense Tracker verification code";
                        String body = "Your verification code is: " + otp + "\n\nIf you didn't request this, ignore the email.";
                        EmailUtil.sendEmail(emailFinal, subject, body);
                    } catch (Exception ex) {
                        this.error = ex;
                    }
                    return null;
                }

                @Override
                protected void done() {
                    progress.dispose();
                    if (error != null) {
                        String msg = error instanceof MessagingException ? error.getMessage() : error.toString();
                        JOptionPane.showMessageDialog(SignupFrame.this, "Failed to send verification email: " + msg);
                        return;
                    }

                    // Ask user to enter OTP
                    String entered = JOptionPane.showInputDialog(SignupFrame.this, "Enter the 6-digit verification code sent to " + emailFinal + ":");
                    if (entered == null) return; // user cancelled
                    if (!entered.trim().equals(otp)) {
                        JOptionPane.showMessageDialog(SignupFrame.this, "Invalid verification code.");
                        return;
                    }

                    try {
                        DBHelper.addUser(nameFinal, phoneFinal, emailFinal, passwordFinal); // store user with email and password
                        JOptionPane.showMessageDialog(SignupFrame.this, "Account created successfully.");
                        dispose(); // close signup
                        new LoginFrame().setVisible(true); // open login
                    } catch (Exception ex) {
                        JOptionPane.showMessageDialog(SignupFrame.this, "Error saving user: " + ex.getMessage());
                    }
                }
            };

            worker.execute();
            progress.setVisible(true);
        } catch (Exception ex) { // DB or other errors
            JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage()); // show error
        }
    }

    private JPasswordField createPasswordField() {
        JPasswordField field = new JPasswordField();
        field.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        field.setForeground(new Color(56, 65, 93));
        field.setBorder(BorderFactory.createEmptyBorder(6, 6, 6, 6));
        field.setOpaque(false);
        return field;
    }
}

/*
 * SignupFrame.java
 *
 * End description: Simple sign up UI that saves new user to the database.
 * Very easy: type name + phone, we validate and save it.
 */