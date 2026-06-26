/*
 * DashboardFrame.java
 *
 * Description (start):
 * Easy: Main app home screen that shows welcome, date/time and big action cards.
 * - Purpose: let user choose Add Data or View Data and log out.
 * - Why: central navigation screen after login.
 *
 * End of start description.
 */
package com.expensetracker.ui; // package for UI screens

import com.expensetracker.DBHelper;
import javax.swing.*; // Swing UI toolkit
import java.awt.*; // AWT for colors, layouts
import java.sql.SQLException;
import java.time.LocalTime; // current time
import java.time.LocalDate; // get current date
import java.time.format.TextStyle; // format day/month names
import java.util.Locale; // locale for display names
import java.util.Map;

public class DashboardFrame extends JFrame { // dashboard window class

    private static DashboardFrame activeDashboard; // most recent dashboard instance for refresh hooks
    private final JLabel welcomeLabel; // label that shows "Welcome, <name>"
    private final JLabel timeLabel; // label that shows current time
    private final Timer headerTimer; // timer to animate header colors
    private final JTextField budgetField; // input for monthly budget
    private final JLabel budgetWarningLabel; // visible when >=80%
    private final String phone; // store logged-in phone for DB access

    public DashboardFrame(String name, String phone) { // constructor receives user name and phone
        this.phone = phone;
        activeDashboard = this; // register current dashboard for refresh after add/edit actions
        setTitle("Expense Tracker - Home"); // window title
        setSize(1280, 760); // default window size
        setLocationRelativeTo(null); // center on screen
        setResizable(true); // allow resizing
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); // exit app on close
        setContentPane(UiKit.plainBackground(new Color(247, 249, 255))); // white-ish background

        JPanel root = new JPanel(new BorderLayout()); // root panel with border layout
        root.setOpaque(false); // transparent so background shows
        root.setBorder(BorderFactory.createEmptyBorder(0, 0, 18, 0)); // bottom padding
        add(root); // add root to frame

        JPanel header = new JPanel(new BorderLayout()); // top header bar
        header.setOpaque(true); // opaque for solid color
        header.setBackground(new Color(63, 88, 214)); // blue header background
        header.setBorder(BorderFactory.createEmptyBorder(18, 30, 18, 30)); // restored header padding
        header.setPreferredSize(new Dimension(0, 100)); // restored header height

        JPanel userBlock = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0)); // left area with user icon + text
        userBlock.setOpaque(false); // transparent
        JPanel userIcon = UiKit.iconCircle(getInitial(name), new Color(255, 255, 255), new Color(73, 93, 180), 56); // restored avatar size
        userBlock.add(userIcon); // add icon

        JPanel textBlock = new JPanel(); // text block for welcome and phone
        textBlock.setOpaque(false); // transparent
        textBlock.setLayout(new BoxLayout(textBlock, BoxLayout.Y_AXIS)); // vertical stack
        welcomeLabel = new JLabel("Welcome, " + name); // welcome message
        welcomeLabel.setFont(new Font("Segoe UI", Font.BOLD, 18)); // restored header font size
        welcomeLabel.setForeground(Color.WHITE); // white text on header
        JLabel phoneLabel = new JLabel("Phone: " + phone); // show phone below name
        phoneLabel.setFont(new Font("Segoe UI", Font.PLAIN, 11)); // smaller font
        phoneLabel.setForeground(new Color(233, 238, 255)); // slightly different white
        textBlock.add(welcomeLabel); // add welcome label
        textBlock.add(phoneLabel); // add phone label
        userBlock.add(textBlock); // add text block to user block
        header.add(userBlock, BorderLayout.WEST); // place user block left in header

        JPanel dateBlock = new JPanel(); // right side of header with date/time
        dateBlock.setOpaque(false); // transparent
        dateBlock.setLayout(new BoxLayout(dateBlock, BoxLayout.Y_AXIS)); // vertical
        LocalDate today = LocalDate.now(); // get today's date
        JLabel day = new JLabel(today.getDayOfWeek().getDisplayName(TextStyle.FULL, Locale.ENGLISH)); // day name like "Monday"
        day.setForeground(Color.WHITE); // white text
        day.setFont(new Font("Segoe UI", Font.PLAIN, 12)); // restored day font size
        JLabel date = new JLabel(today.getMonth().getDisplayName(TextStyle.FULL, Locale.ENGLISH) + " " + today.getDayOfMonth() + ", " + today.getYear()); // full month + day + year
        date.setForeground(Color.WHITE); // white text
        date.setFont(new Font("Segoe UI", Font.PLAIN, 12)); // restored date font size
        timeLabel = new JLabel(com.expensetracker.ValidationUtils.formatTime12Hour(LocalTime.now().withSecond(0).withNano(0))); // show current time in 12-hour format
        timeLabel.setForeground(Color.WHITE); // white text
        timeLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12)); // restored time font size
        dateBlock.add(day); // add day label
        dateBlock.add(date); // add date label
        dateBlock.add(timeLabel); // add time label
        header.add(dateBlock, BorderLayout.EAST); // place date block right in header

        UiKit.startIconPulse(40, userIcon, new Color(255, 255, 255), new Color(233, 238, 255), new Color(73, 93, 180), new Color(36, 56, 156)); // optional icon pulse

        headerTimer = new Timer(40, e -> animateHeader()); // timer to animate header colors
        headerTimer.start(); // start the animation timer

        addWindowListener(new java.awt.event.WindowAdapter() { // stop timer when window closes
            @Override
            public void windowClosed(java.awt.event.WindowEvent e) {
                headerTimer.stop(); // stop timer to free resources
                if (activeDashboard == DashboardFrame.this) {
                    activeDashboard = null; // clear refresh hook when dashboard closes
                }
            }
        });

        root.add(header, BorderLayout.NORTH); // add header to top of root

        // Budget panel: show monthly budget field and save button, plus current spending status
        JPanel budgetPanelOuter = new JPanel(new BorderLayout());
        budgetPanelOuter.setOpaque(false);
        budgetPanelOuter.setBorder(BorderFactory.createEmptyBorder(12, 20, 12, 20));

        JPanel budgetPanelTop = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 8));
        budgetPanelTop.setOpaque(false);
        JLabel budgetLabel = new JLabel("Monthly Budget (Rs.):");
        budgetLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        budgetField = new JTextField(10);
        budgetField.setPreferredSize(new Dimension(160, 28));
        JButton saveBudget = UiKit.primaryButton("Save", new Color(30, 115, 210));
        saveBudget.setPreferredSize(new Dimension(84, 28));
        budgetPanelTop.add(budgetLabel);
        budgetPanelTop.add(budgetField);
        budgetPanelTop.add(saveBudget);

        budgetWarningLabel = new JLabel("");
        budgetWarningLabel.setForeground(Color.RED);
        budgetWarningLabel.setFont(new Font("Segoe UI", Font.BOLD, 11));
        budgetWarningLabel.setVisible(false);
        JPanel warningPanel = new JPanel(new BorderLayout());
        warningPanel.setOpaque(false);
        warningPanel.add(budgetWarningLabel, BorderLayout.WEST);

        budgetPanelOuter.add(budgetPanelTop, BorderLayout.NORTH);
        budgetPanelOuter.add(warningPanel, BorderLayout.CENTER);

        // Load current budget and refresh warning
        try {
            double b = DBHelper.getUserMonthlyBudget(phone);
            if (b > 0) budgetField.setText(String.format("%.0f", b));
        } catch (SQLException ex) {
            ex.printStackTrace();
        }

        saveBudget.addActionListener(e -> {
            String txt = budgetField.getText().trim();
            try {
                double val = 0.0;
                if (!txt.isEmpty()) val = Double.parseDouble(txt);
                if (val < 0) {
                    JOptionPane.showMessageDialog(this, "Budget cannot be negative.");
                    return;
                }
                DBHelper.updateUserMonthlyBudget(phone, val);
                refreshBudgetStatus();
                UiKit.showToast(this, "Budget saved: Rs. " + String.format("%.0f", val));
            } catch (NumberFormatException nfe) {
                JOptionPane.showMessageDialog(this, "Please enter a valid number for budget.");
            } catch (SQLException sqe) {
                sqe.printStackTrace();
                JOptionPane.showMessageDialog(this, "Failed to save budget: " + sqe.getMessage());
            }
        });

        // We'll add the budget panel into the content area once it's created below.

        JPanel content = new JPanel(); // main content panel
        content.setOpaque(false); // transparent
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS)); // vertical layout
        content.setBorder(BorderFactory.createEmptyBorder(20, 24, 20, 24)); // restored content padding
        root.add(content, BorderLayout.CENTER); // put content in center

        // Insert budget panel at top of content
        content.add(budgetPanelOuter);

        content.add(Box.createVerticalGlue()); // push cards toward vertical center
        JPanel center = new JPanel();
        center.setLayout(new BoxLayout(center, BoxLayout.Y_AXIS)); // vertical stack: cards then button
        center.setOpaque(false); // transparent
        center.setAlignmentX(Component.CENTER_ALIGNMENT); // center alignment

        JPanel actionRow = new JPanel(new GridLayout(1, 2, 16, 0)); // two cards side-by-side (tighter gap)
        actionRow.setOpaque(false); // transparent
        actionRow.setPreferredSize(new Dimension(1120, 300)); // restored cards width/height
        actionRow.setMaximumSize(new Dimension(1120, 300)); // max size to keep layout

        actionRow.add(createHomeCard("+", "Add Data", "Add New Expense", new Color(242, 247, 255), new Color(30, 105, 223), () -> { // first card: Add Data
            new AddCategoryFrame(name, phone).setVisible(true); // open AddCategoryFrame when clicked
        }));
        
        // Use a packaged icon resource for the View Data card (place /icons/view.png in resources)
        actionRow.add(createHomeCard("/icons/view.png", "View Data", "View All Expenses", new Color(242, 247, 255), new Color(46, 177, 99), () -> { // second card: View Data
            new ViewExpensesFrame(name, phone).setVisible(true); // open ViewExpensesFrame when clicked
        }));

        actionRow.setAlignmentX(Component.CENTER_ALIGNMENT);
        center.add(actionRow); // add action row to center wrapper
        center.add(Box.createVerticalStrut(18)); // gap before chart button

        JButton chartButton = UiKit.primaryButton("View Chart", new Color(108, 92, 231)); // chart shortcut button
        chartButton.setPreferredSize(new Dimension(280, 44)); // compact button size
        chartButton.setMaximumSize(new Dimension(280, 44)); // keep width fixed
        chartButton.setAlignmentX(Component.CENTER_ALIGNMENT); // center the button
        chartButton.addActionListener(e -> {
            SwingUtilities.invokeLater(() -> {
                try {
                    new ChartFrame(name, phone).setVisible(true);
                } catch (Exception ex) {
                    ex.printStackTrace();
                    JOptionPane.showMessageDialog(this, "Failed to open chart: " + ex.getMessage());
                }
            });
        }); // open chart screen
        JPanel chartButtonRow = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0)); // center wrapper for chart button
        chartButtonRow.setOpaque(false); // transparent
        chartButtonRow.add(chartButton); // add the button
        chartButtonRow.setAlignmentX(Component.CENTER_ALIGNMENT);
        center.add(chartButtonRow); // add chart button row
        content.add(center); // add center to content

        // initial budget warning check
        refreshBudgetStatus();

        content.add(Box.createVerticalStrut(14)); // spacing before logout

        JButton logoutButton = UiKit.dangerButton("Logout"); // danger styled logout button
        logoutButton.setPreferredSize(new Dimension(1120, 48)); // restored logout width and height
        logoutButton.setMaximumSize(new Dimension(1120, 48)); // max size
        logoutButton.addActionListener(e -> { // on click logout
            dispose(); // close dashboard
            new LoginFrame().setVisible(true); // show login
        });

        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0)); // bottom area for logout
        bottom.setOpaque(false); // transparent
        bottom.add(logoutButton); // add logout button
        bottom.setAlignmentX(Component.CENTER_ALIGNMENT); // center
        content.add(bottom); // add bottom to content
        content.add(Box.createVerticalGlue()); // keep the block vertically centered
    }

    private String getInitial(String name) { // helper: first letter of actual user name
        if (name == null) return "U"; // fallback
        String trimmed = name.trim(); // remove spaces
        if (trimmed.isEmpty()) return "U"; // fallback
        for (char ch : trimmed.toCharArray()) { // scan for first real character
            if (Character.isLetterOrDigit(ch)) {
                return String.valueOf(Character.toUpperCase(ch)); // return first initial
            }
        }
        return "U"; // fallback if no useful char found
    }

    private JPanel createHomeCard(String icon, String title, String subtitle, Color bg, Color accent, Runnable onClick) { // helper: builds each action card
        JPanel card = UiKit.cardPanel(); // rounded white card from UiKit
        card.setPreferredSize(new Dimension(560, 300)); // restored card size
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS)); // vertical stack inside card
        card.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)); // show hand cursor to indicate clickable
        card.setBorder(BorderFactory.createEmptyBorder(28, 28, 28, 28)); // restored padding inside card

        JPanel iconWrap = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0)); // wrapper for large icon
        iconWrap.setOpaque(false); // transparent
        // If `icon` is a resource path (starts with '/icons/'), load the image resource.
        if (icon != null && icon.startsWith("/icons/")) {
            JLabel img = UiKit.loadIcon(icon, 66); // load resource or filesystem image
            iconWrap.add(img); // add image label
        } else {
            JPanel iconPanel = UiKit.iconCircle(icon, accent, Color.WHITE, 66); // big circular icon
            iconWrap.add(iconPanel); // add icon to wrapper
            UiKit.startIconPulse(42, iconPanel, accent, accent.brighter(), Color.WHITE, new Color(255, 252, 245)); // icon pulse for circle
        }
        card.add(iconWrap); // add wrapper to card
        card.add(Box.createVerticalStrut(12)); // spacing

        JLabel titleLabel = new JLabel(title, SwingConstants.CENTER); // card title label
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 20)); // font
        titleLabel.setForeground(UiKit.DARK_TEXT); // dark text color from UiKit
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT); // center inside card
        card.add(titleLabel); // add title

        JLabel subtitleLabel = new JLabel(subtitle, SwingConstants.CENTER); // subtitle label
        subtitleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13)); // font
        subtitleLabel.setForeground(UiKit.SOFT_TEXT); // soft text color
        subtitleLabel.setAlignmentX(Component.CENTER_ALIGNMENT); // center
        card.add(Box.createVerticalStrut(4)); // small gap
        card.add(subtitleLabel); // add subtitle

        card.addMouseListener(new java.awt.event.MouseAdapter() { // click listener for the whole card
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                onClick.run(); // run provided action
            }
        });
        UiKit.startPanelGlow(50, card, bg, Color.WHITE, new Color(225, 231, 246), accent.brighter()); // subtle card glow
        return card; // return constructed card
    }

    private void animateHeader() { // animate header labels colors
        double pulse = (Math.sin(System.currentTimeMillis() / 350.0) + 1.0) / 2.0; // oscillating value
        welcomeLabel.setForeground(blend(Color.WHITE, new Color(222, 230, 255), 0.22 + (pulse * 0.18))); // blend welcome color
        timeLabel.setForeground(blend(Color.WHITE, new Color(232, 236, 255), 0.18 + (pulse * 0.12))); // blend time color
    }

    private Color blend(Color first, Color second, double ratio) { // helper: blend two colors
        ratio = Math.max(0.0, Math.min(1.0, ratio)); // clamp ratio
        int red = (int) Math.round(first.getRed() * (1.0 - ratio) + second.getRed() * ratio); // blend red channel
        int green = (int) Math.round(first.getGreen() * (1.0 - ratio) + second.getGreen() * ratio); // blend green
        int blue = (int) Math.round(first.getBlue() * (1.0 - ratio) + second.getBlue() * ratio); // blend blue
        return new Color(red, green, blue); // return blended color
    }

    // Refresh budget warning based on current month's expenses
    public void refreshBudgetStatus() {
        try {
            double budget = DBHelper.getUserMonthlyBudget(phone);
            if (budget > 0) {
                double spending = DBHelper.getCurrentMonthSpending(phone);
                double percentage = (spending / budget) * 100;
                
                if (spending > budget) {
                    // Over budget
                    budgetWarningLabel.setText(String.format("⚠️ OVER BUDGET! You have spent Rs. %.0f out of Rs. %.0f (%.0f%%)", spending, budget, percentage));
                    budgetWarningLabel.setForeground(new Color(220, 38, 38)); // dark red
                    budgetWarningLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
                    budgetWarningLabel.setVisible(true);
                } else if (percentage >= 80) {
                    // Warning: 80% or more
                    budgetWarningLabel.setText(String.format("⚠️ Warning: You have reached %.0f%% of your budget (Rs. %.0f / Rs. %.0f)", percentage, spending, budget));
                    budgetWarningLabel.setForeground(new Color(234, 88, 12)); // orange
                    budgetWarningLabel.setFont(new Font("Segoe UI", Font.BOLD, 11));
                    budgetWarningLabel.setVisible(true);
                } else {
                    // All good
                    budgetWarningLabel.setText(String.format("✓ Spending: Rs. %.0f / Rs. %.0f (%.0f%% used)", spending, budget, percentage));
                    budgetWarningLabel.setForeground(new Color(34, 197, 94)); // green
                    budgetWarningLabel.setFont(new Font("Segoe UI", Font.PLAIN, 11));
                    budgetWarningLabel.setVisible(true);
                }
            } else {
                budgetWarningLabel.setVisible(false);
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            budgetWarningLabel.setVisible(false);
        }
    }

    public static void refreshOpenDashboardBudget() {
        if (activeDashboard != null) {
            activeDashboard.refreshBudgetStatus();
        }
    }
}

/*
 * DashboardFrame.java
 *
 * End description: Central screen for navigation — add/view expenses and logout.
 * Very easy: click a card to go to that screen.
 */