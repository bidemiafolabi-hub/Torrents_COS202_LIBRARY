package gui;

import controller.SearchEngine;
import controller.SortEngine;
import controller.LibraryManager;
import model.LibraryItem;

import javax.swing.*;
import java.awt.*;
import java.util.List;

public class SearchSortPanel extends JPanel {

    public SearchSortPanel(LibraryManager manager, JLabel statusBar) {
        setLayout(new BorderLayout());

        JPanel top = new JPanel(new GridBagLayout());
        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(6,6,6,6);
        g.fill = GridBagConstraints.HORIZONTAL;

        JTextField query = new JTextField(22);
        JComboBox<String> searchBy = new JComboBox<>(new String[]{"Title (Linear)","Title (Binary - needs sorted)","Author (Recursive)"});

        JComboBox<SortEngine.SortField> sortField = new JComboBox<>(SortEngine.SortField.values());
        JComboBox<SortEngine.Algo> sortAlgo = new JComboBox<>(SortEngine.Algo.values());

        JButton runSearch = new JButton("Search");
        JButton runSort = new JButton("Sort");

        JTextArea output = new JTextArea(18, 60);
        output.setEditable(false);

        g.gridx=0; g.gridy=0; top.add(new JLabel("Query:"), g);
        g.gridx=1; top.add(query, g);
        g.gridx=2; top.add(searchBy, g);
        g.gridx=3; top.add(runSearch, g);

        g.gridx=0; g.gridy=1; top.add(new JLabel("Sort Field:"), g);
        g.gridx=1; top.add(sortField, g);
        g.gridx=2; top.add(sortAlgo, g);
        g.gridx=3; top.add(runSort, g);

        add(top, BorderLayout.NORTH);
        add(new JScrollPane(output), BorderLayout.CENTER);

        runSearch.addActionListener(e -> {
            String q = query.getText().trim();
            if (q.isBlank()) {
                JOptionPane.showMessageDialog(this, "Enter a query.", "Validation", JOptionPane.WARNING_MESSAGE);
                return;
            }

            List<LibraryItem> items = manager.getDb().getItems();
            int idx = -1;

            String mode = (String) searchBy.getSelectedItem();
            if (mode.startsWith("Title (Linear")) idx = SearchEngine.linearSearchByTitle(items, q);
            else if (mode.startsWith("Title (Binary")) idx = SearchEngine.binarySearchByTitle(items, q);
            else idx = SearchEngine.recursiveSearchByAuthor(items, q);

            if (idx == -1) {
                output.setText("Not found.\n");
                statusBar.setText("Search: not found");
            } else {
                LibraryItem found = items.get(idx);
                manager.markAccess(found);
                output.setText("Found:\n" + found.toDisplayString() + "\nID: " + found.getId() + "\n");
                statusBar.setText("Search: found " + found.getId());
            }
        });

        runSort.addActionListener(e -> {
            var field = (SortEngine.SortField) sortField.getSelectedItem();
            var algo = (SortEngine.Algo) sortAlgo.getSelectedItem();

            List<LibraryItem> sorted = SortEngine.sort(manager.getDb().getItems(), field, algo);

            StringBuilder sb = new StringBuilder();
            sb.append("Sorted by ").append(field).append(" using ").append(algo).append("\n\n");
            for (LibraryItem it : sorted) sb.append(it.toDisplayString()).append(" | ID=").append(it.getId()).append("\n");

            output.setText(sb.toString());
            statusBar.setText("Sorted items displayed (does not overwrite DB list).");
        });
    }
}