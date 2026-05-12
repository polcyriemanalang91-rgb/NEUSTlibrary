package neustlibrarysystem.view.common;

import neustlibrarysystem.dao.BookDAO;
import neustlibrarysystem.dao.ReservationDAO;
import neustlibrarysystem.model.Book;
import neustlibrarysystem.model.Member;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.List;
import java.util.stream.Collectors;

public class BrowseBooksPanel extends JPanel {

    private final Member         currentMember;
    private final BookDAO        bookDAO;
    private final ReservationDAO reservationDAO;

    private JTable               table;
    private DefaultTableModel    tableModel;
    private JTextField           tfSearch;
    private JComboBox<String>    filterCombo;
    private JButton              btnSearch, btnRefresh;
    private TableRowSorter<DefaultTableModel> sorter;

    // ── Column indices — must match the cols[] array in buildUI() exactly ─────
    // Columns: "ID", "Title", "Author(s)", "Category", "Publisher", "Availability"
    private static final int COL_ID           = 0;
    private static final int COL_TITLE        = 1;
    private static final int COL_AUTHORS      = 2;
    private static final int COL_CATEGORY     = 3;
    private static final int COL_PUBLISHER    = 4;
    private static final int COL_AVAILABILITY = 5;

    public BrowseBooksPanel(Member currentMember) {
        this.currentMember  = currentMember;
        this.bookDAO        = new BookDAO();
        this.reservationDAO = new ReservationDAO();

        setLayout(new BorderLayout());
        setBackground(new Color(0xf4fae8));

        buildUI();
        loadBooks();
    }

    private void buildUI() {

        // ── Header ────────────────────────────────────────────────────────────
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        header.setBorder(new EmptyBorder(0, 0, 14, 0));

        JLabel title = new JLabel("📚 Browse Books");
        title.setFont(new Font("Segoe UI", Font.BOLD, 20));
        title.setForeground(new Color(0x2f3e1b));
        header.add(title, BorderLayout.WEST);

        JPanel searchBar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        searchBar.setOpaque(false);

        tfSearch = new JTextField(20);
        tfSearch.setToolTipText("Search by ID, Title, Author, Category, or Publisher");
        tfSearch.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(0x8aab3c), 1, true),
            new EmptyBorder(5, 10, 5, 10)
        ));
        tfSearch.addKeyListener(new KeyAdapter() {
            @Override public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) applySearch();
            }
        });

        filterCombo = new JComboBox<>(new String[]{"All", "ID", "Title", "Author", "Category", "Publisher"});

        btnSearch  = new JButton("🔍 Search");
        btnRefresh = new JButton("🔄 Show All");

        btnSearch .addActionListener(e -> applySearch());
        btnRefresh.addActionListener(e -> {
            tfSearch.setText("");
            sorter.setRowFilter(null);
        });

        searchBar.add(new JLabel("Search:"));
        searchBar.add(tfSearch);
        searchBar.add(filterCombo);
        searchBar.add(btnSearch);
        searchBar.add(btnRefresh);

        header.add(searchBar, BorderLayout.EAST);
        add(header, BorderLayout.NORTH);

        // ── Table ─────────────────────────────────────────────────────────────
        // IMPORTANT: column order here must stay in sync with COL_* constants above
        String[] cols = {"ID", "Title", "Author(s)", "Category",
                         "Publisher", "Availability"};

        tableModel = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };

        table = new JTable(tableModel);
        table.setRowHeight(28);

        // Row sorter — enables client-side filtering
        sorter = new TableRowSorter<>(tableModel);
        table.setRowSorter(sorter);

        // Header renderer
        table.getTableHeader().setPreferredSize(new Dimension(0, 36));
        table.getTableHeader().setDefaultRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object val,
                    boolean sel, boolean foc, int r, int c) {
                JLabel lbl = new JLabel(val == null ? "" : val.toString());
                lbl.setFont(new Font("Segoe UI", Font.BOLD, 13));
                lbl.setForeground(Color.BLACK);
                lbl.setBackground(new Color(0xd6eaa0));
                lbl.setOpaque(true);
                lbl.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createMatteBorder(0, 0, 2, 1, new Color(0x8aab3c)),
                    BorderFactory.createEmptyBorder(4, 8, 4, 8)
                ));
                return lbl;
            }
        });

        // Row renderer
        table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object val,
                    boolean sel, boolean foc, int r, int c) {
                Component comp = super.getTableCellRendererComponent(t, val, sel, foc, r, c);
                ((JComponent) comp).setOpaque(true);
                if (!sel) {
                    comp.setBackground(r % 2 == 0 ? Color.WHITE : new Color(0xf4fae8));
                    comp.setForeground(new Color(0x303c1b));
                }
                return comp;
            }
        });

        table.setGridColor(new Color(0xd4e6a0));
        table.setShowGrid(true);

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(BorderFactory.createLineBorder(new Color(0x8aab3c), 1));
        scroll.getViewport().setBackground(Color.WHITE);
        add(scroll, BorderLayout.CENTER);
    }

    // ── Load all books once (called only on init / refresh) ──────────────────
    private void loadBooks() {
        tableModel.setRowCount(0);
        sorter.setRowFilter(null);

        new SwingWorker<List<Book>, Void>() {
            @Override
            protected List<Book> doInBackground() {
                return bookDAO.getAllBooks();
            }

            @Override
            protected void done() {
                try {
                    for (Book b : get()) {
                        String authors = "";
                        if (b.getAuthors() != null) {
                            authors = b.getAuthors().stream()
                                .map(a -> a.getFullName())
                                .collect(Collectors.joining(", "));
                        }
                        tableModel.addRow(new Object[]{
                            b.getBookID(),        // COL_ID           = 0
                            b.getTitle(),         // COL_TITLE        = 1
                            authors,              // COL_AUTHORS      = 2
                            b.getCategoryName(),  // COL_CATEGORY     = 3
                            b.getPublisherName(), // COL_PUBLISHER    = 4
                            b.getAvailableCopies() + " / " + b.getTotalCopies() // COL_AVAILABILITY = 5
                        });
                    }
                    // Re-apply search if user typed something before data loaded
                    applySearch();
                } catch (Exception e) {
                    e.printStackTrace();
                    JOptionPane.showMessageDialog(BrowseBooksPanel.this,
                        "Error loading books.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        }.execute();
    }

    // ── Client-side filter — no DAO call needed ───────────────────────────────
    private void applySearch() {
        String kw = tfSearch.getText().trim();
        if (kw.isEmpty()) {
            sorter.setRowFilter(null);
            return;
        }

        String selected = (String) filterCombo.getSelectedItem();
        int[] cols;
        switch (selected == null ? "All" : selected) {
            case "ID":        cols = new int[]{ COL_ID };         break;
            case "Title":     cols = new int[]{ COL_TITLE };      break;
            case "Author":    cols = new int[]{ COL_AUTHORS };    break;
            case "Category":  cols = new int[]{ COL_CATEGORY };   break;
            case "Publisher": cols = new int[]{ COL_PUBLISHER };  break;
            default:          cols = new int[]{ COL_ID, COL_TITLE, COL_AUTHORS,
                                                COL_CATEGORY, COL_PUBLISHER };
        }

        sorter.setRowFilter(RowFilter.regexFilter("(?i)" + kw, cols));
    }
}