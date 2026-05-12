package neustlibrarysystem.view.librarian;

import neustlibrarysystem.dao.MemberDAO;
import neustlibrarysystem.model.Member;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.List;

public class ManageMembersPanel extends JPanel {

    private final MemberDAO   memberDAO;
    private DefaultTableModel tableModel;
    private JTable            table;
    private JTextField        tfSearch;
    private JButton           btnSearch, btnRefresh, btnView, btnToggle;
    private TableRowSorter<DefaultTableModel> sorter;

    public ManageMembersPanel() {
        this.memberDAO = new MemberDAO();
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

        JLabel hdr = new JLabel("👥   Manage Members");
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
        tfSearch.setToolTipText("Search by Member ID, Student ID, Name, Email, or Course");
        tfSearch.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(LibrarianDashboard.CLR_ACCENT, 1, true),
            new EmptyBorder(5, 10, 5, 10)
        ));

        // Press Enter to search
        tfSearch.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) applySearch();
            }
        });

        btnSearch  = LibrarianDashboard.primaryBtn("🔍  Search");
        btnRefresh = LibrarianDashboard.accentBtn("🔄  Refresh");
        btnSearch .addActionListener(e -> applySearch());
        btnRefresh.addActionListener(e -> { tfSearch.setText(""); refresh(); });

        bar.add(searchLbl); bar.add(tfSearch);
        bar.add(btnSearch); bar.add(btnRefresh);
        header.add(bar, BorderLayout.EAST);
        add(header, BorderLayout.NORTH);

        // ── Table ─────────────────────────────────────────────────────────────
        String[] cols = {"Member ID", "Student ID", "Full Name", "Email",
                         "Course", "Year Level", "Status"};
        tableModel = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        table = new JTable(tableModel);
        LibrarianDashboard.styleTable(table);

        // Row sorter enables client-side filtering (no extra DAO call needed)
        sorter = new TableRowSorter<>(tableModel);
        table.setRowSorter(sorter);

        // Custom header renderer
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

        table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object val,
                    boolean sel, boolean foc, int r, int c) {
                Component comp = super.getTableCellRendererComponent(t, val, sel, foc, r, c);
                ((JComponent) comp).setOpaque(true);
                if (!sel) {
                    // Convert view row → model row (important when filter is active)
                    int modelRow = table.convertRowIndexToModel(r);
                    String status = String.valueOf(tableModel.getValueAt(modelRow, 6));
                    if ("Inactive".equals(status)) {
                        comp.setBackground(new Color(0xf5e6e6));
                        comp.setForeground(new Color(0xaaaaaa));
                    } else {
                        comp.setBackground(r % 2 == 0 ? Color.WHITE : new Color(0xf4fae8));
                        comp.setForeground(new Color(0x303c1b));
                    }
                }
                return comp;
            }
        });

        table.getColumnModel().getColumn(0).setMaxWidth(80);
        table.getColumnModel().getColumn(1).setMaxWidth(110);
        table.getColumnModel().getColumn(5).setMaxWidth(90);
        table.getColumnModel().getColumn(6).setMaxWidth(80);

        table.setRowHeight(28);
        table.setShowGrid(true);
        table.setGridColor(new Color(0xd4e6a0));

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(BorderFactory.createLineBorder(LibrarianDashboard.CLR_ACCENT, 1));
        scroll.getViewport().setBackground(Color.WHITE);
        add(scroll, BorderLayout.CENTER);

        // ── Action bar ────────────────────────────────────────────────────────
        JPanel south = new JPanel(new BorderLayout());
        south.setOpaque(false);
        south.setBorder(new EmptyBorder(10, 0, 0, 0));

        JPanel btns = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        btns.setOpaque(false);

        btnView   = LibrarianDashboard.accentBtn("👁  View Details");
        btnToggle = LibrarianDashboard.primaryBtn("🔄  Toggle Active/Inactive");
        btnView  .addActionListener(e -> viewMemberDetails());
        btnToggle.addActionListener(e -> toggleMember());

        btns.add(btnView);
        btns.add(btnToggle);
        south.add(btns, BorderLayout.EAST);
        add(south, BorderLayout.SOUTH);
    }

    // ── Search / Filter logic ─────────────────────────────────────────────────
    private void applySearch() {
        String kw = tfSearch.getText().trim();
        if (kw.isEmpty()) {
            sorter.setRowFilter(null);
            return;
        }
        // Search across: Member ID (0), Student ID (1), Full Name (2),
        //                Email (3), Course (4)
        sorter.setRowFilter(RowFilter.regexFilter("(?i)" + kw, 0, 1, 2, 3, 4));
    }

    // ── SWINGWORKER: refresh ──────────────────────────────────────────────────
    public void refresh() {
        setButtonsEnabled(false);
        tableModel.setRowCount(0);

        new SwingWorker<List<Member>, Void>() {
            @Override
            protected List<Member> doInBackground() {
                return memberDAO.getAllMembers();
            }

            @Override
            protected void done() {
                try {
                    for (Member m : get()) {
                        tableModel.addRow(new Object[]{
                            m.getMemberID(),    m.getStudentID(), m.getFullName(),
                            m.getEmail(),       m.getCourseProgram(), m.getYearLevel(),
                            m.isActive() ? "Active" : "Inactive"
                        });
                    }
                    // Re-apply active search after refresh
                    applySearch();
                } catch (Exception e) {
                    e.printStackTrace();
                    JOptionPane.showMessageDialog(ManageMembersPanel.this,
                        "Error loading members.", "Error", JOptionPane.ERROR_MESSAGE);
                } finally {
                    setButtonsEnabled(true);
                }
            }
        }.execute();
    }

    // ── FIX: getModelRow — always convert view → model safely ────────────────
    private int getModelRow() {
        int viewRow = table.getSelectedRow();
        if (viewRow < 0) return -1;
        return table.convertRowIndexToModel(viewRow);
    }

    private void viewMemberDetails() {
        int row = getModelRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Select a member to view.",
                "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }
        String info = String.format(
            "Member ID  : %s%n" +
            "Student ID : %s%n" +
            "Full Name  : %s%n" +
            "Email      : %s%n" +
            "Course     : %s%n" +
            "Year Level : %s%n" +
            "Status     : %s",
            tableModel.getValueAt(row, 0), tableModel.getValueAt(row, 1),
            tableModel.getValueAt(row, 2), tableModel.getValueAt(row, 3),
            tableModel.getValueAt(row, 4), tableModel.getValueAt(row, 5),
            tableModel.getValueAt(row, 6)
        );
        JOptionPane.showMessageDialog(this, info, "Member Details", JOptionPane.INFORMATION_MESSAGE);
    }

    // ── SWINGWORKER: toggleMember ─────────────────────────────────────────────
    private void toggleMember() {
        int row = getModelRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Select a member first.",
                "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }
        int     id     = (int)    tableModel.getValueAt(row, 0);
        String  name   = (String) tableModel.getValueAt(row, 2);
        String  status = (String) tableModel.getValueAt(row, 6);
        boolean active = "Active".equals(status);
        String  action = active ? "deactivate" : "activate";

        if (JOptionPane.showConfirmDialog(this,
                "Are you sure you want to " + action + " member:\n" + name + "?",
                "Confirm", JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE) != JOptionPane.YES_OPTION) return;

        setButtonsEnabled(false);

        new SwingWorker<Boolean, Void>() {
            @Override
            protected Boolean doInBackground() {
                return memberDAO.setMemberActive(id, !active);
            }

            @Override
            protected void done() {
                try {
                    if (get()) {
                        JOptionPane.showMessageDialog(ManageMembersPanel.this,
                            "✔  Member " + (!active ? "activated" : "deactivated") + " successfully.",
                            "Success", JOptionPane.INFORMATION_MESSAGE);
                        refresh();
                    } else {
                        JOptionPane.showMessageDialog(ManageMembersPanel.this,
                            "Failed.", "Error", JOptionPane.ERROR_MESSAGE);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    JOptionPane.showMessageDialog(ManageMembersPanel.this,
                        "Error: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                } finally {
                    setButtonsEnabled(true);
                }
            }
        }.execute();
    }

    private void setButtonsEnabled(boolean enabled) {
        btnSearch .setEnabled(enabled);
        btnRefresh.setEnabled(enabled);
        btnView   .setEnabled(enabled);
        btnToggle .setEnabled(enabled);
    }
}