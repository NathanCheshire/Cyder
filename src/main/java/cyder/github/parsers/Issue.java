package cyder.github.parsers;

import java.util.ArrayList;
import java.util.LinkedList;

/**
 * A json serialization class for a GitHub repo issue.
 */
@SuppressWarnings("unused")
public class Issue {
    public String url;
    public String repository_url;
    public String labels_url;
    public String comments_url;
    public String events_url;
    public String html_url;
    public int id;
    public String node_id;
    public int number;
    public String title;
    public User user;
    public ArrayList<String> labels;
    public String state;
    public boolean locked;
    public User assignee;
    public LinkedList<User> assignees;
    public boolean milestone;
    public int comments;
    public String created_at;
    public String updated_at;
    public String closed_at;
    public String author_association;
    public boolean active_lock_reason;
    public String body;
    public Reaction reactions;
    public String timeline_url;
    public boolean performed_via_github_app;
    public String state_reason;

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
                && other.repository_url.equals(repository_url)
                && other.labels_url.equals(labels_url)
                && other.comments_url.equals(comments_url)
                && other.events_url.equals(events_url)
                && other.html_url.equals(html_url)
                && other.id == id
                && other.node_id.equals(node_id)
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
                && other.created_at.equals(created_at)
                && other.updated_at.equals(updated_at)
                && other.closed_at.equals(closed_at)
                && other.author_association.equals(author_association)
                && other.active_lock_reason == active_lock_reason
                && other.body.equals(body)
                && other.reactions.equals(reactions)
                && other.timeline_url.equals(timeline_url)
                && other.performed_via_github_app == performed_via_github_app
                && other.state_reason.equals(state_reason);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        int ret = url.hashCode();

        ret = 31 * ret + repository_url.hashCode();
        ret = 31 * ret + labels_url.hashCode();
        ret = 31 * ret + comments_url.hashCode();
        ret = 31 * ret + events_url.hashCode();
        ret = 31 * ret + html_url.hashCode();
        ret = 31 * ret + Integer.hashCode(id);
        ret = 31 * ret + node_id.hashCode();
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
        ret = 31 * ret + created_at.hashCode();
        ret = 31 * ret + updated_at.hashCode();
        ret = 31 * ret + closed_at.hashCode();
        ret = 31 * ret + author_association.hashCode();
        ret = 31 * ret + Boolean.hashCode(active_lock_reason);
        ret = 31 * ret + body.hashCode();
        ret = 31 * ret + reactions.hashCode();
        ret = 31 * ret + timeline_url.hashCode();
        ret = 31 * ret + Boolean.hashCode(performed_via_github_app);
        ret = 31 * ret + state_reason.hashCode();

        return ret;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return "Issue{"
                + "url=\"" + url + "\""
                + ", repository_url=\"" + repository_url + "\""
                + ", labels_url=\"" + labels_url + "\""
                + ", comments_url=\"" + comments_url + "\""
                + ", events_url=\"" + events_url + "\""
                + ", html_url=\"" + html_url + "\""
                + ", id=" + id + ", node_id=\"" + node_id + "\""
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
                + ", created_at=\"" + created_at + "\""
                + ", updated_at=\"" + updated_at + "\""
                + ", closed_at=\"" + closed_at + "\""
                + ", author_association=\"" + author_association + "\""
                + ", active_lock_reason=" + active_lock_reason
                + ", body=\"" + body + "\""
                + ", reactions=" + reactions
                + ", timeline_url=\"" + timeline_url + "\""
                + ", performed_via_github_app=" + performed_via_github_app
                + ", state_reason=\"" + state_reason + "\""
                + "}";
    }
}
