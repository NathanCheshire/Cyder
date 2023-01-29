package cyder.github.parsers;

import com.google.gson.annotations.SerializedName;
import cyder.logging.LogTag;
import cyder.logging.Logger;

import java.util.ArrayList;

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
     * The sequential number of this issue
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
    public boolean activeLockReason;

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

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getRepositoryUrl() {
        return repositoryUrl;
    }

    public void setRepositoryUrl(String repositoryUrl) {
        this.repositoryUrl = repositoryUrl;
    }

    public String getLabelsUrl() {
        return labelsUrl;
    }

    public void setLabelsUrl(String labelsUrl) {
        this.labelsUrl = labelsUrl;
    }

    public String getCommentsUrl() {
        return commentsUrl;
    }

    public void setCommentsUrl(String commentsUrl) {
        this.commentsUrl = commentsUrl;
    }

    public String getEventsUrl() {
        return eventsUrl;
    }

    public void setEventsUrl(String eventsUrl) {
        this.eventsUrl = eventsUrl;
    }

    public String getHtmlUrl() {
        return htmlUrl;
    }

    public void setHtmlUrl(String htmlUrl) {
        this.htmlUrl = htmlUrl;
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
        this.nodeId = nodeId;
    }

    public int getNumber() {
        return number;
    }

    public void setNumber(int number) {
        this.number = number;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public ArrayList<String> getLabels() {
        return labels;
    }

    public void setLabels(ArrayList<String> labels) {
        this.labels = labels;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public boolean isLocked() {
        return locked;
    }

    public void setLocked(boolean locked) {
        this.locked = locked;
    }

    public User getAssignee() {
        return assignee;
    }

    public void setAssignee(User assignee) {
        this.assignee = assignee;
    }

    public ArrayList<User> getAssignees() {
        return assignees;
    }

    public void setAssignees(ArrayList<User> assignees) {
        this.assignees = assignees;
    }

    public boolean isMilestone() {
        return milestone;
    }

    public void setMilestone(boolean milestone) {
        this.milestone = milestone;
    }

    public int getComments() {
        return comments;
    }

    public void setComments(int comments) {
        this.comments = comments;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public String getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(String updatedAt) {
        this.updatedAt = updatedAt;
    }

    public String getClosedAt() {
        return closedAt;
    }

    public void setClosedAt(String closedAt) {
        this.closedAt = closedAt;
    }

    public String getAuthorAssociation() {
        return authorAssociation;
    }

    public void setAuthorAssociation(String authorAssociation) {
        this.authorAssociation = authorAssociation;
    }

    public boolean isActiveLockReason() {
        return activeLockReason;
    }

    public void setActiveLockReason(boolean activeLockReason) {
        this.activeLockReason = activeLockReason;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public Reaction getReactions() {
        return reactions;
    }

    public void setReactions(Reaction reactions) {
        this.reactions = reactions;
    }

    public String getTimelineUrl() {
        return timelineUrl;
    }

    public void setTimelineUrl(String timelineUrl) {
        this.timelineUrl = timelineUrl;
    }

    public boolean isPerformedViaGithubApp() {
        return performedViaGithubApp;
    }

    public void setPerformedViaGithubApp(boolean performedViaGithubApp) {
        this.performedViaGithubApp = performedViaGithubApp;
    }

    public String getStateReason() {
        return stateReason;
    }

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
                && other.activeLockReason == activeLockReason
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
        ret = 31 * ret + Boolean.hashCode(activeLockReason);
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
