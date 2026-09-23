public class Main {
    public static void main(String[] args) {
        // Use system look and feel for native rendering, then override with theme
        try {
            javax.swing.UIManager.setLookAndFeel(
                javax.swing.UIManager.getCrossPlatformLookAndFeelClassName());
        } catch (Exception ignored) {}

        new SplashScreen();
    }
}
