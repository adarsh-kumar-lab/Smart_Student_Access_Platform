import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;


public class ScientificCalculator extends JFrame implements ActionListener {

    private JTextField display;
    private JButton[] numbers = new JButton[10];
    private JButton add, sub, mul, div, equal, clear, delete, dot;
    private JButton sin, cos, tan, sqrt, log, ln, square, power;
    private JButton pi, euler, back;

    private JTextArea historyArea;
    private ArrayList<String> historyList = new ArrayList<>();

    private double num1, num2, result;
    private char operator;


    public ScientificCalculator() {

        setTitle("Scientific Calculator");
        setSize(700, 780);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout());
        getContentPane().setBackground(Theme.BG_DARK);

        // ── Header ────────────────────────────────────────────────
        JPanel header = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 18));
        header.setBackground(Theme.BG_PANEL);
        header.setPreferredSize(new Dimension(0, 70));
        header.setBorder(BorderFactory.createMatteBorder(0, 0, 2, 0, Theme.ACCENT_GREEN));

        JLabel ico = new JLabel("🔬");
        ico.setFont(new Font("Segoe UI", Font.PLAIN, 32));
        JLabel title = new JLabel("Scientific Calculator");
        title.setFont(Theme.FONT_TITLE);
        title.setForeground(Theme.ACCENT_GREEN);
        header.add(ico); header.add(title);
        add(header, BorderLayout.NORTH);

        // ── Display ───────────────────────────────────────────────
        display = new JTextField();
        display.setEditable(false);
        display.setHorizontalAlignment(JTextField.RIGHT);
        display.setFont(Theme.FONT_LARGE);
        display.setBackground(Theme.BG_PANEL);
        display.setForeground(Theme.TEXT_PRIMARY);
        display.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 2, 0, Theme.ACCENT_GREEN),
            BorderFactory.createEmptyBorder(20, 20, 20, 20)));

        JPanel main = new JPanel(new BorderLayout());
        main.setBackground(Theme.BG_DARK);
        main.add(display, BorderLayout.NORTH);

        // ── Buttons ───────────────────────────────────────────────
        JPanel panel = new JPanel(new GridLayout(7, 4, 11, 11));
        panel.setBackground(Theme.BG_DARK);
        panel.setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));

        sin   = sbtn("sin","sci"); cos   = sbtn("cos","sci");
        tan   = sbtn("tan","sci"); sqrt  = sbtn("√","sci");
        log   = sbtn("log","sci"); ln    = sbtn("ln","sci");
        square= sbtn("x²","sci"); power = sbtn("xʸ","op");
        pi    = sbtn("π","sci");   euler = sbtn("e","sci");
        clear = sbtn("AC","clear"); delete= sbtn("←","del");
        div   = sbtn("÷","op");  mul   = sbtn("×","op");
        sub   = sbtn("-","op");   add   = sbtn("+","op");
        dot   = sbtn(".","num");  equal = sbtn("=","equal");
        back  = sbtn("Dashboard","back");

        for (int i = 0; i < 10; i++) numbers[i] = sbtn(String.valueOf(i), "num");

        panel.add(sin);  panel.add(cos);  panel.add(tan);  panel.add(sqrt);
        panel.add(log);  panel.add(ln);   panel.add(square); panel.add(power);
        panel.add(pi);   panel.add(euler); panel.add(clear); panel.add(delete);
        panel.add(numbers[7]); panel.add(numbers[8]); panel.add(numbers[9]); panel.add(div);
        panel.add(numbers[4]); panel.add(numbers[5]); panel.add(numbers[6]); panel.add(mul);
        panel.add(numbers[1]); panel.add(numbers[2]); panel.add(numbers[3]); panel.add(sub);
        panel.add(numbers[0]); panel.add(dot);         panel.add(equal);       panel.add(add);

        main.add(panel, BorderLayout.CENTER);

        JPanel bottom = new JPanel();
        bottom.setBackground(Theme.BG_PANEL);
        bottom.setBorder(BorderFactory.createMatteBorder(2, 0, 0, 0, Theme.BORDER));
        bottom.add(back);
        main.add(bottom, BorderLayout.SOUTH);

        add(main, BorderLayout.CENTER);

        // ── History panel ─────────────────────────────────────────
        JPanel histPanel = new JPanel(new BorderLayout());
        histPanel.setPreferredSize(new Dimension(210, 0));
        histPanel.setBackground(Theme.BG_PANEL);
        histPanel.setBorder(BorderFactory.createMatteBorder(0, 2, 0, 0, Theme.BORDER));

        JLabel histTitle = new JLabel("History", JLabel.CENTER);
        histTitle.setFont(Theme.FONT_HEADER);
        histTitle.setForeground(Theme.ACCENT_GREEN);
        histTitle.setBorder(BorderFactory.createEmptyBorder(8, 0, 8, 0));

        historyArea = new JTextArea();
        historyArea.setEditable(false);
        historyArea.setFont(Theme.FONT_SMALL);
        historyArea.setBackground(Theme.BG_PANEL);
        historyArea.setForeground(Theme.TEXT_SECONDARY);
        historyArea.setBorder(BorderFactory.createEmptyBorder(5, 6, 5, 6));

        JButton clearHist = Theme.makeButton("Clear", Theme.ACCENT_RED);
        clearHist.addActionListener(e -> { historyList.clear(); historyArea.setText(""); });

        histPanel.add(histTitle, BorderLayout.NORTH);
        histPanel.add(new JScrollPane(historyArea), BorderLayout.CENTER);
        histPanel.add(clearHist, BorderLayout.SOUTH);

        add(histPanel, BorderLayout.EAST);
        setVisible(true);
    }


    private JButton sbtn(String text, String type) {
        JButton b = new JButton(text);
        b.setFont(new Font("Segoe UI", Font.BOLD, 17));
        b.setFocusPainted(false);
        b.setBorderPainted(false);

        switch (type) {
            case "equal": b.setBackground(Theme.ACCENT_GREEN); b.setForeground(Color.WHITE); break;
            case "op":    b.setBackground(new Color(30, 70, 55)); b.setForeground(Theme.ACCENT_LIGHT); break;
            case "sci":   b.setBackground(new Color(20, 50, 75)); b.setForeground(Theme.ACCENT_BLUE); break;
            case "clear": b.setBackground(Theme.ACCENT_RED); b.setForeground(Color.WHITE); break;
            case "del":   b.setBackground(Theme.BG_CARD); b.setForeground(Theme.ACCENT_RED); break;
            case "back":  b.setBackground(Theme.ACCENT_BLUE); b.setForeground(Color.WHITE); break;
            default:      b.setBackground(Theme.BG_CARD); b.setForeground(Theme.TEXT_PRIMARY); break;
        }

        b.addActionListener(this);
        return b;
    }


    @Override
    public void actionPerformed(ActionEvent e) {

        String cmd = e.getActionCommand();

        for (int i = 0; i < 10; i++) {
            if (cmd.equals(String.valueOf(i))) {
                display.setText(display.getText() + i);
                return;
            }
        }

        try {
            switch (cmd) {
                case "AC": display.setText(""); break;
                case "←": {
                    String s = display.getText();
                    if (!s.isEmpty()) display.setText(s.substring(0, s.length() - 1));
                    break;
                }
                case "sin": display.setText(String.valueOf(Math.sin(Math.toRadians(parse())))); break;
                case "cos": display.setText(String.valueOf(Math.cos(Math.toRadians(parse())))); break;
                case "tan": display.setText(String.valueOf(Math.tan(Math.toRadians(parse())))); break;
                case "√":   display.setText(String.valueOf(Math.sqrt(parse()))); break;
                case "x²":  { double x = parse(); display.setText(String.valueOf(x * x)); break; }
                case "log": display.setText(String.valueOf(Math.log10(parse()))); break;
                case "ln":  display.setText(String.valueOf(Math.log(parse()))); break;
                case "π":   display.setText(String.valueOf(Math.PI)); break;
                case "e":   display.setText(String.valueOf(Math.E)); break;
                case ".":   display.setText(display.getText() + "."); break;

                case "+": case "-": case "×": case "÷":
                    num1 = parse();
                    if (cmd.equals("+")) operator = '+';
                    if (cmd.equals("-")) operator = '-';
                    if (cmd.equals("×")) operator = '*';
                    if (cmd.equals("÷")) operator = '/';
                    display.setText("");
                    break;

                case "xʸ":
                    num1 = parse();
                    operator = '^';
                    display.setText("");
                    break;

                case "=":
                    num2 = parse();
                    switch (operator) {
                        case '+': result = num1 + num2; break;
                        case '-': result = num1 - num2; break;
                        case '*': result = num1 * num2; break;
                        case '/': result = num1 / num2; break;
                        case '^': result = Math.pow(num1, num2); break;
                    }
                    historyList.add(num1 + " " + operator + " " + num2 + " = " + result);
                    updateHistory();
                    display.setText(String.valueOf(result));
                    break;

                case "Dashboard":
                    new Dashboard();
                    dispose();
                    break;
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Invalid input!", "Error",
                JOptionPane.ERROR_MESSAGE);
        }
    }

    private double parse() {
        return Double.parseDouble(display.getText());
    }

    private void updateHistory() {
        historyArea.setText("");
        for (String h : historyList) historyArea.append(h + "\n\n");
    }
}
