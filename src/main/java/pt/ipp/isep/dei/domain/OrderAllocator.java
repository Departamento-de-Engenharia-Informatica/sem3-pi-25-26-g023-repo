package pt.ipp.isep.dei.domain;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Responsável por alocar inventário às encomendas usando FEFO/FIFO
 */


public class OrderAllocator {

    public enum Mode { STRICT, PARTIAL }

    private Map<String, Item> items;

    public OrderAllocator() {
        this.items = new HashMap<>();
    }

    /**
     * Define os itens disponíveis para cálculo de pesos
     */
    public void setItems(Map<String, Item> items) {
        this.items = items != null ? items : new HashMap<>();
    }

    /**
     * Aloca stock das boxes às encomendas conforme o modo selecionado
     */
    public AllocationResult allocateOrders(List<Order> orders, List<Box> inventory, Mode mode) {
        AllocationResult result = new AllocationResult();

        // Criar CÓPIAS MUTÁVEIS para evitar UnsupportedOperationException em List.of(...)
        List<Order> ordersToProcess = new ArrayList<>(orders == null ? Collections.emptyList() : orders);
        List<Box> stock = new ArrayList<>(inventory == null ? Collections.emptyList() : inventory);

        if (ordersToProcess.isEmpty()) {
            System.out.println("⚠️  Não há orders para processar");
            return result; // Teste 01 espera listas vazias
        }

        // Não sair imediatamente: se inventário estiver vazio, gerar eligibilities UNDISPATCHABLE
        if (stock.isEmpty()) {
            System.out.println("⚠️  O inventário está vazio");
            for (Order o : ordersToProcess) {
                List<OrderLine> linesSorted = o.lines.stream()
                        .sorted(Comparator.comparingInt(l -> l.lineNo))
                        .collect(Collectors.toList());
                for (OrderLine l : linesSorted) {
                    Eligibility e = new Eligibility(
                            o.orderId, l.lineNo, l.sku, l.requestedQty, 0, Status.UNDISPATCHABLE
                    );
                    result.eligibilityList.add(e);
                }
            }
            return result; // Teste 02: agora preenche eligibilityList
        }

        System.out.printf("📦 Processando %d orders com %d boxes no inventário...%n",
                ordersToProcess.size(), stock.size());

        // Ordenar encomendas: prioridade (1 melhor), depois dueDate e id
        ordersToProcess.sort(Comparator
                .comparingInt((Order o) -> o.priority)
                .thenComparing(o -> o.dueDate)
                .thenComparing(o -> o.orderId));

        // Agrupar inventário por SKU
        Map<String, List<Box>> inventoryBySku = stock.stream()
                .collect(Collectors.groupingBy(Box::getSku));

        int totalLinesProcessed = 0;
        int totalAllocations = 0;

        for (Order order : ordersToProcess) {

            // Processar linhas por lineNo ascendente (não mutar a lista original de linhas)
            List<OrderLine> linesSorted = order.lines.stream()
                    .sorted(Comparator.comparingInt(l -> l.lineNo))
                    .collect(Collectors.toList());

            for (OrderLine line : linesSorted) {
                totalLinesProcessed++;
                int remaining = line.requestedQty;
                int allocated = 0;
                List<Allocation> lineAllocations = new ArrayList<>();

                List<Box> boxesForSku = inventoryBySku.getOrDefault(line.sku, Collections.emptyList());

                if (boxesForSku.isEmpty()) {
                    System.out.printf("  ❌ SKU %s não encontrado no inventário para order %s%n",
                            line.sku, order.orderId);
                }

                // FEFO/FIFO: caixas COM validade primeiro (por expiry asc). Sem validade → FIFO por received asc.
                List<Box> sortedBoxes = boxesForSku.stream()
                        .filter(b -> b.getQtyAvailable() > 0)
                        .sorted(Comparator
                                .comparing((Box b) -> b.getExpiryDate() == null)
                                .thenComparing(b -> {
                                    if (b.getExpiryDate() != null)
                                        return b.getExpiryDate().atStartOfDay();
                                    else
                                        return b.getReceivedDate();
                                })
                        ).toList();

                // Alocação de quantidades
                for (Box box : sortedBoxes) {
                    if (remaining <= 0) break;
                    if (box.getQtyAvailable() <= 0) continue;

                    int take = Math.min(remaining, box.getQtyAvailable());
                    if (take <= 0) continue;

                    double allocationWeight = getItemWeight(line.sku) * take;

                    Allocation allocation = new Allocation(
                            order.orderId,
                            line.lineNo,
                            line.sku,
                            take,
                            allocationWeight,
                            box.getBoxId(),
                            box.getAisle(),
                            box.getBay()
                    );
                    lineAllocations.add(allocation);

                    // Consome quantidade da box (campo é mutável no teu modelo)
                    box.qtyAvailable -= take;
                    remaining -= take;
                    allocated += take;
                    totalAllocations++;

                    System.out.printf("  ✅ Alocado: Order %s Line %d - %d unidades de %s (Box %s)%n",
                            order.orderId, line.lineNo, take, line.sku, box.getBoxId());
                }

                // Determinar status conforme o modo
                Status status;
                if (mode == Mode.STRICT) {
                    if (allocated == line.requestedQty) {
                        status = Status.ELIGIBLE;
                        System.out.printf("  🟢 ELIGIBLE: Order %s Line %d - %d/%d unidades%n",
                                order.orderId, line.lineNo, allocated, line.requestedQty);
                    } else {
                        status = Status.UNDISPATCHABLE;
                        System.out.printf("  🔴 UNDISPATCHABLE: Order %s Line %d - %d/%d unidades%n",
                                order.orderId, line.lineNo, allocated, line.requestedQty);
                        // Repor stock (reversão) e descartar alocações parciais em STRICT
                        for (Allocation a : lineAllocations) {
                            inventoryBySku.getOrDefault(line.sku, Collections.emptyList()).stream()
                                    .filter(b -> b.getBoxId().equals(a.boxId))
                                    .findFirst()
                                    .ifPresent(b -> b.qtyAvailable += a.qty);
                        }
                        lineAllocations.clear();
                        allocated = 0;
                    }
                } else { // Mode.PARTIAL
                    if (allocated == 0) {
                        status = Status.UNDISPATCHABLE;
                    } else if (allocated < line.requestedQty) {
                        status = Status.PARTIAL;
                    } else {
                        status = Status.ELIGIBLE;
                    }
                    System.out.printf("  %s: Order %s Line %d - %d/%d unidades%n",
                            status, order.orderId, line.lineNo, allocated, line.requestedQty);
                }

                // Registos de resultado
                Eligibility eligibility = new Eligibility(
                        order.orderId,
                        line.lineNo,
                        line.sku,
                        line.requestedQty,
                        allocated,
                        status
                );
                result.eligibilityList.add(eligibility);
                result.allocations.addAll(lineAllocations);
            }
        }

        System.out.printf("📊 Concluído: %d linhas processadas, %d alocações geradas%n",
                totalLinesProcessed, totalAllocations);

        // Estatísticas finais
        Map<Status, Long> statusCount = result.eligibilityList.stream()
                .collect(Collectors.groupingBy(e -> e.status, Collectors.counting()));

        System.out.println("📈 Estatísticas Finais:");
        statusCount.forEach((status, count) ->
                System.out.printf("  %s: %d linhas%n", status, count));

        return result;
    }

    /**
     * Obtém o peso unitário de um item
     */
    private double getItemWeight(String sku) {
        if (items == null || items.isEmpty()) {
            System.out.printf("⚠️  Mapa de items vazio para SKU %s%n", sku);
            return 1.0;
        }

        Item item = items.get(sku);
        if (item == null) {
            System.out.printf("⚠️  SKU %s não encontrado%n", sku);
            return 1.0;
        }

        return item.getUnitWeight();
    }

    /**
     * Mostra informações do inventário para debug
     */
    public void printInventoryInfo(List<Box> inventory) {
        if (inventory == null || inventory.isEmpty()) {
            System.out.println("📭 Inventário vazio");
            return;
        }

        System.out.println("📦 Informação do Inventário:");
        Map<String, List<Box>> bySku = inventory.stream()
                .collect(Collectors.groupingBy(Box::getSku));

        bySku.forEach((sku, boxes) -> {
            int totalQty = boxes.stream().mapToInt(Box::getQtyAvailable).sum();
            int boxCount = boxes.size();
            System.out.printf("  %s: %d boxes, %d unidades totais%n", sku, boxCount, totalQty);
        });
    }
}
