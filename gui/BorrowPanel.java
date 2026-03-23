package gui;

import controller.BorrowController;
import controller.LibraryManager;
import model.Book;
import model.LibraryItem;

import javax.swing.*;
import java.awt.*;

public class BorrowPanel extends JPanel {

    public BorrowPanel(BorrowController controller, LibraryManager manager, JLabel statusBar) {
        setLayout(new GridBagLayout());
        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(6,6,6,6);
        g.fill = GridBagConstraints.HORIZONTAL;

        JTextField userId = new JTextField(20);
        JTextField fullName = new JTextField(20);
        JTextField itemId = new JTextField(20);

        JButton borrow = new JButton("Borrow");
        JButton returns = new JButton("Return");

        JLabel info = new JLabel("Enter User + Item ID. Borrow returns due in 14 days.");

        g.gridx=0; g.gridy=0; add(new JLabel("User ID:"), g);
        g.gridx=1; add(userId, g);

        g.gridx=0; g.gridy=1; add(new JLabel("Full Name:"), g);
        g.gridx=1; add(fullName, g);

        g.gridx=0; g.gridy=2; add(new JLabel("Item ID:"), g);
        g.gridx=1; add(itemId, g);

        g.gridx=0; g.gridy=3; add(borrow, g);
        g.gridx=1; add(returns, g);

        g.gridx=0; g.gridy=4; g.gridwidth=2; add(info, g);

        // Validation + dialog popups (advanced GUI technique)
        borrow.addActionListener(e -> {
            if (userId.getText().isBlank() || fullName.getText().isBlank() || itemId.getText().isBlank()) {
                JOptionPane.showMessageDialog(this, "All fields are required.", "Validation", JOptionPane.WARNING_MESSAGE);
                return;
            }
            String msg = controller.borrow(userId.getText().trim(), fullName.getText().trim(), itemId.getText().trim());
            statusBar.setText(msg);

            // Show due date for books
            LibraryItem it = manager.getItemById(itemId.getText().trim());
            if (it instanceof Book b && !b.isAvailable()) {
                JOptionPane.showMessageDialog(this, "Due date: " + b.getDueDate(), "Borrow Info", JOptionPane.INFORMATION_MESSAGE);
            }
        });

        returns.addActionListener(e -> {
            if (userId.getText().isBlank() || itemId.getText().isBlank()) {
                JOptionPane.showMessageDialog(this, "User ID and Item ID are required.", "Validation", JOptionPane.WARNING_MESSAGE);
                return;
            }
            String msg = controller.returns(userId.getText().trim(), itemId.getText().trim());
            statusBar.setText(msg);
        });
    }
}