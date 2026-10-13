package securebank.model;

public final class Transaction {
    public enum Type { OPENING, DEPOSIT, WITHDRAWAL }

    private final String id;
    private final String accountNo;
    private final Type type;
    private final long amountCents;
    private final long balanceAfterCents;
    private final long timestamp;

    public Transaction(String id, String accountNo, Type type, long amountCents,
                       long balanceAfterCents, long timestamp) {
        this.id = id;
        this.accountNo = accountNo;
        this.type = type;
        this.amountCents = amountCents;
        this.balanceAfterCents = balanceAfterCents;
        this.timestamp = timestamp;
    }

    public String getId()                { return id; }
    public String getAccountNo()         { return accountNo; }
    public Type getType()                { return type; }
    public long getAmountCents()         { return amountCents; }
    public long getBalanceAfterCents()   { return balanceAfterCents; }
    public long getTimestamp()           { return timestamp; }
}
