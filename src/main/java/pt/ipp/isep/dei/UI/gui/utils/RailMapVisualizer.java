package pt.ipp.isep.dei.UI.gui.utils;

import javafx.animation.AnimationTimer;
import javafx.application.Platform;
import javafx.geometry.Point2D;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
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

    // --- CONTROLO DE CÂMARA (ZOOM & PAN) ---
    private double scale = 1.0;
    private double translateX = 0.0;
    private double translateY = 0.0;

    // Variáveis para arrastar (Pan)
    private double lastMouseX, lastMouseY;

    // --- SIMULAÇÃO ---
    private LocalTime simulationTime;
    private double speedFactor = 120.0;
    private AnimationTimer timer;
    private boolean isRunning = false;
    private boolean initialFitDone = false; // Controlo para ajustar apenas na primeira vez

    public RailMapVisualizer() {
        // Inicializa canvas
        this.canvas = new Canvas(0, 0);
        getChildren().add(canvas);

        // Auto-Resize do Canvas ao mudar o tamanho da janela
        canvas.widthProperty().bind(this.widthProperty());
        canvas.heightProperty().bind(this.heightProperty());

        // Listeners para redesenhar e ajustar zoom se a janela mudar de tamanho
        this.widthProperty().addListener((obs, oldVal, newVal) -> {
            if (!initialFitDone && newVal.doubleValue() > 0 && !stationCoordinates.isEmpty()) {
                fitContent();
                initialFitDone = true;
            }
            updateFrame();
        });
        this.heightProperty().addListener(e -> updateFrame());

        setStyle("-fx-background-color: #111; -fx-border-color: #444;");

        setupInteraction();
        setupMapCoordinates();

        // Tenta ajustar logo no arranque (pode falhar se a janela ainda não tiver tamanho, o listener acima resolve)
        Platform.runLater(this::fitContent);
    }

    /**
     * NOVA FUNÇÃO: Calcula o zoom e posição ideais para ver tudo
     */
    public void fitContent() {
        if (stationCoordinates.isEmpty() || getWidth() <= 0 || getHeight() <= 0) return;

        double minX = Double.MAX_VALUE;
        double maxX = -Double.MAX_VALUE;
        double minY = Double.MAX_VALUE;
        double maxY = -Double.MAX_VALUE;

        // 1. Encontrar limites (Bounding Box) das estações
        for (Point2D p : stationCoordinates.values()) {
            if (p.getX() < minX) minX = p.getX();
            if (p.getX() > maxX) maxX = p.getX();
            if (p.getY() < minY) minY = p.getY();
            if (p.getY() > maxY) maxY = p.getY();
        }

        // Adicionar margem de segurança (padding)
        double padding = 50.0;

        // Tamanho do conteúdo (Rede ferroviária)
        double contentWidth = maxX - minX;
        double contentHeight = maxY - minY;

        // Tamanho da Janela disponível
        double availWidth = getWidth() - (padding * 2);
        double availHeight = getHeight() - (padding * 2);

        // Evitar divisão por zero
        if (contentWidth == 0) contentWidth = 100;
        if (contentHeight == 0) contentHeight = 100;

        // 2. Calcular escalas para caber na largura e na altura
        double scaleX = availWidth / contentWidth;
        double scaleY = availHeight / contentHeight;

        // Escolher a menor escala para garantir que tudo cabe (manter proporção)
        this.scale = Math.min(scaleX, scaleY);

        // Limites de segurança para o zoom automático não ser exagerado
        this.scale = Math.max(0.1, Math.min(this.scale, 2.0));

        // 3. Centrar o conteúdo no ecrã
        double contentCenterX = minX + (contentWidth / 2.0);
        double contentCenterY = minY + (contentHeight / 2.0);

        // Fórmula: (CentroJanela) - (CentroConteúdo * Escala)
        this.translateX = (getWidth() / 2.0) - (contentCenterX * this.scale);
        this.translateY = (getHeight() / 2.0) - (contentCenterY * this.scale);

        updateFrame();
    }

    /**
     * Configura Zoom (Scroll) e Pan (Drag)
     */
    private void setupInteraction() {
        // ZOOM (Roda do Rato)
        this.setOnScroll(event -> {
            double zoomFactor = 1.1;
            if (event.getDeltaY() < 0) {
                zoomFactor = 1 / zoomFactor;
            }

            // Guardar posição do rato antes do zoom para focar nele (opcional, aqui simplificado)
            scale *= zoomFactor;

            // Limites de zoom manual
            if (scale < 0.1) scale = 0.1;
            if (scale > 10.0) scale = 10.0;

            updateFrame();
            event.consume();
        });

        // PAN (Arrastar) - Início
        this.setOnMousePressed(event -> {
            lastMouseX = event.getX();
            lastMouseY = event.getY();
        });

        // PAN (Arrastar) - Movimento
        this.setOnMouseDragged(event -> {
            double deltaX = event.getX() - lastMouseX;
            double deltaY = event.getY() - lastMouseY;

            translateX += deltaX;
            translateY += deltaY;

            lastMouseX = event.getX();
            lastMouseY = event.getY();

            updateFrame();
        });
    }

    private void setupMapCoordinates() {
        // Zona do Porto (Em Baixo)
        addStation("Porto - São Bento", 200, 900);
        addStation("Porto - Campanhã", 250, 850);
        addStation("Contumil", 300, 800);
        addStation("Leixões", 50, 800);
        addStation("Leça do Balio", 100, 800);
        addStation("São Mamede de Infesta", 150, 800);
        addStation("São Gemil", 220, 800);

        // Subida para o Minho
        addStation("Nine", 350, 600);
        addStation("Barcelos", 350, 500);
        addStation("Darque", 350, 350);
        addStation("Viana do Castelo", 320, 300);
        addStation("Caminha", 300, 150);
        addStation("São Pedro da Torre", 320, 100);
        addStation("Valença", 350, 50);

        // Variantes
        addStation("Porto São Bento", 200, 900);
        addStation("Porto Campanhã", 250, 850);
    }

    private void addStation(String name, double x, double y) {
        stationCoordinates.put(name, new Point2D(x, y));
    }

    // --- CARREGAMENTO DE DADOS ---
    public void loadSchedule(Map<Train, List<String>> trainSchedules, LocalTime startTime) {
        this.simulationTime = startTime;
        this.segments.clear();
        this.activeTrains.clear();

        for (Map.Entry<Train, List<String>> entry : trainSchedules.entrySet()) {
            parseTrainLogToVisuals(entry.getKey(), entry.getValue());
        }

        // IMPORTANTE: Recalcula o zoom para ver tudo sempre que carrega novos dados
        fitContent();

        updateFrame();
    }

    private void parseTrainLogToVisuals(Train train, List<String> logs) {
        VisualTrain vTrain = new VisualTrain(train);
        boolean hasValidSegments = false;

        for (String line : logs) {
            if (!line.contains("|")) continue;
            String[] parts = line.split("\\|");
            if (parts.length < 6) continue;

            try {
                String startSt = parts[1].trim();
                String endSt = parts[2].trim();
                String type = parts[3].trim();
                String entryStr = parts[5].trim();
                String exitStr = parts[6].trim();

                if (startSt.equalsIgnoreCase("START")) continue;

                Point2D p1 = stationCoordinates.get(startSt);
                Point2D p2 = stationCoordinates.get(endSt);

                // Fallback visual
                if (p1 == null) { System.out.println("⚠️ Estação sem coord: " + startSt); p1 = new Point2D(400, 400); }
                if (p2 == null) { System.out.println("⚠️ Estação sem coord: " + endSt); p2 = new Point2D(400, 400); }

                if (segments.stream().noneMatch(s -> s.matches(startSt, endSt))) {
                    boolean isDouble = type.equalsIgnoreCase("Double");
                    segments.add(new VisualSegment(startSt, endSt, p1, p2, isDouble));
                }

                if (!entryStr.equals("N/A") && !exitStr.equals("N/A")) {
                    LocalTime entryTime = LocalTime.parse(entryStr);
                    LocalTime exitTime = LocalTime.parse(exitStr);
                    if (exitTime.isBefore(entryTime)) continue;
                    vTrain.addMovement(entryTime, exitTime, p1, p2);
                    hasValidSegments = true;
                }
            } catch (Exception e) {
                // Ignore parse errors
            }
        }
        if (hasValidSegments) activeTrains.add(vTrain);
    }

    // --- LÓGICA DE DESENHO E ANIMAÇÃO ---

    public void startAnimation() {
        if (timer != null) timer.stop();
        timer = new AnimationTimer() {
            private long lastUpdate = 0;
            @Override
            public void handle(long now) {
                if (lastUpdate == 0) { lastUpdate = now; return; }
                double elapsedSeconds = (now - lastUpdate) / 1_000_000_000.0;
                lastUpdate = now;
                long secondsToAdd = (long) (elapsedSeconds * speedFactor);
                if (simulationTime != null) {
                    simulationTime = simulationTime.plusSeconds(secondsToAdd);
                }
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

    private void updateFrame() {
        GraphicsContext gc = canvas.getGraphicsContext2D();
        double w = getWidth();
        double h = getHeight();

        // 1. Limpar
        gc.clearRect(0, 0, w, h);

        // 2. Aplicar Transformação (Zoom & Pan)
        gc.save();
        gc.translate(translateX, translateY);
        gc.scale(scale, scale);

        // 3. Desenhar Grelha de Fundo
        drawGrid(gc);

        // 4. Desenhar Segmentos
        gc.setLineWidth(4);
        for (VisualSegment seg : segments) {
            if (seg.isDouble) {
                gc.setStroke(Color.DODGERBLUE);
                gc.setLineWidth(6);
                gc.strokeLine(seg.p1.getX(), seg.p1.getY(), seg.p2.getX(), seg.p2.getY());
                gc.setStroke(Color.BLACK);
                gc.setLineWidth(2);
                gc.strokeLine(seg.p1.getX(), seg.p1.getY(), seg.p2.getX(), seg.p2.getY());
            } else {
                gc.setStroke(Color.ORANGE);
                gc.setLineWidth(3);
                gc.strokeLine(seg.p1.getX(), seg.p1.getY(), seg.p2.getX(), seg.p2.getY());
            }
        }

        // 5. Desenhar Estações
        for (Map.Entry<String, Point2D> entry : stationCoordinates.entrySet()) {
            Point2D p = entry.getValue();
            gc.setFill(Color.web("#333"));
            gc.fillOval(p.getX() - 8, p.getY() - 8, 16, 16);
            gc.setFill(Color.LIGHTGRAY);
            gc.fillOval(p.getX() - 5, p.getY() - 5, 10, 10);
            gc.setFill(Color.WHITE);
            gc.setFont(Font.font("Arial", FontWeight.NORMAL, 12));
            gc.fillText(entry.getKey(), p.getX() + 12, p.getY() + 4);
        }

        // 6. Desenhar Comboios
        for (VisualTrain train : activeTrains) {
            train.draw(gc, simulationTime);
        }

        gc.restore();

        // 7. Relógio (UI Fixa)
        gc.setFill(Color.WHITE);
        gc.setFont(Font.font("Monospaced", FontWeight.BOLD, 20));
        String timeStr = (simulationTime != null) ? simulationTime.format(DateTimeFormatter.ofPattern("HH:mm:ss")) : "--:--:--";
        gc.fillText("TIME: " + timeStr, w - 200, 30);

        gc.setFont(Font.font(12));
        gc.setFill(Color.LIGHTGRAY);
        gc.fillText(String.format("Zoom: %.2fx | Speed: %.0fx", scale, speedFactor), w - 200, 50);
        gc.fillText("Scroll to Zoom | Drag to Pan", w - 200, 70);
    }

    private void drawGrid(GraphicsContext gc) {
        gc.setStroke(Color.web("#222"));
        gc.setLineWidth(1);
        // Aumentei o range da grelha para garantir que cobre tudo se desenharmos longe
        for (int i = -1000; i < 3000; i += 100) {
            gc.strokeLine(i, -1000, i, 3000);
            gc.strokeLine(-1000, i, 3000, i);
        }
    }

    // --- CLASSES INTERNAS (Dados) ---
    private static class VisualSegment {
        String start, end;
        Point2D p1, p2;
        boolean isDouble;
        public VisualSegment(String s, String e, Point2D p1, Point2D p2, boolean d) {
            this.start = s; this.end = e; this.p1 = p1; this.p2 = p2; this.isDouble = d;
        }
        public boolean matches(String s, String e) {
            return (start.equals(s) && end.equals(e)) || (start.equals(e) && end.equals(s));
        }
    }

    private static class VisualTrain {
        Train train;
        List<TrainMovement> movements = new ArrayList<>();
        public VisualTrain(Train train) { this.train = train; }
        public void addMovement(LocalTime s, LocalTime e, Point2D p1, Point2D p2) { movements.add(new TrainMovement(s, e, p1, p2)); }
        public void draw(GraphicsContext gc, LocalTime curTime) {
            if (curTime == null) return;
            for (TrainMovement mv : movements) {
                if (!curTime.isBefore(mv.start) && !curTime.isAfter(mv.end)) {
                    long totalSec = Duration.between(mv.start, mv.end).getSeconds();
                    long elapsed = Duration.between(mv.start, curTime).getSeconds();
                    if (totalSec == 0) totalSec = 1;
                    double progress = (double) elapsed / totalSec;

                    double x = mv.p1.getX() + (mv.p2.getX() - mv.p1.getX()) * progress;
                    double y = mv.p1.getY() + (mv.p2.getY() - mv.p1.getY()) * progress;

                    gc.setFill(Color.LIMEGREEN);
                    gc.setEffect(new javafx.scene.effect.DropShadow(10, Color.LIME));
                    gc.fillOval(x - 8, y - 8, 16, 16);
                    gc.setEffect(null);

                    gc.setFill(Color.YELLOW);
                    gc.setFont(Font.font("Arial", FontWeight.BOLD, 14));
                    gc.fillText(train.getTrainId(), x + 10, y - 10);
                    return;
                }
            }
        }
    }

    private static class TrainMovement {
        LocalTime start, end;
        Point2D p1, p2;
        public TrainMovement(LocalTime s, LocalTime e, Point2D p1, Point2D p2) { start = s; end = e; this.p1 = p1; this.p2 = p2; }
    }
}