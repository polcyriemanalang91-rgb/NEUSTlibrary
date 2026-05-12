package neustlibrarysystem.view.librarian;

import neustlibrarysystem.dao.BookDAO;
import neustlibrarysystem.dao.BorrowDAO;
import neustlibrarysystem.dao.MemberDAO;
import neustlibrarysystem.model.BorrowedRecord;
import neustlibrarysystem.model.Librarian;
import neustlibrarysystem.view.common.LoginFrame;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.RoundRectangle2D;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class LibrarianDashboard extends JFrame {

    // ── Design Tokens ──────────────────────────────────────────────────────────
    public static final Color CLR_PRIMARY      = new Color(0x183b06);
    public static final Color CLR_ACCENT       = new Color(0x4ade80);
    public static final Color CLR_ACCENT2      = new Color(0x22d3ee);
    public static final Color CLR_ACCENT3      = new Color(0xfbbf24);
    public static final Color CLR_ACCENT4      = new Color(0xf87171);
    public static final Color CLR_BG           = new Color(0x0d1b12);
    public static final Color CLR_CARD         = new Color(0x132218);
    public static final Color CLR_CARD2        = new Color(0x1a2e1f);
    public static final Color CLR_SIDEBAR      = new Color(0x091510);
    public static final Color CLR_SIDEBAR_SEL  = new Color(0x1e3a26);
    public static final Color CLR_SIDEBAR_HOV  = new Color(0x162b1c);
    public static final Color CLR_TOP_BAR      = new Color(0x091510);
    public static final Color CLR_TEXT         = new Color(0xe2f5e8);
    public static final Color CLR_MUTED        = new Color(0x6b9e7a);
    public static final Color CLR_BORDER       = new Color(0x1a3022);
    public static final Color CLR_WHITE        = Color.WHITE;

    private static final Color CLR_DARK_BG     = CLR_BG;
    private static final Color CLR_CARD_BG     = CLR_CARD;
    private static final Color CLR_CARD_GREEN  = new Color(0x4ade80);
    private static final Color CLR_CARD_CYAN   = new Color(0x22d3ee);
    private static final Color CLR_CARD_YELLOW = new Color(0xfbbf24);
    private static final Color CLR_CARD_RED    = new Color(0xf87171);

    public static final Font FONT_NAV    = new Font("Segoe UI", Font.PLAIN,  13);
    public static final Font FONT_HEADER = new Font("Segoe UI", Font.BOLD,   16);
    public static final Font FONT_BODY   = new Font("Segoe UI", Font.PLAIN,  13);
    public static final Font FONT_LABEL  = new Font("Segoe UI", Font.BOLD,   11);
    public static final Font FONT_SMALL  = new Font("Segoe UI", Font.PLAIN,  11);
    private static final Font FONT_STAT  = new Font("Segoe UI", Font.BOLD,   34);
    private static final Font FONT_TITLE = new Font("Segoe UI", Font.BOLD,   20);
    private static final Font FONT_NAV_BOLD = new Font("Segoe UI", Font.BOLD, 13);

    private static final int SIDEBAR_EXPANDED  = 220;
    private static final int SIDEBAR_COLLAPSED = 62;

    private final Librarian  librarian;
    private final BookDAO    bookDAO   = new BookDAO();
    private final BorrowDAO  borrowDAO = new BorrowDAO();
    private final MemberDAO  memberDAO = new MemberDAO();

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

    private JLabel statBooksVal, statMembersVal, statBorrowedVal, statOverdueVal;

    private ManageBooksPanel           booksPanel;
    private ProcessBorrowPanel         borrowPanel;
    private ProcessReturnPanel         returnPanel;
    private ManageReservationsPanel    reservationsPanel;
    private ManageMembersPanel         membersPanel;
    private AcceptBorrowRequestPanel   acceptRequestPanel;

    // ── Icon types ────────────────────────────────────────────────────────────
    public enum IconType {
        BOOKS, BORROW, RETURN, RESERVATIONS, MEMBERS,
        LOGOUT, USER, TOGGLE_CLOSE, TOGGLE_OPEN,
        DASHBOARD, REFRESH, OVERDUE, ADD_USER, REPORTS, SETTINGS,
        APPROVE
    }

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
                    case DASHBOARD    -> drawDashboard(g2, s);
                    case REFRESH      -> drawRefresh(g2, s);
                    case OVERDUE      -> drawOverdue(g2, s);
                    case ADD_USER     -> drawAddUser(g2, s);
                    case REPORTS      -> drawReports(g2, s);
                    case SETTINGS     -> drawSettings(g2, s);
                    case APPROVE      -> drawApprove(g2, s);
                }
                g2.dispose();
            }
        };
    }

    private static int p(float v, float s) { return Math.round(v * s); }
    private static void drawBooks(Graphics2D g, float s) {
        g.fillRoundRect(p(2,s),p(14,s),p(7,s),p(4,s),p(1,s),p(1,s));
        g.fillRoundRect(p(4,s),p(9,s),p(8,s),p(5,s),p(1,s),p(1,s));
        g.fillRoundRect(p(6,s),p(4,s),p(10,s),p(5,s),p(1,s),p(1,s));
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
        g.setColor(new Color(0x09,0x15,0x10));
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
    private static void drawDashboard(Graphics2D g, float s) {
        g.fillRoundRect(p(2,s),p(2,s),p(7,s),p(7,s),p(2,s),p(2,s));
        g.fillRoundRect(p(11,s),p(2,s),p(7,s),p(7,s),p(2,s),p(2,s));
        g.fillRoundRect(p(2,s),p(11,s),p(7,s),p(7,s),p(2,s),p(2,s));
        g.fillRoundRect(p(11,s),p(11,s),p(7,s),p(7,s),p(2,s),p(2,s));
    }
    private static void drawRefresh(Graphics2D g, float s) {
        g.drawArc(p(3,s),p(3,s),p(14,s),p(14,s),30,270);
        g.fillPolygon(new int[]{p(14,s),p(18,s),p(16,s)}, new int[]{p(4,s),p(7,s),p(10,s)}, 3);
    }
    private static void drawOverdue(Graphics2D g, float s) {
        g.fillPolygon(new int[]{p(10,s),p(2,s),p(18,s)}, new int[]{p(2,s),p(18,s),p(18,s)}, 3);
        Color c = g.getColor();
        g.setColor(new Color(0x09,0x15,0x10));
        g.fillRect(p(9,s),p(8,s),p(2,s),p(5,s));
        g.fillRect(p(9,s),p(14,s),p(2,s),p(2,s));
        g.setColor(c);
    }
    private static void drawAddUser(Graphics2D g, float s) {
        g.fillOval(p(4,s),p(2,s),p(8,s),p(8,s));
        g.fillArc(p(0,s),p(11,s),p(14,s),p(8,s),0,180);
        g.drawLine(p(15,s),p(6,s),p(15,s),p(14,s));
        g.drawLine(p(11,s),p(10,s),p(19,s),p(10,s));
    }
    private static void drawReports(Graphics2D g, float s) {
        g.drawRoundRect(p(3,s),p(2,s),p(14,s),p(16,s),p(2,s),p(2,s));
        g.drawLine(p(6,s),p(7,s),p(14,s),p(7,s));
        g.drawLine(p(6,s),p(10,s),p(14,s),p(10,s));
        g.drawLine(p(6,s),p(13,s),p(11,s),p(13,s));
    }
    private static void drawSettings(Graphics2D g, float s) {
        g.drawOval(p(7,s),p(7,s),p(6,s),p(6,s));
        for (int i = 0; i < 8; i++) {
            double a = Math.toRadians(i * 45);
            int x1 = p(10,s) + (int)(Math.cos(a)*5*s);
            int y1 = p(10,s) + (int)(Math.sin(a)*5*s);
            int x2 = p(10,s) + (int)(Math.cos(a)*8*s);
            int y2 = p(10,s) + (int)(Math.sin(a)*8*s);
            g.drawLine(x1,y1,x2,y2);
        }
    }
    private static void drawApprove(Graphics2D g, float s) {
        g.drawOval(p(2,s), p(2,s), p(16,s), p(16,s));
        g.drawLine(p(6,s), p(10,s), p(9,s),  p(13,s));
        g.drawLine(p(9,s), p(13,s), p(14,s), p(7,s));
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
        setMinimumSize(new Dimension(900, 600));
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
        contentPanel.setBorder(new EmptyBorder(24, 28, 24, 28));

        contentPanel.add(buildDashboardPanel(), "dashboard");

        root.add(contentPanel, BorderLayout.CENTER);
        setContentPane(root);

        cardLayout.show(contentPanel, "dashboard");
        SwingUtilities.invokeLater(this::refreshDashboardStats);
        loadPanelAsync("books");
    }

    // ── Sidebar ───────────────────────────────────────────────────────────────
    private JPanel buildSidebar() {
        sidebar = new JPanel(new BorderLayout());
        sidebar.setBackground(CLR_SIDEBAR);
        sidebar.setPreferredSize(new Dimension(SIDEBAR_EXPANDED, 0));
        sidebar.setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, CLR_BORDER));

        // ── Logo area ─────────────────────────────────────────────────────────
        JPanel logoArea = new JPanel();
        logoArea.setBackground(CLR_SIDEBAR);
        logoArea.setLayout(new BoxLayout(logoArea, BoxLayout.Y_AXIS));
        logoArea.setBorder(new EmptyBorder(20, 0, 14, 0));

        // Logo icon with rounded bg
        JPanel logoIconWrap = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                GradientPaint gp = new GradientPaint(0,0, new Color(0x22c55e), getWidth(), getHeight(), new Color(0x16a34a));
                g2.setPaint(gp);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        logoIconWrap.setOpaque(false);
        logoIconWrap.setPreferredSize(new Dimension(36, 36));
        logoIconWrap.setMaximumSize(new Dimension(36, 36));
        logoIconWrap.setLayout(new GridBagLayout());
        logoIconWrap.add(new JLabel(makeIcon(IconType.BOOKS, Color.WHITE, 20)));
        logoIconWrap.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel sysName = new JLabel("NEUST Library");
        sysName.setFont(new Font("Segoe UI", Font.BOLD, 14));
        sysName.setForeground(CLR_TEXT);
        sysName.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Role badge pill
        JLabel roleTag = new JLabel("Librarian Portal") {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(CLR_ACCENT);
                g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.13f));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20);
                g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.35f));
                g2.setStroke(new BasicStroke(1f));
                g2.drawRoundRect(0, 0, getWidth()-1, getHeight()-1, 20, 20);
                g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1f));
                super.paintComponent(g2);
                g2.dispose();
            }
        };
        roleTag.setFont(new Font("Segoe UI", Font.BOLD, 9));
        roleTag.setForeground(CLR_ACCENT);
        roleTag.setHorizontalAlignment(SwingConstants.CENTER);
        roleTag.setBorder(new EmptyBorder(3, 12, 3, 12));
        roleTag.setAlignmentX(Component.CENTER_ALIGNMENT);
        roleTag.setMaximumSize(new Dimension(130, 22));

        // Separator line
        JSeparator topSep = new JSeparator();
        topSep.setForeground(CLR_BORDER);
        topSep.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));

        toggleBtn = new JButton("◀");
        toggleBtn.setFont(new Font("Segoe UI", Font.BOLD, 10));
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

        logoArea.add(logoIconWrap);
        logoArea.add(Box.createVerticalStrut(10));
        logoArea.add(sysName);
        logoArea.add(Box.createVerticalStrut(6));
        logoArea.add(roleTag);
        logoArea.add(Box.createVerticalStrut(14));
        logoArea.add(topSep);
        logoArea.add(Box.createVerticalStrut(8));
        logoArea.add(toggleBtn);
        sidebar.add(logoArea, BorderLayout.NORTH);

        // ── Nav container ─────────────────────────────────────────────────────
        navContainer = new JPanel();
        navContainer.setBackground(CLR_SIDEBAR);
        navContainer.setLayout(new BoxLayout(navContainer, BoxLayout.Y_AXIS));
        navContainer.setBorder(new EmptyBorder(6, 0, 10, 0));

        addSectionLabel("MAIN");
        addNavBtn(IconType.DASHBOARD,    "Dashboard",       "dashboard");
        addSectionLabel("LIBRARY");
        addNavBtn(IconType.APPROVE,      "Accept Requests", "accept-requests");
        addNavBtn(IconType.BOOKS,        "Manage Books",    "books");
        addNavBtn(IconType.BORROW,       "Process Borrow",  "borrow");
        addNavBtn(IconType.RETURN,       "Process Return",  "return");
        addNavBtn(IconType.RESERVATIONS, "Reservations",    "reservations");
        addSectionLabel("ACCOUNT");
        addNavBtn(IconType.MEMBERS,      "Members",         "members");

        JScrollPane navScroll = new JScrollPane(navContainer);
        navScroll.setBorder(null);
        navScroll.setBackground(CLR_SIDEBAR);
        navScroll.getViewport().setBackground(CLR_SIDEBAR);
        navScroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        navScroll.getVerticalScrollBar().setPreferredSize(new Dimension(4, 0));
        sidebar.add(navScroll, BorderLayout.CENTER);

        // ── Bottom user area ──────────────────────────────────────────────────
        JPanel bottomArea = new JPanel();
        bottomArea.setBackground(CLR_SIDEBAR);
        bottomArea.setLayout(new BoxLayout(bottomArea, BoxLayout.Y_AXIS));
        bottomArea.setBorder(new EmptyBorder(8, 0, 10, 0));

        JSeparator sep = new JSeparator();
        sep.setForeground(CLR_BORDER);
        sep.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));
        bottomArea.add(sep);
        bottomArea.add(Box.createVerticalStrut(10));

        // Avatar circle + name row
        JPanel userRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0)) {
            @Override protected void paintComponent(Graphics g) {
                super.paintComponent(g);
            }
        };
        userRow.setBackground(CLR_SIDEBAR);
        userRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 46));

        JPanel avatarCircle = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                GradientPaint gp = new GradientPaint(0,0, new Color(0x22c55e), getWidth(), getHeight(), new Color(0x059669));
                g2.setPaint(gp);
                g2.fillOval(0, 0, getWidth(), getHeight());
                g2.dispose();
                super.paintComponent(g);
            }
        };
        avatarCircle.setOpaque(false);
        avatarCircle.setPreferredSize(new Dimension(30, 30));
        avatarCircle.setMaximumSize(new Dimension(30, 30));
        avatarCircle.setLayout(new GridBagLayout());
        String initials = getInitials(librarian.getFullName());
        JLabel initialsLbl = new JLabel(initials);
        initialsLbl.setFont(new Font("Segoe UI", Font.BOLD, 10));
        initialsLbl.setForeground(Color.WHITE);
        avatarCircle.add(initialsLbl);

        JPanel nameCol = new JPanel();
        nameCol.setOpaque(false);
        nameCol.setLayout(new BoxLayout(nameCol, BoxLayout.Y_AXIS));
        JLabel nameLbl = new JLabel(librarian.getFullName());
        nameLbl.setFont(new Font("Segoe UI", Font.BOLD, 12));
        nameLbl.setForeground(CLR_TEXT);
        JLabel roleLbl = new JLabel("Librarian");
        roleLbl.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        roleLbl.setForeground(CLR_MUTED);
        nameCol.add(nameLbl);
        nameCol.add(roleLbl);

        userRow.add(avatarCircle);
        userRow.add(nameCol);
        userRow.setName("userRow");
        bottomArea.add(userRow);
        bottomArea.add(Box.createVerticalStrut(4));

        JButton logoutBtn = new JButton() {
            @Override protected void paintComponent(Graphics g) {
                if (isContentAreaFilled()) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    g2.setColor(new Color(0xf87171, false));
                    g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.12f));
                    g2.fillRoundRect(6, 2, getWidth()-12, getHeight()-4, 8, 8);
                    g2.dispose();
                }
                super.paintComponent(g);
            }
        };
        logoutBtn.setIcon(makeIcon(IconType.LOGOUT, CLR_ACCENT4, 16));
        logoutBtn.setText("  Logout");
        logoutBtn.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        logoutBtn.setForeground(CLR_ACCENT4);
        logoutBtn.setBackground(new Color(0, 0, 0, 0));
        logoutBtn.setBorderPainted(false); logoutBtn.setFocusPainted(false); logoutBtn.setContentAreaFilled(false);
        logoutBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        logoutBtn.setHorizontalAlignment(SwingConstants.LEFT);
        logoutBtn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        logoutBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        logoutBtn.setBorder(new EmptyBorder(8, 14, 8, 14));
        logoutBtn.setName("logoutBtn");
        logoutBtn.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) { logoutBtn.setContentAreaFilled(true); logoutBtn.repaint(); }
            public void mouseExited (MouseEvent e) { logoutBtn.setContentAreaFilled(false); logoutBtn.repaint(); }
        });
        logoutBtn.addActionListener(e -> logout());
        bottomArea.add(logoutBtn);
        sidebar.add(bottomArea, BorderLayout.SOUTH);
        return sidebar;
    }

    private String getInitials(String fullName) {
        if (fullName == null || fullName.isBlank()) return "?";
        String[] parts = fullName.trim().split("\\s+");
        if (parts.length == 1) return parts[0].substring(0, Math.min(2, parts[0].length())).toUpperCase();
        return ("" + parts[0].charAt(0) + parts[parts.length-1].charAt(0)).toUpperCase();
    }

    private void addSectionLabel(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 9));
        lbl.setForeground(new Color(0x2d5a3d));
        lbl.setBorder(new EmptyBorder(14, 16, 4, 16));
        lbl.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
        lbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        lbl.setName("sectionLabel");
        navContainer.add(lbl);
    }

    private void addNavBtn(IconType iconType, String text, String card) {
        JButton btn = new JButton() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                if (isContentAreaFilled()) {
                    g2.setColor(getBackground());
                    g2.fillRoundRect(6, 2, getWidth()-12, getHeight()-4, 8, 8);
                }
                super.paintComponent(g2);
                g2.dispose();
            }
        };
        btn.setOpaque(false);
        btn.setIcon(makeIcon(iconType, CLR_MUTED, 18));
        btn.setText("  " + text);
        btn.setFont(FONT_NAV);
        btn.setForeground(CLR_MUTED);
        btn.setBackground(CLR_SIDEBAR);
        btn.setBorderPainted(false); btn.setContentAreaFilled(false); btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setHorizontalAlignment(SwingConstants.LEFT);
        btn.setBorder(new EmptyBorder(9, 16, 9, 16));
        btn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
        btn.setAlignmentX(Component.CENTER_ALIGNMENT);
        btn.setIconTextGap(8);
        btn.setName("nav");

        navEntries.add(new NavEntry(btn, iconType, text, card));

        btn.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) {
                if (btn != activeNavBtn) {
                    btn.setContentAreaFilled(true);
                    btn.setBackground(CLR_SIDEBAR_HOV);
                    btn.setForeground(CLR_TEXT);
                    btn.setFont(FONT_NAV);
                    btn.setIcon(makeIcon(iconType, CLR_TEXT, 18));
                }
            }
            public void mouseExited(MouseEvent e) {
                if (btn != activeNavBtn) {
                    btn.setContentAreaFilled(false);
                    btn.setBackground(CLR_SIDEBAR);
                    btn.setForeground(CLR_MUTED);
                    btn.setFont(FONT_NAV);
                    btn.setIcon(makeIcon(iconType, CLR_MUTED, 18));
                }
            }
        });

        btn.addActionListener(e -> {
            if (activeNavBtn != null) {
                NavEntry prev = navEntries.stream().filter(ne -> ne.btn == activeNavBtn).findFirst().orElse(null);
                if (prev != null) activeNavBtn.setIcon(makeIcon(prev.iconType, CLR_MUTED, 18));
                activeNavBtn.setContentAreaFilled(false);
                activeNavBtn.setBackground(CLR_SIDEBAR);
                activeNavBtn.setForeground(CLR_MUTED);
                activeNavBtn.setFont(FONT_NAV);
            }
            btn.setIcon(makeIcon(iconType, CLR_ACCENT, 18));
            btn.setContentAreaFilled(true);
            btn.setBackground(CLR_SIDEBAR_SEL);
            btn.setForeground(CLR_ACCENT);
            btn.setFont(FONT_NAV_BOLD);
            activeNavBtn = btn;

            if ("dashboard".equals(card)) {
                cardLayout.show(contentPanel, "dashboard");
                refreshDashboardStats();
                return;
            }
            if (!isPanelLoaded(card)) {
                loadPanelAsync(card);
            } else {
                cardLayout.show(contentPanel, card);
                if ("reservations".equals(card)    && reservationsPanel  != null) reservationsPanel.refresh();
                if ("members".equals(card)         && membersPanel       != null) membersPanel.refresh();
                if ("return".equals(card)          && returnPanel        != null) returnPanel.refresh();
                if ("accept-requests".equals(card) && acceptRequestPanel != null) acceptRequestPanel.refresh();
            }
        });
        navContainer.add(btn);
        navContainer.add(Box.createVerticalStrut(1));
    }

    // ── Toggle animation ──────────────────────────────────────────────────────
    private void toggleSidebar() {
        sidebarExpanded    = !sidebarExpanded;
        sidebarTargetWidth = sidebarExpanded ? SIDEBAR_EXPANDED : SIDEBAR_COLLAPSED;
        toggleBtn.setText(sidebarExpanded ? "◀" : "▶");

        for (NavEntry ne : navEntries) {
            ne.btn.setText(sidebarExpanded ? "  " + ne.text : "");
            ne.btn.setHorizontalAlignment(sidebarExpanded ? SwingConstants.LEFT : SwingConstants.CENTER);
            ne.btn.setBorder(new EmptyBorder(9, sidebarExpanded ? 16 : 0, 9, sidebarExpanded ? 16 : 0));
        }
        for (Component c : navContainer.getComponents()) {
            if (c instanceof JLabel lbl && "sectionLabel".equals(lbl.getName()))
                lbl.setVisible(sidebarExpanded);
        }

        // Hide/show user row content
        Component bottomComp = sidebar.getComponent(2);
        if (bottomComp instanceof JPanel bottomArea) {
            for (Component c : bottomArea.getComponents()) {
                if (c instanceof JPanel p && "userRow".equals(p.getName())) {
                    p.setVisible(sidebarExpanded);
                }
                if (c instanceof JButton jb && "logoutBtn".equals(jb.getName())) {
                    jb.setText(sidebarExpanded ? "  Logout" : "");
                    jb.setHorizontalAlignment(sidebarExpanded ? SwingConstants.LEFT : SwingConstants.CENTER);
                    jb.setBorder(new EmptyBorder(8, sidebarExpanded ? 14 : 0, 8, sidebarExpanded ? 14 : 0));
                }
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

    private void navigateTo(String card) {
        for (NavEntry ne : navEntries) {
            if (ne.card.equals(card)) { ne.btn.doClick(); break; }
        }
    }

    // ── Top bar ───────────────────────────────────────────────────────────────
    private JPanel buildTopBar() {
        JPanel bar = new JPanel(new BorderLayout()) {
            @Override protected void paintComponent(Graphics g) {
                super.paintComponent(g);
            }
        };
        bar.setBackground(CLR_TOP_BAR);
        bar.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 1, 0, CLR_BORDER),
            new EmptyBorder(13, 22, 13, 22)
        ));

        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        left.setOpaque(false);
        JLabel title = new JLabel("Library Management System");
        title.setFont(new Font("Segoe UI", Font.BOLD, 14));
        title.setForeground(CLR_TEXT);
        JLabel sub = new JLabel("  —  Librarian Portal");
        sub.setFont(FONT_BODY);
        sub.setForeground(CLR_MUTED);
        left.add(title);
        left.add(sub);

        JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 14, 0));
        rightPanel.setOpaque(false);
        String today = LocalDate.now().format(DateTimeFormatter.ofPattern("EEEE, MMM dd, yyyy"));
        JLabel dateLabel = new JLabel(today);
        dateLabel.setFont(FONT_SMALL);
        dateLabel.setForeground(CLR_MUTED);

        // Online dot + name
        JPanel userChip = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        userChip.setOpaque(false);
        JPanel onlineDot = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(CLR_ACCENT);
                g2.fillOval(0, 0, getWidth(), getHeight());
                g2.dispose();
            }
        };
        onlineDot.setPreferredSize(new Dimension(8, 8));
        onlineDot.setOpaque(false);
        JLabel libLabel = new JLabel(librarian.getFullName());
        libLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        libLabel.setForeground(CLR_ACCENT);
        userChip.add(onlineDot);
        userChip.add(libLabel);

        rightPanel.add(dateLabel);
        rightPanel.add(userChip);

        bar.add(left,        BorderLayout.WEST);
        bar.add(rightPanel,  BorderLayout.EAST);
        return bar;
    }

    // ── Dashboard Panel ───────────────────────────────────────────────────────
    private JPanel buildDashboardPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 22));
        panel.setBackground(CLR_BG);

        // Header row
        JPanel headerRow = new JPanel(new BorderLayout());
        headerRow.setOpaque(false);
        JPanel headerText = new JPanel();
        headerText.setOpaque(false);
        headerText.setLayout(new BoxLayout(headerText, BoxLayout.Y_AXIS));
        JLabel h = new JLabel("Dashboard overview");
        h.setFont(FONT_TITLE);
        h.setForeground(CLR_TEXT);
        JLabel sub = new JLabel("Welcome back, " + librarian.getFullName() + "!");
        sub.setFont(FONT_SMALL);
        sub.setForeground(CLR_MUTED);
        headerText.add(h);
        headerText.add(Box.createVerticalStrut(3));
        headerText.add(sub);
        headerRow.add(headerText, BorderLayout.WEST);

        JButton refreshBtn = makeAccentButton("↺  Refresh", CLR_ACCENT2);
        refreshBtn.addActionListener(e -> refreshDashboardStats());
        headerRow.add(refreshBtn, BorderLayout.EAST);
        panel.add(headerRow, BorderLayout.NORTH);

        // Stat cards
        JPanel cardsRow = new JPanel(new GridLayout(1, 4, 14, 0));
        cardsRow.setOpaque(false);
        statBooksVal    = new JLabel("—", SwingConstants.LEFT);
        statMembersVal  = new JLabel("—", SwingConstants.LEFT);
        statBorrowedVal = new JLabel("—", SwingConstants.LEFT);
        statOverdueVal  = new JLabel("—", SwingConstants.LEFT);
        cardsRow.add(buildStatCard("Total Books",        "All catalog titles",  statBooksVal,    CLR_CARD_GREEN,  IconType.BOOKS,     "+12 this mo."));
        cardsRow.add(buildStatCard("Active Members",     "Registered users",    statMembersVal,  CLR_CARD_CYAN,   IconType.MEMBERS,   "Active"));
        cardsRow.add(buildStatCard("Currently Borrowed", "Books on loan",       statBorrowedVal, CLR_CARD_YELLOW, IconType.BORROW,    "On loan"));
        cardsRow.add(buildStatCard("Overdue",            "Past return date",    statOverdueVal,  CLR_CARD_RED,    IconType.OVERDUE,   "Needs action"));

        // Quick actions
        JPanel qa = new JPanel(new BorderLayout(0, 12));
        qa.setOpaque(false);
        JLabel qaLabel = new JLabel("Quick actions");
        qaLabel.setFont(FONT_HEADER);
        qaLabel.setForeground(CLR_TEXT);
        qa.add(qaLabel, BorderLayout.NORTH);

        JPanel qaGrid = new JPanel(new GridLayout(2, 3, 12, 12));
        qaGrid.setOpaque(false);
        qaGrid.add(buildQuickActionCard("Accept Requests",  "Approve or reject borrow requests", IconType.APPROVE,      "accept-requests"));
        qaGrid.add(buildQuickActionCard("Process Borrow",   "Check out books to members",        IconType.BORROW,       "borrow"));
        qaGrid.add(buildQuickActionCard("Process Return",   "Return and process fines",          IconType.RETURN,       "return"));
        qaGrid.add(buildQuickActionCard("Reservations",     "Manage pending reservations",       IconType.RESERVATIONS, "reservations"));
        qaGrid.add(buildQuickActionCard("Manage Books",     "Add, edit, or remove books",        IconType.BOOKS,        "books"));
        qaGrid.add(buildQuickActionCard("Manage Members",   "View and manage member records",    IconType.MEMBERS,      "members"));
        qa.add(qaGrid, BorderLayout.CENTER);

        JPanel center = new JPanel(new BorderLayout(0, 20));
        center.setOpaque(false);
        center.add(cardsRow, BorderLayout.NORTH);
        center.add(qa,       BorderLayout.CENTER);
        panel.add(center, BorderLayout.CENTER);
        return panel;
    }

    private JPanel buildStatCard(String title, String subtitle, JLabel valueLabel,
                                 Color accentColor, IconType iconType, String badgeText) {
        JPanel card = new JPanel(new BorderLayout(0, 10)) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                // Card bg
                g2.setColor(CLR_CARD);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
                // Tinted overlay
                g2.setColor(accentColor);
                g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.06f));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
                // Border
                g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.25f));
                g2.setColor(accentColor);
                g2.setStroke(new BasicStroke(1f));
                g2.drawRoundRect(0, 0, getWidth()-1, getHeight()-1, 12, 12);
                g2.dispose();
            }
        };
        card.setOpaque(false);
        card.setBorder(new EmptyBorder(18, 18, 18, 18));

        // Top row: icon wrap + badge
        JPanel top = new JPanel(new BorderLayout());
        top.setOpaque(false);

        JPanel iconWrap = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(accentColor);
                g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.13f));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        iconWrap.setOpaque(false);
        iconWrap.setPreferredSize(new Dimension(36, 36));
        iconWrap.setLayout(new GridBagLayout());
        iconWrap.add(new JLabel(makeIcon(iconType, accentColor, 20)));

        // Badge pill
        JLabel badge = new JLabel(badgeText) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(accentColor);
                g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.13f));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20);
                g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1f));
                super.paintComponent(g2);
                g2.dispose();
            }
        };
        badge.setFont(new Font("Segoe UI", Font.BOLD, 9));
        badge.setForeground(accentColor);
        badge.setBorder(new EmptyBorder(3, 8, 3, 8));
        badge.setOpaque(false);

        top.add(iconWrap, BorderLayout.WEST);
        top.add(badge,    BorderLayout.EAST);

        // Value
        valueLabel.setFont(FONT_STAT);
        valueLabel.setForeground(CLR_TEXT);

        // Bottom: title + subtitle
        JPanel bottom = new JPanel();
        bottom.setOpaque(false);
        bottom.setLayout(new BoxLayout(bottom, BoxLayout.Y_AXIS));
        JLabel titleLbl = new JLabel(title);
        titleLbl.setFont(new Font("Segoe UI", Font.BOLD, 12));
        titleLbl.setForeground(CLR_TEXT);
        JLabel subLbl = new JLabel(subtitle);
        subLbl.setFont(FONT_SMALL);
        subLbl.setForeground(CLR_MUTED);
        bottom.add(titleLbl);
        bottom.add(subLbl);

        card.add(top,        BorderLayout.NORTH);
        card.add(valueLabel, BorderLayout.CENTER);
        card.add(bottom,     BorderLayout.SOUTH);
        return card;
    }

    private JPanel buildQuickActionCard(String title, String desc, IconType icon, String card) {
        JPanel p = new JPanel(new BorderLayout(0, 8)) {
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
            }
        };
        p.setOpaque(false);
        p.setBackground(CLR_CARD);
        p.setBorder(new EmptyBorder(18, 18, 18, 18));
        p.setCursor(new Cursor(Cursor.HAND_CURSOR));

        // Icon wrap
        JPanel iconWrap = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(CLR_ACCENT);
                g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.12f));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        iconWrap.setOpaque(false);
        iconWrap.setPreferredSize(new Dimension(36, 36));
        iconWrap.setMaximumSize(new Dimension(36, 36));
        iconWrap.setLayout(new GridBagLayout());
        iconWrap.add(new JLabel(makeIcon(icon, CLR_ACCENT, 18)));

        JLabel titleLbl = new JLabel(title);
        titleLbl.setFont(new Font("Segoe UI", Font.BOLD, 13));
        titleLbl.setForeground(CLR_TEXT);
        JLabel descLbl = new JLabel(desc);
        descLbl.setFont(FONT_SMALL);
        descLbl.setForeground(CLR_MUTED);

        JPanel text = new JPanel();
        text.setOpaque(false);
        text.setLayout(new BoxLayout(text, BoxLayout.Y_AXIS));
        text.add(titleLbl);
        text.add(Box.createVerticalStrut(2));
        text.add(descLbl);

        p.add(iconWrap, BorderLayout.NORTH);
        p.add(text,     BorderLayout.CENTER);

        p.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) {
                p.setBackground(CLR_CARD2);
                p.repaint();
            }
            public void mouseExited(MouseEvent e) {
                p.setBackground(CLR_CARD);
                p.repaint();
            }
            public void mouseClicked(MouseEvent e) { navigateTo(card); }
        });
        return p;
    }

    private void refreshDashboardStats() {
        new SwingWorker<int[], Void>() {
            @Override protected int[] doInBackground() {
                int books=0, members=0, borrowed=0, overdue=0;
                try { books   = bookDAO.getTotalBookCount();      } catch (Exception ignored) {}
                try { members = memberDAO.getActiveMemberCount(); } catch (Exception ignored) {}
                try {
                    List<BorrowedRecord> list = borrowDAO.getActiveBorrows();
                    LocalDate today = LocalDate.now();
                    for (BorrowedRecord r : list) {
                        borrowed++;
                        if (r.getDueDate() != null && r.getDueDate().toLocalDate().isBefore(today)) overdue++;
                    }
                } catch (Exception ignored) {}
                return new int[]{ books, members, borrowed, overdue };
            }
            @Override protected void done() {
                try {
                    int[] v = get();
                    statBooksVal   .setText(String.valueOf(v[0]));
                    statMembersVal .setText(String.valueOf(v[1]));
                    statBorrowedVal.setText(String.valueOf(v[2]));
                    statOverdueVal .setText(String.valueOf(v[3]));
                } catch (Exception ignored) {}
            }
        }.execute();
    }

    // ── Lazy loader ───────────────────────────────────────────────────────────
    private void loadPanelAsync(String card) {
        new SwingWorker<Void, Void>() {
            protected Void doInBackground() {
                switch (card) {
                    case "books"           -> { if (booksPanel==null)         booksPanel         = new ManageBooksPanel(librarian); }
                    case "borrow"          -> { if (borrowPanel==null)        borrowPanel        = new ProcessBorrowPanel(librarian); }
                    case "return"          -> { if (returnPanel==null)        returnPanel        = new ProcessReturnPanel(librarian); }
                    case "reservations"    -> { if (reservationsPanel==null)  reservationsPanel  = new ManageReservationsPanel(librarian); }
                    case "members"         -> { if (membersPanel==null)       membersPanel       = new ManageMembersPanel(); }
                    case "accept-requests" -> { if (acceptRequestPanel==null) acceptRequestPanel = new AcceptBorrowRequestPanel(librarian); }
                }
                return null;
            }
            protected void done() {
                switch (card) {
                    case "books"           -> contentPanel.add(booksPanel,         "books");
                    case "borrow"          -> contentPanel.add(borrowPanel,        "borrow");
                    case "return"          -> contentPanel.add(returnPanel,        "return");
                    case "reservations"    -> contentPanel.add(reservationsPanel,  "reservations");
                    case "members"         -> contentPanel.add(membersPanel,       "members");
                    case "accept-requests" -> contentPanel.add(acceptRequestPanel, "accept-requests");
                }
                cardLayout.show(contentPanel, card);
                contentPanel.revalidate(); contentPanel.repaint();
            }
        }.execute();
    }

    private boolean isPanelLoaded(String card) {
        return switch (card) {
            case "books"           -> booksPanel         != null;
            case "borrow"          -> borrowPanel        != null;
            case "return"          -> returnPanel        != null;
            case "reservations"    -> reservationsPanel  != null;
            case "members"         -> membersPanel       != null;
            case "accept-requests" -> acceptRequestPanel != null;
            default                -> false;
        };
    }

    private void logout() {
        int c = JOptionPane.showConfirmDialog(this, "Logout from the system?", "Confirm Logout", JOptionPane.YES_NO_OPTION);
        if (c == JOptionPane.YES_OPTION) { new LoginFrame().setVisible(true); dispose(); }
    }

    // ── Shared static helpers ─────────────────────────────────────────────────
    public static void styleTable(JTable table) {
        table.setFont(FONT_BODY);
        table.setForeground(CLR_TEXT);
        table.setBackground(CLR_CARD);
        table.setRowHeight(34);
        table.setSelectionBackground(new Color(0x1e4a2a));
        table.setSelectionForeground(CLR_ACCENT);
        table.setGridColor(CLR_BORDER);
        table.setShowVerticalLines(false);
        table.setIntercellSpacing(new Dimension(0, 1));

        JTableHeader h = table.getTableHeader();
        h.setFont(FONT_LABEL);
        h.setBackground(new Color(0x091510));
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
                    if (val instanceof String s) {
                        if (s.equals("Active")    || s.equals("ACCEPTED")) setForeground(CLR_ACCENT);
                        else if (s.equals("Inactive") || s.equals("REJECTED")) setForeground(CLR_ACCENT4);
                        else if (s.equals("PENDING"))  setForeground(CLR_ACCENT3);
                    }
                }
                return this;
            }
        });
    }

    public static void applyHeaderRenderer(JTable table, Color bgColor, Color fgColor, Font font) {
        table.getTableHeader().setDefaultRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(
                    JTable t, Object val, boolean sel, boolean focus, int row, int col) {
                JLabel lbl = (JLabel) super.getTableCellRendererComponent(
                        t, val, sel, focus, row, col);
                lbl.setBackground(bgColor);
                lbl.setForeground(fgColor);
                lbl.setFont(font);
                lbl.setOpaque(true);
                lbl.setHorizontalAlignment(SwingConstants.LEFT);
                lbl.setBorder(new EmptyBorder(0, 10, 0, 10));
                return lbl;
            }
        });
        table.getTableHeader().setBackground(bgColor);
        table.getTableHeader().setForeground(fgColor);
    }

    public static JScrollPane styledScrollPane(JTable table) {
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
        sp.getViewport().setBackground(CLR_CARD);
        sp.setBorder(BorderFactory.createLineBorder(CLR_BORDER, 1));
        sp.getVerticalScrollBar().setBackground(CLR_CARD);
        return sp;
    }

    public static JButton makeAccentButton(String text, Color accent) {
        JButton btn = new JButton(text) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getBackground());
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
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
        btn.setBorder(new EmptyBorder(8, 16, 8, 16));
        Color darker = accent.darker();
        btn.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) { btn.setBackground(darker); }
            public void mouseExited (MouseEvent e) { btn.setBackground(accent); }
        });
        return btn;
    }

    public static JButton primaryBtn(String text) { return makeAccentButton(text, new Color(0x1e4a2a)); }
    public static JButton accentBtn(String text)  { return makeAccentButton(text, CLR_ACCENT); }
    public static JButton dangerBtn(String text)  { return makeAccentButton(text, CLR_ACCENT4); }
}