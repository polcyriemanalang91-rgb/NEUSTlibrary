package neustlibrarysystem.view.common;

import neustlibrarysystem.dao.BookDAO;
import neustlibrarysystem.dao.BorrowDAO;
import neustlibrarysystem.dao.ReservationDAO;
import neustlibrarysystem.model.Book;
import neustlibrarysystem.model.BorrowedRecord;
import neustlibrarysystem.model.Member;
import neustlibrarysystem.model.Reservation;   // ← add this import (adjust package if needed)

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class StudentDashboard extends JFrame {

    // ── Design tokens ─────────────────────────────────────────────────────────
    private static final Color CLR_BG          = new Color(0x1a2e1a);
    private static final Color CLR_BG2         = new Color(0x1e331e);
    private static final Color CLR_SIDEBAR     = new Color(0x162816);
    private static final Color CLR_SIDEBAR_SEL = new Color(0x1f3d1f);
    private static final Color CLR_SIDEBAR_HOV = new Color(0x1c371c);
    private static final Color CLR_ACCENT      = new Color(0x4caf50);
    private static final Color CLR_ACCENT2     = new Color(0x00e5ff);
    private static final Color CLR_TOPBAR      = new Color(0x0d1f0d);
    private static final Color CLR_CARD_BG     = new Color(0x1e331e);
    private static final Color CLR_TEXT_DIM    = new Color(0xaaaaaa);
    private static final Color CLR_BORDER      = new Color(0x2e4d2e);

    private static final Color CLR_CARD_GREEN  = new Color(0x4caf50);
    private static final Color CLR_CARD_CYAN   = new Color(0x00e5ff);
    private static final Color CLR_CARD_YELLOW = new Color(0xffc107);
    private static final Color CLR_CARD_RED    = new Color(0xf44336);

    private static final Font FONT_NAV     = new Font("Segoe UI", Font.BOLD,  13);
    private static final Font FONT_HEADER  = new Font("Segoe UI", Font.BOLD,  22);
    private static final Font FONT_SUBHDR  = new Font("Segoe UI", Font.PLAIN, 13);
    private static final Font FONT_BODY    = new Font("Segoe UI", Font.PLAIN, 13);
    private static final Font FONT_LABEL   = new Font("Segoe UI", Font.BOLD,  12);
    private static final Font FONT_STAT    = new Font("Segoe UI", Font.BOLD,  32);
    private static final Font FONT_SECTION = new Font("Segoe UI", Font.BOLD,  10);

    private static final int SIDEBAR_EXPANDED  = 240;
    private static final int SIDEBAR_COLLAPSED = 60;

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
    private boolean    sidebarExpanded   = true;
    private Timer      sidebarTimer;
    private int        sidebarTargetWidth;

    private final List<NavEntry> navEntries  = new ArrayList<>();
    private JButton              activeNavBtn = null;

    // ── Browse Books ──────────────────────────────────────────────────────────
    private JTextField        searchField;
    private JTable            bookTable;
    private DefaultTableModel bookModel;
    private JButton           reserveBtn;

    // ── Borrow Book ───────────────────────────────────────────────────────────
    private JTextField        borrowSearchField;
    private JComboBox<String> borrowCategoryFilter;
    private JTable            borrowTable;
    private DefaultTableModel borrowModel;
    private JLabel            borrowSelectedLabel;
    private JSpinner          borrowDaysSpinner;
    private JLabel            borrowReturnDateLabel;
    private JButton           borrowSubmitBtn;
    private JLabel            borrowStatusLabel;

    // ── Borrow History ────────────────────────────────────────────────────────
    private JTable            historyTable;
    private DefaultTableModel historyModel;

    // ── Reservation History ───────────────────────────────────────────────────
    private JTable            reservationTable;
    private DefaultTableModel reservationModel;
    private JButton           cancelReservationBtn;

    // ── Dashboard stats ───────────────────────────────────────────────────────
    private JLabel statBorrowedVal, statReservedVal, statOverdueVal, statReturnedVal;

    // ── Icon types ────────────────────────────────────────────────────────────
    public enum IconType {
        BOOKS, HISTORY, PROFILE, LOGOUT, USER,
        TOGGLE_CLOSE, TOGGLE_OPEN, SEARCH, RESERVE,
        DASHBOARD, REFRESH, OVERDUE, BORROW
    }

    // ════════════════════════════════════════════════════════════════════════
    //  ICON FACTORY
    // ════════════════════════════════════════════════════════════════════════
    public static Icon makeIcon(IconType type, Color color, int size) {
        return new Icon() {
            public int getIconWidth()  { return size; }
            public int getIconHeight() { return size; }
            public void paintIcon(Component c, Graphics g, int x, int y) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(color);
                g2.translate(x, y);
                float s = size / 20f;
                g2.setStroke(new BasicStroke(1.7f * s, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
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
                    case DASHBOARD    -> drawDashboard(g2, s);
                    case REFRESH      -> drawRefresh(g2, s);
                    case OVERDUE      -> drawOverdue(g2, s);
                    case BORROW       -> drawBorrow(g2, s);
                }
                g2.dispose();
            }
        };
    }

    private static int  p(float v, float s) { return Math.round(v * s); }
    private static void drawBooks(Graphics2D g, float s) {
        g.fillRoundRect(p(2,s),p(14,s),p(7,s),p(4,s),p(1,s),p(1,s));
        g.fillRoundRect(p(4,s),p(9,s),p(8,s),p(5,s),p(1,s),p(1,s));
        g.fillRoundRect(p(6,s),p(4,s),p(10,s),p(5,s),p(1,s),p(1,s));
    }
    private static void drawHistory(Graphics2D g, float s) {
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
        g.setColor(new Color(0x0d,0x1f,0x0d));
        g.fillPolygon(new int[]{p(5,s),p(10,s),p(15,s)},
                      new int[]{p(18,s),p(13,s),p(18,s)}, 3);
        g.setColor(new Color(c.getRed(),c.getGreen(),c.getBlue(),80));
        g.drawLine(p(8,s),p(6,s),p(12,s),p(6,s));
        g.drawLine(p(8,s),p(9,s),p(12,s),p(9,s));
        g.setColor(c);
    }
    private static void drawDashboard(Graphics2D g, float s) {
        g.fillRoundRect(p(2,s),p(2,s),p(7,s),p(7,s),p(2,s),p(2,s));
        g.fillRoundRect(p(11,s),p(2,s),p(7,s),p(7,s),p(2,s),p(2,s));
        g.fillRoundRect(p(2,s),p(11,s),p(7,s),p(7,s),p(2,s),p(2,s));
        g.fillRoundRect(p(11,s),p(11,s),p(7,s),p(7,s),p(2,s),p(2,s));
    }
    private static void drawRefresh(Graphics2D g, float s) {
        g.drawArc(p(3,s),p(3,s),p(14,s),p(14,s),30,270);
        g.fillPolygon(new int[]{p(14,s),p(18,s),p(16,s)},
                      new int[]{p(4,s),p(7,s),p(10,s)}, 3);
    }
    private static void drawOverdue(Graphics2D g, float s) {
        g.fillPolygon(new int[]{p(10,s),p(2,s),p(18,s)},
                      new int[]{p(2,s),p(18,s),p(18,s)}, 3);
        Color c = g.getColor();
        g.setColor(new Color(0x0d,0x1f,0x0d));
        g.fillRect(p(9,s),p(8,s),p(2,s),p(5,s));
        g.fillRect(p(9,s),p(14,s),p(2,s),p(2,s));
        g.setColor(c);
    }
    private static void drawBorrow(Graphics2D g, float s) {
        g.fillRoundRect(p(4,s),p(2,s),p(12,s),p(13,s),p(2,s),p(2,s));
        Color c = g.getColor();
        g.setColor(new Color(0x0d,0x1f,0x0d));
        g.drawLine(p(7,s),p(6,s),p(13,s),p(6,s));
        g.drawLine(p(7,s),p(9,s),p(11,s),p(9,s));
        g.setColor(c);
        g.drawLine(p(10,s),p(16,s),p(10,s),p(19,s));
        g.fillPolygon(new int[]{p(7,s),p(10,s),p(13,s)},
                      new int[]{p(16,s),p(20,s),p(16,s)}, 3);
    }

    // ── Nav entry ─────────────────────────────────────────────────────────────
    private static class NavEntry {
        JButton btn; IconType iconType; String text, card;
        NavEntry(JButton b, IconType i, String t, String c) {
            btn=b; iconType=i; text=t; card=c;
        }
    }

    // ════════════════════════════════════════════════════════════════════════
    //  CONSTRUCTOR
    // ════════════════════════════════════════════════════════════════════════
    public StudentDashboard(Member member) {
        this.member = member;
        setTitle("NEUST Library System — Student Portal");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1150, 720);
        setLocationRelativeTo(null);
        buildUI();
        loadBooks();
        loadBorrowBooks();
    }

    // ════════════════════════════════════════════════════════════════════════
    //  BUILD UI
    // ════════════════════════════════════════════════════════════════════════
    private void buildUI() {
        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(CLR_BG);
        root.add(buildSidebar(), BorderLayout.WEST);

        JPanel mainArea = new JPanel(new BorderLayout());
        mainArea.setBackground(CLR_BG);
        mainArea.add(buildTopBar(), BorderLayout.NORTH);

        cardLayout   = new CardLayout();
        contentPanel = new JPanel(cardLayout);
        contentPanel.setBackground(CLR_BG);
        contentPanel.setBorder(new EmptyBorder(20, 24, 20, 24));

        contentPanel.add(buildDashboardPanel(),       "dashboard");
        contentPanel.add(buildBooksPanel(),           "books");
        contentPanel.add(buildBorrowBookPanel(),      "borrowBook");
        contentPanel.add(buildHistoryPanel(),         "history");
        contentPanel.add(buildReservationHistoryPanel(), "reservations"); // ← NEW
        contentPanel.add(buildProfilePanel(),         "profile");

        mainArea.add(contentPanel, BorderLayout.CENTER);
        root.add(mainArea, BorderLayout.CENTER);
        setContentPane(root);
    }

    // ════════════════════════════════════════════════════════════════════════
    //  SIDEBAR
    // ════════════════════════════════════════════════════════════════════════
    private JPanel buildSidebar() {
        sidebar = new JPanel(new BorderLayout());
        sidebar.setBackground(CLR_SIDEBAR);
        sidebar.setPreferredSize(new Dimension(SIDEBAR_EXPANDED, 0));

        // Top logo area
        JPanel topArea = new JPanel(new BorderLayout());
        topArea.setBackground(CLR_TOPBAR);
        topArea.setBorder(new EmptyBorder(18, 0, 14, 0));

        JPanel logoPanel = new JPanel();
        logoPanel.setBackground(CLR_TOPBAR);
        logoPanel.setLayout(new BoxLayout(logoPanel, BoxLayout.Y_AXIS));

        JLabel logoCircle = new JLabel() {
            @Override protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(0x1e3d1e));
                g2.fillOval(0, 0, 48, 48);
                g2.setColor(CLR_ACCENT);
                g2.setStroke(new BasicStroke(2f));
                g2.fillRoundRect(10, 28, 14, 8, 2, 2);
                g2.fillRoundRect(12, 19, 16, 9, 2, 2);
                g2.fillRoundRect(14, 10, 18, 9, 2, 2);
                g2.dispose();
            }
        };
        logoCircle.setPreferredSize(new Dimension(48, 48));
        logoCircle.setMaximumSize(new Dimension(48, 48));
        logoCircle.setMinimumSize(new Dimension(48, 48));
        logoCircle.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel sysName = new JLabel("NEUST Library");
        sysName.setFont(new Font("Segoe UI", Font.BOLD, 15));
        sysName.setForeground(Color.WHITE);
        sysName.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel roleLabel = new JLabel("Student Portal");
        roleLabel.setFont(new Font("Segoe UI", Font.BOLD, 10));
        roleLabel.setForeground(CLR_ACCENT);
        roleLabel.setBackground(new Color(0x1e3d1e));
        roleLabel.setOpaque(true);
        roleLabel.setBorder(new EmptyBorder(2, 8, 2, 8));
        roleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        logoPanel.add(logoCircle);
        logoPanel.add(Box.createVerticalStrut(8));
        logoPanel.add(sysName);
        logoPanel.add(Box.createVerticalStrut(4));
        logoPanel.add(roleLabel);
        topArea.add(logoPanel, BorderLayout.CENTER);

        toggleBtn = new JButton(makeIcon(IconType.TOGGLE_CLOSE, CLR_ACCENT, 18));
        toggleBtn.setBackground(CLR_TOPBAR);
        toggleBtn.setBorderPainted(false); toggleBtn.setFocusPainted(false);
        toggleBtn.setContentAreaFilled(false);
        toggleBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        toggleBtn.setPreferredSize(new Dimension(34, 34));
        toggleBtn.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) {
                toggleBtn.setContentAreaFilled(true);
                toggleBtn.setBackground(new Color(0x1e3d1e));
            }
            public void mouseExited(MouseEvent e) { toggleBtn.setContentAreaFilled(false); }
        });
        toggleBtn.addActionListener(e -> toggleSidebar());
        topArea.add(toggleBtn, BorderLayout.EAST);
        sidebar.add(topArea, BorderLayout.NORTH);

        // Nav buttons
        JPanel navContainer = new JPanel();
        navContainer.setBackground(CLR_SIDEBAR);
        navContainer.setLayout(new BoxLayout(navContainer, BoxLayout.Y_AXIS));
        navContainer.setBorder(new EmptyBorder(16, 0, 16, 0));

        addSectionLabel(navContainer, "MAIN");
        addNavBtn(navContainer, IconType.DASHBOARD, "Dashboard",        "dashboard");
        addSectionLabel(navContainer, "LIBRARY");
        addNavBtn(navContainer, IconType.BOOKS,     "Browse Books",     "books");
        addNavBtn(navContainer, IconType.BORROW,    "Borrow Book",      "borrowBook");
        addNavBtn(navContainer, IconType.HISTORY,   "Borrow History",   "history");
        addNavBtn(navContainer, IconType.RESERVE,   "My Reservations",  "reservations"); // ← NEW
        addSectionLabel(navContainer, "ACCOUNT");
        addNavBtn(navContainer, IconType.PROFILE,   "My Profile",       "profile");
        sidebar.add(navContainer, BorderLayout.CENTER);

        // Bottom user + logout
        JPanel bottomArea = new JPanel();
        bottomArea.setBackground(CLR_SIDEBAR);
        bottomArea.setLayout(new BoxLayout(bottomArea, BoxLayout.Y_AXIS));
        bottomArea.setBorder(new EmptyBorder(0, 0, 12, 0));

        JSeparator sep = new JSeparator();
        sep.setForeground(new Color(0x2a4a2a));
        sep.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));
        bottomArea.add(sep);
        bottomArea.add(Box.createVerticalStrut(10));

        JPanel userInfoRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 6));
        userInfoRow.setBackground(CLR_SIDEBAR);
        userInfoRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 50));

        JLabel avatar = new JLabel() {
            @Override protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(0x2e5c2e));
                g2.fillOval(0, 0, 32, 32);
                g2.setColor(CLR_ACCENT);
                g2.fillOval(10, 5, 12, 12);
                g2.fillArc(5, 18, 22, 14, 0, 180);
                g2.dispose();
            }
            public Dimension getPreferredSize() { return new Dimension(32, 32); }
        };

        JPanel nameCol = new JPanel();
        nameCol.setBackground(CLR_SIDEBAR);
        nameCol.setLayout(new BoxLayout(nameCol, BoxLayout.Y_AXIS));
        JLabel nameLabel = new JLabel(member.getFirstName() + " " +
            (member.getLastName() != null ? member.getLastName() : ""));
        nameLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        nameLabel.setForeground(Color.WHITE);
        nameLabel.setName("userNameLabel");
        JLabel roleInfo = new JLabel("Student");
        roleInfo.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        roleInfo.setForeground(CLR_ACCENT);
        roleInfo.setName("userRoleLabel");
        nameCol.add(nameLabel);
        nameCol.add(roleInfo);
        userInfoRow.add(avatar);
        userInfoRow.add(nameCol);
        userInfoRow.setName("userInfoRow");
        bottomArea.add(userInfoRow);
        bottomArea.add(Box.createVerticalStrut(4));

        JButton logoutBtn = new JButton();
        logoutBtn.setIcon(makeIcon(IconType.LOGOUT, new Color(0xFF6B6B), 18));
        logoutBtn.setText("  Logout");
        logoutBtn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        logoutBtn.setForeground(new Color(0xFF6B6B));
        logoutBtn.setBackground(new Color(0x2e1a1a));
        logoutBtn.setBorderPainted(false); logoutBtn.setFocusPainted(false);
        logoutBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        logoutBtn.setHorizontalAlignment(SwingConstants.LEFT);
        logoutBtn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        logoutBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        logoutBtn.setBorder(new EmptyBorder(8, 20, 8, 20));
        logoutBtn.setIconTextGap(6);
        logoutBtn.setName("logoutBtn");
        logoutBtn.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) { logoutBtn.setBackground(new Color(0x4a1a1a)); }
            public void mouseExited(MouseEvent e)  { logoutBtn.setBackground(new Color(0x2e1a1a)); }
        });
        logoutBtn.addActionListener(e -> logout());
        bottomArea.add(logoutBtn);
        sidebar.add(bottomArea, BorderLayout.SOUTH);
        return sidebar;
    }

    private void addSectionLabel(JPanel container, String text) {
        if (!sidebarExpanded) return;
        JLabel lbl = new JLabel("  " + text);
        lbl.setFont(FONT_SECTION);
        lbl.setForeground(new Color(0x5a7a5a));
        lbl.setBorder(new EmptyBorder(10, 12, 4, 0));
        lbl.setMaximumSize(new Dimension(Integer.MAX_VALUE, 28));
        lbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        lbl.setName("sectionLabel");
        container.add(lbl);
    }

    private void addNavBtn(JPanel container, IconType iconType, String text, String card) {
        Color iconNormal = new Color(0x6a9a6a);
        JButton btn = new JButton();
        btn.setIcon(makeIcon(iconType, iconNormal, 20));
        btn.setText("  " + text);
        btn.setFont(FONT_NAV);
        btn.setForeground(new Color(0xccddcc));
        btn.setBackground(CLR_SIDEBAR);
        btn.setBorderPainted(false); btn.setContentAreaFilled(false);
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setHorizontalAlignment(SwingConstants.LEFT);
        btn.setBorder(new EmptyBorder(10, 20, 10, 20));
        btn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 48));
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
                activeNavBtn.setForeground(new Color(0xccddcc));
            }
            btn.setIcon(makeIcon(iconType, CLR_ACCENT, 20));
            btn.setContentAreaFilled(true);
            btn.setBackground(CLR_SIDEBAR_SEL);
            btn.setForeground(CLR_ACCENT);
            activeNavBtn = btn;
            cardLayout.show(contentPanel, card);
            if ("history".equals(card))       loadHistory();
            if ("reservations".equals(card))  loadReservations(); // ← NEW
            if ("dashboard".equals(card))     refreshDashboardStats();
            if ("borrowBook".equals(card))    loadBorrowBooks();
        });
        container.add(btn);
        container.add(Box.createVerticalStrut(2));
    }

    // ════════════════════════════════════════════════════════════════════════
    //  SIDEBAR TOGGLE ANIMATION
    // ════════════════════════════════════════════════════════════════════════
    private void toggleSidebar() {
        sidebarExpanded    = !sidebarExpanded;
        sidebarTargetWidth = sidebarExpanded ? SIDEBAR_EXPANDED : SIDEBAR_COLLAPSED;
        toggleBtn.setIcon(makeIcon(
            sidebarExpanded ? IconType.TOGGLE_CLOSE : IconType.TOGGLE_OPEN,
            CLR_ACCENT, 18));

        for (NavEntry ne : navEntries) {
            ne.btn.setText(sidebarExpanded ? "  " + ne.text : "");
            ne.btn.setHorizontalAlignment(
                sidebarExpanded ? SwingConstants.LEFT : SwingConstants.CENTER);
            ne.btn.setBorder(new EmptyBorder(
                10, sidebarExpanded ? 20 : 0,
                10, sidebarExpanded ? 20 : 0));
        }

        JPanel navContainer = (JPanel) sidebar.getComponent(1);
        for (Component c : navContainer.getComponents()) {
            if (c instanceof JLabel lbl && "sectionLabel".equals(lbl.getName()))
                lbl.setVisible(sidebarExpanded);
        }
        JPanel bottomArea = (JPanel) sidebar.getComponent(2);
        for (Component c : bottomArea.getComponents()) {
            if (c instanceof JPanel p && "userInfoRow".equals(p.getName()))
                p.setVisible(sidebarExpanded);
            if (c instanceof JButton jb && "logoutBtn".equals(jb.getName())) {
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
            int step = (sidebarTargetWidth > w) ? 16 : -16;
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

    // ════════════════════════════════════════════════════════════════════════
    //  TOP BAR
    // ════════════════════════════════════════════════════════════════════════
    private JPanel buildTopBar() {
        JPanel bar = new JPanel(new BorderLayout());
        bar.setBackground(CLR_TOPBAR);
        bar.setBorder(new EmptyBorder(14, 24, 14, 24));

        JLabel title = new JLabel("Library Management System  —  Student Portal");
        title.setFont(new Font("Segoe UI", Font.BOLD, 15));
        title.setForeground(Color.WHITE);

        JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 0));
        rightPanel.setBackground(CLR_TOPBAR);
        String today = LocalDate.now().format(DateTimeFormatter.ofPattern("EEEE, MMM dd, yyyy"));
        JLabel dateLabel = new JLabel(today);
        dateLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        dateLabel.setForeground(CLR_TEXT_DIM);
        JLabel badge = new JLabel("● " + member.getFirstName() + " " +
            (member.getLastName() != null ? member.getLastName() : ""));
        badge.setFont(new Font("Segoe UI", Font.BOLD, 13));
        badge.setForeground(CLR_ACCENT2);
        rightPanel.add(dateLabel);
        rightPanel.add(badge);

        bar.add(title,       BorderLayout.WEST);
        bar.add(rightPanel,  BorderLayout.EAST);
        return bar;
    }

    // ════════════════════════════════════════════════════════════════════════
    //  DASHBOARD PANEL
    // ════════════════════════════════════════════════════════════════════════
    private JPanel buildDashboardPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 20));
        panel.setBackground(CLR_BG);

        // Header
        JPanel headerRow = new JPanel(new BorderLayout());
        headerRow.setBackground(CLR_BG);
        JPanel headerText = new JPanel();
        headerText.setBackground(CLR_BG);
        headerText.setLayout(new BoxLayout(headerText, BoxLayout.Y_AXIS));
        JLabel h   = new JLabel("Dashboard Overview");
        h.setFont(FONT_HEADER); h.setForeground(Color.WHITE);
        JLabel sub = new JLabel("Welcome back, " + member.getFirstName() + "!");
        sub.setFont(FONT_SUBHDR); sub.setForeground(CLR_TEXT_DIM);
        headerText.add(h); headerText.add(sub);
        JButton refreshBtn = makeActionButton("  Refresh", IconType.REFRESH, CLR_ACCENT2);
        refreshBtn.addActionListener(e -> refreshDashboardStats());
        headerRow.add(headerText, BorderLayout.WEST);
        headerRow.add(refreshBtn, BorderLayout.EAST);

        // Stat cards
        JPanel cardsRow = new JPanel(new GridLayout(1, 4, 16, 0));
        cardsRow.setBackground(CLR_BG);
        statBorrowedVal = new JLabel("—");
        statReservedVal = new JLabel("—");
        statOverdueVal  = new JLabel("—");
        statReturnedVal = new JLabel("—");
        cardsRow.add(buildStatCard("Currently Borrowed", "Books on loan",        statBorrowedVal, CLR_CARD_YELLOW, IconType.BORROW));
        cardsRow.add(buildStatCard("Reservations",       "Pending reservations", statReservedVal, CLR_CARD_CYAN,   IconType.RESERVE));
        cardsRow.add(buildStatCard("Overdue Books",      "Past return date",     statOverdueVal,  CLR_CARD_RED,    IconType.OVERDUE));
        cardsRow.add(buildStatCard("Books Returned",     "All time",             statReturnedVal, CLR_CARD_GREEN,  IconType.HISTORY));

        // Quick actions — 5 cards now
        JLabel qaLabel = new JLabel("Quick Actions");
        qaLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        qaLabel.setForeground(Color.WHITE);

        JPanel qaGrid = new JPanel(new GridLayout(1, 5, 12, 0));
        qaGrid.setBackground(CLR_BG);
        qaGrid.add(buildQuickActionCard("Browse Books",     "Search library catalog",      IconType.BOOKS,   "books"));
        qaGrid.add(buildQuickActionCard("Borrow Book",      "Borrow available books",      IconType.BORROW,  "borrowBook"));
        qaGrid.add(buildQuickActionCard("Borrow History",   "View your borrowing records", IconType.HISTORY, "history"));
        qaGrid.add(buildQuickActionCard("My Reservations",  "Track reserved books",        IconType.RESERVE, "reservations")); // ← NEW
        qaGrid.add(buildQuickActionCard("My Profile",       "View and edit your info",     IconType.PROFILE, "profile"));

        JPanel center = new JPanel();
        center.setBackground(CLR_BG);
        center.setLayout(new BoxLayout(center, BoxLayout.Y_AXIS));
        center.add(cardsRow);
        center.add(Box.createVerticalStrut(24));
        center.add(qaLabel);
        center.add(Box.createVerticalStrut(12));
        center.add(qaGrid);

        panel.add(headerRow, BorderLayout.NORTH);
        panel.add(center,    BorderLayout.CENTER);

        SwingUtilities.invokeLater(this::refreshDashboardStats);
        return panel;
    }

    private JPanel buildStatCard(String title, String subtitle, JLabel valueLabel,
                                 Color accentColor, IconType iconType) {
        JPanel card = new JPanel(new BorderLayout(10, 0));
        card.setBackground(CLR_CARD_BG);
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(accentColor, 1),
            new EmptyBorder(18, 20, 18, 20)));

        JPanel iconPanel = new JPanel();
        iconPanel.setBackground(CLR_CARD_BG);
        iconPanel.setLayout(new BoxLayout(iconPanel, BoxLayout.Y_AXIS));
        JLabel iconLbl = new JLabel(makeIcon(iconType, accentColor, 28));
        iconLbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        iconPanel.add(iconLbl);
        iconPanel.add(Box.createVerticalGlue());

        JPanel textPanel = new JPanel();
        textPanel.setBackground(CLR_CARD_BG);
        textPanel.setLayout(new BoxLayout(textPanel, BoxLayout.Y_AXIS));
        JLabel subLbl = new JLabel(subtitle);
        subLbl.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        subLbl.setForeground(CLR_TEXT_DIM);
        valueLabel.setFont(FONT_STAT);
        valueLabel.setForeground(accentColor);
        JLabel titleLbl = new JLabel(title);
        titleLbl.setFont(new Font("Segoe UI", Font.BOLD, 13));
        titleLbl.setForeground(accentColor);
        textPanel.add(subLbl); textPanel.add(valueLabel); textPanel.add(titleLbl);

        card.add(iconPanel,  BorderLayout.WEST);
        card.add(textPanel,  BorderLayout.CENTER);
        return card;
    }

    private JPanel buildQuickActionCard(String title, String desc, IconType icon, String card) {
        JPanel p = new JPanel(new BorderLayout(0, 8));
        p.setBackground(CLR_CARD_BG);
        p.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(CLR_BORDER, 1),
            new EmptyBorder(20, 20, 20, 20)));
        p.setCursor(new Cursor(Cursor.HAND_CURSOR));

        JLabel iconLbl  = new JLabel(makeIcon(icon, new Color(0x4a7a4a), 24));
        JLabel titleLbl = new JLabel(title);
        titleLbl.setFont(new Font("Segoe UI", Font.BOLD, 14));
        titleLbl.setForeground(Color.WHITE);
        JLabel descLbl = new JLabel(desc);
        descLbl.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        descLbl.setForeground(CLR_TEXT_DIM);

        JPanel text = new JPanel();
        text.setBackground(CLR_CARD_BG);
        text.setLayout(new BoxLayout(text, BoxLayout.Y_AXIS));
        text.add(titleLbl); text.add(descLbl);

        p.add(iconLbl, BorderLayout.NORTH);
        p.add(text,    BorderLayout.CENTER);

        p.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) {
                p.setBackground(new Color(0x243d24));
                text.setBackground(new Color(0x243d24));
            }
            public void mouseExited(MouseEvent e) {
                p.setBackground(CLR_CARD_BG);
                text.setBackground(CLR_CARD_BG);
            }
            public void mouseClicked(MouseEvent e) {
                cardLayout.show(contentPanel, card);
                selectNavByCard(card);
                if ("history".equals(card))      loadHistory();
                if ("reservations".equals(card)) loadReservations(); // ← NEW
                if ("borrowBook".equals(card))   loadBorrowBooks();
            }
        });
        return p;
    }

    private void selectNavByCard(String card) {
        Color iconNormal = new Color(0x6a9a6a);
        for (NavEntry ne : navEntries) {
            if (ne.card.equals(card)) {
                if (activeNavBtn != null) {
                    NavEntry prev = navEntries.stream()
                        .filter(x -> x.btn == activeNavBtn).findFirst().orElse(null);
                    if (prev != null)
                        activeNavBtn.setIcon(makeIcon(prev.iconType, iconNormal, 20));
                    activeNavBtn.setContentAreaFilled(false);
                    activeNavBtn.setForeground(new Color(0xccddcc));
                }
                ne.btn.setIcon(makeIcon(ne.iconType, CLR_ACCENT, 20));
                ne.btn.setContentAreaFilled(true);
                ne.btn.setBackground(CLR_SIDEBAR_SEL);
                ne.btn.setForeground(CLR_ACCENT);
                activeNavBtn = ne.btn;
                break;
            }
        }
    }

    private void refreshDashboardStats() {
        new SwingWorker<int[], Void>() {
            @Override protected int[] doInBackground() {
                List<BorrowedRecord> history = borrowDAO.getMemberHistory(member.getMemberID());
                int borrowed = 0, overdue = 0, returned = 0;
                LocalDate today = LocalDate.now();
                for (BorrowedRecord r : history) {
                    String status = r.getStatus();
                    if (status == null) continue;
                    if (status.equalsIgnoreCase("borrowed") || status.equalsIgnoreCase("active")) {
                        borrowed++;
                        if (r.getDueDate() != null && r.getDueDate().toLocalDate().isBefore(today)) overdue++;
                    }
                    if (status.equalsIgnoreCase("returned")) returned++;
                }
                int reserved = 0;
                try { reserved = resDAO.getActiveReservationCount(member.getMemberID()); }
                catch (Exception ignored) {}
                return new int[]{ borrowed, reserved, overdue, returned };
            }
            @Override protected void done() {
                try {
                    int[] v = get();
                    statBorrowedVal.setText(String.valueOf(v[0]));
                    statReservedVal.setText(String.valueOf(v[1]));
                    statOverdueVal .setText(String.valueOf(v[2]));
                    statReturnedVal.setText(String.valueOf(v[3]));
                } catch (Exception ignored) {}
            }
        }.execute();
    }

    // ════════════════════════════════════════════════════════════════════════
    //  BROWSE BOOKS PANEL
    // ════════════════════════════════════════════════════════════════════════
    private JPanel buildBooksPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 14));
        panel.setBackground(CLR_BG);

        JPanel headerRow = new JPanel(new BorderLayout());
        headerRow.setBackground(CLR_BG);
        JLabel header = new JLabel("Browse Books");
        header.setFont(FONT_HEADER); header.setForeground(Color.WHITE);
        JLabel sub = new JLabel("Search and reserve books from the catalog");
        sub.setFont(FONT_SUBHDR); sub.setForeground(CLR_TEXT_DIM);
        JPanel hText = new JPanel();
        hText.setBackground(CLR_BG);
        hText.setLayout(new BoxLayout(hText, BoxLayout.Y_AXIS));
        hText.add(header); hText.add(sub);
        headerRow.add(hText, BorderLayout.WEST);

        JPanel searchBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        searchBar.setBackground(CLR_BG);
        searchField = new JTextField(28);
        searchField.setFont(FONT_BODY);
        searchField.setPreferredSize(new Dimension(280, 34));
        searchField.setBackground(CLR_CARD_BG);
        searchField.setForeground(Color.WHITE);
        searchField.setCaretColor(CLR_ACCENT);
        searchField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(CLR_BORDER, 1),
            new EmptyBorder(4, 8, 4, 8)));

        JButton searchBtn = makeActionButton("  Search", IconType.SEARCH, CLR_ACCENT);
        searchBtn.addActionListener(e -> searchBooks());
        searchField.addActionListener(e -> searchBooks());

        JButton clearBtn = new JButton("Show All");
        clearBtn.setFont(FONT_BODY);
        clearBtn.setBackground(new Color(0x2a4a2a));
        clearBtn.setForeground(new Color(0xccddcc));
        clearBtn.setFocusPainted(false); clearBtn.setBorderPainted(false);
        clearBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        clearBtn.setBorder(new EmptyBorder(6, 14, 6, 14));
        clearBtn.addActionListener(e -> { searchField.setText(""); loadBooks(); });

        JLabel searchLbl = new JLabel("Search:");
        searchLbl.setForeground(CLR_TEXT_DIM);
        searchBar.add(searchLbl);
        searchBar.add(searchField);
        searchBar.add(searchBtn);
        searchBar.add(clearBtn);

        JPanel topSection = new JPanel(new BorderLayout(0, 10));
        topSection.setBackground(CLR_BG);
        topSection.add(headerRow,  BorderLayout.NORTH);
        topSection.add(searchBar,  BorderLayout.SOUTH);

        String[] cols = {"ID", "Title", "Authors", "Category", "Publisher", "Availability", "Location"};
        bookModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int row, int col) { return false; }
            @Override public Class<?> getColumnClass(int col) { return String.class; }
        };
        bookTable = new JTable(bookModel);
        styleTable(bookTable);
        bookTable.getColumnModel().getColumn(0).setPreferredWidth(40);
        bookTable.getColumnModel().getColumn(1).setPreferredWidth(230);
        bookTable.getColumnModel().getColumn(2).setPreferredWidth(170);
        bookTable.getColumnModel().getColumn(3).setPreferredWidth(110);
        bookTable.getColumnModel().getColumn(4).setPreferredWidth(120);
        bookTable.getColumnModel().getColumn(5).setPreferredWidth(100);
        bookTable.getColumnModel().getColumn(6).setPreferredWidth(90);

        JScrollPane tableScroll = new JScrollPane(bookTable);
        tableScroll.setBackground(CLR_BG);
        tableScroll.getViewport().setBackground(new Color(0x1a2e1a));
        tableScroll.setBorder(BorderFactory.createLineBorder(CLR_BORDER, 1));

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        actions.setBackground(CLR_BG);
        reserveBtn = makeActionButton("  Reserve Selected Book", IconType.RESERVE, CLR_ACCENT);
        reserveBtn.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) {
                if (reserveBtn.isEnabled()) reserveBtn.setBackground(new Color(0x2e5a2e));
            }
            public void mouseExited(MouseEvent e) {
                if (reserveBtn.isEnabled()) reserveBtn.setBackground(new Color(0x1e4a1e));
            }
        });
        reserveBtn.addActionListener(e -> reserveSelectedBook());
        actions.add(reserveBtn);

        panel.add(topSection,  BorderLayout.NORTH);
        panel.add(tableScroll, BorderLayout.CENTER);
        panel.add(actions,     BorderLayout.SOUTH);
        return panel;
    }

    // ════════════════════════════════════════════════════════════════════════
    //  BORROW BOOK PANEL
    // ════════════════════════════════════════════════════════════════════════
    private JPanel buildBorrowBookPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 14));
        panel.setBackground(CLR_BG);

        JPanel headerRow = new JPanel(new BorderLayout());
        headerRow.setBackground(CLR_BG);
        JPanel hText = new JPanel();
        hText.setBackground(CLR_BG);
        hText.setLayout(new BoxLayout(hText, BoxLayout.Y_AXIS));
        JLabel h   = new JLabel("Borrow a Book");
        h.setFont(FONT_HEADER); h.setForeground(Color.WHITE);
        JLabel sub = new JLabel("Select an available book and submit a borrow request");
        sub.setFont(FONT_SUBHDR); sub.setForeground(CLR_TEXT_DIM);
        hText.add(h); hText.add(sub);

        borrowStatusLabel = new JLabel(" ");
        borrowStatusLabel.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        borrowStatusLabel.setForeground(CLR_ACCENT);
        borrowStatusLabel.setHorizontalAlignment(SwingConstants.RIGHT);

        headerRow.add(hText,              BorderLayout.WEST);
        headerRow.add(borrowStatusLabel,  BorderLayout.EAST);

        JPanel searchBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        searchBar.setBackground(CLR_BG);

        borrowSearchField = new JTextField(22);
        borrowSearchField.setFont(FONT_BODY);
        borrowSearchField.setPreferredSize(new Dimension(240, 34));
        borrowSearchField.setBackground(CLR_CARD_BG);
        borrowSearchField.setForeground(Color.WHITE);
        borrowSearchField.setCaretColor(CLR_ACCENT);
        borrowSearchField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(CLR_BORDER, 1),
            new EmptyBorder(4, 8, 4, 8)));
        borrowSearchField.addKeyListener(new KeyAdapter() {
            @Override public void keyReleased(KeyEvent e) { filterBorrowTable(); }
        });

        String[] categories = {"All Categories", "Programming", "Computer Science",
            "Mathematics", "Physics", "Chemistry", "Engineering", "Philosophy", "Literature"};
        borrowCategoryFilter = new JComboBox<>(categories);
        borrowCategoryFilter.setFont(FONT_BODY);
        borrowCategoryFilter.setBackground(CLR_CARD_BG);
        borrowCategoryFilter.setForeground(Color.WHITE);
        borrowCategoryFilter.setPreferredSize(new Dimension(160, 34));
        borrowCategoryFilter.addActionListener(e -> filterBorrowTable());

        JButton borrowSearchBtn = makeActionButton("  Search", IconType.SEARCH, CLR_ACCENT);
        borrowSearchBtn.addActionListener(e -> filterBorrowTable());

        JButton borrowRefreshBtn = new JButton("  Refresh");
        borrowRefreshBtn.setFont(FONT_BODY);
        borrowRefreshBtn.setBackground(new Color(0x2a4a2a));
        borrowRefreshBtn.setForeground(new Color(0xccddcc));
        borrowRefreshBtn.setFocusPainted(false); borrowRefreshBtn.setBorderPainted(false);
        borrowRefreshBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        borrowRefreshBtn.setBorder(new EmptyBorder(6, 14, 6, 14));
        borrowRefreshBtn.addActionListener(e -> {
            borrowSearchField.setText("");
            borrowCategoryFilter.setSelectedIndex(0);
            loadBorrowBooks();
        });

        JLabel searchLbl = new JLabel("Search:");
        searchLbl.setForeground(CLR_TEXT_DIM);
        JLabel catLbl = new JLabel("Category:");
        catLbl.setForeground(CLR_TEXT_DIM);
        searchBar.add(searchLbl);
        searchBar.add(borrowSearchField);
        searchBar.add(catLbl);
        searchBar.add(borrowCategoryFilter);
        searchBar.add(borrowSearchBtn);
        searchBar.add(borrowRefreshBtn);

        JPanel topSection = new JPanel(new BorderLayout(0, 10));
        topSection.setBackground(CLR_BG);
        topSection.add(headerRow, BorderLayout.NORTH);
        topSection.add(searchBar, BorderLayout.SOUTH);

        String[] cols = {"ID", "Title", "Authors", "Category", "Publisher", "Available", "Location"};
        borrowModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int row, int col) { return false; }
            @Override public Class<?> getColumnClass(int col) { return String.class; }
        };
        borrowTable = new JTable(borrowModel);
        styleTable(borrowTable);
        borrowTable.getColumnModel().getColumn(5).setCellRenderer(new DefaultTableCellRenderer() {
            @Override public Component getTableCellRendererComponent(
                    JTable t, Object v, boolean sel, boolean foc, int r, int c) {
                super.getTableCellRendererComponent(t, v, sel, foc, r, c);
                String val = v != null ? v.toString() : "";
                if (!sel) {
                    if (val.contains("Not available") || val.equals("0 available")) {
                        setForeground(CLR_CARD_RED);
                    } else {
                        setForeground(CLR_CARD_GREEN);
                    }
                    setBackground(new Color(0x1a2e1a));
                }
                setHorizontalAlignment(SwingConstants.CENTER);
                return this;
            }
        });
        borrowTable.getColumnModel().getColumn(0).setPreferredWidth(40);
        borrowTable.getColumnModel().getColumn(1).setPreferredWidth(230);
        borrowTable.getColumnModel().getColumn(2).setPreferredWidth(160);
        borrowTable.getColumnModel().getColumn(3).setPreferredWidth(110);
        borrowTable.getColumnModel().getColumn(4).setPreferredWidth(120);
        borrowTable.getColumnModel().getColumn(5).setPreferredWidth(90);
        borrowTable.getColumnModel().getColumn(6).setPreferredWidth(90);

        borrowTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) onBorrowRowSelected();
        });

        JScrollPane tableScroll = new JScrollPane(borrowTable);
        tableScroll.setBackground(CLR_BG);
        tableScroll.getViewport().setBackground(new Color(0x1a2e1a));
        tableScroll.setBorder(BorderFactory.createLineBorder(CLR_BORDER, 1));

        JPanel formCard = new JPanel(new GridBagLayout());
        formCard.setBackground(CLR_CARD_BG);
        formCard.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(CLR_BORDER, 1),
            new EmptyBorder(14, 20, 14, 20)));

        GridBagConstraints gc = new GridBagConstraints();
        gc.insets  = new Insets(4, 8, 4, 8);
        gc.anchor  = GridBagConstraints.WEST;

        gc.gridx = 0; gc.gridy = 0; gc.gridwidth = 4;
        JLabel formTitle = new JLabel("Borrow Request");
        formTitle.setFont(FONT_LABEL);
        formTitle.setForeground(CLR_ACCENT);
        formCard.add(formTitle, gc);

        gc.gridwidth = 1; gc.gridy = 1; gc.gridx = 0;
        formCard.add(dimLabel("Selected Book:"), gc);
        gc.gridx = 1; gc.gridwidth = 3;
        borrowSelectedLabel = new JLabel("— No book selected —");
        borrowSelectedLabel.setFont(FONT_BODY);
        borrowSelectedLabel.setForeground(CLR_TEXT_DIM);
        formCard.add(borrowSelectedLabel, gc);

        gc.gridwidth = 1; gc.gridy = 2; gc.gridx = 0;
        formCard.add(dimLabel("Loan Duration:"), gc);
        gc.gridx = 1;
        SpinnerNumberModel spinModel = new SpinnerNumberModel(7, 1, 30, 1);
        borrowDaysSpinner = new JSpinner(spinModel);
        borrowDaysSpinner.setPreferredSize(new Dimension(70, 28));
        borrowDaysSpinner.setFont(FONT_BODY);
        ((JSpinner.DefaultEditor) borrowDaysSpinner.getEditor())
            .getTextField().setBackground(CLR_CARD_BG);
        ((JSpinner.DefaultEditor) borrowDaysSpinner.getEditor())
            .getTextField().setForeground(Color.WHITE);
        borrowDaysSpinner.setBorder(BorderFactory.createLineBorder(CLR_BORDER));
        borrowDaysSpinner.addChangeListener(e -> updateBorrowReturnDate());
        formCard.add(borrowDaysSpinner, gc);
        gc.gridx = 2;
        JLabel daysLbl = new JLabel("days");
        daysLbl.setFont(FONT_BODY); daysLbl.setForeground(CLR_TEXT_DIM);
        formCard.add(daysLbl, gc);
        gc.gridx = 3;
        borrowReturnDateLabel = new JLabel();
        borrowReturnDateLabel.setFont(FONT_BODY);
        borrowReturnDateLabel.setForeground(CLR_ACCENT2);
        updateBorrowReturnDate();
        formCard.add(borrowReturnDateLabel, gc);

        gc.gridy = 3; gc.gridx = 0; gc.gridwidth = 2;
        borrowSubmitBtn = makeActionButton("  Borrow Book", IconType.BORROW, CLR_ACCENT);
        borrowSubmitBtn.setEnabled(false);
        borrowSubmitBtn.addActionListener(e -> submitBorrowRequest());
        formCard.add(borrowSubmitBtn, gc);

        gc.gridx = 2; gc.gridwidth = 1;
        JButton clearSelBtn = new JButton("  Clear");
        clearSelBtn.setFont(FONT_BODY);
        clearSelBtn.setBackground(new Color(0x2a4a2a));
        clearSelBtn.setForeground(new Color(0xccddcc));
        clearSelBtn.setFocusPainted(false); clearSelBtn.setBorderPainted(false);
        clearSelBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        clearSelBtn.setBorder(new EmptyBorder(6, 14, 6, 14));
        clearSelBtn.addActionListener(e -> clearBorrowSelection());
        formCard.add(clearSelBtn, gc);

        JPanel centerArea = new JPanel(new BorderLayout(0, 10));
        centerArea.setBackground(CLR_BG);
        centerArea.add(tableScroll, BorderLayout.CENTER);
        centerArea.add(formCard,    BorderLayout.SOUTH);

        panel.add(topSection,  BorderLayout.NORTH);
        panel.add(centerArea,  BorderLayout.CENTER);
        return panel;
    }

    // ════════════════════════════════════════════════════════════════════════
    //  BORROW HISTORY PANEL
    // ════════════════════════════════════════════════════════════════════════
    private JPanel buildHistoryPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 14));
        panel.setBackground(CLR_BG);

        JPanel headerArea = new JPanel();
        headerArea.setBackground(CLR_BG);
        headerArea.setLayout(new BoxLayout(headerArea, BoxLayout.Y_AXIS));
        JLabel title = new JLabel("My Borrow History");
        title.setFont(FONT_HEADER); title.setForeground(Color.WHITE);
        JLabel sub = new JLabel("All your borrowing records");
        sub.setFont(FONT_SUBHDR); sub.setForeground(CLR_TEXT_DIM);
        headerArea.add(title); headerArea.add(sub);

        String[] cols = {"Borrow ID", "Book Title", "Borrow Date",
                         "Due Date", "Return Date", "Status", "Fine (PHP)"};
        historyModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int row, int col) { return false; }
            @Override public Class<?> getColumnClass(int col) { return String.class; }
        };
        historyTable = new JTable(historyModel);
        styleTable(historyTable);
        historyTable.getColumnModel().getColumn(0).setPreferredWidth(80);
        historyTable.getColumnModel().getColumn(1).setPreferredWidth(250);
        historyTable.getColumnModel().getColumn(2).setPreferredWidth(110);
        historyTable.getColumnModel().getColumn(3).setPreferredWidth(110);
        historyTable.getColumnModel().getColumn(4).setPreferredWidth(110);
        historyTable.getColumnModel().getColumn(5).setPreferredWidth(100);
        historyTable.getColumnModel().getColumn(6).setPreferredWidth(90);

        historyTable.getColumnModel().getColumn(5).setCellRenderer(new DefaultTableCellRenderer() {
            @Override public Component getTableCellRendererComponent(
                    JTable t, Object v, boolean sel, boolean foc, int r, int c) {
                super.getTableCellRendererComponent(t, v, sel, foc, r, c);
                String val = v != null ? v.toString() : "";
                if (!sel) {
                    switch (val.toLowerCase()) {
                        case "returned"           -> setForeground(CLR_CARD_GREEN);
                        case "overdue"            -> setForeground(CLR_CARD_RED);
                        case "borrowed", "active" -> setForeground(CLR_CARD_YELLOW);
                        default                   -> setForeground(CLR_TEXT_DIM);
                    }
                    setBackground(new Color(0x1a2e1a));
                }
                return this;
            }
        });

        JScrollPane tableScroll = new JScrollPane(historyTable);
        tableScroll.getViewport().setBackground(new Color(0x1a2e1a));
        tableScroll.setBorder(BorderFactory.createLineBorder(CLR_BORDER, 1));

        panel.add(headerArea,  BorderLayout.NORTH);
        panel.add(tableScroll, BorderLayout.CENTER);
        return panel;
    }

    // ════════════════════════════════════════════════════════════════════════
    //  MY RESERVATIONS PANEL  ← NEW
    // ════════════════════════════════════════════════════════════════════════
    private JPanel buildReservationHistoryPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 14));
        panel.setBackground(CLR_BG);

        // Header
        JPanel headerRow = new JPanel(new BorderLayout());
        headerRow.setBackground(CLR_BG);
        JPanel hText = new JPanel();
        hText.setBackground(CLR_BG);
        hText.setLayout(new BoxLayout(hText, BoxLayout.Y_AXIS));
        JLabel title = new JLabel("My Reservations");
        title.setFont(FONT_HEADER); title.setForeground(Color.WHITE);
        JLabel sub = new JLabel("All books you have reserved");
        sub.setFont(FONT_SUBHDR); sub.setForeground(CLR_TEXT_DIM);
        hText.add(title); hText.add(sub);

        JButton refreshBtn = makeActionButton("  Refresh", IconType.REFRESH, CLR_ACCENT2);
        refreshBtn.addActionListener(e -> loadReservations());
        headerRow.add(hText,       BorderLayout.WEST);
        headerRow.add(refreshBtn,  BorderLayout.EAST);

        // Table
        String[] cols = {
            "Reservation ID", "Book Title", "Authors",
            "Date Reserved", "Expiry Date", "Status"
        };
        reservationModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int row, int col) { return false; }
            @Override public Class<?> getColumnClass(int col) { return String.class; }
        };
        reservationTable = new JTable(reservationModel);
        styleTable(reservationTable);
        reservationTable.getColumnModel().getColumn(0).setPreferredWidth(110);
        reservationTable.getColumnModel().getColumn(1).setPreferredWidth(260);
        reservationTable.getColumnModel().getColumn(2).setPreferredWidth(170);
        reservationTable.getColumnModel().getColumn(3).setPreferredWidth(110);
        reservationTable.getColumnModel().getColumn(4).setPreferredWidth(110);
        reservationTable.getColumnModel().getColumn(5).setPreferredWidth(100);

        // Color-code Status column
        reservationTable.getColumnModel().getColumn(5).setCellRenderer(
            new DefaultTableCellRenderer() {
                @Override public Component getTableCellRendererComponent(
                        JTable t, Object v, boolean sel, boolean foc, int r, int c) {
                    super.getTableCellRendererComponent(t, v, sel, foc, r, c);
                    String val = v != null ? v.toString() : "";
                    if (!sel) {
                        switch (val.toLowerCase()) {
                            case "pending", "active" -> setForeground(CLR_CARD_CYAN);
                            case "fulfilled"         -> setForeground(CLR_CARD_GREEN);
                            case "cancelled"         -> setForeground(CLR_TEXT_DIM);
                            case "expired"           -> setForeground(CLR_CARD_RED);
                            default                  -> setForeground(CLR_TEXT_DIM);
                        }
                        setBackground(new Color(0x1a2e1a));
                    }
                    return this;
                }
            }
        );

        JScrollPane tableScroll = new JScrollPane(reservationTable);
        tableScroll.getViewport().setBackground(new Color(0x1a2e1a));
        tableScroll.setBorder(BorderFactory.createLineBorder(CLR_BORDER, 1));

        // Action bar
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        actions.setBackground(CLR_BG);

        cancelReservationBtn = makeActionButton("  Cancel Reservation", IconType.OVERDUE,
            new Color(0xf44336));
        cancelReservationBtn.setBackground(new Color(0x3d1a1a));
        cancelReservationBtn.setEnabled(false);
        cancelReservationBtn.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) {
                if (cancelReservationBtn.isEnabled())
                    cancelReservationBtn.setBackground(new Color(0x5a1a1a));
            }
            public void mouseExited(MouseEvent e) {
                if (cancelReservationBtn.isEnabled())
                    cancelReservationBtn.setBackground(new Color(0x3d1a1a));
            }
        });
        cancelReservationBtn.addActionListener(e -> cancelSelectedReservation());

        // Enable cancel button only when a pending/active row is selected
        reservationTable.getSelectionModel().addListSelectionListener(e -> {
            if (e.getValueIsAdjusting()) return;
            int row = reservationTable.getSelectedRow();
            if (row < 0) { cancelReservationBtn.setEnabled(false); return; }
            int modelRow = reservationTable.convertRowIndexToModel(row);
            String status = reservationModel.getValueAt(modelRow, 5).toString().toLowerCase();
            cancelReservationBtn.setEnabled(
                status.equals("pending") || status.equals("active"));
        });

        JLabel hint = new JLabel("Select a pending reservation to cancel it.");
        hint.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        hint.setForeground(CLR_TEXT_DIM);

        actions.add(cancelReservationBtn);
        actions.add(hint);

        panel.add(headerRow,   BorderLayout.NORTH);
        panel.add(tableScroll, BorderLayout.CENTER);
        panel.add(actions,     BorderLayout.SOUTH);
        return panel;
    }

    // ════════════════════════════════════════════════════════════════════════
    //  PROFILE PANEL
    // ════════════════════════════════════════════════════════════════════════
    private JPanel buildProfilePanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 14));
        panel.setBackground(CLR_BG);

        JPanel headerArea = new JPanel();
        headerArea.setBackground(CLR_BG);
        headerArea.setLayout(new BoxLayout(headerArea, BoxLayout.Y_AXIS));
        JLabel title = new JLabel("My Profile");
        title.setFont(FONT_HEADER); title.setForeground(Color.WHITE);
        JLabel sub = new JLabel("Your student account information");
        sub.setFont(FONT_SUBHDR); sub.setForeground(CLR_TEXT_DIM);
        headerArea.add(title); headerArea.add(sub);

        JPanel cardWrapper = new JPanel(new FlowLayout(FlowLayout.LEFT));
        cardWrapper.setBackground(CLR_BG);

        JPanel card = new JPanel(new GridBagLayout());
        card.setBackground(CLR_CARD_BG);
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(CLR_BORDER, 1),
            new EmptyBorder(28, 32, 28, 32)));
        card.setPreferredSize(new Dimension(520, 380));

        GridBagConstraints gc = new GridBagConstraints();
        gc.fill = GridBagConstraints.HORIZONTAL;
        gc.insets = new Insets(7, 0, 7, 0);
        gc.gridwidth = GridBagConstraints.REMAINDER;

        addProfileRow(card, gc, "Student ID:",     member.getStudentID());
        addProfileRow(card, gc, "Full Name:",      member.getFullName());
        addProfileRow(card, gc, "Email:",          member.getEmail());
        addProfileRow(card, gc, "Course/Program:", member.getCourseProgram());
        addProfileRow(card, gc, "Year Level:",     member.getYearLevel());
        addProfileRow(card, gc, "Contact Number:", member.getContactNumber());

        gc.insets = new Insets(16, 0, 0, 0);
        JButton editBtn = makeActionButton("Edit Profile", IconType.PROFILE, CLR_ACCENT);
        editBtn.addActionListener(e -> openEditProfile());
        card.add(editBtn, gc);

        cardWrapper.add(card);
        panel.add(headerArea,  BorderLayout.NORTH);
        panel.add(cardWrapper, BorderLayout.CENTER);
        return panel;
    }

    private void addProfileRow(JPanel panel, GridBagConstraints gc,
                               String label, String value) {
        gc.gridwidth = 1; gc.weightx = 0.38;
        JLabel lbl = new JLabel(label);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lbl.setForeground(new Color(0x6aaa6a));
        panel.add(lbl, gc);
        gc.weightx = 0.62; gc.gridwidth = GridBagConstraints.REMAINDER;
        JLabel val = new JLabel(value != null ? value : "—");
        val.setFont(FONT_BODY);
        val.setForeground(Color.WHITE);
        panel.add(val, gc);
    }

    // ════════════════════════════════════════════════════════════════════════
    //  DATA LOADERS
    // ════════════════════════════════════════════════════════════════════════
    private void loadBooks() {
        new SwingWorker<List<Book>, Void>() {
            @Override protected List<Book> doInBackground() { return bookDAO.getAllBooks(); }
            @Override protected void done() {
                try { populateBookTable(get()); } catch (Exception ignored) {}
            }
        }.execute();
    }

    private void searchBooks() {
        String kw = searchField.getText().trim();
        if (kw.isEmpty()) { loadBooks(); return; }
        new SwingWorker<List<Book>, Void>() {
            @Override protected List<Book> doInBackground() { return bookDAO.searchBooks(kw); }
            @Override protected void done() {
                try { populateBookTable(get()); } catch (Exception ignored) {}
            }
        }.execute();
    }

    private void populateBookTable(List<Book> books) {
        bookModel.setRowCount(0);
        for (Book b : books) {
            String authors = b.getAuthors() == null ? "" :
                b.getAuthors().stream().map(a -> a.getFullName())
                    .reduce((x, y) -> x + ", " + y).orElse("");
            bookModel.addRow(new Object[]{
                String.valueOf(b.getBookID()), b.getTitle(), authors,
                b.getCategoryName(), b.getPublisherName(),
                b.isAvailable() ? b.getAvailableCopies() + " available" : "Not available",
                b.getShelfLocation()
            });
        }
    }

    private void loadBorrowBooks() {
        new SwingWorker<List<Book>, Void>() {
            @Override protected List<Book> doInBackground() { return bookDAO.getAllBooks(); }
            @Override protected void done() {
                try { populateBorrowTable(get()); } catch (Exception ignored) {}
            }
        }.execute();
    }

    private void filterBorrowTable() {
        String kw  = borrowSearchField.getText().trim().toLowerCase();
        String cat = (String) borrowCategoryFilter.getSelectedItem();
        boolean allCats = "All Categories".equals(cat);

        new SwingWorker<List<Book>, Void>() {
            @Override protected List<Book> doInBackground() { return bookDAO.getAllBooks(); }
            @Override protected void done() {
                try {
                    List<Book> all = get();
                    borrowModel.setRowCount(0);
                    int shown = 0;
                    for (Book b : all) {
                        boolean matchKw  = kw.isEmpty()
                            || b.getTitle().toLowerCase().contains(kw)
                            || (b.getAuthors() != null && b.getAuthors().stream()
                                .anyMatch(a -> a.getFullName().toLowerCase().contains(kw)));
                        boolean matchCat = allCats
                            || (b.getCategoryName() != null
                                && b.getCategoryName().equalsIgnoreCase(cat));
                        if (matchKw && matchCat) {
                            addBorrowRow(b);
                            shown++;
                        }
                    }
                    borrowStatusLabel.setText("Showing " + shown + " book(s)");
                } catch (Exception ignored) {}
            }
        }.execute();
    }

    private void populateBorrowTable(List<Book> books) {
        borrowModel.setRowCount(0);
        for (Book b : books) addBorrowRow(b);
        borrowStatusLabel.setText("Showing " + books.size() + " book(s)");
    }

    private void addBorrowRow(Book b) {
        String authors = b.getAuthors() == null ? "" :
            b.getAuthors().stream().map(a -> a.getFullName())
                .reduce((x, y) -> x + ", " + y).orElse("");
        borrowModel.addRow(new Object[]{
            String.valueOf(b.getBookID()), b.getTitle(), authors,
            b.getCategoryName(), b.getPublisherName(),
            b.isAvailable() ? b.getAvailableCopies() + " available" : "Not available",
            b.getShelfLocation()
        });
    }

    private void loadHistory() {
        new SwingWorker<List<BorrowedRecord>, Void>() {
            @Override protected List<BorrowedRecord> doInBackground() {
                return borrowDAO.getMemberHistory(member.getMemberID());
            }
            @Override protected void done() {
                try {
                    historyModel.setRowCount(0);
                    for (BorrowedRecord br : get()) {
                        historyModel.addRow(new Object[]{
                            String.valueOf(br.getBorrowID()),
                            br.getBookTitle()  != null ? br.getBookTitle()                : "—",
                            br.getBorrowDate() != null ? br.getBorrowDate().format(DT_FMT) : "—",
                            br.getDueDate()    != null ? br.getDueDate().format(DT_FMT)    : "—",
                            br.getReturnDate() != null ? br.getReturnDate().format(DT_FMT) : "—",
                            br.getStatus()     != null ? br.getStatus()                   : "—",
                            "₱" + (br.getFineAmount() != null
                                ? br.getFineAmount().toPlainString() : "0.00")
                        });
                    }
                } catch (Exception ignored) {}
            }
        }.execute();
    }

    // ── NEW: load reservations ────────────────────────────────────────────────
    private void loadReservations() {
        new SwingWorker<List<Reservation>, Void>() {
            @Override protected List<Reservation> doInBackground() {
                return resDAO.getMemberReservations(member.getMemberID());
            }
            @Override protected void done() {
                try {
                    reservationModel.setRowCount(0);
                    cancelReservationBtn.setEnabled(false);
                    for (Reservation r : get()) {
                        String authors = r.getBookAuthors() != null ? r.getBookAuthors() : "—";
                        reservationModel.addRow(new Object[]{
                            String.valueOf(r.getReservationID()),
                            
                            r.getBookTitle()     != null ? r.getBookTitle()                   : "—",
                            authors,
                            r.getReservedDate() != null
                                ? r.getReservedDate().format(DT_FMT) : "—",
                            r.getExpiryDate()    != null
                                ? r.getExpiryDate().format(DT_FMT)    : "—",
                            r.getStatus()        != null ? r.getStatus()                     : "—"
                        });
                    }
                } catch (Exception ignored) {}
            }
        }.execute();
    }

    // ════════════════════════════════════════════════════════════════════════
    //  BORROW BOOK ACTIONS
    // ════════════════════════════════════════════════════════════════════════
    private void onBorrowRowSelected() {
        int row = borrowTable.getSelectedRow();
        if (row < 0) { clearBorrowSelection(); return; }
        int modelRow = borrowTable.convertRowIndexToModel(row);
        String title = borrowModel.getValueAt(modelRow, 1).toString();
        String avail = borrowModel.getValueAt(modelRow, 5).toString();
        boolean available = !avail.contains("Not available") && !avail.equals("0 available");
        if (available) {
            borrowSelectedLabel.setText(title + "  (" + avail + ")");
            borrowSelectedLabel.setForeground(Color.WHITE);
            borrowSubmitBtn.setEnabled(true);
            borrowStatusLabel.setText("Book selected: " + title);
        } else {
            borrowSelectedLabel.setText(title + "  [No copies available]");
            borrowSelectedLabel.setForeground(CLR_CARD_RED);
            borrowSubmitBtn.setEnabled(false);
            borrowStatusLabel.setText("No available copies for: " + title);
        }
    }

    private void updateBorrowReturnDate() {
        int days = (int) borrowDaysSpinner.getValue();
        LocalDate returnDate = LocalDate.now().plusDays(days);
        borrowReturnDateLabel.setText("Return by: " +
            returnDate.format(DateTimeFormatter.ofPattern("MMM dd, yyyy")));
    }

    private void submitBorrowRequest() {
        int row = borrowTable.getSelectedRow();
        if (row < 0) return;
        int modelRow = borrowTable.convertRowIndexToModel(row);
        int    bookID = Integer.parseInt(borrowModel.getValueAt(modelRow, 0).toString());
        String title  = borrowModel.getValueAt(modelRow, 1).toString();
        int    days   = (int) borrowDaysSpinner.getValue();
        LocalDate returnDate = LocalDate.now().plusDays(days);

        int choice = JOptionPane.showConfirmDialog(this,
            "<html><body style='font-family:Segoe UI;font-size:12px;'>"
            + "<b>Confirm Borrow Request</b><br><br>"
            + "<b>Book:</b> " + title + "<br>"
            + "<b>Loan Period:</b> " + days + " day(s)<br>"
            + "<b>Return Date:</b> " + returnDate.format(DateTimeFormatter.ofPattern("MMMM dd, yyyy"))
            + "<br><br>Proceed with borrowing this book?</body></html>",
            "Confirm Borrow", JOptionPane.YES_NO_OPTION);

        if (choice != JOptionPane.YES_OPTION) return;

        borrowSubmitBtn.setEnabled(false);
        borrowSubmitBtn.setText("  Processing...");

        new SwingWorker<Boolean, Void>() {
            @Override protected Boolean doInBackground() {
                return borrowDAO.submitBorrowRequest(
                    member.getMemberID(), bookID, returnDate);
            }
            @Override protected void done() {
                try {
                    if (get()) {
                        JOptionPane.showMessageDialog(StudentDashboard.this,
                            "<html><body style='font-family:Segoe UI;font-size:12px;'>"
                            + "✔ Borrow request submitted!<br>"
                            + "<b>" + title + "</b><br>"
                            + "Please return on or before <b>"
                            + returnDate.format(DateTimeFormatter.ofPattern("MMMM dd, yyyy"))
                            + "</b>.</body></html>",
                            "Borrow Successful", JOptionPane.INFORMATION_MESSAGE);
                        clearBorrowSelection();
                        loadBorrowBooks();
                        refreshDashboardStats();
                    } else {
                        JOptionPane.showMessageDialog(StudentDashboard.this,
                            "Failed to submit borrow request. Please try again.",
                            "Borrow Failed", JOptionPane.ERROR_MESSAGE);
                    }
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(StudentDashboard.this,
                        "Error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                } finally {
                    borrowSubmitBtn.setEnabled(true);
                    borrowSubmitBtn.setText("  Borrow Book");
                }
            }
        }.execute();
    }

    private void clearBorrowSelection() {
        borrowTable.clearSelection();
        borrowSelectedLabel.setText("— No book selected —");
        borrowSelectedLabel.setForeground(CLR_TEXT_DIM);
        borrowSubmitBtn.setEnabled(false);
        borrowStatusLabel.setText(" ");
    }

    // ════════════════════════════════════════════════════════════════════════
    //  RESERVATION ACTIONS  ← NEW
    // ════════════════════════════════════════════════════════════════════════
    private void cancelSelectedReservation() {
        int row = reservationTable.getSelectedRow();
        if (row < 0) return;
        int modelRow = reservationTable.convertRowIndexToModel(row);
        int    resID = Integer.parseInt(reservationModel.getValueAt(modelRow, 0).toString());
        String title = reservationModel.getValueAt(modelRow, 1).toString();

        int choice = JOptionPane.showConfirmDialog(this,
            "<html><body style='font-family:Segoe UI;font-size:12px;'>"
            + "<b>Cancel Reservation</b><br><br>"
            + "Are you sure you want to cancel the reservation for:<br>"
            + "<b>" + title + "</b>?</body></html>",
            "Confirm Cancellation", JOptionPane.YES_NO_OPTION,
            JOptionPane.WARNING_MESSAGE);

        if (choice != JOptionPane.YES_OPTION) return;

        cancelReservationBtn.setEnabled(false);
        cancelReservationBtn.setText("  Cancelling...");

        new SwingWorker<Boolean, Void>() {
            @Override protected Boolean doInBackground() {
                return resDAO.cancelReservation(resID, member.getMemberID());
            }
            @Override protected void done() {
                try {
                    if (get()) {
                        JOptionPane.showMessageDialog(StudentDashboard.this,
                            "Reservation for \"" + title + "\" has been cancelled.",
                            "Cancelled", JOptionPane.INFORMATION_MESSAGE);
                        loadReservations();
                        refreshDashboardStats();
                    } else {
                        JOptionPane.showMessageDialog(StudentDashboard.this,
                            "Failed to cancel the reservation. Please try again.",
                            "Error", JOptionPane.ERROR_MESSAGE);
                    }
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(StudentDashboard.this,
                        "Error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                } finally {
                    cancelReservationBtn.setText("  Cancel Reservation");
                }
            }
        }.execute();
    }

    // ════════════════════════════════════════════════════════════════════════
    //  BROWSE BOOKS ACTIONS
    // ════════════════════════════════════════════════════════════════════════
    private void reserveSelectedBook() {
        int row = bookTable.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Please select a book to reserve.",
                "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }
        int modelRow = bookTable.convertRowIndexToModel(row);
        int    bookID = Integer.parseInt(bookModel.getValueAt(modelRow, 0).toString());
        String title  = bookModel.getValueAt(modelRow, 1).toString();

        int result = JOptionPane.showConfirmDialog(this,
            "Reserve \"" + title + "\"?",
            "Confirm Reservation", JOptionPane.YES_NO_OPTION);

        if (result == JOptionPane.YES_OPTION) {
            reserveBtn.setEnabled(false);
            reserveBtn.setText("  Reserving...");
            new SwingWorker<Boolean, Void>() {
                @Override protected Boolean doInBackground() {
                    return resDAO.createReservation(bookID, member.getMemberID());
                }
                @Override protected void done() {
                    try {
                        if (get()) {
                            JOptionPane.showMessageDialog(StudentDashboard.this,
                                "Reservation submitted successfully!\n"
                                + "You can view it under \"My Reservations\".");
                            refreshDashboardStats();
                        } else {
                            JOptionPane.showMessageDialog(StudentDashboard.this,
                                "Failed to reserve. You may already have a pending reservation.",
                                "Reservation Failed", JOptionPane.ERROR_MESSAGE);
                        }
                    } catch (Exception ex) {
                        JOptionPane.showMessageDialog(StudentDashboard.this,
                            "Error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                    } finally {
                        reserveBtn.setEnabled(true);
                        reserveBtn.setText("  Reserve Selected Book");
                    }
                }
            }.execute();
        }
    }

    // ════════════════════════════════════════════════════════════════════════
    //  PROFILE ACTIONS
    // ════════════════════════════════════════════════════════════════════════
    private void openEditProfile() {
        JDialog dialog = new JDialog(this, "Edit Profile", true);
        dialog.setSize(550, 600);
        dialog.setLocationRelativeTo(this);
        dialog.setContentPane(new ProfilePanel(member));
        dialog.setVisible(true);
    }

    // ════════════════════════════════════════════════════════════════════════
    //  LOGOUT
    // ════════════════════════════════════════════════════════════════════════
    private void logout() {
        int confirm = JOptionPane.showConfirmDialog(
            this, "Logout from the system?",
            "Confirm Logout", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            new LoginFrame().setVisible(true);
            dispose();
        }
    }

    // ════════════════════════════════════════════════════════════════════════
    //  HELPERS
    // ════════════════════════════════════════════════════════════════════════
    private JButton makeActionButton(String text, IconType icon, Color accentColor) {
        JButton btn = new JButton(text);
        if (icon != null) btn.setIcon(makeIcon(icon, Color.WHITE, 16));
        btn.setFont(FONT_BODY);
        btn.setBackground(new Color(0x1e4a1e));
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false); btn.setBorderPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setBorder(new EmptyBorder(8, 18, 8, 18));
        btn.setIconTextGap(6);
        return btn;
    }

    private JLabel dimLabel(String text) {
        JLabel l = new JLabel(text);
        l.setFont(FONT_LABEL);
        l.setForeground(CLR_TEXT_DIM);
        return l;
    }

    private static void styleTable(JTable table) {
        table.setFont(FONT_BODY);
        table.setRowHeight(32);
        table.setSelectionBackground(new Color(0x2e5c2e));
        table.setSelectionForeground(Color.WHITE);
        table.setGridColor(new Color(0x2a4a2a));
        table.setShowVerticalLines(false);
        table.setBackground(new Color(0x1a2e1a));
        table.setForeground(new Color(0xddeeDD));
        table.setAutoCreateRowSorter(true);
        table.getTableHeader().setReorderingAllowed(false);
        table.getTableHeader().setResizingAllowed(true);

        JTableHeader h = table.getTableHeader();
        h.setFont(FONT_LABEL);
        h.setBackground(new Color(0x0d1f0d));
        h.setForeground(Color.WHITE);
        h.setPreferredSize(new Dimension(0, 38));
        h.setDefaultRenderer(new DefaultTableCellRenderer() {
            @Override public Component getTableCellRendererComponent(
                    JTable t, Object v, boolean sel, boolean foc, int r, int c) {
                JLabel lbl = new JLabel(v != null ? v.toString() : "");
                lbl.setFont(FONT_LABEL);
                lbl.setForeground(Color.WHITE);
                lbl.setBackground(new Color(0x0d1f0d));
                lbl.setOpaque(true);
                lbl.setBorder(new EmptyBorder(0, 10, 0, 10));
                lbl.setHorizontalAlignment(SwingConstants.LEFT);
                return lbl;
            }
        });
    }
}