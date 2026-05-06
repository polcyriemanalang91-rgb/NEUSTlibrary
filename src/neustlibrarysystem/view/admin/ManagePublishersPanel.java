package neustlibrarysystem.view.admin;

import neustlibrarysystem.dao.PublisherDAO;
import neustlibrarysystem.model.Admin;
import neustlibrarysystem.model.Publisher;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.SQLException;
import java.util.List;

public class ManagePublishersPanel extends JPanel {

    private final Admin currentAdmin;
    private JTable publisherTable;
    private DefaultTableModel tableModel;
    private JTextField txtName, txtAddress, txtEmail, txtPhone;
    private JCheckBox chkActive;
    private JButton btnAdd, btnUpdate, btnDelete, btnClear;
    private PublisherDAO publisherDAO;
    private int selectedPublisherID = -1;

    public ManagePublishersPanel(Admin currentAdmin) {
        this.currentAdmin = currentAdmin;
        this.publisherDAO = new PublisherDAO();
        initComponents();
        loadPublishers();
    }

    private void initComponents() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        JLabel title = new JLabel("Manage Publishers");
        title.setFont(new Font("Segoe UI", Font.BOLD, 20));
        title.setForeground(new Color(0x1B4F72));
        add(title, BorderLayout.NORTH);

        String[] cols = {"Publisher ID", "Name", "Address", "Email", "Phone", "Active"};
        tableModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        publisherTable = new JTable(tableModel);
        publisherTable.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        publisherTable.setRowHeight(26);
        publisherTable.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 13));
        publisherTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        publisherTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) populateForm();
        });
        add(new JScrollPane(publisherTable), BorderLayout.CENTER);

        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBorder(BorderFactory.createTitledBorder("Publisher Details"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(7, 8, 7, 8);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;

        txtName    = new JTextField(25);
        txtAddress = new JTextField(25);
        txtEmail   = new JTextField(25);
        txtPhone   = new JTextField(15);
        chkActive  = new JCheckBox("Active", true);

        Object[][] fields = {{"Publisher Name: *", txtName}, {"Address:", txtAddress}, {"Contact Email:", txtEmail}, {"Phone:", txtPhone}};
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
        gbc.gridx = 1; gbc.gridy = 4; formPanel.add(chkActive, gbc);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 8, 5));
        btnAdd    = createBtn("Add",    new Color(0x117A65), Color.WHITE);
        btnUpdate = createBtn("Update", new Color(0x1B4F72), Color.WHITE);
        btnDelete = createBtn("Delete", new Color(0xC0392B), Color.WHITE);
        btnClear  = createBtn("Clear",  Color.LIGHT_GRAY, Color.DARK_GRAY);
        btnAdd.addActionListener(e -> addPublisher());
        btnUpdate.addActionListener(e -> updatePublisher());
        btnDelete.addActionListener(e -> deletePublisher());
        btnClear.addActionListener(e -> clearForm());
        btnPanel.add(btnAdd); btnPanel.add(btnUpdate);
        btnPanel.add(btnDelete); btnPanel.add(btnClear);

        gbc.gridx = 0; gbc.gridy = 5; gbc.gridwidth = 2;
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

    // ─── SwingWorker: loadPublishers ─────────────────────────────────────────────
    private void loadPublishers() {
        setButtonsEnabled(false);

        SwingWorker<List<Publisher>, Void> worker = new SwingWorker<>() {
            @Override
            protected List<Publisher> doInBackground() throws SQLException {
                // Nasa background thread — ligtas mag-query ng DB dito
                return publisherDAO.getAllPublishers();
            }

            @Override
            protected void done() {
                // Balik sa EDT para i-update ang table
                try {
                    List<Publisher> list = get();
                    tableModel.setRowCount(0);
                    for (Publisher p : list) {
                        tableModel.addRow(new Object[]{
                                p.getPublisherID(), p.getPublisherName(), p.getAddress(),
                                p.getContactEmail(), p.getContactPhone(), p.isActive() ? "Yes" : "No"
                        });
                    }
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(
                            ManagePublishersPanel.this,
                            "Error loading publishers: " + ex.getMessage(),
                            "Error", JOptionPane.ERROR_MESSAGE
                    );
                } finally {
                    setButtonsEnabled(true);
                }
            }
        };

        worker.execute();
    }

    // ─── SwingWorker: addPublisher ───────────────────────────────────────────────
    private void addPublisher() {
        String name = txtName.getText().trim();
        if (name.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Publisher name required.", "Validation", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // I-capture ang values bago pumasok sa background thread
        Publisher p = new Publisher(name, txtAddress.getText().trim(), txtEmail.getText().trim(), txtPhone.getText().trim());

        setButtonsEnabled(false);

        SwingWorker<Boolean, Void> worker = new SwingWorker<>() {
            @Override
            protected Boolean doInBackground() throws SQLException {
                return publisherDAO.addPublisher(p);
            }

            @Override
            protected void done() {
                try {
                    if (get()) {
                        JOptionPane.showMessageDialog(
                                ManagePublishersPanel.this,
                                "Publisher added!",
                                "Success", JOptionPane.INFORMATION_MESSAGE
                        );
                        loadPublishers();
                        clearForm();
                    }
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(
                            ManagePublishersPanel.this,
                            "Error: " + ex.getMessage(),
                            "Error", JOptionPane.ERROR_MESSAGE
                    );
                } finally {
                    setButtonsEnabled(true);
                }
            }
        };

        worker.execute();
    }

    // ─── SwingWorker: updatePublisher ───────────────────────────────────────────
    private void updatePublisher() {
        if (selectedPublisherID < 0) {
            JOptionPane.showMessageDialog(this, "Select a publisher first.", "Warning", JOptionPane.WARNING_MESSAGE);
            return;
        }
        String name = txtName.getText().trim();
        if (name.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Publisher name required.", "Validation", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // I-capture ang values bago pumasok sa background thread
        Publisher p = new Publisher(name, txtAddress.getText().trim(), txtEmail.getText().trim(), txtPhone.getText().trim());
        p.setPublisherID(selectedPublisherID);
        p.setActive(chkActive.isSelected());

        setButtonsEnabled(false);

        SwingWorker<Boolean, Void> worker = new SwingWorker<>() {
            @Override
            protected Boolean doInBackground() throws SQLException {
                return publisherDAO.updatePublisher(p);
            }

            @Override
            protected void done() {
                try {
                    if (get()) {
                        JOptionPane.showMessageDialog(
                                ManagePublishersPanel.this,
                                "Publisher updated!",
                                "Success", JOptionPane.INFORMATION_MESSAGE
                        );
                        loadPublishers();
                        clearForm();
                    }
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(
                            ManagePublishersPanel.this,
                            "Error: " + ex.getMessage(),
                            "Error", JOptionPane.ERROR_MESSAGE
                    );
                } finally {
                    setButtonsEnabled(true);
                }
            }
        };

        worker.execute();
    }

    // ─── SwingWorker: deletePublisher ───────────────────────────────────────────
    private void deletePublisher() {
        if (selectedPublisherID < 0) {
            JOptionPane.showMessageDialog(this, "Select a publisher first.", "Warning", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (JOptionPane.showConfirmDialog(this, "Deactivate this publisher?", "Confirm", JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION) return;

        int idToDelete = selectedPublisherID;
        setButtonsEnabled(false);

        SwingWorker<Void, Void> worker = new SwingWorker<>() {
            @Override
            protected Void doInBackground() throws SQLException {
                publisherDAO.deletePublisher(idToDelete);
                return null;
            }

            @Override
            protected void done() {
                try {
                    get();
                    JOptionPane.showMessageDialog(
                            ManagePublishersPanel.this,
                            "Publisher deactivated.",
                            "Success", JOptionPane.INFORMATION_MESSAGE
                    );
                    loadPublishers();
                    clearForm();
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(
                            ManagePublishersPanel.this,
                            "Error: " + ex.getMessage(),
                            "Error", JOptionPane.ERROR_MESSAGE
                    );
                } finally {
                    setButtonsEnabled(true);
                }
            }
        };

        worker.execute();
    }

    private void populateForm() {
        int row = publisherTable.getSelectedRow();
        if (row < 0) return;
        selectedPublisherID = (int) tableModel.getValueAt(row, 0);
        txtName.setText(str(tableModel.getValueAt(row, 1)));
        txtAddress.setText(str(tableModel.getValueAt(row, 2)));
        txtEmail.setText(str(tableModel.getValueAt(row, 3)));
        txtPhone.setText(str(tableModel.getValueAt(row, 4)));
        chkActive.setSelected("Yes".equals(tableModel.getValueAt(row, 5)));
    }

    private void clearForm() {
        selectedPublisherID = -1;
        txtName.setText(""); txtAddress.setText(""); txtEmail.setText(""); txtPhone.setText(""); chkActive.setSelected(true);
        publisherTable.clearSelection();
    }

    private String str(Object o) { return o != null ? o.toString() : ""; }
}