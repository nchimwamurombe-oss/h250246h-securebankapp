package securebank.security;

import java.math.BigDecimal;
import java.util.regex.Pattern;

public final class Validator {
    public static final long MAX_AMOUNT_CENTS = 100_000_000L;
    private static final Pattern USERNAME = Pattern.compile("^[A-Za-z0-9_]{3,20}$");
    private static final Pattern AMOUNT = Pattern.compile("^\\d{1,9}(\\.\\d{1,2})?$");

    private Validator() { }

    public static boolean isValidUsername(String s) {
        return s != null && USERNAME.matcher(s).matches();
    }

    public static String passwordProblem(char[] p) {
        if (p == null || p.length < 8)  return "Password must be at least 8 characters.";
        if (p.length > 64)              return "Password must be at most 64 characters.";
        boolean up = false, lo = false, di = false, sp = false;
        for (char c : p) {
            if (Character.isUpperCase(c)) up = true;
            else if (Character.isLowerCase(c)) lo = true;
            else if (Character.isDigit(c)) di = true;
            else if (!Character.isWhitespace(c)) sp = true;
        }
        if (!(up && lo && di && sp))
            return "Password needs upper & lower case letters, a digit and a symbol.";
        return null;
    }

    public static long parseAmountCents(String text) {
        String t = text == null ? "" : text.trim();
        if (!AMOUNT.matcher(t).matches())
            throw new IllegalArgumentException("Enter a valid amount, e.g. 150 or 150.75");
        long cents = new BigDecimal(t).movePointRight(2).longValueExact();
        if (cents <= 0)
            throw new IllegalArgumentException("Amount must be greater than zero.");
        if (cents > MAX_AMOUNT_CENTS)
            throw new IllegalArgumentException("Amount exceeds the per-transaction limit of 1,000,000.00.");
        return cents;
    }
}
