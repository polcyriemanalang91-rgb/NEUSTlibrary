package neustlibrarysystem.view.admin;

import neustlibrarysystem.dao.AuthorDAO;
import neustlibrarysystem.model.Admin;
import neustlibrarysystem.model.Author;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class ManageAuthorsPanel extends JPanel {

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

    private final Admin     currentAdmin;
    private final AuthorDAO authorDAO;

    private JTable            table;
    private DefaultTableModel tableModel;

    private JTextField txtFirstName, txtLastName, txtSearch;
    private JTextArea  txtBiography;
    private JCheckBox  chkActive;

    private JButton btnAdd, btnUpdate, btnDelete, btnClear;

    private int          selectedAuthorID = -1;
    private List<Author> cachedAuthors    = new ArrayList<>();

    // ── Constructor ───────────────────────────────────────────────────────────
    public ManageAuthorsPanel(Admin currentAdmin) {
        this.currentAdmin = currentAdmin;
        this.authorDAO    = new AuthorDAO();
        setLayout(new BorderLayout(0, 16));
        setBackground(CLR_BG);
        setBorder(new EmptyBorder(0, 0, 0, 0));
        buildUI();
    }

    public void refresh() {
        loadAuthors();
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

        JLabel title = new JLabel("✍  Manage Authors");
        title.setFont(FONT_TITLE);
        title.setForeground(CLR_TEXT);

        JLabel sub = new JLabel("Add, update, or deactivate authors in the catalog");
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
        txtSearch.putClientProperty("JTextField.placeholderText", "Search by ID or name...");
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

        for (Author a : cachedAuthors) {
            String fullName = (a.getFirstName() + " " + a.getLastName()).toLowerCase();
            String id       = String.valueOf(a.getAuthorID());

            if (keyword.isEmpty()
                    || fullName.contains(keyword)
                    || id.contains(keyword)) {
                tableModel.addRow(new Object[]{
                    a.getAuthorID(), a.getFirstName(), a.getLastName(),
                    a.isActive() ? "Yes" : "No", a.getCreatedAt()
                });
            }
        }
    }

    // ── Table ─────────────────────────────────────────────────────────────────
    private JScrollPane buildTable() {
        String[] cols = {"ID", "First Name", "Last Name", "Active", "Created"};
        tableModel = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };

        table = new JTable(tableModel);
        table.setFont(FONT_BODY);
        table.setForeground(CLR_TEXT);
        table.setBackground(CLR_CARD);
        table.setRowHeight(34);
        table.setSelectionBackground(new Color(0x1e4a2a));
        table.setSelectionForeground(CLR_ACCENT);
        table.setGridColor(CLR_BORDER);
        table.setShowVerticalLines(false);
        table.setIntercellSpacing(new Dimension(0, 1));
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        // ── Header renderer ───────────────────────────────────────────────────
        JTableHeader header = table.getTableHeader();
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
        table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
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

        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) populateForm();
        });

        JScrollPane scroll = new JScrollPane(table);
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

        GridBagConstraints g = new GridBagConstraints();
        g.insets  = new Insets(7, 8, 7, 8);
        g.anchor  = GridBagConstraints.WEST;
        g.fill    = GridBagConstraints.HORIZONTAL;

        txtFirstName = darkField(20);
        txtLastName  = darkField(20);
        txtBiography = new JTextArea(3, 22);
        txtBiography.setLineWrap(true);
        txtBiography.setWrapStyleWord(true);
        txtBiography.setFont(FONT_BODY);
        txtBiography.setForeground(CLR_TEXT);
        txtBiography.setBackground(CLR_CARD2);
        txtBiography.setCaretColor(CLR_ACCENT);
        txtBiography.setBorder(new EmptyBorder(6, 10, 6, 10));

        JScrollPane bioScroll = new JScrollPane(txtBiography);
        bioScroll.setBorder(BorderFactory.createLineBorder(CLR_BORDER, 1, true));

        chkActive = new JCheckBox("Active", true);
        chkActive.setFont(FONT_BODY);
        chkActive.setForeground(CLR_TEXT);
        chkActive.setOpaque(false);

        Object[][] rows = {
            {"First Name:",  txtFirstName},
            {"Last Name:",   txtLastName},
            {"Biography:",   bioScroll},
            {"",             chkActive}
        };

        for (int i = 0; i < rows.length; i++) {
            g.gridx = 0; g.gridy = i; g.weightx = 0;
            if (!((String) rows[i][0]).isEmpty()) {
                JLabel lbl = new JLabel((String) rows[i][0]);
                lbl.setFont(FONT_LABEL);
                lbl.setForeground(CLR_MUTED);
                form.add(lbl, g);
            }
            g.gridx = 1; g.weightx = 1;
            form.add((Component) rows[i][1], g);
        }

        // ── Buttons ───────────────────────────────────────────────────────────
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        btnPanel.setOpaque(false);

        btnAdd    = accentBtn("➕  Add",        new Color(0x1e4a2a));
        btnUpdate = accentBtn("✏  Update",      new Color(0x1a3a5c));
        btnDelete = accentBtn("🚫  Deactivate", new Color(0x5c1a1a));
        btnClear  = accentBtn("↺  Clear",       new Color(0x2a2a2a));

        btnAdd   .addActionListener(e -> addAuthor());
        btnUpdate.addActionListener(e -> updateAuthor());
        btnDelete.addActionListener(e -> deleteAuthor());
        btnClear .addActionListener(e -> clearForm());

        btnPanel.add(btnAdd);
        btnPanel.add(btnUpdate);
        btnPanel.add(btnDelete);
        btnPanel.add(btnClear);

        g.gridx = 0; g.gridy = rows.length; g.gridwidth = 2;
        form.add(btnPanel, g);

        wrapper.add(form, BorderLayout.CENTER);
        return wrapper;
    }

    // ── Field Helpers ─────────────────────────────────────────────────────────
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
    private void loadAuthors() {
        setButtonsEnabled(false);
        new SwingWorker<List<Author>, Void>() {
            protected List<Author> doInBackground() throws Exception {
                return authorDAO.getAllAuthors();
            }
            protected void done() {
                try {
                    cachedAuthors = get(); // ✅ i-cache para sa filter
                    filterTable();         // ✅ i-apply ang current search
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    setButtonsEnabled(true);
                }
            }
        }.execute();
    }

    private void populateForm() {
        int row = table.getSelectedRow();
        if (row < 0) return;
        selectedAuthorID = (int) tableModel.getValueAt(row, 0);
        txtFirstName.setText((String) tableModel.getValueAt(row, 1));
        txtLastName .setText((String) tableModel.getValueAt(row, 2));
        chkActive.setSelected("Yes".equals(tableModel.getValueAt(row, 3)));
    }

    private void addAuthor()    { /* existing logic */ }
    private void updateAuthor() { /* existing logic */ }
    private void deleteAuthor() { /* existing logic */ }

    private void clearForm() {
        selectedAuthorID = -1;
        txtFirstName.setText("");
        txtLastName .setText("");
        txtBiography.setText("");
        chkActive.setSelected(true);
        table.clearSelection();
    }
}