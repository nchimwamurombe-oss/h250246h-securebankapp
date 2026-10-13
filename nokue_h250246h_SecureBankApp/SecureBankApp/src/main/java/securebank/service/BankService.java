package securebank.service;

import java.io.IOException;
import java.security.SecureRandom;
import java.util.*;
import securebank.model.*;
import securebank.security.*;
import securebank.storage.FileStore;

public class BankService {
    public static final int MAX_FAILED_ATTEMPTS = 3;
    public static final long LOCK_MILLIS = 60_000L;
    private static final byte[] DUMMY_SALT = new byte[16];
    private static final byte[] DUMMY_HASH = new byte[32];

    private final FileStore store;
    private final Map<String, User> users;
    private final Map<String, Account> accounts;
    private final List<Transaction> transactions;
    private final SecureRandom random = new SecureRandom();

    public BankService(FileStore store) {
        this.store = store;
        this.users = store.loadUsers();
        this.accounts = store.loadAccounts();
        this.transactions = store.loadTransactions();
    }

    public synchronized void register(String username, char[] password, char[] confirm) throws BankException {
        if (!Validator.isValidUsername(username))
            throw new BankException("Username must be 3-20 letters, digits or underscores.");
        String problem = Validator.passwordProblem(password);
        if (problem != null) throw new BankException(problem);
        if (!Arrays.equals(password, confirm)) throw new BankException("Passwords do not match.");
        if (users.containsKey(username.toLowerCase()))
            throw new BankException("That username is already taken.");

        byte[] salt = PasswordHasher.newSalt();
        byte[] hash = PasswordHasher.hash(password, salt, PasswordHasher.ITERATIONS);
        users.put(username.toLowerCase(),
                new User(username, salt, hash, PasswordHasher.ITERATIONS, 0, 0));
        persistUsers();
    }

    public synchronized User login(String username, char[] password) throws BankException {
        final String generic = "Invalid username or password.";
        User u = username == null ? null : users.get(username.trim().toLowerCase());
        if (u == null) {

            PasswordHasher.verify(password, DUMMY_SALT, DUMMY_HASH, PasswordHasher.ITERATIONS);
            throw new BankException(generic);
        }
        long now = System.currentTimeMillis();
        if (u.getLockedUntil() > now) {
            long secs = (u.getLockedUntil() - now + 999) / 1000;
            throw new BankException("Account temporarily locked. Try again in " + secs + "s.");
        }
        if (PasswordHasher.verify(password, u.getSalt(), u.getHash(), u.getIterations())) {
            u.setFailedAttempts(0);
            u.setLockedUntil(0);
            persistUsers();
            return u;
        }
        u.setFailedAttempts(u.getFailedAttempts() + 1);
        if (u.getFailedAttempts() >= MAX_FAILED_ATTEMPTS) {
            u.setLockedUntil(now + LOCK_MILLIS);
            u.setFailedAttempts(0);
            persistUsers();
            throw new BankException("Too many failed attempts. Locked for 60 seconds.");
        }
        persistUsers();
        throw new BankException(generic);
    }

    public synchronized Account createAccount(User user, Account.Type type, long openingCents)
            throws BankException {
        if (openingCents < 0 || openingCents > Validator.MAX_AMOUNT_CENTS)
            throw new BankException("Invalid opening deposit.");
        String no;
        do { no = String.format("%010d", 1_000_000_000L + (long) random.nextInt(900_000_000) * 10L + random.nextInt(10)); }
        while (accounts.containsKey(no));
        Account a = new Account(no, user.getUsername(), type, openingCents);
        accounts.put(no, a);
        if (openingCents > 0) record(a, Transaction.Type.OPENING, openingCents);
        persistAll();
        return a;
    }

    public synchronized List<Account> getAccounts(User user) {
        List<Account> list = new ArrayList<>();
        for (Account a : accounts.values())
            if (a.getOwner().equalsIgnoreCase(user.getUsername())) list.add(a);
        return list;
    }

    public synchronized long getBalance(User user, String accountNo) throws BankException {
        return owned(user, accountNo).getBalanceCents();
    }

    public synchronized void deposit(User user, String accountNo, long cents) throws BankException {
        Account a = owned(user, accountNo);
        checkAmount(cents);
        long newBal;
        try { newBal = Math.addExact(a.getBalanceCents(), cents); }
        catch (ArithmeticException e) { throw new BankException("Balance limit exceeded."); }
        a.setBalanceCents(newBal);
        record(a, Transaction.Type.DEPOSIT, cents);
        persistAll();
    }

    public synchronized void withdraw(User user, String accountNo, long cents) throws BankException {
        Account a = owned(user, accountNo);
        checkAmount(cents);
        if (cents > a.getBalanceCents()) throw new BankException("Insufficient funds.");
        a.setBalanceCents(a.getBalanceCents() - cents);
        record(a, Transaction.Type.WITHDRAWAL, cents);
        persistAll();
    }

    public synchronized List<Transaction> getTransactions(User user, String accountNo) throws BankException {
        owned(user, accountNo);
        List<Transaction> list = new ArrayList<>();
        for (Transaction t : transactions)
            if (t.getAccountNo().equals(accountNo)) list.add(t);
        Collections.reverse(list);
        return list;
    }

    private Account owned(User user, String accountNo) throws BankException {
        Account a = accounts.get(accountNo);
        if (a == null || !a.getOwner().equalsIgnoreCase(user.getUsername()))
            throw new BankException("Account not found.");
        return a;
    }

    private void checkAmount(long cents) throws BankException {
        if (cents <= 0 || cents > Validator.MAX_AMOUNT_CENTS)
            throw new BankException("Invalid amount.");
    }

    private void record(Account a, Transaction.Type type, long cents) {
        transactions.add(new Transaction(UUID.randomUUID().toString().substring(0, 8),
                a.getAccountNo(), type, cents, a.getBalanceCents(), System.currentTimeMillis()));
    }

    private void persistUsers() {
        try { store.saveUsers(users.values()); }
        catch (IOException e) { throw new RuntimeBankException(e); }
    }

    private void persistAll() {
        try {
            store.saveAccounts(accounts.values());
            store.saveTransactions(transactions);
        } catch (IOException e) { throw new RuntimeBankException(e); }
    }

    public static class RuntimeBankException extends RuntimeException {
        public RuntimeBankException(Throwable cause) { super("Storage error", cause); }
    }
}
