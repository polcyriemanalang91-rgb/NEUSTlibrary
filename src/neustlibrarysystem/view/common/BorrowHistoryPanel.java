package neustlibrarysystem.view.common;

import neustlibrarysystem.dao.BorrowDAO;
import neustlibrarysystem.model.BorrowedRecord;
import neustlibrarysystem.model.Member;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class BorrowHistoryPanel extends JPanel {

    private final Member currentMember;
    private JTable historyTable;
    private DefaultTableModel tableModel;
    private BorrowDAO borrowDAO;
    private JPanel summaryPanel;
    private String activeFilter = "All";

    private static final DateTimeFormatter DTF = DateTimeFormatter.ofPattern("MM/dd/yyyy");

    // ── Palette ──────────────────────────────────────────────────────────────
    private static final Color BG_PAGE    = new Color(0xF4F6FB);
    private static final Color BG_CARD    = Color.WHITE;
    private static final Color BG_HEADER  = new Color(0x0D2B55);
    private static final Color BG_THEAD   = new Color(0xEAEEF8);
    private static final Color ACCENT     = new Color(0x2563EB);
    private static final Color TEXT_MAIN  = new Color(0x1E293B);
    private static final Color TEXT_MUTED = new Color(0x64748B);
    private static final Color BORDER_CLR = new Color(0xE2E8F0);

    private static final Color ROW_BORROWED = new Color(0xEFF6FF);
    private static final Color ROW_RETURNED = new Color(0xF0FDF4);
    private static final Color ROW_OVERDUE  = new Color(0xFFF1F2);
    private static final Color ROW_LOST     = new Color(0xFFFBEB);
    private static final Color ROW_STRIPE   = new Color(0xF8FAFC);

    // Animation
    private float panelAlpha = 0f;
    private Timer fadeTimer;

    public BorrowHistoryPanel(Member currentMember) {
        this.currentMember = currentMember;
        this.borrowDAO = new BorrowDAO();
        initComponents();
        loadHistory("All");
        startFadeIn();
    }

    // ── Animations ───────────────────────────────────────────────────────────

    private void startFadeIn() {
        fadeTimer = new Timer(16, null);
        fadeTimer.addActionListener(e -> {
            panelAlpha = Math.min(1f, panelAlpha + 0.05f);
            repaint();
            if (panelAlpha >= 1f) fadeTimer.stop();
        });
        fadeTimer.start();
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, panelAlpha));
        super.paintComponent(g2);
        g2.dispose();
    }

    private void animateSummaryCards() {
        Component[] cards = summaryPanel.getComponents();
        for (int i = 0; i < cards.length; i++) {
            final Component c = cards[i];
            c.setVisible(false);
            Timer t = new Timer(i * 70, null);
            t.setRepeats(false);
            t.addActionListener(e -> { c.setVisible(true); summaryPanel.repaint(); });
            t.start();
        }
    }

    private void animateTable() {
        historyTable.setVisible(false);
        Timer t = new Timer(200, e -> {
            historyTable.setVisible(true);
            ((Timer) e.getSource()).stop();
        });
        t.setRepeats(false);
        t.start();
    }

    // ── UI Construction ──────────────────────────────────────────────────────

    private void initComponents() {
        setLayout(new BorderLayout(0, 0));
        setBackground(BG_PAGE);

        add(buildHeader(),  BorderLayout.NORTH);
        add(buildCenter(),  BorderLayout.CENTER);
        add(buildSummary(), BorderLayout.SOUTH);
    }

    /** Dark navy header bar with title + subtitle */
    private JPanel buildHeader() {
        JPanel header = new JPanel(new BorderLayout()) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(BG_HEADER);
                g2.fillRect(0, 0, getWidth(), getHeight());
                // subtle bottom accent line
                g2.setColor(ACCENT);
                g2.fillRect(0, getHeight() - 3, getWidth(), 3);
                g2.dispose();
            }
        };
        header.setOpaque(false);
        header.setBorder(BorderFactory.createEmptyBorder(18, 22, 18, 22));

        JLabel title = new JLabel("My Borrow History");
        title.setFont(new Font("Segoe UI", Font.BOLD, 22));
        title.setForeground(Color.WHITE);

        JLabel sub = new JLabel("NEUST Library System  ·  " + currentMember.getFullName());
        sub.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        sub.setForeground(new Color(0xA8C4E8));

        JPanel textBox = new JPanel();
        textBox.setLayout(new BoxLayout(textBox, BoxLayout.Y_AXIS));
        textBox.setOpaque(false);
        textBox.add(title);
        textBox.add(Box.createVerticalStrut(3));
        textBox.add(sub);

        header.add(textBox, BorderLayout.WEST);
        return header;
    }

    /** Center: filter toolbar + table card */
    private JPanel buildCenter() {
        JPanel center = new JPanel(new BorderLayout(0, 12));
        center.setOpaque(false);
        center.setBorder(BorderFactory.createEmptyBorder(16, 18, 8, 18));

        center.add(buildFilterBar(), BorderLayout.NORTH);
        center.add(buildTableCard(), BorderLayout.CENTER);
        return center;
    }

    /** Pill-button filter row */
    private JPanel buildFilterBar() {
        JPanel bar = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        bar.setOpaque(false);

        JLabel lbl = new JLabel("Filter:");
        lbl.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lbl.setForeground(TEXT_MUTED);
        bar.add(lbl);

        String[] statuses = {"All", "Borrowed", "Returned", "Overdue", "Lost"};
        for (String s : statuses) {
            bar.add(makePillButton(s));
        }

        // spacer
        bar.add(Box.createHorizontalStrut(12));

        // Refresh button
        JButton refresh = makeIconButton("↺  Refresh");
        refresh.addActionListener(e -> {
            Timer dots = startLoadingDots(refresh, "↺  Refresh");
            new SwingWorker<Void, Void>() {
                @Override protected Void doInBackground() { loadHistory(activeFilter); return null; }
                @Override protected void done() {
                    dots.stop(); refresh.setEnabled(true); refresh.setText("↺  Refresh");
                }
            }.execute();
        });
        bar.add(refresh);
        return bar;
    }

    private JToggleButton makePillButton(String label) {
        Color[] statusAccent = statusColors(label);
        Color bg    = statusAccent[0];
        Color fg    = statusAccent[1];
        Color selBg = statusAccent[2];
        Color selFg = Color.WHITE;

        JToggleButton btn = new JToggleButton(label) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(isSelected() ? selBg : bg);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), getHeight(), getHeight());
                g2.dispose();
                super.paintComponent(g);
            }
            @Override protected void paintBorder(Graphics g) {}
        };
        btn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btn.setForeground(label.equals(activeFilter) ? selFg : fg);
        btn.setSelected(label.equals(activeFilter));
        btn.setContentAreaFilled(false);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setPreferredSize(new Dimension(
                label.equals("All") ? 52 : label.equals("Returned") ? 90 : 82, 30));

        btn.addActionListener(e -> {
            activeFilter = label;
            loadHistory(activeFilter);
            // update all siblings
            Container parent = btn.getParent();
            for (Component c : parent.getComponents()) {
                if (c instanceof JToggleButton tb) {
                    boolean sel = tb.getText().equals(activeFilter);
                    tb.setSelected(sel);
                    Color[] ac = statusColors(tb.getText());
                    tb.setForeground(sel ? Color.WHITE : ac[1]);
                    tb.repaint();
                }
            }
        });
        return btn;
    }

    /** Returns [bgNormal, fgNormal, bgSelected] for each status */
    private Color[] statusColors(String s) {
        return switch (s) {
            case "Borrowed" -> new Color[]{new Color(0xDBEAFE), new Color(0x1D4ED8), new Color(0x2563EB)};
            case "Returned" -> new Color[]{new Color(0xDCFCE7), new Color(0x15803D), new Color(0x16A34A)};
            case "Overdue"  -> new Color[]{new Color(0xFFE4E6), new Color(0xBE123C), new Color(0xE11D48)};
            case "Lost"     -> new Color[]{new Color(0xFEF9C3), new Color(0x854D0E), new Color(0xCA8A04)};
            default         -> new Color[]{new Color(0xE2E8F0), new Color(0x334155), new Color(0x475569)};
        };
    }

    private JButton makeIconButton(String text) {
        JButton btn = new JButton(text) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getModel().isPressed() ? new Color(0xDBEAFE) :
                            getModel().isRollover() ? new Color(0xEFF6FF) : Color.WHITE);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                g2.setColor(BORDER_CLR);
                g2.drawRoundRect(0, 0, getWidth()-1, getHeight()-1, 8, 8);
                g2.dispose();
                super.paintComponent(g);
            }
            @Override protected void paintBorder(Graphics g) {}
        };
        btn.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        btn.setForeground(TEXT_MAIN);
        btn.setContentAreaFilled(false);
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setPreferredSize(new Dimension(110, 30));
        return btn;
    }

    /** Rounded-card wrapper for the table */
    private JPanel buildTableCard() {
        JPanel card = new JPanel(new BorderLayout()) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(BG_CARD);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 14, 14);
                g2.setColor(BORDER_CLR);
                g2.drawRoundRect(0, 0, getWidth()-1, getHeight()-1, 14, 14);
                g2.dispose();
            }
        };
        card.setOpaque(false);

        // ── Table setup ───────────────────────────────────────────────────────
        String[] cols = {"Borrow ID", "Book Title", "ISBN", "Borrow Date", "Due Date", "Return Date", "Status", "Fine (PHP)", "Fine Paid"};
        tableModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };

        historyTable = new JTable(tableModel);
        historyTable.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        historyTable.setRowHeight(34);
        historyTable.setShowGrid(false);
        historyTable.setIntercellSpacing(new Dimension(0, 0));
        historyTable.setSelectionBackground(new Color(0xDBEAFE));
        historyTable.setSelectionForeground(TEXT_MAIN);
        historyTable.setFocusable(false);
        historyTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

        // Column widths
        int[] widths = {75, 220, 130, 95, 95, 95, 90, 95, 75};
        for (int i = 0; i < widths.length; i++)
            historyTable.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);
        historyTable.getColumnModel().getColumn(0).setMaxWidth(80);

        // ── FIX: Table Header with custom renderer ────────────────────────────
        JTableHeader thead = historyTable.getTableHeader();
        thead.setOpaque(true);                              // must be true
        thead.setBackground(BG_THEAD);
        thead.setForeground(TEXT_MAIN);
        thead.setFont(new Font("Segoe UI", Font.BOLD, 13));
        thead.setPreferredSize(new Dimension(thead.getWidth(), 38));
        thead.setReorderingAllowed(false);
        thead.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, BORDER_CLR));

        // Custom header cell renderer — this is the real fix
        thead.setDefaultRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(
                    JTable t, Object val, boolean sel, boolean foc, int row, int col) {

                JLabel lbl = (JLabel) super.getTableCellRendererComponent(
                        t, val, sel, foc, row, col);

                lbl.setFont(new Font("Segoe UI", Font.BOLD, 13));
                lbl.setForeground(TEXT_MAIN);          // #1E293B — dark, always visible
                lbl.setBackground(BG_THEAD);           // #EAEEF8
                lbl.setOpaque(true);
                lbl.setHorizontalAlignment(SwingConstants.LEFT);
                lbl.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createMatteBorder(0, 0, 1, 0, BORDER_CLR),
                    BorderFactory.createEmptyBorder(0, 10, 0, 10)
                ));
                return lbl;
            }
        });
        // ── End Header Fix ────────────────────────────────────────────────────

        // Cell renderer: colour-coded rows + status badge + fine styling
        historyTable.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object val,
                    boolean sel, boolean foc, int row, int col) {

                JLabel lbl = (JLabel) super.getTableCellRendererComponent(t, val, sel, foc, row, col);
                lbl.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 10));
                lbl.setFont(new Font("Segoe UI", Font.PLAIN, 13));
                lbl.setForeground(TEXT_MAIN);

                if (!sel) {
                    String status = tableModel.getValueAt(row, 6) != null
                            ? tableModel.getValueAt(row, 6).toString() : "";
                    lbl.setBackground(switch (status) {
                        case "Borrowed" -> ROW_BORROWED;
                        case "Returned" -> ROW_RETURNED;
                        case "Overdue"  -> ROW_OVERDUE;
                        case "Lost"     -> ROW_LOST;
                        default         -> row % 2 == 0 ? Color.WHITE : ROW_STRIPE;
                    });
                }

                // Status column → pill badge appearance
                if (col == 6 && val != null) {
                    String s = val.toString();
                    Color[] ac = statusColors(s);
                    lbl.setForeground(ac[2]);
                    lbl.setFont(new Font("Segoe UI", Font.BOLD, 12));
                }

                // Fine Paid column
                if (col == 8 && val != null) {
                    boolean paid = "Yes".equalsIgnoreCase(val.toString());
                    lbl.setForeground(paid ? new Color(0x15803D) : new Color(0xBE123C));
                    lbl.setFont(new Font("Segoe UI", Font.BOLD, 12));
                }

                // Fine amount
                if (col == 7 && val != null) {
                    try {
                        double fine = Double.parseDouble(val.toString());
                        if (fine > 0) lbl.setForeground(new Color(0xBE123C));
                        else lbl.setForeground(new Color(0x15803D));
                    } catch (NumberFormatException ignored) {}
                }

                return lbl;
            }
        });

        JScrollPane scroll = new JScrollPane(historyTable);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.getViewport().setBackground(Color.WHITE);
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);

        // Important: keep the column header visible inside the scroll pane
        scroll.setColumnHeaderView(historyTable.getTableHeader());

        card.add(scroll, BorderLayout.CENTER);
        return card;
    }

    /** Bottom summary card strip */
    private JPanel buildSummary() {
        summaryPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        summaryPanel.setOpaque(false);
        summaryPanel.setBorder(BorderFactory.createEmptyBorder(0, 18, 14, 18));
        return summaryPanel;
    }

    // ── Data Loading ─────────────────────────────────────────────────────────

    private void loadHistory(String filter) {
        tableModel.setRowCount(0);
        summaryPanel.removeAll();

        List<BorrowedRecord> records = borrowDAO.getMemberHistory(currentMember.getMemberID());
        int total = 0, active = 0, returned = 0, overdue = 0, lost = 0;

        for (BorrowedRecord r : records) {
            total++;
            switch (r.getStatus() != null ? r.getStatus() : "") {
                case "Borrowed" -> active++;
                case "Returned" -> returned++;
                case "Overdue"  -> overdue++;
                case "Lost"     -> lost++;
            }
            if (!"All".equals(filter) && !filter.equals(r.getStatus())) continue;

            tableModel.addRow(new Object[]{
                r.getBorrowID(),
                r.getBookTitle()  != null ? r.getBookTitle()              : "—",
                r.getIsbn()       != null ? r.getIsbn()                   : "—",
                r.getBorrowDate() != null ? r.getBorrowDate().format(DTF) : "—",
                r.getDueDate()    != null ? r.getDueDate().format(DTF)    : "—",
                r.getReturnDate() != null ? r.getReturnDate().format(DTF) : "—",
                r.getStatus()     != null ? r.getStatus()                 : "—",
                String.format("%.2f", r.getFineAmount().doubleValue()),
                r.isFinePaid() ? "Yes" : "No"
            });
        }

        // Summary mini-cards
        Object[][] summaryItems = {
            {"Total",    total,    new Color(0x1E3A5F), new Color(0xDBEAFE)},
            {"Active",   active,   new Color(0x1D4ED8), new Color(0xEFF6FF)},
            {"Returned", returned, new Color(0x15803D), new Color(0xDCFCE7)},
            {"Overdue",  overdue,  new Color(0xBE123C), new Color(0xFFE4E6)},
            {"Lost",     lost,     new Color(0x92400E), new Color(0xFEF3C7)},
        };

        for (Object[] item : summaryItems) {
            summaryPanel.add(makeSummaryCard(
                (String)  item[0],
                (Integer) item[1],
                (Color)   item[2],
                (Color)   item[3]
            ));
        }

        summaryPanel.revalidate();
        summaryPanel.repaint();
        animateSummaryCards();
        animateTable();
    }

    /** Rounded mini-card for summary counts */
    private JPanel makeSummaryCard(String label, int count, Color fg, Color bg) {
        JPanel card = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(bg);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
                g2.dispose();
            }
        };
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setOpaque(false);
        card.setBorder(BorderFactory.createEmptyBorder(8, 16, 8, 16));
        card.setPreferredSize(new Dimension(110, 58));

        JLabel countLbl = new JLabel(String.valueOf(count));
        countLbl.setFont(new Font("Segoe UI", Font.BOLD, 20));
        countLbl.setForeground(fg);
        countLbl.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel nameLbl = new JLabel(label);
        nameLbl.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        nameLbl.setForeground(fg.darker());
        nameLbl.setAlignmentX(Component.LEFT_ALIGNMENT);

        card.add(countLbl);
        card.add(nameLbl);
        return card;
    }

    private Timer startLoadingDots(JButton btn, String baseText) {
        final String[] dots = {"", ".", "..", "..."};
        final int[] idx = {0};
        btn.setEnabled(false);
        Timer t = new Timer(350, e -> btn.setText(baseText + dots[idx[0]++ % dots.length]));
        t.start();
        return t;
    }
}