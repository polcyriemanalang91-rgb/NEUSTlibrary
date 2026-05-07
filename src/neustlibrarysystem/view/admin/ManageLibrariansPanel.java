package neustlibrarysystem.view.admin;

import neustlibrarysystem.dao.LibrarianDAO;
import neustlibrarysystem.model.Admin;
import neustlibrarysystem.model.Librarian;
import neustlibrarysystem.util.PasswordUtil;
import neustlibrarysystem.util.ValidationUtil;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class ManageLibrariansPanel extends JPanel {

    private final Admin currentAdmin;
    private JTable librarianTable;
    private DefaultTableModel tableModel;
    private JTextField txtEmployeeID, txtFirstName, txtLastName, txtEmail, txtContact;
    private JPasswordField txtPassword;
    private JCheckBox chkActive;
    private JButton btnAdd, btnUpdate, btnDelete, btnClear;
    private final LibrarianDAO librarianDAO = new LibrarianDAO();
    private int selectedLibrarianID = -1;

    // ── Copied color/font constants from ManageMembersPanel / LibrarianDashboard ──
    private static final Color CLR_BG      = new Color(0xf5faed);
    private static final Color CLR_PRIMARY = new Color(0x4a7c10);
    private static final Color CLR_ACCENT  = new Color(0x8aab3c);
    private static final Font  FONT_HEADER = new Font("Segoe UI", Font.BOLD, 22);
    private static final Font  FONT_LABEL  = new Font("Segoe UI", Font.BOLD, 13);
    private static final Font  FONT_BODY   = new Font("Segoe UI", Font.PLAIN, 13);

    public ManageLibrariansPanel(Admin currentAdmin) {
        this.currentAdmin = currentAdmin;
        initComponents();
        loadLibrarians();
    }

    private void initComponents() {
        setLayout(new BorderLayout(0, 0));
        setBackground(CLR_BG);
        setBorder(new EmptyBorder(15, 15, 15, 15));

        // ── Header ────────────────────────────────────────────────────────────
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(CLR_BG);
        header.setBorder(new EmptyBorder(0, 0, 14, 0));

        JLabel title = new JLabel("👤   Manage Librarians");
        title.setFont(FONT_HEADER);
        title.setForeground(CLR_PRIMARY);
        header.add(title, BorderLayout.WEST);
        add(header, BorderLayout.NORTH);

        // ── Table ─────────────────────────────────────────────────────────────
        String[] cols = {"ID", "Employee ID", "First Name", "Last Name", "Email", "Contact", "Active"};
        tableModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        librarianTable = new JTable(tableModel);
        librarianTable.setFont(FONT_BODY);
        librarianTable.setRowHeight(28);
        librarianTable.setShowGrid(true);
        librarianTable.setGridColor(new Color(0xd4e6a0));
        librarianTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        librarianTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) populateForm();
        });

        // ── Custom green header renderer (copied from ManageMembersPanel) ─────
        librarianTable.getTableHeader().setPreferredSize(new Dimension(0, 36));
        librarianTable.getTableHeader().setDefaultRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object val,
                    boolean sel, boolean foc, int r, int c) {
                JLabel lbl = new JLabel(val == null ? "" : val.toString());
                lbl.setFont(FONT_LABEL);
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

        // ── Striped row renderer + inactive gray (copied from ManageMembersPanel) ──
        librarianTable.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object val,
                    boolean sel, boolean foc, int r, int c) {
                Component comp = super.getTableCellRendererComponent(t, val, sel, foc, r, c);
                ((JComponent) comp).setOpaque(true);
                if (!sel) {
                    String active = String.valueOf(tableModel.getValueAt(r, 6));
                    if ("No".equals(active)) {
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

        librarianTable.getColumnModel().getColumn(0).setMaxWidth(50);
        librarianTable.getColumnModel().getColumn(1).setMaxWidth(110);
        librarianTable.getColumnModel().getColumn(6).setMaxWidth(60);

        JScrollPane scroll = new JScrollPane(librarianTable);
        scroll.setBorder(BorderFactory.createLineBorder(CLR_ACCENT, 1));
        scroll.getViewport().setBackground(Color.WHITE);
        add(scroll, BorderLayout.CENTER);

        // ── Form Panel ────────────────────────────────────────────────────────
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBackground(CLR_BG);
        formPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(CLR_ACCENT, 1),
                "Librarian Details",
                javax.swing.border.TitledBorder.LEFT,
                javax.swing.border.TitledBorder.TOP,
                FONT_LABEL, CLR_PRIMARY
            ),
            new EmptyBorder(6, 8, 6, 8)
        ));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets  = new Insets(5, 8, 5, 8);
        gbc.anchor  = GridBagConstraints.WEST;
        gbc.fill    = GridBagConstraints.HORIZONTAL;

        txtEmployeeID = styledField(15);
        txtFirstName  = styledField(20);
        txtLastName   = styledField(20);
        txtEmail      = styledField(25);
        txtContact    = styledField(15);
        txtPassword   = new JPasswordField(20);
        txtPassword.setFont(FONT_BODY);
        txtPassword.setForeground(new Color(0x303c1b));
        txtPassword.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(CLR_ACCENT, 1, true),
            new EmptyBorder(5, 10, 5, 10)
        ));
        chkActive = new JCheckBox("Active", true);
        chkActive.setFont(FONT_BODY);
        chkActive.setBackground(CLR_BG);
        chkActive.setForeground(new Color(0x303c1b));

        Object[][] fields = {
            {"Employee ID: *", txtEmployeeID}, {"First Name: *", txtFirstName},
            {"Last Name: *",   txtLastName},   {"Email: *",      txtEmail},
            {"Contact:",       txtContact},    {"Password:",     txtPassword}
        };

        for (int i = 0; i < fields.length; i++) {
            gbc.gridx = 0; gbc.gridy = i; gbc.weightx = 0;
            JLabel lbl = new JLabel((String) fields[i][0]);
            lbl.setFont(FONT_LABEL);
            lbl.setForeground(new Color(0x303c1b));
            formPanel.add(lbl, gbc);

            gbc.gridx = 1; gbc.weightx = 1;
            formPanel.add((JComponent) fields[i][1], gbc);
        }

        gbc.gridx = 1; gbc.gridy = 6;
        JLabel hint = new JLabel("<html><i>Leave blank to keep existing password on update.</i></html>");
        hint.setFont(new Font("Segoe UI", Font.ITALIC, 11));
        hint.setForeground(Color.GRAY);
        formPanel.add(hint, gbc);

        gbc.gridy = 7;
        formPanel.add(chkActive, gbc);

        // ── Buttons (green theme) ─────────────────────────────────────────────
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 5));
        btnPanel.setOpaque(false);

        btnAdd    = primaryBtn("➕  Add");
        btnUpdate = primaryBtn("✏️  Update");
        btnDelete = accentBtn ("🚫  Deactivate");
        btnClear  = accentBtn ("🗑  Clear");

        btnAdd   .addActionListener(e -> addLibrarian());
        btnUpdate.addActionListener(e -> updateLibrarian());
        btnDelete.addActionListener(e -> deactivateLibrarian());
        btnClear .addActionListener(e -> clearForm());

        btnPanel.add(btnAdd); btnPanel.add(btnUpdate);
        btnPanel.add(btnDelete); btnPanel.add(btnClear);

        gbc.gridx = 0; gbc.gridy = 8; gbc.gridwidth = 2;
        formPanel.add(btnPanel, gbc);

        add(formPanel, BorderLayout.SOUTH);
    }

    // ── Green-themed button helpers ───────────────────────────────────────────
    private JButton primaryBtn(String text) {
        JButton b = new JButton(text);
        b.setFont(FONT_LABEL);
        b.setForeground(Color.WHITE);
        b.setBackground(CLR_PRIMARY);
        b.setFocusPainted(false);
        b.setBorder(new EmptyBorder(7, 18, 7, 18));
        return b;
    }

    private JButton accentBtn(String text) {
        JButton b = new JButton(text);
        b.setFont(FONT_LABEL);
        b.setForeground(new Color(0x303c1b));
        b.setBackground(new Color(0xd6eaa0));
        b.setFocusPainted(false);
        b.setBorder(new EmptyBorder(7, 18, 7, 18));
        return b;
    }

    // ── Styled text field helper ──────────────────────────────────────────────
    private JTextField styledField(int cols) {
        JTextField f = new JTextField(cols);
        f.setFont(FONT_BODY);
        f.setForeground(new Color(0x303c1b));
        f.setBackground(Color.WHITE);
        f.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(CLR_ACCENT, 1, true),
            new EmptyBorder(5, 10, 5, 10)
        ));
        return f;
    }

    private void setButtonsEnabled(boolean enabled) {
        btnAdd.setEnabled(enabled);
        btnUpdate.setEnabled(enabled);
        btnDelete.setEnabled(enabled);
        btnClear.setEnabled(enabled);
    }

    // ─── SwingWorker: loadLibrarians ─────────────────────────────────────────
    private void loadLibrarians() {
        setButtonsEnabled(false);

        new SwingWorker<List<Librarian>, Void>() {
            @Override
            protected List<Librarian> doInBackground() {
                return librarianDAO.getAll();
            }

            @Override
            protected void done() {
                try {
                    tableModel.setRowCount(0);
                    for (Librarian l : get()) {
                        tableModel.addRow(new Object[]{
                            l.getLibrarianID(), l.getEmployeeID(),
                            l.getFirstName(),   l.getLastName(),
                            l.getEmail(),       l.getContactNumber(),
                            l.isActive() ? "Yes" : "No"
                        });
                    }
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(ManageLibrariansPanel.this,
                        "Error loading librarians: " + ex.getMessage(),
                        "Error", JOptionPane.ERROR_MESSAGE);
                } finally {
                    setButtonsEnabled(true);
                }
            }
        }.execute();
    }

    // ─── SwingWorker: addLibrarian ───────────────────────────────────────────
    private void addLibrarian() {
        String empID = txtEmployeeID.getText().trim();
        String fn    = txtFirstName .getText().trim();
        String ln    = txtLastName  .getText().trim();
        String email = txtEmail     .getText().trim();
        String pass  = new String(txtPassword.getPassword());

        if (empID.isEmpty() || fn.isEmpty() || ln.isEmpty() || email.isEmpty() || pass.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                "Employee ID, name, email, and password are required.",
                "Validation", JOptionPane.WARNING_MESSAGE); return;
        }
        if (!ValidationUtil.isValidEmail(email)) {
            JOptionPane.showMessageDialog(this, "Invalid email format.",
                "Validation", JOptionPane.WARNING_MESSAGE); return;
        }

        Librarian l = new Librarian();
        l.setEmployeeID   (empID);
        l.setFirstName    (fn);
        l.setLastName     (ln);
        l.setEmail        (email);
        l.setContactNumber(txtContact.getText().trim());
        l.setActive       (true);

        setButtonsEnabled(false);

        new SwingWorker<Boolean, Void>() {
            @Override protected Boolean doInBackground() { return librarianDAO.add(l, pass); }

            @Override
            protected void done() {
                try {
                    if (get()) {
                        JOptionPane.showMessageDialog(ManageLibrariansPanel.this,
                            "✔  Librarian added successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                        loadLibrarians(); clearForm();
                    } else {
                        JOptionPane.showMessageDialog(ManageLibrariansPanel.this,
                            "Failed to add librarian.", "Error", JOptionPane.ERROR_MESSAGE);
                    }
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(ManageLibrariansPanel.this,
                        "Error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                } finally { setButtonsEnabled(true); }
            }
        }.execute();
    }

    // ─── SwingWorker: updateLibrarian ───────────────────────────────────────
    private void updateLibrarian() {
        if (selectedLibrarianID < 0) {
            JOptionPane.showMessageDialog(this, "Select a librarian first.",
                "Warning", JOptionPane.WARNING_MESSAGE); return;
        }
        String fn    = txtFirstName.getText().trim();
        String ln    = txtLastName .getText().trim();
        String email = txtEmail    .getText().trim();

        if (fn.isEmpty() || ln.isEmpty() || email.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Name and email are required.",
                "Validation", JOptionPane.WARNING_MESSAGE); return;
        }

        int     idToUpdate = selectedLibrarianID;
        String  contact    = txtContact.getText().trim();
        boolean active     = chkActive.isSelected();
        String  pass       = new String(txtPassword.getPassword());

        setButtonsEnabled(false);

        new SwingWorker<Boolean, Void>() {
            @Override
            protected Boolean doInBackground() {
                Librarian l = librarianDAO.getByID(idToUpdate);
                if (l == null) return false;
                l.setFirstName    (fn);
                l.setLastName     (ln);
                l.setEmail        (email);
                l.setContactNumber(contact);
                l.setActive       (active);
                if (!pass.isEmpty()) l.setPasswordHash(PasswordUtil.hashPassword(pass));
                return librarianDAO.update(l);
            }

            @Override
            protected void done() {
                try {
                    Boolean result = get();
                    if (result == null || !result) {
                        JOptionPane.showMessageDialog(ManageLibrariansPanel.this,
                            "Failed to update librarian.", "Error", JOptionPane.ERROR_MESSAGE);
                    } else {
                        JOptionPane.showMessageDialog(ManageLibrariansPanel.this,
                            "✔  Librarian updated successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                        loadLibrarians(); clearForm();
                    }
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(ManageLibrariansPanel.this,
                        "Error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                } finally { setButtonsEnabled(true); }
            }
        }.execute();
    }

    // ─── SwingWorker: deactivateLibrarian ───────────────────────────────────
    private void deactivateLibrarian() {
        if (selectedLibrarianID < 0) {
            JOptionPane.showMessageDialog(this, "Select a librarian first.",
                "Warning", JOptionPane.WARNING_MESSAGE); return;
        }
        if (JOptionPane.showConfirmDialog(this, "Deactivate this librarian?",
                "Confirm", JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE) != JOptionPane.YES_OPTION) return;

        int idToDeactivate = selectedLibrarianID;
        setButtonsEnabled(false);

        new SwingWorker<Boolean, Void>() {
            @Override protected Boolean doInBackground() { return librarianDAO.deactivate(idToDeactivate); }

            @Override
            protected void done() {
                try {
                    if (get()) {
                        JOptionPane.showMessageDialog(ManageLibrariansPanel.this,
                            "✔  Librarian deactivated.", "Success", JOptionPane.INFORMATION_MESSAGE);
                        loadLibrarians(); clearForm();
                    } else {
                        JOptionPane.showMessageDialog(ManageLibrariansPanel.this,
                            "Failed to deactivate.", "Error", JOptionPane.ERROR_MESSAGE);
                    }
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(ManageLibrariansPanel.this,
                        "Error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                } finally { setButtonsEnabled(true); }
            }
        }.execute();
    }

    private void populateForm() {
        int row = librarianTable.getSelectedRow();
        if (row < 0) return;
        selectedLibrarianID = (int) tableModel.getValueAt(row, 0);
        txtEmployeeID.setText(str(tableModel.getValueAt(row, 1)));
        txtFirstName .setText(str(tableModel.getValueAt(row, 2)));
        txtLastName  .setText(str(tableModel.getValueAt(row, 3)));
        txtEmail     .setText(str(tableModel.getValueAt(row, 4)));
        txtContact   .setText(str(tableModel.getValueAt(row, 5)));
        chkActive    .setSelected("Yes".equals(tableModel.getValueAt(row, 6)));
        txtPassword  .setText("");
    }

    private void clearForm() {
        selectedLibrarianID = -1;
        txtEmployeeID.setText(""); txtFirstName.setText(""); txtLastName.setText("");
        txtEmail     .setText(""); txtContact  .setText(""); txtPassword.setText("");
        chkActive.setSelected(true);
        librarianTable.clearSelection();
    }

    private String str(Object o) { return o != null ? o.toString() : ""; }
}