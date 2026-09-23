import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;
import java.awt.*;
import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * AttendanceManager – lets students track per-subject attendance,
 * instantly see if they are in the SAFE / WARNING / DANGER zone,
 * and find out exactly how many classes they can skip OR must attend.
 */
public class AttendanceManager extends JFrame {

    // ── Data ──────────────────────────────────────────────────────
    private ArrayList<SubjectAttendance> subjects = new ArrayList<>();
    private static final double REQUIRED_PCT = 75.0;
    private static final String HISTORY_FILE = "Attendance_History.txt";

    // ── UI Components ─────────────────────────────────────────────
    private JPanel subjectContainer;
    private JLabel overallLabel;
    private JLabel statusLabel;
    private JLabel adviceLabel;
    private JProgressBar overallBar;

    // ── Student info fields ───────────────────────────────────────
    private JTextField nameField;
    private JTextField rollField;
    private JTextField semField;

    public AttendanceManager() {

        setTitle("Attendance Manager");
        setSize(1100, 750);
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

        JLabel icon = new JLabel("  📅");
        icon.setFont(new Font("Segoe UI", Font.PLAIN, 36));

        JLabel title = new JLabel("Attendance Manager");
        title.setFont(Theme.FONT_TITLE);
        title.setForeground(Theme.ACCENT_GREEN);

        JLabel sub = new JLabel("  Track • Analyse • Stay Safe");
        sub.setFont(Theme.FONT_SMALL);
        sub.setForeground(Theme.TEXT_SECONDARY);

        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 20));
        left.setOpaque(false);
        left.add(icon);
        left.add(title);
        left.add(sub);

        // Student info in header
        nameField = Theme.makeField("Your Name");
        rollField = Theme.makeField("Roll No");
        semField  = Theme.makeField("Semester");
        nameField.setPreferredSize(new Dimension(140, 36));
        rollField.setPreferredSize(new Dimension(110, 36));
        semField.setPreferredSize(new Dimension(90, 36));

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 22));
        right.setOpaque(false);
        right.add(makeSmallLabel("Name:"));  right.add(nameField);
        right.add(makeSmallLabel("Roll:"));  right.add(rollField);
        right.add(makeSmallLabel("Sem:"));   right.add(semField);

        header.add(left, BorderLayout.WEST);
        header.add(right, BorderLayout.EAST);
        add(header, BorderLayout.NORTH);
    }

    // ══════════════════════════════════════════════════════════════
    //  MAIN CONTENT
    // ══════════════════════════════════════════════════════════════
    private void buildMain() {

        JPanel main = new JPanel(new BorderLayout(15, 15));
        main.setBackground(Theme.BG_DARK);
        main.setBorder(new EmptyBorder(15, 20, 10, 20));

        // ── Subject list (scrollable) ──────────────────────────
        subjectContainer = new JPanel();
        subjectContainer.setLayout(new BoxLayout(subjectContainer, BoxLayout.Y_AXIS));
        subjectContainer.setBackground(Theme.BG_DARK);

        JScrollPane scroll = new JScrollPane(subjectContainer);
        scroll.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(Theme.ACCENT_GREEN, 1),
            "  Subjects  ", TitledBorder.LEFT, TitledBorder.TOP,
            Theme.FONT_BOLD, Theme.ACCENT_GREEN));
        scroll.getViewport().setBackground(Theme.BG_DARK);
        scroll.setBackground(Theme.BG_DARK);

        // ── Summary panel (right side) ─────────────────────────
        JPanel summaryPanel = buildSummaryPanel();
        summaryPanel.setPreferredSize(new Dimension(300, 0));

        main.add(scroll, BorderLayout.CENTER);
        main.add(summaryPanel, BorderLayout.EAST);

        add(main, BorderLayout.CENTER);
    }

    private JPanel buildSummaryPanel() {

        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(Theme.BG_PANEL);
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Theme.BORDER, 1),
            new EmptyBorder(20, 15, 20, 15)));

        JLabel heading = new JLabel("📊 Overall Summary");
        heading.setFont(Theme.FONT_HEADER);
        heading.setForeground(Theme.ACCENT_GREEN);
        heading.setAlignmentX(Component.LEFT_ALIGNMENT);

        overallLabel = new JLabel("Overall: --");
        overallLabel.setFont(new Font("Segoe UI", Font.BOLD, 32));
        overallLabel.setForeground(Theme.TEXT_PRIMARY);
        overallLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        statusLabel = new JLabel("Status: Not calculated");
        statusLabel.setFont(Theme.FONT_BOLD);
        statusLabel.setForeground(Theme.TEXT_SECONDARY);
        statusLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        overallBar = new JProgressBar(0, 100);
        overallBar.setStringPainted(true);
        overallBar.setFont(Theme.FONT_BOLD);
        overallBar.setMaximumSize(new Dimension(Integer.MAX_VALUE, 28));
        overallBar.setAlignmentX(Component.LEFT_ALIGNMENT);

        JSeparator sep = new JSeparator();
        sep.setForeground(Theme.BORDER);
        sep.setMaximumSize(new Dimension(Integer.MAX_VALUE, 2));
        sep.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel adviceHeading = new JLabel("💡 Smart Advice");
        adviceHeading.setFont(Theme.FONT_BOLD);
        adviceHeading.setForeground(Theme.ACCENT_GOLD);
        adviceHeading.setAlignmentX(Component.LEFT_ALIGNMENT);

        adviceLabel = new JLabel("<html><i>Click 'Calculate' to see personalised advice.</i></html>");
        adviceLabel.setFont(Theme.FONT_SMALL);
        adviceLabel.setForeground(Theme.TEXT_SECONDARY);
        adviceLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Zone legend
        JPanel legend = buildLegend();
        legend.setAlignmentX(Component.LEFT_ALIGNMENT);

        panel.add(heading);
        panel.add(Box.createVerticalStrut(15));
        panel.add(overallLabel);
        panel.add(Box.createVerticalStrut(8));
        panel.add(overallBar);
        panel.add(Box.createVerticalStrut(8));
        panel.add(statusLabel);
        panel.add(Box.createVerticalStrut(20));
        panel.add(sep);
        panel.add(Box.createVerticalStrut(15));
        panel.add(adviceHeading);
        panel.add(Box.createVerticalStrut(10));
        panel.add(adviceLabel);
        panel.add(Box.createVerticalStrut(20));
        panel.add(sep);
        panel.add(Box.createVerticalStrut(15));
        panel.add(legend);

        return panel;
    }

    private JPanel buildLegend() {
        JPanel p = new JPanel(new GridLayout(3, 1, 5, 5));
        p.setOpaque(false);
        p.add(legendEntry("🟢 SAFE ZONE", "≥ 75% attendance", Theme.SAFE));
        p.add(legendEntry("🟡 WARNING ZONE", "65% – 74%", Theme.WARNING));
        p.add(legendEntry("🔴 DANGER ZONE", "< 65%", Theme.DANGER));
        return p;
    }

    private JPanel legendEntry(String title, String desc, Color color) {
        JPanel p = new JPanel(new BorderLayout(5, 0));
        p.setOpaque(false);
        JLabel t = new JLabel(title);
        t.setFont(Theme.FONT_SMALL);
        t.setForeground(color);
        JLabel d = new JLabel(desc);
        d.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        d.setForeground(Theme.TEXT_MUTED);
        p.add(t, BorderLayout.NORTH);
        p.add(d, BorderLayout.CENTER);
        return p;
    }

    // ══════════════════════════════════════════════════════════════
    //  BOTTOM BUTTONS
    // ══════════════════════════════════════════════════════════════
    private void buildBottom() {

        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.CENTER, 12, 10));
        bottom.setBackground(Theme.BG_PANEL);
        bottom.setBorder(BorderFactory.createMatteBorder(2, 0, 0, 0, Theme.BORDER));

        JButton addBtn    = Theme.makeButton("+ Add Subject", Theme.ACCENT_GREEN);
        JButton calcBtn   = Theme.makeButton("⚡ Calculate", Theme.ACCENT_GOLD);
        JButton historyBtn= Theme.makeButton("📂 History", Theme.ACCENT_BLUE);
        JButton resetBtn  = Theme.makeButton("🔄 Reset", Theme.ACCENT_RED);
        JButton backBtn   = Theme.makeButton("🏠 Dashboard", Theme.BG_CARD);
        backBtn.setBorder(BorderFactory.createLineBorder(Theme.ACCENT_GREEN, 1));

        addBtn.addActionListener(e -> addSubject("", "", 0, 0));

        calcBtn.addActionListener(e -> calculate());

        historyBtn.addActionListener(e -> showHistory());

        resetBtn.addActionListener(e -> reset());

        backBtn.addActionListener(e -> {
            new Dashboard();
            dispose();
        });

        for (JButton b : new JButton[]{addBtn, calcBtn, historyBtn, resetBtn, backBtn}) {
            b.setPreferredSize(new Dimension(148, 40));
            bottom.add(b);
        }

        add(bottom, BorderLayout.SOUTH);
    }

    // ══════════════════════════════════════════════════════════════
    //  SUBJECT CARD
    // ══════════════════════════════════════════════════════════════
    private void addSubject(String name, String total, int attended, int missing) {

        SubjectAttendance card = new SubjectAttendance(name,
                total.isEmpty() ? "" : total,
                attended, missing);
        subjects.add(card);
        subjectContainer.add(card);
        subjectContainer.add(Box.createVerticalStrut(8));
        subjectContainer.revalidate();
        subjectContainer.repaint();
    }

    class SubjectAttendance extends JPanel {

        JTextField subjectName;
        JTextField totalField;
        JTextField attendedField;
        JLabel percentLabel;
        JLabel statusDot;
        JProgressBar bar;

        SubjectAttendance(String name, String total, int attended, int missing) {

            setLayout(new GridBagLayout());
            setBackground(Theme.BG_CARD);
            setMaximumSize(new Dimension(Integer.MAX_VALUE, 70));
            setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Theme.BORDER, 1),
                new EmptyBorder(8, 12, 8, 12)));

            GridBagConstraints gbc = new GridBagConstraints();
            gbc.insets = new Insets(0, 6, 0, 6);
            gbc.anchor = GridBagConstraints.WEST;
            gbc.fill   = GridBagConstraints.HORIZONTAL;

            // Subject name
            subjectName = Theme.makeField("Subject Name");
            subjectName.setText(name);
            subjectName.setPreferredSize(new Dimension(180, 34));

            // Total classes
            totalField = Theme.makeField("Total");
            totalField.setText(total);
            totalField.setPreferredSize(new Dimension(70, 34));

            // Attended
            attendedField = Theme.makeField("Present");
            attendedField.setText(attended > 0 ? String.valueOf(attended) : "");
            attendedField.setPreferredSize(new Dimension(70, 34));

            // Progress bar
            bar = new JProgressBar(0, 100);
            bar.setStringPainted(false);
            bar.setBackground(Theme.BG_FIELD);
            bar.setPreferredSize(new Dimension(120, 14));

            // Percent label
            percentLabel = new JLabel("-- %");
            percentLabel.setFont(Theme.FONT_BOLD);
            percentLabel.setForeground(Theme.TEXT_PRIMARY);
            percentLabel.setPreferredSize(new Dimension(55, 20));

            // Status dot
            statusDot = new JLabel("●");
            statusDot.setFont(new Font("Segoe UI", Font.BOLD, 22));
            statusDot.setForeground(Theme.TEXT_MUTED);

            // Remove button
            JButton remove = new JButton("✕");
            remove.setBackground(Theme.ACCENT_RED);
            remove.setForeground(Color.WHITE);
            remove.setFont(new Font("Segoe UI", Font.BOLD, 13));
            remove.setFocusPainted(false);
            remove.setBorderPainted(false);
            remove.setPreferredSize(new Dimension(36, 34));
            remove.addActionListener(e -> {
                subjects.remove(this);
                Container parent = getParent();
                int idx = -1;
                Component[] comps = parent.getComponents();
                for (int i = 0; i < comps.length; i++) {
                    if (comps[i] == this) { idx = i; break; }
                }
                parent.remove(this);
                // remove the spacer strut below it
                if (idx >= 0 && idx < parent.getComponentCount()) {
                    parent.remove(idx);
                }
                parent.revalidate();
                parent.repaint();
            });

            // Layout
            gbc.gridx = 0; gbc.weightx = 2.5; add(subjectName, gbc);
            gbc.gridx = 1; gbc.weightx = 0;   add(makeSmallLabel("Total:"), gbc);
            gbc.gridx = 2; gbc.weightx = 0.5; add(totalField, gbc);
            gbc.gridx = 3; gbc.weightx = 0;   add(makeSmallLabel("Present:"), gbc);
            gbc.gridx = 4; gbc.weightx = 0.5; add(attendedField, gbc);
            gbc.gridx = 5; gbc.weightx = 1.5; add(bar, gbc);
            gbc.gridx = 6; gbc.weightx = 0;   add(percentLabel, gbc);
            gbc.gridx = 7; gbc.weightx = 0;   add(statusDot, gbc);
            gbc.gridx = 8; gbc.weightx = 0;   add(remove, gbc);
        }

        /** Update the row's bar / percent / dot after calculation */
        void refresh(double pct) {
            bar.setValue((int) pct);
            percentLabel.setText(String.format("%.1f%%", pct));
            if (pct >= REQUIRED_PCT) {
                bar.setForeground(Theme.SAFE);
                statusDot.setForeground(Theme.SAFE);
            } else if (pct >= 65) {
                bar.setForeground(Theme.WARNING);
                statusDot.setForeground(Theme.WARNING);
            } else {
                bar.setForeground(Theme.DANGER);
                statusDot.setForeground(Theme.DANGER);
            }
        }
    }

    // ══════════════════════════════════════════════════════════════
    //  CALCULATE
    // ══════════════════════════════════════════════════════════════
    private void calculate() {

        if (subjects.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                "Please add at least one subject first.",
                "No Subjects", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int totalClasses  = 0;
        int totalPresent  = 0;
        StringBuilder detail = new StringBuilder();

        for (SubjectAttendance s : subjects) {
            try {
                int tot = Integer.parseInt(s.totalField.getText().trim());
                int att = Integer.parseInt(s.attendedField.getText().trim());

                if (att > tot) {
                    JOptionPane.showMessageDialog(this,
                        "Present cannot exceed Total for: " + s.subjectName.getText(),
                        "Input Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                double pct = tot == 0 ? 0 : (att * 100.0 / tot);
                s.refresh(pct);

                totalClasses += tot;
                totalPresent += att;

                // Per-subject advice
                detail.append("<b>").append(s.subjectName.getText()).append("</b> — ")
                      .append(String.format("%.1f%%", pct)).append(" → ");

                if (pct >= REQUIRED_PCT) {
                    // How many more can skip?
                    int canSkip = (int) Math.floor((att - 0.75 * tot) / 0.75);
                    detail.append("<font color='#10b981'>✅ Safe. Can skip ").append(canSkip).append(" more class(es).</font>");
                } else if (pct >= 65) {
                    // How many must attend consecutively?
                    int need = (int) Math.ceil((0.75 * tot - att) / 0.25);
                    detail.append("<font color='#f59e0b'>⚠️ Warning! Attend next ").append(need).append(" class(es) to reach 75%.</font>");
                } else {
                    int need = (int) Math.ceil((0.75 * tot - att) / 0.25);
                    detail.append("<font color='#ef4444'>🚨 Danger! Need ").append(need).append(" consecutive class(es) — talk to HOD.</font>");
                }
                detail.append("<br>");

            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this,
                    "Enter valid numbers for: " + s.subjectName.getText(),
                    "Input Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
        }

        double overall = totalClasses == 0 ? 0 : (totalPresent * 100.0 / totalClasses);

        overallLabel.setText(String.format("%.1f%%", overall));
        overallBar.setValue((int) overall);

        if (overall >= REQUIRED_PCT) {
            overallLabel.setForeground(Theme.SAFE);
            overallBar.setForeground(Theme.SAFE);
            statusLabel.setForeground(Theme.SAFE);
            statusLabel.setText("🟢  SAFE ZONE — You're good!");

            int canSkip = (int) Math.floor((totalPresent - 0.75 * totalClasses) / 0.75);
            adviceLabel.setText("<html>" + detail +
                "<br><b>Overall:</b> You can afford to miss <b>" + canSkip + "</b> more class(es) total.</html>");

        } else if (overall >= 65) {
            overallLabel.setForeground(Theme.WARNING);
            overallBar.setForeground(Theme.WARNING);
            statusLabel.setForeground(Theme.WARNING);
            statusLabel.setText("🟡  WARNING — Attend regularly!");

            int need = (int) Math.ceil((0.75 * totalClasses - totalPresent) / 0.25);
            adviceLabel.setText("<html>" + detail +
                "<br><b>Overall:</b> Attend the next <b>" + need + "</b> class(es) without fail to reach 75%.</html>");

        } else {
            overallLabel.setForeground(Theme.DANGER);
            overallBar.setForeground(Theme.DANGER);
            statusLabel.setForeground(Theme.DANGER);
            statusLabel.setText("🔴  DANGER — Visit your HOD!");

            int need = (int) Math.ceil((0.75 * totalClasses - totalPresent) / 0.25);
            adviceLabel.setText("<html>" + detail +
                "<br><b>Overall:</b> Need <b>" + need + "</b> consecutive full-attendance class(es) to recover.</html>");
        }

        saveHistory(overall);
    }

    // ══════════════════════════════════════════════════════════════
    //  HISTORY
    // ══════════════════════════════════════════════════════════════
    private void saveHistory(double overall) {
        try (FileWriter fw = new FileWriter(HISTORY_FILE, true)) {
            String date = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss").format(new Date());
            fw.write("Date    : " + date + "\n");
            fw.write("Name    : " + nameField.getText() + "\n");
            fw.write("Roll No : " + rollField.getText() + "\n");
            fw.write("Sem     : " + semField.getText() + "\n");
            for (SubjectAttendance s : subjects) {
                try {
                    int tot = Integer.parseInt(s.totalField.getText().trim());
                    int att = Integer.parseInt(s.attendedField.getText().trim());
                    double pct = tot == 0 ? 0 : att * 100.0 / tot;
                    fw.write(String.format("  %-25s %d/%d  = %.1f%%\n",
                        s.subjectName.getText(), att, tot, pct));
                } catch (Exception ignored) {}
            }
            fw.write(String.format("Overall : %.1f%%\n", overall));
            fw.write("-".repeat(40) + "\n\n");
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                "Could not save history: " + e.getMessage(),
                "File Error", JOptionPane.WARNING_MESSAGE);
        }
    }

    private void showHistory() {
        File f = new File(HISTORY_FILE);
        if (!f.exists()) {
            JOptionPane.showMessageDialog(this,
                "No history found yet. Calculate first!",
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
            sp.setPreferredSize(new Dimension(520, 420));
            JOptionPane.showMessageDialog(this, sp, "Attendance History",
                JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error reading history.", "Error",
                JOptionPane.ERROR_MESSAGE);
        }
    }

    // ══════════════════════════════════════════════════════════════
    //  RESET
    // ══════════════════════════════════════════════════════════════
    private void reset() {
        subjects.clear();
        subjectContainer.removeAll();
        subjectContainer.revalidate();
        subjectContainer.repaint();
        overallLabel.setText("Overall: --");
        overallLabel.setForeground(Theme.TEXT_PRIMARY);
        statusLabel.setText("Status: Not calculated");
        statusLabel.setForeground(Theme.TEXT_SECONDARY);
        overallBar.setValue(0);
        adviceLabel.setText("<html><i>Click 'Calculate' to see personalised advice.</i></html>");
        adviceLabel.setForeground(Theme.TEXT_SECONDARY);
    }

    // ══════════════════════════════════════════════════════════════
    //  DEFAULT SUBJECTS (CUTM CSE Sem 3 sample)
    // ══════════════════════════════════════════════════════════════
    private void loadDefaultSubjects() {
        String[][] data = {
            {"DSA",             "40", "35"},
            {"DBMS",            "38", "30"},
            {"Cloud AWS",       "20", "14"},
            {"Python",          "36", "28"},
            {"Design Thinking", "20", "18"},
            {"Prompt Engg",     "18", "10"},
        };
        for (String[] d : data) {
            addSubject(d[0], d[1], Integer.parseInt(d[2]), 0);
        }
    }

    // ══════════════════════════════════════════════════════════════
    //  UTIL
    // ══════════════════════════════════════════════════════════════
    private JLabel makeSmallLabel(String text) {
        JLabel l = new JLabel(text);
        l.setFont(Theme.FONT_SMALL);
        l.setForeground(Theme.TEXT_SECONDARY);
        return l;
    }
}
