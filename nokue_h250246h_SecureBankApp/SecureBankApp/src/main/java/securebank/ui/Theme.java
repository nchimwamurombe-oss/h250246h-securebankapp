package securebank.ui;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;

public final class Theme {
    public static final Color PRIMARY   = new Color(0x6A1B9A);
    public static final Color DARK      = new Color(0x4A148C);
    public static final Color DARKER    = new Color(0x311B5C);
    public static final Color ACCENT    = new Color(0x9C4DCC);
    public static final Color LIGHT     = new Color(0xE1BEE7);
    public static final Color BG        = new Color(0xF6EFFA);
    public static final Color CARD      = Color.WHITE;
    public static final Color TEXT      = new Color(0x2B1B3F);
    public static final Color MUTED     = new Color(0x7B6A8D);
    public static final Color BORDER    = new Color(0xD8C5E6);
    public static final Color SUCCESS   = new Color(0x2E7D32);
    public static final Color DANGER    = new Color(0xC62828);

    public static final Font TITLE  = new Font("SansSerif", Font.BOLD, 26);
    public static final Font HEADING= new Font("SansSerif", Font.BOLD, 16);
    public static final Font BODY   = new Font("SansSerif", Font.PLAIN, 14);
    public static final Font SMALL  = new Font("SansSerif", Font.PLAIN, 12);
    public static final Font BIG    = new Font("SansSerif", Font.BOLD, 38);

    private Theme() { }

    public static void install() {
        UIManager.put("OptionPane.background", BG);
        UIManager.put("Panel.background", BG);
        UIManager.put("OptionPane.messageForeground", TEXT);
        UIManager.put("OptionPane.messageFont", BODY);
        UIManager.put("OptionPane.buttonFont", BODY);
        UIManager.put("Button.background", PRIMARY);
        UIManager.put("Button.foreground", Color.WHITE);
        UIManager.put("Label.foreground", TEXT);
        UIManager.put("Label.font", BODY);
        UIManager.put("TextField.font", BODY);
        UIManager.put("PasswordField.font", BODY);
        UIManager.put("ComboBox.font", BODY);
        UIManager.put("ComboBox.selectionBackground", LIGHT);
        UIManager.put("ComboBox.selectionForeground", DARKER);
        UIManager.put("ScrollBar.thumb", ACCENT);
        UIManager.put("ScrollBar.track", BG);
    }

    public static JLabel label(String text, Font font, Color color) {
        JLabel l = new JLabel(text);
        l.setFont(font);
        l.setForeground(color);
        return l;
    }

    public static void styleField(JTextField f) {
        f.setFont(BODY);
        f.setForeground(TEXT);
        f.setCaretColor(PRIMARY);
        f.setSelectionColor(LIGHT);
        f.setBackground(Color.WHITE);
        f.setBorder(fieldBorder(false));
        f.addFocusListener(new java.awt.event.FocusAdapter() {
            @Override public void focusGained(java.awt.event.FocusEvent e) { f.setBorder(fieldBorder(true)); }
            @Override public void focusLost(java.awt.event.FocusEvent e)   { f.setBorder(fieldBorder(false)); }
        });
    }

    private static Border fieldBorder(boolean focused) {
        return new CompoundBorder(new LineBorder(focused ? PRIMARY : BORDER, focused ? 2 : 1, true),
                new EmptyBorder(focused ? 7 : 8, focused ? 9 : 10, focused ? 7 : 8, focused ? 9 : 10));
    }

    public static void styleCombo(JComboBox<?> c) {
        c.setFont(BODY);
        c.setBackground(Color.WHITE);
        c.setForeground(TEXT);
        c.setBorder(new LineBorder(BORDER, 1, true));
    }

    public static void styleTable(JTable t) {
        t.setFont(BODY);
        t.setForeground(TEXT);
        t.setRowHeight(34);
        t.setShowVerticalLines(false);
        t.setGridColor(new Color(0xEBDDF3));
        t.setSelectionBackground(LIGHT);
        t.setSelectionForeground(DARKER);
        t.setIntercellSpacing(new Dimension(0, 1));
        t.setFillsViewportHeight(true);
        JTableHeader h = t.getTableHeader();
        h.setReorderingAllowed(false);
        h.setDefaultRenderer(new DefaultTableCellRenderer() {
            @Override public Component getTableCellRendererComponent(JTable tb, Object v, boolean s,
                                                                      boolean f, int r, int c) {
                JLabel l = (JLabel) super.getTableCellRendererComponent(tb, v, s, f, r, c);
                l.setBackground(PRIMARY);
                l.setForeground(Color.WHITE);
                l.setFont(new Font("SansSerif", Font.BOLD, 13));
                l.setBorder(new EmptyBorder(0, 10, 0, 10));
                l.setHorizontalAlignment(c >= 2 ? SwingConstants.RIGHT : SwingConstants.LEFT);
                l.setOpaque(true);
                return l;
            }
        });
        h.setPreferredSize(new Dimension(100, 36));
    }

    public static JScrollPane scroll(Component c) {
        JScrollPane sp = new JScrollPane(c);
        sp.setBorder(new LineBorder(BORDER, 1, true));
        sp.getViewport().setBackground(Color.WHITE);
        return sp;
    }

    public static class RoundButton extends JButton {
        private final boolean filled;
        private boolean hover;

        public RoundButton(String text, boolean filled) {
            super(text);
            this.filled = filled;
            setFont(new Font("SansSerif", Font.BOLD, 14));
            setForeground(filled ? Color.WHITE : PRIMARY);
            setContentAreaFilled(false);
            setBorderPainted(false);
            setFocusPainted(false);
            setOpaque(false);
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            setBorder(new EmptyBorder(10, 22, 10, 22));
            addMouseListener(new MouseAdapter() {
                @Override public void mouseEntered(MouseEvent e) { hover = true; repaint(); }
                @Override public void mouseExited(MouseEvent e)  { hover = false; repaint(); }
            });
        }

        @Override protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int w = getWidth(), h = getHeight();
            if (!isEnabled()) {
                g2.setColor(new Color(0xD9CFE2));
                g2.fillRoundRect(0, 0, w, h, 14, 14);
            } else if (filled) {
                g2.setPaint(new GradientPaint(0, 0, hover ? ACCENT : PRIMARY, 0, h, hover ? PRIMARY : DARK));
                g2.fillRoundRect(0, 0, w, h, 14, 14);
            } else {
                g2.setColor(hover ? LIGHT : Color.WHITE);
                g2.fillRoundRect(0, 0, w, h, 14, 14);
                g2.setColor(PRIMARY);
                g2.setStroke(new BasicStroke(1.6f));
                g2.drawRoundRect(1, 1, w - 3, h - 3, 14, 14);
            }
            g2.dispose();
            super.paintComponent(g);
        }
    }

    public static class Card extends JPanel {
        public Card() { setOpaque(false); setBorder(new EmptyBorder(20, 22, 20, 22)); }
        @Override protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(CARD);
            g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 20, 20);
            g2.setColor(BORDER);
            g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 20, 20);
            g2.dispose();
            super.paintComponent(g);
        }
    }

    public static class GradientPanel extends JPanel {
        private final Color a, b;
        public GradientPanel(Color a, Color b) { this.a = a; this.b = b; }
        @Override protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setPaint(new GradientPaint(0, 0, a, getWidth(), getHeight(), b));
            g2.fillRect(0, 0, getWidth(), getHeight());

            g2.setColor(new Color(255, 255, 255, 22));
            g2.fillOval(-getWidth() / 4, getHeight() - getHeight() / 3, getWidth(), getWidth());
            g2.setColor(new Color(255, 255, 255, 16));
            g2.fillOval(getWidth() / 2, -getHeight() / 6, getWidth(), getWidth());
            g2.dispose();
        }
    }

    public static class LockIcon implements Icon {
        private final int size; private final Color color;
        public LockIcon(int size, Color color) { this.size = size; this.color = color; }
        @Override public int getIconWidth()  { return size; }
        @Override public int getIconHeight() { return size; }
        @Override public void paintIcon(Component c, Graphics g, int x, int y) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(color);
            float s = size / 10f;
            g2.setStroke(new BasicStroke(s * 1.1f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g2.drawArc(Math.round(x + 2.6f * s), Math.round(y + 0.6f * s), Math.round(4.8f * s), Math.round(5.6f * s), 0, 180);
            g2.drawLine(Math.round(x + 2.6f * s), Math.round(y + 3.4f * s), Math.round(x + 2.6f * s), Math.round(y + 4.6f * s));
            g2.drawLine(Math.round(x + 7.4f * s), Math.round(y + 3.4f * s), Math.round(x + 7.4f * s), Math.round(y + 4.6f * s));
            g2.fillRoundRect(Math.round(x + 1.2f * s), Math.round(y + 4.4f * s), Math.round(7.6f * s), Math.round(5.2f * s), Math.round(1.6f * s), Math.round(1.6f * s));
            g2.dispose();
        }
    }
}
