package pt.ipp.isep.dei.domain;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class InventoryManager {

    private final Inventory inventory = new Inventory();
    private final Map<String, Item> items = new HashMap<>();
    private final List<Warehouse> warehouses = new ArrayList<>();

    // ============================================================
    // ITEMS
    // ============================================================
    public void loadItems(String filePath) throws IOException {
        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line;
            br.readLine(); // Skip header
            while ((line = br.readLine()) != null) {
                // Tentar ambos os separadores: vírgula e ponto e vírgula
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

    // ============================================================
    // BAYS & WAREHOUSES
    // ============================================================
    public List<Bay> loadBays(String filePath) throws IOException {
        List<Bay> allBays = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line;
            br.readLine(); // Skip header
            while ((line = br.readLine()) != null) {
                // Tentar ambos os separadores: vírgula e ponto e vírgula
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

    // ============================================================
    // WAGONS
    // ============================================================
    public List<Wagon> loadWagons(String filePath) throws IOException {
        Map<String, Wagon> wagons = new LinkedHashMap<>();
        DateTimeFormatter fmt = DateTimeFormatter.ISO_DATE_TIME;

        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line;
            br.readLine(); // Skip header
            while ((line = br.readLine()) != null) {
                // Tentar ambos os separadores: vírgula e ponto e vírgula
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

                    // Validate SKU exists
                    if (!items.containsKey(sku)) {
                        System.err.printf("❌ ERRO: SKU desconhecido '%s' no wagon %s%n", sku, wagonId);
                        continue;
                    }

                    // Parse dates - lidar com campos vazios
                    LocalDate expiry = null;
                    if (!expRaw.isEmpty() && !expRaw.equals("null")) {
                        try {
                            expiry = LocalDate.parse(expRaw);
                        } catch (Exception e) {
                            System.err.println("Invalid expiry date format: " + expRaw);
                        }
                    }

                    LocalDateTime receivedAt = LocalDateTime.parse(recvRaw, fmt);

                    // Create box
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

    // ============================================================
    // RETURNS
    // ============================================================
    public List<Return> loadReturns(String filePath) throws IOException {
        List<Return> list = new ArrayList<>();
        DateTimeFormatter fmt = DateTimeFormatter.ISO_DATE_TIME;
        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line;
            br.readLine(); // Skip header
            while ((line = br.readLine()) != null) {
                // Tentar ambos os separadores: vírgula e ponto e vírgula
                String[] p = line.contains(";") ? line.split(";") : line.split(",");
                if (p.length < 6) {
                    System.err.println("Invalid return record: " + line);
                    continue;
                }
                try {
                    String id = p[0].trim();
                    // Remover BOM character se existir
                    if (id.startsWith("\uFEFF")) {
                        id = id.substring(1);
                    }

                    String sku = p[1].trim();
                    int qty = Integer.parseInt(p[2].trim());
                    String reason = p[3].trim();
                    LocalDateTime ts = LocalDateTime.parse(p[4].trim(), fmt);

                    // Parse expiry date - lidar com campos vazios
                    LocalDateTime exp = null;
                    String expRaw = p[5].trim();
                    if (!expRaw.isEmpty() && !expRaw.equals("null")) {
                        try {
                            if (expRaw.contains("T")) {
                                exp = LocalDateTime.parse(expRaw, fmt);
                            } else {
                                exp = LocalDate.parse(expRaw).atStartOfDay();
                            }
                        } catch (Exception e) {
                            System.err.println("Invalid expiry date format: " + expRaw);
                        }
                    }
                    list.add(new Return(id, sku, qty, reason, ts, exp));
                } catch (Exception e) {
                    System.err.println("Error parsing return: " + line + " - " + e.getMessage());
                    e.printStackTrace();
                }
            }
        }
        System.out.printf("✅ Loaded %d returns%n", list.size());
        return list;
    }

    // ============================================================
    // ORDERS
    // ============================================================
    public List<Order> loadOrders(String ordersPath, String linesPath) throws IOException {
        Map<String, Order> orders = new LinkedHashMap<>();
        DateTimeFormatter fmt = DateTimeFormatter.ISO_DATE_TIME;

        // Load orders
        try (BufferedReader br = new BufferedReader(new FileReader(ordersPath))) {
            String line;
            br.readLine(); // Skip header
            while ((line = br.readLine()) != null) {
                // Tentar ambos os separadores: vírgula e ponto e vírgula
                String[] p = line.contains(";") ? line.split(";") : line.split(",");
                if (p.length < 3) {
                    System.err.println("Invalid order record: " + line);
                    continue;
                }
                try {
                    String id = p[0].trim();

                    // Convert to LocalDate even if it comes with time
                    LocalDate due = LocalDateTime.parse(p[1].trim(), fmt).toLocalDate();

                    int priority = Integer.parseInt(p[2].trim());
                    orders.put(id, new Order(id, priority, due));
                } catch (Exception e) {
                    System.err.println("Error parsing order: " + line + " - " + e.getMessage());
                }
            }
        }

        // Load order lines
        try (BufferedReader br = new BufferedReader(new FileReader(linesPath))) {
            String line;
            br.readLine(); // Skip header
            while ((line = br.readLine()) != null) {
                // Tentar ambos os separadores: vírgula e ponto e vírgula
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

        // Validate orders have lines
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

    // ============================================================
    // GETTERS & UTILITÁRIOS
    // ============================================================
    public Inventory getInventory() {
        return inventory;
    }

    public Map<String, Item> getItemsMap() {
        return Collections.unmodifiableMap(items);
    }

    public List<Item> getItems() {
        return new ArrayList<>(items.values());
    }
}