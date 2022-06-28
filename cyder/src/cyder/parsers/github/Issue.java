package cyder.parsers.github;

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
}
