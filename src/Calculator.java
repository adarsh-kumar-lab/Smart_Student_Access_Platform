import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;


public class Calculator extends JFrame implements ActionListener {

    private JTextField display;
    private JButton[] numbers = new JButton[10];
    private JButton add, sub, mul, div;
    private JButton equal, clear, delete;
    private JButton dot, percent, back, doubleZero;

    private JTextArea historyArea;
    private ArrayList<String> historyList = new ArrayList<>();

    private double num1 = 0, num2 = 0, result = 0;
    private char operator;


    public Calculator() {

        setTitle("Basic Calculator");
        setSize(700, 650);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout());
        getContentPane().setBackground(Theme.BG_DARK);

        // ── Display ───────────────────────────────────────────────
        display = new JTextField();
        display.setEditable(false);
        display.setHorizontalAlignment(JTextField.RIGHT);
        display.setFont(Theme.FONT_LARGE);
        display.setBackground(Theme.BG_PANEL);
        display.setForeground(Theme.TEXT_PRIMARY);
        display.setCaretColor(Theme.ACCENT_GREEN);
        display.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 2, 0, Theme.ACCENT_GREEN),
            BorderFactory.createEmptyBorder(25, 20, 25, 20)));

        JPanel main = new JPanel(new BorderLayout());
        main.setBackground(Theme.BG_DARK);
        main.add(display, BorderLayout.NORTH);

        // ── Button panel ──────────────────────────────────────────
        JPanel panel = new JPanel(new GridLayout(5, 4, 12, 12));
        panel.setBackground(Theme.BG_DARK);
        panel.setBorder(BorderFactory.createEmptyBorder(18, 18, 18, 18));

        clear       = btn("AC",        "clear");
        percent     = btn("%",         "normal");
        delete      = btn("←",        "delete");
        div         = btn("÷",        "op");
        mul         = btn("×",        "op");
        sub         = btn("-",         "op");
        add         = btn("+",         "op");
        equal       = btn("=",         "equal");
        dot         = btn(".",         "normal");
        doubleZero  = btn("00",        "normal");
        back        = btn("Dashboard", "back");

        for (int i = 0; i < 10; i++)
            numbers[i] = btn(String.valueOf(i), "normal");

        panel.add(clear);     panel.add(percent); panel.add(delete); panel.add(div);
        panel.add(numbers[7]); panel.add(numbers[8]); panel.add(numbers[9]); panel.add(mul);
        panel.add(numbers[4]); panel.add(numbers[5]); panel.add(numbers[6]); panel.add(sub);
        panel.add(numbers[1]); panel.add(numbers[2]); panel.add(numbers[3]); panel.add(add);
        panel.add(doubleZero); panel.add(numbers[0]); panel.add(dot); panel.add(equal);

        main.add(panel, BorderLayout.CENTER);

        JPanel bottom = new JPanel();
        bottom.setBackground(Theme.BG_PANEL);
        bottom.setBorder(BorderFactory.createMatteBorder(2, 0, 0, 0, Theme.BORDER));
        bottom.add(back);
        main.add(bottom, BorderLayout.SOUTH);

        add(main, BorderLayout.CENTER);

        // ── History panel ─────────────────────────────────────────
        JPanel historyPanel = new JPanel(new BorderLayout());
        historyPanel.setPreferredSize(new Dimension(220, 0));
        historyPanel.setBackground(Theme.BG_PANEL);
        historyPanel.setBorder(BorderFactory.createMatteBorder(0, 2, 0, 0, Theme.BORDER));

        JLabel histTitle = new JLabel("History", JLabel.CENTER);
        histTitle.setFont(Theme.FONT_HEADER);
        histTitle.setForeground(Theme.ACCENT_GREEN);
        histTitle.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));

        historyArea = new JTextArea();
        historyArea.setEditable(false);
        historyArea.setFont(Theme.FONT_SMALL);
        historyArea.setBackground(Theme.BG_PANEL);
        historyArea.setForeground(Theme.TEXT_SECONDARY);
        historyArea.setBorder(BorderFactory.createEmptyBorder(6, 8, 6, 8));

        JButton clearHist = Theme.makeButton("Clear History", Theme.ACCENT_RED);
        clearHist.addActionListener(e -> { historyList.clear(); historyArea.setText(""); });

        historyPanel.add(histTitle, BorderLayout.NORTH);
        historyPanel.add(new JScrollPane(historyArea), BorderLayout.CENTER);
        historyPanel.add(clearHist, BorderLayout.SOUTH);

        add(historyPanel, BorderLayout.EAST);
        setVisible(true);
    }


    private JButton btn(String text, String type) {

        JButton b = new JButton(text);
        b.setFont(new Font("Segoe UI", Font.BOLD, 20));
        b.setFocusPainted(false);
        b.setBorderPainted(false);

        switch (type) {
            case "equal":  b.setBackground(Theme.ACCENT_GREEN);  b.setForeground(Color.WHITE); break;
            case "op":     b.setBackground(new Color(30, 70, 55)); b.setForeground(Theme.ACCENT_LIGHT); break;
            case "delete": b.setBackground(Theme.BG_CARD);       b.setForeground(Theme.ACCENT_RED);    break;
            case "clear":  b.setBackground(Theme.ACCENT_RED);    b.setForeground(Color.WHITE);          break;
            case "back":   b.setBackground(Theme.ACCENT_BLUE);   b.setForeground(Color.WHITE);          break;
            default:       b.setBackground(Theme.BG_CARD);       b.setForeground(Theme.TEXT_PRIMARY);   break;
        }

        b.addActionListener(this);
        return b;
    }


    @Override
    public void actionPerformed(ActionEvent e) {

        for (int i = 0; i < 10; i++) {
            if (e.getSource() == numbers[i]) {
                display.setText(display.getText() + i);
                return;
            }
        }

        if (e.getSource() == doubleZero) { display.setText(display.getText() + "00"); return; }
        if (e.getSource() == dot)   { display.setText(display.getText() + "."); return; }

        if (e.getSource() == clear) {
            display.setText(""); num1 = num2 = result = 0; return;
        }

        if (e.getSource() == delete) {
            String v = display.getText();
            if (v.length() > 0) display.setText(v.substring(0, v.length() - 1));
            return;
        }

        if (e.getSource() == percent) {
            try {
                display.setText(String.valueOf(Double.parseDouble(display.getText()) / 100));
            } catch (Exception ignored) {}
            return;
        }

        if (e.getSource() == add || e.getSource() == sub ||
            e.getSource() == mul || e.getSource() == div) {
            try { num1 = Double.parseDouble(display.getText()); } catch (Exception ignored) {}
            if (e.getSource() == add) operator = '+';
            if (e.getSource() == sub) operator = '-';
            if (e.getSource() == mul) operator = '*';
            if (e.getSource() == div) operator = '/';
            display.setText("");
            return;
        }

        if (e.getSource() == equal) {
            try {
                num2 = Double.parseDouble(display.getText());
                switch (operator) {
                    case '+': result = num1 + num2; break;
                    case '-': result = num1 - num2; break;
                    case '*': result = num1 * num2; break;
                    case '/':
                        if (num2 == 0) {
                            JOptionPane.showMessageDialog(this, "Cannot divide by zero!");
                            return;
                        }
                        result = num1 / num2;
                        break;
                }
                String hist = num1 + " " + operator + " " + num2 + " = " + result;
                historyList.add(hist);
                updateHistory();
                display.setText(String.valueOf(result));
                num1 = result;
            } catch (Exception ignored) {}
        }

        if (e.getSource() == back) {
            new Dashboard();
            dispose();
        }
    }


    private void updateHistory() {
        historyArea.setText("");
        for (String item : historyList) historyArea.append(item + "\n\n");
    }
}
