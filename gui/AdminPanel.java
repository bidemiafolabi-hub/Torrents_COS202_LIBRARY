package gui;

import controller.LibraryManager;

import javax.swing.*;
import java.awt.*;

public class AdminPanel extends JPanel {

    public AdminPanel(LibraryManager manager, JLabel statusBar) {
        setLayout(new BorderLayout());

        JPanel form = new JPanel(new GridBagLayout());
        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(6,6,6,6);
        g.fill = GridBagConstraints.HORIZONTAL;

        JComboBox<String> type = new JComboBox<>(new String[]{"Book","Magazine","Journal"});
        JTextField title = new JTextField(22);
        JTextField author = new JTextField(22);
        JTextField category = new JTextField(22);
        JTextField year = new JTextField(22);

        JButton add = new JButton("Add Item");
        JButton delete = new JButton("Delete by ID");
        JButton undo = new JButton("Undo Last Admin Action");

        JTextField deleteId = new JTextField(22);

        g.gridx=0; g.gridy=0; form.add(new JLabel("Type:"), g);
        g.gridx=1; form.add(type, g);

        g.gridx=0; g.gridy=1; form.add(new JLabel("Title:"), g);
        g.gridx=1; form.add(title, g);

        g.gridx=0; g.gridy=2; form.add(new JLabel("Author:"), g);
        g.gridx=1; form.add(author, g);

        g.gridx=0; g.gridy=3; form.add(new JLabel("Category:"), g);
        g.gridx=1; form.add(category, g);

        g.gridx=0; g.gridy=4; form.add(new JLabel("Year:"), g);
        g.gridx=1; form.add(year, g);

        g.gridx=0; g.gridy=5; form.add(add, g);
        g.gridx=1; form.add(undo, g);

        g.gridx=0; g.gridy=6; form.add(new JLabel("Delete Item ID:"), g);
        g.gridx=1; form.add(deleteId, g);

        g.gridx=0; g.gridy=7; g.gridwidth=2; form.add(delete, g);

        add(form, BorderLayout.NORTH);

        add.addActionListener(e -> {
            try {
                if (title.getText().isBlank() || author.getText().isBlank() || category.getText().isBlank() || year.getText().isBlank()) {
                    JOptionPane.showMessageDialog(this, "All fields are required.", "Validation", JOptionPane.WARNING_MESSAGE);
                    return;
                }
                int y = Integer.parseInt(year.getText().trim());
                var item = manager.addItem((String) type.getSelectedItem(),
                        title.getText().trim(),
                        author.getText().trim(),
                        y,
                        category.getText().trim());

                statusBar.setText("Added: " + item.getId() + " " + item.toDisplayString());
                title.setText(""); author.setText(""); category.setText(""); year.setText("");
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Year must be a number.", "Validation", JOptionPane.WARNING_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Add failed: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        delete.addActionListener(e -> {
            String id = deleteId.getText().trim();
            if (id.isBlank()) {
                JOptionPane.showMessageDialog(this, "Enter an item ID to delete.", "Validation", JOptionPane.WARNING_MESSAGE);
                return;
            }
            boolean ok = manager.deleteItem(id);
            statusBar.setText(ok ? "Deleted item: " + id : "Delete failed: not found");
        });

        undo.addActionListener(e -> {
            boolean ok = manager.undoLastAdminAction();
            statusBar.setText(ok ? "Undo successful." : "Nothing to undo.");
        });
    }
}