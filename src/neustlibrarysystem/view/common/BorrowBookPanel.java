package view.common;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * BorrowBookPanel - Panel for students to borrow available books
 * Part of the NEUST Library Management System - Student Portal
 */
public class BorrowBookPanel extends JPanel {

    // ── Color Palette (matches NEUST Library dark-green theme) ──────────────
    private static final Color BG_DARK        = new Color(13,  30,  13);
    private static final Color BG_PANEL       = new Color(20,  45,  20);
    private static final Color BG_CARD        = new Color(27,  55,  27);
    private static final Color BG_ROW_ALT     = new Color(22,  48,  22);
    private static final Color BG_SELECTED    = new Color(34,  80,  34);
    private static final Color ACCENT_GREEN   = new Color(76, 175,  80);
    private static final Color ACCENT_YELLOW  = new Color(255, 193,   7);
    private static final Color ACCENT_CYAN    = new Color( 38, 198, 218);
    private static final Color ACCENT_RED     = new Color(239,  83,  80);
    private static final Color TEXT_PRIMARY   = new Color(220, 240, 220);
    private static final Color TEXT_SECONDARY = new Color(130, 170, 130);
    private static final Color BORDER_COLOR   = new Color( 40,  80,  40);

    // ── Fonts ────────────────────────────────────────────────────────────────
    private static final Font FONT_TITLE   = new Font("Segoe UI", Font.BOLD,  22);
    private static final Font FONT_LABEL   = new Font("Segoe UI", Font.BOLD,  13);
    private static final Font FONT_BODY    = new Font("Segoe UI", Font.PLAIN, 13);
    private static final Font FONT_SMALL   = new Font("Segoe UI", Font.PLAIN, 11);
    private static final Font FONT_TABLE   = new Font("Segoe UI", Font.PLAIN, 12);
    private static final Font FONT_BTN     = new Font("Segoe UI", Font.BOLD,  12);

    // ── Components ───────────────────────────────────────────────────────────
    private JTextField      searchField;
    private JComboBox<String> categoryFilter;
    private JTable          bookTable;
    private DefaultTableModel tableModel;
    private JLabel          selectedBookLabel;
    private JSpinner        daysSpinner;
    private JLabel          returnDateLabel;
    private JButton         borrowBtn;
    private JLabel          statusLabel;

    // ── Column names ─────────────────────────────────────────────────────────
    private static final String[] COLUMNS = {
        "Book ID", "Title", "Author", "Category", "Available Copies"
    };

    // ── Sample data (replace with DB calls) ──────────────────────────────────
    private static final Object[][] SAMPLE_DATA = {
        {"BK001", "Introduction to Java Programming", "Herbert Schildt",    "Programming",  3},
        {"BK002", "Data Structures & Algorithms",     "Thomas Cormen",      "Computer Sci", 2},
        {"BK003", "Calculus: Early Transcendentals",  "James Stewart",      "Mathematics",  5},
        {"BK004", "University Physics",               "Young & Freedman",   "Physics",      4},
        {"BK005", "General Chemistry",                "Petrucci et al.",    "Chemistry",    2},
        {"BK006", "Engineering Mechanics",            "Hibbeler R.C.",      "Engineering",  3},
        {"BK007", "The Art of War",                   "Sun Tzu",            "Philosophy",   6},
        {"BK008", "Discrete Mathematics",             "Kenneth Rosen",      "Mathematics",  1},
        {"BK009", "Operating Systems Concepts",       "Silberschatz et al.","Computer Sci", 2},
        {"BK010", "Database System Concepts",         "Korth & Sudarshan",  "Computer Sci", 4},
    };

    // ── Constructor ──────────────────────────────────────────────────────────
    public BorrowBookPanel() {
        setLayout(new BorderLayout(0, 0));
        setBackground(BG_DARK);
        setBorder(new EmptyBorder(24, 28, 24, 28));

        add(buildHeader(),       BorderLayout.NORTH);
        add(buildCenter(),       BorderLayout.CENTER);
    }

    // ════════════════════════════════════════════════════════════════════════
    //  HEADER
    // ════════════════════════════════════════════════════════════════════════
    private JPanel buildHeader() {
        JPanel header = new JPanel(new BorderLayout(16, 8));
        header.setOpaque(false);
        header.setBorder(new EmptyBorder(0, 0, 18, 0));

        // Title block
        JPanel titleBlock = new JPanel(new GridLayout(2, 1, 0, 2));
        titleBlock.setOpaque(false);

        JLabel title = new JLabel("Borrow a Book");
        title.setFont(FONT_TITLE);
        title.setForeground(TEXT_PRIMARY);

        JLabel subtitle = new JLabel("Search and borrow available library books");
        subtitle.setFont(FONT_SMALL);
        subtitle.setForeground(TEXT_SECONDARY);

        titleBlock.add(title);
        titleBlock.add(subtitle);

        // Status label (right side)
        statusLabel = new JLabel(" ");
        statusLabel.setFont(FONT_SMALL);
        statusLabel.setForeground(ACCENT_GREEN);
        statusLabel.setHorizontalAlignment(SwingConstants.RIGHT);

        header.add(titleBlock,   BorderLayout.WEST);
        header.add(statusLabel,  BorderLayout.EAST);

        return header;
    }

    // ════════════════════════════════════════════════════════════════════════
    //  CENTER  (search bar  +  table  +  borrow form)
    // ════════════════════════════════════════════════════════════════════════
    private JPanel buildCenter() {
        JPanel center = new JPanel(new BorderLayout(0, 16));
        center.setOpaque(false);

        center.add(buildSearchBar(),   BorderLayout.NORTH);
        center.add(buildTableCard(),   BorderLayout.CENTER);
        center.add(buildBorrowForm(),  BorderLayout.SOUTH);

        return center;
    }

    // ── Search Bar ───────────────────────────────────────────────────────────
    private JPanel buildSearchBar() {
        JPanel bar = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        bar.setOpaque(false);

        // Search field
        searchField = new JTextField(22);
        styleTextField(searchField, "Search by title or author…");
        searchField.addKeyListener(new KeyAdapter() {
            @Override public void keyReleased(KeyEvent e) { filterTable(); }
        });

        // Category combo
        String[] cats = {"All Categories","Programming","Computer Sci",
                          "Mathematics","Physics","Chemistry","Engineering","Philosophy"};
        categoryFilter = new JComboBox<>(cats);
        styleComboBox(categoryFilter);
        categoryFilter.addActionListener(e -> filterTable());

        // Search button
        JButton searchBtn = styledButton("  Search", ACCENT_GREEN, BG_DARK);
        searchBtn.addActionListener(e -> filterTable());

        // Refresh button
        JButton refreshBtn = styledButton("  Refresh", BG_CARD, TEXT_SECONDARY);
        refreshBtn.addActionListener(e -> resetTable());

        bar.add(new JLabel(colorLabel("Search: ", TEXT_SECONDARY)));
        bar.add(searchField);
        bar.add(Box.createHorizontalStrut(6));
        bar.add(new JLabel(colorLabel("Category: ", TEXT_SECONDARY)));
        bar.add(categoryFilter);
        bar.add(Box.createHorizontalStrut(6));
        bar.add(searchBtn);
        bar.add(refreshBtn);

        return bar;
    }

    // ── Book Table ───────────────────────────────────────────────────────────
    private JScrollPane buildTableCard() {
        tableModel = new DefaultTableModel(SAMPLE_DATA, COLUMNS) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
            @Override public Class<?> getColumnClass(int c) {
                return c == 4 ? Integer.class : String.class;
            }
        };

        bookTable = new JTable(tableModel);
        bookTable.setFont(FONT_TABLE);
        bookTable.setForeground(TEXT_PRIMARY);
        bookTable.setBackground(BG_PANEL);
        bookTable.setSelectionBackground(BG_SELECTED);
        bookTable.setSelectionForeground(ACCENT_GREEN);
        bookTable.setGridColor(BORDER_COLOR);
        bookTable.setRowHeight(32);
        bookTable.setShowVerticalLines(false);
        bookTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        bookTable.setFillsViewportHeight(true);

        // Column widths
        int[] widths = {65, 260, 180, 120, 100};
        for (int i = 0; i < widths.length; i++)
            bookTable.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);

        // Header styling
        JTableHeader hdr = bookTable.getTableHeader();
        hdr.setFont(FONT_LABEL);
        hdr.setBackground(BG_CARD);
        hdr.setForeground(ACCENT_GREEN);
        hdr.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, BORDER_COLOR));
        hdr.setReorderingAllowed(false);

        // Alternating rows
        bookTable.setDefaultRenderer(Object.class,  new AlternatingRowRenderer());
        bookTable.setDefaultRenderer(Integer.class, new AlternatingRowRenderer());

        // Row selection → populate borrow form
        bookTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) onRowSelected();
        });

        JScrollPane scroll = new JScrollPane(bookTable);
        scroll.setBackground(BG_PANEL);
        scroll.getViewport().setBackground(BG_PANEL);
        scroll.setBorder(BorderFactory.createLineBorder(BORDER_COLOR));
        scroll.setPreferredSize(new Dimension(0, 280));

        return scroll;
    }

    // ── Borrow Form ──────────────────────────────────────────────────────────
    private JPanel buildBorrowForm() {
        JPanel card = new JPanel(new GridBagLayout());
        card.setBackground(BG_CARD);
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER_COLOR),
            new EmptyBorder(16, 20, 16, 20)
        ));

        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(4, 8, 4, 8);
        c.anchor = GridBagConstraints.WEST;

        // Row 0 – section title
        c.gridx = 0; c.gridy = 0; c.gridwidth = 4;
        JLabel formTitle = new JLabel("Borrow Request");
        formTitle.setFont(FONT_LABEL);
        formTitle.setForeground(ACCENT_GREEN);
        card.add(formTitle, c);

        // Row 1 – Selected Book
        c.gridwidth = 1; c.gridy = 1;
        c.gridx = 0; card.add(label("Selected Book:"), c);
        c.gridx = 1; c.gridwidth = 3;
        selectedBookLabel = new JLabel("— No book selected —");
        selectedBookLabel.setFont(FONT_BODY);
        selectedBookLabel.setForeground(TEXT_SECONDARY);
        card.add(selectedBookLabel, c);

        // Row 2 – Loan Duration + Return Date
        c.gridwidth = 1; c.gridy = 2;
        c.gridx = 0; card.add(label("Loan Duration:"), c);

        c.gridx = 1;
        SpinnerNumberModel spinModel = new SpinnerNumberModel(7, 1, 30, 1);
        daysSpinner = new JSpinner(spinModel);
        daysSpinner.setPreferredSize(new Dimension(70, 28));
        styleSpinner(daysSpinner);
        daysSpinner.addChangeListener(e -> updateReturnDate());
        card.add(daysSpinner, c);

        c.gridx = 2;
        JLabel daysLbl = new JLabel("days");
        daysLbl.setFont(FONT_BODY); daysLbl.setForeground(TEXT_SECONDARY);
        card.add(daysLbl, c);

        c.gridx = 3;
        returnDateLabel = new JLabel();
        returnDateLabel.setFont(FONT_BODY);
        returnDateLabel.setForeground(ACCENT_CYAN);
        updateReturnDate();
        card.add(returnDateLabel, c);

        // Row 3 – Buttons
        c.gridy = 3; c.gridx = 0; c.gridwidth = 2;
        borrowBtn = styledButton("  Borrow Book", ACCENT_GREEN, BG_DARK);
        borrowBtn.setEnabled(false);
        borrowBtn.addActionListener(e -> confirmBorrow());
        card.add(borrowBtn, c);

        c.gridx = 2; c.gridwidth = 1;
        JButton clearBtn = styledButton("  Clear", BG_PANEL, TEXT_SECONDARY);
        clearBtn.addActionListener(e -> clearSelection());
        card.add(clearBtn, c);

        return card;
    }

    // ════════════════════════════════════════════════════════════════════════
    //  ACTIONS
    // ════════════════════════════════════════════════════════════════════════
    private void filterTable() {
        String query    = searchField.getText().trim().toLowerCase();
        String category = (String) categoryFilter.getSelectedItem();
        boolean allCats = "All Categories".equals(category);

        tableModel.setRowCount(0);
        for (Object[] row : SAMPLE_DATA) {
            String title  = row[1].toString().toLowerCase();
            String author = row[2].toString().toLowerCase();
            String cat    = row[3].toString();
            boolean matchQ = query.isEmpty() || title.contains(query) || author.contains(query);
            boolean matchC = allCats || cat.equals(category);
            if (matchQ && matchC) tableModel.addRow(row);
        }
        statusLabel.setText("Showing " + tableModel.getRowCount() + " book(s)");
    }

    private void resetTable() {
        searchField.setText("");
        categoryFilter.setSelectedIndex(0);
        tableModel.setRowCount(0);
        for (Object[] row : SAMPLE_DATA) tableModel.addRow(row);
        statusLabel.setText("All books loaded.");
    }

    private void onRowSelected() {
        int row = bookTable.getSelectedRow();
        if (row < 0) { clearSelection(); return; }

        String title   = tableModel.getValueAt(row, 1).toString();
        int    avail   = (int) tableModel.getValueAt(row, 4);

        if (avail == 0) {
            selectedBookLabel.setText(title + "  [No copies available]");
            selectedBookLabel.setForeground(ACCENT_RED);
            borrowBtn.setEnabled(false);
            statusLabel.setText("No available copies for: " + title);
        } else {
            selectedBookLabel.setText(title + "  (" + avail + " cop" + (avail > 1 ? "ies" : "y") + " available)");
            selectedBookLabel.setForeground(TEXT_PRIMARY);
            borrowBtn.setEnabled(true);
            statusLabel.setText("Book selected: " + title);
        }
    }

    private void updateReturnDate() {
        int days = (int) daysSpinner.getValue();
        LocalDate returnDate = LocalDate.now().plusDays(days);
        returnDateLabel.setText("Return by: " + returnDate.format(DateTimeFormatter.ofPattern("MMMM dd, yyyy")));
    }

    private void confirmBorrow() {
        int row = bookTable.getSelectedRow();
        if (row < 0) return;

        String bookId = tableModel.getValueAt(row, 0).toString();
        String title  = tableModel.getValueAt(row, 1).toString();
        int    days   = (int) daysSpinner.getValue();
        LocalDate returnDate = LocalDate.now().plusDays(days);

        int choice = JOptionPane.showConfirmDialog(
            this,
            "<html><body style='font-family:Segoe UI;font-size:12px;'>"
            + "<b>Confirm Borrow Request</b><br><br>"
            + "<b>Book:</b> " + title + "<br>"
            + "<b>Book ID:</b> " + bookId + "<br>"
            + "<b>Loan Period:</b> " + days + " day(s)<br>"
            + "<b>Return Date:</b> " + returnDate.format(DateTimeFormatter.ofPattern("MMMM dd, yyyy"))
            + "<br><br>Proceed with borrowing this book?"
            + "</body></html>",
            "Confirm Borrow",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.QUESTION_MESSAGE
        );

        if (choice == JOptionPane.YES_OPTION) {
            // TODO: Call controller/DAO to save borrow record in DB
            // BorrowController.borrowBook(studentId, bookId, days);

            JOptionPane.showMessageDialog(
                this,
                "<html><body style='font-family:Segoe UI;font-size:12px;'>"
                + "✔ Borrow request submitted!<br>"
                + "<b>" + title + "</b> will be ready for pick-up.<br>"
                + "Please return on or before <b>"
                + returnDate.format(DateTimeFormatter.ofPattern("MMMM dd, yyyy")) + "</b>."
                + "</body></html>",
                "Borrow Successful",
                JOptionPane.INFORMATION_MESSAGE
            );

            // Refresh view
            resetTable();
            clearSelection();
            statusLabel.setText("Borrow request submitted for: " + title);
        }
    }

    private void clearSelection() {
        bookTable.clearSelection();
        selectedBookLabel.setText("— No book selected —");
        selectedBookLabel.setForeground(TEXT_SECONDARY);
        borrowBtn.setEnabled(false);
        statusLabel.setText(" ");
    }

    // ════════════════════════════════════════════════════════════════════════
    //  HELPER METHODS – Styling
    // ════════════════════════════════════════════════════════════════════════
    private JLabel label(String text) {
        JLabel l = new JLabel(text);
        l.setFont(FONT_LABEL);
        l.setForeground(TEXT_SECONDARY);
        return l;
    }

    private String colorLabel(String text, Color color) {
        return text; // Plain text label, color set on JLabel directly
    }

    private JLabel styledLabel(String text, Color fg) {
        JLabel l = new JLabel(text);
        l.setFont(FONT_BODY);
        l.setForeground(fg);
        return l;
    }

    private void styleTextField(JTextField tf, String placeholder) {
        tf.setFont(FONT_BODY);
        tf.setForeground(TEXT_PRIMARY);
        tf.setBackground(BG_CARD);
        tf.setCaretColor(ACCENT_GREEN);
        tf.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER_COLOR),
            new EmptyBorder(4, 8, 4, 8)
        ));
        // Placeholder
        tf.setText(placeholder);
        tf.setForeground(TEXT_SECONDARY);
        tf.addFocusListener(new FocusAdapter() {
            @Override public void focusGained(FocusEvent e) {
                if (tf.getText().equals(placeholder)) {
                    tf.setText(""); tf.setForeground(TEXT_PRIMARY);
                }
            }
            @Override public void focusLost(FocusEvent e) {
                if (tf.getText().isEmpty()) {
                    tf.setText(placeholder); tf.setForeground(TEXT_SECONDARY);
                }
            }
        });
    }

    private void styleComboBox(JComboBox<String> cb) {
        cb.setFont(FONT_BODY);
        cb.setForeground(TEXT_PRIMARY);
        cb.setBackground(BG_CARD);
        cb.setBorder(BorderFactory.createLineBorder(BORDER_COLOR));
    }

    private void styleSpinner(JSpinner sp) {
        sp.setFont(FONT_BODY);
        ((JSpinner.DefaultEditor) sp.getEditor()).getTextField().setBackground(BG_CARD);
        ((JSpinner.DefaultEditor) sp.getEditor()).getTextField().setForeground(TEXT_PRIMARY);
        ((JSpinner.DefaultEditor) sp.getEditor()).getTextField().setCaretColor(ACCENT_GREEN);
        sp.setBorder(BorderFactory.createLineBorder(BORDER_COLOR));
    }

    private JButton styledButton(String text, Color bg, Color fg) {
        JButton btn = new JButton(text);
        btn.setFont(FONT_BTN);
        btn.setBackground(bg);
        btn.setForeground(fg.equals(BG_DARK) ? Color.WHITE : fg);
        btn.setFocusPainted(false);
        btn.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER_COLOR),
            new EmptyBorder(6, 14, 6, 14)
        ));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) {
                btn.setBackground(bg.brighter());
            }
            @Override public void mouseExited(MouseEvent e) {
                btn.setBackground(bg);
            }
        });
        return btn;
    }

    // ════════════════════════════════════════════════════════════════════════
    //  INNER CLASS – Custom Table Row Renderer
    // ════════════════════════════════════════════════════════════════════════
    private class AlternatingRowRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(
                JTable table, Object value, boolean isSelected,
                boolean hasFocus, int row, int col) {

            super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, col);
            setFont(FONT_TABLE);
            setBorder(new EmptyBorder(0, 8, 0, 8));

            if (isSelected) {
                setBackground(BG_SELECTED);
                setForeground(ACCENT_GREEN);
            } else {
                setBackground(row % 2 == 0 ? BG_PANEL : BG_ROW_ALT);
                // Highlight unavailable books
                if (col == 4 && value instanceof Integer && (int) value == 0) {
                    setForeground(ACCENT_RED);
                } else {
                    setForeground(TEXT_PRIMARY);
                }
            }

            // Center numeric column
            if (col == 4) setHorizontalAlignment(SwingConstants.CENTER);
            else          setHorizontalAlignment(SwingConstants.LEFT);

            return this;
        }
    }

    // ════════════════════════════════════════════════════════════════════════
    //  MAIN – Quick preview (remove in production)
    // ════════════════════════════════════════════════════════════════════════
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("NEUST Library — Borrow Book");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setSize(900, 640);
            frame.setLocationRelativeTo(null);
            frame.getContentPane().setBackground(new Color(13, 30, 13));
            frame.add(new BorrowBookPanel());
            frame.setVisible(true);
        });
    }
}