package gui;

import com.formdev.flatlaf.FlatDarkLaf;
import controller.BorrowController;
import controller.LibraryManager;
import model.LibraryDatabase;
import utils.FileHandler;

import javax.swing.*;
import java.awt.*;
import java.io.File;

public class MainWindow extends JFrame {

    private final LibraryManager manager;
    private final BorrowController borrowController;
    private final JLabel statusBar = new JLabel("Ready.");
    private File currentFile = new File("library.json");

    public MainWindow() {
        super("Smart Library Circulation & Automation System");

        LibraryDatabase db;
        try {
            db = FileHandler.loadDatabase(currentFile);
        } catch (Exception e) {
            db = new LibraryDatabase();
        }

        this.manager = new LibraryManager(db);
        this.borrowController = new BorrowController(manager);

        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(1000, 650);
        setLocationRelativeTo(null);

        setLayout(new BorderLayout());

        // Menu
        setJMenuBar(buildMenuBar());

        // Tabs
        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("View Items", new ViewItemsPanel(manager, statusBar));
        tabs.addTab("Borrow / Return", new BorrowPanel(borrowController, manager, statusBar));
        tabs.addTab("Admin", new AdminPanel(manager, statusBar));
        tabs.addTab("Search & Sort", new SearchSortPanel(manager, statusBar));
        tabs.addTab("Reports", new ReportsPanel(manager, statusBar));

        add(tabs, BorderLayout.CENTER);

        // Status bar
        statusBar.setBorder(BorderFactory.createEmptyBorder(6, 10, 6, 10));
        add(statusBar, BorderLayout.SOUTH);

        // Timer overdue notification (advanced GUI technique)
        Timer t = new Timer(8000, e -> {
            int overdue = manager.overdueCount();
            if (overdue > 0) {
                JOptionPane.showMessageDialog(this,
                        "Overdue alert: " + overdue + " item(s) are overdue!",
                        "Overdue Reminder",
                        JOptionPane.WARNING_MESSAGE);
            }
        });
        t.start();
    }

    private JMenuBar buildMenuBar() {
        JMenuBar bar = new JMenuBar();

        JMenu file = new JMenu("File");
        file.setMnemonic('F');

        JMenuItem save = new JMenuItem("Save");
        save.setMnemonic('S');
        save.addActionListener(e -> doSave());

        JMenuItem load = new JMenuItem("Load");
        load.setMnemonic('L');
        load.addActionListener(e -> doLoad());

        JMenuItem saveAs = new JMenuItem("Save As...");
        saveAs.addActionListener(e -> doSaveAs());

        file.add(save);
        file.add(saveAs);
        file.add(load);

        bar.add(file);
        return bar;
    }

    private void doSave() {
        try {
            FileHandler.saveDatabase(currentFile, manager.getDb());
            statusBar.setText("Saved to: " + currentFile.getName());
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Save failed: " + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void doLoad() {
        try {
            LibraryDatabase db = FileHandler.loadDatabase(currentFile);
            // Simple approach: restart window with loaded DB
            dispose();
            MainWindow w = new MainWindow();
            w.setVisible(true);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Load failed: " + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void doSaveAs() {
        JFileChooser chooser = new JFileChooser();
        int res = chooser.showSaveDialog(this);
        if (res == JFileChooser.APPROVE_OPTION) {
            currentFile = chooser.getSelectedFile();
            doSave();
        }
    }

 public static void main(String[] args) {

    FlatDarkLaf.setup();

    SwingUtilities.invokeLater(() ->
        new MainWindow().setVisible(true)
    );
}
}