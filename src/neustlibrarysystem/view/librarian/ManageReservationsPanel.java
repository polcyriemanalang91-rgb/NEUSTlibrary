package neustlibrarysystem.view.librarian;

import neustlibrarysystem.dao.ReservationDAO;
import neustlibrarysystem.model.Librarian;
import neustlibrarysystem.model.Reservation;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class ManageReservationsPanel extends JPanel {

    private final ReservationDAO resDAO;
    private final Librarian      librarian;
    private DefaultTableModel    tableModel;
    private JTable               table;
    private JButton              btnRefresh, btnCancel, btnConfirm;

    public ManageReservationsPanel(Librarian librarian) {
        this.librarian = librarian;
        this.resDAO    = new ReservationDAO();
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

        JLabel hdr = new JLabel("📌   Manage Reservations");
        hdr.setFont(LibrarianDashboard.FONT_HEADER);
        hdr.setForeground(LibrarianDashboard.CLR_PRIMARY);
        header.add(hdr, BorderLayout.WEST);

        JLabel hint = new JLabel("Select a reservation then Confirm or Cancel");
        hint.setFont(new Font("Segoe UI", Font.ITALIC, 12));
        hint.setForeground(new Color(0x485f48));
        header.add(hint, BorderLayout.EAST);
        add(header, BorderLayout.NORTH);

        // ── Table ─────────────────────────────────────────────────────────────
        String[] cols = {"Res. ID", "Member", "Student ID", "Book Title",
                         "Reserved Date", "Expiry Date", "Status"};
        tableModel = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        table = new JTable(tableModel);
        LibrarianDashboard.styleTable(table);

        // FIX: Custom header renderer — guaranteed black text, cannot be overridden
        table.getTableHeader().setPreferredSize(new Dimension(0, 36));
        table.getTableHeader().setDefaultRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object val,
                    boolean sel, boolean foc, int r, int c) {
                JLabel lbl = new JLabel(val == null ? "" : val.toString());
                lbl.setFont(LibrarianDashboard.FONT_LABEL);
                lbl.setForeground(Color.BLACK);
                lbl.setBackground(new Color(0xd6eaa0));
                lbl.setOpaque(true);
                lbl.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createMatteBorder(0, 0, 2, 1, new Color(0x8aab3c)),
                    BorderFactory.createEmptyBorder(4, 8, 4, 8)
                ));
                lbl.setHorizontalAlignment(SwingConstants.LEFT);
                return lbl;
            }
        });

        // FIX: setOpaque(true) so row colors actually paint
        table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object val,
                    boolean sel, boolean foc, int r, int c) {
                Component comp = super.getTableCellRendererComponent(t, val, sel, foc, r, c);
                ((JComponent) comp).setOpaque(true);
                if (!sel) {
                    String status = String.valueOf(tableModel.getValueAt(r, 6));
                    switch (status) {
                        case "Confirmed":
                            comp.setBackground(new Color(0xe8f8e8));
                            comp.setForeground(new Color(0x1a6b1a));
                            break;
                        case "Cancelled":
                        case "Expired":
                            comp.setBackground(new Color(0xfde8e8));
                            comp.setForeground(new Color(0xaaaaaa));
                            break;
                        default:
                            comp.setBackground(r % 2 == 0 ? Color.WHITE : new Color(0xf4fae8));
                            comp.setForeground(new Color(0x303c1b));
                    }
                }
                return comp;
            }
        });

        table.getColumnModel().getColumn(0).setMaxWidth(70);
        table.getColumnModel().getColumn(2).setMaxWidth(110);
        table.getColumnModel().getColumn(6).setMaxWidth(100);

        // FIX: Ensure rows are tall enough and grid is visible
        table.setRowHeight(28);
        table.setShowGrid(true);
        table.setGridColor(new Color(0xd4e6a0));

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(BorderFactory.createLineBorder(LibrarianDashboard.CLR_ACCENT, 1));
        scroll.getViewport().setBackground(Color.WHITE);
        add(scroll, BorderLayout.CENTER);

        // ── Action bar ────────────────────────────────────────────────────────
        JPanel south = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 10));
        south.setOpaque(false);

        btnRefresh = LibrarianDashboard.accentBtn("🔄  Refresh");
        btnCancel  = LibrarianDashboard.dangerBtn("❌  Cancel");
        btnConfirm = LibrarianDashboard.primaryBtn("✅  Confirm");

        btnRefresh.addActionListener(e -> refresh());
        btnCancel .addActionListener(e -> updateStatus("Cancelled"));
        btnConfirm.addActionListener(e -> updateStatus("Confirmed"));

        south.add(btnRefresh);
        south.add(btnCancel);
        south.add(btnConfirm);
        add(south, BorderLayout.SOUTH);
    }

    // ── SWINGWORKER: refresh ──────────────────────────────────────────────────
    public void refresh() {
        setButtonsEnabled(false);
        tableModel.setRowCount(0);

        new SwingWorker<List<Reservation>, Void>() {
            @Override
            protected List<Reservation> doInBackground() {
                return resDAO.getPendingReservations();
            }

            @Override
            protected void done() {
                try {
                    for (Reservation r : get()) {
                        tableModel.addRow(new Object[]{
                            r.getReservationID(), r.getMemberName(), r.getStudentID(),
                            r.getBookTitle(),     r.getReservedDate(), r.getExpiryDate(),
                            r.getStatus()
                        });
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    JOptionPane.showMessageDialog(ManageReservationsPanel.this,
                        "Error loading reservations.", "Error", JOptionPane.ERROR_MESSAGE);
                } finally {
                    setButtonsEnabled(true);
                }
            }
        }.execute();
    }

    // ── SWINGWORKER: updateStatus ─────────────────────────────────────────────
    private void updateStatus(String status) {
        int row = table.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Select a reservation first.",
                "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String current = (String) tableModel.getValueAt(row, 6);
        if ("Cancelled".equals(current) || "Expired".equals(current) || "Completed".equals(current)) {
            JOptionPane.showMessageDialog(this,
                "This reservation is already " + current + " and cannot be changed.",
                "Not Allowed", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int    resID      = (int)    tableModel.getValueAt(row, 0);
        String memberName = (String) tableModel.getValueAt(row, 1);
        String bookTitle  = (String) tableModel.getValueAt(row, 3);
        String action     = "Confirmed".equals(status) ? "confirm" : "cancel";

        if (JOptionPane.showConfirmDialog(this,
                "Are you sure you want to " + action + " this reservation?\n\n" +
                "Member : " + memberName + "\n" +
                "Book   : " + bookTitle,
                "Confirm Action", JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE) != JOptionPane.YES_OPTION) return;

        setButtonsEnabled(false);

        new SwingWorker<Boolean, Void>() {
            @Override
            protected Boolean doInBackground() {
                return resDAO.updateStatus(resID, status, librarian.getLibrarianID());
            }

            @Override
            protected void done() {
                try {
                    if (get()) {
                        JOptionPane.showMessageDialog(ManageReservationsPanel.this,
                            "✔  Reservation " + status.toLowerCase() + " successfully.",
                            "Success", JOptionPane.INFORMATION_MESSAGE);
                        refresh();
                    } else {
                        JOptionPane.showMessageDialog(ManageReservationsPanel.this,
                            "Operation failed.", "Error", JOptionPane.ERROR_MESSAGE);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    JOptionPane.showMessageDialog(ManageReservationsPanel.this,
                        "Error: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                } finally {
                    setButtonsEnabled(true);
                }
            }
        }.execute();
    }

    private void setButtonsEnabled(boolean enabled) {
        btnRefresh.setEnabled(enabled);
        btnCancel .setEnabled(enabled);
        btnConfirm.setEnabled(enabled);
    }
}