import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * GuideChatbot – A rule-based student guide chatbot for SSAP.
 * Answers questions about attendance, CGPA, SGPA, assignments,
 * CUTM rules, and general student life queries.
 */
public class GuideChatbot extends JFrame {

    private JPanel chatArea;
    private JTextField inputField;
    private JButton sendBtn;
    private JScrollPane chatScroll;
    private JComboBox<String> quickTopics;

    // ── Quick-topic suggestions ──────────────────────────────────
    private static final String[] QUICK = {
        "💬 Ask me anything...",
        "📊 What is 75% attendance rule?",
        "🎓 How to calculate CGPA?",
        "📝 How to calculate SGPA?",
        "⚠️ How many classes can I miss?",
        "📅 What is day-wise attendance?",
        "📋 How to add assignments?",
        "🏆 What CGPA for placements?",
        "📚 Grading system at CUTM?",
        "🔬 What does this app have?",
    };

    // ── Knowledge base ────────────────────────────────────────────
    private static final Object[][] KB = {
        // {keywords[], response}
        {new String[]{"75", "attendance rule", "mandatory", "required pct"},
         "📌 <b>75% Attendance Rule at CUTM:</b><br>" +
         "You must attend at least <b>75%</b> of total classes per subject.<br>" +
         "• Below 65% → Danger zone 🔴 (talk to HOD urgently)<br>" +
         "• 65–74% → Warning zone 🟡 (attend every class)<br>" +
         "• 75%+ → Safe zone 🟢 (you're good!)<br>" +
         "Use the <b>Attendance Manager</b> in this app to track it live!"},

        {new String[]{"cgpa", "cumulative grade"},
         "🎓 <b>CGPA = Σ(Credit × Grade Points) / Σ Credits</b><br><br>" +
         "CUTM Grade → Points:<br>" +
         "O=10 | E=9 | A=8 | B=7 | C=6 | D=5 | F=0<br><br>" +
         "Example: 3-credit subject with grade A (8pts) = 24 points.<br>" +
         "Open the <b>CGPA Calculator</b> in this app to compute yours instantly!"},

        {new String[]{"sgpa", "semester grade", "semester gpa"},
         "📝 <b>SGPA</b> = Semester Grade Point Average<br>" +
         "Formula: <b>Σ(Credit × Grade Points) / Σ Credits</b> — same formula as CGPA but for <i>one semester only</i>.<br><br>" +
         "• SGPA covers only the current/selected semester<br>" +
         "• CGPA is the average of all semesters combined<br>" +
         "Use the <b>SGPA Calculator</b> in this app (bottom of navigation)!"},

        {new String[]{"skip", "miss", "bunk", "absent", "how many"},
         "⏭️ <b>How many classes can you skip?</b><br>" +
         "Formula: <b>canSkip = floor((attended − 0.75×total) / 0.75)</b><br><br>" +
         "Example: 40 total, 36 attended (90%) → can skip 6 more.<br>" +
         "But if you're below 75%, you must attend to recover. Use the<br>" +
         "<b>Attendance Manager → Day-Wise tab</b> to see exactly!"},

        {new String[]{"day wise", "daywise", "daily", "date wise"},
         "📅 <b>Day-wise Attendance</b> lets you log attendance by <i>date</i>:<br>" +
         "• Pick a date from the calendar<br>" +
         "• Mark Present (P) or Absent (A) for each subject<br>" +
         "• App auto-calculates your running percentage<br>" +
         "Find it in <b>Attendance Manager → Day-Wise Tab</b>!"},

        {new String[]{"assignment", "faculty", "homework", "task", "submission"},
         "📋 <b>Faculty Assignment Box:</b><br>" +
         "• Add assignments with subject, title, due date & description<br>" +
         "• Status: Pending 🔴 / In Progress 🟡 / Done 🟢<br>" +
         "• Get alerts for due-today assignments<br>" +
         "• Sort by due date or subject<br>" +
         "Open <b>Assignment Box</b> from the Dashboard!"},

        {new String[]{"placement", "job", "company", "cutoff", "7", "8", "9"},
         "💼 <b>CGPA for Placements:</b><br>" +
         "• Most companies: 6.0+ CGPA minimum<br>" +
         "• Good companies (TCS, Infosys, Wipro): 7.0+ CGPA<br>" +
         "• Top companies (Amazon, Google): 8.0+ CGPA<br>" +
         "• Research/higher studies: 8.5+ CGPA recommended<br>" +
         "Check your current CGPA in the <b>CGPA Calculator</b>!"},

        {new String[]{"grade", "grading", "o grade", "e grade", "marking"},
         "📚 <b>CUTM Grading System:</b><br>" +
         "O  = 90–100 marks → 10 points (Outstanding)<br>" +
         "E  = 80–89 marks → 9 points (Excellent)<br>" +
         "A  = 70–79 marks → 8 points (Very Good)<br>" +
         "B  = 60–69 marks → 7 points (Good)<br>" +
         "C  = 50–59 marks → 6 points (Average)<br>" +
         "D  = 45–49 marks → 5 points (Pass)<br>" +
         "F  = <45 marks   → 0 points (Fail / Backlog)"},

        {new String[]{"feature", "app", "what", "tools", "contain"},
         "🔬 <b>SSAP v3.0 Features:</b><br>" +
         "➕ Basic Calculator (with history)<br>" +
         "🔬 Scientific Calculator (sin, cos, log, √, xʸ)<br>" +
         "🎓 CGPA Calculator + Smart Advisor<br>" +
         "📝 SGPA Calculator (new!)<br>" +
         "📅 Attendance Manager + Day-Wise Tracking (new!)<br>" +
         "📋 Faculty Assignment Box (new!)<br>" +
         "🤖 Guide Chatbot — that's me! (new!)<br>" +
         "All in one dark-theme student platform by CUTM!"},

        {new String[]{"backlog", "fail", "f grade", "clear backlog"},
         "⚠️ <b>Clearing Backlogs at CUTM:</b><br>" +
         "• F grade = 0 points, drags CGPA down heavily<br>" +
         "• You can appear in supplementary/back exam next semester<br>" +
         "• Max marks in back exam may be capped — check your department<br>" +
         "• Clear ALL backlogs before final semester for placement eligibility<br>" +
         "Use CGPA Calculator to simulate your CGPA after clearing them!"},

        {new String[]{"hello", "hi", "hey", "namaste", "good morning", "good evening"},
         "👋 <b>Hello! I'm SSAP Guide Bot!</b><br>" +
         "I can help you with:<br>" +
         "• Attendance rules & calculations<br>" +
         "• CGPA / SGPA formulas<br>" +
         "• Assignment tracking tips<br>" +
         "• CUTM grading system<br>" +
         "• App features & navigation<br><br>" +
         "Ask me anything or pick a topic above! 😊"},

        {new String[]{"thank", "thanks", "tysm", "dhanyawad"},
         "😊 You're welcome! Study smart, attend classes, clear backlogs.<br>" +
         "All the best from Team SSAP — Satyam, Adarsh, Manish & Simpon! 🎓"},

        {new String[]{"cutm", "centurion", "university"},
         "🏫 <b>Centurion University of Technology & Management</b><br>" +
         "Located in Odisha, CUTM is known for its industry-integrated curriculum.<br>" +
         "• Credit-based grading system (CBCS)<br>" +
         "• 75% attendance is mandatory<br>" +
         "• Semester exams + internals + practicals<br>" +
         "• Active placement cell with top recruiters<br>" +
         "SSAP is built by CUTM CSE students to help you succeed! 💪"},
    };

    public GuideChatbot() {
        setTitle("🤖 SSAP Guide Chatbot");
        setSize(780, 700);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout());
        getContentPane().setBackground(Theme.BG_DARK);

        buildHeader();
        buildChatArea();
        buildInputArea();

        setVisible(true);
        showWelcome();
    }

    // ══════════════════════════════════════════════════════════════
    //  HEADER
    // ══════════════════════════════════════════════════════════════
    private void buildHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(Theme.BG_PANEL);
        header.setPreferredSize(new Dimension(0, 75));
        header.setBorder(BorderFactory.createMatteBorder(0, 0, 2, 0, Theme.ACCENT_GREEN));

        JLabel icon = new JLabel("  🤖");
        icon.setFont(new Font("Segoe UI", Font.PLAIN, 36));
        JLabel title = new JLabel("SSAP Guide Chatbot");
        title.setFont(Theme.FONT_TITLE);
        title.setForeground(Theme.ACCENT_GREEN);
        JLabel sub = new JLabel("  Your 24/7 Student Assistant");
        sub.setFont(Theme.FONT_SMALL);
        sub.setForeground(Theme.TEXT_SECONDARY);

        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 18));
        left.setOpaque(false);
        left.add(icon); left.add(title); left.add(sub);

        JButton backBtn = Theme.makeButton("← Dashboard", Theme.ACCENT_BLUE);
        backBtn.addActionListener(e -> { new Dashboard(); dispose(); });
        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 20));
        right.setOpaque(false);
        right.add(backBtn);

        header.add(left, BorderLayout.WEST);
        header.add(right, BorderLayout.EAST);
        add(header, BorderLayout.NORTH);
    }

    // ══════════════════════════════════════════════════════════════
    //  CHAT DISPLAY AREA
    // ══════════════════════════════════════════════════════════════
    private void buildChatArea() {
        chatArea = new JPanel();
        chatArea.setLayout(new BoxLayout(chatArea, BoxLayout.Y_AXIS));
        chatArea.setBackground(Theme.BG_DARK);
        chatArea.setBorder(new EmptyBorder(10, 10, 10, 10));

        chatScroll = new JScrollPane(chatArea);
        chatScroll.getViewport().setBackground(Theme.BG_DARK);
        chatScroll.setBorder(null);
        chatScroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        chatScroll.getVerticalScrollBar().setUnitIncrement(16);

        // Quick topics panel
        JPanel topicBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 8));
        topicBar.setBackground(Theme.BG_PANEL);
        topicBar.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, Theme.BORDER));

        JLabel topicLbl = new JLabel("Quick:");
        topicLbl.setFont(Theme.FONT_SMALL);
        topicLbl.setForeground(Theme.TEXT_SECONDARY);
        topicBar.add(topicLbl);

        quickTopics = new JComboBox<>(QUICK);
        quickTopics.setBackground(Theme.BG_CARD);
        quickTopics.setForeground(Theme.TEXT_PRIMARY);
        quickTopics.setFont(Theme.FONT_SMALL);
        quickTopics.setPreferredSize(new Dimension(340, 32));
        quickTopics.addActionListener(e -> {
            int idx = quickTopics.getSelectedIndex();
            if (idx > 0) {
                String q = QUICK[idx].replaceAll("[🤖📊🎓📝⚠️📅📋🏆📚🔬] ", "");
                inputField.setText(q);
                sendMessage();
                quickTopics.setSelectedIndex(0);
            }
        });
        topicBar.add(quickTopics);

        JPanel center = new JPanel(new BorderLayout());
        center.setBackground(Theme.BG_DARK);
        center.add(topicBar, BorderLayout.NORTH);
        center.add(chatScroll, BorderLayout.CENTER);
        add(center, BorderLayout.CENTER);
    }

    // ══════════════════════════════════════════════════════════════
    //  INPUT AREA
    // ══════════════════════════════════════════════════════════════
    private void buildInputArea() {
        JPanel inputPanel = new JPanel(new BorderLayout(8, 0));
        inputPanel.setBackground(Theme.BG_PANEL);
        inputPanel.setBorder(new EmptyBorder(12, 15, 12, 15));
        inputPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(2, 0, 0, 0, Theme.BORDER),
            new EmptyBorder(12, 15, 12, 15)));

        inputField = new JTextField("Type your question here...");
        inputField.setFont(Theme.FONT_BODY);
        inputField.setBackground(Theme.BG_FIELD);
        inputField.setForeground(Theme.TEXT_MUTED);
        inputField.setCaretColor(Theme.ACCENT_GREEN);
        inputField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Theme.BORDER, 1),
            BorderFactory.createEmptyBorder(8, 12, 8, 12)));

        // Placeholder behavior
        inputField.addFocusListener(new FocusAdapter() {
            public void focusGained(FocusEvent e) {
                if (inputField.getText().equals("Type your question here...")) {
                    inputField.setText("");
                    inputField.setForeground(Theme.TEXT_PRIMARY);
                }
            }
            public void focusLost(FocusEvent e) {
                if (inputField.getText().isEmpty()) {
                    inputField.setText("Type your question here...");
                    inputField.setForeground(Theme.TEXT_MUTED);
                }
            }
        });

        inputField.addKeyListener(new KeyAdapter() {
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) sendMessage();
            }
        });

        sendBtn = Theme.makeButton("Send  ➤", Theme.ACCENT_GREEN);
        sendBtn.setPreferredSize(new Dimension(120, 44));
        sendBtn.addActionListener(e -> sendMessage());

        JButton clearBtn = Theme.makeButton("Clear", Theme.ACCENT_RED);
        clearBtn.setPreferredSize(new Dimension(80, 44));
        clearBtn.addActionListener(e -> {
            chatArea.removeAll();
            chatArea.revalidate();
            chatArea.repaint();
            showWelcome();
        });

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        btnPanel.setOpaque(false);
        btnPanel.add(clearBtn);
        btnPanel.add(sendBtn);

        inputPanel.add(inputField, BorderLayout.CENTER);
        inputPanel.add(btnPanel, BorderLayout.EAST);
        add(inputPanel, BorderLayout.SOUTH);
    }

    // ══════════════════════════════════════════════════════════════
    //  MESSAGE SENDING & RESPONSE
    // ══════════════════════════════════════════════════════════════
    private void sendMessage() {
        String text = inputField.getText().trim();
        if (text.isEmpty() || text.equals("Type your question here...")) return;

        addBubble(text, true);
        inputField.setText("");
        inputField.setForeground(Theme.TEXT_PRIMARY);

        // Simulate typing delay
        Timer delay = new Timer(400, e -> {
            String response = getResponse(text.toLowerCase());
            addBubble(response, false);
        });
        delay.setRepeats(false);
        delay.start();
    }

    private String getResponse(String query) {
        for (Object[] entry : KB) {
            String[] keywords = (String[]) entry[0];
            for (String kw : keywords) {
                if (query.contains(kw.toLowerCase())) {
                    return (String) entry[1];
                }
            }
        }
        return "🤔 I'm not sure about that yet, but here's what I can help with:<br>" +
               "• Attendance rules & 75% calculation<br>" +
               "• CGPA / SGPA formulas<br>" +
               "• Assignment tracking<br>" +
               "• CUTM grading system<br>" +
               "Try asking: <i>\"How to calculate CGPA?\"</i> or <i>\"What is 75% rule?\"</i>";
    }

    // ══════════════════════════════════════════════════════════════
    //  CHAT BUBBLE UI
    // ══════════════════════════════════════════════════════════════
    private void addBubble(String text, boolean isUser) {
        JPanel row = new JPanel(new BorderLayout());
        row.setBackground(Theme.BG_DARK);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));

        JLabel bubble = new JLabel();
        bubble.setFont(Theme.FONT_SMALL);
        bubble.setOpaque(true);

        if (isUser) {
            bubble.setText("<html><body style='width:340px;padding:4px'>" + escapeHtml(text) + "</body></html>");
            bubble.setBackground(new Color(30, 80, 60));
            bubble.setForeground(Theme.TEXT_PRIMARY);
            bubble.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Theme.ACCENT_GREEN, 1),
                new EmptyBorder(10, 14, 10, 14)));
            row.add(bubble, BorderLayout.EAST);
        } else {
            bubble.setText("<html><body style='width:400px;padding:4px'>" + text + "</body></html>");
            bubble.setBackground(Theme.BG_PANEL);
            bubble.setForeground(Theme.TEXT_PRIMARY);
            bubble.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Theme.BORDER, 1),
                new EmptyBorder(10, 14, 10, 14)));

            JLabel avatar = new JLabel("🤖");
            avatar.setFont(new Font("Segoe UI", Font.PLAIN, 20));
            avatar.setVerticalAlignment(SwingConstants.TOP);
            avatar.setBorder(new EmptyBorder(8, 4, 0, 8));

            JPanel botRow = new JPanel(new BorderLayout());
            botRow.setBackground(Theme.BG_DARK);
            botRow.add(avatar, BorderLayout.WEST);
            botRow.add(bubble, BorderLayout.CENTER);
            row.add(botRow, BorderLayout.WEST);
        }

        // Timestamp
        String ts = new SimpleDateFormat("hh:mm a").format(new Date());
        JLabel time = new JLabel(ts);
        time.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        time.setForeground(Theme.TEXT_MUTED);
        time.setBorder(new EmptyBorder(2, isUser ? 0 : 40, 6, isUser ? 4 : 0));
        time.setHorizontalAlignment(isUser ? SwingConstants.RIGHT : SwingConstants.LEFT);

        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setBackground(Theme.BG_DARK);
        wrapper.setBorder(new EmptyBorder(4, 8, 0, 8));
        wrapper.add(row, BorderLayout.CENTER);
        wrapper.add(time, BorderLayout.SOUTH);

        chatArea.add(wrapper);
        chatArea.add(Box.createVerticalStrut(4));
        chatArea.revalidate();
        chatArea.repaint();

        // Scroll to bottom
        SwingUtilities.invokeLater(() -> {
            JScrollBar v = chatScroll.getVerticalScrollBar();
            v.setValue(v.getMaximum());
        });
    }

    private String escapeHtml(String s) {
        return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }

    private void showWelcome() {
        addBubble("👋 <b>Hello! I'm your SSAP Guide Bot!</b><br>" +
            "I can answer questions about attendance, CGPA, SGPA, assignments, and CUTM rules.<br>" +
            "Pick a quick topic above or just type your question! 😊", false);
    }
}
