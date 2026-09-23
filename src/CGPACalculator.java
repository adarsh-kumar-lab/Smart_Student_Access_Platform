import java.awt.*;
import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;
import javax.swing.*;
import javax.swing.border.*;

/**
 * CGPA Calculator – dark emerald theme with Smart Advisor panel.
 * Tells the student exactly what grades they need to improve / maintain CGPA.
 */
public class CGPACalculator extends JFrame {

    // ── Student fields ────────────────────────────────────────────
    private JTextField nameField;
    private JTextField rollField;
    private JTextField branchField;
    private JTextField semesterField;

    // ── Subject list ──────────────────────────────────────────────
    private JPanel subjectContainer;
    private ArrayList<SubjectCard> subjectList = new ArrayList<>();

    // ── Result labels ─────────────────────────────────────────────
    private JLabel cgpaLabel;
    private JLabel percentageLabel;
    private JLabel creditLabel;
    private JLabel gradeLabel;
    private JLabel advisorArea;
    private JProgressBar cgpaBar;

    // ── Grade → Point map ─────────────────────────────────────────
    private static final Map<String, Integer> GRADE_POINTS = new LinkedHashMap<>();
    static {
        GRADE_POINTS.put("O",  10);
        GRADE_POINTS.put("E",   9);
        GRADE_POINTS.put("A",   8);
        GRADE_POINTS.put("B",   7);
        GRADE_POINTS.put("C",   6);
        GRADE_POINTS.put("D",   5);
        GRADE_POINTS.put("F",   0);
    }

    public CGPACalculator() {

        setTitle("CGPA Calculator");
        setSize(1100, 760);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout());
        getContentPane().setBackground(Theme.BG_DARK);

        buildHeader();
        buildMain();
        buildBottom();

        loadDefaultSubjects();
        setVisible(true);
    }

    // ══════════════════════════════════════════════════════════════
    //  HEADER
    // ══════════════════════════════════════════════════════════════
    private void buildHeader() {

        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(Theme.BG_PANEL);
        header.setPreferredSize(new Dimension(0, 80));
        header.setBorder(BorderFactory.createMatteBorder(0, 0, 2, 0, Theme.ACCENT_GREEN));

        JLabel icon = new JLabel("  🎓");
        icon.setFont(new Font("Segoe UI", Font.PLAIN, 36));

        JLabel title = new JLabel("CGPA Calculator");
        title.setFont(Theme.FONT_TITLE);
        title.setForeground(Theme.ACCENT_GREEN);

        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 20));
        left.setOpaque(false);
        left.add(icon);
        left.add(title);

        // Student info
        nameField   = Theme.makeField("Name");
        rollField   = Theme.makeField("Roll No");
        branchField = Theme.makeField("Branch");
        semesterField = Theme.makeField("Semester");
        for (JTextField f : new JTextField[]{nameField, rollField, branchField, semesterField}) {
            f.setPreferredSize(new Dimension(110, 34));
        }
        branchField.setText("CSE");

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 23));
        right.setOpaque(false);
        right.add(ml("Name:")); right.add(nameField);
        right.add(ml("Roll:")); right.add(rollField);
        right.add(ml("Branch:")); right.add(branchField);
        right.add(ml("Sem:")); right.add(semesterField);

        header.add(left, BorderLayout.WEST);
        header.add(right, BorderLayout.EAST);
        add(header, BorderLayout.NORTH);
    }

    // ══════════════════════════════════════════════════════════════
    //  MAIN
    // ══════════════════════════════════════════════════════════════
    private void buildMain() {

        JPanel main = new JPanel(new BorderLayout(15, 0));
        main.setBackground(Theme.BG_DARK);
        main.setBorder(new EmptyBorder(15, 20, 10, 20));

        // Subject scroll area
        subjectContainer = new JPanel();
        subjectContainer.setLayout(new BoxLayout(subjectContainer, BoxLayout.Y_AXIS));
        subjectContainer.setBackground(Theme.BG_DARK);

        JScrollPane scroll = new JScrollPane(subjectContainer);
        scroll.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(Theme.ACCENT_GREEN, 1),
            "  Subjects  ", TitledBorder.LEFT, TitledBorder.TOP,
            Theme.FONT_BOLD, Theme.ACCENT_GREEN));
        scroll.getViewport().setBackground(Theme.BG_DARK);

        // Result + advisor panel (right)
        JPanel resultPanel = buildResultPanel();
        resultPanel.setPreferredSize(new Dimension(300, 0));

        main.add(scroll, BorderLayout.CENTER);
        main.add(resultPanel, BorderLayout.EAST);
        add(main, BorderLayout.CENTER);
    }

    private JPanel buildResultPanel() {

        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(Theme.BG_PANEL);
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Theme.BORDER, 1),
            new EmptyBorder(18, 15, 18, 15)));

        // CGPA heading
        JLabel heading = new JLabel("📈 Results");
        heading.setFont(Theme.FONT_HEADER);
        heading.setForeground(Theme.ACCENT_GREEN);
        heading.setAlignmentX(Component.LEFT_ALIGNMENT);

        cgpaLabel = new JLabel("CGPA: --");
        cgpaLabel.setFont(new Font("Segoe UI", Font.BOLD, 34));
        cgpaLabel.setForeground(Theme.TEXT_PRIMARY);
        cgpaLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        cgpaBar = new JProgressBar(0, 100);
        cgpaBar.setStringPainted(false);
        cgpaBar.setBackground(Theme.BG_FIELD);
        cgpaBar.setForeground(Theme.ACCENT_GREEN);
        cgpaBar.setMaximumSize(new Dimension(Integer.MAX_VALUE, 18));
        cgpaBar.setAlignmentX(Component.LEFT_ALIGNMENT);

        percentageLabel = new JLabel("Percentage: --");
        percentageLabel.setFont(Theme.FONT_BOLD);
        percentageLabel.setForeground(Theme.TEXT_SECONDARY);
        percentageLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        creditLabel = new JLabel("Total Credits: --");
        creditLabel.setFont(Theme.FONT_BOLD);
        creditLabel.setForeground(Theme.TEXT_SECONDARY);
        creditLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        gradeLabel = new JLabel("Grade Band: --");
        gradeLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        gradeLabel.setForeground(Theme.ACCENT_GOLD);
        gradeLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JSeparator sep = new JSeparator();
        sep.setForeground(Theme.BORDER);
        sep.setMaximumSize(new Dimension(Integer.MAX_VALUE, 2));
        sep.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel advisorTitle = new JLabel("🤖 Smart Advisor");
        advisorTitle.setFont(Theme.FONT_BOLD);
        advisorTitle.setForeground(Theme.ACCENT_GOLD);
        advisorTitle.setAlignmentX(Component.LEFT_ALIGNMENT);

        advisorArea = new JLabel("<html><i>Calculate to see personalised advice.</i></html>");
        advisorArea.setFont(Theme.FONT_SMALL);
        advisorArea.setForeground(Theme.TEXT_SECONDARY);
        advisorArea.setAlignmentX(Component.LEFT_ALIGNMENT);
        advisorArea.setVerticalAlignment(SwingConstants.TOP);

        // Grade scale legend
        JSeparator sep2 = new JSeparator();
        sep2.setForeground(Theme.BORDER);
        sep2.setMaximumSize(new Dimension(Integer.MAX_VALUE, 2));
        sep2.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel scaleTitle = new JLabel("📋 Grade Scale (CUTM)");
        scaleTitle.setFont(Theme.FONT_BOLD);
        scaleTitle.setForeground(Theme.TEXT_SECONDARY);
        scaleTitle.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPanel scale = buildGradeScale();
        scale.setAlignmentX(Component.LEFT_ALIGNMENT);

        panel.add(heading);
        panel.add(Box.createVerticalStrut(12));
        panel.add(cgpaLabel);
        panel.add(Box.createVerticalStrut(6));
        panel.add(cgpaBar);
        panel.add(Box.createVerticalStrut(10));
        panel.add(percentageLabel);
        panel.add(Box.createVerticalStrut(4));
        panel.add(creditLabel);
        panel.add(Box.createVerticalStrut(4));
        panel.add(gradeLabel);
        panel.add(Box.createVerticalStrut(14));
        panel.add(sep);
        panel.add(Box.createVerticalStrut(12));
        panel.add(advisorTitle);
        panel.add(Box.createVerticalStrut(8));
        panel.add(advisorArea);
        panel.add(Box.createVerticalStrut(14));
        panel.add(sep2);
        panel.add(Box.createVerticalStrut(10));
        panel.add(scaleTitle);
        panel.add(Box.createVerticalStrut(6));
        panel.add(scale);

        return panel;
    }

    private JPanel buildGradeScale() {
        JPanel p = new JPanel(new GridLayout(7, 2, 4, 2));
        p.setOpaque(false);
        String[][] rows = {
            {"O  (10)", "≥ 90%"},
            {"E  (9)",  "80–89%"},
            {"A  (8)",  "70–79%"},
            {"B  (7)",  "60–69%"},
            {"C  (6)",  "50–59%"},
            {"D  (5)",  "40–49%"},
            {"F  (0)",  "< 40%"},
        };
        for (String[] r : rows) {
            JLabel g = new JLabel(r[0]);
            JLabel d = new JLabel(r[1]);
            g.setFont(Theme.FONT_SMALL);
            d.setFont(Theme.FONT_SMALL);
            g.setForeground(Theme.ACCENT_LIGHT);
            d.setForeground(Theme.TEXT_MUTED);
            p.add(g);
            p.add(d);
        }
        return p;
    }

    // ══════════════════════════════════════════════════════════════
    //  BOTTOM BUTTONS
    // ══════════════════════════════════════════════════════════════
    private void buildBottom() {

        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.CENTER, 12, 10));
        bottom.setBackground(Theme.BG_PANEL);
        bottom.setBorder(BorderFactory.createMatteBorder(2, 0, 0, 0, Theme.BORDER));

        JButton addBtn   = Theme.makeButton("+ Add Subject", Theme.ACCENT_GREEN);
        JButton calcBtn  = Theme.makeButton("⚡ Calculate CGPA", Theme.ACCENT_GOLD);
        JButton histBtn  = Theme.makeButton("📂 History", Theme.ACCENT_BLUE);
        JButton resetBtn = Theme.makeButton("🔄 Reset", Theme.ACCENT_RED);
        JButton backBtn  = Theme.makeButton("🏠 Dashboard", Theme.BG_CARD);
        backBtn.setBorder(BorderFactory.createLineBorder(Theme.ACCENT_GREEN, 1));

        addBtn.addActionListener(e -> {
            SubjectCard card = new SubjectCard("", "");
            subjectList.add(card);
            subjectContainer.add(card);
            subjectContainer.add(Box.createVerticalStrut(8));
            subjectContainer.revalidate();
            subjectContainer.repaint();
        });

        calcBtn.addActionListener(e -> calculateCGPA());

        histBtn.addActionListener(e -> showHistory());

        resetBtn.addActionListener(e -> resetData());

        backBtn.addActionListener(e -> {
            new Dashboard();
            dispose();
        });

        for (JButton b : new JButton[]{addBtn, calcBtn, histBtn, resetBtn, backBtn}) {
            b.setPreferredSize(new Dimension(160, 40));
            bottom.add(b);
        }

        add(bottom, BorderLayout.SOUTH);
    }

    // ══════════════════════════════════════════════════════════════
    //  SUBJECT CARD
    // ══════════════════════════════════════════════════════════════
    class SubjectCard extends JPanel {

        JTextField subjectName;
        JTextField creditField;
        JComboBox<String> gradeBox;

        SubjectCard(String name, String credit) {

            setLayout(new FlowLayout(FlowLayout.LEFT, 10, 8));
            setBackground(Theme.BG_CARD);
            setMaximumSize(new Dimension(Integer.MAX_VALUE, 60));
            setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Theme.BORDER, 1),
                new EmptyBorder(0, 5, 0, 5)));

            subjectName = Theme.makeField("Subject Name");
            subjectName.setText(name);
            subjectName.setPreferredSize(new Dimension(220, 34));

            creditField = Theme.makeField("Credits");
            creditField.setText(credit);
            creditField.setPreferredSize(new Dimension(70, 34));

            gradeBox = new JComboBox<>(new String[]{"O","E","A","B","C","D","F"});
            gradeBox.setBackground(Theme.BG_FIELD);
            gradeBox.setForeground(Theme.TEXT_PRIMARY);
            gradeBox.setFont(Theme.FONT_BODY);
            gradeBox.setPreferredSize(new Dimension(80, 34));

            JButton remove = new JButton("✕");
            remove.setBackground(Theme.ACCENT_RED);
            remove.setForeground(Color.WHITE);
            remove.setFont(Theme.FONT_BOLD);
            remove.setFocusPainted(false);
            remove.setBorderPainted(false);
            remove.setPreferredSize(new Dimension(36, 34));
            remove.addActionListener(e -> {
                subjectList.remove(this);
                Container parent = getParent();
                int idx = -1;
                Component[] comps = parent.getComponents();
                for (int i = 0; i < comps.length; i++) {
                    if (comps[i] == this) { idx = i; break; }
                }
                parent.remove(this);
                if (idx >= 0 && idx < parent.getComponentCount()) parent.remove(idx);
                parent.revalidate();
                parent.repaint();
            });

            add(ml("Subject:")); add(subjectName);
            add(ml("Credit:")); add(creditField);
            add(ml("Grade:")); add(gradeBox);
            add(remove);
        }
    }

    // ══════════════════════════════════════════════════════════════
    //  CALCULATE
    // ══════════════════════════════════════════════════════════════
    private void calculateCGPA() {

        if (subjectList.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                "Please add subjects first.", "No Subjects",
                JOptionPane.WARNING_MESSAGE);
            return;
        }

        double totalPoints = 0;
        int totalCredits   = 0;
        int failCount      = 0;
        String weakSubject = "";
        int weakGP         = 100;

        for (SubjectCard card : subjectList) {
            try {
                int credit = Integer.parseInt(card.creditField.getText().trim());
                String grade = card.gradeBox.getSelectedItem().toString();
                int point = GRADE_POINTS.get(grade);

                totalPoints  += credit * point;
                totalCredits += credit;

                if (point == 0) failCount++;
                if (point < weakGP) {
                    weakGP = point;
                    weakSubject = card.subjectName.getText();
                }
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this,
                    "Enter valid credit for: " + card.subjectName.getText(),
                    "Input Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
        }

        double cgpa       = totalPoints / totalCredits;
        double percentage = cgpa * 10;

        // Colour-code result
        Color cgpaColor;
        String gradeBand;
        if (cgpa >= 9.0)      { cgpaColor = Theme.SAFE;        gradeBand = "🏆 Outstanding (O)"; }
        else if (cgpa >= 8.0) { cgpaColor = Theme.ACCENT_LIGHT; gradeBand = "⭐ Excellent (E)"; }
        else if (cgpa >= 7.0) { cgpaColor = Theme.ACCENT_BLUE;  gradeBand = "👍 Good (A)"; }
        else if (cgpa >= 6.0) { cgpaColor = Theme.ACCENT_GOLD;  gradeBand = "✔️ Average (B/C)"; }
        else                  { cgpaColor = Theme.DANGER;        gradeBand = "⚠️ Below Average"; }

        cgpaLabel.setText(String.format("CGPA: %.2f", cgpa));
        cgpaLabel.setForeground(cgpaColor);
        cgpaBar.setValue((int)(cgpa * 10));
        cgpaBar.setForeground(cgpaColor);
        percentageLabel.setText(String.format("Percentage: %.2f%%", percentage));
        creditLabel.setText("Total Credits: " + totalCredits);
        gradeLabel.setText("Grade Band: " + gradeBand);

        // ── Smart Advisor ────────────────────────────────────────
        StringBuilder adv = new StringBuilder("<html>");

        if (failCount > 0) {
            adv.append("<font color='#ef4444'><b>⚠️ You have ").append(failCount)
               .append(" backlog(s)! Clear them ASAP — backlogs hold you back.<br></b></font>");
        }

        if (cgpa >= 9.0) {
            adv.append("🏆 Exceptional! You qualify for <b>Dean's List</b> and <b>merit scholarships</b>.<br>");
            adv.append("Consider research projects or internships at top companies.<br>");
        } else if (cgpa >= 8.0) {
            double extra = (9.0 * totalCredits - totalPoints) / totalCredits;
            adv.append("⭐ Great performance! To hit <b>9.0 CGPA</b>, average improvement of <b>")
               .append(String.format("%.1f", extra)).append(" grade points</b> needed across subjects.<br>");
            adv.append("Focus on <b>").append(weakSubject.isEmpty() ? "weakest" : weakSubject).append("</b> first.<br>");
        } else if (cgpa >= 7.0) {
            adv.append("👍 Good standing. Push to <b>8.0+</b> for placement advantage.<br>");
            adv.append("Prioritise: <b>").append(weakSubject.isEmpty() ? "your weakest subject" : weakSubject).append("</b>.<br>");
            adv.append("Attend all classes & ask professors for extra doubt sessions.<br>");
        } else if (cgpa >= 6.0) {
            adv.append("⚠️ Average zone. Many companies filter at <b>7.0 CGPA</b>.<br>");
            adv.append("• Improve <b>").append(weakSubject).append("</b> urgently.<br>");
            adv.append("• Join study groups, use NPTEL & YouTube tutorials.<br>");
            adv.append("• Aim for all-A or better next semester.<br>");
        } else {
            adv.append("🚨 Below average. Seek <b>academic counselling</b> immediately.<br>");
            adv.append("• Meet your faculty advisor this week.<br>");
            adv.append("• Clear all F-grade backlogs before next semester.<br>");
            adv.append("• 7.0 CGPA is achievable — plan semester-by-semester.<br>");
        }

        adv.append("</html>");
        advisorArea.setText(adv.toString());
        advisorArea.setForeground(Theme.TEXT_SECONDARY);

        saveHistory(cgpa, percentage);
    }

    // ══════════════════════════════════════════════════════════════
    //  HISTORY / RESET
    // ══════════════════════════════════════════════════════════════
    private void saveHistory(double cgpa, double percentage) {
        try (FileWriter fw = new FileWriter("CGPA_History.txt", true)) {
            String date = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss").format(new Date());
            fw.write("Date       : " + date + "\n");
            fw.write("Name       : " + nameField.getText() + "\n");
            fw.write("Roll No    : " + rollField.getText() + "\n");
            fw.write("Branch     : " + branchField.getText() + "\n");
            fw.write("Semester   : " + semesterField.getText() + "\n");
            fw.write(String.format("CGPA       : %.2f\n", cgpa));
            fw.write(String.format("Percentage : %.2f%%\n", percentage));
            fw.write("-".repeat(40) + "\n\n");
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                "Could not save history.", "File Error",
                JOptionPane.WARNING_MESSAGE);
        }
    }

    private void showHistory() {
        File f = new File("CGPA_History.txt");
        if (!f.exists()) {
            JOptionPane.showMessageDialog(this,
                "No history found. Calculate CGPA first!",
                "No History", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        try (BufferedReader br = new BufferedReader(new FileReader(f))) {
            JTextArea area = new JTextArea();
            area.setEditable(false);
            area.setBackground(Theme.BG_DARK);
            area.setForeground(Theme.TEXT_PRIMARY);
            area.setFont(new Font("Monospaced", Font.PLAIN, 13));
            String line;
            while ((line = br.readLine()) != null) area.append(line + "\n");
            JScrollPane sp = new JScrollPane(area);
            sp.setPreferredSize(new Dimension(480, 400));
            JOptionPane.showMessageDialog(this, sp, "CGPA History",
                JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error reading history.", "Error",
                JOptionPane.ERROR_MESSAGE);
        }
    }

    private void resetData() {
        nameField.setText(""); rollField.setText("");
        semesterField.setText(""); branchField.setText("CSE");
        subjectList.clear();
        subjectContainer.removeAll();
        subjectContainer.revalidate();
        subjectContainer.repaint();
        cgpaLabel.setText("CGPA: --");
        cgpaLabel.setForeground(Theme.TEXT_PRIMARY);
        cgpaBar.setValue(0);
        percentageLabel.setText("Percentage: --");
        creditLabel.setText("Total Credits: --");
        gradeLabel.setText("Grade Band: --");
        advisorArea.setText("<html><i>Calculate to see personalised advice.</i></html>");
    }

    private void loadDefaultSubjects() {
        String[][] data = {
            {"DSA",             "6", "A"},
            {"DBMS",            "4", "B"},
            {"Cloud AWS",       "2", "E"},
            {"Python",          "4", "A"},
            {"Design Thinking", "2", "O"},
            {"Prompt Engg",     "2", "B"},
            {"SKILL COURSE",  "4", "C"},
        };
        for (String[] d : data) {
            SubjectCard card = new SubjectCard(d[0], d[1]);
            card.gradeBox.setSelectedItem(d[2]);
            subjectList.add(card);
            subjectContainer.add(card);
            subjectContainer.add(Box.createVerticalStrut(8));
        }
    }

    private JLabel ml(String text) {
        JLabel l = new JLabel(text);
        l.setFont(Theme.FONT_SMALL);
        l.setForeground(Theme.TEXT_SECONDARY);
        return l;
    }
}
