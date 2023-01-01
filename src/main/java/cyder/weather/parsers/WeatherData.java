package cyder.weather.parsers;

import cyder.logging.LogTag;
import cyder.logging.Logger;

import java.util.ArrayList;

/**
 * The master object for parsing OpenWeatherMap API weather data.
 */
public class WeatherData {
    /**
     * The coord object.
     */
    private Coord coord;

    /**
     * The list of weather objects.
     */
    private ArrayList<WeatherListObject> weather;

    /**
     * The origin of the weather data.
     */
    private String base;

    /**
     * The main object.
     */
    private Main main;

    /**
     * The visibility.
     */
    private int visibility;

    /**
     * The wind object.
     */
    private Wind wind;

    /**
     * The clouds object.
     */
    private Clouds clouds;

    /**
     * The time of calculation.
     */
    private int dt;

    /**
     * The sys object.
     */
    private Sys sys;

    /**
     * The UTC shift for this timezone.
     */
    private int timezone;

    /**
     * The city id.
     */
    private int id;

    /**
     * The city name.
     */
    private String name;

    /**
     * An internal parameter.
     */
    private double cod;

    /**
     * Constructs a new weather data object.
     */
    public WeatherData() {
        Logger.log(LogTag.OBJECT_CREATION, this);
    }

    /**
     * Returns the coords object.
     *
     * @return the coords object
     */
    public Coord getCoord() {
        return coord;
    }

    /**
     * Sets the coords object.
     *
     * @param coord the coords object
     */
    public void setCoord(Coord coord) {
        this.coord = coord;
    }

    /**
     * Returns the list of weather objects.
     *
     * @return the list of weather objects
     */
    public ArrayList<WeatherListObject> getWeather() {
        return weather;
    }

    /**
     * Sets the list of weather objects.
     *
     * @param weather the list of weather objects
     */
    public void setWeather(ArrayList<WeatherListObject> weather) {
        this.weather = weather;
    }

    /**
     * Returns the origin of the weather data.
     *
     * @return the origin of the weather data
     */
    public String getBase() {
        return base;
    }

    /**
     * Sets the origin of the weather data.
     *
     * @param base the origin of the weather data
     */
    public void setBase(String base) {
        this.base = base;
    }

    /**
     * Returns the main object.
     *
     * @return the main object
     */
    public Main getMain() {
        return main;
    }

    /**
     * Sets the main object.
     *
     * @param main the main object
     */
    public void setMain(Main main) {
        this.main = main;
    }

    /**
     * Returns the visibility.
     *
     * @return the visibility
     */
    public int getVisibility() {
        return visibility;
    }

    /**
     * Sets the visibility.
     *
     * @param visibility the visibility
     */
    public void setVisibility(int visibility) {
        this.visibility = visibility;
    }

    /**
     * Return the wind object.
     *
     * @return the wind object
     */
    public Wind getWind() {
        return wind;
    }

    /**
     * Sets the wind object.
     *
     * @param wind the wind object
     */
    public void setWind(Wind wind) {
        this.wind = wind;
    }

    /**
     * Returns the clouds object.
     *
     * @return the clouds object
     */
    public Clouds getClouds() {
        return clouds;
    }

    /**
     * Sets the clouds object.
     *
     * @param clouds the clouds object
     */
    public void setClouds(Clouds clouds) {
        this.clouds = clouds;
    }

    /**
     * Returns the time of calculation.
     *
     * @return the time of calculation
     */
    public int getDt() {
        return dt;
    }

    /**
     * Sets time of calculation.
     *
     * @param dt the time of calculation
     */
    public void setDt(int dt) {
        this.dt = dt;
    }

    /**
     * Returns the sys object.
     *
     * @return the sys object
     */
    public Sys getSys() {
        return sys;
    }

    /**
     * Sets the sys object.
     *
     * @param sys the sys object
     */
    public void setSys(Sys sys) {
        this.sys = sys;
    }

    /**
     * Returns the UTC shift for this timezone.
     *
     * @return the UTC shift for this timezone
     */
    public int getTimezone() {
        return timezone;
    }

    /**
     * Sets the UTC shift for this timezone.
     *
     * @param timezone the UTC shift for this timezone
     */
    public void setTimezone(int timezone) {
        this.timezone = timezone;
    }

    /**
     * Returns the city id.
     *
     * @return the city id
     */
    public int getId() {
        return id;
    }

    /**
     * Sets the city id.
     *
     * @param id the city id
     */
    public void setId(int id) {
        this.id = id;
    }

    /**
     * Returns the city name.
     *
     * @return the city name.
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the city name.
     *
     * @param name the city name
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Returns the cod internal parameter.
     *
     * @return the cod internal parameter
     */
    public double getCod() {
        return cod;
    }

    /**
     * Sets the cod internal parameter.
     *
     * @param cod the cod internal parameter
     */
    public void setCod(double cod) {
        this.cod = cod;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        } else if (!(o instanceof WeatherData)) {
            return false;
        }

        WeatherData other = (WeatherData) o;
        return coord.equals(other.coord)
                && weather.equals(other.weather)
                && base.equals(other.base)
                && main.equals(other.main)
                && visibility == other.visibility
                && wind.equals(other.wind)
                && clouds.equals(other.clouds)
                && dt == other.dt
                && sys.equals(other.sys)
                && timezone == other.timezone
                && id == other.id
                && name.equals(other.name)
                && cod == other.cod;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        int ret = coord.hashCode();
        ret = 31 * ret + weather.hashCode();
        ret = 31 * ret + base.hashCode();
        ret = 31 * ret + main.hashCode();
        ret = 31 * ret + Integer.hashCode(visibility);
        ret = 31 * ret + wind.hashCode();
        ret = 31 * ret + clouds.hashCode();
        ret = 31 * ret + Integer.hashCode(dt);
        ret = 31 * ret + sys.hashCode();
        ret = 31 * ret + Integer.hashCode(timezone);
        ret = 31 * ret + Integer.hashCode(id);
        ret = 31 * ret + name.hashCode();
        ret = 31 * ret + Double.hashCode(cod);
        return ret;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return "WeatherData{"
                + "coord=" + coord
                + ", weather=" + weather
                + ", base=\"" + base + "\""
                + ", main=" + main
                + ", visibility=" + visibility
                + ", wind=" + wind
                + ", clouds=" + clouds
                + ", dt=" + dt
                + ", sys=" + sys
                + ", timezone=" + timezone
                + ", id=" + id
                + ", name=\"" + name + "\""
                + ", cod=" + cod
                + "}";
    }
}
