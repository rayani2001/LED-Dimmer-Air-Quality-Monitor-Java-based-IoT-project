package leddimmer;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;

public class MainWindow extends JFrame {

    private JComboBox<String> portComboBox;
    private JButton refreshButton, connectButton;
    private JLabel statusLabel;

    private JSlider brightnessSlider;
    private JLabel percentLabel;
    private JButton offButton, fullButton;
    private LedPreview ledPreview;

    private JLabel gasValueLabel;
    private JProgressBar gasBar;
    private JLabel gasStatusLabel;

    private JTextArea logArea;
    private SerialManager serialManager;

    public MainWindow() {
        setTitle("LED Dimmer + Air Quality Monitor");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(740, 700);
        setMinimumSize(new Dimension(600, 580));
        setLocationRelativeTo(null);
        getContentPane().setBackground(new Color(14, 18, 24));
        serialManager = new SerialManager(this);
        buildUI();
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) { serialManager.disconnect(); }
        });
    }

    private void buildUI() {
        setLayout(new BorderLayout(8, 8));
        ((JComponent) getContentPane()).setBorder(BorderFactory.createEmptyBorder(10, 12, 10, 12));
        add(buildConnectionPanel(), BorderLayout.NORTH);
        JPanel center = new JPanel(new GridLayout(1, 2, 10, 0));
        center.setOpaque(false);
        center.add(buildLedPanel());
        center.add(buildGasPanel());
        add(center, BorderLayout.CENTER);
        add(buildLogPanel(), BorderLayout.SOUTH);
    }

    private JPanel buildConnectionPanel() {
        JPanel p = darkPanel("🔌  Connection");
        p.setLayout(new FlowLayout(FlowLayout.LEFT, 8, 8));
        portComboBox = new JComboBox<>();
        portComboBox.setPreferredSize(new Dimension(130, 30));
        portComboBox.setBackground(new Color(22, 30, 42));
        portComboBox.setForeground(new Color(180, 200, 220));
        portComboBox.setFont(new Font("Monospaced", Font.PLAIN, 11));
        refreshButton = btn("⟳ Refresh", new Color(40, 65, 90));
        connectButton = btn("Connect", new Color(25, 100, 55));
        statusLabel = new JLabel("● Disconnected");
        statusLabel.setForeground(new Color(180, 55, 55));
        statusLabel.setFont(new Font("Monospaced", Font.BOLD, 11));
        refreshButton.addActionListener(e -> refreshPorts());
        connectButton.addActionListener(e -> toggleConnection());
        p.add(lbl("Port:")); p.add(portComboBox); p.add(refreshButton);
        p.add(connectButton); p.add(Box.createHorizontalStrut(8)); p.add(statusLabel);
        refreshPorts();
        return p;
    }

    private JPanel buildLedPanel() {
        JPanel p = darkPanel("💡  LED Brightness");
        p.setLayout(new BorderLayout(8, 8));
        p.setBorder(BorderFactory.createCompoundBorder(p.getBorder(), BorderFactory.createEmptyBorder(6, 10, 6, 10)));

        ledPreview = new LedPreview();
        ledPreview.setPreferredSize(new Dimension(72, 72));

        percentLabel = new JLabel("0%", SwingConstants.CENTER);
        percentLabel.setFont(new Font("Monospaced", Font.BOLD, 24));
        percentLabel.setForeground(new Color(255, 107, 53));

        JPanel top = new JPanel(new FlowLayout(FlowLayout.CENTER, 18, 4));
        top.setOpaque(false);
        top.add(ledPreview); top.add(percentLabel);

        brightnessSlider = new JSlider(JSlider.VERTICAL, 0, 100, 0);
        brightnessSlider.setMajorTickSpacing(25);
        brightnessSlider.setMinorTickSpacing(5);
        brightnessSlider.setPaintTicks(true);
        brightnessSlider.setPaintLabels(true);
        brightnessSlider.setOpaque(false);
        brightnessSlider.setForeground(new Color(80, 120, 140));
        brightnessSlider.setBackground(new Color(14, 18, 24));
        brightnessSlider.addChangeListener(e -> {
            int v = brightnessSlider.getValue();
            percentLabel.setText(v + "%");
            ledPreview.setBrightness(v);
            if (!brightnessSlider.getValueIsAdjusting()) serialManager.sendBrightness(v);
        });

        offButton = btn("⬛ OFF", new Color(140, 35, 35));
        fullButton = btn("✦ FULL", new Color(25, 100, 55));
        offButton.addActionListener(e -> { brightnessSlider.setValue(0); serialManager.sendBrightness(0); });
        fullButton.addActionListener(e -> { brightnessSlider.setValue(100); serialManager.sendBrightness(100); });

        JPanel btnRow = new JPanel(new GridLayout(1, 2, 8, 0));
        btnRow.setOpaque(false); btnRow.add(offButton); btnRow.add(fullButton);

        JPanel right = new JPanel(new BorderLayout(8, 8));
        right.setOpaque(false); right.add(top, BorderLayout.NORTH); right.add(btnRow, BorderLayout.SOUTH);
        p.add(brightnessSlider, BorderLayout.WEST); p.add(right, BorderLayout.CENTER);
        return p;
    }

    private JPanel buildGasPanel() {
        JPanel p = darkPanel("🌬️  Air Quality");
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));

        gasValueLabel = new JLabel("GAS: --- / 1023");
        gasValueLabel.setFont(new Font("Monospaced", Font.BOLD, 17));
        gasValueLabel.setForeground(new Color(0, 200, 230));
        gasValueLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        gasBar = new JProgressBar(0, 1023);
        gasBar.setPreferredSize(new Dimension(200, 20));
        gasBar.setMaximumSize(new Dimension(Integer.MAX_VALUE, 20));
        gasBar.setForeground(new Color(50, 200, 30));
        gasBar.setBackground(new Color(22, 30, 42));
        gasBar.setBorderPainted(false);

        gasStatusLabel = new JLabel("Waiting for data...");
        gasStatusLabel.setFont(new Font("Dialog", Font.BOLD, 13));
        gasStatusLabel.setForeground(new Color(80, 110, 130));
        gasStatusLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JPanel scale = new JPanel(new GridLayout(1, 3, 4, 0));
        scale.setOpaque(false); scale.setMaximumSize(new Dimension(Integer.MAX_VALUE, 22));
        scale.add(scaleLbl("0–199", new Color(50, 200, 30)));
        scale.add(scaleLbl("200–499", new Color(255, 170, 30)));
        scale.add(scaleLbl("500+", new Color(230, 50, 50)));

        p.add(Box.createVerticalStrut(14));
        p.add(gasValueLabel); p.add(Box.createVerticalStrut(14));
        p.add(gasBar); p.add(Box.createVerticalStrut(10));
        p.add(gasStatusLabel); p.add(Box.createVerticalGlue());
        p.add(scale); p.add(Box.createVerticalStrut(8));
        return p;
    }

    private JPanel buildLogPanel() {
        JPanel p = darkPanel("📋  Serial Log");
        p.setLayout(new BorderLayout(6, 6));
        p.setPreferredSize(new Dimension(0, 145));
        logArea = new JTextArea();
        logArea.setEditable(false);
        logArea.setFont(new Font("Monospaced", Font.PLAIN, 11));
        logArea.setBackground(new Color(6, 9, 14));
        logArea.setForeground(new Color(0, 200, 230));
        logArea.setLineWrap(true);
        JScrollPane scroll = new JScrollPane(logArea);
        scroll.setBorder(BorderFactory.createLineBorder(new Color(22, 35, 50)));
        scroll.getViewport().setBackground(new Color(6, 9, 14));
        JButton clear = btn("Clear", new Color(40, 55, 70));
        clear.addActionListener(e -> logArea.setText(""));
        p.add(scroll, BorderLayout.CENTER); p.add(clear, BorderLayout.EAST);
        return p;
    }

    // Called by SerialManager when GAS:n arrives
    public void updateGasReading(int value) {
        SwingUtilities.invokeLater(() -> {
            gasValueLabel.setText("GAS: " + value + " / 1023");
            gasBar.setValue(value);
            if (value >= 500) {
                gasBar.setForeground(new Color(230, 50, 50));
                gasStatusLabel.setForeground(new Color(230, 50, 50));
                gasStatusLabel.setText("🚨 HIGH LEVEL DETECTED");
            } else if (value >= 200) {
                gasBar.setForeground(new Color(255, 170, 30));
                gasStatusLabel.setForeground(new Color(255, 170, 30));
                gasStatusLabel.setText("⚠ Moderate");
            } else {
                gasBar.setForeground(new Color(50, 200, 30));
                gasStatusLabel.setForeground(new Color(50, 200, 30));
                gasStatusLabel.setText("✓ Clean Air");
            }
        });
    }

    public void log(String msg) {
        SwingUtilities.invokeLater(() -> {
            logArea.append(msg + "\n");
            logArea.setCaretPosition(logArea.getDocument().getLength());
        });
    }

    public void setConnectionStatus(boolean connected, String port) {
        SwingUtilities.invokeLater(() -> {
            if (connected) {
                statusLabel.setText("● Connected: " + port);
                statusLabel.setForeground(new Color(50, 200, 30));
                connectButton.setText("Disconnect");
                connectButton.setBackground(new Color(130, 35, 35));
            } else {
                statusLabel.setText("● Disconnected");
                statusLabel.setForeground(new Color(180, 55, 55));
                connectButton.setText("Connect");
                connectButton.setBackground(new Color(25, 100, 55));
            }
        });
    }

    private void refreshPorts() {
        portComboBox.removeAllItems();
        for (String p : serialManager.getAvailablePorts()) portComboBox.addItem(p);
        if (portComboBox.getItemCount() == 0) portComboBox.addItem("(no ports found)");
    }

    private void toggleConnection() {
        if (serialManager.isConnected()) {
            serialManager.disconnect();
        } else {
            String port = (String) portComboBox.getSelectedItem();
            if (port != null && !port.equals("(no ports found)")) serialManager.connect(port);
            else JOptionPane.showMessageDialog(this, "No COM port selected. Click Refresh.", "No Port", JOptionPane.WARNING_MESSAGE);
        }
    }

    private JPanel darkPanel(String title) {
        JPanel p = new JPanel();
        p.setBackground(new Color(17, 23, 32));
        p.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(26, 38, 55)),
            BorderFactory.createTitledBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4), title,
                TitledBorder.LEFT, TitledBorder.TOP, new Font("Dialog", Font.BOLD, 12), new Color(0, 180, 210))));
        return p;
    }

    private JButton btn(String text, Color bg) {
        JButton b = new JButton(text);
        b.setBackground(bg); b.setForeground(Color.WHITE);
        b.setFont(new Font("Dialog", Font.BOLD, 12));
        b.setFocusPainted(false); b.setBorderPainted(false);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.setPreferredSize(new Dimension(100, 30));
        return b;
    }

    private JLabel lbl(String text) {
        JLabel l = new JLabel(text);
        l.setForeground(new Color(130, 150, 170));
        l.setFont(new Font("Dialog", Font.BOLD, 12));
        return l;
    }

    private JLabel scaleLbl(String text, Color c) {
        JLabel l = new JLabel("● " + text, SwingConstants.CENTER);
        l.setFont(new Font("Monospaced", Font.PLAIN, 9));
        l.setForeground(c); l.setOpaque(false);
        return l;
    }

    // Inner class — glowing LED circle preview
    private static class LedPreview extends JPanel {
        private int brightness = 0;
        public LedPreview() { setOpaque(false); }
        public void setBrightness(int pct) { brightness = pct; repaint(); }

        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int w = getWidth(), h = getHeight();
            int size = Math.min(w, h) - 10;
            int x = (w - size) / 2, y = (h - size) / 2;
            float a = brightness / 100f;
            if (brightness > 0) {
                g2.setColor(new Color(255, 80, 80, Math.min(90, (int)(brightness * 2.2f))));
                g2.fillOval(x - 8, y - 8, size + 16, size + 16);
            }
            g2.setColor(brightness == 0 ? new Color(160, 20, 20)
                : new Color(255, (int)(50 + 160 * a), (int)(50 + 80 * a)));
            g2.fillOval(x, y, size, size);
            g2.setColor(new Color(255, 255, 255, 70));
            g2.fillOval(x + size / 4, y + size / 6, size / 3, size / 4);
            g2.dispose();
        }
    }

    public static void main(String[] args) {
        try { UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName()); } catch (Exception ignored) {}
        SwingUtilities.invokeLater(() -> new MainWindow().setVisible(true));
    }
}
