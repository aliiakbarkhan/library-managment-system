import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

class Book {
    private String isbn;
    private String title;
    private String author;
    private String category;
    private int totalCopies;
    private int availableCopies;
    private LocalDate dateAdded;

    public Book(String isbn, String title, String author, String category, int totalCopies) {
        this.isbn = isbn;
        this.title = title;
        this.author = author;
        this.category = category;
        this.totalCopies = totalCopies;
        this.availableCopies = totalCopies;
        this.dateAdded = LocalDate.now();
    }

    public String getIsbn() { return isbn; }
    public String getTitle() { return title; }
    public String getAuthor() { return author; }
    public String getCategory() { return category; }
    public int getTotalCopies() { return totalCopies; }
    public int getAvailableCopies() { return availableCopies; }
    public LocalDate getDateAdded() { return dateAdded; }

    public void setTitle(String title) { this.title = title; }
    public void setAuthor(String author) { this.author = author; }
    public void setCategory(String category) { this.category = category; }
    public void setTotalCopies(int totalCopies) { 
        this.totalCopies = totalCopies;
        if (availableCopies > totalCopies) {
            availableCopies = totalCopies;
        }
    }

    public boolean borrowBook() {
        if (availableCopies > 0) {
            availableCopies--;
            return true;
        }
        return false;
    }

    public void returnBook() {
        if (availableCopies < totalCopies) {
            availableCopies++;
        }
    }

    public boolean isAvailable() {
        return availableCopies > 0;
    }
}

class BorrowRecord {
    private String isbn;
    private String title;
    private String borrowerName;
    private String borrowerEmail;
    private LocalDate borrowDate;
    private LocalDate dueDate;
    private LocalDate returnDate;
    private boolean isReturned;

    public BorrowRecord(String isbn, String title, String borrowerName, String borrowerEmail) {
        this.isbn = isbn;
        this.title = title;
        this.borrowerName = borrowerName;
        this.borrowerEmail = borrowerEmail;
        this.borrowDate = LocalDate.now();
        this.dueDate = borrowDate.plusDays(14);
        this.isReturned = false;
    }

    public String getIsbn() { return isbn; }
    public String getTitle() { return title; }
    public String getBorrowerName() { return borrowerName; }
    public String getBorrowerEmail() { return borrowerEmail; }
    public LocalDate getBorrowDate() { return borrowDate; }
    public LocalDate getDueDate() { return dueDate; }
    public LocalDate getReturnDate() { return returnDate; }
    public boolean isReturned() { return isReturned; }

    public void markReturned() {
        this.isReturned = true;
        this.returnDate = LocalDate.now();
    }

    public boolean isOverdue() {
        return !isReturned && LocalDate.now().isAfter(dueDate);
    }
}

class LibraryManager {
    private List<Book> books;
    private List<BorrowRecord> borrowRecords;

    public LibraryManager() {
        books = new ArrayList<>();
        borrowRecords = new ArrayList<>();
        initializeSampleData();
    }

    private void initializeSampleData() {
        books.add(new Book("978-0134685991", "Effective Java", "Joshua Bloch", "Programming", 3));
        books.add(new Book("978-0135166307", "Java: The Complete Reference", "Herbert Schildt", "Programming", 2));
        books.add(new Book("978-0132350884", "Clean Code", "Robert Martin", "Programming", 4));
        books.add(new Book("978-0596007126", "Head First Design Patterns", "Eric Freeman", "Programming", 2));
        books.add(new Book("978-1617294945", "Spring in Action", "Craig Walls", "Programming", 1));
    }

    public boolean addBook(Book book) {
        for (Book b : books) {
            if (b.getIsbn().equals(book.getIsbn())) {
                return false;
            }
        }
        books.add(book);
        return true;
    }

    public boolean removeBook(String isbn) {
        return books.removeIf(book -> book.getIsbn().equals(isbn));
    }

    public Book findBookByIsbn(String isbn) {
        return books.stream()
                .filter(book -> book.getIsbn().equals(isbn))
                .findFirst()
                .orElse(null);
    }

    public List<Book> searchBooks(String query) {
        List<Book> results = new ArrayList<>();
        String lowerQuery = query.toLowerCase();
        
        for (Book book : books) {
            if (book.getTitle().toLowerCase().contains(lowerQuery) ||
                book.getAuthor().toLowerCase().contains(lowerQuery) ||
                book.getIsbn().toLowerCase().contains(lowerQuery) ||
                book.getCategory().toLowerCase().contains(lowerQuery)) {
                results.add(book);
            }
        }
        return results;
    }

    public boolean borrowBook(String isbn, String borrowerName, String borrowerEmail) {
        Book book = findBookByIsbn(isbn);
        if (book != null && book.borrowBook()) {
            borrowRecords.add(new BorrowRecord(isbn, book.getTitle(), borrowerName, borrowerEmail));
            return true;
        }
        return false;
    }

    public boolean returnBook(String isbn, String borrowerName) {
        for (BorrowRecord record : borrowRecords) {
            if (record.getIsbn().equals(isbn) && 
                record.getBorrowerName().equals(borrowerName) && 
                !record.isReturned()) {
                record.markReturned();
                Book book = findBookByIsbn(isbn);
                if (book != null) {
                    book.returnBook();
                }
                return true;
            }
        }
        return false;
    }

    public List<Book> getAllBooks() {
        return new ArrayList<>(books);
    }

    public List<BorrowRecord> getAllBorrowRecords() {
        return new ArrayList<>(borrowRecords);
    }

    public List<BorrowRecord> getActiveBorrowRecords() {
        List<BorrowRecord> active = new ArrayList<>();
        for (BorrowRecord record : borrowRecords) {
            if (!record.isReturned()) {
                active.add(record);
            }
        }
        return active;
    }

    public List<BorrowRecord> getOverdueRecords() {
        List<BorrowRecord> overdue = new ArrayList<>();
        for (BorrowRecord record : borrowRecords) {
            if (record.isOverdue()) {
                overdue.add(record);
            }
        }
        return overdue;
    }
}

public class LibraryManagementSystem extends JFrame {
    private LibraryManager libraryManager;
    private JTabbedPane tabbedPane;
    private DefaultTableModel booksTableModel;
    private DefaultTableModel borrowRecordsTableModel;
    private JTable booksTable;
    private JTable borrowRecordsTable;

    public LibraryManagementSystem() {
        libraryManager = new LibraryManager();
        initializeGUI();
        refreshTables();
    }

    private void initializeGUI() {
        setTitle("Library Management System");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1000, 700);
        setLocationRelativeTo(null);

        tabbedPane = new JTabbedPane();
        
        tabbedPane.addTab("Books", createBooksPanel());
        tabbedPane.addTab("Borrow/Return", createBorrowReturnPanel());
        tabbedPane.addTab("Records", createRecordsPanel());
        tabbedPane.addTab("Reports", createReportsPanel());

        add(tabbedPane);
    }

    private JPanel createBooksPanel() {
        JPanel panel = new JPanel(new BorderLayout());

        String[] columnNames = {"ISBN", "Title", "Author", "Category", "Total Copies", "Available", "Date Added"};
        booksTableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        booksTable = new JTable(booksTableModel);
        booksTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        JScrollPane scrollPane = new JScrollPane(booksTable);
        panel.add(scrollPane, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new FlowLayout());
        
        JButton addButton = new JButton("Add Book");
        addButton.addActionListener(e -> showAddBookDialog());
        
        JButton editButton = new JButton("Edit Book");
        editButton.addActionListener(e -> showEditBookDialog());
        
        JButton deleteButton = new JButton("Delete Book");
        deleteButton.addActionListener(e -> deleteSelectedBook());
        
        JTextField searchField = new JTextField(20);
        JButton searchButton = new JButton("Search");
        searchButton.addActionListener(e -> searchBooks(searchField.getText()));

        buttonPanel.add(addButton);
        buttonPanel.add(editButton);
        buttonPanel.add(deleteButton);
        buttonPanel.add(new JLabel("Search:"));
        buttonPanel.add(searchField);
        buttonPanel.add(searchButton);

        panel.add(buttonPanel, BorderLayout.SOUTH);

        return panel;
    }

    private JPanel createBorrowReturnPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);

        JPanel borrowPanel = new JPanel(new GridBagLayout());
        borrowPanel.setBorder(BorderFactory.createTitledBorder("Borrow Book"));
        
        gbc.gridx = 0; gbc.gridy = 0;
        borrowPanel.add(new JLabel("ISBN:"), gbc);
        gbc.gridx = 1;
        JTextField borrowIsbnField = new JTextField(15);
        borrowPanel.add(borrowIsbnField, gbc);

        gbc.gridx = 0; gbc.gridy = 1;
        borrowPanel.add(new JLabel("Borrower Name:"), gbc);
        gbc.gridx = 1;
        JTextField borrowerNameField = new JTextField(15);
        borrowPanel.add(borrowerNameField, gbc);

        gbc.gridx = 0; gbc.gridy = 2;
        borrowPanel.add(new JLabel("Borrower Email:"), gbc);
        gbc.gridx = 1;
        JTextField borrowerEmailField = new JTextField(15);
        borrowPanel.add(borrowerEmailField, gbc);

        gbc.gridx = 1; gbc.gridy = 3;
        JButton borrowButton = new JButton("Borrow Book");
        borrowButton.addActionListener(e -> {
            String isbn = borrowIsbnField.getText().trim();
            String name = borrowerNameField.getText().trim();
            String email = borrowerEmailField.getText().trim();
            
            if (isbn.isEmpty() || name.isEmpty() || email.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Please fill all fields.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            if (libraryManager.borrowBook(isbn, name, email)) {
                JOptionPane.showMessageDialog(this, "Book borrowed successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                borrowIsbnField.setText("");
                borrowerNameField.setText("");
                borrowerEmailField.setText("");
                refreshTables();
            } else {
                JOptionPane.showMessageDialog(this, "Book not available or not found.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });
        borrowPanel.add(borrowButton, gbc);

        JPanel returnPanel = new JPanel(new GridBagLayout());
        returnPanel.setBorder(BorderFactory.createTitledBorder("Return Book"));
        
        gbc.gridx = 0; gbc.gridy = 0;
        returnPanel.add(new JLabel("ISBN:"), gbc);
        gbc.gridx = 1;
        JTextField returnIsbnField = new JTextField(15);
        returnPanel.add(returnIsbnField, gbc);

        gbc.gridx = 0; gbc.gridy = 1;
        returnPanel.add(new JLabel("Borrower Name:"), gbc);
        gbc.gridx = 1;
        JTextField returnNameField = new JTextField(15);
        returnPanel.add(returnNameField, gbc);

        gbc.gridx = 1; gbc.gridy = 2;
        JButton returnButton = new JButton("Return Book");
        returnButton.addActionListener(e -> {
            String isbn = returnIsbnField.getText().trim();
            String name = returnNameField.getText().trim();
            
            if (isbn.isEmpty() || name.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Please fill all fields.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            if (libraryManager.returnBook(isbn, name)) {
                JOptionPane.showMessageDialog(this, "Book returned successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                returnIsbnField.setText("");
                returnNameField.setText("");
                refreshTables();
            } else {
                JOptionPane.showMessageDialog(this, "No matching borrow record found.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });
        returnPanel.add(returnButton, gbc);

        gbc.gridx = 0; gbc.gridy = 0; gbc.anchor = GridBagConstraints.NORTH;
        panel.add(borrowPanel, gbc);
        gbc.gridx = 0; gbc.gridy = 1;
        panel.add(returnPanel, gbc);

        return panel;
    }

    private JPanel createRecordsPanel() {
        JPanel panel = new JPanel(new BorderLayout());

        String[] columnNames = {"ISBN", "Title", "Borrower", "Email", "Borrow Date", "Due Date", "Return Date", "Status"};
        borrowRecordsTableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        borrowRecordsTable = new JTable(borrowRecordsTableModel);

        JScrollPane scrollPane = new JScrollPane(borrowRecordsTable);
        panel.add(scrollPane, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new FlowLayout());
        
        JButton showAllButton = new JButton("Show All Records");
        showAllButton.addActionListener(e -> showAllRecords());
        
        JButton showActiveButton = new JButton("Show Active Records");
        showActiveButton.addActionListener(e -> showActiveRecords());
        
        JButton showOverdueButton = new JButton("Show Overdue Records");
        showOverdueButton.addActionListener(e -> showOverdueRecords());

        buttonPanel.add(showAllButton);
        buttonPanel.add(showActiveButton);
        buttonPanel.add(showOverdueButton);

        panel.add(buttonPanel, BorderLayout.SOUTH);

        return panel;
    }

    private JPanel createReportsPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);

        JTextArea reportArea = new JTextArea(20, 50);
        reportArea.setEditable(false);
        reportArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        JScrollPane scrollPane = new JScrollPane(reportArea);

        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 4;
        panel.add(scrollPane, gbc);

        JButton inventoryButton = new JButton("Inventory Report");
        inventoryButton.addActionListener(e -> {
            StringBuilder report = new StringBuilder();
            report.append("LIBRARY INVENTORY REPORT\n");
            report.append("========================\n\n");
            
            List<Book> books = libraryManager.getAllBooks();
            report.append(String.format("Total Books: %d\n\n", books.size()));
            
            for (Book book : books) {
                report.append(String.format("ISBN: %s\n", book.getIsbn()));
                report.append(String.format("Title: %s\n", book.getTitle()));
                report.append(String.format("Author: %s\n", book.getAuthor()));
                report.append(String.format("Category: %s\n", book.getCategory()));
                report.append(String.format("Total Copies: %d\n", book.getTotalCopies()));
                report.append(String.format("Available: %d\n", book.getAvailableCopies()));
                report.append("----------------------------------------\n");
            }
            
            reportArea.setText(report.toString());
        });

        JButton borrowingButton = new JButton("Borrowing Report");
        borrowingButton.addActionListener(e -> {
            StringBuilder report = new StringBuilder();
            report.append("BORROWING ACTIVITY REPORT\n");
            report.append("=========================\n\n");
            
            List<BorrowRecord> records = libraryManager.getAllBorrowRecords();
            List<BorrowRecord> active = libraryManager.getActiveBorrowRecords();
            List<BorrowRecord> overdue = libraryManager.getOverdueRecords();
            
            report.append(String.format("Total Borrowing Records: %d\n", records.size()));
            report.append(String.format("Active Borrows: %d\n", active.size()));
            report.append(String.format("Overdue Books: %d\n\n", overdue.size()));
            
            if (!overdue.isEmpty()) {
                report.append("OVERDUE BOOKS:\n");
                report.append("==============\n");
                for (BorrowRecord record : overdue) {
                    report.append(String.format("Title: %s\n", record.getTitle()));
                    report.append(String.format("Borrower: %s\n", record.getBorrowerName()));
                    report.append(String.format("Due Date: %s\n", record.getDueDate()));
                    report.append("----------------------------------------\n");
                }
            }
            
            reportArea.setText(report.toString());
        });

        gbc.gridx = 0; gbc.gridy = 1; gbc.gridwidth = 1;
        panel.add(inventoryButton, gbc);
        gbc.gridx = 1;
        panel.add(borrowingButton, gbc);

        return panel;
    }

    private void showAddBookDialog() {
        JDialog dialog = new JDialog(this, "Add New Book", true);
        dialog.setSize(400, 300);
        dialog.setLocationRelativeTo(this);

        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);

        JTextField isbnField = new JTextField(20);
        JTextField titleField = new JTextField(20);
        JTextField authorField = new JTextField(20);
        JTextField categoryField = new JTextField(20);
        JSpinner copiesSpinner = new JSpinner(new SpinnerNumberModel(1, 1, 100, 1));

        gbc.gridx = 0; gbc.gridy = 0; gbc.anchor = GridBagConstraints.WEST;
        panel.add(new JLabel("ISBN:"), gbc);
        gbc.gridx = 1;
        panel.add(isbnField, gbc);

        gbc.gridx = 0; gbc.gridy = 1;
        panel.add(new JLabel("Title:"), gbc);
        gbc.gridx = 1;
        panel.add(titleField, gbc);

        gbc.gridx = 0; gbc.gridy = 2;
        panel.add(new JLabel("Author:"), gbc);
        gbc.gridx = 1;
        panel.add(authorField, gbc);

        gbc.gridx = 0; gbc.gridy = 3;
        panel.add(new JLabel("Category:"), gbc);
        gbc.gridx = 1;
        panel.add(categoryField, gbc);

        gbc.gridx = 0; gbc.gridy = 4;
        panel.add(new JLabel("Copies:"), gbc);
        gbc.gridx = 1;
        panel.add(copiesSpinner, gbc);

        JPanel buttonPanel = new JPanel();
        JButton saveButton = new JButton("Save");
        JButton cancelButton = new JButton("Cancel");

        saveButton.addActionListener(e -> {
            String isbn = isbnField.getText().trim();
            String title = titleField.getText().trim();
            String author = authorField.getText().trim();
            String category = categoryField.getText().trim();
            int copies = (Integer) copiesSpinner.getValue();

            if (isbn.isEmpty() || title.isEmpty() || author.isEmpty() || category.isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "Please fill all fields.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            Book book = new Book(isbn, title, author, category, copies);
            if (libraryManager.addBook(book)) {
                JOptionPane.showMessageDialog(dialog, "Book added successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                dialog.dispose();
                refreshTables();
            } else {
                JOptionPane.showMessageDialog(dialog, "Book with this ISBN already exists.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        cancelButton.addActionListener(e -> dialog.dispose());

        buttonPanel.add(saveButton);
        buttonPanel.add(cancelButton);

        gbc.gridx = 0; gbc.gridy = 5; gbc.gridwidth = 2;
        panel.add(buttonPanel, gbc);

        dialog.add(panel);
        dialog.setVisible(true);
    }

    private void showEditBookDialog() {
        int selectedRow = booksTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a book to edit.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        String isbn = (String) booksTableModel.getValueAt(selectedRow, 0);
        Book book = libraryManager.findBookByIsbn(isbn);
        if (book == null) return;

        JDialog dialog = new JDialog(this, "Edit Book", true);
        dialog.setSize(400, 300);
        dialog.setLocationRelativeTo(this);

        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);

        JTextField isbnField = new JTextField(book.getIsbn(), 20);
        isbnField.setEditable(false);
        JTextField titleField = new JTextField(book.getTitle(), 20);
        JTextField authorField = new JTextField(book.getAuthor(), 20);
        JTextField categoryField = new JTextField(book.getCategory(), 20);
        JSpinner copiesSpinner = new JSpinner(new SpinnerNumberModel(book.getTotalCopies(), 1, 100, 1));

        gbc.gridx = 0; gbc.gridy = 0; gbc.anchor = GridBagConstraints.WEST;
        panel.add(new JLabel("ISBN:"), gbc);
        gbc.gridx = 1;
        panel.add(isbnField, gbc);

        gbc.gridx = 0; gbc.gridy = 1;
        panel.add(new JLabel("Title:"), gbc);
        gbc.gridx = 1;
        panel.add(titleField, gbc);

        gbc.gridx = 0; gbc.gridy = 2;
        panel.add(new JLabel("Author:"), gbc);
        gbc.gridx = 1;
        panel.add(authorField, gbc);

        gbc.gridx = 0; gbc.gridy = 3;
        panel.add(new JLabel("Category:"), gbc);
        gbc.gridx = 1;
        panel.add(categoryField, gbc);

        gbc.gridx = 0; gbc.gridy = 4;
        panel.add(new JLabel("Total Copies:"), gbc);
        gbc.gridx = 1;
        panel.add(copiesSpinner, gbc);

        JPanel buttonPanel = new JPanel();
        JButton saveButton = new JButton("Save");
        JButton cancelButton = new JButton("Cancel");

        saveButton.addActionListener(e -> {
            String title = titleField.getText().trim();
            String author = authorField.getText().trim();
            String category = categoryField.getText().trim();
            int copies = (Integer) copiesSpinner.getValue();

            if (title.isEmpty() || author.isEmpty() || category.isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "Please fill all fields.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            book.setTitle(title);
            book.setAuthor(author);
            book.setCategory(category);
            book.setTotalCopies(copies);

            JOptionPane.showMessageDialog(dialog, "Book updated successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
            dialog.dispose();
            refreshTables();
        });

        cancelButton.addActionListener(e -> dialog.dispose());

        buttonPanel.add(saveButton);
        buttonPanel.add(cancelButton);

        gbc.gridx = 0; gbc.gridy = 5; gbc.gridwidth = 2;
        panel.add(buttonPanel, gbc);

        dialog.add(panel);
        dialog.setVisible(true);
    }

    private void deleteSelectedBook() {
        int selectedRow = booksTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a book to delete.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        String isbn = (String) booksTableModel.getValueAt(selectedRow, 0);
        String title = (String) booksTableModel.getValueAt(selectedRow, 1);

        int confirm = JOptionPane.showConfirmDialog(this, 
            "Are you sure you want to delete '" + title + "'?", 
            "Confirm Delete", 
            JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            if (libraryManager.removeBook(isbn)) {
                JOptionPane.showMessageDialog(this, "Book deleted successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                refreshTables();
            }
        }
    }

    private void searchBooks(String query) {
        if (query.trim().isEmpty()) {
            refreshBooksTable();
            return;
        }

        List<Book> results = libraryManager.searchBooks(query);
        booksTableModel.setRowCount(0);
        
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        for (Book book : results) {
            Object[] row = {
                book.getIsbn(),
                book.getTitle(),
                book.getAuthor(),
                book.getCategory(),
                book.getTotalCopies(),
                book.getAvailableCopies(),
                book.getDateAdded().format(formatter)
            };
            booksTableModel.addRow(row);
        }
    }

    private void showAllRecords() {
        List<BorrowRecord> records = libraryManager.getAllBorrowRecords();
        updateBorrowRecordsTable(records);
    }

    private void showActiveRecords() {
        List<BorrowRecord> records = libraryManager.getActiveBorrowRecords();
        updateBorrowRecordsTable(records);
    }

    private void showOverdueRecords() {
        List<BorrowRecord> records = libraryManager.getOverdueRecords();
        updateBorrowRecordsTable(records);
    }

    private void updateBorrowRecordsTable(List<BorrowRecord> records) {
        borrowRecordsTableModel.setRowCount(0);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        
        for (BorrowRecord record : records) {
            Object[] row = {
                record.getIsbn(),
                record.getTitle(),
                record.getBorrowerName(),
                record.getBorrowerEmail(),
                record.getBorrowDate().format(formatter),
                record.getDueDate().format(formatter),
                record.getReturnDate() != null ? record.getReturnDate().format(formatter) : "",
                record.isReturned() ? "Returned" : (record.isOverdue() ? "Overdue" : "Active")
            };
            borrowRecordsTableModel.addRow(row);
        }
    }

    private void refreshTables() {
        refreshBooksTable();
        showAllRecords();
    }

    private void refreshBooksTable() {
        booksTableModel.setRowCount(0);
        List<Book> books = libraryManager.getAllBooks();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        
        for (Book book : books) {
            Object[] row = {
                book.getIsbn(),
                book.getTitle(),
                book.getAuthor(),
                book.getCategory(),
                book.getTotalCopies(),
                book.getAvailableCopies(),
                book.getDateAdded().format(formatter)
            };
            booksTableModel.addRow(row);
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.getSystemLookAndFeelClassName();
            } catch (Exception e) {
                e.printStackTrace();
            }
            
            new LibraryManagementSystem().setVisible(true);
        });
    }
}