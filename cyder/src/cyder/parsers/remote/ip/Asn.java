package cyder.parsers.remote.ip;

/** An object for parsing returned ip data asn objects. */
public class Asn {
    private String asn;
    private String name;
    private String domain;
    private String route;
    private String type;

    public void setAsn(String asn) {
        this.asn = asn;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setDomain(String domain) {
        this.domain = domain;
    }

    public void setRoute(String route) {
        this.route = route;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getAsn() {
        return asn;
    }

    public String getName() {
        return name;
    }

    public String getDomain() {
        return domain;
    }

    public String getRoute() {
        return route;
    }

    public String getType() {
        return type;
    }
}
