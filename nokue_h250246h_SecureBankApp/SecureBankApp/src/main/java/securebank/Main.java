package securebank;

import javax.swing.*;
import securebank.service.BankService;
import securebank.storage.FileStore;
import securebank.ui.MainFrame;
import securebank.ui.Theme;

public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
                Theme.install();
                BankService bank = new BankService(new FileStore("data"));
                new MainFrame(bank).setVisible(true);
            } catch (Exception e) {
                JOptionPane.showMessageDialog(null, "SecureBank could not start: data folder is not accessible.",
                        "SecureBank", JOptionPane.ERROR_MESSAGE);
                System.exit(1);
            }
        });
    }
}
