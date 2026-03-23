package gui;

import controller.LibraryManager;
import model.LibraryItem;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import java.awt.*;
import java.util.List;

public class ViewItemsPanel extends JPanel {

    private final LibraryManager manager;
    private final JLabel statusBar;
    private final ItemsTableModel model;

    public ViewItemsPanel(LibraryManager manager, JLabel statusBar) {
        this.manager = manager;
        this.statusBar = statusBar;

        setLayout(new BorderLayout());

        model = new ItemsTableModel(manager.getDb().getItems());
        JTable table = new JTable(model);

        // Tooltips (advanced GUI technique)
        table.setToolTipText("Library items table. Select a row to view item details.");

        add(new JScrollPane(table), BorderLayout.CENTER);

        JButton refresh = new JButton("Refresh");
        refresh.addActionListener(e -> {
            model.setData(manager.getDb().getItems());
            statusBar.setText("Refreshed items view.");
        });

        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT));
        top.add(refresh);
        add(top, BorderLayout.NORTH);
    }

    static class ItemsTableModel extends AbstractTableModel {
        private List<LibraryItem> data;
        private final String[] cols = {"ID","Type","Title","Author","Category","Year","Borrowed"};

        ItemsTableModel(List<LibraryItem> data) { this.data = data; }
        public void setData(List<LibraryItem> data) { this.data = data; fireTableDataChanged(); }

        @Override public int getRowCount() { return data.size(); }
        @Override public int getColumnCount() { return cols.length; }
        @Override public String getColumnName(int c) { return cols[c]; }

        @Override
        public Object getValueAt(int r, int c) {
            LibraryItem it = data.get(r);
            return switch (c) {
                case 0 -> it.getId();
                case 1 -> it.getType();
                case 2 -> it.getTitle();
                case 3 -> it.getAuthor();
                case 4 -> it.getCategory();
                case 5 -> it.getYear();
                case 6 -> (it instanceof model.Book b) ? (!b.isAvailable()) : "N/A";
                default -> "";
            };
        }
    }
}