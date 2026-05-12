package neustlibrarysystem.view.librarian;

import neustlibrarysystem.dao.BorrowDAO;
import neustlibrarysystem.model.BorrowedRecord;
import neustlibrarysystem.model.Librarian;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.util.List;

public class ProcessReturnPanel extends JPanel {

    private final BorrowDAO   borrowDAO;
    private final Librarian   librarian;
    private DefaultTableModel tableModel;
    private JTable            table;
    private JButton           btnRefresh, btnReturn, btnSearch;
    private JTextField        tfSearch;

    public ProcessReturnPanel(Librarian librarian) {
        this.librarian = librarian;
        this.borrowDAO = new BorrowDAO();
        setLayout(new BorderLayout(0, 0));
        setBackground(LibrarianDashboard.CLR_BG);
        buildUI();
        refresh();
    }

    private void buildUI() {

        // ── Header ────────────────────────────────────────────────────────────
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(LibrarianDashboard.CLR_BG);
        header.setBorder(new EmptyBorder(0, 0, 14, 0));

        JLabel hdr = new JLabel("↩   Process Return");
        hdr.setFont(LibrarianDashboard.FONT_HEADER);
        hdr.setForeground(LibrarianDashboard.CLR_PRIMARY);
        header.add(hdr, BorderLayout.WEST);

        // ── Search bar ────────────────────────────────────────────────────────
        JPanel bar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        bar.setOpaque(false);

        JLabel searchLbl = new JLabel("Search:");
        searchLbl.setFont(LibrarianDashboard.FONT_LABEL);
        searchLbl.setForeground(new Color(0x303c1b));

        tfSearch = new JTextField(22);
        tfSearch.setFont(LibrarianDashboard.FONT_BODY);
        tfSearch.setForeground(new Color(0x303c1b));
        tfSearch.setBackground(Color.WHITE);
        tfSearch.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(LibrarianDashboard.CLR_ACCENT, 1, true),
            new EmptyBorder(5, 10, 5, 10)
        ));

        btnSearch  = LibrarianDashboard.primaryBtn("🔍  Search");
        btnRefresh = LibrarianDashboard.accentBtn("🔄  Refresh");
        btnSearch .addActionListener(e -> searchReturns());
        btnRefresh.addActionListener(e -> { tfSearch.setText(""); refresh(); });

        bar.add(searchLbl); bar.add(tfSearch);
        bar.add(btnSearch); bar.add(btnRefresh);
        header.add(bar, BorderLayout.EAST);
        add(header, BorderLayout.NORTH);

        // ── Table ─────────────────────────────────────────────────────────────
        String[] cols = {"Borrow ID", "Member", "Student ID", "Book Title",
                         "Borrow Date", "Due Date", "Status", "Fine"};
        tableModel = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        table = new JTable(tableModel);
        LibrarianDashboard.styleTable(table);

        JTableHeader tableHeader = table.getTableHeader();
        tableHeader.setOpaque(true);
        tableHeader.setPreferredSize(new Dimension(tableHeader.getWidth(), 36));
        tableHeader.setDefaultRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object val,
                    boolean sel, boolean foc, int r, int c) {
                JLabel lbl = (JLabel) super.getTableCellRendererComponent(t, val, sel, foc, r, c);
                lbl.setBackground(new Color(0xd4e6a0));
                lbl.setForeground(new Color(0x3a4d1e));
                lbl.setFont(LibrarianDashboard.FONT_LABEL);
                lbl.setHorizontalAlignment(SwingConstants.CENTER);
                lbl.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createMatteBorder(0, 0, 0, 1, new Color(0xb5cc80)),
                    new EmptyBorder(4, 6, 4, 6)
                ));
                lbl.setOpaque(true);
                return lbl;
            }
        });

        table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object val,
                    boolean sel, boolean foc, int r, int c) {
                Component comp = super.getTableCellRendererComponent(t, val, sel, foc, r, c);
                if (!sel) {
                    String status = String.valueOf(tableModel.getValueAt(r, 6));
                    if ("Overdue".equals(status)) {
                        comp.setBackground(new Color(0xfde8e8));
                        comp.setForeground(new Color(0xC0392B));
                    } else {
                        comp.setBackground(r % 2 == 0 ? Color.WHITE : new Color(0xf4fae8));
                        comp.setForeground(new Color(0x303c1b));
                    }
                }
                return comp;
            }
        });

        table.getColumnModel().getColumn(0).setMaxWidth(80);
        table.getColumnModel().getColumn(2).setMaxWidth(110);
        table.getColumnModel().getColumn(6).setMaxWidth(90);
        table.getColumnModel().getColumn(7).setMaxWidth(100);

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(BorderFactory.createLineBorder(LibrarianDashboard.CLR_ACCENT, 1));
        scroll.getViewport().setBackground(Color.WHITE);
        add(scroll, BorderLayout.CENTER);

        // ── Action bar ────────────────────────────────────────────────────────
        JPanel south = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 10));
        south.setOpaque(false);

        btnReturn = LibrarianDashboard.primaryBtn("↩  Process Return");
        btnReturn.addActionListener(e -> processReturn());

        south.add(btnReturn);
        add(south, BorderLayout.SOUTH);
    }

    // ── SWINGWORKER: refresh ──────────────────────────────────────────────────
    public void refresh() {
        btnRefresh.setEnabled(false);
        btnReturn .setEnabled(false);
        btnSearch .setEnabled(false);
        tableModel.setRowCount(0);

        new SwingWorker<List<BorrowedRecord>, Void>() {
            @Override
            protected List<BorrowedRecord> doInBackground() {
                return borrowDAO.getActiveBorrows();
            }

            @Override
            protected void done() {
                try {
                    List<BorrowedRecord> result = get();
                    if (result != null) {
                        for (BorrowedRecord r : result) {
                            tableModel.addRow(new Object[]{
                                r.getBorrowID(),   r.getMemberName(), r.getStudentID(),
                                r.getBookTitle(),  r.getBorrowDate(), r.getDueDate(),
                                r.getStatus(),
                                String.format("₱%.2f", r.getFineAmount())
                            });
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    JOptionPane.showMessageDialog(ProcessReturnPanel.this,
                        "Error loading records.", "Error", JOptionPane.ERROR_MESSAGE);
                } finally {
                    btnRefresh.setEnabled(true);
                    btnReturn .setEnabled(true);
                    btnSearch .setEnabled(true);
                }
            }
        }.execute();
    }

    // ── searchReturns ─────────────────────────────────────────────────────────
    private void searchReturns() {
        String kw = tfSearch.getText().trim();
        if (kw.isEmpty()) { refresh(); return; }

        // Exact match by Borrow ID if pure number
        if (kw.matches("\\d+")) {
            int targetID = Integer.parseInt(kw);
            for (int r = tableModel.getRowCount() - 1; r >= 0; r--) {
                if ((int) tableModel.getValueAt(r, 0) != targetID) {
                    tableModel.removeRow(r);
                }
            }
            if (tableModel.getRowCount() == 0) {
                JOptionPane.showMessageDialog(this,
                    "No record found with Borrow ID: " + targetID,
                    "Not Found", JOptionPane.WARNING_MESSAGE);
            }
            return;
        }

        // Text search via DAO
        tableModel.setRowCount(0);
        List<BorrowedRecord> results = borrowDAO.searchActiveBorrows(kw);
        if (results != null) {
            for (BorrowedRecord r : results) {
                tableModel.addRow(new Object[]{
                    r.getBorrowID(),   r.getMemberName(), r.getStudentID(),
                    r.getBookTitle(),  r.getBorrowDate(), r.getDueDate(),
                    r.getStatus(),
                    String.format("₱%.2f", r.getFineAmount())
                });
            }
        }
        if (tableModel.getRowCount() == 0) {
            JOptionPane.showMessageDialog(this,
                "No records found for: " + kw,
                "Not Found", JOptionPane.WARNING_MESSAGE);
        }
    }

    // ── SWINGWORKER: processReturn ────────────────────────────────────────────
    private void processReturn() {
        int row = table.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Select a borrow record first.",
                "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int    borrowID    = (int)    tableModel.getValueAt(row, 0);
        String memberName  = (String) tableModel.getValueAt(row, 1);
        String bookTitle   = (String) tableModel.getValueAt(row, 3);
        String currentFine = (String) tableModel.getValueAt(row, 7);

        JComboBox<String> cbCond = new JComboBox<>(
            new String[]{"Good", "New", "Fair", "Damaged", "Lost"});
        cbCond.setFont(LibrarianDashboard.FONT_BODY);
        cbCond.setBackground(Color.WHITE);

        JTextField tfRemarks = new JTextField(22);
        tfRemarks.setFont(LibrarianDashboard.FONT_BODY);
        tfRemarks.setForeground(new Color(0x303c1b));
        tfRemarks.setBackground(Color.WHITE);
        tfRemarks.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(LibrarianDashboard.CLR_ACCENT, 1),
            new EmptyBorder(4, 8, 4, 8)
        ));

        JPanel dlg = new JPanel(new GridBagLayout());
        dlg.setBackground(new Color(0xf4fae8));
        dlg.setBorder(new EmptyBorder(14, 16, 14, 16));
        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(8, 8, 8, 8);
        g.anchor = GridBagConstraints.WEST;

        addInfoRow(dlg, g, 0, "Member:", memberName);
        addInfoRow(dlg, g, 1, "Book:",   bookTitle);
        addInfoRow(dlg, g, 2, "Fine:",   currentFine);

        g.gridx = 0; g.gridy = 3; g.gridwidth = 2; g.fill = GridBagConstraints.HORIZONTAL;
        JSeparator sep = new JSeparator();
        sep.setForeground(LibrarianDashboard.CLR_ACCENT);
        dlg.add(sep, g);
        g.gridwidth = 1; g.fill = GridBagConstraints.NONE;

        g.gridx = 0; g.gridy = 4; g.weightx = 0;
        JLabel condLbl = new JLabel("Book Condition: *");
        condLbl.setFont(LibrarianDashboard.FONT_LABEL);
        condLbl.setForeground(new Color(0x303c1b));
        dlg.add(condLbl, g);
        g.gridx = 1; g.weightx = 1; g.fill = GridBagConstraints.HORIZONTAL;
        dlg.add(cbCond, g);

        g.gridx = 0; g.gridy = 5; g.weightx = 0; g.fill = GridBagConstraints.NONE;
        JLabel remLbl = new JLabel("Remarks:");
        remLbl.setFont(LibrarianDashboard.FONT_LABEL);
        remLbl.setForeground(new Color(0x303c1b));
        dlg.add(remLbl, g);
        g.gridx = 1; g.weightx = 1; g.fill = GridBagConstraints.HORIZONTAL;
        dlg.add(tfRemarks, g);

        if (JOptionPane.showConfirmDialog(this, dlg,
                "Process Return — Borrow #" + borrowID,
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE)
                != JOptionPane.OK_OPTION) return;

        btnReturn .setEnabled(false);
        btnRefresh.setEnabled(false);
        btnSearch .setEnabled(false);

        final String condition = (String) cbCond.getSelectedItem();
        final String remarks   = tfRemarks.getText().trim();

        new SwingWorker<Double, Void>() {
            @Override
            protected Double doInBackground() {
                return borrowDAO.processReturn(
                    borrowID, librarian.getLibrarianID(),
                    condition, remarks
                );
            }

            @Override
            protected void done() {
                try {
                    double fine = get();
                    if (fine >= 0) {
                        JOptionPane.showMessageDialog(ProcessReturnPanel.this,
                            "✔  Return processed successfully!\n" +
                            (fine > 0 ? "Fine charged: ₱" + String.format("%.2f", fine)
                                      : "No fine."),
                            "Success", JOptionPane.INFORMATION_MESSAGE);
                        refresh();
                    } else {
                        JOptionPane.showMessageDialog(ProcessReturnPanel.this,
                            "Failed to process return.", "Error", JOptionPane.ERROR_MESSAGE);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    JOptionPane.showMessageDialog(ProcessReturnPanel.this,
                        "Error: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                } finally {
                    btnReturn .setEnabled(true);
                    btnRefresh.setEnabled(true);
                    btnSearch .setEnabled(true);
                }
            }
        }.execute();
    }

    private void addInfoRow(JPanel dlg, GridBagConstraints g,
                             int row, String label, String value) {
        g.gridx = 0; g.gridy = row; g.weightx = 0; g.fill = GridBagConstraints.NONE;
        JLabel lbl = new JLabel(label);
        lbl.setFont(LibrarianDashboard.FONT_LABEL);
        lbl.setForeground(new Color(0x485f48));
        dlg.add(lbl, g);

        g.gridx = 1; g.weightx = 1; g.fill = GridBagConstraints.HORIZONTAL;
        JLabel val = new JLabel(value);
        val.setFont(LibrarianDashboard.FONT_BODY);
        val.setForeground(new Color(0x303c1b));
        dlg.add(val, g);
    }
}