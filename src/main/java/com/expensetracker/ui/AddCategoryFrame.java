/*
 * AddCategoryFrame.java
 *
 * Description (start):
 * Easy: This screen shows category choices (Entertainment, Shopping, Food, Grocery).
 * - Purpose: let user pick a category before adding an expense.
 * - Why: selecting category first keeps Add Expense focused and quick.
 *
 * End of start description.
 */
package com.expensetracker.ui; // ui package

import javax.swing.*; // Swing UI toolkit
import java.awt.*; // AWT for Color, Dimension, layouts

public class AddCategoryFrame extends JFrame { // frame to pick a category

    private final String userName; // user's name passed from previous screen
    private final String userPhone; // user's phone passed from previous screen
    private final Timer titlePulseTimer; // optional timer for title animation

    public AddCategoryFrame(String userName, String userPhone) { // constructor
        this.userName = userName; // store username
        this.userPhone = userPhone; // store phone

        setTitle("Expense Tracker - Add Category"); // window title
        setPreferredSize(new Dimension(1200, 700)); // preferred size for full-screen feel
        setExtendedState(JFrame.MAXIMIZED_BOTH); // open maximized instead of a small popup
        setLocationRelativeTo(null); // center on screen
        setResizable(true); // allow resize
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE); // dispose only this window on close
        setContentPane(UiKit.plainBackground(new Color(248, 250, 255))); // plain background
        getContentPane().setLayout(new BorderLayout()); // use border layout for main content pane

        JPanel topBar = new JPanel(); // small decorative top bar (was blue in original design)
        topBar.setPreferredSize(new Dimension(0, 14)); // height 14px
        topBar.setBackground(new Color(18, 52, 112)); // deep blue color
        getContentPane().add(topBar, BorderLayout.NORTH); // add to top

        JPanel root = new JPanel(new BorderLayout(0, 18)); // root panel with spacing between elements
        root.setOpaque(false); // let background show
        root.setBorder(BorderFactory.createEmptyBorder(16, 18, 16, 18)); // padding around
        getContentPane().add(root, BorderLayout.CENTER); // add root to center

        JLabel title = new JLabel("Select Category", SwingConstants.CENTER); // big centered title
        title.setFont(new Font("Segoe UI", Font.BOLD, 28)); // font size
        title.setForeground(UiKit.DARK_TEXT); // dark text color from UiKit
        root.add(title, BorderLayout.NORTH); // add title to top of root
        titlePulseTimer = UiKit.startPulse(45, UiKit.DARK_TEXT, new Color(70, 104, 214), title); // pulse animation for title
        addWindowListener(new java.awt.event.WindowAdapter() { // ensure timer stops when window closes
            @Override
            public void windowClosed(java.awt.event.WindowEvent e) { // on window close
                titlePulseTimer.stop(); // stop animation timer
            }
        });

        JPanel grid = new JPanel(new GridLayout(2, 2, 14, 14)); // 2x2 grid for category cards
        grid.setOpaque(false); // transparent background
        grid.add(createCategoryCard("Entertainment", "🎬", new Color(148, 76, 224), "Entertainment")); // card 1
        grid.add(createCategoryCard("Shopping", "🛒", new Color(46, 177, 99), "Shopping")); // card 2
        grid.add(createCategoryCard("Food", "🍔", new Color(245, 145, 29), "Food")); // card 3
        grid.add(createCategoryCard("Grocery", "🛍", new Color(49, 127, 242), "Grocery")); // card 4

        JPanel gridWrap = new JPanel(new BorderLayout()); // wrapper to add top spacing
        gridWrap.setOpaque(false); // transparent
        gridWrap.setBorder(BorderFactory.createEmptyBorder(6, 0, 0, 0)); // top margin
        gridWrap.add(grid, BorderLayout.CENTER); // add grid to center of wrapper
        root.add(gridWrap, BorderLayout.CENTER); // add to root center

        JButton backButton = UiKit.secondaryButton("Back", new Color(193, 206, 225), Color.WHITE); // back button style
        backButton.setPreferredSize(new Dimension(0, 42)); // height 42
        backButton.addActionListener(e -> dispose()); // close this window on click

        JPanel bottom = new JPanel(new BorderLayout()); // bottom area for back button
        bottom.setOpaque(false); // transparent
        bottom.add(backButton, BorderLayout.CENTER); // center the button
        root.add(bottom, BorderLayout.SOUTH); // add bottom to root
    }

    private JPanel createCategoryCard(String displayName, String emojiIcon, Color iconColor, String categoryValue) { // builds one category card
        JPanel card = UiKit.cardPanel(); // rounded card panel from UiKit
        card.setLayout(new GridBagLayout()); // keep icon+label centered even when card is tall
        card.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)); // hand cursor to show clickable
        card.setBorder(BorderFactory.createEmptyBorder(14, 14, 14, 14)); // padding inside card

        JPanel content = new JPanel(); // grouped content centered inside card
        content.setOpaque(false); // transparent
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS)); // icon above text

        JPanel iconWrap = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0)); // wrapper for icon
        iconWrap.setOpaque(false); // transparent
        JComponent iconComp = UiKit.iconCircle(emojiIcon, iconColor, Color.WHITE, 58); // emoji drawn inside a colored circle
        iconWrap.add(iconComp);
        iconWrap.setAlignmentX(Component.CENTER_ALIGNMENT); // center in BoxLayout axis

        JLabel label = new JLabel(displayName, SwingConstants.CENTER); // label with category name
        label.setFont(new Font("Segoe UI", Font.BOLD, 18)); // font size
        label.setForeground(UiKit.DARK_TEXT); // dark color
        label.setAlignmentX(Component.CENTER_ALIGNMENT); // center alignment in box layout

        content.add(iconWrap); // add icon
        content.add(Box.createVerticalStrut(10)); // space between icon and label
        content.add(label); // add label

        GridBagConstraints centerGbc = new GridBagConstraints(); // center the grouped content both axes
        centerGbc.gridx = 0; // single column
        centerGbc.gridy = 0;
        centerGbc.weightx = 1; // take all horizontal space
        centerGbc.weighty = 1;
        centerGbc.anchor = GridBagConstraints.CENTER;
        card.add(content, centerGbc);

        card.addMouseListener(new java.awt.event.MouseAdapter() { // click listener for card
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) { // when clicked
                // If right-click, allow user to pick a custom icon for this category
                if (SwingUtilities.isRightMouseButton(e)) {
                    SwingUtilities.invokeLater(() -> {
                        try {
                            JFileChooser chooser = new JFileChooser();
                            chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
                            int res = chooser.showOpenDialog(AddCategoryFrame.this);
                            if (res == JFileChooser.APPROVE_OPTION) {
                                java.io.File chosen = chooser.getSelectedFile();
                                // copy chosen file to user icons folder
                                java.nio.file.Path iconsDir = java.nio.file.Paths.get(System.getProperty("user.home"), ".expense-tracker", "icons");
                                java.nio.file.Files.createDirectories(iconsDir);
                                String ext = ".png";
                                String name = categoryValue.toLowerCase().replaceAll("[^a-z0-9]", "-") + ext;
                                java.nio.file.Path dest = iconsDir.resolve(name);
                                java.nio.file.Files.copy(chosen.toPath(), dest, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
                                // persist absolute path in DB
                                com.expensetracker.DBHelper.updateCategoryIcon(categoryValue, dest.toAbsolutePath().toString());
                                UiKit.showToast(AddCategoryFrame.this, "Icon updated for " + categoryValue);
                            }
                        } catch (Exception ex) {
                            ex.printStackTrace();
                            UiKit.showToast(AddCategoryFrame.this, "Failed to set icon: " + ex.getMessage());
                        }
                    });
                    return; // consume right-click
                }
                SwingUtilities.invokeLater(() -> { // ensure runs on EDT
                    try {
                        System.out.println("[AddCategory] opening AddExpenseFrame for category='" + categoryValue + "'"); // debug log
                        AddExpenseFrame f = new AddExpenseFrame(userName, userPhone, categoryValue); // create add-expense window
                        // make sure the new window is visible and in front
                        f.setAlwaysOnTop(true); // temporarily keep on top so it appears above others
                        f.setVisible(true); // show window
                        f.toFront(); // bring to front
                        f.requestFocus(); // request focus
                        try {
                            System.out.println("[AddCategory] created AddExpenseFrame: visible=" + f.isShowing() + ", focused=" + f.isFocused()); // debug info
                        } catch (Exception dbg) {
                            // some platforms may throw when querying focus/location; ignore debug failure
                            System.out.println("[AddCategory] created AddExpenseFrame (debug info unavailable)");
                        }
                        // remove always-on-top shortly after so it behaves normally
                        new Timer(140, ev -> { // timer to remove always-on-top after short delay
                            f.setAlwaysOnTop(false); // restore normal behavior
                            ((Timer) ev.getSource()).stop(); // stop this timer
                        }).start();
                    } catch (Exception ex) { // if creating new window fails
                        ex.printStackTrace(); // print stacktrace for developer
                        UiKit.showToast(AddCategoryFrame.this, "Failed to open Add Expense: " + ex.getMessage()); // show friendly toast
                    } finally {
                        // dispose the category selector shortly after opening the new frame
                        new Timer(300, ev -> { // delay then close this selector window
                            AddCategoryFrame.this.dispose(); // close current window
                            ((Timer) ev.getSource()).stop(); // stop timer
                        }).start();
                    }
                });
            }
        });

        if (iconComp instanceof JPanel) {
            UiKit.startIconPulse(40, (JPanel) iconComp, iconColor, iconColor.brighter(), Color.WHITE, new Color(255, 250, 240)); // small icon pulse animation
        }
        UiKit.startPanelGlow(50, card, Color.WHITE, new Color(252, 253, 255), new Color(227, 231, 244), new Color(168, 186, 235)); // panel glow effect

        return card; // return the constructed card
    }
}

/*
 * AddCategoryFrame.java
 *
 * End description: Small selector screen used before Add Expense.
 * Very easy: pick a category, it opens Add Expense for that category.
 */
