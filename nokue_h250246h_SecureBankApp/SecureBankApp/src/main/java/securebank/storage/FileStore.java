package securebank.storage;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;
import securebank.model.*;

public class FileStore {
    private final Path dir;
    private final Path usersFile, accountsFile, txFile;
    private static final Base64.Encoder B64E = Base64.getEncoder();
    private static final Base64.Decoder B64D = Base64.getDecoder();

    public FileStore(String dirName) throws IOException {
        this.dir = Paths.get(dirName);
        Files.createDirectories(dir);
        usersFile    = dir.resolve("users.txt");
        accountsFile = dir.resolve("accounts.txt");
        txFile       = dir.resolve("transactions.txt");
        for (Path p : new Path[]{usersFile, accountsFile, txFile})
            if (!Files.exists(p)) Files.createFile(p);
    }

    public Map<String, User> loadUsers() {
        Map<String, User> map = new LinkedHashMap<>();
        for (String[] f : readRows(usersFile, 6)) {
            try {
                User u = new User(f[0], B64D.decode(f[1]), B64D.decode(f[2]),
                        Integer.parseInt(f[3]), Integer.parseInt(f[4]), Long.parseLong(f[5]));
                map.put(u.getUsername().toLowerCase(), u);
            } catch (RuntimeException e) {
                System.err.println("Skipping corrupt user record.");
            }
        }
        return map;
    }

    public void saveUsers(Collection<User> users) throws IOException {
        List<String> lines = new ArrayList<>();
        for (User u : users)
            lines.add(String.join("|", u.getUsername(), B64E.encodeToString(u.getSalt()),
                    B64E.encodeToString(u.getHash()), String.valueOf(u.getIterations()),
                    String.valueOf(u.getFailedAttempts()), String.valueOf(u.getLockedUntil())));
        atomicWrite(usersFile, lines);
    }

    public Map<String, Account> loadAccounts() {
        Map<String, Account> map = new LinkedHashMap<>();
        for (String[] f : readRows(accountsFile, 4)) {
            try {
                Account a = new Account(f[0], f[1], Account.Type.valueOf(f[2]), Long.parseLong(f[3]));
                map.put(a.getAccountNo(), a);
            } catch (RuntimeException e) {
                System.err.println("Skipping corrupt account record.");
            }
        }
        return map;
    }

    public void saveAccounts(Collection<Account> accounts) throws IOException {
        List<String> lines = new ArrayList<>();
        for (Account a : accounts)
            lines.add(String.join("|", a.getAccountNo(), a.getOwner(), a.getType().name(),
                    String.valueOf(a.getBalanceCents())));
        atomicWrite(accountsFile, lines);
    }

    public List<Transaction> loadTransactions() {
        List<Transaction> list = new ArrayList<>();
        for (String[] f : readRows(txFile, 6)) {
            try {
                list.add(new Transaction(f[0], f[1], Transaction.Type.valueOf(f[2]),
                        Long.parseLong(f[3]), Long.parseLong(f[4]), Long.parseLong(f[5])));
            } catch (RuntimeException e) {
                System.err.println("Skipping corrupt transaction record.");
            }
        }
        return list;
    }

    public void saveTransactions(Collection<Transaction> txs) throws IOException {
        List<String> lines = new ArrayList<>();
        for (Transaction t : txs)
            lines.add(String.join("|", t.getId(), t.getAccountNo(), t.getType().name(),
                    String.valueOf(t.getAmountCents()), String.valueOf(t.getBalanceAfterCents()),
                    String.valueOf(t.getTimestamp())));
        atomicWrite(txFile, lines);
    }

    private List<String[]> readRows(Path p, int expectedCols) {
        List<String[]> rows = new ArrayList<>();
        try {
            for (String line : Files.readAllLines(p, StandardCharsets.UTF_8)) {
                if (line.isBlank()) continue;
                String[] f = line.split("\\|", -1);
                if (f.length == expectedCols) rows.add(f);
                else System.err.println("Skipping malformed line in " + p.getFileName());
            }
        } catch (IOException e) {
            System.err.println("Could not read " + p.getFileName());
        }
        return rows;
    }

    private void atomicWrite(Path target, List<String> lines) throws IOException {
        Path tmp = Files.createTempFile(dir, "bank", ".tmp");
        try {
            Files.write(tmp, lines, StandardCharsets.UTF_8);
            try {
                Files.move(tmp, target, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
            } catch (AtomicMoveNotSupportedException e) {
                Files.move(tmp, target, StandardCopyOption.REPLACE_EXISTING);
            }
        } finally {
            Files.deleteIfExists(tmp);
        }
    }
}
