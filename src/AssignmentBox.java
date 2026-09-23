import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * AssignmentBox – Faculty assignment tracker for students.
 * Add assignments with subject, title, due date, priority and status.
 * Highlights today's dues and overdue tasks automatically.
 */
public class AssignmentBox extends JFrame {

    // ── Data ──────────────────────────────────────────────────────
    private ArrayList<Assignment> assignments = new ArrayList<>();
    private static final String DATA_FILE = "Assignments.dat";

    // ── Table ─────────────────────────────────────────────────────
    private JTable table;
    private DefaultTableModel tableModel;

    // ── Input fields ──────────────────────────────────────────────
    private JTextField titleField;
    private JTextField subjectField;
    private JTextField dueDateField;
    private JTextArea descArea;
    private JComboBox<String> priorityBox;
    private JComboBox<String> statusBox;

    // ── Stats labels ──────────────────────────────────────────────
    private JLabel totalLbl, pendingLbl, doneLbl, overdueLbl;

    private static final String[] COLUMNS = {
        "#", "Subject", "Title", "Due Date", "Priority", "Status", "Days Left"
    };

    public AssignmentBox() {
        setTitle("📋 Faculty Assignment Box");
        setSize(1150, 760);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout());
        getContentPane().setBackground(Theme.BG_DARK);

        buildHeader();
        buildMain();
        buildBottom();

        loadData();
        refreshTable();
        checkTodayDues();
        setVisible(true);
    }

    // ══════════════════════════════════════════════════════════════
    //  HEADER
    // ══════════════════════════════════════════════════════════════
    private void buildHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(Theme.BG_PANEL);
        header.setPreferredSize(new Dimension(0, 75));
        header.setBorder(BorderFactory.createMatteBorder(0, 0, 2, 0, Theme.ACCENT_GOLD));

        JLabel icon = new JLabel("  📋");
        icon.setFont(new Font("Segoe UI", Font.PLAIN, 36));
        JLabel title = new JLabel("Faculty Assignment Box");
        title.setFont(Theme.FONT_TITLE);
        title.setForeground(Theme.ACCENT_GOLD);
        JLabel sub = new JLabel("  Track • Organise • Submit on time");
        sub.setFont(Theme.FONT_SMALL);
        sub.setForeground(Theme.TEXT_SECONDARY);

        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 18));
        left.setOpaque(false);
        left.add(icon); left.add(title); left.add(sub);

        JButton backBtn = Theme.makeButton("← Dashboard", Theme.ACCENT_BLUE);
        backBtn.addActionListener(e -> { saveData(); new Dashboard(); dispose(); });
        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 20));
        right.setOpaque(false);
        right.add(backBtn);

        header.add(left, BorderLayout.WEST);
        header.add(right, BorderLayout.EAST);
        add(header, BorderLayout.NORTH);
    }

    // ══════════════════════════════════════════════════════════════
    //  MAIN (table left + add form right)
    // ══════════════════════════════════════════════════════════════
    private void buildMain() {
        JPanel main = new JPanel(new BorderLayout(15, 0));
        main.setBackground(Theme.BG_DARK);
        main.setBorder(new EmptyBorder(15, 15, 10, 15));

        // ── Stats bar ─────────────────────────────────────────────
        JPanel statsBar = buildStatsBar();

        // ── Table ─────────────────────────────────────────────────
        tableModel = new DefaultTableModel(COLUMNS, 0) {
            public boolean isCellEditable(int row, int col) { return false; }
        };
        table = new JTable(tableModel);
        styleTable();

        table.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) showDetail();
            }
        });

        JScrollPane scroll = new JScrollPane(table);
        scroll.getViewport().setBackground(Theme.BG_DARK);
        scroll.setBorder(BorderFactory.createLineBorder(Theme.BORDER));

        JPanel tablePanel = new JPanel(new BorderLayout(0, 10));
        tablePanel.setBackground(Theme.BG_DARK);
        tablePanel.add(statsBar, BorderLayout.NORTH);
        tablePanel.add(scroll, BorderLayout.CENTER);

        // ── Add Form (right panel) ─────────────────────────────────
        JPanel formPanel = buildFormPanel();
        formPanel.setPreferredSize(new Dimension(290, 0));

        main.add(tablePanel, BorderLayout.CENTER);
        main.add(formPanel, BorderLayout.EAST);
        add(main, BorderLayout.CENTER);
    }

    private JPanel buildStatsBar() {
        JPanel bar = new JPanel(new GridLayout(1, 4, 10, 0));
        bar.setBackground(Theme.BG_DARK);
        bar.setPreferredSize(new Dimension(0, 62));

        totalLbl   = statCard("📚 Total",    "0", Theme.ACCENT_BLUE);
        pendingLbl = statCard("⏳ Pending",  "0", Theme.ACCENT_GOLD);
        doneLbl    = statCard("✅ Done",      "0", Theme.SAFE);
        overdueLbl = statCard("🔴 Overdue",  "0", Theme.DANGER);

        bar.add(totalLbl.getParent());
        bar.add(pendingLbl.getParent());
        bar.add(doneLbl.getParent());
        bar.add(overdueLbl.getParent());
        return bar;
    }

    private JLabel statCard(String label, String val, Color accent) {
        JPanel card = new JPanel(new GridLayout(2, 1));
        card.setBackground(Theme.BG_CARD);
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(accent, 1),
            new EmptyBorder(6, 12, 6, 12)));

        JLabel lbl = new JLabel(label);
        lbl.setFont(Theme.FONT_SMALL);
        lbl.setForeground(Theme.TEXT_SECONDARY);

        JLabel valLbl = new JLabel(val, SwingConstants.LEFT);
        valLbl.setFont(new Font("Segoe UI", Font.BOLD, 22));
        valLbl.setForeground(accent);

        card.add(lbl); card.add(valLbl);
        return valLbl;
    }

    private JPanel buildFormPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(Theme.BG_PANEL);
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Theme.ACCENT_GOLD, 1),
            new EmptyBorder(16, 14, 16, 14)));

        JLabel heading = new JLabel("➕ Add Assignment");
        heading.setFont(Theme.FONT_HEADER);
        heading.setForeground(Theme.ACCENT_GOLD);
        heading.setAlignmentX(Component.LEFT_ALIGNMENT);

        titleField   = styledField("Assignment title");
        subjectField = styledField("Subject / Faculty name");
        dueDateField = styledField("DD-MM-YYYY");

        descArea = new JTextArea(3, 20);
        descArea.setFont(Theme.FONT_SMALL);
        descArea.setBackground(Theme.BG_FIELD);
        descArea.setForeground(Theme.TEXT_PRIMARY);
        descArea.setCaretColor(Theme.ACCENT_GREEN);
        descArea.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Theme.BORDER),
            BorderFactory.createEmptyBorder(6, 8, 6, 8)));
        descArea.setLineWrap(true);
        descArea.setWrapStyleWord(true);
        JScrollPane descScroll = new JScrollPane(descArea);
        descScroll.setAlignmentX(Component.LEFT_ALIGNMENT);
        descScroll.setMaximumSize(new Dimension(Integer.MAX_VALUE, 90));

        priorityBox = new JComboBox<>(new String[]{"🔴 High", "🟡 Medium", "🟢 Low"});
        statusBox   = new JComboBox<>(new String[]{"⏳ Pending", "🔨 In Progress", "✅ Done"});
        styleCombo(priorityBox);
        styleCombo(statusBox);

        JButton addBtn = Theme.makeButton("➕ Add Assignment", Theme.ACCENT_GOLD);
        addBtn.setAlignmentX(Component.LEFT_ALIGNMENT);
        addBtn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        addBtn.addActionListener(e -> addAssignment());

        JButton updateBtn = Theme.makeButton("✏️ Update Selected", Theme.ACCENT_BLUE);
        updateBtn.setAlignmentX(Component.LEFT_ALIGNMENT);
        updateBtn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        updateBtn.addActionListener(e -> updateStatus());

        JButton deleteBtn = Theme.makeButton("🗑️ Delete Selected", Theme.ACCENT_RED);
        deleteBtn.setAlignmentX(Component.LEFT_ALIGNMENT);
        deleteBtn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        deleteBtn.addActionListener(e -> deleteSelected());

        panel.add(heading);
        panel.add(Box.createVerticalStrut(14));
        panel.add(fl("Title:"));          panel.add(titleField);
        panel.add(Box.createVerticalStrut(8));
        panel.add(fl("Subject/Faculty:")); panel.add(subjectField);
        panel.add(Box.createVerticalStrut(8));
        panel.add(fl("Due Date (DD-MM-YYYY):")); panel.add(dueDateField);
        panel.add(Box.createVerticalStrut(8));
        panel.add(fl("Description:"));    panel.add(descScroll);
        panel.add(Box.createVerticalStrut(8));
        panel.add(fl("Priority:"));       panel.add(priorityBox);
        panel.add(Box.createVerticalStrut(8));
        panel.add(fl("Status:"));         panel.add(statusBox);
        panel.add(Box.createVerticalStrut(16));
        panel.add(addBtn);
        panel.add(Box.createVerticalStrut(8));
        panel.add(updateBtn);
        panel.add(Box.createVerticalStrut(8));
        panel.add(deleteBtn);

        return panel;
    }

    // ══════════════════════════════════════════════════════════════
    //  BOTTOM TOOLBAR
    // ══════════════════════════════════════════════════════════════
    private void buildBottom() {
        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 8));
        bottom.setBackground(Theme.BG_PANEL);
        bottom.setBorder(BorderFactory.createMatteBorder(2, 0, 0, 0, Theme.BORDER));

        JButton sortDate = Theme.makeButton("Sort by Date", Theme.ACCENT_GREEN);
        sortDate.addActionListener(e -> { sortByDate(); refreshTable(); });

        JButton sortSubj = Theme.makeButton("Sort by Subject", Theme.ACCENT_BLUE);
        sortSubj.addActionListener(e -> { sortBySubject(); refreshTable(); });

        JButton showAll  = Theme.makeButton("Show All", new Color(100, 100, 120));
        showAll.addActionListener(e -> refreshTable());

        JButton showPend = Theme.makeButton("Show Pending", Theme.ACCENT_GOLD);
        showPend.addActionListener(e -> refreshTable("⏳ Pending"));

        JButton clearDone = Theme.makeButton("Clear Done", Theme.ACCENT_RED);
        clearDone.addActionListener(e -> {
            assignments.removeIf(a -> a.status.contains("Done"));
            saveData(); refreshTable();
        });

        bottom.add(sortDate); bottom.add(sortSubj);
        bottom.add(new JSeparator(SwingConstants.VERTICAL));
        bottom.add(showAll); bottom.add(showPend);
        bottom.add(new JSeparator(SwingConstants.VERTICAL));
        bottom.add(clearDone);

        add(bottom, BorderLayout.SOUTH);
    }

    // ══════════════════════════════════════════════════════════════
    //  TABLE STYLING
    // ══════════════════════════════════════════════════════════════
    private void styleTable() {
        table.setBackground(Theme.BG_DARK);
        table.setForeground(Theme.TEXT_PRIMARY);
        table.setFont(Theme.FONT_SMALL);
        table.setRowHeight(36);
        table.setGridColor(Theme.BORDER);
        table.setSelectionBackground(new Color(30, 80, 60));
        table.setSelectionForeground(Theme.TEXT_PRIMARY);
        table.setShowHorizontalLines(true);
        table.setShowVerticalLines(false);
        table.setFillsViewportHeight(true);

        JTableHeader th = table.getTableHeader();
        th.setBackground(Theme.BG_PANEL);
        th.setForeground(Theme.ACCENT_GREEN);
        th.setFont(Theme.FONT_BOLD);
        th.setBorder(BorderFactory.createLineBorder(Theme.BORDER));

        // Column widths
        int[] widths = {30, 130, 200, 100, 90, 110, 80};
        for (int i = 0; i < widths.length; i++) {
            table.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);
        }

        // Custom row renderer
        table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            public Component getTableCellRendererComponent(JTable t, Object val,
                    boolean sel, boolean focus, int row, int col) {
                Component c = super.getTableCellRendererComponent(t, val, sel, focus, row, col);
                if (sel) {
                    c.setBackground(new Color(30, 80, 60));
                } else {
                    String daysLeft = (String) tableModel.getValueAt(row, 6);
                    String status   = (String) tableModel.getValueAt(row, 5);
                    if (status != null && status.contains("Done"))
                        c.setBackground(new Color(20, 45, 35));
                    else if (daysLeft != null && daysLeft.equals("OVERDUE"))
                        c.setBackground(new Color(50, 20, 20));
                    else if (daysLeft != null && daysLeft.equals("TODAY"))
                        c.setBackground(new Color(50, 40, 10));
                    else
                        c.setBackground(row % 2 == 0 ? Theme.BG_DARK : Theme.BG_CARD);
                }

                // Color days-left column
                if (col == 6 && val != null) {
                    if (val.toString().equals("OVERDUE"))      ((JLabel)c).setForeground(Theme.DANGER);
                    else if (val.toString().equals("TODAY"))   ((JLabel)c).setForeground(Theme.ACCENT_GOLD);
                    else                                        ((JLabel)c).setForeground(Theme.TEXT_SECONDARY);
                } else {
                    ((JLabel)c).setForeground(Theme.TEXT_PRIMARY);
                }
                ((JLabel)c).setBorder(new EmptyBorder(0, 8, 0, 8));
                return c;
            }
        });
    }

    // ══════════════════════════════════════════════════════════════
    //  CRUD OPERATIONS
    // ══════════════════════════════════════════════════════════════
    private void addAssignment() {
        String title   = titleField.getText().trim();
        String subject = subjectField.getText().trim();
        String dueDate = dueDateField.getText().trim();

        if (title.isEmpty() || subject.isEmpty() || dueDate.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                "Please fill in Title, Subject, and Due Date.", "Missing Info",
                JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Validate date format
        try {
            new SimpleDateFormat("dd-MM-yyyy").parse(dueDate);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                "Date format should be DD-MM-YYYY (e.g. 15-08-2025)", "Invalid Date",
                JOptionPane.ERROR_MESSAGE);
            return;
        }

        Assignment a = new Assignment();
        a.id       = assignments.size() + 1;
        a.title    = title;
        a.subject  = subject;
        a.dueDate  = dueDate;
        a.desc     = descArea.getText().trim();
        a.priority = priorityBox.getSelectedItem().toString();
        a.status   = statusBox.getSelectedItem().toString();
        a.addedOn  = new SimpleDateFormat("dd-MM-yyyy").format(new Date());

        assignments.add(a);
        saveData();
        refreshTable();
        clearForm();
        JOptionPane.showMessageDialog(this, "Assignment added!", "Success",
            JOptionPane.INFORMATION_MESSAGE);
    }

    private void updateStatus() {
        int row = table.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Select an assignment first.", "No Selection",
                JOptionPane.WARNING_MESSAGE);
            return;
        }
        String[] statuses = {"⏳ Pending", "🔨 In Progress", "✅ Done"};
        String chosen = (String) JOptionPane.showInputDialog(this,
            "Update status to:", "Update Status",
            JOptionPane.QUESTION_MESSAGE, null, statuses, statuses[0]);
        if (chosen != null) {
            assignments.get(row).status = chosen;
            saveData(); refreshTable();
        }
    }

    private void deleteSelected() {
        int row = table.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Select an assignment to delete.", "No Selection",
                JOptionPane.WARNING_MESSAGE);
            return;
        }
        int confirm = JOptionPane.showConfirmDialog(this,
            "Delete \"" + assignments.get(row).title + "\"?", "Confirm Delete",
            JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            assignments.remove(row);
            saveData(); refreshTable();
        }
    }

    private void showDetail() {
        int row = table.getSelectedRow();
        if (row < 0) return;
        Assignment a = assignments.get(row);
        JOptionPane.showMessageDialog(this,
            "📋 " + a.title + "\n" +
            "Subject  : " + a.subject + "\n" +
            "Due Date : " + a.dueDate + "\n" +
            "Priority : " + a.priority + "\n" +
            "Status   : " + a.status + "\n" +
            "Added On : " + a.addedOn + "\n\n" +
            "Description:\n" + (a.desc.isEmpty() ? "(none)" : a.desc),
            "Assignment Details", JOptionPane.INFORMATION_MESSAGE);
    }

    // ══════════════════════════════════════════════════════════════
    //  TABLE REFRESH
    // ══════════════════════════════════════════════════════════════
    private void refreshTable() { refreshTable(null); }

    private void refreshTable(String filterStatus) {
        tableModel.setRowCount(0);
        int total = 0, pending = 0, done = 0, overdue = 0;

        for (int i = 0; i < assignments.size(); i++) {
            Assignment a = assignments.get(i);
            if (filterStatus != null && !a.status.equals(filterStatus)) continue;

            String daysLeft = getDaysLeft(a.dueDate);
            tableModel.addRow(new Object[]{
                i + 1, a.subject, a.title, a.dueDate, a.priority, a.status, daysLeft
            });

            total++;
            if (a.status.contains("Pending") || a.status.contains("Progress")) pending++;
            if (a.status.contains("Done")) done++;
            if (daysLeft.equals("OVERDUE") && !a.status.contains("Done")) overdue++;
        }

        totalLbl.setText(String.valueOf(total));
        pendingLbl.setText(String.valueOf(pending));
        doneLbl.setText(String.valueOf(done));
        overdueLbl.setText(String.valueOf(overdue));
    }

    private String getDaysLeft(String dateStr) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy");
            Date due   = sdf.parse(dateStr);
            Date today = sdf.parse(sdf.format(new Date()));
            long diff  = (due.getTime() - today.getTime()) / (1000 * 60 * 60 * 24);
            if (diff < 0)  return "OVERDUE";
            if (diff == 0) return "TODAY";
            return diff + " day(s)";
        } catch (Exception e) {
            return "?";
        }
    }

    private void checkTodayDues() {
        long count = assignments.stream().filter(a ->
            getDaysLeft(a.dueDate).equals("TODAY") && !a.status.contains("Done")).count();
        if (count > 0) {
            JOptionPane.showMessageDialog(this,
                "📅 You have " + count + " assignment(s) due TODAY!\nCheck and submit them before the deadline.",
                "⚠️ Due Today Alert", JOptionPane.WARNING_MESSAGE);
        }
    }

    // ══════════════════════════════════════════════════════════════
    //  SORT
    // ══════════════════════════════════════════════════════════════
    private void sortByDate() {
        assignments.sort((a, b) -> {
            try {
                SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy");
                return sdf.parse(a.dueDate).compareTo(sdf.parse(b.dueDate));
            } catch (Exception e) { return 0; }
        });
    }

    private void sortBySubject() {
        assignments.sort(Comparator.comparing(a -> a.subject.toLowerCase()));
    }

    // ══════════════════════════════════════════════════════════════
    //  PERSISTENCE (simple text file)
    // ══════════════════════════════════════════════════════════════
    private void saveData() {
        try (PrintWriter pw = new PrintWriter(new FileWriter(DATA_FILE))) {
            for (Assignment a : assignments) {
                pw.println(a.title    + "|||" + a.subject + "|||" + a.dueDate  + "|||" +
                           a.desc     + "|||" + a.priority + "|||" + a.status  + "|||" + a.addedOn);
            }
        } catch (Exception e) {
            System.err.println("Could not save assignments: " + e.getMessage());
        }
    }

    private void loadData() {
        File f = new File(DATA_FILE);
        if (!f.exists()) return;
        try (BufferedReader br = new BufferedReader(new FileReader(f))) {
            String line;
            int id = 1;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split("\\|\\|\\|", -1);
                if (parts.length >= 7) {
                    Assignment a = new Assignment();
                    a.id       = id++;
                    a.title    = parts[0]; a.subject = parts[1]; a.dueDate  = parts[2];
                    a.desc     = parts[3]; a.priority = parts[4]; a.status  = parts[5];
                    a.addedOn  = parts[6];
                    assignments.add(a);
                }
            }
        } catch (Exception e) {
            System.err.println("Could not load assignments: " + e.getMessage());
        }
    }

    // ══════════════════════════════════════════════════════════════
    //  HELPERS
    // ══════════════════════════════════════════════════════════════
    private void clearForm() {
        titleField.setText(""); subjectField.setText(""); dueDateField.setText(""); descArea.setText("");
        priorityBox.setSelectedIndex(0); statusBox.setSelectedIndex(0);
    }

    private JTextField styledField(String placeholder) {
        JTextField f = new JTextField(placeholder);
        f.setFont(Theme.FONT_SMALL);
        f.setBackground(Theme.BG_FIELD);
        f.setForeground(Theme.TEXT_MUTED);
        f.setCaretColor(Theme.ACCENT_GREEN);
        f.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Theme.BORDER),
            BorderFactory.createEmptyBorder(6, 8, 6, 8)));
        f.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        f.setAlignmentX(Component.LEFT_ALIGNMENT);
        f.addFocusListener(new FocusAdapter() {
            public void focusGained(FocusEvent e) {
                if (f.getText().equals(placeholder)) {
                    f.setText(""); f.setForeground(Theme.TEXT_PRIMARY);
                }
            }
            public void focusLost(FocusEvent e) {
                if (f.getText().isEmpty()) {
                    f.setText(placeholder); f.setForeground(Theme.TEXT_MUTED);
                }
            }
        });
        return f;
    }

    private void styleCombo(JComboBox<String> cb) {
        cb.setBackground(Theme.BG_FIELD);
        cb.setForeground(Theme.TEXT_PRIMARY);
        cb.setFont(Theme.FONT_SMALL);
        cb.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        cb.setAlignmentX(Component.LEFT_ALIGNMENT);
    }

    private JLabel fl(String text) {
        JLabel l = new JLabel(text);
        l.setFont(Theme.FONT_SMALL);
        l.setForeground(Theme.TEXT_SECONDARY);
        l.setAlignmentX(Component.LEFT_ALIGNMENT);
        return l;
    }

    // ══════════════════════════════════════════════════════════════
    //  DATA CLASS
    // ══════════════════════════════════════════════════════════════
    static class Assignment {
        int id;
        String title = "", subject = "", dueDate = "", desc = "",
               priority = "", status = "", addedOn = "";
    }
}
