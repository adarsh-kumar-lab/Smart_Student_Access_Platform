import java.awt.*;
import javax.swing.*;


public class SplashScreen extends JFrame {

    private JProgressBar progressBar;

    public SplashScreen() {

        setTitle("Smart Student Access Platform");
        setSize(660, 420);
        setLocationRelativeTo(null);
        setUndecorated(true);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(Theme.BG_DARK);

        // ── Decorative top bar ────────────────────────────────────
        JPanel topBar = new JPanel();
        topBar.setBackground(Theme.ACCENT_GREEN);
        topBar.setMaximumSize(new Dimension(Integer.MAX_VALUE, 5));
        panel.add(topBar);

        // ── Logo emoji ────────────────────────────────────────────
        JLabel logo = new JLabel("🎓");
        logo.setFont(new Font("Segoe UI", Font.PLAIN, 70));
        logo.setAlignmentX(Component.CENTER_ALIGNMENT);

        panel.add(Box.createVerticalStrut(35));
        panel.add(logo);

        // ── App title ─────────────────────────────────────────────
        JLabel title = new JLabel("Smart Student Access Platform");
        title.setFont(new Font("Segoe UI", Font.BOLD, 26));
        title.setForeground(Theme.ACCENT_GREEN);
        title.setAlignmentX(Component.CENTER_ALIGNMENT);

        panel.add(Box.createVerticalStrut(12));
        panel.add(title);

        // ── Subtitle ──────────────────────────────────────────────
        JLabel sub = new JLabel("Centurion University of Technology & Management");
        sub.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        sub.setForeground(Theme.TEXT_SECONDARY);
        sub.setAlignmentX(Component.CENTER_ALIGNMENT);

        panel.add(Box.createVerticalStrut(6));
        panel.add(sub);

        // ── Version badge ─────────────────────────────────────────
        JLabel ver = new JLabel("v3.0");
        ver.setFont(new Font("Segoe UI", Font.BOLD, 12));
        ver.setForeground(Theme.ACCENT_GOLD);
        ver.setAlignmentX(Component.CENTER_ALIGNMENT);

        panel.add(Box.createVerticalStrut(4));
        panel.add(ver);

        // ── Developer credits ─────────────────────────────────────
        JLabel devs = new JLabel(
            "Satyam Pandey  •  Adarsh K. Tiwari  •  Manish Das  •  Simpon Sarangi");
        devs.setFont(new Font("Segoe UI", Font.BOLD, 14));
        devs.setForeground(Theme.TEXT_PRIMARY);
        devs.setAlignmentX(Component.CENTER_ALIGNMENT);

        panel.add(Box.createVerticalStrut(28));
        panel.add(devs);

        // ── Progress bar ──────────────────────────────────────────
        progressBar = new JProgressBar(0, 100);
        progressBar.setValue(0);
        progressBar.setStringPainted(true);
        progressBar.setString("Loading...");
        progressBar.setForeground(Theme.ACCENT_GREEN);
        progressBar.setBackground(Theme.BG_PANEL);
        progressBar.setFont(new Font("Segoe UI", Font.BOLD, 12));
        progressBar.setMaximumSize(new Dimension(520, 22));
        progressBar.setAlignmentX(Component.CENTER_ALIGNMENT);

        panel.add(Box.createVerticalStrut(22));
        panel.add(progressBar);

        // ── Bottom accent bar ─────────────────────────────────────
        JPanel botBar = new JPanel();
        botBar.setBackground(Theme.ACCENT_GREEN);
        botBar.setMaximumSize(new Dimension(Integer.MAX_VALUE, 5));

        panel.add(Box.createVerticalStrut(22));
        panel.add(botBar);

        add(panel);
        setVisible(true);
        startLoading();
    }

    private void startLoading() {

        Timer timer = new Timer(25, null);

        timer.addActionListener(e -> {

            int val = progressBar.getValue();

            if (val < 100) {
                progressBar.setValue(val + 1);
                if (val < 30)  progressBar.setString("Initialising modules...");
                else if (val < 60) progressBar.setString("Loading calculators...");
                else if (val < 85) progressBar.setString("Setting up dashboard...");
                else progressBar.setString("Almost ready!");
            } else {
                ((Timer) e.getSource()).stop();
                dispose();
                new Dashboard();
            }
        });

        timer.start();
    }
}
