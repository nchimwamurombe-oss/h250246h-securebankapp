package securebank.model;

public class User {
    private final String username;
    private final byte[] salt;
    private final byte[] hash;
    private final int iterations;
    private int failedAttempts;
    private long lockedUntil;

    public User(String username, byte[] salt, byte[] hash, int iterations,
                int failedAttempts, long lockedUntil) {
        this.username = username;
        this.salt = salt.clone();
        this.hash = hash.clone();
        this.iterations = iterations;
        this.failedAttempts = failedAttempts;
        this.lockedUntil = lockedUntil;
    }

    public String getUsername()   { return username; }
    public byte[] getSalt()       { return salt.clone(); }
    public byte[] getHash()       { return hash.clone(); }
    public int getIterations()    { return iterations; }
    public int getFailedAttempts(){ return failedAttempts; }
    public long getLockedUntil()  { return lockedUntil; }

    public void setFailedAttempts(int n) { this.failedAttempts = n; }
    public void setLockedUntil(long t)   { this.lockedUntil = t; }
}
