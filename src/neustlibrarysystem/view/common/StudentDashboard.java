package neustlibrarysystem.view.common;

import neustlibrarysystem.dao.BookDAO;
import neustlibrarysystem.dao.BorrowDAO;
import neustlibrarysystem.dao.ReservationDAO;
import neustlibrarysystem.model.Book;
import neustlibrarysystem.model.BorrowedRecord;
import neustlibrarysystem.model.Member;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class StudentDashboard extends JFrame {

    // ── Design tokens (mirrored from LibrarianDashboard) ──────────────────────
    private static final Color CLR_PRIMARY     = new Color(0x1B4F8A);
    private static final Color CLR_ACCENT      = new Color(0x5B9BD5);   // blue accent
    private static final Color CLR_BG          = new Color(0xF0F4F8);
    private static final Color CLR_SIDEBAR     = new Color(0x1a1a2e);
    private static final Color CLR_SIDEBAR_SEL = new Color(0x2a2a3e);
    private static final Color CLR_SIDEBAR_HOV = new Color(0x25253a);
    private static final Color CLR_WHITE       = Color.WHITE;

    private static final Font FONT_NAV    = new Font("Segoe UI", Font.BOLD,  13);
    private static final Font FONT_HEADER = new Font("Segoe UI", Font.BOLD,  18);
    private static final Font FONT_BODY   = new Font("Segoe UI", Font.PLAIN, 13);
    private static final Font FONT_LABEL  = new Font("Segoe UI", Font.BOLD,  12);

    private static final int SIDEBAR_EXPANDED  = 220;
    private static final int SIDEBAR_COLLAPSED = 64;

    private static final DateTimeFormatter DT_FMT =
        DateTimeFormatter.ofPattern("MMM dd, yyyy");

    // ── Dependencies ──────────────────────────────────────────────────────────
    private final Member         member;
    private final BookDAO        bookDAO   = new BookDAO();
    private final BorrowDAO      borrowDAO = new BorrowDAO();
    private final ReservationDAO resDAO    = new ReservationDAO();

    // ── Layout ────────────────────────────────────────────────────────────────
    private JPanel     contentPanel;
    private CardLayout cardLayout;
    private JPanel     sidebar;
    private JButton    toggleBtn;
    private boolean    sidebarExpanded = true;
    private Timer      sidebarTimer;
    private int        sidebarTargetWidth;

    private final List<NavEntry> navEntries = new ArrayList<>();
    private JButton activeNavBtn = null;

    // ── Book table ────────────────────────────────────────────────────────────
    private JTextField        searchField;
    private JTable            bookTable;
    private DefaultTableModel bookModel;
    private JButton           reserveBtn;

    // ── History table ─────────────────────────────────────────────────────────
    private JTable            historyTable;
    private DefaultTableModel historyModel;

    // ── Icon types (student subset) ───────────────────────────────────────────
    public enum IconType {
        BOOKS, HISTORY, PROFILE, LOGOUT, USER,
        TOGGLE_CLOSE, TOGGLE_OPEN, SEARCH, RESERVE
    }

    // ── Vector icon factory ───────────────────────────────────────────────────
    public static Icon makeIcon(IconType type, Color color, int size) {
        return new Icon() {
            public int getIconWidth()  { return size; }
            public int getIconHeight() { return size; }
            public void paintIcon(Component c, Graphics g, int x, int y) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                                    RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(color);
                g2.translate(x, y);
                float s = size / 20f;
                g2.setStroke(new BasicStroke(1.7f * s,
                        BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                switch (type) {
                    case BOOKS        -> drawBooks(g2, s);
                    case HISTORY      -> drawHistory(g2, s);
                    case PROFILE      -> drawUser(g2, s);
                    case LOGOUT       -> drawLogout(g2, s);
                    case USER         -> drawUser(g2, s);
                    case TOGGLE_CLOSE -> drawToggleClose(g2, s);
                    case TOGGLE_OPEN  -> drawToggleOpen(g2, s);
                    case SEARCH       -> drawSearch(g2, s);
                    case RESERVE      -> drawReserve(g2, s);
                }
                g2.dispose();
            }
        };
    }

    private static int p(float v, float s) { return Math.round(v * s); }

    private static void drawBooks(Graphics2D g, float s) {
        g.fillRoundRect(p(2,s),p(14,s),p(7,s),p(4,s),p(1,s),p(1,s));
        g.fillRoundRect(p(4,s),p(9,s), p(8,s),p(5,s),p(1,s),p(1,s));
        g.fillRoundRect(p(6,s),p(4,s), p(10,s),p(5,s),p(1,s),p(1,s));
        Color c = g.getColor();
        g.setColor(new Color(c.getRed(),c.getGreen(),c.getBlue(),70));
        g.fillRect(p(4,s),p(14,s),p(1,s),p(4,s));
        g.fillRect(p(6,s),p(9,s),p(1,s),p(5,s));
        g.fillRect(p(8,s),p(4,s),p(1,s),p(5,s));
        g.setColor(c);
    }
    private static void drawHistory(Graphics2D g, float s) {
        // clock / history icon
        g.drawOval(p(2,s),p(2,s),p(16,s),p(16,s));
        g.drawLine(p(10,s),p(5,s),p(10,s),p(10,s));
        g.drawLine(p(10,s),p(10,s),p(14,s),p(13,s));
    }
    private static void drawUser(Graphics2D g, float s) {
        g.fillOval(p(6,s),p(2,s),p(8,s),p(8,s));
        g.fillArc(p(2,s),p(11,s),p(16,s),p(9,s),0,180);
    }
    private static void drawLogout(Graphics2D g, float s) {
        g.drawRoundRect(p(2,s),p(2,s),p(10,s),p(16,s),p(2,s),p(2,s));
        g.fillOval(p(3,s),p(9,s),p(2,s),p(2,s));
        g.drawLine(p(9,s),p(10,s),p(18,s),p(10,s));
        g.fillPolygon(new int[]{p(14,s),p(18,s),p(14,s)},
                      new int[]{p(6,s),p(10,s),p(14,s)}, 3);
    }
    private static void drawToggleClose(Graphics2D g, float s) {
        g.drawLine(p(12,s),p(4,s),p(7,s),p(10,s));
        g.drawLine(p(7,s),p(10,s),p(12,s),p(16,s));
        g.drawLine(p(16,s),p(4,s),p(11,s),p(10,s));
        g.drawLine(p(11,s),p(10,s),p(16,s),p(16,s));
    }
    private static void drawToggleOpen(Graphics2D g, float s) {
        g.drawLine(p(8,s),p(4,s),p(13,s),p(10,s));
        g.drawLine(p(13,s),p(10,s),p(8,s),p(16,s));
        g.drawLine(p(4,s),p(4,s),p(9,s),p(10,s));
        g.drawLine(p(9,s),p(10,s),p(4,s),p(16,s));
    }
    private static void drawSearch(Graphics2D g, float s) {
        g.drawOval(p(2,s),p(2,s),p(12,s),p(12,s));
        g.drawLine(p(12,s),p(12,s),p(18,s),p(18,s));
    }
    private static void drawReserve(Graphics2D g, float s) {
        g.fillRoundRect(p(5,s),p(2,s),p(10,s),p(16,s),p(2,s),p(2,s));
        Color c = g.getColor();
        g.setColor(new Color(0x1a,0x1a,0x2e));
        g.fillPolygon(new int[]{p(5,s),p(10,s),p(15,s)},
                      new int[]{p(18,s),p(13,s),p(18,s)}, 3);
        g.setColor(new Color(c.getRed(),c.getGreen(),c.getBlue(),80));
        g.drawLine(p(8,s),p(6,s),p(12,s),p(6,s));
        g.drawLine(p(8,s),p(9,s),p(12,s),p(9,s));
        g.setColor(c);
    }

    // ── Nav entry ─────────────────────────────────────────────────────────────
    private static class NavEntry {
        JButton btn; IconType iconType; String text, card;
        NavEntry(JButton b, IconType i, String t, String c) {
            btn=b; iconType=i; text=t; card=c;
        }
    }

    // ── Constructor ───────────────────────────────────────────────────────────
    public StudentDashboard(Member member) {
        this.member = member;
        setTitle("NEUST Library — Student Portal");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1050, 680);
        setLocationRelativeTo(null);
        buildUI();
        loadBooks();
    }

    // ── UI Builder ────────────────────────────────────────────────────────────
    private void buildUI() {
        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(CLR_BG);
        root.add(buildSidebar(), BorderLayout.WEST);
        root.add(buildTopBar(),  BorderLayout.NORTH);

        cardLayout   = new CardLayout();
        contentPanel = new JPanel(cardLayout);
        contentPanel.setBackground(CLR_BG);
        contentPanel.setBorder(new EmptyBorder(20, 20, 20, 20));

        contentPanel.add(buildBooksPanel(),   "books");
        contentPanel.add(buildHistoryPanel(), "history");
        contentPanel.add(buildProfilePanel(), "profile");

        root.add(contentPanel, BorderLayout.CENTER);
        setContentPane(root);
    }

    // ── Sidebar ───────────────────────────────────────────────────────────────
    private JPanel buildSidebar() {
        sidebar = new JPanel(new BorderLayout());
        sidebar.setBackground(CLR_SIDEBAR);
        sidebar.setPreferredSize(new Dimension(SIDEBAR_EXPANDED, 0));

        // Top: logo + toggle
        JPanel topArea = new JPanel(new BorderLayout());
        topArea.setBackground(new Color(0x12122a));
        topArea.setBorder(new EmptyBorder(20, 0, 16, 0));

        JPanel logoPanel = new JPanel();
        logoPanel.setBackground(new Color(0x12122a));
        logoPanel.setLayout(new BoxLayout(logoPanel, BoxLayout.Y_AXIS));
        JLabel logoIcon = new JLabel(makeIcon(IconType.BOOKS, CLR_ACCENT, 36));
        logoIcon.setAlignmentX(Component.CENTER_ALIGNMENT);
        JLabel sysName = new JLabel("NEUST Library");
        sysName.setFont(new Font("Segoe UI", Font.BOLD, 14));
        sysName.setForeground(Color.WHITE);
        sysName.setAlignmentX(Component.CENTER_ALIGNMENT);
        JLabel roleLabel = new JLabel("Student Portal");
        roleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        roleLabel.setForeground(CLR_ACCENT);
        roleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        logoPanel.add(logoIcon);
        logoPanel.add(Box.createVerticalStrut(6));
        logoPanel.add(sysName);
        logoPanel.add(Box.createVerticalStrut(2));
        logoPanel.add(roleLabel);
        topArea.add(logoPanel, BorderLayout.CENTER);

        toggleBtn = new JButton(makeIcon(IconType.TOGGLE_CLOSE, CLR_ACCENT, 20));
        toggleBtn.setBackground(new Color(0x12122a));
        toggleBtn.setBorderPainted(false);
        toggleBtn.setFocusPainted(false);
        toggleBtn.setContentAreaFilled(false);
        toggleBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        toggleBtn.setPreferredSize(new Dimension(38, 38));
        toggleBtn.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) {
                toggleBtn.setContentAreaFilled(true);
                toggleBtn.setBackground(new Color(0x22224a));
            }
            public void mouseExited(MouseEvent e) {
                toggleBtn.setContentAreaFilled(false);
            }
        });
        toggleBtn.addActionListener(e -> toggleSidebar());
        topArea.add(toggleBtn, BorderLayout.EAST);
        sidebar.add(topArea, BorderLayout.NORTH);

        // Nav buttons
        JPanel navContainer = new JPanel();
        navContainer.setBackground(CLR_SIDEBAR);
        navContainer.setLayout(new BoxLayout(navContainer, BoxLayout.Y_AXIS));
        navContainer.setBorder(new EmptyBorder(20, 0, 16, 0));
        addNavBtn(navContainer, IconType.BOOKS,   "Browse Books",   "books");
        addNavBtn(navContainer, IconType.HISTORY, "Borrow History", "history");
        addNavBtn(navContainer, IconType.PROFILE, "My Profile",     "profile");
        sidebar.add(navContainer, BorderLayout.CENTER);

        // Bottom: user info + logout
        JPanel bottomArea = new JPanel();
        bottomArea.setBackground(CLR_SIDEBAR);
        bottomArea.setLayout(new BoxLayout(bottomArea, BoxLayout.Y_AXIS));
        bottomArea.setBorder(new EmptyBorder(0, 0, 12, 0));

        JSeparator sep = new JSeparator();
        sep.setForeground(new Color(0x2e2e4a));
        sep.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));
        bottomArea.add(sep);
        bottomArea.add(Box.createVerticalStrut(10));

        JButton userRow = new JButton();
        userRow.setIcon(makeIcon(IconType.USER, CLR_ACCENT, 18));
        userRow.setText("  " + member.getFirstName());
        userRow.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        userRow.setForeground(new Color(0xccccee));
        userRow.setBackground(CLR_SIDEBAR);
        userRow.setBorderPainted(false); userRow.setFocusPainted(false);
        userRow.setContentAreaFilled(false);
        userRow.setHorizontalAlignment(SwingConstants.LEFT);
        userRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        userRow.setAlignmentX(Component.CENTER_ALIGNMENT);
        userRow.setBorder(new EmptyBorder(4, 20, 4, 20));
        userRow.setName("userRow"); userRow.setIconTextGap(6);
        bottomArea.add(userRow);
        bottomArea.add(Box.createVerticalStrut(4));

        JButton logoutBtn = new JButton();
        logoutBtn.setIcon(makeIcon(IconType.LOGOUT, new Color(0xFF8A80), 18));
        logoutBtn.setText("  Logout");
        logoutBtn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        logoutBtn.setForeground(new Color(0xFF8A80));
        logoutBtn.setBackground(new Color(0x22223a));
        logoutBtn.setBorderPainted(false); logoutBtn.setFocusPainted(false);
        logoutBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        logoutBtn.setHorizontalAlignment(SwingConstants.LEFT);
        logoutBtn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        logoutBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        logoutBtn.setBorder(new EmptyBorder(8, 20, 8, 20));
        logoutBtn.setName("logoutBtn"); logoutBtn.setIconTextGap(6);
        logoutBtn.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) {
                logoutBtn.setBackground(new Color(0x4a1010));
            }
            public void mouseExited(MouseEvent e) {
                logoutBtn.setBackground(new Color(0x22223a));
            }
        });
        logoutBtn.addActionListener(e -> logout());
        bottomArea.add(logoutBtn);
        sidebar.add(bottomArea, BorderLayout.SOUTH);

        return sidebar;
    }

    private void addNavBtn(JPanel container, IconType iconType, String text, String card) {
        Color iconNormal = new Color(0xaaaacc);
        JButton btn = new JButton();
        btn.setIcon(makeIcon(iconType, iconNormal, 20));
        btn.setText("  " + text);
        btn.setFont(FONT_NAV);
        btn.setForeground(new Color(0xccccee));
        btn.setBackground(CLR_SIDEBAR);
        btn.setBorderPainted(false); btn.setContentAreaFilled(false);
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setHorizontalAlignment(SwingConstants.LEFT);
        btn.setBorder(new EmptyBorder(10, 20, 10, 20));
        btn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 50));
        btn.setAlignmentX(Component.CENTER_ALIGNMENT);
        btn.setIconTextGap(10);

        navEntries.add(new NavEntry(btn, iconType, text, card));

        btn.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) {
                if (btn != activeNavBtn) {
                    btn.setContentAreaFilled(true);
                    btn.setBackground(CLR_SIDEBAR_HOV);
                }
            }
            public void mouseExited(MouseEvent e) {
                if (btn != activeNavBtn) {
                    btn.setContentAreaFilled(false);
                    btn.setBackground(CLR_SIDEBAR);
                }
            }
        });
        btn.addActionListener(e -> {
            if (activeNavBtn != null) {
                NavEntry prev = navEntries.stream()
                    .filter(ne -> ne.btn == activeNavBtn).findFirst().orElse(null);
                if (prev != null)
                    activeNavBtn.setIcon(makeIcon(prev.iconType, iconNormal, 20));
                activeNavBtn.setContentAreaFilled(false);
                activeNavBtn.setBackground(CLR_SIDEBAR);
                activeNavBtn.setForeground(new Color(0xccccee));
            }
            btn.setIcon(makeIcon(iconType, CLR_ACCENT, 20));
            btn.setContentAreaFilled(true);
            btn.setBackground(CLR_SIDEBAR_SEL);
            btn.setForeground(CLR_ACCENT);
            activeNavBtn = btn;

            cardLayout.show(contentPanel, card);
            if ("history".equals(card)) loadHistory();
        });
        container.add(btn);
        container.add(Box.createVerticalStrut(2));
    }

    // ── Toggle animation (same as LibrarianDashboard) ─────────────────────────
    private void toggleSidebar() {
        sidebarExpanded    = !sidebarExpanded;
        sidebarTargetWidth = sidebarExpanded ? SIDEBAR_EXPANDED : SIDEBAR_COLLAPSED;
        toggleBtn.setIcon(makeIcon(
            sidebarExpanded ? IconType.TOGGLE_CLOSE : IconType.TOGGLE_OPEN,
            CLR_ACCENT, 20));

        for (NavEntry ne : navEntries) {
            ne.btn.setText(sidebarExpanded ? "  " + ne.text : "");
            ne.btn.setHorizontalAlignment(
                sidebarExpanded ? SwingConstants.LEFT : SwingConstants.CENTER);
            ne.btn.setBorder(new EmptyBorder(
                10, sidebarExpanded ? 20 : 0,
                10, sidebarExpanded ? 20 : 0));
        }

        // Update bottom buttons
        JPanel bottomArea = (JPanel) sidebar.getComponent(2);
        for (Component c : bottomArea.getComponents()) {
            if (!(c instanceof JButton jb)) continue;
            if ("userRow".equals(jb.getName())) {
                jb.setText(sidebarExpanded ? "  " + member.getFirstName() : "");
                jb.setHorizontalAlignment(
                    sidebarExpanded ? SwingConstants.LEFT : SwingConstants.CENTER);
                jb.setBorder(new EmptyBorder(
                    4, sidebarExpanded ? 20 : 0,
                    4, sidebarExpanded ? 20 : 0));
            }
            if ("logoutBtn".equals(jb.getName())) {
                jb.setText(sidebarExpanded ? "  Logout" : "");
                jb.setHorizontalAlignment(
                    sidebarExpanded ? SwingConstants.LEFT : SwingConstants.CENTER);
                jb.setBorder(new EmptyBorder(
                    8, sidebarExpanded ? 20 : 0,
                    8, sidebarExpanded ? 20 : 0));
            }
        }

        if (sidebarTimer != null && sidebarTimer.isRunning()) sidebarTimer.stop();
        sidebarTimer = new Timer(12, null);
        sidebarTimer.addActionListener(e -> {
            int w    = sidebar.getPreferredSize().width;
            int step = (sidebarTargetWidth > w) ? 14 : -14;
            w += step;
            boolean done = step > 0 ? w >= sidebarTargetWidth : w <= sidebarTargetWidth;
            if (done) { w = sidebarTargetWidth; sidebarTimer.stop(); }
            sidebar.setPreferredSize(new Dimension(w, 0));
            sidebar.revalidate();
            getContentPane().revalidate();
            getContentPane().repaint();
        });
        sidebarTimer.start();
    }

    // ── Top bar ───────────────────────────────────────────────────────────────
    private JPanel buildTopBar() {
        JPanel bar = new JPanel(new BorderLayout());
        bar.setBackground(new Color(0x12122a));
        bar.setBorder(new EmptyBorder(12, 24, 12, 24));

        JLabel title = new JLabel("Library Management System — Student Portal");
        title.setFont(new Font("Segoe UI", Font.BOLD, 15));
        title.setForeground(Color.WHITE);

        JLabel welcome = new JLabel("Welcome, " + member.getFirstName() + "! 👋");
        welcome.setFont(FONT_BODY);
        welcome.setForeground(CLR_ACCENT);

        bar.add(title,   BorderLayout.WEST);
        bar.add(welcome, BorderLayout.EAST);
        return bar;
    }

    // ── Books Panel ───────────────────────────────────────────────────────────
    private JPanel buildBooksPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 12));
        panel.setBackground(CLR_BG);

        // Header
        JLabel header = new JLabel("Browse Books");
        header.setFont(FONT_HEADER);
        header.setForeground(CLR_PRIMARY);
        header.setBorder(new EmptyBorder(0, 0, 4, 0));

        // Search bar
        JPanel searchBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        searchBar.setBackground(CLR_BG);

        searchField = new JTextField(30);
        searchField.setFont(FONT_BODY);
        searchField.setPreferredSize(new Dimension(300, 34));

        JButton searchBtn = new JButton("  Search");
        searchBtn.setIcon(makeIcon(IconType.SEARCH, Color.WHITE, 16));
        searchBtn.setFont(FONT_BODY);
        searchBtn.setBackground(new Color(0x1B4F8A));
        searchBtn.setForeground(Color.WHITE);
        searchBtn.setFocusPainted(false);
        searchBtn.setBorderPainted(false);
        searchBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        searchBtn.setBorder(new EmptyBorder(6, 14, 6, 14));
        searchBtn.setIconTextGap(6);
        searchBtn.addActionListener(e -> searchBooks());

        JButton clearBtn = new JButton("Show All");
        clearBtn.setFont(FONT_BODY);
        clearBtn.setBackground(new Color(0x2a2a3e));
        clearBtn.setForeground(Color.WHITE);
        clearBtn.setFocusPainted(false);
        clearBtn.setBorderPainted(false);
        clearBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        clearBtn.setBorder(new EmptyBorder(6, 14, 6, 14));
        clearBtn.addActionListener(e -> { searchField.setText(""); loadBooks(); });

        searchBar.add(new JLabel("Search:"));
        searchBar.add(searchField);
        searchBar.add(searchBtn);
        searchBar.add(clearBtn);

        JPanel topSection = new JPanel(new BorderLayout(0, 8));
        topSection.setBackground(CLR_BG);
        topSection.add(header,    BorderLayout.NORTH);
        topSection.add(searchBar, BorderLayout.SOUTH);

        // Table
        String[] cols = {"ID", "Title", "Authors", "Category", "Publisher", "Available", "Location"};
        bookModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        bookTable = new JTable(bookModel);
        styleTable(bookTable);
        bookTable.getColumnModel().getColumn(0).setPreferredWidth(40);
        bookTable.getColumnModel().getColumn(1).setPreferredWidth(220);
        bookTable.getColumnModel().getColumn(2).setPreferredWidth(160);
        bookTable.getColumnModel().getColumn(5).setPreferredWidth(80);

        // Actions
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        actions.setBackground(CLR_BG);
        reserveBtn = new JButton("  Reserve Selected Book");
        reserveBtn.setIcon(makeIcon(IconType.RESERVE, Color.WHITE, 16));
        reserveBtn.setFont(FONT_BODY);
        reserveBtn.setBackground(CLR_PRIMARY);
        reserveBtn.setForeground(Color.WHITE);
        reserveBtn.setFocusPainted(false);
        reserveBtn.setBorderPainted(false);
        reserveBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        reserveBtn.setBorder(new EmptyBorder(8, 18, 8, 18));
        reserveBtn.setIconTextGap(8);
        reserveBtn.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) {
                if (reserveBtn.isEnabled())
                    reserveBtn.setBackground(new Color(0x154070));
            }
            public void mouseExited(MouseEvent e) {
                if (reserveBtn.isEnabled())
                    reserveBtn.setBackground(CLR_PRIMARY);
            }
        });
        reserveBtn.addActionListener(e -> reserveSelectedBook());
        actions.add(reserveBtn);

        panel.add(topSection,             BorderLayout.NORTH);
        panel.add(new JScrollPane(bookTable), BorderLayout.CENTER);
        panel.add(actions,                BorderLayout.SOUTH);
        return panel;
    }

    // ── History Panel ─────────────────────────────────────────────────────────
    private JPanel buildHistoryPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 12));
        panel.setBackground(CLR_BG);

        JLabel title = new JLabel("My Borrow History");
        title.setFont(FONT_HEADER);
        title.setForeground(CLR_PRIMARY);

        String[] cols = {"Borrow ID", "Book Title", "Borrow Date",
                         "Due Date", "Return Date", "Status", "Fine (PHP)"};
        historyModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        historyTable = new JTable(historyModel);
        styleTable(historyTable);

        panel.add(title,                          BorderLayout.NORTH);
        panel.add(new JScrollPane(historyTable),  BorderLayout.CENTER);
        return panel;
    }

    // ── Profile Panel ─────────────────────────────────────────────────────────
    private JPanel buildProfilePanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(CLR_BG);

        JPanel card = new JPanel(new GridBagLayout());
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(0xDDDDDD), 1),
            new EmptyBorder(24, 28, 24, 28)
        ));

        GridBagConstraints gc = new GridBagConstraints();
        gc.fill      = GridBagConstraints.HORIZONTAL;
        gc.insets    = new Insets(6, 0, 6, 0);
        gc.gridwidth = GridBagConstraints.REMAINDER;

        JLabel title = new JLabel("My Profile");
        title.setFont(FONT_HEADER);
        title.setForeground(CLR_PRIMARY);
        card.add(title, gc);

        addProfileRow(card, gc, "Student ID:",     member.getStudentID());
        addProfileRow(card, gc, "Full Name:",      member.getFullName());
        addProfileRow(card, gc, "Email:",          member.getEmail());
        addProfileRow(card, gc, "Course/Program:", member.getCourseProgram());
        addProfileRow(card, gc, "Year Level:",     member.getYearLevel());
        addProfileRow(card, gc, "Contact Number:", member.getContactNumber());

        JButton editBtn = new JButton("Edit Profile");
        editBtn.setFont(FONT_BODY);
        editBtn.setBackground(CLR_PRIMARY);
        editBtn.setForeground(Color.WHITE);
        editBtn.setFocusPainted(false);
        editBtn.setBorderPainted(false);
        editBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        editBtn.setBorder(new EmptyBorder(8, 18, 8, 18));
        editBtn.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) { editBtn.setBackground(new Color(0x154070)); }
            public void mouseExited (MouseEvent e) { editBtn.setBackground(CLR_PRIMARY); }
        });
        editBtn.addActionListener(e -> openEditProfile());
        card.add(editBtn, gc);

        panel.add(card, new GridBagConstraints());
        return panel;
    }

    private void addProfileRow(JPanel panel, GridBagConstraints gc,
                               String label, String value) {
        gc.gridwidth = 1;
        gc.weightx   = 0.4;
        JLabel lbl = new JLabel(label);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 13));
        panel.add(lbl, gc);
        gc.weightx   = 0.6;
        gc.gridwidth = GridBagConstraints.REMAINDER;
        JLabel val = new JLabel(value != null ? value : "—");
        val.setFont(FONT_BODY);
        panel.add(val, gc);
    }

    // ── Data Loaders ──────────────────────────────────────────────────────────
    private void loadBooks() {
        SwingWorker<List<Book>, Void> w = new SwingWorker<>() {
            @Override protected List<Book> doInBackground() { return bookDAO.getAllBooks(); }
            @Override protected void done() {
                try { populateBookTable(get()); } catch (Exception ignored) {}
            }
        };
        w.execute();
    }

    private void searchBooks() {
        String kw = searchField.getText().trim();
        if (kw.isEmpty()) { loadBooks(); return; }
        SwingWorker<List<Book>, Void> w = new SwingWorker<>() {
            @Override protected List<Book> doInBackground() { return bookDAO.searchBooks(kw); }
            @Override protected void done() {
                try { populateBookTable(get()); } catch (Exception ignored) {}
            }
        };
        w.execute();
    }

    private void populateBookTable(List<Book> books) {
        bookModel.setRowCount(0);
        for (Book b : books) {
            String authors = b.getAuthors() == null ? "" :
                b.getAuthors().stream()
                 .map(a -> a.getFullName())
                 .reduce((x, y) -> x + ", " + y)
                 .orElse("");
            bookModel.addRow(new Object[]{
                b.getBookID(), b.getTitle(), authors,
                b.getCategoryName(), b.getPublisherName(),
                b.isAvailable() ? b.getAvailableCopies() + " available" : "Not available",
                b.getShelfLocation()
            });
        }
    }

    private void loadHistory() {
        SwingWorker<List<BorrowedRecord>, Void> w = new SwingWorker<>() {
            @Override protected List<BorrowedRecord> doInBackground() {
                return borrowDAO.getMemberHistory(member.getMemberID());
            }
            @Override protected void done() {
                try {
                    historyModel.setRowCount(0);
                    for (BorrowedRecord br : get()) {
                        historyModel.addRow(new Object[]{
                            br.getBorrowID(),
                            br.getBookTitle() != null ? br.getBookTitle() : "—",
                            br.getBorrowDate()  != null ? br.getBorrowDate().format(DT_FMT)  : "—",
                            br.getDueDate()     != null ? br.getDueDate().format(DT_FMT)     : "—",
                            br.getReturnDate()  != null ? br.getReturnDate().format(DT_FMT)  : "—",
                            br.getStatus() != null ? br.getStatus() : "—",
                            "₱" + (br.getFineAmount() != null
                                    ? br.getFineAmount().toPlainString() : "0.00")
                        });
                    }
                } catch (Exception ignored) {}
            }
        };
        w.execute();
    }

    // ── Actions ───────────────────────────────────────────────────────────────
    private void reserveSelectedBook() {
        int row = bookTable.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Please select a book to reserve.");
            return;
        }
        int    bookID = (int)    bookModel.getValueAt(row, 0);
        String title  = (String) bookModel.getValueAt(row, 1);

        int result = JOptionPane.showConfirmDialog(this,
            "Reserve \"" + title + "\"?",
            "Confirm Reservation", JOptionPane.YES_NO_OPTION);

        if (result == JOptionPane.YES_OPTION) {
            reserveBtn.setEnabled(false);
            reserveBtn.setText("  Reserving...");

            SwingWorker<Boolean, Void> worker = new SwingWorker<>() {
                @Override protected Boolean doInBackground() {
                    return resDAO.createReservation(bookID, member.getMemberID());
                }
                @Override protected void done() {
                    try {
                        if (get()) {
                            JOptionPane.showMessageDialog(
                                StudentDashboard.this,
                                "Reservation submitted successfully!");
                        } else {
                            JOptionPane.showMessageDialog(
                                StudentDashboard.this,
                                "Failed to reserve. You may already have a pending reservation for this book.",
                                "Reservation Failed", JOptionPane.ERROR_MESSAGE);
                        }
                    } catch (Exception ex) {
                        JOptionPane.showMessageDialog(
                            StudentDashboard.this,
                            "Error: " + ex.getMessage(),
                            "Error", JOptionPane.ERROR_MESSAGE);
                    } finally {
                        reserveBtn.setEnabled(true);
                        reserveBtn.setText("  Reserve Selected Book");
                    }
                }
            };
            worker.execute();
        }
    }

    private void openEditProfile() {
        JDialog dialog = new JDialog(this, "Edit Profile", true);
        dialog.setSize(550, 600);
        dialog.setLocationRelativeTo(this);
        dialog.setContentPane(new ProfilePanel(member));
        dialog.setVisible(true);
    }

    private void logout() {
        int confirm = JOptionPane.showConfirmDialog(
            this, "Logout from the system?",
            "Confirm Logout", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            new LoginFrame().setVisible(true);
            dispose();
        }
    }

    // ── Table styling (same as LibrarianDashboard) ────────────────────────────
    private static void styleTable(JTable table) {
        table.setFont(FONT_BODY);
        table.setRowHeight(30);
        table.setSelectionBackground(new Color(0x5B9BD5));
        table.setSelectionForeground(Color.WHITE);
        table.setGridColor(new Color(0xc5d8f0));
        table.setShowVerticalLines(false);
        table.setBackground(Color.WHITE);
        JTableHeader h = table.getTableHeader();
        h.setFont(FONT_LABEL);
        h.setBackground(new Color(0x1a1a2e));
        h.setForeground(Color.WHITE);
        h.setReorderingAllowed(false);
        h.setPreferredSize(new Dimension(0, 36));
    }
}