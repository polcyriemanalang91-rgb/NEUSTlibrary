package neustlibrarysystem.view.admin;

import neustlibrarysystem.dao.SystemSettingsDAO;
import neustlibrarysystem.model.Admin;
import neustlibrarysystem.model.SystemSettings;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.SQLException;
import java.util.List;

public class SystemSettingsPanel extends JPanel {

    private final Admin currentAdmin;
    private JTable settingsTable;
    private DefaultTableModel tableModel;
    private JTextField txtKey, txtValue, txtDescription;
    private JButton btnUpdate, btnClear;
    private SystemSettingsDAO settingsDAO;
    private int selectedSettingID = -1;

    public SystemSettingsPanel(Admin currentAdmin) {
        this.currentAdmin = currentAdmin;
        this.settingsDAO = new SystemSettingsDAO();
        initComponents();
        loadSettings();
    }

    private void initComponents() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        JLabel title = new JLabel("System Settings");
        title.setFont(new Font("Segoe UI", Font.BOLD, 20));
        title.setForeground(new Color(0x1B4F72));

        JLabel subtitle = new JLabel("Configure library system parameters.");
        subtitle.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        subtitle.setForeground(Color.GRAY);

        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.add(title, BorderLayout.NORTH);
        topPanel.add(subtitle, BorderLayout.CENTER);
        add(topPanel, BorderLayout.NORTH);

        String[] cols = {"ID", "Setting Key", "Value", "Description", "Last Updated"};
        tableModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        settingsTable = new JTable(tableModel);
        settingsTable.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        settingsTable.setRowHeight(26);
        settingsTable.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 13));
        settingsTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        settingsTable.getColumnModel().getColumn(0).setMaxWidth(50);
        settingsTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) populateForm();
        });
        add(new JScrollPane(settingsTable), BorderLayout.CENTER);

        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBorder(BorderFactory.createTitledBorder("Edit Setting"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 10, 8, 10);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;

        txtKey         = new JTextField(20);
        txtKey.setEditable(false);
        txtKey.setBackground(new Color(0xECECEC));
        txtValue       = new JTextField(20);
        txtDescription = new JTextField(30);
        txtDescription.setEditable(false);
        txtDescription.setBackground(new Color(0xECECEC));

        Object[][] fields = {{"Setting Key:", txtKey}, {"Value:", txtValue}, {"Description:", txtDescription}};
        for (int i = 0; i < fields.length; i++) {
            gbc.gridx = 0; gbc.gridy = i; gbc.weightx = 0;
            JLabel lbl = new JLabel((String) fields[i][0]);
            lbl.setFont(new Font("Segoe UI", Font.BOLD, 13));
            formPanel.add(lbl, gbc);
            gbc.gridx = 1; gbc.weightx = 1;
            JTextField tf = (JTextField) fields[i][1];
            tf.setFont(new Font("Segoe UI", Font.PLAIN, 13));
            formPanel.add(tf, gbc);
        }

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 8, 5));
        btnUpdate = new JButton("Save Setting");
        btnUpdate.setBackground(new Color(0x1B4F72));
        btnUpdate.setForeground(Color.WHITE);
        btnUpdate.setFocusPainted(false);
        btnUpdate.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnUpdate.addActionListener(e -> saveSetting());

        btnClear = new JButton("Clear Selection");
        btnClear.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        btnClear.addActionListener(e -> clearForm());

        btnPanel.add(btnUpdate); btnPanel.add(btnClear);

        gbc.gridx = 0; gbc.gridy = 3; gbc.gridwidth = 2;
        formPanel.add(btnPanel, gbc);

        JLabel notice = new JLabel("<html><i>Note: Changes take effect immediately. Restart may be required for some settings.</i></html>");
        notice.setFont(new Font("Segoe UI", Font.ITALIC, 11));
        notice.setForeground(Color.GRAY);
        gbc.gridy = 4;
        formPanel.add(notice, gbc);

        add(formPanel, BorderLayout.SOUTH);
    }

    // ─── SwingWorker: loadSettings ───────────────────────────────────────────────
    private void loadSettings() {
        // Disable buttons habang nag-loload
        btnUpdate.setEnabled(false);
        btnClear.setEnabled(false);

        SwingWorker<List<SystemSettings>, Void> worker = new SwingWorker<>() {
            @Override
            protected List<SystemSettings> doInBackground() throws SQLException {
                // Nasa background thread — ligtas mag-query ng DB dito
                return settingsDAO.getAllSettings();
            }

            @Override
            protected void done() {
                // Balik sa EDT para i-update ang UI
                try {
                    List<SystemSettings> list = get();
                    tableModel.setRowCount(0);
                    for (SystemSettings s : list) {
                        tableModel.addRow(new Object[]{
                                s.getSettingID(), s.getSettingKey(), s.getSettingValue(),
                                s.getDescription(),
                                s.getUpdatedAt() != null ? s.getUpdatedAt().toLocalDate() : ""
                        });
                    }
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(
                            SystemSettingsPanel.this,
                            "Error loading settings: " + ex.getMessage(),
                            "Error", JOptionPane.ERROR_MESSAGE
                    );
                } finally {
                    // I-enable ulit ang buttons pagkatapos
                    btnUpdate.setEnabled(true);
                    btnClear.setEnabled(true);
                }
            }
        };

        worker.execute();
    }

    // ─── SwingWorker: saveSetting ────────────────────────────────────────────────
    private void saveSetting() {
        if (selectedSettingID < 0) {
            JOptionPane.showMessageDialog(this, "Select a setting first.", "Warning", JOptionPane.WARNING_MESSAGE);
            return;
        }
        String newValue = txtValue.getText().trim();
        if (newValue.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Value cannot be empty.", "Validation", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // I-capture ang mga values bago pumasok sa background thread
        String keyToUpdate   = txtKey.getText();
        int    adminID       = currentAdmin.getAdminID();

        // Disable buttons habang nag-sasave
        btnUpdate.setEnabled(false);
        btnClear.setEnabled(false);

        SwingWorker<Boolean, Void> worker = new SwingWorker<>() {
            @Override
            protected Boolean doInBackground() throws SQLException {
                // Nasa background thread — ligtas mag-query ng DB dito
                return settingsDAO.updateSetting(keyToUpdate, newValue, adminID);
            }

            @Override
            protected void done() {
                // Balik sa EDT para i-update ang UI
                try {
                    boolean ok = get();
                    if (ok) {
                        JOptionPane.showMessageDialog(
                                SystemSettingsPanel.this,
                                "Setting updated successfully!",
                                "Success", JOptionPane.INFORMATION_MESSAGE
                        );
                        loadSettings(); // mag-reload (SwingWorker din ito)
                        clearForm();
                    }
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(
                            SystemSettingsPanel.this,
                            "Error: " + ex.getMessage(),
                            "Error", JOptionPane.ERROR_MESSAGE
                    );
                } finally {
                    // I-enable ulit ang buttons pagkatapos
                    btnUpdate.setEnabled(true);
                    btnClear.setEnabled(true);
                }
            }
        };

        worker.execute();
    }

    private void populateForm() {
        int row = settingsTable.getSelectedRow();
        if (row < 0) return;
        selectedSettingID = (int) tableModel.getValueAt(row, 0);
        txtKey.setText(str(tableModel.getValueAt(row, 1)));
        txtValue.setText(str(tableModel.getValueAt(row, 2)));
        txtDescription.setText(str(tableModel.getValueAt(row, 3)));
    }

    private void clearForm() {
        selectedSettingID = -1;
        txtKey.setText(""); txtValue.setText(""); txtDescription.setText("");
        settingsTable.clearSelection();
    }

    private String str(Object o) { return o != null ? o.toString() : ""; }
}