package securebank.ui;

import java.awt.*;
import javax.swing.*;
import securebank.model.User;
import securebank.service.BankService;

public class MainFrame extends JFrame {
    private static final String LOGIN = "login", DASH = "dash";
    private final CardLayout cards = new CardLayout();
    private final JPanel root = new JPanel(cards);
    private final LoginPanel login;
    private final DashboardPanel dashboard;

    public MainFrame(BankService bank) {
        super("SecureBank \u2013 Secure Banking Application");
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        login = new LoginPanel(bank, this::showDashboard);
        dashboard = new DashboardPanel(bank, this::logout);
        root.add(login, LOGIN);
        root.add(dashboard, DASH);
        setContentPane(root);
        getRootPane().setDefaultButton(login.getSubmitButton());

        setMinimumSize(new Dimension(900, 600));
        setSize(1020, 660);
        setLocationRelativeTo(null);
    }

    private void showDashboard(User u) {
        dashboard.setUser(u);
        getRootPane().setDefaultButton(null);
        cards.show(root, DASH);
    }

    private void logout() {
        dashboard.clear();
        login.reset();
        getRootPane().setDefaultButton(login.getSubmitButton());
        cards.show(root, LOGIN);
    }
}
