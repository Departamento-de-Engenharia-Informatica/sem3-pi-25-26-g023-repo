package pt.ipp.isep.dei.domain;

/**
 * Represents a locomotive in the railway system.
 * <p>
 * Each locomotive has:
 * <ul>
 *     <li>A unique ID.</li>
 *     <li>A model name.</li>
 *     <li>A type, either "diesel" or "electric".</li>
 * </ul>
 * This class is immutable; all fields are final and can only be set via the constructor.
 */
public class Locomotive {

    private final int idLocomotiva;
    private final String modelo;
    private final String tipo; // "diesel" or "electric"

    /**
     * Constructs a Locomotive object.
     *
     * @param idLocomotiva Unique identifier of the locomotive.
     * @param modelo       Model name of the locomotive.
     * @param tipo         Type of the locomotive ("diesel" or "eletrica").
     */
    public Locomotive(int idLocomotiva, String modelo, String tipo) {
        this.idLocomotiva = idLocomotiva;
        this.modelo = modelo;
        this.tipo = tipo;
    }

    /**
     * Returns the unique ID of the locomotive.
     *
     * @return Locomotive ID.
     */
    public int getIdLocomotiva() {
        return idLocomotiva;
    }

    /**
     * Returns the model name of the locomotive.
     *
     * @return Model name.
     */
    public String getModelo() {
        return modelo;
    }

    /**
     * Returns the type of the locomotive ("diesel" or "electric").
     *
     * @return Type as a string.
     */
    public String getTipo() {
        return tipo;
    }

    /**
     * Returns a string representation of the locomotive.
     *
     * @return A string in the format: "ID: {id} - Model {model} ({type})".
     */
    @Override
    public String toString() {
        return String.format("ID: %d - Modelo %s (%s)", idLocomotiva, modelo, tipo);
    }
}
