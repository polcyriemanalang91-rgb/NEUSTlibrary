package neustlibrarysystem.view.admin;

import neustlibrarysystem.dao.PublisherDAO;
import neustlibrarysystem.model.Admin;
import neustlibrarysystem.model.Publisher;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ManagePublishersPanel extends JPanel {

    // ── Theme Colors ──────────────────────────────────────────────────────────
    private static final Color BG_DARK        = new Color(0x0F2318);
    private static final Color BG_PANEL       = new Color(0x1A3527);
    private static final Color BG_INPUT       = new Color(0x132B1E);
    private static final Color ACCENT         = new Color(0x2ECC71);
    private static final Color ACCENT_DIM     = new Color(0x1E4D30);
    private static final Color TEXT_PRIMARY   = new Color(0xE8F5EE);
    private static final Color TEXT_SECONDARY = new Color(0x8BBB9E);
    private static final Color TEXT_MUTED     = new Color(0x4D7A5E);
    private static final Color COL_BORDER     = new Color(46, 204, 113, 38);
    private static final Color ROW_SELECTED   = new Color(46, 204, 113, 33);

    private static final Color BTN_ADD_BG = new Color(46, 204, 113, 30);
    private static final Color BTN_ADD_FG = new Color(0x2ECC71);
    private static final Color BTN_UPD_BG = new Color(52, 152, 219, 30);
    private static final Color BTN_UPD_FG = new Color(0x3498DB);
    private static final Color BTN_DEL_BG = new Color(231, 76, 60, 30);
    private static final Color BTN_DEL_FG = new Color(0xE74C3C);

    // ── Fields ────────────────────────────────────────────────────────────────
    private final Admin      currentAdmin;
    private final PublisherDAO publisherDAO;

    private JTable            publisherTable;
    private DefaultTableModel tableModel;

    private JTextField txtName, txtAddress, txtEmail, txtPhone, txtSearch;
    private JCheckBox  chkActive;
    private JButton    btnAdd, btnUpdate, btnDelete, btnClear;

    private int            selectedPublisherID = -1;
    private List<Publisher> cachedPublishers   = new ArrayList<>();

    // ── Constructor ───────────────────────────────────────────────────────────
    public ManagePublishersPanel(Admin currentAdmin) {
        this.currentAdmin = currentAdmin;
        this.publisherDAO = new PublisherDAO();
        setBackground(BG_DARK);
        initComponents();
    }

    public void refresh() {
        loadPublishers();
    }

    // ── UI Construction ───────────────────────────────────────────────────────
    private void initComponents() {
        setLayout(new BorderLayout(0, 16));
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        add(buildHeader(), BorderLayout.NORTH);

        JPanel centerPanel = new JPanel(new BorderLayout(0, 8));
        centerPanel.setOpaque(false);
        centerPanel.add(buildSearchBar(), BorderLayout.NORTH);
        centerPanel.add(buildTable(),     BorderLayout.CENTER);
        add(centerPanel, BorderLayout.CENTER);

        add(buildFormCard(), BorderLayout.SOUTH);
    }

    // ── Header ────────────────────────────────────────────────────────────────
    private JPanel buildHeader() {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        p.setOpaque(false);
        p.setBorder(BorderFactory.createEmptyBorder(0, 0, 4, 0));

        JLabel title = new JLabel("Manage ");
        title.setFont(new Font("Segoe UI", Font.BOLD, 22));
        title.setForeground(TEXT_PRIMARY);

        JLabel accent = new JLabel("Publishers");
        accent.setFont(new Font("Segoe UI", Font.BOLD, 22));
        accent.setForeground(ACCENT);

        p.add(title);
        p.add(accent);
        return p;
    }

    // ── Search Bar ────────────────────────────────────────────────────────────
    private JPanel buildSearchBar() {
        JPanel panel = new JPanel(new BorderLayout(8, 0));
        panel.setOpaque(false);
        panel.setBorder(BorderFactory.createEmptyBorder(6, 0, 2, 0));

        JLabel searchIcon = new JLabel("🔍");
        searchIcon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 14));
        searchIcon.setBorder(BorderFactory.createEmptyBorder(0, 4, 0, 4));

        txtSearch = new JTextField();
        txtSearch.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        txtSearch.setForeground(TEXT_PRIMARY);
        txtSearch.setBackground(BG_INPUT);
        txtSearch.setCaretColor(ACCENT);
        txtSearch.putClientProperty("JTextField.placeholderText", "Search by ID or publisher name...");
        txtSearch.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(COL_BORDER, 1, true),
            BorderFactory.createEmptyBorder(7, 10, 7, 10)
        ));

        txtSearch.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e)  { filterTable(); }
            public void removeUpdate(DocumentEvent e)  { filterTable(); }
            public void changedUpdate(DocumentEvent e) { filterTable(); }
        });

        JButton btnClearSearch = new JButton("✕");
        btnClearSearch.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        btnClearSearch.setForeground(TEXT_MUTED);
        btnClearSearch.setBackground(BG_INPUT);
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

        for (Publisher p : cachedPublishers) {
            String name = p.getPublisherName().toLowerCase();
            String id   = String.valueOf(p.getPublisherID());

            if (keyword.isEmpty()
                    || name.contains(keyword)
                    || id.contains(keyword)) {
                tableModel.addRow(new Object[]{
                    p.getPublisherID(), p.getPublisherName(), p.getAddress(),
                    p.getContactEmail(), p.getContactPhone(), p.isActive() ? "Yes" : "No"
                });
            }
        }
    }

    // ── Table ─────────────────────────────────────────────────────────────────
    private JScrollPane buildTable() {
        String[] cols = {"ID", "Name", "Address", "Email", "Phone", "Active"};
        tableModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };

        publisherTable = new JTable(tableModel) {
            @Override
            public Component prepareRenderer(TableCellRenderer r, int row, int col) {
                Component c = super.prepareRenderer(r, row, col);
                boolean sel = isRowSelected(row);
                c.setBackground(sel ? ROW_SELECTED : (row % 2 == 0 ? BG_PANEL : new Color(0x162D20)));
                c.setForeground(col == 1 ? TEXT_PRIMARY : TEXT_SECONDARY);
                if (col == 0) c.setForeground(TEXT_MUTED);
                return c;
            }
        };

        publisherTable.setBackground(BG_PANEL);
        publisherTable.setForeground(TEXT_SECONDARY);
        publisherTable.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        publisherTable.setRowHeight(32);
        publisherTable.setShowGrid(false);
        publisherTable.setIntercellSpacing(new Dimension(0, 0));
        publisherTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        publisherTable.setSelectionBackground(ROW_SELECTED);
        publisherTable.setSelectionForeground(TEXT_PRIMARY);
        publisherTable.setFocusable(false);

        // Active badge column
        publisherTable.getColumnModel().getColumn(5).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object v, boolean sel, boolean foc, int r, int c) {
                JLabel lbl = new JLabel(v != null ? v.toString() : "");
                lbl.setHorizontalAlignment(SwingConstants.CENTER);
                lbl.setFont(new Font("Segoe UI", Font.BOLD, 11));
                boolean yes = "Yes".equals(v);
                lbl.setForeground(yes ? ACCENT : new Color(0xE74C3C));
                lbl.setBackground(sel ? ROW_SELECTED : (r % 2 == 0 ? BG_PANEL : new Color(0x162D20)));
                lbl.setOpaque(true);
                return lbl;
            }
        });

        int[] widths = {50, 200, 180, 190, 130, 60};
        for (int i = 0; i < widths.length; i++)
            publisherTable.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);

        JTableHeader header = publisherTable.getTableHeader();
        header.setBackground(new Color(0x0D1F14));
        header.setForeground(TEXT_MUTED);
        header.setFont(new Font("Segoe UI", Font.BOLD, 11));
        header.setReorderingAllowed(false);
        header.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, COL_BORDER));
        ((DefaultTableCellRenderer) header.getDefaultRenderer()).setHorizontalAlignment(SwingConstants.LEFT);

        publisherTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) populateForm();
        });

        publisherTable.addMouseMotionListener(new MouseMotionAdapter() {
            int lastRow = -1;
            @Override public void mouseMoved(MouseEvent e) {
                int row = publisherTable.rowAtPoint(e.getPoint());
                if (row != lastRow) { lastRow = row; publisherTable.repaint(); }
            }
        });

        JScrollPane scroll = new JScrollPane(publisherTable);
        scroll.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(COL_BORDER, 1),
                BorderFactory.createEmptyBorder(0, 0, 0, 0)
        ));
        scroll.getViewport().setBackground(BG_PANEL);
        scroll.setBackground(BG_PANEL);
        scroll.getVerticalScrollBar().setBackground(BG_PANEL);
        return scroll;
    }

    // ── Form Card ─────────────────────────────────────────────────────────────
    private JPanel buildFormCard() {
        JPanel card = new JPanel(new BorderLayout(0, 14));
        card.setBackground(BG_PANEL);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(COL_BORDER, 1),
                BorderFactory.createEmptyBorder(16, 18, 16, 18)
        ));

        JPanel sectionHeader = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        sectionHeader.setOpaque(false);
        JLabel dot = new JLabel("●");
        dot.setFont(new Font("Segoe UI", Font.PLAIN, 8));
        dot.setForeground(ACCENT);
        JLabel sectionTitle = new JLabel("Publisher Details");
        sectionTitle.setFont(new Font("Segoe UI", Font.BOLD, 13));
        sectionTitle.setForeground(TEXT_SECONDARY);
        sectionHeader.add(dot);
        sectionHeader.add(sectionTitle);
        card.add(sectionHeader, BorderLayout.NORTH);

        JPanel grid = new JPanel(new GridLayout(2, 4, 14, 10));
        grid.setOpaque(false);

        txtName    = styledField();
        txtAddress = styledField();
        txtEmail   = styledField();
        txtPhone   = styledField();

        grid.add(labeledField("PUBLISHER NAME *", txtName));
        grid.add(labeledField("ADDRESS",          txtAddress));
        grid.add(labeledField("CONTACT EMAIL",    txtEmail));
        grid.add(labeledField("PHONE",            txtPhone));

        chkActive = new JCheckBox("Active");
        chkActive.setSelected(true);
        chkActive.setForeground(TEXT_SECONDARY);
        chkActive.setBackground(BG_PANEL);
        chkActive.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        chkActive.setFocusPainted(false);

        btnAdd    = themedBtn("Add",    BTN_ADD_BG, BTN_ADD_FG);
        btnUpdate = themedBtn("Update", BTN_UPD_BG, BTN_UPD_FG);
        btnDelete = themedBtn("Delete", BTN_DEL_BG, BTN_DEL_FG);
        btnClear  = themedBtn("Clear",  new Color(30, 30, 30, 60), TEXT_MUTED);

        btnAdd.addActionListener(e -> addPublisher());
        btnUpdate.addActionListener(e -> updatePublisher());
        btnDelete.addActionListener(e -> deletePublisher());
        btnClear.addActionListener(e -> clearForm());

        JPanel bottomRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        bottomRow.setOpaque(false);
        bottomRow.add(chkActive);
        bottomRow.add(Box.createHorizontalStrut(10));
        bottomRow.add(btnAdd);
        bottomRow.add(btnUpdate);
        bottomRow.add(btnDelete);
        bottomRow.add(btnClear);

        JPanel formContent = new JPanel(new BorderLayout(0, 10));
        formContent.setOpaque(false);
        formContent.add(grid,      BorderLayout.CENTER);
        formContent.add(bottomRow, BorderLayout.SOUTH);
        card.add(formContent, BorderLayout.CENTER);

        return card;
    }

    // ── Field Helpers ─────────────────────────────────────────────────────────
    private JPanel labeledField(String labelText, JTextField field) {
        JPanel p = new JPanel(new BorderLayout(0, 5));
        p.setOpaque(false);
        JLabel lbl = new JLabel(labelText);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 10));
        lbl.setForeground(TEXT_MUTED);
        p.add(lbl, BorderLayout.NORTH);
        p.add(field, BorderLayout.CENTER);
        return p;
    }

    private JTextField styledField() {
        JTextField tf = new JTextField() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(BG_INPUT);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        tf.setOpaque(false);
        tf.setBackground(BG_INPUT);
        tf.setForeground(TEXT_PRIMARY);
        tf.setCaretColor(ACCENT);
        tf.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        tf.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(COL_BORDER, 1),
                BorderFactory.createEmptyBorder(6, 10, 6, 10)
        ));
        tf.addFocusListener(new FocusAdapter() {
            @Override public void focusGained(FocusEvent e) {
                tf.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(new Color(46, 204, 113, 90), 1),
                        BorderFactory.createEmptyBorder(6, 10, 6, 10)
                ));
            }
            @Override public void focusLost(FocusEvent e) {
                tf.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(COL_BORDER, 1),
                        BorderFactory.createEmptyBorder(6, 10, 6, 10)
                ));
            }
        });
        return tf;
    }

    private JButton themedBtn(String text, Color bg, Color fg) {
        JButton btn = new JButton(text) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getModel().isPressed() ? bg.darker() :
                            getModel().isRollover() ? bg.brighter() : bg);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btn.setForeground(fg);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btn.setOpaque(false);
        btn.setContentAreaFilled(false);
        btn.setFocusPainted(false);
        btn.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(fg.getRed(), fg.getGreen(), fg.getBlue(), 70), 1),
                BorderFactory.createEmptyBorder(7, 18, 7, 18)
        ));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return btn;
    }

    private void setButtonsEnabled(boolean enabled) {
        btnAdd.setEnabled(enabled);
        btnUpdate.setEnabled(enabled);
        btnDelete.setEnabled(enabled);
        btnClear.setEnabled(enabled);
    }

    // ── Data Logic ────────────────────────────────────────────────────────────
    private void loadPublishers() {
        setButtonsEnabled(false);
        new SwingWorker<List<Publisher>, Void>() {
            @Override protected List<Publisher> doInBackground() throws SQLException {
                return publisherDAO.getAllPublishers();
            }
            @Override protected void done() {
                try {
                    cachedPublishers = get(); // ✅ i-cache para sa filter
                    filterTable();             // ✅ i-apply ang current search
                } catch (Exception ex) {
                    showError("Error loading publishers: " + ex.getMessage());
                } finally {
                    setButtonsEnabled(true);
                }
            }
        }.execute();
    }

    private void addPublisher() {
        String name = txtName.getText().trim();
        if (name.isEmpty()) { showWarn("Publisher name is required."); return; }

        Publisher p = new Publisher(name, txtAddress.getText().trim(),
                                    txtEmail.getText().trim(), txtPhone.getText().trim());
        setButtonsEnabled(false);

        new SwingWorker<Boolean, Void>() {
            @Override protected Boolean doInBackground() throws SQLException {
                return publisherDAO.addPublisher(p);
            }
            @Override protected void done() {
                try {
                    if (get()) { showInfo("Publisher added successfully."); loadPublishers(); clearForm(); }
                } catch (Exception ex) { showError("Error: " + ex.getMessage()); }
                finally { setButtonsEnabled(true); }
            }
        }.execute();
    }

    private void updatePublisher() {
        if (selectedPublisherID < 0) { showWarn("Select a publisher first."); return; }
        String name = txtName.getText().trim();
        if (name.isEmpty()) { showWarn("Publisher name is required."); return; }

        Publisher p = new Publisher(name, txtAddress.getText().trim(),
                                    txtEmail.getText().trim(), txtPhone.getText().trim());
        p.setPublisherID(selectedPublisherID);
        p.setActive(chkActive.isSelected());
        setButtonsEnabled(false);

        new SwingWorker<Boolean, Void>() {
            @Override protected Boolean doInBackground() throws SQLException {
                return publisherDAO.updatePublisher(p);
            }
            @Override protected void done() {
                try {
                    if (get()) { showInfo("Publisher updated successfully."); loadPublishers(); clearForm(); }
                } catch (Exception ex) { showError("Error: " + ex.getMessage()); }
                finally { setButtonsEnabled(true); }
            }
        }.execute();
    }

    private void deletePublisher() {
        if (selectedPublisherID < 0) { showWarn("Select a publisher first."); return; }
        if (JOptionPane.showConfirmDialog(this, "Deactivate this publisher?",
                "Confirm", JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION) return;

        int id = selectedPublisherID;
        setButtonsEnabled(false);

        new SwingWorker<Void, Void>() {
            @Override protected Void doInBackground() throws SQLException {
                publisherDAO.deletePublisher(id); return null;
            }
            @Override protected void done() {
                try { get(); showInfo("Publisher deactivated."); loadPublishers(); clearForm(); }
                catch (Exception ex) { showError("Error: " + ex.getMessage()); }
                finally { setButtonsEnabled(true); }
            }
        }.execute();
    }

    private void populateForm() {
        int row = publisherTable.getSelectedRow();
        if (row < 0) return;
        selectedPublisherID = (int) tableModel.getValueAt(row, 0);
        txtName   .setText(str(tableModel.getValueAt(row, 1)));
        txtAddress.setText(str(tableModel.getValueAt(row, 2)));
        txtEmail  .setText(str(tableModel.getValueAt(row, 3)));
        txtPhone  .setText(str(tableModel.getValueAt(row, 4)));
        chkActive.setSelected("Yes".equals(tableModel.getValueAt(row, 5)));
    }

    private void clearForm() {
        selectedPublisherID = -1;
        txtName.setText(""); txtAddress.setText("");
        txtEmail.setText(""); txtPhone.setText("");
        chkActive.setSelected(true);
        publisherTable.clearSelection();
    }

    private String str(Object o) { return o != null ? o.toString() : ""; }

    private void showInfo(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Success", JOptionPane.INFORMATION_MESSAGE);
    }
    private void showWarn(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Warning", JOptionPane.WARNING_MESSAGE);
    }
    private void showError(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Error", JOptionPane.ERROR_MESSAGE);
    }
}