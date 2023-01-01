package cyder.weather.parsers;

/**
 * An object contained in the weather list.
 */
public class WeatherListObject {
    /**
     * The weather condition id.
     */
    private int id;

    /**
     * The group of weather parameter. E.g. rain, snow, extreme.
     */
    private String main;

    /**
     * The weather condition within the group.
     */
    private String description;

    /**
     * The weather icon id.
     */
    private String icon;

    /**
     * Returns the weather condition id.
     *
     * @return the weather condition id
     */
    public int getId() {
        return id;
    }

    /**
     * Sets the weather condition id.
     *
     * @param id the weather condition id
     */
    public void setId(int id) {
        this.id = id;
    }

    /**
     * Returns the group of weather parameter. E.g. rain, snow, extreme.
     *
     * @return the group of weather parameter. E.g. rain, snow, extreme
     */
    public String getMain() {
        return main;
    }

    /**
     * Sets the group of weather parameter. E.g. rain, snow, extreme.
     *
     * @param main the group of weather parameter. E.g. rain, snow, extreme
     */
    public void setMain(String main) {
        this.main = main;
    }

    /**
     * Returns the weather condition within the group.
     *
     * @return the weather condition within the group
     */
    public String getDescription() {
        return description;
    }

    /**
     * Sets the weather condition within the group.
     *
     * @param description the weather condition within the group
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * Returns the weather icon id.
     *
     * @return the weather icon id
     */
    public String getIcon() {
        return icon;
    }

    /**
     * Sets the weather icon id.
     *
     * @param icon the weather icon id
     */
    public void setIcon(String icon) {
        this.icon = icon;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        } else if (!(o instanceof WeatherListObject)) {
            return false;
        }

        WeatherListObject other = (WeatherListObject) o;
        return other.id == id
                && other.main.equals(main)
                && other.description.equals(description)
                && other.icon.equals(icon);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        int ret = Integer.hashCode(id);
        ret = 31 * ret + main.hashCode();
        ret = 31 * ret + description.hashCode();
        ret = 31 * ret + icon.hashCode();
        return ret;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return "WeatherListObject{"
                + "id=" + id
                + ", main=\"" + main + "\""
                + ", description=\"" + description + "\""
                + ", icon=\"" + icon + "\""
                + "}";
    }
}
