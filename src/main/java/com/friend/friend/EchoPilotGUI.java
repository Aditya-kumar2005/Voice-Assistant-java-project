package com.friend.friend;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;

/**
 * Main GUI class for the EchoPilot Assistant application.
 * It provides a visual interface for a voice assistant, including status display,
 * microphone icon with glow animation, control buttons, and command history.
 */
public class EchoPilotGUI extends JFrame {

    private final JLabel statusLabel;
    private final JButton pauseButton;
    private final JButton resumeButton;
    private final JTextArea commandArea; // Added missing field
    private final BackgroundPanel backgroundPanel; // Added missing field
    private final JLabel spinnerLabel; // Added missing field
    private final GlowPanel glowingMic; // Added missing field
    private Timer glowTimer; // Added missing field

    // --- Constants for Resources ---
    private static final String BACKGROUND_IMAGE_PATH = "/images/back12.png"; // Placeholder path
    private static final String MIC_ICON_PATH = "/images/_mic.png"; // Placeholder path
    private static final String SPINNER_GIF_PATH = "/images/Spinner.gif"; // Placeholder path
    // ---

    public EchoPilotGUI() {
        super("⭐ EchoPilot Assistant");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(400, 300);
        setMinimumSize(new Dimension(400, 300));
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        backgroundPanel = new BackgroundPanel();
        backgroundPanel.setLayout(new BorderLayout());
        backgroundPanel.setBorder(new EmptyBorder(15, 15, 15, 15));
        add(backgroundPanel, BorderLayout.CENTER);

        // Central vertical stack
        JPanel centerStack = new JPanel();
        centerStack.setOpaque(false);
        centerStack.setLayout(new BoxLayout(centerStack, BoxLayout.Y_AXIS));
        centerStack.setAlignmentX(CENTER_ALIGNMENT);

        statusLabel = new JLabel(" Initializing...");
        statusLabel.setFont(new Font("Segoe UI", Font.BOLD, 22));
        statusLabel.setForeground(Color.WHITE);
        statusLabel.setAlignmentX(CENTER_ALIGNMENT);
        centerStack.add(statusLabel);
        centerStack.add(Box.createVerticalStrut(10));

        spinnerLabel = new JLabel();
        spinnerLabel.setAlignmentX(CENTER_ALIGNMENT);
        try {
            URL spinnerUrl = EchoPilotGUI.class.getResource(SPINNER_GIF_PATH);
            if (spinnerUrl != null) {
                spinnerLabel.setIcon(new ImageIcon(spinnerUrl));
            } else {
                spinnerLabel.setText("Loading...");
            }
        } catch (Exception e) {
            spinnerLabel.setText("Spinner failed");
        }
        centerStack.add(spinnerLabel);
        centerStack.add(Box.createVerticalStrut(10));

        JLabel micIconLabel = new JLabel();
        micIconLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        micIconLabel.setHorizontalAlignment(SwingConstants.CENTER);
        try {
            Image scaledMic = ImageLoader.loadImage(MIC_ICON_PATH, 80, 80);
            if (scaledMic != null) {
                micIconLabel.setIcon(new ImageIcon(scaledMic));
                micIconLabel.setToolTipText("Microphone icon");
            } else {
                micIconLabel.setText("Mic image not found");
            }
        } catch (Exception e) {
            micIconLabel.setText("Failed to load mic image");
        }

        glowingMic = new GlowPanel(micIconLabel);
        glowingMic.setMaximumSize(new Dimension(100, 100));
        glowingMic.setAlignmentX(CENTER_ALIGNMENT);
        centerStack.add(glowingMic);
        centerStack.add(Box.createVerticalStrut(10));

        JPanel controlPanel = new JPanel(new GridLayout(1, 2, 10, 0));
        controlPanel.setOpaque(false);
        controlPanel.setMaximumSize(new Dimension(300, 40));
        pauseButton = createStyledButton("Pause Listening", Color.RED);
        resumeButton = createStyledButton("Start Listening", new Color(0, 200, 0));
        controlPanel.add(pauseButton);
        controlPanel.add(resumeButton);
        centerStack.add(controlPanel);

        backgroundPanel.add(centerStack, BorderLayout.SOUTH);

        commandArea = new JTextArea("Command Log:\nNone");
        commandArea.setEditable(false);
        commandArea.setLineWrap(true);
        commandArea.setWrapStyleWord(true);
        commandArea.setOpaque(true);
        commandArea.setFont(new Font("Consolas", Font.PLAIN, 13));
        commandArea.setForeground(Color.WHITE);
        commandArea.setBackground(Color.BLACK);
        commandArea.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        JScrollPane scrollPane = new JScrollPane(commandArea);
        scrollPane.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(Color.WHITE),
                "Command History->",
                TitledBorder.LEFT,
                TitledBorder.TOP,
                new Font("Segoe UI", Font.BOLD, 12),
                Color.BLACK
        ));
        scrollPane.setPreferredSize(new Dimension(300, 50));
        backgroundPanel.add(scrollPane, BorderLayout.CENTER);

        setupGlowAnimation();
    }

    /**
     * Initializes the Timer for the mic glow animation, causing the glow to pulse in size.
     */
    private void setupGlowAnimation() {
        glowTimer = new Timer(300, (ActionEvent e) -> {
            int size = glowingMic.getGlowSize() == 2 ? 6 : 2;
            glowingMic.setGlowSize(size);
        });
    }

    /**
     * Utility class for loading images from various sources (resource path, file, URL).
     */
    public static class ImageLoader {
        /**
         * Loads an image from a given path (resource, file, or URL) and optionally scales it.
         *
         * @param path The path to the image resource (e.g., /images/img.png).
         * @param width The desired width. Use -1 to keep original size.
         * @param height The desired height. Use -1 to keep original size.
         * @return The loaded and possibly scaled Image, or null if loading fails.
         */
        public static Image loadImage(String path, int width, int height) {
            Image img = null;
            // 1. Try loading from resources (JAR)
            try (InputStream is = ImageLoader.class.getResourceAsStream(path)) {
                if (is != null) {
                    img = ImageIO.read(is);
                }
            } catch (IOException e) { /* Ignore resource loading error */ }

            // 2. Try loading from file system/URL if resource fails
            if (img == null) {
                try {
                    if (path.startsWith("http://") || path.startsWith("https://")) {
                        img = ImageIO.read(new URL(path));
                    } else {
                        img = ImageIO.read(new File(path));
                    }
                } catch (IOException e) {
                    System.err.println("Failed to load image: " + path);
                    return null;
                }
            }

            // 3. Scale the image if dimensions are provided
            if (img != null && (width > 0 || height > 0)) {
                return img.getScaledInstance(width, height, Image.SCALE_SMOOTH);
            }
            return img;
        }
    }

    /**
     * Custom JPanel for rendering the application's background. It loads an image
     * and uses a gradient fill as a fallback if the image is missing.
     */
    private class BackgroundPanel extends JPanel {
        private Image backgroundImage;

        /**
         * Loads the background image upon construction.
         */
        public BackgroundPanel() {
            backgroundImage = ImageLoader.loadImage(BACKGROUND_IMAGE_PATH, -1, -1);
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2d = (Graphics2D) g.create();

            if (backgroundImage != null) {
                // Draw the image stretched to fill the panel
                g2d.drawImage(backgroundImage, 0, 0, getWidth(), getHeight(), this);
            } else {
                // Fallback to a gradient fill
                GradientPaint gradient = new GradientPaint(0, 0, new Color(50, 50, 70), getWidth(), getHeight(), new Color(30, 30, 50));
                g2d.setPaint(gradient);
                g2d.fillRect(0, 0, getWidth(), getHeight());
            }

            g2d.dispose();
        }

        @Override
        public Dimension getPreferredSize() {
            return new Dimension(520, 460);
        }
    }

    /**
     * Custom JPanel that holds the microphone icon and applies a pulsing radial glow effect
     * when the system is actively listening.
     */
    public class GlowPanel extends JPanel {
        private Color glowColor = Color.CYAN;
        private int glowSize = 4;
        private boolean glowing = false;

        /**
         * Constructs a GlowPanel and places the icon label inside.
         *
         * @param iconLabel The JLabel containing the microphone icon.
         */
        public GlowPanel(JLabel iconLabel) {
            setLayout(new BorderLayout());
            setOpaque(false);
            add(iconLabel, BorderLayout.CENTER);
        }

        /**
         * Enables or disables the glowing effect.
         *
         * @param active True to start glowing, false to stop.
         */
        public void setGlow(boolean active) {
            glowing = active;
            repaint();
        }

        /**
         * Sets the size parameter for the glow effect, influencing its radius and intensity.
         *
         * @param size The new glow size.
         */
        public void setGlowSize(int size) {
            glowSize = size;
            repaint();
        }

        /**
         * Gets the current glow size parameter.
         *
         * @return The current glow size.
         */
        public int getGlowSize() {
            return glowSize;
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (glowing) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                Component mic = getComponent(0); // micIconLabel
                Rectangle bounds = mic.getBounds();
                int centerX = bounds.x + bounds.width / 2;
                int centerY = bounds.y + bounds.height / 2;

                int baseRadius = glowSize * 12; // Increase base radius
                int layers = 3; // Number of layers for the radial gradient

                for (int i = 0; i < layers; i++) {
                    int radius = baseRadius + i * 10;
                    float opacity = 0.4f - i * 0.1f; // Fade out with each layer

                    RadialGradientPaint paint = new RadialGradientPaint(
                        new Point(centerX, centerY),
                        radius,
                        new float[]{0f, 1f},
                        new Color[]{
                            new Color(glowColor.getRed(), glowColor.getGreen(), glowColor.getBlue(), (int)(255 * opacity)),
                            new Color(0, 0, 0, 0) // Fully transparent at the edge
                        }
                    );

                    g2.setPaint(paint);
                    g2.fillOval(centerX - radius, centerY - radius, radius * 2, radius * 2);
                }

                g2.dispose();
            }
        }
    }

    /**
     * Creates a standard styled JButton for the control panel with a specific color scheme.
     *
     * @param text The text to display on the button.
     * @param bgColor The background color of the button.
     * @return A styled JButton instance.
     */
    private JButton createStyledButton(String text, Color bgColor) {
        JButton button = new JButton(text);
        button.setFont(new Font("Segoe UI", Font.BOLD, 18));
        button.setBackground(bgColor);
        button.setForeground(Color.BLACK);
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createLineBorder(Color.WHITE, 2));
        return button;
    }

    /**
     * Sets the action listeners for the Pause and Resume buttons and configures
     * their initial state based on the Recognizer's status.
     */
    public void setRecognizerControls(Runnable resumeAction, Runnable pauseAction, boolean initialState) {
        pauseButton.addActionListener(e -> {
            pauseAction.run();
            // Call updateListeningIndicator to update the glow color and state
            updateListeningIndicator(false); 
        });

        resumeButton.addActionListener(e -> {
            resumeAction.run();
            // Call updateListeningIndicator to update the glow color and state
            updateListeningIndicator(true);
        });

        // Initialize state
        updateListeningIndicator(initialState);
    }
    
    /**
     * Updates the GUI state based on whether the assistant is listening or paused.
     * This method combines status label update, button state, and microphone glow state.
     * * @param isListening True if the recognizer is active, false if paused/in background.
     */
    public void updateListeningIndicator(boolean isListening) {
        SwingUtilities.invokeLater(() -> {
            // 1. Update Status Label
            statusLabel.setText(isListening ? " Listening..." : " Paused");
            
            // 2. Update Control Buttons
            updateControlButtons(isListening);

            // 3. Update Microphone Glow
            if (isListening) {
                glowingMic.glowColor = Color.CYAN; // Active glow color
                startMicGlow();
            } else {
                glowingMic.glowColor = Color.YELLOW; // Paused/Waiting glow color
                stopMicGlow();
                // Optionally show a faint, steady glow when paused
                glowingMic.setGlow(true);
                glowingMic.setGlowSize(1);
            }
        });
    }

    public void updateControlButtons(boolean isListening) {
        SwingUtilities.invokeLater(() -> {
            pauseButton.setEnabled(isListening);
            resumeButton.setEnabled(!isListening);
        });
    }

    public void updateStatus(String status) {
        SwingUtilities.invokeLater(() -> {
            statusLabel.setText(status);
            // Hide spinner once initialization is complete (called by external class)
            if (spinnerLabel != null) {
                spinnerLabel.setVisible(false); 
            }
        });
    }

    public void startMicGlow() {
        glowingMic.setGlow(true);
        glowTimer.start();
    }

    /**
     * Stops the microphone glow animation by stopping the associated timer and resetting the glow state.
     */
    public void stopMicGlow() {
        glowTimer.stop();
        // Do not call setGlow(false) here, as we want to keep a steady glow when paused
    }

    public void updateCommand(String command) {
        SwingUtilities.invokeLater(() -> {
            String currentText = commandArea.getText();
            if (currentText.contains("Command Log:\nNone")) {
                commandArea.setText("Command Log:\n" + command);
            } else {
                commandArea.append("\n" + command);
            }
            // Scroll to the bottom
            commandArea.setCaretPosition(commandArea.getDocument().getLength());
        });
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            EchoPilotGUI gui = new EchoPilotGUI();
            gui.setVisible(true);

            // Example of setting controls with placeholder actions
            gui.setRecognizerControls(
                () -> System.out.println("RESUME clicked"),
                () -> System.out.println("PAUSE clicked"),
                true
            );

            // Example of simulating initialization and command updates
            new Thread(() -> {
                try {
                    Thread.sleep(2000); // Simulate initialization delay
                    gui.updateStatus(" Listening...");
                    gui.updateCommand("User spoke: Open application");
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }).start();
        });
    }
}