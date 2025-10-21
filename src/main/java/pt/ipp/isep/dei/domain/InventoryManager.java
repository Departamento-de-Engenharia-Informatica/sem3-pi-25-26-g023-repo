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
    private final Map<String, Item> items = new HashMap<>(); // SKU -> Item
    private final List<Warehouse> warehouses = new ArrayList<>();

    // ============================================================
    // ITEMS
    // ============================================================
    public void loadItems(String filePath) throws IOException {
        int count = 0;
        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line;
            br.readLine(); // cabeçalho
            while ((line = br.readLine()) != null) {
                // aceitar vírgula OU ponto e vírgula
                String[] p = line.split("[,;]");
                if (p.length < 5) continue;
                String sku = p[0].trim();
                String name = p[1].trim();
                String category = p[2].trim();
                String unit = p[3].trim();
                double weight = Double.parseDouble(p[4].trim());
                items.put(sku, new Item(sku, name, category, unit, weight));
                count++;
            }
        }
        System.out.printf("✅ Loaded %d items.%n", count);
    }

    // ============================================================
    // BAYS & WAREHOUSES
    // ============================================================
    public List<Bay> loadBays(String filePath) throws IOException {
        List<Bay> allBays = new ArrayList<>();
        int countBays = 0;

        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line;
            br.readLine(); // cabeçalho
            while ((line = br.readLine()) != null) {
                // aceitar vírgula OU ponto e vírgula
                String[] p = line.split("[,;]");
                if (p.length < 4) continue;

                String warehouseId = p[0].trim();
                int aisle = Integer.parseInt(p[1].trim());
                int bay = Integer.parseInt(p[2].trim());
                int capacity = Integer.parseInt(p[3].trim());

                Bay newBay = new Bay(warehouseId, aisle, bay, capacity);
                allBays.add(newBay);
                countBays++;

                // adiciona a bay ao warehouse correspondente
                Warehouse wh = warehouses.stream()
                        .filter(w -> w.getWarehouseId().equals(warehouseId))
                        .findFirst()
                        .orElseGet(() -> {
                            Warehouse w = new Warehouse(warehouseId);
                            warehouses.add(w);
                            return w;
                        });
                wh.addBay(newBay);
            }
        }

        warehouses.sort(Comparator.comparing(Warehouse::getWarehouseId));
        System.out.printf("✅ Loaded %d bays across %d warehouse(s).%n", countBays, warehouses.size());
        return allBays;
    }

    public List<Warehouse> getWarehouses() {
        return warehouses;
    }

    // ============================================================
    // WAGONS
    // ============================================================
    public List<Wagon> loadWagons(String filePath) throws IOException {
        Map<String, Wagon> wagons = new LinkedHashMap<>();
        DateTimeFormatter fmt = DateTimeFormatter.ISO_DATE_TIME;
        int countBoxes = 0;

        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line;
            br.readLine();
            while ((line = br.readLine()) != null) {
                String[] p = line.split("[,;]");
                if (p.length < 6) continue;

                String wagonId = p[0].trim();
                String boxId = p[1].trim();
                String sku = p[2].trim();
                int qty = Integer.parseInt(p[3].trim());
                String expRaw = p[4].trim();
                String recvRaw = p[5].trim();

                if (!items.containsKey(sku)) {
                    System.err.printf("⚠️ ERRO: SKU desconhecido '%s' no wagon %s%n", sku, wagonId);
                    continue;
                }

                LocalDate expiry = expRaw.isEmpty() ? null : LocalDate.parse(expRaw);
                LocalDateTime receivedAt = LocalDateTime.parse(recvRaw, fmt);
                Box box = new Box(boxId, sku, qty, expiry, receivedAt, null, null);
                wagons.computeIfAbsent(wagonId, Wagon::new).addBox(box);
                countBoxes++;
            }
        }
        System.out.printf("✅ Loaded %d wagons containing %d boxes.%n", wagons.size(), countBoxes);
        return new ArrayList<>(wagons.values());
    }

    // ============================================================
    // RETURNS
    // ============================================================
    public List<Return> loadReturns(String filePath) throws IOException {
        List<Return> list = new ArrayList<>();
        DateTimeFormatter fmt = DateTimeFormatter.ISO_DATE_TIME;
        int count = 0;

        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line;
            br.readLine();
            while ((line = br.readLine()) != null) {
                String[] p = line.split("[,;]");
                if (p.length < 6) continue;
                String id = p[0].trim();
                String sku = p[1].trim();
                int qty = Integer.parseInt(p[2].trim());
                String reason = p[3].trim();
                LocalDateTime ts = LocalDateTime.parse(p[4].trim(), fmt);
                LocalDateTime exp = p[5].trim().isEmpty() ? null :
                        (p[5].contains("T") ? LocalDateTime.parse(p[5].trim(), fmt)
                                : LocalDate.parse(p[5].trim()).atStartOfDay());
                list.add(new Return(id, sku, qty, reason, ts, exp));
                count++;
            }
        }
        System.out.printf("✅ Loaded %d returns.%n", count);
        return list;
    }

    // ============================================================
    // ORDERS
    // ============================================================
    public List<Order> loadOrders(String ordersPath, String linesPath) throws IOException {
        Map<String, Order> orders = new LinkedHashMap<>();
        DateTimeFormatter fmt = DateTimeFormatter.ISO_DATE_TIME;

        try (BufferedReader br = new BufferedReader(new FileReader(ordersPath))) {
            String line;
            br.readLine();
            while ((line = br.readLine()) != null) {
                String[] p = line.split("[,;]");
                if (p.length < 3) continue;
                String id = p[0].trim();

                // Converte para LocalDate mesmo que venha com hora
                LocalDate due = LocalDateTime.parse(p[1].trim(), fmt).toLocalDate();

                int priority = Integer.parseInt(p[2].trim());
                orders.put(id, new Order(id, priority, due));
            }
        }

        try (BufferedReader br = new BufferedReader(new FileReader(linesPath))) {
            String line;
            br.readLine();
            while ((line = br.readLine()) != null) {
                String[] p = line.split("[,;]");
                if (p.length < 4) continue;
                String orderId = p[0].trim();
                int lineNo = Integer.parseInt(p[1].trim());
                String sku = p[2].trim();
                int qty = Integer.parseInt(p[3].trim());
                if (orders.containsKey(orderId)) {
                    orders.get(orderId).lines.add(new OrderLine(lineNo, sku, qty));
                }
            }
        }

        System.out.printf("✅ Loaded %d orders.%n", orders.size());
        return new ArrayList<>(orders.values());
    }

    // ============================================================
    // GETTERS & UTILITÁRIOS
    // ============================================================
    public Inventory getInventory() {
        return inventory;
    }

    public void unloadAll(List<Wagon> wagons) {
        WMS wms = new WMS(new Quarantine(), inventory, new AuditLog("audit.log"), warehouses);
        wms.unloadWagons(wagons);
    }
}
