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

    /** Lê items.csv e carrega produtos válidos */
    public void loadItems(String filePath) throws IOException {
        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line;
            br.readLine(); // ignora cabeçalho
            while ((line = br.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length < 5) continue;

                String sku = parts[0].trim();
                String name = parts[1].trim();
                String category = parts[2].trim();
                String unit = parts[3].trim();
                double unitWeight = Double.parseDouble(parts[4].trim());

                items.put(sku, new Item(sku, name, category, unit, unitWeight));
            }
        }
    }

    /** Lê wagons.csv e converte em objetos Wagon + Box */
    public List<Wagon> loadWagons(String filePath) throws IOException {
        Map<String, Wagon> wagons = new LinkedHashMap<>();
        DateTimeFormatter formatter = DateTimeFormatter.ISO_DATE_TIME;

        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line;
            br.readLine();
            while ((line = br.readLine()) != null) {
                String[] p = line.split(",");
                if (p.length < 6) continue;

                String wagonId = p[0].trim();
                String boxId = p[1].trim();
                String sku = p[2].trim();
                int qty = Integer.parseInt(p[3].trim());
                String expiryRaw = p[4].trim();
                String receivedRaw = p[5].trim();

                if (!items.containsKey(sku)) {
                    System.err.printf("ERRO: SKU desconhecido '%s' no wagon %s%n", sku, wagonId);
                    continue;
                }
                if (qty <= 0) {
                    System.err.printf("ERRO: Quantidade inválida %d no box %s%n", qty, boxId);
                    continue;
                }

                LocalDate expiry = expiryRaw.isEmpty() ? null : LocalDate.parse(expiryRaw);
                LocalDateTime receivedAt;
                try {
                    receivedAt = LocalDateTime.parse(receivedRaw, formatter);
                } catch (Exception e) {
                    System.err.printf("ERRO: Data receivedAt inválida em %s%n", boxId);
                    continue;
                }

                Box box = new Box(boxId, sku, qty, expiry, receivedAt, null, null);
                wagons.computeIfAbsent(wagonId, Wagon::new).addBox(box);
            }
        }
        return new ArrayList<>(wagons.values());
    }

    public Inventory getInventory() {
        return inventory;
    }

    public void unloadAll(List<Wagon> wagons) {
        WMS wms = new WMS(new Quarantine(), inventory, new AuditLog("audit.log"));
        wms.unloadWagons(wagons);
    }
}
