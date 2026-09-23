import java.awt.*;
import javax.swing.*;

/**
 * Centralized theme constants for Smart Student Access Platform.
 * Dark emerald-green theme with gold accents.
 */
public class Theme {

    // ── Primary palette ──────────────────────────────────────────
    public static final Color BG_DARK       = new Color(13, 27, 42);    // deep navy-black
    public static final Color BG_PANEL      = new Color(20, 40, 60);    // dark navy panel
    public static final Color BG_CARD       = new Color(26, 51, 74);    // card bg
    public static final Color BG_FIELD      = new Color(15, 32, 50);    // input field bg

    public static final Color ACCENT_GREEN  = new Color(16, 185, 129);  // emerald green
    public static final Color ACCENT_LIGHT  = new Color(52, 211, 153);  // lighter emerald
    public static final Color ACCENT_GOLD   = new Color(245, 158, 11);  // gold/amber
    public static final Color ACCENT_RED    = new Color(239, 68, 68);   // danger red
    public static final Color ACCENT_BLUE   = new Color(59, 130, 246);  // info blue

    public static final Color TEXT_PRIMARY  = new Color(241, 245, 249); // near-white
    public static final Color TEXT_SECONDARY= new Color(148, 163, 184); // muted grey
    public static final Color TEXT_MUTED    = new Color(71, 85, 105);   // dim text

    public static final Color BORDER        = new Color(30, 60, 90);    // subtle border

    // ── Fonts ─────────────────────────────────────────────────────
    public static final Font FONT_TITLE  = new Font("Segoe UI", Font.BOLD,  28);
    public static final Font FONT_HEADER = new Font("Segoe UI", Font.BOLD,  20);
    public static final Font FONT_BODY   = new Font("Segoe UI", Font.PLAIN, 15);
    public static final Font FONT_BOLD   = new Font("Segoe UI", Font.BOLD,  15);
    public static final Font FONT_SMALL  = new Font("Segoe UI", Font.PLAIN, 13);
    public static final Font FONT_LARGE  = new Font("Segoe UI", Font.BOLD,  40);

    // ── Safe / Warning / Danger zones ────────────────────────────
    public static final Color SAFE    = new Color(16, 185, 129);   // ≥75%
    public static final Color WARNING = new Color(245, 158, 11);   // 65–74%
    public static final Color DANGER  = new Color(239, 68, 68);    // <65%

    // ── Helper: themed rounded button ────────────────────────────
    public static JButton makeButton(String text, Color bg) {
        JButton btn = new JButton(text);
        btn.setBackground(bg);
        btn.setForeground(Color.WHITE);
        btn.setFont(FONT_BOLD);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return btn;
    }

    // ── Helper: themed text field ─────────────────────────────────
    public static JTextField makeField(String placeholder) {
        JTextField f = new JTextField(placeholder);
        f.setBackground(BG_FIELD);
        f.setForeground(TEXT_PRIMARY);
        f.setCaretColor(ACCENT_GREEN);
        f.setFont(FONT_BODY);
        f.setBorder(javax.swing.BorderFactory.createCompoundBorder(
            javax.swing.BorderFactory.createLineBorder(BORDER, 1),
            javax.swing.BorderFactory.createEmptyBorder(6, 10, 6, 10)));
        return f;
    }

    // ── Helper: section label ─────────────────────────────────────
    public static JLabel sectionLabel(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(FONT_BOLD);
        lbl.setForeground(ACCENT_GREEN);
        return lbl;
    }
}
