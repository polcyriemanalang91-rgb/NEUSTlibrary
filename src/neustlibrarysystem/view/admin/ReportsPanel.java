package neustlibrarysystem.view.admin;

import neustlibrarysystem.dao.BorrowDAO;
import neustlibrarysystem.dao.BookDAO;
import neustlibrarysystem.dao.MemberDAO;
import neustlibrarysystem.model.Admin;

import com.itextpdf.text.Document;
import com.itextpdf.text.Element;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.BaseColor;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;

import javax.swing.*;
import javax.swing.plaf.basic.BasicTabbedPaneUI;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.print.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class ReportsPanel extends JPanel {

    // ── Design tokens — same as AdminDashboard ────────────────────────────────
    private static final Color CLR_BG      = new Color(0x0d1b12);
    private static final Color CLR_CARD    = new Color(0x132218);
    private static final Color CLR_CARD2   = new Color(0x1a2e1f);
    private static final Color CLR_BORDER  = new Color(0x1f3d28);
    private static final Color CLR_TEXT    = new Color(0xe2f5e8);
    private static final Color CLR_MUTED   = new Color(0x6b9e7a);
    private static final Color CLR_ACCENT  = new Color(0x4ade80);
    private static final Color CLR_ACCENT2 = new Color(0x22d3ee);
    private static final Color CLR_ACCENT3 = new Color(0xfbbf24);
    private static final Color CLR_ACCENT4 = new Color(0xf87171);

    private static final Font FONT_TITLE = new Font("Segoe UI", Font.BOLD,  22);
    private static final Font FONT_BODY  = new Font("Segoe UI", Font.PLAIN, 13);
    private static final Font FONT_LABEL = new Font("Segoe UI", Font.BOLD,  12);
    private static final Font FONT_SMALL = new Font("Segoe UI", Font.PLAIN, 11);
    private static final Font FONT_TAB   = new Font("Segoe UI", Font.BOLD,  13);

    private final Admin     currentAdmin;
    private final BorrowDAO borrowDAO = new BorrowDAO();
    private final BookDAO   bookDAO   = new BookDAO();
    private final MemberDAO memberDAO = new MemberDAO();

    private JTable      borrowTable, overdueTable, inventoryTable, memberTable;
    private JTabbedPane reportTabs;

    public ReportsPanel(Admin currentAdmin) {
        this.currentAdmin = currentAdmin;
        initComponents();
    }

    // =========================================================================
    //  UI BUILD
    // =========================================================================
    private void initComponents() {
        setLayout(new BorderLayout(0, 16));
        setBackground(CLR_BG);

        // ── Header ────────────────────────────────────────────────────────────
        JPanel hdr = new JPanel();
        hdr.setOpaque(false);
        hdr.setLayout(new BoxLayout(hdr, BoxLayout.Y_AXIS));
        JLabel title = new JLabel("Reports & Analytics");
        title.setFont(FONT_TITLE);
        title.setForeground(CLR_TEXT);
        JLabel sub = new JLabel("Generate, export, and print system reports");
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
        add(hdr, BorderLayout.NORTH);

        // ── Tabs ──────────────────────────────────────────────────────────────
        reportTabs = new JTabbedPane();
        reportTabs.setFont(FONT_TAB);
        reportTabs.setBackground(CLR_CARD);
        reportTabs.setForeground(Color.WHITE);
        reportTabs.setBorder(BorderFactory.createLineBorder(CLR_BORDER, 1));

        reportTabs.setUI(new BasicTabbedPaneUI() {
            @Override
            protected void installDefaults() {
                super.installDefaults();
                highlight      = CLR_ACCENT;
                lightHighlight = CLR_ACCENT;
                shadow         = CLR_BORDER;
                darkShadow     = CLR_BORDER;
                focus          = CLR_ACCENT;
            }

            @Override
            protected void paintTabBackground(Graphics g, int tabPlacement,
                    int tabIndex, int x, int y, int w, int h, boolean isSelected) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                        RenderingHints.VALUE_ANTIALIAS_ON);
                if (isSelected) {
                    g2.setColor(CLR_ACCENT);
                } else {
                    g2.setColor(CLR_CARD2);
                }
                g2.fillRoundRect(x + 2, y + 2, w - 4, h - 2, 8, 8);
                g2.dispose();
            }

            @Override
            protected void paintTabBorder(Graphics g, int tabPlacement,
                    int tabIndex, int x, int y, int w, int h, boolean isSelected) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                        RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(isSelected ? CLR_ACCENT : CLR_BORDER);
                g2.setStroke(new BasicStroke(isSelected ? 1.5f : 1f));
                g2.drawRoundRect(x + 2, y + 2, w - 4, h - 2, 8, 8);
                g2.dispose();
            }

            @Override
            protected void paintText(Graphics g, int tabPlacement, Font font,
                    FontMetrics metrics, int tabIndex, String title,
                    Rectangle textRect, boolean isSelected) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                        RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
                g2.setFont(FONT_TAB);
                // Selected tab: dark green text on green bg; others: bright white
                g2.setColor(isSelected ? new Color(0x0a1a10) : CLR_TEXT);
                g2.drawString(title, textRect.x, textRect.y + metrics.getAscent());
                g2.dispose();
            }

            @Override
            protected void paintFocusIndicator(Graphics g, int tabPlacement,
                    Rectangle[] rects, int tabIndex, Rectangle iconRect,
                    Rectangle textRect, boolean isSelected) {
                // no focus ring
            }

            @Override
            protected void paintContentBorder(Graphics g, int tabPlacement,
                    int selectedIndex) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setColor(CLR_BORDER);
                int tabAreaH = calculateTabAreaHeight(tabPlacement, runCount, maxTabHeight);
                g2.drawRect(
                    tabAreaInsets.left,
                    tabAreaH - 1,
                    tabPane.getWidth()  - tabAreaInsets.left - tabAreaInsets.right - 1,
                    tabPane.getHeight() - tabAreaH
                );
                g2.dispose();
            }

            @Override
            protected int calculateTabHeight(int tabPlacement, int tabIndex,
                    int fontHeight) {
                return 36; // taller tabs for better readability
            }
        });

        reportTabs.addTab("Borrow Summary",  buildBorrowSummaryTab());
        reportTabs.addTab("Overdue Books",   buildOverdueTab());
        reportTabs.addTab("Book Inventory",  buildInventoryTab());
        reportTabs.addTab("Member Activity", buildMemberActivityTab());
        add(reportTabs, BorderLayout.CENTER);

        // ── Export bar ────────────────────────────────────────────────────────
        JPanel exportBar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 8));
        exportBar.setOpaque(false);

        JButton btnPrint = accentBtn("Print",      CLR_ACCENT2);
        JButton btnPDF   = accentBtn("Export PDF", CLR_ACCENT4);
        JButton btnCSV   = accentBtn("Export CSV", CLR_ACCENT3);

        btnPrint.addActionListener(e -> printCurrentTab());
        btnPDF  .addActionListener(e -> exportPDF());
        btnCSV  .addActionListener(e -> exportCSV());

        exportBar.add(btnPrint);
        exportBar.add(btnPDF);
        exportBar.add(btnCSV);
        add(exportBar, BorderLayout.SOUTH);
    }

    // =========================================================================
    //  TAB BUILDERS
    // =========================================================================
    private JPanel buildBorrowSummaryTab() {
        JPanel panel = new JPanel(new BorderLayout(8, 8));
        panel.setBackground(CLR_BG);
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        String[] cols = {"Borrow ID", "Member Name", "Student ID",
                         "Book Title", "Borrow Date", "Due Date", "Status", "Fine (PHP)"};
        DefaultTableModel model = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        borrowTable = new JTable(model);
        styleTable(borrowTable);

        JPanel topBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        topBar.setOpaque(false);
        JLabel filterLbl = new JLabel("Filter by Status:");
        filterLbl.setFont(FONT_BODY);
        filterLbl.setForeground(CLR_MUTED);
        JComboBox<String> statusBox = new JComboBox<>(
            new String[]{"All", "Borrowed", "Returned", "Overdue", "Lost"});
        statusBox.setFont(FONT_BODY);
        statusBox.setBackground(CLR_CARD2);
        statusBox.setForeground(CLR_TEXT);
        JButton loadBtn = accentBtn("Load", CLR_ACCENT);

        loadBtn.addActionListener(e -> {
            String sel = (String) statusBox.getSelectedItem();
            loadBtn.setEnabled(false); loadBtn.setText("Loading...");
            new SwingWorker<List<Object[]>, Void>() {
                @Override protected List<Object[]> doInBackground() {
                    return borrowDAO.getBorrowSummaryReport(sel);
                }
                @Override protected void done() {
                    try { model.setRowCount(0); for (Object[] row : get()) model.addRow(row); }
                    catch (Exception ex) { showErr("borrow summary", ex); }
                    finally { loadBtn.setEnabled(true); loadBtn.setText("Load"); }
                }
            }.execute();
        });

        topBar.add(filterLbl);
        topBar.add(statusBox);
        topBar.add(loadBtn);
        panel.add(topBar, BorderLayout.NORTH);
        panel.add(styledScroll(borrowTable), BorderLayout.CENTER);
        return panel;
    }

    private JPanel buildOverdueTab() {
        JPanel panel = new JPanel(new BorderLayout(8, 8));
        panel.setBackground(CLR_BG);
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        String[] cols = {"Borrow ID", "Member Name", "Student ID",
                         "Book Title", "Borrow Date", "Due Date", "Days Overdue", "Fine (PHP)"};
        DefaultTableModel model = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        overdueTable = new JTable(model);
        styleTable(overdueTable);

        JPanel topBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        topBar.setOpaque(false);
        JButton loadBtn = accentBtn("Load Overdue Records", CLR_ACCENT4);

        loadBtn.addActionListener(e -> {
            loadBtn.setEnabled(false); loadBtn.setText("Loading...");
            new SwingWorker<List<Object[]>, Void>() {
                @Override protected List<Object[]> doInBackground() {
                    return borrowDAO.getOverdueReport();
                }
                @Override protected void done() {
                    try { model.setRowCount(0); for (Object[] row : get()) model.addRow(row); }
                    catch (Exception ex) { showErr("overdue records", ex); }
                    finally { loadBtn.setEnabled(true); loadBtn.setText("Load Overdue Records"); }
                }
            }.execute();
        });

        topBar.add(loadBtn);
        panel.add(topBar, BorderLayout.NORTH);
        panel.add(styledScroll(overdueTable), BorderLayout.CENTER);
        return panel;
    }

    private JPanel buildInventoryTab() {
        JPanel panel = new JPanel(new BorderLayout(8, 8));
        panel.setBackground(CLR_BG);
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        String[] cols = {"Book ID", "ISBN", "Title", "Category",
                         "Publisher", "Total Copies", "Available", "Status"};
        DefaultTableModel model = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        inventoryTable = new JTable(model);
        styleTable(inventoryTable);

        JPanel topBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        topBar.setOpaque(false);
        JButton loadBtn = accentBtn("Load Inventory", CLR_ACCENT);

        loadBtn.addActionListener(e -> {
            loadBtn.setEnabled(false); loadBtn.setText("Loading...");
            new SwingWorker<List<Object[]>, Void>() {
                @Override protected List<Object[]> doInBackground() {
                    return bookDAO.getInventoryReport();
                }
                @Override protected void done() {
                    try { model.setRowCount(0); for (Object[] row : get()) model.addRow(row); }
                    catch (Exception ex) { showErr("inventory", ex); }
                    finally { loadBtn.setEnabled(true); loadBtn.setText("Load Inventory"); }
                }
            }.execute();
        });

        topBar.add(loadBtn);
        panel.add(topBar, BorderLayout.NORTH);
        panel.add(styledScroll(inventoryTable), BorderLayout.CENTER);
        return panel;
    }

    private JPanel buildMemberActivityTab() {
        JPanel panel = new JPanel(new BorderLayout(8, 8));
        panel.setBackground(CLR_BG);
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        String[] cols = {"Member ID", "Student ID", "Full Name", "Email",
                         "Total Borrows", "Active", "Returned", "Overdue"};
        DefaultTableModel model = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        memberTable = new JTable(model);
        styleTable(memberTable);

        JPanel topBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        topBar.setOpaque(false);
        JButton loadBtn = accentBtn("Load Member Activity", CLR_ACCENT2);

        loadBtn.addActionListener(e -> {
            loadBtn.setEnabled(false); loadBtn.setText("Loading...");
            new SwingWorker<List<Object[]>, Void>() {
                @Override protected List<Object[]> doInBackground() {
                    return memberDAO.getMemberActivityReport();
                }
                @Override protected void done() {
                    try { model.setRowCount(0); for (Object[] row : get()) model.addRow(row); }
                    catch (Exception ex) { showErr("member activity", ex); }
                    finally { loadBtn.setEnabled(true); loadBtn.setText("Load Member Activity"); }
                }
            }.execute();
        });

        topBar.add(loadBtn);
        panel.add(topBar, BorderLayout.NORTH);
        panel.add(styledScroll(memberTable), BorderLayout.CENTER);
        return panel;
    }

    // =========================================================================
    //  HELPERS
    // =========================================================================
    private JTable getActiveTable() {
        return switch (reportTabs.getSelectedIndex()) {
            case 0 -> borrowTable;
            case 1 -> overdueTable;
            case 2 -> inventoryTable;
            case 3 -> memberTable;
            default -> borrowTable;
        };
    }

    private String getActiveTabTitle() {
        return reportTabs.getTitleAt(reportTabs.getSelectedIndex())
                         .replaceAll("^[^a-zA-Z]+", "").trim();
    }

    private boolean hasData() {
        if (((DefaultTableModel) getActiveTable().getModel()).getRowCount() == 0) {
            JOptionPane.showMessageDialog(this,
                "Load data first before exporting.", "No Data", JOptionPane.WARNING_MESSAGE);
            return false;
        }
        return true;
    }

    // =========================================================================
    //  PRINT
    // =========================================================================
    private void printCurrentTab() {
        if (!hasData()) return;
        try {
            getActiveTable().print(
                JTable.PrintMode.FIT_WIDTH,
                new java.text.MessageFormat("NEUST Library — " + getActiveTabTitle()),
                new java.text.MessageFormat("Page {0}")
            );
        } catch (PrinterException e) {
            JOptionPane.showMessageDialog(this,
                "Print error: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // =========================================================================
    //  EXPORT PDF
    // =========================================================================
    private void exportPDF() {
        if (!hasData()) return;

        JTable table            = getActiveTable();
        DefaultTableModel model = (DefaultTableModel) table.getModel();
        String tabTitle         = getActiveTabTitle();
        int colCount            = model.getColumnCount();
        int rowCount            = model.getRowCount();

        String[] headers = new String[colCount];
        for (int c = 0; c < colCount; c++) headers[c] = model.getColumnName(c);
        Object[][] data = new Object[rowCount][colCount];
        for (int r = 0; r < rowCount; r++)
            for (int c = 0; c < colCount; c++)
                data[r][c] = model.getValueAt(r, c);

        JFileChooser fc = new JFileChooser();
        fc.setDialogTitle("Save PDF Report");
        fc.setSelectedFile(new File(tabTitle.replace(" ", "_") + "_"
            + new SimpleDateFormat("yyyyMMdd_HHmm").format(new Date()) + ".pdf"));
        if (fc.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) return;
        File file = fc.getSelectedFile();
        if (!file.getName().toLowerCase().endsWith(".pdf"))
            file = new File(file.getAbsolutePath() + ".pdf");
        final File finalFile = file;

        new SwingWorker<Void, Void>() {
            @Override protected Void doInBackground() throws Exception {
                Document doc = new Document(PageSize.A4.rotate());
                PdfWriter.getInstance(doc, new FileOutputStream(finalFile));
                doc.open();

                com.itextpdf.text.Font titleFont = new com.itextpdf.text.Font(
                    com.itextpdf.text.Font.FontFamily.HELVETICA, 16,
                    com.itextpdf.text.Font.BOLD, new BaseColor(0x0d, 0x1b, 0x12));
                Paragraph pTitle = new Paragraph("NEUST Library — " + tabTitle, titleFont);
                pTitle.setAlignment(Element.ALIGN_CENTER);
                doc.add(pTitle);

                com.itextpdf.text.Font subFont = new com.itextpdf.text.Font(
                    com.itextpdf.text.Font.FontFamily.HELVETICA, 10,
                    com.itextpdf.text.Font.ITALIC, BaseColor.GRAY);
                Paragraph pSub = new Paragraph(
                    "Generated: " + new SimpleDateFormat("MMMM dd, yyyy hh:mm a").format(new Date()),
                    subFont);
                pSub.setAlignment(Element.ALIGN_CENTER);
                pSub.setSpacingAfter(12);
                doc.add(pSub);

                PdfPTable pdfTable = new PdfPTable(colCount);
                pdfTable.setWidthPercentage(100);

                com.itextpdf.text.Font hFont = new com.itextpdf.text.Font(
                    com.itextpdf.text.Font.FontFamily.HELVETICA, 9,
                    com.itextpdf.text.Font.BOLD, BaseColor.WHITE);
                BaseColor headerBg = new BaseColor(0x13, 0x22, 0x18);

                for (String h : headers) {
                    PdfPCell cell = new PdfPCell(new Phrase(h, hFont));
                    cell.setBackgroundColor(headerBg);
                    cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                    cell.setPadding(6);
                    pdfTable.addCell(cell);
                }

                com.itextpdf.text.Font rowFont = new com.itextpdf.text.Font(
                    com.itextpdf.text.Font.FontFamily.HELVETICA, 8);
                BaseColor altBg = new BaseColor(0x1a, 0x2e, 0x1f);

                for (int r = 0; r < rowCount; r++) {
                    for (int c = 0; c < colCount; c++) {
                        String val = data[r][c] == null ? "" : data[r][c].toString();
                        com.itextpdf.text.Font cellFont = new com.itextpdf.text.Font(
                            com.itextpdf.text.Font.FontFamily.HELVETICA, 8,
                            com.itextpdf.text.Font.NORMAL,
                            r % 2 == 1 ? new BaseColor(0xe2, 0xf5, 0xe8) : BaseColor.DARK_GRAY);
                        PdfPCell cell = new PdfPCell(new Phrase(val, cellFont));
                        cell.setPadding(5);
                        if (r % 2 == 1) cell.setBackgroundColor(altBg);
                        pdfTable.addCell(cell);
                    }
                }
                doc.add(pdfTable);
                doc.close();
                return null;
            }

            @Override protected void done() {
                try {
                    get();
                    int ch = JOptionPane.showConfirmDialog(ReportsPanel.this,
                        "PDF saved!\n" + finalFile.getAbsolutePath() + "\n\nOpen file?",
                        "Export Successful", JOptionPane.YES_NO_OPTION,
                        JOptionPane.INFORMATION_MESSAGE);
                    if (ch == JOptionPane.YES_OPTION)
                        Desktop.getDesktop().open(finalFile);
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(ReportsPanel.this,
                        "PDF export failed: " + ex.getMessage(),
                        "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        }.execute();
    }

    // =========================================================================
    //  EXPORT CSV
    // =========================================================================
    private void exportCSV() {
        if (!hasData()) return;

        JTable table            = getActiveTable();
        DefaultTableModel model = (DefaultTableModel) table.getModel();
        int colCount            = model.getColumnCount();
        int rowCount            = model.getRowCount();

        String[] headers = new String[colCount];
        for (int c = 0; c < colCount; c++) headers[c] = model.getColumnName(c);
        Object[][] data = new Object[rowCount][colCount];
        for (int r = 0; r < rowCount; r++)
            for (int c = 0; c < colCount; c++)
                data[r][c] = model.getValueAt(r, c);

        JFileChooser fc = new JFileChooser();
        fc.setDialogTitle("Save CSV Report");
        fc.setSelectedFile(new File(getActiveTabTitle().replace(" ", "_") + "_"
            + new SimpleDateFormat("yyyyMMdd_HHmm").format(new Date()) + ".csv"));
        if (fc.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) return;
        File file = fc.getSelectedFile();
        if (!file.getName().toLowerCase().endsWith(".csv"))
            file = new File(file.getAbsolutePath() + ".csv");
        final File finalFile = file;

        new SwingWorker<Void, Void>() {
            @Override protected Void doInBackground() throws Exception {
                try (BufferedWriter bw = new BufferedWriter(
                        new OutputStreamWriter(new FileOutputStream(finalFile),
                            StandardCharsets.UTF_8))) {
                    bw.write('\uFEFF');
                    StringBuilder sb = new StringBuilder();
                    for (int c = 0; c < colCount; c++) {
                        if (c > 0) sb.append(',');
                        sb.append(csvEscape(headers[c]));
                    }
                    bw.write(sb.toString()); bw.newLine();
                    for (int r = 0; r < rowCount; r++) {
                        sb.setLength(0);
                        for (int c = 0; c < colCount; c++) {
                            if (c > 0) sb.append(',');
                            sb.append(csvEscape(data[r][c] == null ? "" : data[r][c].toString()));
                        }
                        bw.write(sb.toString()); bw.newLine();
                    }
                }
                return null;
            }

            @Override protected void done() {
                try {
                    get();
                    int ch = JOptionPane.showConfirmDialog(ReportsPanel.this,
                        "CSV saved!\n" + finalFile.getAbsolutePath() + "\n\nOpen file?",
                        "Export Successful", JOptionPane.YES_NO_OPTION,
                        JOptionPane.INFORMATION_MESSAGE);
                    if (ch == JOptionPane.YES_OPTION)
                        Desktop.getDesktop().open(finalFile);
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(ReportsPanel.this,
                        "CSV export failed: " + ex.getMessage(),
                        "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        }.execute();
    }

    // =========================================================================
    //  UTILITIES
    // =========================================================================
    private String csvEscape(String val) {
        if (val.contains(",") || val.contains("\"") || val.contains("\n"))
            return "\"" + val.replace("\"", "\"\"") + "\"";
        return val;
    }

    private void showErr(String ctx, Exception ex) {
        JOptionPane.showMessageDialog(this,
            "Error loading " + ctx + ": " + ex.getMessage(),
            "Error", JOptionPane.ERROR_MESSAGE);
    }

    // ── Accent button ─────────────────────────────────────────────────────────
    private JButton accentBtn(String text, Color accent) {
        JButton btn = new JButton(text) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                        RenderingHints.VALUE_ANTIALIAS_ON);
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
        btn.setBorder(BorderFactory.createEmptyBorder(9, 18, 9, 18));
        Color darker = accent.darker();
        btn.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) { if (btn.isEnabled()) btn.setBackground(darker); }
            public void mouseExited(MouseEvent e)  { if (btn.isEnabled()) btn.setBackground(accent); }
        });
        return btn;
    }

    // ── Table styling ─────────────────────────────────────────────────────────
    private void styleTable(JTable table) {
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
                setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 10));
                if (sel) {
                    setBackground(new Color(0x1e4a2a));
                    setForeground(CLR_ACCENT);
                } else {
                    setBackground(row % 2 == 0 ? CLR_CARD : CLR_CARD2);
                    setForeground(CLR_TEXT);
                }
                if (val instanceof String s) {
                    if      (s.equals("Overdue"))   setForeground(CLR_ACCENT4);
                    else if (s.equals("Borrowed"))  setForeground(CLR_ACCENT2);
                    else if (s.equals("Returned"))  setForeground(CLR_ACCENT);
                }
                return this;
            }
        });
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    }

    private JScrollPane styledScroll(JTable table) {
        JScrollPane sp = new JScrollPane(table);
        sp.setBackground(CLR_CARD);
        sp.getViewport().setBackground(CLR_CARD);
        sp.setBorder(BorderFactory.createLineBorder(CLR_BORDER, 1));
        sp.getVerticalScrollBar().setBackground(CLR_CARD);
        return sp;
    }
}