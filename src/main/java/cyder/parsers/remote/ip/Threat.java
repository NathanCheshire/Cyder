package cyder.parsers.remote.ip;

import cyder.logging.LogTag;
import cyder.logging.Logger;

/**
 * An object for parsing returned ip data threat objects.
 */
public class Threat {
    /**
     * Whether the IP address is associated with a node on the Tor network.
     */
    private boolean is_tor;

    /**
     * Whether the IP address belongs to Apple's iCloud relay service.
     */
    private boolean is_icloud_relay;

    /**
     * Whether the ip address is a known proxy.
     */
    private boolean is_proxy;

    /**
     * Whether the IP address belongs to a datacenter including all cloud providers.
     */
    private boolean is_datacenter;

    /**
     * Whether is_tor or is_proxy are true.
     */
    private boolean is_anonymous;

    /**
     * Whether the IP address is a known source of malicious activity, i.e. attacks, malware, botnet activity etc.
     */
    private boolean is_known_attacker;

    /**
     * Whether the IP address is a known source of abuse i.e. spam,
     * harvesters, registration bots and other nuisance bots etc.
     */
    private boolean is_known_abuser;

    /**
     * Whether is_known_abuser or is_known_attacker are true.
     */
    private boolean is_threat;

    /**
     * Whether the ip address is a bogon address.
     */
    private boolean is_bogon;

    /**
     * Constructs a new threat object.
     */
    public Threat() {
        Logger.log(LogTag.OBJECT_CREATION, this);
    }

    /**
     * Sets the IP address is associated with a node on the Tor network.
     *
     * @param is_tor the IP address is associated with a node on the Tor network
     */
    public void setIs_tor(boolean is_tor) {
        this.is_tor = is_tor;
    }

    /**
     * Sets whether the ip address is a known proxy.
     *
     * @param is_proxy whether the ip address is a known proxy
     */
    public void setIs_proxy(boolean is_proxy) {
        this.is_proxy = is_proxy;
    }

    /**
     * Sets whether is_tor or is_proxy are true.
     *
     * @param is_anonymous whether is_tor or is_proxy are true
     */
    public void setIs_anonymous(boolean is_anonymous) {
        this.is_anonymous = is_anonymous;
    }

    /**
     * Sets whether the IP address is a known source of malicious activity, i.e. attacks, malware, botnet activity etc.
     *
     * @param is_known_attacker whether the IP address is a known source of
     *                          malicious activity, i.e. attacks, malware, botnet activity etc
     */
    public void setIs_known_attacker(boolean is_known_attacker) {
        this.is_known_attacker = is_known_attacker;
    }

    /**
     * Sets whether the IP address is a known source of abuse i.e. spam,
     * harvesters, registration bots and other nuisance bots etc.
     *
     * @param is_known_abuser whether whether the IP address is a known source of abuse i.e. spam,
     *                        harvesters, registration bots and other nuisance bots etc
     */
    public void setIs_known_abuser(boolean is_known_abuser) {
        this.is_known_abuser = is_known_abuser;
    }

    /**
     * Sets whether is_known_abuser or is_known_attacker are true.
     *
     * @param is_threat whether is_known_abuser or is_known_attacker are true
     */
    public void setIs_threat(boolean is_threat) {
        this.is_threat = is_threat;
    }

    /**
     * Sets whether the ip address is a bogon address.
     *
     * @param is_bogon whether the ip address is a bogon address
     */
    public void setIs_bogon(boolean is_bogon) {
        this.is_bogon = is_bogon;
    }

    /**
     * Sets whether the IP address belongs to Apple's iCloud relay service.
     *
     * @param is_icloud_relay whether the IP address belongs to Apple's iCloud relay service
     */
    public void setIs_icloud_relay(boolean is_icloud_relay) {
        this.is_icloud_relay = is_icloud_relay;
    }

    /**
     * Sets whether the IP address belongs to a datacenter including all cloud providers.
     *
     * @param is_datacenter whether the IP address belongs to a datacenter including all cloud providers
     */
    public void setIs_datacenter(boolean is_datacenter) {
        this.is_datacenter = is_datacenter;
    }

    /**
     * Returns whether the IP address is associated with a node on the Tor network.
     *
     * @return whether the IP address is associated with a node on the Tor network
     */
    public boolean isIs_tor() {
        return is_tor;
    }

    /**
     * Returns whether the ip address is a known proxy.
     *
     * @return whether the ip address is a known proxy
     */
    public boolean isIs_proxy() {
        return is_proxy;
    }

    /**
     * Returns whether is_tor or is_proxy are true.
     *
     * @return whether is_tor or is_proxy are true
     */
    public boolean isIs_anonymous() {
        return is_anonymous;
    }

    /**
     * Returns whether the IP address is a known source of malicious activity,
     * i.e. attacks, malware, botnet activity etc.
     *
     * @return whether the IP address is a known source of malicious activity,
     * i.e. attacks, malware, botnet activity etc
     */
    public boolean isIs_known_attacker() {
        return is_known_attacker;
    }

    /**
     * Returns whether whether the IP address is a known source of abuse i.e.
     * spam, harvesters, registration bots and other nuisance bots etc.
     *
     * @return whether whether the IP address is a known source of abuse i.e.
     * spam, harvesters, registration bots and other nuisance bots etc
     */
    public boolean isIs_known_abuser() {
        return is_known_abuser;
    }

    /**
     * Returns whether is_known_abuser or is_known_attacker are true.
     *
     * @return whether is_known_abuser or is_known_attacker are true
     */
    public boolean isIs_threat() {
        return is_threat;
    }

    /**
     * Returns whether the ip address is a bogon address.
     *
     * @return the ip address is a bogon address
     */
    public boolean isIs_bogon() {
        return is_bogon;
    }

    /**
     * Returns whether the IP address belongs to Apple's iCloud relay service.
     *
     * @return whether the IP address belongs to Apple's iCloud relay service
     */
    public boolean isIs_icloud_relay() {
        return is_icloud_relay;
    }

    /**
     * Returns whether the IP address belongs to a datacenter including all cloud providers.
     *
     * @return whether the IP address belongs to a datacenter including all cloud providers.
     */
    public boolean isIs_datacenter() {
        return is_datacenter;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        } else if (!(o instanceof Threat)) {
            return false;
        }

        Threat other = (Threat) o;

        return is_tor == other.is_tor
                && is_icloud_relay == other.is_icloud_relay
                && is_proxy == other.is_proxy
                && is_datacenter == other.is_datacenter
                && is_anonymous == other.is_anonymous
                && is_known_attacker == other.is_known_attacker
                && is_known_abuser == other.is_known_abuser
                && is_threat == other.is_threat
                && is_bogon == other.is_bogon;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        int ret = Boolean.hashCode(is_tor);

        ret = 31 * ret + Boolean.hashCode(is_icloud_relay);
        ret = 31 * ret + Boolean.hashCode(is_proxy);
        ret = 31 * ret + Boolean.hashCode(is_datacenter);
        ret = 31 * ret + Boolean.hashCode(is_anonymous);
        ret = 31 * ret + Boolean.hashCode(is_known_attacker);
        ret = 31 * ret + Boolean.hashCode(is_known_abuser);
        ret = 31 * ret + Boolean.hashCode(is_threat);
        ret = 31 * ret + Boolean.hashCode(is_bogon);

        return ret;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return "Threat{"
                + "is_tor=" + is_tor + ", "
                + "is_icloud_relay=" + is_icloud_relay + ", "
                + "is_proxy=" + is_proxy + ", "
                + "is_datacenter=" + is_datacenter + ", "
                + "is_anonymous=" + is_anonymous + ", "
                + "is_known_attacker=" + is_known_attacker + ", "
                + "is_known_abuser=" + is_known_abuser + ", "
                + "is_threat=" + is_threat + ", "
                + "is_bogon=" + is_bogon
                + "}";
    }
}
