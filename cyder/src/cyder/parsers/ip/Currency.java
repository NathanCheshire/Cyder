package cyder.parsers.ip;

import com.google.gson.annotations.SerializedName;

/**
 * An object for parsing returned ip data currency objects.
 */
public class Currency {
    private String name;
    private String code;
    private String symbol;

    @SerializedName("native")
    private String _native;
    private String plural;

    public void setName(String name) {
        this.name = name;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }

    public void set_native(String _native) {
        this._native = _native;
    }

    public void setPlural(String plural) {
        this.plural = plural;
    }

    public String getName() {
        return name;
    }

    public String getCode() {
        return code;
    }

    public String getSymbol() {
        return symbol;
    }

    public String get_native() {
        return _native;
    }

    public String getPlural() {
        return plural;
    }
}
