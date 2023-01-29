package cyder.github.parsers;

import cyder.logging.LogTag;
import cyder.logging.Logger;

/**
 * A json serialization class for a GitHub user.
 * API endpoint: api.github.com/users/username.
 */
@SuppressWarnings("unused")
public class User {
    /**
     * The username.
     */
    public String login;

    /**
     * The user id.
     */
    public int id;

    /**
     * The node id.
     */
    public String node_id;

    /**
     * The url to the user's avatar.
     */
    public String avatar_url;

    /**
     * The id of the user's gravatar if present.
     */
    public String gravatar_id;

    /**
     * The url used to obtain these results.
     */
    public String url;

    /**
     * The url to the user's github profile page.
     */
    public String html_url;

    /**
     * The url to the list of users following this user.
     */
    public String following_url;

    /**
     * The url to the list of users this user is following.
     */
    public String followers_url;

    /**
     * The url to the list of gists this user has created.
     */
    public String gists_url;

    /**
     * The url to the list of repos this user has stared.
     */
    public String starred_url;

    /**
     * The subscriptions url for this user.
     */
    public String subscriptions_url;

    /**
     * The organizations url for this user.
     */
    public String organizations_url;

    /**
     * The repos url for this user.
     */
    public String repos_url;

    /**
     * The events url for this user.
     */
    public String events_url;

    /**
     * The received events url for this user.
     */
    public String received_events_url;

    /**
     * The type of user this is.
     */
    public String type;

    /**
     * Whether this user is an admin.
     */
    public boolean site_admin;

    /**
     * The user's name.
     */
    public String name;

    /**
     * The user's company.
     */
    public String company;

    /**
     * The user's blog (website, I have no idea why GH calls it blog).
     */
    public String blog;

    /**
     * The user's location.
     */
    public String location;

    /**
     * The user's public email.
     */
    public String email;

    /**
     * Whether the user has designated themselves as hireable.
     */
    public boolean hireable;

    /**
     * The user's bio.
     */
    public String bio;

    /**
     * The user's twitter username if present.
     */
    public String twitter_username;

    /**
     * The number of public repos exposed by this user.
     */
    public int public_repos;

    /**
     * The number of public gists exposed by this user.
     */
    public int public_gists;

    /**
     * The number of followers this user has.
     */
    public int followers;

    /**
     * The number of users this user is following.
     */
    public int following;

    /**
     * The time this user was created at.
     */
    public String created_at;

    /**
     * The last time this user was updated at.
     */
    public String updated_at;

    /**
     * Constructs a new GitHub user object.
     */
    public User() {
        Logger.log(LogTag.OBJECT_CREATION, this);
    }

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
                && other.site_admin == site_admin
                && other.name.equals(name)
                && other.company.equals(company)
                && other.location.equals(location)
                && other.email.equals(email)
                && other.hireable == hireable
                && other.bio.equals(bio)
                && other.twitter_username.equals(twitter_username)
                && other.public_repos == public_repos
                && other.public_gists == public_gists
                && other.followers == followers
                && other.following == following
                && other.created_at.equals(created_at)
                && other.updated_at.equals(updated_at);
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
        ret = 31 * ret + name.hashCode();
        ret = 31 * ret + company.hashCode();
        ret = 31 * ret + blog.hashCode();
        ret = 31 * ret + location.hashCode();
        ret = 31 * ret + email.hashCode();
        ret = 31 * ret + Boolean.hashCode(hireable);
        ret = 31 * ret + bio.hashCode();
        ret = 31 * ret + twitter_username.hashCode();
        ret = 31 * ret + Integer.hashCode(public_repos);
        ret = 31 * ret + Integer.hashCode(public_gists);
        ret = 31 * ret + Integer.hashCode(followers);
        ret = 31 * ret + Integer.hashCode(following);
        ret = 31 * ret + created_at.hashCode();
        ret = 31 * ret + updated_at.hashCode();

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
                + ", name=" + name
                + ", company=" + company
                + ", blog=" + blog
                + ", location=" + location
                + ", email=" + email
                + ", hireable=" + hireable
                + ", bio=" + bio
                + ", twitter_username=" + twitter_username
                + ", public_repos=" + public_repos
                + ", public_gists=" + public_gists
                + ", followers=" + followers
                + ", following=" + following
                + ", created_at=" + created_at
                + ", updated_at=" + updated_at
                + "}";
    }
}
