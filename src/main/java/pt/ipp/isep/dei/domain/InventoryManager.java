package pt.ipp.isep.dei.domain;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * Principal Manager - Version 2.2 ("Silent Mode" with European Stations support)
 * This version is modified to NOT print anything to the console.
 * Only loads data and stores counters for Main.java to use.
 * Supports USEI06-08 European station data loading.
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

    /**
     * Default constructor.
     */
    public InventoryManager() {
        // Initialization handled in load methods
    }

    /**
     * Loads items from the CSV file in silent mode.
     * Counts valid items but does not print to console.
     *
     * @param filePath the path to the items CSV file
     * @throws IOException if the file cannot be read
     */
    public void loadItems(String filePath) throws IOException {
        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line;
            br.readLine(); // Skip header
            while ((line = br.readLine()) != null) {
                String[] p = line.contains(";") ? line.split(";") : line.split(",");
                if (p.length < 5) {
                    continue;
                }
                try {
                    String sku = p[0].trim();
                    String name = p[1].trim();
                    String category = p[2].trim();
                    String unit = p[3].trim();
                    double weight = Double.parseDouble(p[4].trim());
                    items.put(sku, new Item(sku, name, category, unit, weight));
                } catch (Exception e) {
                    // Silently ignore parsing errors
                }
            }
        }
        this.itemsCount = items.size();
    }

    /**
     * Loads bays from warehouses CSV file in silent mode.
     * Organizes bays into warehouses and counts statistics.
     *
     * @param filePath the path to the bays CSV file
     * @return list of all loaded bays
     * @throws IOException if the file cannot be read
     */
    public List<Bay> loadBays(String filePath) throws IOException {
        List<Bay> allBays = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line;
            br.readLine(); // Skip header
            while ((line = br.readLine()) != null) {
                String[] p = line.contains(";") ? line.split(";") : line.split(",");
                if (p.length < 4) {
                    continue;
                }
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
                } catch (Exception e) {
                    // Silently ignore parsing errors
                }
            }
        }
        // Sort warehouses for consistent ordering
        warehouses.sort(Comparator.comparing(Warehouse::getWarehouseId));
        this.baysCount = allBays.size();
        this.warehouseCount = warehouses.size();
        return allBays;
    }

    /**
     * Returns an unmodifiable list of all warehouses.
     *
     * @return list of warehouses
     */
    public List<Warehouse> getWarehouses() {
        return Collections.unmodifiableList(warehouses);
    }

    /**
     * Loads wagons with boxes from CSV file in silent mode.
     * Parses wagon data and associates boxes with items.
     *
     * @param filePath the path to the wagons CSV file
     * @return list of all loaded wagons
     * @throws IOException if the file cannot be read
     */
    public List<Wagon> loadWagons(String filePath) throws IOException {
        Map<String, Wagon> wagons = new LinkedHashMap<>();
        DateTimeFormatter fmt = DateTimeFormatter.ISO_DATE_TIME;

        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line;
            br.readLine(); // Skip header
            while ((line = br.readLine()) != null) {
                String[] p = line.contains(";") ? line.split(";") : line.split(",");
                if (p.length < 6) {
                    continue;
                }
                try {
                    String wagonId = p[0].trim();
                    String boxId = p[1].trim();
                    String sku = p[2].trim();
                    int qty = Integer.parseInt(p[3].trim());
                    String expRaw = p[4].trim();
                    String recvRaw = p[5].trim();

                    // Skip if item SKU is not known
                    if (!items.containsKey(sku)) {
                        continue;
                    }

                    LocalDate expiry = null;
                    if (!expRaw.isEmpty() && !expRaw.equals("null")) {
                        try {
                            expiry = LocalDate.parse(expRaw);
                        } catch (Exception e) {
                            // Silently ignore date parsing errors
                        }
                    }

                    LocalDateTime receivedAt = LocalDateTime.parse(recvRaw, fmt);

                    Box box = new Box(boxId, sku, qty, expiry, receivedAt, null, null);
                    wagons.computeIfAbsent(wagonId, Wagon::new).addBox(box);
                } catch (Exception e) {
                    // Silently ignore parsing errors
                }
            }
        }
        this.wagonsCount = wagons.size();
        return new ArrayList<>(wagons.values());
    }

    /**
     * Loads returns from clients CSV file in silent mode.
     * Parses return data with timestamps and reasons.
     *
     * @param filePath the path to the returns CSV file
     * @return list of all loaded returns
     * @throws IOException if the file cannot be read
     */
    public List<Return> loadReturns(String filePath) throws IOException {
        List<Return> list = new ArrayList<>();
        DateTimeFormatter fmt = DateTimeFormatter.ISO_DATE_TIME;
        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line;
            br.readLine(); // Skip header

            while ((line = br.readLine()) != null) {
                String[] p = line.contains(";") ? line.split(";") : line.split(",");
                if (p.length < 5) {
                    continue;
                }
                try {
                    String id = p[0].trim();
                    // Handle BOM character if present
                    if (id.startsWith("\uFEFF")) {
                        id = id.substring(1);
                    }

                    String sku = p[1].trim();
                    int qty = Integer.parseInt(p[2].trim());
                    String reason = p[3].trim();

                    LocalDateTime ts;
                    try {
                        ts = LocalDateTime.parse(p[4].trim(), fmt);
                    } catch (Exception e) {
                        continue; // Skip if timestamp is invalid
                    }

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
                            } catch (Exception e) {
                                // Silently ignore expiry date parsing errors
                            }
                        }
                    }

                    Return returnItem = new Return(id, sku, qty, reason, ts, exp);
                    list.add(returnItem);

                } catch (Exception e) {
                    // Silently ignore parsing errors
                }
            }
        }
        this.returnsCount = list.size();
        return list;
    }

    /**
     * Loads orders and their respective lines from CSV files in silent mode.
     * Associates order lines with orders and validates data.
     *
     * @param ordersPath the path to the orders CSV file
     * @param linesPath the path to the order lines CSV file
     * @return list of all loaded orders with their lines
     * @throws IOException if either file cannot be read
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
                if (p.length < 3) {
                    continue;
                }
                try {
                    String id = p[0].trim();
                    LocalDate due = LocalDateTime.parse(p[1].trim(), fmt).toLocalDate();
                    int priority = Integer.parseInt(p[2].trim());
                    orders.put(id, new Order(id, priority, due));
                } catch (Exception e) {
                    // Silently ignore parsing errors
                }
            }
        }

        // Load order lines
        try (BufferedReader br = new BufferedReader(new FileReader(linesPath))) {
            String line;
            br.readLine(); // Skip header
            while ((line = br.readLine()) != null) {
                String[] p = line.contains(";") ? line.split(";") : line.split(",");
                if (p.length < 4) {
                    continue;
                }
                try {
                    String orderId = p[0].trim();
                    int lineNo = Integer.parseInt(p[1].trim());
                    String sku = p[2].trim();
                    int qty = Integer.parseInt(p[3].trim());

                    if (orders.containsKey(orderId)) {
                        orders.get(orderId).lines.add(new OrderLine(lineNo, sku, qty));
                    }
                } catch (Exception e) {
                    // Silently ignore parsing errors
                }
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
     * Loads European stations from CSV file for USEI06-08 in silent mode.
     * Validates station data and coordinates, counts valid/invalid records.
     *
     * @param filePath the path to the European stations CSV file
     * @return list of valid EuropeanStation objects
     * @throws IOException if the file cannot be read
     */
    public List<EuropeanStation> loadEuropeanStations(String filePath) throws IOException {
        List<EuropeanStation> stations = new ArrayList<>();
        this.validStationCount = 0;
        this.invalidStationCount = 0;

        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String header = br.readLine(); // Skip header
            String line;

            while ((line = br.readLine()) != null) {
                if (line.trim().isEmpty()) {
                    continue;
                }

                String[] fields = parseCSVLine(line);

                try {
                    if (fields.length < 9) {
                        throw new IllegalArgumentException("Insufficient columns in line");
                    }

                    String country = fields[0].trim();
                    String timeZoneGroup = fields[2].trim();
                    String stationName = fields[3].trim();
                    double latitude = Double.parseDouble(fields[4].trim());
                    double longitude = Double.parseDouble(fields[5].trim());
                    boolean isCity = parseBoolean(fields[6].trim());
                    boolean isMainStation = parseBoolean(fields[7].trim());
                    boolean isAirport = parseBoolean(fields[8].trim());

                    // Validate required fields
                    if (stationName.isEmpty() || country.isEmpty() || timeZoneGroup.isEmpty()) {
                        throw new IllegalArgumentException("Station, Country, or TimeZoneGroup is empty");
                    }

                    // Validate coordinate ranges
                    if (latitude < -90 || latitude > 90) {
                        throw new IllegalArgumentException("Latitude out of range: " + latitude);
                    }
                    if (longitude < -180 || longitude > 180) {
                        throw new IllegalArgumentException("Longitude out of range: " + longitude);
                    }

                    EuropeanStation station = new EuropeanStation(stationName, country, timeZoneGroup,
                            latitude, longitude, isCity, isMainStation, isAirport);
                    stations.add(station);
                    this.validStationCount++;

                } catch (Exception e) {
                    // Silent mode: count invalid records but don't print errors
                    this.invalidStationCount++;
                }
            }
        }

        return stations;
    }

    /**
     * Returns the main inventory instance.
     *
     * @return the inventory
     */
    public Inventory getInventory() {
        return inventory;
    }

    /**
     * Returns an unmodifiable map of all items.
     *
     * @return map of items by SKU
     */
    public Map<String, Item> getItemsMap() {
        return Collections.unmodifiableMap(items);
    }

    /**
     * Returns a list of all items.
     *
     * @return list of items
     */
    public List<Item> getItems() {
        return new ArrayList<>(items.values());
    }

    // --- Statistics Getters ---

    /**
     * Gets the count of valid items loaded.
     *
     * @return number of valid items
     */
    public int getItemsCount() {
        return itemsCount;
    }

    /**
     * Gets the count of bays loaded across all warehouses.
     *
     * @return number of bays
     */
    public int getBaysCount() {
        return baysCount;
    }

    /**
     * Gets the count of warehouses loaded.
     *
     * @return number of warehouses
     */
    public int getWarehouseCount() {
        return warehouseCount;
    }

    /**
     * Gets the count of wagons loaded.
     *
     * @return number of wagons
     */
    public int getWagonsCount() {
        return wagonsCount;
    }

    /**
     * Gets the count of returns loaded.
     *
     * @return number of returns
     */
    public int getReturnsCount() {
        return returnsCount;
    }

    /**
     * Gets the count of orders loaded.
     *
     * @return number of orders
     */
    public int getOrdersCount() {
        return ordersCount;
    }

    /**
     * Gets the count of valid European stations loaded.
     *
     * @return number of valid stations
     */
    public int getValidStationCount() {
        return validStationCount;
    }

    /**
     * Gets the count of invalid European station records rejected.
     *
     * @return number of invalid stations
     */
    public int getInvalidStationCount() {
        return invalidStationCount;
    }

    // --- Private Helper Methods ---

    /**
     * Parses boolean values from CSV string representations.
     * Handles "True"/"False" strings (case-insensitive).
     *
     * @param text the string to parse
     * @return true if text represents a boolean true value
     */
    private boolean parseBoolean(String text) {
        return text != null && text.trim().equalsIgnoreCase("True");
    }

    /**
     * Robust CSV line parser that handles quoted fields and commas within values.
     * Properly handles escaped quotes and field boundaries.
     *
     * @param line the CSV line to parse
     * @return array of field values
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
                        // Escaped quote - add to field
                        currentField.append('"');
                        i++;
                    } else {
                        // End of quoted field
                        inQuotes = false;
                    }
                } else {
                    // Regular character inside quotes
                    currentField.append(c);
                }
            } else {
                if (c == '"') {
                    // Start of quoted field
                    inQuotes = true;
                } else if (c == ',') {
                    // Field separator
                    fields.add(currentField.toString());
                    currentField.setLength(0);
                } else {
                    // Regular character outside quotes
                    currentField.append(c);
                }
            }
        }
        // Add the last field
        fields.add(currentField.toString());
        return fields.toArray(new String[0]);
    }
}