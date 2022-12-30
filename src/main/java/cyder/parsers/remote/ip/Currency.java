package cyder.parsers.remote.ip;

import com.google.gson.annotations.SerializedName;
import cyder.logging.LogTag;
import cyder.logging.Logger;

import java.util.Objects;

/**
 * An object for parsing returned ip data currency objects.
 */
public class Currency {
    /**
     * The name of the currency.
     */
    private String name;

    /**
     * The currency code, such as USD.
     */
    private String code;

    /**
     * The currency symbol.
     */
    private String symbol;

    /**
     * The native currency symbol.
     */
    @SerializedName("native")
    private String _native;

    /**
     * The plural name of the currency.
     */
    private String plural;

    /**
     * Constructs a new currency object.
     */
    public Currency() {
        Logger.log(LogTag.OBJECT_CREATION, this);
    }

    /**
     * Sets the name of the currency.
     *
     * @param name the name of the currency
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Sets the currency code, such as USD.
     *
     * @param code the currency code, such as USD
     */
    public void setCode(String code) {
        this.code = code;
    }

    /**
     * Sets the currency symbol.
     *
     * @param symbol the currency symbol
     */
    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }

    /**
     * Sets the native currency symbol.
     *
     * @param _native the native currency symbol
     */
    public void set_native(String _native) {
        this._native = _native;
    }

    /**
     * Sets the plural name of the currency.
     *
     * @param plural the plural name of the currency
     */
    public void setPlural(String plural) {
        this.plural = plural;
    }

    /**
     * Returns the name of the currency.
     *
     * @return the name of the currency
     */
    public String getName() {
        return name;
    }

    /**
     * Returns the currency code, such as USD.
     *
     * @return the currency code, such as USD
     */
    public String getCode() {
        return code;
    }

    /**
     * Returns the currency symbol.
     *
     * @return the currency symbol
     */
    public String getSymbol() {
        return symbol;
    }

    /**
     * Returns the native currency symbol.
     *
     * @return the native currency symbol
     */
    public String get_native() {
        return _native;
    }

    /**
     * Returns the plural name of the currency.
     *
     * @return the plural name of the currency
     */
    public String getPlural() {
        return plural;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        } else if (!(o instanceof Currency)) {
            return false;
        }

        Currency other = (Currency) o;

        return Objects.equals(name, other.name)
                && Objects.equals(code, other.code)
                && Objects.equals(symbol, other.symbol)
                && Objects.equals(_native, other._native)
                && Objects.equals(plural, other.plural);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        int ret = 0;

        if (name != null) ret = 31 * ret + name.hashCode();
        if (code != null) ret = 31 * ret + code.hashCode();
        if (symbol != null) ret = 31 * ret + symbol.hashCode();
        if (_native != null) ret = 31 * ret + _native.hashCode();
        if (plural != null) ret = 31 * ret + plural.hashCode();

        return ret;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return "Currency{"
                + "name=\"" + name + "\", "
                + "code=\"" + code + "\", "
                + "symbol=\"" + symbol + "\", "
                + "_native=\"" + _native + "\", "
                + "plural=\"" + plural + "\""
                + "}";
    }
}
