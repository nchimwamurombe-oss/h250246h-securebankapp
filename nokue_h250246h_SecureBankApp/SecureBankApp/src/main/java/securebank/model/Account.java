package securebank.model;

public class Account {
    public enum Type { SAVINGS, CURRENT }

    private final String accountNo;
    private final String owner;
    private final Type type;
    private long balanceCents;

    public Account(String accountNo, String owner, Type type, long balanceCents) {
        this.accountNo = accountNo;
        this.owner = owner;
        this.type = type;
        this.balanceCents = balanceCents;
    }

    public String getAccountNo()  { return accountNo; }
    public String getOwner()      { return owner; }
    public Type getType()         { return type; }
    public long getBalanceCents() { return balanceCents; }
    public void setBalanceCents(long c) { this.balanceCents = c; }
}
