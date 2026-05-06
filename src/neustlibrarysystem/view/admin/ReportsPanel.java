package neustlibrarysystem.view.admin;

import neustlibrarysystem.dao.BorrowDAO;
import neustlibrarysystem.dao.BookDAO;
import neustlibrarysystem.dao.MemberDAO;
import neustlibrarysystem.model.Admin;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class ReportsPanel extends JPanel {

    private final Admin currentAdmin;
    private final BorrowDAO borrowDAO = new BorrowDAO();
    private final BookDAO   bookDAO   = new BookDAO();
    private final MemberDAO memberDAO = new MemberDAO();

    public ReportsPanel(Admin currentAdmin) {
        this.currentAdmin = currentAdmin;
        initComponents();
    }

    private void initComponents() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        JLabel title = new JLabel("Reports & Analytics");
        title.setFont(new Font("Segoe UI", Font.BOLD, 20));
        title.setForeground(new Color(0x1B4F72));
        add(title, BorderLayout.NORTH);

        JTabbedPane reportTabs = new JTabbedPane();
        reportTabs.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        reportTabs.addTab("Borrow Summary",  buildBorrowSummaryTab());
        reportTabs.addTab("Overdue Books",   buildOverdueTab());
        reportTabs.addTab("Book Inventory",  buildInventoryTab());
        reportTabs.addTab("Member Activity", buildMemberActivityTab());
        add(reportTabs, BorderLayout.CENTER);
    }

    private JPanel buildBorrowSummaryTab() {
        JPanel panel = new JPanel(new BorderLayout(8, 8));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        String[] cols = {"Borrow ID", "Member Name", "Student ID", "Book Title", "Borrow Date", "Due Date", "Status", "Fine"};
        DefaultTableModel model = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable table = new JTable(model);
        styleTable(table);

        JPanel topBar = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JComboBox<String> statusBox = new JComboBox<>(new String[]{"All", "Borrowed", "Returned", "Overdue", "Lost"});
        statusBox.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        JButton loadBtn = styledBtn("Load", new Color(0x1B4F72));

        loadBtn.addActionListener(e -> {
            String selectedStatus = (String) statusBox.getSelectedItem();

            // ─── SwingWorker: Borrow Summary ─────────────────────────────────────
            loadBtn.setEnabled(false);
            loadBtn.setText("Loading...");

            SwingWorker<List<Object[]>, Void> worker = new SwingWorker<>() {
                @Override
                protected List<Object[]> doInBackground() {
                    // Nasa background thread — ligtas mag-query ng DB dito
                    return borrowDAO.getBorrowSummaryReport(selectedStatus);
                }

                @Override
                protected void done() {
                    // Balik sa EDT para i-update ang table
                    try {
                        List<Object[]> rows = get();
                        model.setRowCount(0);
                        for (Object[] row : rows) model.addRow(row);
                    } catch (Exception ex) {
                        JOptionPane.showMessageDialog(
                                ReportsPanel.this,
                                "Error loading borrow summary: " + ex.getMessage(),
                                "Error", JOptionPane.ERROR_MESSAGE
                        );
                    } finally {
                        loadBtn.setEnabled(true);
                        loadBtn.setText("Load");
                    }
                }
            };

            worker.execute();
        });

        topBar.add(new JLabel("Filter by Status:"));
        topBar.add(statusBox);
        topBar.add(loadBtn);
        panel.add(topBar, BorderLayout.NORTH);
        panel.add(new JScrollPane(table), BorderLayout.CENTER);
        return panel;
    }

    private JPanel buildOverdueTab() {
        JPanel panel = new JPanel(new BorderLayout(8, 8));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        String[] cols = {"Borrow ID", "Member Name", "Student ID", "Book Title", "Borrow Date", "Due Date", "Days Overdue", "Fine (PHP)"};
        DefaultTableModel model = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable table = new JTable(model);
        styleTable(table);

        JPanel topBar = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton loadBtn = styledBtn("Load Overdue Records", new Color(0xC0392B));

        loadBtn.addActionListener(e -> {
            // ─── SwingWorker: Overdue ─────────────────────────────────────────────
            loadBtn.setEnabled(false);
            loadBtn.setText("Loading...");

            SwingWorker<List<Object[]>, Void> worker = new SwingWorker<>() {
                @Override
                protected List<Object[]> doInBackground() {
                    return borrowDAO.getOverdueReport();
                }

                @Override
                protected void done() {
                    try {
                        List<Object[]> rows = get();
                        model.setRowCount(0);
                        for (Object[] row : rows) model.addRow(row);
                    } catch (Exception ex) {
                        JOptionPane.showMessageDialog(
                                ReportsPanel.this,
                                "Error loading overdue records: " + ex.getMessage(),
                                "Error", JOptionPane.ERROR_MESSAGE
                        );
                    } finally {
                        loadBtn.setEnabled(true);
                        loadBtn.setText("Load Overdue Records");
                    }
                }
            };

            worker.execute();
        });

        topBar.add(loadBtn);
        panel.add(topBar, BorderLayout.NORTH);
        panel.add(new JScrollPane(table), BorderLayout.CENTER);
        return panel;
    }

    private JPanel buildInventoryTab() {
        JPanel panel = new JPanel(new BorderLayout(8, 8));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        String[] cols = {"Book ID", "ISBN", "Title", "Category", "Publisher", "Total Copies", "Available", "Status"};
        DefaultTableModel model = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable table = new JTable(model);
        styleTable(table);

        JPanel topBar = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton loadBtn = styledBtn("Load Inventory", new Color(0x117A65));

        loadBtn.addActionListener(e -> {
            // ─── SwingWorker: Inventory ───────────────────────────────────────────
            loadBtn.setEnabled(false);
            loadBtn.setText("Loading...");

            SwingWorker<List<Object[]>, Void> worker = new SwingWorker<>() {
                @Override
                protected List<Object[]> doInBackground() {
                    return bookDAO.getInventoryReport();
                }

                @Override
                protected void done() {
                    try {
                        List<Object[]> rows = get();
                        model.setRowCount(0);
                        for (Object[] row : rows) model.addRow(row);
                    } catch (Exception ex) {
                        JOptionPane.showMessageDialog(
                                ReportsPanel.this,
                                "Error loading inventory: " + ex.getMessage(),
                                "Error", JOptionPane.ERROR_MESSAGE
                        );
                    } finally {
                        loadBtn.setEnabled(true);
                        loadBtn.setText("Load Inventory");
                    }
                }
            };

            worker.execute();
        });

        topBar.add(loadBtn);
        panel.add(topBar, BorderLayout.NORTH);
        panel.add(new JScrollPane(table), BorderLayout.CENTER);
        return panel;
    }

    private JPanel buildMemberActivityTab() {
        JPanel panel = new JPanel(new BorderLayout(8, 8));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        String[] cols = {"Member ID", "Student ID", "Full Name", "Email", "Total Borrows", "Active", "Returned", "Overdue"};
        DefaultTableModel model = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable table = new JTable(model);
        styleTable(table);

        JPanel topBar = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton loadBtn = styledBtn("Load Member Activity", new Color(0x1B4F72));

        loadBtn.addActionListener(e -> {
            // ─── SwingWorker: Member Activity ─────────────────────────────────────
            loadBtn.setEnabled(false);
            loadBtn.setText("Loading...");

            SwingWorker<List<Object[]>, Void> worker = new SwingWorker<>() {
                @Override
                protected List<Object[]> doInBackground() {
                    return memberDAO.getMemberActivityReport();
                }

                @Override
                protected void done() {
                    try {
                        List<Object[]> rows = get();
                        model.setRowCount(0);
                        for (Object[] row : rows) model.addRow(row);
                    } catch (Exception ex) {
                        JOptionPane.showMessageDialog(
                                ReportsPanel.this,
                                "Error loading member activity: " + ex.getMessage(),
                                "Error", JOptionPane.ERROR_MESSAGE
                        );
                    } finally {
                        loadBtn.setEnabled(true);
                        loadBtn.setText("Load Member Activity");
                    }
                }
            };

            worker.execute();
        });

        topBar.add(loadBtn);
        panel.add(topBar, BorderLayout.NORTH);
        panel.add(new JScrollPane(table), BorderLayout.CENTER);
        return panel;
    }

    private JButton styledBtn(String text, Color bg) {
        JButton btn = new JButton(text);
        btn.setBackground(bg);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        return btn;
    }

    private void styleTable(JTable table) {
        table.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        table.setRowHeight(26);
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 13));
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    }
}