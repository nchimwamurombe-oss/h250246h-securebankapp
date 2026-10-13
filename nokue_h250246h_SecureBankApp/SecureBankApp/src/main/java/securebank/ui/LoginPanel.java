package securebank.ui;

import java.awt.*;
import java.util.Arrays;
import java.util.function.Consumer;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import securebank.model.User;
import securebank.service.BankException;
import securebank.service.BankService;

public class LoginPanel extends JPanel {
    private final BankService bank;
    private final Consumer<User> onLogin;

    private final JTextField userField = new JTextField();
    private final JPasswordField passField = new JPasswordField();
    private final JPasswordField confirmField = new JPasswordField();
    private final JLabel confirmLabel = Theme.label("Confirm password", Theme.SMALL, Theme.MUTED);
    private final JLabel heading = Theme.label("Welcome back", Theme.TITLE, Theme.DARK);
    private final JLabel sub = Theme.label("Sign in to manage your accounts", Theme.BODY, Theme.MUTED);
    private final JLabel status = Theme.label(" ", Theme.SMALL, Theme.DANGER);
    private final Theme.RoundButton submit = new Theme.RoundButton("Sign In", true);
    private final JButton toggle = new JButton();
    private final Theme.Card formCard = new Theme.Card();
    private boolean registerMode = false;

    public LoginPanel(BankService bank, Consumer<User> onLogin) {
        this.bank = bank;
        this.onLogin = onLogin;
        setLayout(new GridLayout(1, 2));
        add(buildBrandSide());
        add(buildFormSide());
        applyMode();
    }

    private JComponent buildBrandSide() {
        Theme.GradientPanel p = new Theme.GradientPanel(Theme.DARKER, Theme.ACCENT);
        p.setLayout(new GridBagLayout());
        JPanel box = new JPanel();
        box.setOpaque(false);
        box.setLayout(new BoxLayout(box, BoxLayout.Y_AXIS));
        JLabel logo = new JLabel(new Theme.LockIcon(56, Color.WHITE));
        JLabel name = Theme.label("SecureBank", new Font("SansSerif", Font.BOLD, 36), Color.WHITE);
        JLabel tag = Theme.label("Banking you can trust.", Theme.HEADING, new Color(0xEBDDF7));
        for (JLabel l : new JLabel[]{logo, name, tag}) l.setAlignmentX(Component.LEFT_ALIGNMENT);
        box.add(logo); box.add(name); box.add(Box.createVerticalStrut(6)); box.add(tag);
        p.add(box);
        return p;
    }

    private JComponent buildFormSide() {
        JPanel outer = new JPanel(new GridBagLayout());
        outer.setBackground(Theme.BG);

        Theme.Card card = formCard;
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(new EmptyBorder(30, 34, 28, 34));

        Theme.styleField(userField);
        Theme.styleField(passField);
        Theme.styleField(confirmField);
        passField.setEchoChar('\u2022');
        confirmField.setEchoChar('\u2022');

        JLabel ul = Theme.label("Username", Theme.SMALL, Theme.MUTED);
        JLabel pl = Theme.label("Password", Theme.SMALL, Theme.MUTED);

        toggle.setBorderPainted(false);
        toggle.setContentAreaFilled(false);
        toggle.setFocusPainted(false);
        toggle.setMargin(new Insets(0, 0, 0, 0));
        toggle.setHorizontalAlignment(SwingConstants.LEFT);
        toggle.setForeground(Theme.PRIMARY);
        toggle.setFont(new Font("SansSerif", Font.BOLD, 13));
        toggle.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        toggle.addActionListener(e -> { registerMode = !registerMode; applyMode(); });

        submit.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
        submit.addActionListener(e -> submit());

        for (JComponent c : new JComponent[]{heading, sub, ul, userField, pl, passField,
                confirmLabel, confirmField, status, submit, toggle})
            c.setAlignmentX(Component.LEFT_ALIGNMENT);
        for (JComponent c : new JComponent[]{userField, passField, confirmField})
            c.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));

        card.add(heading);
        card.add(Box.createVerticalStrut(4));
        card.add(sub);
        card.add(Box.createVerticalStrut(22));
        card.add(ul); card.add(Box.createVerticalStrut(4)); card.add(userField);
        card.add(Box.createVerticalStrut(12));
        card.add(pl); card.add(Box.createVerticalStrut(4)); card.add(passField);
        card.add(Box.createVerticalStrut(12));
        card.add(confirmLabel); card.add(Box.createVerticalStrut(4)); card.add(confirmField);
        card.add(Box.createVerticalStrut(10));
        card.add(status);
        card.add(Box.createVerticalStrut(8));
        card.add(submit);
        card.add(Box.createVerticalStrut(10));
        card.add(toggle);

        outer.add(card);
        return outer;
    }

    private void applyMode() {
        heading.setText(registerMode ? "Create account" : "Welcome back");
        sub.setText(registerMode ? "Register to open your first account" : "Sign in to manage your accounts");
        submit.setText(registerMode ? "Register" : "Sign In");
        toggle.setText(registerMode ? "Already registered? Sign in" : "New here? Create a login");
        confirmLabel.setVisible(registerMode);
        confirmField.setVisible(registerMode);
        formCard.setPreferredSize(new Dimension(380, registerMode ? 520 : 440));
        setStatus(" ", false);
        revalidate();
        repaint();
    }

    private void setStatus(String msg, boolean ok) {
        status.setForeground(ok ? Theme.SUCCESS : Theme.DANGER);
        status.setText("<html>" + msg.replace("&", "&amp;").replace("<", "&lt;") + "</html>");
    }

    public void reset() {
        userField.setText("");
        passField.setText("");
        confirmField.setText("");
        registerMode = false;
        applyMode();
    }

    public JButton getSubmitButton() { return submit; }

    private void submit() {
        char[] pw = passField.getPassword();
        char[] cf = confirmField.getPassword();
        String username = userField.getText().trim();
        submit.setEnabled(false);
        setStatus("Working\u2026", true);

        new SwingWorker<User, Void>() {
            @Override protected User doInBackground() throws Exception {
                if (registerMode) {
                    bank.register(username, pw, cf);
                    return null;
                }
                return bank.login(username, pw);
            }
            @Override protected void done() {
                submit.setEnabled(true);
                try {
                    User u = get();
                    if (registerMode) {
                        registerMode = false;
                        applyMode();
                        setStatus("Registered! You can now sign in.", true);
                        passField.setText("");
                    } else {
                        passField.setText("");
                        onLogin.accept(u);
                    }
                } catch (Exception ex) {
                    Throwable c = ex.getCause() != null ? ex.getCause() : ex;
                    setStatus(c instanceof BankException ? c.getMessage()
                            : "Something went wrong. Please try again.", false);
                } finally {
                    Arrays.fill(pw, '\0');
                    Arrays.fill(cf, '\0');
                }
            }
        }.execute();
    }
}
