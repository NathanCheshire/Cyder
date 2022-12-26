package cyder.parsers.remote.github;

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
    public LinkedList<String> labels;
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
