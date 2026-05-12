package neustlibrarysystem.view.admin;

import neustlibrarysystem.dao.CategoryDAO;
import neustlibrarysystem.model.Admin;
import neustlibrarysystem.model.Category;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ManageCategoriesPanel extends JPanel {

    // ── Design Tokens (matches AdminDashboard) ────────────────────────────────
    private static final Color CLR_BG      = new Color(0x0d1b12);
    private static final Color CLR_CARD    = new Color(0x132218);
    private static final Color CLR_CARD2   = new Color(0x1a2e1f);
    private static final Color CLR_ACCENT  = new Color(0x4ade80);
    private static final Color CLR_ACCENT4 = new Color(0xf87171);
    private static final Color CLR_TEXT    = new Color(0xe2f5e8);
    private static final Color CLR_MUTED   = new Color(0x6b9e7a);
    private static final Color CLR_BORDER  = new Color(0x1f3d28);
    private static final Color CLR_HDR_BG  = new Color(0x0a1a10);

    private static final Font FONT_TITLE = new Font("Segoe UI", Font.BOLD,  22);
    private static final Font FONT_LABEL = new Font("Segoe UI", Font.BOLD,  12);
    private static final Font FONT_BODY  = new Font("Segoe UI", Font.PLAIN, 13);
    private static final Font FONT_SMALL = new Font("Segoe UI", Font.PLAIN, 11);

    private final Admin       currentAdmin;
    private final CategoryDAO categoryDAO;

    private JTable            categoryTable;
    private DefaultTableModel tableModel;

    private JTextField txtName, txtDescription, txtSearch;
    private JCheckBox  chkActive;
    private JButton    btnAdd, btnUpdate, btnDelete, btnClear;

    private int            selectedCategoryID = -1;
    private List<Category> cachedCategories   = new ArrayList<>();

    // ── Constructor ───────────────────────────────────────────────────────────
    public ManageCategoriesPanel(Admin currentAdmin) {
        this.currentAdmin = currentAdmin;
        this.categoryDAO  = new CategoryDAO();
        setLayout(new BorderLayout(0, 16));
        setBackground(CLR_BG);
        setBorder(new EmptyBorder(0, 0, 0, 0));
        buildUI();
    }

    public void refresh() {
        loadCategories();
    }

    // ── UI Builder ────────────────────────────────────────────────────────────
    private void buildUI() {
        add(buildHeader(), BorderLayout.NORTH);

        JPanel centerPanel = new JPanel(new BorderLayout(0, 8));
        centerPanel.setOpaque(false);
        centerPanel.add(buildSearchBar(), BorderLayout.NORTH);
        centerPanel.add(buildTable(),     BorderLayout.CENTER);
        add(centerPanel, BorderLayout.CENTER);

        add(buildForm(), BorderLayout.SOUTH);
    }

    // ── Header ────────────────────────────────────────────────────────────────
    private JPanel buildHeader() {
        JPanel hdr = new JPanel();
        hdr.setOpaque(false);
        hdr.setLayout(new BoxLayout(hdr, BoxLayout.Y_AXIS));

        JLabel title = new JLabel("🏷  Manage Categories");
        title.setFont(FONT_TITLE);
        title.setForeground(CLR_TEXT);

        JLabel sub = new JLabel("Add, update, or deactivate book categories");
        sub.setFont(FONT_SMALL);
        sub.setForeground(CLR_MUTED);

        hdr.add(title);
        hdr.add(Box.createVerticalStrut(3));
        hdr.add(sub);
        hdr.add(Box.createVerticalStrut(6));

        JSeparator sep = new JSeparator();
        sep.setForeground(CLR_BORDER);
        sep.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));
        hdr.add(sep);
        return hdr;
    }

    // ── Search Bar ────────────────────────────────────────────────────────────
    private JPanel buildSearchBar() {
        JPanel panel = new JPanel(new BorderLayout(8, 0));
        panel.setOpaque(false);
        panel.setBorder(new EmptyBorder(6, 0, 2, 0));

        JLabel searchIcon = new JLabel("🔍");
        searchIcon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 14));
        searchIcon.setBorder(new EmptyBorder(0, 4, 0, 4));

        txtSearch = new JTextField();
        txtSearch.setFont(FONT_BODY);
        txtSearch.setForeground(CLR_TEXT);
        txtSearch.setBackground(CLR_CARD2);
        txtSearch.setCaretColor(CLR_ACCENT);
        txtSearch.putClientProperty("JTextField.placeholderText", "Search by ID or category name...");
        txtSearch.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(CLR_BORDER, 1, true),
            new EmptyBorder(7, 10, 7, 10)
        ));

        txtSearch.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e)  { filterTable(); }
            public void removeUpdate(DocumentEvent e)  { filterTable(); }
            public void changedUpdate(DocumentEvent e) { filterTable(); }
        });

        JButton btnClearSearch = new JButton("✕");
        btnClearSearch.setFont(FONT_BODY);
        btnClearSearch.setForeground(CLR_MUTED);
        btnClearSearch.setBackground(CLR_CARD2);
        btnClearSearch.setBorderPainted(false);
        btnClearSearch.setFocusPainted(false);
        btnClearSearch.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnClearSearch.addActionListener(e -> txtSearch.setText(""));

        panel.add(searchIcon,     BorderLayout.WEST);
        panel.add(txtSearch,      BorderLayout.CENTER);
        panel.add(btnClearSearch, BorderLayout.EAST);
        return panel;
    }

    // ── Filter Logic ──────────────────────────────────────────────────────────
    private void filterTable() {
        String keyword = txtSearch.getText().trim().toLowerCase();
        tableModel.setRowCount(0);

        for (Category c : cachedCategories) {
            String name = c.getCategoryName().toLowerCase();
            String id   = String.valueOf(c.getCategoryID());

            if (keyword.isEmpty()
                    || name.contains(keyword)
                    || id.contains(keyword)) {
                tableModel.addRow(new Object[]{
                    c.getCategoryID(), c.getCategoryName(), c.getDescription(),
                    c.isActive() ? "Yes" : "No",
                    c.getCreatedAt() != null ? c.getCreatedAt().toLocalDate() : ""
                });
            }
        }
    }

    // ── Table ─────────────────────────────────────────────────────────────────
    private JScrollPane buildTable() {
        String[] cols = {"Category ID", "Category Name", "Description", "Active", "Created At"};
        tableModel = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };

        categoryTable = new JTable(tableModel);
        categoryTable.setFont(FONT_BODY);
        categoryTable.setForeground(CLR_TEXT);
        categoryTable.setBackground(CLR_CARD);
        categoryTable.setRowHeight(34);
        categoryTable.setSelectionBackground(new Color(0x1e4a2a));
        categoryTable.setSelectionForeground(CLR_ACCENT);
        categoryTable.setGridColor(CLR_BORDER);
        categoryTable.setShowVerticalLines(false);
        categoryTable.setIntercellSpacing(new Dimension(0, 1));
        categoryTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        // ── Header renderer ───────────────────────────────────────────────────
        JTableHeader header = categoryTable.getTableHeader();
        header.setPreferredSize(new Dimension(0, 40));
        header.setReorderingAllowed(false);
        header.setBorder(BorderFactory.createMatteBorder(0, 0, 2, 0, CLR_ACCENT));
        header.setDefaultRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(
                    JTable t, Object val, boolean sel, boolean foc, int row, int col) {
                JLabel lbl = (JLabel) super.getTableCellRendererComponent(t, val, sel, foc, row, col);
                lbl.setBackground(CLR_HDR_BG);
                lbl.setForeground(CLR_ACCENT);
                lbl.setFont(FONT_LABEL);
                lbl.setOpaque(true);
                lbl.setBorder(new EmptyBorder(0, 10, 0, 10));
                return lbl;
            }
        });

        // ── Row renderer ──────────────────────────────────────────────────────
        categoryTable.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(
                    JTable t, Object val, boolean sel, boolean foc, int row, int col) {
                super.getTableCellRendererComponent(t, val, sel, foc, row, col);
                setFont(FONT_BODY);
                setBorder(new EmptyBorder(0, 10, 0, 10));
                if (sel) {
                    setBackground(new Color(0x1e4a2a));
                    setForeground(CLR_ACCENT);
                } else {
                    setBackground(row % 2 == 0 ? CLR_CARD : CLR_CARD2);
                    setForeground(CLR_TEXT);
                    if (val instanceof String s) {
                        if (s.equals("Yes"))     setForeground(CLR_ACCENT);
                        else if (s.equals("No")) setForeground(CLR_ACCENT4);
                    }
                }
                return this;
            }
        });

        categoryTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) populateForm();
        });

        JScrollPane scroll = new JScrollPane(categoryTable);
        scroll.setOpaque(false);
        scroll.getViewport().setBackground(CLR_CARD);
        scroll.setBorder(BorderFactory.createLineBorder(CLR_BORDER, 1));
        scroll.getVerticalScrollBar().setBackground(CLR_CARD);
        return scroll;
    }

    // ── Form ──────────────────────────────────────────────────────────────────
    private JPanel buildForm() {
        JPanel wrapper = new JPanel(new BorderLayout()) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(CLR_CARD);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 14, 14);
                g2.setColor(CLR_BORDER);
                g2.drawRoundRect(0, 0, getWidth()-1, getHeight()-1, 14, 14);
                g2.dispose();
            }
        };
        wrapper.setOpaque(false);
        wrapper.setBorder(new EmptyBorder(16, 20, 16, 20));

        JPanel form = new JPanel(new GridBagLayout());
        form.setOpaque(false);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(7, 8, 7, 8);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill   = GridBagConstraints.HORIZONTAL;

        txtName        = darkField(20);
        txtDescription = darkField(30);
        chkActive      = new JCheckBox("Active", true);
        chkActive.setFont(FONT_BODY);
        chkActive.setForeground(CLR_TEXT);
        chkActive.setOpaque(false);

        Object[][] rows = {
            {"Category Name:", txtName},
            {"Description:",   txtDescription},
            {"",               chkActive}
        };

        for (int i = 0; i < rows.length; i++) {
            gbc.gridx = 0; gbc.gridy = i; gbc.weightx = 0;
            if (!((String) rows[i][0]).isEmpty()) {
                JLabel lbl = new JLabel((String) rows[i][0]);
                lbl.setFont(FONT_LABEL);
                lbl.setForeground(CLR_MUTED);
                form.add(lbl, gbc);
            }
            gbc.gridx = 1; gbc.weightx = 1;
            form.add((Component) rows[i][1], gbc);
        }

        // ── Buttons ───────────────────────────────────────────────────────────
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        btnPanel.setOpaque(false);

        btnAdd    = accentBtn("➕  Add",        new Color(0x1e4a2a));
        btnUpdate = accentBtn("✏  Update",      new Color(0x1a3a5c));
        btnDelete = accentBtn("🚫  Deactivate", new Color(0x5c1a1a));
        btnClear  = accentBtn("↺  Clear",       new Color(0x2a2a2a));

        btnAdd   .addActionListener(e -> addCategory());
        btnUpdate.addActionListener(e -> updateCategory());
        btnDelete.addActionListener(e -> deleteCategory());
        btnClear .addActionListener(e -> clearForm());

        btnPanel.add(btnAdd);
        btnPanel.add(btnUpdate);
        btnPanel.add(btnDelete);
        btnPanel.add(btnClear);

        gbc.gridx = 0; gbc.gridy = rows.length; gbc.gridwidth = 2;
        form.add(btnPanel, gbc);

        wrapper.add(form, BorderLayout.CENTER);
        return wrapper;
    }

    // ── Helpers ───────────────────────────────────────────────────────────────
    private JTextField darkField(int cols) {
        JTextField tf = new JTextField(cols);
        tf.setFont(FONT_BODY);
        tf.setForeground(CLR_TEXT);
        tf.setBackground(CLR_CARD2);
        tf.setCaretColor(CLR_ACCENT);
        tf.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(CLR_BORDER, 1, true),
            new EmptyBorder(6, 10, 6, 10)
        ));
        return tf;
    }

    private JButton accentBtn(String text, Color bg) {
        JButton btn = new JButton(text) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getBackground());
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                super.paintComponent(g2);
                g2.dispose();
            }
        };
        btn.setOpaque(false);
        btn.setFont(FONT_BODY);
        btn.setForeground(Color.WHITE);
        btn.setBackground(bg);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setContentAreaFilled(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setBorder(new EmptyBorder(8, 16, 8, 16));
        Color darker = bg.darker();
        btn.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent e) { btn.setBackground(darker); }
            public void mouseExited (java.awt.event.MouseEvent e) { btn.setBackground(bg); }
        });
        return btn;
    }

    private void setButtonsEnabled(boolean enabled) {
        btnAdd.setEnabled(enabled);
        btnUpdate.setEnabled(enabled);
        btnDelete.setEnabled(enabled);
        btnClear.setEnabled(enabled);
    }

    // ── Data Logic ────────────────────────────────────────────────────────────
    private void loadCategories() {
        setButtonsEnabled(false);
        new SwingWorker<List<Category>, Void>() {
            @Override protected List<Category> doInBackground() throws SQLException {
                return categoryDAO.getAllCategories();
            }
            @Override protected void done() {
                try {
                    cachedCategories = get(); // ✅ i-cache para sa filter
                    filterTable();             // ✅ i-apply ang current search
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(ManageCategoriesPanel.this,
                        "Error loading categories: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                } finally { setButtonsEnabled(true); }
            }
        }.execute();
    }

    private void addCategory() {
        String name = txtName.getText().trim();
        if (name.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Category name is required.", "Validation", JOptionPane.WARNING_MESSAGE);
            return;
        }
        String description = txtDescription.getText().trim();
        setButtonsEnabled(false);
        new SwingWorker<Boolean, Void>() {
            @Override protected Boolean doInBackground() throws SQLException {
                if (categoryDAO.categoryNameExists(name, -1)) return null;
                return categoryDAO.addCategory(new Category(name, description));
            }
            @Override protected void done() {
                try {
                    Boolean result = get();
                    if (result == null) {
                        JOptionPane.showMessageDialog(ManageCategoriesPanel.this,
                            "Category name already exists.", "Duplicate", JOptionPane.WARNING_MESSAGE);
                    } else if (result) {
                        JOptionPane.showMessageDialog(ManageCategoriesPanel.this,
                            "Category added!", "Success", JOptionPane.INFORMATION_MESSAGE);
                        loadCategories(); clearForm();
                    }
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(ManageCategoriesPanel.this,
                        "Error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                } finally { setButtonsEnabled(true); }
            }
        }.execute();
    }

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
        String  description = txtDescription.getText().trim();
        boolean active      = chkActive.isSelected();
        int     idToUpdate  = selectedCategoryID;
        setButtonsEnabled(false);
        new SwingWorker<Boolean, Void>() {
            @Override protected Boolean doInBackground() throws SQLException {
                Category c = new Category(name, description);
                c.setCategoryID(idToUpdate);
                c.setActive(active);
                return categoryDAO.updateCategory(c);
            }
            @Override protected void done() {
                try {
                    if (get()) {
                        JOptionPane.showMessageDialog(ManageCategoriesPanel.this,
                            "Category updated!", "Success", JOptionPane.INFORMATION_MESSAGE);
                        loadCategories(); clearForm();
                    }
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(ManageCategoriesPanel.this,
                        "Error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                } finally { setButtonsEnabled(true); }
            }
        }.execute();
    }

    private void deleteCategory() {
        if (selectedCategoryID < 0) {
            JOptionPane.showMessageDialog(this, "Select a category first.", "Warning", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (JOptionPane.showConfirmDialog(this, "Deactivate this category?", "Confirm",
                JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION) return;
        int idToDelete = selectedCategoryID;
        setButtonsEnabled(false);
        new SwingWorker<Void, Void>() {
            @Override protected Void doInBackground() throws SQLException {
                categoryDAO.deleteCategory(idToDelete);
                return null;
            }
            @Override protected void done() {
                try {
                    get();
                    JOptionPane.showMessageDialog(ManageCategoriesPanel.this,
                        "Category deactivated.", "Success", JOptionPane.INFORMATION_MESSAGE);
                    loadCategories(); clearForm();
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(ManageCategoriesPanel.this,
                        "Error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                } finally { setButtonsEnabled(true); }
            }
        }.execute();
    }

    private void populateForm() {
        int row = categoryTable.getSelectedRow();
        if (row < 0) return;
        selectedCategoryID = (int) tableModel.getValueAt(row, 0);
        txtName       .setText((String) tableModel.getValueAt(row, 1));
        txtDescription.setText((String) tableModel.getValueAt(row, 2));
        chkActive.setSelected("Yes".equals(tableModel.getValueAt(row, 3)));
    }

    private void clearForm() {
        selectedCategoryID = -1;
        txtName.setText(""); txtDescription.setText(""); chkActive.setSelected(true);
        categoryTable.clearSelection();
    }
}