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
    private static final Color CLR_BG          = new Color(0x080e0a);
    private static final Color CLR_SIDEBAR     = new Color(0x0b1410);
    private static final Color CLR_SURFACE     = new Color(0x101a13);
    private static final Color CLR_CARD        = new Color(0x111a14);
    private static final Color CLR_CARD2       = new Color(0x141f17);
    private static final Color CLR_TOPBAR      = new Color(0x080e0a);
    private static final Color CLR_SIDEBAR_SEL = new Color(0x162019);
    private static final Color CLR_SIDEBAR_HOV = new Color(0x111c14);

    private static final Color CLR_ACCENT      = new Color(0x4ade80);
    private static final Color CLR_ACCENT2     = new Color(0x22d3ee);
    private static final Color CLR_ACCENT3     = new Color(0xfbbf24);
    private static final Color CLR_ACCENT4     = new Color(0xf87171);
    private static final Color CLR_ACCENT_DIM  = new Color(0x4ade80, false);

    private static final Color CLR_TEXT        = new Color(0xe8f5ee);
    private static final Color CLR_MUTED       = new Color(0x5a8a6a);
    private static final Color CLR_MUTED2      = new Color(0x3d6350);
    private static final Color CLR_BORDER      = new Color(74, 222, 128, 25);   // rgba accent @10%
    private static final Color CLR_BORDER2     = new Color(74, 222, 128, 46);   // rgba accent @18%

    private static final Font FONT_TITLE  = new Font("DM Sans",   Font.BOLD,   20);
    private static final Font FONT_NAV    = new Font("DM Sans",   Font.BOLD,   13);
    private static final Font FONT_HEADER = new Font("DM Sans",   Font.BOLD,   16);
    private static final Font FONT_BODY   = new Font("DM Sans",   Font.PLAIN,  13);
    private static final Font FONT_LABEL  = new Font("DM Sans",   Font.BOLD,   11);
    private static final Font FONT_STAT   = new Font("DM Mono",   Font.BOLD,   32);
    private static final Font FONT_SMALL  = new Font("DM Sans",   Font.PLAIN,  11);
    private static final Font FONT_MONO   = new Font("DM Mono",   Font.PLAIN,  12);
    private static final Font FONT_TINY   = new Font("DM Sans",   Font.BOLD,    9);

    private static final int SIDEBAR_EXPANDED  = 228;
    private static final int SIDEBAR_COLLAPSED = 58;
    private static final int CORNER            = 10;

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

    private ManageAuthorsPanel    authorsPanel;
    private ManageCategoriesPanel categoriesPanel;
    private ManagePublishersPanel publishersPanel;

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
        root.add(buildSidebar(),  BorderLayout.WEST);
        root.add(buildTopBar(),   BorderLayout.NORTH);

        cardLayout   = new CardLayout();
        contentPanel = new JPanel(cardLayout);
        contentPanel.setBackground(CLR_BG);
        contentPanel.setBorder(new EmptyBorder(26, 28, 26, 28));

        authorsPanel    = new ManageAuthorsPanel(currentAdmin);
        categoriesPanel = new ManageCategoriesPanel(currentAdmin);
        publishersPanel = new ManagePublishersPanel(currentAdmin);

        contentPanel.add(buildDashboardPanel(),         "DASHBOARD");
        contentPanel.add(buildLibrariansPanel(),        "LIBRARIANS");
        contentPanel.add(authorsPanel,                  "AUTHORS");
        contentPanel.add(categoriesPanel,               "CATEGORIES");
        contentPanel.add(publishersPanel,               "PUBLISHERS");
        contentPanel.add(new ReportsPanel(currentAdmin),"REPORTS");
        contentPanel.add(buildSettingsPanel(),          "SETTINGS");

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

        // ── Logo Zone ────────────────────────────────────────────────────────
        JPanel logoArea = new JPanel();
        logoArea.setBackground(CLR_SIDEBAR);
        logoArea.setLayout(new BoxLayout(logoArea, BoxLayout.Y_AXIS));
        logoArea.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 1, 0, CLR_BORDER),
            new EmptyBorder(20, 0, 18, 0)
        ));

        JPanel logoRow = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        logoRow.setOpaque(false);

        JLabel logoIcon = new JLabel("📚") {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                // subtle gradient bg
                GradientPaint gp = new GradientPaint(0,0,
                    new Color(74,222,128,46), getWidth(),getHeight(),
                    new Color(34,211,238,30));
                g2.setPaint(gp);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 9, 9);
                g2.setColor(new Color(74,222,128,64));
                g2.setStroke(new BasicStroke(1f));
                g2.drawRoundRect(0, 0, getWidth()-1, getHeight()-1, 9, 9);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        logoIcon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 16));
        logoIcon.setHorizontalAlignment(SwingConstants.CENTER);
        logoIcon.setPreferredSize(new Dimension(34, 34));
        logoIcon.setBorder(new EmptyBorder(4, 4, 4, 4));

        JPanel textStack = new JPanel();
        textStack.setOpaque(false);
        textStack.setLayout(new BoxLayout(textStack, BoxLayout.Y_AXIS));
        JLabel appName = new JLabel("NEUST Library");
        appName.setFont(new Font("DM Sans", Font.BOLD, 13));
        appName.setForeground(CLR_TEXT);
        JLabel appSub = new JLabel("Management System");
        appSub.setFont(FONT_SMALL);
        appSub.setForeground(CLR_MUTED);
        textStack.add(appName);
        textStack.add(appSub);

        logoRow.add(logoIcon);
        logoRow.add(textStack);
        logoArea.add(logoRow);
        logoArea.add(Box.createVerticalStrut(10));
        logoArea.add(makeRolePill("● Admin Portal"));

        sidebar.add(logoArea, BorderLayout.NORTH);

        // ── Nav ──────────────────────────────────────────────────────────────
        JPanel nav = new JPanel();
        nav.setBackground(CLR_SIDEBAR);
        nav.setLayout(new BoxLayout(nav, BoxLayout.Y_AXIS));
        nav.setBorder(new EmptyBorder(14, 0, 14, 0));

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
        navScroll.getVerticalScrollBar().setPreferredSize(new Dimension(2, 0));
        sidebar.add(navScroll, BorderLayout.CENTER);

        // ── Footer ───────────────────────────────────────────────────────────
        JPanel bottom = new JPanel();
        bottom.setBackground(CLR_SIDEBAR);
        bottom.setLayout(new BoxLayout(bottom, BoxLayout.Y_AXIS));
        bottom.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(1, 0, 0, 0, CLR_BORDER),
            new EmptyBorder(10, 0, 10, 0)
        ));

        // User row
        JPanel userRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 6));
        userRow.setOpaque(false);
        userRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 48));
        JLabel avatar = makeAvatar(getInitials(currentAdmin.getFullName()));
        JPanel userInfo = new JPanel();
        userInfo.setOpaque(false);
        userInfo.setLayout(new BoxLayout(userInfo, BoxLayout.Y_AXIS));
        JLabel userName = new JLabel(currentAdmin.getFullName());
        userName.setFont(new Font("DM Sans", Font.BOLD, 12));
        userName.setForeground(CLR_TEXT);
        userName.setName("userName");
        JLabel userRole = new JLabel("System Administrator");
        userRole.setFont(FONT_SMALL);
        userRole.setForeground(CLR_MUTED);
        userInfo.add(userName);
        userInfo.add(userRole);
        userRow.add(avatar);
        userRow.add(userInfo);
        userRow.setName("userRow");

        bottom.add(userRow);
        bottom.add(Box.createVerticalStrut(4));

        JButton logoutBtn = makeLogoutBtn();
        logoutBtn.setName("logoutBtn");
        logoutBtn.addActionListener(e -> logout());
        bottom.add(logoutBtn);

        sidebar.add(bottom, BorderLayout.SOUTH);

        // ── Toggle btn ────────────────────────────────────────────────────────
        toggleBtn = new JButton("◀");
        toggleBtn.setFont(new Font("DM Sans", Font.BOLD, 10));
        toggleBtn.setForeground(CLR_MUTED);
        toggleBtn.setBackground(CLR_SIDEBAR);
        toggleBtn.setBorderPainted(false);
        toggleBtn.setFocusPainted(false);
        toggleBtn.setContentAreaFilled(false);
        toggleBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        toggleBtn.addActionListener(e -> toggleSidebar());
        toggleBtn.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) { toggleBtn.setForeground(CLR_ACCENT); }
            public void mouseExited(MouseEvent e)  { toggleBtn.setForeground(CLR_MUTED);  }
        });

        return sidebar;
    }

    private JLabel makeAvatar(String initials) {
        JLabel lbl = new JLabel(initials, SwingConstants.CENTER) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                GradientPaint gp = new GradientPaint(0,0,
                    new Color(74,222,128,64), getWidth(),getHeight(),
                    new Color(34,211,238,38));
                g2.setPaint(gp);
                g2.fillOval(0, 0, getWidth(), getHeight());
                g2.setColor(new Color(74,222,128,76));
                g2.setStroke(new BasicStroke(1f));
                g2.drawOval(0, 0, getWidth()-1, getHeight()-1);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        lbl.setFont(new Font("DM Mono", Font.BOLD, 11));
        lbl.setForeground(CLR_ACCENT);
        lbl.setPreferredSize(new Dimension(28, 28));
        return lbl;
    }

    private String getInitials(String name) {
        if (name == null || name.isBlank()) return "AD";
        String[] parts = name.trim().split("\\s+");
        if (parts.length == 1) return parts[0].substring(0, Math.min(2, parts[0].length())).toUpperCase();
        return ("" + parts[0].charAt(0) + parts[parts.length-1].charAt(0)).toUpperCase();
    }

    private JLabel makeRolePill(String text) {
        JLabel pill = new JLabel(text, SwingConstants.CENTER) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(74,222,128,20));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), getHeight(), getHeight());
                g2.setColor(new Color(74,222,128,46));
                g2.setStroke(new BasicStroke(1f));
                g2.drawRoundRect(0, 0, getWidth()-1, getHeight()-1, getHeight(), getHeight());
                g2.dispose();
                super.paintComponent(g);
            }
        };
        pill.setFont(FONT_TINY);
        pill.setForeground(CLR_ACCENT);
        pill.setBorder(new EmptyBorder(4, 12, 4, 12));
        pill.setAlignmentX(Component.CENTER_ALIGNMENT);
        pill.setMaximumSize(new Dimension(140, 22));
        return pill;
    }

    private JButton makeLogoutBtn() {
        JButton btn = new JButton("⏻  Logout") {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                if (!getBackground().equals(CLR_SIDEBAR)) {
                    g2.setColor(getBackground());
                    g2.fillRoundRect(6, 2, getWidth()-12, getHeight()-4, 8, 8);
                    g2.setColor(new Color(248,113,113,50));
                    g2.setStroke(new BasicStroke(1f));
                    g2.drawRoundRect(6, 2, getWidth()-13, getHeight()-5, 8, 8);
                }
                super.paintComponent(g2);
                g2.dispose();
            }
        };
        btn.setOpaque(false);
        btn.setFont(new Font("DM Sans", Font.BOLD, 12));
        btn.setForeground(CLR_ACCENT4);
        btn.setBackground(CLR_SIDEBAR);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setContentAreaFilled(false);
        btn.setHorizontalAlignment(SwingConstants.LEFT);
        btn.setBorder(new EmptyBorder(8, 20, 8, 20));
        btn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        btn.setAlignmentX(Component.LEFT_ALIGNMENT);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) { btn.setBackground(new Color(248,113,113,20)); }
            public void mouseExited(MouseEvent e)  { btn.setBackground(CLR_SIDEBAR); }
        });
        return btn;
    }

    private void addSectionLabel(JPanel nav, String text) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(FONT_TINY);
        lbl.setForeground(CLR_MUTED2);
        lbl.setBorder(new EmptyBorder(14, 20, 5, 20));
        lbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        lbl.setMaximumSize(new Dimension(Integer.MAX_VALUE, 28));
        lbl.setName("sectionLabel:" + text);
        nav.add(lbl);
    }

    private void addNavBtn(JPanel container, String icon, String text, String card) {
        JButton btn = new JButton(icon + "  " + text) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                Color bg = getBackground();
                if (!bg.equals(CLR_SIDEBAR)) {
                    g2.setColor(bg);
                    g2.fillRoundRect(6, 2, getWidth()-12, getHeight()-4, 8, 8);
                    if (bg.equals(CLR_SIDEBAR_SEL)) {
                        // active indicator bar
                        g2.setColor(CLR_ACCENT);
                        g2.fillRoundRect(6, (getHeight()-20)/2, 3, 20, 3, 3);
                        // border
                        g2.setColor(CLR_BORDER2);
                        g2.setStroke(new BasicStroke(1f));
                        g2.drawRoundRect(6, 2, getWidth()-13, getHeight()-5, 8, 8);
                    }
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
        btn.setBorder(new EmptyBorder(9, 18, 9, 18));
        btn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
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
            if ("LIBRARIANS".equals(card))  loadLibrarians();
            if ("SETTINGS".equals(card))    loadSettings();
            if ("AUTHORS".equals(card))     authorsPanel.refresh();
            if ("CATEGORIES".equals(card))  categoriesPanel.refresh();
            if ("PUBLISHERS".equals(card))  publishersPanel.refresh();
        });

        container.add(btn);
        container.add(Box.createVerticalStrut(1));
    }

    private void toggleSidebar() {
        sidebarExpanded    = !sidebarExpanded;
        sidebarTargetWidth = sidebarExpanded ? SIDEBAR_EXPANDED : SIDEBAR_COLLAPSED;
        toggleBtn.setText(sidebarExpanded ? "◀" : "▶");

        for (NavEntry ne : navEntries) {
            ne.btn.setText(sidebarExpanded ? ne.iconChar + "  " + ne.text : ne.iconChar);
            ne.btn.setHorizontalAlignment(sidebarExpanded ? SwingConstants.LEFT : SwingConstants.CENTER);
            ne.btn.setBorder(new EmptyBorder(9, sidebarExpanded ? 18 : 0, 9, sidebarExpanded ? 18 : 0));
        }

        for (Component c : ((JScrollPane) sidebar.getComponent(1)).getViewport().getComponent(0) instanceof JPanel nav
                ? ((JPanel) ((JScrollPane) sidebar.getComponent(1)).getViewport().getComponent(0)).getComponents()
                : new Component[0]) {
            if (c instanceof JLabel lbl && lbl.getName() != null && lbl.getName().startsWith("sectionLabel:")) {
                lbl.setVisible(sidebarExpanded);
            }
        }

        if (sidebarTimer != null && sidebarTimer.isRunning()) sidebarTimer.stop();
        sidebarTimer = new Timer(10, null);
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

    // ── Top Bar ───────────────────────────────────────────────────────────────
    private JPanel buildTopBar() {
        JPanel bar = new JPanel(new BorderLayout()) {
            @Override protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setColor(CLR_BORDER);
                g2.fillRect(0, getHeight()-1, getWidth(), 1);
                g2.dispose();
            }
        };
        bar.setBackground(CLR_TOPBAR);
        bar.setBorder(new EmptyBorder(0, 24, 0, 24));
        bar.setPreferredSize(new Dimension(0, 54));

        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        left.setOpaque(false);

        JLabel pageLbl = new JLabel("Dashboard");
        pageLbl.setFont(new Font("DM Sans", Font.BOLD, 15));
        pageLbl.setForeground(CLR_TEXT);

        JLabel sep2 = new JLabel("·");
        sep2.setForeground(CLR_MUTED2);
        sep2.setFont(new Font("DM Sans", Font.PLAIN, 14));

        JLabel subLbl = new JLabel("Overview");
        subLbl.setFont(FONT_BODY);
        subLbl.setForeground(CLR_MUTED);

        left.add(pageLbl);
        left.add(sep2);
        left.add(subLbl);

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 14, 0));
        right.setOpaque(false);

        JLabel dateLbl = new JLabel(
            java.time.LocalDate.now().format(
                java.time.format.DateTimeFormatter.ofPattern("EEE, MMM dd, yyyy")));
        dateLbl.setFont(new Font("DM Mono", Font.PLAIN, 11));
        dateLbl.setForeground(CLR_MUTED);

        // Admin badge
        JLabel adminBadge = new JLabel("● " + currentAdmin.getFullName()) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(74,222,128,18));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), getHeight(), getHeight());
                g2.setColor(new Color(74,222,128,38));
                g2.setStroke(new BasicStroke(1f));
                g2.drawRoundRect(0, 0, getWidth()-1, getHeight()-1, getHeight(), getHeight());
                g2.dispose();
                super.paintComponent(g);
            }
        };
        adminBadge.setFont(new Font("DM Sans", Font.BOLD, 11));
        adminBadge.setForeground(CLR_ACCENT);
        adminBadge.setBorder(new EmptyBorder(4, 12, 4, 12));
        adminBadge.setOpaque(false);

        right.add(dateLbl);
        right.add(adminBadge);

        bar.add(left,  BorderLayout.WEST);
        bar.add(right, BorderLayout.EAST);
        return bar;
    }

    // ── Dashboard Panel ───────────────────────────────────────────────────────
    private JPanel buildDashboardPanel() {
        JPanel p = new JPanel(new BorderLayout(0, 22));
        p.setBackground(CLR_BG);

        // Header
        JPanel hdr = new JPanel(new BorderLayout());
        hdr.setOpaque(false);
        JPanel titleStack = new JPanel();
        titleStack.setOpaque(false);
        titleStack.setLayout(new BoxLayout(titleStack, BoxLayout.Y_AXIS));
        JLabel title = new JLabel("Dashboard Overview");
        title.setFont(FONT_TITLE);
        title.setForeground(CLR_TEXT);
        JLabel sub = new JLabel("Welcome back, " + currentAdmin.getFullName() + ". Here's what's happening.");
        sub.setFont(FONT_SMALL);
        sub.setForeground(CLR_MUTED);
        titleStack.add(title);
        titleStack.add(Box.createVerticalStrut(3));
        titleStack.add(sub);
        hdr.add(titleStack, BorderLayout.WEST);

        JButton refreshBtn = accentButton("↺  Refresh", CLR_ACCENT2, false);
        refreshBtn.addActionListener(e -> loadDashboardStats());
        hdr.add(refreshBtn, BorderLayout.EAST);
        p.add(hdr, BorderLayout.NORTH);

        // Stats row
        JPanel statsRow = new JPanel(new GridLayout(1, 4, 14, 0));
        statsRow.setOpaque(false);
        lblBooks    = new JLabel("—", SwingConstants.LEFT);
        lblMembers  = new JLabel("—", SwingConstants.LEFT);
        lblBorrowed = new JLabel("—", SwingConstants.LEFT);
        lblOverdue  = new JLabel("—", SwingConstants.LEFT);
        statsRow.add(dashCard("📚", "Total Books",        lblBooks,    CLR_ACCENT,  "All catalog titles",   "↑ catalog"));
        statsRow.add(dashCard("🧑‍🤝‍🧑", "Active Members",   lblMembers,  CLR_ACCENT2, "Registered users",     "↑ registered"));
        statsRow.add(dashCard("📖", "Currently Borrowed", lblBorrowed, CLR_ACCENT3, "Books on loan",        "on loan now"));
        statsRow.add(dashCard("⚠",  "Overdue",            lblOverdue,  CLR_ACCENT4, "Past return date",     "past due"));

        // Quick Actions
        JPanel qa = new JPanel(new BorderLayout(0, 14));
        qa.setOpaque(false);

        JPanel qaHdrRow = new JPanel(new BorderLayout());
        qaHdrRow.setOpaque(false);
        JLabel qaHdr = new JLabel("Quick Actions");
        qaHdr.setFont(new Font("DM Sans", Font.BOLD, 14));
        qaHdr.setForeground(CLR_TEXT);
        JSeparator qaSep = new JSeparator();
        qaSep.setForeground(CLR_BORDER);
        // wrap in panel to align
        JPanel qaHdrWrap = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        qaHdrWrap.setOpaque(false);
        qaHdrWrap.add(qaHdr);
        qaHdrRow.add(qaHdrWrap, BorderLayout.WEST);
        qaHdrRow.add(qaSep, BorderLayout.CENTER);
        qa.add(qaHdrRow, BorderLayout.NORTH);

        JPanel btnGrid = new JPanel(new GridLayout(2, 3, 12, 12));
        btnGrid.setOpaque(false);
        btnGrid.add(quickActionBtn("👥", "Add Librarian",      "Manage accounts",  CLR_ACCENT,  () -> { cardLayout.show(contentPanel,"LIBRARIANS"); activateNav("LIBRARIANS"); }));
        btnGrid.add(quickActionBtn("📊", "View Reports",       "Analytics & logs", CLR_ACCENT2, () -> { cardLayout.show(contentPanel,"REPORTS");    activateNav("REPORTS"); }));
        btnGrid.add(quickActionBtn("⚙",  "System Settings",   "Config & fines",   CLR_ACCENT3, () -> { cardLayout.show(contentPanel,"SETTINGS");   activateNav("SETTINGS"); }));
        btnGrid.add(quickActionBtn("✍",  "Manage Authors",    "Catalog entries",  CLR_ACCENT,  () -> { cardLayout.show(contentPanel,"AUTHORS");    activateNav("AUTHORS"); }));
        btnGrid.add(quickActionBtn("🏷",  "Manage Categories", "Book tagging",     CLR_ACCENT2, () -> { cardLayout.show(contentPanel,"CATEGORIES");activateNav("CATEGORIES"); }));
        btnGrid.add(quickActionBtn("🏢",  "Manage Publishers", "Publisher records",CLR_ACCENT3, () -> { cardLayout.show(contentPanel,"PUBLISHERS");activateNav("PUBLISHERS"); }));
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

    private JPanel dashCard(String emoji, String title, JLabel valLbl, Color accent, String subtitle, String hint) {
        JPanel card = new JPanel(new BorderLayout(0, 10)) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(CLR_CARD);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
                // top accent bar
                g2.setColor(accent);
                g2.fillRoundRect(0, 0, getWidth(), 3, 3, 3);
                // border
                g2.setColor(CLR_BORDER);
                g2.setStroke(new BasicStroke(1f));
                g2.drawRoundRect(0, 0, getWidth()-1, getHeight()-1, 12, 12);
                g2.dispose();
            }
        };
        card.setOpaque(false);
        card.setBorder(new EmptyBorder(18, 20, 18, 20));

        JPanel top = new JPanel(new BorderLayout());
        top.setOpaque(false);

        // icon badge
        JLabel iconBadge = new JLabel(emoji, SwingConstants.CENTER) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(accent.getRed(), accent.getGreen(), accent.getBlue(), 30));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        iconBadge.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 14));
        iconBadge.setPreferredSize(new Dimension(30, 30));

        JLabel labelLbl = new JLabel(title.toUpperCase());
        labelLbl.setFont(FONT_TINY);
        labelLbl.setForeground(CLR_MUTED);

        top.add(labelLbl,  BorderLayout.WEST);
        top.add(iconBadge, BorderLayout.EAST);

        valLbl.setFont(FONT_STAT);
        valLbl.setForeground(CLR_TEXT);

        JLabel hintLbl = new JLabel(hint);
        hintLbl.setFont(new Font("DM Sans", Font.PLAIN, 10));
        hintLbl.setForeground(CLR_MUTED);

        card.add(top,     BorderLayout.NORTH);
        card.add(valLbl,  BorderLayout.CENTER);
        card.add(hintLbl, BorderLayout.SOUTH);
        return card;
    }

    private JButton quickActionBtn(String emoji, String text, String sub, Color accent, Runnable action) {
        JButton btn = new JButton() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getBackground());
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                // border
                g2.setColor(CLR_BORDER);
                g2.setStroke(new BasicStroke(1f));
                g2.drawRoundRect(0, 0, getWidth()-1, getHeight()-1, 10, 10);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btn.setLayout(new FlowLayout(FlowLayout.LEFT, 14, 0));
        btn.setOpaque(false);
        btn.setBackground(CLR_CARD2);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setContentAreaFilled(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));

        // icon badge
        JLabel iconLbl = new JLabel(emoji, SwingConstants.CENTER) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(accent.getRed(), accent.getGreen(), accent.getBlue(), 26));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 9, 9);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        iconLbl.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 16));
        iconLbl.setPreferredSize(new Dimension(36, 36));

        JPanel stack = new JPanel();
        stack.setOpaque(false);
        stack.setLayout(new BoxLayout(stack, BoxLayout.Y_AXIS));
        JLabel mainLbl = new JLabel(text);
        mainLbl.setFont(new Font("DM Sans", Font.BOLD, 12));
        mainLbl.setForeground(CLR_TEXT);
        JLabel subLbl = new JLabel(sub);
        subLbl.setFont(new Font("DM Sans", Font.PLAIN, 10));
        subLbl.setForeground(CLR_MUTED);
        stack.add(mainLbl);
        stack.add(subLbl);

        btn.add(iconLbl);
        btn.add(stack);

        btn.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) {
                btn.setBackground(new Color(accent.getRed(), accent.getGreen(), accent.getBlue(), 13));
                btn.repaint();
            }
            public void mouseExited(MouseEvent e) {
                btn.setBackground(CLR_CARD2);
                btn.repaint();
            }
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

        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        btnRow.setOpaque(false);
        btnAdd    = accentButton("＋  Add Librarian", CLR_ACCENT,  false);
        btnToggle = accentButton("⇄  Toggle Status",  CLR_ACCENT2, true);
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
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 14, 14);
                g2.setColor(CLR_BORDER);
                g2.setStroke(new BasicStroke(1f));
                g2.drawRoundRect(0, 0, getWidth()-1, getHeight()-1, 14, 14);
                g2.dispose();
            }
        };
        card.setOpaque(false);
        card.setBorder(new EmptyBorder(30, 38, 30, 38));

        GridBagConstraints gc = new GridBagConstraints();
        gc.fill = GridBagConstraints.HORIZONTAL;
        gc.insets = new Insets(8, 0, 8, 0);
        gc.gridwidth = GridBagConstraints.REMAINDER;

        JLabel hdr = new JLabel("Borrowing & Fine Configuration");
        hdr.setFont(FONT_HEADER);
        hdr.setForeground(CLR_TEXT);
        card.add(hdr, gc);

        JSeparator sep = new JSeparator();
        sep.setForeground(CLR_BORDER);
        card.add(sep, gc);

        gc.insets = new Insets(10, 0, 10, 0);
        tfBorrowDays = addSettingRow(card, gc, "📅  Max Borrow Days",      "days");
        tfFineRate   = addSettingRow(card, gc, "💰  Fine Rate per Day",    "₱ per day");
        tfMaxBorrow  = addSettingRow(card, gc, "📚  Max Books per Member", "books");

        gc.insets = new Insets(22, 0, 0, 0);
        btnSave = accentButton("💾  Save Settings", CLR_ACCENT, false);
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
        lbl.setFont(new Font("DM Sans", Font.BOLD, 13));
        lbl.setForeground(CLR_TEXT);
        panel.add(lbl, gc);

        gc.weightx = 0.55; gc.gridwidth = GridBagConstraints.REMAINDER;
        JTextField tf = new JTextField(14);
        tf.setFont(new Font("DM Mono", Font.PLAIN, 13));
        tf.setForeground(CLR_TEXT);
        tf.setBackground(CLR_CARD2);
        tf.setCaretColor(CLR_ACCENT);
        tf.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(CLR_BORDER2, 1, true),
            new EmptyBorder(9, 14, 9, 14)
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
        hdr.add(Box.createVerticalStrut(8));
        JSeparator sep = new JSeparator();
        sep.setForeground(CLR_BORDER);
        sep.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));
        hdr.add(sep);
        return hdr;
    }

    /** @param outline true = outlined style, false = filled */
    private JButton accentButton(String text, Color accent, boolean outline) {
        JButton btn = new JButton(text) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                if (outline) {
                    g2.setColor(new Color(accent.getRed(), accent.getGreen(), accent.getBlue(), 20));
                    g2.fillRoundRect(0, 0, getWidth(), getHeight(), 9, 9);
                    g2.setColor(new Color(accent.getRed(), accent.getGreen(), accent.getBlue(), 80));
                    g2.setStroke(new BasicStroke(1f));
                    g2.drawRoundRect(0, 0, getWidth()-1, getHeight()-1, 9, 9);
                } else {
                    g2.setColor(isEnabled() ? getBackground() : CLR_CARD);
                    g2.fillRoundRect(0, 0, getWidth(), getHeight(), 9, 9);
                }
                super.paintComponent(g2);
                g2.dispose();
            }
        };
        btn.setOpaque(false);
        btn.setFont(new Font("DM Sans", Font.BOLD, 12));
        btn.setForeground(outline ? accent : Color.WHITE);
        btn.setBackground(outline ? CLR_CARD2 : accent);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setContentAreaFilled(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setBorder(new EmptyBorder(9, 18, 9, 18));
        Color darker = accent.darker();
        btn.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) {
                if (!btn.isEnabled()) return;
                btn.setBackground(outline ? new Color(accent.getRed(), accent.getGreen(), accent.getBlue(), 35) : darker);
            }
            public void mouseExited(MouseEvent e) {
                if (!btn.isEnabled()) return;
                btn.setBackground(outline ? CLR_CARD2 : accent);
            }
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
        sp.getVerticalScrollBar().setPreferredSize(new Dimension(4, 0));
        return sp;
    }

    private static void styleTable(JTable table) {
        table.setFont(new Font("DM Mono", Font.PLAIN, 12));
        table.setForeground(CLR_TEXT);
        table.setBackground(CLR_CARD);
        table.setRowHeight(36);
        table.setSelectionBackground(new Color(74, 222, 128, 30));
        table.setSelectionForeground(CLR_ACCENT);
        table.setGridColor(new Color(74, 222, 128, 13));
        table.setShowVerticalLines(false);
        table.setIntercellSpacing(new Dimension(0, 1));

        JTableHeader h = table.getTableHeader();
        h.setFont(new Font("DM Sans", Font.BOLD, 10));
        h.setBackground(new Color(0x080e0a));
        h.setForeground(CLR_ACCENT);
        h.setReorderingAllowed(false);
        h.setPreferredSize(new Dimension(0, 38));
        h.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, CLR_BORDER2));

        table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override public Component getTableCellRendererComponent(
                    JTable t, Object val, boolean sel, boolean foc, int row, int col) {
                super.getTableCellRendererComponent(t, val, sel, foc, row, col);
                setFont(new Font("DM Mono", Font.PLAIN, 12));
                setBorder(new EmptyBorder(0, 14, 0, 14));
                if (sel) {
                    setBackground(new Color(74, 222, 128, 28));
                    setForeground(CLR_ACCENT);
                } else {
                    setBackground(row % 2 == 0 ? CLR_CARD : CLR_CARD2);
                    setForeground(CLR_TEXT);
                }
                if (val instanceof String s) {
                    if (s.equals("Active")) {
                        setForeground(CLR_ACCENT);
                        setText("● " + s);
                    } else if (s.equals("Inactive")) {
                        setForeground(CLR_ACCENT4);
                        setText("● " + s);
                    }
                }
                return this;
            }
        });
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
                    lblBooks   .setText(String.valueOf(s[0]));
                    lblMembers .setText(String.valueOf(s[1]));
                    lblBorrowed.setText(String.valueOf(s[2]));
                    lblOverdue .setText(String.valueOf(s[3]));
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
        dlg.setBorder(new EmptyBorder(12, 12, 12, 12));
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
                        if (get()) { JOptionPane.showMessageDialog(AdminDashboard.this, "Librarian added successfully."); loadLibrarians(); }
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
        if (row < 0) { JOptionPane.showMessageDialog(this, "Please select a librarian first."); return; }
        int     id       = (int)    libTableModel.getValueAt(row, 0);
        String  status   = (String) libTableModel.getValueAt(row, 5);
        boolean isActive = status.contains("Active") && !status.contains("Inactive");

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
                    if (r[0] && r[1] && r[2])
                        JOptionPane.showMessageDialog(AdminDashboard.this, "Settings saved successfully.");
                    else
                        JOptionPane.showMessageDialog(AdminDashboard.this, "Some settings failed to save.", "Warning", JOptionPane.WARNING_MESSAGE);
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
        tf.setFont(new Font("DM Mono", Font.PLAIN, 12));
        tf.setForeground(CLR_TEXT);
        tf.setBackground(CLR_CARD2);
        tf.setCaretColor(CLR_ACCENT);
        tf.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(CLR_BORDER2, 1),
            new EmptyBorder(7, 12, 7, 12)
        ));
    }

    private static JLabel styledLabel(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(new Font("DM Sans", Font.BOLD, 12));
        lbl.setForeground(CLR_TEXT);
        return lbl;
    }
}