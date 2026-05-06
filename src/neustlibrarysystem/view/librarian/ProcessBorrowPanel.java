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

public class ProcessBorrowPanel extends JPanel {

    private final BorrowDAO   borrowDAO;
    private final Librarian   librarian;
    private DefaultTableModel tableModel;
    private JTable            table;
    private JTextField        tfSearch;

    public ProcessBorrowPanel(Librarian librarian) {
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

        JLabel hdr = new JLabel("➕   Process Borrow");
        hdr.setFont(LibrarianDashboard.FONT_HEADER);
        hdr.setForeground(LibrarianDashboard.CLR_PRIMARY);
        header.add(hdr, BorderLayout.WEST);

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

        JButton btnSearch  = LibrarianDashboard.primaryBtn("🔍  Search");
        JButton btnRefresh = LibrarianDashboard.accentBtn("🔄  Refresh");
        btnSearch .addActionListener(e -> searchBorrows());
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

        // ── Fix: visible table header text ────────────────────────────────────
        JTableHeader tableHeader = table.getTableHeader();
        tableHeader.setBackground(new Color(0xd4e6a0));
        tableHeader.setForeground(new Color(0x3a4d1e));
        tableHeader.setFont(LibrarianDashboard.FONT_LABEL);
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

        // Color rows by status
        table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object val,
                    boolean sel, boolean foc, int r, int c) {
                Component comp = super.getTableCellRendererComponent(t, val, sel, foc, r, c);
                if (!sel) {
                    String status = String.valueOf(tableModel.getValueAt(r, 6));
                    switch (status) {
                        case "Overdue":
                            comp.setBackground(new Color(0xfde8e8));
                            comp.setForeground(new Color(0xC0392B));
                            break;
                        case "Borrowed":
                            comp.setBackground(r % 2 == 0 ? Color.WHITE : new Color(0xf4fae8));
                            comp.setForeground(new Color(0x303c1b));
                            break;
                        default:
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

        JButton btnBorrow = LibrarianDashboard.primaryBtn("📖  Issue Borrow");
        btnBorrow.addActionListener(e -> showBorrowDialog());
        south.add(btnBorrow);
        add(south, BorderLayout.SOUTH);
    }

    public void refresh() {
        tableModel.setRowCount(0);
        for (BorrowedRecord r : borrowDAO.getActiveBorrows())
            tableModel.addRow(new Object[]{
                r.getBorrowID(), r.getMemberName(), r.getStudentID(),
                r.getBookTitle(), r.getBorrowDate(), r.getDueDate(),
                r.getStatus(), String.format("₱%.2f", r.getFineAmount())
            });
    }

    private void searchBorrows() {
        String kw = tfSearch.getText().trim();
        if (kw.isEmpty()) { refresh(); return; }
        tableModel.setRowCount(0);
        for (BorrowedRecord r : borrowDAO.searchActiveBorrows(kw))
            tableModel.addRow(new Object[]{
                r.getBorrowID(), r.getMemberName(), r.getStudentID(),
                r.getBookTitle(), r.getBorrowDate(), r.getDueDate(),
                r.getStatus(), String.format("₱%.2f", r.getFineAmount())
            });
    }

    private void showBorrowDialog() {
        JTextField tfBookID   = field("");
        JTextField tfMemberID = field("");
        JTextField tfResID    = field("");

        JPanel dlg = new JPanel(new GridBagLayout());
        dlg.setBackground(new Color(0xf4fae8));
        dlg.setBorder(new EmptyBorder(14, 16, 14, 16));
        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(8, 8, 8, 8);
        g.anchor = GridBagConstraints.WEST;

        Object[][] rows = {
            {"Book ID: *",                 tfBookID},
            {"Member ID: *",               tfMemberID},
            {"Reservation ID (optional):", tfResID}
        };
        for (int i = 0; i < rows.length; i++) {
            g.gridx = 0; g.gridy = i; g.weightx = 0; g.fill = GridBagConstraints.NONE;
            JLabel lbl = new JLabel((String) rows[i][0]);
            lbl.setFont(LibrarianDashboard.FONT_LABEL);
            lbl.setForeground(new Color(0x303c1b));
            dlg.add(lbl, g);
            g.gridx = 1; g.weightx = 1; g.fill = GridBagConstraints.HORIZONTAL;
            dlg.add((Component) rows[i][1], g);
        }

        if (JOptionPane.showConfirmDialog(this, dlg, "Issue Borrow",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE)
                != JOptionPane.OK_OPTION) return;

        try {
            int     bookID   = Integer.parseInt(tfBookID  .getText().trim());
            int     memberID = Integer.parseInt(tfMemberID.getText().trim());
            Integer resID    = tfResID.getText().trim().isEmpty() ? null
                             : Integer.parseInt(tfResID.getText().trim());

            int borrowID = borrowDAO.processBorrow(bookID, memberID, librarian.getLibrarianID(), resID);
            if (borrowID > 0) {
                JOptionPane.showMessageDialog(this,
                    "✔  Borrow issued successfully!\nBorrow ID: " + borrowID,
                    "Success", JOptionPane.INFORMATION_MESSAGE);
                refresh();
            } else {
                JOptionPane.showMessageDialog(this,
                    "Failed. Check borrow limit or book availability.",
                    "Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "IDs must be valid numbers.",
                "Validation", JOptionPane.WARNING_MESSAGE);
        }
    }

    private JTextField field(String val) {
        JTextField tf = new JTextField(val, 20);
        tf.setFont(LibrarianDashboard.FONT_BODY);
        tf.setForeground(new Color(0x303c1b));
        tf.setBackground(Color.WHITE);
        tf.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(LibrarianDashboard.CLR_ACCENT, 1),
            new EmptyBorder(4, 8, 4, 8)
        ));
        return tf;
    }
}