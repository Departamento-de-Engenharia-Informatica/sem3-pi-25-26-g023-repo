package pt.ipp.isep.dei.domain;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*; // Import List e Objects

public class Inventory {
    private final List<Box> boxes = new ArrayList<>();

    /** Create a new box from a returned item (Return.expiryDate pode ser LocalDateTime) */
    public Box createBoxFromReturn(Return r) {
        Objects.requireNonNull(r);

        LocalDate expiry = (r.getExpiryDate() != null) ? r.getExpiryDate().toLocalDate() : null;
        LocalDateTime received = LocalDateTime.now();
        String boxId = "RET-" + r.getReturnId();
        // Cria a caixa SEM localização inicial
        return new Box(boxId, r.getSku(), r.getQty(), expiry, received, null, null);
    }

    /** Insert new box into inventory keeping FEFO/FIFO order */
    public void insertBoxFEFO(Box b) {
        Objects.requireNonNull(b);
        boxes.add(b);
        Collections.sort(boxes); // Mantém a lista ordenada
    }

    /**
     * Restock operation (USEI05) - CORRIGIDO
     * Tenta encontrar um local físico (Bay num Warehouse) para a caixa devolvida.
     * @param r O Return a ser restockado.
     * @param warehouses A lista de armazéns onde tentar guardar a caixa.
     * @return true se a caixa foi restockada com sucesso (encontrou local e foi inserida), false caso contrário.
     */
    public boolean restock(Return r, List<Warehouse> warehouses) {
        Box newBox = createBoxFromReturn(r);
        boolean storedPhysically = false;
        String warehouseIdWhereStored = null; // Variável para guardar o ID do armazém

        // Tenta encontrar um local físico no primeiro Warehouse disponível
        for (Warehouse wh : warehouses) {
            if (wh.storeBox(newBox)) { // storeBox define aisle/bay na newBox
                storedPhysically = true;
                warehouseIdWhereStored = wh.getWarehouseId(); // Guarda o ID do armazém que conseguiu guardar
                break; // Sai do loop assim que encontrar um local
            }
        }

        if (storedPhysically) {
            insertBoxFEFO(newBox); // Adiciona ao inventário lógico (já com aisle/bay definidos)
            // Usa a variável warehouseIdWhereStored no printf
            System.out.printf("  ✅ Restock: Item %s (Box %s) colocado em Warehouse %s, Aisle %s, Bay %s.%n",
                    newBox.getSku(),
                    newBox.getBoxId(),
                    warehouseIdWhereStored, // Argumento corrigido
                    newBox.getAisle(),
                    newBox.getBay());
            return true;
        } else {
            System.out.printf("  ⚠️ Restock Falhou: Não foi encontrado espaço para Box %s (SKU %s) nos armazéns.%n",
                    newBox.getBoxId(), newBox.getSku());
            // Opcional: Logar esta falha no AuditLog
            // auditLog.writeLine(...)
            return false;
        }
    }


    /** Unload list of boxes (e.g., from a wagon) into inventory respecting FEFO/FIFO */
    // Este método só deve ser chamado DEPOIS da caixa ter sido colocada fisicamente num Warehouse
    // A lógica principal está agora em WMS.unloadWagons
    // Mantemos insertBoxFEFO para uso interno e pelo restock.
    /*
    public void unloadBoxes(List<Box> newBoxes) {
        if (newBoxes == null || newBoxes.isEmpty()) return;
        for (Box b : newBoxes) {
             // IMPORTANTE: Assumimos que 'b' já tem aisle/bay definidos ANTES de chamar isto
             if (b.getAisle() == null || b.getBay() == null) {
                 System.err.printf("AVISO: Tentando adicionar Box %s ao inventário sem localização definida!%n", b.getBoxId());
             }
            insertBoxFEFO(b);
        }
    }
    */


    /**
     * Dispatch operation - retira qty unidades do sku seguindo FEFO/FIFO.
     * Percorre as boxes por ordem FEFO (a lista está ordenada) e consome até satisfacer ou acabar.
     * Remove boxes que ficam com qtyAvailable == 0.
     *
     * @param sku sku pedido
     * @param qty quantidade a despachar
     * @return quantidade efectivamente despachada
     */
    public int dispatch(String sku, int qty) {
        if (sku == null || qty <= 0) return 0;

        int remaining = qty;
        int dispatched = 0; // Quantidade efetivamente despachada

        Iterator<Box> it = boxes.iterator();
        while (it.hasNext() && remaining > 0) {
            Box b = it.next();
            // Verifica SKU e se a caixa tem localização (importante para evitar dispatch de caixas sem sítio)
            if (!sku.equals(b.getSku()) || b.getAisle() == null || b.getBay() == null ) {
                continue;
            }

            int take = Math.min(b.getQtyAvailable(), remaining);
            if (take > 0) { // Garante que só faz algo se puder tirar quantidade > 0
                b.qtyAvailable -= take;
                remaining -= take;
                dispatched += take; // Acumula o que foi despachado

                if (b.getQtyAvailable() <= 0) {
                    it.remove(); // Remove a caixa se ficou vazia
                }
            }
        }
        // Retorna a quantidade que foi realmente despachada
        return dispatched; // Antes retornava qty - remaining, que é o mesmo, mas assim é mais explícito
    }


    /**
     * Relocate: atualiza aisle/bay de uma box (procura por boxId).
     * Reinsere a box com nova localização mantendo ordenação FEFO/FIFO do inventário.
     * NOTA: Esta função apenas atualiza o inventário LÓGICO. A lógica de mover
     * fisicamente entre Bays nos Warehouses precisaria ser coordenada (ex: no WMS).
     */
    public boolean relocate(String boxId, String newAisle, String newBay) {
        if (boxId == null || newAisle == null || newBay == null) return false;

        Box boxToRelocate = null;
        int index = -1;

        // Encontra a caixa pelo ID
        for (int i = 0; i < boxes.size(); i++) {
            if (boxId.equals(boxes.get(i).getBoxId())) {
                boxToRelocate = boxes.get(i);
                index = i;
                break;
            }
        }

        if (boxToRelocate != null) {
            // Remove a caixa da posição antiga na lista
            boxes.remove(index);

            // Atualiza a localização na caixa encontrada
            boxToRelocate.setAisle(newAisle);
            boxToRelocate.setBay(newBay);

            // Reinsere a caixa na lista (mantendo a ordem FEFO/FIFO)
            // Como a data de expiração/receção não mudou, apenas a localização,
            // a ordem relativa a outras caixas não deve mudar drasticamente,
            // mas é mais seguro re-adicionar e re-ordenar ou usar insertBoxFEFO.
            insertBoxFEFO(boxToRelocate); // Reinsere e garante a ordem

            System.out.printf("  Relocate: Box %s movida para Aisle %s, Bay %s.%n", boxId, newAisle, newBay);
            return true;
        } else {
            System.out.printf("  Relocate Falhou: Box %s não encontrada no inventário.%n", boxId);
            return false;
        }
    }


    public List<Box> getBoxes() {
        // Retorna cópia para evitar modificações externas indesejadas
        return Collections.unmodifiableList(new ArrayList<>(boxes));
    }

}