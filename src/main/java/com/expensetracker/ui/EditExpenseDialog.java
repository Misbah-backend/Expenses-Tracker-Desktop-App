/*
 * EditExpenseDialog.java
 *
 * Description (start):
 * Very easy: Modal dialog to edit an existing expense row.
 * - Receives raw row fields including `id` so it can call `DBHelper.updateExpense`.
 * - Validates inputs (amount, time, date) and updates DB on save.
 * - Caller checks `isSaved()` after dialog closes to know if reload is needed.
 *
 * End of start description.
 */
package com.expensetracker.ui; // ui package

import com.expensetracker.DBHelper; // DB operations
import com.expensetracker.ValidationUtils; // validation helpers

import javax.swing.*; // Swing widgets
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.text.AbstractDocument; // document filter for numeric input
import java.awt.*; // AWT types
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.Properties;

import org.jdatepicker.impl.JDatePanelImpl;
import org.jdatepicker.impl.JDatePickerImpl;
import org.jdatepicker.impl.UtilDateModel;

public class EditExpenseDialog extends JDialog { // modal dialog for editing

    private final int id; // DB id of expense being edited
    private boolean saved = false; // flag set true when update succeeds
    private final Timer titlePulseTimer; // optional animation timer for header

    private final JComboBox<String> categoryBox; // category selector
    private final JTextField titleField; // title input
    private final JTextField amountField; // amount input
    private final JTextField timeField; // time input
    private final JDatePickerImpl datePicker; // date selector
    private final Border defaultFieldBorder; // cached normal border for reset
    private static final String TITLE_PLACEHOLDER = "e.g. Movie ticket, Dinner, Groceries..."; // helper title hint

    public EditExpenseDialog(Frame owner, int id, String category, String title, String amount, String time, String day, String month, String year) {
        super(owner, "Edit Expense", true); // modal dialog with title
        this.id = id; // store id

        setSize(600, 460); // dialog size
        setLocationRelativeTo(owner); // center relative to owner
        setLayout(new BorderLayout(0, 0)); // border layout

        JPanel header = new JPanel(new BorderLayout()); // header area
        header.setBackground(new Color(255, 132, 25)); // orange
        header.setBorder(BorderFactory.createEmptyBorder(14, 18, 14, 18)); // padding
        JLabel headerTitle = new JLabel("Edit Expense"); // header text
        headerTitle.setForeground(Color.WHITE); // white text
        headerTitle.setFont(new Font("Segoe UI", Font.BOLD, 22)); // bold font
        header.add(headerTitle, BorderLayout.WEST); // add to header
        titlePulseTimer = UiKit.startPulse(45, Color.WHITE, new Color(255, 236, 216), headerTitle); // pulse
        addWindowListener(new java.awt.event.WindowAdapter() { // stop timer on close
            @Override
            public void windowClosed(java.awt.event.WindowEvent e) {
                titlePulseTimer.stop(); // stop animation
            }
        });
        add(header, BorderLayout.NORTH); // attach header to north

        JPanel body = new JPanel(new GridBagLayout()); // body holds card
        body.setBackground(new Color(248, 250, 255)); // background color
        body.setBorder(BorderFactory.createEmptyBorder(18, 18, 18, 18)); // padding
        add(body, BorderLayout.CENTER); // add body to center

        JPanel card = UiKit.cardPanel(); // card panel for form
        card.setPreferredSize(new Dimension(540, 350)); // size
        card.setLayout(new BorderLayout(14, 14)); // internal layout
        card.setBorder(BorderFactory.createEmptyBorder(22, 22, 22, 22)); // padding

        JPanel form = new JPanel(new GridBagLayout()); // actual form grid
        form.setOpaque(false); // transparent
        GridBagConstraints gbc = new GridBagConstraints(); // reuse constraints
        gbc.insets = new Insets(8, 8, 8, 8); // spacing
        gbc.fill = GridBagConstraints.HORIZONTAL; // fields expand horizontally

        categoryBox = UiKit.comboBox(new String[] { "Entertainment", "Shopping", "Food", "Grocery", "Education", "Health", "Travel", "Bills", "Other" }); // categories
        categoryBox.setSelectedItem(category); // select current category passed in

        titleField = new JTextField(title == null || title.trim().isEmpty() ? TITLE_PLACEHOLDER : title); // title prefilled or hint
        titleField.setBorder(UiKit.createFieldBorder()); // style
        titleField.setFont(UiKit.FIELD_FONT); // font from UiKit
        titleField.setForeground(title == null || title.trim().isEmpty() ? new Color(148, 158, 186) : UiKit.DARK_TEXT); // hint color when empty
        installPlaceholderBehavior(titleField, TITLE_PLACEHOLDER); // keep hint text readable

        amountField = new JTextField(amount); // amount prefilled
        amountField.setBorder(UiKit.createFieldBorder()); // styled border
        amountField.setFont(UiKit.FIELD_FONT); // font
        ((AbstractDocument) amountField.getDocument()).setDocumentFilter(new ValidationUtils.NumericDocumentFilter()); // numeric only

        timeField = new JTextField(time); // time prefilled
        timeField.setBorder(UiKit.createFieldBorder()); // style
        timeField.setFont(UiKit.FIELD_FONT); // font

        datePicker = createDatePicker(parseDate(day, month, year)); // proper date picker for editing
        defaultFieldBorder = amountField.getBorder(); // remember normal border

        addRow(form, gbc, 0, "Category:", categoryBox); // add category row
        addRow(form, gbc, 1, "Title:", titleField); // title row
        addRow(form, gbc, 2, "Amount:", amountField); // amount row
        addRow(form, gbc, 3, "Time:", timeField); // time row
        addRow(form, gbc, 4, "Date:", datePicker); // date row

        JPanel buttons = new JPanel(new GridLayout(1, 2, 14, 0)); // save / cancel buttons
        buttons.setOpaque(false); // transparent
        JButton save = UiKit.primaryButton("Save", new Color(33, 154, 89)); // primary style
        JButton cancel = UiKit.secondaryButton("Cancel", new Color(230, 238, 252), UiKit.DARK_TEXT); // secondary style
        buttons.add(save); // add save
        buttons.add(cancel); // add cancel

        card.add(form, BorderLayout.CENTER); // put form in card center
        card.add(buttons, BorderLayout.SOUTH); // put buttons at bottom
        body.add(card); // add card to body

        save.addActionListener(e -> onSave()); // save handler
        cancel.addActionListener(e -> dispose()); // cancel closes dialog
    }

    private void addRow(JPanel panel, GridBagConstraints gbc, int row, String labelText, JComponent field) {
        gbc.gridy = row; // which row
        gbc.gridx = 0; // label column
        gbc.weightx = 0.30; // label width fraction
        panel.add(UiKit.fieldLabel(labelText), gbc); // add styled label
        gbc.gridx = 1; // field column
        gbc.weightx = 0.70; // field width fraction
        if (field == datePicker) {
            field.setPreferredSize(new Dimension(320, 44)); // make the date field wider and taller so text fits cleanly
        } else {
            field.setPreferredSize(new Dimension(280, 36)); // slightly roomier default field size
        }
        panel.add(field, gbc); // add input field
    }

    private void onSave() { // validate inputs and call DB update
        clearValidationStyles(); // reset any previous error markers
        String category = (String) categoryBox.getSelectedItem(); // selected category
        String title = normalizePlaceholder(titleField.getText()); // title text
        String amountText = amountField.getText().trim(); // amount text
        String time = timeField.getText().trim(); // time text
        LocalDate selectedDate = getSelectedDate(); // selected date

        if (category == null || title.isEmpty() || amountText.isEmpty() || time.isEmpty() || selectedDate == null) {
            if (title.isEmpty()) applyErrorBorder(titleField); // highlight title
            if (amountText.isEmpty()) applyErrorBorder(amountField); // highlight amount
            if (time.isEmpty()) applyErrorBorder(timeField); // highlight time
            if (selectedDate == null) applyErrorBorder(datePicker.getJFormattedTextField()); // highlight date
            JOptionPane.showMessageDialog(this, "Please fill all fields."); // require all fields
            return; // abort
        }

        if (!ValidationUtils.isValidAmountString(amountText)) { // amount format check
            applyErrorBorder(amountField); // show error border
            JOptionPane.showMessageDialog(this, "Enter a valid amount greater than zero.");
            return; // abort
        }

        if (!ValidationUtils.isValidTime(time)) { // time format HH:mm or h:mm a
            applyErrorBorder(timeField); // show error border
            JOptionPane.showMessageDialog(this, "Enter time in HH:mm or h:mm AM/PM format.");
            return; // abort
        }

        double amount = Double.parseDouble(amountText); // parse to double
        String day = String.format("%02d", selectedDate.getDayOfMonth()); // normalized day text
        String month = String.format("%02d", selectedDate.getMonthValue()); // normalized month text
        String year = String.valueOf(selectedDate.getYear()); // year text
        try {
            boolean ok = DBHelper.updateExpense(id, category, title, amount, time, day, month, year); // update DB
            if (ok) {
                saved = true; // mark saved so caller knows
                DashboardFrame.refreshOpenDashboardBudget(); // update dashboard warning immediately
                JOptionPane.showMessageDialog(this, "Updated successfully."); // show success
                dispose(); // close dialog
            } else {
                JOptionPane.showMessageDialog(this, "Update failed."); // generic failure
            }
        } catch (Exception ex) { // on exception show message
            JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());
        }
    }

    public boolean isSaved() { return saved; } // caller checks whether save happened

    private JDatePickerImpl createDatePicker(LocalDate selectedDate) { // build date picker control
        UtilDateModel model = new UtilDateModel(); // date backing model
        model.setValue(Date.from(selectedDate.atStartOfDay(ZoneId.systemDefault()).toInstant())); // init value
        model.setSelected(true); // mark selected

        Properties props = new Properties(); // popup labels
        props.put("text.today", "Today");
        props.put("text.month", "Month");
        props.put("text.year", "Year");

        JDatePanelImpl datePanel = new JDatePanelImpl(model, props); // popup panel
        JDatePickerImpl picker = new JDatePickerImpl(datePanel, new org.jdatepicker.impl.DateComponentFormatter()); // picker widget
        picker.setPreferredSize(new Dimension(320, 44)); // match field size and avoid clipping
        picker.getJFormattedTextField().setBorder(UiKit.createFieldBorder()); // style
        picker.getJFormattedTextField().setFont(UiKit.FIELD_FONT); // font
        picker.getJFormattedTextField().setPreferredSize(new Dimension(290, 36)); // ensure the text area has enough vertical room
        return picker;
    }

    private LocalDate parseDate(String day, String month, String year) { // convert saved strings to LocalDate
        try {
            return LocalDate.of(Integer.parseInt(year), Integer.parseInt(month), Integer.parseInt(day));
        } catch (Exception ex) {
            return LocalDate.now();
        }
    }

    private LocalDate getSelectedDate() { // read selected date from picker
        try {
            Object value = datePicker.getModel().getValue();
            if (value instanceof Date date) {
                return Instant.ofEpochMilli(date.getTime()).atZone(ZoneId.systemDefault()).toLocalDate();
            }
        } catch (Exception ignored) {
        }
        return null;
    }

    private String normalizePlaceholder(String value) { // avoid saving the hint text
        String trimmed = value == null ? "" : value.trim();
        return TITLE_PLACEHOLDER.equals(trimmed) ? "" : trimmed;
    }

    private void installPlaceholderBehavior(JTextField field, String placeholder) { // keep hint text readable
        field.addFocusListener(new java.awt.event.FocusAdapter() {
            @Override
            public void focusGained(java.awt.event.FocusEvent e) {
                if (placeholder.equals(field.getText())) {
                    field.setText("");
                    field.setForeground(UiKit.DARK_TEXT);
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

    private void applyErrorBorder(JComponent component) { // red border feedback
        Border redLine = BorderFactory.createLineBorder(new Color(224, 68, 68), 2, true);
        component.setBorder(new CompoundBorder(redLine, BorderFactory.createEmptyBorder(3, 6, 3, 6)));
    }

    private void clearValidationStyles() { // restore normal borders
        titleField.setBorder(UiKit.createFieldBorder());
        amountField.setBorder(defaultFieldBorder);
        timeField.setBorder(UiKit.createFieldBorder());
        datePicker.getJFormattedTextField().setBorder(UiKit.createFieldBorder());
    }
}

/*
 * EditExpenseDialog.java
 *
 * End description: modal edit dialog for a single expense.
 */
