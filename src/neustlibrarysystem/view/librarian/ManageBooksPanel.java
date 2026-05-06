package neustlibrarysystem.view.librarian;

import neustlibrarysystem.dao.BookDAO;
import neustlibrarysystem.model.Author;
import neustlibrarysystem.model.Book;
import neustlibrarysystem.model.Librarian;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;
import java.util.stream.Collectors;

public class ManageBooksPanel extends JPanel {

    private final BookDAO   bookDAO;
    private final Librarian librarian;

    private DefaultTableModel tableModel;
    private JTable            table;
    private JTextField        tfSearch;
    private JButton           btnSearch, btnRefresh, btnAdd, btnEdit, btnDeact;

    public ManageBooksPanel(Librarian librarian) {
        this.librarian = librarian;
        this.bookDAO   = new BookDAO();
        setLayout(new BorderLayout(0, 0));
        setBackground(LibrarianDashboard.CLR_BG);
        buildUI();
        loadBooks();
    }

    private void buildUI() {

        // ── Header ────────────────────────────────────────────────────────────
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(LibrarianDashboard.CLR_BG);
        header.setBorder(new EmptyBorder(0, 0, 14, 0));

        JLabel hdr = new JLabel("📖   Manage Books");
        hdr.setFont(LibrarianDashboard.FONT_HEADER);
        hdr.setForeground(LibrarianDashboard.CLR_PRIMARY);
        header.add(hdr, BorderLayout.WEST);

        JPanel bar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        bar.setOpaque(false);

        JLabel searchLbl = new JLabel("Search:");
        searchLbl.setFont(LibrarianDashboard.FONT_LABEL);
        searchLbl.setForeground(new Color(0x303c1b));

        tfSearch = new JTextField(22);
        tfSearch.setFont(LibrarianDashboard.FONT_BODY);
        tfSearch.setForeground(new Color(0x303c1b));
        tfSearch.setBackground(Color.WHITE);
        tfSearch.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(LibrarianDashboard.CLR_ACCENT, 1, true),
            new EmptyBorder(5, 10, 5, 10)
        ));

        btnSearch  = LibrarianDashboard.primaryBtn("🔍  Search");
        btnRefresh = LibrarianDashboard.accentBtn("🔄  Refresh");
        btnSearch .addActionListener(e -> searchBooks());
        btnRefresh.addActionListener(e -> { tfSearch.setText(""); loadBooks(); });

        bar.add(searchLbl); bar.add(tfSearch);
        bar.add(btnSearch); bar.add(btnRefresh);
        header.add(bar, BorderLayout.EAST);
        add(header, BorderLayout.NORTH);

        // ── Table ─────────────────────────────────────────────────────────────
        String[] cols = {"ID", "Title", "Author(s)", "Category", "Publisher",
                         "ISBN", "Total", "Available", "Shelf", "Status"};
        tableModel = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        table = new JTable(tableModel);
        LibrarianDashboard.styleTable(table);

        // FIX 1: Custom header renderer — guaranteed black text, no override possible
        table.getTableHeader().setPreferredSize(new Dimension(0, 36));
        table.getTableHeader().setDefaultRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object val,
                    boolean sel, boolean foc, int r, int c) {
                JLabel lbl = new JLabel(val == null ? "" : val.toString());
                lbl.setFont(LibrarianDashboard.FONT_LABEL);
                lbl.setForeground(Color.BLACK);
                lbl.setBackground(new Color(0xd6eaa0));
                lbl.setOpaque(true);
                lbl.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createMatteBorder(0, 0, 2, 1, new Color(0x8aab3c)),
                    BorderFactory.createEmptyBorder(4, 8, 4, 8)
                ));
                lbl.setHorizontalAlignment(SwingConstants.LEFT);
                return lbl;
            }
        });

        // FIX 2: setOpaque(true) on cell renderer so row colors actually paint
        table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object val,
                    boolean sel, boolean foc, int r, int c) {
                Component comp = super.getTableCellRendererComponent(t, val, sel, foc, r, c);
                ((JComponent) comp).setOpaque(true); // ← KEY FIX
                if (!sel) {
                    String status = (String) tableModel.getValueAt(r, 9);
                    if ("Inactive".equals(status)) {
                        comp.setBackground(new Color(0xf5e6e6));
                        comp.setForeground(new Color(0xaaaaaa));
                    } else {
                        comp.setBackground(r % 2 == 0 ? Color.WHITE : new Color(0xf4fae8));
                        comp.setForeground(new Color(0x303c1b));
                    }
                }
                return comp;
            }
        });

        table.getColumnModel().getColumn(0).setMaxWidth(50);
        table.getColumnModel().getColumn(6).setMaxWidth(55);
        table.getColumnModel().getColumn(7).setMaxWidth(75);
        table.getColumnModel().getColumn(9).setMaxWidth(80);

        // FIX 3: Make sure row height is tall enough to see text
        table.setRowHeight(28);
        table.setShowGrid(true);
        table.setGridColor(new Color(0xd4e6a0));

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(BorderFactory.createLineBorder(LibrarianDashboard.CLR_ACCENT, 1));
        scroll.getViewport().setBackground(Color.WHITE);
        add(scroll, BorderLayout.CENTER);

        // ── Action bar ────────────────────────────────────────────────────────
        JPanel south = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 10));
        south.setOpaque(false);

        btnAdd   = LibrarianDashboard.primaryBtn("➕  Add Book");
        btnEdit  = LibrarianDashboard.accentBtn("✏️  Edit Book");
        btnDeact = LibrarianDashboard.dangerBtn("🚫  Deactivate");

        btnAdd  .addActionListener(e -> showAddEditDialog(null));
        btnEdit .addActionListener(e -> editSelected());
        btnDeact.addActionListener(e -> deactivateSelected());

        south.add(btnAdd); south.add(btnEdit); south.add(btnDeact);
        add(south, BorderLayout.SOUTH);
    }

    public void refresh() { loadBooks(); }

    // ── SWINGWORKER: loadBooks ────────────────────────────────────────────────
    private void loadBooks() {
        setButtonsEnabled(false);
        tableModel.setRowCount(0);

        new SwingWorker<List<Book>, Void>() {
            @Override
            protected List<Book> doInBackground() {
                return bookDAO.getAllBooks();
            }

            @Override
            protected void done() {
                try {
                    populate(get());
                } catch (Exception e) {
                    e.printStackTrace();
                    JOptionPane.showMessageDialog(ManageBooksPanel.this,
                        "Error loading books.", "Error", JOptionPane.ERROR_MESSAGE);
                } finally {
                    setButtonsEnabled(true);
                }
            }
        }.execute();
    }

    // ── SWINGWORKER: searchBooks ──────────────────────────────────────────────
    private void searchBooks() {
        String kw = tfSearch.getText().trim();
        if (kw.isEmpty()) { loadBooks(); return; }

        setButtonsEnabled(false);
        tableModel.setRowCount(0);

        new SwingWorker<List<Book>, Void>() {
            @Override
            protected List<Book> doInBackground() {
                return bookDAO.searchBooks(kw);
            }

            @Override
            protected void done() {
                try {
                    populate(get());
                } catch (Exception e) {
                    e.printStackTrace();
                    JOptionPane.showMessageDialog(ManageBooksPanel.this,
                        "Error searching books.", "Error", JOptionPane.ERROR_MESSAGE);
                } finally {
                    setButtonsEnabled(true);
                }
            }
        }.execute();
    }

    private void populate(List<Book> books) {
        tableModel.setRowCount(0);
        for (Book b : books) {
            String authors = "";
            if (b.getAuthors() != null && !b.getAuthors().isEmpty()) {
                authors = b.getAuthors().stream()
                    .map(Author::getFullName)
                    .collect(Collectors.joining(", "));
            }
            tableModel.addRow(new Object[]{
                b.getBookID(),        b.getTitle(),     authors,
                b.getCategoryName(),  b.getPublisherName(), b.getIsbn(),
                b.getTotalCopies(),   b.getAvailableCopies(),
                b.getShelfLocation(), b.isActive() ? "Active" : "Inactive"
            });
        }
    }

    private void editSelected() {
        int row = table.getSelectedRow();
        if (row < 0) { warn("Select a book to edit."); return; }

        setButtonsEnabled(false);
        int bookID = (int) tableModel.getValueAt(row, 0);

        new SwingWorker<Book, Void>() {
            @Override
            protected Book doInBackground() {
                return bookDAO.getBookByID(bookID);
            }

            @Override
            protected void done() {
                try {
                    Book book = get();
                    if (book != null) showAddEditDialog(book);
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    setButtonsEnabled(true);
                }
            }
        }.execute();
    }

    private void showAddEditDialog(Book existing) {
        boolean isEdit   = (existing != null);
        String  dlgTitle = isEdit ? "Edit Book" : "Add New Book";

        JTextField tfTitle   = field(isEdit ? existing.getTitle()   : "");
        JTextField tfIsbn    = field(isEdit ? existing.getIsbn()    : "");
        JTextField tfCatID   = field(isEdit ? String.valueOf(existing.getCategoryID())     : "");
        JTextField tfPubID   = field(isEdit ? String.valueOf(existing.getPublisherID())    : "");
        JTextField tfYear    = field(isEdit ? String.valueOf(existing.getPublicationYear()): "");
        JTextField tfEdition = field(isEdit ? existing.getEdition()       : "");
        JTextField tfCopies  = field(isEdit ? String.valueOf(existing.getTotalCopies())   : "1");
        JTextField tfShelf   = field(isEdit ? existing.getShelfLocation() : "");
        JTextArea  taDesc    = new JTextArea(isEdit ? existing.getDescription() : "", 3, 22);
        taDesc.setFont(LibrarianDashboard.FONT_BODY);
        taDesc.setForeground(new Color(0x303c1b));
        taDesc.setBackground(Color.WHITE);
        taDesc.setLineWrap(true);
        taDesc.setWrapStyleWord(true);

        JPanel dlg = new JPanel(new GridBagLayout());
        dlg.setBackground(new Color(0xf4fae8));
        dlg.setBorder(new EmptyBorder(12, 14, 12, 14));
        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(6, 6, 6, 6);
        g.anchor = GridBagConstraints.WEST;

        Object[][] rows = {
            {"Title: *",        tfTitle},
            {"ISBN: *",         tfIsbn},
            {"Category ID: *",  tfCatID},
            {"Publisher ID: *", tfPubID},
            {"Year:",           tfYear},
            {"Edition:",        tfEdition},
            {"Total Copies: *", tfCopies},
            {"Shelf Location:", tfShelf},
            {"Description:",    new JScrollPane(taDesc)}
        };

        for (int i = 0; i < rows.length; i++) {
            g.gridx = 0; g.gridy = i; g.weightx = 0; g.fill = GridBagConstraints.NONE;
            JLabel lbl = new JLabel((String) rows[i][0]);
            lbl.setFont(LibrarianDashboard.FONT_LABEL);
            lbl.setForeground(new Color(0x303c1b));
            dlg.add(lbl, g);
            g.gridx = 1; g.weightx = 1; g.fill = GridBagConstraints.HORIZONTAL;
            dlg.add((Component) rows[i][1], g);
        }

        int result = JOptionPane.showConfirmDialog(this, dlg, dlgTitle,
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (result != JOptionPane.OK_OPTION) return;

        if (tfTitle.getText().trim().isEmpty() || tfIsbn.getText().trim().isEmpty()) {
            warn("Title and ISBN are required."); return;
        }

        try {
            Book b = isEdit ? existing : new Book();
            b.setTitle          (tfTitle  .getText().trim());
            b.setIsbn           (tfIsbn   .getText().trim());
            b.setCategoryID     (Integer.parseInt(tfCatID .getText().trim()));
            b.setPublisherID    (Integer.parseInt(tfPubID .getText().trim()));
            b.setPublicationYear(tfYear.getText().trim().isEmpty() ? 0
                                 : Integer.parseInt(tfYear.getText().trim()));
            b.setEdition        (tfEdition.getText().trim());
            b.setTotalCopies    (Integer.parseInt(tfCopies.getText().trim()));
            b.setShelfLocation  (tfShelf  .getText().trim());
            b.setDescription    (taDesc   .getText().trim());

            setButtonsEnabled(false);
            final boolean editing = isEdit;

            new SwingWorker<Boolean, Void>() {
                @Override
                protected Boolean doInBackground() {
                    return editing ? bookDAO.updateBook(b) : bookDAO.addBook(b);
                }

                @Override
                protected void done() {
                    try {
                        if (get()) {
                            JOptionPane.showMessageDialog(ManageBooksPanel.this,
                                "✔  Book " + (editing ? "updated" : "added") + " successfully!",
                                "Success", JOptionPane.INFORMATION_MESSAGE);
                            loadBooks();
                        } else {
                            JOptionPane.showMessageDialog(ManageBooksPanel.this,
                                "Operation failed.", "Error", JOptionPane.ERROR_MESSAGE);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    } finally {
                        setButtonsEnabled(true);
                    }
                }
            }.execute();

        } catch (NumberFormatException ex) {
            warn("Category ID, Publisher ID, Year, and Copies must be valid numbers.");
        }
    }

    // ── SWINGWORKER: deactivateSelected ──────────────────────────────────────
    private void deactivateSelected() {
        int row = table.getSelectedRow();
        if (row < 0) { warn("Select a book to deactivate."); return; }
        if ("Inactive".equals(tableModel.getValueAt(row, 9))) {
            JOptionPane.showMessageDialog(this, "Book is already inactive."); return;
        }

        int    id    = (int)    tableModel.getValueAt(row, 0);
        String title = (String) tableModel.getValueAt(row, 1);

        if (JOptionPane.showConfirmDialog(this,
                "Deactivate \"" + title + "\"?", "Confirm",
                JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE)
                != JOptionPane.YES_OPTION) return;

        setButtonsEnabled(false);

        new SwingWorker<Boolean, Void>() {
            @Override
            protected Boolean doInBackground() {
                return bookDAO.deactivateBook(id);
            }

            @Override
            protected void done() {
                try {
                    if (get()) {
                        JOptionPane.showMessageDialog(ManageBooksPanel.this,
                            "✔  Book deactivated.", "Success", JOptionPane.INFORMATION_MESSAGE);
                        loadBooks();
                    } else {
                        JOptionPane.showMessageDialog(ManageBooksPanel.this,
                            "Failed.", "Error", JOptionPane.ERROR_MESSAGE);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    setButtonsEnabled(true);
                }
            }
        }.execute();
    }

    private JTextField field(String val) {
        JTextField tf = new JTextField(val, 22);
        tf.setFont(LibrarianDashboard.FONT_BODY);
        tf.setForeground(new Color(0x303c1b));
        tf.setBackground(Color.WHITE);
        tf.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(LibrarianDashboard.CLR_ACCENT, 1),
            new EmptyBorder(4, 8, 4, 8)
        ));
        return tf;
    }

    private void warn(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Warning", JOptionPane.WARNING_MESSAGE);
    }

    private void setButtonsEnabled(boolean enabled) {
        btnSearch .setEnabled(enabled);
        btnRefresh.setEnabled(enabled);
        btnAdd    .setEnabled(enabled);
        btnEdit   .setEnabled(enabled);
        btnDeact  .setEnabled(enabled);
    }
}