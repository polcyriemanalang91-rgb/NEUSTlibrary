package neustlibrarysystem.view.librarian;

import neustlibrarysystem.model.Librarian;
import neustlibrarysystem.view.common.LoginFrame;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.List;

public class LibrarianDashboard extends JFrame {

    public static final Color CLR_PRIMARY     = new Color(0x183b06);
    public static final Color CLR_ACCENT      = new Color(0x9ab55d);
    public static final Color CLR_BG          = new Color(0xeaf7d7);
    public static final Color CLR_SIDEBAR     = new Color(0x1a1a2e);
    public static final Color CLR_SIDEBAR_SEL = new Color(0x2a2a3e);
    public static final Color CLR_SIDEBAR_HOV = new Color(0x25253a);
    public static final Color CLR_WHITE       = Color.WHITE;

    public static final Font FONT_NAV    = new Font("Segoe UI", Font.BOLD,  13);
    public static final Font FONT_HEADER = new Font("Segoe UI", Font.BOLD,  18);
    public static final Font FONT_BODY   = new Font("Segoe UI", Font.PLAIN, 13);
    public static final Font FONT_LABEL  = new Font("Segoe UI", Font.BOLD,  12);

    private static final int SIDEBAR_EXPANDED  = 230;
    private static final int SIDEBAR_COLLAPSED = 66;

    private final Librarian librarian;
    private CardLayout cardLayout;
    private JPanel     contentPanel;
    private JPanel     sidebar;
    private JPanel     navContainer;
    private JButton    toggleBtn;
    private boolean    sidebarExpanded = true;
    private Timer      sidebarTimer;
    private int        sidebarTargetWidth;

    private final List<NavEntry> navEntries = new ArrayList<>();
    private JButton activeNavBtn = null;

    private ManageBooksPanel        booksPanel;
    private ProcessBorrowPanel      borrowPanel;
    private ProcessReturnPanel      returnPanel;
    private ManageReservationsPanel reservationsPanel;
    private ManageMembersPanel      membersPanel;

    // ── Icon types ────────────────────────────────────────────────────────────
    public enum IconType { BOOKS, BORROW, RETURN, RESERVATIONS, MEMBERS, LOGOUT, USER, TOGGLE_CLOSE, TOGGLE_OPEN }

    // ── Custom vector icon factory ────────────────────────────────────────────
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
                    case BORROW       -> drawBorrow(g2, s);
                    case RETURN       -> drawReturn(g2, s);
                    case RESERVATIONS -> drawReservations(g2, s);
                    case MEMBERS      -> drawMembers(g2, s);
                    case LOGOUT       -> drawLogout(g2, s);
                    case USER         -> drawUser(g2, s);
                    case TOGGLE_CLOSE -> drawToggleClose(g2, s);
                    case TOGGLE_OPEN  -> drawToggleOpen(g2, s);
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
    private static void drawBorrow(Graphics2D g, float s) {
        g.drawRoundRect(p(2,s),p(3,s),p(11,s),p(14,s),p(2,s),p(2,s));
        g.drawLine(p(5,s),p(3,s),p(5,s),p(17,s));
        g.drawLine(p(12,s),p(10,s),p(18,s),p(10,s));
        g.fillPolygon(new int[]{p(14,s),p(18,s),p(14,s)}, new int[]{p(7,s),p(10,s),p(13,s)}, 3);
    }
    private static void drawReturn(Graphics2D g, float s) {
        g.drawRoundRect(p(7,s),p(3,s),p(11,s),p(14,s),p(2,s),p(2,s));
        g.drawLine(p(10,s),p(3,s),p(10,s),p(17,s));
        g.drawLine(p(2,s),p(10,s),p(8,s),p(10,s));
        g.fillPolygon(new int[]{p(6,s),p(2,s),p(6,s)}, new int[]{p(7,s),p(10,s),p(13,s)}, 3);
    }
    private static void drawReservations(Graphics2D g, float s) {
        g.fillRoundRect(p(5,s),p(2,s),p(10,s),p(16,s),p(2,s),p(2,s));
        Color c = g.getColor();
        // Cutout notch at bottom
        g.setColor(new Color(0x1a,0x1a,0x2e));
        g.fillPolygon(new int[]{p(5,s),p(10,s),p(15,s)}, new int[]{p(18,s),p(13,s),p(18,s)}, 3);
        g.setColor(new Color(c.getRed(),c.getGreen(),c.getBlue(),80));
        g.drawLine(p(8,s),p(6,s),p(12,s),p(6,s));
        g.drawLine(p(8,s),p(9,s),p(12,s),p(9,s));
        g.setColor(c);
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
    private static void drawLogout(Graphics2D g, float s) {
        g.drawRoundRect(p(2,s),p(2,s),p(10,s),p(16,s),p(2,s),p(2,s));
        g.fillOval(p(3,s),p(9,s),p(2,s),p(2,s));
        g.drawLine(p(9,s),p(10,s),p(18,s),p(10,s));
        g.fillPolygon(new int[]{p(14,s),p(18,s),p(14,s)}, new int[]{p(6,s),p(10,s),p(14,s)}, 3);
    }
    private static void drawUser(Graphics2D g, float s) {
        g.fillOval(p(6,s),p(2,s),p(8,s),p(8,s));
        g.fillArc(p(2,s),p(11,s),p(16,s),p(9,s),0,180);
    }
    private static void drawToggleClose(Graphics2D g, float s) {
        g.drawLine(p(12,s),p(4,s),p(7,s),p(10,s)); g.drawLine(p(7,s),p(10,s),p(12,s),p(16,s));
        g.drawLine(p(16,s),p(4,s),p(11,s),p(10,s)); g.drawLine(p(11,s),p(10,s),p(16,s),p(16,s));
    }
    private static void drawToggleOpen(Graphics2D g, float s) {
        g.drawLine(p(8,s),p(4,s),p(13,s),p(10,s)); g.drawLine(p(13,s),p(10,s),p(8,s),p(16,s));
        g.drawLine(p(4,s),p(4,s),p(9,s),p(10,s)); g.drawLine(p(9,s),p(10,s),p(4,s),p(16,s));
    }

    // ── Nav entry ─────────────────────────────────────────────────────────────
    private static class NavEntry {
        JButton btn; IconType iconType; String text, card;
        NavEntry(JButton b, IconType i, String t, String c) { btn=b; iconType=i; text=t; card=c; }
    }

    // ── Constructor ───────────────────────────────────────────────────────────
    public LibrarianDashboard(Librarian librarian) {
        this.librarian = librarian;
        setTitle("NEUST Library — Librarian Portal");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1150, 720);
        setLocationRelativeTo(null);
        buildUI();
    }

    private void buildUI() {
        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(CLR_BG);
        root.add(buildSidebar(), BorderLayout.WEST);
        root.add(buildTopBar(),  BorderLayout.NORTH);

        cardLayout   = new CardLayout();
        contentPanel = new JPanel(cardLayout);
        contentPanel.setBackground(CLR_BG);
        contentPanel.setBorder(new EmptyBorder(20,20,20,20));

        JPanel loadingPanel = new JPanel(new BorderLayout());
        loadingPanel.setBackground(CLR_BG);
        JLabel loadingLbl = new JLabel("Loading...", SwingConstants.CENTER);
        loadingLbl.setFont(new Font("Segoe UI", Font.ITALIC, 14));
        loadingLbl.setForeground(CLR_ACCENT);
        loadingPanel.add(loadingLbl, BorderLayout.CENTER);
        contentPanel.add(loadingPanel, "loading");

        root.add(contentPanel, BorderLayout.CENTER);
        setContentPane(root);
        cardLayout.show(contentPanel, "loading");
        loadPanelAsync("books");
    }

    // ── Sidebar ───────────────────────────────────────────────────────────────
    private JPanel buildSidebar() {
        sidebar = new JPanel(new BorderLayout());
        sidebar.setBackground(CLR_SIDEBAR);
        sidebar.setPreferredSize(new Dimension(SIDEBAR_EXPANDED, 0));

        // Top: logo + toggle
        JPanel topArea = new JPanel(new BorderLayout());
        topArea.setBackground(new Color(0x12122a));
        topArea.setBorder(new EmptyBorder(20,0,16,0));

        JPanel logoPanel = new JPanel();
        logoPanel.setBackground(new Color(0x12122a));
        logoPanel.setLayout(new BoxLayout(logoPanel, BoxLayout.Y_AXIS));
        JLabel logoIcon = new JLabel(makeIcon(IconType.BOOKS, CLR_ACCENT, 36));
        logoIcon.setAlignmentX(Component.CENTER_ALIGNMENT);
        JLabel sysName = new JLabel("NEUST Library");
        sysName.setFont(new Font("Segoe UI", Font.BOLD, 14));
        sysName.setForeground(Color.WHITE);
        sysName.setAlignmentX(Component.CENTER_ALIGNMENT);
        JLabel roleLabel = new JLabel("Librarian Portal");
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
        toggleBtn.setPreferredSize(new Dimension(38,38));
        toggleBtn.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) { toggleBtn.setContentAreaFilled(true); toggleBtn.setBackground(new Color(0x22224a)); }
            public void mouseExited (MouseEvent e) { toggleBtn.setContentAreaFilled(false); }
        });
        toggleBtn.addActionListener(e -> toggleSidebar());
        topArea.add(toggleBtn, BorderLayout.EAST);
        sidebar.add(topArea, BorderLayout.NORTH);

        // Nav buttons
        navContainer = new JPanel();
        navContainer.setBackground(CLR_SIDEBAR);
        navContainer.setLayout(new BoxLayout(navContainer, BoxLayout.Y_AXIS));
        navContainer.setBorder(new EmptyBorder(20,0,16,0));
        addNavBtn(IconType.BOOKS,        "Manage Books",   "books");
        addNavBtn(IconType.BORROW,       "Process Borrow", "borrow");
        addNavBtn(IconType.RETURN,       "Process Return", "return");
        addNavBtn(IconType.RESERVATIONS, "Reservations",   "reservations");
        addNavBtn(IconType.MEMBERS,      "Members",        "members");
        sidebar.add(navContainer, BorderLayout.CENTER);

        // Bottom: user + logout
        JPanel bottomArea = new JPanel();
        bottomArea.setBackground(CLR_SIDEBAR);
        bottomArea.setLayout(new BoxLayout(bottomArea, BoxLayout.Y_AXIS));
        bottomArea.setBorder(new EmptyBorder(0,0,12,0));
        JSeparator sep = new JSeparator();
        sep.setForeground(new Color(0x2e2e4a));
        sep.setMaximumSize(new Dimension(Integer.MAX_VALUE,1));
        bottomArea.add(sep);
        bottomArea.add(Box.createVerticalStrut(10));

        JButton userRow = new JButton();
        userRow.setIcon(makeIcon(IconType.USER, CLR_ACCENT, 18));
        userRow.setText("  " + librarian.getFullName());
        userRow.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        userRow.setForeground(new Color(0xccccee));
        userRow.setBackground(CLR_SIDEBAR);
        userRow.setBorderPainted(false); userRow.setFocusPainted(false); userRow.setContentAreaFilled(false);
        userRow.setHorizontalAlignment(SwingConstants.LEFT);
        userRow.setMaximumSize(new Dimension(Integer.MAX_VALUE,36));
        userRow.setAlignmentX(Component.CENTER_ALIGNMENT);
        userRow.setBorder(new EmptyBorder(4,20,4,20));
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
        logoutBtn.setMaximumSize(new Dimension(Integer.MAX_VALUE,40));
        logoutBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        logoutBtn.setBorder(new EmptyBorder(8,20,8,20));
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
    private void addNavBtn(IconType iconType, String text, String card) {
        Color iconNormal = new Color(0xaaaacc);
        JButton btn = new JButton();
        btn.setIcon(makeIcon(iconType, iconNormal, 20));
        btn.setText("  " + text);
        btn.setFont(FONT_NAV);
        btn.setForeground(new Color(0xccccee));
        btn.setBackground(CLR_SIDEBAR);
        btn.setBorderPainted(false); btn.setContentAreaFilled(false); btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setHorizontalAlignment(SwingConstants.LEFT);
        btn.setBorder(new EmptyBorder(10,20,10,20));
        btn.setMaximumSize(new Dimension(Integer.MAX_VALUE,50));
        btn.setAlignmentX(Component.CENTER_ALIGNMENT);
        btn.setIconTextGap(10);

        navEntries.add(new NavEntry(btn, iconType, text, card));

        btn.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) {
                if (btn!=activeNavBtn) { btn.setContentAreaFilled(true); btn.setBackground(CLR_SIDEBAR_HOV); }
            }
            public void mouseExited(MouseEvent e) {
                if (btn!=activeNavBtn) { btn.setContentAreaFilled(false); btn.setBackground(CLR_SIDEBAR); }
            }
        });
        btn.addActionListener(e -> {
            if (activeNavBtn != null) {
                NavEntry prev = navEntries.stream().filter(ne->ne.btn==activeNavBtn).findFirst().orElse(null);
                if (prev!=null) activeNavBtn.setIcon(makeIcon(prev.iconType, iconNormal, 20));
                activeNavBtn.setContentAreaFilled(false);
                activeNavBtn.setBackground(CLR_SIDEBAR);
                activeNavBtn.setForeground(new Color(0xccccee));
            }
            btn.setIcon(makeIcon(iconType, CLR_ACCENT, 20));
            btn.setContentAreaFilled(true);
            btn.setBackground(CLR_SIDEBAR_SEL);
            btn.setForeground(CLR_ACCENT);
            activeNavBtn = btn;
            if (!isPanelLoaded(card)) {
                loadPanelAsync(card);
            } else {
                cardLayout.show(contentPanel, card);
                if ("reservations".equals(card) && reservationsPanel!=null) reservationsPanel.refresh();
                if ("members".equals(card)      && membersPanel!=null)      membersPanel.refresh();
                if ("return".equals(card)       && returnPanel!=null)       returnPanel.refresh();
            }
        });
        navContainer.add(btn);
        navContainer.add(Box.createVerticalStrut(2));
    }

    // ── Toggle animation ──────────────────────────────────────────────────────
    private void toggleSidebar() {
        sidebarExpanded    = !sidebarExpanded;
        sidebarTargetWidth = sidebarExpanded ? SIDEBAR_EXPANDED : SIDEBAR_COLLAPSED;
        toggleBtn.setIcon(makeIcon(sidebarExpanded ? IconType.TOGGLE_CLOSE : IconType.TOGGLE_OPEN, CLR_ACCENT, 20));
        for (NavEntry ne : navEntries) {
            ne.btn.setText(sidebarExpanded ? "  " + ne.text : "");
            ne.btn.setHorizontalAlignment(sidebarExpanded ? SwingConstants.LEFT : SwingConstants.CENTER);
            ne.btn.setBorder(new EmptyBorder(10, sidebarExpanded?20:0, 10, sidebarExpanded?20:0));
        }
        // Update bottom buttons
        JPanel bottomArea = (JPanel) sidebar.getComponent(2);
        for (Component c : bottomArea.getComponents()) {
            if (!(c instanceof JButton jb)) continue;
            if ("userRow".equals(jb.getName())) {
                jb.setText(sidebarExpanded ? "  " + librarian.getFullName() : "");
                jb.setHorizontalAlignment(sidebarExpanded ? SwingConstants.LEFT : SwingConstants.CENTER);
                jb.setBorder(new EmptyBorder(4, sidebarExpanded?20:0, 4, sidebarExpanded?20:0));
            }
            if ("logoutBtn".equals(jb.getName())) {
                jb.setText(sidebarExpanded ? "  Logout" : "");
                jb.setHorizontalAlignment(sidebarExpanded ? SwingConstants.LEFT : SwingConstants.CENTER);
                jb.setBorder(new EmptyBorder(8, sidebarExpanded?20:0, 8, sidebarExpanded?20:0));
            }
        }
        if (sidebarTimer!=null && sidebarTimer.isRunning()) sidebarTimer.stop();
        sidebarTimer = new Timer(12, null);
        sidebarTimer.addActionListener(e -> {
            int w = sidebar.getPreferredSize().width;
            int step = (sidebarTargetWidth>w) ? 14 : -14;
            w += step;
            boolean done = step>0 ? w>=sidebarTargetWidth : w<=sidebarTargetWidth;
            if (done) { w=sidebarTargetWidth; sidebarTimer.stop(); }
            sidebar.setPreferredSize(new Dimension(w,0));
            sidebar.revalidate();
            getContentPane().revalidate();
            getContentPane().repaint();
        });
        sidebarTimer.start();
    }

    // ── Lazy loader ───────────────────────────────────────────────────────────
    private void loadPanelAsync(String card) {
        new SwingWorker<Void,Void>() {
            protected Void doInBackground() {
                switch (card) {
                    case "books"        -> { if (booksPanel==null)        booksPanel        = new ManageBooksPanel(librarian); }
                    case "borrow"       -> { if (borrowPanel==null)       borrowPanel       = new ProcessBorrowPanel(librarian); }
                    case "return"       -> { if (returnPanel==null)       returnPanel       = new ProcessReturnPanel(librarian); }
                    case "reservations" -> { if (reservationsPanel==null) reservationsPanel = new ManageReservationsPanel(librarian); }
                    case "members"      -> { if (membersPanel==null)      membersPanel      = new ManageMembersPanel(); }
                }
                return null;
            }
            protected void done() {
                switch (card) {
                    case "books"        -> contentPanel.add(booksPanel,        "books");
                    case "borrow"       -> contentPanel.add(borrowPanel,       "borrow");
                    case "return"       -> contentPanel.add(returnPanel,       "return");
                    case "reservations" -> contentPanel.add(reservationsPanel, "reservations");
                    case "members"      -> contentPanel.add(membersPanel,      "members");
                }
                cardLayout.show(contentPanel, card);
                contentPanel.revalidate(); contentPanel.repaint();
            }
        }.execute();
    }

    private boolean isPanelLoaded(String card) {
        return switch (card) {
            case "books"        -> booksPanel        != null;
            case "borrow"       -> borrowPanel       != null;
            case "return"       -> returnPanel       != null;
            case "reservations" -> reservationsPanel != null;
            case "members"      -> membersPanel      != null;
            default             -> false;
        };
    }

    // ── Top bar ───────────────────────────────────────────────────────────────
    private JPanel buildTopBar() {
        JPanel bar = new JPanel(new BorderLayout());
        bar.setBackground(new Color(0x12122a));
        bar.setBorder(new EmptyBorder(12,24,12,24));
        JLabel title = new JLabel("Library Management System — Librarian Portal");
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

    private void logout() {
        int c = JOptionPane.showConfirmDialog(this, "Logout from the system?", "Confirm Logout", JOptionPane.YES_NO_OPTION);
        if (c==JOptionPane.YES_OPTION) { new LoginFrame().setVisible(true); dispose(); }
    }

    public static void styleTable(JTable table) {
        table.setFont(FONT_BODY); table.setRowHeight(30);
        table.setSelectionBackground(new Color(0x9ab55d)); table.setSelectionForeground(Color.WHITE);
        table.setGridColor(new Color(0xc5e0a0)); table.setShowVerticalLines(false); table.setBackground(Color.WHITE);
        javax.swing.table.JTableHeader h = table.getTableHeader();
        h.setFont(FONT_LABEL); h.setBackground(new Color(0x1a1a2e)); h.setForeground(Color.WHITE);
        h.setReorderingAllowed(false); h.setPreferredSize(new Dimension(0,36));
    }
    public static JButton primaryBtn(String text) {
        JButton b = new JButton(text); b.setFont(FONT_BODY); b.setBackground(new Color(0x183b06));
        b.setForeground(Color.WHITE); b.setFocusPainted(false); b.setBorderPainted(false);
        b.setCursor(new Cursor(Cursor.HAND_CURSOR)); b.setBorder(new EmptyBorder(8,18,8,18)); return b;
    }
    public static JButton accentBtn(String text) {
        JButton b = new JButton(text); b.setFont(FONT_BODY); b.setBackground(new Color(0x9ab55d));
        b.setForeground(Color.WHITE); b.setFocusPainted(false); b.setBorderPainted(false);
        b.setCursor(new Cursor(Cursor.HAND_CURSOR)); b.setBorder(new EmptyBorder(8,18,8,18)); return b;
    }
    public static JButton dangerBtn(String text) {
        JButton b = new JButton(text); b.setFont(FONT_BODY); b.setBackground(new Color(0xC0392B));
        b.setForeground(Color.WHITE); b.setFocusPainted(false); b.setBorderPainted(false);
        b.setCursor(new Cursor(Cursor.HAND_CURSOR)); b.setBorder(new EmptyBorder(8,18,8,18)); return b;
    }
}