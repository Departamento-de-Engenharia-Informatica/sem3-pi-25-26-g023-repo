package pt.ipp.isep.dei.domain;

/**
 * Represents a railway station.
 * <p>
 * Each station has:
 * <ul>
 * <li>A unique station ID.</li>
 * <li>A name.</li>
 * <li>Geographic coordinates (latitude, longitude).</li>
 * <li>A time zone for filtering.</li>
 * </ul>
 * This class is immutable; all fields are final and set via the constructor.
 */
public class Station {

    private final int idEstacao;
    private final String nome;
    private final double latitude;
    private final double longitude;
    private final String timeZone;

    /**
     * Constructs a Station object.
     *
     * @param idEstacao Unique identifier of the station.
     * @param nome      Name of the station.
     */
    public Station(int idEstacao, String nome, double latitude, double longitude, String timeZone) {
        this.idEstacao = idEstacao;
        this.nome = nome;
        this.latitude = latitude;
        this.longitude = longitude;
        this.timeZone = timeZone;
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
     * Returns the latitude of the station.
     *
     * @return Latitude value.
     */
    public double getLatitude() {
        return latitude;
    }

    /**
     * Returns the longitude of the station.
     *
     * @return Longitude value.
     */
    public double getLongitude() {
        return longitude;
    }

    /**
     * Returns the time zone of the station.
     *
     * @return Time zone string.
     */
    public String getTimeZone() {
        return timeZone;
    }


    /**
     * Returns a string representation of the station.
     *
     * @return A string in the format: "ID: {id} - {name} ({lat}, {lon}) [TZ: {tz}]".
     */
    @Override
    public String toString() {
        return String.format("ID: %d - %s (%.4f, %.4f) [TZ: %s]",
                idEstacao, nome, latitude, longitude, timeZone);
    }
}
