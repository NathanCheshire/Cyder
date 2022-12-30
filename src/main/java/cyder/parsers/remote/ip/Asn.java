package cyder.parsers.remote.ip;

import cyder.logging.LogTag;
import cyder.logging.Logger;

import java.util.Objects;

/**
 * An object for parsing returned {@link IpData} ASN (Autonomous System Number) objects.
 */
public class Asn {
    /**
     * The asn.
     */
    private String asn;

    /**
     * The asn name.
     */
    private String name;

    /**
     * The asn domain.
     */
    private String domain;

    /**
     * THe asn route.
     */
    private String route;

    /**
     * The asn type.
     */
    private String type;

    /**
     * Creates a new Asn object.
     */
    public Asn() {
        Logger.log(LogTag.OBJECT_CREATION, this);
    }

    /**
     * Sets the asn.
     *
     * @param asn the asn
     */
    public void setAsn(String asn) {
        this.asn = asn;
    }

    /**
     * Sets the asn name.
     *
     * @param name the asn name
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Sets the asn domain.
     *
     * @param domain the asn domain
     */
    public void setDomain(String domain) {
        this.domain = domain;
    }

    /**
     * Sets the asn route.
     *
     * @param route the asn route
     */
    public void setRoute(String route) {
        this.route = route;
    }

    /**
     * Sets the asn type.
     *
     * @param type the asn type
     */
    public void setType(String type) {
        this.type = type;
    }

    /**
     * Returns the asn.
     *
     * @return the asn
     */
    public String getAsn() {
        return asn;
    }

    /**
     * Returns the asn name.
     *
     * @return the asn name
     */
    public String getName() {
        return name;
    }

    /**
     * Returns the asn domain.
     *
     * @return the asn domain
     */
    public String getDomain() {
        return domain;
    }

    /**
     * Returns the asn route.
     *
     * @return the asn route
     */
    public String getRoute() {
        return route;
    }

    /**
     * Returns the asn type.
     *
     * @return the asn type
     */
    public String getType() {
        return type;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        } else if (!(o instanceof Asn)) {
            return false;
        }

        Asn other = (Asn) o;
        return Objects.equals(asn, other.asn)
                && Objects.equals(name, other.name)
                && Objects.equals(domain, other.domain)
                && Objects.equals(route, other.route)
                && Objects.equals(type, other.type);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        int ret = 0;

        if (asn != null) ret = ret * 31 + asn.hashCode();
        if (name != null) ret = ret * 31 + name.hashCode();
        if (domain != null) ret = ret * 31 + domain.hashCode();
        if (route != null) ret = ret * 31 + route.hashCode();
        if (type != null) ret = ret * 31 + type.hashCode();

        return ret;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return "Asn{"
                + "asn=\"" + asn + "\", "
                + "name=\"" + name + "\", "
                + "domain=\"" + domain + "\", "
                + "route=\"" + route + "\", "
                + "type=\"" + type + "\""
                + "}";
    }
}
