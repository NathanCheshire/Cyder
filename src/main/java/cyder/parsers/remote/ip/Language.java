package cyder.parsers.remote.ip;

import com.google.gson.annotations.SerializedName;
import cyder.logging.LogTag;
import cyder.logging.Logger;

import java.util.Objects;

/**
 * An object for parsing returned ip data language objects.
 */
public class Language {
    /**
     * The name of the language
     */
    private String name;

    /**
     * The native name of the language.
     */
    @SerializedName("native")
    private String native_;

    /**
     * The language code.
     */
    private String code;

    /**
     * Constructs a new language object.
     */
    public Language() {
        Logger.log(LogTag.OBJECT_CREATION, this);
    }

    /**
     * Returns the name of this language.
     *
     * @return the name of this language
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the name of this language.
     *
     * @param name the name of this language
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Returns the native name of this language.
     *
     * @return the native name of this language
     */
    public String getNative_() {
        return native_;
    }

    /**
     * Sets the native name of this language.
     *
     * @param native_ the native name of this language
     */
    public void setNative_(String native_) {
        this.native_ = native_;
    }

    /**
     * Returns the language code.
     *
     * @return the language code
     */
    public String getCode() {
        return code;
    }

    /**
     * Sets the language code.
     *
     * @param code the language code
     */
    public void setCode(String code) {
        this.code = code;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        } else if (!(o instanceof Language)) {
            return false;
        }

        Language other = (Language) o;

        return Objects.equals(name, other.name)
                && Objects.equals(native_, other.native_)
                && Objects.equals(code, other.code);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        int ret = 0;

        if (name != null) ret = 31 * ret + name.hashCode();
        if (native_ != null) ret = 31 * ret + native_.hashCode();
        if (code != null) ret = 31 * ret + code.hashCode();

        return ret;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return "Language{"
                + "name=\"" + name + "\", "
                + "native=\"" + native_ + "\", "
                + "code=\"" + code + "\""
                + "}";
    }
}
