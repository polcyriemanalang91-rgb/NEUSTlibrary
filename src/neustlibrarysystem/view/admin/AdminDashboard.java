package neustlibrarysystem.view.admin;

import neustlibrarysystem.model.Admin;
import neustlibrarysystem.model.Librarian;
import neustlibrarysystem.dao.AdminDAO;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.util.ArrayList;
import java.util.List;

public class AdminDashboard extends JFrame {

    // ── Design Tokens ─────────────────────────────────────────────────────────
    private static final Color CLR_PRIMARY     = new Color(0x0f2027);
    private static final Color CLR_SECONDARY   = new Color(0x1a3a2a);
    private static final Color CLR_ACCENT      = new Color(0x4ade80);
    private static final Color CLR_ACCENT2     = new Color(0x22d3ee);
    private static final Color CLR_ACCENT3     = new Color(0xfbbf24);
    private static final Color CLR_ACCENT4     = new Color(0xf87171);
    private static final Color CLR_BG          = new Color(0x0d1b12);
    private static final Color CLR_CARD        = new Color(0x132218);
    private static final Color CLR_CARD2       = new Color(0x1a2e1f);
    private static final Color CLR_SIDEBAR     = new Color(0x0a1a10);
    private static final Color CLR_SIDEBAR_SEL = new Color(0x1e3a26);
    private static final Color CLR_SIDEBAR_HOV = new Color(0x162b1c);
    private static final Color CLR_TOP_BAR     = new Color(0x091510);
    private static final Color CLR_TEXT        = new Color(0xe2f5e8);
    private static final Color CLR_MUTED       = new Color(0x6b9e7a);
    private static final Color CLR_BORDER      = new Color(0x1f3d28);

    private static final Font FONT_TITLE  = new Font("Segoe UI", Font.BOLD,  22);
    private static final Font FONT_NAV    = new Font("Segoe UI", Font.BOLD,  13);
    private static final Font FONT_HEADER = new Font("Segoe UI", Font.BOLD,  18);
    private static final Font FONT_BODY   = new Font("Segoe UI", Font.PLAIN, 13);
    private static final Font FONT_LABEL  = new Font("Segoe UI", Font.BOLD,  12);
    private static final Font FONT_STAT   = new Font("Segoe UI", Font.BOLD,  36);
    private static final Font FONT_SMALL  = new Font("Segoe UI", Font.PLAIN, 11);

    private static final int SIDEBAR_EXPANDED  = 240;
    private static final int SIDEBAR_COLLAPSED = 62;

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

    // ── Panel widgets ─────────────────────────────────────────────────────────
    private JButton           btnAdd, btnToggle;
    private DefaultTableModel libTableModel;
    private JTable            libTable;
    private JTextField        tfBorrowDays, tfFineRate, tfMaxBorrow;
    private JButton           btnSave;
    private JLabel            lblBooks, lblMembers, lblBorrowed, lblOverdue;

    // ── Nav entry ─────────────────────────────────────────────────────────────
    private static class NavEntry {
        JButton btn; String iconChar; String text; String card;
        NavEntry(JButton b, String ic, String t, String c) {
            btn=b; iconChar=ic; text=t; card=c;
        }
    }

    // ── Constructor ───────────────────────────────────────────────────────────
    public AdminDashboard(Admin admin) {
        this.currentAdmin = admin;
        setTitle("NEUST Library System — Admin Dashboard");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(1200, 720);
        setMinimumSize(new Dimension(900, 600));
        setLocationRelativeTo(null);
        buildUI();
        loadDashboardStats();
    }

    // ── UI Builder ────────────────────────────────────────────────────────────
    private void buildUI() {
        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(CLR_BG);
        root.add(buildSidebar(), BorderLayout.WEST);
        root.add(buildTopBar(), BorderLayout.NORTH);

        cardLayout   = new CardLayout();
        contentPanel = new JPanel(cardLayout);
        contentPanel.setBackground(CLR_BG);
        contentPanel.setBorder(new EmptyBorder(24, 28, 24, 28));

        // ✅ Authors, Categories, Publishers now use their own panel files
        contentPanel.add(buildDashboardPanel(),                   "DASHBOARD");
        contentPanel.add(buildLibrariansPanel(),                  "LIBRARIANS");
        contentPanel.add(new ManageAuthorsPanel(currentAdmin),    "AUTHORS");
        contentPanel.add(new ManageCategoriesPanel(currentAdmin), "CATEGORIES");
        contentPanel.add(new ManagePublishersPanel(currentAdmin), "PUBLISHERS");
        contentPanel.add(new ReportsPanel(currentAdmin), "REPORTS");
        contentPanel.add(buildSettingsPanel(),                    "SETTINGS");

        cardLayout.show(contentPanel, "DASHBOARD");
        root.add(contentPanel, BorderLayout.CENTER);
        setContentPane(root);
    }

    // ── Sidebar ───────────────────────────────────────────────────────────────
    private JPanel buildSidebar() {
        sidebar = new JPanel(new BorderLayout());
        sidebar.setBackground(CLR_SIDEBAR);
        sidebar.setPreferredSize(new Dimension(SIDEBAR_EXPANDED, 0));
        sidebar.setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, CLR_BORDER));

        JPanel logoArea = new GradientPanel(CLR_SIDEBAR, new Color(0x0f2018));
        logoArea.setLayout(new BoxLayout(logoArea, BoxLayout.Y_AXIS));
        logoArea.setBorder(new EmptyBorder(24, 0, 20, 0));

        JLabel logoCircle = new JLabel("📚") {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(CLR_ACCENT);
                g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.12f));
                g2.fillOval(0, 0, getWidth(), getHeight());
                g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1f));
                super.paintComponent(g2);
                g2.dispose();
            }
        };
        logoCircle.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 28));
        logoCircle.setHorizontalAlignment(SwingConstants.CENTER);
        logoCircle.setPreferredSize(new Dimension(56, 56));
        logoCircle.setMaximumSize(new Dimension(56, 56));
        logoCircle.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel appName = new JLabel("NEUST Library");
        appName.setFont(new Font("Segoe UI", Font.BOLD, 15));
        appName.setForeground(CLR_TEXT);
        appName.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel roleTag = makeTag("Admin Portal");

        toggleBtn = new JButton("◀");
        toggleBtn.setFont(new Font("Segoe UI", Font.BOLD, 11));
        toggleBtn.setForeground(CLR_MUTED);
        toggleBtn.setBackground(CLR_SIDEBAR);
        toggleBtn.setBorderPainted(false);
        toggleBtn.setFocusPainted(false);
        toggleBtn.setContentAreaFilled(false);
        toggleBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        toggleBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        toggleBtn.addActionListener(e -> toggleSidebar());
        toggleBtn.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) { toggleBtn.setForeground(CLR_ACCENT); }
            public void mouseExited(MouseEvent e)  { toggleBtn.setForeground(CLR_MUTED); }
        });

        logoArea.add(Box.createVerticalStrut(4));
        logoArea.add(logoCircle);
        logoArea.add(Box.createVerticalStrut(10));
        logoArea.add(appName);
        logoArea.add(Box.createVerticalStrut(4));
        logoArea.add(roleTag);
        logoArea.add(Box.createVerticalStrut(12));
        logoArea.add(toggleBtn);
        sidebar.add(logoArea, BorderLayout.NORTH);

        JPanel nav = new JPanel();
        nav.setBackground(CLR_SIDEBAR);
        nav.setLayout(new BoxLayout(nav, BoxLayout.Y_AXIS));
        nav.setBorder(new EmptyBorder(10, 0, 10, 0));

        addSectionLabel(nav, "MAIN");
        addNavBtn(nav, "⊞", "Dashboard",          "DASHBOARD");
        addNavBtn(nav, "👥", "Manage Librarians",  "LIBRARIANS");

        addSectionLabel(nav, "CATALOG");
        addNavBtn(nav, "✍", "Manage Authors",      "AUTHORS");
        addNavBtn(nav, "🏷", "Manage Categories",  "CATEGORIES");
        addNavBtn(nav, "🏢", "Manage Publishers",  "PUBLISHERS");

        addSectionLabel(nav, "SYSTEM");
        addNavBtn(nav, "📊", "Reports",            "REPORTS");
        addNavBtn(nav, "⚙", "System Settings",    "SETTINGS");

        JScrollPane navScroll = new JScrollPane(nav);
        navScroll.setBorder(null);
        navScroll.setBackground(CLR_SIDEBAR);
        navScroll.getViewport().setBackground(CLR_SIDEBAR);
        navScroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        navScroll.getVerticalScrollBar().setPreferredSize(new Dimension(4, 0));
        sidebar.add(navScroll, BorderLayout.CENTER);

        JPanel bottom = new JPanel();
        bottom.setBackground(new Color(0x091410));
        bottom.setLayout(new BoxLayout(bottom, BoxLayout.Y_AXIS));
        bottom.setBorder(new EmptyBorder(10, 0, 10, 0));

        JSeparator sep = new JSeparator();
        sep.setForeground(CLR_BORDER);
        sep.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));
        bottom.add(sep);
        bottom.add(Box.createVerticalStrut(10));

        JButton userBtn = sidebarSpecialBtn("👤", currentAdmin.getFullName(), CLR_MUTED);
        userBtn.setName("userRow");
        bottom.add(userBtn);
        bottom.add(Box.createVerticalStrut(4));

        JButton logoutBtn = sidebarSpecialBtn("⏻", "Logout", CLR_ACCENT4);
        logoutBtn.setName("logoutBtn");
        logoutBtn.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) { logoutBtn.setBackground(new Color(0x3a0f0f)); }
            public void mouseExited(MouseEvent e)  { logoutBtn.setBackground(new Color(0, 0, 0, 0)); }
        });
        logoutBtn.addActionListener(e -> logout());
        bottom.add(logoutBtn);
        bottom.add(Box.createVerticalStrut(4));
        sidebar.add(bottom, BorderLayout.SOUTH);
        return sidebar;
    }

    private void addSectionLabel(JPanel nav, String text) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 9));
        lbl.setForeground(new Color(0x3a6045));
        lbl.setBorder(new EmptyBorder(14, 20, 4, 20));
        lbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        lbl.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
        lbl.setName("sectionLabel:" + text);
        nav.add(lbl);
    }

    private JButton sidebarSpecialBtn(String icon, String text, Color fg) {
        JButton btn = new JButton(icon + "  " + text);
        btn.setFont(FONT_BODY);
        btn.setForeground(fg);
        btn.setBackground(new Color(0, 0, 0, 0));
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setContentAreaFilled(false);
        btn.setHorizontalAlignment(SwingConstants.LEFT);
        btn.setBorder(new EmptyBorder(8, 20, 8, 20));
        btn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        btn.setAlignmentX(Component.CENTER_ALIGNMENT);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return btn;
    }

    private JLabel makeTag(String text) {
        JLabel tag = new JLabel(text) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(0x4ade80, false));
                g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.15f));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1f));
                super.paintComponent(g2);
                g2.dispose();
            }
        };
        tag.setFont(new Font("Segoe UI", Font.BOLD, 9));
        tag.setForeground(CLR_ACCENT);
        tag.setHorizontalAlignment(SwingConstants.CENTER);
        tag.setBorder(new EmptyBorder(3, 10, 3, 10));
        tag.setAlignmentX(Component.CENTER_ALIGNMENT);
        tag.setMaximumSize(new Dimension(120, 22));
        return tag;
    }

    private void addNavBtn(JPanel container, String icon, String text, String card) {
        JButton btn = new JButton(icon + "  " + text) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                if (getBackground() != CLR_SIDEBAR) {
                    g2.setColor(getBackground());
                    g2.fillRoundRect(6, 2, getWidth()-12, getHeight()-4, 10, 10);
                }
                super.paintComponent(g2);
                g2.dispose();
            }
        };
        btn.setOpaque(false);
        btn.setFont(FONT_NAV);
        btn.setForeground(CLR_MUTED);
        btn.setBackground(CLR_SIDEBAR);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setContentAreaFilled(false);
        btn.setHorizontalAlignment(SwingConstants.LEFT);
        btn.setBorder(new EmptyBorder(10, 20, 10, 20));
        btn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 46));
        btn.setAlignmentX(Component.CENTER_ALIGNMENT);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setName("nav");

        navEntries.add(new NavEntry(btn, icon, text, card));

        btn.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) {
                if (btn != activeNavBtn) {
                    btn.setBackground(CLR_SIDEBAR_HOV);
                    btn.setForeground(CLR_TEXT);
                }
            }
            public void mouseExited(MouseEvent e) {
                if (btn != activeNavBtn) {
                    btn.setBackground(CLR_SIDEBAR);
                    btn.setForeground(CLR_MUTED);
                }
            }
        });

        btn.addActionListener(e -> {
            if (activeNavBtn != null) {
                activeNavBtn.setBackground(CLR_SIDEBAR);
                activeNavBtn.setForeground(CLR_MUTED);
            }
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

    private void toggleSidebar() {
        sidebarExpanded    = !sidebarExpanded;
        sidebarTargetWidth = sidebarExpanded ? SIDEBAR_EXPANDED : SIDEBAR_COLLAPSED;
        toggleBtn.setText(sidebarExpanded ? "◀" : "▶");

        for (NavEntry ne : navEntries) {
            ne.btn.setText(sidebarExpanded ? ne.iconChar + "  " + ne.text : ne.iconChar);
            ne.btn.setHorizontalAlignment(sidebarExpanded ? SwingConstants.LEFT : SwingConstants.CENTER);
            ne.btn.setBorder(new EmptyBorder(10, sidebarExpanded ? 20 : 0, 10, sidebarExpanded ? 20 : 0));
        }

        for (Component c : ((JScrollPane)sidebar.getComponent(1)).getViewport().getComponent(0) instanceof JPanel nav
                ? ((JPanel)((JScrollPane)sidebar.getComponent(1)).getViewport().getComponent(0)).getComponents()
                : new Component[0]) {
            if (c instanceof JLabel lbl && lbl.getName() != null && lbl.getName().startsWith("sectionLabel:")) {
                lbl.setVisible(sidebarExpanded);
            }
        }

        JPanel bottomArea = (JPanel) sidebar.getComponent(2);
        for (Component c : bottomArea.getComponents()) {
            if (!(c instanceof JButton jb)) continue;
            if ("userRow".equals(jb.getName())) {
                jb.setText(sidebarExpanded ? "👤  " + currentAdmin.getFullName() : "👤");
                jb.setHorizontalAlignment(sidebarExpanded ? SwingConstants.LEFT : SwingConstants.CENTER);
                jb.setBorder(new EmptyBorder(8, sidebarExpanded ? 20 : 0, 8, sidebarExpanded ? 20 : 0));
            }
            if ("logoutBtn".equals(jb.getName())) {
                jb.setText(sidebarExpanded ? "⏻  Logout" : "⏻");
                jb.setHorizontalAlignment(sidebarExpanded ? SwingConstants.LEFT : SwingConstants.CENTER);
                jb.setBorder(new EmptyBorder(8, sidebarExpanded ? 20 : 0, 8, sidebarExpanded ? 20 : 0));
            }
        }

        if (sidebarTimer != null && sidebarTimer.isRunning()) sidebarTimer.stop();
        sidebarTimer = new Timer(10, null);
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

    // ── Top Bar ───────────────────────────────────────────────────────────────
    private JPanel buildTopBar() {
        JPanel bar = new GradientPanel(CLR_TOP_BAR, new Color(0x0a1a10));
        bar.setLayout(new BorderLayout());
        bar.setBorder(new CompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 1, 0, CLR_BORDER),
            new EmptyBorder(14, 24, 14, 24)
        ));

        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        left.setOpaque(false);
        JLabel title = new JLabel("Library Management System");
        title.setFont(new Font("Segoe UI", Font.BOLD, 16));
        title.setForeground(CLR_TEXT);
        JLabel sub = new JLabel("  —  Admin Portal");
        sub.setFont(FONT_BODY);
        sub.setForeground(CLR_MUTED);
        left.add(title);
        left.add(sub);

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 0));
        right.setOpaque(false);
        JLabel dateLbl = new JLabel(
            java.time.LocalDate.now().format(
                java.time.format.DateTimeFormatter.ofPattern("EEEE, MMMM dd, yyyy")));
        dateLbl.setFont(FONT_SMALL);
        dateLbl.setForeground(CLR_MUTED);
        JLabel adminBadge = new JLabel("● " + currentAdmin.getFullName());
        adminBadge.setFont(FONT_LABEL);
        adminBadge.setForeground(CLR_ACCENT);
        right.add(dateLbl);
        right.add(adminBadge);

        bar.add(left, BorderLayout.WEST);
        bar.add(right, BorderLayout.EAST);
        return bar;
    }

    // ── Dashboard Panel ───────────────────────────────────────────────────────
    private JPanel buildDashboardPanel() {
        JPanel p = new JPanel(new BorderLayout(0, 24));
        p.setBackground(CLR_BG);

        JPanel hdr = new JPanel(new BorderLayout());
        hdr.setOpaque(false);
        JLabel title = new JLabel("Dashboard Overview");
        title.setFont(FONT_TITLE);
        title.setForeground(CLR_TEXT);
        JLabel sub = new JLabel("Welcome back, " + currentAdmin.getFullName());
        sub.setFont(FONT_SMALL);
        sub.setForeground(CLR_MUTED);
        JPanel titleStack = new JPanel();
        titleStack.setOpaque(false);
        titleStack.setLayout(new BoxLayout(titleStack, BoxLayout.Y_AXIS));
        titleStack.add(title);
        titleStack.add(Box.createVerticalStrut(2));
        titleStack.add(sub);
        hdr.add(titleStack, BorderLayout.WEST);

        JButton refreshBtn = accentButton("↺  Refresh", CLR_ACCENT2);
        refreshBtn.addActionListener(e -> loadDashboardStats());
        hdr.add(refreshBtn, BorderLayout.EAST);
        p.add(hdr, BorderLayout.NORTH);

        JPanel statsRow = new JPanel(new GridLayout(1, 4, 16, 0));
        statsRow.setOpaque(false);
        lblBooks    = new JLabel("—", SwingConstants.CENTER);
        lblMembers  = new JLabel("—", SwingConstants.CENTER);
        lblBorrowed = new JLabel("—", SwingConstants.CENTER);
        lblOverdue  = new JLabel("—", SwingConstants.CENTER);
        statsRow.add(dashCard("📚", "Total Books",        lblBooks,    CLR_ACCENT,  "All catalog titles"));
        statsRow.add(dashCard("🧑‍🤝‍🧑", "Active Members",    lblMembers,  CLR_ACCENT2, "Registered users"));
        statsRow.add(dashCard("📖", "Currently Borrowed", lblBorrowed, CLR_ACCENT3, "Books on loan"));
        statsRow.add(dashCard("⚠",  "Overdue",            lblOverdue,  CLR_ACCENT4, "Past return date"));

        JPanel qa = new JPanel(new BorderLayout(0, 12));
        qa.setOpaque(false);
        JLabel qaHdr = new JLabel("Quick Actions");
        qaHdr.setFont(FONT_HEADER);
        qaHdr.setForeground(CLR_TEXT);
        qa.add(qaHdr, BorderLayout.NORTH);

        JPanel btnGrid = new JPanel(new GridLayout(2, 3, 12, 12));
        btnGrid.setOpaque(false);
        btnGrid.add(quickActionBtn("👥", "Add Librarian",     CLR_ACCENT,  () -> { cardLayout.show(contentPanel,"LIBRARIANS"); activateNav("LIBRARIANS"); }));
        btnGrid.add(quickActionBtn("📊", "View Reports",      CLR_ACCENT2, () -> { cardLayout.show(contentPanel,"REPORTS");    activateNav("REPORTS"); }));
        btnGrid.add(quickActionBtn("⚙",  "System Settings",  CLR_ACCENT3, () -> { cardLayout.show(contentPanel,"SETTINGS");   activateNav("SETTINGS"); }));
        btnGrid.add(quickActionBtn("✍",  "Manage Authors",   CLR_ACCENT,  () -> { cardLayout.show(contentPanel,"AUTHORS");    activateNav("AUTHORS"); }));
        btnGrid.add(quickActionBtn("🏷",  "Manage Categories",CLR_ACCENT2, () -> { cardLayout.show(contentPanel,"CATEGORIES");activateNav("CATEGORIES"); }));
        btnGrid.add(quickActionBtn("🏢",  "Manage Publishers",CLR_ACCENT3, () -> { cardLayout.show(contentPanel,"PUBLISHERS");activateNav("PUBLISHERS"); }));
        qa.add(btnGrid, BorderLayout.CENTER);

        JPanel center = new JPanel(new BorderLayout(0, 20));
        center.setOpaque(false);
        center.add(statsRow, BorderLayout.NORTH);
        center.add(qa, BorderLayout.CENTER);
        p.add(center, BorderLayout.CENTER);
        return p;
    }

    private void activateNav(String card) {
        for (NavEntry ne : navEntries) {
            if (ne.card.equals(card)) {
                if (activeNavBtn != null) {
                    activeNavBtn.setBackground(CLR_SIDEBAR);
                    activeNavBtn.setForeground(CLR_MUTED);
                }
                ne.btn.setBackground(CLR_SIDEBAR_SEL);
                ne.btn.setForeground(CLR_ACCENT);
                activeNavBtn = ne.btn;
                break;
            }
        }
    }

    private JPanel dashCard(String emoji, String title, JLabel valLbl, Color accent, String subtitle) {
        JPanel card = new JPanel(new BorderLayout(0, 8)) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(CLR_CARD);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 14, 14);
                g2.setColor(accent);
                g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.08f));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 14, 14);
                g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1f));
                g2.setColor(accent);
                g2.setStroke(new BasicStroke(2f));
                g2.drawRoundRect(1, 1, getWidth()-2, getHeight()-2, 14, 14);
                g2.dispose();
            }
        };
        card.setOpaque(false);
        card.setBorder(new EmptyBorder(20, 20, 20, 20));

        JPanel top = new JPanel(new BorderLayout());
        top.setOpaque(false);
        JLabel emojiLbl = new JLabel(emoji);
        emojiLbl.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 24));
        JLabel subLbl = new JLabel(subtitle);
        subLbl.setFont(FONT_SMALL);
        subLbl.setForeground(CLR_MUTED);
        top.add(emojiLbl, BorderLayout.WEST);
        top.add(subLbl, BorderLayout.EAST);

        valLbl.setFont(FONT_STAT);
        valLbl.setForeground(CLR_TEXT);

        JLabel titleLbl = new JLabel(title);
        titleLbl.setFont(FONT_LABEL);
        titleLbl.setForeground(CLR_MUTED);

        card.add(top, BorderLayout.NORTH);
        card.add(valLbl, BorderLayout.CENTER);
        card.add(titleLbl, BorderLayout.SOUTH);
        return card;
    }

    private JButton quickActionBtn(String emoji, String text, Color accent, Runnable action) {
        JButton btn = new JButton(emoji + "  " + text) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(isEnabled() ? getBackground() : CLR_CARD);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
                super.paintComponent(g2);
                g2.dispose();
            }
        };
        btn.setOpaque(false);
        btn.setFont(FONT_BODY);
        btn.setForeground(CLR_TEXT);
        btn.setBackground(CLR_CARD2);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setContentAreaFilled(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setHorizontalAlignment(SwingConstants.LEFT);
        btn.setBorder(new EmptyBorder(16, 16, 16, 16));
        btn.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) { btn.setBackground(accent); btn.setForeground(Color.WHITE); }
            public void mouseExited(MouseEvent e)  { btn.setBackground(CLR_CARD2); btn.setForeground(CLR_TEXT); }
        });
        btn.addActionListener(e -> action.run());
        return btn;
    }

    // ── Librarians Panel ──────────────────────────────────────────────────────
    private JPanel buildLibrariansPanel() {
        JPanel p = new JPanel(new BorderLayout(0, 16));
        p.setBackground(CLR_BG);
        p.add(panelHeader("👥  Manage Librarians", "Add, activate, or deactivate librarian accounts"), BorderLayout.NORTH);

        String[] cols = {"ID", "Employee ID", "Full Name", "Email", "Contact", "Status"};
        libTableModel = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        libTable = new JTable(libTableModel);
        styleTable(libTable);
        libTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        p.add(styledScrollPane(libTable), BorderLayout.CENTER);

        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 8));
        btnRow.setOpaque(false);
        btnAdd    = accentButton("＋  Add Librarian", CLR_ACCENT);
        btnToggle = accentButton("⇄  Toggle Status",  CLR_ACCENT2);
        btnAdd   .addActionListener(e -> showAddLibrarianDialog());
        btnToggle.addActionListener(e -> toggleLibrarianStatus());
        btnRow.add(btnAdd);
        btnRow.add(btnToggle);
        p.add(btnRow, BorderLayout.SOUTH);
        return p;
    }
    // ── Settings Panel ────────────────────────────────────────────────────────
    private JPanel buildSettingsPanel() {
        JPanel outer = new JPanel(new BorderLayout(0, 16));
        outer.setBackground(CLR_BG);
        outer.add(panelHeader("⚙  System Settings", "Configure borrowing rules and fine rates"), BorderLayout.NORTH);

        JPanel card = new JPanel(new GridBagLayout()) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(CLR_CARD);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 16, 16);
                g2.setColor(CLR_BORDER);
                g2.drawRoundRect(0, 0, getWidth()-1, getHeight()-1, 16, 16);
                g2.dispose();
            }
        };
        card.setOpaque(false);
        card.setBorder(new EmptyBorder(28, 36, 28, 36));

        GridBagConstraints gc = new GridBagConstraints();
        gc.fill = GridBagConstraints.HORIZONTAL;
        gc.insets = new Insets(10, 0, 10, 0);
        gc.gridwidth = GridBagConstraints.REMAINDER;

        JLabel hdr = new JLabel("Borrowing & Fine Configuration");
        hdr.setFont(FONT_HEADER);
        hdr.setForeground(CLR_TEXT);
        card.add(hdr, gc);

        JSeparator sep = new JSeparator();
        sep.setForeground(CLR_BORDER);
        card.add(sep, gc);

        gc.insets = new Insets(8, 0, 8, 0);
        tfBorrowDays = addSettingRow(card, gc, "📅  Max Borrow Days",      "days");
        tfFineRate   = addSettingRow(card, gc, "💰  Fine Rate per Day",    "₱ per day");
        tfMaxBorrow  = addSettingRow(card, gc, "📚  Max Books per Member", "books");

        gc.insets = new Insets(20, 0, 0, 0);
        btnSave = accentButton("💾  Save Settings", CLR_ACCENT);
        btnSave.addActionListener(e -> saveSettings());
        card.add(btnSave, gc);

        JPanel center = new JPanel(new GridBagLayout());
        center.setOpaque(false);
        center.add(card, new GridBagConstraints());
        outer.add(center, BorderLayout.CENTER);
        return outer;
    }

    private JTextField addSettingRow(JPanel panel, GridBagConstraints gc, String label, String hint) {
        gc.gridwidth = 1; gc.weightx = 0.45;
        JLabel lbl = new JLabel(label);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lbl.setForeground(CLR_TEXT);
        panel.add(lbl, gc);

        gc.weightx = 0.55; gc.gridwidth = GridBagConstraints.REMAINDER;
        JTextField tf = new JTextField(14);
        tf.setFont(FONT_BODY);
        tf.setForeground(CLR_TEXT);
        tf.setBackground(CLR_CARD2);
        tf.setCaretColor(CLR_ACCENT);
        tf.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(CLR_BORDER, 1, true),
            new EmptyBorder(8, 12, 8, 12)
        ));
        panel.add(tf, gc);
        return tf;
    }

    // ── Shared Helpers ────────────────────────────────────────────────────────
    private JPanel panelHeader(String title, String subtitle) {
        JPanel hdr = new JPanel();
        hdr.setOpaque(false);
        hdr.setLayout(new BoxLayout(hdr, BoxLayout.Y_AXIS));
        JLabel t = new JLabel(title);
        t.setFont(FONT_TITLE);
        t.setForeground(CLR_TEXT);
        JLabel s = new JLabel(subtitle);
        s.setFont(FONT_SMALL);
        s.setForeground(CLR_MUTED);
        hdr.add(t);
        hdr.add(Box.createVerticalStrut(3));
        hdr.add(s);
        hdr.add(Box.createVerticalStrut(6));
        JSeparator sep = new JSeparator();
        sep.setForeground(CLR_BORDER);
        sep.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));
        hdr.add(sep);
        return hdr;
    }

    private JButton accentButton(String text, Color accent) {
        JButton btn = new JButton(text) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(isEnabled() ? getBackground() : CLR_CARD);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                super.paintComponent(g2);
                g2.dispose();
            }
        };
        btn.setOpaque(false);
        btn.setFont(FONT_BODY);
        btn.setForeground(Color.WHITE);
        btn.setBackground(accent);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setContentAreaFilled(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setBorder(new EmptyBorder(9, 18, 9, 18));
        Color darker = accent.darker();
        btn.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) { if (btn.isEnabled()) btn.setBackground(darker); }
            public void mouseExited(MouseEvent e)  { if (btn.isEnabled()) btn.setBackground(accent); }
        });
        return btn;
    }

    private JScrollPane styledScrollPane(JTable table) {
        JScrollPane sp = new JScrollPane(table) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(CLR_CARD);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        sp.setOpaque(false);
        sp.getViewport().setOpaque(false);
        sp.setBorder(BorderFactory.createLineBorder(CLR_BORDER, 1));
        sp.getVerticalScrollBar().setBackground(CLR_CARD);
        return sp;
    }

    private static void styleTable(JTable table) {
        table.setFont(FONT_BODY);
        table.setForeground(CLR_TEXT);
        table.setBackground(CLR_CARD);
        table.setRowHeight(34);
        table.setSelectionBackground(new Color(0x4ade80, false).darker());
        table.setSelectionForeground(Color.WHITE);
        table.setGridColor(CLR_BORDER);
        table.setShowVerticalLines(false);
        table.setIntercellSpacing(new Dimension(0, 1));

        JTableHeader h = table.getTableHeader();
        h.setFont(FONT_LABEL);
        h.setBackground(new Color(0x0a1a10));
        h.setForeground(CLR_ACCENT);
        h.setReorderingAllowed(false);
        h.setPreferredSize(new Dimension(0, 40));
        h.setBorder(BorderFactory.createMatteBorder(0, 0, 2, 0, CLR_ACCENT));

        table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override public Component getTableCellRendererComponent(
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
                }
                if (val instanceof String s) {
                    if (s.equals("Active"))        setForeground(CLR_ACCENT);
                    else if (s.equals("Inactive")) setForeground(CLR_ACCENT4);
                }
                return this;
            }
        });
    }

    // ── Gradient Panel ────────────────────────────────────────────────────────
    private static class GradientPanel extends JPanel {
        private final Color c1, c2;
        GradientPanel(Color c1, Color c2) { this.c1=c1; this.c2=c2; setOpaque(false); }
        @Override protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setPaint(new GradientPaint(0, 0, c1, getWidth(), getHeight(), c2));
            g2.fillRect(0, 0, getWidth(), getHeight());
            g2.dispose();
            super.paintComponent(g);
        }
    }

    // ── Data Loaders ──────────────────────────────────────────────────────────
    private void loadDashboardStats() {
        lblBooks.setText("…"); lblMembers.setText("…");
        lblBorrowed.setText("…"); lblOverdue.setText("…");
        new SwingWorker<int[], Void>() {
            @Override protected int[] doInBackground() { return adminDAO.getSummaryReport(); }
            @Override protected void done() {
                try {
                    int[] s = get();
                    lblBooks.setText(String.valueOf(s[0]));
                    lblMembers.setText(String.valueOf(s[1]));
                    lblBorrowed.setText(String.valueOf(s[2]));
                    lblOverdue.setText(String.valueOf(s[3]));
                } catch (Exception ex) {
                    lblBooks.setText("!"); lblMembers.setText("!");
                    lblBorrowed.setText("!"); lblOverdue.setText("!");
                    JOptionPane.showMessageDialog(AdminDashboard.this,
                        "Error loading stats: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        }.execute();
    }

    private void loadLibrarians() {
        btnAdd.setEnabled(false); btnToggle.setEnabled(false);
        new SwingWorker<List<Librarian>, Void>() {
            @Override protected List<Librarian> doInBackground() { return adminDAO.getAllLibrarians(); }
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
                        "Error loading librarians: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                } finally { btnAdd.setEnabled(true); btnToggle.setEnabled(true); }
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
                    String[] v = get();
                    tfBorrowDays.setText(orDefault(v[0], "7"));
                    tfFineRate  .setText(orDefault(v[1], "5"));
                    tfMaxBorrow .setText(orDefault(v[2], "3"));
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(AdminDashboard.this,
                        "Error loading settings: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
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
        JPanel dlg = new JPanel(new GridLayout(6, 2, 10, 10));
        dlg.setBorder(new EmptyBorder(10, 10, 10, 10));
        dlg.setBackground(CLR_CARD);

        JTextField     tfEmpID   = darkField();
        JTextField     tfFirst   = darkField();
        JTextField     tfLast    = darkField();
        JTextField     tfEmail   = darkField();
        JTextField     tfContact = darkField();
        JPasswordField tfPass    = new JPasswordField();
        styleField(tfPass);

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
            lib.setEmployeeID(tfEmpID.getText().trim());
            lib.setFirstName(tfFirst.getText().trim());
            lib.setLastName(tfLast.getText().trim());
            lib.setEmail(tfEmail.getText().trim());
            lib.setContactNumber(tfContact.getText().trim());
            String pass = new String(tfPass.getPassword());

            btnAdd.setEnabled(false); btnToggle.setEnabled(false);
            new SwingWorker<Boolean, Void>() {
                @Override protected Boolean doInBackground() { return adminDAO.addLibrarian(lib, pass); }
                @Override protected void done() {
                    try {
                        if (get()) { JOptionPane.showMessageDialog(AdminDashboard.this, "Librarian added."); loadLibrarians(); }
                        else JOptionPane.showMessageDialog(AdminDashboard.this, "Failed to add librarian.", "Error", JOptionPane.ERROR_MESSAGE);
                    } catch (Exception ex) {
                        JOptionPane.showMessageDialog(AdminDashboard.this, "Error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                    } finally { btnAdd.setEnabled(true); btnToggle.setEnabled(true); }
                }
            }.execute();
        }
    }

    private void toggleLibrarianStatus() {
        int row = libTable.getSelectedRow();
        if (row < 0) { JOptionPane.showMessageDialog(this, "Select a librarian first."); return; }
        int     id       = (int)    libTableModel.getValueAt(row, 0);
        String  status   = (String) libTableModel.getValueAt(row, 5);
        boolean isActive = "Active".equals(status);

        int confirm = JOptionPane.showConfirmDialog(this,
            "Are you sure you want to " + (isActive ? "deactivate" : "activate") + " this librarian?",
            "Confirm", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            btnAdd.setEnabled(false); btnToggle.setEnabled(false);
            new SwingWorker<Boolean, Void>() {
                @Override protected Boolean doInBackground() {
                    return isActive ? adminDAO.deactivateLibrarian(id) : adminDAO.activateLibrarian(id);
                }
                @Override protected void done() {
                    try {
                        if (get()) loadLibrarians();
                        else JOptionPane.showMessageDialog(AdminDashboard.this, "Operation failed.", "Error", JOptionPane.ERROR_MESSAGE);
                    } catch (Exception ex) {
                        JOptionPane.showMessageDialog(AdminDashboard.this, "Error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                    } finally { btnAdd.setEnabled(true); btnToggle.setEnabled(true); }
                }
            }.execute();
        }
    }

    private void saveSettings() {
        String borrowDays = tfBorrowDays.getText().trim();
        String fineRate   = tfFineRate  .getText().trim();
        String maxBorrow  = tfMaxBorrow .getText().trim();
        int    adminID    = currentAdmin.getAdminId();

        btnSave.setEnabled(false); btnSave.setText("💾  Saving…");
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
                    if (r[0] && r[1] && r[2]) JOptionPane.showMessageDialog(AdminDashboard.this, "Settings saved successfully.");
                    else JOptionPane.showMessageDialog(AdminDashboard.this, "Some settings failed to save.", "Warning", JOptionPane.WARNING_MESSAGE);
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(AdminDashboard.this, "Error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                } finally { btnSave.setEnabled(true); btnSave.setText("💾  Save Settings"); }
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

    // ── Field Helpers ─────────────────────────────────────────────────────────
    private JTextField darkField() {
        JTextField tf = new JTextField();
        styleField(tf);
        return tf;
    }

    private void styleField(JTextField tf) {
        tf.setFont(FONT_BODY);
        tf.setForeground(CLR_TEXT);
        tf.setBackground(CLR_CARD2);
        tf.setCaretColor(CLR_ACCENT);
        tf.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(CLR_BORDER, 1),
            new EmptyBorder(6, 10, 6, 10)
        ));
    }

    private static JLabel styledLabel(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(FONT_LABEL);
        lbl.setForeground(CLR_TEXT);
        return lbl;
    }
}