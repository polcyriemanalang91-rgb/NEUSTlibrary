package neustlibrarysystem.view.admin;

import neustlibrarysystem.dao.CategoryDAO;
import neustlibrarysystem.model.Admin;
import neustlibrarysystem.model.Category;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.SQLException;
import java.util.List;

public class ManageCategoriesPanel extends JPanel {

    private final Admin currentAdmin;
    private JTable categoryTable;
    private DefaultTableModel tableModel;
    private JTextField txtName, txtDescription;
    private JCheckBox chkActive;
    private JButton btnAdd, btnUpdate, btnDelete, btnClear;
    private CategoryDAO categoryDAO;
    private int selectedCategoryID = -1;

    public ManageCategoriesPanel(Admin currentAdmin) {
        this.currentAdmin = currentAdmin;
        this.categoryDAO = new CategoryDAO();
        initComponents();
        loadCategories();
    }

    private void initComponents() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        JLabel title = new JLabel("Manage Categories");
        title.setFont(new Font("Segoe UI", Font.BOLD, 20));
        title.setForeground(new Color(0x1B4F72));
        add(title, BorderLayout.NORTH);

        String[] cols = {"Category ID", "Category Name", "Description", "Active", "Created At"};
        tableModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        categoryTable = new JTable(tableModel);
        categoryTable.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        categoryTable.setRowHeight(26);
        categoryTable.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 13));
        categoryTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        categoryTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) populateForm();
        });
        add(new JScrollPane(categoryTable), BorderLayout.CENTER);

        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBorder(BorderFactory.createTitledBorder("Category Details"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(7, 8, 7, 8);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;

        txtName        = new JTextField(20);
        txtDescription = new JTextField(30);
        chkActive      = new JCheckBox("Active", true);

        String[] labels = {"Category Name: *", "Description:"};
        JTextField[] fields = {txtName, txtDescription};
        for (int i = 0; i < labels.length; i++) {
            gbc.gridx = 0; gbc.gridy = i; gbc.weightx = 0;
            JLabel lbl = new JLabel(labels[i]);
            lbl.setFont(new Font("Segoe UI", Font.BOLD, 13));
            formPanel.add(lbl, gbc);
            gbc.gridx = 1; gbc.weightx = 1;
            fields[i].setFont(new Font("Segoe UI", Font.PLAIN, 13));
            formPanel.add(fields[i], gbc);
        }
        gbc.gridx = 1; gbc.gridy = 2;
        formPanel.add(chkActive, gbc);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 8, 5));
        btnAdd    = createBtn("Add",    new Color(0x117A65), Color.WHITE);
        btnUpdate = createBtn("Update", new Color(0x1B4F72), Color.WHITE);
        btnDelete = createBtn("Delete", new Color(0xC0392B), Color.WHITE);
        btnClear  = createBtn("Clear",  Color.LIGHT_GRAY, Color.DARK_GRAY);
        btnAdd.addActionListener(e -> addCategory());
        btnUpdate.addActionListener(e -> updateCategory());
        btnDelete.addActionListener(e -> deleteCategory());
        btnClear.addActionListener(e -> clearForm());
        btnPanel.add(btnAdd); btnPanel.add(btnUpdate);
        btnPanel.add(btnDelete); btnPanel.add(btnClear);

        gbc.gridx = 0; gbc.gridy = 3; gbc.gridwidth = 2;
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

    // ─── SwingWorker: loadCategories ─────────────────────────────────────────────
    private void loadCategories() {
        setButtonsEnabled(false);

        SwingWorker<List<Category>, Void> worker = new SwingWorker<>() {
            @Override
            protected List<Category> doInBackground() throws SQLException {
                // Nasa background thread — ligtas mag-query ng DB dito
                return categoryDAO.getAllCategories();
            }

            @Override
            protected void done() {
                // Balik sa EDT para i-update ang table
                try {
                    List<Category> list = get();
                    tableModel.setRowCount(0);
                    for (Category c : list) {
                        tableModel.addRow(new Object[]{
                                c.getCategoryID(), c.getCategoryName(), c.getDescription(),
                                c.isActive() ? "Yes" : "No",
                                c.getCreatedAt() != null ? c.getCreatedAt().toLocalDate() : ""
                        });
                    }
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(
                            ManageCategoriesPanel.this,
                            "Error loading categories: " + ex.getMessage(),
                            "Error", JOptionPane.ERROR_MESSAGE
                    );
                } finally {
                    setButtonsEnabled(true);
                }
            }
        };

        worker.execute();
    }

    // ─── SwingWorker: addCategory ────────────────────────────────────────────────
    private void addCategory() {
        String name = txtName.getText().trim();
        if (name.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Category name is required.", "Validation", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // I-capture ang values bago pumasok sa background thread
        String description = txtDescription.getText().trim();

        setButtonsEnabled(false);

        SwingWorker<Boolean, Void> worker = new SwingWorker<>() {
            @Override
            protected Boolean doInBackground() throws SQLException {
                // I-check ang duplicate at i-add — parehong nasa background
                if (categoryDAO.categoryNameExists(name, -1)) return null; // null = duplicate
                Category c = new Category(name, description);
                return categoryDAO.addCategory(c);
            }

            @Override
            protected void done() {
                try {
                    Boolean result = get();
                    if (result == null) {
                        JOptionPane.showMessageDialog(
                                ManageCategoriesPanel.this,
                                "Category name already exists.", "Duplicate", JOptionPane.WARNING_MESSAGE
                        );
                    } else if (result) {
                        JOptionPane.showMessageDialog(
                                ManageCategoriesPanel.this,
                                "Category added!", "Success", JOptionPane.INFORMATION_MESSAGE
                        );
                        loadCategories();
                        clearForm();
                    }
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(
                            ManageCategoriesPanel.this,
                            "Error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE
                    );
                } finally {
                    setButtonsEnabled(true);
                }
            }
        };

        worker.execute();
    }

    // ─── SwingWorker: updateCategory ─────────────────────────────────────────────
    private void updateCategory() {
        if (selectedCategoryID < 0) {
            JOptionPane.showMessageDialog(this, "Select a category first.", "Warning", JOptionPane.WARNING_MESSAGE);
            return;
        }
        String name = txtName.getText().trim();
        if (name.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Category name is required.", "Validation", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // I-capture ang values bago pumasok sa background thread
        String  description = txtDescription.getText().trim();
        boolean active      = chkActive.isSelected();
        int     idToUpdate  = selectedCategoryID;

        setButtonsEnabled(false);

        SwingWorker<Boolean, Void> worker = new SwingWorker<>() {
            @Override
            protected Boolean doInBackground() throws SQLException {
                Category c = new Category(name, description);
                c.setCategoryID(idToUpdate);
                c.setActive(active);
                return categoryDAO.updateCategory(c);
            }

            @Override
            protected void done() {
                try {
                    if (get()) {
                        JOptionPane.showMessageDialog(
                                ManageCategoriesPanel.this,
                                "Category updated!", "Success", JOptionPane.INFORMATION_MESSAGE
                        );
                        loadCategories();
                        clearForm();
                    }
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(
                            ManageCategoriesPanel.this,
                            "Error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE
                    );
                } finally {
                    setButtonsEnabled(true);
                }
            }
        };

        worker.execute();
    }

    // ─── SwingWorker: deleteCategory ─────────────────────────────────────────────
    private void deleteCategory() {
        if (selectedCategoryID < 0) {
            JOptionPane.showMessageDialog(this, "Select a category first.", "Warning", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (JOptionPane.showConfirmDialog(this, "Deactivate this category?", "Confirm",
                JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION) return;

        int idToDelete = selectedCategoryID;
        setButtonsEnabled(false);

        SwingWorker<Void, Void> worker = new SwingWorker<>() {
            @Override
            protected Void doInBackground() throws SQLException {
                categoryDAO.deleteCategory(idToDelete);
                return null;
            }

            @Override
            protected void done() {
                try {
                    get();
                    JOptionPane.showMessageDialog(
                            ManageCategoriesPanel.this,
                            "Category deactivated.", "Success", JOptionPane.INFORMATION_MESSAGE
                    );
                    loadCategories();
                    clearForm();
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(
                            ManageCategoriesPanel.this,
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
        int row = categoryTable.getSelectedRow();
        if (row < 0) return;
        selectedCategoryID = (int) tableModel.getValueAt(row, 0);
        txtName.setText((String) tableModel.getValueAt(row, 1));
        txtDescription.setText((String) tableModel.getValueAt(row, 2));
        chkActive.setSelected("Yes".equals(tableModel.getValueAt(row, 3)));
    }

    private void clearForm() {
        selectedCategoryID = -1;
        txtName.setText(""); txtDescription.setText(""); chkActive.setSelected(true);
        categoryTable.clearSelection();
    }
}