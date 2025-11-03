package pt.ipp.isep.dei.domain;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * Principal Manager
 */
public class InventoryManager {

    private final Inventory inventory = new Inventory();
    private final Map<String, Item> items = new HashMap<>();
    private final List<Warehouse> warehouses = new ArrayList<>();

    /**
     * Load items from the file
     */
    public void loadItems(String filePath) throws IOException {
        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line;
            br.readLine();
            while ((line = br.readLine()) != null) {
                String[] p = line.contains(";") ? line.split(";") : line.split(",");
                if (p.length < 5) {
                    System.err.println("Invalid item record: " + line);
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
                    System.err.println("Error parsing item: " + line + " - " + e.getMessage());
                }
            }
        }
        System.out.printf("✅ Loaded %d items%n", items.size());
    }

    /**
     * Load bays from warehouses
     */
    public List<Bay> loadBays(String filePath) throws IOException {
        List<Bay> allBays = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line;
            br.readLine();
            while ((line = br.readLine()) != null) {
                String[] p = line.contains(";") ? line.split(";") : line.split(",");
                if (p.length < 4) {
                    System.err.println("Invalid bay record: " + line);
                    continue;
                }
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
                } catch (Exception e) {
                    System.err.println("Error parsing bay: " + line + " - " + e.getMessage());
                }
            }
        }
        warehouses.sort(Comparator.comparing(Warehouse::getWarehouseId));
        System.out.printf("✅ Loaded %d bays across %d warehouses%n", allBays.size(), warehouses.size());
        return allBays;
    }

    public List<Warehouse> getWarehouses() {
        return Collections.unmodifiableList(warehouses);
    }

    /**
     * Load Wagons with boxes
     */
    public List<Wagon> loadWagons(String filePath) throws IOException {
        Map<String, Wagon> wagons = new LinkedHashMap<>();
        DateTimeFormatter fmt = DateTimeFormatter.ISO_DATE_TIME;

        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line;
            br.readLine();
            while ((line = br.readLine()) != null) {
                String[] p = line.contains(";") ? line.split(";") : line.split(",");
                if (p.length < 6) {
                    System.err.println("Invalid wagon record: " + line);
                    continue;
                }
                try {
                    String wagonId = p[0].trim();
                    String boxId = p[1].trim();
                    String sku = p[2].trim();
                    int qty = Integer.parseInt(p[3].trim());
                    String expRaw = p[4].trim();
                    String recvRaw = p[5].trim();

                    if (!items.containsKey(sku)) {
                        System.err.printf("❌ ERRO: SKU desconhecido '%s' no wagon %s%n", sku, wagonId);
                        continue;
                    }

                    LocalDate expiry = null;
                    if (!expRaw.isEmpty() && !expRaw.equals("null")) {
                        try {
                            expiry = LocalDate.parse(expRaw);
                        } catch (Exception e) {
                            System.err.println("Invalid expiry date format: " + expRaw);
                        }
                    }

                    LocalDateTime receivedAt = LocalDateTime.parse(recvRaw, fmt);

                    Box box = new Box(boxId, sku, qty, expiry, receivedAt, null, null);
                    wagons.computeIfAbsent(wagonId, Wagon::new).addBox(box);
                } catch (Exception e) {
                    System.err.println("Error parsing wagon: " + line + " - " + e.getMessage());
                }
            }
        }
        System.out.printf("✅ Loaded %d wagons%n", wagons.size());
        return new ArrayList<>(wagons.values());
    }

    /**
     * Load returns from clients
     */
    public List<Return> loadReturns(String filePath) throws IOException {
        List<Return> list = new ArrayList<>();
        DateTimeFormatter fmt = DateTimeFormatter.ISO_DATE_TIME;
        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line;
            br.readLine();

            while ((line = br.readLine()) != null) {
                String[] p = line.contains(";") ? line.split(";") : line.split(",");
                if (p.length < 5) {
                    System.err.println("Invalid return record: " + line);
                    continue;
                }
                try {
                    String id = p[0].trim();
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
                        System.err.println("Invalid timestamp format in return: " + line);
                        continue;
                    }

                    LocalDateTime exp = null;
                    if (p.length > 5) {
                        String expRaw = p[5].trim();
                        if (!expRaw.isEmpty() && !expRaw.equals("null") && !expRaw.equals("N/A")) {
                            try {
                                if (expRaw.contains("T")) {
                                    exp = LocalDateTime.parse(expRaw, fmt);
                                } else {
                                    try {
                                        exp = LocalDate.parse(expRaw).atStartOfDay();
                                    } catch (Exception e) {
                                        System.err.println("Invalid expiry date format in return: " + expRaw + " - " + line);
                                    }
                                }
                            } catch (Exception e) {
                                System.err.println("Error parsing expiry date in return: " + expRaw + " - " + line);
                            }
                        }
                    }

                    Return returnItem = new Return(id, sku, qty, reason, ts, exp);
                    list.add(returnItem);

                } catch (Exception e) {
                    System.err.println("Error parsing return: " + line + " - " + e.getMessage());
                }
            }
        }
        System.out.printf("✅ Loaded %d returns%n", list.size());
        return list;
    }

    /**
     * Load Orders and their respective lines
     */
    public List<Order> loadOrders(String ordersPath, String linesPath) throws IOException {
        Map<String, Order> orders = new LinkedHashMap<>();
        DateTimeFormatter fmt = DateTimeFormatter.ISO_DATE_TIME;

        try (BufferedReader br = new BufferedReader(new FileReader(ordersPath))) {
            String line;
            br.readLine();
            while ((line = br.readLine()) != null) {
                String[] p = line.contains(";") ? line.split(";") : line.split(",");
                if (p.length < 3) {
                    System.err.println("Invalid order record: " + line);
                    continue;
                }
                try {
                    String id = p[0].trim();
                    LocalDate due = LocalDateTime.parse(p[1].trim(), fmt).toLocalDate();
                    int priority = Integer.parseInt(p[2].trim());
                    orders.put(id, new Order(id, priority, due));
                } catch (Exception e) {
                    System.err.println("Error parsing order: " + line + " - " + e.getMessage());
                }
            }
        }

        try (BufferedReader br = new BufferedReader(new FileReader(linesPath))) {
            String line;
            br.readLine();
            while ((line = br.readLine()) != null) {
                String[] p = line.contains(";") ? line.split(";") : line.split(",");
                if (p.length < 4) {
                    System.err.println("Invalid order line record: " + line);
                    continue;
                }
                try {
                    String orderId = p[0].trim();
                    int lineNo = Integer.parseInt(p[1].trim());
                    String sku = p[2].trim();
                    int qty = Integer.parseInt(p[3].trim());

                    if (orders.containsKey(orderId)) {
                        orders.get(orderId).lines.add(new OrderLine(lineNo, sku, qty));
                    } else {
                        System.err.println("Order not found for line: " + orderId);
                    }
                } catch (Exception e) {
                    System.err.println("Error parsing order line: " + line + " - " + e.getMessage());
                }
            }
        }

        List<Order> result = new ArrayList<>();
        for (Order order : orders.values()) {
            if (order.lines.isEmpty()) {
                System.err.println("Order has no lines: " + order.orderId);
            } else {
                result.add(order);
            }
        }

        System.out.printf("✅ Loaded %d orders with lines%n", result.size());
        return result;
    }

    public Inventory getInventory() {

        return inventory;
    }

    public Map<String, Item> getItemsMap() {

        return Collections.unmodifiableMap(items);
    }

    public List<Item> getItems() {

        return new ArrayList<>(items.values());
    }

    // --- INÍCIO DO CÓDIGO ATUALIZADO (USEI06 - Sprint 2) ---

    /**
     * Carrega as estações europeias do ficheiro CSV (USEI06).
     * @param filePath Caminho para train_stations_europe.csv
     * @return Lista de EuropeanStation
     * @throws IOException Se o ficheiro não for encontrado
     */
    public List<EuropeanStation> loadEuropeanStations(String filePath) throws IOException {
        List<EuropeanStation> stations = new ArrayList<>();
        String line;
        int validCount = 0;
        int invalidCount = 0;

        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            // Ler cabeçalho
            String header = br.readLine();

            while ((line = br.readLine()) != null) {
                if (line.trim().isEmpty()) {
                    continue;
                }

                // *** CORREÇÃO: Usar o parser robusto em vez de line.split(",") ***
                String[] p = parseCSVLine(line);

                try {
                    // Mapeamento das colunas (baseado no header)
                    // p[0] = country
                    // p[1] = time_zone (ex: "('Europe/Paris',)") - ignorado
                    // p[2] = time_zone_group
                    // p[3] = station
                    // p[4] = latitude
                    // p[5] = longitude
                    // p[6] = is_city
                    // p[7] = is_main_station
                    // p[8] = is_airport

                    if (p.length < 9) {
                        throw new IllegalArgumentException("Linha com colunas insuficientes.");
                    }

                    String country = p[0].trim();
                    String timeZoneGroup = p[2].trim();
                    String stationName = p[3].trim();
                    double latitude = Double.parseDouble(p[4].trim());
                    double longitude = Double.parseDouble(p[5].trim());
                    boolean isCity = parseBoolean(p[6].trim());
                    boolean isMainStation = parseBoolean(p[7].trim());
                    boolean isAirport = parseBoolean(p[8].trim());

                    // Critérios de Aceitação (USEI06): Validação
                    if (stationName.isEmpty() || country.isEmpty() || timeZoneGroup.isEmpty()) {
                        throw new IllegalArgumentException("Station, Country, or TZG is empty.");
                    }
                    if (latitude < -90 || latitude > 90) {
                        throw new IllegalArgumentException("Latitude " + latitude + " out of range [-90, 90].");
                    }
                    if (longitude < -180 || longitude > 180) {
                        throw new IllegalArgumentException("Longitude " + longitude + " out of range [-180, 180].");
                    }

                    // Se for válido, cria e adiciona o objeto
                    EuropeanStation station = new EuropeanStation(stationName, country, timeZoneGroup, latitude, longitude, isCity, isMainStation, isAirport);
                    stations.add(station);
                    validCount++;

                } catch (Exception e) {
                    // Rejeita a linha inválida e reporta o problema
                    System.err.println("⚠️ [USEI06] Invalid station record rejected: " + line + " | Error: " + e.getMessage());
                    invalidCount++;
                }
            }
        }
        System.out.printf("✅ [USEI06] Loaded %d valid stations. (%d invalid rows rejected).%n", validCount, invalidCount);
        return stations;
    }

    /**
     * Helper para converter "True"/"False" de CSV para boolean.
     */
    private boolean parseBoolean(String text) {
        return text != null && text.trim().equalsIgnoreCase("True");
    }

    /**
     * NOVO MÉTODO: Parser de CSV robusto que lida com aspas.
     * Este método divide uma linha de CSV, respeitando aspas (").
     * Uma vírgula dentro de aspas não divide o campo.
     */
    private String[] parseCSVLine(String line) {
        List<String> fields = new ArrayList<>();
        StringBuilder currentField = new StringBuilder();
        boolean inQuotes = false;

        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);

            if (inQuotes) {
                if (c == '"') {
                    // Verifica se é uma aspa dupla de escape ("")
                    if (i + 1 < line.length() && line.charAt(i + 1) == '"') {
                        currentField.append('"');
                        i++; // Pula a próxima aspa
                    } else {
                        inQuotes = false; // Fim do campo com aspas
                    }
                } else {
                    currentField.append(c); // Carácter normal dentro das aspas
                }
            } else {
                if (c == '"') {
                    inQuotes = true; // Início de um campo com aspas
                } else if (c == ',') {
                    fields.add(currentField.toString()); // Fim do campo
                    currentField.setLength(0); // Limpa para o próximo campo
                } else {
                    currentField.append(c); // Carácter normal
                }
            }
        }
        // Adiciona o último campo
        fields.add(currentField.toString());

        return fields.toArray(new String[0]);
    }

    // --- FIM DO CÓDIGO ATUALIZADO (USEI06 - Sprint 2) ---
}