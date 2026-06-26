/*
 * ChartFrame.java
 *
 * Description (start):
 * Easy: This frame shows a pie chart of expenses grouped by category.
 * - Purpose: give a quick visual summary of how spending is split.
 * - Why: charts are easier to present in demos than raw tables.
 *
 * End of start description.
 */
package com.expensetracker.ui; // ui package

import com.expensetracker.DBHelper; // database summary helper
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.labels.StandardPieSectionLabelGenerator;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PiePlot;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.general.DefaultPieDataset;

import javax.swing.*;
import java.awt.*;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;

public class ChartFrame extends JFrame {

    private final Timer titlePulseTimer;
    private final JPanel chartCards;
    private final CardLayout chartLayout;

    public ChartFrame(String userName, String userPhone) {
        setTitle("Expense Tracker - Chart");
        setSize(980, 720);
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setContentPane(UiKit.plainBackground(new Color(248, 250, 255)));
        getContentPane().setLayout(new BorderLayout());

        JPanel root = new JPanel(new BorderLayout());
        root.setOpaque(false);
        root.setBorder(BorderFactory.createEmptyBorder(16, 18, 18, 18));
        getContentPane().add(root, BorderLayout.CENTER);

        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        JLabel title = new JLabel("Expense Chart by Category");
        title.setFont(new Font("Segoe UI", Font.BOLD, 30));
        title.setForeground(UiKit.DARK_TEXT);
        header.add(title, BorderLayout.WEST);

        JButton backButton = UiKit.secondaryButton("Back", new Color(230, 238, 252), UiKit.DARK_TEXT);
        backButton.setPreferredSize(new Dimension(110, 36));
        backButton.addActionListener(e -> dispose());

        JPanel headerActions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        headerActions.setOpaque(false);
        JButton pieButton = UiKit.primaryButton("Pie", new Color(30, 105, 223));
        JButton barButton = UiKit.secondaryButton("Bar", new Color(230, 238, 252), UiKit.DARK_TEXT);
        pieButton.setPreferredSize(new Dimension(80, 36));
        barButton.setPreferredSize(new Dimension(80, 36));
        headerActions.add(pieButton);
        headerActions.add(barButton);
        headerActions.add(backButton);
        header.add(headerActions, BorderLayout.EAST);
        root.add(header, BorderLayout.NORTH);

        JPanel card = UiKit.softCard(new Color(255, 255, 255), new Color(225, 230, 242));
        card.setLayout(new BorderLayout(0, 12));
        card.setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));
        root.add(card, BorderLayout.CENTER);

        Map<String, Double> totals;
        try {
            totals = DBHelper.getTotalsByCategory(userPhone);
        } catch (Exception ex) {
            totals = Map.of();
        }

        chartLayout = new CardLayout();
        chartCards = new JPanel(chartLayout);
        chartCards.setOpaque(false);
        chartCards.add(createPieChartPanel(totals), "pie");
        chartCards.add(createBarChartPanel(totals), "bar");
        chartLayout.show(chartCards, "pie");
        card.add(chartCards, BorderLayout.CENTER);

        pieButton.addActionListener(e -> chartLayout.show(chartCards, "pie"));
        barButton.addActionListener(e -> chartLayout.show(chartCards, "bar"));

        JLabel hint = new JLabel("Use this chart for a quick visual summary during your demo.", SwingConstants.CENTER);
        hint.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        hint.setForeground(UiKit.SOFT_TEXT);
        hint.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));
        card.add(hint, BorderLayout.SOUTH);

        titlePulseTimer = UiKit.startPulse(45, UiKit.DARK_TEXT, new Color(70, 104, 214), title);
        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosed(java.awt.event.WindowEvent e) {
                titlePulseTimer.stop();
            }
        });
    }

    private JPanel createPieChartPanel(Map<String, Double> totals) {
        DefaultPieDataset<String> dataset = new DefaultPieDataset<>();
        List<String> categories = orderedCategories(totals);
        for (String category : categories) {
            double value = totals.getOrDefault(category, 0.0);
            if (value > 0.0) {
                dataset.setValue(category, value);
            }
        }
        if (dataset.getItemCount() == 0) {
            dataset.setValue("No Data", 1.0);
        }

        JFreeChart chart = ChartFactory.createPieChart(
            "Spending by Category",
            dataset,
            true,
            true,
            false
        );
        styleChartBackground(chart);

        @SuppressWarnings("unchecked")
        PiePlot<String> plot = (PiePlot<String>) chart.getPlot();
        stylePiePlot(plot, categories);

        ChartPanel chartPanel = new ChartPanel(chart);
        chartPanel.setOpaque(false);
        chartPanel.setPreferredSize(new Dimension(900, 560));
        return chartPanel;
    }

    private JPanel createBarChartPanel(Map<String, Double> totals) {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        List<String> categories = orderedCategories(totals);
        for (String category : categories) {
            double value = totals.getOrDefault(category, 0.0);
            if (value > 0.0) {
                dataset.addValue(value, "Expense", category);
            }
        }
        if (dataset.getColumnCount() == 0) {
            dataset.addValue(1.0, "Expense", "No Data");
        }

        JFreeChart chart = ChartFactory.createBarChart(
            "Spending by Category",
            "Category",
            "Amount (Rs.)",
            dataset
        );
        styleChartBackground(chart);

        CategoryPlot plot = chart.getCategoryPlot();
        plot.setBackgroundPaint(Color.WHITE);
        plot.setOutlineVisible(false);
        plot.setDomainGridlinePaint(new Color(230, 235, 245));
        plot.setRangeGridlinePaint(new Color(230, 235, 245));

        BarRenderer renderer = (BarRenderer) plot.getRenderer();
        renderer.setDrawBarOutline(false);
        renderer.setShadowVisible(false);
        renderer.setSeriesPaint(0, new Color(70, 104, 214));

        ChartPanel chartPanel = new ChartPanel(chart);
        chartPanel.setOpaque(false);
        chartPanel.setPreferredSize(new Dimension(900, 560));
        return chartPanel;
    }

    private void styleChartBackground(JFreeChart chart) {
        chart.setBackgroundPaint(Color.WHITE);
    }

    private void stylePiePlot(PiePlot<String> plot, List<String> categories) {
        plot.setBackgroundPaint(Color.WHITE);
        plot.setOutlineVisible(false);
        plot.setSectionOutlinesVisible(false);
        plot.setLabelOutlinePaint(null);
        plot.setLabelBackgroundPaint(new Color(255, 255, 255, 210));
        plot.setLabelShadowPaint(null);
        plot.setLabelGenerator(new StandardPieSectionLabelGenerator("{0}: {2}", new DecimalFormat("0"), new DecimalFormat("0.0%")));

        Color[] palette = {
            new Color(70, 104, 214),
            new Color(46, 177, 99),
            new Color(255, 165, 58),
            new Color(153, 105, 255),
            new Color(242, 101, 135),
            new Color(30, 150, 190),
            new Color(110, 120, 245),
            new Color(83, 88, 105),
            new Color(245, 128, 37)
        };

        // Assign colors only to keys actually present in the dataset to avoid UnknownKeyException
        List<String> keys = plot.getDataset().getKeys( );
        for (int i = 0; i < keys.size(); i++) {
            String key = keys.get(i) == null ? "" : keys.get(i).toString();
            plot.setSectionPaint(key, palette[i % palette.length]);
        }
        if (keys.contains("No Data")) {
            plot.setSectionPaint("No Data", new Color(180, 186, 200));
        }
    }

    private List<String> orderedCategories(Map<String, Double> totals) {
        LinkedHashSet<String> ordered = new LinkedHashSet<>();
        ordered.add("Entertainment");
        ordered.add("Shopping");
        ordered.add("Food");
        ordered.add("Grocery");
        ordered.add("Education");
        ordered.add("Health");
        ordered.add("Travel");
        ordered.add("Bills");
        ordered.add("Other");
        ordered.addAll(new java.util.TreeSet<>(totals.keySet()));
        return new ArrayList<>(ordered);
    }
}

/*
 * ChartFrame.java
 *
 * End description: Visual summary screen for expenses, showing both pie and bar charts.
 */