package cyder.github.parsers;

import com.google.gson.annotations.SerializedName;
import cyder.logging.LogTag;
import cyder.logging.Logger;

import java.util.ArrayList;
import java.util.Objects;

/**
 * A json serialization class for a GitHub repo issue.
 */
public class Issue {
    /**
     * The url for this issue.
     */
    public String url;

    /**
     * The url for the repository this issue was created for.
     */
    @SerializedName("repository_url")
    public String repositoryUrl;

    /**
     * The labels url for this issue.
     */
    @SerializedName("labels_url")
    public String labelsUrl;

    /**
     * The comments url for this issue.
     */
    @SerializedName("comments_url")
    public String commentsUrl;

    /**
     * The events url for this issue.
     */
    @SerializedName("events_url")
    public String eventsUrl;

    /**
     * The html url for this issue.
     */
    @SerializedName("html_url")
    public String htmlUrl;

    /**
     * The id of this issue.
     */
    public int id;

    /**
     * The node id of this issue.
     */
    @SerializedName("node_id")
    public String nodeId;

    /**
     * The sequential number of this issue.
     */
    public int number;

    /**
     * The title of this issue.
     */
    public String title;

    /**
     * A user object.
     */
    public User user;

    /**
     * Labels associated with this issue.
     */
    public ArrayList<String> labels;

    /**
     * The state of this issue.
     */
    public String state;

    /**
     * Whether this issue is currently locked.
     */
    public boolean locked;

    /**
     * The assignee for this issue.
     */
    public User assignee;

    /**
     * The assignees for this issue.
     */
    public ArrayList<User> assignees;

    /**
     * Whether this issue is a milestone.
     */
    public boolean milestone;

    /**
     * The number of comments on this issue.
     */
    public int comments;

    /**
     * The time at which this issue was created.
     */
    @SerializedName("created_at")
    public String createdAt;

    /**
     * The time at which this issue was last updated at.
     */
    @SerializedName("updated_at")
    public String updatedAt;

    /**
     * The time at which this issue was closed at.
     */
    @SerializedName("closed_at")
    public String closedAt;

    /**
     * The association of the author of this issue.
     */
    @SerializedName("author_association")
    public String authorAssociation;

    /**
     * The reason for locking the issue.
     */
    @SerializedName("active_lock_reason")
    public String activeLockReason;

    /**
     * The contents of the issue.
     */
    public String body;

    /**
     * The reactions to the issue.
     */
    public Reaction reactions;

    /**
     * The url for the timeline of this issue.
     */
    @SerializedName("timeline_url")
    public String timelineUrl;

    /**
     * Whether this issue was created via the GitHub app.
     */
    @SerializedName("performed_via_github_app")
    public boolean performedViaGithubApp;

    /**
     * The reason for the issue being in the current state.
     */
    @SerializedName("state_reason")
    public String stateReason;

    /**
     * Creates a new issue object.
     */
    public Issue() {
        Logger.log(LogTag.OBJECT_CREATION, this);
    }

    /**
     * Returns the url for this issue.
     *
     * @return the url for this issue
     */
    public String getUrl() {
        return url;
    }

    /**
     * Sets the url for this issue.
     *
     * @param url the url for this issue
     */
    public void setUrl(String url) {
        this.url = url;
    }

    /**
     * Returns the url for the repository this issue was created for.
     *
     * @return the url for the repository this issue was created for
     */
    public String getRepositoryUrl() {
        return repositoryUrl;
    }

    /**
     * Sets the url for the repository this issue was created for.
     *
     * @param repositoryUrl the url for the repository this issue was created for
     */
    public void setRepositoryUrl(String repositoryUrl) {
        this.repositoryUrl = repositoryUrl;
    }

    /**
     * Returns the labels url for this issue.
     *
     * @return the labels url for this issue
     */
    public String getLabelsUrl() {
        return labelsUrl;
    }

    /**
     * Sets the labels url for this issue.
     *
     * @param labelsUrl the labels url for this issue
     */
    public void setLabelsUrl(String labelsUrl) {
        this.labelsUrl = labelsUrl;
    }

    /**
     * Returns the comments url for this issue.
     *
     * @return the comments url for this issue
     */
    public String getCommentsUrl() {
        return commentsUrl;
    }

    /**
     * Sets the comments url for this issue.
     *
     * @param commentsUrl the comments url for this issue
     */
    public void setCommentsUrl(String commentsUrl) {
        this.commentsUrl = commentsUrl;
    }

    /**
     * Returns the events url for this issue.
     *
     * @return the events url for this issue
     */
    public String getEventsUrl() {
        return eventsUrl;
    }

    /**
     * Sets the events url for this issue.
     *
     * @param eventsUrl the events url for this issue
     */
    public void setEventsUrl(String eventsUrl) {
        this.eventsUrl = eventsUrl;
    }

    /**
     * Returns the html url for this issue.
     *
     * @return the html url for this issue
     */
    public String getHtmlUrl() {
        return htmlUrl;
    }

    /**
     * Sets the html url for this issue.
     *
     * @param htmlUrl the html url for this issue
     */
    public void setHtmlUrl(String htmlUrl) {
        this.htmlUrl = htmlUrl;
    }

    /**
     * Returns the id of this issue.
     *
     * @return the id of this issue
     */
    public int getId() {
        return id;
    }

    /**
     * Sets the id of this issue.
     *
     * @param id the id of this issue
     */
    public void setId(int id) {
        this.id = id;
    }

    /**
     * Returns the node id of this issue.
     *
     * @return the node id of this issue
     */
    public String getNodeId() {
        return nodeId;
    }

    /**
     * Sets the node id of this issue
     *
     * @param nodeId the node id of this issue
     */
    public void setNodeId(String nodeId) {
        this.nodeId = nodeId;
    }

    /**
     * Returns the sequential number of this issue.
     *
     * @return the sequential number of this issue
     */
    public int getNumber() {
        return number;
    }

    /**
     * Set the sequential number of this issue.
     *
     * @param number the sequential number of this issue
     */
    public void setNumber(int number) {
        this.number = number;
    }

    /**
     * Returns the title of this issue.
     *
     * @return the title of this issue
     */
    public String getTitle() {
        return title;
    }

    /**
     * Sets the title of this issue.
     *
     * @param title the title of this issue
     */
    public void setTitle(String title) {
        this.title = title;
    }

    /**
     * Returns a user object.
     *
     * @return a user object
     */
    public User getUser() {
        return user;
    }

    /**
     * Sets a user object.
     *
     * @param user a user object
     */
    public void setUser(User user) {
        this.user = user;
    }

    /**
     * Returns the associated with this issue.
     *
     * @return the associated with this issue
     */
    public ArrayList<String> getLabels() {
        return labels;
    }

    /**
     * Sets the associated with this issue.
     *
     * @param labels the associated with this issue
     */
    public void setLabels(ArrayList<String> labels) {
        this.labels = labels;
    }

    /**
     * Returns the state of this issue.
     *
     * @return the state of this issue
     */
    public String getState() {
        return state;
    }

    /**
     * Sets the state of this issue.
     *
     * @param state the state of this issue
     */
    public void setState(String state) {
        this.state = state;
    }

    /**
     * Returns whether this issue is currently locked.
     *
     * @return whether this issue is currently locked
     */
    public boolean isLocked() {
        return locked;
    }

    /**
     * Sets whether this issue is currently locked.
     *
     * @param locked whether this issue is currently locked
     */
    public void setLocked(boolean locked) {
        this.locked = locked;
    }

    /**
     * Returns the assignee for this issue.
     *
     * @return the assignee for this issue
     */
    public User getAssignee() {
        return assignee;
    }

    /**
     * Sets the assignee for this issue.
     *
     * @param assignee the assignee for this issue
     */
    public void setAssignee(User assignee) {
        this.assignee = assignee;
    }

    /**
     * Returns the assignees for this issue.
     *
     * @return the assignees for this issue
     */
    public ArrayList<User> getAssignees() {
        return assignees;
    }

    /**
     * Sets the assignees for this issue.
     *
     * @param assignees the assignees for this issue
     */
    public void setAssignees(ArrayList<User> assignees) {
        this.assignees = assignees;
    }

    /**
     * Returns whether this issue is a milestone
     *
     * @return whether this issue is a milestone
     */
    public boolean isMilestone() {
        return milestone;
    }

    /**
     * Sets whether this issue is a milestone.
     *
     * @param milestone whether this issue is a milestone
     */
    public void setMilestone(boolean milestone) {
        this.milestone = milestone;
    }

    /**
     * Returns the number of comments on this issue.
     *
     * @return the number of comments on this issue
     */
    public int getComments() {
        return comments;
    }

    /**
     * Sets the number of comments on this issue.
     *
     * @param comments the number of comments on this issue
     */
    public void setComments(int comments) {
        this.comments = comments;
    }

    /**
     * Returns the time at which this issue was created.
     *
     * @return the time at which this issue was created
     */
    public String getCreatedAt() {
        return createdAt;
    }

    /**
     * Sets the time at which this issue was created.
     *
     * @param createdAt the time at which this issue was created
     */
    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    /**
     * Returns the time at which this issue was last updated at.
     *
     * @return the time at which this issue was last updated at
     */
    public String getUpdatedAt() {
        return updatedAt;
    }

    /**
     * Sets the time at which this issue was last updated at.
     *
     * @param updatedAt the time at which this issue was last updated at
     */
    public void setUpdatedAt(String updatedAt) {
        this.updatedAt = updatedAt;
    }

    /**
     * Returns the time at which this issue was closed at.
     *
     * @return the time at which this issue was closed at
     */
    public String getClosedAt() {
        return closedAt;
    }

    /**
     * Sets the time at which this issue was closed at.
     *
     * @param closedAt the time at which this issue was closed at
     */
    public void setClosedAt(String closedAt) {
        this.closedAt = closedAt;
    }

    /**
     * Returns the association of the author of this issue.
     *
     * @return the association of the author of this issue
     */
    public String getAuthorAssociation() {
        return authorAssociation;
    }

    /**
     * Sets the association of the author of this issue.
     *
     * @param authorAssociation the association of the author of this issue
     */
    public void setAuthorAssociation(String authorAssociation) {
        this.authorAssociation = authorAssociation;
    }

    /**
     * Returns the reason for locking the issue.
     *
     * @return the reason for locking the issue
     */
    public String isActiveLockReason() {
        return activeLockReason;
    }

    /**
     * Sets the reason for locking the issue.
     *
     * @param activeLockReason the reason for locking the issue
     */
    public void setActiveLockReason(String activeLockReason) {
        this.activeLockReason = activeLockReason;
    }

    /**
     * Returns the contents of the issue.
     *
     * @return the contents of the issue
     */
    public String getBody() {
        return body;
    }

    /**
     * Sets the contents of the issue.
     *
     * @param body the contents of the issue
     */
    public void setBody(String body) {
        this.body = body;
    }

    /**
     * Returns the reactions to the issue.
     *
     * @return the reactions to the issue
     */
    public Reaction getReactions() {
        return reactions;
    }

    /**
     * Sets the reactions to the issue.
     *
     * @param reactions the reactions to the issue
     */
    public void setReactions(Reaction reactions) {
        this.reactions = reactions;
    }

    /**
     * Returns the url for the timeline of this issue.
     *
     * @return the url for the timeline of this issue
     */
    public String getTimelineUrl() {
        return timelineUrl;
    }

    /**
     * Sets the url for the timeline of this issue.
     *
     * @param timelineUrl the url for the timeline of this issue
     */
    public void setTimelineUrl(String timelineUrl) {
        this.timelineUrl = timelineUrl;
    }

    /**
     * Returns whether this issue was created via the GitHub app.
     *
     * @return whether this issue was created via the GitHub app
     */
    public boolean isPerformedViaGithubApp() {
        return performedViaGithubApp;
    }

    /**
     * Sets whether this issue was created via the GitHub app.
     *
     * @param performedViaGithubApp whether this issue was created via the GitHub app
     */
    public void setPerformedViaGithubApp(boolean performedViaGithubApp) {
        this.performedViaGithubApp = performedViaGithubApp;
    }

    /**
     * Returns the reason for the issue being in the current state.
     *
     * @return the reason for the issue being in the current state
     */
    public String getStateReason() {
        return stateReason;
    }

    /**
     * Sets the reason for the issue being in the current state.
     *
     * @param stateReason the reason for the issue being in the current state
     */
    public void setStateReason(String stateReason) {
        this.stateReason = stateReason;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        } else if (!(o instanceof Issue)) {
            return false;
        }

        Issue other = (Issue) o;
        return other.url.equals(url)
                && other.repositoryUrl.equals(repositoryUrl)
                && other.labelsUrl.equals(labelsUrl)
                && other.commentsUrl.equals(commentsUrl)
                && other.eventsUrl.equals(eventsUrl)
                && other.htmlUrl.equals(htmlUrl)
                && other.id == id
                && other.nodeId.equals(nodeId)
                && other.number == number
                && other.title.equals(title)
                && other.user.equals(user)
                && other.labels.equals(labels)
                && other.state.equals(state)
                && other.locked == locked
                && other.assignee.equals(assignee)
                && other.assignees.equals(assignees)
                && other.milestone == milestone
                && other.comments == comments
                && other.createdAt.equals(createdAt)
                && other.updatedAt.equals(updatedAt)
                && other.closedAt.equals(closedAt)
                && other.authorAssociation.equals(authorAssociation)
                && Objects.equals(other.activeLockReason, activeLockReason)
                && other.body.equals(body)
                && other.reactions.equals(reactions)
                && other.timelineUrl.equals(timelineUrl)
                && other.performedViaGithubApp == performedViaGithubApp
                && other.stateReason.equals(stateReason);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        int ret = url.hashCode();

        ret = 31 * ret + repositoryUrl.hashCode();
        ret = 31 * ret + labelsUrl.hashCode();
        ret = 31 * ret + commentsUrl.hashCode();
        ret = 31 * ret + eventsUrl.hashCode();
        ret = 31 * ret + htmlUrl.hashCode();
        ret = 31 * ret + Integer.hashCode(id);
        ret = 31 * ret + nodeId.hashCode();
        ret = 31 * ret + Integer.hashCode(number);
        ret = 31 * ret + title.hashCode();
        ret = 31 * ret + user.hashCode();
        ret = 31 * ret + labels.hashCode();
        ret = 31 * ret + state.hashCode();
        ret = 31 * ret + Boolean.hashCode(locked);
        ret = 31 * ret + assignee.hashCode();
        ret = 31 * ret + assignees.hashCode();
        ret = 31 * ret + Boolean.hashCode(milestone);
        ret = 31 * ret + Integer.hashCode(comments);
        ret = 31 * ret + createdAt.hashCode();
        ret = 31 * ret + updatedAt.hashCode();
        ret = 31 * ret + closedAt.hashCode();
        ret = 31 * ret + authorAssociation.hashCode();
        ret = 31 * ret + activeLockReason.hashCode();
        ret = 31 * ret + body.hashCode();
        ret = 31 * ret + reactions.hashCode();
        ret = 31 * ret + timelineUrl.hashCode();
        ret = 31 * ret + Boolean.hashCode(performedViaGithubApp);
        ret = 31 * ret + stateReason.hashCode();

        return ret;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return "Issue{"
                + "url=\"" + url + "\""
                + ", repository_url=\"" + repositoryUrl + "\""
                + ", labels_url=\"" + labelsUrl + "\""
                + ", comments_url=\"" + commentsUrl + "\""
                + ", events_url=\"" + eventsUrl + "\""
                + ", html_url=\"" + htmlUrl + "\""
                + ", id=" + id + ", node_id=\"" + nodeId + "\""
                + ", number=" + number
                + ", title=\"" + title + "\""
                + ", user=" + user
                + ", labels=" + labels
                + ", state=\"" + state + "\""
                + ", locked=" + locked
                + ", assignee=" + assignee
                + ", assignees=" + assignees
                + ", milestone=" + milestone
                + ", comments=" + comments
                + ", created_at=\"" + createdAt + "\""
                + ", updated_at=\"" + updatedAt + "\""
                + ", closed_at=\"" + closedAt + "\""
                + ", author_association=\"" + authorAssociation + "\""
                + ", active_lock_reason=" + activeLockReason
                + ", body=\"" + body + "\""
                + ", reactions=" + reactions
                + ", timeline_url=\"" + timelineUrl + "\""
                + ", performed_via_github_app=" + performedViaGithubApp
                + ", state_reason=\"" + stateReason + "\""
                + "}";
    }
}
