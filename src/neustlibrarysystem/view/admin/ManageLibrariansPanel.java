package neustlibrarysystem.view.admin;

import neustlibrarysystem.dao.LibrarianDAO;
import neustlibrarysystem.model.Admin;
import neustlibrarysystem.model.Librarian;
import neustlibrarysystem.util.PasswordUtil;
import neustlibrarysystem.util.ValidationUtil;

import javax.swing.*;
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

    public ManageLibrariansPanel(Admin currentAdmin) {
        this.currentAdmin = currentAdmin;
        initComponents();
        loadLibrarians();
    }

    private void initComponents() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        JLabel title = new JLabel("Manage Librarians");
        title.setFont(new Font("Segoe UI", Font.BOLD, 20));
        title.setForeground(new Color(0x1B4F72));
        add(title, BorderLayout.NORTH);

        String[] cols = {"ID", "Employee ID", "First Name", "Last Name", "Email", "Contact", "Active"};
        tableModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        librarianTable = new JTable(tableModel);
        librarianTable.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        librarianTable.setRowHeight(26);
        librarianTable.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 13));
        librarianTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        librarianTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) populateForm();
        });
        add(new JScrollPane(librarianTable), BorderLayout.CENTER);

        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBorder(BorderFactory.createTitledBorder("Librarian Details"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 8, 6, 8);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill   = GridBagConstraints.HORIZONTAL;

        txtEmployeeID = new JTextField(15);
        txtFirstName  = new JTextField(20);
        txtLastName   = new JTextField(20);
        txtEmail      = new JTextField(25);
        txtContact    = new JTextField(15);
        txtPassword   = new JPasswordField(20);
        chkActive     = new JCheckBox("Active", true);

        Object[][] fields = {
            {"Employee ID: *", txtEmployeeID}, {"First Name: *", txtFirstName},
            {"Last Name: *",   txtLastName},   {"Email: *",      txtEmail},
            {"Contact:",       txtContact},    {"Password:",     txtPassword}
        };

        for (int i = 0; i < fields.length; i++) {
            gbc.gridx = 0; gbc.gridy = i; gbc.weightx = 0;
            JLabel lbl = new JLabel((String) fields[i][0]);
            lbl.setFont(new Font("Segoe UI", Font.BOLD, 13));
            formPanel.add(lbl, gbc);
            gbc.gridx = 1; gbc.weightx = 1;
            JComponent comp = (JComponent) fields[i][1];
            comp.setFont(new Font("Segoe UI", Font.PLAIN, 13));
            formPanel.add(comp, gbc);
        }

        gbc.gridx = 1; gbc.gridy = 6;
        JLabel passHint = new JLabel("<html><i>Leave blank to keep existing password on update.</i></html>");
        passHint.setFont(new Font("Segoe UI", Font.ITALIC, 11));
        passHint.setForeground(Color.GRAY);
        formPanel.add(passHint, gbc);
        gbc.gridy = 7;
        formPanel.add(chkActive, gbc);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 8, 5));
        btnAdd    = createBtn("Add",        new Color(0x117A65), Color.WHITE);
        btnUpdate = createBtn("Update",     new Color(0x1B4F72), Color.WHITE);
        btnDelete = createBtn("Deactivate", new Color(0xC0392B), Color.WHITE);
        btnClear  = createBtn("Clear",      Color.LIGHT_GRAY,    Color.DARK_GRAY);

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

    private JButton createBtn(String text, Color bg, Color fg) {
        JButton btn = new JButton(text);
        btn.setBackground(bg); btn.setForeground(fg);
        btn.setFocusPainted(false);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        return btn;
    }

    // Helper — i-disable lahat ng buttons habang nag-proprocess
    private void setButtonsEnabled(boolean enabled) {
        btnAdd.setEnabled(enabled);
        btnUpdate.setEnabled(enabled);
        btnDelete.setEnabled(enabled);
        btnClear.setEnabled(enabled);
    }

    // ─── SwingWorker: loadLibrarians ─────────────────────────────────────────────
    private void loadLibrarians() {
        setButtonsEnabled(false);

        SwingWorker<List<Librarian>, Void> worker = new SwingWorker<>() {
            @Override
            protected List<Librarian> doInBackground() {
                // Nasa background thread — ligtas mag-query ng DB dito
                return librarianDAO.getAll();
            }

            @Override
            protected void done() {
                // Balik sa EDT para i-update ang table
                try {
                    List<Librarian> list = get();
                    tableModel.setRowCount(0);
                    for (Librarian l : list) {
                        tableModel.addRow(new Object[]{
                            l.getLibrarianID(), l.getEmployeeID(),
                            l.getFirstName(),   l.getLastName(),
                            l.getEmail(),       l.getContactNumber(),
                            l.isActive() ? "Yes" : "No"
                        });
                    }
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(
                            ManageLibrariansPanel.this,
                            "Error loading librarians: " + ex.getMessage(),
                            "Error", JOptionPane.ERROR_MESSAGE
                    );
                } finally {
                    setButtonsEnabled(true);
                }
            }
        };

        worker.execute();
    }

    // ─── SwingWorker: addLibrarian ───────────────────────────────────────────────
    private void addLibrarian() {
        String empID = txtEmployeeID.getText().trim();
        String fn    = txtFirstName .getText().trim();
        String ln    = txtLastName  .getText().trim();
        String email = txtEmail     .getText().trim();
        String pass  = new String(txtPassword.getPassword());

        if (empID.isEmpty() || fn.isEmpty() || ln.isEmpty() || email.isEmpty() || pass.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Employee ID, name, email, and password are required.",
                "Validation", JOptionPane.WARNING_MESSAGE); return;
        }
        if (!ValidationUtil.isValidEmail(email)) {
            JOptionPane.showMessageDialog(this, "Invalid email format.",
                "Validation", JOptionPane.WARNING_MESSAGE); return;
        }

        // I-build ang Librarian object bago pumasok sa background thread
        Librarian l = new Librarian();
        l.setEmployeeID   (empID);
        l.setFirstName    (fn);
        l.setLastName     (ln);
        l.setEmail        (email);
        l.setContactNumber(txtContact.getText().trim());
        l.setActive       (true);

        setButtonsEnabled(false);

        SwingWorker<Boolean, Void> worker = new SwingWorker<>() {
            @Override
            protected Boolean doInBackground() {
                return librarianDAO.add(l, pass);
            }

            @Override
            protected void done() {
                try {
                    if (get()) {
                        JOptionPane.showMessageDialog(
                                ManageLibrariansPanel.this,
                                "Librarian added!", "Success", JOptionPane.INFORMATION_MESSAGE
                        );
                        loadLibrarians();
                        clearForm();
                    } else {
                        JOptionPane.showMessageDialog(
                                ManageLibrariansPanel.this,
                                "Failed to add librarian.", "Error", JOptionPane.ERROR_MESSAGE
                        );
                    }
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(
                            ManageLibrariansPanel.this,
                            "Error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE
                    );
                } finally {
                    setButtonsEnabled(true);
                }
            }
        };

        worker.execute();
    }

    // ─── SwingWorker: updateLibrarian ───────────────────────────────────────────
    private void updateLibrarian() {
        if (selectedLibrarianID < 0) {
            JOptionPane.showMessageDialog(this, "Select a librarian first.", "Warning", JOptionPane.WARNING_MESSAGE); return;
        }
        String fn    = txtFirstName.getText().trim();
        String ln    = txtLastName .getText().trim();
        String email = txtEmail    .getText().trim();

        if (fn.isEmpty() || ln.isEmpty() || email.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Name and email are required.",
                "Validation", JOptionPane.WARNING_MESSAGE); return;
        }

        // I-capture ang values bago pumasok sa background thread
        int    idToUpdate   = selectedLibrarianID;
        String contact      = txtContact.getText().trim();
        boolean active      = chkActive.isSelected();
        String pass         = new String(txtPassword.getPassword());

        setButtonsEnabled(false);

        SwingWorker<Boolean, Void> worker = new SwingWorker<>() {
            @Override
            protected Boolean doInBackground() {
                // Fetch at update sa background thread
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
                        JOptionPane.showMessageDialog(
                                ManageLibrariansPanel.this,
                                "Failed to update librarian.", "Error", JOptionPane.ERROR_MESSAGE
                        );
                    } else {
                        JOptionPane.showMessageDialog(
                                ManageLibrariansPanel.this,
                                "Librarian updated!", "Success", JOptionPane.INFORMATION_MESSAGE
                        );
                        loadLibrarians();
                        clearForm();
                    }
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(
                            ManageLibrariansPanel.this,
                            "Error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE
                    );
                } finally {
                    setButtonsEnabled(true);
                }
            }
        };

        worker.execute();
    }

    // ─── SwingWorker: deactivateLibrarian ───────────────────────────────────────
    private void deactivateLibrarian() {
        if (selectedLibrarianID < 0) {
            JOptionPane.showMessageDialog(this, "Select a librarian first.", "Warning", JOptionPane.WARNING_MESSAGE); return;
        }
        if (JOptionPane.showConfirmDialog(this, "Deactivate this librarian?", "Confirm",
                JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION) return;

        int idToDeactivate = selectedLibrarianID;
        setButtonsEnabled(false);

        SwingWorker<Boolean, Void> worker = new SwingWorker<>() {
            @Override
            protected Boolean doInBackground() {
                return librarianDAO.deactivate(idToDeactivate);
            }

            @Override
            protected void done() {
                try {
                    if (get()) {
                        JOptionPane.showMessageDialog(
                                ManageLibrariansPanel.this,
                                "Librarian deactivated.", "Success", JOptionPane.INFORMATION_MESSAGE
                        );
                        loadLibrarians();
                        clearForm();
                    } else {
                        JOptionPane.showMessageDialog(
                                ManageLibrariansPanel.this,
                                "Failed to deactivate librarian.", "Error", JOptionPane.ERROR_MESSAGE
                        );
                    }
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(
                            ManageLibrariansPanel.this,
                            "Error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE
                    );
                } finally {
                    setButtonsEnabled(true);
                }
            }
        };

        worker.execute();
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