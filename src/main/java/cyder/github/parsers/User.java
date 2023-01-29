package cyder.github.parsers;

import com.google.common.base.Preconditions;
import com.google.gson.annotations.SerializedName;
import cyder.logging.LogTag;
import cyder.logging.Logger;

/**
 * A json serialization class for a GitHub user.
 * API endpoint: api.github.com/users/username.
 */
@SuppressWarnings("unused")
public class User {
    /**
     * This user's username.
     */
    public String login;

    /**
     * This user's id.
     */
    public int id;

    /**
     * This user's node id.
     */
    @SerializedName("node_id")
    public String nodeId;

    /**
     * The url to this user's avatar.
     */
    @SerializedName("avatar_url")
    public String avatarUrl;

    /**
     * The id of this user's gravatar if not using a custom avatar.
     */
    @SerializedName("gravatar_id")
    public String gravatarId;

    /**
     * The url used to obtain these results from the users API endpoint.
     */
    public String url;

    /**
     * The url to this user's github profile page.
     */
    @SerializedName("html_url")
    public String htmlUrl;

    /**
     * The url to the list of users following this user.
     */
    @SerializedName("following_url")
    public String followingUrl;

    /**
     * The url to the list of users this user is following.
     */
    @SerializedName("followers_url")
    public String followersUrl;

    /**
     * The url to the list of gists this user has created.
     */
    @SerializedName("gists_url")
    public String gistsUrl;

    /**
     * The url to the list of repos this user has stared.
     */
    @SerializedName("starred_url")
    public String starredUrl;

    /**
     * The subscriptions url for this user.
     */
    @SerializedName("subscriptions_url")
    public String subscriptionsUrl;

    /**
     * The organizations url for this user.
     */
    @SerializedName("organizations_url")
    public String organizationsUrl;

    /**
     * The repos url for this user.
     */
    @SerializedName("repos_url")
    public String reposUrl;

    /**
     * The events url for this user.
     */
    @SerializedName("events_url")
    public String eventsUrl;

    /**
     * The received events url for this user.
     */
    @SerializedName("received_events_url")
    public String receivedEventsUrl;

    /**
     * The type of user this is.
     */
    public String type;

    /**
     * Whether this user is an admin.
     */
    @SerializedName("site_admin")
    public boolean siteAdmin;

    /**
     * This user's display name.
     */
    public String name;

    /**
     * This user's company.
     */
    public String company;

    /**
     * This user's blog (actually website, I have no idea why GitHub calls it blog in their API).
     */
    public String blog;

    /**
     * This user's set location.
     */
    public String location;

    /**
     * This user's public email.
     */
    public String email;

    /**
     * Whether this user has designated themselves as hireable.
     */
    public boolean hireable;

    /**
     * This user's bio.
     */
    public String bio;

    /**
     * This user's twitter username if set.
     */
    @SerializedName("twitter_username")
    public String twitterUsername;

    /**
     * The number of public repos exposed by this user.
     */
    @SerializedName("public_repos")
    public int publicRepos;

    /**
     * The number of public Gists exposed by this user.
     */
    @SerializedName("public_gists")
    public int publicGists;

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
    @SerializedName("created_at")
    public String createdAt;

    /**
     * The last time this user was updated at.
     */
    @SerializedName("updated_at")
    public String updatedAt;

    /**
     * Constructs a new GitHub user object.
     */
    public User() {
        Logger.log(LogTag.OBJECT_CREATION, this);
    }

    /**
     * Returns this user's username.
     *
     * @return this user's username
     */
    public String getLogin() {
        return login;
    }

    /**
     * Sets this user's username.
     *
     * @param login this user's username
     */
    public void setLogin(String login) {
        Preconditions.checkNotNull(login);

        this.login = login;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getNodeId() {
        return nodeId;
    }

    public void setNodeId(String nodeId) {
        Preconditions.checkNotNull(nodeId);

        this.nodeId = nodeId;
    }

    public String getAvatarUrl() {
        return avatarUrl;
    }

    public void setAvatarUrl(String avatarUrl) {
        Preconditions.checkNotNull(avatarUrl);

        this.avatarUrl = avatarUrl;
    }

    public String getGravatarId() {
        return gravatarId;
    }

    public void setGravatarId(String gravatarId) {
        Preconditions.checkNotNull(gravatarId);

        this.gravatarId = gravatarId;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        Preconditions.checkNotNull(url);

        this.url = url;
    }

    public String getHtmlUrl() {
        return htmlUrl;
    }

    public void setHtmlUrl(String htmlUrl) {
        Preconditions.checkNotNull(htmlUrl);

        this.htmlUrl = htmlUrl;
    }

    public String getFollowingUrl() {
        return followingUrl;
    }

    public void setFollowingUrl(String followingUrl) {
        Preconditions.checkNotNull(followingUrl);

        this.followingUrl = followingUrl;
    }

    public String getFollowersUrl() {
        return followersUrl;
    }

    public void setFollowersUrl(String followersUrl) {
        Preconditions.checkNotNull(followersUrl);

        this.followersUrl = followersUrl;
    }

    public String getGistsUrl() {
        return gistsUrl;
    }

    public void setGistsUrl(String gistsUrl) {
        Preconditions.checkNotNull(gistsUrl);

        this.gistsUrl = gistsUrl;
    }

    public String getStarredUrl() {
        return starredUrl;
    }

    public void setStarredUrl(String starredUrl) {
        Preconditions.checkNotNull(starredUrl);

        this.starredUrl = starredUrl;
    }

    public String getSubscriptionsUrl() {
        return subscriptionsUrl;
    }

    public void setSubscriptionsUrl(String subscriptionsUrl) {
        Preconditions.checkNotNull(subscriptionsUrl);

        this.subscriptionsUrl = subscriptionsUrl;
    }

    public String getOrganizationsUrl() {
        return organizationsUrl;
    }

    public void setOrganizationsUrl(String organizationsUrl) {
        Preconditions.checkNotNull(organizationsUrl);

        this.organizationsUrl = organizationsUrl;
    }

    public String getReposUrl() {
        return reposUrl;
    }

    public void setReposUrl(String reposUrl) {
        Preconditions.checkNotNull(reposUrl);

        this.reposUrl = reposUrl;
    }

    public String getEventsUrl() {
        return eventsUrl;
    }

    public void setEventsUrl(String eventsUrl) {
        Preconditions.checkNotNull(eventsUrl);

        this.eventsUrl = eventsUrl;
    }

    public String getReceivedEventsUrl() {
        return receivedEventsUrl;
    }

    public void setReceivedEventsUrl(String receivedEventsUrl) {
        Preconditions.checkNotNull(receivedEventsUrl);

        this.receivedEventsUrl = receivedEventsUrl;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        Preconditions.checkNotNull(type);

        this.type = type;
    }

    public boolean isSiteAdmin() {
        return siteAdmin;
    }

    public void setSiteAdmin(boolean siteAdmin) {
        this.siteAdmin = siteAdmin;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        Preconditions.checkNotNull(name);

        this.name = name;
    }

    public String getCompany() {
        return company;
    }

    public void setCompany(String company) {
        Preconditions.checkNotNull(company);

        this.company = company;
    }

    public String getBlog() {
        return blog;
    }

    public void setBlog(String blog) {
        Preconditions.checkNotNull(blog);

        this.blog = blog;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        Preconditions.checkNotNull(location);

        this.location = location;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        Preconditions.checkNotNull(email);

        this.email = email;
    }

    public boolean isHireable() {
        return hireable;
    }

    public void setHireable(boolean hireable) {
        this.hireable = hireable;
    }

    public String getBio() {
        return bio;
    }

    public void setBio(String bio) {
        Preconditions.checkNotNull(bio);

        this.bio = bio;
    }

    public String getTwitterUsername() {
        return twitterUsername;
    }

    public void setTwitterUsername(String twitterUsername) {
        Preconditions.checkNotNull(twitterUsername);

        this.twitterUsername = twitterUsername;
    }

    public int getPublicRepos() {
        return publicRepos;
    }

    public void setPublicRepos(int publicRepos) {
        this.publicRepos = publicRepos;
    }

    public int getPublicGists() {
        return publicGists;
    }

    public void setPublicGists(int publicGists) {
        this.publicGists = publicGists;
    }

    public int getFollowers() {
        return followers;
    }

    public void setFollowers(int followers) {
        this.followers = followers;
    }

    public int getFollowing() {
        return following;
    }

    public void setFollowing(int following) {
        this.following = following;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        Preconditions.checkNotNull(createdAt);

        this.createdAt = createdAt;
    }

    public String getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(String updatedAt) {
        Preconditions.checkNotNull(updatedAt);

        this.updatedAt = updatedAt;
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
                && other.nodeId.equals(nodeId)
                && other.avatarUrl.equals(avatarUrl)
                && other.gravatarId.equals(gravatarId)
                && other.url.equals(url)
                && other.htmlUrl.equals(htmlUrl)
                && other.followingUrl.equals(followingUrl)
                && other.followersUrl.equals(followersUrl)
                && other.gistsUrl.equals(gistsUrl)
                && other.starredUrl.equals(starredUrl)
                && other.subscriptionsUrl.equals(subscriptionsUrl)
                && other.organizationsUrl.equals(organizationsUrl)
                && other.reposUrl.equals(reposUrl)
                && other.eventsUrl.equals(eventsUrl)
                && other.receivedEventsUrl.equals(receivedEventsUrl)
                && other.type.equals(type)
                && other.siteAdmin == siteAdmin
                && other.name.equals(name)
                && other.company.equals(company)
                && other.location.equals(location)
                && other.email.equals(email)
                && other.hireable == hireable
                && other.bio.equals(bio)
                && other.twitterUsername.equals(twitterUsername)
                && other.publicRepos == publicRepos
                && other.publicGists == publicGists
                && other.followers == followers
                && other.following == following
                && other.createdAt.equals(createdAt)
                && other.updatedAt.equals(updatedAt);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        int ret = login.hashCode();

        ret = 31 * ret + Integer.hashCode(id);
        ret = 31 * ret + nodeId.hashCode();
        ret = 31 * ret + avatarUrl.hashCode();
        ret = 31 * ret + gravatarId.hashCode();
        ret = 31 * ret + url.hashCode();
        ret = 31 * ret + htmlUrl.hashCode();
        ret = 31 * ret + followingUrl.hashCode();
        ret = 31 * ret + followersUrl.hashCode();
        ret = 31 * ret + gistsUrl.hashCode();
        ret = 31 * ret + starredUrl.hashCode();
        ret = 31 * ret + subscriptionsUrl.hashCode();
        ret = 31 * ret + organizationsUrl.hashCode();
        ret = 31 * ret + reposUrl.hashCode();
        ret = 31 * ret + eventsUrl.hashCode();
        ret = 31 * ret + receivedEventsUrl.hashCode();
        ret = 31 * ret + type.hashCode();
        ret = 31 * ret + Boolean.hashCode(siteAdmin);
        ret = 31 * ret + name.hashCode();
        ret = 31 * ret + company.hashCode();
        ret = 31 * ret + blog.hashCode();
        ret = 31 * ret + location.hashCode();
        ret = 31 * ret + email.hashCode();
        ret = 31 * ret + Boolean.hashCode(hireable);
        ret = 31 * ret + bio.hashCode();
        ret = 31 * ret + twitterUsername.hashCode();
        ret = 31 * ret + Integer.hashCode(publicRepos);
        ret = 31 * ret + Integer.hashCode(publicGists);
        ret = 31 * ret + Integer.hashCode(followers);
        ret = 31 * ret + Integer.hashCode(following);
        ret = 31 * ret + createdAt.hashCode();
        ret = 31 * ret + updatedAt.hashCode();

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
                + ", node_id=\"" + nodeId + "\""
                + ", avatar_url=\"" + avatarUrl + "\""
                + ", gravatar_id=\"" + gravatarId + "\""
                + ", url=\"" + url + "\""
                + ", html_url=\"" + htmlUrl + "\""
                + ", following_url=\"" + followingUrl + "\""
                + ", followers_url=\"" + followersUrl + "\""
                + ", gists_url=\"" + gistsUrl + "\""
                + ", starred_url=\"" + starredUrl + "\""
                + ", subscriptions_url=\"" + subscriptionsUrl + "\""
                + ", organizations_url=\"" + organizationsUrl + "\""
                + ", repos_url=\"" + reposUrl + "\""
                + ", events_url=\"" + eventsUrl + "\""
                + ", received_events_url=\"" + receivedEventsUrl + "\""
                + ", type=\"" + type + "\""
                + ", site_admin=" + siteAdmin
                + ", name=" + name
                + ", company=" + company
                + ", blog=" + blog
                + ", location=" + location
                + ", email=" + email
                + ", hireable=" + hireable
                + ", bio=" + bio
                + ", twitter_username=" + twitterUsername
                + ", public_repos=" + publicRepos
                + ", public_gists=" + publicGists
                + ", followers=" + followers
                + ", following=" + following
                + ", created_at=" + createdAt
                + ", updated_at=" + updatedAt
                + "}";
    }
}
