package neustlibrarysystem.view.admin;

import neustlibrarysystem.model.Admin;
import neustlibrarysystem.model.Librarian;
import neustlibrarysystem.dao.AdminDAO;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.List;

public class AdminDashboard extends JFrame {

    // ── Design tokens (unified with LibrarianDashboard) ───────────────────────
    private static final Color CLR_ACCENT      = new Color(0x9ab55d);   // green accent
    private static final Color CLR_BG          = new Color(0xeaf7d7);
    private static final Color CLR_SIDEBAR     = new Color(0x1a1a2e);
    private static final Color CLR_SIDEBAR_SEL = new Color(0x2a2a3e);
    private static final Color CLR_SIDEBAR_HOV = new Color(0x25253a);
    private static final Color CLR_TOP_BAR     = new Color(0x12122a);

    private static final Font FONT_NAV    = new Font("Segoe UI", Font.BOLD,  13);
    private static final Font FONT_HEADER = new Font("Segoe UI", Font.BOLD,  20);
    private static final Font FONT_BODY   = new Font("Segoe UI", Font.PLAIN, 13);
    private static final Font FONT_LABEL  = new Font("Segoe UI", Font.BOLD,  12);

    private static final int SIDEBAR_EXPANDED  = 230;
    private static final int SIDEBAR_COLLAPSED = 64;

    // ── State ─────────────────────────────────────────────────────────────────
    private final Admin    currentAdmin;
    private final AdminDAO adminDAO = new AdminDAO();

    private CardLayout cardLayout;
    private JPanel     contentPanel;
    private JPanel     sidebar;
    private JButton    toggleBtn;
    private boolean    sidebarExpanded = true;
    private Timer      sidebarTimer;
    private int        sidebarTargetWidth;

    private final List<NavEntry> navEntries = new ArrayList<>();
    private JButton activeNavBtn = null;

    // ── Librarians panel widgets ──────────────────────────────────────────────
    private JButton           btnAdd, btnToggle;
    private DefaultTableModel libTableModel;
    private JTable            libTable;

    // ── Settings panel widgets ────────────────────────────────────────────────
    private JTextField tfBorrowDays, tfFineRate, tfMaxBorrow;
    private JButton    btnSave;

    // ── Dashboard stat labels ─────────────────────────────────────────────────
    private JLabel lblBooks, lblMembers, lblBorrowed, lblOverdue;

    // ── Icon types ────────────────────────────────────────────────────────────
    public enum IconType {
        DASHBOARD, LIBRARIANS, SETTINGS, LOGOUT, USER,
        TOGGLE_CLOSE, TOGGLE_OPEN, ADD, TOGGLE_STATUS, SAVE
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
                    case DASHBOARD     -> drawDashboard(g2, s);
                    case LIBRARIANS    -> drawMembers(g2, s);
                    case SETTINGS      -> drawSettings(g2, s);
                    case LOGOUT        -> drawLogout(g2, s);
                    case USER          -> drawUser(g2, s);
                    case TOGGLE_CLOSE  -> drawToggleClose(g2, s);
                    case TOGGLE_OPEN   -> drawToggleOpen(g2, s);
                    case ADD           -> drawAdd(g2, s);
                    case TOGGLE_STATUS -> drawToggleStatus(g2, s);
                    case SAVE          -> drawSave(g2, s);
                }
                g2.dispose();
            }
        };
    }

    private static int p(float v, float s) { return Math.round(v * s); }

    private static void drawDashboard(Graphics2D g, float s) {
        g.fillRoundRect(p(2,s),p(2,s), p(7,s),p(7,s), p(2,s),p(2,s));
        g.fillRoundRect(p(11,s),p(2,s),p(7,s),p(7,s), p(2,s),p(2,s));
        g.fillRoundRect(p(2,s),p(11,s),p(7,s),p(7,s), p(2,s),p(2,s));
        g.fillRoundRect(p(11,s),p(11,s),p(7,s),p(7,s),p(2,s),p(2,s));
    }
    private static void drawMembers(Graphics2D g, float s) {
        Color c = g.getColor();
        g.setColor(new Color(c.getRed(),c.getGreen(),c.getBlue(),150));
        g.fillOval(p(11,s),p(2,s),p(6,s),p(6,s));
        g.fillArc(p(8,s),p(10,s),p(10,s),p(8,s),0,180);
        g.setColor(c);
        g.fillOval(p(3,s),p(3,s),p(7,s),p(7,s));
        g.fillArc(p(0,s),p(11,s),p(12,s),p(8,s),0,180);
    }
    private static void drawSettings(Graphics2D g, float s) {
        g.drawOval(p(6,s),p(6,s),p(8,s),p(8,s));
        for (int i = 0; i < 8; i++) {
            double a = Math.toRadians(i * 45);
            int x1 = p(10,s) + (int)(Math.cos(a)*p(6,s));
            int y1 = p(10,s) + (int)(Math.sin(a)*p(6,s));
            int x2 = p(10,s) + (int)(Math.cos(a)*p(8,s));
            int y2 = p(10,s) + (int)(Math.sin(a)*p(8,s));
            g.drawLine(x1, y1, x2, y2);
        }
        g.fillOval(p(7,s),p(7,s),p(6,s),p(6,s));
    }
    private static void drawLogout(Graphics2D g, float s) {
        g.drawRoundRect(p(2,s),p(2,s),p(10,s),p(16,s),p(2,s),p(2,s));
        g.fillOval(p(3,s),p(9,s),p(2,s),p(2,s));
        g.drawLine(p(9,s),p(10,s),p(18,s),p(10,s));
        g.fillPolygon(new int[]{p(14,s),p(18,s),p(14,s)},
                      new int[]{p(6,s),p(10,s),p(14,s)}, 3);
    }
    private static void drawUser(Graphics2D g, float s) {
        g.fillOval(p(6,s),p(2,s),p(8,s),p(8,s));
        g.fillArc(p(2,s),p(11,s),p(16,s),p(9,s),0,180);
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
    private static void drawAdd(Graphics2D g, float s) {
        g.drawLine(p(10,s),p(3,s),p(10,s),p(17,s));
        g.drawLine(p(3,s),p(10,s),p(17,s),p(10,s));
    }
    private static void drawToggleStatus(Graphics2D g, float s) {
        g.drawArc(p(2,s),p(5,s),p(10,s),p(10,s),90,270);
        g.drawArc(p(8,s),p(5,s),p(10,s),p(10,s),270,270);
        g.drawLine(p(7,s),p(10,s),p(13,s),p(10,s));
        g.fillPolygon(new int[]{p(13,s),p(17,s),p(13,s)},
                      new int[]{p(7,s),p(10,s),p(13,s)}, 3);
    }
    private static void drawSave(Graphics2D g, float s) {
        g.drawRoundRect(p(2,s),p(2,s),p(16,s),p(16,s),p(2,s),p(2,s));
        g.fillRoundRect(p(5,s),p(2,s),p(10,s),p(7,s),p(1,s),p(1,s));
        g.fillRoundRect(p(4,s),p(10,s),p(12,s),p(7,s),p(1,s),p(1,s));
        Color c = g.getColor();
        g.setColor(new Color(0x1a,0x1a,0x2e));
        g.fillRect(p(11,s),p(3,s),p(2,s),p(5,s));
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
    public AdminDashboard(Admin admin) {
        this.currentAdmin = admin;
        setTitle("NEUST Library System — Admin Dashboard");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(1100, 680);
        setLocationRelativeTo(null);
        buildUI();
        loadDashboardStats();
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
        contentPanel.add(buildDashboardPanel(),  "DASHBOARD");
        contentPanel.add(buildLibrariansPanel(), "LIBRARIANS");
        contentPanel.add(buildSettingsPanel(),   "SETTINGS");
        cardLayout.show(contentPanel, "DASHBOARD");

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
        topArea.setBackground(CLR_TOP_BAR);
        topArea.setBorder(new EmptyBorder(20, 0, 16, 0));

        JPanel logoPanel = new JPanel();
        logoPanel.setBackground(CLR_TOP_BAR);
        logoPanel.setLayout(new BoxLayout(logoPanel, BoxLayout.Y_AXIS));
        JLabel logoIcon = new JLabel(makeIcon(IconType.DASHBOARD, CLR_ACCENT, 36));
        logoIcon.setAlignmentX(Component.CENTER_ALIGNMENT);
        JLabel sysName = new JLabel("NEUST Library");
        sysName.setFont(new Font("Segoe UI", Font.BOLD, 14));
        sysName.setForeground(Color.WHITE);
        sysName.setAlignmentX(Component.CENTER_ALIGNMENT);
        JLabel roleLabel = new JLabel("Admin Panel");
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
        toggleBtn.setBackground(CLR_TOP_BAR);
        toggleBtn.setBorderPainted(false); toggleBtn.setFocusPainted(false);
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
        addNavBtn(navContainer, IconType.DASHBOARD,  "Dashboard",         "DASHBOARD");
        addNavBtn(navContainer, IconType.LIBRARIANS, "Manage Librarians", "LIBRARIANS");
        addNavBtn(navContainer, IconType.SETTINGS,   "System Settings",   "SETTINGS");
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
        userRow.setText("  " + currentAdmin.getFullName());
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
            public void mouseEntered(MouseEvent e) { logoutBtn.setBackground(new Color(0x4a1010)); }
            public void mouseExited (MouseEvent e) { logoutBtn.setBackground(new Color(0x22223a)); }
        });
        logoutBtn.addActionListener(e -> logout());
        bottomArea.add(logoutBtn);
        sidebar.add(bottomArea, BorderLayout.SOUTH);

        return sidebar;
    }

    // ── Nav button factory ────────────────────────────────────────────────────
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
            // Deactivate previous
            if (activeNavBtn != null) {
                NavEntry prev = navEntries.stream()
                    .filter(ne -> ne.btn == activeNavBtn).findFirst().orElse(null);
                if (prev != null)
                    activeNavBtn.setIcon(makeIcon(prev.iconType, iconNormal, 20));
                activeNavBtn.setContentAreaFilled(false);
                activeNavBtn.setBackground(CLR_SIDEBAR);
                activeNavBtn.setForeground(new Color(0xccccee));
            }
            // Activate clicked
            btn.setIcon(makeIcon(iconType, CLR_ACCENT, 20));
            btn.setContentAreaFilled(true);
            btn.setBackground(CLR_SIDEBAR_SEL);
            btn.setForeground(CLR_ACCENT);
            activeNavBtn = btn;

            cardLayout.show(contentPanel, card);
            if ("LIBRARIANS".equals(card)) loadLibrarians();
            if ("SETTINGS".equals(card))   loadSettings();
        });
        container.add(btn);
        container.add(Box.createVerticalStrut(2));
    }

    // ── Toggle animation ──────────────────────────────────────────────────────
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

        JPanel bottomArea = (JPanel) sidebar.getComponent(2);
        for (Component c : bottomArea.getComponents()) {
            if (!(c instanceof JButton jb)) continue;
            if ("userRow".equals(jb.getName())) {
                jb.setText(sidebarExpanded ? "  " + currentAdmin.getFullName() : "");
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
        bar.setBackground(CLR_TOP_BAR);
        bar.setBorder(new EmptyBorder(12, 24, 12, 24));
        JLabel title = new JLabel("Library Management System — Admin Portal");
        title.setFont(new Font("Segoe UI", Font.BOLD, 15));
        title.setForeground(Color.WHITE);
        JLabel dateLabel = new JLabel(
            java.time.LocalDate.now().format(
                java.time.format.DateTimeFormatter.ofPattern("EEEE, MMMM dd, yyyy")));
        dateLabel.setFont(FONT_BODY);
        dateLabel.setForeground(CLR_ACCENT);
        bar.add(title,     BorderLayout.WEST);
        bar.add(dateLabel, BorderLayout.EAST);
        return bar;
    }

    // ── Dashboard Panel ───────────────────────────────────────────────────────
    private JPanel buildDashboardPanel() {
        JPanel p = new JPanel(new BorderLayout(0, 20));
        p.setBackground(CLR_BG);

        JLabel hdr = new JLabel("Dashboard Overview");
        hdr.setFont(FONT_HEADER);
        hdr.setForeground(new Color(0x1a1a2e));
        p.add(hdr, BorderLayout.NORTH);

        JPanel cards = new JPanel(new GridLayout(1, 4, 16, 16));
        cards.setOpaque(false);

        lblBooks    = new JLabel("...", SwingConstants.CENTER);
        lblMembers  = new JLabel("...", SwingConstants.CENTER);
        lblBorrowed = new JLabel("...", SwingConstants.CENTER);
        lblOverdue  = new JLabel("...", SwingConstants.CENTER);

        cards.add(statCard("Total Books",        lblBooks,    new Color(0x1a1a2e), new Color(0x9ab55d)));
        cards.add(statCard("Total Members",      lblMembers,  new Color(0x1a1a2e), new Color(0x5B9BD5)));
        cards.add(statCard("Currently Borrowed", lblBorrowed, new Color(0x1a1a2e), new Color(0xF0A500)));
        cards.add(statCard("Overdue",            lblOverdue,  new Color(0x1a1a2e), new Color(0xC0392B)));

        p.add(cards, BorderLayout.CENTER);
        return p;
    }

    private JPanel statCard(String title, JLabel valueLabel, Color bgDark, Color accentColor) {
        JPanel card = new JPanel(new BorderLayout(0, 8));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 4, 0, 0, accentColor),
            new EmptyBorder(20, 20, 20, 20)
        ));

        // Top: colored dot + title
        JPanel topRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        topRow.setOpaque(false);
        JLabel dot = new JLabel("●");
        dot.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        dot.setForeground(accentColor);
        JLabel titleLbl = new JLabel(title);
        titleLbl.setFont(FONT_LABEL);
        titleLbl.setForeground(new Color(0x666688));
        topRow.add(dot);
        topRow.add(titleLbl);

        valueLabel.setFont(new Font("Segoe UI", Font.BOLD, 44));
        valueLabel.setForeground(bgDark);

        card.add(topRow,      BorderLayout.NORTH);
        card.add(valueLabel,  BorderLayout.CENTER);
        return card;
    }

    // ── Librarians Panel ──────────────────────────────────────────────────────
    private JPanel buildLibrariansPanel() {
        JPanel p = new JPanel(new BorderLayout(0, 12));
        p.setBackground(CLR_BG);

        JLabel hdr = new JLabel("Manage Librarians");
        hdr.setFont(FONT_HEADER);
        hdr.setForeground(new Color(0x1a1a2e));
        p.add(hdr, BorderLayout.NORTH);

        String[] cols = {"ID", "Employee ID", "Full Name", "Email", "Contact", "Status"};
        libTableModel = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        libTable = new JTable(libTableModel);
        styleTable(libTable);
        libTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        JScrollPane scroll = new JScrollPane(libTable);
        scroll.setBorder(BorderFactory.createLineBorder(new Color(0xc5e0a0), 1));
        p.add(scroll, BorderLayout.CENTER);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 10));
        btnPanel.setOpaque(false);

        btnAdd    = primaryBtn("  Add Librarian",      IconType.ADD,           new Color(0x183b06));
        btnToggle = primaryBtn("  Toggle Active",       IconType.TOGGLE_STATUS, new Color(0x2a6049));

        btnAdd   .addActionListener(e -> showAddLibrarianDialog());
        btnToggle.addActionListener(e -> toggleLibrarianStatus());

        btnPanel.add(btnAdd);
        btnPanel.add(btnToggle);
        p.add(btnPanel, BorderLayout.SOUTH);
        return p;
    }

    // ── Settings Panel ────────────────────────────────────────────────────────
    private JPanel buildSettingsPanel() {
        JPanel p = new JPanel(new GridBagLayout());
        p.setBackground(CLR_BG);

        JPanel card = new JPanel(new GridBagLayout());
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(0xDDDDDD), 1),
            new EmptyBorder(28, 32, 28, 32)
        ));

        GridBagConstraints gc = new GridBagConstraints();
        gc.fill      = GridBagConstraints.HORIZONTAL;
        gc.insets    = new Insets(8, 0, 8, 0);
        gc.gridwidth = GridBagConstraints.REMAINDER;

        JLabel hdr = new JLabel("System Settings");
        hdr.setFont(FONT_HEADER);
        hdr.setForeground(new Color(0x1a1a2e));
        card.add(hdr, gc);

        gc.insets = new Insets(12, 0, 4, 0);
        tfBorrowDays = addSettingRow(card, gc, "Max Borrow Days");
        tfFineRate   = addSettingRow(card, gc, "Fine Rate per Day (₱)");
        tfMaxBorrow  = addSettingRow(card, gc, "Max Books per Member");

        gc.insets = new Insets(16, 0, 0, 0);
        btnSave = primaryBtn("  Save Settings", IconType.SAVE, new Color(0x183b06));
        btnSave.addActionListener(e -> saveSettings());
        card.add(btnSave, gc);

        p.add(card, new GridBagConstraints());
        return p;
    }

    private JTextField addSettingRow(JPanel panel, GridBagConstraints gc, String label) {
        gc.gridwidth = 1; gc.weightx = 0.4;
        JLabel lbl = new JLabel(label);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 13));
        panel.add(lbl, gc);
        gc.weightx = 0.6; gc.gridwidth = GridBagConstraints.REMAINDER;
        JTextField tf = new JTextField(14);
        tf.setFont(FONT_BODY);
        tf.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(0xcccccc), 1),
            new EmptyBorder(6, 10, 6, 10)
        ));
        panel.add(tf, gc);
        return tf;
    }

    // ── Shared button factory ─────────────────────────────────────────────────
    private JButton primaryBtn(String text, IconType iconType, Color bg) {
        JButton btn = new JButton(text);
        btn.setIcon(makeIcon(iconType, Color.WHITE, 16));
        btn.setFont(FONT_BODY);
        btn.setBackground(bg);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setBorder(new EmptyBorder(8, 18, 8, 18));
        btn.setIconTextGap(8);
        Color darker = bg.darker();
        btn.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) { if (btn.isEnabled()) btn.setBackground(darker); }
            public void mouseExited (MouseEvent e) { if (btn.isEnabled()) btn.setBackground(bg); }
        });
        return btn;
    }

    // ── Table styling ─────────────────────────────────────────────────────────
    private static void styleTable(JTable table) {
        table.setFont(FONT_BODY);
        table.setRowHeight(30);
        table.setSelectionBackground(new Color(0x9ab55d));
        table.setSelectionForeground(Color.WHITE);
        table.setGridColor(new Color(0xc5e0a0));
        table.setShowVerticalLines(false);
        table.setBackground(Color.WHITE);
        JTableHeader h = table.getTableHeader();
        h.setFont(FONT_LABEL);
        h.setBackground(new Color(0x1a1a2e));
        h.setForeground(Color.WHITE);
        h.setReorderingAllowed(false);
        h.setPreferredSize(new Dimension(0, 36));
    }

    // ── Data Loaders ──────────────────────────────────────────────────────────
    private void loadDashboardStats() {
        lblBooks.setText("..."); lblMembers.setText("...");
        lblBorrowed.setText("..."); lblOverdue.setText("...");
        new SwingWorker<int[], Void>() {
            @Override protected int[] doInBackground() { return adminDAO.getSummaryReport(); }
            @Override protected void done() {
                try {
                    int[] stats = get();
                    lblBooks.setText(String.valueOf(stats[0]));
                    lblMembers.setText(String.valueOf(stats[1]));
                    lblBorrowed.setText(String.valueOf(stats[2]));
                    lblOverdue.setText(String.valueOf(stats[3]));
                } catch (Exception ex) {
                    lblBooks.setText("!"); lblMembers.setText("!");
                    lblBorrowed.setText("!"); lblOverdue.setText("!");
                    JOptionPane.showMessageDialog(AdminDashboard.this,
                        "Error loading stats: " + ex.getMessage(),
                        "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        }.execute();
    }

    private void loadLibrarians() {
        btnAdd.setEnabled(false); btnToggle.setEnabled(false);
        new SwingWorker<List<Librarian>, Void>() {
            @Override protected List<Librarian> doInBackground() {
                return adminDAO.getAllLibrarians();
            }
            @Override protected void done() {
                try {
                    libTableModel.setRowCount(0);
                    for (Librarian lib : get()) {
                        libTableModel.addRow(new Object[]{
                            lib.getLibrarianID(), lib.getEmployeeID(),
                            lib.getFullName(), lib.getEmail(),
                            lib.getContactNumber(),
                            lib.isActive() ? "Active" : "Inactive"
                        });
                    }
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(AdminDashboard.this,
                        "Error loading librarians: " + ex.getMessage(),
                        "Error", JOptionPane.ERROR_MESSAGE);
                } finally {
                    btnAdd.setEnabled(true); btnToggle.setEnabled(true);
                }
            }
        }.execute();
    }

    private void loadSettings() {
        tfBorrowDays.setEnabled(false); tfFineRate.setEnabled(false);
        tfMaxBorrow.setEnabled(false);  btnSave.setEnabled(false);
        new SwingWorker<String[], Void>() {
            @Override protected String[] doInBackground() {
                return new String[]{
                    adminDAO.getSetting("borrow_duration_days"),
                    adminDAO.getSetting("fine_per_day"),
                    adminDAO.getSetting("max_borrow_limit")
                };
            }
            @Override protected void done() {
                try {
                    String[] vals = get();
                    tfBorrowDays.setText(orDefault(vals[0], "7"));
                    tfFineRate  .setText(orDefault(vals[1], "5"));
                    tfMaxBorrow .setText(orDefault(vals[2], "3"));
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(AdminDashboard.this,
                        "Error loading settings: " + ex.getMessage(),
                        "Error", JOptionPane.ERROR_MESSAGE);
                } finally {
                    tfBorrowDays.setEnabled(true); tfFineRate.setEnabled(true);
                    tfMaxBorrow.setEnabled(true);  btnSave.setEnabled(true);
                }
            }
        }.execute();
    }

    private String orDefault(String val, String def) {
        return (val != null && !val.isEmpty()) ? val : def;
    }

    // ── Actions ───────────────────────────────────────────────────────────────
    private void showAddLibrarianDialog() {
        JPanel dlg = new JPanel(new GridLayout(6, 2, 8, 8));
        dlg.setBorder(new EmptyBorder(8, 8, 8, 8));
        JTextField   tfEmpID   = new JTextField();
        JTextField   tfFirst   = new JTextField();
        JTextField   tfLast    = new JTextField();
        JTextField   tfEmail   = new JTextField();
        JTextField   tfContact = new JTextField();
        JPasswordField tfPass  = new JPasswordField();

        for (JTextField tf : new JTextField[]{tfEmpID,tfFirst,tfLast,tfEmail,tfContact}) {
            tf.setFont(FONT_BODY);
        }
        tfPass.setFont(FONT_BODY);

        dlg.add(styledLabel("Employee ID:"));    dlg.add(tfEmpID);
        dlg.add(styledLabel("First Name:"));     dlg.add(tfFirst);
        dlg.add(styledLabel("Last Name:"));      dlg.add(tfLast);
        dlg.add(styledLabel("Email:"));          dlg.add(tfEmail);
        dlg.add(styledLabel("Contact Number:")); dlg.add(tfContact);
        dlg.add(styledLabel("Password:"));       dlg.add(tfPass);

        int result = JOptionPane.showConfirmDialog(this, dlg,
            "Add Librarian", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

        if (result == JOptionPane.OK_OPTION) {
            if (tfEmpID.getText().trim().isEmpty() || tfPass.getPassword().length == 0) {
                JOptionPane.showMessageDialog(this, "Employee ID and password are required.");
                return;
            }
            Librarian lib = new Librarian();
            lib.setEmployeeID   (tfEmpID  .getText().trim());
            lib.setFirstName    (tfFirst  .getText().trim());
            lib.setLastName     (tfLast   .getText().trim());
            lib.setEmail        (tfEmail  .getText().trim());
            lib.setContactNumber(tfContact.getText().trim());
            String pass = new String(tfPass.getPassword());

            btnAdd.setEnabled(false); btnToggle.setEnabled(false);
            new SwingWorker<Boolean, Void>() {
                @Override protected Boolean doInBackground() {
                    return adminDAO.addLibrarian(lib, pass);
                }
                @Override protected void done() {
                    try {
                        if (get()) {
                            JOptionPane.showMessageDialog(AdminDashboard.this,
                                "Librarian added successfully.");
                            loadLibrarians();
                        } else {
                            JOptionPane.showMessageDialog(AdminDashboard.this,
                                "Failed to add librarian.", "Error", JOptionPane.ERROR_MESSAGE);
                        }
                    } catch (Exception ex) {
                        JOptionPane.showMessageDialog(AdminDashboard.this,
                            "Error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                    } finally {
                        btnAdd.setEnabled(true); btnToggle.setEnabled(true);
                    }
                }
            }.execute();
        }
    }

    private void toggleLibrarianStatus() {
        int row = libTable.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Select a librarian first.");
            return;
        }
        int    id       = (int)    libTableModel.getValueAt(row, 0);
        String status   = (String) libTableModel.getValueAt(row, 5);
        boolean isActive = "Active".equals(status);

        int confirm = JOptionPane.showConfirmDialog(this,
            "Are you sure you want to " + (isActive ? "deactivate" : "activate") + " this librarian?",
            "Confirm", JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            btnAdd.setEnabled(false); btnToggle.setEnabled(false);
            new SwingWorker<Boolean, Void>() {
                @Override protected Boolean doInBackground() {
                    return isActive ? adminDAO.deactivateLibrarian(id)
                                    : adminDAO.activateLibrarian(id);
                }
                @Override protected void done() {
                    try {
                        if (get()) loadLibrarians();
                        else JOptionPane.showMessageDialog(AdminDashboard.this,
                            "Operation failed.", "Error", JOptionPane.ERROR_MESSAGE);
                    } catch (Exception ex) {
                        JOptionPane.showMessageDialog(AdminDashboard.this,
                            "Error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                    } finally {
                        btnAdd.setEnabled(true); btnToggle.setEnabled(true);
                    }
                }
            }.execute();
        }
    }

    private void saveSettings() {
        String borrowDays = tfBorrowDays.getText().trim();
        String fineRate   = tfFineRate  .getText().trim();
        String maxBorrow  = tfMaxBorrow .getText().trim();
        int    adminID    = currentAdmin.getAdminId();

        btnSave.setEnabled(false); btnSave.setText("  Saving...");
        new SwingWorker<boolean[], Void>() {
            @Override protected boolean[] doInBackground() {
                return new boolean[]{
                    adminDAO.updateSetting("borrow_duration_days", borrowDays, adminID),
                    adminDAO.updateSetting("fine_per_day",         fineRate,   adminID),
                    adminDAO.updateSetting("max_borrow_limit",     maxBorrow,  adminID)
                };
            }
            @Override protected void done() {
                try {
                    boolean[] r = get();
                    if (r[0] && r[1] && r[2])
                        JOptionPane.showMessageDialog(AdminDashboard.this,
                            "Settings saved successfully.");
                    else JOptionPane.showMessageDialog(AdminDashboard.this,
                        "Some settings failed to save.", "Warning", JOptionPane.WARNING_MESSAGE);
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(AdminDashboard.this,
                        "Error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                } finally {
                    btnSave.setEnabled(true); btnSave.setText("  Save Settings");
                }
            }
        }.execute();
    }

    private void logout() {
        int confirm = JOptionPane.showConfirmDialog(this,
            "Are you sure you want to logout?", "Confirm Logout", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            dispose();
            new neustlibrarysystem.view.common.LoginFrame().setVisible(true);
        }
    }

    // ── Helpers ───────────────────────────────────────────────────────────────
    private static JLabel styledLabel(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 13));
        return lbl;
    }
}