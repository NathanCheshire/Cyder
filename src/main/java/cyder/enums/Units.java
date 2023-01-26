package cyder.enums;

/**
 * Measurement units and their abbreviations.
 */
public enum Units {
    /*
    Speed units.
     */
    MILES_PER_HOUR("mph"),
    KILOMETERS_PER_HOUR("kmh"),

    /*
     Pressure units
     */
    ATMOSPHERES("atm"),

    /*
    Degree units
     */
    DEGREES("deg"),
    RADIANS("rad");

    /**
     * The abbreviation for this unit.
     */
    private final String abbreviation;

    Units(String abbreviation) {
        this.abbreviation = abbreviation;
    }

    /**
     * Returns the abbreviation for this unit.
     *
     * @return the abbreviation for this unit
     */
    public String getAbbreviation() {
        return abbreviation;
    }
}
