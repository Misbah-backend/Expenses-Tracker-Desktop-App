/*
 * ViewExpensesFrame.java
 *
 * Description (start):
 * Easy: This frame lists all expenses for the logged-in user and shows summaries.
 * - Shows a table with columns: Category, Title, Amount, DayName, Date (DD-MM-YYYY), Time.
 * - Double-click row -> opens `EditExpenseDialog` for that expense (uses raw ID from DB row).
 * - Summary cards show top categories and a total expense card with animated counter.
 *
 * End of start description.
 */
package com.expensetracker.ui; // ui package

import com.expensetracker.DBHelper; // db operations

import javax.swing.*; // Swing widgets
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel; // table model
import javax.swing.table.TableRowSorter;
import java.awt.*; // AWT types
import java.awt.Desktop;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDate; // for day name calculation
import java.time.format.TextStyle; // for full weekday name
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List; // list of rows
import java.util.Locale; // locale for day names
import java.util.Map; // totals map

public class ViewExpensesFrame extends JFrame { // frame to view expenses

    private final String userName; // user's name for back navigation
    private final String userPhone; // user's phone used as DB key
    private final DefaultTableModel tableModel; // model for JTable rows
    private final JTable table; // table shown on screen
    private java.util.List<String[]> latestRows; // raw DB rows (includes id and fields)
    private final JPanel summaryCards; // panel holding up to 4 summary cards
    private final JLabel totalExpenseLabel; // label that displays total amount
    private final Timer titlePulseTimer; // optional animation timer for title/total

    public ViewExpensesFrame(String userName, String userPhone) {
        this.userName = userName; // keep name for dashboard return
        this.userPhone = userPhone; // store phone for queries

        setTitle("Expense Tracker - View Data"); // window title
        setSize(760, 560); // preferred window size
        setExtendedState(JFrame.MAXIMIZED_BOTH); // open maximized like main screens
        setLocationRelativeTo(null); // center on screen
        setResizable(true); // allow resize
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE); // only this window closes
        setContentPane(UiKit.plainBackground(new Color(248, 250, 255))); // app background
        getContentPane().setLayout(new BorderLayout()); // main layout

        // top blue strip removed per user request (no extra top bar)

        JPanel root = new JPanel(new BorderLayout()); // root container
        root.setOpaque(false); // let background show
        root.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12)); // outer padding
        getContentPane().add(root, BorderLayout.CENTER); // add root to frame

        JPanel container = UiKit.softCard(new Color(255, 255, 255), new Color(225, 230, 242)); // main white card
        container.setLayout(new BorderLayout(0, 12)); // spacing inside
        container.setBorder(BorderFactory.createEmptyBorder(12, 14, 12, 14)); // inner padding
        root.add(container, BorderLayout.CENTER); // add card to root

        JPanel header = new JPanel(new BorderLayout()); // top row with back button + centered title
        header.setOpaque(false); // transparent
        JPanel leftSpacer = new JPanel(); // spacer to keep the centered title visually centered
        leftSpacer.setOpaque(false);
        leftSpacer.setPreferredSize(new Dimension(110, 36));
        header.add(leftSpacer, BorderLayout.WEST);
        
        JButton backButton = UiKit.secondaryButton("Back", new Color(230, 238, 252), UiKit.DARK_TEXT); // back to dashboard
        backButton.setPreferredSize(new Dimension(110, 36));
        backButton.addActionListener(e -> navigateBackToDashboard());
        JButton exportButton = UiKit.primaryButton("Export CSV", new Color(30, 105, 223)); // export to CSV
        exportButton.setPreferredSize(new Dimension(130, 36));
        exportButton.addActionListener(e -> exportTableToCsv());
        JPanel headerActions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        headerActions.setOpaque(false);
        headerActions.add(exportButton);
        headerActions.add(backButton);
        header.add(headerActions, BorderLayout.EAST);
        JLabel title = new JLabel("All Expenses"); // header text
        title.setFont(new Font("Segoe UI", Font.BOLD, 30)); // large font
        title.setForeground(UiKit.DARK_TEXT); // dark color from UiKit
        JPanel titleWrap = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0)); // keep title centered regardless of button width
        titleWrap.setOpaque(false);
        titleWrap.add(title);
        header.add(titleWrap, BorderLayout.CENTER);
        container.add(header, BorderLayout.NORTH); // attach header to card top

        JPanel content = new JPanel(new BorderLayout(0, 10)); // main content area
        content.setOpaque(false); // transparent
        container.add(content, BorderLayout.CENTER); // add to card center

        summaryCards = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0)); // row that can grow to all categories
        summaryCards.setOpaque(false); // transparent
        JScrollPane summaryScroll = new JScrollPane(summaryCards, JScrollPane.VERTICAL_SCROLLBAR_NEVER, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED); // horizontal scrolling for many categories
        summaryScroll.setBorder(BorderFactory.createEmptyBorder());
        summaryScroll.getViewport().setOpaque(false);
        summaryScroll.setOpaque(false);
        summaryScroll.setPreferredSize(new Dimension(0, 86));
        content.add(summaryScroll, BorderLayout.NORTH); // put summaries at top of content

        JPanel tableCard = UiKit.softCard(new Color(255, 255, 255), new Color(228, 232, 243)); // wrapper for table
        tableCard.setLayout(new BorderLayout()); // table fills this card
        tableCard.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4)); // small padding

        String[] cols = {"Category", "Title", "Amount (Rs.)", "Day", "Date", "Time"}; // visible columns
        tableModel = new DefaultTableModel(cols, 0) { // non-editable table model
            public boolean isCellEditable(int row, int column) { return false; }
        };
        table = new JTable(tableModel); // create table with model
        TableRowSorter<DefaultTableModel> sorter = new TableRowSorter<>(tableModel); // enables search/filtering
        table.setRowSorter(sorter); // attach sorter to table
        UiKit.styleTable(table); // apply common table styles from UiKit
        table.getTableHeader().setBackground(new Color(247, 249, 252)); // header background
        table.getTableHeader().setForeground(new Color(58, 66, 86)); // header text color
        table.setRowHeight(30); // row height

        JPanel searchBar = new JPanel(new BorderLayout(10, 0)); // search row above table
        searchBar.setOpaque(false);
        searchBar.setBorder(BorderFactory.createEmptyBorder(0, 0, 8, 0));
        JLabel searchLabel = new JLabel("Search:");
        searchLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
        searchLabel.setForeground(UiKit.DARK_TEXT);
        JTextField searchField = new JTextField();
        searchField.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        searchField.setBorder(UiKit.createFieldBorder());
        searchField.setPreferredSize(new Dimension(320, 34));
        JButton clearSearch = UiKit.secondaryButton("Clear", new Color(230, 238, 252), UiKit.DARK_TEXT);
        clearSearch.setPreferredSize(new Dimension(90, 34));
        clearSearch.addActionListener(e -> searchField.setText(""));
        searchBar.add(searchLabel, BorderLayout.WEST);
        searchBar.add(searchField, BorderLayout.CENTER);
        searchBar.add(clearSearch, BorderLayout.EAST);

        JPopupMenu rowMenu = new JPopupMenu(); // right-click menu for row actions
        JMenuItem editItem = new JMenuItem("Edit");
        JMenuItem deleteItem = new JMenuItem("Delete");
        rowMenu.add(editItem);
        rowMenu.add(deleteItem);
        editItem.addActionListener(e -> openEditDialogForRow(table, table.getSelectedRow()));
        deleteItem.addActionListener(e -> deleteExpenseAtRow(table, table.getSelectedRow()));

        searchField.getDocument().addDocumentListener(new DocumentListener() { // live filter rows as user types
            private void update() {
                String query = searchField.getText().trim();
                if (query.isEmpty()) {
                    sorter.setRowFilter(null);
                    return;
                }
                sorter.setRowFilter(new RowFilter<DefaultTableModel, Integer>() {
                    @Override
                    public boolean include(Entry<? extends DefaultTableModel, ? extends Integer> entry) {
                        for (int i = 0; i < entry.getValueCount(); i++) {
                            Object value = entry.getValue(i);
                            if (value != null && value.toString().toLowerCase(Locale.ENGLISH).contains(query.toLowerCase(Locale.ENGLISH))) {
                                return true;
                            }
                        }
                        return false;
                    }
                });
            }

            @Override public void insertUpdate(DocumentEvent e) { update(); }
            @Override public void removeUpdate(DocumentEvent e) { update(); }
            @Override public void changedUpdate(DocumentEvent e) { update(); }
        });

        JPanel tableSection = new JPanel(new BorderLayout(0, 8));
        tableSection.setOpaque(false);
        tableSection.add(searchBar, BorderLayout.NORTH);

        JScrollPane tableScroll = new JScrollPane(table); // scroll pane for table
        tableScroll.setBorder(BorderFactory.createEmptyBorder()); // no extra border
        tableSection.add(tableScroll, BorderLayout.CENTER);
        tableCard.add(tableSection, BorderLayout.CENTER); // table in center
        content.add(tableCard, BorderLayout.CENTER); // table card in content center

        // Open edit dialog on double-click
        table.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                if (e.getClickCount() == 2 && table.getSelectedRow() >= 0) { // double-click and a row selected
                    openEditDialogForRow(table, table.getSelectedRow()); // open editor on double click
                }
            }

            @Override
            public void mousePressed(java.awt.event.MouseEvent e) {
                maybeShowMenu(e);
            }

            @Override
            public void mouseReleased(java.awt.event.MouseEvent e) {
                maybeShowMenu(e);
            }

            private void maybeShowMenu(java.awt.event.MouseEvent e) {
                if (!e.isPopupTrigger()) return; // only right-click trigger
                int row = table.rowAtPoint(e.getPoint());
                if (row >= 0) {
                    table.setRowSelectionInterval(row, row); // select row under cursor
                    rowMenu.show(e.getComponent(), e.getX(), e.getY()); // open context menu
                }
            }
        });

        JPanel totalPanel = UiKit.softCard(new Color(255, 247, 228), new Color(243, 215, 156)); // total summary card
        totalPanel.setLayout(new BorderLayout()); // simple layout for center label
        totalPanel.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12)); // padding
        totalExpenseLabel = new JLabel("Total Expense: Rs. 0.00", SwingConstants.CENTER); // initial text
        totalExpenseLabel.setForeground(new Color(214, 68, 83)); // red tint for totals
        totalExpenseLabel.setFont(new Font("Segoe UI", Font.BOLD, 24)); // bold large
        totalPanel.add(totalExpenseLabel, BorderLayout.CENTER); // add label
        content.add(totalPanel, BorderLayout.SOUTH); // total card at bottom of content

        titlePulseTimer = UiKit.startPulse(45, UiKit.DARK_TEXT, new Color(70, 104, 214), title, totalExpenseLabel); // pulse on title and total
        UiKit.startPanelGlow(50, tableCard, Color.WHITE, new Color(252, 253, 255), new Color(228, 232, 243), new Color(156, 184, 245)); // glow effect
        UiKit.startPanelGlow(50, totalPanel, new Color(255, 247, 228), new Color(255, 252, 240), new Color(243, 215, 156), new Color(255, 191, 92)); // glow on total
        addWindowListener(new java.awt.event.WindowAdapter() { // stop animations when closed
            @Override
            public void windowClosed(java.awt.event.WindowEvent e) {
                titlePulseTimer.stop(); // stop pulse timer
            }
        });

        loadData(); // initial load of data from DB
    }

    private void loadData() {
        try {
            List<String[]> rows = DBHelper.getExpensesForUser(userPhone); // fetch raw rows: id, category, title, amount, time, day, month, year
            // keep a copy of raw rows so edit dialog can access IDs and raw fields
            latestRows = rows; // store raw rows for later edit dialog mapping
            tableModel.setRowCount(0); // clear previous rows in visible table
            for (String[] r : rows) { // convert raw row to display row
                String day = r[5]; // day string
                String month = r[6]; // month string
                String year = r[7]; // year string
                String date = String.format("%02d-%02d-%04d", Integer.parseInt(day), Integer.parseInt(month), Integer.parseInt(year)); // format DD-MM-YYYY
                String title = (r[2] == null || r[2].trim().isEmpty()) ? "(No title)" : r[2]; // avoid blank title cells for old data
                tableModel.addRow(new String[] {
                    r[1], // category
                    title, // title
                    String.format("%.2f", Double.parseDouble(r[3])), // amount formatted 2 decimals
                    dayNameFromDate(day, month, year), // weekday name like Monday
                    date, // formatted date
                    r[4] // time string
                });
            }

            Map<String, Double> totals = DBHelper.getTotalsByCategory(userPhone); // totals per category
            updateSummaryCards(totals); // refresh summary cards
            updateTotalExpense(totals); // animate total expense label
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error loading data: " + ex.getMessage()); // show error
        }
    }

    private void updateSummaryCards(Map<String, Double> totals) {
        summaryCards.removeAll(); // clear existing cards
        if (totals.isEmpty()) { // no data case
            summaryCards.add(createSummaryCard("No Data", "Rs. 0.00", new Color(246, 247, 252), new Color(200, 204, 220), new Color(79, 86, 110)));
        } else {
            int shown = 0; // count cards added
            for (String category : orderedCategories(totals)) { // render all known + saved categories
                double value = totals.getOrDefault(category, 0.0); // show 0 when category has no data yet
                Color accent; // card fill color
                Color valueColor; // amount color
                switch (shown % 4) { // pick palette by index to vary colors
                    case 0 -> {
                        accent = new Color(229, 208, 247);
                        valueColor = new Color(125, 64, 172);
                    }
                    case 1 -> {
                        accent = new Color(213, 241, 221);
                        valueColor = new Color(41, 146, 86);
                    }
                    case 2 -> {
                        accent = new Color(252, 236, 208);
                        valueColor = new Color(212, 133, 37);
                    }
                    default -> {
                        accent = new Color(215, 233, 250);
                        valueColor = new Color(50, 117, 198);
                    }
                }
                summaryCards.add(createSummaryCard(category, String.format("Rs. %.2f", value), accent, new Color(227, 232, 244), valueColor)); // add card
                shown++;
            }
        }
        summaryCards.revalidate(); // refresh layout
        summaryCards.repaint(); // repaint visuals
    }

    private List<String> orderedCategories(Map<String, Double> totals) { // keep app categories visible in summary
        LinkedHashSet<String> ordered = new LinkedHashSet<>(Arrays.asList(
            "Entertainment", "Shopping", "Food", "Grocery"
        ));
        java.util.TreeSet<String> dynamic = new java.util.TreeSet<>(totals.keySet()); // include any extra custom categories in stable order
        ordered.addAll(dynamic);
        return new java.util.ArrayList<>(ordered);
    }

    private JPanel createSummaryCard(String label, String value, Color fill, Color border, Color valueColor) { // builds small card
        JPanel card = UiKit.softCard(fill, border); // soft card from UiKit
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS)); // vertical stacking
        card.setBorder(BorderFactory.createEmptyBorder(8, 10, 8, 10)); // padding

        JLabel title = new JLabel(label.isEmpty() ? " " : label, SwingConstants.CENTER); // title label or blank
        title.setAlignmentX(Component.CENTER_ALIGNMENT); // center horizontally
        title.setForeground(new Color(83, 88, 105)); // muted text color
        title.setFont(new Font("Segoe UI", Font.BOLD, 11)); // small bold

        JLabel amount = new JLabel(value.isEmpty() ? " " : value, SwingConstants.CENTER); // amount label or blank
        amount.setAlignmentX(Component.CENTER_ALIGNMENT); // center
        amount.setForeground(valueColor); // color passed in
        amount.setFont(new Font("Segoe UI", Font.BOLD, 13)); // slightly larger

        card.add(title); // add title
        card.add(Box.createVerticalStrut(4)); // small gap
        card.add(amount); // add amount
        UiKit.startPanelGlow(46, card, fill, fill.brighter(), border, valueColor.brighter()); // subtle glow effect
        return card; // return built card
    }

    private void updateTotalExpense(Map<String, Double> totals) {
        double total = 0.0; // sum all totals
        for (double value : totals.values()) {
            total += value; // accumulate
        }
        // animate from current displayed value to new total
        double current = 0.0; // current number parsed from label
        try {
            String txt = totalExpenseLabel.getText(); // e.g. "Total Expense: Rs. 123.45"
            int idx = txt.indexOf("Rs."); // find currency marker
            if (idx >= 0) {
                String num = txt.substring(idx + 3).trim(); // take text after Rs.
                current = Double.parseDouble(num.replaceAll("[^0-9.\\-]", "")); // sanitize and parse
            }
        } catch (Exception ignored) {}
        UiKit.animateCounter(totalExpenseLabel, current, total, 700); // animate label from current->total in ms
    }

    private void openEditDialogForRow(JTable table, int viewRow) { // open edit dialog for selected row index
        try {
            if (latestRows == null || viewRow < 0) return; // safety checks if nothing is selected
            int modelRow = table.convertRowIndexToModel(viewRow); // convert filtered row index to actual model index
            if (modelRow < 0 || modelRow >= latestRows.size()) return; // make sure row is still valid
            String[] r = latestRows.get(modelRow); // raw DB row values include id and all fields
            int id = Integer.parseInt(r[0]); // ID is always stored first in the row array
            String category = r[1]; // category text
            String title = r[2]; // expense title
            String amount = r[3]; // amount string
            String time = r[4]; // time string
            String day = r[5]; // day string
            String month = r[6]; // month string
            String year = r[7]; // year string
            EditExpenseDialog dlg = new EditExpenseDialog(this, id, category, title, amount, time, day, month, year); // create edit dialog with current values
            dlg.setLocationRelativeTo(this); // show dialog centered over this frame
            dlg.setVisible(true); // display modal dialog and wait until closed
            if (dlg.isSaved()) { // if user clicked Save inside the dialog
                loadData(); // reload the expense list and summary cards
            }
        } catch (Exception ex) {
            ex.printStackTrace(); // developer debug output
            UiKit.showToast(this, "Failed to open edit: " + ex.getMessage()); // user-friendly toast
        }
    }

    private void deleteExpenseAtRow(JTable table, int viewRow) { // delete selected row after confirmation
        try {
            if (latestRows == null || viewRow < 0) return; // nothing selected or no data
            int modelRow = table.convertRowIndexToModel(viewRow); // convert displayed row to model row
            if (modelRow < 0 || modelRow >= latestRows.size()) return; // validate row index
            int confirm = JOptionPane.showConfirmDialog(this, "Delete this expense?", "Confirm Delete", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
            if (confirm != JOptionPane.YES_OPTION) return; // only delete if user confirms
            int id = Integer.parseInt(latestRows.get(modelRow)[0]); // id stored in raw row
            boolean deleted = DBHelper.deleteExpense(id); // perform DB delete
            if (deleted) {
                UiKit.showToast(this, "Expense deleted."); // notify user
                loadData(); // refresh table and summaries
                DashboardFrame.refreshOpenDashboardBudget(); // update dashboard budget warning if needed
            } else {
                UiKit.showToast(this, "Could not delete expense."); // show failure message
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            UiKit.showToast(this, "Delete failed: " + ex.getMessage()); // friendly toast when exception occurs
        }
    }

    private void exportTableToCsv() { // export visible expense rows to a CSV file
        if (tableModel.getRowCount() == 0) {
            UiKit.showToast(this, "No expenses to export.");
            return;
        }

        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Save CSV Export");
        chooser.setSelectedFile(new File("expenses.csv"));
        int result = chooser.showSaveDialog(this);
        if (result != JFileChooser.APPROVE_OPTION) {
            return;
        }

        File file = chooser.getSelectedFile();
        if (!file.getName().toLowerCase().endsWith(".csv")) {
            file = new File(file.getParentFile(), file.getName() + ".csv");
        }

        try (FileWriter writer = new FileWriter(file)) {
            writer.write('\uFEFF');
            writer.write("Category,Title,Amount (Rs.),Day,Date,Time\n");
            for (int row = 0; row < tableModel.getRowCount(); row++) {
                writer.write(csvCell(tableModel.getValueAt(row, 0)) + ",");
                writer.write(csvCell(tableModel.getValueAt(row, 1)) + ",");
                writer.write(csvCell(tableModel.getValueAt(row, 2)) + ",");
                writer.write(csvCell(tableModel.getValueAt(row, 3)) + ",");
                writer.write(csvCell(tableModel.getValueAt(row, 4)) + ",");
                writer.write(csvCell(tableModel.getValueAt(row, 5)) + "\n");
            }
            writer.flush();
            UiKit.showToast(this, "CSV exported successfully.");
            if (Desktop.isDesktopSupported()) {
                try {
                    Desktop.getDesktop().open(file);
                } catch (Exception ignored) {
                    // If opening fails, the file is still saved for the user.
                }
            }
        } catch (IOException ex) {
            UiKit.showToast(this, "CSV export failed: " + ex.getMessage());
        }
    }

    private String csvCell(Object value) { // escape a value for CSV output
        String text = value == null ? "" : value.toString();
        text = text.replace("\"", "\"\""); // double quotes inside CSV needs escaping
        return "\"" + text + "\""; // wrap value in quotes
    }

    private void navigateBackToDashboard() { // return to the main dashboard if it is already open, otherwise create a new one
        for (Frame frame : Frame.getFrames()) {
            if (frame instanceof DashboardFrame && frame.isDisplayable()) {
                frame.setState(Frame.NORMAL);
                frame.setVisible(true);
                frame.toFront();
                dispose();
                return;
            }
        }
        new DashboardFrame(userName, userPhone).setVisible(true);
        dispose();
    }

    private String dayNameFromDate(String day, String month, String year) {
        try {
            int d = Integer.parseInt(day); // parse day
            int m = Integer.parseInt(month); // parse month
            int y = Integer.parseInt(year); // parse year
            LocalDate date = LocalDate.of(y, m, d); // create LocalDate
            return date.getDayOfWeek().getDisplayName(TextStyle.FULL, Locale.ENGLISH); // return full weekday name
        } catch (Exception ex) {
            return day; // fallback: return day number if parsing fails
        }
    }
}

/*
 * ViewExpensesFrame.java
 *
 * End description: list + summaries screen for user's expenses.
 */
