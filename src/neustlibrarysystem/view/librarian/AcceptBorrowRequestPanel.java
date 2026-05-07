package neustlibrarysystem.view.librarian;

import neustlibrarysystem.dao.BookDAO;
import neustlibrarysystem.dao.BorrowDAO;
import neustlibrarysystem.dao.MemberDAO;
import neustlibrarysystem.model.Book;
import neustlibrarysystem.model.BorrowRequest;
import neustlibrarysystem.model.Librarian;
import neustlibrarysystem.model.Member;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Panel for accepting/rejecting pending borrow requests submitted by members.
 * Shows a list of all pending requests with member info, book info, and action buttons.
 */
public class AcceptBorrowRequestPanel extends JPanel {

    // ── Colors (dark-green theme matching LibrarianDashboard) ─────────────────
    private static final Color CLR_DARK_BG   = new Color(0x1a2e1a);
    private static final Color CLR_CARD_BG   = new Color(0x1e331e);
    private static final Color CLR_ACCENT    = new Color(0x9ab55d);
    private static final Color CLR_PRIMARY   = new Color(0x183b06);
    private static final Color CLR_HEADER_BG = new Color(0x0a1a10); // matches LibrarianDashboard dark header
    private static final Color CLR_GREEN     = new Color(0x4caf50);
    private static final Color CLR_RED       = new Color(0xf44336);
    private static final Color CLR_YELLOW    = new Color(0xffc107);
    private static final Color CLR_CYAN      = new Color(0x00bcd4);
    private static final Color CLR_ROW_ODD   = new Color(0xf5fbee);
    private static final Color CLR_ROW_EVEN  = new Color(0xeaf7d7);

    private static final String[] COLUMNS = {
        "Request ID", "Member Name", "Member ID", "Book Title", "ISBN",
        "Request Date", "Preferred Pickup", "Status"
    };

    private final Librarian         librarian;
    private final BorrowDAO         borrowDAO  = new BorrowDAO();
    private final BookDAO           bookDAO    = new BookDAO();
    private final MemberDAO         memberDAO  = new MemberDAO();

    private DefaultTableModel tableModel;
    private JTable            table;
    private JLabel            pendingCountLbl;
    private JTextField        searchField;
    private JComboBox<String> statusFilter;
    private List<BorrowRequest> currentRequests;

    // ── Constructor ───────────────────────────────────────────────────────────
    public AcceptBorrowRequestPanel(Librarian librarian) {
        this.librarian = librarian;
        setLayout(new BorderLayout(0, 16));
        setBackground(CLR_DARK_BG);
        setBorder(new EmptyBorder(0, 0, 0, 0));

        add(buildHeader(),       BorderLayout.NORTH);
        add(buildTableSection(), BorderLayout.CENTER);
        add(buildActionBar(),    BorderLayout.SOUTH);

        refresh();
    }

    // ── Header ────────────────────────────────────────────────────────────────
    private JPanel buildHeader() {
        JPanel header = new JPanel(new BorderLayout(16, 0));
        header.setBackground(CLR_DARK_BG);
        header.setBorder(new EmptyBorder(0, 0, 4, 0));

        // Left: title + subtitle
        JPanel titlePanel = new JPanel();
        titlePanel.setBackground(CLR_DARK_BG);
        titlePanel.setLayout(new BoxLayout(titlePanel, BoxLayout.Y_AXIS));

        JLabel title = new JLabel("Borrow Request Approvals");
        title.setFont(new Font("Segoe UI", Font.BOLD, 24));
        title.setForeground(Color.WHITE);

        JLabel subtitle = new JLabel("Review and accept or reject member borrow requests");
        subtitle.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        subtitle.setForeground(new Color(0xaaaaaa));

        titlePanel.add(title);
        titlePanel.add(Box.createVerticalStrut(4));
        titlePanel.add(subtitle);

        // Right: stat pill + refresh button
        JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        rightPanel.setBackground(CLR_DARK_BG);

        pendingCountLbl = new JLabel("● 0 Pending");
        pendingCountLbl.setFont(new Font("Segoe UI", Font.BOLD, 13));
        pendingCountLbl.setForeground(CLR_YELLOW);
        pendingCountLbl.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(CLR_YELLOW, 1),
            new EmptyBorder(6, 14, 6, 14)
        ));

        JButton refreshBtn = new JButton("  Refresh");
        refreshBtn.setIcon(LibrarianDashboard.makeIcon(LibrarianDashboard.IconType.REFRESH, Color.WHITE, 15));
        refreshBtn.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        refreshBtn.setBackground(CLR_CYAN);
        refreshBtn.setForeground(Color.WHITE);
        refreshBtn.setFocusPainted(false);
        refreshBtn.setBorderPainted(false);
        refreshBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        refreshBtn.setBorder(new EmptyBorder(8, 16, 8, 16));
        refreshBtn.setIconTextGap(6);
        refreshBtn.addActionListener(e -> refresh());

        rightPanel.add(pendingCountLbl);
        rightPanel.add(refreshBtn);

        header.add(titlePanel,  BorderLayout.WEST);
        header.add(rightPanel,  BorderLayout.EAST);
        return header;
    }

    // ── Search & filter bar ───────────────────────────────────────────────────
    private JPanel buildFilterBar() {
        JPanel bar = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        bar.setBackground(CLR_CARD_BG);
        bar.setBorder(new EmptyBorder(12, 16, 12, 16));

        JLabel searchLbl = new JLabel("Search:");
        searchLbl.setFont(new Font("Segoe UI", Font.BOLD, 12));
        searchLbl.setForeground(CLR_ACCENT);

        searchField = new JTextField(22);
        searchField.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        searchField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(0x3a5a3a), 1),
            new EmptyBorder(6, 10, 6, 10)
        ));
        searchField.setBackground(new Color(0x243d24));
        searchField.setForeground(Color.WHITE);
        searchField.setCaretColor(CLR_ACCENT);
        searchField.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e)  { filterTable(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e)  { filterTable(); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) { filterTable(); }
        });

        JLabel filterLbl = new JLabel("Status:");
        filterLbl.setFont(new Font("Segoe UI", Font.BOLD, 12));
        filterLbl.setForeground(CLR_ACCENT);

        statusFilter = new JComboBox<>(new String[]{"All", "PENDING", "ACCEPTED", "REJECTED"});
        statusFilter.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        statusFilter.setBackground(new Color(0x243d24));
        statusFilter.setForeground(Color.WHITE);
        statusFilter.setBorder(BorderFactory.createLineBorder(new Color(0x3a5a3a), 1));
        statusFilter.addActionListener(e -> filterTable());

        bar.add(searchLbl);
        bar.add(searchField);
        bar.add(Box.createHorizontalStrut(12));
        bar.add(filterLbl);
        bar.add(statusFilter);

        return bar;
    }

    // ── Table section ─────────────────────────────────────────────────────────
    private JPanel buildTableSection() {
        JPanel section = new JPanel(new BorderLayout(0, 0));
        section.setBackground(CLR_CARD_BG);
        section.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(0x2e4d2e), 1),
            new EmptyBorder(0, 0, 0, 0)
        ));

        section.add(buildFilterBar(), BorderLayout.NORTH);

        tableModel = new DefaultTableModel(COLUMNS, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };

        table = new JTable(tableModel);

        // ── Apply shared theme first ──────────────────────────────────────────
        LibrarianDashboard.styleTable(table);

        // ── Override header renderer to show WHITE text on dark background ────
        // (styleTable() sets CLR_ACCENT green text; here we want plain white)
        LibrarianDashboard.applyHeaderRenderer(
            table,
            CLR_HEADER_BG,                              // background: #0a1a10
            Color.WHITE,                                // foreground: white
            new Font("Segoe UI", Font.BOLD, 13)         // font
        );

        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        // Column widths
        int[] widths = {80, 150, 90, 200, 120, 110, 120, 90};
        for (int i = 0; i < widths.length; i++)
            table.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);

        // Double-click to open detail dialog
        table.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2 && table.getSelectedRow() != -1)
                    openDetailDialog(table.getSelectedRow());
            }
        });

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBackground(CLR_CARD_BG);
        scroll.getViewport().setBackground(Color.WHITE);
        scroll.setBorder(BorderFactory.createEmptyBorder());

        section.add(scroll, BorderLayout.CENTER);
        return section;
    }

    // ── Bottom action bar ─────────────────────────────────────────────────────
    private JPanel buildActionBar() {
        JPanel bar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        bar.setBackground(CLR_DARK_BG);
        bar.setBorder(new EmptyBorder(8, 0, 0, 0));

        JButton viewBtn = LibrarianDashboard.accentBtn("View Details");
        viewBtn.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row == -1) { JOptionPane.showMessageDialog(this, "Please select a request first."); return; }
            openDetailDialog(row);
        });

        JButton acceptBtn = LibrarianDashboard.primaryBtn("✔ Accept");
        acceptBtn.setBackground(CLR_GREEN);
        acceptBtn.addActionListener(e -> processSelected("ACCEPTED"));

        JButton rejectBtn = LibrarianDashboard.dangerBtn("✘ Reject");
        rejectBtn.addActionListener(e -> processSelected("REJECTED"));

        bar.add(viewBtn);
        bar.add(acceptBtn);
        bar.add(rejectBtn);
        return bar;
    }

    // ── Data loading ──────────────────────────────────────────────────────────
    public void refresh() {
        new SwingWorker<List<BorrowRequest>, Void>() {
            @Override
            protected List<BorrowRequest> doInBackground() {
                try {
                    return borrowDAO.getAllBorrowRequests();
                } catch (Exception ex) {
                    ex.printStackTrace();
                    return List.of();
                }
            }
            @Override
            protected void done() {
                try {
                    currentRequests = get();
                    populateTable(currentRequests);
                    long pending = currentRequests.stream()
                        .filter(r -> "PENDING".equalsIgnoreCase(r.getStatus()))
                        .count();
                    pendingCountLbl.setText("● " + pending + " Pending");
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        }.execute();
    }

    private void populateTable(List<BorrowRequest> requests) {
        tableModel.setRowCount(0);
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("MMM dd, yyyy");
        for (BorrowRequest req : requests) {
            tableModel.addRow(new Object[]{
                req.getRequestId(),
                req.getMemberName(),
                req.getMemberId(),
                req.getBookTitle(),
                req.getIsbn(),
                req.getRequestDate()     != null ? req.getRequestDate().format(fmt)     : "—",
                req.getPreferredPickup() != null ? req.getPreferredPickup().format(fmt) : "—",
                req.getStatus()          != null ? req.getStatus().toUpperCase()        : "PENDING"
            });
        }
    }

    private void filterTable() {
        if (currentRequests == null) return;
        String query  = searchField.getText().trim().toLowerCase();
        String status = (String) statusFilter.getSelectedItem();

        List<BorrowRequest> filtered = currentRequests.stream()
            .filter(r -> {
                boolean matchSearch = query.isEmpty()
                    || (r.getMemberName() != null && r.getMemberName().toLowerCase().contains(query))
                    || (r.getBookTitle()  != null && r.getBookTitle().toLowerCase().contains(query))
                    || (r.getIsbn()       != null && r.getIsbn().toLowerCase().contains(query))
                    || String.valueOf(r.getRequestId()).contains(query);
                boolean matchStatus = "All".equals(status)
                    || (r.getStatus() != null && r.getStatus().equalsIgnoreCase(status));
                return matchSearch && matchStatus;
            })
            .toList();

        populateTable(filtered);
    }

    // ── Process accept/reject ─────────────────────────────────────────────────
    private void processSelected(String action) {
        int row = table.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Please select a request first.",
                "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int    requestId = (int)    tableModel.getValueAt(row, 0);
        String member    = (String) tableModel.getValueAt(row, 1);
        String title     = (String) tableModel.getValueAt(row, 3);
        String current   = (String) tableModel.getValueAt(row, 7);

        if (!"PENDING".equalsIgnoreCase(current)) {
            JOptionPane.showMessageDialog(this,
                "This request has already been " + current + ".",
                "Already Processed", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        String verb    = "ACCEPTED".equals(action) ? "accept" : "reject";
        int    confirm = JOptionPane.showConfirmDialog(this,
            "<html>Are you sure you want to <b>" + verb + "</b> the borrow request?<br><br>"
            + "<b>Member:</b> " + member + "<br>"
            + "<b>Book:</b> "   + title  + "</html>",
            "Confirm " + ("ACCEPTED".equals(action) ? "Accept" : "Reject"),
            JOptionPane.YES_NO_OPTION,
            JOptionPane.QUESTION_MESSAGE);

        if (confirm != JOptionPane.YES_OPTION) return;

        // If accepting, ask for loan duration
        int loanDays = 7;
        if ("ACCEPTED".equals(action)) {
            String[] options = {"7 days", "14 days", "21 days", "30 days"};
            int choice = JOptionPane.showOptionDialog(this,
                "Select the loan period for this borrow:",
                "Loan Duration",
                JOptionPane.DEFAULT_OPTION, JOptionPane.QUESTION_MESSAGE,
                null, options, options[0]);
            if (choice == JOptionPane.CLOSED_OPTION) return;
            loanDays = (choice + 1) * 7;
        }

        final int finalLoanDays = loanDays;
        new SwingWorker<Boolean, Void>() {
            @Override
            protected Boolean doInBackground() {
                try {
                    if ("ACCEPTED".equals(action)) {
                        LocalDate dueDate = LocalDate.now().plusDays(finalLoanDays);
                        borrowDAO.acceptBorrowRequest(requestId, librarian.getLibrarianID(), dueDate);
                    } else {
                        borrowDAO.rejectBorrowRequest(requestId, librarian.getLibrarianID());
                    }
                    return true;
                } catch (Exception ex) {
                    ex.printStackTrace();
                    return false;
                }
            }
            @Override
            protected void done() {
                try {
                    boolean ok = get();
                    if (ok) {
                        String msg = "ACCEPTED".equals(action)
                            ? "✔ Borrow request accepted! Loan period: " + finalLoanDays + " days."
                            : "✘ Borrow request rejected.";
                        JOptionPane.showMessageDialog(AcceptBorrowRequestPanel.this,
                            msg, "Done", JOptionPane.INFORMATION_MESSAGE);
                        refresh();
                    } else {
                        JOptionPane.showMessageDialog(AcceptBorrowRequestPanel.this,
                            "Failed to process the request. Please try again.",
                            "Error", JOptionPane.ERROR_MESSAGE);
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        }.execute();
    }

    // ── Detail dialog ─────────────────────────────────────────────────────────
    private void openDetailDialog(int row) {
        int requestId = (int) tableModel.getValueAt(row, 0);

        BorrowRequest req = currentRequests == null ? null :
            currentRequests.stream()
                .filter(r -> r.getRequestId() == requestId)
                .findFirst().orElse(null);

        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this),
            "Borrow Request Details", true);
        dialog.setSize(480, 480);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new BorderLayout());

        JPanel content = new JPanel();
        content.setBackground(CLR_CARD_BG);
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setBorder(new EmptyBorder(24, 28, 16, 28));

        // Title
        JLabel header = new JLabel("Request #" + requestId);
        header.setFont(new Font("Segoe UI", Font.BOLD, 20));
        header.setForeground(CLR_ACCENT);
        header.setAlignmentX(Component.LEFT_ALIGNMENT);
        content.add(header);
        content.add(Box.createVerticalStrut(16));

        if (req != null) {
            addDetailRow(content, "Member Name",      req.getMemberName());
            addDetailRow(content, "Member ID",        String.valueOf(req.getMemberId()));
            addDetailRow(content, "Book Title",       req.getBookTitle());
            addDetailRow(content, "ISBN",             req.getIsbn());
            addDetailRow(content, "Request Date",     req.getRequestDate() != null
                ? req.getRequestDate().format(DateTimeFormatter.ofPattern("MMM dd, yyyy")) : "—");
            addDetailRow(content, "Preferred Pickup", req.getPreferredPickup() != null
                ? req.getPreferredPickup().format(DateTimeFormatter.ofPattern("MMM dd, yyyy")) : "—");
            addDetailRow(content, "Status",           req.getStatus() != null ? req.getStatus() : "PENDING");
            if (req.getNotes() != null && !req.getNotes().isBlank())
                addDetailRow(content, "Notes", req.getNotes());
        }

        content.add(Box.createVerticalStrut(20));

        // Buttons
        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        btnRow.setBackground(CLR_CARD_BG);
        btnRow.setAlignmentX(Component.LEFT_ALIGNMENT);

        String status = req != null && req.getStatus() != null ? req.getStatus().toUpperCase() : "PENDING";
        if ("PENDING".equals(status)) {
            JButton acceptBtn = LibrarianDashboard.primaryBtn("✔ Accept");
            acceptBtn.setBackground(CLR_GREEN);
            acceptBtn.addActionListener(e -> {
                dialog.dispose();
                table.setRowSelectionInterval(row, row);
                processSelected("ACCEPTED");
            });

            JButton rejectBtn = LibrarianDashboard.dangerBtn("✘ Reject");
            rejectBtn.addActionListener(e -> {
                dialog.dispose();
                table.setRowSelectionInterval(row, row);
                processSelected("REJECTED");
            });

            btnRow.add(acceptBtn);
            btnRow.add(rejectBtn);
        }

        JButton closeBtn = LibrarianDashboard.accentBtn("Close");
        closeBtn.addActionListener(e -> dialog.dispose());
        btnRow.add(closeBtn);

        content.add(btnRow);

        dialog.add(content, BorderLayout.CENTER);
        dialog.getContentPane().setBackground(CLR_CARD_BG);
        dialog.setVisible(true);
    }

    private void addDetailRow(JPanel panel, String label, String value) {
        JPanel row = new JPanel(new BorderLayout(12, 0));
        row.setBackground(CLR_CARD_BG);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 32));
        row.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel lbl = new JLabel(label + ":");
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lbl.setForeground(CLR_ACCENT);
        lbl.setPreferredSize(new Dimension(130, 24));

        JLabel val = new JLabel(value != null ? value : "—");
        val.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        val.setForeground(Color.WHITE);

        row.add(lbl, BorderLayout.WEST);
        row.add(val, BorderLayout.CENTER);
        panel.add(row);
        panel.add(Box.createVerticalStrut(6));
    }
}