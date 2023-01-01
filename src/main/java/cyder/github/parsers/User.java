package cyder.github.parsers;

/**
 * A json serialization class for a GitHub user.
 */
@SuppressWarnings("unused")
public class User {
    public String login;
    public int id;
    public String node_id;
    public String avatar_url;
    public String gravatar_id;
    public String url;
    public String html_url;
    public String following_url;
    public String followers_url;
    public String gists_url;
    public String starred_url;
    public String subscriptions_url;
    public String organizations_url;
    public String repos_url;
    public String events_url;
    public String received_events_url;
    public String type;
    public boolean site_admin;

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        } else if (!(o instanceof User)) {
            return false;
        }

        User other = (User) o;
        return other.login.equals(login)
                && other.id == id
                && other.node_id.equals(node_id)
                && other.avatar_url.equals(avatar_url)
                && other.gravatar_id.equals(gravatar_id)
                && other.url.equals(url)
                && other.html_url.equals(html_url)
                && other.following_url.equals(following_url)
                && other.followers_url.equals(followers_url)
                && other.gists_url.equals(gists_url)
                && other.starred_url.equals(starred_url)
                && other.subscriptions_url.equals(subscriptions_url)
                && other.organizations_url.equals(organizations_url)
                && other.repos_url.equals(repos_url)
                && other.events_url.equals(events_url)
                && other.received_events_url.equals(received_events_url)
                && other.type.equals(type)
                && other.site_admin == site_admin;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        int ret = login.hashCode();
        ret = 31 * ret + Integer.hashCode(id);
        ret = 31 * ret + node_id.hashCode();
        ret = 31 * ret + avatar_url.hashCode();
        ret = 31 * ret + gravatar_id.hashCode();
        ret = 31 * ret + url.hashCode();
        ret = 31 * ret + html_url.hashCode();
        ret = 31 * ret + following_url.hashCode();
        ret = 31 * ret + followers_url.hashCode();
        ret = 31 * ret + gists_url.hashCode();
        ret = 31 * ret + starred_url.hashCode();
        ret = 31 * ret + subscriptions_url.hashCode();
        ret = 31 * ret + organizations_url.hashCode();
        ret = 31 * ret + repos_url.hashCode();
        ret = 31 * ret + events_url.hashCode();
        ret = 31 * ret + received_events_url.hashCode();
        ret = 31 * ret + type.hashCode();
        ret = 31 * ret + Boolean.hashCode(site_admin);

        return ret;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return "User{"
                + "login=\"" + login + "\""
                + ", id=" + id
                + ", node_id=\"" + node_id + "\""
                + ", avatar_url=\"" + avatar_url + "\""
                + ", gravatar_id=\"" + gravatar_id + "\""
                + ", url=\"" + url + "\""
                + ", html_url=\"" + html_url + "\""
                + ", following_url=\"" + following_url + "\""
                + ", followers_url=\"" + followers_url + "\""
                + ", gists_url=\"" + gists_url + "\""
                + ", starred_url=\"" + starred_url + "\""
                + ", subscriptions_url=\"" + subscriptions_url + "\""
                + ", organizations_url=\"" + organizations_url + "\""
                + ", repos_url=\"" + repos_url + "\""
                + ", events_url=\"" + events_url + "\""
                + ", received_events_url=\"" + received_events_url + "\""
                + ", type=\"" + type + "\""
                + ", site_admin=" + site_admin
                + "}";
    }
}
