package neustlibrarysystem.view.common;

import neustlibrarysystem.dao.BookDAO;
import neustlibrarysystem.dao.ReservationDAO;
import neustlibrarysystem.model.Book;
import neustlibrarysystem.model.Member;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;
import java.util.stream.Collectors;

public class BrowseBooksPanel extends JPanel {

    private final Member currentMember;
    private final BookDAO bookDAO;
    private final ReservationDAO reservationDAO;

    private JTable table;
    private DefaultTableModel tableModel;
    private JTextField tfSearch;
    private JComboBox<String> filterCombo;

    private JButton btnSearch, btnRefresh;

    public BrowseBooksPanel(Member currentMember) {
        this.currentMember = currentMember;
        this.bookDAO = new BookDAO();
        this.reservationDAO = new ReservationDAO();

        setLayout(new BorderLayout());
        setBackground(new Color(0xf4fae8));

        buildUI();
        loadBooks(null);
    }

    private void buildUI() {

        // ===== HEADER =====
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
        tfSearch.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(0x8aab3c), 1, true),
                new EmptyBorder(5, 10, 5, 10)
        ));

        filterCombo = new JComboBox<>(new String[]{"All", "Title", "Author", "ISBN", "Category"});

        btnSearch = new JButton("🔍 Search");
        btnRefresh = new JButton("🔄 Refresh");

        btnSearch.addActionListener(e -> searchBooks());
        btnRefresh.addActionListener(e -> {
            tfSearch.setText("");
            loadBooks(null);
        });

        searchBar.add(new JLabel("Search:"));
        searchBar.add(tfSearch);
        searchBar.add(filterCombo);
        searchBar.add(btnSearch);
        searchBar.add(btnRefresh);

        header.add(searchBar, BorderLayout.EAST);

        add(header, BorderLayout.NORTH);

        // ===== TABLE =====
        String[] cols = {"ID", "ISBN", "Title", "Author(s)", "Category",
                "Publisher", "Year", "Available", "Total"};

        tableModel = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };

        table = new JTable(tableModel);
        table.setRowHeight(28);

        // HEADER STYLE (same as ManageBooks)
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

        // ROW STYLE (alternate + soft green)
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

    // ===== LOAD BOOKS =====
    private void loadBooks(String keyword) {
        List<Book> books;

        try {
            if (keyword == null || keyword.isEmpty()) {
                books = bookDAO.getAllBooks();
            } else {
                books = bookDAO.searchBooks(keyword);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }

        tableModel.setRowCount(0);

        for (Book b : books) {

            String authors = "";
            if (b.getAuthors() != null) {
                authors = b.getAuthors().stream()
                        .map(a -> a.getFullName())
                        .collect(Collectors.joining(", "));
            }

            tableModel.addRow(new Object[]{
                    b.getBookID(),
                    b.getIsbn(),
                    b.getTitle(),
                    authors,
                    b.getCategoryName(),
                    b.getPublisherName(),
                    b.getPublicationYear(),
                    b.getAvailableCopies(),
                    b.getTotalCopies()
            });
        }
    }

    private void searchBooks() {
        loadBooks(tfSearch.getText().trim());
    }
}