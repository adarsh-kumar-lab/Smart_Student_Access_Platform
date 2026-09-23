import java.awt.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import javax.swing.*;
import javax.swing.border.*;


public class Dashboard extends JFrame {

    private JLabel timeLabel;

    public Dashboard() {

        setTitle("Smart Student Access Platform");
        setSize(1280, 760);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());
        getContentPane().setBackground(Theme.BG_DARK);

        buildHeader();
        buildSidebar();
        buildCards();

        setVisible(true);
    }

    // ══════════════════════════════════════════════════════════════
    //  HEADER
    // ══════════════════════════════════════════════════════════════
    private void buildHeader() {

        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(Theme.BG_PANEL);
        header.setPreferredSize(new Dimension(0, 80));
        header.setBorder(BorderFactory.createMatteBorder(0, 0, 3, 0, Theme.ACCENT_GREEN));

        // Logo + title
        JLabel logo = new JLabel("  🎓");
        logo.setFont(new Font("Segoe UI", Font.PLAIN, 38));

        JLabel title = new JLabel("Smart Student Access Platform");
        title.setFont(Theme.FONT_TITLE);
        title.setForeground(Theme.ACCENT_GREEN);

        JLabel tagline = new JLabel("   Centurion University of Technology & Management");
        tagline.setFont(Theme.FONT_SMALL);
        tagline.setForeground(Theme.TEXT_SECONDARY);

        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 18));
        left.setOpaque(false);
        left.add(logo);
        left.add(title);
        left.add(tagline);

        // Clock
        timeLabel = new JLabel();
        timeLabel.setFont(new Font("Segoe UI", Font.BOLD, 15));
        timeLabel.setForeground(Theme.ACCENT_GREEN);

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 25, 28));
        right.setOpaque(false);
        right.add(new JLabel("🕒") {{
            setFont(new Font("Segoe UI", Font.PLAIN, 18));
        }});
        right.add(timeLabel);

        header.add(left, BorderLayout.WEST);
        header.add(right, BorderLayout.EAST);

        add(header, BorderLayout.NORTH);
        startClock();
    }

    // ══════════════════════════════════════════════════════════════
    //  SIDEBAR
    // ══════════════════════════════════════════════════════════════
    private void buildSidebar() {

        JPanel sidebar = new JPanel();
        sidebar.setPreferredSize(new Dimension(220, 0));
        sidebar.setBackground(Theme.BG_PANEL);
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, Theme.BORDER));

        JLabel menuLbl = new JLabel("NAVIGATION");
        menuLbl.setFont(new Font("Segoe UI", Font.BOLD, 12));
        menuLbl.setForeground(Theme.TEXT_MUTED);
        menuLbl.setAlignmentX(Component.CENTER_ALIGNMENT);

        sidebar.add(Box.createVerticalStrut(20));
        sidebar.add(menuLbl);
        sidebar.add(Box.createVerticalStrut(15));

        String[][] items = {
            {"🏠", "Home"},
            {"➕", "Basic Calculator"},
            {"🔬", "Scientific Calculator"},
            {"🎓", "CGPA Calculator"},
            {"📝", "SGPA Calculator"},
            {"📅", "Attendance Manager"},
            {"🗓️", "Day-Wise Attendance"},
            {"📋", "Assignment Box"},
            {"🤖", "Guide Chatbot"},
            {"ℹ️", "About"},
            {"🚪", "Exit"},
        };

        for (String[] item : items) {
            JButton btn = createSidebarButton(item[0], item[1]);
            sidebar.add(btn);
            sidebar.add(Box.createVerticalStrut(8));
            btn.addActionListener(e -> handleNav(item[1]));
        }

        sidebar.add(Box.createVerticalGlue());

        // Version tag at bottom
        JLabel ver = new JLabel("v4.0  •  CUTM");
        ver.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        ver.setForeground(Theme.TEXT_MUTED);
        ver.setAlignmentX(Component.CENTER_ALIGNMENT);
        sidebar.add(ver);
        sidebar.add(Box.createVerticalStrut(15));

        add(sidebar, BorderLayout.WEST);
    }

    private JButton createSidebarButton(String emoji, String text) {

        JButton btn = new JButton(emoji + "  " + text);
        btn.setMaximumSize(new Dimension(200, 46));
        btn.setAlignmentX(Component.CENTER_ALIGNMENT);
        btn.setHorizontalAlignment(SwingConstants.LEFT);
        btn.setBackground(Theme.BG_CARD);
        btn.setForeground(Theme.TEXT_PRIMARY);
        btn.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        btn.setFocusPainted(false);
        btn.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Theme.BORDER, 1),
            BorderFactory.createEmptyBorder(0, 14, 0, 0)));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        btn.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent e) {
                btn.setBackground(new Color(30, 70, 50));
                btn.setForeground(Theme.ACCENT_GREEN);
            }
            public void mouseExited(java.awt.event.MouseEvent e) {
                btn.setBackground(Theme.BG_CARD);
                btn.setForeground(Theme.TEXT_PRIMARY);
            }
        });

        return btn;
    }

    // ══════════════════════════════════════════════════════════════
    //  FEATURE CARDS (3 × 2 grid)
    // ══════════════════════════════════════════════════════════════
    private void buildCards() {

        JPanel grid = new JPanel(new GridLayout(3, 3, 16, 16));
        grid.setBackground(Theme.BG_DARK);
        grid.setBorder(new EmptyBorder(20, 20, 20, 20));

        grid.add(createCard("➕ Basic Calculator",
            "Addition • Subtraction\nMultiplication • Division\nHistory • Backspace",
            Theme.ACCENT_GREEN, "Basic Calculator"));

        grid.add(createCard("🔬 Scientific Calculator",
            "sin cos tan\nlog  ln  √  x²  xʸ\nπ and e constants",
            Theme.ACCENT_BLUE, "Scientific Calculator"));

        grid.add(createCard("🎓 CGPA Calculator",
            "CUTM credit-based CGPA\nGrade ➜ point conversion\nSmart Advisor included",
            Theme.ACCENT_GOLD, "CGPA Calculator"));

        grid.add(createCard("📝 SGPA Calculator",
            "Semester-wise GPA\nSame grade formula\nWith Smart Advisor",
            new Color(167, 139, 250), "SGPA Calculator")); // purple

        grid.add(createCard("📅 Attendance Manager",
            "Per-subject tracking\nSafe / Warning / Danger zone\nSkip-count advice",
            new Color(52, 211, 153), "Attendance Manager"));

        grid.add(createCard("🗓️ Day-Wise Attendance",
            "Mark P/A by date\nRunning % per subject\nExport log to file",
            new Color(94, 234, 212), "Day-Wise Attendance")); // teal

        grid.add(createCard("📋 Assignment Box",
            "Track faculty assignments\nDue-date alerts\nSort by subject / date",
            Theme.ACCENT_GOLD, "Assignment Box"));

        grid.add(createCard("🤖 Guide Chatbot",
            "Ask about attendance rules\nCGPA / SGPA formulas\nCUTM grading help",
            new Color(251, 146, 60), "Guide Chatbot")); // orange

        grid.add(createCard("🚪 Exit",
            "Close the application\nsafely",
            Theme.ACCENT_RED, "Exit"));

        add(grid, BorderLayout.CENTER);
    }

    private JPanel createCard(String title, String desc, Color accentColor, String action) {

        JPanel card = new JPanel(new BorderLayout(0, 12));
        card.setBackground(Theme.BG_CARD);
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(accentColor, 2, true),
            new EmptyBorder(20, 20, 20, 20)));

        JLabel heading = new JLabel("<html><b>" + title + "</b></html>");
        heading.setFont(new Font("Segoe UI", Font.BOLD, 17));
        heading.setForeground(accentColor);

        JTextArea body = new JTextArea(desc);
        body.setEditable(false);
        body.setOpaque(false);
        body.setFont(Theme.FONT_SMALL);
        body.setForeground(Theme.TEXT_SECONDARY);

        JButton open = Theme.makeButton("OPEN →", accentColor);
        open.setPreferredSize(new Dimension(0, 38));
        open.addActionListener(e -> handleNav(action));

        card.add(heading, BorderLayout.NORTH);
        card.add(body, BorderLayout.CENTER);
        card.add(open, BorderLayout.SOUTH);

        // Hover effect on card
        card.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent e) {
                card.setBackground(new Color(30, 55, 80));
            }
            public void mouseExited(java.awt.event.MouseEvent e) {
                card.setBackground(Theme.BG_CARD);
            }
        });

        return card;
    }

    // ══════════════════════════════════════════════════════════════
    //  NAVIGATION HANDLER
    // ══════════════════════════════════════════════════════════════
    private void handleNav(String target) {
        switch (target) {
            case "Home":
                dispose();
                new Dashboard();
                break;
            case "Basic Calculator":
                new Calculator();
                dispose();
                break;
            case "Scientific Calculator":
                new ScientificCalculator();
                dispose();
                break;
            case "CGPA Calculator":
                new CGPACalculator();
                dispose();
                break;
            case "Attendance Manager":
                new AttendanceManager();
                dispose();
                break;
            case "SGPA Calculator":
                new SGPACalculator();
                dispose();
                break;
            case "Day-Wise Attendance":
                new DayWiseAttendance();
                dispose();
                break;
            case "Assignment Box":
                new AssignmentBox();
                dispose();
                break;
            case "Guide Chatbot":
                new GuideChatbot();
                dispose();
                break;
            case "About":
                JOptionPane.showMessageDialog(this,
                    "🎓 Smart Student Access Platform\n" +
                    "Version 4.0\n\n" +
                    "Developed by:\n" +
                    "  • Satyam Pandey\n" +
                    "  • Adarsh K. Tiwari\n" +
                    "  • Manish Das\n" +
                    "  • Simpon Sarangi\n\n" +
                    "Centurion University of Technology & Management",
                    "About", JOptionPane.INFORMATION_MESSAGE);
                break;
            case "Exit":
                System.exit(0);
                break;
        }
    }

    // ══════════════════════════════════════════════════════════════
    //  CLOCK
    // ══════════════════════════════════════════════════════════════
    private void startClock() {
        new Timer(1000, e ->
            timeLabel.setText(
                new SimpleDateFormat("dd MMM yyyy  hh:mm:ss a").format(new Date()))
        ).start();
    }
}
