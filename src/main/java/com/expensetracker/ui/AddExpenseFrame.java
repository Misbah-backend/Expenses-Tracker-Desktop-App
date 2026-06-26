/*
 * AddExpenseFrame.java
 *
 * Description (start):
 * Very easy: This frame allows user to add a single expense record.
 * - Fields: Title, Amount, Time, Date (DD - MM - YYYY).
 * - Category: provided by caller or defaults to "Food".
 * - On save: validates inputs, writes to DB via DBHelper.addExpense.
 *
 * End of start description.
 */
package com.expensetracker.ui; // package for UI windows

import com.expensetracker.DBHelper; // DB helper for persistence
import com.expensetracker.ValidationUtils; // input validation helpers

import javax.swing.*; // Swing components
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.text.AbstractDocument; // document filter for numeric input
import java.awt.*; // AWT types: Color, Dimension, layouts
import java.time.Instant;
import java.time.LocalDate; // current date helpers
import java.time.LocalTime; // current time helpers
import java.time.ZoneId;
import java.time.format.DateTimeFormatter; // time formatting
import java.util.Date;
import java.util.Map;
import java.util.Properties;

import org.jdatepicker.impl.JDatePanelImpl;
import org.jdatepicker.impl.JDatePickerImpl;
import org.jdatepicker.impl.UtilDateModel;

public class AddExpenseFrame extends JFrame { // window to add an expense

    // --- fields: store values and UI components ---
    private final String userName; // user's name from previous screen
    private final String userPhone; // user's phone used as DB key
    private final Timer titlePulseTimer; // optional title animation timer
    private final JTextField titleField; // input: expense title
    private final JTextField amountField; // input: amount
    private final JTextField timeField; // input: time (12-hour friendly)
    private final JDatePickerImpl datePicker; // date picker for day-month-year selection
    private final JLabel totalLabel; // summary label for selected category total
    private final Border defaultFieldBorder; // cached normal border for restoring field style
    private static final String TITLE_PLACEHOLDER = "e.g. Movie ticket, Dinner, Groceries..."; // helper placeholder text for title
    private final String defaultCategory; // chosen category to attach to this expense

    // simple ctor uses default category "Food"
    public AddExpenseFrame(String userName, String userPhone) {
        this(userName, userPhone, "Food"); // delegate to full constructor
    }

    // main constructor with explicit default category
    public AddExpenseFrame(String userName, String userPhone, String defaultCategory) {
        this.userName = userName; // save username locally
        this.userPhone = userPhone; // save phone locally
        this.defaultCategory = defaultCategory; // save category

        setTitle("Expense Tracker - Add Expense"); // window title shown in OS
        setSize(1020, 890); // initial preferred size
        setExtendedState(JFrame.MAXIMIZED_BOTH); // start maximized so user sees full form
        setLocationRelativeTo(null); // center on screen if not maximized
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE); // close only this window, not app
        setContentPane(UiKit.plainBackground(new Color(248, 250, 255))); // light app background
        getContentPane().setLayout(new BorderLayout()); // main layout for frame

        JPanel root = new JPanel(new BorderLayout()); // root container for header/body
        root.setOpaque(false); // transparent so background shows
        getContentPane().add(root, BorderLayout.CENTER); // add root to main content

        JPanel header = new JPanel(new BorderLayout()); // top header bar
        header.setBackground(new Color(255, 132, 25)); // orange header color
        header.setBorder(BorderFactory.createEmptyBorder(18, 28, 18, 28)); // inner padding for header
        JLabel title = new JLabel(defaultCategory + " Expense"); // header title shows category
        title.setForeground(Color.WHITE); // white text on orange
        title.setFont(new Font("Segoe UI", Font.BOLD, 30)); // large bold font
        header.add(title, BorderLayout.WEST); // place title on left side
        // show signed-in user on the right side of the header (uses userName so field is referenced)
        JLabel userLabel = new JLabel("Signed in as " + (this.userName == null ? "" : this.userName));
        userLabel.setForeground(new Color(255, 255, 255, 200));
        userLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        header.add(userLabel, BorderLayout.EAST);
        titlePulseTimer = UiKit.startPulse(45, Color.WHITE, new Color(255, 236, 216), title); // subtle pulse on title
        addWindowListener(new java.awt.event.WindowAdapter() { // ensure timer stops when closed
            @Override
            public void windowClosed(java.awt.event.WindowEvent e) {
                titlePulseTimer.stop(); // stop title animation to avoid leaks
            }
        });
        root.add(header, BorderLayout.NORTH); // attach header to top of root

        JPanel body = new JPanel(new GridBagLayout()); // main content area uses GridBag for centering
        body.setOpaque(false); // transparent
        body.setBorder(BorderFactory.createEmptyBorder(32, 32, 32, 32)); // outer padding
        root.add(body, BorderLayout.CENTER); // add body to center

        JPanel card = UiKit.cardPanel(); // card-like panel from UiKit for form
        card.setPreferredSize(new Dimension(700, 500)); // card preferred size
        card.setLayout(new BorderLayout(28, 28)); // inside card: form center, actions bottom
        card.setBorder(BorderFactory.createEmptyBorder(32, 32, 32, 32)); // card padding

        JPanel form = new JPanel(new GridBagLayout()); // form grid for labels + fields
        form.setOpaque(false); // transparent
        GridBagConstraints gbc = new GridBagConstraints(); // constraints object to reuse
        gbc.insets = new Insets(12, 12, 12, 12); // spacing around each cell
        gbc.fill = GridBagConstraints.HORIZONTAL; // make fields expand horizontally

        titleField = createField(); // title text field
        installPlaceholderBehavior(titleField, TITLE_PLACEHOLDER); // help users with example title text
        titleField.setText(TITLE_PLACEHOLDER); // show placeholder initially
        titleField.setForeground(new Color(148, 158, 186)); // light placeholder color
        amountField = createField(); // amount text field
        ((AbstractDocument) amountField.getDocument()).setDocumentFilter(new ValidationUtils.NumericDocumentFilter()); // only numbers allowed
        timeField = createField(); // time text field
        timeField.setText(LocalTime.now().format(DateTimeFormatter.ofPattern("h:mm a"))); // default to current time in 12-hour format
        datePicker = createDatePicker(LocalDate.now()); // proper date picker instead of split text boxes
        defaultFieldBorder = amountField.getBorder(); // remember normal field border for validation reset

        addRow(form, gbc, 0, "Title:", titleField); // add title row
        addRow(form, gbc, 1, "Amount (Rs.):", amountField); // add amount row
        addRow(form, gbc, 2, "Time:", timeField); // add time row
        addRow(form, gbc, 3, "Date:", datePicker); // add date picker row

        JPanel buttons = new JPanel(new GridLayout(1, 2, 14, 0)); // save/back buttons side-by-side
        buttons.setOpaque(false); // transparent
        JButton saveButton = UiKit.primaryButton("Save Expense", new Color(33, 154, 89)); // green primary
        JButton backButton = UiKit.secondaryButton("Back", new Color(230, 238, 252), UiKit.DARK_TEXT); // neutral secondary
        saveButton.addActionListener(e -> handleSave()); // on save click, validate and persist
        backButton.addActionListener(e -> dispose()); // back closes this window
        buttons.add(saveButton); // add both buttons to panel
        buttons.add(backButton);

        JPanel totalPanel = UiKit.softCard(new Color(255, 247, 228), new Color(243, 215, 156)); // small summary card
        totalPanel.setLayout(new BorderLayout()); // simple layout
        totalPanel.setBorder(BorderFactory.createEmptyBorder(16, 18, 16, 18)); // padding
        totalLabel = new JLabel("Your total will appear here", SwingConstants.CENTER); // avoid misleading zero for new users
        totalLabel.setForeground(new Color(179, 110, 41)); // amber text color
        totalLabel.setFont(new Font("Segoe UI", Font.BOLD, 17)); // bold small font
        totalPanel.add(totalLabel, BorderLayout.CENTER); // center the label
        refreshCategoryTotal(); // show real total only when category has data

        JPanel bottom = new JPanel(new BorderLayout(0, 12)); // bottom area contains buttons then total
        bottom.setOpaque(false); // transparent
        bottom.add(buttons, BorderLayout.NORTH); // buttons on top of bottom area
        bottom.add(totalPanel, BorderLayout.CENTER); // total below buttons

        card.add(form, BorderLayout.CENTER); // form fills card center
        card.add(bottom, BorderLayout.SOUTH); // bottom area anchored to card bottom
        body.add(card); // add card to body
        UiKit.fadeInWindow(this, 320); // fade-in effect for window
        UiKit.startPanelGlow(48, card, Color.WHITE, new Color(253, 254, 255), new Color(226, 231, 244), new Color(255, 186, 92)); // glow around card
        UiKit.startPanelGlow(48, totalPanel, new Color(255, 247, 228), new Color(255, 252, 240), new Color(243, 215, 156), new Color(255, 191, 92)); // glow on total panel
        UiKit.startPulse(52, new Color(179, 110, 41), new Color(255, 137, 34), totalLabel); // subtle pulse on total
        try {
            UiKit.fadeInWindow(this, 320); // extra safe fade-in call (harmless if animations disabled)
        } catch (Exception ex) {
            ex.printStackTrace(); // print stack trace if fade-in fails
        }
    }

    // helper: create standard text field for form
    private JTextField createField() {
        JTextField field = new JTextField(); // create text field
        field.setBorder(UiKit.createFieldBorder()); // apply UiKit border style
        field.setFont(new Font("Segoe UI", Font.PLAIN, 16)); // set font
        field.setPreferredSize(new Dimension(340, 40)); // preferred size for regular fields
        return field; // return created field
    }

    // (removed unused createDateField helper — the form uses a full JDatePicker)

    private JDatePickerImpl createDatePicker(LocalDate initialDate) { // creates JDatePicker with app styling
        UtilDateModel model = new UtilDateModel(); // date model backing the picker
        model.setValue(Date.from(initialDate.atStartOfDay(ZoneId.systemDefault()).toInstant())); // initialize with today's date
        model.setSelected(true); // mark selected date

        Properties props = new Properties(); // calendar labels and helper text
        props.put("text.today", "Today");
        props.put("text.month", "Month");
        props.put("text.year", "Year");

        JDatePanelImpl datePanel = new JDatePanelImpl(model, props); // popup calendar panel
        JDatePickerImpl picker = new JDatePickerImpl(datePanel, new org.jdatepicker.impl.DateComponentFormatter()); // text + popup picker
        picker.setPreferredSize(new Dimension(420, 40)); // slightly wider so full date text is visible
        picker.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40)); // allow horizontal expansion in layouts
        picker.getJFormattedTextField().setBorder(UiKit.createFieldBorder()); // apply app border style
        picker.getJFormattedTextField().setFont(new Font("Segoe UI", Font.PLAIN, 16)); // matching font
        picker.getJFormattedTextField().setPreferredSize(new Dimension(360, 40)); // ensure inner text field is wide enough
        return picker;
    }

    // helper: add a labelled row to the form
    private void addRow(JPanel panel, GridBagConstraints gbc, int row, String labelText, JComponent field) {
        gbc.gridy = row; // set row index for this entry

        gbc.gridx = 0; // label column
        gbc.weightx = 0.28; // label width fraction
        JLabel label = UiKit.fieldLabel(labelText); // create styled label
        label.setFont(new Font("Segoe UI", Font.BOLD, 15)); // bold label font
        panel.add(label, gbc); // place label

        gbc.gridx = 1; // field column
        gbc.weightx = 0.72; // field takes remaining space
        panel.add(field, gbc); // place input field
    }

    // action: validate inputs and save to database
    private void handleSave() {
        clearValidationStyles(); // reset red borders from previous submit attempt
        String category = defaultCategory; // category chosen earlier
        String title = normalizePlaceholder(titleField.getText()); // treat placeholder as empty title
        String amountText = amountField.getText().trim(); // read amount as string
        String time = timeField.getText().trim(); // read time
        LocalDate selectedDate = getSelectedDate(); // read selected date from picker

        if (title.isEmpty()) {
            applyErrorBorder(titleField); // highlight empty title field
            JOptionPane.showMessageDialog(this, "Title is required");
            return;
        }

        // Specific validation feedback requested: red border + explicit message for amount.
        if (amountText.isEmpty()) {
            applyErrorBorder(amountField); // highlight problematic field in red
            JOptionPane.showMessageDialog(this, "Amount is required");
            return; // abort save
        }

        if (time.isEmpty()) {
            applyErrorBorder(timeField); // highlight empty time field
            JOptionPane.showMessageDialog(this, "Time is required");
            return;
        }

        if (selectedDate == null) {
            applyErrorBorder(datePicker.getJFormattedTextField()); // highlight missing date selection
            JOptionPane.showMessageDialog(this, "Date is required");
            return;
        }

        // validate amount string using ValidationUtils helper
        if (!ValidationUtils.isValidAmountString(amountText)) {
            applyErrorBorder(amountField); // keep visible cue on invalid amount
            JOptionPane.showMessageDialog(this, "Enter a valid amount greater than zero."); // inform user
            return; // abort
        }

        // validate time format (24-hour or 12-hour)
        if (!ValidationUtils.isValidTime(time)) {
            applyErrorBorder(timeField); // highlight invalid time field
            JOptionPane.showMessageDialog(this, "Enter time in HH:mm or h:mm AM/PM format."); // guide user
            return; // abort
        }

        String day = String.format("%02d", selectedDate.getDayOfMonth()); // normalized day text
        String month = String.format("%02d", selectedDate.getMonthValue()); // always two digits, e.g. 05
        String year = String.valueOf(selectedDate.getYear()); // year as 4-digit text

        double amount = Double.parseDouble(amountText); // parse to double (we validated format earlier)
        
        // Check budget before saving
        try {
            double budget = DBHelper.getUserMonthlyBudget(userPhone);
            if (budget > 0) {
                double currentSpending = DBHelper.getCurrentMonthSpending(userPhone);
                double newTotal = currentSpending + amount;
                
                if (newTotal > budget) {
                    // Over budget - show warning
                    int result = JOptionPane.showConfirmDialog(this, 
                        String.format("⚠️ WARNING: This expense will put you OVER budget!\n\n" +
                                    "Current spending: Rs. %.0f\n" +
                                    "Monthly budget: Rs. %.0f\n" +
                                    "New total: Rs. %.0f (%.0f%% over)\n\n" +
                                    "Do you want to continue?", 
                                    currentSpending, budget, newTotal, ((newTotal - budget) / budget * 100)),
                        "Over Budget",
                        JOptionPane.YES_NO_OPTION,
                        JOptionPane.WARNING_MESSAGE);
                    if (result != JOptionPane.YES_OPTION) return;
                } else if (newTotal >= 0.8 * budget) {
                    // Warning: approaching budget limit
                    int result = JOptionPane.showConfirmDialog(this,
                        String.format("⚠️ You are approaching your budget limit!\n\n" +
                                    "Current spending: Rs. %.0f\n" +
                                    "Monthly budget: Rs. %.0f\n" +
                                    "New total: Rs. %.0f (%.0f%% used)\n\n" +
                                    "Continue?",
                                    currentSpending, budget, newTotal, (newTotal / budget * 100)),
                        "Approaching Budget",
                        JOptionPane.YES_NO_OPTION,
                        JOptionPane.WARNING_MESSAGE);
                    if (result != JOptionPane.YES_OPTION) return;
                }
            }
        } catch (Exception ex) {
            // If budget check fails, log but continue with save
            ex.printStackTrace();
        }
        
        // Use SwingWorker to save in background and avoid freezing the UI
        final JDialog progress = new JDialog(this, "Saving...", true);
        JPanel p = new JPanel(new BorderLayout(8, 8));
        p.add(new JLabel("Saving expense, please wait..."), BorderLayout.NORTH);
        JProgressBar bar = new JProgressBar();
        bar.setIndeterminate(true);
        p.add(bar, BorderLayout.CENTER);
        progress.setContentPane(p);
        progress.pack();
        progress.setLocationRelativeTo(this);

        SwingWorker<Boolean, Void> worker = new SwingWorker<>() {
            private Exception error;

            @Override
            protected Boolean doInBackground() {
                try {
                    return DBHelper.addExpense(userPhone, category, title, amount, time, day, month, year);
                } catch (Exception ex) {
                    this.error = ex;
                    return false;
                }
            }

            @Override
            protected void done() {
                progress.dispose();
                if (error != null) {
                    JOptionPane.showMessageDialog(AddExpenseFrame.this, "Error: " + error.getMessage());
                    return;
                }
                try {
                    boolean saved = get();
                    if (saved) {
                        UiKit.showToast(AddExpenseFrame.this, "Expense saved successfully.");
                        clearFields();
                        refreshCategoryTotal();
                        DashboardFrame.refreshOpenDashboardBudget();
                    } else {
                        JOptionPane.showMessageDialog(AddExpenseFrame.this, "Could not save expense.");
                    }
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(AddExpenseFrame.this, "Error: " + ex.getMessage());
                }
            }
        };
        worker.execute();
        progress.setVisible(true);
    }

    // reset form fields to defaults (current date/time)
    private void clearFields() {
        titleField.setText(TITLE_PLACEHOLDER); // restore placeholder hint
        titleField.setForeground(new Color(148, 158, 186)); // placeholder color
        amountField.setText(""); // clear amount
        timeField.setText(LocalTime.now().format(DateTimeFormatter.ofPattern("h:mm a"))); // reset time to now in 12-hour format
        ((UtilDateModel) datePicker.getModel()).setValue(Date.from(LocalDate.now().atStartOfDay(ZoneId.systemDefault()).toInstant())); // reset picker to today
        clearValidationStyles(); // remove error borders
    }

    private LocalDate getSelectedDate() { // read selected date from picker safely
        try {
            Object value = datePicker.getModel().getValue(); // picker returns java.util.Date
            if (value instanceof Date date) {
                return Instant.ofEpochMilli(date.getTime()).atZone(ZoneId.systemDefault()).toLocalDate();
            }
        } catch (Exception ignored) {
            // keep null and let caller show validation message
        }
        return null;
    }

    private void refreshCategoryTotal() { // update total label based on saved data
        try {
            Map<String, Double> totals = DBHelper.getTotalsByCategory(userPhone); // fetch all category totals
            Double categoryTotal = totals.get(defaultCategory); // total for selected category
            if (categoryTotal == null || categoryTotal <= 0.0) {
                totalLabel.setText("Your total will appear here"); // clearer empty state
                return;
            }
            totalLabel.setText(String.format("Total %s Expense: Rs. %.2f", defaultCategory, categoryTotal));
        } catch (Exception ex) {
            totalLabel.setText("Your total will appear here"); // fallback message on query error
        }
    }

    private void applyErrorBorder(JComponent field) { // mark field with red border for validation feedback
        Border redLine = BorderFactory.createLineBorder(new Color(224, 68, 68), 2, true);
        field.setBorder(new CompoundBorder(redLine, BorderFactory.createEmptyBorder(3, 6, 3, 6)));
    }

    private void clearValidationStyles() { // restore normal borders after validation pass
        titleField.setBorder(defaultFieldBorder);
        amountField.setBorder(defaultFieldBorder);
        timeField.setBorder(defaultFieldBorder);
        datePicker.getJFormattedTextField().setBorder(defaultFieldBorder);
    }

    private String normalizePlaceholder(String value) { // avoid saving placeholder text as title
        String trimmed = value == null ? "" : value.trim();
        return TITLE_PLACEHOLDER.equals(trimmed) ? "" : trimmed;
    }

    private void installPlaceholderBehavior(JTextField field, String placeholder) { // placeholder helper for title field
        field.addFocusListener(new java.awt.event.FocusAdapter() {
            @Override
            public void focusGained(java.awt.event.FocusEvent e) {
                if (placeholder.equals(field.getText())) {
                    field.setText("");
                    field.setForeground(new Color(56, 65, 93));
                }
            }

            @Override
            public void focusLost(java.awt.event.FocusEvent e) {
                if (field.getText().trim().isEmpty()) {
                    field.setText(placeholder);
                    field.setForeground(new Color(148, 158, 186));
                }
            }
        });
    }
}

/*
 * AddExpenseFrame.java
 *
 * End description: simple add-expense form with Title, Amount, Time, Date.
 */