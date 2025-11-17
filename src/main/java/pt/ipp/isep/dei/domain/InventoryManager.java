package pt.ipp.isep.dei.domain;


import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

//

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

    /**
     * Default constructor.
     */
    public InventoryManager() {
        // Initialization handled in load methods
    }

    /**
     * Loads items from the CSV file in silent mode.
     */
    public void loadItems(String filePath) throws IOException {
        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line;
            br.readLine(); // Skip header
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
                } catch (Exception e) { /* Silently ignore */ }
            }
        }
        this.itemsCount = items.size();
    }

    /**
     * Loads bays from warehouses CSV file in silent mode.
     */
    public List<Bay> loadBays(String filePath) throws IOException {
        List<Bay> allBays = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line;
            br.readLine(); // Skip header
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

                    // Find or create warehouse
                    Warehouse wh = warehouses.stream()
                            .filter(w -> w.getWarehouseId().equals(warehouseId))
                            .findFirst()
                            .orElseGet(() -> {
                                Warehouse w = new Warehouse(warehouseId);
                                warehouses.add(w);
                                return w;
                            });
                    wh.addBay(newBay);
                } catch (Exception e) { /* Silently ignore */ }
            }
        }
        warehouses.sort(Comparator.comparing(Warehouse::getWarehouseId));
        this.baysCount = allBays.size();
        this.warehouseCount = warehouses.size();
        return allBays;
    }

    /**
     * Returns an unmodifiable list of all warehouses.
     */
    public List<Warehouse> getWarehouses() {
        return Collections.unmodifiableList(warehouses);
    }

    /**
     * Loads wagons with boxes from CSV file in silent mode.
     */
    public List<Wagon> loadWagons(String filePath) throws IOException {
        Map<String, Wagon> wagons = new LinkedHashMap<>();
        DateTimeFormatter fmt = DateTimeFormatter.ISO_DATE_TIME;

        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line;
            br.readLine(); // Skip header
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
                        try {
                            expiry = LocalDate.parse(expRaw);
                        } catch (Exception e) { /* Silently ignore date parsing errors */ }
                    }

                    LocalDateTime receivedAt = LocalDateTime.parse(recvRaw, fmt);

                    Box box = new Box(boxId, sku, qty, expiry, receivedAt, null, null);
                    wagons.computeIfAbsent(wagonId, Wagon::new).addBox(box);
                } catch (Exception e) { /* Silently ignore parsing errors */ }
            }
        }
        this.wagonsCount = wagons.size();
        return new ArrayList<>(wagons.values());
    }

    /**
     * Loads returns from clients CSV file in silent mode.
     */
    public List<Return> loadReturns(String filePath) throws IOException {
        List<Return> list = new ArrayList<>();
        DateTimeFormatter fmt = DateTimeFormatter.ISO_DATE_TIME;
        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line;
            br.readLine(); // Skip header

            while ((line = br.readLine()) != null) {
                String[] p = line.contains(";") ? line.split(";") : line.split(",");
                if (p.length < 5) continue;
                try {
                    String id = p[0].trim();
                    if (id.startsWith("\uFEFF")) { id = id.substring(1); }

                    String sku = p[1].trim();
                    int qty = Integer.parseInt(p[2].trim());
                    String reason = p[3].trim();

                    LocalDateTime ts;
                    try {
                        ts = LocalDateTime.parse(p[4].trim(), fmt);
                    } catch (Exception e) { continue; } // Skip if timestamp is invalid

                    LocalDateTime exp = null;
                    if (p.length > 5) {
                        String expRaw = p[5].trim();
                        if (!expRaw.isEmpty() && !expRaw.equals("null") && !expRaw.equals("N/A")) {
                            try {
                                if (expRaw.contains("T")) {
                                    exp = LocalDateTime.parse(expRaw, fmt);
                                } else {
                                    exp = LocalDate.parse(expRaw).atStartOfDay();
                                }
                            } catch (Exception e) { /* Silently ignore expiry date parsing errors */ }
                        }
                    }

                    Return returnItem = new Return(id, sku, qty, reason, ts, exp);
                    list.add(returnItem);

                } catch (Exception e) { /* Silently ignore parsing errors */ }
            }
        }
        this.returnsCount = list.size();
        return list;
    }

    /**
     * Loads orders and their respective lines from CSV files in silent mode.
     */
    public List<Order> loadOrders(String ordersPath, String linesPath) throws IOException {
        Map<String, Order> orders = new LinkedHashMap<>();
        DateTimeFormatter fmt = DateTimeFormatter.ISO_DATE_TIME;

        // Load orders
        try (BufferedReader br = new BufferedReader(new FileReader(ordersPath))) {
            String line;
            br.readLine(); // Skip header
            while ((line = br.readLine()) != null) {
                String[] p = line.contains(";") ? line.split(";") : line.split(",");
                if (p.length < 3) continue;
                try {
                    String id = p[0].trim();
                    LocalDate due = LocalDateTime.parse(p[1].trim(), fmt).toLocalDate();
                    int priority = Integer.parseInt(p[2].trim());
                    orders.put(id, new Order(id, priority, due));
                } catch (Exception e) { /* Silently ignore parsing errors */ }
            }
        }

        // Load order lines
        try (BufferedReader br = new BufferedReader(new FileReader(linesPath))) {
            String line;
            br.readLine(); // Skip header
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
                } catch (Exception e) { /* Silently ignore parsing errors */ }
            }
        }

        // Filter out orders with no lines
        List<Order> result = new ArrayList<>();
        for (Order order : orders.values()) {
            if (!order.lines.isEmpty()) {
                result.add(order);
            }
        }
        this.ordersCount = result.size();
        return result;
    }

    /**
     * Loads European stations from CSV file.
     */
    public List<EuropeanStation> loadEuropeanStations(String filePath) throws IOException {
        List<EuropeanStation> stations = new ArrayList<>();
        this.validStationCount = 0;
        this.invalidStationCount = 0;

        // CORREÇÃO: Contador para gerar IDs únicos sequenciais (facility_id)
        int currentId = 1;

        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            br.readLine(); // Skip header
            String line;

            while ((line = br.readLine()) != null) {
                if (line.trim().isEmpty()) continue;

                String[] fields = parseCSVLine(line);

                try {
                    if (fields.length < 9) {
                        throw new IllegalArgumentException("Insufficient columns in line");
                    }

                    // Mapeamento dos campos (baseado no seu código)
                    String country = fields[0].trim();
                    String timeZoneGroup = fields[2].trim();
                    String stationName = fields[3].trim();
                    double latitude = Double.parseDouble(fields[4].trim());
                    double longitude = Double.parseDouble(fields[5].trim());
                    boolean isCity = parseBoolean(fields[6].trim());
                    boolean isMainStation = parseBoolean(fields[7].trim());
                    boolean isAirport = parseBoolean(fields[8].trim());

                    // Validações...
                    if (stationName.isEmpty() || country.isEmpty() || timeZoneGroup.isEmpty() || latitude < -90 || latitude > 90 || longitude < -180 || longitude > 180) {
                        throw new IllegalArgumentException("Invalid data found.");
                    }

                    // CRIAÇÃO DO OBJETO: Agora passa 9 argumentos, incluindo o ID gerado
                    EuropeanStation station = new EuropeanStation(
                            currentId, // 1. int idEstacao (Corrigindo o erro 'Cannot resolve symbol')
                            stationName, // 2. String station
                            country,
                            timeZoneGroup,
                            latitude,
                            longitude,
                            isCity,
                            isMainStation,
                            isAirport);

                    stations.add(station);
                    this.validStationCount++;

                    currentId++; // Incrementa o ID para o próximo registro
                } catch (Exception e) {
                    this.invalidStationCount++;
                }
            }
        }

        return stations;
    }

    // --- Statistics Getters (Mantidos como estavam) ---

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


    // --- Private Helper Methods (Mantidos como estavam) ---

    private boolean parseBoolean(String text) {
        return text != null && text.trim().equalsIgnoreCase("True");
    }

    /**
     * Robust CSV line parser that handles quoted fields and commas within values.
     * (Usando a sua implementação robusta de parseCSVLine)
     */
    private String[] parseCSVLine(String line) {
        List<String> fields = new ArrayList<>();
        StringBuilder currentField = new StringBuilder();
        boolean inQuotes = false;

        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);

            if (inQuotes) {
                if (c == '"') {
                    if (i + 1 < line.length() && line.charAt(i + 1) == '"') {
                        currentField.append('"');
                        i++;
                    } else {
                        inQuotes = false;
                    }
                } else {
                    currentField.append(c);
                }
            } else {
                if (c == '"') {
                    inQuotes = true;
                } else if (c == ',') {
                    fields.add(currentField.toString());
                    currentField.setLength(0);
                } else {
                    currentField.append(c);
                }
            }
        }
        fields.add(currentField.toString());
        return fields.toArray(new String[0]);
    }
}