package gui;

import controller.LibraryManager;
import model.LibraryItem;

import javax.swing.*;
import java.awt.*;
import java.util.Map;

public class ReportsPanel extends JPanel {

    public ReportsPanel(LibraryManager manager, JLabel statusBar) {
        setLayout(new BorderLayout());

        JTextArea out = new JTextArea(20, 70);
        out.setEditable(false);

        JButton refresh = new JButton("Generate Reports");

        refresh.addActionListener(e -> {
            StringBuilder sb = new StringBuilder();

            sb.append("=== MOST BORROWED ITEMS ===\n");
            for (LibraryItem it : manager.mostBorrowed(5)) {
                sb.append(it.getTimesBorrowed()).append("x - ").append(it.toDisplayString())
                        .append(" (ID=").append(it.getId()).append(")\n");
            }

            sb.append("\n=== USERS WITH OVERDUE ITEMS ===\n");
            var overdueUsers = manager.usersWithOverdueItems();
            if (overdueUsers.isEmpty()) sb.append("None.\n");
            else for (String u : overdueUsers) sb.append(u).append("\n");

            sb.append("\n=== CATEGORY DISTRIBUTION ===\n");
            for (Map.Entry<String,Integer> en : manager.categoryDistribution().entrySet()) {
                sb.append(en.getKey()).append(": ").append(en.getValue()).append("\n");
            }

            sb.append("\n=== HOT CACHE (Most Accessed - Fixed Array) ===\n");
            var cache = manager.getHotCache();
            for (int i = 0; i < cache.length; i++) {
                sb.append("#").append(i+1).append(": ")
                        .append(cache[i] == null ? "empty" : (cache[i].getTimesAccessed() + "x - " + cache[i].toDisplayString()))
                        .append("\n");
            }

            out.setText(sb.toString());
            statusBar.setText("Reports generated.");
        });

        add(refresh, BorderLayout.NORTH);
        add(new JScrollPane(out), BorderLayout.CENTER);
    }
}