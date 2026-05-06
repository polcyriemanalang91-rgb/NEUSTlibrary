package neustlibrarysystem.view.common;

import neustlibrarysystem.dao.ReservationDAO;
import neustlibrarysystem.model.Member;
import neustlibrarysystem.model.Reservation;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class ReservationPanel extends JPanel {

    private final Member currentMember;
    private JTable reservationTable;
    private DefaultTableModel tableModel;
    private final ReservationDAO reservationDAO = new ReservationDAO();
    private JButton refreshBtn, cancelBtn;
    private static final DateTimeFormatter DTF = DateTimeFormatter.ofPattern("MM/dd/yyyy HH:mm");

    public ReservationPanel(Member currentMember) {
        this.currentMember = currentMember;
        initComponents();
        loadReservations();
    }

    private void initComponents() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        JLabel title = new JLabel("My Reservations");
        title.setFont(new Font("Segoe UI", Font.BOLD, 20));
        title.setForeground(new Color(0x1B4F72));

        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.add(title, BorderLayout.NORTH);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        refreshBtn = new JButton("Refresh");
        refreshBtn.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        refreshBtn.addActionListener(e -> loadReservations());
        btnPanel.add(refreshBtn);
        topPanel.add(btnPanel, BorderLayout.CENTER);
        add(topPanel, BorderLayout.NORTH);

        String[] cols = {"Res. ID", "Book Title", "Reserved Date", "Expiry Date", "Status", "Notes"};
        tableModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        reservationTable = new JTable(tableModel);
        reservationTable.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        reservationTable.setRowHeight(26);
        reservationTable.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 13));
        reservationTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        reservationTable.getColumnModel().getColumn(0).setMaxWidth(70);
        reservationTable.getColumnModel().getColumn(4).setMaxWidth(100);
        add(new JScrollPane(reservationTable), BorderLayout.CENTER);

        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        cancelBtn = new JButton("Cancel Reservation");
        cancelBtn.setBackground(new Color(0xC0392B));
        cancelBtn.setForeground(Color.WHITE);
        cancelBtn.setFocusPainted(false);
        cancelBtn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        cancelBtn.addActionListener(e -> cancelSelectedReservation());
        actionPanel.add(cancelBtn);
        add(actionPanel, BorderLayout.SOUTH);
    }

    // ── SWINGWORKER: loadReservations ─────────────────────────────────────────
    private void loadReservations() {
        refreshBtn.setEnabled(false);
        cancelBtn .setEnabled(false);
        tableModel.setRowCount(0);

        new SwingWorker<List<Reservation>, Void>() {
            @Override
            protected List<Reservation> doInBackground() {
                return reservationDAO.getMemberReservations(currentMember.getMemberID());
            }

            @Override
            protected void done() {
                try {
                    for (Reservation r : get()) {
                        tableModel.addRow(new Object[]{
                            r.getReservationID(),
                            r.getBookTitle()    != null ? r.getBookTitle()                : "—",
                            r.getReservedDate() != null ? r.getReservedDate().format(DTF) : "—",
                            r.getExpiryDate()   != null ? r.getExpiryDate().format(DTF)   : "—",
                            r.getStatus()       != null ? r.getStatus()                   : "—",
                            r.getNotes()        != null ? r.getNotes()                    : ""
                        });
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    JOptionPane.showMessageDialog(ReservationPanel.this,
                        "Error loading reservations.", "Error", JOptionPane.ERROR_MESSAGE);
                } finally {
                    refreshBtn.setEnabled(true);
                    cancelBtn .setEnabled(true);
                }
            }
        }.execute();
    }

    // ── SWINGWORKER: cancelSelectedReservation ────────────────────────────────
    private void cancelSelectedReservation() {
        int row = reservationTable.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Please select a reservation to cancel.",
                "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }
        String status = (String) tableModel.getValueAt(row, 4);
        if (!"Pending".equals(status) && !"Confirmed".equals(status)) {
            JOptionPane.showMessageDialog(this, "Only Pending or Confirmed reservations can be cancelled.",
                "Cannot Cancel", JOptionPane.WARNING_MESSAGE);
            return;
        }
        int resID = (int) tableModel.getValueAt(row, 0);
        if (JOptionPane.showConfirmDialog(this, "Cancel this reservation?", "Confirm",
                JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION) return;

        refreshBtn.setEnabled(false);
        cancelBtn .setEnabled(false);

        new SwingWorker<Boolean, Void>() {
            @Override
            protected Boolean doInBackground() {
                return reservationDAO.cancelReservation(resID);
            }

            @Override
            protected void done() {
                try {
                    if (get()) {
                        JOptionPane.showMessageDialog(ReservationPanel.this,
                            "Reservation cancelled.", "Success", JOptionPane.INFORMATION_MESSAGE);
                        loadReservations();
                    } else {
                        JOptionPane.showMessageDialog(ReservationPanel.this,
                            "Failed to cancel reservation.", "Error", JOptionPane.ERROR_MESSAGE);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    JOptionPane.showMessageDialog(ReservationPanel.this,
                        "Error: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                } finally {
                    refreshBtn.setEnabled(true);
                    cancelBtn .setEnabled(true);
                }
            }
        }.execute();
    }
}