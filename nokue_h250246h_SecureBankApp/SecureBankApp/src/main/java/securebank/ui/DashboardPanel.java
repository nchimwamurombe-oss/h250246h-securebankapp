package securebank.ui;

import java.awt.*;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.*;
import securebank.model.*;
import securebank.security.Validator;
import securebank.service.BankException;
import securebank.service.BankService;

public class DashboardPanel extends JPanel {
    private final BankService bank;
    private final Runnable onLogout;
    private User user;

    private final DefaultListModel<Account> listModel = new DefaultListModel<>();
    private final JList<Account> accountList = new JList<>(listModel);
    private final JLabel welcome = Theme.label(" ", Theme.BODY, new Color(0xEBDDF7));
    private final JLabel accTitle = Theme.label("No account selected", Theme.HEADING, Theme.MUTED);
    private final JLabel balance = Theme.label("-", Theme.BIG, Theme.DARK);
    private final Theme.RoundButton depositBtn = new Theme.RoundButton("+  Deposit", true);
    private final Theme.RoundButton withdrawBtn = new Theme.RoundButton("\u2212  Withdraw", false);
    private final DefaultTableModel tableModel = new DefaultTableModel(
            new String[]{"Date", "Type", "Amount", "Balance"}, 0) {
        @Override public boolean isCellEditable(int r, int c) { return false; }
    };
    private final JTable table = new JTable(tableModel);
    private final NumberFormat money = NumberFormat.getCurrencyInstance(Locale.US);
    private final SimpleDateFormat dateFmt = new SimpleDateFormat("dd MMM yyyy  HH:mm");

    public DashboardPanel(BankService bank, Runnable onLogout) {
        this.bank = bank;
        this.onLogout = onLogout;
        setLayout(new BorderLayout());
        setBackground(Theme.BG);
        add(buildHeader(), BorderLayout.NORTH);
        add(buildSidebar(), BorderLayout.WEST);
        add(buildMain(), BorderLayout.CENTER);
    }

    private JComponent buildHeader() {
        Theme.GradientPanel h = new Theme.GradientPanel(Theme.DARKER, Theme.PRIMARY);
        h.setLayout(new BorderLayout());
        h.setBorder(new EmptyBorder(14, 24, 14, 24));
        JLabel logo = Theme.label("  SecureBank", new Font("SansSerif", Font.BOLD, 22), Color.WHITE);
        logo.setIcon(new Theme.LockIcon(22, Color.WHITE));
        Theme.RoundButton out = new Theme.RoundButton("Log out", false);
        out.addActionListener(e -> onLogout.run());
        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 14, 0));
        right.setOpaque(false);
        right.add(welcome);
        right.add(out);
        h.add(logo, BorderLayout.WEST);
        h.add(right, BorderLayout.EAST);
        return h;
    }

    private JComponent buildSidebar() {
        JPanel side = new JPanel(new BorderLayout(0, 12));
        side.setBackground(Theme.BG);
        side.setBorder(new EmptyBorder(20, 20, 20, 8));
        side.setPreferredSize(new Dimension(270, 0));

        side.add(Theme.label("My Accounts", Theme.HEADING, Theme.DARK), BorderLayout.NORTH);

        accountList.setCellRenderer(new AccountRenderer());
        accountList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        accountList.setBackground(Theme.BG);
        accountList.setFixedCellHeight(70);
        accountList.addListSelectionListener(e -> { if (!e.getValueIsAdjusting()) refreshDetails(); });
        JScrollPane sp = new JScrollPane(accountList);
        sp.setBorder(null);
        sp.getViewport().setBackground(Theme.BG);
        side.add(sp, BorderLayout.CENTER);

        Theme.RoundButton add = new Theme.RoundButton("+  New Account", true);
        add.addActionListener(e -> createAccount());
        side.add(add, BorderLayout.SOUTH);
        return side;
    }

    private JComponent buildMain() {
        JPanel main = new JPanel(new BorderLayout(0, 16));
        main.setBackground(Theme.BG);
        main.setBorder(new EmptyBorder(20, 8, 20, 24));

        Theme.Card top = new Theme.Card();
        top.setLayout(new BorderLayout());
        JPanel left = new JPanel();
        left.setOpaque(false);
        left.setLayout(new BoxLayout(left, BoxLayout.Y_AXIS));
        accTitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        balance.setAlignmentX(Component.LEFT_ALIGNMENT);
        JLabel cap = Theme.label("Available balance", Theme.SMALL, Theme.MUTED);
        cap.setAlignmentX(Component.LEFT_ALIGNMENT);
        left.add(accTitle); left.add(Box.createVerticalStrut(10));
        left.add(cap); left.add(balance);

        JPanel btns = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        btns.setOpaque(false);
        depositBtn.addActionListener(e -> transact(true));
        withdrawBtn.addActionListener(e -> transact(false));
        btns.add(depositBtn);
        btns.add(withdrawBtn);

        top.add(left, BorderLayout.CENTER);
        top.add(btns, BorderLayout.EAST);
        main.add(top, BorderLayout.NORTH);

        JPanel hist = new JPanel(new BorderLayout(0, 8));
        hist.setOpaque(false);
        hist.add(Theme.label("Transaction History", Theme.HEADING, Theme.DARK), BorderLayout.NORTH);
        Theme.styleTable(table);
        table.setDefaultRenderer(Object.class, new TxRenderer());
        hist.add(Theme.scroll(table), BorderLayout.CENTER);
        main.add(hist, BorderLayout.CENTER);
        return main;
    }

    public void setUser(User u) {
        this.user = u;
        welcome.setText("Signed in as  " + u.getUsername());
        reloadAccounts(null);
    }

    public void clear() {
        user = null;
        listModel.clear();
        tableModel.setRowCount(0);
        balance.setText("-");
    }

    private void reloadAccounts(String selectNo) {
        listModel.clear();
        int sel = 0, i = 0;
        for (Account a : bank.getAccounts(user)) {
            listModel.addElement(a);
            if (a.getAccountNo().equals(selectNo)) sel = i;
            i++;
        }
        if (!listModel.isEmpty()) accountList.setSelectedIndex(sel);
        refreshDetails();
    }

    private Account selected() { return accountList.getSelectedValue(); }

    private void refreshDetails() {
        Account a = selected();
        boolean has = a != null;
        depositBtn.setEnabled(has);
        withdrawBtn.setEnabled(has);
        tableModel.setRowCount(0);
        if (!has) {
            accTitle.setText(listModel.isEmpty() ? "Open an account to get started" : "No account selected");
            balance.setText("-");
            return;
        }
        try {
            accTitle.setText(cap(a.getType().name()) + " \u2022 " + a.getAccountNo());
            balance.setText(fmt(bank.getBalance(user, a.getAccountNo())));
            for (Transaction t : bank.getTransactions(user, a.getAccountNo()))
                tableModel.addRow(new Object[]{dateFmt.format(new Date(t.getTimestamp())),
                        cap(t.getType().name()),
                        (t.getType() == Transaction.Type.WITHDRAWAL ? "-" : "+") + fmt(t.getAmountCents()),
                        fmt(t.getBalanceAfterCents())});
        } catch (BankException e) {
            error(e.getMessage());
        }
    }

    private void createAccount() {
        JComboBox<String> type = new JComboBox<>(new String[]{"Savings", "Current"});
        Theme.styleCombo(type);
        JTextField opening = new JTextField("0");
        Theme.styleField(opening);
        JPanel p = new JPanel(new GridLayout(0, 1, 0, 6));
        p.setOpaque(false);
        p.add(Theme.label("Account type", Theme.SMALL, Theme.MUTED));
        p.add(type);
        p.add(Theme.label("Opening deposit (optional)", Theme.SMALL, Theme.MUTED));
        p.add(opening);
        if (JOptionPane.showConfirmDialog(this, p, "Open New Account",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE) != JOptionPane.OK_OPTION) return;
        try {
            String txt = opening.getText().trim();
            long cents = (txt.isEmpty() || txt.equals("0")) ? 0 : Validator.parseAmountCents(txt);
            Account a = bank.createAccount(user,
                    type.getSelectedIndex() == 0 ? Account.Type.SAVINGS : Account.Type.CURRENT, cents);
            reloadAccounts(a.getAccountNo());
        } catch (IllegalArgumentException | BankException ex) {
            error(ex.getMessage());
        } catch (BankService.RuntimeBankException ex) {
            error("Could not save your data. Please try again.");
        }
    }

    private void transact(boolean deposit) {
        Account a = selected();
        if (a == null) return;
        JTextField amt = new JTextField();
        Theme.styleField(amt);
        JPanel p = new JPanel(new GridLayout(0, 1, 0, 6));
        p.setOpaque(false);
        p.add(Theme.label(deposit ? "Amount to deposit" : "Amount to withdraw", Theme.SMALL, Theme.MUTED));
        p.add(amt);
        p.setPreferredSize(new Dimension(280, 70));
        if (JOptionPane.showConfirmDialog(this, p, deposit ? "Deposit Funds" : "Withdraw Funds",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE) != JOptionPane.OK_OPTION) return;
        try {
            long cents = Validator.parseAmountCents(amt.getText());
            if (deposit) bank.deposit(user, a.getAccountNo(), cents);
            else bank.withdraw(user, a.getAccountNo(), cents);
            reloadAccounts(a.getAccountNo());
        } catch (IllegalArgumentException | BankException ex) {
            error(ex.getMessage());
        } catch (BankService.RuntimeBankException ex) {
            error("Could not save your data. Please try again.");
        }
    }

    private void error(String msg) {
        JOptionPane.showMessageDialog(this, msg, "SecureBank", JOptionPane.WARNING_MESSAGE);
    }

    private String fmt(long cents) { return money.format(cents / 100.0); }
    private static String cap(String s) { return s.charAt(0) + s.substring(1).toLowerCase(); }

    private class AccountRenderer extends JPanel implements ListCellRenderer<Account> {
        private String line1 = "", line2 = "";
        private boolean sel;

        AccountRenderer() { setOpaque(false); }

        @Override public Component getListCellRendererComponent(JList<? extends Account> l, Account a,
                int i, boolean isSel, boolean focus) {
            sel = isSel;
            line1 = cap(a.getType().name()) + "  \u2022\u2022" + a.getAccountNo().substring(6);
            line2 = fmt(a.getBalanceCents());
            return this;
        }

        @Override protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            int w = getWidth() - 1, h = getHeight() - 8;
            if (sel) g2.setPaint(new GradientPaint(0, 0, Theme.PRIMARY, w, 0, Theme.ACCENT));
            else g2.setColor(Color.WHITE);
            g2.fillRoundRect(0, 3, w, h, 16, 16);
            if (!sel) { g2.setColor(Theme.BORDER); g2.drawRoundRect(0, 3, w, h, 16, 16); }
            g2.setFont(new Font("SansSerif", Font.BOLD, 14));
            g2.setColor(sel ? Color.WHITE : Theme.TEXT);
            g2.drawString(line1, 16, 30);
            g2.setFont(Theme.BODY);
            g2.setColor(sel ? new Color(0xEBDDF7) : Theme.MUTED);
            g2.drawString(line2, 16, 52);
            g2.dispose();
        }
    }

    private static class TxRenderer extends DefaultTableCellRenderer {
        @Override public Component getTableCellRendererComponent(JTable t, Object v, boolean s,
                                                                  boolean f, int r, int c) {
            JLabel l = (JLabel) super.getTableCellRendererComponent(t, v, s, f, r, c);
            l.setBorder(new EmptyBorder(0, 10, 0, 10));
            l.setHorizontalAlignment(c >= 2 ? SwingConstants.RIGHT : SwingConstants.LEFT);
            if (!s) {
                l.setBackground(r % 2 == 0 ? Color.WHITE : new Color(0xFAF5FD));
                l.setForeground(c == 2 ? (String.valueOf(v).startsWith("-") ? Theme.DANGER : Theme.SUCCESS)
                        : Theme.TEXT);
            }
            if (c == 2) l.setFont(l.getFont().deriveFont(Font.BOLD));
            return l;
        }
    }
}
