package cyder.parsers.remote.ip;

import com.google.gson.annotations.SerializedName;

/**
 * An object for parsing returned ip data language objects.
 */
public class Language {
    private String name;

    @SerializedName("native")
    private String native_;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getNative_() {
        return native_;
    }

    public void setNative_(String native_) {
        this.native_ = native_;
    }
}
