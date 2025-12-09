package com.friend.friend;

import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.web.WebView;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

/**
 * AssistantOverlay: a lightweight JavaFX overlay providing a small animated avatar,
 * live transcript, confidence meter and simple waveform placeholder.
 *
 * This runs in the JavaFX thread. It is intentionally lightweight and pluggable.
 */
public class AssistantOverlay {
    private final Stage stage;
    private final Label transcriptLabel;
    private final ProgressBar confidenceBar;
    private final Canvas waveformCanvas;
    private final WebView webView;

    public AssistantOverlay() {
        stage = new Stage(StageStyle.TRANSPARENT);
        stage.setAlwaysOnTop(true);
        stage.initStyle(StageStyle.TRANSPARENT);

        VBox root = new VBox(6);
        root.setPadding(new Insets(8));
        root.setStyle("-fx-background-color: rgba(0,0,0,0.45); -fx-background-radius: 12; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.4), 10, 0, 0, 2);");

        // WebView for animated avatar (Lottie/html fallback)
        webView = new WebView();
        webView.setPrefSize(120, 120);
        // Minimal HTML to show animated PNG or fallback
        String html = "<html><body style='margin:0;background:transparent;'><img src='" + getClass().getResource("/images/avatar-small.png") + "' width='120' height='120' /></body></html>";
        try {
            webView.getEngine().loadContent(html);
        } catch (Exception e) {
            // ignore
        }

        transcriptLabel = new Label("...");
        transcriptLabel.setStyle("-fx-text-fill: white; -fx-font-size: 14px;");
        transcriptLabel.setWrapText(true);
        transcriptLabel.setMaxWidth(320);

        confidenceBar = new ProgressBar(0.0);
        confidenceBar.setPrefWidth(200);

        waveformCanvas = new Canvas(260, 40);
        drawWaveformPlaceholder();

        HBox top = new HBox(8);
        top.getChildren().addAll(webView, transcriptLabel);
        HBox.setHgrow(transcriptLabel, Priority.ALWAYS);

        root.getChildren().addAll(top, confidenceBar, waveformCanvas);

        Scene scene = new Scene(root);
        scene.setFill(Color.TRANSPARENT);
        stage.setScene(scene);
        stage.setWidth(380);
        stage.setHeight(180);
        stage.setX(40);
        stage.setY(40);
        stage.setAlwaysOnTop(true);
        stage.setResizable(false);
        stage.setOpacity(0.95);
    }

    private void drawWaveformPlaceholder() {
        GraphicsContext gc = waveformCanvas.getGraphicsContext2D();
        gc.setFill(Color.web("#222", 0.2));
        gc.fillRect(0, 0, waveformCanvas.getWidth(), waveformCanvas.getHeight());
        gc.setStroke(Color.web("#aaccff", 0.8));
        for (int i = 0; i < waveformCanvas.getWidth(); i += 6) {
            double h = 6 + Math.random() * (waveformCanvas.getHeight() - 12);
            gc.strokeLine(i, waveformCanvas.getHeight() / 2 - h / 2, i, waveformCanvas.getHeight() / 2 + h / 2);
        }
    }

    public void show() {
        if (!stage.isShowing()) {
            stage.show();
        }
    }

    public void hide() {
        if (stage.isShowing()) {
            stage.hide();
        }
    }

    public void updateTranscript(String text) {
        transcriptLabel.setText(text == null || text.isEmpty() ? "..." : text);
    }

    public void updateConfidence(double conf) {
        confidenceBar.setProgress(Math.max(0.0, Math.min(1.0, conf)));
    }

    public void setState(String state) {
        // simple visual cue by adjusting background color
        switch ((state == null) ? "idle" : state) {
            case "listening":
                stage.getScene().getRoot().setStyle("-fx-background-color: rgba(0,60,0,0.45); -fx-background-radius: 12;");
                break;
            case "speaking":
                stage.getScene().getRoot().setStyle("-fx-background-color: rgba(60,0,0,0.45); -fx-background-radius: 12;");
                break;
            default:
                stage.getScene().getRoot().setStyle("-fx-background-color: rgba(0,0,0,0.45); -fx-background-radius: 12;");
                break;
        }
    }

    public void updateWaveform(double[] samples) {
        GraphicsContext gc = waveformCanvas.getGraphicsContext2D();
        gc.clearRect(0, 0, waveformCanvas.getWidth(), waveformCanvas.getHeight());
        gc.setFill(Color.web("#111", 0.12));
        gc.fillRect(0, 0, waveformCanvas.getWidth(), waveformCanvas.getHeight());
        gc.setStroke(Color.web("#aaccff", 0.9));
        double w = waveformCanvas.getWidth();
        double h = waveformCanvas.getHeight();
        if (samples == null || samples.length == 0) {
            drawWaveformPlaceholder();
            return;
        }
        int step = Math.max(1, samples.length / (int) w);
        for (int i = 0, x = 0; i < samples.length && x < w; i += step, x++) {
            double v = Math.abs(samples[i]);
            double y1 = h / 2 - v * (h / 2);
            double y2 = h / 2 + v * (h / 2);
            gc.strokeLine(x, y1, x, y2);
        }
    }
}
