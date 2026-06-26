/*
 * UiKit.java
 *
 * Description (start):
 * Easy explanation: This file has common UI helpers we use across the app.
 * - Colors, fonts, small custom components (rounded panels, buttons, icons).
 * - Simple animation helpers (pulse, glow, toast, counters) that can be turned off.
 * I made it so we keep all visual style in one place for easy reuse.
 *
 * End of start description.
 */
package com.expensetracker.ui; // package declaration: code lives under this package

import javax.swing.*; // import Swing UI classes
import javax.swing.border.Border; // import Border interface
import javax.swing.border.CompoundBorder; // import CompoundBorder helper
import javax.swing.table.JTableHeader; // import table header class
import java.awt.*; // import AWT classes (Color, Font, Graphics...)
import java.net.URL; // import URL for loading resources
import java.awt.geom.RoundRectangle2D; // import shape for rounded rectangles

public final class UiKit { // final utility class - not meant to be instantiated

    static final Color PURPLE_TOP = new Color(90, 92, 214); // easy: top purple for gradients
    static final Color PURPLE_BOTTOM = new Color(52, 72, 198); // easy: bottom purple for gradients
    static final Color GREEN_TOP = new Color(62, 196, 140); // easy: top green
    static final Color GREEN_BOTTOM = new Color(37, 167, 118); // easy: bottom green
    static final Color BLUE_TOP = new Color(57, 94, 240); // easy: top blue
    static final Color BLUE_BOTTOM = new Color(30, 70, 205); // easy: bottom blue
    static final Color ORANGE_TOP = new Color(255, 156, 45); // easy: top orange
    static final Color ORANGE_BOTTOM = new Color(255, 126, 25); // easy: bottom orange
    static final Color WHITE = new Color(250, 251, 255); // easy: white background helper
    static final Color SOFT_TEXT = new Color(110, 120, 145); // easy: soft gray text color
    static final Color DARK_TEXT = new Color(40, 45, 62); // easy: dark text color
    static final Color CARD_BORDER = new Color(227, 231, 244); // easy: subtle card border color

    static final Font TITLE_FONT = new Font("Segoe UI", Font.BOLD, 30); // easy: big title font
    static final Font HEADER_FONT = new Font("Segoe UI", Font.BOLD, 20); // easy: section header font
    static final Font SUBTITLE_FONT = new Font("Segoe UI", Font.PLAIN, 14); // easy: subtitle font
    static final Font LABEL_FONT = new Font("Segoe UI", Font.BOLD, 13); // easy: label font
    static final Font FIELD_FONT = new Font("Segoe UI", Font.PLAIN, 14); // easy: input field font
    static final Font BUTTON_FONT = new Font("Segoe UI", Font.BOLD, 15); // easy: button font
    static final Font TABLE_FONT = new Font("Segoe UI", Font.PLAIN, 13); // easy: table font

    private UiKit() { } // private constructor: prevent creating instance of utility class

    // Global toggle to enable/disable all animations.
    // Easy: set to false to turn off animations (helps reliability on some machines).
    static boolean ANIMATIONS_ENABLED = false; // default: animations off to avoid timing issues

    public static void setAnimationsEnabled(boolean enabled) { // easy setter for runtime toggle
        ANIMATIONS_ENABLED = enabled; // set global flag
    }

    public static void installLookAndFeel() { // easy: installs system LAF and default fonts
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); // use native look and feel
        } catch (Exception ignored) { }

        UIManager.put("Button.font", BUTTON_FONT); // set default Button font
        UIManager.put("Label.font", FIELD_FONT); // set default Label font
        UIManager.put("TextField.font", FIELD_FONT); // set default TextField font
        UIManager.put("TextArea.font", FIELD_FONT); // set default TextArea font
        UIManager.put("ComboBox.font", FIELD_FONT); // set default ComboBox font
        UIManager.put("Table.font", TABLE_FONT); // set default Table font
        UIManager.put("TableHeader.font", LABEL_FONT); // set table header font
    }

    static JPanel gradientBackground(Color top, Color bottom) { // easy: panel with vertical gradient
        return new JPanel() { // anonymous panel
            @Override
            protected void paintComponent(Graphics g) { // paint gradient when panel draws
                super.paintComponent(g); // default paint
                Graphics2D g2 = (Graphics2D) g.create(); // create copy of graphics
                g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY); // quality hint
                g2.setPaint(new GradientPaint(0, 0, top, 0, getHeight(), bottom)); // create gradient paint
                g2.fillRect(0, 0, getWidth(), getHeight()); // fill whole panel
                g2.dispose(); // dispose graphics copy
            }
        };
    }

    static JPanel plainBackground(Color color) { // easy: simple panel with single background color
        JPanel panel = new JPanel(); // create panel
        panel.setBackground(color); // set background
        return panel; // return panel
    }

    static JLabel titleLabel(String text, Color color) { // easy: centered title label factory
        JLabel label = new JLabel(text, SwingConstants.CENTER); // label with centered text
        label.setFont(TITLE_FONT); // set title font
        label.setForeground(color); // set text color
        return label; // return label
    }

    static JLabel headerLabel(String text, Color color) { // easy: simple header label factory
        JLabel label = new JLabel(text); // create label
        label.setFont(HEADER_FONT); // header font
        label.setForeground(color); // color
        return label; // return
    }

    static JLabel subtitleLabel(String text, Color color) { // easy: centered subtitle label
        JLabel label = new JLabel(text, SwingConstants.CENTER); // centered text
        label.setFont(SUBTITLE_FONT); // subtitle font
        label.setForeground(color); // color
        return label; // return
    }

    static JLabel fieldLabel(String text) { // easy: label for form fields
        JLabel label = new JLabel(text); // create label
        label.setFont(LABEL_FONT); // label style
        label.setForeground(DARK_TEXT); // dark text
        return label; // return
    }

    static JTextField textField(String placeholder) { // easy: create a text field with border and tooltip
        JTextField field = new JTextField(); // new text field
        field.setFont(FIELD_FONT); // set font
        field.setBorder(createFieldBorder()); // set custom border
        field.setPreferredSize(new Dimension(220, 34)); // preferred size
        field.setToolTipText(placeholder); // use placeholder as tooltip for quick hint
        return field; // return
    }

    static JComboBox<String> comboBox(String[] values) { // easy: create combo box with given values
        JComboBox<String> combo = new JComboBox<>(values); // new combo box
        combo.setFont(FIELD_FONT); // font
        combo.setBackground(Color.WHITE); // white background
        combo.setBorder(createFieldBorder()); // custom border
        return combo; // return
    }

    static JButton primaryButton(String text, Color bg) { // easy: primary styled button
        return new RoundedButton(text, bg, Color.WHITE); // use rounded button with white text
    }

    static JButton secondaryButton(String text, Color bg, Color fg) { // easy: secondary button
        return new RoundedButton(text, bg, fg); // rounded button with custom fg
    }

    static JButton dangerButton(String text) { // easy: red danger button
        return new RoundedButton(text, new Color(232, 51, 82), Color.WHITE); // red background
    }

    static JPanel cardPanel() { // easy: white rounded card panel (semi-transparent + soft shadow)
        return new RoundedPanel(new Color(255, 255, 255, 215), CARD_BORDER, 20); // rounded panel with border, softer radius
    }

    static JPanel softCard(Color fill, Color border) { // easy: smaller rounded card with custom colors
        return new RoundedPanel(fill, border, 18); // rounded panel
    }

    static JLabel badge(String text, Color bg, Color fg, int size) { // easy: small circular badge label
        JLabel label = new JLabel(text, SwingConstants.CENTER); // centered text
        label.setOpaque(true); // allow background color
        label.setBackground(bg); // set background
        label.setForeground(fg); // set text color
        label.setFont(new Font("Segoe UI", Font.BOLD, size)); // font size based on input
        label.setPreferredSize(new Dimension(size * 2, size * 2)); // square preferred size
        label.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8)); // padding
        return label; // return
    }

    static JPanel iconCircle(String text, Color bg, Color fg, int diameter) { // easy: small circular icon panel
        return new CircleIconPanel(text, bg, fg, diameter); // use custom circle panel
    }

    static JLabel imageIcon(String resourcePath, int size) { // easy: load image resource as icon
        JLabel label = new JLabel(); // empty label
        URL resource = UiKit.class.getResource(resourcePath); // find resource URL
        if (resource != null) { // if resource found
            ImageIcon icon = new ImageIcon(resource); // load icon
            Image scaled = icon.getImage().getScaledInstance(size, size, Image.SCALE_SMOOTH); // scale image
            label.setIcon(new ImageIcon(scaled)); // set scaled icon
        } else { // fallback when resource missing
            label.setText("E"); // show placeholder letter
            label.setFont(new Font("Segoe UI", Font.BOLD, Math.max(20, size / 2))); // set fallback font size
            label.setForeground(new Color(255, 130, 60)); // fallback color
        }
        label.setPreferredSize(new Dimension(size, size)); // prefer given size
        label.setHorizontalAlignment(SwingConstants.CENTER); // center horizontally
        label.setVerticalAlignment(SwingConstants.CENTER); // center vertically
        return label; // return label
    }

    // Load an icon by either a resource path (e.g. '/icons/food.png') or an absolute filesystem path.
    static JLabel loadIcon(String resourceOrPath, int size) {
        JLabel label = new JLabel();
        if (resourceOrPath == null || resourceOrPath.isEmpty()) {
            // fallback to empty placeholder
            label.setText("");
            label.setPreferredSize(new Dimension(size, size));
            return label;
        }
        // Try resource first
        URL resource = UiKit.class.getResource(resourceOrPath);
        try {
            if (resource != null) {
                ImageIcon icon = new ImageIcon(resource);
                Image scaled = icon.getImage().getScaledInstance(size, size, Image.SCALE_SMOOTH);
                label.setIcon(new ImageIcon(scaled));
            } else {
                // Try filesystem path
                java.io.File f = new java.io.File(resourceOrPath);
                if (f.exists()) {
                    ImageIcon icon = new ImageIcon(resourceOrPath);
                    Image scaled = icon.getImage().getScaledInstance(size, size, Image.SCALE_SMOOTH);
                    label.setIcon(new ImageIcon(scaled));
                } else {
                    // fallback: derive a sensible placeholder from the filename (e.g. '/icons/view.png' -> 'V')
                    String name = resourceOrPath;
                    // handle resource paths and Windows paths
                    if (name.contains("/")) name = name.substring(name.lastIndexOf('/') + 1);
                    if (name.contains("\\")) name = name.substring(name.lastIndexOf('\\') + 1);
                    // pick first alphanumeric character as placeholder
                    char first = 0;
                    for (char ch : name.toCharArray()) {
                        if (Character.isLetterOrDigit(ch)) { first = ch; break; }
                    }
                    String text = first == 0 ? "" : String.valueOf(Character.toUpperCase(first));
                    label.setText(text);
                    label.setFont(new Font("Segoe UI", Font.BOLD, Math.max(20, size / 2)));
                    label.setForeground(new Color(255, 130, 60));
                }
            }
        } catch (Exception ex) {
            label.setText("");
        }
        label.setPreferredSize(new Dimension(size, size));
        label.setHorizontalAlignment(SwingConstants.CENTER);
        label.setVerticalAlignment(SwingConstants.CENTER);
        return label;
    }

    static JPanel walletIcon(int diameter) { // easy: wallet artwork panel
        return new WalletIconPanel(diameter); // custom wallet drawing
    }

    static Icon eyeIcon(boolean open, int size, Color color) { // easy: draw a simple eye/eye-off icon
        return new Icon() {
            @Override
            public int getIconWidth() {
                return size;
            }

            @Override
            public int getIconHeight() {
                return size;
            }

            @Override
            public void paintIcon(Component c, Graphics g, int x, int y) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setStroke(new BasicStroke(Math.max(1.5f, size / 10f), BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                g2.setColor(color);

                int w = size;
                int h = size;
                int eyeY = y + h / 2;
                int left = x + (int) (w * 0.12);
                int right = x + (int) (w * 0.88);
                int top = y + (int) (h * 0.28);
                int bottom = y + (int) (h * 0.72);

                if (open) {
                    int[] xsTop = {left, x + w / 2, right};
                    int[] ysTop = {eyeY, top, eyeY};
                    int[] xsBottom = {left, x + w / 2, right};
                    int[] ysBottom = {eyeY, bottom, eyeY};
                    g2.drawPolyline(xsTop, ysTop, xsTop.length);
                    g2.drawPolyline(xsBottom, ysBottom, xsBottom.length);
                    g2.fillOval(x + w / 2 - Math.max(2, w / 10), y + h / 2 - Math.max(2, h / 10), Math.max(4, w / 5), Math.max(4, h / 5));
                } else {
                    g2.draw(new java.awt.geom.Ellipse2D.Double(x + (w * 0.12), y + (h * 0.30), w * 0.76, h * 0.40));
                    g2.drawLine(x + (int) (w * 0.18), y + (int) (h * 0.18), x + (int) (w * 0.82), y + (int) (h * 0.82));
                }

                g2.dispose();
            }
        };
    }

    static JPanel minimalLogo(int diameter) { // minimal flat wallet + chart logo (white)
        return new JPanel() {
            @Override
            public Dimension getPreferredSize() { return new Dimension(diameter, diameter); }
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                int w = getWidth();
                int h = getHeight();
                // background circle
                g2.setColor(new Color(255, 255, 255, 200));
                int pad = Math.max(2, w / 12);
                g2.fillOval(pad, pad, w - pad * 2, h - pad * 2);
                // wallet body (white stroke on transparent background)
                int ww = (int) (w * 0.58);
                int wh = (int) (h * 0.38);
                int wx = (w - ww) / 2;
                int wy = (h - wh) / 2 + (int) (h * 0.06);
                g2.setColor(new Color(38, 72, 158));
                g2.fillRoundRect(wx, wy, ww, wh, 10, 10);
                // small chart lines on wallet (light)
                g2.setColor(new Color(255, 255, 255));
                int bx = wx + (int) (ww * 0.12);
                int by = wy + (int) (wh * 0.18);
                int bw = (int) (ww * 0.6);
                int bh = (int) (wh * 0.48);
                int colW = bw / 5;
                for (int i = 0; i < 5; i++) {
                    int barH = (int) (bh * (0.4 + i * 0.12));
                    int x = bx + i * colW + i * 2;
                    int y = by + (bh - barH);
                    g2.fillRoundRect(x, y, Math.max(4, colW - 6), barH, 4, 4);
                }
                g2.dispose();
            }
        };
    }

    static void styleTable(JTable table) { // easy: apply consistent look to tables
        table.setFont(TABLE_FONT); // table font
        table.setRowHeight(28); // row height
        table.setShowGrid(false); // no grid lines
        table.setIntercellSpacing(new Dimension(0, 0)); // no spacing
        table.setFillsViewportHeight(true); // fill viewport
        table.setSelectionBackground(new Color(224, 235, 255)); // light selection color
        table.setSelectionForeground(DARK_TEXT); // selection text color
        table.setBackground(Color.WHITE); // white background
        table.setForeground(DARK_TEXT); // normal text color
        table.setBorder(BorderFactory.createEmptyBorder()); // no border
        JTableHeader header = table.getTableHeader(); // header object
        header.setFont(LABEL_FONT); // header font
        header.setForeground(DARK_TEXT); // header text color
        header.setBackground(new Color(245, 247, 252)); // header background
        header.setReorderingAllowed(false); // prevent column reorder
    }

    static Timer startPulse(int delayMs, Color from, Color to, JLabel... labels) { // easy: animate label color pulse
        if (!ANIMATIONS_ENABLED) { // if animations off
            // still set final color for labels once
            for (JLabel label : labels) if (label != null) label.setForeground(to); // set to target color
            return new Timer(0, null); // return dummy timer
        }
        Timer timer = new Timer(delayMs, null); // create timer with given delay
        timer.addActionListener(e -> { // action each tick
            double pulse = (Math.sin(System.currentTimeMillis() / 220.0) + 1.0) / 2.0; // oscillating value 0..1
            double ratio = 0.28 + (pulse * 0.42); // map to usable blend range
            for (JLabel label : labels) { // update each label
                if (label != null) { // skip nulls
                    label.setForeground(blend(from, to, ratio)); // set blended color
                }
            }
        });
        timer.start(); // start animation
        return timer; // return timer so caller can stop it
    }

    static Timer startPanelGlow(int delayMs, JPanel panel, Color fillFrom, Color fillTo, Color borderFrom, Color borderTo) { // easy: animate rounded panel colors
        if (!ANIMATIONS_ENABLED) { // fallback when animations off
            if (panel instanceof RoundedPanel rp) { // if our custom panel
                rp.fill = fillTo; // set final fill
                rp.border = borderTo; // set final border
                panel.repaint(); // repaint to show change
            }
            return new Timer(0, null); // dummy timer
        }
        Timer timer = new Timer(delayMs, null); // timer for animation
        timer.addActionListener(e -> {
            if (!(panel instanceof RoundedPanel roundedPanel)) { // only operate on RoundedPanel
                return; // do nothing if different type
            }
            double pulse = (Math.sin(System.currentTimeMillis() / 260.0) + 1.0) / 2.0; // oscillating 0..1
            roundedPanel.fill = blend(fillFrom, fillTo, 0.20 + (pulse * 0.40)); // blend fill
            roundedPanel.border = blend(borderFrom, borderTo, 0.20 + (pulse * 0.55)); // blend border
            panel.repaint(); // repaint to show glow
        });
        timer.start(); // start timer
        return timer; // return timer
    }

    static Timer startIconPulse(int delayMs, JPanel panel, Color bgFrom, Color bgTo, Color fgFrom, Color fgTo) { // easy: animate small circular icons
        if (!ANIMATIONS_ENABLED) { // fallback when off
            if (panel instanceof CircleIconPanel ip) { // our custom icon panel
                ip.bg = bgTo; // set final bg
                ip.fg = fgTo; // set final fg
                panel.repaint(); // repaint
            }
            return new Timer(0, null); // dummy
        }
        Timer timer = new Timer(delayMs, null); // timer
        timer.addActionListener(e -> {
            if (!(panel instanceof CircleIconPanel iconPanel)) {
                return; // require CircleIconPanel
            }
            double pulse = (Math.sin(System.currentTimeMillis() / 240.0) + 1.0) / 2.0; // oscillate
            iconPanel.bg = blend(bgFrom, bgTo, 0.22 + (pulse * 0.46)); // blend bg
            iconPanel.fg = blend(fgFrom, fgTo, 0.16 + (pulse * 0.42)); // blend fg
            panel.repaint(); // repaint to show changes
        });
        timer.start(); // start
        return timer; // return timer
    }

    static void showToast(Window owner, String message) { // easy: small toast message near bottom of owner window
        if (!ANIMATIONS_ENABLED) { // fallback if animations off
            // fallback to simple JOptionPane if animations disabled
            if (owner instanceof Frame f) { // owner is a Frame
                JOptionPane.showMessageDialog(f, message); // simple dialog
            } else {
                JOptionPane.showMessageDialog(null, message); // global dialog
            }
            return; // done
        }
        JWindow toast = new JWindow(owner); // transparent window for toast
        toast.setBackground(new Color(0, 0, 0, 0)); // fully transparent background
        JPanel content = new JPanel() { // content panel with rounded dark background
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create(); // graphics copy
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON); // smooth
                g2.setColor(new Color(34, 34, 34, 220)); // dark semi-transparent
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12); // rounded rect
                g2.dispose(); // dispose
            }
        };
        content.setOpaque(false); // transparent content
        content.setBorder(BorderFactory.createEmptyBorder(10, 14, 10, 14)); // padding
        JLabel label = new JLabel(message); // message label
        label.setForeground(Color.WHITE); // white text
        label.setFont(FIELD_FONT); // use field font
        content.add(label); // add label
        toast.add(content); // add to window
        toast.pack(); // size to content
        Point p = owner.getLocationOnScreen(); // owner's screen location
        int x = p.x + owner.getWidth() / 2 - toast.getWidth() / 2; // center horizontally
        int y = p.y + owner.getHeight() - toast.getHeight() - 40; // place above bottom
        toast.setLocation(x, y); // set location
        toast.setOpacity(0f); // start invisible
        toast.setVisible(true); // show
        Timer t = new Timer(30, null); // timer to animate opacity
        final long start = System.currentTimeMillis(); // start time
        t.addActionListener(e -> {
            long elapsed = System.currentTimeMillis() - start; // elapsed ms
            if (elapsed < 240) { // fade in
                float op = (float) elapsed / 240f; // 0..1
                toast.setOpacity(Math.min(1f, op)); // set opacity
            } else if (elapsed < 1800) { // visible time
                toast.setOpacity(1f); // full opacity
            } else if (elapsed < 2040) { // fade out
                float op = 1f - (float) (elapsed - 1800) / 240f; // decreasing
                toast.setOpacity(Math.max(0f, op)); // set opacity
            } else { // cleanup
                t.stop(); // stop timer
                toast.setVisible(false); // hide
                toast.dispose(); // free resources
            }
        });
        t.start(); // start animation timer
    }

    static void animateCounter(JLabel label, double from, double to, int durationMs) { // easy: animate number change in label
        if (!ANIMATIONS_ENABLED) { // if off, set final text quickly
            label.setText(String.format("Total Expense: Rs. %.2f", to)); // final text
            return; // done
        }
        int steps = Math.max(6, durationMs / 30); // number of steps
        Timer t = new Timer(durationMs / steps, null); // timer interval
        final long start = System.currentTimeMillis(); // start time
        t.addActionListener(e -> {
            long elapsed = System.currentTimeMillis() - start; // elapsed
            double frac = Math.min(1.0, (double) elapsed / durationMs); // progress 0..1
            double val = from + (to - from) * easeOutCubic(frac); // eased value
            label.setText(String.format("Total Expense: Rs. %.2f", val)); // update label
            if (frac >= 1.0) { // stop condition
                t.stop(); // stop timer
            }
        });
        t.start(); // start animation
    }

    private static double easeOutCubic(double t) { // easy: easing function for smooth end
        double p = t - 1.0; // shift
        return 1.0 + p * p * p; // cubic ease out formula
    }

    static void fadeInWindow(Window w, int durationMs) { // easy: fade in a window by changing opacity
        if (w == null) return; // guard null
        if (!ANIMATIONS_ENABLED) { // if disabled
            // just show the window immediately
            w.setOpacity(1f); // full opacity
            return; // done
        }
        w.setOpacity(0f); // start invisible
        Timer t = new Timer(30, null); // timer for fade
        final long start = System.currentTimeMillis(); // start time
        t.addActionListener(e -> {
            long elapsed = System.currentTimeMillis() - start; // elapsed
            double frac = Math.min(1.0, (double) elapsed / durationMs); // progress
            w.setOpacity((float) frac); // set opacity
            if (frac >= 1.0) t.stop(); // stop when done
        });
        t.start(); // start timer
    }

    private static Color blend(Color first, Color second, double ratio) { // easy: linear color blend helper
        ratio = Math.max(0.0, Math.min(1.0, ratio)); // clamp ratio 0..1
        int red = (int) Math.round(first.getRed() * (1.0 - ratio) + second.getRed() * ratio); // blend red
        int green = (int) Math.round(first.getGreen() * (1.0 - ratio) + second.getGreen() * ratio); // blend green
        int blue = (int) Math.round(first.getBlue() * (1.0 - ratio) + second.getBlue() * ratio); // blend blue
        return new Color(red, green, blue); // return blended color
    }

    static Border createFieldBorder() { // easy: create border used by input fields
        return new CompoundBorder(
            BorderFactory.createLineBorder(new Color(210, 217, 235), 1, true), // outer rounded line
            BorderFactory.createEmptyBorder(8, 12, 8, 12) // inner padding
        );
    }

    private static final class RoundedPanel extends JPanel { // easy: panel with rounded corners
        private Color fill; // panel fill color
        private Color border; // panel border color
        private final int arc; // corner arc size

        private RoundedPanel(Color fill, Color border, int arc) { // constructor
            this.fill = fill; // store fill
            this.border = border; // store border color
            this.arc = arc; // store corner radius
            setOpaque(false); // transparent background so rounded shape shows
        }

        @Override
        protected void paintComponent(Graphics g) { // custom painting for rounded panel
            Graphics2D g2 = (Graphics2D) g.create(); // copy graphics
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON); // smooth
            g2.setColor(fill); // set fill
            g2.fill(new RoundRectangle2D.Double(0, 0, getWidth() - 1, getHeight() - 1, arc, arc)); // fill rounded rect
            g2.setColor(border); // set border color
            g2.draw(new RoundRectangle2D.Double(0.5, 0.5, getWidth() - 2, getHeight() - 2, arc, arc)); // draw border
            g2.dispose(); // dispose
            super.paintComponent(g); // paint children
        }
    }

    private static final class RoundedButton extends JButton { // easy: button drawn with rounded background
        private final Color backgroundColor; // base background color
        private final Color foregroundColor; // base foreground color
        private RoundedButton(String text, Color backgroundColor, Color foregroundColor) { // constructor
            super(text); // set button text
            this.backgroundColor = backgroundColor; // store bg
            this.foregroundColor = foregroundColor; // store fg
            setFont(BUTTON_FONT); // set font
            setForeground(foregroundColor); // set text color
            setContentAreaFilled(false); // we paint background ourselves
            setFocusPainted(false); // no focus painting
            setBorderPainted(false); // border painted in paintBorder
            setOpaque(false); // let custom painting show
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)); // hand cursor for clickable
            setMargin(new Insets(10, 18, 10, 18)); // padding inside button
        }

        @Override
        protected void paintComponent(Graphics g) { // paint rounded background
            Graphics2D g2 = (Graphics2D) g.create(); // graphics copy
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON); // smooth
            Color fill = getModel().isPressed() ? backgroundColor.darker().darker() : (getModel().isRollover() ? backgroundColor.darker() : backgroundColor); // adjust for states: hover slightly darker
            g2.setColor(fill); // set fill color
            g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 18, 18); // draw rounded rect
            g2.dispose(); // dispose copy
            super.paintComponent(g); // draw text on top
        }

        @Override
        protected void paintBorder(Graphics g) { // paint border around button
            Graphics2D g2 = (Graphics2D) g.create(); // copy
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON); // smooth
            g2.setColor(backgroundColor.darker()); // darker border
            g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 18, 18); // draw border
            g2.dispose(); // dispose
        }

        @Override
        public void setForeground(Color fg) { // override to keep default fg if null
            super.setForeground(fg == null ? foregroundColor : fg); // set provided or fallback
        }
    }

    private static final class CircleIconPanel extends JPanel { // easy: small circular icon with text inside
        private final String text; // the text to draw inside circle
        private Color bg; // background color of circle
        private Color fg; // text color

        private CircleIconPanel(String text, Color bg, Color fg, int diameter) { // constructor
            this.text = text; // set text
            this.bg = bg; // set bg
            this.fg = fg; // set fg
            setPreferredSize(new Dimension(diameter, diameter)); // size
            setOpaque(false); // transparent background
        }

        @Override
        protected void paintComponent(Graphics g) { // draw circle and text
            Graphics2D g2 = (Graphics2D) g.create(); // copy graphics
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON); // smooth
            g2.setColor(bg); // bg color
            g2.fillOval(0, 0, getWidth() - 1, getHeight() - 1); // circle
            g2.setColor(fg); // text color
            Font font = new Font("Segoe UI Symbol", Font.BOLD, Math.max(18, getWidth() / 3)); // adaptive font
            g2.setFont(font); // set font
            FontMetrics fm = g2.getFontMetrics(); // metrics to center text
            int x = (getWidth() - fm.stringWidth(text)) / 2; // center x
            int y = (getHeight() - fm.getHeight()) / 2 + fm.getAscent(); // center y
            g2.drawString(text, x, y); // draw text
            g2.dispose(); // dispose
        }
    }

    private static final class WalletIconPanel extends JPanel { // easy: custom drawn wallet icon
        private final int diameter; // size

        private WalletIconPanel(int diameter) { // constructor
            this.diameter = diameter; // store size
            setPreferredSize(new Dimension(diameter, diameter)); // set preferred
            setOpaque(false); // allow transparent corners
        }

        @Override
        public Dimension getPreferredSize() { // use stored diameter so the field is referenced
            return new Dimension(diameter, diameter);
        }

        @Override
        protected void paintComponent(Graphics g) { // draw wallet artwork
            Graphics2D g2 = (Graphics2D) g.create(); // copy graphics
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON); // smooth

            int w = getWidth(); // width
            int h = getHeight(); // height

            g2.setColor(new Color(255, 255, 255)); // white circle base
            g2.fillOval(0, 0, w - 1, h - 1); // base circle

            int walletWidth = (int) (w * 0.58); // wallet width relative
            int walletHeight = (int) (h * 0.38); // wallet height relative
            int walletX = (w - walletWidth) / 2; // wallet x pos
            int walletY = (h - walletHeight) / 2 + (int) (h * 0.07); // wallet y pos

            g2.setColor(new Color(220, 109, 28)); // wallet main color
            g2.fillRoundRect(walletX, walletY, walletWidth, walletHeight, 14, 14); // fill wallet
            g2.setColor(new Color(183, 85, 16)); // wallet outline
            g2.drawRoundRect(walletX, walletY, walletWidth, walletHeight, 14, 14); // draw outline

            int flapWidth = (int) (walletWidth * 0.52); // flap width
            int flapHeight = (int) (walletHeight * 0.5); // flap height
            int flapX = walletX + (int) (walletWidth * 0.12); // flap x
            int flapY = walletY - flapHeight + 3; // flap y (above wallet)

            g2.setColor(new Color(32, 118, 230)); // flap color
            g2.fillRoundRect(flapX, flapY, flapWidth, flapHeight, 12, 12); // draw flap
            g2.setColor(new Color(24, 92, 189)); // flap outline
            g2.drawRoundRect(flapX, flapY, flapWidth, flapHeight, 12, 12); // draw flap outline

            int cardWidth = (int) (walletWidth * 0.45); // card width inside wallet
            int cardHeight = (int) (walletHeight * 0.48); // card height
            int cardX = walletX + (int) (walletWidth * 0.44); // card x
            int cardY = flapY + 4; // card y

            g2.setColor(new Color(95, 154, 84)); // card color
            g2.fillRoundRect(cardX, cardY, cardWidth, cardHeight, 12, 12); // draw card
            g2.setColor(new Color(72, 125, 61)); // card outline
            g2.drawRoundRect(cardX, cardY, cardWidth, cardHeight, 12, 12); // draw card outline

            int buttonW = (int) (walletWidth * 0.2); // small button width
            int buttonH = (int) (walletHeight * 0.28); // small button height
            int buttonX = walletX + walletWidth - buttonW / 2; // button x pos
            int buttonY = walletY + (walletHeight / 2) - (buttonH / 2); // button y pos

            g2.setColor(new Color(242, 193, 41)); // button fill
            g2.fillOval(buttonX, buttonY, buttonW, buttonH); // draw button
            g2.setColor(new Color(214, 162, 22)); // button outline
            g2.drawOval(buttonX, buttonY, buttonW, buttonH); // draw outline

            g2.setColor(new Color(255, 181, 69)); // stitch color
            for (int i = 0; i < 6; i++) { // draw stitching decoration
                int stitchY = walletY + 1; // stitch y
                int stitchX = walletX + 5 + (i * (walletWidth - 10) / 6); // stitch x
                g2.fillOval(stitchX, stitchY, 7, 4); // draw small oval
            }

            g2.dispose(); // dispose graphics copy
        }
    }
}

/*
 * UiKit.java
 *
 * End description: This file groups reusable UI widgets and helpers.
 * Why we use it (very easy): Put all style and small custom components here
 * so other screens can use them quickly without repeating code.
 */