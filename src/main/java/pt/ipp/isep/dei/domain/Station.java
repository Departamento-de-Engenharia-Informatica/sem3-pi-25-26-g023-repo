package pt.ipp.isep.dei.domain;

/**
 * Represents a railway station.
 * <p>
 * Each station has:
 * <ul>
 *     <li>A unique station ID.</li>
 *     <li>A name.</li>
 * </ul>
 * This class is immutable; all fields are final and set via the constructor.
 */
public class Station {

    private final int idEstacao;
    private final String nome;

    /**
     * Constructs a Station object.
     *
     * @param idEstacao Unique identifier of the station.
     * @param nome      Name of the station.
     */
    public Station(int idEstacao, String nome) {
        this.idEstacao = idEstacao;
        this.nome = nome;
    }

    /**
     * Returns the unique ID of the station.
     *
     * @return Station ID.
     */
    public int getIdEstacao() {
        return idEstacao;
    }

    /**
     * Returns the name of the station.
     *
     * @return Station name.
     */
    public String getNome() {
        return nome;
    }

    /**
     * Returns a string representation of the station.
     *
     * @return A string in the format: "ID: {id} - {name}".
     */
    @Override
    public String toString() {
        return String.format("ID: %d - %s", idEstacao, nome);
    }
}
