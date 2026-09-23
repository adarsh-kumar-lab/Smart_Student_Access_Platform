import java.awt.*;
import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;
import javax.swing.*;
import javax.swing.border.*;

/**
 * SGPACalculator – Calculates SGPA for one semester using CUTM's
 * credit-based grading. Shows grade-point summary and smart advice.
 */
public class SGPACalculator extends JFrame {

    // ── Student fields ─────────────────────────────────────────────
    private JTextField nameField, rollField, branchField, semField;

    // ── Subject list ───────────────────────────────────────────────
    private JPanel subjectContainer;
    private ArrayList<SubjectRow> subjectList = new ArrayList<>();

    // ── Result labels ──────────────────────────────────────────────
    private JLabel sgpaLabel, percentageLabel, creditLabel, gradeLabel, advisorArea;
    private JProgressBar sgpaBar;

    // ── Grade table ────────────────────────────────────────────────
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

    // ── Counter for auto-numbering ─────────────────────────────────
    private int subjectCounter = 1;

    public SGPACalculator() {
        setTitle("📝 SGPA Calculator");
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
        header.setBorder(BorderFactory.createMatteBorder(0, 0, 2, 0, new Color(167, 139, 250)));

        JLabel icon  = new JLabel("  📝");
        icon.setFont(new Font("Segoe UI", Font.PLAIN, 36));
        JLabel title = new JLabel("SGPA Calculator");
        title.setFont(Theme.FONT_TITLE);
        title.setForeground(new Color(167, 139, 250)); // purple

        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 20));
        left.setOpaque(false);
        left.add(icon); left.add(title);

        // Student info row
        nameField   = Theme.makeField("Name");
        rollField   = Theme.makeField("Roll No");
        branchField = Theme.makeField("Branch");
        semField    = Theme.makeField("Semester");
        for (JTextField f : new JTextField[]{nameField, rollField, branchField, semField})
            f.setPreferredSize(new Dimension(110, 34));
        branchField.setText("CSE");

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 23));
        right.setOpaque(false);
        right.add(ml("Name:"));   right.add(nameField);
        right.add(ml("Roll:"));   right.add(rollField);
        right.add(ml("Branch:")); right.add(branchField);
        right.add(ml("Sem:"));    right.add(semField);

        header.add(left,  BorderLayout.WEST);
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

        // Subject scroll
        subjectContainer = new JPanel();
        subjectContainer.setLayout(new BoxLayout(subjectContainer, BoxLayout.Y_AXIS));
        subjectContainer.setBackground(Theme.BG_DARK);

        JScrollPane scroll = new JScrollPane(subjectContainer);
        scroll.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(new Color(167, 139, 250), 1),
            "  Semester Subjects  ", TitledBorder.LEFT, TitledBorder.TOP,
            Theme.FONT_BOLD, new Color(167, 139, 250)));
        scroll.getViewport().setBackground(Theme.BG_DARK);

        // Result panel
        JPanel result = buildResultPanel();
        result.setPreferredSize(new Dimension(300, 0));

        main.add(scroll, BorderLayout.CENTER);
        main.add(result, BorderLayout.EAST);
        add(main, BorderLayout.CENTER);
    }

    private JPanel buildResultPanel() {
        Color purple = new Color(167, 139, 250);

        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(Theme.BG_PANEL);
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Theme.BORDER, 1),
            new EmptyBorder(18, 15, 18, 15)));

        JLabel heading = new JLabel("📊 SGPA Results");
        heading.setFont(Theme.FONT_HEADER);
        heading.setForeground(purple);
        heading.setAlignmentX(Component.LEFT_ALIGNMENT);

        sgpaLabel = new JLabel("SGPA: --");
        sgpaLabel.setFont(new Font("Segoe UI", Font.BOLD, 34));
        sgpaLabel.setForeground(Theme.TEXT_PRIMARY);
        sgpaLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        sgpaBar = new JProgressBar(0, 100);
        sgpaBar.setStringPainted(false);
        sgpaBar.setBackground(Theme.BG_FIELD);
        sgpaBar.setForeground(purple);
        sgpaBar.setMaximumSize(new Dimension(Integer.MAX_VALUE, 18));
        sgpaBar.setAlignmentX(Component.LEFT_ALIGNMENT);

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

        JLabel advTitle = new JLabel("🤖 Smart Advisor");
        advTitle.setFont(Theme.FONT_BOLD);
        advTitle.setForeground(Theme.ACCENT_GOLD);
        advTitle.setAlignmentX(Component.LEFT_ALIGNMENT);

        advisorArea = new JLabel("<html><i>Calculate to see advice.</i></html>");
        advisorArea.setFont(Theme.FONT_SMALL);
        advisorArea.setForeground(Theme.TEXT_SECONDARY);
        advisorArea.setAlignmentX(Component.LEFT_ALIGNMENT);
        advisorArea.setVerticalAlignment(SwingConstants.TOP);

        // Grade legend
        JPanel legend = buildLegend(purple);
        legend.setAlignmentX(Component.LEFT_ALIGNMENT);

        panel.add(heading);
        panel.add(Box.createVerticalStrut(14));
        panel.add(sgpaLabel);
        panel.add(Box.createVerticalStrut(6));
        panel.add(sgpaBar);
        panel.add(Box.createVerticalStrut(8));
        panel.add(percentageLabel);
        panel.add(Box.createVerticalStrut(4));
        panel.add(creditLabel);
        panel.add(Box.createVerticalStrut(8));
        panel.add(gradeLabel);
        panel.add(Box.createVerticalStrut(14));
        panel.add(sep);
        panel.add(Box.createVerticalStrut(12));
        panel.add(advTitle);
        panel.add(Box.createVerticalStrut(8));
        panel.add(advisorArea);
        panel.add(Box.createVerticalStrut(16));
        panel.add(sep);
        panel.add(Box.createVerticalStrut(12));
        panel.add(legend);

        return panel;
    }

    private JPanel buildLegend(Color purple) {
        JPanel p = new JPanel(new GridLayout(7, 2, 4, 3));
        p.setBackground(Theme.BG_CARD);
        p.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Theme.BORDER),
            new EmptyBorder(8, 10, 8, 10)));

        JLabel t = new JLabel("Grade Scale");
        t.setFont(Theme.FONT_BOLD);
        t.setForeground(purple);
        p.add(t); p.add(new JLabel(""));

        String[][] rows = {
            {"O (Outstanding)", "10 pts"},
            {"E (Excellent)",   "9 pts"},
            {"A (Very Good)",   "8 pts"},
            {"B (Good)",        "7 pts"},
            {"C (Average)",     "6 pts"},
            {"D (Pass)",        "5 pts"},
        };
        for (String[] r : rows) {
            JLabel k = new JLabel(r[0]); k.setFont(Theme.FONT_SMALL); k.setForeground(Theme.TEXT_SECONDARY);
            JLabel v = new JLabel(r[1]); v.setFont(Theme.FONT_SMALL); v.setForeground(Theme.ACCENT_LIGHT);
            p.add(k); p.add(v);
        }
        return p;
    }

    // ══════════════════════════════════════════════════════════════
    //  BOTTOM BUTTONS
    // ══════════════════════════════════════════════════════════════
    private void buildBottom() {
        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 8));
        bottom.setBackground(Theme.BG_PANEL);
        bottom.setBorder(BorderFactory.createMatteBorder(2, 0, 0, 0, Theme.BORDER));

        JButton addSubj = Theme.makeButton("➕ Add Subject", new Color(167, 139, 250));
        addSubj.addActionListener(e -> addSubjectRow("", "4"));

        JButton calc = Theme.makeButton("📊 Calculate SGPA", Theme.ACCENT_GREEN);
        calc.addActionListener(e -> calculateSGPA());

        JButton reset = Theme.makeButton("🔄 Reset", Theme.ACCENT_RED);
        reset.addActionListener(e -> resetData());

        JButton hist = Theme.makeButton("📁 History", Theme.ACCENT_BLUE);
        hist.addActionListener(e -> showHistory());

        JButton back = Theme.makeButton("← Dashboard", Theme.ACCENT_BLUE);
        back.addActionListener(e -> { new Dashboard(); dispose(); });

        bottom.add(addSubj); bottom.add(calc); bottom.add(reset); bottom.add(hist);
        bottom.add(Box.createHorizontalStrut(20)); bottom.add(back);
        add(bottom, BorderLayout.SOUTH);
    }

    // ══════════════════════════════════════════════════════════════
    //  SUBJECT ROW
    // ══════════════════════════════════════════════════════════════
    private void addSubjectRow(String name, String credit) {
        SubjectRow row = new SubjectRow(name, credit, subjectCounter++);
        subjectList.add(row);
        subjectContainer.add(row);
        subjectContainer.add(Box.createVerticalStrut(6));
        subjectContainer.revalidate();
        subjectContainer.repaint();
    }

    private class SubjectRow extends JPanel {
        JTextField subjectName, creditField;
        JComboBox<String> gradeBox;

        SubjectRow(String name, String credit, int num) {
            setLayout(new FlowLayout(FlowLayout.LEFT, 10, 8));
            setBackground(Theme.BG_CARD);
            setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(80, 60, 120), 1),
                new EmptyBorder(2, 8, 2, 8)));
            setMaximumSize(new Dimension(Integer.MAX_VALUE, 55));

            JLabel numLbl = new JLabel(num + ".");
            numLbl.setFont(Theme.FONT_BOLD);
            numLbl.setForeground(new Color(167, 139, 250));

            subjectName = Theme.makeField(name.isEmpty() ? "Subject Name" : name);
            subjectName.setPreferredSize(new Dimension(200, 34));
            subjectName.setText(name);

            creditField = Theme.makeField("Credits");
            creditField.setPreferredSize(new Dimension(70, 34));
            creditField.setText(credit);

            gradeBox = new JComboBox<>(GRADE_POINTS.keySet().toArray(new String[0]));
            gradeBox.setBackground(Theme.BG_FIELD);
            gradeBox.setForeground(Theme.TEXT_PRIMARY);
            gradeBox.setFont(Theme.FONT_BODY);
            gradeBox.setPreferredSize(new Dimension(70, 34));

            JLabel gLabel = ml("Grade:");

            JButton remove = Theme.makeButton("✕", Theme.ACCENT_RED);
            remove.setPreferredSize(new Dimension(36, 30));
            remove.setFont(new Font("Segoe UI", Font.BOLD, 12));
            remove.addActionListener(e -> {
                subjectList.remove(this);
                subjectContainer.remove(this);
                subjectContainer.revalidate();
                subjectContainer.repaint();
            });

            add(numLbl); add(subjectName); add(ml("Credits:")); add(creditField); add(gLabel); add(gradeBox); add(remove);
        }
    }

    // ══════════════════════════════════════════════════════════════
    //  CALCULATE
    // ══════════════════════════════════════════════════════════════
    private void calculateSGPA() {
        if (subjectList.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Add subjects first!", "No Subjects",
                JOptionPane.WARNING_MESSAGE);
            return;
        }

        double totalPoints = 0;
        int totalCredits = 0, failCount = 0;
        String weakSubject = "";
        int weakGP = 100;

        for (SubjectRow row : subjectList) {
            try {
                int credit = Integer.parseInt(row.creditField.getText().trim());
                String grade = row.gradeBox.getSelectedItem().toString();
                int point = GRADE_POINTS.get(grade);
                totalPoints  += credit * point;
                totalCredits += credit;
                if (point == 0) failCount++;
                if (point < weakGP) {
                    weakGP = point;
                    weakSubject = row.subjectName.getText();
                }
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this,
                    "Enter valid credit for: " + row.subjectName.getText(),
                    "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
        }

        double sgpa = totalPoints / totalCredits;
        double pct  = sgpa * 10;

        Color c; String band;
        if      (sgpa >= 9.0) { c = Theme.SAFE;         band = "🏆 Outstanding (O)"; }
        else if (sgpa >= 8.0) { c = Theme.ACCENT_LIGHT; band = "⭐ Excellent (E)"; }
        else if (sgpa >= 7.0) { c = Theme.ACCENT_BLUE;  band = "👍 Good (A)"; }
        else if (sgpa >= 6.0) { c = Theme.ACCENT_GOLD;  band = "✔️ Average (B/C)"; }
        else                  { c = Theme.DANGER;        band = "⚠️ Below Average"; }

        sgpaLabel.setText(String.format("SGPA: %.2f", sgpa));
        sgpaLabel.setForeground(c);
        sgpaBar.setValue((int)(sgpa * 10));
        sgpaBar.setForeground(c);
        percentageLabel.setText(String.format("Percentage: %.1f%%", pct));
        creditLabel.setText("Total Credits: " + totalCredits);
        gradeLabel.setText("Grade Band: " + band);

        StringBuilder adv = new StringBuilder("<html>");
        if (failCount > 0)
            adv.append("<font color='#ef4444'><b>⚠️ ").append(failCount)
               .append(" backlog(s)! Appear in supplementary exam.<br></b></font>");
        if (sgpa >= 9.0)
            adv.append("🏆 Exceptional semester! Maintain this for Dean's List.<br>");
        else if (sgpa >= 8.0)
            adv.append("⭐ Excellent! Push ").append(weakSubject).append(" to improve further.<br>");
        else if (sgpa >= 7.0)
            adv.append("👍 Good semester. Focus on ").append(weakSubject).append(" for placement edge.<br>");
        else if (sgpa >= 6.0)
            adv.append("⚠️ Average. Aim for 7.0+ next sem. Study ").append(weakSubject).append(" harder.<br>");
        else
            adv.append("🚨 Below average. See your faculty advisor immediately.<br>");
        adv.append("<br><i>SGPA covers this semester only. CGPA averages all semesters.</i></html>");
        advisorArea.setText(adv.toString());

        saveHistory(sgpa, pct, totalCredits);
    }

    // ══════════════════════════════════════════════════════════════
    //  HISTORY / RESET
    // ══════════════════════════════════════════════════════════════
    private void saveHistory(double sgpa, double pct, int credits) {
        try (FileWriter fw = new FileWriter("SGPA_History.txt", true)) {
            String date = new SimpleDateFormat("dd-MM-yyyy HH:mm").format(new Date());
            fw.write("Date     : " + date + "\n");
            fw.write("Name     : " + nameField.getText() + "\n");
            fw.write("Roll     : " + rollField.getText() + "\n");
            fw.write("Sem      : " + semField.getText() + "\n");
            fw.write(String.format("SGPA     : %.2f  |  Pct: %.1f%%  |  Credits: %d\n", sgpa, pct, credits));
            fw.write("-".repeat(40) + "\n\n");
        } catch (Exception ignored) {}
    }

    private void showHistory() {
        File f = new File("SGPA_History.txt");
        if (!f.exists()) {
            JOptionPane.showMessageDialog(this, "No history yet.", "History", JOptionPane.INFORMATION_MESSAGE);
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
            sp.setPreferredSize(new Dimension(480, 380));
            JOptionPane.showMessageDialog(this, sp, "SGPA History", JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error reading history.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void resetData() {
        subjectList.clear();
        subjectContainer.removeAll();
        subjectContainer.revalidate();
        subjectContainer.repaint();
        subjectCounter = 1;
        sgpaLabel.setText("SGPA: --");
        sgpaLabel.setForeground(Theme.TEXT_PRIMARY);
        sgpaBar.setValue(0);
        percentageLabel.setText("Percentage: --");
        creditLabel.setText("Total Credits: --");
        gradeLabel.setText("Grade Band: --");
        advisorArea.setText("<html><i>Calculate to see advice.</i></html>");
    }

    private void loadDefaultSubjects() {
        String[][] data = {
            {"DSA",             "6", "A"},
            {"DBMS",            "4", "B"},
            {"Cloud AWS",       "2", "E"},
            {"Python",          "4", "A"},
            {"Design Thinking", "2", "O"},
            {"Prompt Engg",     "2", "B"},
        };
        for (String[] d : data) {
            addSubjectRow(d[0], d[1]);
            subjectList.get(subjectList.size() - 1).gradeBox.setSelectedItem(d[2]);
        }
    }

    private JLabel ml(String text) {
        JLabel l = new JLabel(text);
        l.setFont(Theme.FONT_SMALL);
        l.setForeground(Theme.TEXT_SECONDARY);
        return l;
    }
}
