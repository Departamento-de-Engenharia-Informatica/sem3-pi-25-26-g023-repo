package pt.ipp.isep.dei.UI.gui.utils;

import javafx.animation.AnimationTimer;
import javafx.geometry.Point2D;
import javafx.geometry.VPos;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.ToggleButton;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.TextAlignment;
import pt.ipp.isep.dei.domain.Train;

import java.time.Duration;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class RailMapVisualizer extends Pane {

    private final Canvas canvas;
    private final Map<String, Point2D> stationCoordinates = new HashMap<>();
    private final List<VisualSegment> segments = new ArrayList<>();
    private final List<VisualTrain> activeTrains = new ArrayList<>();

    // --- CONTROLO DE CÂMARA ---
    private double scale = 1.0;
    private double translateX = 0.0;
    private double translateY = 0.0;

    // Alvos para a interpolação (Movimento Suave)
    private double targetScale = 1.0;
    private double targetTranslateX = 0.0;
    private double targetTranslateY = 0.0;
    private boolean autoCameraMode = true;

    private double lastMouseX, lastMouseY;

    // --- SIMULAÇÃO ---
    private LocalTime simulationTime;
    private LocalTime maxSimulationTime;
    private double speedFactor = 100.0;
    private AnimationTimer timer;
    private boolean isRunning = false;
    private boolean isFinished = false;

    // --- UI CONTROLS ---
    private final Button btnZoomIn;
    private final Button btnZoomOut;
    private final ToggleButton btnAutoCam;

    public RailMapVisualizer() {
        // 1. Inicializa Canvas
        this.canvas = new Canvas(0, 0);

        // 2. Controlos UI
        this.btnZoomIn = createButton("+", e -> manualZoom(1.2));
        this.btnZoomOut = createButton("-", e -> manualZoom(0.8));

        this.btnAutoCam = new ToggleButton("Auto Cam");
        this.btnAutoCam.setSelected(true);
        this.btnAutoCam.setStyle("-fx-background-color: #2c3e50; -fx-text-fill: white; -fx-font-size: 10px;");
        this.btnAutoCam.selectedProperty().addListener((obs, old, isSelected) -> autoCameraMode = isSelected);

        VBox controls = new VBox(5, btnZoomIn, btnZoomOut, btnAutoCam);
        controls.setLayoutX(20);
        controls.setLayoutY(20);

        getChildren().addAll(canvas, controls);

        // 3. Bindings
        canvas.widthProperty().bind(this.widthProperty());
        canvas.heightProperty().bind(this.heightProperty());

        this.widthProperty().addListener(e -> updateFrame());
        this.heightProperty().addListener(e -> updateFrame());

        setStyle("-fx-background-color: #1e1e1e; -fx-border-color: #444;");

        setupInteraction();
        setupMapCoordinates();
    }

    private Button createButton(String text, javafx.event.EventHandler<javafx.event.ActionEvent> action) {
        Button btn = new Button(text);
        String style = "-fx-background-color: rgba(255,255,255,0.15); -fx-text-fill: white; -fx-font-weight: bold; -fx-min-width: 30px; -fx-cursor: hand;";
        btn.setStyle(style);
        btn.setOnAction(action);
        return btn;
    }

    private void manualZoom(double factor) {
        autoCameraMode = false;
        btnAutoCam.setSelected(false);
        targetScale *= factor;
        // Clamp manual zoom
        if(targetScale < 0.01) targetScale = 0.01;
        if(targetScale > 10.0) targetScale = 10.0;

        scale = targetScale;
        updateFrame();
    }

    private void fitBounds(double minX, double maxX, double minY, double maxY, double padding) {
        if (getWidth() <= 0 || getHeight() <= 0) return;

        double contentW = maxX - minX;
        double contentH = maxY - minY;

        if (contentW < 100) contentW = 100;
        if (contentH < 100) contentH = 100;

        double availW = getWidth() - padding * 2;
        double availH = getHeight() - padding * 2;

        double scaleX = availW / contentW;
        double scaleY = availH / contentH;

        double newScale = Math.min(scaleX, scaleY);
        // Limites ajustados
        newScale = Math.max(0.05, Math.min(newScale, 2.0));

        double contentCX = minX + contentW / 2.0;
        double contentCY = minY + contentH / 2.0;

        double newTX = (getWidth() / 2.0) - (contentCX * newScale);
        double newTY = (getHeight() / 2.0) - (contentCY * newScale);

        this.targetScale = newScale;
        this.targetTranslateX = newTX;
        this.targetTranslateY = newTY;
    }

    private void updateCameraLogic() {
        if (!autoCameraMode) return;

        List<Point2D> activePoints = new ArrayList<>();

        if (simulationTime != null) {
            for (VisualTrain vt : activeTrains) {
                Point2D pos = vt.getCurrentPositionOrLastStation(simulationTime);
                if (pos != null) {
                    activePoints.add(pos);
                }
            }
        }

        double minX = Double.MAX_VALUE, maxX = -Double.MAX_VALUE;
        double minY = Double.MAX_VALUE, maxY = -Double.MAX_VALUE;
        double padding = 100;

        if (activePoints.isEmpty()) {
            // Mostrar mapa todo se não houver comboios
            for(VisualSegment vs : segments) {
                Point2D p1 = stationCoordinates.get(vs.start);
                Point2D p2 = stationCoordinates.get(vs.end);
                if (p1 != null) { minX = Math.min(minX, p1.getX()); maxX = Math.max(maxX, p1.getX()); minY = Math.min(minY, p1.getY()); maxY = Math.max(maxY, p1.getY()); }
                if (p2 != null) { minX = Math.min(minX, p2.getX()); maxX = Math.max(maxX, p2.getX()); minY = Math.min(minY, p2.getY()); maxY = Math.max(maxY, p2.getY()); }
            }
        } else {
            // Focar nos comboios
            for (Point2D p : activePoints) {
                minX = Math.min(minX, p.getX());
                maxX = Math.max(maxX, p.getX());
                minY = Math.min(minY, p.getY());
                maxY = Math.max(maxY, p.getY());
            }
            padding = (activePoints.size() == 1) ? 300 : 200;
        }

        if (minX == Double.MAX_VALUE) return;

        fitBounds(minX, maxX, minY, maxY, padding);
    }

    private void applyCameraSmoothing() {
        double smoothFactor = 0.08;
        if (Math.abs(targetScale - scale) < 0.001) {
            scale = targetScale;
            translateX = targetTranslateX;
            translateY = targetTranslateY;
        } else {
            scale += (targetScale - scale) * smoothFactor;
            translateX += (targetTranslateX - translateX) * smoothFactor;
            translateY += (targetTranslateY - translateY) * smoothFactor;
        }
    }

    private void setupInteraction() {
        this.setOnScroll(event -> {
            autoCameraMode = false;
            btnAutoCam.setSelected(false);
            double zoomFactor = (event.getDeltaY() > 0) ? 1.1 : 0.9;
            targetScale *= zoomFactor;
            event.consume();
        });

        this.setOnMousePressed(event -> {
            lastMouseX = event.getX();
            lastMouseY = event.getY();
        });

        this.setOnMouseDragged(event -> {
            autoCameraMode = false;
            btnAutoCam.setSelected(false);
            targetTranslateX += (event.getX() - lastMouseX);
            targetTranslateY += (event.getY() - lastMouseY);
            // Atualização imediata para drag responsivo
            translateX = targetTranslateX;
            translateY = targetTranslateY;

            lastMouseX = event.getX();
            lastMouseY = event.getY();
            updateFrame();
        });
    }

    // --- MAPA ESPAÇADO E LIMPO ---
    private void setupMapCoordinates() {
        // Eixo Central X=1000. Espaçamento Y grande para evitar overlaps

        // LINHA DO MINHO
        addStation("Valença", 1000, 0);
        addStation("São Pedro da Torre", 1000, 200);
        addStation("Vila Nova de Cerveira", 1000, 400);
        addStation("Caminha", 1000, 600);
        addStation("Viana do Castelo", 1000, 800);
        addStation("Darque", 1000, 1000);
        addStation("Barcelos", 1000, 1200);
        addStation("Nine", 1000, 1400); // HUB

        // Ramal Braga (Direita)
        addStation("Braga", 1300, 1400);

        addStation("Famalicão", 1000, 1600);
        addStation("Trofa", 1000, 1800);

        // Ramal Guimarães (Direita)
        addStation("Santo Tirso", 1200, 1800);
        addStation("Guimarães", 1400, 1800);

        // Entrada Porto
        addStation("Ermesinde", 1000, 2000);

        // Linha de Leixões (Esquerda)
        addStation("São Gemil", 800, 2000);
        addStation("Leça do Balio", 600, 2000);
        addStation("Leixões", 400, 2000);

        addStation("Rio Tinto", 1000, 2150);
        addStation("Contumil", 1000, 2300);

        addStation("Porto - Campanhã", 1000, 2500); // HUB

        // Ramal São Bento (Esquerda)
        addStation("Porto - São Bento", 700, 2500);

        // SUL (Linha do Norte)
        addStation("Vila Nova de Gaia", 1000, 2700);
        addStation("Espinho", 1000, 3000);
        addStation("Ovar", 1000, 3200);
        addStation("Aveiro", 1000, 3500);
        addStation("Coimbra-B", 1000, 3900);
        addStation("Pombal", 1000, 4200);
        addStation("Entroncamento", 1000, 4600);
        addStation("Santarém", 1000, 4900);
        addStation("Lisboa - Oriente", 1000, 5400);
        addStation("Lisboa - Santa Apolónia", 1000, 5600);
    }

    private void addStation(String name, double x, double y) {
        stationCoordinates.put(name, new Point2D(x, y));
    }

    public void loadSchedule(Map<Train, List<String>> trainSchedules, LocalTime localTime) {
        this.segments.clear();
        this.activeTrains.clear();
        this.isFinished = false;

        LocalTime globalStart = LocalTime.MAX;
        LocalTime globalEnd = LocalTime.MIN;

        for (Map.Entry<Train, List<String>> entry : trainSchedules.entrySet()) {
            Train train = entry.getKey();
            List<String> logs = entry.getValue();

            VisualTrain vTrain = new VisualTrain(train);
            boolean hasSegments = false;

            for (String line : logs) {
                if (!line.contains("|")) continue;
                String[] parts = line.split("\\|");
                if (parts.length < 7) continue;

                try {
                    String startSt = parts[1].trim();
                    String endSt = parts[2].trim();
                    String type = parts[3].trim();
                    String entryTimeStr = parts[5].trim();
                    String exitTimeStr = parts[6].trim();

                    if (startSt.equalsIgnoreCase("START") || startSt.equalsIgnoreCase("END")) continue;
                    if (entryTimeStr.equals("N/A") || exitTimeStr.equals("N/A")) continue;

                    LocalTime tIn = LocalTime.parse(entryTimeStr);
                    LocalTime tOut = LocalTime.parse(exitTimeStr);

                    if (tIn.isBefore(globalStart)) globalStart = tIn;
                    if (tOut.isAfter(globalEnd)) globalEnd = tOut;

                    Point2D p1 = getCoordinates(startSt);
                    Point2D p2 = getCoordinates(endSt);

                    if (segments.stream().noneMatch(s -> s.matches(startSt, endSt))) {
                        segments.add(new VisualSegment(startSt, endSt, p1, p2, type.equalsIgnoreCase("Double")));
                    }

                    vTrain.addMovement(tIn, tOut, p1, p2);
                    hasSegments = true;

                } catch (Exception e) { }
            }
            if (hasSegments) activeTrains.add(vTrain);
        }

        if (globalStart != LocalTime.MAX) {
            this.simulationTime = globalStart.minusSeconds(1);
            this.maxSimulationTime = globalEnd;
        } else {
            this.simulationTime = LocalTime.of(8, 0);
        }

        autoCameraMode = true;
        btnAutoCam.setSelected(true);
        updateCameraLogic();
        scale = targetScale; translateX = targetTranslateX; translateY = targetTranslateY;

        updateFrame();
    }

    private Point2D getCoordinates(String name) {
        if (stationCoordinates.containsKey(name)) return stationCoordinates.get(name);
        for (String key : stationCoordinates.keySet()) {
            if (key.contains(name) || name.contains(key)) return stationCoordinates.get(key);
        }
        return new Point2D(0, 0);
    }

    public void startAnimation() {
        if (timer != null) timer.stop();
        if (activeTrains.isEmpty()) return;
        isFinished = false;

        timer = new AnimationTimer() {
            private long lastUpdate = 0;
            @Override
            public void handle(long now) {
                if (lastUpdate == 0) { lastUpdate = now; return; }

                double elapsedSecondsReal = (now - lastUpdate) / 1_000_000_000.0;
                lastUpdate = now;

                long secondsToAdd = (long) (elapsedSecondsReal * speedFactor);

                if (simulationTime != null) {
                    simulationTime = simulationTime.plusSeconds(secondsToAdd);
                    if (maxSimulationTime != null && simulationTime.isAfter(maxSimulationTime)) {
                        simulationTime = maxSimulationTime;
                        isFinished = true;
                        this.stop();
                    }
                }

                updateCameraLogic();
                applyCameraSmoothing();
                updateFrame();
            }
        };
        timer.start();
        isRunning = true;
    }

    public void stopAnimation() {
        if (timer != null) timer.stop();
        isRunning = false;
    }

    public void setSpeedFactor(double factor) { this.speedFactor = factor; }

    // --- CORAÇÃO DO RENDER (SOLUÇÃO DA "SALGALHADA") ---
    private void updateFrame() {
        GraphicsContext gc = canvas.getGraphicsContext2D();
        double w = getWidth();
        double h = getHeight();

        // 1. Limpar Fundo
        gc.setFill(Color.web("#1e1e1e"));
        gc.fillRect(0,0, w, h);

        // 2. LAYER MUNDO (LINHAS) - Aplica Transformação da Câmara
        gc.save();
        gc.translate(translateX, translateY);
        gc.scale(scale, scale);

        gc.setLineCap(javafx.scene.shape.StrokeLineCap.ROUND);
        for (VisualSegment seg : segments) {
            // TRUQUE: Dividir grossura pelo scale para manter espessura visual constante
            // Mas permitimos que fique *ligeiramente* mais grosso com zoom, mas não linearmente
            double baseWidth = seg.isDouble ? 3.0 : 1.5;
            double visualWidth = baseWidth / Math.sqrt(scale); // Suaviza o crescimento

            // Fundo da linha
            gc.setStroke(Color.web("#333"));
            gc.setLineWidth(visualWidth * 2.5);
            gc.strokeLine(seg.p1.getX(), seg.p1.getY(), seg.p2.getX(), seg.p2.getY());

            // Linha colorida
            gc.setStroke(seg.isDouble ? Color.DODGERBLUE : Color.ORANGE);
            gc.setLineWidth(visualWidth);
            gc.strokeLine(seg.p1.getX(), seg.p1.getY(), seg.p2.getX(), seg.p2.getY());
        }
        gc.restore(); // Fim do Layer Mundo

        // 3. LAYER ECRÃ (ESTAÇÕES E TEXTO)
        // Calculamos a posição no ecrã manualmente para desenhar ícones com tamanho FIXO (INVARIANTE)
        Set<String> drawnStations = new HashSet<>();
        for (VisualSegment seg : segments) {
            drawStationScreenSpace(gc, seg.start, seg.p1, drawnStations);
            drawStationScreenSpace(gc, seg.end, seg.p2, drawnStations);
        }

        // 4. LAYER COMBOIOS (SCREEN SPACE)
        List<String> waitingTrains = new ArrayList<>();
        for (VisualTrain train : activeTrains) {
            Point2D worldPos = train.getCurrentPositionOrLastStation(simulationTime);
            if (worldPos != null) {
                // Converter para Screen Space
                double sx = worldPos.getX() * scale + translateX;
                double sy = worldPos.getY() * scale + translateY;

                boolean waiting = train.isWaiting(simulationTime);
                if (waiting) waitingTrains.add(train.train.getTrainId());

                // Desenha Comboio (Tamanho Fixo!)
                gc.setFill(waiting ? Color.ORANGERED : Color.LIME);
                gc.setEffect(new javafx.scene.effect.DropShadow(10, waiting ? Color.RED : Color.LIME));
                gc.fillOval(sx - 7, sy - 7, 14, 14); // Sempre 14px
                gc.setEffect(null);

                // Label do Comboio
                gc.setFill(Color.YELLOW);
                gc.setFont(Font.font("Arial", FontWeight.BOLD, 12));
                gc.fillText(train.train.getTrainId(), sx + 10, sy - 10);
            }
        }

        // 5. HUD
        drawHUD(gc, w, h);
        drawAlerts(gc, w, waitingTrains);
    }

    private void drawStationScreenSpace(GraphicsContext gc, String name, Point2D worldPos, Set<String> drawn) {
        if (drawn.contains(name)) return;
        drawn.add(name);

        // Converter coordenadas do mundo para pixeis no ecrã
        double sx = worldPos.getX() * scale + translateX;
        double sy = worldPos.getY() * scale + translateY;

        // Só desenha se estiver dentro do ecrã (Optimization)
        if (sx < -50 || sx > getWidth() + 50 || sy < -50 || sy > getHeight() + 50) return;

        // Ponto da Estação (Tamanho Fixo: 8px)
        gc.setFill(Color.WHITE);
        gc.fillOval(sx - 4, sy - 4, 8, 8);

        // Texto (Tamanho Fixo: 11px)
        // Só mostra texto se o zoom não for microscópico (para não poluir se estiver muito longe)
        if (scale > 0.1) {
            gc.setFill(Color.LIGHTGRAY);
            gc.setFont(Font.font("Arial", FontWeight.NORMAL, 11));

            // Smart Labeling: Esquerda ou Direita baseado na posição X original
            if (worldPos.getX() < 990) {
                gc.setTextAlign(TextAlignment.RIGHT);
                gc.fillText(name, sx - 10, sy + 4);
            } else {
                gc.setTextAlign(TextAlignment.LEFT);
                gc.fillText(name, sx + 10, sy + 4);
            }
            gc.setTextAlign(TextAlignment.LEFT); // Reset
        }
    }

    private void drawHUD(GraphicsContext gc, double w, double h) {
        gc.setFill(Color.color(0, 0, 0, 0.7));
        gc.fillRoundRect(w - 260, 10, 240, 100, 15, 15);
        gc.setStroke(Color.WHITE);
        gc.setLineWidth(1);
        gc.strokeRoundRect(w - 260, 10, 240, 100, 15, 15);

        gc.setFill(Color.WHITE);
        gc.setFont(Font.font("Monospaced", FontWeight.BOLD, 22));

        String timeStr = (simulationTime != null) ? simulationTime.format(DateTimeFormatter.ofPattern("HH:mm:ss")) : "--:--:--";
        gc.fillText("HORA: " + timeStr, w - 240, 40);

        String remainingStr = "Concluído";
        if (simulationTime != null && maxSimulationTime != null && !isFinished) {
            long secondsLeft = java.time.Duration.between(simulationTime, maxSimulationTime).getSeconds();
            if (secondsLeft < 0) secondsLeft = 0;
            long hh = secondsLeft / 3600;
            long mm = (secondsLeft % 3600) / 60;
            long ss = secondsLeft % 60;
            remainingStr = String.format("-%02d:%02d:%02d", hh, mm, ss);
        }

        gc.setFill(isFinished ? Color.LIGHTGREEN : Color.ORANGE);
        gc.setFont(Font.font("Monospaced", FontWeight.BOLD, 16));
        gc.fillText("FIM EM: " + remainingStr, w - 240, 65);

        gc.setFill(Color.LIGHTGRAY);
        gc.setFont(Font.font("Arial", 11));
        String mode = autoCameraMode ? "AUTO" : "MANUAL";
        gc.fillText(String.format("Cam: %s | Speed: %.0fx", mode, speedFactor), w - 240, 90);
    }

    private void drawAlerts(GraphicsContext gc, double w, List<String> waitingTrains) {
        if (waitingTrains.isEmpty()) return;

        String text = "EM ESPERA: " + String.join(", ", waitingTrains);

        gc.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        double textWidth = text.length() * 9 + 30;
        double boxX = (w - textWidth) / 2;

        gc.setFill(Color.rgb(255, 69, 0, 0.9));
        gc.fillRoundRect(boxX, 10, textWidth, 30, 10, 10);
        gc.setStroke(Color.WHITE);
        gc.setLineWidth(1);
        gc.strokeRoundRect(boxX, 10, textWidth, 30, 10, 10);

        gc.setFill(Color.WHITE);
        gc.setTextAlign(TextAlignment.CENTER);
        gc.setTextBaseline(VPos.CENTER);
        gc.fillText(text, w / 2, 25);
        gc.setTextAlign(TextAlignment.LEFT);
        gc.setTextBaseline(VPos.BASELINE);
    }

    private static class VisualSegment {
        String start, end;
        Point2D p1, p2;
        boolean isDouble;
        VisualSegment(String s, String e, Point2D p1, Point2D p2, boolean d) {
            this.start = s; this.end = e; this.p1 = p1; this.p2 = p2; this.isDouble = d;
        }
        boolean matches(String s, String e) {
            return (start.equals(s) && end.equals(e)) || (start.equals(e) && end.equals(s));
        }
    }

    private static class VisualTrain {
        Train train;
        List<Movement> movements = new ArrayList<>();
        VisualTrain(Train t) { this.train = t; }

        void addMovement(LocalTime s, LocalTime e, Point2D p1, Point2D p2) {
            movements.add(new Movement(s, e, p1, p2));
            movements.sort(Comparator.comparing(m -> m.start));
        }

        boolean isWaiting(LocalTime now) {
            if (now == null || movements.isEmpty()) return false;
            if (now.isBefore(movements.get(0).start) || now.isAfter(movements.get(movements.size()-1).end)) return false;
            for (Movement m : movements) {
                if (!now.isBefore(m.start) && !now.isAfter(m.end)) return false;
            }
            return true;
        }

        Point2D getCurrentPositionOrLastStation(LocalTime now) {
            if (now == null || movements.isEmpty()) return null;

            for (Movement m : movements) {
                if (!now.isBefore(m.start) && !now.isAfter(m.end)) {
                    long total = Duration.between(m.start, m.end).getSeconds();
                    long current = Duration.between(m.start, now).getSeconds();
                    if (total <= 0) total = 1;
                    double t = (double) current / total;
                    return new Point2D(
                            m.p1.getX() + (m.p2.getX() - m.p1.getX()) * t,
                            m.p1.getY() + (m.p2.getY() - m.p1.getY()) * t
                    );
                }
            }

            for (int i = 0; i < movements.size() - 1; i++) {
                Movement current = movements.get(i);
                Movement next = movements.get(i+1);
                if (now.isAfter(current.end) && now.isBefore(next.start)) {
                    return current.p2;
                }
            }

            return null;
        }
    }

    private static class Movement {
        LocalTime start, end;
        Point2D p1, p2;
        Movement(LocalTime s, LocalTime e, Point2D p1, Point2D p2) {
            this.start = s; this.end = e; this.p1 = p1; this.p2 = p2;
        }
    }
}