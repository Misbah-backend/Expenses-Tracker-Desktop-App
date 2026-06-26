/*
 * LoginFrame.java
 *
 * Description (start):
 * Easy: This file shows the Login window where user types phone and name.
 * - Purpose: let existing users log in and open the Dashboard.
 * - Why: we need a simple entry screen before showing expenses.
 * I made it simple so new users can sign up or login quickly.
 *
 * End of start description.
 */
package com.expensetracker.ui; // package: UI screens for the app

import com.expensetracker.DBHelper; // DB helper to check users in database

import javax.swing.*; // Swing UI toolkit
import java.awt.*; // AWT classes like Color, Dimension
import java.awt.event.FocusAdapter; // focus listener helper
import java.awt.event.FocusEvent; // focus  
import java.awt.event.MouseAdapter; // mouse listener helper
import java.awt.event.MouseEvent; // mouse events

public class LoginFrame extends JFrame { // Login window class

    private final JTextField nameField; // input for user name
    private final JTextField phoneField; // input for phone number
    private final JPasswordField passwordField; // input for password
    private final Timer titlePulseTimer; // timer for title pulsing animation // to stop when window closes
    private final JPanel inlineMessageHolder; // container for inline transient messages
    private static final String PHONE_PLACEHOLDER = "Enter your phone number"; // placeholder text
    private static final String NAME_PLACEHOLDER = "Enter your name"; // placeholder text

    public LoginFrame() { // constructor builds the UI
        setTitle("Expense Tracker - Login"); // window title shown on top
        setPreferredSize(new Dimension(1200, 700)); // preferred size for modern displays
        setExtendedState(JFrame.MAXIMIZED_BOTH); // start maximized so users don't need to resize
        setLocationRelativeTo(null); // center on screen
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); // exit app when window closed
        setContentPane(UiKit.gradientBackground(new Color(60, 104, 224), new Color(96, 84, 228))); // set gradient background

        JPanel root = new JPanel(new GridBagLayout()); // root panel uses GridBagLayout to center card
        root.setOpaque(false); // transparent so gradient shows
        root.setBorder(BorderFactory.createEmptyBorder(16, 20, 16, 20)); // padding around edges
        add(root); // add root to frame

        JPanel card = new JPanel(); // card panel (visual card in center)
        card.setOpaque(false); // allow background to show through
        card.setBackground(new Color(255, 255, 255, 28)); // semi-transparent white overlay
        card.setBorder(BorderFactory.createCompoundBorder( // rounded border + padding
            BorderFactory.createLineBorder(new Color(255, 255, 255, 55), 1, true),
            BorderFactory.createEmptyBorder(22, 30, 18, 30)
        ));
        card.setPreferredSize(new Dimension(500, 520)); // slightly taller card so logo and form breathe
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS)); // vertical stacking inside
        card.setAlignmentX(Component.CENTER_ALIGNMENT); // center alignment

        JPanel iconWrap = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0)); // wrapper for brand logo
        iconWrap.setOpaque(false); // transparent
        iconWrap.add(createBrandLogo()); // add larger logo for the first screen
        iconWrap.setAlignmentX(Component.CENTER_ALIGNMENT); // center
        card.add(iconWrap); // add to card

        card.add(Box.createVerticalStrut(14)); // vertical spacing below logo
        JLabel title = UiKit.titleLabel("Expense Tracker", Color.WHITE); // big title label
        title.setFont(new Font("Segoe UI", Font.BOLD, 34)); // app title size 34, bold
        title.setAlignmentX(Component.CENTER_ALIGNMENT); // center label
        card.add(title); // add to card
        card.add(Box.createVerticalStrut(8)); // title -> subtitle gap
        JLabel subtitle = UiKit.subtitleLabel("Login to your account", new Color(230, 235, 255)); // subtitle
        subtitle.setFont(new Font("Segoe UI", Font.PLAIN, 15)); // subtitle size 15 for readability
        subtitle.setAlignmentX(Component.CENTER_ALIGNMENT);
        card.add(subtitle); // add subtitle
        // inline message holder: used for auto-dismissing success messages (e.g. Welcome)
        inlineMessageHolder = new JPanel(new BorderLayout());
        inlineMessageHolder.setOpaque(false);
        inlineMessageHolder.setMaximumSize(new Dimension(360, 40));
        inlineMessageHolder.setPreferredSize(new Dimension(360, 40));
        card.add(Box.createVerticalStrut(20)); // subtitle -> first field gap
        card.add(inlineMessageHolder);
        titlePulseTimer = UiKit.startPulse(40, Color.WHITE, new Color(226, 231, 255), title, subtitle); // start label pulse animation
        addWindowListener(new java.awt.event.WindowAdapter() { // on window events
            @Override
            public void windowClosed(java.awt.event.WindowEvent e) { // when window closed
                titlePulseTimer.stop(); // stop animation timer to free resources
            }
        });
        card.add(Box.createVerticalStrut(20)); // gap before form

        JPanel form = new JPanel(); // form container
        form.setOpaque(false); // transparent
        form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS)); // vertical layout
        form.setAlignmentX(Component.CENTER_ALIGNMENT); // center
        form.setMaximumSize(new Dimension(360, Integer.MAX_VALUE)); // width limit, height flexible

        form.add(createFieldLabelPanel("Phone Number")); // label row for phone
        phoneField = createInputField(PHONE_PLACEHOLDER); // create phone input
        form.add(createInputRow("📞", phoneField)); // row with icon and field
        form.add(Box.createVerticalStrut(15)); // spacing between inputs

        form.add(createFieldLabelPanel("Name")); // label for name
        nameField = createInputField(NAME_PLACEHOLDER); // create name input
        form.add(createInputRow("👤", nameField)); // row with icon and field
        form.add(Box.createVerticalStrut(15)); // spacing between fields

        form.add(createFieldLabelPanel("Password")); // password label
        passwordField = createPasswordField(); // masked password input
        form.add(createPasswordRow("🔒", passwordField)); // row with icon and visibility toggle
        form.add(Box.createVerticalStrut(20)); // gap before login button

        JButton loginButton = UiKit.primaryButton("Login", new Color(18, 122, 246)); // primary login button
        loginButton.setPreferredSize(new Dimension(360, 46)); // size matching card inner width
        loginButton.setMaximumSize(new Dimension(360, 46)); // max size to keep shape
        loginButton.setAlignmentX(Component.CENTER_ALIGNMENT); // center
        loginButton.addActionListener(e -> handleLogin()); // click handler calls handleLogin
        form.add(loginButton); // add button
        form.add(Box.createVerticalStrut(12)); // spacing

        JPanel footer = new JPanel(new FlowLayout(FlowLayout.CENTER, 8, 0)); // footer with signup link
        footer.setOpaque(false); // transparent
        footer.setAlignmentX(Component.CENTER_ALIGNMENT); // center
        JLabel msg = new JLabel("Don't have an account?"); // helper message
        msg.setForeground(new Color(233, 237, 255)); // color
        msg.setFont(new Font("Segoe UI", Font.PLAIN, 14)); // font
        JLabel signUpLink = new JLabel("Sign Up"); // sign up clickable label
        signUpLink.setForeground(new Color(70, 226, 255)); // link color
        signUpLink.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)); // hand cursor to show clickable
        signUpLink.setFont(new Font("Segoe UI", Font.BOLD, 14)); // bold link font
        signUpLink.addMouseListener(new MouseAdapter() { // when clicked open Signup frame
            @Override
            public void mouseClicked(MouseEvent e) { // mouse clicked
                dispose(); // close login window
                new SignupFrame().setVisible(true); // open signup
            }
        });
        footer.add(msg); // add message
        footer.add(signUpLink); // add link
        form.add(footer); // add footer to form

        JLabel forgotPasswordLink = new JLabel("Forgot Password?");
        forgotPasswordLink.setForeground(new Color(70, 226, 255));
        forgotPasswordLink.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        forgotPasswordLink.setFont(new Font("Segoe UI", Font.BOLD, 14));
        forgotPasswordLink.setAlignmentX(Component.CENTER_ALIGNMENT);
        forgotPasswordLink.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                new ResetPasswordFrame().setVisible(true);
            }
        });
        form.add(Box.createVerticalStrut(10));
        form.add(forgotPasswordLink);

        card.add(form); // add form to card

        GridBagConstraints gbc = new GridBagConstraints(); // constraints for root layout
        gbc.gridx = 0; // column 0
        gbc.gridy = 0; // row 0
        gbc.weightx = 1.0; // expand horizontally
        gbc.weighty = 1.0; // expand vertically
        gbc.anchor = GridBagConstraints.CENTER; // center anchor
        root.add(card, gbc); // add card to root with constraints
    }

    private JComponent createBrandLogo() { // helper: choose a clear logo for first screen
        // Prefer packaged image logo if present; otherwise use drawn wallet icon from UiKit.
        JLabel logo = UiKit.loadIcon("/icons/login.png", 96);
        if (logo.getIcon() == null) {
            logo = UiKit.loadIcon("/icons/logo.png", 96);
        }
        if (logo.getIcon() != null) {
            logo.setAlignmentX(Component.CENTER_ALIGNMENT);
            return logo;
        }

        JPanel fallback = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
        fallback.setOpaque(false);
        fallback.add(UiKit.walletIcon(96));
        return fallback;
    }

    private void showInlineSuccess(String message) {
        try {
            inlineMessageHolder.removeAll();
            JPanel panel = new JPanel(new BorderLayout()) {
                @Override
                protected void paintComponent(Graphics g) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    g2.setColor(new Color(72, 187, 120)); // green background
                    g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
                    g2.dispose();
                }
            };
            panel.setOpaque(false);
            panel.setBorder(BorderFactory.createEmptyBorder(6, 12, 6, 12));
            JLabel lbl = new JLabel(message, SwingConstants.CENTER);
            lbl.setForeground(Color.WHITE);
            lbl.setFont(new Font("Segoe UI", Font.BOLD, 14));
            panel.add(lbl, BorderLayout.CENTER);
            inlineMessageHolder.add(panel, BorderLayout.CENTER);
            inlineMessageHolder.revalidate();
            inlineMessageHolder.repaint();
            Timer t = new Timer(2000, ev -> {
                inlineMessageHolder.removeAll();
                inlineMessageHolder.revalidate();
                inlineMessageHolder.repaint();
                ((Timer) ev.getSource()).stop();
            });
            t.setRepeats(false);
            t.start();
        } catch (Exception ex) {
            // fallback: silent
        }
    }

    private JPanel createFieldLabelPanel(String text) { // helper: creates a small label panel used above fields
        JLabel label = new JLabel(text); // label with provided text
        label.setForeground(new Color(236, 240, 255)); // light color
        label.setFont(new Font("Segoe UI", Font.BOLD, 14)); // bold font
        label.setBorder(BorderFactory.createEmptyBorder(0, 2, 5, 0)); // spacing under label
        JPanel panel = new JPanel(new BorderLayout()); // panel to hold label aligned center
        panel.setOpaque(false); // transparent
        panel.setMaximumSize(new Dimension(360, 22)); // size limit
        panel.setPreferredSize(new Dimension(360, 22)); // preferred size
        panel.add(label, BorderLayout.CENTER); // center label
        panel.setAlignmentX(Component.CENTER_ALIGNMENT); // center panel
        return panel; // return panel
    }

    private JTextField createInputField(String placeholder) { // helper: builds a text field with placeholder behavior
        JTextField field = new JTextField(placeholder); // set placeholder as initial text
        field.setFont(new Font("Segoe UI", Font.PLAIN, 13)); // font
        field.setForeground(new Color(148, 158, 186)); // placeholder color
        field.setBorder(BorderFactory.createEmptyBorder(6, 6, 6, 6)); // padding inside field
        field.setOpaque(false); // transparent background
        installPlaceholderBehavior(field, placeholder); // add focus behavior to show/hide placeholder
        return field; // return field
    }

    private JPasswordField createPasswordField() {
        JPasswordField field = new JPasswordField();
        field.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        field.setForeground(new Color(56, 65, 93));
        field.setBorder(BorderFactory.createEmptyBorder(6, 6, 6, 6));
        field.setOpaque(false);
        return field;
    }

    private JPanel createInputRow(String icon, JTextField field) { // helper: row with small icon and input field
        JPanel row = new JPanel(new BorderLayout(6, 0)); // border layout with gap
        row.setBackground(new Color(246, 249, 255)); // row background (subtle)
        row.setMaximumSize(new Dimension(360, 42)); // fixed size
        row.setPreferredSize(new Dimension(360, 42)); // preferred
        row.setAlignmentX(Component.CENTER_ALIGNMENT); // center
        row.setBorder(BorderFactory.createCompoundBorder( // border + padding
            BorderFactory.createLineBorder(new Color(225, 232, 248), 1, true),
            BorderFactory.createEmptyBorder(2, 10, 2, 10)
        ));

        JLabel iconLabel = new JLabel(icon); // small emoji icon label
        iconLabel.setForeground(new Color(148, 158, 186)); // icon color
        iconLabel.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 16)); // emoji font
        row.add(iconLabel, BorderLayout.WEST); // add icon left
        row.add(field, BorderLayout.CENTER); // add text field center
        return row; // return constructed row
    }

    private JPanel createPasswordRow(String icon, JPasswordField field) {
        JPanel row = new JPanel(new BorderLayout(6, 0));
        row.setBackground(new Color(246, 249, 255));
        row.setMaximumSize(new Dimension(360, 42));
        row.setPreferredSize(new Dimension(360, 42));
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

    private void installPlaceholderBehavior(JTextField field, String placeholder) { // helper: focus listener for placeholder text
        field.addFocusListener(new FocusAdapter() { // add listener
            @Override
            public void focusGained(FocusEvent e) { // when field gains focus
                if (placeholder.equals(field.getText())) { // if showing placeholder
                    field.setText(""); // clear text
                    field.setForeground(new Color(56, 65, 93)); // darker color for real input
                }
            }

            @Override
            public void focusLost(FocusEvent e) { // when focus lost
                if (field.getText().trim().isEmpty()) { // if nothing typed
                    field.setText(placeholder); // restore placeholder
                    field.setForeground(new Color(148, 158, 186)); // placeholder color
                }
            }
        });
    }

    private void handleLogin() { // called when user clicks Login
        String enteredName = nameField.getText().trim(); // read name typed
        String phone = phoneField.getText().trim(); // read phone typed
        String password = new String(passwordField.getPassword()).trim(); // read password typed

        if (PHONE_PLACEHOLDER.equals(phone)) { // if phone still shows placeholder
            phone = ""; // treat as empty
        }
        if (NAME_PLACEHOLDER.equals(enteredName)) { // if name still placeholder
            enteredName = ""; // empty
        }

        if (phone.isEmpty() || enteredName.isEmpty() || password.isEmpty()) { // validation: all required
            JOptionPane.showMessageDialog(this, "Please fill all fields."); // show message
            return; // stop processing
        }

        try {
            if (DBHelper.authenticateUser(phone, enteredName, password)) { // verify user and password
                String userName = DBHelper.getUserName(phone); // get stored name
                // show inline success message (green) and auto-dismiss after 2s
                showInlineSuccess("Welcome, " + userName + "!");
                // open dashboard shortly after showing message so user sees confirmation
                final String userNameFinal = userName;
                final String phoneFinal = phone;
                new Timer(900, ev -> {
                    ((Timer) ev.getSource()).stop();
                    dispose(); // close login window
                    new DashboardFrame(userNameFinal, phoneFinal).setVisible(true); // open dashboard
                }).start();
            } else {
                JOptionPane.showMessageDialog(this, "Invalid phone or password."); // prompt to fix credentials
            }
        } catch (Exception ex) { // catch DB errors
            JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage()); // show error
        }
    }
}

/*
 * LoginFrame.java
 *
 * End description: This file builds the login screen UI and handles simple login flow.
 * Very easy: User types name and phone, we check DB and open Dashboard if found.
 */