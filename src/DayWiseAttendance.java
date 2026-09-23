import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * DayWiseAttendance – Log attendance date-by-date.
 * Each day you can mark P/A for each subject, and the app
 * auto-calculates your running percentage per subject.
 */
public class DayWiseAttendance extends JFrame {

    // ── Subjects (user-configurable) ──────────────────────────────
    private ArrayList<String> subjects = new ArrayList<>();
    // ── Log: Map<date, Map<subject, "P"/"A">> ─────────────────────
    private TreeMap<String, HashMap<String, String>> log = new TreeMap<>();
    private static final String LOG_FILE = "DayWise_Log.dat";

    // ── UI ─────────────────────────────────────────────────────────
    private JTextField dateField;
    private JPanel subjectMarkPanel;
    private JTable summaryTable;
    private DefaultTableModel summaryModel;
    private JTable logTable;
    private DefaultTableModel logModel;

    // Per-day mark buttons (subject -> button)
    private LinkedHashMap<String, JToggleButton> markBtns = new LinkedHashMap<>();

    private static final SimpleDateFormat SDF = new SimpleDateFormat("dd-MM-yyyy");

    public DayWiseAttendance() {
        setTitle("📅 Day-Wise Attendance");
        setSize(1150, 760);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout());
        getContentPane().setBackground(Theme.BG_DARK);

        loadDefaultSubjects();
        buildHeader();
        buildMain();
        buildBottom();
        loadLog();
        refreshAll();
        setVisible(true);
    }

    // ══════════════════════════════════════════════════════════════
    //  HEADER
    // ══════════════════════════════════════════════════════════════
    private void buildHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(Theme.BG_PANEL);
        header.setPreferredSize(new Dimension(0, 75));
        header.setBorder(BorderFactory.createMatteBorder(0, 0, 2, 0, Theme.ACCENT_GREEN));

        JLabel icon  = new JLabel("  📅");
        icon.setFont(new Font("Segoe UI", Font.PLAIN, 36));
        JLabel title = new JLabel("Day-Wise Attendance Tracker");
        title.setFont(Theme.FONT_TITLE);
        title.setForeground(Theme.ACCENT_GREEN);
        JLabel sub   = new JLabel("  Log daily • See running %" );
        sub.setFont(Theme.FONT_SMALL);
        sub.setForeground(Theme.TEXT_SECONDARY);

        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 18));
        left.setOpaque(false);
        left.add(icon); left.add(title); left.add(sub);

        JButton backBtn = Theme.makeButton("← Dashboard", Theme.ACCENT_BLUE);
        backBtn.addActionListener(e -> { new Dashboard(); dispose(); });
        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 20));
        right.setOpaque(false); right.add(backBtn);

        header.add(left, BorderLayout.WEST);
        header.add(right, BorderLayout.EAST);
        add(header, BorderLayout.NORTH);
    }

    // ══════════════════════════════════════════════════════════════
    //  MAIN – left: mark panel | right: summary + log
    // ══════════════════════════════════════════════════════════════
    private void buildMain() {
        JPanel main = new JPanel(new BorderLayout(15, 0));
        main.setBackground(Theme.BG_DARK);
        main.setBorder(new EmptyBorder(14, 14, 10, 14));

        // ── LEFT: Date entry + subject marking ────────────────────
        JPanel left = buildMarkPanel();
        left.setPreferredSize(new Dimension(340, 0));

        // ── RIGHT: tabbed summary + log ───────────────────────────
        JTabbedPane tabs = new JTabbedPane();
        tabs.setBackground(Theme.BG_DARK);
        tabs.setForeground(Theme.ACCENT_GREEN);
        tabs.setFont(Theme.FONT_BOLD);
        tabs.addTab("📊 Subject Summary", buildSummaryPanel());
        tabs.addTab("📋 Full Log",         buildLogPanel());

        main.add(left, BorderLayout.WEST);
        main.add(tabs, BorderLayout.CENTER);
        add(main, BorderLayout.CENTER);
    }

    private JPanel buildMarkPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(Theme.BG_PANEL);
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Theme.ACCENT_GREEN, 1),
            new EmptyBorder(16, 14, 16, 14)));

        JLabel heading = new JLabel("📌 Mark Attendance");
        heading.setFont(Theme.FONT_HEADER);
        heading.setForeground(Theme.ACCENT_GREEN);
        heading.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Date field
        dateField = new JTextField(SDF.format(new Date()));
        dateField.setFont(Theme.FONT_BODY);
        dateField.setBackground(Theme.BG_FIELD);
        dateField.setForeground(Theme.TEXT_PRIMARY);
        dateField.setCaretColor(Theme.ACCENT_GREEN);
        dateField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Theme.BORDER),
            BorderFactory.createEmptyBorder(6, 10, 6, 10)));
        dateField.setAlignmentX(Component.LEFT_ALIGNMENT);
        dateField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));

        JLabel dateLbl = fl("Date (DD-MM-YYYY):");

        // Subject mark buttons
        subjectMarkPanel = new JPanel();
        subjectMarkPanel.setLayout(new BoxLayout(subjectMarkPanel, BoxLayout.Y_AXIS));
        subjectMarkPanel.setBackground(Theme.BG_PANEL);
        subjectMarkPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        buildMarkButtons();

        JButton saveBtn = Theme.makeButton("💾 Save Day", Theme.ACCENT_GREEN);
        saveBtn.setAlignmentX(Component.LEFT_ALIGNMENT);
        saveBtn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        saveBtn.addActionListener(e -> saveDay());

        JButton todayBtn = Theme.makeButton("📅 Jump to Today", Theme.ACCENT_BLUE);
        todayBtn.setAlignmentX(Component.LEFT_ALIGNMENT);
        todayBtn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        todayBtn.addActionListener(e -> {
            dateField.setText(SDF.format(new Date()));
            loadDayMarks();
        });

        JButton loadBtn = Theme.makeButton("📂 Load Date", new Color(80, 80, 100));
        loadBtn.setAlignmentX(Component.LEFT_ALIGNMENT);
        loadBtn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        loadBtn.addActionListener(e -> loadDayMarks());

        // Mark all P / all A shortcuts
        JPanel shortcut = new JPanel(new GridLayout(1, 2, 8, 0));
        shortcut.setBackground(Theme.BG_PANEL);
        shortcut.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        shortcut.setAlignmentX(Component.LEFT_ALIGNMENT);
        JButton allP = Theme.makeButton("✅ All Present", Theme.SAFE);
        allP.addActionListener(e -> markAll("P"));
        JButton allA = Theme.makeButton("❌ All Absent", Theme.DANGER);
        allA.addActionListener(e -> markAll("A"));
        shortcut.add(allP); shortcut.add(allA);

        panel.add(heading);
        panel.add(Box.createVerticalStrut(14));
        panel.add(dateLbl);
        panel.add(dateField);
        panel.add(Box.createVerticalStrut(10));
        panel.add(new JScrollPane(subjectMarkPanel) {{
            setBackground(Theme.BG_PANEL);
            getViewport().setBackground(Theme.BG_PANEL);
            setBorder(null);
            setMaximumSize(new Dimension(Integer.MAX_VALUE, 260));
            setAlignmentX(Component.LEFT_ALIGNMENT);
        }});
        panel.add(Box.createVerticalStrut(8));
        panel.add(shortcut);
        panel.add(Box.createVerticalStrut(12));
        panel.add(saveBtn);
        panel.add(Box.createVerticalStrut(6));
        panel.add(loadBtn);
        panel.add(Box.createVerticalStrut(6));
        panel.add(todayBtn);

        // ── Add/remove subject section ─────────────────────────────
        panel.add(Box.createVerticalStrut(18));
        JSeparator sep = new JSeparator();
        sep.setForeground(Theme.BORDER);
        sep.setMaximumSize(new Dimension(Integer.MAX_VALUE, 2));
        sep.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(sep);
        panel.add(Box.createVerticalStrut(12));

        JLabel subjHead = new JLabel("⚙️ Manage Subjects");
        subjHead.setFont(Theme.FONT_BOLD);
        subjHead.setForeground(Theme.ACCENT_GOLD);
        subjHead.setAlignmentX(Component.LEFT_ALIGNMENT);

        JTextField newSubj = Theme.makeField("New subject name");
        newSubj.setAlignmentX(Component.LEFT_ALIGNMENT);
        newSubj.setMaximumSize(new Dimension(Integer.MAX_VALUE, 34));

        JButton addSubjBtn = Theme.makeButton("➕ Add Subject", Theme.ACCENT_GOLD);
        addSubjBtn.setAlignmentX(Component.LEFT_ALIGNMENT);
        addSubjBtn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        addSubjBtn.addActionListener(e -> {
            String name = newSubj.getText().trim();
            if (!name.isEmpty() && !subjects.contains(name)) {
                subjects.add(name);
                buildMarkButtons();
                refreshAll();
                newSubj.setText("");
            }
        });

        panel.add(subjHead);
        panel.add(Box.createVerticalStrut(8));
        panel.add(newSubj);
        panel.add(Box.createVerticalStrut(6));
        panel.add(addSubjBtn);

        return panel;
    }

    private void buildMarkButtons() {
        subjectMarkPanel.removeAll();
        markBtns.clear();
        for (String subj : subjects) {
            JPanel row = new JPanel(new BorderLayout(8, 0));
            row.setBackground(Theme.BG_PANEL);
            row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
            row.setAlignmentX(Component.LEFT_ALIGNMENT);

            JLabel subjLbl = new JLabel(subj);
            subjLbl.setFont(Theme.FONT_SMALL);
            subjLbl.setForeground(Theme.TEXT_PRIMARY);
            subjLbl.setPreferredSize(new Dimension(140, 32));

            JToggleButton btn = new JToggleButton("P");
            btn.setFont(new Font("Segoe UI", Font.BOLD, 13));
            btn.setPreferredSize(new Dimension(55, 32));
            btn.setFocusPainted(false);
            btn.setSelected(true); // default: Present
            updateBtnStyle(btn);

            btn.addActionListener(e -> {
                btn.setText(btn.isSelected() ? "P" : "A");
                updateBtnStyle(btn);
            });

            markBtns.put(subj, btn);
            row.add(subjLbl, BorderLayout.WEST);
            row.add(btn, BorderLayout.EAST);
            subjectMarkPanel.add(row);
            subjectMarkPanel.add(Box.createVerticalStrut(4));
        }
        subjectMarkPanel.revalidate();
        subjectMarkPanel.repaint();
    }

    private void updateBtnStyle(JToggleButton btn) {
        if (btn.isSelected()) {
            btn.setBackground(Theme.SAFE);
            btn.setForeground(Color.WHITE);
        } else {
            btn.setBackground(Theme.DANGER);
            btn.setForeground(Color.WHITE);
        }
    }

    private void markAll(String mark) {
        for (Map.Entry<String, JToggleButton> e : markBtns.entrySet()) {
            JToggleButton btn = e.getValue();
            btn.setSelected(mark.equals("P"));
            btn.setText(mark);
            updateBtnStyle(btn);
        }
    }

    private JPanel buildSummaryPanel() {
        String[] cols = {"Subject", "Total Classes", "Present", "Absent", "Percentage", "Status"};
        summaryModel = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        summaryTable = new JTable(summaryModel);
        styleTable(summaryTable, new Color[]{
            Theme.ACCENT_BLUE, Theme.SAFE, Theme.DANGER, Theme.ACCENT_GOLD, Theme.TEXT_PRIMARY, Theme.TEXT_SECONDARY
        });

        summaryTable.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            public Component getTableCellRendererComponent(JTable t, Object val,
                    boolean sel, boolean focus, int row, int col) {
                Component c = super.getTableCellRendererComponent(t, val, sel, focus, row, col);
                c.setBackground(sel ? new Color(30, 80, 60) : (row % 2 == 0 ? Theme.BG_DARK : Theme.BG_CARD));
                if (col == 4 && val != null) {
                    try {
                        double pct = Double.parseDouble(val.toString().replace("%", ""));
                        if (pct >= 75)      ((JLabel)c).setForeground(Theme.SAFE);
                        else if (pct >= 65) ((JLabel)c).setForeground(Theme.WARNING);
                        else               ((JLabel)c).setForeground(Theme.DANGER);
                    } catch (Exception ignored) { ((JLabel)c).setForeground(Theme.TEXT_PRIMARY); }
                } else {
                    ((JLabel)c).setForeground(Theme.TEXT_PRIMARY);
                }
                ((JLabel)c).setBorder(new EmptyBorder(0, 10, 0, 10));
                return c;
            }
        });

        JScrollPane sp = new JScrollPane(summaryTable);
        sp.getViewport().setBackground(Theme.BG_DARK);
        sp.setBorder(null);

        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(Theme.BG_DARK);
        p.add(sp, BorderLayout.CENTER);
        return p;
    }

    private JPanel buildLogPanel() {
        String[] cols = {"Date"};
        // Cols will be rebuilt dynamically in refreshAll()
        logModel = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        logTable = new JTable(logModel);
        styleTable(logTable, null);

        JScrollPane sp = new JScrollPane(logTable);
        sp.getViewport().setBackground(Theme.BG_DARK);
        sp.setBorder(null);

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 6));
        buttons.setBackground(Theme.BG_PANEL);
        JButton delDay = Theme.makeButton("🗑️ Delete Date", Theme.ACCENT_RED);
        delDay.addActionListener(e -> deleteSelectedDay());
        JButton exportBtn = Theme.makeButton("📁 Export Log", Theme.ACCENT_BLUE);
        exportBtn.addActionListener(e -> exportLog());
        buttons.add(delDay); buttons.add(exportBtn);

        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(Theme.BG_DARK);
        p.add(sp, BorderLayout.CENTER);
        p.add(buttons, BorderLayout.SOUTH);
        return p;
    }

    // ══════════════════════════════════════════════════════════════
    //  BOTTOM
    // ══════════════════════════════════════════════════════════════
    private void buildBottom() {
        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 8));
        bottom.setBackground(Theme.BG_PANEL);
        bottom.setBorder(BorderFactory.createMatteBorder(2, 0, 0, 0, Theme.BORDER));

        JButton clearLog = Theme.makeButton("🧹 Clear All Log", Theme.ACCENT_RED);
        clearLog.addActionListener(e -> {
            int confirm = JOptionPane.showConfirmDialog(this,
                "Delete ALL attendance logs?", "Confirm", JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                log.clear(); saveLog(); refreshAll();
            }
        });

        JLabel hint = new JLabel("💡 Tip: Double-click a date in Full Log to load it.");
        hint.setFont(Theme.FONT_SMALL);
        hint.setForeground(Theme.TEXT_MUTED);

        bottom.add(clearLog);
        bottom.add(Box.createHorizontalStrut(20));
        bottom.add(hint);
        add(bottom, BorderLayout.SOUTH);
    }

    // ══════════════════════════════════════════════════════════════
    //  SAVE / LOAD DAY
    // ══════════════════════════════════════════════════════════════
    private void saveDay() {
        String date = dateField.getText().trim();
        try { SDF.parse(date); } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Invalid date format (DD-MM-YYYY).", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        HashMap<String, String> dayMap = new HashMap<>();
        for (Map.Entry<String, JToggleButton> entry : markBtns.entrySet()) {
            dayMap.put(entry.getKey(), entry.getValue().getText());
        }
        log.put(date, dayMap);
        saveLog();
        refreshAll();
        JOptionPane.showMessageDialog(this, "Attendance saved for " + date + "!", "Saved", JOptionPane.INFORMATION_MESSAGE);
    }

    private void loadDayMarks() {
        String date = dateField.getText().trim();
        HashMap<String, String> dayMap = log.get(date);
        if (dayMap == null) {
            JOptionPane.showMessageDialog(this, "No record found for " + date, "Not Found", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        for (Map.Entry<String, JToggleButton> entry : markBtns.entrySet()) {
            String mark = dayMap.getOrDefault(entry.getKey(), "A");
            entry.getValue().setText(mark);
            entry.getValue().setSelected(mark.equals("P"));
            updateBtnStyle(entry.getValue());
        }
    }

    // ══════════════════════════════════════════════════════════════
    //  REFRESH TABLES
    // ══════════════════════════════════════════════════════════════
    private void refreshAll() {
        refreshSummary();
        refreshLog();
    }

    private void refreshSummary() {
        summaryModel.setRowCount(0);
        for (String subj : subjects) {
            int total = 0, present = 0;
            for (HashMap<String, String> dayMap : log.values()) {
                String mark = dayMap.get(subj);
                if (mark != null) {
                    total++;
                    if (mark.equals("P")) present++;
                }
            }
            int absent = total - present;
            double pct = total == 0 ? 0 : present * 100.0 / total;
            String status;
            if (total == 0)     status = "No data";
            else if (pct >= 75) status = "🟢 Safe";
            else if (pct >= 65) status = "🟡 Warning";
            else                status = "🔴 Danger";

            summaryModel.addRow(new Object[]{
                subj, total, present, absent,
                total == 0 ? "--" : String.format("%.1f%%", pct), status
            });
        }
    }

    private void refreshLog() {
        // Rebuild columns: Date + one per subject
        String[] cols = new String[subjects.size() + 1];
        cols[0] = "Date";
        for (int i = 0; i < subjects.size(); i++) cols[i + 1] = subjects.get(i);
        logModel = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        logTable.setModel(logModel);
        styleTable(logTable, null);

        // Custom renderer for P/A cells
        logTable.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            public Component getTableCellRendererComponent(JTable t, Object val,
                    boolean sel, boolean focus, int row, int col) {
                Component c = super.getTableCellRendererComponent(t, val, sel, focus, row, col);
                c.setBackground(sel ? new Color(30, 80, 60) : (row % 2 == 0 ? Theme.BG_DARK : Theme.BG_CARD));
                if (col > 0 && val != null) {
                    if (val.toString().equals("P"))       { ((JLabel)c).setForeground(Theme.SAFE); }
                    else if (val.toString().equals("A")) { ((JLabel)c).setForeground(Theme.DANGER); }
                    else                                  { ((JLabel)c).setForeground(Theme.TEXT_MUTED); }
                } else {
                    ((JLabel)c).setForeground(Theme.TEXT_PRIMARY);
                }
                ((JLabel)c).setHorizontalAlignment(SwingConstants.CENTER);
                ((JLabel)c).setBorder(new EmptyBorder(0, 6, 0, 6));
                return c;
            }
        });

        for (Map.Entry<String, HashMap<String, String>> entry : log.descendingMap().entrySet()) {
            Object[] row = new Object[subjects.size() + 1];
            row[0] = entry.getKey();
            HashMap<String, String> dayMap = entry.getValue();
            for (int i = 0; i < subjects.size(); i++) {
                row[i + 1] = dayMap.getOrDefault(subjects.get(i), "-");
            }
            logModel.addRow(row);
        }

        // Double-click to load into mark panel
        logTable.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    int row = logTable.getSelectedRow();
                    if (row >= 0) {
                        dateField.setText((String) logModel.getValueAt(row, 0));
                        loadDayMarks();
                    }
                }
            }
        });
    }

    // ══════════════════════════════════════════════════════════════
    //  DELETE & EXPORT
    // ══════════════════════════════════════════════════════════════
    private void deleteSelectedDay() {
        int row = logTable.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Select a date to delete.", "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }
        String date = (String) logModel.getValueAt(row, 0);
        int confirm = JOptionPane.showConfirmDialog(this,
            "Delete attendance for " + date + "?", "Confirm", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            log.remove(date); saveLog(); refreshAll();
        }
    }

    private void exportLog() {
        String fname = "DayWise_Export_" + new SimpleDateFormat("ddMMyyyy_HHmm").format(new Date()) + ".txt";
        try (PrintWriter pw = new PrintWriter(new FileWriter(fname))) {
            pw.println("Day-Wise Attendance Export");
            pw.println("Generated: " + new SimpleDateFormat("dd-MM-yyyy HH:mm").format(new Date()));
            pw.println("=".repeat(60));
            // header
            pw.printf("%-14s", "Date");
            for (String s : subjects) pw.printf("%-18s", s);
            pw.println();
            pw.println("-".repeat(60));
            for (Map.Entry<String, HashMap<String, String>> entry : log.entrySet()) {
                pw.printf("%-14s", entry.getKey());
                for (String s : subjects) pw.printf("%-18s", entry.getValue().getOrDefault(s, "-"));
                pw.println();
            }
            pw.println("=".repeat(60));
            // summary
            for (String subj : subjects) {
                int tot = 0, pres = 0;
                for (HashMap<String, String> d : log.values()) {
                    if (d.containsKey(subj)) { tot++; if (d.get(subj).equals("P")) pres++; }
                }
                pw.printf("%s: %d/%d = %.1f%%\n", subj, pres, tot,
                    tot == 0 ? 0 : pres * 100.0 / tot);
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Export failed: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        JOptionPane.showMessageDialog(this, "Exported to " + fname, "Export Done", JOptionPane.INFORMATION_MESSAGE);
    }

    // ══════════════════════════════════════════════════════════════
    //  PERSISTENCE
    // ══════════════════════════════════════════════════════════════
    private void saveLog() {
        try (PrintWriter pw = new PrintWriter(new FileWriter(LOG_FILE))) {
            // Save subjects list
            pw.println("SUBJECTS:" + String.join(",", subjects));
            for (Map.Entry<String, HashMap<String, String>> entry : log.entrySet()) {
                StringBuilder line = new StringBuilder(entry.getKey() + "|");
                for (Map.Entry<String, String> m : entry.getValue().entrySet())
                    line.append(m.getKey()).append("=").append(m.getValue()).append(";");
                pw.println(line);
            }
        } catch (Exception ignored) {}
    }

    private void loadLog() {
        File f = new File(LOG_FILE);
        if (!f.exists()) return;
        try (BufferedReader br = new BufferedReader(new FileReader(f))) {
            String line;
            boolean first = true;
            while ((line = br.readLine()) != null) {
                if (line.startsWith("SUBJECTS:")) {
                    if (first) { // Only load subjects from file if default load skipped
                        String[] saved = line.substring(9).split(",");
                        for (String s : saved) if (!subjects.contains(s)) subjects.add(s);
                        buildMarkButtons();
                        first = false;
                    }
                    continue;
                }
                int idx = line.indexOf('|');
                if (idx < 0) continue;
                String date = line.substring(0, idx);
                String rest = line.substring(idx + 1);
                HashMap<String, String> dayMap = new HashMap<>();
                for (String pair : rest.split(";")) {
                    String[] kv = pair.split("=");
                    if (kv.length == 2) dayMap.put(kv[0], kv[1]);
                }
                log.put(date, dayMap);
            }
        } catch (Exception ignored) {}
    }

    // ══════════════════════════════════════════════════════════════
    //  HELPERS
    // ══════════════════════════════════════════════════════════════
    private void loadDefaultSubjects() {
        subjects.add("DSA");
        subjects.add("DBMS");
        subjects.add("Cloud AWS");
        subjects.add("Python");
        subjects.add("Design Thinking");
        subjects.add("Prompt Engg");
    }

    private void styleTable(JTable t, Color[] headerColors) {
        t.setBackground(Theme.BG_DARK);
        t.setForeground(Theme.TEXT_PRIMARY);
        t.setFont(Theme.FONT_SMALL);
        t.setRowHeight(34);
        t.setGridColor(Theme.BORDER);
        t.setSelectionBackground(new Color(30, 80, 60));
        t.setShowHorizontalLines(true);
        t.setShowVerticalLines(false);
        t.setFillsViewportHeight(true);

        JTableHeader th = t.getTableHeader();
        th.setBackground(Theme.BG_PANEL);
        th.setForeground(Theme.ACCENT_GREEN);
        th.setFont(Theme.FONT_BOLD);
        th.setBorder(BorderFactory.createLineBorder(Theme.BORDER));
    }

    private JLabel fl(String text) {
        JLabel l = new JLabel(text);
        l.setFont(Theme.FONT_SMALL);
        l.setForeground(Theme.TEXT_SECONDARY);
        l.setAlignmentX(Component.LEFT_ALIGNMENT);
        return l;
    }
}
