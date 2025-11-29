
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

public class LibraryAndVendingSystem {

    enum Role { READER, LIBRARIAN, ADMIN }

    static abstract class User {
        protected final int id;
        protected String name;
        protected String email;
        protected Role role;

        public User(int id, String name, String email, Role role) {
            this.id = id; this.name = name; this.email = email; this.role = role;
        }

        public int getId() { return id; }
        public String getName() { return name; }
        public String getEmail() { return email; }
        public Role getRole() { return role; }
    }

    static class Reader extends User {
        private final List<Reservation> reservationHistory = new ArrayList<>();

        public Reader(int id, String name, String email) {
            super(id, name, email, Role.READER);
        }

        public void addReservationToHistory(Reservation r) { reservationHistory.add(r); }
        public List<Reservation> getReservationHistory() { return Collections.unmodifiableList(reservationHistory); }
    }

    static class Librarian extends User {
        public Librarian(int id, String name, String email) {
            super(id, name, email, Role.LIBRARIAN);
        }
    }

    static class Administrator extends User {
        public Administrator(int id, String name, String email) {
            super(id, name, email, Role.ADMIN);
        }
    }

    static class Book {
        private final int id;
        private String title;
        private String author;
        private String genre;
        private int totalCopies;

        public Book(int id, String title, String author, String genre, int totalCopies) {
            this.id = id; this.title = title; this.author = author; this.genre = genre; this.totalCopies = totalCopies;
        }

        public int getId() { return id; }
        public String getTitle() { return title; }
        public String getAuthor() { return author; }
        public String getGenre() { return genre; }
        public int getTotalCopies() { return totalCopies; }
        public void setTitle(String t) { this.title = t; }
        public void setAuthor(String a) { this.author = a; }
        public void setGenre(String g) { this.genre = g; }
        public void setTotalCopies(int c) { this.totalCopies = c; }

        @Override
        public String toString() {
            return String.format("Book(id=%d, title='%s', author='%s', genre='%s', copies=%d)", id, title, author, genre, totalCopies);
        }
    }

    static class Branch {
        private final int id;
        private String name;
        private String address;

        public Branch(int id, String name, String address) {
            this.id = id; this.name = name; this.address = address;
        }

        public int getId() { return id; }
        public String getName() { return name; }
        public String getAddress() { return address; }

        @Override
        public String toString() {
            return String.format("Branch(id=%d, name='%s')", id, name);
        }
    }

    static class Reservation {
        private final int id;
        private final Reader reader;
        private final Book book;
        private final Branch branch;
        private final LocalDateTime createdAt;
        private boolean active;

        public Reservation(int id, Reader reader, Book book, Branch branch) {
            this.id = id; this.reader = reader; this.book = book; this.branch = branch;
            this.createdAt = LocalDateTime.now(); this.active = true;
        }

        public int getId() { return id; }
        public Reader getReader() { return reader; }
        public Book getBook() { return book; }
        public Branch getBranch() { return branch; }
        public LocalDateTime getCreatedAt() { return createdAt; }
        public boolean isActive() { return active; }
        public void cancel() { this.active = false; }

        @Override
        public String toString() {
            return String.format("Reservation(id=%d, reader=%s, book=%s, branch=%s, active=%s)", id, reader.getName(), book.getTitle(), branch.getName(), active);
        }
    }

    static class LibraryRepository {
        private final Map<Integer, Book> books = new HashMap<>();
        private final Map<Integer, Branch> branches = new HashMap<>();
        private final Map<Integer, User> users = new HashMap<>();
        private final Map<Integer, Reservation> reservations = new HashMap<>();

        private int nextBookId = 100;
        private int nextBranchId = 10;
        private int nextUserId = 1;
        private int nextReservationId = 1000;

        public Reader registerReader(String name, String email) {
            int id = nextUserId++;
            Reader r = new Reader(id, name, email);
            users.put(id, r);
            return r;
        }
        public Librarian registerLibrarian(String name, String email) {
            int id = nextUserId++;
            Librarian l = new Librarian(id, name, email);
            users.put(id, l);
            return l;
        }
        public Administrator registerAdmin(String name, String email) {
            int id = nextUserId++;
            Administrator a = new Administrator(id, name, email);
            users.put(id, a);
            return a;
        }
        public User getUser(int id) { return users.get(id); }

        public Book addBook(String title, String author, String genre, int copies) {
            int id = nextBookId++;
            Book b = new Book(id, title, author, genre, copies);
            books.put(id, b);
            return b;
        }
        public boolean removeBook(int bookId) {
            return books.remove(bookId) != null;
        }
        public Book getBook(int id) { return books.get(id); }
        public List<Book> findBooks(String title, String author, String genre) {
            return books.values().stream().filter(b ->
                    (title == null || b.getTitle().toLowerCase().contains(title.toLowerCase()))
                            && (author == null || b.getAuthor().toLowerCase().contains(author.toLowerCase()))
                            && (genre == null || b.getGenre().toLowerCase().contains(genre.toLowerCase()))
            ).collect(Collectors.toList());
        }

        public Branch addBranch(String name, String address) {
            int id = nextBranchId++;
            Branch br = new Branch(id, name, address);
            branches.put(id, br);
            return br;
        }
        public boolean removeBranch(int branchId) {
            return branches.remove(branchId) != null;
        }
        public Branch getBranch(int id) { return branches.get(id); }
        public Collection<Branch> getAllBranches() { return branches.values(); }

        public Reservation createReservation(Reader reader, Book book, Branch branch) {
            int id = nextReservationId++;
            Reservation r = new Reservation(id, reader, book, branch);
            reservations.put(id, r);
            // decrease book copy count for simplicity
            book.setTotalCopies(Math.max(0, book.getTotalCopies() - 1));
            reader.addReservationToHistory(r);
            return r;
        }
        public boolean cancelReservation(int reservationId) {
            Reservation r = reservations.get(reservationId);
            if (r == null || !r.isActive()) return false;
            r.cancel();
            // return book copy
            r.getBook().setTotalCopies(r.getBook().getTotalCopies() + 1);
            return true;
        }
        public List<Reservation> getActiveReservations() {
            return reservations.values().stream().filter(Reservation::isActive).collect(Collectors.toList());
        }
        public Collection<Book> allBooks() { return books.values(); }
        public Collection<User> allUsers() { return users.values(); }

        // Analytics helpers
        public long totalIssuedBooks() {
            // For demo, count reservations ever created
            return reservations.size();
        }
        public List<Book> mostPopularBooks(int limit) {
            // naive: return books sorted by total copies originally (not accurate), demo purposes
            return books.values().stream().sorted(Comparator.comparingInt(Book::getTotalCopies)).limit(limit).collect(Collectors.toList());
        }
    }

    // Services that implement use-case logic (could be controllers in real app)
    static class LibraryService {
        private final LibraryRepository repo;

        public LibraryService(LibraryRepository repo) { this.repo = repo; }

        // Registration
        public Reader registerReader(String name, String email) {
            Reader r = repo.registerReader(name, email);
            System.out.println("Reader registered: " + r.getName() + " (id=" + r.getId() + ")");
            return r;
        }

        // Search books
        public List<Book> searchBooks(String title, String author, String genre) {
            List<Book> res = repo.findBooks(title, author, genre);
            System.out.println("Search result (" + res.size() + "):");
            for (Book b : res) System.out.println("  " + b);
            return res;
        }

        // Book reservation
        public Reservation reserveBook(Reader reader, int bookId, int branchId) {
            Book book = repo.getBook(bookId);
            Branch branch = repo.getBranch(branchId);
            if (book == null) {
                System.out.println("Book not found id=" + bookId);
                return null;
            }
            if (branch == null) {
                System.out.println("Branch not found id=" + branchId);
                return null;
            }
            if (book.getTotalCopies() <= 0) {
                System.out.println("No copies available for book " + book.getTitle());
                return null;
            }
            Reservation r = repo.createReservation(reader, book, branch);
            System.out.println("Reservation created: " + r);
            return r;
        }

        public boolean cancelReservation(int reservationId) {
            boolean ok = repo.cancelReservation(reservationId);
            System.out.println("Cancel reservation " + reservationId + " -> " + ok);
            return ok;
        }

        // Librarian functions
        public Book addBook(Librarian librarian, String title, String author, String genre, int copies) {
            Book b = repo.addBook(title, author, genre, copies);
            System.out.println("Librarian " + librarian.getName() + " added " + b);
            return b;
        }
        public boolean removeBook(Librarian librarian, int bookId) {
            boolean ok = repo.removeBook(bookId);
            System.out.println("Librarian " + librarian.getName() + " removed book " + bookId + " -> " + ok);
            return ok;
        }
        public void showActiveReservations(Librarian librarian) {
            System.out.println("Active reservations:");
            for (Reservation r : repo.getActiveReservations()) {
                System.out.println("  " + r);
            }
        }

        // Admin functions
        public Branch addBranch(Administrator admin, String name, String address) {
            Branch b = repo.addBranch(name, address);
            System.out.println("Admin " + admin.getName() + " added branch " + b);
            return b;
        }
        public boolean removeBranch(Administrator admin, int branchId) {
            boolean ok = repo.removeBranch(branchId);
            System.out.println("Admin " + admin.getName() + " removed branch " + branchId + " -> " + ok);
            return ok;
        }
        public void showAnalytics(Administrator admin) {
            System.out.println("Analytics for admin " + admin.getName() + ":");
            System.out.println("  total reservations ever: " + repo.totalIssuedBooks());
            // demo top books
            System.out.println("  all books:");
            for (Book b : repo.allBooks()) System.out.println("    " + b);
        }

        // Reader view history
        public void showReaderHistory(Reader r) {
            System.out.println("History for reader " + r.getName() + ":");
            for (Reservation res : r.getReservationHistory()) System.out.println("  " + res);
        }
    }

    // ---------------------------
    // PART B — Ticket Vending Machine (State Pattern)
    // ---------------------------

    // Vending state interface
    interface VendingState {
        void selectTicket(String ticketType);
        void insertMoney(double amount);
        void cancel();
        void dispense();
    }

    // Ticket Vending Machine
    static class TicketVendingMachine {
        // States
        private final VendingState idleState = new IdleState(this);
        private final VendingState waitingForMoneyState = new WaitingForMoneyState(this);
        private final VendingState moneyReceivedState = new MoneyReceivedState(this);
        private final VendingState ticketDispensedState = new TicketDispensedState(this);
        private final VendingState canceledState = new TransactionCanceledState(this);

        private VendingState currentState = idleState;
        private String selectedTicket = null;
        private double insertedAmount = 0.0;
        private final Map<String, Double> priceList = new HashMap<>();
        private int ticketsInMachine = 100;

        public TicketVendingMachine() {
            // sample ticket types
            priceList.put("Standard", 1.50);
            priceList.put("VIP", 3.00);
            priceList.put("Student", 0.75);
        }

        // state transitions
        void setState(VendingState s) { this.currentState = s; }
        VendingState getIdleState() { return idleState; }
        VendingState getWaitingForMoneyState() { return waitingForMoneyState; }
        VendingState getMoneyReceivedState() { return moneyReceivedState; }
        VendingState getTicketDispensedState() { return ticketDispensedState; }
        VendingState getCanceledState() { return canceledState; }

        // actions delegated to state
        public void selectTicketType(String type) { currentState.selectTicket(type); }
        public void insertMoney(double amount) { currentState.insertMoney(amount); }
        public void cancelTransaction() { currentState.cancel(); }
        public void dispenseTicket() { currentState.dispense(); }

        // helpers
        boolean hasTicketInStock() { return ticketsInMachine > 0; }
        void reduceTicket() { if (ticketsInMachine > 0) ticketsInMachine--; }
        double getPrice(String type) { return priceList.getOrDefault(type, Double.POSITIVE_INFINITY); }

        void resetSelection() {
            selectedTicket = null;
            insertedAmount = 0.0;
        }

        void setSelectedTicket(String t) { selectedTicket = t; }
        String getSelectedTicket() { return selectedTicket; }
        void addInsertedAmount(double a) { insertedAmount += a; }
        double getInsertedAmount() { return insertedAmount; }
        void returnChange() {
            double price = getPrice(selectedTicket);
            double change = insertedAmount - price;
            if (change > 0) {
                System.out.printf("Returning change: %.2f\n", change);
            }
        }
    }

    // Concrete states
    static class IdleState implements VendingState {
        private final TicketVendingMachine machine;
        public IdleState(TicketVendingMachine m) { this.machine = m; }

        @Override
        public void selectTicket(String ticketType) {
            System.out.println("Ticket selected: " + ticketType);
            if (!machine.hasTicketInStock()) {
                System.out.println("Machine: No tickets in stock.");
                return;
            }
            machine.setSelectedTicket(ticketType);
            machine.setState(machine.getWaitingForMoneyState());
        }

        @Override public void insertMoney(double amount) { System.out.println("Please select ticket first."); }
        @Override public void cancel() { System.out.println("Nothing to cancel."); }
        @Override public void dispense() { System.out.println("Select and pay first."); }
    }

    static class WaitingForMoneyState implements VendingState {
        private final TicketVendingMachine machine;
        public WaitingForMoneyState(TicketVendingMachine m) { this.machine = m; }

        @Override
        public void selectTicket(String ticketType) {
            System.out.println("Changing selection to: " + ticketType);
            machine.setSelectedTicket(ticketType);
        }

        @Override
        public void insertMoney(double amount) {
            machine.addInsertedAmount(amount);
            System.out.printf("Inserted %.2f, total inserted: %.2f\n", amount, machine.getInsertedAmount());
            double price = machine.getPrice(machine.getSelectedTicket());
            if (machine.getInsertedAmount() >= price) {
                machine.setState(machine.getMoneyReceivedState());
            }
        }

        @Override public void cancel() {
            System.out.println("Transaction canceled by user (while waiting for money). Returning to Idle.");
            machine.resetSelection();
            machine.setState(machine.getCanceledState());
        }

        @Override public void dispense() { System.out.println("Not enough money yet."); }
    }

    static class MoneyReceivedState implements VendingState {
        private final TicketVendingMachine machine;
        public MoneyReceivedState(TicketVendingMachine m) { this.machine = m; }

        @Override public void selectTicket(String ticketType) {
            System.out.println("Can't change selection after full payment (refund then select).");
        }

        @Override
        public void insertMoney(double amount) {
            machine.addInsertedAmount(amount);
            System.out.printf("Additional inserted %.2f, total: %.2f\n", amount, machine.getInsertedAmount());
        }

        @Override
        public void cancel() {
            System.out.println("Transaction canceled after receiving money. Returning funds.");
            machine.returnChange();
            machine.resetSelection();
            machine.setState(machine.getCanceledState());
        }

        @Override
        public void dispense() {
            // Dispense ticket
            if (!machine.hasTicketInStock()) {
                System.out.println("Cannot dispense: no tickets left.");
                return;
            }
            System.out.println("Dispensing ticket: " + machine.getSelectedTicket());
            machine.reduceTicket();
            machine.returnChange();
            machine.setState(machine.getTicketDispensedState());
        }
    }

    static class TicketDispensedState implements VendingState {
        private final TicketVendingMachine machine;
        public TicketDispensedState(TicketVendingMachine m) { this.machine = m; }

        @Override public void selectTicket(String ticketType) { System.out.println("Please wait. Transaction finishing."); }
        @Override public void insertMoney(double amount) { System.out.println("Please wait. Transaction finishing."); }
        @Override public void cancel() { System.out.println("Cannot cancel: ticket already dispensed."); }
        @Override public void dispense() {
            System.out.println("Transaction complete. Thank you!");
            machine.resetSelection();
            machine.setState(machine.getIdleState());
        }
    }

    static class TransactionCanceledState implements VendingState {
        private final TicketVendingMachine machine;
        public TransactionCanceledState(TicketVendingMachine m) { this.machine = m; }

        @Override public void selectTicket(String ticketType) {
            machine.setSelectedTicket(ticketType);
            machine.setState(machine.getWaitingForMoneyState());
        }
        @Override public void insertMoney(double amount) { System.out.println("Please select ticket first (canceled state)."); }
        @Override public void cancel() { System.out.println("Already canceled."); }
        @Override public void dispense() { System.out.println("Canceled; no dispense."); }
    }

    // ---------------------------
    // MAIN — демонстрация обох подсистем
    // ---------------------------
    public static void main(String[] args) {
        System.out.println("=== Library System Demo ===");
        LibraryRepository repo = new LibraryRepository();
        LibraryService service = new LibraryService(repo);

        // Admin and branches
        Administrator admin = repo.registerAdmin("SuperAdmin", "admin@lib.local");
        Branch br1 = service.addBranch(admin, "Central Library", "Main St. 1");
        Branch br2 = service.addBranch(admin, "East Branch", "East Ave. 5");

        // Librarian
        Librarian lib = repo.registerLibrarian("Alice", "alice@lib.local");

        // Add books
        Book b1 = service.addBook(lib, "Clean Code", "Robert C. Martin", "Programming", 3);
        Book b2 = service.addBook(lib, "The Hobbit", "J.R.R. Tolkien", "Fantasy", 2);
        Book b3 = service.addBook(lib, "Crime and Punishment", "F. Dostoevsky", "Classic", 1);

        // Reader registers and searches
        Reader reader = service.registerReader("Aida", "aida@example.com");
        service.searchBooks("hobbit", null, null);

        // Reserve a book
        Reservation r1 = service.reserveBook(reader, b2.getId(), br1.getId());
        // Attempt to reserve same book until copies exhausted
        Reservation r2 = service.reserveBook(reader, b2.getId(), br1.getId());
        Reservation r3 = service.reserveBook(reader, b2.getId(), br1.getId()); // may fail (no copies)

        // Reader views history
        service.showReaderHistory(reader);

        // Librarian views active reservations
        service.showActiveReservations(lib);

        // Cancel reservation
        if (r1 != null) service.cancelReservation(r1.getId());

        // Admin views analytics
        service.showAnalytics(admin);

        System.out.println("\n=== Ticket Vending Machine Demo ===");
        TicketVendingMachine machine = new TicketVendingMachine();

        // Scenario: normal purchase
        machine.selectTicketType("Standard");
        machine.insertMoney(1.0);
        machine.insertMoney(0.5); // enough now
        machine.dispenseTicket(); // attempt to dispense
        machine.dispenseTicket(); // finish and go to idle

        // Scenario: cancel while waiting
        machine.selectTicketType("VIP");
        machine.insertMoney(1.0);
        machine.cancelTransaction(); // cancel and return to idle
        machine.selectTicketType("VIP");
        machine.insertMoney(3.0);
        machine.dispenseTicket();
        machine.dispenseTicket();


        for (int i = 0; i < 98; i++) machine.selectTicketType("Standard"); // this will select then wait; to avoid complexity, just set ticketsInMachine low:

        System.out.println("\nDemo finished.");
    }
}
