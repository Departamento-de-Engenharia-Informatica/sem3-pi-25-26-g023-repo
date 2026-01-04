package pt.ipp.isep.dei.domain;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;
import pt.ipp.isep.dei.controller.RoutePlannerController;

/**
 * Manages the loading and storage of all inventory-related domain objects.
 */
public class InventoryManager {

    private final Inventory inventory = new Inventory();
    private final Map<String, Item> items = new HashMap<>();
    private final List<Warehouse> warehouses = new ArrayList<>();

    // --- Counters for Main Summary ---
    private int itemsCount = 0;
    private int baysCount = 0;
    private int warehouseCount = 0;
    private int wagonsCount = 0;
    private int returnsCount = 0;
    private int ordersCount = 0;
    private int validStationCount = 0;
    private int invalidStationCount = 0;

    // NOVO: Armazenamento temporário da rota planeada para importação na USLP09
    private static RoutePlannerController.PlannedRoute lastPlannedRoute = null;

    public InventoryManager() {
    }

    public static void savePlannedRoute(RoutePlannerController.PlannedRoute route) {
        lastPlannedRoute = route;
    }

    public static RoutePlannerController.PlannedRoute getLastPlannedRoute() {
        return lastPlannedRoute;
    }

    /**
     * Retorna as encomendas carregadas do sistema.
     */
    public List<Order> getOrders() {
        try {
            return loadOrders("src/main/java/pt/ipp/isep/dei/FicheirosCSV/orders.csv",
                    "src/main/java/pt/ipp/isep/dei/FicheirosCSV/order_lines.csv");
        } catch (IOException e) {
            return new ArrayList<>();
        }
    }

    public void loadItems(String filePath) throws IOException {
        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line;
            br.readLine();
            while ((line = br.readLine()) != null) {
                String[] p = line.contains(";") ? line.split(";") : line.split(",");
                if (p.length < 5) continue;
                try {
                    String sku = p[0].trim();
                    String name = p[1].trim();
                    String category = p[2].trim();
                    String unit = p[3].trim();
                    double weight = Double.parseDouble(p[4].trim());
                    items.put(sku, new Item(sku, name, category, unit, weight));
                } catch (Exception e) { }
            }
        }
        this.itemsCount = items.size();
    }

    public List<Bay> loadBays(String filePath) throws IOException {
        List<Bay> allBays = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line;
            br.readLine();
            while ((line = br.readLine()) != null) {
                String[] p = line.contains(";") ? line.split(";") : line.split(",");
                if (p.length < 4) continue;
                try {
                    String warehouseId = p[0].trim();
                    int aisle = Integer.parseInt(p[1].trim());
                    int bay = Integer.parseInt(p[2].trim());
                    int capacity = Integer.parseInt(p[3].trim());
                    Bay newBay = new Bay(warehouseId, aisle, bay, capacity);
                    allBays.add(newBay);
                    Warehouse wh = warehouses.stream()
                            .filter(w -> w.getWarehouseId().equals(warehouseId))
                            .findFirst()
                            .orElseGet(() -> {
                                Warehouse w = new Warehouse(warehouseId);
                                warehouses.add(w);
                                return w;
                            });
                    wh.addBay(newBay);
                } catch (Exception e) { }
            }
        }
        warehouses.sort(Comparator.comparing(Warehouse::getWarehouseId));
        this.baysCount = allBays.size();
        this.warehouseCount = warehouses.size();
        return allBays;
    }

    public List<Warehouse> getWarehouses() {
        return Collections.unmodifiableList(warehouses);
    }

    public List<Wagon> loadWagons(String filePath) throws IOException {
        Map<String, Wagon> wagons = new LinkedHashMap<>();
        DateTimeFormatter fmt = DateTimeFormatter.ISO_DATE_TIME;
        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line;
            br.readLine();
            while ((line = br.readLine()) != null) {
                String[] p = line.contains(";") ? line.split(";") : line.split(",");
                if (p.length < 6) continue;
                try {
                    String wagonId = p[0].trim();
                    String boxId = p[1].trim();
                    String sku = p[2].trim();
                    int qty = Integer.parseInt(p[3].trim());
                    String expRaw = p[4].trim();
                    String recvRaw = p[5].trim();
                    if (!items.containsKey(sku)) continue;
                    LocalDate expiry = null;
                    if (!expRaw.isEmpty() && !expRaw.equals("null")) {
                        try { expiry = LocalDate.parse(expRaw); } catch (Exception e) { }
                    }
                    LocalDateTime receivedAt = LocalDateTime.parse(recvRaw, fmt);
                    Box box = new Box(boxId, sku, qty, expiry, receivedAt, null, null);
                    wagons.computeIfAbsent(wagonId, Wagon::new).addBox(box);
                } catch (Exception e) { }
            }
        }
        this.wagonsCount = wagons.size();
        return new ArrayList<>(wagons.values());
    }

    public List<Return> loadReturns(String filePath) throws IOException {
        List<Return> list = new ArrayList<>();
        DateTimeFormatter fmt = DateTimeFormatter.ISO_DATE_TIME;
        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line;
            br.readLine();
            while ((line = br.readLine()) != null) {
                String[] p = line.contains(";") ? line.split(";") : line.split(",");
                if (p.length < 5) continue;
                try {
                    String id = p[0].trim();
                    if (id.startsWith("\uFEFF")) { id = id.substring(1); }
                    String sku = p[1].trim();
                    int qty = Integer.parseInt(p[2].trim());
                    String reason = p[3].trim();
                    LocalDateTime ts = LocalDateTime.parse(p[4].trim(), fmt);
                    LocalDateTime exp = null;
                    if (p.length > 5) {
                        String expRaw = p[5].trim();
                        if (!expRaw.isEmpty() && !expRaw.equals("null") && !expRaw.equals("N/A")) {
                            try {
                                if (expRaw.contains("T")) exp = LocalDateTime.parse(expRaw, fmt);
                                else exp = LocalDate.parse(expRaw).atStartOfDay();
                            } catch (Exception e) { }
                        }
                    }
                    list.add(new Return(id, sku, qty, reason, ts, exp));
                } catch (Exception e) { }
            }
        }
        this.returnsCount = list.size();
        return list;
    }

    public List<Order> loadOrders(String ordersPath, String linesPath) throws IOException {
        Map<String, Order> orders = new LinkedHashMap<>();
        DateTimeFormatter fmt = DateTimeFormatter.ISO_DATE_TIME;
        try (BufferedReader br = new BufferedReader(new FileReader(ordersPath))) {
            String line;
            br.readLine();
            while ((line = br.readLine()) != null) {
                String[] p = line.contains(";") ? line.split(";") : line.split(",");
                if (p.length < 3) continue;
                try {
                    String id = p[0].trim();
                    LocalDate due = LocalDateTime.parse(p[1].trim(), fmt).toLocalDate();
                    int priority = Integer.parseInt(p[2].trim());
                    orders.put(id, new Order(id, priority, due));
                } catch (Exception e) { }
            }
        }
        try (BufferedReader br = new BufferedReader(new FileReader(linesPath))) {
            String line;
            br.readLine();
            while ((line = br.readLine()) != null) {
                String[] p = line.contains(";") ? line.split(";") : line.split(",");
                if (p.length < 4) continue;
                try {
                    String orderId = p[0].trim();
                    int lineNo = Integer.parseInt(p[1].trim());
                    String sku = p[2].trim();
                    int qty = Integer.parseInt(p[3].trim());
                    if (orders.containsKey(orderId)) {
                        orders.get(orderId).lines.add(new OrderLine(lineNo, sku, qty));
                    }
                } catch (Exception e) { }
            }
        }
        List<Order> result = orders.values().stream().filter(o -> !o.lines.isEmpty()).collect(Collectors.toList());
        this.ordersCount = result.size();
        return result;
    }

    public List<EuropeanStation> loadEuropeanStations(String filePath) {
        List<EuropeanStation> stations = new ArrayList<>();
        int currentId = 1;
        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            br.readLine();
            String line;
            while ((line = br.readLine()) != null) {
                if (line.trim().isEmpty()) continue;
                String[] fields = parseCSVLine(line);
                try {
                    String stationName, country, timeZoneGroup;
                    double lat, lon;
                    boolean city, main, airport;
                    if (fields.length == 8) {
                        stationName = fields[0].trim(); lat = Double.parseDouble(fields[1].trim());
                        lon = Double.parseDouble(fields[2].trim()); country = fields[3].trim();
                        timeZoneGroup = fields[4].trim(); city = parseBoolean(fields[5].trim());
                        main = parseBoolean(fields[6].trim()); airport = parseBoolean(fields[7].trim());
                    } else {
                        country = fields[0].trim(); timeZoneGroup = fields[2].trim();
                        stationName = fields[3].trim(); lat = Double.parseDouble(fields[4].trim());
                        lon = Double.parseDouble(fields[5].trim()); city = parseBoolean(fields[6].trim());
                        main = parseBoolean(fields[7].trim()); airport = parseBoolean(fields[8].trim());
                    }
                    stations.add(new EuropeanStation(currentId++, stationName, country, timeZoneGroup, lat, lon, city, main, airport));
                    this.validStationCount++;
                } catch (Exception e) { this.invalidStationCount++; }
            }
        } catch (IOException e) { throw new RuntimeException(e); }
        return stations;
    }

    public Inventory getInventory() { return inventory; }
    public Map<String, Item> getItemsMap() { return Collections.unmodifiableMap(items); }
    public List<Item> getItems() { return new ArrayList<>(items.values()); }
    public int getItemsCount() { return itemsCount; }
    public int getBaysCount() { return baysCount; }
    public int getWarehouseCount() { return warehouseCount; }
    public int getWagonsCount() { return wagonsCount; }
    public int getReturnsCount() { return returnsCount; }
    public int getOrdersCount() { return ordersCount; }
    public int getValidStationCount() { return validStationCount; }
    public int getInvalidStationCount() { return invalidStationCount; }

    private boolean parseBoolean(String text) {
        return text != null && text.trim().equalsIgnoreCase("True");
    }

    private String[] parseCSVLine(String line) {
        List<String> fields = new ArrayList<>();
        StringBuilder currentField = new StringBuilder();
        boolean inQuotes = false;
        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            if (inQuotes) {
                if (c == '"') {
                    if (i + 1 < line.length() && line.charAt(i + 1) == '"') { currentField.append('"'); i++; }
                    else inQuotes = false;
                } else currentField.append(c);
            } else {
                if (c == '"') inQuotes = true;
                else if (c == ',') { fields.add(currentField.toString()); currentField.setLength(0); }
                else currentField.append(c);
            }
        }
        fields.add(currentField.toString());
        return fields.toArray(new String[0]);
    }
}