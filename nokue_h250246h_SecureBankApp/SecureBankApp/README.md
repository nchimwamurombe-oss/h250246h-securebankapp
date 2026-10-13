# Secure Banking Application

A Java Swing desktop banking application with a purple-themed GUI, built with Apache Maven.
It demonstrates Object-Oriented design together with secure coding practices
(password hashing, input validation, lock-out, safe file persistence).

## Student Details

- **Name:** _Your Full Name_
- **Registration Number:** _Your Reg Number_

## Features

- **User authentication** – register and sign in; passwords are never stored in plain text
- **Account management** – open Savings or Current accounts (several per user) and view balances
- **Transactions** – deposit and withdraw funds with full per-account transaction history
- **Data persistence** – users, accounts and transactions are saved to text files in `data/` and reloaded on start-up
- **Purple-themed GUI** – Swing interface with custom-painted buttons, cards, gradients and tables

## Security Measures

| Area | What is done |
|------|--------------|
| Password storage | Salted PBKDF2-HMAC-SHA256 (120,000 iterations, 16-byte random salt); constant-time comparison |
| Password policy | 8–64 characters with upper case, lower case, digit and symbol |
| Brute-force defence | Account locks for 60 s after 3 failed sign-ins (persisted across restarts) |
| User enumeration | Identical error message for bad username / bad password; dummy hash computed for unknown users |
| Input validation | Whitelist regex for usernames and amounts; amount limit of 1,000,000.00 per transaction |
| Money handling | Stored as integer cents (`long`) with `Math.addExact` – no floating-point rounding or silent overflow |
| Access control | Every account operation re-checks that the logged-in user owns the account |
| Memory hygiene | Passwords handled as `char[]` and cleared after use |
| File safety | Writes go to a temp file then atomically replace the real file; corrupt lines are skipped, not trusted |
| Error handling | Users see friendly messages; internal details are never shown |
| Responsiveness | Slow password hashing runs on a background thread so the GUI never freezes |

## Project Structure

```
SecureBankApp/
├── pom.xml                    Maven build file
├── data/                      users.txt, accounts.txt, transactions.txt
└── src/main/java/securebank/
    ├── Main.java              entry point
    ├── model/                 User, Account, Transaction
    ├── security/              PasswordHasher, Validator
    ├── storage/               FileStore (text-file persistence)
    ├── service/               BankService (all business rules), BankException
    └── ui/                    Theme, LoginPanel, DashboardPanel, MainFrame
```

## Requirements

- JDK 11 or newer
- Apache Maven 3.6 or newer

## Build and Run

Run these from the project root (so the `data/` folder is found):

```bash
mvn compile exec:java     # compile and launch the GUI
mvn package               # build target/SecureBankApp.jar
java -jar target/SecureBankApp.jar
mvn clean                 # remove target/
```

## Usage

1. Click **New here? Create a login**, choose a username and a strong password, then **Register**.
2. **Sign In**, then press **+ New Account** to open a Savings or Current account.
3. Select an account and use **Deposit** / **Withdraw**; the history table updates immediately.
4. **Log out** returns to the sign-in screen. Closing and reopening the app keeps all your data.

## Data File Formats (pipe-delimited)

- `users.txt` – `username|salt(Base64)|hash(Base64)|iterations|failedAttempts|lockedUntil`
- `accounts.txt` – `accountNo|owner|type|balanceCents`
- `transactions.txt` – `id|accountNo|type|amountCents|balanceAfterCents|timestamp`

> Note: the data files are not encrypted or integrity-protected; anyone with file-system access could edit balances.
> In a production system this would be a database with access controls.
